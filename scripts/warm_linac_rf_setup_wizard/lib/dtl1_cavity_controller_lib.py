# The DTL1 cavity controller
# This controller is different form others DTL-CCL cavities' controllers in the first scan.
# The first scan will use the BPM amplitude to find the estimation for the design phase.
# The second step in fitting should be the same.

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
from javax.swing import JTable
from java.util import ArrayList
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from javax.swing.table import AbstractTableModel, TableModel


from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel, GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat

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


false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

from abstract_cavity_controller_lib import Abstract_Cavity_Controller
from constants_lib import GRAPH_LEGEND_KEY
from functions_and_classes_lib import calculateAvgErr, makePhaseNear, HarmonicsAnalyzer 
from functions_and_classes_lib import dumpGraphDataToDA, readGraphDataFromDA
from data_acquisition_classes_lib import BPM_Scan_Data

#------------------------------------------------------------------------
#           Auxiliary classes for PASTA fitting
#------------------------------------------------------------------------	
class PASTA_Fitting_Diff_Calculator:
	""" The class calculates the difference between model and PASTA scan results """
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller	
		
	def caluclateDiff(self,nPhasePoints,Ekin_in,cav_amp_coeff,phase_shift):
		phase_diff2 = 0.
		count = 0		
		if(nPhasePoints < 3):
			txt = "Number of phase points for fitting is too low! N = "+str(nPhasePoints)+ " Stop."
			return (false,txt,phase_diff2)				
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		cav_wrapper = self.cav_controller.cav_wrapper
		local_bpm_wrappers = cav_bpms_controller.local_bpm_wrappers		
		particle_tracker_model = self.main_loop_controller.particle_tracker_model
		particle_tracker_model.setActiveCavity(cav_wrapper)		
		#--------------------------------------------------
		pasta_scan_data_arr = cav_bpms_controller.pasta_scan_data_arr
		max_dev = 0.
		max_dev_phase = 0.
		max_dev_amp = 0.		
		if(len(pasta_scan_data_arr) < 1):
			txt = "No PASTA scan data! Stop."
			return (false,txt,phase_diff2)	
		cav_phase_design = cav_wrapper.design_phase
		for [cav_amp,bpm_scan_data0,bpm_scan_data1] in pasta_scan_data_arr:
			cav_model_amp = cav_amp_coeff*cav_amp
			particle_tracker_model.setModelAmpPhaseToActiveCav(cav_model_amp,cav_phase_design,0.)
			phase_min = self.cav_controller.scan_main_panel.start_fit_phase_text.getValue()
			phase_max = self.cav_controller.scan_main_panel.stop_fit_phase_text.getValue()
			phase_step = (phase_max - phase_min)/(nPhasePoints-1)
			bpm_scan_data0.phase_fit_gd.removeAllPoints()
			bpm_scan_data1.phase_fit_gd.removeAllPoints()
			for ind in range(nPhasePoints):
				cav_phase = phase_min + ind*phase_step
				particle_tracker_model.setModelPhase(cav_phase,phase_shift)
				[bpm_phase0,bpm_phase1] = particle_tracker_model.getBPM_Phases(Ekin_in,cav_phase,phase_shift,local_bpm_wrappers)
				bpm_scan_data0.phase_fit_gd.addPoint(cav_phase,bpm_phase0)
				bpm_scan_data1.phase_fit_gd.addPoint(cav_phase,bpm_phase1)
			bpm_scan_data0.shiftToPhase(bpm_scan_data0.phase_fit_gd,bpm_scan_data0.phase_gd.getY(0))
			bpm_scan_data1.shiftToPhase(bpm_scan_data1.phase_fit_gd,bpm_scan_data1.phase_gd.getY(0))
			for ind in range(bpm_scan_data0.phase_gd.getNumbOfPoints()):
				cav_phase = bpm_scan_data0.phase_gd.getX(ind)
				diff_fit = bpm_scan_data1.phase_fit_gd.getValueY(cav_phase) - bpm_scan_data0.phase_fit_gd.getValueY(cav_phase)
				diff = bpm_scan_data1.phase_gd.getValueY(cav_phase) - bpm_scan_data0.phase_gd.getValueY(cav_phase)
				phase_diff2 += (diff_fit - diff)**2
				if((diff_fit - diff)**2 > max_dev):
					max_dev = (diff_fit - diff)**2
					max_dev_phase = cav_phase
					max_dev_amp = cav_amp
				count += 1
		if(count > 1): phase_diff2 /= count
		#print "debug max_dev=",math.sqrt(max_dev)," max_dev_phase=",max_dev_phase," max_dev_amp =",max_dev_amp 
		return (true,"",phase_diff2)	

class PASTA_Fitter:
	""" The class starts the PASTA fitting procedure. """
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller	
		self.solver = null
		
	def fit(self):
		cav_wrapper = self.cav_controller.cav_wrapper
		#------------------------------------
		Ekin_in = cav_wrapper.Ekin_in
		cav_amp_coeff = cav_wrapper.cav_amp_coeff
		phase_shift = cav_wrapper.cav_phase_guess - cav_wrapper.design_phase
		#------------------------------------
		variables = ArrayList()
		variables_fit = ArrayList()
		delta_hint = InitialDelta()
		#----- variable cavity phase offset
		var = Variable("phase_shift",phase_shift, - Double.MAX_VALUE, Double.MAX_VALUE)
		variables.add(var)		
		if(cav_wrapper.cav_phase_guess_fit_use):
			variables_fit.add(var)
			delta_hint.addInitialDelta(var,1.0)		
		#----- variable Ekin_in
		var = Variable("Ekin_in",Ekin_in, - Double.MAX_VALUE, Double.MAX_VALUE)
		variables.add(var)
		if(cav_wrapper.Ekin_in_fit_use):
			variables_fit.add(var)
			delta_hint.addInitialDelta(var,0.003)
		#----- variable cavity amplitude
		var = Variable("cav_amp_coeff",cav_amp_coeff, - Double.MAX_VALUE,  Double.MAX_VALUE)
		variables.add(var)
		if(cav_wrapper.cav_amp_coeff_fit_use):
			variables_fit.add(var)
			delta_hint.addInitialDelta(var,cav_amp_coeff*0.005)
		#-------- solve the fitting problem
		tm = self.cav_controller.scan_main_panel.time_limit_fit_text.getValue()
		scorer = PASTA_Scorer(self.main_loop_controller,self.cav_controller,variables)
		maxTimerStopper = SolveStopperFactory.maxElapsedTimeStopper(tm) 
		self.solver = Solver(SimplexSearchAlgorithm(),maxTimerStopper)
		problem = ProblemFactory.getInverseSquareMinimizerProblem(variables_fit,scorer,0.0001)
		problem.addHint(delta_hint)
		self.solver.solve(problem)
		self.cav_controller.local_bpms_and_params_tables_panel.progressBar.setValue(0)
		#------- get results
		trial = self.solver.getScoreBoard().getBestSolution()
		err2 = scorer.score(trial,variables_fit)	
		self.cav_controller.local_bpms_and_params_tables_panel.pasta_error_text.setValue(math.sqrt(err2))
		[phase_shift,Ekin_in,cav_amp_coeff] = scorer.getTrialParams(trial)
		cav_wrapper.Ekin_in = Ekin_in
		cav_wrapper.cav_phase_guess = makePhaseNear(cav_wrapper.design_phase + phase_shift,0.)
		cav_wrapper.newPhase = cav_wrapper.cav_phase_guess
		cav_wrapper.cav_amp_coeff = cav_amp_coeff
		cav_wrapper.newAmp = cav_wrapper.design_amp/cav_amp_coeff
		cav_wrapper.avg_pasta_err = math.sqrt(err2)
		#------------------------------------------
		self.solver = null
		#------------------------------------------		
		nPhasePoints = 30
		(res,txt,phase_diff2) = self.cav_controller.pasta_fitting_diff_calculator.caluclateDiff(nPhasePoints,Ekin_in,cav_amp_coeff,phase_shift)
		if(not res):
			return (res,txt)
		particle_tracker_model = self.main_loop_controller.particle_tracker_model
		particle_tracker_model.setModelAmpPhaseToActiveCav(cav_wrapper.design_amp,cav_wrapper.design_phase,0.)
		particle_tracker_model.trackProbe(Ekin_in)
		Ekin_out = particle_tracker_model.getEkin_Out()
		cav_wrapper.Ekin_out = Ekin_out
		#------------------------------------------
		self.cav_controller.local_bpms_and_params_tables_panel.updateAllTables()
		self.cav_controller.graphs_panel.updateGraphs()
		cav_ind = self.main_loop_controller.cav_wrappers.index(self.cav_controller.cav_wrapper)
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)	
		self.cav_controller.graphs_panel.setSelectedIndex(1)	
		self.cav_controller.graphs_panel.updateGraphs()		
		return (true,"")
		
	def stopFitting(self):
		if(self.solver != null):
			self.solver.stopSolving()

