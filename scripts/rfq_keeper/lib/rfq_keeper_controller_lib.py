# The initialization and keeper controller

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *

from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
from java.awt.event import ActionEvent, ActionListener

from xal.ca import ChannelFactory
from xal.extension.widgets.swing import DoubleInputTextField 

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

# pv MEBT_Diag:BCM11:currentTBT - array of avg currents
# pv ICS_Tim:Gate_BeamOn:Switch - beam enabled or not
# pv ICS_Tim:Gate_BeamOn:SSMode - continuous or not
# pv ICS_Tim:Gate_BeamOn:RR - reprate
# pv RFQ_LLRF:FCM1:CtlRFPW - RFQ pulse width
# 

#------------------------------------------------------------------------
#           Auxiliary SCAN classes and functions
#------------------------------------------------------------------------	
class PV_Data_Reader_Setter:
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		self.pv_current_tbt = ChannelFactory.defaultFactory().getChannel("MEBT_Diag:BCM11:currentTBT")
		self.pv_beamOn = ChannelFactory.defaultFactory().getChannel("ICS_Tim:Gate_BeamOn:Switch")
		self.pv_continuous = ChannelFactory.defaultFactory().getChannel("ICS_Tim:Gate_BeamOn:SSMode")
		self.pv_reprate = ChannelFactory.defaultFactory().getChannel("ICS_Tim:Gate_BeamOn:RR")
		self.pv_rfq_PW = ChannelFactory.defaultFactory().getChannel("RFQ_LLRF:FCM1:CtlRFPW")
		self.pv_current_tbt.connectAndWait(0.5)
		self.pv_beamOn.connectAndWait(0.5)
		self.pv_continuous.connectAndWait(0.5)
		self.pv_reprate.connectAndWait(0.5)
		self.pv_rfq_PW.connectAndWait(0.5)
		self.current_integral = 0.
		self.beam_on = 0
		self.continuous = 0
		self.reprate = 0.
		self.pw = 0.
		
	def readData(self):
		current_arr = self.pv_current_tbt.getArrDbl() 
		current_arr = current_arr[1:]
		self.current_integral = self.currArrAnalysis(current_arr)
		self.beam_on = self.pv_beamOn.getValInt()
		self.continuous = self.pv_continuous.getValInt()
		self.reprate = self.pv_reprate.getValDbl()
		self.pw = self.pv_rfq_PW.getValDbl()
		
	def currArrAnalysis(self,arr):
		max_val = current_arr[0]
		for val in current_arr:
			if(val > max_val):
				max_val = val
		threshold = max_val/2.
		ind_min = 0
		val_old = current_arr[0]
		for ind in range(1,len(current_arr)):
			val = current_arr[ind]
			if(threshold < val and threshold > val_old):
				ind_min = ind
				break
		val_old = current_arr[len(current_arr)-1]
		ind_max = len(current_arr)-1
		for ind in range(len(current_arr)-2,0,-1):
			val = current_arr[ind]
			if(threshold < val and threshold > val_old):
				ind_max = ind
				break			
		curr_integral = 0.
		if(ind_max > ind_min): 
			for ind in range(ind_min,ind_max):
				curr_integral += current_arr[ind]
		return curr_integral
		
	def setRFQ_PW(self,pw):
		""" ?????? """
		#self.pv_rfq_PW.putVal(pw)
		pass
		
	def getRFQ_PW(self):
		self.pw = self.pv_rfq_PW.getValDbl()
		return self.pw
					
