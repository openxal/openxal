# The SCL Longitudinal Tune-Up rescaling controller
# It will scan rescale the phases of all SCL cavities
# according to the user input

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing import JTable
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension
from java.beans import PropertyChangeListener

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.ca import ChannelFactory

from xal.extension.widgets.swing import Wheelswitch

from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#    Online Model for phase analysis
#------------------------------------------------------------------------
class SCL_RfGaps_Fitter_Tracker_Model:
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		self.part_tracker = AlgorithmFactory.createParticleTracker(self.scl_accSeq)
		self.part_tracker.setRfGapPhaseCalculation(true)
		self.part_probe_init = ProbeFactory.createParticleProbe(self.scl_accSeq,self.part_tracker)
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		self.scenario = scl_long_tuneup_phase_analysis_controller.scl_one_cavity_tracker_model.scenario
		if(self.scenario == null):
			self.scenario = Scenario.newScenarioFor(self.scl_accSeq)
			self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
			self.scenario.resync()
		else:
			self.scenario.unsetStartNode()
			self.scenario.unsetStopNode()			
		# in the dictionary we will have 
		# cav_wrappers_param_dict[cav_wrapper] = [cavAmp,phase]
		self.cav_wrappers_param_dict = {}
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		self.cav_amp_phase_dict = {}
		for cav_wrapper in cav_wrappers:
			amp = cav_wrapper.cav.getDfltCavAmp()
			phase = cav_wrapper.cav.getDfltCavPhase()
			self.cav_amp_phase_dict[cav_wrapper] = [amp,phase]
		#------ Make rf gap arrays for each cavity. 
		#------ The elements are IdealRfGap instances not AcceleratorNode. 
		#------ self.cavToGapsDict has {cav_name:[irfGaps]}
		rfGaps = self.scl_accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cavToGapsDict = {}		
		for cav_wrapper in cav_wrappers:
			self.cavToGapsDict[cav_wrapper] = []
			for rfGap in rfGaps:
				if(rfGap.getId().find(cav_wrapper.cav.getId()) >= 0):
					irfGaps = self.scenario.elementsMappedTo(rfGap)
					self.cavToGapsDict[cav_wrapper].append(irfGaps[0])				
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_PHASE,phase)
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_ETL,val)
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_E0,val)
		#self.scenario.setModelInput(quad,ElectromagnetPropertyAccessor,PROPERTY_FIELD,val)		
		#----------------------------------------------------------------
		self.modelArrivalTimesIsReady = false
		self.new_cav_amp_phase_dict = {}
		for cav_wrapper in cav_wrappers:
			self.new_cav_amp_phase_dict[cav_wrapper] = self.cav_amp_phase_dict[cav_wrapper][:]
		
	def restoreInitAmpPhases(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			[amp,phase] = self.cav_amp_phase_dict[cav_wrapper]
			cav_wrapper.cav.updateDesignAmp(amp)
			cav_wrapper.cav.updateDesignPhase(phase)

	def getAvgGapPhase(self,cav_wrapper):
		#------------- calculate avg. RF gap phase -----------
		if(cav_wrapper == null or not(self.cavToGapsDict.has_key(cav_wrapper))): return 0.
		rf_gap_arr = self.cavToGapsDict[cav_wrapper]
		phase_rf_gaps_avg = 0.
		for irfGap in rf_gap_arr:
			phase_rf_gaps_avg += makePhaseNear(irfGap.getPhase(),0.)
			#print "debug gap=",irfGap.getId()," phase=",irfGap.getPhase()*180./math.pi
		phase_rf_gaps_avg /= len(rf_gap_arr)
		phase_rf_gaps_avg = makePhaseNear((phase_rf_gaps_avg*180./math.pi)%360.,0.)
		return phase_rf_gaps_avg
			
	def setRescaledModelCavityAmplitudes(self):
		cav_amp_low_limit = 0.5
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		amp_design_coeff_avg = 0.
		count = 0
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				amp_design_coeff_avg += cav_wrapper.designAmp/cav_wrapper.initLiveAmp
				count += 1
		if(count > 1): amp_design_coeff_avg /= count
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				amp_coeff = cav_wrapper.rescaleBacket.liveAmp/cav_wrapper.initLiveAmp
				cav_wrapper.rescaleBacket.designAmp = amp_coeff*cav_wrapper.designAmp
			else:
				if(cav_wrapper.rescaleBacket.liveAmp > cav_amp_low_limit):
					cav_wrapper.rescaleBacket.designAmp = amp_design_coeff_avg*cav_wrapper.rescaleBacket.liveAmp
				else:
					cav_wrapper.rescaleBacket.designAmp = 0.
		for cav_wrapper in cav_wrappers:
			cav_wrapper.cav.updateDesignAmp(cav_wrapper.rescaleBacket.designAmp)
			cav_wrapper.cav.updateDesignPhase(cav_wrapper.rescaleBacket.designPhase)
				
	def setAnalysisModelCavityAmplitudes(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.cav.updateDesignAmp(cav_wrapper.designAmp)	
				cav_wrapper.cav.updateDesignPhase(cav_wrapper.designPhase)
				#print "debug cav=", cav_wrapper.alias," amp=",cav_wrapper.cav.getDfltCavAmp()," phase=",cav_wrapper.cav.getDfltCavPhase()					
			else:
				cav_wrapper.cav.updateDesignAmp(0.)
				cav_wrapper.cav.updateDesignPhase(0.)
			
	def isModelReady(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				if(not (cav_wrapper.isMeasured and cav_wrapper.isAnalyzed)):
					return false
		return true
			
	def initTrackingModelAndArrivalTimes(self):
		# It fills out the arrival time for the our base case 
		# It also returns the energy
		self.setAnalysisModelCavityAmplitudes()		
		part_probe = ParticleProbe(self.part_probe_init)
		eKin_in = self.scl_long_tuneup_controller.cav_wrappers[0].eKin_in
		part_probe.setKineticEnergy(eKin_in*1.0e+6)
		self.scenario.setProbe(part_probe)	
		self.scenario.resync()
		self.scenario.run()
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(self.cavToGapsDict.has_key(cav_wrapper)):
				irfGap = self.cavToGapsDict[cav_wrapper][0]
				state = self.scenario.getTrajectory().stateForElement(irfGap.getId())
				cav_wrapper.rescaleBacket.arrivalTime = state.getTime()
				self.new_cav_amp_phase_dict[cav_wrapper][1] = cav_wrapper.designPhase
		self.modelArrivalTimesIsReady = true
		self.restoreInitAmpPhases()
		return self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
			
	def calcForNewAmpsAndPhases(self):
		self.setRescaledModelCavityAmplitudes()
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			amp = cav_wrapper.rescaleBacket.designAmp
			phase = self.new_cav_amp_phase_dict[cav_wrapper][1]
			self.new_cav_amp_phase_dict[cav_wrapper] = [amp,phase]
		self.scenario.resync()
		#---set up phases : we should reproduce the rf gap avg. phases from the proxies
		max_limit_phase_diff = 0.01
		max_phase_diff = 1.0
		min_cav_amp = 0.5
		max_iter = 30
		it = 0
		while(max_phase_diff > max_limit_phase_diff):
			it += 1
			for cav_wrapper in cav_wrappers:
				 irfGap = self.cavToGapsDict[cav_wrapper][0]
				 phase = self.new_cav_amp_phase_dict[cav_wrapper][1]
				 irfGap.setPhase(phase*math.pi/180.)
			part_probe = ParticleProbe(self.part_probe_init)
			eKin_in = self.scl_long_tuneup_controller.cav_wrappers[0].eKin_in
			part_probe.setKineticEnergy(eKin_in*1.0e+6)
			self.scenario.setProbe(part_probe)	
			self.scenario.run()
			max_phase_diff = 0.0
			for cav_wrapper in cav_wrappers:
				phase_rf_gaps_avg = self.getAvgGapPhase(cav_wrapper)
				phase_diff = phase_rf_gaps_avg - cav_wrapper.rescaleBacket.avg_gap_phase
				#print "debug cav=",cav_wrapper.alias," phase_diff=",phase_diff," phase avg =",phase_rf_gaps_avg," design avg=",cav_wrapper.avg_gap_phase
				#print "debug ======= amp=",cav_wrapper.cav.getDfltCavAmp()," phase=",self.cavToGapsDict[cav_wrapper][0].getPhase()*180./math.pi
				if(cav_wrapper.rescaleBacket.designAmp > min_cav_amp):
					self.new_cav_amp_phase_dict[cav_wrapper][1] -= phase_diff/1.3
					phase_diff = math.fabs(phase_diff)
					if(max_phase_diff < phase_diff): max_phase_diff = phase_diff
			eKin_out = self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6	
			#print "debug eKin_out=",eKin_out
			if(it > max_iter): break	
		#---- update the cavities' new model phases
		for cav_wrapper in cav_wrappers:
			phase = self.new_cav_amp_phase_dict[cav_wrapper][1]
			cav_wrapper.rescaleBacket.designPhase = makePhaseNear(phase,0.)
		#print "debug iter=",it
		eKin_out = self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		init_amp_phases_panel.energy_text.setValue(eKin_out)	
		self.restoreInitAmpPhases()
		return eKin_out
		
	def calculateNewLivePhases(self):
		self.calcForNewAmpsAndPhases()
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			arr_time_old = cav_wrapper.rescaleBacket.arrivalTime
			irfGap = self.cavToGapsDict[cav_wrapper][0]
			cav_wrapper.rescaleBacket.designPhase = irfGap.getPhase()*180./math.pi
			state = self.scenario.getTrajectory().stateForElement(irfGap.getId())
			arr_time_new = state.getTime()
			freq = 1.0e+6*cav_wrapper.cav.getCavFreq()
			# this sign "-" is correct. It was checked experimentally. 
			delta_phase = - makePhaseNear(360.*freq*(arr_time_new - arr_time_old),0.)
			delta_phase_model = cav_wrapper.rescaleBacket.designPhase - cav_wrapper.designPhase
			cav_wrapper.rescaleBacket.livePhase = makePhaseNear(delta_phase + delta_phase_model + cav_wrapper.livePhase,0.)
	
#------------------------------------------------------------------------
#           Auxiliary Rescaling classes and functions
#------------------------------------------------------------------------	
class BPMs_Shift_Measure_Runner(Runnable):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
	
	def run(self):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		bpms_phase_shift_panel = scl_long_tuneup_rescale_controller.bpms_phase_shift_panel		
		bpms_phase_shift_panel.old_phases_text.setText("")
		bpm1_avg_phase = 0.
		bpm2_avg_phase = 0.
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		bpm_wrapper1 = cav_wrapper.bpm_wrappers[0]
		bpm_wrapper2 = cav_wrapper.bpm_wrappers[1]
		if(cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper1) and cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper2)):
			(graphDataAmp1,graphDataPhase1) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper1]			
			(graphDataAmp2,graphDataPhase2) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper2]
			n_points1 = graphDataPhase1.getNumbOfPoints()
			n_points2 = graphDataPhase2.getNumbOfPoints()
			for ip in range(n_points1):
				bpm1_avg_phase +=  graphDataPhase1.getY(ip)
			for ip in range(n_points2):
				bpm2_avg_phase +=  graphDataPhase2.getY(ip)
			if(n_points1 > 0): bpm1_avg_phase /= n_points1
			if(n_points2 > 0): bpm2_avg_phase /= n_points2
		bpms_phase_shift_panel.old_phases_text.setText("%+4.1f  /  %+4.1f"%(bpm1_avg_phase,bpm2_avg_phase))
		result = self.run_measurement(bpm1_avg_phase,bpm2_avg_phase)
		if(not result):
			self.scl_long_tuneup_controller.getMessageTextField().setText("Cannot measure the BPM00a/b phases!")

	def run_measurement(self,bpm1_avg_phase_old,bpm2_avg_phase_old):
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		bpms_phase_shift_panel = scl_long_tuneup_rescale_controller.bpms_phase_shift_panel
		measure_stopper = bpms_phase_shift_panel.measure_stopper
		measure_stopper.shouldStop = false		
		n_iter = int(bpms_phase_shift_panel.iter_measuremen_text.getValue())
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		bpm_wrapper1 = cav_wrapper.bpm_wrappers[0]
		bpm_wrapper2 = cav_wrapper.bpm_wrappers[1]
		ch_phase1 = ChannelFactory.defaultFactory().getChannel(bpm_wrapper1.bpm.getId()+":phaseAvg")
		ch_phase2 = ChannelFactory.defaultFactory().getChannel(bpm_wrapper2.bpm.getId()+":phaseAvg")
		ch_phase1.connectAndWait(0.5)
		ch_phase2.connectAndWait(0.5)
		phase1_arr = []
		phase2_arr = []
		count = 0
		while(count < n_iter):
			if(measure_stopper.shouldStop): break
			time.sleep(1.2)
			phase1_arr.append(ch_phase1.getValDbl())
			phase2_arr.append(ch_phase2.getValDbl())
			if(measure_stopper.shouldStop): break
			count += 1
			if(measure_stopper.shouldStop): break
			bpms_phase_shift_panel.iter_measuremen_text.setValue(float(n_iter-count))
		bpm1_avg_phase = 0.
		bpm2_avg_phase = 0.
		for phase in 	phase1_arr:
			bpm1_avg_phase += phase/len(phase1_arr)
		for phase in 	phase2_arr:
			bpm2_avg_phase += phase/len(phase2_arr)
		bpms_phase_shift_panel.new_phases_text.setText("%+4.1f  /  %+4.1f"%(bpm1_avg_phase,bpm2_avg_phase))
		bpm_numb = 0
		phase_diff = 0.
		if(bpm_wrapper1.isGood):
			phase_diff += makePhaseNear(bpm1_avg_phase - bpm1_avg_phase_old,0.)
			bpm_numb += 1
		if(bpm_wrapper2.isGood):
			phase_diff += makePhaseNear(bpm2_avg_phase - bpm2_avg_phase_old,0.)
			bpm_numb += 1	
		if(bpm_numb > 0):
			phase_diff /= bpm_numb
		bpms_phase_shift_panel.phase_shift_text.setValue(phase_diff)
		bpms_phase_shift_panel.iter_measuremen_text.setValue(float(n_iter))
		if(bpm_numb == 0 or count == 0): return false
		return true
	
