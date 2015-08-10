# The controller and auxilary classes for the main loop over MEBT, DTL, and CCL cavities

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
from java.util import ArrayList
from javax.swing.table import AbstractTableModel, TableModel
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener

from xal.ca import ChannelFactory

from xal.smf.data import XMLDataManager
from xal.smf import AcceleratorSeqCombo

from xal.smf.impl import Marker, Quadrupole, RfGap, BPM
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier


from abstract_cavity_controller_lib import Abstract_Cavity_Controller 
from mebt_cavity_controller_lib import MEBT_Cavity_Controller
from dtl1_cavity_controller_lib import DTL1_Cavity_Controller
from dtl_ccl_cavity_controller_lib import DTL_CCL_Cavity_Controller
from particle_tracker_model_lib import Particle_Tracker_Model, Envelop_Tracker_Model
from data_acquisition_classes_lib import BPM_Wrapper
from functions_and_classes_lib import makePhaseNear

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#   Auxiliary Classes and Functions
#------------------------------------------------------------------------	
class Cavity_Wrapper:
	def __init__(self,cav,pos,alias,amp_pv_name,phase_pv_name,cav_pwr_pv_name):
		self.cav = cav
		self.pos = pos
		self.alias = alias
		self.design_amp = cav.getDfltCavAmp()
		self.design_phase = cav.getDfltCavPhase()
		self.amp_pv_name = amp_pv_name
		self.phase_pv_name = phase_pv_name
		self.cav_pwr_pv_name = cav_pwr_pv_name
		self.amp_pv = null
		self.phase_pv = null
		self.cav_pwr_pv = null
		#---- initial amp and phase 
		self.initAmp = 0.
		self.initPhase = 0.
		#---- live found amp and phase 
		self.liveAmp = 0.
		self.livePhase = 0.			
		#---- new found amp and phase 
		self.newAmp = 0.
		self.newPhase = 0.	
		#---- new pwr goal, avg and sigma (in %) measured 
		self.net_pwr_goal = 0.
		self.net_pwr_avg = 0.
		self.net_pwr_sigma = 0.
		#---- energy parameters
		self.Ekin_in = 0.
		self.Ekin_out = 0.
		self.Ekin_in_design = 0.
		self.Ekin_in_delta_design = 0.
		self.Ekin_out_design = 0.
		self.Ekin_out_delta_design = 0.
		#---- found to design realtions
		#---- live amp = self.cav_amp_coeff*amp_design
		#---- self.newPhase = about self.cav_phase_guess
		self.cav_amp_coeff = 1.0
		self.cav_phase_guess = 0.
		self.avg_pasta_err = 0.
		#---- fitting params usage
		self.Ekin_in_fit_use = true
		self.cav_amp_coeff_fit_use = true
		self.cav_phase_guess_fit_use = true
		#---- safe region width for the cavity's amplitude in %
		self.safe_relative_amp_up_text = null
		self.safe_relative_amp_down_text = null
		
	def connectPVs(self):
		if(self.amp_pv == null or self.phase_pv == null or self.cav_pwr_pv == null):
			self.amp_pv = ChannelFactory.defaultFactory().getChannel(self.amp_pv_name)
			self.phase_pv = ChannelFactory.defaultFactory().getChannel(self.phase_pv_name)
			self.cav_pwr_pv = ChannelFactory.defaultFactory().getChannel(self.cav_pwr_pv_name)
			self.amp_pv.connectAndWait(0.5)
			self.phase_pv.connectAndWait(0.5)
			self.cav_pwr_pv.connectAndWait(0.5)
		
	def init(self):
		self.initAmp = self.amp_pv.getValDbl()
		self.initPhase = self.phase_pv.getValDbl()
		self.net_pwr_avg = self.cav_pwr_pv.getValDbl()
		self.newAmp = 0.
		self.newPhase = 0.
		
	def getLiveNetPower(self):
		self.net_pwr_avg = self.cav_pwr_pv.getValDbl()
		return self.net_pwr_avg
		
	def restoreInitialAmpPhase(self):
		# ??????
		if(self.initAmp != 0.):
			self.amp_pv.putVal(self.initAmp)
		self.phase_pv.putVal(self.initPhase)
		pass
		
	def setLiveAmp(self,amp):
		if(amp == 0.): return
		if(self.safe_relative_amp_up_text == null or self.safe_relative_amp_down_text == null):
			self.amp_pv.putVal(amp)
		else:
			amp_new = amp
			amp_max = self.initAmp*(1.0+0.01*self.safe_relative_amp_up_text.getValue())
			amp_min = self.initAmp*(1.0-0.01*self.safe_relative_amp_down_text.getValue())
			if(amp > amp_max): amp_new = amp_max
			if(amp < amp_min): amp_new = amp_min
			self.amp_pv.putVal(amp_new)
		# ??????
		pass
	
	def setLivePhase(self,phase):
		self.phase_pv.putVal(makePhaseNear(phase,0.))
		# ??????
		pass
	
	def getLivePhase(self):
		return self.phase_pv.getValDbl()
		
	def getLiveAmp(self):
		return self.amp_pv.getValDbl()		
	
	def scaleDesignAmp(self,coeff):
		self.cav.updateDesignAmp(self.design_amp*coeff)	
		
	def setBlankBeam(self,bool_val):
		self.cav.setBlankBeam(bool_val)
		# ??????
		pass
	
	def writeDataToXML(self,cav_wrapper_da):
		cav_wrapper_da.setValue("initAmp",self.initAmp)
		cav_wrapper_da.setValue("initPhase",self.initPhase)
		cav_wrapper_da.setValue("liveAmp",self.liveAmp)
		cav_wrapper_da.setValue("livePhase",self.livePhase)
		cav_wrapper_da.setValue("newAmp",self.newAmp)
		cav_wrapper_da.setValue("newPhase",self.newPhase)
		cav_wrapper_da.setValue("net_pwr_goal",self.net_pwr_goal)
		cav_wrapper_da.setValue("net_pwr_avg",self.net_pwr_avg)
		cav_wrapper_da.setValue("net_pwr_sigma",self.net_pwr_sigma)
		design_and_fit_params_da = cav_wrapper_da.createChild("design_and_fit_params")
		design_and_fit_params_da.setValue("Ekin_in",self.Ekin_in)
		design_and_fit_params_da.setValue("Ekin_out",self.Ekin_out)
		design_and_fit_params_da.setValue("Ekin_in_design",self.Ekin_in_design)
		design_and_fit_params_da.setValue("Ekin_in_delta_design",self.Ekin_in_delta_design)
		design_and_fit_params_da.setValue("Ekin_out_design",self.Ekin_out_design)
		design_and_fit_params_da.setValue("Ekin_out_delta_design",self.Ekin_out_delta_design)
		design_and_fit_params_da.setValue("cav_amp_coeff",self.cav_amp_coeff)
		design_and_fit_params_da.setValue("cav_phase_guess",self.cav_phase_guess)
		design_and_fit_params_da.setValue("avg_pasta_err",self.avg_pasta_err)

	def readDataFromXML(self,cav_wrapper_da):
		self.initAmp = cav_wrapper_da.doubleValue("initAmp")
		self.initPhase = cav_wrapper_da.doubleValue("initPhase")
		self.liveAmp = cav_wrapper_da.doubleValue("liveAmp")
		self.livePhase = cav_wrapper_da.doubleValue("livePhase")
		self.newAmp = cav_wrapper_da.doubleValue("newAmp")
		self.newPhase = cav_wrapper_da.doubleValue("newPhase")
		self.net_pwr_goal = cav_wrapper_da.doubleValue("net_pwr_goal")
		self.net_pwr_avg = cav_wrapper_da.doubleValue("net_pwr_avg")
		if(cav_wrapper_da.hasAttribute("net_pwr_sigma")):
			self.net_pwr_sigma = cav_wrapper_da.doubleValue("net_pwr_sigma")
		design_and_fit_params_da = cav_wrapper_da.childAdaptor("design_and_fit_params")
		self.Ekin_in = design_and_fit_params_da.doubleValue("Ekin_in")
		self.Ekin_out = design_and_fit_params_da.doubleValue("Ekin_out")
		self.Ekin_in_design = design_and_fit_params_da.doubleValue("Ekin_in_design")
		self.Ekin_in_delta_design = design_and_fit_params_da.doubleValue("Ekin_in_delta_design")
		self.Ekin_out_design = design_and_fit_params_da.doubleValue("Ekin_out_design")
		self.Ekin_out_delta_design = design_and_fit_params_da.doubleValue("Ekin_out_delta_design")
		self.cav_amp_coeff = design_and_fit_params_da.doubleValue("cav_amp_coeff")
		self.cav_phase_guess = design_and_fit_params_da.doubleValue("cav_phase_guess")
		self.avg_pasta_err = design_and_fit_params_da.doubleValue("avg_pasta_err")

