# The abstarct cavity controller - parent class for each cavity controller (MEBT,DTL,CCL)

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from java.awt import BorderLayout, GridLayout, FlowLayout
from java.awt import Color
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener

from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Abstarct Cavity Controller
#------------------------------------------------------------------------
class Abstract_Cavity_Controller:
	def __init__(self,main_loop_controller,cav_wrapper):
		self.main_loop_controller = main_loop_controller
		self.cav_wrapper = cav_wrapper
		self.main_panel = JPanel(BorderLayout())
		self.parameters_panel = JPanel(BorderLayout())
		#---- backward cavity amplitude move params
		panel0 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		cav_amp_backward_steps_mult_label = JLabel("N cav. amp. backward steps multiplier =",JLabel.RIGHT)
		self.cav_amp_backward_steps_mult_text =  DoubleInputTextField(1.0,FortranNumberFormat("G3.1"),5)
		panel0.add(cav_amp_backward_steps_mult_label)
		panel0.add(self.cav_amp_backward_steps_mult_text)
		#---- cavity's wait time multiplier
		panel1 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		cav_amp_wait_time_mult_label = JLabel("Cav. amp. time wait multiplier =",JLabel.RIGHT)
		self.cav_amp_wait_time_mult_text =  DoubleInputTextField(3.0,FortranNumberFormat("G3.1"),5)
		panel1.add(cav_amp_wait_time_mult_label)
		panel1.add(self.cav_amp_wait_time_mult_text)
		#---- cavity's safe amplitude up and down limits		
		panel2 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		safe_relative_amp_up_label = JLabel("Cav. amp. safe Up [%]=",JLabel.RIGHT)
		self.safe_relative_amp_up_text = DoubleInputTextField(7.0,FortranNumberFormat("G3.1"),5)
		panel2.add(safe_relative_amp_up_label)
		panel2.add(self.safe_relative_amp_up_text)
		panel3 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		safe_relative_amp_down_label = JLabel("Cav. amp. safe Down [%]=",JLabel.RIGHT)
		self.safe_relative_amp_down_text = DoubleInputTextField(7.0,FortranNumberFormat("G3.1"),5)
		panel3.add(safe_relative_amp_down_label)
		panel3.add(self.safe_relative_amp_down_text)
		#----- cavity's guess phase[deg] and amplitude[%] corrections after 360 deg full scan for inner BPMs
		panel4 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		guess_phase_shift_label = JLabel("Cav. Guess Phase Shift after Full Scan [deg]=",JLabel.RIGHT)
		self.guess_phase_shift_text =  DoubleInputTextField(0.0,FortranNumberFormat("F3.1"),5)
		panel4.add(guess_phase_shift_label)
		panel4.add(self.guess_phase_shift_text)
		panel5 = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		guess_cav_amp_shift_label = JLabel("Cav. Amp Shift after Full Scan [%]=",JLabel.RIGHT)
		self.guess_cav_amp_shift_text =  DoubleInputTextField(0.0,FortranNumberFormat("F3.1"),5)
		panel5.add(guess_cav_amp_shift_label)
		panel5.add(self.guess_cav_amp_shift_text)		
		#-----------------------------------------------
		params_panel = JPanel(GridLayout(6,1,1,1))
		params_panel.add(panel0)
		params_panel.add(panel1)
		params_panel.add(panel2)
		params_panel.add(panel3)
		params_panel.add(panel4)
		params_panel.add(panel5)
		#------------------------------------------------
		self.parameters_panel.add(params_panel,BorderLayout.NORTH)
		#------------------------------------------------
		self.scan_progress_bar = Scan_Progress_Bar(self.main_loop_controller,self)
		#------------------------------------------------
		cav_wrapper.safe_relative_amp_up_text = self.safe_relative_amp_up_text
		cav_wrapper.safe_relative_amp_down_text = self.safe_relative_amp_down_text

	def getMainPanel(self):
		return self.main_panel	
		
	def getParamsPanel(self):
		return self.parameters_panel		
		
	def getScanProgressBarPanel(self):
		return self.scan_progress_bar.scan_progress_panel
		
	def getScanProgressBar(self):
		return self.scan_progress_bar
	
	def runSetUpAlgorithm(self):
		""" Returns (true, text) in the case of the success """
		text = "Good. Cav="+self.cav_wrapper.alias
		return (true,text)
		
	def init(self):
		""" reads the pv values """ 
		self.cav_wrapper.init()
		self.scan_progress_bar.init()
		
	def writeDataToXML(self,root_da):
		""" Abstract method. This method should be implemented in the subclass """
		cav_cntrl_data_da = root_da.createChild("CAVITY_CONTROLLER_"+self.cav_wrapper.alias)
		
	def readDataFromXML(self,cav_cntrl_data_da):
		""" Abstract method. This method should be implemented in the subclass """
		pass
	
	def checkBPM_Usage(self,bpm_wrapper):
		""" Abstract method. Returns True or False about this controller usage of the BPM """
		return false
	
	def timeSleep(self, time_sleep):
		n_parts = 5
		tm = time_sleep/n_parts
		for ind in range(n_parts):
			time.sleep(tm)
			self.scan_progress_bar.count_and_update(tm)
			if(self.main_loop_controller.loop_run_state.shouldStop):
				return
			
	def setMaxTimeCount(self):
		""" 
		Abstract method. It should be implemented in each subclass.
		It returns the maximal time count.
		"""
		print "Debug. Abstract_Cavity_Controller.setMaxTimeCount() This method should be implemented in a subclass."
		self.scan_progress_bar.setMaxTimeCount(100.)
		return self.scan_progress_bar.count_max
		
	def getPastaFittingTime(self):
		""" Abstract method. It should be implemented in each subclass. """
		return 0.
		
	def initProgressBar(self):
		self.scan_progress_bar.init()
			
	def wrtiteAbstractCntrlToXML(self,root_da):
		abstr_cntrl_data_da = root_da.createChild("ABSTRACT_CAVITY_CONTROLLER_PARAMS")	
		abstr_cntrl_data_da.setValue("amp_steps_mult",self.cav_amp_backward_steps_mult_text.getValue())
		abstr_cntrl_data_da.setValue("amp_time_mult",self.cav_amp_wait_time_mult_text.getValue())
		abstr_cntrl_data_da.setValue("amp_safe_up_percent",self.safe_relative_amp_up_text.getValue())
		abstr_cntrl_data_da.setValue("amp_safe_down_percent",self.safe_relative_amp_down_text.getValue())
		abstr_cntrl_data_da.setValue("guess_phase_shift",self.guess_phase_shift_text.getValue())
		abstr_cntrl_data_da.setValue("guess_amp_shift_percent",self.guess_cav_amp_shift_text.getValue())
		
	def readAbstractCntrlFromXML(self,root_da):
		abstr_cntrl_data_da = root_da.childAdaptor("ABSTRACT_CAVITY_CONTROLLER_PARAMS")
		self.cav_amp_backward_steps_mult_text.setValue(abstr_cntrl_data_da.doubleValue("amp_steps_mult"))
		self.cav_amp_wait_time_mult_text.setValue(abstr_cntrl_data_da.doubleValue("amp_time_mult"))
		if(abstr_cntrl_data_da.hasAttribute("amp_safe_up_percent")):
			self.safe_relative_amp_up_text.setValue(abstr_cntrl_data_da.doubleValue("amp_safe_up_percent"))
		if(abstr_cntrl_data_da.hasAttribute("amp_safe_down_percent")):
			self.safe_relative_amp_down_text.setValue(abstr_cntrl_data_da.doubleValue("amp_safe_down_percent"))
		if(abstr_cntrl_data_da.hasAttribute("guess_phase_shift")):
			self.guess_phase_shift_text.setValue(abstr_cntrl_data_da.doubleValue("guess_phase_shift"))
		if(abstr_cntrl_data_da.hasAttribute("guess_amp_shift_percent")):
			self.guess_cav_amp_shift_text.setValue(abstr_cntrl_data_da.doubleValue("guess_amp_shift_percent"))
			
class Scan_Progress_Bar:
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		#----- scan progress bar panel
		scan_progress_label = JLabel("Scan Progress=",JLabel.RIGHT)
		self.scan_progressBar = JProgressBar(0,100)
		self.scan_progressBar.setStringPainted(true)
		panel2 = JPanel(BorderLayout())
		panel2.add(scan_progress_label,BorderLayout.WEST)
		panel2.add(self.scan_progressBar,BorderLayout.CENTER)
		etched_border = BorderFactory.createEtchedBorder()
		self.scan_progress_panel = JPanel(BorderLayout())
		self.scan_progress_panel.add(panel2,BorderLayout.NORTH)
		self.scan_progress_panel.setBorder(etched_border)
		#-------------------------------------------------
		self.count_max = 100.
		self.count = 0.
		
	def init(self):
		self.count = 0.
		self.update()
		
	def setMaxTimeCount(self,count_max):
		self.count_max = 1.0*count_max 
		
	def countEvent(self, val = 1.0):
		self.count += val
		
	def update(self):
		self.scan_progressBar.setValue(int(100.0*self.count/self.count_max))
		
	def count_and_update(self, val = 1.0):
		self.countEvent(val)
		self.update()
		
