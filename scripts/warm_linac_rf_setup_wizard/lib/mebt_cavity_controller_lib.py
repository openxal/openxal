# The MEBT cavity controller

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing import JTable
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from javax.swing.table import AbstractTableModel, TableModel


from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

from abstract_cavity_controller_lib import Abstract_Cavity_Controller
from constants_lib import GRAPH_LEGEND_KEY
from functions_and_classes_lib import calculateAvgErr, makePhaseNear, HarmonicsAnalyzer 
from functions_and_classes_lib import dumpGraphDataToDA, readGraphDataFromDA
from data_acquisition_classes_lib import BPM_Scan_Data

#------------------------------------------------------------------------
#           Auxiliary classes
#------------------------------------------------------------------------

class Cav_Phase_Iteration_Fitting_Runner(Runnable):
	# the thread runner for the cavity's phase iterative fitting
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
	
	def run(self):
		self.main_loop_controller.getMessageTextField().setText("")
		if(self.main_loop_controller.loop_run_state.isRunning): return
		self.main_loop_controller.loop_run_state.isRunning = true
		self.main_loop_controller.loop_run_state.shouldStop = false
		self.cav_controller.initProgressBar()
		(res,txt) = self.cav_controller.runIterativeCavPhase_Finder()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)
		self.main_loop_controller.loop_run_state.isRunning = false
		self.main_loop_controller.loop_run_state.shouldStop = false
		self.cav_controller.initProgressBar()

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class Scan_Main_Panel(JPanel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.setLayout(BorderLayout())
		self.setBorder(BorderFactory.createEtchedBorder())
		#---- 360 deg scan params.
		full_scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		full_scan_panel.setBorder(BorderFactory.createEtchedBorder())
		full_scan_label = JLabel("360 deg. scan params:  Phase step=",JLabel.RIGHT)
		self.full_scan_phase_step_text = DoubleInputTextField(20.0,FortranNumberFormat("G4.2"),6)
		time_step_label = JLabel(" sleep time[sec]=",JLabel.RIGHT)
		self.time_step_text = DoubleInputTextField(1.5,FortranNumberFormat("G4.2"),6)
		min_bpm_amp_label = JLabel("    Min BPM ampitude[mA]=",JLabel.RIGHT)
		self.min_bpm_amp_text = DoubleInputTextField(1.0,FortranNumberFormat("G4.2"),6)
		full_scan_panel.add(full_scan_label)
		full_scan_panel.add(self.full_scan_phase_step_text)
		full_scan_panel.add(time_step_label)
		full_scan_panel.add(self.time_step_text)
		full_scan_panel.add(min_bpm_amp_label)
		full_scan_panel.add(self.min_bpm_amp_text)
		#---- around -90 deg scan params.
		#-----------cav amp scan 
		amp_scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		amp_scan_panel.setBorder(BorderFactory.createEtchedBorder())
		min_cav_amp_label = JLabel("Min Cav. Amp=",JLabel.RIGHT)
		self.min_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G9.5"),8)
		max_cav_amp_label = JLabel(" Max=",JLabel.RIGHT)
		self.max_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G8.5"),8)
		step_cav_amp_label = JLabel("N amp. steps=",JLabel.RIGHT)
		self.nsteps_cav_amp_text =  DoubleInputTextField(2.0,FortranNumberFormat("G4.1"),5)
		live_cav_amp_label = JLabel(" Live Ampl.=",JLabel.RIGHT)
		self.live_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G8.5"),8)		
		amp_scan_panel.add(min_cav_amp_label)
		amp_scan_panel.add(self.min_cav_amp_text)
		amp_scan_panel.add(max_cav_amp_label)
		amp_scan_panel.add(self.max_cav_amp_text)
		amp_scan_panel.add(step_cav_amp_label)
		amp_scan_panel.add(self.nsteps_cav_amp_text)
		amp_scan_panel.add(live_cav_amp_label)
		amp_scan_panel.add(self.live_cav_amp_text)
		#-----------cav phase scan
		scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		scan_panel.setBorder(BorderFactory.createEtchedBorder())
		scan_label = JLabel("Around -90 deg scan params:  Phase Scan width[deg]=",JLabel.RIGHT)
		self.scan_phase_width_text = DoubleInputTextField(30.0,FortranNumberFormat("G4.2"),6)
		scan_phase_step_label = JLabel("  Phase step[deg]=",JLabel.RIGHT)
		self.scan_phase_step_text = DoubleInputTextField(2.0,FortranNumberFormat("G4.2"),6)
		scan_panel.add(scan_label)
		scan_panel.add(self.scan_phase_width_text)
		scan_panel.add(scan_phase_step_label)
		scan_panel.add(self.scan_phase_step_text)
		#-----------BPM phase based iterative cav. phase fitting
		bpm_iter_fitting_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		bpm_iter_fitting_panel.setBorder(BorderFactory.createEtchedBorder())
		bpm_iter_label = JLabel("Iterative Cavity Phase Fitting  N avg=",JLabel.RIGHT)
		self.bpm_iter_avg_text = DoubleInputTextField(5,FortranNumberFormat("G4.1"),4)	
		bpm_iter_fitting_steps_label = JLabel("   steps=",JLabel.RIGHT)
		self.bpm_iter_steps_text = DoubleInputTextField(2,FortranNumberFormat("G4.1"),4)
		self.use_bpm_iter_button = JRadioButton("Use")
		bpm_iter_fitting_panel.add(self.use_bpm_iter_button)
		bpm_iter_fitting_panel.add(bpm_iter_label)
		bpm_iter_fitting_panel.add(self.bpm_iter_avg_text)
		bpm_iter_fitting_panel.add(bpm_iter_fitting_steps_label)
		bpm_iter_fitting_panel.add(self.bpm_iter_steps_text)
		#---------------------------------------------------
		amp_phase_scan_tmp0_panel = JPanel(GridLayout(2,1,1,1))
		amp_phase_scan_tmp0_panel.add(amp_scan_panel)
		amp_phase_scan_tmp0_panel.add(scan_panel)
		amp_phase_scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		self.use_amp_phase_scan_button = JRadioButton("Use")
		amp_phase_scan_panel.add(self.use_amp_phase_scan_button)
		amp_phase_scan_panel.add(amp_phase_scan_tmp0_panel)
		#---------------------------------------------------
		# usage of the second step (scan or iter) of the phase finding process 
		self.use_bpm_iter_or_amp_phase_scan_button = JRadioButton("Use")
		self.use_bpm_iter_or_amp_phase_scan_button.setSelected(true)
		tmp_1 = JPanel(BorderLayout())
		tmp_1.add(self.use_bpm_iter_or_amp_phase_scan_button,BorderLayout.WEST)
		tmp_2 = JPanel(BorderLayout())
		tmp_2.add(amp_phase_scan_panel,BorderLayout.CENTER)
		tmp_2.add(bpm_iter_fitting_panel,BorderLayout.SOUTH)	
		tmp_1.add(tmp_2,BorderLayout.CENTER)
		tmp_1.setBorder(BorderFactory.createEtchedBorder())
		tmp_2.setBorder(BorderFactory.createEtchedBorder())		
		#---------------------------------------------------
		tmp_panel = JPanel(BorderLayout())
		tmp_panel.add(full_scan_panel,BorderLayout.NORTH)
		tmp_panel.add(tmp_1,BorderLayout.CENTER)
		#---------------------------------------------------
		button_goup = ButtonGroup()
		button_goup.add(self.use_amp_phase_scan_button)
		button_goup.add(self.use_bpm_iter_button)
		self.use_amp_phase_scan_button.setSelected(true)
		#---------------------------------------------------
		self.add(tmp_panel,BorderLayout.NORTH)

		
class Graphs_Panel(JTabbedPane):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		#----------------------------------------
		etched_border = BorderFactory.createEtchedBorder()
		self.gp_sine_phase_scan = FunctionGraphsJPanel()
		self.gp_sine_amp_scan = FunctionGraphsJPanel()		
		self.gp_amp_phase_scan_phase = FunctionGraphsJPanel()
		self.gp_amp_phase_scan_amp = FunctionGraphsJPanel()
		self.gp_cav_calibation = FunctionGraphsJPanel()
		#------------------------------------------
		self.gp_sine_phase_scan.setLegendButtonVisible(true)
		self.gp_sine_amp_scan.setLegendButtonVisible(true)
		self.gp_amp_phase_scan_phase.setLegendButtonVisible(true)
		self.gp_amp_phase_scan_amp.setLegendButtonVisible(true)
		self.gp_cav_calibation.setLegendButtonVisible(true)
		#------------------------------------------	
		self.gp_sine_phase_scan.setChooseModeButtonVisible(true)	
		self.gp_sine_amp_scan.setChooseModeButtonVisible(true)
		self.gp_amp_phase_scan_phase.setChooseModeButtonVisible(true)
		self.gp_amp_phase_scan_amp.setChooseModeButtonVisible(true)
		self.gp_cav_calibation.setChooseModeButtonVisible(true)
		self.gp_sine_phase_scan.setName("360 deg Caity Phase Scan: BPM Phase")
		self.gp_sine_amp_scan.setName("360 deg Caity Phase Scan: BPM Amplitude")
		self.gp_amp_phase_scan_phase.setName("Caity Amplitude & Phase Scans: BPM Phases")
		self.gp_amp_phase_scan_amp.setName("Caity Amplitude & Phase Scans: BPM Amp.")
		self.gp_cav_calibation.setName("Caity Max. Eenergy Gain vs. Amplitude")
		self.gp_sine_phase_scan.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")	
		self.gp_sine_amp_scan.setAxisNames("Cav Phase, [deg]","BPM Amplitude, a.u.")	
		self.gp_amp_phase_scan_phase.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")
		self.gp_amp_phase_scan_amp.setAxisNames("Cav Phase, [deg]","BPM Amp., a.u.")
		self.gp_cav_calibation.setAxisNames("Cav. Amplitude, [a.u.]","Max. Energy Gain, [keV]")		
		self.gp_sine_phase_scan.setBorder(etched_border)
		self.gp_sine_amp_scan.setBorder(etched_border)
		self.gp_amp_phase_scan_phase.setBorder(etched_border)	
		self.gp_amp_phase_scan_amp.setBorder(etched_border)	
		self.gp_cav_calibation.setBorder(etched_border)	
		#------------------------------------
		#---------------------------------------
		sine_like_graph_panel = JSplitPane(JSplitPane.VERTICAL_SPLIT,self.gp_sine_phase_scan,self.gp_sine_amp_scan)
		sine_like_graph_panel.setDividerLocation(0.5)
		sine_like_graph_panel.setResizeWeight(0.5)
		amp_phase_scan_panel = JSplitPane(JSplitPane.VERTICAL_SPLIT,self.gp_amp_phase_scan_phase,self.gp_amp_phase_scan_amp)
		amp_phase_scan_panel.setDividerLocation(0.5)
		amp_phase_scan_panel.setResizeWeight(0.5)
		energy_gain_graph_panel = JPanel(BorderLayout())
		self.add("Sine Like Scan",sine_like_graph_panel)
		self.add("Ampl. and Phase Scans",amp_phase_scan_panel)
		self.add("Cav. Amp. Calibration",energy_gain_graph_panel)		
		energy_gain_graph_panel.add(self.gp_cav_calibation,BorderLayout.CENTER)
		#-----------------------------------
		self.gp_sine_phase_scan.addVerticalLine(0.,Color.RED)
		self.gp_sine_amp_scan.addVerticalLine(0.,Color.RED)
		self.gp_amp_phase_scan_phase.addVerticalLine(0.,Color.RED)
		self.gp_amp_phase_scan_amp.addVerticalLine(0.,Color.RED)
		
	def setFoundCavityPhase(self,cav_phase):
		self.gp_sine_phase_scan.setVerticalLineValue(cav_phase,0)
		self.gp_sine_amp_scan.setVerticalLineValue(cav_phase,0)
		self.gp_amp_phase_scan_phase.setVerticalLineValue(cav_phase,0)
		self.gp_amp_phase_scan_amp.setVerticalLineValue(cav_phase,0)
		
	def refreshGraphJPanels(self):
		self.gp_sine_phase_scan.refreshGraphJPanel()
		self.gp_sine_amp_scan.refreshGraphJPanel()
		self.gp_amp_phase_scan_phase.refreshGraphJPanel()
		self.gp_amp_phase_scan_amp.refreshGraphJPanel()
		self.gp_cav_calibation.refreshGraphJPanel()
		
	def updateGraphs(self):
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		bpm_scan_data = cav_bpms_controller.getBPM_ScanData_for_SineWave()
		self.gp_sine_phase_scan.removeAllGraphData()
		self.gp_sine_amp_scan.removeAllGraphData()
		self.gp_sine_phase_scan.addGraphData(bpm_scan_data.phase_gd)
		self.gp_sine_phase_scan.addGraphData(bpm_scan_data.phase_fit_gd)
		self.gp_sine_amp_scan.addGraphData(bpm_scan_data.amp_gd)
		#---------------------------------------------
		self.gp_amp_phase_scan_phase.removeAllGraphData()
		self.gp_amp_phase_scan_amp.removeAllGraphData()
		for bpm_wrapper_ind in range(len(cav_bpms_controller.local_bpm_wrappers)):
			bpm_wrapper = cav_bpms_controller.local_bpm_wrappers[bpm_wrapper_ind]
			if(not cav_bpms_controller.local_bpm_isInUse[bpm_wrapper_ind]): continue
			for bpm_scan_data in cav_bpms_controller.scan_data_dict[bpm_wrapper]:
				self.gp_amp_phase_scan_phase.addGraphData(bpm_scan_data.phase_gd)
				self.gp_amp_phase_scan_phase.addGraphData(bpm_scan_data.phase_fit_gd)
				self.gp_amp_phase_scan_amp.addGraphData(bpm_scan_data.amp_gd)
		#---------------------------------------------
		self.gp_cav_calibation.removeAllGraphData()
		self.gp_cav_calibation.addGraphData(cav_bpms_controller.calibr_gd)
		self.gp_cav_calibation.addGraphData(cav_bpms_controller.calibr_fit_gd)
		
	def removeOnePoint(self):
		self.main_loop_controller.getMessageTextField().setText("")
		pane_ind = self.getSelectedIndex()
		gp_active = null
		gd_arr = []
		if(pane_ind == 0):
			gp_active = self.gp_sine_phase_scan
			for bpm_scan_data in self.cav_controller.cav_bpms_controller.sin_wave_scan_data_arr:
				gd_arr.append(bpm_scan_data.phase_gd)
				gd_arr.append(bpm_scan_data.amp_gd)
		if(pane_ind == 1):
			gp_active = self.gp_amp_phase_scan_phase
			for bpm_wrapper in self.cav_controller.cav_bpms_controller.local_bpm_wrappers:
				bpm_scan_data_arr = self.cav_controller.cav_bpms_controller.scan_data_dict[bpm_wrapper]
				for bpm_scan_data in bpm_scan_data_arr:
					gd_arr.append(bpm_scan_data.phase_gd)
					gd_arr.append(bpm_scan_data.amp_gd)					
		if(gp_active == null):
			self.main_loop_controller.getMessageTextField().setText("Cannot remove point from this graph!")
			return
		if(len(gd_arr) < 1): return
		#-------------------------------------------------------
		minX = gp_active.getCurrentMinX()
		maxX = gp_active.getCurrentMaxX()
		nPoints = 0
		point_ind = -1
		gd = gd_arr[0]
		for ind in range(gd.getNumbOfPoints()):
			x = gd.getX(ind)
			if( x > minX and x < maxX):
				nPoints += 1
				point_ind = ind
		if(nPoints != 1):
			self.main_loop_controller.getMessageTextField().setText("Use zoom to select one point on the upper graph!")
			return
		for gd in gd_arr:
			gd.removePoint(point_ind)
		gp_active.clearZoomStack()
		gp_active.refreshGraphJPanel()
	
class Local_BPMs_Table_Panel(JPanel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.setLayout(BorderLayout())
		self.setBorder(BorderFactory.createEtchedBorder())	
		#------------------------------
		self.local_bpms_table = JTable(Local_BPMs_Table_Model(main_loop_controller,cav_controller))
		self.local_bpms_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.local_bpms_table.setFillsViewportHeight(true)
		self.local_bpms_table.setPreferredScrollableViewportSize(Dimension(450,100))
		table_panel = JScrollPane(self.local_bpms_table)
		table_panel.setBorder(BorderFactory.createEtchedBorder())
		#-------------------------------
		knob_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		re_analyze_button = JButton(" Re-Analyze Scans ")
		re_analyze_button.addActionListener(Re_Analyze_Button_Listener(self.main_loop_controller,self.cav_controller))	
		set_to_epics_button = JButton(" Set New Phase to EPICS ")
		set_to_epics_button.addActionListener(Set_New_Phase_to_EPICS_Button_Listener(self.main_loop_controller,self.cav_controller))			
		knob_panel.add(re_analyze_button)
		knob_panel.add(set_to_epics_button)
		#-------------------------------------
		sine_min_acc_phase_label = JLabel("Min. acc. phase[deg]=",JLabel.RIGHT)
		self.sine_min_acc_phase_text =  DoubleInputTextField(0.0,FortranNumberFormat("G10.5"),10)
		sine_max_acc_phase_label = JLabel("Max. acc. phase[deg]=",JLabel.RIGHT)
		self.sine_max_acc_phase_text =  DoubleInputTextField(0.0,FortranNumberFormat("G10.5"),10)		
		zero_acc_phase_label = JLabel("Zero acc. phase[deg]=",JLabel.RIGHT)
		self.zero_acc_phase_text =  DoubleInputTextField(0.0,FortranNumberFormat("G10.5"),10)	
		zero_acc_phase_err_label = JLabel("Err. Zero acc. phase[deg]=",JLabel.RIGHT)
		self.zero_acc_phase_err_text =  DoubleInputTextField(0.0,FortranNumberFormat("G10.5"),10)			
		cav_energy_gain_label = JLabel("Cavity's E0TL [keV]=",JLabel.RIGHT)
		self.cav_energy_gain_text =  DoubleInputTextField(0.0,FortranNumberFormat("G10.5"),10)
		self.sine_min_acc_phase_text.setHorizontalAlignment(JTextField.CENTER)
		self.sine_max_acc_phase_text.setHorizontalAlignment(JTextField.CENTER)
		self.zero_acc_phase_text.setHorizontalAlignment(JTextField.CENTER)
		self.zero_acc_phase_err_text.setHorizontalAlignment(JTextField.CENTER)
		self.cav_energy_gain_text.setHorizontalAlignment(JTextField.CENTER)
		res_params_panel = JPanel(GridLayout(5,2,1,1))
		res_params_panel.add(sine_min_acc_phase_label)
		res_params_panel.add(self.sine_min_acc_phase_text)
		res_params_panel.add(sine_max_acc_phase_label)
		res_params_panel.add(self.sine_max_acc_phase_text)
		res_params_panel.add(zero_acc_phase_label)
		res_params_panel.add(self.zero_acc_phase_text)
		res_params_panel.add(zero_acc_phase_err_label)
		res_params_panel.add(self.zero_acc_phase_err_text)		
		res_params_panel.add(cav_energy_gain_label)
		res_params_panel.add(self.cav_energy_gain_text)
		#-------------------------------------
		tmp_panel0 = JPanel(BorderLayout())
		tmp_panel0.add(table_panel,BorderLayout.NORTH)
		tmp_panel0.add(knob_panel,BorderLayout.SOUTH)
		tmp_panel1 = JPanel(BorderLayout())
		tmp_panel1.add(tmp_panel0,BorderLayout.NORTH)
		tmp_panel1.add(res_params_panel,BorderLayout.CENTER)
		#--------------------------------------
		re_analyze_sine_like_button = JButton(" Re-Analyze Sine-Like Scan ")
		re_analyze_sine_like_button.addActionListener(Re_Analyze_Sine_Like_Button_Listener(self.main_loop_controller,self.cav_controller))			
		tmp_panel2 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		tmp_panel2.add(re_analyze_sine_like_button)
		tmp_panel3 = JPanel(BorderLayout())
		tmp_panel3.add(tmp_panel1,BorderLayout.CENTER)
		tmp_panel3.add(tmp_panel2,BorderLayout.SOUTH)
		#---------------------------------------
		self.cav_off_on_bpm_table = JTable(Cav_Off_On_BPM_Data_Table_Model(main_loop_controller,cav_controller))
		self.cav_off_on_bpm_table.setFillsViewportHeight(true)
		self.cav_off_on_bpm_table.setPreferredScrollableViewportSize(Dimension(450,40))
		cav_off_on_table_panel = JScrollPane(self.cav_off_on_bpm_table)
		etched_border = BorderFactory.createEtchedBorder()
		cav_off_on_table_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavity Off/On BPM's Data"))
		tmp_panel4 = JPanel(BorderLayout())
		tmp_panel4.add(tmp_panel3,BorderLayout.CENTER)
		tmp_panel4.add(cav_off_on_table_panel,BorderLayout.SOUTH)		
		#--------------------------------------
		start_iterarion_fitting_button = JButton("Start Iteration Fitting")
		start_iterarion_fitting_button.addActionListener(Start_Iteration_Fit_Button_Listener(self.main_loop_controller,self.cav_controller))	
		stop_iterarion_fitting_button = JButton("Stop")
		stop_iterarion_fitting_button.addActionListener(Stop_Iteration_Fit_Button_Listener(self.main_loop_controller,self.cav_controller))
		tmp_panel5 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		tmp_panel5.add(start_iterarion_fitting_button)
		tmp_panel5.add(stop_iterarion_fitting_button)
		tmp_panel6 = JPanel(BorderLayout())
		tmp_panel6.add(tmp_panel4,BorderLayout.NORTH)
		tmp_panel6.add(tmp_panel5,BorderLayout.CENTER)	
		#--------------------------------------
		tmp_panel6.add(self.cav_controller.getScanProgressBarPanel(),BorderLayout.SOUTH)		
		#--------------------------------------
		self.add(tmp_panel6,BorderLayout.NORTH)
		
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------		
class Re_Analyze_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")		
		sine_bpm_scan_data = self.cav_controller.cav_bpms_controller.getBPM_ScanData_for_SineWave()
		self.cav_controller.graphs_panel.setSelectedIndex(0)	
		if(not sine_bpm_scan_data.makeHarmonicFit()):
			self.main_loop_controller.getMessageTextField().setText("Cannot make a harmonic fit! Bad data!")
			return
		second_stage_use = self.cav_controller.scan_main_panel.use_amp_phase_scan_button.isSelected()
		second_stage_use = second_stage_use and self.cav_controller.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.isSelected()
		if(not second_stage_use):
			(res,txt) = self.cav_controller.findGuessCavParametersAfterSineLikeScan()
			if(not res):
				self.main_loop_controller.getMessageTextField().setText(txt)
			return			
		self.cav_controller.graphs_panel.setSelectedIndex(1)
		(res,cav_zero_accel_phase,cav_zero_accel_phase_err) = self.cav_controller.cav_bpms_controller.findIntersection()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText("Cannot find intersection! Bad data!")
			return
		self.cav_controller.graphs_panel.updateGraphs()
		self.cav_controller.graphs_panel.setFoundCavityPhase(cav_zero_accel_phase)	
		local_bpms_table_panel = self.cav_controller.local_bpms_table_panel
		local_bpms_table_panel.sine_min_acc_phase_text.setValue(sine_bpm_scan_data.min_accel_phase)
		local_bpms_table_panel.sine_max_acc_phase_text.setValue(sine_bpm_scan_data.max_accel_phase )		
		local_bpms_table_panel.zero_acc_phase_text.setValue(makePhaseNear(cav_zero_accel_phase,0.))
		local_bpms_table_panel.zero_acc_phase_err_text.setValue(cav_zero_accel_phase_err)
		maxE_gain = self.cav_controller.cav_bpms_controller.maxE_gain
		local_bpms_table_panel.cav_energy_gain_text.setValue(maxE_gain)
				
class Set_New_Phase_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")	
		local_bpms_table_panel = self.cav_controller.local_bpms_table_panel
		cav_zero_accel_phase = local_bpms_table_panel.zero_acc_phase_text.getValue()
		self.cav_controller.cav_wrapper.setLivePhase(cav_zero_accel_phase)
		self.cav_controller.cav_wrapper.newPhase = makePhaseNear(cav_zero_accel_phase,0.)	
		cav_ind = self.main_loop_controller.cav_wrappers.index(self.cav_controller.cav_wrapper)
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)		
	
class Re_Analyze_Sine_Like_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")		
		sine_bpm_scan_data = self.cav_controller.cav_bpms_controller.getBPM_ScanData_for_SineWave()
		bpm_wrapper = sine_bpm_scan_data.bpm_wrapper
		self.cav_controller.graphs_panel.setSelectedIndex(0)
		(res,txt) = self.cav_controller.findGuessCavParametersAfterSineLikeScan()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)
			return
		