class Measure_Stopper:
	def __init__(self):
		self.shouldStop = false

class RF_Gap_Phases_Holder_for_Table_Selection:
	# This is a class that keeps base values of the RF gap phases for the cavity table selection
	# It is used for Wheel functionality
	def __init__(self,scl_long_tuneup_controller,index_start,index_end):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.index_start = index_start
		self.index_end = index_end
		self.n_points = index_end - index_start + 1
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		self.scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		self.rf_gap_phase_arr = []
		for ip in range(self.n_points):
			cav_ind = ip + self.index_start
			cav_wrapper = cav_wrappers[cav_ind]
			self.rf_gap_phase_arr.append(cav_wrapper.rescaleBacket.avg_gap_phase)
			
	def setNewPhases(self,shift_phase_val):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for ip in range(self.n_points):
			cav_ind = ip + self.index_start
			cav_wrapper = cav_wrappers[cav_ind]
			phase_ini = self.rf_gap_phase_arr[ip]
			cav_wrapper.rescaleBacket.avg_gap_phase = phase_ini + shift_phase_val

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class BPMs_Phase_Shift_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPM00a/b Phase Shift Measurements")
		self.setBorder(titled_border)	
		#---- measurement stopper
		self.measure_stopper = Measure_Stopper()
		#--- buttons------------
		start_measurement_button = JButton("Start")
		start_measurement_button.addActionListener(Start_BPMs_Shift_Measurements_Listener(self.scl_long_tuneup_controller))	
		stop_measurement_button = JButton("Stop")
		stop_measurement_button.addActionListener(Stop_BPMs_Shift_Measurements_Listener(self.scl_long_tuneup_controller))	
		iter_measurement_lbl = JLabel("Iteration=",JLabel.RIGHT)
		self.iter_measuremen_text = DoubleInputTextField(10.,FortranNumberFormat("F3.0"),4)
		bpm_names_lbl = JLabel(" Phases BPM00a/b ",JLabel.RIGHT)
		old_phases_lbl = JLabel("Old=",JLabel.RIGHT)
		self.old_phases_text = JTextField(12)
		new_phases_lbl = JLabel("  New=",JLabel.RIGHT)
		self.new_phases_text = JTextField(12)
		phase_shift_lbl = JLabel("  Phase Shift[deg]=",JLabel.RIGHT)
		self.phase_shift_text = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),6)		
		#---- buttons panel
		buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		buttons_panel.add(start_measurement_button)
		buttons_panel.add(stop_measurement_button)
		buttons_panel.add(iter_measurement_lbl)
		buttons_panel.add(self.iter_measuremen_text)
		buttons_panel.add(bpm_names_lbl)
		buttons_panel.add(old_phases_lbl)
		buttons_panel.add(self.old_phases_text)
		buttons_panel.add(new_phases_lbl)
		buttons_panel.add(self.new_phases_text)
		buttons_panel.add(phase_shift_lbl)
		buttons_panel.add(self.phase_shift_text)
		#---- add to the main subpanel
		self.add(buttons_panel)
		
