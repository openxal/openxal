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
 * <p>
 * Represents a generalized map between homogeneous phase space coordinates.
 * Note that it does not necessarily need to be symplectic.
 * </p>
 * <p>
 * Currently the action of the map, denoted <b>&phi;</b>, simply consists of
 * a linear portion plus an offset.  Second order effects are to be
 * designed and implemented in the future.
 * </p>
 * <p>
 * The action of the linear map 
 * <b>&phi;</b><sub>0</sub> : <b>R</b><sup>6</sup> &times; {1} &rarr; <b>R</b><sup>6</sup> &times; {1} 
 * on a phase vector <b>z</b> &in; <b>R</b><sup>6</sup> &times; {1} is given by
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>&phi;</b><sub>0</sub>(<b>z</b>) = <b>&Phi;</b><sub>0</sub> &sdot; 
 *                                       (<b>z</b> - <b>z</b><sub>0</sub>) 
 *                                       + <b>&Delta;</b><sub>0</sub> ,
 * <br>
 * <br>
 * where <b>&Phi;</b><sub>0</sub> &in; <b>R</b><sup>7&times;7</sup> is the linear part of the map
 * (a matrix representation), <b>z</b><sub>0</sub> is the evaluation point or center, and
 * <b>&Delta;</b><sub>0</sub> is the value or offset of 
 * the mapping.  Note that <b>&phi;</b><sub>0</sub> : <b>z</b><sub>0</sub> &rarr; <b>&phi;</b><sub>0</sub>, 
 * that is, <b>z</b><sub>0</sub> is the location of the linear expansion of the
 * map <b>&phi;</b><sub>0</sub> and <b>&Delta;</b><sub>0</sub> is the value there.
 * </p>
 * <p>
 * The above representation is in direct correlation with the Taylor expansion of 
 * a continuous map <b>F</b><sub>0</sub> &in; <i>C</i>(<b>R</b><sup>6</sup>&times;{1} 
 * &rarr; <b>R</b><sup>6</sup>&times;{1}).
 * Say we wish to expand <b>F</b><sub>0</sub> about the point <b>z</b><sub>0</sub> &in; <b>R</b><sup>6</sup>.
 * The Taylor expansion of <b>F</b><sub>0</sub> at the point <b>z</b> &in; <b>R</b><sup>6</sup> is
 * given by
 * <br>
 * <br>
 * &nbsp; &nbsp; <b>F</b><sub>0</sub>(<b>z</b>) = <b>F</b>(<b>z</b><sub>0</sub>) 
 *                                  + <b>F</b><sub>0</sub>'(<b>z</b><sub>0</sub>) &sdot; (<b>z</b> - <b>z</b><sub>0</sub>) 
 *                                  + <i>O</i>(||<b>z</b> - <b>z</b><sub>0</sub>||<sup>2</sup>) ,
 * <br>
 * <br>
 * where <b>F</b><sub>0</sub>'(<b>z</b><sub>0</sub>) is the matrix of first partial derivatives of <b>F</b> evaluated at the
 * point <b>z</b><sub>0</sub> &in; <b>R</b><sup>6</sup>&times;{1}.  By identifying <b>z</b><sub>0</sub>,
 * <b>&Delta;</b><sub>0</sub>,  
 * and <b>&Phi;</b><sub>0</sub> with <b>z</b><sub>0</sub>, <b>F</b><sub>0</sub>(<b>z</b><sub>0</sub>), 
 * and <b>F</b><sub>0</sub>'(<b>z</b><sub>0</sub>), respectively, we
 * arrive at our Taylor map representation for this class as seen by definition of
 * <b>&phi;</b><sub>0</sub>.
 * </p>
 * <p>
 * Hopefully, this interface should be general enough so that when a consensus is reached as to 
 * represent higher-order effects of the transfer map, the implementation can be supported here. 
 * </p>
 *
 * @author  Christopher K. Allen
 * @since   Mar 26, 2003
 * @version Nov 1, 2013
 */
public class PhaseMap implements IArchive {
    
    
    
    /*
     * Global Attributes
     */
     

    /** label for the domain center component of the map */
    public static final String      LBL_CNTR = "cntr";
    