class Start_Iteration_Fit_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")		
		if(self.main_loop_controller.loop_run_state.isRunning):
			messageTextField.setText("The measurement loop already running!")
			return
		self.cav_controller.setMaxTimeCount()
		sine_bpm_scan_data = self.cav_controller.cav_bpms_controller.getBPM_ScanData_for_SineWave()
		bpm_wrapper = sine_bpm_scan_data.bpm_wrapper
		self.cav_controller.graphs_panel.setSelectedIndex(0)
		(res,txt) = self.cav_controller.findGuessCavParametersAfterSineLikeScan()
		if(not res):
			messageTextField.setText(txt)
			return
		#-----------------------------------------------------
		runner = Cav_Phase_Iteration_Fitting_Runner(self.main_loop_controller,self.cav_controller)
		thr = Thread(runner)
		thr.start()	
		
class Stop_Iteration_Fit_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		if(not self.main_loop_controller.loop_run_state.isRunning):
			messageTextField.setText("The cavity's phase iterative fitting is not running!")
			return	
		self.main_loop_controller.loop_run_state.shouldStop = true				

class Remove_One_Point_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")	
		self.cav_controller.graphs_panel.removeOnePoint()
		
#------------------------------------------------
#  JTable models
#------------------------------------------------
class Local_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.columnNames = ["BPM",]
		self.columnNames += ["Rel.Pos.[m]",]
		self.columnNames += ["Sine-Like Scan",]
		self.columnNames += ["Use for SetUp",]
		self.columnNames += ["Show",]
		self.columnNames += ["Use",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.cav_controller.cav_bpms_controller.local_bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.cav_controller.cav_bpms_controller.local_bpm_wrappers[row]
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		local_bpm_isInUse = cav_bpms_controller.local_bpm_isInUse
		res_sine_use = false
		if(row == cav_bpms_controller.work_sine_bpm_index): res_sine_use = true
		res_scan_use = false
		if(row == cav_bpms_controller.work_bpm_index): res_scan_use = true
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return "%6.3f"%(bpm_wrapper.pos - self.cav_controller.cav_wrapper.pos)
		if(col == 2): return res_sine_use
		if(col == 3): return res_scan_use
		if(col == 4): return local_bpm_isInUse[row]
		if(col == 5): return bpm_wrapper.isOn
		return ""
				
	def getColumnClass(self,col):
		if(col == 0 or col == 1):
			return self.string_class	
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 0 or col == 1):
			return false
		return true
			
	def setValueAt(self, value, row, col):
		if(col == 0 or col == 1): return
		self.main_loop_controller.getMessageTextField().setText("")
		bpm_wrapper = self.cav_controller.cav_bpms_controller.local_bpm_wrappers[row]		
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		if(col == 2 and value):
			if(bpm_wrapper.isOn):			
				cav_bpms_controller.work_sine_bpm_index = row
				cav_bpms_controller.local_bpm_isInUse[row] = true
			else:
				self.main_loop_controller.getMessageTextField().setText("BPM "+bpm_wrapper.alias+" not in use!")
		if(col == 3 and value):
			if(bpm_wrapper.isOn):
				cav_bpms_controller.work_bpm_index = row
				cav_bpms_controller.local_bpm_isInUse[row] = true
			else:
				self.main_loop_controller.getMessageTextField().setText("BPM "+bpm_wrapper.alias+" not in use!")
			self.cav_controller.local_bpms_table_panel.cav_off_on_bpm_table.getModel().fireTableDataChanged()
		if(col == 4):
			if(not value):
				if(row != cav_bpms_controller.work_sine_bpm_index and row != cav_bpms_controller.work_bpm_index):
					cav_bpms_controller.local_bpm_isInUse[row] =  value
			else:
					cav_bpms_controller.local_bpm_isInUse[row] =  value
		if(col == 5):
			if(not value):
				res = self.main_loop_controller.setBPM_Off(bpm_wrapper)
				if(res):
					bpm_wrapper.isOn = false
				else:
					bpm_wrapper.isOn = true
			else:
				bpm_wrapper.isOn = true
		self.fireTableDataChanged()
		self.cav_controller.graphs_panel.updateGraphs()	
			
