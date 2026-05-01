# The SCL Longitudinal Tune-Up - Energy Meter
# It will calculate the energy of the beam in the HEBT1
import sys
import math
import types
import time
import random

from java.awt.geom import Ellipse2D

from xal.smf.data import XMLDataManager
#from xal.sim.scenario import ProbeFactory
#from xal.sim.scenario import Scenario
from xal.smf import AcceleratorSeqCombo

from xjava.lang import *
from xjava.swing import *
from javax.swing import JTable, JComboBox
from javax.swing.event import TableModelEvent, TableModelListener, ListSelectionListener
from java.awt import Color, BorderLayout, GridLayout, FlowLayout
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat
from javax.swing.table import AbstractTableModel, TableModel
from java.awt.event import ActionEvent, ActionListener
from java.awt import Dimension
from java.beans import PropertyChangeListener
from javax.swing.filechooser import FileNameExtensionFilter
from javax.swing.border import TitledBorder

from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from xal.extension.widgets.plot import GraphDataOperations
from xal.extension.widgets.swing import DoubleInputTextField, IntegerInputTextField 
from xal.smf.impl import Marker, Quadrupole, RfGap
from xal.smf.impl.qualify import AndTypeQualifier, OrTypeQualifier
from xal.model.probe import ParticleProbe
from xal.sim.scenario import Scenario, AlgorithmFactory, ProbeFactory
from xal.ca import ChannelFactory
from xal.model.alg import TransferMapTracker
from xal.tools.beam import PhaseVector

from xal.ca.correlator import ChannelCorrelator


from xal.extension.widgets.swing import Wheelswitch

import constants_lib
from constants_lib import GRAPH_LEGEND_KEY
from scl_phase_scan_data_acquisition_lib import BPM_Batch_Reader
from magnet_scaling_lib import EnergyScalingLinacRingSNS
from harmonics_fitter_lib import HarmonicsAnalyzer, HramonicsFunc, makePhaseNear, calculateAvgErr

false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = None
		
		

