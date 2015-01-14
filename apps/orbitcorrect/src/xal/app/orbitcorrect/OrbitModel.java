/*
 * OrbitModel.java
 *
 * Created on Wed Jan 07 14:56:32 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import xal.tools.messaging.MessageCenter;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.MagnetType;
import xal.tools.data.*;
import xal.model.alg.*;
import xal.model.probe.Probe;
import xal.sim.scenario.*;

import java.util.*;


/**
 * OrbitModel manages the instances for monitoring and correcting the orbit.
 * @author  tap
 */
public class OrbitModel implements DataListener {
	/** message center which broadcasts messages to the registered listeners */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards messages to registered listeners */
	final protected OrbitModelListener EVENT_PROXY;
	
	/** beam excursion/orbit adaptor */
	protected final BeamExcursionOrbitAdaptor BEAM_EXCURSION_ORBIT_ADAPTOR;
	
	/** Accelerator sequence whose orbit we wish to monitor */
	protected AcceleratorSeq _sequence;
	
	/** List of all available BPMs in the sequence */
	final protected List<BpmAgent> AVAILABLE_BPM_AGENTS;
	
	/** List of enabled BPMs in the sequence */
	protected List<BpmAgent> _bpmAgents;
	
	/** List of corrector agents */
	protected List<CorrectorAgent> _correctorAgents;
	
	/** List of corrector supplies */
	protected List<CorrectorSupply> _correctorSupplies;
	
	/** Map of corrector supplies keyed by corrector ID */
	protected Map<String,CorrectorSupply> _correctorSupplyMap;
	
	/** orbit sources */
	protected List<OrbitSource> _orbitSources;
	
	/** flattener */
	protected Flattener _flattener;
	
	/** flag indicating whether to monitor beam events to trigger orbit capture */
	private boolean _useBeamEventTrigger;
	
	/** the modification store */
	protected ModificationStore _modificationStore;

	/* Base probe from which others are copied for model calculations. Only copies of it should ever be run. */
	private Probe<?> _baseProbe;
	
	
	/**
	 * Primary OrbitModel Constructor
	 * @param modificationStore the modification store
	 * @param sequence the sequence to model
	 */
	public OrbitModel( final ModificationStore modificationStore, final AcceleratorSeq sequence ) {
		MESSAGE_CENTER = new MessageCenter( "Orbit Model" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, OrbitModelListener.class );
		
		BEAM_EXCURSION_ORBIT_ADAPTOR = new BeamExcursionOrbitAdaptor( sequence, new ArrayList<BpmAgent>() );

		_baseProbe = null;
		_modificationStore = modificationStore;

		AVAILABLE_BPM_AGENTS = new ArrayList<BpmAgent>();
		
		createOrbitSources();
		
		setUseBeamEventTrigger( true );
		setSequence( sequence );
	}
	
	
	/**
	 * OrbitModel Constructor
	 * @param modificationStore the modification store
	 */
	public OrbitModel( final ModificationStore modificationStore ) {
		this( modificationStore, null );
	}
	
	
	/** Dispose of this model's resources */
	public void dispose() {
		setSequence( null );
		_orbitSources = null;
		MESSAGE_CENTER.removeSource( this, OrbitModelListener.class );
		_bpmAgents = null;
		AVAILABLE_BPM_AGENTS.clear();
		_correctorAgents = null;
	}
	
	
	/**
	 * Get this document's modification store.
	 * @return this document's modification store
	 */
	public ModificationStore getModificationStore() {
		return _modificationStore;
	}
    
    
    /** mark the model as having a modification */
    public void postModification() {
        _modificationStore.postModification( this );
    }
	
	
	/**
	 * Get the flattener.
	 * @return a new flattener
	 */
	public Flattener getFlattener() {
		if ( _flattener == null ) {
			_flattener = new Flattener( this );
			_modificationStore.postModification( this );
		}
		return _flattener;
	}


