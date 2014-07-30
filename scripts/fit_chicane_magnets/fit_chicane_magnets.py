#!/usr/bin/env jython
# This script fits chicane magnets to ring orbit ripple.

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
#from gov.sns.xal.smf.proxy import *
from xal.model import *
from xal.model.probe import *
from xal.model.probe.traj import *
from xal.model.alg import *
from xal.sim.scenario import *
from xal.tools.beam.calc import SimpleSimResultsAdaptor
from xal.extension.solver import *


# java constants
true = (1==1)
false = not true
null = None
		

# Evaluates the chicane problem.
class ChicaneEvaluator (Evaluator):
	def __init__( self, objectives, variables, ripple, base_fields, orbit_simulator, magnets, magnet_use, bpms ):
		self.objectives = objectives
		self.variables = variables
		self.ripple = ripple
		self.base_fields = base_fields
		self.orbit_simulator = orbit_simulator
		self.magnets = magnets
		self.magnet_use = magnet_use
		self.bpms = bpms

	def evaluate( self, trial ):
		fields = []
		var_index = 0
		for index in range( len( self.magnets ) ):
			base_field = self.base_fields[index]
			if self.magnet_use[index]:
				variable = self.variables.get( var_index )
				field = trial.getTrialPoint().getValue( variable )
				var_index += 1
			else:
				field = 0.0
			fields.append( field + base_field )
		distortion = self.orbit_simulator.get_simulation_distortion( self.bpms, self.magnets, fields )
		
		for index in range( len( self.objectives ) ):
			objective = self.objectives[index]
			score = objective.score( self.ripple, distortion )
			trial.setScore( objective, score )



# Closure objective.
class ClosureObjective (Objective):
	def __init__( self, name, tolerance ):
		Objective.__init__( self, name )
		self.tolerance = tolerance
	
	def score( self, ripple, distortion ):
		orbit_fit = subtract_vectors( ripple, distortion.get_orbit() )
		rms_error = self.calc_rms( orbit_fit )
		return rms_error

	def satisfaction( self, value ):
		return SatisfactionCurve.inverseSatisfaction( value, self.tolerance )

	# compute the rms of the vector
	def calc_rms( self, vector ):
		sum = 0.0
		count = 0
		for index in range( len( vector ) ):
			if not Double.isNaN( vector[index] ):
				sum += vector[index] * vector[index]
				count += 1
		return Math.sqrt( sum / count )



# Foil displacement objective.
class FoilDisplacementObjective (Objective):
	def __init__( self, name, tolerance ):
		Objective.__init__( self, name )
		self.tolerance = tolerance
	
	def score( self, ripple, distortion ):
		displacement = distortion.get_foil_displacement()
		return displacement

	def satisfaction( self, value ):
		return SatisfactionCurve.inverseSatisfaction( value, self.tolerance )



# Foil angle objective.
class FoilAngleObjective (Objective):
	def __init__( self, name, tolerance ):
		Objective.__init__( self, name )
		self.tolerance = tolerance
	
	def score( self, ripple, distortion ):
		angle = distortion.get_foil_angle()
		return angle

	def satisfaction( self, value ):
		return SatisfactionCurve.inverseSatisfaction( value, self.tolerance )



class Simulation:
	def __init__( self, orbit, foil_displacement, foil_angle ):
		self.orbit = orbit
		self.foil_displacement = foil_displacement
		self.foil_angle = foil_angle
	
	def get_orbit( self ):  return self.orbit
	
	def get_foil_displacement( self ):  return self.foil_displacement
	
	def get_foil_angle( self ):  return self.foil_angle
	
	def get_distortion( self, simulation ):
		orbit_distortion = subtract_vectors( self.orbit, simulation.get_orbit() )
		foil_displacement_change = self.foil_displacement - simulation.get_foil_displacement()
		foil_angle_change = self.foil_angle - simulation.get_foil_angle()
		return Simulation( orbit_distortion, foil_displacement_change, foil_angle_change )
		


