package xal.smf.attr;


/**
 * A container class for Twiss parameter information
 *
 *
 * @author  John Galambos, Christopher K. Allen
 * @version 1.1
 */


public class TwissBucket extends AttributeBucket {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    
    /*
     *  Constants
     */
    
    public final static String  c_strType = "twiss"; 

    public final static String[] c_arrNames = { "x",
                                                "y",
                                                "ax", 
                                                "bx",
                                                "ex",
                                                "ay", 
                                                "by",
                                                "ey",
                                                "az",
                                                "bz",
                                                "ez",
						"etx",
						"etpx",
						"ety",
						"etpy",
						"mux",
						"muy"
                                    };
                                    

    // Enumeration of the three phase plane indexes
    public final static int     iXPlane = 0;        
    public final static int     iYPlane = 1;
    public final static int     iZPlane = 2;
    
    
   
    /*
     *  Local Attributes
     */
    private Attribute   m_attX;    
    private Attribute   m_attY;

    private Attribute   m_attAlphaX;
    private Attribute   m_attBetaX;
    private Attribute   m_attEmitX;

    private Attribute   m_attAlphaY;
    private Attribute   m_attBetaY;
    private Attribute   m_attEmitY;

    private Attribute   m_attAlphaZ;
    private Attribute   m_attBetaZ;
    private Attribute   m_attEmitZ;

    private Attribute	m_attEtaX;
    private Attribute	m_attEtaPx;
    private Attribute	m_attEtaY;
    private Attribute	m_attEtaPy;
	
    private Attribute	m_attMuX;
    private Attribute	m_attMuY;

    
    /*
     *  User Interface
     */
    
 
    /** Override virtual to provide type signature */
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    
   
    public TwissBucket() {
        super();
        m_attX = new Attribute(0.0);
        m_attY  = new Attribute(0.0);   

        m_attAlphaX = new Attribute(0.0);
        m_attBetaX  = new Attribute(0.0);
        m_attEmitX  = new Attribute(0.0);
        
        m_attAlphaY = new Attribute(0.0);
        m_attBetaY  = new Attribute(0.0);
        m_attEmitY  = new Attribute(0.0);
        
        m_attAlphaZ = new Attribute(0.0);
        m_attBetaZ  = new Attribute(0.0);
        m_attEmitZ  = new Attribute(0.0);
	
	m_attEtaX   = new Attribute(0.0);
	m_attEtaPx  = new Attribute(0.0);
	m_attEtaY   = new Attribute(0.0);
	m_attEtaPy  = new Attribute(0.0);
	
	m_attMuX    = new Attribute(0.0);
	m_attMuY    = new Attribute(0.0);
	
        super.registerAttribute(c_arrNames[0], m_attX);
        super.registerAttribute(c_arrNames[1], m_attY);
        
        super.registerAttribute(c_arrNames[2], m_attAlphaX);
        super.registerAttribute(c_arrNames[3], m_attBetaX);
        super.registerAttribute(c_arrNames[4], m_attEmitX);

        super.registerAttribute(c_arrNames[5], m_attAlphaY);
        super.registerAttribute(c_arrNames[6], m_attBetaY);
        super.registerAttribute(c_arrNames[7], m_attEmitY);

        super.registerAttribute(c_arrNames[8], m_attAlphaZ);
        super.registerAttribute(c_arrNames[9], m_attBetaZ);
        super.registerAttribute(c_arrNames[10], m_attEmitZ);
	
	super.registerAttribute(c_arrNames[11], m_attEtaX);
	super.registerAttribute(c_arrNames[12], m_attEtaPx);
	super.registerAttribute(c_arrNames[13], m_attEtaY);
	super.registerAttribute(c_arrNames[14], m_attEtaPy);
	
	super.registerAttribute(c_arrNames[15], m_attMuX);
	super.registerAttribute(c_arrNames[16], m_attMuY);
    };

    
    /*
     *  Data Query
     */
     
    public double   getX()      { return m_attX.getDouble(); };
    public double   getY()      { return m_attY.getDouble(); };
    
    public double   getAlphaX()     { return m_attAlphaX.getDouble(); };
    public double   getBetaX()      { return m_attBetaX.getDouble(); };
    public double   getEmitX()      { return m_attEmitX.getDouble(); };
    
    public double   getAlphaY()     { return m_attAlphaY.getDouble(); };
    public double   getBetaY()      { return m_attBetaY.getDouble(); };
    public double   getEmitY()      { return m_attEmitY.getDouble(); };
    
    public double   getAlphaZ()     { return m_attAlphaZ.getDouble(); };
    public double   getBetaZ()      { return m_attBetaZ.getDouble(); };
    public double   getEmitZ()      { return m_attEmitZ.getDouble(); };
    
    public double   getEtaX()       { return m_attEtaX.getDouble(); };
    public double   getEtaPx()      { return m_attEtaPx.getDouble(); };
    public double   getEtaY()       { return m_attEtaY.getDouble(); };
    public double   getEtaPy()      { return m_attEtaPy.getDouble(); };
    
