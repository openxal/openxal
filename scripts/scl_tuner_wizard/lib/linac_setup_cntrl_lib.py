# The Linac Wizard SetUp classes

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


from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf import AcceleratorSeqCombo
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap
from xal.smf.data import XMLDataManager

from constants_lib import GRAPH_LEGEND_KEY
import constants_lib

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#-----------------------------------------------------------------
#  Accelerator Seqences JTable model for accSeq selection
#-----------------------------------------------------------------		
class WS_Records_Table_Model(AbstractTableModel):
	def __init__(self,name):
		self.accSeqName_arr = ["MEBT","DTL1","DTL2","DTL3","DTL4","DTL5","DTL6","CCL1","CCL2","CCL3","CCL4","SCLMed","SCLHigh","HEBT1"]
		self.columnNames = [name+" Sequence",]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return 1
		
	def getRowCount(self):
		return len(self.accSeqName_arr)
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		return self.accSeqName_arr[row]
			
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		return false

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------

class Make_Sequence_Listener(ActionListener):
	def __init__(self,linac_setup_controller):
		self.linac_setup_controller = linac_setup_controller
		
	def actionPerformed(self,actionEvent):
		seq_names = self.linac_setup_controller.getSelectedSequencesNames()	
		self.linac_setup_controller.setAccSeqNames(seq_names)
		self.linac_setup_controller.linac_wizard_document.ws_lw_controller.cleanOldWSdata()
		
