package xal.smf.impl;


import xal.smf.impl.qualify.ElementTypeManager;



/** 
 * The implementation of the DTLTank sequence, which derives from the
 * AcceleratorSeq class.
 * This is a container to be used in handling Drift Tube Linacs
 * These devices have RfGaps in them, which are controlled by a single
 * RfCavity. That is, the RfCavity contains the hooks to the klystron
 * signals, which controll all of the RfGaps together.
 * As the DTLTank is also a sequence, it is possible for it to contain
 * other types of nodes, such as quads and BPMs.
 * 
 * @author J. Galambos
 */

public class DTLTank extends RfCavity {
  
  
  
    /*
     *  Constants
     */
    public static final String    s_strType = "DTLTank";



    /*
     * Static setup
     */
    
    
    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType(DTLTank.class, s_strType);
    }


    /*
     * Local Attributes
     */
    
    
    
    /**
     * I just added this comment - didn't do any work.
     * 
     * @param strId     identifier string of the DTL
     *
     * @author  Christopher K. Allen
     * @since   May 3, 2011
     */
    public DTLTank(String strId) {
        this(strId, 0);
    }
   
    /**
     * I just added this comment - didn't do any work.
     * 
     * @param strId     identifier string of the DTL
     * @param intReserve optional parameter for specifying memory to reserve for the DTL cells
     *                  if known.
     *
     * @author  Christopher K. Allen
     * @since   May 3, 2011
     */
    public DTLTank(String strId, int intReserve) {
        super(strId, intReserve);
    }
    
    
    
    
    /** Support the node type */
    public String getType() { return s_strType; };


    /** The RFCavity for this DTLTank 
     * This is the connection to the klystron
    */
    //protected RfCavity mainRfCavity;
   
  
}

