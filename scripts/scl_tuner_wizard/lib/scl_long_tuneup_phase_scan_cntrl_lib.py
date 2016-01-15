# The SCL Longitudinal Tune-Up Cavities' Phase Scan controller
# It will scan all SCL cavities

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

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Auxiliary SCAN classes and functions
#------------------------------------------------------------------------	
class ScanStateController:
	def __init__(self):
		self.isRunning = false
		self.shouldStop = false
		
	def getIsRunning(self):
		return self.isRunning
		
	def getShouldStop(self):
		return self.shouldStop

	def setIsRunning(self,val):
		self.isRunning = val
		
	def setShouldStop(self,val):
		self.shouldStop = val
			
class PhaseScan_Runner(Runnable):
	def __init__(self,scl_long_tuneup_controller, run_to_end = true):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.run_to_end = run_to_end
		self.harmonicsAnalyzer = HarmonicsAnalyzer(2)
		#--- fake scan boolean variable
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		set_phase_shift_panel = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel
		self.fake_scan = set_phase_shift_panel.scanSim_RadioButton.isSelected()
		self.useTrigger = set_phase_shift_panel.beamTrigger_RadioButton.isSelected()
		self.keepCavPhases = set_phase_shift_panel.keepLiveCavPhases_RadioButton.isSelected()
		self.wrapPhases = set_phase_shift_panel.wrapPhases_RadioButton.isSelected()
	
	def run(self):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		if(messageTextField != null):
			messageTextField.setText("")	
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		scan_state_controller = scl_long_tuneup_phase_scan_controller.scan_state_controller
		cavs_table = scl_long_tuneup_phase_scan_controller.cavs_table
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		n_cavs = len(cav_wrappers)
		phase_step = scl_long_tuneup_phase_scan_controller.start_stop_scan_panel.phase_step_text.getValue()
		time_wait = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.time_wait_text.getValue()
		scan_status_text = scl_long_tuneup_phase_scan_controller.start_stop_scan_panel.scan_status_text
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		if(not scl_long_tuneup_init_controller.allPairsSet()):
			if(messageTextField != null):
				messageTextField.setText("You should Initialize the scan first! Go to the Init tab!")		
			return
		scl_long_tuneup_init_controller.connectAllBPMs()
		beamTrigger = self.scl_long_tuneup_controller.beamTrigger
		bpmBatchReader = self.scl_long_tuneup_controller.bpmBatchReader
		bpm_amp_min_limit = scl_long_tuneup_phase_scan_controller.post_scan_panel.amp_limit_text.getValue()
		#------------------------------- CA START
		# ?????? For live CA
		if(beamTrigger == null):
			if(messageTextField != null):
				messageTextField.setText("You should Initialize the scan first! Go to the Init tab!")		
			return
		else:
			beamTrigger.setFakeScan(self.fake_scan)
			beamTrigger.setUseTrigger(self.useTrigger)
		beamTrigger.setSleepTime(time_wait)
		beamTrigger.scan_state_controller = scan_state_controller
		bpmBatchReader.setBeamTrigger(beamTrigger)
		#------------------------------- CA END
		#---------start deal with selected cavities
		cav_selected_inds = cavs_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0): 
			if(messageTextField != null):
				messageTextField.setText("Select one or more cavities to start Phase Scan!")	
				beamTrigger.setFakeScan(false)
			return
		# these are the table model indexes
		cav_wrapper = null
		start_ind = cav_selected_inds[0]
		last_ind = cav_selected_inds[len(cav_selected_inds)-1]
		if(self.run_to_end): last_ind = n_cavs
		#----- blank and clean scan data for all downstream cavities
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		if(start_ind != 0):
			cav_wrapper = cav_wrappers[start_ind-1]
		cav_wrapper.clean()
		for cav_table_ind in range(start_ind+1,n_cavs+1):
			cav_wrapper = cav_wrappers[cav_table_ind-1]
			if(cav_wrapper.isGood):
				# ?????? should be tested with CA
				if(not self.fake_scan): cav_wrapper.setBlankBeam(true)
				cav_wrapper.clean()
		cavs_table.getModel().fireTableDataChanged()
		#-------start loop over cavities in the table
		result = true
		time_start = time.time()
		n_total = last_ind - start_ind + 1
		n_count = 0
		for cav_table_ind in range(start_ind,last_ind+1):
			cavs_table.setRowSelectionInterval(cav_table_ind,cav_table_ind)
			cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
			if(cav_table_ind != 0):
				cav_wrapper = cav_wrappers[cav_table_ind-1]
			if(not cav_wrapper.isGood): continue
			if(cav_table_ind != 0):
				# ?????? should be tested with CA
				if(not self.fake_scan): cav_wrapper.setBlankBeam(false)
			txt = "Scan is running! Cavity="+cav_wrapper.alias
			if(start_ind != last_ind): txt = txt + " to Cavity="+cav_wrappers[last_ind-1].alias
			if(n_count > 1):
				run_time = time.time() - time_start
				run_time_sec = int(run_time % 60)
				run_time_min = int(run_time/60.)
				eat_time = ((run_time/n_count)*(n_total - n_count))
				eat_time_sec = int(eat_time % 60)
				eat_time_min = int(eat_time/60.)
				txt = txt + "  ETA= %3d min  %2d sec "%(eat_time_min,eat_time_sec)
				txt = txt + "  Elapse= %3d min  %2d sec "%(run_time_min,run_time_sec)
			scan_status_text.setText(txt)
			if(scan_state_controller.getShouldStop()): break
			cav_phase = -180.
			while(cav_phase <= 180.):
				#debug
				#print "debug cav=",cav_wrapper.alias," phase=",cav_phase
				#--------set cavity phase
				if(cav_table_ind != 0):
					# ?????? should be tested with CA
					if(not self.fake_scan): cav_wrapper.cav.setCavPhase(cav_phase)
				if(cav_wrapper == self.scl_long_tuneup_controller.cav0_wrapper):
					scan_status_text.setText("Scan running  cavity= "+cav_wrapper.alias+" phase="+str(cav_phase))
				if(scan_state_controller.getShouldStop()): break
				time.sleep(time_wait)
				if(scan_state_controller.getShouldStop()): break
				# ?????? For live CA
				result = false
				if(not self.fake_scan):
					result = bpmBatchReader.makePhaseScanStep(cav_phase,cav_wrapper,bpm_amp_min_limit)
				else:
					result = self.fakeMakePhaseScanStep(cav_phase,cav_wrapper)
				if(scan_state_controller.getShouldStop()): break
				if(result == false):
					scan_status_text.setText("Scan stopped. Could not get BPM data. Please check!")
					if(messageTextField != null):
						messageTextField.setText("")
						beamTrigger.setFakeScan(false)
					return 
				if(cav_table_ind != 0):
					cav_wrapper.addScanPointToPhaseDiffData()			
				cav_phase += phase_step
			#----end of phase loop
			if(scan_state_controller.getShouldStop()): break
			#set up the Cavity phase according to scan
			if(cav_table_ind != 0):
				res_phase_set = self.setUpCavityPhase(cav_wrapper)
				time.sleep(time_wait)
				if(not res_phase_set):
					scan_status_text.setText("Scan stopped! Bad scan! Cannot set up phase for cavity="+cav_wrapper.alias)
					beamTrigger.setFakeScan(false)
					return
			cav_wrapper.isMeasured = true
			cavs_table.getModel().fireTableDataChanged()
			n_count += 1
		#------end of cavities loop
		txt = ""
		if(cav_wrapper != null):
			txt = " The last cavity was "+cav_wrapper.alias+" ."
		run_time = time.time() - time_start
		run_time_sec = int(run_time % 60)
		run_time_min = int(run_time/60.)
		txt = txt + "  Total time= %3d min  %2d sec "%(run_time_min,run_time_sec)
		if(scan_state_controller.getShouldStop()):
			scan_status_text.setText("Scan was interrupted!"+txt)
		else:
			scan_status_text.setText("Scan finished!"+txt)
		#restore the beam trigger fake scan state
		beamTrigger.setFakeScan(false)
	
	def setUpCavityPhase(self,cav_wrapper):
		#--- wrap all BPMs phase scans at once
		if(self.wrapPhases): 
			res_phase_wrap = self.allBPMs_PhaseWrapper(cav_wrapper)	
			if(not res_phase_wrap): return false
		max_bad_points_count = 2
		gd = cav_wrapper.phaseDiffPlot
		gdTh = cav_wrapper.phaseDiffPlotTh
		gdTh.removeAllPoints()
		if(gd.getNumbOfPoints() < 8): return false
		err = self.harmonicsAnalyzer.analyzeData(gd)	
		harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		#-----remove bad points
		bad_points_count = 0
		bad_index = 1
		while(bad_index >= 0):
			bad_index = -1
			for i in range(gd.getNumbOfPoints()):
				phase = gd.getX(i)
				y_appr = harm_function.getValue(phase)
				y = gd.getY(i)
				if(math.fabs(y-y_appr) > 3.0*err):
					bad_index = i
					bad_points_count += 1
					break
			if(bad_index >= 0):
				gd.removePoint(bad_index)
				bpm_wrappers = cav_wrapper.bpm_wrappers
				for bpm_wrapper in bpm_wrappers:				
					if(cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper)):
						(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
						graphDataAmp.removePoint(bad_index)
						graphDataPhase.removePoint(bad_index)
			# we should stop if we have too many bad points
			if(bad_points_count > max_bad_points_count):
				return false
		if(bad_points_count > 0):
			err = self.harmonicsAnalyzer.analyzeData(gd)
			harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		#----find a new cavity phase
		min_phase = self.harmonicsAnalyzer.getPositionOfMin()
		new_phase = cav_wrapper.initLivePhase
		if(self.keepCavPhases):
			cav_wrapper.scanPhaseShift = makePhaseNear(new_phase - min_phase,0.)
		else:
			new_phase = makePhaseNear(min_phase + cav_wrapper.scanPhaseShift,0.)
		# ?????? should be tested with CA
		cav_wrapper.livePhase = new_phase
		if(not self.fake_scan):
			cav_wrapper.cav.setCavPhase(cav_wrapper.livePhase)
		cav_wrapper.livePhase = new_phase
		cav_wrapper.phase_scan_harm_amp = harm_function.getParamArr()[1]
		cav_wrapper.phase_scan_harm_err = err
		cav_wrapper.phase_scan_harm_funcion.setParamArr(self.harmonicsAnalyzer.getHrmonicsFunction().getParamArr())
		#----make theory graph plot
		x_arr = []
		y_arr = []
		for i in range(73):
		 phase = -180.0 + 5.0*i
		 y = harm_function.getValue(phase)
		 x_arr.append(phase)
		 y_arr.append(y)
		gdTh.addPoint(x_arr,y_arr)
		return true
	
	def fakeMakePhaseScanStep(self,cav_phase,cav_wrapper):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		mass = self.scl_long_tuneup_controller.mass/1.0e+6
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq
		c = self.scl_long_tuneup_controller.c_light	
		cav_pos0 = cav_wrappers[0].pos
		max_pos = cav_wrappers[len(cav_wrappers)-1].pos - cav_pos0
		cav_pos = cav_wrapper.pos
		eKin_in = 185.6+650.0*(cav_pos-cav_pos0)/max_pos
		cav_phase_offset = 30.
		phase = makePhaseNear(cav_phase + cav_phase_offset,0.)
		eKin = eKin_in
		if(self.scl_long_tuneup_controller.cav0_wrapper != cav_wrapper):
			eKin +=  8.0*math.cos(2.0*math.pi*phase/360.)
		else:
			eKin = 185.6
		gamma = (eKin+mass)/mass
		beta = math.sqrt(1.0 - 1.0/gamma**2)
		c = 2.99792458e+8
		freq = 402.5e+6
		bpm_phase_coef = 360.0*freq/(c*beta)
		#print "debug cav_phase=",cav_phase," beta=",beta," eKin=",eKin," cav_pos=",cav_pos," coeff=",bpm_phase_coef
		bpm_wrappers = cav_wrapper.bpm_wrappers
		for bpm_wrapper in bpm_wrappers:				
			if(cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper)):
				(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
				if(bpm_wrapper.pos < cav_pos):
					graphDataAmp.addPoint(cav_phase,10.0)
					graphDataPhase.addPoint(cav_phase,20.0)
					continue
				bpm_phase = makePhaseNear(bpm_phase_coef*(bpm_wrapper.pos - cav_pos),0.)
				bpm_phase += 1.0*(random.random()-0.5)
				bpm_amp = 25.0*1./(1.+(bpm_wrapper.pos-cav_pos)/20.)
				graphDataAmp.addPoint(cav_phase,bpm_amp)
				old_bpm_phase = 0.
				if(graphDataPhase.getNumbOfPoints() > 0):
					old_bpm_phase = graphDataPhase.getY(graphDataPhase.getNumbOfPoints() - 1)
				graphDataPhase.addPoint(cav_phase,makePhaseNear(bpm_phase,old_bpm_phase))
		return true
		
	def allBPMs_PhaseWrapper(self,cav_wrapper):
		# it will wrap all BPM phases for the cvity by iteration from the BPM closest to cavity
		if(not cav_wrapper.isGood): return
		cav_pos = cav_wrapper.pos
		bpm_wrappers = []
		for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
			bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
			res_bool = cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper)
			res_bool = res_bool and bpm_wrapper.isGood
			res_bool = res_bool and (bpm_wrapper.pos > cav_pos)
			if(res_bool):
				bpm_wrappers.append(bpm_wrapper)
		for bpm_ind in range(len(bpm_wrappers)-1):
			(graphDataAmp0,graphDataPhase0) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers[bpm_ind]]
			(graphDataAmp1,graphDataPhase1) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers[bpm_ind+1]]
			if(graphDataPhase0.getNumbOfPoints() < 1): break
			if(graphDataPhase0.getNumbOfPoints() != graphDataPhase1.getNumbOfPoints()):
				txt = "Phase Wrapper BPM=",bpm_wrappers[bpm_ind].alias
				txt += " and BPM=",bpm_wrappers[bpm_ind+1].alias
				txt += " have different number of RF phase points n1=",graphDataPhase0.getNumbOfPoints()
				txt += " n2=",graphDataPhase1.getNumbOfPoints()
				self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
				print "debug ==================="
				print txt
				time.sleep(10.)
				return false
			y_arr = []
			for ip in range(graphDataPhase1.getNumbOfPoints()):
				y_arr.append(graphDataPhase1.getY(ip))
			base_phase_diff = y_arr[0] - graphDataPhase0.getY(0)
			for ip in range(1,graphDataPhase0.getNumbOfPoints()):
				y0 = graphDataPhase0.getY(ip)
				y_arr[ip] = makePhaseNear(y_arr[ip],y0+base_phase_diff)
				base_phase_diff = y_arr[ip] - y0
			#move all data by 360. to make the avg close to 0.
			y_avg = 0.
			for y in y_arr:
				y_avg += y
			if(len(y_arr) > 1): y_avg /= len(y_arr)
			y_shift = int(y_avg/360.)*360.
			for ip in range(len(y_arr)):
				y_arr[ip] -= y_shift
			#--- update all bpm phases
			graphDataPhase1.updateValuesY(y_arr)	
		# recreate phase difference
		cav_wrapper.recalculatePhaseDiffData()
		return true

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class BPMs_Amp_Phase_Graphs_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(GridLayout(2,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.gp_bpm_phases = FunctionGraphsJPanel()
		self.gp_bpm_amps = FunctionGraphsJPanel()
		self.gp_bpm_phases.setLegendButtonVisible(true)
		self.gp_bpm_phases.setChooseModeButtonVisible(true)	
		self.gp_bpm_amps.setLegendButtonVisible(true)
		self.gp_bpm_amps.setChooseModeButtonVisible(true)
		self.gp_bpm_phases.setName("BPM Phases")
		self.gp_bpm_phases.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")	
		self.gp_bpm_amps.setName("BPM Amplitude")
		self.gp_bpm_amps.setAxisNames("Cav Phase, [deg]","Amplitude, a.u.")	
		self.gp_bpm_phases.setBorder(etched_border)
		self.gp_bpm_amps.setBorder(etched_border)
		self.add(self.gp_bpm_phases)
		self.add(self.gp_bpm_amps)	
		
	def removeAllGraphData(self):
		self.gp_bpm_phases.removeAllGraphData()
		self.gp_bpm_amps.removeAllGraphData()		
		
	def updateGraphData(self):
		self.gp_bpm_phases.removeAllGraphData()
		self.gp_bpm_amps.removeAllGraphData()
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		bpms_table_model = scl_long_tuneup_phase_scan_controller.bpms_table_model
		bpm_wrappers = bpms_table_model.bpm_wrappers
		bpm_selected_inds = bpms_table.getSelectedRows()
		if(len(bpm_selected_inds) == 0 or bpm_selected_inds[0] < 0):
			return
		cav_wrapper = bpms_table_model.cav_wrapper
		if(cav_wrapper == null or not cav_wrapper.isGood): return
		for ind in bpm_selected_inds:
			bpm_wrapper = bpm_wrappers[ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.getAmpPhaseGraphs(bpm_wrapper)
			if(graphDataAmp != null):
				self.gp_bpm_amps.addGraphData(graphDataAmp)
			if(graphDataPhase != null):
				self.gp_bpm_phases.addGraphData(graphDataPhase)

class BPM_PhaseDiff_Graph_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(GridLayout(1,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.gp_bpm_phaseDiff = FunctionGraphsJPanel()
		self.gp_bpm_phaseDiff.setLegendButtonVisible(true)
		self.gp_bpm_phaseDiff.setChooseModeButtonVisible(true)
		self.gp_bpm_phaseDiff.setName("BPMs Phase Difference: ")
		self.gp_bpm_phaseDiff.setAxisNames("Cav Phase, [deg]","BPMs Phase Diff., [deg]")	
		self.gp_bpm_phaseDiff.setBorder(etched_border)
		self.add(self.gp_bpm_phaseDiff)
		
	def removeAllGraphData(self):
		self.gp_bpm_phaseDiff.removeAllGraphData()
			
	def updateGraphData(self):
		self.gp_bpm_phaseDiff.removeAllGraphData()
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		cavs_table = scl_long_tuneup_phase_scan_controller.cavs_table
		cav_selected_inds = cavs_table.getSelectedRows()
		if(len(cav_selected_inds) == 0 or cav_selected_inds[0] < 0):
			return
		for ind in cav_selected_inds:
			cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
			if(ind != 0):
				cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[ind-1]
			if(cav_wrapper == null or not cav_wrapper.isGood): 
				continue
			self.gp_bpm_phaseDiff.addGraphData(cav_wrapper.phaseDiffPlot)
			self.gp_bpm_phaseDiff.addGraphData(cav_wrapper.phaseDiffPlotTh)
			
class SetPhaseShiftAndTimeStep_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,1,1))
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		sub1_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		sub2_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		self.add(sub1_panel,BorderLayout.NORTH)
		self.add(sub2_panel,BorderLayout.SOUTH)
		set_phase_shift_button = JButton(" Set Phase Shift to Selected Cavs ")
		set_phase_shift_button.addActionListener(Set_Phase_Shift_Button_Listener(self.scl_long_tuneup_controller))
		sub1_panel.add(set_phase_shift_button)
		self.phase_shift_text = DoubleInputTextField(-18.0,ScientificNumberFormat(4),6)
		phase_shift_lbl = JLabel("Phase Shift[deg]=",JLabel.RIGHT)
		sub1_panel.add(phase_shift_lbl)
		sub1_panel.add(self.phase_shift_text)
		time_wait_lbl = JLabel("   Scan Wait Time[sec]=",JLabel.LEFT)
		self.time_wait_text = DoubleInputTextField(0.5,ScientificNumberFormat(4),6)
		sub1_panel.add(time_wait_lbl)
		sub1_panel.add(self.time_wait_text)
		self.keepLiveCavPhases_RadioButton = JRadioButton("Keep Cav. Phases")
		self.wrapPhases_RadioButton = JRadioButton("Wrap Phases")
		self.beamTrigger_RadioButton = JRadioButton("Use Beam Trigger")
		self.scanSim_RadioButton = JRadioButton("Simulation")
		self.keepLiveCavPhases_RadioButton.setSelected(false)
		self.wrapPhases_RadioButton.setSelected(true)		
		self.beamTrigger_RadioButton.setSelected(false)
		self.scanSim_RadioButton.setSelected(false)
		sub2_panel.add(self.wrapPhases_RadioButton)
		sub2_panel.add(self.keepLiveCavPhases_RadioButton)
		sub2_panel.add(self.beamTrigger_RadioButton)
		sub2_panel.add(self.scanSim_RadioButton)
		
class StartStopPhaseScan_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		self.phase_step_text = DoubleInputTextField(20.0,ScientificNumberFormat(4),8)
		phase_step_lbl = JLabel("Phase Step[deg]=",JLabel.RIGHT)
		start_scan_button = JButton("Start Scan")
		start_scan_button.addActionListener(Start_Phase_Scan_Button_Listener(self.scl_long_tuneup_controller))
		start_scan_for_selection_button = JButton("Start for Selected Cavs.")
		start_scan_for_selection_button.addActionListener(Start_Phase_Scan_Select_Cavs_Button_Listener(self.scl_long_tuneup_controller))
		stop_scan_button = JButton("Stop Scan")
		stop_scan_button.addActionListener(Stop_Phase_Scan_Button_Listener(self.scl_long_tuneup_controller))
		buttons_panel.add(phase_step_lbl)
		buttons_panel.add(self.phase_step_text)
		buttons_panel.add(start_scan_button)
		buttons_panel.add(start_scan_for_selection_button)
		buttons_panel.add(stop_scan_button)
		self.scan_status_text = JTextField()
		self.scan_status_text.setText("Scan status")
		self.scan_status_text.setHorizontalAlignment(JTextField.LEFT)
		self.scan_status_text.setForeground(Color.red)
		self.add(buttons_panel,BorderLayout.WEST)
		self.add(self.scan_status_text,BorderLayout.CENTER)
		
class PostPhaseScanActions_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		remove_bpm_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		titled_border = BorderFactory.createTitledBorder(etched_border,"Post Scan: Remove Selected BPMs from Analysis")
		remove_bpm_panel.setBorder(titled_border)
		remove_bpm_button1 = JButton(" X for Selected Cavs. ")
		remove_bpm_button1.addActionListener(Remove_BPM_1_Button_Listener(self.scl_long_tuneup_controller))			
		remove_bpm_button2 = JButton(" X for All Cavs. ")
		remove_bpm_button2.addActionListener(Remove_BPM_2_Button_Listener(self.scl_long_tuneup_controller))
		remove_bpm_button3 = JButton(" X as Bad BPMs ")
		remove_bpm_button3.addActionListener(Remove_BPM_3_Button_Listener(self.scl_long_tuneup_controller))
		remove_bpm_button4 = JButton(" Set All BPMs as YES! ")
		remove_bpm_button4.addActionListener(Remove_BPM_4_Button_Listener(self.scl_long_tuneup_controller))		
		remove_bpm_panel.add(remove_bpm_button1)
		remove_bpm_panel.add(remove_bpm_button2)
		remove_bpm_panel.add(remove_bpm_button3)
		remove_bpm_panel.add(remove_bpm_button4)
		amp_limit_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		titled_border = BorderFactory.createTitledBorder(etched_border,"Post Scan: Apply BPM Amp. Limit to all Cavs and BPMs")
		amp_limit_panel.setBorder(titled_border)
		self.amp_limit_text = DoubleInputTextField(1.0,ScientificNumberFormat(4),8)
		amp_limit_lbl = JLabel("BPM Amp Limit=",JLabel.RIGHT)
		amp_limit_bpm_button = JButton(" Apply BPM Amp. Limit ")
		amp_limit_bpm_button.addActionListener(Apply_BPM_Amp_Limit_Button_Listener(self.scl_long_tuneup_controller))
		amp_limit_panel.add(amp_limit_lbl)
		amp_limit_panel.add(self.amp_limit_text)
		amp_limit_panel.add(amp_limit_bpm_button)
		#--------------------------
		self.add(remove_bpm_panel)
		self.add(amp_limit_panel)

#------------------------------------------------
#  JTable models
#------------------------------------------------

class Cavs_PhaseScan_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cavity","Use","Done"]
		self.columnNames += ["<html>BPM<SUB>1</SUB><html>","<html>BPM<SUB>2</SUB><html>"]
		self.columnNames += ["<html>RF &phi;<SUB>old</SUB>(deg)<html>",]
		self.columnNames += ["<html>RF &phi;<SUB>new</SUB>(deg)<html>",]
		self.columnNames += ["<html>Fit A<SUB>&phi;</SUB>(deg)<html>",]
		self.columnNames += ["<html>Fit &delta;A<SUB>&phi;</SUB>(deg)<html>",]
		self.columnNames += ["<html>&Delta;&phi;<SUB>RF</SUB>(deg)<html>",]
		self.nf3 = NumberFormat.getInstance()
		self.nf3.setMaximumFractionDigits(3)		
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return (len(self.scl_long_tuneup_controller.cav_wrappers)+1)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		if(row != 0):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row-1]
		if(col == 0): 
			if(row != 0):
				return cav_wrapper.alias
			else:
				return "All Off"
		if(col == 1): return cav_wrapper.isGood	
		if(col == 2): return cav_wrapper.isMeasured
		if(col == 3):
			bpm_wrapper0 = cav_wrapper.bpm_wrapper0
			if(bpm_wrapper0 != null):
				return bpm_wrapper0.alias
		if(col == 4): 
			bpm_wrapper1 = cav_wrapper.bpm_wrapper1
			if(bpm_wrapper1 != null):
				return bpm_wrapper1.alias	
		if(col == 5): return self.nf3.format(cav_wrapper.initLivePhase)
		if(col == 6): return self.nf3.format(cav_wrapper.livePhase)
		if(col == 7): return self.nf3.format(cav_wrapper.phase_scan_harm_amp)		
		if(col == 8): return self.nf3.format(cav_wrapper.phase_scan_harm_err)				
		if(col == 9): return self.nf3.format(cav_wrapper.scanPhaseShift)
		return ""
				
	def getColumnClass(self,col):
		if(col == 1 or col == 2):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(row == 0): return false
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row-1]		
		if(col == 9 and (not cav_wrapper.isMeasured)):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(row == 0): return
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row-1]
		if(col == 9 and (not cav_wrapper.isMeasured)):
			 cav_wrapper.scanPhaseShift = Double.parseDouble(value)

