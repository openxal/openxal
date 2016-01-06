/*
 * Element.java
 *
 * Created on August 11, 2002, 8:43 AM
 *
 */

package xal.model.elem;



import java.io.PrintWriter;
import java.util.ArrayList;

import xal.model.IAlgorithm;
import xal.model.IComposite;
import xal.model.IElement;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.alg.Tracker;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.IConstants;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.r3.R3;



/**
 * Convenience abstract base class for constructing XAL modeling elements.
 * This class implements many of the general methods for the 
 * <code>IElement</code> interface that are not element specific.
 *
 * @author  Christopher Allen
 * @author Craig McChesney
 */

public abstract class Element implements IElement {

    
    /*
     *  Global Attributes
     */
    
    /** Global index counter for object identifiers */
    private static  int     s_cntInstances;
    
    
    /*
     *  Class loader initialization
     */
    static {
        s_cntInstances = 0;
    };
    
    
    /*
     *  Local Attributes
     */

	/** internal unique identifier of element */
    private int         m_intUID;
    
    /** the element type identifier */
    private String      m_strType;
    
    /** modeling element string identifier (not necessarily unique) */
    private String      m_strId;
    
    /** Identifier string of the model hardware node */
    private String      strSmfId;
    
    /** the parent composite structure that owns this element */
    private IComposite  cpsParent;
    
//  sako
    //position in s (m)
    /** This is the center position of the element with the lattice - CKA */
    private double      dblPos;
    
    //sako closeElements (for fringe field calculations)
    /** 
     * This is a container of nearest-neighbor elements used for computing
     * transfer maps in the presence of permanent magnet quadrupoles.
     */
    private ArrayList<Element> closeElements = null;
  
    
    //hs alignment
    private double alignx = 0.0;
    private double aligny = 0.0;
    private double alignz = 0.0;
    

    /*
     * Initialization
     */
                
    /**
     *  Creates a new instance of Element
     *
     *  @param  strType     type identifier of the element
     */
    public Element(String strType)    {
        this(strType, "NULLID");
    };
    
    /**
     *  Creates a new instance of Element
     *
     *  @param  strType     type identifier of the element
     *  @param  strId       string identifier of the element
     */
    public Element(String strType, String strId)    {
        this.m_intUID  = s_cntInstances++;
        this.m_strType = strType;
        this.m_strId   = strId;
        this.strSmfId = "";
        this.dblPos = 0.0;
        this.cpsParent = null;
    };
    
    /**
     *  Set the string identifier for the element.
     *
     *  @param  strId       new string identifier for element
     */
    public void setId(String strId) {
        m_strId = strId;
    };
    
    /**
     * Sets the string identifier of the hardware node which this
     * element models.  Node that this element may only model part
     * of the underlying hardware node or simply some aspect of it.
     * Thus, this is not a unique value amount all modeling elements.
     * 
     * @param strSmfId  identifier for the modeled hardware node (SMF object)
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2014
     */
    public void setHardwareNodeId(String strSmfId) {
        this.strSmfId = strSmfId;
    }

    /**
     * Set the center position of the element with the containing
     * lattice.
     * 
     * @param dblPos    center position along the design trajectory (meters) 
     */
    public void setPosition(double dblPos) {
        this.dblPos = dblPos;
    }
    
    /**
     * Set the alignment parameters all at once.
     * 
     * @param vecAlign  (dx,dy,dz)
     * 
     * @author Christopher K. Allen
     */
    public void setAlign(R3 vecAlign)    {
        alignx = vecAlign.getx();
        aligny = vecAlign.gety();
        alignz = vecAlign.getz();
    }
    
    
    /**
     * Set the horizontal misalignment
     * 
     * @param x     misalignment (in m)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public void setAlignX(double x) {
        alignx = x;
    }

    /**
     * Set the vertical misalignment
     * 
     * @param y     misalignment (in m)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public void setAlignY(double y) {
        aligny = y;
    }

    /**
     * Set the longitudinal misalignment
     * 
     * @param z     misalignment (in m)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public void setAlignZ(double z) {
        alignz = z;
    }

    
    /*
     *  Property Queries
     */
    
    /** 
     *  Return the internal class unique identifier of this element.
     *
     *  @return     the unique identifier of this object
     */
    public int  getUID()  { 
        return m_intUID; 
    };
    
