/*
 * PhaseMap.java
 *
 * Created on March 26, 2003, 7:51 PM
 */

package xal.tools.beam;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;


/**
 * Represents a generalized map between homogeneous phase space coordinates.
 * Note that it does not necessarily need to be symplectic.
 *
 * @author  Christopher K. Allen
 */
public class PhaseMap implements IArchive {
    
    
    
    /*
     * Global Attributes
     */
     

    /** label for zero-order map component (offset) */
    public static final String     LABEL_ZERO  = "deg0";
    
    /** label for first-order map component */
    public static final String     LABEL_ONE   = "deg1";
    
    /** label for second-order map component */
    public static final String     LABEL_TWO   = "deg2";
    
    /** label for third-order map component */
    public static final String     LABEL_THREE = "deg3";
    
    /** attribute marker for data */
    public static final String     ATTR_DATA   = "data";
     
     
    
    /*
     *  Global Methods
     */
    
    /**
     *  Create an identity phase map
     *
     *  @return         identity map for phase space
     */
    public static PhaseMap  identity()   {
        PhaseMap    mapId = new PhaseMap();
        
        mapId.setZeroOrder( PhaseVector.zero() );
        mapId.setFirstOrder( PhaseMatrix.identity() );
        
        return mapId;
    }
    
    

    /*
     *  Local Attributes
     */
    
    /** zero order part of map */
    private PhaseVector m_vecOffset;
    
    /** linear part of map */
    private PhaseMatrix m_matLinear;
    
    /** second order part of map */
    @SuppressWarnings("unused")
    private double[][][] m_arrSecond;
    
    /** third order part of map */
    @SuppressWarnings("unused")
    private double[][][][] m_arrThird;
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of PhaseMap - produces the identity map
     */
    public PhaseMap() {
        m_vecOffset = PhaseVector.zero();
        m_matLinear = PhaseMatrix.identity();
    }
    
	
    /**
     *  Create a new instance of PhaseMap which behaves as simple translation in phase
     *  space given by the specified displacement vector.
     *
     *  @param  vecDispl    displacement of the phase map
     */
     public PhaseMap(PhaseVector vecDispl) {
         m_vecOffset = vecDispl;
         m_matLinear = PhaseMatrix.identity();
     };
    
	
    /**
     *  Create a new instance of PhaseMap which behaves as a linear transform given 
     *  by the specified matrix.
     *
     *  @param  matTrans   linear portion of the phase map
     */
     public PhaseMap(PhaseMatrix matTrans) {
         m_vecOffset = PhaseVector.zero();
         m_matLinear = matTrans;
     };
    
    public PhaseMap( PhaseMap phaseMapCopy ) {
        this.m_vecOffset = new PhaseVector( phaseMapCopy.m_vecOffset );
        this.m_matLinear = new PhaseMatrix( phaseMapCopy.m_matLinear );
    }
	 
	 
	 /**
	  * Make a deep copy of this phase map.
	  * 
	  * @return  a deep copy of this phase map
	  */
	 public PhaseMap copy() {
		 return new PhaseMap( this );
	 }
	 
	 
	 /**
	  * Set this map to copy the specified map.
	  *
	  * @param map the map from which to copy this map's settings.
	  */
	 public void setFrom( final PhaseMap map ) {
		 setZeroOrder( new PhaseVector( map.getZeroOrder() ) );
		 setFirstOrder( new PhaseMatrix( map.getFirstOrder() ) );
	 }
    
     
    /**
     *  Set the zero-order portion of the map
     *
     *  @param  vecOff  the output of the zero phase vector
     */
    public void setZeroOrder(PhaseVector vecOff) {
        m_vecOffset = vecOff;
    }
    
	 
    /**
     *  Set the linear part of the map
     *
     *  @param  matLin  linear portion of phase map
     */
    public void setFirstOrder(PhaseMatrix matLin) {
        m_matLinear = matLin;
    }

    

    /*
     * IArchive Interface
     */    
    
    /**
     * Save the value of this <code>PhaseMatrix</code> to a data sink 
     * represented by the <code>DataAdaptor</code> interface.
     * 
     * @param daptArchive   interface to data sink 
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    public void save(DataAdaptor daptArchive) {
        DataAdaptor daptZero = daptArchive.createChild(PhaseMap.LABEL_ZERO);
        this.getZeroOrder().save(daptZero);
        
        DataAdaptor daptOne = daptArchive.createChild(PhaseMap.LABEL_ONE);  
        this.getFirstOrder().save(daptOne);      
    }

    /**
     * Restore the value of the this <code>PhaseMatrix</code> from the
     * contents of a data archive.
     * 
     * @param daptArchive   interface to data source
     * 
     * @throws DataFormatException      malformed data
     * @throws IllegalArgumentException wrong number of string tokens
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    public void load(DataAdaptor daptArchive) 
        throws DataFormatException, IllegalArgumentException 
    {
        DataAdaptor daptZero = daptArchive.childAdaptor(PhaseMap.LABEL_ZERO);
        if (daptZero != null)
            this.getZeroOrder().load(daptZero);
            
        DataAdaptor daptOne = daptArchive.childAdaptor(PhaseMap.LABEL_ONE);
        if (daptOne != null)
            this.getFirstOrder().load(daptOne);
    }
    


    /*
     *  Property Query
     */
    
