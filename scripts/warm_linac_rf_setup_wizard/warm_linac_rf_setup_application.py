# This script is the Warm Linac SetUp OpenXAL Application

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

from xal.tools.xml import XmlDataAdaptor

#-----local jython libraries import----
from lib.time_and_date_lib import DateAndTimeText
from lib.main_loop_controller_lib import Main_Loop_Controller
from lib.rf_net_power_cotroller_lib import RF_NET_Power_Controller

false = Boolean("false").booleanValue()
true = Boolean("true").booleanValue()
null = None

#-------------------------------------------------------------------
# Local Classes that are not subclasses of XAL Accelerator Framework
#-------------------------------------------------------------------

class Warm_Linac_RF_SetUp_Window:
	def __init__(self,warm_linac_rf_setup_document):
		#--- warm_linac_rf_setup_document the parent document for all controllers
		self.warm_linac_rf_setup_document = warm_linac_rf_setup_document
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

class Warm_Linac_RF_SetUp_Document:
	""" This is a place where you put everything that it is yours logic and GUI """
	def __init__(self,accl):
		self.warm_linac_rf_setup_window = null
		self.mainPanel = JTabbedPane()
		#---- place to create all subcontrollers
		self.main_loop_controller = Main_Loop_Controller(self,accl)
		self.rf_power_controller = RF_NET_Power_Controller(self,self.main_loop_controller)
		#----------------------------------------------------------
		self.mainPanel.add("Set RF Power",self.rf_power_controller.getMainPanel())
		self.mainPanel.add("PASTA Scans",self.main_loop_controller.getMainPanel())
	
	def setWindow(self,warm_linac_rf_setup_window):
		self.warm_linac_rf_setup_window = warm_linac_rf_setup_window
		self.warm_linac_rf_setup_window.getMainPanel().add(self.mainPanel,BorderLayout.CENTER)		
		
	def getWindow(self):
		return self.warm_linac_rf_setup_window
		
	def getMainPanel(self):
		return self.mainPanel		
		
	def getMessageTextField(self):
		if(self.warm_linac_rf_setup_window != null):
			return self.warm_linac_rf_setup_window.getMessageTextField()
		else:
			return null

#-------------------------------------------------
# SUBCLASSES of XAL Accelerator Framework
#-------------------------------------------------

#-------------------------------------------------
#        DOCUMENT Class
#-------------------------------------------------	
class Warm_Linac_RF_SetUp_OpenXAL_Document(AcceleratorDocument):
	def __init__(self,url = null):
		self.mainPanel = JPanel(BorderLayout())

		#==== set up accelerator 
		if(not self.loadDefaultAccelerator()):
			self.applySelectedAcceleratorWithDefaultPath("/default/main.xal")
			
		self.warm_linac_rf_setup_document = Warm_Linac_RF_SetUp_Document(self.getAccelerator())
		self.warm_linac_rf_setup_window = Warm_Linac_RF_SetUp_Window(self.warm_linac_rf_setup_document)
		
		if(url != null):
			self.setSource(url)
			self.readWarm_Linac_RF_SetUp_Document(url)
			#super class method - will show "Save" menu active
			if(url.getProtocol().find("jar") >= 0):
				self.setHasChanges(false)
			else:
				self.setHasChanges(true)
				
	def makeMainWindow(self):
		self.mainWindow = Warm_Linac_RF_SetUp_OpenXAL_Window(self)
		self.mainWindow.getContentPane().setLayout(BorderLayout())
		self.mainWindow.getContentPane().add(self.mainPanel,BorderLayout.CENTER)			
		self.warm_linac_rf_setup_window.setFrame(self.mainWindow,self.mainPanel)
		self.warm_linac_rf_setup_document.setWindow(self.warm_linac_rf_setup_window)
		self.mainWindow.setSize(Dimension(800, 600))

	def saveDocumentAs(	self,url):
		# here you save of the application to the XML file 
		da = XmlDataAdaptor.newEmptyDocumentAdaptor()
		root_da = da.createChild("Warm_Linac_SetUp")	
		self.warm_linac_rf_setup_document.main_loop_controller.writeDataToXML(root_da)
		da.writeToUrl(url)
	
	def readWarm_Linac_RF_SetUp_Document(self,url):
		# here you put the initialization of the application from the XML file 
		da = XmlDataAdaptor.adaptorForUrl(url,false)
		root_da = da.childAdaptor("Warm_Linac_SetUp")		
		self.warm_linac_rf_setup_document.main_loop_controller.readDataFromXML(root_da)
		
#--------------------------------------------------
#        WINDOW Class
#--------------------------------------------------				
class Warm_Linac_RF_SetUp_OpenXAL_Window(AcceleratorWindow):
	def __init__(self,warm_linac_rf_setup_openxal_document):
		AcceleratorWindow.__init__(self,warm_linac_rf_setup_openxal_document)
		
	def getMainPanel(self):
		return self.document.mainPanel
	
#--------------------------------------------------
#        MAIN Class
#--------------------------------------------------
class Warm_Linac_RF_SetUp_OpenXAL_Main(ApplicationAdaptor):
	def __init__(self):
		ApplicationAdaptor.__init__(self)
		script_dir = os.path.dirname(os.path.realpath(__file__))
		self.setResourcesParentDirectoryWithPath(script_dir)
	
	def readableDocumentTypes(self):
		return ["wlwz",]
		
	def writableDocumentTypes(self):
		return self.readableDocumentTypes()

	def newEmptyDocument(self, *args):
		if len( args ) > 0:
			return ApplicationAdaptor.newEmptyDocument(self,*args)
		else:
			return self.newDocument(null)

	def newDocument(self,location):
		return Warm_Linac_RF_SetUp_OpenXAL_Document(location)

	def applicationName(self):
		return "Warm Linac RF SetUp Wizard"
				
AcceleratorApplication.launch(Warm_Linac_RF_SetUp_OpenXAL_Main())

