package se.lu.esss.ics.jels.smf.attr;

import xal.smf.attr.Attribute;
import xal.smf.attr.RfCavityBucket;



/**
 * An extended set of RF cavity attributes. Added are TTF/STF coefficients for the start gap.
 *
 * @author  Ivo List
 */


public class ESSRfCavityBucket extends RfCavityBucket {
    
	private static final long serialVersionUID = 1;
	
    /*
     *  Constants
     */
    
    public final static String  c_strType = "rfcavity"; 

    final static String[]       c_arrNames = {  "TTF_startCoefs", 
    											"TTFPrime_startCoefs",
                                                "STF_startCoefs",
                                                "STFPrime_startCoefs",
						                         };
    
    
    /*
     *  Local Attributes
     */
    
    /** quadratic fit coefficients for the transit time factor as a function of beta  for the start cells (constant, linear, quad) */     
    private Attribute   m_attTTF_startCoefs;
    /** quadratic fit coefficients for the transit time factor prime as a function of beta for the start cells  (constant, linear, quad) */
    private Attribute   m_attTTFPrime_startCoefs;   
    /** quadratic fit coefficients for the "S transit time factor" as a function of beta for the start cells  (constant, linear, quad) */
    private Attribute   m_attSTF_startCoefs;
    /** quadratic fit coefficients for the "S transit time factor" prime as a function of beta for the start cells (constant, linear, quad) */
    private Attribute   m_attSTFPrime_startCoefs; 
    
    /*
     *  User Interface
     */
        
    /** Override virtual to provide type signature */
    public String getType() { return c_strType; };
    
    /** Override virtual to provide type signature */
    @Override
    public String[] getAttrNames()  { 
    	String[] attrs = super.getAttrNames();
    	String[] allAttrs = new String[attrs.length + c_arrNames.length];
    	System.arraycopy(attrs, 0, allAttrs, 0, attrs.length);
    	System.arraycopy(c_arrNames, 0, allAttrs, attrs.length, c_arrNames.length);
    	return allAttrs;    	
    }

    public ESSRfCavityBucket() {
        super();
        
    	m_attTTF_startCoefs = new Attribute(new double[] {});
		m_attTTFPrime_startCoefs = new Attribute(new double[] {});
		m_attSTF_startCoefs = new Attribute(new double[] {});
		m_attSTFPrime_startCoefs = new Attribute(new double[] {});
	
        super.registerAttribute(c_arrNames[0], m_attTTF_startCoefs);	
        super.registerAttribute(c_arrNames[1], m_attTTFPrime_startCoefs);
        super.registerAttribute(c_arrNames[2], m_attSTF_startCoefs);	
        super.registerAttribute(c_arrNames[3], m_attSTFPrime_startCoefs);        
    }
    
     
    public double []   getTTF_startCoefs(){ return m_attTTF_startCoefs.getArrDbl(); }
    public double []   getTTFPrime_startCoefs(){ return m_attTTFPrime_startCoefs.getArrDbl(); }
    public double []   getSTF_startCoefs(){ return m_attSTF_startCoefs.getArrDbl(); }
    public double []   getSTFPrime_startCoefs(){ return m_attSTFPrime_startCoefs.getArrDbl(); } 

    public void setTTF_startCoefs(double [] arrVal){ m_attTTF_startCoefs.set(arrVal); }
    public void setTTFPrime_startCoefs(double [] arrVal){ m_attTTFPrime_startCoefs.set(arrVal); }  
    public void setSTF_startCoefs(double [] arrVal){ m_attSTF_startCoefs.set(arrVal); }
    public void setSTFPrime_startCoefs(double [] arrVal){ m_attSTFPrime_startCoefs.set(arrVal); }    
}
