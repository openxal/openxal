package xal.smf.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.RfCavityBucket;
import xal.smf.impl.qualify.ElementTypeManager;
import xal.tools.data.DataAdaptor;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;



/**
 * The implementation of the RF Cavity element.
 * The Rf Cavity is the device that is directly connected to a klystron.
 * There are internal RF gap(s) within this cavity, which are controlled
 * by the cavity. The RfGaps are a separate class of type AcceleratorNode. 
 * The beam dynamics are done in the RfGap class.
 * Note: the "knob" connections are to the klystron.
 * The 
 *
 * @author  Nikolay Malitsky, Christopher K. Allen
 */

public class RfCavity extends AcceleratorSeq {
	/** accessible properties */
	public enum Property { AMPLITUDE, PHASE }


    /*
     * Constant PV signal names
     */
    static public final String CAV_AMP_SET_HANDLE = "cavAmpSet";
    static public final String CAV_PHASE_SET_HANDLE = "cavPhaseSet";
    static public final String CAV_AMP_AVG_HANDLE = "cavAmpAvg";
    static public final String CAV_PHASE_AVG_HANDLE = "cavPhaseAvg";
    static public final String DELTA_TRF_START_HANDLE = "deltaTRFStart";
    static public final String DELTA_TRF_END_HANDLE = "deltaTRFEnd";
    static public final String T_DELAY_HANDLE = "tDelay";
	static public final String BLANK_BEAM_HANDLE = "blankBeam";

    /** accelerator node type */
    public static final String      s_strType = "RF";
    
    /** RF Cavity parameters */
    protected RfCavityBucket           m_bucRfCavity;           // RfCavityStruct parameters
	
    /**<p> 
     * container of the enclosed RfGap(s) in this cavity sorted by position 
     * </p>
     * <h3>NOTE:</h3>
     * <p>
     * An <code>RfCavityStruct</code> is an <code>AcceleratorSeq</code> which is
     * already an ordered list of <code>AcceleratorNode</code>s.  This
     * attribute and any reliance on it seems dangerously redundant.
     * <h4>NOTE:</h4>
     * This appears to be used to process the gaps and only the gaps within this
     * cavity structure. 
     * </p>
     */
    protected List<RfGap> _gaps;  // rf gaps within this multi-gap device
	
	
	// static initializer
    static {
        registerType();
    }


	/** Primary Constructor */
	public RfCavity( final String strId, final ChannelFactory channelFactory, final int intReserve ) {
		super( strId, channelFactory, intReserve );
		setRfField( new RfCavityBucket() );
	}


	/** Constructor */
	public RfCavity( final String strId, final ChannelFactory channelFactory ) {
		this( strId, channelFactory, 0 );
	}


    /** Constructor */
    public RfCavity( final String strId ) {
        this( strId, 0 );
    }
    
    
    /** Constructor */    
    public RfCavity( final String strId, final int intReserve ) {
        this( strId, null, intReserve );
    }
	
    
    /** Register accelerator node type for qualification */
    private static void registerType() {
        ElementTypeManager typeManager = ElementTypeManager.defaultManager();
        typeManager.registerType( RfCavity.class, s_strType );
        typeManager.registerType( RfCavity.class, "rfcavity" );
    }
    
    
    /** Override to provide type signature */
    public String getType()         { return s_strType; };
    

    /** Collect all of the enclosed rf gaps for convenience */
    public void update( final DataAdaptor adaptor ) {
        super.update( adaptor );
        final List<AcceleratorNode> nodes = getNodesOfType( RfGap.s_strType, true );
        _gaps = new ArrayList<RfGap>( nodes.size() );
		for ( final AcceleratorNode node : nodes ) {
			_gaps.add( (RfGap)node );
		}
		processGaps();
    }
    
	
    /** loop through the gaps in this cavity to initialize some stuff */
    private void processGaps() {
		Iterator<RfGap> gapIter = _gaps.iterator();
		int index = 0;
		double position = 0.;
		double oldPosition = 0.;
		double oldCellLength = 0.;
		double oldGapOffset = 0.;
		// presently the gappOffset is commented out.
		// to do it right we need the gapOffset of the 1st
		// gap in a cavity to come from an external source.
		while ( gapIter.hasNext() ) {
            RfGap gap = gapIter.next();
			
			if( index == 0) {
				gap.setFirstGap(true);	   
			}
			else{
				gap.setFirstGap(false);	   
			}
			position = gap.getPosition();
			//if(getId().startsWith("DTL")) 
			//    gap.m_bucRfGap.setGapOffset(0.);
			index += 1;
		}
    }
	
	
    /** returns the bucket for the RfField of this cavity */
    public RfCavityBucket getRfField() { 
        return m_bucRfCavity; 
    }
    