# orbit simulator
class OrbitSimulator:
	def __init__( self, sequence, base_fields ):
		self.sequence = sequence
		self.base_fields = base_fields
		self.probe = self.make_probe( sequence )
		self.scenario = self.make_scenario( sequence, self.probe )
		self.has_synched = false
		self.base_simulation = null


	# get a new scenario
	def make_scenario( self, sequence, probe ):
		scenario = Scenario.newScenarioFor( sequence )
		scenario.setSynchronizationMode( Scenario.SYNC_MODE_RF_DESIGN )
		scenario.setProbe( probe )
		return scenario
	
	
	# get a new probe
	def make_probe( self, sequence ):
		return ProbeFactory.getTransferMapProbe( sequence, TransferMapTracker() )


	# apply magnet field
	def apply_magnet_field( self, magnet, field ):
		self.scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, field )
	
	# remove the magnet field override
	def remove_magnet_override( self, magnet ):
		self.scenario.removeModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD )


	# apply magnet fields
	def apply_magnet_fields( self, magnets, fields ):
		for index in range( len( magnets ) ):
			self.apply_magnet_field( magnets[ index ], fields[ index ] )
	
	def get_base_simulation( self, bpms, magnets ):
		if self.base_simulation != null:	 return self.base_simulation
		self.base_simulation = self.get_simulation( bpms, magnets, self.base_fields )
		return self.base_simulation
	
	# get the zero field orbit
	def get_base_orbit( self, bpms, magnets ):
		return self.get_base_simulation( bpms, magnets ).get_orbit()
	
	# get the orbit distortion due to the magnet fields
	def get_orbit_distortion( self, bpms, magnets, fields ):
		base_orbit = self.get_base_orbit( bpms, magnets )
		orbit = self.get_orbit( bpms, magnets, fields )
		return subtract_vectors( orbit, base_orbit )
	
	
	# get the simulated orbit for the specified magnet fields
	def get_orbit( self, bpms, magnets, fields ):
		simulation = self.get_simulation( bpms, magnets, fields )
		return simulation.orbit
	
	# get the simulation distortion
	def get_simulation_distortion( self, bpms, magnets, fields ):
		base_simulation = self.get_base_simulation( bpms, magnets )
		simulation = self.get_simulation( bpms, magnets, fields )
		return simulation.get_distortion( base_simulation )
	
	# get the simulated orbit for the specified magnet fields
	def get_simulation( self, bpms, magnets, fields ):
		self.use_field_setpoint( magnets )
		self.apply_magnet_fields( magnets, fields )
		if self.has_synched:
			self.scenario.resyncFromCache()
		else:
			self.scenario.resync()
			self.has_synched = true
		self.probe.reset()
		self.scenario.run()
		for magnet in magnets:  self.remove_magnet_override( magnet )
		trajectory = self.probe.getTrajectory()
		resultsAdaptor = SimpleSimResultsAdaptor( trajectory )

		foil_state = trajectory.statesForElement( "Ring_Inj:Foil" )[0]
		fixed_orbit_at_foil = resultsAdaptor.computeFixedOrbit( foil_state )
		foil_displacement = 1000 * fixed_orbit_at_foil.getx()
		foil_angle = 1000 * fixed_orbit_at_foil.getxp()
		
		orbit = []
		for bpm in bpms:
			state = trajectory.statesForElement( bpm.getId() )[0]
			fixed_orbit = resultsAdaptor.computeFixedOrbit( state )
			displacement = 1000 * fixed_orbit.getx()
			orbit.append( displacement )
			
		simulation = Simulation( orbit, foil_displacement, foil_angle )
			
		return simulation
	
	# use the field setpoint for simulations
	def use_field_setpoint( self, magnets ):
		for magnet in magnets:  magnet.setUseFieldReadback( false )



