#
# This script finds the initial conditions at the CCL entrance
# calculates the simulated orbit, and corrects it.
# It uses live data source
#

import sys
import math
import types
import time

from jarray import *
from java.lang import *
from java.util import *
from java.io import *
from javax.swing import *
from java.awt import *
from java.awt.event import ActionListener

from java.awt.event import WindowAdapter

from org.xml.sax import *
from xal.smf import *
from xal.smf.data import *
from xal.model import *
from xal.model.probe import *
from xal.sim.scenario import *
from xal.tools.beam import *
from xal.extension.widgets.plot import *
from xal.smf.data import *
from xal.model.alg import *
from xal.model.probe import *
from xal.model.probe.traj import *
from xal.smf.impl.qualify import *
from xal.smf.impl import *
from xal.extension.solver import *
from xal.extension.solver.hint import InitialDelta


false= Boolean("false").booleanValue()
true= Boolean("true").booleanValue()
null = Integer.getInteger("")

class AccNode:
	def __init__(self, Id, node = None):
		self.node = node
		if(node != None):
			self.Id = node.getId()
		else:
			self.Id = Id
		self.offSetX = None
		self.offSetY = None
		self.posS = 0.
		self.posX = 0.
		self.posY = 0.
		self.posExperX = 0.
		self.posExperY = 0.
		self.phMatr = None

	def getNode(self):
		return self.node

	def getId(self):
		return self.Id

	def setPhMatr(self,phMatr):
		self.phMatr = phMatr

	def setOffSets(self, offSetX, offSetY):
		self.offSetX = offSetX
		self.offSetY = offSetY

	def setPosS(self, s):
		self.posS = s

	def getPosS(self):
		return self.posS

	def getPosX(self):
		return self.posX

	def getPosY(self):
		return self.posY

	def getPosExperX(self):
		return self.posExperX

	def getPosExperY(self):
		return self.posExperY

	def setPosExperXY(self, x, y):
		self.posExperX = x
		self.posExperY = y

	def getDiff2(self):
		diffX = self.posExperX - self.posX
		diffY = self.posExperY - self.posY
		return (diffX*diffX+diffY*diffY)

	def track(self,phaseVector):
#		print "Tracking Node: %s" % ( self.getId() )
#		print "\t Input phase vector: %s" % ( phaseVector )
#		print "\t Phase Matrix: %s" % ( self.phMatr )

		if(self.offSetX != None):
			phaseVector.setx(phaseVector.getx()+self.offSetX/1000.)
		if(self.offSetY != None):
			phaseVector.sety(phaseVector.gety()+self.offSetY/1000.)
		self.posX = phaseVector.getx()*1000.
		self.posY = phaseVector.gety()*1000.
		if(self.phMatr != None):
			phaseVector = self.phMatr.times(phaseVector)
		if(self.offSetX != None):
			phaseVector.setx(phaseVector.getx()-self.offSetX/1000.)
		if(self.offSetY != None):
			phaseVector.sety(phaseVector.gety()-self.offSetY/1000.)

#		print "\t Output phase vector: %s\n" % ( phaseVector )

		return phaseVector
		
	def track1(self,phaseVector):
		#this method will do traking, but orbit will be calculated 
		#as (x,y) position of the beam outside the acc. element
		if(self.offSetX != None):
			phaseVector.setx(phaseVector.getx()+self.offSetX/1000.)
		if(self.offSetY != None):
			phaseVector.sety(phaseVector.gety()+self.offSetY/1000.)
		if(self.phMatr != None):
			phaseVector = self.phMatr.times(phaseVector)
		if(self.offSetX != None):
			phaseVector.setx(phaseVector.getx()-self.offSetX/1000.)
		if(self.offSetY != None):
			phaseVector.sety(phaseVector.gety()-self.offSetY/1000.)
		self.posX = phaseVector.getx()*1000.
		self.posY = phaseVector.gety()*1000.			
		return phaseVector

