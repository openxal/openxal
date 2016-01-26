/*
 * PhaseVector.java
 *
 * Created on March 19, 2003, 2:13 PM
 * Modified
 *      10/06   - CKA added indexing 
 */

package xal.tools.beam;

import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.StringTokenizer;

import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.tools.math.BaseVector;
import xal.tools.math.IIndex;
import xal.tools.math.r3.R3;
import xal.tools.math.r4.R4;
import xal.tools.math.r6.R6;



/**
 *  <p>
 *  Represents a vector of homogeneous phase space coordinates for three spatial 
 *  dimensions.  Thus, each phase vector is an element of R7, the set of real 
 *  7-tuples.  
 *  </p>
 *  <p>
 *  The coordinates are as follows:
 *  <pre>
 *      (x, xp, y, yp, z, zp, 1)'
 *  </pre>
 *  where the prime indicates transposition and
 *  <pre>
 *      x  = x-plane position
 *      xp = x-plane momentum
 *      y  = y-plane position
 *      yp = y-plane momentum
 *      z  = z-plane position
 *      zp = z-plane momentum
 *  </pre>
 *  </p>
 *  <p>
 *  Homogeneous coordinates are parameterizations of the projective spaces <i>P<sup>n</sup></i>.  
 *  They are
 *  useful here to allow vector transpositions, normally produced by vector addition, to 
 *  be represented as matrix multiplications.  These operations can be embodied by the class
 *  <code>PhaseMatrix</code>.  Thus, <code>PhaseVector</code>'s are not intended to support
 *  vector addition.
 *  </p>
 *
 *
 * @author  Christopher Allen
 *
 *  @see    xal.tools.math.Vector
 *  @see    PhaseMatrix
 */
/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @since  Oct 11, 2013
 */
public class PhaseVector extends BaseVector<PhaseVector> implements java.io.Serializable, IArchive {
    
    
    /*
     * Global Constants
     */
    
    /** Serialization Identifier  */
    private static final long serialVersionUID = 1L;
    
    
    /** Size of vectors */
    private static final int        INT_SIZE = 7;
    
    
//    /** attribute marker for data */
//    public static final String     ATTR_DATA   = "values";
//    
    
    
    /*
     * Internal Classes
     */
    
    /**
     * Enumeration for the element position indices for homogeneous
     * phase space objects.  This set include the phase space coordinates
     * and the homogeneous coordinate.
     *
     * @author Christopher K. Allen
     * @since  Oct 8, 2013
     */
    public enum IND implements IIndex {

        /*
         * Enumeration Constants
         */
        
        /** Index of the x coordinate */
        X(0),
        
        /** Index of the X' coordinate */
        Xp(1),
        
        /** Index of the Y coordinate */
        Y(2),
        
        /** Index of the Y' coordinate */
        Yp(3),
        
        /** Index of the Z (longitudinal) coordinate */
        Z   (4),        
        
        /** Index of the Z' (change in momenutum) coordinate */
        Zp  (5),        
        
        /** Index of the homogeneous coordinate */
        HOM(6);
        
        
        /*
         * Global Constants
         */
        
        /** the set of IND constants that only include phase space variables (not the homogeneous coordinate) */
        private final static EnumSet<IND> SET_PHASE = EnumSet.of(X, Xp, Y, Yp, Z, Zp);

        
        /*
         * Global Operations
         */
        
        /**
         * Returns the set of index constants that correspond to phase
         * coordinates only.  The homogeneous coordinate index is not
         * included (i.e., the <code>IND.HOM</code> constant).
         * 
         * @return  the set of phase indices <code>IND</code> less the <code>HOM</code> constant
         *
         * @see xal.tools.math.BaseMatrix.IIndex#val()
         *
         * @author Christopher K. Allen
         * @since  Oct 15, 2013
         */
        public static EnumSet<IND>  valuesPhase() {
            return SET_PHASE;
        }

        
        /*
         * IIndex Interface
         */
        
        /**
         * Returns the numerical index of this enumeration constant,
         * corresponding to the index into the phase matrix.
         * 
         * @return  numerical value of this index
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        public int val() {
            return this.val;
        }
        
        /*
         * Initialization
         */
        
        /** The numerical value of this enumeration index */
        final public    int     val; 
        
