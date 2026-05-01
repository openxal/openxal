# The SCL Cavity Failure Test
# Necessary PVs:
#---------Tuning motor---------- full detuning +200.
# SCL_HPRF:Tun08b:Mot        tuning motor position set PV
# SCL_HPRF:Tun08b:Mot.RBV    tuning motor position readback PV
#
#-------- Cavity OFF PV
# SCL_LLRF:Cav08b:OK
#
#--------- Beam On/Off
#ICS_Tim:Gate_BeamOn:SSMode
#ICS_Tim:Gate_BeamOn:Switch
#
#To return beam back we have to just BYPASS the faulty cavity. The beam will be back by itself.
#The bypass PV is like this (for SCL cavity 03a): SCL_LLRF:HPM03a:FPAR_LDmp_swmask_set
#SCL_LLRF:HPM03a:FPAR_LDmp_swmask_set = 0 means bypass is off= NORMAL
#SCL_LLRF:HPM03a:FPAR_LDmp_swmask_set = 1 means bypass is on = MASKED

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
#           Auxiliary Cavity Failure Test classes and functions
#------------------------------------------------------------------------
class RunStateController:
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

class CavTuner_Runner(Runnable):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
	
	def run(self):
		cav_tuner_state_cntrl = self.scl_acvity_failure_ctrl.cav_tuner_state_cntrl
		diff_max = 2.0
		(tuner_val,tuner_val_rb) = self.scl_acvity_failure_ctrl.getTunerPV_Values()
		cav_tuner_state_cntrl.setIsRunning(True)
		cav_tuner_state_cntrl.setShouldStop(False)
		while(abs(tuner_val-tuner_val_rb) > diff_max):
			if(cav_tuner_state_cntrl.getIsRunning() and (not cav_tuner_state_cntrl.getShouldStop())):
				time.sleep(0.3)
				(tuner_val,tuner_val_rb) = self.scl_acvity_failure_ctrl.getTunerPV_Values()
				self.scl_acvity_failure_ctrl.top_buttons_panel.cav_detuning_readback_txt.setValue(tuner_val_rb)
			else:
				break
		cav_tuner_state_cntrl.setIsRunning(false)
		cav_tuner_state_cntrl.setShouldStop(false)
		return
		
class CavWatcher_Runner(Runnable):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
	
	def run(self):
		self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText("")
		watching_state_cntrl = self.scl_acvity_failure_ctrl.watching_state_cntrl
		watching_state_cntrl.setIsRunning(True)
		watching_state_cntrl.setShouldStop(False)
		time_step = 0.5
		state_ind = self.scl_acvity_failure_ctrl.pv_cav_state.getValInt()
		#print "debug initial state state_ind = ",state_ind
		time_start = time.time()
		while(state_ind == 0):
			time.sleep(time_step)
			state_ind = self.scl_acvity_failure_ctrl.pv_cav_state.getValInt()
			run_time_txt = "Watcher is running. Time[sec]: %5.1f "%(time.time() - time_start)
			self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText(run_time_txt)
			if(watching_state_cntrl.getShouldStop()):
				watching_state_cntrl.setIsRunning(False)
				watching_state_cntrl.setShouldStop(True)
				self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText("Not running.")
				return
		#-------------------------------------------------
		#print "debug start tuner runner ========="
		#--- start tuner runner
		tuner_target_val = self.scl_acvity_failure_ctrl.top_buttons_panel.cav_detuning_goal_txt.getValue()
		self.scl_acvity_failure_ctrl.pv_cav_tuner.putVal(tuner_target_val)		
		cav_tuner_runner = CavTuner_Runner(self.scl_acvity_failure_ctrl)
		thr = Thread(cav_tuner_runner)
		thr.start()
		#----------------------------------------
		#----- upload AFF WFs and new phases
		#print "debug ==== start new phases uploading ====="
		self.scl_acvity_failure_ctrl.setupNewPhases()
		#print "debug ==== start new LL WF uploading ====="
		self.scl_acvity_failure_ctrl.putNewWF_to_AFF()
		#print "debug ==== waiting for tuner  ====="
		#----------------------------------------
		cav_tuner_state_cntrl = self.scl_acvity_failure_ctrl.cav_tuner_state_cntrl
		while(cav_tuner_state_cntrl.getIsRunning() and (not watching_state_cntrl.getShouldStop())):
			run_time_txt = "Compensation started. Tuner is moving. Time[sec]: %5.1f "%(time.time() - time_start)
			self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText(run_time_txt)
			if(watching_state_cntrl.getShouldStop()):
				watching_state_cntrl.setIsRunning(False)
				watching_state_cntrl.setShouldStop(True)
				self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText("Not running.")
				return			
		#------ start the beam
		#self.scl_acvity_failure_ctrl.pv_beam_switch.setVal(1)
		#print "debug ========== now restore the beam ==================="
		self.scl_acvity_failure_ctrl.startBeamAgain()
		run_time_txt = "Compensation took [sec]: %5.1f "%(time.time() - time_start)
		self.scl_acvity_failure_ctrl.top_buttons_panel.watcher_state_txt.setText(run_time_txt)
		#---------------------------------------------
		watching_state_cntrl.setIsRunning(false)
		watching_state_cntrl.setShouldStop(false)
		return	
	