    /** label for zero-order map component (offset) */
    public static final String     LBL_DSPL  = "dspl";
    
    /** label for first-order map component */
    public static final String     LBL_LIN   = "lin";
    
    /** label for second-order map component */
    public static final String     LBL_2ND   = "deg2";
    
    /** label for third-order map component */
    public static final String     LBL_THREE = "deg3";
    
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
        
        mapId.setRangeDisplace( PhaseVector.newZero() );
        mapId.setLinearPart( PhaseMatrix.identity() );
        
        return mapId;
    }
    
    

    /*
     *  Local Attributes
     */
    
    /** Domain center (zero-order part of map) */
    private PhaseVector     vecDomCntr;
    
    /** Range offset (zero order part of map) */
    private PhaseVector     vecRngDspl;
    
    /** linear part of map */
    private PhaseMatrix     matLinear;
    
    /** second order part of map */
    @SuppressWarnings("unused")
    private double[][][]    arrSecond;
    
    /** third order part of map */
    @SuppressWarnings("unused")
    private double[][][][]  arrThird;
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of <code>PhaseMap</code>.  Creates
     *  the identity map centered at <b>0</b> &in; <b>R</b><sup>6</sup>&times;{1}.
     *  
     */
    public PhaseMap() {
        this.vecDomCntr = PhaseVector.newZero();
        this.vecRngDspl = PhaseVector.newZero();
        this.matLinear  = PhaseMatrix.identity();
    }
    

    /**
     *  Create a new instance of <code>PhaseMap</code> which behaves as simple 
     *  translation in phase
     *  space given by the specified displacement vector.
     *
     *  @param  vecDispl    displacement in the range of the phase map, or <b>&Delta;</b><sub>0</sub> 
     */
    public PhaseMap(PhaseVector vecDispl) {
        this.vecRngDspl = vecDispl;
        this.matLinear  = PhaseMatrix.identity();
    };


    /**
     *  Create a new instance of PhaseMap which behaves as a linear transform given 
     *  by the specified matrix.
     *
     *  @param  matTrans   linear portion of the phase map, specifically <b>&Phi;</b><sub>0</sub>
     */
    public PhaseMap(PhaseMatrix matTrans) {
        this.vecDomCntr = PhaseVector.newZero();
        this.vecRngDspl = PhaseVector.newZero();
        this.matLinear  = matTrans;
    };

    /**
      * Initializing constructor for <code>PhaseMap</code> class. Sets the 
      * map center <b>z</b><sub>0</sub>, the map offset <b>&Delta;</b><sub>0</sub>,
      * and the map linear part <b>&Phi;</b><sub>0</sub> to the given quantities, 
      * respectively.
      *
      * @param vecCntr      point in map domain unaffected by map action
      * @param vecDspl      the image of the map center
      * @param matLin       linear part of the map around the map center point 
      *
      * @author Christopher K. Allen
      * @since  Nov 4, 2013
      */
     public PhaseMap(PhaseVector vecCntr, PhaseVector vecDspl, PhaseMatrix matLin) {
         this.vecDomCntr = vecCntr;
         this.vecRngDspl = vecDspl;
         this.matLinear  = matLin;
     }
    
    /**
     * Copy constructor for <code>PhaseMap</code>.  Creates a new,
     * deep copy of the given <code>PhaseMap</code> object.
     *
     * @param mapParent     parent map to clone
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2013
     */
    public PhaseMap( PhaseMap mapParent ) {
        this.vecDomCntr = new PhaseVector( mapParent.vecDomCntr );
        this.vecRngDspl = new PhaseVector( mapParent.vecRngDspl );
        this.matLinear  = new PhaseMatrix( mapParent.matLinear );
    }
	 
	 
    /*
     * Operations
     */
    
	 /**
	  * Make a deep copy of this phase map.
	  * 
	  * @return  a cloned copy of this phase map
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
	     this.setDomainCenter( map.getDomainCenter() );
		 this.setRangeDisplace( new PhaseVector( map.getRangeDisplace() ) );
		 this.setLinearPart( new PhaseMatrix( map.getFirstOrder() ) );
	 }
    
     
	 /**
	  * Set the map's domain center, or <b>z</b><sub>0</sub> in the
	  * notation of the class documentation.  The given vector is copied
	  * rather than referenced and, thus, unchanged.
	  * 
	  * @param vecDomCntr  the point <b>z</b><sub>0</sub> in phase space about which the
	  *                    map is expanded 
	  *
	  * @author Christopher K. Allen
	  * @since  Nov 4, 2013
	  */
	 public void   setDomainCenter(final PhaseVector vecDomCntr) {
	     this.vecDomCntr = new PhaseVector( vecDomCntr );
	 }
	 
    /**
     * Set the map's offset in the range, this is the quantity
     * <b>&Delta;</b><sub>0</sub> in the class documentation.  
     * The given vector is copied
     * rather than referenced and, thus, unchanged.
     *
     * @param  vecRngDspl  the displacement 
     *                      <b>&Delta;</b><sub>0</sub> &in; <i>Rng</i>{<b>&phi;</b><sub>0</sub>} of the map 
     */
    public void setRangeDisplace(final PhaseVector vecRngDspl) {
        this.vecRngDspl = new PhaseVector( vecRngDspl );
    }
    
	 
    /**
     *  Set the linear part of the map, this is the matrix
     *  <b>&Phi;</b><sub>0</sub> in the notation of the class documentation.
     * The given matrix is copied
     * rather than referenced and, thus, left unchanged.
     *
     *  @param  matPhi  linear portion <b>&Phi;</b><sub>0</sub> of phase map <b>&phi;</b><sub>0</sub>
     */
    public void setLinearPart(final PhaseMatrix matPhi) {
        this.matLinear = new PhaseMatrix( matPhi );
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
        DataAdaptor daptCntr = daptArchive.createChild(LBL_CNTR);
        this.getDomainCenter().save(daptCntr);
        
        DataAdaptor daptDspl = daptArchive.createChild(PhaseMap.LBL_DSPL);
        this.getRangeDisplace().save(daptDspl);
        
        DataAdaptor daptLin = daptArchive.createChild(PhaseMap.LBL_LIN);  
        this.getFirstOrder().save(daptLin);      
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
        DataAdaptor daptCntr = daptArchive.childAdaptor(LBL_CNTR);
        if (daptCntr != null)
            this.getDomainCenter().load(daptCntr);
        
        DataAdaptor daptDspl = daptArchive.childAdaptor(PhaseMap.LBL_DSPL);
        if (daptDspl != null)
            this.getRangeDisplace().load(daptDspl);
            
        DataAdaptor daptLin = daptArchive.childAdaptor(PhaseMap.LBL_LIN);
        if (daptLin != null)
            this.getFirstOrder().load(daptLin);
    }
    


    /*
     *  Property Query
     */
    
    /**
     * Returns the map center in its domain.  This is the quantity
     * <b>z</b><sub>0</sub> in the class documentation.
     * 
     * @return  returns the point in the domain where the map has no action (only displacement)
     *
     * @author Christopher K. Allen
     * @since  Nov 4, 2013
     */
    public PhaseVector  getDomainCenter() {
        return this.vecDomCntr;
    }
    
    /**
     *  Get the map offset in the range, the vector 
     *  <b>&Delta;</b><sub>0</sub> in the class documentation.  
     *
     *  @return         the point <b>&Delta;</b><sub>0</sub> &in; <i>Rng</i>{<b>&phi;</b><sub>0</sub>} that is
     *                  the image of the center <b>z</b><sub>0</sub>.
     */
    public PhaseVector getRangeDisplace() {
        return this.vecRngDspl;
    }
    
    /**
     *  Get the linear portion of the map, this is the quantity
     *  <b>&Phi;</b><sub>0</sub> in the class documentation.
     *
     *  @return         linear part of map <b>&phi;</b><sub>0</sub> given by 
     *                  <b>&Phi;</b><sub>0</sub> &equiv; &part;<b>&phi;</b><sub>0</sub>/&part;<b>z</b>
     *                  evaluated at <b>z</b><sub>0</sub> in <i>Dom</i>{<b>&phi;<b/><sub>0</sub>}
     */
    public PhaseMatrix getFirstOrder() {
        return this.matLinear;
    }
    
    
    /*
     *  Map Operations
     */
	 
	/**
	 * <p>
	 * Compute and return the inverse map <b>&phi;</b><sup>-1</sup> of this map <b>&phi;</b>.  
	 * By inverse map we mean that the composition <b>&phi;</b><sup>-1</sup> &sdot; <b>&phi;</b>
	 * is the identity map <b>id</b> : <b>z</b> &rarr; <b>z</b> on <b>R</b><sup>6</sup>&times;{1}.
	 * </p>
	 * <p>
	 * Consider linear maps <b>&phi;</b> defined according to the class documentation
	 * <br>
	 * <br>
	 * &nbsp; &nbsp; <b>&phi;</b>(<b>z</b>) = <b>&Phi;</b> &sdot; 
	 *                                       (<b>z</b> - <b>z</b><sub>0</sub>) 
	 *                                       + <b>&Delta;</b><sub>0</sub> ,
	 * <br>
	 * <br>
	 * where <b>&Phi;</b> &in; <b>R</b><sup>7&times;7</sup> is the linear part of the map
	 * (a matrix representation), <b>z</b><sub>0</sub> is the evaluation point or center, and
	 * <b>&Delta;</b><sub>0</sub> is the value or offset of the mapping.  Then the inverse
	 * map <b>&phi;</b><sup>-1</sup> : <b>R</b><sup>6</sup>&times;{1} &rarr; <b>R</b><sup>6</sup>&times;{1}
	 * is given by
	 * <br>
	 * <br>
	 * &nbsp; &nbsp; <b>&phi;</b><sup>-1</sup>(<b>y</b>) = <b>&Phi;</b><sub>0</sub><sup>-1</sup>
	 *                                                   &sdot; (<b>y</b> - <b>&Delta;</b><sub>0</sub>)
	 *                                                   + <b>z</b><sub>0</sub> ,
	 * <br>
	 * <br>                                                  
	 * which can be derived by solving the equation <b>&phi;</b>(<b>z</b>) = <b>y</b> for
	 * <b>y</b>.
	 * </p>  	  
	 *
	 * @return a new map which is the inverse of this map.
	 */
	 public PhaseMap inverse() {
	     
	     // Compute the components of the inverse map
	     PhaseVector   vecInvCntr = new PhaseVector( this.getRangeDisplace() );
	     PhaseVector   vecInvDspl = new PhaseVector( this.getDomainCenter() );
	     PhaseMatrix   matInvLin  = this.getFirstOrder().inverse();
	     
	     // Create the inverse map and return it
	     PhaseMap      mapInv = new PhaseMap(vecInvCntr, vecInvDspl, matInvLin);
	     
	     return mapInv;
	 }
	 
    /**
     *  <p>
     *  Non-destructive map composition - binary composition of two PhaseMaps.  Let
     *  <b>&phi;</b><sub>1</sub> denote the given map and <b>&phi;</b><sub>2</sub> denote 
     *  <code>this</code> map; that is, <b>&phi;</b><sub>2</sub> &equiv; <b>&phi;</b><sub><i>this</i></sub>. 
     *  Referring to the class documentation 
     *  <code>{@link PhaseMap}</code>, the composition 
     *  <b>&phi;</b> &equiv; <b>&phi;</b><sub>2</sub> &sdot; <b>&phi;</b><sub>1</sub> is determined
     *  by its action on an element <b>z</b> &in; <b>R</b><sup>6</sup>&times;{1} 
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&phi;</b>(<b>z</b>) = <b>&phi;</b><sub>2</sub> &sdot; <b>&phi;</b><sub>1</sub>(<b>z</b>) ,  
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <b>&phi;</b><sub>2</sub> 
     *                                         [ <b>&Phi;</b><sub>1</sub>&sdot;(<b>z</b> - <b>z</b><sub>1</sub>) 
     *                                         + <b>&Delta;</b><sub>1</sub> - <b>z</b><sub>2</sub>] ,
     *  
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; &nbsp; = <b>&Phi;</b><sub>2</sub>&sdot;<b>&Phi;</b><sub>1</sub>
     *                               (<b>z</b> - <b>z</b><sub>1</sub>)
     *                               + <b>&Phi;</b><sub>2</sub> &sdot; (<b>&Delta;</b><sub>1</sub> - <b>z</b><sub>2</sub>)
     *                               + <b>&Delta;</b><sub>2</sub>                                          
     *  <br>
     *  <br>
     *  where <b>&Phi;</b><sub>1</sub> and <b>&Phi;</b><sub>2</sub> are the linear parts (matrices)
     *  of maps <b>&phi;</b><sub>1</sub> and <b>&phi;</b><sub>2</sub>, respectively, 
     *  <b>&Delta;</b><sub>1</sub> and <b>&Delta;</b><sub>2</sub> are the (range) offsets 
     *  of maps <b>&phi;</b><sub>1</sub> and <b>&phi;</b><sub>2</sub>, respectively, and
     *  <b>z</b><sub>1</sub> and <b>z</b><sub>2</sub> are the (domain) centers of maps
     *  <b>&phi;</b><sub>1</sub> and <b>&phi;</b><sub>2</sub>, respectively.
     *  </p>
     *  <p>
     *  Thus, the center <b>z</b><sub>3</sub>, displacement <b>&Delta;</b><sub>3</sub>, and 
     *  linear part <b>&Phi;</b><sub>3</sub> of the new composite map 
     *  <b>&phi;</b><sub>3</sub> &equiv; <b>&phi;</b><sub>2</sub> &sdot; <b>&phi;</b><sub>1</sub>
     *  are respectively given by 
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>z</b><sub>3</sub> = <b>z</b><sub>1</sub> ,
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&Delta;</b><sub>3</sub> = <b>&Phi;</b><sub>2</sub> &sdot; 
     *                                          (<b>&Delta;</b><sub>1</sub> - <b>z</b><sub>2</sub>) 
     *                                          + <b>&Delta;</b><sub>2</sub> ,
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&Phi;</b><sub>3</sub> = <b>&Phi;</b><sub>2</sub> &sdot; <b>&Phi;</b><sub>1</sub> ,
     *  <br>
     *  <br>
     *  The above equations are the formulas for the returned map.
     *  </p>
     *
     *  @param  mapRight    right argument <b>&phi;</b><sub>1</sub> to map composition
     *
     *  @return             composition <b>&phi;</b> &equiv; <b>&phi;</b><sub>2</sub> &sdot; <b>&phi;</b><sub>1</sub>
     */
    public PhaseMap compose(PhaseMap mapRight)  {

        // Compute the new center (trivial - same as last)
        PhaseVector vecCntr = mapRight.getDomainCenter();
        
        // Compute the new displacement vector
        PhaseVector vecDspl = this.compComposedDisplacement(this, mapRight);
//        
//        PhaseVector vecTrm1 = mapRight.getRangeDisplace().minus( this.getDomainCenter() );
//        PhaseVector vecTrm2 = this.getFirstOrder().times( vecTrm1 );
//        PhaseVector vecDspl = vecTrm2.plus( this.getRangeDisplace() );
        
        // Compute the new first-order part
        PhaseMatrix matLin = this.getFirstOrder().times( mapRight.getFirstOrder() );

        // Create the new map and return it
        PhaseMap    mapCmp = new PhaseMap(vecCntr, vecDspl, matLin);
        
        return mapCmp;
    }
    
	 
    /**
     *  In-place map composition, binary composition of two <code>PhaseMap</code>
     *  objects where this map 
     *  This map <b>&phi;</b><sub>2</sub> is replaced by
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&phi;</b><sub>2</sub> &larr; <b>&phi;</b><sub>2</sub> &sdot; <b>&phi;</b><sub>1</sub>
     *  <br>
     *  <br>
     *  where <b>&phi;</b><sub>1</sub> the argument
     *  map.  See method <code>{@link #compose(PhaseMap)}</code> for details of the
     *  computation.
     *
     *  @param  mapRight    right argument <b>&phi;</b><sub>0</sub> of map composition
     */
    public void composeEquals(PhaseMap mapRight)  {
        PhaseVector     vecCntr = mapRight.getDomainCenter();
        PhaseVector     vecDspl = this.compComposedDisplacement(this,  mapRight);
        PhaseMatrix     matCmp  = this.getFirstOrder().times( mapRight.getFirstOrder() );
        
        this.setDomainCenter(vecCntr);
        this.setRangeDisplace(vecDspl);
        this.setLinearPart(matCmp);
    }
    
    /**
     *  Apply this map to the given phase vector.  Denoting the given 
     *  phase vector as <b>z</b>, currently for the linear map <b>&phi;</b>
     *  the result is 
     *  <br>
     *  <br>
     *  &nbsp; &nbsp; <b>&phi;</b>(<b>z</b>) = <b>&Phi;</b> &sdot; (<b>z</b> - <b>z</b><sub>0</sub>)
     *                                       + <b>&Delta;</b> ,
     *  <br>
     *  <br>
     *  where <b>z</b><sub>0</sub> is the center of <b>&phi;</b>, <b>&Delta;</b> is the
     *  range offset of <b>&phi;</b>, and <b>&Phi;</b> is the linear part of 
     *  <b>&phi;</b>.                                       
     *
     *  @param  vecIn   phase vector <b>z</b>
     *
     *  @return         the action of <b>&phi;</b> on input phase vector <b>z</b>
     */
    public PhaseVector apply(PhaseVector vecIn) {
        PhaseVector     vecCntrd = vecIn.minus( this.getDomainCenter() );
        PhaseVector     vecMappd = this.getFirstOrder().times( vecCntrd );
        PhaseVector     vecDspld = vecMappd.plus( this.getRangeDisplace() );
        
        return vecDspld;
    }
	 
    
