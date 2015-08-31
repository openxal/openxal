#
# This is a collection of classes for the Event Monitor App
#

import sys
import math
import types
import time
import os

from jarray import *
from java.lang import *
from java.util import *
from java.io import *
from javax.swing import *
from java.awt import *
from java.text import SimpleDateFormat
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from javax.swing.table import AbstractTableModel, TableModel
from javax.swing.filechooser import FileNameExtensionFilter

from xal.smf.data import XMLDataManager
from xal.tools.xml import XmlDataAdaptor

from xal.smf import AcceleratorSeqCombo

from xal.tools.apputils import VerticalLayout

from xal.extension.widgets.plot import BasicGraphData,FunctionGraphsJPanel

from xal.ca import ChannelFactory,IEventSinkValue,Monitor,IEventSinkValTime,Channel
from xal.ca.view import ChannelNameDocument

from xal.extension.widgets.swing import DoubleInputTextField
from xal.tools.text import ScientificNumberFormat

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#window closer will kill this apps 
class WindowCloser(WindowAdapter):
	def windowClosing(self,windowEvent):
		sys.exit(1)
	

buffer_size_text = DoubleInputTextField(5.0,ScientificNumberFormat(1),10)

class Event_Checker:
	def __init__(self, event_handler, min_limit_text, max_limit_text):
		self.event_handler = event_handler
		self.min_limit_text = min_limit_text
		self.max_limit_text = max_limit_text
		
	def checkEvent(self, buffer_holder):
		buff_arr = buffer_holder.buff_arr
		#print "debug checkEvent buff_arr=",buff_arr		
		[time_in,val_arr,date_time] = buff_arr[len(buff_arr)/2]
		#print "debug checkEvent val_arr=",val_arr
		n_vals = len(val_arr)
		val_ind = n_vals/2
		val = val_arr[val_ind]
		val_min = self.min_limit_text.getValue()
		val_max = self.max_limit_text.getValue()
		#print "debug checkEvent val=",val,"  min=",val_min," max=",val_max
		if(val_min < val and val < val_max):
			tm0 = time_in
			if(n_vals != 1):
				time_step = 1.0/(n_vals-1)
				tm0 = time_in - (n_vals - 1 - val_ind)*time_step
			self.event_handler.eventHappened(tm0,date_time)
			#print "debug checkEvent eventHappened time=",tm0,"  date=",date_time
			
class Values_Buffer_Holder:
	def __init__(self):
		# self.buff_arr[time,val_arr,date]
		self.buff_arr = []
		self.buff_size = int(buffer_size_text.getValue())
		self.val_count = 0
		self.event_checker = null

	def setEventChecker(self,event_checker):
		self.event_checker = event_checker

	def clear(self):
		self.buff_arr = []
		self.val_count = 0	
		
	def setBuffSize(self,buff_size):
		self.buff_size = buff_size
		buffer_size_text.setValue(1.0*buff_size)
		self.clear()

	def removeEventChecker(self):
		self.event_checker = null
		self.clear()

	def addValue(self,time_in,val_arr,date_time):
		self.buff_size = int(buffer_size_text.getValue())
		#print "debug Values_Buffer_Holder.addValue  count=",self.val_count
		if(self.val_count != self.buff_size):
			self.buff_arr.append([time_in,val_arr,date_time])
			self.val_count += 1
		else:
			for i in range(self.buff_size-1):
				self.buff_arr[i] = self.buff_arr[i+1]
			self.buff_arr[self.buff_size-1] = [time_in,val_arr,date_time]
			if(self.event_checker != null):
				self.event_checker.checkEvent(self)
				
	def getCollectedData(self,tm0):
		""" Collects all data in time and values linear array """
		res_val_arr = []
		res_time_arr = []	
		if(len(self.buff_arr) == 0): return [[],[]]
		n_vals = len(self.buff_arr[0][1])
		if(n_vals == 1):
			for [time_in,val_arr,date_time] in self.buff_arr:
				res_time_arr.append(time_in - tm0)
				res_val_arr.append(val_arr[0])
		else:
			time_step = 1.0/(n_vals -1)
			for [time_in,val_arr,date_time] in self.buff_arr:
				for j in range(n_vals):
					val = val_arr[j]
					tm = time_in - (n_vals - 1 - j)*time_step
					res_val_arr.append(val)
					res_time_arr.append(tm - tm0)
		return [res_time_arr,res_val_arr]
							
