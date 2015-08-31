# The controller and auxilary classes for the net RF power settings of the warm linac cavities 
# We use only DTL1-CCL4 cavities here.

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

from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat

from xal.smf.impl import Marker, Quadrupole, RfGap, BPM
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class Loop_Run_State:
	""" Describes the measuring Net Pwr loop state """
	def __init__(self):
		self.isRunning  = false
		self.shouldStop = false
	
class Loop_Runner(Runnable):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller 

	def run(self):
		self.rf_power_controller.loop_run_state.isRunning = true
		self.rf_power_controller.loop_run_state.shouldStop = false
		cav_selected_inds = self.rf_power_controller.rf_power_table.getSelectedRows()	
		cav_selected_ind0 = -1
		cav_selected_ind1 = -1
		if(len(cav_selected_inds) > 0):
			cav_selected_ind0 = cav_selected_inds[0]
			cav_selected_ind1 = cav_selected_inds[len(cav_selected_inds)-1]
		self.runMainLoop()
		self.rf_power_controller.loop_run_state.isRunning = false
		self.rf_power_controller.loop_run_state.shouldStop = false		
		self.rf_power_controller.rf_power_table.getModel().fireTableDataChanged()
		if(len(cav_selected_inds) > 0):
			self.rf_power_controller.rf_power_table.setRowSelectionInterval(cav_selected_ind0,cav_selected_ind1)
				
	def runMainLoop(self):
		res = self.rf_power_controller.main_loop_controller.connectAllPVs()
		if(not res):
			return
		status_text = self.rf_power_controller.start_stop_panel.status_text
		time_step = self.rf_power_controller.start_stop_panel.time_step_text.getValue()
		n_avg = int(self.rf_power_controller.start_stop_panel.n_avg_text.getValue())
		cav_wrappers = self.rf_power_controller.main_loop_controller.cav_wrappers[3:] 
		status_text.setText("running")
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		force_stop = false
		avg_net_power_arr = []
		avg_net_power2_arr = []
		for cav_index in range(len(cav_wrappers)):
			avg_net_power_arr.append(0.)
			avg_net_power2_arr.append(0.)
		count = 0
		for i_step in range(n_avg):
			time.sleep(time_step)
			for cav_index in range(len(cav_wrappers)):
				cav_wrapper = cav_wrappers[cav_index]
				net_pwr = math.fabs(cav_wrapper.getLiveNetPower())
				avg_net_power_arr[cav_index] += net_pwr
				avg_net_power2_arr[cav_index] += net_pwr*net_pwr
			count += 1
			status_text.setText("Running. Step # "+str(count)+" out of "+str(n_avg))
			if(self.rf_power_controller.loop_run_state.shouldStop):
				force_stop = true
				break
		if(count > 0):
			for cav_index in range(len(cav_wrappers)):
				cav_wrapper = cav_wrappers[cav_index]
				net_pwr_avg = avg_net_power_arr[cav_index]/count
				net_pwr2_avg = avg_net_power2_arr[cav_index]/count
				net_pwr_sigma = 0
				if(count > 1):
					net_pwr_sigma = math.sqrt((net_pwr2_avg-net_pwr_avg*net_pwr_avg)/(count))
				cav_wrapper.net_pwr_avg = net_pwr_avg
				if(net_pwr_avg != 0.):
					net_pwr_sigma = 100.0*net_pwr_sigma/net_pwr_avg
				cav_wrapper.net_pwr_sigma = net_pwr_sigma
		else:
			for cav_index in range(len(cav_wrappers)):
				cav_wrapper = cav_wrappers[cav_index]
				cav_wrapper.net_pwr_avg = 0.
				cav_wrapper.net_pwr_sigma = 0.
		for cav_index in range(len(cav_wrappers)):
			cav_wrapper = cav_wrappers[cav_index]
			coeff = 0.
			if(cav_wrapper.net_pwr_avg > 0.):
				coeff = cav_wrapper.net_pwr_goal/cav_wrapper.net_pwr_avg
			cav_wrapper.newAmp = math.sqrt(math.fabs(coeff))*cav_wrapper.initAmp 
		if(force_stop):
			messageTextField.setText("The averaging stopped by user's request!")
		status_text.setText("Not running.")
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class Init_RF_Power_Controller_Panel(JPanel):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		self.setBorder(BorderFactory.createEtchedBorder())
		init_cavs_button = JButton("Initialize")
		init_cavs_button.addActionListener(Init_Cavs_Button_Listener(self.rf_power_controller))	
		restore_cavs_button = JButton("Restore Init Amps. -> EPICS")
		restore_cavs_button.addActionListener(Restore_Cavs_Button_Listener(self.rf_power_controller))	
		set_new_amp_button = JButton("Set New Amps. -> EPICS ")
		set_new_amp_button.addActionListener(Set_New_Amps_Button_Listener(self.rf_power_controller))			
		self.add(init_cavs_button)
		self.add(restore_cavs_button)
		self.add(set_new_amp_button)
		
