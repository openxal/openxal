# The WS and LW acquisition classes

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
from java.util import Date
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.swing import DoubleInputTextField 
from xal.tools.text import FortranNumberFormat
from xal.smf.impl import Marker, ProfileMonitor, Quadrupole, RfGap
from xal.extension.wirescan.apputils import GaussFitter, WireScanData
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier

from xal.ca import *

from constants_lib import GRAPH_LEGEND_KEY

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

WS_DIRECTION_NULL = 0
WS_DIRECTION_HOR = 1
WS_DIRECTION_VER = 2

class WS_Scan_Record:
	"""
	This class keeps the results of the scan in one directions for one LW or WS
	"""
	def __init__(self, ws_node, ws_direction = WS_DIRECTION_NULL):
		self.ws_node = ws_node
		self.gauss_sigma = 0.
		self.gd_wf = BasicGraphData()
		self.gd_log_wf = BasicGraphData()
		self.gd_wf.setDrawLinesOn(false)
		self.gd_log_wf.setDrawLinesOn(false)
		self.gd_wf.setGraphPointSize(5)
		self.gd_log_wf.setGraphPointSize(5)
		self.gd_wf.setGraphColor(Color.BLUE)
		self.gd_log_wf.setGraphColor(Color.BLUE)
		self.setDirection(ws_direction)
		self.left_limit = 0.
		self.right_limit = 0.
		
	def setWF(self,wf_x_arr,wf_y_arr):
		#set waive form
		self.gd_wf.removeAllPoints()
		self.gd_log_wf.removeAllPoints()
		if(len(wf_x_arr) == len(wf_y_arr) and len(wf_y_arr) > 0):
			self.left_limit = wf_x_arr[0]
			self.right_limit = wf_x_arr[len(wf_x_arr)-1]
			y_max = 0.
			for y in wf_y_arr:
				if(math.fabs(y_max) < math.fabs(y)):
					y_max = y
			if(y_max < 0):
				for i in range(len(wf_y_arr)):
					wf_y_arr[i] = - wf_y_arr[i]
				y_max = - y_max
			self.gd_wf.addPoint(wf_x_arr,wf_y_arr)
			for i in range(len(wf_y_arr)):
				if(wf_y_arr[i] > 0.):
					self.gd_log_wf.addPoint(wf_x_arr[i],math.log(wf_y_arr[i]))
					
	def setDirection(	self,ws_direction):
		self.ws_direction = ws_direction
		legendKey = GRAPH_LEGEND_KEY
		legendName = self.ws_node.getId() 
		if(self.ws_direction == WS_DIRECTION_HOR): legendName += " Hor. "
		if(self.ws_direction == WS_DIRECTION_VER): legendName += " Ver. "
		self.gd_wf.setGraphProperty(legendKey,legendName+" Data")
		self.gd_log_wf.setGraphProperty(legendKey,"Log "+legendName+" Data ")	
						
	def getName(self):
		return self.ws_node.getId()
		
	def getAccNode(self):
		return self.ws_node
		
	def getCopy(self):
		""" 
		This method will return the partial copy of the record
		"""
		cp_record = WS_Scan_Record(self.ws_node,self.ws_direction)
		cp_record.left_limit  = self.left_limit
		cp_record.right_limit = self.right_limit
		cp_record.gauss_sigma = self.gauss_sigma
		#copy Graph data
		for i in range(self.gd_wf.getNumbOfPoints()):
			cp_record.gd_wf.addPoint(self.gd_wf.getX(i),self.gd_wf.getY(i))
			cp_record.gd_log_wf.addPoint(self.gd_log_wf.getX(i),self.gd_log_wf.getY(i))
		return cp_record
		
class WS_Scan_and_Fit_Record:
	def __init__(self,ws_scan_Record):
		self.isOn = true
		self.index = -1
		self.pos = 0.
		self.fit_is_good = false 
		self.gauss_sigma = ws_scan_Record.gauss_sigma
		self.ws_node = ws_scan_Record.ws_node
		self.ws_direction = ws_scan_Record.ws_direction
		self.custom_gauss_sigma = 0.
		self.custom_rms_sigma = 0.
		self.gd_wf = BasicGraphData()
		self.gd_fit_wf = BasicGraphData()
		self.gd_log_wf = BasicGraphData()
		self.gd_log_fit_wf = BasicGraphData()
		self.gd_wf.setDrawLinesOn(false)
		self.gd_log_wf.setDrawLinesOn(false)
		self.gd_fit_wf.setDrawPointsOn(false)
		self.gd_log_fit_wf.setDrawPointsOn(false)
		self.gd_wf.setGraphPointSize(5)
		self.gd_log_wf.setGraphPointSize(5)
		self.gd_fit_wf.setLineThick(3)
		self.gd_log_fit_wf.setLineThick(3)
		self.gd_wf.setGraphColor(Color.BLUE)
		self.gd_log_wf.setGraphColor(Color.BLUE)
		self.gd_fit_wf.setGraphColor(Color.RED)
		self.gd_log_fit_wf.setGraphColor(Color.RED)
		legendKey = GRAPH_LEGEND_KEY
		legendName = self.ws_node.getId() 
		if(self.ws_direction == WS_DIRECTION_HOR): legendName += " Hor. "
		if(self.ws_direction == WS_DIRECTION_VER): legendName += " Ver. "
		self.gd_wf.setGraphProperty(legendKey,legendName+" Data")
		self.gd_log_wf.setGraphProperty(legendKey,"Log "+legendName+" Data ")
		self.gd_fit_wf.setGraphProperty(legendKey,"Log "+legendName+" Fit ")
		self.gd_log_fit_wf.setGraphProperty(legendKey,"Log "+legendName+" Fit ")
		#----------- copy Graph data -------------
		for i in range(ws_scan_Record.gd_wf.getNumbOfPoints()):
			self.gd_wf.addPoint(ws_scan_Record.gd_wf.getX(i),ws_scan_Record.gd_wf.getY(i))
		for i in range(ws_scan_Record.gd_log_wf.getNumbOfPoints()):
			self.gd_log_wf.addPoint(ws_scan_Record.gd_log_wf.getX(i),ws_scan_Record.gd_log_wf.getY(i))	
		self.n_fit_points = 150
		self.quad_dict = {}
		self.cav_amp_phase_dict = {}
		self.param_dict = [self.quad_dict,self.cav_amp_phase_dict]
		#-----Gauss Fitting params----------------
		self.CONST = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		self.A0 = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		self.X_CENTER = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		self.SIGMA = DoubleInputTextField(self.gauss_sigma,FortranNumberFormat("G8.4"),10)
		self.X_MIN = DoubleInputTextField(ws_scan_Record.left_limit,FortranNumberFormat("G8.4"),10)
		self.X_MAX = DoubleInputTextField(ws_scan_Record.right_limit,FortranNumberFormat("G8.4"),10)
				
	def plotFitData(self):
		x_min = self.X_MIN.getValue()
		x_max = self.X_MAX.getValue()
		step = (x_max - x_min)/(self.n_fit_points - 1)
		base = self.CONST.getValue()
		a0 = self.A0.getValue()
		x0 = self.X_CENTER.getValue()
		sigma2 = (self.SIGMA.getValue())**2
		self.gd_fit_wf.removeAllPoints()
		self.gd_log_fit_wf.removeAllPoints()
		for i in range(self.n_fit_points):
			x = x_min + i*step
			y = base + a0*math.exp(-(x-x0)**2/(2*sigma2))
			self.gd_fit_wf.addPoint(x,y)
			if(y > 0.):
				self.gd_log_fit_wf.addPoint(x,math.log(y))
				
	def setParamDict(self,param_dict):
		self.param_dict = param_dict
		[self.quad_dict,self.cav_amp_phase_dict] = self.param_dict
				
	def getName(self):
		return self.ws_node.getId()
		
	def getAccNode(self):
		return self.ws_node
		
	def updateGaussParams(self):
		self.custom_gauss_sigma = self.SIGMA.getValue()
		
