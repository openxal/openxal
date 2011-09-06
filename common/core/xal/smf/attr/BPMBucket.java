package xal.smf.attr;

/**
 * An attribute set for the BPM
 * @author John Galambos, 
 * @version 1.1
 */


public class BPMBucket extends AttributeBucket  {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    

    
    /*
     *  Constants
     */
    
    public final static String  c_strType = "bpm"; 

    final static String[]       c_arrNames = {  "frequency", 
                                                "length",
                                                "orientation"
                                };
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute       m_attFrequency;     // the phase frequency (MHz)
    private Attribute       m_attLength;        // stripline length (m)
    private Attribute       m_attOrientation;       // leads come in up (1)or downstream (-1)  
    /*
     *  User Interface
     */
    
    /** Override virtual to provide type signature */
    
    public BPMBucket() {
        super();
        
        m_attFrequency = new Attribute(0.0);
        m_attLength = new Attribute(0.0);
        m_attOrientation = new Attribute(1);
               
        super.registerAttribute(c_arrNames[0], m_attFrequency);
        super.registerAttribute(c_arrNames[1], m_attLength);
        super.registerAttribute(c_arrNames[2], m_attOrientation);
     };
    
    public String getType()         { return c_strType; };
    
    public String[] getAttrNames()  { return c_arrNames; };

    /** Returns the displacement offsets */
    public double getFrequency()    { return m_attFrequency.getDouble(); };
    public double getLength()    { return m_attLength.getDouble(); };
    public double getOrientation()    { return m_attOrientation.getInteger(); };
      
    
    public void setFrequency(double dblVal)     { m_attFrequency.set(dblVal); };
    public void setLength(double dblVal)     { m_attLength.set(dblVal); };
    public void setOrientation(int  intVal)     { m_attOrientation.set(intVal); };  

};