# compute the scalar product of two vectors
def scalar_product( vector_a, vector_b ):
	product = 0.0
	for index in range( len( vector_a ) ):
		if not Double.isNaN( vector_a[index] ) and not Double.isNaN( vector_b[index] ):
			product += vector_a[index] * vector_b[index]
	return product
	

# normalize the vector
def normalize_vector_norm( vector, norm ):
	scale = 1.0 / norm
	
	unit_vector = []
	for element in vector:
		unit_vector.append( element * scale )
	return unit_vector
	

# normalize the vector
def normalize_vector( vector ):
	norm = Math.sqrt( scalar_product( vector, vector ) )
	return normalize_vector_norm( vector, norm )



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
		
	def take_orbit( self ):
		for reader in self.bpm_readers:
			reader.take_x_snapshot()
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



# subtract two vectors
def subtract_vectors( vector_a, vector_b ):
	difference = []
	for index in range( len( vector_a ) ):
		difference.append( vector_a[ index ] - vector_b[ index ] )
	return difference


# report scoreboard updates
class ScoreBoardReporter(ScoreBoardListener):
	def trialScored( self, scoreboard, trial ):  pass
	
	def trialVetoed( self, scoreboard, trial ):  pass
	
	def newOptimalSolution( self, scoreboard, trial ):  print scoreboard



