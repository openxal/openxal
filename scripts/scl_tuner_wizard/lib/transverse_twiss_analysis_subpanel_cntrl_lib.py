# The subpanel of the transverse Twiss WS/LW data analysis main panel
# This module consists of the classes for data fitting based on the online model

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
from java.awt import Dimension

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap
from xal.extension.wirescan.apputils import GaussFitter, WireScanData
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory

from constants_lib import GRAPH_LEGEND_KEY
from ws_lw_acquisition_cntrl_lib import WS_DIRECTION_HOR, WS_DIRECTION_VER, WS_DIRECTION_NULL
from transverse_twiss_fitter_lib import Twiss_Fitting_Listener, Stop_Twiss_Fitting_Listener, One_Pass_Listener, Twiss_Fitter

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

class Twiss_Params_Holder:
	def __init__(self):
		#------paramArr[x,y,z][alpha,beta,emitt][value, error, fit step]
		self.paramArr = []
		self.paramArr.append([[0.,0.,0.],[0.,0.,0.],[0.,0.,0.]])
		self.paramArr.append([[0.,0.,0.],[0.,0.,0.],[0.,0.,0.]])
		self.paramArr.append([[0.,0.,0.],[0.,0.,0.],[0.,0.,0.]])
		
	def getParamArr(self):
		return self.paramArr
		
	def getAlphaX(self):
		return self.paramArr[0][0][0]
		
	def getAlphaX_Err(self):
		return self.paramArr[0][0][1]

	def getAlphaX_Step(self):
		return self.paramArr[0][0][2]
			
	def setAlphaX(self, alpha):
		self.paramArr[0][0][0] = alpha
		
	def setAlphaX_Err(self, alpha_err):
		self.paramArr[0][0][1] = alpha_err

	def setAlphaX_Step(self, alpha_step):
		self.paramArr[0][0][2] = alpha_step
		
	def getBetaX(self):
		return self.paramArr[0][1][0]
		
	def getBetaX_Err(self):
		return self.paramArr[0][1][1]

	def getBetaX_Step(self):
		return self.paramArr[0][1][2]
			
	def setBetaX(self, beta):
		self.paramArr[0][1][0] = beta
		
	def setBetaX_Err(self, beta_err):
		self.paramArr[0][1][1] = beta_err

	def setBetaX_Step(self, beta_step):
		self.paramArr[0][1][2] = beta_step
		
	def getEmittX(self):
		return self.paramArr[0][2][0]
		
	def getEmittX_Err(self):
		return self.paramArr[0][2][1]

	def getEmittX_Step(self):
		return self.paramArr[0][2][2]
			
	def setEmittX(self, emitt):
		self.paramArr[0][2][0] = emitt
		
	def setEmittX_Err(self, emitt_err):
		self.paramArr[0][2][1] = emitt_err

	def setEmittX_Step(self, emitt_step):
		self.paramArr[0][2][2] = emitt_step
		
	def getAlphaY(self):
		return self.paramArr[1][0][0]
		
	def getAlphaY_Err(self):
		return self.paramArr[1][0][1]

	def getAlphaY_Step(self):
		return self.paramArr[1][0][2]
			
	def setAlphaY(self, alpha):
		self.paramArr[1][0][0] = alpha
		
	def setAlphaY_Err(self, alpha_err):
		self.paramArr[1][0][1] = alpha_err

	def setAlphaY_Step(self, alpha_step):
		self.paramArr[1][0][2] = alpha_step
		
	def getBetaY(self):
		return self.paramArr[1][1][0]
		
	def getBetaY_Err(self):
		return self.paramArr[1][1][1]

	def getBetaY_Step(self):
		return self.paramArr[1][1][2]
			
	def setBetaY(self, beta):
		self.paramArr[1][1][0] = beta
		
	def setBetaY_Err(self, beta_err):
		self.paramArr[1][1][1] = beta_err

	def setBetaY_Step(self, beta_step):
		self.paramArr[1][1][2] = beta_step
		
	def getEmittY(self):
		return self.paramArr[1][2][0]
		
	def getEmittY_Err(self):
		return self.paramArr[1][2][1]

	def getEmittY_Step(self):
		return self.paramArr[1][2][2]
			
	def setEmittY(self, emitt):
		self.paramArr[1][2][0] = emitt
		
	def setEmittY_Err(self, emitt_err):
		self.paramArr[1][2][1] = emitt_err

	def setEmittY_Step(self, emitt_step):
		self.paramArr[1][2][2] = emitt_step	

	def getAlphaZ(self):
		return self.paramArr[2][0][0]
		
	def getAlphaZ_Err(self):
		return self.paramArr[2][0][1]

	def getAlphaZ_Step(self):
		return self.paramArr[2][0][2]
			
	def setAlphaZ(self, alpha):
		self.paramArr[2][0][0] = alpha
		
	def setAlphaZ_Err(self, alpha_err):
		self.paramArr[2][0][1] = alpha_err

	def setAlphaZ_Step(self, alpha_step):
		self.paramArr[2][0][2] = alpha_step
		
	def getBetaZ(self):
		return self.paramArr[2][1][0]
		
	def getBetaZ_Err(self):
		return self.paramArr[2][1][1]

	def getBetaZ_Step(self):
		return self.paramArr[2][1][2]
			
	def setBetaZ(self, beta):
		self.paramArr[2][1][0] = beta
		
	def setBetaZ_Err(self, beta_err):
		self.paramArr[2][1][1] = beta_err

	def setBetaZ_Step(self, beta_step):
		self.paramArr[2][1][2] = beta_step
		
	def getEmittZ(self):
		return self.paramArr[2][2][0]
		
	def getEmittZ_Err(self):
		return self.paramArr[2][2][1]

	def getEmittZ_Step(self):
		return self.paramArr[2][2][2]
			
	def setEmittZ(self, emitt):
		self.paramArr[2][2][0] = emitt
		
	def setEmittZ_Err(self, emitt_err):
		self.paramArr[2][2][1] = emitt_err

	def setEmittZ_Step(self, emitt_step):
		self.paramArr[2][2][2] = emitt_step	

	def printParams(self):
		print "==========================================="
		print "Alpha X val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[0][0][0],self.paramArr[0][0][1],self.paramArr[0][0][2])
		print "Beta  X val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[0][1][0],self.paramArr[0][1][1],self.paramArr[0][1][2])
		print "Emitt X val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[0][2][0],self.paramArr[0][2][1],self.paramArr[0][2][2])
		print "==========================================="
		print "Alpha Y val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[1][0][0],self.paramArr[1][0][1],self.paramArr[1][0][2])
		print "Beta  Y val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[1][1][0],self.paramArr[1][1][1],self.paramArr[1][1][2])
		print "Emitt Y val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[1][2][0],self.paramArr[1][2][1],self.paramArr[1][2][2])
		print "==========================================="
		print "Alpha Z val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[2][0][0],self.paramArr[2][0][1],self.paramArr[2][0][2])
		print "Beta  Z val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[2][1][0],self.paramArr[2][1][1],self.paramArr[2][1][2])
		print "Emitt Z val= %12.5g  err= %12.5g  step= %12.5g"%(self.paramArr[2][2][0],self.paramArr[2][2][1],self.paramArr[2][2][2])
		print "==========================================="

	def setParams(self, axis,alpha,beta,emitt):
		self.paramArr[axis][0][0] = alpha
		self.paramArr[axis][1][0] = beta
		self.paramArr[axis][2][0] = emitt

	def setParamsStep(self, axis,alphaStep,betaStep,emittStep):
		self.paramArr[axis][0][2] = alphaStep
		self.paramArr[axis][1][2] = betaStep
		self.paramArr[axis][2][2] = emittStep

	def setParamsErr(self, axis,alphaErr,betaErr,emittErr):
		self.paramArr[axis][0][1] = alphaErr
		self.paramArr[axis][1][1] = betaErr
		self.paramArr[axis][2][1] = emittErr

	def getParams(self, axis):
		alpha = self.paramArr[axis][0][0]
		beta = self.paramArr[axis][1][0]
		emitt = self.paramArr[axis][2][0]
		return [alpha,beta,emitt]
		
	def getParamsStep(self, axis):
		alphaStep = self.paramArr[axis][0][2]
		betaStep = self.paramArr[axis][1][2]
		emittStep = self.paramArr[axis][2][2]
		return [alphaStep,betaStep,emittStep]		
		
	def getParamsErr(self, axis):
		alphaErr = self.paramArr[axis][0][1]
		betaErr = self.paramArr[axis][1][1]
		emittErr = self.paramArr[axis][2][1]
		return [alphaErr,betaErr,emittErr]		
		
