package xal.smf;

import xal.tools.messaging.MessageCenter;
import xal.sim.scenario.ElementMapping;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;
import xal.ca.ChannelFactory;

import java.util.*;
import java.lang.reflect.*;
import java.text.*;

/** 
 * The hierarchical tree of accelerator nodes, elements and sequences of elements.
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

public class Accelerator extends AcceleratorSeq implements /* IElement, */ DataListener {
    /** accelerator system unique identifier */
    private String              m_strSysId;    
    /** date stamp */     
    private String              m_strDate;     
    /** version stamp */
    private String              m_strVer;           
    

	/** Map of predefined combo sequences mapped by combo sequence ID */
	private Map<String,AcceleratorSeqCombo> _comboSequences;    
    
    /** Map of main power supplies keyed by the power supply id */
    private Map<String,MagnetMainSupply> magnetMainSupplies;
    
    /** Map of trim power supplies keyed by the power supply id */
    private Map<String,MagnetTrimSupply> magnetTrimSupplies;
        
    /** edit context holds the dynamic data */
    private EditContext editContext;
	
	/** timing center for this accelerator */
	private TimingCenter _timingCenter;
	
	/** factory for generating accelerator nodes */
	private AcceleratorNodeFactory _nodeFactory;
	
	/** Model element mapping */
	private ElementMapping     elementMapping;

    
    // DataAdaptor interface ----------------------
    
    /** 
     * dataLabel() provides the name used to identify the accelerator in an 
     * external data source.
     * @return The accelerator's tag
     */
    public String dataLabel() { return "xdxf"; }
    
    
    /**
     * Instructs the accelerator to update its data based on the given adaptor.
     * @param adaptor The adaptor from which to update the accelerator's data
     */
    public void update( final DataAdaptor adaptor ) throws NumberFormatException {
        // only the primary optics should supply this data
        if ( adaptor.hasAttribute( "system" ) ) {
            m_strSysId = adaptor.stringValue( "system" );
        }
        if ( adaptor.hasAttribute( "ver" ) ) {
            m_strVer = adaptor.stringValue( "ver" );
        }
		if ( adaptor.hasAttribute( "date") ) {
			m_strDate = adaptor.stringValue( "date" );
		}
        
        DataAdaptor powerSuppliesAdaptor = adaptor.childAdaptor("powersupplies");
        if ( powerSuppliesAdaptor != null ) {
            updatePowerSupplies(powerSuppliesAdaptor);
        }
        
        super.update(adaptor);
		
        // read all pre defined combo sequences
        final List<DataAdaptor> comboAdaptors = adaptor.childAdaptors( "comboseq" );
        for ( final DataAdaptor comboAdaptor : comboAdaptors ) {
            try {
                addComboSequence( comboAdaptor );
            }
            catch (ClassNotFoundException excpt) {
                System.err.println(excpt);
                excpt.printStackTrace();
            }
        }
    }
    
    
    /**
     * Update the power supplies given the power supply adaptor
     * @param adaptor The adaptor for the accelerator power supplies
     */
    protected void updatePowerSupplies( final DataAdaptor adaptor ) {
        final List<DataAdaptor> powerSupplyAdaptors = adaptor.childAdaptors( "ps" );
        for ( final DataAdaptor powerSupplyAdaptor : powerSupplyAdaptors ) {
            String powerSupplyType = powerSupplyAdaptor.stringValue("type");
            String powerSupplyId = powerSupplyAdaptor.stringValue("id");
            if ( powerSupplyType.equals("main") ) {
				MagnetMainSupply powerSupply = getMagnetMainSupply( powerSupplyId );
                if ( powerSupply == null )  powerSupply = new MagnetMainSupply(this);
                powerSupply.update(powerSupplyAdaptor);
				putMagnetMainSupply( powerSupply );
            }
            else if ( powerSupplyType.equals("trim") ) {
				MagnetTrimSupply powerSupply = getMagnetTrimSupply( powerSupplyId );
                if ( powerSupply == null )  powerSupply = new MagnetTrimSupply(this);
                powerSupply.update(powerSupplyAdaptor);
				putMagnetTrimSupply( powerSupply );
            }
        }
    }