class PASTA_Scorer(Scorer):
	""" 
	Calculate the difference between model and measured points for PASTA scans. 
	"""		
	def __init__(self,main_loop_controller,cav_controller,variables):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller	
		self.variables = variables	
		self.phase_shift =self.variables.get(0).getInitialValue()
		self.Ekin_in = self.variables.get(1).getInitialValue()
		self.cav_amp_coeff = self.variables.get(2).getInitialValue()
		self.nPhasePoints = int(self.cav_controller.scan_main_panel.n_points_fit_text.getValue())
		self.progressBar   = self.cav_controller.local_bpms_and_params_tables_panel.progressBar 
		self.progressBar.setValue(0)
		self.pasta_error_text = self.cav_controller.local_bpms_and_params_tables_panel.pasta_error_text
		self.total_time_target = self.cav_controller.scan_main_panel.time_limit_fit_text.getValue()
		self.min_diff2 = Double.MAX_VALUE
		self.time_start = time.time()
		self.time_local_start = time.time()
		self.update_time_step = 0.5
		self.count = 0

	def score(self,trial,variables_in):	
		self.getTrialParams(trial)
		return self.getDiff2()
		
	def getDiff2(self):
		self.count += 1
		#-----calculate diff==========================
		nPhasePoints = self.nPhasePoints
		phase_shift = self.phase_shift
		Ekin_in = self.Ekin_in
		cav_amp_coeff = self.cav_amp_coeff
		(res,txt,phase_diff2) = self.cav_controller.pasta_fitting_diff_calculator.caluclateDiff(nPhasePoints,Ekin_in,cav_amp_coeff,phase_shift)
		if(not res):
			return 0.
		if(phase_diff2 < self.min_diff2):
				self.min_diff2 = phase_diff2
				self.pasta_error_text.setValue(math.sqrt(self.min_diff2))
				#print "debug count=",self.count
		if(time.time() - self.time_local_start > self.update_time_step):
			self.time_local_start = time.time()
			self.progressBar.setValue(int(100.*(self.time_local_start-self.time_start)/self.total_time_target))
		return phase_diff2
		
	def getTrialParams(self,trial):
		#------set up parameters from Trial map
		var_map = trial.getTrialPoint().getValueMap()
		variables = self.variables
		if(var_map.containsKey(variables.get(0))): self.phase_shift = trial.getTrialPoint().getValue(variables.get(0))
		if(var_map.containsKey(variables.get(1))): self.Ekin_in = trial.getTrialPoint().getValue(variables.get(1))
		if(var_map.containsKey(variables.get(1))): self.cav_amp_coeff = trial.getTrialPoint().getValue(variables.get(2))
		return [self.phase_shift,self.Ekin_in,self.cav_amp_coeff]

class PASTA_Fitter_Runner(Runnable):
	# main thread runner for the anlysis
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
	
	def run(self):
		self.main_loop_controller.getMessageTextField().setText("")
		(res,txt) = self.cav_controller.pasta_fitter.fit()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)

class Local_Scan_Run_State:
	""" Describes the local scan state """
	def __init__(self):
		self.isFullScan_Running = false
		self.isPASTA_Running = false
		self.shouldStop = false

class Local_Full_Scan_Runner(Runnable):
	# the thread runner for the 360 deg scan
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
	
	def run(self):
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.local_scan_run_state.isFullScan_Running = true
		self.cav_controller.local_scan_run_state.isPASTA_Running = false
		self.cav_controller.local_scan_run_state.shouldStop = false
		self.main_loop_controller.loop_run_state.isRunning = true
		(res,txt) = self.cav_controller.runFullRangeScan()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)
		self.cav_controller.local_scan_run_state.isFullScan_Running = false	
		self.cav_controller.local_scan_run_state.isPASTA_Running = false
		self.cav_controller.local_scan_run_state.shouldStop = false
		self.main_loop_controller.loop_run_state.isRunning = false
		self.cav_controller.initProgressBar()

