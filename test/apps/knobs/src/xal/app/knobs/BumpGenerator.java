//
//  BumpGenerator.java
//  xal
//
//  Created by Thomas Pelaia on 3/9/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;

import java.util.ArrayList;
import java.util.List;

import xal.model.ModelException;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.NodeChannelRef;
import xal.smf.Ring;
import xal.smf.impl.BPM;
import xal.smf.impl.Dipole;
import xal.smf.impl.Electromagnet;
import xal.smf.impl.MagnetMainSupply;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.tools.messaging.MessageCenter;
import xal.tools.beam.calc.*;
import Jama.Matrix;


/** Generates closed bump knobs using the specified number of correctors */
public class BumpGenerator {
	/** message center for dispatching events to registered listeners */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registers listeners */
	final protected BumpGeneratorListener EVENT_PROXY;
	
	/** the knobs model into which we will be putting the bump knobs */
	final protected KnobsModel KNOBS_MODEL;
	
	/** accelerator sequence where we want the knobs */
	final protected AcceleratorSeq _sequence;
	
	/** knob group into which we should add the bumps knobs */
	final protected KnobGroup _knobGroup;
	
	/** plane (horizontal or vertical) adaptor for handling plane specific behavior */
	protected PlaneAdaptor _planeAdaptor;
	
	/** bump shape adaptor for determining the shape of the bump */
	protected BumpShapeAdaptor _bumpShapeAdaptor;
	
	/** number of correctors to use in the bump */
	protected int _elementCount;
	
	/** online model scenario */
	protected Scenario _scenario;
	
	/** online model probe */
	protected Probe<?> _probe;
	
	/** base trajectory */
	protected Trajectory<?> _baseTrajectory;
	
	
	/** lock for synchronizing runs */
	final protected Object RUN_LOCK;
	
	/** number of bumps to make */
	protected int _bumpCount;
	
	/** determines whether or not to use a live model */
	protected boolean _usesLiveModel;
	
	/** number of bumps processed whether they were successfully made or not */
	volatile protected int _processedBumpCount;
	
