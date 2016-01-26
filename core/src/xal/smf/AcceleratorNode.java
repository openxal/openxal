package xal.smf;

import xal.ca.*;
import xal.tools.data.*;
import xal.smf.attr.*;
import xal.smf.data.BucketParser;
import xal.smf.impl.qualify.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The base class in the hierarchy of different accelerator node types.
 * @author  Nikolay Malitsky, Christopher K. Allen, Nick D. Pattengale
 */
public abstract class AcceleratorNode implements /* IElement, */ ElementType, DataListener {
    
    /*
     *  Local Attributes
     */
    
    /** node identifier  */
    protected String            m_strId;
    
    /** physics identifier  */
    protected String            m_strPId;
    
    /** engineering identifier  */
    protected String            m_strEId;
    
    /** position of node   */
    protected double            m_dblPos;
    
    /** length of node */
    protected double            m_dblLen;
    

    
    /**   parent sequence object  */
    protected AcceleratorSeq    m_seqParent;
    
    /**   the associated Accelerator object  */
    protected Accelerator       m_objAccel;
    
    /**   all attribute buckets for node   */
    protected Map<String,AttributeBucket>   m_mapAttrs;
    
    /**   alignment attribute bucket for node */
    protected AlignmentBucket   m_bucAlign;
    
    /**    twiss parameter bucket for node   */
    protected TwissBucket       m_bucTwiss;
    
    /**                  aperture parameters for node   */
    protected ApertureBucket    m_bucAper;
    
    /** Indicator as to whether the Accelerator Node is functional */
    protected boolean            m_bolStatus;
    
    /** Indicator as to whether accelerator node is valid */
    protected boolean            m_bolValid;
    
    /** "s" position for global display */
    protected double 			m_dblS;
        
    
    /** Indicator if this node is a "softNode" copy */
    protected boolean       m_bolIsSoft=false;
    
    
    /** channel suite associated with this node */
    protected ChannelSuite channelSuite;
    
    
    /** Derived class must furnish a unique type id */
    abstract public String getType();
    
    
    /** Derived class may furnish a unique software type */
	public String getSoftType() {
		return null;
	}


	/**
	 * Designated constructor
	 * @param strId the string ID for this node
	 * @param channelFactory channel factory (null for default) for generating this node's channels
	 */
	public AcceleratorNode( final String strId, final ChannelFactory channelFactory ) {
		m_strId = strId;

		m_bolStatus = true;
		m_bolValid = true;

		m_mapAttrs = new HashMap<String,AttributeBucket>();

		setAlign(new AlignmentBucket());
		setAper(new ApertureBucket());
		setTwiss(new TwissBucket());

		channelSuite = new ChannelSuite( channelFactory );
	}


    /**
	 * Convenience constructor using the default channel factory
     * @param strId the string ID for this node
	 */
    public AcceleratorNode( final String strId ) {
		this( strId, ChannelFactory.defaultFactory() );
    }
    
    
    // DataListener interface -tap
    
