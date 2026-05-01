/*
 *
 * HSteerer.java
 *
 * Created on March 18, 2003, 1:01 AM
 */

package xal.sim.slg;

/**
 * The horizontal steerer element (a thin element).
 *
 * @author  wdklotz
 */
public class HSteerer extends ThinElement {
    private static final String type="hsteerer";
    
    /** Creates a new instance of HSteerer */
    public HSteerer(double position,double len, String name) {
        super(name,position,0.0);
		handleAsThick = false;
    }
    
    /** Creates a new instance of HSteerer */
    public HSteerer(double position,double len) {
        this(position,len,"DCH");
    }
    
    /** Creates a new instance of HSteerer */
    public HSteerer(double position) {
        this(position,0.0);
    }
    
    /** Creates a new instance of HSteerer */
    public HSteerer(Double position,Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of HSteerer */
    public HSteerer(Double position,Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /** Creates a new instance of HSteerer */
    public HSteerer(Double position) {
        this(position.doubleValue());
    }
    
    /**
     * Return the element type.
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
        parameterAdaptor.setValue("name","orientation");
        parameterAdaptor.setValue("type","int");
        IElectromagnet elmg=new IdealMagSteeringDipole();
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