    /**
     * Get the horizontal misalignment
     * 
     * @return  the misalignment (in meters)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public double getAlignX() {
        return alignx;
    }

    /**
     * Get the vertical misalignment
     * 
     * @return  the misalignment (in meters)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public double getAlignY() {
        return aligny;
    }
    
    /**
     * Get the longitudinal misalignment
     * 
     * @return  the misalignment (in meters)
     *
     * @since  Dec 17, 2014   by Christopher K. Allen
     */
    public double getAlignZ() {
        return alignz;
    }
    
    
    /*
     * Operations
     */
    
    /**
     * Add an element to the list of nearest neighbor elements used when
     * considering the effects of PMQs
     * 
     * @param closeElem     an adjacent element
     */
    public void addCloseElements(Element closeElem) {
        if (closeElements == null) {
            closeElements = new ArrayList<Element>();
        }
        closeElements.add(closeElem);
    }
    
    /**
     * Returns the given transfer matrix adjusted by the
     * misalignment parameters of this element.
     *
     * @param matPhi    a transfer matrix
     * 
     * @return          the given transfer matrix modified to contain misalignment errors
     *
     * @author Christopher K. Allen
     * @since  Apr 13, 2011
     */
    protected PhaseMatrix applyAlignError(PhaseMatrix matPhi) {

        double delx = getAlignX();
         double dely = getAlignY();
         double delz = getAlignZ();
         
         if ((delx==0)&&(dely==0)&&(delz==0)) {
             return matPhi; // do nothing
         }

         PhaseMatrix T = new PhaseMatrix();
         
         for (int i=0;i<7;i++) {
             T.setElem(i,i,1);
         }
         
         T.setElem(0,6,-delx);
         T.setElem(2,6,-dely);
         T.setElem(4,6,-delz);
         PhaseMatrix Phidx = T.inverse().times(matPhi).times(T);

         return Phidx;
     }
     
    /**
     * <p>This method is intended to return the location of the probe within
     * the current element.  Actually, we compute and return the distance of 
     * the probe from the element entrance location.
     * </p>
     * 
     * NOTE
     * <p> This method will always return a value regardless of 
     * whether or not the probe is within the element domain.  The
     * returned value is the distance from the entrance location.
     * Thus, negative values indicate an upstream position and values
     * larger than the element length indicate positions downstream.
     * </p>
     * 
     * @param   probe   probe within element domain
     * 
     * @return  the distance within the element entrance
     * 
     * @author Christopher K. Allen
     */
    public double  compProbeLocation(IProbe probe) {
        
        double lenElem = this.getLength();          // element length
        double sCenter = this.getLatticePosition(); // center position w/in lattice
        
        double sProbe  = probe.getPosition();   // probe position within lattice
        
        double sElem = sProbe - (sCenter - lenElem/2.0);
        
        return sElem;
    }
    
    
    //
    // Methods for PMQ Support
    //
    
    /**
     * Return the list of nearest adjacent elements to this element.
     * THis is used primarily in permanent magnet quadrupole considerations.
     * 
     * @return  List of adjacent modeling elements
     */
    public ArrayList<Element> getCloseElements() {
        return closeElements;
    }
    
    /** 
     * Compute the time the probe <code>probe</code> spends drifting a
     * a distance <code>dblLen</code>.
     *  
     * @param   probe       interface to drifting probe
     * @param   dblLen      length of drift in <b>meters</b>  
     * 
     * @return              time interval during drift in <b>seconds</b>
     */
    
    public double compDriftingTime(IProbe probe, double dblLen) {

        double dblTime = 0.0;                // the time interval
        double dblBeta = probe.getBeta();    // normalized probe velocity
     
        dblTime = dblLen / (IConstants.LightSpeed * dblBeta);
        
        return dblTime;
    }
    
    
    /*
     *  IComponent Interface
     */
    
    /**
     *  Return the element type identifier
     *
     *  @return     element type string
     */
    @Override
    public String   getType()   { return m_strType; };
    
    /**
     *  Returns the string identifier for this element.
     *
     *  @return     string identifier
     */
    @Override
    public String   getId()     { return m_strId; };
    
    /**
     * Returns the string identifier of the hardware node which this
     * element models.  Note that the element may model only a 
     * portion of the hardware object or simply an aspect of it.
     * Thus, this is not a unique values among modeling elements.
     * 
     * @return      the identifier string of the hardware this element models
     *
     * @author Christopher K. Allen
     * @since  Sep 2, 2014
     */
    @Override
    public String   getHardwareNodeId() {
        return this.strSmfId;
    }

