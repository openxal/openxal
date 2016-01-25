/*
 * PhaseMatrix.java
 *
 * Created on March 19, 2003, 2:32 PM
 */

package xal.tools.beam;

import java.util.EnumSet;

import Jama.Matrix;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r3.R3;
import xal.tools.math.r3.R3x3;
import xal.tools.math.r3.R3x3.POS;
import xal.tools.math.r4.R4x4;
import xal.tools.math.r6.R6;
import xal.tools.math.r6.R6x6;
import xal.tools.beam.PhaseVector;



/**
 *  <p>
 *  Represents a two-tensor on the space of homogeneous phase space coordinates 
 *  in three spatial dimensions.  Thus, each <code>PhaseMatrix</code> is an element of R7x7, 
 *  the set of real 7x7 matrices.  
 *  </p>
 *  <p>
 *  The coordinates in homogeneous phase space are as follows:
 *  <pre>
 *      (<i>x, x<sub>p</sub>, y, y<sub>p</sub>, z, z<sub>p</sub>,</i> 1)'
 *  </pre>
 *  where the prime indicates transposition and
 *  <pre>
 *      <i>x</i>  = x-plane position
 *      <i>x<sub>p</sub></i> = x-plane momentum
 *      <i>y</i>  = y-plane position
 *      <i>y<sub>p</sub></i> = y-plane momentum
 *      <i>z</i>  = z-plane position
 *      <i>z<sub>p</sub></i> = z-plane momentum
 *  </pre>
 *  </p>
 *  <p>
 *  Homogeneous coordinates are parameterizations of the projective spaces <b>P</b><sup><i>n</i></sup>.
 *  They are
 *  useful here to allow vector translations, normally produced by vector addition, to 
 *  be represented as matrix multiplications.  These operations can be embodied by 
 *  <code>PhaseMatrix</code>.  Thus, <code>PhaseMatrix</code> objects can represent any 
 *  linear operation, including translation, on <code>PhaseVector</code> objects.
 *  </p>
 * 
 * 
 * @author  Christopher Allen
 * @since   March, 2003
 * @version Oct, 2013
 *
 *  @see    SquareMatrix
 *  @see    PhaseVector
 *  @see    CovarianceMatrix
 */
public class PhaseMatrix extends SquareMatrix<PhaseMatrix> implements java.io.Serializable {

    
    /*
     * Internal Types
     */
    
    /** 
     * Enumeration for the element position indices of a homogeneous
     * phase space objects.  Extends the <code>PhaseIndex</code> class
     * by adding the homogeneous element position <code>HOM</code>.
     * 
     * @author  Christopher K. Allen
     * @since   Oct, 2013
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
        
        /** Index of the Z' (change in momentum) coordinate */
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
         * Return the integer value of the index position 
         */
        public int val()    { return i; };

        
        /*
         * Local Attributes
         */
        
        /** index value */
        private final int i;
        
        
        /*
         * Initialization
         */
        
        /** 
         * Default enumeration constructor 
         */
        IND(int i)  {
            this.i = i;
        }
    }

    
    /*
     * Global Constants
     */
    
    /** Serialization identifier */
    private static final long serialVersionUID = 1L;
    
    
    /** index of x position */
    public static final int    IND_X = 0;
    
    /** index of x' position */
    public static final int    IND_XP = 1;
    
    /** index of y position */
    public static final int    IND_Y = 2;
    
    /** index of y' position */
    public static final int    IND_YP = 3;
    
    /** index of z position */
    public static final int    IND_Z = 4;
    
    /** index of z' position */
    public static final int    IND_ZP = 5;
    
    /** index of homogeneous coordinate */
    public static final int    IND_HOM = 6;
    
   
    
    /** number of dimensions (DIM=7) */
    public static final int    INT_SIZE = 7;

    
    /*
     * Global Attributes
     */
    