class WS_Table_Element:
	def __init__(self,ws_node):
		self.ws = ws_node
		self.ws_scan_record_hor = WS_Scan_Record(ws_node,WS_DIRECTION_HOR)
		self.ws_scan_record_ver = WS_Scan_Record(ws_node,WS_DIRECTION_VER)
		self.pv_pos_wf_hor = null
		self.pv_wf_hor = null
		self.pv_pos_wf_ver = null
		self.pv_wf_ver = null
		self.pv_sigma_hor = null
		self.pv_sigma_ver = null
		self.isOn = false
		
	def setPV_Names(self,pv_pos_wf_name,pv_wf_name,sigma_name,ws_direction):
		if(ws_direction == WS_DIRECTION_HOR):
			self.pv_pos_wf_hor = ChannelFactory.defaultFactory().getChannel(pv_pos_wf_name)
			self.pv_wf_hor = ChannelFactory.defaultFactory().getChannel(pv_wf_name)
			self.pv_sigma_hor = ChannelFactory.defaultFactory().getChannel(sigma_name)
		if(ws_direction == WS_DIRECTION_VER):
			self.pv_pos_wf_ver = ChannelFactory.defaultFactory().getChannel(pv_pos_wf_name)
			self.pv_wf_ver = ChannelFactory.defaultFactory().getChannel(pv_wf_name)
			self.pv_sigma_ver = ChannelFactory.defaultFactory().getChannel(sigma_name)
		
	def readPVData(self):
		wf_hor_x_arr = self.pv_pos_wf_hor.getArrDbl()
		wf_ver_x_arr = self.pv_pos_wf_ver.getArrDbl()
		wf_hor_y_arr = self.pv_wf_hor.getArrDbl()
		wf_ver_y_arr = self.pv_wf_ver.getArrDbl()
		sigma_hor = self.pv_sigma_hor.getValDbl()
		sigma_ver = self.pv_sigma_ver.getValDbl()
		(wf_hor_x_arr,wf_hor_y_arr) = self.cleanUpArr(wf_hor_x_arr,wf_hor_y_arr)
		(wf_ver_x_arr,wf_ver_y_arr) = self.cleanUpArr(wf_ver_x_arr,wf_ver_y_arr)
		#print "debug wf_hor_x_arr=",wf_hor_x_arr
		#print "debug wf_hor_y_arr=",wf_hor_y_arr
		#-------hor---------------
		self.ws_scan_record_hor.setWF(wf_hor_x_arr,wf_hor_y_arr)
		self.ws_scan_record_hor.gauss_sigma = sigma_hor
		#-------ver---------------
		self.ws_scan_record_ver.setWF(wf_ver_x_arr,wf_ver_y_arr)
		self.ws_scan_record_ver.gauss_sigma = sigma_ver	
		#print "debug readPVData ====END===="
		
	def cleanUpArr(self,x_arr,y_arr):
		if(math.fabs(y_arr[len(y_arr)-1]) > 0.): 
			y_arr = y_arr[1:]
			x_arr = x_arr[1:]
			return (x_arr,y_arr)
		i_max = -1
		for i in range(len(y_arr)-2,0,-1):
			if(math.fabs(y_arr[i]) > 0.):
				i_max = i
				break
		y_arr = y_arr[1:(i_max+1)] 
		x_arr = x_arr[1:(i_max+1)]
		return (x_arr,y_arr)
		
	def getRecords(self,ws_direction):
		if(ws_direction == WS_DIRECTION_HOR):
			return self.ws_scan_record_hor
		if(ws_direction == WS_DIRECTION_VER):
			return self.ws_scan_record_ver
			
	def getName(self):
		return self.ws.getId()
			
	def getAccNode(self):
		return self.ws		

