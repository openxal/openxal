#
# This script scan the surface of the target with the beam  
# to calibrate the TIS
#

import sys
import math
import types
import time

from jarray import *
from xjava.lang import *
from xjava.util import *
from xjava.io import *
from xjava.swing import *
from java.awt import *
from org.xml.sax import *
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener

from xal.extension.scan import WrappedChannel
from datetime import datetime

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#window closer will kill this apps 
class WindowCloser(WindowAdapter):
	def windowClosing(self,windowEvent):
		sys.exit(1)

class Trigger:
	def __init__(self):
		self.beamTriggerWPV = WrappedChannel("ICS_Tim:Gate_BeamOn:SSTrigger")
		self.testPV = WrappedChannel("Target_Diag:TIS:xMean")
		self.testPV.startMonitor()
		self.sleepMeasureTime = 1.5
		self.run = 1

	def makeShot(self):
		#return
		self.testPV.setValueChanged(false)       
		self.beamTriggerWPV.setValue(1.0)
		time.sleep(self.sleepMeasureTime)
		count = 0
		time_init = self.sleepMeasureTime
		while(self.testPV.valueChanged() == false):
			count = count + 1
			time_init = time_init + 1.
			time.sleep(time_init)
			print "Attention!!! Something wrong. count=", count
			
	def setRun(self, runVar):
		self.run = runVar
			
	def wait(self):
		while(self.run == 0):
			time.sleep(self.sleepMeasureTime)

class StopActionListener(ActionListener):
	def __init__(self,trigger):
		self.trigger = trigger
	
	def actionPerformed(self,e):
		self.trigger.setRun(0)

class StartActionListener(ActionListener):
	def __init__(self,trigger):
		self.trigger = trigger
	
	def actionPerformed(self,e):
		self.trigger.setRun(1)

class ExitActionListener(ActionListener):
	def __init__(self):
		pass
	
	def actionPerformed(self,e):
		sys.exit(1)
		
#-------------------------------------
# Classes for the Dipole Corr. scans
#-------------------------------------
class CorrDict:
	def __init__(self,ch_arr,curr_dict = {}):
		# ch_arr has WrappedChannels for current in the correctors
		self.ch_arr = ch_arr
		self.curr_dict = {}
		if(len(curr_dict) == 0):
			for ch in self.ch_arr:
				self.curr_dict[ch] = ch.getValue()
		else:
			for ch in self.ch_arr:
				if(curr_dict.has_key(ch)):
					self.curr_dict[ch] = curr_dict[ch]
			
	def setValue(self,ch,val):
		self.curr_dict[ch] = val
		
	def setDict(self, curr_dict):
		for ch in self.ch_arr:
			if(curr_dict.has_key(ch)):
				self.curr_dict[ch] = curr_dict[ch]
		
	def getValue(self,ch):
		return self.curr_dict[ch]
		
	def setToAccelerator(self):
		for ch in self.ch_arr:
			ch.setValue(self.curr_dict[ch])
			
	def printChs(self):
		for ch in self.ch_arr:
			print ch.getChannelName()," val_rb= %12.5g "%ch.getValue(),"    val_set= %12.5g "%self.curr_dict[ch]
	
class TIS_Data:
	def __init__(self, outF):
		self.outF = outF
		self.ch_arr = []
		ch = WrappedChannel("Target_Diag:TIS:xMean")
		self.ch_arr.append(ch)
		ch = WrappedChannel("Target_Diag:TIS:yMean")
		self.ch_arr.append(ch)
		ch = WrappedChannel("Target_Diag:TIS:xAmp")
		self.ch_arr.append(ch)
		ch = WrappedChannel("Target_Diag:TIS:yAmp")
		self.ch_arr.append(ch)
		ch = WrappedChannel("Target_Diag:TIS:imgPD")
		self.ch_arr.append(ch)
		ch = WrappedChannel("Target_Diag:TIS:fitPD")
		self.ch_arr.append(ch)
		time.sleep(3.0)
		s = ""
		for ch in self.ch_arr:
			s = s + ch.getChannelName()+"  "
		self.outF.write(s+"\n")
		
	def dumpLine(self):
		s = ""
		for ch in self.ch_arr:
			s = s + " %12.5e "%ch.getValue()
		self.outF.write(s+"\n")
		self.outF.flush()

	def printLine(self):		
		print "===========TIS data============="
		for ch in self.ch_arr:		
			print "debug ",ch.getChannelName(),"= %12.5e  "%ch.getValue()	
		
	def close(self):
		self.outF.close()
		
		