# experiment
class Experiment:
	def __init__( self ):
		self.sequence = XMLDataManager.loadDefaultAccelerator().findSequence( "Ring" )
		all_bpms = self.sequence.getNodesOfType( BPM.s_strType, true )
		self.bpms = all_bpms.subList( 1, all_bpms.size() - 1 )	#exclude the BPMs on the ends (i.e. ears) since we only care about the ripple
		self.reference_field_index = 1		# the reference field is DH_A11 since we don't want this to change since it affects injected beam foil position
		self.field_errors = []
		self.base_fields = []
		self.initial_amplitude = -0.005
		self.final_amplitude = 0.005
		self.magnets = self.get_chicane_magnets( self.sequence )
		self.scales = self.load_scales( self.magnets )
		self.proposal_scales = []
		self.proposed_fields = []
		self.orbit_simulator = OrbitSimulator( self.sequence, self.base_fields )
		self.orbit_reader = OrbitReader( self.sequence, self.bpms )
		self.solving_duration = 60.0
		self.magnet_use = [0, 0, 1, 1]
	
	def get_solving_duration( self ):  return self.solving_duration
	
	def get_magnet_use( self ):  return self.magnet_use
	
	def get_chicane_magnets( self, sequence ):
		magnets = []
		magnets.append( sequence.getNodeWithId( "Ring_Mag:DH_A10" ) )
		magnets.append( sequence.getNodeWithId( "Ring_Mag:DH_A11" ) )
		magnets.append( sequence.getNodeWithId( "Ring_Mag:DH_A12" ) )
		magnets.append( sequence.getNodeWithId( "Ring_Mag:DH_A13" ) )
		return magnets
	
	def load_scales( self, magnets ):
		fields = []
		for magnet in magnets:  fields.append( magnet.getFieldSetting() )
		scales = []
		for field in fields:  scales.append( field / fields[ self.reference_field_index ] )
		self.base_fields = fields
		return scales
	
	def get_scales( self ):  return self.scales
	
	def get_scale( self, index ):  return self.scales[ index ]
	
	def set_scale( self, index, value ):  self.scales[ index ] = value
	
	def apply_amplitude( self, amplitude ):
		fields = self.get_fields_with_amplitude( amplitude )
		for index in range( len( self.magnets ) ):
			magnet = self.magnets[index]
			magnet.setField( fields[index] )
			print magnet.getId(), "  field: ", fields[index]
				
	def get_fields_with_amplitude( self, amplitude ):
		fields = []
		ref_field = self.base_fields[ self.reference_field_index ]
		for index in range( len( self.magnets ) ):
			magnet = self.magnets[index]
			scale = self.scales[index]
			base_field = self.base_fields[index]
			fields.append( base_field + scale * amplitude * ref_field )
		return fields
	
	def get_simulation_with_amplitude( self, amplitude ):
		fields = self.get_fields_with_amplitude( amplitude )
		simulation = self.orbit_simulator.get_simulation( self.bpms, self.magnets, fields )
		return simulation
	
	def get_initial_amplitude( self ):  return self.initial_amplitude
		
	def set_initial_amplitude( self, amplitude ):
		self.initial_amplitude = amplitude
		self.apply_amplitude( amplitude )
	
	def take_initial_orbit( self ):
		self.initial_orbit = self.orbit_reader.take_orbit()
	
	def get_final_amplitude( self ):  return self.final_amplitude
	
	def set_final_amplitude( self, amplitude ):
		self.final_amplitude = amplitude
		self.apply_amplitude( amplitude )
	
	def take_final_orbit( self ):
		self.final_orbit = self.orbit_reader.take_orbit()
	
	def restore_base_fields( self ):
		for index in range( len( self.magnets ) ):
			magnet = self.magnets[index]
			base_field = self.base_fields[index]
			magnet.setField( base_field )
	
	def fit_ripple( self, duration ):
		self.solving_duration = duration
		magnet_use = self.magnet_use
		self.ripple = subtract_vectors( self.initial_orbit, self.final_orbit )
		ripple_rms = self.get_ripple_rms()
		print "ripple RMS: ", ripple_rms
		tolerance = 0.01 * ripple_rms	# set the tolerance to 1% of the measured ripple
		
		variable_magnets = []
		for index in range( len( self.magnets ) ):
			if magnet_use[index]:  variable_magnets.append( self.magnets[index] )
		
		variables = ArrayList()
		for magnet in variable_magnets:  variables.add( Variable( magnet.getId(), 0.0, -0.01, 0.01 ) )

		objectives = ArrayList()
		objectives.add( ClosureObjective( "closure", tolerance ) )
		#objectives.add( FoilDisplacementObjective( "foil displacement", 5.0 ) )
		#objectives.add( FoilAngleObjective( "foil angle", 1.0 ) )

		evaluator = ChicaneEvaluator( objectives, variables, self.ripple, self.base_fields, self.orbit_simulator, self.magnets, magnet_use, self.bpms  )

		problem = Problem( objectives, variables, evaluator )
		maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 5.0, duration, 0.99 )
		solver = Solver( maxSolutionStopper )
		solver.getScoreBoard().addScoreBoardListener( ScoreBoardReporter() )
		solver.solve( problem )
		print solver.getScoreBoard()
		best_solution = solver.getScoreBoard().getBestSolution()
		best_point = best_solution.getTrialPoint()
		
		self.field_errors = []
		var_index = 0
		for index in range( len( magnet_use ) ):
			if magnet_use[index]:
				variable = variables.get( var_index )
				self.field_errors.append( best_point.getValue( variable ) )
				var_index += 1
			else:  self.field_errors.append( 0.0 )
		print "field errors: ", self.field_errors
		
		self.proposal_scales = []
		ref_field = self.base_fields[ self.reference_field_index ]
		for index in range( len( self.magnets ) ):
			field_error = self.field_errors[index]
			proposed_scale = self.scales[index] + field_error / ( ref_field * ( self.final_amplitude - self.initial_amplitude ) )
			self.proposal_scales.append( proposed_scale )
		
		proposed_fields = []
		for index in range( len( self.proposal_scales ) ):  proposed_fields.append( ref_field * self.proposal_scales[ index ] )
		self.proposed_fields = proposed_fields

		return self.field_errors
		
	def apply_proposal( self ):
		for index in range( len( self.proposal_scales ) ):  self.scales[index] = self.proposal_scales[index]
	
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
		if ( len( self.field_errors ) == 0 ):  return Double.NaN
		fitting_fields = []
		for index in range( len( self.magnets ) ):
			fitting_fields.append( self.base_fields[index] + self.field_errors[index] )
		field_ripple = self.orbit_simulator.get_orbit_distortion( self.bpms, self.magnets, fitting_fields )
		fit_error = subtract_vectors( field_ripple, self.ripple )
		sum = 0.0
		count = 0
		for offset in fit_error:
			if not Double.isNaN( offset ):
				sum += offset * offset
				count += 1
		return Math.sqrt( sum / count )
		
	
	def get_base_fields( self ):  return self.base_fields
	
	def get_base_field( self, index ):  return self.base_fields[ index ]
	
	def get_proposal_scales( self ):  return self.proposal_scales
	
	def get_proposal_scale( self, index ):  return self.proposal_scales[ index ]
	
	def get_proposed_fields( self ):  return self.proposed_fields
	
	def get_proposed_field( self, index ):  return self.proposed_fields[ index ]
	
	def get_field_errors( self ):  return self.field_errors
	
	def get_field_error( self, index ):  return self.field_errors[ index ]
		
	def get_magnets( self ):  return self.magnets
	
	def get_magnet( self, index ):  return self.magnets[ index ]

	

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



