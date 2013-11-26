/*
 * SphereR3.java
 *
 * Created on January 27, 2003, 3:35 PM
 */

package xal.tools.math.r3;


/**
 *  Represents a sphere in three-space.
 *
 * @author  Christopher Allen
 * @since   Jan, 2003
 */
public class Sphere implements java.io.Serializable {
    
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    /** center of the sphere */
    private R3      m_ptOrg;
    
    /** radius of the sphere */
    private double  m_dblRad = 0.0;
    
    
    
    /** 
     *  Creates a new instance of SphereR3 initialized to the arguments.
     *
     *  @param  ptOrg   the centroid of the sphere
     *  @param  dblRad  the radius of the sphere
     */
    public Sphere(R3 ptOrg, double dblRad) {
        m_ptOrg = ptOrg;
        m_dblRad = dblRad;
    }
    
    /**
     *  Get the radius of the sphere
     */
    public double   getRadius() { return m_dblRad; };
    
    /**
     *  Get the centroid of the sphere
     */
    public R3       getCentroid()   { return m_ptOrg; };
    
    
    
    /**
     *  Determine whether a point is an element of the sphere
     *
     *  @param  pt      point to be tested for membership
     *
     *  @return         true if pt is an element of the sphere
     */
    public boolean membership(R3 pt) {
        R3      vecDis = pt.minus(m_ptOrg);
        double  dblDis = vecDis.norm2();
        
        if (dblDis <= m_dblRad) return true;
        
        return false;
    }
    
    
    /**
     *  Determine whether a point is a boundary element of the sphere
     *
     *  @param  pt      point to be tested for boundary membership
     *
     *  @return         true if pt is an element of the sphere boundary
     */
    public boolean boundary(R3 pt)  {
        R3      vecDis = pt.minus(m_ptOrg);
        double  dblDis = vecDis.norm2();
        
        if (dblDis == m_dblRad) return true;
        
        return false;
    }
    
    /**
     *  Compute the volume of this sphere
     *
     *  @return     volume of sphere
     */
    public double volume() {
        return (4.0/3.0)*Math.PI*m_dblRad*m_dblRad*m_dblRad;
    }
    
}
