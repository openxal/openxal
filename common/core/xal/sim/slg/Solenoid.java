package xal.sim.slg;

/**
 * The solenoid element (a thick element).
 *
 * @author  chu
  */
public class Solenoid extends Element {
    private static final String type="solenoid";
    
    /** Creates a new instance of Quadrupole */
    public Solenoid(double position, double len, String name) {
        super(name,position,len);
		handleAsThick = true;
   }
    
    /** Creates a new instance of Solenoid */
    public Solenoid(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Solenoid */
    public Solenoid(double position, double len) {
        this(position,len,"SOL");
    }
    
    /** Creates a new instance of Solenoid */
    public Solenoid(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /*
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
