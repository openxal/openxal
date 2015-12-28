package xal.smf.attr;



/**
 * A set of RF cavity attributes. Here's what's in it:
 *
 *  amp - the default field amplitude (in kV/m)
 *  phase - the default phase (deg)
 *  freq - the frequency (MHz)
 *  ampFactor - calibration factor for klystron amplitude to cavity field value (ratio)
 *  phaseOffset - calibration offset for beam - klystron phase
 *  TTFCoefs - coefficients of a 2nd order polynomial representing the transit time factor function T(betat) 
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */


public class RfCavityBucket extends AttributeBucket {
    
	private static final long serialVersionUID = 1;
	
    /*
     *  Constants
     */
    
    public final static String  c_strType = "rfcavity"; 

    final static String[]       c_arrNames = {  "amp", 
                                                "phase",
                                                "freq",
                                                "ampFactor",
                                                "phaseOffset",
                                                "TTFCoefs",
                                                "TTFPrimeCoefs",
                                                "STFCoefs",
                                                "STFPrimeCoefs",
                                                "TTF_endCoefs",
                                                "TTFPrime_EndCoefs",
                                                "STF_endCoefs",
                                                "STFPrime_endCoefs",
                                                "structureMode",
                                                "qLoaded",
                                                "structureTTF"
    };
    
    
    /*
     *  Local Attributes
     */
    
    /** Default field amplitude (in kV/m) */
    private Attribute   m_attAmp;
    
    /** Default (design) cavity RF phase */
    private Attribute   m_attPhase;
    
    /** Design cavity resonant frequency */
    private Attribute   m_attFreq;
    
    /** Calibration factor for klystron amplitude to cavity field value (ratio) */
    private Attribute   m_attAmpFactor;
    
    /** Calibration offset for beam-to-klystron phase */
    private Attribute   m_attPhaseOffset;
    
    /** quadratic fit coefficients for the transit time factor as a function of beta (constant, linear, quad) */
    private Attribute   m_attTTFCoefs;
    
    /** quadratic fit coefficients for the transit time factor prime as a function of beta (constant, linear, quad) */
    private Attribute   m_attTTFPrimeCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" as a function of beta (constant, linear, quad) */
    private Attribute   m_attSTFCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" prime as a function of beta (constant, linear, quad) */
    private Attribute   m_attSTFPrimeCoefs;
    
   /** quadratic fit coefficients for the transit time factor as a function of beta  for the end cells (constant, linear, quad) */     
    private Attribute   m_attTTF_endCoefs;
    
    /** quadratic fit coefficients for the transit time factor prime as a function of beta for the end cells  (constant, linear, quad) */
    private Attribute   m_attTTFPrime_endCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" as a function of beta for the end cells  (constant, linear, quad) */
    private Attribute   m_attSTF_endCoefs;
    
    /** quadratic fit coefficients for the "S transit time factor" prime as a function of beta for the end cells (constant, linear, quad) */
    private Attribute   m_attSTFPrime_endCoefs;
    
    /** flag for the structure type (0 or pi mode) (CKA pi mode has a value 1/2) */
    private Attribute m_attStructureMode;
    
    /** quality factor with all external contribution */
    private Attribute m_attQLoaded;
    
    /** TTF used in the real accelerator LLRF */
    private Attribute m_attStructureTTF;
    
