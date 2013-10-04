/*
 * R2.java
 *
 * Created on April 9, 2003, 4:05 PM
 */

package xal.tools.math.r2;

/**
 *  Class representing a point on the plane in R2.
 *
 * @author  Christopher Allen
 */
public class R2 implements java.io.Serializable {
    
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

    /*
     *  Local Attributes
     */
    
    /** first coordinate */
    public double x1 = 0.0;
    
    /** second coordinate */
    public double x2 = 0.0;
    
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of R2, the zero element. 
     */
    public R2() {
    }
    
    /** 
     *  Creates a new instance of R2 initialized to arguments.
     *
     *  @param  x1   first coordinate value
     *  @param  x2   first coordinate value
     */
    public R2(double x1, double x2) {
        this.x1 = x1;
        this.x2 = x2;
    }
    
    /** 
     *  Creates a new instance of R2 initialized to argument.
     *
     *  @param  vecPt   deep copies this value
     */
    public R2(R2 vecPt) {
        this.x1 = vecPt.x1;
        this.x2 = vecPt.x2;
    }
    
    /**
     *  Performs a deep copy operation.
     *
     *  @return     cloned R3 object
     */
    public R2   copy()  {
        return new R2(this);
    }

    
    /**
     *  Set first coordinate value.
     */
    public void set1(double x1)  { this.x1 = x1; }
    
    /**
     *  Set second coordinate value.
     */
    public void set2(double x2)  { this.x2 = x2; }
    
    

    /**
     *  Set first coordinate value.
     */
    public void setx(double x)  { this.x1 = x; }
    
    /**
     *  Set second coordinate value.
     */
    public void sety(double y)  { this.x2 = y; }
    
    
    /**
     *  Set all coordinates to value
     */
    public void set(double s)   { this.x1 = this.x2 = s; };
    

    
    /*
     *  Properties
     */
    
    /**
     *  Return first coordinate value.
     */
    public double get1() { return x1; }
    
    /**
     *  Return second coordinate value.
     */
    public double get2() { return x2; }
    
    

    
    /**
     *  Return first coordinate value.
     */
    public double getx() { return x1; }
    
    /**
     *  Return second coordinate value.
     */
    public double gety() { return x2; }
    
    
    
    
    /*
     *  Coordinate Transforms
     */
    
    /**
     *  Apply coordinate transform from cartesian to polar coordinates.
     *
     *  @return     polar coordinates (r,phi) of this cartesian point
     */
    public R2   cartesian2Polar()   {
        double r = Math.sqrt(x1*x1 + x2*x2);
        double a = Math.atan2(x2, x1);
        
        return new R2(r, a);
    }
    
    /**
     *  Apply coordinate transform from polar to cartesian coordinates
     *
     *  @return     cartesian coordinates (x,y) of this polar point
     */
    public R2   polar2Cartesian()    {
        double  x = x1*Math.cos(x2);
        double  y = x1*Math.sin(x2);
        
        return new R2(x,y);
    }
    
    /*
     *  Algebraic Methods
     */
    
    /**
     *  Scalar multiplication.
     *
     *  @param  s       scalar to multiply this vector
     *
     *  @return         new R2 object scaled by s
     */
    public R2   times(double s) {
        return new R2(s*x1, s*x2);
    }
    
    /**
     *  Vector addition.
     *
     *  @param  r   vector displacement
     *
     *  @return     new R3 object equal to this displaced by r
     */
    public R2 plus(R2 r) {
        return new R2(x1 + r.x1, x2 + r.x2);
    }

    /**
     *  Vector subtraction.
     *
     *  @param  r   vector displacement
     *
     *  @return     new R3 object equal to this displaced by r
     */
    public R2 minus(R2 r) {
        return new R2(x1 - r.x1, x2 - r.x2);
    }
    
    /**
     *  Vector multiplication, i.e. complex multiplication.
     *
     *  @param  r   second (right) operand in cross-product (this is first operand)
     *
     *  @return     result of vector cross product in three space
     */
    public R2 times(R2 r)   {
        return new R2(x1*r.x1 - x2*r.x2, x1*r.x2 + x2*r.x1);
    }
    
    
    /*
     *  Geometric Methods
     */
    
    
    /**
     *  Compute the l2 norm of the vector in R2.
     *
     *  @return         l2 norm = (x1^2 + x2^2)^1/2
     */
    public double norm2() {
        return Math.sqrt(x1*x1 + x2*x2);
    }
    
    
    
    
    /*
     *  Testing and Debugging
     */
    
    /**
     *  Convert to a string representation
     *
     *  @return     string in standard format "(x1,x2)"
     */
    @Override
    public String   toString()  {
        String  strVec = "(" + x1 + "," + x2 + ")";
        
        return strVec;
    }
    
    /**
     *  Print out centents on an output stream.
     *
     *  @param  os      output stream receive content dump
     */
    public void print(java.io.PrintWriter os)   {
        os.print(this.toString());
    }
    
    /**
     *  Print out centents on an output stream, terminate with new line character.
     *
     *  @param  os      output stream receive content dump
     */
    public void println(java.io.PrintWriter os)   {
        os.println(this.toString());
    }
    
}
