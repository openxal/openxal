# The SCL Longitudinal Tune-Up Longitudinal Twiss Analysis controller
# It will calculate longitudinal Twiss parameters along the SCL at each cavity
# For the first cavity we will use Twiss at the beginning of the SCL 
# (the entrance of LEDP).

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

from java.util import List, ArrayList

from Jama import Matrix

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat
from xal.smf.impl import Marker, Quadrupole, RfGap, SCLCavity
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.model.probe import EnvelopeProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.tools.beam import CovarianceMatrix
from xal.ca import ChannelFactory

from xal.extension.widgets.swing import Wheelswitch

from xal.tools.beam import Twiss

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
from harmonics_fitter_lib import calculateAvgErr

from bessel_lib import I0_Bessel

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#    Online Model for Longitudinal Twiss analysis
#------------------------------------------------------------------------
class AnalysisStateController:
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

class Analysis_Runner(Runnable):
	def __init__(self,scl_long_tuneup_controller, run_to_end = true):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.run_to_end = run_to_end
		
	def run(self):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		analysis_state_controller = scl_long_twiss_analysis_controller.analysis_state_controller
		analysis_state_controller.setIsRunning(true)
		twiss_table = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel.twiss_table
		analysis_status_text = scl_long_twiss_analysis_controller.start_stop_panel.analysis_status_text
		iterations_text = scl_long_twiss_analysis_controller.start_stop_panel.iterations_text
		n_iter = int(iterations_text.getValue())
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		scl_twiss_tracker_model.setUpCavAmpPhasesFromPhaseAnalysis()
		scl_twiss_tracker_model.setUpQuads()
		#---------start deal with selected cavities
		cav_selected_inds = twiss_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0): 
			messageTextField.setText("Select one or more cavities to start Twiss Analysis!")
			analysis_state_controller.setIsRunning(false)
			scl_twiss_tracker_model.restoreInitAmpPhases()
			return
		# these are the table model indexes
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers	
		n_cavs = len(cav_wrappers)		
		cav_wrapper = null
		start_ind = cav_selected_inds[0]
		last_ind = cav_selected_inds[len(cav_selected_inds)-1]
		if(self.run_to_end): last_ind = n_cavs - 1
		#----- set long. Twiss isReady=false for all downstream cavities
		for cav_table_ind in range(start_ind,last_ind+1):
			cav_wrapper = cav_wrappers[cav_table_ind]
			cav_wrapper.longTwissBucket.isReady  = false
			cav_wrapper.longTwissBucket.cleanFittingValues()
		twiss_table.getModel().fireTableDataChanged()
		#-------start loop over cavities in the table
		time_start = time.time()
		n_total = last_ind - start_ind + 1
		n_count = 0
		for cav_table_ind in range(start_ind,last_ind+1):
			#print "debug cav_table_index=",cav_table_ind
			iterations_text.setValue(1.0*n_iter)
			cav_wrapper = cav_wrappers[cav_table_ind]		
			longTwissBucket = cav_wrapper.longTwissBucket
			if((not (cav_wrapper.isMeasured and cav_wrapper.isAnalyzed)) and cav_wrapper.isGood):
				messageTextField.setText("Cavity "+cav_wrapper.alias+" has not been analyzed. Fix it!")
				analysis_state_controller.setIsRunning(false)
				scl_twiss_tracker_model.restoreInitAmpPhases()
				iterations_text.setValue(1.0*n_iter)
				return					
			twiss_table.setRowSelectionInterval(cav_table_ind,cav_table_ind)
			txt = "Analysis is running! Cavity="+cav_wrapper.alias
			if(start_ind != last_ind): txt = txt + " to Cavity="+cav_wrappers[last_ind].alias
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
			# put model based analysis here
			scl_twiss_tracker_model.setActiveCavity(cav_wrapper)
			scl_twiss_tracker_model.fit(n_iter)
			if(analysis_state_controller.getShouldStop()): break
			longTwissBucket.isReady = true
			twiss_table.getModel().fireTableDataChanged()
			#-----set input Twiss for the next cavity for the next cavity		
			n_count += 1
		#------end of cavities loop		
		run_time = time.time() - time_start
		run_time_sec = int(run_time % 60)
		run_time_min = int(run_time/60.)		
		iterations_text.setValue(1.0*n_iter)
		txt = ""
		if(analysis_state_controller.getShouldStop()):
			txt = "Interrupted by User! Total time= %3d min  %2d sec "%(run_time_min,run_time_sec)
		else:
			txt = "Finished! Total time= %3d min  %2d sec "%(run_time_min,run_time_sec)
		analysis_status_text.setText(txt)
		