class Local_Gauss_Fitter:
	def __init__(self):
		self.gauss_fitter = GaussFitter()
		self.wireScanData = WireScanData()
		self.gauss_fitter.setIterations(200)
		self.gauss_fitter.setWidthCoeff(1.0e+10)
		self.gd_wf = self.wireScanData.getRawWFX()
		self.rc = null
		
	def setRecord(self,ws_scan_and_fit_Record):
		self.rc = ws_scan_and_fit_Record
			
	def updateFitData(self):
		x_min = self.rc.X_MIN.getValue()
		x_max = self.rc.X_MAX.getValue()
		self.gd_wf.removeAllPoints()
		for i in range(self.rc.gd_wf.getNumbOfPoints()):
			x = self.rc.gd_wf.getX(i)
			if(x >= x_min and x <= x_max):
				self.gd_wf.addPoint(x,self.rc.gd_wf.getY(i))	
				#print "debug update fit data x=",x," y=",self.rc.gd_wf.getY(i)

	def guessAndFit(self):
		self.updateFitData()
		x_min = self.rc.X_MIN.getValue()
		x_max = self.rc.X_MAX.getValue()		
		result = self.gauss_fitter.guessAndFitX(self.wireScanData)
		if(result):
			sigma = self.wireScanData.getSigmaX()
			x_center = self.wireScanData.getCenterX()
			self.rc.X_MIN.setValue(x_center - 3.0*sigma)
			self.rc.X_MAX.setValue(x_center + 3.0*sigma)
			self.updateParams()
			self.updateFitData()
			result = self.gauss_fitter.fitAgainX(self.wireScanData)
			if(result):
				self.updateParams()
				self.rc.fit_is_good = true
				return self.rc.fit_is_good
		self.updateParamsToZero()
		self.rc.fit_is_good = false
		self.rc.X_MIN.setValue(x_min)
		self.rc.X_MAX.setValue(x_max)
		return self.rc.fit_is_good
			
	def fit(self):
		self.updateFitData()
		self.wireScanData.setBaseX(self.rc.CONST.getValue())
		self.wireScanData.setAmpX(self.rc.A0.getValue())
		self.wireScanData.setCenterX(self.rc.X_CENTER.getValue())
		self.wireScanData.setSigmaX(self.rc.SIGMA.getValue())
		#print "debug before fit sigma=",self.wireScanData.getSigmaX()," center=",self.rc.X_CENTER.getValue()
		result = self.gauss_fitter.fitAgainX(self.wireScanData)
		#print "debug after fit sigma RMS=",self.wireScanData.getSigmaRmsX()," center=",self.wireScanData.getCenterX()
		#print "debug res=",result
		if(result):
			self.updateParams()
			self.rc.fit_is_good = true
		else:
			self.updateParamsToZero()
			self.rc.fit_is_good = false	
		return self.rc.fit_is_good		
			
	def _calculateSigmaRMS(self):
		""" 
		This method is needed because the RMS calculations in Gauss Fitter XAL class assume
		zero noise-base. We want to substract it.
		"""
		x_min = self.rc.X_MIN.getValue()
		x_max = self.rc.X_MAX.getValue()		
		y_min = 1.0e+36
		for i in range(self.rc.gd_wf.getNumbOfPoints()):
			x = self.rc.gd_wf.getX(i)
			if(x >= x_min and x <= x_max):
				y = self.rc.gd_wf.getY(i)
				if(y_min > y): y_min = y
		y_min = math.fabs(y_min)
		centerRms = 0.
		weight = 0.
		for i in range(self.rc.gd_wf.getNumbOfPoints()):
			x = self.rc.gd_wf.getX(i)
			if(x >= x_min and x <= x_max):
				y = self.rc.gd_wf.getY(i)
				weight += (y-y_min)
				centerRms += x*(y-y_min)
		if(weight == 0.): return 0.
		centerRms = centerRms/weight
		sigmaRms = 0.
		for i in range(self.rc.gd_wf.getNumbOfPoints()):
			x = self.rc.gd_wf.getX(i)
			if(x >= x_min and x <= x_max):
				y = self.rc.gd_wf.getY(i)			
				sigmaRms += (x - centerRms)*(x - centerRms)*(y-y_min)
		sigmaRms = math.sqrt(sigmaRms/weight)
		return sigmaRms 
				
	def updateParams(self):
		self.rc.CONST.setValueQuietly(self.wireScanData.getBaseX())
		self.rc.A0.setValueQuietly(self.wireScanData.getAmpX())
		self.rc.X_CENTER.setValueQuietly(self.wireScanData.getCenterX())
		self.rc.SIGMA.setValueQuietly(self.wireScanData.getSigmaX())
		self.rc.custom_gauss_sigma =  self.wireScanData.getSigmaX()
		#self.rc.custom_rms_sigma = self.wireScanData.	getSigmaRmsX()
		self.rc.custom_rms_sigma = self._calculateSigmaRMS()
		print "debug gauss fit sigma RMS=",self.wireScanData.getSigmaRmsX()," new RMS=",self.rc.custom_rms_sigma
		self.rc.plotFitData()
		
	def updateParamsToZero(self):
		self.rc.custom_gauss_sigma = 0.
		self.rc.custom_rms_sigma = 0.
		self.rc.CONST.setValueQuietly(0.)
		self.rc.A0.setValueQuietly(0.)
		self.rc.X_CENTER.setValueQuietly(0.)
		self.rc.SIGMA.setValueQuietly(0.)
		self.rc.gd_fit_wf.removeAllPoints()
		self.rc.gd_log_fit_wf.removeAllPoints()

#------------------------------------------------
#  WS (or LW) JTable model
#------------------------------------------------
class WS_Table_Model(AbstractTableModel):
	def __init__(self):
		self.ws_table_element_arr = []
		self.columnNames = ["Name","Use","Fit H, mm","Fit V, mm"]
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(2)
		self.boolean_class = Boolean(true).getClass()
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.ws_table_element_arr)
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0): return self.ws_table_element_arr[row].getName()
		if(col == 1): return Boolean(self.ws_table_element_arr[row].isOn)
		if(col == 2): return self.nf.format(self.ws_table_element_arr[row].getRecords(WS_DIRECTION_HOR).gauss_sigma)
		if(col == 3): return self.nf.format(self.ws_table_element_arr[row].getRecords(WS_DIRECTION_VER).gauss_sigma)
			
	def getColumnClass(self,col):
		if(col != 1):
			return self.string_class
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 1):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 1):
			self.ws_table_element_arr[row].isOn = value
			self.fireTableCellUpdated(row, col)
			
	def addWS_TableElement(self,ws_table_element):
		self.ws_table_element_arr.append(ws_table_element)
		
	def getWS_TableElements(self):
		return self.ws_table_element_arr
			
	def readPVData(self):
		for ws_table_element in self.ws_table_element_arr:
			ws_table_element.readPVData()
			
