/*
 *
 * Drift.java
 *
 * Created on March 17, 2003, 11:08 PM
 */

package xal.sim.slg;

/**
 * The drift space element (a thick element).
 *
 * @author  wdklotz
 */
public class Drift extends Element {
    private static final String type="drift";
    
    /** Creates a new instance of Drift */
    public Drift(double position, double len, String name) {
        super(name,position,len);
        handleAsThick = true;
    }
    
    /** Creates a new instance of Drift */
    public Drift(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Drift */
    public Drift(double position, double len) {
        this(position,len,"DRFT");
    }
    
    /** Creates a new instance of Drift */
    public Drift(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
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
