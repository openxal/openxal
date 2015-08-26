/*
 * RFGap.java
 *
 * Created on March 18, 2003, 12:42 AM
 */

package xal.sim.slg;

/**
 * The RF gap element (a slim element). 
 *
 * @author  wdklotz
 */
public class RFGap extends ThinElement {
    private static final String type="rfgap";
    
    /** Creates a new instance of RFGap */
    public RFGap(double position,double len, String name) {
        super(name,position,len);
		if (len == 0.0) {
			handleAsThick = false;
		} else {
			handleAsThick = true;
		}
    }
    
    /** Creates a new instance of RFGap */
    public RFGap(double position,double len) {
        this(position,len,"RFG");
    }
    
    /** Creates a new instance of RFGap */
    public RFGap(double position) {
        this(position,0.0);
    }
    
    /** Creates a new instance of RFGap */
    public RFGap(Double position,Double len, String name) {
        this(position.doubleValue(),len.doubleValue(),name);
    }
    
    /** Creates a new instance of RFGap */
    public RFGap(Double position,Double len) {
        this(position.doubleValue(),len.doubleValue());
    }
    
    /** Creates a new instance of RFGap */
    public RFGap(Double position) {
        this(position.doubleValue());
    }
    
    /**
     * Return the element type.
     */
    public String getType() {
        return type;
    } 

    public String toCoutString() {
        //NumberFormat fmt=NumberFormat.getNumberInstance();
        //((DecimalFormat)fmt).applyPattern("0.0000");
        String retval="";
        double el_pos=getPosition();
        double el_len=getEffLength();
		double a_start = toAbsolutePosition(getStartPosition());
        String name=getName();
        String type=getType();
        retval +="s="+fmt.format(a_start)+" m\t"+name+"\t"+type+" p="+fmt.format(el_pos)+" leff="+fmt.format(el_len);
        return retval;
    }

    /**  
     * Implementation of interface xal.tools.data.DataListener:
     * Instructs the implementor to write its data to the adaptor for external
     * storage.
     */
    /*public void write(DataAdaptor adaptor) {
        super.write(adaptor);
        DataAdaptor parameterAdaptor;
        RfGap rfgap=(RfGap)this.getAcceleratorNode();
        //parameter 
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","frequency");
        parameterAdaptor.setValue("type","double");
        parameterAdaptor.setValue("value",Double.toString(rfgap.getGapDfltFrequency()));
        //parameter         
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","phase");
        parameterAdaptor.setValue("type","double");
        parameterAdaptor.setValue("value",Double.toString(rfgap.getGapDfltPhase()));
        //parameter         
        parameterAdaptor=adaptor.createChild("Parameter");
        parameterAdaptor.setValue("name","eTL");
        parameterAdaptor.setValue("type","double");
        parameterAdaptor.setValue("value",Double.toString(rfgap.getGapDfltE0TL()));
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
