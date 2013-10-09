/*
 * Created on Mar 17, 2004
 */
package xal.model.elem;

/**
 * @author Craig McChesney
 */
public interface IRfCavity  {

    /**
     *  Get the RF cavity field amplitude.
     *
     *  @return     cavity amplitude (in <bold>MV/m</bold>).
     */
    public double getCavAmp();
    
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
     *  Set the RF cavity field phase.
     *
     *  @param  dblAmp    cavity phase (in <bold>Rad</bold>).
     */
    public void setCavPhase(double dblPhase);
}
