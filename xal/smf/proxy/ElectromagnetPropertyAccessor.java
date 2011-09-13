/*
 * Created on Oct 24, 2003
 */
package xal.smf.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xal.sim.scenario.Scenario;
import xal.smf.AcceleratorNode;
import xal.smf.impl.Electromagnet;

/**
 * @author Craig McChesney
 */
public class ElectromagnetPropertyAccessor implements PropertyAccessor {
	
	// Constants ===============================================================
	
	// Property Names
	
	public static final String PROPERTY_FIELD = "Field";
	
	// Method Names
	
	public static final String METHOD_LIVE_FIELD = "getField";	
	public static final String METHOD_DESIGN_FIELD = "getDesignField";	
	
	// Static Variables ========================================================
	
	private static ArrayList<String> propertyNames;
	
	private static HashMap<String,PropertyProxy> liveProxies;
	private static HashMap<String,PropertyProxy> designProxies;
	
	
	// Static Initialization ===================================================
	
	static {
		propertyNames = new ArrayList<String>();
		propertyNames.add(PROPERTY_FIELD);
		
		initLiveProxies();
		
		initDesignProxies();
	}
	
	private static void initLiveProxies() {
		liveProxies = new HashMap<String,PropertyProxy>(propertyNames.size());
		liveProxies.put(PROPERTY_FIELD, 
			new PropertyProxy(Electromagnet.class, METHOD_LIVE_FIELD));
	}
	
	private static void initDesignProxies() {
		designProxies = new HashMap<String,PropertyProxy>(propertyNames.size());
		designProxies.put(PROPERTY_FIELD, 
			new PropertyProxy(Electromagnet.class, METHOD_DESIGN_FIELD));
	}
	
	
	// PropertyAccessor Interface ==============================================

	public double doubleValueFor(AcceleratorNode node, String property, 
                                 String mode) throws ProxyException {
        
		PropertyProxy proxy = null;
		if (mode.equals(Scenario.SYNC_MODE_LIVE)) { 
			proxy = liveProxies.get(property);
		} else if (mode.equals(Scenario.SYNC_MODE_DESIGN)) {
			proxy = designProxies.get(property);
        } else if (mode.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
            proxy = liveProxies.get(property);
		} else {
			throw new IllegalArgumentException("Unknown mode: " + mode);
		}
		if (proxy == null)
			throw new IllegalArgumentException("Unknown property: " + property);
		return proxy.doubleValueFor(node);
	}
	
	
	// Accessing ===============================================================
	
    @SuppressWarnings( "unchecked" )    // clone doesn't support generics, so we must cast
	public List<String> propertyNames() {
		return (List<String>) propertyNames.clone();
	}

}