    /*
     *  User Interface
     */
    
    
    /** Override virtual to provide type signature */
    public String getType() { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    
    
    public RfCavityBucket() {
        super();
        
        m_attAmp = new Attribute( 0. );
        m_attPhase = new Attribute(0. );
        m_attFreq = new Attribute(0. );
		m_attAmpFactor = new Attribute(1.);
		m_attPhaseOffset = new Attribute(0. );
		m_attTTFCoefs = new Attribute(new double[] {});
		m_attTTFPrimeCoefs = new Attribute(new double[] {});
		m_attSTFCoefs = new Attribute(new double[] {});
		m_attSTFPrimeCoefs = new Attribute(new double[] {});
		m_attTTF_endCoefs = new Attribute(new double[] {});
		m_attTTFPrime_endCoefs = new Attribute(new double[] {});
		m_attSTF_endCoefs = new Attribute(new double[] {});
		m_attSTFPrime_endCoefs = new Attribute(new double[] {});
		m_attStructureMode = new Attribute(0. );
		m_attQLoaded = new Attribute(0. );
		m_attStructureTTF = new Attribute(1. );
	
        super.registerAttribute(c_arrNames[0], m_attAmp);
        super.registerAttribute(c_arrNames[1], m_attPhase);
        super.registerAttribute(c_arrNames[2], m_attFreq);
        super.registerAttribute(c_arrNames[3], m_attAmpFactor);
        super.registerAttribute(c_arrNames[4], m_attPhaseOffset);
        super.registerAttribute(c_arrNames[5], m_attTTFCoefs);	
        super.registerAttribute(c_arrNames[6], m_attTTFPrimeCoefs);
        super.registerAttribute(c_arrNames[7], m_attSTFCoefs);	
        super.registerAttribute(c_arrNames[8], m_attSTFPrimeCoefs);		
        super.registerAttribute(c_arrNames[9], m_attTTF_endCoefs);	
        super.registerAttribute(c_arrNames[10], m_attTTFPrime_endCoefs);
        super.registerAttribute(c_arrNames[11], m_attSTF_endCoefs);	
        super.registerAttribute(c_arrNames[12], m_attSTFPrime_endCoefs);
        super.registerAttribute(c_arrNames[13], m_attStructureMode);
        super.registerAttribute(c_arrNames[14], m_attQLoaded);
        super.registerAttribute(c_arrNames[15], m_attStructureTTF);
    }
    
     
    public double   getAmplitude()  { return m_attAmp.getDouble(); }
    public double   getPhase()      { return m_attPhase.getDouble(); }
    public double   getFrequency()  { return m_attFreq.getDouble(); }
    public double   getAmpFactor()  { return m_attAmpFactor.getDouble(); }
    public double   getPhaseOffset(){ return m_attPhaseOffset.getDouble(); }
    public double []   getTTFCoefs(){ return m_attTTFCoefs.getArrDbl(); }
    public double []   getTTFPrimeCoefs(){ return m_attTTFPrimeCoefs.getArrDbl(); }
    public double []   getSTFCoefs(){ return m_attSTFCoefs.getArrDbl(); }
    public double []   getSTFPrimeCoefs(){ return m_attSTFPrimeCoefs.getArrDbl(); }  
    public double []   getTTF_endCoefs(){ return m_attTTF_endCoefs.getArrDbl(); }
    public double []   getTTFPrime_endCoefs(){ return m_attTTFPrime_endCoefs.getArrDbl(); }
    public double []   getSTF_endCoefs(){ return m_attSTF_endCoefs.getArrDbl(); }
    public double []   getSTFPrime_endCoefs(){ return m_attSTFPrime_endCoefs.getArrDbl(); } 
    public double getStructureMode() { return m_attStructureMode.getDouble();} 
    public double getQLoaded() {return m_attQLoaded.getDouble();}
    public double getStructureTTF() {return m_attStructureTTF.getDouble();}

    public void setAmplitude(double dblVal)  { m_attAmp.set(dblVal); }
    public void setPhase(double dblVal)      { m_attPhase.set(dblVal); }
    public void setFrequency(double dblVal)  { m_attFreq.set(dblVal); }
    public void setAmpFactor(double dblVal)  { m_attAmpFactor.set(dblVal); }
    public void setPhaseOffset(double dblVal){ m_attPhaseOffset.set(dblVal); }
    public void setTTFCoefs(double [] arrVal){ m_attTTFCoefs.set(arrVal); }
    public void setTTFPrimeCoefs(double [] arrVal){ m_attTTFPrimeCoefs.set(arrVal); }  
    public void setSTFCoefs(double [] arrVal){ m_attSTFCoefs.set(arrVal); }
    public void setSTFPrimeCoefs(double [] arrVal){ m_attSTFPrimeCoefs.set(arrVal);}
    public void setTTF_endCoefs(double [] arrVal){ m_attTTF_endCoefs.set(arrVal); }
    public void setTTFPrime_endCoefs(double [] arrVal){ m_attTTFPrime_endCoefs.set(arrVal); }  
    public void setSTF_endCoefs(double [] arrVal){ m_attSTF_endCoefs.set(arrVal); }
    public void setSTFPrime_endCoefs(double [] arrVal){ m_attSTFPrime_endCoefs.set(arrVal); }
    public void setStructureMode(double dblVal)  { m_attStructureMode.set(dblVal); }
    public void setQLoaded(double dblVal) { m_attQLoaded.set(dblVal); }
    public void setStructureTTF(double dblVal) { m_attStructureTTF.set(dblVal); }
};