    /** implement DataListener interface */
    public String dataLabel() { return "node"; }
    
    
    /** implement DataListener interface */
    public void update(DataAdaptor adaptor) throws NumberFormatException {
        // set the id only the first time
        if ( m_strId == null ) {
            m_strId = adaptor.stringValue("id");
        }
        
        // update physics id
        if ( adaptor.hasAttribute("pid") ) {
        	m_strPId = adaptor.stringValue("pid");
        }
        
        // update engineering id
        if ( adaptor.hasAttribute("eid") ) {
        	m_strEId = adaptor.stringValue("eid");
        }
                
        // get the status of the node which identifies whether the node is operational
        if ( adaptor.hasAttribute("status") ) {
            m_bolStatus = adaptor.booleanValue("status");
        }
        
        
        // update length attribute if the adaptor supplies it
        if ( adaptor.hasAttribute("len") ) {
            double newLength;
            try {
                newLength = adaptor.doubleValue("len");
            }
            catch(NumberFormatException exception) {
				final String message = "Error reading node: " + m_strId;
                System.err.println( message );
                System.err.println( exception );
				Logger.getLogger("global").log( Level.SEVERE, message, exception );
                newLength = Double.NaN;
            }
            
            setLength(newLength);
        }
        
        // update position attribute if the adaptor supplies it
        if ( adaptor.hasAttribute("pos") ) {
            double newPosition = adaptor.doubleValue("pos");
            setPosition(newPosition);
        }
        
        // update s display coordinate if there is one
        if ( adaptor.hasAttribute("s") ) {
            double newSDisplay = adaptor.doubleValue("s");
            setSDisplay(newSDisplay);
        }        
        
        // read the channel suites
        DataAdaptor suiteAdaptor = adaptor.childAdaptor("channelsuite");
        if ( suiteAdaptor != null ) {
            channelSuite.update(suiteAdaptor);
        }
        
        // read the attribute buckets
        final List<DataAdaptor> parserAdaptors = adaptor.childAdaptors( "attributes" );
        for ( final DataAdaptor parserAdaptor : parserAdaptors ) {
            final Collection<AttributeBucket> buckets = getBuckets();
            final BucketParser parser = new BucketParser( buckets );
            parser.update( parserAdaptor );
            
            // get the attribute buckets from the parser
            final Collection<AttributeBucket> bucketList = parser.getBuckets();
			for ( final AttributeBucket bucket : bucketList ) {
                // add the bucket only if it already hasn't been added
                if ( !hasBucket(bucket) ) {
                    addBucket(bucket);
                }
            }
        }
    }
    
    
    /** implement DataListener interface */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue( "id", m_strId );
        adaptor.setValue("pid", m_strPId);
        adaptor.setValue("eid", m_strEId);
        adaptor.setValue( "type", getType() );
		if ( getSoftType() != null ) {
			adaptor.setValue( "softType", getSoftType() );
		}
        adaptor.setValue( "status", m_bolStatus );
        adaptor.setValue( "pos", m_dblPos );
        adaptor.setValue("s", m_dblS);
        adaptor.setValue( "len", m_dblLen );
        
        Collection<AttributeBucket> buckets = getBuckets();
        adaptor.writeNode( new BucketParser(buckets) );
        
