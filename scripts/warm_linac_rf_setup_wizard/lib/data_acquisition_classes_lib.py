# The auxiliary classes for data acquisition

import sys
import math
import types
import time
import random

from java.lang import *
from javax.swing import *
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

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

from abstract_cavity_controller_lib import Abstract_Cavity_Controller
from constants_lib import GRAPH_LEGEND_KEY
from functions_and_classes_lib import calculateAvgErr, makePhaseNear, HarmonicsAnalyzer 
from functions_and_classes_lib import dumpGraphDataToDA, readGraphDataFromDA

#------------------------------------------------------------------------
#           Auxiliary classes for data 
#------------------------------------------------------------------------			
class BPM_Scan_Data:
	def __init__(self,main_loop_controller,cav_controller,bpm_wrapper):
		self.main_loop_controller = main_loop_controller
		self.cav_controller = cav_controller
		self.bpm_wrapper = bpm_wrapper
		self.cav_amp = 0.
		self.derivative = 0.
		self.zero_accel_phase = 0.
		self.max_accel_phase = 0.
		self.min_accel_phase = 0.
		#------ for MEBT measurements for Iteration process only
		self.cav_off_bpm_phase = 0.
		self.cav_off_bpm_phase_err = 0.
		self.cav_on_bpm_phase = 0.
		self.cav_on_bpm_phase_err = 0.
		self.cav_off_bpm_amp = 0.
		self.cav_off_bpm_amp_err = 0.
		self.cav_on_bpm_amp = 0.
		self.cav_on_bpm_amp_err = 0.
		#-----------------------------------------------------		
		self.harmonicsAnalyzer = HarmonicsAnalyzer(2)		
		self.phase_gd = BasicGraphData()
		self.phase_gd.setLineThick(3)
		self.phase_gd.setGraphPointSize(7)
		self.phase_gd.setGraphColor(Color.BLUE)
		self.phase_gd.setGraphProperty(GRAPH_LEGEND_KEY,self.bpm_wrapper.alias)
		self.phase_gd.setDrawLinesOn(true)
		self.phase_gd.setDrawPointsOn(true)
		#------------------------------
		self.amp_gd = BasicGraphData()
		self.amp_gd.setLineThick(3)
		self.amp_gd.setGraphPointSize(7)
		self.amp_gd.setGraphColor(Color.BLUE)
		self.amp_gd.setGraphProperty(GRAPH_LEGEND_KEY,self.bpm_wrapper.alias)
		self.amp_gd.setDrawLinesOn(true)
		self.amp_gd.setDrawPointsOn(true)		
		#------------------------------------
		self.phase_fit_gd = BasicGraphData()
		self.phase_fit_gd.setLineThick(3)
		self.phase_fit_gd.setGraphPointSize(3)
		self.phase_fit_gd.setGraphColor(Color.RED)
		self.phase_fit_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Fit "+self.bpm_wrapper.alias)
		self.phase_fit_gd.setDrawLinesOn(true)
		self.phase_fit_gd.setDrawPointsOn(false)		
		
	def clean(self):
		self.phase_gd.removeAllPoints()	
		self.amp_gd.removeAllPoints()	
		self.phase_fit_gd.removeAllPoints()
		self.derivative = 0.
		self.zero_accel_phase = 0.
		self.max_accel_phase = 0.
		self.min_accel_phase = 0.		
		#------ for MEBT measurements for Iteration process only
		self.cav_off_bpm_phase = 0.
		self.cav_off_bpm_phase_err = 0.
		self.cav_on_bpm_phase = 0.
		self.cav_on_bpm_phase_err = 0.
		self.cav_off_bpm_amp = 0.
		self.cav_off_bpm_amp_err = 0.
		self.cav_on_bpm_amp = 0.
		self.cav_on_bpm_amp_err = 0.		

	def addPoint(self,cav_phase):
		if(not self.bpm_wrapper.isOn): return
		bpm_amp = self.bpm_wrapper.bpm.getAmpAvg()		
		bpm_phase = self.bpm_wrapper.bpm.getPhaseAvg()
		self.addExternalPoint(cav_phase,bpm_amp,bpm_phase)
		
	def getAmpAndPhase(self):
		if(not self.bpm_wrapper.isOn): return (0.,0.)
		bpm_amp = self.bpm_wrapper.bpm.getAmpAvg()		
		bpm_phase = self.bpm_wrapper.bpm.getPhaseAvg()
		return (bpm_amp,bpm_phase)		

	def addExternalPoint(self,cav_phase,bpm_amp,bpm_phase):
		if(self.phase_gd.getNumbOfPoints() != 0):
			cav_phase_old = self.phase_gd.getX(self.phase_gd.getNumbOfPoints()-1)
			bpm_phase_old = self.phase_gd.getY(self.phase_gd.getNumbOfPoints()-1)
			cav_phase = makePhaseNear(cav_phase,cav_phase_old)
			bpm_phase = makePhaseNear(bpm_phase,bpm_phase_old)
		self.phase_gd.addPoint(cav_phase,bpm_phase)
		self.amp_gd.addPoint(cav_phase,bpm_amp)		
		
	def shiftToPhase(self,gd,bpm_phase_init):
		nP = gd.getNumbOfPoints()
		if(nP == 0): return
		x_arr = []
		y_arr = []
		err_arr = []
		for ip in range(nP):
			x_arr.append(gd.getX(ip))	
			y_arr.append(gd.getY(ip))	
			err_arr.append(gd.getErr(ip))
		gd.removeAllPoints()
		y_arr[0] = makePhaseNear(y_arr[0],bpm_phase_init)
		for ip in range(1,nP):
			y_arr[ip] = makePhaseNear(y_arr[ip],y_arr[ip-1])	
		gd.addPoint(x_arr,y_arr,err_arr)			
			
	def makeLinearFit(self):
		self.phase_fit_gd.removeAllPoints()	
		self.derivative = 0.
		if(self.phase_gd.getNumbOfPoints() > 1):
			GraphDataOperations.polynomialFit(self.phase_gd,self.phase_fit_gd,1)
			self.phase_fit_gd.setGraphColor(Color.RED)
			nP = self.phase_fit_gd.getNumbOfPoints()
			self.derivative = 0.
			if(nP > 1):
				X0 = self.phase_fit_gd.getX(0)
				X1 = self.phase_fit_gd.getX(nP-1)
				self.derivative = self.phase_fit_gd.getValueDerivativeY((X0+X1)/2.0)
			return true
		return false
				
	def makeHarmonicFit(self):
		self.phase_fit_gd.removeAllPoints()	
		if(self.phase_gd.getNumbOfPoints() < 8): return false
		err = self.harmonicsAnalyzer.analyzeData(self.phase_gd)	
		harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()		
		#-----remove bad points
		gd = self.phase_gd
		max_bad_points_count = 3
		bad_points_count = 0
		bad_index = 1
		while(bad_index >= 0):
			bad_index = -1
			for i in range(gd.getNumbOfPoints()):
				phase = gd.getX(i)
				y_appr = harm_function.getValue(phase)
				y = gd.getY(i)
				if(math.fabs(y-y_appr) > 3.0*err):
					bad_index = i
					bad_points_count += 1
					break
			if(bad_index >= 0):
				gd.removePoint(bad_index)
				self.amp_gd.removePoint(bad_index)
			# we should stop if we have too many bad points
			if(bad_points_count > max_bad_points_count):
				return false
		if(bad_points_count > 0):
			err = self.harmonicsAnalyzer.analyzeData(gd)
			harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		#----find a new cavity phase		
		min_phase = makePhaseNear(self.harmonicsAnalyzer.getPositionOfMin(),0.)
		max_phase = makePhaseNear(self.harmonicsAnalyzer.getPositionOfMax(),0.)
		# guess phase is -90 deg if max acceleratiom phase is 0.
		self.zero_accel_phase = makePhaseNear(min_phase - 90.,0.)
		self.max_accel_phase = min_phase
		self.min_accel_phase = max_phase 
		#print "debug min_phase=",min_phase
		#print "debug max_phase=",max_phase
		#print "debug zero_accel_phase =",self.zero_accel_phase
		#----make theory graph plot
		harm_function = self.harmonicsAnalyzer.getHrmonicsFunction()
		x_arr = []
		y_arr = []
		for i in range(73):
		 phase = -180.0 + 5.0*i
		 y = harm_function.getValue(phase)
		 x_arr.append(phase)
		 y_arr.append(y)
		self.phase_fit_gd.addPoint(x_arr,y_arr)
		#--------------------		
		return true
				
	def setCavAmplitudeParam(self,cav_amp):
		self.cav_amp = cav_amp
		self.phase_fit_gd.setGraphProperty(GRAPH_LEGEND_KEY,"Fit "+self.bpm_wrapper.alias+" CavAmp= %6.4f "%self.cav_amp)
		self.phase_gd.setGraphProperty(GRAPH_LEGEND_KEY,self.bpm_wrapper.alias+" CavAmp= %6.4f "%self.cav_amp)
		self.amp_gd.setGraphProperty(GRAPH_LEGEND_KEY,self.bpm_wrapper.alias+" CavAmp= %6.4f "%self.cav_amp)
		
	def checkLastDataPoint(self,min_bpm_amp):
		if(not self.bpm_wrapper.isOn): return false
		res = true
		nP = self.phase_gd.getNumbOfPoints()
		if(nP > 1):
			if(math.fabs(self.phase_gd.getY(nP-2) - self.phase_gd.getY(nP-1)) < 0.000000001):
				return false
		if(nP > 0):
			if(self.amp_gd.getY(nP-1) < min_bpm_amp):
				return false
		return res
				
	def removeLastPoint(self):
		if(not self.bpm_wrapper.isOn): return
		nP = self.phase_gd.getNumbOfPoints()
		if(nP < 1): return
		self.phase_gd.removePoint(nP-1)
		self.amp_gd.removePoint(nP-1)
		
	def getAvgPhaseAndErr(self):
		nP = self.phase_gd.getNumbOfPoints()
		if(nP < 1): return (0.,0.)
		phase_arr = []
		for ind in range(nP):
			phase_arr.append(self.phase_gd.getY(ind))
		(avg_phase,avg_phase_err) = calculateAvgErr(phase_arr)
		return (avg_phase,avg_phase_err)
	
	def writeDataToXML(self,root_da):
		bpm_scan_data_da = root_da.createChild("bpm_scan_data")
		bpm_scan_data_da.setValue("cav",self.cav_controller.cav_wrapper.alias)	
		bpm_scan_data_da.setValue("bpm",self.bpm_wrapper.alias)	
		bpm_scan_data_da.setValue("cav_amp",self.cav_amp)
		bpm_scan_data_da.setValue("derivative","%7.5f"%self.derivative)
		bpm_scan_data_da.setValue("zero_accel_phase","%7.3f"%self.zero_accel_phase)
		bpm_scan_data_da.setValue("max_accel_phase", "%7.3f"%self.max_accel_phase)
		if(self.cav_controller.cav_wrapper.alias.find("MEBT") >= 0):
			mebt_cav_off_on_da = bpm_scan_data_da.createChild("cav_off_on_data")
			mebt_cav_off_on_da.setValue("cav_off_bpm_phase",self.cav_off_bpm_phase)
			mebt_cav_off_on_da.setValue("cav_off_bpm_phase_err",self.cav_off_bpm_phase_err)
			mebt_cav_off_on_da.setValue("cav_on_bpm_phase",self.cav_on_bpm_phase)
			mebt_cav_off_on_da.setValue("cav_on_bpm_phase_err",self.cav_on_bpm_phase_err)
			mebt_cav_off_on_da.setValue("cav_off_bpm_amp",self.cav_off_bpm_amp)
			mebt_cav_off_on_da.setValue("cav_off_bpm_amp_err",self.cav_off_bpm_amp_err)
			mebt_cav_off_on_da.setValue("cav_on_bpm_amp",self.cav_on_bpm_amp)
			mebt_cav_off_on_da.setValue("cav_on_bpm_amp_err",self.cav_on_bpm_amp_err)
		dumpGraphDataToDA(self.phase_gd,bpm_scan_data_da,"phase_scan_gd")
		dumpGraphDataToDA(self.amp_gd,bpm_scan_data_da,"amp_scan_gd")
		dumpGraphDataToDA(self.phase_fit_gd,bpm_scan_data_da,"phase_scan_fit_gd")
		
	def readDataFromXML(self,bpm_scan_data_da):
		self.cav_amp = bpm_scan_data_da.doubleValue("cav_amp")
		self.derivative = bpm_scan_data_da.doubleValue("derivative")
		self.zero_accel_phase = bpm_scan_data_da.doubleValue("zero_accel_phase")
		self.max_accel_phase = bpm_scan_data_da.doubleValue("max_accel_phase")
		mebt_cav_off_on_da = bpm_scan_data_da.childAdaptor("cav_off_on_data")
		if(mebt_cav_off_on_da != null):
			self.cav_off_bpm_phase	= mebt_cav_off_on_da.doubleValue("cav_off_bpm_phase")
			self.cav_off_bpm_phase_err	= mebt_cav_off_on_da.doubleValue("cav_off_bpm_phase_err")
			self.cav_on_bpm_phase	= mebt_cav_off_on_da.doubleValue("cav_on_bpm_phase")
			self.cav_on_bpm_phase_err	= mebt_cav_off_on_da.doubleValue("cav_on_bpm_phase_err")
			self.cav_off_bpm_amp	= mebt_cav_off_on_da.doubleValue("cav_off_bpm_amp")
			self.cav_off_bpm_amp_err	= mebt_cav_off_on_da.doubleValue("cav_off_bpm_amp_err")
			self.cav_on_bpm_amp	= mebt_cav_off_on_da.doubleValue("cav_on_bpm_amp")
			self.cav_on_bpm_amp_err	= mebt_cav_off_on_da.doubleValue("cav_on_bpm_amp_err")
		readGraphDataFromDA(self.phase_gd,bpm_scan_data_da,"phase_scan_gd")
		readGraphDataFromDA(self.amp_gd,bpm_scan_data_da,"amp_scan_gd")
		readGraphDataFromDA(self.phase_fit_gd,bpm_scan_data_da,"phase_scan_fit_gd")
		
class BPM_Wrapper:
	def __init__(self,bpm,pos):
		self.bpm = bpm	
		self.pos = pos
		self.isOn = true
		self.alias = bpm.getId().replace("_Diag","")
		
	def writeDataToXML(self,bpms_da):
		bpm_da = bpms_da.createChild(self.alias)
		bpm_da.setValue("isOn",self.isOn)
			
	def readDataFromXML(self,bpms_da):		
		bpm_da = bpms_da.childAdaptor(self.alias)
		if(bpm_da != null):
			self.isOn = false
			if(bpm_da.intValue("isOn") == 1):
				self.isOn = true

		
		
