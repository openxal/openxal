# This script is SCL Tune Wizard OPEN XAL app. It performs the SCL RF phase scan.

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
from lib.ws_lw_acquisition_cntrl_lib import WS_DIRECTION_HOR, WS_DIRECTION_VER, WS_DIRECTION_NULL
from lib.ws_lw_acquisition_cntrl_lib import WS_LW_Acquisition_Controller
from lib.linac_wizard_read_write_file_lib import WS_Wizard_IO_Controller, WS_LW_Data_Write_Listener, WS_LW_Data_Read_Listener
from lib.linac_setup_cntrl_lib import LINAC_SetUp_Controller
from lib.transverse_twiss_analysis_cntrl_lib import Transverse_Twiss_Analysis_Controller
from lib.scl_long_tuneup_cntrl_lib import SCL_Long_TuneUp_Controller
import lib.constants_lib

false = Boolean("false").booleanValue()
true = Boolean("true").booleanValue()
null = None

#window closer will kill this apps 
class WindowCloser(WindowAdapter):
	def windowClosing(self,windowEvent):
		sys.exit(1)

#-------------------------------------------------------------------
# Local Classes that are not subclasses of XAL Accelerator Framework
#-------------------------------------------------------------------

class LINAC_Wizard_Window:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document
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

class LINAC_Wizard_Document:
	def __init__(self,accl):
		self.accl = accl
		self.accSeq = null
		self.linac_wizard_window = null
		self.linac_setup_controller = LINAC_SetUp_Controller(self)
		self.ws_lw_controller = WS_LW_Acquisition_Controller(self)
		self.io_controller = WS_Wizard_IO_Controller(self)
		self.tr_twiss_analysis_controller = Transverse_Twiss_Analysis_Controller(self)
		self.scl_long_tuneup_controller = SCL_Long_TuneUp_Controller(self)
		#--------fill out the tabbed panel
		self.tabbedPane = JTabbedPane()		
		self.tabbedPane.add("Acc. Seq. SetUp",self.linac_setup_controller.getMainPanel())
		self.tabbedPane0 = JTabbedPane()
		self.tabbedPane0.add("WS,LW Data Acquisition",self.ws_lw_controller.getMainPanel())
		self.tabbedPane0.add("Transv. Twiss Analysis",self.tr_twiss_analysis_controller.getMainPanel())
		self.tabbedPane.add("Transverse Twiss",self.tabbedPane0)
		self.tabbedPane.add("SCL Long. TuneUp",self.scl_long_tuneup_controller.getMainPanel())
		
	def setLINAC_Wizard_Window(self,linac_wizard_window):
		self.linac_wizard_window = linac_wizard_window
		self.linac_wizard_window.getMainPanel().add(self.tabbedPane,BorderLayout.CENTER)		
		
	def getMessageTextField(self):
		if(self.linac_wizard_window != null):
			return self.linac_wizard_window.getMessageTextField()
		else:
			return null
		
	def getWS_LW_Controller(self):
		return self.ws_lw_controller
		
	def getIO_Controller(self):
		return self.io_controller
		
	def getSetUp_Controller(self):
		return self.linac_setup_controller
		
	def setAccSeq(self,accSeq):
		self.accSeq = accSeq
		self.ws_lw_controller.setAccSeq(accSeq)
		
	def getAccSeq(self):
		return self.accSeq 
		
	def getAccl(self):
		return self.accl

#-------------------------------------------------
# SUBCLASSES of XAL Accelerator Framework
#-------------------------------------------------

#-------------------------------------------------
#        DOCUMENT Class
#-------------------------------------------------	
class SCL_Wizard_Document(AcceleratorDocument):
	def __init__(self,url = null):
		self.mainPanel = JPanel(BorderLayout())

		#==== set up accelerator 
		if(not self.loadDefaultAccelerator()):
			self.applySelectedAcceleratorWithDefaultPath("/default/main.xal")
			
		self.linac_wizard_document = LINAC_Wizard_Document(self.getAccelerator())
		self.linac_wizard_window = LINAC_Wizard_Window(self.linac_wizard_document)
		
		if(url != null):
			self.setSource(url)
			self.readSCL_Wizard_Document(url)
			#super class method - will show "Save" menu active
			if(url.getProtocol().find("jar") >= 0):
				self.setHasChanges(false)
			else:
				self.setHasChanges(true)
			
		#----- set up path for the data		
		lib.constants_lib.const_path_dict["XAL_XML_ACC_FILES_DIRS_PATH"] = self.getAcceleratorFilePath()
		lib.constants_lib.const_path_dict["OPENXAL_XML_ACC_FILES_DIRS_PATH"] = self.getAcceleratorFilePath()
		lib.constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"] = self.getDisplayFilePath()
				
	def makeMainWindow(self):
		self.mainWindow = SCL_Wizard_Window(self)
		self.mainWindow.getContentPane().setLayout(BorderLayout())
		self.mainWindow.getContentPane().add(self.mainPanel,BorderLayout.CENTER)			
		self.linac_wizard_window.setFrame(self.mainWindow,self.mainPanel)
		self.linac_wizard_document.setLINAC_Wizard_Window(self.linac_wizard_window)
		self.mainWindow.setSize(Dimension(800, 600))

	def saveDocumentAs(	self,url):
		io_controller = self.linac_wizard_document.getIO_Controller()
		io_controller.writeData(url)	
	
	def readSCL_Wizard_Document(self,url):
		io_controller = self.linac_wizard_document.getIO_Controller()
		io_controller.readData(url)
		
#--------------------------------------------------
#        WINDOW Class
#--------------------------------------------------				
class SCL_Wizard_Window(AcceleratorWindow):
	def __init__(self,scl_wizard_document):
		AcceleratorWindow.__init__(self,scl_wizard_document)
		
	def getMainPanel(self):
		return self.document.mainPanel
	
#--------------------------------------------------
#        MAIN Class
#--------------------------------------------------
class SCL_Wizard_Main(ApplicationAdaptor):
	def __init__(self):
		ApplicationAdaptor.__init__(self)
		script_dir = os.path.dirname(os.path.realpath(__file__))
		self.setResourcesParentDirectoryWithPath(script_dir)
	
	def readableDocumentTypes(self):
		return ["sclw",]
		
	def writableDocumentTypes(self):
		return self.readableDocumentTypes()

	def newEmptyDocument(self, *args):
		if len( args ) > 0:
			return ApplicationAdaptor.newEmptyDocument(self,*args)
		else:
			return self.newDocument(null)

	def newDocument(self,location):
		return SCL_Wizard_Document(location)

	def applicationName(self):
		return "SCL Wizard"
				
AcceleratorApplication.launch(SCL_Wizard_Main())