        adaptor.writeNode(channelSuite);
    }
    // end DataListener interface -tap
    
    
	
	/**
	 * Attempt to find a channel for the given handle.
	 * @param handle the handle for which to find an associated channel
	 * @return channel for the given handle or null if none could be found
	 */
	public Channel findChannel( final String handle ) {
		return channelSuite.getChannel( handle );
	}
    
    
    // added by nickp 2/8/2002
    /** this method returns the Channel object of this node, associated with
     * a prescibed PV name. Note - xal interacts with EPICS via Channel objects.
     * @param chanHandle The handle to the epics channel in stored in the channel suite
     */
    public Channel getChannel( final String chanHandle ) throws NoSuchChannelException {
        final Channel channel = findChannel( chanHandle );
        
        if ( channel == null ) {
           throw new NoSuchChannelException(this, chanHandle);
        }
        
        return channel;
    }
    
    
    /**
     * Get the channel corresponding to the specified handle and connect it. 
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by the handle is associated with this node.
     * @throws xal.ca.ConnectionException if the channel cannot be connected
     */
    public Channel getAndConnectChannel( final String handle ) throws NoSuchChannelException, ConnectionException {
        final Channel channel = getChannel(handle);
        channel.connectAndWait();
        
        return channel;
    }
    
    
    /**
     * A method to make an EPICS ca  connection for a given PV name
     * The channel connection is initiated, and no extra work is
     * done, if the channel connection already exists
     */
    public Channel lazilyGetAndConnect(String chanHandle, Channel channel) throws ConnectionException, NoSuchChannelException {
        Channel tmpChan;
        
        if(channel == null) {
            tmpChan = getChannel(chanHandle);
            if ( tmpChan == null ) {
                throw new NoSuchChannelException(this, chanHandle);
            }
        }
        else {
            tmpChan = channel;
        }
        
        tmpChan.connectAndWait();
        
        return tmpChan;
    }
    

	/** Get the design value for the specified property */
	public double getDesignPropertyValue( final String propertyName ) {
		throw new IllegalArgumentException( "Unsupported AcceleratorNode design value property: " + propertyName );
	}


	/** Get the live property value for the corresponding array of channel values in the order given by getLivePropertyChannels() */
	public double getLivePropertyValue( final String propertyName, final double[] channelValues ) {
		throw new IllegalArgumentException( "Unsupported AcceleratorNode live value property: " + propertyName );
	}


	/** Get the array of channels for the specified property */
	public Channel[] getLivePropertyChannels( final String propertyName ) {
		throw new IllegalArgumentException( "Unsupported AcceleratorNode live channels property: " + propertyName );
	}

    
    
    /*
     *  User Interface
     */
    
    /** return the ID of this node */
    public String           getId()             { return m_strId;  };
    
    /** return the engineering ID of this node */   
    public String           getEId()             { return m_strEId;  };
    
    /** return the physics ID of this node */   
    public String           getPId()             { return m_strPId;  };
    
    /** return the physical length of this node (m) */
    public double           getLength()         { return m_dblLen; };
    
    /** return the position of this node,  along the reference orbit
     * within its sequence (m) */
    public double           getPosition()       { return m_dblPos; };
    
    
    /**
     * return global "s" display coordinate
     * @return s coordinate
     */
    public double 			getSDisplay()		{ return m_dblS;};
    
    /** return the top level accelerator that this node belongs to */
    public Accelerator      getAccelerator()    { return m_objAccel; };
    
    /** return the parent sequence that this node belongs to */
    public AcceleratorSeq  getParent()         { return m_seqParent; }
	
	
	/** get the primary ancestor sequence that is a direct child of the accelerator */
	public AcceleratorSeq getPrimaryAncestor() {
		return getParent().getPrimaryAncestor();
	}
    
    
    /** Indicates if the node has a parent set */
    public boolean  hasParent()         {
        return (m_seqParent != null);
    }
    
    /**
     *  Runtime indication of accelerator component operation
     *  @return         true(up and running)
     *                  false(down)
     */
    public boolean          getStatus()         { return m_bolStatus; };
    
    /**
     *  Runtime indication of the validatity of component operation
     *  @return         true(valid operation)
     *                  false(questionable operation)
     */
    public boolean          getValid()          { return m_bolValid; };
    
    
    void     setPId(String value)         { m_strPId = value; }
    void     setEId(String value)         { m_strEId = value; }

    /** set the position of this accelerator node within its parent sequence */
	public void setPosition( final double position )  { m_dblPos = position; };
	
	
	/** set the length of this accelerator node  */
	public void setLength( final double length )    { m_dblLen = length; };
    
    /**
     * set "s" coordinate
     * @param dblS s coordinate
     */
    public void 	setSDisplay(double dblS) { m_dblS = dblS; };
    
    
    /**
     *  Runtime indication of accelerator operation
     *  @param      bolStatus       true(up and running)
     *                              false(down)
     */
    public void     setStatus(boolean bolStatus)    { m_bolStatus = bolStatus; };
    
    
    /**
     *  Runtime indication of the validatity of component operation
     *  @param  bolValid    true(valid operation)
     *                      false(questionable operation)
     */
    public void     setValid(boolean bolValid)      { m_bolValid = bolValid; };
    
    
    /*
     *  SMF Attribute Buckets Support
     */
    
    /** General attribute buckets support */
    public void addBucket(AttributeBucket buc)  {
        
        if (buc.getClass().equals( TwissBucket.class )) {
            setTwiss((TwissBucket)buc);
        }
        if (buc.getClass().equals( AlignmentBucket.class ))  {
            setAlign((AlignmentBucket)buc);
        }
        if (buc.getClass().equals( ApertureBucket.class )){
            setAper((ApertureBucket)buc);
        }
        
        // List of all attribute buckets
        m_mapAttrs.put( buc.getType(), buc );
    };
    
    public Collection<AttributeBucket>       getBuckets()            { return m_mapAttrs.values(); };
    public AttributeBucket  getBucket(String type)  { return m_mapAttrs.get(type); };
    public boolean hasBucket(AttributeBucket bucket) {
        String bucketType = bucket.getType();
        return bucket == getBucket(bucketType);
    }
    
    
    // Specific Buckets
    
    /** returns the bucket containing the twiss parameters
     *   - see attr.TwissBucket  */
    public TwissBucket      getTwiss()          { return m_bucTwiss; };
    
    /** returns the bucket containing the alignment parameters
     *   - see attr.AlignBucket  */
    public AlignmentBucket  getAlign()          { return m_bucAlign; };
    
    /**
     * returns device pitch angle in degrees
     * @return pitch angle
     */
    public double getPitchAngle() {
    	return m_bucAlign.getPitch();
    }
    
    /**
     * returns device yaw angle in degrees
     * @return yaw angle
     */
    public double getYawAngle() {
    	return m_bucAlign.getYaw();
    }
    
    /**
     * returns device roll angle in degrees
     * @return roll angle
     */
    public double getRollAngle() {
    	return m_bucAlign.getRoll();
    }
    
    /**
     * returns device x offset
     * @return x offset
     */
    public double getXOffset() {
    	return m_bucAlign.getX();
    }
    
    /**
     * returns device y offset
     * @return y offset
     */
    public double getYOffset() {
    	return m_bucAlign.getY();
    }
    
    /**
     * returns device z offset
     * @return z offset
     */
    public double getZOffset() {
    	return m_bucAlign.getZ();
    }
    
    /** returns the bucket containing the Aperture parameters
     *   - see attr.ApertureBucket  */
    public ApertureBucket   getAper()           { return m_bucAper; };
    
    /** sets the bucket containing the twiss parameters
     *   - see attr.TwissBucket  */
    
    public void setAlign(AlignmentBucket buc)   { m_bucAlign = buc; m_mapAttrs.put(buc.getType(), buc); };
    
    /** sets the bucket containing the alignment parameters
     *   - see attr.AlignBucket  */
    public void setTwiss(TwissBucket buc)       { m_bucTwiss = buc; m_mapAttrs.put(buc.getType(), buc); };
    
    /** sets the bucket containing the Aperture parameters
     *   - see attr.ApertureBucket  */
    public void setAper(ApertureBucket buc)     { m_bucAper = buc;  m_mapAttrs.put(buc.getType(), buc); };
    
    /**
     * set device pitch angle
     * @param angle pitch angle in degree
     */
    public void setPitchAngle(double angle) {
    	m_bucAlign.setPitch(angle);
    }
    
    /**
     * set device yaw angle
     * @param angle yaw angle in degree
     */
    public void setYawAngle(double angle) {
    	m_bucAlign.setYaw(angle);
    }
    
    /**
     * set device roll angle
     * @param angle roll angle in degree
     */
    public void setRollAngle(double angle) {
    	m_bucAlign.setRoll(angle);
    }
    
    /**
     * set device x offset
     * @param offset x offset
     */
    public void setXOffset(double offset) {
    	m_bucAlign.setX(offset);
    }
    
    /**
     * set device y offset
     * @param offset y offset
     */
    public void setYOffset(double offset) {
    	m_bucAlign.setY(offset);
    }
    
    /**
     * set device z offset
     * @param offset z offset
     */
    public void setZOffset(double offset) {
    	m_bucAlign.setZ(offset);
    }
    
    /*
     *  SMF Data Structure Methods
     */
    
    /**
     * remove this node from the accelerator hieracrhcy
     */
    public void clear() {
        removeFromParent();
    };
    
    /**
     * remove this node from its immediate parent sequence
     */
    protected void removeFromParent()  {
        if(m_seqParent == null) return;
        m_seqParent.removeNode(this);
    };
    
    /**
     * define the parent sequence for this node
     */
    protected void  setParent(AcceleratorSeq parent)   {
        removeFromParent();
        m_seqParent = parent;
    };
    
    /**
     * set the top level accelerator for this node
     */
    protected void setAccelerator(Accelerator accel) {
        if ( m_objAccel != null ) {
            m_objAccel.nodeRemoved(this);
        }
        
        m_objAccel = accel;
        
        if ( accel != null ) {
            accel.nodeAdded(this);
        }
    }
    
    
    
    
    /** channel suite accessor */
    public ChannelSuite channelSuite() {
        return channelSuite;
    }
    
    
    /** accessor to channel suite handles */
    public Collection<String> getHandles() {
        return channelSuite.getHandles();
    }
    
    
    //------- ElementType interface --------------------------------
    
    /** 
     * Determine if a node is of the specified type.  The comparison is based 
     * upon the node's class and the element type manager handles checking 
     * for inherited classes to types get inherited.  Subclasses can override 
     * this method if the types comparison is more complicated (e.g. if more 
     * than one type can be associated with the same node class).
     * @param compType The type against which to compare.
     * @return true if the node is of the specified type; false otherwise.
     */
    public boolean isKindOf(String compType) {
        return ElementTypeManager.defaultManager().match(this.getClass(), compType);
    }
    
    
    /**
     * Determine if the node is a magnet.
     * @return true if the node is a magnet; false other.
     */
    public boolean isMagnet() {
        return false;   // by default, a node is not an magnet
    }
    
    
    //------------------ Object Overrides ------------------------------\\
    
    /**
     * Returns the identifier string of the node.
     * 
     * @return  the physical hardware identifier
     * 
     * @since 	Aug 20, 2009
     * @author  Christopher K. Allen
     *
     * @see    java.lang.Object#toString()
     * @see    AcceleratorNode#getId()
     *
     */
    @Override
    public String       toString()      {
        return this.getId();
    }
}


