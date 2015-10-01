package xal.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.ca.ChannelFactory;

public class EDipole extends Electrostatic {
	/** standard type for instances of this class */
    public static String      s_strType   = "EDipole";

    // orientation constants
    public final static int NO_ORIENTATION = 0;
    public final static int HORIZONTAL = 1;
    public final static int VERTICAL = 2;

	/** horizontal dipole type */
    public static final String HORIZONTAL_TYPE = "DHE";
	
	/** vertical dipole type */
    public static final String VERTICAL_TYPE = "DVE";


	// static initialization
    static {
        registerType();
    }
    
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( EDipole.class, s_strType );
    }


	/** Primary Constructor */
	public EDipole( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
	}


	/** Constructor */
    public EDipole( final String strId ) {
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
    	return s_strType.equalsIgnoreCase( HORIZONTAL_TYPE ) ? HORIZONTAL : VERTICAL;
    }          
      
    /**
     * Get the dipole bend magnet bending angle.
     */
    public double getDfltBendAngle() {
        return m_bucMagnet.getBendAngle();
    }
    
    /** returns design path length in meters */
    public double getDfltPathLength() {
        return m_bucMagnet.getPathLength();
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
