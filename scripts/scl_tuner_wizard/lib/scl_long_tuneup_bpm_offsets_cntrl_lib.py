# The SCL Longitudinal Tune-Up Cavities'
# It will measure and calculate BPMs' phase offsets

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
from java.io import File, BufferedWriter, FileWriter
from javax.swing.filechooser import FileNameExtensionFilter

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.plot import GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from xal.ca import ChannelFactory

import constants_lib 
from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, makePhaseNear, calculateAvgErr

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Auxiliary BPM Offsets Package classes and functions
#------------------------------------------------------------------------	
def initEnergyInGuessHarmFunc(cav_wrapper,scl_long_tuneup_controller):
	#this function will find the approximate parameters of 
	#the energy_guess_harm_funcion in cav_wrapper
	eKin_in = cav_wrapper.eKin_in_guess
	mass = scl_long_tuneup_controller.mass/1.0e+6			
	c_light = scl_long_tuneup_controller.c_light	
	bpm_freq = scl_long_tuneup_controller.bpm_freq
	coeff = 360.0*bpm_freq*(cav_wrapper.bpm_wrapper1.pos - cav_wrapper.bpm_wrapper0.pos)
	coeff /= c_light
	p = math.sqrt(eKin_in*(eKin_in+2*mass))
	coeff *= mass*mass/p**3
	# the cav_wrapper.phase_scan_harm_funcion should be prepared 
	# when we found the new cavity phase in the scan	
	param_arr = cav_wrapper.phase_scan_harm_funcion.getParamArr()[:]
	dE = param_arr[1]/coeff
	param_arr[0] = eKin_in
	param_arr[1] /= -coeff
	param_arr[3] /= -coeff
	cav_wrapper.energy_guess_harm_funcion.setParamArr(param_arr)
	#print "debug cav=",cav_wrapper.alias," dE[MeV]=",dE," eKin_in =",eKin_in
	
def initEnergyOutGuessHarmFunc(cav_wrapper,scl_long_tuneup_controller):
	#this function will find the approximate parameters of 
	#the energy_guess_harm_funcion in cav_wrapper
	eKin_in = cav_wrapper.eKin_out_guess
	mass = scl_long_tuneup_controller.mass/1.0e+6			
	c_light = scl_long_tuneup_controller.c_light	
	bpm_freq = scl_long_tuneup_controller.bpm_freq
	coeff = 360.0*bpm_freq*(cav_wrapper.bpm_wrapper1.pos - cav_wrapper.bpm_wrapper0.pos)
	coeff /= c_light
	p = math.sqrt(eKin_in*(eKin_in+2*mass))
	coeff *= mass*mass/p**3
	# the cav_wrapper.phase_scan_harm_funcion should be prepared 
	# when we found the new cavity phase in the scan	
	param_arr = cav_wrapper.phase_scan_harm_funcion.getParamArr()[:]
	dE = param_arr[1]/coeff
	param_arr[0] = eKin_in
	param_arr[1] /= -coeff
	param_arr[3] /= -coeff
	cav_wrapper.energy_guess_harm_funcion.setParamArr(param_arr)
	eKin_out = cav_wrapper.energy_guess_harm_funcion.getValue(cav_wrapper.livePhase)
	eKin_in = eKin_in - (eKin_out - eKin_in)
	param_arr[0] = eKin_in
	cav_wrapper.energy_guess_harm_funcion.setParamArr(param_arr)
	cav_wrapper.eKin_in_guess = eKin_in
	#print "debug cav=",cav_wrapper.alias," dE[MeV]=",dE," eKin_in =",eKin_in	
	