class SCL_Twiss_Tracker_Model:
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		self.scenario = Scenario.newScenarioFor(self.scl_accSeq)
		self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
		self.scenario.unsetStartNode()
		self.scenario.unsetStopNode()			
		self.env_tracker = AlgorithmFactory.createEnvTrackerAdapt(self.scl_accSeq)
		self.env_tracker.setRfGapPhaseCalculation(true)
		self.env_probe_init = ProbeFactory.getEnvelopeProbe(self.scl_accSeq,self.env_tracker)
		self.scenario.resync()
		# in the dictionary we will have 
		# cav_wrappers_param_dict[cav_wrapper] = [cavAmp,phase]
		self.cav_wrappers_param_dict = {}
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		self.cav_amp_phase_dict = {}
		for cav_wrapper in cav_wrappers:
			amp = cav_wrapper.cav.getDfltCavAmp()
			phase = cav_wrapper.cav.getDfltCavPhase()
			self.cav_amp_phase_dict[cav_wrapper] = [amp,phase]
		#---- bpm calibration constants [amp_avg,amp_err,bessel]
		self.bpm_calib_dict = {}
		#---- active cavity
		self.active_cav_wrapper = null
		#---- solver for fitting
		self.solver = null

	def getNewInitlProbe(self):
		return EnvelopeProbe(self.env_probe_init)

	def restoreInitAmpPhases(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			(amp,phase) = self.cav_amp_phase_dict[cav_wrapper]
			cav_wrapper.cav.updateDesignAmp(amp)
			cav_wrapper.cav.updateDesignPhase(phase)
		self.scenario.unsetStartNode()
		self.scenario.unsetStopNode()		

	def setUpCavAmpPhasesFromPhaseAnalysis(self):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isGood and cav_wrapper.isAnalyzed):
				(amp,phase) = (cav_wrapper.designAmp,cav_wrapper.designPhase)
				cav_wrapper.cav.updateDesignAmp(amp)
				cav_wrapper.cav.updateDesignPhase(phase)
			if(not cav_wrapper.isGood):
				cav_wrapper.cav.updateDesignAmp(0.)
					
	def setActiveCavity(self,cav_wrapper):	
		# set up start and stop node for scenario and 0 cav. amp for all others cavities
		self.active_cav_wrapper = cav_wrapper
		gap_list = cav_wrapper.cav.getGapsAsList()
		gap_first = gap_list.get(0)
		self.scenario.setStartNode(gap_first.getId())
		if(cav_wrapper == self.scl_long_tuneup_controller.cav_wrappers[0]):
			self.scenario.unsetStartNode()
		last_bpm_wrapper = cav_wrapper.bpm_wrappers[0]
		for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
			bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
			if(cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] and bpm_wrapper.pos < 280.):
				last_bpm_wrapper = bpm_wrapper
		self.scenario.setStopNode(last_bpm_wrapper.bpm.getId())
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers	
		for cav_wrapper_tmp in cav_wrappers:
			if(cav_wrapper_tmp != cav_wrapper):
				cav_wrapper_tmp.cav.updateDesignAmp(0.)
			
	def setTrackingSelectedCavToLastCav(self,cav_wrapper):		
		self.setUpCavAmpPhasesFromPhaseAnalysis()		
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers	
		gap_list = cav_wrapper.cav.getGapsAsList()
		gap_first = gap_list.get(0)
		self.scenario.setStartNode(gap_first.getId())
		if(cav_wrapper == self.scl_long_tuneup_controller.cav_wrappers[0]):
			self.scenario.unsetStartNode()
		cav_wrapper = cav_wrappers[len(cav_wrappers)-1]
		last_bpm_wrapper = cav_wrapper.bpm_wrappers[0]
		for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
			bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
			if(cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind]):
				last_bpm_wrapper = bpm_wrapper
		self.scenario.setStopNode(last_bpm_wrapper.bpm.getId())		
		
	def isGoodForAnalysis(self, cav_index_arr = null):
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers		
		if(cav_index_arr == null):
			for cav_wrapper in cav_wrappers:
				if(cav_wrapper.isGood):
					if(not cav_wrapper.isAnalyzed):
						return false
		else:
			for cav_index in 	cav_index_arr:
				cav_wrapper = cav_wrappers[cav_index]
				if(cav_wrapper.isGood):
					if(not cav_wrapper.isAnalyzed):
						return false				
		return true
		
	def calibrateBPMs(self):
		# this method used the reversed cav_wrappers array (starts from the end)
		if(not self.isGoodForAnalysis()): return
		#----- consider only SCL-HEBT1
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				if(bpm_wrapper.pos > 280.):
					cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = false
		#----- now reverse the cavs order
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers[:]
		cav_wrappers.reverse()
		cav_wrapper_base = cav_wrappers[0]
		for cav_wrapper in cav_wrappers:
			if(cav_wrapper.isAnalyzed):
				cav_wrapper_base = cav_wrapper
				break
		bpm_wrappers_all = cav_wrapper_base.bpm_wrappers
		bpm_wrappers_and_amps_arr = []
		for bpm_ind in range(len(cav_wrapper_base.bpm_wrappers)):
			bpm_wrapper = cav_wrapper_base.bpm_wrappers[bpm_ind]
			if(not cav_wrapper_base.bpm_wrappers_useInAmpBPMs[bpm_ind]): continue
			#---- we will consider only SCL-HEBT1
			if(bpm_wrapper.pos > 280.): continue
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			amp_arr = []
			for ip in range(graphDataAmp.getNumbOfPoints()):
				amp_arr.append(graphDataAmp.getY(ip))
			(amp_avg,amp_err)= calculateAvgErr(amp_arr)
			amp_err *= math.sqrt(graphDataAmp.getNumbOfPoints())
			bpm_wrappers_and_amps_arr.append([bpm_wrapper,amp_avg,amp_err])
		#---- calibration 
		mass = self.scl_long_tuneup_controller.mass/1.0e+6
		c_light = self.scl_long_tuneup_controller.c_light
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq
		bore_r = 0.035
		scl_bore_r = 0.035
		hebt1_bore_r = 0.0555
		self.bpm_calib_dict = {}
		for [bpm_wrapper,amp_avg,amp_err] in bpm_wrappers_and_amps_arr:
			if(bpm_wrapper.alias.find("HEBT") >= 0): bore_r = hebt1_bore_r
			if(bpm_wrapper.alias.find("SCL") >= 0): bore_r = scl_bore_r
			cav_wrapper_before = null
			for cav_wrapper in cav_wrappers:
				if(cav_wrapper.pos < bpm_wrapper.pos):
					cav_wrapper_before = cav_wrapper
					break
			eKin = -1000.
			if(cav_wrapper_before == null):
				eKin = self.scl_long_tuneup_controller.cav_wrappers[0].eKin_in
			else:
				eKin = cav_wrapper_before.bpm_eKin_out
			beta = math.sqrt(eKin*(eKin+2*mass))/(eKin+mass)
			gamma = 1./math.sqrt(1.0-beta*beta)
			var = 2*math.pi*bpm_freq*bore_r/(beta*gamma*c_light)
			bessel = I0_Bessel(var)
			self.bpm_calib_dict[bpm_wrapper] = [amp_avg,amp_err,bessel,bore_r]
			#print "debug bpm=",bpm_wrapper.alias," amp_avg=",amp_avg," amp_err=",amp_err," bessel=",bessel
		
	def setUpQuads(self):
		self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.setUpOnlineModel()
			
	def runModel(self,env_probe):
		# returns the trajectory
		self.scenario.resync()
		self.scenario.setProbe(env_probe)
		self.scenario.run()
		return self.scenario.getTrajectory()	

	def clean(self):
		self.active_cav_wrapper = null
		self.bpm_calib_dict = {}

	def fit(self,n_iter):
		if(self.active_cav_wrapper == null): return
		cav_wrapper = self.active_cav_wrapper
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
		current = cavity_twiss_params_panel.current_text.getValue()*0.001
		use_cav0_emitt = cavity_twiss_params_panel.keepEmitt_RadioButton.isSelected()
		twiss_arr = cav_wrapper.longTwissBucket.twiss_arr
		steps_arr = cav_wrapper.longTwissBucket.long_Twiss_arr_steps
		(alphaX, betaX, emittX) = (twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance())
		(alphaY, betaY, emittY) = (twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance())
		(alphaZ, betaZ, emittZ) = (twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance())
		cav0_wrapper = self.scl_long_tuneup_controller.cav_wrappers[0]
		if(use_cav0_emitt and cav0_wrapper.longTwissBucket.isReady):
			mass = self.scl_long_tuneup_controller.mass/1.0e+6			
			#emitt0 = cav0_wrapper.longTwissBucket.long_Twiss_fit.getEmittance()
			emitt0 = cav0_wrapper.longTwissBucket.twiss_arr[2].getEmittance()
			eKin0 = cav0_wrapper.eKin_in
			eKin = cav_wrapper.eKin_in
			beta = math.sqrt(eKin*(eKin+2*mass))/(eKin+mass)
			beta0 = math.sqrt(eKin0*(eKin0+2*mass))/(eKin0+mass)
			gamma = 1./math.sqrt(1.0-beta*beta)
			gamma0 = 1./math.sqrt(1.0-beta0*beta0)
			emittZ = emitt0*gamma0**3*beta0/(gamma**3*beta)
			#print "debug cav=",cav_wrapper.alias," eKin0=",eKin0," eKin=",eKin," emitZ0=",emitt0," emittZ=",emittZ
		variables = ArrayList()
		variables.add(Variable("alphaZ", alphaZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("betaZ", betaZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("emittZ", emittZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		delta_hint = InitialDelta()
		variables_fit = ArrayList()
		if(steps_arr[0] != 0.):
			variables_fit.add(variables.get(0))
			delta_hint.addInitialDelta(variables.get(0),steps_arr[0])
		if(steps_arr[1] != 0.):
			variables_fit.add(variables.get(1))
			delta_hint.addInitialDelta(variables.get(1),steps_arr[1])
		if(steps_arr[2] != 0. and (not use_cav0_emitt)):
			variables_fit.add(variables.get(2))
			delta_hint.addInitialDelta(variables.get(2),steps_arr[2]*1.0e-6)
		twiss_arr = []
		twiss_arr.append(Twiss(alphaX, betaX, emittX))
		twiss_arr.append(Twiss(alphaY, betaY, emittY))
		twiss_arr.append(Twiss(alphaZ, betaZ, emittZ))
		scorer = AccLongTwissCalculator(self.scl_long_tuneup_controller,cav_wrapper,variables,twiss_arr,current)
		if(not  scorer.isGood): return
		maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(n_iter)
		self.solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)
		problem = ProblemFactory.getInverseSquareMinimizerProblem(variables_fit,scorer,0.0001)
		problem.addHint(delta_hint)
		self.solver.solve(problem)
		if(not scorer.isGood): return
		#------- get results
		trial = self.solver.getScoreBoard().getBestSolution()
		scorer.trialToTwiss(trial)
		twiss_arr = scorer.getTwissArr()
		(alphaZ, betaZ, emittZ) = (twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance())	
		cav_wrapper.longTwissBucket.long_Twiss_fit.setTwiss(alphaZ, betaZ, emittZ)
		cav_wrapper.longTwissBucket.fit_bpm_amp_avg_err = scorer.chi2_txt.getValue()
		#---- calculate matrix results
		scorer_res = scorer.calculateDiff2(true)
		if(not scorer.isGood): return
		(diff2,tr_mtrx_arr) = scorer_res
		self.solver = null
		nSize = len(tr_mtrx_arr)
		if(nSize < 3): return
		mMatrZ = Matrix(nSize,3)
		weghtVectorZ = []
		size2VectorZ = []	
		for isz in range(nSize):
			[size2,size2_err,m11,m12,m22] = tr_mtrx_arr[isz]
			mMatrZ.set(isz,0,m11)
			mMatrZ.set(isz,1,m12)
			mMatrZ.set(isz,2,m22)			
			size2VectorZ.append(size2)
			weghtVectorZ.append(1./size2_err**2)
		#mwmMatr = (M^T*W*M)
		mwmMatrZ = Matrix(3,3)
		for i0 in range(3):
			for i1 in range(3):
				sumZ = 0.
				for j in range(nSize):
					sumZ +=  mMatrZ.get(j,i0)*weghtVectorZ[j]*mMatrZ.get(j,i1)					
				mwmMatrZ.set(i0,i1,sumZ)
		errorMatrix_Z = mwmMatrZ.inverse()
		correlValsVector_Z = [0.,0.,0.]
		for i0 in range(3):
			for i1 in range(3):
				for isz in range(nSize):
					correlValsVector_Z[i0] += errorMatrix_Z.get(i0,i1)*mMatrZ.get(isz,i1)*weghtVectorZ[isz]*size2VectorZ[isz]
		#--------Errors for values [ <z2>, <z*zp>, <zp2>]		
		correlErrValsVectorZ = [0.,0.,0.]
		for i0 in range(3):
			correlErrValsVectorZ[i0] = math.sqrt(math.fabs(errorMatrix_Z.get(i0,i0)))
		([z2,z_zp,zp2],[z2_err,z_zp_err,zp2_err]) = (correlValsVector_Z,correlErrValsVectorZ)
		cav_wrapper.longTwissBucket.long_Twiss_z2_zzp_zp2_err[0] = z2_err
		cav_wrapper.longTwissBucket.long_Twiss_z2_zzp_zp2_err[1] = z_zp_err
		cav_wrapper.longTwissBucket.long_Twiss_z2_zzp_zp2_err[2] = zp2_err
		emittZ = math.sqrt(math.fabs(z2*zp2 - z_zp**2))
		betaZ = z2/emittZ
		alphaZ = - z_zp/emittZ
		emittZ_err = math.fabs(math.sqrt((zp2*z2_err/(2*emittZ))**2 + (z2*zp2_err/(2*emittZ))**2 + (z_zp*z_zp_err/emittZ)**2))
		betaZ_err = math.fabs(math.sqrt((z2_err/emittZ)**2 + (z2*emittZ_err/(emittZ**2))**2))
		alphaZ_err = math.fabs(math.sqrt((z_zp_err/emittZ)**2 + (z_zp*emittZ_err/(emittZ**2))**2))
		cav_wrapper.longTwissBucket.long_Twiss_matrix.setTwiss(alphaZ, betaZ, emittZ)
		cav_wrapper.longTwissBucket.long_Twiss_arr_err[0] = alphaZ_err
		cav_wrapper.longTwissBucket.long_Twiss_arr_err[1] = betaZ_err
		cav_wrapper.longTwissBucket.long_Twiss_arr_err[2] = emittZ_err
		
#------------------------------------------------------------------------
#           Auxiliary Long.Twiss Analysis classes and functions
#------------------------------------------------------------------------	
class AccLongTwissCalculator(Scorer):
	def __init__(self,scl_long_tuneup_controller,cav_wrapper,variables,twiss_arr,current):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		bpm_calib_dict = scl_twiss_tracker_model.bpm_calib_dict		
		self.cav_wrapper = cav_wrapper
		self.variables = variables
		self.twiss_arr = twiss_arr
		self.current = current
		#-------------------------------
		self.min_diff2 = 1.0e+36
		#-------------------------------
		self.count_txt = scl_long_twiss_analysis_controller.start_stop_panel.iterations_text
		self.count = self.count_txt.getValue()
		self.chi2_txt = scl_long_twiss_analysis_controller.start_stop_panel.chi2_txt
		#-------------------------------
		self.irfGap = scl_twiss_tracker_model.scenario.elementsMappedTo(cav_wrapper.cav.getGapsAsList().get(0))[0]
		for cav_wrapper_tmp in self.scl_long_tuneup_controller.cav_wrappers:
			cav_wrapper_tmp.cav.updateDesignAmp(0.)
			if(cav_wrapper_tmp == self.cav_wrapper):
				cav_wrapper_tmp.cav.updateDesignAmp(cav_wrapper_tmp.designAmp)
		#-------------------------------
		self.bpm_wrappers = []
		for bpm_wrapper_ind in range(len(self.cav_wrapper.bpm_wrappers)):
			bpm_wrapper = self.cav_wrapper.bpm_wrappers[bpm_wrapper_ind]
			if(not bpm_wrapper.isGood): continue
			if(not self.cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_wrapper_ind]): continue
			if(bpm_calib_dict.has_key(bpm_wrapper) and bpm_wrapper.pos > self.cav_wrapper.pos and bpm_wrapper.pos < 280.):
				self.bpm_wrappers.append(bpm_wrapper)
		#-------------------------------
		scl_twiss_tracker_model.scenario.resync()	
		#---- clean up the message text
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		#-------------------------------
		self.isGood = true
		#------check calibration--------
		if(len(self.bpm_wrappers) < 1): 
			self.isGood = false
			self.scl_long_tuneup_controller.getMessageTextField().setText("Please Calibrate BPMs's Amplitudes!")				
		
	def score(self,trial,variables_in):
		#print "debug count=",self.count," min_diff=",math.sqrt(self.min_diff2)
		self.count_txt.setValue(1.0*self.count)
		self.count -= 1
		if(self.count < 0):
			return 0.
		self.trialToTwiss(trial)
		diff2 = self.calculateDiff2()
		#print "debug diff=",math.sqrt(diff2)
		if(self.min_diff2 > diff2):
			self.min_diff2 = diff2
			self.updateGUI_Elements()
		return diff2		

	def calculateDiff2(self,calc_mtrx = false):
		mass = self.scl_long_tuneup_controller.mass/1.0e+6
		c_light = self.scl_long_tuneup_controller.c_light
		bpm_freq = self.scl_long_tuneup_controller.bpm_freq		
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		bpm_calib_dict = scl_twiss_tracker_model.bpm_calib_dict
		if(len(self.bpm_wrappers) < 1): 
			self.isGood = false
			self.scl_long_tuneup_controller.getMessageTextField().setText("Please Calibrate BPMs's Amplitudes!")		
			return 0.
		tr_mtrx_arr = null
		if(calc_mtrx):
			tr_mtrx_arr = []
		(graphDataAmp,graphDataPhase) = self.cav_wrapper.bpm_amp_phase_dict[self.bpm_wrappers[0]]
		diff2 = 0.
		count = 0.
		cav_delta_phase = self.cav_wrapper.livePhase - self.cav_wrapper.designPhase
		for bpm_wrapper in self.bpm_wrappers:
				bpm_wrapper_ind = self.cav_wrapper.bpm_wrappers.index(bpm_wrapper)
				[bpm_wrapper_tmp,amp_plotTh] = self.cav_wrapper.longTwissBucket.bpm_amp_plotTh_arr[bpm_wrapper_ind]
				amp_plotTh.removeAllPoints()	
		for ip in range(graphDataAmp.getNumbOfPoints()):
			cav_phase = graphDataAmp.getX(ip)
			cav_model_phase = makePhaseNear(cav_phase - cav_delta_phase,0.)
			self.irfGap.setPhase(cav_model_phase*math.pi/180.)
			env_probe = scl_twiss_tracker_model.getNewInitlProbe()
			env_probe.initFromTwiss(self.twiss_arr)
			env_probe.setBeamCurrent(self.current)
			env_probe.setKineticEnergy(self.cav_wrapper.eKin_in*1.0e+6)
			scl_twiss_tracker_model.scenario.setProbe(env_probe)
			try:
				scl_twiss_tracker_model.scenario.run()
			except:
				self.scl_long_tuneup_controller.getMessageTextField().setText("Cannot track the envelop! Please remove some BPMs!")
				scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
				analysis_state_controller = scl_long_twiss_analysis_controller.analysis_state_controller
				analysis_state_controller.setShouldStop(true)	
				scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
				if(scl_twiss_tracker_model.solver != null):
					scl_twiss_tracker_model.solver.stopSolving()	
				self.isGood = false
				return 0.
			traj = scl_twiss_tracker_model.scenario.getTrajectory()				
			state = traj.stateForElement(self.bpm_wrappers[0].bpm.getId())
			eKin = state.getKineticEnergy()/1.0e+6
			beta = math.sqrt(eKin*(eKin+2*mass))/(eKin+mass)
			gamma = 1./math.sqrt(1.0-beta*beta)			
			for bpm_wrapper in self.bpm_wrappers:
				(graphDataAmp,graphDataPhase) = self.cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
				amp_exp = graphDataAmp.getY(ip)
				state = traj.stateForElement(bpm_wrapper.bpm.getId())
				if(state == null): continue
				[amp0_avg,amp0_err,bessel0,bore_r] = bpm_calib_dict[bpm_wrapper]
				bessel = I0_Bessel(2*math.pi*bpm_freq*bore_r/(beta*gamma*c_light))
				sizeZ_sec = state.twissParameters()[2].getEnvelopeRadius()/(c_light*beta)
				amp_th = (amp0_avg*bessel0/bessel)*math.exp(-(2*math.pi*bpm_freq*sizeZ_sec)**2/2.0)
				amp_th_err = amp_th*amp0_err/amp0_avg
				bpm_wrapper_ind = self.cav_wrapper.bpm_wrappers.index(bpm_wrapper)
				[bpm_wrapper_tmp,amp_plotTh] = self.cav_wrapper.longTwissBucket.bpm_amp_plotTh_arr[bpm_wrapper_ind]
				amp_plotTh.addPoint(cav_phase,amp_th,amp_th_err)
				diff2 += (amp_th - amp_exp)**2
				count += 1
				if(calc_mtrx):
					 		m = state.getResponseMatrix()
							size2_sec = -2.*math.log(amp_exp*bessel/amp0_avg)/(2*math.pi*bpm_freq)**2
							size2_err_sec = (2./(2*math.pi*bpm_freq)**2)*amp0_err/amp_exp
							size2 = size2_sec*(beta*c_light)**2
							size2_err = size2_err_sec*(beta*c_light)**2
							tr_mtrx_arr.append([size2,size2_err,(m.getElem(4,4))**2,2*m.getElem(4,4)*m.getElem(4,5),(m.getElem(4,5))**2])
		if(count > 0): diff2 /= count
		if(calc_mtrx):
			return (diff2,tr_mtrx_arr)			
		return diff2
		
	def getTwissArr(self):
		return self.twiss_arr
		
	def trialToTwiss(self,trial):
		#---	set up initial Twiss	from Trial
		twissArr = self.twiss_arr
		variables = self.variables
		(alphaZ, betaZ, emittZ) = (twissArr[2].getAlpha(),twissArr[2].getBeta(),twissArr[2].getEmittance())			
		var_map = trial.getTrialPoint().getValueMap()
		if(var_map.containsKey(variables.get(0))): alphaZ =  trial.getTrialPoint().getValue(variables.get(0))
		if(var_map.containsKey(variables.get(1))): betaZ =  math.fabs(trial.getTrialPoint().getValue(variables.get(1)))
		if(var_map.containsKey(variables.get(2))): emittZ =  math.fabs(trial.getTrialPoint().getValue(variables.get(2)))		
		self.twiss_arr[2].setTwiss(alphaZ, betaZ, emittZ)			
		
	def updateGUI_Elements(self):
		self.chi2_txt.setValue(math.sqrt(self.min_diff2))
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		bpm_amps_graph_panel = scl_long_twiss_analysis_controller.bpm_amps_graph_panel
		bpm_amps_graph_panel.updateGraphData()	
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
		cavity_twiss_params_panel.cavTwiss_table.getModel().fireTableDataChanged()
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class CavityTwissParams_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"Cavity Initial Parameters")
		self.setBorder(titled_border)		
		self.cavTwiss_table = JTable(Cavity_Twiss_Table_Model(self.scl_long_tuneup_controller))
		self.cavTwiss_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cavTwiss_table.setFillsViewportHeight(true)
		self.cavTwiss_table.setPreferredScrollableViewportSize(Dimension(360,80))
		cavTwiss_panel = JPanel(BorderLayout())
		cavTwiss_panel.add(JScrollPane(self.cavTwiss_table), BorderLayout.CENTER)
		#----- the buttons panel
		calibrate_bpm_amps_button = JButton("Calibrate BPM Amps.")
		calibrate_bpm_amps_button.addActionListener(Calibrate_BPM_Amps_Button_Listener(self.scl_long_tuneup_controller))	
		read_bpm_calibration_button = JButton("Import BPM Calibration")
		read_bpm_calibration_button.addActionListener(Read_BPM_Calibration_Button_Listener(self.scl_long_tuneup_controller))			
		init_twiss_design_button = JButton("Init. Twiss from Design")
		init_twiss_design_button.addActionListener(Init_Twiss_from_Design_Button_Listener(self.scl_long_tuneup_controller))
		init_twiss_lw_button = JButton("Init. Tr. Twiss from LW Analysis")
		init_twiss_lw_button.addActionListener(Init_Twiss_from_LW_Button_Listener(self.scl_long_tuneup_controller))
		self.current_text = DoubleInputTextField(32.0,ScientificNumberFormat(4),8)
		current_lbl = JLabel("Current[mA]=",JLabel.RIGHT)	
		button1_panel = JPanel(BorderLayout())
		button1_panel.add(current_lbl,BorderLayout.WEST)
		button1_panel.add(self.current_text,BorderLayout.EAST)
		self.keepEmitt_RadioButton = JRadioButton("Constant Long. Emitt")
		calculate_twiss_button = JButton("Calc. Twiss Selected->Cav23d")
		calculate_twiss_button.addActionListener(Calc_Twiss_to_End_Button_Listener(self.scl_long_tuneup_controller))
		fit_to_calc_twiss_button = JButton("Copy Cav. Fit Twiss to Calc.")
		fit_to_calc_twiss_button.addActionListener(Copy_Fit_to_Calc_Button_Listener(self.scl_long_tuneup_controller))
		mtrx_to_calc_twiss_button = JButton("Copy Cav. Mtrx Twiss to Calc.")
		mtrx_to_calc_twiss_button.addActionListener(Copy_Mtrx_to_Calc_Button_Listener(self.scl_long_tuneup_controller))		
		button_panel = JPanel(GridLayout(4,2,2,2))
		button_panel.add(calibrate_bpm_amps_button)
		button_panel.add(read_bpm_calibration_button)		
		button_panel.add(init_twiss_design_button)
		button_panel.add(init_twiss_lw_button)
		button_panel.add(fit_to_calc_twiss_button)
		button_panel.add(mtrx_to_calc_twiss_button)
		button_panel.add(button1_panel)
		button_panel.add(self.keepEmitt_RadioButton)
		button_panel.add(calculate_twiss_button)		
		#---- next left button panel
		select_bpms_button = JButton("Select BPMs")
		select_bpms_button.addActionListener(Select_BPMs_in_Analysis_Button_Listener(self.scl_long_tuneup_controller))		
		deselect_bpms_button = JButton("Deselect BPMs")
		deselect_bpms_button.addActionListener(Deselect_BPMs_in_Analysis_Button_Listener(self.scl_long_tuneup_controller))	
		export_bpm_calibration_button = JButton("Export BPM Calibration")
		export_bpm_calibration_button.addActionListener(Export_BPM_Calibration_Button_Listener(self.scl_long_tuneup_controller))	
		dump_twiss_data_button = JButton("Export Twiss Data to ASCII")
		dump_twiss_data_button.addActionListener(Dump_Twiss_Data_Button_Listener(self.scl_long_tuneup_controller))	
		dump_cav_amp_phase_data_button = JButton("Export Cav. Amp.&Phases to ASCII")
		dump_cav_amp_phase_data_button.addActionListener(Dump_Cav_Amp_Phases_Data_Button_Listener(self.scl_long_tuneup_controller))			
		dump_quad_fields_button = JButton("Export Quad Fields to ASCII")
		dump_quad_fields_button.addActionListener(Dump_Quad_Fields_Button_Listener(self.scl_long_tuneup_controller))	
		read_quad_fields_button = JButton("Import Quad Fields from ASCII")
		read_quad_fields_button.addActionListener(Read_Quad_Fields_Button_Listener(self.scl_long_tuneup_controller))
		button_panel1 = JPanel(GridLayout(4,1,2,2))
		button_panel1.add(export_bpm_calibration_button)			
		button_panel1.add(select_bpms_button)
		button_panel1.add(deselect_bpms_button)	
		button_panel1.add(calculate_twiss_button)
		button_panel2 = JPanel(GridLayout(4,1,2,2))
		button_panel2.add(dump_twiss_data_button)
		button_panel2.add(dump_cav_amp_phase_data_button)
		button_panel2.add(dump_quad_fields_button)
		button_panel2.add(read_quad_fields_button)
		#-------------- the final panel
		center0_panel =JPanel(BorderLayout())
		center1_panel =JPanel(BorderLayout())
		center1_panel.add(button_panel,BorderLayout.WEST)
		center1_panel.add(button_panel1,BorderLayout.CENTER)
		center1_panel.add(button_panel2,BorderLayout.EAST)
		center0_panel.add(center1_panel,BorderLayout.WEST)
		self.add(cavTwiss_panel,BorderLayout.WEST)
		self.add(center0_panel,BorderLayout.CENTER)

class LongTwissParamsTable_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		#------ twiss table panel
		self.twiss_table_panel = JPanel(BorderLayout())
		titled_border = BorderFactory.createTitledBorder(etched_border,"Longitudinal Twiss Parameters")
		self.twiss_table_panel.setBorder(titled_border)
		#------ bpm table panel
		self.bpm_table_panel = JPanel(BorderLayout())
		#------ JTables
		self.twiss_table = JTable(Cavs_Input_Long_Twiss_Table_Model(self.scl_long_tuneup_controller))
		self.twiss_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.twiss_table.setFillsViewportHeight(true)
		self.twiss_table.getSelectionModel().addListSelectionListener(Cavs_Table_Selection_Listener(self.scl_long_tuneup_controller))		
		self.bpms_table_model = LongTwissAnalysis_BPMs_Table_Model(self.scl_long_tuneup_controller)
		self.bpms_table = JTable(self.bpms_table_model)
		self.bpms_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.bpms_table.setFillsViewportHeight(true)	
		self.bpms_table.getSelectionModel().addListSelectionListener(BPMs_Table_Selection_Listener(self.scl_long_tuneup_controller))
		self.bpms_table.setPreferredScrollableViewportSize(Dimension(200,300))
		scrl_panel0 = JScrollPane(self.twiss_table)
		scrl_panel0.setBorder(etched_border)
		scrl_panel1 = JScrollPane(self.bpms_table)
		self.bpm_table_border = BorderFactory.createTitledBorder(etched_border,"Cavity")
		scrl_panel1.setBorder(self.bpm_table_border)		
		#--------------------------
		self.twiss_table_panel.add(scrl_panel0,BorderLayout.CENTER)
		self.bpm_table_panel.add(scrl_panel1,BorderLayout.CENTER)
		#--------------------------
		self.add(self.twiss_table_panel,BorderLayout.CENTER)
		self.add(self.bpm_table_panel,BorderLayout.EAST)

class StartStopAnalysis_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
		self.iterations_text = DoubleInputTextField(200.0,ScientificNumberFormat(4),8)
		iterations_lbl = JLabel("Fit Iterations=",JLabel.RIGHT)
		self.chi2_txt = DoubleInputTextField(0.0,ScientificNumberFormat(6),10)
		chi2_label = JLabel("Avg. Err=",JLabel.RIGHT)
		start_scan_button = JButton("Start Analysis")
		start_scan_button.addActionListener(Start_Analysis_Button_Listener(self.scl_long_tuneup_controller))
		start_scan_for_selection_button = JButton("Start for Selected Cavs.")
		start_scan_for_selection_button.addActionListener(Start_Analysis_Button_Listener(self.scl_long_tuneup_controller,false))
		enough_analysis_button = JButton("Enough")
		enough_analysis_button.addActionListener(Enough_Analysis_Button_Listener(self.scl_long_tuneup_controller))
		stop_scan_button = JButton("Stop Analysis")
		stop_scan_button.addActionListener(Stop_Analysis_Button_Listener(self.scl_long_tuneup_controller))
		buttons_panel.add(iterations_lbl)
		buttons_panel.add(self.iterations_text)
		buttons_panel.add(chi2_label)
		buttons_panel.add(self.chi2_txt)
		buttons_panel.add(start_scan_button)
		buttons_panel.add(start_scan_for_selection_button)
		buttons_panel.add(enough_analysis_button)
		buttons_panel.add(stop_scan_button)
		self.analysis_status_text = JTextField()
		self.analysis_status_text.setText("Analysis status")
		self.analysis_status_text.setHorizontalAlignment(JTextField.LEFT)
		self.analysis_status_text.setForeground(Color.red)
		self.add(buttons_panel,BorderLayout.WEST)
		self.add(self.analysis_status_text,BorderLayout.CENTER)

class BPM_Amps_Graph_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(GridLayout(1,1))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		self.bpm_amps_plot = FunctionGraphsJPanel()
		self.bpm_amps_plot.setLegendButtonVisible(true)
		self.bpm_amps_plot.setChooseModeButtonVisible(true)
		self.bpm_amps_plot.setName("BPM Amp. vs. Cavity Phase: ")
		self.bpm_amps_plot.setAxisNames("Cav Phase, [deg]","BPM Amp., a.u.")	
		self.bpm_amps_plot.setBorder(etched_border)
		self.add(self.bpm_amps_plot)
		
	def removeAllGraphData(self):
		self.bpm_amps_plot.removeAllGraphData()
			
	def updateGraphData(self):
		self.bpm_amps_plot.removeAllGraphData()
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		twiss_table = twiss_bpm_tables_panel.twiss_table
		bpms_table = twiss_bpm_tables_panel.bpms_table
		cav_index = twiss_table.getSelectedRow()
		bpm_indexes = bpms_table.getSelectedRows()
		if(cav_index < 0 or len(bpm_indexes) <= 0): return
 		if(bpm_indexes[0] < 0): return
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_index]
		longTwissBucket = cav_wrapper.longTwissBucket
		for bpm_wrapper_ind in range(len(cav_wrapper.bpm_wrappers)):
			if(not bpm_wrapper_ind in bpm_indexes): continue
			if(not cav_wrapper.bpm_wrappers[bpm_wrapper_ind].isGood): continue			
			bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_wrapper_ind]
			[bpm_wrapper_tmp,amp_plotTh] = longTwissBucket.bpm_amp_plotTh_arr[bpm_wrapper_ind]
			(graphDataAmp,graphDataPhase) = cav_wrapper.bpm_amp_phase_dict[bpm_wrapper]
			self.bpm_amps_plot.addGraphData(amp_plotTh)
			self.bpm_amps_plot.addGraphData(graphDataAmp)

