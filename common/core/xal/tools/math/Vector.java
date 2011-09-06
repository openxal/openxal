/*
 * Vector.java
 *
 * Created on August 15, 2002, 11:02 AM
 */

package xal.tools.math;

import java.lang.IllegalArgumentException;
import java.lang.ArrayIndexOutOfBoundsException;
import java.io.PrintWriter;

import Jama.Matrix;


/**
 *  Specializes the Jama.Matrix class to a vector.
 *
 * @author  CKAllen
 */

public class Vector extends Jama.Matrix implements java.io.Serializable {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    /** Generate a vector with uniformly distributed random elements
     *  @param  nSize   size of created vector
     */
    public static Vector random(int nSize) {
        return (Vector)Matrix.random(nSize, 1);
    };

    
    
    /** Constructs a column vector of zeros 
     *  @param  nSize   vector size
     */
    public Vector(int nSize) { 
        super(nSize, 1);  
    };
    
    
    /** Constructs a constant column vector
     *  @param  nSize   vector size
     *  @param  dblVal  constant value
     */
    public Vector(int nSize, double dblVal) { 
        super(nSize, 1, dblVal); 
    };
    
    
    /** Constructs a Vector specified by the double array
     *  @param  arrVals     element values for vector
     */
    public Vector(double[] arrVals) {
        super(arrVals.length, 1);
        
        for (int i=0; i<arrVals.length; i++)
            super.set(i, 0, arrVals[i]);
    };
    
    /** 
     *  Copy Constructor
     *  Constructs a new Vector initialized to the argument.
     *
     *  @param  vecInit     initial value
     */
    public Vector(Vector vecInit)   {
        this(vecInit.getSize());
        for (int i=0; i<vecInit.getSize(); i++)
            this.set( i, vecInit.get(i) );
    };
    
    /** 
     *  Constructs a new Vector from a Matrix object.  Vector is initialized
     *  to the first column of the matrix.  This constructor is meant to take
     *  a column vector in Matrix form to the standard vector format.
     *
     *  @param  mat     initial values 
     */
    public Vector(Matrix mat)   {
        super(mat.getRowDimension(), 1);
        
        for (int i=0; i<mat.getRowDimension(); i++)
            this.set(i, mat.get(i, 0));
    };
    
    
    
    /**
     *  Perform a deep copy of this Vector object
     */
    public Vector copyVector()    {
        return new Vector(this);
    };
    
    
    /**
     *  Get size of Vector (number of elements)
     */
    public int getSize()    {
        return super.getRowDimension();
    };
    
    /** Get individual element of a vector
     *  @param  iIndex  index of element
     *  @return         value of element
     *  @exception  ArrayIndexOutOfBoundsException  iIndex is larger than vector size
     */
    public double get(int iIndex) throws ArrayIndexOutOfBoundsException {
        return this.get(iIndex, 0);
    };
    
    
    /** Set individual element of a vector
     *  @param  iIndex  index of element
     *  @param  dblVal  new value of element
     *
     *  @exception  ArrayIndexOutOfBoundsException  iIndex is larger than the vector
     */
    public void set(int iIndex, double dblVal) throws ArrayIndexOutOfBoundsException {
        super.set(iIndex, 0, dblVal);
    };
    
   
    /**
     *  Vector in-place addition
     *
     *  @param  vec     Vector to add to this vector
     *
     *  @exception  IllegalArgumentException    vec is not proper dimension
     */
    public void plusEquals(Vector vec) throws IllegalArgumentException {
        super.plusEquals(vec);
    };
    
    /**
     *  Vector addition without destruction
     *
     *  @param  vec     vector to add 
     *
     *  @exception  IllegalArgumentException    vec is not proper dimension
     */
    public Vector plus(Vector vec) throws IllegalArgumentException {
        return (Vector)super.plus(vec);
    };
    
    
    /** 
     *  Scalar multiplication
     *
     *  @param  s   scalar value
     *
     *  @return     result of scalar multiplication
     */
    public Vector timesScalar(double s)   {
        return (Vector)super.times(s);
    };
    

    /** 
     *  In place scalar multiplication
     *  
     *  @param  s   scalar
     */
    public void timesScalarEqual(double s)   {
        this.timesEquals(s);
    };
    

    /**
     *  Vector inner product.
     *
     *  Computes the inner product of two vectors of the same dimension.
     *  
     *  @param  vec     second vector
     *
     *  @return         inner product of this vector and argument
     *
     *  @exception  IllegalArgumentException    dimensions must agree
     */
    public double   innerProd(Vector vec)   throws IllegalArgumentException {
        int         N;      // vector dimensions
        int         n;      // coordinate index
        double      dblSum; // running sum
        
        N = vec.getSize();
        if (this.getSize() != N)
            throw new IllegalArgumentException("Vector#innerProd() - unequal dimensions.");
        
        dblSum = 0.0;
        for (n=0; n<N; n++) 
            dblSum += this.get(n)*vec.get(n);
        
        return dblSum;
    }
    
    /**
     *  Vector outer product - computes the tensor product of two vector objects.
     *
     *  Returns the value this*vec' where the prime indicates transposition
     *
     *  @param  vec     right argument
     *
     *  @return         outer product 
     *  
     *  @exception  IllegalArgumentException    vector dimension must agree
     */
    public Matrix   outerProd(Vector vec)   throws IllegalArgumentException {
        return  super.times( vec.transpose() );
    }
    
    /** 
     *  Vector left multiplication (post-multiply vector by matrix).
     *
     *  @param  mat     matrix operator
     *
     *  @return         result of vector-matrix product
     *
     *  @exception  IllegalArgumentException    dimensions must agree
     */
    public Vector leftMultiply(Matrix mat) throws IllegalArgumentException   {
        Matrix  matRes = this.times(mat).transpose();
        
        return new Vector(matRes);
    };
    
    /** 
     *  Vector right multiplication (pre-multiply vector by matrix).
     *
     *  @param  mat     matrix operator
     *
     *  @return         result of vector-matrix product
     *
     *  @exception  IllegalArgumentException    dimensions must agree
     */
    public Vector rightMultiply(Matrix mat) throws IllegalArgumentException   {
        Matrix  matRes = mat.times(this);
        
        return new Vector(matRes);
    };
    
    
    
    /**
     *  Print the vector contents to an output stream,
     *  does not add new line.
     *
     *  @param  os      output stream object 
     */
    public void print(PrintWriter os)   {

        // Create vector string
        int     indLast = this.getSize() - 1;
        String  strVec = "(";

        for (int i=0; i<indLast; i++)
            strVec = strVec + this.get(i) + ",";
        strVec = strVec + this.get(indLast) + ")";

        // Send to output stream
        os.print(strVec);
    };
            
    /**
     *  Print the vector contents to an output stream, 
     *  add new line character.
     *
     *  @param  os      output stream object 
     */
    public void println(PrintWriter os)   {

        // Create vector string
        int     indLast = this.getSize() - 1;
        String  strVec = "(";

        for (int i=0; i<indLast; i++)
            strVec = strVec + this.get(i) + ",";
        strVec = strVec + this.get(indLast) + ")";

        // Send to output stream
        os.println(strVec);
    };
            
}
