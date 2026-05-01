#
# Uses TBT BPM data in the ring to calculate position and angle
# at the foil. Makes a small change to the calculation from the
# original version. Simplifies the interface a bit and adds a new
# plot to show all the calculated positions in a scatter plot. 
#
# update 2019-09-26 - I am adding an additional plot to show the real-space trajectory
# of the injected particles - I would like to add this as a tab 
# This will help verify that injection parameters are set correctly for SCBD 

import sys
import math
import types
import time
import pickle

from jarray import *
from xjava.lang import *
from xjava.util import *
from xjava.io import *
from xjava.swing import *
from java.awt import *
from java.awt.event import ActionListener 
from java.awt.event import MouseListener, MouseEvent

from java.awt.event import WindowAdapter

from org.xml.sax import *
from xal.smf import *
from xal.smf.data import *
from xal.model import *
from xal.model.probe import *
from xal.sim.scenario import *
from xal.tools.beam import *
from xal.tools.beam.calc import *
from xal.extension.widgets.plot import *
from xal.smf.data import *
from xal.model.alg import *
from xal.model.probe import *
from xal.model.probe.traj import *
from xal.smf.impl.qualify import *
from xal.smf.impl import *
from xal.extension.solver import *
from xal.extension.solver.hint import InitialDelta
from xal.extension.fit import DampedSinusoidFit #Tune fitting. 

import java.text.NumberFormat
import java.text.DecimalFormat
from xal.extension.widgets.swing import *

from javax.swing.table import AbstractTableModel, TableModel
from java.text import SimpleDateFormat,NumberFormat,DecimalFormat

from lib.scanEvent import scanEvent


false = Boolean("false").booleanValue()
true  = Boolean("true").booleanValue()
null  = Integer.getInteger("")

def fitTBTData(tbtdata, points):
	arr = []
	try:
		fit = DampedSinusoidFit(tbtdata, points)
		fit.solveWithNoiseMaxEvaluations(0.0, 1000)
		fitDict = {
					"Frequency":fit.getFrequency(),
					"Phase":fit.getCosineLikePhase(),
					"Slope":-fit.getGrowthRate(),
					"Amp":fit.getAmplitude(),
					"Offset":fit.getOffset(),
					"Variance":fit.getSignalVariance() 
					}
	except:
		fitDict = {
					"Frequency":0.0,
					"Phase":0.0,
					"Slope":0.0,
					"Amp":0.0,
					"Offset":0.0,
					"Variance":0.0 
					}
	
	return fitDict

# Preferred method for calculating the initial coordinates
# These two methods should reduce to the same value: 
# They don't becuase the above method gets the tune from the fit, 
# which doesn't match the tune of the model. The model tune is
# used to extract the matched twiss functions. 
def calculateInitialCoordinates( tbtFit, twissAt, plane ):
	if( plane == 'x'):
		alpha = twissAt[0].getAlpha();
		beta  = twissAt[0].getBeta();
	elif( plane == 'y'):
		alpha = twissAt[1].getAlpha();
		beta  = twissAt[1].getBeta();

	a  = tbtFit["Amp"]
	ph = tbtFit["Phase"]
	nu = tbtFit["Frequency"]

	x0  = a * Math.cos( ph );					      # x is known.
	xp0 = -a*(Math.sin(ph) + alpha*Math.cos(ph))/beta # x' from model/fit.	

	return [x0, xp0]

def calculateInitialCoordinates2( tbtFit, fullturnAtElement, plane ):
	if( plane == 'x'):
		m00 = fullturnAtElement.getElem( 0, 0 );
		m01 = fullturnAtElement.getElem( 0, 1 );
	elif( plane == 'y'):
		m00 = fullturnAtElement.getElem( 2, 2 );
		m01 = fullturnAtElement.getElem( 2, 3 );

	a  = tbtFit["Amp"]
	ph = tbtFit["Phase"]
	nu = tbtFit["Frequency"]
	
	x0  = a * Math.cos( ph );					 # compute turn 0 just the betatron oscillation
	x1  = a * Math.cos( 2 * Math.PI * nu + ph ); # just the betatron oscillation
	xp0 = ( x1 - m00 * x0 ) / m01;			     # just the betatron oscillation
	# m00 = cos(2PI nu) + alpha*sin(2PI nu)
	# m01 = beta*sin(2PI nu)
	# If you expand this it simplifies to the expression in initial coordinates 2. 
	# If they don't agree model and fit tunes are probably different.

	return [x0, xp0]


#----------------------------------------------------------------
#Tracking should be done by openxal code, not me!
def track( matrix, coord4D ):
	tracked = [0,0,0,0]
	for i in range(4):
		for j in range(4):
			tracked[i] += coord4D[j]*matrix.getElem(i,j)

	return tracked

def getAngles(x0,x1,y0,y1,fullTurn):
	m=fullTurn
	angleX = (x1-m.getAngle(1,1)*x0)/m.getElem(2,1)
	angleY = (y1-m.getAngle(3,3)*y0)/m.getElem(3,4)
	return [angleX, angleY]
#----------------------------------------------------------------

class bpmTableElement():
	def __init__(self, bpmNode, isOn ):
		self.node = bpmNode
		self.active = isOn

	def getId(self):
		return self.node.getId()

	def getNode(self):
		return self.node

	def isActive(self):
		return self.active

	def setActive(self, active):
		self.active = active

