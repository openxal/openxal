from javax.swing import JTable, DefaultCellEditor, JTextField, ListSelectionModel, JLabel, BorderFactory, JButton, JPanel, BoxLayout, JTabbedPane, GroupLayout, LayoutStyle, JRadioButton, JScrollPane, Box, JCheckBox
from xal.extension.application import XalDocument, ApplicationAdaptor
from xal.extension.application.smf import AcceleratorApplication, AcceleratorDocument, AcceleratorWindow
from xal.smf.data import XMLDataManager
from xal.tools.xml import XmlDataAdaptor
import sys, os, math, random, time
from javax.swing.event import ChangeListener


thisDir = os.path.dirname(os.path.abspath( __file__ ))
sys.path.append(thisDir)


from TunePane import TunePane
from IKickPane import IKickPane
from OrbitControlPane import OrbitControlPane

from ScbApp import RingOptics
import pickle
import gzip
import marshal
import itertools
from array import array

"""
ro = RingOptics()



frame = JFrame( "Ring injection control" )
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
frame.setBounds( 100, 100, 1400, 900 )

tabbedPane = JTabbedPane()

tabbedPane.add("IKick calibration",IKickPane(ro).panel)
tabbedPane.add("Tune control",TunePane(ro).panel)
tabbedPane.add("Injection orbit control",JPanel())

frame.add(tabbedPane)
frame.show()
"""




#p1.updateValues([1,2,3], double[5,6,7])
#for i in range(100):
#    p1.graph.updateValues([1,2,3], [5+i,6+i,7+i])
#    time.sleep(1)




#----------------------------------------------------------------------------------------------------------------------------------------------------------------
# This script is a Template for the OpenXAL Application

import sys
import math
import types
import time
import random
import os

from java.lang import Boolean
#from javax.swing import *
from java.awt import BorderLayout
from java.awt import Color
from java.awt import Dimension
from java.awt.event import WindowAdapter
from java.beans import PropertyChangeListener
from java.awt.event import ActionListener
from java.util import ArrayList
from java.io import File
from java.net import URL

from xal.extension.application import XalDocument, ApplicationAdaptor
from xal.extension.application.smf import AcceleratorApplication, AcceleratorDocument, AcceleratorWindow

from xal.smf.data import XMLDataManager
from xal.smf import AcceleratorSeqCombo

#-----local jython libraries import----
from time_and_date_lib import DateAndTimeText

false = Boolean("false").booleanValue()
true = Boolean("true").booleanValue()
null = None

#-------------------------------------------------------------------
# Local Classes that are not subclasses of XAL Accelerator Framework
#-------------------------------------------------------------------

class Empty_Window:
	def __init__(self,empty_document):
		#--- empty_document the parent document for all controllers
		self.empty_document = empty_document
		self.frame = null
		self.centerPanel = JPanel(BorderLayout())		
		self.mainPanel = JPanel(BorderLayout())
		self.time_txt = DateAndTimeText()
		self.messageTextField = JTextField()
		#---------------------------------------
		timePanel = JPanel(BorderLayout())
		timePanel.add(self.time_txt.getTimeTextField(),BorderLayout.CENTER)		
		self.messageTextField.setForeground(Color.red)
		self.centerPanel.add(self.mainPanel,BorderLayout.CENTER)
		tmpP = JPanel(BorderLayout())
		tmpP.add(self.messageTextField, BorderLayout.CENTER)
		tmpP.add(timePanel, BorderLayout.WEST)
		self.centerPanel.add(tmpP,BorderLayout.SOUTH)
		
	def setFrame(self,xal_frame,mainPanel):
		self.frame = xal_frame
		mainPanel.add(self.centerPanel,BorderLayout.CENTER)
		
	def getMainPanel(self):
		return self.mainPanel
		 
	def getMessageTextField(self):
		return self.messageTextField