//    /** matrix element parsing format - fixed */
//    final static private DecimalFormat FIXED_FORMAT = new DecimalFormat("####.########", //$NON-NLS-1$ 
//    		new DecimalFormatSymbols(Locale.ROOT)); 
//    
//    /** matrix element parsing format - scientific */
//    final static private DecimalFormat SCI_FORMAT = new DecimalFormat("0.00000000E00", //$NON-NLS-1$ 
//    		new DecimalFormatSymbols(Locale.ROOT));
    
    
    
    
    
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero phase matrix.
     *
     *  @return         zero vector
     */
    public static PhaseMatrix  zero()   {
        PhaseMatrix matZero = new PhaseMatrix();
        matZero.assignZero();
        matZero.setElem(IND.HOM, IND.HOM, 1.0);
        return matZero;
    }
    
    /**
     *  Create an identity phase matrix
     *
     *  @return         7x7 real identity matrix
     */
    public static PhaseMatrix  identity()   {
//        PhaseMatrix matIden = new PhaseMatrix();
//        matIden.assignIdentity();
//        matIden.setElem(IND.HOM, IND.HOM, 1.0);
        double[][]  arrInternal = new double[INT_SIZE][INT_SIZE];
        for (int i=0; i<INT_SIZE; i++)
            arrInternal[i][i] = 1.0;
        
        PhaseMatrix matIden = new PhaseMatrix( arrInternal );
        return matIden;
    }
    
    /**
     * <p>
     * Create a phase matrix representing a linear translation
     * operator on homogeneous phase space.  Multiplication by the 
     * returned <code>PhaseMatrix</code> object is equivalent to
     * translation by the given <code>PhaseVector</code> argument.
     * Specifically, if the argument <b>&Delta;</b> has coordinates
     * <br>
     * <br>
     * &nbsp; &nbsp;  <b>&Delta;</b> = (<i>&Delta;x, &Delta;x', &Delta;dy, &Delta;dy', 
     *                                     &Delta;dz, &Delta;dz'</i>, 1)<sup><i>T</i></sup>
     * <br>
     * <br>
     * then the returned matrix <b>T</b>(<b>&Delta;</b>) has the form
     * <pre>
     * 
     *          |1 0 0 0 0 0 <i>&Delta;x</i> |
     *          |0 1 0 0 0 0 <i>&Delta;x</i>'|
     *   <b>T</b>(<b>&Delta;</b>) = |0 0 1 0 0 0 <i>&Delta;y</i> |
     *          |0 0 0 1 0 0 <i>&Delta;y</i>'|
     *          |0 0 0 0 1 0 <i>&Delta;z</i> |
     *          |0 0 0 0 0 1 <i>&Delta;z</i>'|
     *          |0 0 0 0 0 0  1 |
     * </pre>
     * Consequently, given a phase vector <b>v</b> of the form
     * <pre>
     *          |<i>x</i> |
     *          |<i>x'</i>|
     *      <b>v</b> = |<i>y</i> |
     *          |<i>y'</i>|
     *          |<i>z</i> |
     *          |<i>z'</i>|
     *          |1 |
     * </pre>         
     * Then operation on <b>v</b> by <b>T</b>(<b>&Delta;</b>) has the result
     * <pre>
     *           |<i>x + &Delta;x</i> |
     *           |<i>x'+ &Delta;x'</i>|
     *   <b>T</b>(<b>&Delta;</b>)<b>v</b> = |<i>y + &Delta;y</i> |
     *           |<i>y'+ &Delta;y'</i>|
     *           |<i>z + &Delta;z</i> |
     *           |<i>z'+ &Delta;z'</i>|
     *           |  1    |
     * </pre>
     * which we see is equivalent to the simple vector addition <b>v</b> + <b>&Delta;</b>.
     * 
     *  @param  vecTrans    translation vector <b>&Delta;</b>
     *  
     *  @return             translation operator <b>T</b>(<b>&Delta;</b>) as a phase matrix     
     */
    public static PhaseMatrix  translation(PhaseVector vecTrans)   {
        PhaseMatrix     matTrans = PhaseMatrix.identity();
        
        matTrans.setElem(IND.X,  IND.HOM, vecTrans.getx());
        matTrans.setElem(IND.Xp, IND.HOM, vecTrans.getxp());
        matTrans.setElem(IND.Y,  IND.HOM, vecTrans.gety());
        matTrans.setElem(IND.Yp, IND.HOM, vecTrans.getyp());
        matTrans.setElem(IND.Z,  IND.HOM, vecTrans.getz());
        matTrans.setElem(IND.Zp, IND.HOM, vecTrans.getzp());
        
        return matTrans;
    }
    
    /**
     * <p>
     * Create a phase matrix representing a linear translation
     * operator on homogeneous phase space that only affects the
     * spatial coordinates.  Multiplication by the 
     * returned <code>PhaseMatrix</code> object is equivalent to
     * translation by the given <code>R3</code> argument projected
     * into phase space.
     * Specifically, if the argument <b>&Delta;</b> has coordinates
     * <br>
     * <br>
     * &nbsp; &nbsp;  <b>&Delta;</b> = (<i>&Delta;x, &Delta;dy, &Delta;dz</i>)<sup><i>T</i></sup>
     * <br>
     * <br>
     * then the returned matrix <b>T</b>(<b>dv</b>) has the form
     * <pre>
     * 
     *          |1 0 0 0 0 0 <i>&Delta;x</i>|
     *          |0 1 0 0 0 0 0 |
     *  <b>T</b>(<b>dv</b>) = |0 0 1 0 0 0 <i>&Delta;y</i>|
     *          |0 0 0 1 0 0 0 |
     *          |0 0 0 0 1 0 <i>&Delta;z</i>|
     *          |0 0 0 0 0 1 0 |
     *          |0 0 0 0 0 0  1|
     * </pre>
     * which is the translation operator in phase space restricted to the
     * spatial coordinates.
     *
     * <p>
     * See <code>{@link PhaseMatrix#translation(PhaseVector)}</code> for further
     * discussion of translation operators and their representation by 
     * homogeneous phase-space matrices.
     * </p>
     *
     * @param vecDispl      the spatial displacement vector <b>&Delta;</b>
     * 
     * @return              the translation operator matrix representation <b>T</b>(<b>&Delta;</b>)
     *
     * @author Christopher K. Allen
     * @since  Aug 25, 2011
     * 
     * @see PhaseMatrix#translation(PhaseVector)
     */
    public static PhaseMatrix   spatialTranslation(R3 vecDispl) {
        
        PhaseMatrix     matTrans = PhaseMatrix.identity();
        
        matTrans.setElem(IND.X,  IND.HOM, vecDispl.getx());
        matTrans.setElem(IND.Xp, IND.HOM, 0);
        matTrans.setElem(IND.Y,  IND.HOM, vecDispl.gety());
        matTrans.setElem(IND.Yp, IND.HOM, 0);
        matTrans.setElem(IND.Z,  IND.HOM, vecDispl.getz());
        matTrans.setElem(IND.Zp, IND.HOM, 0);
        
        return matTrans;
    }
      
    /**
     * <p>
     * Compute the rotation matrix in phase space that is essentially the 
     * Cartesian product of the given rotation matrix in <i>SO</i>(3).  That is,
     * if the given argument is the rotation <b>O</b>, the returned matrix, 
     * denoted <b>M</b>, is the <b>M</b> = <b>O</b>&times;<b>O</b>&times;<b>I</b> embedding
     * into homogeneous phase space <b>R</b><sup>6&times;6</sup>&times;{1}. Thus, 
     * <b>M</b> &in; <i>SO</i>(6) &sub; <b>R</b><sup>6&times;6</sup>&times;{1}.   
     * </p>
     * <p>
     * Viewing phase-space as a 6D manifold built as the tangent bundle over
     * <b>R</b><sup>3</sup> configuration space, then the fibers of 3D configuration space at a 
     * point (<i>x,y,z</i>) are represented by the Cartesian planes (<i>x',y',z'</i>).  The returned
     * phase matrix rotates these fibers in the same manner as their base point (<i>x,y,z</i>).  
     * </p>
     * <p>
     * This is a convenience method to build the above rotation matrix in <i>SO</i>(7).
     * </p>
     * 
     * @param matSO3    a rotation matrix in three dimensions, i.e., a member of <i>SO</i>(3) &sub; <b>R</b><sup>3&times;3</sup> 
     * 
     * @return  rotation matrix in <i>S0</i>(7) which is direct product of rotations in <i>S0</i>(3) 
     */
    public static PhaseMatrix  rotationProduct(R3x3 matSO3)  {

        // Populate the phase rotation matrix
        PhaseMatrix matSO7 = PhaseMatrix.identity();
        
        int         m, n;       // indices into the SO(7) matrix
        double      val;        // matSO3 matrix element
        
        for (POS pos : POS.values())  {
            m = 2*pos.row();
            n = 2*pos.col();
            
            val = matSO3.getElem(pos);
            
            matSO7.setElem(m,  n,   val);   // configuration space
            matSO7.setElem(m+1,n+1, val);   // momentum space
        }
        return matSO7;
    }
    
    
    /**
     *  Create a PhaseMatrix instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 7x7=49 numeric values
     *  
     *  @return             phase matrix with elements initialized by the given numeric tokens 
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static PhaseMatrix parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new PhaseMatrix(strTokens);
    }
    
    /**
     * Create a new <code>PhaseMatrix</code> object and initialize with the data 
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
    public static PhaseMatrix   loadFrom(DataAdaptor daSource) throws DataFormatException {
        PhaseMatrix     matNew = new PhaseMatrix(daSource);
        
        return matNew;
    }
    
    
    
    /*
     * Object Overrides
     */
    
    /**
     * Creates and returns a deep copy of this matrix.
     *
     * @see xal.tools.math.BaseMatrix#clone()
     *
     * @author Christopher K. Allen
     * @since  Jul 3, 2014
     */
    @Override
    public PhaseMatrix clone() {
        return new PhaseMatrix(this);
    }

    
    /*
     * Initialization
     */
    
    /*
         *  Assignment
         */
        
    //    /**
    //     *  Parsing assignment - set the <code>PhaseMatrix</code> value
    //     *  according to a token string of element values.  
    //     *
    //     *  The token string argument is assumed to be one-dimensional and packed by
    //     *  column (aka FORTRAN).
    //     *
    //     *  @param  strValues   token vector of 7x7=49 numeric values
    //     *
    //     *  @exception  IllegalArgumentException    wrong number of token strings
    //     *  @exception  NumberFormatException       bad number format, unparseable
    //     */
    //    public void setMatrix(String strValues)
    //        throws NumberFormatException, IllegalArgumentException
    //    {
    //        
    //        // Error check the number of token strings
    //        StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()[]{}"); //$NON-NLS-1$
    //        
    //        if (tokArgs.countTokens() != 49)
    //            throw new IllegalArgumentException("PhaseMatrix#setMatrix - wrong number of token strings: " + strValues); //$NON-NLS-1$
    //        
    //        
    //        // Extract initial phase coordinate values
    //        for (int i=0; i<INT_SIZE; i++)
    //            for (int j=0; j<INT_SIZE; j++) {
    //                String  strVal = tokArgs.nextToken();
    //                double  dblVal = Double.valueOf(strVal).doubleValue();
    //            
    //                this.setElem(i,j, dblVal);
    //            }
    //    }
        
    //    /**
    //     *  Element assignment - assigns matrix element to the specified value
    //     *
    //     *  @param  i       row index
    //     *  @param  j       column index
    //     *  @param  s       new matrix element value
    //     *
    //     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2,3,4,5,6}
    //     */
    //    public void setElem(int i, int j, double s) 
    //        throws ArrayIndexOutOfBoundsException
    //    {
    //        this.getMatrix().set(i,j, s);
    //    }
        
    /**
     * Handles object creation required by the base class. 
     *
     * @see xal.tools.math.BaseMatrix#newInstance()
     *
     * @author Ivo List
     * @author Christopher K. Allen
     * @since  Jun 17, 2014
     */
    @Override
    protected PhaseMatrix newInstance() {
        return new PhaseMatrix();
    }