        /**
         * Creates a new <code>IND</code> enumeration constant
         * initialized to the given index value.
         * 
         * @param index     numerical index value for this constant
         *
         * @author Christopher K. Allen
         * @since  Sep 25, 2013
         */
        private IND(int index) {
            this.val = index;
        }
    }
    
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero phase vector.
     *
     *  @return         zero vector
     */
    public static PhaseVector  newZero()   {
        PhaseVector vecZero = new PhaseVector();
        
        vecZero.assignZero();
        
        return vecZero;
    }
    
    /**
     * Creates a deep copy of the given <code>PhaseVector</code>
     * object.  Thus, the argument is unmodified and unreferenced.
     * 
     * @param   vecParent    vector object to clone
     * 
     * @return              deep copy of the argument
     */
    public static PhaseVector copy(final PhaseVector vecParent)    {
        return new PhaseVector(vecParent);
    }
    
    /**
     * Create a new instance of PhaseVector with initial value determined
     * by the formatted string argument.  The string should be formatted as
     * 
     *  "(x,x',y,y',z,zp')"
     * 
     * where x, x', y, y', z, z' are floating point representations.
     *
     *  @param  strTokens   six-token string representing values phase coordinates
     *
     *  @exception  IllegalArgumentException    wrong number of tokens in argument (must be 6)
     *  @exception  NumberFormatException       bad numeric value, unparseable
     */
    public static PhaseVector parse(String   strTokens)    
        throws NumberFormatException, IllegalArgumentException
    {
        return new PhaseVector(strTokens);
    }
    
    /**
     * Create a new <code>PhaseVector</code> object and initialize with the data 
     * source behind the given <code>DataAdaptor</code> interface.
     * 
     * @param   daSource    data source containing initialization data
     * 
     * @throws DataFormatException      malformed data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     *
     * @since  Jan 4, 2016,   Christopher K. Allen
     */
    public static PhaseVector   loadFrom(DataAdaptor daSource) throws DataFormatException {
        PhaseVector     vecNew = new PhaseVector(daSource);
        
        return vecNew;
    }
    
    
    
    /**
     * Embeds the given vector <b>z</b> &in; <b>R</b><sup>6</sup> into 
     * homogeneous phase space.  The given vector is treated like a vector
     * of phase space coordinates corresponding to the first 6 elements
     * of a <code>PhaseVector</code> object.  The last element of 
     * the returned phase vector has value 1, as do all phase vectors.
     * 
     * @param vecCoords     vector <b>z</b> containing the first 6 element 
     *                      values of the returned phase vector
     * 
     * @return              the augmented vector (<b>z</b>,1)
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public static PhaseVector embed(final R6 vecCoords) {
        PhaseVector vecPhase = new PhaseVector();
        
        for (IND i : IND.valuesPhase()) {
            double  dblVal = vecCoords.getElem(i);
            
            vecPhase.setElem(i, dblVal);
        }
        
        vecPhase.setElem(IND.HOM, 1.0);
        
        return vecPhase;
    }

    /**
     * Embeds the given vector <b>z</b> &in; <b>R</b><sup>4</sup> into 
     * homogeneous phase space.  The given vector is treated like a vector
     * of transverse phase space coordinates corresponding to the first 4 elements
     * of a <code>PhaseVector</code> object.  The vector elements corresonding
     * to the longitudinal phase space coordinates <i>z</i> and <i>z'</i> are
     * both set to zero.  The last element of 
     * the returned phase vector has value 1, as do all phase vectors.
     * 
     * @param vecCoords     vector <b>z</b> containing the first 6 element 
     *                      values of the returned phase vector
     * 
     * @return              the augmented vector (<b>z</b>,1)
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public static PhaseVector embed(final R4 vecCoords) {
        PhaseVector vecPhase = new PhaseVector();
        
        for (R4.IND i : R4.IND.values()) {
            double  dblVal = vecCoords.getElem(i);
            
            vecPhase.setElem(i, dblVal);
        }
        
        vecPhase.setElem(IND.HOM, 1.0);
        
        return vecPhase;
    }

    
    
//    /*
//     *  Local Attributes
//     */
//    
//    /** internal vector representation */
//    private Jama.Matrix     jamaVector;
//    
    
    
    /**
     *  Creates a new instance of PhaseVector with zero initial value.
     */
    public PhaseVector() {
        super(INT_SIZE);
        this.setElem(IND.HOM, 1.0);
    }
    
