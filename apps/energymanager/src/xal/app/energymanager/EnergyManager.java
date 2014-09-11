//
//  EnergyManager.java
//  xal
//
//  Created by Thomas Pelaia on 2/18/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import xal.extension.solver.Variable;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.impl.Bend;
import xal.smf.impl.Dipole;
import xal.smf.impl.HDipoleCorr;
import xal.smf.impl.Marker;
import xal.smf.impl.PermQuadrupole;
import xal.smf.impl.Quadrupole;
import xal.smf.impl.RfCavity;
import xal.smf.impl.VDipoleCorr;
import xal.smf.impl.qualify.AndTypeQualifier;
import xal.smf.impl.qualify.KindQualifier;
import xal.smf.impl.qualify.NotTypeQualifier;
import xal.smf.impl.qualify.OrTypeQualifier;
import xal.smf.impl.qualify.QualifierFactory;
import xal.smf.impl.qualify.TypeQualifier;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.data.KeyValueQualifier;
import xal.tools.data.Qualifier;
import xal.tools.data.SortOrdering;
import xal.tools.messaging.MessageCenter;
import xal.tools.xml.XmlTableIO;


/** Main model for managing the optics energy. */
public class EnergyManager implements DataListener, ParameterStoreListener, OpticsOptimizerListener {
    /** conversion factor to convert eV to MeV */
    final static private double CONVERT_MEV_TO_EV = 1.0e6;
    
	/** message center for dispatching events to registered listeners */
	protected MessageCenter _messageCenter;
	
	/** proxy which forwards events to registered listeners. */
	protected EnergyManagerListener _eventProxy;
	
	/** the sequence on which to operate */
	protected AcceleratorSeq _sequence;
	
	/** all of the node agents */
	protected List<NodeAgent> _nodeAgents;
	
	/** agents keyed by node ID */
	protected Map<String,NodeAgent> _nodeAgentTable;
	
	/** all of the live parameters */
	protected ParameterStore _parameterStore;
	
	/** optics optimizer */
	protected OpticsOptimizer _optimizer;
	
	/** the design simulation */
	protected Simulation _designSimulation;
	
	/** the custom probe to use in simulations */
	protected Probe<?> _entranceProbe;
	
	/** position range of nodes for evaluating a simulation */
	protected double[] _evaluationRange;
	