class AccCalculator(Scorer):
	"""
	This class calculates orbit with account of quads offset
	"""
	def __init__( self, accSeq, nodes, offSetsFile, variable_proxies ):
		self.accSeq = accSeq
		self.nodes = nodes

		self.proxiesV = variable_proxies
		self.x0_Proxy = self.proxiesV.get( 0 )
		self.xp0_Proxy = self.proxiesV.get( 1 )
		self.y0_Proxy = self.proxiesV.get( 2 )
		self.yp0_Proxy = self.proxiesV.get( 3 )

		#read offsets parameters file
		dicOffsets = {}
		inF = open(offSetsFile)
		while(1 < 2):
			ln = inF.readline()
			if(len(ln) == 0):
				break
			vals = ln.split()
			if(len(vals) == 3):
				dicOffsets[vals[0]] = (float(vals[1]),float(vals[2]))
		inF.close()
		#-----------------------------
		scenario = Scenario.newScenarioFor(accSeq)
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN)

#		ptracker = AlgorithmFactory.createEnvTrackerAdapt( accSeq )
#		probe = ProbeFactory.getEnvelopeProbe( accSeq, ptracker )

		ptracker = AlgorithmFactory.createParticleTracker( accSeq )
		probe = ProbeFactory.createParticleProbe( accSeq, ptracker )

		scenario.setProbe(probe)
		probe.reset()
		#probe.setBeamCharge(0.0)
		scenario.resync()
		scenario.run()
		traj = scenario.getTrajectory()
		self.accNodes = []
		self.accNodeElems = []
		self.AccNodeBPMs = []
		self.AccNodeQuads = []
		self.accNodes.append(AccNode("start"))
		phMatr0 = PhaseMatrix(traj.stateWithIndex(0).getResponseMatrix())
		s_old = 0
		for node in nodes:
			nodeId = node.getId()
			inds = traj.indicesForElement(nodeId)
			stateStart = traj.stateWithIndex(inds[0]-1)
			stateStop = traj.stateWithIndex(inds[len(inds)-1])
			phMatr1 =  PhaseMatrix(stateStart.getResponseMatrix())
			phMatr2 =  PhaseMatrix(stateStop.getResponseMatrix())
			phMatrD =  phMatr1.times(phMatr0.inverse())
			phMatrE = phMatr2.times(phMatr1.inverse())
			phMatr0 = phMatr2
			#print "debug id=",nodeId," matr=",phMatrE.toString()

#			print "node: %s" % ( nodeId )
#			print "\t matr1: %s" % ( phMatr1 )
#			print "\t matr2: %s" % ( phMatr2 )
#			print "\t matrD: %s" % ( phMatrD )
#			print "\t matrE: %s" % ( phMatrE )
#			print ""

			accNodeD = AccNode("general")
			accNodeE = AccNode(nodeId,node)
			accNodeD.setPhMatr(phMatrD)
			accNodeE.setPhMatr(phMatrE)
			s = accSeq.getPosition(node)
			accNodeD.setPosS((s+s_old)/2.0)
			accNodeE.setPosS(s)
			if(dicOffsets.has_key(nodeId)):
				(offSetX,offSetY) = dicOffsets[nodeId]
				accNodeE.setOffSets(offSetX,offSetY)
			s_old = s
			if(nodeId.rfind("BPM") >= 0):
				self.AccNodeBPMs.append(accNodeE)
				x_bpm = node.getXAvg()
				y_bpm = node.getYAvg()
				accNodeE.setPosExperXY(x_bpm,y_bpm)
			if(nodeId.rfind("CCL_Mag:") >= 0):
				self.AccNodeQuads.append(accNodeE)
			self.accNodes.append(accNodeD)
			self.accNodes.append(accNodeE)
			self.accNodeElems.append(accNodeE)


	def score( self, trial, variables ):
		return self.raw_score()

	def raw_score( self ):
		""" Returns different between experiments and simulation"""
		x0 = self.x0_Proxy.getValue()
		xp0 = self.xp0_Proxy.getValue()
		y0 = self.y0_Proxy.getValue()
		yp0 = self.yp0_Proxy.getValue()
		phaseVector = PhaseVector(x0/1000.,xp0/1000.,y0/1000.,yp0/1000.,0.,0.)

		#print x0, xp0, y0, yp0