# table model for displaying magnet scales and results
class MagnetTableModel( AbstractTableModel ):
	def __init__( self, experiment ):
		self.MAGNET_COLUMN = 0
		self.BASE_FIELD_COLUMN = 1
		self.SCALE_COLUMN = 2
		self.FIELD_ERROR_COLUMN = 3
		self.PROPOSED_SCALE_COLUMN = 4
		self.PROPOSED_FIELD_COLUMN = 5
		self.experiment = experiment
	
	def getRowCount( self ):
		return len( self.experiment.get_magnets() )
	
	def getColumnCount( self ):  return 6
	
	def getColumnName( self, column ):
		if column == self.MAGNET_COLUMN:  return "Magnet"
		elif column == self.BASE_FIELD_COLUMN:  return "Base Field (T)"
		elif column == self.SCALE_COLUMN:  return "Scale"
		elif column == self.FIELD_ERROR_COLUMN:  return "Field Error (T)"
		elif column == self.PROPOSED_SCALE_COLUMN:  return "Proposed Scale"
		elif column == self.PROPOSED_FIELD_COLUMN:  return "Proposed Field (T)"
		else:  return "?"
	
	def getColumnClass( self, column ):
		if column == self.MAGNET_COLUMN:  return String
		elif column == self.BASE_FIELD_COLUMN:  return FormattedNumber
		elif column == self.SCALE_COLUMN:  return FormattedNumber
		elif column == self.FIELD_ERROR_COLUMN:  return FormattedNumber
		elif column == self.PROPOSED_SCALE_COLUMN:  return FormattedNumber
		elif column == self.PROPOSED_FIELD_COLUMN:  return FormattedNumber
		else:  return String

	def isCellEditable( self, row, column ):
		return column == self.SCALE_COLUMN
	
	# convenience method to give us a proposal scale even before the run is complete
	def getProposalScale( self, row ):
		scale = Double.NaN
		if len( self.experiment.get_proposal_scales() ) > row:  scale = self.experiment.get_proposal_scale( row )
		return scale
	
	# convenience method to give us a proposed field even before the run is complete
	def getProposedField( self, row ):
		fields = self.experiment.get_proposed_fields() 
		if len( fields ) > row:  return fields[ row ]
		else:  return Double.NaN
	
	# convenience method to give us a field even before the run is complete
	def getFieldError( self, row ):
		field = Double.NaN
		if len( self.experiment.get_field_errors() ) > row:  field = self.experiment.get_field_error( row )
		return field
	
	def getValueAt( self, row, column ):
		if column == self.MAGNET_COLUMN:
			return self.experiment.get_magnet( row ).getId()
		elif column == self.BASE_FIELD_COLUMN:
			field = self.experiment.get_base_field( row )
			return FormattedNumber( "#,##0.00000", field )
		elif column == self.SCALE_COLUMN:
			scale = self.experiment.get_scale( row )
			return FormattedNumber( "#,##0.000", scale )
		elif column == self.FIELD_ERROR_COLUMN:
			field = self.getFieldError( row )
			return FormattedNumber( "#,##0.00000", field )
		elif column == self.PROPOSED_SCALE_COLUMN:
			scale = self.getProposalScale( row )
			return FormattedNumber( "#,##0.000", scale )
		elif column == self.PROPOSED_FIELD_COLUMN:
			field = self.getProposedField( row )
			return FormattedNumber( "#,##0.0000", field )
		else:
			return ""
	
	def setValueAt( self, value, row, column ):
		if column == self.SCALE_COLUMN:
			self.experiment.set_scale( row, value.doubleValue() )



