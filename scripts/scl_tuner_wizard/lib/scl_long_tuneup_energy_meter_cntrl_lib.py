# The SCL Longitudinal Tune-Up - Energy Meter
# It will calculate the energy of the beam in the HEBT1
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
from javax.swing.filechooser import FileNameExtensionFilter

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.plot import GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.ca import ChannelFactory

from xal.extension.widgets.swing import Wheelswitch

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear, calculateAvgErr

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None
#------------------------------------------------------------------------
#           Auxiliary Energy Meter classes and functions
#------------------------------------------------------------------------	
class StatisticStateController:
	def __init__(self):
		self.isRunning = false
		self.shouldStop = false
		
	def getIsRunning(self):
		return self.isRunning
		
	def getShouldStop(self):
		return self.shouldStop

	def setIsRunning(self,val):
		self.isRunning = val
		
	def setShouldStop(self,val):
		self.shouldStop = val		
		

class BPMs_Wrappers_Holder:
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.clean()
		
	def clean(self):
		self.em_bpm_wrpprs = []
		#---- self.use_bpms is array with boolean values yes/no to use in Eneregy Meter
		self.use_bpms = []
		#---- self.bpm_phase_offsets array of BPM offsets
		self.bpm_phase_offsets = []	
		#---- self.bpm_amp_phase_data_dict[bpm_wrapper] = [amp_arr,phase_arr]
		#---- it is a buffer to keep measured BPM amps. and phases 
		self.bpm_amp_phase_data_dict = {}
		#---- self.bpm_phase_diff_arr[bpm_ind] = [phase_diffs] 
		#---- it is a buffer to keep arrays of measured BPM phase differences
		self.bpm_phase_diff_arr = []
		
	def setBPM_Wrappers(self):
		# we will include only BPMs after the last cavity and before HEBT2
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		cav_wrapper = cav_wrappers[len(cav_wrappers)-1]
		max_pos = 280.0
		self.clean()
		for bpm_wrapper in bpm_wrappers:
			if(bpm_wrapper.isGood and bpm_wrapper.pos > cav_wrapper.pos and bpm_wrapper.pos < max_pos):
				self.em_bpm_wrpprs.append(bpm_wrapper)
				phase_offset = 0.
				if(bpm_wrapper.final_phase_offset.isReady):
					phase_offset = bpm_wrapper.final_phase_offset.phaseOffset_avg
					self.use_bpms.append(true)
				else:
					self.use_bpms.append(false)
				self.bpm_phase_offsets.append(phase_offset)
				self.bpm_amp_phase_data_dict[bpm_wrapper] = ([],[])
				self.bpm_phase_diff_arr.append([])
			
	def resetDataArr(self):
		for bpm_wrapper in self.bpm_amp_phase_data_dict.keys():
			self.bpm_amp_phase_data_dict[bpm_wrapper] = ([],[])
		for ind in range(len(self.bpm_phase_diff_arr)):
			self.bpm_phase_diff_arr[ind] = []
				
	def setOffsetsToZero(self):
		for ind in range(len(self.bpm_phase_offsets)):
			self.bpm_phase_offsets[ind] = 0.
			
	def getNumbOfBPMs(self):
		return len(self.em_bpm_wrpprs)
					
	def calculateEnergy(self,eKin_in):
		mass = self.scl_long_tuneup_controller.mass/1.0e+6			
		c_light = self.scl_long_tuneup_controller.c_light	
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq
		coeff_init = 360.0*bpm_freq/c_light
		beta = math.sqrt(eKin_in*(eKin_in+2*mass))/(eKin_in+mass)		
		coeff = coeff_init/beta		
		#------ calculate avg bpm phases		
		res_arr = []
		for bpm_ind in range(len(self.em_bpm_wrpprs)):
			bpm_wrapper = self.em_bpm_wrpprs[bpm_ind]
			if(not self.use_bpms[bpm_ind]): continue
			if(not self.bpm_amp_phase_data_dict.has_key(bpm_wrapper)): continue
			(amp_arr,phase_arr) = self.bpm_amp_phase_data_dict[bpm_wrapper]
			(phase_avg,phase_err) = calculateAvgErr(phase_arr)
			#print "debug bpm=",bpm_wrapper.alias," (phase_avg,phase_err) =",(phase_avg,phase_err) 
			res_arr.append([bpm_ind,bpm_wrapper,phase_avg,phase_err])
		n_res = len(res_arr)
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		table_and_plots_panel = scl_long_tuneup_energy_meter_controller.table_and_plots_panel
		init_start_stop_panel = scl_long_tuneup_energy_meter_controller.init_start_stop_panel
		fixInitEenergy_RadioButton = init_start_stop_panel.fixInitEenergy_RadioButton
		buffer_size = int(init_start_stop_panel.buffer_size_text.getValue())
		if(n_res < 2): return eKin_in
		[bpm_ind0,bpm_wrapper0,phase_avg0,phase_err0] = res_arr[0]
		phase_avg0 = phase_avg0 - self.bpm_phase_offsets[0]
		res_arr[0][2] = phase_avg0
		base_pos = bpm_wrapper0.pos
		#"""
		for ind in range(n_res-1):
			[bpm_ind0,bpm_wrapper0,phase_avg0,phase_err0] = res_arr[ind]
			[bpm_ind1,bpm_wrapper1,phase_avg1,phase_err1] = res_arr[ind+1]
			bpm1_phase = phase_avg1 - self.bpm_phase_offsets[bpm_ind1]
			bpm1_phase_guess = phase_avg0 + coeff*(bpm_wrapper1.pos - bpm_wrapper0.pos)
			bpm1_phase = makePhaseNear(bpm1_phase,bpm1_phase_guess)
			res_arr[ind+1][2] = bpm1_phase
		"""
		for ind in range(1,n_res):
			[bpm_ind,bpm_wrapper,phase_avg,phase_err] = res_arr[ind]
			bpm_phase = phase_avg - self.bpm_phase_offsets[bpm_ind]
			delta_pos = bpm_wrapper.pos - base_pos
			bpm_phase_guess = phase_avg0 + coeff*delta_pos
			bpm_phase = makePhaseNear(bpm_phase,bpm_phase_guess)
			res_arr[ind][2] = bpm_phase	
		"""
		#----- make phase plot
		gd = BasicGraphData()
		for ind in range(n_res):
			[bpm_ind,bpm_wrapper,phase_avg,phase_err] = res_arr[ind]
			gd.addPoint(bpm_wrapper.pos - base_pos,phase_avg,phase_err)
		res_poly_arr = GraphDataOperations.polynomialFit(gd,-1.e+36,+1.e+36,1)
		if(res_poly_arr == null): return eKin_in
		slope = res_poly_arr[0][1]
		init_phase = res_poly_arr[0][0]
		if(fixInitEenergy_RadioButton.isSelected()):
			slope = coeff
			init_phase = phase_avg0 
		beta = coeff_init/slope
		gamma = 1./math.sqrt(1.0-beta*beta)
		eKin = mass*(gamma-1.0)
		slope_err = res_poly_arr[1][1]
		delta_eKin = mass*gamma**3*beta**3*slope_err/coeff_init		
		#make phase error plot
		x_arr = []
		y_arr = []
		err_arr = []
		for ind in range(n_res):
			[bpm_ind,bpm_wrapper,phase_avg,phase_err] = res_arr[ind]
			x_arr.append(bpm_wrapper.pos)
			phase_diff = makePhaseNear(phase_avg - (init_phase + slope*(bpm_wrapper.pos - base_pos)),0.)
			self.bpm_phase_diff_arr[bpm_ind].append(phase_diff)
			if(len(self.bpm_phase_diff_arr[bpm_ind]) > buffer_size):
				self.bpm_phase_diff_arr[bpm_ind] = self.bpm_phase_diff_arr[bpm_ind][1:]
			(phase_diff,phase_err) = calculateAvgErr(self.bpm_phase_diff_arr[bpm_ind])
			y_arr.append(phase_diff)
			err_arr.append(phase_err) 	
		table_and_plots_panel.bpm_phase_err_gd.removeAllPoints()			
		table_and_plots_panel.bpm_phase_err_gd.addPoint(x_arr,y_arr,err_arr)
		return (eKin,delta_eKin)
		
	def addPhaseDiffToOffsets(self):
		for bpm_ind in range(len(self.em_bpm_wrpprs)):
			bpm_wrapper = self.em_bpm_wrpprs[bpm_ind]
			if(self.use_bpms[bpm_ind]):
				self.bpm_phase_offsets[bpm_ind] += calculateAvgErr(self.bpm_phase_diff_arr[bpm_ind])[0]
				self.bpm_phase_offsets[bpm_ind] = makePhaseNear(self.bpm_phase_offsets[bpm_ind],0.)
			