    public double   getMuX()      { return m_attMuX.getDouble(); };
    public double   getMuY()      { return m_attMuY.getDouble(); };
    
    
    public double  getCentroid(int iPlane)  {
        switch (iPlane) { 
            case iXPlane: return getX(); 
            case iYPlane: return getY();
            default:      return Double.NaN;
        }
    };    

    public double   getAlpha(int iPlane)    { 
        switch (iPlane) { 
            case iXPlane: return getAlphaX(); 
            case iYPlane: return getAlphaY();
            case iZPlane: return getAlphaZ();
            default:      return Double.NaN;
        }
    };
    
    public double   getBeta(int iPlane)     { 
        switch (iPlane) {
            case iXPlane: return getBetaX();
            case iYPlane: return getBetaY();
            case iZPlane: return getBetaZ();
            default:      return Double.NaN;
        }
    };
    
    public double   getEmit(int iPlane) {
        switch (iPlane) {
            case iXPlane: return getEmitX();
            case iYPlane: return getEmitY();
            case iZPlane: return getEmitZ();
            default:      return Double.NaN;
        }
    };
    
    public double   getEta(int iPlane) {
        switch (iPlane) {
	    case iXPlane: return getEtaX();
	    case iYPlane: return getEtaY();
            default:      return Double.NaN;
        }
    };

    public double   getEtaP(int iPlane) {
        switch (iPlane) {
	    case iXPlane: return getEtaPx();
	    case iYPlane: return getEtaPy();
            default:      return Double.NaN;
        }
    };

    public double   getMu(int iPlane) {
        switch (iPlane) {
	    case iXPlane: return getMuX();
	    case iYPlane: return getMuY();
            default:      return Double.NaN;
        }
    };


    
    /*
     *  Data Assignment
     */
     
    public void setX(double dblVal)     { m_attX.set(dblVal); };
    public void setY(double dblVal)     { m_attY.set(dblVal); };
    
    public void setAlphaX(double dblVal)    { m_attAlphaX.set(dblVal); };
    public void setBetaX(double dblVal)     { m_attBetaX.set(dblVal); };
    public void setEmitX(double dblVal)     { m_attEmitX.set(dblVal); };
     
    public void setAlphaY(double dblVal)    { m_attAlphaY.set(dblVal); };
    public void setBetaY(double dblVal)     { m_attBetaY.set(dblVal); };
    public void setEmitY(double dblVal)     { m_attEmitY.set(dblVal); };
     
    public void setAlphaZ(double dblVal)    { m_attAlphaZ.set(dblVal); };
    public void setBetaZ(double dblVal)     { m_attBetaZ.set(dblVal); };
    public void setEmitZ(double dblVal)     { m_attEmitZ.set(dblVal); };

    public void setEtaX(double dblVal)      { m_attEtaX.set(dblVal); };
    public void setEtaPx(double dblVal)     { m_attEtaPx.set(dblVal); };
    public void setEtaY(double dblVal)      { m_attEtaY.set(dblVal); };
    public void setEtaPy(double dblVal)     { m_attEtaPy.set(dblVal); };
    
    public void setMuX(double dblVal)       { m_attMuX.set(dblVal); };
    public void setMuY(double dblVal)       { m_attMuY.set(dblVal); };
     
    
    public void  setCentroid(int iPlane, double dblVal)  {
        switch (iPlane) { 
            case iXPlane: setX(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iYPlane: setY(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };    

    public void setAlpha(int iPlane, double dblVal) { 
        switch (iPlane) {
            case iXPlane: setAlphaX(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iYPlane: setAlphaY(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iZPlane: setAlphaZ(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };
        
    public void setBeta(int iPlane, double dblVal)  { 
        switch (iPlane) {
            case iXPlane: setBetaX(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iYPlane: setBetaY(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iZPlane: setBetaZ(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };

    public void setEmit(int iPlane, double dblVal)  { 
        switch (iPlane) {
            case iXPlane: setEmitX(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iYPlane: setEmitY(dblVal); break;  // tap added this break statement as it seems to be the intent
            case iZPlane: setEmitZ(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };
    
    public void setEta(int iPlane, double dblVal) {
        switch (iPlane) {
	    case iXPlane: setEtaX(dblVal); break;  // tap added this break statement as it seems to be the intent
	    case iYPlane: setEtaY(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };

    public void getEtaP(int iPlane, double dblVal) {
        switch (iPlane) {
	    case iXPlane: setEtaPx(dblVal); break;  // tap added this break statement as it seems to be the intent
	    case iYPlane: setEtaPy(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };

    public void getMu(int iPlane, double dblVal) {
        switch (iPlane) {
	    case iXPlane: setMuX(dblVal); break;  // tap added this break statement as it seems to be the intent
	    case iYPlane: setMuY(dblVal); break;  // tap added this break statement as it seems to be the intent
        }
    };

    
 
};















