# The SCL Wizard read and write data Controller

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from javax.swing.filechooser import FileNameExtensionFilter
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
from java.util import Date, ArrayList
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.io import File

from xal.extension.widgets.plot import BasicGraphData 
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.tools.xml import XmlDataAdaptor
from xal.tools.data import DataAdaptor
from xal.smf import AcceleratorSeqCombo

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from ws_lw_acquisition_cntrl_lib import WS_DIRECTION_HOR,WS_DIRECTION_VER
from ws_lw_acquisition_cntrl_lib import WS_Scan_Record, WS_Scan_and_Fit_Record

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------
# Related classes: 
# WS_LW_Acquisition_Controller
# SCL_Long_TuneUp_Controller
#------------------------------------------------------------

def dumpGraphDataToDA(gd,gd_da,title,py_x_format = "%12.5g",py_y_format = "%12.5g"):
	txt_x_arr = ""
	txt_y_arr = ""
	for i in range(gd.getNumbOfPoints()):
		txt_x_arr += " "+py_x_format%gd.getX(i)
		txt_y_arr += " "+py_y_format%gd.getY(i)
	xy_da = gd_da.createChild(title)
	x_arr_da = xy_da.createChild("x")
	x_arr_da.setValue("arr",txt_x_arr)
	y_arr_da = xy_da.createChild("y")
	y_arr_da.setValue("arr",txt_y_arr)
	legend_da = xy_da.createChild("legend")
	legend_obj = gd.getGraphProperty(GRAPH_LEGEND_KEY)
	if(legend_obj != null):
		legend_da.setValue("legend",legend_obj)
	
def readGraphDataFromDA(gd,gd_root_da,title):
	x_arr = []
	y_arr = []	
	if(gd != null): gd.removeAllPoints()
	gd_da_list = gd_root_da.childAdaptors(title)
	if(gd_da_list.isEmpty()): return (x_arr,y_arr)
	gd_da = gd_da_list.get(0)
	dg_x_da = gd_da.childAdaptor("x")	
	txt_x_arr = dg_x_da.stringValue("arr")
	dg_y_da = gd_da.childAdaptor("y")	
	txt_y_arr = dg_y_da.stringValue("arr")	
	res_x_arr = txt_x_arr.split()
	res_y_arr = txt_y_arr.split()
	for i in range(len(res_x_arr)):
		x_arr.append(float(res_x_arr[i]))
		y_arr.append(float(res_y_arr[i]))
	#---- this addition will eleminate the same x-points
	x_arr_tmp = x_arr[:]
	y_arr_tmp = y_arr[:]
	if(len(x_arr_tmp) > 0):
		x_arr = [x_arr_tmp[0],]
		y_arr = [y_arr_tmp[0],]
	for ix in range(1,len(x_arr_tmp)):
		if(x_arr_tmp[ix] !=  x_arr_tmp[ix-1]):
			x_arr.append(x_arr_tmp[ix])
			y_arr.append(y_arr_tmp[ix])
	#---------------------------------------------------
	if(gd != null): gd.addPoint(x_arr,y_arr)
	legend_da = gd_da.childAdaptor("legend")
	if(legend_da != null):
		if(gd != null): gd.setGraphProperty(GRAPH_LEGEND_KEY,legend_da.stringValue("legend"))
	return (x_arr,y_arr)

#-------------------------------------------
#   Action Listeners
#-------------------------------------------
class WS_LW_Data_Write_Listener(ActionListener):
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document

	def actionPerformed(self,actionEvent):
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save data into the file ...")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("SCL Wizard",["sclw",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".sclw") != (len(fl_path) - 5)):
				fl_out = File(fl_out.getPath()+".sclw")
			io_controller = self.linac_wizard_document.getIO_Controller()
			io_controller.writeData(fl_out.toURI().toURL())
			io_controller.old_fl_out_name = fl_out.getName()
			self.linac_wizard_document.linac_wizard_window.setTitle(io_controller.old_fl_out_name)

