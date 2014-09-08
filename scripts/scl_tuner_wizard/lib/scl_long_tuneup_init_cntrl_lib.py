# The SCL Longitudinal Tune-Up Init controller
# It initializes BPM and Cavities connections

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
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension

from xal.ca import BatchGetValueRequest, ChannelFactory
from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from constants_lib import GRAPH_LEGEND_KEY
from beam_trigger_lib import BeamTrigger

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None
#------------------------------------------------------------------------
#   Auxiliary Init Longitudinal TuneUp Package classes and Functions
#------------------------------------------------------------------------
def sclBPM_PairDistance(cav_index,delta_s0 = 22.0,delta_s_max = 75.0):
	# minimal and max distance between two BPMs at the beginning of SCL
	n_cavs = 81
	# eKin in MeV
	eKin_min = 185.6
	eKin_max = 1000.0
	# mass in MeV
	mass = 938.
	eKin = eKin_min + cav_index*(eKin_max-eKin_min)/n_cavs
	beta_min = math.sqrt(eKin_min*(eKin_min+2*mass))/(eKin_min+mass)
	gamma_min = (eKin_min+mass)/mass	
	beta = math.sqrt(eKin*(eKin+2*mass))/(eKin+mass)
	gamma = (eKin+mass)/mass
	coeff = gamma*beta/(gamma_min*beta_min)
	delta_s = delta_s0*coeff**3
	if(delta_s > delta_s_max): return delta_s_max
	return delta_s
	
class SCL_Quad_Fields_Dict_Holder:
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		self.quad_field_dict = {}
		self.quads = []
		quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
		for quad in quads:
			self.quads.append(quad)
			self.quad_field_dict[quad] = quad.getDfltField()
		
	def getQuadFields(self):
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		fake_scan = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.scanSim_RadioButton.isSelected()
		scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		self.quad_field_dict = {}
		self.quads = []
		quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
		for quad in quads:
			self.quads.append(quad)
			field = quad.getDfltField()
			if(not fake_scan):
				field = quad.getFieldReadback()
			self.quad_field_dict[quad] = field
			
	def setUpOnlineModel(self):
		for quad in self.quads:
			if(self.quad_field_dict.has_key(quad)):
				field = self.quad_field_dict[quad]
				quad.setDfltField(field)
			
	def writeDataToXML(self,root_da):
		scl_quads_da = root_da.createChild("SCL_QUADS_FIELDS")
		#---- write fields for all quads
		for quad in self.quads:
			if(self.quad_field_dict.has_key(quad)):
				field = self.quad_field_dict[quad]
				quad_da = scl_quads_da.createChild(quad.getId())
				quad_da.setValue("field",field)
			
	def getQuadById(self,quad_name,quads):
		for quad in quads:
			if(quad.getId().find(quad_name) >= 0):
				return quad
		return null
	
	def readDataFromXML(self,root_da):		
		list_da = root_da.childAdaptors("SCL_QUADS_FIELDS")
		if(not list_da.isEmpty()):
			scl_quads_da = list_da.get(0)
			scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
			quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
			self.quad_field_dict = {}
			self.quads = []			
			for quad_da in scl_quads_da.childAdaptors():
				quad_name = quad_da.name()
				quad = self.getQuadById(quad_name,quads)
				if(quad != null):
					self.quads.append(quad)
					field = quad_da.doubleValue("field")
					self.quad_field_dict[quad] = field
	
#------------------------------------------------
#  JTable models
#------------------------------------------------
class Init_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["BPM","Z[m]","Use"]
		self.nf3 = NumberFormat.getInstance()
		self.nf3.setMaximumFractionDigits(3)
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		return len(bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.scl_long_tuneup_controller.bpm_wrappers[row]
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return self.nf3.format(bpm_wrapper.getPosition())
		if(col == 2): return bpm_wrapper.isGood
		return ""
				
	def getColumnClass(self,col):
		if(col == 2):
			return self.boolean_class
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 2 ):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		bpm_wrapper = self.scl_long_tuneup_controller.bpm_wrappers[row]
		if(col == 2 ):
			 bpm_wrapper.isGood = value

