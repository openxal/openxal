from xal.smf.data import XMLDataManager 
from xal.sim.scenario import ProbeFactory
from xal.sim.scenario import Scenario
from xal.smf import AcceleratorSeqCombo
from xal.ca import ChannelFactory
from xal.sim.scenario import AlgorithmFactory
from xal.tools.xml import XmlDataAdaptor
from xal.tools.data import DataAdaptor
from xal.service.pvlogger import RemoteLoggingCenter
from xal.service.pvlogger.sim import PVLoggerDataSource
from java.util import ArrayList
from java.lang import Thread, Boolean
from xal.smf import AcceleratorSeqCombo
from xal.sim.scenario import ScenarioGenerator
from xal.model.alg import TransferMapTracker
from xal.tools.beam import PhaseMatrix
from xal.tools.beam import PhaseVector
from time import gmtime, strftime
import sys, math, os, time
from xal.tools.math import SquareMatrix
#from xal.app.pta import MainApplication
from Jama import Matrix
from array import array

from xal.extension.fit import GaussianSinusoidFit
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
from xal.extension.widgets.plot import BasicGraphData, FunctionGraphsJPanel
from java.io import File
from javax.swing import JFileChooser, JFrame, JPanel



thisDir = os.path.dirname(os.path.abspath( __file__ ))
sys.path.append(thisDir)

from OrthVector import OrthVector
from scanEvent import scanEvent

ov = OrthVector()


