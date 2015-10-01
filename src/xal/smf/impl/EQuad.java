package xal.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.ca.ChannelFactory;

public class EQuad extends Electrostatic {

    public static String      s_strType   = "EQuad";

	/** horizontal quadrupole type */
    public static final String HORIZONTAL_TYPE = "QHE";
	
	/** vertical quadrupole type */
    public static final String VERTICAL_TYPE = "QVE";
    
    /**
     * skew quadrupole type
     */
    public static final String SKEW_TYPE = "QSE";
    	
    static {
        registerType();
    }
    
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( EQuad.class, s_strType );
    }


	/** Primary Constructor */
	public EQuad( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


	/** Constructor */
    public EQuad( final String strId ) {
		this( strId, null );
	}


	@Override
	public String getType() {
		return s_strType;
	}

    
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the quad is determined by its type: QH or QV
     * @return One of HORIZONTAL or VERTICAL
     */
	@Override
    public int getOrientation() {
    	if (s_strType.equalsIgnoreCase(SKEW_TYPE))
    		return NO_ORIENTATION;
    	else
    		return s_strType.equalsIgnoreCase( HORIZONTAL_TYPE ) ? HORIZONTAL : VERTICAL;
    }
  
    /**
     * Update the instance with data from the data adaptor.  Overrides the default implementation to 
	 * set the quadrupole type since a quadrupole type can be either "QHE" or "QVE".
     * @param adaptor The data provider.
     */
    public void update( final DataAdaptor adaptor ) {
    	if ( adaptor.hasAttribute( "type" ) ) {
            s_strType = adaptor.stringValue( "type" );
        }
        super.update( adaptor );
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        // check if this type already registered first.  If not, register it.
        if (!(typeManager.match(EQuad.class, s_strType)))
        	typeManager.registerType(EQuad.class, s_strType);
    }
    
    public boolean isKindOf( final String type ) {
        return type.equalsIgnoreCase( s_strType ) || super.isKindOf( type );
    }
}