#-----------------------------------------------------------------
#  WS (or LW) Records JTable model for Horizontal or Vertical data
#-----------------------------------------------------------------		
class WS_Records_Table_Model(AbstractTableModel):
	def __init__(self):
		self.ws_rec_table_element_arr = []
		self.columnNames = ["#","Name","Use","Dir","Sigma, mm","User Fit","User RMS"]
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(2)
		self.boolean_class = Boolean(true).getClass()
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.ws_rec_table_element_arr)
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0): return self.nf.format(row+1)
		if(col == 1): return self.ws_rec_table_element_arr[row].getName()
		if(col == 2): return Boolean(self.ws_rec_table_element_arr[row].isOn)
		if(col == 3): 
			if(self.ws_rec_table_element_arr[row].ws_direction == WS_DIRECTION_HOR):
				return "Hor"
			if(self.ws_rec_table_element_arr[row].ws_direction == WS_DIRECTION_VER):
				return "Ver"
		if(col == 4): return self.nf.format(self.ws_rec_table_element_arr[row].gauss_sigma)
		if(col == 5): return self.nf.format(self.ws_rec_table_element_arr[row].custom_gauss_sigma)
		if(col == 6): return self.nf.format(self.ws_rec_table_element_arr[row].custom_rms_sigma)
			
	def getColumnClass(self,col):
		if(col != 2):
			return self.string_class
		return self.boolean_class
	
	def isCellEditable(self,row,col):
		if(col == 2 or col == 4 or col == 5 or col == 6 ):
			return true
		return false
			
	def setValueAt(self, value, row, col):
		if(col == 2):
			self.ws_rec_table_element_arr[row].isOn = value
			self.fireTableCellUpdated(row, col)
		if(col == 4):
			self.ws_rec_table_element_arr[row].gauss_sigma = Double.parseDouble(value)
		if(col == 5):
			self.ws_rec_table_element_arr[row].custom_gauss_sigma = Double.parseDouble(value)
		if(col == 6):
			self.ws_rec_table_element_arr[row].custom_rms_sigma = Double.parseDouble(value)
			
	def addWS_RecordElement(self,ws_table_element):
		self.ws_rec_table_element_arr.append(ws_table_element)
		
	def getWS_RecordElements(self):
		return self.ws_rec_table_element_arr

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class WS_Table_Listener(TableModelListener):
	""" For now this class is not used here """
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		
	def tableChanged(self,tableModelEvent):
		row = tableModelEvent.getFirstRow()
		column = tableModelEvent.getColumn()
		tableModel = tableModelEvent.getSource()
		self.ws_lw_acquisition_controller.setDataToGraphPanels()

class WS_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		
	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		self.ws_lw_acquisition_controller.setDataToGraphPanels(index)

class WS_GET_Data_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		
	def actionPerformed(self,actionEvent):
		ws_table_elements_arr = self.ws_lw_acquisition_controller.ws_table_model.getWS_TableElements()
		for ws_table_element in ws_table_elements_arr:	
			ws_table_element.readPVData()		
		self.ws_lw_acquisition_controller.ws_table_model.fireTableDataChanged()
		
class Switch_HV_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		
	def actionPerformed(self,actionEvent):
		ws_table_elements_arr = self.ws_lw_acquisition_controller.ws_table_model.getWS_TableElements()
		ws_table = self.ws_lw_acquisition_controller.ws_table
		index = ws_table.getSelectedRow()
		if(index < 0): return		
		ws_table_element = ws_table_elements_arr[index]
		ws_scan_record_hor = ws_table_element.ws_scan_record_hor
		ws_scan_record_hor.setDirection(WS_DIRECTION_VER)
		ws_scan_record_ver = ws_table_element.ws_scan_record_ver
		ws_scan_record_ver.setDirection(WS_DIRECTION_HOR)
		ws_table_element.ws_scan_record_hor = ws_scan_record_ver
		ws_table_element.ws_scan_record_ver = ws_scan_record_hor
		self.ws_lw_acquisition_controller.ws_table_model.fireTableDataChanged()
		self.ws_lw_acquisition_controller.setDataToGraphPanels(index)
		ws_table.setRowSelectionInterval(index,index)
	
class WS_Data_to_Pool_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		
	def actionPerformed(self,actionEvent):
		ws_table_elements_arr = self.ws_lw_acquisition_controller.ws_table_model.getWS_TableElements()
		ws_records_table_model = self.ws_lw_acquisition_controller.ws_data_analysis_controller.ws_records_table_model
		quadCavDict = self.ws_lw_acquisition_controller.getFreshCopyQuadCavDict()
		record_index = len(ws_records_table_model.ws_rec_table_element_arr) - 1
		for ws_table_elemen in ws_table_elements_arr:
			if(ws_table_elemen.isOn):
				record_index += 1
				ws_hor_record = ws_table_elemen.getRecords(WS_DIRECTION_HOR)
				ws_wf_record = WS_Scan_and_Fit_Record(ws_hor_record)
				ws_wf_record.setParamDict(quadCavDict)
				ws_wf_record.index = record_index
				ws_records_table_model.addWS_RecordElement(ws_wf_record)
				record_index += 1
				ws_ver_record = ws_table_elemen.getRecords(WS_DIRECTION_VER)
				ws_wf_record = WS_Scan_and_Fit_Record(ws_ver_record)
				ws_wf_record.setParamDict(quadCavDict)
				ws_wf_record.index = record_index
				ws_records_table_model.addWS_RecordElement(ws_wf_record)

		self.ws_lw_acquisition_controller.ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()

