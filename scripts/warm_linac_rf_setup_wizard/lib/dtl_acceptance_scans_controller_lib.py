# The controller and auxilary classes for the acceptance scan loop over DTL 
# cavities

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from java.util import ArrayList
from javax.swing.table import AbstractTableModel, TableModel
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat

from xal.ca import ChannelFactory

from xal.smf.data import XMLDataManager
from xal.smf import AcceleratorSeqCombo

from xal.smf.impl import Marker, Quadrupole, RfGap, BPM
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat


from abstract_cavity_controller_lib import Scan_Progress_Bar
from dtl_acceptance_scans_patterns_lib import DTL_Acc_Scan_Patterns_Controller
from dtl_acceptance_scans_data_lib import DTL_Acc_Scan_Data

from functions_and_classes_lib import calculateAvgErr, makePhaseNear 

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#   Auxiliary Classes and Functions
#------------------------------------------------------------------------

class Loop_Run_State:
	""" Describes the acceptance scans loop state """
	def __init__(self):
		self.isRunning  = false
		self.shouldStop = false
	
class Acc_Scans_Loop_Runner(Runnable):
	def __init__(self,dtl_acceptance_scans_controller, cav_selected_inds):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.cav_selected_inds = cav_selected_inds[:]
		self.cav_active_ind = -1

	def run(self):
		self.dtl_acceptance_scans_controller.loop_run_state.isRunning = true
		self.dtl_acceptance_scans_controller.loop_run_state.shouldStop = false
		self.cav_active_ind = -1
		self.runMainLoop()
		self.dtl_acceptance_scans_controller.loop_run_state.isRunning = false
		self.dtl_acceptance_scans_controller.loop_run_state.shouldStop = false		
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		if(self.cav_active_ind >= 0):
			self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(self.cav_active_ind,self.cav_active_ind)

	def runMainLoop(self):
		res = self.dtl_acceptance_scans_controller.connectAllPVs()
		if(not res):
			return
		status_text = self.dtl_acceptance_scans_controller.start_stop_panel.status_text
		status_text.setText("running")
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.cav_selected_inds
		cav_wrapper = null
		start_ind = cav_selected_inds[0]
		last_ind = cav_selected_inds[len(cav_selected_inds)-1]
		#----- remove all FCs before the start cavity
		try:
			for cav_ind in range(0,start_ind):
				dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[cav_ind]
				dtl_acc_scan_cavity_controller.moveActuator(0)
		except:
			messageTextField.setText("Cannot move Faraday Cups out!")	
			status_text.setText("Not running.")
			return
		time.sleep(1.5)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()		
		#------------------------------------------
		# check if all FCs out ??????
		#------------------------------------------
		self.dtl_acceptance_scans_controller.acc_scans_loop_timer.init(start_ind,last_ind)
		self.dtl_acceptance_scans_controller.acc_scans_loop_timer.startMonitor()
		start_cav_name = self.dtl_acceptance_scans_controller.cav_wrappers[start_ind].alias
		stop_cav_name = self.dtl_acceptance_scans_controller.cav_wrappers[last_ind].alias
		txt_status = "From "+ start_cav_name+" to "+stop_cav_name+". Cavity= "
		self.cav_active_ind = -1
		force_stop = false
		for cav_ind in range(start_ind,last_ind+1):
			self.cav_active_ind = cav_ind
			if(self.dtl_acceptance_scans_controller.loop_run_state.shouldStop):
				force_stop = true
				break
			cav_wrapper = self.dtl_acceptance_scans_controller.cav_wrappers[cav_ind]
			dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[cav_ind]
			dtl_acc_scan_cavity_controller.moveActuator(1)
			time.sleep(1.5)
			#------------------------------------------
			# check if this FC is in	??????
			#------------------------------------------
			status_text.setText(txt_status+cav_wrapper.alias+" setup is running!")
			self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)
			dtl_acc_scan_cavity_controller.initProgressBar()
			#------ perform acceptance scan of the caity and findings of the cavity's amp. and phase
			#---- Phase Scans Start =========================================
			(res,txt) = dtl_acc_scan_cavity_controller.runSetUpAlgorithm()
			#---- Phase Scans Stop  =========================================
			if(res and (not self.dtl_acceptance_scans_controller.start_stop_panel.keepPhases_RadioButton.isSelected())):
				cav_wrapper.setLivePhase(cav_wrapper.newPhase)
			else:
				if(dtl_acc_scan_cavity_controller.isInitialized()):
					cav_wrapper.setLivePhase(cav_wrapper.initPhase)
			if(res and (not self.dtl_acceptance_scans_controller.start_stop_panel.keepAmps_RadioButton.isSelected())):
				cav_wrapper.setLiveAmp(cav_wrapper.newAmp)
			else:
				if(dtl_acc_scan_cavity_controller.isInitialized()):
					cav_wrapper.setLiveAmp(cav_wrapper.initAmp)
			#--------------------------------------------------------	
			dtl_acc_scan_cavity_controller.dtl_scan_data.cav_prod_phase_text.setValue(cav_wrapper.initPhase)
			dtl_acc_scan_cavity_controller.dtl_scan_data.cav_amp_text.setValue(cav_wrapper.initAmp)
			dtl_acc_scan_cavity_controller.initProgressBar()
			if(not res):
				messageTextField.setText(txt)
				status_text.setText("Not running.")
				return
			if(self.dtl_acceptance_scans_controller.loop_run_state.shouldStop):
				force_stop = true
				break
			self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
			dtl_acc_scan_cavity_controller.moveActuator(1)
			time.sleep(1.5)
			#---- check if this FC is out ??????
			#----- end of setup process for a particular cavity with cav_active_ind
		cav_wrapper = self.dtl_acceptance_scans_controller.cav_wrappers[self.cav_active_ind]
		if(force_stop):
			messageTextField.setText("The setup stopped by user's request! Cavity="+cav_wrapper.alias)
		else:
			messageTextField.setText("The setup finished at cavity="+cav_wrapper.alias+"!")
		status_text.setText("Not running.")
		return		
		