class Beam_Sizes_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---- plots for Hor-Ver and Longitudinal
		self.hor_ver_plot = FunctionGraphsJPanel()
		self.hor_ver_plot.setLegendButtonVisible(true)
		self.hor_ver_plot.setChooseModeButtonVisible(true)
		self.hor_ver_plot.setName("Horizontal and Vertical Planes")
		self.hor_ver_plot.setAxisNames("Position, m","Parameter")	
		self.hor_ver_plot.setBorder(etched_border)		
		self.long_plot = FunctionGraphsJPanel()
		self.long_plot.setLegendButtonVisible(true)
		self.long_plot.setChooseModeButtonVisible(true)
		self.long_plot.setName("Longitudinal Direction")
		self.long_plot.setAxisNames("Position, m","Parameter")	
		self.long_plot.setBorder(etched_border)
		#---- panels
		graph_panel = JPanel(GridLayout(2,1))
		graph_panel.add(self.hor_ver_plot)
		graph_panel.add(self.long_plot)
		buttons_panel = JPanel(BorderLayout())
		plot_button_panel = JPanel(GridLayout(1,1))
		hor_ver_buttons_panel = JPanel(GridLayout(4,1))
		hor_ver_buttons_panel.setBorder(etched_border)
		long_buttons_panel = JPanel(GridLayout(12,1))
		long_buttons_panel.setBorder(etched_border)
		buttons_panel_tmp1 = JPanel(BorderLayout())
		buttons_panel_tmp1.add(hor_ver_buttons_panel,BorderLayout.CENTER)
		buttons_panel_tmp1.add(long_buttons_panel,BorderLayout.SOUTH)
		buttons_panel.add(buttons_panel_tmp1,BorderLayout.NORTH)
		#----------------------------------------------------
		replot_button = JButton("Replot All Graphs")
		plot_button_panel.add(replot_button)
		#----------------------------------------------------
		self.xSize_Button = JRadioButton("Hor. Beam Size [mm]")
		self.ySize_Button = JRadioButton("Ver. Beam Size [mm]")
		self.xBeta_Button = JRadioButton("Hor. Beta Norm.[mm/mrad]")
		self.yBeta_Button = JRadioButton("Ver. Beta Norm.[mm/mrad]")
		hor_ver_buttons_panel.add(self.xSize_Button)
		hor_ver_buttons_panel.add(self.ySize_Button)
		hor_ver_buttons_panel.add(self.xBeta_Button)
		hor_ver_buttons_panel.add(self.yBeta_Button)
		self.xSize_Button.setSelected(false)
		self.ySize_Button.setSelected(false)
		self.xBeta_Button.setSelected(false)
		self.yBeta_Button.setSelected(false)
		self.hor_ver_gd_arr = []
		for i in range(4):
			gd = BasicGraphData()
			gd.setLineThick(3)
			gd.setGraphPointSize(7)
			self.hor_ver_gd_arr.append(gd)
		self.hor_ver_gd_arr[0].setGraphColor(Color.BLUE)
		self.hor_ver_gd_arr[0].setGraphProperty(GRAPH_LEGEND_KEY,"Hor. Beam Size [mm]")
		self.hor_ver_gd_arr[1].setGraphColor(Color.RED)
		self.hor_ver_gd_arr[1].setGraphProperty(GRAPH_LEGEND_KEY,"Ver. Beam Size [mm]")
		self.hor_ver_gd_arr[2].setGraphColor(Color.BLUE)
		self.hor_ver_gd_arr[2].setGraphProperty(GRAPH_LEGEND_KEY,"Hor. Beta Norm.[mm/mrad]")
		self.hor_ver_gd_arr[3].setGraphColor(Color.RED)
		self.hor_ver_gd_arr[3].setGraphProperty(GRAPH_LEGEND_KEY,"Ver. Beta Norm.[mm/mrad]")
		#----------------------------------------------------
		self.zSize_Calc_Button  = JRadioButton("Long. Size [deg] Calc.")
		self.zSize_Fit_Button   = JRadioButton("Long. Size [deg] Fit.")
		self.zSize_Matr_Button  = JRadioButton("Long. Size [deg] Matr.")
		self.zEmitt_Calc_Button = JRadioButton("Long. Emitt. [deg*MeV] Calc.")
		self.zEmitt_Fit_Button  = JRadioButton("Long. Emitt. [deg*MeV] Fit.")
		self.zEmitt_Matr_Button = JRadioButton("Long. Emitt. [deg*MeV] Matr.")
		self.zBeta_Calc_Button  = JRadioButton("Long. Beta. [deg/MeV] Calc.")
		self.zBeta_Fit_Button   = JRadioButton("Long. Beta. [deg/MeV] Fit.")
		self.zBeta_Matr_Button  = JRadioButton("Long. Beta. [deg/MeV] Matr.")
		self.zAlpha_Calc_Button = JRadioButton("Long. Alpha. Calc.")
		self.zAlpha_Fit_Button  = JRadioButton("Long. Alpha. Fit.")
		self.zAlpha_Matr_Button = JRadioButton("Long.Alpha.  Matr.")
		self.zParam_button_arr = []
		self.zParam_button_arr.append(self.zSize_Calc_Button)
		self.zParam_button_arr.append(self.zSize_Fit_Button)
		self.zParam_button_arr.append(self.zSize_Matr_Button)
		self.zParam_button_arr.append(self.zEmitt_Calc_Button)
		self.zParam_button_arr.append(self.zEmitt_Fit_Button)
		self.zParam_button_arr.append(self.zEmitt_Matr_Button)
		self.zParam_button_arr.append(self.zBeta_Calc_Button)
		self.zParam_button_arr.append(self.zBeta_Fit_Button)
		self.zParam_button_arr.append(self.zBeta_Matr_Button)
		self.zParam_button_arr.append(self.zAlpha_Calc_Button)
		self.zParam_button_arr.append(self.zAlpha_Fit_Button)
		self.zParam_button_arr.append(self.zAlpha_Matr_Button)
		for button in self.zParam_button_arr:
			button.setSelected(false)
			long_buttons_panel.add(button)
		self.long_gd_arr = []
		for i in range(12):
			gd = BasicGraphData()
			gd.setLineThick(3)
			gd.setGraphPointSize(7)
			if(i%3 == 0): gd.setGraphColor(Color.BLACK)
			if(i%3 == 1): gd.setGraphColor(Color.BLUE)
			if(i%3 == 2): gd.setGraphColor(Color.RED)
			button = self.zParam_button_arr[i]
			gd.setGraphProperty(GRAPH_LEGEND_KEY,button.getText())
			self.long_gd_arr.append(gd)
		#----------------------------------------------------
		replot_listener = Beam_Size_Replot_Button_Listener(self.scl_long_tuneup_controller)
		replot_button.addActionListener(replot_listener)
		radio_buttons_listener = Beam_Size_Radio_Buttons_Listener(self.scl_long_tuneup_controller)
		self.xSize_Button.addActionListener(radio_buttons_listener)
		self.ySize_Button.addActionListener(radio_buttons_listener)
		self.xBeta_Button.addActionListener(radio_buttons_listener)
		self.yBeta_Button.addActionListener(radio_buttons_listener)
		for button in self.zParam_button_arr:
			button.addActionListener(radio_buttons_listener)
		#----------------------------------------------------
		scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
		cavs = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(SCLCavity.s_strType))).andStatus(true))
		for quad in quads:
			pos = scl_accSeq.getPosition(quad)
			self.hor_ver_plot.addVerticalLine(pos,Color.BLACK)
			self.long_plot.addVerticalLine(pos,Color.BLACK)																 
		for cav in cavs:	
			pos = scl_accSeq.getPosition(cav)	
			self.hor_ver_plot.addVerticalLine(pos,Color.RED)
			self.long_plot.addVerticalLine(pos,Color.RED)		
		#----------------------------------------------------
		self.add(graph_panel,BorderLayout.CENTER)
		left_panel =  JPanel(BorderLayout())
		left_panel.add(plot_button_panel,BorderLayout.NORTH)
		left_panel.add(JScrollPane(buttons_panel,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER),BorderLayout.CENTER)
		self.add(left_panel,BorderLayout.WEST)
		
	def removeAllGraphData(self):
		self.hor_ver_plot.removeAllGraphData()
		self.long_plot.removeAllGraphData()
			
	def recalculateGraphs(self):
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		self.removeAllGraphData()
		for gd in (self.long_gd_arr+self.hor_ver_gd_arr):
			gd.removeAllPoints()	
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel		
		cav_selected_inds = twiss_bpm_tables_panel.twiss_table.getSelectedRows()	
		cav_selected_ind0 = 0
		cav_selected_ind1 = -1 
		if(len(cav_selected_inds) > 1):
			cav_selected_ind0 = cav_selected_inds[0]
			cav_selected_ind1 = cav_selected_inds[len(cav_selected_inds)-1]
		if(not scl_twiss_tracker_model.isGoodForAnalysis()): return
		eKin_in = self.scl_long_tuneup_controller.cav_wrappers[0].eKin_in
		mass = self.scl_long_tuneup_controller.mass/1.0e+6
		c = 299792458.
		if(cav_selected_ind1 < 0): cav_selected_ind1 = len(self.scl_long_tuneup_controller.cav_wrappers) - 1
		for cav_ind in range(cav_selected_ind0,cav_selected_ind1+1):
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_ind]
			if(cav_wrapper.isGood):
				pos = self.scl_long_tuneup_controller.scl_accSeq.getPosition(cav_wrapper.cav.getGapsAsList()[0])
				eKin_in = cav_wrapper.eKin_in
				if(cav_wrapper == self.scl_long_tuneup_controller.cav_wrappers[0]): 
					pos = 0.
				longTwissBucket = cav_wrapper.longTwissBucket
				twiss_arr = longTwissBucket.twiss_arr
				(alphaX,betaX,emittX) = (twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance()*1.0e+6)
				(alphaY,betaY,emittY) = (twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance()*1.0e+6)
				(alphaZ,betaZ,emittZ) = (twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance()*1.0e+6)
				sizeX = math.sqrt(emittX*betaX)
				sizeY = math.sqrt(emittY*betaY)					
				sizeZ = math.sqrt(emittZ*betaZ)	
				twiss_fit = longTwissBucket.long_Twiss_fit
				(alphaZ_fit,betaZ_fit,emittZ_fit) = (twiss_fit.getAlpha(),twiss_fit.getBeta(),twiss_fit.getEmittance()*1.0e+6)
				sizeZ_fit = math.sqrt(math.fabs(emittZ_fit*betaZ_fit))
				twiss_mtr = longTwissBucket.long_Twiss_matrix
				(alphaZ_mtr,betaZ_mtr,emittZ_mtr) = (twiss_mtr.getAlpha(),twiss_mtr.getBeta(),twiss_mtr.getEmittance()*1.0e+6)
				sizeZ_mtr = math.sqrt(math.fabs(emittZ_mtr*betaZ_mtr))	
				alphaZ_err = longTwissBucket.long_Twiss_arr_err[0]
				betaZ_err = longTwissBucket.long_Twiss_arr_err[1]
				emittZ_err = longTwissBucket.long_Twiss_arr_err[2]
				sizeZ_err = 0.
				if(sizeZ != 0.):
					sizeZ_err = longTwissBucket.long_Twiss_z2_zzp_zp2_err[0]*1.0e+6/(2*sizeZ)
					#print "debug cav=",cav_wrapper.alias," sizeZ=",sizeZ," sizeZ_err[mm]=",sizeZ_err
				beta = 0.
				gamma = 1.0
				sizeZ_deg = 0.
				sizeZ_fit_deg = 0.
				sizeZ_mtr_deg = 0.					
				if(eKin_in != 0.): 
					beta = math.sqrt(eKin_in*(eKin_in+2*mass))/(eKin_in+mass)
					gamma = 1.0/math.sqrt(1.-beta**2)					
					sizeZ_deg = 360.*sizeZ*1.0e-3*805.0e+6/(beta*c)
					sizeZ_fit_deg = 360.*sizeZ_fit*1.0e-3*805.0e+6/(beta*c)
					sizeZ_mtr_deg = 360.*sizeZ_mtr*1.0e-3*805.0e+6/(beta*c)
					sizeZ_err_deg = 360.*sizeZ_err*1.0e-3*805.0e+6/(beta*c)
					betaX /= gamma*beta
					betaY /= gamma*beta
					emittZ *= (gamma**3*beta*mass*360.*805.0e+6/c)*1.0e-6
					emittZ_fit *= (gamma**3*beta*mass*360.*805.0e+6/c)*1.0e-6 
					emittZ_mtr *= (gamma**3*beta*mass*360.*805.0e+6/c)*1.0e-6
					betaZ *= 360.*805.0e+6/(c*gamma**3*beta**3*mass)
					betaZ_fit *= 360.*805.0e+6/(c*gamma**3*beta**3*mass)
					betaZ_mtr *= 360.*805.0e+6/(c*gamma**3*beta**3*mass)
					betaZ_err *= 360.*805.0e+6/(c*gamma**3*beta**3*mass)
					emittZ_err *= (gamma**3*beta*mass*360.*805.0e+6/c)
				#-----------------------------------------
				if(emittX != 0.): self.hor_ver_gd_arr[0].addPoint(pos,sizeX)
				if(emittY != 0.): self.hor_ver_gd_arr[1].addPoint(pos,sizeY)
				if(emittX != 0.): self.hor_ver_gd_arr[2].addPoint(pos,betaX)
				if(emittY != 0.): self.hor_ver_gd_arr[3].addPoint(pos,betaY)
				#-----------------------------------------
				if(emittZ != 0.): self.long_gd_arr[0].addPoint(pos,sizeZ_deg)
				if(emittZ_fit != 0.): self.long_gd_arr[1].addPoint(pos,sizeZ_fit_deg,sizeZ_err_deg)
				if(emittZ_mtr != 0.): self.long_gd_arr[2].addPoint(pos,sizeZ_mtr_deg,sizeZ_err_deg)
				if(emittZ != 0.): self.long_gd_arr[3].addPoint(pos,emittZ)
				if(emittZ_fit != 0.): self.long_gd_arr[4].addPoint(pos,emittZ_fit,emittZ_err)
				if(emittZ_mtr != 0.): self.long_gd_arr[5].addPoint(pos,emittZ_mtr,emittZ_err)
				if(emittZ != 0.): self.long_gd_arr[6].addPoint(pos,betaZ)
				if(emittZ_fit != 0.): self.long_gd_arr[7].addPoint(pos,betaZ_fit,betaZ_err)
				if(emittZ_mtr != 0.): self.long_gd_arr[8].addPoint(pos,betaZ_mtr,betaZ_err)
				if(emittZ != 0.): self.long_gd_arr[9].addPoint(pos,alphaZ)
				if(emittZ_fit != 0.): self.long_gd_arr[10].addPoint(pos,alphaZ_fit,alphaZ_err)
				if(emittZ_mtr != 0.): self.long_gd_arr[11].addPoint(pos,alphaZ_mtr,alphaZ_err)
		self.updateGraphData()
					
			
	def updateGraphData(self):
		self.removeAllGraphData()
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		for ind in range(len(self.zParam_button_arr)):
			button = self.zParam_button_arr[ind]
			gd = self.long_gd_arr[ind]
			if(button.isSelected()): self.long_plot.addGraphData(gd)
		if(self.xSize_Button.isSelected()): self.hor_ver_plot.addGraphData(self.hor_ver_gd_arr[0])
		if(self.ySize_Button.isSelected()): self.hor_ver_plot.addGraphData(self.hor_ver_gd_arr[1])
		if(self.xBeta_Button.isSelected()): self.hor_ver_plot.addGraphData(self.hor_ver_gd_arr[2])
		if(self.yBeta_Button.isSelected()): self.hor_ver_plot.addGraphData(self.hor_ver_gd_arr[3])
	