class PV_Value_Monitor(IEventSinkValTime):
	def __init__(self,pv):
		self.buffer_holder = Values_Buffer_Holder()
		self.pv = pv
		self.date_frmt = SimpleDateFormat("yyyy.MM.dd'_'HH:mm:ss")
		self.isOn = false

	def eventValue(self, channel_time_record, chan):
		if(self.isOn): 
			val_arr = channel_time_record.doubleArray()
			tm = channel_time_record.timeStampInSeconds()
			date_time = channel_time_record.getTimestamp().toString(self.date_frmt)
			d_p = date_time.rfind(":")
			date_time = date_time[:(d_p+3)]
			#print "debug PV_Value_Monitor date=",date_time #," val_arr=",val_arr
			self.buffer_holder.addValue(tm,val_arr,date_time)
		
	def getBufferHolder(self):
		return self.buffer_holder
		
	def getPV(self):
		return self.pv
		
	def getPV_Name(self):
		return self.pv.channelName()
		
	def setIsOn(self, isOn):
		if(isOn == false):
			self.buffer_holder.clear()
		self.isOn = isOn
		
	def getIsOn(self):
		return self.isOn
		
class PV_Value_Monitor_Dict:
	""" Factory for PV_Value_Monitor """
	def __init__(self):
		self.pv_names_dict = {}
		
	def getPV_Value_Monitor(self,pv_name):
		if(self.pv_names_dict.has_key(pv_name)):
			pv_value_monitor = self.pv_names_dict[pv_name]
			pv_value_monitor.setIsOn(false)
			return pv_value_monitor
		ch = ChannelFactory.defaultFactory().getChannel(pv_name)
		pv_value_monitor = PV_Value_Monitor(ch)
		ch.addMonitorValTime(pv_value_monitor,Monitor.VALUE)
		pv_value_monitor.setIsOn(false)
		self.pv_names_dict[pv_name] = pv_value_monitor
		return pv_value_monitor
			
class Event_Handler:
	def __init__(self, event_holder, min_limit_text, max_limit_text):
		self.event_holder = event_holder
		self.event_checker = Event_Checker(self, min_limit_text, max_limit_text)
		self.pv_value_monitor_dict = PV_Value_Monitor_Dict()
		self.trigger_monitor = null
		self.pv_monitors = []
		
	def clean(self):
		if(self.trigger_monitor != null):
			self.trigger_monitor.getBufferHolder().removeEventChecker()
		self.trigger_monitor = null
		self.pv_monitors = []		
		
	def setTriggerMonitor(self,pv_name):
		if(self.trigger_monitor != null):
			self.trigger_monitor.setIsOn(false)
			self.trigger_monitor.getBufferHolder().removeEventChecker()
		try:
			self.trigger_monitor = self.pv_value_monitor_dict.getPV_Value_Monitor(pv_name)
			self.trigger_monitor.getBufferHolder().setEventChecker(self.event_checker)
		except:
			self.trigger_monitor = null
		
	def addPV_Value_Monitor(self,pv_name):
		pv_monitor = self.pv_value_monitor_dict.getPV_Value_Monitor(pv_name)
		self.pv_monitors.append(pv_monitor)
		
	def setMonitoring(self,isOn):
		if(self.trigger_monitor != null):
			self.trigger_monitor.setIsOn(isOn)
			for pv_monitor in self.pv_monitors:
				pv_monitor.setIsOn(isOn)

	def getIsOn(self):
		if(self.trigger_monitor == null): return false
		return self.trigger_monitor.getIsOn()
		
	def removeAllMonitors(self):
		self.setMonitoring(false)
		self.pv_monitors = []
		
	def getPV_Monitors(self):
		return self.pv_monitors
		
	def setBuffSize(self,buff_size):
		if(self.trigger_monitor != null):
			self.trigger_monitor.getBufferHolder().setBuffSize(buff_size)
		for pv_monitor in self.pv_monitors:
			pv_monitor.getBufferHolder().setBuffSize(buff_size)
		
	def eventHappened(self,tm0,date_time):
		if(self.trigger_monitor != null):
			#make an event object
			buffer_holder = self.trigger_monitor.getBufferHolder()
			[time_arr,val_arr] = buffer_holder.getCollectedData(tm0)
			trigger_data =[self.trigger_monitor.getPV().channelName(),[time_arr,val_arr]]
			pv_monitors_data = []
			for pv_monitor in self.pv_monitors:
				buffer_holder = pv_monitor.getBufferHolder()
				[time_arr,val_arr] = buffer_holder.getCollectedData(tm0)
				pv_name = pv_monitor.getPV().channelName()
				pv_monitors_data.append([pv_name,[time_arr,val_arr]])
			event_object = Event_Object(date_time,trigger_data,pv_monitors_data)
			self.event_holder.addEvent(event_object)
			#print "debug eventHappened date=",date_time," buff size=",len(self.event_holder.getEvents())
			self.trigger_monitor.getBufferHolder().clear()

