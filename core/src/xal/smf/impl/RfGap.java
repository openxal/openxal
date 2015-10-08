package xal.smf.impl;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.smf.AcceleratorNode;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.RfCavityBucket;
import xal.smf.attr.RfGapBucket;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.math.poly.UnivariateRealPolynomial;


/**
 * The implementation of the RF gap element.
 *
 * The RfGap class is meant to be used in connection with a set of 
 * related RF gaps, such as the gaps in a DTL Tank, which are all
 * part of a single resonant cavity
 * controlled by a single klystron. Each gap may have a fixed scale
 * factor for both the field and phase, relative to a nominal 
 * field and phase. 
 * @author  J. Galambos
 */

public class RfGap extends AcceleratorNode {
	/** accessible properties */
	public enum Property { ETL, PHASE, FREQUENCY, FIELD }


    /*
     *  Constants
     */
    
    public static final String      s_strType = "RG";
  

    static {
        registerType();
    }

    
    /*
     * Register type for qualification
     */
    private static void registerType() {
		ElementTypeManager.defaultManager().registerTypes( RfGap.class, s_strType, "rfgap" );
    }
    

    /*
     *  Local Attributes
     */

    /** The rf  gap bucket containing the length, ampFactor, phaseFactor and TTF*/
    
    protected RfGapBucket           m_bucRfGap;           // RfGap parameters

    /**  The RF Field in the gap (kV/m) */
    private double ampAvg;

    /**  The RF phase in the gap (kV/m) */
    private double phaseAvg;
    
    /** a flag indicating whether this gap is the first gap in a cavity string */
    private boolean firstGap = false;
    
    /** Override to provide type signature */
    public String getType()         { return s_strType; };


	/** Primary Constructor */
	public RfGap( final String strId, final ChannelFactory channelFactory ) {
		super( strId, channelFactory );
		setRfGap(new RfGapBucket());
	}


	/** Constructor */
    public RfGap( final String strId ) {
        this( strId, null );
    }

	
    /*
     *  Attributes
     */
  
    public RfGapBucket getRfGap() { 
        return m_bucRfGap; 
    }
    
    
    public void setRfGap(RfGapBucket buc) { 
        m_bucRfGap = buc; 
        super.addBucket(buc); 
    }
    
    
    /** Override AcceleratorNode implementation to check for a RfGapBucket */
    public void addBucket(AttributeBucket buc)  {
        if (buc.getClass().equals( RfGapBucket.class )) 
            setRfGap((RfGapBucket)buc);

        super.addBucket(buc);
    }
    
	
    // public process variable accessors ---------------------------


	/** Get the design value for the specified property */
	public double getDesignPropertyValue( final String propertyName ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case ETL:
					return getGapDfltE0TL();
				case PHASE:
					return getGapDfltPhase();
				case FREQUENCY:
					return getGapDfltFrequency();
				case FIELD:
					return getGapDfltAmp();
				default:
					throw new IllegalArgumentException( "Unsupported RfGap design value property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getDesignPropertyValue( propertyName );
		}
	}


