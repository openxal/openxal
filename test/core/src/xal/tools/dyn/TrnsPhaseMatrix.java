/**
 * TransPhaseMatrix.java
 *
 * Author  : Christopher K. Allen
 * Since   : Sep 25, 2013
 */
package xal.tools.dyn;


import java.util.EnumSet;

import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.math.IIndex;
import xal.tools.math.SquareMatrix;
import xal.tools.math.r2.R2x2;
import xal.tools.math.r6.R6x6;

/**
 * <p>  
 * Class <code>TransPhaseMatrix</code> represents operations on the transverse 
 * phase coordinates.  This set of phase space matrices is essentially isomorphic to 
 * <b>R</b><sup>4&times;4</sup>, however, in order to represent translations
 * of phase coordinates by matrix multiplication, this class is embedded
 * in the linear operations <b>R</b><sup>5&times;5</sup> "projective space" 
 * with "homogeneous coordinates".
 * <p>  
 * </p>
 * The last coordinate of all homogeneous (transverse) phase vectors 
 * <b>v</b> &in; <b>R</b><sup>5</sup>
 * is 1.  Thus <b>v</b> = (<i>x,x',y,y'</i>,1).  Likewise, the (5,5) element of
 * any projective matrix <b>&Phi;</b> &in; <b>R</b><sup>5&times;5</sup> 
 * representing an operator in homogeneous coordinates must be 1.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Sep 25, 2013
 */
public class TrnsPhaseMatrix extends SquareMatrix<TrnsPhaseMatrix> {

    
    
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
		
		/** Index of the homogeneous coordinate */
		HOM(4);
		
		
        /*
         * Global Constants
         */

        /** the set of IND constants that only include phase space variables (not the homogeneous coordinate) */
        private final static EnumSet<IND> SET_PHASE = EnumSet.of(X, Xp, Y, Yp);
        
        
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
		 * @return	numerical value of this index
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
		final private int     val; 
		