class Init_Cavities_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cavity","Z[m]","Use","<html>RF A<SUB>design</SUB>(MV)<html>"]
		self.columnNames += ["<html>RF &phi;<SUB>design</SUB>(deg)<html>",]
		self.columnNames += ["<html>RF A<SUB>live</SUB>(MV)<html>",]
		self.columnNames += ["<html>RF &phi;<SUB>live</SUB>(deg)<html>",]
		self.columnNames += ["<html>BPM<SUB>1</SUB><html>","<html>BPM<SUB>2</SUB><html>"]
		self.nf4 = NumberFormat.getInstance()
		self.nf4.setMaximumFractionDigits(4)
		self.nf3 = NumberFormat.getInstance()
		self.nf3.setMaximumFractionDigits(3)		
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.scl_long_tuneup_controller.cav_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		if(col == 0): return cav_wrapper.alias
		if(col == 1): return self.nf3.format(cav_wrapper.getPosition())
		if(col == 2): return cav_wrapper.isGood
		if(col == 3): return self.nf4.format(cav_wrapper.initDesignAmp)
		if(col == 4): return self.nf4.format(cav_wrapper.initDesignPhase)	
		if(col == 5): return self.nf4.format(cav_wrapper.initLiveAmp)	
		if(col == 6): return self.nf4.format(cav_wrapper.initLivePhase)	
		if(col == 7):
			bpm_wrapper0 = cav_wrapper.bpm_wrapper0
			if(bpm_wrapper0 != null):
				return bpm_wrapper0.alias
		if(col == 8): 
			bpm_wrapper1 = cav_wrapper.bpm_wrapper1
			if(bpm_wrapper1 != null):
				return bpm_wrapper1.alias	
		return ""
				
	def getColumnClass(self,col):
		if(col == 2):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		if(col == 2 ):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		if(col == 2 ):
			 cav_wrapper.isGood = value		


#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------

