package xal.model.probe.traj;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;

import xal.model.probe.Probe;
import xal.model.xml.ParsingException;

/**
 * Stores a snapshot of a probes state at a particular instant in time.  Concrete
 * extensions to this class should be developed for each type of probe.
 * 
 * @author Craig McChesney
 * @version $id:
 * 
 */
public abstract class ProbeState implements IProbeState {



    /*
     * Global Constants
     */	
     
    // *********** I/O Support

    /** element tag for the probe state data */        
    public final static String STATE_LABEL = "state";
    
    /** attribute tag for concrete type of probe state */
    protected final static String TYPE_LABEL = "type";    
    
    
    /** element tag for locate state data */
    private final static String LOCATION_LABEL = "location";
    
    /** attribute tag for associated lattice element */
    private final static String ELEMENT_LABEL = "elem";
    
    /** attribute tag for probe elapsed time */
    private final static String TIME_LABEL = "t";

    /** attribute tag for probe position */
    private final static String POSITION_LABEL = "s";
    
    /** attribute tag for probe kinetic energy */
    private final static String KINETICENERGY_LABEL = "W";
    
    
    
    /** element tag for particle species data */
    private final static String SPECIES_LABEL = "species";
    
    /** attribute tag for particle charge */
    private final static String PARTCHARGE_LABEL="q";
    
    /** attribute tag for particle rest energy */
    private final static String PARTRESTENERGY_LABEL="Er";
    
    /*
     * Local Attributes
     */
     
    /** Species charge */
    private double  m_dblParQ = 0.0;
    
    /** Species rest energy */
    private double  m_dblParEr = 0.0;
    

    /** element id */
    private String m_strElemId = "";

    /** Current probe position in beamline */
    private double m_dblPos = 0.0;
    	    
    /** The time elapsed from the beginning of the tracking (sec) */
     private double m_dblTime = 0.0;

    /** Probe's average kinetic Energy */
    private double  m_dblW = 0.0;

    /** Probe's relativistic gamma */
    private double  m_dblGamma = Double.NaN;
    
    
//  CKA This does not belong here
//      We have not way of knowing whether or not the  
//      derived probe class actually has Twiss parameters.
//    
//    protected boolean bolSaveTwiss = false;
//    
    
    
    
    /*
     * Initialization
     */


    /**
     *  Default constructor - creates an empty <code>ProbeState</code> object. 
     */    
    public ProbeState() {
    }
    
    /**
     * Initializing Constructor.  Creates a <code>ProbeState</code> object initialized
     * to the state of the <code>Probe</code> argument.
     * 
     * @param probe     <code>Probe</code> object containing initial values
     */
    public ProbeState(Probe probe) {
        this.setSpeciesCharge( probe.getSpeciesCharge() );
        this.setSpeciesRestEnergy( probe.getSpeciesRestEnergy() );

        this.setElementId( probe.getCurrentElement() );
        this.setPosition( probe.getPosition() );
        this.setTime( probe.getTime() );
        this.setKineticEnergy( probe.getKineticEnergy() );
    }
    
    /** 
     *  Set the charge of the particle species in the beam 
     *  
     *  @param  q       species particle charge (<b>Coulombs</b>)
     */
    public void setSpeciesCharge(double q) { 
        m_dblParQ = q; 
    }
    
    
    /** 
     *  Set the rest energy of a single particle in the beam 
     *
     *  @param  Er      particle rest energy (<b>electron-volts</b>)
     */
    public void setSpeciesRestEnergy(double Er) { 
        m_dblParEr = Er; 
    }



    /** 
     *  Set the current position of the probe along the beamline.
     *
     *  @param  s       new probe position (<b>meters</b>)
     *
     *  @see    #getPosition
     */
    public void setPosition(double s) {
    	m_dblPos = s;
    }
    
    /** 
     * Set the current probe time elapsed from the start of the probe tracking.
     *  
     * @param   dblTime     elapsed time in <b>seconds</b>
     */
    public void setTime(double dblTime) {
        m_dblTime = dblTime; 
     }

    /**
     *  Set the current kinetic energy of the probe.
     *
     *  @param  W       new probe kinetic energy (<b>electron-volts</b>)
     *
     *  @see    #getKineticEnergy
     */
    public void setKineticEnergy(double W) {
        m_dblW = W;
    }
    
    /**
     * Set the lattice element id associated with this state.
     * 
     * @param id  element id of current lattice element
     */
    public void setElementId(String id) {
        m_strElemId = id;
    }
    
//    //sako
//    public void setUseTwiss(boolean bool) {
//        bolSaveTwiss = bool;
//    }
//    


    /*
     *  Data Query 
     */

//    //sako
//    public boolean getUseTwiss() {
//        return bolSaveTwiss;
//    }
//

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
    public double getSpeciesRestEnergy() { return m_dblParEr; }
    


    /**
     * Returns the id of the lattice element associated with this state.
     * 
     * @return  string ID of associated lattice element
     */
    public String getElementId() {
        return m_strElemId;
    }
    
    /** 
     *  Returns the current beam-line position of the probe 
     *  
     *  @return     probe position (<b>meters</b>)
     */
    public double getPosition() {
        return m_dblPos;
    }
    
    /** 
     * Return the time elapsed from the start of the probe tracking
     * 
     * @return      time elapsed since probe began tracking, in <b>seconds</b> 
     */
    public double getTime() { 
        return m_dblTime;
    };
    