class Local_PASTA_Scan_Runner(Runnable):
	# the thread runner for the 360 deg scan
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
	
	def run(self):
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.local_scan_run_state.isFullScan_Running = false
		self.cav_controller.local_scan_run_state.isPASTA_Running = true
		self.cav_controller.local_scan_run_state.shouldStop = false
		self.main_loop_controller.loop_run_state.isRunning = true
		(res,txt) = self.cav_controller.runPASTA_Scan()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)
		self.cav_controller.local_scan_run_state.isFullScan_Running = false	
		self.cav_controller.local_scan_run_state.isPASTA_Running = false
		self.cav_controller.local_scan_run_state.shouldStop = false
		self.main_loop_controller.loop_run_state.isRunning = false
		self.cav_controller.initProgressBar()
		
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------	
class Scan_Main_Panel(JPanel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.setLayout(BorderLayout())
		self.setBorder(BorderFactory.createEtchedBorder())
		#---- 360 deg scan params.
		full_scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		full_scan_panel.setBorder(BorderFactory.createEtchedBorder())
		full_scan_label = JLabel("Phase step=",JLabel.RIGHT)
		self.full_scan_phase_step_text = DoubleInputTextField(15.0,FortranNumberFormat("G4.2"),6)
		time_step_label = JLabel(" sleep time[sec]=",JLabel.RIGHT)
		self.time_step_text = DoubleInputTextField(2.2,FortranNumberFormat("G4.2"),6)
		full_scan_panel.add(full_scan_label)
		full_scan_panel.add(self.full_scan_phase_step_text)
		full_scan_panel.add(time_step_label)
		full_scan_panel.add(self.time_step_text)
		etched_border = BorderFactory.createEtchedBorder(Color.BLACK,Color.BLACK)
		titled_border = BorderFactory.createTitledBorder(etched_border,"360 Deg. Scan")
		full_scan_panel.setBorder(titled_border)
		#---- around -90 deg scan params.
		#-----------cav amp scan 
		amp_scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		amp_scan_panel.setBorder(BorderFactory.createEtchedBorder())
		min_cav_amp_label = JLabel("Min Cav. Amp=",JLabel.RIGHT)
		self.min_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G9.5"),8)
		max_cav_amp_label = JLabel("  Max=",JLabel.RIGHT)
		self.max_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G8.5"),8)
		step_cav_amp_label = JLabel(" N amp. scan steps=",JLabel.RIGHT)
		self.nsteps_cav_amp_text =  DoubleInputTextField(2.0,FortranNumberFormat("G4.1"),5)
		live_cav_amp_label = JLabel("   Live Ampl.=",JLabel.RIGHT)
		self.live_cav_amp_text =  DoubleInputTextField(0.0,FortranNumberFormat("G8.5"),8)		
		amp_scan_panel.add(min_cav_amp_label)
		amp_scan_panel.add(self.min_cav_amp_text)
		amp_scan_panel.add(max_cav_amp_label)
		amp_scan_panel.add(self.max_cav_amp_text)
		amp_scan_panel.add(step_cav_amp_label)
		amp_scan_panel.add(self.nsteps_cav_amp_text)
		amp_scan_panel.add(live_cav_amp_label)
		amp_scan_panel.add(self.live_cav_amp_text)
		#-----------cav phase scan
		scan_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		scan_panel.setBorder(BorderFactory.createEtchedBorder())
		scan_label = JLabel("Cavity Phase Scan Width[deg]=",JLabel.RIGHT)
		self.scan_phase_width_text = DoubleInputTextField(30.0,FortranNumberFormat("G4.2"),6)
		scan_phase_step_label = JLabel("  Phase step[deg]=",JLabel.RIGHT)
		self.scan_phase_step_text = DoubleInputTextField(3.0,FortranNumberFormat("G4.2"),6)
		min_bpm_amp_label = JLabel("    Min BPM ampitude[mA]=",JLabel.RIGHT)
		self.min_bpm_amp_text = DoubleInputTextField(1.0,FortranNumberFormat("G4.2"),6)		
		scan_panel.add(scan_label)
		scan_panel.add(self.scan_phase_width_text)
		scan_panel.add(scan_phase_step_label)
		scan_panel.add(self.scan_phase_step_text)
		scan_panel.add(min_bpm_amp_label)
		scan_panel.add(self.min_bpm_amp_text)
		#---------analysis panel
		analysis_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		analysis_panel.setBorder(BorderFactory.createEtchedBorder())
		analysis_label =JLabel("Model Fit: From phase[deg]=",JLabel.RIGHT)
		self.start_fit_phase_text = DoubleInputTextField(0.0,FortranNumberFormat("G4.2"),6)
		stop_fit_phase_label = JLabel(" to phase [deg]=",JLabel.RIGHT)
		self.stop_fit_phase_text = DoubleInputTextField(0.0,FortranNumberFormat("G4.2"),6)
		n_points_fit_label = JLabel(" fit points=",JLabel.RIGHT)
		self.n_points_fit_text = DoubleInputTextField(10.0,FortranNumberFormat("G4.1"),6)
		time_limit_fit_label = JLabel(" Fit time[sec]=",JLabel.RIGHT)
		self.time_limit_fit_text = DoubleInputTextField(20.0,FortranNumberFormat("G4.1"),6)		
		analysis_panel.add(analysis_label)
		analysis_panel.add(self.start_fit_phase_text)
		analysis_panel.add(stop_fit_phase_label)
		analysis_panel.add(self.stop_fit_phase_text)
		analysis_panel.add(n_points_fit_label)
		analysis_panel.add(self.n_points_fit_text)
		analysis_panel.add(time_limit_fit_label)
		analysis_panel.add(self.time_limit_fit_text)
		#----------------------------
		etched_border = BorderFactory.createEtchedBorder(Color.BLACK,Color.BLACK)
		titled_border = BorderFactory.createTitledBorder(etched_border,"PASTA Scan and Analysis")
		pasta_panel = JPanel(GridLayout(3,1,1,1))
		pasta_panel.setBorder(titled_border)
		pasta_panel.add(amp_scan_panel)
		pasta_panel.add(scan_panel)
		pasta_panel.add(analysis_panel)
		#----------------------------
		self.add(full_scan_panel,BorderLayout.NORTH)
		self.add(pasta_panel,BorderLayout.CENTER)
		
class Graphs_Panel(JTabbedPane):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		#----------------------------------------
		etched_border = BorderFactory.createEtchedBorder()
		self.gp_bpm_amp_phase_scan = FunctionGraphsJPanel()
		self.gp_pasta_phase_diff_scan = FunctionGraphsJPanel()		
		self.gp_pasta_bpm0_phase_scan = FunctionGraphsJPanel()
		self.gp_pasta_bpm1_phase_scan = FunctionGraphsJPanel()
		self.gp_pasta_bpms_amp_scan = FunctionGraphsJPanel()
		#------------------------------------------
		self.gp_bpm_amp_phase_scan.setLegendButtonVisible(true)
		self.gp_pasta_phase_diff_scan.setLegendButtonVisible(true)
		self.gp_pasta_bpm0_phase_scan.setLegendButtonVisible(true)
		self.gp_pasta_bpm1_phase_scan.setLegendButtonVisible(true)
		self.gp_pasta_bpms_amp_scan.setLegendButtonVisible(true)
		#------------------------------------------	
		self.gp_bpm_amp_phase_scan.setChooseModeButtonVisible(true)	
		self.gp_pasta_phase_diff_scan.setChooseModeButtonVisible(true)
		self.gp_pasta_bpm0_phase_scan.setChooseModeButtonVisible(true)
		self.gp_pasta_bpm1_phase_scan.setChooseModeButtonVisible(true)
		self.gp_pasta_bpms_amp_scan.setChooseModeButtonVisible(true)
		self.gp_bpm_amp_phase_scan.setName("360 deg Caity Phase Scan: BPM Amp")
		self.gp_pasta_phase_diff_scan.setName("PASTA Scan: DTL:BPM209 and DTL:BPM203 phase diff")
		self.gp_pasta_bpm0_phase_scan.setName("PASTA Scan: DTL:BPM203 Phases")
		self.gp_pasta_bpm1_phase_scan.setName("PASTA Scan: DTL:BPM209 Phases")
		self.gp_pasta_bpms_amp_scan.setName("PASTA Scan: DTL:BPM203 and DTL:BPM209 Amp")
		self.gp_bpm_amp_phase_scan.setAxisNames("Cav Phase, [deg]","BPM Amp, [a.u.]")	
		self.gp_pasta_phase_diff_scan.setAxisNames("Cav Phase, [deg]","BPMs Phase Diff, [deg]")	
		self.gp_pasta_bpm0_phase_scan.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")
		self.gp_pasta_bpm1_phase_scan.setAxisNames("Cav Phase, [deg]","BPM Phase, [deg]")
		self.gp_pasta_bpms_amp_scan.setAxisNames("Cav Phase","BPM Amp, [a.u.]")		
		self.gp_bpm_amp_phase_scan.setBorder(etched_border)
		self.gp_pasta_phase_diff_scan.setBorder(etched_border)
		self.gp_pasta_bpm0_phase_scan.setBorder(etched_border)	
		self.gp_pasta_bpm1_phase_scan.setBorder(etched_border)	
		self.gp_pasta_bpms_amp_scan.setBorder(etched_border)	
		#------------------------------------
		bpms_phases_scan_panel = JPanel(GridLayout(2,1,1,1))
		bpms_phases_scan_panel.add(self.gp_pasta_bpm0_phase_scan)
		bpms_phases_scan_panel.add(self.gp_pasta_bpm1_phase_scan)
		amp_phase_scan_panel = JSplitPane(JSplitPane.VERTICAL_SPLIT,bpms_phases_scan_panel,self.gp_pasta_bpms_amp_scan)
		amp_phase_scan_panel.setDividerLocation(0.7)
		amp_phase_scan_panel.setResizeWeight(0.7)
		self.add("DTL:BPM203 Amp Scan",self.gp_bpm_amp_phase_scan)
		self.add("PASTA Phase Diff Fit",self.gp_pasta_phase_diff_scan)
		self.add("PASTA BPMs' Phase and Amp",amp_phase_scan_panel)		
		#-----------------------------------
		self.gp_bpm_amp_phase_scan.addVerticalLine(0.,Color.RED)
		self.gp_bpm_amp_phase_scan.addVerticalLine(0.,Color.BLACK)		
		self.gp_pasta_phase_diff_scan.addVerticalLine(0.,Color.RED)
		self.gp_pasta_bpm0_phase_scan.addVerticalLine(0.,Color.RED)
		self.gp_pasta_bpm1_phase_scan.addVerticalLine(0.,Color.RED)
		self.gp_pasta_bpms_amp_scan.addVerticalLine(0.,Color.RED)
		
	def setFoundCavityPhase(self,cav_phase):
		self.gp_bpm_amp_phase_scan.setVerticalLineValue(cav_phase,0)
		self.gp_pasta_phase_diff_scan.setVerticalLineValue(cav_phase,0)
		self.gp_pasta_bpm0_phase_scan.setVerticalLineValue(cav_phase,0)
		self.gp_pasta_bpm1_phase_scan.setVerticalLineValue(cav_phase,0)
		self.gp_pasta_bpms_amp_scan.setVerticalLineValue(cav_phase,0)
		amp_front_phase_pos = self.cav_controller.cav_bpms_controller.amp_front_phase_pos
		self.gp_bpm_amp_phase_scan.setVerticalLineValue(amp_front_phase_pos,1)		
		
	def setGuessedCavityPhase(self,cav_phase):
		self.gp_bpm_amp_phase_scan.setVerticalLineValue(cav_phase,1)
		
	def refreshGraphJPanels(self):
		self.gp_bpm_amp_phase_scan.refreshGraphJPanel()
		self.gp_pasta_phase_diff_scan.refreshGraphJPanel()
		self.gp_pasta_bpm0_phase_scan.refreshGraphJPanel()
		self.gp_pasta_bpm1_phase_scan.refreshGraphJPanel()
		self.gp_pasta_bpms_amp_scan.refreshGraphJPanel()
		
	def updateGraphs(self):
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		bpm_scan_data = cav_bpms_controller.bpm_scan_data_amp
		self.gp_bpm_amp_phase_scan.removeAllGraphData()
		self.gp_bpm_amp_phase_scan.addGraphData(bpm_scan_data.amp_gd)
		#---------------------------------------------------------------
		color_arr = [Color.BLACK,Color.BLUE,Color.RED,Color.MAGENTA,Color.ORANGE,Color.GREEN]
		self.gp_pasta_phase_diff_scan.removeAllGraphData()
		color_count = 0
		for [cav_amp,bpm_scan_data0,bpm_scan_data1] in cav_bpms_controller.pasta_scan_data_arr:
			cl = color_arr[color_count]
			color_count += 1
			if(color_count >= len(color_arr)): color_count = 0
			phase_diff_gd = BasicGraphData()
			phase_diff_gd.setLineThick(1)
			phase_diff_gd.setGraphPointSize(7)
			phase_diff_gd.setGraphColor(cl)
			phase_diff_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Diff Amp= %5.3f"%cav_amp)
			phase_diff_gd.setDrawLinesOn(true)
			phase_diff_gd.setDrawPointsOn(true)
			phase_diff_fit_gd = BasicGraphData()
			phase_diff_fit_gd.setLineThick(3)
			phase_diff_fit_gd.setGraphPointSize(7)
			phase_diff_fit_gd.setGraphColor(cl)
			phase_diff_fit_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Diff Fit Amp= %5.3f"%cav_amp)
			phase_diff_fit_gd.setDrawLinesOn(true)
			phase_diff_fit_gd.setDrawPointsOn(false)
			y_diff_old = 0.
			for ind in range(bpm_scan_data0.phase_gd.getNumbOfPoints()):
				x = bpm_scan_data0.phase_gd.getX(ind)
				y0 = bpm_scan_data0.phase_gd.getY(ind)
				y1 = bpm_scan_data1.phase_gd.getValueY(x)
				y_diff = makePhaseNear(y1-y0,y_diff_old)
				y_diff_old = y_diff
				phase_diff_gd.addPoint(x,y_diff)
			for ind in range(bpm_scan_data0.phase_fit_gd.getNumbOfPoints()):
				x = bpm_scan_data0.phase_fit_gd.getX(ind)
				y0 = bpm_scan_data0.phase_fit_gd.getY(ind)
				y1 = bpm_scan_data1.phase_fit_gd.getValueY(x)
				y_diff = makePhaseNear(y1-y0,phase_diff_gd.getValueY(x))
				phase_diff_fit_gd.addPoint(x,y_diff)
			self.gp_pasta_phase_diff_scan.addGraphData(phase_diff_gd)
			self.gp_pasta_phase_diff_scan.addGraphData(phase_diff_fit_gd)
		#---------------------------------------------
		self.gp_pasta_bpm0_phase_scan.removeAllGraphData()
		self.gp_pasta_bpm1_phase_scan.removeAllGraphData()
		self.gp_pasta_bpms_amp_scan.removeAllGraphData()
		for [cav_amp,bpm_scan_data0,bpm_scan_data1] in cav_bpms_controller.pasta_scan_data_arr:
			self.gp_pasta_bpm0_phase_scan.addGraphData(bpm_scan_data0.phase_gd)
			self.gp_pasta_bpm0_phase_scan.addGraphData(bpm_scan_data0.phase_fit_gd)
			self.gp_pasta_bpm1_phase_scan.addGraphData(bpm_scan_data1.phase_gd)
			self.gp_pasta_bpm1_phase_scan.addGraphData(bpm_scan_data1.phase_fit_gd)
			self.gp_pasta_bpms_amp_scan.addGraphData(bpm_scan_data0.amp_gd)
			self.gp_pasta_bpms_amp_scan.addGraphData(bpm_scan_data1.amp_gd)
		#-----------------------------------------------------------------
		cav_phase = self.cav_controller.cav_wrapper.cav_phase_guess
		self.setFoundCavityPhase(cav_phase)
		
	def removeOnePoint(self):
		self.main_loop_controller.getMessageTextField().setText("")
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		pane_ind = self.getSelectedIndex()
		gp_active = null
		gd_arr = []
		if(pane_ind == 0):
			gp_active = self.gp_bpm_amp_phase_scan
			gd_arr.append(cav_bpms_controller.bpm_scan_data_amp.amp_gd)
		if(pane_ind == 1):
			gp_active = self.gp_pasta_phase_diff_scan
		if(pane_ind == 2):
			gp_active = self.gp_pasta_bpm0_phase_scan
		if(pane_ind == 1 or pane_ind == 2):
			for [cav_amp,bpm_scan_data0,bpm_scan_data1] in cav_bpms_controller.pasta_scan_data_arr:
				gd_arr.append(bpm_scan_data0.phase_gd)
				gd_arr.append(bpm_scan_data1.phase_gd)
				gd_arr.append(bpm_scan_data0.amp_gd)
				gd_arr.append(bpm_scan_data1.amp_gd)				
		if(gp_active == null):
			self.main_loop_controller.getMessageTextField().setText("Cannot remove point from this graph!")
			return
		if(len(gd_arr) < 1): return
		#-------------------------------------------------------
		minX = gp_active.getCurrentMinX()
		maxX = gp_active.getCurrentMaxX()
		nPoints = 0
		point_ind = -1
		gd = gd_arr[0]
		for ind in range(gd.getNumbOfPoints()):
			x = gd.getX(ind)
			if( x > minX and x < maxX):
				nPoints += 1
				point_ind = ind
		if(nPoints != 1):
			self.main_loop_controller.getMessageTextField().setText("Use zoom to select one point on the upper graph!")
			return
		for gd in gd_arr:
			gd.removePoint(point_ind)
		gp_active.clearZoomStack()
		self.updateGraphs()
	
class Local_BPMs_and_Params_Tables_Panel(JPanel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.setLayout(BorderLayout())
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)	
		#------------------------------
		self.local_bpms_table = JTable(Local_BPMs_Table_Model(main_loop_controller,cav_controller))
		self.local_bpms_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.local_bpms_table.setFillsViewportHeight(true)
		self.local_bpms_table.setPreferredScrollableViewportSize(Dimension(350,50))
		bpm_table_panel = JScrollPane(self.local_bpms_table)
		titled_border = BorderFactory.createTitledBorder(etched_border,"BPMs Table")
		bpm_table_panel.setBorder(titled_border)
		#-------------------------------------
		full_scan_local_button = JButton("360 deg Scan")
		full_scan_local_button.addActionListener(Full_Scan_Local_Button_Listener(self.main_loop_controller,self.cav_controller))	
		pasta_scan_local_button = JButton("PASTA Scan")
		pasta_scan_local_button.addActionListener(Pasta_Scan_Local_Button_Listener(self.main_loop_controller,self.cav_controller))	
		stop_scan_local_button = JButton("STOP")
		stop_scan_local_button.addActionListener(Stop_Scan_Local_Button_Listener(self.main_loop_controller,self.cav_controller))
		local_scan_buttons_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		local_scan_buttons_panel.setBorder(etched_border)
		local_scan_buttons_panel.add(full_scan_local_button)
		local_scan_buttons_panel.add(pasta_scan_local_button)
		local_scan_buttons_panel.add(stop_scan_local_button)
		#-------------------------------------
		self.fitting_params_table = JTable(Fit_Params_Table_Model(main_loop_controller,cav_controller))
		self.fitting_params_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.fitting_params_table.setFillsViewportHeight(true)
		self.fitting_params_table.setPreferredScrollableViewportSize(Dimension(350,60))
		titled_border = BorderFactory.createTitledBorder(etched_border,"PASTA Fitting Parameters")
		fitting_params_table_panel = JScrollPane(self.fitting_params_table)
		fitting_params_table_panel.setBorder(titled_border)	
		#-------------------------------------
		self.found_params_table = JTable(Found_Params_Table_Model(main_loop_controller,cav_controller))
		self.found_params_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
		self.found_params_table.setFillsViewportHeight(true)
		self.found_params_table.setPreferredScrollableViewportSize(Dimension(350,70))
		titled_border = BorderFactory.createTitledBorder(etched_border,"PASTA Results")
		found_params_table_panel = JScrollPane(self.found_params_table)
		found_params_table_panel.setBorder(titled_border)			
		#-------------------------------------
		knob_panel = JPanel(FlowLayout(FlowLayout.LEFT,1,3))
		knob_panel.setBorder(etched_border)
		make_guess_button = JButton("Make Guess")
		make_guess_button.addActionListener(Make_Guess_Button_Listener(self.main_loop_controller,self.cav_controller))		
		re_analyze_button = JButton("Analyze PASTA")
		re_analyze_button.addActionListener(Re_Analyze_Button_Listener(self.main_loop_controller,self.cav_controller))	
		stop_analysis_button = JButton("STOP")
		stop_analysis_button.addActionListener(Stop_Analysis_Button_Listener(self.main_loop_controller,self.cav_controller))			
		knob_panel.add(make_guess_button)
		knob_panel.add(re_analyze_button)	
		knob_panel.add(stop_analysis_button)
		#-------------------------------------
		progress_label =JLabel("Fitting Progress=",JLabel.RIGHT)
		self.progressBar = JProgressBar(0,100)
		self.progressBar.setStringPainted(true)
		pasta_error_label =JLabel("PASTA Err[deg]=",JLabel.RIGHT)
		self.pasta_error_text = DoubleInputTextField(0.0,FortranNumberFormat("G12.5"),12)	
		self.pasta_error_text.setHorizontalAlignment(JTextField.CENTER)
		pasta_err_panel = JPanel(BorderLayout())
		pasta_err_panel.add(pasta_error_label,BorderLayout.WEST)
		pasta_err_panel.add(self.pasta_error_text,BorderLayout.CENTER)
		pasta_progress_label_panel = JPanel(GridLayout(2,1,1,1))
		pasta_progress_label_panel.add(progress_label)
		pasta_progress_label_panel.add(pasta_error_label)
		pasta_progress_bar_panel = JPanel(GridLayout(2,1,1,1))
		pasta_progress_bar_panel.add(self.progressBar)
		pasta_progress_bar_panel.add(self.pasta_error_text)
		progress_panel = JPanel(BorderLayout())
		progress_panel.setBorder(etched_border)
		progress_panel.add(pasta_progress_label_panel,BorderLayout.WEST)
		progress_panel.add(pasta_progress_bar_panel,BorderLayout.CENTER)
		#-------------------------------------
		set_amp_value_button = JButton("Set Amp.to EPICS")
		set_amp_value_button.addActionListener(Set_Amp_to_EPICS_Button_Listener(self.main_loop_controller,self.cav_controller))	
		set_phase_value_button = JButton("Set Phase to EPICS")
		set_phase_value_button.addActionListener(Set_Phase_to_EPICS_Button_Listener(self.main_loop_controller,self.cav_controller))	
		gen_simulations_button = JButton("Generate Simulations")
		gen_simulations_button.addActionListener(Generate_Simulations_Button_Listener(self.main_loop_controller,self.cav_controller))			
		set_values_panel0 = JPanel(FlowLayout(FlowLayout.CENTER,10,10))
		set_values_panel0.add(set_amp_value_button)
		set_values_panel0.add(set_phase_value_button)		
		set_values_panel1 = JPanel(FlowLayout(FlowLayout.CENTER,10,10))	
		set_values_panel1.add(gen_simulations_button)
		set_values_panel = JPanel(BorderLayout())
		set_values_panel.add(set_values_panel0,BorderLayout.NORTH)
		set_values_panel.add(set_values_panel1,BorderLayout.SOUTH)
		#-------------------------------------
		tmp_panel0 = JPanel(BorderLayout())
		tmp_panel0.add(bpm_table_panel,BorderLayout.NORTH)
		tmp_panel00 = JPanel(BorderLayout())
		tmp_panel00.add(local_scan_buttons_panel,BorderLayout.NORTH)
		tmp_panel00.add(self.cav_controller.getScanProgressBarPanel(),BorderLayout.CENTER)
		tmp_panel0.add(tmp_panel00,BorderLayout.CENTER)
		tmp_panel0.add(fitting_params_table_panel,BorderLayout.SOUTH)
		tmp_panel1 = JPanel(BorderLayout())
		tmp_panel1.add(tmp_panel0,BorderLayout.NORTH)		
		tmp_panel1.add(found_params_table_panel,BorderLayout.CENTER)		
		tmp_panel1.add(knob_panel,BorderLayout.SOUTH)	
		tmp_panel2 = JPanel(BorderLayout())
		tmp_panel2.add(tmp_panel1,BorderLayout.NORTH)		
		tmp_panel2.add(progress_panel,BorderLayout.CENTER)		
		tmp_panel2.add(set_values_panel,BorderLayout.SOUTH)
		#--------------------------------------
		self.add(tmp_panel2,BorderLayout.NORTH)
		
	def updateAllTables(self):
		self.fitting_params_table.getModel().fireTableDataChanged()
		self.found_params_table.getModel().fireTableDataChanged()
		
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------	
class Full_Scan_Local_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		if(self.main_loop_controller.loop_run_state.isRunning): return
		runner = Local_Full_Scan_Runner(self.main_loop_controller,self.cav_controller)
		thr = Thread(runner)
		thr.start()			
		
class Pasta_Scan_Local_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		if(self.main_loop_controller.loop_run_state.isRunning): return
		runner = Local_PASTA_Scan_Runner(self.main_loop_controller,self.cav_controller)
		thr = Thread(runner)
		thr.start()			
		
class Stop_Scan_Local_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.local_scan_run_state.shouldStop = true

class Re_Analyze_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		runner =  PASTA_Fitter_Runner(self.main_loop_controller,self.cav_controller)
		thr = Thread(runner)
		thr.start()			

class Make_Guess_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		(res,txt) = self.cav_controller.guessInitialPASTA_Params()
		if(not res):
			self.main_loop_controller.getMessageTextField().setText(txt)
	
class Stop_Analysis_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.pasta_fitter.stopFitting()

class Set_Amp_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.cav_wrapper.setLiveAmp(self.cav_controller.cav_wrapper.newAmp)
		
class Set_Phase_to_EPICS_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		self.cav_controller.cav_wrapper.setLivePhase(self.cav_controller.cav_wrapper.newPhase)		

class Generate_Simulations_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")
		particle_tracker_model = self.main_loop_controller.particle_tracker_model
		particle_tracker_model.setActiveCavity(self.cav_controller.cav_wrapper)
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		cav_bpms_controller.clean()
		#------------------------------------------------------
		cav_phase_target = -175.0
		self.cav_controller.cav_wrapper.initAmp = 0.183
		bpm_scan_data_amp = cav_bpms_controller.bpm_scan_data_amp
		for cav_phase_ind in range(-180,180,1):
			cav_phase = 1.0*cav_phase_ind
			bpm_amp = 0.001
			if(math.fabs(makePhaseNear(cav_phase-cav_phase_target,0.)) < 50.0):
				bpm_amp = 10.0
			bpm_scan_data_amp.addExternalPoint(cav_phase,bpm_amp,0.)
		#------------------------------------------------------		
		amp0 = self.cav_controller.cav_wrapper.initAmp
		cav_amp_coeff = self.cav_controller.cav_wrapper.design_amp/self.cav_controller.cav_wrapper.initAmp
		cav_phase0 = cav_phase_target
		cav_phase_width = self.cav_controller.scan_main_panel.scan_phase_width_text.getValue()
		cav_phase_step = self.cav_controller.scan_main_panel.scan_phase_step_text.getValue()
		cav_phase_n_steps = int(cav_phase_width/cav_phase_step)+1
		Ekin_in0 = self.cav_controller.cav_wrapper.Ekin_in
		phase_shift = cav_phase0 - self.cav_controller.cav_wrapper.design_phase
		bpm_amp = 10.
		cav_amp_arr = [0.95,0.975,1.0,1.025,1.05]
		for coeff in cav_amp_arr:
			cav_amp = coeff*amp0
			cav_bpms_controller.addNewCavAmpPastaDataSet(cav_amp)
		local_bpm_wrappers = cav_bpms_controller.local_bpm_wrappers
		for [cav_amp,bpm_scan_data_0,bpm_scan_data_1] in cav_bpms_controller.pasta_scan_data_arr:
			particle_tracker_model.setModelAmpPhaseToActiveCav(cav_amp*cav_amp_coeff,cav_phase0,phase_shift)
			for phase_ind in range(cav_phase_n_steps):
				cav_phase = cav_phase0 - cav_phase_width/2.0 + cav_phase_step*phase_ind
				particle_tracker_model.setModelPhase(cav_phase,phase_shift)
				bpm_phases_arr = particle_tracker_model.getBPM_Phases(Ekin_in0,cav_phase,phase_shift,local_bpm_wrappers)
				[bpm_phase0,bpm_phase1] = bpm_phases_arr
				bpm_scan_data_0.addExternalPoint(cav_phase,bpm_amp,bpm_phase0)
				bpm_scan_data_1.addExternalPoint(cav_phase,bpm_amp,bpm_phase1)
		self.cav_controller.graphs_panel.updateGraphs()
		cav_ind = self.main_loop_controller.cav_wrappers.index(self.cav_controller.cav_wrapper)
		self.main_loop_controller.cav_table.getModel().fireTableDataChanged()
		self.main_loop_controller.cav_table.setRowSelectionInterval(cav_ind,cav_ind)	

class Remove_One_Point_Button_Listener(ActionListener):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller =main_loop_controller
		self.cav_controller = cav_controller
		
	def actionPerformed(self,actionEvent):	
		self.main_loop_controller.getMessageTextField().setText("")	
		self.cav_controller.graphs_panel.removeOnePoint()		
		
#------------------------------------------------
#  JTable models
#------------------------------------------------
class Local_BPMs_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.columnNames = ["BPM",]
		self.columnNames += ["Rel.Pos.[m]",]
		self.columnNames += ["360 Scan",]
		self.columnNames += ["PASTA Use",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.cav_controller.cav_bpms_controller.local_bpm_wrappers)

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		bpm_wrapper = self.cav_controller.cav_bpms_controller.local_bpm_wrappers[row]
		cav_bpms_controller = self.cav_controller.cav_bpms_controller
		amp_scan_use = false
		if(row == 0):  amp_scan_use= true
		pasta_use = true
		if(col == 0): return bpm_wrapper.alias
		if(col == 1): return "%6.3f"%(bpm_wrapper.pos - self.cav_controller.cav_wrapper.pos)
		if(col == 2): return amp_scan_use
		if(col == 3): return pasta_use
		return ""
				
	def getColumnClass(self,col):
		if(col == 0 or col == 1):
			return self.string_class	
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		pass
	
class Fit_Params_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.columnNames = ["Parameter",]
		self.columnNames += ["Value",]
		self.columnNames += ["Use",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 3

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0):
			if(row == 0): return "Cav. Phase Guess[deg]"
			if(row == 1): return "Input Ekin [MeV]"
			if(row == 2): return "Cav. Ampl. Scale Coeff."
		cav_wrapper = self.cav_controller.cav_wrapper
		if(col == 1):
			if(row == 0): return "%6.2f"%cav_wrapper.cav_phase_guess
			if(row == 1): return "%8.4f"%cav_wrapper.Ekin_in
			if(row == 2): return "%10.6f"%	cav_wrapper.cav_amp_coeff
		if(col == 2):
			if(row == 0): return cav_wrapper.cav_phase_guess_fit_use
			if(row == 1): return cav_wrapper.Ekin_in_fit_use
			if(row == 2): return cav_wrapper.cav_amp_coeff_fit_use
		return ""
				
	def getColumnClass(self,col):
		if(col == 0 or col == 1):
			return self.string_class	
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 0): return false
		if(col == 1): return true
		if(col == 2): return true
		return false
			
	def setValueAt(self, value, row, col):
		cav_wrapper = self.cav_controller.cav_wrapper
		if(col == 1):
			if(row == 0): cav_wrapper.cav_phase_guess = float(value)
			if(row == 1): cav_wrapper.Ekin_in = float(value)
			if(row == 2): cav_wrapper.cav_amp_coeff = float(value)
		if(col == 2):
			if(row == 0): cav_wrapper.cav_phase_guess_fit_use = value
			if(row == 1): cav_wrapper.Ekin_in_fit_use = value
			if(row == 2): cav_wrapper.cav_amp_coeff_fit_use = value
	    
