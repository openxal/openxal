package xal.smf.impl;

import xal.smf.*;
import xal.smf.attr.*;
import xal.smf.impl.qualify.*;
import xal.ca.*;

/** 
 * The abstract Magnet Class element. This class contains
 * elements common to all magnets in an accelerator.
 * 
 * @author  J. Galambos
 * 
 */

public abstract class Magnet extends AcceleratorNode implements MagnetType {
	// static initialization
    static {
        registerType();
    }
    
    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( Magnet.class, "magnet" );
    }
    
 
    /**
     * The effective magnetic length (m)
     */
    public double leff;
 
    /**
     * The container for the magnet information
     */
    protected MagnetBucket       m_bucMagnet; 


	/** Primary Constructor */
	public Magnet( final String strId, final ChannelFactory channelFactory )     {
		super( strId, channelFactory );
		setMagBucket( new MagnetBucket() );
	}


	/** Constructor */
    public Magnet( final String strId )     {
        this( strId, ChannelFactory.defaultFactory() );
    }

    
    /**
     *  
     * @return    the attribute bucket containing the machine multipole fields
     */

    public MagnetBucket  getMagBucket()   { return m_bucMagnet; };

 
   /**
     *  
     * Set the attribute bucket containing the machine magnet info
     */

    public void setMagBucket(MagnetBucket buc) 
        { m_bucMagnet = buc; super.addBucket(buc); };
    
    
    /**
     *
     * Override AcceleratorNode implementation to check for a MultipoleBucket
     */

    public void addBucket(AttributeBucket buc)  {

        if (buc.getClass().equals(MagnetBucket.class))
              setMagBucket((MagnetBucket)buc);

        super.addBucket(buc);
    };    


    /**
     * Override the inherited method to be true since all magnets are of the 
     * magnet type.
     * @return true
     */
    public boolean isMagnet() {
        return true;
    }
    
    
    /**
     * Test if the magnet is of the specified pole type.  MagnetType defines
     * the list of accepted pole types.
     * @param compPole Comparison pole which should be one of MagnetType.poles
     * @return true if the magnet is of the specified pole.
     */
    public boolean isPole(String compPole) {
        return false;
    }
    
    
    /**
     * Get the orientation of the magnet as defined by MagnetType.
     * @return One of HORIZONTAL, VERTICAL or NO_ORIENTATION
     */
    public int getOrientation() {
        return NO_ORIENTATION;
    }
    
    
    /**
     * Determine whether this magnet is oriented horizontally.
     * @return true if this magnet is oriented horizontally; false otherwise.
     */
    final public boolean isHorizontal() {
        return getOrientation() == HORIZONTAL;
    }
    
    
    /**
     * Determine whether this magnet is oriented vertically.
     * @return true if this magnet is oriented vertically; false otherwise.
     */
    final public boolean isVertical() {
        return getOrientation() == VERTICAL;
    }
    
    
    /**
     * Determine whether this magnet is a skew magnet.
     * @return true if the magnet is skew and false otherwise.
     */
    public boolean isSkew() {
        return false;
    }
    
    
    /**
     * Get whether this magnet is a permanent magnet or an electromagnet.
     * @return true if the magnet is permanent and false otherwise.
     */
    public boolean isPermanent() {
        return false;
    }
    
   
    /**
     * Determine whether this magnet is a corrector.
     * @return true if this magnet is a corrector.
     */
    public boolean isCorrector() {
        return false;
    }
    
    
    /**
     * get the design field for the magnet (T for dipole, T/m for quad, etc.)
     */

    public double getDesignField() {
        return m_bucMagnet.getDfltField() ;
    }

    /** 
     * get the effective magnetic length (m)
     */

    public double getEffLength() {
        return m_bucMagnet.getEffLength() ;
    } 
    
    /**
     * get the default magnetic field
     */       
    public double getDfltField() {
        return m_bucMagnet.getDfltField() ;
    }
    
    /**
     * get magnet polarity
     */
    public double   getPolarity() {
        try{
            return m_bucMagnet.getPolarity();
        }  
        catch (Exception e) {	    
            System.out.println(" Polarity not set on " + this.getId() 
                           + ", for stability sake, using + field");
            return 1;
        }
    }
    
    /**
     * get Normal fields
     */
    public double[] getNormField() {
        return m_bucMagnet.getNormField() ;
    }
    
    /**
     * get tangential fields
     */
    public double[] getTangField() {
        return m_bucMagnet.getTangField() ;
    }
    
    /**
     * Method setDfltField
     * @param field the default magnetic field to be changed to.
     */
    public void setDfltField(double field) {
    	m_bucMagnet.setDfltField(field);
    }
    
}