	/** 
	 * Programmatically add or replace a magnet main supply keyed by its ID. 
	 * If a power supply has the same ID as another power supply in this accelerator then it will replace that one.
	 * @param mainSupply main power supply to add or replace
	 * @throws IllegalArgumentException if the power supply's accelerator does not match this accelerator to which it is being put
	 */
	public void putMagnetMainSupply( final MagnetMainSupply mainSupply ) throws IllegalArgumentException {
		if ( mainSupply.getAccelerator() == this ) {	// make sure this supply belongs here
			magnetMainSupplies.put( mainSupply.getId(), mainSupply );
		} else {
			throw new IllegalArgumentException( "Attempted to put Magnet Main Supply: " + mainSupply.getId() + " whose accelerator does not match the accelerator to which it is being put." );
		}
	}


	/**
	 * Programmatically add or replace a magnet trim supply keyed by its ID.
	 * If a power supply has the same ID as another power supply in this accelerator then it will replace that one.
	 * @param trimSupply trim power supply to add or replace
	 * @throws IllegalArgumentException if the power supply's accelerator does not match this accelerator to which it is being put
	 */
	public void putMagnetTrimSupply( final MagnetTrimSupply trimSupply ) {
		if ( trimSupply.getAccelerator() == this ) {	// make sure this supply belongs here
			magnetTrimSupplies.put( trimSupply.getId(), trimSupply );
		} else {
			throw new IllegalArgumentException( "Attempted to put Magnet Trim Supply: " + trimSupply.getId() + " whose accelerator does not match the accelerator to which it is being put." );
		}
	}


    /**
     * Instructs the accelerator to write its data to the adaptor for external
     * storage.
     * @param adaptor The adaptor to which the accelerator's data is written
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("system", m_strSysId);
        adaptor.setValue("ver", m_strVer);     // what if several inputs?

        Date today = new Date();
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM.dd.yyyy");
        String dateString = dateFormatter.format(today);
        adaptor.setValue("date", dateString);
        
        super.write(adaptor);
    }
    
    
    /** 
	 * Add a combo sequence generated from the comboAdaptor
	 * @param comboAdaptor The data adaptor from which to generate the combo sequence
	 */
    private void addComboSequence(DataAdaptor comboAdaptor) throws ClassNotFoundException {
        String comboType = comboAdaptor.stringValue("type");
		String comboID = comboAdaptor.stringValue("id");
		// check if we already have the sequence
		AcceleratorSeqCombo comboSequence = getComboSequence( comboID );
		// if the sequence doesn't already exist, create a new one
		// and add the new sequence to the accelerator root sequence
		if ( comboSequence == null ) {
			comboSequence = instantiateComboSequence(comboType, comboID, comboAdaptor);
			comboSequence.setAccelerator(this.getAccelerator());
			addComboSequence(comboSequence);
		}
		else {
			comboSequence.update(comboAdaptor);   // update the sequence
		}
    }
	
	
	/**
	 * Instantiate a predefined combo sequence.
	 * @param comboType the type of combo sequence identifying the combo sequence subclass
	 * @param comboID the ID of the combo sequence
	 * @param comboAdaptor the data adaptor that defines the combo sequence
	 */
    @SuppressWarnings( { "unchecked", "rawtypes" } )
	private AcceleratorSeqCombo instantiateComboSequence(String comboType, String comboID, DataAdaptor comboAdaptor) throws ClassNotFoundException {
		if ( comboType == null || comboType.isEmpty() ) {
			return AcceleratorSeqCombo.getInstance(comboID, this, comboAdaptor);
		}
		try {
			final Class<?> comboClass = Class.forName( comboType );
			final Constructor<?> constructor = comboClass.getConstructor( new Class[] {String.class, Accelerator.class, DataAdaptor.class} );
			return (AcceleratorSeqCombo)constructor.newInstance( new Object[] {comboID, this, comboAdaptor} );
		}
		catch(Exception exception) {
			System.err.println(exception);
			exception.printStackTrace();
			return null;
		}
	}
	
	
    // end DataAdaptor interface ----------------------
    

