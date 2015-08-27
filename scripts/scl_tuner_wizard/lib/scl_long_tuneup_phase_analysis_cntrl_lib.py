# The SCL Longitudinal Tune-Up Cavities' Phase Scan Analysis controller
# It will analyze the cavities phase scans data 

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing import JTable
from java.util import ArrayList
from java.util import Calendar
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
from xal.smf.impl import Marker, Quadrupole, RfGap, SCLCavity
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.smf.data import XMLDataManager
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.smf.proxy import RfGapPropertyAccessor
from xal.tools.xml import XmlDataAdaptor
from xal.extension.solver import Scorer
from xal.extension.solver import Trial
from xal.extension.solver import Variable
from xal.extension.solver import Stopper
from xal.extension.solver import SolveStopperFactory
from xal.extension.solver import ProblemFactory
from xal.extension.solver import Solver
from xal.extension.solver import Problem
from xal.extension.solver.algorithm import SimplexSearchAlgorithm
from xal.extension.solver.hint import Hint
from xal.extension.solver.hint import InitialDelta

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear
from harmonics_fitter_lib import makePhaseNear180

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#    Online Model for phase analysis
#------------------------------------------------------------------------
class SCL_One_Cavity_Tracker_Model:
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		self.part_tracker = AlgorithmFactory.createParticleTracker(self.scl_accSeq)
		self.part_tracker.setRfGapPhaseCalculation(true)
		self.part_probe_init = ProbeFactory.createParticleProbe(self.scl_accSeq,self.part_tracker)
		self.scenario = Scenario.newScenarioFor(self.scl_accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.resync()
		# in the dictionary we will have 
		# cav_wrappers_param_dict[cav_wrapper] = [cavAmp,phase,[[gapLattElem,E0,ETL],...]]
		# E0 and ETL are parameters for all RF gaps
		self.cav_wrappers_param_dict = {}
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		self.cav_amp_phase_dict = {}
		for cav_wrapper in cav_wrappers:
			amp = cav_wrapper.cav.getDfltCavAmp()
			phase = cav_wrapper.cav.getDfltCavPhase()
			self.cav_amp_phase_dict[cav_wrapper] = (amp,phase)
		#------ Make rf gap arrays for each cavity. 
		#------ The elements are IdealRfGap instances not AcceleratorNode. 
		#------ self.cavToGapsDict has {cav_name:[irfGaps]}
		rfGaps = self.scl_accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cavToGapsDict = {}		
		for cav_wrapper in cav_wrappers:
			self.cavToGapsDict[cav_wrapper] = []
			for rfGap in rfGaps:
				if(rfGap.getId().find(cav_wrapper.cav.getId()) >= 0):
					irfGaps = self.scenario.elementsMappedTo(rfGap)
					self.cavToGapsDict[cav_wrapper].append(irfGaps[0])				
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_PHASE,phase)
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_ETL,val)
		#self.scenario.setModelInput(self.gap_first,RfGapPropertyAccessor.PROPERTY_E0,val)
		#self.scenario.setModelInput(quad,ElectromagnetPropertyAccessor,PROPERTY_FIELD,val)		
		#----------------------------------------------------------------
		self.scan_gd = BasicGraphData()
		self.harmonicsAnalyzer = HarmonicsAnalyzer(2)
		self.eKin_in = 185.6
		self.cav_amp = 14.0
		self.cav_phase_shift = 0.
		#------------------------
		self.active_cav_wrapper = null
		self.solver = null
		
	def restoreInitAmpPhases(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			(amp,phase) = self.cav_amp_phase_dict[cav_wrapper]
			self.active_cav_wrapper.cav.updateDesignAmp(amp)
			self.active_cav_wrapper.cav.updateDesignPhase(phase)
		self.setActiveCavity(null)
		
	def getEkinAmpPhaseShift(self):
		return (self.eKin_in,self.cav_amp,self.cav_phase_shift)
		
	def setModelAmpPhaseToActiveCav(self,amp,phase,phase_shift):
		if(self.active_cav_wrapper != null):
			self.active_cav_wrapper.cav.updateDesignAmp(amp)
			self.active_cav_wrapper.cav.updateDesignPhase(phase-phase_shift)
			
	def getAvgGapPhase(self):
		#------------- calculate avg. RF gap phase -----------
		if(self.active_cav_wrapper == null): return 0.
		rf_gap_arr = self.cavToGapsDict[self.active_cav_wrapper]
		phase_rf_gaps_avg = 0.
		for irfGap in rf_gap_arr:
			phase_rf_gaps_avg += makePhaseNear(irfGap.getPhase(),0.)
		phase_rf_gaps_avg /= len(rf_gap_arr)
		phase_rf_gaps_avg = makePhaseNear((phase_rf_gaps_avg*180./math.pi)%360.,0.)
		return phase_rf_gaps_avg
			
	def getModelEnergyOut(self,eKin_in,amp,phase,phase_shift):
		if(self.active_cav_wrapper == null): return 0.
		self.setModelAmpPhaseToActiveCav(amp,phase,phase_shift)
		part_probe = ParticleProbe(self.part_probe_init)
		part_probe.setKineticEnergy(eKin_in*1.0e+6)
		self.scenario.setProbe(part_probe)	
		self.scenario.resync()
		self.scenario.run()
		return self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
			
	def fillOutEneregyVsPhase(self,eKin_in,amp,phase_shift,phase_arr):
		self.scan_gd.removeAllPoints()
		if(self.active_cav_wrapper == null): return
		self.active_cav_wrapper.cav.updateDesignAmp(amp)
		self.scenario.resync()
		irfGap = self.cavToGapsDict[self.active_cav_wrapper][0]	
		for phase in phase_arr:
			part_probe = ParticleProbe(self.part_probe_init)
			part_probe.setKineticEnergy(eKin_in*1.0e+6)
			self.scenario.setProbe(part_probe)		
			#self.active_cav_wrapper.cav.updateDesignPhase(phase-phase_shift)
			#self.scenario.resync()
			irfGap.setPhase((phase-phase_shift)*math.pi/180.)
			self.scenario.run()
			eKin_out = self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
			self.scan_gd.addPoint(phase,eKin_out)
		return self.scan_gd
			
	def getDiff2(self,eKin_in,amp,phase_shift):
		if(self.active_cav_wrapper == null): return 0.
		scan_gdExp = self.active_cav_wrapper.eKinOutPlot
		n_points = scan_gdExp.getNumbOfPoints()
		if(n_points <= 0): return 0.
		phase_arr = []
		for ip in range(n_points):
			phase_arr.append(scan_gdExp.getX(ip))
		scan_gd = self.fillOutEneregyVsPhase(eKin_in,amp,phase_shift,phase_arr)
		diff2 = 0.
		for ip in range(n_points):
			diff2 += (scan_gd.getY(ip) - scan_gdExp.getY(ip))**2
		diff2 /= n_points
		return diff2
			
	def setActiveCavity(self,cav_wrapper):
		self.active_cav_wrapper = cav_wrapper
		if(cav_wrapper != null):
			self.gap_list = cav_wrapper.cav.getGapsAsList()
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
			
	def harmonicsAnalysisStep(self):
		if(self.active_cav_wrapper == null): return
		self.eKin_in = self.active_cav_wrapper.eKin_in
		self.cav_amp = 14.0
		self.cav_phase_shift = 0.
		#--------- first iteration
		self.getDiff2(self.eKin_in,self.cav_amp,self.cav_phase_shift)
		err = self.harmonicsAnalyzer.analyzeData(self.scan_gd)	
		harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		energy_amp_test = harm_function.getParamArr()[1]
		energy_amp_exp = self.active_cav_wrapper.energy_guess_harm_funcion.getParamArr()[1]
		self.cav_amp = self.cav_amp*energy_amp_exp/energy_amp_test
		#--------- second iteration	
		self.getDiff2(self.eKin_in,self.cav_amp,self.cav_phase_shift)
		err = self.harmonicsAnalyzer.analyzeData(self.scan_gd)	
		harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		energy_amp_test = harm_function.getParamArr()[1]
		energy_amp_exp = self.active_cav_wrapper.energy_guess_harm_funcion.getParamArr()[1]
		self.cav_amp = self.cav_amp*energy_amp_exp/energy_amp_test
		max_model_energy_phase = self.harmonicsAnalyzer.getPositionOfMax()
		max_exp_energy_phase = self.active_cav_wrapper.energy_guess_harm_funcion.findMax()
		self.cav_phase_shift = makePhaseNear(-(max_model_energy_phase - max_exp_energy_phase),0.)
		#print "debug model max=",max_model_energy_phase," exp=",max_exp_energy_phase," shift=",self.cav_phase_shift," amp=",self.cav_amp
		
	def fit(self):
		if(self.active_cav_wrapper == null): return
		variables = ArrayList()
		delta_hint = InitialDelta()
		#----- variable eKin_in
		var = Variable("eKin_in",self.eKin_in, - Double.MAX_VALUE, Double.MAX_VALUE)
		variables.add(var)
		delta_hint.addInitialDelta(var,0.3)
		#----- variable cavity amplitude
		var = Variable("cav_amp",self.cav_amp, - Double.MAX_VALUE, Double.MAX_VALUE)
		variables.add(var)
		delta_hint.addInitialDelta(var,self.cav_amp*0.01)
		#----- variable cavity phase offset
		var = Variable("phase_offset",self.cav_phase_shift, - Double.MAX_VALUE, Double.MAX_VALUE)
		variables.add(var)
		delta_hint.addInitialDelta(var,1.0)
		#-------- solve the fitting problem
		scorer = CavAmpPhaseScorer(self,variables)
		maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(120) 
		self.solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)
		problem = ProblemFactory.getInverseSquareMinimizerProblem(variables,scorer,0.0001)
		problem.addHint(delta_hint)
		self.solver.solve(problem)
		#------- get results
		trial = self.solver.getScoreBoard().getBestSolution()
		err2 = scorer.score(trial,variables)	
		[self.eKin_in,self.cav_amp,self.cav_phase_shift] = scorer	.getTrialParams(trial)	
		self.active_cav_wrapper.eKin_in = self.eKin_in
		self.active_cav_wrapper.designPhase = makePhaseNear(self.active_cav_wrapper.livePhase - self.cav_phase_shift,0.)
		self.active_cav_wrapper.eKin_err = math.sqrt(err2)
		cav_phase = self.active_cav_wrapper.livePhase
		self.active_cav_wrapper.eKin_out = self.getModelEnergyOut(self.eKin_in,self.cav_amp,cav_phase,self.cav_phase_shift)
		#print "debug cav=",self.active_cav_wrapper.alias," shift=",self.cav_phase_shift," amp=",self.cav_amp," err2=",	math.sqrt(err2)," ekinOut=",	self.active_cav_wrapper.eKin_out		
		#----- this defenition of the avg. gap phase will be replaced by another with self.model_eKin_in
		self.active_cav_wrapper.avg_gap_phase = self.getAvgGapPhase()
		self.active_cav_wrapper.designAmp = self.cav_amp
		self.solver = null
		#----make theory graph plot
		x_arr = []
		y_arr = []
		for i in range(self.scan_gd.getNumbOfPoints()):
			phase = self.scan_gd.getX(i)
			y = self.scan_gd.getY(i)
			x_arr.append(phase)
			y_arr.append(y)
		self.active_cav_wrapper.eKinOutPlotTh.addPoint(x_arr,y_arr)			
		
	def stopFitting(self):
		if(self.solver != null):
			self.solver.stopSolving()

