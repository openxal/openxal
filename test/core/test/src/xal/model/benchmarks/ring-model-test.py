#!/usr/bin/env jython

import sys

from jarray import *
from java.lang import *
from java.util import *
from java.io import *
from java.net import *
from java.text import *
from javax.swing import *
from java.awt.event import *
from java.awt import *
from java.util.regex import *

from gov.sns.xal.smf import *
from gov.sns.xal.smf.data import *
from gov.sns.xal.smf.impl.qualify import *
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
POSITION_FORMAT = DecimalFormat( "0.000" )
COORIDINATE_FORMAT = DecimalFormat( "0.0E0" )


# handler of the main window events
class WindowHandler(WindowAdapter):
	def windowClosed(self, event):
		sys.exit(0)


# class for holding node results
class NodeResult:	
	def __init__( self, position, alphax, betax, phasex, etax, etapx, alphay, betay, phasey, etay, etapy, x, y ):
		self.position = position
		self.values = []
		self.values.append( alphax )
		self.values.append( betax )
		self.values.append( phasex )
		self.values.append( etax )
		self.values.append( etapx )
		self.values.append( alphay )
		self.values.append( betay )
		self.values.append( phasey )
		self.values.append( etay )
		self.values.append( etapy )
		self.values.append( x )
		self.values.append( y )
	
	def get_value( self, field ):
		return self.values[field];


# Plotter for plotting results in a chart on a window
class Plotter:
	def __init__( self, title, label ):
		self.frame = JFrame( title )
		self.frame.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE )
		#self.frame.addWindowListener( WindowHandler() )
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
		
	def plot_node_results( self, results, field, color, label ):
		graph_data = BasicGraphData()
		graph_data.setGraphProperty( self.chart.getLegendKeyString(), label )
		self.chart.setLegendVisible( true );
		graph_data.setGraphColor( color )
		
		for result in results:
			graph_data.addPoint( result.position, result.get_value( field ) )
			
		self.chart.addGraphData( graph_data )



# calculate the magnetic field which produces the specified kick in radians
def field_for_kick( kick, magnet, probe ):
	return kick * probe.getBeta() * probe.getGamma() * probe.getSpeciesRestEnergy() / ( magnet.getEffLength() * probe.getSpeciesCharge() * IConstants.LightSpeed )


# change a corrector to kick the beam
def kickBeam( scenario, sequence, corrector, kick ):
	field = field_for_kick( kick, corrector, scenario.getProbe() )
	scenario.setModelInput( corrector, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field )
	print "add field", field, "Tesla to corrector: ", corrector.getId(), ", position: ", sequence.getPosition(corrector), ", effective length: ", corrector.getEffLength()


# print results for the ring
def print_ring_results( trajectory ):
	print ""
	print "tunes: ", trajectory.getTunes()
	print ""


# plot node results
def generate_xal_results( trajectory, sequence, nodes ):
	node_iterator = nodes.iterator()
	results = []
	while node_iterator.hasNext():
		node = node_iterator.next()
		position = sequence.getPosition( node )
		
		print "get state for element: ", node.getId()
		state = trajectory.stateForElement( node.getId() )
		
		twiss = state.getTwiss()
		phase = state.getBetatronPhase()
		orbit = state.getFixedOrbit()
		
		alphax = twiss[0].getAlpha()
		betax = twiss[0].getBeta()
		etax = state.getChromDispersionX()
		etapx = state.getChromDispersionSlopeX()
		phasex = phase.getx()
		alphay = twiss[1].getAlpha()
		betay = twiss[1].getBeta()
		etay = state.getChromDispersionY()
		etapy = state.getChromDispersionSlopeY()
		phasey = phase.gety()
		x = orbit.getx() * 1000		# convert from meters to mm
		y = orbit.gety() * 1000		# convert from meters to mm
		
		print node.getId(), sequence.getPosition( node )
		print "twiss: ", twiss
		print "dispersion: ", state.getChromDispersionX(), state.getChromDispersionY()
		print "map: ", state.getFullTurnMap().getFirstOrder()
		print "orbit: ", orbit
		print ""
		
		result = NodeResult( position, alphax, betax, phasex, etax, etapx, alphay, betay, phasey, etay, etapy, x, y )
		results.append( result )
		
	return results