class Quad_and_Cav_Parameters_Panel(JPanel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.setLayout(GridLayout(1,2))
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)
		#--------- quad and cavities params tables
		self.quad_fields_table_model = Quad_Fileds_Table_Model(scl_long_tuneup_controller)
		self.quad_fields_table = JTable(self.quad_fields_table_model)
		self.quad_fields_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.quad_fields_table.setFillsViewportHeight(true)	
		quad_table_panel = JPanel(BorderLayout())
		quad_table_panel.add(JScrollPane(self.quad_fields_table), BorderLayout.CENTER)		
		self.cav_amp_phases_table_model = Cav_Amp_Phases_Table_Model(scl_long_tuneup_controller)
		self.cav_amp_phases_table = JTable(self.cav_amp_phases_table_model)
		self.cav_amp_phases_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.cav_amp_phases_table.setFillsViewportHeight(true)	
		cav_table_panel = JPanel(BorderLayout())
		cav_table_panel.add(JScrollPane(self.cav_amp_phases_table), BorderLayout.CENTER)
		#---------------------------------------
		self.add(quad_table_panel)
		self.add(cav_table_panel)

#------------------------------------------------
#  JTable models
#------------------------------------------------
class Cavs_Input_Long_Twiss_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cavity","Use","Done"]
		self.columnNames += ["<html>&alpha;<SUB>fit</SUB><html>",]
		self.columnNames += ["<html>&beta;<SUB>fit</SUB>[mm/mrad]<html>",]
		self.columnNames += ["<html>&epsilon;<SUB>fit</SUB>[mm*mrad]<html>",]
		self.columnNames += ["<html>&alpha;<SUB>mtr</SUB><html>",]
		self.columnNames += ["<html>&beta;<SUB>mtr</SUB>[mm/mrad]<html>",]
		self.columnNames += ["<html>&epsilon;<SUB>mtr</SUB>[mm*mrad]<html>",]
		self.columnNames += ["<html>&delta;&alpha;<SUB>mtr</SUB><html>",]
		self.columnNames += ["<html>&delta;&beta;<SUB>mtr</SUB>[mm/mrad]<html>",]
		self.columnNames += ["<html>&delta;&epsilon;<SUB>mtr</SUB>[mm*mrad]<html>",]		
		self.columnNames += ["<html>&alpha;<SUB>calc</SUB><html>",]
		self.columnNames += ["<html>&beta;<SUB>calc</SUB>[mm/mrad]<html>",]
		self.columnNames += ["<html>&epsilon;<SUB>calc</SUB>[mm*mrad]<html>",]
		self.columnNames += ["Avg.Amp.Err.",]
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
		if(col == 0): 
			return cav_wrapper.alias
		if(col == 1): return cav_wrapper.isGood	
		if(col == 2): return cav_wrapper.longTwissBucket.isReady
		if(not cav_wrapper.isGood): return ""
		if(not cav_wrapper.longTwissBucket.isReady):
			if(col != 12 and col != 13 and col != 14):
				return ""
		if(not cav_wrapper.isAnalyzed): return ""
		longTwissBucket = cav_wrapper.longTwissBucket
		twiss = longTwissBucket.twiss_arr[2]
		if(col == 3 or col == 4 or col == 5): twiss = longTwissBucket.long_Twiss_fit
		if(col == 6 or col == 7 or col == 8): twiss = longTwissBucket.long_Twiss_matrix
		if(col == 3 or col == 6 or col == 12): return "%6.3f"%twiss.getAlpha()
		if(col == 4 or col == 7 or col == 13): return "%6.3f"%twiss.getBeta()
		if(col == 5 or col == 8 or col == 14): return "%6.3f"%(twiss.getEmittance()*1.0e+6)
		if(col == 9): return "%6.3f"%(longTwissBucket.long_Twiss_arr_err[0])
		if(col == 10): return "%6.3f"%(longTwissBucket.long_Twiss_arr_err[1])
		if(col == 11): return "%6.3f"%(longTwissBucket.long_Twiss_arr_err[2]*1.0e+6)
		if(col == 15): return "%6.3f"%cav_wrapper.longTwissBucket.fit_bpm_amp_avg_err
		return ""
				
	def getColumnClass(self,col):
		if(col == 1 or col == 2):
			return self.boolean_class
		return self.string_class		
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		pass

