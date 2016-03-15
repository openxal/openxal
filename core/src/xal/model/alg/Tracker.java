/*
 * Tracker.java
 *
 * Created on December 5, 2002, 9:26 PM
 */

package xal.model.alg;


import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.DataTable;
import xal.tools.data.EditContext;
import xal.tools.data.GenericRecord;
import xal.tools.data.IArchive;
import xal.model.IAlgorithm;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.sim.scenario.AlgorithmFactory;
import xal.smf.AcceleratorSeq;

import java.util.ArrayList;
import java.util.List;



/**
 * <h1>Abstract Base Class for Tracking Algorithms</h1> 
 *  
 * <p>
 * Provides boiler plate functionality for probe 
 * tracking algorithms from which more sophisticated algorithms may
 * inherit. 
 * </p>
 * <p>
 * Tracking algorithm objects are  classes that implement the 
 * <code>IAlgorithm</code> interface.  This class provides default implementations
 * for all methods of the <code>IAlgorithm</code> interface.  Derived classes
 * must implement the <b>doPropagation()</b> abstract method in order to 
 * provide the actual dynamics of the algorithm.  Note that derived classes may 
 * wish to override the <code>propagate(IProbe, IElement)</code> method directly
 * for complete control of the propagation mechanism. 
 * </p>
 * <p>
 * This class provides the 
 * methods the methods <code>advanceProbe</code> and <code>retractProbe</code> 
 * for forward-propagating and back-propagating the common probe
 * properties upstream and downstream, respectively.  (Common
 * properties meaning all properties except the defining state, which
 * is clearly unknowable by this class.)  These methods are conveniences
 * meant to be used in the abstract method <code>doPropagation</code>.
 * </p>  
 * <p>
 * <strong>NOTES:</strong> &nbsp; CKA
 * <br>
 * &middot; We might get a significant performance upgrade by 
 * eliminating the calls to <code>validElement(IElement)</code>
 * and <code>validProbe(IProbe)</code> within the method
 * <code>propagate(IProbe, IElement)</code>.
 * <br>
 * &middot; Derived classes should call <code>registerProbeType(Class)</code>
 * in order that the 
 * the <code>validProbe()</code> method function properly, 
 * that is, identify all probes that this class recognizes.
 * <br>
 * &middot; Perhaps it's better to eliminate <code>validProbe</code>
 * as a safety measure since, in the
 * current implementation, each probe carries it's own algorithm object.
 * So the probe already knows it's valid. 
 * </p>
 *
 * @author Christopher K. Allen
 * @author Craig McChesney
 * 
 * @see #doPropagation(IProbe, IElement)
 *
 * @see xal.model.IAlgorithm
 */
public abstract class Tracker implements IAlgorithm, IArchive {


    /*
     *  Abstract Methods
     */
    
    /**
     * <p>
     * The implementation must propagate the probe through the element 
     * according to the dynamics of the 
     * specific algorithm.  Derived classes must implement this method but the
     * <code>Tracker</code> base provided convenient methods for this implementation.
     * </p>
     * <p>
     * NOTE:
     * <br>The protected method 
     * <code>advanceProbe(IProbe, IElement, double)</code>
     * is available for derived classes.  It is a convenience method
     * for performing many of the common tasks in the forward propagation 
     * of any probe.  Thus, its use is not required.
     * </p>
     *
     *  @param  probe   probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     * 
     *  @see  Tracker#validProbe(IProbe)
     */
    public abstract void doPropagation(IProbe probe, IElement elem) throws ModelException;
  
  

    /*
     * Global Constants
     */
     
     // Trajectory update policy enumerations
     
     /** never update - this is done custom somewhere else */
     public static final int    UPDATE_CUSTOM = 0;
     
     /** save every state whenever probe is moved */
     public static final int    UPDATE_ALWAYS = 1;
         
     /** save state at element exit only      */
     public static final int    UPDATE_EXIT = 2;
     
     /** save state at element entrance */
     public static final int    UPDATE_ENTRANCE = 4;
     
     /** save state at element exit and entrance */
     public static final int    UPDATE_ENTRANCEANDEXIT = 6;
     
     
     
     
    // Archiving
     
     /** data node label for algorithm data */
     public final static String      NODETAG_ALG = "algorithm";
     