#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------

class Init_Twiss_Button_Listener(ActionListener):
	def __init__(self,transverse_twiss_fitting_controller):
		self.transverse_twiss_fitting_controller = transverse_twiss_fitting_controller
	
	def actionPerformed(self,actionEvent):
		linac_wizard_document = self.transverse_twiss_fitting_controller.linac_wizard_document		
		if(linac_wizard_document.getAccSeq() == null):
			return
		accSeq = linac_wizard_document.getAccSeq()
		quads = linac_wizard_document.ws_lw_controller.quads
		cavs = linac_wizard_document.ws_lw_controller.cavs
		env_tracker = AlgorithmFactory.createEnvTrackerAdapt(accSeq)
		design_probe = ProbeFactory.getEnvelopeProbe(accSeq,env_tracker)
		twiss_arr = design_probe.getCovariance().computeTwiss()
		initial_twiss_params_holder = self.transverse_twiss_fitting_controller.initial_twiss_params_holder
		ph = initial_twiss_params_holder
		#------------X--------------------
		ph.setAlphaX(twiss_arr[0].getAlpha())
		ph.setBetaX(twiss_arr[0].getBeta())
		ph.setEmittX(twiss_arr[0].getEmittance()*1.0e+6)
		ph.setAlphaX_Err(0.)
		ph.setBetaX_Err(0.)
		ph.setEmittX_Err(0.)
		ph.setAlphaX_Step(0.1)
		ph.setBetaX_Step(0.1)
		ph.setEmittX_Step(ph.getEmittX()*0.05)		
		#------------Y--------------------
		ph.setAlphaY(twiss_arr[1].getAlpha())
		ph.setBetaY(twiss_arr[1].getBeta())
		ph.setEmittY(twiss_arr[1].getEmittance()*1.0e+6)
		ph.setAlphaY_Err(0.)
		ph.setBetaY_Err(0.)
		ph.setEmittY_Err(0.)
		ph.setAlphaY_Step(0.1)
		ph.setBetaY_Step(0.1)
		ph.setEmittY_Step(ph.getEmittY()*0.05)
		#------------Z--------------------
		ph.setAlphaZ(twiss_arr[2].getAlpha())
		ph.setBetaZ(twiss_arr[2].getBeta())
		ph.setEmittZ(twiss_arr[2].getEmittance()*1.0e+6)
		ph.setAlphaZ_Err(0.)
		ph.setBetaZ_Err(0.)
		ph.setEmittZ_Err(0.)
		ph.setAlphaZ_Step(0.)
		ph.setBetaZ_Step(0.)
		ph.setEmittZ_Step(0.)			
		#initial_twiss_params_holder.printParams()
		self.transverse_twiss_fitting_controller.initTwiss_table.getModel().fireTableDataChanged()
		init_and_fit_params_controller = self.transverse_twiss_fitting_controller.init_and_fit_params_controller
		init_and_fit_params_controller.eKin_text.setValue(design_probe.getKineticEnergy()/1.0e+6)
		init_and_fit_params_controller.current_text.setValue(design_probe.getBeamCurrent()*1000)
		#eKin = design_probe.getKineticEnergy()
		#current = design_probe.getBeamCurrent()
		#freq = design_probe.getBunchFrequency()
		#print "debug eKin=",eKin/1.0e+6," curr=",current," freq=",freq 
		