class WS_Record_Table_Selection_Listener(ListSelectionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		self.dbltxt_arr = []
		for i in range(6):
			self.dbltxt_arr.append(DoubleInputTextField("0."))
		
	def valueChanged(self,listSelectionEvent):
		if(listSelectionEvent.getValueIsAdjusting()): return
		listSelectionModel = listSelectionEvent.getSource()
		index = listSelectionModel.getMinSelectionIndex()	
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		#---------set up Gaussian Params
		gauss_fit_param_panel = ws_data_analysis_controller.gauss_fit_param_panel
		if(index < 0):
			gauss_fit_param_panel = ws_data_analysis_controller.gauss_fit_param_panel
			gauss_fit_param_panel.setGaussParamFiled("const",self.dbltxt_arr[0])
			gauss_fit_param_panel.setGaussParamFiled("A0",self.dbltxt_arr[1])
			gauss_fit_param_panel.setGaussParamFiled("X center",self.dbltxt_arr[2])
			gauss_fit_param_panel.setGaussParamFiled("sigma",self.dbltxt_arr[3])
			gauss_fit_param_panel.setGaussParamFiled("x_min",self.dbltxt_arr[4])
			gauss_fit_param_panel.setGaussParamFiled("x_max",self.dbltxt_arr[5])	
			ws_data_analysis_controller.gpanel_WF.removeAllGraphData()
			ws_data_analysis_controller.gpanel_WF.setVerticalLineValue(-1.0e+30,0)
			ws_data_analysis_controller.gpanel_WF.setVerticalLineValue(+1.0e+30,1)
		else:
			ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
			ws_data_analysis_controller.setDataToGraphPanel(ws_scan_and_fit_record)		
			gauss_fit_param_panel.setGaussParamFiled("const",ws_scan_and_fit_record.CONST)
			gauss_fit_param_panel.setGaussParamFiled("A0",ws_scan_and_fit_record.A0)
			gauss_fit_param_panel.setGaussParamFiled("X center",ws_scan_and_fit_record.X_CENTER)
			gauss_fit_param_panel.setGaussParamFiled("sigma",ws_scan_and_fit_record.SIGMA)
			gauss_fit_param_panel.setGaussParamFiled("x_min",ws_scan_and_fit_record.X_MIN)
			gauss_fit_param_panel.setGaussParamFiled("x_max",ws_scan_and_fit_record.X_MAX)
			ws_data_analysis_controller.gpanel_WF.setVerticalLineValue(ws_scan_and_fit_record.X_MIN.getValue(),0)
			ws_data_analysis_controller.gpanel_WF.setVerticalLineValue(ws_scan_and_fit_record.X_MAX.getValue(),1)	
		gauss_fit_param_panel.validate()
		gauss_fit_param_panel.repaint()			
		#print "debug selection index=",index,"  adjust=",listSelectionEvent.getValueIsAdjusting()
		
class Guess_And_Fit_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		records_table = ws_data_analysis_controller.records_table
		index = records_table.getSelectedRow()
		#print "debug fit index=",index
		if(index < 0): return
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
		local_gauss_fitter = ws_data_analysis_controller.local_gauss_fitter
		local_gauss_fitter.setRecord(ws_scan_and_fit_record)
		local_gauss_fitter.guessAndFit()
		ws_data_analysis_controller.setDataToGraphPanel(ws_scan_and_fit_record)
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		records_table.setRowSelectionInterval(index,index)

class Guess_And_Fit_All_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		records_table = ws_data_analysis_controller.records_table
		index = records_table.getSelectedRow()
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		for ws_scan_and_fit_record in ws_records_table_model.ws_rec_table_element_arr:
			if(ws_scan_and_fit_record.isOn):
				local_gauss_fitter = ws_data_analysis_controller.local_gauss_fitter
				local_gauss_fitter.setRecord(ws_scan_and_fit_record)
				local_gauss_fitter.guessAndFit()
		if(index >= 0):
			ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
			ws_data_analysis_controller.setDataToGraphPanel(ws_scan_and_fit_record)
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		if(index >= 0):
			records_table.setRowSelectionInterval(index,index)

class Fit_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		records_table = ws_data_analysis_controller.records_table
		index = records_table.getSelectedRow()
		#print "debug fit index=",index
		if(index < 0): return
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
		local_gauss_fitter = ws_data_analysis_controller.local_gauss_fitter
		local_gauss_fitter.setRecord(ws_scan_and_fit_record)
		local_gauss_fitter.fit()
		ws_data_analysis_controller.setDataToGraphPanel(ws_scan_and_fit_record)
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		records_table.setRowSelectionInterval(index,index)
		
class Fit_All_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		records_table = ws_data_analysis_controller.records_table
		index = records_table.getSelectedRow()
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		for ws_scan_and_fit_record in ws_records_table_model.ws_rec_table_element_arr:
			local_gauss_fitter = ws_data_analysis_controller.local_gauss_fitter
			local_gauss_fitter.setRecord(ws_scan_and_fit_record)
			local_gauss_fitter.fit()
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		if(index >= 0):
			ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
			ws_data_analysis_controller.setDataToGraphPanel(ws_scan_and_fit_record)			
			records_table.setRowSelectionInterval(index,index)		

class Position_Limits_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		grf = actionEvent.getSource()
		limit_index = grf.getDraggedLineIndex()
		if(limit_index != 0 and limit_index != 1): return
		x_limit = grf.getVerticalValue(limit_index)
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		records_table = ws_data_analysis_controller.records_table
		index = records_table.getSelectedRow()
		if(index < 0): return
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		ws_scan_and_fit_record = ws_records_table_model.ws_rec_table_element_arr[index]
		if(limit_index == 0):
			ws_scan_and_fit_record.X_MIN.setValue(x_limit)
		else:
			ws_scan_and_fit_record.X_MAX.setValue(x_limit)

class SendToAnalysis_Listener(ActionListener):
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller

	def actionPerformed(self,actionEvent):
		#----------initial data in ws_rec_table_element_arr---------------
		ws_data_analysis_controller = self.ws_lw_acquisition_controller.ws_data_analysis_controller
		ws_records_table_model = ws_data_analysis_controller.ws_records_table_model
		ws_rec_table_element_arr = ws_records_table_model.ws_rec_table_element_arr
		for ind in range(len(ws_rec_table_element_arr)):
			ws_rec_table_element_arr[ind].index = ind + 1
		#-------data for analysis -------------------------------------
		linac_wizard_document = self.ws_lw_acquisition_controller.linac_wizard_document
		tr_twiss_analysis_controller = linac_wizard_document.tr_twiss_analysis_controller
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		accStatesKeeper.clean()
		for ws_scan_and_fit_record in ws_rec_table_element_arr:
			if(ws_scan_and_fit_record.isOn):
				ws_scan_and_fit_record.pos = linac_wizard_document.accSeq.getPosition(ws_scan_and_fit_record.ws_node)
				ws_lw_size_record = tr_twiss_analysis_controller.makeWS_LW_Size_Record()
				ws_lw_size_record.setUpParams(ws_scan_and_fit_record)
				accStatesKeeper.addRecord(ws_lw_size_record)
				#print "debug ws_lw_size_record.ws_node=",ws_lw_size_record.ws_node.getId()," index=",ws_lw_size_record.index," ind0=",ws_scan_and_fit_record.index
		tr_twiss_analysis_controller.dict_panel.dict_table.getModel().fireTableDataChanged()
		
#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class WS_LW_Acquisition_Controller:
	def __init__(self,linac_wizard_document):
		#--- linac_wizard_document the parent document for all controllers
		self.linac_wizard_document = linac_wizard_document		
		self.main_panel = JPanel(GridLayout(2,1))
		self.quadFieldDict = {}
		self.cavAmpPhaseDict = {}
		self.quads = []
		self.cavs = []
		self.ws_table_model = WS_Table_Model()
		#----make LW or WS nodes
		self.ws_nodes = []
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---make the WS_JTable + H&V Graphs Panels
		self.ws_table = JTable(self.ws_table_model)
		self.ws_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.ws_table.setFillsViewportHeight(true)	
		ws_panel = JPanel(BorderLayout())
		ws_panel.add(JScrollPane(self.ws_table), BorderLayout.CENTER)
		ws_knobs_panel = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		ws_panel.add(ws_knobs_panel, BorderLayout.SOUTH)
		ws_panel.setBorder(etched_border)
		#---ws knobs ---------------------------------------------
		get_ws_data_button = JButton("Get WS/LW Data")
		switch_hv_button = JButton(" Switch H/V  ")	
		add_data_to_pool_button = JButton("  Add Data to Pool  ")
		ws_knobs_panel.add(get_ws_data_button)
		ws_knobs_panel.add(switch_hv_button)
		ws_knobs_panel.add(add_data_to_pool_button)
		get_ws_data_button.addActionListener(WS_GET_Data_Listener(self))
		switch_hv_button.addActionListener(Switch_HV_Listener(self))
		add_data_to_pool_button.addActionListener(WS_Data_to_Pool_Listener(self))
		#---------------------------------------------------------
		ws_and_graphs_panel = JPanel(BorderLayout())
		border = BorderFactory.createTitledBorder(etched_border,"WS/LW Raw Data Acquisition")
		ws_and_graphs_panel.setBorder(border)
		ws_and_graphs_panel.add(ws_panel, BorderLayout.WEST)
		graphs_panel = JPanel(GridLayout(1,2))
		self.gpanel_horWF = FunctionGraphsJPanel()
		self.gpanel_verWF = FunctionGraphsJPanel()
		self.gpanel_horWF.setLegendButtonVisible(true)
		self.gpanel_horWF.setChooseModeButtonVisible(true)			
		self.gpanel_verWF.setLegendButtonVisible(true)
		self.gpanel_verWF.setChooseModeButtonVisible(true)			
		self.gpanel_horWF.setName("Horizontal Profiles")
		self.gpanel_horWF.setAxisNames("pos, [mm]","Amp, [arb. units]")	
		self.gpanel_verWF.setName("Vertical Profiles")
		self.gpanel_verWF.setAxisNames("pos, [mm]","Amp, [arb. units]")	
		self.gpanel_horWF.setBorder(etched_border)
		self.gpanel_verWF.setBorder(etched_border)
		graphs_panel.add(self.gpanel_horWF)
		graphs_panel.add(self.gpanel_verWF)
		ws_and_graphs_panel.add(graphs_panel, BorderLayout.CENTER)
		self.setDataToGraphPanels(-1)
		#------analysis sub-panel
		self.ws_data_analysis_controller = WS_Data_Analysis_Controller(self)
		#---put everything into the main_panel
		self.main_panel.add(ws_and_graphs_panel)
		self.main_panel.add(self.ws_data_analysis_controller.main_panel)
		#---set up Listeners 
		self.ws_table.getSelectionModel().addListSelectionListener(WS_Table_Selection_Listener(self))	
		
	def setAccSeq(self,accSeq):
		self.ws_table_model.ws_table_element_arr = []
		self.ws_nodes = []
		if(accSeq == null): 
			self.ws_table_model.fireTableDataChanged()
			return
		nodes = accSeq.filterNodesByStatus(accSeq.getAllNodesOfType(ProfileMonitor.s_strType),true)
		for node in nodes:
			self.ws_nodes.append(node)
		nodes = accSeq.filterNodesByStatus(accSeq.getAllNodesOfType(Marker.s_strType),true)
		for node in nodes:
			if(node.getId().find("LW") >= 0 and node.getId().find("SCL") >= 0):
				self.ws_nodes.append(node)
		for node in self.ws_nodes:
			self.ws_table_model.addWS_TableElement(WS_Table_Element(node))	
		self.makePVforWS()
		#ws_table_elements_arr = self.ws_table_model.getWS_TableElements()
		#for ws_table_element in ws_table_elements_arr:	
		#	ws_table_element.readPVData()
		#----update data and graphs
		self.ws_table_model.fireTableDataChanged()
		self.setDataToGraphPanels(-1)
		#------------set up the quad and cavities dictionaries
		self.quadFieldDict = {}
		self.quads = accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(Quadrupole.s_strType)))
		for quad in self.quads:
			self.quadFieldDict[quad] = quad.getDfltField()
			"""
			if(quad.isPermanent()):
				self.quadFieldDict[quad] = quad.getDfltField()
			else:
				self.quadFieldDict[quad] = quad.getFieldReadback()
			"""
		self.cavAmpPhaseDict = {}
		rf_gaps = accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))	
		self.cavs = []
		for rf_gap in rf_gaps:
			cav = rf_gap.getParent()
			if(cav not in self.cavs):
				self.cavs.append(cav)
				amp = cav.getDfltCavAmp()
				phase = cav.getDfltCavPhase()
				self.cavAmpPhaseDict[cav] = [amp,phase]
			
	def cleanOldWSdata(self):
		#----clean all data for analysis -- may be we want to keep them?
		ws_data_analysis_controller = self.ws_data_analysis_controller
		ws_data_analysis_controller.ws_records_table_model.ws_rec_table_element_arr = []
		ws_data_analysis_controller.ws_records_table_model.fireTableDataChanged()
		tr_twiss_analysis_controller = self.linac_wizard_document.tr_twiss_analysis_controller
		accStatesKeeper = tr_twiss_analysis_controller.accStatesKeeper
		accStatesKeeper.clean()
		tr_twiss_analysis_controller.dict_panel.dict_table.getModel().fireTableDataChanged()

	def getFreshCopyQuadCavDict(self):
		""" Returns a copy of the fresh  dictionaries [quadFieldDict,cavAmpPhaseDict] """
		quadFieldDict = {}
		for quad in self.quads:
			if(quad.isPermanent()):
				self.quadFieldDict[quad] = quad.getDfltField()
			else:
				self.quadFieldDict[quad] = quad.getFieldReadback()
			quadFieldDict[quad] = self.quadFieldDict[quad]
			#print "debug quad=",quad.getId()," G=",self.quadFieldDict[quad]
		cavAmpPhaseDict = {}
		for cav in self.cavs:
			amp = cav.getDfltCavAmp()
			phase = cav.getDfltCavPhase()
			self.cavAmpPhaseDict[cav] = [amp,phase]
			cavAmpPhaseDict[cav] = [amp,phase]
		return [quadFieldDict,cavAmpPhaseDict]

	def getMainPanel(self):
		return self.main_panel
		
	def setDataToGraphPanels(self,index):
		self.gpanel_horWF.removeAllGraphData()
		self.gpanel_verWF.removeAllGraphData()
		ws_table_elements_arr = self.ws_table_model.getWS_TableElements()
		if(index < 0 or index >= len(ws_table_elements_arr)): return
		ws_table_element = ws_table_elements_arr[index]
		hor_record = ws_table_element.getRecords(WS_DIRECTION_HOR)
		ver_record = ws_table_element.getRecords(WS_DIRECTION_VER)			
		self.gpanel_horWF.addGraphData(hor_record.gd_wf)
		self.gpanel_verWF.addGraphData(ver_record.gd_wf)
				
	def makePVforWS(self):
		ws_table_elements_arr = self.ws_table_model.getWS_TableElements()
		for ws_table_element in ws_table_elements_arr:
			ws_node = ws_table_element.ws
			if(ws_node.getId().find("WS") >= 0):
				pv_pos_wf_name = ws_node.getId()+":Hor_prof_pos"
				pv_wf_name     = ws_node.getId()+":Hor_prof_sig"
				sigma_name     = ws_node.getId()+":Hor_Sigma_gs"
				ws_table_element.setPV_Names(pv_pos_wf_name,pv_wf_name,sigma_name,WS_DIRECTION_HOR)
				pv_pos_wf_name = ws_node.getId()+":Ver_prof_pos"
				pv_wf_name     = ws_node.getId()+":Ver_prof_sig"
				sigma_name     = ws_node.getId()+":Ver_Sigma_gs"
				ws_table_element.setPV_Names(pv_pos_wf_name,pv_wf_name,sigma_name,WS_DIRECTION_VER)
			else:
				name = ws_node.getId().replace(":LW",":LW_")+":LASER_WIRE_axis"
				pv_pos_wf_name = name+"01_ScanPosArray"
				pv_wf_name     = name+"01_Peak_Profile_WFM"
				sigma_name     = name+"01_Peak_Info_Sigma"
				ws_table_element.setPV_Names(pv_pos_wf_name,pv_wf_name,sigma_name,WS_DIRECTION_HOR)
				pv_pos_wf_name = name+"02_ScanPosArray"
				pv_wf_name     = name+"02_Peak_Profile_WFM"
				sigma_name     = name+"02_Peak_Info_Sigma"
				ws_table_element.setPV_Names(pv_pos_wf_name,pv_wf_name,sigma_name,WS_DIRECTION_VER)
			#print "debug node=",ws_node.getId()

