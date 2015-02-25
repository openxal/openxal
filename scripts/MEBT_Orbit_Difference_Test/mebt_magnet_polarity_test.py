# This script is MEBT Orbit Difference and Polarity Tests OpenXAL Application

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
from lib.main_orbit_diff_cntrl_lib import MEBT_Main_Orbit_Diff_Controller

false = Boolean("false").booleanValue()
true = Boolean("true").booleanValue()
null = None

#-------------------------------------------------------------------
# Local Classes that are not subclasses of XAL Accelerator Framework
#-------------------------------------------------------------------

class MEBT_Magnet_Polarity_Window:
	def __init__(self,mebt_magnet_polarity_document):
		#--- mebt_magnet_polarity_document the parent document for all controllers
		self.mebt_magnet_polarity_document = mebt_magnet_polarity_document
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

class MEBT_Magnet_Polarity_Document:
	""" This is a place where you put everything that it is yours logic and GUI """
	def __init__(self,accl):
		self.mebt_magnet_polarity_window = null
		#---- place to create all subcontrollers
		self.mebt_main_orbit_diff_cntrl = MEBT_Main_Orbit_Diff_Controller(self,accl)
		#--------fill out the tabbed panel
		self.tabbedPane = JTabbedPane()		
		self.tabbedPane.add("MEBT Orbit Diff.",self.mebt_main_orbit_diff_cntrl.getMainPanel())

	def setWindow(self,mebt_magnet_polarity_window):
		self.mebt_magnet_polarity_window = mebt_magnet_polarity_window
		self.mebt_magnet_polarity_window.getMainPanel().add(self.tabbedPane,BorderLayout.CENTER)		
		
	def getWindow(self):
		return self.mebt_magnet_polarity_window
		
	def getMessageTextField(self):
		if(self.mebt_magnet_polarity_window != null):
			return self.mebt_magnet_polarity_window.getMessageTextField()
		else:
			return null

#-------------------------------------------------
# SUBCLASSES of XAL Accelerator Framework
#-------------------------------------------------

#-------------------------------------------------
#        DOCUMENT Class
#-------------------------------------------------	
class MEBT_Magnet_Polarity_OpenXAL_Document(AcceleratorDocument):
	def __init__(self,url = null):
		self.mainPanel = JPanel(BorderLayout())

		#==== set up accelerator 
		if(not self.loadDefaultAccelerator()):
			self.applySelectedAcceleratorWithDefaultPath("/default/main.xal")
			
		self.mebt_magnet_polarity_document = MEBT_Magnet_Polarity_Document(self.getAccelerator())
		self.mebt_magnet_polarity_window = MEBT_Magnet_Polarity_Window(self.mebt_magnet_polarity_document)
		
		if(url != null):
			self.setSource(url)
			self.readMEBT_Magnet_Polarity_Document(url)
			#super class method - will show "Save" menu active
			if(url.getProtocol().find("jar") >= 0):
				self.setHasChanges(false)
			else:
				self.setHasChanges(true)
				
	def makeMainWindow(self):
		self.mainWindow = MEBT_Magnet_Polarity_OpenXAL_Window(self)
		self.mainWindow.getContentPane().setLayout(BorderLayout())
		self.mainWindow.getContentPane().add(self.mainPanel,BorderLayout.CENTER)			
		self.mebt_magnet_polarity_window.setFrame(self.mainWindow,self.mainPanel)
		self.mebt_magnet_polarity_document.setWindow(self.mebt_magnet_polarity_window)
		self.mainWindow.setSize(Dimension(800, 600))

	def saveDocumentAs(	self,url):
		# here you save of the application to the XML file 
		pass
	
	def readMEBT_Magnet_Polarity_Document(self,url):
		# here you put the initialization of the application from the XML file 
		pass
		
#--------------------------------------------------
#        WINDOW Class
#--------------------------------------------------				
class MEBT_Magnet_Polarity_OpenXAL_Window(AcceleratorWindow):
	def __init__(self,mebt_magnet_polarity_openxal_document):
		AcceleratorWindow.__init__(self,mebt_magnet_polarity_openxal_document)
		
	def getMainPanel(self):
		return self.document.mainPanel
	
#--------------------------------------------------
#        MAIN Class
#--------------------------------------------------
class MEBT_Magnet_Polarity_OpenXAL_Main(ApplicationAdaptor):
	def __init__(self):
		ApplicationAdaptor.__init__(self)
		script_dir = os.path.dirname(os.path.realpath(__file__))
		self.setResourcesParentDirectoryWithPath(script_dir)
	
	def readableDocumentTypes(self):
		return ["mebt",]
		
	def writableDocumentTypes(self):
		return self.readableDocumentTypes()

	def newEmptyDocument(self, *args):
		if len( args ) > 0:
			return ApplicationAdaptor.newEmptyDocument(self,*args)
		else:
			return self.newDocument(null)

	def newDocument(self,location):
		return MEBT_Magnet_Polarity_OpenXAL_Document(location)

	def applicationName(self):
		return "MEBT Magnet Polarity Test"
				
AcceleratorApplication.launch(MEBT_Magnet_Polarity_OpenXAL_Main())

