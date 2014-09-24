/*
 * @(#)TransferMatrixGenerator.java          07/2012
 *
 * Copyright (c) 2012 Oak Ridge National Laboratory
 * Oak Ridge, Tennessee 37831, U.S.A.
 * All rights reserved.
 *
 */
package xal.tools.twissobserver;


import gov.sns.tools.beam.CorrelationMatrix;
import gov.sns.tools.beam.PhaseMap;
import gov.sns.tools.beam.PhaseMatrix;
import gov.sns.xal.model.ModelException;
import gov.sns.xal.model.alg.EnvTrackerAdapt;
import gov.sns.xal.model.alg.TransferMapTracker;
import gov.sns.xal.model.probe.EnvelopeProbe;
import gov.sns.xal.model.probe.ProbeFactory;
import gov.sns.xal.model.probe.TransferMapProbe;
import gov.sns.xal.model.probe.traj.EnvelopeProbeState;
import gov.sns.xal.model.probe.traj.EnvelopeTrajectory;
import gov.sns.xal.model.probe.traj.Trajectory;
import gov.sns.xal.model.probe.traj.TransferMapState;
import gov.sns.xal.model.probe.traj.TransferMapTrajectory;
import gov.sns.xal.model.pvlogger.PVLoggerDataSource;
import gov.sns.xal.model.scenario.Scenario;
import gov.sns.xal.model.sync.SynchronizationException;
import gov.sns.xal.smf.AcceleratorSeq;


/**
 * <p>
 * Class for generating transfer matrices for the XAL accelerator beamlines.  The
 * transfer matrices are for the hardware systems only, no space charge is assumed.
 * </p>
 * @author Eric Dai
 * @author Christopher K Allen
 * @since June 8, 2012
 */
public class TransferMatrixGenerator {

    
    /**
     * Enumeration set representing the possible synchronization modes of the
     * <code>{@link Scenario}</code> class.
     *
     * @author Christopher K. Allen
     * @since   Jul 19, 2012
     */
    public enum SYNC {
    
        /** Synchronize the machine parameters to the current live values */
        LIVE(Scenario.SYNC_MODE_LIVE),
        
        /** Synchronize to the current live values, except for the RF parameter which are design */
        DESIGNRF(Scenario.SYNC_MODE_RF_DESIGN),
        
        /** Synchronize the machine parameters to their design values */
        DESIGN(Scenario.SYNC_MODE_DESIGN);
        
        /*
         * Operations
         */
        
        /**
         * Returns the value of the synchronization mode represented by this constant,
         * as used by the <code>{@link Scenario}</code> class.
         * 
         * @return  string value of the synchronization mode
         *
         * @author Christopher K. Allen
         * @since  Jul 19, 2012
         * 
         * @see Scenario#setSynchronizationMode(String)
         */
        public String   getSynchronizationValue() {
            return this.strSynMode;
        }
        
        /*
         * Private
         */
        
        /** Value of the synchronization code used by <code>Scenario</code> */
        private final String        strSynMode;
        
        /**
         * Enumeration constant constructor - initialize the synchronization mode value.
         * 
         * @param   strSyncMode      synchronization mode string used by <code>Scenario</code>
         */
        private SYNC(String strSyncMode) {
            this.strSynMode = strSyncMode;
        }
    }
    
    
	
	/*
	 * Local Attributes
	 */
	/** Accelerator sequence where transfer maps are being computed */
    private AcceleratorSeq     smfSeq;
    

	/** Transfer map probe used for simulation without space charge */
	private TransferMapProbe   mdlTmapProbe;
	
	/** Envelope probe used to simulation with space charge */
	private EnvelopeProbe      mdlEnvProbe;
	
    /** Model of the above hardware */
    private Scenario           mdlBeamline;
    
    /** The probe propagation history through machine */
    private Trajectory         mdlTraj;
    
	


	
	/*
	 * Initialization
	 */
	
    /**
     * Creates a new <code>TransferMatrixGenerator</code> object using the 
     * given sequence, and synchronizes to the design parameters.
     * 
     * @param   smfSeq   the desired sequence for transfer matrix computation
     *
     * @throws ModelException  and error occurred when instantiating the machine model
     */
    public TransferMatrixGenerator(AcceleratorSeq  smfSeq) throws ModelException {
        this(smfSeq, SYNC.DESIGN);
    }

