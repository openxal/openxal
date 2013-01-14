package xal.smf.impl;

import xal.smf.*;
import xal.smf.impl.qualify.*;


/**
 * Representation of a Bunch Shape Monitor
 * @author tom pelaia
 */
public class BunchShapeMonitor extends AcceleratorNode {
	/** device type */
	public static final String s_strType = "BSM";
	
	
	// static initializer
	static {
		registerType();
	}
	
	
	/* Register type for qualification */
	private static void registerType() {
		ElementTypeManager.defaultManager().registerType( BunchShapeMonitor.class, s_strType );
	}
	
	
	/** Override to provide type signature */
	public String getType() {
		return s_strType;
	}
	
	
	/** Constructor */
	public BunchShapeMonitor( final String deviceID ) {
		super( deviceID );
	}
}