class Init_BPMs_and_Cavities_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")			
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		fake_scan = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.scanSim_RadioButton.isSelected()		
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		for bpm_wrapper in bpm_wrappers:
			bpm_wrapper.clean()
		# ?????? should be tested with CA
		bad_bpm_wrappers = []	
		if(not fake_scan):
			bpm_batch_reader = self.scl_long_tuneup_controller.bpmBatchReader		
			bpm_batch_reader.setBPMs(bpm_wrappers)
			bpm_batch_reader.batchGetRequest.submitAndWait(2.)
			bad_channels = bpm_batch_reader.batchGetRequest.getFailedChannels()	
			for bad_channel in bad_channels:
				ch_name = bad_channel.channelName()
				res_arr = ch_name.split(":")
				bpm_name = "======"
				if(len(res_arr) >= 2):
					bpm_name = res_arr[0]+":"+res_arr[1]
				for bpm_wrapper in bpm_wrappers:
					if(bpm_wrapper.getBPM().getId().find(bpm_name) >= 0):
						bpm_wrapper.isGood = false
						if(bpm_wrapper not in  bad_bpm_wrappers):
							bad_bpm_wrappers.append(bpm_wrapper)
		# cavities loop
		min_bpm_dist = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.min_bpm_dist_txt.getValue()
		max_bpm_dist = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.max_bpm_dist_txt.getValue()
		pos_advance =  sclBPM_PairDistance(0,min_bpm_dist,max_bpm_dist) # position of the bpm1 advance realtive to bpm0
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers 
		for cav_wrapper_ind in range(len(cav_wrappers)):	
			pos_advance = sclBPM_PairDistance(cav_wrapper_ind,min_bpm_dist,max_bpm_dist)
			cav_wrapper = cav_wrappers[cav_wrapper_ind]		
			#-----------define blanking PV 
			if(cav_wrapper.blank_channel == null and (not fake_scan)):
				ch_name = "SCL_LLRF:"+cav_wrapper.alias.replace("Cav","FCM")+":BlnkBeam"
				cav_wrapper.blank_channel = ChannelFactory.defaultFactory().getChannel(ch_name)
				cav_wrapper.blank_channel.connectAndWait()	
			#-----------------------------------------------------------
			cav_wrapper.initDesignAmp = cav_wrapper.cav.getDfltCavAmp()
			cav_wrapper.initDesignPhase = 	cav_wrapper.cav.getDfltCavPhase()		
			if(cav_wrapper.isGood):
				#update all bpm wrappers for the cavity 
				cav_wrapper.setUpBPM_Wrappers(bpm_wrappers,self.scl_long_tuneup_controller)
				cav = cav_wrapper.cav
				# ?????? should be tested with CA
				if(fake_scan):
					cav_wrapper.initLiveAmp = cav.getDfltCavAmp()
					cav_wrapper.initLivePhase = cav.getDfltCavPhase()
				else:
					cav_wrapper.initLiveAmp = cav.getCavAmpSetPoint()
					cav_wrapper.initLivePhase = cav.getCavPhaseSetPoint()
				#---------------------------------------------------
				cav_wrapper.livePhase = 0.
				replace_bpm_wrapper = false
				bpm_wrapper = cav_wrapper.bpm_wrapper0 
				if(bpm_wrapper == null): replace_bpm_wrapper = true
				if(bpm_wrapper != null and bpm_wrapper.isGood == false): replace_bpm_wrapper = true
				if(bpm_wrapper != null and bpm_wrapper in bad_bpm_wrappers): replace_bpm_wrapper = true
				if(replace_bpm_wrapper):
					for bpm_wrapper in cav_wrapper.bpm_wrappers:
						if(bpm_wrapper.isGood and bpm_wrapper.pos > cav_wrapper.pos):
							cav_wrapper.bpm_wrapper0 = bpm_wrapper 
							break
				replace_bpm_wrapper = false
				bpm_wrapper = cav_wrapper.bpm_wrapper1 				
				if(bpm_wrapper == null): replace_bpm_wrapper = true
				if(bpm_wrapper != null and bpm_wrapper.isGood == false): replace_bpm_wrapper = true
				if(bpm_wrapper != null and bpm_wrapper in bad_bpm_wrappers): replace_bpm_wrapper = true
				if(bpm_wrapper != null and bpm_wrapper.pos <= (cav_wrapper.bpm_wrapper0.pos + pos_advance)): replace_bpm_wrapper = true
				if(replace_bpm_wrapper):							
					for bpm_wrapper in cav_wrapper.bpm_wrappers:
						if(bpm_wrapper.isGood and bpm_wrapper.pos > (cav_wrapper.bpm_wrapper0.pos + pos_advance)):
							cav_wrapper.setBPM_0_1_Wrappers(cav_wrapper.bpm_wrapper0,bpm_wrapper)
							break	
			else:
			#cavity is not good
				cav_wrapper.initLiveAmp = 0.0
				cav_wrapper.initLivePhase = 0.0
				cav_wrapper.setBPM_0_1_Wrappers(null,null)
			cav_wrapper.clean()
		#---- setup All Cav Off -------------
		cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		cav0_wrapper.clean()
		cav_wrapper_1 =  self.scl_long_tuneup_controller.cav_wrappers[0]
		cav0_wrapper.setUpBPM_Wrappers(cav_wrapper_1.bpm_wrappers,self.scl_long_tuneup_controller)
		#---- get all quads fields
		self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.getQuadFields()
		#---- update bpm and cavity tables
		self.scl_long_tuneup_controller.updateAllTables()
 		self.scl_long_tuneup_controller.beamTrigger = BeamTrigger(self.scl_long_tuneup_controller)
 		
class Get_Quad_Fields_Button_Listener	(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")			
		#---- get all quads fields
		self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.getQuadFields()		
		
class Restore_Init_BPMs_and_Cavities_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")			
		alive_cavity_amp_limit = 0.5
		scl_long_tuneup_phase_scan_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_scan_controller
		fake_scan = scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.scanSim_RadioButton.isSelected()				
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers 
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood and cav_wrapper.initLiveAmp > alive_cavity_amp_limit):
				cav = cav_wrapper.cav
				# ?????? should be tested with CA
				if(fake_scan):
					cav_wrapper.initLiveAmp = cav.getDfltCavAmp()
					cav_wrapper.initLivePhase = cav.getDfltCavPhase()
				else:
					cav.setCavAmp(cav_wrapper.initLiveAmp)
					cav.setCavPhase(cav_wrapper.initLivePhase)
		#---- update bpm and cavity tables
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		bpm_table = scl_long_tuneup_init_controller.bpm_table
		cav_table = scl_long_tuneup_init_controller.cav_table		
		cav_table.getModel().fireTableDataChanged()
 		bpm_table.getModel().fireTableDataChanged()
	

class Unblank_All_Cavities_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):	
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.setBlankBeam(false)