#		print "Input phase vector: %s" % ( phaseVector )

		for accNode in self.accNodes:
			phaseVector = accNode.track(phaseVector)

#		print "Output phase vector: %s" % ( phaseVector )

		diff2 = 0.
		for accNode in self.AccNodeBPMs:
			diff2 = diff2 + accNode.getDiff2()

		if(len(self.AccNodeBPMs) > 0):
			diff2 = diff2 /len(self.AccNodeBPMs)

#		print "Mean diff2: %s" % ( diff2 )

		return diff2

	def track1(self):
		# the (x,y) position of the beam will be calculated
		# as outside the acc.elements 
		x0 = self.x0_Proxy.getValue()
		xp0 = self.xp0_Proxy.getValue()
		y0 = self.y0_Proxy.getValue()
		yp0 = self.yp0_Proxy.getValue()
		phaseVector = PhaseVector(x0/1000.,xp0/1000.,y0/1000.,yp0/1000.,0.,0.)
		for accNode in self.accNodes:
			phaseVector = accNode.track1(phaseVector)	

	def getAccNodesBPMs(self):
		return self.AccNodeBPMs

	def getAccNodesQuads(self):
		return self.AccNodeQuads

	def getMaxDiff(self):
		maxDiff = 0.
		for accNode in self.AccNodeBPMs:
			diff = math.fabs(accNode.getPosExperX() - accNode.getPosX())
			if(maxDiff < diff): maxDiff = diff
			diff = math.fabs(accNode.getPosExperY() - accNode.getPosY())
			if(maxDiff < diff): maxDiff = diff
		return maxDiff

	def getInitProxiesV(self):
		return self.proxiesV

	def getAccSeq(self):
		return self.accSeq

class AccCorr:
	"""
	The corrector class that keeps the dictionary with coeffitients to the
	particular quad. These coffitients multiplied by the corrector's field
	will give the orbit change at the position of the quad.
	"""
	def __init__( self, dcorr, problem, hint ):
		self.dcorr = dcorr
		self.dicCoeff = {}
		self.memB = dcorr.getField()
		self.maxB = dcorr.upperFieldLimit()
		self.minB = dcorr.lowerFieldLimit()
		step = math.fabs(self.maxB*0.15)

		field_variable = Variable( dcorr.getId()+":B", self.memB, self.minB, self.maxB )
		problem.addVariable( field_variable )
		hint.addInitialDelta( field_variable, step )
		self.fieldProxy = problem.getValueReference( field_variable )

		#self.fieldProxy = ParameterProxy(dcorr.getId()+":B", self.memB, step)

		self.width =math.fabs(self.maxB)*0.02
		self.hight = 10.0

	def getCorr(self):
		return self.dcorr

	def getProxy(self):
		return self.fieldProxy

	def getMemB(self):
		return self.memB

	def reduceWeightWidth(self):
		self.width = self.width/2.0
		self.hight = self.hight*2.0

	def getWeight(self):
		B = self.fieldProxy.getValue()
		dlow = (B - self.minB)/self.width
		dupp = (B - self.maxB)/self.width
		if(math.fabs(dlow) > 30): dlow = 30.0*math.fabs(dlow)/dlow
		if(math.fabs(dupp) > 30): dupp = 30.0*math.fabs(dupp)/dupp
		w = self.hight*(math.exp(-dlow)+math.exp(dupp))
		w = 0.
		return w

	def isValid(self, B):
		rez = (2 > 1)
		if(B < self.maxB and B > self.minB): return rez
		return (not rez)

	def addCoeff(self, quad, coeff):
		self.dicCoeff[quad] = coeff

	def setField(self):
		self.dcorr.setField(self.fieldProxy.getValue())

	def addOrbitChange(self, quadOrbDic):
		for quad in quadOrbDic.keys():
			if(self.dicCoeff.has_key(quad)):
				quadOrbDic[quad] = quadOrbDic[quad] + (self.fieldProxy.getValue() - self.memB)*self.dicCoeff[quad]