		/**
		 * Creates a new <code>IND</code> enumeration constant
		 * initialized to the given index value.
		 * 
		 * @param index		numerical index value for this constant
		 *
		 * @author Christopher K. Allen
		 * @since  Sep 25, 2013
		 */
		private IND(int index) {
			this.val = index;
		}
	}
	
	
	/*
	 * Global Constants
	 */
	
    /** number of matrix dimensions  */
    public static final int    INT_SIZE = 5;
   
	
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create a new instance of a zero phase matrix.
     *
     *  @return         zero vector
     */
    public static TrnsPhaseMatrix  newZero()   {
        TrnsPhaseMatrix matZero = new TrnsPhaseMatrix();
        
        // Zero then set the homogeneous element to unity
        matZero.assignZero();
        matZero.setElem(IND.HOM.val, IND.HOM.val, 1.0);
        
        return matZero; 
    }
    
    /**
     *  Create an identity phase matrix
     *
     *  @return         7x7 real identity matrix
     */
    public static TrnsPhaseMatrix  newIdentity()   {
        TrnsPhaseMatrix matIden = new TrnsPhaseMatrix();
        
        // Zero then set the homogeneous element to unity
        matIden.assignIdentity();
        
        return matIden; 
    }
    
    /**
     * <p>
     * Create a phase matrix representing a linear translation
     * operator on homogeneous transverse phase space.  Multiplication by the 
     * returned <code>TransPhaseMatrix</code> object is equivalent to
     * translation by the given <code>PhaseVector</code> argument.
     * Specifically, if the argument <b>dv</b> has coordinates
     * <br>
     * <pre>
     * 
     *      <b>dv</b> = (dx,dx',dy,dy',1)<sup><i>T</i></sup>
     *      
     * then the returned matrix <b>T</b>(<b>dv</b>) has the form
     * 
     *           |1 0 0 0 dx |
     *           |0 1 0 0 dx'|
     *  <b>T</b>(<b>dv</b>)  = |0 0 1 0 dy |
     *           |0 0 0 1 dy'|
     *           |0 0 0 0  1 |
     *
     * Consequently, given a phase vector <b>v</b> of the form
     *
     *      <b>v</b> = |x |
     *          |x'|
     *          |y |
     *          |y'|
     *          |1 |
     *
     * Then operation on <b>v</b> by <b>T</b>(<b>dv</b>)  has the result
     * 
     *  <b>T</b>(<b>dv</b>) <b>v</b> = |x + dx |
     *            |x'+ dx'|
     *            |y + dy |
     *            |y'+ dy'|
     *            |  1    |
     * </pre>
     * </p>
     * 
     *  @param  vecTrans    translation vector
     *  
     *  @return             translation operator as a phase matrix     
     */
    public static TrnsPhaseMatrix  translation(TrnsPhaseVector vecTrans)   {
        TrnsPhaseMatrix     matTrans = TrnsPhaseMatrix.newIdentity();
        
        matTrans.setElem(IND.X,  IND.HOM, vecTrans.getElem(TrnsPhaseVector.IND.X));
        matTrans.setElem(IND.Xp, IND.HOM, vecTrans.getElem(IND.Xp));
        matTrans.setElem(IND.Y,  IND.HOM, vecTrans.getElem(IND.Y));
        matTrans.setElem(IND.Yp, IND.HOM, vecTrans.getElem(IND.Yp));
        
        return matTrans;
    }
      
    /**<p>
     * Compute the rotation matrix in phase space that is essentially the 
     * Cartesian product of the given rotation matrix in <i>SO</i>(2).  That is,
     * denote the given argument as <b>O</b>, then the returned matrix <b>M</b> 
     * is the embedding <b>M</b> = <b>O</b>&times;<b>O</b>&times;<b>I</b> 
     * into homogeneous transverse phase space which is isomorphic to 
     * <b>R</b><sup>4&times;4</sup>&times;{1}. Thus, M &in; <b>SO</b>(4) &sub;
     * <b>R</b><sup>4&times;4</sup>&times;{1} &sub; <b>R</b><sup>5&times;5</sup>.   
     * </p>
     * <p>
     * Viewing transverse phase-space as a 4D manifold built as the tangent 
     * bundle over transverse coordinate space <b>R</b><sup>2</sup>, then the fibers 
     * of 2D configuration space at a 
     * point (<i>x,y</i>) are represented by the Cartesian planes (<i>x',y'</i>).  The returned
     * phase matrix rotates these fibers in the same manner as their base point (x,y,z).
     * </p>  
     * 
     * This is a convenience method to build the above rotation matrix in <i>SO</i>(4) &sub; <b>R</b><sup>5&times;5</sup>.
     * 
     * @return  rotation matrix in <i>SO</i>(4) &sub; <i>S0</i>(5) which is 
     *          direct product of rotations in <i>S0</i>(2) 
     */
    public static TrnsPhaseMatrix  rotationProduct(R2x2 matSO2)  {

        // Populate the phase rotation matrix
        TrnsPhaseMatrix matSO4 = TrnsPhaseMatrix.newIdentity();
        
        int         m, n;       // indices into the SO(4) matrix
        double      val;        // matSO3 matrix element
        
        for (R2x2.IND iRow : R2x2.IND.values() ) {
            
            m = 2*iRow.val();

            for ( R2x2.IND iCol : R2x2.IND.values() ) {
         
                n = 2*iCol.val();
                
                val = matSO2.getElem(iRow, iCol);
                
                matSO4.setElem(m, n, val);      // configuration space
                matSO4.setElem(m+1, n+1, val);  // momentum space
            }
        }
        
        return matSO4;
    }
    
    /**
     * Extracts a copy of the transverse portion of the given <code>PhaseMatrix</code>
     * and returns it.  
     * 
     * @param matPhi    phase matrix to be copied
     * 
     * @return          the part of the phase matrix corresponding to the transverse 
     *                  phase coordinates <i>x, x', y, </i> and <i> y'</i>.
     *
     * @author Christopher K. Allen
     * @since  Oct 15, 2013
     */
    public static TrnsPhaseMatrix  extractTransverse( final PhaseMatrix matPhi ) {
        TrnsPhaseMatrix    matTrans = TrnsPhaseMatrix.newZero();
        
        for (IND i : IND.valuesPhase())
            for (IND j : IND.valuesPhase()) {
                double  dblVal = matPhi.getElem(i, j);
                
                matTrans.setElem(i, j, dblVal);
            }
        
        return matTrans;
    }
            

    /**
     *  Create a PhaseMatrix instance and initialize it
     *  according to a token string of element values.  
     *
     *  The token string argument is assumed to be one-dimensional and packed by
     *  column (ala FORTRAN).
     *
     *  @param  strTokens   token vector of 5x5=25 numeric values
     *
     *  @exception  IllegalArgumentException    wrong number of token strings
     *  @exception  NumberFormatException       bad number format, unparseable
     */
    public static TrnsPhaseMatrix parse(String strTokens)    
        throws IllegalArgumentException, NumberFormatException
    {
        return new TrnsPhaseMatrix(strTokens);
    }

    
    
    
//    /*
//     *  Local Attributes
//     */
//    
//    /** internal matrix storage */
//    private Jama.Matrix     m_matPhase;
//    
//    
    
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
    public TrnsPhaseMatrix clone() {
        return new TrnsPhaseMatrix(this);
    }

    
    /*
     * Initialization
     */
    
    
    /** 
     *  Creates a new instance of PhaseMatrix initialized to zero.
     */
    public TrnsPhaseMatrix() {
        super(INT_SIZE);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }
    
    /**
     *  Copy Constructor - create a deep copy of the target phase matrix.
     *
     *  @param  matInit     initial value
     */
    public TrnsPhaseMatrix(TrnsPhaseMatrix matInit) {
       super(matInit);
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
    public TrnsPhaseMatrix(String strValues)    
        throws IllegalArgumentException, NumberFormatException
    {
        super(INT_SIZE, strValues);
        this.setElem(IND.HOM, IND.HOM, 1.0);
    }

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
	protected TrnsPhaseMatrix newInstance() {
		return new TrnsPhaseMatrix();
	}
    


    /*
     *  Algebraic Operations
     */
    

    /*
     *  Assignment
     */
    

    /*
     *  Internal Support
     */
    
    
}