class Keeper_Runner(Runnable):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
	
	def run(self):
		messageTextField = self.rfq_keeper_controller.rfq_keeper_document.getMessageTextField()
		statusTextField = self.rfq_keeper_controller.star_stop_keeper_panel.keeper_status_text
		statusTextField.setText("Running!")	
		rfq_pw_parameters_holder = self.rfq_keeper_controller.rfq_pw_parameters_holder
		pw_addition_old = 0.
		update_time = self.rfq_keeper_controller.init_keeper_panel.update_time_text.getValue()
		while(2 > 1):
			messageTextField.setText("")	
			try:
				if(self.rfq_keeper_controller.keeperShouldStop): return
				time.sleep(update_time)
				if(self.rfq_keeper_controller.keeperShouldStop): return
				self.rfq_keeper_controller.pv_data_reader_setter.readData()
				pw_addition_new = rfq_pw_parameters_holder.calculateAdditionPW()
				pw = self.rfq_keeper_controller.pv_data_reader_setter.pw - pw_addition_old + pw_addition_new
				self.rfq_keeper_controller.pv_data_reader_setter.setRFQ_PW(pw)
				pw_addition_old = pw_addition_new
				st = "Running!"+" extra PW= %5.1 "%pw_addition_new
				statusTextField.setText(st)
				#-----------debug section START
				print "debug current_integral =",self.rfq_keeper_controller.pv_data_reader_setter.current_integral
				print "debug beam_on =",self.rfq_keeper_controller.pv_data_reader_setter.beam_on
				print "debug continuous =",self.rfq_keeper_controller.pv_data_reader_setter.continuous
				print "debug reprate =",self.rfq_keeper_controller.pv_data_reader_setter.reprate
				print "debug pw =",self.rfq_keeper_controller.pv_data_reader_setter.pw
				#-----------debug section STOP
				if(self.rfq_keeper_controller.keeperShouldStop): return
			except:
				messageTextField.setText("Cannot read PV! Stop!")
				break
		statusTextField.setText("Not running!")
		self.rfq_keeper_controller.keeperShouldStop = false
		self.rfq_keeper_controller.keeperIsRuning = false
		try:
			pw = self.rfq_keeper_controller.pv_data_reader_setter.getRFQ_PW()
			pw -= pw_addition_old
			self.rfq_keeper_controller.pv_data_reader_setter.setRFQ_PW(pw)
		except:
			messageTextField.setText("Could not restore the RFQ Pulse Width! Try to subtract "+str(pw_addition_old)+" manually!")
		

class RFQ_PW_Parameter_Holder:
	def __init__(self,rfq_keeper_controller):
		self.pw_addition = 0.
		
	def calculateAdditionPW(self):
		pv_data_reader_setter = self.rfq_keeper_controller.pv_data_reader_setter
		current_integral = pv_data_reader_setter.current_integral
		beam_on = pv_data_reader_setter.beam_on
		beam_countinuous = pv_data_reader_setter.continuous
		rep_rate = pv_data_reader_setter.reprate	
		#------------------------------------------
		init_keeper_panel = self.rfq_keeper_controller.init_keeper_panel
		current_integral_init = init_keeper_panel.current_integral_text.getValue()
		rep_rate_init = init_keeper_panel.reprate_text.getValue()
		max_pw_addition = init_keeper_panel.rfq_max_pw_correction_text.getValue()
		if(current_integral_init != 0.):
			coeff = current_integral*rep_rate/(current_integral_init*rep_rate_init)
			self.pw_addition = max_pw_addition*(1.0-beam_on*beam_countinuous*coeff)
		else:
			self.pw_addition = 0.
		return self.pw_addition

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class StartStopKeeper_Panel(JPanel):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		#---------------------------------------------
		start_keeper_button = JButton("Start")
		start_keeper_button.addActionListener(Start_Keeper_Button_Listener(self.rfq_keeper_controller))
		stop_keeper_button = JButton("Stop")
		stop_keeper_button.addActionListener(Stop_Keeper_Button_Listener(self.rfq_keeper_controller))
		#---------------------------------------------
		buttons_panel.add(start_keeper_button)
		buttons_panel.add(stop_keeper_button)
		self.keeper_status_text = JTextField(20)
		self.keeper_status_text.setText("Scan status")
		self.keeper_status_text.setHorizontalAlignment(JTextField.LEFT)
		self.keeper_status_text.setForeground(Color.red)
		self.add(buttons_panel,BorderLayout.WEST)
		self.add(self.keeper_status_text,BorderLayout.CENTER)

