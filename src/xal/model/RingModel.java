/*
 * RingModel
 * 
 * Created on May 17, 2004
 *
 */
package xal.model;

import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseMap;

import java.util.ArrayList;
import java.util.List;

import xal.model.elem.ElementSeq;
import xal.model.probe.SynchronousProbe;
import xal.model.probe.TransferMapProbe;


/**
 * @author Christopher K. Allen
 *
 * @deprecated This class is never used
 */
@Deprecated
public class RingModel extends ElementSeq {
    /** default number of element positions to reserve in list array */
    public static final int    s_szDefReserve = 100;
    
    /** the string type identifier for all Lattice objects */
    public static final String      s_strType = "RingModel";
    
    
    /** version of lattice */
    private String      m_strVersion;
    
    /** lattice author */
    private String      m_strAuthor;
    
    /** lattice date */
    private String      m_strDate;
    
    
    /**
     * Creates a new, empty instance of <code>RingModel</code>.
     */
    public RingModel() {
        this(null, s_szDefReserve);
    }
 
	
    /**
     * Creates a new instance of <code>RingModel</code> with specified string
     * identifier.
     *
     *  @param  strId       identifier of the lattice
     */
    public RingModel(String strId) {
        this(strId, s_szDefReserve);
    }
 
	
    /**
     *  Creates a new instance of <code>RingModel</code> and reserves space for a 
     *  szReserve length lattice.
     *
     *  @param  strId       identifier of the lattice
     *  @param  szReserve   number of Element spaces to reserve
     */
    public RingModel(String strId, int szReserve) {
        super(s_strType, strId, szReserve);
    }
 
	
    /**
     *  Sets the version tag
     *
     *  @param  strVersion      revision number of lattice
     */
    public void setVersion(String strVersion)   {
        m_strVersion = strVersion;
    }
    
	
    /**
     *  Sets the author tag
     *
     *  @param  strAuthor       author of lattice description
     */
    public void setAuthor(String strAuthor)     {
        m_strAuthor = strAuthor;
    }
    
	
    /**
     *  Sets the date tag
     *
     *  @param  strDate         date string of lattice description
     */
    public void setDate(String strDate)         {
        m_strDate = strDate;
    }
    
    
    
    /**
     *  Propagate a probe through the lattice.  The probe is first initialized by calling
     *  the <code>initialize()</code> method of the probe then updated by calling the
     *  <code>update()</code> method in order to save the initial state of the probe 
     *  into its trajectory.
     *
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     */
    @Override
    public void propagate( final IProbe probe ) throws ModelException {   

        probe.initialize();
        probe.update();
		
		setupOrigin( probe );
        
        super.propagate(probe);
    }
	
	
    /**
     *  <p>
     *  Back propagation of a probe through the lattice.  The probe is first 
     *  initialized by calling
     *  the <code>initialize()</code> method of the probe then updated by calling the
     *  <code>update()</code> method in order to save the initial state of the probe 
     *  into its trajectory.
     * </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     *  @param  probe   the state of the probe will be advance using the elements dynamics
     *
     *  @exception  ModelException    an error occurred while advancing the probe state
     *  
     *  @author Christopher K. Allen
     *  @since Feb 27, 2009
     *  
     *  @see ElementSeq#backPropagate(IProbe)
     */
    @Override
    public void backPropagate( final IProbe probe ) throws ModelException {   

        probe.initialize();
        probe.update();
        
        setupOrigin( probe );
        
        super.backPropagate(probe);
    }
    
    
	/**
	 * Check to see if a start element has been specified.  If so, reorganize the component list so 
	 * that the list remains a full turn.
	 */
	private void setupOrigin( final IProbe probe ) {
		final String startElementID = probe.getAlgorithm().getStartElementId();
		if ( startElementID == null )  return;	// nothing to do
		
		int startIndex = 0;
		final List<IComponent> elements = getForwardCompList();
		final int numElements = elements.size();
		for ( int index = 0 ; index < numElements ; index++ ) {
			final IComponent element = elements.get( index );
			if ( element.getId().equals( startElementID ) ) {
				startIndex = index;
				break;
			}
		}
		
		if ( startIndex > 0 ) {
			final List<IComponent> newElements = new ArrayList<IComponent>( numElements );
			newElements.addAll( elements.subList( startIndex, numElements ) );
			newElements.addAll( elements.subList( 0, startIndex ) );			
			setCompList( newElements );
		}		
	}
    
    
    
    
    /**
     *  Get the version of the lattice
     *  
     *  @return     lattice revision number
     */
    public String   getVersion()        { return m_strVersion==null? "":m_strVersion; }
    
	
    /**
     *  Get the author of the lattice definition
     *
     *  @return     lattice author
     */
    public String   getAuthor()         { return m_strAuthor==null? "":m_strAuthor; }
    
	
    /**
     *  Get the date of lattice description
     *
     *  @return     lattice model date
     */
    public String   getDate()           { return m_strDate==null? "":m_strDate; }
    
    
    /**
     * Compute the one-turn map of the ring for a particular 
     * <code>TransferMapProbe</code> describing the beam at injection.
     * 
     * @param   probe       probe with injection properties of the beam
     * 
     * @return              one-turn map of ring for particular probe 
     * 
     * @throws  ModelException  error in ring propagation of the probe
     */
    public PhaseMap compOneTurnMatrix(TransferMapProbe probe) throws ModelException {
        this.propagate(probe);
        
        return probe.getTransferMap();
    }


    /**
     * <p>
     * <em>Nothing is done here.</em>
     * </p>
     * <p>
     * <small>
     * Compute the closed-orbit correlation matrix of the ring for a particular
     * injection probe (of type <code>TransferMapProbe</code>).
     * </small>
     * </p>
     * 
     * @param probe
     * 
     * @return  This method simply returns the zero matrix
     */
    public  CovarianceMatrix compClosedOrbit(SynchronousProbe  probe)  {
        return CovarianceMatrix.newZero();
    }
}