class Cav_Off_On_BPM_Data_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.columnNames = ["BPM Signal",]
		self.columnNames += ["Cavity Off",]
		self.columnNames += ["Cavity On",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 2

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		bpm_scan_data = cav_bpms_controller.sin_wave_scan_data_arr[cav_bpms_controller.work_bpm_index]
		if(col == 0):
			if(row == 0): return bpm_scan_data.bpm_wrapper.alias+" phase"
			if(row == 1): return bpm_scan_data.bpm_wrapper.alias+" ampl."
		res = ""
		if(col == 1):
			if(row == 0): 
				res = " %4.1f +- %4.1f "%(bpm_scan_data.cav_off_bpm_phase,bpm_scan_data.cav_off_bpm_phase_err)
			if(row == 1): 
				res = " %4.1f +- %4.1f "%(bpm_scan_data.cav_off_bpm_amp,bpm_scan_data.cav_off_bpm_amp_err)
		if(col == 2):
			if(row == 0): 
				res = " %4.1f +- %4.1f "%(bpm_scan_data.cav_on_bpm_phase,bpm_scan_data.cav_on_bpm_phase_err)
			if(row == 1): 
				res = " %4.1f +- %4.1f "%(bpm_scan_data.cav_on_bpm_amp,bpm_scan_data.cav_on_bpm_amp_err)
		return res
				
	def getColumnClass(self,col):
		return self.string_class	
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		return
#------------------------------------------------------------------------
#           Auxiliary Controllers
#------------------------------------------------------------------------		
class Cavity_BPMs_Controller:
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		#---- local BPMs
		self.local_bpm_wrappers = []
		self.local_bpm_isInUse = []
		self.sin_wave_scan_data_arr = []
		for bpm_wrapper in self.main_loop_controller.bpm_wrappers:
			if(bpm_wrapper.pos > self.cav_controller.cav_wrapper.pos):
				if(self.useThisBPM(bpm_wrapper)):
					self.local_bpm_wrappers.append(bpm_wrapper)
					self.local_bpm_isInUse.append(true)
					bpm_scan_data = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,bpm_wrapper)
					self.sin_wave_scan_data_arr.append(bpm_scan_data)
		#---- BPM for "sine" wave-form scan
		self.work_sine_bpm_index = 0
		#---- BPM that will be used for the cross-line position analysis
		self.work_bpm_index = 0
		#---- maximal energy gain by the cavity in keV
		self.maxE_gain = 0.
		#---- self.scan_data_dict[bpm_wrapper] = [BPM_Scan_Data, ...]
		self.scan_data_dict = {}
		for bpm_wrapper in self.local_bpm_wrappers:
			self.scan_data_dict[bpm_wrapper] = []
		#---- cavity amp vs. energy gain
		self.calibr_gd = BasicGraphData()
		self.calibr_gd.setLineThick(3)
		self.calibr_gd.setGraphPointSize(7)
		self.calibr_gd.setGraphColor(Color.BLUE)
		self.calibr_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Calibration for cav= "+self.cav_controller.cav_wrapper.alias)
		self.calibr_gd.setDrawLinesOn(true)
		self.calibr_gd.setDrawPointsOn(true)	
		#-----------------------------------
		self.calibr_fit_gd = BasicGraphData()
		self.calibr_fit_gd.setLineThick(3)
		self.calibr_gd.setGraphPointSize(7)
		self.calibr_fit_gd.setGraphColor(Color.RED)
		self.calibr_fit_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Fit claib. for cav= "+self.cav_controller.cav_wrapper.alias)
		self.calibr_fit_gd.setDrawLinesOn(true)
		self.calibr_fit_gd.setDrawPointsOn(true)			
			
	def useThisBPM(self,bpm_wrapper):
		""" This method could be overriden to accomodate other beam lines. """
		return (bpm_wrapper.alias.find("MEBT") >= 0)
			
	def getBPM_ScanData_for_SineWave(self):
		return self.sin_wave_scan_data_arr[self.work_sine_bpm_index]
			
	def clean(self):
		for bpm_wrapper in self.local_bpm_wrappers:
			self.scan_data_dict[bpm_wrapper] = []
		for bpm_scan_data in self.sin_wave_scan_data_arr:
			bpm_scan_data.clean()	
		self.calibr_gd.removeAllPoints()
		self.calibr_fit_gd.removeAllPoints()
	
	def addPointToSineWave(self,cav_phase):
		for bpm_scan_data in self.sin_wave_scan_data_arr:
			bpm_scan_data.addPoint(cav_phase)
			
	def setCavAmpToSineWave(self):
		cav_amp = self.cav_controller.cav_wrapper.getLiveAmp()
		for bpm_scan_data in self.sin_wave_scan_data_arr:
			bpm_scan_data.setCavAmplitudeParam(cav_amp)	
			
	def addNewCavAmpSimpleDataSet(self,cav_amp):
		for bpm_wrapper in self.local_bpm_wrappers:
			bpm_scan_data = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,bpm_wrapper)
			bpm_scan_data.setCavAmplitudeParam(cav_amp)	
			self.scan_data_dict[bpm_wrapper].append(bpm_scan_data)
			
	def addPointToSimpleDataSet(self,cav_phase):
		for bpm_wrapper_ind in range(len(self.local_bpm_wrappers)):
			bpm_wrapper = self.local_bpm_wrappers[bpm_wrapper_ind]
			n_data = len(self.scan_data_dict[bpm_wrapper])
			if(n_data == 0 or (not self.local_bpm_isInUse[bpm_wrapper_ind])): continue
			bpm_scan_data = self.scan_data_dict[bpm_wrapper][n_data-1]
			bpm_scan_data.addPoint(cav_phase)
			
	def checkLastDataForSineWave(self):
		min_bpm_amp = self.cav_controller.scan_main_panel.min_bpm_amp_text.getValue()
		res = true
		if(not self.sin_wave_scan_data_arr[self.work_sine_bpm_index].checkLastDataPoint(min_bpm_amp)):
			res = false
			for bpm_scan_data in self.sin_wave_scan_data_arr:
				bpm_scan_data.removeLastPoint()
		return res
		
	def checkLastDataForSimple(self):
		min_bpm_amp = self.cav_controller.scan_main_panel.min_bpm_amp_text.getValue()
		res = true
		for bpm_wrapper_ind in range(len(self.local_bpm_wrappers)):
			bpm_wrapper = self.local_bpm_wrappers[bpm_wrapper_ind]
			n_data = len(self.scan_data_dict[bpm_wrapper])
			if(n_data == 0 or (not self.local_bpm_isInUse[bpm_wrapper_ind])): continue
			bpm_scan_data = self.scan_data_dict[bpm_wrapper][n_data-1]
			if(bpm_wrapper_ind == self.work_bpm_index):
				if(not bpm_scan_data.checkLastDataPoint(min_bpm_amp)):
					res = false
					break
		if(not res):
			for bpm_wrapper_ind in range(len(self.local_bpm_wrappers)):
				bpm_wrapper = self.local_bpm_wrappers[bpm_wrapper_ind]
				n_data = len(self.scan_data_dict[bpm_wrapper])
				if(n_data == 0 or (not self.local_bpm_isInUse[bpm_wrapper_ind])): continue
				bpm_scan_data = self.scan_data_dict[bpm_wrapper][n_data-1]
				bpm_scan_data.removeLastPoint()
		return res		
			
	def findIntersection(self):
		self.calibr_gd.removeAllPoints()
		self.calibr_fit_gd.removeAllPoints()
		res = true
		cav_zero_accel_phase = 0.
		cav_zero_accel_phase_err = 0.
		bpm_wrapper = self.local_bpm_wrappers[self.work_bpm_index]
		bpm_scan_data_arr = self.scan_data_dict[bpm_wrapper]
		nAmps = len(bpm_scan_data_arr)
		#-----------------------------------------------------------
		if(nAmps < 2):
			return (false,cav_zero_accel_phase,cav_zero_accel_phase_err)
		#-----------------------------------------------------------
		if(bpm_scan_data_arr[nAmps-1].phase_gd.getNumbOfPoints() > 0):
			for bpm_scan_data in bpm_scan_data_arr:
				bpm_scan_data.shiftToPhase(bpm_scan_data.phase_gd,bpm_scan_data_arr[nAmps-1].phase_gd.getY(0))
		#------------------------------------------------------------
		for bpm_scan_data in bpm_scan_data_arr:
			if(not bpm_scan_data.makeLinearFit()):
				return (false,cav_zero_accel_phase,cav_zero_accel_phase_err)
		#---- cavity energy gain coeff. to dPhaseBPM/dPhaseCav
		mass = 938.272+2*0.511
		e_kin = 2.5
		bpm_freq = 805.0e+6
		c = 2.9979e+8
		beta = math.sqrt(2*e_kin/mass)
		energy_gain_coeff = e_kin*(2.0*beta*c/(2*math.pi*bpm_freq*(bpm_wrapper.pos - self.cav_controller.cav_wrapper.pos)))	
		#----------------------------------------------
		for bpm_scan_data in bpm_scan_data_arr:
			cav_amp = bpm_scan_data.cav_amp
			derivative = bpm_scan_data.derivative
			deltaE = energy_gain_coeff*math.fabs(derivative)
			self.calibr_gd.addPoint(cav_amp,deltaE*1000.)
		self.maxE_gain = self.calibr_gd.getY(self.calibr_gd.getNumbOfPoints()-1)
		max_cav_amp = self.calibr_gd.getX(self.calibr_gd.getNumbOfPoints()-1)
		min_cav_amp = self.calibr_gd.getX(0)
		#GraphDataOperations.polynomialFit(self.calibr_gd,self.calibr_fit_gd,1)
		fit_arr = GraphDataOperations.polynomialFit(self.calibr_gd,min_cav_amp,max_cav_amp,1)
		if(fit_arr == null):
			cav_zero_accel_phase = 0.
			cav_zero_accel_phase_err = 0.			
			return (false,cav_zero_accel_phase,cav_zero_accel_phase_err)
		nAmpPoints = 5
		for amp_ind in range(nAmpPoints):
			cav_amp = max_cav_amp*amp_ind/(nAmpPoints-1)
			e_gain = fit_arr[0][0]+cav_amp*fit_arr[0][1]
			e_gain_err = math.sqrt((fit_arr[1][0])**2 + (cav_amp*fit_arr[1][1])**2)
			self.calibr_fit_gd.addPoint(cav_amp,e_gain,e_gain_err)
		self.calibr_fit_gd.setGraphColor(Color.RED)
		cav_zero_accel_phase_arr = []
		for ind1 in range(len(bpm_scan_data_arr)):
			gd1 = bpm_scan_data_arr[ind1].phase_fit_gd
			for ind2 in range(ind1+1,len(bpm_scan_data_arr)):
				gd2 = bpm_scan_data_arr[ind2].phase_fit_gd
				x_cross = GraphDataOperations.findIntersectionX(gd1,gd2, -10000., +10000.,0.01) 
				if(x_cross != null):
					cav_zero_accel_phase_arr.append(x_cross)
		(cav_zero_accel_phase,cav_zero_accel_phase_err) = calculateAvgErr(cav_zero_accel_phase_arr)
		return (res,cav_zero_accel_phase,cav_zero_accel_phase_err)

	def writeDataToXML(self,root_da):
		cav_bpms_cntrl_da = root_da.createChild("cavity_bpms_controller")
		cav_bpms_cntrl_da.setValue("cav",self.cav_controller.cav_wrapper.alias)
		cav_bpms_cntrl_da.setValue("work_sine_bpm_index",self.work_sine_bpm_index)
		cav_bpms_cntrl_da.setValue("work_bpm_index",self.work_bpm_index)
		#---------------------------------------------------
		dumpGraphDataToDA(self.calibr_gd,cav_bpms_cntrl_da,"amp_calibration_gd")
		dumpGraphDataToDA(self.calibr_fit_gd,cav_bpms_cntrl_da,"amp_calibration_fit_gd")
		#---------------------------------------------------		
		for bpm_scan_data in self.sin_wave_scan_data_arr:
			bpm_scan_data.writeDataToXML(cav_bpms_cntrl_da)
		#---------------------------------------------------
		for bpm_wrapper in self.local_bpm_wrappers:
			bpm_scan_data_da = cav_bpms_cntrl_da.createChild("amp_phase_scan_data_"+bpm_wrapper.alias)
			for bpm_scan_data in self.scan_data_dict[bpm_wrapper]:
				bpm_scan_data.writeDataToXML(bpm_scan_data_da)
		#---------------------------------------------------
		inUse_arr_da = cav_bpms_cntrl_da.createChild("inUse_bpms_arr")
		st = ""
		for res in self.local_bpm_isInUse:
			if(res == true): 
				st += " 1 "
			else:
				st += " 0 "
		inUse_arr_da.setValue("arr",st)

	def readDataFromXML(self,cav_bpms_cntrl_da):	
		self.work_sine_bpm_index = cav_bpms_cntrl_da.intValue("work_sine_bpm_index")
		self.work_bpm_index = cav_bpms_cntrl_da.intValue("work_bpm_index")
		bpm_scan_data_da_list = cav_bpms_cntrl_da.childAdaptors("bpm_scan_data")
		readGraphDataFromDA(self.calibr_gd,cav_bpms_cntrl_da,"amp_calibration_gd")	
		readGraphDataFromDA(self.calibr_fit_gd,cav_bpms_cntrl_da,"amp_calibration_fit_gd")	
		for bpm_scan_data_ind in range(len(self.sin_wave_scan_data_arr)):
			bpm_scan_data = self.sin_wave_scan_data_arr[bpm_scan_data_ind]
			bpm_scan_data_da = bpm_scan_data_da_list.get(bpm_scan_data_ind)
			bpm_scan_data.readDataFromXML(bpm_scan_data_da)
		#--------------------------------------------
		for bpm_wrapper in self.local_bpm_wrappers:
			self.scan_data_dict[bpm_wrapper] = []
			bpm_scan_data_root_da = cav_bpms_cntrl_da.childAdaptor("amp_phase_scan_data_"+bpm_wrapper.alias)
			bpm_scan_data_da_list = bpm_scan_data_root_da.childAdaptors("bpm_scan_data")
			for bpm_scan_data_ind in range(bpm_scan_data_da_list.size()):
				bpm_scan_data_da = bpm_scan_data_da_list.get(bpm_scan_data_ind)
				bpm_scan_data = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,bpm_wrapper)
				bpm_scan_data.readDataFromXML(bpm_scan_data_da)
				self.scan_data_dict[bpm_wrapper].append(bpm_scan_data)
		#--------------------------------------------
		inUse_arr_da = cav_bpms_cntrl_da.childAdaptor("inUse_bpms_arr")
		st = inUse_arr_da.stringValue("arr")
		res_arr = st.split()
		for ind in range(len(self.local_bpm_isInUse)):
			self.local_bpm_isInUse[ind] = false
			if(res_arr[ind] == "1"): self.local_bpm_isInUse[ind] = true