    /**
     * Conversion method to be provided by the user
     * 
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        String  strElemId = latticeElement.getModelingElementId();
        String  strSmfId  = latticeElement.getHardwareNode().getId();
        
        setId( strElemId != null ? strElemId : strSmfId);
        setHardwareNodeId(strSmfId);
        setPosition(latticeElement.getCenterPosition());
        
//        // CKA: Added to include hardware ID attribute for the new element.
//        //   This is bound to ScenarioGenerator#collectElements(). 
//        //   If "ELEMENT_CENTER" is changed you must modify both!
//        if ( this instanceof Marker && 
//             this.getId().startsWith("ELEMENT_CENTER")
//             )
//            setHardwareNodeId(this.getId().replace("ELEMENT_SEQUENCE:", "") );
    }
    
    /**
     *  Return the length of this element.  Derived class must
     *  implement this because it is undetermined whether or not this is a thin
     *  or thick element.
     */
    @Override
    public abstract double getLength();
    
    /**
     * Return the center position of the element along the design trajectory.
     * This is the position with the containing lattice.
     * 
     * @return  center position of the element (meters)
     */
    @Override
    public double getPosition() {
        return dblPos;
    }
    
    /**
     *
     * @see xal.model.IComponent#getLatticePosition()
     *
     * @since  Dec 3, 2015,  Christopher K. Allen
     */
    @Override
    public double getLatticePosition() {
        if (this.getParent() == null)
            return this.getPosition();
        
        double  dblLocPos = this.getPosition();
        double  dblParPos = this.getParent().getLatticePosition();
        double  dblParLen = this.getParent().getLength();
        double  dblGblPos = (dblParPos - dblParLen/2.0) + dblLocPos;
        
        return dblGblPos;
    }

    /**
     * @return  returns the composite structure owning this element, 
     *          or <code>null</code> if this component is isolated
     *
     * @see xal.model.IComponent#getParent()
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public IComposite getParent() {
        return this.cpsParent;
    }

    /**
     *
     * @see xal.model.IComponent#setParent(xal.model.IComposite)
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public void setParent(IComposite cpsParent) {
        this.cpsParent = cpsParent;
        this.cpsParent.setDirty(this);
    }

    /** 
     * <p>
     * Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}
     * Propagates the Probe object through this element based on the associated algorithm.
     * </p>  
     *  
     *  <p>NOTE: CKA
     *  <br>
     *  The position of the probe within the element appears to be kept as a
     *  field of the algorithm object.  I am not exactly sure of any 
     *  side-effects of this implementation when using the
     *  <code>{@link xal.model.alg.Tracker#propagate(IProbe, IElement)}</code> of the 
     *  <code>{@link xal.model.alg.Tracker}</code> class.  Careful when modifying.
     *  </p>
     *
     *  @param  probe       probe object to propagate
     *  @param  pos         I think it is position of the probe within this element
     *
     *  @exception  ModelException    error occurred during propagation
     * 
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     *  @see xal.model.alg.Tracker#propagate(IProbe, IElement)
     */
    @Override
    public void propagate(IProbe probe, double pos) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
            Tracker tracker = (Tracker)alg;
//          System.out.println("tracker.setElemPosition to "+pos);
            