class Loop_Run_State:
	""" Describes the main loop state """
	def __init__(self):
		self.isRunning  = false
		self.shouldStop = false
	
class Loop_Runner(Runnable):
	def __init__(self,main_loop_controller, run_to_end = true):
		self.main_loop_controller = main_loop_controller 
		self.run_to_end = run_to_end
		self.cav_active_ind = -1

	def run(self):
		self.main_loop_controller.loop_run_state.isRunning = true
		self.main_loop_controller.loop_run_state.shouldStop = false
		self.cav_active_ind = -1
		self.runMainLoop()
		self.main_loop_controller.loop_run_state.isRunning = false
		self.main_loop_controller.loop_run_state.shouldStop = false		
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		if(self.cav_active_ind >= 0):
			self.main_loop_controller.cav_table.setRowSelectionInterval(self.cav_active_ind,self.cav_active_ind)
		
		
	def runMainLoop(self):
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return
		status_text = self.main_loop_controller.status_text
		status_text.setText("running")
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		#---------start deal with selected cavities
		cav_selected_inds = self.main_loop_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to start SetUp!")	
			status_text.setText("Not running.")
			return
		# these are the table indexes
		cav_wrapper = null
		start_ind = cav_selected_inds[0]
		last_ind = cav_selected_inds[len(cav_selected_inds)-1]
		if(self.run_to_end): last_ind = len(self.main_loop_controller.cav_wrappers) - 1
		#----- blank cavities started from (start_ind+1)
		try:
			for cav_ind in range(0,start_ind+1):
				cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
				cav_wrapper.setBlankBeam(false)			
			for cav_ind in range(start_ind+1,len(self.main_loop_controller.cav_wrappers)):
				cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
				cav_wrapper.setBlankBeam(true)
		except:
			messageTextField.setText("Cannot blank/un-blank cavities!")	
			status_text.setText("Not running.")
			return	
		self.main_loop_controller.main_loop_timer.init(start_ind,last_ind)
		self.main_loop_controller.main_loop_timer.startMonitor()
		start_cav_name = self.main_loop_controller.cav_wrappers[start_ind].alias
		stop_cav_name = self.main_loop_controller.cav_wrappers[last_ind].alias
		txt_status = "From "+ start_cav_name+" to "+stop_cav_name+". Cav="
		self.cav_active_ind = -1
		force_stop = false
		for cav_ind in range(start_ind,last_ind+1):
			self.cav_active_ind = cav_ind
			if(self.main_loop_controller.loop_run_state.shouldStop):
				force_stop = true
				break
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
			cav_wrapper.setBlankBeam(false)
			status_text.setText(txt_status+cav_wrapper.alias+" setup is running!")
			self.main_loop_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)
			cav_controller = self.main_loop_controller.cav_controllers[cav_ind]
			#--------------------------------------------------------
			cav_controller.initProgressBar()
			(res,txt) = cav_controller.runSetUpAlgorithm()
			if(res and (not self.main_loop_controller.keepAllCavParams_RadioButton.isSelected())):
				cav_wrapper.setLivePhase(cav_wrapper.newPhase)
				if(self.main_loop_controller.keepAmps_RadioButton.isSelected()):
					cav_wrapper.setLiveAmp(cav_wrapper.initAmp)
				else:
					cav_wrapper.setLiveAmp(cav_wrapper.newAmp)
			else:
				cav_wrapper.setLivePhase(cav_wrapper.initPhase)
				cav_wrapper.setLiveAmp(cav_wrapper.initAmp)
			cav_controller.initProgressBar()
			#--------------------------------------------------------	
			if(not res):
				messageTextField.setText(txt)
				status_text.setText("Not running.")
				return
			if(self.main_loop_controller.loop_run_state.shouldStop):
				force_stop = true
				break
			self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		cav_wrapper = self.main_loop_controller.cav_wrappers[self.cav_active_ind]
		if(force_stop):
			messageTextField.setText("The setup stopped by user's request! Cavity="+cav_wrapper.alias)
		else:
			messageTextField.setText("The setup finished at cavity="+cav_wrapper.alias+"!")
		status_text.setText("Not running.")
		return		