class Acc_Scans_Loop_Timer_Runner(Runnable):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.time_step = 1.0

	def run(self):
		while(1 < 2):
			time.sleep(self.time_step)
			if(not self.dtl_acceptance_scans_controller.loop_run_state.isRunning):
				self.dtl_acceptance_scans_controller.acc_scans_loop_timer.time_estimate_text.setText("")
				return
			self.dtl_acceptance_scans_controller.acc_scans_loop_timer.updateProgress()

class Acc_Scans_Loop_Timer:
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller 
		self.time_estimate_text = 	JTextField(30)
		self.time_estimate_label = JLabel("Time:",JLabel.RIGHT)
		self.total_time = 0.
		self.run_time = 0.
		self.start_time = 0.
		
	def init(self,start_ind,last_ind):
		self.total_time = 0.
		for cav_ind in range(start_ind,last_ind+1):
			cav_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[cav_ind]
			self.total_time += cav_controller.getTimeCount()
		#---- add (last_ind - start_ind)+1 of 3 sec
		self.total_time += 3.0*(last_ind - start_ind + 1)
		self.start_time = time.time()
		self.time_estimate_text.setText("")
	
	def updateProgress(self):
		self.run_time = time.time() - self.start_time
		txt = ""
		if(self.run_time < self.total_time):
			txt = self.timeToString(self.run_time)
			txt += " =out of= "+self.timeToString(self.total_time)
		else:
			txt = "Overtime:"+self.timeToString(self.run_time-self.total_time)
			txt += " =out of= "+self.timeToString(self.total_time)
		self.time_estimate_text.setText(txt)
		
	def timeToString(self,tm):
		time_sec = int(tm % 60)
		time_min = int(tm/60.)
		return " %3d min  %2d sec "%(time_min,time_sec)		
		
	def startMonitor(self):
		runner = Acc_Scans_Loop_Timer_Runner(self.dtl_acceptance_scans_controller)
		thr = Thread(runner)
		thr.start()

