package xal.smf;

import xal.tools.messaging.MessageCenter;

import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;

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
	protected Map _comboSequences;    
    
    /** Map of main power supplies keyed by the power supply id */
    protected Map magnetMainSupplies;
    
    /** Map of trim power supplies keyed by the power supply id */
    protected Map magnetTrimSupplies;
        
    /** edit context holds the dynamic data */
    private EditContext editContext;
	
	/** timing center for this accelerator */
	private TimingCenter _timingCenter;
	
	/** factory for generating accelerator nodes */
	private AcceleratorNodeFactory _nodeFactory;

    
    // IDataAdaptor interface ----------------------
    
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
    public void update( final IDataAdaptor adaptor ) throws NumberFormatException {
        // only the primary optics should supply this data
        if ( adaptor.hasAttribute("System") ) {
            m_strSysId = adaptor.stringValue("system");
        }
        if ( adaptor.hasAttribute("ver") ) {
            m_strVer = adaptor.stringValue("ver");
        }
        
        IDataAdaptor powerSuppliesAdaptor = adaptor.childAdaptor("powersupplies");
        if ( powerSuppliesAdaptor != null ) {
            updatePowerSupplies(powerSuppliesAdaptor);
        }
        
        super.update(adaptor);
		
        // read all pre defined combo sequences
        final List<IDataAdaptor> comboAdaptors = adaptor.childAdaptors( "comboseq" );
        for ( final IDataAdaptor comboAdaptor : comboAdaptors ) {
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
    protected void updatePowerSupplies( final IDataAdaptor adaptor ) {
        final List<IDataAdaptor> powerSupplyAdaptors = adaptor.childAdaptors( "ps" );
        for ( final IDataAdaptor powerSupplyAdaptor : powerSupplyAdaptors ) {
            String powerSupplyType = powerSupplyAdaptor.stringValue("type");
            String powerSupplyId = powerSupplyAdaptor.stringValue("id");
            if ( powerSupplyType.equals("main") ) {
                MagnetMainSupply powerSupply = getMagnetMainSupply(powerSupplyId);
                if ( powerSupply == null )  powerSupply = new MagnetMainSupply(this);
                powerSupply.update(powerSupplyAdaptor);
                magnetMainSupplies.put(powerSupply.getId(), powerSupply);
            }
            else if ( powerSupplyType.equals("trim") ) {
                MagnetTrimSupply powerSupply = getMagnetTrimSupply(powerSupplyId);
                if ( powerSupply == null )  powerSupply = new MagnetTrimSupply(this);
                powerSupply.update(powerSupplyAdaptor);
                magnetTrimSupplies.put(powerSupply.getId(), powerSupply);
            }
        }
    }
    
    
    /**
     * Instructs the accelerator to write its data to the adaptor for external
     * storage.
     * @param adaptor The adaptor to which the accelerator's data is written
     */
    public void write(IDataAdaptor adaptor) {
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
    private void addComboSequence(IDataAdaptor comboAdaptor) throws ClassNotFoundException {
        String comboType = comboAdaptor.stringValue("type");
		String comboID = comboAdaptor.stringValue("id");
		// check if we already have the sequence
		AcceleratorSeqCombo comboSequence = (AcceleratorSeqCombo)getComboSequence(comboID);
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
	private AcceleratorSeqCombo instantiateComboSequence(String comboType, String comboID, IDataAdaptor comboAdaptor) throws ClassNotFoundException {
		if ( comboType == null || comboType == "" ) {
			return AcceleratorSeqCombo.getInstance(comboID, this, comboAdaptor);
		}
		try {
			Class comboClass = Class.forName(comboType);
			Constructor constructor = comboClass.getConstructor(new Class[] {String.class, Accelerator.class, IDataAdaptor.class});
			return (AcceleratorSeqCombo)constructor.newInstance(new Object[] {comboID, this, comboAdaptor});
		}
		catch(Exception exception) {
			System.err.println(exception);
			exception.printStackTrace();
			return null;
		}
	}
	
	
    // end IDataAdaptor interface ----------------------
    

    // empty constructor added by tap  2/25/2002
    public Accelerator() {
        this( "" );
    }
    
    
    /** Primary constructor */
    public Accelerator( final String sysId ) {
        super( sysId );
        
        m_strSysId = sysId;
		_comboSequences = new HashMap();

        // Create hash maps to hold the main and trim power supplies
        magnetMainSupplies = new HashMap();
        magnetTrimSupplies = new HashMap();
        
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
		return (AcceleratorSeqCombo)_comboSequences.get( comboID );
	}
	
	
	/**
	 * Get the list of predefined combo sequences ordered by ID.
	 * @return the list of predefined combo sequences ordered by ID.
	 */
	public List getComboSequences() {
		List sequences = new ArrayList(_comboSequences.values());
		Collections.sort(sequences, new Comparator() {
			public int compare(Object item1, Object item2) {
				AcceleratorSeqCombo combo1 = (AcceleratorSeqCombo)item1;
				AcceleratorSeqCombo combo2 = (AcceleratorSeqCombo)item2;
				return combo1.getId().compareTo(combo2.getId()); 
			}
			public boolean equals(Object comparator) {
				return false;
			}
		});
		return sequences;
	}
	
	
	/**
	 * Get the ring in this accelerator with the specified ID
	 * @param ringID the ID of the ring to get
	 * @return the ring in this accelerator with the specified ID or null if none exists
	 */
	public Ring getRing(String ringID) {
		return (Ring)getComboSequence(ringID);
	}
	
	
	/**
	 * Get the list of all rings in the accelerator
	 * @return a list of all rings in the accelerator
	 */
	public List getRings() {
		List comboSequences = getComboSequences();
		List rings = new ArrayList();
		Iterator comboIter = comboSequences.iterator();
		while ( comboIter.hasNext() ) {
			Object candidate = comboIter.next();
			if ( candidate instanceof Ring )  rings.add(candidate);
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
     * @param strId - the name to match 
     */
    public AcceleratorNode  getNode(String strId)   {
        List                lstAllNodes;    // list of all the nodes
        Iterator            iterNodes;      // node iterator
        AcceleratorNode     node;           // current node
     
        lstAllNodes = getAllNodes();
        iterNodes   = lstAllNodes.iterator();
        while (iterNodes.hasNext()) {
            node = (AcceleratorNode)iterNodes.next();
            
            if (node.getId().equals(strId))
                return node;
        }
        
        return null;
    }
	
	
	/**
	 * Get the set of all magnet main supplies
	 * @return the set of all magnet main supplies
	 */
	public Collection getMagnetMainSupplies() {
		return magnetMainSupplies.values();
	}
    
    
    /**
     * Get a main power supply whose id is supplyId
     * @param supplyId The id of the main power supply to get
     * @return The main power supply or null if the supplyId is not found
     */
    public MagnetMainSupply getMagnetMainSupply(String supplyId) {
        return (MagnetMainSupply)magnetMainSupplies.get(supplyId);
    }
	
	
	/**
	 * Get the set of all magnet trim supplies
	 * @return the set of all magnet trim supplies
	 */
	public Collection getMagnetTrimSupplies() {
		return magnetTrimSupplies.values();
	}
    
    
    /**
     * Get a main power supply whose id is supplyId
     * @param supplyId The id of the main power supply to get
     * @return The main power supply or null if the supplyId is not found
     */
    public MagnetTrimSupply getMagnetTrimSupply(String supplyId) {
        return (MagnetTrimSupply)magnetTrimSupplies.get(supplyId);
    }
}
