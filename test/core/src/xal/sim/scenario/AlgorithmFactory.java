/*
 * AlgorithmFactory.java
 *
 * Created on January 6, 2003, 7:40 PM
 */

package xal.sim.scenario;



import xal.model.IAlgorithm;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.alg.EnvelopeBacktracker;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.EnvelopeTrackerPmqDipole;
import xal.model.alg.ParticleTracker;
import xal.model.alg.SynchronousTracker;
import xal.model.alg.Trace3dTracker;
import xal.model.alg.Tracker;
import xal.model.alg.TransferMapTracker;
import xal.model.alg.TwissTracker;
import xal.model.alg.TwissTrackerPmq;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.tools.data.EditContext;



/**
 * <p>
 * Factory class for instantiating and initializing algorithm objects for the
 * XAL online model.  Algorithm classes are typically derived from the 
 * <code>Tracker</code> base class and must expose the <code>IAlgorithm</code> interface.
 * We take the initialization parameters from the XAL "<tt>model.params</tt>" file, using
 * the <i>edit context</i> mechanism in XAL.  (An <code>EditContext</code> object is
 * associated with each <code>Accelerator</code> object.)
 * The file contains data tables which are structured for the different types of
 * algorithms.  Each algorithm may have many different tables, typically different
 * parameters for different locations along the beamline.  The table label 
 * data is typically defined in the source file for the algorithm, whereas the
 * specific table name is given by the accelerator sequence identifier where the
 * algorithm starts.
 * </p>
 * <p>
 * Each algorithm will support the propagation of one type of simulation 
 * probe, however, each probe class may accept multiple types of algorithms for
 * its propagation.  It is the responsibility of the developer to create and
 * use the appropriate algorithm object for the given probe type.
 * </p>
 * 
 * @see EditContext
 * @see IAlgorithm
 * @see Accelerator#editContext()
 * @see AcceleratorSeq#getId()
 *
 * @author  Christopher K. Allen
 * @since   Oct 25, 2012
 */
public final class AlgorithmFactory {
    
    /*
     * Global Constants
     */
    
    
    /** data node label for algorithm data */
    public final static String      NODETAG_ALG = "algorithm";
    
    /** attribute label for type string identifier */
    public final static String      ATTRTAG_TYPE = "type";
    
    
    
    
    /*
     * Factory Methods
     */
    
    
    /*
     * Single Particle Probe Algorithms
     */
    
