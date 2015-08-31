# The SCL Longitudinal Tune-Up - Laser Stripping Experiment Setup
import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing import JTable
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
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
#           Auxiliary Laser Stripping classes and functions
#------------------------------------------------------------------------	

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
#------------------------------------------------
#  JTable models
#------------------------------------------------


#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
				
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Laser_Stripping_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		#------table panel --------
		center_panel = JPanel(BorderLayout())
		#-------- bottom actions panel
		bottom_panel = JPanel(BorderLayout())
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		#self.???table.getModel().fireTableDataChanged()
		pass