#------------------------------------------------------------------------
#           DTL Acceptance Scan Cavity Controller
#------------------------------------------------------------------------
class DTL_Acc_Scan_Cavity_Controller:
	def __init__(self,dtl_acceptance_scans_controller,cav_wrapper,fc_name):
		#---- dtl_acceptance_scans_controller - main controller for acceptance scans
		#---- it keeps referencies to controllers for each cavity		
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.is_initialized = false
		self.cav_wrapper = cav_wrapper
		self.main_panel = JPanel(BorderLayout())
		#------------------------------------------------
		self.scan_progress_bar = Scan_Progress_Bar(self.dtl_acceptance_scans_controller,self)		
		#-----------------------------------------
		self.cav_scan_cotnroller_main_panel = Cavity_Scan_Controller_Main_Panel(self.dtl_acceptance_scans_controller,self)
		self.main_panel.add(self.cav_scan_cotnroller_main_panel,BorderLayout.CENTER)
		#-----------------------------------------
		self.fc_name = fc_name	
		self.fc_actuator_pv = null
		self.fc_actuator_in_pv = null
		self.fc_actuator_out_pv = null
		self.fc_signal_pv = null
		#------------------------------------------------
		self.dtl_acc_scan_patterns_controller = DTL_Acc_Scan_Patterns_Controller(self)
		#------------------------------------------------
		self.dtl_scan_data = DTL_Acc_Scan_Data(self)
		self.cav_scan_cotnroller_main_panel.scan_data_graph.gp_acc_scan.addGraphData(self.dtl_scan_data.getScanGraphData())
		self.cav_scan_cotnroller_main_panel.scan_data_graph.gp_acc_scan.addGraphData(self.dtl_scan_data.getHalfHeightGraphData())

	def getNamePrefix(self):
		prefix = "DTL_Diag:"
		if(self.fc_name == "FC104"): prefix = "CCL_Diag:"
		return prefix

	def isInitialized(self):
		return self.is_initialized 
		
	def getFC_Name(self):
		return self.fc_name
		
	def connectPVs(self):
		prefix = self.getNamePrefix()	
		if(self.fc_actuator_pv == null):
			self.fc_actuator_pv = ChannelFactory.defaultFactory().getChannel(prefix+self.fc_name+":Actuator")
		if(self.fc_actuator_in_pv == null):
			self.fc_actuator_in_pv = ChannelFactory.defaultFactory().getChannel(prefix+self.fc_name+":Actuator_In")
		if(self.fc_actuator_out_pv == null):
			self.fc_actuator_out_pv = ChannelFactory.defaultFactory().getChannel(prefix+self.fc_name+":Actuator_Out")
		if(self.fc_signal_pv == null):
			self.fc_signal_pv = ChannelFactory.defaultFactory().getChannel(prefix+self.fc_name+":Fast:QGt")
		#----------------------------------------------------------
		if(not self.fc_actuator_pv.connectAndWait(0.5)):
			print "debug bad pv=",self.fc_actuator_pv.channelName()
			return false
		if(not self.fc_signal_pv.connectAndWait(0.5)):
			print "debug bad pv=",self.fc_signal_pv.channelName()
			return false
		if(not self.fc_actuator_in_pv.connectAndWait(0.5)):
			print "debug bad pv=",self.fc_actuator_in_pv.channelName()
			return false
		if(not self.fc_actuator_out_pv.connectAndWait(0.5)):
			print "debug bad pv=",self.fc_actuator_out_pv.channelName()
			return false
		#print "debug fc connections are fine! for ", self.fc_name 
		return true
	
	def init(self):
		if(not self.connectPVs()):
			prefix = self.getNamePrefix()			
			messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
			messageTextField.setText("Cannot Initialize. Cannot connect FC PVs for FC="+prefix+self.fc_name)
			return false
		self.cav_wrapper.connectPVs()
		self.cav_wrapper.init()
		self.dtl_scan_data.clearData()
		self.is_initialized = true
		return true
		
	def initProgressBar(self):
		self.scan_progress_bar.init()
		
	def getMainPanel(self):
		return self.main_panel
		
	def getPatternPanel(self):
		return self.dtl_acc_scan_patterns_controller.getMainPanel()
		
	def moveActuator(self, pos = +1):
		if(self. fc_actuator_pv == null):
			prefix = "DTL_Diag:"
			if(self.fc_name == "FC104"): prefix = "CCL_Diag:"			
			messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
			messageTextField.setText("Cannot move FC. Cannot connect FC PVs for FC="+prefix+self.fc_name)			
			return false
		self.fc_actuator_pv.putVal(1.0*pos)
		return true
		
	def isActuatorIn(self):
		if(self.fc_actuator_in_pv == null): return false
		if(self.fc_actuator_in_pv.getValInt() == 1): return true
		return false
		
	def isActuatorOut(self):
		if(self.fc_actuator_in_pv == null): return false
		if(self.fc_actuator_out_pv.getValInt() == 1): return true
		return false

	def getFC_Charge(self):
		if(self.fc_signal_pv == null): return 0.
		return self.fc_signal_pv.getValDbl()
		
	def getCavPhase(self):
		return self.cav_wrapper.getLivePhase()
		
	def getCavAmp(self):
		return self.cav_wrapper.getLiveAmp()
		
	def getTimeCount(self):
		phase_min = self.cav_scan_cotnroller_main_panel.phase_min_text.getValue()
		phase_max = self.cav_scan_cotnroller_main_panel.phase_max_text.getValue()
		phase_step = self.cav_scan_cotnroller_main_panel.phase_step_text.getValue()
		time_step = self.cav_scan_cotnroller_main_panel.phase_scan_time_step_text.getValue()
		total_time = time_step*(1.0+(phase_max - phase_min)/phase_step)
		return total_time
		
	def getPhasePointsArr(self):
		phase_min = self.cav_scan_cotnroller_main_panel.phase_min_text.getValue()
		phase_max = self.cav_scan_cotnroller_main_panel.phase_max_text.getValue()
		phase_step = self.cav_scan_cotnroller_main_panel.phase_step_text.getValue()
		n_points = int(1.0+(phase_max - phase_min)/phase_step)
		phase_arr = []
		for ind in range(n_points):
			phase = phase_min + ind*phase_step
			if(phase > phase_max): break
			phase_arr.append(phase)
		return phase_arr

	def runSetUpAlgorithm(self):
		if(not self.isInitialized()):
			return (false,"Cavity " + self.cav_wrapper.alias + " is not initialized! Stop scan.")
		cav_amp = self.cav_wrapper.getLiveAmp()
		self.cav_scan_cotnroller_main_panel.cav_ampl_live_text.setValue(cav_amp)
		self.dtl_scan_data.clearData()
		sleep_time = self.cav_scan_cotnroller_main_panel.phase_scan_time_step_text.getValue()
		phase_arr = self.getPhasePointsArr()
		n_points = len(phase_arr)
		self.scan_progress_bar.setMaxTimeCount(1.0*n_points)
		#print "debug === setup cav=",self.cav_wrapper.alias
		for phase_ind in range(n_points):
			phase = phase_arr[phase_ind]
			if(self.dtl_acceptance_scans_controller.loop_run_state.shouldStop):
				return (true,"")
			self.cav_wrapper.setLivePhase(phase)
			time.sleep(sleep_time)
			self.cav_scan_cotnroller_main_panel.cav_phase_live_text.setValue(self.cav_wrapper.getLivePhase())
			fc_qt = self.getFC_Charge()
			self.dtl_scan_data.addScanPoint(phase,fc_qt)
			if(self.dtl_acceptance_scans_controller.loop_run_state.shouldStop):
				return (true,"")
			#print "debug cav. phase=",phase," ind=",phase_ind," n_points=",n_points
			self.scan_progress_bar.countEvent(1.0)
			self.scan_progress_bar.update()
		self.scan_progress_bar.init()
		self.cav_scan_cotnroller_main_panel.cav_ampl_live_text.setValue(0.)
		self.cav_scan_cotnroller_main_panel.cav_phase_live_text.setValue(0.)
		#---------------------------------------------------------------------
		if(not self.dtl_scan_data.performAnalysis()):
			return (false,"Data for cavity " + self.cav_wrapper.alias + " cannot be analyzed! Stop scan.")
		#---- set cavity phase : here we will compare our scan data with patterns
		width = self.dtl_scan_data.getFitWidth()
		left_slope_phase = self.dtl_scan_data.getLeftHalfHeightPosition()
		self.cav_scan_cotnroller_main_panel.acc_scan_width_text.setValue(width)		
		(res,cav_phase_new,cav_amp_new,txt) = self.dtl_acc_scan_patterns_controller.getNewPhaseAmpAndPattern(width,left_slope_phase,cav_amp)
		cav_phase_new = makePhaseNear(cav_phase_new,0.)
		if(not res):
			return (false,txt)
		self.cav_scan_cotnroller_main_panel.new_cav_phase_text.setValue(cav_phase_new)
		self.cav_scan_cotnroller_main_panel.new_cav_amp_text.setValue(cav_amp_new)
		self.cav_wrapper.newPhase = cav_phase_new
		self.cav_wrapper.newAmp = cav_amp_new
		return (true,"")

	def writeDataToXML(self,root_da):
		scan_cav_cntrl_da = root_da.createChild("dtl_acc_scan_cavity_cntroller")
		self.dtl_acc_scan_patterns_controller.writeDataToXML(scan_cav_cntrl_da)
		self.dtl_scan_data.writeDataToXML(scan_cav_cntrl_da)
		self.cav_scan_cotnroller_main_panel.writeDataToXML(scan_cav_cntrl_da)

	def readDataFromXML(self,scan_cav_cntrl_da):
		#print "debug scan_cav_cntrl_da=",scan_cav_cntrl_da
		scan_patterns_da = scan_cav_cntrl_da.childAdaptor("dtl_acc_scan_patterns")
		self.dtl_acc_scan_patterns_controller.readDataFromXML(scan_patterns_da)
		scan_data_da = scan_cav_cntrl_da.childAdaptor("dtl_acceptance_scan_data")
		#print "debug scan_data_da=",scan_data_da
		self.dtl_scan_data.readDataFromXML(scan_data_da)
		cav_scan_main_panel_da = scan_cav_cntrl_da.childAdaptor("cav_main_panel")
		self.cav_scan_cotnroller_main_panel.readDataFromXML(cav_scan_main_panel_da)
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------

