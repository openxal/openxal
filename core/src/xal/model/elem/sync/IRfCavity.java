/*
 * Created on Mar 17, 2004
 */
package xal.model.elem.sync;

/**
 * Common parameters of RF Cavity structures.
 * 
 * TODO CKA Add PROPERTY_FREQUENCY property to RfCavitySynchronizer??
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 * 
 * @since Mar 17, 2004
 * @version Jan 25, 2015
 */
public interface IRfCavity  {

    /**
     *  Get the RF cavity field amplitude.
     *
     *  @return     cavity amplitude (in <bold>MV/m</bold>).
     */
    public double getCavAmp();
    
    /**
     *  Get the operating frequency of the cavity.
     *
     *  @return  frequency of RF cavity (in <bold>Hertz</bold>)
     */
    public double getCavFrequency();
    
    /**
     *  Get the RF cavity field phase.
     *
     *  @return     cavity phase (in <bold>Rad</bold>).
     */
    public double getCavPhase();
    
    /**
     *  Set the RF cavity field amplitude.
     *
     *  @param  dblAmp    cavity amplitude (in <bold>MV/m</bold>).
     */
    public void setCavAmp(double dblAmp);

    /**
     * Sets the frequency of the RF in the cavity.
     *  
     * @param dblFreq   RF frequency (in <b>Hz</b>)
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    public void setCavFrequency(double dblFreq);
        
    /**
     *  Set the RF cavity field phase.
     *
     *  @param  dblAmp    cavity phase (in <bold>Rad</bold>).
     */
    public void setCavPhase(double dblPhase);
}