class OrbitCorr(Scorer):
	"""
	This class will calculate the fields in dipole correctors
	to correct the orbit. The accelerator sequence from accCalc
	is different from accSeq, because accSeq includes dipoles from
	prevous section. The accSeq should include the whole sequence
	form accCalc.
	orientation = "DCH" or "DCV"
	"""
	def __init__( self, accCalc, accSeq, dcorrs, orientation, problem, hint ):
		if(orientation != "DCH" and orientation != "DCV"):
			print "orientation should be DCH or DCV!"
			print "orientation =",orientation
			print "Stop"
			sys.exit(1)
		#------------------------------------------------
		self.accCalc = accCalc
		self.accSeq = accSeq
		self.orientation = orientation
		#------------------------------------------------
		scenario = Scenario.newScenarioFor(accSeq)
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN)

#		ptracker = EnvelopeTracker()
#		probe = ProbeFactory.getEnvelopeProbe(accSeq, ptracker)

		ptracker = AlgorithmFactory.createEnvTrackerAdapt( accSeq )
		probe = ProbeFactory.getEnvelopeProbe( accSeq, ptracker )

		scenario.setProbe(probe)
		probe.reset()
		#probe.setBeamCharge(0.0)
		probe.setBeamCurrent( 0.0 )
		scenario.resync()
		scenario.run()
		traj = scenario.getTrajectory()
		#---------  we have trajectory -------------------
		self.accQuads = accCalc.getAccNodesQuads()
		self.accCorrs = []
		self.proxiesV = Vector()
		for dcorr in  dcorrs:
			accCorr = AccCorr( dcorr, problem, hint )
			probeState = traj.statesForElement(dcorr.getId())[0]
			W0 = probeState.getSpeciesRestEnergy()
			gamma = probeState.getGamma()
			beta = math.sqrt(1.0 - 1.0 / (gamma * gamma))
			L = dcorr.getEffLength()
			c = 2.997924E+8
			res_coeff = (L * c) / (W0 * beta * gamma)
			nQuads = 0
			for accQuad in self.accQuads:
				quad = accQuad.getNode()
				dcorr_pos = self.accSeq.getPosition(dcorr)
				quad_pos = self.accSeq.getPosition(quad)
				if(quad_pos > dcorr_pos):
					phMatr = traj.stateResponse(dcorr.getId(), quad.getId())
					me = 0.
					if(orientation == "DCH"): me = phMatr.getElem(0, 1)
					if(orientation == "DCV"): me = phMatr.getElem(2, 3)
					#result [mm/T] (1000 coeff it is Question!!!!!)
					coeff = res_coeff * me * 1000.
					accCorr.addCoeff(quad,coeff)
					nQuads = nQuads + 1
			if(nQuads > 0):
				self.proxiesV.add(accCorr.getProxy())
				self.accCorrs.append(accCorr)
		#------------------------------------------------
		#Set the quads trajectories
		self.dicQuadsTraj_Init = {}
		self.dicQuadsTraj = {}
		for accQuad in self.accQuads:
			quad = accQuad.getNode()
			if(orientation == "DCH"):
				self.dicQuadsTraj_Init[quad] = accQuad.getPosX()
			if(orientation == "DCV"):
				self.dicQuadsTraj_Init[quad] = accQuad.getPosY()
			self.dicQuadsTraj[quad] = self.dicQuadsTraj_Init[quad]

	def getProxiesV(self):
		return self.proxiesV

	def getTrajDic(self):
		return self.dicQuadsTraj


	def score( self, trial, variables ):
		return self.raw_score()


	def raw_score( self ):
		""" Returns resulting orbit deviation"""
		for quad in self.dicQuadsTraj_Init.keys():
			self.dicQuadsTraj[quad] = self.dicQuadsTraj_Init[quad]
		corr_w= 0.
		for accCorr in self.accCorrs:
			accCorr.addOrbitChange(self.dicQuadsTraj)
			corr_w = corr_w + accCorr.getWeight()
		diff2 = 0.
		i = 0
		for quad in self.dicQuadsTraj.keys():
			diff = self.dicQuadsTraj[quad]
			diff2 = diff2 + diff*diff
			i = i + 1
		if(i > 0): diff2=diff2/i
		return (diff2+corr_w)

	def applyCorrections(self):
		for accCorr in self.accCorrs:
			accCorr.setField()

	def printCorrectors(self):
		for accCorr in self.accCorrs:
			print accCorr.getCorr()," B= %6.4f "% accCorr.getProxy().getValue(),"memB = %6.4f  max,min = %6.4f  %6.4f "%(accCorr.memB, accCorr.maxB,accCorr.minB)