    /** sets the bucket for the RfField of this cavity */    
    public void setRfField(RfCavityBucket buc) { 
        m_bucRfCavity = buc; 
        super.addBucket(buc); 
    }    
    
    /** Override AcceleratorNode implementation to check for a RfCavityStruct Bucket */
    public void addBucket(AttributeBucket buc)  {

        if (buc.getClass().equals( RfCavityBucket.class )) 
            setRfField((RfCavityBucket)buc);

        super.addBucket(buc);
    }


	/** Get the design value for the specified property */
	public double getDesignPropertyValue( final String propertyName ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case AMPLITUDE:
					return getDfltCavAmp();
				case PHASE:
					return getDfltCavPhase();
				default:
					throw new IllegalArgumentException( "Unsupported RfCavity design value property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return getDesignPropertyValue( propertyName );
		}
	}


	/** Get the live property value for the corresponding array of channel values in the order given by getLivePropertyChannels() */
	public double getLivePropertyValue( final String propertyName, final double[] channelValues ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case AMPLITUDE:
					return toCavAmpAvgFromCA( channelValues[0] );
				case PHASE:
					return toCavPhaseAvgFromCA( channelValues[0] );
				default:
					throw new IllegalArgumentException( "Unsupported RfCavity live value property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyValue( propertyName, channelValues );
		}
	}


	/** Get the array of channels for the specified property */
	public Channel[] getLivePropertyChannels( final String propertyName ) {
		try {
			final Property property = Property.valueOf( propertyName );		// throws IllegalArgumentException if no matching property
			switch( property ) {
				case AMPLITUDE:
					return new Channel[] { findChannel( CAV_AMP_AVG_HANDLE ) };
				case PHASE:
					return new Channel[] { findChannel( CAV_PHASE_AVG_HANDLE ) };
				default:
					throw new IllegalArgumentException( "Unsupported RfCavity live channels property: " + propertyName );
			}
		}
		catch( IllegalArgumentException exception ) {
			return super.getLivePropertyChannels( propertyName );
		}
	}


    private Channel cavAmpSetC = null;
    private Channel cavPhaseSetC = null;
    private Channel cavAmpAvgC = null;
    private Channel cavPhaseAvgC = null;
    private Channel deltaTRFStartC = null;
    private Channel deltaTRFEndC = null;
    private Channel tDelayC = null;

    
    /** get the cavity amplitude (kV)   
     * and publish this to all the gaps connected to this cavity
     * note the cavity amp [kV]  = klystron amplitude *  ampFactor
     * where ampFactor is a calibration factor determined experimentally
     */ 
    public double getCavAmpAvg() throws ConnectionException, GetException {
        cavAmpAvgC = this.lazilyGetAndConnect(CAV_AMP_AVG_HANDLE, cavAmpAvgC);
        final double amplitudeAverage = toCavAmpAvgFromCA( cavAmpAvgC.getValDbl() );
        publishAmplitude( amplitudeAverage );
        return amplitudeAverage;
    }
	
	
	/**
	 * Convert the raw channel access value to get the cavity amplitude in kV.
	 * @param rawValue the raw channel value
	 * @return the cavity amplitude in kV
	 */
	public double toCavAmpAvgFromCA( final double rawValue ) {
        return rawValue * m_bucRfCavity.getAmpFactor(); 
	}
	
	
	/**
	 * Convert the cavity amplitude to channel access.
	 * @param value the cavity amplitude
	 * @return the channel access value
	 */
	public double toCAFromCavAmpAvg( final double value ) {
        return value / m_bucRfCavity.getAmpFactor(); 
	}
	
    
    /** Get the cavity phase relative to the beam (deg)    
     *  and publish it to all the rf gaps associated with this cavity
     * note the cavity phase = klystron phase + phaseOffset
     * where phaseOffset is a calibration factor determined experimentally
     */
    public double getCavPhaseAvg() throws ConnectionException, GetException {
        cavPhaseAvgC = this.lazilyGetAndConnect(CAV_PHASE_AVG_HANDLE, cavPhaseAvgC);
        final double phaseAverage = toCavPhaseAvgFromCA( cavPhaseAvgC.getValDbl() );
        publishPhase( phaseAverage );
        return phaseAverage;
    }
	
	
	/**
	 * Convert the raw channel access value to get the cavity phase in degrees.
	 * @param rawValue the raw channel value
	 * @return the cavity phase in degrees
	 */
	public double toCavPhaseAvgFromCA( final double rawValue ) {
		return rawValue + m_bucRfCavity.getPhaseOffset();
	}
	
	
	/**
	 * Convert the cavity phase to channel access.
	 * @param value the cavity phase
	 * @return the channel access value
	 */
	public double toCAFromCavPhaseAvg( final double value ) {
		return value - m_bucRfCavity.getPhaseOffset();
	}
	