	/** the nodes where the evaluations should be counted */
	final protected List<AcceleratorNode> _evaluationNodes;
	
	
	/** Constructor */
	public EnergyManager() {
		_messageCenter = new MessageCenter( "Energy Manager" );
		_eventProxy = _messageCenter.registerSource( this, EnergyManagerListener.class );
		
		_parameterStore = new ParameterStore();
		_parameterStore.addParameterStoreListener( this );
		
		_nodeAgents = Collections.emptyList();
		_sequence = null;
		_entranceProbe = null;
		
		_evaluationRange = new double[2];
		_evaluationNodes = new ArrayList<AcceleratorNode>();
	}
	
	
	/** Dispose of resources for this model. */
	public void dispose() {
		_sequence = null;
		
		_parameterStore.removeParameterStoreListener( this );
		_parameterStore.clear();
		
		disposeNodeAgents();
	}
	
	
	/**
	 * Add the specified listener as a receiver of energy manager events from this model.
	 * @param listener the listener to add as a receiver of events
	 */
	public void addEnergyManagerListener( final EnergyManagerListener listener ) {
		_messageCenter.registerTarget( listener, this, EnergyManagerListener.class );
		listener.sequenceChanged( this, _sequence, _nodeAgents, _parameterStore.getLiveParameters() );
	}
	
	
	/**
	 * Remove the specified listener from receiving energy manager events from this model.
	 * @param listener the listener to remove from receiving energy manager events
	 */
	public void removeEnergyManagerListener( final EnergyManagerListener listener ) {
		_messageCenter.removeTarget( listener, this, EnergyManagerListener.class );
	}
	
	
	/**
	 * Get the node agents.
	 * @return the node agents
	 */
	public List<NodeAgent> getNodeAgents() {
		return _nodeAgents;
	}
	
	
	/**
	 * Get the node agent corresponding to the specified node ID.
	 * @param ID the node ID of the agent to get
	 * @return the node agent corresponding to the specified ID or null if none exists
	 */
	public NodeAgent getNodeAgentWithID( final String ID ) {
		return _nodeAgentTable.get( ID );
	}
	
	
	/** Dispose of node agents. */
	protected void disposeNodeAgents() {
		final Iterator<NodeAgent> agentIter = _nodeAgents.iterator();  // CKA - never used
//		while ( agentIter.hasNext() ) {
//			final NodeAgent agent = (NodeAgent)agentIter.next();
//			agent.dispose();
//		}
		
        for(NodeAgent nodeAgent : _nodeAgents) {
            nodeAgent.dispose();
        }
	}
	
	
	/**
	 * Get the design simulation.
	 * @return the design simulation
	 */
	public Simulation getDesignSimulation() {
		if ( _designSimulation == null ) {
			_designSimulation = new OnlineModelSimulator( _sequence, _evaluationNodes ).run();
		}
		
		return _designSimulation;
	}
	
	
	/**
	 * Get the core parameters which pass the specified qualifier test.
	 * @param qualifier the qualifier with which to test the parameters
	 * @return the core parameters which pass the qualifier test
	 */
	public List<CoreParameter> getCoreParameters( final Qualifier qualifier ) {
		final List<CoreParameter> allParameters = _parameterStore.getCoreParameters();
		final List<CoreParameter> parameters = new ArrayList<CoreParameter>();
		
		if ( qualifier == null ) {
			parameters.addAll( allParameters );
		}
		else {
			for ( CoreParameter parameter : allParameters ) {
				if ( qualifier.matches( parameter ) )  parameters.add( parameter );				
			}
		}
		
		return parameters;
	}
	
	
	/**
	 * Get a filtered list of live parameters from the specified live parameters.
	 * @param parameters the parameters from which we will filter;  this parameter list remains unchanged
	 * @param qualifier the qualifier to filter the parameters
	 * @return the filtered parameters
	 */
	static public List<LiveParameter> getFilteredLiveParameters( final List<LiveParameter> parameters, final Qualifier qualifier ) {
		final List<LiveParameter> filteredParameters = new ArrayList<LiveParameter>( parameters.size() );
		
		if ( qualifier == null ) {
			filteredParameters.addAll( parameters );
		}
		else {
			for ( LiveParameter parameter : parameters ) {
				if ( qualifier.matches( parameter ) )  filteredParameters.add( parameter );				
			}
		}
		
		return filteredParameters;
	}
	
	
	/**
	 * Get the parameters which pass the specified qualifier test.
	 * @param qualifier the qualifier with which to test the parameters
	 * @return the parameters which pass the qualifier test
	 */
	public List<LiveParameter> getLiveParameters( final Qualifier qualifier ) {
		final List<LiveParameter> allParameters = _parameterStore.getLiveParameters();
		return getFilteredLiveParameters( allParameters, qualifier );
	}
	
	
	/**
	 * Set the entrance probe.
	 * @param probe the new entrance probe
	 */
	public void setEntranceProbe( final Probe<?> probe ) {
		if ( _entranceProbe != probe ) {
			_entranceProbe = probe;			
		}
		
		if ( _optimizer != null ) {
			_optimizer.setEntranceProbe( probe );
		}
		
		_eventProxy.entranceProbeChanged( this, probe );
	}
	
	
	/**
	 * Get the entrance probe.
	 * @return the entrance probe
	 */
	public Probe<?> getEntranceProbe() {
		return _entranceProbe;
	}
	
	
	/**
	 * Set the sequence.
	 * @param sequence the sequence that was set.
	 */
	public void setSequence( final AcceleratorSeq sequence ) {
		if ( sequence == _sequence )  return;	// nothing to do
		
		disposeNodeAgents();	// dispose of the current agents
		
		_entranceProbe = null;
		_designSimulation = null;
		_parameterStore.clear();
		final List<NodeAgent> nodeAgents = ( sequence != null ) ? generateNodeAgents( sequence, _parameterStore ) : Collections.<NodeAgent>emptyList();
		
		final List<LiveParameter> parameters = new ArrayList<LiveParameter>();
//		final Iterator<NodeAgent> nodeIter = nodeAgents.iterator();
//		while ( nodeIter.hasNext() ) {
//			final NodeAgent agent = (NodeAgent)nodeIter.next();
//			parameters.addAll( agent.getLiveParameters() );
//		}
        
        for(NodeAgent nodeAgent : nodeAgents) {
            parameters.addAll( nodeAgent.getLiveParameters() );
        }
		
		setOptimizer( null );
		
		_nodeAgents = nodeAgents;
		_sequence = sequence;
		_entranceProbe = ( sequence != null ) ? OnlineModelSimulator.getDefaultProbe( sequence ) : null;
		setEvaluationRange( 0.0, ( sequence != null ) ? sequence.getLength() : 0.0 );
		
		Logger.getLogger( "global" ).log( Level.INFO, "New sequence set:  " + sequence );
		
		_eventProxy.sequenceChanged( this, sequence, nodeAgents, _parameterStore.getLiveParameters() );
	}
	
	
	/**
	 * Get the position range of evaluation nodes.
	 * @return an array representing the first and last positions of evaluation nodes
	 */
	public double[] getEvaluationRange() {
		return _evaluationRange;
	}
	
	
	/**
	 * Set the evaluation range which determines the evaluation nodes.
	 * @param firstPosition the first postion to include in the evaluation node range
	 * @param lastPosition the last position to include in the evaluation node range
	 */
	public void setEvaluationRange( final double firstPosition, final double lastPosition ) {		
		_designSimulation = null;
		
		populateEvaluationNodes( firstPosition, lastPosition );
		
		_evaluationRange[0] = firstPosition;
		_evaluationRange[1] = lastPosition;
		
		_eventProxy.evaluationNodesChanged( this, _evaluationRange, _evaluationNodes );
	}
	
	
	/** Populate the nodes that will be used for evaluating the corresponding probe states. */
	protected void populateEvaluationNodes( final double firstPosition, final double lastPosition ) {
		if ( _sequence != null ) {
			final OrTypeQualifier qualifier = new OrTypeQualifier().or( Quadrupole.s_strType ).or( Bend.s_strType ).or( RfCavity.s_strType);
			final List<AcceleratorNode> testNodes = _sequence.getNodesWithQualifier( qualifier );
			int fromIndex = 0;
			int toIndex = testNodes.size();
			final int NUM_NODES = testNodes.size();
			for ( int index = 0 ; index < NUM_NODES ; index++ ) {
				final AcceleratorNode node = testNodes.get( index );
				final double position = _sequence.getPosition( node );
				if ( position < firstPosition ) {
					fromIndex = index + 1;
				}
				else if ( position > lastPosition ) {
					toIndex = index;
					break;
				}
			}
			
			final List<AcceleratorNode> evaluationNodes = testNodes.subList( fromIndex, toIndex );
			
			if ( fromIndex < NUM_NODES && toIndex <= NUM_NODES ) {
				if ( _optimizer != null ) {
					_optimizer.setEvaluationNodes( evaluationNodes );
				}
				
				_evaluationNodes.clear();
				_evaluationNodes.addAll( evaluationNodes );				
			}
		}
	}
	
	
	/**
	 * Load the node agents for the specified sequence.
	 * @param sequence the sequence for which to generate node agents.
	 * @return the list of node agents for the sequence.
	 */
	protected List<NodeAgent> generateNodeAgents( final AcceleratorSeq sequence, final ParameterStore parameterStore ) {
		final List<NodeAgent> agents = new ArrayList<NodeAgent>();
		final Map<String,NodeAgent> agentTable = new HashMap<String,NodeAgent>();
		
		// get electromagnet quads with good status
		final TypeQualifier quadQualifier = new AndTypeQualifier().and( QualifierFactory.getStatusQualifier( true ) ).and( Quadrupole.s_strType ).and( new NotTypeQualifier( PermQuadrupole.s_strType ) );
		final List<Quadrupole> quadrupoles = sequence.getAllInclusiveNodesWithQualifier( quadQualifier );
        
        for(Quadrupole quadrupole : quadrupoles) {
            final QuadAgent agent = new QuadAgent( sequence, quadrupole, parameterStore );
			agents.add( agent );
			agentTable.put( quadrupole.getId(), agent );
        }
        
//		final Iterator quadrupoleIter = quadrupoles.iterator();
//		while ( quadrupoleIter.hasNext() ) {
//			final Quadrupole quadrupole = (Quadrupole)quadrupoleIter.next();
//			final QuadAgent agent = new QuadAgent( sequence, quadrupole, parameterStore );
//			agents.add( agent );
//			agentTable.put( quadrupole.getId(), agent );
//		}
		
		// get bend magnets with good status
		final TypeQualifier bendQualifier = new AndTypeQualifier().and( QualifierFactory.getStatusQualifier( true ) ).and( Bend.s_strType );
		final List<Bend> bends = sequence.getAllInclusiveNodesWithQualifier( bendQualifier );
		
        for(Bend bend : bends) {
            final BendAgent agent = new BendAgent( sequence, bend, parameterStore );
			agents.add( agent );
			agentTable.put( bend.getId(), agent );
        }
        
//        final Iterator bendIter = bends.iterator();
//		while ( bendIter.hasNext() ) {
//			final Bend bend = (Bend)bendIter.next();
//			final BendAgent agent = new BendAgent( sequence, bend, parameterStore );
//			agents.add( agent );
//			agentTable.put( bend.getId(), agent );
//		}
		
		// get dipole corrector magnets with good status
		final TypeQualifier dipoleCorrectorQualifier = new AndTypeQualifier().and( QualifierFactory.getStatusQualifier( true ) ).and( OrTypeQualifier.qualifierForKinds( VDipoleCorr.s_strType, HDipoleCorr.s_strType ) );
		final List<Dipole> correctors = sequence.getAllInclusiveNodesWithQualifier( dipoleCorrectorQualifier );
        for ( final Dipole corrector : correctors ) {
			final DipoleCorrectorAgent agent = new DipoleCorrectorAgent( sequence, corrector, parameterStore );
			agents.add( agent );
			agentTable.put( corrector.getId(), agent );
		}
		
		// get RF cavities with good status
		final List<RfCavity> cavities = sequence.getAllInclusiveNodesWithQualifier( new AndTypeQualifier().and( new KindQualifier( RfCavity.s_strType ) ).and( QualifierFactory.getStatusQualifier( true ) ) );
		
        for(RfCavity cavity : cavities) {
            RFCavityAgent agent = new RFCavityAgent( sequence, cavity, parameterStore );
			agents.add( agent );
			agentTable.put( cavity.getId(), agent );
        }
        
//        final Iterator cavityIter = cavities.iterator();
//		while ( cavityIter.hasNext() ) {
//			final RfCavity cavity = (RfCavity)cavityIter.next();
//			RFCavityAgent agent = new RFCavityAgent( sequence, cavity, parameterStore );
//			agents.add( agent );
//			agentTable.put( cavity.getId(), agent );
//		}
		
		_nodeAgentTable = agentTable;
		
		return agents;
	}
	
	
	/**
	 * Get the current optimizer.
	 * @return the optimizer if it exists or a new optimizer if none already exists or null if the sequence is unspecified
	 */
	public OpticsOptimizer getOptimizer() {
		if ( _optimizer == null ) {
			if ( _sequence != null ) {
				setOptimizer( new OpticsOptimizer( _sequence, _evaluationNodes, _entranceProbe, _parameterStore ) );
			}
			else {
				return null;
			}
		}
		
		return _optimizer;
	}
	
	
	/**
	 * Set the optimizer to the one specified.
	 * @param optimizer the new optimizer
	 */
	public void setOptimizer( final OpticsOptimizer optimizer ) {
		if ( _optimizer != null ) {
			_optimizer.removeOpticsOptimizerListener( this );
		}
		
		_optimizer = optimizer;
		
		if ( optimizer != null ) {
			optimizer.addOpticsOptimizerListener( this );
		}
	}
	
	
	/**
	 * Upload initial value settings to the accelerator.
	 * @return the number of settings uploaded and the number requested.
	 */
	static public int[] uploadInitialValues( final List<LiveParameter> parameters ) {		
		int numUploaded = 0;
		for ( LiveParameter parameter : parameters ) {
			numUploaded += parameter.uploadInitialValue() ? 1 : 0;
		}
		xal.ca.Channel.pendEvent( 2.0 );
		xal.ca.Channel.flushIO();
		
		return new int[] { numUploaded, parameters.size() };
	}
	
	
	/**
	 * Load the settings from the settings map into the specified parameters.
	 * @param settingsMap table of settings keyed by the node name, and the value is a map of value and limits
	 * @param parameters the parameters to modify
	 */
	public void loadCustomSettings( final Map<String, Map<String, Object>> settingsMap, final List<LiveParameter> parameters ) {
		final Map<String, LiveParameter> parameterTable = new HashMap<String, LiveParameter>();
		for ( LiveParameter parameter : parameters ) {
			parameterTable.put( parameter.getNode().getId(), parameter );
		}
		
        
        for( Map.Entry<String, Map<String, Object>> settingsEntry : settingsMap.entrySet() ) {
            final String name = settingsEntry.getKey();
			final LiveParameter parameter = parameterTable.get( name );
			if ( parameter != null ) {
				final Map<String, Object> settings = settingsEntry.getValue();
				final Double value = (Double)settings.get( "value" );
				final double[] limits = (double[])settings.get( "limits" );
				if ( value != null ) {
					parameter.setCustomValue( value.doubleValue() );
				}
				if ( limits != null ) {
					parameter.setCustomLimits( limits );
				}
			}
        }
        
//		final Iterator<Set<Map.Entry<String, Double>>> settingsIter = settingsMap.entrySet().iterator();
//		while ( settingsIter.hasNext() ) {
//			final Map.Entry settingsEntry = (Map.Entry)settingsIter.next();
//			final String name = (String)settingsEntry.getKey();
//			final LiveParameter parameter = parameterTable.get( name );
//			if ( parameter != null ) {
//				final Map<String, Object> settings = settingsEntry.getValue();
//				final Double value = (Double)settings.get( "value" );
//				final double[] limits = (double[])settings.get( "limits" );
//				if ( value != null ) {
//					parameter.setCustomValue( value.doubleValue() );
//				}
//				if ( limits != null ) {
//					parameter.setCustomLimits( limits );
//				}
//			}
//		}
	}
	
	
	/** Freeze the parameters of a cavity if its amplitude is zero. */
	public void freezeParametersOfDisabledCavities() {
        final Iterator<NodeAgent> cavityIter = getNodeAgentsOfType( RfCavity.s_strType ).iterator();
		while ( cavityIter.hasNext() ) {
			final RFCavityAgent cavityAgent = (RFCavityAgent)cavityIter.next();
			final LiveParameter amplitudeParam = cavityAgent.getAmplitudeParameter();
			if ( amplitudeParam.getInitialValue() == 0.0 ) {
				amplitudeParam.setIsVariable( false );
				cavityAgent.getPhaseParameter().setIsVariable( false );
			}
		}
	}
	
	
	/** Import into the custom values, the optimal values found in the last optimization run. */
	public void importOptimalValues() {
		if ( _optimizer != null ) {
			_optimizer.copyOptimalToCustomValues();
			Logger.getLogger( "global" ).log( Level.INFO, "Optimal values imported." );
		}
	}
	
	
	/**
	 * Write the model input parameters.
	 * @param outputURL the URL to which the model input parameters will be written
	 */
	public void exportModelInputParameters( final java.net.URL outputURL ) {
		System.out.println( "Exporting model input parameters..." );
		final Accelerator accelerator = _sequence.getAccelerator();
		final EditContext editContext = new EditContext();	// create a new edit context so we don't disturb the original one
        final String PARAM_GROUP = "modelparams";
		editContext.importTablesFromContext( accelerator.editContext(), PARAM_GROUP );	// perform a deep copy for the model param tables
		
		// here we need to run the online model with evaluation at cavities and markers
        final TypeQualifier evaluationQualifier = OrTypeQualifier.qualifierForKinds( RfCavity.s_strType, Marker.s_strType );
        final List<AcceleratorNode> evaluationNodes = _sequence.getAllNodesWithQualifier( evaluationQualifier );
		final OnlineModelSimulator simulator = new OnlineModelSimulator( _sequence, evaluationNodes, _entranceProbe );
		simulator.applyTrackerUpdatePolicy( xal.model.alg.Tracker.UPDATE_ENTRANCE );
		final Simulation simulation = runOnlineModelSimulationWithCurrentValues( simulator );

		// update the twiss and location parameters
        try {
            // get the known species for use later in location table
            final DataTable speciesTable = editContext.getTable( "species" );
            final List<GenericRecord> speciesRecords = speciesTable.getRecords( new SortOrdering( "name" ) );
            
            // replace the twiss table with the one we will generate
            final DataTable oldTwissTable = editContext.getTable( "twiss" );
            editContext.remove( oldTwissTable );
            final DataTable twissTable = new DataTable( "twiss", oldTwissTable.attributes() );
            editContext.addTableToGroup( twissTable, PARAM_GROUP );
            
            // replace the location table with the one we will generate
            final DataTable oldLocationTable = editContext.getTable( "location" );
            editContext.remove( oldLocationTable );
            final DataTable locationTable = new DataTable( "location", oldLocationTable.attributes() );
            editContext.addTableToGroup( locationTable, PARAM_GROUP );
            
            // get the values we need from the simulation
            final String[] evaluationNodeIDs = simulation.getEvaluationNodeIDs();
            final int evaluationNodeCount = evaluationNodeIDs.length;
            final double[][] betas = simulation.getBeta();
            final double[][] alphas = simulation.getAlpha();
            final double[][] emittances = simulation.getEmittance();
            final double[] kineticEnergies = simulation.getKineticEnergy();
            final double[] restEnergies = simulation.getRestEnergy();
            final double[] speciesCharges = simulation.getSpeciesCharge();
            final String[] coordinates = new String[] { "x", "y", "z" };            
            
            final String SEQUENCE_MARKER_PREFIX = "Begin_Of_";      // identifies the beginning of a sequence
            
            // for each evaluated node, generate the model parameters
            for ( int nodeIndex = 0 ; nodeIndex < evaluationNodeCount ; nodeIndex++ ) {
                final String nodeID = evaluationNodeIDs[nodeIndex];
                // sequences should be identified by ID, so we need to strip the "Begin_Of_" prefix
                final String nodeName = nodeID.startsWith( SEQUENCE_MARKER_PREFIX ) ? nodeID.substring( SEQUENCE_MARKER_PREFIX.length() ) : nodeID;
                // generate the twiss parameters for each coordinate and populate the twiss table
                for ( int planeIndex = 0 ; planeIndex < 3 ; planeIndex++ ) {
                    // get the twiss parameters for the current node and coordinate
                    final String coordinate = coordinates[planeIndex];
                    final double beta = betas[planeIndex][nodeIndex];
                    final double alpha = alphas[planeIndex][nodeIndex];
                    final double emittance = emittances[planeIndex][nodeIndex];
                    
                    // create a new twiss record and populate it with the twiss parameters
                    final GenericRecord twissRecord = new GenericRecord( twissTable );
                    twissRecord.setValueForKey( nodeName, "name" );
                    twissRecord.setValueForKey( coordinate, "coordinate" );
                    twissRecord.setValueForKey( alpha, "alpha" );
                    twissRecord.setValueForKey( beta, "beta" );
                    twissRecord.setValueForKey( emittance, "emittance" );
                    twissTable.add( twissRecord );                    
                }
                // generate the location parameters and populate the location table
                final double kineticEnergy = CONVERT_MEV_TO_EV * kineticEnergies[nodeIndex];    // energy in eV
                final double restEnergy = CONVERT_MEV_TO_EV * restEnergies[nodeIndex];          // energy in eV
                final double speciesCharge = speciesCharges[nodeIndex];                         // charge in units of positive electron charge
                // lookup the species name that corresponds to the charge and energy at the current element (could change due to stripping)
                final String speciesName = findSpeciesName( speciesRecords, speciesCharge, restEnergy );
                final String speciesID = speciesName != null ? speciesName : "Unknown";
                
                // create a new location record and populate it with the location parameters
                final GenericRecord locationRecord = new GenericRecord( locationTable );
                locationRecord.setValueForKey( nodeName, "name" );
                locationRecord.setValueForKey( kineticEnergy, "W" );
                locationRecord.setValueForKey( speciesID, "species" );
                locationTable.add( locationRecord );
            }
            
            // publish the new table group to the specified URL
            XmlTableIO.writeTableGroupToUrl( editContext, PARAM_GROUP, outputURL.toString() );
        }
        catch( Exception exception ) {
            exception.printStackTrace();
            throw( new RuntimeException( exception ) );
        }
	}
    
    
    /** find the species which best matches the charge and mass or null if there is no match */
    private static String findSpeciesName( final List<GenericRecord> speciesRecords, final double charge, final double mass ) {
        final double tolerance = 1e-6;
        for ( final GenericRecord record : speciesRecords ) {
            final double recordCharge = record.doubleValueForKey( "charge" );
            final double recordMass = record.doubleValueForKey( "mass" );
            
            if ( Math.abs( recordCharge - charge ) < tolerance * Math.abs( charge ) ) {
                if ( Math.abs( recordMass - mass ) < tolerance * Math.abs( mass ) ) {
                    return record.stringValueForKey( "name" );
                }
            }
        }
        return null;
    }
	
	
	/**
	 * Write the initial parameter values.
	 * @param writer the writer to which the parameters should be written
	 */
	public void exportInitialParameters( final java.io.Writer writer ) throws java.io.IOException {
		writer.write( "# Current parameters generated by the Energy Management Application \n" );
		writer.write( "\nExported:  " + new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" ).format( new Date() ) + "\n" );
		
		exportParameters( writer, Collections.<String, Double>emptyMap() );
	}
	
	
	/** Determine whether optimal results can be exported */
	public boolean canExportOptimalResults() {
		try {
			final OpticsOptimizer optimizer = getOptimizer();
			return optimizer != null && optimizer.canExportObjectiveResults();
		}
		catch ( Exception exception ) {
			return false;
		}
	}
	
	
	/**
	 * Write the optimization results to the specified writer.
	 * @param writer the writer to which the results should be written
	 */
	public void exportOptimalResults( final java.io.Writer writer ) throws java.io.IOException {
		writer.write( "# Optimal Results generated by the Energy Management Application \n" );
		writer.write( "\nExported:  " + new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" ).format( new Date() ) + "\n" );
		
		getOptimizer().exportObjectiveResults( writer );
		exportParameters( writer, getOptimizer().getBestVariableValues() );
	}
	
	
	/**
	 * Write the parameter values to the specified writer.
	 * @param writer the writer to which to write the parameter values
	 * @param valueMap the map of core parameter value overrides keyed by core parameter name
	 */
	public void exportParameters( final java.io.Writer writer, final Map<String, Double> valueMap ) throws java.io.IOException {
		QuadAgent.exportParameterHeader( writer );
		exportParameters( writer, getNodeAgentsOfType( Quadrupole.s_strType ), valueMap );
		RFCavityAgent.exportParameterHeader( writer );
		exportParameters( writer, getNodeAgentsOfType( RfCavity.s_strType ), valueMap );
	}
	
	
	/**
	 * Write the parameter values to the specified writer.
	 * @param writer the writer to which to write the parameter values
	 * @param nodeAgents the list of nodeAgents to write
	 * @param valueMap the map of core parameter value overrides keyed by core parameter name
	 */
	public void exportParameters( final java.io.Writer writer, final List<NodeAgent> nodeAgents, final Map<String, Double> valueMap ) throws java.io.IOException {
        
        for(NodeAgent nodeAgent : nodeAgents) {
            nodeAgent.exportParameters( writer, valueMap );
        }
        
//		final Iterator nodeIter = nodeAgents.iterator();
//		while ( nodeIter.hasNext() ) {
//			final NodeAgent nodeAgent = (NodeAgent)nodeIter.next();
//			nodeAgent.exportParameters( writer, valueMap );
//		}
	}
	
	
	/** Determine whether twiss parameters can be exported */
	public boolean canExportTwiss() {
		return canExportOptimalResults();
	}
	
	
	/**
	 * Write the twiss parameters for the most recent evaluation or optimization run.
	 * @param writer the writer to which the results should be written
	 */
	public void exportTwiss( final java.io.Writer writer ) throws java.io.IOException {
		final Probe<?> probe = getEntranceProbe();
		
		writer.write( "# Generator:  Energy Manager \n" );
		writer.write( "# Twiss Exported:  " + new SimpleDateFormat( "MMM dd, yyyy HH:mm:ss" ).format( new Date() ) + "\n" );
		writer.write( "# Initial Kinetic Energy:  " + probe.getKineticEnergy() * 1.0e-6 + "\n" );
		writer.write( "# Units:  Position: meters, Energy:  MeV, Beta: meters, Eta: meters \n" );
		writer.write( "# Element	Position	Kinetic Energy	Beta x	Beta y	Beta z	Eta x	Eta y\n" );
		writer.write( "\n" );
		
		final Simulation simulation = getOptimizer().getBestSimulation();
		if ( simulation != null ) {
			final ProbeState<?>[] states = simulation.getStates();
			final double[] positions = simulation.getPositions();
			final double[] kineticEnergy = simulation.getKineticEnergy();
			final double[][] beta = simulation.getBeta();
			final double[][] eta = simulation.getEta();
			for ( int index = 0 ; index < positions.length ; index++ ) {
				writer.write( states[index].getElementId() + "\t" + positions[index] + "\t" + kineticEnergy[index] );
				for ( int bindex = 0 ; bindex < 3 ; bindex ++ ) {
					writer.write( "\t" + beta[bindex][index] );
				}
				for ( int eindex = 0 ; eindex < 2 ; eindex ++ ) {
					writer.write( "\t" + eta[eindex][index] );
				}
				writer.write( "\n" );
			}
		}
		else {
			throw new RuntimeException( "No simulation available." );
		}
	}
	
	
	/**
	 * Get node agents whose node is of the specified type.
	 * @param type the desired node type
	 * @return the node agents corresponding to the specified type
	 */
	public List<NodeAgent> getNodeAgentsOfType( final String type ) {
		final List<NodeAgent> filteredNodes = new ArrayList<NodeAgent>();
		
        for(NodeAgent nodeAgent : _nodeAgents) {
            if ( nodeAgent.getNode().isKindOf( type ) ) {
				filteredNodes.add( nodeAgent );
			}
        }
//        
//        final Iterator nodeIter = _nodeAgents.iterator();
//		while ( nodeIter.hasNext() ) {
//			final NodeAgent nodeAgent = (NodeAgent)nodeIter.next();
//			if ( nodeAgent.getNode().isKindOf( type ) ) {
//				filteredNodes.add( nodeAgent );
//			}
//		}
		
		return filteredNodes;
	}


	/** Run the online model simulation given the current custom settings. */
	private Simulation runOnlineModelSimulationWithCurrentValues( final OnlineModelSimulator simulator ) {
		final List<CoreParameter> customParameters = new ArrayList<CoreParameter>();

		final List<CoreParameter> coreParameters = _parameterStore.getCoreParameters();
		for ( CoreParameter parameter : coreParameters ) {
			if ( parameter.getActiveSource() != CoreParameter.DESIGN_SOURCE ) {
				customParameters.add( parameter );
			}
		}

		return runOnlineModelSimulationWithCustomValues( simulator, customParameters );
	}


	/** Run the online model simulation given the specified custom settings. */
	private Simulation runOnlineModelSimulationWithCustomValues( final OnlineModelSimulator simulator, final List<CoreParameter> customParameters ) {
		try {
			simulator.setFixedCustomParameterValues( customParameters );
			return simulator.run();
		}
		finally {
			simulator.cleanup( Collections.<Variable>emptyList(), customParameters );
		}
	}

	
	/** Run the online model simulation given the current settings, sequence, evaluation nodes and entrance probe. */
	public Simulation runOnlineModelSimulation() {
		final OnlineModelSimulator simulator = new OnlineModelSimulator( _sequence, _evaluationNodes, _entranceProbe );
		return runOnlineModelSimulationWithCurrentValues( simulator );
	}
	
	
	/** apply the best guess */
	public void applyBestGuess() {
		guessRFPhaseToPreserveLongitudinalFocusing();			
		scaleMagnetFieldsToEnergy();
		
		Logger.getLogger( "global" ).log( Level.INFO, "Best guess of RF phases and quadrupole fields have been applied." );
	}
	
	
	/** Scale the design magnet fields to the energy based on the current RF settings. */
	public void scaleMagnetFieldsToEnergy() {
		final Qualifier qualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, ElectromagnetFieldAdaptor.NAME );
		final List<LiveParameter> magnetParameters = getLiveParameters( qualifier );
		
		scaleMagnetFieldsToEnergy( magnetParameters );
	}
	
	
	/** Scale the design magnet fields of the specified magnets to the energy based on the current RF settings. */
	public void scaleMagnetFieldsToEnergy( final List<LiveParameter> magnetParameters ) {
		final Simulation simulation = runOnlineModelSimulation();
		final Trajectory<?> trajectory = simulation.getTrajectory();
		final Trajectory<?> designTrajectory = getDesignSimulation().getTrajectory();
		
		for ( LiveParameter parameter : magnetParameters ) {
			final ElectromagnetAgent agent = (ElectromagnetAgent)parameter.getNodeAgent();
			final String nodeID = agent.getNode().getId();
			final ProbeState<?> state = trajectory.stateForElement( nodeID );
			final ProbeState<?> designState = designTrajectory.stateForElement( nodeID );
			agent.preserveDesignInfluence( state.getKineticEnergy(), designState.getKineticEnergy(), state.getSpeciesRestEnergy() );
		}
	}
	
	
	/**
	 * Scale magnetic fields for energy change assuming no acceleration within the sequence.
	 * @param source parameter source of the initial field
	 * @param initialKineticEnergy the initial kinetic energy
	 * @param targetKineticEnergy the kinetic energy we want
	 */
	public void scaleMagneticFieldsForEnergyChange( final String source, final double initialKineticEnergy, final double targetKineticEnergy ) {
		final double restEnergy = getEntranceProbe().getSpeciesRestEnergy();
		final double initialEnergy = initialKineticEnergy + restEnergy;
		final double targetEnergy = targetKineticEnergy + restEnergy;
		final double initialBetaGamma = Math.sqrt( Math.pow( initialEnergy / restEnergy, 2 ) - 1.0 );
		final double targetBetaGamma = Math.sqrt( Math.pow( targetEnergy / restEnergy, 2 ) - 1.0 );
		final double scale = targetBetaGamma / initialBetaGamma;
		
		final Qualifier qualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, ElectromagnetFieldAdaptor.NAME );
		final List<LiveParameter> magnetParameters = getLiveParameters( qualifier );
		
		// since parameters can share common core parameters, we need to handle the situation when we attempt to scale the custom value
		// without compounding for each core parameter.
		final Map<LiveParameter,Double> valueMap = new HashMap<LiveParameter,Double>();
		for ( LiveParameter parameter : magnetParameters ) {
			final Double initialField = (Double)parameter.valueForKey( source );
			if ( initialField != null ) {
				final double targetField = scale * initialField.doubleValue();
				valueMap.put( parameter, targetField );
			}
		}
		for ( LiveParameter parameter : magnetParameters ) {
			final Double field = valueMap.get( parameter );
			if ( field != null ) {
				parameter.setCustomValue( field );
			}
		}
	}
	
	
	/**
	 * Make an educated guess for the RF phase to preserve longitudinal focusing.
	 */
	public void guessRFPhaseToPreserveLongitudinalFocusing() {
		final Qualifier qualifier = new KeyValueQualifier( LiveParameter.NAME_KEY, RFCavityAgent.PHASE_ADAPTOR.getName() );
        
        for(LiveParameter parameter : getLiveParameters( qualifier )) {
            final RFCavityAgent agent = (RFCavityAgent)parameter.getNodeAgent();
			agent.preserveDesignFocusingWithPhase();
        }
        
//		final Iterator paramIter = getLiveParameters( qualifier ).iterator();
//		while ( paramIter.hasNext() ) {
//			final LiveParameter parameter = (LiveParameter)paramIter.next();
//			final RFCavityAgent agent = (RFCavityAgent)parameter.getNodeAgent();
//			agent.preserveDesignFocusingWithPhase();
//		}		
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "EnergyManagerModel";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "EntranceKineticEnergy" ) ) {
			final double kineticEnergy = adaptor.doubleValue( "EntranceKineticEnergy" );
			_entranceProbe.setKineticEnergy( kineticEnergy );
		}
		
		if ( adaptor.hasAttribute( "firstEvaluationPosition") ) {
			final double firstPosition = adaptor.doubleValue( "firstEvaluationPosition" );
			final double lastPosition = adaptor.doubleValue( "lastEvaluationPosition" );
			setEvaluationRange( firstPosition, lastPosition );
		}
		
		final DataAdaptor probeAdaptor = adaptor.childAdaptor( "probe" );
		if ( probeAdaptor != null ) {
			_entranceProbe.load( probeAdaptor );
			_entranceProbe.reset();
		}
		
		final DataAdaptor storeAdaptor = adaptor.childAdaptor( _parameterStore.dataLabel() );
		_parameterStore.update( storeAdaptor );
		
		final DataAdaptor optimizerAdaptor = adaptor.childAdaptor( OpticsOptimizer.DATA_LABEL );
		if ( optimizerAdaptor != null ) {
			getOptimizer().update( optimizerAdaptor );
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		if ( _entranceProbe != null ) {
			_entranceProbe.save( adaptor );			
		}
		
		adaptor.setValue( "firstEvaluationPosition", _evaluationRange[0] );
		adaptor.setValue( "lastEvaluationPosition", _evaluationRange[1] );
		
		final DataAdaptor storeAdaptor = adaptor.createChild( _parameterStore.dataLabel() );
		_parameterStore.write( storeAdaptor );
		
		if ( _optimizer != null ) {
			final DataAdaptor optimizerAdaptor = adaptor.createChild( _optimizer.dataLabel() );
			_optimizer.write( optimizerAdaptor );
		}
	}
	
	
	/**
	 * Event indicating that the parameter store's parameters have been cleared.
	 * @param store the source of the event.
	 */
	public void parametersCleared( final ParameterStore store ) {}
	
	
	/**
	 * Event indicating that a live parameter has been added.
	 * @param store the source of the event.
	 * @param parameter the parameter which has been added.
	 */
	public void liveParameterAdded( final ParameterStore store, final LiveParameter parameter ) {}
	
	
	/**
	 * Event indicating that a core parameter has been added.
	 * @param store the source of the event.
	 * @param parameter the parameter which has been added.
	 */
	public void coreParameterAdded( final ParameterStore store, final CoreParameter parameter ) {}
	
	
	/**
	 * Event indicating that a live parameter has been modified.
	 * @param store the source of the event.
	 * @param parameter the parameter which has changed.
	 */
	public void liveParameterModified( final ParameterStore store, final LiveParameter parameter ) {
		_eventProxy.liveParameterModified( this, parameter );
	}
	
	
	/**
	 * Event indicating that a new trial has been evaluated.
	 * @param optimizer the optimizer producing the event
	 * @param trial the trial which was scored
	 */
	public void trialScored( final OpticsOptimizer optimizer, final xal.extension.solver.Trial trial ) {}
	
	
	/**
	 * Event indicating that a new optimal solution has been found
	 * @param optimizer the optimizer producing the event
	 * @param solution the new optimal solution
	 */
	public void newOptimalSolution( final OpticsOptimizer optimizer, final xal.extension.solver.Trial solution ) {
		_eventProxy.newOptimalSolutionFound( this, optimizer );
	}
	
	
	/**
	 * Event indicating that an optimization run has been started.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStarted( final OpticsOptimizer optimizer ) {
		Logger.getLogger( "global" ).log( Level.INFO, "Optimizer Started." );
		_eventProxy.optimizerStarted( this, optimizer );
	}
	
	
	/**
	 * Event indicating that an optimization run has stopped.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizationStopped( final OpticsOptimizer optimizer ) {
		Logger.getLogger( "global" ).log( Level.INFO, "Optimizer Stopped." );
	}
	
	
	/**
	 * Event indicating that an optimization run has failed.
	 * @param optimizer the optimizer producing the event
	 * @param exception the exception thrown during optimization
	 */
	public void optimizationFailed( final OpticsOptimizer optimizer, final Exception exception ) {
		Logger.getLogger( "global" ).log( Level.SEVERE, "Optimization failed!", exception );
	}
	
	
	/**
	 * Event indicating that optimizer settings have changed.
	 * @param optimizer the optimizer producing the event
	 */
	public void optimizerSettingsChanged( final OpticsOptimizer optimizer ) {
		_eventProxy.optimizerSettingsChanged( this, optimizer );
	}
}



