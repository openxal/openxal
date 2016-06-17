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
            "dipoleEntrRotAngle",   // dipole rotation angle for entrance pole face
            "dipoleExitRotAngle",   // dipole rotation angle for exit pole face
            "dipoleQuadComponent",   // quadrupole component for bend dipole
            "frFldIntK0"           // zero-order fringe field integral
    };
    



    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };

    public String[] getAttrNames()  { return c_arrNames; };


    public MagnetBucket() {
        super();

        this.attLenEff  = new Attribute(0.0);
        this.attFldDflt = new Attribute(0.0 );
        this.attBendAngle = new Attribute(0.0 );
        this.attPolarity = new Attribute(1.0 );
        this.attFldNorm = new Attribute(new double[] {} );
        this.attFldTang = new Attribute(new double[] {} );
        this.attPathLength = new Attribute(0.0 );
        this.attDipoleEntrRotAngle = new Attribute(0.0 );
        this.attDipoleExitRotAngle = new Attribute(0.0 );
        this.attDipoleQuadComponent = new Attribute(0.0 );
        this.attFrFldIntK0 = new Attribute(0.0);


        super.registerAttribute(c_arrNames[0], attLenEff);
        super.registerAttribute(c_arrNames[1], attFldDflt);
        super.registerAttribute(c_arrNames[2], attPolarity);
        super.registerAttribute(c_arrNames[3], attBendAngle);
        super.registerAttribute(c_arrNames[4], attFldNorm);
        super.registerAttribute(c_arrNames[5], attFldTang);
        super.registerAttribute(c_arrNames[6], attPathLength);
        super.registerAttribute(c_arrNames[7], attDipoleEntrRotAngle);
        super.registerAttribute(c_arrNames[8], attDipoleExitRotAngle);
        super.registerAttribute(c_arrNames[9], attDipoleQuadComponent);
        super.registerAttribute(c_arrNames[10], attFrFldIntK0);
    }

     
    /** return the magnetic length (in m) */
    public double   getEffLength()  { return attLenEff.getDouble(); };
    /** return the design magnetic field strength (in Tesla) */
    public double   getDfltField()  { return attFldDflt.getDouble(); };
    /** return the magnet polarity ( 1 or -1) */
    public double   getPolarity()   { return attPolarity.getDouble(); };
    
    /** return the dipole bend angle (in degrees) */
    public double   getBendAngle()   { return attBendAngle.getDouble(); };
    
    /** 
     * Return the field in the normal direction 
     * 
     * @return  field component in the direction of propagation,
     *
     * @since  Jun 15, 2016,   Christopher K. Allen
     */
    public double[] getNormField()  { return attFldNorm.getArrDbl(); };
    
    /** 
     * Return the field in the tangential direction 
     * 
     * @return  field component in the orthogonal to the direction of propagation,
     *
     * @since  Jun 15, 2016,   Christopher K. Allen
     */
    public double[] getTangField()  { return attFldTang.getArrDbl(); };
    
    /** return the design path length (in m) */
    public double   getPathLength() { return attPathLength.getDouble(); };
    /** return the dipole rotation angle for entrance pole face (in degrees) */
    public double   getDipoleEntrRotAngle() { return attDipoleEntrRotAngle.getDouble(); };
    /** return the dipole rotation angle for exit pole face (in degrees) */
    public double   getDipoleExitRotAngle() { return attDipoleExitRotAngle.getDouble(); };
    /** return the quadrupole component for bend dipole */
    public double   getDipoleQuadComponent() { return attDipoleQuadComponent.getDouble(); };
    
    /**
     * Return the fringe field integral
     * <br/>
     * What are the units?
     * <br/>
     * How is the integral defined?
     */
    public double   getFringeFieldIntegralK0() {
    	return attFrFldIntK0.getDouble();
    }
    
    /** set the magnetic length (in m) 
     * @param dblVal magnetic length in meters
     */
    public void setEffLength(double dblVal)     { attLenEff.set(dblVal); };
    /** set the magnet polarity 
     * @param dblVal magnet polarity (1 or -1)
     */
    public void setPolarity(double dblVal)      { attPolarity.set(dblVal); };
    /** set the dipole bend angle (in degrees)
     * @param dblVal dipole bend angle in degrees
     */
    public void setBendAngle(double dblVal)      { attBendAngle.set(dblVal); };
    public void setNormField(double[] arrVal)   { attFldNorm.set(arrVal); };
    public void setTangField(double[] arrVal)   { attFldTang.set(arrVal); };
    public void setDfltField(double dblVal)   { attFldDflt.set(dblVal); };   
    /** set the dipole path length (in m) 
     * @param dblVal path length in meters
     */
    public void setPathLength(double dblVal)    { attPathLength.set(dblVal); };
    /** set the dipole rotation angle for entrance pole face (in degrees) 
     * @param dblVal dipole rotation angle for entrance pole face in degrees
     */
    public void setDipoleEntrRotAngle(double dblVal)    { attDipoleEntrRotAngle.set(dblVal); };
    /** set the dipole rotation angle for exit pole face (in degrees) 
     * @param dblVal dipole rotation angle for exit pole face in degrees
     */
    public void setDipoleExitRotAngle(double dblVal)    { attDipoleExitRotAngle.set(dblVal); };
    /** set the quadrupole component for bend dipole
     * @param dblVal quadrupole component for bend dipole
     */
    public void setDipoleQuadComponent(double dblVal)    { attDipoleQuadComponent.set(dblVal); };
    
    /**
     * set fringe filed integral
     * <br/>
     * What are the units?
     * <br/>
     * How is the integral defined?
     */
    public void setFringeFieldIntegralK0( double dblVal ) {
    	attFrFldIntK0.set( dblVal );
    }
    
    /*
     *  Local Attributes
     */
    
    /** effective magnetic length (m) */
    private Attribute       attLenEff;            
    
    /**  design field strength (T/m^(n-1)), n=1 for dipole, 2 for quad,... */
    private Attribute       attFldDflt;           
    
    /** polarity */
    private Attribute       attPolarity;          
    
    /** bend Angle for dipoles (deg)  */
    private Attribute       attBendAngle;         
    
    /**  normal field multipole coefficients */
    private Attribute       attFldNorm;           
    
    /** tangential field multipole coefficients */
    private Attribute       attFldTang;
    
    /** path length  (m) */
    private Attribute       attPathLength;
    
    /** dipole rotation angle for entrance pole face (degree) */
    private Attribute       attDipoleEntrRotAngle;
    
    /** dipole rotation angle for exit pole face (degree) */
    private Attribute       attDipoleExitRotAngle;
    
    /** quadrupole component for bend dipole */
    private Attribute       attDipoleQuadComponent;
    
    /** zero-order fringe field integral*/
    private Attribute      attFrFldIntK0;
}