	/** clear the flattener's simulator */
	public void clearFlattenSimulator() {
		final Flattener flattener = _flattener;
		if ( flattener != null ) {
			final MachineSimulator simulator = flattener.getSimulator();
			if ( simulator != null ) {
				simulator.clear();
			}
		}
	}
	
	
	/** Create the model's orbit sources. */
	protected void createOrbitSources() {
		_orbitSources = new ArrayList<OrbitSource>();
		
		addOrbitSource( new LiveOrbitSource( "Live Orbit", _useBeamEventTrigger ) );
	}
	
	
	/**
	 * Add a new OrbitSource to this model's orbit sources.
	 * @param orbitSource the new orbit source to add
	 */
	public void addOrbitSource( final OrbitSource orbitSource ) {
		synchronized( _orbitSources ) {
			if ( !_orbitSources.contains( orbitSource ) ) {
				orbitSource.setSequence( _sequence, _bpmAgents );
				_orbitSources.add( orbitSource );
				EVENT_PROXY.orbitSourceAdded( this, orbitSource );
			}
		}
        
        postModification();
	}
    
    
    /** Add a new orbit source archived in the data adaptor */
    public void addOrbitSourceFromArchive( final DataAdaptor adaptor ) {
        final SnapshotOrbitSource snapshot = SnapshotOrbitSource.getInstance( adaptor, _sequence, _bpmAgents );
        addOrbitSource( snapshot );
    }
	
	
	/**
	 * Remove an OrbitSource from this model's orbit sources.
	 * @param orbitSource the orbit source to remove.
	 */
	public void removeOrbitSource( final OrbitSource orbitSource ) {
		synchronized( _orbitSources ) {
			_orbitSources.remove( orbitSource );
			EVENT_PROXY.orbitSourceRemoved( this, orbitSource );
		}
        
        postModification();
	}
	
	
	/**
	 * Add the listener as a receiver of OrbitModelListener events from this model.
	 * @param listener the listener to register for receiving events.
	 */
	public void addOrbitModelListener( final OrbitModelListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, OrbitModelListener.class );
	}
	
	
	/**
	 * Remove the listener from receiving OrbitModelListener events from this model.
	 * @param listener the listener to remove from receiving events.
	 */
	public void removeOrbitModelListener( final OrbitModelListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, OrbitModelListener.class );
	}
	
	
	/**
	 * Set the selected accelerator;
	 * @param accelerator the new selected accelerator
	 */
	public void setAccelerator( final Accelerator accelerator ) {
		setSequence( null );
	}
	
	
	/**
	 * Get the accelerator sequence to monitor
	 * @return the selected accelerator sequence
	 */
	public AcceleratorSeq getSequence() {
		return _sequence;
	}
	
	
	/**
	 * Set the selected sequence
	 * @param sequence the new selected sequence
	 */
	public void setSequence( final AcceleratorSeq sequence ) {
		_sequence = sequence;

		// clear the probe since a new one will need to be made for the new sequence
		_baseProbe = null;

		loadBPMs();
		loadCorrectors();
		useSetpoints( sequence );
		
		BEAM_EXCURSION_ORBIT_ADAPTOR.setSequence( sequence );
		BEAM_EXCURSION_ORBIT_ADAPTOR.setBPMAgents( _bpmAgents );
		
		synchronized(_orbitSources) {
			for ( final OrbitSource orbitSource : _orbitSources ) {
				orbitSource.setSequence( sequence, _bpmAgents );
			}
		}
		
		_modificationStore.postModification( this );
		EVENT_PROXY.sequenceChanged(this, sequence);
	}


	/**
	 * Make a new probe off of the base probe by performing a deep copy.
	 * @return a new probe or null if one cannot be made (e.g. no selected sequence)
	 */
	public Probe<?> makeProbe() {
		final Probe<?> baseProbe = getBaseProbe();
		if ( baseProbe != null ) {
			final Probe<?> probe = baseProbe.copy();
			probe.initialize();
			return probe;
		} else {
			return null;
		}
	}


	/**
	 * Get the base probe for configuration. It should never be run! Instead use makeProbe() to make a copy which you can run.
	 * @return the base probe generating it if necessary or null if one cannot be generated (e.g. no selected sequence)
	 */
	Probe<?> getBaseProbe() {
		// if there is no base probe and the sequence is not null then generate a new base probe for the sequence
		if ( _baseProbe == null && _sequence != null ) {
			try {
				if ( _sequence instanceof Ring ) {
					final TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker( _sequence );
					_baseProbe = ProbeFactory.getTransferMapProbe( _sequence, tracker );

				} else {
					final ParticleTracker  tracker = AlgorithmFactory.createParticleTracker( _sequence );
					_baseProbe = ProbeFactory.createParticleProbe( _sequence, tracker );
				}
			}
			catch (Exception exception) {
				throw new RuntimeException( "Exception making a new probe.", exception );
			}
		}

		return _baseProbe;
	}


	/** determine whether the model is using a beam event trigger for live orbit snapshots */
	public boolean usesBeamEventTrigger() {
		return _useBeamEventTrigger;
	}
	
	
	/** Set whether to use the beam event to trigger orbit snapshots */
	public void setUseBeamEventTrigger( final boolean useTrigger ) {
		_useBeamEventTrigger = useTrigger;
		
		synchronized(_orbitSources) {
			for ( final OrbitSource orbitSource : _orbitSources ) {
				orbitSource.setUseBeamEventTrigger( useTrigger );
			}
		}
	}
	
	
	/**
	 * Use setpoints for electromagnets.
	 */
	public void useSetpoints( final AcceleratorSeq sequence ) {
		if ( sequence != null ) {
			final List<AcceleratorNode> magnets = sequence.getNodesOfType( Electromagnet.s_strType, true );
			for ( AcceleratorNode node : magnets ) {
				((Electromagnet)node).setUseFieldReadback( false );
			}
		}
	}
	
	
	/**
	 * Get the list of BPM agents
	 * @return the list of BPM agents
	 */
	public List<BpmAgent> getBPMAgents() {
		return _bpmAgents;
	}
	
	
	/**
	 * Get the list of BPM agents
	 * @return the list of BPM agents
	 */
	public List<BpmAgent> getAvailableBPMAgents() {
		return AVAILABLE_BPM_AGENTS;
	}
	
	
	/** Load the bpms from the selected sequence and construct the BPM agents. */
	public void loadBPMs() {
		if ( _sequence == null ) {
			AVAILABLE_BPM_AGENTS.clear();
			_bpmAgents = new ArrayList<BpmAgent>();
			BEAM_EXCURSION_ORBIT_ADAPTOR.setBPMAgents( _bpmAgents );
		}
		else {
			final List<BPM> allBPMs = _sequence.getAllNodesOfType( BPM.s_strType );
			final List<BPM> bpms = AcceleratorSeq.filterNodesByStatus( allBPMs, true );
			final Iterator<BPM> bpmIter = bpms.iterator();
			final List<BpmAgent> bpmAgents = new ArrayList<BpmAgent>( bpms.size() );
			while( bpmIter.hasNext() ) {
				final BPM bpm = bpmIter.next();
				if ( bpm.getValid() ) {
					bpmAgents.add( new BpmAgent( bpm ) );
				}
			}
			Collections.sort( bpmAgents, new BPMComparator( _sequence ) );
			BEAM_EXCURSION_ORBIT_ADAPTOR.setBPMAgents( bpmAgents );
			
			AVAILABLE_BPM_AGENTS.clear();
			AVAILABLE_BPM_AGENTS.addAll( bpmAgents );
			refreshEnabledBPMs( false );			
		}		
	}
	
	
	/** refresh enabled BPMs */
	protected void refreshEnabledBPMs( final boolean postChange ) {
		final List<BpmAgent> bpmAgents = new ArrayList<BpmAgent>( AVAILABLE_BPM_AGENTS.size() );
		
		for ( final BpmAgent bpmAgent : AVAILABLE_BPM_AGENTS ) {
			if ( bpmAgent.isEnabled() ) {
				bpmAgents.add( bpmAgent );
			}
		}
		_bpmAgents = new ArrayList<BpmAgent>();
		_bpmAgents.addAll( bpmAgents );
		
		if ( postChange ) {
			for ( final OrbitSource source : _orbitSources ) {
				source.setSequence( _sequence, _bpmAgents );
			}
			EVENT_PROXY.enabledBPMsChanged( this, _bpmAgents );
		}
	}
	
	
	/** refresh enabled BPMs */
	public void refreshEnabledBPMs() {
		refreshEnabledBPMs( true );
	}
	
	
	/**
	 * Get this model's orbit sources.
	 * @return this model's orbit sources
	 */
	public List<OrbitSource> getOrbitSources() {
		synchronized( _orbitSources ) {
			return new ArrayList<OrbitSource>( _orbitSources );
		}
	}
	
	
	/**
	 * Get the beam excursion / orbit adaptor
	 * @return beam excursion / orbit adaptor
	 */
	public BeamExcursionOrbitAdaptor getBeamExcursionOrbitAdaptor() {
		return BEAM_EXCURSION_ORBIT_ADAPTOR;
	}
	
	
	/**
	 * Get the list of correctors agents.
	 * @return the list of corrector agents
	 */
	public List<CorrectorAgent> getCorrectorAgents() {
		return _correctorAgents;
	}
	
	
	/**
	 * Get the list of horizontal corrector agents
	 * @return the list of horizontal corrector agents
	 */
	public List<CorrectorAgent> getHorizontalCorrectorAgents() {
		final List<CorrectorAgent> horizontalCorrectorAgents = new ArrayList<CorrectorAgent>( _correctorAgents.size() );
		for ( CorrectorAgent corrector : _correctorAgents ) {
			if ( corrector.isHorizontal() ) {
				horizontalCorrectorAgents.add( corrector );
			}
		}
		return horizontalCorrectorAgents;
	}
	
	
	/**
	 * Get the list of vertical corrector agents
	 * @return the list of vertical corrector agents
	 */
	public List<CorrectorAgent> getVerticalCorrectorAgents() {
		final List<CorrectorAgent> verticalCorrectorAgents = new ArrayList<CorrectorAgent>( _correctorAgents.size() );
		for ( CorrectorAgent corrector : _correctorAgents ) {
			if ( corrector.isVertical() ) {
				verticalCorrectorAgents.add( corrector );
			}
		}
		return verticalCorrectorAgents;
	}
	
	
	/**
	 * Get the list of corrector supplies.
	 * @return the list of corrector supplies
	 */
	public List<CorrectorSupply> getCorrectorSupplies() {
		return _correctorSupplies;
	}
	
	
	/**
	 * Get the list of horizontal corrector supplies.
	 * @return the list of horizontal corrector supplies
	 */
	public List<CorrectorSupply> getHorizontalCorrectorSupplies() {
		final List<CorrectorSupply> horizontalSupplies = new ArrayList<CorrectorSupply>( _correctorSupplies.size() );
		for ( CorrectorSupply supply : _correctorSupplies ) {
			if ( supply.isHorizontal() ) {
				horizontalSupplies.add( supply );
			}
		}
		return horizontalSupplies;
	}
	
	
	/**
	 * Get the list of vertical corrector supplies.
	 * @return the list of vertical corrector supplies
	 */
	public List<CorrectorSupply> getVerticalCorrectorSupplies() {
		final List<CorrectorSupply> verticalSupplies = new ArrayList<CorrectorSupply>( _correctorSupplies.size() );
		for ( CorrectorSupply supply : _correctorSupplies ) {
			if ( supply.isVertical() ) {
				verticalSupplies.add( supply );
			}
		}
		return verticalSupplies;
	}
	
	
	/** Load the horizontal and vertical correctors from the selected sequence and construct the Corrector agents. */
	public void loadCorrectors() {
		_correctorAgents = new ArrayList<CorrectorAgent>();
		
		if ( _sequence != null ) {
			loadCorrectors( _correctorAgents, MagnetType.DIPOLE );
		}
	}
	
	
	/**
	 * Load the correctors of the specified type, construct the corresponding corrector agents and populate the corrector agent list.
	 * @param correctorAgents the list to which the corrector agents should be added
	 * @param nodeType the type of corrector nodes to fetch
	 */
	public void loadCorrectors( final List<CorrectorAgent> correctorAgents, final String nodeType ) {
		final Map<String,CorrectorSupply> supplyMap = new HashMap<String,CorrectorSupply>();
		final List<Dipole> allCorrectors = _sequence.getAllNodesOfType( nodeType );
		final List<Dipole> correctors = AcceleratorSeq.filterNodesByStatus( allCorrectors, true );
		
		final Iterator<Dipole> correctorIter = correctors.iterator();
		while( correctorIter.hasNext() ) {
			final Dipole corrector = correctorIter.next();
			final MagnetMainSupply supply = corrector.getMainSupply();
			if ( supply != null ) {
				final String supplyID = supply.getId();
				if ( !supplyMap.containsKey( supplyID ) ) {
					supplyMap.put( supplyID, new CorrectorSupply( supply ) );
				}
				final CorrectorSupply supplyAgent = supplyMap.get( supplyID );
				final CorrectorAgent correctorAgent = new CorrectorAgent( corrector );
				supplyAgent.addCorrector( correctorAgent );
				_correctorAgents.add( correctorAgent );
			}
		}
		
		_correctorSupplyMap = supplyMap;
		_correctorSupplies = new ArrayList<CorrectorSupply>( supplyMap.values() );
		Collections.sort( _correctorSupplies, CorrectorSupply.getFirstCorrectorPositionComparator( _sequence ) );
	}
	
	
	/**
	 * Determine if the specified magnet shares its main power supply.
	 * @param magnet the magnet to test
	 * @return true if the magnet shares its power supply and false if not
	 */
    
    //getNodes() could not be cast to a type so the conversion could not be checked
    @SuppressWarnings( "unchecked" )
	private static boolean sharesMainSupply( final Electromagnet magnet ) {
		final Collection<Electromagnet> nodes = magnet.getMainSupply().getNodes();
		final Iterator<Electromagnet> nodeIter = nodes.iterator();
		
		while ( nodeIter.hasNext() ) {
			final Electromagnet node = nodeIter.next();
			if ( !node.getId().equals( magnet.getId() ) ) {
				return true;
			}
		}
		
		return false;
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "OrbitModel";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		if ( _correctorSupplyMap != null ) {
			final List<DataAdaptor> supplyAdaptors = adaptor.childAdaptors( "supply" );
			for( final DataAdaptor supplyAdaptor : supplyAdaptors ) {
				final String supplyID = supplyAdaptor.stringValue( "id" );
				final CorrectorSupply supply = _correctorSupplyMap.get( supplyID );
				if ( supply != null ) {
                    if ( supplyAdaptor.hasAttribute( "enable" ) ) {
                        final boolean enable = supplyAdaptor.booleanValue( "enable" );
                        supply.setEnabled( enable );
                    }
                    
                    if ( supplyAdaptor.hasAttribute( "lowerFieldLimit" ) ) {
                        supply.setLowerFieldLimit( supplyAdaptor.doubleValue( "lowerFieldLimit" ) );
                    }
                    
                    if ( supplyAdaptor.hasAttribute( "upperFieldLimit" ) ) {
                        supply.setUpperFieldLimit( supplyAdaptor.doubleValue( "upperFieldLimit" ) );
                    }
				}
			}
		}
        
        final List<DataAdaptor> bpmAdaptors = adaptor.childAdaptors( "bpm" );
        if ( bpmAdaptors != null && bpmAdaptors.size() > 0 && _bpmAgents != null ) {
            // cache all our bpms so we can access them by ID
            final Map<String,BpmAgent> bpmAgentMap = new HashMap<String,BpmAgent>( _bpmAgents.size() );
            for ( final BpmAgent bpmAgent : _bpmAgents ) {
                bpmAgentMap.put( bpmAgent.getID(), bpmAgent );
            }
            
            for ( final DataAdaptor bpmAdaptor : bpmAdaptors ) {
                final String bpmID = bpmAdaptor.stringValue( "id" );
                final BpmAgent bpmAgent = bpmAgentMap.get( bpmID );
                if ( bpmAgent != null ) {
                    if ( bpmAdaptor.hasAttribute( "flattenEnable" ) ) {
                        bpmAgent.setFlattenEnabled( bpmAdaptor.booleanValue( "flattenEnable" ) );
                    }
                }
            }
        }
		
		final DataAdaptor flattenerAdaptor = adaptor.childAdaptor( Flattener.DATA_LABEL );
		if ( flattenerAdaptor != null ) {
			getFlattener().update( flattenerAdaptor );
		}
		
		final List<DataAdaptor> orbitSourceAdaptors = adaptor.childAdaptors( OrbitSource.DATA_LABEL );
		for ( DataAdaptor orbitSourceAdaptor : orbitSourceAdaptors ) {
			final String type = orbitSourceAdaptor.stringValue( "type" );
			if ( type.equals( "snapshot" ) ) {
				final SnapshotOrbitSource orbitSource = SnapshotOrbitSource.getInstance( orbitSourceAdaptor, _sequence, _bpmAgents );
				addOrbitSource( orbitSource );
			}
		}
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		if ( _correctorSupplyMap != null ) {
			for( final CorrectorSupply supply : _correctorSupplyMap.values() ) {
				final DataAdaptor supplyAdaptor = adaptor.createChild( "supply" );
				supplyAdaptor.setValue( "id", supply.getID() );
				supplyAdaptor.setValue( "enable", supply.isEnabled() );
                
                if ( supply.isLowerFieldLimitCustom() ) {
                    supplyAdaptor.setValue( "lowerFieldLimit", supply.getLowerFieldLimit() );
                }
                
                if ( supply.isUpperFieldLimitCustom() ) {
                    supplyAdaptor.setValue( "upperFieldLimit", supply.getUpperFieldLimit() );
                }
			}
		}
        
        if ( _bpmAgents != null ) {
            for ( final BpmAgent bpmAgent : _bpmAgents ) {
                final boolean flattenEnable = bpmAgent.getFlattenEnabled();
                if ( !flattenEnable ) {     // only need to store the exceptions
                    final DataAdaptor bpmAdaptor = adaptor.createChild( "bpm" );
                    bpmAdaptor.setValue( "id", bpmAgent.getID() );
                    bpmAdaptor.setValue( "flattenEnable", flattenEnable );
                }
            }
        }
		
		if ( _flattener != null ) {
			adaptor.writeNode( _flattener );
		}
		
		for ( OrbitSource orbitSource : _orbitSources ) {
			if ( orbitSource instanceof SnapshotOrbitSource || orbitSource instanceof LiveOrbitSource ) {
				adaptor.writeNode( orbitSource );
			}
		}
	}
}

