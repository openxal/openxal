/*
 * Probe.java
 *
 * Created on August 12, 2002, 5:15 PM
 */

package xal.model.probe;


import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.model.IAlgorithm;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.model.xml.ParsingException;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;


/**
 *  Provides a base class implementation of the IProbe interface that is useful for
 *  most standard probe types.  This class provides most of the functionality necessary
 *  for the implementation of the IProbe interface, <bold>except</bold> a definition
 *  and implementation of a probe "state".  Thus, it is up to base classes to provide
 *  and implement the particular aspect of a beam the probe represents.
 *
 *  
 *
 * @author  Christopher K. Allen
 */
public abstract class Probe<S extends ProbeState<S>> implements IProbe, IArchive {
    /*
     * global attributes
     */
     
    /* for archive operations*/
    
    /** element tag for probe data */
    public static final String PROBE_LABEL = "probe";
    
    /** attribute tag for probe type name */ 
    protected final static String TYPE_LABEL = "type";
    
    /** attribute tag for the time stamp*/
    protected final static String TIME_LABEL = "time";
    
    
    /** element tag for comment data */
    protected static final String COMMENT_LABEL = "comment";
    
    /** attribute tag for the date */
    private static final String DATE_LABEL = "date";

    /** attribute tag for author */
    private static final String AUTHOR_LABEL = "author";
    
    /** attribute tag for comment text */
    protected static final String TEXT_LABEL = "text";

    
    /** data node label for algorithm data */
    private static final String  NODETAG_ALG = "algorithm";
    

    
    
    /** attribute tag for time stamp data */    
//    private static final String TIMESTAMP_LABEL = "timestamp";
    
    /** attribute tag for user comment data */
//    private static final String DESCRIPTION_LABEL = "description";
    
    
    /*
     *  Local Attributes
     */
    
    /** User comment associated with probe */
    private String  m_strComment = "";
    
    /** Time stamp associated with probe */
    private Date    m_dateStamp = new Date();
        

        
    /** Species charge */
    private double  m_dblParQ = 0.0;
    
    /** Species rest energy */
    private double  m_dblParEr = 0.0;
    


    /** Current lattice element probe is visiting */
    private String  m_strElemId = "";
    
    /** Current probe position in beamline */
    private double m_dblPos = 0.0;
    
    /** The time elapsed from the beginning of the tracking (sec) */
    private double m_dblTime = 0.0;
    
    /** Probe's kinetic Energy */
    private double  m_dblW = 0.0;


    
    /** toggle trajHist tracking for a probe */
    private boolean         bolTrack = true;
    
    /** initial state of probe, set when initialize is called */
    private S               stateInit;
    
    /** current state of the probe - defines the probe */
    protected S             stateCurrent;
    
    /** Current probe trajHist */
    protected Trajectory<S> trajHist;
    
    
    
    /** algorithm providing probe dynamics */
    private IAlgorithm  algTracker = null;
    


    
    /*
     *  Derived Parameters
     */
    
    /** Collective speed w.r.t. the speed of light */
    private double m_dblBeta = 0.0;
    
    /** Collective relativistic factor */
    private double m_dblGamma = 0.0;
    
    
    
    
    
    /*
     * Factory Methods
     */
    
    
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * an instance of the appropriate Probe species.
     * 
     * @param container <code>DataAdaptor</code> to read a Probe from
     * @return a Probe for the contents of the DataAdaptor
     * @throws ParsingException error encountered reading the DataAdaptor
     */
    public static Probe readFrom(DataAdaptor container)
            throws ParsingException {
                
        DataAdaptor daptProbe = container.childAdaptor(Probe.PROBE_LABEL);
        if (daptProbe == null)
            throw new ParsingException("Probe#readFrom() - no Probe data node.");
            
        String type = daptProbe.stringValue(Probe.TYPE_LABEL);
        Probe probe;
        try {
            Class<?> probeClass = Class.forName(type);
            probe = (Probe) probeClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
            throw new ParsingException(e.getMessage());
        }
        probe.load(daptProbe);
        return probe;   
    }

