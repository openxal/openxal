# The transverse Twiss WS/LW data analysis classes

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
from java.awt import Dimension
from javax.swing.filechooser import FileNameExtensionFilter

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap, RfCavity 
from xal.extension.wirescan.apputils import GaussFitter, WireScanData
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.model.probe import EnvelopeProbe

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from ws_lw_acquisition_cntrl_lib import WS_DIRECTION_HOR, WS_DIRECTION_VER, WS_DIRECTION_NULL
from transverse_twiss_analysis_subpanel_cntrl_lib  import Transverse_Twiss_Fitting_Controller
from linac_wizard_read_write_file_lib import dumpGraphDataToDA, readGraphDataFromDA

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class WS_LW_Size_Record:
	"""
	This class keeps the results of the scan in one directions for one LW or WS
	"""
	def __init__(self):
		self.isOn = true
		self.ws_node = null
		self.gauss_sigma = 0.	
		self.custom_gauss_sigma = 0.
		self.custom_rms_sigma = 0.		
		self.ws_direction = WS_DIRECTION_NULL
		self.quad_cav_dict = null
		self.model_sigma = 0.
		self.index = -1
		self.pos = -10.
		self.ws_scan_and_fit_record = null

	def setUpParams(self,ws_scan_and_fit_record):
		self.ws_node = ws_scan_and_fit_record.ws_node	
		self.gauss_sigma = ws_scan_and_fit_record.gauss_sigma		
		self.custom_gauss_sigma = ws_scan_and_fit_record.custom_gauss_sigma
		self.custom_rms_sigma = ws_scan_and_fit_record.custom_rms_sigma
		self.ws_direction = ws_scan_and_fit_record.ws_direction
		self.index = ws_scan_and_fit_record.index
		self.pos = ws_scan_and_fit_record.pos
		self.ws_scan_and_fit_record = ws_scan_and_fit_record			
		self.setQuadAndCavDict(ws_scan_and_fit_record.param_dict)
		
	def setQuadAndCavDict(self,quad_cav_dict):
		if(self.ws_scan_and_fit_record != null):
			self.ws_scan_and_fit_record.param_dict = quad_cav_dict
		self.quad_cav_dict = quad_cav_dict
		
	def getQuadAndCavDict(self):
		return self.quad_cav_dict
	