class Start_Stop_Panel(JPanel):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.setLayout(GridLayout(3,1,1,1))
		self.setBorder(BorderFactory.createEtchedBorder())
		start_button = JButton("Start Averaging")
		start_button.addActionListener(Start_Button_Listener(self.rf_power_controller))		
		stop_button = JButton("Stop")
		stop_button.addActionListener(Stop_Button_Listener(self.rf_power_controller))	
		n_avg_label = JLabel("N Avg.=",JLabel.RIGHT)
		self.n_avg_text = DoubleInputTextField(10.0,ScientificNumberFormat(2),4)
		time_step_label = JLabel("Time Step[sec]=",JLabel.RIGHT)
		self.time_step_text = DoubleInputTextField(1.1,ScientificNumberFormat(3),4)
		send_amp_phase_to_EPICS_button = JButton("Send New Amp to Selected Cavs")
		send_amp_phase_to_EPICS_button.addActionListener(Send_Amp_Phase_to_EPICS_Button_Listener(self.rf_power_controller))	
		make_new_pwrs_as_target_button = JButton("Make Measured Powers as New Traget for Selected Cavs")
		make_new_pwrs_as_target_button.addActionListener(Make_New_Pwrs_as_Target_Button_Listener(self.rf_power_controller))			
		self.status_text = JTextField(30)
		self.status_text.setForeground(Color.red)
		self.status_text.setText("Not running.")
		status_text_label = JLabel("Averaging status:",JLabel.RIGHT)
		status_panel = JPanel(BorderLayout())
		status_panel.add(status_text_label,BorderLayout.WEST)
		status_panel.add(self.status_text,BorderLayout.CENTER)
		status_panel.setBorder(BorderFactory.createEtchedBorder())
		buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,1))
		buttons_panel.add(start_button)
		buttons_panel.add(stop_button)
		buttons_panel.add(n_avg_label)
		buttons_panel.add(self.n_avg_text)
		buttons_panel.add(time_step_label)
		buttons_panel.add(self.time_step_text)
		#---------------------------------------
		bottom_buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		bottom_buttons_panel_tmp = JPanel(GridLayout(2,1,1,1))
		bottom_buttons_panel_tmp0 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		bottom_buttons_panel_tmp1 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		bottom_buttons_panel_tmp.add(bottom_buttons_panel_tmp0)
		bottom_buttons_panel_tmp.add(bottom_buttons_panel_tmp1)
		bottom_buttons_panel.add(bottom_buttons_panel_tmp)
		bottom_buttons_panel_tmp0.add(send_amp_phase_to_EPICS_button)
		bottom_buttons_panel_tmp1.add(make_new_pwrs_as_target_button)
		#---------------------------------------
		self.add(buttons_panel)	
		self.add(status_panel)
		self.add(bottom_buttons_panel)
		
#------------------------------------------------
#  JTable models
#------------------------------------------------

class RF_Power_Table_Model(AbstractTableModel):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		self.columnNames = ["Cavity",]
		self.columnNames += ["<html>P<SUB>target</SUB>[kW]<html>",]
		self.columnNames += ["<html>P<SUB>measured</SUB>[kW]<html>",]
		self.columnNames += ["<html>&sigma;(P)<SUB>measured</SUB>[%]<html>",]
		self.columnNames += ["<html>Amp<SUB>init</SUB> [a.u.]<html>",]
		self.columnNames += ["<html>Amp<SUB>new</SUB> [a.u.]<html>",]
		self.columnNames += ["<html>&delta;A<SUB>new-init</SUB>[%]<html>",]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return (len(self.main_loop_controller.cav_wrappers)-4)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.main_loop_controller.cav_wrappers[row+4]
		if(col == 0): return cav_wrapper.alias
		if(col == 1): return "%8.1f"%cav_wrapper.net_pwr_goal
		if(col == 2): return "%8.1f"%cav_wrapper.net_pwr_avg 
		if(col == 3): return "%8.1f"%cav_wrapper.net_pwr_sigma 
		if(col == 4): return "%8.4f"%cav_wrapper.initAmp
		if(col == 5): return "%8.4f"%cav_wrapper.newAmp	
		if(col == 6): 
			delta = cav_wrapper.newAmp - cav_wrapper.initAmp
			if(cav_wrapper.initAmp > 0.): 
				delta /= cav_wrapper.initAmp/100.
			return "%8.1f"%delta	
		return ""
	
	def getColumnClass(self,col):
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(col == 1): return true
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.main_loop_controller.cav_wrappers[row+4]
		try:
			cav_wrapper.net_pwr_goal = float(value)
		except:
			cav_wrapper.net_pwr_goal = 0.

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Init_Cavs_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		self.main_loop_controller.initAllCavControllers()
		self.rf_power_controller.rf_power_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
			