    /**
     *  Return the kinetic energy of the probe.  Depending upon the probe type,
     *  this could be the actual kinetic energy of a single constituent particle,
     *  the average kinetic energy of an ensemble, the design energy, etc.
     *
     *  @return     probe kinetic energy    (<b>electron-volts</b>)
     */
    public double getKineticEnergy() {
    	return m_dblW;
    }
    
    
    /**
     *  Return the relativistic gamma of the probe.  Depending upon the probe type,
     *  this could be the actual gamma of a single constituent particle,
     *  the average gamma of an ensemble, the design gamma, etc.
     *
     *  @return     probe kinetic energy    (<b>electron-volts</b>)
     */
    public double getGamma() {

        if (Double.isNaN(m_dblGamma))  {
            
            m_dblGamma = 1. + m_dblW / m_dblParEr;
        }
        
        return m_dblGamma;
    }

    
    /*
     * Debugging
     */


    /**
     *  Return a textual representation of the <code>ProbeState</code> internal state.
     * 
     *  @return     string containing current <code>ProbeState</code> state
     */
    @Override
    public String toString() {
    	return " elem: "     + getElementId() +
               " s=" + getPosition() + 
               " t=" + getTime() + 
               " W=" + getKineticEnergy();
    }
    
    
    
    /*
     * IArchive Interface
     */
// Sako,  Sorry I need to revive this
//  CKA - No way to know if Twiss parameters exist in this base class!    
//    
//    /**
//     * Save the state information to a data sink represented by 
//     * a <code>DataAdaptor</code> interface
//     * 
//     * @param   container   data source to receive state information
//     * @param  bolSaveTwiss    If want to dump Twiss parameters instead of correlation matrix, set it to 'true'
//     */
//    public void save(DataAdaptor container, boolean useTwiss) {
//        
//        DataAdaptor stateNode = container.createChild(STATE_LABEL);
//        stateNode.setValue(TYPE_LABEL, getClass().getName());
//        stateNode.setValue("id", this.getElementId());
//        
//        this.bolSaveTwiss = useTwiss;
//        addPropertiesTo(stateNode);
//    }
    
    /**
     * Save the state information to a data sink represented by 
     * a <code>DataAdaptor</code> interface
     * 
     * @param   daSink   data source to receive state information
     */
    public final void save(DataAdaptor daSink) {
        
        DataAdaptor stateNode = daSink.createChild(STATE_LABEL);
        stateNode.setValue(TYPE_LABEL, getClass().getName());
        stateNode.setValue("id", this.getElementId());
        
        addPropertiesTo(stateNode);
    }

    /**
     * Recovers the state information from a data source represented
     * by a <code>DataAdaptor</code> interface.
     * 
     * @param   container   data source containing state information
     * 
     *  @exception DataFormatException  data in <code>container</code> is malformated
     */    
    public final void load(DataAdaptor container) throws DataFormatException {
        try {
            readPropertiesFrom(container);
        } catch (ParsingException e) {
            e.printStackTrace();
            throw new DataFormatException("error loading from adaptor: " + 
                    e.getMessage());
        }
    }


    /*
     * Support Methods
     */    
     
    /**
     * Save the state information to a <code>DataAdaptor</code> interface.
     * 
     * @param  container   data sink with <code>DataAdaptor</code> interface
     */
    protected void addPropertiesTo(DataAdaptor container) {
        DataAdaptor  specNode = container.createChild(SPECIES_LABEL);
        specNode.setValue(PARTCHARGE_LABEL, getSpeciesCharge());
        specNode.setValue(PARTRESTENERGY_LABEL, getSpeciesRestEnergy());

        DataAdaptor locNode = container.createChild(LOCATION_LABEL);
        locNode.setValue(ELEMENT_LABEL, getElementId());
        locNode.setValue(POSITION_LABEL, getPosition());
        locNode.setValue(TIME_LABEL, getTime());
        locNode.setValue(KINETICENERGY_LABEL, getKineticEnergy());
    }
    
    /**
     * Recover the state information from a <code>DataAdaptor</code> interface.
     * 
     * @param container             data source with <code>DataAdaptor</code> interface
     * 
     * @throws ParsingException     data source is malformatted
     */
    protected void readPropertiesFrom(DataAdaptor container) 
    		throws ParsingException 
    {
        // Read particle species data
        DataAdaptor specNode = container.childAdaptor(SPECIES_LABEL);
        if (specNode == null)
            throw new ParsingException("ProbeState#readPropertiesFrom(): no child element = " + SPECIES_LABEL);
        
        if (specNode.hasAttribute(PARTCHARGE_LABEL))
            setSpeciesCharge(specNode.doubleValue(PARTCHARGE_LABEL));
        if (specNode.hasAttribute(PARTRESTENERGY_LABEL))
            setSpeciesRestEnergy(specNode.doubleValue(PARTRESTENERGY_LABEL));


        // Read state data
        DataAdaptor locNode = container.childAdaptor(LOCATION_LABEL);
        if (locNode == null)
            throw new ParsingException("ProbeState#readPropertiesFrom(): no child element = " + LOCATION_LABEL);

        if (locNode.hasAttribute(ELEMENT_LABEL))
            setElementId(locNode.stringValue(ELEMENT_LABEL));
        if (locNode.hasAttribute(POSITION_LABEL))        
            setPosition(locNode.doubleValue(POSITION_LABEL));
        if (locNode.hasAttribute(TIME_LABEL))
            setTime( locNode.doubleValue(TIME_LABEL));
        if (locNode.hasAttribute(KINETICENERGY_LABEL))
            setKineticEnergy(locNode.doubleValue(KINETICENERGY_LABEL));

    }
    
}