class AccState:
	"""
	AccState keeps the dictionary with quad fields, cavity amp. and phases, 
	the graph plot with WS/LW sizes, the calculated beam sizes, and 
	the scenario for this quad/cavities parameters.
	"""
	def __init__(self,quad_cav_dict,linac_wizard_document):
		self.quad_cav_dict = quad_cav_dict
		self.linac_wizard_document = linac_wizard_document
		self.isOn = true
		self.size_hor_record_arr = []
		self.size_ver_record_arr = []
		self.isSelected_ = false
		#----------- graph data 
		self.gd_exp_hor =  BasicGraphData()
		self.gd_exp_ver =  BasicGraphData()
		self.gd_exp_hor.setDrawLinesOn(false)
		self.gd_exp_ver.setDrawLinesOn(false)
		self.gd_exp_hor.setGraphPointSize(11)
		self.gd_exp_ver.setGraphPointSize(11)
		self.gd_exp_hor.setGraphColor(Color.BLUE)
		self.gd_exp_ver.setGraphColor(Color.BLUE)
		self.gd_model_hor =  BasicGraphData()
		self.gd_model_ver =  BasicGraphData()
		self.gd_model_lon =  BasicGraphData()
		self.gd_model_hor.setGraphColor(Color.RED)
		self.gd_model_ver.setGraphColor(Color.RED)
		self.gd_model_lon.setGraphColor(Color.RED)
		self.gd_model_hor.setLineThick(3)
		self.gd_model_ver.setLineThick(3)
		self.gd_model_lon.setLineThick(4)
		self.gd_model_hor.setDrawPointsOn(false)
		self.gd_model_ver.setDrawPointsOn(false)
		self.gd_exp_hor.setGraphProperty(GRAPH_LEGEND_KEY,"LW/WS sizes")
		self.gd_exp_ver.setGraphProperty(GRAPH_LEGEND_KEY,"LW/WS sizes")
		self.gd_model_hor.setGraphProperty(GRAPH_LEGEND_KEY,"Model Hor. Size")
		self.gd_model_ver.setGraphProperty(GRAPH_LEGEND_KEY,"Model Ver. Size")
		self.gd_model_lon.setGraphProperty(GRAPH_LEGEND_KEY,"Model Longitudinal Size")
		#------ accelerator model set up
		accSeq = self.linac_wizard_document.getAccSeq()
		quads = self.linac_wizard_document.ws_lw_controller.quads
		cavs = self.linac_wizard_document.ws_lw_controller.cavs
		#--memorize the initial values
		self.quad_field_arr = []
		for quad in quads:
			self.quad_field_arr.append([quad,quad.getDfltField()])
		self.cav_amp_phase_arr = []
		for cav in cavs:
			self.cav_amp_phase_arr.append([cav,cav.getDfltCavAmp(),cav.getDfltCavPhase()])
		#-- set up values from dictionaries
		[quad_dict,cav_amp_phase_dict] = self.quad_cav_dict
		for quad in quads:
			if(quad_dict.has_key(quad)):
				quad.setDfltField(quad_dict[quad])
		for cav in cavs:
			if(cav_amp_phase_dict.has_key(cav)):
				cav.updateDesignAmp(cav_amp_phase_dict[cav][0])
				cav.updateDesignPhase(cav_amp_phase_dict[cav][1])
		self.env_tracker = AlgorithmFactory.createEnvTrackerAdapt(accSeq)
		self.env_tracker.setRfGapPhaseCalculation(true)
		self.env_tracker.setUseSpacecharge(true)
		self.design_probe = ProbeFactory.getEnvelopeProbe(accSeq,self.env_tracker)
		probe = EnvelopeProbe(self.design_probe)
		self.scenario = Scenario.newScenarioFor(accSeq)
		self.scenario.setProbe(probe)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.resync()
		self.scenario.run()
		self.traj = self.scenario.getTrajectory()
		#---restore the quads and cav parameters
		for [quad,field] in self.quad_field_arr:
			quad.setDfltField(field)
		for [cav,amp,phase] in self.cav_amp_phase_arr:
			cav.updateDesignAmp(amp)
			cav.updateDesignPhase(phase)

	def resyncScenario(self):
		#-- set up values from dictionaries
		[quad_dict,cav_amp_phase_dict] = self.quad_cav_dict
		for [quad,field] in self.quad_field_arr:
			if(quad_dict.has_key(quad)):
				quad.setDfltField(quad_dict[quad])
		for [cav,amp,phase] in self.cav_amp_phase_arr:
			if(cav_amp_phase_dict.has_key(cav)):
				cav.updateDesignAmp(cav_amp_phase_dict[cav][0])
				cav.updateDesignPhase(cav_amp_phase_dict[cav][1])
		self.scenario.resync()
		#---restore the quads and cav parameters
		for [quad,field] in self.quad_field_arr:
			quad.setDfltField(field)
		for [cav,amp,phase] in self.cav_amp_phase_arr:
			cav.updateDesignAmp(amp)
			cav.updateDesignPhase(phase)		

	def updateGraphData(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		fit_param_index = tr_twiss_analysis_controller.fit_param_index
		self.gd_exp_hor.removeAllPoints()
		self.gd_exp_ver.removeAllPoints()
		for size_record in self.size_hor_record_arr:
			if(not size_record.isOn): continue
			size = size_record.gauss_sigma
			if(fit_param_index == 1): size = size_record.custom_gauss_sigma
			if(fit_param_index == 2): size = size_record.custom_rms_sigma
			pos = size_record.pos
			self.gd_exp_hor.addPoint(pos,size)
		for size_record in self.size_ver_record_arr:
			if(not size_record.isOn): continue
			size = size_record.gauss_sigma
			if(fit_param_index == 1): size = size_record.custom_gauss_sigma
			if(fit_param_index == 2): size = size_record.custom_rms_sigma
			pos = size_record.pos
			self.gd_exp_ver.addPoint(pos,size)

	def addSizeRecord(self,size_record):
		if(size_record.ws_direction == WS_DIRECTION_HOR):
			self.size_hor_record_arr.append(size_record)
		if(size_record.ws_direction == WS_DIRECTION_VER):
			self.size_ver_record_arr.append(size_record)
		
	def getQuadCavDict(self):
		return self.quad_cav_dict
		
	def setSelection(self,selection = false):
		self.isSelected_ = selection
		
	def isSelected(self):
		return self.isSelected_
		
class AccStatesKeeper:
	"""
	This class keeps quad fields and cavities' apm. and phases dictionaries and a method
	to compare them.
	"""
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document
		#----- array of quad fileds and cavity amp. and phases dicts
		self.accState_arr = []
		self.record_hor_arr = []
		self.record_ver_arr = []
		# phases tolerance is in deg
		self.field_rel_tolerance = 0.01
		self.amp_abs_tolerance = 0.001
		self.field_abs_tolerance = 0.01
		self.phase_tolerance = 0.2
		
	def addRecord(self,ws_lw_size_record):
		dict_in = ws_lw_size_record.getQuadAndCavDict()
		if(ws_lw_size_record.ws_direction == WS_DIRECTION_HOR):
			self.record_hor_arr.append(ws_lw_size_record)
		if(ws_lw_size_record.ws_direction == WS_DIRECTION_VER):
			self.record_ver_arr.append(ws_lw_size_record)			
		for accState in self.accState_arr:
			dict0 = accState.getQuadCavDict()
			res = self.compareDicts(dict0,dict_in)
			if(res):
				ws_lw_size_record.setQuadAndCavDict(dict0)
				accState.addSizeRecord(ws_lw_size_record)
				return
		accState = AccState(dict_in,self.linac_wizard_document)
		accState.addSizeRecord(ws_lw_size_record)
		self.accState_arr.append(accState)

	def clean(self):
		self.accState_arr = []
		self.record_hor_arr = []
		self.record_ver_arr = []

	def getAccStatesArr(self):
		return self.accState_arr
		
	def compareDicts(self,dict0,dict1):
		res = false
		[quad_dict0,cav_amp_phase_dict0] = dict0
		[quad_dict1,cav_amp_phase_dict1] = dict1
		for quad in quad_dict0.keys():
			if(quad_dict1.has_key(quad)):
				field0 = quad_dict0[quad]
				field1 = quad_dict1[quad]
				if(field0 == 0.):
					if(field0 == field1):
						continue
					else:
						if(math.fabs(field0 - field1) > self.field_abs_tolerance):
							return res
						else:
							continue
				else:
					if(math.fabs((field0 - field1)/field0) > self.field_rel_tolerance):
						return res
					continue
			else:
				return res
		for cav in cav_amp_phase_dict0.keys():
			res = false
			[amp0,phase0] = cav_amp_phase_dict0[cav]
			[amp1,phase1] = cav_amp_phase_dict1[cav]
			if(math.fabs((phase0 - phase1)) > self.phase_tolerance):
				return res
			if(math.fabs((amp0 - amp1)) > self.amp_abs_tolerance):
				return res
		return true

	def writeDataToXML(self,root_da):
		accStates_Arr_da = root_da.createChild("Accelerator_States_Arr_Analysis_Panel")
		for accState in self.accState_arr:
			accState_da = accStates_Arr_da.createChild("Accelerator_State")
			accState_da.setValue("isOn", accState.isOn)
			tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
			dict_panel = tr_twiss_analysis_controller.dict_panel
			button_ind = -1
			if(dict_panel.gaussButton.isSelected()): button_ind = 0
			if(dict_panel.custom_gaussButton.isSelected()): button_ind = 1
			if(dict_panel.custom_rmsButton.isSelected()): button_ind = 2
			accState_da.setValue("fit_button_ind", button_ind)
			dumpGraphDataToDA(accState.gd_model_hor,accState_da,"gd_model_hor","%12.5f","%12.5f")
			dumpGraphDataToDA(accState.gd_model_ver,accState_da,"gd_model_ver","%12.5f","%12.5f")
			dumpGraphDataToDA(accState.gd_model_lon,accState_da,"gd_model_lon","%12.5f","%12.5f")
			size_record_da = accState_da.createChild("HOR_RECORDS")
			for ws_lw_size_record in accState.size_hor_record_arr:
				ws_lw_size_record_da = size_record_da.createChild(ws_lw_size_record.ws_node.getId())
				ws_lw_size_record_da.setValue("isOn", ws_lw_size_record.isOn)
				ws_lw_size_record_da.setValue("index", ws_lw_size_record.index)
			size_record_da = accState_da.createChild("VER_RECORDS")
			for ws_lw_size_record in accState.size_ver_record_arr:
				ws_lw_size_record_da = size_record_da.createChild(ws_lw_size_record.ws_node.getId())
				ws_lw_size_record_da.setValue("isOn", ws_lw_size_record.isOn)
				ws_lw_size_record_da.setValue("index", ws_lw_size_record.index)
				
	def readDataFromXML(self,root_da):
		accStates_Arr_da = root_da.childAdaptor("Accelerator_States_Arr_Analysis_Panel")
		if(accStates_Arr_da == null): return
		accState_isOn_arr = []
		for accState_da in accStates_Arr_da.childAdaptors("Accelerator_State"):
			acc_state_isOn = Boolean(accState_da.intValue("isOn")).booleanValue()
			record_isOn_arr = []
			record_ind_arr = []
			records_da = accState_da.childAdaptor("HOR_RECORDS")
			for ws_lw_size_record_da in records_da.childAdaptors():
				ws_lw_size_record_isOn = Boolean(ws_lw_size_record_da.intValue("isOn")).booleanValue()
				ws_lw_size_record_ind = ws_lw_size_record_da.intValue("index")
				record_isOn_arr.append(ws_lw_size_record_isOn)
				record_ind_arr.append(ws_lw_size_record_ind)
			records_da = accState_da.childAdaptor("VER_RECORDS")
			for ws_lw_size_record_da in records_da.childAdaptors():
				ws_lw_size_record_isOn = Boolean(ws_lw_size_record_da.intValue("isOn")).booleanValue()
				ws_lw_size_record_ind = ws_lw_size_record_da.intValue("index")
				record_isOn_arr.append(ws_lw_size_record_isOn)
				record_ind_arr.append(ws_lw_size_record_ind)
			accState_isOn_arr.append([acc_state_isOn,record_isOn_arr,record_ind_arr])
		#-------now make AccState data for Twiss analysis -------------------------------------
		linac_wizard_document = self.linac_wizard_document
		ws_data_analysis_controller = linac_wizard_document.ws_lw_controller.ws_data_analysis_controller
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		ws_rec_table_element_arr = ws_records_table_model.ws_rec_table_element_arr			
		tr_twiss_analysis_controller = linac_wizard_document.tr_twiss_analysis_controller
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		accStatesKeeper.clean()			
		for [acc_state_isOn,record_isOn_arr,record_ind_arr] in accState_isOn_arr:
			for itr in range(len(record_isOn_arr)):
				ws_lw_size_record_ind = record_ind_arr[itr]
				ws_lw_size_record_isOn = record_isOn_arr[itr]
				#---- index of the ws_scan_and_fit_record is shifted by 1 to show it in the tables
				ws_scan_and_fit_record = ws_rec_table_element_arr[ws_lw_size_record_ind-1]
				ws_scan_and_fit_record.pos = linac_wizard_document.accSeq.getPosition(ws_scan_and_fit_record.ws_node)
				ws_lw_size_record = tr_twiss_analysis_controller.makeWS_LW_Size_Record()
				ws_lw_size_record.setUpParams(ws_scan_and_fit_record)
				accStatesKeeper.addRecord(ws_lw_size_record)
				ws_lw_size_record.isOn = ws_lw_size_record_isOn
		dict_panel = tr_twiss_analysis_controller.dict_panel				
		count = 0
		for accState_da in accStates_Arr_da.childAdaptors("Accelerator_State"):
			accState = self.accState_arr[count]
			accState.isOn = accState_isOn_arr[count][0]		
			readGraphDataFromDA(accState.gd_model_hor,accState_da,"gd_model_hor")	
			readGraphDataFromDA(accState.gd_model_ver,accState_da,"gd_model_ver")	
			readGraphDataFromDA(accState.gd_model_lon,accState_da,"gd_model_lon")
			button_ind = accState_da.intValue("fit_button_ind")
			if(button_ind == 0): dict_panel.gaussButton.setSelected(true)
			if(button_ind == 1): dict_panel.custom_gaussButton.setSelected(true)
			if(button_ind == 2): dict_panel.custom_rmsButton.setSelected(true)			
			count += 1
		tr_twiss_analysis_controller.dict_panel.dict_table.getModel().fireTableDataChanged()	
		
	def updateGraphData(self):
		for accState in self.accState_arr:
			accState.updateGraphData()
			
	def resyncScenario(self):
		for accState in self.accState_arr:
			accState.resyncScenario()

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------

class Dump_Quad_Fields_Button_Listener(ActionListener):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		
	def actionPerformed(self,actionEvent):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		quad_cav_dict = accState.getQuadCavDict()
		[quad_dict,cav_amp_phase_dict] = quad_cav_dict
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		#----------set the output file
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Quad Gradients to ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")		
			#----- start dump
			for quad in quads:
				if(quad_dict.has_key(quad)):
					txt = quad.getId()+" %7.4f "%quad_dict[quad]
					fl_out.write(txt+"\n")
			fl_out.close()		

class Dump_Cav_Amps_Phases_Button_Listener(ActionListener):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		
	def actionPerformed(self,actionEvent):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		quad_cav_dict = accState.getQuadCavDict()
		[quad_dict,cav_amp_phase_dict] = quad_cav_dict
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		#----------set the output file
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Cavities Amps.&Phases to ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")		
			#----- start dump
			for cav in cavs:
				if(cav_amp_phase_dict.has_key(cav)):
					txt = cav.getId()+" %8.6f  %7.3f "%(cav_amp_phase_dict[cav][0],cav_amp_phase_dict[cav][1])
					fl_out.write(txt+"\n")
			fl_out.close()

class Read_Cav_Amps_Phases_Button_Listener(ActionListener):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		
	def actionPerformed(self,actionEvent):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		quad_cav_dict = accState.getQuadCavDict()
		[quad_dict,cav_amp_phase_dict] = quad_cav_dict
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		#----------set the output file
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read Cavities Amps.&Phases from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"r")	
			lns = fl_out.readlines()
			fl_out.close()
			cav_name_amp_phase_dict = {}
			for ln in lns:
				res = ln.split()
				if(len(res) == 3):
					cav_name_amp_phase_dict[res[0]]=(float(res[1]),float(res[2]))
			#----- start dump
			for cav in cavs:
				if(cav_amp_phase_dict.has_key(cav)):
					if(cav_name_amp_phase_dict.has_key(cav.getId())):
						(amp,phase) = cav_name_amp_phase_dict[cav.getId()]
						cav_amp_phase_dict[cav][0] = amp
						cav_amp_phase_dict[cav][1] = phase
			self.transverse_twiss_analysis_Controller.quad_cav_params_tables_panel.cav_amp_phases_table.getModel().fireTableDataChanged()

