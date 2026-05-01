#!/usr/bin/env jython

import sys

from jarray import *
from java.lang import *
from java.util import *
from java.io import *
from java.text import *
from javax.swing import *
from java.awt.event import *
from java.awt import *
from java.util.regex import *

from gov.sns.xal.smf import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.model import *
from gov.sns.xal.model.alg import *
from gov.sns.xal.model.alg.resp import *
from gov.sns.xal.model.probe import *
from gov.sns.xal.model.probe.resp import *
from gov.sns.xal.model.probe.resp.traj import *
from gov.sns.xal.model.xml import *
from gov.sns.xal.model.mpx import *
from gov.sns.xal.model.scenario import *
from gov.sns.tools.beam import *
from gov.sns.xal.slg import *
from gov.sns.xal.smf.proxy import *
from gov.sns.tools.plot import *


# Java definitions
false = 0
true = 1
null = None

# constants
POSITION_FORMAT = DecimalFormat("0.000")
COORIDINATE_FORMAT = DecimalFormat("0.0E0")


# handler of the main window events
class WindowHandler(WindowAdapter):
	def windowClosed(self, event):
		#sys.exit(0)
		print "Closing window..."


# class for holding node results
class NodeResult:	
	def __init__( self, position, x, y, z ):
		self.position = position
		self.values = []
		self.values.append( x )
		self.values.append( y )
		self.values.append( z )
	
	def get_value( self, field ):
		return self.values[field];


# Plotter for plotting results in a chart on a window
class Plotter:
	def __init__( self, title, label ):
		self.frame = JFrame( title )
		self.frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE)
		self.frame.addWindowListener( WindowHandler() )
		self.frame.setSize( 1000, 500 )
		
		container = self.frame.getContentPane()
		container.setLayout( BorderLayout() )
		box = Box( BoxLayout.Y_AXIS )
		container.add( box )
		self.chart = FunctionGraphsJPanel()
		self.chart.setName( title )
		self.chart.setAxisNameX( " position (m)" );
		self.chart.setAxisNameY( label );
		box.add( self.chart )
		self.frame.show()
		
	def plot_node_results( self, results, field, isLineGraph, color, label ):
		graph_data = BasicGraphData()
		graph_data.setDrawLinesOn( isLineGraph )
		graph_data.setGraphProperty( self.chart.getLegendKeyString(), label )
		self.chart.setLegendVisible( true );
		graph_data.setGraphColor( color )
		
		for result in results:
			graph_data.addPoint( result.position, result.get_value( field ) )
			
		self.chart.addGraphData( graph_data )


# change a corrector to kick the beam
def kickBeam( scenario, sequence, corrector, field ):
	scenario.setModelInput( corrector, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field )
	print "add field", field, "Tesla to corrector: ", corrector.getId(), ", position: ", sequence.getPosition(corrector), ", effective length: ", corrector.getEffLength()


# print results for the ring
def print_ring_results( trajectory ):
	print ""
	print "tunes: ", trajectory.getTunes()


# plot node results
def generate_xal_results( scenario, trajectory, sequence, nodes ):
	node_iterator = nodes.iterator()
	closed_results = []
	winding_results = []
	NUM_TURNS = 100
	TURN_STEP = 25
	print "First elements: ", trajectory.statesInPositionRange( 0.0, 0.1 )
	while node_iterator.hasNext():
		node = node_iterator.next()
		position = sequence.getPosition( node )
		
		state = trajectory.stateForElement( node.getId() )
		phase_orbit_array = state.phaseCoordinatesTurnByTurn( NUM_TURNS )
		closed_orbit = state.getFixedOrbit()
		
		x = closed_orbit.getx() * 1000		# convert from meters to mm
		y = closed_orbit.gety() * 1000		# convert from meters to mm
		z = closed_orbit.getz()
		
		print node.getId(), sequence.getPosition( node ), state.getPosition(), scenario.getPositionRelativeToStart( sequence.getPosition( node ) )
		print "closed orbit: ", closed_orbit
		print ""
		
		closed_results.append( NodeResult( position, x, y, z ) )
		
		xAvg = 0
		for index in range( 0, NUM_TURNS, TURN_STEP ):
			phase_orbit = phase_orbit_array[index]
			phase_x = phase_orbit.getx() * 1000		# convert from meters to mm
			phase_y = phase_orbit.gety() * 1000		# convert from meters to mm
			phase_z = phase_orbit.getz() * 1000		# convert from meters to mm
			xAvg += phase_x
			winding_results.append( NodeResult( position, phase_x, phase_y, phase_z ) )
	
	return [ closed_results, winding_results ]


# load the optics
accelerator = XMLDataManager.loadDefaultAccelerator()
print "loading the accelerator..."
#accelerator = XMLDataManager.acceleratorWithPath("/Users/t6p/Projects/xal/main/xal_xmls/main_ring.xal")
sequence = accelerator.getComboSequence("Ring")
print ""

# setup the model
probe = ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )
#probe.setPhaseCoordinates( PhaseVector(0.0, 0.0, 0.0, 0.0, 0.0, 0.000266) )
probe.setPhaseCoordinates( PhaseVector(.01225, 0.00036, -0.002, -0.0009, 0.0, 0.0) )
#probe.setPhaseCoordinates( PhaseVector(0.01, 0.0, -0.002, -0.0005, 0.0, 0.0) )
scenario = Scenario.newScenarioFor( sequence )
scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN )
scenario.setProbe( probe )
#scenario.setStartElementId("Ring_Mag:DCV_A13")
scenario.setStartElementId("Ring_Inj:Foil")
#scenario.setStartElementId("Ring_Mag:DCV_C07")
print "Origin relative to start:  ", scenario.getPositionRelativeToStart( 0.0 )

# pick the 1st corrector and change it
horizontal_correctors = sequence.getNodesOfType( "dch" )		# fetch horizontal correctors
vertical_correctors = sequence.getNodesOfType( "dcv" )			# fetch vertical correctors
h_corr = horizontal_correctors.get(6)
v_corr = vertical_correctors.get(5)
kickBeam( scenario, sequence, h_corr, 0.009 )					# 1.0 mrad kick
kickBeam( scenario, sequence, v_corr, 0.0099 )					# 1.0 mrad kick

# run the online model and generate the trajectory
scenario.resync()
scenario.run()
trajectory = probe.getTrajectory()

# print results
print_ring_results( trajectory )

# generate the results at the magnets
nodes = sequence.getAllNodes()
results = generate_xal_results( scenario, trajectory, sequence, nodes )
closed_results = results[0]
winding_results = results[1]

# plot results
plotter = Plotter( "Horizontal Motion", "Horizontal Orbit (mm)" )
plotter.plot_node_results( closed_results, 0, true, Color.blue, "Closed Orbit" )
plotter.plot_node_results( winding_results, 0, false, Color.red, "Winding Orbit" )

plotter = Plotter( "Vertical Motion", "Vertical Orbit (mm)" )
plotter.plot_node_results( closed_results, 1, true, Color.blue, "Closed Orbit" )
plotter.plot_node_results( winding_results, 1, false, Color.red, "Winding Orbit" )

plotter = Plotter( "Longitudinal Motion", "z (mm)" )
plotter.plot_node_results( closed_results, 2, true, Color.blue, "Closed Orbit" )
plotter.plot_node_results( winding_results, 2, false, Color.red, "Winding Orbit" )

