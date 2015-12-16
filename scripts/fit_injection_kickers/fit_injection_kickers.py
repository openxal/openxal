#!/usr/bin/env jython
# This script fits kicker amplitude fields to orbit ripple.
# H Kicker 1 Waveform:  Ring_Mag:PS_IKickH01:7275:WFCH1
# H Kicker 1 Amplitude:  Ring_Mag:PS_IKickH01:7121:AMPL

import sys
import string

from jarray import *
from java.io import *
from java.lang import *
from java.util import *
from java.text import *
from javax.swing import *
from javax.swing.event import *
from javax.swing.table import *
from java.awt import *
from java.awt.event import *
from java.awt.datatransfer import *

from xal.tools.statistics import *
from xal.tools.apputils import *
from xal.tools.text import *
#from gov.sns.xal.tools.widgets import *
from xal.extension.widgets import *
from xal.extension.widgets.apputils import *
from xal.extension.widgets.plot import *
from xal.extension.bricks import *
from xal.ca import *
from xal.smf import *
from xal.smf.impl  import *
from xal.smf.data import *
from xal.smf.impl.qualify import *
from xal.smf.proxy import *
from xal.model import *
from xal.model.probe import *
from xal.model.probe.traj import *
from xal.model.alg import *
from xal.sim.scenario import *
from xal.tools.beam.calc import SimpleSimResultsAdaptor



# java constants
true = (1==1)
false = not true
null = None


# plane adaptor
class PlaneAdaptor:
	def __init__( self ):
		self.KICKER_TYPE = ""
		
	def __repr__( self ):
		return none
		
	def getKickers( self, sequence ):
		return sequence.getNodesOfType( self.KICKER_TYPE, true )
	
	# offset in mm
	def getOffset( self, phaseVector ):
		return 0
	
	# angle in mrad
	def getAngle( self, phaseVector ):
		return 0
		
	def take_snapshot( self, bpm_reader ):
		pass
		
		
# horizontal plane adaptor
class HorizontalAdaptor(PlaneAdaptor):
	def __init__( self ):
		self.KICKER_TYPE = HorizontalKicker.s_strType
		
	def __repr__( self ):
		return "Horizontal"
	
	# offset in mm
	def getOffset( self, phaseVector ):
		return 1000 * phaseVector.getx()
	
	# angle in mrad
	def getAngle( self, phaseVector ):
		return 1000 * phaseVector.getxp()
		
	def take_snapshot( self, bpm_reader ):
		bpm_reader.take_x_snapshot()

		
# vertical plane adaptor
class VerticalAdaptor(PlaneAdaptor):
	def __init__( self ):
		self.KICKER_TYPE = VerticalKicker.s_strType
		
	def __repr__( self ):
		return "Vertical"
	
	# offset in mm
	def getOffset( self, phaseVector ):
		return 1000 * phaseVector.gety()
	
	# angle in mrad
	def getAngle( self, phaseVector ):
		return 1000 * phaseVector.getyp()
		
	def take_snapshot( self, bpm_reader ):
		bpm_reader.take_y_snapshot()
		

# register the vertical kicker type with the type manager
def registerKickerTypes():
        ElementTypeManager.defaultManager().registerType( VerticalKicker, VerticalKicker.s_strType );
        ElementTypeManager.defaultManager().registerType( HorizontalKicker, HorizontalKicker.s_strType );