class LongTwissAnalysis_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.cav_wrapper = null
		self.bpm_wrappers = []
		self.bpm_wrappers_useInAmpBPMs = []
		self.columnNames = ["BPM"," UseInAnalysis"]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.bpm_wrappers[row]
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return self.bpm_wrappers_useInAmpBPMs[row]
		return ""
				
	def getColumnClass(self,col):
		if(col == 1):
			return self.boolean_class		
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 1 and row != 0):
			return true		
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 1 and row != 0):
			 self.bpm_wrappers_useInAmpBPMs[row] = value			
			 
class Cavity_Twiss_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cav",]
		self.columnNames += ["<html>&alpha;<SUB>ini</SUB><html>",]
		self.columnNames += ["<html>&beta;<SUB>ini</SUB>[m/rad]<html>",]
		self.columnNames += ["<html>&epsilon;<SUB>ini</SUB>[mm*mrad]<html>",]		
		self.rowNames = ["X Twiss","Y Twiss","Z Twiss","Z Fit Step"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 4

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0):
			return self.rowNames[row]
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		twiss_table = twiss_bpm_tables_panel.twiss_table
		cav_index = twiss_table.getSelectedRow()
		if(cav_index < 0):
			return ""
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_index]
		longTwissBucket = cav_wrapper.longTwissBucket
		if(row == 0 or row == 1 or row == 2):
			twiss =longTwissBucket.twiss_arr[row]
			if(col == 1):
				return "%7.4f"%twiss.getAlpha()
			if(col == 2):
				return "%7.4f"%twiss.getBeta()
			if(col == 3):
				return "%7.4f"%(twiss.getEmittance()*1.0e+6)
		return "%7.4f"%longTwissBucket.long_Twiss_arr_steps[col-1]
					
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 0): return false
		return true
			
	def setValueAt(self, value, row, col):
		if(col == 0): return
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		twiss_table = twiss_bpm_tables_panel.twiss_table
		cav_index = twiss_table.getSelectedRow()
		if(cav_index < 0): return
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_index]
		longTwissBucket = cav_wrapper.longTwissBucket
		if(row == 0 or row == 1 or row == 2):
			twiss =longTwissBucket.twiss_arr[row]
			alpha = twiss.getAlpha()
			beta = twiss.getBeta()
			emitt = twiss.getEmittance()
			if(col == 1):
				alpha =  Double.parseDouble(value)
			if(col == 2):
				beta = Double.parseDouble(value)
			if(col == 3):
				emitt = Double.parseDouble(value)*1.0e-6
			twiss.setTwiss(alpha,beta,emitt)
			twiss_table.getModel().fireTableDataChanged()
			twiss_table.setRowSelectionInterval(cav_index,cav_index)			
			return
		longTwissBucket.long_Twiss_arr_steps[col-1] = Double.parseDouble(value)
	