class CavAmpPhaseScorer(Scorer):
	""" 
	Calculate the difference between model and measured points.
	variables is Java's ArrayList() with Variable istances from the solver package.
	Parameters are eKin_in, amp. of cavity, and cavity phase shift  
	"""
	def __init__(self, scl_one_cavity_tracker_model, variables):
		self.tracker_model = scl_one_cavity_tracker_model
		self.variables = variables
		self.param_arr = [0.]*self.variables.size()
		
	def score(self,trial,variables_in):	
		self.getTrialParams(trial)
		return self.getDiff2()
		
	def getDiff2(self):
		#-----calculate diff==========================
		return self.tracker_model.getDiff2(self.param_arr[0],self.param_arr[1],self.param_arr[2])
		
	def getTrialParams(self,trial):
		#------set up harmonics function parameters from Trial map
		var_map = trial.getTrialPoint().getValueMap()
		self.param_arr[0] = math.fabs(trial.getTrialPoint().getValue(self.variables.get(0)))
		self.param_arr[1] = math.fabs(trial.getTrialPoint().getValue(self.variables.get(1)))	
		self.param_arr[2] = makePhaseNear(trial.getTrialPoint().getValue(self.variables.get(2)),0.)	
		return self.param_arr

#------------------------------------------------------------------------
#           Auxiliary Phase Analysis functions
#------------------------------------------------------------------------	
def makeEnergyGuessHarmFunc(eKin_in,cav_wrapper,scl_long_tuneup_controller):
	#This function will find the approximate parameters of 
	#     the energy_guess_harm_funcion in cav_wrapper
	#It will return false if the phase_scan_harm_funcion is not ready 
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
	if(len(param_arr) < 4): return false
	dE = param_arr[1]/coeff
	param_arr[0] = eKin_in
	param_arr[1] /= -coeff
	param_arr[3] /= -coeff
	cav_wrapper.energy_guess_harm_funcion.setParamArr(param_arr)
	cav_wrapper.eKin_out_guess = cav_wrapper.energy_guess_harm_funcion.getValue(cav_wrapper.livePhase)
	#print "debug energy guess cav=",cav_wrapper.alias," dE[MeV]=",dE," eKin_in =",eKin_in," eKin_out=",cav_wrapper.eKin_out_guess
	return true