# vertical injection kicker
class VerticalKicker(VDipoleCorr):
	s_strType = "IKickV"
	
	def __init__( self, nodeID, model, position ):
		VDipoleCorr.__init__( self, nodeID )
		self.model = model
		self.mag_length = model.getLength()
		self.setPosition( position )
		amplitude_pv = "Ring_Mag:PS_" + nodeID[9:] + ":7121:AMPL"
		self.amplitude_channel = ChannelFactory.defaultFactory().getChannel( amplitude_pv )
		self.amplitude_channel.requestConnection()
		self.scale_factor = 1.0
	
	def getType( self ):  return s_strType
	
	# get the scale factor to apply to amplitude put operations
	def getScaleFactor( self ):  return self.scale_factor
	
	# set the scale factor to apply to amplitude put operations
	def setScaleFactor( self, value ):  self.scale_factor = value
	
	def getField( self ):  return 0.0			# prevent error on live synchronization since this is an artificial node with no field PV
	
	def getEffLength( self ):  return self.mag_length
	
	def getCurrent( self, field ):  return self.model.getCurrent( field )
	
	# get the flat top waveform voltage
	def getVoltage( self, field ):  return self.model.getVoltage( self.getCurrent( field ) )
	
	def request_amplitude( self, amplitude ):
		voltage = amplitude * self.scale_factor
		if self.amplitude_channel.isConnected():
			self.amplitude_channel.putVal( voltage )



# horizontal injection kicker
class HorizontalKicker(HDipoleCorr):
	s_strType = "IKickH"
	
	def __init__( self, nodeID, model, position ):
		HDipoleCorr.__init__( self, nodeID )
		self.model = model
		self.mag_length = model.getLength()
		self.setPosition( position )
		amplitude_pv = "Ring_Mag:PS_" + nodeID[9:] + ":7121:AMPL"
		self.amplitude_channel = ChannelFactory.defaultFactory().getChannel( amplitude_pv )
		self.amplitude_channel.requestConnection()
		self.scale_factor = 1.0
	
	def getType( self ):  return s_strType
	
	# get the scale factor to apply to amplitude put operations
	def getScaleFactor( self ):  return self.scale_factor
	
	# set the scale factor to apply to amplitude put operations
	def setScaleFactor( self, value ):  self.scale_factor = value
	
	def getField( self ):  return 0.0			# prevent error on live synchronization since this is an artificial node with no field PV
	
	def getEffLength( self ):  return self.mag_length
	
	def getCurrent( self, field ):  return self.model.getCurrent( field )
	
	# get the flat top waveform voltage
	def getVoltage( self, field ):  return self.model.getVoltage( self.getCurrent( field ) )
	
	def request_amplitude( self, amplitude ):
		voltage = amplitude * self.scale_factor
		if self.amplitude_channel.isConnected():
			self.amplitude_channel.putVal( voltage )



# kicker model which identifies a common length and conversion factors
class KickerModel:
	def __init__( self, length, b_to_i ):
		self.length = length
		self.b_to_i = b_to_i
	
	def getLength( self ):  return self.length
	
	def getCurrent( self, field ):  return self.b_to_i * field
	
	# get the flat top waveform voltage
	def getVoltage( self, current ):  return current / 140		# voltage = 10 V ( current / 1400 Amps )



# get the ring with the injection kickers added
def getRingWithInjectionKickers( accelerator ):
	registerKickerTypes()
	
	short_model = KickerModel( 0.428, 14855.74 )
	long_model = KickerModel( 0.839, 15731.25 )
	
	ring1 = accelerator.getSequence( "Ring1" )
	ring1.addNode( HorizontalKicker( "Ring_Mag:IKickH03", short_model, 11.125862 ) )
	ring1.addNode( VerticalKicker( "Ring_Mag:IKickV03", short_model, 10.585862 ) )
	ring1.addNode( HorizontalKicker( "Ring_Mag:IKickH04", long_model, 13.815862 ) )
	ring1.addNode( VerticalKicker( "Ring_Mag:IKickV04", long_model, 12.655862 ) )
	
	ring5 = accelerator.getSequence( "Ring5" )
	ring5.addNode( HorizontalKicker( "Ring_Mag:IKickH01", long_model, 0.45 ) )
	ring5.addNode( VerticalKicker( "Ring_Mag:IKickV01", long_model, 1.61 ) )
	ring5.addNode( HorizontalKicker( "Ring_Mag:IKickH02", short_model, 3.1395 ) )
	ring5.addNode( VerticalKicker( "Ring_Mag:IKickV02", short_model, 3.6795 ) )
	
	ring2 = accelerator.getSequence( "Ring2" )
	ring3 = accelerator.getSequence( "Ring3" )
	ring4 = accelerator.getSequence( "Ring4" )
	
	segments = ArrayList()
	segments.add( ring1 )
	segments.add( ring2 )
	segments.add( ring3 )
	segments.add( ring4 )
	segments.add( ring5 )

	return AcceleratorSeqCombo( "Ring", segments )


