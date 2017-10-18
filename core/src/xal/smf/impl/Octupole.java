package xal.smf.impl;

import xal.tools.data.DataAdaptor;
import xal.ca.ChannelFactory;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.MagnetType;

/**
 *  Ocutpole magnet node.
 *  @author Liu Weibin
 */
public class Octupole extends Electromagnet {
    // Constants
    public static final String s_strType   = "O";
    public static final String HORIZONTAL_TYPE = "OH";
    public static final String VERTICAL_TYPE = "OV";
    public static final String HORIZONTAL_SKEW_TYPE = "OSH";
    public static final String VERTICAL_SKEW_TYPE = "OSV";

    /** identifies the type of octupole (horizontal, vertical, skew) */
    protected String _type;
    

    // static initializer
    static {
        registerType();
    }
    
    
    /**
     * Register type for qualification.  These are the types that are common to all instances.  
     * The <code>isKindOf</code> method handles the type qualification specific to an instance.
     * @see #isKindOf
     */
     private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType( Octupole.class, s_strType );
        typeManager.registerType( Octupole.class, "emoct" );
        typeManager.registerType( Octupole.class, "oct" );
        typeManager.registerType( Octupole.class, "octupole" );
        typeManager.registerType( Octupole.class, MagnetType.OCTUPOLE );
    }
    
     /**
      * Constructor
      * @param strID unique node ID
      */
    public Octupole(String strId, final ChannelFactory channelFactory) {
        super(strId, channelFactory );
    }

    /**
     * Constructor
     * @param strID unique node ID
     */
   public Octupole(String strId) {
       super(strId);
   }
   
    /** 
     * Override to provide the correct type signature per instance.  This is 
     * necessary since the Octupole class can represent more than one 
     * official type (OH or OV).
     * @return The official type consistent with the naming convention.
     */
    public String getType() {
        return _type;
    }
    
    /**
     * Update the instance with data from the data adaptor.  Overrides the default implementation to 
     * set the octupole type since a octupole type can be either "OH" or "OV".
     * @param adaptor The data provider.
     */
    public void update( final DataAdaptor adaptor ) {
        if ( adaptor.hasAttribute( "type" ) ) {
            _type = adaptor.stringValue( "type" );
        }
        super.update( adaptor );
    }
    
    /*
     * Determine whether this magnet is of the pole specified.
     * @param compPole The pole against which this magnet is being compared.
     * @return true if this magnet matches the specified pole.
     */ 
    public boolean isPole( final String pole ) {
        return pole.equals( MagnetType.OCTUPOLE );
    }

    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation of the octupole is determined by its type.
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {
        return ( _type.equalsIgnoreCase( HORIZONTAL_TYPE ) || _type.equalsIgnoreCase( HORIZONTAL_SKEW_TYPE ) ) ? HORIZONTAL : VERTICAL;
    }
    
    
    /**
     * Determine whether this magnet is a skew magnet.
     * @return true if the magnet is skew and false otherwise.
     */
    public boolean isSkew() {
        return _type.equalsIgnoreCase( HORIZONTAL_SKEW_TYPE ) || _type.equalsIgnoreCase( VERTICAL_SKEW_TYPE );
    }
    
    
    /** 
     * Determine if this node is of the specified type.  Override the default method since a octupole could represent 
     * either a vertical or horizontal type.  Must also handle inheritance checking so we must or the direct 
     * type comparison with the inherited type checking.
     * @param type the type against which to compare this octupole
     * @return true if the node is a match and false otherwise.
     */
    public boolean isKindOf( final String type ) {
        return type.equalsIgnoreCase( _type ) || super.isKindOf( type );
    }
}
