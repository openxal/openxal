/*
 * PermMarker.java
 *
 * Created on March 22, 2003, 2:27 PM
 */

package xal.sim.slg;

/**
 * The permanent marker element (a thin element)
 *
 * @author  wdklotz
 */
public class PermMarker extends ThinElement {
    private static final String type="pmarker";
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(double position, double len, String name) {
        super(name,position,0.0);
		handleAsThick = false;
    }
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(double position, double len) {
        this(position,len,"===");
    }
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(double position) {
        this(position,0.0);
    }
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /** Creates a new instance of PermanentMarker */
    public PermMarker(Double position) {
        this(position.doubleValue());
    }
    
    /**
     * Getter for the element type property.
     */
    public String getType() {
        return type;
    } 
    
    /**
     * When called with a Visitor reference the implementor can either
     * reject to be visited (empty method body) or call the Visitor by
     * passing its own object reference.
     *
     *@param v the Visitor which wants to visit this object.
     */
    public void accept(Visitor v) {
        v.visit( this );
    }
    
}
