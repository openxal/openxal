/**
 * IdealRfCavity.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 3, 2014
 */
package xal.model.elem;

/**
 * <p>
 * This class represents a general RF cavity being an composition of
 * RF gaps and cavity drifts.  The types and parameters of the internal
 * elements define the operation (and configuration) of the cavity.
 * </p>
 * <p>
 * The propagation is done via the base class <code>ElementSeq</code> which
 * just runs through the child elements propagating (or back propagating)
 * in order.  Thus, currently at least, this element is really just a 
 * container of elements.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since  Dec 3, 2014
 */
public class IdealRfCavity extends ElementSeq {

    
    
    /*
     * Local Attributes
     */
    
    /** The frequency of the enclosing cavity */
    private double dblFreq;
    
    /** The mode constant (1/2 the mode number) of the cavity which we are exciting */
    private double  dblModeConst;
    
    
    /*
     * Initialization
     */
    
    /**
     * Constructor for IdealRfCavity.
     *
     * @param strType
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity(String strType) {
        super(strType);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor for IdealRfCavity.
     *
     * @param strType
     * @param strId
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity(String strType, String strId) {
        super(strType, strId);
        // TODO Auto-generated constructor stub
    }

    /**
     * Constructor for IdealRfCavity.
     *
     * @param strType
     * @param strId
     * @param szReserve
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity(String strType, String strId, int szReserve) {
        super(strType, strId, szReserve);
        // TODO Auto-generated constructor stub
    }

    /**
     * Set the frequency of the RF cavity containing this drift space.
     * 
     * @param dblFreq   fundamental RF frequency of the enclosing RF cavity
     */
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }

    /**
     * <p>
     * Set the operating mode constant &lambda; for the RF cavity design. The constant
     * is half of the mode number <i>q</i>.  Specifically,
     * <br/>
     * <br/>
     *  &nbsp; &nbsp; &lambda; = 0 &nbsp; (<i>q</i>=0) &rArr;  0 mode cavity structure (e.g. DTL)
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; &lambda; = 1/2 (<i>q</i>=1) &rArr; &pi;/2 mode structure (bi-periodic structures, e.g., SideCC)
     *  <br/>
     *  <br/> 
     *  &nbsp; &nbsp; &lambda; = 1 &nbsp; (<i>q</i>=2) &rArr; &pi;-mode cavity (e.g. CCL, super-conducting)
     * </p>
     * 
     * @param dblModeConst  the new mode constant &lambda; for the cavity drift
     */
    public void setCavityModeConstant(double dblModeConst) {
        this.dblModeConst = dblModeConst;
    }

    
    /*
     * Attribute Query
     */
    
    /**
     * Get the operating frequency of the RF cavity.
     * 
     * @return  the fundamental mode frequency <i>f</i><sub>0</sub> of the enclosing RF cavity 
     */
    public double getFrequency() {
        return dblFreq;
    }

    /**
     * <p>
     * Get the operating mode constant &lambda; for the RF cavity design. The constant
     * is half of the mode number <i>q</i>.  Specifically,
     * <br/>
     * <br/>
     *  &nbsp; &nbsp; &lambda; = 0 &nbsp; (<i>q</i>=0) &rArr;  0 mode cavity structure (e.g. DTL)
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; &lambda; = 1/2 (<i>q</i>=1) &rArr; &pi;/2 mode structure (bi-periodic structures, e.g., SideCC)
     *  <br/>
     *  <br/> 
     *  &nbsp; &nbsp; &lambda; = 1 &nbsp; (<i>q</i>=2) &rArr; &pi;-mode cavity (e.g. CCL, super-conducting)
     * </p>
     * 
     * @return  the operating mode constant &lambda; for the cavity drift
     */
    public double getCavityModeConstant() {
        return dblModeConst;
    }


}