# table model of magnet use
class MagnetUseTableModel( AbstractTableModel ):
	def __init__( self, experiment ):
		self.MAGNET_COLUMN = 0
		self.VARIABLE_COLUMN = 1
		
		self.experiment = experiment
	
	def getRowCount( self ):
		return len( self.experiment.get_magnets() )
	
	def getColumnCount( self ):  return 2
	
	def getColumnName( self, column ):
		if column == self.MAGNET_COLUMN:  return "Magnet"
		elif column == self.VARIABLE_COLUMN:  return "Variable"
		else:  return "?"
	
	def getColumnClass( self, column ):
		if column == self.MAGNET_COLUMN:  return String
		elif column == self.VARIABLE_COLUMN:  return Boolean
		else:  return String

	def isCellEditable( self, row, column ):
		return column == self.VARIABLE_COLUMN
		
	def getValueAt( self, row, column ):
		if column == self.MAGNET_COLUMN:
			return self.experiment.get_magnet( row ).getId()
		elif column == self.VARIABLE_COLUMN:
			isVariable = self.experiment.get_magnet_use()[row]
			if isVariable:  return Boolean(1)
			else:  return Boolean(0)
		else:
			return ""
	
	def setValueAt( self, value, row, column ):
		if column == self.VARIABLE_COLUMN:
			self.experiment.get_magnet_use()[row] = value
				
	
	