class FitParam_Buttons_Listener(ActionListener):
	def __init__(self,transverse_twiss_analysis_Controller, selected_index):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.selected_index = selected_index
		
	def actionPerformed(self,actionEvent):
		if(actionEvent.getSource().isSelected()):
			self.transverse_twiss_analysis_Controller.fit_param_index = self.selected_index
			index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
			if(index < 0): return
			accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
			self.transverse_twiss_analysis_Controller.graphs_panel.updateGraphData()
	
class QuadCavDict_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		if(index < 0):
			for accState in self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr():
				accState.setSelection(false)
			self.transverse_twiss_analysis_Controller.graphs_panel.updateGraphData()
			self.transverse_twiss_analysis_Controller.hor_size_table.getModel().fireTableDataChanged()
			self.transverse_twiss_analysis_Controller.ver_size_table.getModel().fireTableDataChanged()
			return 
		for accState in self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr():
			accState.setSelection(false)
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		accState.setSelection(true)
		self.transverse_twiss_analysis_Controller.graphs_panel.updateGraphData()
		self.transverse_twiss_analysis_Controller.hor_size_table.getModel().fireTableDataChanged()
		self.transverse_twiss_analysis_Controller.ver_size_table.getModel().fireTableDataChanged()
		self.transverse_twiss_analysis_Controller.quad_cav_params_tables_panel.quad_fields_table.getModel().fireTableDataChanged()
		self.transverse_twiss_analysis_Controller.quad_cav_params_tables_panel.cav_amp_phases_table.getModel().fireTableDataChanged()

