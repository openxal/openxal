package xal.smf.impl;

import xal.tools.data.DataAdaptor;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.MagnetBucket;
import xal.smf.impl.qualify.ElementTypeManager;

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
//        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
//        typeManager.registerType(EQuad.class, "EQuad");
    }
    
    public EQuad(String strId) {
		super(strId);
		// TODO Auto-generated constructor stub
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
    public int getOrientation() {
    	if (s_strType.equalsIgnoreCase(SKEW_TYPE))
    		return NO_ORIENTATION;
    	else
    		return s_strType.equalsIgnoreCase( HORIZONTAL_TYPE ) ? HORIZONTAL : VERTICAL;
    }
            
    /*
     * Determine whether this EQuad is oriented horizontally.
     * @return true if this EQuad is oriented horizontally; false otherwise.
     */
    final public boolean isHorizontal() {
        return getOrientation() == HORIZONTAL;
    }
    
    /*
     * Determine whether this EQuad is oriented vertically.
     * @return true if this EQuad is oriented vertically; false otherwise.
     */
    final public boolean isVertical() {
        return getOrientation() == VERTICAL;
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
