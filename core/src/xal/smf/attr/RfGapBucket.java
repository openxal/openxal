package xal.smf.attr;



/**
 * <p>
 * A set of RF gap attributes.
 * </p>
 * <p>
 * This bucket contains information about a specific gap within a resonate cavity such as a DTL
 * or CCL, which is driven by a common RF source.
 * </p>
 * <p>
 *   <pre>
 * Elements of this bucket are:
 * length - The length is the length of the gap (m)
 * phaseFactor - the ratio of the RF phase in the gap over the phase in the first gap
 * ampFactor - the ratio of the RF amplitude  in the gap over the amplitude in the first gap
 * TTF - The transit time factor of this gap
 *   </pre>
 * </p>
 *
 * @author  J. Galambos
 * @author Christopher K. Allen
 * 
 * @since   The Beginning
 * @version May 29, 2015
 */
public class RfGapBucket extends AttributeBucket {

    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Global Constants
     */
    
    public final static String  c_strType = "rfgap"; 

    final static String[]       c_arrNames = {  "length", 
                                                "phaseFactor",
                                                "ampFactor",
                                                "TTF",
                                                "amp",
                                                "phase",
                                                "freq",
                                                "testAmpFactor",
                                                "testPhaseFactor",
                                                "endCell",
                                                "gapOffset",
                                                "ttfCoeffs",
                                                "ttfpCoeffs",
                                                "stfCoeffs",
                                                "stfpCoeffs"
                                };
    
    /*
     *  Local Attributes
     */
    
    private Attribute   m_attLength;
    private Attribute   m_attPhaseFactor;
    private Attribute   m_attAmpFactor;
    private Attribute   m_attTTF;
    private Attribute   m_attAmp;
    private Attribute   m_attPhase;
    private Attribute   m_attFreq;
    private Attribute   m_attTestAmpFactor;
    private Attribute   m_attTestPhaseFactor;
    
    /** flag for whether this is and end cell (i.e. uses the end cell TTFs)" */
    private Attribute m_attEndCell;
    
    /** the distance between the Electric and geometric center (E_ctr - G_ctr) (m) */
    private Attribute  m_attGapOffset;    
    
    /** (Polynomial) coefficients for an expansion of the T(b) transit time factor about the design value */
    private Attribute   attTCoeffs;
    
    /** (Polynomial) coefficients for an expansion of the T(b) derivative (w.r.t. k) about the design value */
    private Attribute   attTpCoeffs;
    
    /** (Polynomial) coefficients for an expansion of the S(b) transit time factor about the design value */
    private Attribute   attSCoeffs;
    
    /** (Polynomial) coefficients for an expansion of the S(b) derivative (w.r.t. k) about the design value */
    private Attribute   attSpCoeffs;
    
    
    
    /*
     *  User Interface
     */
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for RfGapBucket.
     *
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public RfGapBucket() {
        super();
        
        // 
        //  Instantiate the original attributes for RF gap
        m_attLength = new Attribute(0.0);
        m_attPhaseFactor = new Attribute(0.0);
        m_attAmpFactor = new Attribute(0.0);
        m_attTTF = new Attribute(0.0);
        m_attAmp = new Attribute(new double[] {} );
        m_attPhase = new Attribute(new double[] {} );
        m_attFreq = new Attribute(new double[] {} );
        m_attTestAmpFactor = new Attribute(0.0);
        m_attTestPhaseFactor = new Attribute(0.0);
        m_attEndCell = new Attribute(0);
        m_attGapOffset = new Attribute(0.);
        
        // Instantiate the fits for the transit time factors
        //  and derivatives
        attTCoeffs  = new Attribute(new double[] {});
        attTpCoeffs = new Attribute(new double[] {});
        attSCoeffs  = new Attribute(new double[] {});
        attSpCoeffs = new Attribute(new double[] {});
    
        // Register the attributes with the attribute manager in the
        //  base class.
        super.registerAttribute(c_arrNames[0], m_attLength);
        super.registerAttribute(c_arrNames[1], m_attPhaseFactor);
        super.registerAttribute(c_arrNames[2], m_attAmpFactor);
        super.registerAttribute(c_arrNames[3], m_attTTF);
        super.registerAttribute(c_arrNames[4], m_attAmp);
        super.registerAttribute(c_arrNames[5], m_attPhase);
        super.registerAttribute(c_arrNames[6], m_attFreq);
        super.registerAttribute(c_arrNames[7], m_attTestAmpFactor);
        super.registerAttribute(c_arrNames[8], m_attTestPhaseFactor);
        super.registerAttribute(c_arrNames[9], m_attEndCell);
        super.registerAttribute(c_arrNames[10], m_attGapOffset);
        
        // Register the fits for the transit time factors with the
        //  base class attribute manager.
        super.registerAttribute(c_arrNames[11], attTCoeffs);
        super.registerAttribute(c_arrNames[12], attTpCoeffs);
        super.registerAttribute(c_arrNames[13], attSCoeffs);
        super.registerAttribute(c_arrNames[14], attSpCoeffs);
    }
    