class bpmTableModel(AbstractTableModel):
	def __init__(self):
		self.bpm_table_element_arr = []
		self.columnNames = ["Name", "Use"]
		self.nf = NumberFormat.getInstance()
		self.nf.setMaximumFractionDigits(2)
		self.boolean_class = Boolean(true).getClass()
		self.string_class = String().getClass()
		
	def getColumnCount(self):
		return len(self.columnNames)
		
	def getRowCount(self):
		return len(self.bpm_table_element_arr)
		
	def getColumnName(self,col):
		return self.columnNames[col]
		
	def getValueAt(self,row,col):
		if(col == 0): return self.bpm_table_element_arr[row].getId()
		if(col == 1): return Boolean(self.bpm_table_element_arr[row].isActive())
			
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
			self.bpm_table_element_arr[row].setActive(value)
			self.fireTableCellUpdated(row, col)
			
	def addBPM_TableElement(self,bpm_table_element):
		self.bpm_table_element_arr.append(bpm_table_element)
		
	def getBPM_TableElements(self):
		return self.bpm_table_element_arr
			
	def getBPM_TableElement(self,row):
		return self.bpm_table_element_arr[row]

	def readPVData(self):
		# for bpm_table_element in self.bpm_table_element_arr:
		# 	bpm_table_element.readPVData()
		return

def createTab1(panel):
	# panel = JPanel(GridLayout(2,1))
	border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"TBT Data Plot")
	panel.setBorder(border)
	return 

def createTab2(panel):
	fingerprintPanelH   = JPanel(BorderLayout())  
	fingerprintPanelV   = JPanel(BorderLayout())  
	panel.add(fingerprintPanelH)
	panel.add(fingerprintPanelV)

	border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Phase Fingerprints")
	panel.setBorder(border)
	return 

#window closer will kill this apps
class WindowCloser(WindowAdapter):
	def windowClosing(self,windowEvent):
		sys.exit(1)
#===============================================================
#              MAIN PROGRAM
#===============================================================

#+++++++++++++++++++++++++++++++++++++++++++++++
# # locate the enclosing folder and get the offset data file
# script_folder = File( sys.argv[0] ).getParentFile()
# offSetsFileName = File( script_folder, "ccl_bpm_quad_offsets_final.dat" ).getPath()
# #accCalc = AccCalculator(accSeq , nodes, offSetsFileName)

#----------------------------------------------------------------------------------------
# Global instantiation of accelerator		

bad_bpms = ["Ring_Diag:BPM_B01","Ring_Diag:BPM_D10"]; #Removed BPM_B02 added B01 11/01/2019 nje

acc      = XMLDataManager.loadDefaultAccelerator()
event    = scanEvent(acc)
event.setBadBPMS(bad_bpms)


#----------------------------------------------------------------------------------------
#make GUI
#
#----------------------------------------------------------------------------------------
# There should be a panel with all the BPM's listed for the ring. 
# Select which BPM's to use, default is all. 
# Press button to acquire TBT data for all, and fit data. 
# Should also calculate and plot position at foil. 
# Store TBT, and fit data for each BPM 
# Some mechanism to select a particular BPM and show the
# TBT data for that BPM, the fit, and a model prediction of the
# TBT data given the initial PS values extracted from the fit. 
#----------------------------------------------------------------------------------------
frame = JFrame("Ring Injection 2")
frame.getContentPane().setLayout(BorderLayout())

leftPanel   = JPanel()
leftPanel.setLayout(BoxLayout(leftPanel, BoxLayout.PAGE_AXIS))
plotsPanel = JPanel(GridLayout(1,0))

rightPanel = JPanel()
rightPanel.setLayout(BoxLayout(rightPanel,BoxLayout.PAGE_AXIS))
rightPanel.setPreferredSize(Dimension(200,200))

frame.getContentPane().add(leftPanel ,BorderLayout.WEST)
frame.getContentPane().add(plotsPanel,BorderLayout.CENTER)
frame.getContentPane().add(rightPanel,BorderLayout.EAST)


#Build left panel ----------------------------------------------------------------------

buttonPanel = JPanel(GridLayout(2,2))
inputPanel  = JPanel(GridLayout(1,3))
textPanel   = JPanel(BorderLayout())

leftPanel.add(buttonPanel)
leftPanel.add(inputPanel )
leftPanel.add(textPanel  )

#Build button panel and input panel
plotButton = JButton("Fetch Data and Plot ") 
buttonPanel.add(plotButton)
fingerprintButton = JButton("Set Ref Fingerprint") 
buttonPanel.add(fingerprintButton)

loadRefButton = JButton("Load Ref Fingerprint") 
buttonPanel.add(loadRefButton)

saveRefButton = JButton("Save Ref Fingerprint") 
buttonPanel.add(saveRefButton)

energyFor = NumberFormat.getNumberInstance();
energyFor.setMaximumFractionDigits(2);
energyField = DecimalField(1300.0,6, energyFor)
inputPanel.add(JLabel("Energy"))
inputPanel.add( energyField )
inputPanel.add(JLabel(" MeV"))

#Build text panel
textArea = JTextArea()
textArea.setText(null)
textPanel.add(textArea,BorderLayout.CENTER)

#Build tab panels ----------------------------------------------------------------------

mainPane = JTabbedPane(JTabbedPane.TOP);
p1 = JPanel(GridLayout(2,1))
p2 = JPanel()
p3 = JPanel()