    /**
     * Creates a new <code>TransferMatrixGenerator</code> object using 
     * the given sequence, initializes the model to the historical machine state identified
     * by the given PV Logger ID.
     * 
     * @param smfSeq       the desired sequence for transfer matrix computation
     * @param lngPvLogId   PV Logger ID of the historical machine snapshot where model parameters are taken  

     * @throws ModelException  and error occurred when instantiating the machine model
     */
    public TransferMatrixGenerator(AcceleratorSeq  smfSeq, long lngPvLogId) throws ModelException {
        this(smfSeq);
        this.setSyncToMachineHistory(lngPvLogId);
    }
    
	/**
	 * Creates a new <code>TransferMatrixGenerator</code> object using the given accelerator (XML definition file)
	 * accelerator, the given sequence, and the given synchronization mode.
	 * 
     * @param smfSeq       the desired sequence for transfer matrix computation
     * @param enmSyn       source of machine parameters

	 * @throws ModelException  and error occurred when instantiating the machine model
	 */
	public TransferMatrixGenerator(AcceleratorSeq  smfSeq, SYNC enmSyn) throws ModelException {
	    
        // Get the sequence we are working in
        this.smfSeq = smfSeq;
        
        
        // Create the zero-current probe 
        TransferMapTracker algXmap = new TransferMapTracker();

        this.mdlTmapProbe = ProbeFactory.getTransferMapProbe(this.smfSeq, algXmap);
        this.mdlTmapProbe.reset(); 

        
        // Create the finite space charge probe
        EnvTrackerAdapt    algEnvTrk = new EnvTrackerAdapt();
        algEnvTrk.setMaxIterations(1000);
        
        // Create and initialize the envelope probe
        this.mdlEnvProbe = ProbeFactory.getEnvelopeProbe(this.smfSeq, algEnvTrk);
        this.mdlEnvProbe.reset();


        // Create the model and load the parameters from the saved PV Logger data
        this.mdlBeamline = Scenario.newScenarioFor(smfSeq);
        this.setSynchronizationMode(enmSyn);
   
        
        // Obviate uninitialized objects
        this.mdlTraj  = null;
	}
	
    /**
     * Sets the source of hardware parameters for the simulations.  The given
     * synchronization mode determines where this parameters are taken from (e.g., the live machine,
     * the design parameters, etc.).  It is also possible to synchronize the hardware
     * parameters to a historical machine state with a know PV Logger ID 
     * (see <code>{@link #setSyncToMachineHistory(long)}</code>).
     *
     * @param enmSync  synchronization mode determining parameter source
     *
     * @throws SynchronizationException  general error during model synchronization 
     *  
     * @author Christopher K. Allen
     * @since  Jul 19, 2012
     */
    public void setSynchronizationMode(SYNC enmSync) throws SynchronizationException {
        this.mdlBeamline.setSynchronizationMode(enmSync.getSynchronizationValue());
        this.mdlBeamline.resync();
    }
    
	/**
	 * Synchronizes to the machine parameters at the time of the given PV logger
	 * snapshot ID.  The transfer matrices provided will be from this machine state.
	 *
	 * @param lngPvLogId   PV Logger ID for the historical machine state
	 *
     * @throws SynchronizationException  general error during model synchronization 
     * 
	 * @author Christopher K. Allen
	 * @since  Jul 19, 2012
	 */
	public void setSyncToMachineHistory(final long lngPvLogId) throws SynchronizationException {
	    
        PVLoggerDataSource  srcPvLog = new PVLoggerDataSource(lngPvLogId);
        this.mdlBeamline = srcPvLog.setModelSource(this.smfSeq, this.mdlBeamline);
        this.mdlBeamline.resync();
	}
	
	
    
    /*
     * Operations
     */
    