#window closer will kill this apps
class WindowCloser(WindowAdapter):
	def windowClosing(self,windowEvent):
		sys.exit(1)
#===============================================================
#              MAIN PROGRAM
#===============================================================

# read the accelerator & make the sequence
accl = XMLDataManager.loadDefaultAccelerator()

lst = ArrayList()
ccl1 = accl.getSequence("CCL1")
ccl2 = accl.getSequence("CCL2")
ccl3 = accl.getSequence("CCL3")
ccl4 = accl.getSequence("CCL4")

lst.add(ccl1)
lst.add(ccl2)
lst.add(ccl3)
lst.add(ccl4)

#+++++++++++++++++++++++++++++++++++++++++++++
accSeq = AcceleratorSeqCombo("SEQUENCE", lst)
nodeQualifier = AndTypeQualifier().and((OrTypeQualifier()).or(BPM.s_strType).or(Quadrupole.s_strType))
nodeQualifier.andStatus(true)
nodes_init = accSeq.getAllNodesWithQualifier(nodeQualifier)
#accSeq.getAllNodesOfType(HDipoleCorr.s_strType);

#bpm_bad_list = ["CCL_Diag:BPM312","CCL_Diag:BPM402","CCL_Diag:BPM409"]
bpm_bad_list = []

quads = []
bpms = []
dicIdToNode = {}
nodes = []
for node in nodes_init:
	s = accSeq.getPosition(node)
	name = node.getId()
	dicIdToNode[name] = node
	if(name.rfind("BPM") >= 0):
		if(name not in bpm_bad_list):
			bpms.append(node)
			nodes.append(node)
			#print "s=",s," bpm=",name
	if(name.rfind("CCL_Mag:") >= 0):
		quads.append(node)
		nodes.append(node)

#+++++++++++++++++++++++++++++++++++++++++++++++
# locate the enclosing folder and get the offset data file
script_folder = File( sys.argv[0] ).getParentFile()
offSetsFileName = File( script_folder, "ccl_bpm_quad_offsets_final.dat" ).getPath()
#accCalc = AccCalculator(accSeq , nodes, offSetsFileName)

#----------------------------------------------------------
#make GUI
#----------------------------------------------------------
frame = JFrame("CCL ORBIT Correction")
frame.getContentPane().setLayout(BorderLayout())

plotsPanel = JPanel(GridLayout(2,1)) 
plotsBeforePanel = JPanel(GridLayout(2,1)) 
plotsAfterPanel = JPanel(GridLayout(2,1)) 
plotsPanel.add(plotsBeforePanel)
plotsPanel.add(plotsAfterPanel)

border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"CCL Orbit Before Correction")
plotsBeforePanel.setBorder(border)

border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"CCL Orbit After Correction (Prediction)")
plotsAfterPanel.setBorder(border)

buttonPanel = JPanel(GridLayout(2,1))
leftPanel = JPanel(BorderLayout())
textPanel = JPanel(BorderLayout())
leftPanel.add(buttonPanel,BorderLayout.NORTH)
leftPanel.add(textPanel,BorderLayout.CENTER)

textArea = JTextArea()
textArea.setText(null)
textPanel.add(textArea,BorderLayout.CENTER)

plotButton = JButton(" Calculate Existing CCL Orbit (wait 30 sec) ") 
correctButton = JButton(" Flatten the CCL Orbit (wait 2 min) ") 
buttonPanel.add(plotButton )
buttonPanel.add(correctButton)

