# The classes for acceptance scan patterns. It includes the Acc. Scan Patterns
# Panel

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from java.util import ArrayList
from javax.swing.table import AbstractTableModel, TableModel
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
from javax.swing.filechooser import FileNameExtensionFilter

from xal.ca import ChannelFactory

from xal.smf.data import XMLDataManager
from xal.smf import AcceleratorSeqCombo

from xal.smf.impl import Marker, Quadrupole, RfGap, BPM
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat

import constants_lib
from dtl_acceptance_scans_data_lib import DTL_Acc_Scan_Data
from functions_and_classes_lib import calculateAvgErr, makePhaseNear 
from functions_and_classes_lib import dumpGraphDataToDA, readGraphDataFromDA


false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           DTL Acceptance Scan Patterns Controller
#------------------------------------------------------------------------
class DTL_Acc_Scan_Patterns_Controller:
	"""
	This controller keeps referneces to all acceptance scan patterns for particular cavity
	and the panel with these patterns.
	"""
	def __init__(self,dtl_acc_scan_cavity_controller):
		#---- dtl_acceptance_scans_controller - main controller for acceptance scans
		#---- it keeps referencies to controllers for each cavity - dtl_acc_scan_cavity_controller	
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller		
		self.dtl_acceptance_scans_controller = self.dtl_acc_scan_cavity_controller.dtl_acceptance_scans_controller
		#------------------------------------ array for DTL_Acc_Scan_Data instances
		self.dtl_acc_scan_data_arr = []
		#---- main panel 
		self.dtl_acc_scan_pattern_panel = DTL_Acc_Scan_Patterns_Main_Panel(self)
		#---- graph data with Acc. Scan Width vs. Cav. Amp data
		self.acc_scan_width_vs_amp_gd = BasicGraphData()
		self.acc_scan_width_vs_amp_gd.setLineThick(2)
		self.acc_scan_width_vs_amp_gd.setGraphPointSize(7)
		self.acc_scan_width_vs_amp_gd.setGraphColor(Color.BLACK)
		self.acc_scan_width_vs_amp_gd.setGraphProperty(constants_lib.GRAPH_LEGEND_KEY,"Scan Width vs. Cavity Amplitude")
		self.acc_scan_width_vs_amp_gd.setDrawLinesOn(true)
		self.acc_scan_width_vs_amp_gd.setDrawPointsOn(true)
		#---- graph data with Acc. Scan Phase Shift vs. Cav. Amp data
		self.acc_scan_phase_shift_vs_amp_gd = BasicGraphData()
		self.acc_scan_phase_shift_vs_amp_gd.setLineThick(2)
		self.acc_scan_phase_shift_vs_amp_gd.setGraphPointSize(7)
		self.acc_scan_phase_shift_vs_amp_gd.setGraphColor(Color.BLUE)
		self.acc_scan_phase_shift_vs_amp_gd.setGraphProperty(constants_lib.GRAPH_LEGEND_KEY,"Phase Shift vs. Cavity Amplitude")
		self.acc_scan_phase_shift_vs_amp_gd.setDrawLinesOn(true)
		self.acc_scan_phase_shift_vs_amp_gd.setDrawPointsOn(true)		
		
	def getMainPanel(self):
		return self.dtl_acc_scan_pattern_panel
		
	def addAccScanPattern(self,dtl_acc_scan_data):
		self.dtl_acc_scan_data_arr.append(dtl_acc_scan_data)
		self.dtl_acc_scan_pattern_panel.acc_scan_data_table.getModel().fireTableDataChanged()
		self.updateGraphs()
		
	def updateGraphs(self):
		self.acc_scan_width_vs_amp_gd.removeAllPoints()
		self.acc_scan_phase_shift_vs_amp_gd.removeAllPoints()
		for dtl_acc_scan_data in self.dtl_acc_scan_data_arr:
			dtl_acc_scan_data.updateGraphProperties()
			cav_amp = dtl_acc_scan_data.cav_amp_text.getValue()
			width = dtl_acc_scan_data.getFitWidth()
			phase_shift = dtl_acc_scan_data.getCavPhaseShiftFromFit()
			self.acc_scan_width_vs_amp_gd.addPoint(cav_amp,width)
			self.acc_scan_phase_shift_vs_amp_gd.addPoint(cav_amp,phase_shift)
		self.dtl_acc_scan_pattern_panel.patterns_graphs_panel_holder.updateGraphs()
		
	def getNewPhaseAmpAndPattern(self,width,left_slope_phase,cav_amp):
		"""
		Returns new cavity phase and amplitude, and a reference to 
		the production puttern of the acceptance scan data.
		If the mesured wdth is outside the existing data Width vs. Cav. Amp
		it returns false as a first parameter in the tuple.
		"""
		res = false
		dtl_acc_scan_data_prod_arr = self.getProductionScanData()
		if(len(dtl_acc_scan_data_prod_arr) != 1):
			return (res,0.,0.," The number of production scans in patters is not 1! Stop.")
		dtl_acc_scan_data_prod = dtl_acc_scan_data_prod_arr[0]
		width_gd = self.acc_scan_width_vs_amp_gd
		if(width_gd.getNumbOfPoints() < 2):
			return (res,0.,0.," Not enough points in the width_vs_amp patterns data! Stop.")
		width_min = 360.
		width_max = 0.
		for ind in range(width_gd.getNumbOfPoints()):
			y = width_gd.getY(ind)
			if(width_min > y): width_min = y
			if(width_max < y): width_max = y
		if(width < width_min or width > width_max):
			return (res,0.,0.," Patterns data do not cover the measured width! Stop.")
		width_ind = -1
		for ind in range(width_gd.getNumbOfPoints()-1):
			y0 = width_gd.getY(ind)
			y1 = width_gd.getY(ind+1)
			if(width >= y0 and width <= y1):
				width_ind = ind
				break
		x0 = width_gd.getX(width_ind)
		x1 = width_gd.getX(width_ind+1)
		y0 = width_gd.getY(width_ind)
		y1 = width_gd.getY(width_ind+1)
		smallest_phase_diff = 0.1
		amp_existing = (x1+x0)/2
		if(abs(y1-y0) > smallest_phase_diff):
			amp_existing = x0 + (x1-x0)*(width - y0)/(y1-y0)
		amp_production = dtl_acc_scan_data_prod.cav_amp_text.getValue()
		cav_amp_new = cav_amp*(amp_production/amp_existing)
		phase_shift0 = self.dtl_acc_scan_data_arr[width_ind].getCavPhaseShiftFromFit()
		phase_shift1 = self.dtl_acc_scan_data_arr[width_ind+1].getCavPhaseShiftFromFit()
		phase_shift = (phase_shift0 + phase_shift1)/2
		if(abs(y1-y0) > smallest_phase_diff):
			phase_shift = phase_shift0 + (phase_shift0 - phase_shift1)*(width - y0)/(y1-y0)
		cav_phase_new = left_slope_phase + phase_shift
		res = true
		return (res,cav_phase_new,cav_amp_new,"")

	def getProductionScanData(self):
		dtl_acc_scan_data_prod_arr = []
		for dtl_acc_scan_data in self.dtl_acc_scan_data_arr:
			if(dtl_acc_scan_data.is_production_scan):
				dtl_acc_scan_data_prod_arr.append(dtl_acc_scan_data)
		return dtl_acc_scan_data_prod_arr
		
	def writeDataToXML(self,root_da):
		scan_patterns_da = root_da.createChild("dtl_acc_scan_patterns")
		for dtl_acc_scan_data in self.dtl_acc_scan_data_arr:
			dtl_acc_scan_data.writeDataToXML(scan_patterns_da)
		
	def readDataFromXML(self,scan_patterns_da):
		if(scan_patterns_da == null): return
		self.dtl_acc_scan_data_arr = []
		dtl_acc_scan_data_arr_da = scan_patterns_da.childAdaptors("dtl_acceptance_scan_data")
		if(dtl_acc_scan_data_arr_da != null):
			for scan_data_da in dtl_acc_scan_data_arr_da:
				dtl_acc_scan_data = DTL_Acc_Scan_Data(self.dtl_acc_scan_cavity_controller)
				dtl_acc_scan_data.readDataFromXML(scan_data_da)
				dtl_acc_scan_data.show_on_patterns_plot = true
				self.dtl_acc_scan_data_arr.append(dtl_acc_scan_data)
		self.dtl_acc_scan_pattern_panel.acc_scan_data_table.getModel().fireTableDataChanged()
		self.updateGraphs()

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------
class DTL_Acc_Scan_Patterns_Main_Panel(JPanel):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller		
		self.dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_cavity_controller
		#-------------------------------------------------------------------
		self.setLayout(BorderLayout())
		self.setBorder(BorderFactory.createEtchedBorder())
		etched_border = BorderFactory.createEtchedBorder()
		#----------------------------------------------------
		self.acc_scan_data_table = JTable(Patterns_Table_Model(self.dtl_acc_scan_patterns_controller))
		self.acc_scan_data_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.acc_scan_data_table.setFillsViewportHeight(true)
		self.acc_scan_data_table.setPreferredScrollableViewportSize(Dimension(500,120))
		self.acc_scan_data_table.getSelectionModel().addListSelectionListener(Acc_Scan_Data_Table_Selection_Listener(self.dtl_acc_scan_patterns_controller))
		scrl_acc_scan_data_panel = JScrollPane(self.acc_scan_data_table)	
		#----------------------------------------------------
		buttons_panel_tmp0 = JPanel(GridLayout(3,4,4,4))
		re_analyze_pattern_button = JButton("Re-Analyze Selected Patterns")
		re_analyze_pattern_button.addActionListener(Re_Analyze_Patterns_Button_Listener(self.dtl_acc_scan_patterns_controller))
		read_acc_scan_data_button = JButton("Read Acc. Scan Data from ASCII")
		read_acc_scan_data_button.addActionListener(Read_Acc_Scan_Data_Button_Listener(self.dtl_acc_scan_patterns_controller))
		delete_acc_scan_data_button = JButton("Delete Read Acc. Scan Data")
		delete_acc_scan_data_button.addActionListener(Delete_Acc_Scan_Data_Button_Listener(self.dtl_acc_scan_patterns_controller))		
		buttons_panel_tmp0 .add(re_analyze_pattern_button)
		buttons_panel_tmp0 .add(read_acc_scan_data_button)
		buttons_panel_tmp0 .add(delete_acc_scan_data_button)
		buttons_panel = JPanel(BorderLayout())
		buttons_panel.add(buttons_panel_tmp0,BorderLayout.NORTH)
		#----------------------------------------------------
		top_panel = JPanel(BorderLayout())
		top_panel.add(buttons_panel,BorderLayout.WEST)
		top_panel.add(scrl_acc_scan_data_panel,BorderLayout.CENTER)
		#----------------------------------------------------
		self.patterns_graphs_panel_holder = Patterns_Graphs_Panel_Holder(self.dtl_acc_scan_patterns_controller)
		#----------------------------------------------------
		self.add(top_panel,BorderLayout.NORTH)
		self.add(self.patterns_graphs_panel_holder.getGraphsPanel(),BorderLayout.CENTER)
	