#------------------------------------------------
#  JTable models
#------------------------------------------------
class Tr_Size_Table_Model(AbstractTableModel):
	def __init__(self,ws_direction,transverse_twiss_analysis_Controller):
		self.ws_direction = ws_direction
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.columnNames = ["#","WS/LW","Pos.[m]","Use","S,[mm]","Gauss","RMS"]
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(2)
		self.boolean_class = Boolean(true).getClass()
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		accStatesKeeper = self.transverse_twiss_analysis_Controller.accStatesKeeper
		accState = null
		for accState0 in accStatesKeeper.getAccStatesArr():
			if(accState0.isSelected()):
				accState = accState0
				break
		if(accState == null): return 0
		nrow  = len(accState.size_hor_record_arr)
		if(self.ws_direction == WS_DIRECTION_VER):
			nrow  = len(accState.size_ver_record_arr)
		return nrow		

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		accStatesKeeper = self.transverse_twiss_analysis_Controller.accStatesKeeper
		accState = null
		for accState0 in accStatesKeeper.getAccStatesArr():
			if(accState0.isSelected()):
				accState = accState0
				break	
		size_record_arr = accState.size_hor_record_arr
		if(self.ws_direction == WS_DIRECTION_VER):
			 size_record_arr = accState.size_ver_record_arr
		if(col == 0): return self.nf.format(size_record_arr[row].index)
		if(col == 1): return size_record_arr[row].ws_node.getId()
		if(col == 2): return self.nf.format(size_record_arr[row].pos)
		if(col == 3): return Boolean(size_record_arr[row].isOn)
		if(col == 4): return self.nf.format(size_record_arr[row].gauss_sigma)
		if(col == 5): return self.nf.format(size_record_arr[row].custom_gauss_sigma)
		if(col == 6): return self.nf.format(size_record_arr[row].custom_rms_sigma)
				
	def getColumnClass(self,col):
		if(col != 3):
			return self.string_class
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 3 or col == 4 or col == 5 or col == 6 ):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		accStatesKeeper = self.transverse_twiss_analysis_Controller.accStatesKeeper
		accState = null
		for accState0 in accStatesKeeper.getAccStatesArr():
			if(accState0.isSelected()):
				accState = accState0
				break
		size_record_arr = accState.size_hor_record_arr
		if(self.ws_direction == WS_DIRECTION_VER):
			 size_record_arr = accState.size_ver_record_arr			
		if(col == 3):
			size_record_arr[row].isOn = value
			self.fireTableCellUpdated(row, col)
		if(col == 4):
			size_record_arr[row].gauss_sigma = Double.parseDouble(value)
		if(col == 5):
			size_record_arr[row].custom_gauss_sigma = Double.parseDouble(value)
		if(col == 6):
			size_record_arr[row].custom_rms_sigma = Double.parseDouble(value)	
		self.transverse_twiss_analysis_Controller.graphs_panel.updateGraphData()
		