# get a new scenario
def get_scenario( sequence, probe ):
	scenario = Scenario.newScenarioFor( sequence )
	scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN )
	scenario.setProbe( probe )
	return scenario
	
	
# get a new probe
def get_probe( sequence ):
	return ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )


# get the beam displacements for the specified plane
def get_orbit( bpms, trajectory, plane_adaptor ):
	resultsAdaptor = SimpleSimResultsAdaptor( trajectory )
	orbit = []
	for bpm in bpms:
		state = trajectory.statesForElement( bpm.getId() )[0]
		displacement = plane_adaptor.getOffset( resultsAdaptor.computeFixedOrbit( state ) )
		orbit.append( displacement )
		
	return orbit


# apply kicker field
def apply_kicker_field( scenario, kicker, field ):
	scenario.setModelInput( kicker, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field )


# apply kicker fields
def apply_kicker_fields( scenario, kickers, fields ):
	for index in range( len( kickers ) ):
		apply_kicker_field( scenario, kickers[ index ], fields[ index ] )


# compute the scalar product of two vectors
def scalar_product( vector_a, vector_b ):
	product = 0.0
	for index in range( len( vector_a ) ):
		if not Double.isNaN( vector_a[index] ) and not Double.isNaN( vector_b[index] ):
			product += vector_a[index] * vector_b[index]
	return product
	

# normalize the vector
def normalize_vector_norm( vector, norm ):
	scale = 1.0 / Math.sqrt( norm )
	
	unit_vector = []
	for element in vector:
		unit_vector.append( element * scale )
	return unit_vector
	

# normalize the vector
def normalize_vector( vector ):
	norm = scalar_product( vector, vector )
	return normalize_vector( vector, norm )
	
	
# generate the design orbit
def get_design_orbit( bpms ):
	orbit = []
	for index in range( len( bpms ) ):
		orbit.append( 0.0 )
	orbit[0] = 0.01
	orbit[ len(bpms) - 1 ] = 0.01
	return orbit	
	


# BPM reader
class BPMReader(IEventSinkValDbl):
	def __init__( self, bpm ):
		self.bpm = bpm
		self.latest_value = Double.NaN
		self.x_avg_channel = bpm.getChannel( BPM.X_AVG_HANDLE )
		self.x_avg_channel.requestConnection()
		self.y_avg_channel = bpm.getChannel( BPM.Y_AVG_HANDLE )
		self.y_avg_channel.requestConnection()
	
	def take_x_snapshot( self ):
		self.latest_value = Double.NaN
		if self.x_avg_channel.isConnected():
			self.take_snapshot( self.x_avg_channel )
	
	def take_y_snapshot( self ):
		self.latest_value = Double.NaN
		if self.y_avg_channel.isConnected():
			self.take_snapshot( self.y_avg_channel )
		 
	def take_snapshot( self, channel ):
		channel.getValDblCallback( self )
		
	def has_reading( self ):
		return not Double.isNaN( self.latest_value )
	
	def get_latest_value( self ):
		return self.latest_value
	
	def eventValue( self, value, channel ):
		self.latest_value = value
		


