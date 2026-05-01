# The SCL Longitudinal Tune-Up - Magnet scaling
# This library consists with classes for global magnet 
# scaling from HEBT1 to RTBT2 including IDump, ExtDump,
# Injection and Extraction Kickers

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
from java.beans import PropertyChangeListener
from javax.swing.filechooser import FileNameExtensionFilter
from java.lang import System

from xal.extension.widgets.swing import DoubleInputTextField 
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf.impl import Electromagnet
from xal.smf.impl import MagnetMainSupply
from xal.smf.impl import MagnetTrimSupply
from xal.smf.impl import TrimmedQuadrupole

from xal.extension.widgets.swing import Wheelswitch

from xal.ca import ChannelFactory

import constants_lib

from magnet_scaling_lib import EnergyScalingLinacRingSNS

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None


#------------------------------------------------------------------------
#           Auxiliary Magnet Scaling classes and functions
#------------------------------------------------------------------------	
class SettingStateController:
	def __init__(self):
		self.shouldStop = false
		
	def getShouldStop(self):
		return self.shouldStop
		
	def setShouldStop(self,val):
		self.shouldStop = val

class Acc_Sequence_Wrapper:
	"""
	This is a container for all information related to the accelerator sequence.
	The information includes references to the magnets, dipole correctors, bends,
	injection and extraction kickers fields.
	"""
	def __init__(self,magnet_scaler_controller,accl,alias,useInScaling, isParticleProton = True):
		self.magnet_scaler_controller = magnet_scaler_controller
		self.accl = accl
		self.alias = alias
		self.acc_seq = self.accl.getSequence(self.alias)
		if(self.acc_seq == None):
			self.acc_seq = self.accl.getComboSequence(self.alias)
		self.useInScaling = useInScaling
		self.initialized = False
		#----- it is not good if one of the PSs in this sequence has new field value
		#----- not in the available limits
		self.isGood = True
		#------------------
		self.main_ps_arr = self.getMainPSs()
		self.ps_wrappers = []
		#-----------------
		self.isParticleProton = isParticleProton
		"""
		for main_ps in self.main_ps_arr:
			ps_wrapper = Main_PS_Wrapper(self,self.magnet_scaler_controller,main_ps)
			self.ps_wrappers.append(ps_wrapper)
		"""

	def initialize(self):		
		"""
		Connects the power supply PVs
		"""
		self.isGood = True
		if(self.initialized):
			for ps_wrapper in self.ps_wrappers:
				try:
					ps_wrapper.update()
					self.magnet_scaler_controller.setProgressBar(ps_wrapper)
				except:
					messageTextField = self.magnet_scaler_controller.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
					messageTextField.setText("Cannot connect MainPS =" + ps_wrapper.main_ps.getId() + "  Check EPICS connection. Stop.")
					self.magnet_scaler_controller.updateTables()
					self.initialized = False
					return False
		else:
			self.ps_wrappers = []
			for main_ps in self.main_ps_arr:
				try:
					ps_wrapper = Main_PS_Wrapper(self,self.magnet_scaler_controller,main_ps)
					self.magnet_scaler_controller.setProgressBar_for_MainPS(main_ps)
				except:
					messageTextField = self.magnet_scaler_controller.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
					messageTextField.setText("Cannot connect MainPS ="+main_ps.getId()+"  Check EPICS connection. Stop.")
					self.magnet_scaler_controller.updateTables()
					self.initialized = False
					return False
				self.ps_wrappers.append(ps_wrapper)
		#-------------------------------------------
		self.initialized = True
		messageTextField = self.magnet_scaler_controller.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText(None)
		self.magnet_scaler_controller.updateTables()
		return True

	def getMainPSs(self):	
		magnets = self.acc_seq.getAllNodesWithQualifier((OrTypeQualifier()).or(Electromagnet.s_strType))
		main_ps_arr = []
		for magnet in magnets:
			#----- at this moment we do not have trim magnets in the sequences that we are considering here
			#if(isinstance(magnet,TrimmedQuadrupole)):
			#	print "debug magnet = ",magnet.getId()," has a trim!!!!!!!!!!!!!!!!!"
			main_ps = magnet.getMainSupply()
			#----- for some reason there is no FIELD_BOOK channel for skew quads in the real Ring
			#----- For now we are not using skew quads in operations, so we exclude them here
			if(main_ps.getId().find("PS_QS") > 0): continue				
			if(not main_ps in main_ps_arr):
				main_ps_arr.append(main_ps)
		return main_ps_arr

	def getIsGood(self):
		"""
		It is not good if PSs has new field value not in the available limits.
		"""
		return self.isGood

	def checkLimitsForNewField(self):
		if(not self.useInScaling):
			self.isGood = True
			return
		self.isGood = True
		for ps_wrapper in self.ps_wrappers:
			ps_wrapper.checkLimitsForNewField()
			if(not ps_wrapper.isGood):
				self.isGood = False

	def calculateNewFields(self,coeff_for_linac,coeff_for_ring):
		coeff = coeff_for_linac
		if(self.isParticleProton): coeff = coeff_for_ring
		for ps_wrapper in self.ps_wrappers:
			res = ps_wrapper.calculateNewFields(coeff)
			if(not res):
				return False
		return True
	
	def setNewFields(self):
		setting_state_cntrl = self.magnet_scaler_controller.setting_state_cntrl
		good_outcome = True
		for ps_wrapper in self.ps_wrappers:
			if(not ps_wrapper.use_in_rescaling): continue
			if(setting_state_cntrl.getShouldStop()):
				return False
			res = ps_wrapper.setNewFields()
			self.magnet_scaler_controller.setProgressBar(ps_wrapper)
			if(not res):
				System.out.println("Error cannot setup PS="+ps_wrapper.alias)
				ps_wrapper.isGood = False
				self.isGood = False
				good_outcome = False
		return good_outcome

	def restoreInitFields(self):
		setting_state_cntrl = self.magnet_scaler_controller.setting_state_cntrl
		good_outcome = True
		for ps_wrapper in self.ps_wrappers:
			if(not ps_wrapper.use_in_rescaling): continue
			if(setting_state_cntrl.getShouldStop()):
				return False
			res = ps_wrapper.restoreInitFields()
			self.magnet_scaler_controller.setProgressBar(ps_wrapper)
			if(not res):
				System.out.println("Error cannot setup PS="+ps_wrapper.alias)
				ps_wrapper.isGood = False
				self.isGood = False
				good_outcome = False
		return good_outcome