class PhaseScan_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.cav_wrapper = null
		self.bpm_wrappers = []
		self.bpm_wrappers_useInPhaseAnalysis = []
		self.columnNames = ["BPM"," UseInAnalysis"]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.bpm_wrappers[row]
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return self.bpm_wrappers_useInPhaseAnalysis[row]
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class		
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 1 and row != 0):
			return true		
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 1 and row != 0):
			 self.bpm_wrappers_useInPhaseAnalysis[row] = value			

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		bpms_table_model = scl_long_tuneup_phase_scan_controller.bpms_table_model		
		if(index < 0):
			bpms_table_model.cav_wrapper = null
			return 
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		if(index != 0):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[index-1]
		if(cav_wrapper.isGood):
			bpms_table_model.cav_wrapper = cav_wrapper		
			bpms_table_model.bpm_wrappers = cav_wrapper.bpm_wrappers
			bpms_table_model.bpm_wrappers_useInPhaseAnalysis = cav_wrapper.bpm_wrappers_useInPhaseAnalysis
		else:
			bpms_table_model.cav_wrapper = null		
			bpms_table_model.bpm_wrappers = []
			bpms_table_model.bpm_wrappers_useInPhaseAnalysis = []
		txt = cav_wrapper.alias
		if(index == 0): txt = "All Off"
		scl_long_tuneup_phase_scan_controller.bpm_table_border.setTitle("Cavity: "+txt)
		bpms_table_model.fireTableDataChanged()
		scl_long_tuneup_phase_scan_controller.bpm_table_panel.repaint()
		scl_long_tuneup_phase_scan_controller.bpm_phaseDiff_graph_panel.updateGraphData()
		