tab_ps_panel  = JPanel(GridLayout(1,0))
p1.add(tab_ps_panel)

tab_tbt_panel = JPanel(GridLayout(2,1))
p1.add(tab_tbt_panel)

mainPane.addTab("Phase Space", p1)
mainPane.addTab("Real Space" , p2)
mainPane.addTab("Fingerprint", p3)

plotsPanel.add(mainPane)
# middleTop = JPanel(GridLayout(1,3))
# middleBot = JPanel(GridLayout(2,1)) #Bottom Panel is 
# bpmTBTPanel = JPanel(GridLayout(2,1))         #Subpanel of plotsPanel
# foilPhaseSpacePanel = JPanel(BorderLayout())  #Subpanel of plotsPanel
# fingerprintPanelH   = JPanel(BorderLayout())  #Subpanel of plotsPanel
# fingerprintPanelV   = JPanel(BorderLayout())  #Subpanel of plotsPanel
# bpmRealSpacePanel   = JPanel(BorderLayout())  #Subpanel of plotsPanel

# middleTop.add(bpmTBTPanel)
# middleTop.add(foilPhaseSpacePanel)
# middleTop.add(bpmRealSpacePanel) # Real space trajectory panel

# middleBot.add(fingerprintPanelH)
# middleBot.add(fingerprintPanelV)

# plotsPanel.add(middleTop)
# plotsPanel.add(middleBot)
# # plotsPanel.add(bpmTBTPanel)
# # plotsPanel.add(foilPhaseSpacePanel)
# # plotsPanel.add(fingerprintPanelH)
# # plotsPanel.add(fingerprintPanelV)

# border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"TBT Data Plot")
# bpmTBTPanel.setBorder(border)

# border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Phase Space at Foil")
# foilPhaseSpacePanel.setBorder(border)

# border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Real Space Trajectory")
# bpmRealSpacePanel.setBorder(border)

# border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),"Phase Fingerprints")
# middleBot.setBorder(border)


#Build the right panel ----------------------------------------------------------------------

sequence = acc.findSequence( "Ring" ).getNodesOfType(BPM.s_strType)
bpmTable = JTable(bpmTableModel())

for bpm in sequence:
	if( bpm.getId() not in bad_bpms ):
		bpmTable.getModel().addBPM_TableElement(bpmTableElement(bpm, True))
	else:
		bpmTable.getModel().addBPM_TableElement(bpmTableElement(bpm, False))

bpmTable.setPreferredSize(Dimension(200,200))
bpmTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF)
bpmTable.getColumnModel().getColumn(0).setPreferredWidth(150)
bpmTable.getColumnModel().getColumn(1).setPreferredWidth(50)
bpmTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
bpmTable.setFillsViewportHeight(true)

# tbtButton = JButton("Selected TBT")

rightPanel.add(JScrollPane(bpmTable))
# rightPanel.add(tbtButton)

# Finish initializing the window -------------------------------------------------------------

frame.addWindowListener(WindowCloser())

frame.setSize(Dimension(900,600))
frame.show()

#
# GUI Finished
#----------------------------------------------------------------------------------------

