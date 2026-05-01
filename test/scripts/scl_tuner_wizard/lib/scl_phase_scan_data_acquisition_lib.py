# The classes for the SCL cavities phase scan data acquisition

import sys
import math
import time
import random

from xjava.lang import *
from java.awt import Color

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.tools.beam import Twiss
from xal.ca import BatchGetValueRequest, ChannelFactory

from harmonics_fitter_lib import  HarmonicsAnalyzer, HramonicsFunc, makePhaseNear
from constants_lib import GRAPH_LEGEND_KEY
from linac_wizard_read_write_file_lib import dumpGraphDataToDA, readGraphDataFromDA

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------
#  Auxiliary Classes
#------------------------------------------------------

class BPM_Batch_Reader:
	"""
	Collects PV channel data for all BPMs
	"""
	def __init__(self,top_level_cotroller = null):
		self.top_level_cotroller = top_level_cotroller
		self.bpms = []
		self.batchGetRequest = null
		self.beam_trigger = null
		#--------------------------------------------
		self.bpm_ch_amp_phase_dict = {}
		self.bpm_ch_x_y_dict = {}
		self.bpm_old_phases_dict = {}
		self.bpm_wrappers = []

	def setBPMs(self,bpm_wrappers):
		self.batchGetRequest = BatchGetValueRequest()
		self.bpm_wrappers = bpm_wrappers
		#--------------------------------------------
		self.bpm_ch_amp_phase_dict = {}
		self.bpm_ch_x_y_dict = {}
		self.bpm_old_phases_dict = {}	
		ch_arr = []
		for bpm_wrapper in self.bpm_wrappers:
			if(not bpm_wrapper.isGood): continue
			bpm = bpm_wrapper.getBPM()
			ch_ampl = ChannelFactory.defaultFactory().getChannel(bpm.getId()+":amplitudeAvg")
			ch_phase = ChannelFactory.defaultFactory().getChannel(bpm.getId()+":phaseAvg")
			ch_posX = ChannelFactory.defaultFactory().getChannel(bpm.getId()+":xAvg")
			ch_posY = ChannelFactory.defaultFactory().getChannel(bpm.getId()+":yAvg")
			#---- this is a pv channel for the time shift parameter 
			ch_oeda = bpm_wrapper.bpm_timing_bucket.pv_oeda
			if(ch_ampl.connectAndWait(2.0) and ch_phase.connectAndWait(2.0) and ch_oeda.connectAndWait(2.0)):	
				self.bpm_ch_amp_phase_dict[bpm_wrapper] = (ch_ampl,ch_phase)
				self.bpm_ch_x_y_dict[bpm_wrapper] = (ch_posX,ch_posY) 
				#print "debug bpm=",bpm_wrapper.alias," added!"
				ch_arr.append(ch_ampl)
				ch_arr.append(ch_phase)
				ch_arr.append(ch_posX)
				ch_arr.append(ch_posY)
			else:
				bpm_wrapper.isGood = false
				#print "debug bad BPM =",bpm_wrapper.alias
		for ch in ch_arr:
			self.batchGetRequest.addChannel(ch)

	def getBPM_Wrappers(self):
		return self.bpm_wrappers
	
	def setBeamTrigger(self,beam_trigger):
		self.beam_trigger = beam_trigger
	
	def getBatchGetRequest(self):
		""" returns the batch request object. """
		return self.batchGetRequest

	def makeMeasurement(self):
		""" 
		It will get a new set of BPM data into the self.batchGetRequest
		"""		
		count = 0
		bad_count = 0
		result_info = false 
		#print "debug make shot!"
		tm_start = time.time()
		trigger_res = self.beam_trigger.makeShot()
		#print "debug shot is done = dT=",time.time() -tm_start 
		if(not trigger_res): return false
		while(result_info == false):
			count += 1
			if(count > 20): return false
			result_info = true
			res_info = self.batchGetRequest.submitAndWait(3.)
			#print "debug shot batch request is done res=",res_info," dT=",time.time() -tm_start
			if(res_info == false):
				if(self.top_level_cotroller != null):
					failed_channels = self.batchGetRequest.getFailedChannels()
					st = ""
					if(len(failed_channels) > 0): st = " One of bad channels = "+failed_channels[0].channelName()
					for channel in failed_channels:
						print "Failed channel name = ",channel.channelName()
					messageTextField = self.top_level_cotroller.getMessageTextField()
					messageTextField.setText("Cannot read BPM data! Stop and repare the BPM list!" + st)
				return false
			#check if the results are different from the previous measurements
			bad_bpm_name = null
			for bpm_wrapper in self.bpm_wrappers:
				bool_var = self.bpm_old_phases_dict.has_key(bpm_wrapper)
				bool_var = bool_var and self.bpm_ch_amp_phase_dict.has_key(bpm_wrapper)
				if(bool_var):
					(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[bpm_wrapper]
					bpm_phase_new = self.batchGetRequest.getRecord(ch_phase).doubleValue()
					bpm_phase_old = self.bpm_old_phases_dict[bpm_wrapper]
					if(bpm_phase_new == bpm_phase_old):
						#print "debug bad point for bpm=",bpm_wrapper.alias," old phase=",bpm_phase_old," new=",bpm_phase_new
						result_info = false
						bad_bpm_name = bpm_wrapper.alias
						bad_count += 1
						break
			#print "debug measure is done res=",result_info," dT=",time.time() -tm_start," top=",self.top_level_cotroller
			if(not result_info):
				if(self.top_level_cotroller != null):
					messageTextField = self.top_level_cotroller.getMessageTextField()
					txt = ""
					if(bad_bpm_name != null):
						txt = " Bad BPM="+bad_bpm_name+"!"
					messageTextField.setText("BPM data did not update after the beam trigger! Do something! Also check MEBT:BPM01."+txt)	
				if(bad_count > 5): time.sleep(1.)
				trigger_res = self.beam_trigger.makeShot()
				if(not trigger_res): return false
				time.sleep(0.01)
				continue
		for bpm_wrapper in self.bpm_wrappers:
			if(self.bpm_ch_amp_phase_dict.has_key(bpm_wrapper)):
				(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[bpm_wrapper]
				bpm_phase = self.batchGetRequest.getRecord(ch_phase).doubleValue()
				self.bpm_old_phases_dict[bpm_wrapper] = bpm_phase
		#print "debug measure is done return dT=",time.time() -tm_start
		return true		

	def makePhaseScanStep(self, cav_phase, cav_wrapper, bpm_amp_min_limit = 0.1):
		""" 
		It will add one point (cav_phase,bpm_amp) or (cav_phase,bpm_phase) to the plots in the dictonary.
		bpm_amp_phase_dict[bpm_wrapper] = (graphDataAmp,graphDataPhase)
		"""	
		scan_is_good = false
		count = 0
		bad_count_limit = 3
		while(not scan_is_good):
			scan_is_good = true
			count += 1
			if(count >bad_count_limit): return false
			if(not self.makeMeasurement()): return false
			bpm_amp_phase_dict = cav_wrapper.bpm_amp_phase_dict
			bpm_xy_dict = cav_wrapper.bpm_xy_dict
			#--------get bpm amp and phase -------------
			if(bpm_amp_phase_dict.has_key(cav_wrapper.bpm_wrapper0)):
				(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[cav_wrapper.bpm_wrapper0]
				bpm_amp = self.batchGetRequest.getRecord(ch_ampl).doubleValue()				
				bpm_phase = self.batchGetRequest.getRecord(ch_phase).doubleValue()
				if(bpm_amp < bpm_amp_min_limit): scan_is_good = false
			if(bpm_amp_phase_dict.has_key(cav_wrapper.bpm_wrapper1)):
				(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[cav_wrapper.bpm_wrapper1]
				bpm_amp = self.batchGetRequest.getRecord(ch_ampl).doubleValue()				
				bpm_phase = self.batchGetRequest.getRecord(ch_phase).doubleValue()
				if(bpm_amp < bpm_amp_min_limit): scan_is_good = false
			if(cav_wrapper.bpm_wrapper0 == null):
				#that is for "All Off" cavity wrapper
				bpm_wrapper_first_good = null
				for bpm_wrapper in self.bpm_wrappers:
					if(bpm_wrapper.isGood and bpm_amp_phase_dict.has_key(bpm_wrapper)): 
						bpm_wrapper_first_good = bpm_wrapper
						break
				if(bpm_wrapper_first_good != null):
					(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[bpm_wrapper_first_good]
					bpm_amp = self.batchGetRequest.getRecord(ch_ampl).doubleValue()
					if(bpm_amp < bpm_amp_min_limit): scan_is_good = false
				else:
					scan_is_good = false
			if(scan_is_good == false): continue
			for bpm_wrapper in self.bpm_wrappers:
				if(bpm_amp_phase_dict.has_key(bpm_wrapper) and self.bpm_ch_amp_phase_dict.has_key(bpm_wrapper)):
					(graphDataAmp,graphDataPhase) = bpm_amp_phase_dict[bpm_wrapper]
					(graphDataX,graphDataY) = bpm_xy_dict[bpm_wrapper]
					(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[bpm_wrapper]
					(ch_posX,ch_posY) = self.bpm_ch_x_y_dict[bpm_wrapper]
					#print "debug ch amp=",ch_ampl.channelName()," phase=",ch_phase.channelName()
					bpm_amp = self.batchGetRequest.getRecord(ch_ampl).doubleValue()				
					bpm_phase = self.batchGetRequest.getRecord(ch_phase).doubleValue()
					pos_x = self.batchGetRequest.getRecord(ch_posX).doubleValue()
					pos_y = self.batchGetRequest.getRecord(ch_posY).doubleValue()
					graphDataAmp.addPoint(cav_phase,bpm_amp)
					old_bpm_phase = 0.
					if(graphDataPhase.getNumbOfPoints() > 0):
						old_bpm_phase = graphDataPhase.getY(graphDataPhase.getNumbOfPoints() - 1)
					graphDataPhase.addPoint(cav_phase,makePhaseNear(bpm_phase,old_bpm_phase))
					graphDataX.addPoint(cav_phase,pos_x)
					graphDataY.addPoint(cav_phase,pos_y)
		return true
		
	def collectStatistics(self,bpm_amp_phase_data_dict):
		""" 
		It will add one point BPMs' amp and phase to the arrays in the dictonary.
		bpm_amp_phase_data_dict[bpm_wrapper] = ([amp0,amp1,...],[phase0,phase1,...])
		"""		
		if(not self.makeMeasurement()): return false
		#--------get bpm amp and phase -------------
		for bpm_wrapper in self.bpm_wrappers:
			if(bpm_amp_phase_data_dict.has_key(bpm_wrapper) and  self.bpm_ch_amp_phase_dict.has_key(bpm_wrapper)):
				(amp_arr,phase_arr) = bpm_amp_phase_data_dict[bpm_wrapper]
				(ch_ampl,ch_phase) = self.bpm_ch_amp_phase_dict[bpm_wrapper]
				#print "debug ch amp=",ch_ampl.channelName()," phase=",ch_phase.channelName()
				bpm_amp = self.batchGetRequest.getRecord(ch_ampl).doubleValue()				
				bpm_phase = self.batchGetRequest.getRecord(ch_phase).doubleValue()
				amp_arr.append(bpm_amp)
				phase_arr.append(bpm_phase)
		return true		

class CavityRescaleBucket:
	# This class keeps the rescaling data 
	def __init__(self,cav_wrapper):
		self.cav_wrapper = cav_wrapper
		self.clean()
		
	def clean(self):
		self.arrivalTime = 0.
		self.liveAmp = 0.
		self.livePhase = 0.
		self.designAmp = 0.
		self.designPhase = 0.
		self.avg_gap_phase = 0.
		self.eKin_in = 0.
		self.eKin_out = 0.		
 		
	def writeDataToXML(self,root_da):
		rescale_bucket_da = root_da.createChild("cavity_rescale_bucket")
		rescale_bucket_da.setValue("arrivalTime",self.arrivalTime)
		rescale_bucket_da.setValue("liveAmp","%9.4f"%self.liveAmp)
		rescale_bucket_da.setValue("livePhase","%6.2f"%self.livePhase)
		rescale_bucket_da.setValue("designAmp", "%9.4f"%self.designAmp)
		rescale_bucket_da.setValue("designPhase", "%6.2f"%self.designPhase)
		rescale_bucket_da.setValue("avg_gap_phase", "%6.2f"%self.avg_gap_phase)
		rescale_bucket_da.setValue("eKin_in", "%9.4f"%self.eKin_in)
		rescale_bucket_da.setValue("eKin_out", "%9.4f"%self.eKin_out)
		
	def readDataFromXML(self,root_da):
		rescale_bucket_da = root_da.childAdaptor("cavity_rescale_bucket")
		if(rescale_bucket_da == null): return
		#--------- read parameters-------------------------
		self.arrivalTime = rescale_bucket_da.doubleValue("arrivalTime")
		self.liveAmp = rescale_bucket_da.doubleValue("liveAmp")
		self.livePhase = rescale_bucket_da.doubleValue("livePhase")
		self.designAmp = rescale_bucket_da.doubleValue("designAmp")
		self.designPhase = rescale_bucket_da.doubleValue("designPhase")
		self.avg_gap_phase = rescale_bucket_da.doubleValue("avg_gap_phase")
		self.eKin_in = rescale_bucket_da.doubleValue("eKin_in")
		self.eKin_out = rescale_bucket_da.doubleValue("eKin_out")		
		
class BPM_Phase_Offset:
	def __init__(self,bpm_wrapper):
		self.bpm_wrapper = bpm_wrapper
		self.phaseOffset_avg = 0.
		self.phaseOffset_err = 0.
		self.isReady = false
		#--- the base bpm with zero phase offset 
		self.base_bpm_wrapper = null
		#--- phase offset array for the statistical calculations
		self.phaseOffset_arr = []
		self.phase_spread = 25.0
		#----- temp values for convenience 
		self.phase_val_tmp = 0.
		self.phase_val_err_tmp = 0.
		
	def setBaseBPM_Wrapper(self,base_bpm_wrapper):
		self.base_bpm_wrapper = base_bpm_wrapper
	
	def getBaseBPM_Wrapper(self):
		return self.base_bpm_wrapper
			
	def setReady(self,isReady = false):
		self.isReady = isReady
		
	def clean(self):
		self.isReady = false
		self.phaseOffset_avg = 0.
		self.phaseOffset_err = 0.
		self.phaseOffset_arr = []

	def calculateFromStatistics(self):
		# ?????? there should be statistical calculations of the phase offset
		pass
	
	def writeDataToXML(self,root_da, name):
		offset_da = root_da.createChild(name)
		offset_da.setValue("isReady",self.isReady)
		offset_da.setValue("phaseOffset_avg",self.phaseOffset_avg)
		offset_da.setValue("phaseOffset_err",self.phaseOffset_err)
		if(self.base_bpm_wrapper != null):
			offset_da.setValue("base_bpm_alias",self.base_bpm_wrapper.alias)
		else:
			offset_da.setValue("base_bpm_alias","null")
			
	def readDataFromXML(self,offset_da,scl_long_tuneup_controller):
		self.isReady = Boolean(offset_da.intValue("isReady")).booleanValue()
		self.phaseOffset_avg = offset_da.doubleValue("phaseOffset_avg")
		self.phaseOffset_err = offset_da.doubleValue("phaseOffset_err")
		self.base_bpm_wrapper = scl_long_tuneup_controller.getBPM_Wrapper(offset_da.stringValue("base_bpm_alias"))
		
class BPM_Wrapper:
	def __init__(self,bpm):
		self.bpm = bpm
		st = "SCL:"
		if(bpm.getId().find("HEBT") >= 0): st = "HEBT:"
		self.alias = st+bpm.getId().split(":")[1]
		self.isGood = true
		self.pos = 0.
		self.sigmaPhase = 0.
		self.sigmaAmp = 0.
		self.phaseAvg = 0.
		self.ampAvg = 0.
		#--- phase Offsets parameters
		#--- left - for the start from CCL1
		#--- right - for the start from HEBT1 and ring
		#--- final - will be used in analysis
		self.left_phase_offset = BPM_Phase_Offset(self)
		self.right_phase_offset = BPM_Phase_Offset(self)
		self.final_phase_offset = BPM_Phase_Offset(self)
		#---- BPM timing shift data 
		self.bpm_timing_bucket = BPM_Timing_Bucket(self)
		
	def setPosition(self,accSeq):
		bpm = self.bpm
		self.pos = accSeq.getPosition(bpm) + 0.5*bpm.getBPMBucket().getLength()*bpm.getBPMBucket().getOrientation() 
		
	def getPosition(self):
		return self.pos
		
	def getBPM(self):
		return self.bpm
		
	def clean(self):
		self.sigmaPhase = 0.
		self.sigmaAmp = 0.
		self.phaseAvg = 0.
		self.ampAvg = 0.
		self.left_phase_offset.clean()
		self.right_phase_offset.clean()
		self.final_phase_offset.clean()
		
	def writeDataToXML(self,root_da):
		bpm_wrapper_da = root_da.createChild(self.bpm.getId())
		bpm_wrapper_da.setValue("alias",self.alias)
		bpm_wrapper_da.setValue("isGood", self.isGood)
		bpm_wrapper_da.setValue("pos", self.pos)			
		bpm_wrapper_da.setValue("sigmaPhase",self.sigmaPhase)			
		bpm_wrapper_da.setValue("sigmaAmp",self.sigmaAmp)			
		bpm_wrapper_da.setValue("phaseAvg",self.phaseAvg)			
		bpm_wrapper_da.setValue("ampAvg",self.ampAvg)			
		self.left_phase_offset.writeDataToXML(bpm_wrapper_da,"left_phase_offset")
		self.right_phase_offset.writeDataToXML(bpm_wrapper_da,"right_phase_offset")
		self.final_phase_offset.writeDataToXML(bpm_wrapper_da,"final_phase_offset")
		self.bpm_timing_bucket.writeDataToXML(bpm_wrapper_da)

	def readDataFromXML(self,root_da,scl_long_tuneup_controller):
		self.isGood = Boolean(root_da.intValue("isGood")).booleanValue()
		self.sigmaPhase = root_da.doubleValue("sigmaPhase")
		self.sigmaAmp = root_da.doubleValue("sigmaAmp")
		self.phaseAvg = root_da.doubleValue("phaseAvg")
		self.ampAvg = root_da.doubleValue("ampAvg")
		self.left_phase_offset.readDataFromXML(root_da.childAdaptor("left_phase_offset"),scl_long_tuneup_controller)
		self.right_phase_offset.readDataFromXML(root_da.childAdaptor("right_phase_offset"),scl_long_tuneup_controller)
		self.final_phase_offset.readDataFromXML(root_da.childAdaptor("final_phase_offset"),scl_long_tuneup_controller)
		self.bpm_timing_bucket.readDataFromXML(root_da)

class BPM_Timing_Bucket:
	def __init__(self,bpm_wrapper):
		"""
		PV name = SCL_Diag:BPM??:OEDA
		OEDA stands for Off Energy Delay Adjustment.
		"""
		self.bpm_wrapper = bpm_wrapper
		self.pv_oeda = ChannelFactory.defaultFactory().getChannel(self.bpm_wrapper.bpm.getId()+":OEDA")
		#---- production time shift is defined by time shift scan for production setup
		self.production_time_shift = 0.
		#---- memorized production time shift used to restore prodiction time shift if it is not 0
		self.memorized_time_shift = 0.
		#---- tuning time shift is defined by online model run with some cavities blanked
		self.tuning_time_shift = 0.
		#---- test timing shift for testing and debugging. No need to save/restore
		self.test_time_shift = 0.
		#---- graph data -------------------------
		self.amp_timing_graph_data = BasicGraphData()
		self.amp_timing_graph_data.setDrawPointsOn(true)
		self.amp_timing_graph_data.setGraphColor(Color.BLUE)
		self.amp_timing_graph_data.setLineThick(3)
		self.amp_timing_graph_data.setGraphPointSize(5)
		self.amp_timing_graph_data.setGraphProperty(GRAPH_LEGEND_KEY,bpm_wrapper.alias)
		#----
		self.phase_timing_graph_data = BasicGraphData()
		self.phase_timing_graph_data.setDrawPointsOn(true)
		self.phase_timing_graph_data.setGraphColor(Color.BLUE)
		self.phase_timing_graph_data.setLineThick(3)
		self.phase_timing_graph_data.setGraphPointSize(5)
		self.phase_timing_graph_data.setGraphProperty(GRAPH_LEGEND_KEY,bpm_wrapper.alias)
		
	def clean(self):
		self.amp_timing_graph_data.removeAllPoints()
		self.phase_timing_graph_data.removeAllPoints()                 
		                   
	def getGraphDataArr(self):
		return [self.amp_timing_graph_data,self.phase_timing_graph_data]
		
	def getTuningTimeShift(self):
		return self.tuning_time_shift
		
	def setTuningTimeShift(self,tuning_time_shift):
		self.tuning_time_shift = tuning_time_shift
		
	def getTestTimeShift(self):
		return self.test_time_shift
		
	def setTestTimeShift(self,tuning_time_shift):
		self.test_time_shift = tuning_time_shift		
		
	def getProductionTimeShift(self):
		return self.production_time_shift

	def updateProductionTimeShift(self):
		self.production_time_shift = 0.
		if(self.pv_oeda.connectAndWait(2.0)):
			self.production_time_shift = self.pv_oeda.getValDbl()*1.0e+6
			
	def externalUpdateProductionTimeShift(self, production_time_shift):
		"""
		This method should be used only if we know the self.pv_oeda.getValDbl() 
		value already from extrenal PV reading. 
		"""
		self.production_time_shift = production_time_shift

	def setProductionTimeShift(self,production_time_shift):
		if(self.pv_oeda.connectAndWait(2.0)):
			self.pv_oeda.putVal(production_time_shift/1.0e+6)
		self.production_time_shift = production_time_shift
		
	def getMemorizedTimeShift(self):
		return self.memorized_time_shift
		
	def setMemorizedTimeShift(self,memorized_time_shift):
		self.memorized_time_shift = memorized_time_shift

	def writeDataToXML(self,root_da):
		#------ write data for BPM timing shift to the XML stucture
		timing_wrapper_da = root_da.createChild("BPM_Timing_Shift")
		timing_wrapper_da.setValue("BPM",self.bpm_wrapper.alias)
		timing_wrapper_da.setValue("prod_time_shift",self.production_time_shift)
		timing_wrapper_da.setValue("memorized_time_shift",self.memorized_time_shift)		
		timing_wrapper_da.setValue("tuning_time_shift",self.tuning_time_shift)
		scan_data_da = timing_wrapper_da.createChild("timing_shift_scan_data")
		dumpGraphDataToDA(self.amp_timing_graph_data,scan_data_da,"amp","%8.6f","%8.6f")
		dumpGraphDataToDA(self.phase_timing_graph_data,scan_data_da,"phase","%8.6f","%10.3g")
		
	def readDataFromXML(self,root_da):		
		#------ read data for BPM timing shift from the XML stucture
		timing_wrapper_da = root_da.childAdaptor("BPM_Timing_Shift")
		if(timing_wrapper_da == null): return
		self.production_time_shift = timing_wrapper_da.doubleValue("prod_time_shift")
		self.tuning_time_shift = timing_wrapper_da.doubleValue("tuning_time_shift")
		if(timing_wrapper_da.hasAttribute("memorized_time_shift")):
			self.memorized_time_shift = timing_wrapper_da.doubleValue("memorized_time_shift")
		else:
			self.memorized_time_shift = 0.
		scan_data_da = timing_wrapper_da.childAdaptor("timing_shift_scan_data")
		if(scan_data_da == null): return
		readGraphDataFromDA(self.amp_timing_graph_data,scan_data_da,"amp")
		readGraphDataFromDA(self.phase_timing_graph_data,scan_data_da,"phase")

class Long_Twiss_Bucket:
	def __init__(self,cav_wrapper):
		self.cav_wrapper = cav_wrapper
		self.twiss_arr = [Twiss(),Twiss(),Twiss()]
		self.long_Twiss_fit = Twiss()
		self.long_Twiss_matrix = Twiss()
		self.long_Twiss_z2_zzp_zp2_err = [0.,0.,0]
		#---- alpha, beta, and emittance errors 
		self.long_Twiss_arr_err = [0.,0.,0]
		#---- alpha, beta, and emittance steps
		self.long_Twiss_arr_steps = [0.,0.,0]
		self.fit_bpm_amp_avg_err = 0.
		self.isReady = false
		self.update()

	def update(self):
		self.bpm_amp_plotTh_arr = []
		for bpm_wrapper in self.cav_wrapper.bpm_wrappers:
			amp_plotTh = BasicGraphData()
			amp_plotTh.setDrawPointsOn(false)
			amp_plotTh.setGraphColor(Color.RED)
			amp_plotTh.setLineThick(3)
			amp_plotTh.setGraphProperty(GRAPH_LEGEND_KEY,"Cav: "+self.cav_wrapper.alias+" BPM: "+bpm_wrapper.alias)
			self.bpm_amp_plotTh_arr.append([bpm_wrapper,amp_plotTh])			

	def clean(self):
		self.isReady = false
		self.long_Twiss_z2_zzp_zp2_err = [0.,0.,0]
		self.long_Twiss_arr_err = [0.,0.,0]	
		self.long_Twiss_arr_steps = [0.,0.,0]
		self.fit_bpm_amp_avg_err = 0.
		for [bpm_wrapper,amp_plotTh] in self.bpm_amp_plotTh_arr:
			amp_plotTh.removeAllPoints()
		self.twiss_arr = [Twiss(),Twiss(),Twiss()]
		self.long_Twiss_fit = Twiss()
		self.long_Twiss_matrix = Twiss()	
		
	def cleanFittingValues(self):
		self.long_Twiss_z2_zzp_zp2_err = [0.,0.,0]
		self.long_Twiss_arr_err = [0.,0.,0]	
		self.fit_bpm_amp_avg_err = 0.
		for [bpm_wrapper,amp_plotTh] in self.bpm_amp_plotTh_arr:
			amp_plotTh.removeAllPoints()
		self.long_Twiss_fit = Twiss()
		self.long_Twiss_matrix = Twiss()			
		
	def writeDataToXML(self,root_da):
		cav_long_twiss_bucket_da = root_da.createChild("Cavity_Long_Twiss_Bucket")
		cav_long_twiss_bucket_da.setValue("cav",self.cav_wrapper.alias)
		cav_long_twiss_bucket_da.setValue("isReady",self.isReady)
		cav_long_twiss_bucket_da.setValue("amp_avg_fit_err",self.fit_bpm_amp_avg_err)			
		twiss_arr_da = cav_long_twiss_bucket_da.createChild("Twiss_arr")
		twiss_arr = self.twiss_arr	
		[alphaX,betaX,emittX] = [twiss_arr[0].getAlpha(),twiss_arr[0].getBeta(),twiss_arr[0].getEmittance()]
		[alphaY,betaY,emittY] = [twiss_arr[1].getAlpha(),twiss_arr[1].getBeta(),twiss_arr[1].getEmittance()]
		[alphaZ,betaZ,emittZ] = [twiss_arr[2].getAlpha(),twiss_arr[2].getBeta(),twiss_arr[2].getEmittance()]
		twiss_arr_da.setValue("alphaX",alphaX)
		twiss_arr_da.setValue("betaX",betaX)
		twiss_arr_da.setValue("emittX",emittX)
		twiss_arr_da.setValue("alphaY",alphaY)
		twiss_arr_da.setValue("betaY",betaY)
		twiss_arr_da.setValue("emittY",emittY)
		twiss_arr_da.setValue("alphaZ",alphaZ)
		twiss_arr_da.setValue("betaZ",betaZ)
		twiss_arr_da.setValue("emittZ",emittZ)
		twiss_arr_da.setValue("alphaZ_err",self.long_Twiss_arr_err[0])
		twiss_arr_da.setValue("betaZ_err",self.long_Twiss_arr_err[1])
		twiss_arr_da.setValue("emittZ_err",self.long_Twiss_arr_err[2])	
		twiss_arr_da.setValue("alphaZ_steps",self.long_Twiss_arr_steps[0])
		twiss_arr_da.setValue("betaZ_steps",self.long_Twiss_arr_steps[1])
		twiss_arr_da.setValue("emittZ_steps",self.long_Twiss_arr_steps[2])			
		twiss_fit_da = cav_long_twiss_bucket_da.createChild("Twiss_fit")
		twiss_fit_da.setValue("alphaZ",self.long_Twiss_fit.getAlpha())			
		twiss_fit_da.setValue("betaZ",self.long_Twiss_fit.getBeta())			
		twiss_fit_da.setValue("emittZ",self.long_Twiss_fit.getEmittance())			
		twiss_matrix_da = cav_long_twiss_bucket_da.createChild("Twiss_matrix")
		twiss_matrix_da.setValue("alphaZ",self.long_Twiss_matrix.getAlpha())			
		twiss_matrix_da.setValue("betaZ",self.long_Twiss_matrix.getBeta())			
		twiss_matrix_da.setValue("emittZ",self.long_Twiss_matrix.getEmittance())
		twiss_matrix_da.setValue("z2_err",self.long_Twiss_z2_zzp_zp2_err[0])
		twiss_matrix_da.setValue("z_zp_err",self.long_Twiss_z2_zzp_zp2_err[1])
		twiss_matrix_da.setValue("z2_err",self.long_Twiss_z2_zzp_zp2_err[2])
		bpm_amps_da = cav_long_twiss_bucket_da.createChild("BPM_Amplitudes")
		for [bpm_wrapper,amp_plotTh] in self.bpm_amp_plotTh_arr:
			if(amp_plotTh.getNumbOfPoints() > 0):
				bpm_amp_da = bpm_amps_da.createChild("BPM")
				bpm_amp_da.setValue("BPM",bpm_wrapper.alias)
				dumpGraphDataToDA(amp_plotTh,bpm_amp_da,"BPM_AMPL_GD","%10.5f","%10.5f")
	
	def readDataFromXML(self,cav_long_twiss_bucket_da,scl_long_tuneup_controller):		
		#------ read data for cavity from the XML stucture
		self.isReady = Boolean(cav_long_twiss_bucket_da.intValue("isReady")).booleanValue()
		self.fit_bpm_amp_avg_err = cav_long_twiss_bucket_da.doubleValue("amp_avg_fit_err")
		twiss_arr_da = cav_long_twiss_bucket_da.childAdaptor("Twiss_arr")
		alphaX = twiss_arr_da.doubleValue("alphaX")
		betaX = twiss_arr_da.doubleValue("betaX")
		emittX = twiss_arr_da.doubleValue("emittX")
		alphaY = twiss_arr_da.doubleValue("alphaY")
		betaY = twiss_arr_da.doubleValue("betaY")
		emittY = twiss_arr_da.doubleValue("emittY")		
		alphaZ = twiss_arr_da.doubleValue("alphaZ")
		betaZ = twiss_arr_da.doubleValue("betaZ")
		emittZ = twiss_arr_da.doubleValue("emittZ")			
		self.twiss_arr[0].setTwiss(alphaX,betaX,emittX)
		self.twiss_arr[1].setTwiss(alphaY,betaY,emittY)
		self.twiss_arr[2].setTwiss(alphaZ,betaZ,emittZ)
		self.long_Twiss_arr_err[0] = twiss_arr_da.doubleValue("alphaZ_err")
		self.long_Twiss_arr_err[1] = twiss_arr_da.doubleValue("betaZ_err")
		self.long_Twiss_arr_err[2] = twiss_arr_da.doubleValue("emittZ_err")
		self.long_Twiss_arr_steps[0] = twiss_arr_da.doubleValue("alphaZ_steps")
		self.long_Twiss_arr_steps[1] = twiss_arr_da.doubleValue("betaZ_steps")
		self.long_Twiss_arr_steps[2] = twiss_arr_da.doubleValue("emittZ_steps")
		twiss_fit_da = cav_long_twiss_bucket_da.childAdaptor("Twiss_fit")
		alphaZ = twiss_fit_da.doubleValue("alphaZ")
		betaZ = twiss_fit_da.doubleValue("betaZ")
		emittZ = twiss_fit_da.doubleValue("emittZ")
		self.long_Twiss_fit.setTwiss(alphaZ,betaZ,emittZ)
		twiss_matrix_da = cav_long_twiss_bucket_da.childAdaptor("Twiss_matrix")
		alphaZ = twiss_matrix_da.doubleValue("alphaZ")
		betaZ = twiss_matrix_da.doubleValue("betaZ")
		emittZ = twiss_matrix_da.doubleValue("emittZ")
		self.long_Twiss_matrix.setTwiss(alphaZ,betaZ,emittZ)
		self.long_Twiss_z2_zzp_zp2_err[0] =twiss_matrix_da.doubleValue("z2_err")
		self.long_Twiss_z2_zzp_zp2_err[1] =twiss_matrix_da.doubleValue("z_zp_err")
		self.long_Twiss_z2_zzp_zp2_err[2] =twiss_matrix_da.doubleValue("z2_err")
		#---- clean up and read the amp graphs
		self.update()
		bpm_amps_da = cav_long_twiss_bucket_da.childAdaptor("BPM_Amplitudes")
		for bpm_amp_da in bpm_amps_da.childAdaptors("BPM"):
			bpm_wrapper = scl_long_tuneup_controller.getBPM_Wrapper(bpm_amp_da.stringValue("BPM"))
			for [bpm_wrapper_tmp,amp_plotTh] in self.bpm_amp_plotTh_arr:
				if(bpm_wrapper_tmp == bpm_wrapper):
					readGraphDataFromDA(amp_plotTh,bpm_amp_da,"BPM_AMPL_GD")
					break

class BlankBeamCavity_Bucket:
	def __init__(self,cav_wrapper):
		self.cav_wrapper = cav_wrapper
		self.blank_beam_state = false
		
	def update(self):
		self.blank_beam_state = self.cav_wrapper.getBlankBeam()
		
	def getBlankBeamState(self):
		return self.blank_beam_state
		
	def setBlankBeamState(self, blank_beam_state):
		self.blank_beam_state = blank_beam_state
		
	def writeDataToXML(self,root_da):
		"""
		At this moment we do not need the saved values for blanking.
		"""
		cav_long_twiss_bucket_da = root_da.createChild("Cavity_Blank_Beam_State_Bucket")
		cav_long_twiss_bucket_da.setValue("cav",self.cav_wrapper.alias)
		cav_long_twiss_bucket_da.setValue("balnk_state",self.blank_beam_state)

	def readDataFromXML(self,root_da):
		"""
		At this moment we do not need to read the saved values for blanking.
		"""		
		cav_blank_beam_state_bucket_da = root_da.childAdaptor("Cavity_Blank_Beam_State_Bucket")
		if(cav_blank_beam_state_bucket_da == null): return
		#------ read data for cavity from the XML stucture
		self.blank_beam_state = Boolean(cav_blank_beam_state_bucket_da.intValue("balnk_state")).booleanValue()


class SCL_Cavity_Wrapper:
	def __init__(self,cav):
		#self.bpm_ch_amp_phase_dic[BPM_Wrapper] = (graphDataAmp,graphDataPhase)
		self.cav = cav
		self.alias = cav.getId().split(":")[1]		
		self.isGood = true
		self.isMeasured = false
		self.isAnalyzed = false
		self.pos = 0.
		#---- bpm_amp_phase_dict and bpm_xy_dict dictionary with BasicGraph data
		self.bpm_amp_phase_dict = {}
		self.bpm_xy_dict = {}
		self.bpm_wrappers = []
		#--- use or not in phase scan analysis
		self.bpm_wrappers_useInPhaseAnalysis = []
		#--- use or not in BPMs' amplitudes analysis
		self.bpm_wrappers_useInAmpBPMs = []
		#--- BPM wrappers for BPM0 and BPM1 during cavity phase setup after the phase scan
		self.bpm_wrapper0 = null
		self.bpm_wrapper1 = null
		self.phaseDiffPlot = BasicGraphData()
		self.phaseDiffPlot.setGraphPointSize(7)
		self.phaseDiffPlot.setGraphColor(Color.BLUE)
		self.phaseDiffPlotTh = BasicGraphData()
		self.phaseDiffPlotTh.setDrawPointsOn(false)
		self.phaseDiffPlotTh.setGraphColor(Color.RED)
		self.phaseDiffPlotTh.setLineThick(3)
		#----cavity's parameters 
		self.initDesignAmp = 0.
		self.initDesignPhase = 0.
		#-- design parameters will be defined after analysis of the phase scan data
		self.designAmp = 0.
		self.designPhase = 0.
		self.avg_gap_phase = 0.  # this is a model parameter that will be used in rescaling 
		#--- initial live parameters are measured after initialization 
		self.initLiveAmp = 0.
		self.initLivePhase = 0.
		#live phase will be defined after scan
		self.livePhase = 0.
		self.scanPhaseShift = -18.0
		self.real_scanPhaseShift = 0.
		#--- avg. phase error and harmonic amp after harmonics fitting during phase scan
		self.phase_scan_harm_err = 0.
		self.phase_scan_harm_amp = 0.
		self.phase_scan_harm_funcion = HramonicsFunc([0.,])
		self.energy_guess_harm_funcion = HramonicsFunc([0.,])
		self.eKinOutPlot = BasicGraphData()
		self.eKinOutPlot.setGraphPointSize(7)
		self.eKinOutPlot.setGraphColor(Color.BLUE)
		self.eKinOutPlotTh = BasicGraphData()
		self.eKinOutPlotTh.setDrawPointsOn(false)
		self.eKinOutPlotTh.setGraphColor(Color.RED)
		self.eKinOutPlotTh.setLineThick(3)	
		self.eKinOutPlot.setGraphProperty(GRAPH_LEGEND_KEY," Ekin Out "+self.alias)		
		self.eKinOutPlotTh.setGraphProperty(GRAPH_LEGEND_KEY," Ekin Out Fit "+self.alias)		
		#--- energy params
		self.eKin_in_guess = 0. # is used for CCL4 forward analysis
		self.eKin_out_guess = 0. # is used for CCL4 forward analysis		
		self.eKin_in = 0.
		self.eKin_out = 0.
		self.eKin_err = 0.
		self.bpm_eKin_out = 0.
		self.model_eKin_out = 0.
		#------- the rescale data bucket
		self.rescaleBacket = CavityRescaleBucket(self)
		#------- the longitudinal Twiss parameters bucket
		self.longTwissBucket = Long_Twiss_Bucket(self)
		#------- Blank beam state bucket
		self.blankBeamBacket = BlankBeamCavity_Bucket(self)
		#------- LLRF Buffer resettings PVs
		self.ch_aff_mode = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+self.alias.replace("Cav","")+":AFF_Mode")
		self.ch_aff_reset = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+self.alias.replace("Cav","")+":AFF_Reset")
		#------- Amplitude set goal value PV
		self.ampl_goal_pv = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:FCM"+self.alias.replace("Cav","")+":cavAmpGoal")
		#print "debug conncted cav=",self.alias
		#------- bypass state for the cavity: 0 means bypass is off= NORMAL; 1 means bypass is on = MASKED
		self.bypass_pv = ChannelFactory.defaultFactory().getChannel("SCL_LLRF:HPM"+self.alias.replace("Cav","")+":FPAR_LDmp_swmask_set")
		
	def getBypassState(self):
		if(self.bypass_pv.connectAndWait(3.0)):
			bypass_state = int(self.bypass_pv.getValInt())
			return bypass_state
		return None
		
	def setBypassState(self,bypass_state):
		if(self.bypass_pv.connectAndWait(3.0)):
			self.bypass_pv.putVal(bypass_state)	
		
	def getLiveAmlitudeSetGoal(self):
		if(self.ampl_goal_pv.connectAndWait(3.0)):
			return self.ampl_goal_pv.getValDbl()
		return 0.
		
	def setAFF_Off(self):
		if(self.ch_aff_mode.connectAndWait(0.1)):
			self.ch_aff_mode.putVal(0)
	
	def setAFF_On(self):
		if(self.ch_aff_mode.connectAndWait(0.1)):
			self.ch_aff_mode.putVal(2)
			
	def resetAFF(self):
		if(self.ch_aff_reset.connectAndWait(0.1)):
			self.ch_aff_reset.putVal(1)
		
	def setBlankBeam(self,bool_val):
		self.cav.setBlankBeam(bool_val)
		
	def getBlankBeam(self):
		return self.cav.getBlankBeam()	
		
	def setUpBPM_Wrappers(self,bpm_wrappers,scl_long_tuneup_controller):
		self.bpm_amp_phase_dict = {}
		self.bpm_xy_dict = {}
		self.bpm_wrappers = []
		self.bpm_wrappers_useInPhaseAnalysis = []
		self.bpm_wrappers_useInAmpBPMs = []
		for bpm_wrapper in bpm_wrappers:
			#we will use all bpm_wrappers even if they are before the cavity
			#if(bpm_wrapper.getPosition() > self.getPosition() and bpm_wrapper.isGood):
			if(bpm_wrapper.isGood):
				self.bpm_wrappers.append(bpm_wrapper)
				self.bpm_wrappers_useInPhaseAnalysis.append(true)
				self.bpm_wrappers_useInAmpBPMs.append(true)
				(graphDataAmp,graphDataPhase) = (BasicGraphData(),BasicGraphData())
				graphDataAmp.setGraphPointSize(5)
				graphDataPhase.setGraphPointSize(5)
				graphDataAmp.setGraphColor(Color.BLUE)
				graphDataPhase.setGraphColor(Color.BLUE)
				graphDataPhase.setGraphProperty(GRAPH_LEGEND_KEY," Phase "+bpm_wrapper.alias)
				graphDataAmp.setGraphProperty(GRAPH_LEGEND_KEY," Amp "+bpm_wrapper.alias)
				self.bpm_amp_phase_dict[bpm_wrapper] = (graphDataAmp,graphDataPhase)
				(graphDataX,graphDataY) = (BasicGraphData(),BasicGraphData())
				self.bpm_xy_dict[bpm_wrapper] = (graphDataX,graphDataY)
		# HEBT2 BPMs should not be used in the phase and amp analysis (pending the XAL Online Model fix)
		for ind in range(len(self.bpm_wrappers)):
			bpm_wrapper = self.bpm_wrappers[ind]
			#---- HEBT2 should be excluded - usually energy is wrong for beam transport to HEBT2
			if(bpm_wrapper.pos > 280.):
				self.bpm_wrappers_useInPhaseAnalysis[ind] = false
				self.bpm_wrappers_useInAmpBPMs[ind] = false
		# longitudinal twiss bucket update
		self.longTwissBucket.update()
 
 	def removeBPM_Wrapper(self,bpm_wrapper):
 		"""
 		It will remove one BPM wrapper for the cavity.
 		"""
 		bpm_index = self.bpm_wrappers.index(bpm_wrapper)
 		if(bpm_index < 0):
 			return
 		del self.bpm_wrappers[bpm_index]
 		del self.bpm_wrappers_useInPhaseAnalysis[bpm_index]
 		del self.bpm_wrappers_useInAmpBPMs[bpm_index]
 		del self.bpm_amp_phase_dict[bpm_wrapper]
 		del self.bpm_xy_dict[bpm_wrapper]
 		self.longTwissBucket.update()
 
	def setBPM_0_1_Wrappers(self,bpm_wrapper0,bpm_wrapper1):
		self.phaseDiffPlot.removeAllPoints()
		self.phaseDiffPlotTh.removeAllPoints()		
		self.bpm_wrapper0 = null
		self.bpm_wrapper1 = null	
		if(self.bpm_amp_phase_dict.has_key(bpm_wrapper0)):
			self.bpm_wrapper0 = bpm_wrapper0
		if(self.bpm_amp_phase_dict.has_key(bpm_wrapper1)):
			self.bpm_wrapper1 = bpm_wrapper1
		self.phaseDiffPlot.removeAllPoints()
		legendKey = GRAPH_LEGEND_KEY
		txt = self.alias + " Phase diff "
		self.phaseDiffPlotTh.setGraphProperty(legendKey,txt + "Fit")
		if(self.bpm_wrapper0 != null and self.bpm_wrapper1 != null):
			self.phaseDiffPlot.setGraphProperty(legendKey,txt+self.bpm_wrapper0.alias+" "+self.bpm_wrapper1.alias)
		else:
			self.phaseDiffPlot.setGraphProperty(legendKey,txt)
			return
		#---- we will update phase diff plot and analysis if the date are not empty
		self.updateDiffPlotsAndAnalysis()

	def updateDiffPlotsAndAnalysis(self):
		"""
		This method will update the diff plot after the user assigned 
		new self.bpm_wrapper0 and self.bpm_wrapper1 to the cavity wrapper.
		"""
		self.phaseDiffPlot.removeAllPoints()
		self.phaseDiffPlotTh.removeAllPoints()
		(graphDataAmp0,graphDataPhase0) = self.bpm_amp_phase_dict[self.bpm_wrapper0]
		(graphDataAmp1,graphDataPhase1) = self.bpm_amp_phase_dict[self.bpm_wrapper1]
		if(graphDataPhase0.getNumbOfPoints() != graphDataPhase1.getNumbOfPoints()): return
		#---- we need at least 8 points for analysis
		if(graphDataPhase0.getNumbOfPoints() < 8): return
		self.isAnalyzed = false	
		self.phase_scan_harm_err = 0.
		self.phase_scan_harm_amp = 0.
		self.phase_scan_harm_funcion.setParamArr([0.,])
		#--------------------------------------------
		for ind in range(graphDataPhase0.getNumbOfPoints()):
			cav_phase = graphDataPhase0.getX(ind)
			y0 = graphDataPhase0.getY(ind)
			y1 = graphDataPhase1.getY(ind)
			self.phaseDiffPlot.addPoint(cav_phase,y1-y0)
		#---- analysis
		harmonicsAnalyzer = HarmonicsAnalyzer(2)
		err = harmonicsAnalyzer.analyzeData(self.phaseDiffPlot)	
		harm_function = harmonicsAnalyzer.getHrmonicsFunction()
		min_phase = harmonicsAnalyzer.getPositionOfMin()
		new_phase = makePhaseNear(min_phase + self.scanPhaseShift,0.)
		self.livePhase = new_phase
		self.phase_scan_harm_amp = harm_function.getParamArr()[1]
		self.phase_scan_harm_err = err
		self.phase_scan_harm_funcion.setParamArr(harmonicsAnalyzer.getHrmonicsFunction().getParamArr())
		#----make theory graph plot
		x_arr = []
		y_arr = []
		for i in range(73):
		 phase = -180.0 + 5.0*i
		 y = harm_function.getValue(phase)
		 x_arr.append(phase)
		 y_arr.append(y)
		self.phaseDiffPlotTh.addPoint(x_arr,y_arr)	

	def clean(self):
		for bpm_wrapper in self.bpm_wrappers:
			(graphDataAmp,graphDataPhase) = self.bpm_amp_phase_dict[bpm_wrapper]
			graphDataAmp.removeAllPoints()
			graphDataPhase.removeAllPoints()
			(graphDataX,graphDataY) = self.bpm_xy_dict[bpm_wrapper]
			graphDataX.removeAllPoints()
			graphDataY.removeAllPoints()
		self.livePhase = 0.
		self.isMeasured = false
		self.isAnalyzed = false	
		self.phase_scan_harm_err = 0.
		self.phase_scan_harm_amp = 0.
		self.avg_gap_phase = 0.
		self.eKin_err = 0.		
		self.model_eKin_out = 0.
		self.bpm_eKin_out = 0.
		self.real_scanPhaseShift = 0.		
		self.phase_scan_harm_funcion.setParamArr([0.,])
		self.phaseDiffPlot.removeAllPoints()
		self.phaseDiffPlotTh.removeAllPoints()
		self.eKinOutPlot.removeAllPoints()
		self.eKinOutPlotTh.removeAllPoints()
		#---clean the rescale data
		self.rescaleBacket.clean()
		#---clean longitudinal Twiss data
		self.longTwissBucket.clean()
		
	def addScanPointToPhaseDiffData(self):
		if(self.bpm_wrapper0 == null or self.bpm_wrapper1 == null): return
		(graphDataAmp0,graphDataPhase0) = self.bpm_amp_phase_dict[self.bpm_wrapper0]
		(graphDataAmp1,graphDataPhase1) = self.bpm_amp_phase_dict[self.bpm_wrapper1]
		if(graphDataPhase0.getNumbOfPoints() != graphDataPhase1.getNumbOfPoints()): return
		n_points = graphDataPhase0.getNumbOfPoints()
		ind = n_points-1
		x = graphDataPhase0.getX(ind)
		y = graphDataPhase1.getY(ind) - graphDataPhase0.getY(ind)
		self.phaseDiffPlot.addPoint(x,y)
		
	def recalculatePhaseDiffData(self):
		if(self.bpm_wrapper0 == null or self.bpm_wrapper1 == null): return
		(graphDataAmp0,graphDataPhase0) = self.bpm_amp_phase_dict[self.bpm_wrapper0]
		(graphDataAmp1,graphDataPhase1) = self.bpm_amp_phase_dict[self.bpm_wrapper1]
		if(graphDataPhase0.getNumbOfPoints() != graphDataPhase1.getNumbOfPoints()): return
		x_arr = []
		y_arr = []
		for ind in range(graphDataPhase0.getNumbOfPoints()):
			x_arr.append(graphDataPhase0.getX(ind))
			y_arr.append(graphDataPhase1.getY(ind) - graphDataPhase0.getY(ind))
		self.phaseDiffPlot.removeAllPoints()
		self.phaseDiffPlot.addPoint(x_arr,y_arr)
		
	def getAmpPhaseGraphs(self,bpm_wrapper):
		if(self.bpm_amp_phase_dict.has_key(bpm_wrapper)):
			(graphDataAmp,graphDataPhase) = self.bpm_amp_phase_dict[bpm_wrapper]
			return (graphDataAmp,graphDataPhase)
		return (null,null)

	def getXYGraphs(self,bpm_wrapper):
		if(self.bpm_xy_dict.has_key(bpm_wrapper)):
			(graphDataX,graphDataY) = self.bpm_xy_dict[bpm_wrapper]
			return (graphDataX,graphDataY)
		return (null,null)

	def setPosition(self,accSeq):
		self.pos = accSeq.getPosition(self.cav)
		
	def getPosition(self):
		return self.pos

	def writeDataToXML(self,root_da):
		cav_wrapper_da = root_da.createChild(self.alias)
		cav_wrapper_da.setValue("cav",self.cav.getId())
		bpm0_name = "null"
		bpm1_name = "null"
		if(self.bpm_wrapper0 != null): bpm0_name = self.bpm_wrapper0.alias
		if(self.bpm_wrapper1 != null): bpm1_name = self.bpm_wrapper1.alias
		cav_wrapper_da.setValue("bpm0",bpm0_name)
		cav_wrapper_da.setValue("bpm1",bpm1_name)
		cav_wrapper_da.setValue("isGood",self.isGood )
		cav_wrapper_da.setValue("isMeasured",self.isMeasured)
		cav_wrapper_da.setValue("isAnalyzed",self.isAnalyzed)
		#--------------------------------
		cav_wrapper_params_da = cav_wrapper_da.createChild("Params")
		cav_wrapper_params_da.setValue("initDesignAmp",self.initDesignAmp)
		cav_wrapper_params_da.setValue("initDesignPhase",self.initDesignPhase)
		cav_wrapper_params_da.setValue("designAmp",self.designAmp)
		cav_wrapper_params_da.setValue("designPhase",self.designPhase)
		cav_wrapper_params_da.setValue("avg_gap_phase",self.avg_gap_phase)
		cav_wrapper_params_da.setValue("initLiveAmp",self.initLiveAmp)
		cav_wrapper_params_da.setValue("initLivePhase",self.initLivePhase)
		cav_wrapper_params_da.setValue("livePhase",self.livePhase)
		cav_wrapper_params_da.setValue("scanPhaseShift",self.scanPhaseShift)
		cav_wrapper_params_da.setValue("phase_scan_harm_err",self.phase_scan_harm_err)
		cav_wrapper_params_da.setValue("phase_scan_harm_amp",self.phase_scan_harm_amp)
		cav_wrapper_params_da.setValue("eKin_in_guess",self.eKin_in_guess)
		cav_wrapper_params_da.setValue("eKin_out_guess",self.eKin_out_guess)
		cav_wrapper_params_da.setValue("eKin_in",self.eKin_in)
		cav_wrapper_params_da.setValue("eKin_out",self.eKin_out)
		cav_wrapper_params_da.setValue("eKin_err",self.eKin_err)
		cav_wrapper_params_da.setValue("bpm_eKin_out",self.bpm_eKin_out)
		cav_wrapper_params_da.setValue("model_eKin_out",self.model_eKin_out)
		cav_wrapper_params_da.setValue("real_scanPhaseShift",self.real_scanPhaseShift)
		#--------- write the cavity rescale data
		self.rescaleBacket.writeDataToXML(cav_wrapper_da)
		#--------- write blank beam state - At this moment we do not need this
		#self.blankBeamBacket.writeDataToXML(cav_wrapper_da)
		#---------------------------------
		cav_wrapper_phase_harm_da = cav_wrapper_da.createChild("Phase_Harm_Func")
		cav_wrapper_phase_harm_da.setValue("params_arr",self.phase_scan_harm_funcion.getTxtParamArr())
		cav_wrapper_energy_harm_da = cav_wrapper_da.createChild("Eenergy_guess_Harm_Func")
		cav_wrapper_energy_harm_da.setValue("params_arr",self.energy_guess_harm_funcion.getTxtParamArr())
		#----------------------------------
		dumpGraphDataToDA(self.phaseDiffPlot,cav_wrapper_da,"Phase_Diff_GD","%8.3f","%8.3f")	
		dumpGraphDataToDA(self.phaseDiffPlotTh,cav_wrapper_da,"Phase_Diff_Fit_GD","%8.3f","%8.3f")	
		dumpGraphDataToDA(self.eKinOutPlot,cav_wrapper_da,"Ekin_Out_GD","%8.3f","%10.4f")	
		dumpGraphDataToDA(self.eKinOutPlotTh,cav_wrapper_da,"Ekin_Out_Fit_GD","%8.3f","%10.4f")	
		#------------------------------------
		cav_wrapper_bpm_list_da = cav_wrapper_da.createChild("bpm_arr")
		txt = ""
		for bpm_wrapper in self.bpm_wrappers:
			txt = txt +" "+ bpm_wrapper.alias
		cav_wrapper_bpm_list_da.setValue("bpm_wrappers",txt)
		cav_wrapper_bpm_list_da = cav_wrapper_da.createChild("bpm_use_in_phase_analysis_arr")
		txt = ""
		for bool_val in self.bpm_wrappers_useInPhaseAnalysis:
			val = "0"
			if(bool_val): val = "1"
			txt = txt +" "+ val
		cav_wrapper_bpm_list_da.setValue("use_arr",txt)
		cav_wrapper_bpm_list_da = cav_wrapper_da.createChild("bpm_use_in_amp_analysis_arr")
		txt = ""
		for bool_val in self.bpm_wrappers_useInAmpBPMs:
			val = "0"
			if(bool_val): val = "1"
			txt = txt +" "+ val
		cav_wrapper_bpm_list_da.setValue("use_arr",txt)
		#---------------------------------------------
		cav_wrapper_scan_data_da = cav_wrapper_da.createChild("scan_data")
		for bpm_wrapper in self.bpm_wrappers:
			if(self.bpm_amp_phase_dict.has_key(bpm_wrapper)):
				(graphDataAmp,graphDataPhase) = self.bpm_amp_phase_dict[bpm_wrapper]
				cav_wrapper_scan_data_bpm_da = cav_wrapper_scan_data_da.createChild(bpm_wrapper.alias)
				dumpGraphDataToDA(graphDataPhase,cav_wrapper_scan_data_bpm_da,"phase","%8.3f","%8.3f")
				dumpGraphDataToDA(graphDataAmp,cav_wrapper_scan_data_bpm_da,"amplitude","%8.3f","%10.3g")
				if(self.bpm_xy_dict.has_key(bpm_wrapper)):
					(graphDataX,graphDataY) = self.bpm_xy_dict[bpm_wrapper]
					dumpGraphDataToDA(graphDataX,cav_wrapper_scan_data_bpm_da,"posX","%8.3f","%8.3f")
					dumpGraphDataToDA(graphDataY,cav_wrapper_scan_data_bpm_da,"posY","%8.3f","%8.3f")
				
	def readDataFromXML(self,cav_wrapper_da,scl_long_tuneup_controller):
		#print "debug ============= cav_wrapper_da=",cav_wrapper_da.name()
		self.bpm_wrapper0 = scl_long_tuneup_controller.getBPM_Wrapper(cav_wrapper_da.stringValue("bpm0"))
		self.bpm_wrapper1 = scl_long_tuneup_controller.getBPM_Wrapper(cav_wrapper_da.stringValue("bpm1"))
		self.isGood = Boolean(cav_wrapper_da.intValue("isGood")).booleanValue()
		self.isMeasured = Boolean(cav_wrapper_da.intValue("isMeasured")).booleanValue()
		self.isAnalyzed = Boolean(cav_wrapper_da.intValue("isAnalyzed")).booleanValue()
		#--------- read parameters-------------------------
		cav_wrapper_params_da = cav_wrapper_da.childAdaptor("Params")	
		self.initDesignAmp = cav_wrapper_params_da.doubleValue("initDesignAmp")
		self.initDesignPhase = cav_wrapper_params_da.doubleValue("initDesignPhase")
		self.designAmp = cav_wrapper_params_da.doubleValue("designAmp")
		self.designPhase = cav_wrapper_params_da.doubleValue("designPhase")
		self.avg_gap_phase =	cav_wrapper_params_da.doubleValue("avg_gap_phase")
		self.initLiveAmp = cav_wrapper_params_da.doubleValue("initLiveAmp")
		self.initLivePhase = cav_wrapper_params_da.doubleValue("initLivePhase")
		self.livePhase = cav_wrapper_params_da.doubleValue("livePhase")
		self.scanPhaseShift = cav_wrapper_params_da.doubleValue("scanPhaseShift")
		self.phase_scan_harm_err = cav_wrapper_params_da.doubleValue("phase_scan_harm_err")
		self.phase_scan_harm_amp = cav_wrapper_params_da.doubleValue("phase_scan_harm_amp")
		self.eKin_in_guess = cav_wrapper_params_da.doubleValue("eKin_in_guess")
		self.eKin_out_guess  = cav_wrapper_params_da.doubleValue("eKin_out_guess")
		self.eKin_in = cav_wrapper_params_da.doubleValue("eKin_in")
		self.eKin_out = cav_wrapper_params_da.doubleValue("eKin_out")
		self.eKin_err = cav_wrapper_params_da.doubleValue("eKin_err")
		self.bpm_eKin_out  = cav_wrapper_params_da.doubleValue("bpm_eKin_out")
		self.model_eKin_out  = cav_wrapper_params_da.doubleValue("model_eKin_out")
		self.real_scanPhaseShift  = cav_wrapper_params_da.doubleValue("real_scanPhaseShift")
		#--------- read the cavity rescale data
		self.rescaleBacket.readDataFromXML(cav_wrapper_da)
		#--------- read saved blank beam state - At this moment we do not need this
		#self.blankBeamBacket.readDataFromXML(cav_wrapper_da)
		#--------- read harm. functions-------------------------
		cav_wrapper_phase_harm_da = cav_wrapper_da.childAdaptor("Phase_Harm_Func")
		self.phase_scan_harm_funcion.parsePramArr(cav_wrapper_phase_harm_da.stringValue("params_arr"))
		cav_wrapper_energy_harm_da = cav_wrapper_da.childAdaptor("Eenergy_guess_Harm_Func")
		self.energy_guess_harm_funcion.parsePramArr(cav_wrapper_energy_harm_da.stringValue("params_arr"))
		#--------- read phase Diff. graph data		
		readGraphDataFromDA(self.phaseDiffPlot,cav_wrapper_da,"Phase_Diff_GD")
		readGraphDataFromDA(self.phaseDiffPlotTh,cav_wrapper_da,"Phase_Diff_Fit_GD")
		readGraphDataFromDA(self.eKinOutPlot,cav_wrapper_da,"Ekin_Out_GD")
		readGraphDataFromDA(self.eKinOutPlotTh,cav_wrapper_da,"Ekin_Out_Fit_GD")		
		#--------- loop over bpm wrappers for this cavity
		cav_wrapper_bpm_list_da = cav_wrapper_da.childAdaptor("bpm_arr")
		bpm_wrapper_alias_arr = cav_wrapper_bpm_list_da.stringValue("bpm_wrappers").split()
		cav_wrapper_bpm_list_da = cav_wrapper_da.childAdaptor("bpm_use_in_phase_analysis_arr")
		use_in_phase_analysis_arr = cav_wrapper_bpm_list_da.stringValue("use_arr").split()
		cav_wrapper_bpm_list_da = cav_wrapper_da.childAdaptor("bpm_use_in_amp_analysis_arr")
		use_in_amp_analysis_arr = cav_wrapper_bpm_list_da.stringValue("use_arr").split()
		bpm_wrapper_arr = []
		for bpm_wrapper_alias in bpm_wrapper_alias_arr:
			bpm_wrapper = scl_long_tuneup_controller.getBPM_Wrapper(bpm_wrapper_alias)
			bpm_wrapper_arr.append(bpm_wrapper)
		bpm_wrappers = []
		use_in_phase_arr = []
		use_in_amp_arr = []
		for ind in range(len(bpm_wrapper_arr)):
			bpm_wrapper = bpm_wrapper_arr[ind]
			if(bpm_wrapper != null):
				bpm_wrappers.append(bpm_wrapper)
				use_in_phase_arr.append(use_in_phase_analysis_arr[ind])
				use_in_amp_arr.append(use_in_amp_analysis_arr[ind])
		use_in_phase_analysis_arr = use_in_phase_arr
		use_in_amp_analysis_arr = use_in_amp_arr
		self.setUpBPM_Wrappers(bpm_wrappers,scl_long_tuneup_controller)
		for ind in range(len(self.bpm_wrappers)):
			bpm_wrapper = self.bpm_wrappers[ind]
			self.bpm_wrappers_useInPhaseAnalysis[ind] = Boolean(int(use_in_phase_analysis_arr[ind])).booleanValue()
			self.bpm_wrappers_useInAmpBPMs[ind] = 		Boolean(int(use_in_amp_analysis_arr[ind])).booleanValue()
		#-------- read the phase scan data
		# we have to keep in mind that in self.bpm_wrappers we have only bpm wrappers that exist in the 
		# scl_long_tuneup_controller.bpm_wrappers and therefore exist in the self.bpm_amp_phase_dict as keys
		# Threfore they have (graphDataAmp,graphDataPhase) and we just have to fill them out.
		cav_wrapper_scan_data_da = cav_wrapper_da.childAdaptor("scan_data")
		for bpm_wrapper in self.bpm_wrappers:
			if(self.bpm_amp_phase_dict.has_key(bpm_wrapper)):
				(graphDataAmp,graphDataPhase) = self.bpm_amp_phase_dict[bpm_wrapper]
				cav_wrapper_scan_data_bpm_da = cav_wrapper_scan_data_da.childAdaptor(bpm_wrapper.alias)
				if(cav_wrapper_scan_data_bpm_da != null):
					readGraphDataFromDA(graphDataPhase,cav_wrapper_scan_data_bpm_da,"phase")
					readGraphDataFromDA(graphDataAmp,cav_wrapper_scan_data_bpm_da,"amplitude")
			if(self.bpm_xy_dict.has_key(bpm_wrapper)):
				(graphDataX,graphDataY) = self.bpm_xy_dict[bpm_wrapper]
				cav_wrapper_scan_data_bpm_da = cav_wrapper_scan_data_da.childAdaptor(bpm_wrapper.alias)
				if(cav_wrapper_scan_data_bpm_da != null):
					readGraphDataFromDA(graphDataX,cav_wrapper_scan_data_bpm_da,"posX")
					readGraphDataFromDA(graphDataY,cav_wrapper_scan_data_bpm_da,"posY")				
			
		