class RingOptics:
    
    def __init__(self):
                
        self.doc = XmlDataAdaptor.newEmptyDocumentAdaptor()
        #self.runAdaptor = self.doc.createChild('run')
        
        self.dav = {}
        self.d = {'BPMWaveForms':{}, 'BLQuadCorrections':{},'IKickParams':{},'TripleScan':{},'TkinNominalMeV':1000.0, 'WFLength':40,'Npulses':100, 'useBPMs':[]}
        self.davfoil = {}
        self.dfoil = {'BPMWaveForms':{},'usepulses':[]}

        self.solver = None
        
        self.d['TripleScan']['scan-0'] = {'ids':["Ring_Mag:IKickH01","Ring_Mag:IKickH02","Ring_Mag:IKickH03"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-1'] = {'ids':["Ring_Mag:IKickH01","Ring_Mag:IKickH02","Ring_Mag:IKickH04"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-2'] = {'ids':["Ring_Mag:IKickH01","Ring_Mag:IKickH03","Ring_Mag:IKickH04"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-3'] = {'ids':["Ring_Mag:IKickH02","Ring_Mag:IKickH03","Ring_Mag:IKickH04"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-4'] = {'ids':["Ring_Mag:IKickV01","Ring_Mag:IKickV02","Ring_Mag:IKickV03"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-5'] = {'ids':["Ring_Mag:IKickV01","Ring_Mag:IKickV02","Ring_Mag:IKickV04"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-6'] = {'ids':["Ring_Mag:IKickV01","Ring_Mag:IKickV03","Ring_Mag:IKickV04"],'dV':[1.0, 1.0, 1.0],'status':None}
        self.d['TripleScan']['scan-7'] = {'ids':["Ring_Mag:IKickV02","Ring_Mag:IKickV03","Ring_Mag:IKickV04"],'dV':[1.0, 1.0, 1.0],'status':None}

        self.caF = ChannelFactory.defaultFactory()
 

        #self.resetChs = []
        #for id in ["Ring_Diag:BPM_A:IOC_Cmd","Ring_Diag:BPM_B:IOC_Cmd","Ring_Diag:BPM_C:IOC_Cmd","Ring_Diag:BPM_D:IOC_Cmd","Ring_Diag:BPM_E:IOC_Cmd","Ring_Diag:BPM_F:IOC_Cmd"]:
            #self.resetChs.append(self.caF.getChannel(id))
            
        
        
        self.IkickIds = ["Ring_Mag:IKickH01","Ring_Mag:IKickH02","Ring_Mag:IKickH03","Ring_Mag:IKickH04","Ring_Mag:IKickV01","Ring_Mag:IKickV02","Ring_Mag:IKickV03","Ring_Mag:IKickV04"]
        

        self.dIKick = {}
        self.relativeCoeff = {}
        for id in self.IkickIds:
            ch = self.caF.getChannel(id[:9] + "PS_" + id[9:]+":UTCA:AMPL")
            #val = 0
            val = round(ch.getValDbl(),7)
            self.dIKick[id] = 0.0
            self.relativeCoeff[id] = None
            

            self.d['IKickParams'][id] = {}
            self.d['IKickParams'][id]['date/time'] = None
            self.d['IKickParams'][id]['pvLoggerId'] = -1
            self.d['IKickParams'][id]['mrad/V'] = 1.0
            self.d['IKickParams'][id]['voltage-1'] = val
            self.d['IKickParams'][id]['voltage-2'] = val + 1.0
            self.d['IKickParams'][id]['usepulses'] = {}
            #self.d['IKickParams'][id]['usepulses']['voltage-1'] = []
            #self.d['IKickParams'][id]['usepulses']['voltage-2'] = []



            
        self.plotFoil = [True,True,True,True,True,True,True,True]
        #self.calculateAllAverageWaveforms()


        self.psIds = ["Ring_Mag:PS_QV01a09","Ring_Mag:PS_QH02a08","Ring_Mag:PS_QV03a05a07","Ring_Mag:PS_QH04a06","Ring_Mag:PS_QH10a13","Ring_Mag:PS_QV11a12"]
        self.bendIds = ["Ring_Mag:PS_DH_A10","Ring_Mag:PS_DH_A11","Ring_Mag:PS_DH_A12","Ring_Mag:PS_DH_A13","Ring_Mag:PS_DH_Main"]
        
        for id in self.psIds:
            self.d['BLQuadCorrections'][id] = 1.0

        self.b = []
        self.quadsLat = []
        for id in self.psIds:
            self.quadsLat.append(PhaseMatrix.identity())
            self.b.append(Boolean(1))
            

        acc = XMLDataManager.acceleratorWithPath(thisDir + "/design/main.xal")
        
        seq1 = acc.getSequence("Ring1")
        seq2 = acc.getSequence("Ring2")
        seq3 = acc.getSequence("Ring3")
        seq4 = acc.getSequence("Ring4")
        seq5 = acc.getSequence("Ring5")

        types = ["BLM","BCM","Bnch","RG","SV","SH","QSC","DCV","DCH"]

        
        for node in seq1.getAllInclusiveNodes():
            if node.getType() in types:
                seq1.removeNode(node)
        for node in seq2.getAllInclusiveNodes():
            if node.getType() in types:
                seq2.removeNode(node)
        for node in seq3.getAllInclusiveNodes():
            if node.getType() in types:
                seq3.removeNode(node)
        for node in seq4.getAllInclusiveNodes():
            if node.getType() in types:
                seq4.removeNode(node)
        for node in seq5.getAllInclusiveNodes():
            if node.getType() in types:
                seq5.removeNode(node)

        lst = ArrayList()

        lst.add(seq1)
        lst.add(seq2)
        lst.add(seq3)
        lst.add(seq4)
        lst.add(seq5)
        
        self.seq = AcceleratorSeqCombo("RING",lst)
        
        self.m = 0.9393014e9
        #self.T = 0.604e9
        self.c = 299792458
        self.getOpticsFromPV(-1)
        self.SetQuadFields()
        
        self.alphasX = {}
        self.betasX = {}
        self.alphasY = {}
        self.betasY = {}
        
        self.matrFoilToNode = {}
        self.matrFoilToNodeInv = {}
        
        self.tuneX, self.tuneY = self.getTunes()
        self.getMatrFoilToNode()
        self.tuneX0, self.tuneY0 = self.tuneX, self.tuneY
        
        
        
        self.dxf = 0
        self.dxpf = 0
        self.dyf = 0
        self.dypf = 0
        
        self.stop = False
        
        #for i in range(8):
        #    self.tripleValueChanged(i)


        self.bpms = []
        self.allBPMsIds = []
        for node in self.seq.getAllInclusiveNodes():
            if node.getType() == "BPM":
                self.bpms.append(node)
                self.allBPMsIds.append(node.getId())
                
                if node.getId() not in ['Ring_Diag:BPM_D10','Ring_Diag:BPM_A13','Ring_Diag:BPM_A10']:
                    self.d['useBPMs'].append(node.getId())
                
        
        
        
             
        for kickId in self.IkickIds:
            self.dav[kickId] = {}
            for vid in ['voltage-1','voltage-2']:
                self.dav[kickId][vid] = {}
                for bpmId in self.allBPMsIds:
                    self.dav[kickId][vid][bpmId] = {}
                    self.dav[kickId][vid][bpmId]['tuneX'] = 0
                    self.dav[kickId][vid][bpmId]['tuneY'] = 0
                    self.dav[kickId][vid][bpmId]['ampX'] = 0
                    self.dav[kickId][vid][bpmId]['ampY'] = 0
                    self.dav[kickId][vid][bpmId]['omegaX'] = 0
                    self.dav[kickId][vid][bpmId]['omegaY'] = 0
                    self.dav[kickId][vid][bpmId]['phaseX'] = 0
                    self.dav[kickId][vid][bpmId]['phaseY'] = 0
                    self.dav[kickId][vid][bpmId]['kX'] = 0
                    self.dav[kickId][vid][bpmId]['kY'] = 0
                    self.dav[kickId][vid][bpmId]['offsetX'] = 0
                    self.dav[kickId][vid][bpmId]['offsetY'] = 0
                    self.dav[kickId][vid][bpmId]['foil-X'] = 0
                    self.dav[kickId][vid][bpmId]['foil-XP'] = 0
                    self.dav[kickId][vid][bpmId]['foil-Y'] = 0
                    self.dav[kickId][vid][bpmId]['foil-YP'] = 0
                    self.dav[kickId][vid][bpmId]['waveform-x'] = [0]*self.d['WFLength']
                    self.dav[kickId][vid][bpmId]['waveform-y'] = [0]*self.d['WFLength']
        

        

        for bpmId in self.allBPMsIds:
            self.davfoil[bpmId] = {}
            self.davfoil[bpmId]['tuneX'] = 0
            self.davfoil[bpmId]['tuneY'] = 0
            self.davfoil[bpmId]['ampX'] = 0
            self.davfoil[bpmId]['ampY'] = 0
            self.davfoil[bpmId]['omegaX'] = 0
            self.davfoil[bpmId]['omegaY'] = 0
            self.davfoil[bpmId]['phaseX'] = 0
            self.davfoil[bpmId]['phaseY'] = 0
            self.davfoil[bpmId]['kX'] = 0
            self.davfoil[bpmId]['kY'] = 0
            self.davfoil[bpmId]['offsetX'] = 0
            self.davfoil[bpmId]['offsetY'] = 0
            self.davfoil[bpmId]['foil-X'] = 0
            self.davfoil[bpmId]['foil-XP'] = 0
            self.davfoil[bpmId]['foil-Y'] = 0
            self.davfoil[bpmId]['foil-YP'] = 0
            self.davfoil[bpmId]['waveform-x'] = [0]*self.d['WFLength']
            self.davfoil[bpmId]['waveform-y'] = [0]*self.d['WFLength']
            
        

                    
                    
        #excludeBpms = ['Ring_Diag:BPM_A01','Ring_Diag:BPM_A02','Ring_Diag:BPM_A03','Ring_Diag:BPM_A04','Ring_Diag:BPM_A05','Ring_Diag:BPM_A06','Ring_Diag:BPM_A07','Ring_Diag:BPM_A08','Ring_Diag:BPM_D10','Ring_Diag:BPM_C08','Ring_Diag:BPM_A13','Ring_Diag:BPM_A10']



            
            
            
        
        self.tuneXprogress = 0
        self.tuneYprogress = 0
        self.dx = 1.0
        self.dy = 1.0
        self.dxp = 1.0
        self.dyp = 1.0
        
 
        
        #self.NturnMax = self.caF.getChannel("ICS_Tim:Chop_Flavor1:BeamOn").getValInt() #"ICS_Tim:RTDLGen:StoredTurns"
        
        #self.NturnMax = self.getNturns()
        #self.NturnMax = 0


            
            

            

        
        #for id in self.IkickIds:
        #    ch3 = self.runAdaptor.childAdaptor('BPMWaveForms').removeChilds(id)
            
            
            
    def resetBPMsfilter(self):
        for ch in self.resetChs:
            ch.putVal(1)

        

    

    def SetQuadFields(self):
        
        for i in range(len(self.psIds)):
            
            L = self.quadEffLen[i]
            #correctionCoefficient = self.qch.childAdaptor(self.psIds[i]).doubleValue("coeff")
            correctionCoefficient = self.d['BLQuadCorrections'][self.psIds[i]]
            #Tkin = self.runAdaptor.doubleValue("TkinNominalMeV")*1e6
            Tkin = self.d['TkinNominalMeV']*1e6
            
            Bmax = self.BQuadSet[i]*correctionCoefficient
            sqrtk = math.sqrt(self.c*Bmax/math.sqrt(Tkin*(2*self.m + Tkin)))
            
            cos = math.cos(sqrtk*L)
            sin = math.sin(sqrtk*L)
            cosh = math.cosh(sqrtk*L)
            sinh = math.sinh(sqrtk*L)
            
            if "PS_QH" in self.psIds[i]:
                self.quadsLat[i].setElem(0,0,cos)
                self.quadsLat[i].setElem(0,1,sin/sqrtk)
                self.quadsLat[i].setElem(1,0,-sqrtk*sin)
                self.quadsLat[i].setElem(1,1,cos)

                self.quadsLat[i].setElem(2,2,cosh)
                self.quadsLat[i].setElem(2,3,sinh/sqrtk)
                self.quadsLat[i].setElem(3,2,sqrtk*sinh)
                self.quadsLat[i].setElem(3,3,cosh)

                self.quadsLat[i].setElem(4,5,L)

            if "PS_QV" in self.psIds[i]:
                self.quadsLat[i].setElem(0,0,cosh)
                self.quadsLat[i].setElem(0,1,sinh/sqrtk)
                self.quadsLat[i].setElem(1,0,sqrtk*sinh)
                self.quadsLat[i].setElem(1,1,cosh)

                self.quadsLat[i].setElem(2,2,cos)
                self.quadsLat[i].setElem(2,3,sin/sqrtk)
                self.quadsLat[i].setElem(3,2,-sqrtk*sin)
                self.quadsLat[i].setElem(3,3,cos)

                self.quadsLat[i].setElem(4,5,L)             
        


    
    def getAlpha0Beta0(self):
        
        rm = PhaseMatrix.identity()
        for m in self.lat:
            rm = m.times(rm)
            

        c0x = 1-rm.getElem(0,1)*rm.getElem(1,0)+rm.getElem(0,0)*rm.getElem(1,1)
        c0y = 1-rm.getElem(2,3)*rm.getElem(3,2)+rm.getElem(2,2)*rm.getElem(3,3)
        
        if (c0x**2-(rm.getElem(0,0)+rm.getElem(1,1))**2 > 0 and c0y**2-(rm.getElem(2,2)+rm.getElem(3,3))**2 > 0):
        
            cx=math.sqrt(c0x**2-(rm.getElem(0,0)+rm.getElem(1,1))**2)
            bX0=rm.getElem(0,1)*c0x/cx
            aX0=(rm.getElem(0,0)*(c0x-1)-rm.getElem(1,1))/cx

            cy=math.sqrt(c0y**2-(rm.getElem(2,2)+rm.getElem(3,3))**2)
            bY0=rm.getElem(2,3)*c0y/cy
            aY0=(rm.getElem(2,2)*(c0y-1)-rm.getElem(3,3))/cy

            return aX0, bX0, aY0, bY0
        
        return 0.0, -1.0, 0.0, -1.0
    
    
    def getTunes(self):
        
        phiX=0
        phiY=0
        alphaX, betaX, alphaY, betaY = self.getAlpha0Beta0()
        
        for i in range(len(self.lat)):
            
            m = self.lat[i]
            id = self.lids[i]

            cx=betaX*m.getElem(0,0)-alphaX*m.getElem(0,1)
            cy=betaY*m.getElem(2,2)-alphaY*m.getElem(2,3)
            phiX+=math.atan(m.getElem(0,1)/cx)
            phiY+=math.atan(m.getElem(2,3)/cy)

            alphaX, betaX=-(cx*(betaX*m.getElem(1,0)-alphaX*m.getElem(1,1))+m.getElem(0,1)*m.getElem(1,1))/betaX, (cx**2+m.getElem(0,1)**2)/betaX
            alphaY, betaY=-(cy*(betaY*m.getElem(3,2)-alphaY*m.getElem(3,3))+m.getElem(2,3)*m.getElem(3,3))/betaY, (cy**2+m.getElem(2,3)**2)/betaY
            
            self.alphasX[id] = alphaX
            self.betasX[id] = betaX
            self.alphasY[id] = alphaY
            self.betasY[id] = betaY
            
        return phiX/(2*math.pi), phiY/(2*math.pi)
    
    
    
    
    def calculate_grad(self):

        grad = [[0,0,0,0,0,0],[0,0,0,0,0,0]]

        tX0, tY0 = self.getTunes()

        db = 1e-6
        
        bset0 = self.BQuadSet[:]

        for i in range(6):
            self.BQuadSet = bset0[:]
            self.BQuadSet[i] += db

            self.SetQuadFields()

            tX, tY = self.getTunes()

            grad[0][i] = (tX - tX0)/db
            grad[1][i] = (tY - tY0)/db


        self.BQuadSet = bset0[:]
        self.SetQuadFields()

        return grad
    

    def one_step(self, increment, parInd):
                
        grad = self.calculate_grad()
        
        ov.indices = [i for i in range(len(self.b)) if self.b[i] == Boolean(False)]
        
        sol = ov.getVectorSolution(grad[parInd], [grad[k] for k in range(len(grad)) if k != parInd])
        VectordQ = ov.getVectorForValue(sol, grad[parInd], increment)
        print len(self.BQuadSet)
        print self.BQuadSet
        print VectordQ
        for i in range(len(self.BQuadSet)):
            self.BQuadSet[i] += VectordQ[i]
            
        self.SetQuadFields()
            
        return
    
    def multisteps(self, td):
        
        
        tuneX0, tuneY0 = self.getTunes()
        
        #self.BQuadSet = []
        #for qs in self.BQuadSet0:
            #self.BQuadSet.append(qs)
            
        #self.SetQuadFields()
        
        dtune0 = 1e-5
        
        ind = -1
        dtune = 0
        nsteps = 0
        
        if abs(self.tuneX - tuneX0) > 1e-6:
            ind = 0
            nsteps = int(abs(self.tuneX - tuneX0)/dtune0)
            dtune = (self.tuneX - tuneX0)/nsteps

        if abs(self.tuneY - tuneY0) > 1e-6:
            ind = 1
            nsteps = int(abs(self.tuneY - tuneY0)/dtune0)
            dtune = (self.tuneY - tuneY0)/nsteps

        if ind == -1:
            return

        for i in range (nsteps):
            self.one_step(dtune, ind)
            
            if ind == 0:
                self.tuneXprogress = int(i*100.0/nsteps)
                td.fireTableDataChanged()
            if ind == 1:
                self.tuneYprogress = int(i*100.0/nsteps)
                td.fireTableDataChanged()
            
            #if int(i*100.0/nsteps) != int((i-1)*100.0/nsteps):
                #print "progress = ",int(i*100.0/nsteps),"%"

        self.tuneX, self.tuneY = self.getTunes()
        self.getMatrFoilToNode()
        #self.tuneX0, self.tuneY0 = self.tuneX, self.tuneY
        

    def setLiveQuads(self):
        
        
        self.tuneX0, self.tuneY0 = self.tuneX, self.tuneY
        self.BQuadSet0 = [qs for qs in self.BQuadSet]

        for i in range(len(self.psIds)):
            
            id = self.psIds[i]

            chB = self.caF.getChannel(id + ":B_Set")
            chBook = self.caF.getChannel(id + ":B_Book")
                      
            chB.putVal(self.BQuadSet[i])
            chBook.putVal(self.BQuadSet[i])
            

        
    """        
    def getNturns(self):
        
        nlist = []
        useBPMs = [bpm for bpm in self.bpms if bpm.getId() in self.d['useBPMs']]
        
        event = scanEvent(useBPMs)
        event.submitBatchRequest()
        event.populateXMLEvent()
        
        for bpmId in self.d['useBPMs']:
            vx = event.getTBT(bpmId,"x")
            vy = event.getTBT(bpmId,"y")
            nlist.append(len(vx))
            nlist.append(len(vy))
            
        if nlist[1:] == nlist[:-1]:
            #if (self.Nturn > nlist[0]):
                    #self.Nturn = nlist[0]
            return nlist[0]
        else:
            return 0
    """
    
    
    def measureOrbit(self, kickId, pane):

        
        useBPMs = [bpm for bpm in self.bpms if bpm.getId() in self.d['useBPMs']]
        
        pane.JP.removeAllGraphData()
        self.d['BPMWaveForms'][kickId] = {}
        self.d['IKickParams'][kickId]['date/time'] = strftime("%b %d, %Y %H:%M:%S")
                
        #PvLogId = 42450795
        #PvLogId = xal.app.pta.MainApplication.pvLoggerSnappulse("Ring optics configuration")
        PvLogId = RemoteLoggingCenter().takeAndPublishSnapshot( "default", "Ring optics")
        print "PvLogId = ", PvLogId
        self.d['IKickParams'][kickId]['pvLoggerId'] = PvLogId

        chan = self.caF.getChannel(kickId[:9] + "PS_" + kickId[9:]+":UTCA:AMPL")
        val0 = chan.getValDbl()
        

        for vid in ['voltage-1','voltage-2']:
            
            self.d['BPMWaveForms'][kickId][vid] = {}
            self.d['IKickParams'][kickId]['usepulses'][vid] = []

            val = self.d['IKickParams'][kickId][vid]
            chan.putVal(val)
            time.sleep(5)
            
            #self.d['IKickParams'][kickId][vid] = val + int(vid == 'voltage-2')*self.IkickdV[kickId]

            event = scanEvent(useBPMs)

            for i in range(self.d['Npulses']):
                event.submitBatchRequest()
                event.populateXMLEvent()
                time.sleep(1)

                self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)] = {}
                self.d['IKickParams'][kickId]['usepulses'][vid].append('pulse-' + str(i))
                
                for bpmId in self.d['useBPMs']:
                    
                    self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)][bpmId] = {}
                    self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)][bpmId]['waveform-x'] = event.getTBT(bpmId,"x")[:self.d['WFLength']]
                    self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)][bpmId]['waveform-y'] = event.getTBT(bpmId,"y")[:self.d['WFLength']]
                    
                    #self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)][bpmId]['waveform-x'] = [ii for ii in range(self.Nturn)]
                    #self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)][bpmId]['waveform-y'] = [ii for ii in range(self.Nturn)]

                pane.addPulseToPlot(self.d['BPMWaveForms'][kickId][vid]['pulse-' + str(i)])
                pane.updatePulsetable(kickId)
                pane.updatePulseselection(kickId)
                
            #self.resetBPMsfilter()
            
            
        chan.putVal(val0)
        #self.resetBPMsfilter()
        self.calculateAllAverageWaveforms()
        
                
                
    
    def idsdataCalc(self, kickId):
        
        bpmXaver1 = []
        bpmYaver1 = []
        bpmXaver2 = []
        bpmYaver2 = []
        zind = []
        showBPMsIDs = []
        
        for vid in self.dav[kickId].keys():
            indBPM = 0
            zind = []
            showBPMsIDs = []
            for bpmId in self.allBPMsIds:
                if bpmId in self.d['useBPMs'] and bpmId in self.dav[kickId][vid].keys():
                    if (vid == "voltage-1"):
                        bpmXaver1.append(self.dav[kickId][vid][bpmId]['offsetX'])
                        bpmYaver1.append(self.dav[kickId][vid][bpmId]['offsetY'])
                    if (vid == "voltage-2"):
                        bpmXaver2.append(self.dav[kickId][vid][bpmId]['offsetX'])
                        bpmYaver2.append(self.dav[kickId][vid][bpmId]['offsetY'])
                    zind.append(indBPM)
                    showBPMsIDs.append(bpmId)
                indBPM += 1
                
        dxaver = [bpmXaver2[i] - bpmXaver1[i] for i in range(len(bpmXaver1))]
        dyaver = [bpmYaver2[i] - bpmYaver1[i] for i in range(len(bpmYaver1))]
            

        return zind, showBPMsIDs, bpmXaver1, bpmYaver1, bpmXaver2, bpmYaver2, dxaver, dyaver
    
    
    
    def getOpticsFromPV(self, pvlogid):
        
        generator = ScenarioGenerator(self.seq)
        generator.setHalfMag(False)
        model = generator.generateScenario()
        
        self.BQuadSet = []
        self.BBendSet = []
        if pvlogid != -1:
            dsrc = PVLoggerDataSource(pvlogid)
            dsrc.setUsesLoggedBendFields(True)
            model = dsrc.setModelSource(self.seq, model)

            for id in self.psIds:
                val = dsrc.getChannelSnapshotValue(id +":B_Set")[0]
                self.BQuadSet.append(val)
                
            for id in self.bendIds:
                val = dsrc.getChannelSnapshotValue(id +":B_Set")[0]
                self.BBendSet.append(val)


        else:

            model.setSynchronizationMode("LIVE")

            for id in self.psIds:
                chB = self.caF.getChannel(id + ":B_Set")
                val = round(chB.getArrDbl()[0],7)
                self.BQuadSet.append(val)
                
            for id in self.bendIds:
                chB = self.caF.getChannel(id + ":B_Set")
                val = round(chB.getArrDbl()[0],7)
                self.BBendSet.append(val)
                
            
            
            
        probe = ProbeFactory.getTransferMapProbe(self.seq, TransferMapTracker())

        probe.setSpeciesRestEnergy(self.m)
        probe.setKineticEnergy(self.d['TkinNominalMeV']*1e6)
        probe.setSpeciesCharge(+1)

        model.setProbe(probe)
        model.resync()
        model.run()
        
        trajectory = model.getTrajectory()
        num = trajectory.numStates()

        self.lids = []
        self.lat = []
        self.quadEffLen = [0, 0, 0, 0, 0, 0]

        for i in range(num-1):
            st = trajectory.stateWithIndex(i+1)
            self.lids.append(st.getElementId())
            self.lat.append(st.getPartialTransferMap().getFirstOrder())
            
            #This function assign all quadrupoles of the lattice as 6 types of elements of self.quadsLat list
            if "Ring_Mag:QT" in st.getElementId():
                node = self.seq.getNodeWithId(st.getElementId())
                psId = node.getMainSupply().getId()
                psInd = self.psIds.index(psId)
                self.quadEffLen[psInd] = node.getEffLength()
                for ii in range(7):
                    for jj in range(7):
                        self.quadsLat[psInd].setElem(ii,jj, self.lat[i].getElem(ii,jj))
                        
                self.lat[i] = self.quadsLat[psInd]
        
        
        self.injLat = []
        self.injIds = []
        
        indH01 = self.lids.index("Ring_Mag:IKickH01")
        indH04 = self.lids.index("Ring_Mag:IKickH04")
        
        for i in range(indH01, len(self.lat)) + range(1,indH04 + 1):
            self.injLat.append(self.lat[i])
            self.injIds.append(self.lids[i])
        
                
        self.BQuadSet0 = [qs for qs in self.BQuadSet]

                
        
                


      

    def analyse(self, useTunes, useCoupledIKick, label):
        
        allKickdata = {}
        pvIds = []
        
        ntotalpoints = 0
        for id in self.IkickIds:
            
            zind, showBPMsIDs, bpmXaver1, bpmYaver1, bpmXaver2, bpmYaver2, dxaver, dyaver = self.idsdataCalc(id)
            allKickdata[id] = showBPMsIDs, dxaver, dyaver
            
            ntotalpoints += len(showBPMsIDs)
            pvIds.append(self.d['IKickParams'][id]['pvLoggerId'])
        PvLogId = max(pvIds)
        

        self.getOpticsFromPV(PvLogId)

        
        
        var = ArrayList()
        delta_hint = InitialDelta()

        for id in self.psIds:
            v = Variable(id, self.d['BLQuadCorrections'][id], 0.9, 1.1)
            var.add(v)
            delta_hint.addInitialDelta(v, 0.0001)
            
            
        
        arrtuneX = [self.dav[kickId]['voltage-1'][bpmId]['tuneX'] for kickId in self.dav.keys() for bpmId in self.dav[kickId]['voltage-1'].keys() if bpmId in self.d['useBPMs']]
        arrtuneY = [self.dav[kickId]['voltage-1'][bpmId]['tuneY'] for kickId in self.dav.keys() for bpmId in self.dav[kickId]['voltage-1'].keys() if bpmId in self.d['useBPMs']]

        tuneXfixed = sum(arrtuneX) / len(arrtuneX) + int(self.tuneX)
        tuneYfixed = sum(arrtuneY) / len(arrtuneY) + int(self.tuneY)
        
        ro = self
        class MyScorer(Scorer):

            def __init__(self, variables):
                self.variables = variables
                self.j = 0

            def score(self, trial, variables_in):
                tp = trial.getTrialPoint()

                for i in range(len(ro.psIds)):
                    ro.d['BLQuadCorrections'][ro.psIds[i]] = tp.getValue(self.variables.get(i))

                ro.SetQuadFields()
                
                label.setText("Nsteps = " + str(self.j))
                self.j += 1

                sc = 0
                if useCoupledIKick:
                    sc1 = ro.IKickRMSerror(["Ring_Mag:IKickH01","Ring_Mag:IKickH04","Ring_Mag:IKickV01","Ring_Mag:IKickV04"],allKickdata)
                    sc2 = ro.IKickRMSerror(["Ring_Mag:IKickH02","Ring_Mag:IKickH03","Ring_Mag:IKickV02","Ring_Mag:IKickV03"],allKickdata)
                    sc = (sc1 + sc2)/ntotalpoints
                else:
                    sc = sum([ro.IKickRMSerror([id],allKickdata) for id in ro.IkickIds])/ntotalpoints                
                
                if useTunes:
                    ro.tuneX, ro.tuneY = ro.getTunes()
                    sc += 1000*((ro.tuneX - tuneXfixed)**2 + (ro.tuneY - tuneYfixed)**2)
                    
                return sc
       
        

        maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(10000)
        solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)

        scorer = MyScorer(var)

        problem = ProblemFactory.getInverseSquareMinimizerProblem(var,scorer,0.00001)
        problem.addHint(delta_hint)
        solver.solve(problem)
        
        
        ro.tuneX, ro.tuneY = ro.getTunes()
        self.tuneX0, self.tuneY0 = self.tuneX, self.tuneY
        self.getMatrFoilToNode()
        self.calculateAllAverageWaveforms()
        
        

    def ModelOrbits(self,kickid, showBPMsIDs):
        
        num = len(self.lids)
        ind = self.lids.index(kickid)
        indexes = range(ind+1,num)+range(1,ind)


        mbpm = {}
        rm = PhaseMatrix.identity()
        
        for ii in indexes:
            rm = self.lat[ii].times(rm)
            if self.lids[ii] in showBPMsIDs + ["Ring_Inj:Foil"]:
                matr = Matrix(4,4)
                for i in range(4):
                    for j in range(4):
                        matr.set(i,j, rm.getElem(i,j))
                mbpm[self.lids[ii]] = matr
                
        im0 = Matrix(4,4)
        for i in range(4):
            for j in range(4):
                im0.set(i,j,int(i==j) - rm.getElem(i,j))
        im = im0.inverse()
        
        
        
        
        n = len(showBPMsIDs)
        dV = self.d['IKickParams'][kickid]['voltage-2'] - self.d['IKickParams'][kickid]['voltage-1']
        
        mfoil = mbpm["Ring_Inj:Foil"]

        
        if("IKickH" in kickid):
            
            vx = [im.get(0,1),im.get(1,1),im.get(2,1),im.get(3,1)]
            dxt = [mbpm[id].get(0,0)*vx[0] + mbpm[id].get(0,1)*vx[1] + mbpm[id].get(0,2)*vx[2] + mbpm[id].get(0,3)*vx[3]  for id in showBPMsIDs]
            
            dxk = dV*self.d['IKickParams'][kickid]['mrad/V']
            arrxt = [dxk*dxt[i] for i in range(n)]
            
            self.dxf = dxk*(mfoil.get(0,0)*vx[0] + mfoil.get(0,1)*vx[1] + mfoil.get(0,2)*vx[2] + mfoil.get(0,3)*vx[3])
            self.dxpf = dxk*(mfoil.get(1,0)*vx[0] + mfoil.get(1,1)*vx[1] + mfoil.get(1,2)*vx[2] + mfoil.get(1,3)*vx[3])
            self.dyf = dxk*(mfoil.get(2,0)*vx[0] + mfoil.get(2,1)*vx[1] + mfoil.get(2,2)*vx[2] + mfoil.get(2,3)*vx[3])
            self.dypf = dxk*(mfoil.get(3,0)*vx[0] + mfoil.get(3,1)*vx[1] + mfoil.get(3,2)*vx[2] + mfoil.get(3,3)*vx[3])
            
            return arrxt
        
        if("IKickV" in kickid):
            
            vy = [im.get(0,3),im.get(1,3),im.get(2,3),im.get(3,3)]
            dyt = [mbpm[id].get(2,0)*vy[0] + mbpm[id].get(2,1)*vy[1] + mbpm[id].get(2,2)*vy[2] + mbpm[id].get(2,3)*vy[3]  for id in showBPMsIDs]
            
            dyk = dV*self.d['IKickParams'][kickid]['mrad/V']
            arryt = [dyk*dyt[i] for i in range(n)]
            
            self.dxf = dyk*(mfoil.get(0,0)*vy[0] + mfoil.get(0,1)*vy[1] + mfoil.get(0,2)*vy[2] + mfoil.get(0,3)*vy[3])
            self.dxpf = dyk*(mfoil.get(1,0)*vy[0] + mfoil.get(1,1)*vy[1] + mfoil.get(1,2)*vy[2] + mfoil.get(1,3)*vy[3])
            self.dyf = dyk*(mfoil.get(2,0)*vy[0] + mfoil.get(2,1)*vy[1] + mfoil.get(2,2)*vy[2] + mfoil.get(2,3)*vy[3])
            self.dypf = dyk*(mfoil.get(3,0)*vy[0] + mfoil.get(3,1)*vy[1] + mfoil.get(3,2)*vy[2] + mfoil.get(3,3)*vy[3])
             
            return arryt
            


        return

    def getdxtdyt(self, kickid, bpmids):
        
        num = len(self.lids)
        ind = self.lids.index(kickid)
        indexes = range(ind+1,num)+range(1,ind)


        mbpm = {}
        rm = PhaseMatrix.identity()
        
        for ii in indexes:
            rm = self.lat[ii].times(rm)
            if self.lids[ii] in bpmids:
                matr = Matrix(4,4)
                for i in range(4):
                    for j in range(4):
                        matr.set(i,j, rm.getElem(i,j))
                mbpm[self.lids[ii]] = matr



        im0 = Matrix(4,4)
        for i in range(4):
            for j in range(4):
                im0.set(i,j,int(i==j) - rm.getElem(i,j))
        im = im0.inverse()
        
        vx = [im.get(0,1),im.get(1,1),im.get(2,1),im.get(3,1)]
        vy = [im.get(0,3),im.get(1,3),im.get(2,3),im.get(3,3)]
        
        dxt = [mbpm[id].get(0,0)*vx[0] + mbpm[id].get(0,1)*vx[1] + mbpm[id].get(0,2)*vx[2] + mbpm[id].get(0,3)*vx[3]  for id in bpmids]
        dyt = [mbpm[id].get(2,0)*vy[0] + mbpm[id].get(2,1)*vy[1] + mbpm[id].get(2,2)*vy[2] + mbpm[id].get(2,3)*vy[3]  for id in bpmids]        
        

        return dxt, dyt
    
    
       
    def getMatrFoilToNode(self):
        
        
        
        num = len(self.lids)
        ind = self.lids.index("Ring_Inj:Foil")
        indexes = range(ind+1,num)+range(1,ind + 1)


        self.matrFoilToNode = {}
        self.matrFoilToNodeInv = {}
        
        rm = PhaseMatrix.identity()
        for ii in indexes:
            rm = self.lat[ii].times(rm)

            matr = Matrix(4,4)
            for i in range(4):
                for j in range(4):
                    matr.set(i,j, rm.getElem(i,j))
            self.matrFoilToNode[self.lids[ii]] = matr
            self.matrFoilToNodeInv[self.lids[ii]] = matr.inverse()
            
            
    



     
    def IKickRMSerror(self, kickids, allKickdata):

        dt2 = {}
        de2 = {}
        det = {}
        dv = {}
        
        for id in kickids:
            dxt, dyt = self.getdxtdyt(id, allKickdata[id][0])
            dxe = allKickdata[id][1]
            dye = allKickdata[id][2]
            
            n = len(allKickdata[id][0])
            dv[id] = self.d['IKickParams'][id]['voltage-2'] - self.d['IKickParams'][id]['voltage-1']
            
            if("IKickH" in id):
                dt2[id] = sum([dxt[j]*dxt[j] for j in range(n)])
                de2[id] = sum([dxe[j]*dxe[j] for j in range(n)])
                det[id] = sum([dxt[j]*dxe[j] for j in range(n)])
            if("IKickV" in id):
                dt2[id] = sum([dyt[j]*dyt[j] for j in range(n)])
                de2[id] = sum([dye[j]*dye[j] for j in range(n)])
                det[id] = sum([dyt[j]*dye[j] for j in range(n)])

        dk = sum([det[id] for id in kickids])/sum([dv[id]*dt2[id] for id in kickids])
        
        for id in kickids:
            self.d['IKickParams'][id]['mrad/V'] = dk
   
        return sum([de2[j] + dv[j]*dk*dv[j]*dk*dt2[j] - 2*dv[j]*dk*det[j] for j in kickids])
        #sum([(dxe[j] - dv*dk*dxt[j])**2 for j in range(n)])    
    
    
    
    
    def calculateRealPhaseParamIKick(self):
        

        indH01 = self.injIds.index("Ring_Mag:IKickH01")
        indH02 = self.injIds.index("Ring_Mag:IKickH02")
        indH03 = self.injIds.index("Ring_Mag:IKickH03")
        indH04 = self.injIds.index("Ring_Mag:IKickH04")
        indV01 = self.injIds.index("Ring_Mag:IKickV01")
        indV02 = self.injIds.index("Ring_Mag:IKickV02")
        indV03 = self.injIds.index("Ring_Mag:IKickV03")
        indV04 = self.injIds.index("Ring_Mag:IKickV04")
        indFoil = self.injIds.index("Ring_Inj:Foil")

        H1f = PhaseMatrix.identity()
        for i in range(indH01+1,indFoil+1):
            H1f = self.injLat[i].times(H1f)

        V1f = PhaseMatrix.identity()
        for i in range(indV01+1,indFoil+1):
            V1f = self.injLat[i].times(V1f)
            
        H2f = PhaseMatrix.identity()
        for i in range(indH02+1,indFoil+1):
            H2f = self.injLat[i].times(H2f)

        V2f = PhaseMatrix.identity()
        for i in range(indV02+1,indFoil+1):
            V2f = self.injLat[i].times(V2f)
            
        
        M = Matrix(4,4)

        M.set(0,0,H1f.getElem(0,1))
        M.set(1,0,H1f.getElem(1,1))
        M.set(2,0,H1f.getElem(2,1))
        M.set(3,0,H1f.getElem(3,1))
        
        M.set(0,1,H2f.getElem(0,1))
        M.set(1,1,H2f.getElem(1,1))
        M.set(2,1,H2f.getElem(2,1))
        M.set(3,1,H2f.getElem(3,1))
        
        M.set(0,2,V1f.getElem(0,3))
        M.set(1,2,V1f.getElem(1,3))
        M.set(2,2,V1f.getElem(2,3))
        M.set(3,2,V1f.getElem(3,3))
        
        M.set(0,3,V2f.getElem(0,3))
        M.set(1,3,V2f.getElem(1,3))
        M.set(2,3,V2f.getElem(2,3))
        M.set(3,3,V2f.getElem(3,3))
        
        Minv = M.inverse()

        self.dIKick["Ring_Mag:IKickH01"] = self.dx*Minv.get(0,0) + self.dxp*Minv.get(0,1) + self.dy*Minv.get(0,2) + self.dyp*Minv.get(0,3)
        self.dIKick["Ring_Mag:IKickH02"] = self.dx*Minv.get(1,0) + self.dxp*Minv.get(1,1) + self.dy*Minv.get(1,2) + self.dyp*Minv.get(1,3)
        self.dIKick["Ring_Mag:IKickV01"] = self.dx*Minv.get(2,0) + self.dxp*Minv.get(2,1) + self.dy*Minv.get(2,2) + self.dyp*Minv.get(2,3)
        self.dIKick["Ring_Mag:IKickV02"] = self.dx*Minv.get(3,0) + self.dxp*Minv.get(3,1) + self.dy*Minv.get(3,2) + self.dyp*Minv.get(3,3)
        
        
        fH4 = PhaseMatrix.identity()
        for i in range(indFoil, indH04 + 1):
            fH4 = self.injLat[i].times(fH4)
        
        V3H4 = PhaseMatrix.identity()
        for i in range(indV03, indH04 + 1):
            V3H4 = self.injLat[i].times(V3H4)
            
        H3H4 = PhaseMatrix.identity()
        for i in range(indH03, indH04 + 1):
            H3H4 = self.injLat[i].times(H3H4)
        
        V4H4 = PhaseMatrix.identity()
        for i in range(indV04, indH04 + 1):
            V4H4 = self.injLat[i].times(V4H4)
            


        M = Matrix(4,4)

        M.set(0,0,V3H4.getElem(0,3))
        M.set(1,0,V3H4.getElem(1,3))
        M.set(2,0,V3H4.getElem(2,3))
        M.set(3,0,V3H4.getElem(3,3))
        
        M.set(0,1,H3H4.getElem(0,1))
        M.set(1,1,H3H4.getElem(1,1))
        M.set(2,1,H3H4.getElem(2,1))
        M.set(3,1,H3H4.getElem(3,1))
        
        M.set(0,2,V4H4.getElem(0,3))
        M.set(1,2,V4H4.getElem(1,3))
        M.set(2,2,V4H4.getElem(2,3))
        M.set(3,2,V4H4.getElem(3,3))
        
        M.set(0,3, 0.0)
        M.set(1,3, 1.0)
        M.set(2,3, 0.0)
        M.set(3,3, 0.0)
        
        Mfh4 = Matrix(4,4)
        
        for i in [0,1,2,3]:
            for j in [0,1,2,3]:
                Mfh4.set(i,j, fH4.getElem(i,j))
                
        C = (M.inverse()).times(Mfh4).uminus()

        self.dIKick["Ring_Mag:IKickV03"] = self.dx*C.get(0,0) + self.dxp*C.get(0,1) + self.dy*C.get(0,2) + self.dyp*C.get(0,3)
        self.dIKick["Ring_Mag:IKickH03"] = self.dx*C.get(1,0) + self.dxp*C.get(1,1) + self.dy*C.get(1,2) + self.dyp*C.get(1,3)
        self.dIKick["Ring_Mag:IKickV04"] = self.dx*C.get(2,0) + self.dxp*C.get(2,1) + self.dy*C.get(2,2) + self.dyp*C.get(2,3)
        self.dIKick["Ring_Mag:IKickH04"] = self.dx*C.get(3,0) + self.dxp*C.get(3,1) + self.dy*C.get(3,2) + self.dyp*C.get(3,3)
        
        
        
        """
        self.injLat[indH01].setElem(1,6,self.dIKick["Ring_Mag:IKickH01"])
        self.injLat[indH02].setElem(1,6,self.dIKick["Ring_Mag:IKickH02"])
        self.injLat[indH03].setElem(1,6,self.dIKick["Ring_Mag:IKickH03"])
        self.injLat[indH04].setElem(1,6,self.dIKick["Ring_Mag:IKickH04"])
        
        self.injLat[indV01].setElem(3,6,self.dIKick["Ring_Mag:IKickV01"])
        self.injLat[indV02].setElem(3,6,self.dIKick["Ring_Mag:IKickV02"])
        self.injLat[indV03].setElem(3,6,self.dIKick["Ring_Mag:IKickV03"])
        self.injLat[indV04].setElem(3,6,self.dIKick["Ring_Mag:IKickV04"])
        """
        
        
        """
        num = len(self.injLat)
        phasevector = PhaseVector( 0.0, 0.0, 0.0, 0.0, 0.008, 0.0 )
        for i in range(num):
            phasevector = self.injLat[i].times(phasevector)
            #print self.injLat[0].toStringMatrix()
            print "end of element = ",self.injIds[i],phasevector.toString()#,self.injLat[i].toStringMatrix()
        """
        
        
        #iH01 = self.lids.index("Ring_Mag:IKickH01")
        #iH02 = self.lids.index("Ring_Mag:IKickH02")
        #iH03 = self.lids.index("Ring_Mag:IKickH03")
        #iH04 = self.lids.index("Ring_Mag:IKickH04")
        #iV01 = self.lids.index("Ring_Mag:IKickV01")
        #iV02 = self.lids.index("Ring_Mag:IKickV02")
        #iV03 = self.lids.index("Ring_Mag:IKickV03")
        #iV04 = self.lids.index("Ring_Mag:IKickV04")
        
        #self.lat[iH01].setElem(1,6, 0.635359)
        #self.lat[iH02].setElem(1,6, -0.35235)
        #self.lat[iH03].setElem(1,6, -0.35313)
        #self.lat[iH04].setElem(1,6, 0.686402)
        
        #self.lat[iH01].setElem(1,6, (8.591766 - 15.964)*self.kch.childAdaptor("Ring_Mag:IKickH01").doubleValue("mrad-per-v"))
        #self.lat[iH02].setElem(1,6, (8.026799 - 11.0)*self.kch.childAdaptor("Ring_Mag:IKickH02").doubleValue("mrad-per-v"))
        #self.lat[iH03].setElem(1,6, (7.993419 - 11.0)*self.kch.childAdaptor("Ring_Mag:IKickH03").doubleValue("mrad-per-v"))
        #self.lat[iH04].setElem(1,6, (9.104691 - 15.964)*self.kch.childAdaptor("Ring_Mag:IKickH04").doubleValue("mrad-per-v"))
        
        #self.lat[iV01].setElem(3,6, (7.353609 - 10.35)*self.kch.childAdaptor("Ring_Mag:IKickV01").doubleValue("mrad-per-v"))
        #self.lat[iV02].setElem(3,6, (8.110449 - 11.5)*self.kch.childAdaptor("Ring_Mag:IKickV02").doubleValue("mrad-per-v"))
        #self.lat[iV03].setElem(3,6, (7.637513- 11.0)*self.kch.childAdaptor("Ring_Mag:IKickV03").doubleValue("mrad-per-v"))
        #self.lat[iV04].setElem(3,6, (7.317667 - 10.35)*self.kch.childAdaptor("Ring_Mag:IKickV04").doubleValue("mrad-per-v"))
        
        #val = self.kch.childAdaptor("Ring_Mag:IKickH01").doubleValue("mrad-per-v")
        #print val,"     ",self.IkickV["Ring_Mag:IKickH01"]*self.dIKick["Ring_Mag:IKickH01"]/val
    
        """
        oneTurnMatrix = PhaseMatrix.identity()
        
        for mat in self.lat:
            oneTurnMatrix = mat.times(oneTurnMatrix)
            
        print oneTurnMatrix.toStringMatrix()
            
        ot4 = Matrix(4,4)
        otv = []
        for i in [0,1,2,3]:
            for j in [0,1,2,3]:
                ot4.set(i,j, int(i==j) - oneTurnMatrix.getElem(i,j))
            otv.append(oneTurnMatrix.getElem(i,6))
                
        im = ot4.inverse()
        
        x,xp,y,yp = 0,0,0,0
        for i in [0,1,2,3]:
            x += im.get(0,i)*otv[i]
            xp += im.get(1,i)*otv[i]
            y += im.get(2,i)*otv[i]
            yp += im.get(3,i)*otv[i]
            
        ev = PhaseVector(x, xp, y, yp, 0.0, 0.0)
        
        print ev
        print oneTurnMatrix.times(ev)
        
        num = len(self.lat)
        for i in range(num):
            ev = self.lat[i].times(ev)
            #if "BPM" in self.lids[i]:# == "Ring_Diag:BPM_B13":
            #print self.lids[i]," x = ",ev.getElem(0)
            print ev.getElem(2)
        """
        
        
        
            
        
        
    def getRealVoltage(self):
        for id in self.IkickIds:
            ch = self.caF.getChannel(id[:9] + "PS_" + id[9:]+":UTCA:AMPL")
            val = round(ch.getValDbl(),7)
            self.d['IKickParams'][id]['voltage-1'] = val
        
        

    def setupIKickers(self):
        for id in self.IkickIds:
            ch = self.caF.getChannel(id[:9] + "PS_" + id[9:]+":UTCA:AMPL")
            
            #val = self.kch.childAdaptor(id).doubleValue("mrad-per-v")
            val = self.d['IKickParams'][id]['mrad/V']
            vgoal = self.d['IKickParams'][id]['voltage-1'] + self.dIKick[id]/val
            ch.putVal(vgoal)
            self.dIKick[id] = 0.0
            self.d['IKickParams'][id]['voltage-1'] = vgoal
            
            
    def getRelKick23(self, id1,id2,id3):
        
        ind1 = self.injIds.index(id1)
        ind2 = self.injIds.index(id2)
        ind3 = self.injIds.index(id3)
        
        
        M2 = PhaseMatrix.identity()
        for i in range(ind2,ind3 + 1):
            M2 = self.injLat[i].times(M2)
            
        M12 = PhaseMatrix.identity()
        for i in range(ind1,ind3 + 1):
            M12 = self.injLat[i].times(M12)
            
            
            
        dk1 = 1.0
        if "IKickH" in id1:
            dk2 = - dk1*M12.getElem(0,1)/M2.getElem(0,1)
            dk3 = - dk1*(M12.getElem(1,1)*M2.getElem(0,1) - M12.getElem(0,1)*M2.getElem(1,1))/M2.getElem(0,1)

        if "IKickV" in id1:
            dk2 = - dk1*M12.getElem(2,3)/M2.getElem(2,3)
            dk3 = - dk1*(M12.getElem(3,3)*M2.getElem(2,3) - M12.getElem(2,3)*M2.getElem(3,3))/M2.getElem(2,3)
            
        return dk1, dk2, dk3
            
            
    def tripleValueEstimate(self, ind):

        id1, id2, id3 = self.d['TripleScan']['scan-' + str(ind)]['ids']
        dv1, dv2, dv3 = self.d['TripleScan']['scan-' + str(ind)]['dV']
        
        mradPerV1 = self.d['IKickParams'][id1]['mrad/V']
        mradPerV2 = self.d['IKickParams'][id2]['mrad/V']
        mradPerV3 = self.d['IKickParams'][id3]['mrad/V']
        
        dk1 = mradPerV1*dv1
        dk2 = mradPerV2*dv2
        dk3 = mradPerV3*dv3
        
        
        dk1rel, dk2rel, dk3rel = self.getRelKick23(id1,id2,id3)
        
        dk2 = dk1*dk2rel
        dk3 = dk1*dk3rel
            
        dv1 = dk1/mradPerV1
        dv2 = dk2/mradPerV2
        dv3 = dk3/mradPerV3

        self.d['TripleScan']['scan-' + str(ind)]['dV'] = [dv1, dv2, dv3]
        self.d['TripleScan']['scan-' + str(ind)]['status'] = 'calculated'

        
        
        
    def measureClosedOrbit(self, Npulses, pane):
        
        self.d['closed-orit'] = {}
        useBPMs = [bpm for bpm in self.bpms if bpm.getId() in self.d['useBPMs']]

        PvLogId = RemoteLoggingCenter().takeAndPublishSnapshot( "default", "Ring optics")
        print "PvLogId = ", PvLogId

        self.d['closed-orit']['pvLoggerId'] = PvLogId
        
        event = scanEvent(useBPMs)
        
        for i in range(Npulses):
            event.submitBatchRequest()
            event.populateXMLEvent()
            time.sleep(1)

            self.d['closed-orit']['pulse-' + str(i)] = {}
            for bpmId in self.d['useBPMs']:
                self.d['closed-orit']['pulse-' + str(i)][bpmId] = {}
                self.d['closed-orit']['pulse-' + str(i)][bpmId]['waveform-x'] = event.getTBT(bpmId,"x")[:self.d['WFLength']]
                self.d['closed-orit']['pulse-' + str(i)][bpmId]['waveform-y'] = event.getTBT(bpmId,"y")[:self.d['WFLength']]
                
            pane.addpulseToPlot(self.d['closed-orit']['pulse-' + str(i)])




    def optimizeOrbit(self, ind, pane):

        id1,id2,id3 = self.d['TripleScan']['scan-' + str(ind)]['ids']
        dv1,dv2,dv3 = self.d['TripleScan']['scan-' + str(ind)]['dV']
        
        ch1 = self.caF.getChannel(id1[:9] + "PS_" + id1[9:]+":UTCA:AMPL")
        ch2 = self.caF.getChannel(id2[:9] + "PS_" + id2[9:]+":UTCA:AMPL")
        ch3 = self.caF.getChannel(id3[:9] + "PS_" + id3[9:]+":UTCA:AMPL")
        
        v1 = ch1.getValDbl()
        v2 = ch2.getValDbl()
        v3 = ch3.getValDbl()
        
        #co = self.runAdaptor.childAdaptor("closed-orit")
        
        #Npulses = co.nodeCount()
        Npulses = len(self.d['closed-orit'])
        
        measuredBPMsIds = []
        useBPMs = [bpm for bpm in self.bpms if bpm.getId() in self.d['useBPMs']]
        bpmXaver = []
        bpmYaver = []
        
        for i in range(Npulses):
            
            measuredBPMsIds = []
            bpmX = []
            bpmY = []

            for bpmId in self.d['closed-orit']['pulse-' + str(i)].keys():
                
                arx = self.d['closed-orit']['pulse-' + str(i)][bpmId]['waveform-x']
                ary = self.d['closed-orit']['pulse-' + str(i)][bpmId]['waveform-y']
                
                averx = sum(arx)/self.d['WFLength']
                avery = sum(ary)/self.d['WFLength']
                
                measuredBPMsIds.append(bpmId)
                bpmX.append(averx)
                bpmY.append(avery)

            bpmXaver.append(bpmX)
            bpmYaver.append(bpmY)
            
        def aver(the_array):
            return [sum([the_array[j][i] for j in range(len(the_array))])/len(the_array) for i in range(len(the_array[0]))]

        indBPM = 0
        zind = []
        for bpm in self.bpms:
            if bpm.getId() in measuredBPMsIds:
                zind.append(indBPM)
            indBPM += 1
        
        bpmXaver = aver(bpmXaver)
        bpmYaver = aver(bpmYaver)
        
        
        gdx = BasicGraphData()
        gdx.setLineThick(2)
        gdx.addPoint(zind, bpmXaver)
        gdx.setGraphColor(Color.RED)
        pane.JP.addGraphData(gdx)
        
        gdy = BasicGraphData()
        gdy.setLineThick(2)
        gdy.addPoint(zind, bpmYaver)
        gdy.setGraphColor(Color.BLUE)
        pane.JP.addGraphData(gdy)        
        
        
        event = scanEvent(useBPMs)
        
        gdxlive = BasicGraphData()
        gdxlive.setGraphColor(Color.RED)
        gdylive = BasicGraphData()
        gdylive.setGraphColor(Color.BLUE)
        
        pane.JP.addGraphData(gdxlive)
        pane.JP.addGraphData(gdylive)
        
        
        
        def getRmsError():

            event.submitBatchRequest()
            event.populateXMLEvent()
            bpmX = []
            bpmY = []        
            for bpmId in self.d['useBPMs']:
                arx = event.getTBT(bpmId,"x")[:self.d['WFLength']]
                ary = event.getTBT(bpmId,"y")[:self.d['WFLength']]
                averx = sum(arx)/self.d['WFLength']
                avery = sum(ary)/self.d['WFLength']
                bpmX.append(averx)
                bpmY.append(avery)

            gdxlive.updateValues(zind, bpmX)
            gdylive.updateValues(zind, bpmY)
            
            errX = 0
            for i in range(len(bpmXaver)):
                errX += (bpmX[i] - bpmXaver[i])**2
            errX /= len(bpmXaver)
            
            errY = 0
            for i in range(len(bpmXaver)):
                errY += (bpmY[i] - bpmYaver[i])**2
            errY /= len(bpmYaver)
            
            return errX, errY

            
            
            
        var = ArrayList()
        delta_hint = InitialDelta()

        
        var2 = Variable(id2, dv2, -100.0, 100.0)
        var.add(var2)
        delta_hint.addInitialDelta(var2, 0.1)
        
        var3 = Variable(id3, dv3, -100.0, 100.0)
        var.add(var3)
        delta_hint.addInitialDelta(var3, 0.1)
        
        
        ch1.putVal(v1 + dv1)
        
        
        

        ro = self
        class MyScorer(Scorer):

            def __init__(self, variables):
                self.variables = variables
                self.j = 0

            def score(self, trial, variables_in):
                tp = trial.getTrialPoint()
                
                dv2 = tp.getValue(self.variables.get(0))
                dv3 = tp.getValue(self.variables.get(1))
                
                ch2.putVal(v2 + dv2)
                ch3.putVal(v3 + dv3)
                
                ro.d['TripleScan']['scan-' + str(ind)]['dV'][1] = dv2
                ro.d['TripleScan']['scan-' + str(ind)]['dV'][2] = dv3
                
                
                pane.ikd3.fireTableCellUpdated(ind, 3)
                pane.ikd3.fireTableCellUpdated(ind, 5)
                
                #self.resetBPMsfilter()
                
                time.sleep(30)
                
                errX, errY = getRmsError()
                
                #errX = dv2**2 + dv3**2
                
                if "IKickH" in id1:
                    print "dv2, dv3 = ", dv2, dv3, "errX = ",math.sqrt(errX)
                    return errX
                if "IKickV" in id2:
                    print "dv2, dv3 = ", dv2, dv3, "errY = ",math.sqrt(errY)
                    return errY


       
        

        maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(10000)
        
        self.solver = Solver(SimplexSearchAlgorithm(),maxSolutionStopper)
        scorer = MyScorer(var)

        problem = ProblemFactory.getInverseSquareMinimizerProblem(var,scorer,0.00001)
        problem.addHint(delta_hint)
        self.solver.solve(problem)
        
        self.d['TripleScan']['scan-' + str(ind)]['status'] = 'optimized'
        pane.ikd3.fireTableCellUpdated(ind, 6)
        
    def stopOptimization(self):
        self.solver.stopSolving()
        
        
    def analyseIKickers(self):
        
        
        h1divh4 = 0
        h2divh3 = 0
        h1divh2 = 0
        
        for ind in [0,1,2,3]:
            
            id1, id2, id3 = self.d['TripleScan']['scan-' + str(ind)]['ids']
            dv1, dv2, dv3 = self.d['TripleScan']['scan-' + str(ind)]['dV']
            
            k1rel, k2rel, k3rel = self.getRelKick23(id1,id2,id3)
            
            mradPerV1r = k1rel/dv1
            mradPerV2r = k2rel/dv2
            mradPerV3r = k3rel/dv3
            
            print [id1,id2,id3],"   ",[mradPerV1r, mradPerV2r, mradPerV3r]
            
            if ind == 0:
                h2divh3 += mradPerV2r/mradPerV3r
                h1divh2 += mradPerV1r/mradPerV2r
            if ind == 1:
                h1divh4 += mradPerV1r/mradPerV3r
                h1divh2 += mradPerV1r/mradPerV2r
            if ind == 2:
                h1divh4 += mradPerV1r/mradPerV3r
            if ind == 3:
                h2divh3 += mradPerV1r/mradPerV2r
                
        h1divh4 *= 0.5
        h2divh3 *= 0.5
        h1divh2 *= 0.5
                
        
        h1r = 1.0
        h2r = h1r/h1divh2
        h3r = h2r/h2divh3
        h4r = h1r/h1divh4
        
        
        self.relativeCoeff["Ring_Mag:IKickH01"] = h1r
        self.relativeCoeff["Ring_Mag:IKickH02"] = h2r
        self.relativeCoeff["Ring_Mag:IKickH03"] = h3r
        self.relativeCoeff["Ring_Mag:IKickH04"] = h4r
        

       
       
       
        v1divv4 = 0
        v2divv3 = 0
        v1divv2 = 0
        
        for ind in [4,5,6,7]:

            id1, id2, id3 = self.d['TripleScan']['scan-' + str(ind)]['ids']
            dv1, dv2, dv3 = self.d['TripleScan']['scan-' + str(ind)]['dV']
            
            
            k1rel, k2rel, k3rel = self.getRelKick23(id1,id2,id3)
            
            
            mradPerV1r = k1rel/dv1
            mradPerV2r = k2rel/dv2
            mradPerV3r = k3rel/dv3
            
            print [id1,id2,id3],"   ",[mradPerV1r, mradPerV2r, mradPerV3r]
            
            if ind == 4:
                v2divv3 += mradPerV2r/mradPerV3r
                v1divv2 += mradPerV1r/mradPerV2r
            if ind == 5:
                v1divv4 += mradPerV1r/mradPerV3r
                v1divv2 += mradPerV1r/mradPerV2r
            if ind == 6:
                v1divv4 += mradPerV1r/mradPerV3r
            if ind == 7:
                v2divv3 += mradPerV1r/mradPerV2r
                
        v1divv4 *= 0.5
        v2divv3 *= 0.5
        v1divv2 *= 0.5
                
        
        v1r = 1.0
        v2r = v1r/v1divv2
        v3r = v2r/v2divv3
        v4r = v1r/v1divv4
        
        
        self.relativeCoeff["Ring_Mag:IKickV01"] = v1r
        self.relativeCoeff["Ring_Mag:IKickV02"] = v2r
        self.relativeCoeff["Ring_Mag:IKickV03"] = v3r
        self.relativeCoeff["Ring_Mag:IKickV04"] = v4r
        

    def generateRingxdxf(self):
        
        ringdoc = XmlDataAdaptor.adaptorForFile( File(thisDir+"/design/Ring.xdxf"), False)
        
        ads1 = ringdoc.childAdaptor("xdxf").childAdaptors()
        
        for ad1 in ads1:
            ads2 = ad1.childAdaptors()
            for ad2 in ads2:
                id = ad2.stringValue("id")
                    
                node = self.seq.getNodeWithId(id)
                psId = node.getMainSupply().getId()

                if psId in self.psIds:
                    i = self.psIds.index(psId)
                    sign = +1
                    if "QV" in psId:
                        sign = -1
                    correctionCoefficient = self.d['BLQuadCorrections'][psId]
                    Bmax = self.BQuadSet[i]*correctionCoefficient
                    ad2.childAdaptor("attributes").childAdaptor("magnet").setValue("dfltMagFld",sign*Bmax)
                    #Tkin = self.runAdaptor.doubleValue("TkinNominalMeV")*1e6
                if psId in self.bendIds:
                    i = self.bendIds.index(psId)
                    Bmax = self.BBendSet[i]
                    
                    sign = +1
                    oldB = ad2.childAdaptor("attributes").childAdaptor("magnet").doubleValue("dfltMagFld")
                    if oldB < 0:
                        sign = -1
                    ad2.childAdaptor("attributes").childAdaptor("magnet").setValue("dfltMagFld",sign*Bmax)
                    
                    
        fileChooser = JFileChooser() 
        fileChooser.setDialogTitle('Select Export Location')
        fileChooser.setSelectedFile(File(thisDir+"/design/Ring.xdxf"))
        quserSelection = fileChooser.showSaveDialog(JPanel())

        if quserSelection == JFileChooser.APPROVE_OPTION:
            fileToSave = fileChooser.getSelectedFile()
            ringdoc.writeTo(fileToSave)
        
        

    


    def getFoilParameters(self, wf, usePulsesIds):
        
        bpmav = {}
        
        for bpmId in wf['pulse-0'].keys():
            

            
            bpmav[bpmId] = {}

            iterablesX = []
            iterablesY = []

            for pulseId in wf.keys():
                if pulseId in usePulsesIds:
                    iterablesX.append(wf[pulseId][bpmId]['waveform-x'])
                    iterablesY.append(wf[pulseId][bpmId]['waveform-y'])


            wfx = [sum(item)/len(iterablesX) for item in zip(*iterablesX)]
            wfy = [sum(item)/len(iterablesY) for item in zip(*iterablesY)]


            bpmav[bpmId]['waveform-x'] = wfx
            bpmav[bpmId]['waveform-y'] = wfy
            


            fitgx = GaussianSinusoidFit(wfx, self.d['WFLength'])
            fitgy = GaussianSinusoidFit(wfy, self.d['WFLength'])


            fitgx.solveWithNoiseMaxEvaluations(0.0, 200)
            fitgy.solveWithNoiseMaxEvaluations(0.0, 200)
            


            bpmav[bpmId]['offsetX'] = fitgx.getOffset()
            bpmav[bpmId]['offsetY'] = fitgy.getOffset()

            bpmav[bpmId]['tuneX'] = fitgx.getFractionalTune()
            bpmav[bpmId]['tuneY'] = fitgy.getFractionalTune()
            
            bpmav[bpmId]['ampX'] = fitgx.getAmplitude()
            bpmav[bpmId]['ampY'] = fitgy.getAmplitude()
            
            bpmav[bpmId]['phaseX'] = fitgx.getPhase()
            bpmav[bpmId]['phaseY'] = fitgy.getPhase()
            
            bpmav[bpmId]['omegaX'] = fitgx.getFrequency()
            bpmav[bpmId]['omegaY'] = fitgy.getFrequency()
            
            bpmav[bpmId]['kX'] = fitgx.getGrowthRate()
            bpmav[bpmId]['kY'] = fitgy.getGrowthRate()

            AmpX = bpmav[bpmId]['ampX']
            AmpY = bpmav[bpmId]['ampY']

            phaseX = bpmav[bpmId]['phaseX']
            phaseY = bpmav[bpmId]['phaseY']
            
            

            alphaX = self.alphasX[bpmId]
            alphaY = self.alphasY[bpmId]
            betaX = self.betasX[bpmId]
            betaY = self.betasY[bpmId]

            Xbpm  = AmpX*math.cos(phaseX)
            XPbpm = -AmpX*(math.sin(phaseX) + alphaX*math.cos(phaseX))/betaX

            Ybpm  = AmpY*math.cos(phaseY)
            YPbpm = -AmpY*(math.sin(phaseY) + alphaY*math.cos(phaseY))/betaY

            mi = self.matrFoilToNodeInv[bpmId]

            Xfoil = Xbpm*mi.get(0,0) + XPbpm*mi.get(0,1) + Ybpm*mi.get(0,2) + YPbpm*mi.get(0,3)
            XPfoil = Xbpm*mi.get(1,0) + XPbpm*mi.get(1,1) + Ybpm*mi.get(1,2) + YPbpm*mi.get(1,3)
            Yfoil = Xbpm*mi.get(2,0) + XPbpm*mi.get(2,1) + Ybpm*mi.get(2,2) + YPbpm*mi.get(2,3)
            YPfoil = Xbpm*mi.get(3,0) + XPbpm*mi.get(3,1) + Ybpm*mi.get(3,2) + YPbpm*mi.get(3,3)

            bpmav[bpmId]['foil-X'] = Xfoil
            bpmav[bpmId]['foil-XP'] = XPfoil
            bpmav[bpmId]['foil-Y'] = Yfoil
            bpmav[bpmId]['foil-YP'] = YPfoil
        
        return bpmav
    
    
    
    def calculateAllAverageWaveforms(self):
        
        for kickId in self.d['BPMWaveForms'].keys():
            for vid in self.d['BPMWaveForms'][kickId].keys():
                self.dav[kickId][vid] = self.getFoilParameters(self.d['BPMWaveForms'][kickId][vid], self.d['IKickParams'][kickId]['usepulses'][vid])
     
        return
                        
                        
             

    def startPhaseSpaceMeas(self, pane):

        self.stop = False
        
        useBPMs = [bpm for bpm in self.bpms if bpm.getId() in self.d['useBPMs']]
        event = scanEvent(useBPMs)
        
        self.dfoil['usepulses'] = []
        self.dfoil['BPMWaveForms'] = {}
        

        i = 0
        #for i in range(100):
        while not self.stop:
            event.submitBatchRequest()
            event.populateXMLEvent()
            time.sleep(1)
            self.dfoil['BPMWaveForms']['pulse-' + str(i)] = {}
            self.dfoil['usepulses'].append('pulse-' + str(i))

            for bpmId in self.d['useBPMs']:

                self.dfoil['BPMWaveForms']['pulse-' + str(i)][bpmId] = {}
                #self.dfoil['BPMWaveForms']['pulse-' + str(i)][bpmId]['waveform-x'] = event.getTBT(bpmId,"x")[:self.d['WFLength']]
                #self.dfoil['BPMWaveForms']['pulse-' + str(i)][bpmId]['waveform-y'] = event.getTBT(bpmId,"y")[:self.d['WFLength']]
                
                
                self.dfoil['BPMWaveForms']['pulse-' + str(i)][bpmId]['waveform-x'] = self.d['BPMWaveForms']["Ring_Mag:IKickH04"]['voltage-2']['pulse-' + str(i)][bpmId]['waveform-x']
                self.dfoil['BPMWaveForms']['pulse-' + str(i)][bpmId]['waveform-y'] = self.d['BPMWaveForms']["Ring_Mag:IKickH04"]['voltage-2']['pulse-' + str(i)][bpmId]['waveform-y']

            i += 1

            pane.updatePulsetable()
            pane.updatePulseselection()
            
            

            
            
            

        self.stop = True
        self.davfoil = self.getFoilParameters(self.dfoil['BPMWaveForms'], self.dfoil['usepulses'])

        
        
    def stopPhaseSpaceMeas(self):
        
        self.stop = True
        
        
        
    def rescaleMagnetsToEnergy(self, T0, T1):
        
        bg0 = math.sqrt(T0*1e6*(2*self.m + T0*1e6))/self.m
        bg1 = math.sqrt(T1*1e6*(2*self.m + T1*1e6))/self.m
        print (bg1/bg0)
        
        #change quad fields
        for i in range(len(self.psIds)):
            self.BQuadSet[i] = (bg1/bg0)*self.BQuadSet[i]
            self.BQuadSet0[i] = self.BQuadSet[i]
            
            
        #calculate kickers settings and kickers koefficients
        for id in self.IkickIds:

            self.d['IKickParams'][id]['voltage-1'] = (bg1/bg0)*self.d['IKickParams'][id]['voltage-1']
            self.d['IKickParams'][id]['mrad/V'] = self.d['IKickParams'][id]['mrad/V']/(bg1/bg0)
        
        #calculate new lattice and tunes
        self.SetQuadFields()
        self.tuneX, self.tuneY = self.getTunes()
        self.tuneX0, self.tuneY0 = self.tuneX, self.tuneY
        self.getMatrFoilToNode()
        

        print self.matrFoilToNode["Ring_Inj:Foil"].print(10,10)
            


        



                        
                            
                        