#------------------------------------------------------------------------
#           Controllers
#------------------------------------------------------------------------
class Dispersion_Measurement_Controller:
	def __init__(self, em_controller):

                self.harmonicsAnalyzer = HarmonicsAnalyzer(2)
                self.dphi = 5.0
                self.de = 0.0
                self.dpp = 0.0
                self.dx = 0.0
                self.disp = 0.0
                self.nmeas = 10

                #acc = XMLDataManager.acceleratorWithPath("/home/tg4/Documents/Wolfram-Mathematica/laser stripping/design/main.xal")
                acc = XMLDataManager.loadDefaultAccelerator()
                

                self.cavs = acc.getAllNodesOfType("SCLCavity")
                seq = acc.getSequence("HEBT2")
                self.nodes = seq.getNodes()
                
                remove_node = seq.getNodeWithId("HEBT_Diag:BPM29")
                seq.removeNode(remove_node)
                self.blms = seq.getAllNodesOfType("BLM") 
                for blm in self.blms: 
                    seq.removeNode(blm)

                
                
                self.nodes_list = JList(self.nodes)
		self.nodes_list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
		self.nodes_list.setLayoutOrientation(JList.VERTICAL)
		self.nodes_list.setSelectionForeground(Color.red)
                #self.cav_list.setSelectedIndex(self.cavs.size()-1)  
                si =  seq.getIndexOfNode(seq.getNodeWithId("HEBT_Diag:WS28"))   
                self.nodes_list.setSelectedIndex(si)      
                self.nodes_scrollPane = JScrollPane(self.nodes_list)     
                self.meas_node = self.nodes_list.getSelectedValue()     
                #self.nodes_list.addListSelectionListener(Change_disp_meas_Listener(self))     
                
                self.nodes_combo_box= JComboBox(self.nodes)
                self.nodes_combo_box.setSelectedItem(seq.getNodeWithId("HEBT_Diag:WS28"))
                self.nodes_combo_box.addActionListener(Change_disp_meas_Listener(self))
                
                
                
                
                
                #ptracker = TransferMapTracker()
                self.model = Scenario.newScenarioFor(seq)
                self.model.setSynchronizationMode(Scenario.SYNC_MODE_LIVE)
                
                part_tracker = AlgorithmFactory.createParticleTracker(seq)
                self.part_probe_init = ProbeFactory.createParticleProbe(seq, part_tracker)
                #self.part_probe_init.setPhaseCoordinates( PhaseVector(1.0, 0.0, 0.0, 0.0, 0.0, 0.0) )
                
                       

                self.bpms = seq.getAllNodesOfType("BPM")    
                      
                caF = ChannelFactory.defaultFactory()

                    
                
                self.channels = []
                self.bpm_pos = []
                for bpm in self.bpms:
                    #print bpm.getId()
                    self.channels.append(caF.getChannel(bpm.getId() + ":xAvg"))
                    self.bpm_pos.append(bpm.getPosition())

                    

                
                #for i in range(11):
                    #self.bpms.append(self.bpms.get(i))
                #print "bad = ", self.bpms.get(11)    
                #self.cor = ChannelCorrelator(0.1)
                



                #self.bpm_pos = []
                #for bpm in self.bpms:
                #    self.cor.addChannel(bpm.getId() + ":xAvg")
                #    self.bpm_pos.append(bpm.getPosition())
                    

                    
                
                #print self.bpm_pos     
                #self.model = self.getModelParameters()
		#self.scl_long_tuneup_controller = scl_long_tuneup_controller

		self.main_panel = JPanel(BorderLayout())
                self.energy_text = em_controller.scl_long_tuneup_energy_meter_controller.init_start_stop_panel.energy_text
                
                self.cav_combo_box= JComboBox(self.cavs)
                self.cav_combo_box.setSelectedItem(self.cavs[len(self.cavs)-1])

		self.bp = Buttons_Panel(self)

                plots_panel = JPanel(GridLayout(2,1))


		self.scan_plot = FunctionGraphsJPanel()
                self.disp_plot = FunctionGraphsJPanel()
                #self.disp_plot.setLimitsAndTicksX(0, 100, 0.1)
                
                self.disp_plot.setLegendVisible(True)
                
                self.scan_plot.setName("RF phase scan")
                self.scan_plot.setAxisNames("Cavity phase [deg.]","Eout [MeV]")	
                
                self.disp_plot.setName("HEBT2")
                self.disp_plot.setAxisNames("z [m]","BPMx [mm], Dispersionx [m]")	
                
                self.scan_plot.addVerticalLine(0,Color.RED)
                self.disp_plot.addVerticalLine(0,Color.RED)
                self.disp_plot.setVerticalLineValue(self.meas_node.getPosition(),0)

                self.scan_data = BasicGraphData()                
                self.disp_data = BasicGraphData()
                
                self.disp_data.setGraphProperty(self.disp_plot.getLegendKeyString(), "model D [m]")
                self.dispz_data = BasicGraphData()
                self.dispz_data.setGraphProperty(self.disp_plot.getLegendKeyString(), "model dD/dz")
                self.disp_data.setGraphColor(Color.RED)
                self.disp_data.setDrawPointsOn(False)
                
                self.dispz_data.setGraphColor(Color.BLUE)
                self.dispz_data.setDrawPointsOn(False)
                #self.disp_data.setGraphPointSize(4)
                
                self.disp_data_meas = BasicGraphData()
                self.disp_data_meas.setGraphProperty(self.disp_plot.getLegendKeyString(), "measured D [m]")
                self.disp_data_meas.setGraphColor(Color.RED)
                self.disp_data_meas.setDrawLinesOn(False)
                
                self.disp_data.setGraphPointShape(Ellipse2D.Double(0,0,8,8))
                
                self.bpm_data_live = BasicGraphData()
                self.bpm_data_live.setGraphProperty(self.disp_plot.getLegendKeyString(), "BPMs-X [mm]")
                self.bpm_data_dead = BasicGraphData()

                self.bpm_data_live.setGraphColor(Color.black)
                self.bpm_data_dead.setGraphColor(Color.CYAN.darker().darker())
                self.bpm_data_live.setDrawLinesOn(False)
                self.bpm_data_dead.setDrawLinesOn(False)
                
                self.scan_plot.addGraphData(self.scan_data)
                self.disp_plot.addGraphData(self.disp_data)
                self.disp_plot.addGraphData(self.dispz_data)
                self.disp_plot.addGraphData(self.disp_data_meas)
                self.disp_plot.addGraphData(self.bpm_data_live)
                self.disp_plot.addGraphData(self.bpm_data_dead)
               

                plots_panel.add(self.scan_plot)
                plots_panel.add(self.disp_plot)

		self.main_panel.add(self.bp,BorderLayout.NORTH)
		self.main_panel.add(plots_panel,BorderLayout.CENTER)


                self.monitorEnergy()
                self.monitorBPMs()

                
                
        def calculate_model_disp(self):
        
            #self.disp_meas.meas_node.getId()
            #if st1.getElementId() == self.disp_meas.meas_node.getId():
            #print z, dispx, dispxz
            meas_node = self.nodes_combo_box.getSelectedItem()
            
            pos = meas_node.getPosition()
            d = self.disp_data.getValueY(pos)
            dz = self.dispz_data.getValueY(pos)
            self.bp.disp_text.setText(String.format("%.5f", d))
            self.bp.dispz_text.setText(String.format("%.5f", dz))
            
            
        def calculate_measured_disp(self):
        
            #self.disp_meas.meas_node.getId()
            #if st1.getElementId() == self.disp_meas.meas_node.getId():
            #print z, dispx, dispxz
            meas_node = self.nodes_combo_box.getSelectedItem()
            pos = meas_node.getPosition()
            d = self.disp_data_meas.getValueY(pos)
            dz = self.disp_data_meas.getValueDerivativeY(pos)
            self.bp.dispm_text.setText(String.format("%.5f", d))
            self.bp.dispzm_text.setText(String.format("%.5f", dz))
                




        
        def monitorEnergy(self):
            class Thr(Runnable):
                def run(self):
                    _monitorEnergy()
            Thread(Thr()).start()

            def _monitorEnergy():
                
                while (1 < 2):
                    en = self.getNewEnergy()
                    self.buttons_panel.energy_text.setText(String.format("%.5f", en))

        



        def monitorBPMs(self):
            class Thr(Runnable):
                def run(self):
                    _monitorBPMs()
            Thread(Thr()).start()

            def _monitorBPMs():
            
            
                pos1X = []
                for ch in self.channels:
                    pos1X.append(ch.getValDbl())

                
                while (1 < 2):

                    
                    pos2X = []
                    for ch in self.channels:
                        pos2X.append(ch.getValDbl())
                        
                    #print self.bpm_pos,pos2X
                    
                    bpm_pos_live = []
                    bpm_pos_dead = []
                    posX_live = []
                    posX_dead = []
                    for i in range(len(pos2X)):
                        if pos2X[i] == pos1X[i]:
                            posX_dead.append(pos2X[i])
                            bpm_pos_dead.append(self.bpm_pos[i])
                        else:
                            posX_live.append(pos2X[i])
                            bpm_pos_live.append(self.bpm_pos[i])
                         
                    self.bpm_data_live.updateValues(bpm_pos_live, posX_live)
                    self.bpm_data_dead.updateValues(bpm_pos_dead, posX_dead)
                    
                    pos1X = pos2X


                    time.sleep(1.1)


        def getNewEnergy(self):

            val = new_val = self.energy_text.getValue()
            while(new_val == val):
                new_val = self.energy_text.getValue()
                time.sleep(0.01)

            return new_val
                    


                    

                

            
        


                
        def getDeltaEnergy(self):

            pars = self.harmonicsAnalyzer.getParamsArr()
            phase0 = self.cav_combo_box.getSelectedItem().getCavPhaseSetPoint()

                

            e1 = pars[0] + pars[1]*math.cos(2*math.pi*(phase0 + pars[2])/360)
            e2 = pars[0] + pars[1]*math.cos(2*math.pi*(phase0 + self.dphi + pars[2])/360)
            
            return e2 - e1


        def getDeltaP_P(self):

            pars = self.harmonicsAnalyzer.getParamsArr()
            phase0 = self.cav_combo_box.getSelectedItem().getCavPhaseSetPoint()
                

            T1 = pars[0] + pars[1]*math.cos(2*math.pi*(phase0 + pars[2])/360)
            T2 = pars[0] + pars[1]*math.cos(2*math.pi*(phase0 + self.dphi + pars[2])/360)

            m = 938.256 + 2*0.511
            E1 = m + T1
            E2 = m + T2
            P1 = math.sqrt(E1*E1 - m*m)
            P2 = math.sqrt(E2*E2 - m*m)
            Pav = (P1 + P2)/2
            
            return (P2 - P1)/Pav



        def getModelParameters(self):
            return


		
	def getMainPanel(self):
            return self.main_panel
		
	def updateTables(self):
            return
		
	def writeDataToXML(self,root_da):
            return

	def readDataFromXML(self,root_da):
            return
						
	def isWorthToSave(self):
		if(len(self.bpm_wrappers_holder.em_bpm_wrpprs) > 0):
			return true
		else:
			return false
			
	def clean(self):
		return	