    /**
     * @return default (design) cavity amplitude
     */
    public double getDfltCavAmp() {
        return getRfField().getAmplitude();
    }
    
    /**
     * @return default (design) cavity phase
     */
    public double getDfltCavPhase() {
        return getRfField().getPhase();
    }
    
    public void setDfltCavAmp(double value) {
    	getRfField().setAmplitude(value);
    }
    
    public void setDfltCavPhase(double value) {
    	getRfField().setPhase(value);
    }
	
    /**
     * @return default (design) average cavity phase 
     * (averaged over all RF gaps in the cavity)
     */
    public double getDfltAvgCavPhase() {
		return toAvgCavPhaseFromCavPhase( getDfltCavPhase() );
    }
	
    
    /**
     * CKA - Never used
     * 
     * @return default (design) average cavity TTF 
     * (averaged over all RF gaps in the cavity)
     */
    public double getDfltAvgCavTTF() {
	    double sum = 0.;
		for ( final RfGap gap : _gaps ) {
		    sum += gap.getGapTTF() * gap.m_bucRfGap.getAmpFactor() * Math.cos(gap.getGapDfltPhase() * Math.PI/180.);
	    }
	    return sum/((double) _gaps.size() * Math.cos(getDfltAvgCavPhase()* Math.PI/180.));
    }
    
	
    /** get the length of the active RF accelerating structure in this cavity (m) */
    public double getRFLength() {
	    double sum = 0.;
		for ( final RfGap gap : _gaps ) {
		    sum += gap.getGapLength();
	    }
	    return sum;
    }
	
	
	/**
	 * Convert the cavity phase (phase at entrance to cavity) to average cavity phase by averaging the phase over the gaps.
	 * @param cavityPhase the phase at the start of the cavity.
	 * @return the average phase of the cavity
	 */
	public double toAvgCavPhaseFromCavPhase( final double cavityPhase ) {
		double sum = 0.0;
		for ( final RfGap gap : _gaps ) {
			sum += gap.toGapPhaseFromCavityPhase( cavityPhase );
		}
		
		return sum / _gaps.size();
	}
	
	
	/**
	 * Calculate the average phase of the gaps at the center of the cavity from the phase at the entrance to the cavity.
	 * @param cavityPhase the phase at the start of the cavity.
	 * @return the average phase of the cavity
	 */
	public double toCenterAvgCavPhaseFromCavPhase( final double cavityPhase ) {
		final int gapCount = _gaps.size();
		if ( gapCount < 1 )  return cavityPhase;
		
		// if the gap count is even then average over the two center gaps;  if the gap 
		final int startIndex = ( gapCount - 1 ) / 2;
		final int endIndex = 1 + gapCount / 2;
		
		final List<RfGap> gaps = new ArrayList<RfGap>( endIndex - startIndex );
		
		double phaseSum = 0.0;
		double totalLength = 0.0;
		for ( int index = startIndex ; index < endIndex ; index++ ) {
			final RfGap gap = _gaps.get( index );
			final double gapLength = gap.getGapLength();
			phaseSum += gap.toGapPhaseFromCavityPhase( cavityPhase ) * gapLength;
			totalLength += gapLength;
		}
		
		return phaseSum / totalLength;
	}
	
	
    /** Set the cavity amplitude [kV]    
     * note the cavity amp [kV]  = klystron amp *  ampFactor
     * where ampFactor is a calibration factor determined experimentally
     */     
    public void setCavAmp(double newAmp) throws ConnectionException, PutException {
        cavAmpSetC = this.lazilyGetAndConnect(CAV_AMP_SET_HANDLE, cavAmpSetC);
        cavAmpSetC.putVal( toCAFromCavAmpAvg( newAmp ) );
    }
    
	
    /** Set the cavity phase relative to the beam (deg)    
     * note the cavity phase = klystron phase + phaseOffset
     * where phaseOffset is a calibration factor determined experimentally
     */
    
    public void setCavPhase(double newPhase) throws ConnectionException, PutException {
        cavPhaseSetC = this.lazilyGetAndConnect(CAV_PHASE_SET_HANDLE, cavPhaseSetC);
        cavPhaseSetC.putVal( toCAFromCavPhaseAvg( newPhase ) );
    }
    
    /** return the present live set point for the amplitude */
    public double getCavAmpSetPoint() throws ConnectionException, GetException {
	cavAmpSetC = this.lazilyGetAndConnect(CAV_AMP_SET_HANDLE, cavAmpSetC);
        return cavAmpSetC.getValDbl() * m_bucRfCavity.getAmpFactor();
    }
    