class Patterns_Graphs_Panel_Holder():
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller		
		self.dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		self.dtl_acc_scan_cavity_controller = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_cavity_controller
		#----------------------------------------
		etched_border = BorderFactory.createEtchedBorder()
		self.gp_acc_scan_patterns = FunctionGraphsJPanel()
		self.gp_acc_scan_width = FunctionGraphsJPanel()
		self.gp_acc_scan_phase_shift = FunctionGraphsJPanel()
		#------------------------------------------
		self.gp_acc_scan_patterns.setLegendButtonVisible(true)
		self.gp_acc_scan_width.setLegendButtonVisible(true)
		self.gp_acc_scan_phase_shift.setLegendButtonVisible(true)
		#------------------------------------------	
		self.gp_acc_scan_patterns.setChooseModeButtonVisible(true)
		self.gp_acc_scan_width.setChooseModeButtonVisible(true)
		self.gp_acc_scan_phase_shift.setChooseModeButtonVisible(true)
		#------------------------------------------
		self.gp_acc_scan_patterns.setName("Acceptance Scans Patterns: FC vs. RF Phase")
		self.gp_acc_scan_width.setName("Acceptance Scan Width vs. Cav. Ampl.")
		self.gp_acc_scan_phase_shift.setName("Cav. Phase Shift vs. Cav. Ampl.")
		self.gp_acc_scan_patterns.setAxisNames("Cav Phase, [deg]","FC Q, [a.u.]")
		self.gp_acc_scan_width.setAxisNames("Cav. Ampl., [arb units]"," Scan Width, [deg]")
		self.gp_acc_scan_phase_shift.setAxisNames("Cav. Ampl., [arb units]","Phase shift, [deg]")
		self.gp_acc_scan_patterns.setBorder(etched_border)
		self.gp_acc_scan_width.setBorder(etched_border)
		self.gp_acc_scan_phase_shift.setBorder(etched_border)
		#---------------------------------------------
		self.graphs_panel = JTabbedPane()
		self.graphs_panel.add("Acceptance Scan Patterns",self.gp_acc_scan_patterns)
		width_and_phase_shift_panel = JPanel(GridLayout(2,1,1,1))
		width_and_phase_shift_panel.add(self.gp_acc_scan_width)
		width_and_phase_shift_panel.add(self.gp_acc_scan_phase_shift)
		self.graphs_panel.add("Scan Width & Phase Shift vs. Cavity Ampl.",width_and_phase_shift_panel)
		
	def getGraphsPanel(self):
		return self.graphs_panel
		
	def refreshGraphs(self):
		self.gp_acc_scan_patterns.refreshGraphJPanel()
		self.gp_acc_scan_width.refreshGraphJPanel()
		
	def updateGraphs(self):
		dtl_acc_scan_data_arr = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr
		self.gp_acc_scan_patterns.removeAllGraphData()
		self.gp_acc_scan_width.removeAllGraphData()
		self.gp_acc_scan_phase_shift.removeAllGraphData()
		for dtl_acc_scan_data in dtl_acc_scan_data_arr:
			if(dtl_acc_scan_data.show_on_patterns_plot):
				self.gp_acc_scan_patterns.addGraphData(dtl_acc_scan_data.scan_gd)
				self.gp_acc_scan_patterns.addGraphData(dtl_acc_scan_data.getHalfHeightGraphData())
		self.gp_acc_scan_width.addGraphData(self.dtl_acc_scan_patterns_controller.acc_scan_width_vs_amp_gd)
		self.gp_acc_scan_phase_shift.addGraphData(self.dtl_acc_scan_patterns_controller.acc_scan_phase_shift_vs_amp_gd)

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Acc_Scan_Data_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index_start = listSelectionModel.getMinSelectionIndex()
		index_stop = listSelectionModel.getMaxSelectionIndex()
		