class Init_New_Amps_Phases_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"SetUp Cavities' New Design Amplitudes and Phases")
		self.setBorder(titled_border)	
		#--- buttons------------
		init_model_button = JButton("Init SCL Model")
		init_model_button.addActionListener(Init_Tracking_Model_Listener(self.scl_long_tuneup_controller))			
		read_epics_button = JButton("<html>Get New Live A<SUB>EPICS</SUB><html>")		
		read_epics_button.addActionListener(Read_From_EPICS_Listener(self.scl_long_tuneup_controller))	
		copy_old_button = JButton("<html>Copy Old (A<SUB>EPICS</SUB>,&Delta;&phi;<SUB>RF</SUB>)->New (A<SUB>EPICS</SUB>,&Delta;&phi<SUB>RF</SUB>;)<html>")
		copy_old_button.addActionListener(Copy_Old_to_New_Listener(self.scl_long_tuneup_controller))	
		change_selected_lbl = JLabel("<html>Change Selected New &Delta;&phi;<SUB>RF</SUB>(deg)<html>",JLabel.RIGHT)
		self.avg_gap_phase_wheel = Wheelswitch()
		self.avg_gap_phase_wheel.setFormat("+###.#")
		self.avg_gap_phase_wheel.setValue(0.)
		self.avg_gap_phase_wheel.addPropertyChangeListener("value", Wheel_Listener(self.scl_long_tuneup_controller))	
		self.is_wheel_listen = true
		#---- buttons panel
		buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		buttons_panel.add(init_model_button)
		buttons_panel.add(read_epics_button)
		buttons_panel.add(copy_old_button)
		buttons_panel.add(change_selected_lbl)
		buttons_panel.add(self.avg_gap_phase_wheel)
		#---- the energy panel
		energy_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		energy_calc_button = JButton("<html>Calculate E<SUB>kin</SUB><html>")
		energy_calc_button.addActionListener(Calculate_Energy_Listener(self.scl_long_tuneup_controller))			
		energy_lbl = JLabel("<html>SCL Final E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.energy_text = DoubleInputTextField(0.,FortranNumberFormat("G10.6"),12)
		energy_panel.add(energy_calc_button)
		energy_panel.add(energy_lbl)
		energy_panel.add(self.energy_text)
		#---- add to the main subpanel
		self.add(buttons_panel,BorderLayout.NORTH)
		self.add(energy_panel,BorderLayout.SOUTH)
		#---- this is a holder for RF_Gap_Phases_Holder_for_Table_Selection
		self.rf_gap_phases_holder = null
		
		
class NEW_Amp_and_Phases_to_EPICS_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"Calculate New Live Phases and Upload")
		self.setBorder(titled_border)	
		#--- buttons------------
		calculate_button = JButton("<html>Calculate New &phi;<SUB>live</SUB><html>")
		calculate_button.addActionListener(Calculate_New_Live_Listener(self.scl_long_tuneup_controller))	
		send_phases_button = JButton("<html>Send New &phi;<SUB>live</SUB> to EPICS<html>")
		send_phases_button.addActionListener(Send_Phases_to_EPICS_Listener(self.scl_long_tuneup_controller))		
		#---- buttons panel
		buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		buttons_panel.add(calculate_button)
		buttons_panel.add(send_phases_button)
		#---- add to the main subpanel
		self.add(buttons_panel,BorderLayout.NORTH)