def calculateOffsetsForNotDoneBPMsLeft(cav_wrapper,scl_long_tuneup_controller,bpm_wrappers_good_arr,bpm_wrappers_not_done_arr):
	# this function will calculate offsets for "left" offsets of 
	# "Not yet Done" BPMs by using "good" (already done) BPMs
	eKin_in = cav_wrapper.eKin_in_guess
	mass = scl_long_tuneup_controller.mass/1.0e+6			
	c_light = scl_long_tuneup_controller.c_light	
	bpm_freq = scl_long_tuneup_controller.bpm_freq
	coeff_init = 360.0*bpm_freq/c_light
	for bpm_wrapper in bpm_wrappers_not_done_arr:
		bpm_wrapper.left_phase_offset.phaseOffset_arr = []
	phaseDiffPlot = cav_wrapper.phaseDiffPlot
	cav_wrapper.eKinOutPlot.removeAllPoints()
	cav_wrapper.eKinOutPlotTh.removeAllPoints()
	for ip in range(phaseDiffPlot.getNumbOfPoints()):
		cav_phase = phaseDiffPlot.getX(ip)
		ekin_guess =  cav_wrapper.energy_guess_harm_funcion.getValue(cav_phase)
		beta_guess = math.sqrt(ekin_guess*(ekin_guess+2*mass))/(ekin_guess+mass)
		coeff = coeff_init/beta_guess
		# let's make bpm_phase(z) points for good BPMs
		gd = BasicGraphData()
		base_bpm_wrapper = bpm_wrappers_good_arr[0]
		(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[base_bpm_wrapper]
		base_bpm_offset = base_bpm_wrapper.left_phase_offset.phaseOffset_avg	
		base_bpm_phase = graphDataPhase.getY(ip) - base_bpm_offset
		#print "debug ==== ip=",ip," cav_phase=",cav_phase," eKin_guess=",ekin_guess	
		gd.addPoint(base_bpm_wrapper.pos,base_bpm_phase)
		for bpm_ind in range(1,len(bpm_wrappers_good_arr)):
			bpm_wrapper = bpm_wrappers_good_arr[bpm_ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			bpm_phase = graphDataPhase.getY(ip) - bpm_wrapper.left_phase_offset.phaseOffset_avg
			bpm_pos = bpm_wrapper.pos
			bpm_phase_guess = gd.getY(bpm_ind-1) + coeff*(bpm_pos-gd.getX(bpm_ind-1))
			bpm_phase = makePhaseNear(bpm_phase,bpm_phase_guess)
			gd.addPoint(bpm_pos,bpm_phase)
			#print "debug bpm=",bpm_wrapper.alias," pos=",bpm_pos," phase=",bpm_phase
		res_arr = GraphDataOperations.polynomialFit(gd,-1.e+36,+1.e+36,1)
		slope = res_arr[0][1]
		slope_err = res_arr[1][1]
		init_phase = res_arr[0][0]
		init_phase_err = res_arr[1][0]
		beta = coeff_init/slope
		gamma = 1./math.sqrt(1.0-beta*beta)
		eKin = mass*(gamma-1.0)	
		delta_eKin = mass*gamma**3*beta**3*slope_err/coeff_init
		cav_wrapper.eKinOutPlot.addPoint(cav_phase,eKin,delta_eKin)
		#print "debug cav_phase=",cav_phase,"eKin_guess=",ekin_guess," eKin=",eKin," delta_E=",delta_eKin
		# let's go over the bad BPMs and calculate offsets for particular cavity phase
		for bpm_wrapper in bpm_wrappers_not_done_arr:
			bpm_pos = bpm_wrapper.pos
			bpm_phase_th = init_phase + slope*bpm_pos
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			bpm_phase = graphDataPhase.getY(ip)
			bpm_offset = makePhaseNear(bpm_phase - bpm_phase_th,0.)
			bpm_wrapper.left_phase_offset.phaseOffset_arr.append(bpm_offset)
	#----set up the output energy guess
	cav_wrapper.eKin_out_guess = cav_wrapper.eKinOutPlot.getValueY(cav_wrapper.livePhase)
	# let's calculate statistics for "not done" BPMs' offsets
	for bpm_wrapper in bpm_wrappers_not_done_arr:
		phase_arr = bpm_wrapper.left_phase_offset.phaseOffset_arr
		(phase0_avg, phase0_err) = calculateAvgErr(phase_arr)
		phase_arr = []
		for phase in bpm_wrapper.left_phase_offset.phaseOffset_arr:
			phase_arr.append(makePhaseNear(phase-180.,0.))
		(phase1_avg, phase1_err) = calculateAvgErr(phase_arr)
		phase1_avg = makePhaseNear(phase1_avg+180.,0.)
		if(phase1_err < phase0_err):
			bpm_wrapper.left_phase_offset.phaseOffset_avg = phase1_avg
			bpm_wrapper.left_phase_offset.phaseOffset_err = phase1_err
		else:
			bpm_wrapper.left_phase_offset.phaseOffset_avg = phase0_avg
			bpm_wrapper.left_phase_offset.phaseOffset_err = phase0_err
		bpm_wrapper.left_phase_offset.isReady = true

def calculateOffsetsForNotDoneBPMsRight(cav_wrapper,scl_long_tuneup_controller,bpm_wrappers_good_arr,bpm_wrappers_not_done_arr):
	# this function will calculate offsets for "right" offsets of 
	# "Not yet Done" BPMs by using "good" (already done) BPMs
	eKin_in = cav_wrapper.eKin_in_guess
	mass = scl_long_tuneup_controller.mass/1.0e+6			
	c_light = scl_long_tuneup_controller.c_light	
	bpm_freq = scl_long_tuneup_controller.bpm_freq
	coeff_init = 360.0*bpm_freq/c_light
	for bpm_wrapper in bpm_wrappers_not_done_arr:
		bpm_wrapper.right_phase_offset.phaseOffset_arr = []
	(graphDataAmp,phaseDiffPlot) = cav_wrapper.bpm_amp_phase_dict[bpm_wrappers_good_arr[0]]
	cav_wrapper.eKinOutPlot.removeAllPoints()
	cav_wrapper.eKinOutPlotTh.removeAllPoints()
	for ip in range(phaseDiffPlot.getNumbOfPoints()):
		cav_phase = phaseDiffPlot.getX(ip)
		ekin_guess =  cav_wrapper.energy_guess_harm_funcion.getValue(cav_phase)
		beta_guess = math.sqrt(ekin_guess*(ekin_guess+2*mass))/(ekin_guess+mass)
		coeff = coeff_init/beta_guess
		# let's make bpm_phase(z) points for good BPMs
		gd = BasicGraphData()
		base_bpm_wrapper = bpm_wrappers_good_arr[0]
		(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[base_bpm_wrapper]
		base_bpm_offset = base_bpm_wrapper.right_phase_offset.phaseOffset_avg	
		base_bpm_phase = graphDataPhase.getY(ip) - base_bpm_offset
		#print "debug ==== ip=",ip," cav_phase=",cav_phase," eKin_guess=",ekin_guess	
		gd.addPoint(base_bpm_wrapper.pos,base_bpm_phase)
		for bpm_ind in range(1,len(bpm_wrappers_good_arr)):
			bpm_wrapper = bpm_wrappers_good_arr[bpm_ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			bpm_phase = graphDataPhase.getY(ip) - bpm_wrapper.right_phase_offset.phaseOffset_avg
			bpm_pos = bpm_wrapper.pos
			bpm_phase_guess = gd.getY(bpm_ind-1) + coeff*(bpm_pos-gd.getX(bpm_ind-1))
			bpm_phase = makePhaseNear(bpm_phase,bpm_phase_guess)
			gd.addPoint(bpm_pos,bpm_phase)
			#print "debug bpm=",bpm_wrapper.alias," pos=",bpm_pos," phase=",bpm_phase
		res_arr = GraphDataOperations.polynomialFit(gd,-1.e+36,+1.e+36,1)
		slope = res_arr[0][1]
		slope_err = res_arr[1][1]
		init_phase = res_arr[0][0]
		init_phase_err = res_arr[1][0]
		beta = coeff_init/slope
		gamma = 1./math.sqrt(1.0-beta*beta)
		eKin = mass*(gamma-1.0)	
		delta_eKin = mass*gamma**3*beta**3*slope_err/coeff_init
		cav_wrapper.eKinOutPlot.addPoint(cav_phase,eKin,delta_eKin)
		#print "debug cav_phase=",cav_phase,"eKin_guess=",ekin_guess," eKin=",eKin," delta_E=",delta_eKin
		# let's go over the bad BPMs and calculate offsets for particular cavity phase
		for bpm_wrapper in bpm_wrappers_not_done_arr:
			bpm_pos = bpm_wrapper.pos
			bpm_phase_th = init_phase + slope*bpm_pos
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			bpm_phase = graphDataPhase.getY(ip)
			bpm_offset = makePhaseNear(bpm_phase - bpm_phase_th,0.)
			bpm_wrapper.right_phase_offset.phaseOffset_arr.append(bpm_offset)
	#----set up the output energy guess
	cav_wrapper.eKin_out_guess = cav_wrapper.eKinOutPlot.getValueY(cav_wrapper.livePhase)
	eKin_avg = 0.
	for i_ep in range(cav_wrapper.eKinOutPlot.getNumbOfPoints()):
		eKin_avg += cav_wrapper.eKinOutPlot.getY(i_ep)
	if(cav_wrapper.eKinOutPlot.getNumbOfPoints() != 0.):
		eKin_avg /= cav_wrapper.eKinOutPlot.getNumbOfPoints()
	cav_wrapper.eKin_in_guess = eKin_avg
	# let's calculate statistics for "not done" BPMs' offsets
	for bpm_wrapper in bpm_wrappers_not_done_arr:
		phase_arr = bpm_wrapper.right_phase_offset.phaseOffset_arr
		(phase0_avg, phase0_err) = calculateAvgErr(phase_arr)
		phase_arr = []
		for phase in bpm_wrapper.right_phase_offset.phaseOffset_arr:
			phase_arr.append(makePhaseNear(phase-180.,0.))
		(phase1_avg, phase1_err) = calculateAvgErr(phase_arr)
		phase1_avg = makePhaseNear(phase1_avg+180.,0.)
		if(phase1_err < phase0_err):
			bpm_wrapper.right_phase_offset.phaseOffset_avg = phase1_avg
			bpm_wrapper.right_phase_offset.phaseOffset_err = phase1_err
		else:
			bpm_wrapper.right_phase_offset.phaseOffset_avg = phase0_avg
			bpm_wrapper.right_phase_offset.phaseOffset_err = phase0_err
		bpm_wrapper.right_phase_offset.isReady = true

class BPMs_Offsets_from_CCL4_Calculator:
	# It calculates the BPM offsets starting from the CCL4 end of the SCL	
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def calculateOffSets(self):
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller			
		bpms_offsets_from_ccl4_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_ccl4_panel	
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		#--- transform eV to MeV
		mass = self.scl_long_tuneup_controller.mass/1.0e+6			
		c_light = self.scl_long_tuneup_controller.c_light
		eKin_in = bpms_offsets_from_ccl4_panel.ccl4_energy_text.getValue()
		gamma = (mass+eKin_in)/mass
		beta = math.sqrt(1.0-1.0/(gamma*gamma))		
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq	
		#--- initial parameters 
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			bpm_wrapper.left_phase_offset.phaseOffset_avg = 0.
			bpm_wrapper.left_phase_offset.phaseOffset_err = 0.
			bpm_wrapper.left_phase_offset.isReady = false
			bpm_wrapper.left_phase_offset.phaseOffset_arr = []
		#---- analysis of the "All Cavities Off" case
		cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		if(cav0_wrapper.isGood and cav0_wrapper.isMeasured):
			bpm_local_wrappers = []
			for bpm_ind in range(len(cav0_wrapper.bpm_wrappers)):
				bpm_wrapper = cav0_wrapper.bpm_wrappers[bpm_ind]
				if(cav0_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]):
					bpm_local_wrappers.append(bpm_wrapper)
					(graphDataAmp,graphDataPhase) = cav0_wrapper.bpm_amp_phase_dict[bpm_wrapper]
					phase_offset_obj = bpm_wrapper.left_phase_offset
					phase_arr= []
					for ip in range(graphDataPhase.getNumbOfPoints()):
						phase_arr.append(graphDataPhase.getY(ip))
					(phase_avg, err) = calculateAvgErr(phase_arr)
					phase_offset_obj.phase_val_tmp = phase_avg
					phase_offset_obj.phase_val_err_tmp = err
			if(len(bpm_local_wrappers) == 0):
				txt = "Cavity "+cav0_wrapper.alias+" all data are bad! Cannot do anything!"
				self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
				return
			#---- the average BPM phases for all cavities off are ready here ------- 
			bpm_phase_coef = 360.0*bpm_freq/(c_light*beta)
			base_bpm_wrapper = bpm_local_wrappers[0]
			base_pos = base_bpm_wrapper.pos
			base_bpm_phase = base_bpm_wrapper.left_phase_offset.phase_val_tmp
			base_bpm_phase_err = base_bpm_wrapper.left_phase_offset.phaseOffset_err
			for bpm_wrapper in bpm_local_wrappers:
				pos_delta = bpm_wrapper.pos - base_pos
				bpm_phase_th = base_bpm_phase + bpm_phase_coef*pos_delta
				phase_offset = makePhaseNear(bpm_wrapper.left_phase_offset.phase_val_tmp - bpm_phase_th,0.)
				err = math.sqrt(base_bpm_phase_err**2 + bpm_wrapper.left_phase_offset.phase_val_err_tmp**2)
				bpm_wrapper.left_phase_offset.phaseOffset_avg = phase_offset
				bpm_wrapper.left_phase_offset.phaseOffset_err = err
				bpm_wrapper.left_phase_offset.isReady = true
				bpm_wrapper.left_phase_offset.phase_val_tmp = 0.
				bpm_wrapper.left_phase_offset.phase_val_err_tmp = 0.
				#print "debug cav=",cav0_wrapper.alias," BPM=",bpm_wrapper.alias," offset= %4.1f +- %4.1f"%(phase_offset,err)
		#------ start moving along cavities -------------------
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[0]
		cav_wrapper.eKin_in_guess = eKin_in
		n_cavs = len(self.scl_long_tuneup_controller.cav_wrappers)
		for cav_ind in range(n_cavs):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind]
			if(cav_wrapper.isGood and (not cav_wrapper.isMeasured)): break
			if(cav_ind > 0):
				cav_wrapper.eKin_in_guess = self.scl_long_tuneup_controller.cav_wrappers[cav_ind-1].eKin_out_guess		
			if(not cav_wrapper.isGood):
				cav_wrapper.eKin_out_guess = cav_wrapper.eKin_in_guess
				continue
			# make array of BPMs already having offsets and not having ones
			bpm_wrappers_good_arr = []
			bpm_wrappers_not_done_arr = []
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				is_wanted = bpm_wrapper.isGood and bpm_wrapper.pos > cav_wrapper.pos
				is_wanted = is_wanted and cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]
				if(is_wanted):
					if(bpm_wrapper.left_phase_offset.isReady):
						bpm_wrappers_good_arr.append(bpm_wrapper)
					else:
						bpm_wrappers_not_done_arr.append(bpm_wrapper)
			initEnergyInGuessHarmFunc(cav_wrapper,self.scl_long_tuneup_controller)
			if(len(bpm_wrappers_good_arr) < 2):
				txt = "Cavity "+cav_wrapper.alias+" does not have enough good BPMs! Cannot do anything!"
				self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
				return				
			calculateOffsetsForNotDoneBPMsLeft(cav_wrapper,self.scl_long_tuneup_controller,bpm_wrappers_good_arr,bpm_wrappers_not_done_arr)
			#print "debug cav=",cav_wrapper.alias," n_bad=",len(bpm_wrappers_not_done_arr)
		#----set all BPMs that do not have offsets as not good for phase analysis
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.isGood):
				if(not bpm_wrapper.left_phase_offset.isReady):
					cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
					bpm_ind = cav0_wrapper.bpm_wrappers.index(bpm_wrapper)
					if(bpm_ind >= 0):
						cav0_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
						cav0_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
					for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
						bpm_ind = cav_wrapper.bpm_wrappers.index(bpm_wrapper)
						if(bpm_ind >= 0):
							cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
							cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
		#---- re-calculate phase offesets to the base BPM which is the first BPM after
		#---- the last cavity
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[n_cavs-1]
		base_bpm_wrapper = null
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.isGood):
				if(bpm_wrapper.left_phase_offset.isReady):
					if(bpm_wrapper.pos > cav_wrapper.pos):
						base_bpm_wrapper = bpm_wrapper
						break
		if(base_bpm_wrapper != null):
			base_bpm_phase_offset = base_bpm_wrapper.left_phase_offset.phaseOffset_avg
			for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
				if(bpm_wrapper.isGood):
					if(bpm_wrapper.left_phase_offset.isReady):
						phase_offset = bpm_wrapper.left_phase_offset.phaseOffset_avg
						phase_offset = makePhaseNear(phase_offset - base_bpm_phase_offset,0.)
						bpm_wrapper.left_phase_offset.phaseOffset_avg = phase_offset 
		#---- set the final energy after the last cavity
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[n_cavs-1]
		bpms_offsets_from_hebt1_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_hebt1_panel
		bpms_offsets_from_hebt1_panel.ring_energy_text.setValue(cav_wrapper.eKin_out_guess)
		self.scl_long_tuneup_controller.updateAllTables()
		