# takes an orbit snapshot
class OrbitReader:
	def __init__( self, sequence, bpms ):
		self.sequence = sequence
		self.bpms = bpms
		self.make_bpm_readers()
	
	def make_bpm_readers( self ):
		readers = []
		for bpm in self.bpms:
			readers.append( BPMReader( bpm ) )
		self.bpm_readers = readers
		Channel.flushIO()
		
	def take_orbit( self, plane_adaptor ):
		for reader in self.bpm_readers:
			plane_adaptor.take_snapshot( reader )
		Channel.flushIO()
		return self.get_orbit()
	
	def has_orbit( self ):
		for reader in self.bpm_readers:
			if not reader.has_reading():  return false
		return true
	
	def get_orbit( self ):
		max_trials = 10
		while max_trials > 0 and not self.has_orbit():
			max_trials -= 1
			Thread.sleep( 500 )
		orbit = []
		for reader in self.bpm_readers:
			orbit.append( reader.get_latest_value() )
		return orbit
		


# orbit simulator
class OrbitSimulator:
	def __init__( self, sequence ):
		self.sequence = sequence
		self.probe = self.make_probe( sequence )
		self.scenario = self.make_scenario( sequence, self.probe )
		self.has_synched = false
		self.zero_field_orbit_plane = null


	# get a new scenario
	def make_scenario( self, sequence, probe ):
		scenario = Scenario.newScenarioFor( sequence )
		scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN )
		scenario.setProbe( probe )
		return scenario
	
	
	# get a new probe
	def make_probe( self, sequence ):
		return ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )


	# apply kicker field
	def apply_kicker_field( self, kicker, field ):
		self.scenario.setModelInput( kicker, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field )


	# apply kicker fields
	def apply_kicker_fields( self, kickers, fields ):
		for index in range( len( kickers ) ):
			self.apply_kicker_field( kickers[ index ], fields[ index ] )
	
	# get the zero field orbit
	def get_zero_field_orbit( self, bpms, plane_adaptor ):
		if self.zero_field_orbit_plane == plane_adaptor:		# need to recalculate only if the plane has changed since last calculation
			return self.zero_field_orbit
		if self.has_synched:
			self.scenario.resyncFromCache()
		else:
			self.scenario.resync()
			self.has_synched = true
		self.probe.reset()
		self.scenario.run()
		trajectory = self.probe.getTrajectory()
		resultsAdaptor = SimpleSimResultsAdaptor( trajectory )
		
		orbit = []
		for bpm in bpms:
			state = trajectory.statesForElement( bpm.getId() )[0]
			displacement = plane_adaptor.getOffset( resultsAdaptor.computeFixedOrbit( state ) )
			orbit.append( displacement )
			
		self.zero_field_orbit = orbit
		self.zero_field_orbit_plane =  plane_adaptor
		return orbit
		
	
	# get the orbit distortion due to the kicker fields
	def get_orbit_distortion( self, bpms, kickers, plane_adaptor, fields ):
		orbit = self.get_orbit( bpms, kickers, plane_adaptor, fields )
		zero_field_orbit = self.get_zero_field_orbit( bpms, plane_adaptor )
		return subtract_vectors( orbit, zero_field_orbit )
	
	
	# get the simulated orbit for the specified kicker settings
	def get_orbit( self, bpms, kickers, plane_adaptor, fields ):
		self.apply_kicker_fields( kickers, fields )
		if self.has_synched:
			self.scenario.resyncFromCache()
		else:
			self.scenario.resync()
			self.has_synched = true
		self.probe.reset()
		self.scenario.run()
		for kicker in kickers:  self.apply_kicker_field( kicker, 0.0 )
		trajectory = self.probe.getTrajectory()
		resultsAdaptor = SimpleSimResultsAdaptor( trajectory )

		orbit = []
		for bpm in bpms:
			state = trajectory.statesForElement( bpm.getId() )[0]
			displacement = plane_adaptor.getOffset( resultsAdaptor.computeFixedOrbit( state ) )
			orbit.append( displacement )
			
		return orbit



