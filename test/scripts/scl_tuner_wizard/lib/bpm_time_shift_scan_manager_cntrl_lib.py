#-------------------------------------------------------------------------------
# The BPM time shift has nothing to do with the phase offset. It defines 
# the center of the time window for BPM analysis of the bunches phases 
# and amplitudes. The phase offset is defined for 402.5 MHz frequency 
# ( 1 deg is in order of ps) , and the time shift is in order of 0.1 us.
# These classes will scan BPM timing (example of the PV name 
# SCL_Diag:BPM11:OEDA) the recorded values will be BPM phases and amplitudes.
# The BPM scans can be done concurrently, and they should be done after SCL
# Wizard finished tuning and analysis of the SCL cavities.
# After BPM scans the found SCL_Diag:BPM??:OEDA values will be uploaded to
# BPMs IOCs.
# OEDA stands for Off Energy Delay Adjustment
#------------------------------------------------------------------------------

import sys
import math
import types
import time
import random

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
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
from xal.smf.impl import Marker, BPM, Quadrupole, RfGap, SCLCavity
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.model.probe import EnvelopeProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.tools.beam import CovarianceMatrix
from xal.ca import ChannelFactory, BatchGetValueRequest
from xal.smf import AcceleratorSeqCombo

from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.smf.proxy import RfGapPropertyAccessor

import constants_lib 
from constants_lib import GRAPH_LEGEND_KEY
from harmonics_fitter_lib import calculateAvgErr
from scl_phase_scan_data_acquisition_lib import BPM_Wrapper

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None

#------------------------------------------------------------------------
#           Auxiliary SCAN classes and functions
#------------------------------------------------------------------------   
class ScanStateController:
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

class TimingScan_Runner(Runnable):
    def __init__(self,scl_long_tuneup_controller, run_to_end = true):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        self.run_to_end = run_to_end
        self.initial_time_shift_dict = {}
    
    def run(self):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        if(messageTextField != null):
            messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_buttons_panel = bpm_time_shift_scan_conroller.bpms_time_scan_buttons_panel
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        scan_state_controller = bpm_time_shift_scan_conroller.scan_state_controller 
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        bpm_selected_inds = bpms_time_scan_results_panel.bpm_table.getSelectedRows()
        if(self.run_to_end):
            bpm_selected_inds = [i for i in range(len(bpm_wrappers))]
            bpms_time_scan_results_panel.bpm_table.setRowSelectionInterval(0,len(bpm_wrappers)-1)
        if(len(bpm_selected_inds) == 0):
            messageTextField.setText("Please select BPMs in the table. Stop.")
            return
        start_time_shift = bpms_time_scan_buttons_panel.time_start_text.getValue()
        step_time_shift = bpms_time_scan_buttons_panel.time_step_text.getValue()
        n_steps = int(bpms_time_scan_buttons_panel.n_time_steps_text.getValue())
        sleep_time = bpms_time_scan_buttons_panel.sleep_time_text.getValue()
        min_bpm_amp = bpms_time_scan_buttons_panel.min_bpm_amp_text.getValue()
        #------ memorize the initial timing shifts
        bpm_local_wrappers = []
        for ind in bpm_selected_inds:
            bpm_wrapper = bpm_wrappers[ind]
            if(bpm_wrapper.isGood):
                bpm_local_wrappers.append(bpm_wrapper)      
        bpm_timing_batch_reader = BPM_Timing_Batch_Reader(self.scl_long_tuneup_controller,bpm_local_wrappers)
        bpm_timing_batch_reader.updateOEDA_BPMsTiming()
        self.initial_time_shift_dict = {}
        for bpm_wrapper in bpm_local_wrappers:
            bpm_wrapper.bpm_timing_bucket.clean()
            time_shift_init = bpm_wrapper.bpm_timing_bucket.getProductionTimeShift()
            self.initial_time_shift_dict[bpm_wrapper] = time_shift_init
        #-------------------------------------------
        scan_good_finish_type = True
        for step_ind in range(n_steps):
            time_shift = start_time_shift + step_ind*step_time_shift
            bpms_time_scan_buttons_panel.scan_status_text.setText("Scan is on. Index=" + str(step_ind+1) + " out of " + str(n_steps))
            for bpm_wrapper in bpm_local_wrappers:
                #??????
                bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(time_shift)
                if(scan_state_controller.getShouldStop()):
                    scan_good_finish_type = False
                    break
            if(not scan_good_finish_type): 
                break
            #---------- sleep to get new beem pulse
            time.sleep(sleep_time)
            bpm_timing_batch_reader = BPM_Timing_Batch_Reader(self.scl_long_tuneup_controller,bpm_local_wrappers)
            bpm_timing_batch_reader.updateBPM_TimingScanPlots(time_shift)
            if(scan_state_controller.getShouldStop()):
                scan_good_finish_type = False
                break                       
            if(not scan_good_finish_type): 
                break
        #------- finalizing scans 
        scan_state_controller.setIsRunning(false)
        scan_state_controller.setShouldStop(false)
        self.restoreInitialTimeShifts()     
        if(not scan_good_finish_type):
            bpms_time_scan_buttons_panel.scan_status_text.setText("Scan was stopped by user.")
        else:
            self.calculateCenter(bpm_selected_inds)
            bpms_time_scan_buttons_panel.scan_status_text.setText("Scan was successful.")
        #---- update table
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()
        bpms_time_scan_results_panel.bpm_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[-1])
        bpm_time_shift_scan_conroller.bpms_time_scan_results_panel.updateGraphData()    
            
    def restoreInitialTimeShifts(self):
        for (bpm_wrapper,time_shift_init) in self.initial_time_shift_dict.items():
            #??????
            bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(time_shift_init)
            bpm_wrapper.bpm_timing_bucket.setTuningTimeShift(time_shift_init)
            
    def calculateCenter(self,bpm_selected_inds):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for ind in bpm_selected_inds:
            bpm_wrapper = bpm_wrappers[ind]
            if(not bpm_wrapper.isGood): continue
            dg = bpm_wrapper.bpm_timing_bucket.amp_timing_graph_data
            #---- This is done because we have to analyze periodic data 
            #---- with periodicity around 1 us.  
            #---- let's find the average max value for set of points
            #---- over 0.2 us. It will eliminate parts at the beginning
            #---- and end of the scan time
            max_avg = 0.
            max_pos_ind0 = -1
            max_pos_ind1 = -1
            time_avg = 0.2
            for ind in range(dg.getNumbOfPoints()):
                ind_start = ind
                ind_stop = -1
                amp_avg = 0.
                n_ponts = 0
                x_start = dg.getX(ind_start)
                for ind_tmp in range(ind_start,dg.getNumbOfPoints()):
                    x_tmp = dg.getX(ind_tmp)
                    if(x_tmp - x_start > 0.2):
                        ind_stop = ind_tmp - 1
                        break
                    else:
                        amp_avg +=  dg.getY(ind_tmp)
                        n_ponts += 1
                if(n_ponts > 0):
                    amp_avg /= n_ponts
                if(amp_avg > max_avg):
                    max_avg = amp_avg
                    max_pos_ind0 = ind_start
                    max_pos_ind1 = ind_stop
            #---- max avg and range are found
            if(max_pos_ind0 < 0 or max_pos_ind1 < 0):
                bpm_wrapper.bpm_timing_bucket.setTuningTimeShift(0.)
                continue
            #---- Now we find the indexes for 0.5*max_amplitude crossing
            half_max_cross_index_arr = []
            for ind in range(dg.getNumbOfPoints()-1):
                amp1 = dg.getY(ind)
                amp2 = dg.getY(ind+1)
                if((amp1 - 0.5*amp_avg)*(amp2 - 0.5*amp_avg) < 0.):
                    half_max_cross_index_arr.append(ind)
            half_max_ind0 = 0
            half_max_ind1 = dg.getNumbOfPoints()-1
            max_pos_ind_avg = int((max_pos_ind0 + max_pos_ind1)/2)
            min_dist_ind0 = dg.getNumbOfPoints()-1
            min_dist_ind1 = dg.getNumbOfPoints()-1
            for ind in half_max_cross_index_arr:
                if(ind < max_pos_ind_avg and abs(ind - max_pos_ind_avg) < min_dist_ind0):
                    half_max_ind0 = ind
                    min_dist_ind0 = max_pos_ind_avg - ind
                if(ind > max_pos_ind_avg and abs(ind - max_pos_ind_avg) < min_dist_ind1):
                    half_max_ind1 = ind
                    min_dist_ind1 = max_pos_ind_avg - ind
            #---- now let's calculate avg over [half_max_ind0 -  half_max_ind1]
            #---- and up BPM's timing
            sum_amp = 0.
            time_shift_avg = 0.
            #for ind in range(max_pos_ind0,max_pos_ind1 + 1):
            for ind in range(half_max_ind0,half_max_ind1 + 1):
                time_shift = dg.getX(ind)
                amp = dg.getY(ind)
                sum_amp += amp
                time_shift_avg += amp*time_shift
            if(sum_amp != 0.):
                time_shift_avg /= sum_amp
            bpm_wrapper.bpm_timing_bucket.setTuningTimeShift(time_shift_avg)
            