class Cavity_AFF_Controller:
	def __init__(self,cav_wrapper,scl_acvity_failure_ctrl):
		self.cav_wrapper = cav_wrapper
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller		
		cav_short_name = self.cav_wrapper.alias.replace("Cav","")
		self.pv_get_aff_i_wf = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":AFF_Acc_I")
		self.pv_get_aff_q_wf = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":AFF_Acc_Q")
		self.pv_set_aff_i_wf = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":AFF_Set_I")
		self.pv_set_aff_q_wf = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":AFF_Set_Q")
		#------------------------------
		self.pv_aff_mode =  ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":AFF_Mode")
		self.pv_aff_pulse_width = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":CtlRFPW")
		self.pv_aff_usec_per_step = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+cav_short_name+":Wf_Dt")
		#------------------------------
		pv_arr = [self.pv_get_aff_i_wf,self.pv_get_aff_q_wf,self.pv_set_aff_i_wf,self.pv_set_aff_q_wf]
		pv_arr += [self.pv_aff_mode,self.pv_aff_pulse_width,self.pv_aff_usec_per_step]
		self.bad_cntrl = False
		for pv in pv_arr:
			if(not pv.connectAndWait(0.5)):
				self.scl_long_tuneup_controller.getMessageTextField().setText("Cannot connect pv="+pv.channelName())
				self.bad_cntrl = True
		#-------------------------------------------------------------------
		GRAPH_LEGEND_KEY = FunctionGraphsJPanel().getLegendKeyString()
		self.gd_i = BasicGraphData()
		self.gd_q = BasicGraphData()
		self.gd_i.setLineThick(3)
		self.gd_q.setLineThick(3)
		self.gd_i.setGraphColor(Color.BLUE)
		self.gd_q.setGraphColor(Color.RED)
		self.gd_i.setGraphProperty(GRAPH_LEGEND_KEY,"Cav= SCL"+cav_short_name+" I_WF")
		self.gd_q.setGraphProperty(GRAPH_LEGEND_KEY,"Cav= SCL"+cav_short_name+" Q_WF")
		#----------------------
		self.aff_mode = 0
		self.aff_pulse_width = 0.
		self.aff_usec_per_step = 0.
		#----------------------
		self.readAFF_Data()

	def isGood(self):
		return (not self.bad_cntrl)
		
	def readAFF_Data(self):
		self.gd_i.removeAllPoints()
		self.gd_q.removeAllPoints()
		self.wf_i_arr = self.pv_get_aff_i_wf.getArrDbl()
		self.wf_q_arr = self.pv_get_aff_q_wf.getArrDbl()
		x_arr = []
		for ind in range(len(self.wf_i_arr)):
			x_arr.append(1.0*ind)
		self.gd_i.addPoint(x_arr,self.wf_i_arr)
		x_arr = []
		for ind in range(len(self.wf_q_arr)):
			x_arr.append(1.0*ind)		
		self.gd_q.addPoint(x_arr,self.wf_q_arr)
		#print "debug pv=",self.pv_get_aff_i_wf.channelName()," arr=",self.wf_i_arr
		#print "debug pv=",self.pv_get_aff_q_wf.channelName()," arr=",self.wf_q_arr
		#-------------------------------------------------------------------
		self.aff_mode = self.pv_aff_mode.getValInt()
		self.aff_pulse_width = self.pv_aff_pulse_width.getValDbl()
		self.aff_usec_per_step = self.pv_aff_usec_per_step.getValDbl()	
			
	def getGraphData(self):
		return (self.gd_i,self.gd_q)
		
	def putNewWF_to_AFF(self):
		self.pv_set_aff_i_wf.putVal(self.wf_i_arr)
		self.pv_set_aff_q_wf.putVal(self.wf_q_arr)
		#---- Setting AFF mode to 'Freeze'
		self.pv_aff_mode.putVal(self.aff_mode)
		self.pv_aff_pulse_width.putVal(self.aff_pulse_width)
		#self.pv_aff_usec_per_step.putVal(self.aff_usec_per_step)
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class Cavities_List_JPanel(JPanel):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#--------------------------------------------------------
		self.cav_to_watch_list = JList(Cavities_ListModel(self.scl_acvity_failure_ctrl))
		self.cav_to_watch_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cav_to_watch_list.setLayoutOrientation(JList.VERTICAL)
		self.cav_to_watch_list.setSelectionForeground(Color.red)
		cav_to_watch_scrollPane = JScrollPane(self.cav_to_watch_list)
		self.cav_corrector_list = JList(Cavities_ListModel(self.scl_acvity_failure_ctrl))
		self.cav_corrector_list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION )
		self.cav_corrector_list.setLayoutOrientation(JList.VERTICAL)
		self.cav_corrector_list.setSelectionForeground(Color.red)
		cav_corrector_scrollPane = JScrollPane(self.cav_corrector_list)
		#--------------------------------------------------------		
		cav_to_watch_label = JLabel("    Cavity to Watch    ",JLabel.CENTER)
		cav_corrector_label = JLabel("    Corrector Cavity    ",JLabel.CENTER)
		cavs_lists_panel_center = JPanel(GridLayout(1,2,2,2))
		cavs_lists_panel_top = JPanel(GridLayout(1,2,2,2))
		cavs_lists_panel_top.add(cav_to_watch_label)
		cavs_lists_panel_top.add(cav_corrector_label)
		cavs_lists_panel_center.add(cav_to_watch_scrollPane)
		cavs_lists_panel_center.add(cav_corrector_scrollPane)
		cavs_lists_panel = JPanel(BorderLayout())
		cavs_lists_panel.add(cavs_lists_panel_top,BorderLayout.NORTH)
		cavs_lists_panel.add(cavs_lists_panel_center,BorderLayout.CENTER)
		cavs_lists_panel.setBorder(etched_border)	
		tmp1_panel = JPanel(BorderLayout())
		tmp1_panel.add(cavs_lists_panel,BorderLayout.NORTH)
		self.add(tmp1_panel,BorderLayout.WEST)

