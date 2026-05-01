package xal.sim.slg;

public class EQuad extends Element {

    private static final String type="EQuad";

//	protected EQuad(String name, double position, double len) {
//		super(name, position, len);
//		// TODO Auto-generated constructor stub
//	}

    /** Creates a new instance of Electrostatic Quadrupole */
    public EQuad(double position, double len, String name) {
        super(name,position,len);
		handleAsThick = true;
   }
    
    /** Creates a new instance of Electrostatic Quadrupole */
    public EQuad(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Electrostatic Quadrupole */
    public EQuad(double position, double len) {
        this(position,len,"NQP");
    }
    
    /** Creates a new instance of Electrostatic Quadrupole */
    public EQuad(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
	@Override
    /*
     * Getter for the element type property.
     */
    public String getType() {
        return type;
    }

	@Override
	public void accept(Visitor v) {
        v.visit(this);
	}

}