class QuadCavDict_Table_Model(AbstractTableModel):
	def __init__(self,accStatesKeeper):
		self.accStatesKeeper = accStatesKeeper
		self.columnNames = ["#","Use"]
		self.boolean_class = Boolean(true).getClass()
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.accStatesKeeper.getAccStatesArr())

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0): return row
		return Boolean(self.accStatesKeeper.getAccStatesArr()[row].isOn)
				
	def getColumnClass(self,col):
		if(col == 0):
			return self.string_class
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 1):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 1):
			self.accStatesKeeper.getAccStatesArr()[row].isOn = value
			self.fireTableCellUpdated(row, col)

class Quad_Fileds_Table_Model(AbstractTableModel):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.columnNames = ["Quad","Pos.[m]","Field [T/m]"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return 0
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		return len(quads)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return ""	
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		quad_cav_dict = accState.getQuadCavDict()
		[quad_dict,cav_amp_phase_dict] = quad_cav_dict	
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		accSeq = linac_wizard_document.accSeq
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs	
		quad = quads[row]
		if(col == 0): return quad.getId()
		if(col == 1): return " %8.3f "%accSeq.getPosition(quad)
		if(col == 2 and quad_dict.has_key(quad)): return " %8.4f "%quad_dict[quad]
		return ""
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 2):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 2):
			index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
			if(index < 0): return 
			accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
			quad_cav_dict = accState.getQuadCavDict()
			[quad_dict,cav_amp_phase_dict] = quad_cav_dict	
			linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
			accSeq = linac_wizard_document.accSeq
			quads = linac_wizard_document.ws_lw_controller.quads
			cavs = linac_wizard_document.ws_lw_controller.cavs	
			quad = quads[row]			
			quad_dict[quad] =  Double.parseDouble(value)
			self.fireTableCellUpdated(row, col)