//	 /** 
//	 * Calculate the fixed point solution vector representing the closed orbit at the location of this element.
//	 * We find the fixed point for the six phase space coordinates.
//	 * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 6x6 submatrix less the identity
//	 * matrix and <code>b</code> is the 7th column excluding the 7th row element.  The reason for this is that the
//	 * fixed point is defined by the point for which the transfer map maps to the same point.  This is
//	 * <code>M * v = v</code>.  
//	 * 
//	 * @return the fixed point solution
//	 */
//	 public PhaseVector calculateFixedPoint() {
//		 return getFirstOrder().calculateFixedPoint();
//	 }	 
//	 
//	 
//	 /** 
//	 * Calculate the fixed point solution vector representing the dispersion at the location of this element.
//	 * We find the fixed point for the four transverse phase space coordinates.
//	 * The equation to solve is <code>Ax + b = 0</code> where <code>A</code> is the 4x4 submatrix less the identity
//	 * matrix and <code>b</code> is the 6th column excluding the longitudinal row element.  The reason for this is that the
//	 * fixed point is defined by the point for which the transfer map maps to the same point.  This is
//	 * <code>M * v = v</code>.  
//	 * 
//	 * @return the dispersion vector
//	 */
//	 public double[] calculateDispersion(final double gamma) {
//		 return getFirstOrder().calculateDispersion(gamma);
//	 }
    
    
    /*
     * Support Methods
     */
    
    /**
     * Computes the new range displacement vector <b>&Delta;</b><sub>3</sub> for the
     * composition <b>&phi;</b><sub>3</sub> = <b>&phi;</b><sub>2</sub> &sdot; 
     * <b>&phi;</b><sub>1</sub>.
     * 
     * @param mapLeft   the left map <b>&phi;</b><sub>2</sub> in the composition
     * @param mapRight  the right map <b>&phi;</b><sub>1</sub> in the composition
     * 
     * @return  the displacement vector <b>&Delta</b><sub>3</sub> as described in the
     *          class documentation. 
     *
     * @author Christopher K. Allen
     * @since  Nov 5, 2013
     */
    private PhaseVector compComposedDisplacement(PhaseMap mapLeft, PhaseMap mapRight) {
        
        // Compute the new displacement vector
        PhaseVector vecTrm1 = mapRight.getRangeDisplace().minus( mapLeft.getDomainCenter() );
        PhaseVector vecTrm2 = mapLeft.getFirstOrder().times( vecTrm1 );
        PhaseVector vecDspl = vecTrm2.plus( mapLeft.getRangeDisplace() );
        
        return vecDspl;
    }
}