    /** return the present live set point for the phase */
    public double getCavPhaseSetPoint() throws ConnectionException, GetException {
	cavPhaseSetC = this.lazilyGetAndConnect(CAV_PHASE_SET_HANDLE, cavPhaseSetC);
        return cavPhaseSetC.getValDbl();
    }

    
    /*
     * set the amplitude of all of the gaps
     * @param The amplitude  of the first gap (kV/m)
     */
    protected void publishAmplitude(double newCavAmp) {
		for ( final RfGap gap : _gaps ) {
            gap.setGapAmp( newCavAmp );
        }
    }

    
    /*
     * set the phases of all of the gaps
     * @param The phase of the first gap (deg)
     */
    public void publishPhase(double newCavPhase) {
		for ( final RfGap gap : _gaps ) {
            gap.setGapPhase( newCavPhase );
        }
    }
	

    /**
     * Determine whether the beam is blanked
     * @return true if the beam is blanked and false if not
     */
    public boolean getBlankBeam() throws ConnectionException, GetException {
        final Channel blankBeamChannel = getAndConnectChannel( BLANK_BEAM_HANDLE );
        return blankBeamChannel != null ? ( blankBeamChannel.getValEnum() == 1 ? true : false ) : false;
    }


    /**
     * Blank the beam
     * @param mode true to blank the beam and false for continuous on
     */
    public void setBlankBeam( final boolean mode ) throws ConnectionException, PutException {
        final Channel blankBeamChannel = getAndConnectChannel( BLANK_BEAM_HANDLE );
        if ( blankBeamChannel != null ) {
            blankBeamChannel.putVal( mode ? 1 : 0 );
        }
        else {
            throw new RuntimeException( "Attempting to blank the beam for " + getId() + " but no channel can be found." );
        }
    }


    /** method to return the gaps associated with this cavity */
    public Collection<RfGap> getGaps() { return _gaps; }
    
	
	/** method to return the gaps associated with this cavity as a List*/
    public List<RfGap> getGapsAsList() { return _gaps; }   
    
	
    /** 
	 * Set the design phase
     * @param phase new design phase (deg)
     */
    public void updateDesignPhase(double phase) {
	    RfCavityBucket rfCavBuc = this.getRfField();
	    rfCavBuc.setPhase(phase);
    }
    
   /** method to set the design amplitude
    * @param amp new design amplitude (kV)
    */
    public void updateDesignAmp(double amp) {
	    RfCavityBucket rfCavBuc = this.getRfField();
	    rfCavBuc.setAmplitude(amp);
    }
    
    /** return a polynomial fit of the transit time factor as a function of beta */  
    public RealUnivariatePolynomial getTTFFit() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getTTFCoefs());
    }

    /** return a polynomial fit of the transit time factor prime as a function of beta */  
    public RealUnivariatePolynomial getTTFPrimeFit() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getTTFPrimeCoefs());
    }   
    
    /** return a polynomial fit of the "S" transit time factor as a function of beta */  
    public RealUnivariatePolynomial getSTFFit() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getSTFCoefs());
    }

    /** return a polynomial fit of the "S" transit time factor prime as a function of beta */  
    public RealUnivariatePolynomial getSTFPrimeFit() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getSTFPrimeCoefs());
    } 
      
   /** return a polynomial fit of the transit time factor for end cells as a function of beta */  
    public RealUnivariatePolynomial getTTFFitEnd() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getTTF_endCoefs());
    }

    /** return a polynomial fit of the transit time factor prime for end cells as a function of beta */  
    public RealUnivariatePolynomial getTTFPrimeFitEnd() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getTTFPrime_endCoefs());
    }   
    
    /** return a polynomial fit of the "S" transit time factor for end cells as a function of beta */  
    public RealUnivariatePolynomial getSTFFitEnd() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getSTF_endCoefs());
    }

    /** return a polynomial fit of the "S" transit time factor prime for end cells as a function of beta */  
    public RealUnivariatePolynomial getSTFPrimeFitEnd() { 
	    RfCavityBucket rfCavBuc = this.getRfField();
	    return new RealUnivariatePolynomial(rfCavBuc.getSTFPrime_endCoefs());
    } 
    
 	/** returns 0 if the gap is part of a 0 mode cavity structure (e.g. DTL)
	* returns 1 if the gap is part of a pi mode cavity (e.g. CCL, Superconducting)
	*/
	
	public double getStructureMode() {
		RfCavityBucket rfCavBuc = this.getRfField();
		return rfCavBuc.getStructureMode();
	}  
	
	/**
	 * Get RF cavity frequency.
	 * 
	 * @return RF cavity frequency
	 */
	public double getCavFreq() {
		return this.getRfField().getFrequency();
	}
	
}
