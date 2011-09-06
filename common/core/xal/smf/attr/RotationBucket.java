package xal.smf.attr;

/**
 * An attribute set for rotation alignment attributes (pitch, yaw, roll).
 * These alignments are offsets  in the local lattice coordinate system.
 * pitch is the rotation about x [mrad]
 * yaw is the rotation about y [mrad]
 * roll is the rotation about z [mrad]
 *
 * @author John Galambos, Christopher K. Allen
 * @version 1.1
 */


public class RotationBucket extends AttributeBucket  {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public final static String  c_strType = "rotation"; 

    final static String[]       c_arrNames = {  "pitch",
                                                "yaw",
                                                "roll"
                                };
    
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute       m_attAngPitch;      // pitch angle offset
    private Attribute       m_attAngYaw;        // yaw angle offset
    private Attribute       m_attAngRoll;       // roll angle offset

    
    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    
    public RotationBucket() {
        super();
        
        m_attAngPitch = new Attribute(0.0);
        m_attAngYaw   = new Attribute(0.0);
        m_attAngRoll  = new Attribute(0.0);
        
        super.registerAttribute(c_arrNames[0], m_attAngPitch);
        super.registerAttribute(c_arrNames[1], m_attAngYaw);
        super.registerAttribute(c_arrNames[2], m_attAngRoll);
    };
    

    /*
     *  Data Query
     */
    
    public double getPitch()    { return m_attAngPitch.getDouble(); };
    public double getYaw()      { return m_attAngYaw.getDouble(); };
    public double getRoll()     { return m_attAngRoll.getDouble(); };
    
    
    /*
     *  Data Assignment
     */
    
    public void setPitch(double dblVal) { m_attAngPitch.set(dblVal); };
    public void setYaw(double dblVal)   { m_attAngYaw.set(dblVal); };
    public void setRoll(double dblVal)  { m_attAngRoll.set(dblVal); };
    
    
   
};
