/*
 *
 * Quadrupole.java
 *
 * Created on March 17, 2003, 11:22 PM
 */

package xal.sim.slg;

/**
 * The quadrupole element (a thick element).
 *
 * @author  wdklotz
 */
public class Quadrupole extends Element {
    private static final String type="quadrupole";
    
    /** Creates a new instance of Quadrupole */
    public Quadrupole(double position, double len, String name) {
        super(name,position,len);
		handleAsThick = true;
   }
    
    /** Creates a new instance of Quadrupole */
    public Quadrupole(Double position, Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of Quadrupole */
    public Quadrupole(double position, double len) {
        this(position,len,"NQP");
    }
    
    /** Creates a new instance of Quadrupole */
    public Quadrupole(Double position, Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /*
     * Getter for the element type property.
     */
    public String getType() {
        return type;
    }

    /**  
     * Implementation of interface xal.tools.data.DataListener:
     * Instructs the implementor to write its data to the adaptor for external
     * storage.
     */
    /*public void write(DataAdaptor adaptor) {
        super.write(adaptor);
        DataAdaptor parameterAdaptor;
        Magnet magnet=(Magnet)this.getAcceleratorNode();
        //parameter 
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","magField");
        parameterAdaptor.setValue("type","double");
        parameterAdaptor.setValue("value",Double.toString(magnet.getDesignField()));
        //parameter         
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","effLength");
        parameterAdaptor.setValue("type","double");
        double effLen=magnet.getEffLength()*this.getLength()/magnet.getLength();
        parameterAdaptor.setValue("value",Double.toString(effLen));
        //parameter         
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","orientation");
        parameterAdaptor.setValue("type","int");
        IElectromagnet elmg=new IdealMagQuad();
        int orientation=elmg.ORIENT_NONE;
        if(magnet.isHorizontal()) { orientation=elmg.ORIENT_HOR;}
        if(magnet.isVertical()) { orientation=elmg.ORIENT_VER;}
        parameterAdaptor.setValue("value",Integer.toString(orientation));
    }*/
    
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
