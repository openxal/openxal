# The main MEBT orbit difference controller library

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
from java.util import Date
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.util import ArrayList
from java.awt import Dimension
from java.io import File, BufferedReader, InputStreamReader, FileInputStream
from javax.swing.filechooser import FileNameExtensionFilter
from java.beans import PropertyChangeListener

from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf import AcceleratorSeqCombo
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap, BPM
from xal.smf.data import XMLDataManager
from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory

from xal.extension.widgets.swing import DoubleInputTextField
from xal.tools.text import FortranNumberFormat

from xal.extension.widgets.swing import Wheelswitch

from constants_lib import GRAPH_LEGEND_KEY
import constants_lib
from functions_lib import calculateAvgErr

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#   Auxiliary Classes and Functions
#------------------------------------------------------------------------
class Magnet_Wrapper:
	def __init__(self,magnet,pos):
		self.magnet = magnet
		self.pos = pos		
		self.field = self.magnet.getDesignField()
		self.design_field = self.field
		self.inverted = false
		
	def readField(self):
		if(not self.magnet.isPermanent()):
			self.field = self.magnet.getFieldReadback()
			if(self.inverted):
				self.field = - self.field
		return self.field
		
	def getField(self):
		return self.field
		
	def setField(self,field):
		self.field = field
		if(self.inverted):
			self.field = - self.field
		return self.field		
		
	def setRawField(self,field):
		self.field = field
	
	def setInverted(self,inverted):
		inverted_old = self.inverted
		self.inverted = inverted
		if(self.inverted != inverted_old):
			self.field = - self.field		
			
	def scale(self,coeff):
		self.field = self.design_field*coeff
		if(self.inverted):
			self.field = - self.field		
		
	def getUpperFieldLimit(self):
		return self.magnet.upperFieldLimit()
		
	def setFiledToDesign(self):
		self.magnet.setDfltField(self.field)
		
	def setFieldToEPICS(self,field):
		self.magnet.setField(field)
		#---- ??????
		#return
		
	
class Cavity_Wrapper:
	def __init__(self,cav,pos):
		self.cav = cav
		self.pos = pos
		self.design_amp = cav.getDfltCavAmp()
		
	def scale(self,coeff):
		self.cav.updateDesignAmp(self.design_amp*coeff)	
		
class BPM_Wrapper:
	def __init__(self,bpm,pos):
		self.bpm = bpm	
		self.pos = pos
		self.use = true
		self.invertedX = false 		
		self.invertedY = false 		
		self.x_arr = []
		self.y_arr = []
		self.x_avg = 0.
		self.y_avg = 0.
		self.x_err = 0.
		self.y_err = 0.
		
	def init(self):
		self.x_arr = []
		self.y_arr = []
		self.x_avg = 0.
		self.y_avg = 0.
		self.x_err = 0.
		self.y_err = 0.
			
	def accountSignal(self):
		x = self.bpm.getXAvg()
		y = self.bpm.getYAvg()
		#---- ??????
		#x = random.gauss(1.0,0.5)
		#y = random.gauss(-2.0,0.5)
		self.x_arr.append(x)
		self.y_arr.append(y)
		
	def calcStatistics(self):
		(self.x_avg,self.x_err) = calculateAvgErr(self.x_arr)
		(self.y_avg,self.y_err) = calculateAvgErr(self.y_arr)
		if(self.invertedX):
			self.x_avg = - self.x_avg
		if(self.invertedY):
			self.y_avg = - self.y_avg			
		
	def setInvertedX(self,invertedX):
		invertedX_old = self.invertedX
		self.invertedX = invertedX
		if(self.invertedX != invertedX_old):
			self.x_avg = - self.x_avg	
			
	def setInvertedY(self,invertedY):	
		invertedY_old = self.invertedY
		self.invertedY = invertedY
		if(self.invertedY != invertedY_old):
			self.y_avg = - self.y_avg	
		