class Statistic_Runner(Runnable):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		#------------------------------------------------------------
		self.bpm_wrappers_good_arr = []
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		for ind in range(len(bpm_wrappers_holder.em_bpm_wrpprs)):
			bpm_wrapper = bpm_wrappers_holder.em_bpm_wrpprs[ind]
			if(bpm_wrappers_holder.use_bpms[ind]):
				self.bpm_wrappers_good_arr.append([ind,bpm_wrapper])
	
	def run(self):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		if(messageTextField != null):
			messageTextField.setText("")	
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		statistic_state_controller = scl_long_tuneup_energy_meter_controller.statistic_state_controller
		init_start_stop_panel = scl_long_tuneup_energy_meter_controller.init_start_stop_panel
		init_start_stop_panel.energy_text.setValue(0.)
		init_start_stop_panel.energy_err_text.setValue(0.)
		buffer_size = int(init_start_stop_panel.buffer_size_text.getValue())		
		if(init_start_stop_panel.energy_guess_text.getValue() == 0.):
			if(messageTextField != null):
				messageTextField.setText("Set non-zero initial energy!")			
				return				
		bpm_table = scl_long_tuneup_energy_meter_controller.table_and_plots_panel.bpm_table
		counter_text = init_start_stop_panel.counter_text
		counter_text.setValue(0.)
		scl_long_tuneup_energy_meter_controller.table_and_plots_panel.bpm_phase_err_gd.removeAllPoints()
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		scl_long_tuneup_init_controller.connectAllBPMs()		
		bpmBatchReader = self.scl_long_tuneup_controller.bpmBatchReader
		beamTrigger = self.scl_long_tuneup_controller.beamTrigger
		beamTrigger.scan_state_controller = statistic_state_controller
		statistic_state_controller.setShouldStop(false)
		bpmBatchReader.setBeamTrigger(beamTrigger)
		bpm_wrappers_holder.resetDataArr()
		count = 0
		while(1 < 2):
			counter_text.setValue(float(count))
			bpm_amp_phase_data_dict = {}
			res = bpmBatchReader.makeMeasurement()
			count += 1
			#print "debug count=",count
			bpmBatchReader.collectStatistics(bpm_wrappers_holder.bpm_amp_phase_data_dict)
			bpm_amp_phase_data_dict = bpm_wrappers_holder.bpm_amp_phase_data_dict
			for bpm_wrapper in bpm_amp_phase_data_dict.keys():
				(amp_arr,phase_arr) = bpm_amp_phase_data_dict[bpm_wrapper]
				n_len = len(amp_arr)
				if(n_len > buffer_size):
					(amp_arr,phase_arr) = (amp_arr[1:n_len],phase_arr[1:n_len])
					bpm_amp_phase_data_dict[bpm_wrapper] = (amp_arr,phase_arr)
			eKin_in = init_start_stop_panel.energy_guess_text.getValue()
			(eKin,delta_eKin) = bpm_wrappers_holder.calculateEnergy(eKin_in)
			init_start_stop_panel.energy_err_text.setValue(delta_eKin)
			init_start_stop_panel.energy_text.setValue(eKin)
			if(not res):
				if(messageTextField != null):
					messageTextField.setText("cannot measure the BPM phases! Stop.")
					if(statistic_state_controller.getShouldStop()):
						messageTextField.setText("The Statistics stopped upon user's request!")
					break
			
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class InitStartStopStatistics_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"Init, Start, Stop Statistic Measurements")
		self.setBorder(titled_border)	
		#----- buttons panel
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		init_meter_button = JButton("Initialize Meter")
		init_meter_button.addActionListener(Init_Meter_Button_Listener(self.scl_long_tuneup_controller))		
		start_statistics_button = JButton("Start Statistic")
		start_statistics_button.addActionListener(Start_Statistics_Button_Listener(self.scl_long_tuneup_controller))
		stop_statistics_button = JButton("Stop")
		stop_statistics_button.addActionListener(Stop_Statistics_Button_Listener(self.scl_long_tuneup_controller))		
		counter_lbl = JLabel("  Counter=",JLabel.RIGHT)
		self.counter_text = DoubleInputTextField(0.,FortranNumberFormat("F4.0"),4)	
		buffer_size_lbl = JLabel("  Buffer Size=",JLabel.RIGHT)
		self.buffer_size_text = DoubleInputTextField(10.,FortranNumberFormat("F4.0"),4)	
		self.fixInitEenergy_RadioButton = JRadioButton("Keep Guess Energy (to get offsets)")	
		self.fixInitEenergy_RadioButton.setSelected(false)
		buttons_panel.add(init_meter_button)
		buttons_panel.add(start_statistics_button)
		buttons_panel.add(stop_statistics_button)
		buttons_panel.add(counter_lbl)
		buttons_panel.add(self.counter_text)
		buttons_panel.add(self.fixInitEenergy_RadioButton)
		buttons_panel.add(buffer_size_lbl)
		buttons_panel.add(self.buffer_size_text)
		#----- energy panel
		energy_panel =  JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		energy_guess_lbl = JLabel("<html>Initial Guess E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.energy_guess_text = DoubleInputTextField(0.,FortranNumberFormat("G10.6"),12)		
		energy_lbl = JLabel("<html>Found E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.energy_text = DoubleInputTextField(0.,FortranNumberFormat("G10.6"),12)
		energy_err_lbl = JLabel("+-",JLabel.RIGHT)
		self.energy_err_text = DoubleInputTextField(0.,FortranNumberFormat("G10.6"),12)
		energy_panel.add(energy_guess_lbl)
		energy_panel.add(self.energy_guess_text)		
		energy_panel.add(energy_lbl)
		energy_panel.add(self.energy_text)
		energy_panel.add(energy_err_lbl)
		energy_panel.add(self.energy_err_text)
		#----- main panel		
		self.add(buttons_panel,BorderLayout.NORTH)
		self.add(energy_panel,BorderLayout.CENTER)
		
class Table_and_Plots_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPM Table and Plots")
		self.setBorder(titled_border)	
		#----------------------------------------
		self.bpm_table = JTable(Energy_Meter_BPMs_Table_Model(self.scl_long_tuneup_controller))
		self.bpm_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.bpm_table.setFillsViewportHeight(true)
		self.bpm_table.setPreferredScrollableViewportSize(Dimension(400,300))	
		scrl_panel = JScrollPane(self.bpm_table)
		scrl_panel.setBorder(etched_border)
		bpm_table_panel = JPanel(BorderLayout())
		bpm_table_panel.add(scrl_panel,BorderLayout.WEST)
		#-----------------------------------------
		self.gp_bpm_phase_err = FunctionGraphsJPanel()
		self.gp_bpm_phase_err.setLegendButtonVisible(true)
		self.gp_bpm_phase_err.setChooseModeButtonVisible(true)	
		self.gp_bpm_phase_err.setName("BPM Phase Errors")
		self.gp_bpm_phase_err.setAxisNames("position, [m]","BPM Phase Error, [deg]")	
		self.gp_bpm_phase_err.setBorder(etched_border)
		bpm_graph_panel = JPanel(BorderLayout())
		bpm_graph_panel.add(self.gp_bpm_phase_err)
		#------graph data 
		self.bpm_phase_err_gd = BasicGraphData()
		self.bpm_phase_err_gd.setGraphPointSize(8)
		self.bpm_phase_err_gd.setDrawLinesOn(false)
		self.bpm_phase_err_gd.setGraphColor(Color.BLUE)	
		self.gp_bpm_phase_err.addGraphData(self.bpm_phase_err_gd)
		#-----------------------------------------
		self.add(bpm_table_panel,BorderLayout.WEST)	
		self.add(bpm_graph_panel,BorderLayout.CENTER)	
		
class Manipulate_Offsets_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPM Phase Offsets Manipulation")
		self.setBorder(titled_border)	
		#----- buttons panel
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		zero_offsets_button = JButton("Zero all Offsets")
		zero_offsets_button.addActionListener(Zero_Offsets_Button_Listener(self.scl_long_tuneup_controller))		
		correct_offsets_button = JButton("Correct Offsets with Phase Diff")
		correct_offsets_button.addActionListener(Correct_Offsets_Diff_Button_Listener(self.scl_long_tuneup_controller))
		write_to_ascii_button = JButton("Write Offsets to ASCII")
		write_to_ascii_button.addActionListener(Write_To_ASCII_Button_Listener(self.scl_long_tuneup_controller))
		read_from_ascii_button = JButton("Read Offsets from ASCII")
		read_from_ascii_button.addActionListener(Read_From_ASCII_Button_Listener(self.scl_long_tuneup_controller))
		buttons_panel.add(zero_offsets_button)
		buttons_panel.add(correct_offsets_button)
		buttons_panel.add(write_to_ascii_button)
		buttons_panel.add(read_from_ascii_button)
		#----- main panel		
		self.add(buttons_panel,BorderLayout.NORTH)
				
#------------------------------------------------
#  JTable models
#------------------------------------------------
class Energy_Meter_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["BPM","Use in Meter","pos[m]"]
		self.columnNames += ["<html>&Delta;&phi;<SUB>BPM</SUB>(deg)<html>",]	
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
		return len(em_bpm_wrpprs)	
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
		use_bpms = bpm_wrappers_holder.use_bpms
		bpm_phase_offsets = bpm_wrappers_holder.bpm_phase_offsets
		if(col == 0): return em_bpm_wrpprs[row].alias
		if(col == 1): return use_bpms[row]
		if(col == 2): return "%8.3f"%em_bpm_wrpprs[row].pos
		if(not use_bpms[row]): return "" 
		if(col == 3): return "%6.3f"%bpm_phase_offsets[row]
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
		if(	col == 1): return true
		return false
			
	def setValueAt(self, value, row, col):
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
		use_bpms = bpm_wrappers_holder.use_bpms
		if(not em_bpm_wrpprs[row].isGood):
			use_bpms[row] = false
			self.fireTableCellUpdated(row,1)
			self.fireTableCellUpdated(row,3)			
		if(col == 1 and em_bpm_wrpprs[row].isGood):
			use_bpms[row] = value
			self.fireTableCellUpdated(row,1)
			self.fireTableCellUpdated(row,3)

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Init_Meter_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller		
		statistic_state_controller = scl_long_tuneup_energy_meter_controller.statistic_state_controller
		if(statistic_state_controller.getIsRunning()): return		
		#------ set up batch BPM Reader		
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		bpm_batch_reader = self.scl_long_tuneup_controller.bpmBatchReader		
		bpm_batch_reader.setBPMs(bpm_wrappers)
		#------------ energy meter setup
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		bpm_wrappers_holder.clean()
		bpm_wrappers_holder.setBPM_Wrappers()
		init_start_stop_panel = scl_long_tuneup_energy_meter_controller.init_start_stop_panel
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		eKin_end = cav_wrappers[len(cav_wrappers)-1].eKin_out
		init_start_stop_panel.energy_guess_text.setValue(eKin_end)
		init_start_stop_panel.energy_err_text.setValue(0.)
		table_and_plots_panel = scl_long_tuneup_energy_meter_controller.table_and_plots_panel
		table_and_plots_panel.bpm_table.getModel().fireTableDataChanged()

class Start_Statistics_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		statistic_state_controller = scl_long_tuneup_energy_meter_controller.statistic_state_controller
		if(statistic_state_controller.getIsRunning()): return
		statistic_runner = Statistic_Runner(self.scl_long_tuneup_controller)
		thr = Thread(statistic_runner)
		thr.start()				

class Stop_Statistics_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		scl_long_tuneup_energy_meter_controller.statistic_state_controller.setShouldStop(true)
		
class Zero_Offsets_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller		

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		bpm_wrappers_holder.setOffsetsToZero()
		table_and_plots_panel = scl_long_tuneup_energy_meter_controller.table_and_plots_panel
		table_and_plots_panel.bpm_table.getModel().fireTableDataChanged()		
		
class Correct_Offsets_Diff_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		bpm_wrappers_holder.addPhaseDiffToOffsets()
		table_and_plots_panel = scl_long_tuneup_energy_meter_controller.table_and_plots_panel
		table_and_plots_panel.bpm_table.getModel().fireTableDataChanged()
		
class Write_To_ASCII_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")			
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save BPM Offsets Table into ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_path+".dat"		
			fl_ph_out = open(fl_path,"w")
			txt = "#    BPM     pos[m]    offset[deg]   err[deg]"
			fl_ph_out.write(txt+"\n")
			scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
			bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
			em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
			count = 0
			for bpm_ind in range(len(em_bpm_wrpprs)):
				bpm_wrapper = em_bpm_wrpprs[bpm_ind]
				count += 1
				txt = str(count)+" "+bpm_wrapper.bpm.getId()+ " %9.3f "%bpm_wrapper.pos 
				txt += "  %7.2f"%bpm_wrappers_holder.bpm_phase_offsets[bpm_ind]
				txt += " 0.0 "
				fl_ph_out.write(txt+"\n")
			#---- end of writing
			fl_ph_out.flush()
			fl_ph_out.close()		
		
class Read_From_ASCII_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def actionPerformed(self,actionEvent):	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")			
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read BPM Offsets from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
			bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder			
			bpm_wrappers_holder.clean()
			fl_in = fc.getSelectedFile()
			fl_ph_in = open(fl_in.getPath(),"r")
			lns = fl_ph_in.readlines()
			fl_ph_in.close()
			for ln in lns:
				res_arr = ln.split()
				if(len(res_arr) == 5):
					bpm_name = res_arr[1]
					bpm_wrapper = self.scl_long_tuneup_controller.getBPM_WrapperForBPM_Id(bpm_name)
					if(bpm_wrapper != null):
						bpm_wrappers_holder.em_bpm_wrpprs.append(bpm_wrapper)
						bpm_wrappers_holder.use_bpms.append(true)
						bpm_wrappers_holder.bpm_phase_offsets.append(float(res_arr[3]))
						bpm_wrappers_holder.bpm_amp_phase_data_dict[bpm_wrapper] = ([],[])
						bpm_wrappers_holder.bpm_phase_diff_arr.append([])						
			#----- update table
			table_and_plots_panel = scl_long_tuneup_energy_meter_controller.table_and_plots_panel
			table_and_plots_panel.bpm_table.getModel().fireTableDataChanged()
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Energy_Meter_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()	
		#----- auxilary classes 
		self.bpm_wrappers_holder = BPMs_Wrappers_Holder(self.scl_long_tuneup_controller)
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		#------table panel --------
		center_panel = JPanel(BorderLayout())
		#-------- bottom actions panel
		bottom_panel = JPanel(BorderLayout())
		#--------------------------------------------------
		self.init_start_stop_panel = InitStartStopStatistics_Panel(self.scl_long_tuneup_controller)
		top_panel.add(self.init_start_stop_panel,BorderLayout.NORTH)
		self.table_and_plots_panel = Table_and_Plots_Panel(self.scl_long_tuneup_controller)
		center_panel.add(self.table_and_plots_panel,BorderLayout.CENTER)
		self.manipulate_offsets_panel = Manipulate_Offsets_Panel(self.scl_long_tuneup_controller)
		bottom_panel.add(self.manipulate_offsets_panel,BorderLayout.NORTH)
		#------ Statistic run controller
		self.statistic_state_controller = StatisticStateController()
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		self.table_and_plots_panel.bpm_table.getModel().fireTableDataChanged()
		
	def writeDataToXML(self,root_da):
		if(self.isWorthToSave()):
			energy_meter_da = root_da.createChild("Long_Tune_Energy_Meter_Data")
			eKin_guess = self.init_start_stop_panel.energy_guess_text.getValue()
			energy_meter_da.setValue("eKin_guess",eKin_guess)
			for bpm_ind in range(len( self.bpm_wrappers_holder.em_bpm_wrpprs)):
				bpm_wrapper = self.bpm_wrappers_holder.em_bpm_wrpprs[bpm_ind]
				bpm_da = energy_meter_da.createChild("BPM")
				bpm_da.setValue("alias",bpm_wrapper.alias)
				bpm_da.setValue("Use",self.bpm_wrappers_holder.use_bpms[bpm_ind])
				bpm_da.setValue("offset",self.bpm_wrappers_holder.bpm_phase_offsets[bpm_ind])
				
	def readDataFromXML(self,root_da):
		self.bpm_wrappers_holder.clean()
		energy_meter_da = root_da.childAdaptor("Long_Tune_Energy_Meter_Data")
		if(energy_meter_da == null): return
		#print "debug energy_meter_da=",energy_meter_da
		eKin_guess = energy_meter_da.doubleValue("eKin_guess")
		self.init_start_stop_panel.energy_guess_text.setValue(eKin_guess)
		for bpm_da in energy_meter_da.childAdaptors("BPM"):
			alias = bpm_da.stringValue("alias")
			use = Boolean(bpm_da.intValue("Use")).booleanValue()
			offset = bpm_da.doubleValue("offset")
			bpm_wrapper = self.scl_long_tuneup_controller.getBPM_Wrapper(alias)
			if(bpm_wrapper != null):
				self.bpm_wrappers_holder.em_bpm_wrpprs.append(bpm_wrapper)
				self.bpm_wrappers_holder.use_bpms.append(use)
				self.bpm_wrappers_holder.bpm_phase_offsets.append(offset)
				self.bpm_wrappers_holder.bpm_amp_phase_data_dict[bpm_wrapper] = ([],[])
				self.bpm_wrappers_holder.bpm_phase_diff_arr.append([])
				#print "debug alias=",bpm_wrapper.alias," use=",use," offset=",offset," size=",len(self.bpm_wrappers_holder.em_bpm_wrpprs)
						
	def isWorthToSave(self):
		if(len(self.bpm_wrappers_holder.em_bpm_wrpprs) > 0):
			return true
		else:
			return false
			
	def clean(self):
		self.bpm_wrappers_holder.clean()
		self.statistic_state_controller.setShouldStop(true)

	