"""
class Start_Scan_Button_Listener(ActionListener):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas

	def actionPerformed(self,actionEvent):	

		scan_runner = Scan_Runner(self.disp_meas) 
		thr = Thread(scan_runner)
		thr.start()
                
                



class Scan_Runner(Runnable):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas

	
	def run(self):

            print "running"	

"""			
#------------------------------------------------------------------------
#           Auxiliary panels
#------------------------------------------------------------------------		
class Buttons_Panel(JPanel):
	def __init__(self,disp_meas):

		self.setLayout(BorderLayout())
		#----etched border
		etched_border = BorderFactory.createEtchedBorder()
		titled_border = BorderFactory.createTitledBorder(etched_border,"Phase scan, Dispersion measurement")
		self.setBorder(titled_border)	
		#----- buttons panel
		self.buttons_panel =  JPanel(FlowLayout(FlowLayout.LEFT,10,3))
		
		start_scan_button = JButton("Start Scan")
		start_scan_button.addActionListener(Start_Scan_Button_Listener(disp_meas))

                disp_meas_button = JButton("Measure dispersion")
                disp_meas_button.addActionListener(Disp_Meas_Button_Listener(disp_meas))
                
                



                #self.cav_combo_box= JComboBox(disp_meas.cavs)
                self.buttons_panel.add(disp_meas.cav_combo_box)
		self.buttons_panel.add(start_scan_button)
                self.buttons_panel.add(disp_meas_button)


		#----- energy panel--------
		energy_panel =  JPanel(FlowLayout(FlowLayout.LEFT,10,3))

		energy_lbl = JLabel("<html>LINAC E<SUB>kin</SUB>[MeV]=<html>",JLabel.RIGHT)
		self.energy_text = JLabel()
                self.energy_text.setPreferredSize(Dimension(80,20))
                self.energy_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))

		nmeas_lbl = JLabel("<html>aver.size of x<sub>BPM</sub> :<html>",JLabel.RIGHT)
		self.nmeas_text = IntegerInputTextField(disp_meas.nmeas, DecimalFormat("####"),4)
                self.nmeas_text.addActionListener(nmeas_Window_Listener(self, disp_meas))


		dphi_lbl = JLabel("<html>&Delta&phi<SUB>RF</SUB> [deg.] =<html>",JLabel.RIGHT)
		self.dphi_text = DoubleInputTextField(disp_meas.dphi, DecimalFormat("####"),4)
                self.dphi_text.addActionListener(Dphi_Window_Listener(self, disp_meas))

                

		den_lbl = JLabel("<html>&Delta;T [MeV] =<html>",JLabel.RIGHT)
                self.den_text = JLabel()
                self.den_text.setPreferredSize(Dimension(80,20))
                self.den_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))
                
                #self.den_text.setEditable(False)
                #self.den_text.setFocusable(False)
                #self.den_text.setBackground(Color.LIGHT_GRAY)
		dpp_lbl = JLabel("<html><sup>&Delta;p</sup>&frasl;<sub>p</sub> =<html>",JLabel.RIGHT)
		self.dpp_text = JLabel()
                self.dpp_text.setPreferredSize(Dimension(80,20))
                self.dpp_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))
                
                
                disp_lbl = JLabel("<html>D<SUB>x</SUB>[m] =<html> ",JLabel.RIGHT)
                dispz_lbl = JLabel("<html>&part;D<SUB>x</SUB>/&part;z =<html> ",JLabel.RIGHT)                

		self.disp_text = JLabel()
                self.disp_text.setPreferredSize(Dimension(100,20))
                self.disp_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))

		self.dispz_text = JLabel()
                self.dispz_text.setPreferredSize(Dimension(100,20))
                self.dispz_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))

		self.dispm_text = JLabel()
                self.dispm_text.setPreferredSize(Dimension(100,20))
                self.dispm_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))

		self.dispzm_text = JLabel()
                self.dispzm_text.setPreferredSize(Dimension(100,20))
                self.dispzm_text.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY))                
                
                self.disp_panel = JPanel(GridLayout(3,3))
                etched_border = etched_border = BorderFactory.createEtchedBorder()
                self.disp_panel.setBorder(BorderFactory.createTitledBorder(etched_border,"Dispersion for selected node")) 
                

                
                #title_node = BorderFactory.createTitledBorder("" + disp_meas.meas_node.getId())
                #title_node.setTitleJustification(TitledBorder.CENTER)
                #self.disp_panel.setBorder(title_node)
	
		self.buttons_panel.add(energy_lbl)
		self.buttons_panel.add(self.energy_text)

		energy_panel.add(dphi_lbl)
		energy_panel.add(self.dphi_text)
                
		energy_panel.add(nmeas_lbl)
		energy_panel.add(self.nmeas_text)

		energy_panel.add(den_lbl)
		energy_panel.add(self.den_text)

		energy_panel.add(dpp_lbl)
		energy_panel.add(self.dpp_text)

                    
                self.disp_panel.add(disp_meas.nodes_combo_box)
                self.disp_panel.add(JLabel("<html> model <font color='red'> &mdash&mdash&mdash&mdash</font> <html>", JLabel.CENTER))
                self.disp_panel.add(JLabel("<html> measured <font color='red'> <font size='5'> &middot &middot &middot &middot </font> <html>", JLabel.CENTER))                
                

		self.disp_panel.add(disp_lbl)
                
		self.disp_panel.add(self.disp_text)
                self.disp_panel.add(self.dispm_text)
                		                                
		self.disp_panel.add(dispz_lbl)
		self.disp_panel.add(self.dispz_text)
		self.disp_panel.add(self.dispzm_text)  
                
                #self.disp_label_panel = JPanel(GridLayout(6,1))
                #self.disp_label_panel.add(JLabel())
                #self.disp_label_panel.add(disp_lbl)
                #self.disp_label_panel.add(dispz_lbl)
                #self.disp_label_panel.add(JLabel())
                #self.disp_label_panel.add(dispm_lbl)
                #self.disp_label_panel.add(dispzm_lbl)
                
                #energy_panel.add(self.disp_panel)              

		#----- main panel
                
                left_control_panel = JPanel(BorderLayout())
                
                left_control_panel.add(self.buttons_panel,BorderLayout.NORTH)
		left_control_panel.add(energy_panel,BorderLayout.CENTER)
                
                
                def_panel=JPanel(FlowLayout())

                self.disp_definition = JLabel("<html>&Delta;x<SUB>BPM</SUB> = &mdash;D<SUB>x</SUB><sup>&Delta;p</sup>&frasl;<sub>p</sub><html>", JLabel.CENTER)
                self.disp_definition.setPreferredSize(Dimension(150,50))
                self.disp_definition.setBorder(BorderFactory.createLineBorder(Color.RED))    
                


                def_panel.add(self.disp_definition)
                left_control_panel.add(def_panel,BorderLayout.SOUTH)
                
                
		#self.add(self.buttons_panel,BorderLayout.NORTH)
		self.add(left_control_panel,BorderLayout.CENTER)
                self.add(self.disp_panel,BorderLayout.EAST)


                
                