class Empty_Document:
	#This is a place where you put everything that it is yours logic and GUI
	def __init__(self):
            
            
            
                self.ro = RingOptics()  

		self.empty_window = null
		#---- place to create all subcontrollers
		#---- ??????
		#--------fill out the tabbed panel
		self.tabbedPane = JTabbedPane()
                self.ikp = IKickPane(self.ro)
                self.ocp = OrbitControlPane(self.ro)
                self.tp = TunePane(self.ro)
                
                self.tabbedPane.add("IKick calibration", self.ikp.panel)
                self.tabbedPane.add("Tune control",self.tp.panel)
                self.tabbedPane.add("Injection beam control and measurement",self.ocp.panel)
                
                
                #update bpm tables selection automatically
                ikp = self.ikp
                ocp = self.ocp
                class TSelectChange(ChangeListener):
                   def stateChanged(self, e):
                       ikp.updateBPMselection()
                       ocp.updateBPMselection()
                self.tabbedPane.addChangeListener(TSelectChange())


	def setWindow(self,empty_window):
		self.empty_window = empty_window
		self.empty_window.getMainPanel().add(self.tabbedPane,BorderLayout.CENTER)		
		
	def getWindow(self):
		return self.empty_window
		
	def getMessageTextField(self):
		if(self.empty_window != null):
			return self.empty_window.getMessageTextField()
		else:
			return null

#-------------------------------------------------
# SUBCLASSES of XAL Accelerator Framework
#-------------------------------------------------