class BPMs_Offsets_from_HEBT1_Calculator:
	# It calculates the BPM offsets starting from the HEBT1 end of the SCL	
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def calculateOffSets(self):
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller			
		bpms_offsets_from_hebt1_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_hebt1_panel
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		#--- transform eV to MeV
		mass = self.scl_long_tuneup_controller.mass/1.0e+6			
		c_light = self.scl_long_tuneup_controller.c_light
		eKin_out = bpms_offsets_from_hebt1_panel.ring_energy_text.getValue()
		if(eKin_out == 0.):
			self.scl_long_tuneup_controller.getMessageTextField().setText("The Ring Energy is 0 GeV! Cannot calculate BPM phase offsets!")
			return
		gamma = (mass+eKin_out)/mass
		beta = math.sqrt(1.0-1.0/(gamma*gamma))		
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq	
		n_cavs = len(self.scl_long_tuneup_controller.cav_wrappers)
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[n_cavs-1]
		#--- initial parameters 
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.pos < cav_wrapper.pos):
				bpm_wrapper.right_phase_offset.phaseOffset_avg = 0.
				bpm_wrapper.right_phase_offset.phaseOffset_err = 0.
				bpm_wrapper.right_phase_offset.isReady = false
				bpm_wrapper.right_phase_offset.phaseOffset_arr = []
		cav_wrapper.eKin_out_guess = eKin_out
		#------ start moving along cavities -------------------
		for cav_ind in range(n_cavs-1,-1,-1):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind]
			if(cav_wrapper.isGood and (not cav_wrapper.isMeasured)): break
			if(cav_ind != n_cavs-1):
				cav_wrapper.eKin_out_guess = self.scl_long_tuneup_controller.cav_wrappers[cav_ind+1].eKin_in_guess
			if(not cav_wrapper.isGood):
				cav_wrapper.eKin_in_guess = cav_wrapper.eKin_out_guess
				continue				
			# make array of BPMs already having offsets and not having ones
			bpm_wrappers_good_arr = []
			bpm_wrappers_not_done_arr = []
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				is_wanted = bpm_wrapper.isGood and bpm_wrapper.pos > cav_wrapper.pos
				is_wanted = is_wanted and cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]
				if(is_wanted):
					if(bpm_wrapper.right_phase_offset.isReady):
						bpm_wrappers_good_arr.append(bpm_wrapper)
					else:
						bpm_wrappers_not_done_arr.append(bpm_wrapper)
			initEnergyOutGuessHarmFunc(cav_wrapper,self.scl_long_tuneup_controller)
			if(len(bpm_wrappers_good_arr) < 2):
				txt = "Cavity "+cav_wrapper.alias+" does not have enough good BPMs! Cannot do anything!"
				self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
				return				
			calculateOffsetsForNotDoneBPMsRight(cav_wrapper,self.scl_long_tuneup_controller,bpm_wrappers_good_arr,bpm_wrappers_not_done_arr)
			#print "debug cav=",cav_wrapper.alias," n_bad=",len(bpm_wrappers_not_done_arr)," eKin_in=",cav_wrapper.eKin_in_guess," eKin_out=",cav_wrapper.eKin_out_guess
		#---- analysis of the "All Cavities Off" case
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[0]
		cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
		param_arr = [cav_wrapper.eKin_in_guess,]
		cav0_wrapper.energy_guess_harm_funcion.setParamArr(param_arr)
		cav0_wrapper.eKin_in_guess = cav_wrapper.eKin_in_guess
		cav0_wrapper.eKin_out_guess = cav_wrapper.eKin_in_guess
		if(cav0_wrapper.isGood and cav0_wrapper.isMeasured):
			# make array of BPMs already having offsets and not having ones
			bpm_wrappers_good_arr = []
			bpm_wrappers_not_done_arr = []
			for bpm_ind in range(len(cav0_wrapper.bpm_wrappers)):
				bpm_wrapper = cav0_wrapper.bpm_wrappers[bpm_ind]
				is_wanted = bpm_wrapper.isGood
				is_wanted = is_wanted and cav0_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]
				if(is_wanted):
					if(bpm_wrapper.right_phase_offset.isReady):
						bpm_wrappers_good_arr.append(bpm_wrapper)
					else:
						bpm_wrappers_not_done_arr.append(bpm_wrapper)
			if(len(bpm_wrappers_good_arr) < 2):
				txt = "Cavity "+cav_wrapper.alias+" does not have enough good BPMs! Cannot do anything!"
				self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
				return
			calculateOffsetsForNotDoneBPMsRight(cav0_wrapper,self.scl_long_tuneup_controller,bpm_wrappers_good_arr,bpm_wrappers_not_done_arr)
			#print "debug cav=",cav0_wrapper.alias," n_bad=",len(bpm_wrappers_not_done_arr)," eKin_in=",cav0_wrapper.eKin_in_guess," eKin_out=",cav0_wrapper.eKin_out_guess
		else:
			txt = "All cavities Off Case does not have the measured data!"
			self.scl_long_tuneup_controller.getMessageTextField().setText(txt)
		#---- set the calculated energy at CCL4 exit
		bpms_offsets_from_hebt1_panel.init_energy_text.setValue(cav0_wrapper.eKin_out_guess)
		#----set all BPMs that do not have offsets as not good for phase analysis
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.isGood):
				if(not bpm_wrapper.right_phase_offset.isReady):
					cav0_wrapper = self.scl_long_tuneup_controller.cav0_wrapper
					bpm_ind = cav0_wrapper.bpm_wrappers.index(bpm_wrapper)
					if(bpm_ind >= 0):
						cav0_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
						cav0_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
					for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
						bpm_ind = cav_wrapper.bpm_wrappers.index(bpm_wrapper)
						if(bpm_ind >= 0):
							cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind] = false
							cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
		#---- recalculate phase offesets to the base BPM which is the first BPM after
		#---- the last cavity
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[n_cavs-1]
		base_bpm_wrapper = null
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.isGood):
				if(bpm_wrapper.right_phase_offset.isReady):
					if(bpm_wrapper.pos > cav_wrapper.pos):
						base_bpm_wrapper = bpm_wrapper
						break
		if(base_bpm_wrapper != null):
			base_bpm_phase_offset = base_bpm_wrapper.right_phase_offset.phaseOffset_avg
			for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
				if(bpm_wrapper.isGood):
					if(bpm_wrapper.right_phase_offset.isReady):
						phase_offset = bpm_wrapper.right_phase_offset.phaseOffset_avg
						phase_offset = makePhaseNear(phase_offset - base_bpm_phase_offset,0.)
						bpm_wrapper.right_phase_offset.phaseOffset_avg = phase_offset 
		self.scl_long_tuneup_controller.updateAllTables()		
	