def calculateEneregyVsPhase(cav_wrapper,scl_long_tuneup_controller,bpm_wrappers_good_arr):
	# This function will calculate output energy vs. cav. phase by using known BPM offsets
	# It will return false if the cavity cannot be analysed 
	eKin_in = cav_wrapper.eKin_in	
	# make cav_wrapper.energy_guess_harm_funcion harmonic function
	res = makeEnergyGuessHarmFunc(eKin_in,cav_wrapper,scl_long_tuneup_controller)
	if(not res): return false
	mass = scl_long_tuneup_controller.mass/1.0e+6			
	c_light = scl_long_tuneup_controller.c_light	
	bpm_freq = scl_long_tuneup_controller.bpm_freq
	coeff_init = 360.0*bpm_freq/c_light
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
		base_bpm_offset = base_bpm_wrapper.final_phase_offset.phaseOffset_avg	
		base_bpm_phase = makePhaseNear180(graphDataPhase.getY(ip) - base_bpm_offset,0.)
		gd.addPoint(base_bpm_wrapper.pos,base_bpm_phase)
		#print "debug ==== ip=",ip," cav_phase=",cav_phase," eKin_guess=",ekin_guess	
		for bpm_ind in range(1,len(bpm_wrappers_good_arr)):
			bpm_wrapper = bpm_wrappers_good_arr[bpm_ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			bpm_phase = graphDataPhase.getY(ip) - bpm_wrapper.final_phase_offset.phaseOffset_avg
			delta_pos = bpm_wrapper.pos - gd.getX(bpm_ind-1)
			bpm_phase_guess = gd.getY(bpm_ind-1) + coeff*delta_pos
			bpm_phase = makePhaseNear180(bpm_phase,bpm_phase_guess)
			gd.addPoint(bpm_wrapper.pos,bpm_phase)
			#print "debug bpm=",bpm_wrapper.alias," pos=",bpm_pos," phase=",bpm_phase
		res_arr = GraphDataOperations.polynomialFit(gd,-1.e+36,+1.e+36,1)		
		if(res_arr == null): return false
		slope = res_arr[0][1]
		init_phase = res_arr[0][0]
		bad_point_ind = 1
		bad_points_count = 0
		while(bad_point_ind >= 0):
			bad_point_ind = -1
			avg_err2 = 0.
			for index in range(gd.getNumbOfPoints()):
				avg_err2 += (gd.getY(index) - (init_phase + slope*gd.getX(index)))**2
			if(gd.getNumbOfPoints() > 1): avg_err2 /= gd.getNumbOfPoints()
			avg_err = math.sqrt(avg_err2)
			for index in range(gd.getNumbOfPoints()):
				diff = gd.getY(index) - (init_phase + slope*gd.getX(index))
				if(math.fabs(diff) > 3.0*avg_err):
					bad_point_ind = index
					break
			if(bad_point_ind >= 0):
				bad_points_count += 1
				gd.removePoint(bad_point_ind)
				res_arr = GraphDataOperations.polynomialFit(gd,-1.e+36,+1.e+36,1)		
				if(res_arr == null): return false	
				slope = res_arr[0][1]
				init_phase = res_arr[0][0]	
			if(bad_points_count > 4):
				return false
		slope_err = res_arr[1][1]	
		init_phase_err = res_arr[1][0]			
		beta = coeff_init/slope
		gamma = 1./math.sqrt(1.0-beta*beta)
		eKin = mass*(gamma-1.0)
		delta_eKin = mass*gamma**3*beta**3*slope_err/coeff_init
		cav_wrapper.eKinOutPlot.addPoint(cav_phase,eKin,delta_eKin)
		"""
		print "debug ==================== cav_phase=",cav_phase," eKin_out=",eKin," dE=",delta_eKin," ekin_guess=",ekin_guess
		for ip0 in range(gd.getNumbOfPoints()):
			print "debug bpm_ind=",ip0," pos=",gd.getX(ip0)," Y=",gd.getY(ip0)," delta=",(gd.getY(ip0)-(res_arr[0][1]*gd.getX(ip0)+init_phase))
		"""
	return true

#------------------------------------------------------------------------
#           Auxiliary Phase Analysis classes
#------------------------------------------------------------------------	
class AnalysisStateController:
	# this class is used to stop the analysis and to inform that it is running
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
		
class PhaseAnalysis_Runner(Runnable):
	# main thread runner for the anlysis
	def __init__(self,scl_long_tuneup_controller, run_to_end = true):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.run_to_end = run_to_end
		self.harmonicsAnalyzer = HarmonicsAnalyzer(2)
	
	def run(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers	
		# this is a guess energy here
		cav_wrappers[0].eKin_in = 185.6
		n_cavs = len(cav_wrappers)
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		if(messageTextField != null):
			messageTextField.setText("")	
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		scl_one_cavity_tracker_model = scl_long_tuneup_phase_analysis_controller.scl_one_cavity_tracker_model
		analysis_state_controller = scl_long_tuneup_phase_analysis_controller.analysis_state_controller
		analysis_state_controller.setIsRunning(true)
		cavs_table = scl_long_tuneup_phase_analysis_controller.cavs_table
		analysis_status_text = scl_long_tuneup_phase_analysis_controller.start_stop_analysis_panel.analysis_status_text
		#---------start deal with selected cavities
		cav_selected_inds = cavs_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0): 
			if(messageTextField != null):
				messageTextField.setText("Select one or more cavities to start Phase Analysis!")
				analysis_state_controller.setIsRunning(false)
			return
		# these are the table model indexes
		cav_wrapper = null
		start_ind = cav_selected_inds[0]
		last_ind = cav_selected_inds[len(cav_selected_inds)-1]
		if(self.run_to_end): last_ind = n_cavs - 1
		#----- set isAnalyzed=false for all downstream cavities
		cav_wrapper = cav_wrappers[start_ind]
		cav_wrapper.isAnalyzed = false
		for cav_table_ind in range(start_ind+1,n_cavs):
			cav_wrapper = cav_wrappers[cav_table_ind]
			cav_wrapper.isAnalyzed = false
			cav_wrapper.eKin_err = 0.
			cav_wrapper.model_eKin_out = 0.
			cav_wrapper.bpm_eKin_out = 0.
			cav_wrapper.real_scanPhaseShift = 0.
			cav_wrapper.eKinOutPlot.removeAllPoints()
			cav_wrapper.eKinOutPlotTh.removeAllPoints()
		cavs_table.getModel().fireTableDataChanged()
		#-------start loop over cavities in the table
		time_start = time.time()
		n_total = last_ind - start_ind + 1
		n_count = 0
		for cav_table_ind in range(start_ind,last_ind+1):
			#print "debug cav_table_index=",cav_table_ind
			cav_wrapper = cav_wrappers[cav_table_ind]
			if(cav_wrapper.eKin_in == 0.):
				if(messageTextField != null):
					messageTextField.setText("Cavity "+cav_wrapper.alias+" has 0. inpit energy. Fix it!")
					analysis_state_controller.setIsRunning(false)
				return					
			if(not cav_wrapper.isGood):
				cav_wrapper.eKin_out = cav_wrapper.eKin_in
				cav_wrapper.model_eKin_out = cav_wrapper.eKin_out
				if(cav_table_ind != 0):
					cav_wrapper.model_eKin_out = cav_wrappers[cav_table_ind-1].model_eKin_out
				#-----set the eKin_in  for the next cavity
				if(cav_table_ind != len(cav_wrappers)-1):
					cav_wrappers[cav_table_ind+1].eKin_in = cav_wrapper.eKin_out						
				continue
			if(not cav_wrapper.isMeasured):
				if(messageTextField != null):
					messageTextField.setText("Cavity "+cav_wrapper.alias+" does not have measured data! Stop analysis.")
					analysis_state_controller.setIsRunning(false)
				return				
			cavs_table.setRowSelectionInterval(cav_table_ind,cav_table_ind)
			txt = "Analysis is running! Cavity="+cav_wrapper.alias
			if(start_ind != last_ind): txt = txt + " to Cavity="+cav_wrappers[last_ind-1].alias
			if(n_count > 1):
				run_time = time.time() - time_start
				run_time_sec = int(run_time % 60)
				run_time_min = int(run_time/60.)
				eta_time = ((run_time/n_count)*(n_total - n_count))
				eta_time_sec = int(eta_time % 60)
				eta_time_min = int(eta_time/60.)
				txt = txt + "  ETA= %3d min  %2d sec "%(eta_time_min,eta_time_sec)
				txt = txt + "  Elapse= %3d min  %2d sec "%(run_time_min,run_time_sec)
			analysis_status_text.setText(txt)
			if(analysis_state_controller.getShouldStop()): break
			#calculate the out energy vs. cav. phase
			bpm_wrappers_good_arr = []
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				# make array of BPMs already having offsets and good for phase analysis
				is_wanted = bpm_wrapper.isGood and bpm_wrapper.pos > cav_wrapper.pos
				is_wanted = is_wanted and cav_wrapper.bpm_wrappers_useInPhaseAnalysis[bpm_ind]
				if(is_wanted):
					if(bpm_wrapper.final_phase_offset.isReady):
						bpm_wrappers_good_arr.append(bpm_wrapper)
			#print "debug start E(rf phase) calculation"
			if(len(bpm_wrappers_good_arr) < 3):
				if(messageTextField != null):
					n_good_bpms = len(bpm_wrappers_good_arr)
					messageTextField.setText("Cavity "+cav_wrapper.alias+" does not enough BPMs with offsets! Stop analysis. N BPMs="+str(n_good_bpms))
					analysis_state_controller.setIsRunning(false)
				return						
			calculateEneregyVsPhase(cav_wrapper,self.scl_long_tuneup_controller,bpm_wrappers_good_arr)
			#print "debug stop E(rf phase) calculation"
			if(analysis_state_controller.getShouldStop()): break
			#fit the harmonics function to the energy gain
			cav_wrapper.eKin_err = self.harmonicsAnalyzer.analyzeData(cav_wrapper.eKinOutPlot)
			harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
			#-----check fitting quality - remove bad points
			bad_point_ind = 0
			bad_points_count = 0
			while(bad_point_ind >= 0):
				bad_point_ind = -1
				for ibp in range(cav_wrapper.eKinOutPlot.getNumbOfPoints()):
					exp_val = cav_wrapper.eKinOutPlot.getY(ibp)
					harm_val = harm_function.getValue(cav_wrapper.eKinOutPlot.getX(ibp))
					diff = math.fabs(exp_val-harm_val)
					if(math.fabs(exp_val-harm_val) > 2.8*cav_wrapper.eKin_err):
						bad_point_ind = ibp
						break
				if(bad_point_ind >= 0):
					bad_points_count += 1 
					cav_wrapper.eKinOutPlot.removePoint(bad_point_ind)
					cav_wrapper.eKin_err = self.harmonicsAnalyzer.analyzeData(cav_wrapper.eKinOutPlot)
					harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
				if(bad_points_count > int(0.2*cav_wrapper.eKinOutPlot.getNumbOfPoints())):
					analysis_status_text.setText("Analysis stopped! Bad scan! Cav="+cav_wrapper.alias)
					return
			cav_wrapper.bpm_eKin_out = harm_function.getValue(cav_wrapper.livePhase)
			cav_wrapper.real_scanPhaseShift =  makePhaseNear(cav_wrapper.livePhase - harm_function.findMax(),0.)
			cav_wrapper.energy_guess_harm_funcion.setParamArr(harm_function.getParamArr())
			if(analysis_state_controller.getShouldStop()): break
			# put model based analysis here
			scl_one_cavity_tracker_model.setActiveCavity(cav_wrapper)
			scl_one_cavity_tracker_model.harmonicsAnalysisStep()
			scl_one_cavity_tracker_model.fit()
			model_eKin_in = cav_wrapper.eKin_in
			if(cav_table_ind != 0):
				model_eKin_in = cav_wrappers[cav_table_ind-1].model_eKin_out
			cav_amp = scl_one_cavity_tracker_model.cav_amp
			cav_phase = cav_wrapper.livePhase
			cav_phase_shift = scl_one_cavity_tracker_model.cav_phase_shift
			cav_wrapper.model_eKin_out = scl_one_cavity_tracker_model.getModelEnergyOut(model_eKin_in,cav_amp,cav_phase,cav_phase_shift)
			cav_wrapper.avg_gap_phase = scl_one_cavity_tracker_model.getAvgGapPhase()
			if(analysis_state_controller.getShouldStop()): break
			cav_wrapper.isAnalyzed = true
			cavs_table.getModel().fireTableDataChanged()
			#-----set the eKin_in  for the next cavity
			if(cav_table_ind != len(cav_wrappers)-1):
				cav_wrappers[cav_table_ind+1].eKin_in = cav_wrapper.eKin_out			
			n_count += 1
		#------end of cavities loop
		txt = ""
		if(cav_wrapper != null):
			txt = " The last cavity was "+cav_wrapper.alias+" ."
		run_time = time.time() - time_start
		run_time_sec = int(run_time % 60)
		run_time_min = int(run_time/60.)
		txt = txt + "  Total time= %3d min  %2d sec "%(run_time_min,run_time_sec)
		if(analysis_state_controller.getShouldStop()):
			analysis_status_text.setText("Analysis was interrupted!"+txt)
		else:
			analysis_status_text.setText("Analysis finished!"+txt)
		analysis_state_controller.setIsRunning(false)
		scl_one_cavity_tracker_model.restoreInitAmpPhases()
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class Cav_Energy_Out_Graph_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(GridLayout(1,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.gp_energy_plot = FunctionGraphsJPanel()
		self.gp_energy_plot.setLegendButtonVisible(true)
		self.gp_energy_plot.setChooseModeButtonVisible(true)
		self.gp_energy_plot.setName("Output Energy vs. Cavity Phase: ")
		self.gp_energy_plot.setAxisNames("Cav Phase, [deg]","Ekin Out, [MeV]")	
		self.gp_energy_plot.setBorder(etched_border)
		self.add(self.gp_energy_plot)
		
	def removeAllGraphData(self):
		self.gp_energy_plot.removeAllGraphData()
			
	def updateGraphData(self):
		self.gp_energy_plot.removeAllGraphData()
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		cavs_table = scl_long_tuneup_phase_analysis_controller.cavs_table
		cav_selected_inds = cavs_table.getSelectedRows()
		if(len(cav_selected_inds) == 0 or cav_selected_inds[0] < 0):
			return
		for ind in cav_selected_inds:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[ind]
			if(cav_wrapper == null or not cav_wrapper.isGood): 
				continue
			self.gp_energy_plot.addGraphData(cav_wrapper.eKinOutPlot)
			self.gp_energy_plot.addGraphData(cav_wrapper.eKinOutPlotTh)
		
class StartStopPhaseAnalysis_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		start_analysis_button = JButton("Start Analysis")
		start_analysis_button.addActionListener(Start_Phase_Analysis_Button_Listener(self.scl_long_tuneup_controller))
		start_scan_for_selection_button = JButton("Start for Selected Cavs.")
		start_scan_for_selection_button.addActionListener(Start_Phase_Analysis_Select_Cavs_Button_Listener(self.scl_long_tuneup_controller))
		stop_analysis_button = JButton("Stop Analysis")
		stop_analysis_button.addActionListener(Stop_Phase_Analysis_Button_Listener(self.scl_long_tuneup_controller))
		buttons_panel.add(start_analysis_button)
		buttons_panel.add(start_scan_for_selection_button)
		buttons_panel.add(stop_analysis_button)
		self.analysis_status_text = JTextField()
		self.analysis_status_text.setText("Analysis status")
		self.analysis_status_text.setHorizontalAlignment(JTextField.LEFT)
		self.analysis_status_text.setForeground(Color.red)
		self.add(buttons_panel,BorderLayout.WEST)
		self.add(self.analysis_status_text,BorderLayout.CENTER)
		
class PostPhaseAnalysisActions_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(FlowLayout(FlowLayout.LEFT,3,3))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		post_analysis_panel = JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		titled_border = BorderFactory.createTitledBorder(etched_border,"Post Analysis Actions")
		post_analysis_panel.setBorder(titled_border)
		make_xal_acc_file_button = JButton("Make SCL.xdxf XAL Accelerator file")
		make_xal_acc_file_button.addActionListener(Make_XAL_Acc_File_Button_Listener(self.scl_long_tuneup_controller))
		make_openxal_acc_file_button = JButton(" Make OpenXAL Accelerator file for SCL")
		make_openxal_acc_file_button.addActionListener(Make_OpenXAL_Acc_File_Button_Listener(self.scl_long_tuneup_controller))
		export_to_ascii_button = JButton("Export Table to ASCII")
		export_to_ascii_button.addActionListener(Export_Table_to_ASCII_Listener(self.scl_long_tuneup_controller))		
		post_analysis_panel.add(make_xal_acc_file_button)
		post_analysis_panel.add(make_openxal_acc_file_button)
		post_analysis_panel.add(export_to_ascii_button)
		#--------------------------
		self.add(post_analysis_panel)

#------------------------------------------------
#  JTable models
#------------------------------------------------

class Cavs_PhaseAnalysis_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["#","Cavity","Use","Done",]
		self.columnNames += ["<html>E<SUB>in</SUB>(MeV)<html>",]
		self.columnNames += ["<html>E<SUB>out</SUB>(MeV)<html>",]
		self.columnNames += ["<html>&Delta;E<SUB>k/(k-1)</SUB>(keV)<html>",]
		self.columnNames += ["<html>BPMs E<SUB>out</SUB>(MeV)<html>",]
		self.columnNames += ["<html>Model E<SUB>out</SUB>(MeV)<html>",]
		self.columnNames += ["<html>A<SUB>cav</SUB>(MV)<html>",]
		self.columnNames += ["<html>&phi;<SUB>1st gap</SUB>(deg)<html>",]
		self.columnNames += ["<html>&phi;<SUB>gap avg</SUB>(deg)<html>",]
		self.columnNames += ["<html>&delta;E<SUB>fit</SUB>(keV)<html>",]
		self.columnNames += ["<html>Real &Delta;&phi;<SUB>RF</SUB>(deg)<html>",]
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
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		cav_wrapper = cav_wrappers[row]
		if(col == 0): 
			return str(row+1)		
		if(col == 1): 
			return cav_wrapper.alias
		if(col == 2): return cav_wrapper.isGood	
		if(col == 3): return cav_wrapper.isAnalyzed
		if(not cav_wrapper.isGood): return ""	
		if(col == 4): return self.nf3.format(cav_wrapper.eKin_in)
		if(not cav_wrapper.isAnalyzed): return ""		
		if(col == 5): return self.nf3.format(cav_wrapper.eKin_out)
		if(col == 6):
			if(row == 0): return ""
			dE = (cav_wrapper.eKin_in - cav_wrappers[row-1].eKin_out)*1000.
			return "% 6.1f"%dE
		if(col == 7): return self.nf3.format(cav_wrapper.bpm_eKin_out)				
		if(col == 8): return self.nf3.format(cav_wrapper.model_eKin_out)		
		if(col == 9): return self.nf3.format(cav_wrapper.designAmp)		
		if(col == 10): return "% 6.1f"%cav_wrapper.designPhase			
		if(col == 11): return "% 6.1f"%cav_wrapper.avg_gap_phase
		if(col == 12): return "% 6.1f"%(1000.*cav_wrapper.eKin_err)
		if(col == 13): return "% 6.2f"%(cav_wrapper.real_scanPhaseShift)		
		return ""
				
	def getColumnClass(self,col):
		if(col == 2 or col == 3):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]		
		if(col == 4):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		if(col == 4):
			 cav_wrapper.eKin_in = Double.parseDouble(value)
			 
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		analysis_state_controller = scl_long_tuneup_phase_analysis_controller.analysis_state_controller
		if(analysis_state_controller.isRunning): return
		cav_energy_out_graph_panel = scl_long_tuneup_phase_analysis_controller.cav_energy_out_graph_panel
		cav_energy_out_graph_panel.updateGraphData()
		
class Make_XAL_Acc_File_Button_Listener(ActionListener):
	# make XAL accelerator file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		rightNow = Calendar.getInstance()
		date_format = SimpleDateFormat("MM.dd.yyyy")
		time_str = date_format.format(rightNow.getTime())				
		fc = JFileChooser(constants_lib.const_path_dict["XAL_XML_ACC_FILES_DIRS_PATH"])
		fc.setDialogTitle("Save SCL data into the SCL_new.xdxf file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("SCL Acc File",["xdxf",])
		fc.setFileFilter(fl_filter)
		fc.setSelectedFile(File("SCL_"+time_str+".xdxf"))		
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".xdxf") != (len(fl_path) - 5)):
				fl_out = File(fl_out.getPath()+".xdxf")	
			#---------prepare the XmlDataAdaptor 
			root_DA = XmlDataAdaptor.newEmptyDocumentAdaptor()
			scl_DA = root_DA.createChild("xdxf")	
			scl_DA.setValue("date",time_str)
			scl_DA.setValue("system","sns")
			scl_DA.setValue("version","2.0")
			#---- SCLMed	
			seq_name_arr = ["SCLMed","SCLHigh","HEBT1"]
			for seq_name in seq_name_arr:
				accl = self.scl_long_tuneup_controller.linac_wizard_document.accl
				seq = accl.findSequence(seq_name)
				cavs = seq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(SCLCavity.s_strType)))
				quads = seq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType)))
				scl_seq_DA = scl_DA.createChild("sequence")
				scl_seq_DA.setValue("id",seq.getId())
				for quad in quads:
					node_DA = scl_seq_DA.createChild("node")
					node_DA.setValue("id",quad.getId())
					attr_DA = node_DA.createChild("attributes")
					field_DA = attr_DA.createChild("magnet")
					scl_quad_fields_dict_holder = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder
					field_DA.setValue("dfltMagFld",str(scl_quad_fields_dict_holder.quad_field_dict[quad]))
				for cav in cavs:
					node_DA = scl_seq_DA.createChild("sequence")
					node_DA.setValue("id",cav.getId())
					attr_DA = node_DA.createChild("attributes")
					rf_cav_DA = attr_DA.createChild("rfcavity")
					cav_wrappper = self.scl_long_tuneup_controller.getCav_WrapperForCavId(cav.getId())
					(amp,phase) =  (cav_wrappper.designAmp,cav_wrappper.designPhase)
					rf_cav_DA.setValue("amp",float("%8.5f"%amp))
					rf_cav_DA.setValue("phase",float("%8.3f"%phase))
			root_DA.writeTo(fl_out)		
		