class BPMs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		scl_long_tuneup_phase_scan_controller.bpm_phase_and_amp_graph_panel.updateGraphData()	

class Set_Phase_Shift_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		cavs_table = scl_long_tuneup_phase_scan_controller.cavs_table
		phase_shift_text = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.phase_shift_text
		cav_selected_inds = cavs_table.getSelectedRows()
		if(len(cav_selected_inds) == 0 or cav_selected_inds[0] < 0):
			return
		for ind in cav_selected_inds:
			if(ind == 0): continue
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[ind-1]
			if(cav_wrapper == null or not cav_wrapper.isGood): 
				continue
			cav_wrapper.scanPhaseShift = phase_shift_text.getValue()
		cavs_table.getModel().fireTableDataChanged()

class Remove_BPM_1_Button_Listener(ActionListener):
	#remove the selected BPMs from the phase analysis for selected cavities
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		cavs_table = scl_long_tuneup_phase_scan_controller.cavs_table
		cav_selected_inds = cavs_table.getSelectedRows()
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		bpm_selected_inds = bpms_table.getSelectedRows()	
		for cav_ind in cav_selected_inds:
			if(cav_ind >= 0): 
				cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
				if(cav_ind != 0): cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind-1]			
				for bpm_ind in bpm_selected_inds:
					if(bpm_ind >= 0):
						bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
						cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
		bpms_table.getModel().fireTableDataChanged()
		if(len(bpm_selected_inds) > 0):
			bpms_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[len(bpm_selected_inds)-1])
						