class Main_Loop_Timer_Runner(Runnable):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller 
		self.time_step = 1.0

	def run(self):
		while(1 < 2):
			time.sleep(self.time_step)
			if(not self.main_loop_controller.loop_run_state.isRunning):
				self.main_loop_controller.main_loop_timer.time_estimate_text.setText("")
				return
			self.main_loop_controller.main_loop_timer.updateProgress()

class Main_Loop_Timer:
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller 
		self.time_estimate_text = 	JTextField(30)
		self.time_estimate_label = JLabel("Time:",JLabel.RIGHT)
		self.total_time = 0.
		self.run_time = 0.
		self.start_time = 0.
		
	def init(self,start_ind,last_ind):
		self.total_time = 0.
		for cav_ind in range(start_ind,last_ind+1):
			cav_controller = self.main_loop_controller.cav_controllers[cav_ind]
			self.total_time += cav_controller.setMaxTimeCount()
			self.total_time += cav_controller.getPastaFittingTime()
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
		runner = Main_Loop_Timer_Runner(self.main_loop_controller)
		thr = Thread(runner)
		thr.start()				

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	

class Init_Cav_Controllers_Panel(JPanel):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		self.setBorder(BorderFactory.createEtchedBorder())
		init_cavs_button = JButton("Init All")
		init_cavs_button.addActionListener(Init_Cavs_Button_Listener(self.main_loop_controller))	
		init_selected_cavs_button = JButton("Init Selected")
		init_selected_cavs_button.addActionListener(Init_Selected_Cavs_Button_Listener(self.main_loop_controller))	
		restore_cavs_button = JButton("Restore Init -> EPICS")
		restore_cavs_button.addActionListener(Restore_Cavs_Button_Listener(self.main_loop_controller))		
		unBlank_cavs_button = JButton("UnBlank All Cavs")
		unBlank_cavs_button.addActionListener(UnBlank_Cavs_Button_Listener(self.main_loop_controller))	
		self.add(init_cavs_button)
		self.add(init_selected_cavs_button)
		self.add(restore_cavs_button)
		self.add(unBlank_cavs_button)
		