    public void setLength(double Val)  { m_attLength.set(Val); ;}
    public void setAmpFactor(double Val)  { m_attAmpFactor.set(Val); };
    public void setPhaseFactor(double Val)      { m_attPhaseFactor.set(Val); };
    public void setTTF(double Val)  { m_attTTF.set(Val); };
    public void setAmp(double[] Val)  { m_attAmp.set(Val); };
    public void setPhase(double[] Val)  { m_attPhase.set(Val); };
    public void setFrequency(double[] Val)  { m_attFreq.set(Val); };
    public void setTestAmpFactor(double Val)  { m_attTestAmpFactor.set(Val); };
    public void setTestPhaseFactor(double Val)  { m_attTestPhaseFactor.set(Val); };
    public void setEndCell(int intVal)  { m_attEndCell.set(intVal); }    
    public void setGapOffset(double dblVal)  { m_attGapOffset.set(dblVal); }     

    /**
     * Sets the coefficients of the polynomial expansion for transit time
     * factor <i>T</i>(&beta;).  (The Fourier cosine transform of <i>E<sub>z</sub></i>(<i>z</i>).)
     * The coefficients should be in increasing order
     * of monomial degree. Specifically,
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>T</i>(&beta;) &approx; <i>a</i><sub>0</sub> + <i>a</i><sub>1</sub>&beta; + <i>a</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * 
     * @param arrCoeffs     {<i>a</i><sub>0</sub>, <i>a</i><sub>1</sub>, <i>a</i><sub>2</sub>, ...}
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public void setTCoefficients(double[] arrCoeffs) {
        this.attTCoeffs.set(arrCoeffs);
    }
    
    /**
     * Sets the array of coefficients forming the polynomial expansion for the transit time
     * factor derivative <i>T</i>'(&beta;) with respect to wave number <i>k</i>. (Note that the argument is &beta;.) 
     * The coefficients are arranged in increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>T</i>'(&beta;) &approx; <i>b</i><sub>0</sub> + <i>b</i><sub>1</sub>&beta; + <i>b</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @param arrCoeffs {<i>b</i><sub>0</sub>,<i>b</i><sub>1</sub>,<i>b</i><sub>2</sub>,...}
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public void setTpCoefficients(double[] arrCoeffs) {
        this.attTpCoeffs.set(arrCoeffs);
    }

    /**
     * Sets the coefficients of the polynomial expansion for transit time
     * factor <i>S</i>(&beta;). (The Fourier sine transform of <i>E<sub>z</sub></i>(<i>z</i>).) The coefficients should be in increasing order
     * of monomial degree. Specifically,
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>S</i>(&beta;) &approx; <i>a</i><sub>0</sub> + <i>a</i><sub>1</sub>&beta; + <i>a</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * 
     * @param arrCoeffs     {<i>a</i><sub>0</sub>, <i>a</i><sub>1</sub>, <i>a</i><sub>2</sub>, ...}
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public void setSCoefficients(double[] arrCoeffs) {
        this.attSCoeffs.set(arrCoeffs);
    }

    /**
     * Sets the array of coefficients forming the polynomial expansion for the transit time
     * factor derivative <i>S</i>'(&beta;) with respect to wave number <i>k</i>. (Note that the argument is &beta;.) 
     * The coefficients are arranged in increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>S</i>'(&beta;) &approx; <i>b</i><sub>0</sub> + <i>b</i><sub>1</sub>&beta; + <i>b</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @param arrCoeffs {<i>b</i><sub>0</sub>,<i>b</i><sub>1</sub>,<i>b</i><sub>2</sub>,...}
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public void setSpCoefficients(double[] arrCoeffs) {
        this.attSpCoeffs.set(arrCoeffs);
    }


    /*
     * Attribute Query
     */
    
