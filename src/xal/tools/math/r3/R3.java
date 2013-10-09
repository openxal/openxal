/*
 * R3.java
 *
 * Created on January 8, 2003, 8:52 AM
 */

package xal.tools.math.r3;

import java.util.StringTokenizer;

/**
 *  Represents an element of R^3, the three-dimensional cartesian real space.
 *
 * @author  Christopher Allen
 */

public class R3 implements java.io.Serializable {
    
    
    /*
     * Global Constants
     */
    
    
    /** Serialization version identifier */
    private static final long serialVersionUID = 1L;

    
    /*
     *  Attributes
     */
    
    /** first coordinate */
    public double x1 = 0.0;
    
    /** second coordinate */
    public double x2 = 0.0;
    
    /** third coordinate */
    public double x3 = 0.0;
    
    


    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of the zero vector.
     *
     *  @return         zero vector
     */
    public static R3  zero()   {
        return new R3( 0.0, 0.0, 0.0 );
    }
    
    /**
     * Create a new instance of R3 with initial value determined
     * by the formatted string argument.  The string should be formatted as
     * 
     *  "(x,y,z)"
     * 
     * where x, y, z are floating point representations.
     *
     * @param  strTokens   six-token string representing values phase coordinates
     * 
     * @return             3-vector built from the given string representation 
     *
     * @exception  IllegalArgumentException    wrong number of tokens in argument (must be 6)
     * @exception  NumberFormatException       bad numeric value, unparseable
     */
    public static R3 parse(String   strTokens)    
        throws NumberFormatException, IllegalArgumentException
    {
        return new R3(strTokens);
    }




    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of R3, the zero element. 
     */
    public R3() {
    }
    
    /** 
     *  Creates a new instance of R3 initialized to arguments.
     *
     *  @param  x1   first coordinate value
     *  @param  x2   first coordinate value
     *  @param  x3   first coordinate value
     */
    public R3(double x1, double x2, double x3) {
        this.x1 = x1;
        this.x2 = x2;
        this.x3 = x3;
    }
    
    /** 
     *  Creates a new instance of R3 initialized to argument.
     *  If the initializing array has length greater than 3 
     *  the first three values are taken, if it has length less
     *  than three then the remain coordinates of the new 
     *  <code>R3</code> object are zero.
     *
     *  @param  arrVals     double array of initializing values
     */
    public R3(double[] arrVals) {
        
        if (arrVals.length >= 3)    {
            this.x1 = arrVals[0];
            this.x2 = arrVals[1];
            this.x3 = arrVals[2];
            
            return;
        }
        
        if (arrVals.length >= 1)
            this.x1 = arrVals[0];
        if (arrVals.length >= 2)
            this.x2 = arrVals[1];
        if (arrVals.length >=3)
            this.x3 = arrVals[2];
    }
    
    /** 
     *  Creates a new instance of R3 initialized to argument.
     *
     *  @param  vecPt   deep copies this value
     */
    public R3(R3 vecPt) {
        this.x1 = vecPt.x1;
        this.x2 = vecPt.x2;
        this.x3 = vecPt.x3;
    }

    /**
     * Create a new instance of R3 with specified initial value specified by the 
     * formatted string argument.  
     *
     *  The string should be formatted as
     * 
     *  "(x,y,z)"
     * 
     * where x, y, z are floating point representations.
     * 
     * @param  strTokens   token string representing values phase coordinates
     *
     * @exception  IllegalArgumentException    wrong number of tokens in argument (must be 6 or 7)
     * @exception  NumberFormatException       bad numeric value, unparseable
     */
    public R3(String   strTokens)    
        throws NumberFormatException, IllegalArgumentException
    {
        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strTokens, " ,()");
        
        if (tokArgs.countTokens() < 3)
            throw new IllegalArgumentException("R3(strTokens) - wrong number of token strings: " + strTokens);
        
        
        // Extract initial values
        int                 i;      // loop control
        
