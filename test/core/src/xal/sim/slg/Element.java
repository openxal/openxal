/*
 *
 * Element.java
 *
 * Created on March 17, 2003, 1:18 PM
 */

package xal.sim.slg;

import xal.smf.AcceleratorNode;

import java.lang.reflect.Constructor;
import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;


/**
 * The super class of all lattice elements.
 *
 * @author  wdklotz
 */
public abstract class Element implements VisitorListener, Cloneable {
    
    /**
     *  <p>
     *  Indicates the component relationship of this model representation
     *  with respect to its hardware counterpart.  For example,
     *  does this element represent the entire hardware
     *  component (value <code>WHOLE</code>) or some subsection.
     *  </p>
     *
     * @since  Sep 2, 2009
     * @author Christopher K. Allen
     */
    public enum SECTION {
        
        /** Component relationship unknown */
        UNKNOWN,
        
        /** Element represents one point of the hardware element */
        POINT,

        /** Element represents upstream end of hardware element */
        UPSTREAM,
        
        /** Element represents downstream end of hardware element */
        DNSTREAM,
        
        /** Element represents whole hardware element */
        WHOLE,
        
        /** Element represent and internal portion of the hardware */ 
        INTERNAL;
    }
    
    
    /** Element's corresponding hardware section */
    private SECTION     secHware;
    
    private final String name;         //the element name
    private double position;     //the relative position in distance units
    private double base;         //the base offset for rel. positions
    private double len;          //the length in distance units
    public static NumberFormat fmt;  //number formater
    private AcceleratorNode xalNode; //the xal AcceleratorNode object
    protected boolean handleAsThick; //flag used by slim elements
    
    /** Creates a new instance of Element */
    protected Element(String name, double position, double len) {
        this.secHware = SECTION.UNKNOWN;
        
        fmt=Lattice.fmt;    // number format is defined in Lattice
        this.name=name;
        this.position=position;   //always relative to base
        this.base=0.0;
        this.len=len;
    }
    
    /**
     * Create a new, initialized <code>Element</code> object.
     *
     * @param secHware   element's hardware subsection 
     * @param name      string identifier for the element
     * @param position  position of the element within the lattice 
     * @param len       length of the element
     *
     * @since     Sep 2, 2009
     * @author    Christopher K. Allen
     */
    protected Element(SECTION secHware, String name, double position, double len) {
        this(name, position, len);
        
        this.secHware = secHware;
    }
    
    @Override
    public Object clone() throws CloneNotSupportedException {
    	return super.clone();
    }
    
    /**
     * The XAL AcceleratorNode property.
     */
    public void setAcceleratorNode(AcceleratorNode node) {
        xalNode=node;
    }
    
    /**
     * The XAL AcceleratorNode property.
     */
    public AcceleratorNode getAcceleratorNode() {
        return xalNode;
    }
    
    
    /**
     * Set the hardware section that this element 
     * represents.
     *
     * @param secHware  enumeration of possible hardware subsections
     * 
     * @since  Sep 2, 2009
     * @author Christopher K. Allen
     */
    public void setHardwareSection(SECTION secHware) {
        this.secHware = secHware;
    }
    
    /**
     * Returns the hardware subsection that this element
     * represents.
     *
     * @return  the model element's corresponding hardware subsection
     * 
     * @since  Sep 2, 2009
     * @author Christopher K. Allen
     */
    public SECTION getHardwareSection() {
        return this.secHware;
    }
    
    /**
     * Return the element type.
     */
    public abstract String getType();
    
	/**
	 * Is this really a thick element?
	 * @return true if element is to be treated as a thick element, i.e. appended to the lattice
	 * in phase 1 of lattice generation; false if element is treated as a thin element; i.e. inserted 
	 * in the lattice in phase 2 of lattice generation.
	 */
	public boolean isThick() {
		return handleAsThick;
	}

    /**
     * Return the upstream start position of this element.
     */
    public double getStartPosition() {
    	double pos = position-len*0.5;
    	if (pos < 0.)
    		pos = 0.;
        return pos;
    }
    
    /**
     * Return the downstream end position of this element
     */
    public double getEndPosition() {
        return position+len*0.5;
    }
    
    /**
     * Return the center position of this element
     */
    public double getPosition() {
        return position;
    }
    
    /**
     * Convert to absolute position
     */
    public  double toAbsolutePosition(double position) {
    	return base+position;
    }
    
    /**
     * Return the length of this element in distance units.
     */
    public double getLength() {
        return len;
    }
    
    /**
     * Return the name of this element.
     */
    public String getName() {
        return name;
    }
    
    /**
     * Return the base for relative positions.
     */
    public double getBase() {
        return base;
    }
    
    private double[] getSlicePositions(double cut_pos) {
        //calculate length and position of sliced parts.
        double up_len=cut_pos-getStartPosition();
        //        System.out.println("getSlicePostions: "+cut_pos+","+getStartPosition());
        if(Math.abs(up_len) < Lattice.EPS) {
            up_len=0.0;
        }
        double dn_len=getLength()-up_len;
        if(Math.abs(dn_len) < Lattice.EPS) {
            dn_len=0.0;
        }
        double up_pos=getStartPosition()+up_len*0.5;
        double dn_pos=getEndPosition()-dn_len*0.5;
        double[] retval={up_pos,up_len,dn_pos,dn_len};
        //        System.out.println("up_p,up_l,dn_p,dn_l: "+up_pos+","+up_len+","+dn_pos+","+dn_len);
        return retval;
    }
    