    /**
     *  Create a new instance of <code>PhaseVector</code> with specified initial value.
     *
     *  @param  x   x-plane position
     *  @param  xp  x-plane momentum
     *  @param  y   y-plane position
     *  @param  yp  y-plane momentum
     *  @param  z   z-plane position
     *  @param  zp  z-plane momentum
     *
     */
    public PhaseVector(double x, double xp, double y, double yp, double z, double zp)    {
        super(INT_SIZE);
        double[] arrVecInit = { x, xp, y, yp, z, zp, 1.0 };
        this.setVector(arrVecInit);
    }
    
    /**
     *  Copy Constructor
     *
     *  Creates new <code>PhaseVector</code> object which is a <b>deep copy</b> of the
     *  given argument.
     *
     *  @param  vecInit     initial value
     */
    public PhaseVector(PhaseVector vecInit) {
        super(vecInit);
    };
    
    
    
    /**
     *  Create a new instance of PhaseVector with specified initial value.
     *
     *  @param  arrVals      length 6 array of initial values
     *
     *  @exception  IllegalArgumentException  argument must be a length-six array
     */
    public PhaseVector(double arrVals[])    {
//        super(INT_SIZE);
//      this.setVector(arrVals);
        super(arrVals);
        this.setElem(IND.HOM, 1.0);
    }
    
    /**
     *  Create a new instance of PhaseVector with specified initial value.
     *
     *  @param  vecPos  position vector (x,y,z) in R3
     *  @param  vecMom  momentum vector (xp, yp, zp) in R3
     */
    public PhaseVector(R3 vecPos, R3 vecMom)    {
        this(vecPos.getx(), vecMom.getx(), vecPos.gety(), vecMom.gety(), vecPos.getz(), vecMom.getz());
    }
    
    /**
     * Create a new <code>PhaseVector</code> object and initialize it with the
     * data behind the <code>DataAdaptor</code> data source.
     * 
     * @param   daSource    data source containing initialization data
     * 
     * @throws DataFormatException      malformed data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    public PhaseVector(DataAdaptor daSource) throws DataFormatException {
        this();
        this.load(daSource);
    }
    
    /**
     * Create a new instance of PhaseVector with specified initial value specified 
     * by the formatted string argument.  The input
     * string may or may not contain the final coordinate which always has value 1.
     * 
     * The string should be formatted as
     * 
     *  "(x,x',y,y',z,zp')"
     * 
     * where x, x', y, y', z, z' are floating point representations.
     * 
     * @param  strTokens   token string representing values phase coordinates
     *
     * @exception  IllegalArgumentException    wrong number of tokens in argument (must be 6 or 7)
     * @exception  NumberFormatException       bad numeric value, un-parseable
     * 
     * @see PhaseVector#setVector(java.lang.String)
     */
    public PhaseVector(String   strTokens)    
        throws NumberFormatException, IllegalArgumentException
    {
        this();
        this.setVector(strTokens);
    }
    
//    /** 
//     * Return a deep copy object of the current <code>PhaseVector<code> object.
//     * Thus, the current object is unmodified and unreferenced.
//     * 
//     * @return      deep copy of the current object
//     */
//    public PhaseVector  copy()  {
//        return new PhaseVector(this);
//    }
    