# fits orbits to kicker fields
class KickerFitter:
	def __init__( self, sequence, orbit_simulator, kickers, plane_adaptor ):
		self.sequence = sequence
		self.orbit_simulator = orbit_simulator
		self.kickers = kickers
		self.plane_adaptor = plane_adaptor
	
	# find the kickers that fit the specified orbit to within the tolerance
	def fit_kickers( self, bpms, orbit, tolerance ):
		field_scales = []
		responses = self.get_kicker_response_vectors( bpms, field_scales )
		coefficients = []
		for kicker in self.kickers:  coefficients.append( 0.0 )
		remainder = orbit
		for index in range( 10000 ):
			remainder = self.find_best_kicker_match( remainder, responses, coefficients )
			rms_error = self.calc_rms( remainder )
			if rms_error < tolerance:  break
		fields = []
		for index in range( len( coefficients ) ):
			fields.append( coefficients[index] * field_scales[index] )
		return fields

	# compute the rms of the vector
	def calc_rms( self, vector ):
		sum = 0.0
		count = 0
		for index in range( len( vector ) ):
			if not Double.isNaN( vector[index] ):
				sum += vector[index] * vector[index]
				count += 1
		return Math.sqrt( sum / count )
	
	# generate the normalized kicker response vectors
	def get_kicker_response_vectors( self, bpms, scales ):
		responses = []
		fields = []
		for kicker in self.kickers:
			fields.append( 0.0 )
			
		for kindex in range( len( self.kickers ) ):
			kicker = self.kickers.get( kindex )
			field_excursion = 0.001
			fields[ kindex ] = field_excursion
			
			orbit = self.orbit_simulator.get_orbit_distortion( bpms, self.kickers, self.plane_adaptor, fields )
			norm = scalar_product( orbit, orbit )
			scales.append( field_excursion / Math.sqrt( norm ) )
			response = normalize_vector_norm( orbit, norm )
			responses.append( response )
			
			fields[ kindex ] = 0.0
			
		return responses


	# calculate the remainder from the specified response and scale factor
	def get_remainder( self, orbit, response, coefficient ):
		remainder = []
		for index in range( len( orbit ) ):
			remainder.append( orbit[index] - coefficient * response[index] )
		return remainder

		

	# find the best kicker coefficient and add it to the coefficient vector and return the orbit remainder
	def find_best_kicker_match( self, orbit, responses, coefficients ):
		best_coef = 0.0
		best_index = -1
		for index in range( len( responses ) ):
			response = responses[index]
			coefficient = scalar_product( orbit, response )
			if Math.abs( coefficient ) > Math.abs( best_coef ):
				best_coef = coefficient
				best_index = index
		coefficients[best_index] += best_coef	
		return self.get_remainder( orbit, responses[best_index], best_coef )



# subtract two vectors
def subtract_vectors( vector_a, vector_b ):
	difference = []
	for index in range( len( vector_a ) ):
		difference.append( vector_a[ index ] - vector_b[ index ] )
	return difference
	


