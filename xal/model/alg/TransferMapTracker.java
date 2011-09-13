/*
 * Created on Jun 22, 2004
 *
 *  Copyright 2004 by SNS/LANL
 * 
 */
package xal.model.alg;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseVector;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.TransferMapProbe;


/**
 * @author Christopher K. Allen
 *
 */
public class TransferMapTracker extends Tracker {
    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = TransferMapTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<TransferMapProbe>       s_clsProbeType = TransferMapProbe.class;

	
    /**
     * Default constructor for a <code>TransferMapTracker</code> objects.  These
     * objects have no internal state information.
     * 
     */
    public TransferMapTracker() {
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }


    /**
     * Perform the actual probe propagation through the the modeling element.
     * 
     * @param ifcProbe  interface to <code>TransferMapProbe</code> to be advanced
     * @param elem      interface to modeling element through which to advance probe
     * 
     * @throws ModelException   error during propagation
     * 
     * @see xal.model.alg.Tracker#doPropagation(xal.model.IProbe, xal.model.IElement)
     */
    @Override
    public void doPropagation(IProbe ifcProbe, IElement elem) throws ModelException {
        if (!this.validProbe(ifcProbe))
            throw new ModelException("TransferMapTracker::propagate() - cannot propagate, invalid probe type.");
        TransferMapProbe probe = (TransferMapProbe)ifcProbe;

        double    dblLen = elem.getLength();
        
        this.advanceState(probe, elem, dblLen);
        this.advanceProbe(probe, elem, dblLen);
    }


    /** 
     * Advances the probe state through the element.  
     *
     *  @param  probe    interface to probe being modified
     *  @param  ifcElem     interface to element acting on probe
     *  @param  dblLen      element length
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState( final TransferMapProbe probe, final IElement element, final double length ) throws ModelException {
        // Properties of the element
        final PhaseMap mapPhi = element.transferMap( probe, length );
      // Compose the transfer maps
        final PhaseMap mapProbe = probe.getTransferMap();
        final PhaseMap mapComp = mapPhi.compose( mapProbe );
        probe.setTransferMap( mapComp );
		
		final PhaseVector z0 = probe.phaseCoordinates();
		final PhaseVector z1 = mapPhi.apply( z0 );
        probe.setPhaseCoordinates( z1 );
	}

}