    /*
     *  Assignment
     */
    

//    /**
//     * Create a new instance of PhaseVector with specified initial value specified 
//     * by the formatted string argument.  The input
//     * string may or may not contain the final coordinate which always has value 1.
//     * 
//     * The string should be formatted as
//     * 
//     *  "(x,x',y,y',z,zp')"
//     * 
//     * where x, x', y, y', z, z' are floating point representations.
//     * 
//     * @param  strValues   token string representing values phase coordinates
//     *
//     * @exception  IllegalArgumentException    wrong number of tokens in argument (must be 6 or 7)
//     * @exception  NumberFormatException       bad numeric value, unparseable
//     */
//    public void setVector(String strValues) 
//        throws DataFormatException, IllegalArgumentException
//    {
//        // Error check the number of token strings
//        StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()");
//        
//        if (tokArgs.countTokens() < 6)
//            throw new IllegalArgumentException("PhaseVector#setVector - wrong number of token strings: " + strValues);
//        
//        
//        // Extract initial phase coordinate values
//        int                 i;      // loop control
//        
//        for (i=0; i<5; i++)  {
//            String  strVal = tokArgs.nextToken();
//            double  dblVal = Double.valueOf(strVal).doubleValue();
//            
//            this.getMatrix().set(i,0, dblVal);
//        }
//        
//        this.getMatrix().set(6,0, 1.0);
//    }
//
//
//    /**
//     *  Set the element at index.  Note that you cannot change the last element value,
//     *  it must remain 1.
//     *
//     *  @param  i       index of new element value
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2,3,4,5}
//     */
//    public void setElem(int i, double dblVal) throws ArrayIndexOutOfBoundsException {
//        if (i>5) 
//            throw new ArrayIndexOutOfBoundsException("PhaseMatrix#set() - index greater than 5.");
//        
//        this.getMatrix().set(i,0, dblVal);
//    }
//    
//    /**
//     *  Set the element at index.  Note that you cannot change the last element value,
//     *  the projective coordinate value. It must remain 1.
//     *  
//     * @param i         index of vector where element will be set
//     * @param dblVal    value to which it is set
//     *
//     * @author Christopher K. Allen
//     * @since  Oct 7, 2013
//     */
//    public void setElem(IIndex i, double dblVal) {
//        this.getMatrix().set(i.val(), 0, dblVal);
//    }
//    
    /**
     * Must override this method to ignore any missing homogeneous coordinate 
     * since it is understood that this value is always there and unchanging.
     *
     * @see xal.tools.math.BaseVector#setVector(java.lang.String)
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2014
     */
    @Override
    public void setVector(String strValues) {
        
        // Error check the number of token strings
        StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()[]{}");
        
        if (tokArgs.countTokens() < 6)
            throw new IllegalArgumentException("Missing Values: You must have at least 6 values for a PhaseVector " + strValues);
        
        // Extract initial phase coordinate values
        for (int i=0; i<6; i++) {
            String  strVal = tokArgs.nextToken();
            double  dblVal = Double.valueOf(strVal).doubleValue();

            this.setElem(i,dblVal);
        }
        
        this.setElem(IND.HOM, 1.0);
    }
    
    /**
     * Must override this method to ignore any missing homogeneous coordinate 
     * since it is understood that this value is always there and unchanging.
     *
     * @see xal.tools.math.BaseVector#setVector(double[])
     *
     * @author Christopher K. Allen
     * @since  Sep 6, 2014
     */
    public void setVector(double[] arrVector) throws IllegalArgumentException {
        
        // Check the dimensions of the argument double array
        if (arrVector.length < 6)
            throw new IllegalArgumentException(
                    "Missing values: You need at least 6 values for a PhaseVector - " 
                   + arrVector
                   );
        
        // Set the elements of this array to that given by the corresponding 
        //  argument entries
        for (int i=0; i<6; i++) {
            double dblVal = arrVector[i];

            this.setElem(i, dblVal);
        }
        
        this.setElem(IND.HOM, 1.0);
    }

    /**
     *  Set the element at index.  Note that you cannot change the last element value,
     *  it must remain 1.
     *
     *  @param  i       index of new element value
     */
    public void setElem(PhaseIndex i, double dblVal)  {
        super.setElem(i, dblVal);
    }
    
    /**
     *  Set the x position coordinate
     */
    public void setx(double dblVal)  { this.setElem(0, dblVal); };
    
    /**
     *  Set the x momentum coordinate
     */
    public void setxp(double dblVal)  { this.setElem(1, dblVal); };
    
    /**
     *  Set the y position coordinate
     */
    public void sety(double dblVal)  { this.setElem(2, dblVal); };
    
    /**
     *  Set the y momentum coordinate
     */
    public void setyp(double dblVal)  { this.setElem(3, dblVal); };
    
    /**
     *  Set the z position coordinate
     */
    public void setz(double dblVal)  { this.setElem(4, dblVal); };
    
    /**
     *  Set the z momentum coordinate
     */
    public void setzp(double dblVal)  { this.setElem(5, dblVal); };
    
    
