/*
 * ThinElement.java
 *
 * Created on March 17, 2003, 11:59 PM
 */

package xal.sim.slg;

import java.util.List;
import java.util.ArrayList;


/**
 * The superclass of all thin elements.
 *
 * @author  wdklotz
 */
public abstract class ThinElement extends Element {
    
    /** Creates a new instance of ThinElement */
    protected ThinElement(String name, double position, double len) {
        super(SECTION.POINT,name,position,len);
    }
    
    /**
     * Return the element type.
     */
    @Override
    public abstract String getType();
    
    /**
     * Return the (upstream) start position of this element.
     */
    @Override
    public double getStartPosition() {
        return super.getPosition();
    }
    
    /**
     * Return the (downstream) end position of this element.
     */
    @Override
    public double getEndPosition() {
        return super.getPosition();
    }
    
    /**
     * Return the length of this element.
     */
    @Override
    public double getLength() {
        return 0.0;
    }
    
    /**
     * Return the effective length of this element.
     */
    public double getEffLength() {
        return super.getLength();
    }
    
    /**
     * Return the upstream drift space of a slim element.
     */
    public Element getUpstreamDrift() {
        double len=getEffLength()*0.5;
        double position=getPosition();
        if(Math.abs(len) < Lattice.EPS) {
            return new Marker(position);
        } else {
            position=position-len*0.5;
            return new Drift(position,len);
        }
    }
    
    /**
     * Return the downstream drift space of a slim element.
     */
    public Element getDownstreamDrift() {
        double len=getEffLength()*0.5;
        double position=getPosition();
        if(Math.abs(len) < Lattice.EPS) {
            return new Marker(position);
        } else {
            position=position+len*0.5;
            return new Drift(position,len);
        }
    }
    
    /**
     * Return the slim element as a tuple (drift,element,drift).
     */
    public List<Element> asTuple() {
        ArrayList<Element> retval=new ArrayList<Element>();
        retval.add(getUpstreamDrift());
        retval.add(this);
        retval.add(getDownstreamDrift());
        return retval;
    }
    
    /**
     * Split the thin element means insert it behind this.
     */
    @Override
    public List<Element> split(Element insert) {
        ArrayList<Element> retval=new ArrayList<Element>();
        retval.add(this);
        retval.add(insert);
        return retval;
    }
    
    /**
     * Return a printable String of the elment.
     */
    @Override
    public String toCoutString() {
        String retval="";
        double el_pos=getPosition();
		double a_start = toAbsolutePosition(getPosition());
        String name=getName();
        String type=getType();
        retval +="s="+fmt.format(a_start)+" m\t"+name+"\t"+type+" p="+fmt.format(el_pos);
        return retval;
    }
    
    @Override
    public String getFam() {
        return "THIN";
    }
    
    /**
     * Return a version string wo the cvs keyword (i.e. $Id$).
     */
    public static String version() {
        String st=" ";
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
    @Override
    public abstract void accept(Visitor v);
    
}