class Re_Analyze_Patterns_Button_Listener(ActionListener):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller
	
	def actionPerformed(self,actionEvent):
		dtl_acc_scan_pattern_panel = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_pattern_panel
		dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		messageTextField = dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		acc_scan_data_table = dtl_acc_scan_pattern_panel.acc_scan_data_table
		data_selected_inds = acc_scan_data_table.getSelectedRows()
		if(len(data_selected_inds) < 1 or data_selected_inds[0] < 0):
			messageTextField.setText("Select one or more patterns for analysis!")	
			return
		ind_start = data_selected_inds[0]
		ind_stop =  data_selected_inds[len(data_selected_inds)-1]
		for ind in data_selected_inds:
			self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr[ind].performAnalysis()
		#----- update graphs
		self.dtl_acc_scan_patterns_controller.updateGraphs()
		#----- update table
		acc_scan_data_table.getModel().fireTableDataChanged()			

class Read_Acc_Scan_Data_Button_Listener(ActionListener):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller
	
	def actionPerformed(self,actionEvent):
		dtl_acc_scan_pattern_panel = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_pattern_panel
		dtl_acc_scan_cavity_controller = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_cavity_controller
		dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		messageTextField = dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		acc_scan_data_table = dtl_acc_scan_pattern_panel.acc_scan_data_table
		#---------------------------------
		fc = JFileChooser(constants_lib.const_path_dict["WARM_LINAC_SETUP_FILES_DIR_PATH"])
		fc.setDialogTitle("Read Acceptance Scan Data from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		frame = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller.top_document.warm_linac_rf_setup_window.frame
		returnVal = fc.showOpenDialog(frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			fl_ph_in = open(fl_in.getPath(),"r")
			lns = fl_ph_in.readlines()
			fl_ph_in.close()
			lns = lns[2:]
			phase_arr = []
			q_arr = []
			for ln in lns:
				res_arr = ln.split()
				#print "debug str=",res_arr
				phase_arr.append(float(res_arr[0]))
				q_arr.append(float(res_arr[1]))
			#------------------------------------------------------
			scan_data = DTL_Acc_Scan_Data(dtl_acc_scan_cavity_controller)
			scan_data.scan_gd.addPoint(phase_arr,q_arr)
			self.dtl_acc_scan_patterns_controller.addAccScanPattern(scan_data)
			#----- update table
			acc_scan_data_table.getModel().fireTableDataChanged()		
			
class Delete_Acc_Scan_Data_Button_Listener(ActionListener):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller
	
	def actionPerformed(self,actionEvent):
		dtl_acc_scan_pattern_panel = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_pattern_panel
		dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		messageTextField = dtl_acceptance_scans_controller.getMessageTextField()
		messageTextField.setText("")
		acc_scan_data_table = dtl_acc_scan_pattern_panel.acc_scan_data_table
		data_selected_inds = acc_scan_data_table.getSelectedRows()
		if(len(data_selected_inds) < 1 or data_selected_inds[0] < 0):
			messageTextField.setText("Select one or more patterns to delete!")	
			return
		ind_start = data_selected_inds[0]
		ind_stop =  data_selected_inds[len(data_selected_inds)-1]
		dtl_acc_scan_data_arr = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr[:]
		for ind in data_selected_inds:
			dtl_acc_scan_data = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr[ind]
			dtl_acc_scan_data_arr.remove(dtl_acc_scan_data)
		self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr = []
		self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr += dtl_acc_scan_data_arr
		#----- update graphs
		self.dtl_acc_scan_patterns_controller.updateGraphs()
		#----- update table
		acc_scan_data_table.getModel().fireTableDataChanged()			

#------------------------------------------------
#  JTable models
#------------------------------------------------
class Patterns_Table_Model(AbstractTableModel):
	def __init__(self,dtl_acc_scan_patterns_controller):
		self.dtl_acc_scan_patterns_controller = dtl_acc_scan_patterns_controller
		self.dtl_acceptance_scans_controller = self.dtl_acc_scan_patterns_controller.dtl_acceptance_scans_controller
		self.columnNames = ["Cav. Amp. [a.u.]",]
		self.columnNames += ["Cav. Phase [deg]",]
		self.columnNames += ["Slope Start [deg]",]
		self.columnNames += ["Phase Shift [deg]",]
		self.columnNames += ["Width [deg]",]
		self.columnNames += ["Production",]
		self.columnNames += ["Show on Plot",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		#---- it will be number of patterns
		return len(self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		dtl_acc_scan_data = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr[row]
		if(col == 0): return "%6.4f"%dtl_acc_scan_data.cav_amp_text.getValue()
		if(col == 1): return "%5.1f"%dtl_acc_scan_data.cav_prod_phase_text.getValue()
		if(col == 2): return "%5.3f"%dtl_acc_scan_data.getLeftHalfHeightPosition()
		if(col == 3): return "%5.1f"%dtl_acc_scan_data.fit_phase_shift_text.getValue()
		if(col == 4): return "%5.1f"%dtl_acc_scan_data.getFitWidth()
		if(col == 5): return dtl_acc_scan_data.isItProductionScan()
		if(col == 6): return dtl_acc_scan_data.show_on_patterns_plot
		return ""
				
	def getColumnClass(self,col):
		if(col == 5 or col == 6): return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(col == 0 or col == 1 or col == 5 or col == 6): return true
		return false
			
	def setValueAt(self, value, row, col):
		dtl_acc_scan_data = self.dtl_acc_scan_patterns_controller.dtl_acc_scan_data_arr[row]
		if(col == 0):
			dtl_acc_scan_data.cav_amp_text.setValue(float(value))
		if(col == 1):
			dtl_acc_scan_data.cav_prod_phase_text.setValue(float(value))
		if(col == 5):
			dtl_acc_scan_data.setIfItIsProductionScan(value)
		if(col == 6):
			dtl_acc_scan_data.show_on_patterns_plot = value
			self.dtl_acc_scan_patterns_controller.updateGraphs()
			