//    /** 
//     *  Return the element at index.
//     *
//     *  @return     the i-th element of the phase vector
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2,3,4,5,6}
//     */
//    public double   getElem(int i)  
//        throws ArrayIndexOutOfBoundsException
//    {
//        return this.getMatrix().get(i, 0);
//    }
//    
//    /** 
//     *  Return the element at index.
//     *
//     *  @return     the i-th element of the phase vector
//     */
//    public double   getElem(SquareMatrix.IIndex i)   {
//        return this.getMatrix().get(i.val(), 0);
//    }
//    
    /**
     *  Return the x position coordinate
     */
	@Units( "meters" )
    public double   getx()  { return this.getElem(0); };
    
    /**
     *  Return the x momentum coordinate
     */
	@Units( "radians" )
    public double   getxp() { return this.getElem(1); };
    
    /**
     *  Return the y position coordinate
     */
	@Units( "meters" )
    public double   gety()  { return this.getElem(2); };
    
    /**
     *  Return the y momentum coordinate
     */
	@Units( "radians" )
    public double   getyp() { return this.getElem(3); };
    
    /**
     *  Return the z momentum coordinate
     */
	@Units( "meters" )
    public double   getz()  { return this.getElem(4); };
    
    /**
     *  Return the z momentum coordinate
     */
	@Units( "radians" )
    public double   getzp() { return this.getElem(5); };
    
    /**
     *  Get position coordinates in R3.
     *
     *  @return     (x,y,z)
     */
	@NoEdit		// returns a new instance so should not edit
    public R3   getPosition()   { return new R3(getx(), gety(), getz()); };
    
    /**
     *  Get momentum coordinate in R3.
     *
     *  @return     (xp,yp,zp)
     */
	@NoEdit		// returns a new instance so should not edit
    public  R3  getMomentum()   { return new R3(getxp(), getyp(), getzp()); };
        
    
    
    
//    /*
//     * IArchive Interface
//     */    
//
//    /**
//     * Save the value of this <code>PhaseVector</code> to disk.
//     * 
//     * @param daptArchive   interface to data sink 
//     * 
//     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
//     */
//    public void save(DataAdaptor daptArchive) {
//        daptArchive.setValue(PhaseVector.ATTR_DATA, this.toString());
//    }
//
//    /**
//     * Restore the value of the this <code>PhaseVector</code> from the
//     * contents of a data archive.
//     * 
//     * @param daptArchive   interface to data source
//     * 
//     * @throws DataFormatException      malformed data
//     * 
//     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
//     */
//    public void load(DataAdaptor daptArchive) throws DataFormatException {
//        if ( daptArchive.hasAttribute(PhaseVector.ATTR_DATA) )  {
//            String  strValues = daptArchive.stringValue(PhaseVector.ATTR_DATA);
//            this.setVector(strValues);         
//        }
//    }
//    
    
    
//    /*
//     *  Object method overrides
//     */
//     
//    /**
//     *  Convert the vector contents to a string.
//     *
//     *  @return     vector value as a string (v0, v1, ..., v5)
//     */
//    @Override
//    public String   toString()  {
//
//        // Create vector string
//        String  strVec = "(";
//
//        for (int i=0; i<6; i++)
//            strVec = strVec + this.getElem(i) + ",";
//        strVec = strVec + this.getElem(6) + ")";
//        
//        return strVec;
//    }
//    
//    @Override
//    public boolean equals(Object o) {
//        if(o == this) { return true; }
//        if(! (o instanceof PhaseVector)) { return false; }
//        
//        PhaseVector target = (PhaseVector)o;
//        return getElem(0) == target.getElem(0) &&
//            getElem(1) == target.getElem(1) &&
//            getElem(2) == target.getElem(2) &&
//            getElem(3) == target.getElem(3) &&
//            getElem(4) == target.getElem(4) &&
//            getElem(5) == target.getElem(5);
//    }
//    
//    /**
//     * "Borrowed" implementation from AffineTransform, since it is based on
//     * double attribute values.  Must implement hashCode to be consistent with
//     * equals as specified by contract of hashCode in <code>Object</code>.
//     * 
//     * @return a hashCode for this object
//     */
//    @Override
//    public int hashCode() {
//        long bits = Double.doubleToLongBits(getElem(0));
//        bits = bits * 31 + Double.doubleToLongBits(getElem(1));
//        bits = bits * 31 + Double.doubleToLongBits(getElem(2));
//        bits = bits * 31 + Double.doubleToLongBits(getElem(3));
//        bits = bits * 31 + Double.doubleToLongBits(getElem(4));
//        bits = bits * 31 + Double.doubleToLongBits(getElem(5));
//        return (((int) bits) ^ ((int) (bits >> 32)));
//    }
//    
//
//    
//    
    
    /*
     *  Algebraic Operations
     */
    