     /** attribute label for type string identifier */
     public final static String      ATTRTAG_TYPE = "type";
     
     /** attribute tag for the algorithm version number */
     public static final String      ATTRTAG_VER = "ver";
     
     
     // tracking options
    /** data node tag for common data */
    public static final String      NODETAG_TRACKER = "tracker";

    /** attribute tag for trajectory state update policy */
    public static final String      ATTRTAG_UPDATE = "update";

    /** attribute tag for debugging flag */
    public static final String      ATTRTAG_DEBUG = "debug";

    /** flag to update the beam phase in RF gaps ( a la parmila) rather than use default  values */
    public static final String       ATTRTAG_RFGAP_PHASE = "calcRfGapPhase";
      

    // EditContext
    /** EditContext table name for Tracker Data */
    private static final String     TBL_LBL_ALGORITHM = "Algorithm";
      
    /** Table record primary key name */
    public final static String      TBL_PRIM_KEY_NAME = "name";
    
    
    
    
    
    /*
     * Global Factory Methods
     */
    
    
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * a new instance of the appropriate <code>IAlgorithm</code> object
     * initialized with the data source behind the <code>DataAdaptor</code>
     * interface.
     * 
     * @param daSource <code>DataAdaptor</code> to read a Probe from
     * 
     * @return          new IAlgorithm object initialized to the given data source
     * 
     * @throws DataFormatException  bad data format, error reading data
     */
    public static IAlgorithm newInstance(DataAdaptor daSource)
            throws DataFormatException 
    {
        // Make sure this is an algorithm node
        String  strDataName = daSource.name();
        if ( !strDataName.equals(Tracker.NODETAG_ALG) )
            throw new DataFormatException("Tracker#newInstance(): not an algorithm data node - " + strDataName);
                
        String      strTypeName = daSource.stringValue(Tracker.ATTRTAG_TYPE);
        
        // Create an instance of the algorithm object
        IAlgorithm  algorithm;
        try {
            Class<?> clsAlg = Class.forName(strTypeName);
            algorithm = (IAlgorithm)clsAlg.newInstance();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFormatException("Tracker#newInstance() - unknown algorithm type " + strTypeName);
            
        }
        
        // Load the algorithm and return it
        algorithm.load(daSource);
        return algorithm;   
    }

    
    
    /**
     * <p>
     * Load the sequence's model parameters for the adaptive tracker from the 
     * global XAL edit context, which is retrievable through the given
     * <code>AcceleratorSeq</code> argument.
     * </p>
     *
     * <p>
     * The actual record used for the initializing data is taken as the that
     * with the primary key having the name of the given 
     * <code>AcceleratorSeq</code> name. 
     * </p>
     * 
     * <h3>NOTE</h3>
     * <p>
     * &middot; The returned algorithm type is that specified by the <tt>"type"</tt> attribute
     * of the <code>&lt;Algorithm&gt;</code> table contained in the 
     * <code>model.params</code> file.  The developer must ensure that the <tt>Probe</tt>
     * and the <tt>Algorithm</tt> objects are of the correct type.
     * </p>
     * 
     * @param sequence  sequence from which to retrieve the algorithms' parameters.
     * 
     * @return          new IAlgorithm object initialized to the given table data
     * 
     * @throws DataFormatException  bad data format, error reading data
     * 
     * @author Christopher K. Allen
     * @since  &gt; Oct 17, 2012
     * 
     * @deprecated  I want to discourage use of this method since it return the same "default"
     *              algorithm regardless the type of probe being used.  Please refer to
     *              <code>{@link AlgorithmFactory}</code>. 
     */
    @Deprecated
    public static IAlgorithm newFromEditContext( final AcceleratorSeq sequence ) {
        return newFromEditContext( sequence.getEntranceID(), sequence );
    }
        
