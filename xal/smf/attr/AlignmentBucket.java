package xal.smf.attr;

/**
 * An attribute set for alignment attributes (x, y, z, pitch, yaw, roll).
 * These alignments are offsets  in the local lattice coordinate system.
 * x is in the horizontal direction [mm]
 * y is in the vertical direction [mm]
 * z is in the longitudinal direction along the beam [mm]
 * pitch is the rotation about x [mrad]
 * yaw is the rotation about y [mrad]
 * roll is the rotation about z [mrad]
 *
 * @author John Galambos, Nikolay Malitsky, Christopher K. Allen
 * @version 1.1
 */


public class AlignmentBucket extends AttributeBucket  {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    /*
     *  Constants
     */
    
    public final static String  c_strType = "align"; 

    final static String[]       c_arrNames = {  "x", 
                                                "y",
                                                "z",
                                                "pitch",
                                                "yaw",
                                                "roll"
                                };
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute       m_attDspX;          // x plane offset
    private Attribute       m_attDspY;          // y plane offset
    private Attribute       m_attDspZ;          // z plane offset
    
    private Attribute       m_attAngPitch;      // pitch angle offset
    private Attribute       m_attAngYaw;        // yaw angle offset
    private Attribute       m_attAngRoll;       // roll angle offset

    
    
    /*
     *  User Interface
     */
    
    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    
    
    public AlignmentBucket() {
        super();
        
        m_attDspX = new Attribute(0.0);
        m_attDspY = new Attribute(0.0);
        m_attDspZ = new Attribute(0.0);
        
        m_attAngPitch = new Attribute(0.0);
        m_attAngYaw   = new Attribute(0.0);
        m_attAngRoll  = new Attribute(0.0);
        
        super.registerAttribute(c_arrNames[0], m_attDspX);
        super.registerAttribute(c_arrNames[1], m_attDspY);
        super.registerAttribute(c_arrNames[2], m_attDspZ);
        
        super.registerAttribute(c_arrNames[3], m_attAngPitch);
        super.registerAttribute(c_arrNames[4], m_attAngYaw);
        super.registerAttribute(c_arrNames[5], m_attAngRoll);
    };
    

    /** Returns the displacement offsets */
    public double getX()    { return m_attDspX.getDouble(); };
    public double getY()    { return m_attDspY.getDouble(); };
    public double getZ()    { return m_attDspZ.getDouble(); };
    
    /** Returns the offset angles */
    public double getPitch()    { return m_attAngPitch.getDouble(); };
    public double getYaw()      { return m_attAngYaw.getDouble(); };
    public double getRoll()     { return m_attAngRoll.getDouble(); };
    
    
    public void setX(double dblVal)     { m_attDspX.set(dblVal); };
    public void setY(double dblVal)     { m_attDspY.set(dblVal); };
    public void setZ(double dblVal)     { m_attDspZ.set(dblVal); };
    
    public void setPitch(double dblVal) { m_attAngPitch.set(dblVal); };
    public void setYaw(double dblVal)   { m_attAngYaw.set(dblVal); };
    public void setRoll(double dblVal)  { m_attAngRoll.set(dblVal); };
    
    
   
};