class WS_Data_Analysis_Controller:
	""" 
	This controller manages the WS data analysis. 
	Its panel is in the WS_LW_Acquisition_Controller panel. 
	"""
	def __init__(self,ws_lw_acquisition_controller):
		self.ws_lw_acquisition_controller = ws_lw_acquisition_controller
		self.local_gauss_fitter = Local_Gauss_Fitter()
		self.main_panel = JPanel(BorderLayout())
		self.ws_records_table_model = WS_Records_Table_Model()
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		#---make the WS_Records_JTable + Graph Panel
		self.records_table = JTable(self.ws_records_table_model)
		self.records_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.records_table.setFillsViewportHeight(true)
		#----set up records_table columns width
		columnModel = self.records_table.getColumnModel()
		columnModel.getColumn(0).setPreferredWidth(30)
		columnModel.getColumn(1).setPreferredWidth(160)
		columnModel.getColumn(2).setPreferredWidth(40)
		columnModel.getColumn(3).setPreferredWidth(40)
		columnModel.getColumn(4).setPreferredWidth(80)
		columnModel.getColumn(5).setPreferredWidth(70)
		columnModel.getColumn(6).setPreferredWidth(70)
		#---------------------------------------------------
		records_panel = JPanel(BorderLayout())
		records_panel.add(JScrollPane(self.records_table), BorderLayout.CENTER)
		knobs_gauss_panel = JPanel(BorderLayout())
		records_panel.add(knobs_gauss_panel, BorderLayout.SOUTH)
		records_panel.setBorder(etched_border)
		#---records knobs ---------------------------------------------
		records_knobs_panel = JPanel(FlowLayout(FlowLayout.CENTER,5,5))
		guess_and_fit_button = JButton("Guess and Fit")
		analysis_all_button = JButton("G&F All")		
		fit_button = JButton("Fit")
		fit_all_button = JButton("Fit All")
		send_to_twiss_button = JButton("Send to Twiss Analysis")
		records_knobs_panel.add(guess_and_fit_button)
		records_knobs_panel.add(analysis_all_button)		
		records_knobs_panel.add(fit_button)
		records_knobs_panel.add(fit_all_button)
		records_knobs_panel.add(send_to_twiss_button)
		guess_and_fit_button.addActionListener(Guess_And_Fit_Listener(self.ws_lw_acquisition_controller))
		fit_button.addActionListener(Fit_Listener(self.ws_lw_acquisition_controller))
		fit_all_button.addActionListener(Fit_All_Listener(self.ws_lw_acquisition_controller))
		analysis_all_button.addActionListener(Guess_And_Fit_All_Listener(self.ws_lw_acquisition_controller))
		send_to_twiss_button.addActionListener(SendToAnalysis_Listener(self.ws_lw_acquisition_controller))
		self.gauss_fit_param_panel = GaussFitDataPanel()
		knobs_gauss_panel.add(records_knobs_panel, BorderLayout.NORTH)
		knobs_gauss_panel.add(self.gauss_fit_param_panel, BorderLayout.SOUTH)
		#---------------------------------------------------------
		self.record_analysis_panel = JPanel(BorderLayout())
		border = BorderFactory.createTitledBorder(etched_border,"WS/LW Wave Form Analysis")
		self.record_analysis_panel.setBorder(border)
		self.record_analysis_panel.add(records_panel, BorderLayout.WEST)
		self.gpanel_WF = FunctionGraphsJPanel()
		self.gpanel_WF.setLegendButtonVisible(true)
		self.gpanel_WF.setChooseModeButtonVisible(true)			
		self.gpanel_WF.setName("Profile")
		self.gpanel_WF.setAxisNames("pos, [mm]","Amp, [arb. units]")	
		self.gpanel_WF.setBorder(etched_border)
		self.gpanel_WF.addVerticalLine(-1.0e+30,Color.red)
		self.gpanel_WF.addVerticalLine(+1.0e+30,Color.red)
		self.gpanel_WF.setVerLinesButtonVisible(true)
		self.gpanel_WF.addDraggedVerLinesListener(Position_Limits_Listener(self.ws_lw_acquisition_controller))
		self.record_analysis_panel.add(self.gpanel_WF, BorderLayout.CENTER)
		self.main_panel.add(self.record_analysis_panel, BorderLayout.CENTER)
		#---set up Listeners 
		self.records_table.getSelectionModel().addListSelectionListener(WS_Record_Table_Selection_Listener(self.ws_lw_acquisition_controller))

	def setDataToGraphPanel(self,ws_record):
		self.gpanel_WF.removeAllGraphData()
		self.gpanel_WF.addGraphData(ws_record.gd_wf)
		self.gpanel_WF.addGraphData(ws_record.gd_fit_wf)
		