class Event_Object:
	""" The event data holder """
	def __init__(self,date_time,trigger_data,pv_monitors_data):
		# date_time is a string
		# trigger_data = [pv_name,[time_arr,val_arr]]
		# pv_monitors_data is array of [pv_name,[time_arr,val_arr]]
		self.date_time = date_time
		self.trigger_data = trigger_data
		self.pv_monitors_data = pv_monitors_data
		self.pv_shown = []
		for i in range(len(self.pv_monitors_data)):
			self.pv_shown.append(true)
		self.is_dumped = false
		
	def getDateAndTime(self):
		return self.date_time
		
	def getTriggerPV_Name(self):
		return self.trigger_data[0]
		
	def getNumberOfPVs(self):
		return len(self.pv_monitors_data)
		
	def getPV_Name(self, ind):
		return self.pv_monitors_data[ind][0]
		
	def getTriggerTimeValArr(self):
		return self.trigger_data[1]
		
	def getPV_TimeValArr(self, ind):
		return self.pv_monitors_data[ind][1]

class Event_Object_Holder:
	""" The events holder """
	def __init__(self,event_buffer_size_text,monitor_controller):
		self.events = []	
		self.event_buffer_size_text = event_buffer_size_text
		self.monitor_controller = monitor_controller
		
	def addEvent(self,event):
		n_buff_max = int(self.event_buffer_size_text.getValue())
		if(len(self.events) == n_buff_max): self.events = self.events[1:]
		self.events.append(event)
		self.monitor_controller.events_table.getModel().fireTableDataChanged()
		if(self.monitor_controller.dump_button.isSelected()):
			self.dumpEvents()
		
	def getNumberOfEvents(self):
		return len(self.events)
		
	def getEvents(self):
		return self.events
		
	def getEvent(self, ind):
		return self.events[ind]
		
	def clean(self):
		self.events = []	
		
	def dumpEvents(self):
		events = self.events[:]
		if(len(events) == 0): return
		dump_dir = self.monitor_controller.dumpDirJText.getText()
		for event in events:
			if(event.is_dumped == false):
				event.is_dumped = true
				date_time = event.getDateAndTime()
				try:
					os.chdir(dump_dir)
				except:
					textMess = self.monitor_controller.event_monitor_document.event_monitor_window.getMessageTextField()
					textMess.setText("Cannot find the directory:"+dump_dir)
					return
				os.mkdir(date_time)
				os.chdir("./"+date_time)
				#trigger buffer
				[pv_name,[time_arr,val_arr]] = event.trigger_data
				fl_out = open("trigger_"+pv_name+".dat","w")
				for i in range(len(time_arr)):
					tm = time_arr[i]
					val = val_arr[i]
					st = "%7.3f  %12.5g"%(tm,val)
					fl_out.write(st+"\n")
				fl_out.close()
				#----all monitored pvs ------
				for [pv_name,[time_arr,val_arr]] in event.pv_monitors_data:
					fl_out = open(pv_name+".dat","w")
					for i in range(len(time_arr)):
						tm = time_arr[i]
						val = val_arr[i]
						st = "%7.3f  %12.5g"%(tm,val)
						fl_out.write(st+"\n")
					fl_out.close()
				os.chdir("..")				
	
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------	
class Set_Trigger_PV_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		self.monitor_controller.sinceTimeJText.setText("Not Running.")
		self.monitor_controller.event_handler.setMonitoring(false)
		pv_name = self.monitor_controller.pvTriggerJText.getText()
		self.monitor_controller.setTriggerMonitor(pv_name)
		text = self.monitor_controller.event_monitor_document.event_monitor_window.getMessageTextField()
		if(self.monitor_controller.event_handler.trigger_monitor == null):
			text.setText("Cannot set up Trigger PV:"+pv_name)
		else:
			text.setText("")
		self.monitor_controller.pv_monitored_table.getModel().fireTableDataChanged()
		
	
class Add_PV_To_Monitored_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		self.monitor_controller.sinceTimeJText.setText("Not Running.")
		pv_name = self.monitor_controller.pvMonitorJText.getText()
		if(len(pv_name) == 0): return
		try:
			self.monitor_controller.event_handler.addPV_Value_Monitor(pv_name)	
			self.monitor_controller.event_handler.setMonitoring(false)
			self.monitor_controller.pv_monitored_table.getModel().fireTableDataChanged()
			self.monitor_controller.pvMonitorJText.setText("")
		except:
			self.monitor_controller.event_handler.setMonitoring(false)
			self.monitor_controller.pvMonitorJText.setText("")
			
