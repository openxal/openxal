# The OpenXAL online model for a particle probe

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

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.plot import GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf.data import XMLDataManager
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory


from functions_and_classes_lib import calculateAvgErr, makePhaseNear, HarmonicsAnalyzer 


false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#    Online Model for particle tracking
#------------------------------------------------------------------------
class Particle_Tracker_Model:
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		cav_wrappers = self.main_loop_controller.cav_wrappers	
		self.accSeq = self.main_loop_controller.accSeq
		self.part_tracker = AlgorithmFactory.createParticleTracker(self.accSeq)
		self.part_tracker.setRfGapPhaseCalculation(true)
		self.part_probe_init = ProbeFactory.createParticleProbe(self.accSeq,self.part_tracker)
		self.scenario = Scenario.newScenarioFor(self.accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.resync()
		part_probe = ParticleProbe(self.part_probe_init)
		part_probe.setKineticEnergy(self.part_probe_init.getKineticEnergy())
		self.scenario.setProbe(part_probe)	
		self.scenario.run()	
		#-------------------------------------------------
		# The cavToGapsDict is needed for reference to the irfGap to set phase and do not use  scenario.resync()
		rfGaps = self.accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cavToGapsDict = {}
		self.cavEnergyInOutDict = {}		
		for cav_wrapper in cav_wrappers:
			self.cavToGapsDict[cav_wrapper] = []
			for rfGap in rfGaps:
				if(rfGap.getParent().getId() == cav_wrapper.cav.getId()):
					irfGaps = self.scenario.elementsMappedTo(rfGap)
					self.cavToGapsDict[cav_wrapper].append(irfGaps[0])
		for cav_wrapper_ind in range(len(cav_wrappers)):
			cav_wrapper = cav_wrappers[cav_wrapper_ind]
			irfGaps = self.cavToGapsDict[cav_wrapper]
			nGaps = len(irfGaps)
			state_in = self.scenario.getTrajectory().statesForElement(irfGaps[0].getId()).get(0)
			if(cav_wrapper_ind > 0):
				irfGaps_0 = self.cavToGapsDict[cav_wrappers[cav_wrapper_ind-1]]
				irfGap_0 = irfGaps_0[len(irfGaps_0)-1]
				state_in = self.scenario.getTrajectory().statesForElement(irfGap_0.getId()).get(0)
			state_out = self.scenario.getTrajectory().statesForElement(irfGaps[nGaps-1].getId()).get(0)
			#print "debug cav=",cav_wrapper.alias," eKin_in=",state_in.getKineticEnergy()/1.0e+6," eKin_out=",state_out.getKineticEnergy()/1.0e+6
			self.cavEnergyInOutDict[cav_wrapper] = (state_in.getKineticEnergy()/1.0e+6,state_out.getKineticEnergy()/1.0e+6)
		# cav_wrappers_param_dict[cav_wrapper] = (cavAmp,phase)
		self.cav_wrappers_param_dict = {}
		self.cav_amp_phase_dict = {}
		for cav_wrapper in cav_wrappers:
			amp = cav_wrapper.cav.getDfltCavAmp()
			phase = cav_wrapper.cav.getDfltCavPhase()
			self.cav_amp_phase_dict[cav_wrapper] = (amp,phase)
		#----------------------------------------------------------------
		self.active_cav_wrapper = null
		self.gap_first = null
		self.gap_last = null	
		self.gap_list = null
		
	def restoreDesignAmpPhases(self):
		cav_wrappers = self.main_loop_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			(amp,phase) = self.cav_amp_phase_dict[cav_wrapper]
			cav_wrapper.cav.updateDesignAmp(amp)
			cav_wrapper.cav.updateDesignPhase(phase)
		
	def setModelAmpPhaseToActiveCav(self,amp,phase,phase_shift):
		if(self.active_cav_wrapper != null):
			self.active_cav_wrapper.cav.updateDesignAmp(amp)
			self.active_cav_wrapper.cav.updateDesignPhase(phase-phase_shift)
		self.scenario.resync()
			
	def setModelPhase(self,phase,phase_shift):
		""" There is no need in self.scenario.resync() for right phase """
		if(self.active_cav_wrapper == null): return
		irfGaps = self.cavToGapsDict[self.active_cav_wrapper]
		irfGaps[0].setPhase((phase-phase_shift)*math.pi/180.)
		
	def trackProbe(self,eKin_in):
		""" Returns the Trajectory. The input energy is in MeV """
		part_probe = ParticleProbe(self.part_probe_init)
		part_probe.setKineticEnergy(eKin_in*1.0e+6)
		self.scenario.setProbe(part_probe)	
		self.scenario.run()	
		return 	self.scenario.getTrajectory()	
		
	def getBPM_Phases(self,eKin_in,phase,phase_shift,bpm_wrappers):
		self.setModelPhase(phase,phase_shift)
		self.trackProbe(eKin_in)
		bpm_phases_arr = []		
		if(self.active_cav_wrapper == null): return bpm_phases_arr
		first_gap = self.gap_list.get(0)
		last_gap = self.gap_list.get(self.gap_list.size()-1)
		pos_max = self.accSeq.getPosition(last_gap)
		pos_min = self.accSeq.getPosition(first_gap)
		traj = self.scenario.getTrajectory()
		ind = traj.indicesForElement(last_gap.getId())[0]	
		last_gap_tm = traj.stateWithIndex(ind).getTime()
		for bpm_wrapper in bpm_wrappers:
			phase = 0.
			if(bpm_wrapper.pos > pos_min):
				tm = 0.
				if(bpm_wrapper.pos < pos_max):
					ind = traj.indicesForElement(bpm_wrapper.bpm.getId())[0]
					state0 = traj.stateWithIndex(ind-1)
					state1 = traj.stateWithIndex(ind+1)
					tm0 = state0.getTime()
					tm1 = state1.getTime()
					pos0 = state0.getPosition()
					pos1 = state1.getPosition()
					pos = bpm_wrapper.pos - pos_min
					tm = tm0 + (tm1-tm0)*(pos-pos0)/(pos1-pos0)
				else:
					c = 2.997924e+8
					beta = traj.finalState().getBeta()
					tm = (bpm_wrapper.pos - pos_max)/(c*beta)
					tm += last_gap_tm 
				phase = bpm_wrapper.bpm.getBPMBucket().getFrequency()*1.0e+6*360.*tm
			bpm_phases_arr.append(makePhaseNear(phase,0.))					
		return bpm_phases_arr
		
	def getCavOffBPM_Phases(self,eKin_in,bpm_wrappers):
		bpm_phases_arr = []		
		if(self.active_cav_wrapper == null): return bpm_phases_arr
		first_gap = self.gap_list.get(0)
		pos_min = self.accSeq.getPosition(first_gap)
		mass = self.scenario.getProbe().getSpeciesRestEnergy()/1.0e+6
		c = 2.997924e+8
		beta = math.sqrt((eKin_in+2*mass)*eKin_in)/(eKin_in+mass)		
		for bpm_wrapper in bpm_wrappers:
			phase = 0.
			if(bpm_wrapper.pos > pos_min):
				tm = (bpm_wrapper.pos - pos_min)/(c*beta) 
				phase = bpm_wrapper.bpm.getBPMBucket().getFrequency()*1.0e+6*360.*tm
			bpm_phases_arr.append(makePhaseNear(phase,0.))					
		return bpm_phases_arr	
		
	def calculateEkin(self,bpm_scan_data0,bpm_scan_data1):
		if(self.active_cav_wrapper == null): (false,0.)
		cav_wrapper = self.active_cav_wrapper
		(phase0,err0) = bpm_scan_data0.getAvgPhaseAndErr()
		(phase1,err1) = bpm_scan_data1.getAvgPhaseAndErr()
		pos0 = bpm_scan_data0.bpm_wrapper.pos
		pos1 = bpm_scan_data1.bpm_wrapper.pos
		eKinIn_guess = cav_wrapper.Ekin_in_design
		bpm_freq0 = bpm_scan_data0.bpm_wrapper.bpm.getBPMBucket().getFrequency()*1.0e+6
		bpm_freq1 = bpm_scan_data1.bpm_wrapper.bpm.getBPMBucket().getFrequency()*1.0e+6		
		mass = self.scenario.getProbe().getSpeciesRestEnergy()/1.0e+6
		c = 2.997924e+8
		beta = math.sqrt((eKinIn_guess+2*mass)*eKinIn_guess)/(eKinIn_guess+mass)
		if(math.fabs(bpm_freq0 - bpm_freq1) > 1.0e+6):
			txt = "Cav. off analysis: BPMs have different frequences!"
			txt += " BPM0=" + bpm_scan_data0.bpm_wrapper.alias
			txt += " BPM1=" + bpm_scan_data1.bpm_wrapper.alias
			self.main_loop_controller.getMessageTextField().setText(txt)
			return (false,0.)
		bpm_freq = bpm_freq0
		delta_phase_guess = 360.0*(pos1 - pos0)*bpm_freq/(beta*c)
		delta_phase = makePhaseNear(phase1 - phase0,delta_phase_guess)
		beta_res = 360.0*(pos1 - pos0)*bpm_freq/(c*delta_phase)
		if(beta_res <= 0. or beta_res >= 1.):
			txt = "Cav. off analysis: data does not make sense!"
			txt += " BPM0=" + bpm_scan_data0.bpm_wrapper.alias
			txt += " BPM1=" + bpm_scan_data1.bpm_wrapper.alias
			self.main_loop_controller.getMessageTextField().setText(txt)
			return (false,0.)
		gamma = 1.0/math.sqrt(1.0-beta_res**2)
		eKinIn = mass*(gamma - 1.)
		return (true,eKinIn)
			
	def getEkin_Out(self):
		return self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
			
	def setActiveCavity(self,cav_wrapper):
		self.restoreDesignAmpPhases()
		cav_wrappers = self.main_loop_controller.cav_wrappers
		self.active_cav_wrapper = cav_wrapper
		if(self.active_cav_wrapper != null):
			for cav_wrapper in cav_wrappers:
				if(cav_wrapper.pos > self.active_cav_wrapper.pos):
					cav_wrapper.cav.updateDesignAmp(0.)
			self.gap_list = self.active_cav_wrapper.cav.getGapsAsList()
			self.gap_first = self.gap_list.get(0)
			self.gap_last = self.gap_list.get(self.gap_list.size()-1)
			self.scenario.setStartNode(self.gap_first.getId())
			self.scenario.setStopNode(self.gap_last.getId())
		else:
			self.scenario.unsetStartNode()
			self.scenario.unsetStopNode()
			self.gap_first = null
			self.gap_last = null	
			self.gap_list = null

#------------------------------------------------------------------------
#    Online Model for Envelop tracking
#------------------------------------------------------------------------
class Envelop_Tracker_Model:
	def __init__(self,main_loop_controller):
		self.main_loop_controller = main_loop_controller
		cav_wrappers = self.main_loop_controller.cav_wrappers	
		self.accSeq = self.main_loop_controller.accSeq
		self.env_tracker = AlgorithmFactory.createEnvTrackerAdapt(self.accSeq)
		self.env_tracker.setRfGapPhaseCalculation(true)
		self.env_probe = ProbeFactory.getEnvelopeProbe(self.accSeq,self.env_tracker)
		self.scenario = Scenario.newScenarioFor(self.accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.resync()
		self.scenario.setProbe(self.env_probe)	
		self.scenario.run()	
		#-------------------------------------------------	
		for cav_wrapper_ind in range(len(cav_wrappers)):
			cav_wrapper = cav_wrappers[cav_wrapper_ind]
			gap_list = cav_wrapper.cav.getGapsAsList()
			gap_first = gap_list.get(0)
			gap_last = gap_list.get(gap_list.size()-1)
			ind0 = self.scenario.getTrajectory().indicesForElement(gap_first.getId())[0] - 1
			ind1 = self.scenario.getTrajectory().indicesForElement(gap_last.getId())[0]
			state_in = self.scenario.getTrajectory().stateWithIndex(ind0)
			state_out = self.scenario.getTrajectory().stateWithIndex(ind1)
			Ekin_in = state_in.getKineticEnergy()/1.0e+6
			Ekin_out = state_out.getKineticEnergy()/1.0e+6
			twiss_in = state_in.twissParameters()[2]
			twiss_out = state_out.twissParameters()[2]
			z_prim_in = math.sqrt(twiss_in.getGamma()*twiss_in.getEmittance())
			z_prim_out = math.sqrt(twiss_out.getGamma()*twiss_out.getEmittance())
			beta_gamma_in = state_in.getBeta()*state_in.getGamma()
			beta_gamma_out = state_out.getBeta()*state_out.getGamma()
			mass = state_in.getSpeciesRestEnergy()/1.0e+6
			delta_Ekin_in = (mass+Ekin_in)*beta_gamma_in**2*z_prim_in
			delta_Ekin_out = (mass+Ekin_out)*beta_gamma_out**2*z_prim_out
			cav_wrapper.Ekin_in = Ekin_in            
			cav_wrapper.Ekin_out = Ekin_out
			cav_wrapper.Ekin_in_design = Ekin_in
			cav_wrapper.Ekin_in_delta_design = delta_Ekin_in
			cav_wrapper.Ekin_out_design = Ekin_out
			cav_wrapper.Ekin_out_delta_design = delta_Ekin_out 
		for cav_wrapper_ind in range(len(cav_wrappers)-1):
			cav_wrappers[cav_wrapper_ind].Ekin_out_delta_design = cav_wrappers[cav_wrapper_ind+1].Ekin_in_delta_design
			