class Remove_BPM_2_Button_Listener(ActionListener):
	#remove the selected BPMs from the phase analysis for all cavities
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		bpm_selected_inds = bpms_table.getSelectedRows()	
		for cav_ind in range(len(self.scl_long_tuneup_controller.cav_wrappers)+1):
			cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
			if(cav_ind != 0): cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind-1]			
			for bpm_ind in bpm_selected_inds:
				if(bpm_ind >= 0):
					bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
					cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
		bpms_table.getModel().fireTableDataChanged()
		if(len(bpm_selected_inds) > 0):
			bpms_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[len(bpm_selected_inds)-1])
			
class Remove_BPM_3_Button_Listener(ActionListener):
	#remove the selected BPMs from the phase and amplitude analyses for all cavities
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		bpm_selected_inds = bpms_table.getSelectedRows()	
		for cav_ind in range(len(self.scl_long_tuneup_controller.cav_wrappers)+1):
			cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
			if(cav_ind != 0): cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind-1]	
			for bpm_ind in bpm_selected_inds:
				if(bpm_ind >= 0):
					bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
					cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
					cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = false
		bpms_table.getModel().fireTableDataChanged()
		if(len(bpm_selected_inds) > 0):
			bpms_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[len(bpm_selected_inds)-1])		
		