class Cavity_Scan_Controller_Main_Panel(JPanel):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller		
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		self.setLayout(BorderLayout())
		self.setBorder(BorderFactory.createEtchedBorder())
		etched_border = BorderFactory.createEtchedBorder()
		#----------------------------------------------------
		scan_params_panel_2 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		scan_params_panel_2.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavity Phase Scan Parameters"))
		scan_params_panel_3 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		scan_params_panel_3.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavity Live Parameters"))
		#----------------------------------------------
		phase_step_label = JLabel("Step[deg]=",JLabel.RIGHT)
		phase_min_label = JLabel(" Min[deg]=",JLabel.RIGHT)
		phase_max_label = JLabel(" Max[deg]=",JLabel.RIGHT)
		phase_scan_time_step_label = JLabel(" Time/Step[sec]=",JLabel.RIGHT)
		self.phase_step_text = DoubleInputTextField(3.,DecimalFormat("##.##"),5)
		self.phase_min_text = DoubleInputTextField(-180.,DecimalFormat("####.#"),6)
		self.phase_max_text = DoubleInputTextField(180.,DecimalFormat("####.#"),6)
		self.phase_scan_time_step_text = DoubleInputTextField(1.5,DecimalFormat("##.#"),4)
		scan_params_panel_2.add(phase_step_label)
		scan_params_panel_2.add(self.phase_step_text)
		scan_params_panel_2.add(phase_min_label)
		scan_params_panel_2.add(self.phase_min_text)
		scan_params_panel_2.add(phase_max_label)
		scan_params_panel_2.add(self.phase_max_text)
		scan_params_panel_2.add(phase_scan_time_step_label)
		scan_params_panel_2.add(self.phase_scan_time_step_text)
		#----------------------------------------------
		cav_ampl_live_label = JLabel("Cavity Ampl.=",JLabel.RIGHT)
		cav_phase_live_label = JLabel(" Phase[deg]=",JLabel.RIGHT)
		self.cav_ampl_live_text = DoubleInputTextField(0.,DecimalFormat("#.###"),5)
		self.cav_phase_live_text = DoubleInputTextField(0.,DecimalFormat("####.##"),7)
		read_live_phase_amp_button = JButton("Read EPICS Cavity Parameters")
		read_live_phase_amp_button.addActionListener(Read_Live_Params_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))		
		scan_params_panel_3.add(cav_ampl_live_label)
		scan_params_panel_3.add(self.cav_ampl_live_text)
		scan_params_panel_3.add(cav_phase_live_label)
		scan_params_panel_3.add(self.cav_phase_live_text)
		scan_params_panel_3.add(read_live_phase_amp_button)
		#-----------------------------------------------
		cntrl_upper_panel = JPanel(GridLayout(2,1,1,1))	
		cntrl_upper_panel.add(scan_params_panel_2)
		cntrl_upper_panel.add(scan_params_panel_3)
		#----------------------------------------------
		scan_res_panel = JPanel(BorderLayout())
		#----------------------------------------------
		scan_progress_bar = self.dtl_acc_scan_cavity_controller.scan_progress_bar
		scan_progress_bar_panel = scan_progress_bar.scan_progress_panel
		left_scan_res_panel_0 = JPanel(BorderLayout())
		left_scan_res_panel_0.add(scan_progress_bar_panel,BorderLayout.CENTER)
		#---------------------------------------------
		left_scan_res_panel_1_1 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		re_analyze_scan_button = JButton("Re-Analyze Scan")
		re_analyze_scan_button.addActionListener(Re_Analyze_Scan_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))
		find_amp_phase_button = JButton("Find New A&Phase")
		find_amp_phase_button.addActionListener(Find_New_Amp_Phase_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))
		left_scan_res_panel_1_1.add(re_analyze_scan_button)
		left_scan_res_panel_1_1.add(find_amp_phase_button)
		#---------------------------------------------
		left_scan_res_panel_1_2 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		set_new_phase_button = JButton("New Phase to EPICS")
		set_new_phase_button.addActionListener(Set_New_Phase_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))	
		set_new_amp_button = JButton("New Ampl. to EPICS")
		set_new_amp_button.addActionListener(Set_New_Amp_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))	
		left_scan_res_panel_1_2.add(set_new_phase_button)
		left_scan_res_panel_1_2.add(set_new_amp_button)
		#----------------------------------------------
		left_scan_res_panel_2 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))	
		acc_scan_width_label = JLabel("Acc. Scan Width[deg]=",JLabel.RIGHT)
		self.acc_scan_width_text = DoubleInputTextField(0.,DecimalFormat("####.##"),7)
		left_scan_res_panel_2.add(acc_scan_width_label)
		left_scan_res_panel_2.add(self.acc_scan_width_text)
		#----------------------------------------------
		left_scan_res_panel_3 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))	
		new_cav_phase_label = JLabel("New Phase[deg]       =",JLabel.RIGHT)
		self.new_cav_phase_text = DoubleInputTextField(0.,DecimalFormat("####.##"),7)
		left_scan_res_panel_3.add(new_cav_phase_label)
		left_scan_res_panel_3.add(self.new_cav_phase_text)
		#----------------------------------------------		
		left_scan_res_panel_4 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))	
		new_cav_amp_label = JLabel("New Cav. Amplitude  =",JLabel.RIGHT)
		self.new_cav_amp_text = DoubleInputTextField(0.,DecimalFormat("#.####"),7)
		left_scan_res_panel_4.add(new_cav_amp_label)
		left_scan_res_panel_4.add(self.new_cav_amp_text)
		#----------------------------------------------
		scan_res_grid_panel = JPanel(GridLayout(6,1,1,1))
		scan_res_grid_panel.add(left_scan_res_panel_0)
		scan_res_grid_panel.add(left_scan_res_panel_1_1)
		scan_res_grid_panel.add(left_scan_res_panel_1_2)
		scan_res_grid_panel.add(left_scan_res_panel_2)
		scan_res_grid_panel.add(left_scan_res_panel_3)
		scan_res_grid_panel.add(left_scan_res_panel_4)
		#----------------------------------------------
		scan_to_pattern_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		scan_to_pattern_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"Scan to Patterns Copy: for Experts"))		
		shift_scan_button = JButton("Shift Scan by +10 deg")
		shift_scan_button.addActionListener(Shift_Scan_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))
		copy_to_pattern_button = JButton("Copy to Patterns")
		copy_to_pattern_button.addActionListener(Copy_To_Pattern_Button_Listener(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller))	
		scan_to_pattern_panel.add(shift_scan_button)
		scan_to_pattern_panel.add(copy_to_pattern_button)		
		#----------------------------------------------
		scan_res_panel_tmp0 = JPanel(BorderLayout())
		scan_res_panel_tmp0.add(scan_res_grid_panel,BorderLayout.CENTER)
		scan_res_panel_tmp0.add(scan_to_pattern_panel,BorderLayout.SOUTH)
		scan_res_panel.add(scan_res_panel_tmp0,BorderLayout.NORTH)
		#----------------------------------------------
		self.scan_data_graph = Acceptance_Graphs_Panel_Holder(self.dtl_acceptance_scans_controller,self.dtl_acc_scan_cavity_controller)
		self.add(cntrl_upper_panel,BorderLayout.NORTH)	
		self.add(self.scan_data_graph.getGraphsPanel(),BorderLayout.CENTER)
		self.add(scan_res_panel,BorderLayout.WEST)
		
	def writeDataToXML(self,root_da):
		cav_scan_main_panel_da = root_da.createChild("cav_main_panel")
		scan_params_da = cav_scan_main_panel_da.createChild("scan_params")
		scan_params_da.setValue("phase_step",self.phase_step_text.getValue())
		scan_params_da.setValue("phase_min",self.phase_min_text.getValue())
		scan_params_da.setValue("phase_max",self.phase_max_text.getValue())
		scan_params_da.setValue("time_step",self.phase_scan_time_step_text.getValue())
		scan_params_da.setValue("width",self.acc_scan_width_text.getValue())
		scan_params_da.setValue("new_cav_phase",self.new_cav_phase_text.getValue())
		scan_params_da.setValue("new_cav_amp",self.new_cav_amp_text.getValue())
		
	def readDataFromXML(self,cav_scan_main_panel_da):
		scan_params_da = cav_scan_main_panel_da.childAdaptor("scan_params")
		self.phase_step_text.setValue(scan_params_da.doubleValue("phase_step"))
		self.phase_min_text.setValue(scan_params_da.doubleValue("phase_min"))
		self.phase_max_text.setValue(scan_params_da.doubleValue("phase_max"))
		self.phase_scan_time_step_text.setValue(scan_params_da.doubleValue("time_step"))
		self.acc_scan_width_text.setValue(scan_params_da.doubleValue("width"))
		self.new_cav_phase_text.setValue(scan_params_da.doubleValue("new_cav_phase"))
		self.new_cav_amp_text.setValue(scan_params_da.doubleValue("new_cav_amp"))

