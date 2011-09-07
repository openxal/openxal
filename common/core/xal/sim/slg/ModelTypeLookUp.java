/*
 * ModelTypeLookUp.java
 *
 * Created on April 2, 2003, 4:57 PM
 */

package xal.sim.slg;

import java.util.HashMap;
import java.util.Map;
/**
 * A lookup table that maps element ids as used by this lattice generator
 * to element ids as used by the on-line model for its xml presentation.
 *
 * @author  wdklotz
 */
public class ModelTypeLookUp implements Map<String,String> {
    private final static Map<String,String> map;
    
    static {
        map=new HashMap<String,String>();
        map.put("marker","Marker");
        map.put("pmarker","Marker");
        map.put("VIW", "Marker");
        map.put("Harp", "Marker");
        map.put("Foil", "Marker");
        map.put("Tgt", "Marker");
        map.put("drift","IdealDrift");
        map.put("dipole","ThickDipole");
        map.put("quadrupole","IdealMagQuad");
        map.put("sextupole","IdealMagSextupole");
        map.put("octupole","IdealMagOct");
        map.put("hsteerer","IdealMagSteeringDipole");
        map.put("vsteerer","IdealMagSteeringDipole");
        map.put("skewquadrupole","SkewMagQuad");
        map.put("skewsextupole","SkewMagSex");
        map.put("beampositionmonitor","Marker");
        map.put("beamcurrentmonitor","Marker");
        map.put("beamlossmonitor","Marker");
        map.put("rfgap","IdealRfGap");
        map.put("wirescanner","Marker");
    }
    
    /** Creates a new instance of ModelTypeMap */
    public ModelTypeLookUp() {
    }
    
    public void clear() {
    }
    
    public boolean containsKey(Object obj) {
        return map.containsKey(obj);
    }
    
    public boolean containsValue(Object obj) {
        return map.containsValue(obj);
    }
    
    public java.util.Set<Map.Entry<String,String>> entrySet() {
        return map.entrySet();
    }
    
    public String get( final Object key ) {
        return map.get(key);
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public java.util.Set<String> keySet() {
        return map.keySet();
    }
    
    public String put(String obj, String obj1) {
        return  null;
    }
    
    public void putAll(java.util.Map<? extends String,? extends String> map) {
    }
    
    public String remove(Object obj) {
        return null;
    }
    
    public int size() {
        return map.size();
    }
    
    public java.util.Collection<String> values() {
        return map.values();
    }
    
    public String ValueForKey(String key) {
        return get(key);
    }
    
}