class Main_PS_Wrapper:
	"""
	This class represents the Main Power Supply for a magnet.
	"""
	def __init__(self,acc_seq_wrapper,magnet_scaler_controller,main_ps):
		self.acc_seq_wrapper = acc_seq_wrapper
		self.magnet_scaler_controller = magnet_scaler_controller
		self.main_ps = main_ps
		self.alias = main_ps.getId()
		#--- defines if we will rescale at all
		self.use_in_rescaling = True
		#----- it is not good if PSs has new field value not in the available limits
		self.isGood = True
		#----- Sleep time in seconds for multiple steps if new field more than 8% from book field
		self.sleep_time = 0.1
		#----- maximal alowable deviation new field_set from field_book value
		self.max_deviation_from_book = 0.08
		self.max_steps = 100
		#---- ???
		self.field_book_pv = self.main_ps.findChannel(MagnetMainSupply.FIELD_BOOK_HANDLE)
		self.field_rb_pv = self.main_ps.findChannel(MagnetMainSupply.FIELD_RB_HANDLE)	
		self.field_set_pv = self.main_ps.findChannel(MagnetMainSupply.FIELD_SET_HANDLE)
		self.lower_field_limit = self.main_ps.lowerFieldLimit()
		self.upper_field_limit = self.main_ps.upperFieldLimit()
		self.field_rb = self.field_rb_pv.getValDbl()
		self.field_set = 	self.field_set_pv.getValDbl()
		if(self.field_book_pv != None):
			self.field_book = self.field_book_pv.getValDbl()
		else:
			self.field_book = 0.
		#----------------------------------------
		self.new_field_rb = self.field_rb
		self.new_field_set = self.field_set
		
	def update(self):
		#---- ????
		self.field_rb = self.field_rb_pv.getValDbl()
		self.field_set = 	self.field_set_pv.getValDbl()
		if(self.field_book_pv != None):
			self.field_book = self.field_book_pv.getValDbl()
		else:
			self.field_book = 0.
		
	def getIsGood(self):
		"""
		It is not good if PSs has new field value not in the available limits.
		The check is done in checkLimitsForNewField method.
		"""
		return self.isGood

	def checkLimitsForNewField(self):
		if(not self.use_in_rescaling):
			self.isGood = True
			return
		self.isGood = True
		if(self.new_field_set < self.lower_field_limit): 
			self.isGood = False
		if(self.new_field_set > self.upper_field_limit): 
			self.isGood = False

	def calculateNewFields(self,coeff):
		self.new_field_rb = self.field_rb*coeff
		self.new_field_set = self.field_set*coeff
		return True
	
	def setNewFields(self):
		setting_state_cntrl = self.magnet_scaler_controller.setting_state_cntrl
		new_field = self.new_field_set
		if(self.magnet_scaler_controller.top_button_subpanel.use_rb_RadioButton.isSelected()):
			new_field = self.new_field_rb
		if(self.field_book_pv != None):
			step = new_field - self.field_book 
			n_steps = 1
			max_val = min(abs(self.new_field_rb),abs(self.new_field_set))
			if(abs(step) > self.max_deviation_from_book*max_val):
				n_steps = int(abs(step)/(self.max_deviation_from_book*max_val)) + 1
				step = (new_field - self.field_book)/n_steps
			if(n_steps > self.max_steps):
				st  = "debug ========= Magnet Scaling Main_PS_Wrapper method setNewFields =========== \n"
				st += "debug PS=" + self.alias + " need more than "+ str(self.max_steps)
				st += " to set field=" + str(new_field) + "\n"
				st += "debug Stop for this PS. \n"
				System.out.println(st)
				return False
			for ind in range(n_steps):
				if(setting_state_cntrl.getShouldStop()): return False				
				field = self.field_book + step
				#---- ???
				self.field_set_pv.putVal(field)
				self.field_book_pv.putVal(field)
				#------------------------------------
				self.new_field_set = field
				self.field_book = field
				time.sleep(self.sleep_time)
				if(setting_state_cntrl.getShouldStop()): return False
		else:
			#---- ???
			self.field_set_pv.putVal(new_field)
			self.new_field_set = new_field
			if(setting_state_cntrl.getShouldStop()): return False
		#--------------------------------
		return True
		
	def restoreInitFields(self):
		setting_state_cntrl = self.magnet_scaler_controller.setting_state_cntrl
		new_field = self.field_set
		new_field_rb = self.field_rb			
		if(self.field_book_pv != None):
			field_book = self.field_book_pv.getValDbl()
			step = new_field - field_book
			n_steps = 1
			max_val = min(abs(new_field_rb),abs(new_field))
			if(abs(step) > self.max_deviation_from_book*max_val):
				n_steps = int(abs(step)/(self.max_deviation_from_book*max_val)) + 1
				step = (new_field - field_book)/n_steps
			if(n_steps > self.max_steps):
				st  = "debug ========= Magnet Scaling Main_PS_Wrapper method restoreInitFields =========== \n"
				st += "debug PS=" + self.alias + " need more than "+ str(self.max_steps)
				st += " to set field=" + str(new_field) + "\n"
				st += "debug Stop for this PS. \n"
				System.out.println(st)
				return False
			for ind in range(n_steps):
				if(setting_state_cntrl.getShouldStop()): return False				
				field = field_book + step
				#---- ???
				self.field_set_pv.putVal(field)
				self.field_book_pv.putVal(field)
				time.sleep(self.sleep_time)
				if(setting_state_cntrl.getShouldStop()): return False
			self.field_set_pv.putVal(self.field_set)
			self.field_book_pv.putVal(self.field_book)
		else:
			#---- ???
			self.field_set_pv.putVal(new_field)
			if(setting_state_cntrl.getShouldStop()): return False
		#--------------------------------
		time.sleep(self.sleep_time)
		self.field_rb = self.field_rb_pv.getValDbl()
		return True	
		