class Statistic_Runner(Runnable):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		max_pos = 280.0		
		self.bpm_wrappers_arr = []
		self.bpm_amp_phase_data_dict = {}
		n_cavs = len(self.scl_long_tuneup_controller.cav_wrappers)
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[n_cavs-1]
		for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
			if(bpm_wrapper.isGood and (bpm_wrapper.pos > cav_wrapper.pos) and bpm_wrapper.pos < max_pos):
				self.bpm_wrappers_arr.append(bpm_wrapper)
				bpm_wrapper.right_phase_offset.phaseOffset_avg = 0.
				bpm_wrapper.right_phase_offset.phaseOffset_err = 0.
				bpm_wrapper.right_phase_offset.isReady = false
				bpm_wrapper.right_phase_offset.phaseOffset_arr = []	
				self.bpm_amp_phase_data_dict[bpm_wrapper] = ([],[])
	
	def run(self):
		mass = self.scl_long_tuneup_controller.mass/1.0e+6			
		c_light = self.scl_long_tuneup_controller.c_light
		ring_length = 	self.scl_long_tuneup_controller.ring_length
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		if(messageTextField != null):
			messageTextField.setText("")
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		bpms_offsets_from_hebt1_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_hebt1_panel
		n_iter = int(bpms_offsets_from_hebt1_panel.iter_text.getValue())
		iter_left_text = bpms_offsets_from_hebt1_panel.iter_left_text
		ring_energy_text = bpms_offsets_from_hebt1_panel.ring_energy_text
		ring_energy_err_text = bpms_offsets_from_hebt1_panel.ring_energy_err_text
		ca_ring_freq = ChannelFactory.defaultFactory().getChannel("Ring_Diag:BCM_D09:FFT_peak2")
		scl_long_tuneup_init_controller = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller
		scl_long_tuneup_init_controller.connectAllBPMs()		
		bpmBatchReader = self.scl_long_tuneup_controller.bpmBatchReader
		beamTrigger = self.scl_long_tuneup_controller.beamTrigger
		statistic_state_controller = bpms_offsets_from_hebt1_panel.statistic_state_controller
		beamTrigger.scan_state_controller = statistic_state_controller
		statistic_state_controller.setShouldStop(false)
		ring_energy_text.setValue(0.)
		ring_energy_err_text.setValue(0.)
		eKin_ring_arr = []
		count = n_iter
		while(count > 0):
			iter_left_text.setValue(1.0*count)
			res = bpmBatchReader.makeMeasurement()
			count -= 1
			if(not res):
				if(messageTextField != null):
					messageTextField.setText("cannot measure the BPM phases! Stop.")
					if(statistic_state_controller.getShouldStop()):
						messageTextField.setText("The Statistics stopped upon user's request!")
					break
			#print "debug count=",count
			bpmBatchReader.collectStatistics(self.bpm_amp_phase_data_dict)
			ring_freq = ca_ring_freq.getValDbl()
			beta = ring_length*ring_freq/c_light
			gamma = 1./math.sqrt(1.0-beta*beta)
			eKin = mass*(gamma-1.0)
			eKin_ring_arr.append(eKin)
		n_iter = len(eKin_ring_arr)
		iter_left_text.setValue(0.)
		if(n_iter >= 1):
			(eKin_ring,eKin_ring_err) = calculateAvgErr(eKin_ring_arr)
			ring_energy_text.setValue(eKin_ring)
			ring_energy_err_text.setValue(eKin_ring_err)
			self.calculatePhaseOffsets(eKin_ring)
		statistic_state_controller.setShouldStop(true)
			
	def calculatePhaseOffsets(self,eKin_ring):
		mass = self.scl_long_tuneup_controller.mass/1.0e+6			
		c_light = self.scl_long_tuneup_controller.c_light	
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq
		beta = math.sqrt(eKin_ring*(eKin_ring+2*mass))/(eKin_ring+mass)		
		slope = 360.0*bpm_freq/(c_light*beta)
		if(len(self.bpm_wrappers_arr) < 2): return false
		base_bpm_wrapper = self.bpm_wrappers_arr[0]
		(base_bpm_phase,base_bpm_phase_err) = calculateAvgErr(self.bpm_amp_phase_data_dict[base_bpm_wrapper][1])
		for bpm_wrapper in self.bpm_wrappers_arr:
			pos = bpm_wrapper.pos - base_bpm_wrapper.pos
			(phase,phase_err) = calculateAvgErr(self.bpm_amp_phase_data_dict[bpm_wrapper][1])
			phase_diff = makePhaseNear(phase - base_bpm_phase - slope*pos,0.)
			bpm_wrapper.right_phase_offset.phaseOffset_avg = phase_diff
			bpm_wrapper.right_phase_offset.phaseOffset_err = phase_err
			bpm_wrapper.right_phase_offset.isReady = true	
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		scl_long_tuneup_bpm_offsets_controller.bpm_offsets_table.getModel().fireTableDataChanged()		
		
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
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class BPMs_Offsets_from_CCL4_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPMs Phase Offsets: start from CCL4 forward")
		self.setBorder(titled_border)	
		#--- buttons
		get_offsets_ccl1_button = JButton("Calculate Offsets from CCL4 forward")
		get_offsets_ccl1_button.addActionListener(Offsets_from_CCL4_Button_Listener(self.scl_long_tuneup_controller))	
		ccl4_energy_lbl = JLabel("<html> SCL Entrance E<SUB>kin</SUB>[MeV] = <html>",JLabel.RIGHT)
		self.ccl4_energy_text = DoubleInputTextField(185.6,FortranNumberFormat("G9.6"),6)
		self.add(get_offsets_ccl1_button)
		self.add(ccl4_energy_lbl)
		self.add(self.ccl4_energy_text)