class Quad_Fileds_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Quad","Pos.[m]","Field [T/m]"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		quads = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder.quads
		return len(quads)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
		scl_quad_fields_dict_holder = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder
		quads = scl_quad_fields_dict_holder.quads
		quad = quads[row]
		quad_field_dict = scl_quad_fields_dict_holder.quad_field_dict
		if(col == 0): return quad.getId()
		if(col == 1): return " %8.3f "%scl_accSeq.getPosition(quad)
		if(col == 2): return " %8.4f "%quad_field_dict[quad]
		return ""
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		return false

class Cav_Amp_Phases_Table_Model(AbstractTableModel):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.columnNames = ["Cavity","Pos.[m]","Amp.[MV/m]","Phase[deg]"]
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.scl_long_tuneup_controller.cav_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
		if(col == 0): return cav_wrapper.alias
		if(col == 1): return " %8.3f "%cav_wrapper.pos
		if(col == 2): return " %8.4f "%cav_wrapper.designAmp
		if(col == 3): return " %7.2f "%cav_wrapper.designPhase
		return ""
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		return false
					
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		bpms_table_model = twiss_bpm_tables_panel.bpms_table_model	
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel		
		if(index < 0):
			bpms_table_model.cav_wrapper = null
			cavity_twiss_params_panel.cavTwiss_table.getModel().columnNames[0] = "Cavity"
			#cavity_twiss_params_panel.cavTwiss_table.getColumnModel().getColumn(0).setHeaderValue("Cavity")
			cavity_twiss_params_panel.cavTwiss_table.getModel().fireTableDataChanged()
			cavity_twiss_params_panel.cavTwiss_table.getModel().fireTableStructureChanged()
			return 
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[index]
		if(cav_wrapper.isGood):
			bpms_table_model.cav_wrapper = cav_wrapper		
			bpms_table_model.bpm_wrappers = cav_wrapper.bpm_wrappers
			bpms_table_model.bpm_wrappers_useInAmpBPMs = cav_wrapper.bpm_wrappers_useInAmpBPMs
		else:
			bpms_table_model.cav_wrapper = null		
			bpms_table_model.bpm_wrappers = []
			bpms_table_model.bpm_wrappers_useInPhaseAnalysis = []
		txt = cav_wrapper.alias
		twiss_bpm_tables_panel.bpm_table_border.setTitle("Cavity: "+txt)
		bpms_table_model.fireTableDataChanged()
		twiss_bpm_tables_panel.bpm_table_panel.repaint()
		cavity_twiss_params_panel.cavTwiss_table.getModel().columnNames[0] = txt
		#cavity_twiss_params_panel.cavTwiss_table.getColumnModel().getColumn(0).setHeaderValue(txt)
		cavity_twiss_params_panel.cavTwiss_table.getModel().fireTableDataChanged()
		cavity_twiss_params_panel.cavTwiss_table.getModel().fireTableStructureChanged()
		#---- graphs re-plotting
		bpm_amps_graph_panel = scl_long_twiss_analysis_controller.bpm_amps_graph_panel
		bpm_amps_graph_panel.updateGraphData()		
	
class BPMs_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller

	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		if(index < 0): return
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		bpm_amps_graph_panel = scl_long_twiss_analysis_controller.bpm_amps_graph_panel
		bpm_amps_graph_panel.updateGraphData()

class Init_Twiss_from_Design_Button_Listener(ActionListener):
	#This button will initialize the Twiss from the design
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")			
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		if(not scl_twiss_tracker_model.isGoodForAnalysis()):
			messageTextField.setText("Twiss Tracking. Not all cavities had not been analyzed. Fix it!")
			return
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
		current = cavity_twiss_params_panel.current_text.getValue()*0.001
		scl_twiss_tracker_model.setUpCavAmpPhasesFromPhaseAnalysis()
		scl_twiss_tracker_model.setUpQuads()		
		scl_twiss_tracker_model.scenario.unsetStartNode()
		scl_twiss_tracker_model.scenario.unsetStopNode()			
		env_probe = scl_twiss_tracker_model.getNewInitlProbe()
		env_probe.setBeamCurrent(current)
		traj = scl_twiss_tracker_model.runModel(env_probe)
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			elem_ind = traj.indicesForElement(cav_wrapper.cav.getGapsAsList().get(0).getId())[0] - 1
			if(cav_wrapper == cav_wrappers[0]): elem_ind = 0
			state = traj.stateWithIndex(elem_ind)
			twiss_arr = state.twissParameters()
			cav_wrapper.longTwissBucket.twiss_arr = twiss_arr
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss_arr[2].getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss_arr[2].getEmittance()*0.05*1.0e+6	
		scl_long_twiss_analysis_controller.updateTables()		

class Init_Twiss_from_LW_Button_Listener(ActionListener):
	#This button will initialize the Twiss from the LW data
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):	
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		if(not scl_twiss_tracker_model.isGoodForAnalysis()):
			messageTextField.setText("Twiss Tracking. Not all cavities had not been analyzed. Fix it!")
			return			
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
		current = cavity_twiss_params_panel.current_text.getValue()*0.001				
		scl_twiss_tracker_model.setUpCavAmpPhasesFromPhaseAnalysis()	
		scl_twiss_tracker_model.setUpQuads()		
		scl_twiss_tracker_model.scenario.unsetStartNode()
		scl_twiss_tracker_model.scenario.unsetStopNode()			
		env_probe = scl_twiss_tracker_model.getNewInitlProbe()
		#---- get twiss from LW analysis
		linac_wizard_document	= self.scl_long_tuneup_controller.linac_wizard_document
		tr_twiss_analysis_controller = linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		initial_twiss_params_holder = transverse_twiss_fitting_controller.initial_twiss_params_holder
		[alphaX,betaX,emittX] = initial_twiss_params_holder.getParams(0)
		[alphaY,betaY,emittY] = initial_twiss_params_holder.getParams(1)
		if(emittX == 0. or emittY == 0.):
			messageTextField.setText("Cannot get Twiss from LW Transverse analysis!")	
			return
		env_probe_state = env_probe.createProbeState()
		twiss_arr = env_probe_state.twissParameters()
		twiss_arr[0].setTwiss(alphaX,betaX,emittX*1.0e-6)
		twiss_arr[1].setTwiss(alphaY,betaY,emittY*1.0e-6)
		#set up long. Twiss from the cavity if it was initialized  
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[0]
		if(cav_wrapper.longTwissBucket.twiss_arr[2].getEmittance() != 0.):
			twiss_arr[2] = cav_wrapper.longTwissBucket.twiss_arr[2]
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss_arr[2].getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss_arr[2].getEmittance()*0.05*1.0e+6				
		env_probe.initFromTwiss(twiss_arr)
		env_probe.setBeamCurrent(current)
		traj = scl_twiss_tracker_model.runModel(env_probe)
		cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
		for cav_wrapper in cav_wrappers:
			elem_ind = traj.indicesForElement(cav_wrapper.cav.getGapsAsList().get(0).getId())[0] - 1
			if(cav_wrapper == cav_wrappers[0]): elem_ind = 0
			state = traj.stateWithIndex(elem_ind)
			cav_wrapper.longTwissBucket.twiss_arr = state.twissParameters()
			twiss_arr = cav_wrapper.longTwissBucket.twiss_arr
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss_arr[2].getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss_arr[2].getEmittance()*0.05*1.0e+6				
		scl_long_twiss_analysis_controller.updateTables()
	
class Copy_Fit_to_Calc_Button_Listener(ActionListener):
	#This button will copy the long. Twiss params from fit values to calculated ones
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel		
		twiss_table = twiss_bpm_tables_panel.twiss_table		
		cav_selected_inds = twiss_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0): 
			messageTextField.setText("Select one or more cavities to copy Twiss!")
			return
		for cav_table_ind in cav_selected_inds:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_table_ind]
			twiss = cav_wrapper.longTwissBucket.long_Twiss_fit
			cav_wrapper.longTwissBucket.twiss_arr[2].setTwiss(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance())
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss.getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss.getEmittance()*0.05*1.0e+6
		twiss_table.getModel().fireTableDataChanged()
	
class Copy_Mtrx_to_Calc_Button_Listener(ActionListener):
	#This button will copy the long. Twiss params from fit values to calculated ones
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel		
		twiss_table = twiss_bpm_tables_panel.twiss_table		
		cav_selected_inds = twiss_table.getSelectedRows()
		if(len(cav_selected_inds) < 1 or cav_selected_inds[0] < 0): 
			messageTextField.setText("Select one or more cavities to copy Twiss!")
			return
		for cav_table_ind in cav_selected_inds:
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_table_ind]
			twiss = cav_wrapper.longTwissBucket.long_Twiss_matrix
			cav_wrapper.longTwissBucket.twiss_arr[2].setTwiss(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance())
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss.getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss.getEmittance()*0.05*1.0e+6
		scl_long_twiss_analysis_controller.updateTables()
	