# experiment
class Experiment:
	def __init__( self, sequence, bpms ):
		self.sequence = sequence
		self.bpms = bpms
		self.orbit_simulator = OrbitSimulator( self.sequence )
		self.orbit_reader = OrbitReader( self.sequence, self.bpms )
		self.fields = []
		self.initial_kicker_voltage = Double.NaN
		self.final_kicker_voltage = Double.NaN
	
	def set_plane_adaptor( self, plane_adaptor ):
		self.plane_adaptor = plane_adaptor
		self.kickers = plane_adaptor.getKickers( self.sequence )
	
	def set_initial_kicker_voltage( self, kicker_voltage ):
		self.initial_kicker_voltage = kicker_voltage
		for kicker in self.kickers:
			kicker.request_amplitude( kicker_voltage )
	
	def take_initial_orbit( self ):
		self.initial_orbit = self.orbit_reader.take_orbit( self.plane_adaptor )
	
	def set_final_kicker_voltage( self, kicker_voltage ):
		self.final_kicker_voltage = kicker_voltage
		for kicker in self.kickers:
			kicker.request_amplitude( kicker_voltage )
	
	def take_final_orbit( self ):
		self.final_orbit = self.orbit_reader.take_orbit( self.plane_adaptor )
	
	def fit_ripple( self ):
		self.ripple = subtract_vectors( self.initial_orbit, self.final_orbit )
		fitter = KickerFitter( self.sequence, self.orbit_simulator, self.kickers, self.plane_adaptor )
		self.fields = fitter.fit_kickers( self.bpms, self.ripple, 0.00001 )
		return self.fields
	
	def get_initial_kicker_voltage( self ):  return self.initial_kicker_voltage
	
	def get_final_kicker_voltage( self ):  return self.final_kicker_voltage
	
	def get_initial_orbit( self ):  return self.initial_orbit
	
	def get_final_orbit( self ):  return self.final_orbit
	
	def get_ripple( self ):  return self.ripple
	
	def get_ripple_rms( self ):
		sum = 0.0
		count = 0
		for offset in self.ripple:
			if not Double.isNaN( offset ):
				sum += offset * offset
				count += 1
		return Math.sqrt( sum / count )
	
	def get_fit_rms( self ):
		field_ripple = self.orbit_simulator.get_orbit_distortion( self.bpms, self.kickers, self.plane_adaptor, self.fields )
		fit_error = subtract_vectors( field_ripple, self.ripple )
		sum = 0.0
		count = 0
		for offset in fit_error:
			if not Double.isNaN( offset ):
				sum += offset * offset
				count += 1
		return Math.sqrt( sum / count )
		
	
	def get_fields( self ):  return self.fields
		
	def get_kickers( self ):  return self.kickers

	

# run handler
class RunButtonHandler( ActionListener, Runnable ):
	def __init__( self, window_reference, button, main_controller ):
		self.window_reference = window_reference
		self.button = button
		self.main_controller = main_controller
		
	def actionPerformed( self, event ):
		self.main_controller.run_current_stage()

	

# copy handler which copies results to the clipboard
class CopyReportButtonHandler( ActionListener, Runnable ):
	def __init__( self, window_reference, button, main_controller ):
		self.window_reference = window_reference
		self.button = button
		self.main_controller = main_controller
		
	def actionPerformed( self, event ):
		self.main_controller.copy_report()



# table model for displaying kicker results
class KickerTableModel( AbstractTableModel ):
	def __init__( self, kickers, fields ):
		self.KICKER_COLUMN = 0
		self.SCALE_COLUMN = 1
		self.FIELD_COLUMN = 2
		self.CURRENT_COLUMN = 3
		self.VOLTAGE_COLUMN = 4
		self.kickers = kickers
		self.fields = fields
	
	def getRowCount( self ):
		return len( self.kickers )
	
	def getColumnCount( self ):  return 5
	
	def getColumnName( self, column ):
		if column == self.KICKER_COLUMN:  return "Kicker"
		elif column == self.SCALE_COLUMN:  return "Scale"
		elif column == self.FIELD_COLUMN:  return "Field (T)"
		elif column == self.CURRENT_COLUMN:  return "Current (A)"
		elif column == self.VOLTAGE_COLUMN:  return "Voltage (V)"
		else:  return "?"
	
	def getColumnClass( self, column ):
		if column == self.KICKER_COLUMN:  return String
		elif column == self.SCALE_COLUMN:  return FormattedNumber
		elif column == self.FIELD_COLUMN:  return FormattedNumber
		elif column == self.CURRENT_COLUMN:  return FormattedNumber
		elif column == self.VOLTAGE_COLUMN:  return FormattedNumber
		else:  return String

	def isCellEditable( self, row, column ):
		return column == self.SCALE_COLUMN
	
	def getField( self, row ):
		field = Double.NaN
		if len( self.fields ) > row:  field = self.fields[ row ]
		return field
	
	def getValueAt( self, row, column ):
		if column == self.KICKER_COLUMN:
			return self.kickers[ row ]
		elif column == self.SCALE_COLUMN:
			scale = self.kickers[ row ].getScaleFactor()
			return FormattedNumber( "#,##0.000", scale )
		elif column == self.FIELD_COLUMN:
			field = self.getField( row )
			return FormattedNumber( "#,##0.00000", field )
		elif column == self.CURRENT_COLUMN:
			field = self.getField( row )
			kicker = self.kickers[ row ]
			current = kicker.getCurrent( field )
			return FormattedNumber( "#,##0.0", current )
		elif column == self.VOLTAGE_COLUMN:
			field = self.getField( row )
			kicker = self.kickers[ row ]
			voltage = kicker.getVoltage( field )
			return FormattedNumber( "#,##0.000", voltage )
		else:
			return ""
	
	def setValueAt( self, value, row, column ):
		if column == self.SCALE_COLUMN:
			self.kickers[ row ].setScaleFactor( value.doubleValue() )


	