    // empty constructor added by tap  2/25/2002
    public Accelerator() {
        this( "" );
    }
    
    
    /** Primary constructor */
    public Accelerator( final String sysId ) {
		this( sysId, ChannelFactory.defaultFactory() );
    }


	/** Primary constructor */
	public Accelerator( final ChannelFactory channelFactory ) {
		this( "", channelFactory );
	}


	/** Primary constructor */
	public Accelerator( final String sysId, final ChannelFactory channelFactory ) {
		super( sysId, channelFactory );

		//System.out.println( "Instantiating Accelerator with channel factory: " + channelFactory );

		m_strSysId = sysId;
		_comboSequences = new HashMap<String,AcceleratorSeqCombo>();

		// Create hash maps to hold the main and trim power supplies
		magnetMainSupplies = new HashMap<String,MagnetMainSupply>();
		magnetTrimSupplies = new HashMap<String,MagnetTrimSupply>();

		// Create an edit context to hold dynamic data -tap 6/7/2002
		editContext = new EditContext();

		// initialize the timing center
		_timingCenter = new TimingCenter();
	}


	/**
	 * Handle the event indicating that a node has been added.
	 * @param p_node the node that has been added
	 */
    protected void nodeAdded( final AcceleratorNode p_node ) {
    }
	
	
	/** 
	 * Handle the event indicating that a node has been removed.
	 * @param p_node the node that has been removed
	 */
    protected void nodeRemoved( final AcceleratorNode p_node ) {
    }
	

    public String           getSystemId()   { return m_strSysId; };
    public String           getDate()       { return m_strDate; };
    public String           getVersion()    { return m_strVer; };
    
    
    public AcceleratorSeq   getRoot()       { return this; };
    public Accelerator getAccelerator() { return this; }

    
    public void setDate(String strDate)     { m_strDate = strDate; };
    public void setVersion(String strVer)   { m_strVer = strVer; };
  
	
	/** Get the accelerator node factory */
	public AcceleratorNodeFactory getNodeFactory() {
		return _nodeFactory;
	}
	
	
	/** Set the factory used to generate new accelerator nodes */
	public void setNodeFactory( final AcceleratorNodeFactory nodeFactory ) {
		_nodeFactory = nodeFactory;
	}
    
	/**
	 * Sets the model element mapping used by this accelerator
	 * object.
	 *
	 * @param elementMapping     the new element mapping 
	 *
	 * @author Ivo List
	 */
	public void setElementMapping(ElementMapping elementMapping) {
	    this.elementMapping = elementMapping;
	}
	
	/**
	 * Returns the model element mapping currently in use
	 * by this accelerator.
	 *
	 * @return     accelerator's model element mapping
	 *
	 * @author Ivo List
	 */
	public ElementMapping getElementMapping() {
	    return this.elementMapping;
	}
	
