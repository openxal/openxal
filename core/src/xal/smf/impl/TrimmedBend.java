package xal.smf.impl;

import java.util.Collection;
import java.util.HashSet;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.smf.impl.qualify.ElementTypeManager;

/**
 * TrimmedBend is a subclass of Bend that has a trim power supply in addition to a main power supply.
 * @author Liu Weibin
 */
public class TrimmedBend extends Bend implements TrimmedMagnet {
    public static String      s_strType   = "DTH";

    /** horizontal quadrupole type */
    public static final String HORIZONTAL_TYPE = "XTBEND";
    
    /** vertical quadrupole type */
    public static final String VERTICAL_TYPE = "YTBEND";
    
    /** unique ID for this magnet's trim supply */
    protected String _trimSupplyID = "";
    
    private String _type = HORIZONTAL_TYPE;
    
    static {
        registerType();
    }
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerTypes(TrimmedBend.class, s_strType, "trimmedbend");
//        typeManager.registerType(TrimmedBend.class, "trimmedbend");
    }
    
    /**
     * Constructor
     * @param strID this magnet's unique node ID
     */
    public TrimmedBend(String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory);
    }

    /**
     * Constructor
     * @param strID this magnet's unique node ID
     */
    public TrimmedBend(String strId) {
        super(strId);
    }

    /**
     * Update data from the power supply data adaptor.
     * @param powerSupplyAdaptor The data provider of power supply information.
     */
    protected void updatePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        super.updatePowerSupplies( powerSupplyAdaptor );
        _trimSupplyID = powerSupplyAdaptor.stringValue( "trim" );
    }
    
    /**
     * Write data to the power supply data adaptor.
     * @param powerSupplyAdaptor The data sink for the power supply information
     */
    protected void writePowerSupplies( final DataAdaptor powerSupplyAdaptor ) {
        super.writePowerSupplies( powerSupplyAdaptor );
        powerSupplyAdaptor.setValue( "trim", _trimSupplyID );
    }
    
    /** 
     * Get the channel handles.  Overrides the default method to add handles from the trim power supply.
     * @return The channel handles associated with this node
     */
    public Collection<String> getHandles() {
        final Collection<String> handles = new HashSet<String>( super.getHandles() );
        try {
            handles.addAll( getTrimSupply().getChannelSuite().getHandles() );
        }
        catch(NullPointerException exception) {
            System.err.println("exception getting handles from the trim supply \"" + getTrimSupply() + "\" for trimmed bend: " + getId());
            throw exception;
        }
        return handles;
    }
    
    /**
     * Find the channel for the specified handle searching the trim supply if necessary.
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     */
    public Channel findChannel( final String handle ) {
        final Channel channel = super.findChannel( handle );
        return channel != null ? channel : getTrimSupply().getChannelSuite().getChannel( handle );
    }
    
    /**
     * Get the trim power supply for this magnet.
     * @return The trim power supply for this magnet
     */
    public MagnetTrimSupply getTrimSupply() {
        return getAccelerator().getMagnetTrimSupply( _trimSupplyID );
    }
    
    /** 
     * Set the trim power supply field contribution in the magnet.  If cycle enable 
     * is true then the magnet is cycled before the field is set to the specified value.
     * @param newField is the new field level in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */  
    public void setTrimField( final double newField ) throws ConnectionException, PutException {
        getTrimSupply().setField( toCAFromField( newField ) );
    }
    
    /**
     * Get the value to which the trim supply's field contribution is set.  Note that this is not the readback.
     * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */
    public double getTrimFieldSetting() throws ConnectionException, GetException {
        return toFieldFromCA( getTrimSupply().getFieldSetting() );
    }
    
    
    /**
     * Get the value to which the field is set including both the main supply and trim supply contributions.  
     * Note that this is not the readback.
     * @return the field setting in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc.
     */
    public double getTotalFieldSetting() throws ConnectionException, GetException {
        return getFieldSetting() + getTrimFieldSetting();
    }
    
    /** 
     * Get the field in this electromagnet via ca.
     * @return the readback field in T/(m^(n-1)), where n = 1 for dipole, 2 for quad, etc. 
     */
    @Override
    public double getFieldReadback() throws ConnectionException, GetException {
        Channel fieldRBChannel = getAndConnectChannel( FIELD_RB_HANDLE );
        Channel fieldTrimRBChannel = getAndConnectChannel( MagnetTrimSupply.FIELD_RB_HANDLE );
        
        return toFieldFromCA( fieldRBChannel.getValDbl() + fieldTrimRBChannel.getValDbl());
    }

    /** Get the trim power supply current in this electromagnet via ca (A) */
    public double getTrimCurrent() throws ConnectionException, GetException {
        return getTrimSupply().getCurrent();
    }
    
    
    /** 
     * set the trim power supply current in the magnet (A)
     * @param newCurrent is the new current (A)
     */  
    public void setTrimCurrent( final double newCurrent ) throws ConnectionException, PutException {
        getTrimSupply().setCurrent( newCurrent );
    }
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the bend is determined by its type: XTBEND or YTBEND
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
        return ( _type.equalsIgnoreCase( HORIZONTAL_TYPE ) ) ? HORIZONTAL : VERTICAL;
    }

}