    /**
     * Clone factory method.  Creates a new <code>Probe</code> object, of the appropriate
     * type, initialized to the argument <code>Probe</code>.
     * 
     * NOTE: There is now a reset() method that is preferable to this one.  It
     * clears the probe trajHist and restores the initial state saved in the
     * initialize() method, without creating a new probe instance.
     * 
     * @param probeInit     <code>Probe</code> object containing initial data
     * 
     * @return              new <code>Probe</code> object initialized to argument 
     */
    public static Probe newProbeInitializedFrom( final Probe probeInit ) {
        Class<?> pClass = probeInit.getClass();
        Probe pNew;
        try {
            pNew = (Probe) pClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
            return null;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
		
		pNew.initializeFrom( probeInit );
		
        return pNew;
    }
	
	
    
    /*
     *  Abstract Methods
     */
    
    /**
     * Require concrete implementations to override this method to create a
     * <code>Trajectory</code> object of the appropriate species.
     * 
     * @return a <code>Trajectory</code> of the appropriate species for this probe type
     */
    public abstract Trajectory<S> createTrajectory();
    
    /**
     * Require concrete implementations to override this method to retrieve a 
     * <code>Trajectory</code> object of the appropriate species.
     * 
     * @return the <code>Trajectory</code> object for the given probe type
     */
    public abstract Trajectory<S> getTrajectory();
    
    /**
     * Captures the probe's state in a ProbeState of the appropriate species.
     */
    public abstract S createProbeState();
    
    
    /*
     * ---------------------------------------------------------------
     *
     * TODO 
     * 
     * Jonathan, I had to add this method for the work I am doing in 
     * ProbeState.  This should replace the abstract method 
     * createProbeState().  We'll deal with that later.
     */
    
    /**
     * This method returns a clone of the current state of this probe.
     * That is, the <code>ProbeState</code> object is a representation of this
     * probe at the moment this method was called.
     * 
     * @return  a deep copy of the current state of this probe
     *
     * @author Christopher K. Allen
     * @since  Jun 26, 2014
     */
    public S cloneCurrentProbeState() {
        return this.stateCurrent.copy();
    }
    
    /*
     * ---------------------------------------------------------------
     */
   
    
    /**
     * Creates a deep copy of the probe
     */
    public abstract Probe<S> copy();
    
    /**
     * Apply the contents of ProbeState to update my current state.  Subclass
     * implementations should call super.applyState to ensure superclass
     * state is applied.
     * 
     * @param state     <code>ProbeState</code> object containing new probe state data
     */
    public void applyState(ProbeState state) {
        setSpeciesRestEnergy(state.getSpeciesRestEnergy());
        setSpeciesCharge(state.getSpeciesCharge());

        setCurrentElement(state.getElementId());
        setPosition(state.getPosition());
        setTime(state.getTime());
        setKineticEnergy(state.getKineticEnergy());
    }
    
    
    /**
     *  <p>
     *  Load the "state" information of a particle from a data archive represented by a
     *  <code>DataAdaptor</code> interface.  Each derived class should know how to load
     *  its particular state information.
     *  </p>
     *  <p>
     *  The state information for a particular probe should be stored as children
     *  of the "state" data adaptor, anaologous to the XML representation.
     *  </p>
     *
     *  @param  daptState   the "state" parent adaptor containing probe data
     */
//    abstract public void loadState(DataAdaptor daptState) throws DataFormatException;
    
    /**
     *  <p>
     *  Save the "state" information to a data archive represented by a data adaptor
     *  interface.  Particular state information should be save as children of the 
     *  parent (state) data adaptor passed to this method.
     *
     *  @param  daptState   the "state" parent adaptor to receive probe data
     */
//    abstract public void saveState(DataAdaptor daptState);

    
    
    /*
     *  Initialization
     */
    
    
    /** 
     *  Creates a new instance of Probe.
     *  Since Probe is an abstract base only derived classes may call constructor.
     *
     *  @param  ifcAlg      default dynamics algorithm for probe
     */
    protected Probe() {
    }
    
    /** 
     *  Creates a new instance of Probe.
     *  Since Probe is an abstract base only derived classes may call constructor.
     *
     *  @param  ifcAlg      default dynamics algorithm for probe
     */
    protected Probe(IAlgorithm ifcAlg) {
        this.algTracker = ifcAlg;
    }
    
    /**
     *  Copy constructor for Probe.  This constructor does a deep copy of the Probe base
     *  attributes by calling member support method deepCopyProbeBase().
     *  
     *  @param  probe   Probe object to be cloned 
     */
    public Probe(Probe<S> probe)   {
        this.deepCopyProbeBase(probe);
    }
    



    /**
     * Initialize this probe from the one specified.
     * 
     * @param probe the probe from which to initialize this one
     */
    protected void initializeFrom( final Probe probe ) {
        final ProbeState initialState = probe.getTrajectory().initialState();
        if ( initialState != null ) {
            applyState( initialState );         
        }
        
        setAlgorithm( probe.getAlgorithm() );
        setTimestamp( new Date() );
        setComment( probe.getComment() );
    }
    
    

    /**
     *  Provide a user comment associated with the probe
     *
     *  @param  strComment  user comment string
     */
    public void setComment(String strComment)   { m_strComment = strComment; };

    /**
     *  Sets a time stamp for the probe.
     *
     *  @param  dateStamp   time stamp for probe
     */
    public void setTimestamp(Date dateStamp)    { m_dateStamp = dateStamp; };

    


    /** 
     *  Set the charge of the particle species in the beam 
     *  
     *  @param  q       species particle charge in units of positive electron charge
     */
    public void setSpeciesCharge(double q) { m_dblParQ = q; };
    
    
    /** 
     *  Set the rest energy of a single particle in the beam 
     *
     *  @param  Er      particle rest energy (<b>electron-volts</b>)
     */
    public void setSpeciesRestEnergy(double Er) { m_dblParEr = Er; };



    /**
     *  Set the algorithm defining the probes dynamics through elements
     *
     *  @param  ifcAlg   object exposing the IAlgorithm interface
     */
    public boolean setAlgorithm(IAlgorithm ifcAlg) { 
        if (!ifcAlg.validProbe(this)) return false;
        
        algTracker = ifcAlg;
        return true;
    };
   
    /**
     *  Set particle trajHist tracking for probes.
     *
     *  @param  bolTrack    turn tracking on or off
     */
    public void setTracking(boolean bolTrack) { this.bolTrack = bolTrack; };

    
    
    /*
     *  Data Query
     */
    
    /**
     *  Returns the comment string associated with the probe
     *
     *  @return     comment string
     */
    public String   getComment()                { return m_strComment; };
    
    /**
     *  Returns the time stamp of the probe.
     *
     *  @return     time stamp
     */
	@NoEdit	// editors should not access this property
    public Date     getTimestamp()              { return m_dateStamp; };



    
//    /**
//     *  Get the state history of the probe.
//     * 
//     *  @return     Trajectory object of the proper sub-type for the probe type 
//     */
//	@NoEdit	// editors should not access this property
//    public Trajectory<? extends ProbeState> getTrajectory() {
//        if (trajHist == null) {
//            this.m_trajHist = createTrajectory();
//        }
//        return trajHist; 
//    }
    
    
      
    
    /*
     *  IProbe Interface
     */
    
    /** 
     *  Returns the charge of probe's particle species 
     *  
     *  @return     particle species charge (<b>Coulombs</b>)
     */
    public double getSpeciesCharge() { return m_dblParQ; }
    
    /** 
     *  Returns the rest energy of particle species 
     *
     *  @return     particle species rest energy (<b>electron-volts</b>)
     */
	@Units( "eV" )
    public double getSpeciesRestEnergy() { return m_dblParEr; }
    
    /** Returns the momentum
     * 
     * @return particle momentum
     */
    public double getMomentum() {
    	return (getSpeciesRestEnergy()*getGamma()*getBeta());
    }
    
    /**
     * Returns the id of the current lattice element that the probe is visiting.
     * 
     * @return id of current lattice element
     */
    public String getCurrentElement() { return m_strElemId; };
    
    /** 
     *  Returns the current beam-line position of the probe 
     *  
     *  @return     probe position (<b>meters</b>)
     */
	@Units( "meters" )
    public double getPosition() { return m_dblPos; };
    
    /**
     * Return the time elapsed since the probe began propagation.
     * 
     * @return      elapsed time in <b>seconds</b>
     */
	@Units( "seconds" )
    public double   getTime()   {
        return this.m_dblTime;
    }
    
    /**
     *  Return the kinetic energy of the probe.  Depending upon the probe type,
     *  this could be the actual kinetic energy of a single constituent particle,
     *  the average kinetic energy of an ensemble, the design energy, etc.
     *
     *  @return     probe kinetic energy    (<b>electron-volts</b>)
     */
	@Units( "eV" )
    public double getKineticEnergy()   { return m_dblW; };

    
    /** 
     *  Returns the probe velocity normalized to the speed of light. 
     *
     *  @return     normalized probe velocity v/c (<b>unitless</b>
     */
    public double getBeta() { return m_dblBeta;  }
    
    /** 
     *  Returns the relativistic parameter corresponding to the probe 
     *  velocity.
     *  The relativistic factor is given by the formulae
     *      gamma =  (Kinetic Energy/Rest Energy) + 1 
     *            = sqrt[1/(1-v^2/c^2)]
     *
     *  @return     probe relatistic factor (<b>unitless</b>)
     */
    public double getGamma() { return m_dblGamma; }


    
    /**
     * Set the current lattice element id.
     * 
     * @param id  element id of current lattice element
     */
    public void setCurrentElement(String id) {m_strElemId = id; };
    
    /** 
     *  Set the current position of the probe along the beamline.
     *
     *  @param  s       new probe position (<b>meters</b>)
     *
     *  @see    #getPosition
     */
    public void setPosition(double s)  { m_dblPos = s; };
    
    /** 
     * Set the current time since the probe began propagating
     * 
     * @param   dblTime     new probe current time in <b>seconds</b>
     * 
     * @author jdg
     */
	@NoEdit	// editors should not edit this property
    public void setTime(double dblTime) {
        this.m_dblTime = dblTime;
    }
    

    /**
     *  Set the current kinetic energy of the probe.
     *
     *  @param  W       new probe kinetic energy (<b>electron-volts</b>)
     *
     *  @see    #getKineticEnergy
     */
    public void setKineticEnergy(double W)    { 
        m_dblW = W; 
        m_dblGamma = this.computeGammaFromW(m_dblW);
        m_dblBeta = this.computeBetaFromGamma(m_dblGamma);
    };




//    /**
//     * Set the element from which to start propagation.
//     * 
//     * @param id <code>String</code> id of element to start propagation from
//     */
//    public void setStartElementId(String id) {
//        getAlgorithm().setStartElementId(id);
//    }
//
//    /**
//     * Set the element at which to stop propagation.
//     * 
//     * @param id <code>String</code> id of the element at which to stop propagation
//     */
//    public void setStopElementId(String id) {
//        getAlgorithm().setStopElement(id);
//    }
//
    /**
     * Initializes the probe, resetting state as necessary.
     */
    public void initialize() {
    	this.stateInit = this.createProbeState();
    	this.stateCurrent = this.stateInit;
    	
        this.trajHist = this.createTrajectory();
//        this.getAlgorithm().initialize();  // CKA - I think these should be uncommented
    }
    
    /**
     * Resets the probe to the saved initial state, if there is one and clears
     * the trajHist.
     */
    public void reset() {
    	if (stateInit != null) { 
    		this.applyState(stateInit);
    		this.stateCurrent = stateCurrent.copy();
    	}
    	this.trajHist = this.createTrajectory();
//        this.getAlgorithm().initialize(); // CKA - I think these should be uncommented
    }

    /**
     *  Save the probe state into trajHist.
     */
    public void update() throws ModelException  {
    	
        if (!bolTrack) return;
        
        this.getTrajectory().update(this);
    };
	
	
	/**
	 * Subclasses should override this method to perform any required post processing upon completion 
	 * of algorithm processing.  This method implementation does nothing.
	 */
	public void performPostProcessing() {
	}

   

    /**
     *  Return the algorithm defining the probes dynamics.
     *
     *  @return         interface to probe dynamics
     */
    public IAlgorithm getAlgorithm()    { return algTracker; };


    /**
     * Return the archiving interface for this object.
     * 
     * @see xal.tools.data.IArchive
     */
	@NoEdit	// hide this property so it doesn't appear in editors
    public IArchive getArchive()        { return this; };

    
    // Object Overrides ========================================================
    
    
//	/**
//	 * "Borrowed" implementation from AffineTransform, since it is based on
//	 * double attribute values.  Must implement hashCode to be consistent with
//	 * equals as specified by contract of hashCode in <code>Object</code>.
//	 * 
//	 * @return a hashCode for this object
//	 */
//	public int hashCode() {
//	   long bits = Double.doubleToLongBits(getElem(0));
//	   bits = bits * 31 + Double.doubleToLongBits(getElem(1));
//	   bits = bits * 31 + Double.doubleToLongBits(getElem(2));
//	   bits = bits * 31 + Double.doubleToLongBits(getElem(3));
//	   bits = bits * 31 + Double.doubleToLongBits(getElem(4));
//	   bits = bits * 31 + Double.doubleToLongBits(getElem(5));
//	   return (((int) bits) ^ ((int) (bits >> 32)));
//	}           
        
    
    /*
     *  IArchive Interface
     */
    
    /** 
     *  Load the contents of a probe from an data archive represented by
     *  a <code>DataAdaptor</code> interface.
     *
     *  @param  daptSource   data archive containing probe info
     *
     *  @exception  DataFormatException     bad probe type, missing child data node, or bad number format
     */
    public void load(DataAdaptor daptSource) throws DataFormatException {
        
        // Make sure we have the correct data source
        String strDataName = daptSource.name();
        if ( !strDataName.equals(Probe.PROBE_LABEL) )
            throw new DataFormatException("Probe#load() - not a probe data source " + strDataName);
        DataAdaptor daptProbe = daptSource;
            
        // Check for proper probe type 
        String  strType = daptProbe.stringValue(Probe.TYPE_LABEL);
        String  strClass = this.getClass().getName();
        if ( !strType.equals(strClass) )
            throw new DataFormatException("Probe#load() - data adaptor has wrong probe type: " + strType);
            
        // Get time stamp
        DateFormat  frmDate = DateFormat.getDateTimeInstance();
        
        if (daptProbe.hasAttribute(Probe.TIME_LABEL)) 
            try {
            String strTStamp = daptProbe.stringValue(TIME_LABEL);
            this.setTimestamp( frmDate.parse(strTStamp) );
            } catch (ParseException e) {};

//        try {
//            String  strTStamp = daptProbe.stringValue("date");
//            if (strTStamp != null) this.setTimestamp( frmDate.parse( strTStamp ) );
//        } catch (ParseException e)  {
//        }
        
        // Load any comments
        DataAdaptor daptComm = daptProbe.childAdaptor(Probe.COMMENT_LABEL);
        if (daptComm != null) {
            String strAuth = daptComm.stringValue(Probe.AUTHOR_LABEL);
            String strDate = daptComm.stringValue(Probe.DATE_LABEL);
            String strText = daptComm.stringValue(Probe.TEXT_LABEL);
                
            String strComm = strAuth + "|" + strDate + "|" + strText;
            
            this.setComment(strComm);
        }
        
        
        // Load the probe algorithm
        DataAdaptor daptAlg = daptProbe.childAdaptor(Probe.NODETAG_ALG);
        if (daptAlg == null)
            throw new DataFormatException("Probe#load() - no algorithm data");
            
        IAlgorithm  ifcAlg  = Tracker.newInstance(daptAlg);
        this.setAlgorithm(ifcAlg);
            
        
        // Load state data
        DataAdaptor daptState   = daptProbe.childAdaptor(ProbeState.STATE_LABEL);
        if (daptState == null) 
            throw new DataFormatException("Probe#load() - no state data");
        ProbeState state;
        try {
            state = readStateFrom(daptState);
        } catch (ParsingException e) {
            throw new DataFormatException("Probe#load() - exception parsing state element");
        }
        this.applyState(state);
    };
    
    /**
     * Read the contents of the supplied <code>DataAdaptor</code> and return
     * an instance of the appropriate Trajectory species.
     * 
     * @param container <code>DataAdaptor</code> to read a Trajectory from
     * @return a ProbeState for the contents of the DataAdaptor
     * @throws ParsingException error encountered reading the DataAdaptor
     */
    protected abstract ProbeState readStateFrom(DataAdaptor container) throws ParsingException;
    
  
//
//  CKA - we do not know if the Probe base class has Twiss parameters!
//
//    /**
//     *  Save the contents of a probe to a data archive represented by a 
//     *  <code>DataAdaptor</code> interface.
//     *
//     *  @param  daSink   data archive to receive probe information
//     *  @param  useTwiss    If want to dump Twiss parameters instead of correlation matrix, set it to 'true'
//     */
//    public void save(DataAdaptor daSink, boolean useTwiss)  { // CKA - we do not know if probe has twis parameters
//        
//    	DataAdaptor daProbe = daSink.createChild(Probe.PROBE_LABEL);
//    	
//        // Save the probe type information and time stamp
//        DateFormat  frmDate = DateFormat.getDateTimeInstance();
//
//        if (this.getTimestamp() == null)
//            this.setTimestamp(new Date());
//        
//        daProbe.setValue(Probe.TYPE_LABEL, this.getClass().getName());
//        daProbe.setValue(Probe.TIME_LABEL, frmDate.format( this.getTimestamp() ) );
//        
//        // Save the comment
//        DataAdaptor daptComm = daProbe.createChild(Probe.COMMENT_LABEL);
//        daptComm.setValue(Probe.TEXT_LABEL, this.getComment() );
//        
//        // Save the algorithm type
//        this.getAlgorithm().save(daProbe);
//                
//        // Save the probe state information
//        ProbeState state = createProbeState();
//        state.save(daProbe, useTwiss);  // CKA - we do not know if probe has Twiss parameters
//    };
    
    /**
     *  Save the contents of a probe to a data archive represented by a 
     *  <code>DataAdaptor</code> interface.
     *
     *  @param  daSink      data archive to receive probe information
     */
    public void save(DataAdaptor daSink) {
        
        DataAdaptor daProbe = daSink.createChild(Probe.PROBE_LABEL);
        
        // Save the probe type information and time stamp
        DateFormat  frmDate = DateFormat.getDateTimeInstance();

        if (this.getTimestamp() == null)
            this.setTimestamp(new Date());
        
        daProbe.setValue(Probe.TYPE_LABEL, this.getClass().getName());
        daProbe.setValue(Probe.TIME_LABEL, frmDate.format( this.getTimestamp() ) );
        
        // Save the comment
        DataAdaptor daptComm = daProbe.createChild(Probe.COMMENT_LABEL);
        daptComm.setValue(Probe.TEXT_LABEL, this.getComment() );
        
        // Save the algorithm type
        this.getAlgorithm().save(daProbe);
                
        // Save the probe state information
        ProbeState state = createProbeState();
        state.save(daProbe);  
    }
    
        

    /*
     *  Support Functions
     */
    
    /**
     *  This is a convenience function for derived classes when implementing their
     *  deepCopy() method.  This functions deep copies all attributes in the Probe
     *  base class.  The derived classes need only then deep copy all attributes
     *  particular to their type (e.g., state, etc.).
     *
     *  @param  probe   probe object whose Probe base is to be deep copied into this
     */
    protected void  deepCopyProbeBase(Probe probe)    {
        
        // Copy all the Probe base attributes
        this.m_dblParQ  = probe.m_dblParQ;
        this.m_dblParEr = probe.m_dblParEr;


        this.m_dblPos   = probe.m_dblPos;
        this.m_dblTime  = probe.m_dblTime;
        this.m_dblW     = probe.m_dblW;

        this.m_dblBeta  = probe.m_dblBeta;
        this.m_dblGamma = probe.m_dblGamma;
        this.bolTrack = probe.bolTrack;
        
        
        // Copy the algorithm object if we have one
        this.algTracker = null;
        final IAlgorithm algorithm = probe.getAlgorithm();
        if ( algorithm != null )   {
            this.setAlgorithm( algorithm.copy() );
        }
    };


    /** 
     *  Computes the relatavistic factor gamma from the current beta value
     *  
     *  @param  beta    speed of probe w.r.t. the speed of light
     *  @return         relatavistic factor gamma
     */
    protected double computeGammaFromBeta(double beta) { 
        return 1.0/Math.sqrt(1.0 - beta*beta); 
    };
    
    /**
     *  Convenience function for computing the relatistic factor gamma from the 
     *  probe's kinetic energy (using the particle species rest energy m_dblParEr).
     *
     *  @param  W       kinetic energy of the probe
     *  @return         relatavistic factor gamma
     */
    protected double computeGammaFromW(double W)   {
        double gamma = W/m_dblParEr + 1.0;
        
        return gamma;
    };
    
    /**
     *  Convenience function for computing the probe's velocity beta (w.r.t. the 
     *  speed of light) from the relatistic factor gamma.
     *
     *  @param beta     relatavistic factor gamma
     *  @return         speed of probe (w.r.t. speed of light)
     */
    protected double computeBetaFromGamma(double gamma) {
        double beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));

        return beta;
    };
    
    /** 
     *  Convenience function for multiplication of beta * gamma
     */
    protected double getBetaGamma() { return m_dblBeta*m_dblGamma; };
    

};