#------------------------------------------------
#  Runnable Objects implementations
#------------------------------------------------	
class Initialize_Runner(Runnable):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller

	def run(self):
		acc_seq_wrappers = self.magnet_scaler_controller.acc_seq_wrappers
		self.magnet_scaler_controller.progressBar.setValue(0)
		for acc_seq_wrapper in acc_seq_wrappers:
			if(not acc_seq_wrapper.initialize()):
				self.magnet_scaler_controller.progressBar.setValue(0)
				return
		self.magnet_scaler_controller.progressBar.setValue(0)		

class SetNewFields_Runner(Runnable):
	def __init__(self,magnet_scaler_controller, setNewFields = True):
		#---- setNewFields = True we set scaled fields if not we restore initial ones
		self.magnet_scaler_controller = magnet_scaler_controller
		self.setNewFields = setNewFields
	
	def run(self):
		messageTextField = self.magnet_scaler_controller.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
		messageTextField.setText("")
		acc_seq_wrappers = self.magnet_scaler_controller.acc_seq_wrappers
		setting_state_cntrl = self.magnet_scaler_controller.setting_state_cntrl
		setting_state_cntrl.setShouldStop(False)
		self.magnet_scaler_controller.progressBar.setValue(0)
		for acc_seq_wrapper in acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling):
				if(not acc_seq_wrapper.initialized):
					messageTextField.setText("Cannot change fields in AccSequence = "+acc_seq_wrapper.alias+" Please initialize all PSs!")
					self.magnet_scaler_controller.updateTables()
					self.magnet_scaler_controller.progressBar.setValue(0)
					return 	
		for acc_seq_wrapper in acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling and acc_seq_wrapper.initialized):
				if(setting_state_cntrl.getShouldStop()):
					messageTextField.setText("Stop per user requests.")
					return
				#------------------------------
				res = False
				if(self.setNewFields):
					res = acc_seq_wrapper.setNewFields()
				else:				
					res = acc_seq_wrapper.restoreInitFields()
				#------------------------------
				if(setting_state_cntrl.getShouldStop()):
					messageTextField.setText("Stop per user requests.")
					return
				if(not res):
					messageTextField.setText("There is a problem with sequence ="+acc_seq_wrapper.alias+" See Menue - View: Show Console")
					self.magnet_scaler_controller.progressBar.setValue(0)
		self.magnet_scaler_controller.updateTables()
		self.magnet_scaler_controller.progressBar.setValue(0)		
		