#------------------------------------------------------------------------
# Batch BPM reader
#------------------------------------------------------------------------
class BPM_Timing_Batch_Reader:
    """
    Collects PV channel data for all BPMs - amplitudes, phases, and oeda timing
    """
    def __init__(self,scl_long_tuneup_controller, bpm_wrappers):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        self.bpm_wrappers = bpm_wrappers
        self.bpm_ch_amp_phase_time_dict = {}
        self.ch_arr = []
        for bpm_wrapper in self.bpm_wrappers:
            if(not bpm_wrapper.isGood): continue
            ch_ampl = ChannelFactory.defaultFactory().getChannel(bpm_wrapper.bpm.getId()+":amplitudeAvg")
            ch_phase = ChannelFactory.defaultFactory().getChannel(bpm_wrapper.bpm.getId()+":phaseAvg")
            #---- this is a pv channel for the time shift parameter 
            ch_oeda = bpm_wrapper.bpm_timing_bucket.pv_oeda
            if(ch_ampl.connectAndWait(2.0) and ch_phase.connectAndWait(2.0) and ch_oeda.connectAndWait(2.0)):
                self.bpm_ch_amp_phase_time_dict[bpm_wrapper] = (ch_ampl,ch_phase,ch_oeda)
                self.ch_arr.append(ch_ampl)
                self.ch_arr.append(ch_phase)
                self.ch_arr.append(ch_oeda)
                
    def getBatchBPMs_Data(self):
        """
        This method will measure the BPMs' amplitudes, phases, and OEDA.
        It will return the dictionary with these values:
        bpm_amp_phase_time_dict[bpm_wrapper] = (bpm_ampl,bpm_phase,bpm_oeda).
        """
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_amp_phase_time_dict = {}
        batchGetRequest = BatchGetValueRequest()
        for ch in self.ch_arr:
            batchGetRequest.addChannel(ch)
        result_info = true
        res_info = batchGetRequest.submitAndWait(3.)
        if(res_info == false):
            bad_chs = batchGetRequest.getFailedChannels()
            bad_bpms_names = []
            txt = ""
            for bad_ch in bad_chs:
                ch_name = bad_ch.channelName().replace("_Diag","")
                ind = ch_name.rfind(":")
                ch_name = ch_name[0:ind]
                if(not ch_name in bad_bpms_names): 
                    bad_bpms_names.append(ch_name)
                    txt += ch_name
            messageTextField.setText("Cannot read BPM data! BPMs: "+txt)
        #------------------------------
        bpm_amp_phase_time_dict = {}
        good_chs = batchGetRequest.getResultChannels()
        for bpm_wrapper in self.bpm_wrappers:
            if(self.bpm_ch_amp_phase_time_dict.has_key(bpm_wrapper)):
                (ch_ampl,ch_phase,ch_oeda) = self.bpm_ch_amp_phase_time_dict[bpm_wrapper]
                if(ch_ampl in good_chs and ch_phase in good_chs and ch_oeda in good_chs):
                    bpm_ampl = batchGetRequest.getRecord(ch_ampl).doubleValue()
                    bpm_phase = batchGetRequest.getRecord(ch_phase).doubleValue()
                    #---- here we have OEDA value raw from BPM, in the plots we use micro-seconds units
                    bpm_oeda = batchGetRequest.getRecord(ch_oeda).doubleValue()
                    bpm_amp_phase_time_dict[bpm_wrapper] = (bpm_ampl,bpm_phase,bpm_oeda)
        return bpm_amp_phase_time_dict
        
    def updateBPM_TimingScanPlots(self, time_shift):
        """
        This method updates the graph data on the plots BPMs' ampl and phases vs. OEDA time shift.
        """
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_buttons_panel = bpm_time_shift_scan_conroller.bpms_time_scan_buttons_panel       
        min_bpm_amp = bpms_time_scan_buttons_panel.min_bpm_amp_text.getValue()
        bpm_amp_phase_time_dict = self.getBatchBPMs_Data()
        for bpm_wrapper in self.bpm_wrappers:
            (bpm_ampl,bpm_phase,bpm_oeda) = bpm_amp_phase_time_dict[bpm_wrapper]
            if(bpm_ampl > min_bpm_amp):
                bpm_wrapper.bpm_timing_bucket.amp_timing_graph_data.addPoint(time_shift,bpm_ampl)
                bpm_wrapper.bpm_timing_bucket.phase_timing_graph_data.addPoint(time_shift,bpm_phase)
    
    def updateOEDA_BPMsTiming(self):
        """
        This method will set bpm_wrapper.production_time_shift for all BPMs in this class
        """
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")                
        bpm_amp_phase_time_dict = self.getBatchBPMs_Data()
        st = ""
        for bpm_wrapper in self.bpm_wrappers:
            if(not bpm_wrapper.isGood):
                bpm_wrapper.bpm_timing_bucket.externalUpdateProductionTimeShift(0.)
                continue
            if(bpm_amp_phase_time_dict.has_key(bpm_wrapper)):
                (bpm_ampl,bpm_phase,bpm_oeda) = bpm_amp_phase_time_dict[bpm_wrapper]
                bpm_wrapper.bpm_timing_bucket.externalUpdateProductionTimeShift(bpm_oeda*1.0e+6)
            else:
                bpm_wrapper.bpm_timing_bucket.externalUpdateProductionTimeShift(0.)
                st += " "+bpm_wrapper.alias
        if(len(st) > 1):
            messageTextField.setText("Bad BPMs: " + st)

