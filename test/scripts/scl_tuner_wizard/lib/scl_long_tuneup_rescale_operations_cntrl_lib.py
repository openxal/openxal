# The SCL Longitudinal Tune-Up rescaling controller for operations.
# It is a strip down variant of the expert rescale controller.
# It will scan rescale the phases of all SCL cavities
# according to the user input

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension
from java.beans import PropertyChangeListener

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
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
		cav_wrappers[len(cav_wrappers)-1].rescaleBacket.eKin_out = eKin_out 
		eKin_in = self.scenario.getTrajectory().initialState().getKineticEnergy()/1.0e+6
		cav_wrappers[0].rescaleBacket.eKin_in = eKin_in 
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
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
		#---- set energies in and out
		for cav_wrapper_ind in range(len(cav_wrappers)-1):
			cav_wrapper0 = cav_wrappers[cav_wrapper_ind]
			cav_wrapper1 = cav_wrappers[cav_wrapper_ind+1]
			irfGaps0 = self.cavToGapsDict[cav_wrapper0]
			irfGaps1 = self.cavToGapsDict[cav_wrapper1]
			eKin_out_0 = self.scenario.getTrajectory().stateForElement(irfGaps0[len(irfGaps0)-1].getId()).getKineticEnergy()/1.0e+6
			cav_wrapper0.rescaleBacket.eKin_out = eKin_out_0
			cav_wrapper1.rescaleBacket.eKin_in = eKin_out_0
	
#------------------------------------------------------------------------
#           Auxiliary Rescaling classes and functions
#------------------------------------------------------------------------	
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
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		self.scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
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
		copy_old_button = JButton("Restore Synch. Phases")
		copy_old_button.addActionListener(Copy_Old_to_New_Listener(self.scl_long_tuneup_controller))
		alanysis_final_energy_lbl = JLabel("<html>   Phase Analysis SCL Final E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.analysis_final_energy_text = DoubleInputTextField(0.,DecimalFormat("####.#####"),12)
		#---- wheelswitch setup
		change_selected_lbl = JLabel("<html>Change Selected New &Delta;&phi;<SUB>Synch</SUB>(deg)<html>",JLabel.RIGHT)
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
		buttons_panel.add(alanysis_final_energy_lbl)
		buttons_panel.add(self.analysis_final_energy_text)
		#---- wheel panle
		wheel_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		wheel_panel.add(change_selected_lbl)
		wheel_panel.add(self.avg_gap_phase_wheel)
		#---- the energy panel
		energy_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		energy_calc_button = JButton("<html>Calculate E<SUB>kin</SUB><html>")
		energy_calc_button.addActionListener(Calculate_Energy_Listener(self.scl_long_tuneup_controller))			
		energy_lbl = JLabel("<html>SCL Final E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.energy_text = DoubleInputTextField(0.,DecimalFormat("####.#####"),12)
		energy_panel.add(energy_calc_button)
		energy_panel.add(energy_lbl)
		energy_panel.add(self.energy_text)
		#---- add to the main subpanel
		self.add(buttons_panel,BorderLayout.NORTH)
		self.add(energy_panel,BorderLayout.CENTER)
		self.add(wheel_panel,BorderLayout.SOUTH)
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
		calculate_button = JButton("<html>Calculate New &phi;<SUB>EPICS</SUB><html>")
		calculate_button.addActionListener(Calculate_New_Live_Listener(self.scl_long_tuneup_controller))	
		send_phases_button = JButton("<html>Send New &phi;<SUB>EPICS</SUB> to EPICS<html>")
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
		self.columnNames += ["<html>Old &Delta;&phi;<SUB>Synch</SUB>(deg)<html>",]
		self.columnNames += ["<html>New &Delta;&phi;<SUB>Synch</SUB>(deg)<html>",]	
		self.columnNames += ["<html>Old &phi;<SUB>EPICS</SUB>(deg)<html>",]
		self.columnNames += ["<html>New &phi;<SUB>EPICS</SUB>(deg)<html>",]		
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
		if(col == 7): return "%+6.2f"%cav_wrapper.livePhase
		if(col == 8): return "%+6.2f"%rescaleBacket.livePhase
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
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
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
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
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

class Init_Tracking_Model_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return
		eKin_out = scl_tracker_model.initTrackingModelAndArrivalTimes()
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
		init_amp_phases_panel.energy_text.setValue(eKin_out)
		#-----------------------------------------
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		cav_wrapper = cav_wrappers[len(cav_wrappers)-1]
		analysis_model_eKin_out = cav_wrapper.model_eKin_out
		init_amp_phases_panel.analysis_final_energy_text.setValue(analysis_model_eKin_out)

class Read_From_EPICS_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.rescaleBacket.liveAmp = cav_wrapper.cav.getCavAmpSetPoint()
				cav_wrapper.rescaleBacket.avg_gap_phase = cav_wrapper.avg_gap_phase
		scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Copy_Old_to_New_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.rescaleBacket.avg_gap_phase = cav_wrapper.avg_gap_phase
		scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()		
		
class Calculate_Energy_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		scl_tracker_model.calcForNewAmpsAndPhases()
		scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Wheel_Listener(PropertyChangeListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def propertyChange(self,event):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
		if(init_amp_phases_panel.rf_gap_phases_holder == null): return 
		if(not init_amp_phases_panel.is_wheel_listen): return
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
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
			scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableCellUpdated(ind,6)
			scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableCellUpdated(ind,8)

class Calculate_New_Live_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		scl_tracker_model.calculateNewLivePhases()
		scl_long_tuneup_operations_rescale_controller.cavs_rescale_table.getModel().fireTableDataChanged()
		
class Send_Phases_to_EPICS_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("This actions is not implemented yet!")
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_rescale_controller = self.scl_long_tuneup_controller.scl_long_tuneup_rescale_controller
		scl_long_tuneup_operations_rescale_controller = scl_long_tuneup_rescale_controller.scl_long_tuneup_operations_rescale_controller		
		init_amp_phases_panel = scl_long_tuneup_operations_rescale_controller.init_amp_phases_panel
		scl_tracker_model = scl_long_tuneup_operations_rescale_controller.scl_tracker_model
		if(not scl_tracker_model.isModelReady()):
			txt = "The data for the SCL model is not ready! Go to Phase Analysis!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		if(not scl_tracker_model.modelArrivalTimesIsReady):
			txt = "The model was not initialized! Hit the Init SCL Model button!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
			return		
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				new_cav_phase = makePhaseNear(cav_wrapper.rescaleBacket.livePhase,0.)
				cav = cav_wrapper.cav
				phase_tmp = new_cav_phase
				if False and (cav.getId().find("23d") >= 0 or int(cav.getId()[10:12]) >= 31):
					phase_tmp = - phase_tmp
				cav_wrapper.cav.setCavPhase(phase_tmp)
				#time.sleep(0.02)

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_Operations_Rescale_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		self.init_amp_phases_panel = Init_New_Amps_Phases_Panel(self.scl_long_tuneup_controller)
		self.new_amp_phases_to_epics_panel = NEW_Amp_and_Phases_to_EPICS_Panel(self.scl_long_tuneup_controller)
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
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		#----- model for tracking 	
		self.scl_tracker_model = SCL_RfGaps_Fitter_Tracker_Model(self.scl_long_tuneup_controller)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		self.cavs_rescale_table.getModel().fireTableDataChanged()