    /**
     * <p>
     * Load the sequence's model parameters for the adaptive tracker from the 
     * global XAL edit context, which is retrievable through the given
     * <code>AcceleratorSeq</code> argument.
     * </p>
     * 
     * <p>
     * The record used to initialize the data is that with the primary key
     * have the given value of the argument <code>strLocationId</code>.
     * </p>
     * 
     * <h3>NOTE</h3>
     * <p>
     * &middot; The returned algorithm type is that specified by the <tt>"type"</tt> attribute
     * of the <code>&lt;Algorithm&gt;</code> table contained in the 
     * <code>model.params</code> file.  The developer must ensure that the <tt>Probe</tt>
     * and the <tt>Algorithm</tt> objects are of the correct type.
     * </p>
     * 
     * @param strLocationId The location ID of the entrance parameters to use
     * @param sequence The sequence for which to get the adaptive tracker parameters.
     *
     * @return  new IAlgorithm object initialized to the given table data
     * 
     * @throws DataFormatException  bad data format, error reading data
     * 
     * @author Christopher K. Allen
     * @since  &gt; Oct 17, 2012
     * 
     * @deprecated  I want to discourage use of this method since it return the same "default"
     *              algorithm regardless the type of probe being used.  Please refer to
     *              <code>{@link AlgorithmFactory}</code>. 
     */
    @Deprecated
    public static IAlgorithm newFromEditContext( final String strLocationId, final AcceleratorSeq sequence ) {

        // If locationID is null then take the sequence entrance identifier
        String strPrimKeyVal = ( strLocationId != null ) ? strLocationId : sequence.getEntranceID();

        // Get the algorithm class name from the EditContext
        EditContext   ecXalGlobal = sequence.getAccelerator().editContext();
        DataTable     tblAlgorithm = ecXalGlobal.getTable( Tracker.TBL_LBL_ALGORITHM );
        GenericRecord recAlgorithm = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME,  strPrimKeyVal );

        if ( recAlgorithm == null ) {
            recAlgorithm = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME, "default" );  // just use the default record
        }
        
        // Get the algorithm class name from the algorithm record
        final String    strClsAlg = recAlgorithm.stringValueForKey( Tracker.ATTRTAG_TYPE );
        
        
        // Create the algorithm instance and load it with data
        IAlgorithm  algorithm;      // the new algorithm to create and initialize
        try {
            Class<?> clsTracker = Class.forName(strClsAlg);
            algorithm = (IAlgorithm) clsTracker.newInstance();
            
        } catch (Exception e) {
            e.printStackTrace();
            throw new DataFormatException("Tracker.newFromEditContext(): unknow algorithm type " + strClsAlg);
            
        }
        algorithm.load(strPrimKeyVal, ecXalGlobal);
        
        return algorithm;
    }
    
    

    
    /*
     * Local Attributes
     */

    /** string type identifier of this algorithm */
    private String              m_strType;
    
    /** version of this algorithm */
    private int                 m_intVersion;
    
    /** 
     * List of all probe classes recognized by this algorithm 
     */
    private List<Class<? extends IProbe>>   m_lstProbes;

    /** 
     * flag to track the beam phase in multi gap cavities 
     */
    private boolean            m_bolCalcRfGapPhase = false;

    /*
     * Tracker Settings
     */
     
    /**
     * The frequency of storing probe states
     */
    private int         m_enmUpdatePolicy = Tracker.UPDATE_ALWAYS;
    
    /**
     * Debugging output flag
     */
    private boolean     m_bolDebug = false;
    
    /** 
     * id of element from which to start propagation 
     */
    private String      m_strElemStart = null;
    
    /** 
     * id of element at which to stop propagation 
     */
    private String      m_strElemStop = null;
    
    /**
     * Flag indicating that propagation should stop at the entrance of
     * the stop element.
     */
    private boolean     bolInclStopElem = true;
    
    /** 
     * have we started propagating 
     */
    private boolean     m_bolIsStarted = true;
    
    /**
     * has stop element already been encountered in propagation?
     */
    private boolean     m_bolIsStopped = false;
    
    

    /*
     * Tracker State 
     */    
     
    /** 
     * The tracking position of the within the current element    
     */
    private double      m_dblPosElem = 0.0;
    
    /**
     * Class type of the current probe.
     * @deprecated This property is never used 
     */
    @Deprecated
    private Class<? extends IProbe>         probeType;
    
    /*
     * Initialization
     */
    
    
    /**
     *  <p>Creates a new, empty, instance of Tracker.</p>
     *
     *  <p>
     *  Note that if the child algorithm may handle more than one probe type the 
     *  additional types should be registered using the method registerProbeType()
     *  in the constructor.
     *  </p>
     *
     *  @param      strType         string type identifier of algorithm
     *  @param      intVersion      version of algorithm
     *  @param      clsProbeType    class object for probe handled by this algorithm.
     */
    protected Tracker(String strType, int intVersion, Class<? extends IProbe> clsProbeType) {
        this.m_strType = strType;
        this.m_intVersion = intVersion;
        this.m_lstProbes = new ArrayList<Class<? extends IProbe>>();
        this.probeType = clsProbeType;
        this.m_bolDebug = false;
        
        this.registerProbeType(clsProbeType);
    };
    
    /**
     * Copy constructor for Tracker
     *
     * @param       sourceTracker   Tracker that is being copied
     */
    protected Tracker(Tracker sourceTracker) {
        this(sourceTracker.m_strType, sourceTracker.m_intVersion, sourceTracker.probeType);
        
        this.m_bolCalcRfGapPhase = sourceTracker.m_bolCalcRfGapPhase;
        this.m_enmUpdatePolicy = sourceTracker.m_enmUpdatePolicy;
        this.m_bolDebug = sourceTracker.m_bolDebug;
        this.m_strElemStart = sourceTracker.m_strElemStart;
        this.m_strElemStop = sourceTracker.m_strElemStop;
        this.m_bolIsStopped = sourceTracker.m_bolIsStopped;
        this.m_bolIsStarted = sourceTracker.m_bolIsStarted;
        this.m_dblPosElem = sourceTracker.m_dblPosElem;
    }
    
    /**
     * Creates a deep copy of Tracker
     */