class Copy_Twiss_Listener(ActionListener):
	def __init__(self,transverse_twiss_fitting_controller):
		self.transverse_twiss_fitting_controller = transverse_twiss_fitting_controller
	
	def actionPerformed(self,actionEvent):
		init_and_fit_params_controller = self.transverse_twiss_fitting_controller.init_and_fit_params_controller
		initial_twiss_params_holder = self.transverse_twiss_fitting_controller.initial_twiss_params_holder
		final_twiss_params_holder = init_and_fit_params_controller.final_twiss_params_holder
		ph_to = initial_twiss_params_holder	
		ph_from = final_twiss_params_holder
		#------------X--------------------
		ph_to .setAlphaX(ph_from.getAlphaX())
		ph_to .setBetaX(ph_from.getBetaX())
		ph_to .setEmittX(ph_from.getEmittX())
		ph_to .setAlphaX_Err(ph_from.getAlphaX_Err())
		ph_to .setBetaX_Err(ph_from.getBetaX_Err())
		ph_to .setEmittX_Err(ph_from.getEmittX_Err())
		#------------Y--------------------
		ph_to .setAlphaY(ph_from.getAlphaY())
		ph_to .setBetaY(ph_from.getBetaY())
		ph_to .setEmittY(ph_from.getEmittY())
		ph_to .setAlphaY_Err(ph_from.getAlphaY_Err())
		ph_to .setBetaY_Err(ph_from.getBetaY_Err())
		ph_to .setEmittY_Err(ph_from.getEmittY_Err())
		#------------Z--------------------
		ph_to .setAlphaZ(ph_from.getAlphaZ())
		ph_to .setBetaZ(ph_from.getBetaZ())
		ph_to .setEmittZ(ph_from.getEmittZ())
		ph_to .setAlphaZ_Err(ph_from.getAlphaZ_Err())
		ph_to .setBetaZ_Err(ph_from.getBetaZ_Err())
		ph_to .setEmittZ_Err(ph_from.getEmittZ_Err())		
		self.transverse_twiss_fitting_controller.initTwiss_table.getModel().fireTableDataChanged()

