/*
 * ParticleTracker.java
 *
 * Created on September 9, 2002, 11:16 AM
 */

package xal.model.alg;


import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.probe.ParticleProbe;



/**
 * Algorithm for tracking a single particle, represented by the class
 * <code>ParticleProbe</code> through a XAL modeling element, represented
 * by an object exposing the <code>IComponent</code> interface.
 *
 * @author  Christopher K. Allen
 * @author Craig McChesney
 */


public class ParticleTracker extends Tracker {

    /*
     *  Global Attributes
     */

    /** Label for edit context table containing algorithm parameters - i.e., in "model.params" file */ 
    private static final String STR_LBL_TABLE = "ParticleTracker";
    

    /** string type identifier for this algorithm */
    public static final String      s_strTypeId = ParticleTracker.class.getName();
    
    /** current version of this algorithm */
    public static final int         s_intVersion = 1;

    /** probe type recognized by this algorithm */
    public static final Class<ParticleProbe>       s_clsProbeType = ParticleProbe.class;


    
    /*
     *  Local Attributes
     */
     
    
    
    
    /*
     * Initialization
     */
     
    
    /** 
     *  Creates a new instance of ParticleTracker 
     */
    public ParticleTracker() { 
        super(s_strTypeId, s_intVersion, s_clsProbeType);
    }; 
    
    /**
     * Copy constructor for ParticleTracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    public ParticleTracker( ParticleTracker sourceTracker ) {
        super( sourceTracker );
    }
    
    /**
     * Create a deep copy of ParticleTracker
     */
    @Override
    public ParticleTracker copy() {
        return new ParticleTracker( this );
    }


    /*
     *  Data Queries
     */    
    
//    /**
//     * Returns the maximum element subsection length (in meters) that the probe 
//     * may be advanced before saving particle state.
//     */
//    private double getMaxStepSize() {
//        return this.m_dblMaxStep;
//    }
    

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
     * Propagates the probe through the element.
     *
     *  @param  iProbe  probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     */
    @Override
    public void doPropagation(IProbe iProbe, IElement elem) throws ModelException {
        
        if (!this.validProbe(iProbe))
            throw new ModelException("ParticleTracker::propagate() - cannot propagate, invalid probe type.");
        ParticleProbe probe = (ParticleProbe)iProbe;

//        probe.setCurrentElement(elem.getId());
      
        double    dblLen = elem.getLength();
        
        this.advanceState(probe, elem, dblLen);
        this.advanceProbe(probe, elem, dblLen);

            
//      // take snapshot at beginning of element
//      probe.update();
//        
//      int nSteps = compStepCount(elem);
//      double sectionSize = elem.getLength() / nSteps;
//      for (int i=0 ; i<nSteps ; i++) {
//        this.advanceState(probe, elem, sectionSize);
//        this.advanceProbe(probe, elem, sectionSize);
//        probe.update();
//      }
    };
  


    /*
     * Support Methods
     */
  
//    /**
//     * Returns the number of subsections to break the specified element into for
//     * propagation. Always one for a particle tracker.
//     * 
//     * @param elem Element currently acting on probe
//     * 
//     * @return one
//     */
//    protected int compStepCount(IElement elem) {
//        double dblSecs = Math.ceil(elem.getLength() / getMaxStepSize());
//        int nSecs      = (int) Math.max(dblSecs, 1.0);
//        
//        return nSecs;
//    }
        
    /** 
     * Advances the probe state through the element.  
     *
     *  @param  probe       probe being modified
     *  @param  elem        element acting on probe
     *  @param  dblLen      length of element to advance
     *
     *  @exception ModelException     bad element transfer matrix/corrupt probe state
     */
    protected void advanceState(ParticleProbe probe, IElement elem, double dblLen)
    		throws ModelException {
        
        // Properties of the element
        PhaseMap  mapPhi = elem.transferMap(probe, dblLen);
        
        // Advance state vector
        PhaseVector  z0 = probe.getPhaseCoordinates();
        PhaseVector  z1 = mapPhi.apply(z0);
        
        probe.setPhaseCoordinates(z1);
        
        // Advance response matrix
        PhaseMatrix matPhi = mapPhi.getFirstOrder();
        PhaseMatrix R0 = probe.getResponseMatrix();
        PhaseMatrix R1 = matPhi.times( R0 );
        
        probe.setResponseMatrix(R1);
    }



}