//    @Override
//    public Tracker copy() {
//        return new Tracker( this );
//    }
    
    
    /**
     * Set the frequency of probe trajectory updates.
     * 
     * @param enmPolicy  probe update policy enumeration code
     */
    public void setProbeUpdatePolicy(int enmPolicy) {
        this.m_enmUpdatePolicy = enmPolicy;
    }
    
    /**
     * Set or clear the debugging mode flag.  
     * 
     * @param bolDebug
     */
    public void setDebugMode(boolean bolDebug)  {
        this.m_bolDebug = bolDebug;
    }
    
    
    
    
    
    /*
     * Accessing
     */
    
    
    /**
     * Return the probe trajectory updating policy.
     * 
     * @return  enumeration code for the update policy
     */ 
    public int  getProbeUpdatePolicy()  {
        return this.m_enmUpdatePolicy;
    }
    
    /**
     * Indicate whether or not algorithm is in debug mode.
     *  
     * @return  <code>true</code> if in debug mode
     */
    public boolean getDebugMode()   {
        return this.m_bolDebug;
    }
    
    /**
     * Returns the flag that indicates whether or not the stop
     * element is propagated through.
     * 
     * @return  <code>true</code> indicates propagation stops after the stop element,
     *          <code>false</code> indicates propagation stops before the stop element (entrance)
     *
     * @author Christopher K. Allen
     * @since  Oct 20, 2014
     */
    public boolean  isStopElementIncluded() {
        return this.bolInclStopElem;
    }
    