#------------------------------------------------
#  JTable models
#------------------------------------------------

class Cavs_Rescale_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		# Delta Phi RF are actually the average RF gap phases!
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cavity","Use"]
		self.columnNames += ["<html>Old A<SUB>EPICS</SUB>(MV)<html>",]
		self.columnNames += ["<html>New A<SUB>EPICS</SUB>(MV)<html>",]
		self.columnNames += ["<html>A<SUB>New/Old</SUB>(%)<html>",]
		self.columnNames += ["<html>Old &Delta;&phi;<SUB>RF</SUB>(deg)<html>",]
		self.columnNames += ["<html>New &Delta;&phi;<SUB>RF</SUB>(deg)<html>",]	
		self.columnNames += ["<html>Old &phi;<SUB>model</SUB>(deg)<html>",]
		self.columnNames += ["<html>New &phi;<SUB>model</SUB>(deg)<html>",]
		self.columnNames += ["<html>Old &phi;<SUB>live</SUB>(deg)<html>",]
		self.columnNames += ["<html>New &phi;<SUB>live</SUB>(deg)<html>",]		
		self.nf3 = NumberFormat.getInstance()	
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.scl_long_tuneup_controller.cav_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		rescaleBacket = cav_wrapper.rescaleBacket
		if(col == 0): 
			return cav_wrapper.alias
		if(col == 1): return cav_wrapper.isGood	
		if(not (cav_wrapper.isMeasured and cav_wrapper.isAnalyzed)): return ""
		if(col == 2): return "%6.3f"%cav_wrapper.initLiveAmp
		if(col == 3): return "%6.3f"%rescaleBacket.liveAmp
		if(col == 4): return "%5.1f"%(100.*rescaleBacket.liveAmp/cav_wrapper.initLiveAmp)
		if(col == 5): return "%+6.2f"%cav_wrapper.avg_gap_phase
		if(col == 6): return "%+6.2f"%rescaleBacket.avg_gap_phase
		if(col == 7): return "%+6.2f"%cav_wrapper.designPhase
		if(col == 8): return "%+6.2f"%rescaleBacket.designPhase
		if(col == 9): return "%+6.2f"%cav_wrapper.livePhase
		if(col == 10): return "%+6.2f"%rescaleBacket.livePhase
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]		
		if(col == 3 and cav_wrapper.isMeasured and cav_wrapper.isAnalyzed):
			return true
		if(col == 6 and cav_wrapper.isMeasured and cav_wrapper.isAnalyzed):
			return true		
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		if(col == 3 and cav_wrapper.isMeasured and cav_wrapper.isAnalyzed):
			 cav_wrapper.rescaleBacket.liveAmp = Double.parseDouble(value)
			 self.fireTableCellUpdated(row,col+1)
		if(col == 6 and cav_wrapper.isMeasured and cav_wrapper.isAnalyzed):
			 cav_wrapper.rescaleBacket.avg_gap_phase = Double.parseDouble(value)
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		scl_tracker_model.calcForNewAmpsAndPhases()		