class Dphi_Window_Listener(ActionListener):
	def __init__(self,button_panel,disp_meas):
		self.disp_meas = disp_meas
                self.button_panel = button_panel

	def actionPerformed(self,actionEvent):                

                self.disp_meas.dphi = self.button_panel.dphi_text.getValue()
                
                self.disp_meas.de = self.disp_meas.getDeltaEnergy()
                self.button_panel.den_text.setText(String.format("%.5f", self.disp_meas.de))
                
                self.disp_meas.dpp = self.disp_meas.getDeltaP_P()
                self.button_panel.dpp_text.setText(String.format("%.5f", self.disp_meas.dpp))
                
               
                
class nmeas_Window_Listener(ActionListener):
	def __init__(self,button_panel,disp_meas):
		self.disp_meas = disp_meas
                self.button_panel = button_panel
	def actionPerformed(self,actionEvent):
                self.disp_meas.nmeas = self.button_panel.nmeas_text.getValue()
                
               
class Change_disp_meas_Listener(ActionListener):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas
                
	def actionPerformed(self,actionEvent):
        
                meas_node = self.disp_meas.nodes_combo_box.getSelectedItem()
                self.disp_meas.disp_plot.setVerticalLineValue(meas_node.getPosition(),0)
		self.disp_meas.calculate_model_disp()
                self.disp_meas.calculate_measured_disp()
                

	def valueChanged(self,listSelectionEvent):	
        
                self.disp_meas.disp_plot.setVerticalLineValue(self.disp_meas.meas_node.getPosition(),0)
                #title_node = BorderFactory.createTitledBorder("dispersions at " + self.disp_meas.meas_node.getId())
                #title_node.setTitleJustification(TitledBorder.CENTER)
                
                #self.title_node = BorderFactory.createTitledBorder("Measured and model dispersions at  " + disp_meas.meas_node.getId())
                #self.disp_meas.bp.disp_panel.setBorder(title_node)                
                
                
		self.disp_meas.calculate_model_disp()
                self.disp_meas.calculate_measured_disp()
                
            
    