frame.getContentPane().add(plotsPanel,BorderLayout.CENTER)
frame.getContentPane().add(leftPanel,BorderLayout.WEST)

frame.addWindowListener(WindowCloser())

frame.setSize(Dimension(800,600))
frame.show()


class MinimizingObjective(Objective):
	def __init__( self, tolerance ):
		Objective.__init__( self, "MinimizingObjective" )
		self.tolerance = tolerance

	def satisfaction( self, score ):
		satisfaction = SatisfactionCurve.inverseSatisfaction( score, self.tolerance )
		return satisfaction


class ScoringEvaluator(Evaluator):
	def __init__( self, scorer, variables, objective ):
		self.scorer = scorer
		self.variables = variables
		self.objective = objective

	def evaluate( self, trial ):
		score = self.scorer.score( trial, self.variables )
		trial.setScore( self.objective, score )
		#print "score: %s" % score


#-----------------------------------------
#make button action
#-----------------------------------------

class PlotActionListener(ActionListener):
	def __init__( self):
		self.accCalc = None
	
	def getAccCalc(self):
		return self.accCalc
	
	def actionPerformed(self,e):
		self.problem = Problem()
		objective = MinimizingObjective( 0.05 )
		self.problem.addObjective( objective )

		hint = InitialDelta( 0.5 )
		self.problem.addHint( hint )

		x0_var = Variable( "x0", 0.0, -10.0, 10.0 )
		hint.addInitialDelta( x0_var, 0.5 )
		self.problem.addVariable( x0_var )
		self.x0_Proxy = self.problem.getValueReference( x0_var )

		xp0_var = Variable( "xp0", 0.0, -10.0, 10.0 )
		hint.addInitialDelta( xp0_var, 0.5 )
		self.problem.addVariable( xp0_var )
		self.xp0_Proxy = self.problem.getValueReference( xp0_var )

		y0_var = Variable( "y0", 0.0, -10.0, 10.0 )
		hint.addInitialDelta( y0_var, 0.5 )
		self.problem.addVariable( y0_var )
		self.y0_Proxy = self.problem.getValueReference( y0_var )

		yp0_var = Variable( "yp0", 0.0, -10.0, 10.0 )
		hint.addInitialDelta( yp0_var, 0.5 )
		self.problem.addVariable( yp0_var )
		self.yp0_Proxy = self.problem.getValueReference( yp0_var )

		variable_proxies = Vector()
		variable_proxies.add( self.x0_Proxy )
		variable_proxies.add( self.xp0_Proxy )
		variable_proxies.add( self.y0_Proxy )
		variable_proxies.add( self.yp0_Proxy )

		self.accCalc = AccCalculator(accSeq , nodes, offSetsFileName, variable_proxies )
		accCalc = self.accCalc
		evaluator = ScoringEvaluator( self.accCalc, self.problem.getVariables(), objective )
		self.problem.setEvaluator( evaluator )

		textArea.setText(null)
		plotsBeforePanel.removeAll()
		#find initial conditions
		solver = Solver( SolveStopperFactory.maxEvaluationsStopper( 2500 ) )
		res = "Score= %6.4f maxDiff= %6.3f \n"%(accCalc.raw_score(),accCalc.getMaxDiff())
		textArea.append(res)
		textArea.append("===after CCL entrance coord. fit ===\n")
		solver.solve( self.problem )
		scoreboard = solver.getScoreBoard()
		best_solution = scoreboard.getBestSolution()
#		print best_solution

		# apply the best solution to the model so the variable proxies represent the optimal values
		self.problem.evaluate( best_solution )

		res = "Score= %6.4f maxDiff= %6.3f \n"%(accCalc.raw_score(),accCalc.getMaxDiff())
		textArea.append(res)
		textArea.append("=== CCL entrance coordinates ===\n")