# application controller
class ControlApp:
	def __init__( self, window_reference ):
		self.window_reference = window_reference
		accelerator = XMLDataManager.loadDefaultAccelerator()
		sequence = getRingWithInjectionKickers( accelerator )
		all_bpms = sequence.getNodesOfType( BPM.s_strType, true )
		bpms = all_bpms.subList( 1, all_bpms.size() - 1 )	#exclude the BPMs on the ends (i.e. ears) since we only care about the ripple
		self.experiment = Experiment( sequence, bpms )
		self.make_stages( window_reference )
		
	
	def make_stages( self, window_reference ):
		self.stages = []
		self.stages.append( window_reference.getView( "KickerSelectionStage" ) )
		self.stages.append( window_reference.getView( "InitialKickerAmplitudeStage" ) )
		self.stages.append( window_reference.getView( "InitialBeamCirculationStage" ) )
		self.stages.append( window_reference.getView( "FinalKickerAmplitudeStage" ) )
		self.stages.append( window_reference.getView( "FinalBeamCirculationStage" ) )
		self.stages.append( window_reference.getView( "CleanupStage" ) )
		button_group = ButtonGroup()
		for stage in self.stages:
			button_group.add( stage )
	
	
	def run_current_stage( self ):
		stage_index = self.current_stage_index()
		
		if stage_index == 0:
			horizontal_radiobutton = self.window_reference.getView( "HorizontalSelector" )
			plane_adaptor = null
			if horizontal_radiobutton.isSelected():
				plane_adaptor = HorizontalAdaptor()
			else:
				plane_adaptor = VerticalAdaptor()
			self.experiment.set_plane_adaptor( plane_adaptor )
			self.display_kicker_table()
		elif stage_index == 1:
			voltage_text = self.window_reference.getView( "InitialKickerVoltField" ).getText()
			voltage = Double.parseDouble( voltage_text )
			self.experiment.set_initial_kicker_voltage( voltage )
		elif stage_index == 2:
			self.experiment.take_initial_orbit()
		elif stage_index == 3:
			voltage_text = self.window_reference.getView( "FinalKickerVoltField" ).getText()
			voltage = Double.parseDouble( voltage_text )
			self.experiment.set_final_kicker_voltage( voltage )
		elif stage_index == 4:
			self.experiment.take_final_orbit()
			fields = self.experiment.fit_ripple()
			self.display_results()
		else:
			return
			
		next_stage_index = stage_index + 1
		if next_stage_index < len( self.stages ):
			self.stages[ next_stage_index ].setSelected( true )
	
	
	def current_stage_index( self ):
		index = 0
		for stage in self.stages:
			if stage.isSelected(): return index
			index += 1
		return -1
	
	
	# display the results
	def display_results( self ):
		formatter = DecimalFormat( "#,##0.000" )
		
		self.plot_ripple()
		
		fit_rms_label = self.window_reference.getView( "FitRMSResult" )
		fit_rms_label.setText( formatter.format( self.experiment.get_fit_rms() ) )
		
		ripple_rms_label = self.window_reference.getView( "RippleRMSResult" )
		ripple_rms_label.setText( formatter.format( self.experiment.get_ripple_rms() ) )
		
		self.display_kicker_table()
	
	
	# display the kicker table
	def display_kicker_table( self ):
		kicker_table = self.window_reference.getView( "KickerTable" )
		kicker_table.setCellSelectionEnabled( true )
		table_model = KickerTableModel( self.experiment.get_kickers(), self.experiment.get_fields() )
		kicker_table.setModel( table_model )
		
	
	# plot the ripple
	def plot_ripple( self ):
		plot = self.window_reference.getView( "RipplePlot" )
		plot.removeAllGraphData()
		plot.setAxisNameX( "BPM Index" )
		plot.setAxisNameY( "Ripple (mm)" )
		graph_data = BasicGraphData()
		graph_data.setGraphColor( Color.BLUE )
		graph_data.setGraphProperty( plot.getLegendKeyString(), "Ripple" )
		ripple = self.experiment.get_ripple()
		index = 0
		for offset in ripple:
			if not Double.isNaN( offset ):
				graph_data.addPoint( index, offset )
				index += 1
		series = Vector(1)
		series.add( graph_data )
		plot.addGraphData( series )
	
	# copy the summary to the clipboard
	def copy_report( self ):
		formatter_fix3 = DecimalFormat( "#,##0.000" )
		formatter_fix4 = DecimalFormat( "#,##0.0000" )
		formatter_fix5 = DecimalFormat( "#,##0.00000" )
		report = ""
		report += "Voltage range(V):  " + str( self.experiment.get_initial_kicker_voltage() ) + " to " + str( self.experiment.get_final_kicker_voltage() )
		report += "\n"
		report += "RMS Ripple(mm):  " + formatter_fix3.format( self.experiment.get_fit_rms() )
		report += "\n"
		report += "RMS Fit(mm):  " + formatter_fix3.format( self.experiment.get_fit_rms() )
		report += "\n"
		report += "Kicker, Scale, Field(T), Current(A), Voltage(V)"
		kickers = self.experiment.get_kickers()
		fields = self.experiment.get_fields()
		for index in range( len( kickers ) ):
			kicker = kickers[ index ]
			field = fields[ index ]
			current = kicker.getCurrent( field )
			voltage = kicker.getVoltage( field )
			report += "\n"
			report += kicker.getId()
			report += "\t" + formatter_fix3.format( kicker.getScaleFactor() )
			report += "\t" + formatter_fix5.format( field )
			report += "\t" + formatter_fix3.format( current )
			report += "\t" + formatter_fix4.format( voltage )
			
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
		contents = StringSelection( report )
		clipboard.setContents( contents, contents )



# load the user interface
def load_user_interface():
	# locate the enclosing folder and get the bricks file within it
	folder = File( sys.argv[0] ).getParentFile()
	url = File( folder, "gui.bricks" ).toURI().toURL()
	
	# generate a window reference resource and pass the desired constructor arguments
	window_reference = WindowReference( url, "MainWindow", [] )
	
	horizontal_radiobutton = window_reference.getView( "HorizontalSelector" )
	vertical_radiobutton = window_reference.getView( "VerticalSelector" )
	plane_group = ButtonGroup()
	plane_group.add( horizontal_radiobutton )
	plane_group.add( vertical_radiobutton )
	
	main_controller = ControlApp( window_reference )
	
	run_button = window_reference.getView( "RunButton" )
	run_button.addActionListener( RunButtonHandler( window_reference, run_button, main_controller ) )
	
	copy_report_button = window_reference.getView( "CopyReportButton" )
	copy_report_button.addActionListener( CopyReportButtonHandler( window_reference, copy_report_button, main_controller ) )	
	
	window = window_reference.getWindow()
	window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE )
	window.setVisible( true )
	
	return window_reference


# ---------------- main procedure -----------------------
window_ref = load_user_interface()