# application controller
class ControlApp:
	def __init__( self, window_reference ):
		self.window_reference = window_reference
		self.experiment = Experiment()
		
		amplitude_formatter = DecimalFormat( "#,##0.000" )
		self.window_reference.getView( "InitialAmplitudeField" ).setText( amplitude_formatter.format( self.experiment.get_initial_amplitude() ) )
		self.window_reference.getView( "FinalAmplitudeField" ).setText( amplitude_formatter.format( self.experiment.get_final_amplitude() ) )
		self.window_reference.getView( "SolvingDurationField" ).setText( amplitude_formatter.format( self.experiment.get_solving_duration() ) )
		
		magnet_variable_table = self.window_reference.getView( "MagnetVariableTable" )
		self.magnet_use_table_model = MagnetUseTableModel( self.experiment )
		magnet_variable_table.setModel( self.magnet_use_table_model )
		
		self.make_stages( window_reference )
		self.display_magnet_table()
		
	
	def make_stages( self, window_reference ):
		self.stages = []
		self.stages.append( window_reference.getView( "InitialFieldAmplitudeStage" ) )
		self.stages.append( window_reference.getView( "InitialBeamCirculationStage" ) )
		self.stages.append( window_reference.getView( "FinalFieldAmplitudeStage" ) )
		self.stages.append( window_reference.getView( "FinalBeamCirculationStage" ) )
		self.stages.append( window_reference.getView( "OptimizeStage" ) )
		self.stages.append( window_reference.getView( "IterateStage" ) )
		button_group = ButtonGroup()
		for stage in self.stages:
			button_group.add( stage )
	
	
	def run_current_stage( self ):
		stage_index = self.current_stage_index()
		
		if stage_index == 0:
			amplitude_text = self.window_reference.getView( "InitialAmplitudeField" ).getText()
			amplitude = Double.parseDouble( amplitude_text )
			self.experiment.set_initial_amplitude( amplitude )
		elif stage_index == 1:
			self.experiment.take_initial_orbit()
		elif stage_index == 2:
			amplitude_text = self.window_reference.getView( "FinalAmplitudeField" ).getText()
			amplitude = Double.parseDouble( amplitude_text )
			self.experiment.set_final_amplitude( amplitude )
		elif stage_index == 3:
			self.experiment.take_final_orbit()
		elif stage_index == 4:
			duration_text = self.window_reference.getView( "SolvingDurationField" ).getText()
			duration = Double.parseDouble( duration_text )
			print "solving duration: ", duration
			fields = self.experiment.fit_ripple( duration )
			self.experiment.restore_base_fields()
			self.display_results()
		elif stage_index == 5:
			self.experiment.apply_proposal()
		else:
			return
			
		next_stage_index = stage_index + 1
		if next_stage_index == len( self.stages ):  next_stage_index = 0
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
		
		self.display_magnet_table()
	
	
	# display the magnet table
	def display_magnet_table( self ):
		magnet_table = self.window_reference.getView( "MagnetTable" )
		magnet_table.setCellSelectionEnabled( true )
		table_model = MagnetTableModel( self.experiment )
		magnet_table.setModel( table_model )
		
	
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
		report += "Field range(Tesla):  " + str( self.experiment.get_initial_amplitude() ) + " to " + str( self.experiment.get_final_amplitude() )
		report += "\nRMS Ripple(mm):  " + formatter_fix3.format( self.experiment.get_ripple_rms() )
		report += "\nRMS Fit(mm):  " + formatter_fix3.format( self.experiment.get_fit_rms() )
		report += "\nMagnet \t Base Field(T) \t Scale \t Field Error(T) \t Proposed Scale \t Proposed Field(T) \t"
		magnets = self.experiment.get_magnets()
		base_fields = self.experiment.get_base_fields()
		fields = self.experiment.get_field_errors()
		scales = self.experiment.get_scales()
		proposed_scales = self.experiment.get_proposal_scales()
		proposed_fields = self.experiment.get_proposed_fields()
		for index in range( len( magnets ) ):
			magnet = magnets[ index ]
			base_field = base_fields[ index ]
			field = fields[ index ]
			scale = scales[ index ]
			proposed_scale = proposed_scales[ index ]
			proposed_field = proposed_fields[ index ]
			report += "\n" + magnet.getId()
			report += "\t" + formatter_fix5.format( base_field )
			report += "\t" + formatter_fix3.format( scale )
			report += "\t" + formatter_fix5.format( field )
			report += "\t" + formatter_fix3.format( proposed_scale )
			report += "\t" + formatter_fix5.format( proposed_field )
			
		clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
		contents = StringSelection( report )
		clipboard.setContents( contents, contents )



# setup the help
def load_help( window_reference ):
	helpPane = window_reference.getView( "HelpEditorPane" )
	helpPane.setContentType( "text/html" )
	folder = File( sys.argv[0] ).getParentFile()
	helpFile = File( folder, "help.html" )
	contents = ""
	reader = BufferedReader( FileReader( helpFile ) )
	while reader.ready():  contents += reader.readLine()
	helpPane.setText( contents )



# load the user interface
def load_user_interface():
	# locate the enclosing folder and get the bricks file within it
	folder = File( sys.argv[0] ).getParentFile()
	url = File( folder, "gui.bricks" ).toURI().toURL()
	
	# generate a window reference resource and pass the desired constructor arguments
	window_reference = WindowReference( url, "MainWindow", [] )
	
	load_help( window_reference )
		
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