class tbt_controller():
	# This is kind of gross because scan event independently gets BPMs. 
	# probably need to refactor so the scan event handles everything.
	def __init__(self,acc):
		self.acc     = acc
		# self.bpms    = acc.findSequence("Ring").getNodesOfType(BPM.s_strType)
		self.bpms = []
		self.tbtBPM  = [] # This is just the BPM that will be plotted in the TBT window.
		# This should ask which BPM isn't selected as bad. For now [0] is ok.   

		# self.event = scanEvent(acc)
		self.event = event
		self.event.javaFile = File("untitled.xml")
		self.ring = acc.getComboSequence("Ring")

		self.kineticEnergy = 1.011E9
		self.zeroSkews = True # Doesn't work yet.
		self.turnsToFit = 50

		self.ringEngine = []

		#Calculated values - should be cleared for each calcualtion.
		self.init_positions  = []
		self.init_positions2 = []
		self.foil_ps         = []
		self.foil_ps2        = []
		self.sim_tbt_ps      = []
		self.sim_tbt_ps2     = []
		self.tbtX = []
		self.tbtY = []
		self.xFit = []
		self.yFit = []
		self.refFitX = []
		self.refFitY = []
		self.ps_avg = [] #list of average values [x,x',y,y'] at the foil
		self.bpm_index = {}

		self.calcTunes = []

		self.bpm_phase_history = [] # Should store phase of each bpm from fits over time. 

		self.prepareTracking()

		return

	def setTBTBPM(self,bpm):
		self.tbtBPM = bpm
		for b in self.bpms:
			if b.getId() == bpm:
				self.tbtBPM = b
		return

	def prepareTracking(self):
		self.kineticEnergy = energyField.getDoubleValue()*1.0E6
		seq = self.ring
		Tracker = AlgorithmFactory.createTransferMapTracker(seq)
		Probe   = ProbeFactory.getTransferMapProbe(seq, Tracker)
		
		Probe.initialize()
		scenario = Scenario.newScenarioFor(seq)
		scenario.setProbe(Probe)
		
		Probe.setKineticEnergy(self.kineticEnergy) # Should get the kinetic energy from the ring clock... maybe?
		print "Kinetic energy = ",Probe.getKineticEnergy()
		
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_LIVE)
		scenario.resync()
		scenario.run()
		self.traj = Probe.getTrajectory()
		self.ringEngine = CalculationsOnRings(self.traj)
		print "Full Model Tunes = ",self.ringEngine.computeFullTunes()
		self.calcTunes = self.ringEngine.computeFractionalTunes()
		print "Frac Model Tunes = ",self.calcTunes

		nodes = seq.getNodes()
		# for node in nodes:
		# 	if( node.getType() == "BPM" ):
		# 		print " name = %-20s "%node.getId()," pos= %8.4f "%seq.getPosition(node)

		return 

	def setKineticEnergy(self, kineticEnergy):
		self.kineticEnergy = kineticEnergy
		prepareTracking()
		return

	def setTurnsToFit(self, ttf):
		self.turnsToFit = ttf
		return

	def fetch_event(self):
		""" Fetching the event does not trigger it. It only gets the last batch of data. """
		self.prepareTracking()
		# should update bad_bpms based on table check boxes. 
		# Look through list and set bad_bpms on event.
		self.event.setBadBPMS(bad_bpms)
		self.event.submitBatchRequest() # add some testing here.
		self.event.populateXMLEvent()
		# Update the list of good BPMS
		self.bpms   = [i[0] for i in self.event.bpm_ch]
		if self.tbtBPM == []:
			self.setTBTBPM(self.bpms[0].getId())
		return 

	def perform_calculations(self):

		# bpm = self.tbtBPM
		self.init_positions = []
		self.init_positions2 = []
		self.tbtX = []
		self.tbtY = []
		self.xFit = []
		self.yFit = []
		self.sim_tbt_ps = []
		self.sim_tbt_ps2 = []
		self.foil_ps = []
		self.foil_ps2 = []
		self.ps_avg = [] #list of average values [x,x',y,y'] at the foil
		#fit for every bpm in the list
		#initial position for every good, active BPM in the list
		#only need to track one BPM... could put this somewhere else.
		injstate = self.traj.stateForElement("Ring_Inj:Foil")
		# for bpm in self.bpms:
		goodBPMS = []
		# This is a little junky. 
		# Should keep one list of BPM's, maybe exclude, and bad BPM
		for bpm in bpmTable.getModel().getBPM_TableElements():
			bpmIsGood = True
			if( bpm.isActive() ):
				if( bpm.getId() in bad_bpms ):
						bpmIsGood = False
						bpm.setActive(False)
				if( bpmIsGood ):
					goodBPMS.append(bpm.getNode())

		bpmind = 0
		for bpm in goodBPMS:
			bpmState    = self.traj.statesForElement(bpm.getId()).get(0) # This line was missing in 9/24/2018 Data
			bpmFullTurn = self.ringEngine.computeRingFullTurnMatrixAt(bpmState)
			localTwiss  = self.ringEngine.computeMatchedTwissAt(bpmState)
			bpmToFoil   = self.ringEngine.computeRingTransferMatrix( injstate, bpmState ).inverse()
			
			turnsToFit = self.turnsToFit; # this should come from the GUI
			
			tbtX = self.event.getTBT(bpm.getId(), 'x')
			tbtY = self.event.getTBT(bpm.getId(), 'y')
			turnsToFit = min(turnsToFit, len(tbtX))

			xFit = fitTBTData(tbtX, turnsToFit)
			yFit = fitTBTData(tbtY, turnsToFit)


			x0 = calculateInitialCoordinates(xFit, localTwiss, 'x')
			y0 = calculateInitialCoordinates(yFit, localTwiss, 'y')

			x02 = calculateInitialCoordinates2(xFit, bpmFullTurn, 'x')
			y02 = calculateInitialCoordinates2(yFit, bpmFullTurn, 'y')


			init   = x0  + y0 
			init2  = x02 + y02
			
			# Stored results - should probably have a seperate
			# object for each BPM. 
			self.tbtX.append(tbtX)
			self.tbtY.append(tbtY)
			self.xFit.append(xFit)
			self.yFit.append(yFit)

			self.init_positions.append(init)
			self.init_positions2.append(init2)

			self.foil_ps.append(track(bpmToFoil ,init))
			self.foil_ps2.append(track(bpmToFoil,init2))
			
			# Single turn bpm plot - should be able to do this
			# by double clicking a particular BPM or something 
			self.bpm_index[ bpm.getId() ] = bpmind
			bpmind += 1

		if(len(self.refFitX) == 0):
			self.refFitX = self.xFit
		if(len(self.refFitY) == 0):
			self.refFitY = self.yFit


		self.singleBPMtbt()

		xavg = 0
		xpavg = 0
		yavg = 0
		ypavg = 0
		samples = (float)(len(self.foil_ps))
		for [x,xp,y,yp] in self.foil_ps:
			# print x 
			xavg  += x
			xpavg += xp
			yavg  += y
			ypavg += yp

		xavg  /= samples
		xpavg /= samples
		yavg  /= samples
		ypavg /= samples
		
		self.ps_avg = [xavg,xpavg,yavg,ypavg]

		return 

	def setRef(self):
		""" 
		Sets reference fits - will be stored until reference is stored again
		this could get troublesome if you accidentally overwrite it - might be nice
		to write to file and read in. 
		"""
		self.refFitX = self.xFit
		self.refFitY = self.yFit

	def loadRef(self):
		""" 
		Sets reference fits - will be stored until reference is stored again
		this could get troublesome if you accidentally overwrite it - might be nice
		to write to file and read in. 
		"""

		pfile = open('/Users/nhe/TEMP/pickled.scbdfit','rb')
		fits = pickle.load(pfile)
		pfile.close()

		self.refFitX = fits[0]
		self.refFitY = fits[1]

	def saveRef(self):
		""" 
		Sets reference fits - will be stored until reference is stored again
		this could get troublesome if you accidentally overwrite it - might be nice
		to write to file and read in. 
		"""

		pfile = open('/Users/nhe/TEMP/pickled.scbdfit','wb')
		pickle.dump([self.refFitX,self.refFitY],pfile)
		pfile.close()


	def singleBPMtbt(self):
		# tracked is a list of x,x',y,y' tuples. 
		bpmid    = self.tbtBPM.getId()
		whichBPM = self.bpm_index[ bpmid ] 
		bpmState    = self.traj.statesForElement(bpmid).get(0) # This line was missing in 9/24/2018 Data
		bpmFullTurn = self.ringEngine.computeRingFullTurnMatrixAt(bpmState)
		xoff = (self.xFit[whichBPM])["Offset"]
		yoff = (self.yFit[whichBPM])["Offset"]

		tracked  = [self.init_positions[whichBPM]]
		tracked2 = [self.init_positions2[whichBPM]]

		for j in range(self.turnsToFit):
			tracked.append(track(bpmFullTurn,tracked[-1]))
			tracked2.append(track(bpmFullTurn,tracked2[-1]))

		#Add the offset to tracked data so plots line up. 
		#Might want to do this when plotting instead...
		self.sim_tbt_ps = []
		for j in tracked:
			offset = [j[0]+xoff, j[1], j[2]+yoff, j[3]]
			self.sim_tbt_ps.append(offset)

		self.sim_tbt_ps2 = []
		for j in tracked2:
			offset = [j[0]+xoff, j[1], j[2]+yoff, j[3]]
			self.sim_tbt_ps2.append(offset)

		# self.sim_tbt_ps  = tracked
		# self.sim_tbt_ps2 = tracked2