class Top_Buttons_JPanel(JPanel):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller		
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#--------------------------------------------------------
		initialize_button = JButton("INIT Watcher")
		initialize_button.addActionListener(Init_Button_Listener(self.scl_acvity_failure_ctrl))
		cav_to_watch_label = JLabel("   Cavity to Watch:",JLabel.RIGHT)
		self.cav_to_watch_text = JTextField(10)
		cav_corrector_label = JLabel("   Corrector Cavity:",JLabel.RIGHT)
		self.cav_corrector_text = JTextField(30)
		tmp1_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		tmp1_panel.add(initialize_button)
		tmp1_panel.add(cav_to_watch_label)
		tmp1_panel.add(self.cav_to_watch_text)
		tmp1_panel.add(cav_corrector_label)
		tmp1_panel.add(self.cav_corrector_text)
		#------------------------------------------------------		
		self.read_cav_aff_waveform_button = JButton("READ AFF WF for CORRECTOR CAVs")
		self.read_cav_aff_waveform_button.addActionListener(Read_AFF_Data_Button_Listener(self.scl_acvity_failure_ctrl))
		self.write_cav_aff_waveform_button = JButton("Upload AFF WF")
		self.write_cav_aff_waveform_button.addActionListener(Upload_AFF_Data_Button_Listener(self.scl_acvity_failure_ctrl))
		tmp2_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		tmp2_panel.add(self.read_cav_aff_waveform_button)
		tmp2_panel.add(self.write_cav_aff_waveform_button)
		#------------------------------------------------------	
		cav_detuning_goal_label = JLabel("Detuning: Goal:",JLabel.RIGHT)
		self.cav_detuning_goal_txt = DoubleInputTextField(200.0,DecimalFormat("###.#"),8)
		cav_detuning_readback_label = JLabel("   Readback:",JLabel.RIGHT)
		self.cav_detuning_readback_txt = DoubleInputTextField(0.0,DecimalFormat("###.#"),8)
		start_detuning_button = JButton("Start Detuning")
		stop_detuning_button = JButton("Stop Detuning")
		start_detuning_button.addActionListener(Start_Detuning_Button_Listener(self.scl_acvity_failure_ctrl))
		stop_detuning_button.addActionListener(Stop_Detuning_Button_Listener(self.scl_acvity_failure_ctrl))
		tmp3_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		tmp3_panel.add(cav_detuning_goal_label)
		tmp3_panel.add(self.cav_detuning_goal_txt)
		tmp3_panel.add(cav_detuning_readback_label)
		tmp3_panel.add(self.cav_detuning_readback_txt)
		tmp3_panel.add(start_detuning_button)
		tmp3_panel.add(stop_detuning_button)
		#------------------------------------------------------
		start_watcher_button = JButton("START Watcher")
		stop_watcher_button = JButton("STOP Watcher")
		start_watcher_button.addActionListener(Start_Watcher_Button_Listener(self.scl_acvity_failure_ctrl))
		stop_watcher_button.addActionListener(Stop_Watcher_Button_Listener(self.scl_acvity_failure_ctrl))
		self.watcher_state_txt = JTextField(40)
		self.watcher_state_txt.setText("Not running.")
		self.watcher_state_txt.setForeground(Color.red)
		tmp4_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		tmp4_panel.add(start_watcher_button)
		tmp4_panel.add(stop_watcher_button)
		tmp4_panel.add(self.watcher_state_txt)
		#--------------------------------------------
		self.setLayout(GridLayout(4,2,2,2))
		self.add(tmp1_panel)
		self.add(tmp2_panel)
		self.add(tmp3_panel)
		self.add(tmp4_panel)
		self.setBorder(etched_border)
		
	def setTunerRbTXT(self,val_rb):
		self.cav_detuning_readback_txt.setValue(val_rb)

