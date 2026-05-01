package xal.smf.attr;

/**
 * An attribute set for displacement alignment attributes (x, y, z).
 * These alignments are offsets  in the local lattice coordinate system.
 * x is in the horizontal direction 
 * y is in the vertical direction 
 * z is in the longitudinal direction along the beam
 *
 * @author John Galambos, Christopher K. Allen
 * @version 1.0
 */


public class DisplaceBucket extends AttributeBucket  {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public final static String  c_strType = "displacement"; 

    final static String[]       c_arrNames = {  "x", 
                                                "y",
                                                "z"
                                };
    
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute       m_attDspX;          // x plane offset
    private Attribute       m_attDspY;          // y plane offset
    private Attribute       m_attDspZ;          // z plane offset
    
                                
                                
    /*
     *  User Interface
     */
                                
    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    
    
    public DisplaceBucket() {
        super();
        
        m_attDspX = new Attribute(0.0);
        m_attDspY = new Attribute(0.0);
        m_attDspZ = new Attribute(0.0);
        
        super.registerAttribute(c_arrNames[0], m_attDspX);
        super.registerAttribute(c_arrNames[1], m_attDspY);
        super.registerAttribute(c_arrNames[2], m_attDspZ);
    };
    

    /** Returns the displacement offsets */
    public double getX()    { return m_attDspX.getDouble(); };
    public double getY()    { return m_attDspY.getDouble(); };
    public double getZ()    { return m_attDspZ.getDouble(); };
    
    
    public void setX(double dblVal)     { m_attDspX.set(dblVal); };
    public void setY(double dblVal)     { m_attDspY.set(dblVal); };
    public void setZ(double dblVal)     { m_attDspZ.set(dblVal); };
    
   
};