	/** Get the live property value for the corresponding array of channel values in the order given by getLivePropertyChannels() */
	public double getLivePropertyValue( final String propertyName, final double[] channelValues ) {
        final RfCavity cavity = (RfCavity)this.getParent();

		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case ETL:
					final double gapField = toGapAmpFromCavityAmp( cavity.getLivePropertyValue( RfCavity.Property.AMPLITUDE.toString(), channelValues ) );
					return toE0TLFromGapField( gapField );
				case FIELD:
					return toGapAmpFromCavityAmp( cavity.getLivePropertyValue( RfCavity.Property.AMPLITUDE.toString(), channelValues ) );
				case PHASE:
					return toGapPhaseFromCavityPhase( cavity.getLivePropertyValue( RfCavity.Property.PHASE.toString(), channelValues ) );
				case FREQUENCY:
					return getGapDfltFrequency();
				default:
					throw new IllegalArgumentException( "Unsupported RfGap live value property: " + propertyName );
			}
		}
		catch ( IllegalArgumentException exception ) {
			return super.getLivePropertyValue( propertyName, channelValues );
		}
	}


	/** Get the array of channels for the specified property */
	public Channel[] getLivePropertyChannels( final String propertyName ) {
        final RfCavity cavity = (RfCavity)this.getParent();

		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case ETL: case FIELD:
					return cavity.getLivePropertyChannels( RfCavity.Property.AMPLITUDE.name() );
				case PHASE:
					return cavity.getLivePropertyChannels( RfCavity.Property.PHASE.name() );
				case FREQUENCY:
					return new Channel[0];
				default:
					throw new IllegalArgumentException( "Unsupported RfGap live channels property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyChannels( propertyName );
		}
	}

	
    /** return the RF amplitude in the gap (kV/m). Note, this method should probably be modified */
    public double getGapAmpAvg() throws ConnectionException, GetException {
        final RfCavity rfCav = (RfCavity) this.getParent();
		return toGapAmpFromCavityAmp( rfCav.getCavAmpAvg() );
    }
    
	
    /** return the RF amplitude in the gap (kV/m) */
    public double getGapDfltAmp() {
        final RfCavity rfCav = (RfCavity) this.getParent();
		final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return toGapAmpFromCavityAmp( rfCavBuc.getAmplitude() );	
    }
    
	
    /**  
     * This includes the calibration offset factor if it has been set
	 * @return the RF phase in the gap  (deg).
	 */
    public double getGapPhaseAvg() throws ConnectionException, GetException {
        final RfCavity rfCav = (RfCavity) this.getParent();
		return toGapPhaseFromCavityPhase( rfCav.getCavPhaseAvg() );
    }

	
    /** 
     * This is the product of the field * gap length * TTF
	 * @return the E0TL product (kV)
	 */
    public double getGapE0TL() throws ConnectionException, GetException {
		return toE0TLFromGapField( getGapAmpAvg() );
    }
    
	
    /**
     * This is the product of the field * gap length * TTF
	 * @return the E0TL product (kV)
	 */
    public double getGapDfltE0TL() {
		return toE0TLFromGapField( getGapDfltAmp() );
    }
    
	
    /** return the RF phase in the gap  (deg) */
    public double getGapDfltPhase() {
        final RfCavity rfCav = (RfCavity) this.getParent();
		final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return  toGapPhaseFromCavityPhase( rfCavBuc.getPhase() );
    }
    
	
    /** return the RF fundamental frequency */
    public double getGapDfltFrequency() {
        final RfCavity rfCav = (RfCavity) this.getParent();
		final RfCavityBucket rfCavBuc = rfCav.getRfField();
        return rfCavBuc.getFrequency();
    }
	
	
	/**
	 * Convert RF cavity amplitude to get the RF gap's amplitude.
	 * @param cavityAmp the RF cavity's amplitude
	 * @return this RF gap's amplitude
	 */
	public double toGapAmpFromCavityAmp( final double cavityAmp ) {
		return cavityAmp * m_bucRfGap.getAmpFactor();	
	}
	
	
	/**
	 * Convert RF cavity phase to get the RF gap's phase.
	 * @param cavityPhase the RF cavity's phase
	 * @return this RF gap's phase
	 */
	public double toGapPhaseFromCavityPhase( final double cavityPhase ) {
		return cavityPhase + m_bucRfGap.getPhaseFactor();
	}
	
	
	/**
	 * Convert RF gap field, E0, to E0TL.  This is the product of the field * gap length * TTF.
	 * @param field the RF field in KV/m
	 * @return the E0TL product (kV)
	 */
	public double toE0TLFromGapField( final double field ) {
		return field * m_bucRfGap.getLength() * m_bucRfGap.getTTF();
	}
	
    
    /** 
     * return Rf Gap Length
     */
    public double getGapLength() {
        return m_bucRfGap.getLength() ;
    }

	
    /** return TTF */
    public double getGapTTF() {
        return m_bucRfGap.getTTF();
    }

	
    /** 
     *  Set the RF amplitude in the  (kV/m) 
     *  should be done by the parent cavity (e.g. DTL tank)
     *  <br>
     *  <br>
     *  <em>Currently this method does nothing!</em>  
     *  
     *     
     * @param cavAmp The amplitude  of the first gap (kV/m)
     */
    public void setGapAmp(double cavAmp){ 
    	//       	ampAvg = cavAmp *  m_bucRfGap.getAmpFactor();
    }

    /** Set the  RF phase in the gap  (deg) 
     *   should be done by the parent cavity (e.g. DTL tank) 
    * @param cavPhase The phase of the first gap (deg)
    */              
    public void setGapPhase(double cavPhase){ 
       	phaseAvg = cavPhase + m_bucRfGap.getPhaseFactor();;
    }
 
 // the RfGapDataSource interface methods:

   /** return a polynomial fit of the transit time factor as a function of beta */  
    public UnivariateRealPolynomial getTTFFit() { 
        RfCavity rfCav = (RfCavity) this.getParent();
	if(isEndCell()) {
		return rfCav.getTTFFitEnd();
		//return rfCav.getTTFFit();
	}
	else
		return rfCav.getTTFFit();
    }
   /** return a polynomial fit of the TTF-prime factor as a function of beta */  
    public UnivariateRealPolynomial getTTFPrimeFit() { 
        RfCavity rfCav = (RfCavity) this.getParent();
	if (isEndCell())
		return rfCav.getTTFPrimeFitEnd();
		//return rfCav.getTTFPrimeFit();
	else
		return rfCav.getTTFPrimeFit();
    } 
 
  /** return a polynomial fit of the S factor as a function of beta */  
    public UnivariateRealPolynomial getSFit() { 
        RfCavity rfCav = (RfCavity) this.getParent();
	if (isEndCell())
		return rfCav.getSTFFitEnd();
		//return rfCav.getSTFFit();
	else
		return rfCav.getSTFFit();
    }
   /** return a polynomial fit of the S-prime factor as a function of beta */  
    public UnivariateRealPolynomial getSPrimeFit() { 
        RfCavity rfCav = (RfCavity) this.getParent();
	if (isEndCell()) 
		return rfCav.getSTFPrimeFitEnd();
		//return rfCav.getSTFPrimeFit();
	
	else
		return rfCav.getSTFPrimeFit();
    }    
    
 
	/** returns 0 if the gap is part of a 0 mode cavity structure (e.g. DTL)
	* returns 1 if the gap is part of a pi mode cavity (e.g. CCL, Superconducting)
	*/
	
	public double getStructureMode() {
		RfCavity rfCav = (RfCavity) this.getParent();
		return rfCav.getStructureMode();
	}    
    
    /** 
     *  these may be different, for example, for a DTL cavity 
     * @return the offset of the gap center from the cell center (m) 
     */
    public double getGapOffset() { 
	    return  m_bucRfGap.getGapOffset();
    }
    
    /** sets the flag indicating whether this is the first gap in a cavity */
    public void setFirstGap(boolean tf) { firstGap = tf;}
    
    /** returns whether this is the first gap of a cavity string */
    public boolean isFirstGap() {return firstGap;}
    
    /** returns whether this is the first gap of a cavity string */
    public boolean isEndCell() {
	    if (m_bucRfGap.getEndCell() == 1)
		    return true;
	    else
		    return false;
    }  
    /**
     *  Computes and returns the design value of the energy gain for this gap.  
     *  The energy gain is given by the Panofsky equation dW=q E0 T L cos(phi).
     *
     *  @return     design energy gain (eV)
     *  
     *  Added 10/17/02  CKA
     */
    public double getDesignEnergyGain() {
        double  ETL = this.getGapDfltE0TL();
        double  phi = this.getGapDfltPhase();
        
        return ETL*Math.cos(phi);
    };
    
 
}