#------------------------------------------------------------------------
#          BPM Timing Calculator
#------------------------------------------------------------------------       
class BPM_Timing_Calculator:
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        #----------------------------------------------
        self.scl_accSeq = self.scl_long_tuneup_controller.scl_accSeq
        self.part_tracker = AlgorithmFactory.createParticleTracker(self.scl_accSeq)
        #self.part_tracker.setRfGapPhaseCalculation(true)
        self.part_probe_init = ProbeFactory.createParticleProbe(self.scl_accSeq,self.part_tracker)
        self.scenario = Scenario.newScenarioFor(self.scl_accSeq)
        self.scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN)
        self.scenario.resync()      
        #-----------------------------------------------
        self.cav_amp_phase_design_dict = {}
        self.memorizeDesignAmPhase()
        #-----------------------------------------------
        rfGaps = self.scl_accSeq.getAllNodesWithQualifier(AndTypeQualifier().and((OrTypeQualifier()).or(RfGap.s_strType)))  
        self.cavToGapsDict = {} 
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            self.cavToGapsDict[cav_wrapper] = []
            for rfGap in rfGaps:
                if(rfGap.getId().find(cav_wrapper.cav.getId()) >= 0):
                    irfGaps = self.scenario.elementsMappedTo(rfGap)
                    self.cavToGapsDict[cav_wrapper].append(irfGaps[0])
    
    def memorizeDesignAmPhase(self):
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            amp = cav_wrapper.cav.getDfltCavAmp()
            phase = cav_wrapper.cav.getDfltCavPhase()
            #print "debug cav=",cav_wrapper.alias," amp=",amp," phase=",phase
            self.cav_amp_phase_design_dict[cav_wrapper] = (amp,phase)       
        
    def restoreDesignAmPhase(self):
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            (amp,phase) = self.cav_amp_phase_design_dict[cav_wrapper]
            cav_wrapper.cav.updateDesignAmp(amp)
            cav_wrapper.cav.updateDesignPhase(phase)
    
    def calculateBPM_ArrivalTime(self,account_for_blanked = False):
        self.memorizeDesignAmPhase()
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            if(cav_wrapper.isGood):
                blankBeamState = cav_wrapper.blankBeamBacket.getBlankBeamState()
                if(account_for_blanked == False or (not blankBeamState)):
                    cav_wrapper.cav.updateDesignAmp(cav_wrapper.designAmp)
                    #---- mistery - results are different for Design Phase to cavity and to the 1st RF gap
                    cav_wrapper.cav.updateDesignPhase(cav_wrapper.designPhase)
                    #irfGap = self.cavToGapsDict[cav_wrapper][0]
                    #irfGap.setPhase(cav_wrapper.designPhase*math.pi/180.)
                    #print "debug cav=",cav_wrapper.alias," amp=",cav_wrapper.designAmp," phase=",cav_wrapper.designPhase
                else:
                    cav_wrapper.cav.updateDesignAmp(0.) 
            else:
                cav_wrapper.cav.updateDesignAmp(0.)
        eKin_in = cav_wrappers[0].eKin_in
        #print "debug eKin_in=",eKin_in
        start_time = time.clock()
        part_probe = ParticleProbe(self.part_probe_init)
        part_probe.setKineticEnergy(eKin_in*1.0e+6)
        self.scenario.setProbe(part_probe)  
        self.scenario.resync()
        self.scenario.run()
        traj = self.scenario.getTrajectory()
        bpm_arrival_time_dict = {}
        for bpm_wrapper in bpm_wrappers:
            state = traj.stateForElement(bpm_wrapper.bpm.getId())
            bpm_arrival_time_dict[bpm_wrapper] = state.getTime()*1.0e+6
            eKin = state.getKineticEnergy()/1.0e+6
            #print "debug bpm=",bpm_wrapper.alias," eKin =",eKin," time=",state.getTime()*1.0e+6
        eKin_final = self.scenario.getTrajectory().finalState().getKineticEnergy()/1.0e+6
        #print "debug eKin_final =",eKin_final
        #print "calculation time=",(time.clock() - start_time)
        #-------------------------------------------
        #---- restoration the design values for cavity amplitudes and phases
        self.restoreDesignAmPhase()
        return bpm_arrival_time_dict
        
    def calculateBPM_ArrivalTimeSimple(self, account_for_blanked = False):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        #---- unblanked cavities starting from the 1st cavity
        cav_wrappers_unblanked = cav_wrappers
        if(account_for_blanked):
            cav_wrappers_unblanked = []
            for cav_wrapper in cav_wrappers:
                if(cav_wrapper.blankBeamBacket.getBlankBeamState()):
                    break
                cav_wrappers_unblanked.append(cav_wrapper)
        #----------------------------------------------------
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        timing_rescale_panel = bpm_time_shift_scan_conroller.timing_rescale_panel
        final_energy_estimation_text = timing_rescale_panel.final_energy_estimation_text
        #---- calculating energy vs. position assuming all cavities are on, and 
        #---- there is linear dependency
        ekin_vs_pos_gd = BasicGraphData()
        pos_start = cav_wrappers[0].getPosition()
        pos_stop = cav_wrappers[-1].getPosition()
        eKin_start = cav_wrappers[0].eKin_in
        eKin_stop = final_energy_estimation_text.getValue()
        ekin_vs_pos_gd.addPoint(0.,eKin_start)
        eKin_last = eKin_start
        for cav_wrapper in cav_wrappers_unblanked:
            pos = cav_wrapper.getPosition()
            eKin_last = eKin_start + (eKin_stop - eKin_start)*(pos - pos_start)/(pos_stop - pos_start)
            ekin_vs_pos_gd.addPoint(pos,eKin_last)
        bpm_pos_last = bpm_wrappers[-1].getPosition()
        ekin_vs_pos_gd.addPoint(bpm_pos_last,eKin_last)
        #---- now we will integrate a linear dependency of the energy for cavities 
        #---- to get arrival times at BPMs for case eKin = 185.6 - 1000 MeV
        c_light = 2.997924e+8
        pos_step = 0.5
        bpm_time_vs_pos_fake_production_gd = BasicGraphData()
        bpm_time_vs_pos_fake_production_gd.addPoint(0.,0.)
        n_steps = int(bpm_pos_last/pos_step) + 2
        mass = self.part_probe_init.getSpeciesRestEnergy()/1.0e+6
        time_total = 0.
        for ind in range(n_steps):
            pos = pos_step*(ind + 0.5)
            eKin = ekin_vs_pos_gd.getValueY(pos)
            gamma = (eKin + mass)/mass
            beta = math.sqrt(1.0 - 1.0/gamma**2)
            time_total += 1.0e+6*pos_step/(c_light*beta)
            bpm_time_vs_pos_fake_production_gd.addPoint(pos,time_total)
        #----------------------------------------------------------
        bpm_arrival_time_dict = {}
        for bpm_wrapper in bpm_wrappers:
            pos = bpm_wrapper.getPosition()
            bpm_time = bpm_time_vs_pos_fake_production_gd.getValueY(pos)
            bpm_arrival_time_dict[bpm_wrapper] = bpm_time
        return bpm_arrival_time_dict

    def calculateBPM_Prod_Test_Time_Diff(self, use_simple_scaling = True):
        bpm_arrival_time_prod_dict = {}
        bpm_arrival_time_test_dict = {}
        if(use_simple_scaling):
            bpm_arrival_time_prod_dict = self.calculateBPM_ArrivalTimeSimple(False)
            bpm_arrival_time_test_dict = self.calculateBPM_ArrivalTimeSimple(True)
        else:
            bpm_arrival_time_prod_dict = self.calculateBPM_ArrivalTime(False)
            bpm_arrival_time_test_dict = self.calculateBPM_ArrivalTime(True)
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        timing_rescale_panel = bpm_time_shift_scan_conroller.timing_rescale_panel
        timing_rescale_panel.prod_test_diff_timing_graph_data.removeAllPoints()
        timing_rescale_panel.new_test_diff_timing_graph_data.removeAllPoints()
        for bpm_wrapper_ind in range(len(bpm_wrappers)):
            bpm_wrapper = bpm_wrappers[bpm_wrapper_ind]
            time_diff = bpm_arrival_time_test_dict[bpm_wrapper] - bpm_arrival_time_prod_dict[bpm_wrapper]
            bpm_wrapper.bpm_timing_bucket.setTestTimeShift(time_diff)

    def showBPM_Prod_Test_Time_Diff(self):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        timing_rescale_panel = bpm_time_shift_scan_conroller.timing_rescale_panel
        timing_rescale_panel.prod_test_diff_timing_graph_data.removeAllPoints()
        timing_rescale_panel.new_test_diff_timing_graph_data.removeAllPoints()
        for bpm_wrapper_ind in range(len(bpm_wrappers)):
            bpm_wrapper = bpm_wrappers[bpm_wrapper_ind]
            time_prod_diff = bpm_wrapper.bpm_timing_bucket.getProductionTimeShift()
            time_test_diff = bpm_wrapper.bpm_timing_bucket.getTestTimeShift()
            time_new_diff = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
            timing_rescale_panel.prod_test_diff_timing_graph_data.addPoint(bpm_wrapper_ind*1.0,(time_test_diff-time_prod_diff))
            timing_rescale_panel.new_test_diff_timing_graph_data.addPoint(bpm_wrapper_ind*1.0,(time_new_diff - time_test_diff))
        timing_rescale_panel.bpm_test_timimg_table.getModel().fireTableDataChanged()
        
    def calculateAndSetBPMsOEDA_Timing(self):
        """
        It will calculate and set OEDA timing by using a simplified model.
        We need the simplified model, because we do not have SCL scan analysis
        yet.
        This method will be used form Phase Scan part of SCL Tuner Wizard.
        """
        #--------------Update blanked states for SCL RF cavities ----
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            if(cav_wrapper.isGood):
                cav_wrapper.blankBeamBacket.update()
        #-------------------------------------------------------------
        self.calculateBPM_Prod_Test_Time_Diff()
        self.showBPM_Prod_Test_Time_Diff()
        #---- set calculated test time shifts to production OEDA time for all BPMs
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(not bpm_wrapper.isGood): continue
            test_time_shift = bpm_wrapper.bpm_timing_bucket.getTestTimeShift()
            memorized_time_shift = bpm_wrapper.bpm_timing_bucket.getMemorizedTimeShift()
            bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(test_time_shift + memorized_time_shift)