class Init_Keeper_Panel(JPanel):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		#---------------------------------------------
		init_keeper_button = JButton("Initialize Keeper")
		init_keeper_button.addActionListener(Init_Keeper_Button_Listener(self.rfq_keeper_controller))
		#---------------------------------------------
		buttons_panel.add(init_keeper_button)
		#---------------------------------------------
		info_panel = JPanel(GridLayout(5,1))
		label_current_integral = JLabel("Current Integral=",JLabel.RIGHT)
		label_reprate = JLabel("Rep. rate[Hz]=",JLabel.RIGHT)
		label_rfq_pw = JLabel("RFQ Pulse Width=",JLabel.RIGHT)
		label_rfq_max_pw_correction = JLabel("Max PW Correction=",JLabel.RIGHT)
		label_update_time = JLabel("Update Time [sec]=",JLabel.RIGHT)
		#-------
		self.current_integral_text = DoubleInputTextField("G12.5")
		self.reprate_text = DoubleInputTextField("F4.1")
		self.rfq_pw_text = DoubleInputTextField("F6.1")
		self.rfq_max_pw_correction_text = DoubleInputTextField("F6.1")
		self.update_time_text = DoubleInputTextField("F6.1")
		#-------
		self.current_integral_text.setHorizontalAlignment(JTextField.CENTER)
		self.reprate_text.setHorizontalAlignment(JTextField.CENTER)
		self.rfq_pw_text.setHorizontalAlignment(JTextField.CENTER)
		self.rfq_max_pw_correction_text.setHorizontalAlignment(JTextField.CENTER)
		self.update_time_text.setHorizontalAlignment(JTextField.CENTER)
		#-------
		self.rfq_max_pw_correction_text.setValue(20.)
		self.update_time_text .setValue(1.0)
		#-------
		info_panel.add(label_current_integral)
		info_panel.add(self.current_integral_text)		
		info_panel.add(label_reprate)
		info_panel.add(self.reprate_text)		
		info_panel.add(label_rfq_pw)
		info_panel.add(self.rfq_pw_text)		
		info_panel.add(label_rfq_max_pw_correction)
		info_panel.add(self.rfq_max_pw_correction_text)
		info_panel.add(label_update_time)
		info_panel.add(self.update_time_text)
		#----------------------------------------------
		inner_0_panel = JPanel(BorderLayout())
		inner_0_panel.add(buttons_panel,BorderLayout.NORTH)
		inner_0_panel.add(info_panel,BorderLayout.CENTER)
		self.add(inner_0_panel,BorderLayout.NORTH)

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Init_Keeper_Button_Listener(ActionListener):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.rfq_keeper_controller.rfq_keeper_document.getMessageTextField()
		messageTextField.setText("")
		init_keeper_panel = self.rfq_keeper_controller.init_keeper_panel
		try:
			pv_data_reader_setter = self.rfq_keeper_controller.pv_data_reader_setter
			pv_data_reader_setter.readData()
			print "debug current_integral =",pv_data_reader_setter.current_integral
			print "debug beam_on =",pv_data_reader_setter.beam_on
			print "debug continuous =",pv_data_reader_setter.continuous
			print "debug reprate =",pv_data_reader_setter.reprate
			print "debug pw =",pv_data_reader_setter.pw
			init_keeper_panel.current_integral_text.setValue(pv_data_reader_setter.current_integral)
			init_keeper_panel.reprate_text.setValue(pv_data_reader_setter.reprate)
			init_keeper_panel.rfq_pw_text.setValue(pv_data_reader_setter.pw)
		except:
			messageTextField.setText("Cannot read PV to initilize the RFQ Keeper!")


class Start_Keeper_Button_Listener(ActionListener):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		
	def actionPerformed(self,actionEvent):
		if(self.rfq_keeper_controller.keeperIsRuning == true): return
		self.rfq_keeper_controller.keeperShouldStop = false
		runner = Keeper_Runner(self.rfq_keeper_controller)
		thr = Thread(runner)
		thr.start()			

class Stop_Keeper_Button_Listener(ActionListener):
	def __init__(self,rfq_keeper_controller):
		self.rfq_keeper_controller = rfq_keeper_controller
		
	def actionPerformed(self,actionEvent):
		self.rfq_keeper_controller.keeperShouldStop = true
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class RFQ_Keeper_Controller:
	def __init__(self,rfq_keeper_document):
		#--- RFQ_Keeper_Controller the main cotroller
		self.rfq_keeper_document = rfq_keeper_document
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()	
		#---- functionality
		self.pv_data_reader_setter = PV_Data_Reader_Setter(self)
		self.keeperShouldStop = true
		self.keeperIsRuning = false
		self.star_stop_keeper_panel = StartStopKeeper_Panel(self)
		self.init_keeper_panel = Init_Keeper_Panel(self)
		self.rfq_pw_parameters_holder = RFQ_PW_Parameter_Holder(self)
		#------top params panel-----------------------
		left_top_panel = JPanel(BorderLayout())
		left_top_panel.add(self.init_keeper_panel,BorderLayout.NORTH)
		left_top_panel.add(self.star_stop_keeper_panel,BorderLayout.CENTER)
		#--------------------------------------------------
		left_panel = JPanel(BorderLayout())
		left_panel.add(left_top_panel,BorderLayout.NORTH)
		self.main_panel.add(left_panel,BorderLayout.WEST)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		pass
		
	def clean(self):
		pass

	def writeDataToXML(self,root_da):
		pass
				
	def readDataFromXML(self,root_da):
		pass

