package eu.ess.jels.smf.impl;

import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import eu.ess.jels.smf.attr.Magnet2Bucket;

public class Bend extends xal.smf.impl.Bend {
	
	/** horizontal dipole type */
    public static final String HORIZONTAL_TYPE = "DH";
	
	/** vertical dipole type */
    public static final String VERTICAL_TYPE = "DV";
	
    /** the type of dipole (horizontal or vertical) */
    protected String _type;
	
    
    static {
        registerType();
    }
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Bend.class, "D");
    }
    
	
	private Magnet2Bucket magnet2Bucket = new Magnet2Bucket();
	
	public Bend(String strId) {
		this(strId, HORIZONTAL);
	}
	
	public Bend(String strId, int orientation) {
		super(strId);		
		this._type = orientation == HORIZONTAL ? HORIZONTAL_TYPE : VERTICAL_TYPE;
		setMagBucket(magnet2Bucket);
	}
	
	
    /** 
     * Override to provide the correct type signature per instance.  This is 
     * necessary since the Dipole class can represent more than one 
     * official type (DH or DV).
     * @return The official type consistent with the naming convention.
     */
    public String getType()   { 
        return _type; 
    }
	
    public void setOrientation(int orientation)
    {
    	this._type = orientation == HORIZONTAL ? HORIZONTAL_TYPE : VERTICAL_TYPE;
    }
    
    /**
     * Update the instance with data from the data adaptor.  Overrides the default implementation to 
	 * set the dipole type since a dipole type can be either "DH" or "DV".
     * @param adaptor The data provider.
     */
    public void update( final DataAdaptor adaptor ) {
        if ( adaptor.hasAttribute( "type" ) ) {
            _type = adaptor.stringValue( "type" );
        }
        super.update( adaptor );
    }

	public double getGap() {
		return magnet2Bucket.getGap();
	}

	public void setGap(double value) {
		magnet2Bucket.setGap(value);
	}

	public double getEntrK1() {
		return magnet2Bucket.getEntrK1();
	}

	public void setEntrK1(double value) {
		magnet2Bucket.setEntrK1(value);
	}

	public double getEntrK2() {
		return magnet2Bucket.getEntrK2();
	}

	public void setEntrK2(double value) {
		magnet2Bucket.setEntrK2(value);
	}

	public double getExitK1() {
		return magnet2Bucket.getExitK1();
	}

	public void setExitK1(double value) {
		magnet2Bucket.setExitK1(value);
	}

	public double getExitK2() {
		return magnet2Bucket.getExitK2();
	}

	public void setExitK2(double value) {
		magnet2Bucket.setExitK2(value);
	}	
	
	
	   
    /**
     * Get the orientation of the magnet as defined by MagnetType.  The orientation
     * of the dipole is determined by its type: DH or DV
     * @return One of HORIZONTAL or VERTICAL
     */
    public int getOrientation() {    	
    	return _type.equalsIgnoreCase( HORIZONTAL_TYPE ) ? HORIZONTAL : VERTICAL;
    }
        
    
    /** 
     * Determine if this node is of the specified type.  Override the default method since a dipole 
	 * could represent either a vertical or horizontal type.  Must also handle inheritance checking so 
	 * we must or the direct type comparison with the inherited type checking.
     * @param type The type against which to compare this quadrupole's type.
     * @return true if the node is a match and false otherwise.
     */
    public boolean isKindOf( final String type ) {
        return type.equalsIgnoreCase( _type ) || super.isKindOf( type );
    }
}
