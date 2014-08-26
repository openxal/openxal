#----------------------------------------------------------
# This is a beam trigger class
#----------------------------------------------------------

import time

from java.lang import *

from xal.extension.scan import WrappedChannel

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class BeamTrigger:
	"""
	It will trigger the beam once.
	"""
	def __init__(self,top_level_cotroller = null):
		self.top_level_cotroller = top_level_cotroller
		self.beamTriggerWPV = null
		self.testPV = null
		self.sleepMeasureTime = 0.1
		self.scan_state_controller = null
		self.fake_scan = false
		self.use_trigger = false
		
	def initChannels(self):
		if(not self.fake_scan):
			if(self.beamTriggerWPV == null or self.testPV == null):
				self.beamTriggerWPV = WrappedChannel("ICS_Tim:Gate_BeamOn:SSTrigger")
				self.testPV = WrappedChannel("MEBT_Diag:BPM01:amplitudeAvg")
				self.testPV.startMonitor()	
				time.sleep(2.0)
		
	def setScanStateController(self,scan_state_controller):
		self.scan_state_controller = scan_state_controller
		
	def setSleepTime(self, tm):
		self.sleepMeasureTime = tm
		
	def getSleepTime(self):
		return self.sleepMeasureTime
		
	def setFakeScan(self, bool_val):
		self.fake_scan = bool_val
		
	def getFakeScan(self):
		return self.fake_scan
		
	def setUseTrigger(self, bool_val):
		self.use_trigger = bool_val
		
	def getUseTrigger(self):
		return self.use_trigger
		
	def makeShot(self):
		self.initChannels()
		state_cntrl = self.scan_state_controller 
		messageTextField = null
		if(self.top_level_cotroller != null):
			messageTextField = self.top_level_cotroller.getMessageTextField()
		if(not self.fake_scan):
			self.testPV.setValueChanged(false) 
			time.sleep(0.02)
			if(self.use_trigger): self.beamTriggerWPV.setValue(1.0)
		if(state_cntrl != null and state_cntrl.getShouldStop() == true): return false
		time.sleep(self.sleepMeasureTime)
		if(state_cntrl != null and state_cntrl.getShouldStop() == true): return false		
		count = 0
		time_sleep = 0.05
		if(not self.fake_scan):
			while(self.testPV.valueChanged() == false):
				count = count + 1
				if(state_cntrl != null and state_cntrl.getShouldStop() == true): return false			
				time.sleep(time_sleep)
				if(state_cntrl != null and state_cntrl.getShouldStop() == true): return false			
				if(count % 25 == 0): 
					time_sleep = time_sleep + 3.0
				if(count > 25):
					if(messageTextField != null):
						messageTextField.setText("Attention!!! Something wrong. Please fire the beam manually! Bad count="+str(count))			
			if(messageTextField != null):
				messageTextField.setText("")
		return true