#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		init_amp_phases_panel.rf_gap_phases_holder = null
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index_min = listSelectionModel.getMinSelectionIndex()	
		index_max = listSelectionModel.getMaxSelectionIndex()
		if(index_max < 0): return
		init_amp_phases_panel.is_wheel_listen = false
		avg_gap_phase_wheel = init_amp_phases_panel.avg_gap_phase_wheel
		avg_gap_phase_wheel.setValue(0.)
		init_amp_phases_panel.is_wheel_listen = true
		init_amp_phases_panel.rf_gap_phases_holder = RF_Gap_Phases_Holder_for_Table_Selection(self.scl_long_tuneup_controller,index_min,index_max)

class Start_BPMs_Shift_Measurements_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		self.startMeasurement()

	def startMeasurement(self):
		runner = BPMs_Shift_Measure_Runner(self.scl_long_tuneup_controller)
		thr = Thread(runner)
		thr.start()	

class Stop_BPMs_Shift_Measurements_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		bpms_phase_shift_panel = scl_long_tuneup_rescale_controller.bpms_phase_shift_panel
		bpms_phase_shift_panel.measure_stopper.shouldStop = true

class Init_Tracking_Model_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return
		eKin_out = scl_tracker_model.initTrackingModelAndArrivalTimes()
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		init_amp_phases_panel.energy_text.setValue(eKin_out)