#------------------------------------------------
#  JTable models
#------------------------------------------------
class Init_Twiss_Table_Model(AbstractTableModel):
	def __init__(self,initial_twiss_params_holder):
		self.initial_twiss_params_holder = initial_twiss_params_holder
		self.columnNames = ["Name","value","Error","Fit Step"]
		self.rowNames = []
		tmpAxis = ["X","Y","Z"]
		for i in range(3):
			self.rowNames.append("Alpha "+tmpAxis[i])
			self.rowNames.append("Beta "+tmpAxis[i])
			self.rowNames.append("Emitt "+tmpAxis[i])
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(4)
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 9

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		axis_ind = int(row/3.0)
		par_index = row%3
		params_arr = self.initial_twiss_params_holder.getParamArr()
		if(col == 0): return self.rowNames[row]
		return self.nf.format(params_arr[axis_ind][par_index][col-1])
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 0 ):
			return false
		return true
			
	def setValueAt(self, value, row, col):
		axis_ind = int(row/3.0)
		par_index = row%3
		params_arr = self.initial_twiss_params_holder.getParamArr()	
		if(col > 0):
			params_arr[axis_ind][par_index][col-1] = Double.parseDouble(value)


class Final_Twiss_Table_Model(AbstractTableModel):
	def __init__(self,final_twiss_params_holder):
		self.final_twiss_params_holder = final_twiss_params_holder
		self.columnNames = ["Name","value","Error"]
		self.rowNames = []
		tmpAxis = ["X","Y","Z"]
		for i in range(3):
			self.rowNames.append("Alpha "+tmpAxis[i])
			self.rowNames.append("Beta "+tmpAxis[i])
			self.rowNames.append("Emitt "+tmpAxis[i])
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(4)
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return 9

	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		axis_ind = int(row/3.0)
		par_index = row%3
		params_arr = self.final_twiss_params_holder.getParamArr()
		if(col == 0): return self.rowNames[row]
		return self.nf.format(params_arr[axis_ind][par_index][col-1])
				
	def getColumnClass(self,col):
		return self.string_class
	
	def isCellEditable(self,row,col):
		if(col == 0 ):
			return false
		return true
			
	def setValueAt(self, value, row, col):
		axis_ind = int(row/3.0)
		par_index = row%3
		params_arr = self.final_twiss_params_holder.getParamArr()	
		if(col > 0):
			params_arr[axis_ind][par_index][col-1] = Double.parseDouble(value)

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class Transverse_Twiss_Fitting_Controller:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document
		#--------------------------------------------------------------
		self.main_panel = JPanel(BorderLayout())
		#-------------------------------------------------------------
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#-------------------------------------------------------------		
		self.initial_twiss_params_holder = Twiss_Params_Holder()
		self.initTwiss_table = JTable(Init_Twiss_Table_Model(self.initial_twiss_params_holder))
		self.initTwiss_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.initTwiss_table.setFillsViewportHeight(true)
		self.initTwiss_table.setPreferredScrollableViewportSize(Dimension(240,160))
		initTwiss_panel = JPanel(BorderLayout())
		initTwiss_panel.add(JScrollPane(self.initTwiss_table), BorderLayout.CENTER)
		initTwiss_knobs_panel = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		initTwiss_button = JButton("Init Twiss from Design")
		initTwiss_button.addActionListener(Init_Twiss_Button_Listener(self))
		initTwiss_knobs_panel.add(initTwiss_button)
		initTwiss_panel.add(initTwiss_knobs_panel, BorderLayout.SOUTH)
		border = BorderFactory.createTitledBorder(etched_border,"Initial Twiss Parameters")
		initTwiss_panel.setBorder(border)			
		#--------Init_and_Fit_Params_Controller panel ----------
		self.init_and_fit_params_controller = Init_and_Fit_Params_Controller(self.linac_wizard_document,self)
		#Twiss fitter is defined in the transverse_twiss_fitter_lib.py 
		self.twiss_fitter = null
		#----add panels to the main
		tmp_panel = JPanel(BorderLayout())
		tmp_panel.add(initTwiss_panel, BorderLayout.WEST)
		tmp_panel.add(self.init_and_fit_params_controller.getMainPanel(),BorderLayout.EAST)
		self.main_panel.add(tmp_panel, BorderLayout.WEST)
		
	def getMainPanel(self):
		return self.main_panel		

