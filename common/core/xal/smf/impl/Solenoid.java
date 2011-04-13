package xal.smf.impl;

import xal.tools.data.IDataAdaptor;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.smf.impl.qualify.MagnetType;

public class Solenoid extends Electromagnet {
    public static final String      s_strType   = "SOL";
	// static initializer
    static {
        registerType();
    }

    
    /** Register type for qualification */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(Solenoid.class, s_strType);
        typeManager.registerType(Solenoid.class, MagnetType.SOLENOID);
    }
  
    /**
     * Constructor
	 * @param strID the dipole's unique ID
     */    
    public Solenoid( final String strID )     { 
        super( strID );
        
    }

	@Override
	public String getType() {
		return s_strType;
	}

/*	public void update(IDataAdaptor adaptor) {
		
	}
*/    
  

}
