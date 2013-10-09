package xal.smf.attr;


/**
 * Attribute set for magnet information <br>
 *
 * len - is the effective magnetic length [m]<br>
 * dfltMagField is the default field value (T for dipole, T/m for quad, etc.)<br>
 * polarity - is the polarity flag. 1 means positive current = positive field.
 *     -1 means positive current = negative field<br>
 * multFieldNorm - is an array of the normal direction multipole components 
 *   element n is the n'th pole field level over the primary field
 *   where n= 0 for dipole, n=1 for quad, ...<br>
 * multFieldSkew - is the same as multFieldNorm, but is for the skew direction<br>
 *
 * @author  Nikolay Malitsky 
 * @author  John Galambos
 * @author  Christopher K. Allen
 * @author  Paul C. Chu
 */
public class MagnetBucket extends AttributeBucket {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    private final static String     c_strType = "magnet"; 

    private final static String[]   c_arrNames = {  "len",      // effective length
                                                    "dfltMagFld", // default field value
                                                    "polarity", // default polarity value
                                                    "bendAngle", // bend angle
                                                    "multFieldNorm",      // normal field components
                                                    "multFieldSkew",       // skew field components
                                                    "pathLength",   // path length
                                                    "dipoleEntrRotAngle",  // dipole rotation angle for entrance pole face
                                                    "dipoleExitRotAngle",  // dipole rotation angle for exit pole face
						    "dipoleQuadComponent" // quadrupole component for bend dipole
                                    };
    
    
     
    
    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    public MagnetBucket() {
        super();
        
        m_attLenEff  = new Attribute(0.0);
        m_attFldDflt = new Attribute(0.0 );
        m_attBendAngle = new Attribute(0.0 );
        m_attPolarity = new Attribute(1.0 );
        m_attFldNorm = new Attribute(new double[] {} );
        m_attFldTang = new Attribute(new double[] {} );
        m_attPathLength = new Attribute(0.0 );
        m_attDipoleEntrRotAngle = new Attribute(0.0 );
        m_attDipoleExitRotAngle = new Attribute(0.0 );
        m_attDipoleQuadComponent = new Attribute(0.0 );
	
        super.registerAttribute(c_arrNames[0], m_attLenEff);
        super.registerAttribute(c_arrNames[1], m_attFldDflt);
        super.registerAttribute(c_arrNames[2], m_attPolarity);
        super.registerAttribute(c_arrNames[3], m_attBendAngle);
        super.registerAttribute(c_arrNames[4], m_attFldNorm);
        super.registerAttribute(c_arrNames[5], m_attFldTang);
        super.registerAttribute(c_arrNames[6], m_attPathLength);
        super.registerAttribute(c_arrNames[7], m_attDipoleEntrRotAngle);
        super.registerAttribute(c_arrNames[8], m_attDipoleExitRotAngle);
	super.registerAttribute(c_arrNames[9], m_attDipoleQuadComponent);
    }
    
     
    /** return the magnetic length (in m) */
    public double   getEffLength()  { return m_attLenEff.getDouble(); };
    /** return the design magnetic field strength (in Tesla) */
    public double   getDfltField()  { return m_attFldDflt.getDouble(); };
    /** return the magnet polarity ( 1 or -1) */
    public double   getPolarity()   { return m_attPolarity.getDouble(); };
    /** return the dipole bend angle (in degrees) */
    public double   getBendAngle()   { return m_attBendAngle.getDouble(); };
    public double[] getNormField()  { return m_attFldNorm.getArrDbl(); };
    public double[] getTangField()  { return m_attFldTang.getArrDbl(); };
    /** return the design path length (in m) */
    public double   getPathLength() { return m_attPathLength.getDouble(); };
    /** return the dipole rotation angle for entrance pole face (in degrees) */
    public double   getDipoleEntrRotAngle() { return m_attDipoleEntrRotAngle.getDouble(); };
    /** return the dipole rotation angle for exit pole face (in degrees) */
    public double   getDipoleExitRotAngle() { return m_attDipoleExitRotAngle.getDouble(); };
    /** return the quadrupole component for bend dipole */
    public double   getDipoleQuadComponent() { return m_attDipoleQuadComponent.getDouble(); };
    
    /** set the magnetic length (in m) 
     * @param dblVal magnetic length in meters
     */
    public void setEffLength(double dblVal)     { m_attLenEff.set(dblVal); };
    /** set the magnet polarity 
     * @param dblVal magnet polarity (1 or -1)
     */
    public void setPolarity(double dblVal)      { m_attPolarity.set(dblVal); };
    /** set the dipole bend angle (in degrees)
     * @param dblVal dipole bend angle in degrees
     */
    public void setBendAngle(double dblVal)      { m_attBendAngle.set(dblVal); };
    public void setNormField(double[] arrVal)   { m_attFldNorm.set(arrVal); };
    public void setTangField(double[] arrVal)   { m_attFldTang.set(arrVal); };
    public void setDfltField(double dblVal)   { m_attFldDflt.set(dblVal); };   
    /** set the dipole path length (in m) 
     * @param dblVal path length in meters
     */
    public void setPathLength(double dblVal)    { m_attPathLength.set(dblVal); };
    /** set the dipole rotation angle for entrance pole face (in degrees) 
     * @param dblVal dipole rotation angle for entrance pole face in degrees
     */
    public void setDipoleEntrRotAngle(double dblVal)    { m_attDipoleEntrRotAngle.set(dblVal); };
    /** set the dipole rotation angle for exit pole face (in degrees) 
     * @param dblVal dipole rotation angle for exit pole face in degrees
     */
    public void setDipoleExitRotAngle(double dblVal)    { m_attDipoleExitRotAngle.set(dblVal); };
    /** set the quadrupole component for bend dipole
     * @param dblVal quadrupole component for bend dipole
     */
    public void setDipoleQuadComponent(double dblVal)    { m_attDipoleQuadComponent.set(dblVal); };    
    
    /*
     *  Local Attributes
     */
    
    /** effective magnetic length (m) */
    private Attribute       m_attLenEff;            
    /**  design field strength (T/m^(n-1)), n=1 for dipole, 2 for quad,... */
    private Attribute       m_attFldDflt;           
    /** polarity */
    private Attribute       m_attPolarity;          
    /** bend Angle for dipoles (deg)  */
    private Attribute       m_attBendAngle;         
    /**  normal field multipole coefficients */
    private Attribute       m_attFldNorm;           
    /** tangential field multipole coefficients */
    private Attribute       m_attFldTang;
    /** path length  (m) */
    private Attribute       m_attPathLength;
    /** dipole rotation angle for entrance pole face (degree) */
    private Attribute       m_attDipoleEntrRotAngle;
    /** dipole rotation angle for exit pole face (degree) */
    private Attribute       m_attDipoleExitRotAngle;
    /** quadrupole component for bend dipole */
    private Attribute       m_attDipoleQuadComponent;
}