        for (i=0; i<3; i++)  {
            String  strVal = tokArgs.nextToken();
            double  dblVal = Double.valueOf(strVal).doubleValue();
            
            this.set(i, dblVal);
        }
    }
    


    
    /**
     *  Performs a deep copy operation.
     *
     *  @return     cloned R3 object
     */
    public R3   copy()  {
        return new R3(this);
    }

    
    /**
     * Set index to value.
     * 
     * @param i     element index 0<=i<=2
     * @param val   new element value
     * 
     * @throws ArrayIndexOutOfBoundsException   the index <var>i</var> was greater than 2 
     */
    public void set(int i, double val)  throws ArrayIndexOutOfBoundsException {
        
        switch (i)  {
            
            case 0:
               this.x1 = val; 
               break;
               
           case 1:
                this.x2 = val;
                break;
                
           case 2:
                this.x3 = val;
                break;
                
            default:
                throw new ArrayIndexOutOfBoundsException("index value not in [0,2]");       
        }
    }
    
    
    /**
     *  Set first coordinate value.
     *  
     * @param x1    first coordinate of 3-vector
     */
    public void set1(double x1)  { this.x1 = x1; }
    
    /**
     *  Set second coordinate value.
     *  
     * @param x2    second coordinate of 3-vector 
     */
    public void set2(double x2)  { this.x2 = x2; }
    
    /**
     *  Set third coordinate value.
     *  
     * @param x3    third coordinate of 3-vector 
     */
    public void set3(double x3)  { this.x3 = x3; }
    

    /**
     *  Set first coordinate value.
     *  
     *  @param  x   first coordinate of 3-vector
     */
    public void setx(double x)  { this.x1 = x; }
    
    /**
     *  Set second coordinate value.
     *  
     *  @param  y   second coordinate of 3-vector
     */
    public void sety(double y)  { this.x2 = y; }
    
    /**
     *  Set third coordinate value.
     *  
     *  @param  z   first coordinate of 3-vector
     */
    public void setz(double z)  { this.x3 = z; }
    
    
    /**
     *  Set all coordinates to value
     *  
     *  @param  s   new value of all vector coordinates
     */
    public void set(double s)   { this.x1 = this.x2 = this.x3 = s; };
    

    
    /*
     *  Properties
     */
	
	
	/**
	 * Get all the vector values as a 3-array.
	 * 
	 * @return     the array {<i>x</i><sub>1</sub>, <i>x</i><sub>2</sub>, <i>x</i><sub>3</sub>}.
	 */
	public double[] toArray() {
		return new double[] { x1, x2, x3 };
	}
	
	
	/**
	 * Get the coordinate value for the specified mode.
	 * @param mode the mode for which to get the coordinate value
	 * @return the coordinate value for the specified mode.
	 * @throws java.lang.IllegalArgumentException if the mode is not one of 1, 2 or 3.
	 */
	public double getValue( final int mode ) {
		switch ( mode ) {
			case 1:
				return x1;
			case 2:
				return x2;
			case 3:
				return x3;
			default:
				throw new IllegalArgumentException( "The mode must be one of only 1, 2 or 3. You specified a mode of: " + mode );
		}
	}
	
    
    /**
     *  Return first coordinate value.
     *  
     * @return  the first vector coordinate 
     */
    public double get1() { return x1; }
    
    /**
     *  Return second coordinate value.
     *  
     *  @return the second coordinate vector
     */
    public double get2() { return x2; }
    
    /**
     *  Return third coordinate value.
     *  
     *  @return the third coordinate vector
     */
    public double get3() { return x3; }
    

    
    /**
     *  Return first coordinate value.
     *  
     * @return the first coordinate vector
     */
    public double getx() { return x1; }
    
    /**
     *  Return second coordinate value.
     *  
     *  @return the second coordinate vector
     */
    public double gety() { return x2; }
    
    /**
     *  Return third coordinate value.
     *  
     *  @return the third coordinate vector
     */
    public double getz() { return x3; }
    
    
    
    
    /*
     *  Object method overrides
     */
     
    /**
     *  Convert the vector contents to a string.
     *
     *  @return     vector value as a string (v0, v1, ..., v5)
     */
    @Override
    public String   toString()  {

        // Create vector string
        String  strVec = "(" + x1 + "," + x2 + "," + x3 + ")";
        
        return strVec;
    }
        

    /*
     *  Coordinate Transforms
     */
    
    /**
     *  Apply coordinate transform from cartesian to cylindrical coordinates.
     *
     *  @return     polar coordinates (r,phi,z) of this cartesian point
     */
    public R3   cartesian2Cylindrical()   {
        double r = Math.sqrt(x1*x1 + x2*x2);
        double a = Math.atan2(x2, x1);
        double z = x3;
        
        return new R3(r, a, z);
    }
    
    /**
     *  Apply coordinate transform from cartesian to spherical coordinates.
     *
     *  @return     polar coordinates (R, theta, phi) of this cartesian point
     */
    public R3   cartesian2Spherical()   {
        double r_2 = x1*x1 + x2*x2;
        double r   = Math.sqrt(r_2);
        double R   = Math.sqrt(r_2 + x3*x3);
        double theta = Math.atan2(x3, r);
        double phi   = Math.atan2(x2, x1);

        return new R3(R, theta, phi);
    }
    
    /**
     *  Apply coordinate transform from cylindrical to cartesian coordinates
     *
     *  @return     cartesian coordinates (x,y,z) of this cylindrical point
     */
    public R3   cylindrical2Cartesian()    {
        double  x = x1*Math.cos(x2);
        double  y = x1*Math.sin(x2);
        double  z = x3;
        
        return new R3(x,y,z);
    }
    
    /**
     *  Apply coordinate tranform from spherical to cartesian coordinates
     *
     *  @return     cartesian coordinates (x,y,z) of this sphereical point
     */
    public R3   spherical2Cartesian()   {
        double  r = x1*Math.cos(x2);
        double  x = r*Math.cos(x3);
        double  y = r*Math.sin(x3);
        double  z = x1*Math.sin(x2);
        
        return new R3(x, y, z);
    }
    
    /*
     *  Algebraic Methods
     */
    
    /**
     *  Scalar multiplication.
     *
     *  @param  s       scalar to multiply this vector
     *
     *  @return         new R3 object scaled by s
     */
    public R3   times(double s) {
        return new R3(s*x1, s*x2, s*x3);
    }
    
    /**
     *  Vector addition.
     *
     *  @param  r   vector displacement
     *
     *  @return     new R3 object equal to this displaced by r
     */
    public R3 plus(R3 r) {
        return new R3(x1 + r.x1, x2 + r.x2, x3 + r.x3);
    }

    /**
     *  Vector subtraction.
     *
     *  @param  r   vector displacement
     *
     *  @return     new R3 object equal to this displaced by r
     */
    public R3 minus(R3 r) {
        return new R3(x1 - r.x1, x2 - r.x2, x3 - r.x3);
    }
    
    /**
     *  Vector multiplication using three-dimensional cross product.
     *
     *  @param  r   second (right) operand in cross-product (this is first operand)
     *
     *  @return     result of vector cross product in three space
     */
    public R3 times(R3 r)   {
        return new R3(x2*r.x3 - x3*r.x2, x3*r.x1 - x1*r.x3, x1*r.x2 - x2*r.x1);
    }
    
    
    /*
     *  Geometric Methods
     */
    
    
    /**
     *  Compute the <i>l</i><sub>2</sub> norm of the vector in R3.
     *
     *  @return         <i>l</i><sub>2</sub> norm &equiv; (<i>x</i><sub>1</sub><sup>2</sup> 
     *                                            + <i>x</i><sub>2</sub><sup>2</sup>  
     *                                            + <i>x</i><sub>3</sub><sup>2</sup>)<sup>1/2</sup>
     */
    public double norm2() {
        return Math.sqrt(x1*x1 + x2*x2 + x3*x3);
    }
    
    /**
     * Returns the vector of squared elements.
     *
     * @return  the vector (<i>x</i><sub>1</sub><sup>2</sup>, 
     *                      <i>x</i><sub>2</sub><sup>2</sup>, 
     *                      <i>x</i><sub>3</sub><sup>2</sup>) 
     *
     * @author Christopher K. Allen
     * @since  Aug 25, 2011
     */
    public R3 squared() { 
        return new R3(x1*x1, x2*x2, x3*x3);
    }
    
    
    
    /*
     *  Testing and Debugging
     */
    
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