class LabelDoulbeTextFieldPanel(JPanel):	
	def __init__(self,n_row,n_col):
		JPanel.__init__(self,GridLayout(n_row,n_col))
		etched_border = BorderFactory.createEtchedBorder()
		self.setBorder(etched_border)

	def addLabelAndText(self,label_text,dbl_text_filed):
		sub_panel = JPanel(GridLayout(1,2))
		sub_panel.add(JLabel(label_text,JLabel.CENTER))
		sub_panel.add(dbl_text_filed)
		self.add(sub_panel)
		
	def setDblTextField(self,dbl_text_filed, index):
		sub_panel = self.getComponent(index)
		label = sub_panel.getComponent(0)
		sub_panel.removeAll()
		sub_panel.add(label)
		sub_panel.add(dbl_text_filed)

class GaussFitDataPanel(LabelDoulbeTextFieldPanel):
	def __init__(self):
		LabelDoulbeTextFieldPanel.__init__(self,3,2)
		dbl_text_filed1 = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		dbl_text_filed2 = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		dbl_text_filed3 = DoubleInputTextField(0.,FortranNumberFormat("G8.4"),10)
		dbl_text_filed4 = DoubleInputTextField(0.,FortranNumberFormat("G8.2"),10)
		dbl_text_filed5 = DoubleInputTextField(0.,FortranNumberFormat("G8.2"),10)
		dbl_text_filed6 = DoubleInputTextField(0.,FortranNumberFormat("G8.2"),10)
		dbl_text_filed1.setValueQuietly(0.)
		dbl_text_filed2.setValueQuietly(0.)
		dbl_text_filed3.setValueQuietly(0.)
		dbl_text_filed4.setValueQuietly(0.)
		dbl_text_filed5.setValueQuietly(0.)
		dbl_text_filed6.setValueQuietly(0.)
		dbl_text_filed1.setAlertBackground(Color.white)
		dbl_text_filed2.setAlertBackground(Color.white)
		dbl_text_filed3.setAlertBackground(Color.white)
		dbl_text_filed4.setAlertBackground(Color.white)
		dbl_text_filed5.setAlertBackground(Color.white)
		dbl_text_filed6.setAlertBackground(Color.white)
		self.addLabelAndText("const"   ,dbl_text_filed1)
		self.addLabelAndText("A0"      ,dbl_text_filed2)
		self.addLabelAndText("X center",dbl_text_filed3)
		self.addLabelAndText("sigma"   ,dbl_text_filed4)
		self.addLabelAndText("x_min"   ,dbl_text_filed5)
		self.addLabelAndText("x_max"   ,dbl_text_filed6)

	def setGaussParamFiled(self,field, dbl_text_filed):
		if(field == "const"):
			self.setDblTextField(dbl_text_filed,0)
		if(field == "A0"):
			self.setDblTextField(dbl_text_filed,1)
		if(field == "X center"):
			self.setDblTextField(dbl_text_filed,2)
		if(field == "sigma"):
			self.setDblTextField(dbl_text_filed,3)
		if(field == "x_min"):
			self.setDblTextField(dbl_text_filed,4)
		if(field == "x_max"):
			self.setDblTextField(dbl_text_filed,5)
		

		
		
				