class Read_Cav_Amp_Phase_Dict_Listener(ActionListener):
	def __init__(self,linac_setup_controller):
		self.linac_setup_controller = linac_setup_controller
		
	def actionPerformed(self,actionEvent):
		accl = self.linac_setup_controller.linac_wizard_document.getAccl()
		cav_name_node_dict = self.linac_setup_controller.getCavNameNodeDict(accl)
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read Cavities Amp. and Phases from external file ...")
		fc.setApproveButtonText("Open")
		returnVal = fc.showOpenDialog(self.linac_setup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			file_name = fl_in.getName()
			buff_in = BufferedReader(InputStreamReader(FileInputStream(fl_in)))
			line = buff_in.readLine()
			while( line != null):
				#print "debug line=",line				
				res_arr = line.split()
				if(len(res_arr) == 3):
					cav_name = res_arr[0]
					amp = float(res_arr[1])
					phase = float(res_arr[2])
					if(cav_name_node_dict.has_key(cav_name)):
						#print "debug cav=",cav_name," amp=",amp," phase",phase						
						cav = cav_name_node_dict[cav_name]
						cav.setDfltCavAmp(amp)
						cav.setDfltCavPhase(phase)
				line = buff_in.readLine()
	
class Get_SCL_Cav_Amp_Phase_Listener	(ActionListener):
	def __init__(self,linac_setup_controller):
		self.linac_setup_controller = linac_setup_controller
		
	def actionPerformed(self,actionEvent):
		linac_wizard_document = self.linac_setup_controller.linac_wizard_document
		scl_long_tuneup_controller = linac_wizard_document.scl_long_tuneup_controller
		scl_long_tuneup_controller.putFoundConfigInAccelerator()
	
class SetUp_New_Accelerator_Listener(ActionListener):
	def __init__(self,linac_setup_controller):
		self.linac_setup_controller = linac_setup_controller
		
	def actionPerformed(self,actionEvent):
		fc = JFileChooser(constants_lib.const_path_dict["OPENXAL_XML_ACC_FILES_DIRS_PATH"])
		fc.setDialogTitle("Read Main Accelerator Config File ...")
		fc.setApproveButtonText("Open")
		fl_filter = FileNameExtensionFilter("Accelerator Conf. File",["xal",])
		fc.setFileFilter(fl_filter)		
		returnVal = fc.showOpenDialog(self.linac_setup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			file_name = fl_in.getName()	
			accl_new = XMLDataManager.acceleratorWithPath(file_name)
			cav_name_node_new_dict = self.linac_setup_controller.getCavNameNodeDict(accl_new)	
			accl = self.linac_setup_controller.linac_wizard_document.getAccl()
			cav_name_node_dict = self.linac_setup_controller.getCavNameNodeDict(accl)
			for cav_name in 	cav_name_node_new_dict.keys():
				if(cav_name_node_dict.has_key(cav_name)):
					cav_new = cav_name_node_new_dict[cav_name]
					cav = cav_name_node_dict[cav_name]
					cav.setDfltCavAmp(cav_new.getDfltCavAmp())
					cav.setDfltCavPhase(cav_new.getDfltCavPhase())
							
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class LINAC_SetUp_Controller:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document		
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()		
		#------tables with Seq. names and button
		tables_panel = JPanel(BorderLayout())
		tables_panel.setBorder(etched_border)
		self.first_table = JTable(WS_Records_Table_Model("First "))
		self.last_table = JTable(WS_Records_Table_Model("Last "))
		self.first_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.last_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.first_table.setFillsViewportHeight(true)
		self.last_table.setFillsViewportHeight(true)
		tables01_panel = JPanel(BorderLayout())
		scrl_panel0 = JScrollPane(self.first_table)
		scrl_panel1 = JScrollPane(self.last_table)
		#tables01_panel.add(JScrollPane(self.first_table),BorderLayout.WEST)
		#tables01_panel.add(JScrollPane(self.last_table),BorderLayout.EAST)
		self.first_table.getCellRenderer(0,0).setHorizontalAlignment(JLabel.CENTER)
		self.last_table.getCellRenderer(0,0).setHorizontalAlignment(JLabel.CENTER)
		self.first_table.setPreferredScrollableViewportSize(Dimension(120,300))
		self.last_table.setPreferredScrollableViewportSize(Dimension(120,300))
		tables01_panel.add(scrl_panel0,BorderLayout.WEST)
		tables01_panel.add(scrl_panel1,BorderLayout.EAST)	
		tables_button_panel = JPanel(BorderLayout())
		tables_button_panel.add(tables01_panel,BorderLayout.WEST)
		seq_button_panel = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		seq_set_button = JButton(" Set ComboSequence ")	
		seq_button_panel.add(seq_set_button)
		tables_button_panel.add(seq_button_panel,BorderLayout.SOUTH)
		tables_panel.add(tables_button_panel,BorderLayout.NORTH)
		self.main_panel.add(tables_panel,BorderLayout.WEST)
		#--------central panel-------
		cav_amp_phase_button = JButton(" Read Cavities Amp.&Phases from Ext. File ")	
		cav_info_from_scl_tuneup_button = JButton("Get SCL Cav. Amp.&Phases from SCL Long. TuneUp")	
		new_accelerator_button = JButton(" Setup a New Accelerator File ")
		center_buttons_panel0 = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		center_buttons_panel0.add(cav_amp_phase_button)
		center_buttons_panel1 = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		center_buttons_panel1.add(cav_info_from_scl_tuneup_button)			
		center_buttons_panel2 = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		center_buttons_panel2.add(new_accelerator_button)	
		center_buttons_panel = JPanel(GridLayout(3,1))
		center_buttons_panel.add(center_buttons_panel0)
		center_buttons_panel.add(center_buttons_panel1)
		center_buttons_panel.add(center_buttons_panel2)
		center_panel = JPanel(BorderLayout())		
		center_panel.add(center_buttons_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		#---------add actions listeners
		seq_set_button.addActionListener(Make_Sequence_Listener(self))
		cav_amp_phase_button.addActionListener(Read_Cav_Amp_Phase_Dict_Listener(self))
		cav_info_from_scl_tuneup_button.addActionListener(Get_SCL_Cav_Amp_Phase_Listener(self))
		new_accelerator_button.addActionListener(SetUp_New_Accelerator_Listener(self))	
		
	def getMainPanel(self):
		return self.main_panel
		
	def setSelectedSequences(self,first_seq_name,last_seq_name):
		index0 = self.first_table.getModel().accSeqName_arr.index(first_seq_name)
		index1 = self.last_table.getModel().accSeqName_arr.index(last_seq_name)
		self.first_table.setRowSelectionInterval(index0,index0)
		self.last_table.setRowSelectionInterval(index1,index1)
		
	def getSelectedSequencesNames(self):
		first_table = self.first_table
		index0 = first_table.getSelectedRow()
		last_table = self.last_table
		index1 = last_table.getSelectedRow()
		if(index0 < 0 or index1 < 0): return []
		seq_names = []
		if(index0 == index1):
			seq_names.append(first_table.getModel().accSeqName_arr[index0])
		else:
			if(index1 < index0):
				(index0,index1) = (index1,index0)
			for i in range(index0,index1+1):
				seq_names.append(first_table.getModel().accSeqName_arr[i])
		return seq_names
		
	def setAccSeqNames(self,seq_names):
		accl = self.linac_wizard_document.getAccl()
		if(len(seq_names) == 0): 
			accSeq = null
			self.linac_wizard_document.setAccSeq(accSeq)
			return
		lst = ArrayList()
		for seqName in seq_names:
			lst.add(accl.getSequence(seqName))
		accSeq = AcceleratorSeqCombo("SEQUENCE", lst)	
		self.linac_wizard_document.setAccSeq(accSeq)
		
	def getCavNameNodeDict(self,accl):
		rf_gaps = accl.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		cavs = []
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			if(cav not in cavs):
				cavs.append(cav)
		cav_name_node_dict = {}
		cav_names = []
		for cav in cavs:
			cav_names.append(cav.getId())
			cav_name_node_dict[cav.getId()] = cav		
		return cav_name_node_dict
		