#		x0 = best_solution.getTrialPoint().getValue( x0_var )
#		xp0 = best_solution.getTrialPoint().getValue( xp0_var )
#		y0 = best_solution.getTrialPoint().getValue( y0_var )
#		yp0 = best_solution.getTrialPoint().getValue( yp0_var )

		x0 = self.x0_Proxy.getValue()
		xp0 = self.xp0_Proxy.getValue()
		y0 = self.y0_Proxy.getValue()
		yp0 = self.yp0_Proxy.getValue()

		textArea.append("x0 [mm]  = %6.3f \n"%x0)
		textArea.append("xp0[mrad]= %6.3f \n"%xp0)
		textArea.append("y0 [mm]  = %6.3f \n"%y0)
		textArea.append("yp0[mrad]= %6.3f \n"%yp0)
		#plot graphs
		plotx = FunctionGraphsJPanel()
		ploty = FunctionGraphsJPanel()
		orbX = BasicGraphData()
		orbY = BasicGraphData()
		orbX.setDrawLinesOn(true)
		orbX.setGraphColor(Color.RED)
		orbY.setDrawLinesOn(true)
		orbY.setGraphColor(Color.RED)
		for accNode in accCalc.getAccNodesQuads():
			s = accNode.getPosS()
			x = accNode.getPosX()
			y = accNode.getPosY()
			orbX.addPoint(s,x)
			orbY.addPoint(s,y)
		for accNode in accCalc.getAccNodesBPMs():
			s = accNode.getPosS()
			x = accNode.getPosX()
			y = accNode.getPosY()
			orbX.addPoint(s,x)
			orbY.addPoint(s,y)
		plotx.addGraphData(orbX)
		ploty.addGraphData(orbY)
		#add BPMs points
		bpmX = BasicGraphData()
		bpmY = BasicGraphData()
		bpmX.setDrawLinesOn(false)
		bpmY.setDrawLinesOn(false)
		bpmX.setGraphColor(Color.BLACK)
		bpmY.setGraphColor(Color.BLACK)
		for accNode in accCalc.getAccNodesBPMs():
			s = accNode.getPosS()
			x = accNode.getPosExperX()
			y = accNode.getPosExperY()
			bpmX.addPoint(s,x)
			bpmY.addPoint(s,y)
		plotx.addGraphData(bpmX)
		ploty.addGraphData(bpmY)
		plotx.setName("HORIZONTAL (Red-Model Black-BPMs)")
		ploty.setName("VERTICAL   (Red-Model Black-BPMs)")
		plotsBeforePanel.add(plotx)
		plotsBeforePanel.add(ploty)
		
plotActionListener = PlotActionListener()		
plotButton.addActionListener(plotActionListener)	

#---------------------------------------------------
# correction action
#---------------------------------------------------

#New sequence for correction
lst = ArrayList()
dtl6 = accl.getSequence("DTL6")
ccl1 = accl.getSequence("CCL1")
ccl2 = accl.getSequence("CCL2")
ccl3 = accl.getSequence("CCL3")
ccl4 = accl.getSequence("CCL4")

lst.add(dtl6)
lst.add(ccl1)
lst.add(ccl2)
lst.add(ccl3)
lst.add(ccl4)

accNodeForCorr = AcceleratorSeqCombo("SEQ_CORR", lst)

corrH_Qualifier = AndTypeQualifier().and((OrTypeQualifier()).or(HDipoleCorr.s_strType))
corrH_Qualifier.andStatus(true)

corrV_Qualifier = AndTypeQualifier().and((OrTypeQualifier()).or(VDipoleCorr.s_strType))
corrV_Qualifier.andStatus(true)

dcorrsH = accNodeForCorr.getAllNodesWithQualifier(corrH_Qualifier)
dcorrsV = accNodeForCorr.getAllNodesWithQualifier(corrV_Qualifier)