#-------------------------------------------------
#        DOCUMENT Class
#-------------------------------------------------	
class Empty_OpenXAL_Document(AcceleratorDocument):
	def __init__(self,url = null):
		self.mainPanel = JPanel(BorderLayout())

		#==== set up accelerator 
		if(not self.loadDefaultAccelerator()):
			self.applySelectedAcceleratorWithDefaultPath("/default/main.xal")
			
		self.empty_document = Empty_Document()
		self.empty_window = Empty_Window(self.empty_document)
		
		if(url != null):
			self.setSource(url)
			self.readEmpty_Document(url)
			#super class method - will show "Save" menu active
			if(url.getProtocol().find("jar") >= 0):
				self.setHasChanges(false)
			else:
				self.setHasChanges(true)
				
	def makeMainWindow(self):
		self.mainWindow = Empty_OpenXAL_Window(self)
		self.mainWindow.getContentPane().setLayout(BorderLayout())
		self.mainWindow.getContentPane().add(self.mainPanel,BorderLayout.CENTER)			
		self.empty_window.setFrame(self.mainWindow,self.mainPanel)
		self.empty_document.setWindow(self.empty_window)
		self.mainWindow.setSize(Dimension(1800, 1100))

	def saveDocumentAs(self,url):
            
                ro = self.empty_document.ro

                wfk = []
                d = ro.d['BPMWaveForms']
                filewf = open(url.getPath().replace('.ric','.waveforms'), "wb")
                
                while type(d) is dict:
                    wfk.append(d.keys())
                    d = d[d.keys()[0]]

                ro.d['WaveFormsKeys'] = wfk
                

                for keys in itertools.product(*wfk):
                    d = ro.d['BPMWaveForms']
                    for key in keys:
                        d = d[key]
                    #filewf.write(array('d', d))
                    filewf.write(d)
                    
                
                filewf.close()

                ds = {k: ro.d[k] for k in ro.d.keys() if k != 'BPMWaveForms'}

                wfile = open( url.getPath(), "wb" )
                pickle.dump(ds, wfile )
                wfile.close()

	def readEmpty_Document(self,url):
            
                ro = self.empty_document.ro
                ikp = self.empty_document.ikp
                
                rfile = open( url.getPath(), "rb" )
                ro.d = pickle.load( rfile )
                rfile.close()
                

                
                ro.d['BPMWaveForms'] = {}
                wfk = ro.d['WaveFormsKeys']
                


                filewf = open( url.getPath().replace('.ric','.waveforms'), "rb" )
                


                for keys in itertools.product(*wfk):
                    d = ro.d['BPMWaveForms']
                    for key in keys[:-1]:
                        if not d.has_key(key):
                            d[key] = {}
                        d = d[key]

                    d[keys[-1]] = array('d')
                    d[keys[-1]].fromfile(filewf, ro.d['WFLength'])

                    
                filewf.close()
                

                #ro.d['WFLength'] = 40
                #ro.d['Npulses'] = 50
                

                
                
                """
                for id in ro.d['IKickParams'].keys():
                    if not ro.d['IKickParams'][id].has_key('usepulses'):
                        ro.d['IKickParams'][id]['usepulses'] = {}
                        ro.d['IKickParams'][id]['usepulses']['voltage-1'] = ['pulse-' + str(i) for i in range(100)]
                        ro.d['IKickParams'][id]['usepulses']['voltage-2'] = ['pulse-' + str(i) for i in range(100)]

                        
                #temporary code
                if not ro.d.has_key('useBPMs'):
                    ro.d['useBPMs'] = []
                    for bpmId in ro.allBPMsIds:
                        if bpmId not in ['Ring_Diag:BPM_D10','Ring_Diag:BPM_A13','Ring_Diag:BPM_A10']:
                            ro.d['useBPMs'].append(bpmId)
                """
                
                #for id, pvlog in [('Ring_Mag:IKickV02', 47151292L), ('Ring_Mag:IKickV01', 47151267L), ('Ring_Mag:IKickH04', 47151230L), ('Ring_Mag:IKickH03', 47151211L), ('Ring_Mag:IKickH02', 47151171L), ('Ring_Mag:IKickH01', 47151146L), ('Ring_Mag:IKickV04', 47151349L), ('Ring_Mag:IKickV03', 47151331L)]:
                #    ro.d['IKickParams'][id]['pvLoggerId'] = pvlog
                """
                ro.d['IKickParams']['Ring_Mag:IKickH01']['voltage-1'] = 14.53016
                ro.d['IKickParams']['Ring_Mag:IKickH02']['voltage-1'] = 8.88123
                ro.d['IKickParams']['Ring_Mag:IKickH03']['voltage-1'] = 13.15043
                ro.d['IKickParams']['Ring_Mag:IKickH04']['voltage-1'] = 17.46992
                ro.d['IKickParams']['Ring_Mag:IKickV01']['voltage-1'] = 13.87936
                ro.d['IKickParams']['Ring_Mag:IKickV02']['voltage-1'] = 16.03659
                ro.d['IKickParams']['Ring_Mag:IKickV03']['voltage-1'] = 12.50613
                ro.d['IKickParams']['Ring_Mag:IKickV04']['voltage-1'] = 12.41964
                
                ro.d['IKickParams']['Ring_Mag:IKickH01']['voltage-2'] = 14.53016+1
                ro.d['IKickParams']['Ring_Mag:IKickH02']['voltage-2'] = 8.88123+1
                ro.d['IKickParams']['Ring_Mag:IKickH03']['voltage-2'] = 13.15043+1
                ro.d['IKickParams']['Ring_Mag:IKickH04']['voltage-2'] = 17.46992+1
                ro.d['IKickParams']['Ring_Mag:IKickV01']['voltage-2'] = 13.87936+1
                ro.d['IKickParams']['Ring_Mag:IKickV02']['voltage-2'] = 16.03659+1
                ro.d['IKickParams']['Ring_Mag:IKickV03']['voltage-2'] = 12.50613+1
                ro.d['IKickParams']['Ring_Mag:IKickV04']['voltage-2'] = 12.41964+1
                    
                for id in ro.psIds:
                    ro.d['BLQuadCorrections'][id] = 1.0
                """            

                PvLogId = max([ro.d['IKickParams'][id]['pvLoggerId'] for id in ro.d['IKickParams'].keys()])
                print PvLogId
                #PvLogId = 47151349
                


                
                
                options = ['Live magnets','Magnets from pvlogger of IKickers scan']
                answer = JOptionPane.showOptionDialog(JFrame(),"Choose magnets for Ring optics","Ring optics chooser", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, None,  options, options[0])

                

                if answer == JOptionPane.NO_OPTION:
                    ro.getOpticsFromPV(PvLogId)
                    #for kickid in ro.IkickIds:
                    #    print kickid, ro.d['IKickParams'][kickid]['voltage-1']
                    #    print kickid, ro.d['IKickParams'][kickid]['voltage-2']
                else:
                    ro.getOpticsFromPV(-1)
                    
                    for kickid in ro.IkickIds:
                        ch = ro.caF.getChannel(kickid[:9] + "PS_" + kickid[9:]+":UTCA:AMPL")
                        val = round(ch.getValDbl(),7)
                        ro.d['IKickParams'][kickid]['voltage-1'] = val
                        ro.d['IKickParams'][kickid]['voltage-2'] = val + 1.0
                




                ro.SetQuadFields()

                ro.tuneX, ro.tuneY = ro.getTunes()
                ro.getMatrFoilToNode()
                ro.tuneX0, ro.tuneY0 = ro.tuneX, ro.tuneY

                
                ro.calculateAllAverageWaveforms()

                
                #bpm table consist of fixed bpms
                ikp.updateBPMselection()
                ikp.plotTunes()
                ikp.plotClosedOrbits(ro.IkickIds[0])
                ikp.updatePulsetable(ro.IkickIds[0])
                ikp.updatePulseselection(ro.IkickIds[0])
                ikp.plotWaveform(ro.IkickIds[0], ro.allBPMsIds[0])
                
                ikp.plotIKickPhaseSpaceFoil()
                ikp.table14.getModel().fireTableDataChanged()
                
                if ikp.table12.getModel().getRowCount() > 0:
                    ikp.plotPulseWaveform(ro.IkickIds[0], ro.allBPMsIds[0], 0)


                
                #for i in range(8):
                    #if ro.trs.childAdaptor("scan-" + str(i)).stringValue("status") != "optimized":
                        #ro.tripleValueChanged(i)




        #PvLogId = self.runAdaptor.longValue("pvLoggerId")
        
        
        #self.getOpticsFromPV(PvLogId)
                        
                    
                #ro.NturnMax = len(chKicks.get(0).childAdaptors().get(0).childAdaptors().get(0).childAdaptors().get(0).childAdaptor("position-x").doubleArray("ring-turns"))

                #if (ro.Nturn > ro.NturnMax):
                    #ro.Nturn = ro.NturnMax
  
                

#--------------------------------------------------
#        WINDOW Class
#--------------------------------------------------				
class Empty_OpenXAL_Window(AcceleratorWindow):
	def __init__(self,empty_openxal_document):
		AcceleratorWindow.__init__(self,empty_openxal_document)
		
	def getMainPanel(self):
		return self.document.mainPanel
	
#--------------------------------------------------
#        MAIN Class
#--------------------------------------------------
class Empty_OpenXAL_Main(ApplicationAdaptor):
	def __init__(self):
		ApplicationAdaptor.__init__(self)
		script_dir = os.path.dirname(os.path.realpath(__file__))
		self.setResourcesParentDirectoryWithPath(script_dir)
	
	def readableDocumentTypes(self):
		return ["ric",]
		
	def writableDocumentTypes(self):
		return self.readableDocumentTypes()

	def newEmptyDocument(self, *args):
		if len( args ) > 0:
			return ApplicationAdaptor.newEmptyDocument(self,*args)
		else:
			return self.newDocument(null)

	def newDocument(self,location):
		return Empty_OpenXAL_Document(location)

	def applicationName(self):
		return "Ring Injection Control"
				
AcceleratorApplication.launch(Empty_OpenXAL_Main())
