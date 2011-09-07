/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationManager;
import xal.smf.AcceleratorNode;
import xal.smf.impl.RfGap;

/**
 * Returns property values for RfGap nodes.
 * 
 * @author Craig McChesney
 */
public class RfGapPropertyAccessor implements PropertyAccessor {
	
	// Constants ===============================================================
	
	// Property Names
	
	public static final String PROPERTY_ETL = "ETL";
	public static final String PROPERTY_PHASE = "Phase";
	public static final String PROPERTY_FREQUENCY = "Frequency";
	public static final String PROPERTY_E0 = "Field";
	
	
	// Method Names
	
	public static final String METHOD_LIVE_ETL = "getGapE0TL";	
	public static final String METHOD_LIVE_PHASE = "getGapPhaseAvg";	
	public static final String METHOD_LIVE_FREQUENCY = "getGapDfltFrequency";	
	public static final String METHOD_LIVE_E0 = "getGapAmpAvg";		
	public static final String METHOD_DESIGN_ETL = "getGapDfltE0TL";	
	public static final String METHOD_DESIGN_PHASE = "getGapDfltPhase";	
	public static final String METHOD_DESIGN_FREQUENCY = "getGapDfltFrequency";	
	public static final String METHOD_DESIGN_E0 = "getGapDfltAmp";		
	// Scaling Factors for unit conversion
	
	public static final double SCALE_ETL = 1.e6;
	public static final double SCALE_PHASE = Math.PI/180.;
	public static final double SCALE_FREQUENCY = 1.e6;
	public static final double SCALE_E0= 1.e6; 
	
	
	// Static Variables ========================================================
	
	private static ArrayList<String> propertyNames;
	
	private static HashMap<String,PropertyProxy> liveProxies;
	private static HashMap<String,PropertyProxy> designProxies;
	
	
	// Static Initialization ===================================================
	
	static {
		propertyNames = new ArrayList<String>();
		propertyNames.add(PROPERTY_ETL);
		propertyNames.add(PROPERTY_PHASE);
		propertyNames.add(PROPERTY_FREQUENCY);
		propertyNames.add(PROPERTY_E0);
		
		initLiveProxies();
		
		initDesignProxies();
	}
	
	private static void initLiveProxies() {
		liveProxies = new HashMap<String,PropertyProxy>(propertyNames.size());
		liveProxies.put(PROPERTY_ETL, 
			new PropertyProxy(RfGap.class, METHOD_LIVE_ETL, SCALE_ETL));
		liveProxies.put(PROPERTY_PHASE,
			new PropertyProxy(RfGap.class, METHOD_LIVE_PHASE, SCALE_PHASE));
		liveProxies.put(PROPERTY_FREQUENCY,
			new PropertyProxy(RfGap.class, METHOD_LIVE_FREQUENCY, SCALE_FREQUENCY));
		liveProxies.put(PROPERTY_E0,
			new PropertyProxy(RfGap.class, METHOD_LIVE_E0, SCALE_E0));			
	}
	
	private static void initDesignProxies() {
		designProxies = new HashMap<String,PropertyProxy>(propertyNames.size());
		designProxies.put(PROPERTY_ETL, 
			new PropertyProxy(RfGap.class, METHOD_DESIGN_ETL, SCALE_ETL));
		designProxies.put(PROPERTY_PHASE,
			new PropertyProxy(RfGap.class, METHOD_DESIGN_PHASE, SCALE_PHASE));
		designProxies.put(PROPERTY_FREQUENCY,
			new PropertyProxy(RfGap.class, METHOD_DESIGN_FREQUENCY, SCALE_FREQUENCY));
		designProxies.put(PROPERTY_E0,
			new PropertyProxy(RfGap.class, METHOD_DESIGN_E0, SCALE_E0));			
	}
	
	
    // PropertyAccessor Interface ==============================================

	@Override
    public double doubleValueFor(AcceleratorNode node, String property, String mode) throws ProxyException {
        PropertyProxy proxy = null;
        if (mode.equals(Scenario.SYNC_MODE_LIVE)) {
            proxy = liveProxies.get(property);
        } else if (mode.equals(Scenario.SYNC_MODE_DESIGN)) {
            proxy = designProxies.get(property);
        } else if (mode.equals(Scenario.SYNC_MODE_RF_DESIGN)) {
            proxy = designProxies.get(property);
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