	/** indicates whether the generator should cancel generating bumps */
	volatile protected boolean _shouldCancelBumpGeneration;
	
	
	/** Constructor */
	public BumpGenerator( final KnobsModel model, final KnobGroup group ) {
		MESSAGE_CENTER = new MessageCenter( "Bump Generator" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BumpGeneratorListener.class );
		
		RUN_LOCK = new Object();
		
		KNOBS_MODEL = model;
		_knobGroup = group;
		_sequence = model.getSequence();
		_usesLiveModel = false;
		_bumpShapeAdaptor = BumpShapeAdaptor.getBumpOffsetAdaptor();
	}
	
	
	/** register listener for receiving bump generator events from this source */
	public void addBumpGeneratorListener( final BumpGeneratorListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BumpGeneratorListener.class );
	}
	
	
	/** remove the listener from receiving bump generator events from this source */
	public void removeBumpGeneratorListener( final BumpGeneratorListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BumpGeneratorListener.class );
	}
	
	
	/** set whether or not to use a live model */
	public void setUsesLiveModel( final boolean usesLiveModel ) {
		synchronized( RUN_LOCK ) {
			_usesLiveModel = usesLiveModel;
		}
	}
	
	
	/** determine whether or not we are using the live model */
	public boolean usesLiveModel() {
		return _usesLiveModel;
	}
	
	
	/** set the plane adaptor */
	public void setPlaneAdaptor( final PlaneAdaptor planeAdaptor ) {
		synchronized( RUN_LOCK ) {
			_planeAdaptor = planeAdaptor;
		}
	}
	
	
	/** set the bump shape adaptor */
	public void setBumpShapeAdaptor( final BumpShapeAdaptor shapeAdaptor ) {
		synchronized( RUN_LOCK ) {
			_bumpShapeAdaptor = shapeAdaptor;
		}
	}
	
	
	/** get the bump shape adaptor */
	public BumpShapeAdaptor getBumpShapeAdaptor() {
		return _bumpShapeAdaptor;
	}
		
	
	/** set the number of bumps elements */
	public void setBumpElementCount( final int count ) {
		synchronized( RUN_LOCK ) {
			_elementCount = count;
		}
	}
	
	
	/** get the number of bumps to make */
	public int getBumpCount() {
		return _bumpCount;
	}
	
	
	/** get the number of bumps processed whether or not the bump was successfully made */
	public int getProcessedBumpCount() {
		return _processedBumpCount;
	}
	
	
	/** set whether the generator should cancel processing bumps */
	public void cancelBumpGeneration() {
		_shouldCancelBumpGeneration = true;
	}
	
	
	/** get correctors near the reference node */
	protected List<Dipole> getCorrectorsNear( final AcceleratorNode node ) {
		final List<Dipole> planeMagnets = _planeAdaptor.getCorrectors( _sequence );
		return nodesSurroundingReference( planeMagnets, node );
	}
	
	
	/** get the nearest nodes by proximity, but making sure that there is at least one node on either side of the reference */
	protected List<Dipole> nodesSurroundingReference( final List<Dipole> magnets, final AcceleratorNode referenceNode ) {
		final AcceleratorSeq sequence = _sequence;
		final int ELEMENT_COUNT = _elementCount;
		
		final List<Dipole> sortedMagnets = new ArrayList<Dipole>( magnets );
		sequence.sortNodesByProximity( sortedMagnets, referenceNode );		// sort magnets by proximity to the reference node
		
		final List<Dipole> bumpMagnets = new ArrayList<Dipole>( ELEMENT_COUNT );
		double lastSign = 2.0;
		double sign_sum = 0.0;
		for ( Dipole magnet : sortedMagnets ) {
			if ( bumpMagnets.size() == ELEMENT_COUNT )  break;
			final double relativePosition = sequence.getShortestRelativePosition( magnet, referenceNode );
			final double sign = Math.signum( relativePosition );
			if ( bumpMagnets.size() < ELEMENT_COUNT - 1 || Math.abs( sign_sum ) != ELEMENT_COUNT -  1 || sign != lastSign ) {
				lastSign = sign;
				sign_sum += sign;
				bumpMagnets.add( magnet );
			}
		}
		
		sequence.sortNodesByRelativePosition( bumpMagnets, referenceNode );	// sort magnets from negative to positive position relative to ref
		return bumpMagnets;
	}
	
	
	/** calculate the base trajectory */
	protected void calculateBaseTrajectory() {
//		final PlaneAdaptor planeAdaptor = _planeAdaptor;
		
		try {
			_probe.reset();
			_scenario.resync();
			_scenario.run();
			_baseTrajectory = _probe.getTrajectory();	
		}
		catch ( ModelException exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Model Exception in base trajectory calculation.", exception );
		}		
	}
	
	
	/** calculate the base orbit */
	protected double[] getBaseOrbit( final AcceleratorNode bumpNode, final AcceleratorNode endNode ) {
		final PlaneAdaptor planeAdaptor = _planeAdaptor;
		
		final Trajectory<?> trajectory = _baseTrajectory;
		final SimResultsAdaptor simulator = new SimpleSimResultsAdaptor( trajectory );
        final ProbeState<?> bumpState = trajectory.statesForElement( bumpNode.getId() ).get(0);
        final ProbeState<?> endState = trajectory.statesForElement( endNode.getId() ).get(0);
		
		return _bumpShapeAdaptor.getOrbit( simulator, planeAdaptor, bumpState, endState, _elementCount );
	}
	
	
	/** calculate the orbit */
	protected double[] calculateResponse( final AcceleratorNode bumpNode, final AcceleratorNode endNode, final Dipole magnet, final double amplitude ) {
		final PlaneAdaptor planeAdaptor = _planeAdaptor;
				
		try {
			final double[] baseOrbit = getBaseOrbit( bumpNode, endNode );
			
			_scenario.setModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD, magnet.toFieldFromCA( amplitude ) );
			_probe.reset();
			
			// if the sequence is linear, we only need to track within the bump
			if ( _sequence.isLinear() ) {
				try {
					final int stopNodeIndex = 1 + _sequence.getIndexOfNode( endNode );
					
					if ( stopNodeIndex < _sequence.getNodeCount() ) {
						final AcceleratorNode stopNode = _sequence.getNodeAt( stopNodeIndex );
						_scenario.setStopNode( stopNode.getId() );
					}
					
					final String startNodeID = _sequence.getIndexOfNode( bumpNode ) < _sequence.getIndexOfNode( magnet ) ? bumpNode.getId() : magnet.getId();
					_scenario.setStartNode( startNodeID );
					final double startKineticEnergy = _baseTrajectory.stateForElement( startNodeID ).getKineticEnergy();
					_probe.setKineticEnergy( startKineticEnergy );
				}
				catch( Exception exception ) {
					_scenario.setStopElement( null );
				}
			}
			
			_scenario.resyncFromCache();
			_scenario.run();
			_scenario.removeModelInput( magnet, ElectromagnetPropertyAccessor.PROPERTY_FIELD );
			final Trajectory<?> trajectory = _probe.getTrajectory();
			final SimResultsAdaptor simulator = new SimpleSimResultsAdaptor( trajectory );

            final ProbeState<?> bumpState = trajectory.statesForElement( bumpNode.getId() ).get(0);
            final ProbeState<?> endState  = trajectory.statesForElement( endNode.getId() ).get(0);
			
			final double[] response = _bumpShapeAdaptor.getOrbit( simulator, planeAdaptor, bumpState, endState, _elementCount );
			// adjust the response to account for the base orbit offset and scale by amplitude to get the response per unit of magnetic field
			for ( int index = 0 ; index < response.length ; index++ ) {
				response[index] = ( response[index] - baseOrbit[index] ) / amplitude;
			}
			
			return response;
		}
		catch ( ModelException exception ) {
			throw new RuntimeException( "Model Exception in orbit calculation.", exception );
		}
	}
	
	
	/** calculate the bump */
	protected double[] calculateBumpFields( final List<Dipole> magnets, final AcceleratorNode targetNode ) {
		final int numMagnets = magnets.size();
		
		final AcceleratorNode endNode = magnets.get( numMagnets - 1 );
				
		// response of each node's orbit to the magnets
		final int orbitSize = _bumpShapeAdaptor.getOrbitSize( _elementCount );
		final Matrix responseMatrix = new Matrix( orbitSize, numMagnets );
		for ( int magIndex = 0 ; magIndex < numMagnets ; magIndex++ ) {
			final double[] response = calculateResponse( targetNode, endNode, magnets.get( magIndex ), 0.01 );
			for ( int nodeIndex = 0 ; nodeIndex < response.length ; nodeIndex++ ) {
				responseMatrix.set( nodeIndex, magIndex, response[nodeIndex] );
			}
		}
		responseMatrix.print( 10, 10 );
		
		// the minimal magnet fields that produce the desired bump are given by:
		// f = R<sup>T</sup>(RR<sup>T</sup>)<sup>-1</sup>b  where f is the field vector, b is the bump vector and R is the response matrix
		final Matrix bumpVector = new Matrix( orbitSize, 1 );		// bump vector ( bumpAmplitude, 0, 0, 0 )
		bumpVector.set( 0, 0, _bumpShapeAdaptor.getAmplitudeScale() );
		final Matrix responseTranspose = responseMatrix.transpose();
		final Matrix fieldVector = responseTranspose.times( ( responseMatrix.times( responseTranspose ) ).inverse() ).times( bumpVector );
		final double[] fields = new double[numMagnets];
		for ( int magIndex = 0 ; magIndex < numMagnets ; magIndex++ ) {
			fields[magIndex] = fieldVector.get( magIndex, 0 );
		}
		
		return fields;
	}
	
	
	/** create a bump knob */
	protected Knob makeBumpKnob( final AcceleratorNode node ) {
		final int elementCount = _elementCount;
		final AcceleratorSeq sequence = _sequence;
		final Accelerator accelerator = sequence.getAccelerator();
		
		final List<Dipole> bumpMagnets = getCorrectorsNear( node );
		if ( bumpMagnets.size() != elementCount )  return null;
		final double[] fields = calculateBumpFields( bumpMagnets, node );
		final String bumpName = elementCount + _planeAdaptor.shortPlaneName() + "bump - " + node.getId();
		final Knob knob = KNOBS_MODEL.createKnobInGroup( _knobGroup, bumpName );
		for ( int magnetIndex = 0 ; magnetIndex < elementCount ; magnetIndex++ ) {
			final KnobElement element = new KnobElement();
			element.setAccelerator( accelerator );
			element.setNodeChannelRef( new NodeChannelRef( bumpMagnets.get( magnetIndex ), MagnetMainSupply.FIELD_SET_HANDLE ) );
			element.setCoefficient( fields[magnetIndex] );
			knob.addElement( element );
		}
		EVENT_PROXY.knobGenerated( this, knob );
		System.out.println( "Bump Knob created:  " + knob );
		return knob;
	}
	
	
	/** create the bumps */
	public void makeBumpKnobs() {
		synchronized( RUN_LOCK ) {
//			final int elementCount = _elementCount;
			final AcceleratorSeq sequence = _sequence;
//			final Accelerator accelerator = sequence.getAccelerator();
			
			final List<AcceleratorNode> electromagnets = sequence.getNodesOfType( Electromagnet.s_strType, true );
			for ( AcceleratorNode electromagnet : electromagnets ) {
				((Electromagnet)electromagnet).setUseFieldReadback( false );
			}
			
			try {
				_shouldCancelBumpGeneration = false;
                try{
                    _probe = sequence instanceof Ring ? ProbeFactory.getTransferMapProbe( sequence, AlgorithmFactory.createTransferMapTracker(sequence) ) : ProbeFactory.getEnvelopeProbe( sequence, AlgorithmFactory.createEnvTrackerAdapt( sequence ) );
                }
                catch ( InstantiationException exception ) {
                    System.err.println( "Instantiation exception creating probe." );
                    exception.printStackTrace();
                }
                
				_scenario = Scenario.newScenarioFor( sequence );
				_scenario.setSynchronizationMode( _usesLiveModel ? Scenario.SYNC_MODE_RF_DESIGN : Scenario.SYNC_MODE_DESIGN );
				_scenario.setProbe( _probe );
				
//				final List<Dipole> magnets = _planeAdaptor.getCorrectors( sequence );
				final List<AcceleratorNode> nodes = sequence.getNodesOfType( BPM.s_strType, true );
				
				calculateBaseTrajectory();
				
				_bumpCount = nodes.size();
				_processedBumpCount = 0;
				final List<Knob> knobs = new ArrayList<Knob>( _bumpCount );
				for ( AcceleratorNode node : nodes ) {
					if ( _shouldCancelBumpGeneration )  break;
					try {
						EVENT_PROXY.willGenerateKnob( this, node );
						final Knob knob = makeBumpKnob( node );
						if ( knob != null )  knobs.add( knob );
					}
					catch( Exception exception ) {
						EVENT_PROXY.knobGeneratorException( this, node, exception );
						System.out.println( "Can't make bump for node:  " + node );
					}
					finally {
						++_processedBumpCount;
					}
				}
				EVENT_PROXY.knobGenerationComplete( this, knobs );
			}
			catch( ModelException exception ) {
				EVENT_PROXY.knobGenerationFailed( this, exception );
			}
		}
	}
}