class Found_Params_Table_Model(AbstractTableModel):
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.columnNames = ["Parameter",]
		self.columnNames += ["Value",]
		self.columnNames += ["Design",]
		self.string_class = String().getClass()
		self.boolean_class = Boolean(true).getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 4

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		cav_wrapper = self.cav_controller.cav_wrapper		
		if(col == 0):
			if(row == 0): return "Cavity Phase[deg]"
			if(row == 1): return "Cavity Amplitude"			
			if(row == 2): return "Input  Ekin [MeV]"
			if(row == 3): return "Output Ekin [MeV]"
		if(col == 1):
			if(row == 0): return "%6.2f"%cav_wrapper.newPhase
			if(row == 1): return "%8.4f"%cav_wrapper.newAmp
			if(row == 2): return "%7.3f"%	cav_wrapper.Ekin_in
			if(row == 3): return "%7.3f"%	cav_wrapper.Ekin_out
		if(col == 2):
			if(row == 2): return "%7.3f +- %5.3f"%(cav_wrapper.Ekin_in_design,cav_wrapper.Ekin_in_delta_design)
			if(row == 3): return "%7.3f +- %5.3f"%(cav_wrapper.Ekin_out_design,cav_wrapper.Ekin_out_delta_design)
		return ""
				
	def getColumnClass(self,col):
		return self.string_class	
	
	def isCellEditable(self,row,col):
		return false
			
	def setValueAt(self, value, row, col):
		pass
	    			