//    /**
//     * TODO CKA - Remove, never used.
//     * 
//     * @author Christopher K. Allen
//     * @since  Oct 20, 2014
//     */
//    public Class<? extends IProbe> getProbeType() {
//        return probeType;
//    }
    
    
    /*
     *  Abstract Methods
     */
    
    /**
     * <p>
     * The implementation must propagate the probe through the element 
     * according to the dynamics of the 
     * specific algorithm.  Derived classes must implement this method but the
     * <code>Tracker</code> base provided convenient methods for this implementation.
     * </p>
     * <p>
     * NOTE:
     * <br/>The protected method 
     * <code>advanceProbe(IProbe, IElement, double)</code>
     * is available for derived classes.  It is a convenience method
     * for performing many of the common tasks in the forward propagation 
     * of any probe.  Thus, its use is not required.
     * </p>
     *
     *  @param  probe   probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     * 
     * @author Christopher K. Allen
     * @since  Oct 20, 2014
     */
    public Class<? extends IProbe> getProbeType() {
        return probeType;
    }
    
    
    /*
     *  IAlgorithm Interface
     */

    /** 
     * Indicates whether to calculate the beam phase in multi gap Rf cavities,
     * (a la Parmila) rather than use default values (a  la Trace   3D)
     * 
     * @return <code>true</code> if phase calculations are made, <code>false</code> otherwise
     */  
    public boolean getRfGapPhaseCalculation() { return m_bolCalcRfGapPhase;}


    /**
     *  Return the algorithm type.
     *  
     *  @return     name of the integration algorithm
     */
    @Override
    public String getType() { return m_strType; };
    
    /** 
     *  Returns the version number of this algorithm
     *
     *  @return     version number of the integration algorithm 
     */
    @Override
    public int getVersion() { return m_intVersion; };
    
    /**  
     *  Check if probe can be handled by this algorithm.
     *
     *  @param  ifcProbe    probe interface to be validated
     *  @return             true if algorithm supports the probe type
     */
    public boolean validProbe(IProbe ifcProbe)  {
        return m_lstProbes.contains( ifcProbe.getClass() );
    }
    
    /**
     * Get the modeling element string identifier where propagation is to start.
     * 
     * @return  string id if element is defined, null otherwise
     */
    @Override
    public String getStartElementId() {
        return m_strElemStart;
    }

    /**
     * Get the modeling element string identifier where propagation is to stop.
     * 
     * @return  string id if element is defined, null otherwise
     */
    @Override
    public String getStopElementId() {
        return m_strElemStop;
    }

    
    
    /** 
     * Toggle the RF phase calculation on or off. 
     * 
     * @param   tf  flag for turning on/off the phase calculations
     */
    @Override
    public void setRfGapPhaseCalculation(boolean tf) { m_bolCalcRfGapPhase=tf;}

    /**
     * Sets the element from which to start propagation.
     * 
     * @param id <code>String</code> id of the element from which to start propagation
     */
    @Override
    public void setStartElementId(String id) {
        this.m_strElemStart = id;
        
        if (id == null) {
            this.m_bolIsStarted = true;
            this.m_bolIsStopped = false;

        } else  {
            this.m_bolIsStarted = false;
            this.m_bolIsStopped = false;
        }
    }

    /**
     * Sets the element at which to stop propagation.
     * 
     * @param id <code>String</code> id of the element at which to stop propagation
     */
    @Override
    public void setStopElementId(String id) {
        this.m_strElemStop = id;
        this.m_bolIsStopped = false;
    }
    
    /**
     * Sets the flag that determines whether or not the
     * propagation stops at the entrance of the stop element (if set),
     * or at the exit of the stop node.  The later case is the default.
     *  
     * @param bolInclStopElem    propagation stops after stop element if <code>true</code>,
     *                           before the stop element if <code>false</code>
     *
     * @author Christopher K. Allen
     * @since  Oct 20, 2014
     */
    @Override
    public void setIncludeStopElement(boolean bolInclStopElem) {
        this.bolInclStopElem = bolInclStopElem; 
    }

    /**
     * reset the Start Element Id to null
     */
    @Override
    public void unsetStartElementId() {
        setStartElementId(null);
    }

    /**
     * reset the Stop Element Id to null
     */
    @Override
    public void unsetStopElementId() {
        setStopElementId(null);
    }

    /**
     * Initializes the algorithm to begin a new propagation cycle.
     * 
     * @see xal.model.IAlgorithm#initialize()
     */
    @Override
    public void initialize()    {
        if (this.getStartElementId() == null)   {
            this.m_bolIsStarted = false;
            
        }   else {
            this.m_bolIsStarted = true;
        }

        this.m_bolIsStopped = false;
    }

    /**
     * <h3>Propagates the probe through the element</h3>
     * 
     * <p>
     * <strong>NOTE:</strong> &nbsp; CKA
     * <br>
     * &middot; We might get a significant performance upgrade by 
     * eliminating the internal call to 
     * <code>{@link #validProbe(IProbe)}</code>.
     * <br>
     * &middot; The method <code>validElement(IElement)}</code>
     * needs to be called here in the current implementation.  It is 
     * called for every element - Is there a better (faster) way?
     * </p>
     * 
     *  @param  probe   probe to propagate
     *  @param  elem    element acting on probe
     *
     *  @exception  ModelException  invalid probe type or error in advancing probe
     */
    @Override
    public void propagate(IProbe probe, IElement elem) throws ModelException {
        
//        if (!this.validProbe(probe))        // wrong probe type for algorithm
//            throw new ModelException("Tracker::propagate() - cannot propagate, invalid probe type.");

        if (!this.validElement(elem))       // check if we can propagate with this algorithm
            return;
        
       probe.setCurrentElement(elem.getId());
       probe.setCurrentHardwareId(elem.getHardwareNodeId());
//     sako *** IMPORTANT CHANGES
//        this is now moved to Element.propagate this.setElemPosition(0.0);
        
        if ((this.getProbeUpdatePolicy() & Tracker.UPDATE_ENTRANCE) == Tracker.UPDATE_ENTRANCE)
            probe.update();
              
        doPropagation(probe, elem);
        
        if ((this.getProbeUpdatePolicy() & Tracker.UPDATE_EXIT) == Tracker.UPDATE_EXIT)
            probe.update();
    };

    

    /*
     * IContextAware Interface
     */
    
    /**
     * Load the parameters of this <code>IAlgorithm</code> object from the
     * table data in the given <code>EditContext</code>.  
     * 
     * Here we load only the parameters specific to the base class.  It is expected
     * that Subclasses should override this method to recover the data particular 
     * to there own operation.
     * 
     * @param   strPrimKeyVal   primary key value specifying the name of the data record
     * @param   ecTableData     EditContext containing table data
     * 
     * @see xal.tools.data.IContextAware#load(String, EditContext)
     */
    @Override
    public void load(final String strPrimKeyVal, final EditContext ecTableData) throws DataFormatException {
        
        // Get the algorithm class name from the EditContext
        DataTable     tblAlgorithm = ecTableData.getTable( Tracker.TBL_LBL_ALGORITHM );
        GenericRecord recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME,  strPrimKeyVal );

        if ( recTracker == null ) {
            recTracker = tblAlgorithm.record( Tracker.TBL_PRIM_KEY_NAME, "default" );  // just use the default record
        }

        final boolean   bolDebug  = recTracker.booleanValueForKey( Tracker.ATTRTAG_DEBUG );
        final int       enmUpdate = recTracker.intValueForKey( Tracker.ATTRTAG_UPDATE );
        final boolean   bolCalcRf = recTracker.booleanValueForKey( Tracker.ATTRTAG_RFGAP_PHASE );
        
        this.setDebugMode(bolDebug);
        this.setProbeUpdatePolicy(enmUpdate);
        this.setRfGapPhaseCalculation(bolCalcRf);
    }    

    

    /*
     * IArchive Interface
     */
     

    /**
     * Load the state and settings of this algorithm from a data source
     * exposing the <code>DataAdaptor</code> interface.  Subclasses should
     * override this method to recover the data particular to there own
     * operation.
     * 
     * @param   daSource     data source containing algorithm configuration
     * 
     * @throws  DataFormatException     bad format in algorithm data
     * 
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        
        // Make sure we have proper data source
        String  strDataName = daSource.name();
        if ( !strDataName.equals(Tracker.NODETAG_ALG) )
            throw new DataFormatException("Tracker#load() - wrong data source " + strDataName);

        // Get the data node for the base class parameters
        DataAdaptor daTracker = daSource.childAdaptor(NODETAG_TRACKER);
        if (daTracker == null)
            throw new DataFormatException("Tracker#load() - missing data node " + Tracker.NODETAG_TRACKER);
        
        if (daTracker.hasAttribute(ATTRTAG_UPDATE))
            this.setProbeUpdatePolicy( daTracker.intValue(ATTRTAG_UPDATE) );
            
        if (daTracker.hasAttribute(ATTRTAG_DEBUG))
            this.setDebugMode( daTracker.booleanValue(ATTRTAG_DEBUG) );
 
        if (daTracker.hasAttribute(ATTRTAG_RFGAP_PHASE))
            m_bolCalcRfGapPhase = daTracker.booleanValue(ATTRTAG_RFGAP_PHASE);
    }

    /**
     * Save the state and settings of this algorithm to a data source 
     * exposing the <code>DataAdaptor</code> interface.  Subclasses should
     * override this method to store the data particular to there own 
     * operation.
     * 
     * @param   daptArchive     data source to receive algorithm configuration
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daptArchive) {
        
        DataAdaptor daptAlg = daptArchive.createChild(NODETAG_ALG);
        daptAlg.setValue(ATTRTAG_TYPE, this.getType());
        daptAlg.setValue(ATTRTAG_VER, this.getVersion());
        
        DataAdaptor daptTrack = daptAlg.createChild(NODETAG_TRACKER);
        daptTrack.setValue(ATTRTAG_DEBUG, this.getDebugMode());
        daptTrack.setValue(ATTRTAG_UPDATE, this.getProbeUpdatePolicy());
        daptTrack.setValue(ATTRTAG_RFGAP_PHASE, this.getRfGapPhaseCalculation());
    }
     
    
    /*
     * Propagation Dynamics
     */    
    
    /**
     *  <p>
     *  Convenience method for common propagation dynamics for all probes.  
     *  This method performs
     *  propagation tasks that are common to all probes.
     *  </p>
     *  <p>
     *  Advances the position, time, and the kinetic energy of the probe through
     *  a subsection of the specified element of the specified length.
     *  </p>
     *
     *  @param  probe       target probe whose position and energy will be modified.
     *  @param  elem        element acting on probe
     *  @param  dblLen      length of element subsection to advance through
     *
     *  @exception  ModelException    unable to advance probe through element
     */
    protected void advanceProbe(IProbe probe, IElement elem, double dblLen) 
        throws ModelException 
    {

        // Initial conditions of the probe
        double  s0   = probe.getPosition();
        double  t0   = probe.getTime();
        double  phi0 = probe.getLongitinalPhase();
        double  W0   = probe.getKineticEnergy();
        
        // Properties of the element
        double  dL   = dblLen;
        double  dT   = elem.elapsedTime(probe, dblLen);
        double  dphi = elem.longitudinalPhaseAdvance(probe, dblLen);
        double  dW   = elem.energyGain(probe, dblLen);
        
        // Advance the probe position and energy
        double  s1   = s0 + dL;
        double  t1   = t0 + dT;
        double  phi1 = phi0 + dphi;
        double  W1   = W0 + dW;
        
        probe.setPosition(s1);
        probe.setTime(t1);
        probe.setLongitudinalPhase(phi1);
        probe.setKineticEnergy(W1);
        
        // The algorithm "element position" is also set in Element#propagate() ??!!
        this.setElemPosition(this.getElemPosition() + dL);

        // Update probe trajectory
        if (this.getProbeUpdatePolicy() == Tracker.UPDATE_ALWAYS)
            probe.update();
    };
    
    /** 
     * <p>Override of xal.model.alg.Tracker#advanceProbe(xal.model.IProbe, xal.model.IElement, double)</p>
     * 
     * <p>
     * This method is the <b>converse</b> of 
     * <code>advanceProbe(IProbe, IElement, double)</code>.
     * Rather than forward propagating the probe through an 
     * <code>IElement</code> object, the method back-propagates the
     * probe object. These effects include the <em>loss</em> of 
     * kinetic energy, the <em>decrement</em> of lattice position, etc.
     * </p>
     * <p>
     * NOTES: CKA
     * <br>
     * &middot; In order to use this method the derived class
     * must override the <code>propagate</code> method, since it default
     * implementation forward propagates the probe.
     * </p>
     * 
     * @param probe     beam probe to propagate backwards 
     * @param elem      element through which beam is backwards propagated
     * @param dblLen    distance to backward propagate the beam
     * 
     * @throws ModelException error occured while trying to update the probe values (unlikely)
     *
     * @author Christopher K. Allen
     * @since Feb 3, 2009
     *
     * @see #advanceProbe(IProbe, IElement, double)
     */
    protected void retractProbe(IProbe probe, IElement elem, double dblLen)
        throws ModelException 
    {

        // Initial conditions of the probe
        double  h0 = this.getElemPosition();    // position within element
        double  s0 = probe.getPosition();       // position within sequence
        double  t0 = probe.getTime();           // time in sequence
        double  W0 = probe.getKineticEnergy();  // total kinetic energy

        // Properties of the element
        double  dh  = dblLen;                           // change in position
        double  dt = elem.elapsedTime(probe, dblLen);   // change in time
        double  dW = elem.energyGain(probe, dblLen);    // change in energy

        // Retreat the probe position and energy
        double  h1 = h0 - dh;
        double  s1 = s0 - dh;
        double  t1 = t0 - dt;
        double  W1 = W0 - dW;

        this.setElemPosition(h1);
        probe.setPosition(s1);
        probe.setTime(t1);
        probe.setKineticEnergy(W1);
        
        // Update probe trajectory
        if (this.getProbeUpdatePolicy() == Tracker.UPDATE_ALWAYS)
            probe.update();
    }


    
    /**
     * <p>
     * Check if the specified element is requires probe propagating.  That is,
     * is the element within the specified start and stop boundaries defined
     * with this algorithm object.
     * </p>
     * <p>
     * NOTE: CKA
     * <br>
     * &middot; Maybe there is a better way of using start and stop
     * elements.
     * </p>
     * 
     * @param       elem    element interface to validate propagation
     * @return      <b>true</b> if we propagate, <b>false</b> if not
     */
    protected boolean validElement(IElement elem) {

        // Check if we're already stopped
        if (this.m_bolIsStopped) {  
            return false;
        }

        // Check if there is a starting element defined and 
        if (!this.m_bolIsStarted) {                     // we haven't started propagating yet         
               
            if (this.getStartElementId().equals(elem.getId()) 
            		|| this.getStartElementId().equals("BEGIN_"+elem.getParent().getId())) {  // IL: backward compatibility with BEGIN_section markers  
            	// reached the starting element
                this.m_bolIsStarted = true;                
                
            } else {                // we haven't started and we haven't reached the start element
                return false;
                
            }
        }


        // Check if this is the last element to propagate (still propagate, but set flag) 
        if (this.getStopElementId() != null )
            if (this.getStopElementId().equals(elem.getId())) {
                this.m_bolIsStopped = true;
                
                if(this.isStopElementIncluded() == false)
                    return false;
            }
        
        // No stopping criterion encountered
        return true;
    }
        

    
    
    

    
    /*
     *  Support Functions
     */
    
    /**
     *  Register the class of a probe recognized by this algorithm.  This method should
     *  be called in the constructor of all derived classes for each additional probe that 
     *  the algorithm may propagate.
     *
     *  @param  clsProbeType    class object of probe which this algorithm can propagate
     */
    protected void  registerProbeType(Class<? extends IProbe> clsProbeType)   {
        m_lstProbes.add(clsProbeType);
    }
    
    
     /**
      * Return the current position within the element through which
      * the probe is being propagated
      * 
      * @return     the current element position in <b>meters</b>
      */
    protected double getElemPosition() {
        return m_dblPosElem;
    }
    
    /**
     * Set the current position within the element though which the 
     * probe is being propagated.
     * 
     * @param dblPosElem    current element position in <b>meters</b>
     */
    public void setElemPosition(double dblPosElem) {
        this.m_dblPosElem = dblPosElem;
    }

}



