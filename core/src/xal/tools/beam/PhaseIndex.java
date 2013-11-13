/*
 * PhaseIndex.java
 * 
 * Created December, 2006
 * 
 * Christopher K. Allen
 */

package xal.tools.beam;

import java.util.EnumSet;

import xal.tools.math.IIndex;

/** 
 * Enumeration for the element position indices of six-dimensional
 * phase space object. 
 * 
 * @author  Christopher K. Allen
 * @version Nov 12, 2013
 */
public enum PhaseIndex implements IIndex {

    
    /*
     * Enumeration Constants
     */
    X   (0, 1),        // x plane spatial
    Xp  (1, 0),        // x plane momentum
    Y   (2, 3),        // y plane spatial
    Yp  (3, 2),        // y plane momentum
    Z   (4, 5),        // z plane spatial
    Zp  (5, 4);        // z plane momentum
                

    
    /*
     * Global Methods
     */
    
    /**
     * Return the set of indices corresponding to spatial
     * coordinates.
     * 
     * @return  set of spatial indices
     */
    static public EnumSet<PhaseIndex>  spatialIndices()    {
        return EnumSet.of(X,Y,Z);
    }
    
    /**
     * Return the set of indices corresponding to momentum
     * coordinates.
     * 
     * @return  set of momentum indices
     */
    static public EnumSet<PhaseIndex>  momentumIndices()  {
        return EnumSet.of(Xp,Yp,Zp);
    }
    
    
    /*
     * Local Attributes
     */
    
    /** index value */
    private final int   iVal;
    
    /** index value of conjugate coordinate */
    private final int   iConj;

    /** Default enumeration constructor */
    PhaseIndex(int iVal, int iConj)        { 
        this.iVal  = iVal;
        this.iConj = iConj;
    };
    
    
    /*
     * Public Methods
     */
    
    /** 
     * Return the integer value of the index position
     * 
     * @return  value of this index
     */
    public int val()    { 
        return this.iVal; 
    };
    
    
    /**
     * Return the conjugate variable index to this.
     * 
     * NOTE:
     * - This function is highly under-optimized.
     * 
     * @return          conjugate variable index
     */
    public PhaseIndex   conjugate() {
        for (PhaseIndex i : values()) {
            if (i.val() == this.iConj)
                return i;
        }
        
        return this;
    }
    
    
    
}