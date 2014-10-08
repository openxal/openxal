# This Event Monitor App will monitor one PV and creates time buffers for others
# If the conditions are met the data will be saved into the files

import sys
import math
import types
import time
import random
import os

from java.lang import *
from javax.swing import *
from java.awt import BorderLayout
from java.awt import Color
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from java.util import ArrayList
from java.io import File
from java.net import URL

from xal.extension.application import XalDocument, ApplicationAdaptor
from xal.extension.application.smf import AcceleratorApplication, AcceleratorDocument, AcceleratorWindow

from xal.smf.data import XMLDataManager
from xal.smf import AcceleratorSeqCombo

#-----local jython libraries import----
from lib.time_and_date_lib import DateAndTimeText
from lib.event_monitor_cotroller_lib import Monitor_Controller,Config_Controller


false = Boolean("false").booleanValue()
true = Boolean("true").booleanValue()
null = None

#-------------------------------------------------------------------
# Local Classes that are not subclasses of XAL Accelerator Framework
#-------------------------------------------------------------------

class Event_Monitor_Window:
	def __init__(self,event_monitor_document):
		#--- event_monitor_document the parent document for all controllers
		self.event_monitor_document = event_monitor_document
		self.frame = null
		self.centerPanel = JPanel(BorderLayout())		
		self.mainPanel = JPanel(BorderLayout())
		self.time_txt = DateAndTimeText()
		self.messageTextField = JTextField()
		#---------------------------------------
		timePanel = JPanel(BorderLayout())
		timePanel.add(self.time_txt.getTimeTextField(),BorderLayout.CENTER)		
		self.messageTextField.setForeground(Color.red)
		self.centerPanel.add(self.mainPanel,BorderLayout.CENTER)
		tmpP = JPanel(BorderLayout())
		tmpP.add(self.messageTextField, BorderLayout.CENTER)
		tmpP.add(timePanel, BorderLayout.WEST)
		self.centerPanel.add(tmpP,BorderLayout.SOUTH)
		
	def setFrame(self,xal_frame,mainPanel):
		self.frame = xal_frame
		mainPanel.add(self.centerPanel,BorderLayout.CENTER)
		
	def getMainPanel(self):
		return self.mainPanel
		 
	def getMessageTextField(self):
		return self.messageTextField

class Event_Monitor_Document:
	""" This is a place where you put everything that it is yours logic and GUI """
	def __init__(self):
		self.event_monitor_window = null
		#----  Controllers ----------------------
		self.monitor_controller = Monitor_Controller(self)
		self.config_controller = Config_Controller(self)	
		self.app_mane_panel = self.monitor_controller.getMainPanel()

	def setWindow(self,event_monitor_window):
		self.event_monitor_window = event_monitor_window
		self.event_monitor_window.getMainPanel().add(self.app_mane_panel)		
		
	def getWindow(self):
		return self.event_monitor_window
		
	def getMessageTextField(self):
		if(self.event_monitor_window != null):
			return self.event_monitor_window.getMessageTextField()
		else:
			return null

#-------------------------------------------------
# SUBCLASSES of XAL Accelerator Framework
#-------------------------------------------------

#-------------------------------------------------
#        DOCUMENT Class
#-------------------------------------------------	
class Event_Monitor_OpenXAL_Document(AcceleratorDocument):
	def __init__(self,url = null):
		self.mainPanel = JPanel(BorderLayout())

		#==== set up accelerator 
		if(not self.loadDefaultAccelerator()):
			self.applySelectedAcceleratorWithDefaultPath("/default/main.xal")
			
		self.event_monitor_document = Event_Monitor_Document()
		self.event_monitor_window = Event_Monitor_Window(self.event_monitor_document)
		
		if(url != null):
			self.setSource(url)
			self.readEvent_Monitor_Document(url)
			#super class method - will show "Save" menu active
			if(url.getProtocol().find("jar") >= 0):
				self.setHasChanges(false)
			else:
				self.setHasChanges(true)
				
	def makeMainWindow(self):
		self.mainWindow = Event_Monitor_OpenXAL_Window(self)
		self.mainWindow.getContentPane().setLayout(BorderLayout())
		self.mainWindow.getContentPane().add(self.mainPanel,BorderLayout.CENTER)			
		self.event_monitor_window.setFrame(self.mainWindow,self.mainPanel)
		self.event_monitor_document.setWindow(self.event_monitor_window)
		self.mainWindow.setSize(Dimension(800, 600))

	def saveDocumentAs(	self,url):
		# here you save of the application to the XML file 
		self.event_monitor_document.config_controller.writeData(url)
	
	def readEvent_Monitor_Document(self,url):
		# here you put the initialization of the application from the XML file 
		self.event_monitor_document.config_controller.readData(url)
		
#--------------------------------------------------
#        WINDOW Class
#--------------------------------------------------				
class Event_Monitor_OpenXAL_Window(AcceleratorWindow):
	def __init__(self,event_monitor_openxal_document):
		AcceleratorWindow.__init__(self,event_monitor_openxal_document)
		
	def getMainPanel(self):
		return self.document.mainPanel
	
#--------------------------------------------------
#        MAIN Class
#--------------------------------------------------
class Event_Monitor_OpenXAL_Main(ApplicationAdaptor):
	def __init__(self):
		ApplicationAdaptor.__init__(self)
		script_dir = os.path.dirname(os.path.realpath(__file__))
		self.setResourcesParentDirectoryWithPath(script_dir)
	
	def readableDocumentTypes(self):
		return ["evnt",]
		
	def writableDocumentTypes(self):
		return self.readableDocumentTypes()

	def newEmptyDocument(self, *args):
		if len( args ) > 0:
			return ApplicationAdaptor.newEmptyDocument(self,*args)
		else:
			return self.newDocument(null)

	def newDocument(self,location):
		return Event_Monitor_OpenXAL_Document(location)

	def applicationName(self):
		return "Event Monitor Application"
				
AcceleratorApplication.launch(Event_Monitor_OpenXAL_Main())