class tbt_plot_controller():
	def __init__(self,calculator):
		self.calc = calculator
		self.ps_plot = FunctionGraphsJPanel()
		self.fp_plot = FunctionGraphsJPanel()
		self.plotx   = FunctionGraphsJPanel()
		self.ploty   = FunctionGraphsJPanel()
		self.rs_plot  = FunctionGraphsJPanel()
		self.fp_panel = []
		self.ps_panel = []
		self.rs_panel = []
		self.tbt_panel = []
		# self.clean_up()	

		return

	def clean_up(self):
		self.clean_up_tbt()
		self.clean_up_ps()
		self.clean_up_real_space()
		self.clean_up_fp()

		return

	def addPSPanel(self,panel):
		self.ps_panel = panel
		return

	def addRSPanel(self,panel):
		self.rs_panel = panel
		return

	def addTBTPanel(self,panel):
		self.tbt_panel = panel
		return

	def addFPPanel(self,panel):
		self.fp_panel = panel
		return

	def clean_up_ps(self):
		self.ps_plot = FunctionGraphsJPanel()

		self.ps_panel.removeAll()
		self.ps_panel.setLayout(GridLayout(1,0))

		self.ps_plot.removeAll()
		self.ps_plot.setName("Position at Foil x=Red, y=Black")
		self.ps_plot.setAxisNameX("x, y [mm]")
		self.ps_plot.setAxisNameY("x', y' [mrad]")

		self.ps_plot.setLimitsAndTicksX(-20.0,120.0,20.0,1)
		self.ps_plot.setLimitsAndTicksY(-10.0,10.0,1.0,1)

		self.ps_panel.add(self.ps_plot)

	def clean_up_tbt(self):
		self.tbt_panel.removeAll()
		self.tbt_panel.setLayout(GridLayout(2,1))

		self.plotx = FunctionGraphsJPanel()
		self.ploty = FunctionGraphsJPanel()

		self.plotx.setName("HORIZONTAL (Red-Data Blk-Model Grn-Fit) "+(self.calc.tbtBPM.getId()))
		self.ploty.setName("VERTICAL   (Red-Data Blk-Model Grn-Fit) "+(self.calc.tbtBPM.getId()))

		self.plotx.setAxisNameX("Turn")
		self.plotx.setAxisNameY("x [mm]")

		self.ploty.setAxisNameX("Turn")
		self.ploty.setAxisNameY("y [mm]")

		self.plotx.setLimitsAndTicksY(-100.0,100.0,20.0,1)
		self.ploty.setLimitsAndTicksY(-100.0,100.0,20.0,1)

		self.tbt_panel.add(self.plotx)
		self.tbt_panel.add(self.ploty)

	def clean_up_real_space(self):
		self.rs_plot = FunctionGraphsJPanel()

		self.rs_panel.removeAll()
		self.rs_panel.setLayout(GridLayout(1,0))

		self.rs_plot.removeAll()
		self.rs_plot.setName("X-Y at BPM "+(self.calc.tbtBPM.getId()))
		self.rs_plot.setAxisNameX("x [mm]")
		self.rs_plot.setAxisNameY("y [mm]")

		self.rs_plot.setLimitsAndTicksX(-120.0,120.0,20.0,1)
		self.rs_plot.setLimitsAndTicksY(-120.0,120.0,20.0,1)

		self.rs_panel.add(self.rs_plot)

	def clean_up_fp(self):
		self.fp_panel.removeAll()
		self.fp_panel.setLayout(GridLayout(2,1))

		self.fp_plotH = FunctionGraphsJPanel() 
		self.fp_plotH.setName("H - Fingerprint (Red = Ref, Black = Live)")
		self.fp_plotH.setAxisNameX("BPM")
		self.fp_plotH.setAxisNameY("Phase")

		self.fp_plotV = FunctionGraphsJPanel() 
		self.fp_plotV.setName("V - Fingerprint (Red = Ref, Black = Live)")
		self.fp_plotV.setAxisNameX("BPM")
		self.fp_plotV.setAxisNameY("Phase")

		# Set limits on plot - should be fixed.
		self.fp_plotH.setLimitsAndTicksX(0.0,250.0,25.0,5)
		self.fp_plotH.setLimitsAndTicksY(-3.5,3.5,3.5,1)

		self.fp_plotV.setLimitsAndTicksX(0.0,250.0,25.0,5)
		self.fp_plotV.setLimitsAndTicksY(-3.5,3.5,3.5,1)


		self.fp_panel.add(self.fp_plotH)
		self.fp_panel.add(self.fp_plotV)


	def plot_foil_coordinates(self):
		#
		# Should be able to change plot from x-x' and y-y' to x-y and x'-y' 
		#

		psx   = [i[0] for i in self.calc.foil_ps ]
		psxp  = [i[1] for i in self.calc.foil_ps ]
		psy   = [i[2] for i in self.calc.foil_ps ]
		psyp  = [i[3] for i in self.calc.foil_ps ]

		self.scatter_plot(self.ps_plot, psx, psxp, Color.RED)
		self.scatter_plot(self.ps_plot, psy, psyp, Color.BLACK)

		xmax  = max(psx)
		xpmax = max(psxp)
		ymax  = max(psy)
		ypmax = max(psyp)

		xmin  = min(psx)
		xpmin = min(psxp)
		ymin  = min(psy)
		ypmin = min(psyp)

		d = BasicGraphData()
		d.setDrawLinesOn(True)
		d.setDrawPointsOn(False)
		d.setGraphPointFillingShape(False)
		d.setGraphColor(Color.RED)
		d.addPoint(self.calc.ps_avg[0],xpmin)
		d.addPoint(self.calc.ps_avg[0],xpmax)
		self.ps_plot.addGraphData(d)

		d = BasicGraphData()
		d.setDrawLinesOn(True)
		d.setDrawPointsOn(False)
		d.setGraphPointFillingShape(False)
		d.setGraphColor(Color.RED)
		d.addPoint(xmin,self.calc.ps_avg[1])
		d.addPoint(xmax,self.calc.ps_avg[1])
		self.ps_plot.addGraphData(d)

		d = BasicGraphData()
		d.setDrawLinesOn(True)
		d.setDrawPointsOn(False)
		d.setGraphPointFillingShape(False)
		d.setGraphColor(Color.BLACK)
		d.addPoint(self.calc.ps_avg[2],ypmin)
		d.addPoint(self.calc.ps_avg[2],ypmax)
		self.ps_plot.addGraphData(d)

		d = BasicGraphData()
		d.setDrawLinesOn(True)
		d.setDrawPointsOn(False)
		d.setGraphPointFillingShape(False)
		d.setGraphColor(Color.BLACK)
		d.addPoint(ymin,self.calc.ps_avg[3])
		d.addPoint(ymax,self.calc.ps_avg[3])
		self.ps_plot.addGraphData(d)

		textArea.append("=== Average Foil Coordinates ===\n")
		textArea.append("x = %6.3f \n"%self.calc.ps_avg[0])
		textArea.append("x'= %6.3f \n"%self.calc.ps_avg[1])
		textArea.append("y = %6.3f \n"%self.calc.ps_avg[2])
		textArea.append("y'= %6.3f \n"%self.calc.ps_avg[3])

		textArea.append("\nx max= %6.3f \n"%xmax)
		textArea.append("x'max= %6.3f \n"%xpmax)
		textArea.append("y max= %6.3f \n"%ymax)
		textArea.append("y'max= %6.3f \n"%ypmax)

		textArea.append("\nx min= %6.3f \n"%xmin)
		textArea.append("x'min= %6.3f \n"%xpmin)
		textArea.append("y min= %6.3f \n"%ymin)
		textArea.append("y'min= %6.3f \n"%ypmin)
		
		bpmid = self.calc.bpm_index[self.calc.tbtBPM.getId()]
		textArea.append("\n")
		textArea.append("=== Tunes ===\n")
		textArea.append("x tune meas = %6.3f\n"%self.calc.xFit[bpmid]["Frequency"])
		textArea.append("y tune meas = %6.3f\n"%self.calc.yFit[bpmid]["Frequency"])
		textArea.append("\n")
		textArea.append("x tune calc = %6.3f\n"%self.calc.calcTunes[0])
		textArea.append("y tune calc = %6.3f\n"%self.calc.calcTunes[1])

		return 

	def plot_tbt_data(self):
		turnsToFit = self.calc.turnsToFit # this should come from the GUI
		bpmid = self.calc.bpm_index[ self.calc.tbtBPM.getId() ]
		tbtX = self.calc.tbtX[bpmid]
		tbtY = self.calc.tbtY[bpmid]

		turnsToFit = min(turnsToFit, len(tbtX))

		self.joined_plot(self.plotx,range(turnsToFit),tbtX[:turnsToFit],Color.RED)
		self.joined_plot(self.ploty,range(turnsToFit),tbtY[:turnsToFit],Color.RED)

		return

	def plot_real_space_data(self):
		turnsToFit = self.calc.turnsToFit # this should come from the GUI
		bpmid = self.calc.bpm_index[ self.calc.tbtBPM.getId() ]
		tbtX = self.calc.tbtX[bpmid]
		tbtY = self.calc.tbtY[bpmid]

		turnsToFit = min(turnsToFit, len(tbtX))

		self.joined_plot(self.rs_plot,tbtX[:turnsToFit],tbtY[:turnsToFit],Color.RED)

	def plot_fit_data(self):
		bpmid = self.calc.bpm_index[ self.calc.tbtBPM.getId() ]
		turnsToFit = self.calc.turnsToFit

		xFit = self.calc.xFit[bpmid]
		yFit = self.calc.yFit[bpmid]

		self.plot_damped_sin(self.plotx,xFit,0,turnsToFit,10,Color.GREEN)
		self.plot_damped_sin(self.ploty,yFit,0,turnsToFit,10,Color.GREEN)

		return

	def plot_simulated(self):
		bpmid = self.calc.bpm_index[ self.calc.tbtBPM.getId() ]
		xinit = self.calc.init_positions[bpmid][0]
		yinit = self.calc.init_positions[bpmid][1]

		xinit2 = self.calc.init_positions2[bpmid][0]
		yinit2 = self.calc.init_positions2[bpmid][1]

		self.joined_plot(self.plotx, range(len(self.calc.sim_tbt_ps)),[i[0] for i in self.calc.sim_tbt_ps], Color.BLACK)
		self.joined_plot(self.ploty, range(len(self.calc.sim_tbt_ps)),[i[2] for i in self.calc.sim_tbt_ps], Color.BLACK)

		# self.joined_plot(self.plotx, range(len(self.calc.sim_tbt_ps2)),[i[0] for i in self.calc.sim_tbt_ps2], Color.BLUE)
		# self.joined_plot(self.ploty, range(len(self.calc.sim_tbt_ps2)),[i[2] for i in self.calc.sim_tbt_ps2], Color.BLUE)	

	def plot_fingerprint(self):
		# Basically the same plot as the BPM TBT plots but for the phases around the ring

		# Live
		xPhase = []
		yPhase = []

		ind = range(len(self.calc.bpm_index))
		pos = []
		for bpmid in self.calc.bpm_index:
			ith = self.calc.bpm_index[bpmid]
			node = self.calc.ring.getNodeWithId(bpmid)
			xPhase.append((self.calc.xFit[ith])["Phase"])
			yPhase.append((self.calc.yFit[ith])["Phase"])
			pos.append(self.calc.ring.getPosition(node))

		self.joined_plot(self.fp_plotH, pos, xPhase, Color.BLACK) # Live
		self.joined_plot(self.fp_plotV, pos, yPhase, Color.BLACK) # Live

		# Ref 
		xPhase = []
		yPhase = []

		ind = range(len(self.calc.bpm_index))
		pos = []
		for bpmid in self.calc.bpm_index:
			ith = self.calc.bpm_index[bpmid]
			node = self.calc.ring.getNodeWithId(bpmid)
			xPhase.append((self.calc.refFitX[ith])["Phase"])
			yPhase.append((self.calc.refFitY[ith])["Phase"])
			pos.append(self.calc.ring.getPosition(node))

		self.joined_plot(self.fp_plotH, pos, xPhase, Color.RED) # Live
		self.joined_plot(self.fp_plotV, pos, yPhase, Color.RED) # Live

		rmsX = 0.0;
		rmsY = 0.0;
		for i in range(len(xPhase)):
			xd = (self.calc.xFit[ith])["Phase"] - (self.calc.refFitX[ith])["Phase"]
			yd = (self.calc.yFit[ith])["Phase"] - (self.calc.refFitY[ith])["Phase"]

			rmsX += math.sqrt(xd*xd)
			rmsY += math.sqrt(yd*yd)

		rmsX = rmsX/(float(len(xPhase)))
		rmsY = rmsY/(float(len(xPhase)))

		textArea.append("\n\n========== Fingerprint Info =============\n")
		textArea.append("x Phase RMS = %6.3f \n"%rmsX)
		textArea.append("y Phase RMS = %6.3f \n"%rmsY)
		textArea.append("\n\n")

		return

	def plotRealSpace(self):
		bpmid = self.calc.bpm_index[ self.calc.tbtBPM.getId() ]
		tbtX = self.calc.tbtX[bpmid]
		tbtY = self.calc.tbtY[bpmid]

		turnsToPlot = min(self.calc.turnsToFit, len(tbtX))

		self.joined_plot(self.plotx,range(turnsToPlot),tbtX[:turnsToPlot],Color.RED)
		self.joined_plot(self.ploty,range(turnsToPlot),tbtY[:turnsToPlot],Color.RED)

		return


	def scatter_plot(self, plot, x, y, color):
		d = BasicGraphData()
		d.setDrawLinesOn(false)
		d.setGraphColor(color)
		d.addPoint(x,y)
		plot.addGraphData(d)
		return

	def joined_plot(self, plot, x, y, color):
		d = BasicGraphData()
		d.setDrawLinesOn(true)
		d.setGraphColor(color)
		d.addPoint(x,y)
		plot.addGraphData(d)
		return

	def plot_damped_sin(self,plot,fit,t0,tf,stepsPerTurn,color):
		d = BasicGraphData()
		d.setDrawLinesOn(true)
		d.setDrawPointsOn(false)
		d.setGraphColor(color)
		f   = fit["Frequency"]
		amp = fit["Amp"]
		tau = fit["Slope"]
		ph  = fit["Phase"]
		c   = fit["Offset"]
		numSteps = (tf-t0)*stepsPerTurn
		for i in range(numSteps):
			x = t0+i*tf/((float)(numSteps-1.0))
			arg=(2*Math.PI*f*x)
			fx=amp*(Math.exp(-x*tau))
			fx=fx*Math.cos(arg+ph)
			fx=fx+c
			d.addPoint(x,fx)
		
		plot.addGraphData(d)

		return

	def report_initial_coordinates(self):
		textArea.append("=== Initial Coordinates ===\n")
		textArea.append("x0 [mm]  = %6.3f \n"%self.calc.init_positions[0][0])
		textArea.append("xp0[mrad]= %6.3f \n"%self.calc.init_positions[0][1])
		textArea.append("y0 [mm]  = %6.3f \n"%self.calc.init_positions[0][2])		
		textArea.append("yp0[mrad]= %6.3f \n"%self.calc.init_positions[0][3])

		textArea.append("x0-2 [mm]  = %6.3f \n"%self.calc.init_positions2[0][0])
		textArea.append("xp0-2[mrad]= %6.3f \n"%self.calc.init_positions2[0][1])
		textArea.append("y0-2 [mm]  = %6.3f \n"%self.calc.init_positions2[0][2])	
		textArea.append("yp0-2[mrad]= %6.3f \n"%self.calc.init_positions2[0][3])

		return