    /**
     *  Get the zero offset vector of the map.
     *
     *  @return         the phase coordinates of the zero map
     */
    public PhaseVector getZeroOrder() {
        return m_vecOffset;
    }
    
    /**
     *  Get the linear portion of the PhaseMap
     *
     *  @return         linear part of map
     */
    public PhaseMatrix getFirstOrder() {
        return m_matLinear;
    }
    
    
    /*
     *  Map Operations
     */
    
	 
	/**
	 * Generate an inverse map of this map.  By inverse map we mean that If we apply a map to an 
	 * initial state and then apply the inverse map to that state then we get the original state back.
	 * If the map is defined by the linear operator <code>A</code> and a translation <code>b</code> then
	 * the inverse map has linear operator <code>A<sup>-1</sup></code> and translation
	 * <code>-A<sup>-1</sup> b</code>.
	 *
	 * @return a new map which is the inverse of this map.
	 */
	 public PhaseMap inverse() {
		 final PhaseMap map = new PhaseMap();
		 
		 final PhaseMatrix firstOrder = getFirstOrder().inverse();
		 final PhaseVector zeroOrder = firstOrder.times( getZeroOrder().times(-1.0) );
		 
		 map.setZeroOrder( zeroOrder );
		 map.setFirstOrder( firstOrder );
		 
		 return map;
	 }
	 
	 
    /**
     *  Non-destructive map composition - binary composition of two PhaseMaps
     *
     *  @param  mapRight    right argument to map composition
     *
     *  @return             composition this*mapRight
     */
    public PhaseMap compose(PhaseMap mapRight)  {
        PhaseMap    mapNew = new PhaseMap();
        
        mapNew.setZeroOrder( this.getZeroOrder().plus( mapRight.getZeroOrder() ) );
        mapNew.setFirstOrder( this.getFirstOrder().times( mapRight.getFirstOrder() ) );
        
        return mapNew;
    }
    
	 
    /**
     *  In-place map composition - binary composition of two PhaseMaps
     *
     *  @param  mapRight    right argument to map composition
     */
    public void composeEquals(PhaseMap mapRight)  {
        this.getZeroOrder().plusEquals( mapRight.getZeroOrder() );
        this.getFirstOrder().timesEquals( mapRight.getFirstOrder() );
    }
    
    /**
     *  Apply the full transform of map to phase coordinates.
     *
     *  @param  vecIn   phase vector input to map
     *
     *  @return         output phase coordinates
     */
    public PhaseVector apply(PhaseVector vecIn) {
        PhaseVector     vecOut;     // returned value
        
        vecOut = this.getFirstOrder().times( vecIn);
        vecOut = this.getZeroOrder().plus( vecOut );
        
        return vecOut;
    }
	 
	 
	 /** 
	 * Calculate the fixed point solution vector representing the closed orbit at the location of this element.
	 * We find the fixed point for the six phase space coordinates.
	 * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 6x6 submatrix less the identity
	 * matrix and <code>b</code> is the 7th column excluding the 7th row element.  The reason for this is that the
	 * fixed point is defined by the point for which the transfer map maps to the same point.  This is
	 * <code>M * v = v</code>.  
	 * 
	 * @return the fixed point solution
	 */
	 public PhaseVector calculateFixedPoint() {
		 return getFirstOrder().calculateFixedPoint();
	 }	 
	 
	 
	 /** 
	 * Calculate the fixed point solution vector representing the dispersion at the location of this element.
	 * We find the fixed point for the four transverse phase space coordinates.
	 * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 4x4 submatrix less the identity
	 * matrix and <code>b</code> is the 6th column excluding the longitudinal row element.  The reason for this is that the
	 * fixed point is defined by the point for which the transfer map maps to the same point.  This is
	 * <code>M * v = v</code>.  
	 * 
	 * @return the dispersion vector
	 */
	 public double[] calculateDispersion(final double gamma) {
		 return getFirstOrder().calculateDispersion(gamma);
	 }
}