/*
 *  Storage
 */


    
///**
//*  All derived algorithms must implement this method for advancing the state
//*  of supported probes.
//*  This method advances the state of the probe through the provided element.
//*
//*  @param  probe       probe whose state is to be advanced
//*  @param  elem        element that acts on probe
//*  @param  dblLen      length of element subsection to advance
//*
//*  @exception  ModelException    unable to advance probe state
//*/
//protected abstract void advanceState(IProbe probe, IElement elem, double dblLen) 
//throws ModelException;
        
///**
//* Returns the number of sections to break the specified element into for
//* propagation.
//* 
//* @param elem element currenly acting on probe
//* 
//* @return integer indicating number of element subsections
//*/
//protected abstract int elementSubsections(IElement elem);


///**
//* Propagates the probe through the element.
//*
//*  @param  probe   probe to propagate
//*  @param  elem    element acting on probe
//*
//*  @exception  ModelException  invalid probe type or error in advancing probe
//*/
//public void propagate(IProbe probe, IElement elem) throws ModelException {
//        
//  if (!this.validProbe(probe))
//      throw new ModelException("Tracker::propagate() - cannot propagate, invalid probe type.");
//        
//  double currentPosition = 0;
//  double nextInterval;
//  do {
//    nextInterval = nextIntervalFrom(elem, currentPosition);
//    System.out.println("propagate elem: " + elem.getId() + " currPos: " + currentPosition + " nextInterval: " + nextInterval);
//    this.advanceState(probe, elem, nextInterval);
//    this.advanceProbe(probe, elem, nextInterval);
//    probe.update();
//    currentPosition += nextInterval;
//  } while (currentPosition < elem.getLength());
//};


