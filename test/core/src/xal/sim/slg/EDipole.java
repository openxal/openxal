package xal.sim.slg;

public class EDipole extends Element {

    private static final String type="EDipole";

    protected EDipole(double position, double len, String name) {
		// TODO Auto-generated constructor stub
        super(name,position,len);
		handleAsThick = true;
	}

    /** Creates a new instance of Electrostatic Dipole */
    public EDipole(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Electrostatic Dipole */
    public EDipole(double position, double len) {
        this(position,len,"NQP");
    }
    
    /** Creates a new instance of Electrostatic Dipole */
    public EDipole(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
	@Override
	public String getType() {
		// TODO Auto-generated method stub
        return type;
	}

	@Override
	public void accept(Visitor v) {
		// TODO Auto-generated method stub
        v.visit(this);
		
	}

}
