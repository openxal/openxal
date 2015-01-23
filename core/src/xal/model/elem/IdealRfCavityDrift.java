/**
 * IdealRfCavityDrift.java
 *
 * Author  : Christopher K. Allen
 * Since   : Nov 28, 2014
 */
package xal.model.elem;

import xal.model.IProbe;

/**
 * <p>
 * Represents a drift region between RF cavity accelerating gaps.
 * Extends the <code>IdealDrift</code> class to include a frequency parameter
 * <i>f</i> which is necessary in computing probe phase advance through the drift.
 * </p>
 * I think we are going to include the structure mode number <i>q</i> in here as well.
 * This way the phase advance due to the higher order modes can be accounted for here.
 * </p>
 *
 * @author Christopher K. Allen
 * @since  Nov 28, 2014
 */
public class IdealRfCavityDrift extends IdealDrift {

    
    /*
     *  Global Constants
     */
    
    /** string type identifier for all IdealDrift objects */
    public static final String      STR_TYPEID = "IdealRfCavityDrift";
    
    
    
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
     * Fully initializing constructor for <code>IdealRfCavityDrift</code>.
     * Constructor initializes all defining parameters of this RF cavity
     * drift space.
     *
     * @param strId         string identifier of the drift
     * @param dblLen        length of the RF cavity drift
     * @param dblFreq       RF frequency of the enclosing RF Cavity
     * @param dblModeConst  RF structure constant for the cavity operating mode
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavityDrift(String strId, double dblLen, double dblFreq, double dblModeConst) {
        super(STR_TYPEID, strId, dblLen);
//        super(strId, dblLen);
        
        this.dblFreq = dblFreq;
        this.dblModeConst = dblModeConst;
    }
    
    /**
     * Constructor for IdealRfCavityDrift.
     *
     * @param strId     the string identifier of the drift space
     * @param dblLen    the length of the drift space
     *
     * @author Christopher K. Allen
     * @since  Nov 28, 2014
     */
    public IdealRfCavityDrift(String strId, double dblLen) {
        super(strId, dblLen);
        
        this.dblFreq = 0.0;
        this.dblModeConst = 0.0;
    }

    /**
     * Constructor for IdealRfCavityDrift.
     *
     * @author Christopher K. Allen
     * @since  Nov 28, 2014
     */
    public IdealRfCavityDrift() {
        super();

        this.dblFreq = 0.0;
        this.dblModeConst = 0.0;
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
     * Get the frequency of the RF cavity containing this drift space.
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

    /*
     * Base Class Overrides
     */
    
    /**
     * Computes and returns the phase advance of the probe while drifting through
     * the given segment of this drift.
     *
     * @see xal.model.elem.Element#longitudinalPhaseAdvance(xal.model.IProbe, double)
     *
     * @author Christopher K. Allen
     * @since  Dec 2, 2014
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        double  dt  = super.elapsedTime(probe, dblLen);
        double  f   = this.getFrequency();
        double  w   = 2.0 * Math.PI * f;
        double dphi = w * dt;
        
        return dphi;
    }

    
    /*
     * Object Overrides
     */
    
    /**
     *
     * @see xal.model.elem.Element#toString()
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public String toString() {
        StringBuffer    bufOut = new StringBuffer();
        
        bufOut.append(super.toString());
        
        bufOut.append("  Frequency          : " + this.getFrequency());
        bufOut.append('\n');

        bufOut.append("  Cavity mode const. : " + this.getCavityModeConstant());
        bufOut.append('\n');
        
        return bufOut.toString();
    }
    
    
}