#------------------------------------------------------------------------
#           Auxiliary Controllers
#------------------------------------------------------------------------		
class Cavity_BPMs_Controller:
	def __init__(self,main_loop_controller,cav_controller):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		#---- local BPMs
		self.local_bpm_wrappers = []
		for bpm_wrapper in self.main_loop_controller.bpm_wrappers:
			if(self.useThisBPM(bpm_wrapper)):
				self.local_bpm_wrappers.append(bpm_wrapper)
		#------------------------------------------
		self.bpm_scan_data_amp = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,self.local_bpm_wrappers[0])
		#---- self.pasta_scan_data_arr[[cav_amp,bpm_scan_data_0,bpm_scan_data_1],...]
		self.pasta_scan_data_arr = []
		#-------------------------------------------
		self.amp_front_phase_pos = 0.
		self.phase_shift_from_front_dtl1 = 50.0
						
	def useThisBPM(self,bpm_wrapper):
		""" This method could be overriden to accomodate other beam lines. """
		if(bpm_wrapper.alias.find("DTL:BPM203") >= 0 or bpm_wrapper.alias.find("DTL:BPM209") >= 0):
			return true
		return false
			
	def clean(self):
		self.bpm_scan_data_amp.clean()
		self.pasta_scan_data_arr = []
		self.amp_front_phase_pos = 0.
		
	def cleanPASTA_Scan(self):
		self.pasta_scan_data_arr = []
	
	def addPointToBPM_AmpData(self,cav_phase):
		self.bpm_scan_data_amp.addPoint(cav_phase)
			
	def setCavAmpToBPM_ampData(self):
		cav_amp = self.cav_controller.cav_wrapper.getLiveAmp()
		self.bpm_scan_data_amp.setCavAmplitudeParam(cav_amp)	
			
	def addNewCavAmpPastaDataSet(self,cav_amp):
		bpm_scan_data_0 = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,self.local_bpm_wrappers[0])
		bpm_scan_data_1 = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,self.local_bpm_wrappers[1])
		bpm_scan_data_0.setCavAmplitudeParam(cav_amp)
		bpm_scan_data_1.setCavAmplitudeParam(cav_amp)
		self.pasta_scan_data_arr.append([cav_amp,bpm_scan_data_0,bpm_scan_data_1])
			
	def addPointToPastaDataSet(self,cav_phase):
		n_data = len(self.pasta_scan_data_arr)
		if(n_data == 0): return
		[cav_amp,bpm_scan_data_0,bpm_scan_data_1] = self.pasta_scan_data_arr[n_data - 1]
		bpm_scan_data_0.addPoint(cav_phase)
		bpm_scan_data_1.addPoint(cav_phase)
			
	def checkLastDataForBPM_AmpData(self):
		res = self.bpm_scan_data_amp.checkLastDataPoint(0.)
		if(not res):
				self.bpm_scan_data_amp.removeLastPoint()
		return res
		
	def checkLastDataForPastaDataSet(self):
		min_bpm_amp = self.cav_controller.scan_main_panel.min_bpm_amp_text.getValue()
		n_data = len(self.pasta_scan_data_arr)
		if(n_data == 0): return true		
		[cav_amp,bpm_scan_data_0,bpm_scan_data_1] = self.pasta_scan_data_arr[n_data - 1]
		res = true
		if(not bpm_scan_data_0.checkLastDataPoint(min_bpm_amp)):
			res = false
		else:
			if(not bpm_scan_data_1.checkLastDataPoint(min_bpm_amp)):
				res = false
		if(not res):
			bpm_scan_data_0.removeLastPoint()
			bpm_scan_data_1.removeLastPoint()
		return res
				
	def findBPM_AmpFrontPosition(self):
		bpm_amp_gd = self.bpm_scan_data_amp.amp_gd
		if(bpm_amp_gd.getNumbOfPoints() < 8): return (false,0.)
		max_amp = 0.
		max_amp_pos = 0.
		for ind in range(bpm_amp_gd.getNumbOfPoints()):
			if(max_amp < bpm_amp_gd.getY(ind)):
				max_amp = bpm_amp_gd.getY(ind)
				max_amp_pos = bpm_amp_gd.getX(ind)
		#-------- find the front pos at half of the max amplitude value
		bpm_amp_shifted_gd = BasicGraphData()
		for ind in range(bpm_amp_gd.getNumbOfPoints()):
			bpm_amp_shifted_gd.addPoint(makePhaseNear(bpm_amp_gd.getX(ind)-max_amp_pos,0.),bpm_amp_gd.getY(ind))
		ind0 = -1
		val0 = 0.
		val1 = 0.
		for ind in range(bpm_amp_shifted_gd.getNumbOfPoints()-1):
			val0 = bpm_amp_shifted_gd.getY(ind) - max_amp/2.0
			val1 = bpm_amp_shifted_gd.getY(ind+1) - max_amp/2.0
			if(val1 > val0 and val0*val1 <= 0):
				ind0 = ind 
				break
		if(ind0 < 0):
			return (false,0.)
		delta_x = bpm_amp_shifted_gd.getX(ind0+1) - bpm_amp_shifted_gd.getX(ind0)
		front_pos = bpm_amp_shifted_gd.getX(ind0) + delta_x*math.fabs(val0)/(math.fabs(val0)+math.fabs(val1))
		front_pos = makePhaseNear(front_pos+max_amp_pos,0.)
		self.amp_front_phase_pos = front_pos
		#------------------------------
		cav_wrapper = self.cav_controller.cav_wrapper
		cav_wrapper.newPhase = makePhaseNear(self.amp_front_phase_pos + self.phase_shift_from_front_dtl1,0.)
		cav_wrapper.cav_phase_guess = cav_wrapper.newPhase
		return (true,cav_wrapper.cav_phase_guess)
		
	def writeDataToXML(self,root_da):
		cav_bpms_cntrl_da = root_da.createChild("cavity_bpms_controller")
		cav_bpms_cntrl_da.setValue("cav",self.cav_controller.cav_wrapper.alias)
		cav_bpms_cntrl_da.setValue("amp_front_phase_pos",self.amp_front_phase_pos)
		cav_bpms_cntrl_da.setValue("phase_shift_from_front",self.phase_shift_from_front_dtl1)
		#---------------------------------------------------
		self.bpm_scan_data_amp.writeDataToXML(cav_bpms_cntrl_da)
		pasta_scan_data_da = cav_bpms_cntrl_da.createChild("pasta_scan_data")
		for [cav_amp,bpm_scan_data0,bpm_scan_data1] in self.pasta_scan_data_arr:
			pasta_record_da = pasta_scan_data_da.createChild("pasta_record")
			pasta_record_da.setValue("cav_amp",cav_amp)
			pasta_data0_da = pasta_record_da.createChild("pasta_data_bpm_0")
			bpm_scan_data0.writeDataToXML(pasta_data0_da)
			pasta_data1_da = pasta_record_da.createChild("pasta_data_bpm_1")
			bpm_scan_data1.writeDataToXML(pasta_data1_da)

	def readDataFromXML(self,cav_bpms_cntrl_da):	
		self.amp_front_phase_pos = cav_bpms_cntrl_da.doubleValue("amp_front_phase_pos")
		self.phase_shift_from_front_dtl1 = cav_bpms_cntrl_da.doubleValue("phase_shift_from_front")
		#-------------------------------------------------------------
		bpm_scan_data_da = cav_bpms_cntrl_da.childAdaptor("bpm_scan_data")		
		self.bpm_scan_data_amp.readDataFromXML(bpm_scan_data_da)
		#-------------------------------------------------------------
		self.pasta_scan_data_arr = []
		pasta_scan_data_da = cav_bpms_cntrl_da.childAdaptor("pasta_scan_data")
		pasta_record_da_list = pasta_scan_data_da.childAdaptors("pasta_record")
		for pasta_record_da in pasta_record_da_list:
			cav_amp = pasta_record_da.doubleValue("cav_amp")
			pasta_data0_da = pasta_record_da.childAdaptor("pasta_data_bpm_0")
			pasta_data1_da = pasta_record_da.childAdaptor("pasta_data_bpm_1")
			bpm_scan_data0 = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,self.local_bpm_wrappers[0])
			bpm_scan_data1 = BPM_Scan_Data(self.main_loop_controller,self.cav_controller,self.local_bpm_wrappers[1])
			bpm_scan_data0.readDataFromXML(pasta_data0_da.childAdaptor("bpm_scan_data"))
			bpm_scan_data1.readDataFromXML(pasta_data1_da.childAdaptor("bpm_scan_data"))
			self.pasta_scan_data_arr.append([cav_amp,bpm_scan_data0,bpm_scan_data1])
		