# plot results from Mad results file with dispersion scaled by relativistic beta to conform to the usual definition
def generate_mad_results( data_file, relativistic_beta ):
	pattern = Pattern.compile( "\\s+\\d+ \\w+\\s+\\d\\s+\\d+\\.\\d+\\s+\\d+\\.\\d+.*" )
	reader = BufferedReader( FileReader( data_file ) )
	results = []
	
	pi2 = 2 * Math.PI
	while true :
		line = reader.readLine()
		if line == null: break
		matcher = pattern.matcher( String(line) )
		if ( matcher.matches() ):
			position = Double.parseDouble( String(line).substring(22, 29) )
			betax = Double.parseDouble( String(line).substring( 29, 38 ) )
			alphax = Double.parseDouble( String(line).substring( 38, 45 ) )
			phasex = Double.parseDouble( String(line).substring( 45, 53 ) )
			x = Double.parseDouble( String(line).substring( 53, 60 ) )
			etax = relativistic_beta * Double.parseDouble( String(line).substring( 67, 74 ) )
			etapx = relativistic_beta * Double.parseDouble( String(line).substring( 74, 80 ) )
			betay = Double.parseDouble( String(line).substring( 80, 89 ) )
			alphay = Double.parseDouble( String(line).substring( 89, 96 ) )
			phasey = Double.parseDouble( String(line).substring( 96, 104 ) )
			y = Double.parseDouble( String(line).substring( 104, 111 ) )
			etay = relativistic_beta * Double.parseDouble( String(line).substring( 118, 125 ) )
			etapy = relativistic_beta * Double.parseDouble( String(line).substring( 125, 131 ) )
			
			phasex = pi2 * ( phasex - Math.floor( phasex ) )
			phasey = pi2 * ( phasey - Math.floor( phasey ) )
			
			results.append( NodeResult( position, alphax, betax, phasex, etax, etapx, alphay, betay, phasey, etay, etapy, x, y ) )
	
	return results


# determine whether or not to use kicks in the test
def should_test_with_kicks( commands ):
	if commands.has_key('kicks'):  return true
	else: return false


# get the file path to the specified file
def get_data_file( filename ):
	current_folder = File( sys.argv[0] ).getParent()
	data_folder = File( current_folder, "data" )
	return File( data_folder, filename )
	
	
# get command line arguments as a map
def get_commands():
	commands = {}
	args = ArrayList()
	for arg in sys.argv:
		args.add( arg )
	iter = args.iterator()
	iter.next()
	while( iter.hasNext() ):
		arg = iter.next()
		if arg == "kicks":
			commands['kicks'] = true
		elif arg == "dPoP":
			dPoP = iter.next()
			commands['dPoP'] = dPoP
	return commands


# main procedure
print "processing user commands..."
commands = get_commands()

print "loading the accelerator..."
accelerator = XMLDataManager.loadDefaultAccelerator()
sequence = accelerator.getComboSequence("Ring")

# setup the model
probe = ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )
if commands.has_key('dPoP'):
	dPoP = float( commands['dPoP'] )		# delta P / P
	beta = probe.getBeta()
	K = probe.getKineticEnergy() + beta * beta * ( probe.getKineticEnergy() + probe.getSpeciesRestEnergy() ) * dPoP
	probe.setKineticEnergy( K );
scenario = Scenario.newScenarioFor( sequence )
scenario.setSynchronizationMode( Scenario.SYNC_MODE_DESIGN )
scenario.setProbe( probe )

# if the user wants to test with kicks, pick the same horizontal and vertical correctors as used in the MAD input and specify a 1.0 mrad kick
if should_test_with_kicks( commands ):
	h_corr = sequence.getNodeWithId( "Ring_Mag:DCH_B13" )
	v_corr = sequence.getNodeWithId( "Ring_Mag:DCV_B09" )
	kickBeam( scenario, sequence, h_corr, 0.001 )	# 1.0 mrad kick
	kickBeam( scenario, sequence, v_corr, 0.001 )	# 1.0 mrad kick

# run the online model and generate the trajectory
scenario.resync()
scenario.run()
trajectory = probe.getTrajectory()

# print results
print_ring_results( trajectory )

# generate the results at the magnets
nodes = sequence.getAllNodes()
xal_results = generate_xal_results( trajectory, sequence, nodes )

relativistic_beta = probe.getBeta()
if should_test_with_kicks( commands ):
	mad_results = generate_mad_results( get_data_file( "mad-out-kicks.txt" ), relativistic_beta )