#===============================================================
#              MAIN PROGRAM
#===============================================================

if len(sys.argv) != 2 :
        outDir = "/Diagnostics/Data/physics/TIS_Raster/"
	print "Usage: >jython ",sys.argv[0]," <name of output file>, will put scan result into ",outDir  	
        outFileName = outDir + datetime.now().strftime("%Y-%m-%d-%H-%M-%S.txt")
else :
        outFileName = sys.argv[i1]

outF = open(outFileName,"w")

tis_data = TIS_Data(outF)
#tis_data.printLine()


trigger = Trigger()
startListener = StartActionListener(trigger)
stopListener = StopActionListener(trigger)
exitListener = ExitActionListener()

#----------------------------------------------------------
#make GUI
#----------------------------------------------------------
frame = JFrame("TIS Rastering")
frame.getContentPane().setLayout(BorderLayout())
buttonPanel = JPanel(GridLayout(1,3))

startButton = JButton("Go") 
stopButton = JButton("Stop") 
exitButton = JButton("Exit") 
buttonPanel.add(startButton)
buttonPanel.add(stopButton)
buttonPanel.add(exitButton)

startButton.addActionListener(startListener)
stopButton.addActionListener(stopListener)
exitButton.addActionListener(exitListener)

frame.getContentPane().add(buttonPanel,BorderLayout.CENTER)
frame.addWindowListener(WindowCloser())
frame.setSize(Dimension(400,100))
frame.show()
#----------GUI is done-------------------------------

trigger.setRun(0)
trigger.wait()

#----------------------------------------
# let's make the data points for scan
#----------------------------------------

dcv_pv_names = []
dcv_pv_names.append("RTBT_Mag:PS_DCV30:I_Set")
dcv_pv_names.append("RTBT_Mag:PS_DCV28:I_Set")
dcv_pv_names.append("RTBT_Mag:PS_DCV21:I_Set")

dch_pv_names = []
dch_pv_names.append("RTBT_Mag:PS_DCH30:I_Set")
dch_pv_names.append("RTBT_Mag:PS_DCH28:I_Set")
dch_pv_names.append("RTBT_Mag:PS_DCH22:I_Set")
dch_pv_names.append("RTBT_Mag:PS_DCH20:I_Set")
#dch_pv_names.append("RTBT_Mag:PS_DCH16:I_Set")

name_to_ch_dict = {}

dcv_ch_arr = []
for name in dcv_pv_names:
	ch = WrappedChannel(name)
	dcv_ch_arr.append(ch)
	name_to_ch_dict[name] = ch

dch_ch_arr = []
for name in dch_pv_names:
	ch = WrappedChannel(name)
	dch_ch_arr.append(ch)
	name_to_ch_dict[name] = ch

time.sleep(2.0)

dcv_curr_dict = {}
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  -10.0

dch_curr_dict = {}
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH30:I_Set"]] =  120.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH28:I_Set"]] =  120.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH22:I_Set"]] =  -14.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH20:I_Set"]] =  14.0
#dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH16:I_Set"]] =  14.0

#-----set up horizontal movements
corrDictH_arr = []
# add DCH16 changes
#for curr in [14.0,11.0,8.0,5, 1.5]:
#	dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH16:I_Set"]] =  curr
#	corrDict = CorrDict(dch_ch_arr,dch_curr_dict)
#	corrDictH_arr.append(corrDict)
	
# add DCH20 changes
for curr in [14.0,11.0,8.0,5,-1.5]:
	dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH20:I_Set"]] =  curr
	corrDict = CorrDict(dch_ch_arr,dch_curr_dict)
	corrDictH_arr.append(corrDict)
	
# add DCH22 changes
for curr in [-14.0,-7.,0.,7.0, 14.0]:
	dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH22:I_Set"]] =  curr
	corrDict = CorrDict(dch_ch_arr,dch_curr_dict)
	corrDictH_arr.append(corrDict)
	
# add DCH28 changes
for curr in [120.0,80.0,40.0,0.0,-40.0,-80.0,-120.0]:
	dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH28:I_Set"]] =  curr
	corrDict = CorrDict(dch_ch_arr,dch_curr_dict)
	corrDictH_arr.append(corrDict)
	