	/**
	 * <p>
	 * Generates all the transfer matrices for the zero-current
	 * case.  The model is run using a transfer map probe which only
	 * collects machine data.
	 * </p>
	 * <p>
	 * <h4>NOTE:</h4>
	 * &middot; This simulation always starts with the first element in the sequence
	 * since the transfer matrices are not dependent upon beam state.
	 * </p>
	 * 
	 * @throws ModelException  general error during model synchronization or simulation
	 *
	 * @author Christopher K. Allen
	 * @since  Jul 26, 2012
	 */
	public void generateWithoutSpaceCharge() throws ModelException {

	    // Initialize Probe
        this.mdlTmapProbe.reset(); 

        // Load the probe, synchronize the model, and run 
        this.mdlBeamline.setProbe(this.mdlTmapProbe);
        this.mdlBeamline.resyncFromCache();
        this.mdlBeamline.run();
        
        // Save the trajectory
        this.mdlTraj = this.mdlBeamline.getTrajectory();
	}
	
	/**
     * <p>
     * Generates all the transfer matrices including space 
     * charge effects.  The simulation is performed with the given bunch charge
     * and the initial beam state at the entrance to the associated accelerator
     * sequence.     
     * </p>
     * <p>
     * This methods is a direct proxy to 
     * <code>{@link #generateWithSpaceCharge(String, double, CorrelationMatrix)}</code>
     * using <code>null</code> as the first argument.
     * </p>
     * 
     * @param dblBnchChg       beam bunch charge in Coulombs
     * @param matInitState     initial state of the beam, initial covariance matrix
     * 
     * @throws ModelException  general error during model synchronization or simulation
	 *
	 * @author Christopher K. Allen
	 * @since  Mar 28, 2013
	 * 
	 * @see    #generateWithSpaceCharge(String, double, CorrelationMatrix)
	 */
	public void generateWithSpaceCharge(double dblBnchChg, CorrelationMatrix matInitState)
	        throws ModelException 
    {
	    this.generateWithSpaceCharge(null, dblBnchChg, matInitState);
    }
	
	/**
	 * <p>
	 * Generates all the transfer matrices when there are significant space
	 * charge effects.  The simulation is performed with the given bunch charge
	 * and the initial beam state at the given element location.  Thus, the given
	 * probe state corresponds to the given element.  
	 * </p>
	 * <p>
	 * There is a self-consistency issue between the space charge 
	 * effects and the beam size.  Thus, the transfer matrices will typically be 
	 * different for different starting configurations of the beam.
	 * </p>
	 * <p>
	 * <h4>NOTE:</h4>
	 * &middot; Bunch charge <i>Q</i> is given by beam current <i>I</i> divided by
	 *          machine frequency <i>f</i>.  Specifically, <i>Q</i> = <i>I</i>/<i>f</i>.
	 * </p> 
	 *
     * @param strDevIdStart    ID of device with which to start the simulation,
     *                         or <code>null</code> for the beginning of the sequence 
	 * @param dblBnchChg       beam bunch charge in Coulombs
	 * @param matInitState     initial state of the beam, initial covariance matrix
	 * 
     * @throws ModelException  general error during model synchronization or simulation
	 *
	 * @author Christopher K. Allen
	 * @since  Jul 26, 2012
	 */
	public void generateWithSpaceCharge(String strDevIdStart, double dblBnchChg, CorrelationMatrix matInitState)
	    throws ModelException 
	{
	    
	    // Initialize the probe
        this.mdlEnvProbe.reset();
	    this.mdlEnvProbe.setCorrelation(matInitState);
	    this.mdlEnvProbe.setBeamCharge(dblBnchChg);
	    

	    // Load the probe into the model, synchronize the model parameters, 
	    //     set the start location, and run
	    this.mdlBeamline.setProbe(this.mdlEnvProbe);
	    this.mdlBeamline.resyncFromCache();
        if (strDevIdStart != null)
            this.mdlBeamline.setStartNode(strDevIdStart);
	    this.mdlBeamline.run();
	    
	    
	    // Save the trajectory
	    this.mdlTraj = this.mdlBeamline.getTrajectory();
	}
	