else:
	mad_results = generate_mad_results( get_data_file( "mad-out.txt" ), relativistic_beta )


# plot results -------------------------------------------------------------

plotter = Plotter( "Alpha X - MAD Versus XAL", "Horizontal Alpha (m)" )
plotter.plot_node_results( mad_results, 0, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 0, Color.BLUE, "XAL" )

plotter = Plotter( "Beta X - MAD Versus XAL", "Horizontal Beta (m)" )
plotter.plot_node_results( mad_results, 1, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 1, Color.BLUE, "XAL" )

plotter = Plotter( "Horizontal Phase - MAD Versus XAL", "Horizontal Phase (rad)" )
plotter.plot_node_results( mad_results, 2, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 2, Color.BLUE, "XAL" )

plotter = Plotter( "Horizontal Dispersion - MAD Versus XAL", "Horizontal Dispersion (m)" )
plotter.plot_node_results( mad_results, 3, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 3, Color.BLUE, "XAL" )

plotter = Plotter( "Horizontal Dispersion Slope (Eta\') - MAD Versus XAL", "Horizontal Dispersion Slope (Eta\') (m)" )
plotter.plot_node_results( mad_results, 4, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 4, Color.BLUE, "XAL" )

plotter = Plotter( "Alpha Y - MAD Versus XAL", "Vertical Alpha (m)" )
plotter.plot_node_results( mad_results, 5, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 5, Color.BLUE, "XAL" )

plotter = Plotter( "Beta Y - MAD Versus XAL", "Vertical Beta (m)" )
plotter.plot_node_results( mad_results, 6, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 6, Color.BLUE, "XAL" )

plotter = Plotter( "Vertical Phase - MAD Versus XAL", "Vertical Phase (rad)"  )
plotter.plot_node_results( mad_results, 7, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 7, Color.BLUE, "XAL" )

plotter = Plotter( "Vertical Dispersion - MAD Versus XAL", "Vertical Dispersion (m)" )
plotter.plot_node_results( mad_results, 8, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 8, Color.BLUE, "XAL" )

plotter = Plotter( "Vertical Dispersion Slope (Eta\') - MAD Versus XAL", "Vertical Dispersion Slope (Eta\') (m)" )
plotter.plot_node_results( mad_results, 9, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 9, Color.BLUE, "XAL" )

plotter = Plotter( "Horizontal Orbit - MAD Versus XAL", "X (mm)"  )
plotter.plot_node_results( mad_results, 10, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 10, Color.BLUE, "XAL" )

plotter = Plotter( "Vertical Orbit - MAD Versus XAL", "Y (mm)" )
plotter.plot_node_results( mad_results, 11, Color.RED, "MAD" )
plotter.plot_node_results( xal_results, 11, Color.BLUE, "XAL" )

plotter = Plotter( "Alpha - MAD Versus XAL", "Alpha (m)" )
plotter.plot_node_results( mad_results, 0, Color.RED, "MAD Horizontal Alpha" )
plotter.plot_node_results( xal_results, 0, Color.BLUE, "XAL Horizontal Alpha" )
plotter.plot_node_results( mad_results, 5, Color.ORANGE, "MAD Vertical Alpha" )
plotter.plot_node_results( xal_results, 5, Color.GREEN, "XAL Vertical Alpha" )

plotter = Plotter( "Beta - MAD Versus XAL", "Beta (m)" )
plotter.plot_node_results( mad_results, 1, Color.RED, "MAD Horizontal Beta" )
plotter.plot_node_results( xal_results, 1, Color.BLUE, "XAL Horizontal Beta" )
plotter.plot_node_results( mad_results, 6, Color.ORANGE, "MAD Vertical Beta" )
plotter.plot_node_results( xal_results, 6, Color.GREEN, "XAL Vertical Beta" )

plotter = Plotter( "Closed Orbit - MAD Versus XAL", "Closed Orbit (mm)" )
plotter.plot_node_results( mad_results, 10, Color.RED, "MAD Horizontal Orbit" )
plotter.plot_node_results( xal_results, 10, Color.BLUE, "XAL Horizontal Orbit" )
plotter.plot_node_results( mad_results, 11, Color.ORANGE, "MAD Vertical Orbit" )
plotter.plot_node_results( xal_results, 11, Color.GREEN, "XAL Vertical Orbit" )

print ""