class Start_Scan_Button_Listener(ActionListener):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas

	def actionPerformed(self,actionEvent):	

		scan_runner = Scan_Runner(self.disp_meas)
		thr = Thread(scan_runner)
		thr.start()


class Scan_Runner(Runnable):
	def __init__(self,disp_meas):
            self.disp_meas = disp_meas

	
	def run(self):    
        
            #print "tu tu"
            self.disp_meas.scan_data.removeAllPoints()
            

            cav = self.disp_meas.cav_combo_box.getSelectedItem()
            phase0 = cav.getCavPhaseSetPoint()


            self.disp_meas.scan_plot.setVerticalLineValue(phase0,0)



            for i in range(18):

                phase = -180 + i*20

                cav.setCavPhase(phase)
                time.sleep(3.1)
                
                #en = self.disp_meas.getNewEnergy()

                    
                self.disp_meas.scan_data.addPoint(phase, en)
                #self.disp_meas.scan_data.addPoint(phase, 1300 + 10*math.cos(2*math.pi*(phase + 67)/360))

    
            cav.setCavPhase(phase0)

            err = self.disp_meas.harmonicsAnalyzer.analyzeData(self.disp_meas.scan_data)
            #print self.disp_meas.harmonicsAnalyzer.getParamsArr()
            
            
            self.disp_meas.dphi = self.disp_meas.bp.dphi_text.getValue()
            
            self.disp_meas.de = self.disp_meas.getDeltaEnergy()
            self.disp_meas.bp.den_text.setText(String.format("%.5f", self.disp_meas.de))


            self.disp_meas.dpp = self.disp_meas.getDeltaP_P()
            self.disp_meas.bp.dpp_text.setText(String.format("%.5f", self.disp_meas.dpp))
            
            
            
            
            
            self.disp_meas.disp_data.removeAllPoints()
            
            
            pars = self.disp_meas.harmonicsAnalyzer.getParamsArr()
            T0 = pars[0] + pars[1]*math.cos(2*math.pi*(phase0 + pars[2])/360)    
            dT = 1.0
            
            m = 938.256 + 2*0.511
            E1 = m + T0
            E2 = m + T0 + dT
            P1 = math.sqrt(E1*E1 - m*m)
            P2 = math.sqrt(E2*E2 - m*m)
            
            dpp = 2*(P2 - P1)/(P1 + P2)
                    
            
            self.disp_meas.part_probe_init.setKineticEnergy(T0*1e6)
            part_probe = ParticleProbe(self.disp_meas.part_probe_init)
            self.disp_meas.model.setProbe(part_probe)
            self.disp_meas.model.resync()
            self.disp_meas.model.run()
            traj1 = self.disp_meas.model.getTrajectory() 
            
            self.disp_meas.part_probe_init.setKineticEnergy((T0 + dT)*1e6)
            part_probe = ParticleProbe(self.disp_meas.part_probe_init)
            self.disp_meas.model.setProbe(part_probe)
            self.disp_meas.model.resync()
            self.disp_meas.model.run()
            traj2 = self.disp_meas.model.getTrajectory()              

            num = traj1.numStates()
            

            for i in range(num-1):
                st1 = traj1.stateWithIndex(i+1)
                st2 = traj2.stateWithIndex(i+1) 
                z = st1.getPosition()     
                dispx =  -(st2.getPhaseCoordinates().getx() - st1.getPhaseCoordinates().getx())/dpp     
                dispxz = -(st2.getPhaseCoordinates().getxp() - st1.getPhaseCoordinates().getxp())/dpp
                self.disp_meas.disp_data.addPoint(z, dispx)
                self.disp_meas.dispz_data.addPoint(z, dispxz)
                    
            self.disp_meas.calculate_model_disp()
                    
                    
            
                    
                  
   