class Acceptance_Graphs_Panel_Holder():
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		#----------------------------------------
		etched_border = BorderFactory.createEtchedBorder()
		self.gp_acc_scan = FunctionGraphsJPanel()
		#self.gp_scan_and_pattern = FunctionGraphsJPanel()
		#------------------------------------------
		self.gp_acc_scan.setLegendButtonVisible(true)
		#self.gp_scan_and_pattern.setLegendButtonVisible(true)
		#------------------------------------------	
		self.gp_acc_scan.setChooseModeButtonVisible(true)
		#self.gp_scan_and_pattern.setChooseModeButtonVisible(true)
		#------------------------------------------
		self.gp_acc_scan.setName("Acceptance Scan: FC vs. RF Phase")
		#self.gp_scan_and_pattern.setName("Scan and Pattern Comparison")
		self.gp_acc_scan.setAxisNames("Cav Phase, [deg]","FC Q, [a.u.]")
		#self.gp_scan_and_pattern.setAxisNames("RF Phase, [deg]","FC Charge, [arb. units]")
		self.gp_acc_scan.setBorder(etched_border)
		#self.gp_scan_and_pattern.setBorder(etched_border)
		#---------------------------------------------
		remove_one_point_button = JButton(" Remove One Cav. Phase Point ")
		remove_one_point_button.addActionListener(Remove_One_Point_Button_Listener(self.dtl_acc_scan_cavity_controller))
		scan_panel_tmp0 = JPanel(FlowLayout(FlowLayout.CENTER,3,1))
		scan_panel_tmp0.add(remove_one_point_button)
		scan_panel_tmp1 = JPanel(BorderLayout())
		scan_panel_tmp1.add(self.gp_acc_scan,BorderLayout.CENTER)
		scan_panel_tmp1.add(scan_panel_tmp0,BorderLayout.SOUTH)
		#---------------------------------------------
		self.graphs_panel = JTabbedPane()
		self.graphs_panel.add("Acceptance Scan Data",scan_panel_tmp1)
		#self.graphs_panel.add("Scan & Pattern",self.gp_scan_and_pattern)
		
	def getGraphsPanel(self):
		return self.graphs_panel
		
	def refreshGraphs(self):
		self.gp_acc_scan.refreshGraphJPanel()
		#self.gp_scan_and_pattern.refreshGraphJPanel()