	/**
	 * Get this accelerator's edit context
	 * @return This accelerator's edit context
	 */
    public EditContext editContext() {
        return editContext;
    }
    
    
	/**
	 * Set this accelerator's edit context
	 * @param newContext the accelerator's new edit context
	 */
    public void setEditContext( final EditContext newContext ) {
        editContext = newContext;
    }
	
	
	/**
	 * Get this accelerator's timing center
	 * @return This accelerator's timing center
	 */
	public TimingCenter getTimingCenter() {
		return _timingCenter;
	}
	
	
	/**
	 * Set this accelerator's timing center
	 * @param timingCenter the accelerator's new timing center
	 */
	public void setTimingCenter( final TimingCenter timingCenter ) {
		_timingCenter = timingCenter;
	}
    
    
    /** 
	 * Add a combo sequence to this accelerator
	 * @param comboSequence The combo sequence to add
	 */
    protected void addComboSequence( final AcceleratorSeqCombo comboSequence ) {
		_comboSequences.put( comboSequence.getId(), comboSequence );
	}
	
	
	/**
	 * Fetch the predefined combo sequence based on its ID
	 * @param comboID the id identifying the combo sequence
	 * @return the combo sequence for the ID or null if none matches
	 */
	public AcceleratorSeqCombo getComboSequence( final String comboID ) {
		return _comboSequences.get( comboID );
	}
	
	
	/**
	 * Get the list of predefined combo sequences ordered by ID.
	 * @return the list of predefined combo sequences ordered by ID.
	 */
	public List<AcceleratorSeqCombo> getComboSequences() {
		final List<AcceleratorSeqCombo> sequences = new ArrayList<AcceleratorSeqCombo>( _comboSequences.values() );
		Collections.sort( sequences, new Comparator<AcceleratorSeqCombo>() {
			public int compare( final AcceleratorSeqCombo combo1, final AcceleratorSeqCombo combo2 ) {
				return combo1.getId().compareTo( combo2.getId() ); 
			}
            
			public boolean equals( final Object comparator ) {
				return this.equals( comparator );
			}
		});
		return sequences;
	}
	
	
	/**
	 * Get the ring in this accelerator with the specified ID
	 * @param ringID the ID of the ring to get
	 * @return the ring in this accelerator with the specified ID or null if none exists
	 */
	public Ring getRing( final String ringID ) {
		return (Ring)getComboSequence( ringID );
	}
	
	
	/**
	 * Get the list of all rings in the accelerator
	 * @return a list of all rings in the accelerator
	 */
	public List<Ring> getRings() {
		final List<AcceleratorSeqCombo> comboSequences = getComboSequences();
		final List<Ring> rings = new ArrayList<Ring>();
        
        for ( final AcceleratorSeqCombo candidate : comboSequences ) {
			if ( candidate instanceof Ring ) rings.add( (Ring)candidate );
        }
		
		return rings;
	}
	
	
	/**
	 * Find a sequence with the specified ID.  The sequence may either be a direct child
	 * sequence or a predefined combo sequence.
	 * @param sequenceID the id identifying the desired sequence
	 * @return the sequence for the ID or null if none matches
	 */
	public AcceleratorSeq findSequence(String sequenceID) {
		AcceleratorSeq sequence = null;
		
		sequence = getSequence(sequenceID);		
		if ( sequence != null )  return sequence;
		
		return getComboSequence(sequenceID);
	}
	    
    
    /*
     *  Convenience Functions
     */
    

    /** 
	 * Returns the AcceleratorNode with a requsted name
     * @param nodeID - the name to match 
     */
    public AcceleratorNode  getNode( final String nodeID )   {
        final List<AcceleratorNode> allNodes = getAllNodes();
        for ( final AcceleratorNode node : allNodes ) {
            if ( node.getId().equals( nodeID ) )
                return node;
        }
        
        return null;
    }
	
	
	/**
	 * Get the set of all magnet main supplies
	 * @return the set of all magnet main supplies
	 */
	public Collection<MagnetMainSupply> getMagnetMainSupplies() {
		return magnetMainSupplies.values();
	}
    
    
    /**
     * Get a main power supply whose id is supplyId
     * @param supplyId The id of the main power supply to get
     * @return The main power supply or null if the supplyId is not found
     */
    public MagnetMainSupply getMagnetMainSupply(String supplyId) {
        return magnetMainSupplies.get( supplyId );
    }
	
	
	/**
	 * Get the set of all magnet trim supplies
	 * @return the set of all magnet trim supplies
	 */
	public Collection<MagnetTrimSupply> getMagnetTrimSupplies() {
		return magnetTrimSupplies.values();
	}
    
    
    /**
     * Get a main power supply whose id is supplyId
     * @param supplyId The id of the main power supply to get
     * @return The main power supply or null if the supplyId is not found
     */
    public MagnetTrimSupply getMagnetTrimSupply(String supplyId) {
        return magnetTrimSupplies.get(supplyId);
    }
}