#refreshGraphJPanel()
                

        

            #if (pane != None):    
                #pane.addpulseToPlot(c1)
        

        """
        nlist = []
        
        event = scanEvent(self.UseBpms)
        event.submitBatchRequest()
        event.populateXMLEvent()
        
        for bpm in self.UseBpms:
            vx = event.getTBT(bpm.getId(),"x")
            vy = event.getTBT(bpm.getId(),"y")
            nlist.append(len(vx))
            nlist.append(len(vy))
            
        if nlist[1:] == nlist[:-1]:
            #if (self.Nturn > nlist[0]):
                    #self.Nturn = nlist[0]
            return nlist[0]
        else:
            return 0
        
        
        #ch.putVal(vgoal)
        print v1,v2,v3
        print dv1,dv2,dv3
        """
        
        
        
        
        """
        for i in range(len(tripleIKick)):
            scanchild = self.trs.createChild("scan-" + str(i))
            for id in tripleIKick[i]: 
                kickchild = scanchild.createChild(id)
                kickchild.setValue("dV",1.0)
                
        self.trs.childAdaptors().get(ind).childAdaptors().get(0)
        
        print "triple value changed"
        
        indH01 = self.injIds.index("Ring_Mag:IKickH01")
        indH02 = self.injIds.index("Ring_Mag:IKickH02")
        indH03 = self.injIds.index("Ring_Mag:IKickH03")
        indH04 = self.injIds.index("Ring_Mag:IKickH04")
        indV01 = self.injIds.index("Ring_Mag:IKickV01")
        indV02 = self.injIds.index("Ring_Mag:IKickV02")
        indV03 = self.injIds.index("Ring_Mag:IKickV03")
        indV04 = self.injIds.index("Ring_Mag:IKickV04")
        indFoil = self.injIds.index("Ring_Inj:Foil")

        H1f = PhaseMatrix.identity()
        for i in range(indH01+1,indFoil+1):
            H1f = self.injLat[i].times(H1f)

        V1f = PhaseMatrix.identity()
        for i in range(indV01+1,indFoil+1):
            V1f = self.injLat[i].times(V1f)
            
        H2f = PhaseMatrix.identity()
        for i in range(indH02+1,indFoil+1):
            H2f = self.injLat[i].times(H2f)

        V2f = PhaseMatrix.identity()
        for i in range(indV02+1,indFoil+1):
            V2f = self.injLat[i].times(V2f)
            
        
        M = Matrix(4,4)

        M.set(0,0,H1f.getElem(0,1))
        M.set(1,0,H1f.getElem(1,1))
        M.set(2,0,H1f.getElem(2,1))
        M.set(3,0,H1f.getElem(3,1))
        
        M.set(0,1,H2f.getElem(0,1))
        M.set(1,1,H2f.getElem(1,1))
        M.set(2,1,H2f.getElem(2,1))
        M.set(3,1,H2f.getElem(3,1))
        
        M.set(0,2,V1f.getElem(0,3))
        M.set(1,2,V1f.getElem(1,3))
        M.set(2,2,V1f.getElem(2,3))
        M.set(3,2,V1f.getElem(3,3))
        
        M.set(0,3,V2f.getElem(0,3))
        M.set(1,3,V2f.getElem(1,3))
        M.set(2,3,V2f.getElem(2,3))
        M.set(3,3,V2f.getElem(3,3))
        
        Minv = M.inverse()

        self.dIKick["Ring_Mag:IKickH01"] = self.dx*Minv.get(0,0) + self.dxp*Minv.get(0,1) + self.dy*Minv.get(0,2) + self.dyp*Minv.get(0,3)
        self.dIKick["Ring_Mag:IKickH02"] = self.dx*Minv.get(1,0) + self.dxp*Minv.get(1,1) + self.dy*Minv.get(1,2) + self.dyp*Minv.get(1,3)
        self.dIKick["Ring_Mag:IKickV01"] = self.dx*Minv.get(2,0) + self.dxp*Minv.get(2,1) + self.dy*Minv.get(2,2) + self.dyp*Minv.get(2,3)
        self.dIKick["Ring_Mag:IKickV02"] = self.dx*Minv.get(3,0) + self.dxp*Minv.get(3,1) + self.dy*Minv.get(3,2) + self.dyp*Minv.get(3,3)
        """

        
        """
        indV03 = self.lids.index("Ring_Mag:IKickV03")
        indH03 = self.lids.index("Ring_Mag:IKickH03")
        indV04 = self.lids.index("Ring_Mag:IKickV04")
        indH04 = self.lids.index("Ring_Mag:IKickH04")
        indFoil = self.lids.index("Ring_Inj:Foil")
        num = len(self.lids)
        
        
        
        fH4 = PhaseMatrix.identity()
        for i in range(indFoil, num) + range(1,indH04 + 1):
            fH4 = self.lat[i].times(fH4)
        
        V3H4 = PhaseMatrix.identity()
        for i in range(indV03, indH04 + 1):
            V3H4 = self.lat[i].times(V3H4)
            
        H3H4 = PhaseMatrix.identity()
        for i in range(indH03, indH04 + 1):
            H3H4 = self.lat[i].times(H3H4)
        
        V4H4 = PhaseMatrix.identity()
        for i in range(indV04, indH04 + 1):
            V4H4 = self.lat[i].times(V4H4)
            


        M = Matrix(4,4)

        M.set(0,0,V3H4.getElem(0,3))
        M.set(1,0,V3H4.getElem(1,3))
        M.set(2,0,V3H4.getElem(2,3))
        M.set(3,0,V3H4.getElem(3,3))
        
        M.set(0,1,H3H4.getElem(0,1))
        M.set(1,1,H3H4.getElem(1,1))
        M.set(2,1,H3H4.getElem(2,1))
        M.set(3,1,H3H4.getElem(3,1))
        
        M.set(0,2,V4H4.getElem(0,3))
        M.set(1,2,V4H4.getElem(1,3))
        M.set(2,2,V4H4.getElem(2,3))
        M.set(3,2,V4H4.getElem(3,3))
        
        M.set(0,3, 0.0)
        M.set(1,3, 1.0)
        M.set(2,3, 0.0)
        M.set(3,3, 0.0)
        
        Mfh4 = Matrix(4,4)
        
        for i in [0,1,2,3]:
            for j in [0,1,2,3]:
                Mfh4.set(i,j, fH4.getElem(i,j))
                
        C = (M.inverse()).times(Mfh4).uminus()

        self.dIKick["Ring_Mag:IKickV03"] = self.dx*C.get(0,0) + self.dxp*C.get(0,1) + self.dy*C.get(0,2) + self.dyp*C.get(0,3)
        self.dIKick["Ring_Mag:IKickH03"] = self.dx*C.get(1,0) + self.dxp*C.get(1,1) + self.dy*C.get(1,2) + self.dyp*C.get(1,3)
        self.dIKick["Ring_Mag:IKickV04"] = self.dx*C.get(2,0) + self.dxp*C.get(2,1) + self.dy*C.get(2,2) + self.dyp*C.get(2,3)
        self.dIKick["Ring_Mag:IKickH04"] = self.dx*C.get(3,0) + self.dxp*C.get(3,1) + self.dy*C.get(3,2) + self.dyp*C.get(3,3)
        """
        

        
        """
        vect = PhaseVector(0., 0., 0., 0., 0., 0.)
        for i in range(indH01,num)+range(1,indH04 + 1):
            
            
            if i == indH01:
                self.lat[i].setElem(1,6, kh1)
            if i == indH02:
                self.lat[i].setElem(1,6, kh2)
            if i == indH03:
                self.lat[i].setElem(1,6, kh3)
            if i == indH04:
                self.lat[i].setElem(1,6, kh4)
                
            if i == indV01:
                self.lat[i].setElem(3,6, kv1)
            if i == indV02:
                self.lat[i].setElem(3,6, kv2)
            if i == indV03:
                self.lat[i].setElem(3,6, kv3)
            if i == indV04:
                self.lat[i].setElem(3,6, kv4)
            
            
            
            vect = self.lat[i].times(vect)
            print self.lids[i], vect.toString()
        """
            
                
        
        

        #chKick = self.runAdaptor.childAdaptor(kickId)
        
        #for i in range(100):
        #    print i
            #time.sleep(1)
        
        
        #adaptors = chKick.childAdaptors()
        #chKick = self.runAdaptor.createChild(id)
        
        
        #ch = self.runAdaptor.createChild(self.IkickIds[0])
        
        #mainNode.getChildNodes()
        #doc = self.runAdaptor.document()
        #newadapt = XmlDataAdaptor(doc)
        #newdoc = newadapt.document()
        #nodeList = newdoc.getChildNodes()
        #print "name = ",nodeList.item(0)
        #print "nodeList.getLength()",nodeList.getLength()
        #print "nodeList.getLength()",nodeList.size()
        #print "nodeList.getLength()",nodeList.nodeCount()
        
        #for i in range(nodeList.getLength()):
        #    print i, nodeList.item(i)
        
        #for node in nodeList:
            #print "777",node
        

        #adaptors = self.runAdaptor.childAdaptors() #no
        
        #print nodeList.getLength()
        #print adaptors.size()           #no

        #print self.runAdaptor.name()
        #print doc.getNodeName()
        

        
        #ch1 = chKick.childAdaptor("Ring_Mag:PS_IKickH01ch1")
        #removeChild
        
        #mnode = chKick.document()
        #chnode = ch1.document()
        #chKick.removeChild(ch1)
        
        #print chKick.nodeCount()
        #print adaptors.size()

        #event = scanEvent(self.UseBpms)

        #event.submitBatchRequest()
        #event.populateXMLEvent()
        
        #print self.UseBpms[1].getId(),event.getTBT(self.UseBpms[1].getId(),"x")
        #event.submitBatchRequest()
        #event.populateXMLEvent()
        #print self.UseBpms[1].getId(),event.getTBT(self.UseBpms[1].getId(),"x")
        #orb = event.getTBT(self.UseBpms[1].getId(),"x")
        #chKick.createChild("new")
        #chKick.setValue("orbitX",orb)
        #print orb
        
            

        #for i in range(self.Nmeas):
        #for i in range(100):
        #    print i
            #time.sleep(1)
            

    
    """
    if col > 1:

        if row < 6:
            calculate_grad()

            sol = ov.getVectorSolution(grad[row], [grad[k] for k in range(len(grad)) if k != row])
            VectordQ = ov.getVectorForValue(sol, grad[row], increment)

            q0 = getQuads()
            setQuads([q0[i] + VectordQ[i]  for i in range(len(q0))])
            run_model()

        if row == 6:
            calculate_grad_dz()

            sol = ov.getVectorSolution(grad[5], [grad[k] for k in range(len(grad)) if k != 5])
            VectordQ = ov.getVectorForValue(sol, grad[5], increment)

            q0 = getQuads()
            setQuads([q0[i] + VectordQ[i]  for i in range(len(q0))])

            run_model()
            change_ip_position()


        table_twiss.changeSelection(row, col, False, False)
        """




        
                
                
                