class WFs_Graph_JPanel(JPanel):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller		
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#--------------------------------------------------------
		self.graph_panel = FunctionGraphsJPanel()
		self.graph_panel.setLegendButtonVisible(true)
		self.graph_panel.setChooseModeButtonVisible(true)
		self.graph_panel.setName("AFF WaveForms")
		self.graph_panel.setAxisNames("Index","WaveForm Value")	
		self.graph_panel.setBorder(etched_border)
		#-----------------------------------------
		self.setLayout(BorderLayout())
		self.add(self.graph_panel,BorderLayout.CENTER)
		
	def updateGraphs(self):
		self.graph_panel.removeAllGraphData()
		for cav_aff_cntrl in self.scl_acvity_failure_ctrl.cav_aff_cntrl_arr:
			(gd_i,gd_q) = cav_aff_cntrl.getGraphData()
			self.graph_panel.addGraphData(gd_i)
			self.graph_panel.addGraphData(gd_q)

#------------------------------------------------
#  JTable and JList models
#------------------------------------------------
class Cavities_ListModel(AbstractListModel):
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def getElementAt(self,index):
		return self.scl_long_tuneup_controller.cav_wrappers[index].alias
		
	def getSize(self):
		return len(self.scl_long_tuneup_controller.cav_wrappers)
	
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Init_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		cav_to_watch_list = self.scl_acvity_failure_ctrl.cavities_list_panel.cav_to_watch_list
		cav_corrector_list = self.scl_acvity_failure_ctrl.cavities_list_panel.cav_corrector_list
		watch_cav_index = cav_to_watch_list.getSelectedIndex()
		corrector_cav_index_arr = cav_corrector_list.getSelectedIndices()
		cav_to_watch_text = self.scl_acvity_failure_ctrl.top_buttons_panel.cav_to_watch_text
		cav_corrector_text = self.scl_acvity_failure_ctrl.top_buttons_panel.cav_corrector_text
		if(watch_cav_index < 0 or len(corrector_cav_index_arr) <= 0):
			cav_to_watch_text.setText("")
			cav_corrector_text.setText("")
			messageTextField.setText("Error: Please select cavities: one for watching and another for correction!")
			return
		#-----------------------------------------------------------------------------------------------
		if(not self.scl_acvity_failure_ctrl.connectPVs(watch_cav_index,corrector_cav_index_arr)):
			return
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[watch_cav_index]
		cav_to_watch_text.setText(cav_wrapper.alias)
		self.scl_acvity_failure_ctrl.cav_new_phases_dict = {}
		txt = ""
		for index in corrector_cav_index_arr:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[index]
			cav = cav_wrapper.cav
			phase_tmp = cav.getCavPhaseSetPoint()
			if False and (cav.getId().find("23d") >= 0 or int(cav.getId()[10:12]) >= 31):
				phase_tmp = - phase_tmp
			self.scl_acvity_failure_ctrl.cav_new_phases_dict[cav_wrapper] = phase_tmp
			txt += cav_wrapper.alias + "  "
		cav_corrector_text.setText(txt)
		(tuner_val,tuner_val_rb) = self.scl_acvity_failure_ctrl.getTunerPV_Values()
		self.scl_acvity_failure_ctrl.top_buttons_panel.setTunerRbTXT(tuner_val_rb)