#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------       
class CavitiesButtons_Panel(JPanel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        #----etched border
        etched_border = BorderFactory.createEtchedBorder()
        self.setBorder(etched_border)   
        #---- setup layout
        self.setLayout(GridLayout(3,2,2,2))
        update_blank_states_button = JButton("Update All Cavs")
        update_blank_states_button.addActionListener(Update_Blank_States_Button_Listener(self.scl_long_tuneup_controller))      
        update_blank_states_select_button = JButton("Update Selected Cavs")
        update_blank_states_select_button.addActionListener(Update_Blank_States_Selected_Button_Listener(self.scl_long_tuneup_controller))
        set_blank_on_for_all_button = JButton("Blank On All Cavs")
        set_blank_on_for_all_button.addActionListener(Set_Blank_On_for_All_Button_Listener(self.scl_long_tuneup_controller))
        set_blank_on_for_select_button = JButton("Blank On Select. Cavs")
        set_blank_on_for_select_button.addActionListener(Set_Blank_On_for_Selected_Button_Listener(self.scl_long_tuneup_controller))
        set_blank_off_for_all_button = JButton("Blank Off All Cavs")
        set_blank_off_for_all_button.addActionListener(Set_Blank_Off_for_All_Button_Listener(self.scl_long_tuneup_controller))
        set_blank_off_for_select_button = JButton("Blank Off Select. Cavs")
        set_blank_off_for_select_button.addActionListener(Set_Blank_Off_for_Selected_Button_Listener(self.scl_long_tuneup_controller))
        self.add(update_blank_states_button)
        self.add(update_blank_states_select_button)
        self.add(set_blank_on_for_all_button)
        self.add(set_blank_on_for_select_button)
        self.add(set_blank_off_for_all_button)
        self.add(set_blank_off_for_select_button)

class BPMs_Time_Scan_Buttons_Panel(JPanel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        #----etched border
        etched_border = BorderFactory.createEtchedBorder()
        self.setBorder(etched_border)   
        #---- setup layout
        self.setLayout(GridLayout(2,1,2,2))
        #-------------------------------------------
        buttons_panel1 =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
        buttons_panel2 =  JPanel(BorderLayout())
        buttons_panel3 =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
        time_step_lbl = JLabel(" Time Step[us]=",JLabel.RIGHT)
        self.time_step_text = DoubleInputTextField(0.02,DecimalFormat("#.###"),8)
        time_start_lbl = JLabel(" Start[us]=",JLabel.RIGHT)
        self.time_start_text = DoubleInputTextField(-0.5,DecimalFormat("#.###"),8)
        time_steps_lbl = JLabel(" N steps=",JLabel.RIGHT)
        self.n_time_steps_text = DoubleInputTextField(50,DecimalFormat("###"),8)
        sleep_time_lbl = JLabel(" Sleep[s]=",JLabel.RIGHT)
        self.sleep_time_text = DoubleInputTextField(1.1,DecimalFormat("##.##"),8)
        min_bpm_amp_lbl = JLabel(" Min BPM Amp.=",JLabel.RIGHT)
        self.min_bpm_amp_text = DoubleInputTextField(0.05,DecimalFormat("#.##"),8)      
        start_scan_button = JButton("Start Scan")
        start_scan_button.addActionListener(Start_Time_Scan_Button_Listener(self.scl_long_tuneup_controller))
        start_scan_for_selection_button = JButton("Start for Selected BPMs")
        start_scan_for_selection_button.addActionListener(Start_Time_Scan_Select_BPMs_Button_Listener(self.scl_long_tuneup_controller))
        stop_scan_button = JButton("Stop Scan")
        stop_scan_button.addActionListener(Stop_Time_Scan_Button_Listener(self.scl_long_tuneup_controller))
        buttons_panel1.add(time_step_lbl)
        buttons_panel1.add(self.time_step_text)
        buttons_panel1.add(time_start_lbl)
        buttons_panel1.add(self.time_start_text)
        buttons_panel1.add(time_steps_lbl)
        buttons_panel1.add(self.n_time_steps_text)
        buttons_panel1.add(sleep_time_lbl)
        buttons_panel1.add(self.sleep_time_text)
        buttons_panel1.add(min_bpm_amp_lbl)
        buttons_panel1.add(self.min_bpm_amp_text)
        buttons_panel3.add(start_scan_button)
        buttons_panel3.add(start_scan_for_selection_button)
        buttons_panel3.add(stop_scan_button)
        scan_status_lbl = JLabel(" Scan Status=",JLabel.RIGHT)
        self.scan_status_text = JTextField(10)
        self.scan_status_text.setText("Scan status")
        self.scan_status_text.setHorizontalAlignment(JTextField.LEFT)
        self.scan_status_text.setForeground(Color.red)
        buttons_panel3.add(scan_status_lbl)
        buttons_panel2.add(buttons_panel3,BorderLayout.WEST)
        buttons_panel2.add(self.scan_status_text,BorderLayout.CENTER)
        self.add(buttons_panel1)
        self.add(buttons_panel2)

class BPMs_Time_Scan_Results_Panel(JPanel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        #----etched border
        etched_border = BorderFactory.createEtchedBorder()
        self.setBorder(etched_border)   
        #---- setup layout
        self.setLayout(BorderLayout())
        #-------------------------------------------
        read_prod_timing_button = JButton("Read Production")
        read_prod_timing_button.addActionListener(Read_Prodiction_Timing_Button_Listener(self.scl_long_tuneup_controller))
        scan_to_prod_button = JButton("Scan -> Production")
        scan_to_prod_button.addActionListener(Set_Scan_to_Production_Timing_Button_Listene(self.scl_long_tuneup_controller))
        zero_prod_timing_button = JButton("Zero -> Production")
        zero_prod_timing_button.addActionListener(Set_Zero_to_Production_Button_Listener(self.scl_long_tuneup_controller))
        zero_to_scan_timing_button = JButton("Clean Scan")
        zero_to_scan_timing_button.addActionListener(Set_Zero_to_Scan_Button_Listener(self.scl_long_tuneup_controller))
        scan_to_memorized_timing_butto = JButton("Scan -> Memory")
        scan_to_memorized_timing_butto.addActionListener(Set_Scan_to_Memory_Button_Listener(self.scl_long_tuneup_controller))
        prod_to_memorized_timing_button = JButton("Prod. -> Memory")
        prod_to_memorized_timing_button.addActionListener(Set_Production_to_Memory_Button_Listener(self.scl_long_tuneup_controller))
        memorized_to_prod_timing_button = JButton("Memory -> Prod.")
        memorized_to_prod_timing_button.addActionListener(Set_Memory_to_Production_Button_Listener(self.scl_long_tuneup_controller))
        zero_to_memorized_timing_button = JButton("Zero -> Memory")
        zero_to_memorized_timing_button.addActionListener(Set_Zero_to_Memory_Button_Listener(self.scl_long_tuneup_controller))
        empty_1_button = JButton("")
        #---------------------------------------------------------------
        bpm_scan_res_buttons_panel = JPanel(GridLayout(3,3,2,2))
        bpm_scan_res_buttons_panel.add(read_prod_timing_button)
        bpm_scan_res_buttons_panel.add(prod_to_memorized_timing_button)
        bpm_scan_res_buttons_panel.add(scan_to_prod_button)
        #----
        bpm_scan_res_buttons_panel.add(zero_prod_timing_button )
        bpm_scan_res_buttons_panel.add(memorized_to_prod_timing_button)
        bpm_scan_res_buttons_panel.add(zero_to_scan_timing_button)
        #----
        bpm_scan_res_buttons_panel.add(empty_1_button)
        bpm_scan_res_buttons_panel.add(zero_to_memorized_timing_button)
        bpm_scan_res_buttons_panel.add(scan_to_memorized_timing_butto)
        #-------------------------------------------
        self.bpm_table = JTable(BPMs_Table_Model(self.scl_long_tuneup_controller))
        self.bpm_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
        self.bpm_table.setFillsViewportHeight(true)
        self.bpm_table.getSelectionModel().addListSelectionListener(BPMs_Table_Selection_Listener(self.scl_long_tuneup_controller)) 
        self.bpm_table.setPreferredScrollableViewportSize(Dimension(320,300))       
        scrl_panel = JScrollPane(self.bpm_table)
        scrl_panel.setBorder(etched_border)
        #----------------------------------------
        tmp_panel = JPanel(BorderLayout())
        tmp_panel.add(scrl_panel,BorderLayout.CENTER)
        tmp_panel.add(bpm_scan_res_buttons_panel,BorderLayout.NORTH)
        self.add(tmp_panel,BorderLayout.WEST)
        #--------------------------------------
        self.gp_bpm_ampl = FunctionGraphsJPanel()
        self.gp_bpm_ampl.setLegendButtonVisible(true)
        self.gp_bpm_ampl.setChooseModeButtonVisible(true)
        self.gp_bpm_ampl.setName("BPM Amplitude vs. Time Shift:")
        self.gp_bpm_ampl.setAxisNames("Time Shift, [us]","BPM Amplitude, arb. unit")    
        self.gp_bpm_ampl.setBorder(etched_border)
        #----
        self.gp_bpm_phase = FunctionGraphsJPanel()
        self.gp_bpm_phase.setLegendButtonVisible(true)
        self.gp_bpm_phase.setChooseModeButtonVisible(true)
        self.gp_bpm_phase.setName("BPM Phase vs. Time Shift:")
        self.gp_bpm_phase.setAxisNames("Time Shift, [us]","BPM Phase, deg") 
        self.gp_bpm_phase.setBorder(etched_border)
        #---- add vertical line
        self.gp_bpm_ampl.addVerticalLine(0.0,Color.RED)
        self.gp_bpm_phase.addVerticalLine(0.0,Color.RED)
        #--------------------
        graph_panel = JPanel(GridLayout(2,1,2,2))
        graph_panel.add(self.gp_bpm_ampl)
        graph_panel.add(self.gp_bpm_phase)
        #--------------------------------------
        self.add(graph_panel,BorderLayout.CENTER)

    def updateGraphData(self):
        self.gp_bpm_ampl.removeAllGraphData()
        self.gp_bpm_phase.removeAllGraphData()
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_table = self.bpm_table
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        bpm_selected_inds = bpm_table.getSelectedRows()
        if(len(bpm_selected_inds) == 0):
            return
        for ind in bpm_selected_inds:
            bpm_wrapper = bpm_wrappers[ind]
            if(not bpm_wrapper.isGood): continue
            bpm_timing_bucket = bpm_wrapper.bpm_timing_bucket
            [amp_timing_graph_data,phase_timing_graph_data] = bpm_timing_bucket.getGraphDataArr()
            if(ind == bpm_selected_inds[0]):
                time_shift = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
                self.gp_bpm_ampl.setVerticalLineValue(time_shift,0)
                self.gp_bpm_phase.setVerticalLineValue(time_shift,0)
            if(amp_timing_graph_data != null):
                self.gp_bpm_ampl.addGraphData(amp_timing_graph_data)
            if(phase_timing_graph_data != null):
                self.gp_bpm_phase.addGraphData(phase_timing_graph_data)

class Timing_Rescale_Test_Panel(JPanel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        #----etched border
        etched_border = BorderFactory.createEtchedBorder()
        self.setBorder(etched_border)   
        #---- setup layout
        self.setLayout(BorderLayout())
        #------------------------------
        self.bpm_test_timimg_table = JTable(BPMs_Test_Timing_Table_Model(   self.scl_long_tuneup_controller))
        self.bpm_test_timimg_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
        self.bpm_test_timimg_table.setFillsViewportHeight(true)
        self.bpm_test_timimg_table.setPreferredScrollableViewportSize(Dimension(400,300))   
        scrl_bpm_test_timing_panel = JScrollPane(self.bpm_test_timimg_table)
        bpm_test_timing_border = BorderFactory.createTitledBorder(etched_border,"BPM Timing Parameters")
        scrl_bpm_test_timing_panel.setBorder(bpm_test_timing_border)
        test_to_scan_timing_button = JButton("Test -> Scan")
        test_to_scan_timing_button.addActionListener(Test_to_Scan_Timing_Button_Listener(self.scl_long_tuneup_controller))      
        bpm_test_scan_timing_panel = JPanel(BorderLayout())
        bpm_test_scan_timing_panel.add(test_to_scan_timing_button,BorderLayout.NORTH)
        bpm_test_scan_timing_panel.add(scrl_bpm_test_timing_panel,BorderLayout.CENTER)
        #-------------------------------
        calculate_test_timing_button = JButton("Calculate Test Shift")
        calculate_test_timing_button.addActionListener(Calculate_Test_Timing_Button_Listener(self.scl_long_tuneup_controller))
        buttons_panel_0 =  JPanel(FlowLayout(FlowLayout.LEFT,3,3))
        buttons_panel_0.add(calculate_test_timing_button)
        self.use_simplify_scaling_button = JRadioButton("Use Linear Scaling",True)
        final_energy_estimation_lbl = JLabel("for Final Ekin[MeV]=",JLabel.RIGHT)
        self.final_energy_estimation_text = DoubleInputTextField(1000.,DecimalFormat("####.#"),8)
        buttons_panel_0.add(self.use_simplify_scaling_button)
        buttons_panel_0.add(final_energy_estimation_lbl)
        buttons_panel_0.add(self.final_energy_estimation_text)
        buttons_panel = JPanel(BorderLayout())
        buttons_panel.add(buttons_panel_0,BorderLayout.NORTH)
        
        #-------------------------------
        #--------------------------------------
        self.gp_test_timing = FunctionGraphsJPanel()
        self.gp_test_timing.setLegendButtonVisible(true)
        self.gp_test_timing.setChooseModeButtonVisible(true)
        self.gp_test_timing.setName("Differences Production-Test and Scan-Test Timing")
        self.gp_test_timing.setAxisNames("BPM #]","Time Shift, us") 
        self.gp_test_timing.setBorder(etched_border)
        #---------------------------------------
        self.prod_test_diff_timing_graph_data = BasicGraphData()
        self.prod_test_diff_timing_graph_data.setDrawPointsOn(true)
        self.prod_test_diff_timing_graph_data.setGraphColor(Color.RED)
        self.prod_test_diff_timing_graph_data.setLineThick(3)
        self.prod_test_diff_timing_graph_data.setGraphPointSize(5)
        self.prod_test_diff_timing_graph_data.setGraphProperty(GRAPH_LEGEND_KEY,"Production-Test Time Diff.")
        #--------------------------------------
        self.new_test_diff_timing_graph_data = BasicGraphData()
        self.new_test_diff_timing_graph_data.setDrawPointsOn(true)
        self.new_test_diff_timing_graph_data.setGraphColor(Color.BLUE)
        self.new_test_diff_timing_graph_data.setLineThick(3)
        self.new_test_diff_timing_graph_data.setGraphPointSize(5)
        self.new_test_diff_timing_graph_data.setGraphProperty(GRAPH_LEGEND_KEY,"Scan-Test Time Diff.")      
        #-------------------------------------
        self.gp_test_timing.addGraphData(self.prod_test_diff_timing_graph_data)
        self.gp_test_timing.addGraphData(self.new_test_diff_timing_graph_data)
        #------------------------------------
        smulation_panel = JPanel(BorderLayout())
        smulation_panel.add(buttons_panel,BorderLayout.NORTH)
        smulation_panel.add(self.gp_test_timing,BorderLayout.CENTER)
        #-------------------------------------
        self.add(bpm_test_scan_timing_panel,BorderLayout.WEST)
        self.add(smulation_panel,BorderLayout.CENTER)

#------------------------------------------------
#  JTable models
#------------------------------------------------
class Cavs_Params_Table_Model(AbstractTableModel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        self.columnNames = ["Cavity","Use","Amp","Blanked"]
        self.nf3 = NumberFormat.getInstance()
        self.nf3.setMaximumFractionDigits(3)        
        self.string_class = String().getClass()
        self.boolean_class = Boolean(true).getClass()
        
    def getColumnCount(self):
        return len(self.columnNames)
        
    def getRowCount(self):
        return (len(self.scl_long_tuneup_controller.cav_wrappers))

    def getColumnName(self,col):
        return self.columnNames[col]
        
    def getValueAt(self,row,col):

        cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
        if(col == 0): 
            return cav_wrapper.alias
        if(col == 1): return cav_wrapper.isGood 
        if(col == 2): return cav_wrapper.designAmp
        if(col == 3): return cav_wrapper.blankBeamBacket.getBlankBeamState()
        return ""
                
    def getColumnClass(self,col):
        if(col == 1 or col == 3):
            return self.boolean_class
        return self.string_class        
    
    def isCellEditable(self,row,col):
        cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]     
        if(col == 3 and cav_wrapper.isGood):
            return true
        return false
            
    def setValueAt(self, value, row, col):
        cav_wrapper = self.scl_long_tuneup_controller.cav_wrappers[row]
        if(col == 3 and cav_wrapper.isGood):
            #?????? - this is for real runs to provide blanking/unblanking from the table
            cav_wrapper.cav.setBlankBeam(value)
            cav_wrapper.blankBeamBacket.update()
            #????? This for debugging only
            #cav_wrapper.blankBeamBacket.setBlankBeamState(value)

class BPMs_Table_Model(AbstractTableModel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        self.columnNames = ["BPM","Use"]
        self.columnNames.append("<html>&Delta;T<SUB>prod</SUB>[us]<html>")
        self.columnNames.append("<html>&Delta;T<SUB>memory</SUB>[us]<html>")
        self.columnNames.append("<html>&Delta;T<SUB>scan</SUB>[us]<html>")
        self.nf3 = NumberFormat.getInstance()
        self.nf3.setMaximumFractionDigits(4)
        self.string_class = String().getClass()
        self.boolean_class = Boolean(true).getClass()
        
    def getColumnCount(self):
        return len(self.columnNames)
        
    def getRowCount(self):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        return len(bpm_wrappers)

    def getColumnName(self,col):
        return self.columnNames[col]
        
    def getValueAt(self,row,col):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrapper = bpm_time_shift_scan_conroller.bpm_wrappers[row]
        if(col == 0): return bpm_wrapper.alias
        if(col == 1): return bpm_wrapper.isGood
        if(col == 2):
            time_shift = bpm_wrapper.bpm_timing_bucket.getProductionTimeShift()
            return self.nf3.format(time_shift)
        if(col == 3): 
            time_shift = bpm_wrapper.bpm_timing_bucket.getMemorizedTimeShift()
            return self.nf3.format(time_shift)          
        if(col == 4): 
            time_shift = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
            return self.nf3.format(time_shift)
        return ""
                
    def getColumnClass(self,col):
        if(col == 1):
            return self.boolean_class
        return self.string_class
    
    def isCellEditable(self,row,col):
        return false

class BPMs_Test_Timing_Table_Model(AbstractTableModel):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        self.columnNames = [" # ","BPM","Use"]
        self.columnNames.append("<html>&Delta;T<SUB>prod</SUB>[us]<html>")
        self.columnNames.append("<html>&Delta;T<SUB>scan</SUB>[us]<html>")
        self.columnNames.append("<html>&Delta;T<SUB>test</SUB>[us]<html>")
        self.nf3 = NumberFormat.getInstance()
        self.nf3.setMaximumFractionDigits(4)
        self.string_class = String().getClass()
        self.boolean_class = Boolean(true).getClass()
        
    def getColumnCount(self):
        return len(self.columnNames)
        
    def getRowCount(self):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        return len(bpm_wrappers)

    def getColumnName(self,col):
        return self.columnNames[col]
        
    def getValueAt(self,row,col):
        if(col == 0): return str(row)
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_wrapper = bpm_time_shift_scan_conroller.bpm_wrappers[row]
        if(col == 1): return bpm_wrapper.alias
        if(col == 2): return bpm_wrapper.isGood
        if(col == 3): 
            time_shift = bpm_wrapper.bpm_timing_bucket.getProductionTimeShift()
            return self.nf3.format(time_shift)
        if(col == 4): 
            time_shift = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
            return self.nf3.format(time_shift)
        if(col == 5): 
            time_shift = bpm_wrapper.bpm_timing_bucket.getTestTimeShift()
            return self.nf3.format(time_shift)          
        return ""
                
    def getColumnClass(self,col):
        if(col == 2):
            return self.boolean_class
        return self.string_class
    
    def isCellEditable(self,row,col):
        return false

#------------------------------------------------------------------------
#           Listeners
#------------------------------------------------------------------------
class Cavs_Params_Table_Selection_Listener(ListSelectionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def valueChanged(self,listSelectionEvent):
        if(listSelectionEvent.getValueIsAdjusting()): return
        listSelectionModel = listSelectionEvent.getSource()
        index = listSelectionModel.getMinSelectionIndex()


class Start_Time_Scan_Button_Listener(ActionListener):
    #This button will start time scan for all BPMs
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        self.scl_long_tuneup_controller.getMessageTextField().setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        scan_state_controller = bpm_time_shift_scan_conroller.scan_state_controller
        scan_state_controller.setShouldStop(false)
        scan_state_controller.setIsRunning(true)
        runner = TimingScan_Runner(self.scl_long_tuneup_controller,true)
        #---- this part for the debug only -----START----
        """
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel       
        bpm_selected_inds = bpms_time_scan_results_panel.bpm_table.getSelectedRows()
        runner.calculateCenter(bpm_selected_inds)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()
        bpms_time_scan_results_panel.bpm_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[-1])
        bpm_time_shift_scan_conroller.bpms_time_scan_results_panel.updateGraphData()
        """
        #---- this part for the debug only -----STOP---- Thread run should be switched off
        thr = Thread(runner)
        thr.start()             
        
class Start_Time_Scan_Select_BPMs_Button_Listener(ActionListener):
    #This button will start time scan for selcted BPMs
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        self.scl_long_tuneup_controller.getMessageTextField().setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        scan_state_controller = bpm_time_shift_scan_conroller.scan_state_controller
        scan_state_controller.setShouldStop(false)
        scan_state_controller.setIsRunning(true)
        runner = TimingScan_Runner(self.scl_long_tuneup_controller,false)
        thr = Thread(runner)
        thr.start()

class Stop_Time_Scan_Button_Listener(ActionListener):
    #This button will stop time scan
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        self.scl_long_tuneup_controller.getMessageTextField().setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        scan_state_controller = bpm_time_shift_scan_conroller.scan_state_controller
        scan_state_controller.setShouldStop(true)

class Update_Blank_States_Button_Listener(ActionListener):
    #This button will update Blanking states
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            if(cav_wrapper.isGood):
                cav_wrapper.blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()

class Update_Blank_States_Selected_Button_Listener(ActionListener):
    #This button will 
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        cav_ind_arr = cavs_params_table.getSelectedRows()
        if(len(cav_ind_arr) == 0):
            messageTextField.setText("Please select cavities!")
            return
        for ind in cav_ind_arr:
            if(cav_wrappers[ind].isGood):
                cav_wrappers[ind].blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()
        
class Set_Blank_On_for_All_Button_Listener(ActionListener):
    #This button will 
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            if(cav_wrapper.isGood):
                cav_wrapper.setBlankBeam(true)
                cav_wrapper.blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()

class Set_Blank_On_for_Selected_Button_Listener(ActionListener):
    #This button will 
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        cav_ind_arr = cavs_params_table.getSelectedRows()
        if(len(cav_ind_arr) == 0):
            messageTextField.setText("Please select cavities!")
            return
        for ind in cav_ind_arr:
            if(cav_wrappers[ind].isGood):
                cav_wrappers[ind].setBlankBeam(true)
                cav_wrappers[ind].blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()

class Set_Blank_Off_for_All_Button_Listener(ActionListener):
    #This button will 
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        for cav_wrapper in cav_wrappers:
            if(cav_wrapper.isGood):
                cav_wrapper.setBlankBeam(false)
                cav_wrapper.blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()

class Set_Blank_Off_for_Selected_Button_Listener(ActionListener):
    #This button will remove blanking for selected cavities
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        cavs_params_table = bpm_time_shift_scan_conroller.cavs_params_table
        cav_wrappers = self.scl_long_tuneup_controller.cav_wrappers
        cav_ind_arr = cavs_params_table.getSelectedRows()
        if(len(cav_ind_arr) == 0):
            messageTextField.setText("Please select cavities!")
            return
        for ind in cav_ind_arr:
            if(cav_wrappers[ind].isGood):
                cav_wrappers[ind].setBlankBeam(false)
                cav_wrappers[ind].blankBeamBacket.update()
        cavs_params_table.getModel().fireTableDataChanged()

class Read_Prodiction_Timing_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_selected_inds = bpms_time_scan_results_panel.bpm_table.getSelectedRows()
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        bpm_local_wrappers = []
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                bpm_local_wrappers.append(bpm_wrapper)
        bpm_timing_batch_reader = BPM_Timing_Batch_Reader(self.scl_long_tuneup_controller,bpm_local_wrappers)
        bpm_timing_batch_reader.updateOEDA_BPMsTiming()
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()
        if(len(bpm_selected_inds) != 0):
            bpms_time_scan_results_panel.bpm_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[-1])      
        

class Set_Scan_to_Production_Timing_Button_Listene(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_selected_inds = bpms_time_scan_results_panel.bpm_table.getSelectedRows()
        if(len(bpm_selected_inds) == 0):
            messageTextField.setText("Please select BPMs for which you want to copy New to Production timing shift.")
            return
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_ind in bpm_selected_inds:
            bpm_wrapper = bpm_wrappers[bpm_ind]
            if(bpm_wrapper.isGood):
                time_shift = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
                bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(time_shift)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()
        if(len(bpm_selected_inds) != 0):
            bpms_time_scan_results_panel.bpm_table.setRowSelectionInterval(bpm_selected_inds[0],bpm_selected_inds[-1])

class Set_Zero_to_Production_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller
        
    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_selected_inds = bpms_time_scan_results_panel.bpm_table.getSelectedRows()
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(0.)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()

class Set_Zero_to_Scan_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                bpm_wrapper.bpm_timing_bucket.setTuningTimeShift(0.)
                bpm_wrapper.bpm_timing_bucket.clean()
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()
        
class Set_Scan_to_Memory_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                scan_time_shift = bpm_wrapper.bpm_timing_bucket.getTuningTimeShift()
                bpm_wrapper.bpm_timing_bucket.setMemorizedTimeShift(scan_time_shift)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()

class Set_Production_to_Memory_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                production_time_shift = bpm_wrapper.bpm_timing_bucket.getProductionTimeShift()
                bpm_wrapper.bpm_timing_bucket.setMemorizedTimeShift(production_time_shift)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()

class Set_Memory_to_Production_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                memory_time_shift = bpm_wrapper.bpm_timing_bucket.getMemorizedTimeShift()
                bpm_wrapper.bpm_timing_bucket.setProductionTimeShift(memory_time_shift)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()

class Set_Zero_to_Memory_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpms_time_scan_results_panel = bpm_time_shift_scan_conroller.bpms_time_scan_results_panel
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            if(bpm_wrapper.isGood):
                bpm_wrapper.bpm_timing_bucket.setMemorizedTimeShift(0.)
        bpms_time_scan_results_panel.bpm_table.getModel().fireTableDataChanged()

class Test_to_Scan_Timing_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        timing_rescale_panel = bpm_time_shift_scan_conroller.timing_rescale_panel
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_wrappers = bpm_time_shift_scan_conroller.bpm_wrappers
        for bpm_wrapper in bpm_wrappers:
            time_shift = bpm_wrapper.bpm_timing_bucket.getTestTimeShift()
            bpm_wrapper.bpm_timing_bucket.setTuningTimeShift(time_shift)
        timing_rescale_panel.bpm_test_timimg_table.getModel().fireTableDataChanged()

class Calculate_Test_Timing_Button_Listener(ActionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def actionPerformed(self,actionEvent):
        messageTextField = self.scl_long_tuneup_controller.getMessageTextField()
        messageTextField.setText("")
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_timing_calculator = bpm_time_shift_scan_conroller.bpm_timing_calculator
        timing_rescale_panel = bpm_time_shift_scan_conroller.timing_rescale_panel
        use_simple_scaling = timing_rescale_panel.use_simplify_scaling_button.isSelected()
        bpm_timing_calculator.calculateBPM_Prod_Test_Time_Diff(use_simple_scaling)
        bpm_timing_calculator.showBPM_Prod_Test_Time_Diff()

class BPMs_Table_Selection_Listener(ListSelectionListener):
    def __init__(self,scl_long_tuneup_controller):
        self.scl_long_tuneup_controller = scl_long_tuneup_controller

    def valueChanged(self,listSelectionEvent):
        if(listSelectionEvent.getValueIsAdjusting()): return
        listSelectionModel = listSelectionEvent.getSource()
        index = listSelectionModel.getMinSelectionIndex()   
        bpm_time_shift_scan_conroller = self.scl_long_tuneup_controller.bpm_time_shift_scan_conroller
        bpm_time_shift_scan_conroller.bpms_time_scan_results_panel.updateGraphData()    

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class BPM_Time_Shift_Scan_Controller:
    def __init__(self,scl_long_tuneup_controller):
        #--- scl_long_tuneup_controller the parent document for all SCL tune up controllers
        self.scl_long_tuneup_controller =   scl_long_tuneup_controller  
        self.main_panel = JPanel(BorderLayout())
        #-----------------------------------------------------
        #---- set up BPM wrappers including HEBT2
        self.bpm_wrappers = self.scl_long_tuneup_controller.bpm_wrappers[:]
        accl = self.scl_long_tuneup_controller.linac_wizard_document.accl
        lst = ArrayList()
        lst.add(accl.getSequence("HEBT2"))
        hebt2_accSeq = AcceleratorSeqCombo("SCL_SEQUENCE", lst)          
        bpms = hebt2_accSeq.getAllNodesWithQualifier((AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType))).andStatus(true))
        for bpm in bpms:
            #print "debug bpm=",bpm.getId()
            bpm_wrapper = BPM_Wrapper(bpm)
            bpm_wrapper.setPosition( hebt2_accSeq)
            self.bpm_wrappers.append(bpm_wrapper)        
        #----etched border
        etched_border = BorderFactory.createEtchedBorder()          
        #------cavities parameters panel-----------------------
        cavity_parameters_panel = JPanel(BorderLayout())
        self.cavities_buttons_panel = CavitiesButtons_Panel(self.scl_long_tuneup_controller)
        self.cavs_params_table = JTable(Cavs_Params_Table_Model(self.scl_long_tuneup_controller))
        self.cavs_params_table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION)
        self.cavs_params_table.setFillsViewportHeight(true)
        self.cavs_params_table.setPreferredScrollableViewportSize(Dimension(300,300))   
        self.cavs_params_table.getSelectionModel().addListSelectionListener(Cavs_Params_Table_Selection_Listener(self.scl_long_tuneup_controller))
        scrl_cavs_panel = JScrollPane(self.cavs_params_table)
        cavs_table_border = BorderFactory.createTitledBorder(etched_border,"Cavities Parameters")
        scrl_cavs_panel.setBorder(cavs_table_border)
        cavity_parameters_panel.add(self.cavities_buttons_panel,BorderLayout.NORTH)
        cavity_parameters_panel.add(scrl_cavs_panel,BorderLayout.CENTER)
        #------BPMs Timing Scan panel --------
        bpms_timing_scan_panel = JPanel(BorderLayout())
        bpms_timing_scan_panel.setBorder(etched_border)
        self.bpms_time_scan_buttons_panel = BPMs_Time_Scan_Buttons_Panel(self.scl_long_tuneup_controller)
        self.bpms_time_scan_results_panel = BPMs_Time_Scan_Results_Panel(self.scl_long_tuneup_controller)
        bpms_timing_scan_panel.add(self.bpms_time_scan_buttons_panel,BorderLayout.NORTH)
        bpms_timing_scan_panel.add(self.bpms_time_scan_results_panel,BorderLayout.CENTER)
        #----Timing Rescale Panel
        self.timing_rescale_panel = Timing_Rescale_Test_Panel(self.scl_long_tuneup_controller)
        #---- Tubbed BPM  panel
        tabbedPane = JTabbedPane()  
        tabbedPane.add("BPMs Timing Scan",bpms_timing_scan_panel)       
        tabbedPane.add("Timing Rescale Test",self.timing_rescale_panel)     
        #--------------------------------------------------
        self.main_panel.add(cavity_parameters_panel,BorderLayout.WEST)
        self.main_panel.add(tabbedPane,BorderLayout.CENTER)
        #------ BPM Timing Calculator
        self.bpm_timing_calculator = BPM_Timing_Calculator(self.scl_long_tuneup_controller)
        #------scan state
        self.scan_state_controller = ScanStateController()
        
        
    def getMainPanel(self):
        return self.main_panel