class Init_Set_BPM_to_Cav_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller, bpm_ind):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.bpm_ind = bpm_ind
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		messageTextField = self.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")		
		bpm_table = scl_long_tuneup_init_controller.bpm_table
		cav_table = scl_long_tuneup_init_controller.cav_table
		cav_selected_inds = cav_table.getSelectedRows()
		if(len(cav_selected_inds) == 0): 
			messageTextField.setText("There are no selected cavities!")
			return
		bpm_selected_ind = bpm_table.getSelectedRow()
		if(bpm_selected_ind >= 0):
			bpm_wrapper = self.scl_long_tuneup_controller.bpm_wrappers[bpm_selected_ind]
			for cav_ind in cav_selected_inds:
				if(cav_ind >= 0):
					cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind]
					if(bpm_wrapper.getPosition() < cav_wrapper.getPosition()):	
						messageTextField.setText("BPM "+bpm_wrapper.alias+" is before Cavity "+cav_wrapper.alias+"! Fix it!")
						break
					if(self.bpm_ind == 1):	
						if(bpm_wrapper == cav_wrapper.bpm_wrapper1):
							messageTextField.setText("Cavity "+cav_wrapper.alias+" cannot have the same BPM1 and BPM2! Fix it!")
							break						
						cav_wrapper.setBPM_0_1_Wrappers(bpm_wrapper,cav_wrapper.bpm_wrapper1)
					if(self.bpm_ind == 2):
						if(bpm_wrapper == cav_wrapper.bpm_wrapper0):
							messageTextField.setText("Cavity "+cav_wrapper.alias+" cannot have the same BPM1 and BPM2! Fix it!")
							break							
						cav_wrapper.setBPM_0_1_Wrappers(cav_wrapper.bpm_wrapper0,bpm_wrapper)
		cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		cav_wrapper_1 =  self.scl_long_tuneup_controller.cav_wrappers[0]
		cav0_wrapper.setUpBPM_Wrappers(cav_wrapper_1.bpm_wrappers,self.scl_long_tuneup_controller)
		cav_table.getModel().fireTableDataChanged()
		cav_table.setRowSelectionInterval(cav_selected_inds[0],cav_selected_inds[len(cav_selected_inds)-1])