class Cav_Amp_Phases_Table_Model(AbstractTableModel):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.columnNames = ["Cavity","Pos.[m]","Amp.[MV/m]","Phase[deg]"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return 0
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		return len(cavs)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
		if(index < 0): return ""	
		accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
		quad_cav_dict = accState.getQuadCavDict()
		[quad_dict,cav_amp_phase_dict] = quad_cav_dict	
		linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
		accSeq = linac_wizard_document.accSeq
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs	
		cav = cavs[row]
		if(col == 0): return cav.getId()
		if(col == 1): return " %8.3f "%accSeq.getPosition(cav)
		if(col == 2 and cav_amp_phase_dict.has_key(cav)): return " %8.4f "%cav_amp_phase_dict[cav][0]
		if(col == 3 and cav_amp_phase_dict.has_key(cav)): return " %7.2f "%cav_amp_phase_dict[cav][1]
		return ""
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 2 or col == 3):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 2 or col == 3):
			index = self.transverse_twiss_analysis_Controller.dict_panel.dict_table.getSelectedRow()
			if(index < 0): return 
			accState = self.transverse_twiss_analysis_Controller.accStatesKeeper.getAccStatesArr()[index]
			quad_cav_dict = accState.getQuadCavDict()
			[quad_dict,cav_amp_phase_dict] = quad_cav_dict	
			linac_wizard_document = self.transverse_twiss_analysis_Controller.linac_wizard_document
			accSeq = linac_wizard_document.accSeq
			quads = linac_wizard_document.ws_lw_controller.quads
			cavs = linac_wizard_document.ws_lw_controller.cavs	
			cav = cavs[row]		
			if(col == 2):
				cav_amp_phase_dict[cav][0] =  Double.parseDouble(value)
			if(col == 3):
				cav_amp_phase_dict[cav][1] =  Double.parseDouble(value)
			self.fireTableCellUpdated(row, col)

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class Graphs_Panel(JPanel):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.setLayout(GridLayout(3,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.gpanel_hor = FunctionGraphsJPanel()
		self.gpanel_ver = FunctionGraphsJPanel()
		self.gpanel_lon = FunctionGraphsJPanel()		
		self.gpanel_hor.setLegendButtonVisible(true)
		self.gpanel_ver.setLegendButtonVisible(true)
		self.gpanel_lon.setLegendButtonVisible(true)
		self.gpanel_hor.setName("Horizontal Size")
		self.gpanel_hor.setAxisNames("pos, [m]","Size, [mm]")	
		self.gpanel_ver.setName("Vertical Size")
		self.gpanel_ver.setAxisNames("pos, [m]","Size, [mm]")	
		self.gpanel_lon.setName("Longitudinal Size, RF Freq. = 402.5 MHz")
		self.gpanel_lon.setAxisNames("pos, [m]","Size, [deg]")	
		self.gpanel_hor.setBorder(etched_border)
		self.gpanel_ver.setBorder(etched_border)
		self.gpanel_lon.setBorder(etched_border)
		self.add(self.gpanel_hor)
		self.add(self.gpanel_ver)	
		self.add(self.gpanel_lon)
		
	def removeAllGraphData(self):
		self.gpanel_hor.removeAllGraphData()
		self.gpanel_ver.removeAllGraphData()
		self.gpanel_lon.removeAllGraphData()		
		
	def updateGraphData(self):
		self.gpanel_hor.removeAllGraphData()
		self.gpanel_ver.removeAllGraphData()
		self.gpanel_lon.removeAllGraphData()
		accStatesKeeper = self.transverse_twiss_analysis_Controller.accStatesKeeper
		accStatesKeeper.updateGraphData()
		accState = null
		for accState0 in accStatesKeeper.getAccStatesArr():
			if(accState0.isSelected()):
				accState = accState0
				break
		if(accState == null): return
		accState.updateGraphData()
		self.gpanel_hor.addGraphData(accState.gd_exp_hor)
		self.gpanel_hor.addGraphData(accState.gd_model_hor)
		self.gpanel_ver.addGraphData(accState.gd_exp_ver)
		self.gpanel_ver.addGraphData(accState.gd_model_ver)
		self.gpanel_lon.addGraphData(accState.gd_model_lon)
		#---set up vertical lines
		self.gpanel_hor.removeHorizontalValues()
		self.gpanel_ver.removeHorizontalValues()
		self.gpanel_lon.removeHorizontalValues()
		self.gpanel_hor.removeVerticalValues()
		self.gpanel_ver.removeVerticalValues()
		self.gpanel_lon.removeVerticalValues()
		accSeq = self.transverse_twiss_analysis_Controller.linac_wizard_document.accSeq
		quads = accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
		cavs = accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(RfCavity.s_strType))).andStatus(true))
		for quad in quads:
			pos = accSeq.getPosition(quad)
			self.gpanel_hor.addVerticalLine(pos,Color.BLACK)
			self.gpanel_ver.addVerticalLine(pos,Color.BLACK)
			self.gpanel_lon.addVerticalLine(pos,Color.BLACK)															 
		for cav in cavs:	
			pos = accSeq.getPosition(cav)	
			self.gpanel_hor.addVerticalLine(pos,Color.RED)
			self.gpanel_ver.addVerticalLine(pos,Color.RED)
			self.gpanel_lon.addVerticalLine(pos,Color.RED)
	
		