class Start_Stop_Panel(JPanel):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.setLayout(GridLayout(3,1,1,1))
		self.setBorder(BorderFactory.createEtchedBorder())
		#-----------------------------------------------------------
		start_selected_button = JButton("Start Scan for Selected Cavs")
		start_selected_button.addActionListener(Start_Scan_Selected_Cavs_Button_Listener(self.dtl_acceptance_scans_controller))		
		stop_button = JButton("Stop")
		stop_button.addActionListener(Stop_Scan_Button_Listener(self.dtl_acceptance_scans_controller))
		send_phase_to_EPICS_button = JButton("Send New Phase to EPICS for Selected Cavs")
		send_phase_to_EPICS_button.addActionListener(Send_Phase_to_EPICS_Button_Listener(self.dtl_acceptance_scans_controller))		
		send_amp_phase_to_EPICS_button = JButton("Send New Amplitude to EPICS for Selected Cavs")
		send_amp_phase_to_EPICS_button.addActionListener(Send_Amp_to_EPICS_Button_Listener(self.dtl_acceptance_scans_controller))
		restore_phase_to_EPICS_button = JButton("Restore Init Phase to EPICS for Selected Cavs")
		restore_phase_to_EPICS_button.addActionListener(Restore_Phase_of_Selected_Cavs_to_EPICS_Button_Listener(self.dtl_acceptance_scans_controller))
		restore_amp_to_EPICS_button = JButton("Restore Init Amplitude to EPICS for Selected Cavs")
		restore_amp_to_EPICS_button.addActionListener(Restore_Amp_of_Selected_Cavs_to_EPICS_Button_Listener(self.dtl_acceptance_scans_controller))		
		self.keepPhases_RadioButton = JRadioButton("Keep Cavities Phases")
		self.keepAmps_RadioButton = JRadioButton("Keep Cavities Amplitudes")
		#-----------------------------------------------------------
		self.status_text = JTextField(30)
		self.status_text.setForeground(Color.red)
		self.status_text.setText("Not running.")
		status_text_label = JLabel("Loop status:",JLabel.RIGHT)
		status_panel_tmp0 = JPanel(GridLayout(2,1,1,1))
		status_panel_tmp0.add(status_text_label)
		status_panel_tmp0.add(self.dtl_acceptance_scans_controller.acc_scans_loop_timer.time_estimate_label)
		status_panel_tmp1 = JPanel(GridLayout(2,1,1,1))
		status_panel_tmp1.add(self.status_text)
		status_panel_tmp1.add(self.dtl_acceptance_scans_controller.acc_scans_loop_timer.time_estimate_text)
		status_panel = JPanel(BorderLayout())
		status_panel.add(status_panel_tmp0,BorderLayout.WEST)
		status_panel.add(status_panel_tmp1,BorderLayout.CENTER)
		status_panel.setBorder(BorderFactory.createEtchedBorder())
		#------------------------------------------------
		buttons_panel0 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		buttons_panel0.add(start_selected_button)
		buttons_panel0.add(stop_button)
		buttons_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		buttons_panel1.add(self.keepPhases_RadioButton)
		buttons_panel1.add(self.keepAmps_RadioButton)
		buttons_panel = JPanel(GridLayout(2,1,1,1))
		buttons_panel.add(buttons_panel0)
		buttons_panel.add(buttons_panel1)
		#---------------------------------------
		bottom_buttons_panel0 = JPanel(BorderLayout())
		bottom_buttons_panel0.add(send_phase_to_EPICS_button,BorderLayout.CENTER)		
		bottom_buttons_panel1 = JPanel(BorderLayout())
		bottom_buttons_panel1.add(send_amp_phase_to_EPICS_button,BorderLayout.CENTER)
		bottom_buttons_panel2 = JPanel(BorderLayout())
		bottom_buttons_panel2.add(restore_phase_to_EPICS_button,BorderLayout.CENTER)
		bottom_buttons_panel3 = JPanel(BorderLayout())
		bottom_buttons_panel3.add(restore_amp_to_EPICS_button,BorderLayout.CENTER)		
		bottom_buttons_panel_tmp0 = JPanel(GridLayout(4,1,1,1))
		bottom_buttons_panel_tmp0.add(bottom_buttons_panel0)
		bottom_buttons_panel_tmp0.add(bottom_buttons_panel1)
		bottom_buttons_panel_tmp0.add(bottom_buttons_panel2)
		bottom_buttons_panel_tmp0.add(bottom_buttons_panel3)
		bottom_buttons_panel = JPanel(BorderLayout())
		bottom_buttons_panel.add(bottom_buttons_panel_tmp0,BorderLayout.WEST)
		#---------------------------------------
		self.add(buttons_panel)	
		self.add(status_panel)
		self.add(bottom_buttons_panel)

#------------------------------------------------
#  JTable models
#------------------------------------------------

class Cavities_Table_Model(AbstractTableModel):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.columnNames = ["Cavity",]
		self.columnNames += ["<html>&phi;<SUB>design</SUB>[deg]<html>",]
		self.columnNames += ["<html>A<SUB>init</SUB>[a.u.]<html>",]
		self.columnNames += ["<html>&phi;<SUB>init</SUB>[deg]<html>",]
		self.columnNames += ["<html>A<SUB>new</SUB>[a.u.]<html>",]
		self.columnNames += ["<html>&phi;<SUB>new</SUB>[deg]<html>",]
		self.columnNames += ["<html>FC<SUB>in</SUB><html>",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		#---- it will be DTL 1-6
		return len(self.dtl_acceptance_scans_controller.cav_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.dtl_acceptance_scans_controller.cav_wrappers[row]
		cav_acc_scan_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[row]
		if(col == 0): return cav_wrapper.alias
		if(col == 1): return "%5.1f"%cav_wrapper.design_phase
		if(col == 2): return "%5.3f"%cav_wrapper.initAmp
		if(col == 3): return "%5.1f"%cav_wrapper.initPhase
		if(col == 4): return "%5.3f"%cav_wrapper.newAmp	
		if(col == 5): return "%5.1f"%cav_wrapper.newPhase
		if(col == 6): return cav_acc_scan_controller.isActuatorIn()
		return ""
				
	def getColumnClass(self,col):
		if(col == 6): return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.dtl_acceptance_scans_controller.cav_wrappers[row]


#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		cav_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[index]
		tabbedPane = self.dtl_acceptance_scans_controller.tabbedPane		
		tabbedPane.setComponentAt(0,cav_controller.getMainPanel())
		tabbedPane.setComponentAt(1,cav_controller.getPatternPanel())
		tabbedPane.setSelectedIndex(0)
		tabbedPane.setTitleAt(0,cav_controller.cav_wrapper.alias)
		tabbedPane.setTitleAt(1,"Acceptance Scan Pattern")

class Init_Selected_Cavs_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to initialize!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		self.dtl_acceptance_scans_controller.initAllCavControllers(ind_start,ind_stop)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)
		
class FC_In_Selected_Cavs_Button_Listener	(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to put FC in!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			cav_acc_scan_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			if(not cav_acc_scan_controller.moveActuator(+1)): break
		time.sleep(1.5)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)	
		
class FC_Out_Selected_Cavs_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to move FC out!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			cav_acc_scan_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			if(not cav_acc_scan_controller.moveActuator(0)): break
		time.sleep(1.5)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)
	
class Start_Scan_Selected_Cavs_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to scan!")	
			return
		runner = Acc_Scans_Loop_Runner(self.dtl_acceptance_scans_controller,cav_selected_inds[:])
		thr = Thread(runner)
		thr.start()
				