class Start_Stop_Panel(JPanel):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		self.setLayout(GridLayout(3,1,1,1))
		self.setBorder(BorderFactory.createEtchedBorder())
		start_button = JButton("Start")
		start_button.addActionListener(Start_Button_Listener(self.main_loop_controller))	
		start_selected_button = JButton("Start Selected Cavs.")
		start_selected_button.addActionListener(Start_Selected_Cavs_Button_Listener(self.main_loop_controller))		
		stop_button = JButton("Stop")
		stop_button.addActionListener(Stop_Button_Listener(self.main_loop_controller))	
		send_amp_phase_to_EPICS_button = JButton("Send New Amp&Phase to EPICS for Selected Cavs")
		send_amp_phase_to_EPICS_button.addActionListener(Send_Amp_Phase_to_EPICS_Button_Listener(self.main_loop_controller))	
		restore_amp_phase_to_EPICS_button = JButton("Restore Init Amp&Phase to EPICS for Selected Cavs")
		restore_amp_phase_to_EPICS_button.addActionListener(Restore_Amp_Phase_of_Selected_Cavs_to_EPICS_Button_Listener(self.main_loop_controller))	
		correct_phase_shifts_button = JButton("Correct Phase Shifts for 360 deg Scans")
		correct_phase_shifts_button.addActionListener(Correct_Phase_Shifts_for_360deg_Scans_Button_Listener(self.main_loop_controller))		
		self.keepAllCavParams_RadioButton = JRadioButton("Keep Cavs' Amps&Phases")
		self.keepAmps_RadioButton = JRadioButton("Keep Cavities' Amplitudes")		
		self.status_text = JTextField(30)
		self.status_text.setForeground(Color.red)
		self.status_text.setText("Not running.")
		status_text_label = JLabel("Loop status:",JLabel.RIGHT)
		status_panel_tmp0 = JPanel(GridLayout(2,1,1,1))
		status_panel_tmp0.add(status_text_label)
		status_panel_tmp0.add(self.main_loop_controller.main_loop_timer.time_estimate_label)
		status_panel_tmp1 = JPanel(GridLayout(2,1,1,1))
		status_panel_tmp1.add(self.status_text)
		status_panel_tmp1.add(self.main_loop_controller.main_loop_timer.time_estimate_text)
		status_panel = JPanel(BorderLayout())
		status_panel.add(status_panel_tmp0,BorderLayout.WEST)
		status_panel.add(status_panel_tmp1,BorderLayout.CENTER)
		status_panel.setBorder(BorderFactory.createEtchedBorder())
		buttons_panel0 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		buttons_panel0.add(start_button)
		buttons_panel0.add(start_selected_button)
		buttons_panel0.add(stop_button)
		buttons_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		buttons_panel1.add(self.keepAllCavParams_RadioButton)
		buttons_panel1.add(self.keepAmps_RadioButton)
		buttons_panel = JPanel(GridLayout(2,1,1,1))
		buttons_panel.add(buttons_panel0)
		buttons_panel.add(buttons_panel1)
		#---------------------------------------
		bottom_buttons_panel0 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		bottom_buttons_panel0.add(send_amp_phase_to_EPICS_button)
		bottom_buttons_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		bottom_buttons_panel1.add(restore_amp_phase_to_EPICS_button)
		bottom_buttons_panel2 = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		bottom_buttons_panel2.add(correct_phase_shifts_button)
		bottom_buttons_panel = JPanel(GridLayout(3,1,1,1))
		bottom_buttons_panel.add(bottom_buttons_panel0)
		bottom_buttons_panel.add(bottom_buttons_panel1)
		bottom_buttons_panel.add(bottom_buttons_panel2)
		#---------------------------------------
		self.add(buttons_panel)	
		self.add(status_panel)
		self.add(bottom_buttons_panel)

#------------------------------------------------
#  JTable models
#------------------------------------------------