class Restore_Cavs_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return		
		cav_wrappers = self.main_loop_controller.cav_wrappers
		for cav_index in range(len(cav_wrappers)-4):
			cav_wrapper = cav_wrappers[cav_index+4]
			try:
				cav_wrapper.restoreInitialAmpPhase()
			except:
				messageTextField.setText("Cannot write to cavity PVs! Cavity="+cav_wrapper.alias)
				return		
			
class Set_New_Amps_Button_Listener	(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return		
		cav_wrappers = self.main_loop_controller.cav_wrappers
		for cav_index in range(len(cav_wrappers)-4):
			cav_wrapper = cav_wrappers[cav_index+4]
			try:
				cav_wrapper.setLiveAmp(cav_wrapper.newAmp)
			except:
				messageTextField.setText("Cannot write to cavities' PVs! Cav="+cav_wrapper.alias)
				return

class Start_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		if(self.rf_power_controller.loop_run_state.isRunning): 
			messageTextField.setText("The SetUp Loop is running already!")
			return
		runner = Loop_Runner(self.rf_power_controller)
		thr = Thread(runner)
		thr.start()					

class Stop_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		if(not self.rf_power_controller.loop_run_state.isRunning):
			messageTextField.setText("The Averaging Loop is not running!")
			return	
		self.rf_power_controller.loop_run_state.shouldStop = true

class Send_Amp_Phase_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		res = self.main_loop_controller.connectAllPVs()
		if(not res):
			return	
		cav_selected_inds = self.rf_power_controller.rf_power_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to set New Amp&Phase!")	
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind+4]
			cav_wrapper.setLiveAmp(cav_wrapper.newAmp)
		
class Make_New_Pwrs_as_Target_Button_Listener(ActionListener):
	def __init__(self,rf_power_controller):
		self.rf_power_controller = rf_power_controller
		self.main_loop_controller = self.rf_power_controller.main_loop_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rf_power_controller.getMessageTextField()
		messageTextField.setText("")
		cav_selected_inds = self.rf_power_controller.rf_power_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0):
			messageTextField.setText("Select one or more cavities to set the target powers!")	
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.main_loop_controller.cav_wrappers[cav_ind+4]
			cav_wrapper.net_pwr_goal = cav_wrapper.net_pwr_avg
		self.rf_power_controller.rf_power_table.getModel().fireTableDataChanged()		
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class RF_NET_Power_Controller:
	def __init__(self,top_document,main_loop_controller):
		#--- top_document is a parent document for all controllers
		self.top_document = top_document		
		self.main_loop_controller = main_loop_controller
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#----------------------------------------------
		left_panel = JPanel(BorderLayout())
		self.rf_power_table = JTable(RF_Power_Table_Model(self))
		self.rf_power_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.rf_power_table.setFillsViewportHeight(true)
		self.rf_power_table.setPreferredScrollableViewportSize(Dimension(800,240))
		scrl_rf_power_panel = JScrollPane(self.rf_power_table)
		scrl_rf_power_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"RF Net Power"))
		self.init_buttons_panel = Init_RF_Power_Controller_Panel(self)
		self.start_stop_panel = Start_Stop_Panel(self)
		tmp0_panel = JPanel(BorderLayout())
		tmp0_panel.add(self.init_buttons_panel,BorderLayout.NORTH)
		tmp0_panel.add(scrl_rf_power_panel,BorderLayout.CENTER)
		tmp0_panel.add(self.start_stop_panel,BorderLayout.SOUTH)
		tmp1_panel = JPanel(BorderLayout())
		tmp1_panel.add(tmp0_panel,BorderLayout.NORTH)
		left_panel.add(tmp1_panel,BorderLayout.WEST)
		#--------------------------------------------------------
		self.main_panel.add(left_panel,BorderLayout.WEST)
		#---- non GUI controllers
		self.loop_run_state = Loop_Run_State()
		
	def getMainPanel(self):
		return self.main_panel
		
	def getMessageTextField(self):
		return self.top_document.getMessageTextField()	
		
	def writeDataToXML(self,root_da):
		rf_power_cntrl_da = root_da.createChild("RF_NET_POWER")			
			
	def readDataFromXML(self,root_da):		
		rf_power_cntrl_da = root_da.childAdaptor("MAIN_CONTROLLER")