class Stop_Scan_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		self.dtl_acceptance_scans_controller.loop_run_state.shouldStop = true

class Send_Amp_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			cav_wrapper = dtl_acc_scan_cavity_controller.cav_wrapper
			cav_wrapper.connectPVs()
			cav_wrapper.setLiveAmp(cav_wrapper.newAmp)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)			

class Send_Phase_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			cav_wrapper = dtl_acc_scan_cavity_controller.cav_wrapper
			cav_wrapper.connectPVs()
			cav_wrapper.setLivePhase(cav_wrapper.newPhase)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)		

class Restore_Phase_of_Selected_Cavs_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			cav_wrapper = dtl_acc_scan_cavity_controller.cav_wrapper
			cav_wrapper.connectPVs()
			cav_wrapper.setLivePhase(cav_wrapper.initPhase)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)
		
class Restore_Amp_of_Selected_Cavs_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		for ind in range(ind_start,ind_stop+1):
			dtl_acc_scan_cavity_controller = self.dtl_acceptance_scans_controller.cav_acc_scan_controllers[ind]
			cav_wrapper = dtl_acc_scan_cavity_controller.cav_wrapper
			cav_wrapper.connectPVs()
			cav_wrapper.setLiveAmp(cav_wrapper.initAmp)
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)		

class Read_Live_Params_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_wrapper = self.dtl_acc_scan_cavity_controller.cav_wrapper
		cav_wrapper.connectPVs()
		phase = cav_wrapper.getLivePhase()
		amp = cav_wrapper.getLiveAmp()
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.cav_ampl_live_text.setValue(amp)
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.cav_phase_live_text.setValue(phase)

class Re_Analyze_Scan_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		self.dtl_acc_scan_cavity_controller.dtl_scan_data.performAnalysis()
		width = self.dtl_acc_scan_cavity_controller.dtl_scan_data.getFitWidth()
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.acc_scan_width_text.setValue(width)

class Find_New_Amp_Phase_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_wrapper = self.dtl_acc_scan_cavity_controller.cav_wrapper
		self.dtl_acc_scan_cavity_controller.dtl_scan_data.performAnalysis()
		width = self.dtl_acc_scan_cavity_controller.dtl_scan_data.getFitWidth()
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.acc_scan_width_text.setValue(width)
		left_slope_phase = self.dtl_acc_scan_cavity_controller.dtl_scan_data.getLeftHalfHeightPosition()
		cav_amp = cav_wrapper.initAmp
		dtl_acc_scan_patterns_controller = self.dtl_acc_scan_cavity_controller.dtl_acc_scan_patterns_controller
		(res,cav_phase_new,cav_amp_new,txt) = dtl_acc_scan_patterns_controller.getNewPhaseAmpAndPattern(width,left_slope_phase,cav_amp)
		cav_phase_new = makePhaseNear(cav_phase_new,0.)
		if(not res):
			messageTextField.setText(txt)
			return
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.new_cav_phase_text.setValue(cav_phase_new)
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.new_cav_amp_text.setValue(cav_amp_new)
		cav_wrapper.newPhase = cav_phase_new
		cav_wrapper.newAmp = cav_amp_new
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]		
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)

class Set_New_Phase_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) != 1): return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]		
		cav_wrapper = self.dtl_acc_scan_cavity_controller.cav_wrapper
		newPhase = self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.new_cav_phase_text.getValue()
		cav_wrapper.connectPVs()
		cav_wrapper.setLivePhase(newPhase)
		cav_wrapper.newPhase = newPhase
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)	
		
class Set_New_Amp_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.dtl_acceptance_scans_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) != 1): return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]		
		cav_wrapper = self.dtl_acc_scan_cavity_controller.cav_wrapper
		newAmp = self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.new_cav_amp_text.getValue()
		cav_wrapper.connectPVs()
		cav_wrapper.setLiveAmp(newAmp)
		cav_wrapper.newAmp = newAmp
		self.dtl_acceptance_scans_controller.cav_table.getModel().fireTableDataChanged()
		self.dtl_acceptance_scans_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)		
		
class Shift_Scan_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		acc_scan_data = self.dtl_acc_scan_cavity_controller.dtl_scan_data
		acc_scan_data.shiftScanData(10.)

class Copy_To_Pattern_Button_Listener(ActionListener):
	def __init__(self,dtl_acceptance_scans_controller,dtl_acc_scan_cavity_controller):
		self.dtl_acceptance_scans_controller = dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		acc_scan_data = self.dtl_acc_scan_cavity_controller.dtl_scan_data.getCopy()
		dtl_acc_scan_patterns_controller = self.dtl_acc_scan_cavity_controller.dtl_acc_scan_patterns_controller
		dtl_acc_scan_patterns_controller.addAccScanPattern(acc_scan_data)
		