class Remove_Monitored_PVs_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		self.monitor_controller.event_handler.setMonitoring(false)
		ind_arr = self.monitor_controller.pv_monitored_table.getSelectedRows()
		pv_monitors = self.monitor_controller.event_handler.getPV_Monitors()[:]
		pv_monitors_to_remove = []
		for ind in ind_arr:
			pv_monitors_to_remove.append(pv_monitors[ind])
		pv_monitors_to_keep = []
		for pv_monitor in pv_monitors:
			if(not pv_monitor in pv_monitors_to_remove):
				pv_monitors_to_keep.append(pv_monitor)
		self.monitor_controller.event_handler.removeAllMonitors()
		for pv_monitor in pv_monitors_to_keep:
			self.monitor_controller.event_handler.addPV_Value_Monitor(pv_monitor.getPV_Name())
		self.monitor_controller.pv_monitored_table.getModel().fireTableDataChanged()
		self.monitor_controller.sinceTimeJText.setText("Not Running.")

class Start_Monitoring_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		textMess = self.monitor_controller.event_monitor_document.event_monitor_window.getMessageTextField()
		if(self.monitor_controller.event_handler.trigger_monitor != null):
			self.monitor_controller.event_handler.setMonitoring(true)			
			text = self.monitor_controller.event_monitor_document.event_monitor_window.time_txt.getTimeTextField()
			self.monitor_controller.sinceTimeJText.setText("Rinning Since "+text.getText())
			textMess.setText("")
		else:
			textMess.setText("No Trigger PV!")
			
class Stop_Monitoring_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		self.monitor_controller.event_handler.setMonitoring(false)
		self.monitor_controller.sinceTimeJText.setText("Not Running.")
		textMess = self.monitor_controller.event_monitor_document.event_monitor_window.getMessageTextField()
		textMess.setText("")