class Make_OpenXAL_Acc_File_Button_Listener(ActionListener):
	# make OpenXAL accelerator file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller	
		self.scl_long_tuneup_controller.getMessageTextField().setText("This button's action is not defined yet.")
		
class Export_Table_to_ASCII_Listener(ActionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")		
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save SCL Table data into ASCII file")
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
			txt = "# cav  pos  cav_amp_epics  cav_amp_model  cav_phase rf_gap_avg_phase"
			txt += " phase_offset real_offset eKin_in  eKin_out "
			txt += " delta_eKin_in_out_keV bpm_eKin_out model_eKin_out delta_eKin_fit_keV  E0TL_MeV"
			buffer_out.write(txt)
			buffer_out.newLine()
			buffer_out.flush()
			cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
			for cav_ind in range(len(cav_wrappers)):
				cav_wrapper = cav_wrappers[cav_ind]
				txt = str(cav_ind+1)+" "
				txt += cav_wrapper.cav.getId()+" %8.3f "%cav_wrapper.pos+" %12.5g "%cav_wrapper.initLiveAmp
				txt += " %12.5g "%cav_wrapper.designAmp + " %8.3f "%cav_wrapper.designPhase 
				txt += " %8.3f "%cav_wrapper.avg_gap_phase + " %8.3f "%cav_wrapper.scanPhaseShift
				txt += " %8.3f "%cav_wrapper.real_scanPhaseShift
				txt += " %12.5g "%cav_wrapper.eKin_in + " %12.5g "%cav_wrapper.eKin_out
				dE = 0.
				if(cav_ind != 0):
					dE = (cav_wrapper.eKin_in - cav_wrappers[cav_ind-1].eKin_out)*1000.
				txt += " %12.5g "%dE + " %12.5g "%cav_wrapper.bpm_eKin_out+ " %12.5g "%cav_wrapper.model_eKin_out
				txt += "% 6.1f"%(1000.*cav_wrapper.eKin_err)
				E0TL = 0.
				if(len(cav_wrapper.energy_guess_harm_funcion.getParamArr()) > 1):
					E0TL = cav_wrapper.energy_guess_harm_funcion.getParamArr()[1]
				txt += " %12.5g "%E0TL
				buffer_out.write(txt)
				buffer_out.newLine()
			#---- end of writing
			buffer_out.flush()
			buffer_out.close()
				
class Start_Phase_Analysis_Button_Listener(ActionListener):
	#This button will start analysis to the end
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")			
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		analysis_state_controller = scl_long_tuneup_phase_analysis_controller.analysis_state_controller
		analysis_state_controller.setShouldStop(false)			
		runner = PhaseAnalysis_Runner(self.scl_long_tuneup_controller,true)
		thr = Thread(runner)
		thr.start()			
		
class Start_Phase_Analysis_Select_Cavs_Button_Listener(ActionListener):
	#This button will start analysis for the selected cavities only
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")	
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		analysis_state_controller = scl_long_tuneup_phase_analysis_controller.analysis_state_controller
		analysis_state_controller.setShouldStop(false)		
		runner = PhaseAnalysis_Runner(self.scl_long_tuneup_controller,false)
		thr = Thread(runner)
		thr.start()		
		
class Stop_Phase_Analysis_Button_Listener(ActionListener):						
	#This button action will stop the analysis
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")			
		scl_long_tuneup_phase_analysis_controller = self.scl_long_tuneup_controller.scl_long_tuneup_phase_analysis_controller
		analysis_state_controller = scl_long_tuneup_phase_analysis_controller.analysis_state_controller
		analysis_state_controller.setShouldStop(true)
		scl_one_cavity_tracker_model = scl_long_tuneup_phase_analysis_controller.scl_one_cavity_tracker_model
		scl_one_cavity_tracker_model.stopFitting()

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_TuneUp_PhaseAnalysis_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller	
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_params_panel = JPanel(BorderLayout())
		self.start_stop_analysis_panel = StartStopPhaseAnalysis_Panel(self.scl_long_tuneup_controller)
		top_params_panel.add(self.start_stop_analysis_panel,BorderLayout.NORTH)
		#------cavities scan table panel --------
		cavs_scan_panel = JPanel(BorderLayout())
		self.cavs_table = JTable(Cavs_PhaseAnalysis_Table_Model(self.scl_long_tuneup_controller))
		self.cavs_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.cavs_table.setFillsViewportHeight(true)
		self.cavs_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self.scl_long_tuneup_controller))		
		scrl_panel0 = JScrollPane(self.cavs_table)
		scrl_panel0.setBorder(etched_border)
		cavs_scan_panel.add(scrl_panel0,BorderLayout.CENTER)
		#---------- graph panels --------------------------
		self.cav_energy_out_graph_panel = Cav_Energy_Out_Graph_Panel(self.scl_long_tuneup_controller)
		graph_panel = JPanel(GridLayout(1,1))
		graph_panel.add(self.cav_energy_out_graph_panel)
		#--------center panel = graphs + tables-------------
		center_panel = JPanel(GridLayout(2,1))
		center_panel.add(cavs_scan_panel)
		center_panel.add(graph_panel)
		#-------- post-scan filtering panel
		bottom_panel = JPanel(BorderLayout())
		self.post_analysis_panel = PostPhaseAnalysisActions_Panel(self.scl_long_tuneup_controller)
		bottom_panel.add(self.post_analysis_panel,BorderLayout.WEST)
		#--------------------------------------------------
		self.main_panel.add(top_params_panel,BorderLayout.NORTH)
		self.main_panel.add(bottom_panel,BorderLayout.SOUTH)
		self.main_panel.add(center_panel,BorderLayout.CENTER)
		#------scan state
		self.analysis_state_controller = AnalysisStateController()
		self.scl_one_cavity_tracker_model = SCL_One_Cavity_Tracker_Model(scl_long_tuneup_controller)
		
	def getMainPanel(self):
		return self.main_panel