    /** Override virtual to provide type signature */
    public String getType() { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    public double   getLength()  { return m_attLength.getDouble(); };
    public double   getAmpFactor()  { return m_attAmpFactor.getDouble(); };
    public double   getPhaseFactor()      { return m_attPhaseFactor.getDouble(); };
    public double   getTTF()  { return m_attTTF.getDouble(); };
    public double[]   getAmp()  { return m_attAmp.getArrDbl(); };
    public double[]   getPhase()  { return m_attPhase.getArrDbl(); };
    public double[]   getFrequency()  { return m_attFreq.getArrDbl(); };
    public double   getTestAmpFactor() {return m_attTestAmpFactor.getDouble(); };   
    public double   getTestPhaseFactor() {return m_attTestPhaseFactor.getDouble(); };   
    public int getEndCell() { return m_attEndCell.getInteger();}     
    public double getGapOffset() { return m_attGapOffset.getDouble();}
    
    /**
     * Returns the array of coefficients forming the polynomial expansion for the transit time
     * factor <i>T</i>(&beta;) about the design &beta;. (This is the Fourier cosine transform.) 
     * The coefficients are arranged in
     * increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>T</i>(&beta;) &approx; <i>a</i><sub>0</sub> + <i>a</i><sub>1</sub>&beta; + <i>a</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @return      {<i>a</i><sub>0</sub>,<i>a</i><sub>1</sub>,<i>a</i><sub>2</sub>,...} 
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public double[] getTCoefficients() {
        return this.attTCoeffs.getArrDbl();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for the transit time
     * factor derivative <i>T</i>'(&beta;) with respect to wave number <i>k</i>. (Note that the argument is &beta;.) 
     * The coefficients are arranged in increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>T</i>'(&beta;) &approx; <i>b</i><sub>0</sub> + <i>b</i><sub>1</sub>&beta; + <i>b</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @return      {<i>b</i><sub>0</sub>,<i>b</i><sub>1</sub>,<i>b</i><sub>2</sub>,...} 
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public double[] getTpCoefficients() {
        return this.attTpCoeffs.getArrDbl();
    }

    /**
     * Returns the array of coefficients forming the polynomial expansion for the transit time
     * factor <i>S</i>(&beta;) about the design &beta;. (This is the Fourier sine transform.) 
     * The coefficients are arranged in
     * increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>S</i>(&beta;) &approx; <i>a</i><sub>0</sub> + <i>a</i><sub>1</sub>&beta; + <i>a</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @return      {<i>a</i><sub>0</sub>,<i>a</i><sub>1</sub>,<i>a</i><sub>2</sub>,...} 
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public double[] getSCoefficients() {
        return this.attSCoeffs.getArrDbl();
    }
    
    /**
     * Returns the array of coefficients forming the polynomial expansion for the transit time
     * factor derivative <i>S</i>'(&beta;) with respect to wave number <i>k</i>. (Note that the argument is &beta;.) 
     * The coefficients are arranged in increasing order so that
     * <br/> 
     * <br/>
     * &nbsp; &nbsp; <i>S</i>'(&beta;) &approx; <i>b</i><sub>0</sub> + <i>b</i><sub>1</sub>&beta; + <i>b</i><sub>2</sub>&beta;<sup>2</sup> + ... 
     * 
     * @return      {<i>b</i><sub>0</sub>,<i>b</i><sub>1</sub>,<i>b</i><sub>2</sub>,...} 
     *
     * @since  May 29, 2015   by Christopher K. Allen
     */
    public double[] getSpCoefficients() {
        return this.attSpCoeffs.getArrDbl();
    }


};