    /**
     * <p>
     * Convenience method: Creates a new <code>SynchronousTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>SynchronousTracker</code> algorithm will
     * propagate a <code>SynchronousProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to constructor access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 26, 2012
     */
    public static SynchronousTracker    createSynchronousTracker(AcceleratorSeq smfSeq) throws InstantiationException {
        
        SynchronousTracker  algSync = AlgorithmFactory.createTrackerFor(smfSeq, SynchronousTracker.class);
        
        return algSync;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>ParticleTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>ParticleTracker</code> algorithm will
     * propagate a <code>ParticleProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to constructor access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 26, 2012
     */
    public static ParticleTracker       createParticleTracker(AcceleratorSeq smfSeq) throws InstantiationException {
        
        ParticleTracker     algPrtl = AlgorithmFactory.createTrackerFor(smfSeq, ParticleTracker.class);
        
        return algPrtl;
    }
    
    
    /*
     * Machine Parameter Tracking Algorithms
     */
    
    /**
     * <p>
     * Convenience method: Creates a new <code>TransferMapTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>TransferMapTracker</code> algorithm will
     * propagate a <code>TransferMapProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to constructor access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 26, 2012
     */
    public static TransferMapTracker    createTransferMapTracker(AcceleratorSeq smfSeq) throws InstantiationException {

        TransferMapTracker      algXfer = AlgorithmFactory.createTrackerFor(smfSeq, TransferMapTracker.class);
        
        return algXfer;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>TwissTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>TwissTracker</code> algorithm will
     * propagate a <code>TwissProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to constructor access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 26, 2012
     */
    public static TwissTracker          createTwissTracker(AcceleratorSeq smfSeq) throws InstantiationException {
        
        TwissTracker        algTwiss = AlgorithmFactory.createTrackerFor(smfSeq, TwissTracker.class);
        
        return algTwiss;
    }

    /**
     * <p>
     * Convenience method: Creates a new <code>TwissTrackerPmq</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>TwissTrackerPmq</code> algorithm will
     * propagate a <code>TwissProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to constructor access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 29, 2012
     */
    public static TwissTrackerPmq          createTwissTrackerPmq(AcceleratorSeq smfSeq) throws InstantiationException {
        
        TwissTrackerPmq        algTwsPmq = AlgorithmFactory.createTrackerFor(smfSeq, TwissTrackerPmq.class);
        
        return algTwsPmq;
    }

    
    /*
     * EnvelopeProbe Algorithms
     */
    
    /**
     * <p>
     * Convenience method: Creates a new <code>Trace3dTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>Trace3dTracker</code> algorithm will
     * propagate an <code>EnvelopeProbe</code> object.
     * </p>
     * <h3>NOTES:</h3>
     * <p>
     * &middot;  The <code>Trace3dTracker</code> algorithm is all but deprecated.  Use of this
     * method is discouraged except for benchmarking purposes.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 25, 2012
     */
    static public Trace3dTracker    createTrace3dTracker(AcceleratorSeq smfSeq) throws InstantiationException {
        
        Trace3dTracker  algEnv = AlgorithmFactory.createTrackerFor(smfSeq, Trace3dTracker.class);
        
        return algEnv;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>EnvelopeTracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>EnvelopeTracker</code> algorithm will
     * propagate an <code>EnvelopeProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @throws          InstantiationException  unable to instantiate the object due to access violation
     *                                          or the class has no nullary constructor
     *
     * @author Christopher K. Allen
     * @since  Oct 24, 2012
     */
    public static EnvelopeTracker   createEnvelopeTracker(AcceleratorSeq smfSeq) throws InstantiationException {
        
        EnvelopeTracker algEnv = AlgorithmFactory.createTrackerFor(smfSeq, EnvelopeTracker.class);
        
        return algEnv;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>EnvelopeBacktracker</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>EnvelopeBacktracker</code> algorithm will
     * propagate an <code>EnvelopeProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @throws          InstantiationException  unable to instantiate the object due to access violation
     *                                          or the class has no nullary constructor
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @author Christopher K. Allen
     * @since  Oct 24, 2012
     */
    public static EnvelopeBacktracker   createEnvelopeBacktracker(AcceleratorSeq smfSeq) throws InstantiationException {

        EnvelopeBacktracker algEnv = AlgorithmFactory.createTrackerFor(smfSeq, EnvelopeBacktracker.class);
        
        return algEnv;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>EnvTrackerAdapt</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>EnvTrackerAdapt</code> algorithm will
     * propagate an <code>EnvelopeProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @throws          InstantiationException  unable to instantiate the object due to access violation
     *                                          or the class has no nullary constructor
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @author Christopher K. Allen
     * @since  Oct 24, 2012
     */
    public static EnvTrackerAdapt       createEnvTrackerAdapt(AcceleratorSeq smfSeq) throws InstantiationException {
        
        EnvTrackerAdapt algEnv = AlgorithmFactory.createTrackerFor(smfSeq, EnvTrackerAdapt.class);
        
        return algEnv;
    }
    
    /**
     * <p>
     * Convenience method: Creates a new <code>EnvelopeTrackerPmqDipole</code> instance by calling the
     * class method <code>{@link #createTrackerFor(AcceleratorSeq, Class)}</code> with the 
     * returned class type.  
     * </p>
     * <p>
     * The new algorithm instance is initialized
     * with parameters specified in the edit context of the given accelerator
     * sequence object.  The returned <code>EnvelopeTrackerPmq</code> algorithm will
     * propagate an <code>EnvelopeProbe</code> object.
     * </p>
     *
     * @param smfSeq    Accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @throws          InstantiationException  unable to instantiate the object due to access violation
     *                                          or the class has no nullary constructor
     *
     * @see             xal.model.probe.EnvelopeProbe
     * 
     * @author Christopher K. Allen
     * @since  Oct 24, 2012
     */
    public static EnvelopeTrackerPmqDipole  createEnvelopeTrackerPmqDipole(AcceleratorSeq smfSeq) throws InstantiationException {
        
        EnvelopeTrackerPmqDipole    algEnv = AlgorithmFactory.createTrackerFor(smfSeq, EnvelopeTrackerPmqDipole.class);
        
        return algEnv;
    }
    
    
    /**
     * Creates a new <code>Tracker</code> derived algorithm instance and initializes
     * it with parameters specified in the edit context of the given accelerator
     * sequence object.  These parameters are located in the "<tt>model.params</tt>" 
     * file which is part of the XAL configuration initialization.  Within the file are
     * data tables for initializing algorithms, the labels of these tables are particular
     * to the algorithm type being instantiated (typically they are defined within the
     * algorithm source file).  The actual table name from which the initialization parameters 
     * are taken is given by the sequence id of the provided accelerator sequence.  If there are no
     * tables with that name then the mechanism automatically defers
     * to the table named "<tt>default</tt>".  
     * 
     * @param <T>       Type of the algorithm to be instantiated, must be derived from <code>Tracker</code>
     *  
     * @param smfSeq    accelerator sequence object indirectly containing the edit context through
     *                  its associated accelerator 
     * @param clsTkr    the class type of the algorithm to be instantiated  
     *                  
     * @return          newly instantiated algorithm that was initialized from the given accelerator
     *                  sequence's edit context.
     *
     * @throws          InstantiationException  unable to instantiate the tracker object due to access violation
     *                                          or the class has no nullary constructor
     *  
     * @see             xal.model.alg.Tracker
     * 
     * @author Christopher K. Allen
     * @since  Oct 24, 2012
     */
    public static <T extends Tracker> T createTrackerFor(AcceleratorSeq smfSeq, Class<T> clsTkr) throws InstantiationException {
        String          strSeqId  = smfSeq.getId();
        Accelerator     smfAccel  = smfSeq.getAccelerator();
        EditContext     ctxParams = smfAccel.editContext();
        
        T algTracker;
        try {
            algTracker = clsTkr.newInstance();
            algTracker.load(strSeqId, ctxParams);
            
            return algTracker;
            
        } catch (IllegalAccessException e) {
            throw new InstantiationException("Unable to access constructor for " + clsTkr.getName() 
                    + ". Source Msg: " + e.getMessage()
                    );
        }
        
    }
    
}