class Clear_BPM0_BPM1_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		messageTextField = self.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")		
		cav_table = scl_long_tuneup_init_controller.cav_table
		cav_selected_inds = cav_table.getSelectedRows()
		if(len(cav_selected_inds) == 0): 
			messageTextField.setText("There are no selected cavities!")
			return
		for cav_ind in cav_selected_inds:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind]
			cav_wrapper.setBPM_0_1_Wrappers(null,null)
		cav_table.getModel().fireTableDataChanged()
		cav_table.setRowSelectionInterval(cav_selected_inds[0],cav_selected_inds[len(cav_selected_inds)-1])	
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_Init_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top init panel-----------------------
		top_init_panel = JPanel(BorderLayout())
		init_panel = JPanel(BorderLayout())
		init_panel0 = JPanel(BorderLayout())
		init_button = JButton(" Init BPMs and Cavities ")
		get_quad_fields_button = JButton(" Get Quad Fields ")
		restore_init_button = JButton(" Restore Cavities' Phases ")
		set_unblanked_button = JButton("Un-blank all Cavities")
		init_button.addActionListener(Init_BPMs_and_Cavities_Button_Listener(self.scl_long_tuneup_controller))
		get_quad_fields_button.addActionListener(Get_Quad_Fields_Button_Listener(self.scl_long_tuneup_controller))
		restore_init_button.addActionListener(Restore_Init_BPMs_and_Cavities_Button_Listener(self.scl_long_tuneup_controller))
		set_unblanked_button.addActionListener(Unblank_All_Cavities_Button_Listener(self.scl_long_tuneup_controller))
		expl_text = "Checks response from BPMs and memorizes initial amp./phases of cavities. \n"
		expl_text = expl_text + " D - Design,  I.L. - Initial Live values. \n"
		expl_text = expl_text + " BPM1 and BPM2 will be used for the cavity phase setup during the raw phase scan.\n"
		init_text = JTextArea(expl_text)
		init_panel01 = JPanel(GridLayout(4,1,10,10))
		init_panel01.add(init_button)
		init_panel01.add(get_quad_fields_button)
		init_panel01.add(restore_init_button)
		init_panel01.add(set_unblanked_button)
		init_panel01.setBorder(etched_border)
		init_panel0.add(init_panel01,BorderLayout.WEST)
		init_panel0.add(init_text,BorderLayout.CENTER)	
		init_panel1 = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		init_set_bpm1_button = JButton(" Set BPM1 to Selected Cavs ")
		init_set_bpm2_button = JButton(" Set BPM2 to Selected Cavs ")
		init_set_bpm3_button = JButton(" Clear BPM1/BPM2 for Selected Cavs ")
		init_set_bpm1_button.addActionListener(Init_Set_BPM_to_Cav_Button_Listener(self.scl_long_tuneup_controller,1))
		init_set_bpm2_button.addActionListener(Init_Set_BPM_to_Cav_Button_Listener(self.scl_long_tuneup_controller,2))
		init_set_bpm3_button.addActionListener(Clear_BPM0_BPM1_Button_Listener(self.scl_long_tuneup_controller))
		min_bpm_dist_label = JLabel("Min. BPM 1-2 Dist.[m]=",JLabel.RIGHT)
		self.min_bpm_dist_txt = DoubleInputTextField(22.0,FortranNumberFormat("G10.5"),6)
		max_bpm_dist_label = JLabel("Max.=",JLabel.RIGHT)
		self.max_bpm_dist_txt = DoubleInputTextField(75.0,FortranNumberFormat("G10.5"),6)		
		init_panel1.add(init_set_bpm1_button)
		init_panel1.add(init_set_bpm2_button)
		init_panel1.add(init_set_bpm3_button)
		init_panel1.add(min_bpm_dist_label)
		init_panel1.add(self.min_bpm_dist_txt)
		init_panel1.add(max_bpm_dist_label)
		init_panel1.add(self.max_bpm_dist_txt)
		init_panel.add(init_panel0,BorderLayout.SOUTH)
		init_panel.add(init_panel1,BorderLayout.NORTH)		
		init_panel.setBorder(etched_border)
		top_init_panel.add(init_panel,BorderLayout.NORTH)
		self.bpm_table = JTable(Init_BPMs_Table_Model(self.scl_long_tuneup_controller))
		self.cav_table = JTable(Init_Cavities_Table_Model(self.scl_long_tuneup_controller))
		self.bpm_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cav_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.bpm_table.setFillsViewportHeight(true)
		self.cav_table.setFillsViewportHeight(true)
		self.bpm_table.setPreferredScrollableViewportSize(Dimension(300,300))
		#self.cav_table.setPreferredScrollableViewportSize(Dimension(120,300))		
		scrl_panel0 = JScrollPane(self.bpm_table)
		scrl_panel1 = JScrollPane(self.cav_table)
		scrl_panel0.setBorder(etched_border)
		scrl_panel1.setBorder(etched_border)
		bpm_cav_tables_panel = JPanel(BorderLayout())
		bpm_cav_tables_panel.add(scrl_panel0,BorderLayout.WEST)
		bpm_cav_tables_panel.add(scrl_panel1,BorderLayout.CENTER)
		top_init_panel.add(bpm_cav_tables_panel,BorderLayout.CENTER)
		#--------------------------------------------------
		self.main_panel.add(top_init_panel,BorderLayout.CENTER)
		#--------------------------------------------------
		self.scl_quad_fields_dict_holder = SCL_Quad_Fields_Dict_Holder(self.scl_long_tuneup_controller)
		
	def getMainPanel(self):
		return self.main_panel
		
	def allPairsSet(self):
		res = true
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
			if(cav_wrapper.isGood and (cav_wrapper.bpm_wrapper0 == null or cav_wrapper.bpm_wrapper1 == null)):
				res = false
		return res
		
	def connectAllBPMs(self):
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		bpm_batch_reader = self.scl_long_tuneup_controller.bpmBatchReader
		#if(bpm_wrappers != bpm_batch_reader.bpm_wrappers):
		bpm_batch_reader.setBPMs(bpm_wrappers)
		if(self.scl_long_tuneup_controller.beamTrigger == null):
			self.scl_long_tuneup_controller.beamTrigger = BeamTrigger(self.scl_long_tuneup_controller)
		if(bpm_batch_reader.beam_trigger == null):
			bpm_batch_reader.setBeamTrigger(self.scl_long_tuneup_controller.beamTrigger)
		self.scl_long_tuneup_controller.beamTrigger.setUseTrigger(false)
		