class Cavities_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		self.columnNames = ["Cavity",]
		self.columnNames += ["<html>&phi;<SUB>design</SUB>[deg]<html>",]
		self.columnNames += ["<html>A<SUB>init</SUB>[a.u.]<html>",]
		self.columnNames += ["<html>&phi;<SUB>init</SUB>[deg]<html>",]
		self.columnNames += ["<html>A<SUB>new</SUB>[a.u.]<html>",]
		self.columnNames += ["<html>&phi;<SUB>new</SUB>[deg]<html>",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.main_loop_controller.cav_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.main_loop_controller.cav_wrappers[row]
		if(col == 0): return cav_wrapper.alias
		if(col == 1): return "%5.1f"%cav_wrapper.design_phase
		if(col == 2): return "%5.3f"%cav_wrapper.initAmp
		if(col == 3): return "%5.1f"%cav_wrapper.initPhase
		if(col == 4): return "%5.3f"%cav_wrapper.newAmp	
		if(col == 5): return "%5.1f"%cav_wrapper.newPhase 
		return ""
				
	def getColumnClass(self,col):
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.main_loop_controller.cav_wrappers[row]

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		cav_controller = self.main_loop_controller.cav_controllers[index]
		tabbedPane = self.main_loop_controller.tabbedPane		
		tabbedPane.setComponentAt(0,cav_controller.getMainPanel())
		tabbedPane.setComponentAt(1,cav_controller.getParamsPanel())
		tabbedPane.setSelectedIndex(0)
		tabbedPane.setTitleAt(0,cav_controller.cav_wrapper.alias)
		tabbedPane.setTitleAt(1,"Tune Params.")

class Init_Cavs_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		self.main_loop_controller.initAllCavControllers()
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
			
class Init_Selected_Cavs_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.main_loop_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to set New Amp&Phase!")	
			return
		ind_start = cav_selected_inds[0]
		ind_stop =  cav_selected_inds[len(cav_selected_inds)-1]
		self.main_loop_controller.initAllCavControllers(ind_start,ind_stop)
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.setRowSelectionInterval(ind_start,ind_stop)
			
class Restore_Cavs_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return		
		cav_wrappers =  	self.main_loop_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			try:
				cav_wrapper.restoreInitialAmpPhase()
			except:
				messageTextField.setText("Cannot write to cavity PVs! Cavity="+cav_wrapper.alias)
				return		
			
class UnBlank_Cavs_Button_Listener	(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return		
		cav_wrappers =  	self.main_loop_controller.cav_wrappers
		try:
			for cav_wrapper in cav_wrappers:
				cav_wrapper.setBlankBeam(false)
		except:
			messageTextField.setText("Cannot write to cavities' PVs!")
			return
			
class Start_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		if(self.main_loop_controller.loop_run_state.isRunning): 
			messageTextField.setText("The SetUp Loop is running already!")
			return
		runner = Loop_Runner(self.main_loop_controller,true)
		thr = Thread(runner)
		thr.start()					
			

class Start_Selected_Cavs_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		if(self.main_loop_controller.loop_run_state.isRunning):
			messageTextField.setText("The SetUp Loop is running already!")
			return
		runner = Loop_Runner(self.main_loop_controller,false)
		thr = Thread(runner)
		thr.start()					

class Stop_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		if(not self.main_loop_controller.loop_run_state.isRunning):
			messageTextField.setText("The SetUp Loop is not running!")
			return	
		self.main_loop_controller.loop_run_state.shouldStop = true

class Send_Amp_Phase_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return	
		cav_selected_inds = self.main_loop_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to set New Amp&Phase!")	
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
			cav_wrapper.setLivePhase(cav_wrapper.newPhase)
			cav_wrapper.setLiveAmp(cav_wrapper.newAmp)

class Restore_Amp_Phase_of_Selected_Cavs_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return	
		cav_selected_inds = self.main_loop_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to restore Amp&Phase!")	
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
			cav_wrapper.setLivePhase(cav_wrapper.initPhase)
			cav_wrapper.setLiveAmp(cav_wrapper.initAmp)

class Correct_Phase_Shifts_for_360deg_Scans_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.main_loop_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return	
		cav_selected_inds = self.main_loop_controller.cav_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to correct Phase Shifts for 360 deg Scans!")	
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind]
			cav_controller = self.main_loop_controller.cav_controllers[cav_ind]
			phase_shift = cav_controller.guess_phase_shift_text.getValue()
			initPhase = cav_wrapper.initPhase
			newPhase = cav_wrapper.newPhase
			delta_phase_shift = initPhase -  newPhase
			phase_shift += delta_phase_shift
			phase_shift = makePhaseNear(phase_shift,0.)
			cav_controller.guess_phase_shift_text.setValue(phase_shift)

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class Main_Loop_Controller:
	def __init__(self,top_document,accl):
		#--- top_document is a parent document for all controllers
		self.top_document = top_document		
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#----main loop timer
		self.main_loop_timer = Main_Loop_Timer(self)		
		#---- set up accSeq
		self.accSeq = null
		seq_names = ["MEBT","DTL1","DTL2","DTL3","DTL4","DTL5","DTL6","CCL1","CCL2","CCL3","CCL4"]
		lst = ArrayList()
		for seqName in seq_names:
			lst.add(accl.getSequence(seqName))
		self.accSeq = AcceleratorSeqCombo("SEQUENCE", lst)
		rf_gaps = self.accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cav_wrappers = []
		cavs = []
		count = 0
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			pos = self.accSeq.getPosition(cav)
			if(cav not in cavs):
				#print "debug cav=",cav.getId()," count=",count
				cavs.append(cav)				
				alias = "null"
				if(count > 3):
					alias = seq_names[count+1-4]
				else:
					alias = seq_names[0]+str(count+1)
				cav_llrf_name = "null"
				cav_pwr_pv_name = "nullPwr"
				if(count <= 3):
					cav_llrf_name = "MEBT_LLRF:FCM"+str(count+1)
					cav_pwr_pv_name = "MEBT_LLRF:Cav"+str(count+1)+":NetPwr"
				if(count > 3 and count < 10):
					cav_llrf_name = "DTL_LLRF:FCM"+str(count-3)
					cav_pwr_pv_name = "DTL_LLRF:Cav"+str(count-3)+":NetPwr"
				if(count > 9):
					cav_llrf_name = "CCL_LLRF:FCM"+str(count-9)
					cav_pwr_pv_name = "CCL_LLRF:Cav"+str(count-9)+":NetPwr"					
				amp_pv_name = cav_llrf_name+":CtlAmpSet"
				phase_pv_name = cav_llrf_name+":CtlPhaseSet"
				self.cav_wrappers.append(Cavity_Wrapper(cav,pos,alias,amp_pv_name,phase_pv_name,cav_pwr_pv_name))	
				#print "debug =================================="
				#print "debug cav=",cav.getId(),"  alias=",alias,"  amp_pv_name=",amp_pv_name," phase_pv_name=",phase_pv_name
				#print "debug         cav_pwr_pv_name=",cav_pwr_pv_name
				#print "debug cav=",cav.getId()," pos =",pos," L=",cav.getLength()
				count += 1
		#---- BPMs
		self.bpm_wrappers = []
		bpms = self.accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType)))	
		for bpm in bpms:
			pos = self.accSeq.getPosition(bpm)
			pos += 0.5*bpm.getBPMBucket().getLength()*bpm.getBPMBucket().getOrientation() 
			bpm_wrapper = BPM_Wrapper(bpm,pos)
			#print "debug bpm=",bpm_wrapper.alias," pos =",pos		
			self.bpm_wrappers.append(bpm_wrapper)
		#---- SCL first BPMs and Cavs
		lst = ArrayList()
		for seqName in ["SCLMed",]:	
			lst.add(accl.getSequence(seqName))
		self.scl_accSeq = AcceleratorSeqCombo("SCL_SEQUENCE", lst)
		bpms = self.scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType))).andStatus(true))
		bpms = bpms[:5]
		self.scl_bpm_wrappers = []
		for bpm in bpms:
			pos = self.scl_accSeq.getPosition(bpm) + self.accSeq.getLength()
			pos += 0.5*bpm.getBPMBucket().getLength()*bpm.getBPMBucket().getOrientation()		
			bpm_wrapper = BPM_Wrapper(bpm,pos)
			#print "debug bpm=",bpm_wrapper.alias," pos =",pos				
			self.scl_bpm_wrappers.append(bpm_wrapper)
		rf_gaps = self.scl_accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		cavs = []
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			if((cav not in cavs) and cav.getStatus()):
				cavs.append(cav)
		cavs = cavs[:9]
		self.scl_cav_wrappers = []
		for cav in cavs:
			pos = self.scl_accSeq.getPosition(cav) + self.accSeq.getLength()
			alias = cav.getId().split(":")[1]	
			amp_pv_name = "SCL_LLRF:"+alias.replace("Cav","FCM")+":CtlAmpSet"
			phase_pv_name = "SCL_LLRF:"+alias.replace("Cav","FCM")+":CtlPhaseSet"
			cav_pwr_pv_name = "SCL_LLRF:"+alias+":NetPwr"	
			self.scl_cav_wrappers.append(Cavity_Wrapper(cav,pos,alias,amp_pv_name,phase_pv_name,cav_pwr_pv_name))
			#print "debug =================================="
			#print "debug cav=",cav.getId(),"  alias=",alias,"  amp_pv_name=",amp_pv_name," phase_pv_name=",phase_pv_name
			#print "debug         cav_pwr_pv_name=",cav_pwr_pv_name			
		#---- Panels setup
		#---- Auxiliaries setup
		self.loop_run_state = Loop_Run_State()
		self.loop_run_state.isRunning = false
		self.loop_run_state.shouldStop = false
		#---------------------------------------------
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---- Cavities' Controllers 
		self.cav_controllers = []
		self.cav_controllers.append(MEBT_Cavity_Controller(self,self.cav_wrappers[0]))
		self.cav_controllers.append(MEBT_Cavity_Controller(self,self.cav_wrappers[1]))
		self.cav_controllers.append(MEBT_Cavity_Controller(self,self.cav_wrappers[2]))
		self.cav_controllers.append(MEBT_Cavity_Controller(self,self.cav_wrappers[3]))
		#---------------------------------
		self.cav_controllers.append(DTL1_Cavity_Controller(self,self.cav_wrappers[4]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[5]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[6]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[7]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[8]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[9]))
		#----------------------------------
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[10]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[11]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[12]))
		self.cav_controllers.append(DTL_CCL_Cavity_Controller(self,self.cav_wrappers[13]))	
		#------ cavity controllers customization --------------------
		#------ MEBT Reb. 4 -------
		self.cav_controllers[3].cav_amp_backward_steps_mult_text.setValue(8.0)
		#----- amplitudes limits
		self.cav_controllers[0].safe_relative_amp_down_text.setValue(50.)
		self.cav_controllers[1].safe_relative_amp_down_text.setValue(50.)
		self.cav_controllers[2].safe_relative_amp_down_text.setValue(50.)
		self.cav_controllers[3].safe_relative_amp_down_text.setValue(50.)
		#----- No PASTA Use for DTL2-6 and CCL4
		self.cav_controllers[5].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		self.cav_controllers[6].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		self.cav_controllers[7].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		self.cav_controllers[8].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		self.cav_controllers[9].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		self.cav_controllers[13].scan_main_panel.use_PASTA_RadioButton.setSelected(false)
		#------ Phase corrections after Full scan for MEBT
		self.cav_controllers[0].guess_phase_shift_text.setValue(-10.0)
		self.cav_controllers[1].guess_phase_shift_text.setValue(-6.0)
		#------ Phase and amplitude corrections after Full scan for inner BPM
		self.cav_controllers[5].guess_phase_shift_text.setValue(-4.0)
		self.cav_controllers[5].guess_cav_amp_shift_text.setValue(-2.0)
		self.cav_controllers[6].guess_phase_shift_text.setValue(1.)
		self.cav_controllers[6].guess_cav_amp_shift_text.setValue(0.7)
		self.cav_controllers[8].guess_phase_shift_text.setValue(0.)
		self.cav_controllers[8].guess_cav_amp_shift_text.setValue(-0.7)
		self.cav_controllers[10].guess_phase_shift_text.setValue(0.)
		self.cav_controllers[10].guess_cav_amp_shift_text.setValue(-3.6)
		self.cav_controllers[11].guess_phase_shift_text.setValue(0.)
		self.cav_controllers[11].guess_cav_amp_shift_text.setValue(-3.7)		
		#------ The BPMs for Full Scan and PASTA
		self.cav_controllers[0].cav_bpms_controller.work_bpm_index = 1 
		self.cav_controllers[1].cav_bpms_controller.work_bpm_index = 1 		
		self.cav_controllers[5].cav_bpms_controller.pasta_bpm_0_index = 2
		self.cav_controllers[5].cav_bpms_controller.pasta_bpm_1_index = 5
		self.cav_controllers[6].cav_bpms_controller.pasta_bpm_0_index = 2
		self.cav_controllers[6].cav_bpms_controller.pasta_bpm_1_index = 4
		self.cav_controllers[7].cav_bpms_controller.pasta_bpm_0_index = 2
		self.cav_controllers[7].cav_bpms_controller.pasta_bpm_1_index = 5
		self.cav_controllers[8].cav_bpms_controller.pasta_bpm_0_index = 5
		self.cav_controllers[8].cav_bpms_controller.pasta_bpm_1_index = 6
		self.cav_controllers[9].cav_bpms_controller.pasta_bpm_0_index = 3
		self.cav_controllers[9].cav_bpms_controller.pasta_bpm_1_index = 5
		self.cav_controllers[10].cav_bpms_controller.pasta_bpm_0_index = 5
		self.cav_controllers[10].cav_bpms_controller.pasta_bpm_1_index = 6
		self.cav_controllers[11].cav_bpms_controller.pasta_bpm_0_index = 2
		self.cav_controllers[11].cav_bpms_controller.pasta_bpm_1_index = 3
		self.cav_controllers[12].cav_bpms_controller.pasta_bpm_0_index = 2
		self.cav_controllers[12].cav_bpms_controller.pasta_bpm_1_index = 4
		self.cav_controllers[13].cav_bpms_controller.pasta_bpm_0_index = 3
		self.cav_controllers[13].cav_bpms_controller.pasta_bpm_1_index = 4
		self.cav_controllers[11].cav_bpms_controller.sin_wave_bpm_index = 0
		self.cav_controllers[12].cav_bpms_controller.sin_wave_bpm_index = 0
		self.cav_controllers[13].cav_bpms_controller.sin_wave_bpm_index = 1
		#-----Target power levels
		self.cav_controllers[4].cav_wrapper.net_pwr_goal =  378.0
		self.cav_controllers[5].cav_wrapper.net_pwr_goal = 1208.0
		self.cav_controllers[6].cav_wrapper.net_pwr_goal = 1294.0
		self.cav_controllers[7].cav_wrapper.net_pwr_goal = 1416.0
		self.cav_controllers[8].cav_wrapper.net_pwr_goal = 1454.0
		self.cav_controllers[9].cav_wrapper.net_pwr_goal = 1414.0
		self.cav_controllers[10].cav_wrapper.net_pwr_goal = 2531.0
		self.cav_controllers[11].cav_wrapper.net_pwr_goal = 2984.0
		self.cav_controllers[12].cav_wrapper.net_pwr_goal = 3018.0
		self.cav_controllers[13].cav_wrapper.net_pwr_goal = 2856.0
		#----------------------------------------------   
		left_panel = JPanel(BorderLayout())
		self.tabbedPane = JTabbedPane()		
		self.tabbedPane.add("Cavity",JPanel(BorderLayout()))	
		self.tabbedPane.add("Parameters",JPanel(BorderLayout()))
		#--------------------------------------------------------
		self.cav_table = JTable(Cavities_Table_Model(self))
		self.cav_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.cav_table.setFillsViewportHeight(true)
		self.cav_table.setPreferredScrollableViewportSize(Dimension(500,240))
		self.cav_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self))
		scrl_cav_panel = JScrollPane(self.cav_table)
		scrl_cav_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavities' Parameters"))
		init_buttons_panel = Init_Cav_Controllers_Panel(self)
		start_stop_panel = Start_Stop_Panel(self)
		#---- fake scan button
		self.keepAllCavParams_RadioButton = start_stop_panel.keepAllCavParams_RadioButton
		self.keepAmps_RadioButton = start_stop_panel.keepAmps_RadioButton
		#---- status text field
		self.status_text = start_stop_panel.status_text
		tmp0_panel = JPanel(BorderLayout())
		tmp0_panel.add(init_buttons_panel,BorderLayout.NORTH)
		tmp0_panel.add(scrl_cav_panel,BorderLayout.CENTER)
		tmp0_panel.add(start_stop_panel,BorderLayout.SOUTH)
		tmp1_panel = JPanel(BorderLayout())
		tmp1_panel.add(tmp0_panel,BorderLayout.NORTH)
		left_panel.add(tmp1_panel,BorderLayout.WEST)
		#--------------------------------------------------------
		self.main_panel.add(left_panel,BorderLayout.WEST)
		self.main_panel.add(self.tabbedPane,BorderLayout.CENTER)
		#---- non GUI controllers
		self.particle_tracker_model = Particle_Tracker_Model(self)
		self.env_tracker_model = Envelop_Tracker_Model(self)
		
	def getMainPanel(self):
		return self.main_panel
		
	def getMessageTextField(self):
		return self.top_document.getMessageTextField()
		
	def writeDataToXML(self,root_da):
		main_loop_cntrl_da = root_da.createChild("MAIN_CONTROLLER")
		main_loop_cntrl_da.setValue("keep_cav_params",self.keepAllCavParams_RadioButton.isSelected())
		main_loop_cntrl_da.setValue("keep_cav_amp",self.keepAmps_RadioButton.isSelected())
		#---------------------------------------------------------------------------------
		bpm_wrappers_da = main_loop_cntrl_da.createChild("BPM_WRAPPERS")
		for bpm_wrapper in self.bpm_wrappers:
			bpm_wrapper.writeDataToXML(bpm_wrappers_da)
		for bpm_wrapper in self.scl_bpm_wrappers:
			bpm_wrapper.writeDataToXML(bpm_wrappers_da)		
		#---------------------------------------------------------------------------------
		for cav_controller in self.cav_controllers:
			cav_controller.writeDataToXML(main_loop_cntrl_da)

	def readDataFromXML(self,root_da):		
		main_loop_cntrl_da = root_da.childAdaptor("MAIN_CONTROLLER")
		if(main_loop_cntrl_da.intValue("keep_cav_params") == 1):
			self.keepAllCavParams_RadioButton.setSelected(true)
		else:
			self.keepAllCavParams_RadioButton.setSelected(false)
		if(main_loop_cntrl_da.intValue("keep_cav_amp") == 1):
				self.keepAmps_RadioButton.setSelected(true)
		else:
				self.keepAmps_RadioButton.setSelected(false)
		#-----------------------------------------------------
		bpm_wrappers_da = main_loop_cntrl_da.childAdaptor("BPM_WRAPPERS")
		if(bpm_wrappers_da != null):
			for bpm_wrapper in self.bpm_wrappers:
				bpm_wrapper.readDataFromXML(bpm_wrappers_da)
			for bpm_wrapper in self.scl_bpm_wrappers:
				bpm_wrapper.readDataFromXML(bpm_wrappers_da)			
		#-----------------------------------------------------
		for cav_controller in self.cav_controllers:
			cav_cntrl_data_da = main_loop_cntrl_da.childAdaptor("CAVITY_CONTROLLER_"+cav_controller.cav_wrapper.alias)
			cav_controller.readDataFromXML(cav_cntrl_data_da)
			
	def connectAllPVs(self):
		for cav_wrapper in self.cav_wrappers:
			try:
				cav_wrapper.connectPVs()
			except:
				self.getMessageTextField().setText("Cannot connect PVs for cavity="+cav_wrapper.alias)
				return false
		return true
		
	def initAllCavControllers(self, ind_start = -1, ind_stop = -1):
		res = self.connectAllPVs()
		if(not res):
			return false
		if(ind_start < 0):
			ind_start = 0
			ind_stop = len(self.cav_controllers) - 1
		for cav_controller in self.cav_controllers[ind_start:ind_stop+1]:
			try:
				cav_controller.init()
			except:
				self.getMessageTextField().setText("Cannot read cavities' PVs! Cavity="+cav_controller.cav_wrapper.alias)
				return	false
		return true
		
	def setBPM_Off(self,bpm_wrapper):
		""" 
		Set BPM as bad and unused in measurements. 
		Returns true if it is successful, false otherwise.
		"""
		self.getMessageTextField().setText("")
		txt = ""
		for cav_controller in self.cav_controllers:
			if(cav_controller.checkBPM_Usage(bpm_wrapper)):
				txt += " "+cav_controller.cav_wrapper.alias
		if(txt != ""):
			self.getMessageTextField().setText("Cannot remove "+bpm_wrapper.alias+" It is used in cavs: "+txt)
			return false
		return true