class WS_LW_Data_Read_Listener(ActionListener):
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document

	def actionPerformed(self,actionEvent):
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read data from the file ...")
		fc.setApproveButtonText("Open")
		fl_filter = FileNameExtensionFilter("SCL Wizard",["sclw",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			io_controller = self.linac_wizard_document.getIO_Controller()
			io_controller.readData(fl_in.toURI().toURL())
			io_controller.old_fl_out_name = fl_in.getName()
			self.linac_wizard_document.linac_wizard_window.setTitle(io_controller.old_fl_out_name)
			
#-------------------------------------------
#         I/O Controller
#-------------------------------------------
class WS_Wizard_IO_Controller:
	def __init__(self, linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document		
		self.accl = self.linac_wizard_document.accl
		self.old_fl_out_name = null

	#-------------------------------------------------
	#  Read methods
	#-------------------------------------------------		
	def readData(self, url):
		ws_lw_acquisition_controller = self.linac_wizard_document.getWS_LW_Controller()
		da = XmlDataAdaptor.adaptorForUrl(url,false)
		root_da = da.childAdaptor("LINAC_Wizard")		
		title = root_da.stringValue("Title")
		seqNames = root_da.stringValue("AccSequence").split()
		if(len(seqNames) != 0): 
			#------set up selected names in the SetUp tables
			linac_setup_controller	= self.linac_wizard_document.getSetUp_Controller()
			linac_setup_controller.setSelectedSequences(seqNames[0],seqNames[len(seqNames)-1])
			#-----------------------------------------------
			lst = ArrayList()
			for seqName in seqNames:
				lst.add(self.accl.getSequence(seqName))
			accSeq = AcceleratorSeqCombo("SEQUENCE", lst)
			self.linac_wizard_document.ws_lw_controller.cleanOldWSdata()
			self.linac_wizard_document.setAccSeq(accSeq)
			nodes = accSeq.getAllNodes(true)
			name_to_node_dict = {}
			for node in nodes:
				name_to_node_dict[node.getId()] = node	
			#-----add cavities to the nodes dictinary ------------
			for cav in ws_lw_acquisition_controller.cavs:
				name_to_node_dict[cav.getId()] = cav	
			#----read WS or LW wave_form data
			self.readData_WS_LW_Records(root_da,name_to_node_dict)
			#----read Initial Twiss and beam parameters
			self.readTransverseTwiss(root_da)
		#---- read SCL Phase Scan data
		self.linac_wizard_document.scl_long_tuneup_controller.clean()
		self.linac_wizard_document.scl_long_tuneup_controller.readDataFromXML(root_da)		
		self.linac_wizard_document.scl_long_tuneup_controller.updateAllTables()

	def readData_WS_LW_Records(self, root_da,name_to_node_dict):
		ws_lw_acquisition_controller = self.linac_wizard_document.getWS_LW_Controller()
		ws_records_da = root_da.childAdaptor("WS_LW_Wave_Form_and_Params_Panel")
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		ws_data_analysis_controller = ws_lw_acquisition_controller.ws_data_analysis_controller
		ws_data_analysis_controller.ws_records_table_model.ws_rec_table_element_arr = []		
		ws_rec_table_element_arr = ws_data_analysis_controller.ws_records_table_model.ws_rec_table_element_arr
		index_count = 0
		for ws_rec_da in ws_records_da.childAdaptors():
			index_count += 1
			ws_name = ws_rec_da.stringValue("Name")
			ws_node = name_to_node_dict[ws_name]
			direction = ws_rec_da.intValue("direction")
			#print "debug name=",ws_name," dir=",direction
			rec_isOn = true
			if(ws_rec_da.hasAttribute("isOn")):			
				rec_isOn = Boolean(ws_rec_da.intValue("isOn")).booleanValue()
			ws_scan_rec = WS_Scan_Record(ws_node,direction)
			(wf_x_arr,wf_y_arr) = readGraphDataFromDA(null,ws_rec_da,"wave_form")
			ws_scan_rec.setWF(wf_x_arr,wf_y_arr)
			ws_scan_and_fit_rec = WS_Scan_and_Fit_Record(ws_scan_rec)
			ws_scan_and_fit_rec.isOn = rec_isOn
			ws_scan_and_fit_rec.gauss_sigma = ws_rec_da.doubleValue("gauss_sigma")
			ws_scan_and_fit_rec.custom_gauss_sigma = ws_rec_da.doubleValue("custom_gauss_sigma")
			ws_scan_and_fit_rec.custom_rms_sigma = ws_rec_da.doubleValue("custom_rms_sigma")
			gauss_da = ws_rec_da.childAdaptor("gauss_params")	
			ws_scan_and_fit_rec.CONST.setValue(gauss_da.doubleValue("CONST"))
			ws_scan_and_fit_rec.A0.setValue(gauss_da.doubleValue("A0"))
			ws_scan_and_fit_rec.X_CENTER.setValue(gauss_da.doubleValue("CENTER"))
			ws_scan_and_fit_rec.SIGMA.setValue(gauss_da.doubleValue("SIGMA"))
			ws_scan_and_fit_rec.X_MIN.setValue(gauss_da.doubleValue("X_MIN"))
			ws_scan_and_fit_rec.X_MAX.setValue(gauss_da.doubleValue("X_MAX"))
			ws_rec_table_element_arr.append(ws_scan_and_fit_rec)
			ws_scan_and_fit_rec.index = index_count
			#------ read the quad dict ---------------
			dict_quad_da = ws_rec_da.childAdaptor("quad_fields")
			quad_dict = {}
			for quad_da in dict_quad_da.childAdaptors():
				quad = name_to_node_dict[quad_da.stringValue("name")]
				filed = quad_da.doubleValue("field")
				quad_dict[quad] = filed
			#------ read the cavities amp and phases dict ---------------
			dict_cavities_da = ws_rec_da.childAdaptor("rf_amp_phases")
			cav_dict = {}
			for cav_da in dict_cavities_da.childAdaptors():
				#print "debug name=",cav_da.stringValue("name")
				cav = name_to_node_dict[cav_da.stringValue("name")]
				amp = cav_da.doubleValue("amp")
				phase = cav_da.doubleValue("phase")
				cav_dict[cav] = [amp,phase]
			quadCavDict = [quad_dict,cav_dict]
			ws_scan_and_fit_rec.setParamDict(quadCavDict)
		tr_twiss_analysis_controller.accStatesKeeper.readDataFromXML(root_da)
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		
	def readTransverseTwiss(self, root_da):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller
		initial_twiss_params_holder = transverse_twiss_fitting_controller.initial_twiss_params_holder	
		#------------------------------------------------------------------------------
		childs_da_List = root_da.childAdaptors("Init_Trans_Twiss_Transverse_Twiss_Analysis_Controller")
		if(childs_da_List.size() != 1): return
		twiss_and_params_da = childs_da_List.get(0)
		params_da = twiss_and_params_da.childAdaptor("Initial_Parameters")
		eKin = params_da.doubleValue("eKin")
		current = params_da.doubleValue("current")
		fit_err = params_da.doubleValue("fit_err")
		twiss_x_da = twiss_and_params_da.childAdaptor("Initial_X_Twiss")
		twiss_y_da = twiss_and_params_da.childAdaptor("Initial_Y_Twiss")
		twiss_z_da = twiss_and_params_da.childAdaptor("Initial_Z_Twiss")
		(alphaX,betaX,emittX) = (twiss_x_da.doubleValue("alpha"),twiss_x_da.doubleValue("beta"),twiss_x_da.doubleValue("emitt"))
		(alphaY,betaY,emittY) = (twiss_y_da.doubleValue("alpha"),twiss_y_da.doubleValue("beta"),twiss_y_da.doubleValue("emitt"))
		(alphaZ,betaZ,emittZ) = (twiss_z_da.doubleValue("alpha"),twiss_z_da.doubleValue("beta"),twiss_z_da.doubleValue("emitt"))
		(alphaXStep,betaXStep,emittXStep) = (twiss_x_da.doubleValue("alphaStep"),twiss_x_da.doubleValue("betaStep"),twiss_x_da.doubleValue("emittStep"))
		(alphaYStep,betaYStep,emittYStep) = (twiss_y_da.doubleValue("alphaStep"),twiss_y_da.doubleValue("betaStep"),twiss_y_da.doubleValue("emittStep"))
		(alphaZStep,betaZStep,emittZStep) = (twiss_z_da.doubleValue("alphaStep"),twiss_z_da.doubleValue("betaStep"),twiss_z_da.doubleValue("emittStep"))
		(alphaXErr,betaXErr,emittXErr) = (twiss_x_da.doubleValue("alphaErr"),twiss_x_da.doubleValue("betaErr"),twiss_x_da.doubleValue("emittErr"))
		(alphaYErr,betaYErr,emittYErr) = (twiss_y_da.doubleValue("alphaErr"),twiss_y_da.doubleValue("betaErr"),twiss_y_da.doubleValue("emittErr"))
		(alphaZErr,betaZErr,emittZErr) = (twiss_z_da.doubleValue("alphaErr"),twiss_z_da.doubleValue("betaErr"),twiss_z_da.doubleValue("emittErr"))
		#------------------------------------------------------------------------------
		initial_twiss_params_holder.setParams(0,alphaX, betaX, emittX)
		initial_twiss_params_holder.setParams(1,alphaY, betaY, emittY)
		initial_twiss_params_holder.setParams(2,alphaZ, betaZ, emittZ)
		initial_twiss_params_holder.setParamsStep(0,alphaXStep, betaXStep, emittXStep)
		initial_twiss_params_holder.setParamsStep(1,alphaYStep, betaYStep, emittYStep)
		initial_twiss_params_holder.setParamsStep(2,alphaZStep, betaZStep, emittZStep)
		initial_twiss_params_holder.setParamsErr(0,alphaXErr, betaXErr, emittXErr)
		initial_twiss_params_holder.setParamsErr(1,alphaYErr, betaYErr, emittYErr)
		initial_twiss_params_holder.setParamsErr(2,alphaZErr, betaZErr, emittZErr)
		transverse_twiss_fitting_controller.initTwiss_table.getModel().fireTableDataChanged()
		init_and_fit_params_controller.eKin_text.setValue(eKin)
		init_and_fit_params_controller.current_text.setValue(current)	
		init_and_fit_params_controller.fit_err_text.setValue(fit_err)	
		
	#-------------------------------------------------
	#  Write methods
	#-------------------------------------------------
	
	def writeData(self, url):
		da = XmlDataAdaptor.newEmptyDocumentAdaptor()
		root_da = da.createChild("LINAC_Wizard")
		root_da.setValue("Title", url.getFile())
		root_da.setValue("AccSequence", self.getSeqNames())
		#---- write WS or LW data records --------
		self.writeData_WS_LW_Records(root_da)
		#---- write Initial Twiss and beam parameters --------
		self.writeTransverseTwiss(root_da)
		#---- write SCL Phase Scan data
		self.linac_wizard_document.scl_long_tuneup_controller.writeDataToXML(root_da)
		#---- dump data into the file ------------
		da.writeToUrl(url)
	
	def writeData_WS_LW_Records(self, root_da):
		ws_lw_acquisition_controller = self.linac_wizard_document.getWS_LW_Controller()
		ws_records_da = root_da.createChild("WS_LW_Wave_Form_and_Params_Panel")
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		ws_data_analysis_controller = ws_lw_acquisition_controller.ws_data_analysis_controller
		ws_rec_table_element_arr = ws_data_analysis_controller.ws_records_table_model.ws_rec_table_element_arr
		for ws_rec in ws_rec_table_element_arr:
			name = ws_rec.getName()
			ws_direction = ws_rec.ws_direction
			gauss_sigma = ws_rec.gauss_sigma
			custom_gauss_sigma = ws_rec.custom_gauss_sigma
			custom_rms_sigma = ws_rec.custom_rms_sigma
			gd_wf = ws_rec.gd_wf
			ws_rec_da = ws_records_da.createChild("WS_Record")
			ws_rec_da.setValue("Name", name)
			ws_rec_da.setValue("direction", str(ws_direction))
			ws_rec_da.setValue("isOn", ws_rec.isOn)
			ws_rec_da.setValue("gauss_sigma", gauss_sigma)				
			ws_rec_da.setValue("custom_gauss_sigma", custom_gauss_sigma)
			ws_rec_da.setValue("custom_rms_sigma", custom_rms_sigma)
			gauss_da = ws_rec_da.createChild("gauss_params")		
			gauss_da.setValue("CONST", ws_rec.CONST.getValue())	
			gauss_da.setValue("A0", ws_rec.A0.getValue())	
			gauss_da.setValue("CENTER", ws_rec.X_CENTER.getValue())	
			gauss_da.setValue("SIGMA", ws_rec.SIGMA.getValue())
			gauss_da.setValue("X_MIN", ws_rec.X_MIN.getValue())	
			gauss_da.setValue("X_MAX", ws_rec.X_MAX.getValue())
			#----dump wave form ------
			dumpGraphDataToDA(gd_wf,ws_rec_da,"wave_form")
			#----params dict - quads and RF cavities
			[quad_dict,cav_amp_phase_dict] = ws_rec.param_dict
			dict_da = ws_rec_da.createChild("quad_fields")
			self.writeQuadDict(quad_dict,dict_da)
			dict_da = ws_rec_da.createChild("rf_amp_phases")
			self.writeRF_Dict(cav_amp_phase_dict,dict_da)
		tr_twiss_analysis_controller.accStatesKeeper.writeDataToXML(root_da)
				
	def writeQuadDict(self,param_dict, dict_da):
		for quad in param_dict.keys():
			field = param_dict[quad]
			quad_da = dict_da.createChild("quad")
			quad_da.setValue("name", quad.getId())
			quad_da.setValue("field", field)
			
	def writeRF_Dict(self,param_dict, dict_da):
		for cav in param_dict.keys():
			[amp,phase] = param_dict[cav]
			cav_da = dict_da.createChild("cavity")
			cav_da.setValue("name", cav.getId())
			cav_da.setValue("amp", amp)
			cav_da.setValue("phase", phase)
					
	def getSeqNames(self):
		allSeqs = ["MEBT","DTL1","DTL2","DTL3","DTL4","DTL5","DTL6","CCL1","CCL2","CCL3","CCL4","SCLMed","SCLHigh","HEBT1"]
		accSeq = self.linac_wizard_document.accSeq
		if(accSeq == null): return ""
		nodes = accSeq.getNodes()
		node_first = nodes[0]
		node_last = nodes[len(nodes)-1]
		seq_name_start = node_first.getParent().getId()
		seq_name_last = node_last.getParent().getId()
		ind_0 =allSeqs.index(seq_name_start)
		ind_1 =allSeqs.index(seq_name_last)
		if(ind_0 == ind_1):
			return seq_name_start
		else:
			st = ""
			for seq in allSeqs[ind_0:(ind_1+1)]:
				st = st + " "+seq
			return st
			
	def writeTransverseTwiss(self, root_da):
		twiss_and_params_da = root_da.createChild("Init_Trans_Twiss_Transverse_Twiss_Analysis_Controller")
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller
		initial_twiss_params_holder = transverse_twiss_fitting_controller.initial_twiss_params_holder	
		(alphaX, betaX, emittX) = initial_twiss_params_holder.getParams(0)
		(alphaY, betaY, emittY) = initial_twiss_params_holder.getParams(1)
		(alphaZ, betaZ, emittZ) = initial_twiss_params_holder.getParams(2)
		(alphaXStep, betaXStep, emittXStep) = initial_twiss_params_holder.getParamsStep(0)
		(alphaYStep, betaYStep, emittYStep) = initial_twiss_params_holder.getParamsStep(1)
		(alphaZStep, betaZStep, emittZStep) = initial_twiss_params_holder.getParamsStep(2)
		(alphaXErr, betaXErr, emittXErr) = initial_twiss_params_holder.getParamsErr(0)
		(alphaYErr, betaYErr, emittYErr) = initial_twiss_params_holder.getParamsErr(1)
		(alphaZErr, betaZErr, emittZErr) = initial_twiss_params_holder.getParamsErr(2)	
		eKin = init_and_fit_params_controller.eKin_text.getValue()
		current = init_and_fit_params_controller.current_text.getValue()	
		fit_err = init_and_fit_params_controller.fit_err_text.getValue()	
		params_da = twiss_and_params_da.createChild("Initial_Parameters")
		params_da.setValue("eKin", eKin)
		params_da.setValue("current", current)
		params_da.setValue("fit_err", fit_err)
		twiss_x_da = twiss_and_params_da.createChild("Initial_X_Twiss")
		twiss_x_da.setValue("alpha", alphaX)
		twiss_x_da.setValue("beta", betaX)
		twiss_x_da.setValue("emitt", emittX)
		twiss_x_da.setValue("alphaStep", alphaXStep)
		twiss_x_da.setValue("betaStep", betaXStep)
		twiss_x_da.setValue("emittStep", emittXStep)
		twiss_x_da.setValue("alphaErr", alphaXErr)
		twiss_x_da.setValue("betaErr", betaXErr)
		twiss_x_da.setValue("emittErr", emittXErr)
		twiss_y_da = twiss_and_params_da.createChild("Initial_Y_Twiss")
		twiss_y_da.setValue("alpha", alphaY)
		twiss_y_da.setValue("beta", betaY)
		twiss_y_da.setValue("emitt", emittY)
		twiss_y_da.setValue("alphaStep", alphaYStep)
		twiss_y_da.setValue("betaStep", betaYStep)
		twiss_y_da.setValue("emittStep", emittYStep)
		twiss_y_da.setValue("alphaErr", alphaYErr)
		twiss_y_da.setValue("betaErr", betaYErr)
		twiss_y_da.setValue("emittErr", emittYErr)		
		twiss_z_da = twiss_and_params_da.createChild("Initial_Z_Twiss")
		twiss_z_da.setValue("alpha", alphaZ)
		twiss_z_da.setValue("beta", betaZ)
		twiss_z_da.setValue("emitt", emittZ)
		twiss_z_da.setValue("alphaStep", alphaZStep)
		twiss_z_da.setValue("betaStep", betaZStep)
		twiss_z_da.setValue("emittStep", emittZStep)
		twiss_z_da.setValue("alphaErr", alphaZErr)
		twiss_z_da.setValue("betaErr", betaZErr)
		twiss_z_da.setValue("emittErr", emittZErr)
		
		
			
		
				
		
		

