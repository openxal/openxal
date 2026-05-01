# The phase scan data cleanup controller. 
# It is a part of the SCL Phase Scan controller (scl_long_tuneup_phase_scan_cntrl_lib.py).
# It analyzes the scan data to find the bad phase points significantly different
# from the expected sin-like curve in the data for each cavity and each BPM.
# these bpms (or data points) should be excluded from the analysis.

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension
from java.util import Vector
from java.io import File, BufferedWriter, FileWriter
from javax.swing.filechooser import FileNameExtensionFilter

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Auxiliary classes and functions
#------------------------------------------------------------------------	


#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class Bad_BPMs_Amp_Phase_Graphs_Panel(JPanel):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		self.setLayout(GridLayout(2,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.gp_bpm_phases = FunctionGraphsJPanel()
		self.gp_bpm_amps = FunctionGraphsJPanel()
		self.gp_bpm_phases.setLegendButtonVisible(true)
		self.gp_bpm_phases.setChooseModeButtonVisible(true)	
		self.gp_bpm_amps.setLegendButtonVisible(true)
		self.gp_bpm_amps.setChooseModeButtonVisible(true)
		self.gp_bpm_phases.setName("BPM Phases")
		self.gp_bpm_phases.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")	
		self.gp_bpm_amps.setName("BPM Amplitude")
		self.gp_bpm_amps.setAxisNames("Cav Phase, [deg]","Amplitude, a.u.")	
		self.gp_bpm_phases.setBorder(etched_border)
		self.gp_bpm_amps.setBorder(etched_border)
		self.add(self.gp_bpm_phases)
		self.add(self.gp_bpm_amps)	
		
	def removeAllGraphData(self):
		self.gp_bpm_phases.removeAllGraphData()
		self.gp_bpm_amps.removeAllGraphData()		
		
	def updateGraphData(self):
		self.gp_bpm_phases.removeAllGraphData()
		self.gp_bpm_amps.removeAllGraphData()
		bpms_table = self.scl_scan_data_cleanup_controller.bpms_table
		cavs_table = self.scl_scan_data_cleanup_controller.cavs_table
		bpm_wrappers = self.scl_scan_data_cleanup_controller.bpms_arr
		cav_wrappers = self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr
		bpm_selected_ind = bpms_table.getSelectedRow()
		cav_selected_ind = cavs_table.getSelectedRow()
		if(bpm_selected_ind >= 0 and cav_selected_ind >= 0):
			bpm_wrapper = bpm_wrappers[bpm_selected_ind]
			cav_wrapper = cav_wrappers[cav_selected_ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.getAmpPhaseGraphs(bpm_wrapper)
			if(graphDataAmp != null):
				self.gp_bpm_amps.addGraphData(graphDataAmp)
			if(graphDataPhase != null):
				self.gp_bpm_phases.addGraphData(graphDataPhase)


#------------------------------------------------
#  JTable models
#------------------------------------------------
class Cavs_with_Bad_PhaseScan_Table_Model(AbstractTableModel):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		self.columnNames = ["Cavity","N Bad BPMs"]		
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return (len(self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr))

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr[row]
		if(col == 0):
			return cav_wrapper.alias
		if(col == 1):
			[bpm_wrappers,bpms_to_points_dict] = self.scl_scan_data_cleanup_controller.cavs_to_bpm_dict[cav_wrapper]			
			nBad_bpms = len(bpm_wrappers)
			return str(nBad_bpms)
		return ""
				
	def getColumnClass(self,col):
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false

class Bad_PhaseScan_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		self.columnNames = ["BPM","Bad Points"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return (len(self.scl_scan_data_cleanup_controller.bpms_arr))

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):	
		bpm_wrapper = self.scl_scan_data_cleanup_controller.bpms_arr[row]
		cav_ind = self.scl_scan_data_cleanup_controller.cavs_table.getSelectedRow()
		if(cav_ind < 0): return ""
		cav_wrapper = self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr[cav_ind]
		if(col == 0):
			return bpm_wrapper.alias
		if(col == 1):
			[bpm_wrappers,bpms_to_points_dict] = self.scl_scan_data_cleanup_controller.cavs_to_bpm_dict[cav_wrapper]			
			nBad_Points = len(bpms_to_points_dict[bpm_wrapper])
			return str(nBad_Points)
		return ""
				
	def getColumnClass(self,col):
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false
		
class Bad_Points_Table_Model(AbstractTableModel):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		self.columnNames = ["ind","phase","delta"]		
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return (len(self.scl_scan_data_cleanup_controller.points_arr))

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		[ind,phase,delta] = self.scl_scan_data_cleanup_controller.points_arr[row]
		if(col == 0):
			return str(ind)
		if(col == 1):
			return "%5.1f"%phase
		if(col == 2):
			return "%5.1f"%delta			
		return "None"
	
	def getColumnClass(self,col):
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false		

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller

	def valueChanged(self,listSelectionEvent):
		self.scl_scan_data_cleanup_controller.bpms_arr = []
		self.scl_scan_data_cleanup_controller.points_arr = []
		listSelectionModel = listSelectionEvent.getSource()		
		index = listSelectionModel.getMinSelectionIndex()
		if(index >= 0):
			cav_wrapper = self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr[index]
			cavs_dict = self.scl_scan_data_cleanup_controller.cavs_to_bpm_dict
			if(cavs_dict.has_key(cav_wrapper)):
				[bpm_wrappers,bpms_to_points_dict] = cavs_dict[cav_wrapper]
				self.scl_scan_data_cleanup_controller.bpms_arr = bpm_wrappers[:]
				self.scl_scan_data_cleanup_controller.points_arr = []
		self.scl_scan_data_cleanup_controller.bpms_table.getModel().fireTableDataChanged()
		self.scl_scan_data_cleanup_controller.bad_bpms_amp_phase_graphs_panel.updateGraphData()
		
class BPMs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller

	def valueChanged(self,listSelectionEvent):
		self.scl_scan_data_cleanup_controller.points_arr = []
		cav_ind = self.scl_scan_data_cleanup_controller.cavs_table.getSelectedRow()
		if(cav_ind >= 0):
			cav_wrapper = self.scl_scan_data_cleanup_controller.cavs_with_bad_data_arr[cav_ind]
			cavs_dict = self.scl_scan_data_cleanup_controller.cavs_to_bpm_dict
			if(cavs_dict.has_key(cav_wrapper)):
				[bpm_wrappers,bpms_to_points_dict] = cavs_dict[cav_wrapper]
				listSelectionModel = listSelectionEvent.getSource()
				index = listSelectionModel.getMinSelectionIndex()
				if(index >= 0):
					bpm_wrapper = self.scl_scan_data_cleanup_controller.bpms_arr[index]
					if(bpms_to_points_dict.has_key(bpm_wrapper)):
						points_arr = bpms_to_points_dict[bpm_wrapper]
						self.scl_scan_data_cleanup_controller.points_arr = points_arr[:]
		self.scl_scan_data_cleanup_controller.points_table.getModel().fireTableDataChanged()
		self.scl_scan_data_cleanup_controller.bad_bpms_amp_phase_graphs_panel.updateGraphData()
		
		
class Analyze_Data_Button_Listener(ActionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_scan_data_cleanup_controller.analyze_Data_Method()
		
class CleanUp_Worst_Phase_Points_Data_Button_Listener(ActionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_scan_data_cleanup_controller.cleanUp_Worst_Phase_Points_Data_Method()
		
class CleanUp_All_Bad_BPMs_Button_Listener(ActionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_scan_data_cleanup_controller.cleanUp_All_Bad_BPMs_Method()
								
class Export_Scan_Data_Button_Listener(ActionListener):
	def __init__(self,scl_scan_data_cleanup_controller):
		self.scl_scan_data_cleanup_controller = scl_scan_data_cleanup_controller
		self.scl_long_tuneup_controller = self.scl_scan_data_cleanup_controller.scl_long_tuneup_controller
		self.scl_long_tuneup_phase_scan_controller = null
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller		
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		#=======================================================
		cavs_table = self.scl_long_tuneup_phase_scan_controller.cavs_table
		cavs_slected_row = cavs_table.getSelectedRow()
		if(cavs_slected_row < 0):
			self.scl_long_tuneup_controller.getMessageTextField().setText("Select cavity.")	
			return
		cav_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		if(cavs_slected_row >= 1):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cavs_slected_row-1]
		bpms_table = self.scl_long_tuneup_phase_scan_controller.bpms_table
		bpms_selcted_rows = bpms_table.getSelectedRows()
		bpm_wrappers = []
		for ind in bpms_selcted_rows:
			if(cav_wrapper.bpm_wrappers_useInPhaseAnalysis[ind]):
				bpm_wrappers.append(cav_wrapper.bpm_wrappers[ind])
		if(len(bpm_wrappers) == 0):
			self.scl_long_tuneup_controller.getMessageTextField().setText("Select BPMs.")
			return 		
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Selected Cav. Scan Data to ASCII")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_out = File(fl_out.getPath()+".dat")			
			buffer_out = BufferedWriter(FileWriter(fl_out))
			
			txt  = "# cav="+cav_wrapper.alias+"  pos = %8.3f "%cav_wrapper.pos+" XAL_amp= %8.3f "%cav_wrapper.designAmp
			buffer_out.write(txt)
			buffer_out.newLine()

			txt = "# cav_phase  "
			for bpm_wrapper in bpm_wrappers:
				txt += bpm_wrapper.alias+"_phase "+bpm_wrapper.alias+"_amp    "
			buffer_out.write(txt)
			buffer_out.newLine()
			
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers[0]]
			
			cav_phases_arr = []
			for i in range(graphDataPhase.getNumbOfPoints()):
				cav_phases_arr.append(graphDataPhase.getX(i))
			
			for cav_phase_ind in range(len(cav_phases_arr)):
				cav_phase = cav_phases_arr[cav_phase_ind]
				st = " %8.3f    "%cav_phase
				for bpm_wrapper in bpm_wrappers:
					(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
					bpm_phase = graphDataPhase.getY(cav_phase_ind)
					bpm_amp = graphDataAmp.getY(cav_phase_ind)
					st += "    %8.3f "%bpm_phase+" %8.3f "%bpm_amp
				buffer_out.write(st)
				buffer_out.newLine()			

			buffer_out.flush()

			#---- end of writing
			buffer_out.flush()
			buffer_out.close()
	
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Scan_Data_CleanUp_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()	
		#----main panel
		self.main_panel = JPanel(BorderLayout())
		#------top params panel-----------------------
		right_info_panel = JPanel(BorderLayout())
		right_top_control_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		analysis_button = JButton("Find BAD Points")
		analysis_button.addActionListener(Analyze_Data_Button_Listener(self))
		right_top_control_panel.add(analysis_button)
		#------ maximal bpm phase error after the scan
		max_phase_err_lbl = JLabel("Max BPM Phase Err = ",JLabel.RIGHT)
		self.max_phase_err_text = DoubleInputTextField(10.0,DecimalFormat("##.#"),5)		
		right_top_control_panel.add(max_phase_err_lbl)
		right_top_control_panel.add(self.max_phase_err_text)
		#-----------------------------------------------
		right_bottom_control_panel = JPanel(BorderLayout())
		right_bottom_control_panel0 = JPanel(BorderLayout())
		right_bottom_control_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,1,1))
		right_bottom_control_panel.add(right_bottom_control_panel0,BorderLayout.NORTH)
		right_bottom_control_panel.add(right_bottom_control_panel1,BorderLayout.SOUTH)
		statistics_of_errors_lbl = JLabel("Statistics:",JLabel.RIGHT)
		self.statistics_of_errors_txt = JTextField()
		right_bottom_control_panel0.add(statistics_of_errors_lbl,BorderLayout.WEST)
		right_bottom_control_panel0.add(self.statistics_of_errors_txt,BorderLayout.CENTER)
		remove_worst_points_button = JButton("Remove Worst Points")
		remove_worst_points_button.addActionListener(CleanUp_Worst_Phase_Points_Data_Button_Listener(self))
		remove_all_bad_bpms_button = JButton("Remove All Bad BPMs")		
		remove_all_bad_bpms_button.addActionListener(CleanUp_All_Bad_BPMs_Button_Listener(self))	
		export_scan_data_button = JButton("Export Scan Data")		
		export_scan_data_button.addActionListener(Export_Scan_Data_Button_Listener(self))			
		right_bottom_control_panel1.add(remove_worst_points_button)
		right_bottom_control_panel1.add(remove_all_bad_bpms_button)
		right_bottom_control_panel1.add(export_scan_data_button)
		#-----------------------------------------------
		right_tables_panel = JPanel(GridLayout(1,3))	
		right_info_panel.add(right_top_control_panel,BorderLayout.NORTH)
		right_info_panel.add(right_tables_panel,BorderLayout.CENTER)
		right_info_panel.add(right_bottom_control_panel,BorderLayout.SOUTH)
		self.main_panel.add(right_info_panel,BorderLayout.EAST)
		#------cavities scan table panel --------
		self.bad_bpms_amp_phase_graphs_panel = Bad_BPMs_Amp_Phase_Graphs_Panel(self)
		self.main_panel.add(self.bad_bpms_amp_phase_graphs_panel,BorderLayout.CENTER)
		#------ let's make tables for a list of cavities, bpms, and bad points indexes
		self.cavs_with_bad_data_table_model = Cavs_with_Bad_PhaseScan_Table_Model(self)
		self.cavs_table = JTable(self.cavs_with_bad_data_table_model)
		self.cavs_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cavs_table.setFillsViewportHeight(true)
		self.cavs_table.setPreferredScrollableViewportSize(Dimension(180,300))
		self.cavs_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self))		
		self.bpms_table_model = Bad_PhaseScan_BPMs_Table_Model(self)
		self.bpms_table = JTable(self.bpms_table_model)
		self.bpms_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.bpms_table.setFillsViewportHeight(true)	
		self.bpms_table.getSelectionModel().addListSelectionListener(BPMs_Table_Selection_Listener(self))	
		self.bpms_table.setPreferredScrollableViewportSize(Dimension(180,300))
		self.points_table_model = Bad_Points_Table_Model(self)
		self.points_table = JTable(self.points_table_model)
		self.points_table.setFillsViewportHeight(true)	
		self.points_table.setPreferredScrollableViewportSize(Dimension(180,300))
		#----- set scroll panes
		scrl_panel0 = JScrollPane(self.cavs_table)
		scrl_panel1 = JScrollPane(self.bpms_table)
		scrl_panel2 = JScrollPane(self.points_table)		
		scrl_panel0.setBorder(BorderFactory.createTitledBorder(etched_border,"Cavs"))		
		scrl_panel1.setBorder(BorderFactory.createTitledBorder(etched_border,"BPMs"))
		scrl_panel2.setBorder(BorderFactory.createTitledBorder(etched_border,"Bad Points"))
		right_tables_panel.setBorder(etched_border)
		right_tables_panel.add(scrl_panel0)
		right_tables_panel.add(scrl_panel1)
		right_tables_panel.add(scrl_panel2)
		#----- dictionary with the bad data self.cavs_to_bpm_dict[cav_wrapper] = [bpm_wrappers,bpms_to_points_dict]
		self.cavs_to_bpm_dict = {}	
		#----- arrays with bad data for the tables
		self.cavs_with_bad_data_arr = []
		self.bpms_arr = []
		self.points_arr = []
		
	def cleanUp_All_Bad_BPMs_Method(self):
		scl_long_tuneup_controller = self.scl_long_tuneup_controller
		cav_wrappers = scl_long_tuneup_controller.cav_wrappers[1:]
		#----- dictionary with the bad data 
		cavs_to_bpm_dict = self.cavs_to_bpm_dict
		#----- let's go through all cavities to find the bad bpms
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood and cav_wrapper.isMeasured):
				if(cavs_to_bpm_dict.has_key(cav_wrapper)):
					[bpm_wrappers,bpms_to_points_dict] = cavs_to_bpm_dict[cav_wrapper]
					for bpm_wrapper_bad in bpm_wrappers:
						for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
							bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
							if(bpm_wrapper_bad == bpm_wrapper):
								cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = false
								cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
		
	def cleanUp_Worst_Phase_Points_Data_Method(self):
		scl_long_tuneup_controller = self.scl_long_tuneup_controller
		cav_wrappers = scl_long_tuneup_controller.cav_wrappers[1:]
		#----- dictionary with the bad data 
		cavs_to_bpm_dict = self.cavs_to_bpm_dict
		#----- let's go through all cavities to find the worst phase scan points
		#----- worst points means maximal number of bad bpms with it
		n_total_removed_points = 0
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood and cav_wrapper.isMeasured):
				if(cavs_to_bpm_dict.has_key(cav_wrapper)):
					[bpm_wrappers,bpms_to_points_dict] = cavs_to_bpm_dict[cav_wrapper]
					#----- numb_of_points_dict[point_scan_ind] = [bpm1,...]
					numb_of_points_dict = {}
					for bpm_wrapper in bpm_wrappers:
						bad_points_arr = bpms_to_points_dict[bpm_wrapper]
						for [ind,x,y] in bad_points_arr:
							if(numb_of_points_dict.has_key(ind)):
								numb_of_points_dict[ind].append(bpm_wrapper)
							else:
								numb_of_points_dict[ind] = [bpm_wrapper,]
					max_bpms_ind = -1
					max_numb_of_bpms = 0
					for ind in numb_of_points_dict.keys():
						n_bpms = len(numb_of_points_dict[ind])
						if(n_bpms > max_numb_of_bpms):
							max_bpms_ind = ind
					if(max_bpms_ind >= 0):
						#---- the phase scan data with this index should be removed 
						n_bpms = len(numb_of_points_dict[max_bpms_ind])
						if(n_bpms > 1):
							n_total_removed_points += n_bpms
							gd = cav_wrapper.phaseDiffPlot
							gd.removePoint(max_bpms_ind)
							for bpm_wrapper in cav_wrapper.bpm_wrappers:
								(graphDataAmp,graphDataPhase) = cav_wrapper.getAmpPhaseGraphs(bpm_wrapper)
								if(graphDataAmp != null):
									graphDataAmp.removePoint(max_bpms_ind)
								if(graphDataPhase != null):
									graphDataPhase.removePoint(max_bpms_ind)													
							#print "debug cav=",cav_wrapper.alias," n bpms=",n_bpms
		#print "debug 	n_total_removed_points=",n_total_removed_points
		
	def analyze_Data_Method(self):
		scl_long_tuneup_controller = self.scl_long_tuneup_controller
		max_phase_diff0 = self.max_phase_err_text.getValue()
		cav_wrappers = scl_long_tuneup_controller.cav_wrappers[1:]
		#----- dictionary with the bad data 
		self.cavs_to_bpm_dict = {}
		cavs_to_bpm_dict = self.cavs_to_bpm_dict
		#----- arrays with bad data for the tables
		self.cavs_with_bad_data_arr = []
		self.bpms_arr = []
		self.points_arr = []
		#---- data analysis for bad points
		total_nPoints = 0		
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood and cav_wrapper.isMeasured):
				bpm_wrapper0 = cav_wrapper.bpm_wrapper0
				bpm_wrapper1 = cav_wrapper.bpm_wrapper1
				pos0 = bpm_wrapper0.pos
				pos1 = bpm_wrapper1.pos
				phaseDiffPlot = cav_wrapper.phaseDiffPlot
				(graphDataAmp0,graphDataPhase0) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper0]
				if(phaseDiffPlot.getNumbOfPoints() == 0): continue
				for bpm_wrapper_ind in range(len(cav_wrapper.bpm_wrappers)):
					bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_wrapper_ind]				
					pos = bpm_wrapper.pos
					if(pos < pos0): continue
					pos_coeff = (pos - pos0)/(pos1 - pos0)
					max_phase_diff = max_phase_diff0
					if(pos_coeff >= 1.0): max_phase_diff = pos_coeff*max_phase_diff0
					if(cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_wrapper_ind]):
						(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
						nPoints = phaseDiffPlot.getNumbOfPoints()
						bpm_phase_delta = graphDataPhase.getY(nPoints-1) - graphDataPhase0.getY(nPoints-1) - pos_coeff*phaseDiffPlot.getY(nPoints-1)
						bad_points_arr = []
						for ind in range(phaseDiffPlot.getNumbOfPoints()):
							total_nPoints += 1
							y = graphDataPhase.getY(ind) - graphDataPhase0.getY(ind) - pos_coeff*phaseDiffPlot.getY(ind) - bpm_phase_delta
							y = makePhaseNear(y,0.)
							if(abs(y) > max_phase_diff):
								bad_points_arr.append([ind,graphDataPhase.getX(ind),y])
						if(len(bad_points_arr) > 0):
							if(not cavs_to_bpm_dict.has_key(cav_wrapper)):
								cavs_to_bpm_dict[cav_wrapper] = [[],{}]
							[bpm_wrappers,bpms_to_points_dict] = cavs_to_bpm_dict[cav_wrapper]
							bpm_wrappers.append(bpm_wrapper)
							bpms_to_points_dict[bpm_wrapper] = bad_points_arr	
		for cav_wrapper in cav_wrappers:
			if(cavs_to_bpm_dict.has_key(cav_wrapper)):
				self.cavs_with_bad_data_arr.append(cav_wrapper)
		#--------------------------------------------------------------------
		nCavs = 0
		nBPMs = 0
		nPoints = 0
		for cav_wrapper in self.cavs_with_bad_data_arr:
			[bpm_wrappers,bpms_to_points_dict] = cavs_to_bpm_dict[cav_wrapper]
			nCavs += 1
			nBPMs += len(bpm_wrappers)
			for bpm_wrapper in bpm_wrappers:
				points_arr = bpms_to_points_dict[bpm_wrapper]
				nPoints += len(points_arr)
		st = "N Bad:  Cavs= "+str(nCavs )+"  BPMs= "+str(nBPMs)+"  Phase Points="+str(nPoints)+"/"+str(total_nPoints)
		self.statistics_of_errors_txt.setText(st)
		self.cavs_table.getModel().fireTableDataChanged()
		self.bpms_table.getModel().fireTableDataChanged()
		self.points_table.getModel().fireTableDataChanged()
		nCavs = len(self.cavs_with_bad_data_arr)