# add DCH30 changes
for curr in [120.0,80.0,40.0,0.0,-40.0,-80.0,-120.0]:
	dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH30:I_Set"]] =  curr
	corrDict = CorrDict(dch_ch_arr,dch_curr_dict)
	corrDictH_arr.append(corrDict)	
	
#----set up vertical movements
corrDictV_arr = []

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  -10.0
corrDict = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictV_arr.append(corrDict)

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  40.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  40.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  2.0
corrDict = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictV_arr.append(corrDict)

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =   0.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] = -40.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  2.0
corrDict = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictV_arr.append(corrDict)

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] = -60.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] = -80.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  2.0
corrDict = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictV_arr.append(corrDict)

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =-100.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =-120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  2.0
corrDict = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictV_arr.append(corrDict)

#----set up the Step6 movement
corrDictHV_Step6_arr = []

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] = -120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] = -120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =   6.0


dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH30:I_Set"]] =    0.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH28:I_Set"]] =    0.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH22:I_Set"]] =   -4.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH20:I_Set"]] =   -1.5
#dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH16:I_Set"]] =   1.5

corrDictH = CorrDict(dch_ch_arr,dch_curr_dict)
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Step6_arr.append([corrDictH,corrDictV])

# add DCV21 changes
for curr in [6.0,3.0,0.0,-5.0,-10.0]:
	dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  curr
	corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
	corrDictHV_Step6_arr.append([corrDictH,corrDictV])
	
# add DCV28 changes
for curr in [-120.0,-80.0,-40.0,0.0,40.0,80.0,120.0]:
	dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  curr
	corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
	corrDictHV_Step6_arr.append([corrDictH,corrDictV])
	
 # add DCV30 changes
for curr in [-120.0,-80.0,-40.0,0.0,40.0,80.0,120.0]:
	dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  curr
	corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
	corrDictHV_Step6_arr.append([corrDictH,corrDictV])                            

#-----set up part 2------------
corrDictHV_Part2_arr = []

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =    0.0

dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH30:I_Set"]] =  120.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH28:I_Set"]] =  120.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH22:I_Set"]] =  -10.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH20:I_Set"]] =  10.0
#dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH16:I_Set"]] =  10.0

corrDictH = CorrDict(dch_ch_arr,dch_curr_dict)
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =  -10.0
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =    3.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  -40.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] = -120.0
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH30:I_Set"]] =   20.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH28:I_Set"]] =   20.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH22:I_Set"]] =   10.0
dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH20:I_Set"]] =   -1.5
#dch_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCH16:I_Set"]] =   1.5
corrDictH = CorrDict(dch_ch_arr,dch_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV30:I_Set"]] =  120.0
dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV28:I_Set"]] =  120.0
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

dcv_curr_dict[name_to_ch_dict["RTBT_Mag:PS_DCV21:I_Set"]] =    0.0
corrDictV = CorrDict(dcv_ch_arr,dcv_curr_dict)
corrDictHV_Part2_arr.append([corrDictH,corrDictV])

#-----------combine all corrections cofnigurations in one array
corrDictHV_arr = []
order = +1
for corrDictV in corrDictV_arr:
	corrDictH_tmp_arr = corrDictH_arr[:]
	if(order < 0):
		corrDictH_tmp_arr.reverse()
	for corrDictH in corrDictH_tmp_arr:
		corrDictHV_arr.append([corrDictH,corrDictV])
	order = - order
	
for [corrDictH,corrDictV] in corrDictHV_Step6_arr:
	corrDictHV_arr.append([corrDictH,corrDictV])
	
for [corrDictH,corrDictV] in corrDictHV_Part2_arr:
	corrDictHV_arr.append([corrDictH,corrDictV])

n_total = len(corrDictHV_arr)
print "total number of steps=",n_total

for i in range(len(corrDictHV_arr)):
	trigger.wait()
	print "step=",i," from ",n_total,"   ======================================="
	[corrDictH,corrDictV] = corrDictHV_arr[i]
	corrDictH.setToAccelerator()
	corrDictV.setToAccelerator()
	trigger.wait()
	time.sleep(4.0)
	trigger.wait()
	trigger.makeShot()
	trigger.wait()
	corrDictH.printChs()
	corrDictV.printChs()
	tis_data.dumpLine()
	tis_data.printLine()
	
tis_data.close()
print "Done."
sys.exit(1)