            // The algorithm "element position" is also set in Tracker#advanceProbe() ??!!
            tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    };
    
    /** 
     * <p>
     * Override of {@link xal.model.IComponent#propagate(xal.model.IProbe, double)}
     * Propagates the Probe object through this element based on the associated algorithm.
     * </p>  
     *
     *  @param  probe       probe object to propagate
     *
     *  @exception  ModelException    error occurred during propagation
     *  
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
            Tracker tracker = (Tracker)alg;

            // The algorithm "element position" is also set in Tracker#advanceProbe() ??!!
            tracker.setElemPosition(0);
        }
        alg.propagate(probe, this);
    };
    
    /** 
     *  <p>
     *  Back propagates the Probe object through this element 
     *  based on the associated algorithm.
     *  </p>
     * <p>
     * <strong>NOTES</strong>: CKA
     *  <br>
     *  The position of the probe within the element appears to be kept as a
     *  field of the algorithm object.  I am not exactly sure of any 
     *  side-effects of this implementation when using the
     *  <code>{@link Tracker#propagate(IProbe, IElement)}</code> of the 
     *  <code>{@link Tracker}</code> class.  Careful when modifying.
     * <br>
     * &middot; Support for backward propagation
     * February, 2009.
     * <br>
     * &middot; You must use the <em>proper algorithm</em> object
     * for this method to work correctly!
     * </p>
     * 
     *
     *  @param  probe       probe object to propagate
     *  @param  pos         I think it is position of the probe within this element
     *
     *  @exception  ModelException    error occurred during propagation
     * 
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     *  @see xal.model.alg.Tracker#propagate(IProbe, IElement)
     */
    @Override
    public void backPropagate(IProbe probe, double pos) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
            Tracker tracker = (Tracker)alg;
            System.out.println("tracker.setElemPosition to "+pos);

            // The algorithm "element position" is also set in Tracker#advanceProbe() ??!!
            tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    }

    /** 
     * <p>
     * Back propagates the Probe object through this element based on the 
     * associated algorithm.
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
     *  @param  probe       probe object to propagate
     *
     *  @exception  ModelException    error occurred during propagation
     *  
     *  @see xal.model.IComponent#propagate(xal.model.IProbe, double)
     */
    @Override
    public void backPropagate(IProbe probe) throws ModelException {
        
        IAlgorithm      alg;    // algorithm for the probe
        
        alg = probe.getAlgorithm();
        if (alg instanceof Tracker) {
            Tracker tracker = (Tracker)alg;
    
            // set position at the exit of the element
            double pos = this.getLength();

            // The algorithm "element position" is also set in Tracker#advanceProbe() ??!!
            tracker.setElemPosition(pos);
        }
        alg.propagate(probe, this);
    }

    
    /*
     * IElement Interface
     */
    
    /**
     * Returns the time taken for the probe <code>probe</code> to propagate 
     * through a subsection of the element with length <code>dblLen</code>.
     * 
     *  @param  probe   determine energy gain for this probe
     *  @param  dblLen  length of subsection to calculate energy gain for
     *  
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    public abstract double elapsedTime(IProbe probe, double dblLen);
    
    /** 
     *  Returns energy gain for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.
     *
     *  @param  probe   determine energy gain for this probe
     *
     *  @return         the energy gain provided by this element <bold>Units: eV</bold> 
     */
    public abstract double energyGain(IProbe probe, double dblLen);

    /**
     * This is a kluge to make RF gaps work, since frequency is not defined for most
     * modeling elements.  For such elements we simply return 0 phase advance.  For
     * elements where frequency is defined, we can override this.
     *
     * @see xal.model.IElement#longitudinalPhaseAdvance(xal.model.IProbe, double)
     *
     * @author Christopher K. Allen
     * @since  Nov 23, 2014
     */
    public abstract double   longitudinalPhaseAdvance(IProbe probe, double dblLen);

    /**
     *  Compute the transfer matrix for <b>subsection</b> of this element of length 
     *  <code>dblLen</code> for the specified given probe.  That is, this method should 
     *  return the incremental transfer matrix.
     *
     *  @param  dblLen      length of sub-element
     *  @param  probe       probe containing parameters for the sub-sectional transfer matrix
     *
     *  @return             transfer map for an element of length dblLen
     *
     *  @exception  ModelException    unable to compute transfer map
     *
     *  @see    xal.model.IElement#transferMap(IProbe,double)
     */
    public abstract PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;   


    /*
     * Object Overrides
     */
    
    /**
     *
     * @see java.lang.Object#toString()
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public String toString() {
        StringBuffer    bufOut = new StringBuffer();
        
        bufOut.append("  Element - " + this.getId());
        bufOut.append('\n');
        
        bufOut.append("  element type       : " + this.getType() );
        bufOut.append('\n');

        bufOut.append("  element UID        : " + this.getUID() );
        bufOut.append('\n');

        bufOut.append("  element length     : " + this.getLength() );
        bufOut.append('\n');

        return bufOut.toString();
    }

     
    /*
     *  Testing and Debugging
     */
    
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    public void print(PrintWriter os)    {
//        os.println("  Element - " + this.getId());
//        os.println("  element type       : " + this.getType() );
//        os.println("  element UID        : " + this.getUID() );
//        os.println("  element length     : " + this.getLength() );
        os.println(this.toString());
    };

};