class Calc_Twiss_to_End_Button_Listener(ActionListener):
	#This button will start scan for the selected cavities only
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
		messageTextField.setText("")		
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel		
		cav_selected_inds = twiss_bpm_tables_panel.twiss_table.getSelectedRows()
		if(len(cav_selected_inds) != 1 or cav_selected_inds[0] < 0): 
			messageTextField.setText("Select one cavity to start tracking Twiss down!")
			return		
		if(not scl_twiss_tracker_model.isGoodForAnalysis()):
			messageTextField.setText("Twiss Tracking. Cavities had not been analyzed. Fix it!")
			return	
		cav_selected_ind = cav_selected_inds[0]
		cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
		current = cavity_twiss_params_panel.current_text.getValue()*0.001		
		env_probe = scl_twiss_tracker_model.getNewInitlProbe()
		#---- get twiss from the first cavity
		cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_selected_ind]
		twiss_arr = cav_wrapper.longTwissBucket.twiss_arr	
		[alphaX,betaX,emittX] = [twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance()]
		[alphaY,betaY,emittY] = [twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance()]
		[alphaZ,betaZ,emittZ] = [twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance()]
		if(emittX == 0. or emittY == 0. or emittZ == 0.):
			self.scl_long_tuneup_controller.getMessageTextField().setText("Cannot track! At Cav01a the emittance is zero!")	
			return
		scl_twiss_tracker_model.setTrackingSelectedCavToLastCav(cav_wrapper)
		scl_twiss_tracker_model.setUpQuads()
		twiss_arr = [Twiss(alphaX,betaX,emittX),Twiss(alphaY,betaY,emittY),Twiss(alphaZ,betaZ,emittZ)]
		eKin_in = cav_wrapper.eKin_in
		if(cav_selected_ind > 0):
			eKin_in = self.scl_long_tuneup_controller.cav_wrappers[cav_selected_ind-1].bpm_eKin_out
		env_probe.initFromTwiss(twiss_arr)
		env_probe.setBeamCurrent(current)
		env_probe.setKineticEnergy(eKin_in*1.0e+6)
		traj = scl_twiss_tracker_model.runModel(env_probe)
		#for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
		#	print "debug cav=",cav_wrapper.cav.getId()," amp=",cav_wrapper.cav.getDfltCavAmp()," phase=",cav_wrapper.cav.getDfltCavPhase()
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers[(cav_selected_ind+1):]:
			if(cav_wrapper == self.scl_long_tuneup_controller.cav_wrappers[0]): continue
			elem_ind = traj.indicesForElement(cav_wrapper.cav.getGapsAsList().get(0).getId())[0] - 1
			state = traj.stateWithIndex(elem_ind)
			#print "debug element=",state.getElementId()," pos=",state.getPosition()," gap_pos=",self.scl_long_tuneup_controller.scl_accSeq.getPosition(cav_wrapper.cav.getGapsAsList().get(0))
			cav_wrapper.longTwissBucket.twiss_arr = state.twissParameters()
			#print "debug eKinIn=",state.getKineticEnergy()/1.0e+6
			twiss_arr = cav_wrapper.longTwissBucket.twiss_arr
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[0] = 0.1
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[1] = twiss_arr[2].getBeta()*0.05
			cav_wrapper.longTwissBucket.long_Twiss_arr_steps[2] = twiss_arr[2].getEmittance()*0.05*1.0e+6				
		scl_long_twiss_analysis_controller.updateTables()		

class Select_BPMs_in_Analysis_Button_Listener(ActionListener):
	#This button will select BPM for analysis for the selected cavities 
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		twiss_table = twiss_bpm_tables_panel.twiss_table
		bpms_table = twiss_bpm_tables_panel.bpms_table
		cav_indexes = twiss_table.getSelectedRows()
		bpm_indexes = bpms_table.getSelectedRows()
		if(len(cav_indexes) <= 0 or len(bpm_indexes) <= 0): return
 		if(cav_indexes[0] < 0 or bpm_indexes[0] < 0): return
		for cav_index in cav_indexes:		
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_index]
			if(not cav_wrapper.isGood): continue
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				if(not bpm_ind in bpm_indexes): continue
				bpm_wrapper = cav_wrapper.bpm_wrappers[bpm_ind]
				if(not bpm_wrapper.isGood): continue
				cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = true
		bpms_table.getModel().fireTableDataChanged()				

class Deselect_BPMs_in_Analysis_Button_Listener(ActionListener):
	#This button will start analysis to the end
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		twiss_bpm_tables_panel = scl_long_twiss_analysis_controller.twiss_bpm_tables_panel
		twiss_table = twiss_bpm_tables_panel.twiss_table
		bpms_table = twiss_bpm_tables_panel.bpms_table
		cav_indexes = twiss_table.getSelectedRows()
		bpm_indexes = bpms_table.getSelectedRows()
		if(len(cav_indexes) <= 0 or len(bpm_indexes) <= 0): return
 		if(cav_indexes[0] < 0 or bpm_indexes[0] < 0): return
		for cav_index in cav_indexes:		
			cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[cav_index]
			if(not cav_wrapper.isGood): continue
			for bpm_ind in range(len(cav_wrapper.bpm_wrappers)):
				if(not bpm_ind in bpm_indexes): continue
				cav_wrapper.bpm_wrappers_useInAmpBPMs[bpm_ind] = false		
		bpms_table.getModel().fireTableDataChanged()

class Calibrate_BPM_Amps_Button_Listener(ActionListener):
	#This button will calibrate BPM amplitudes
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		if(not scl_twiss_tracker_model.isGoodForAnalysis()):
			self.scl_long_tuneup_controller.setText("BPM calibration. Not all phase scans of the cavities had not been analyzed. Fix it!")
			return	
		scl_twiss_tracker_model.calibrateBPMs()	
		
class Read_BPM_Calibration_Button_Listener(ActionListener):
	#This button will read BPM amps. calibration data from the external file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read BPM amps. calibration data from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			fl_path = fl_in.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_in.getPath()+".dat"
			fl_in = open(fl_path,"r")
			lns = fl_in.readlines()[1:]
			fl_in.close()
			scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
			scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model	
			cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
			scl_twiss_tracker_model.bpm_calib_dict = {}
			current = cavity_twiss_params_panel.current_text.getValue()
			for ln in lns:
				res_arr = ln.split()
				if(len(res_arr) == 5):
					name = res_arr[0]
					amp_avg = float(res_arr[1])*current
					amp_err = float(res_arr[2])*current
					bessel = float(res_arr[3])
					bore_r = float(res_arr[4])
					bpm_wrapper = self.scl_long_tuneup_controller.getBPM_Wrapper(name)
					scl_twiss_tracker_model.bpm_calib_dict[bpm_wrapper] = [amp_avg,amp_err,bessel,bore_r]
		
class Export_BPM_Calibration_Button_Listener(ActionListener):
	#This button will write BPM amps. calibration data to the external file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")			
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save BPM amps. calibration data into ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")
			scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
			scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model	
			cavity_twiss_params_panel = scl_long_twiss_analysis_controller.cavity_twiss_params_panel
			current = cavity_twiss_params_panel.current_text.getValue()
			fl_out.write("% name amp_avg_norm amp_err_norm bessel bore_r")
			for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
				if(scl_twiss_tracker_model.bpm_calib_dict.has_key(bpm_wrapper)):
					[amp_avg,amp_err,bessel,bore_r] = scl_twiss_tracker_model.bpm_calib_dict[bpm_wrapper]
					txt = bpm_wrapper.alias+" "+str(amp_avg/current)+" "+str(amp_err/current)+" "+str(bessel)+" "+str(bore_r)
					fl_out.write(txt+"\n")
			fl_out.close()

class Dump_Twiss_Data_Button_Listener(ActionListener):
	#This button will write Twiss data for all cavities to the external ASCII file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Twiss Analysis Data into ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")
			scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
			scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model	
			txt = "# cavity pos[m]  EkinInBPM EkinOutBPM  EkinOutModel "
			txt += " alphaX betaX emittX "
			txt += " alphaY betaY emittY "
			txt += " alphaZ betaZ emittZ "
			txt += " alphaZ_fit betaZ_fit emittZ_fit "			
			txt += " alphaZ_mtr betaZ_mtr emittZ_mtr "
			txt += " alphaZ_err betaZ_err emittZ_err "
			txt += " z2[mm^2] zzp[mm*mrad] zp2[mrad^2] "
			txt += " z2_err zzp_err zp2_err "
			txt += " bpm_amp_err "
			txt += " sizeX[mm] sizeY[mm] sizeZ[mm]  sizeZ[deg] sizeZ_fit[deg] sizeZ_mtr[deg]  "
			fl_out.write(txt+"\n")
			eKin_in = self.scl_long_tuneup_controller.cav_wrappers[0].eKin_in
			mass = self.scl_long_tuneup_controller.mass/1.0e+6
			c = 299792458.
			count = 0
			for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
				count += 1
				txt = ""
				if(cav_wrapper.isGood):
					pos = self.scl_long_tuneup_controller.scl_accSeq.getPosition(cav_wrapper.cav.getGapsAsList()[0])
					#pos = cav_wrapper.pos
					if(cav_wrapper == self.scl_long_tuneup_controller.cav_wrappers[0]): pos = 0.
					txt += " %3d "%count + cav_wrapper.alias + " %9.4f "%pos 
					txt += " %9.4f "%eKin_in + " %9.4f "%cav_wrapper.bpm_eKin_out + " %9.4f "%cav_wrapper.model_eKin_out
					longTwissBucket = cav_wrapper.longTwissBucket
					twiss_arr = longTwissBucket.twiss_arr
					(alphaX,betaX,emittX) = (twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance()*1.0e+6)
					(alphaY,betaY,emittY) = (twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance()*1.0e+6)
					(alphaZ,betaZ,emittZ) = (twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance()*1.0e+6)
					sizeX = math.sqrt(emittX*betaX)
					sizeY = math.sqrt(emittY*betaY)					
					sizeZ = math.sqrt(emittZ*betaZ)
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaX,betaX,emittX)
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaY,betaY,emittY)
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaZ,betaZ,emittZ)
					twiss_fit = longTwissBucket.long_Twiss_fit
					(alphaZ,betaZ,emittZ) = (twiss_fit.getAlpha(),twiss_fit.getBeta(),twiss_fit.getEmittance()*1.0e+6)
					sizeZ_fit = math.sqrt(emittZ*betaZ)
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaZ,betaZ,emittZ)
					twiss_mtr = longTwissBucket.long_Twiss_matrix
					(alphaZ,betaZ,emittZ) = (twiss_mtr.getAlpha(),twiss_mtr.getBeta(),twiss_mtr.getEmittance()*1.0e+6)
					sizeZ_mtr = math.sqrt(emittZ*betaZ)
					z2 = emittZ*betaZ
					zzp = -alphaZ*emittZ
					zp2 = 0.
					if(betaZ != 0.): zp2 = (1.0+alphaZ**2)*emittZ/betaZ					
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaZ,betaZ,emittZ)					
					[alphaZ_err,betaZ_err,emittZ_err] = longTwissBucket.long_Twiss_arr_err
					txt += "  %8.5f  %8.5f  %8.5f  "%(alphaZ_err,betaZ_err,emittZ_err)
					[z2_err,zzp_err,zp2_err] = longTwissBucket.long_Twiss_z2_zzp_zp2_err
					txt += "  %8.5f  %8.5f  %8.5f  "%(z2,zzp,zp2)
					#----- z2,zzp,zp2 in [mm] and [mrad]
					txt += "  %8.5f  %8.5f  %8.5f  "%(z2_err*1.0e+6,zzp_err*1.0e+6,zp2_err*1.0e+6)
					txt += "  %5.3f  "%longTwissBucket.fit_bpm_amp_avg_err
					txt += ""
					sizeZ_deg = 0.
					sizeZ_fit_deg = 0.
					sizeZ_mtr_deg = 0.
					eKin = eKin_in
					beta = math.sqrt(eKin*(eKin+2*mass))/(eKin+mass)
					if(eKin != 0): 
						sizeZ_deg = 360.*sizeZ*1.0e-3*805.0e+6/(beta*c)
						sizeZ_fit_deg = 360.*sizeZ_fit*1.0e-3*805.0e+6/(beta*c)
						sizeZ_mtr_deg = 360.*sizeZ_mtr*1.0e-3*805.0e+6/(beta*c)
					txt += " %5.2f %5.2f %5.2f   %5.2f %5.2f %5.2f "%(sizeX,sizeY,sizeZ,sizeZ_deg,sizeZ_fit_deg,sizeZ_mtr_deg)
					fl_out.write(txt+"\n")
					eKin_in = cav_wrapper.bpm_eKin_out
			fl_out.close()		