class Table_Selection_Listener(ListSelectionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		self.monitor_controller.graphs_panel.updateGraphs()

class Find_Dump_Dir_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		dir_name = self.monitor_controller.dumpDirJText.getText()
		fc = JFileChooser()
		if(len(dir_name) != 0):
			try:
				fc.setCurrentDirectory(File(dir_name))
			except:
				self.monitor_controller.dumpDirJText.setText("")
		fc.setDialogTitle("Set the dump directory for events ...")
		fc.setApproveButtonText("Set")
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY)
		returnVal = fc.showOpenDialog(self.monitor_controller.event_monitor_document.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			dir_name = fc.getSelectedFile().getPath()
			self.monitor_controller.dumpDirJText.setText(dir_name)

class Dump_Events_To_Files_Listener(ActionListener):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		
	def actionPerformed(self,actionEvent):
		button = actionEvent.getSource()
		if(button.isSelected()):
			self.monitor_controller.event_holder.dumpEvents()

#-----------------------------------------------------------------
#  JTable models for PVs and Buffers
#-----------------------------------------------------------------		

class Monitored_PV_Table_Model(AbstractTableModel):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		self.columnNames = ["Monitored PV Name"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		n_pvs = len(self.monitor_controller.event_handler.pv_monitors)
		return n_pvs
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		return self.monitor_controller.event_handler.pv_monitors[row].getPV_Name()

	def getColumnClass(self,col):
		return self.string_class

	def isCellEditable(self,row,col):
		return false
		
class Buffer_Events_Table_Model(AbstractTableModel):
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		self.columnNames = ["Event Date"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		n_pvs = len(self.monitor_controller.event_holder.getEvents())
		return n_pvs
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		event = self.monitor_controller.event_holder.getEvent(row)
		return event.getDateAndTime()

	def getColumnClass(self,col):
		return self.string_class

	def isCellEditable(self,row,col):
		return false		
		
#---------------------------------------------------
#          GUI
#---------------------------------------------------	
class DateAndTimeText:
	
	def __init__(self):
		self.dFormat = SimpleDateFormat("'Time': MM.dd.yy HH:mm ")
		self.dateTimeField = JFormattedTextField(self.dFormat)
		self.dateTimeField.setEditable(false)
		thr = Thread(Timer(self.dateTimeField))
		thr.start()

	def getTime(self):
		return self.dateTimeField.getText()
		
	def getTimeTextField(self):
		return self.dateTimeField
		
	def getNewTimeTextField(self):
		newText = JTextField()
		newText.setDocument(self.dateTimeField.getDocument())
		newText.setEditable(false)
		return newText

class Timer(Runnable):
	
	def __init__(self,dateTimeField):
		self.dateTimeField = dateTimeField
	
	def run(self):
		while(true):
			self.dateTimeField.setValue(Date())
			time.sleep(10.)

class OneGraphPanel:
	def __init__(self, title):
		self.gpF = FunctionGraphsJPanel()
		self.gpF.setLegendButtonVisible(true)
		self.gpF.setChooseModeButtonVisible(true)			
		self.gpF.setName(title)
		self.gpF.setAxisNames("time, [sec]","Value, [arb. units]")	
		etched_border = BorderFactory.createEtchedBorder()
		self.gpF.setBorder(etched_border)
		self.gd_arr = []
		self.color_arr = [Color.BLUE,Color.BLACK,Color.RED,Color.GREEN,Color.MAGENTA]
	
	def clean(self):
		self.gpF.removeAllGraphData()
		self.gd_arr = []
	
	def addGraph(self,pv_name,time_arr,val_arr):
		ind = len(self.gd_arr)%len(self.color_arr)
		gd = BasicGraphData()
		gd.setGraphProperty("Legend",pv_name)
		gd.addPoint(time_arr,val_arr)
		gd.setGraphColor(self.color_arr[ind])
		self.gpF.addGraphData(gd)
		self.gd_arr.append(gd)
		
class TwoGraphsPanel:
	def __init__(self,monitor_controller):
		self.monitor_controller = monitor_controller
		etched_border = BorderFactory.createEtchedBorder()
		#etched_border = BorderFactory.createRaisedSoftBevelBorder()
		self.trigger_graph = OneGraphPanel("Trigger Data")
		self.pvMon_graph = OneGraphPanel("Monitored PVs Data")
		self.main_panel = JPanel(GridLayout(2,1))
		self.main_panel.add(self.trigger_graph.gpF)
		self.main_panel.add(self.pvMon_graph.gpF)
		self.main_panel.setBorder(etched_border)
		
	def clear(self):
		self.trigger_graph.clean()
		self.pvMon_graph.clean()
		
	def updateGraphs(self):
		self.clear()
		events = self.monitor_controller.event_holder.getEvents()[:]
		event_ind = self.monitor_controller.events_table.getSelectedRow()
		if(event_ind > (len(events)-1) or event_ind < 0):
			return
		event = events[event_ind]
		[pv_name,[time_arr,val_arr]] = event.trigger_data
		self.trigger_graph.addGraph(pv_name,time_arr,val_arr)
		ind_arr = self.monitor_controller.pv_monitored_table.getSelectedRows()
		pv_names_arr = []
		for i in ind_arr:
			pv_monitor = self.monitor_controller.event_handler.pv_monitors[i]
			pv_names_arr.append(pv_monitor.getPV_Name())
		pv_monitors_data = event.pv_monitors_data
		for [pv_name,[time_arr,val_arr]] in pv_monitors_data:
			if(pv_name in pv_names_arr):
				self.pvMon_graph.addGraph(pv_name,time_arr,val_arr)
		
	def getMainPanel(self):
		return self.main_panel		
	
class Monitor_Controller:
	def __init__(self, event_monitor_document):
		self.event_monitor_document = event_monitor_document
		self.main_panel = JPanel(BorderLayout())	
		etched_border = BorderFactory.createEtchedBorder()
		#etched_border = BorderFactory.createRaisedSoftBevelBorder()
		etched_border = BorderFactory.createLineBorder(Color.black, 2, false)
		#--------------------------------------------------------------
		self.left_panel = JPanel(VerticalLayout())
		main_label = JLabel("============ Event Monitor Parameters =============",JLabel.CENTER)
		self.left_panel.add(main_label)	
		self.left_panel.setBorder(etched_border)
		#----------Event Monotor Params-------------
		monitor_params0_panel = JPanel(GridLayout(4,2))
		self.buffer_size_text = buffer_size_text
		buffer_size_label = JLabel("Buff. Time [sec]:",JLabel.RIGHT)
		monitor_params0_panel.add(buffer_size_label)
		monitor_params0_panel.add(self.buffer_size_text)
		self.event_buffer_size_text = DoubleInputTextField(3.0,ScientificNumberFormat(1),10)
		event_buffer_size_label = JLabel("Event Buff. Size:",JLabel.RIGHT)
		monitor_params0_panel.add(event_buffer_size_label)
		monitor_params0_panel.add(self.event_buffer_size_text)	
		self.min_limit_text = DoubleInputTextField(1.0e-8,ScientificNumberFormat(4),10)
		self.max_limit_text = DoubleInputTextField(1.0e-3,ScientificNumberFormat(4),10)
		min_lim_label = JLabel("Trigger Min Value:",JLabel.RIGHT)
		max_lim_label = JLabel("Trigger Max Value:",JLabel.RIGHT)
		monitor_params0_panel.add(min_lim_label)
		monitor_params0_panel.add(self.min_limit_text)
		monitor_params0_panel.add(max_lim_label)
		monitor_params0_panel.add(self.max_limit_text)
		monitor_params1_panel = JPanel(BorderLayout())
		monitor_params1_panel.add(monitor_params0_panel,BorderLayout.WEST)
		monitor_params_panel = JPanel(BorderLayout())
		monitor_params_panel.add(monitor_params1_panel,BorderLayout.NORTH)
		self.left_panel.add(monitor_params_panel)	
		self.pvTriggerJText = JTextField(ChannelNameDocument(),"",30)
		pvTriggerButton = JButton("Set Trigger PV")
		pvTriggerButton.addActionListener(Set_Trigger_PV_Listener(self))
		triggerPanel = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		triggerPanel.add(pvTriggerButton)
		triggerPanel.add(self.pvTriggerJText)
		self.left_panel.add(triggerPanel)
		self.pvMonitorNameJText = JTextField(ChannelNameDocument(),"",30)
		#------------Two Tables Panel --------------------
		twoTables_panel = JPanel(GridLayout(2,1))
		#------------Monitored PV table-------------------
		pvMonitored_panel = JPanel(BorderLayout())
		border = BorderFactory.createTitledBorder(etched_border,"Monitored PVs")
		pvMonitored_panel.setBorder(border)
		self.pvMonitorJText   = JTextField(ChannelNameDocument(),"",25)
		pvMonitorButton = JButton("Add PV to Monitored")
		pvMonitorRemoveButton = JButton("Remove Selected PVs")
		pvMonitorButton.addActionListener(Add_PV_To_Monitored_Listener(self))
		pvMonitorRemoveButton.addActionListener(Remove_Monitored_PVs_Listener(self))
		pvMonitoredPanel0 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		pvMonitoredPanel0.add(pvMonitorButton)
		pvMonitoredPanel0.add(self.pvMonitorJText)
		pvMonitoredPanel1 = JPanel(FlowLayout(FlowLayout.RIGHT,1,1))
		pvMonitoredPanel1.add(pvMonitorRemoveButton)
		pvMonitoredPanel2 = JPanel(BorderLayout())
		pvMonitoredPanel2.add(pvMonitoredPanel0,BorderLayout.NORTH)
		pvMonitoredPanel2.add(pvMonitoredPanel1,BorderLayout.SOUTH)
		pvMonitored_panel.add(pvMonitoredPanel2,BorderLayout.NORTH)
		self.pv_monitored_table = JTable(Monitored_PV_Table_Model(self))
		self.pv_monitored_table.setPreferredScrollableViewportSize(Dimension(200,160))
		#self.pv_monitored_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.pv_monitored_table.setFillsViewportHeight(true)
		pvMonitored_panel.add(JScrollPane(self.pv_monitored_table), BorderLayout.CENTER)		
		twoTables_panel.add(pvMonitored_panel)
		#------------Event Buffer Table-------------------
		buffEvents_panel = JPanel(BorderLayout())
		border = BorderFactory.createTitledBorder(etched_border,"Buffered Events")
		buffEvents_panel.setBorder(border)
		self.events_table = JTable(Buffer_Events_Table_Model(self))
		self.events_table.setPreferredScrollableViewportSize(Dimension(200,160))
		self.events_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.events_table.setFillsViewportHeight(true)
		buffEvents_panel.add(JScrollPane(self.events_table), BorderLayout.CENTER)
		twoTables_panel.add(buffEvents_panel)
		#------Stop - Start Monitoring Buttons
		startStopPanel = JPanel(BorderLayout())
		startStopPanel.setBorder(etched_border)
		self.startMonitoringButton = JButton("Start Monitoring")
		self.stopMonitoringButton = JButton("Stop Monitoring")
		self.startMonitoringButton.addActionListener(Start_Monitoring_Listener(self))
		self.stopMonitoringButton.addActionListener(Stop_Monitoring_Listener(self))
		startStopButtonsPanel = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		startStopButtonsPanel.add(self.startMonitoringButton)
		startStopButtonsPanel.add(self.stopMonitoringButton)		
		startStopPanel.add(startStopButtonsPanel,BorderLayout.NORTH)
		self.sinceTimeJText = JTextField(30)
		self.sinceTimeJText.setForeground(Color.red)
		self.sinceTimeJText.setText("Not Running.")
		startStopPanel.add(self.sinceTimeJText,BorderLayout.SOUTH)
		#-------Event Handlers--------------------------------
		self.event_holder = Event_Object_Holder(self.event_buffer_size_text,self)
		self.event_handler = Event_Handler(self.event_holder,self.min_limit_text,self.max_limit_text)
		#-----graph panels--------------------
		self.graphs_panel = TwoGraphsPanel(self)
		#-----setup dump directory -----------
		self.dump_directory_panel = JPanel(BorderLayout())
		self.dump_button = JRadioButton("Dump Events to Files")
		self.dump_button.addActionListener(Dump_Events_To_Files_Listener(self))
		dump_dir_choose_button = JButton(" Choose Dump Dir. ")
		dump_dir_choose_button.addActionListener(Find_Dump_Dir_Listener(self))
		dump_dir_label = JLabel("  Dump Dir:",JLabel.RIGHT)
		self.dumpDirJText = JTextField(50)
		self.dumpDirJText.setText("/ade/xal/docs/EventMonitor/EventDirs/")
		dump_dir_panel0 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		dump_dir_panel0.add(self.dump_button)
		dump_dir_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		dump_dir_panel1.add(dump_dir_choose_button)
		dump_dir_panel1.add(dump_dir_label)
		dump_dir_panel2 = JPanel(BorderLayout())
		dump_dir_panel2.add(dump_dir_panel1,BorderLayout.WEST)
		dump_dir_panel2.add(self.dumpDirJText,BorderLayout.CENTER)
		self.dump_directory_panel.add(dump_dir_panel0,BorderLayout.NORTH)
		self.dump_directory_panel.add(dump_dir_panel2,BorderLayout.SOUTH)
		#-----set up listeners ------------------------------------
		tables_selection_listener = Table_Selection_Listener(self)
		self.pv_monitored_table.getSelectionModel().addListSelectionListener(tables_selection_listener)
		self.events_table.getSelectionModel().addListSelectionListener(tables_selection_listener)
		#--------- set up main panel
		params_and_tables_panel = JPanel(BorderLayout())
		params_and_tables_panel.add(self.left_panel,BorderLayout.NORTH)
		params_and_tables_panel.add(twoTables_panel,BorderLayout.CENTER)
		params_and_tables_panel.add(startStopPanel,BorderLayout.SOUTH)
		self.main_panel.add(params_and_tables_panel,BorderLayout.WEST)
		self.main_panel.add(self.graphs_panel.getMainPanel(),BorderLayout.CENTER)
		self.main_panel.add(self.dump_directory_panel,BorderLayout.SOUTH)

	def setTriggerMonitor(self,pv_name):
		self.event_handler.setTriggerMonitor(pv_name)
		
	def getMainPanel(self):
		return self.main_panel
	
#---------------------------------------------------------
#         I/O Configuration Controller Action Listeners
#---------------------------------------------------------
class Config_Read_Button_Listener(ActionListener):
	def __init__(self,config_controller):
		self.config_controller = config_controller

	def actionPerformed(self,actionEvent):
		oldFileName = self.config_controller.filePathJText.getText()
		fc = JFileChooser()
		if(len(oldFileName) != 0):
			try:
				fc.setSelectedFile(File(oldFileName))
			except:
				fc.setCurrentDirectory(File(oldFileName))
				self.config_controller.filePathJText.setText("")
		fc.setDialogTitle("Read Configuration from the file ...")
		fc.setApproveButtonText("Open")
		fl_filter = FileNameExtensionFilter("Event Monitor Conf.",["evnt",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.config_controller.event_monitor_document.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			if(fl_in.isFile() and fl_in.canRead()):
				self.config_controller.readData(fl_in.toURI().toURL())
				self.config_controller.filePathJText.setText(fl_in.getPath())
				messTxt = self.config_controller.event_monitor_document.event_monitor_window.getMessageTextField()
				messTxt.setText("")				
			else:
				messTxt = self.config_controller.event_monitor_document.event_monitor_window.getMessageTextField()
				messTxt.setText("Could not find file:"+fl_in.getPath())
	
class Config_Save_Button_Listener(ActionListener):
	def __init__(self,config_controller):
		self.config_controller = config_controller

	def actionPerformed(self,actionEvent):
		oldFileName = self.config_controller.filePathJText.getText()
		if(len(oldFileName) == 0): 
			fc = JFileChooser()
			fc.setCurrentDirectory(File("/ade/xal/docs/EventMonitor/Configs/"))
			fc.setDialogTitle("Save Configuration to the file ...")
			fc.setApproveButtonText("Save")
			fl_filter = FileNameExtensionFilter("Event Monitor Conf.",["evnt",])
			fc.setFileFilter(fl_filter)
			returnVal = fc.showOpenDialog(self.config_controller.event_monitor_document.frame)
			if(returnVal == JFileChooser.APPROVE_OPTION):
				fl_in = fc.getSelectedFile()
				if(fl_in.getPath().rfind(".evnt") < 0):
					fl_in = File(fl_in.getPath()+".evnt")		    
				self.config_controller.writeData(fl_in.toURI().toURL())
			return
		try:
			fl_in = File(oldFileName)
			self.config_controller.writeData(fl_in.toURI().toURL())
		except:
			self.config_controller.filePathJText.setText("")

class Config_Save_As_Button_Listener(ActionListener):
	def __init__(self,config_controller):
		self.config_controller = config_controller

	def actionPerformed(self,actionEvent):
		fc = JFileChooser()
		fc.setCurrentDirectory(File("/ade/xal/docs/EventMonitor/Configs/"))
		fc.setDialogTitle("Save Configuration to the file ...")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("Event Monitor Conf.",["evnt",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.config_controller.event_monitor_document.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			if(fl_in.getPath().rfind(".evnt") < 0):
				fl_in = File(fl_in.getPath()+".evnt")
			self.config_controller.writeData(fl_in.toURI().toURL())

#-------------------------------------------
#         I/O Configuration Controller
#-------------------------------------------
class Config_Controller:
	def __init__(self, event_monitor_document):
		self.event_monitor_document = event_monitor_document
		etched_border = BorderFactory.createEtchedBorder()
		#etched_border = BorderFactory.createRaisedSoftBevelBorder()
		etched_border = BorderFactory.createLineBorder(Color.black, 2, false)		
		latestConf_label = JLabel("Latest Cinfiguration File:",JLabel.RIGHT)
		self.filePathJText = JTextField(50)
		self.filePathJText.setText("")
		file_name_panel = JPanel(BorderLayout())
		file_name_panel.add(latestConf_label,BorderLayout.WEST)
		file_name_panel.add(self.filePathJText,BorderLayout.CENTER)
		readConf_button = JButton("Read Configuration")
		saveConf_button = JButton("Save Configuration")
		saveNewConf_button = JButton("Save Configuration As ...")
		readConf_button.addActionListener(Config_Read_Button_Listener(self))
		saveConf_button.addActionListener(Config_Save_Button_Listener(self))
		saveNewConf_button.addActionListener(Config_Save_As_Button_Listener(self))
		buttonsPanel = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		buttonsPanel.add(readConf_button)
		buttonsPanel.add(saveConf_button)
		buttonsPanel.add(saveNewConf_button)
		temp_panel = JPanel(BorderLayout())
		temp_panel.add(file_name_panel,BorderLayout.NORTH)
		temp_panel.add(buttonsPanel,BorderLayout.SOUTH)
		#------set up main panel
		self.main_panel = JPanel(BorderLayout())
		self.main_panel.add(temp_panel,BorderLayout.NORTH)
	
	def getMainPanel(self):
		return self.main_panel
		
	#-------------------------------------------------
	#  Read methods
	#-------------------------------------------------		
	def readData(self, url):
		monitor_controller = self.event_monitor_document.monitor_controller
		monitor_controller.event_holder.clean()
		monitor_controller.events_table.getModel().fireTableDataChanged()
		#------- event handler configuration
		event_handler = monitor_controller.event_handler 
		event_handler.clean()
		da = XmlDataAdaptor.adaptorForUrl(url,false)
		root_da = da.childAdaptor("Event_Monitor")
		buff_size_time = root_da.doubleValue("buff_size_time") 
		event_buff_size = root_da.doubleValue("event_buff_size")
		min_limit = root_da.doubleValue("min_limit")
		max_limit = root_da.doubleValue("max_limit")
		trigger_pv_name = root_da.stringValue("triggerPV")
		eventDir = root_da.stringValue("eventDir")
		monitor_controller.buffer_size_text.setValue(buff_size_time)
		monitor_controller.event_buffer_size_text.setValue(event_buff_size)
		monitor_controller.min_limit_text.setValue(min_limit)
		monitor_controller.max_limit_text.setValue(max_limit)
		event_handler.setTriggerMonitor(trigger_pv_name)
		monitor_controller.pvTriggerJText.setText(trigger_pv_name)
		monitor_controller.dumpDirJText.setText(eventDir)
		for pv_da in root_da.childAdaptors():
			pv_name = pv_da.stringValue("pv_name")
			event_handler.addPV_Value_Monitor(pv_name)
		event_handler.setMonitoring(false)
		monitor_controller.sinceTimeJText.setText("Not Running.")
		monitor_controller.dump_button.setSelected(false)
		monitor_controller.pv_monitored_table.getModel().fireTableDataChanged()

	#-------------------------------------------------
	#  Write methods
	#-------------------------------------------------
	def writeData(self, url):
		monitor_controller = self.event_monitor_document.monitor_controller
		da = XmlDataAdaptor.newEmptyDocumentAdaptor()
		root_da = da.createChild("Event_Monitor")
		root_da.setValue("buff_size_time", monitor_controller.buffer_size_text.getValue())
		root_da.setValue("event_buff_size", monitor_controller.event_buffer_size_text.getValue())
		root_da.setValue("min_limit", monitor_controller.min_limit_text.getValue())		
		root_da.setValue("max_limit", monitor_controller.max_limit_text.getValue())		
		root_da.setValue("triggerPV", monitor_controller.pvTriggerJText.getText())	
		root_da.setValue("eventDir", monitor_controller.dumpDirJText.getText())	
		event_handler = monitor_controller.event_handler
		pv_monitors = monitor_controller.event_handler.pv_monitors[:]
		for pv_monitor in pv_monitors:
			pv_name = pv_monitor.getPV_Name()
			pv_da = root_da.createChild("monitored_pv")
			pv_da.setValue("pv_name",pv_name)
		#---- dump data into the file ------------
		da.writeToUrl(url)
		self.filePathJText.setText(url.getFile())
		