class Read_AFF_Data_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		self.scl_acvity_failure_ctrl.readAFF_Data()
		self.scl_acvity_failure_ctrl.wfs_graph_panel.updateGraphs()

class Upload_AFF_Data_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		self.scl_acvity_failure_ctrl.putNewWF_to_AFF()

class Start_Detuning_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		if(self.scl_acvity_failure_ctrl.pv_cav_tuner == None):
			messageTextField.setText("No cavity for tuning! Start INIT procedure!")
			return
		(val,val_rb) = self.scl_acvity_failure_ctrl.getTunerPV_Values()
		tuner_target_val = self.scl_acvity_failure_ctrl.top_buttons_panel.cav_detuning_goal_txt.getValue()
		self.scl_acvity_failure_ctrl.pv_cav_tuner.putVal(tuner_target_val)
		cav_tuner_runner = CavTuner_Runner(self.scl_acvity_failure_ctrl)
		thr = Thread(cav_tuner_runner)
		thr.start()
		
class Stop_Detuning_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		self.scl_acvity_failure_ctrl.cav_tuner_state_cntrl.setShouldStop(True)

class Start_Watcher_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		if(self.scl_acvity_failure_ctrl.pv_cav_tuner == None):
			messageTextField.setText("Nothing to watch! Start INIT procedure!")
			return
		watcher_runner = CavWatcher_Runner(self.scl_acvity_failure_ctrl)
		thr = Thread(watcher_runner)
		thr.start()		