#------------------------------------------------------------------------
#           MEBT Cavity Controller
#------------------------------------------------------------------------
class MEBT_Cavity_Controller(Abstract_Cavity_Controller):
	def __init__(self,main_loop_controller,cav_wrapper):
		Abstract_Cavity_Controller.__init__(self,main_loop_controller,cav_wrapper)
		self.scan_main_panel = Scan_Main_Panel(self.main_loop_controller,self)
		self.cav_bpms_controller = Cavity_BPMs_Controller(self.main_loop_controller,self)
		self.graphs_panel = Graphs_Panel(self.main_loop_controller,self)
		remove_one_point_button = JButton(" Remove One Cav. Phase Point ")
		remove_one_point_button.addActionListener(Remove_One_Point_Button_Listener(main_loop_controller,self))
		graphs_button_panel = JPanel(BorderLayout())
		graphs_button_panel.add(self.graphs_panel,BorderLayout.CENTER)
		graphs_button_panel.add(remove_one_point_button,BorderLayout.SOUTH)
		self.local_bpms_table_panel = Local_BPMs_Table_Panel(self.main_loop_controller,self)
		self.getMainPanel().add(self.scan_main_panel,BorderLayout.NORTH)
		self.getMainPanel().add(graphs_button_panel,BorderLayout.CENTER)
		self.getMainPanel().add(self.local_bpms_table_panel,BorderLayout.WEST)
		self.graphs_panel.updateGraphs()
				
	def runSetUpAlgorithm(self):
		""" Returns (true, text) in the case of the success """
		self.setMaxTimeCount()
		if(self.main_loop_controller.loop_run_state.shouldStop): return (false,"User requested stop! Cavity="+self.cav_wrapper.alias)
		if(self.cav_wrapper.initAmp == 0.): return (false,"Push Init Button First!")
		self.cav_wrapper.newAmp = self.cav_wrapper.initAmp
		self.cav_wrapper.newPhase = 0.
		self.cav_bpms_controller.clean()
		self.graphs_panel.updateGraphs()	
		bpm1 = self.cav_bpms_controller.local_bpm_wrappers[self.cav_bpms_controller.work_sine_bpm_index]
		bpm2 = self.cav_bpms_controller.local_bpm_wrappers[self.cav_bpms_controller.work_bpm_index]		
		if(not (bpm1.isOn and bpm2.isOn)):
			return (false,"We need them! BPMs: "+bpm1.alias+" and "+bpm2.alias+" should be set to <Use> in the Main Table!")
		sine_bpm_scan_data = self.cav_bpms_controller.getBPM_ScanData_for_SineWave()
		phase_step = self.scan_main_panel.full_scan_phase_step_text.getValue()
		sleep_time = self.scan_main_panel.time_step_text.getValue()
		#---- start of sine wave masurements
		self.graphs_panel.setSelectedIndex(0)
		cav_phase = -180.
		miss_count = 0.
		miss_count_max = 5
		self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
		sine_bpm_scan_data.setCavAmplitudeParam(self.cav_wrapper.initAmp)	
		while(cav_phase <= 180.):
			if(miss_count > miss_count_max):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot get valid BPM data for sine-like scan! Stop. Cavity="+self.cav_wrapper.alias)
			self.cav_wrapper.setLivePhase(cav_phase)
			self.timeSleep(sleep_time)
			if(self.main_loop_controller.loop_run_state.shouldStop):	
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
				self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)						
				return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)				
			self.cav_bpms_controller.addPointToSineWave(cav_phase)
			if(not self.cav_bpms_controller.checkLastDataForSineWave()):
				miss_count += 1
				continue
			self.graphs_panel.refreshGraphJPanels()
			cav_phase += phase_step
		#--- end of the sine measurements loop
		#---- make a harmonics fit to sine-like data
		(res,txt) = self.findGuessCavParametersAfterSineLikeScan()
		if(not res):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot fit the sine-like scan data! Stop. Cavity="+self.cav_wrapper.alias)		
		#----- finilazing the sine-like scan by correcting zero_accel_phase
		zero_accel_phase = self.local_bpms_table_panel.zero_acc_phase_text.getValue()
		#-------------PASTA-like Scan Beginning ----------------------------
		#---- if we do nothing here
		if(not self.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.isSelected()):
			return (true,"")
		#---- if we do iterations instead of scan
		if(not self.scan_main_panel.use_amp_phase_scan_button.isSelected()):
			(res,txt) = self.runIterativeCavPhase_Finder()
			return (res,txt)
		self.graphs_panel.setSelectedIndex(1)
		#------------------------------------
		cav_amp_steps_mult = int(self.cav_amp_backward_steps_mult_text.getValue())
		cav_amp_time_mult = int(self.cav_amp_wait_time_mult_text.getValue())
		#------------------------------------		
		min_amp = self.scan_main_panel.min_cav_amp_text.getValue()
		max_amp = self.scan_main_panel.max_cav_amp_text.getValue()
		nAmpSteps = int(self.scan_main_panel.nsteps_cav_amp_text.getValue())
		if(nAmpSteps < 1): nAmpSteps = 1
		amp_step = (max_amp - min_amp)/nAmpSteps
		cav_phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
		phase_step = self.scan_main_panel.scan_phase_step_text.getValue()
		cav_phase_start = zero_accel_phase - cav_phase_width/2.0
		cav_phase_stop = cav_phase_start + cav_phase_width		
		cav_amp = max_amp
		miss_count_max = 5
		for amp_ind in range(nAmpSteps+1):
			cav_amp = max_amp - amp_ind*amp_step
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)		
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			self.cav_bpms_controller.addNewCavAmpSimpleDataSet(cav_amp)
			self.graphs_panel.updateGraphs()			
			cav_phase = cav_phase_start
			miss_count = 0.
			while(cav_phase <= cav_phase_stop):
				if(miss_count > miss_count_max):
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)	
					return (false,"Cannot get valid BPM data for sine-like scan! Stop. Cavity="+self.cav_wrapper.alias)
				self.cav_wrapper.setLivePhase(cav_phase)
				self.timeSleep(sleep_time)
				if(self.main_loop_controller.loop_run_state.shouldStop):	
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)	
					return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)
				self.cav_bpms_controller.addPointToSimpleDataSet(cav_phase)
				if(not self.cav_bpms_controller.checkLastDataForSimple()):
					miss_count += 1
					self.graphs_panel.refreshGraphJPanels()
					continue
				self.graphs_panel.refreshGraphJPanels()
				#---- end loop over cavity's phase	
				cav_phase += phase_step
			#---- end loop over cavity's amplitude
		#---- restore the initial amplitude
		cav_amp += amp_step/cav_amp_steps_mult
		while(cav_amp <= max_amp*1.0001):
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			cav_amp += amp_step/cav_amp_steps_mult
 		#---- analysis
		self.local_bpms_table_panel.cav_energy_gain_text.setValue(0.)	
		(res,cav_zero_accel_phase,cav_zero_accel_phase_err) = self.cav_bpms_controller.findIntersection()
		self.graphs_panel.updateGraphs()
		if(not res):
			self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
			self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			return (false,"Cannot find cavity phase from scan crossing! Stop. Cavity="+self.cav_wrapper.alias)
		self.graphs_panel.setFoundCavityPhase(cav_zero_accel_phase)	
		self.graphs_panel.refreshGraphJPanels()
		self.cav_wrapper.newPhase = makePhaseNear(cav_zero_accel_phase,0.)
		self.cav_wrapper.newAmp = self.cav_wrapper.initAmp
		self.local_bpms_table_panel.zero_acc_phase_text.setValue(self.cav_wrapper.newPhase)	
		self.local_bpms_table_panel.zero_acc_phase_err_text.setValue(cav_zero_accel_phase_err)
		maxE_gain = self.cav_bpms_controller.maxE_gain
		self.local_bpms_table_panel.cav_energy_gain_text.setValue(maxE_gain)
		return (true,"")		
		
	def findGuessCavParametersAfterSineLikeScan(self):	
		sine_bpm_scan_data = self.cav_bpms_controller.getBPM_ScanData_for_SineWave()
		bpm_wrapper = sine_bpm_scan_data.bpm_wrapper
		self.graphs_panel.setSelectedIndex(0)	
		if(not sine_bpm_scan_data.makeHarmonicFit()):
			return (false,"Cannot make a harmonic fit! Bad data!")
		cav_zero_accel_phase = sine_bpm_scan_data.zero_accel_phase
		cav_zero_accel_phase += self.guess_phase_shift_text.getValue() 
		cav_zero_accel_phase = makePhaseNear(cav_zero_accel_phase,0.)
		cav_zero_accel_phase_err = 0.
		self.graphs_panel.updateGraphs()
		self.graphs_panel.setFoundCavityPhase(cav_zero_accel_phase)	
		local_bpms_table_panel = self.local_bpms_table_panel
		local_bpms_table_panel.sine_min_acc_phase_text.setValue(sine_bpm_scan_data.min_accel_phase)
		local_bpms_table_panel.sine_max_acc_phase_text.setValue(sine_bpm_scan_data.max_accel_phase)		
		local_bpms_table_panel.zero_acc_phase_text.setValue(cav_zero_accel_phase)
		local_bpms_table_panel.zero_acc_phase_err_text.setValue(cav_zero_accel_phase_err)
		self.cav_wrapper.newPhase = cav_zero_accel_phase
		#----- update the main table
		cav_ind = self.main_loop_controller.cav_wrappers.index(self.cav_wrapper)
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)		
		#---- cavity energy gain coeff. to dPhaseBPM/dPhaseCav
		mass = 938.272+2*0.511
		e_kin = 2.5
		bpm_freq = 805.0e+6
		c = 2.9979e+8
		beta = math.sqrt(2*e_kin/mass)
		energy_gain_coeff = e_kin*(2.0*beta*c/(2*math.pi*bpm_freq*(bpm_wrapper.pos - self.cav_wrapper.pos)))	
		#--------- maxE_gain in [keV]
		harm_function = sine_bpm_scan_data.harmonicsAnalyzer.getHrmonicsFunction()
		max_bpm_phase = harm_function.getValue(sine_bpm_scan_data.min_accel_phase)
		min_bpm_phase = harm_function.getValue(sine_bpm_scan_data.max_accel_phase)
		derivative = 0.5*math.fabs(max_bpm_phase - min_bpm_phase)*math.pi/180.
		maxE_gain = 1000.0*energy_gain_coeff*derivative
		local_bpms_table_panel.cav_energy_gain_text.setValue(maxE_gain)
		self.cav_bpms_controller.maxE_gain = maxE_gain
		return (true,"")

	def runIterativeCavPhase_Finder(self):
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return (false,"Cannot connect the necessary PVs!")
		sleep_time = self.scan_main_panel.time_step_text.getValue()
		bpm_scan_data = self.cav_bpms_controller.sin_wave_scan_data_arr[self.cav_bpms_controller.work_bpm_index]
		bpm_scan_data.cav_off_bpm_phase = 0.
		bpm_scan_data.cav_off_bpm_phase_err = 0.
		bpm_scan_data.cav_off_bpm_amp = 0.
		bpm_scan_data.cav_off_bpm_amp_err = 0.
		bpm_scan_data.cav_on_bpm_phase = 0.
		bpm_scan_data.cav_on_bpm_phase_err = 0.
		bpm_scan_data.cav_on_bpm_amp = 0.
		bpm_scan_data.cav_on_bpm_amp_err = 0.
		#----- calculate the derivative near the zero acceleration phase
		local_bpms_table_panel = self.local_bpms_table_panel
		local_bpms_table_panel.cav_off_on_bpm_table.getModel().fireTableDataChanged()
		cav_zero_accel_phase = local_bpms_table_panel.zero_acc_phase_text.getValue()
		bpm_phase_arr = []
		cav_phase_arr = []
		cav_phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
		derivative = 0.
		cav_phase_min = +1000.
		cav_phase_max = -1000.
		for ind in range(bpm_scan_data.phase_gd.getNumbOfPoints()):
			cav_phase = makePhaseNear(bpm_scan_data.phase_gd.getX(ind),cav_zero_accel_phase)
			bpm_phase = bpm_scan_data.phase_gd.getY(ind)	
			if(math.fabs(cav_phase - cav_zero_accel_phase) < cav_phase_width*0.7):
				if(len(bpm_phase_arr) > 1):
					bpm_phase = makePhaseNear(bpm_phase,bpm_phase_arr[len(bpm_phase_arr)-1])
				bpm_phase_arr.append(bpm_phase)
				cav_phase_arr.append(cav_phase)
				if(cav_phase > cav_phase_max): cav_phase_max = cav_phase
				if(cav_phase < cav_phase_min): cav_phase_min = cav_phase
		min_cav_phase_spread = 1.0
		if(len(cav_phase_arr) < 2 or math.fabs(cav_phase_max-cav_phase_min) < min_cav_phase_spread):
			self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
			txt = "Cav.="+self.cav_wrapper.alias+". "
			txt +=  "Cannot calculate the BPM's phase derivative for BPM="+bpm_scan_data.bpm_wrapper.alias
			return (false,txt)
		gd = BasicGraphData()
		gd_fit = BasicGraphData()
		for ind in range(len(bpm_phase_arr)):
			bpm_phase = makePhaseNear(bpm_phase_arr[ind],bpm_phase_arr[0])
			gd.addPoint(cav_phase_arr[ind],bpm_phase)
		GraphDataOperations.polynomialFit(gd,gd_fit,1)
		nP = gd_fit.getNumbOfPoints()
		x = (gd_fit.getX(0)+gd_fit.getX(nP-1))/2.
		derivative = gd_fit.getValueDerivativeY(x)
		#----- we solve the inverse problem
		if(derivative != 0.): derivative = 1.0/derivative
		cav_zero_accel_phase_err = 0.
		#---------------------------------------------------------------
		nAvg = int(self.scan_main_panel.bpm_iter_avg_text.getValue())
		nSteps = int(self.scan_main_panel.bpm_iter_steps_text.getValue())
		bad_points_max = 5
		min_bpm_amp = self.scan_main_panel.min_bpm_amp_text.getValue()
		self.cav_wrapper.setLivePhase(cav_zero_accel_phase)
		for step_ind in range(nSteps):
			#print "debug === step ind=",step_ind
			bad_points = 0
			cav_phase = self.cav_wrapper.getLivePhase()
			cav_off_phase_arr = []
			cav_on_phase_arr = []
			cav_off_amp_arr = []
			cav_on_amp_arr = []	
			#-------- cav On ---------
			self.cav_wrapper.setBlankBeam(false)	
			count = 0
			while(count < nAvg and bad_points < bad_points_max):
				#print "debug ============== cav on iter=",count," out of ",nAvg
				if(self.main_loop_controller.loop_run_state.shouldStop): break				
				self.timeSleep(sleep_time)
				if(self.main_loop_controller.loop_run_state.shouldStop): break
				(bpm_amp,bpm_phase) = bpm_scan_data.getAmpAndPhase()
				if(bpm_amp > min_bpm_amp):
					cav_on_phase_arr.append(bpm_phase)
					cav_on_amp_arr.append(bpm_amp)
					count += 1
				else:
					bad_points += 1
			#-------- cav Off --------
			self.cav_wrapper.setBlankBeam(true)	
			count = 0
			while(count < nAvg and bad_points < bad_points_max):
				#print "debug ============== cav off iter=",count," out of ",nAvg
				if(self.main_loop_controller.loop_run_state.shouldStop): break				
				self.timeSleep(sleep_time)
				if(self.main_loop_controller.loop_run_state.shouldStop): break
				(bpm_amp,bpm_phase) = bpm_scan_data.getAmpAndPhase()
				if(bpm_amp > min_bpm_amp):
					cav_off_phase_arr.append(bpm_phase)
					cav_off_amp_arr.append(bpm_amp)
					count += 1 
				else:
					bad_points += 1		
			if(bad_points >= bad_points_max):
				self.cav_wrapper.setBlankBeam(false)
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)				
				txt = "Cav.="+self.cav_wrapper.alias
				txt = txt + "A lot of bad BPM data. BPM="+bpm_scan_data.bpm_wrapper.alias
				return (false,txt)
			#--------------------------
			if(len(cav_on_phase_arr) < 1 or len(cav_off_phase_arr) < 1):
				self.cav_wrapper.setBlankBeam(false)
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)				
				txt = "Cav.="+self.cav_wrapper.alias+". Not enough data for the iterative fitting." 
				return (false,txt)
			(cav_off_phase_avg,cav_off_phase_err) = calculateAvgErr(cav_off_phase_arr)
			(cav_on_phase_avg,cav_on_phase_err) = calculateAvgErr(cav_on_phase_arr)
			(cav_off_amp_avg,cav_off_amp_err) = calculateAvgErr(cav_off_amp_arr)
			(cav_on_amp_avg,cav_on_amp_err) = calculateAvgErr(cav_on_amp_arr)
			delta_bpm_phase = makePhaseNear(cav_on_phase_avg - cav_off_phase_avg,0.)
			cav_phase =  makePhaseNear(cav_phase - derivative*delta_bpm_phase,0.)
			self.cav_wrapper.setLivePhase(cav_phase)
			self.timeSleep(sleep_time)
			bpm_scan_data.cav_off_bpm_phase = cav_off_phase_avg
			bpm_scan_data.cav_off_bpm_phase_err = cav_off_phase_err
			bpm_scan_data.cav_off_bpm_amp = cav_off_amp_avg
			bpm_scan_data.cav_off_bpm_amp_err = cav_off_amp_err
			bpm_scan_data.cav_on_bpm_phase = cav_on_phase_avg
			bpm_scan_data.cav_on_bpm_phase_err = cav_on_phase_err
			bpm_scan_data.cav_on_bpm_amp = cav_on_amp_avg
			bpm_scan_data.cav_on_bpm_amp_err = cav_on_amp_err	
			phase_err2 = (delta_bpm_phase)**2 + cav_off_phase_err**2 + cav_on_phase_err**2
			cav_zero_accel_phase_err = math.fabs(derivative*math.sqrt(phase_err2))
			local_bpms_table_panel.cav_off_on_bpm_table.getModel().fireTableDataChanged()
			if(self.main_loop_controller.loop_run_state.shouldStop): break
			#print "debug ======================= end of step ================="
		#---------------------------------------------
		self.cav_wrapper.setBlankBeam(false)
		cav_zero_accel_phase = self.cav_wrapper.getLivePhase()
		bpm_scan_data.zero_accel_phase = cav_zero_accel_phase
		self.graphs_panel.setFoundCavityPhase(cav_zero_accel_phase)	
		self.graphs_panel.refreshGraphJPanels()
		self.cav_wrapper.newPhase = makePhaseNear(cav_zero_accel_phase,0.)
		self.cav_wrapper.newAmp = self.cav_wrapper.initAmp
		local_bpms_table_panel.zero_acc_phase_text.setValue(self.cav_wrapper.newPhase)	
		local_bpms_table_panel.zero_acc_phase_err_text.setValue(cav_zero_accel_phase_err)
		if(self.main_loop_controller.loop_run_state.shouldStop):
			self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
			return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)		
		return (true,"")

	def init(self):
		""" reads the pv values """
		Abstract_Cavity_Controller.init(self)
		#------ set up amp scan parameters
		initAmp_max = self.cav_wrapper.initAmp
		initAmp_min = initAmp_max*0.75
		nAmpStep = 2
		self.scan_main_panel.min_cav_amp_text.setValue(initAmp_min)
		self.scan_main_panel.max_cav_amp_text.setValue(initAmp_max)
		self.scan_main_panel.nsteps_cav_amp_text.setValue(nAmpStep)
		self.scan_main_panel.live_cav_amp_text.setValue(initAmp_max)

	def checkBPM_Usage(self,bpm_wrapper):
		""" 
		Implementation of the abstarct method. 
		Returns True or False about this controller usage of the BPM 
		"""
		cav_bpms_controller = self.cav_bpms_controller
		local_bpm_wrappers = cav_bpms_controller.local_bpm_wrappers
		if(local_bpm_wrappers[cav_bpms_controller.work_sine_bpm_index] == bpm_wrapper): return true
		if(local_bpm_wrappers[cav_bpms_controller.work_bpm_index] == bpm_wrapper): return true
		return false

	def setMaxTimeCount(self):
		""" This is implementation of the abstarct method of the parent class."""
		totalTimeCount = 0.
		time_step = self.scan_main_panel.time_step_text.getValue()
		#----- 360 deg scan 
		totalTimeCount += time_step*(1.0+360./self.scan_main_panel.full_scan_phase_step_text.getValue())
		#----- PASTA-like scan ----------------------------------------
		if(self.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.isSelected()):
			if(self.scan_main_panel.use_amp_phase_scan_button.isSelected()):
				phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
				phase_step = self.scan_main_panel.scan_phase_step_text.getValue()
				n_amp_steps = self.scan_main_panel.nsteps_cav_amp_text.getValue()
				n_amp_back_steps_mult = self.cav_amp_backward_steps_mult_text.getValue()
				n_amp_back_time_mult = self.cav_amp_wait_time_mult_text.getValue()
				totalTimeCount += time_step*(n_amp_steps+1)*(1+phase_width/phase_step)
				totalTimeCount += time_step*(n_amp_steps+1)*n_amp_back_time_mult
				totalTimeCount += time_step*(n_amp_steps+1)*n_amp_back_steps_mult*n_amp_back_time_mult
			#---- Cavity Phase Iterative Fitter
			else:
				nAvg = self.scan_main_panel.bpm_iter_avg_text.getValue()
				nSteps = self.scan_main_panel.bpm_iter_steps_text.getValue()		
				totalTimeCount += (2*nAvg*nSteps + 1)*time_step
		self.scan_progress_bar.setMaxTimeCount(totalTimeCount)
		return self.scan_progress_bar.count_max
		
	def writeDataToXML(self,root_da):
		cav_cntrl_data_da = root_da.createChild("CAVITY_CONTROLLER_"+self.cav_wrapper.alias)
		cav_cntrl_params_da =  cav_cntrl_data_da.createChild("PARAMS")
		cav_cntrl_params_da.setValue("sine_phase_step",self.scan_main_panel.full_scan_phase_step_text.getValue())
		cav_cntrl_params_da.setValue("sleep_time",self.scan_main_panel.time_step_text.getValue())
		cav_cntrl_params_da.setValue("min_amp",self.scan_main_panel.min_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("max_amp",self.scan_main_panel.max_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("n_amp_step",self.scan_main_panel.nsteps_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("cav_phase_width",self.scan_main_panel.scan_phase_width_text.getValue())
		cav_cntrl_params_da.setValue("phase_step",self.scan_main_panel.scan_phase_step_text.getValue())
		cav_cntrl_params_da.setValue("use_second_stage_scan",self.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.isSelected())
		cav_cntrl_params_da.setValue("use_amp_phase_scan",self.scan_main_panel.use_amp_phase_scan_button.isSelected())
		cav_cntrl_params_da.setValue("iter_n_avg",self.scan_main_panel.bpm_iter_avg_text.getValue())
		cav_cntrl_params_da.setValue("iter_n_steps",self.scan_main_panel.bpm_iter_steps_text.getValue())
		#---------------------------------------------------------
		self.wrtiteAbstractCntrlToXML(cav_cntrl_data_da)
		#---------------------------------------------------------		
		analysis_params_da =  cav_cntrl_data_da.createChild("ANALYSIS_PARAMS")
		analysis_params_da.setValue("sine_min_acc_phase_text",self.local_bpms_table_panel.sine_min_acc_phase_text.getValue())
		analysis_params_da.setValue("sine_max_acc_phase_text",self.local_bpms_table_panel.sine_max_acc_phase_text.getValue())
		analysis_params_da.setValue("zero_acc_phase_text",self.local_bpms_table_panel.zero_acc_phase_text.getValue())
		analysis_params_da.setValue("zero_acc_phase_err_text",self.local_bpms_table_panel.zero_acc_phase_err_text.getValue())		
		analysis_params_da.setValue("cav_energy_gain_text",self.local_bpms_table_panel.cav_energy_gain_text.getValue())
		#---------------------------------------------------------
		cav_wrapper_da = cav_cntrl_data_da.createChild("CAV_WRAPPER_PARAMS")
		self.cav_wrapper.writeDataToXML(cav_wrapper_da)
		self.cav_bpms_controller.writeDataToXML(cav_cntrl_data_da)
		
	def readDataFromXML(self,cav_cntrl_data_da):	
		params_da = cav_cntrl_data_da.childAdaptor("PARAMS")
		self.scan_main_panel.full_scan_phase_step_text.setValue(params_da.doubleValue("sine_phase_step"))
		self.scan_main_panel.time_step_text.setValue(params_da.doubleValue("sleep_time"))
		self.scan_main_panel.min_cav_amp_text.setValue(params_da.doubleValue("min_amp"))
		self.scan_main_panel.max_cav_amp_text.setValue(params_da.doubleValue("max_amp"))
		self.scan_main_panel.nsteps_cav_amp_text.setValue(params_da.doubleValue("n_amp_step"))
		self.scan_main_panel.scan_phase_width_text.setValue(params_da.doubleValue("cav_phase_width"))
		self.scan_main_panel.scan_phase_step_text.setValue(params_da.doubleValue("phase_step"))
		if(params_da.hasAttribute("use_second_stage_scan")):
			use_it = int(params_da.intValue("use_second_stage_scan"))
			if(use_it == 1):
				self.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.setSelected(true)
			else:
				self.scan_main_panel.use_bpm_iter_or_amp_phase_scan_button.setSelected(false)
		if(params_da.hasAttribute("use_amp_phase_scan")):
				use_it = int(params_da.intValue("use_amp_phase_scan"))
				if(use_it == 1):
					self.scan_main_panel.use_amp_phase_scan_button.setSelected(true)
					self.scan_main_panel.use_bpm_iter_button.setSelected(false)
				else:
					self.scan_main_panel.use_amp_phase_scan_button.setSelected(false)
					self.scan_main_panel.use_bpm_iter_button.setSelected(true)
				self.scan_main_panel.bpm_iter_avg_text.setValue(params_da.doubleValue("iter_n_avg"))
				self.scan_main_panel.bpm_iter_steps_text.setValue(params_da.doubleValue("iter_n_steps"))	
		#-------------------------------------------------
		self.readAbstractCntrlFromXML(cav_cntrl_data_da)
		#-------------------------------------------------		
		analysis_params_da =  cav_cntrl_data_da.childAdaptor("ANALYSIS_PARAMS")
		self.local_bpms_table_panel.sine_min_acc_phase_text.setValue(analysis_params_da.doubleValue("sine_min_acc_phase_text"))
		self.local_bpms_table_panel.sine_max_acc_phase_text.setValue(analysis_params_da.doubleValue("sine_max_acc_phase_text"))
		self.local_bpms_table_panel.zero_acc_phase_text.setValue(analysis_params_da.doubleValue("zero_acc_phase_text"))
		self.local_bpms_table_panel.zero_acc_phase_err_text.setValue(analysis_params_da.doubleValue("zero_acc_phase_err_text"))
		self.local_bpms_table_panel.cav_energy_gain_text.setValue(analysis_params_da.doubleValue("cav_energy_gain_text"))
		#-------------------------------------------------
		cav_wrapper_da = cav_cntrl_data_da.childAdaptor("CAV_WRAPPER_PARAMS")
		self.cav_wrapper.readDataFromXML(cav_wrapper_da)
		cav_bpms_cntrl_da = cav_cntrl_data_da.childAdaptor("cavity_bpms_controller")
		self.cav_bpms_controller.readDataFromXML(cav_bpms_cntrl_da)
		self.graphs_panel.updateGraphs()	
		self.graphs_panel.setFoundCavityPhase(self.local_bpms_table_panel.zero_acc_phase_text.getValue())

