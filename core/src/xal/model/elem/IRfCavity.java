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
     *  @return     cavity amplitude (in <b>MV/m</b>).
     */
    public double getCavAmp();
    
    /**
     *  Get the RF cavity field phase.
     *
     *  @return     cavity phase (in <b>Rad</b>).
     */
    public double getCavPhase();
    
    /**
     *  Set the RF cavity field amplitude.
     *
     *  @param  dblAmp    cavity amplitude (in <b>MV/m</b>).
     */
    public void setCavAmp(double dblAmp);

    /**
     *  Set the RF cavity field phase.
     *
     *  @param  dblPhase    cavity phase (in <b>Rad</b>).
     */
    public void setCavPhase(double dblPhase);
}