calc     = tbt_controller(acc)
tbt_plot = tbt_plot_controller(calc)
tbt_plot.addFPPanel(p3)
tbt_plot.addRSPanel(p2)
tbt_plot.addPSPanel(tab_ps_panel)
tbt_plot.addTBTPanel(tab_tbt_panel)

#-----------------------------------------
# make button action
#-----------------------------------------

class mouseListen(MouseListener):
	def __init__(self):
		return
    
	def mouseClicked(self,e):
		if( e.getClickCount() >= 2 ):
			row = bpmTable.getSelectedRow()

			bpm = (bpmTable.getModel().getBPM_TableElement(row)).getNode()
			calc.setTBTBPM(bpm)
			calc.singleBPMtbt()
			tbt_plot.clean_up_tbt()
			tbt_plot.clean_up_real_space()

			tbt_plot.plot_tbt_data()    #	TBT Data
			tbt_plot.plot_real_space_data()    #	TBT Data
			tbt_plot.plot_fit_data()    #	Fit function
			tbt_plot.plot_simulated()   #	Simulate Data

			frame.revalidate()

		return

	def mousePressed(self,e):
		return

	def mouseReleased(self,e):
		return

	def mouseEntered(self,e):
		return

	def mouseExited(self,e):
		return

listen = mouseListen()
bpmTable.addMouseListener(listen)