class BPMs_Offsets_from_HEBT1_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPMs Phase Offsets: start from HEBT1 backward")
		self.setBorder(titled_border)	
		#--- stage 1 GUI elements
		stage1_lbl = JLabel("Stage 1.",JLabel.RIGHT)
		get_statistics_button = JButton("Start HEBT1 BPMs Satatistics")
		get_statistics_button.addActionListener(Get_HEBT_Statistics_Button_Listener(self.scl_long_tuneup_controller))
		stop_statistics_button = JButton("Stop")
		stop_statistics_button.addActionListener(Stop_HEBT_Statistics_Button_Listener(self.scl_long_tuneup_controller))		
		iter_lbl = JLabel("Iterations=",JLabel.RIGHT)
		self.iter_text = DoubleInputTextField(10.0,FortranNumberFormat("G2.0"),2)
		iter_left_lbl = JLabel("Count=",JLabel.RIGHT)
		self.iter_left_text = DoubleInputTextField(0.0,FortranNumberFormat("G2.0"),2)
		ring_energy_lbl = JLabel("<html>E<SUB>RING</SUB>[MeV]= <html>",JLabel.RIGHT)
		self.ring_energy_text = DoubleInputTextField(0.0,FortranNumberFormat("G9.6"),8)
		ring_energy_err_lbl = JLabel(" +- ",JLabel.RIGHT)		
		self.ring_energy_err_text = DoubleInputTextField(0.0,FortranNumberFormat("G9.3"),8)
		panel_1_1 = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		panel_1_1.setBorder(etched_border)		
		panel_1_1.add(get_statistics_button)
		panel_1_1.add(stop_statistics_button)
		panel_1_1.add(iter_lbl)
		panel_1_1.add(self.iter_text)
		panel_1_1.add(iter_left_lbl)
		panel_1_1.add(self.iter_left_text)
		panel_1_1.add(ring_energy_lbl)
		panel_1_1.add(self.ring_energy_text)
		panel_1_1.add(ring_energy_err_lbl)
		panel_1_1.add(self.ring_energy_err_text)
		get_em_phase_offsets_button = JButton("Get BPM Offsets from Energy Meter")
		get_em_phase_offsets_button.addActionListener(Get_Energy_Meter_Offsets_Button_Listener(self.scl_long_tuneup_controller))		
		panel_1_2 = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		panel_1_2.setBorder(etched_border)
		panel_1_2.add(get_em_phase_offsets_button)
		#--- stage 2 GUI elements		
		stage2_lbl = JLabel("Stage 2.",JLabel.RIGHT)
		get_offsets_hebt1_button = JButton("Calculate Offsets from HEBT1 backward")
		get_offsets_hebt1_button.addActionListener(Offsets_from_HEBT1_Button_Listener(self.scl_long_tuneup_controller))	
		init_energy_lbl = JLabel("<html> SCL Entrance E<SUB>kin</SUB>[MeV] = <html>",JLabel.RIGHT)
		self.init_energy_text = DoubleInputTextField(0.,FortranNumberFormat("G9.6"),6)
		#------ Stage 1 Panel
		stage1_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		stage1_panel.setBorder(etched_border)
		stage1_panel.add(stage1_lbl)
		stage1_panel.add(panel_1_1)
		stage1_panel.add(panel_1_2)
		#------ Stage 2 Panel
		stage2_panel = JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		stage2_panel.setBorder(etched_border)
		stage2_panel.add(stage2_lbl)
		stage2_panel.add(get_offsets_hebt1_button)
		stage2_panel.add(init_energy_lbl)
		stage2_panel.add(self.init_energy_text)
		#--------------------------------------------
		self.add(stage1_panel,BorderLayout.NORTH)
		self.add(stage2_panel,BorderLayout.SOUTH)
		#------ Statistic run controller
		self.statistic_state_controller = StatisticStateController()		

