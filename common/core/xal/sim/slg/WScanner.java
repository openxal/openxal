/*
 * WScanner.java
 *
 * Created on March 18, 2003, 1:11 AM
 */

package xal.sim.slg;

/**
 * The wire scanner profile monitor (a thin element).
 *
 * @author  wdklotz
 */
public class WScanner extends ThinElement {
    private static final String type="wirescanner";
    
    /** Creates a new instance of WScanner */
    public WScanner(double position,double len, String name) {
        super(name,position,0.0);
		handleAsThick = false;
    }
    
    /** Creates a new instance of WScanner */
    public WScanner(double position,double len) {
        this(position,len,"WSM");
    }
    
    /** Creates a new instance of WScanner */
    public WScanner(double position) {
        this(position,0.0);
    }
    
    /** Creates a new instance of WScanner */
    public WScanner(Double position,Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of WScanner */
    public WScanner(Double position,Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /** Creates a new instance of WScanner */
    public WScanner(Double position) {
        this(position.doubleValue());
    }
    
    /**
     * Return the element type.
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