class Stop_Watcher_Button_Listener(ActionListener):
	#This button will initialize the watcher parameters
	def __init__(self,scl_acvity_failure_ctrl):
		self.scl_acvity_failure_ctrl = scl_acvity_failure_ctrl
		self.scl_long_tuneup_controller = self.scl_acvity_failure_ctrl.scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		self.scl_acvity_failure_ctrl.watching_state_cntrl.setShouldStop(True)
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Cavity_Failure_Test_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		#------ JLists for cavity to watch and correction cavity selection 
		self.cavities_list_panel = Cavities_List_JPanel(self)
		self.top_buttons_panel = Top_Buttons_JPanel(self)
		top_panel.add(self.cavities_list_panel,BorderLayout.WEST)
		top_panel.add(self.top_buttons_panel,BorderLayout.CENTER)
		#-----the top-right buttons panel
		#------table panel --------
		center_panel = JPanel(BorderLayout())
		self.wfs_graph_panel = WFs_Graph_JPanel(self)
		center_panel.add(self.wfs_graph_panel,BorderLayout.CENTER)
		#-------- bottom actions panel
		bottom_panel = JPanel(BorderLayout())
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		#--------------------------------------------------
		self.cav_tuner_state_cntrl = RunStateController()
		self.watching_state_cntrl = RunStateController()
		#--------------------------------------------------
		self.cav_wrapper_watch = None
		self.cav_aff_cntrl_arr = []
		#--------------------------------------------------
		self.pv_beam_switch = None
		self.pv_cav_tuner = None
		self.pv_cav_tuner_rb = None
		self.pv_cav_state = None
		#---------------------------------------------------
		#---- self.cav_new_phases_dict[cav_wrapper] = phase
		self.cav_new_phases_dict = {}
				
	def cleanAllPVs(self):
		self.pv_beam_switch = None
		self.cav_wrapper_watch = None
		self.cav_aff_cntrl_arr = []
		self.pv_cav_tuner = None
		self.pv_cav_tuner_rb = None	
		self.pv_cav_state = None
		#---- self.cav_new_phases_dict[cav_wrapper] = phase
		self.cav_new_phases_dict = {}		
				
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		#self.???table.getModel().fireTableDataChanged()
		pass
	
	def getTunerPV_Values(self):
		val = self.pv_cav_tuner.getValDbl()
		val_rb = self.pv_cav_tuner_rb.getValDbl()
		return (val,val_rb)
	
	def readAFF_Data(self):
		for cav_aff_cntrl in self.cav_aff_cntrl_arr:
			cav_aff_cntrl.readAFF_Data()		
	
	def putNewWF_to_AFF(self):
		for cav_aff_cntrl in self.cav_aff_cntrl_arr:
			cav_aff_cntrl.putNewWF_to_AFF()
	
	def connectPVs(self,watch_cav_index,corrector_cav_index_arr):
		pv_arr = []
		self.cav_wrapper_watch = self.scl_long_tuneup_controller.cav_wrappers[watch_cav_index]
		self.cav_wrapper_corrector_arr = []
		self.cav_aff_cntrl_arr = []
		for index in corrector_cav_index_arr:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[index]
			cav_aff_cntrl = Cavity_AFF_Controller(cav_wrapper,self)
			self.cav_aff_cntrl_arr.append(cav_aff_cntrl )
			if(not cav_aff_cntrl.isGood()):
				self.cleanAllPVs()
				return false
		#--------------------------------------------------------------------------
		self.cav_new_phases_dict = {}
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
			if(cav_wrapper.isGood):
				self.cav_new_phases_dict[cav_wrapper] = cav_wrapper.rescaleBacket.livePhase
		#--------------------------------------------------------------------------
		cav_short_name = self.cav_wrapper_watch.alias.replace("Cav","")
		self.pv_cav_tuner = ChannelFactory.defaultFactory().getChannel("SCL_HPRF:Tun"+cav_short_name+":Mot")
		self.pv_cav_tuner_rb = ChannelFactory.defaultFactory().getChannel("SCL_HPRF:Tun"+cav_short_name+":Mot.RBV")
		pv_arr.append(self.pv_cav_tuner)
		pv_arr.append(self.pv_cav_tuner_rb)
		self.pv_cav_state = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:Cav"+cav_short_name+":OK")
		pv_arr.append(self.pv_cav_state)
		self.pv_beam_switch = ChannelFactory.defaultFactory().getChannel("ICS_Tim:Gate_BeamOn:Switch")
		#pv_arr.append(self.pv_beam_switch)
		#---------------------------------------------------------------------------
		for pv in pv_arr:
			if(not pv.connectAndWait(1.5)):
				self.scl_long_tuneup_controller.getMessageTextField().setText("Cannot connect pv="+pv.channelName())
				self.cleanAllPVs()
				return false
		self.wfs_graph_panel.updateGraphs()
		return true
		
	def setupNewPhases(self):
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
			if(cav_wrapper.isGood and self.cav_new_phases_dict.has_key(cav_wrapper)):
				new_cav_phase = self.cav_new_phases_dict[cav_wrapper]
				new_cav_phase = makePhaseNear(new_cav_phase,0.)
				cav = cav_wrapper.cav
				phase_tmp = new_cav_phase
				if False and (cav.getId().find("23d") >= 0 or int(cav.getId()[10:12]) >= 31):
					phase_tmp = - phase_tmp
				cav_wrapper.cav.setCavPhase(phase_tmp)
				#print "debug cav=",cav_wrapper.alias," new phase",new_cav_phase
				
				
	def startBeamAgain(self):
		#start the beam again
		#self.pv_beam_switch.setVal(1)
		self.cav_wrapper_watch.setBypassState(1)
				