class Init_and_Fit_Params_Controller:
	def __init__(self,linac_wizard_document,transverse_twiss_fitting_controller):
		self.linac_wizard_document = linac_wizard_document
		self.transverse_twiss_fitting_controller = transverse_twiss_fitting_controller
		self.main_panel = JPanel(BorderLayout())
		tmp_panel = JPanel(GridLayout(7,2))
		self.eKin_text = DoubleInputTextField(0.,FortranNumberFormat("G8.6"),8)
		eKin_lbl = JLabel("eKin[MeV]=",JLabel.RIGHT)
		self.current_text = DoubleInputTextField(0.,FortranNumberFormat("G8.3"),8) 
		current_lbl = JLabel("Curr.[mA]=",JLabel.RIGHT)
		self.fit_err_text = DoubleInputTextField(0.,FortranNumberFormat("G8.3"),8) 
		fit_err_lbl = JLabel("Fit Err,%=",JLabel.RIGHT)	
		self.fit_err_text.setValue(5.0)
		self.fit_iter_text = DoubleInputTextField(0.,FortranNumberFormat("G8.0"),8)
		iter_lbl = JLabel("Fit Iterations=",JLabel.RIGHT)
		self.fit_iter_text.setValue(200)
		self.fit_iter_left_text = DoubleInputTextField(0.,FortranNumberFormat("G8.0"),8)
		iter_left_lbl = JLabel("Iters. Left=",JLabel.RIGHT)		
		self.avg_diff_text = DoubleInputTextField(0.,FortranNumberFormat("G8.6"),8) 
		avg_diff_lbl = JLabel("Avg.Diff.[mm]=",JLabel.RIGHT)
		tmp_panel.add(eKin_lbl)
		tmp_panel.add(self.eKin_text)
		tmp_panel.add(current_lbl)
		tmp_panel.add(self.current_text)
		tmp_panel.add(fit_err_lbl)
		tmp_panel.add(self.fit_err_text)		
		tmp0_lbl = JLabel("==========",JLabel.RIGHT)
		tmp1_lbl = JLabel("==========",JLabel.RIGHT)
		tmp_panel.add(tmp0_lbl)
		tmp_panel.add(tmp1_lbl)	
		tmp_panel.add(iter_lbl)
		tmp_panel.add(self.fit_iter_text)
		tmp_panel.add(iter_left_lbl)
		tmp_panel.add(self.fit_iter_left_text)		
		tmp_panel.add(avg_diff_lbl)
		tmp_panel.add(self.avg_diff_text)
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()		
		#------buttons panel ------------
		one_pass_button = JButton("Make One Pass")
		one_pass_button.addActionListener(One_Pass_Listener(self.linac_wizard_document))		
		fit_button = JButton("Start Fitting")
		fit_button.addActionListener(Twiss_Fitting_Listener(self.linac_wizard_document))	
		stop_fit_button = JButton("Stop Fitting")
		stop_fit_button.addActionListener(Stop_Twiss_Fitting_Listener(self.linac_wizard_document))			
		buttons_panel = JPanel(GridLayout(3,1))
		button0_panel = JPanel(FlowLayout(FlowLayout.CENTER,3,3))
		button0_panel.add(one_pass_button)
		button1_panel = JPanel(FlowLayout(FlowLayout.CENTER,3,3))
		button1_panel.add(fit_button)
		button2_panel = JPanel(FlowLayout(FlowLayout.CENTER,3,3))
		button2_panel.add(stop_fit_button)
		buttons_panel.add(button0_panel)
		buttons_panel.add(button1_panel)
		buttons_panel.add(button2_panel)
		#--------- Final Twiss parameters table -----
		self.final_twiss_params_holder = Twiss_Params_Holder()
		self.finalTwiss_table = JTable(Final_Twiss_Table_Model(self.final_twiss_params_holder))
		self.finalTwiss_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.finalTwiss_table.setFillsViewportHeight(true)
		self.finalTwiss_table.setPreferredScrollableViewportSize(Dimension(180,80))
		final_to_init_button = JButton("Copy Results To Initial Twiss")
		final_to_init_button.addActionListener(Copy_Twiss_Listener(self.transverse_twiss_fitting_controller))
		button2_panel = JPanel(FlowLayout(FlowLayout.CENTER,3,3))
		button2_panel.add(final_to_init_button)
		finalTwiss_panel = JPanel(BorderLayout())
		finalTwiss_panel.add(JScrollPane(self.finalTwiss_table), BorderLayout.CENTER)	
		finalTwiss_panel.add(button2_panel,BorderLayout.SOUTH)
		border = BorderFactory.createTitledBorder(etched_border,"Final Twiss Fitting Results")
		finalTwiss_panel.setBorder(border)		
		#---------make main panel --------------
		tmp1_panel = JPanel(BorderLayout())
		tmp1_panel.add(tmp_panel,BorderLayout.NORTH)
		tmp1_panel.add(buttons_panel,BorderLayout.SOUTH)
		tmp2_panel = JPanel(BorderLayout())
		tmp2_panel.add(tmp1_panel,BorderLayout.WEST)
		tmp2_panel.add(finalTwiss_panel,BorderLayout.CENTER)
		self.main_panel.add(tmp2_panel,BorderLayout.NORTH)

			
	def getMainPanel(self):
		return self.main_panel				