class PlotActionListener(ActionListener):
	def __init__(self):
		return 
	
	def actionPerformed(self,e):

	# Calculations
		calc.fetch_event() # Fetch
		calc.perform_calculations() # calculate fits, etc. and store results. 

		textArea.setText(null)
		
	# Plots
 	
		tbt_plot.clean_up()	            # This cleans up everything.
		#BPM TBT
		tbt_plot.plot_tbt_data()        # TBT Data
		tbt_plot.plot_fit_data()        # Fit function
		tbt_plot.plot_simulated()       # Simulate Data
		tbt_plot.plot_real_space_data() # BPM Real Space Trajectory
		tbt_plot.plot_fingerprint()
		tbt_plot.report_initial_coordinates()
	
		tbt_plot.plot_foil_coordinates()# Phase space plot
	#--------------------------------------------------------------------------------

		frame.revalidate()

		return

plotActionListener = PlotActionListener()		
plotButton.addActionListener(plotActionListener)

class SetRefListener(ActionListener):
	def __init__(self):
		return 
	def actionPerformed(self,e):
		calc.setRef()

		return

class SaveRefListener(ActionListener):
	def __init__(self):
		return 
	def actionPerformed(self,e):
		calc.saveRef()

		return

class LoadRefListener(ActionListener):
	def __init__(self):
		return 
	def actionPerformed(self,e):
		calc.loadRef()
		tbt_plot.clean_up_fp()
		tbt_plot.plot_fingerprint()

		frame.revalidate()

		return

# Need a button to set the reference fits.
# Once refernce fit has been set, just make fingerprint relative to 
# ref part of the output when a new event is taken
# maybe have a button to clear the ref, if ref is empty, no fingerprint 
# calculated - 


fingerprintButton.addActionListener(SetRefListener())
saveRefButton.addActionListener(SaveRefListener())
loadRefButton.addActionListener(LoadRefListener())


while(1 < 2):
	time.sleep(2.0)


print "Done."

#sys.exit(1)