class BPMs_Offsets_from_ExtFile_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPMs Phase Offsets: read/write from/to ASCII File")
		self.setBorder(titled_border)	
		#--- buttons
		get_offsets_ccl1_button = JButton("Read BPMs Phase Offsets from ASCII")
		get_offsets_ccl1_button.addActionListener(Read_Offsets_from_File_Button_Listener(self.scl_long_tuneup_controller))
		self.add(get_offsets_ccl1_button)
		write_offsets_ccl1_button = JButton("Write BPMs Phase Offsets to ASCII file")
		write_offsets_ccl1_button.addActionListener(Write_Offsets_to_File_Button_Listener(self.scl_long_tuneup_controller))
		self.add(write_offsets_ccl1_button)		
		
class Set_BPMs_as_Bad_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)	
		#--- buttons
		sub_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		sub_panel.setBorder(etched_border)	
		final_offsets_lbl = JLabel("Use the folowing Offsets as final= ",JLabel.LEFT)
		self.ccl4_button = JRadioButton("CCL4")
		self.hebt1_button = JRadioButton("HEBT1")
		self.ccl4_button.setSelected(true)
		self.hebt1_button.setSelected(false)
		self.final_select_button_group = ButtonGroup()
		self.final_select_button_group.add(self.ccl4_button)
		self.final_select_button_group.add(self.hebt1_button)
		self.ccl4_button.addActionListener(Set_as_Final_Button_Listener(self.scl_long_tuneup_controller))
		self.hebt1_button.addActionListener(Set_as_Final_Button_Listener(self.scl_long_tuneup_controller))
		sub_panel.add(final_offsets_lbl)
		sub_panel.add(self.ccl4_button)
		sub_panel.add(self.hebt1_button)
		set_bpms_as_bad_button = JButton("Mark Selected BPMs as Bad")
		set_bpms_as_bad_button.addActionListener(Set_BPms_as_Bad_Button_Listener(self.scl_long_tuneup_controller))	
		set_bpms_as_bad_lbl = JLabel("This will mark all selected BPMs as BAD! Irreversibly!",JLabel.LEFT)
		self.add(sub_panel)
		self.add(set_bpms_as_bad_button)
		self.add(set_bpms_as_bad_lbl)