//    /**
//     *
//     * @see xal.tools.math.BaseMatrix#newInstance(Jama.Matrix)
//     *
//     * @since  Jul 17, 2015   by Christopher K. Allen
//     */
//    @Override
//    protected PhaseMatrix newInstance(Matrix impInit) {
//        
//        M   matNewInst = this.newInstance();
//        
//        ((BaseMatrix<M>)matNewInst).assignMatrix(impInit);
//        
//        return matNewInst;
//    }

    /** 
     *  Creates a new instance of PhaseMatrix initialized to zero.
     */
    public PhaseMatrix() {
        super(INT_SIZE);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    
    /**
     *  Copy Constructor - create a <b>deep copy</b> of the target phase matrix.
     *
     *  @param  matInit     initial value
     */
    public PhaseMatrix(PhaseMatrix matInit) {
        super(matInit);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    
    /**
     * Create a new <code>PhaseMatrix</code> object and initialize with the data 
     * source behind the <code>DataAdaptor</code> interface.
     * 
     * @param   daSource    data source containing initialization data
     * 
     * @throws DataFormatException      malformed data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    public PhaseMatrix(DataAdaptor daSource) throws DataFormatException {
        this();
        this.load(daSource);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    
    /**
     *  Parsing Constructor - create a PhaseMatrix instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strValues   token vector of 7x7=49 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     * 
     *  @see    PhaseMatrix#setMatrix(java.lang.String)
     */
    public PhaseMatrix(String strValues)    
        throws IllegalArgumentException, NumberFormatException
    {
        super(INT_SIZE, strValues);
        this.setElem(IND.HOM, IND.HOM, 1.0);

//        this.setMatrix(strValues);
        
//        // Error check the number of token strings
//        StringTokenizer     tokArgs = new StringTokenizer(strTokens, " ,()[]{}");
//        
//        if (tokArgs.countTokens() != 49)
//            throw new IllegalArgumentException("PhaseMatrix(strTokens) - wrong number of token strings: " + strTokens);
//        
//        
//        // Extract initial phase coordinate values
//        int         i, j;       // matrix indices
//        int                 cntIndex = 0;
//        
//        for (i=0; i<DIM; i++)
//            for (j=0; j<DIM; j++) {
//                String  strVal = tokArgs.nextToken();
//                double  dblVal = Double.valueOf(strVal).doubleValue();
//            
//                this.setElem(i,j, dblVal);
//            }
    }
    
    /**
     * Initializing constructor for class <code>PhaseMatrix</code>.  The values of
     * the new matrix object are set to the given Java primitive type array (the
     * array is not modified).
     * 
     * @param arrValues initial values for the new matrix
     * 
     * @throws ArrayIndexOutOfBoundsException the argument must have dimensions 7&times;7
     *
     * @author Christopher K. Allen
     * @since  Oct 7, 2013
     */
    public PhaseMatrix(double[][] arrValues) throws ArrayIndexOutOfBoundsException {
        super(arrValues);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    


    /*
     *  Assignment
     */
    
//    /**
//     *  Parsing assignment - set the <code>PhaseMatrix</code> value
//     *  according to a token string of element values.  
//     *
//     *  The token string argument is assumed to be one-dimensional and packed by
//     *  column (aka FORTRAN).
//     *
//     *  @param  strValues   token vector of 7x7=49 numeric values
//     *
//     *  @exception  IllegalArgumentException    wrong number of token strings
//     *  @exception  NumberFormatException       bad number format, unparseable
//     */
//    public void setMatrix(String strValues)
//        throws NumberFormatException, IllegalArgumentException
//    {
//        
//        // Error check the number of token strings
//        StringTokenizer     tokArgs = new StringTokenizer(strValues, " ,()[]{}"); //$NON-NLS-1$
//        
//        if (tokArgs.countTokens() != 49)
//            throw new IllegalArgumentException("PhaseMatrix#setMatrix - wrong number of token strings: " + strValues); //$NON-NLS-1$
//        
//        
//        // Extract initial phase coordinate values
//        for (int i=0; i<INT_SIZE; i++)
//            for (int j=0; j<INT_SIZE; j++) {
//                String  strVal = tokArgs.nextToken();
//                double  dblVal = Double.valueOf(strVal).doubleValue();
//            
//                this.setElem(i,j, dblVal);
//            }
//    }
    
//    /**
//     *  Element assignment - assigns matrix element to the specified value
//     *
//     *  @param  i       row index
//     *  @param  j       column index
//     *  @param  s       new matrix element value
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2,3,4,5,6}
//     */
//    public void setElem(int i, int j, double s) 
//        throws ArrayIndexOutOfBoundsException
//    {
//        this.getMatrix().set(i,j, s);
//    }
    
    /**
     *  Element assignment - assigns matrix element to the specified value
     *
     *  @param  iRow    row index
     *  @param  iCol    column index
     *  @param  s       new matrix element value
     *
     */
    public void setElem(IND iRow,IND iCol, double s)   {
        super.setElem(iRow, iCol, s);
    }
    
    /**
     * Explicitly enforce the homogeneous nature of this matrix.  That is,
     * make sure that it is can represent a linear operator on 6D
     * projective space which does not translate vectors.  The condition 
     * is that the last row and last column consist of all zeros, except for
     * the diagonal element, which is 1.
     *   
     * @author Christopher K. Allen
     * @since  Oct 10, 2013
     */
    public void homogenize() {
        for (IND i : IND.values()) {
            this.setElem(IND.HOM, i, 0.0);
            this.setElem(i, IND.HOM, 0.0);
        }
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    
    
    /*
     * Matrix Operations
     */
    
    /**
     * <p>
     * Projects the <code>PhaseMatrix</code> onto the space of
     * 6&times;6 matrices.  The projective dimension of this phase matrix
     * is lost in the projection, that is, the homogeneous coordinate and
     * all the actions associated with it (primarily translations).
     * </p>
     * <p>
     * This method is useful when the phase matrix represents the 
     * statistics of a centered beam, or when it represents a transfer
     * map without any translation.
     * </p>
     * 
     * @return      the top 6&times;6 diagonal block of this matrix 
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public R6x6  projectR6x6() {
        
        R6x6    matProj = new R6x6();
        
        for (IND i : IND.valuesPhase()) 
            for (IND j : IND.valuesPhase()) {
                double  dblVal = this.getElem(i, j);
                
                matProj.setElem(i, j, dblVal);
            }
        
        return matProj;
    }
    
    /**
     * <p>
     * Projects the <code>PhaseMatrix</code> onto the space of
     * 4&times;4 matrices.  The projective dimension of this phase matrix
     * is lost in the projection, as is the longitudinal components of the
     * matrix. that is, the last three columns and the last three rows are
     * truncated.
     * </p>
     * <p>
     * This method is useful when the phase matrix represents the 
     * transverse properties of a beam, or when it represents a transverse
     * action on a beam.
     * </p>
     * 
     * @return      the top 6&times;6 diagonal block of this matrix 
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public R4x4  projectR4x4() {
        
        R4x4    matProj = new R4x4();
        
        for (R4x4.IND i : R4x4.IND.values()) 
            for (R4x4.IND j :R4x4.IND.values()) {
                double  dblVal = this.getElem(i, j);
                
                matProj.setElem(i, j, dblVal);
            }
        
        return matProj;
    }
    
    /**
     * Projects the <i>i<sup>th</sup></i> row onto <b>R</b><sup>6</sup>.
     * Specifically, the projective element (the 7<i><sup>th</sup></i> element
     * in this case) is dropped and that part of the <i>i<sup>th</sup></i> row 
     * in the 6 dimensional phase space is returned.
     *  
     * @param i index of the matrix row to be returned, <i>i</i> &in; {0,...,5}
     *  
     * @return  matrix row at the above index, less the final projective element 
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     * 
     * @see #projectRow(int)
     */
    public R6 projectRow(IND i) {
        return this.projectRow( i.val() );
//        R6  vecProj  = new R6();
//        
//        for (IND j : IND.valuesPhase()) {
//            double dblVal = this.getElem(i,  j);
//            
//            vecProj.setElem(j, dblVal);
//        }
//        
//        return vecProj;
    }
    
    /**
     * Projects the <i>i<sup>th</sup></i> row onto <b>R</b><sup>6</sup>.
     * Specifically, the projective element (the 7<i><sup>th</sup></i> element
     * in this case) is dropped and that part of the <i>i<sup>th</sup></i> row 
     * in the 6 dimensional phase space is returned.
     *  
     * @param i index of the matrix row to be returned, <i>i</i> &in; {0,...,5}
     *  
     * @return  matrix row at the above index, less the final projective element 
     *
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public R6 projectRow(int i) {
        R6  vecProj  = new R6();
        
        for (int j=0; j<this.getSize() -1; j++) {
            double dblVal = this.getElem(i,  j);
            
            vecProj.setElem(j, dblVal);
        }
        
        return vecProj;
    }

    /**
     * Projects the <i>j<sup>th</sup></i> column onto <b>R</b><sup>6</sup>.
     * Specifically, the projective element (the 7<i><sup>th</sup></i> element
     * in this case) is dropped and that part of the <i>j<sup>th</sup></i> column 
     * in the 6 dimensional phase space is returned.
     *  
     * @param j index of the matrix column to be returned, <i>j</i> &in; {0,...,5}
     *  
     * @return  matrix row at the above index, less the final projective element 
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     * 
     * @see #projectColumn(int)
     */
    public R6 projectColumn(IND j) {
        
        return this.projectColumn( j.val() );
//        R6  vecProj  = new R6();
//        
//        for (IND i : IND.valuesPhase()) {
//            double dblVal = this.getElem(i,  j);
//            
//            vecProj.setElem(i, dblVal);
//        }
//        
//        return vecProj;
    }

    /**
     * Projects the <i>j<sup>th</sup></i> column onto <b>R</b><sup>6</sup>.
     * Specifically, the projective element (the 7<i><sup>th</sup></i> element
     * in this case) is dropped and that part of the <i>j<sup>th</sup></i> column 
     * in the 6 dimensional phase space is returned.
     *  
     * @param j index of the matrix column to be returned, <i>j</i> &in; {0,...,5}
     *  
     * @return  matrix row at the above index, less the final projective element 
     *
     * @author Christopher K. Allen
     * @since  Oct 16, 2013
     */
    public R6 projectColumn(int j) {
        R6  vecProj  = new R6();
        
        for (int i=0; i<this.getSize()-1; i++) {
            double dblVal = this.getElem(i,  j);
            
            vecProj.setElem(i, dblVal);
        }
        
        return vecProj;
    }
    
    
    /*
     *  Algebraic Operations
     */
    
    /**
     * <p>
     *  Non-destructive matrix addition.  The homogeneous pivot
     *  element on the diagonal is unchanged at value 1.
     *  </p>
     *  <h3>NOTE:</h3>
     *  <p>
     *  BE VERY CAREFUL when using this function.  The homogeneous coordinates
     *  are not meant for addition operations.
     *  </p>
     *
     *  @param  matAddend     matrix to be added to this
     *
     *  @return         element wise sum of two matrices
     */
    public PhaseMatrix  plus(PhaseMatrix matAddend)   {
        PhaseMatrix matSum = super.plus(matAddend);
        matSum.setElem(IND.HOM,IND.HOM, 1.00);
        return matSum;
    }
    
    /** 
     *  <p>
     *  In-place matrix addition. The homogeneous pivot
     *  element on the diagonal is unchanged at value 1.
     *  </p>
     *  <h3>NOTE:</h3>
     *  <p>
     *  BE VERY CAREFUL when using this function.  The homogeneous coordinates
     *  are not meant for addition operations.
     *  </p>
     *
     *  @param  matAddend     matrix to be added to this (result replaces this)
     */
    public void plusEquals(PhaseMatrix  matAddend)    {
        super.plusEquals(matAddend);
        this.setElem(IND.HOM,IND.HOM, 1.00);
    }
    
    /**
     * <p>
     *  Non-destructive matrix subtraction.  The homogeneous pivot
     *  element on the diagonal is unchanged at value 1.
     *  </p>
     *  <h3>NOTE:</h3>
     *  <p>
     *  BE VERY CAREFUL when using this function.  The homogeneous coordinates
     *  are not meant for subtraction operations.
     *  </p>
     *
     *  @param  matSub     matrix to be subtracted from this one (subtrahend)
     *
     *  @return         difference of this matrix and the given one
     */
    public PhaseMatrix  minus(PhaseMatrix matSub)   {
        PhaseMatrix matDif = super.minus(matSub);
        matDif.setElem(IND.HOM,IND.HOM, 1.00);
        return matDif;
    }
    
    /**
     *  <p>
     *  In-place matrix subtraction. The homogeneous pivot
     *  element on the diagonal is unchanged at value 1.
     *  </p>
     *  <h3>NOTE:</h3>
     *  <p>
     *  BE VERY CAREFUL when using this function.  The homogeneous coordinates
     *  are not meant for addition operations.
     *  </p>
     *
     *  @param  matSub     matrix to be subtracted from this matrix (result replaces this)
     */
    public void minusEquals(PhaseMatrix  matSub)    {
        super.minusEquals( matSub );
        this.setElem(IND.HOM,IND.HOM, 1.00);
    }
    
    
    /*
     * Topological Operations
     */
    
    /**
     * We must redefine the norm of any matrix on projective space to 
     * eliminate the homogeneous coordinate.  See the base class
     * <code>{@link xal.tools.math.BaseMatrix}</code> for information on the specific
     * norm.
     * 
     * @see xal.tools.math.BaseMatrix#max()
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2013
     */
    @Override
    public double   max() {
        R6x6    matLin = this.projectR6x6();
        double  dblNorm = matLin.max();
        
        return dblNorm;
    }
    
    /**
     * We must redefine the norm of any matrix on projective space to 
     * eliminate the homogeneous coordinate.  See the base class
     * <code>{@link xal.tools.math.BaseMatrix}</code> for information on the specific
     * norm.
     * 
     * @see xal.tools.math.BaseMatrix#normInf()
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2013
     */
    @Override
    public double   normInf() {
        R6x6    matLin = this.projectR6x6();
        double  dblNorm = matLin.normInf();
        
        return dblNorm;
    }
    
    /**
     * We must redefine the norm of any matrix on projective space to 
     * eliminate the homogeneous coordinate.  See the base class
     * <code>{@link xal.tools.math.BaseMatrix}</code> for information on the specific
     * norm.
     * 
     * @see xal.tools.math.BaseMatrix#norm1()
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2013
     */
    @Override
    public double   norm1() {
        R6x6    matLin = this.projectR6x6();
        double  dblNorm = matLin.norm1();
        
        return dblNorm;
    }
    
    /**
     * We must redefine the norm of any matrix on projective space to 
     * eliminate the homogeneous coordinate.  See the base class
     * <code>{@link xal.tools.math.BaseMatrix}</code> for information on the specific
     * norm.
     * 
     * @see xal.tools.math.BaseMatrix#norm2()
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2013
     */
    @Override
    public double   norm2() {
        R6x6    matLin = this.projectR6x6();
        double  dblNorm = matLin.norm2();
        
        return dblNorm;
    }
    
    /**
     * We must redefine the norm of any matrix on projective space to 
     * eliminate the homogeneous coordinate.  See the base class
     * <code>{@link xal.tools.math.BaseMatrix}</code> for information on the specific
     * norm.
     * 
     * @see xal.tools.math.BaseMatrix#normF()
     *
     * @author Christopher K. Allen
     * @since  Nov 21, 2013
     */
    @Override
    public double   normF() {
        R6x6    matLin = this.projectR6x6();
        double  dblNorm = matLin.normF();
        
        return dblNorm;
    }
    
//    /**
//     *  Set a submatrix within the phase matrix.
//     *
//     *  @param  i0      row index of upper left block
//     *  @param  i1      row index of lower right block
//     *  @param  j0      column index of upper left block
//     *  @param  j1      column index of lower right block
//     *  @param  arrSub  two-dimensional sub element array
//     *
//     *  @exception  ArrayIndexOutOfBoundsException  submatrix does not fit into 7x7 phase matrix
//     */
//    public void setSubMatrix(int i0, int i1, int j0,  int j1, double[][] arrSub)
//        throws ArrayIndexOutOfBoundsException
//    {
//        Jama.Matrix matSub = new Matrix(arrSub);
//        
//        this.getMatrix().setMatrix(i0,i1,j0,j1, matSub);
//    }
    
    
    /*
     *  Matrix Properties
     */

//    /**
//     *  Return matrix element value.  Get matrix element value at specified index
//     *
//     *  @param  i       row index
//     *  @param  j       column index
//     *
//     *  @return         the element value at the given index
//     *  
//     *  @exception  ArrayIndexOutOfBoundsException  index must be in {0,1,2,3,4,5,6}
//     */
//    public double getElem(int i, int j) 
//        throws ArrayIndexOutOfBoundsException
//    {
//        return this.getMatrix().get(i,j);
//    }

//    /**
//     *  Return matrix element value.  Get matrix element value at specified index
//     *
//     *  @param  iRow    row index
//     *  @param  iCol    column index
//     *
//     *  @return         the matrix element value at the given index
//     */
//    public double getElem(IND iRow, IND iCol)  {
//        return super.getElem(iRow, iCol);
//    }
//    

//    /**
//     *  Check if matrix is symmetric.  
//     * 
//     *  @return true if matrix is symmetric 
//     */
//    public boolean isSymmetric()   {
//        int     i,j;        //loop control variables
//        
//        for (i=0; i<INT_SIZE; i++)
//            for (j=i+1; j<INT_SIZE; j++) {
//                if (getElem(i,j) != getElem(j,i) )
//                    return false;
//            }
//        return true;
//    }

    


//    /*
//     * IArchive Interface
//     */    
//     
//    /**
//     * Save the value of this <code>PhaseMatrix</code> to a data sink 
//     * represented by the <code>DataAdaptor</code> interface.
//     * 
//     * @param daptArchive   interface to data sink 
//     * 
//     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
//     */
//    public void save(DataAdaptor daptArchive) {
//        daptArchive.setValue(PhaseMatrix.ATTR_DATA, this.toString());
//    }
//
//    /**
//     * Restore the value of the this <code>PhaseMatrix</code> from the
//     * contents of a data archive.
//     * 
//     * @param daptArchive   interface to data source
//     * 
//     * @throws DataFormatException      malformed data
//     * @throws IllegalArgumentException wrong number of string tokens
//     * 
//     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
//     */
//    public void load(DataAdaptor daptArchive) throws DataFormatException {
//        if ( daptArchive.hasAttribute(PhaseMatrix.ATTR_DATA) )  {
//            String  strValues = daptArchive.stringValue(PhaseMatrix.ATTR_DATA);
//            this.setMatrix(strValues);         
//        }
//    }
//    


    /*
     *  Object method overrides
     */
     
//    /**
//     * Return true if this object is equal to o, false otherwise.
//     */
//    @Override
//    public boolean equals(Object o) {
//        if (o == this) return true;
//        if ( !(o instanceof PhaseMatrix)) return false;
//        PhaseMatrix pm = (PhaseMatrix)o;
//        for (int i=0; i<INT_SIZE; i++) {
//            for (int j= 0; j<INT_SIZE; j++) {
//                if (!(this.getElem(i,j) == (pm.getElem(i,j)))) return false;
//            }
//       }
//       
//       return true;
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
//        long bits = 0;
//        for (int i=0; i<INT_SIZE; i++) {
//            for (int j= 0; j<INT_SIZE; j++) {
//                bits = bits * 31 + Double.doubleToLongBits(getElem(i,j));;
//            }
//        }
//        
//        return (((int) bits) ^ ((int) (bits >> 32)));
//    }
    
    
   /*
    * Beam Operations
    */
     
//    /**
//     * Compute and return the betatron phase advance for a particle produced
//     * by this matrix when used as a transfer matrix.
//     * 
//     * @param   twissOld    Twiss parameter before application of matrix
//     * @param   twissNew    Twiss parameter after application of matrix
//     * 
//     * @return  vector (sigx,sigy,sigz) of phase advances in <b>radians</b>
//     */
//    public R3   compPhaseAdvance(Twiss[] twissOld, Twiss[] twissNew)  {
//        
//        int     i;          // loop control
//        int     iElem;      // matrix element index
//        double  dblR12;     // sub-matrix element R12
//        double  dblPhsAd;   // phase advance
//        double  betaOld, betaNew;
//        R3      vecPhsAd = new R3();    // returned set of phase advances
//        
//        for (i=0; i<3; i++) {           // Loop through each plane
//
//            iElem = 2*i;
//            /*
//             dblTrace = getElem(iElem,iElem) + getElem(iElem+1,iElem+1);
//             
//             if (dblTrace >= 2.0)    {
//             dblPhsAd = 0.0;
//             
//             } else if (dblTrace <= -2.0)    {
//             dblPhsAd = Math.PI;
//             
//             } else  {
//             dblPhsAd = Math.acos(0.5*dblTrace);
//             
//             }
//             */
//            dblR12 = getElem(iElem, iElem+1);
//            betaOld = twissOld[i].getBeta();
//            betaNew = twissNew[i].getBeta();
//            dblPhsAd = Math.asin(dblR12/Math.sqrt(betaOld * betaNew) );
//            
//            vecPhsAd.set(i, dblPhsAd);
//            
//        }
//        
//        return vecPhsAd;
//    }
//    
//    /** 
//     * Calculate the fixed point solution vector representing the closed orbit at the location of this element.
//     * We find the fixed point for the six phase space coordinates.
//     * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 6x6 submatrix less the identity
//     * matrix and <code>b</code> is the 7th column excluding the 7th row element.  The reason for this is that the
//     * fixed point is defined by the point for which the transfer map maps to the same point.  This is
//     * <code>M * v = v</code>.  
//     * 
//     * @return the fixed point solution
//     */
//    public PhaseVector calculateFixedPoint() {
//        Matrix A = m_matPhase.getMatrix( 0, IND_ZP, 0, IND_ZP ).minus( Matrix.identity(IND_ZP+1, IND_ZP+1) );
//        Matrix b = m_matPhase.getMatrix( 0, IND_ZP, IND_HOM, IND_HOM ).times( -1 );
//
//        //sako
//        //Matrix MZ = m_matPhase.getMatrix(IND_Z,IND_ZP,IND_Z,IND_ZP);
//        //      System.out.println("det(MZ), det(A) = "+MZ.det()+" "+A.det());
//        //      System.out.println("###### MZ = ("+MZ.get(0,0)+","+MZ.get(0,1)+")("+MZ.get(1,0)+","+MZ.get(1,1)+")");
//
//        PhaseVector sol;
//
//        if (A.det()==0) {
//            Matrix Axy = m_matPhase.getMatrix( 0, IND_YP, 0, IND_YP ).minus( Matrix.identity(IND_YP+1, IND_YP+1) );
//            Matrix bxy = m_matPhase.getMatrix( 0, IND_YP, IND_HOM, IND_HOM ).times( -1 );
//            Matrix solutionxy = Axy.solve(bxy);
//            //System.out.println("A.det()=0, sxy solved");
//            sol = new PhaseVector( solutionxy.get(IND_X, 0), solutionxy.get(IND_XP, 0), solutionxy.get(IND_Y, 0), solutionxy.get(IND_YP, 0), 0, 0 );//sako, check z, zp components!
//        } else {
//
//            Matrix solution = A.solve(b);
//            sol = new PhaseVector( solution.get(IND_X, 0), solution.get(IND_XP, 0), solution.get(IND_Y, 0), solution.get(IND_YP, 0), solution.get(IND_Z, 0), solution.get(IND_ZP, 0) );
//        }
//        return sol;
//    }
    
    
//    /** 
//     * Calculate the fixed point solution vector representing the dispersion at the location of this element.
//     * We find the fixed point for the four transverse phase space coordinates.
//     * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 4x4 submatrix less the identity
//     * matrix and <code>b</code> is the 6th column excluding the longitudinal row element.  The reason for this is that the
//     * fixed point is defined by the point for which the transfer map maps to the same point.  This is
//     * <code>M * v = v</code>.  
//     * 
//     * @param    gamma   I think it's the relativistic factor (CKA)
//     * 
//     * @return the dispersion vector
//     */
//    public double[] calculateDispersion(final double gamma) {
//        Matrix A = m_matPhase.getMatrix( 0, IND_YP, 0, IND_YP ).minus( Matrix.identity(IND_YP+1, IND_YP+1) );
//        Matrix b = m_matPhase.getMatrix( 0, IND_YP, IND_ZP, IND_ZP ).rightMultiply( -1./(gamma*gamma) );
//        
//        Matrix solution = A.solve(b);
//        
//        return new double[] { solution.get(IND_X, 0), solution.get(IND_XP, 0), solution.get(IND_Y, 0), solution.get(IND_YP, 0) };
//    }
//    
//    
    /*
     *  Matrix Operations
     */
    
    
//    /**
//     *  Matrix determinant function.
//     *
//     *  @return     det(this)
//     */
//    public double det()     { return this.getMatrix().det(); };
//    
//    /**
//     *  Nondestructive transpose of this matrix.
//     * 
//     *  @return     transposed matrix
//     */
//    public PhaseMatrix transpose()  {
//        return new PhaseMatrix( this.getMatrix().transpose() );
//    }
//    
//    /**
//     *  Nondestructive inverse of this matrix.
//     *
//     *  @return     the algebraic inverse of this matrix
//     */
//    public PhaseMatrix inverse()    {
//       return new PhaseMatrix( this.getMatrix().inverse() );
//    }
//
//    
    
//    /** 
//     *  Non-destructive scalar multiplication.  The homogeneous pivot
//     *  element on the diagonal is unchanged.
//     *
//     *  @param  s   scalar value to multiply this matrix
//     *
//     *  @return     new matrix equal to s*this
//     */
//    public PhaseMatrix times(double s) {
//        PhaseMatrix matProd = super.times(s);
//        matProd.setElem(IND.HOM, IND.HOM, 1.0);
//        
//        return matProd;
//    }
//    
//    /**
//     *  In-place scalar multiplication.  The homogeneous pivot 
//     *  element on the diagonal is not changed.
//     *
//     *  @param  s   scalar value to multiply this matrix (result replaces this)
//     */
//    public void timesEquals(double s) {
//        super.timesEquals(s);
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//    }
//    
//    /**
//     * Non-destructive Matrix-Vector multiplication.
//     *  
//     * @param vec   right-hand argument of the matrix-vector product 
//     *
//     *  @return     this*vec
//     */
//    public PhaseVector  times(PhaseVector vec)  {
//        PhaseVector     vecSum = new PhaseVector();
//        
//        for (IND i : IND.values() ) {
//            
//            double  dblVal = 0.0;
//            
//            for (IND j : IND.values() ) {
//                dblVal += this.getElem(i, j)*vec.getElem(i);
//            }
//            
//            vecSum.setElem(i, dblVal);
//        }
//        
//        Jama.Matrix     matRes;     // resultant vector
//        
//        matRes = this.getMatrix().times( vec.getMatrix() );
//     
//        return new PhaseVector( matRes );
//    }
//
//    /**
//     *  Matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     *
//     *  @return             this*matRight
//     */
//    public PhaseMatrix  times(PhaseMatrix matRight) {
//        return new PhaseMatrix( this.getMatrix().times( matRight.getMatrix() ) );
//    }
//    
//    /**
//     *  In-place matrix multiplication.  
//     *
//     *  @param  matRight    right operand of matrix multiplication operator
//     *
//     */
//    public void timesEquals(PhaseMatrix matRight) {
//        this.getMatrix().arrayTimesEquals( matRight.getMatrix() );
//    }
    

    
//    /**
//     *  Function for transpose conjugation of <em>this</em> 
//     *  matrix <b>&sigma;</b> by the argument matrix <b>&Phi;</b>
//     *  (e.i, <var>matPhi</var>).  
//     *  This method is nondestructive, return a new matrix.
//     *
//     *  @param  matPhi      conjugating matrix <b>&Phi;</b>
//     *                      (typically a transfer matrix)
//     *
//     *  @return             <b>&Phi; &middot; &sigma; &middot; &Phi;</b><sup><i>T</i></sup>
//     */
//    public PhaseMatrix conjugateTrans(PhaseMatrix matPhi) {
//        PhaseMatrix matResult;      // resultant matrix
//        
//        matResult = this.times(matPhi.transpose());
//        matResult = matPhi.times(matResult);
//        
//        return matResult;
//    };
//    
//    /**
//     *  Function for inverse conjugation of this matrix by the argument matrix.  
//     *  This method is nondestructive, return a new matrix.
//     *
//     *  @param  matPhi      conjugating matrix (typically a transfer matrix)
//     *
//     *  @return             matPhi*this*matPhi^-1
//     */
//    public PhaseMatrix conjugateInv(PhaseMatrix matPhi) {
//        PhaseMatrix matResult;      // resultant matrix
//        
//        matResult = this.times(matPhi.inverse());
//        matResult = matPhi.times(matResult);
//        
//        return matResult;
//    };
//    
//    
    
    /*
     *  Topological Operations
     */
    
//    /**
//     * Return the maximum element value of this matrix
//     * 
//     * NOTE:
//     * This operation does not include the effect of the 
//     * homogeneous element at index (HOM,HOM)
//     * 
//     * @return  maximum absolute value
//     */
//    public double   max()   {
//        double      val = 0.0;
//        double      max = Math.abs(getElem(0,0));
//        
//        this.setElem(IND.HOM, IND.HOM, 0.0);
//        for (IND i : IND.values())
//            for (IND j : IND.values()) {
//                val = Math.abs( getElem(i,j) );
//                if (val > max)
//                    max = val;
//            }
//
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//        return max;
//    }
//
//    /**
//     * <p>
//     * Return the <i>l</i><sub>1</sub> norm of this matrix; specifically, the maximum
//     * column sum.
//     * </p>
//     * <p>
//     * <h4>NOTE:</h4>
//     * &middot; This operation does not include the effect of the 
//     * homogeneous element at index (HOM,HOM)
//     * </p>
//     * 
//     *  @return     ||<b>M</b>||<sub>1</sub> &equiv; 
//     *              sup<sub><i>j</i></sub> &Sigma;<sub><i>i</i></sub> |<i>m<sub>ij</sub></i>|
//     */
//    public double   norm1()     { 
//        this.setElem(IND.HOM, IND.HOM, 0.0);
//        double  dblNorm = this.getMatrix().norm1();
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//        
//        return dblNorm; 
//    };
//    
//    /**
//     * <p>
//     * Return the <i>l</i>-2 norm of this matrix, which is the maximum
//     * singular value.
//     * </p>
//     * <p>
//     * NOTE:
//     * &middot; This operation does not include the effect of the 
//     * homogeneous element at index (HOM,HOM)
//     * </p>
//     * 
//     *  @return     max &sigma;<sub><i>ij</i></sub> where <b>M</b> = <b>U*&Sigma;*V</b>, <b>U,V</b> orthogonal
//     */
//    public double   norm2()     { 
//        this.setElem(IND.HOM, IND.HOM, 0.0);
//        double  dblNorm = this.getMatrix().norm2();
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//        
//        return dblNorm; 
//    };
//    
//    /**
//     * <p>
//     * Return the Lebesgue-infinity norm of this matrix; specifically, the maximum row sum.
//     * </p>
//     * <p>
//     * <h4>NOTE:</h4>
//     * &middot; This operation does not include the effect of the 
//     * homogeneous element at index (HOM,HOM)
//     * </p>
//     * 
//     *  @return     ||<b>M</b>||<sub>&infin;</sub> &equiv; sup<sub><i>i</i></sub> 
//     *              &Sigma;<sub><i>j</i></sub> |<i>m<sub>ij</sub></i>|
//     */
//    public double   normInf()   { 
//        this.setElem(IND.HOM, IND.HOM, 0.0);
//        double  dblNorm = this.getMatrix().normInf();
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//        
//        return dblNorm; 
//    };
//    
//    /**
//     * <p>
//     * Return the Frobenius norm of this matrix; specifically, the square-root of the sum of all 
//     * elements squared.
//     * </p>
//     * <p>
//     * <h4>NOTE:</h4>
//     * &middot; This operation does not include the effect of the 
//     * homogeneous element at index (HOM,HOM)
//     * </p>
//     * 
//     *  @return     ||<b>M</b>||<sub>F</sub> &equiv; [ &Sigma; <i>m<sub>ij</sub></i><sup>2</sup> ]<sup>1/2</sup>
//     */
//    public double   normF()     { 
//        this.setElem(IND.HOM, IND.HOM, 0.0);
//        double  dblNorm = this.getMatrix().normF();
//        this.setElem(IND.HOM, IND.HOM, 1.0);
//        
//        return dblNorm; 
//    };
//    
    
    
    /*
     *  Internal Support
     */
    
    
//    /**
//     *  Construct a PhaseMatrix from a suitable Jama.Matrix.  Note that the
//     *  argument should be a new object not owned by another object, because
//     *  the internal matrix representation is assigned to the target argument.
//     *
//     *  @param  matInit     a 7x7 Jama.Matrix object
//     */
//    public PhaseMatrix(Jama.Matrix matInit)  {
//        m_matPhase = matInit;
//    }
//    
//    /**
//     *  Return the internal matrix representation.
//     *
//     *  @return     the Jama matrix object
//     */
//    Jama.Matrix getMatrix()   { return m_matPhase; };
//    
    
    /*
     *  Testing and Debugging
     */
    
//    /**
//     *  Convert the contents of the matrix to a string representation.
//     *  The format is similar to that of Mathematica, e.g.
//     *
//     *      { {a b }{c d } }
//     *
//     *  @return     string representation of the matrix
//     */
//    @Override
//    public String   toString()  {
//        double num;
//        final int size = (INT_SIZE*INT_SIZE * 16) + (INT_SIZE*2) + 4; // double is 15 significant digits plus the spaces and brackets
//        StringBuffer strBuf = new StringBuffer(size);
//        
//        synchronized(strBuf) { // get lock once instead of once per append
//            strBuf.append("{ "); //$NON-NLS-1$
//            for (int i=0; i<INT_SIZE; i++) {
//                strBuf.append("{ "); //$NON-NLS-1$
//                for (int j=0; j<INT_SIZE; j++) {
////                  strBuf.append(this.getElem(i,j));
//                    // xal.tools.text.DoubleToString.append(strBuf, this.getElem(i,j)); // DoubleToString is much more efficient that jdk
//                    // jdg - trim fractions to avoid false  no-diagonal symmetry
//                    num = this.getElem(i,j);
//                    if(Math.abs(num) < 1000. && Math.abs(num) > 0.1)
//                        strBuf.append( FIXED_FORMAT.format( num ) );
//                    else
//                        strBuf.append( SCI_FORMAT.format( num ) );
//                    strBuf.append(" "); //$NON-NLS-1$
//                }
//                strBuf.append("}"); //$NON-NLS-1$
//            }
//            strBuf.append(" }"); //$NON-NLS-1$
//        }
//        
//        return strBuf.toString();
//        
//    }
//    
//    
//    /**
//     * Print this matrix to standard out.
//     */
//    public void print() {
//        //def   m_matPhase.print( 10, 5 );
//        m_matPhase.print( 10, 13 );
//    }
    
    
//    /**
//     *  Print out the contents of the PhaseMatrix in text format.
//     *
//     *  @param  os      output stream to receive text dump
//     */
//    public void print(PrintWriter os)   {
////        m_matPhase.print(os, DIM, DIM);
//        m_matPhase.print(os, new DecimalFormat("0.#####E0"), INT_SIZE); //$NON-NLS-1$
//    }
    
    
    
//    /*
//     * Debugging
//     */
//    
//    /**
//     *  Testing Driver
//     *  
//     * @param arrArgs   command line arguments 
//     */
//    public static void main(String arrArgs[])   {
//        PrintWriter     os = new PrintWriter(System.out);
//        
//        PhaseMatrix     mat1 = PhaseMatrix.identity();
//        mat1.print(os);
//        
//        PhaseMatrix     mat2 = new PhaseMatrix();
//        mat2.print(os);
//        
//        os.flush();
//    }

}