#------------------------------------------------------------------------
#           DTL1 Cavity Controller
#------------------------------------------------------------------------
class DTL1_Cavity_Controller(Abstract_Cavity_Controller):
	def __init__(self,main_loop_controller,cav_wrapper):
		Abstract_Cavity_Controller.__init__(self,main_loop_controller,cav_wrapper)
		self.scan_main_panel = Scan_Main_Panel(self.main_loop_controller,self)
		self.cav_bpms_controller = Cavity_BPMs_Controller(self.main_loop_controller,self)
		self.graphs_panel = Graphs_Panel(self.main_loop_controller,self)
		remove_one_point_button = JButton(" Remove One Cav. Phase Point ")
		remove_one_point_button.addActionListener(Remove_One_Point_Button_Listener(main_loop_controller,self))
		graphs_button_panel = JPanel(BorderLayout())
		graphs_button_panel.add(self.graphs_panel,BorderLayout.CENTER)
		graphs_button_panel.add(remove_one_point_button,BorderLayout.SOUTH)		
		self.local_bpms_and_params_tables_panel = Local_BPMs_and_Params_Tables_Panel(self.main_loop_controller,self)
		self.pasta_fitting_diff_calculator = PASTA_Fitting_Diff_Calculator(self.main_loop_controller,self)
		self.pasta_fitter = PASTA_Fitter(self.main_loop_controller,self)
		self.getMainPanel().add(self.scan_main_panel,BorderLayout.NORTH)
		self.getMainPanel().add(graphs_button_panel,BorderLayout.CENTER)
		self.getMainPanel().add(self.local_bpms_and_params_tables_panel,BorderLayout.WEST)
		self.graphs_panel.updateGraphs()
		self.local_scan_run_state = Local_Scan_Run_State()
				
	def runSetUpAlgorithm(self):
		""" Returns (true, text) in the case of the success """
		self.setMaxTimeCount()
		if(self.main_loop_controller.loop_run_state.shouldStop): return (false,"User requested stop! Cavity="+self.cav_wrapper.alias)
		if(self.cav_wrapper.initAmp == 0.): return (false,"Push Init Button First!")
		self.cav_wrapper.newAmp = 0.
		self.cav_wrapper.newPhase = 0.
		self.cav_bpms_controller.clean()
		self.graphs_panel.updateGraphs()	
		bpm_scan_data_amp = self.cav_bpms_controller.bpm_scan_data_amp
		phase_step = self.scan_main_panel.full_scan_phase_step_text.getValue()
		sleep_time = self.scan_main_panel.time_step_text.getValue()
		#------------------------------------
		cav_amp_steps_mult = int(self.cav_amp_backward_steps_mult_text.getValue())
		cav_amp_time_mult = int(self.cav_amp_wait_time_mult_text.getValue())
		#---- start of 360 deg scan measurements
		self.graphs_panel.setSelectedIndex(0)
		cav_phase = -180.
		miss_count = 0.
		miss_count_max = 5
		self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
		bpm_scan_data_amp.setCavAmplitudeParam(self.cav_wrapper.initAmp)	
		while(cav_phase <= 180.):
			if(miss_count > miss_count_max):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot get valid BPM data for 360 deg scan! Stop. Cavity="+self.cav_wrapper.alias)
			self.cav_wrapper.setLivePhase(cav_phase)
			self.timeSleep(sleep_time)
			if(self.main_loop_controller.loop_run_state.shouldStop):	
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
				self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)						
				return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)				
			self.cav_bpms_controller.addPointToBPM_AmpData(cav_phase)
			if(not self.cav_bpms_controller.checkLastDataForBPM_AmpData()):
				#print "debug not good data for 360 deg scan cav_phase=",cav_phase," count=",miss_count
				miss_count += 1
				continue
			#print "debug not good data for 360 deg scan cav_phase=",cav_phase
			self.graphs_panel.refreshGraphJPanels()
			cav_phase += phase_step
		#--- end of the 360 deg scan measurements loop
		#---- make a guess for the cavity phase
		(res,cav_phase_guess) = self.cav_bpms_controller.findBPM_AmpFrontPosition()
		if(not res):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot find the half-hight bpm amplitude position! Cavity="+self.cav_wrapper.alias)		
		#----- start PASTA scan measurements
		self.graphs_panel.setSelectedIndex(1)		
		self.graphs_panel.setFoundCavityPhase(cav_phase_guess)
		self.local_bpms_and_params_tables_panel.updateAllTables()
		min_amp = self.scan_main_panel.min_cav_amp_text.getValue()
		max_amp = self.scan_main_panel.max_cav_amp_text.getValue()
		nAmpSteps = int(self.scan_main_panel.nsteps_cav_amp_text.getValue())
		if(nAmpSteps < 1): nAmpSteps = 1
		amp_step = (max_amp - min_amp)/nAmpSteps
		cav_phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
		phase_step = self.scan_main_panel.scan_phase_step_text.getValue()
		cav_phase_start = cav_phase_guess - cav_phase_width/2.0
		cav_phase_stop = cav_phase_start + cav_phase_width		
		cav_amp = max_amp
		miss_count_max = 5
		for amp_ind in range(nAmpSteps+1):
			cav_amp = max_amp - amp_ind*amp_step
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)		
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			self.cav_bpms_controller.addNewCavAmpPastaDataSet(cav_amp)
			self.graphs_panel.updateGraphs()			
			cav_phase = cav_phase_start
			miss_count = 0.
			while(cav_phase <= cav_phase_stop):
				if(miss_count > miss_count_max):
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)
					return (false,"Cannot get valid BPM data for PASTA scan! Stop. Cavity="+self.cav_wrapper.alias)
				self.cav_wrapper.setLivePhase(cav_phase)
				self.timeSleep(sleep_time)
				if(self.main_loop_controller.loop_run_state.shouldStop):	
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)						
					return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)					
				self.cav_bpms_controller.addPointToPastaDataSet(cav_phase)
				if(not self.cav_bpms_controller.checkLastDataForPastaDataSet()):
					miss_count += 1
					continue
				self.graphs_panel.updateGraphs()
				#---- end loop over cavity's phase	
				cav_phase += phase_step
			#---- end loop over cavity's amplitude
		#---- restore the initial amplitude
		cav_amp += amp_step/cav_amp_steps_mult
		while(cav_amp <= self.cav_wrapper.initAmp):				
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			cav_amp += amp_step/cav_amp_steps_mult
		cav_amp = self.cav_wrapper.initAmp
		self.cav_wrapper.setLiveAmp(cav_amp)
		self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)
		self.timeSleep(3*sleep_time)
 		#---- analysis---------------------------------------
		#---- 1st analysis step - guess initial fitting params
		(res,txt) = self.guessInitialPASTA_Params()
		if(not res):
			return (res,txt)
		#---- 2nd analysis step - PASTA fitting
		(res,txt) = self.pasta_fitter.fit()
		if(not res):
			return (res,txt)
		if(not res):
			self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
			self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
			self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)
			self.timeSleep(3*sleep_time)
			return (false,txt)
		self.graphs_panel.updateGraphs()
		self.graphs_panel.setSelectedIndex(1)
		#---- here we restore the initial phase and amp., but the new values 
		#---- will be sent to EPICS by the main loop controller if it is not a fake scan 
		self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
		self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
		self.timeSleep(3*sleep_time)			
		return (true,"")		

	def init(self):
		""" reads the pv values """
		Abstract_Cavity_Controller.init(self)
		#------ set up amp scan parameters
		initAmp_max = self.cav_wrapper.initAmp*1.025
		initAmp_min = self.cav_wrapper.initAmp*0.975
		nAmpStep = 2
		self.scan_main_panel.min_cav_amp_text.setValue(initAmp_min)
		self.scan_main_panel.max_cav_amp_text.setValue(initAmp_max)
		self.scan_main_panel.nsteps_cav_amp_text.setValue(nAmpStep)
		self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)

	def checkBPM_Usage(self,bpm_wrapper):
		""" 
		Implementation of the abstarct method. 
		Returns True or False about this controller usage of the BPM 
		"""
		cav_bpms_controller = self.cav_bpms_controller
		local_bpm_wrappers = cav_bpms_controller.local_bpm_wrappers
		if(local_bpm_wrappers[0] == bpm_wrapper): return true
		if(local_bpm_wrappers[1] == bpm_wrapper): return true
		return false

	def setMaxTimeCount(self):
		""" This is implementation of the abstarct method of the parent class."""
		totalTimeCount = self.setMaxTimeCountFor360() + self.setMaxTimeCountForPASTA()
		self.scan_progress_bar.setMaxTimeCount(totalTimeCount)	
		return self.scan_progress_bar.count_max
		
	def setMaxTimeCountFor360(self):
		totalTimeCount = 0.
		time_step = self.scan_main_panel.time_step_text.getValue()
		#----- 360 deg scan 
		totalTimeCount += time_step*(1.0+360./self.scan_main_panel.full_scan_phase_step_text.getValue())
		self.scan_progress_bar.setMaxTimeCount(totalTimeCount)	
		return self.scan_progress_bar.count_max
		
	def setMaxTimeCountForPASTA(self):
		totalTimeCount = 0.
		time_step = self.scan_main_panel.time_step_text.getValue()
		#----- PASTA-like scan
		phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
		phase_step = self.scan_main_panel.scan_phase_step_text.getValue()
		n_amp_steps = self.scan_main_panel.nsteps_cav_amp_text.getValue()
		n_amp_back_steps = self.cav_amp_backward_steps_mult_text.getValue()
		n_amp_back_time_mult = self.cav_amp_wait_time_mult_text.getValue()
		totalTimeCount += time_step*(n_amp_steps+1)*(1+phase_width/phase_step)
		totalTimeCount += time_step*(n_amp_steps+1)*n_amp_back_time_mult
		totalTimeCount += time_step*(n_amp_back_steps+1)*n_amp_back_time_mult
		self.scan_progress_bar.setMaxTimeCount(totalTimeCount)	
		return self.scan_progress_bar.count_max		
		
	def getPastaFittingTime(self):
		""" This is a override of the parent method """
		return self.scan_main_panel.time_limit_fit_text.getValue()

	def runFullRangeScan(self):
		""" Returns (true, text) in the case of the success """
		self.setMaxTimeCountFor360() 
		if(self.main_loop_controller.loop_run_state.shouldStop): return (false,"User requested stop! Cavity="+self.cav_wrapper.alias)
		if(self.cav_wrapper.initAmp == 0.): return (false,"Push Init Button First!")
		if(self.local_scan_run_state.shouldStop): return (false,"User requested stop! Cavity="+self.cav_wrapper.alias)
		self.cav_wrapper.newAmp = 0.
		self.cav_wrapper.newPhase = 0.
		self.cav_bpms_controller.clean()
		self.graphs_panel.updateGraphs()	
		bpm_scan_data_amp = self.cav_bpms_controller.bpm_scan_data_amp
		phase_step = self.scan_main_panel.full_scan_phase_step_text.getValue()
		sleep_time = self.scan_main_panel.time_step_text.getValue()
		#---- start of sine wave masurements
		self.graphs_panel.setSelectedIndex(0)
		cav_phase = -180.
		miss_count = 0.
		miss_count_max = 5
		self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
		bpm_scan_data_amp.setCavAmplitudeParam(self.cav_wrapper.initAmp)	
		while(cav_phase <= 180.):
			if(miss_count > miss_count_max):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot get valid BPM data for 360 deg scan! Stop. Cavity="+self.cav_wrapper.alias)
			self.cav_wrapper.setLivePhase(cav_phase)
			self.timeSleep(sleep_time)
			if(self.main_loop_controller.loop_run_state.shouldStop or self.local_scan_run_state.shouldStop):	
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
				self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)						
				return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)				
			self.cav_bpms_controller.addPointToBPM_AmpData(cav_phase)
			if(not self.cav_bpms_controller.checkLastDataForBPM_AmpData()):
				#print "debug not good data for 360 deg scan cav_phase=",cav_phase," count=",miss_count
				miss_count += 1
				continue
			#print "debug not good data for 360 deg scan cav_phase=",cav_phase
			self.graphs_panel.refreshGraphJPanels()
			cav_phase += phase_step
		#--- end of the 360 deg scan measurements loop
		#---- make a guess for the cavity phase
		(res,cav_phase_guess) = self.cav_bpms_controller.findBPM_AmpFrontPosition()
		self.local_bpms_and_params_tables_panel.updateAllTables()
		if(not res):
				self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
				return (false,"Cannot find the half-hight bpm amplitude position! Cavity="+self.cav_wrapper.alias)		
		self.graphs_panel.setSelectedIndex(1)		
		self.graphs_panel.setFoundCavityPhase(cav_phase_guess)
		return (true,"")

	def runPASTA_Scan(self):
		self.setMaxTimeCountForPASTA()		
		cav_phase_guess = self.cav_wrapper.cav_phase_guess
		self.graphs_panel.setSelectedIndex(1)
		self.graphs_panel.setFoundCavityPhase(cav_phase_guess)
		#------------------------------------
		self.cav_bpms_controller.cleanPASTA_Scan()
		self.graphs_panel.updateGraphs()
		#------------------------------------
		cav_amp_steps_mult = int(self.cav_amp_backward_steps_mult_text.getValue())
		cav_amp_time_mult = int(self.cav_amp_wait_time_mult_text.getValue())
		#------------------------------------
		sleep_time = self.scan_main_panel.time_step_text.getValue()
		min_amp = self.scan_main_panel.min_cav_amp_text.getValue()
		max_amp = self.scan_main_panel.max_cav_amp_text.getValue()
		nAmpSteps = int(self.scan_main_panel.nsteps_cav_amp_text.getValue())
		if(nAmpSteps < 1): nAmpSteps = 1
		amp_step = (max_amp - min_amp)/nAmpSteps
		cav_phase_width = self.scan_main_panel.scan_phase_width_text.getValue()
		phase_step = self.scan_main_panel.scan_phase_step_text.getValue()
		cav_phase_start = cav_phase_guess - cav_phase_width/2.0
		cav_phase_stop = cav_phase_start + cav_phase_width		
		cav_amp = max_amp
		miss_count_max = 5
		for amp_ind in range(nAmpSteps+1):
			cav_amp = max_amp - amp_ind*amp_step
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)		
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			self.cav_bpms_controller.addNewCavAmpPastaDataSet(cav_amp)
			self.graphs_panel.updateGraphs()			
			cav_phase = cav_phase_start
			miss_count = 0.
			while(cav_phase <= cav_phase_stop):
				if(miss_count > miss_count_max):
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)	
					return (false,"Cannot get valid BPM data for PASTA scan! Stop. Cavity="+self.cav_wrapper.alias)
				self.cav_wrapper.setLivePhase(cav_phase)
				self.timeSleep(sleep_time)
				if(self.main_loop_controller.loop_run_state.shouldStop or self.local_scan_run_state.shouldStop):	
					self.cav_wrapper.setLivePhase(self.cav_wrapper.initPhase)
					self.cav_wrapper.setLiveAmp(self.cav_wrapper.initAmp)
					self.scan_main_panel.live_cav_amp_text.setValue(self.cav_wrapper.initAmp)
					return (false,"Scan stopped by user's request. Cavity="+self.cav_wrapper.alias)					
				self.cav_bpms_controller.addPointToPastaDataSet(cav_phase)
				if(not self.cav_bpms_controller.checkLastDataForPastaDataSet()):
					miss_count += 1
					continue
				self.graphs_panel.updateGraphs()
				#---- end loop over cavity's phase	
				cav_phase += phase_step
			#---- end loop over cavity's amplitude
		#---- restore the initial amplitude
		cav_amp += amp_step/cav_amp_steps_mult
		while(cav_amp <= self.cav_wrapper.initAmp):
			self.cav_wrapper.setLiveAmp(cav_amp)
			self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)
			self.timeSleep(cav_amp_time_mult*sleep_time)
			cav_amp += amp_step/cav_amp_steps_mult
		cav_amp = self.cav_wrapper.initAmp
		self.cav_wrapper.setLiveAmp(cav_amp)
		self.timeSleep(cav_amp_time_mult*sleep_time)
		self.scan_main_panel.live_cav_amp_text.setValue(cav_amp)
		return (true,"")

	def guessInitialPASTA_Params(self):
		self.main_loop_controller.getMessageTextField().setText("")
		cav_bpms_controller = self.cav_bpms_controller	
		cav_wrapper = self.cav_wrapper
		#--------------------------------------------------
		if(cav_wrapper.initAmp < 0.001):
			txt = "Please initialize the the scan wizard!"
			return (false,txt)
		#--------------------------------------------------
		(res,cav_phase_guess) = cav_bpms_controller.findBPM_AmpFrontPosition()
		if(not res):
			txt = "Cannot find the half-hight bpm amplitude position!"
			return (false,txt)
		#--------------------------------------------------
		cav_wrapper.cav_phase_guess = cav_phase_guess
		cav_wrapper.Ekin_in = cav_wrapper.Ekin_in_design
		cav_wrapper.Ekin_out = cav_wrapper.Ekin_out_design
		cav_wrapper.cav_amp_coeff = cav_wrapper.design_amp/cav_wrapper.initAmp
		cav_wrapper.newAmp = cav_wrapper.initAmp
		self.local_bpms_and_params_tables_panel.updateAllTables()
		#--------------------------------------------------
		pasta_scan_data_arr = cav_bpms_controller.pasta_scan_data_arr
		if(len(pasta_scan_data_arr) < 1):
			txt = "No PASTA scan data! Stop."
			return (false,txt)	
		bpm_diff_phase_avg = 0.
		for [cav_amp,bpm_scan_data0,bpm_scan_data1] in pasta_scan_data_arr:
			phase0 = bpm_scan_data0.phase_gd.getValueY(cav_phase_guess)
			phase1 = bpm_scan_data1.phase_gd.getValueY(cav_phase_guess)
			bpm_diff_phase_avg += makePhaseNear(phase1 - phase0,0.)
		bpm_diff_phase_avg /= len(pasta_scan_data_arr)
		particle_tracker_model = self.main_loop_controller.particle_tracker_model
		particle_tracker_model.setActiveCavity(cav_wrapper)
		Ekin_in = cav_wrapper.Ekin_in_design
		cav_phase = cav_wrapper.design_phase 
		local_bpm_wrappers = cav_bpms_controller.local_bpm_wrappers
		[bpm_phase0,bpm_phase1] = particle_tracker_model.getBPM_Phases(Ekin_in,cav_phase,0.,local_bpm_wrappers)
		bpm_diff_phase_design = makePhaseNear(bpm_phase1 - bpm_phase0,0.)
		#---- the bpm diff. phase shift is -12 deg on +20 keV
		Ekin_delta = - 0.02*(bpm_diff_phase_avg - bpm_diff_phase_design)/12.0
		Ekin_in_guess = Ekin_in + Ekin_delta 		
		cav_wrapper.Ekin_in = Ekin_in_guess
		particle_tracker_model.restoreDesignAmpPhases()
		particle_tracker_model.scenario.resync()
		particle_tracker_model.trackProbe(cav_wrapper.Ekin_in)
		cav_wrapper.Ekin_out = particle_tracker_model.getEkin_Out()
		#---------------------------------------------
		[cav_amp,bpm_scan_data0,bpm_scan_data1] = pasta_scan_data_arr[0]
		phase_min = bpm_scan_data0.phase_gd.getX(0)
		phase_max = bpm_scan_data0.phase_gd.getX(bpm_scan_data0.phase_gd.getNumbOfPoints()-1)
		self.scan_main_panel.start_fit_phase_text.setValue(phase_min)
		self.scan_main_panel.stop_fit_phase_text.setValue(phase_max)
		#---------------------------------------------
		nPhasePoints = 30
		Ekin_in = cav_wrapper.Ekin_in
		cav_amp_coeff = cav_wrapper.cav_amp_coeff
		cav_phase = cav_wrapper.cav_phase_guess
		phase_shift = cav_wrapper.cav_phase_guess - cav_wrapper.design_phase 
		(res,txt,phase_diff2) = self.pasta_fitting_diff_calculator.caluclateDiff(nPhasePoints,Ekin_in,cav_amp_coeff,phase_shift)
		cav_wrapper.avg_pasta_err = math.sqrt(phase_diff2)
		self.local_bpms_and_params_tables_panel.pasta_error_text.setValue(math.sqrt(phase_diff2))
		if(not res):
			return (res,txt)
		#---------------------------------------------
		self.graphs_panel.setSelectedIndex(1)	
		self.graphs_panel.updateGraphs()
		if(math.fabs(Ekin_delta) > 0.025):
			txt = "The MEBT Reb 4 settings could be wrong! The DTL1 input energy is %8.4f "%Ekin_in_guess
			reurn (false,txt)	
		return (true,"")

	def writeDataToXML(self,root_da):
		cav_cntrl_data_da = root_da.createChild("CAVITY_CONTROLLER_"+self.cav_wrapper.alias)
		cav_cntrl_params_da =  cav_cntrl_data_da.createChild("PARAMS")
		cav_cntrl_params_da.setValue("full_scan_phase_step",self.scan_main_panel.full_scan_phase_step_text.getValue())
		cav_cntrl_params_da.setValue("sleep_time",self.scan_main_panel.time_step_text.getValue())
		cav_cntrl_params_da.setValue("min_amp",self.scan_main_panel.min_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("max_amp",self.scan_main_panel.max_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("n_amp_step",self.scan_main_panel.nsteps_cav_amp_text.getValue())
		cav_cntrl_params_da.setValue("cav_phase_width",self.scan_main_panel.scan_phase_width_text.getValue())
		cav_cntrl_params_da.setValue("phase_step",self.scan_main_panel.scan_phase_step_text.getValue())
		cav_cntrl_params_da.setValue("min_phase_fit",self.scan_main_panel.start_fit_phase_text.getValue())
		cav_cntrl_params_da.setValue("max_phase_fit",self.scan_main_panel.stop_fit_phase_text.getValue())
		cav_cntrl_params_da.setValue("fit_points",self.scan_main_panel.n_points_fit_text.getValue())
		cav_cntrl_params_da.setValue("fit_time",self.scan_main_panel.time_limit_fit_text.getValue())
		#---------------------------------------------------------
		self.wrtiteAbstractCntrlToXML(cav_cntrl_data_da)
		#---------------------------------------------------------
		cav_wrapper_da = cav_cntrl_data_da.createChild("CAV_WRAPPER_PARAMS")
		self.cav_wrapper.writeDataToXML(cav_wrapper_da)
		self.cav_bpms_controller.writeDataToXML(cav_cntrl_data_da)
		
	def readDataFromXML(self,cav_cntrl_data_da):	
		params_da = cav_cntrl_data_da.childAdaptor("PARAMS")
		self.scan_main_panel.full_scan_phase_step_text.setValue(params_da.doubleValue("full_scan_phase_step"))
		self.scan_main_panel.time_step_text.setValue(params_da.doubleValue("sleep_time"))
		self.scan_main_panel.min_cav_amp_text.setValue(params_da.doubleValue("min_amp"))
		self.scan_main_panel.max_cav_amp_text.setValue(params_da.doubleValue("max_amp"))
		self.scan_main_panel.nsteps_cav_amp_text.setValue(params_da.doubleValue("n_amp_step"))
		self.scan_main_panel.scan_phase_width_text.setValue(params_da.doubleValue("cav_phase_width"))
		self.scan_main_panel.scan_phase_step_text.setValue(params_da.doubleValue("phase_step"))
		self.scan_main_panel.n_points_fit_text.setValue(params_da.doubleValue("fit_points"))
		self.scan_main_panel.time_limit_fit_text.setValue(params_da.doubleValue("fit_time"))
		self.scan_main_panel.start_fit_phase_text.setValue(params_da.doubleValue("min_phase_fit"))
		self.scan_main_panel.stop_fit_phase_text.setValue(params_da.doubleValue("max_phase_fit"))
		#-------------------------------------------------
		self.readAbstractCntrlFromXML(cav_cntrl_data_da)
		#-------------------------------------------------
		cav_wrapper_da = cav_cntrl_data_da.childAdaptor("CAV_WRAPPER_PARAMS")
		self.cav_wrapper.readDataFromXML(cav_wrapper_da)
		cav_bpms_cntrl_da = cav_cntrl_data_da.childAdaptor("cavity_bpms_controller")
		self.cav_bpms_controller.readDataFromXML(cav_bpms_cntrl_da)
		self.graphs_panel.updateGraphs()	
		self.local_bpms_and_params_tables_panel.pasta_error_text.setValue(self.cav_wrapper.avg_pasta_err)