class Dump_Cav_Amp_Phases_Data_Button_Listener(ActionListener):
	#This button will write cavities amp. and phases data to the external ASCII file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Cav. Amp. and Phases into ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")
			for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
				txt = cav_wrapper.cav.getId()
				txt += " %12.5g "%cav_wrapper.designAmp + " %8.3f "%cav_wrapper.designPhase 
				fl_out.write(txt+"\n")
			fl_out.close()

class Dump_Quad_Fields_Button_Listener(ActionListener):
	#This button will write quad field data to the external ASCII file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Save Quad Fields into ASCII file")
		fc.setApproveButtonText("Save")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_out = fc.getSelectedFile()
			fl_path = fl_out.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_out.getPath()+".dat"
			fl_out = open(fl_path,"w")
			scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
			quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
			scl_quad_fields_dict_holder = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder
			quad_field_dict = scl_quad_fields_dict_holder.quad_field_dict
			for quad in quads:
				field = quad.getDfltField()
				if(quad_field_dict.has_key(quad)): field = quad_field_dict[quad]
				txt = quad.getId() + " %8.4f "%field
				fl_out.write(txt+"\n")
			fl_out.close()
			
class Read_Quad_Fields_Button_Listener(ActionListener):
	#This button will read quad field data from the external ASCII file
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		fc = JFileChooser(constants_lib.const_path_dict["LINAC_WIZARD_FILES_DIR_PATH"])
		fc.setDialogTitle("Read Quad Fields from ASCII file")
		fc.setApproveButtonText("Read")
		fl_filter = FileNameExtensionFilter("ASCII *.dat File",["dat",])
		fc.setFileFilter(fl_filter)
		returnVal = fc.showOpenDialog(self.scl_long_tuneup_controller.linac_wizard_document.linac_wizard_window.frame)
		if(returnVal == JFileChooser.APPROVE_OPTION):
			fl_in = fc.getSelectedFile()
			fl_path = fl_in.getPath()
			if(fl_path.rfind(".dat") != (len(fl_path) - 4)):
				fl_path = fl_in.getPath()+".dat"
			fl_in = open(fl_path,"r")
			lns = fl_in.readlines()
			fl_in.close()
			quad_name_field_tmp_dict = {}		
			for ln in lns:
				res_arr = ln.split()
				if(len(res_arr) == 2):
					quad_name_field_tmp_dict[res_arr[0]]= float(res_arr[1])
			scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq	
			quads = scl_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType))).andStatus(true))
			scl_quad_fields_dict_holder = self.scl_long_tuneup_controller.scl_long_tuneup_init_controller.scl_quad_fields_dict_holder
			quad_field_dict = scl_quad_fields_dict_holder.quad_field_dict
			for quad in quads:
				if(quad_name_field_tmp_dict.has_key(quad.getId())):
					field = quad_name_field_tmp_dict[quad.getId()]
					quad_field_dict[quad] = field
					quad.setDfltField(field)
			scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller		
			scl_long_twiss_analysis_controller.updateTables()

class Start_Analysis_Button_Listener(ActionListener):
	#This button will start analysis to the end or for the selected cavities only
	def __init__(self,scl_long_tuneup_controller, run_to_end	= true):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		self.run_to_end = run_to_end		
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		analysis_state_controller = scl_long_twiss_analysis_controller.analysis_state_controller
		analysis_state_controller.setShouldStop(false)			
		runner = Analysis_Runner(self.scl_long_tuneup_controller,self.run_to_end)
		thr = Thread(runner)
		thr.start()			

class Enough_Analysis_Button_Listener(ActionListener):						
	#This button action will stop the analysis for the one cavity
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		if(scl_twiss_tracker_model.solver != null):
			scl_twiss_tracker_model.solver.stopSolving()

class Stop_Analysis_Button_Listener(ActionListener):						
	#This button action will stop the analysis
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		analysis_state_controller = scl_long_twiss_analysis_controller.analysis_state_controller
		analysis_state_controller.setShouldStop(true)
		scl_twiss_tracker_model = scl_long_twiss_analysis_controller.scl_twiss_tracker_model
		if(scl_twiss_tracker_model.solver != null):
			scl_twiss_tracker_model.solver.stopSolving()

class Beam_Size_Replot_Button_Listener(ActionListener):						
	#This button action will stop the analysis
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_long_twiss_analysis_controller.beam_sizes_panel.recalculateGraphs()
		scl_long_twiss_analysis_controller.updateTables()		
		
class Beam_Size_Radio_Buttons_Listener(ActionListener):						
	#This button action will stop the analysis
	def __init__(self,scl_long_tuneup_controller):
		self.scl_long_tuneup_controller = scl_long_tuneup_controller
		
	def actionPerformed(self,actionEvent):
		self.scl_long_tuneup_controller.getMessageTextField().setText("")
		scl_long_twiss_analysis_controller = self.scl_long_tuneup_controller.scl_long_twiss_analysis_controller
		scl_long_twiss_analysis_controller.beam_sizes_panel.updateGraphData()
		scl_long_twiss_analysis_controller.updateTables()
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class SCL_Long_Twiss_Analysis_Controller:
	def __init__(self,scl_long_tuneup_controller):
		#--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
		self.scl_long_tuneup_controller = 	scl_long_tuneup_controller
		self.analysis_state_controller = AnalysisStateController()
		self.main_panel = JPanel(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()			
		#------top params panel-----------------------
		top_panel = JPanel(BorderLayout())
		#------ one cavity initial Twiss
		self.cavity_twiss_params_panel = CavityTwissParams_Panel(self.scl_long_tuneup_controller)
		#------ start stop analysis panel
		self.start_stop_panel = StartStopAnalysis_Panel(self.scl_long_tuneup_controller)
		top_panel.add(self.cavity_twiss_params_panel,BorderLayout.NORTH)
		top_panel.add(self.start_stop_panel,BorderLayout.SOUTH)
		#------table panel --------
		center_panel = JPanel(BorderLayout())
		self.twiss_bpm_tables_panel = LongTwissParamsTable_Panel(self.scl_long_tuneup_controller)
		center_panel.add(self.twiss_bpm_tables_panel,BorderLayout.CENTER)
		#-------- bottom panel
		bottom_panel = JPanel(BorderLayout())
		self.bpm_amps_graph_panel = BPM_Amps_Graph_Panel(self.scl_long_tuneup_controller)
		self.beam_sizes_panel = Beam_Sizes_Panel(self.scl_long_tuneup_controller)
		self.quad_and_cav_parameters_panel = Quad_and_Cav_Parameters_Panel(self.scl_long_tuneup_controller)
		bottom_tabbedPane = JTabbedPane()
		bottom_tabbedPane.add("BPM Amps. Plots",self.bpm_amps_graph_panel)
		bottom_tabbedPane.add("Beam Sizes Plots",self.beam_sizes_panel)
		bottom_tabbedPane.add("Quads and Cavities Parameters",self.quad_and_cav_parameters_panel)
		bottom_panel.add(bottom_tabbedPane,BorderLayout.CENTER)
		#---- split panel
		split_panel = JSplitPane(JSplitPane.VERTICAL_SPLIT,center_panel,bottom_panel)
		split_panel.setDividerLocation(0.5)
		split_panel.setResizeWeight(0.5)
		#--------------------------------------------------
		self.main_panel.add(top_panel,BorderLayout.NORTH)
		self.main_panel.add(split_panel,BorderLayout.CENTER)
		#----- model for tracking 	
		self.scl_twiss_tracker_model = SCL_Twiss_Tracker_Model(self.scl_long_tuneup_controller)
		
	def getMainPanel(self):
		return self.main_panel
		
	def updateTables(self):
		self.twiss_bpm_tables_panel.twiss_table.getModel().fireTableDataChanged()
		self.quad_and_cav_parameters_panel.quad_fields_table_model.fireTableDataChanged()
		self.quad_and_cav_parameters_panel.cav_amp_phases_table_model.fireTableDataChanged()
		
	def clean(self):
		self.scl_twiss_tracker_model.clean()	
		self.start_stop_panel.chi2_txt.setValue(0.)
		
	def isWorthToSave(self):
		for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
			if(cav_wrapper.longTwissBucket.isReady):
				return true
		return false
		
	def writeDataToXML(self,root_da):
		if(self.isWorthToSave()):
			scl_long_twiss_da = root_da.createChild("SCL_Longitudinal_Twiss_Analysis_Data")
			scl_long_twiss_params_da = scl_long_twiss_da.createChild("PARAMETERS")
			current = self.cavity_twiss_params_panel.current_text.getValue()
			scl_long_twiss_params_da.setValue("current",current)
			scl_long_twiss_calib_da = scl_long_twiss_da.createChild("BPM_Calibration")
			for bpm_wrapper in self.scl_long_tuneup_controller.bpm_wrappers:
				if(self.scl_twiss_tracker_model.bpm_calib_dict.has_key(bpm_wrapper)):
					[amp_avg,amp_err,bessel,bore_r] = self.scl_twiss_tracker_model.bpm_calib_dict[bpm_wrapper]
					scl_long_twiss_bpm_calib_da = scl_long_twiss_calib_da.createChild("BPM")
					scl_long_twiss_bpm_calib_da.setValue("name",bpm_wrapper.alias)
					scl_long_twiss_bpm_calib_da.setValue("amp_avg",amp_avg)
					scl_long_twiss_bpm_calib_da.setValue("amp_err",amp_err)
					scl_long_twiss_bpm_calib_da.setValue("bessel",bessel)
					scl_long_twiss_bpm_calib_da.setValue("bore_r",bore_r)
			scl_long_twiss_bucket_da = scl_long_twiss_da.createChild("Cavities_Long_Twiss_Buckets")
			for cav_wrapper in self.scl_long_tuneup_controller.cav_wrappers:
				cav_wrapper.longTwissBucket.writeDataToXML(scl_long_twiss_bucket_da)
				
	def readDataFromXML(self,scl_phase_scan_da):		
		#------ read data from the longitudinal Twiss analysis controller
		list_da = scl_phase_scan_da.childAdaptors("SCL_Longitudinal_Twiss_Analysis_Data")
		if(not list_da.isEmpty()):
			scl_long_twiss_da = list_da.get(0)
			#---- get general Twiss controller parameters
			scl_long_twiss_params_da = scl_long_twiss_da.childAdaptor("PARAMETERS")
			current = scl_long_twiss_params_da.doubleValue("current")
			self.cavity_twiss_params_panel.current_text.setValue(current)
			scl_long_twiss_calib_da = scl_long_twiss_da.childAdaptor("BPM_Calibration")
			self.scl_twiss_tracker_model.bpm_calib_dict = {}
			if(scl_long_twiss_calib_da != null):
				for bpm_calibration_da in scl_long_twiss_calib_da.childAdaptors("BPM"):
					bpm_wrapper = self.scl_long_tuneup_controller.getBPM_Wrapper(bpm_calibration_da.stringValue("name"))
					amp_avg = bpm_calibration_da.doubleValue("amp_avg")
					amp_err = bpm_calibration_da.doubleValue("amp_err")
					bessel = bpm_calibration_da.doubleValue("bessel")
					bore_r = bpm_calibration_da.doubleValue("bore_r")
					self.scl_twiss_tracker_model.bpm_calib_dict[bpm_wrapper] = [amp_avg,amp_err,bessel,bore_r]
			scl_long_twiss_bucket_da = scl_long_twiss_da.childAdaptor("Cavities_Long_Twiss_Buckets")
			for cav_long_twiss_da in scl_long_twiss_bucket_da.childAdaptors("Cavity_Long_Twiss_Bucket"):
				cav_wrapper = self.scl_long_tuneup_controller.getCav_Wrapper(cav_long_twiss_da.stringValue("cav"))
				cav_wrapper.longTwissBucket.readDataFromXML(cav_long_twiss_da,self.scl_long_tuneup_controller)
			
		
		