#------------------------------------------------
#  JTable models
#------------------------------------------------
class Magnet_Scaler_Seq_Table_Model(AbstractTableModel):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		self.columnNames = ["Sequence","Use"]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		acc_seq_wrappers = self.magnet_scaler_controller.acc_seq_wrappers
		return len(acc_seq_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		acc_seq_wrapper = self.magnet_scaler_controller.acc_seq_wrappers[row]
		if(col == 0):
			if(acc_seq_wrapper.useInScaling and (not acc_seq_wrapper.getIsGood())):
				return ("<html><font color=red>" + acc_seq_wrapper.alias +"</html>")
			return acc_seq_wrapper.alias
		if(col == 1): return acc_seq_wrapper.useInScaling
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 1 ):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		acc_seq_wrapper = self.magnet_scaler_controller.acc_seq_wrappers[row]
		if(col == 1 ):
			acc_seq_wrapper.useInScaling = value
			acc_seq_wrapper.checkLimitsForNewField()

class Magnet_Scaler_Magnet_Table_Model(AbstractTableModel):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		self.columnNames = ["PS Name","Use in Scaling","Field (Rb)"," Field (Set)","New Field (Rb)","New Field (Set)","Low Limit","Upper Limit"," Book Val."]
		self.acc_seq_wrapper = None
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		if(self.acc_seq_wrapper == None):
			return 0
		return len(self.acc_seq_wrapper.ps_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(self.acc_seq_wrapper == None):
			return ""	
		ps_wrapper = self.acc_seq_wrapper.ps_wrappers[row]			
		if(col == 0):
			if(ps_wrapper.use_in_rescaling and (not ps_wrapper.getIsGood())):
				return ("<html><font color=red>" + ps_wrapper.alias +"</html>")
			return ps_wrapper.alias			
		if(col == 1): return ps_wrapper.use_in_rescaling
		if(col == 2): return "%+8.4f"%ps_wrapper.field_rb
		if(col == 3): return "%+8.4f"%ps_wrapper.field_set
		if(col == 4): return ("<html><font color=black style='font-weight: bold'>" + "%+8.4f"%ps_wrapper.new_field_rb + "</html>")
		if(col == 5): return ("<html><font color=black style='font-weight: bold'>" + "%+8.4f"%ps_wrapper.new_field_set + "</html>")
		if(col == 6): return ("<html><font color=blue style='font-weight: bold'>" + "%+8.4f"%ps_wrapper.lower_field_limit + "</html>")
		if(col == 7): return ("<html><font color=blue style='font-weight: bold'>" + "%+8.4f"%ps_wrapper.upper_field_limit + "</html>")
		if(col == 8):
			if(ps_wrapper.field_book_pv != None):
				return ("<html><font color=blue>" + "%+8.4f"%ps_wrapper.field_book + "</html>")
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col ==  1):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(self.acc_seq_wrapper == None): return
		if(col == 1 ):
			ps_wrapper = self.acc_seq_wrapper.ps_wrappers[row]
			ps_wrapper.use_in_rescaling = value
		return

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------

