# The DTL acceptance scan data classes. They are used to keep scan and 
# analysis information.

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from java.util import *
from javax.swing import JTable
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
from java.awt import Color

from xal.extension.widgets.plot import BasicGraphData, GraphDataOperations

from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import ScientificNumberFormat

#----------------------------------------
# Calsses of OpenXAL Solver Package 
#----------------------------------------
from xal.extension.solver import Scorer
from xal.extension.solver import Trial
from xal.extension.solver import Variable
from xal.extension.solver import Stopper
from xal.extension.solver import SolveStopperFactory
from xal.extension.solver import ProblemFactory
from xal.extension.solver import Solver
from xal.extension.solver import Problem
from xal.extension.solver.algorithm import SimplexSearchAlgorithm
from xal.extension.solver.algorithm import RandomShrinkSearch
from xal.extension.solver.hint import Hint
from xal.extension.solver.hint import InitialDelta

from constants_lib import GRAPH_LEGEND_KEY
from functions_and_classes_lib import calculateAvgErr, makePhaseNear 
from functions_and_classes_lib import dumpGraphDataToDA, readGraphDataFromDA

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           DTL Acceptance Scan Data
#------------------------------------------------------------------------
class DTL_Acc_Scan_Data:
	"""
	The Scan Data includes the scan data itself and another information about
	the scan and data analysis
	"""
	def __init__(self,dtl_acc_scan_cavity_controller):
		#---- dtl_acceptance_scans_controller - main controller for acceptance scans
		#---- it keeps referencies to controllers for each cavity - dtl_acc_scan_cavity_controller
		self.dtl_acc_scan_cavity_controller = dtl_acc_scan_cavity_controller		
		self.dtl_acceptance_scans_controller = self.dtl_acc_scan_cavity_controller.dtl_acceptance_scans_controller
		#---- This bool variable defines if it is scan for real production state
		self.is_production_scan = false
		#---- This is variable to show this data on patterns plot
		self.show_on_patterns_plot = false
		#---- the production phase. This value was found by another method and was used in production 
		self.cav_prod_phase_text = DoubleInputTextField(0.,DecimalFormat("####.#"),6)
		#---- cavity amplitude during the scan
		self.cav_amp_text = DoubleInputTextField(0.,DecimalFormat("#.####"),6)
		#---- Fit parameters
		self.max_value_text = DoubleInputTextField(0.,ScientificNumberFormat(),10)
		self.min_value_text = DoubleInputTextField(0.,ScientificNumberFormat(),10)
		self.half_height_pos_text = DoubleInputTextField(0.,DecimalFormat("####.#"),6)
		#---- phase shift of cav. phase from the front of a trapezoidal-like curve
		self.fit_phase_shift_text = DoubleInputTextField(0.,DecimalFormat("####.#"),6)
		#---- phase width
		self.fit_phase_width_text = DoubleInputTextField(0.,DecimalFormat("####.#"),6)
		#---------------------------------------------------
		self.scan_gd = BasicGraphData()
		self.scan_gd.setLineThick(2)
		self.scan_gd.setGraphPointSize(7)
		self.scan_gd.setGraphColor(Color.BLACK)
		self.scan_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Cavity Amp= %5.4f"%(0.))
		self.scan_gd.setDrawLinesOn(true)
		self.scan_gd.setDrawPointsOn(true)
		#---------------------------------------------------
		self.scan_half_height_gd = BasicGraphData()
		self.scan_half_height_gd.setLineThick(3)
		self.scan_half_height_gd.setGraphColor(Color.BLUE)
		self.scan_half_height_gd.setGraphProperty(GRAPH_LEGEND_KEY,"0.5*Max Cavity Amp= %5.4f"%(0.))
		self.scan_half_height_gd.setDrawLinesOn(true)
		self.scan_half_height_gd.setDrawPointsOn(false)
		#---------------------------------------------------
		
	def getCopy(self):
		acc_scan_data = DTL_Acc_Scan_Data(self.dtl_acc_scan_cavity_controller)
		acc_scan_data.is_production_scan = self.is_production_scan
		acc_scan_data.show_on_patterns_plot = self.show_on_patterns_plot
		acc_scan_data.cav_prod_phase_text.setValue(self.cav_prod_phase_text.getValue())
		acc_scan_data.cav_amp_text.setValue(self.cav_amp_text.getValue())
		acc_scan_data.max_value_text.setValue(self.max_value_text.getValue())
		acc_scan_data.min_value_text.setValue(self.min_value_text.getValue())
		acc_scan_data.half_height_pos_text.setValue(self.half_height_pos_text.getValue())
		acc_scan_data.fit_phase_width_text.setValue(self.fit_phase_width_text.getValue())
		acc_scan_data.fit_phase_shift_text.setValue(self.fit_phase_shift_text.getValue())
		for ind in range(self.scan_gd.getNumbOfPoints()):
			x = self.scan_gd.getX(ind)
			y = self.scan_gd.getY(ind)
			acc_scan_data.scan_gd.addPoint(x,y)
		for ind in range(self.scan_half_height_gd.getNumbOfPoints()):
			x = self.scan_half_height_gd.getX(ind)
			y = self.scan_half_height_gd.getY(ind)
			acc_scan_data.scan_half_height_gd.addPoint(x,y)
		acc_scan_data.updateGraphProperties()
		return acc_scan_data

	def clearData(self):
		self.clearAnalysis()
		self.scan_gd.removeAllPoints()
		
	def clearAnalysis(self):
		self.max_value_text.setValue(0.)
		self.min_value_text.setValue(0.)
		self.half_height_pos_text.setValue(0.)
		self.fit_phase_shift_text.setValue(0.)
		self.fit_phase_width_text.setValue(0.)
		self.scan_half_height_gd.removeAllPoints()
		
	def addScanPoint(self,phase,fc_qt):
		self.scan_gd.addPoint(phase,fc_qt)
		
	def updateGraphProperties(self):
		amp = self.cav_amp_text.getValue()
		self.scan_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Cavity Amp= %5.4f"%(amp))
		self.scan_half_height_gd.setGraphProperty(GRAPH_LEGEND_KEY,"0.5*Max Cavity Amp= %5.4f"%(amp))
		
	def isItProductionScan(self):
		#---- This bool variable defines if it is scan for real production state
		return self.is_production_scan
		
	def setIfItIsProductionScan(self,is_production_scan):
		#---- This bool variable defines if it is scan for real production state
		self.is_production_scan = is_production_scan
		
	def getCavPhaseShiftFromFit(self):
		return self.fit_phase_shift_text.getValue()

	def getFitWidth(self):
		return self.fit_phase_width_text.getValue()
		
	def getLeftHalfHeightPosition(self):
		return self.half_height_pos_text.getValue()
		
	def getScanGraphData(self):
		return self.scan_gd
		
	def getHalfHeightGraphData(self):
		return self.scan_half_height_gd
		
	def performAnalysis(self):
		y_min = +1.0e+36
		y_max = -1.0e+36
		for ind in range(self.scan_gd.getNumbOfPoints()):
			y = self.scan_gd.getY(ind)
			if(y < y_min): y_min = y
			if(y > y_max): y_max = y
		y_avg = (y_max + y_min)/2
		x_up = 0.
		x_down = 0.
		n_points_up = 0
		n_points_down = 0
		for ind in range(self.scan_gd.getNumbOfPoints()-1):
			x0 = self.scan_gd.getX(ind)
			x1 = self.scan_gd.getX(ind+1)
			y0 = self.scan_gd.getY(ind)
			y1 = self.scan_gd.getY(ind+1)
			if( (y_avg-y0) >= 0. and (y1-y_avg) >= 0.):
				n_points_up += 1
				x_up = x0 + (x1-x0)*(y_avg-y0)/(y1-y0)
			if( (y_avg-y0) <= 0. and (y1-y_avg) <= 0.):
				n_points_down += 1
				x_down = x0 + (x1-x0)*(y_avg-y0)/(y1-y0)
		width = x_down - x_up
		if(x_down < x_up):
			width = abs(180.-x_up) + abs(-180.-x_down)
		phase_shift = makePhaseNear(self.cav_prod_phase_text.getValue() - x_up,0.)
		#print "debug wdth=",width," x_up=",x_up," phase_shift=",phase_shift," y_min=",y_min," y_max=",y_max
		self.half_height_pos_text.setValue(x_up)
		self.fit_phase_width_text.setValue(width)
		self.fit_phase_shift_text.setValue(phase_shift)
		self.min_value_text.setValue(y_min)
		self.max_value_text.setValue(y_max)
		self.scan_half_height_gd.removeAllPoints()
		self.scan_half_height_gd.addPoint(-180.,y_avg)
		self.scan_half_height_gd.addPoint(+180.,y_avg)
		if(n_points_up > 1 or n_points_down > 1): 
			return false
		return true
		
	def shiftScanData(self,shit_delta):
		x_arr = []
		y_arr = []
		for ind in range(self.scan_gd.getNumbOfPoints()):
			x_arr.append(self.scan_gd.getX(ind))
			y_arr.append(self.scan_gd.getY(ind))
		for ind in range(len(x_arr)):
			x_arr[ind] = makePhaseNear(x_arr[ind]+shit_delta,0.)
		self.scan_gd.removeAllPoints()
		self.scan_gd.addPoint(x_arr,y_arr)
		new_prod_phase = makePhaseNear(self.cav_prod_phase_text.getValue() + shit_delta,0.)
		self.cav_prod_phase_text.setValue(new_prod_phase)
		self.performAnalysis()
		
	def writeDataToXML(self,root_da):
		scan_data_da = root_da.createChild("dtl_acceptance_scan_data")
		scan_data_da.setValue("cav",self.dtl_acc_scan_cavity_controller.cav_wrapper.alias)
		scan_data_da.setValue("is_production_scan",self.is_production_scan)
		scan_data_da.setValue("phase",self.cav_prod_phase_text.getValue())
		scan_data_da.setValue("amp",self.cav_amp_text.getValue())
		scan_data_da.setValue("max_val",self.max_value_text.getValue())
		scan_data_da.setValue("min_val",self.min_value_text.getValue())
		scan_data_da.setValue("half_height_pos",self.half_height_pos_text.getValue())
		scan_data_da.setValue("phase_shift",self.fit_phase_shift_text.getValue())
		scan_data_da.setValue("phase_width",self.fit_phase_width_text.getValue())
		dumpGraphDataToDA(self.scan_gd,scan_data_da,"acc_scan_gd")
		dumpGraphDataToDA(self.scan_half_height_gd,scan_data_da,"acc_scan_fit_gd")
		
	def readDataFromXML(self,scan_data_da):
		self.is_production_scan = false
		is_production = scan_data_da.intValue("is_production_scan")
		if(is_production == 1): self.is_production_scan = true
		self.cav_prod_phase_text.setValue(scan_data_da.doubleValue("phase"))
		self.cav_amp_text.setValue(scan_data_da.doubleValue("amp"))
		self.max_value_text.setValue(scan_data_da.doubleValue("max_val"))
		self.min_value_text.setValue(scan_data_da.doubleValue("min_val"))
		self.half_height_pos_text.setValue(scan_data_da.doubleValue("half_height_pos"))
		self.fit_phase_shift_text.setValue(scan_data_da.doubleValue("phase_shift"))
		self.fit_phase_width_text.setValue(scan_data_da.doubleValue("phase_width"))
		readGraphDataFromDA(self.scan_gd,scan_data_da,"acc_scan_gd")
		readGraphDataFromDA(self.scan_half_height_gd,scan_data_da,"acc_scan_fit_gd")
