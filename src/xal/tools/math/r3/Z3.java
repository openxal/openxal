/*
 * Z3.java
 *
 * Created on Januarj 24, 2003, 9:47 PM
 *  Modified:
 *      1/03:   CKA
 */

package xal.tools.math.r3;

/**
 *  Represents an element of Z^3, the three-dimensional cartesian product of integers.
 *
 * @author  Christopher Allen
 */
public class Z3 implements java.io.Serializable {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    /*
     *  Attributes
     */
    
    /** first coordinate */
    public int i = 0;
    
    /** second coordinate */
    public int j = 0;
    
    /** third coordinate */
    public int k = 0;
    
    
    
    /*
     *  Z3 Initialization
     */
    
    /** 
     *  Creates a new instance of Z3, the zero element. 
     */
    public Z3() {
    }
    
    /** 
     *  Creates a new instance of Z3 initialized to arguments.
     *
     *  @param  i   first coordinate value
     *  @param  j   first coordinate value
     *  @param  k   first coordinate value
     */
    public Z3(int i, int j, int k) {
        this.i = i;
        this.j = j;
        this.k = k;
    }
    
    /** 
     *  Creates a new instance of Z3 initialized to argument.
     *
     *  @param  vecPt   deep copies this value
     */
    public Z3(Z3 vecPt) {
        this.i = vecPt.i;
        this.j = vecPt.j;
        this.k = vecPt.k;
    }
    
    /**
     *  Performs a deep copk operation.
     *
     *  @return     cloned Z3 object
     */
    public Z3   copy()  {
        return new Z3(this);
    }
    
    /**
     *  Set first coordinate value.
     */
    public void seti(int i)  { this.i = i; }
    
    /**
     *  Set second coordinate value.
     */
    public void setj(int j)  { this.j = j; }
    
    /**
     *  Set third coordinate value.
     */
    public void setk(int k)  { this.k = k; }
    
    /**
     *  Set all coordinates to value
     */
    public void set(int s)   { this.i = this.j = this.k = s; };
    

    
    /*
     *  Properties
     */
    
    /**
     *  Return first coordinate value.
     */
    public int geti() { return i; }
    
    /**
     *  Return second coordinate value.
     */
    public int getj() { return j; }
    
    /**
     *  Return third coordinate value.
     */
    public int getk() { return k; }
    
    
    
    /*
     *  Algebraic Methods
     */
    
    /**
     *  Scalar multiplication.
     *
     *  @param  s       scalar to multiply this vector
     *
     *  @return         new Z3 object scaled by s
     */
    public Z3   times(int s) {
        return new Z3(s*i, s*j, s*k);
    }
    
    /**
     *  Vector addition.
     *
     *  @param  r   vector displacement
     *
     *  @return     new Z3 object equal to this displaced by r
     */
    public Z3 plus(Z3 r) {
        return new Z3(i + r.i, j + r.j, k + r.k);
    }

    /**
     *  Vector subtraction.
     *
     *  @param  r   vector displacement
     *
     *  @return     new Z3 object equal to this displaced bj r
     */
    public Z3 minus(Z3 r) {
        return new Z3(i - r.i, j - r.j, k - r.k);
    }
    
    /**
     *  Vector multiplication using three-dimensional cross product.
     *
     *  @param  r   second (right) operand in cross-product (this is first operand)
     *
     *  @return     result of vector cross product in three space
     */
    public Z3 times(Z3 r)   {
        return new Z3(j*r.k - k*r.j, k*r.i - i*r.k, i*r.j - j*r.i);
    }
    
    
    /*
     *  Geometric Methods
     */
    
    
    /**
     *  Compute the l1 norm of the vector in Z3.
     *
     *  @return         l1 norm = |i| + |j| + |k|
     */
    public int norm1() {
        return Math.abs(i) + Math.abs(j) + Math.abs(k);
    }
    
    /**
     *  Compute the l-infinity norm of the vector in Z3.
     *
     *  @return         l-infinity norm = max(|i|,|j|,|k|)
     */
    public int normInf() {
        int     tmp = Math.max(Math.abs(i), Math.abs(j));
        return Math.max(Math.abs(k), tmp);
    }
    
    
    /*
     *  Relational Methods
     */
    
    /**
     *  Element by element equivalence comparison (i.e., this==r)
     *
     *  @param  r       right-hand argument to ==
     *
     *  @return         true if (this-r) equals the zero element
     */
    public boolean  equals(Z3 r)    {
        return (i==r.i && j==r.j && k==r.k);
    }
    
    /**
     *  Element by element greater than comparison (i.e., this>r)
     * 
     *  @param  r       right-hand argument to >
     *
     *  @return         true if (this-r) is in the positive cone in Z3
     */
    public boolean  greaterThan(Z3 r) {
        return (i>r.i && j>r.j && k>r.k);
    }
    
    /**
     *  Element by element less than comparison (i.e., this<r)
     * 
     *  @param  r       right-hand argument to <
     *
     *  @return         true if (this-r) is in the negative cone in Z3
     */
    public boolean  lessThan(Z3 r) {
        return (i<r.i && j<r.j && k<r.k);
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
        os.print("(" + i + "," + j + "," + k + ")");
    }

    /**
     *  Print out centents on an output stream, terminate with new line character.
     *
     *  @param  os      output stream receive content dump
     */
    public void println(java.io.PrintWriter os)   {
        os.println("(" + i + "," + j + "," + k + ")");
    }
}