class CorrectActionListener(ActionListener):
	def actionPerformed(self,e):
		if(plotActionListener.getAccCalc() == None):
			textArea.setText("Can not do anything! Calculate the existing orbit first!")
			return
		accCalc = plotActionListener.getAccCalc()
		#accCalc.track1()

		# Flatten the horizontal orbit
		problem = Problem()
		objective = MinimizingObjective( 0.05 )
		problem.addObjective( objective )

		hint = InitialDelta()
		problem.addHint( hint )

		orbCorrH = OrbitCorr( accCalc,accNodeForCorr,dcorrsH,"DCH", problem, hint )

		evaluator = ScoringEvaluator( orbCorrH, problem.getVariables(), objective )
		problem.setEvaluator( evaluator )

		solver = Solver( SolveStopperFactory.maxEvaluationsStopper(1000) )

#		solver = Solver()
#		solver.setScorer(orbCorrH)
#		solver.setVariables(orbCorrH.getProxiesV())
#		stopper = SolveStopperFactory.maxIterationStopper(1000)
#		solver.setStopper(stopper)
#		solver.solve()

		textArea.append("====== orbit correction ===== \n")
		textArea.append( "Orbit H score before = %6.4f \n"%orbCorrH.raw_score() )

		solver.solve( problem )
		scoreboard = solver.getScoreBoard()
		best_solution = scoreboard.getBestSolution()
		#print scoreboard

		# apply the best solution to the model so the variable proxies represent the optimal values
		problem.evaluate( best_solution )

		textArea.append( "Orbit H score after  = %6.4f \n"%orbCorrH.raw_score() )
		#orbCorrH.printCorrectors()
		#print solver.getScoreboard().toString()
		#accCalc.track1()

		# Flatten the vertical orbit
		problem = Problem()
		objective = MinimizingObjective( 0.05 )
		problem.addObjective( objective )

		hint = InitialDelta()
		problem.addHint( hint )

		orbCorrV = OrbitCorr( accCalc,accNodeForCorr,dcorrsV,"DCV", problem, hint )

		evaluator = ScoringEvaluator( orbCorrV, problem.getVariables(), objective )
		problem.setEvaluator( evaluator )

		solver = Solver( SolveStopperFactory.maxEvaluationsStopper(1000) )

#		solver = Solver()
#		solver.setScorer(orbCorrV)
#		solver.setVariables(orbCorrV.getProxiesV())
#		stopper = SolveStopperFactory.maxIterationStopper(1000)
#		solver.setStopper(stopper)

		textArea.append("-------------------\n")
		textArea.append("Orbit V score before = %6.4f \n"%orbCorrV.raw_score())

		solver.solve( problem )
		scoreboard = solver.getScoreBoard()
		best_solution = scoreboard.getBestSolution()
		#print scoreboard

		# apply the best solution to the model so the variable proxies represent the optimal values
		problem.evaluate( best_solution )
		
		textArea.append( "Orbit V score after  = %6.4f \n"%orbCorrV.raw_score() )

		#orbCorrV.printCorrectors()
		#print solver.getScoreboard().toString()
		#============PLOTTING=======================
		plotx = FunctionGraphsJPanel()
		ploty = FunctionGraphsJPanel()
		#----------------------------------
		orbX = BasicGraphData()
		orbY = BasicGraphData()
		orbX.setDrawLinesOn(true)
		orbX.setGraphColor(Color.RED)
		orbY.setDrawLinesOn(true)
		orbY.setGraphColor(Color.RED)
		for accNode in accCalc.getAccNodesQuads():
			quad = accNode.getNode()
			s = accNode.getPosS()
			x = orbCorrH.getTrajDic()[quad]
			y = orbCorrV.getTrajDic()[quad]
			orbX.addPoint(s,x)
			orbY.addPoint(s,y)
		plotx.addGraphData(orbX)
		ploty.addGraphData(orbY)
		plotx.setName("HORIZONTAL (Red-Model Only)")
		ploty.setName("VERTICAL   (Red-Model Only)")	
		plotsAfterPanel.removeAll()
		plotsAfterPanel.add(plotx)
		plotsAfterPanel.add(ploty)
		#----------------------------------
		orbCorrH.applyCorrections()
		orbCorrV.applyCorrections()

correctActionListener = CorrectActionListener()
correctButton.addActionListener(correctActionListener)


while(1 < 2):
	time.sleep(2.0)




print "Done."

#sys.exit(1)