//    /**
//     * Element by element in-place negation.  The current 
//     * <code>PhaseVector</code> object is changed in place.
//     * 
//     */
//    public void    negateEquals() {
//        double val;
//        for (PhaseIndex i : PhaseIndex.values()) {
//            val = this.getElem(i);
//            
//            this.setElem(i, -val);
//        }
//    }
//    
//    /** 
//     * Element by element negation.  A new object is returned and the
//     * current one is unmodified.
//     * 
//     * @return     antipodal vector of the current object
//     */
//    public PhaseVector negate()    {
//        PhaseVector    vecNeg = this.copy();
//        
//        vecNeg.negateEquals();
//        return vecNeg;
//    }
//    
//    /**
//     *  Vector nondestructive addition.  Note only the phase coordinates are added.
//     *
//     *  @param  vec     vector to be added
//     *  
//     *  @return         vector sum (componentwise)
//     */
//    public PhaseVector  plus(PhaseVector vec)    {
//        Jama.Matrix     matRes = this.getMatrix().plus( vec.getMatrix() );
//        matRes.set(6,0, 1.0);
//        
//        return new PhaseVector( matRes );
//    }
//    
//    /**
//     *  Vector in-place addition.  Note only the phase coordinates are added.
//     *
//     *  @param  vec     vector to be added
//     */
//    public void plusEquals(PhaseVector vec)    {
//        this.getMatrix().plusEquals( vec.getMatrix() );
//        this.getMatrix().set(6,0, 1.0);
//    }
//    
//    /** 
//     *  Nondestructive scalar multiplication
//     *
//     *  @param  s   scalar
//     *
//     *  @return     result of scalar multiplication
//     */
//    public PhaseVector times(double s) {
//        Jama.Matrix     matRes = this.getMatrix().times(s);
//        matRes.set(6,0, 1.0);
//        
//        return new PhaseVector( matRes );
//    }
//    
//    /** 
//     *  In-place scalar multiplication
//     *
//     *  @param  s   scalar
//     */
//    public void timesEquals(double s) {
//        this.getMatrix().times(s);
//        this.getMatrix().set(6,0, 1.0);
//    }
//    
//    /** 
//     *  Premultiply a PhaseMatrix by this PhaseVector.
//     *
//     *  @param  mat     matrix operator
//     *  @return         result of matrix-vector product
//     */
//    public PhaseVector times(PhaseMatrix mat) {
//        
//        PhaseVector     vecSum = new PhaseVector();
//        
//        for (PhaseMatrix.IND j : PhaseMatrix.IND.values()) {
//            
//            double  dblVal = 0.0;
//            
//            for (PhaseMatrix.IND i : PhaseMatrix.IND.values()) {
//                dblVal += this.getElem(i) * mat.getElem(i, j);
//            }
//            
//            vecSum.setElem(j,  dblVal);
//        }
//        
//        return vecSum;
//    }
//    
//    /**
//     *  Vector inner product operation using ONLY the <b>phase coordinates</b>.
//     *
//     *  @param  vec     second argument to inner product operation
//     *
//     *  @return         inner product (this,vec)
//     */
//    public double   innerProd(PhaseVector vec)  {
//        int     i;          // loop control
//        double  dblSum;     // running sum
//        
//        dblSum = 0.0;
//        for (i=0; i<6; i++) 
//            dblSum += this.getElem(i)*vec.getElem(i);
//        
//        return dblSum;
//    }
//    
    /**
     *  Vector outer product operation.  Returns the tensor outer product
     *  as a <code>PhaseMatrix</code> object
     *
     *  @param  vec     second argument to tensor product
     *
     *  @return         outer product = [ this_i*vec_j ]
     */
    public PhaseMatrix outerProd(PhaseVector vec)   {
        PhaseMatrix matRes = new PhaseMatrix();
        
        for (IND i : IND.values()) {
            for (IND j : IND.values()) {
                double dblVal = this.getElem(i)*vec.getElem(j);
                
                matRes.setElem(i.val(),j.val(), dblVal);
            }
        }
        return matRes;
    }
    
    /*
     * BaseVector Overrides
     */
    
    /**
     * Handles object creation required by the base class.
     *  
     * @see xal.tools.math.BaseVector#newInstance()
     *
     * @author Ivo List
     * @author Christopher K. Allen
     * @since  Jun 17, 2014
     */
    @Override
    protected PhaseVector newInstance() {
        return new PhaseVector();
    }

    /**
     *
     * @see xal.tools.math.BaseVector#newInstance(double[])
     *
     * @since  Jul 24, 2015   by Christopher K. Allen
     */
    @Override
    protected PhaseVector newInstance(double[] arrVecInt) {
        return new PhaseVector(arrVecInt);
    }

    /**
     * We need to redefine this method in order to set the
     * homogeneous coordinate back to unity.
     * 
     * @see xal.tools.math.BaseVector#assignZero()
     *
     * @author Christopher K. Allen
     * @since  Nov 6, 2014
     */
    @Override
    public void assignZero() {
        super.assignZero();
        super.setElem(IND.HOM, 1.0);
        
    }

    
    // 
    // Algebraic Operations
    //
    
    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#negate()
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public PhaseVector negate() {
        PhaseVector vecNeg = super.negate();

        vecNeg.setElem(IND.HOM, 1.0);
        
        return vecNeg;
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#negateEquals()
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public void negateEquals() {
        super.negateEquals();
        super.setElem(IND.HOM, 1.0);
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#plusEquals(xal.tools.math.BaseVector)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public void plusEquals(PhaseVector vecAdd) {
        super.plusEquals(vecAdd);
        super.setElem(IND.HOM, 1.0);
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#plus(xal.tools.math.BaseVector)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public PhaseVector plus(PhaseVector vecAdd) {
        PhaseVector vecSum = super.plus(vecAdd);
        
        vecSum.setElem(IND.HOM, 1.0);
        
        return vecSum;
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#minusEquals(xal.tools.math.BaseVector)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public void minusEquals(PhaseVector vecSub) {
        super.minusEquals(vecSub);
        super.setElem(IND.HOM, 1.0);
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#minus(xal.tools.math.BaseVector)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public PhaseVector minus(PhaseVector vecSub) {
        PhaseVector vecDif = super.minus(vecSub);
        
        vecDif.setElem(IND.HOM, 1.0);
        
        return vecDif;
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#times(double)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public PhaseVector times(double s) {
        PhaseVector vecScaled = super.times(s);
        
        vecScaled.setElem(IND.HOM, 1.0);
        
        return vecScaled;
    }

    /**
     *  Must override to protect the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#timesEquals(double)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public void timesEquals(double s) {
        super.timesEquals(s);
        super.setElem(IND.HOM, 1.0);
    }

    
    //
    //  Topological Operations
    //
    
    /**
     *  Must override to account for the homogeneous coordinate.
     *  
     * @see xal.tools.math.BaseVector#innerProd(xal.tools.math.BaseVector)
     *
     * @since  Jan 7, 2015   by Christopher K. Allen
     */
    @Override
    public double innerProd(PhaseVector v) throws IllegalArgumentException {
        return super.innerProd(v) - 1.0;
    }

    /**
     * Return the <i>l</i><sub>1</sub> norm of the vector.
     * Must override to account for the projective coordinate.
     *
     * @return     ||z||<sub>1</sub> = &Sigma;<sub><i>i&ne;6</i></sub> |<i>z<sub>i</sub></i>|
     *
     * @see xal.tools.math.BaseVector#norm1()
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    @Override
    public double   norm1()     { 
        int         i;          // loop control
        double      dblSum;     // running sum
        
        dblSum = 0.0;
        for (i=0; i<6; i++) 
            dblSum += Math.abs( this.getElem(i) );
        
        return dblSum;
    };
    
    /**
     * Return the <i>l</i><sub>2</sub> norm of the vector.
     * Must override to account for the projective coordinate.
     *
     * @return     ||z||<sub>2</sub> = [ &Sigma;<sub><i>i</i>&ne;6</sub> <i>z<sub>i</sub></i><sup>2</sup> ]<sup>1/2</sup>
     *
     * @see xal.tools.math.BaseVector#norm2()
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    @Override
    public double   norm2()     { 
        int         i;          // loop control
        double      dblSum;     // running sum
        
        dblSum = 0.0;
        for (i=0; i<6; i++) 
            dblSum += this.getElem(i)*this.getElem(i);
        
        return dblSum;
    }
    
    /**
     * Return the <i>l</i><sub>&infin; norm of the vector.
     * Must override to account for the projective coordinate.
     *
     * @return     ||<i>z</i>||<sub>&infin;</sub> = sup<sub><i>i</i>&ne;6</sub> |<i>z<sub>i</sub></i>|
     *
     * @see xal.tools.math.BaseVector#normInf()
     *
     * @author Christopher K. Allen
     * @since  Oct 11, 2013
     */
    @Override
    public double   normInf()     { 
        int         i;          // loop control
        double      dblMax;     // running maximum
        
        dblMax = 0.0;
        for (i=0; i<6; i++) 
            if (Math.abs( this.getElem(i) ) > dblMax ) 
                dblMax = Math.abs( this.getElem(i) );
        
        return dblMax;
    }
    
    
    /*
     * Object Method Overrides
     */
    
    /**
     * Creates and returns a deep copy of <b>this</b> vector.
     * 
     * @see xal.tools.math.BaseVector#clone()
     * 
     * @author Jonathan M. Freed
     * @since Jul 3, 2014
     */
    @Override
    public PhaseVector clone(){
        return new PhaseVector(this);
    }
    
    
    /*
     *  Internal Support
     */
    
//    /**
//     *  Construct a PhaseVector from a suitable Jama.Matrix.  Note that the
//     *  argument should be a new object not owned by another object, because
//     *  the internal matrix representation is assigned to the target argument.
//     *
//     *  @param  matInit     a 7x1 Jama.Matrix object
//     */
//    PhaseVector(Jama.Matrix matInit)  {
//        jamaVector = matInit;
//    }
//    
//    /**
//     *  Return the internal vector representation
//     *
//     *  @return     the Jama.Matrix object
//     */
//    Jama.Matrix   getMatrix()     { return jamaVector; }
//    


    
    /*
     *  Testing and Debugging
     */


//    /**
//     * Print this vector to standard out.
//     */
//    public void print() {
//        jamaVector.print( 10, 5 );
//    }
//    
//    
    /**
     *  Print the vector contents to an output stream,
     *  does not add new line.
     *
     *  @param  os      output stream object 
     */
    public void print(PrintWriter os)   {

        // Create vector string
        String  strVec = this.toString();

        // Send to output stream
        os.print(strVec);
    }
            
    /**
     *  Print the vector contents to an output stream, 
     *  add new line character.
     *
     *  @param  os      output stream object 
     */
    public void println(PrintWriter os)   {

        // Create vector string
        String  strVec = this.toString();

        // Send to output stream
        os.println(strVec);
    };
    
    /**
     * Print the vector contents to a String.
     */
    public String printString() {
        String strVec = "";
        for (int i=0; i<5; i++)
            strVec = strVec + this.getElem(i) + ",";
        strVec = strVec + this.getElem(6);
        return strVec;
    }
    
    /**
     *  Test driver
     */
    public static void main(String arrArgs[])   {
        PrintWriter     os = new PrintWriter( System.out );
        
        PhaseVector     z1 = new PhaseVector();
        os.print("Vector #1 = ");
        z1.println(os);
        
        PhaseVector     z2 = new PhaseVector(1.0, 0.0, 2.0, 0.0, 3.0, 0.0);
        os.print("Vector #2 = ");
        z2.println(os);
        
        PhaseVector     z3 = new PhaseVector("1.0 2.0 3.0 4.0 5.0 6.0");
        os.print("Vector #3 = ");
        z3.println(os);
        
        os.flush();
    }

}
