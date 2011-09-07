/*
 * Octupole.java
 *
 * Created on March 17, 2003, 11:37 PM
 */

package xal.sim.slg;

/**
 * The octupole element (a thick element).
 *
 * @author  wdklotz
 */
public class Octupole extends Element {
    private static final String type="octupole";
    
    /** Creates a new instance of Octupole */
    public Octupole(double position, double len, String name) {
        super(name,position,len);
		handleAsThick = true;
    }
    
    /** Creates a new instance of Octupole */
    public Octupole(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Octupole */
    public Octupole(double position, double len) {
        this(position,len,"OCT");
    }
    
    /** Creates a new instance of Octupole */
    public Octupole(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /*
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
