/*
 * PhaseIndexHom.java
 * 
 * Created December, 2006
 * 
 * Christopher K. Allen
 */
package xal.tools.beam;

/** 
 * Enumeration for the element position indices of a homogeneous
 * phase space objects.  Extends the <code>PhaseIndex</code> class
 * by adding the homogeneous element position <code>HOM</code>.
 * 
 * @author  Christopher K. Allen
 */
public enum PhaseIndexHom {
    
    /*
     * Enumeration Constants
     */
    X   (0),        // x plane spatial
    Xp  (1),        // x plane momentum
    Y   (2),        // y plane spatial
    Yp  (3),        // y plane momentum
    Z   (4),        // z plane spatial
    Zp  (5),        // z plane momentum
    HOM (6);        // homogeneous coordinate position
                
    
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
    PhaseIndexHom(int i)  {
        this.i = i;
    }


    /*
     * Indexing
     */
    
    /** 
     * Return the integer value of the index position 
     */
    public int val()    { return i; };
    
}