class Remove_BPM_4_Button_Listener(ActionListener):
	#mark all BPMs as UseInAnalysis = yes and useInAmpAnalysis=yes for all cavities
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		for cav_ind in range(len(self.scl_long_tuneup_controller.cav_wrappers)+1):
			cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
			if(cav_ind != 0): cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind-1]	
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = true
				cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
				# HEBT2 BPMs should not be used in the phase analysis (pending the XAL Online Model fix)
				if(bpm_wrapper.pos > 280.):
					cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
					cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
		bpms_table.getModel().fireTableDataChanged()
		
class Start_Phase_Scan_Button_Listener(ActionListener):
	#This button will start scan to the end
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		scan_state_controller = scl_long_tuneup_phase_scan_controller.scan_state_controller
		scan_state_controller.setShouldStop(false)			
		runner = PhaseScan_Runner(self.scl_long_tuneup_controller,true)
		thr = Thread(runner)
		thr.start()			
		
class Start_Phase_Scan_Select_Cavs_Button_Listener(ActionListener):
	#This button will start scan for the selected cavities only
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		scan_state_controller = scl_long_tuneup_phase_scan_controller.scan_state_controller
		scan_state_controller.setShouldStop(false)		
		runner = PhaseScan_Runner(self.scl_long_tuneup_controller,false)
		thr = Thread(runner)
		thr.start()			
		