class Quad_and_Cav_Params_Table_Panel(JPanel):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.setLayout(GridLayout(1,2))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		#--------- quad and cavities params tables
		self.quad_fields_table_model = Quad_Fileds_Table_Model(transverse_twiss_analysis_Controller)
		self.quad_fields_table = JTable(self.quad_fields_table_model)
		self.quad_fields_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.quad_fields_table.setFillsViewportHeight(true)	
		quad_table_panel = JPanel(BorderLayout())
		quad_table_panel.add(JScrollPane(self.quad_fields_table), BorderLayout.CENTER)		
		self.cav_amp_phases_table_model = Cav_Amp_Phases_Table_Model(transverse_twiss_analysis_Controller)
		self.cav_amp_phases_table = JTable(self.cav_amp_phases_table_model)
		self.cav_amp_phases_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cav_amp_phases_table.setFillsViewportHeight(true)	
		cav_table_panel = JPanel(BorderLayout())
		cav_table_panel.add(JScrollPane(self.cav_amp_phases_table), BorderLayout.CENTER)
		#---------------------------------------
		self.add(quad_table_panel)
		self.add(cav_table_panel)
		

class QuadCavDictSelection_Panel	(JPanel):
	def __init__(self,transverse_twiss_analysis_Controller):
		self.transverse_twiss_analysis_Controller = transverse_twiss_analysis_Controller
		self.setLayout(BorderLayout())
		#-----------dict table panel
		etched_border = BorderFactory.createEtchedBorder()
		border = BorderFactory.createTitledBorder(etched_border,"Quad and Cavities Amp.&Phases Sets")
		self.setBorder(border)		
		self.quad_cav_dict_table_model = QuadCavDict_Table_Model(self.transverse_twiss_analysis_Controller.accStatesKeeper)
		self.dict_table = JTable(self.quad_cav_dict_table_model)
		self.dict_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.dict_table.setFillsViewportHeight(true)
		self.dict_table.setPreferredScrollableViewportSize(Dimension(120,100))
		self.dict_table.getSelectionModel().addListSelectionListener(QuadCavDict_Table_Selection_Listener(self.transverse_twiss_analysis_Controller))
		#--------buttons panel
		button_panel = JPanel(BorderLayout())
		self.gaussButton = JRadioButton("Use Ext. Gauss Fit")
		self.custom_gaussButton = JRadioButton("Use Gauss Fit")
		self.custom_rmsButton = JRadioButton("Use RMS")
		button_group = ButtonGroup()
		button_group.add(self.gaussButton)
		button_group.add(self.custom_gaussButton)
		button_group.add(self.custom_rmsButton)
		button_group.clearSelection()
		self.gaussButton.setSelected(true)
		button_panel0 = JPanel(FlowLayout(FlowLayout.LEFT,2,2))
		button_panel0.add(self.gaussButton)
		button_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,2,2))
		button_panel1.add(self.custom_gaussButton)
		button_panel2 = JPanel(FlowLayout(FlowLayout.LEFT,2,2))
		button_panel2.add(self.custom_rmsButton)
		button_panel012 = JPanel(GridLayout(3,1))
		button_panel012.add(button_panel0)
		button_panel012.add(button_panel1)
		button_panel012.add(button_panel2)
		#-------new buttons-----
		button_bottom_panel = JPanel(FlowLayout(FlowLayout.LEFT,2,2))
		button_bottom_panel0 = JPanel(GridLayout(3,1,2,2))
		dump_quad_fields_button = JButton("Dump Quad Fields to ASCII")
		dump_quad_fields_button.addActionListener(Dump_Quad_Fields_Button_Listener(self.transverse_twiss_analysis_Controller))
		button_bottom_panel0.add(dump_quad_fields_button)
		dump_cav_amps_phases_button = JButton("Dump. Cav Amps. Phases to ASCII")
		dump_cav_amps_phases_button.addActionListener(Dump_Cav_Amps_Phases_Button_Listener(self.transverse_twiss_analysis_Controller))
		button_bottom_panel0.add(dump_cav_amps_phases_button)	
		read_cav_amps_phases_button = JButton("Read Cav Amps. Phases from ASCII")
		read_cav_amps_phases_button.addActionListener(Read_Cav_Amps_Phases_Button_Listener(self.transverse_twiss_analysis_Controller))
		button_bottom_panel0.add(read_cav_amps_phases_button)
		button_bottom_panel.add(button_bottom_panel0)
		#----- final knobs panel 
		button_panel.add(button_panel012,BorderLayout.NORTH)
		button_panel.add(button_bottom_panel,BorderLayout.SOUTH)
		self.gaussButton.addActionListener(FitParam_Buttons_Listener(self.transverse_twiss_analysis_Controller,0))
		self.custom_gaussButton.addActionListener(FitParam_Buttons_Listener(self.transverse_twiss_analysis_Controller,1))
		self.custom_rmsButton.addActionListener(FitParam_Buttons_Listener(self.transverse_twiss_analysis_Controller,2))
		#----------------------------------------------------------
		self.add(JScrollPane(self.dict_table), BorderLayout.WEST)
		self.add(button_panel, BorderLayout.CENTER)

	def getMainPanel(self):
		return self
	
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class Transverse_Twiss_Analysis_Controller:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document
		#----		fit_param_index = 0,1,2 gauss, custom gauss, custom rms
		self.fit_param_index = 0
		#--------------------------------------------------------------
		self.accStatesKeeper = AccStatesKeeper(self.linac_wizard_document)
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()		
		#---------hor. ver. graph panels
		self.graphs_panel = Graphs_Panel(self)
		#---------quads' and cavities' parameters tables panels
		self.quad_cav_params_tables_panel = Quad_and_Cav_Params_Table_Panel(self)
		#-------- tabbed panel with graphs and tables
		self.graphs_and_tables_tabbed_panel = JTabbedPane()
		self.graphs_and_tables_tabbed_panel.add("Hor. Ver. Long. Plots",self.graphs_panel)
		self.graphs_and_tables_tabbed_panel.add("Cavities and Quads Tables",self.quad_cav_params_tables_panel)
		#------ sets selection table
		self.dict_panel = QuadCavDictSelection_Panel(self)		
		#---------H and V Size Tables
		hor_size_table_border = BorderFactory.createTitledBorder(etched_border,"Horizontal")
		ver_size_table_border = BorderFactory.createTitledBorder(etched_border,"Vertical")
		self.hor_size_table_model = Tr_Size_Table_Model(WS_DIRECTION_HOR,self)
		self.hor_size_table = JTable(self.hor_size_table_model)
		self.hor_size_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.hor_size_table.setFillsViewportHeight(true)	
		hor_size_panel = JPanel(BorderLayout())
		hor_size_panel.setBorder(hor_size_table_border)
		hor_size_panel.add(JScrollPane(self.hor_size_table), BorderLayout.CENTER)
		self.ver_size_table_model = Tr_Size_Table_Model(WS_DIRECTION_VER,self)
		self.ver_size_table = JTable(self.ver_size_table_model)
		self.ver_size_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.ver_size_table.setFillsViewportHeight(true)	
		ver_size_panel = JPanel(BorderLayout())
		ver_size_panel.setBorder(ver_size_table_border)
		ver_size_panel.add(JScrollPane(self.ver_size_table), BorderLayout.CENTER)
		sizes_table_panel = JPanel(GridLayout(3,1))
		#-----------column sizes ---------
		columnModel = self.hor_size_table.getColumnModel()
		columnModel.getColumn(0).setPreferredWidth(30)
		columnModel.getColumn(1).setPreferredWidth(120)
		columnModel.getColumn(2).setPreferredWidth(60)
		columnModel.getColumn(3).setPreferredWidth(30)
		columnModel.getColumn(4).setPreferredWidth(50)
		columnModel.getColumn(5).setPreferredWidth(50)
		columnModel.getColumn(6).setPreferredWidth(50)	
		columnModel = self.ver_size_table.getColumnModel()
		columnModel.getColumn(0).setPreferredWidth(30)
		columnModel.getColumn(1).setPreferredWidth(120)
		columnModel.getColumn(2).setPreferredWidth(60)
		columnModel.getColumn(3).setPreferredWidth(30)
		columnModel.getColumn(4).setPreferredWidth(50)
		columnModel.getColumn(5).setPreferredWidth(50)
		columnModel.getColumn(6).setPreferredWidth(50)
		sizes_table_panel.add(self.dict_panel.getMainPanel())
		sizes_table_panel.add(hor_size_panel)
		sizes_table_panel.add(ver_size_panel)
		#---------Transverse Fitting Controller
		self.transverse_twiss_fitting_controller = Transverse_Twiss_Fitting_Controller(self.linac_wizard_document)
		#----add panels to the main
		self.main_panel.add(self.graphs_and_tables_tabbed_panel, BorderLayout.CENTER)
		self.main_panel.add(sizes_table_panel, BorderLayout.WEST)
		self.main_panel.add(self.transverse_twiss_fitting_controller.getMainPanel(), BorderLayout.SOUTH)
		
	def getMainPanel(self):
		return self.main_panel		
		
	def makeWS_LW_Size_Record(self):
		return WS_LW_Size_Record()
		
		

