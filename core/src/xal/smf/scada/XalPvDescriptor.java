/**
 * XalPvDescriptor.java
 *
 *  Created	: Nov 6, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.smf.scada;


/**
 * Encapsulates the properties of an EPICS process
 * variable, as referenced through the XAL channel
 * lookup mechanism (that is, through the XAL channel
 * handle).
 *
 * @since  Nov 6, 2009
 * @author Christopher K. Allen
 */
public class XalPvDescriptor {

    
    /**
     * Used by the enumerations to indicate they
     * return PV descriptors for their enumeration
     * values.
     *
     * @since  Nov 7, 2009
     * @author Christopher K. Allen
     */
    public interface IPvDescriptor {
        
        /**
         * Return the XAL process variable descriptor
         * for the enumeration constant.
         *
         * @return      XAL process variable descriptor
         * 
         * @since  Nov 7, 2009
         * @author Christopher K. Allen
         */
        public XalPvDescriptor  getPvDescriptor();
    }

    
    
    
    /*
     * Local Attributes
     */
    
    /** XAL channel handle for PV read back */
    private final String        strHandleRb;
    
    /** XAL channel handle for PV set */
    private final String        strHandleSet;
    
    /** Data type of the PV value */
    private final Class<?>      clsType;
    
    /** Is the process variable controllable? */
    private final Boolean       bolControllable;

    
    /*
     * Initialization
     */
    
    /**
     * Create a new <code>XalPvDescriptor</code> object with read back
     * values only.  The read back channel hand the given handles 
     * and the PV value has the given data type.
     *
     * @param clsType           PV value data type
     * @param strHandleRb       read back channel XAL handle
     *
     * @since     Nov 6, 2009
     * @author    Christopher K. Allen
     */
    public XalPvDescriptor(Class<?> clsType, String strHandleRb) 
    {
        this.clsType = clsType;
        this.strHandleRb = strHandleRb;
        this.strHandleSet = null;
        this.bolControllable = false;
    }
    
    /**
     * Create a new <code>XalPvDescriptor</code> object that is
     * controllable with the given channel handles and data type.
     *
     * @param clsType           PV value data type
     * @param strHandleRb       read back channel XAL handle
     * @param strHandleSet      set channel XAL handle
     *
     * @since     Nov 6, 2009
     * @author    Christopher K. Allen
     */
    public XalPvDescriptor(Class<?> clsType, 
                    String strHandleRb, 
                    String strHandleSet
                    ) 
    {
        this.clsType = clsType;
        this.strHandleRb = strHandleRb;
        this.strHandleSet = strHandleSet;
        
        if ( strHandleSet != null && !strHandleSet.isEmpty() ) {
            this.bolControllable = true;
        } else {
            this.bolControllable = false;
        }
    }
    
    /**
     * Copy constructor.  Creats a new a deep copy of
     * the given PV Descriptor.
     * 
     * <p>
     * <b>NOTE:</b> Added to implement the functionality of PvDescriptor in XAL.<br>
     * &middot; Jonathan M. Freed <br>
     * &middot; Jul 15, 2014
     * </p>
     * 
     * @param pvd   PV descriptor to be copied
     *
     * @author  Christopher K. Allen
     * @since   Mar 10, 2011
     */
    public XalPvDescriptor(XalPvDescriptor pvd) {
        this.clsType = pvd.clsType;
        this.strHandleRb = pvd.strHandleRb;
        this.strHandleSet = pvd.strHandleSet;
        this.bolControllable = pvd.bolControllable;
    }
    
    
    /*
     * Attributes
     */
    
    /**
     * Return the channel handle for the PV corresponding
     * to the current enumeration constant.
     *
     * @return      XAL channel handle for read back signal
     *  
     * @since       Nov 6, 2009
     * @author  Christopher K. Allen
     */
    public String getRbHandle() {
        return this.strHandleRb;
    };

    /**
     * Returns the channel handle of the channel
     * used to set the PV.
     * 
     * @return      PV set XAL channel handle
     *
     * @since   Nov 6, 2009
     * @author  Christopher K. Allen
     */
    public String getSetHandle() {
        return this.strHandleSet;
    }

    /**
     * Return the data type of Process Variable associate
     * with this handle.
     *
     * @return  return the data type of the PV value
     *  
     * @since       Nov 6, 2009
     * @author  Christopher K. Allen
     */
    public Class<?> getPvType() {
        return this.clsType;
    }
    
    /**
     * Indicates whether or not the process variable
     * can be set.
     *
     * @return  <code>true</code> if the PV is controllable,
     *          <code>false</code> if it can only be read
     * 
     * @since  Nov 6, 2009
     * @author Christopher K. Allen
     */
    public boolean      isControllable()        {
        return this.bolControllable;
    }
    
    
    /*
     * Object Overrides
     */

    /**
     * Returns a deep copy of this object, as deep as you can
     * get anyway.
     * 
     * <p>
     * <b>NOTE:</b> Added to implement the functionality of PvDescriptor in XAL.<br>
     * &middot; Jonathan M. Freed <br>
     * &middot; Jul 15, 2014
     * </p>
     * 
     * @since Apr 19, 2012
     * @see java.lang.Object#clone()
     */
    @Override
    protected Object clone() throws CloneNotSupportedException {
        XalPvDescriptor    pvdCopy = new XalPvDescriptor(this.clsType, this.strHandleRb, this.strHandleSet);
        
        return pvdCopy;
    }
    
    /**
     * Write out the readback and set signal names for this PV descriptor.
     *
     * <p>
     * <b>NOTE:</b> Added to implement the functionality of PvDescriptor in XAL.<br>
     * &middot; Jonathan M. Freed <br>
     * &middot; Jul 15, 2014
     * </p>
     *
     * @see java.lang.Object#toString()
     *
     * @author Christopher K. Allen
     * @since  Apr 29, 2014
     */
    @Override
    public String   toString() {
        String strBuf = "PV Descriptor RB = " + this.strHandleRb + ", SET = " + this.strHandleSet + ", TYPE = " + this.clsType;
        
        return strBuf;
    
    }

}