class Disp_Meas_Button_Listener(ActionListener):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas

	def actionPerformed(self,actionEvent):	

		disp_meas_runner = Disp_Meas_Runner(self.disp_meas)
		thr = Thread(disp_meas_runner)
		thr.start()	


class Disp_Meas_Runner(Runnable):
	def __init__(self,disp_meas):
		self.disp_meas = disp_meas

	
	def run(self):

            #print "disp meas"
            self.disp_meas.disp_data_meas.removeAllPoints()
            
            nb = len(self.disp_meas.bpm_pos)
            nm = self.disp_meas.nmeas
            
            x1 = [0] * nb
            x2 = [0] * nb
          

            cav = self.disp_meas.cav_combo_box.getSelectedItem()
            phase0 = cav.getCavPhaseSetPoint()

            
            for i in range(nm): 
                time.sleep(1.1)
                for j in range(nb):
                    x1[j] += self.disp_meas.channels[j].getValDbl()/nm                                 
                                   
            cav.setCavPhase(phase0 + self.disp_meas.dphi)

        
            for i in range(nm): 
                time.sleep(1.1)
                for j in range(nb):
                    x2[j] += self.disp_meas.channels[j].getValDbl()/nm  
                       
            cav.setCavPhase(phase0)   
        
            for j in range(nb):
                pos = self.disp_meas.bpm_pos[j]
                disp = -0.001*(x2[j] - x1[j])/self.disp_meas.dpp               
                self.disp_meas.disp_data_meas.addPoint(pos, disp)
                
                
            self.disp_meas.calculate_measured_disp()
            
            

            

                
            
            
            


            


            

			


		

		


	