class Remove_One_Point_Button_Listener(ActionListener):
	def __init__(self,dtl_acc_scan_cavity_controller):
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.dtl_acc_scan_cavity_controller.dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		gp_active = self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.scan_data_graph.gp_acc_scan
		gd = self.dtl_acc_scan_cavity_controller.dtl_scan_data.scan_gd
		minX = gp_active.getCurrentMinX()
		maxX = gp_active.getCurrentMaxX()
		nPoints = 0
		point_ind = -1
		for ind in range(gd.getNumbOfPoints()):
			x = gd.getX(ind)
			if( x > minX and x < maxX):
				nPoints += 1
				point_ind = ind
		if(nPoints != 1):
			messageTextField.setText("Use zoom to select one point on this graph!")
			return
		gd.removePoint(point_ind)
		gp_active.clearZoomStack()
		self.dtl_acc_scan_cavity_controller.cav_scan_cotnroller_main_panel.scan_data_graph.refreshGraphs()

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class DTL_Acceptance_Scans_Controller:
	def __init__(self,top_document,accl):
		#--- top_document is a parent document for all controllers
		self.top_document = top_document
		self.main_panel = JPanel(BorderLayout())
		#---- the state of the acceptance scans loop
		self.loop_run_state = Loop_Run_State()
		#----acceptance scans loop timer
		self.acc_scans_loop_timer = Acc_Scans_Loop_Timer(self)		
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---------------------------------------------
		#---- Cavities' Controllers 
		self.cav_acc_scan_controllers = []
		self.cav_wrappers = self.top_document.main_loop_controller.cav_wrappers[4:10]
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[0],"FC160"))
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[1],"FC248"))
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[2],"FC334"))
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[3],"FC428"))
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[4],"FC524"))
		self.cav_acc_scan_controllers.append(DTL_Acc_Scan_Cavity_Controller(self,self.cav_wrappers[5],"FC104"))
		#----------------------------------------------   
		self.tabbedPane = JTabbedPane()		
		self.tabbedPane.add("Cavity",JPanel(BorderLayout()))	
		self.tabbedPane.add("Pattern",JPanel(BorderLayout()))
		#--------------------------------------------------------
		self.cav_table = JTable(Cavities_Table_Model(self))
		self.cav_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.cav_table.setFillsViewportHeight(true)
		self.cav_table.setPreferredScrollableViewportSize(Dimension(500,120))
		self.cav_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self))
		scrl_cav_panel = JScrollPane(self.cav_table)
		#-------------------------------------------------------
		scrl_cav_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavities' Parameters"))
		init_buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,5,2))
		#---- initialization buttons
		init_selected_cavs_button = JButton("Init Selected Cavs")
		init_selected_cavs_button.addActionListener(Init_Selected_Cavs_Button_Listener(self))
		fc_in_selected_cavs_button = JButton("FC In for Selected Cavs")
		fc_in_selected_cavs_button.addActionListener(FC_In_Selected_Cavs_Button_Listener(self))	
		fc_out_selected_cavs_button = JButton("FC Out for Selected Cavs")
		fc_out_selected_cavs_button.addActionListener(FC_Out_Selected_Cavs_Button_Listener(self))
		init_buttons_panel.add(init_selected_cavs_button)
		init_buttons_panel.add(fc_in_selected_cavs_button)
		init_buttons_panel.add(fc_out_selected_cavs_button)
		#---- start stop buttons panel
		self.start_stop_panel = Start_Stop_Panel(self)
		#-------------------------------------------------
		tmp0_panel = JPanel(BorderLayout())
		tmp0_panel.add(init_buttons_panel,BorderLayout.NORTH)
		tmp0_panel.add(scrl_cav_panel,BorderLayout.CENTER)
		tmp0_panel.add(self.start_stop_panel,BorderLayout.SOUTH)
		tmp1_panel = JPanel(BorderLayout())
		tmp1_panel.add(tmp0_panel,BorderLayout.NORTH)
		#-------------------------------------------------
		left_panel = JPanel(BorderLayout())
		left_panel.add(tmp1_panel,BorderLayout.WEST)
		#--------------------------------------------------
		self.main_panel.add(left_panel,BorderLayout.WEST)
		self.main_panel.add(self.tabbedPane,BorderLayout.CENTER)
		#---- non GUI controllers
		
		
	def connectAllPVs(self):
		self.getMessageTextField().setText("")
		for cav_acc_scan_controller in self.cav_acc_scan_controllers:
			res = cav_acc_scan_controller.connectPVs()
			if(not res):
				self.getMessageTextField().setText("Cannot connect PVs for cavity="+cav_acc_scan_controller.cav_wrapper.alias)
				return false
		return true
		
	def initAllCavControllers(self, ind_start = -1, ind_stop = -1):
		res = self.connectAllPVs()
		if(not res):
			return false
		if(ind_start < 0):
			ind_start = 0
			ind_stop = len(self.cav_acc_scan_controllers) - 1
		for cav_acc_scan_controller in self.cav_acc_scan_controllers[ind_start:ind_stop+1]:
			res = cav_acc_scan_controller.init()
			if(not res):
				self.getMessageTextField().setText("Cannot read cavity's PVs! Cavity="+cav_acc_scan_controller.cav_wrapper.alias)
				return	false
		return true		

	def getMainPanel(self):
		return self.main_panel
		
	def getMessageTextField(self):
		return self.top_document.getMessageTextField()
		
	def writeDataToXML(self,root_da):
		dtl_scans_loop_cntrl_da = root_da.createChild("DTL_ACCPT_SCANS_CONTROLLER")
		buttons_states_da = dtl_scans_loop_cntrl_da.createChild("buttons")
		buttons_states_da.setValue("keep_phases",self.start_stop_panel.keepPhases_RadioButton.isSelected())
		buttons_states_da.setValue("keep_amps",self.start_stop_panel.keepAmps_RadioButton.isSelected())
		for cav_acc_scan_controller in self.cav_acc_scan_controllers:
			cav_acc_scan_controller_da = dtl_scans_loop_cntrl_da.createChild(cav_acc_scan_controller.cav_wrapper.alias)
			cav_acc_scan_controller.writeDataToXML(cav_acc_scan_controller_da)

	def readDataFromXML(self,root_da):		
		dtl_scans_loop_cntrl_da = root_da.childAdaptor("DTL_ACCPT_SCANS_CONTROLLER")
		if(dtl_scans_loop_cntrl_da == null): return
		#----------------------------------------------------------------
		buttons_states_da = dtl_scans_loop_cntrl_da.childAdaptor("buttons")
		if(buttons_states_da.intValue("keep_phases") == 1):
			self.start_stop_panel.keepPhases_RadioButton.setSelected(true)
		else:
			self.start_stop_panel.keepPhases_RadioButton.setSelected(false)
		if(buttons_states_da.intValue("keep_amps") == 1):
			self.start_stop_panel.keepAmps_RadioButton.setSelected(true)
		else:
			self.start_stop_panel.keepAmps_RadioButton.setSelected(false)
		#----------------------------------------------------------------
		if(dtl_scans_loop_cntrl_da == null): return
		for cav_acc_scan_controller in self.cav_acc_scan_controllers:
			cav_acc_scan_controller_top_da = dtl_scans_loop_cntrl_da.childAdaptor(cav_acc_scan_controller.cav_wrapper.alias)
			cav_acc_scan_controller_da = cav_acc_scan_controller_top_da.childAdaptor("dtl_acc_scan_cavity_cntroller")
			cav_acc_scan_controller.readDataFromXML(cav_acc_scan_controller_da)

