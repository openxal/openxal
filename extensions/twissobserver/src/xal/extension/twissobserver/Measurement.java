/**
 * MeasurementCurve.java
 *
 * @author Christopher K. Allen
 * @since  Apr 29, 2013
 */
package xal.extension.twissobserver;


/**
 * <p>
 * Data structure containing beam size measurements at a specific beamline
 * location.
 * </p>
 * 
 * @author Eric Dai
 * @author Christopher K. Allen
 * @since June 19, 2012
 *
 */
public class Measurement implements Cloneable {
    
    /** String containing device ID where the RMS beam sizes are located */
    public String strDevId;
    
    
	/** Horizontal RMS beam size at specified element location */
	public Double dblSigHor;
	
	/** Vertical RMS beam size at given element location */
	public Double dblSigVer;
	
	/** The RMS beam size in the longitudinal direction at the given location */
	public Double  dblSigLng;
	
	
	/*
	 * Initialization
	 */
	
	
	/**
	 * Creates a new, empty instance of <code>Measurement</code>.
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 26, 2013
	 */
	public Measurement() {
        this.strDevId  = null;
        this.dblSigHor = 0.0;
        this.dblSigVer = 0.0;
        this.dblSigLng = 0.0;
	}
	
	/**
	 * Creates a new, initialized instance of <code>Measurement</code>.
	 *
	 * @param strDevId     device ID of measurement data
	 * @param dblSigHor    horizontal RMS beam size
	 * @param dblSigVer    vertical RMS beam size
	 * @param dblSigLng    longitudinal RMS beam size
	 *
	 * @author Christopher K. Allen
	 * @since  Apr 26, 2013
	 */
	public Measurement(String strDevId, double dblSigHor, double dblSigVer, double dblSigLng) {
	    this.strDevId  = strDevId;
	    this.dblSigHor = dblSigHor;
	    this.dblSigVer = dblSigVer;
	    this.dblSigLng = dblSigLng;
	}
	
	
	/*
	 * Object Overrides
	 */
	
	/**
	 * Returns a string representation of this
	 * measurement value.
	 * 
	 * @since Sep 6, 2012
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String  toString() {
	    String strBuf = "(DevId="  + this.strDevId +
	                    ",sx=" + this.dblSigHor + 
	                    ",sy=" + this.dblSigVer + 
	                    ",sz=" + this.dblSigLng +
	                    ")";
	    
	    return strBuf;
	}

    /** 
     * Creates a deep copy of this <code>Measurement</code> and returns it
     * according to the Java <code>Cloneable</code> interface convention.
     *
     * @throws CloneNotSupportedException   if the object's class does not support the <code>Cloneable</code> 
     *                                      interface. Subclasses that override the clone method can also throw 
     *                                      this exception to indicate that an instance cannot be cloned.     
     *                                     
     * @ see java.lang.Object#clone()
     *
     * @author Christopher K. Allen
     * @since  Apr 26, 2013
     */
    @Override
    public Measurement clone() {
        
        Measurement mstClone = new Measurement();
        
        mstClone.strDevId  = this.strDevId;
        mstClone.dblSigHor = this.dblSigHor;
        mstClone.dblSigVer = this.dblSigVer;
        mstClone.dblSigLng = this.dblSigLng;
        
        return mstClone;
    }
}
