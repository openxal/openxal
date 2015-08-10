# The collection of classes for initial transverse Twiss parameters fitting

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.awt.event import ActionEvent, ActionListener

from java.util import List, ArrayList

from Jama import Matrix
from Jama import EigenvalueDecomposition

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel 
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.model.probe import EnvelopeProbe

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


from ws_lw_acquisition_cntrl_lib import WS_DIRECTION_HOR, WS_DIRECTION_VER, WS_DIRECTION_NULL

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class Init_Twiss_Fitter_Runner(Runnable):
	
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document
	
	def run(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		twiss_fitter = transverse_twiss_fitting_controller.twiss_fitter
		twiss_fitter.run()
		transverse_twiss_fitting_controller.twiss_fitter = null

#--------------------------------------------------------------
# Listeners to the transverse_twiss_analysis_subpanel_cntrl
#--------------------------------------------------------------

class Twiss_Fitting_Listener(ActionListener):
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document

	def actionPerformed(self,actionEvent):
		self.startFitting()

	def startFitting(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		twiss_fitter = transverse_twiss_fitting_controller.twiss_fitter
		if(twiss_fitter != null): return
		transverse_twiss_fitting_controller.twiss_fitter = Twiss_Fitter(self.linac_wizard_document)
		runner = Init_Twiss_Fitter_Runner(self.linac_wizard_document)
		thr = Thread(runner)
		thr.start()		
		
class One_Pass_Listener(ActionListener):
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document
		
	def actionPerformed(self,actionEvent):	
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		initial_twiss_params_holder = transverse_twiss_fitting_controller.initial_twiss_params_holder	
		(alphaX, betaX, emittX) = initial_twiss_params_holder.getParams(0)
		(alphaY, betaY, emittY) = initial_twiss_params_holder.getParams(1)
		(alphaZ, betaZ, emittZ) = initial_twiss_params_holder.getParams(2)
		if(emittX == 0. or emittY == 0. or emittZ == 0.): return
		twiss_arr = []
		twiss_arr.append(Twiss(alphaX, betaX, emittX))
		twiss_arr.append(Twiss(alphaY, betaY, emittY))
		twiss_arr.append(Twiss(alphaZ, betaZ, emittZ))		
		scorer = AccScoreCalculator(self.linac_wizard_document,null,twiss_arr)
		scorer.count = 0
		scorer.min_diff2 = scorer.calculateDiff2()
		scorer.updateGUI_Elements()
		scorer.runErrorsCalculator()		
	
class Stop_Twiss_Fitting_Listener(ActionListener):
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document
	
	def actionPerformed(self,actionEvent):	
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		twiss_fitter = transverse_twiss_fitting_controller.twiss_fitter
		if(twiss_fitter != null):
			twiss_fitter.stopFitting()

#----------------------------------------------------------------
#   Solvers and Fitters
#----------------------------------------------------------------
class Twiss_Fitter:
	""" The instance of this class is created on-fly by start fitting button """
	def __init__(self,linac_wizard_document):
		self.linac_wizard_document = linac_wizard_document
		self.solver = null
                              
	def run(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller
		initial_twiss_params_holder = transverse_twiss_fitting_controller.initial_twiss_params_holder
		final_twiss_params_holder = init_and_fit_params_controller.final_twiss_params_holder
		nIterations = int(init_and_fit_params_controller.fit_iter_text.getValue())	
		#print "debug Twiss_Fitter start to run! Iter=",nIterations
		(alphaX, betaX, emittX) = initial_twiss_params_holder.getParams(0)
		(alphaY, betaY, emittY) = initial_twiss_params_holder.getParams(1)
		(alphaZ, betaZ, emittZ) = initial_twiss_params_holder.getParams(2)
		twiss_arr = []
		twiss_arr.append(Twiss(alphaX, betaX, emittX))
		twiss_arr.append(Twiss(alphaY, betaY, emittY))
		twiss_arr.append(Twiss(alphaZ, betaZ, emittZ))
		(alphaXStep, betaXStep, emittXStep) = initial_twiss_params_holder.getParamsStep(0)
		(alphaYStep, betaYStep, emittYStep) = initial_twiss_params_holder.getParamsStep(1)
		(alphaZStep, betaZStep, emittZStep) = initial_twiss_params_holder.getParamsStep(2)	
		variables = ArrayList()
		variables.add(Variable("alphaX", alphaX, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("betaX",   betaX, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("emittX", emittX, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("alphaY", alphaY, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("betaY",   betaY, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("emittY", emittY, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("alphaZ", alphaZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("betaZ",   betaZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		variables.add(Variable("emittZ", emittZ, - Double.MAX_VALUE, Double.MAX_VALUE))
		delta_hint = InitialDelta()
		variables_fit = ArrayList()
		#---------- X
		if(alphaXStep != 0.): 
			variables_fit.add(variables.get(0))
			delta_hint.addInitialDelta(variables.get(0), alphaXStep)
		if(betaXStep != 0.): 
			variables_fit.add(variables.get(1))
			delta_hint.addInitialDelta(variables.get(1), betaXStep)
		if(emittXStep != 0.): 
			variables_fit.add(variables.get(2))
			delta_hint.addInitialDelta(variables.get(2), emittXStep)
		#---------- Y
		if(alphaYStep != 0.): 
			variables_fit.add(variables.get(3))
			delta_hint.addInitialDelta(variables.get(3), alphaYStep)
		if(betaYStep != 0.): 
			variables_fit.add(variables.get(4))
			delta_hint.addInitialDelta(variables.get(4), betaYStep)
		if(emittYStep != 0.): 
			variables_fit.add(variables.get(5))
			delta_hint.addInitialDelta(variables.get(5), emittYStep)			
		#---------- Z
		if(alphaZStep != 0.): 
			variables_fit.add(variables.get(6))
			delta_hint.addInitialDelta(variables.get(6), alphaZStep)
		if(betaZStep != 0.): 
			variables_fit.add(variables.get(7))
			delta_hint.addInitialDelta(variables.get(7), betaZStep)
		if(emittZStep != 0.): 
			variables_fit.add(variables.get(8))
			delta_hint.addInitialDelta(variables.get(8), emittZStep)	
		#------- fitting process with solver
		if(variables_fit.isEmpty()):
			return
		scorer = AccScoreCalculator(self.linac_wizard_document,variables,twiss_arr)
		maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(nIterations) 
		solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)
		self.solver = solver
		problem = ProblemFactory.getInverseSquareMinimizerProblem(variables_fit,scorer,0.001)
		problem.addHint(delta_hint)
		solver.solve(problem)
		#------- get results
		trial = solver.getScoreBoard().getBestSolution()
		scorer.trialToTwiss(trial)
		twiss_arr = scorer.getTwissArr()
		(alphaX, betaX, emittX) = (twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance())
		(alphaY, betaY, emittY) = (twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance())
		(alphaZ, betaZ, emittZ) = (twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance())			
		final_twiss_params_holder.setParams(0,alphaX, betaX, emittX)
		final_twiss_params_holder.setParams(1,alphaY, betaY, emittY)
		final_twiss_params_holder.setParams(2,alphaZ, betaZ, emittZ)
		init_and_fit_params_controller.finalTwiss_table.getModel().fireTableDataChanged()
		init_and_fit_params_controller.fit_iter_left_text.setValue(0.)
		scorer.setUpLastNode(false)
		scorer.calculateDiff2()
                          
	def stopFitting(self):
		#print "debug stop Twiss_Fitter!"
		if(self.solver != null):
			self.solver.stopSolving()

class AccScoreCalculator(Scorer):
	def __init__(self,linac_wizard_document,variables,twiss_arr):
		self.linac_wizard_document = linac_wizard_document
		self.variables = variables
		self.twiss_arr = twiss_arr
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller		
		self.count = int(init_and_fit_params_controller.fit_iter_text.getValue())
		self.count_txt = init_and_fit_params_controller.fit_iter_left_text
		self.min_diff2 = 1.0e+36		
		self.updateGUI_Elements()
		self.setUpLastNode()
		tr_twiss_analysis_controller.accStatesKeeper.resyncScenario()
		self.eKin = init_and_fit_params_controller.eKin_text.getValue()
		self.current = init_and_fit_params_controller.current_text.getValue()
		
	def setUpLastNode(self, setUp = true):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		if(setUp):
			node_max_with_max_pos = null
			max_pos = 0.
			for accState in accStatesKeeper.getAccStatesArr():
				if(not accState.isOn): continue
				for size_record in accState.size_hor_record_arr:
					if(not size_record.isOn): continue
					if(max_pos < size_record.pos):
						max_pos = size_record.pos
						node_max_with_max_pos =size_record.ws_node
				for size_record in accState.size_ver_record_arr:
					if(not size_record.isOn): continue
					if(max_pos < size_record.pos):
						max_pos = size_record.pos
						node_max_with_max_pos = size_record.ws_node
			#print "debug stop node=",node_max_with_max_pos.getId()
			for accState in accStatesKeeper.getAccStatesArr():
				if(node_max_with_max_pos != null):
					accState.scenario	.setStopNode(node_max_with_max_pos.getId())
				else:
					accState.scenario	.unsetStopNode()
		else:
			for accState in accStatesKeeper.getAccStatesArr():
				accState.scenario	.unsetStopNode()						

	def updateGUI_Elements(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller
		final_twiss_params_holder = init_and_fit_params_controller.final_twiss_params_holder	
		twissArr = self.twiss_arr
		(alphaX, betaX, emittX) = (twissArr[0].getAlpha(),twissArr[0].getBeta(),twissArr[0].getEmittance())
		(alphaY, betaY, emittY) = (twissArr[1].getAlpha(),twissArr[1].getBeta(),twissArr[1].getEmittance())
		(alphaZ, betaZ, emittZ) = (twissArr[2].getAlpha(),twissArr[2].getBeta(),twissArr[2].getEmittance())		
		final_twiss_params_holder.setParams(0,alphaX, betaX, emittX)
		final_twiss_params_holder.setParams(1,alphaY, betaY, emittY)
		final_twiss_params_holder.setParams(2,alphaZ, betaZ, emittZ)
		final_twiss_params_holder.setParamsErr(0,0.,0.,0.)
		final_twiss_params_holder.setParamsErr(1,0.,0.,0.)
		final_twiss_params_holder.setParamsErr(2,0.,0.,0.)		
		init_and_fit_params_controller.finalTwiss_table.getModel().fireTableDataChanged()
		self.count_txt.setValue(1.0*self.count)
		init_and_fit_params_controller.avg_diff_text.setValue(math.sqrt(self.min_diff2))
		self.updateGraphs()
		
	def updateGraphs(self):
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		tr_twiss_analysis_controller.graphs_panel.removeAllGraphData()
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper		
		n_points = 150
		for accState in accStatesKeeper.getAccStatesArr():
			if(accState.isOn):
				accState.gd_model_hor.removeAllPoints()
				accState.gd_model_ver.removeAllPoints()
				accState.gd_model_lon.removeAllPoints()
				traj = accState.traj				
				if(traj == null): continue				
				final_state = traj.finalState()
				max_pos = final_state.getPosition()
				step = max_pos/(n_points-1)
				for i in range(n_points):
					pos = step*i
					state = traj.stateNearestPosition(pos)
					pos = state.getPosition()
					if(state == null):
						continue
					size_theory_x = state.twissParameters()[0].getEnvelopeRadius()*1000.
					size_theory_y = state.twissParameters()[1].getEnvelopeRadius()*1000.
					size_theory_z = state.twissParameters()[2].getEnvelopeRadius()
					gamma = state.getGamma()
					beta = math.sqrt(1.0 - 1.0/gamma**2)
					c = 299792458.
					z_size = size_theory_z*360.*402.5e+6/(c*beta)
					accState.gd_model_hor.addPoint(pos,size_theory_x)
					accState.gd_model_ver.addPoint(pos,size_theory_y)
					accState.gd_model_lon.addPoint(pos,z_size)
		tr_twiss_analysis_controller.graphs_panel.updateGraphData()
		
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
	
	def getTwissArr(self):
		return self.twiss_arr
	
	def trialToTwiss(self,trial):
		#---	set up initial Twiss	from Trial
		twissArr = self.twiss_arr
		variables = self.variables
		(alphaX, betaX, emittX) = (twissArr[0].getAlpha(),twissArr[0].getBeta(),twissArr[0].getEmittance())
		(alphaY, betaY, emittY) = (twissArr[1].getAlpha(),twissArr[1].getBeta(),twissArr[1].getEmittance())
		(alphaZ, betaZ, emittZ) = (twissArr[2].getAlpha(),twissArr[2].getBeta(),twissArr[2].getEmittance())				
		var_map = trial.getTrialPoint().getValueMap()
		if(var_map.containsKey(variables.get(0))): alphaX =  trial.getTrialPoint().getValue(variables.get(0))
		if(var_map.containsKey(variables.get(1))): betaX =  math.fabs(trial.getTrialPoint().getValue(variables.get(1)))
		if(var_map.containsKey(variables.get(2))): emittX =  math.fabs(trial.getTrialPoint().getValue(variables.get(2)))
		if(var_map.containsKey(variables.get(3))): alphaY =  trial.getTrialPoint().getValue(variables.get(3))
		if(var_map.containsKey(variables.get(4))): betaY =  math.fabs(trial.getTrialPoint().getValue(variables.get(4)))
		if(var_map.containsKey(variables.get(5))): emittY =  math.fabs(trial.getTrialPoint().getValue(variables.get(5)))
		if(var_map.containsKey(variables.get(6))): alphaZ =  trial.getTrialPoint().getValue(variables.get(6))
		if(var_map.containsKey(variables.get(7))): betaZ =  math.fabs(trial.getTrialPoint().getValue(variables.get(7)))
		if(var_map.containsKey(variables.get(8))): emittZ =  math.fabs(trial.getTrialPoint().getValue(variables.get(8)))
		self.twiss_arr[0].setTwiss(alphaX, betaX, emittX)
		self.twiss_arr[1].setTwiss(alphaY, betaY, emittY)
		self.twiss_arr[2].setTwiss(alphaZ, betaZ, emittZ)			

	def calculateDiff2(self):
		if(self.eKin == 0.): return 0.
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		fit_param_index = tr_twiss_analysis_controller.fit_param_index
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		#---	set up initial Twiss	
		twiss_arr = []
		twiss = self.twiss_arr[0]
		twiss_arr.append(Twiss(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance()*1.0e-6))
		twiss = self.twiss_arr[1]
		twiss_arr.append(Twiss(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance()*1.0e-6))
		twiss = self.twiss_arr[2]
		twiss_arr.append(Twiss(twiss.getAlpha(),twiss.getBeta(),twiss.getEmittance()*1.0e-6))
		diff2 = 0.
		n_ws_points = 0.
		for accState in accStatesKeeper.getAccStatesArr():
			if(accState.isOn):
				probe = EnvelopeProbe(accState.design_probe)
				probe.setKineticEnergy(self.eKin*1.0e+6)
				probe.setBeamCurrent(self.current*0.001)
				probe.setBunchFrequency(402.5e+6)
				probe.initFromTwiss(twiss_arr)
				accState.scenario.setProbe(probe)
				accState.scenario.resyncFromCache()
				accState.scenario.run()
				accState.traj = accState.scenario.getTrajectory()
				for size_record in accState.size_hor_record_arr:
					if(not size_record.isOn): continue
					n_ws_points += 1
					size = size_record.gauss_sigma
					if(fit_param_index == 1): size = size_record.custom_gauss_sigma
					if(fit_param_index == 2): size = size_record.custom_rms_sigma
					pos = size_record.pos
					ws_node = size_record.ws_node
					probe_state =  accState.traj.stateForElement(ws_node.getId())
					size_theory = probe_state.twissParameters()[0].getEnvelopeRadius()*1000.
					diff2 += (size_theory - size)**2
					#print "debug hor ws=",ws_node.getId()," size=",size," size_theory=",size_theory
				for size_record in accState.size_ver_record_arr:
					if(not size_record.isOn): continue
					n_ws_points += 1
					size = size_record.gauss_sigma
					if(fit_param_index == 1): size = size_record.custom_gauss_sigma
					if(fit_param_index == 2): size = size_record.custom_rms_sigma
					pos = size_record.pos				
					ws_node = size_record.ws_node
					probe_state =  accState.traj.stateForElement(ws_node.getId())
					size_theory = probe_state.twissParameters()[1].getEnvelopeRadius()*1000.
					diff2 += (size_theory - size)**2	
					#print "debug ver ws=",ws_node.getId()," size=",size," size_theory=",size_theory
		if(n_ws_points > 0):
			diff2 /= n_ws_points
		if(diff2 != diff2):
				return 0.
		return diff2

	def runErrorsCalculator(self):
		# this method should be preceded by calculateDiff2(). All trajectories should be ready.
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		transverse_twiss_fitting_controller = tr_twiss_analysis_controller.transverse_twiss_fitting_controller
		init_and_fit_params_controller = transverse_twiss_fitting_controller.init_and_fit_params_controller		
		final_twiss_params_holder = init_and_fit_params_controller.final_twiss_params_holder			
		fit_param_index = tr_twiss_analysis_controller.fit_param_index
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		#---- fit_err_text - in %
		size_accuracy = init_and_fit_params_controller.fit_err_text.getValue()/100.
		#matrices to keep [m11^2,2*m11*m12,m12^2] raws
		mMatrX0 = [] 
		mMatrY0 = []	
		weghtVectorX = []
		weghtVectorY = []
		sizesVectorX = []
		sizesVectorY = []	
		for accState in accStatesKeeper.getAccStatesArr():
			if(accState.isOn):
				traj = accState.scenario.getTrajectory()
				for size_record in accState.size_hor_record_arr:
					if(not size_record.isOn): continue
					size = size_record.gauss_sigma
					if(fit_param_index == 1): size = size_record.custom_gauss_sigma
					if(fit_param_index == 2): size = size_record.custom_rms_sigma
					ws_node = size_record.ws_node
					probe_state =  accState.traj.stateForElement(ws_node.getId())
					m = probe_state.getResponseMatrix()
					mMatrX0.append([m.getElem(0,0)*m.getElem(0,0),2*m.getElem(0,0)*m.getElem(0,1),m.getElem(0,1)*m.getElem(0,1)])
					weghtVectorX.append(1./(4.0*size**4*size_accuracy**2))
					sizesVectorX.append(size)
				for size_record in accState.size_ver_record_arr:
					if(not size_record.isOn): continue
					size = size_record.gauss_sigma
					if(fit_param_index == 1): size = size_record.custom_gauss_sigma
					if(fit_param_index == 2): size = size_record.custom_rms_sigma
					ws_node = size_record.ws_node
					probe_state =  accState.traj.stateForElement(ws_node.getId())
					m = probe_state.getResponseMatrix()
					mMatrY0.append([m.getElem(2,2)*m.getElem(2,2),2*m.getElem(2,2)*m.getElem(2,3),m.getElem(2,3)*m.getElem(2,3)])
					weghtVectorY.append(1./(4.0*size**4*size_accuracy**2))
					sizesVectorY.append(size)	
		#---from Python arr to Matrix
		if(len(sizesVectorX) < 3 or len(sizesVectorY) < 3):
			print "debug There is not enough data! We have only Nx=",len(sizesVectorX)," and Ny=",len(sizesVectorY)
			final_twiss_params_holder.setParamsErr(0,0.,0.,0.)
			final_twiss_params_holder.setParamsErr(1,0.,0.,0.)		
			init_and_fit_params_controller.finalTwiss_table.getModel().fireTableDataChanged()			
			return
		mMatrX = Matrix(mMatrX0,len(mMatrX0),3)
		mMatrY = Matrix(mMatrY0,len(mMatrY0),3)
		#=== mwmMatr = (M^T*W*M) =======
		mwmMatrX = Matrix(3,3)
		for i0 in range(3):
			for i1 in range(3):
				sum_val = 0.
				for j in range(len(weghtVectorX)):
					sum_val +=  mMatrX.get(j,i0)*weghtVectorX[j]*mMatrX.get(j,i1)					
				mwmMatrX.set(i0,i1,sum_val)
		mwmMatrY = Matrix(3,3)
		for i0 in range(3):
			for i1 in range(3):
				sum_val = 0.
				for j in range(len(weghtVectorY)):
					sum_val +=  mMatrY.get(j,i0)*weghtVectorY[j]*mMatrY.get(j,i1)					
				mwmMatrY.set(i0,i1,sum_val)				
		#print "debug Matrix X det =",mwmMatrX.det(),mwmMatrX.print(15,5)
		#print "debug Matrix Y det =",mwmMatrY.det(),mwmMatrY.print(15,5)	
		errorMatrix_X = mwmMatrX.inverse()
		errorMatrix_Y = mwmMatrY.inverse()
		#correlation results [<x^2>, <x*xp>, <xp^2>] [<y^2>, <y*yp>, <yp^2>]
		correlValsVector_X = [0.,0.,0.]	
		for i0 in range(3):
			for i1 in range(3):
				for iws in range(len(weghtVectorX)):
					correlValsVector_X[i0] += errorMatrix_X.get(i0,i1)*mMatrX.get(iws,i1)*weghtVectorX[iws]*(sizesVectorX[iws])**2
		correlValsVector_Y = [0.,0.,0.]	
		for i0 in range(3):
			for i1 in range(3):
				for iws in range(len(weghtVectorY)):
					correlValsVector_Y[i0] += errorMatrix_Y.get(i0,i1)*mMatrY.get(iws,i1)*weghtVectorY[iws]*(sizesVectorY[iws])**2
		#--------Errors for values [ <x2>, <x*xp>, <xp2>]		
		correlErrValsVectorX = [0.,0.,0.]
		correlErrValsVectorY = [0.,0.,0.]
		for i0 in range(3):
			#print "debug i=",i0," matr X(i,i) = ",errorMatrix_X.get(i0,i0)
			#print "debug i=",i0," matr Y(i,i) = ",errorMatrix_Y.get(i0,i0)
			correlErrValsVectorX[i0] = math.sqrt(math.fabs(errorMatrix_X.get(i0,i0)))
			correlErrValsVectorY[i0] = math.sqrt(math.fabs(errorMatrix_Y.get(i0,i0)))
		res_tuple = (correlValsVector_X,correlErrValsVectorX,correlValsVector_Y,correlErrValsVectorY)
		([x2,x_xp,xp2],[x2_err,x_xp_err,xp2_err],[y2,y_yp,yp2],[y2_err,y_yp_err,yp2_err]) = res_tuple
		emittX = math.sqrt(math.fabs(x2*xp2 - x_xp**2))
		emittY = math.sqrt(math.fabs(y2*yp2 - y_yp**2))
		betaX = x2/emittX
		betaY = y2/emittY
		alphaX = - x_xp/emittX
		alphaY = - y_yp/emittY
		emittX_err = math.fabs(math.sqrt((xp2*x2_err/(2*emittX))**2 + (x2*xp2_err/(2*emittX))**2 + (x_xp*x_xp_err/emittX)**2))
		emittY_err = math.fabs(math.sqrt((yp2*y2_err/(2*emittY))**2 + (y2*yp2_err/(2*emittY))**2 + (y_yp*y_yp_err/emittY)**2))
		betaX_err = math.fabs(math.sqrt((x2_err/emittX)**2 + (x2*emittX_err/(emittX**2))**2))
		betaY_err = math.fabs(math.sqrt((y2_err/emittY)**2 + (y2*emittY_err/(emittY**2))**2))
		alphaX_err = math.fabs(math.sqrt((x_xp_err/emittX)**2 + (x_xp*emittX_err/(emittX**2))**2))
		alphaY_err = math.fabs(math.sqrt((y_yp_err/emittY)**2 + (y_yp*emittY_err/(emittY**2))**2))
		print "======== one pass knob error analysis results ============="
		print "<x^2>,<x*xp>,<xp^2> = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(x2,x2_err,x_xp,x_xp_err,xp2,xp2_err) 		
		print "<y^2>,<y*yp>,<yp^2> = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(y2,y2_err,y_yp,y_yp_err,yp2,yp2_err) 
		print "==========================================================="
		final_twiss_params_holder.setParamsErr(0,0.,0.,0.)
		final_twiss_params_holder.setParamsErr(1,0.,0.,0.)	
		if(x2 < 0. or xp2 < 0. or y2 < 0. or yp2 < 0. or x2_err < 0. or xp2_err < 0. or y2_err < 0. or yp2_err < 0.): 
			final_twiss_params_holder.setParamsErr(0,0.,0.,0.)
			final_twiss_params_holder.setParamsErr(1,0.,0.,0.)		
			init_and_fit_params_controller.finalTwiss_table.getModel().fireTableDataChanged()
			return
		x_err = math.sqrt(x2_err)/(2.*math.sqrt(x2))
		xp_err = math.sqrt(xp2_err)/(2.*math.sqrt(xp2))
		y_err = math.sqrt(y2_err)/(2.*math.sqrt(y2))
		yp_err = math.sqrt(yp2_err)/(2.*math.sqrt(yp2))
		print "sqrt(<x^2>),<x*xp>,sqrt(<xp^2>) = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(math.sqrt(x2),x_err,x_xp,x_xp_err,math.sqrt(xp2),xp_err) 		
		print "sqrt(<y^2>),<y*yp>,sqrt(<yp^2>) = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(math.sqrt(y2),y_err,y_yp,y_yp_err,math.sqrt(yp2),yp_err) 		
		print "==========================================================="
		print "X alpha, beta, emitt = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(alphaX,alphaX_err,betaX,betaX_err,emittX,emittX_err) 
		print "Y alpha, beta, emitt = (%12.5g +- %10.3g) (%12.5g +- %10.3g) (%12.5g +- %10.3g)"%(alphaY,alphaY_err,betaY,betaY_err,emittY,emittY_err) 	
		final_twiss_params_holder.setParams(0,alphaX, betaX, emittX)
		final_twiss_params_holder.setParams(1,alphaY, betaY, emittY)
		final_twiss_params_holder.setParamsErr(0,alphaX_err, betaX_err, emittX_err)
		final_twiss_params_holder.setParamsErr(1,alphaY_err, betaY_err, emittY_err)
		init_and_fit_params_controller.finalTwiss_table.getModel().fireTableDataChanged()		
		
		
		
		
		
