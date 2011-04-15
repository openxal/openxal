/*
 * PhaseIndex.java
 * 
 * Created December, 2006
 * 
 * Christopher K. Allen
 */

package xal.tools.beam;

/** 
 * Enumeration for the element position indices of three-dimensional objects. 
 * 
 * @author  Christopher K. Alen
 */
public enum SpaceIndex3D {

    
    /*
     * Enumeration Constants
     */
    /** The horizontal axis  */
    X   (0),        // x plane spatial
    
    /** The vertical axis  */
    Y   (1),        // y plane spatial
    
    /** The longitudinal axis */
    Z   (2);        // z plane spatial
                
    
    /*
     * Local Attributes
     */
    
    /** index value */
    private final int i;
    
    
    /** 
     * Default enumeration constructor
     * 
     * @param i     enumeration constant
     */
    SpaceIndex3D(int i)        { this.i = i; };
    
    /** 
     * Return the integer value of the index position
     *  
     * @return  Integer value of enumeration constant
     */
    public int val()    { return i; };
    
}