class Read_From_EPICS_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.rescaleBacket.liveAmp = cav_wrapper.cav.getCavAmpSetPoint()
				cav_wrapper.rescaleBacket.avg_gap_phase = cav_wrapper.avg_gap_phase
		scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Copy_Old_to_New_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.rescaleBacket.liveAmp = cav_wrapper.initLiveAmp
				cav_wrapper.rescaleBacket.avg_gap_phase = cav_wrapper.avg_gap_phase
		scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()		
		
class Calculate_Energy_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		scl_tracker_model.calcForNewAmpsAndPhases()
		scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Wheel_Listener(PropertyChangeListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def propertyChange(self,event):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		if(init_amp_phases_panel.rf_gap_phases_holder == null): return 
		if(not init_amp_phases_panel.is_wheel_listen): return
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return
		avg_gap_phase_wheel = init_amp_phases_panel.avg_gap_phase_wheel
		val = avg_gap_phase_wheel.getValue()
		init_amp_phases_panel.rf_gap_phases_holder.setNewPhases(val)
		scl_tracker_model.calcForNewAmpsAndPhases()
		ind_start = init_amp_phases_panel.rf_gap_phases_holder.index_start
		ind_end = init_amp_phases_panel.rf_gap_phases_holder.index_end
		for ind in range(ind_start,ind_end+1):
			scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableCellUpdated(ind,6)
			scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableCellUpdated(ind,8)

class Calculate_New_Live_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		scl_tracker_model.calculateNewLivePhases()
		scl_long_tuneup_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Send_Phases_to_EPICS_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("This actions is not implemented yet!")
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_rescale_controller.init_amp_phases_panel
		scl_tracker_model = scl_long_tuneup_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		bpms_phase_shift_panel = scl_long_tuneup_rescale_controller.bpms_phase_shift_panel
		# the frequency of BPMs in SCL is 0.5 of the frequency of RF cavities
		phase_shift_global = 2*bpms_phase_shift_panel.phase_shift_text.getValue()
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				new_cav_phase = makePhaseNear(cav_wrapper.rescaleBacket.livePhase + phase_shift_global,0.)
				cav_wrapper.cav.setCavPhase(new_cav_phase)
				#time.sleep(0.02)
				
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_Rescale_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		self.bpms_phase_shift_panel = BPMs_Phase_Shift_Panel(self.scl_long_tuneup_controller)
		self.init_amp_phases_panel = Init_New_Amps_Phases_Panel(self.scl_long_tuneup_controller)
		self.new_amp_phases_to_epics_panel = NEW_Amp_and_Phases_to_EPICS_Panel(self.scl_long_tuneup_controller)
		top_panel.add(self.bpms_phase_shift_panel,BorderLayout.NORTH)
		top_panel.add(self.init_amp_phases_panel,BorderLayout.CENTER)
		top_panel.add(self.new_amp_phases_to_epics_panel,BorderLayout.SOUTH)
		#------cavities table panel --------
		cavs_rescale_panel = JPanel(BorderLayout())
		txt = "Cavities' Prameters. New Amp and Avg. Gap Phases can be changed manually."
		rescale_table_border = BorderFactory.createTitledBorder(etched_border,txt)
		cavs_rescale_panel.setBorder(rescale_table_border)		
		self.cavs_rescale_table = JTable(Cavs_Rescale_Table_Model(self.scl_long_tuneup_controller))
		self.cavs_rescale_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.cavs_rescale_table.setFillsViewportHeight(true)
		self.cavs_rescale_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self.scl_long_tuneup_controller))		
		scrl_panel0 = JScrollPane(self.cavs_rescale_table)			
		cavs_rescale_panel.add(scrl_panel0,BorderLayout.CENTER)
		center_panel = JPanel(BorderLayout())
		center_panel.add(cavs_rescale_panel,BorderLayout.CENTER)
		#-------- post-rescale actions panel
		bottom_panel = JPanel(BorderLayout())
		#bottom_panel.add(self.post_rescale_panel,BorderLayout.WEST)
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		#----- model for tracking 	
		self.scl_tracker_model = SCL_RfGaps_Fitter_Tracker_Model(self.scl_long_tuneup_controller)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		self.cavs_rescale_table.getModel().fireTableDataChanged()
