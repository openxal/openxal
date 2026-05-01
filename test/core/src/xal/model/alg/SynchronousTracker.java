/*
 * Created on Jun 8, 2004
 *
 * Copyright SNS/LANL, 2004
 */
package xal.model.alg;

import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.SynchronousProbe;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;

/**
 * Algorithm for propagating a <code>SynchronousParticle</code> probe object 
 * through any modeling element that exposes the <code>IComponent</code> 
 * interface.
 * 
 * @author Christopher K. Allen
 *
 */
public class SynchronousTracker extends Tracker {




    /*
     *  Global Constants
     */

    /** Label of the edit context parameter table in the "model.params" file */
    private static final String STR_LBL_TABLE = "SynchronousTracker";

    
    
    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = SynchronousTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<SynchronousProbe>       s_clsProbeType = SynchronousProbe.class;
    

    
    /*
     *  Local Attributes
     */
    
    
    
    /*
     * Initialization
     */
     
    /**
     * Default constructor for a <code>SynchronousTracker</code> objects.  These
     * objects have no internal state information.
     * 
     */
    public SynchronousTracker() {
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }
    
    /**
     * Copy constructor for SynchronousTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public SynchronousTracker( SynchronousTracker sourceTracker ) {
        super( sourceTracker );
    }

    /**
     * Creates a deep copy of SynchronousTracker
     */
    @Override
    public SynchronousTracker copy() {
        return new SynchronousTracker( this );
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
     * Tracker Protocol
     */
     
     
    /**
     * Perform the actual probe propagation through the the modeling element.
     * 
     * @param probe     interface to <code>SynchronousProbe</code> to be advanced
     * @param elem      interface to modeling element through which to advance probe
     * 
     * @throws ModelException   error during propagation
     * 
     * @see xal.model.alg.Tracker#doPropagation(xal.model.IProbe, xal.model.IElement)
     */
    @Override
    public void doPropagation(IProbe probe, IElement elem)
        throws ModelException 
    {
    
    }

}