class Init_Magnet_Fields_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		runner = Initialize_Runner(self.magnet_scaler_controller)
		thr = Thread(runner)
		thr.start()		

class Calculate_New_Fields_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		acc_seq_wrappers = self.magnet_scaler_controller.acc_seq_wrappers
		(coeff_for_linac,coeff_for_ring) = self.magnet_scaler_controller.top_button_subpanel.calculateFieldCoefficient()
		self.magnet_scaler_controller.progressBar.setValue(0)
		for acc_seq_wrapper in acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling):
				if(not acc_seq_wrapper.initialized):
					messageTextField = self.magnet_scaler_controller.scl_long_tuneup_controller.linac_wizard_document.getMessageTextField()
					messageTextField.setText("Cannot calculate fields in AccSequence = "+acc_seq_wrapper.alias+" Please initialize all PSs!")
					self.magnet_scaler_controller.updateTables()
					return
		for acc_seq_wrapper in acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling and acc_seq_wrapper.initialized):			
				res = acc_seq_wrapper.calculateNewFields(coeff_for_linac,coeff_for_ring)
				if(not res):
					break
				acc_seq_wrapper.checkLimitsForNewField()
		self.magnet_scaler_controller.updateTables()

class Set_New_Fields_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		runner = SetNewFields_Runner(self.magnet_scaler_controller)
		thr = Thread(runner)
		thr.start()

class Restore_Init_Fields_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		runner = SetNewFields_Runner(self.magnet_scaler_controller,False)
		thr = Thread(runner)
		thr.start()

class Stop_Setting_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		self.magnet_scaler_controller.setting_state_cntrl.setShouldStop(True)

class Sequence_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()
		magnet_table_model = self.magnet_scaler_controller.magnet_table.getModel()
		if(index < 0):
			magnet_table_model.acc_seq_wrapper = None
		else:
			magnet_table_model.acc_seq_wrapper = self.magnet_scaler_controller.acc_seq_wrappers[index]
		self.magnet_scaler_controller.magnet_table.getModel().fireTableDataChanged()


class Use_Selected_PSs_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		magnet_table = self.magnet_scaler_controller.magnet_table
		selected_ind_arr = magnet_table.getSelectedRows()
		if(len(selected_ind_arr) == 0): return 
		magnet_table_model = self.magnet_scaler_controller.magnet_table.getModel()
		for ind in selected_ind_arr:
			ps_wrapper = magnet_table_model.acc_seq_wrapper.ps_wrappers[ind]
			ps_wrapper.use_in_rescaling = True
		magnet_table_model.fireTableDataChanged()
	
class Dont_Use_Selected_PSs_Button_Listener(ActionListener):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		
	def actionPerformed(self,actionEvent):
		magnet_table = self.magnet_scaler_controller.magnet_table
		selected_ind_arr = magnet_table.getSelectedRows()
		if(len(selected_ind_arr) == 0): return 
		magnet_table_model = self.magnet_scaler_controller.magnet_table.getModel()
		for ind in selected_ind_arr:
			ps_wrapper = magnet_table_model.acc_seq_wrapper.ps_wrappers[ind]
			ps_wrapper.use_in_rescaling = False
		magnet_table_model.fireTableDataChanged()		

class Energy_Recalculation_Linac_to_Ring_Listener(ActionListener):
	def __init__(self,child_text,energyScaler):
		self.child_text = child_text
		self.energyScaler = energyScaler
		
	def actionPerformed(self,actionEvent):
		parent_txt = actionEvent.getSource()
		parent_val = parent_txt.getValue()
		child_val = self.energyScaler.eKin_Hm_to_P(parent_val)
		self.child_text.setValueQuietly(child_val)
			
