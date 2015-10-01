package xal.smf.impl;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.tools.data.DataAdaptor;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.MagnetBucket;
import xal.smf.impl.qualify.ElementTypeManager;


public class Electrostatic extends AcceleratorNode {
	/** standard type for instances of this class */
	public static final String s_strType   = "estat";

    // orientation constants
    public final static int NO_ORIENTATION = 0;
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

    /**
     * The container for the magnet information
     *
     */   
    protected MagnetBucket       m_bucMagnet; 

	/** indicates whether to use the actual field readback or the field setting in the getField() method */
	protected boolean _useFieldReadback;
    
    /** field readback handle */
    public static final String FIELD_RB_HANDLE = "fieldRB";
    
    /** the ID of this magnet's main power supply */
    protected String mainSupplyId;


	// static initialization
	static {
		registerType();
	}


	// Register types for qualification
	private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Electromagnet.class, s_strType, "electrostatic" );
	}


	/** Primary Constructor */
	public Electrostatic( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


	/** Constructor */
	public Electrostatic( final String strId ) {
		this( strId, null );
	}


    /**
     *  
     * @return    the attribute bucket containing the machine multipole fields
     */

    public MagnetBucket  getMagBucket()   { return m_bucMagnet; };

 
    /**
    *
    * Override AcceleratorNode implementation to check for a MultipoleBucket
    */

   public void addBucket(AttributeBucket buc)  {

       if (buc.getClass().equals(MagnetBucket.class))
             setMagBucket((MagnetBucket)buc);

       super.addBucket(buc);
   };    

   /**
     *  
     * Set the attribute bucket containing the machine magnet info
     */

    public void setMagBucket(MagnetBucket buc) 
        { m_bucMagnet = buc; super.addBucket(buc); };
    
	@Override
	public String getType() {
		return s_strType;
	}

    /**
     * get the design field for the magnet (T for dipole, T/m for quad, etc.)
     */

    public double getDesignField() {
        return m_bucMagnet.getDfltField() ;
    }

    /** 
     * get the effective magnetic length (m)
     */

    public double getEffLength() {
        return m_bucMagnet.getEffLength() ;
    } 
    
    /**
     * get the default magnetic field
     */       
    public double getDfltField() {
        return m_bucMagnet.getDfltField() ;
    }
    
    /** 
	 * Get the field in this electromagnet via ca.
	 * @return the field in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
	 */
    public double getField() throws ConnectionException, GetException {
        return ( _useFieldReadback ) ? getFieldReadback() : getTotalFieldSetting();
    }
    
	/**
	 * Get the value to which the field is set including both the main supply and possible trim supply contributions.  
	 * Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getTotalFieldSetting() throws ConnectionException, GetException {
        return getFieldSetting();
	}
	
    /** 
	 * Get the field in this electromagnet via ca.
	 * @return the readback field in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
	 */
    public double getFieldReadback() throws ConnectionException, GetException {
        Channel fieldRBChannel = getAndConnectChannel( FIELD_RB_HANDLE );
        
        return toFieldFromCA( fieldRBChannel.getValDbl() );
    }
    
	/**
	 * Get the value to which the main power supply's field contribution is set.  Note that this is not the readback.
	 * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
	 */
	public double getFieldSetting() throws ConnectionException, GetException {
        return toFieldFromCA( getMainSupply().getFieldSetting() );
	}
	
    /**
     * Get the main power supply for this magnet.
     * @return The main power supply for this magnet
     */
    public MagnetMainSupply getMainSupply() {
        return getAccelerator().getMagnetMainSupply( mainSupplyId );
    }
    
    /**
     * get magnet polarity
     */
    public double   getPolarity() {
        try{
            return m_bucMagnet.getPolarity();
        }  
        catch (Exception e) {	    
            System.out.println(" Polarity not set on " + this.getId() 
                           + ", for stability sake, using + field");
            return 1;
        }
    }

	/**
	 * Convert the raw channel access value to get the field.
	 * @param rawValue the raw channel value
	 * @return the magnetic field in T/m^(n-1)
	 */
	final public double toFieldFromCA( final double rawValue ) {
        return rawValue * getPolarity(); 
	}
	
	
	/**
	 * Convert the field value to a channel access value.
	 * @param field the magnetic field in T/m^(n-1)
	 * @return the channel access value
	 */
	final public double toCAFromField( final double field ) {
        return field * getPolarity(); 
	}
	
    /**
     * Write data to the power supply data adaptor.  Put the information about
     * the main power supply into the data adaptor.
     * @param powerSupplyAdaptor The data sink for the power supply information
     */
    protected void writePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        powerSupplyAdaptor.setValue( "main", mainSupplyId );
    }
	
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the quad is determined by its type: QH or QV
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
    	return NO_ORIENTATION;
    }
    
    /*
     * Determine whether this EQuad is oriented horizontally.
     * @return true if this EQuad is oriented horizontally; false otherwise.
     */
    public boolean isHorizontal() {
        return getOrientation() == HORIZONTAL;
    }
    
    /*
     * Determine whether this EQuad is oriented vertically.
     * @return true if this EQuad is oriented vertically; false otherwise.
     */
    public boolean isVertical() {
        return getOrientation() == VERTICAL;
    }    
}
