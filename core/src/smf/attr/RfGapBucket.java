package xal.smf.attr;



/**
 * A set of RF gap attributes.
 * 
 * This bucket contains information about a specific gap withing a resonating cavity such as a DTL
 * or CCL, which is driven by a common RF source.
 * Elements of this bucket are:
 * length - The length is the length of the gap (m)
 * phaseFactor - the ratio of the RF phase in the gap over the phase in the first gap
 * ampFactor - the ratio of the RF amplitude  in the gap over the amplitude in the first gap
 * TTF - The transit time factor of this gap
 *
 * @author  J. Galambos
 */


public class RfGapBucket extends AttributeBucket {
    
    
    /*
     *  Constants
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
						"gapOffset"						
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
    
    
    /*
     *  User Interface
     */
    
    
    /** Override virtual to provide type signature */
    public String getType() { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };
    
    
    public RfGapBucket() {
        super();
        
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
    }
    
     
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

};
