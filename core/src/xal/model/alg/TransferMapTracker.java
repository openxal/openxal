/*
 * Created on Jun 22, 2004
 *
 *  Copyright 2004 by SNS/LANL
 * 
 */
package xal.model.alg;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.TransferMapProbe;


/**
 * Propagates a <code>TransferMapPropbe</code> through a hardware element.  This
 * algorithm does not consider space charge, as that is an artifact of the beam, 
 * whereas the true transfer matrix is dependent upon the hardware only.  The transfer
 * maps of each hardware section are multiplied and then added to the probe's history.
 * 
 * 
 * @author Christopher K. Allen
 * @since  2002
 */
public class TransferMapTracker extends Tracker {
    
    
    /*
     * Global Constants
     */
    
    /** probe type recognized by this algorithm */
    public static final Class<TransferMapProbe>       s_clsProbeType = TransferMapProbe.class;



    /** Label for edit context table containing algorithm parameters - i.e., in "model.params" file */ 
    private static final String STR_LBL_TABLE = "TransferMapTracker";
    

    
    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = TransferMapTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    

	
    
    /*
     * Initialization
     */
    
    /**
     * Default constructor for a <code>TransferMapTracker</code> objects.  These
     * objects have no internal state information.
     * 
     */
    public TransferMapTracker() {
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }
    
    /**
     * Copy constructor for TransferMapTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public TransferMapTracker(TransferMapTracker sourceTracker) {
        super(sourceTracker);
    }

    /**
     * Creates a deep copy of TransferMapTracker
     */
    @Override
    public TransferMapTracker copy() {
        return new TransferMapTracker( this );
    }

    /*
     * IArchive Interface
     */
    
    /**
     * Place holder for loading additional parameters from an edit context.
     *  
     * @since Oct 26, 2012
     * @see xal.model.alg.Tracker#load(java.lang.String, xal.tools.data.EditContext)
     */
    @Override
    public void load(String strPrimKeyVal, EditContext ecTableData) throws DataFormatException {
        super.load(strPrimKeyVal, ecTableData);
        
        // Get the algorithm class name from the EditContext
        DataTable     tblAlgorithm = ecTableData.getTable( STR_LBL_TABLE );
        GenericRecord recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME,  strPrimKeyVal );
    
        if ( recTracker == null ) {
            recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME, "default" );  // just use the default record
        }
        
    }


    /**
     * Place holder for loading additional parameters from a data adaptor.
     * 
     * @since Oct 26, 2012
     * @see xal.model.alg.Tracker#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        super.load(daSource);
    }


    /**
     * Place holder for loading additional parameters from a data adaptor.
     * 
     * @since Oct 26, 2012
     * @see xal.model.alg.Tracker#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daptArchive) {
        super.save(daptArchive);
    }


    

    /*
     *  Tracker Abstract Protocol
     */

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
     *  @param  dblLng      element length
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState( final TransferMapProbe probe, final IElement ifcElem, final double dblLng ) throws ModelException {
        
        // Properties of the element
        final PhaseMap mapPhi = ifcElem.transferMap( probe, dblLng );

        // Set the partial (state) transfer map
        probe.setPartialTransferMap( mapPhi );
        
        // Compose the transfer maps
        final PhaseMap mapProbe = probe.getTransferMap();
        final PhaseMap mapComp = mapPhi.compose( mapProbe );
        probe.setTransferMap( mapComp );
	}

}