	/**
	 *
     * Compute and return the transfer matrix from the beginning 
     * of the current accelerator sequence to the entrance of the given element.
     * This method is viable only after a call to methods 
     * <code>{@link #generateWithoutSpaceCharge()}</code> or 
     * <code>{@link #generateWithSpaceCharge(String, double, CorrelationMatrix)}</code>.
     * 
     * @param strElemStop     string identifier of the terminal element
     * 
     * @return             beam optics transfer matrix for the beam line up to the given element
     * 
     * @throws IllegalStateException   The transfer matrices have not been generated yet
	 *
     * @see    #generateWithoutSpaceCharge()
     * @see    #generateWithSpaceCharge(String, double, CorrelationMatrix)
     * 
	 * @author Christopher K. Allen
	 * @since  Jul 27, 2012
	 */
	public PhaseMatrix retrieveTransferMatrix(String strElemStop) throws IllegalStateException {
	    
        // Error condition - the simulation hasn't been run yet
        if (this.mdlTraj == null)
            throw new IllegalStateException("No transfer matrices, model has not been run");
        
        // The transfer matrices are beam independent
        if (this.mdlTraj instanceof TransferMapTrajectory) {
            TransferMapTrajectory  trjMap = (TransferMapTrajectory)this.mdlTraj;
            
            TransferMapState    stateElem = (TransferMapState)trjMap.stateForElement(strElemStop);
            PhaseMap            mapPhi    = stateElem.getTransferMap();
            PhaseMatrix matPhi = mapPhi.getFirstOrder();
            
            return matPhi;
        }
        
        // The transfer matrices contain space charge effects
        if (this.mdlTraj instanceof EnvelopeTrajectory) {
            EnvelopeTrajectory      trjEnv = (EnvelopeTrajectory)this.mdlTraj;
            
            EnvelopeProbeState  stateEnv = (EnvelopeProbeState)trjEnv.stateForElement(strElemStop);
            PhaseMatrix         matPhi   = stateEnv.getResponseMatrix();
            
            return matPhi;
        }
        
        // There can only be envelope trajectories or transfer map trajectories
        //  There is no way to reach this point so a serious error has occurred
        throw new IllegalStateException("Serious internal error - unknown simulation trajectory");
	}
	
	/**
	 * Compute and return the transfer matrix from the entrance of the given starting element
	 * to the entrance of the given stop element (within the current accelerator sequence).
	 * This method is viable only after a call to methods 
	 * <code>{@link #generateWithoutSpaceCharge()}</code> or 
	 * <code>{@link #generateWithSpaceCharge(String, double, CorrelationMatrix)}</code>.
	 * 
	 * @param strElemStart    string identifier of the starting element
	 * @param strElemStop     string identifier of the terminal element
	 * 
	 * @return             beam optics transfer matrix for the beam line up to the given element
	 * 
     * @throws IllegalStateException   The transfer matrices have not been generated yet
     * 
     * @see    #generateWithoutSpaceCharge()
     * @see    #generateWithSpaceCharge(String, double, CorrelationMatrix)
	 *
	 * @author Christopher K. Allen
	 * @since  Jul 18, 2012
	 */
	public PhaseMatrix retrieveTransferMatrix(String strElemStart, String strElemStop) throws IllegalStateException {
	    
	    // Error condition - the simulation hasn't been run yet
	    if (this.mdlTraj == null)
	        throw new IllegalStateException("No transfer matrices, model has not been run");
	    
	    // The transfer matrices are beam independent
	    if (this.mdlTraj instanceof TransferMapTrajectory) {
	        TransferMapTrajectory  trjMap = (TransferMapTrajectory)this.mdlTraj;
	        
	        PhaseMatrix matPhi = trjMap.getTransferMatrix(strElemStart, strElemStop);
	        
	        return matPhi;
	    }
	    
	    // The transfer matrices contain space charge effects 
        if (this.mdlTraj instanceof EnvelopeTrajectory) {
            EnvelopeTrajectory trjEnv = (EnvelopeTrajectory)this.mdlTraj;
            
            PhaseMatrix matPhi = trjEnv.getTransferMatrix(strElemStart, strElemStop);
            
            return matPhi;
        }
        
        // There can only be envelope trajectories or transfer map trajectories
        //  There is no way to reach this point so a serious error has occurred
        throw new IllegalStateException("Serious internal error - unknown simulation trajectory");
	}

}