"""       
    





def rf(num):
    if abs(num) < 10e-10:
        return 0
    return num








phiX=0
phiY=0
for i in range(0, num - 1):
    cx=betaX*lat[i].getElem(0,0)-alphaX*lat[i].getElem(0,1)
    cy=betaY*lat[i].getElem(2,2)-alphaY*lat[i].getElem(2,3)
    phiX+=math.atan(lat[i].getElem(0,1)/cx)
    phiY+=math.atan(lat[i].getElem(2,3)/cy)
        
    alphaX, betaX=-(cx*(betaX*lat[i].getElem(1,0)-alphaX*lat[i].getElem(1,1))+lat[i].getElem(0,1)*lat[i].getElem(1,1))/betaX, (cx**2+lat[i].getElem(0,1)**2)/betaX
    alphaY, betaY=-(cy*(betaY*lat[i].getElem(3,2)-alphaY*lat[i].getElem(3,3))+lat[i].getElem(2,3)*lat[i].getElem(3,3))/betaY, (cy**2+lat[i].getElem(2,3)**2)/betaY
    

print phiX/(2*math.pi)
print phiY/(2*math.pi)

print alphaX, alphaY, betaX, betaY

#im = PhaseMatrix.identity()
#print im
#print im.getElem(0,0)
#im.setElem(0,0,7)
#print im.plus(im)



sys.exit()
"""