    /**
     * Set the base for relative positions.
     */
    public void setBase(double base) {
        this.base=base;
    }
    
    /**
     * Set the element's center position.
     */
    public void setPosition(double position) {
        this.position=position;
    }
    
    /**
     * Set the element length.
     */
    public void setLength(double length) {
        this.len=length;
    }

	@SuppressWarnings( "rawtypes" )		// arrays don't support generics
    protected List<Element> split(Element insert) throws LatticeError {
        //The slice (and replace) operation. The thick element (this)
        //is cut into an upstream and a downstream part and then element 'insert'
        //is inserted (with limiting markers) into the lattice.
        final ArrayList<Element> retval=new ArrayList<Element>();
        Object[] args=new Object[3];
        Element upstream=null, downstream=null;
        
        double cut_pos=insert.getPosition();
        double[] positions=getSlicePositions(cut_pos);
        //consistyency check: any negative length ?
        double neg_len=0.f;
        boolean error=false;
        if(positions[1] < -Lattice.EPS) {
            neg_len=positions[1];
            error=true;
        } else if(positions[3] < -Lattice.EPS) {
            neg_len=positions[3];
            error=true;
        }
        if(error) {
            //ooops! negative length: severe error ...
            String message="negative length when splitting: "+getName()+": pos= "+getPosition()+", len= "+getLength();
            message+=": calculated length= "+neg_len;
            message+="\n\t while inserting: "+insert.getName()+": pos= "+insert.getPosition()+", len= "+insert.getLength();
            throw new LatticeError(message);
        }
        
        try {
            Class[] params=new Class[3];
            params[0]=Class.forName("java.lang.Double");
            params[1]=Class.forName("java.lang.Double");
            params[2]=Class.forName("java.lang.String");
            Constructor<?> constructor = this.getClass().getConstructor(params);
            
            args[0]=new Double(positions[0]);
            args[1]=new Double(positions[1]);
//            if (getPosition() > ((Double)args[0]).doubleValue()) 
//                args[2]=getName()+"x";
//            else
//                args[2]=getName();
            args[2]=getName();

            upstream=(Element)constructor.newInstance(args);
            upstream.setAcceleratorNode(this.xalNode);
            
            args[0]=new Double(positions[2]);
            args[1]=new Double(positions[3]);
//            if (getType() != "drift") {
//                args[2]=getName()+"y";
//            }
//            else {
//                args[2]=getName();
//            }
            args[2]=getName();
            
            downstream=(Element)constructor.newInstance(args);
            downstream.setAcceleratorNode(this.xalNode);

            // Assign the modeling element relationship to the hardware
            switch ( this.getHardwareSection() ) {
            case POINT:
                upstream.setHardwareSection(SECTION.POINT);
                downstream.setHardwareSection(SECTION.POINT);
                break;
                
            case WHOLE:
                upstream.setHardwareSection(SECTION.UPSTREAM);
                downstream.setHardwareSection(SECTION.DNSTREAM);
                break;
                
            case UPSTREAM:
                upstream.setHardwareSection(SECTION.UPSTREAM);
                downstream.setHardwareSection(SECTION.INTERNAL);
                break;
                
            case DNSTREAM:
                upstream.setHardwareSection(SECTION.INTERNAL);
                downstream.setHardwareSection(SECTION.DNSTREAM);
                break;
                
            case INTERNAL:
                upstream.setHardwareSection(SECTION.INTERNAL);
                downstream.setHardwareSection(SECTION.INTERNAL);
                break;
                
            default:
                upstream.setHardwareSection(SECTION.UNKNOWN);
                downstream.setHardwareSection(SECTION.UNKNOWN);
            }
            
        } catch(Exception exptn) {
            System.out.println(exptn);
            System.exit(-1);
        }
        
        Element marker=new Marker(cut_pos);
        
        if(Math.abs(upstream.getLength()) < Lattice.EPS) {
            retval.add(insert);
            retval.add(marker);
            retval.add(this);
        } else if(Math.abs(downstream.getLength()) < Lattice.EPS) {
            retval.add(this);
            retval.add(marker);
            retval.add(insert);
        } else {
            retval.add(upstream);
            retval.add(marker);
            retval.add(insert);
            retval.add(marker);
            retval.add(downstream);
        }
        
        return retval;
    }
    
    /**
     * Returns a printable string of this element.
     */
    public String toCoutString() {
        String retval="";
        double el_pos=getPosition();
        double el_len=getLength();
		double a_start = toAbsolutePosition(getStartPosition());
        String name=getName();
        String type=getType();
        retval +="s="+fmt.format(a_start)+" m\t"+name+"\t"+type+" p="+fmt.format(el_pos)+" l="+fmt.format(el_len);
        return retval;
    }
    
    public String getFam() {
        return "THICK";
    }
    
    /**
     * Return a version string wo the cvs keyword (i.e. $Id$).
     */
    public static String version() {
        String st="";
        char[] woId=new char[st.length()];
        int srcBegin=5;
        int srcEnd=st.length()-6;
        int srccnt=srcEnd-srcBegin;
        if(srccnt <= 0) { return ""; }
        st.getChars(srcBegin,srcEnd,woId,0);
        return new String(woId,0,srccnt);
    }
        
    /**
     * When called with a Visitor reference the implementor can either
     * reject to be visited (empty method body) or call the Visitor by
     * passing its own object reference.
     *
     *@param v the Visitor which wants to visit this object.
     */
    public abstract void accept(Visitor v);
    
}///////////////////////////////////////////// Element