class Energy_Recalculation_Ring_to_Linac_Listener(ActionListener):
	def __init__(self,child_text,energyScaler):
		self.child_text = child_text
		self.energyScaler = energyScaler
		
	def actionPerformed(self,actionEvent):
		parent_txt = actionEvent.getSource()
		parent_val = parent_txt.getValue()
		child_val = self.energyScaler.eKin_P_to_Hm(parent_val)
		self.child_text.setValueQuietly(child_val)		

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class Top_Buttons_SubPanel(JPanel):
	def __init__(self,magnet_scaler_controller):
		self.magnet_scaler_controller = magnet_scaler_controller
		energyScaler = self.magnet_scaler_controller.energyScaler
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		#-----------------------------------------
		scale_panel = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		
		#old_energy_lbl = JLabel("Old Energy [MeV] =",JLabel.RIGHT)
		#self.old_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)
		#new_energy_lbl = JLabel("     New Energy [MeV] =",JLabel.RIGHT)	
		#self.new_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)
		
		energy_fields_panel = JPanel(GridLayout(2,2))

		titled_border = BorderFactory.createTitledBorder(etched_border,"Energy Linac or Ring, Old and New")
		energy_fields_panel.setBorder(titled_border)
		
		energy_fields_panel_1_1 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		linac_old_energy_lbl = JLabel("Linac Old [MeV] =",JLabel.RIGHT)
		self.linac_old_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)
		energy_fields_panel_1_1.add(linac_old_energy_lbl)
		energy_fields_panel_1_1.add(self.linac_old_energy_text)
		
		energy_fields_panel_1_2 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		linac_new_energy_lbl = JLabel("Linac New [MeV] =",JLabel.RIGHT)	
		self.linac_new_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)	
		energy_fields_panel_1_2.add(linac_new_energy_lbl)
		energy_fields_panel_1_2.add(self.linac_new_energy_text)

		energy_fields_panel_2_1 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		ring_old_energy_lbl = JLabel("Ring Old [MeV] =",JLabel.RIGHT)
		self.ring_old_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)
		energy_fields_panel_2_1.add(ring_old_energy_lbl)
		energy_fields_panel_2_1.add(self.ring_old_energy_text)
		
		energy_fields_panel_2_2 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		ring_new_energy_lbl = JLabel("Ring New [MeV] =",JLabel.RIGHT)	
		self.ring_new_energy_text = DoubleInputTextField(1000.0,DecimalFormat("####.###"),8)	
		energy_fields_panel_2_2.add(ring_new_energy_lbl)
		energy_fields_panel_2_2.add(self.ring_new_energy_text)
		
		energy_fields_panel.add(energy_fields_panel_1_1)
		energy_fields_panel.add(energy_fields_panel_1_2)
		energy_fields_panel.add(energy_fields_panel_2_1)
		energy_fields_panel.add(energy_fields_panel_2_2)
		
		self.linac_old_energy_text.addActionListener(Energy_Recalculation_Linac_to_Ring_Listener(self.ring_old_energy_text,energyScaler))
		self.linac_new_energy_text.addActionListener(Energy_Recalculation_Linac_to_Ring_Listener(self.ring_new_energy_text,energyScaler))
		
		self.ring_old_energy_text.addActionListener(Energy_Recalculation_Ring_to_Linac_Listener(self.linac_old_energy_text,energyScaler))
		self.ring_new_energy_text.addActionListener(Energy_Recalculation_Ring_to_Linac_Listener(self.linac_new_energy_text,energyScaler))		
		
		#field_coeff_lbl = JLabel(" Scale Coefficient = ",JLabel.RIGHT)
		#self.field_coeff_text = DoubleInputTextField(1.0,DecimalFormat("#.#####"),8)
		
		scale_coeff_panel = JPanel(GridLayout(2,1))
		
		titled_border = BorderFactory.createTitledBorder(etched_border,"Magnet Fields Scale Coefficients")
		scale_coeff_panel.setBorder(titled_border)		
		
		scale_coeff_panel_1 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		linac_field_coeff_lbl = JLabel("Linac Scale Coeff.= ",JLabel.RIGHT)
		self.linac_field_coeff_text = DoubleInputTextField(1.0,DecimalFormat("#.########"),10)
		scale_coeff_panel_1.add(linac_field_coeff_lbl)
		scale_coeff_panel_1.add(self.linac_field_coeff_text)
		
		scale_coeff_panel_2 = JPanel(FlowLayout(FlowLayout.RIGHT,5,5))
		ring_field_coeff_lbl = JLabel("Ring Scale Coeff.= ",JLabel.RIGHT)
		self.ring_field_coeff_text = DoubleInputTextField(1.0,DecimalFormat("#.########"),10)
		scale_coeff_panel_2.add(ring_field_coeff_lbl)
		scale_coeff_panel_2.add(self.ring_field_coeff_text)
		
		scale_coeff_panel.add(scale_coeff_panel_1)
		scale_coeff_panel.add(scale_coeff_panel_2)
		
		scale_calc_button = JButton("Calculate Scaled Fields")
		scale_calc_button.addActionListener(Calculate_New_Fields_Button_Listener(self.magnet_scaler_controller))
		
		scale_panel.add(energy_fields_panel)
		scale_panel.add(scale_calc_button)
		scale_panel.add(scale_coeff_panel)
		
		#-----------------------------------------
		self.use_rb_RadioButton = JRadioButton("Use ReadBack Field in Scaling")
		set_use_in_scale_button = JButton("Use Selected PSs in Scaling")
		set_use_in_scale_button.addActionListener(Use_Selected_PSs_Button_Listener(self.magnet_scaler_controller))
		set_dont_use_in_scale_button = JButton("Do not Use Selected PSs in Scaling")
		set_dont_use_in_scale_button.addActionListener(Dont_Use_Selected_PSs_Button_Listener(self.magnet_scaler_controller))
		cntrl_panel = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		cntrl_panel.add(self.use_rb_RadioButton)
		cntrl_panel.add(set_use_in_scale_button)
		cntrl_panel.add(set_dont_use_in_scale_button)
		#------------------------------------------
		self.add(scale_panel,BorderLayout.CENTER)
		self.add(cntrl_panel,BorderLayout.SOUTH)
		
	def calculateFieldCoefficient(self):
		energyScaler = self.magnet_scaler_controller.energyScaler
		eKin_old = self.ring_old_energy_text.getValue()
		momentum_old = energyScaler.momentum_from_eKin_P(eKin_old*1.0e+6)
		eKin_new = self.ring_new_energy_text.getValue()
		momentum_new = energyScaler.momentum_from_eKin_P(eKin_new*1.0e+6)
		coeff_for_ring = momentum_new/momentum_old
		self.ring_field_coeff_text.setValue(coeff_for_ring)
		
		eKin_old = self.linac_old_energy_text.getValue()
		momentum_old = energyScaler.momentum_from_eKin_Hm(eKin_old*1.0e+6)
		eKin_new = self.linac_new_energy_text.getValue()
		momentum_new = energyScaler.momentum_from_eKin_Hm(eKin_new*1.0e+6)
		coeff_for_linac = momentum_new/momentum_old
		self.linac_field_coeff_text.setValue(coeff_for_linac)

		return (coeff_for_linac,coeff_for_ring)
		
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class Magnet_Scaler_Controller:
	def __init__(self,scl_long_tuneup_controller):
		self.energyScaler = EnergyScalingLinacRingSNS()
		#---------------------------------------------------
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller
		self.linac_wizard_document = self.scl_long_tuneup_controller.linac_wizard_document
		#----scl_accSeq is a specific for this controller
		accl = self.linac_wizard_document.accl
		self.acc_seq_wrappers = []
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"HEBT1",false, isParticleProton = False))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"HEBT2",true, isParticleProton = False))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"LDmp",true, isParticleProton = False))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"IDmp-",true, isParticleProton = True))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"IDmp+",true, isParticleProton = True))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"Ring",true, isParticleProton = True))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"RTBT",true, isParticleProton = True))
		self.acc_seq_wrappers.append(Acc_Sequence_Wrapper(self,accl,"EDmp",true, isParticleProton = True))
		#----------------------------------------------
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()	
		#------top panel-----------------------
		top_panel = JPanel(BorderLayout())
		#------center panel --------
		center_panel = JPanel(BorderLayout())
		#-------- bottom panel
		bottom_panel = JPanel(BorderLayout())
		#--------------------------------------------------
		#-------- setup the elements on the top panel
		top_button_panel = JPanel(BorderLayout())
		init_button = JButton("Initialize Fields from EPICS")
		init_button.addActionListener(Init_Magnet_Fields_Button_Listener(self))
		restore_init_button = JButton("Restore Init. for Selected PSs")
		restore_init_button.addActionListener(Restore_Init_Fields_Button_Listener(self))
		set_button = JButton("Set New Fields to EPICS")
		set_button.addActionListener(Set_New_Fields_Button_Listener(self))
		stop_setting_button = JButton("Stop Setting Process")
		stop_setting_button.addActionListener(Stop_Setting_Button_Listener(self))
		init_restore_panel = JPanel(GridLayout(2,1,2,2))
		init_restore_panel.add(init_button)
		init_restore_panel.add(restore_init_button)
		start_stop_panel = JPanel(GridLayout(2,1,2,2))
		start_stop_panel.add(set_button)
		start_stop_panel.add(stop_setting_button)
		top_button_panel_tmp0 = JPanel(BorderLayout())
		top_button_panel_tmp0.add(init_restore_panel,BorderLayout.NORTH)
		top_button_panel_tmp0.add(start_stop_panel,BorderLayout.SOUTH)
		top_button_panel_tmp1 = JPanel(BorderLayout())
		top_button_panel_tmp1.add(top_button_panel_tmp0,BorderLayout.WEST)
		top_button_panel.add(top_button_panel_tmp1,BorderLayout.WEST)
		#-----
		top_button_panel_tmp2 = JPanel(FlowLayout(FlowLayout.LEFT,5,5))
		self.top_button_subpanel = Top_Buttons_SubPanel(self)
		top_button_panel_tmp2.add(self.top_button_subpanel)
		top_button_panel.add(top_button_panel_tmp2,BorderLayout.CENTER)
		#-----
		top_panel.add(top_button_panel,BorderLayout.WEST)
		#-------- setup the tables on the central panel
		self.seq_table = JTable(Magnet_Scaler_Seq_Table_Model(self))
		self.magnet_table = JTable(Magnet_Scaler_Magnet_Table_Model(self))
		self.seq_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.seq_table.getSelectionModel().addListSelectionListener(Sequence_Table_Selection_Listener(self))
		self.magnet_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.seq_table.setFillsViewportHeight(true)
		self.magnet_table.setFillsViewportHeight(true)
		self.seq_table.setPreferredScrollableViewportSize(Dimension(300,300))	
		scrl_panel0 = JScrollPane(self.seq_table)
		scrl_panel1 = JScrollPane(self.magnet_table)
		scrl_panel0.setBorder(etched_border)
		scrl_panel1.setBorder(etched_border)
		seq_magnet_table_panel_tmp1 = JPanel(BorderLayout())
		seq_magnet_table_panel_tmp1.add(scrl_panel1,BorderLayout.CENTER)
		seq_magnet_tables_panel = JPanel(BorderLayout())
		seq_magnet_tables_panel.add(scrl_panel0,BorderLayout.WEST)
		seq_magnet_tables_panel.add(seq_magnet_table_panel_tmp1,BorderLayout.CENTER)
		progress_label =JLabel("Init/Set Progress=",JLabel.RIGHT)
		self.progressBar = JProgressBar(0,100)
		self.progressBar.setStringPainted(true)
		progress_bar_panel = JPanel(BorderLayout())
		progress_bar_panel.setBorder(etched_border)		
		progress_bar_panel.add(progress_label,BorderLayout.WEST)
		progress_bar_panel.add(self.progressBar,BorderLayout.CENTER)
		seq_magnet_table_panel_tmp1.add(progress_bar_panel,BorderLayout.NORTH)
		center_panel.add(seq_magnet_tables_panel,BorderLayout.CENTER)
		#--------------------------------------------------
		#---- set up panels
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		#---- allows to stop settings
		self.setting_state_cntrl = SettingStateController()

		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		self.seq_table.getModel().fireTableDataChanged()
		self.magnet_table.getModel().fireTableDataChanged()
		
	def setProgressBar(self,ps_wrapper_in):
		total_number = 0
		found = 0
		count = 0
		for acc_seq_wrapper in self.acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling):
				for ps_wrapper in acc_seq_wrapper.ps_wrappers:
					total_number += 1
					if(ps_wrapper == ps_wrapper_in): found = +1
					if(found < 1):
						count += 1
		progress = (100.*count)/total_number
		self.progressBar.setValue(int(progress))
		
	def setProgressBar_for_MainPS(self,main_ps_in):
		total_number = 0
		found = 0
		count = 0
		for acc_seq_wrapper in self.acc_seq_wrappers:
			if(acc_seq_wrapper.useInScaling):
				for main_ps in acc_seq_wrapper.main_ps_arr:
					total_number += 1
					if(main_ps == main_ps_in): found = +1
					if(found < 1):
						count += 1
		progress = (100.*count)/total_number
		self.progressBar.setValue(int(progress))		
	
	def writeDataToXML(self,root_da):
		if(self.isWorthToSave()):
			magnet_scaler_da = root_da.createChild("Long_Tune_Magnet_Scaler_Data")
		pass
				
	def readDataFromXML(self,root_da):
		magnet_scaler_da = root_da.childAdaptor("Long_Tune_Magnet_Scaler_Data")
		if(energy_meter_da == null): return
		pass
						
	def isWorthToSave(self):
		if(-1 > 0):
			return true
		else:
			return false
			
	def clean(self):
		pass

	