class Stop_Phase_Scan_Button_Listener(ActionListener):						
	#This button action will stop the scan
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		scan_state_controller = scl_long_tuneup_phase_scan_controller.scan_state_controller
		scan_state_controller.setShouldStop(true)
		
class Apply_BPM_Amp_Limit_Button_Listener(ActionListener):	
	#This button action will mark all bpms with the low amplitudes as "Do not use in a Phase Analysis"
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		amp_limit = scl_long_tuneup_phase_scan_controller.post_scan_panel.amp_limit_text.getValue()
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		bpms_table = scl_long_tuneup_phase_scan_controller.bpms_table
		for cav_wrapper in cav_wrappers:
			if((not cav_wrapper.isGood) or (not cav_wrapper.isMeasured)): continue
			self.applyBPM_AmpLimit(amp_limit,cav_wrapper)
		if(self.scl_long_tuneup_controller.cav0_wrapper.isMeasured):
			self.applyBPM_AmpLimit(amp_limit,self.scl_long_tuneup_controller.cav0_wrapper)
		bpms_table.getModel().fireTableDataChanged()	
		#wrap all BPM phase scan results in the phase analysis
		for cav_wrapper in cav_wrappers:
			if((not cav_wrapper.isGood) or (not cav_wrapper.isMeasured)): continue
			cav_pos = cav_wrapper.pos
			bpm_wrappers = []
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				res_bool = cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper)
				res_bool = res_bool and cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]
				res_bool = res_bool and (bpm_wrapper.pos > cav_pos)
				if(res_bool):
					bpm_wrappers.append(bpm_wrapper)
			for bpm_ind in range(len(bpm_wrappers)-1):
				(graphDataAmp0,graphDataPhase0) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers[bpm_ind]]
				(graphDataAmp1,graphDataPhase1) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers[bpm_ind+1]]
				if(graphDataPhase0.getNumbOfPoints() < 1): break
				y_arr = []
				for ip in range(graphDataPhase1.getNumbOfPoints()):
					y_arr.append(graphDataPhase1.getY(ip))
					base_phase_diff = y_arr[0] - graphDataPhase0.getY(0)
				for ip in range(1,graphDataPhase0.getNumbOfPoints()):
					y0 = graphDataPhase0.getY(ip)
					y_arr[ip] = makePhaseNear(y_arr[ip],y0+base_phase_diff)
					base_phase_diff = y_arr[ip] - y0
				#move all data by 360. to make the avg close to 0.
				y_avg = 0.
				for y in y_arr:
					y_avg += y
				if(len(y_arr) > 1): y_avg /= len(y_arr)
				y_shift = int(y_avg/360.)*360.
				for ip in range(len(y_arr)):
					y_arr[ip] -= y_shift
				#--- update all bpm phases
				graphDataPhase1.updateValuesY(y_arr)
					
	def applyBPM_AmpLimit(self,amp_limit,cav_wrapper):
		for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
			bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
			#---- HEBT2 should be excluded - usually energy is wrong for beam transport to HEBT2
			if(bpm_wrapper.pos > 280.):
				cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = false
			if(cav_wrapper.bpm_amp_phase_dict.has_key(bpm_wrapper)):
				(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
				for ip in range(graphDataAmp.getNumbOfPoints()):
					if(graphDataAmp.getY(ip) < amp_limit):
						cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
						break		
	
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_PhaseScan_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_params_panel = JPanel(BorderLayout())
		self.set_phase_shift_panel = SetPhaseShiftAndTimeStep_Panel(self.scl_long_tuneup_controller)
		self.start_stop_scan_panel = StartStopPhaseScan_Panel(self.scl_long_tuneup_controller)
		top_params_panel.add(self.set_phase_shift_panel,BorderLayout.NORTH)
		top_params_panel.add(self.start_stop_scan_panel,BorderLayout.SOUTH)
		#------cavities scan table panel --------
		cavs_scan_panel = JPanel(BorderLayout())
		self.cavs_table = JTable(Cavs_PhaseScan_Table_Model(self.scl_long_tuneup_controller))
		self.cavs_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.cavs_table.setFillsViewportHeight(true)
		self.cavs_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self.scl_long_tuneup_controller))		
		self.bpms_table_model = PhaseScan_BPMs_Table_Model(self.scl_long_tuneup_controller)
		self.bpms_table = JTable(self.bpms_table_model)
		self.bpms_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.bpms_table.setFillsViewportHeight(true)	
		self.bpms_table.getSelectionModel().addListSelectionListener(BPMs_Table_Selection_Listener(self.scl_long_tuneup_controller))	
		self.bpms_table.setPreferredScrollableViewportSize(Dimension(200,300))
		scrl_panel0 = JScrollPane(self.cavs_table)
		scrl_panel0.setBorder(etched_border)
		scrl_panel1 = JScrollPane(self.bpms_table)
		self.bpm_table_border = BorderFactory.createTitledBorder(etched_border,"Cavity")
		scrl_panel1.setBorder(self.bpm_table_border)
		cavs_scan_panel.add(scrl_panel0,BorderLayout.CENTER)
		cavs_scan_panel.add(scrl_panel1,BorderLayout.EAST)
		self.bpm_table_panel = scrl_panel1
		#---------- graph panels --------------------------
		self.bpm_phaseDiff_graph_panel = BPM_PhaseDiff_Graph_Panel(self.scl_long_tuneup_controller)
		self.bpm_phase_and_amp_graph_panel = BPMs_Amp_Phase_Graphs_Panel(self.scl_long_tuneup_controller)
		grap_panel = JPanel(GridLayout(1,2))
		grap_panel.add(self.bpm_phaseDiff_graph_panel)		
		grap_panel.add(self.bpm_phase_and_amp_graph_panel)
		#--------center panel = graphs + tables-------------
		center_panel = JPanel(GridLayout(2,1))
		center_panel.add(cavs_scan_panel)
		center_panel.add(grap_panel)
		#-------- post-scan filtering panel
		bottom_panel = JPanel(BorderLayout())
		self.post_scan_panel = PostPhaseScanActions_Panel(self.scl_long_tuneup_controller)
		bottom_panel.add(self.post_scan_panel,BorderLayout.WEST)
		#--------------------------------------------------
		self.main_panel.add(top_params_panel,BorderLayout.NORTH)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		#------scan state
		self.scan_state_controller = ScanStateController()
		
	def getMainPanel(self):
		return self.main_panel

