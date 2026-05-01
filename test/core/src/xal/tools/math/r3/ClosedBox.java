/*
 * ClosedBox.java
 *
 * Created on January 27, 2003, 11:05 AM
 */

package xal.tools.math.r3;

import  xal.tools.math.ClosedInterval;
import xal.tools.math.MathException;

/**
 *  Represents a Cartesian box in <b>R</b><sup>3</sup>.
 *
 * @author  Christopher K. Allen
 * @since   Jan 27, 2003
 */
public class ClosedBox implements java.io.Serializable {
    
    
    
    /*
     *  Global Constants 
     */
    
    /** serialization version identifier */
    private static final long serialVersionUID = 1L;

    
    /*
     * Local Attributes
     */
    
    /** first dimension extent of domain */
    public ClosedInterval I1;
    
    /** second dimension extent of domain */
    public ClosedInterval I2;
    
    /** third dimension extent of domain */
    public ClosedInterval I3;
    
    /** 
     *  Default constructor - creates an empty DomainR3 object to be initialized by the
     *  user
     */
    public ClosedBox()   {
    }
    
    /**
     *  Initializing constructor - creates a new instance of DomainR3 according to the
     *  given parameters.
     *
     *  @param  I1  interval of definition in x dimension
     *  @param  I2  interval of definition in y dimension
     *  @param  I3  interval of definition in z dimension
     */
    public ClosedBox(ClosedInterval I1, ClosedInterval I2, ClosedInterval I3) {
        this.I1 = I1;
        this.I2 = I2;
        this.I3 = I3;
    }
    
    /**
     *  Initializing constructor - creates a new instance of DomainR3 according to the
     *  given parameters.
     *
     *	Not that the box is defined by the intervals
     *  [xmin,xmax]     interval of definition in x dimension
     *  [ymin,ymax]     interval of definition in y dimension
     *  [zmin,zmax]     interval of definition in z dimension
     * 
     *  @param  xmin		x dimension minimum value
     * 	@param	xmax     	x dimension maximum value
     *  @param  ymin        y dimension minimum value
     * 	@param	ymax     	y dimension maximum value
     *  @param  zmin        z dimension minimum value
     *  @param  zmax        z dimension maximum value
     *  
     * @throws MathException one or more axis intervals are malformed 
     *                       (i.e., &alpha;<sub><i>max</i></sub> &lt; &alpha;<sub><i>min</i></sub>, 
     *                       where &alpha; &isin; {<i>x,y,z</i>})
     * 
     */
    public ClosedBox(double xmin, double xmax, double ymin, double ymax, double zmin, double zmax) throws MathException   {
        
        I1 = new ClosedInterval(xmin, xmax);
        I2 = new ClosedInterval(ymin, ymax);
        I3 = new ClosedInterval(zmin, zmax);
    }
    

    /*
     *  Grid Properties
     */
    
    /**
     *  Get first dimension extent
     */
    public ClosedInterval get1()      { return I1; };
    
    /**
     *  Get second dimension extent
     */
    public ClosedInterval get2()      { return I2; };
    
    /**
     *  Get second dimension extent
     */
    public ClosedInterval get3()      { return I3; };
    
    
    /**
     *  Get the x dimension
     */
    public ClosedInterval getXDimension() { return I1; };
    
    /**
     *  Get the y dimension
     */
    public ClosedInterval getYDimension() { return I2; };
    
    /**
     *  Get the z dimension
     */
    public ClosedInterval getZDimension() { return I3; };
    
    /**
     *  Get the minimum vertex
     */
    public R3 getVertexMin() {
        return new R3(I1.getMin(), I2.getMin(), I3.getMin());
    }
    
    /**
     *  Get the maximum vertex
     */
    public R3 getVertexMax()    {
        return new R3(I1.getMax(), I2.getMax(), I3.getMax());
    }

    
    /**
     *  Determine whether point pt is an element of the domain.
     *
     *  @return true if pt is in domain
     */
    public boolean membership(R3 pt) {
        
        if (!I1.membership(pt.getx())) return false;
        if (!I2.membership(pt.gety())) return false;
        if (!I3.membership(pt.getz())) return false;
        
        return true;
    }
    
    /**
     *  Determine whether or not point pt is member of the boundary of this set.
     *
     *  @return     true if pt is a boundary element
     */
    public boolean  boundary(R3 pt) {
        if (I1.isBoundary(pt.getx())) return true;
        if (I2.isBoundary(pt.gety())) return true;
        if (I3.isBoundary(pt.getz())) return true;
        
        return false;
    };

    
    
    /**
     *  Compute the centroid of the domain
     */
    public R3 centroid()    {
        return new R3(I1.midpoint(), I2.midpoint(), I3.midpoint());
    }
    
    /**
     *  Computes the diameter of the domain.
     */
    public double diameter()    {
        return this.dimensions().norm2();
    }
    
    /**
     *  Compute the volume of the domain.
     */
    public double volume() {
        return I1.measure() * I2.measure() * I3.measure();
    }
    
    /**
     *  Compute the dimensions of the domain
     *
     *  @return (lx,ly,lz)
     */
    public R3 dimensions() {
        return new R3(I1.measure(), I2.measure(), I3.measure());
    }
    
    
    
    
    /*
     *  Testing and Debugging
     */
    
    /**
     *  Print out contents on an output stream
     *
     *  @param  os      output stream receiving content dump
     */
    public void print(java.io.PrintWriter os)   {
        I1.print(os);
        os.print("x");
        I2.print(os);
        os.print("x");
        I3.print(os);
    }

    /**
     *  Print out contents on an output stream, terminate in newline character
     *
     *  @param  os      output stream receiving content dump
     */
    public void println(java.io.PrintWriter os)   {
        I1.print(os);
        os.print("x");
        I2.print(os);
        os.print("x");
        I3.println(os);
    }
}
