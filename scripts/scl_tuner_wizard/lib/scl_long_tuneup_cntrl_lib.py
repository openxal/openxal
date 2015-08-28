# The SCL Longitudinal Tune-Up main controller. 
# It keeps the references to all other sub-controllers.

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from java.util import ArrayList
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap, BPM
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf import AcceleratorSeqCombo
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.smf.proxy import RfGapPropertyAccessor,ElectromagnetPropertyAccessor


from constants_lib import GRAPH_LEGEND_KEY
from beam_trigger_lib import BeamTrigger
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader

from scl_long_tuneup_init_cntrl_lib import SCL_Long_TuneUp_Init_Controller
from scl_phase_scan_data_acquisition_lib import BPM_Wrapper, SCL_Cavity_Wrapper
from scl_long_tuneup_phase_scan_cntrl_lib  import SCL_Long_TuneUp_PhaseScan_Controller
from scl_long_tuneup_bpm_offsets_cntrl_lib import SCL_Long_TuneUp_BPM_Offsets_Controller
from scl_long_tuneup_phase_analysis_cntrl_lib import SCL_Long_TuneUp_PhaseAnalysis_Controller
from scl_long_tuneup_rescale_cntrl_lib import SCL_Long_TuneUp_Rescale_Controller
from scl_long_tuneup_energy_meter_cntrl_lib import SCL_Energy_Meter_Controller
from scl_long_tuneup_long_twiss_cntrl_lib import SCL_Long_Twiss_Analysis_Controller
from scl_long_tuneup_laser_stripping_cntrl_lib import SCL_Laser_Stripping_Controller

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_Controller:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document	
		#----scl_accSeq is a specific for this controller
		accl = self.linac_wizard_document.accl
		lst = ArrayList()
		for seqName in ["SCLMed","SCLHigh","HEBT1","HEBT2"]:	
			lst.add(accl.getSequence(seqName))
		self.scl_accSeq = AcceleratorSeqCombo("SCL_SEQUENCE", lst)	
		part_tracker = AlgorithmFactory.createParticleTracker(self.scl_accSeq)
		part_probe = ProbeFactory.createParticleProbe(self.scl_accSeq,part_tracker)
		self.mass = part_probe.getSpeciesRestEnergy()
		self.bpm_freq = 402.5e+6
		self.c_light = 2.99792458e+8
		#-------- Ring length 
		self.ring_length = accl.findSequence("Ring").getLength()
		#-------- BPMs and Cavities arrays
		self.bpm_wrappers = []
		self.cav_wrappers = []
		#-------- self.cav0_wrapper is used for all cavities (includind Cav01a) Blanked statistics
		self.cav0_wrapper = null
		self.fillOut_Arrays()	
		#--- the main tabbed pane		
		self.tabbedPane = JTabbedPane()
		#-------- child controllers 
		self.scl_long_tuneup_init_controller = SCL_Long_TuneUp_Init_Controller(self)
		self.scl_long_tuneup_phase_scan_controller = SCL_Long_TuneUp_PhaseScan_Controller(self)
		self.scl_long_tuneup_bpm_offsets_controller = SCL_Long_TuneUp_BPM_Offsets_Controller(self)
		self.scl_long_tuneup_phase_analysis_controller = SCL_Long_TuneUp_PhaseAnalysis_Controller(self)
		self.scl_long_tuneup_rescale_controller = SCL_Long_TuneUp_Rescale_Controller(self)
		self.scl_long_tuneup_energy_meter_controller = SCL_Energy_Meter_Controller(self)	
		self.scl_long_twiss_analysis_controller = SCL_Long_Twiss_Analysis_Controller(self)
		self.scl_long_laser_stripping_controller = SCL_Laser_Stripping_Controller(self)
		#the beamTrigger will be initialized after the user hit "Init" button in SCL_Long_Init_Controller 
		self.beamTrigger = null
		#sets of BPMs for BPM_Batch_Reader will be setup in "Init" button in SCL_Long_Init_Controller 
		self.bpmBatchReader = BPM_Batch_Reader(self)
		#----add all subpanels to the SCL main tab panel
		self.tabbedPane.add("Init",self.scl_long_tuneup_init_controller.getMainPanel())
		self.tabbedPane.add("Phase Scan",self.scl_long_tuneup_phase_scan_controller.getMainPanel())		
		self.tabbedPane.add("BPM Offsets",self.scl_long_tuneup_bpm_offsets_controller.getMainPanel())	
		self.tabbedPane.add("Phase Analysis",self.scl_long_tuneup_phase_analysis_controller.getMainPanel())
		self.tabbedPane.add("Rescale SCL",self.scl_long_tuneup_rescale_controller.getMainPanel())
		self.tabbedPane.add("Energy Meter",self.scl_long_tuneup_energy_meter_controller.getMainPanel())
		self.tabbedPane.add("Long. Twiss",self.scl_long_twiss_analysis_controller.getMainPanel())
		self.tabbedPane.add("Laser Stripping",self.scl_long_laser_stripping_controller.getMainPanel())
		
	def getMainPanel(self):
		return self.tabbedPane
		
	def fillOut_Arrays(self):
		bpms = self.scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType))).andStatus(true))
		for bpm in bpms:
			#print "debug bpm=",bpm.getId()
			bpm_wrapper = BPM_Wrapper(bpm)
			bpm_wrapper.setPosition(self.scl_accSeq)
			self.bpm_wrappers.append(bpm_wrapper)
		rf_gaps = self.scl_accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		cavs = []
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			if((cav not in cavs) and cav.getStatus()):
				#print "debug cav=",cav.getId()				
				cavs.append(cav)
				cav_wrapper = SCL_Cavity_Wrapper(cav)
				cav_wrapper.setPosition(self.scl_accSeq)
				cav_wrapper.setUpBPM_Wrappers(self.bpm_wrappers,self)
				amp = cav.getDfltCavAmp()
				phase = cav.getDfltCavPhase()				
				cav_wrapper.initDesignAmp = amp
				cav_wrapper.initDesignPhase = phase
				cav_wrapper.designAmp = 0.
				cav_wrapper.designPhase = 0.
				if(amp < 0.1 ):
					cav_wrapper.isGood = false
				self.cav_wrappers.append(cav_wrapper)
		self.cav_wrappers[0].eKin_in = 185.6
		self.cav0_wrapper = SCL_Cavity_Wrapper(cavs[0])
		self.cav0_wrapper.alias = "AllOff"
		self.cav0_wrapper.setUpBPM_Wrappers(self.cav_wrappers[0].bpm_wrappers,self)
		self.cav0_wrapper.scanPhaseShift = 0.
		
	def getMessageTextField(self):
		return self.linac_wizard_document.getMessageTextField()
		
	def getBPM_Wrapper(self,alias):
		for bpm_wrapper in self.bpm_wrappers:
			if(alias == bpm_wrapper.alias): return bpm_wrapper
		return null
		
	def getBPM_WrapperForBPM_Id(self,bpm_id):
		for bpm_wrapper in self.bpm_wrappers:
			if(bpm_id == bpm_wrapper.bpm.getId()): return bpm_wrapper
		return null			
		
	def getCav_Wrapper(self,alias):
		if(alias == self.cav0_wrapper.alias): return self.cav0_wrapper
		for cav_wrapper in self.cav_wrappers:
			if(alias == cav_wrapper.alias): return cav_wrapper
		return null		
		
	def getCav_WrapperForCavId(self,cav_id):
		for cav_wrapper in self.cav_wrappers:
			if(cav_id == cav_wrapper.cav.getId()): return cav_wrapper
		return null	
		
	def clean(self):
		for bpm_wrapper in self.bpm_wrappers:
			bpm_wrapper.clean()
		self.cav0_wrapper.clean()
		for cav_wrapper in self.cav_wrappers:
			cav_wrapper.clean()
		self.cav_wrappers[0].eKin_in = 185.6
		self.scl_long_tuneup_energy_meter_controller.clean()
		self.scl_long_twiss_analysis_controller.clean()
		
	def updateAllTables(self):
		self.scl_long_tuneup_init_controller.bpm_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_init_controller.cav_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_phase_scan_controller.cavs_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_phase_scan_controller.bpms_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_bpm_offsets_controller.bpm_offsets_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_phase_analysis_controller.cavs_table.getModel().fireTableDataChanged()
		self.scl_long_tuneup_rescale_controller.updateTables()
		self.scl_long_tuneup_energy_meter_controller.updateTables()
		self.scl_long_twiss_analysis_controller.updateTables()
		
	def writeDataToXML(self,root_da):
		if(self.isWorthToSave()):
			scl_phase_scan_da = root_da.createChild("SCL_Longitudinal_Tuneup_Data")
			#---- write parameters of the scan
			scan_parameters_da = scl_phase_scan_da.createChild("SCAN_Parameters")
			bpm_amp_limit = self.scl_long_tuneup_phase_scan_controller.post_scan_panel.amp_limit_text.getValue()
			rf_phase_step = self.scl_long_tuneup_phase_scan_controller.start_stop_scan_panel.phase_step_text.getValue()
			time_wait = self.scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.time_wait_text.getValue()
			min_bpm_dist = self.scl_long_tuneup_init_controller.min_bpm_dist_txt.getValue()
			max_bpm_dist = self.scl_long_tuneup_init_controller.max_bpm_dist_txt.getValue()
			scan_parameters_da.setValue("bpm_amp_limit",bpm_amp_limit)			
			scan_parameters_da.setValue("rf_phase_step",rf_phase_step)
			scan_parameters_da.setValue("time_wait",time_wait)
			scan_parameters_da.setValue("min_bpm_dist",min_bpm_dist)
			scan_parameters_da.setValue("max_bpm_dist",max_bpm_dist)
			#---- write BPMs and Cavities params
			bpm_wrappers_da = scl_phase_scan_da.createChild("BPMs_Parameters_and_Data")
			for bpm_wrapper in self.bpm_wrappers:
				bpm_wrapper.writeDataToXML(bpm_wrappers_da)
			cav_wrappers_da = scl_phase_scan_da.createChild("Cavs_Parameters_and_Data")
			self.cav0_wrapper.writeDataToXML(cav_wrappers_da)
			for cav_wrapper in self.cav_wrappers:
				cav_wrapper.writeDataToXML(cav_wrappers_da)
			self.scl_long_twiss_analysis_controller.writeDataToXML(scl_phase_scan_da)		
			self.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.writeDataToXML(scl_phase_scan_da)					
		#------ write info from other controllers
		self.scl_long_tuneup_energy_meter_controller.writeDataToXML(root_da)
	
	def readDataFromXML(self,root_da):		
		#----------- SCL Long. Scan data ---------------------------------
		list_da = root_da.childAdaptors("SCL_Longitudinal_Tuneup_Data")
		if(not list_da.isEmpty()):
			scl_phase_scan_da = list_da.get(0)
			#---- get general scan parameters
			scan_parameters_da = scl_phase_scan_da.childAdaptor("SCAN_Parameters")
			if(scan_parameters_da != null):
				if(scan_parameters_da.hasAttribute("bpm_amp_limit")):
					bpm_amp_limit = scan_parameters_da.doubleValue("bpm_amp_limit")
					self.scl_long_tuneup_phase_scan_controller.post_scan_panel.amp_limit_text.setValue(bpm_amp_limit)
				if(scan_parameters_da.hasAttribute("rf_phase_step")):
					rf_phase_step = scan_parameters_da.doubleValue("rf_phase_step")
					self.scl_long_tuneup_phase_scan_controller.start_stop_scan_panel.phase_step_text.setValue(rf_phase_step)
				if(scan_parameters_da.hasAttribute("time_wait")):
					time_wait = scan_parameters_da.doubleValue("time_wait")
					self.scl_long_tuneup_phase_scan_controller.set_phase_shift_panel.time_wait_text.setValue(time_wait)
				if(scan_parameters_da.hasAttribute("min_bpm_dist")):
					min_bpm_dist = scan_parameters_da.doubleValue("min_bpm_dist")
					self.scl_long_tuneup_init_controller.min_bpm_dist_txt.setValue(min_bpm_dist)
				if(scan_parameters_da.hasAttribute("max_bpm_dist")):
					max_bpm_dist = scan_parameters_da.doubleValue("max_bpm_dist")
					self.scl_long_tuneup_init_controller.max_bpm_dist_txt.setValue(max_bpm_dist)	
			#---- get BPM amd Cav information
			bpm_wrappers_missing = self.bpm_wrappers[:]
			bpm_wrappers_da = scl_phase_scan_da.childAdaptor("BPMs_Parameters_and_Data")
			for bpm_wrapper_da in bpm_wrappers_da.childAdaptors():
				bpm_wrapper = self.getBPM_Wrapper(bpm_wrapper_da.stringValue("alias"))
				if(bpm_wrapper != null):
					#print "debug bpm=",bpm_wrapper.alias
					bpm_wrapper.readDataFromXML(bpm_wrapper_da,self)
					bpm_wrappers_missing.remove(bpm_wrapper)
			for bpm_wrapper in bpm_wrappers_missing:
				bpm_wrapper.isGood = false
			#----------- cavities ------------------------------------------
			cav_wrappers_da = scl_phase_scan_da.childAdaptor("Cavs_Parameters_and_Data")
			for cav_wrapper_da in cav_wrappers_da.childAdaptors():
				cav_wrapper = self.getCav_Wrapper(cav_wrapper_da.name())
				if(cav_wrapper != null):
					#print "debug cav=",cav_wrapper.alias
					cav_wrapper.readDataFromXML(cav_wrapper_da,self)
			#------ read info from other longitudinal scan controllers
			self.scl_long_twiss_analysis_controller.readDataFromXML(scl_phase_scan_da)	
			self.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.readDataFromXML(scl_phase_scan_da)			
		#------ read info from other controllers
		self.scl_long_tuneup_energy_meter_controller.readDataFromXML(root_da)
	
	def putFoundConfigInAccelerator(self):
		# This method will be called from linac setup controller to 
		# update accelerator from the found cavities amplitudes and phases
		# After this method the design amp and phases will be different
		for cav_wrapper in self.cav_wrappers:
			if(cav_wrapper.isGood):
				cav_wrapper.cav.updateDesignAmp(cav_wrapper.designAmp)
				cav_wrapper.cav.updateDesignPhase(cav_wrapper.designPhase)
			else:
				cav_wrapper.cav.updateDesignAmp(0.)
				cav_wrapper.cav.updateDesignPhase(0.)				
				
	def isWorthToSave(self):
		is_worth = false
		for cav_wrapper in self.cav_wrappers:
			if(cav_wrapper.isGood):
				if(cav_wrapper.isMeasured or cav_wrapper.isAnalyzed):
					is_worth = true
					break
		return is_worth
					
					
		

						