class Model_Orbit_Holder:
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl	
		self.accSeq = self.mebt_main_orbit_diff_cntrl.accSeq
		self.scenario = Scenario.newScenarioFor(self.accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.unsetStartNode()
		self.scenario.unsetStopNode()			
		self.part_tracker = AlgorithmFactory.createParticleTracker(self.accSeq)
		self.part_tracker.setRfGapPhaseCalculation(true)
		self.part_probe_init = ProbeFactory.createParticleProbe(self.accSeq,self.part_tracker)
		self.scenario.resync()
		part_probe = ParticleProbe(self.part_probe_init)
		self.scenario.setProbe(part_probe)
		self.scenario.run()
		self.traj = self.scenario.getTrajectory()	
		
	def runModel(self):
		quad_wrappers = self.mebt_main_orbit_diff_cntrl.quad_wrappers
		for quad_wrapper in quad_wrappers:
			quad_wrapper.magnet.setDfltField(quad_wrapper.field)
		dc_wrappers = self.mebt_main_orbit_diff_cntrl.dc_wrappers
		for dc_wrapper in dc_wrappers:
			dc_wrapper.magnet.setDfltField(dc_wrapper.field)
		#"""
		self.scenario = Scenario.newScenarioFor(self.accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.unsetStartNode()
		self.scenario.unsetStopNode()			
		#"""	
		self.scenario.resync()
		part_probe = ParticleProbe(self.part_probe_init)
		self.scenario.setProbe(part_probe)
		self.scenario.run()
		self.traj = self.scenario.getTrajectory()
	
	def getTrajectory(self):
		return self.traj
		
class BPM_Orbit_Holder:
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.bpm_wrappers = self.mebt_main_orbit_diff_cntrl.bpm_wrappers
		#----- self.bpm_x_y_dict[bpm_wrapper] = [[x_avg,x_err],[y_avg,y_err]]
		self.bpm_x_y_dict = {}
		for bpm_wrapper in self.bpm_wrappers:
			self.bpm_x_y_dict[bpm_wrapper] = [[0.,0.],[0.,0.]]
			
	def init(self):
		for bpm_wrapper in self.bpm_wrappers:
			bpm_wrapper.init()		
			
	def accountSignal(self):
		for bpm_wrapper in self.bpm_wrappers:
			bpm_wrapper.accountSignal()
			
	def calcStatistics(self):
		for bpm_wrapper in self.bpm_wrappers:
			bpm_wrapper.calcStatistics()	
			self.bpm_x_y_dict[bpm_wrapper] = [[bpm_wrapper.x_avg,bpm_wrapper.x_err],[bpm_wrapper.y_avg,bpm_wrapper.y_err]]

	def getXY_and_Err(self,bpm_wrapper):
		[[x_avg,x_err],[y_avg,y_err]]	= 	self.bpm_x_y_dict[bpm_wrapper]
		return (x_avg,x_err,y_avg,y_err)


class Orbit_Measurer:
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.mebt_orbit_holder_0 = Model_Orbit_Holder(self.mebt_main_orbit_diff_cntrl)
		self.mebt_orbit_holder_1 = Model_Orbit_Holder(self.mebt_main_orbit_diff_cntrl)
		self.bpm_orbit_holder_0 = BPM_Orbit_Holder(self.mebt_main_orbit_diff_cntrl)
		self.bpm_orbit_holder_1 = BPM_Orbit_Holder(self.mebt_main_orbit_diff_cntrl)
		
	def init(self,index):
		if(index == 0):
			self.bpm_orbit_holder_0.init()
		else:
			self.bpm_orbit_holder_1.init()		
		
	def accountSignal(self,index):
		if(index == 0):
			self.bpm_orbit_holder_0.accountSignal()
		else:
			self.bpm_orbit_holder_1.accountSignal()
			
	def calcStatistics(self,index):
		if(index == 0):
			self.bpm_orbit_holder_0.	calcStatistics()
		else:
			self.bpm_orbit_holder_1.	calcStatistics()
			
	def runModel(self,index):
		if(index == 0):
			self.mebt_orbit_holder_0.runModel()
		else:
			self.mebt_orbit_holder_1.runModel()
			
class Measure_Runner(Runnable):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def run(self):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")		
		self.mebt_main_orbit_diff_cntrl.measure_running = true
		status_txt = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel.status_txt
		status_txt.setText("Running.")
		self.execute_run()
		self.mebt_main_orbit_diff_cntrl.measure_running = false
		status_txt.setText("Not running.")
		magnet_and_bpm_panel = self.mebt_main_orbit_diff_cntrl.magnet_and_bpm_panel
		magnet_and_bpm_panel.bpm_table.getModel().fireTableDataChanged()		
		
		
	def execute_run(self):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		self.mebt_main_orbit_diff_cntrl.measure_running = true
		#print "debug start running!"
		dc_index = self.mebt_main_orbit_diff_cntrl.magnet_and_bpm_panel.dc_table.getSelectedRow()
		if(dc_index < 0): 
			messageTextField.setText("Please select Dipole Corrector from the table first!")
			return
		dc_wrapper = self.mebt_main_orbit_diff_cntrl.dc_wrappers[dc_index]
		orbit_measurer_cotroller_panel = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel
		dc_field0 = orbit_measurer_cotroller_panel.orbit_index_info_panel_0.dc_filed_txt.getValue()
		dc_field1 = orbit_measurer_cotroller_panel.orbit_index_info_panel_1.dc_filed_txt.getValue()
		n_avg = int(orbit_measurer_cotroller_panel.avg_number_txt.getValue())
		if(n_avg <= 0): return
		time_step = orbit_measurer_cotroller_panel.time_step_txt.getValue()
		status_txt = orbit_measurer_cotroller_panel.status_txt
		orbit_measurer = self.mebt_main_orbit_diff_cntrl.orbit_measurer
		#---------------------------------------------
		count = 0
		if(self.mebt_main_orbit_diff_cntrl.measure_running):		
			dc_wrapper.setFieldToEPICS(dc_field0)
			orbit_measurer.init(0)
			for it in range(n_avg):
				time.sleep(time_step)
				if(not self.mebt_main_orbit_diff_cntrl.measure_running): break
				count += 1
				status_txt.setText("Running! Step "+str(count)+" out of "+str(2*n_avg))
				orbit_measurer.accountSignal(0)
			if(self.mebt_main_orbit_diff_cntrl.measure_running):	
				orbit_measurer.calcStatistics(0)
				dc_wrapper.setFieldToEPICS(dc_field1)
				time.sleep(time_step)
				if(self.mebt_main_orbit_diff_cntrl.measure_running):
					orbit_measurer.init(1)
					for it in range(n_avg):
						time.sleep(time_step)
						if(not self.mebt_main_orbit_diff_cntrl.measure_running): break
						count += 1
						status_txt.setText("Running! Step "+str(count)+" out of "+str(2*n_avg))
						orbit_measurer.accountSignal(1)
					orbit_measurer.calcStatistics(1)
		#--------------------------------	
		field_old = dc_wrapper.getField()
		dc_wrapper.setField(dc_field0)
		orbit_measurer.runModel(0)
		dc_wrapper.setField(dc_field1)
		orbit_measurer.runModel(1)
		dc_wrapper.setRawField(field_old)
		self.mebt_main_orbit_diff_cntrl.orbit_diff_graphs_panel.updateGraphData()		
		
#------------------------------------------------
#  JTable models
#------------------------------------------------	
class Magnet_Table_Model(AbstractTableModel):
	def __init__(self,mebt_main_orbit_diff_cntrl,magnet_wrappers):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.magnet_wrappers = magnet_wrappers
		self.columnNames = ["Magnet","Z[m]","Filed","Rev. Polarity"]	
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.magnet_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		magnet_wrapper = self.magnet_wrappers[row]
		if(col == 0): return magnet_wrapper.magnet.getId()
		if(col == 1): return "%7.3f"%magnet_wrapper.pos
		if(col == 2): return "%9.6f"%magnet_wrapper.field
		if(col == 3): return magnet_wrapper.inverted	
		return ""
				
	def getColumnClass(self,col):
		if(col == 3):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(col == 3):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		magnet_wrapper = self.magnet_wrappers[row]
		if(col == 3):
			magnet_wrapper.setInverted(value)
			
			
class BPM_Table_Model(AbstractTableModel):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.columnNames = ["BPM","Z[m]","X[mm]","Y[mm]","Rev. X","Rev. Y"," Use "]	
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.mebt_main_orbit_diff_cntrl.bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.mebt_main_orbit_diff_cntrl.bpm_wrappers[row]
		if(col == 0): return bpm_wrapper.bpm.getId()
		if(col == 1): return "%7.3f"%bpm_wrapper.pos
		if(col == 2): return "%4.1f"%bpm_wrapper.x_avg
		if(col == 3): return "%4.1f"%bpm_wrapper.y_avg
		if(col == 4): return bpm_wrapper.invertedX	
		if(col == 5): return bpm_wrapper.invertedY	
		if(col == 6): return bpm_wrapper.use
		return ""
				
	def getColumnClass(self,col):
		if(col == 4 or col == 5 or col == 6):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(col == 4 or col == 5 or col == 6):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		bpm_wrapper = self.mebt_main_orbit_diff_cntrl.bpm_wrappers[row]
		if(col == 4):
			bpm_wrapper.setInvertedX(value)
		if(col == 5):
			bpm_wrapper.setInvertedY(value)
		if(col == 6):
			bpm_wrapper.use = value
			
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------			
class Magnet_and_BPM_Panel(JPanel):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		quad_titled_border = BorderFactory.createTitledBorder(etched_border,"Quads")
		dc_titled_border = BorderFactory.createTitledBorder(etched_border,"Dipole Correctors")
		bpm_titled_border = BorderFactory.createTitledBorder(etched_border,"BPMs")
		#-------------------------------------------
		quad_panel = JPanel(BorderLayout())
		dc_panel = JPanel(BorderLayout())
		bpm_panel = JPanel(BorderLayout())
		quad_panel.setBorder(quad_titled_border)
		dc_panel.setBorder(dc_titled_border)
		bpm_panel.setBorder(bpm_titled_border)
		self.quad_table = JTable(Magnet_Table_Model(self.mebt_main_orbit_diff_cntrl,self.mebt_main_orbit_diff_cntrl.quad_wrappers))
		self.dc_table = JTable(Magnet_Table_Model(self.mebt_main_orbit_diff_cntrl,self.mebt_main_orbit_diff_cntrl.dc_wrappers))
		self.quad_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.dc_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.bpm_table = JTable(BPM_Table_Model(self.mebt_main_orbit_diff_cntrl))
		self.bpm_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		quad_panel.add(JScrollPane(self.quad_table),BorderLayout.CENTER)
		dc_panel.add(JScrollPane(self.dc_table),BorderLayout.CENTER)
		bpm_panel.add(JScrollPane(self.bpm_table),BorderLayout.CENTER)
		#-------------------------------------------
		self.dc_table.getSelectionModel().addListSelectionListener(DCs_Table_Selection_Listener(self.mebt_main_orbit_diff_cntrl))	
		#-------------------------------------------
		read_magnets_button = JButton("Update Magnet Fields from EPICS")
		read_magnets_button.addActionListener(Read_Magnets_Button_Listener(self.mebt_main_orbit_diff_cntrl))
		button_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		button_panel.add(read_magnets_button)
		#-------------------------------------------	
		tables_panel = JPanel(GridLayout(3,1,3,3)) 
		tables_panel.add(quad_panel)
		tables_panel.add(dc_panel)
		tables_panel.add(bpm_panel)
		self.add(tables_panel,BorderLayout.CENTER)
		self.add(button_panel,BorderLayout.SOUTH)


class Correction_Coeffs_Panel(JPanel):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"Correction Coeffitients for DTL")
		self.setBorder(titled_border)
		#-------------------------------------------
		self.dtl_quad_coeff_wheel = Wheelswitch()
		self.dtl_quad_coeff_wheel.setFormat("+#.####")
		self.dtl_quad_coeff_wheel.setValue(1.)
		self.dtl_quad_coeff_wheel.addPropertyChangeListener("value", DTL_Quad_Coeffs_Wheel_Listener(self.mebt_main_orbit_diff_cntrl))			
		self.dtl_cav_coeff_wheel = Wheelswitch()
		self.dtl_cav_coeff_wheel.setFormat("+#.####")
		self.dtl_cav_coeff_wheel.setValue(1.)
		self.dtl_cav_coeff_wheel.addPropertyChangeListener("value", DTL_Cav_Coeffs_Wheel_Listener(self.mebt_main_orbit_diff_cntrl))	
		quad_label = JLabel("DTL Quad Filed Coeffs:",JLabel.RIGHT)
		cav_label = JLabel("      DTL Cav. Amp. Coeffs:",JLabel.RIGHT)
		self.add(quad_label)
		self.add(self.dtl_quad_coeff_wheel)
		self.add(cav_label)
		self.add(self.dtl_cav_coeff_wheel)

class Orbit_Diff_Graphs_Panel(JPanel):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.setLayout(BorderLayout())
		tabbedPane = JTabbedPane()
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---- plots for Hor-Ver and Longitudinal
		self.hor_plot = FunctionGraphsJPanel()
		self.hor_plot.setLegendButtonVisible(true)
		self.hor_plot.setChooseModeButtonVisible(true)
		self.hor_plot.setName("Horizontal Plane (Vert.Lns.: Black:quads Blue:DC Red:Cavs)")
		self.hor_plot.setAxisNames("Position, m","X[mm]")	
		self.hor_plot.setBorder(etched_border)		
		self.ver_plot = FunctionGraphsJPanel()
		self.ver_plot.setLegendButtonVisible(true)
		self.ver_plot.setChooseModeButtonVisible(true)
		self.ver_plot.setName("Vertical Plane (Vert.Lns.: Black:quads Blue:DC Red:Cavs)")
		self.ver_plot.setAxisNames("Position, m","Y[mm]")	
		self.ver_plot.setBorder(etched_border)		
		#------------------------------------------------
		self.hor_diff_plot = FunctionGraphsJPanel()
		self.hor_diff_plot.setLegendButtonVisible(true)
		self.hor_diff_plot.setChooseModeButtonVisible(true)
		self.hor_diff_plot.setName("Horizontal Plane (Vert.Lns.: Black:quads Blue:DC Red:Cavs)")
		self.hor_diff_plot.setAxisNames("Position, m","Diff X[mm]")	
		self.hor_diff_plot.setBorder(etched_border)		
		self.ver_diff_plot = FunctionGraphsJPanel()
		self.ver_diff_plot.setLegendButtonVisible(true)
		self.ver_diff_plot.setChooseModeButtonVisible(true)
		self.ver_diff_plot.setName("Vertical Plane (Vert.Lns.: Black:quads Blue:DC Red:Cavs)")
		self.ver_diff_plot.setAxisNames("Position, m","Diff Y[mm]")	
		self.ver_diff_plot.setBorder(etched_border)	
		#--------------------------------------------------------------------
		quad_wrappers = self.mebt_main_orbit_diff_cntrl.quad_wrappers
		dc_wrappers = self.mebt_main_orbit_diff_cntrl.dc_wrappers
		mebt_cav_wrappers = self.mebt_main_orbit_diff_cntrl.mebt_cav_wrappers
		for wrapper in quad_wrappers:
			self.hor_plot.addVerticalLine(wrapper.pos,Color.BLACK)
			self.ver_plot.addVerticalLine(wrapper.pos,Color.BLACK)
			self.hor_diff_plot.addVerticalLine(wrapper.pos,Color.BLACK)
			self.ver_diff_plot.addVerticalLine(wrapper.pos,Color.BLACK)
		for wrapper in dc_wrappers:
			self.hor_plot.addVerticalLine(wrapper.pos,Color.BLUE)
			self.ver_plot.addVerticalLine(wrapper.pos,Color.BLUE)
			self.hor_diff_plot.addVerticalLine(wrapper.pos,Color.BLUE)
			self.ver_diff_plot.addVerticalLine(wrapper.pos,Color.BLUE)
		for wrapper in mebt_cav_wrappers:
			self.hor_plot.addVerticalLine(wrapper.pos,Color.RED)
			self.ver_plot.addVerticalLine(wrapper.pos,Color.RED)
			self.hor_diff_plot.addVerticalLine(wrapper.pos,Color.RED)
			self.ver_diff_plot.addVerticalLine(wrapper.pos,Color.RED)
		#---------------------------------------------------------------------
		#--------------------------------------------------
		#---- panels
		graph_diff_panel = JPanel(GridLayout(2,1))
		graph_diff_panel.add(self.hor_diff_plot)
		graph_diff_panel.add(self.ver_diff_plot)
		#----------------------------------
		graph_panel = JPanel(GridLayout(2,1))
		graph_panel.add(self.hor_plot)
		graph_panel.add(self.ver_plot)
		#----------------------------------
		tabbedPane.add("Orbit Difference",graph_diff_panel)
		tabbedPane.add("Orbit",graph_panel)
		#-------------------------------------
		self.x_model_gd = BasicGraphData()
		self.x_model_gd.setLineThick(3)
		self.x_model_gd.setGraphPointSize(7)
		self.x_model_gd.setGraphColor(Color.BLUE)
		self.x_model_gd.setGraphProperty(GRAPH_LEGEND_KEY,"X Model [mm]")
		self.x_model_gd.setDrawLinesOn(true)
		self.x_model_gd.setDrawPointsOn(false)
		#-------------------------------------
		self.y_model_gd = BasicGraphData()
		self.y_model_gd.setLineThick(3)
		self.y_model_gd.setGraphPointSize(7)
		self.y_model_gd.setGraphColor(Color.RED)
		self.y_model_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Y Model [mm]")
		self.y_model_gd.setDrawLinesOn(true)
		self.y_model_gd.setDrawPointsOn(false)
		#-------------------------------------
		self.x_model_diff_gd = BasicGraphData()
		self.x_model_diff_gd.setLineThick(3)
		self.x_model_diff_gd.setGraphPointSize(7)
		self.x_model_diff_gd.setGraphColor(Color.BLUE)
		self.x_model_diff_gd.setGraphProperty(GRAPH_LEGEND_KEY,"X Diff Model [mm]")
		self.x_model_diff_gd.setDrawLinesOn(true)
		self.x_model_diff_gd.setDrawPointsOn(false)
		#-------------------------------------
		self.y_model_diff_gd = BasicGraphData()
		self.y_model_diff_gd.setLineThick(3)
		self.y_model_diff_gd.setGraphPointSize(7)
		self.y_model_diff_gd.setGraphColor(Color.RED)
		self.y_model_diff_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Y Diff Model [mm]")
		self.y_model_diff_gd.setDrawLinesOn(true)
		self.y_model_diff_gd.setDrawPointsOn(false)
		#-------------------------------------
		#-------------------------------------
		self.x_bpm_gd = BasicGraphData()
		self.x_bpm_gd.setLineThick(3)
		self.x_bpm_gd.setGraphPointSize(7)
		self.x_bpm_gd.setGraphColor(Color.BLUE)
		self.x_bpm_gd.setGraphProperty(GRAPH_LEGEND_KEY,"X BPM [mm]")
		self.x_bpm_gd.setDrawLinesOn(false)
		self.x_bpm_gd.setDrawPointsOn(true)
		#-------------------------------------
		self.y_bpm_gd = BasicGraphData()
		self.y_bpm_gd.setLineThick(3)
		self.y_bpm_gd.setGraphPointSize(7)
		self.y_bpm_gd.setGraphColor(Color.RED)
		self.y_bpm_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Y BPM [mm]")
		self.y_bpm_gd.setDrawLinesOn(false)
		self.y_bpm_gd.setDrawPointsOn(true)
		#-------------------------------------
		self.x_bpm_diff_gd = BasicGraphData()
		self.x_bpm_diff_gd.setLineThick(3)
		self.x_bpm_diff_gd.setGraphPointSize(7)
		self.x_bpm_diff_gd.setGraphColor(Color.BLUE)
		self.x_bpm_diff_gd.setGraphProperty(GRAPH_LEGEND_KEY,"X Diff BPM [mm]")
		self.x_bpm_diff_gd.setDrawLinesOn(false)
		self.x_bpm_diff_gd.setDrawPointsOn(true)
		#-------------------------------------
		self.y_bpm_diff_gd = BasicGraphData()
		self.y_bpm_diff_gd.setLineThick(3)
		self.y_bpm_diff_gd.setGraphPointSize(7)
		self.y_bpm_diff_gd.setGraphColor(Color.RED)
		self.y_bpm_diff_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Y Diff BPM [mm]")
		self.y_bpm_diff_gd.setDrawLinesOn(false)
		self.y_bpm_diff_gd.setDrawPointsOn(true)
		#-------------------------------------		
		self.index0_button = JRadioButton("Orbit #0")
		self.index1_button = JRadioButton("Orbit #1")
		self.button_group = ButtonGroup()
		self.button_group.add(self.index0_button)
		self.button_group.add(self.index1_button)
		self.index0_button.setSelected(true)
		replot_button = JButton("Replot Graphs")
		replot_button.addActionListener(Replot_Button_Listener(self.mebt_main_orbit_diff_cntrl))
		button_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		button_panel.add(self.index0_button)
		button_panel.add(self.index1_button)
		button_panel.add(replot_button)
		#-----------------------------------------------
		self.add(tabbedPane,BorderLayout.CENTER)
		self.add(button_panel,BorderLayout.SOUTH)
				
	def removeAllGraphData(self):
		self.hor_plot.removeAllGraphData()
		self.ver_plot.removeAllGraphData()
		self.hor_diff_plot.removeAllGraphData()
		self.ver_diff_plot.removeAllGraphData()
		#--------------------------------------
		self.x_model_gd.removeAllPoints()
		self.y_model_gd.removeAllPoints()
		self.x_model_diff_gd.removeAllPoints()
		self.y_model_diff_gd.removeAllPoints()
		#--------------------------------------
		self.x_bpm_gd.removeAllPoints()
		self.y_bpm_gd.removeAllPoints()
		self.x_bpm_diff_gd.removeAllPoints()
		self.y_bpm_diff_gd.removeAllPoints()
								
	def updateGraphData(self):
		self.removeAllGraphData()
		orb_index = 0
		if(self.index1_button.isSelected()):
			orb_index = 1
		#print "debug orb_index=",orb_index
		#==== update graph data from calculator and measurer
		orbit_measurer = self.mebt_main_orbit_diff_cntrl.orbit_measurer
		mebt_orbit_holder_0 = orbit_measurer.mebt_orbit_holder_0
		mebt_orbit_holder_1 = orbit_measurer.mebt_orbit_holder_1
		bpm_orbit_holder_0 = orbit_measurer.bpm_orbit_holder_0
		bpm_orbit_holder_1 = orbit_measurer.bpm_orbit_holder_1
		bpm_wrappers = self.mebt_main_orbit_diff_cntrl.bpm_wrappers
		max_pos = 0.
		for bpm_wrapper in bpm_wrappers:
			if(bpm_wrapper.use):
				if(max_pos < bpm_wrapper.pos):
					max_pos = bpm_wrapper.pos
		#----------------------------------------
		pos_step = 0.1
		pos_old = -1.
		traj0 = mebt_orbit_holder_0.getTrajectory()
		traj1 = mebt_orbit_holder_1.getTrajectory()
		for ind in range(traj0.numStates()):
			state0 = traj0.stateWithIndex(ind)
			pos = state0.getPosition()
			state1 = traj1.stateNearestPosition(pos)
			if(pos > (pos_old + pos_step) and pos_old < max_pos):
				x0 = state0.getPhaseCoordinates().getx()*1000.
				y0 = state0.getPhaseCoordinates().gety()*1000.
				x1 = state1.getPhaseCoordinates().getx()*1000.
				y1 = state1.getPhaseCoordinates().gety()*1000.
				pos_old = pos
				if(orb_index == 0):
					self.x_model_gd.addPoint(pos,x0)
					self.y_model_gd.addPoint(pos,y0)
				else:
					self.x_model_gd.addPoint(pos,x1)
					self.y_model_gd.addPoint(pos,y1)					
				self.x_model_diff_gd.addPoint(pos,x1-x0)
				self.y_model_diff_gd.addPoint(pos,y1-y0)
		#----------------------------------------------
		bpm_orbit_holder_0.calcStatistics()
		bpm_orbit_holder_1.calcStatistics()
		for bpm_wrapper in bpm_wrappers:
			if(bpm_wrapper.use):		
				(x0,x0_err,y0,y0_err) = bpm_orbit_holder_0.getXY_and_Err(bpm_wrapper)
				(x1,x1_err,y1,y1_err) = bpm_orbit_holder_1.getXY_and_Err(bpm_wrapper)
				if(orb_index == 0):
					self.x_bpm_gd.addPoint(bpm_wrapper.pos,x0,x0_err)
					self.y_bpm_gd.addPoint(bpm_wrapper.pos,y0,y0_err)
				else:
					self.x_bpm_gd.addPoint(bpm_wrapper.pos,x1,x1_err)
					self.y_bpm_gd.addPoint(bpm_wrapper.pos,y1,y1_err)					
				self.x_bpm_diff_gd.addPoint(bpm_wrapper.pos,x1-x0,math.sqrt(x0_err**2+x1_err**2))
				self.y_bpm_diff_gd.addPoint(bpm_wrapper.pos,y1-y0,math.sqrt(y0_err**2+y1_err**2))
		#-------------------------------------
		self.hor_plot.addGraphData(self.x_model_gd)
		self.hor_plot.addGraphData(self.x_bpm_gd)
		self.ver_plot.addGraphData(self.y_model_gd)
		self.ver_plot.addGraphData(self.y_bpm_gd)
		#-------------------------------------
		self.hor_diff_plot.addGraphData(self.x_model_diff_gd)
		self.hor_diff_plot.addGraphData(self.x_bpm_diff_gd)
		self.ver_diff_plot.addGraphData(self.y_model_diff_gd)
		self.ver_diff_plot.addGraphData(self.y_bpm_diff_gd)
		
				
class Orbit_Index_Info_Panel(JPanel):
	def __init__(self,mebt_main_orbit_diff_cntrl,index):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		label = JLabel("Orbit #"+str(index)+"  ",JLabel.LEFT)
		self.dc_label = JLabel("Dipole Corr.: none  ",JLabel.RIGHT)
		field_label = JLabel("  field[T]=",JLabel.RIGHT)
		self.dc_filed_txt = DoubleInputTextField(0.0,FortranNumberFormat("G8.5"),10)
		percent_label = JLabel("   % of Max Field=",JLabel.RIGHT)
		self.percent_txt = DoubleInputTextField(90.0,FortranNumberFormat("G3.0"),5)
		self.add(label)
		self.add(self.dc_label)
		self.add(field_label)
		self.add(self.dc_filed_txt)
		self.add(percent_label)
		self.add(self.percent_txt)
		
class Orbit_Measurer_Controller_Panel(JPanel):
		def __init__(self,mebt_main_orbit_diff_cntrl):
			self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
			self.setLayout(GridLayout(4,1,3,3))
			self.orbit_index_info_panel_0 = Orbit_Index_Info_Panel(self.mebt_main_orbit_diff_cntrl,0)
			self.orbit_index_info_panel_1 = Orbit_Index_Info_Panel(self.mebt_main_orbit_diff_cntrl,1)
			self.avg_label = JLabel("Number of Avg.=",JLabel.RIGHT)
			self.avg_number_txt = DoubleInputTextField(1.0,FortranNumberFormat("G3.0"),5)
			self.time_step_label = JLabel("  Time step[sec]=",JLabel.RIGHT)
			self.time_step_txt = DoubleInputTextField(2.0,FortranNumberFormat("G4.2"),5)
			start_measuring_button = JButton("Start Measuring")
			start_measuring_button.addActionListener(Start_Measuring_Button_Listener(self.mebt_main_orbit_diff_cntrl))
			stop_measuring_button = JButton("Stop")
			stop_measuring_button.addActionListener(Stop_Measuring_Button_Listener(self.mebt_main_orbit_diff_cntrl))
			recalculate_model_button = JButton("Recalculate Model")
			recalculate_model_button.addActionListener(Recalculate_Model_Button_Listener(self.mebt_main_orbit_diff_cntrl))			
			status_label = JLabel("Measuring Status:",JLabel.RIGHT)
			self.status_txt = JTextField(25)
			self.status_txt.setText("Not running.")
			self.status_txt.setForeground(Color.red)
			#------------------------------------------
			tmp_panel_1 = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
			tmp_panel_1.add(self.orbit_index_info_panel_0)
			tmp_panel_2 = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
			tmp_panel_2.add(self.orbit_index_info_panel_1)
			tmp_panel_3 = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
			tmp_panel_3.add(self.avg_label)
			tmp_panel_3.add(self.avg_number_txt)		
			tmp_panel_3.add(self.time_step_label)		
			tmp_panel_3.add(self.time_step_txt)		
			tmp_panel_3.add(start_measuring_button)		
			tmp_panel_3.add(stop_measuring_button)		
			tmp_panel_3.add(recalculate_model_button)
			tmp_panel_4 = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
			tmp_panel_4.add(status_label)
			tmp_panel_4.add(self.status_txt)
			self.add(tmp_panel_1)
			self.add(tmp_panel_2)
			self.add(tmp_panel_3)
			self.add(tmp_panel_4)
			
		def runModel(self):
			messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
			messageTextField.setText("")				
			dc_index = self.mebt_main_orbit_diff_cntrl.magnet_and_bpm_panel.dc_table.getSelectedRow()
			if(dc_index < 0): 
				messageTextField.setText("Please select Dipole Corrector from the table first!")	
				return
			dc_wrapper = self.mebt_main_orbit_diff_cntrl.dc_wrappers[dc_index]
			field_old = dc_wrapper.getField()			
			dc_field = self.orbit_index_info_panel_0.dc_filed_txt.getValue()
			dc_wrapper.setField(dc_field)
			orbit_measurer = self.mebt_main_orbit_diff_cntrl.orbit_measurer
			orbit_measurer.runModel(0)
			dc_field = self.orbit_index_info_panel_1.dc_filed_txt.getValue()
			dc_wrapper.setField(dc_field)
			orbit_measurer.runModel(1)
			dc_wrapper.setRawField(field_old)
			self.mebt_main_orbit_diff_cntrl.orbit_diff_graphs_panel.updateGraphData()
			
			
		
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------	
class Read_Magnets_Button_Listener(ActionListener):
	#This button will read magnets fields from the EPICS
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")	
		for quad_wrapper in self.mebt_main_orbit_diff_cntrl.quad_wrappers:
			quad_wrapper.readField()
		for dc_wrapper in self.mebt_main_orbit_diff_cntrl.dc_wrappers:
			dc_wrapper.readField()
		for bpm_wrapper in self.mebt_main_orbit_diff_cntrl.bpm_wrappers:
			bpm_wrapper.accountSignal()
			bpm_wrapper.calcStatistics()
		magnet_and_bpm_panel = self.mebt_main_orbit_diff_cntrl.magnet_and_bpm_panel
		magnet_and_bpm_panel.quad_table.getModel().fireTableDataChanged() 
		magnet_and_bpm_panel.dc_table.getModel().fireTableDataChanged()
		magnet_and_bpm_panel.bpm_table.getModel().fireTableDataChanged()

class DCs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		#print "debug index=",index
		dc_wrapper = self.mebt_main_orbit_diff_cntrl.dc_wrappers[index]
		orbit_measurer_cotroller_panel = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel
		orbit_index_info_panel_0 = orbit_measurer_cotroller_panel.orbit_index_info_panel_0 
		orbit_index_info_panel_1 = orbit_measurer_cotroller_panel.orbit_index_info_panel_1 
		orbit_index_info_panel_0.dc_label.setText("Dipole Corr.: "+dc_wrapper.magnet.getId())
		orbit_index_info_panel_1.dc_label.setText("Dipole Corr.: "+dc_wrapper.magnet.getId())
		coeff0 = orbit_index_info_panel_0.percent_txt.getValue()/100.
		coeff1 = orbit_index_info_panel_1.percent_txt.getValue()/100.
		field0 = - dc_wrapper.getUpperFieldLimit()*coeff0 
		field1 = dc_wrapper.getUpperFieldLimit()*coeff1 
		orbit_index_info_panel_0.dc_filed_txt.setValue(field0)
		orbit_index_info_panel_1.dc_filed_txt.setValue(field1)
		

class Replot_Button_Listener(ActionListener):
	#This button will replot all graphs
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")		
		orbit_diff_graphs_panel = self.mebt_main_orbit_diff_cntrl.orbit_diff_graphs_panel
		orbit_diff_graphs_panel.updateGraphData()
		
class Start_Measuring_Button_Listener(ActionListener):
	#This button will start the orbit shaking
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def actionPerformed(self,actionEvent):
		if(self.mebt_main_orbit_diff_cntrl.measure_running): return
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")	
		dc_index = self.mebt_main_orbit_diff_cntrl.magnet_and_bpm_panel.dc_table.getSelectedRow()
		if(dc_index < 0): 
			messageTextField.setText("Please select Dipole Corrector from the table first!")	
			return		
		runner = Measure_Runner(self.mebt_main_orbit_diff_cntrl)
		thr = Thread(runner)
		thr.start()			
		
class Stop_Measuring_Button_Listener(ActionListener):
	#This button will stop the he orbit shaking
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")		
		self.mebt_main_orbit_diff_cntrl.measure_running = false
		
class Recalculate_Model_Button_Listener(ActionListener):
	#This button will recalculate the model results and replot them
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.mebt_main_orbit_diff_cntrl.top_document.getMessageTextField()
		messageTextField.setText("")		
		orbit_measurer_cotroller_panel = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel
		orbit_measurer_cotroller_panel.runModel()

class DTL_Quad_Coeffs_Wheel_Listener	(PropertyChangeListener):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def propertyChange(self,event):
		wheel = self.mebt_main_orbit_diff_cntrl.correction_coeffs_panel.dtl_quad_coeff_wheel
		val = wheel.getValue()	
		for quad_wrapper in self.mebt_main_orbit_diff_cntrl.perm_quads_wrappers:
			quad_wrapper.scale(val)
			quad_wrapper.setFiledToDesign()
			#print "debug quad=",quad_wrapper.magnet.getId()," dfltF=",quad_wrapper.magnet.getDfltField()," DesgnF=",quad_wrapper.magnet.getDesignField()
		orbit_measurer_cotroller_panel = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel
		orbit_measurer_cotroller_panel.runModel()			

class DTL_Cav_Coeffs_Wheel_Listener(PropertyChangeListener):
	def __init__(self,mebt_main_orbit_diff_cntrl):
		self.mebt_main_orbit_diff_cntrl = mebt_main_orbit_diff_cntrl
		
	def propertyChange(self,event):
		wheel = self.mebt_main_orbit_diff_cntrl.correction_coeffs_panel.dtl_cav_coeff_wheel
		val = wheel.getValue()	
		for cav_wrapper in self.mebt_main_orbit_diff_cntrl.cav_wrappers:
			cav_wrapper.scale(val)
		orbit_measurer_cotroller_panel = self.mebt_main_orbit_diff_cntrl.orbit_measurer_cotroller_panel
		orbit_measurer_cotroller_panel.runModel()
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class MEBT_Main_Orbit_Diff_Controller:
	def __init__(self,top_document,accl):
		#--- top_document is a parent document for all controllers
		self.top_document = top_document		
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---- set up accSeq
		self.accSeq = null
		seq_names = ["MEBT","DTL1","DTL2","DTL3","DTL4","DTL5","DTL6"]
		lst = ArrayList()
		for seqName in seq_names:
			lst.add(accl.getSequence(seqName))
		self.accSeq = AcceleratorSeqCombo("SEQUENCE", lst)
		#---- setup magnets
		nodes = self.accSeq.getAllNodes()
		self.quad_wrappers = []
		self.perm_quads_wrappers = []
		self.dc_wrappers = []
		for node in nodes:
			pos = self.accSeq.getPosition(node)
			if(node.isMagnet()):
				if(node.isPermanent()):
					self.perm_quads_wrappers.append(Magnet_Wrapper(node,pos))
				else:
					if(node.isCorrector()):
						self.dc_wrappers.append(Magnet_Wrapper(node,pos))
					else:
						self.quad_wrappers.append(Magnet_Wrapper(node,pos))
		#---- cavs 
		rf_gaps = self.accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cav_wrappers = []
		self.mebt_cav_wrappers = []
		cavs = []
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			pos = self.accSeq.getPosition(cav)
			if((cav not in cavs) and cav.getId().find("DTL") >= 0):
				cavs.append(cav)
				self.cav_wrappers.append(Cavity_Wrapper(cav,pos))
			if((cav not in cavs) and cav.getId().find("MEBT") >= 0):
				cavs.append(cav)
				self.mebt_cav_wrappers.append(Cavity_Wrapper(cav,pos))			
		#---- BPMs
		self.bpm_wrappers = []
		bpms = self.accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType)))	
		for bpm in bpms:
			pos = self.accSeq.getPosition(bpm)
			bpm_wrapper = BPM_Wrapper(bpm,pos)
			if(bpm_wrapper.bpm.getId().find("DTL") >= 0):
				bpm_wrapper.use = false
			self.bpm_wrappers.append(bpm_wrapper)
		#---- debug print 
		"""
		for quad_wrapper in self.quad_wrappers:
			print "debug quad=",quad_wrapper.magnet.getId()
		for perm_quad_wrapper in self.perm_quads_wrappers:
			print "debug perm quad=",perm_quad_wrapper.magnet.getId()
		for dc_wrapper in self.dc_wrappers:
			print "debug dc=",dc_wrapper.magnet.getId()	
		for cav_wrapper in self.cav_wrappers:
			print "debug cav=",cav_wrapper.cav.getId()	
		for bpm_wrapper in self.bpm_wrappers:
			print "debug bpm=",bpm_wrapper.bpm.getId()	
		"""
		#---- Panels setup
		self.magnet_and_bpm_panel = Magnet_and_BPM_Panel(self)
		self.correction_coeffs_panel = Correction_Coeffs_Panel(self)
		self.orbit_diff_graphs_panel = Orbit_Diff_Graphs_Panel(self)
		self.orbit_measurer_cotroller_panel = Orbit_Measurer_Controller_Panel(self)
		#---- Auxiliaries setup
		self.orbit_measurer = Orbit_Measurer(self)
		self.measure_running = false
		#---------------------------------------------
		tmp0_panel = JPanel(BorderLayout())
		tmp0_panel.add(self.orbit_diff_graphs_panel,BorderLayout.CENTER)
		tmp0_panel.add(self.orbit_measurer_cotroller_panel,BorderLayout.SOUTH)
		tmp0_panel.add(self.correction_coeffs_panel,BorderLayout.NORTH)
		self.main_panel.add(self.magnet_and_bpm_panel,BorderLayout.WEST)
		self.main_panel.add(tmp0_panel,BorderLayout.CENTER)

	def getMainPanel(self):
		return self.main_panel