#------------------------------------------------
#  JTable models
#------------------------------------------------
class PhaseOffsets_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["BPM","Use"]
		self.columnNames += ["<html>From CCL4 &Delta;&phi;<SUB>BPM</SUB>(deg)<html>",]
		self.columnNames += ["<html>From HEBT1 &Delta;&phi;<SUB>BPM</SUB>(deg)<html>",]
		self.columnNames += ["<html>Final &Delta;&phi;<SUB>BPM</SUB>(deg)<html>",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()		
		#self.dataFormat = FortranNumberFormat("G4.1")

	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.scl_long_tuneup_controller.bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.scl_long_tuneup_controller.bpm_wrappers[row]
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return bpm_wrapper.isGood
		if(not bpm_wrapper.isGood): return "" 
		phase_offset_cntrl = null
		if(col == 2):
			phase_offset_cntrl = bpm_wrapper.left_phase_offset
		if(col == 3):
			phase_offset_cntrl = bpm_wrapper.right_phase_offset			
		if(col == 4):
			phase_offset_cntrl = bpm_wrapper.final_phase_offset
		if(phase_offset_cntrl != null and phase_offset_cntrl.isReady):
			txt = "% 5.1f"%phase_offset_cntrl.phaseOffset_avg
			txt = txt +" +- "
			txt = txt +"% 5.1f"%phase_offset_cntrl.phaseOffset_err
			return txt
		return ""
				
	def getColumnClass(self,col):	
		if(col == 1):
			return self.boolean_class		
		return self.string_class
	
	def isCellEditable(self,row,col):	
		return false
			
	def setValueAt(self, value, row, col):
		pass

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Offsets_from_CCL4_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		calculator = BPMs_Offsets_from_CCL4_Calculator(self.scl_long_tuneup_controller)
		calculator.calculateOffSets()
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		scl_long_tuneup_bpm_offsets_controller.setFinalOffsets()

class Set_as_Final_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		scl_long_tuneup_bpm_offsets_controller.setFinalOffsets()
		
class Set_BPms_as_Bad_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		bpm_offsets_table = scl_long_tuneup_bpm_offsets_controller.bpm_offsets_table
		# ?????? actions through the all cavities and bpms
		scl_long_tuneup_bpm_offsets_controller.setFinalOffsets()
		self.scl_long_tuneup_controller.getMessageTextField().setText("This button's action is not defined yet.")
	
class Get_HEBT_Statistics_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("This button's action is not defined yet.")
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		bpms_offsets_from_hebt1_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_hebt1_panel		
		statistic_state_controller = bpms_offsets_from_hebt1_panel.statistic_state_controller			
		if(statistic_state_controller.getIsRunning()): return
		statistic_runner = Statistic_Runner(self.scl_long_tuneup_controller)
		thr = Thread(statistic_runner)
		thr.start()				
		
class Stop_HEBT_Statistics_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		bpms_offsets_from_hebt1_panel = scl_long_tuneup_bpm_offsets_controller.bpms_offsets_from_hebt1_panel		
		bpms_offsets_from_hebt1_panel.statistic_state_controller.setShouldStop(true)		
		
class Get_Energy_Meter_Offsets_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_tuneup_energy_meter_controller = self.scl_long_tuneup_controller.scl_long_tuneup_energy_meter_controller
		bpm_wrappers_holder = scl_long_tuneup_energy_meter_controller.bpm_wrappers_holder
		em_bpm_wrpprs = bpm_wrappers_holder.em_bpm_wrpprs
		use_bpms = bpm_wrappers_holder.use_bpms
		bpm_phase_offsets = bpm_wrappers_holder.bpm_phase_offsets
		for bpm_ind in range(len(self.scl_long_tuneup_controller.bpm_wrappers)):
			bpm_wrapper = self.scl_long_tuneup_controller.bpm_wrappers[bpm_ind]
			bpm_wrapper.right_phase_offset.phaseOffset_err = 0.
			bpm_wrapper.right_phase_offset.isReady = false
			bpm_wrapper.right_phase_offset.phase_val_tmp = 0.
			bpm_wrapper.right_phase_offset.phase_val_err_tmp = 0.
			bpm_wrapper.right_phase_offset.phaseOffset_arr = []			
			if(bpm_wrapper.isGood):
				em_bpm_ind = -1
				if(bpm_wrapper in em_bpm_wrpprs):
					em_bpm_ind = em_bpm_wrpprs.index(bpm_wrapper) 
				if(em_bpm_ind >= 0):
					if(use_bpms[em_bpm_ind]):
						bpm_wrapper.right_phase_offset.phaseOffset_avg = bpm_phase_offsets[em_bpm_ind]
						bpm_wrapper.right_phase_offset.isReady = true
		#----- update table
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		scl_long_tuneup_bpm_offsets_controller.bpm_offsets_table.getModel().fireTableDataChanged()
			
class Offsets_from_HEBT1_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		calculator = BPMs_Offsets_from_HEBT1_Calculator(self.scl_long_tuneup_controller)
		calculator.calculateOffSets()
		scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
		scl_long_tuneup_bpm_offsets_controller.setFinalOffsets()		
	
class Read_Offsets_from_File_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])		
		fc.setDialogTitle("Read BPM Final Phase Offsets from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			fl_ph_in = open(fl_in.getPath(),"r")
			lns = fl_ph_in.readlines()
			fl_ph_in.close()
			#----- set all as "not ready"
			for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
				bpm_wrapper.final_phase_offset.isReady = false
				bpm_wrapper.final_phase_offset.phaseOffset_avg = 0.
				bpm_wrapper.final_phase_offset.phaseOffset_err = 0.
			#----- set existing in file as "ready"	
			for ln in lns:
				res_arr = ln.split()
				if(len(res_arr) == 5):
					bpm_name = res_arr[1]
					bpm_wrapper = self.scl_long_tuneup_controller.getBPM_WrapperForBPM_Id(bpm_name)
					if(bpm_wrapper != null):
						bpm_wrapper.final_phase_offset.isReady = true
						bpm_wrapper.final_phase_offset.phaseOffset_avg = float(res_arr[3])
						bpm_wrapper.final_phase_offset.phaseOffset_err = float(res_arr[4])
			#----- update table
			scl_long_tuneup_bpm_offsets_controller = self.scl_long_tuneup_controller.scl_long_tuneup_bpm_offsets_controller
			scl_long_tuneup_bpm_offsets_controller.bpm_offsets_table.getModel().fireTableDataChanged()

class Write_Offsets_to_File_Button_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])		
		fc.setDialogTitle("Save BPM Final Phase Offsets Table into ASCII file")
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
			txt = "#    BPM     pos[m]    offset[deg]   offset_err[deg]"
			fl_ph_out.write(txt+"\n")
			bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
			count = 0
			for bpm_ind in range(len(bpm_wrappers)):
				bpm_wrapper = bpm_wrappers[bpm_ind]
				if(bpm_wrapper.isGood and bpm_wrapper.final_phase_offset.isReady):
					count += 1
					txt = str(count)+" "+bpm_wrapper.bpm.getId()+ " %9.3f "%bpm_wrapper.pos 
					txt += "  %7.2f"% bpm_wrapper.final_phase_offset.phaseOffset_avg
					txt += " %7.2f"% bpm_wrapper.final_phase_offset.phaseOffset_err
					fl_ph_out.write(txt+"\n")
			#---- end of writing
			fl_ph_out.flush()
			fl_ph_out.close()		
			
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_BPM_Offsets_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top panels = offsets analysis ----------------------------
		top_panel = JPanel(BorderLayout())
		self.bpms_offsets_from_ccl4_panel = BPMs_Offsets_from_CCL4_Panel(self.scl_long_tuneup_controller)
		self.bpms_offsets_from_hebt1_panel = BPMs_Offsets_from_HEBT1_Panel(self.scl_long_tuneup_controller)
		self.bpms_offsets_from_ext_file_panel = BPMs_Offsets_from_ExtFile_Panel(self.scl_long_tuneup_controller)
		top_panel0 = JPanel(BorderLayout())
		top_panel0.add(self.bpms_offsets_from_ccl4_panel,BorderLayout.NORTH)
		top_panel0.add(self.bpms_offsets_from_hebt1_panel,BorderLayout.SOUTH)
		top_panel1 = JPanel(BorderLayout())
		top_panel1.add(top_panel0,BorderLayout.NORTH)
		top_panel1.add(self.bpms_offsets_from_ext_file_panel,BorderLayout.SOUTH)
		top_panel.add(top_panel1,BorderLayout.NORTH)
		#--------center panel = table------------
		center_panel = JPanel(BorderLayout())		
		self.bpm_offsets_table_model = PhaseOffsets_BPMs_Table_Model(self.scl_long_tuneup_controller)
		self.bpm_offsets_table = JTable(self.bpm_offsets_table_model)
		self.bpm_offsets_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.bpm_offsets_table.setFillsViewportHeight(true)	
		scrl_panel = JScrollPane(self.bpm_offsets_table)
		scrl_panel.setBorder(etched_border)
		center_panel.add(scrl_panel,BorderLayout.CENTER)
		#-------- bottom panel = post analysis ---------------
		bottom_panel = JPanel(BorderLayout())
		self.set_bpms_as_bad_panel = Set_BPMs_as_Bad_Panel(self.scl_long_tuneup_controller)
		bottom_panel.add(self.set_bpms_as_bad_panel,BorderLayout.SOUTH)
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)		
		
	def getMainPanel(self):
		return self.main_panel
		
	def setFinalOffsets(self):
		ccl4_button = self.set_bpms_as_bad_panel.ccl4_button
		ccl4_final = ccl4_button.isSelected()
		bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers
		for bpm_wrapper in bpm_wrappers:
			if(bpm_wrapper.isGood):
				if(ccl4_final):
					bpm_wrapper.final_phase_offset.phaseOffset_avg = bpm_wrapper.left_phase_offset.phaseOffset_avg
					bpm_wrapper.final_phase_offset.phaseOffset_err = bpm_wrapper.left_phase_offset.phaseOffset_err
					bpm_wrapper.final_phase_offset.isReady = bpm_wrapper.left_phase_offset.isReady 
				else:
					bpm_wrapper.final_phase_offset.phaseOffset_avg = bpm_wrapper.right_phase_offset.phaseOffset_avg
					bpm_wrapper.final_phase_offset.phaseOffset_err = bpm_wrapper.right_phase_offset.phaseOffset_err		
					bpm_wrapper.final_phase_offset.isReady = bpm_wrapper.right_phase_offset.isReady 
		self.bpm_offsets_table.getModel().fireTableDataChanged()
		
