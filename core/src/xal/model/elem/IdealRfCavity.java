/**
 * IdealRfCavity.java
 *
 * Author  : Christopher K. Allen
 * Since   : Dec 3, 2014
 */
package xal.model.elem;

import java.util.Iterator;

import xal.model.IComponent;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavityCell;
import xal.sim.scenario.LatticeElement;
import xal.smf.AcceleratorNode;
import xal.smf.impl.RfCavity;

/**
 * <p>
 * This class represents a general RF cavity being an composition of
 * RF gaps and cavity drifts.  The types and parameters of the internal
 * elements define the operation (and configuration) of the cavity.
 * </p>
 * <p>
 * The propagation is done via the base class <code>ElementSeq</code> which
 * just runs through the child elements propagating (or back propagating)
 * in order.  Thus, currently at least, this element is really just a 
 * container of elements.
 * </p>  
 *
 * @author Christopher K. Allen
 * @since  Dec 3, 2014
 */
public class IdealRfCavity extends ElementSeq {

    
    /*
     *  Global Constants
     */
    
    /** the string type identifier for all Sector objects */
    public static final String      STR_TYPEID = "RfCavity";
    
    
    /*
     * Local Attributes
     */
    
    /** The current operating phase with respect to the arriving particle (in radians) */
    private double  dblPhase;
    
    /** The amplitude of the RF signal at the cavity RF window, i.e., the klystron amplitude (in Volts) */
    private double  dblAmp;
    
    /** The frequency of the enclosing cavity (in Hertz) */
    private double  dblFreq;
    
    /** The mode constant (1/2 the mode number) of the cavity which we are exciting */
    private double  dblModeConst;
    
    
    /*
     * Initialization
     */
    
    /**
     * Zero constructor for <code>IdealRfCavity</code>.
     *
     * @param strType
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity() {
        super(STR_TYPEID);
    }

    /**
     * Constructor for <code>IdealRfCavity</code> with string identifier.
     *
     * @param strId     string identifier for the RF cavity
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity(String strId) {
        super(STR_TYPEID, strId);
    }

    /**
     * Constructor for IdealRfCavity.
     *
     * @param strId     string identifier for the RF cavity
     * @param szReserve number of initial element positions to allocate 
     *                  (marginally increases performance, maybe)
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public IdealRfCavity(String strId, int szReserve) {
        super(STR_TYPEID, strId, szReserve);
    }

    /**
     * Set the operating frequency of the RF cavity in Hertz.
     * 
     * @param dblFreq   fundamental RF frequency of the RF cavity (in Hertz)
     */
    public void setFrequency(double dblFreq) {
        this.dblFreq = dblFreq;
    }
    
    /**
     * Sets the amplitude of the RF signal feeding the cavity.  Specifically,
     * the voltage of the RF at the cavity RF window.
     * 
     * @param dblAmp    high-power signal level at the cavity (in Volts)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public void setAmplitude(double dblAmp) {
        this.dblAmp = dblAmp;
    }
    
    /**
     * Sets the RF phase of the cavity with respect to the propagating probe.
     * Specifically, this is the RF phase seen by the probe as it first enters
     * the cavity.
     * 
     * @param dblPhase      RF phase of the cavity upon probe arrival (in radians)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public void setPhase(double dblPhase) {
        this.dblPhase = dblPhase;
    }

    /**
     * <p>
     * Set the operating mode constant &lambda; for the RF cavity design. The constant
     * is half of the mode number <i>q</i>.  Specifically,
     * <br/>
     * <br/>
     *  &nbsp; &nbsp; &lambda; = 0 &nbsp; (<i>q</i>=0) &rArr;  0 mode cavity structure (e.g. DTL)
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; &lambda; = 1/2 (<i>q</i>=1) &rArr; &pi;/2 mode structure (bi-periodic structures, e.g., SideCC)
     *  <br/>
     *  <br/> 
     *  &nbsp; &nbsp; &lambda; = 1 &nbsp; (<i>q</i>=2) &rArr; &pi;-mode cavity (e.g. CCL, super-conducting)
     * </p>
     * 
     * @param dblModeConst  the new mode constant &lambda; for the cavity drift
     */
    public void setCavityModeConstant(double dblModeConst) {
        this.dblModeConst = dblModeConst;
    }

    
    /*
     * Attribute Query
     */
    
    /**
     * Get the operating frequency of the RF cavity in Hertz.
     * 
     * @return  the fundamental mode frequency <i>f</i><sub>0</sub> of the RF cavity 
     */
    public double getFrequency() {
        return this.dblFreq;
    }

    /**
     * Get the amplitude of the RF signal feeding the cavity.  Specifically,
     * the voltage of the RF at the cavity RF window.
     * 
     * @return  high-power signal level at the cavity (in Volts)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public double   getAmplitude() {
        return this.dblAmp;
    }

    /**
     * Get the RF phase of the cavity with respect to the propagating probe.
     * Specifically, this is the RF phase seen by the probe as it first enters
     * the cavity.
     * 
     * @return  RF phase of the cavity upon probe arrival (in radians)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    public double   getPhase() {
        return this.dblPhase;
    }

    /**
     * <p>
     * Get the operating mode constant &lambda; for the RF cavity design. The constant
     * is half of the mode number <i>q</i>.  Specifically,
     * <br/>
     * <br/>
     *  &nbsp; &nbsp; &lambda; = 0 &nbsp; (<i>q</i>=0) &rArr;  0 mode cavity structure (e.g. DTL)
     *  <br/>
     *  <br/>
     *  &nbsp; &nbsp; &lambda; = 1/2 (<i>q</i>=1) &rArr; &pi;/2 mode structure (bi-periodic structures, e.g., SideCC)
     *  <br/>
     *  <br/> 
     *  &nbsp; &nbsp; &lambda; = 1 &nbsp; (<i>q</i>=2) &rArr; &pi;-mode cavity (e.g. CCL, super-conducting)
     * </p>
     * 
     * @return  the operating mode constant &lambda; for the cavity drift
     */
    public double getCavityModeConstant() {
        return this.dblModeConst;
    }

    
    /*
     * IComposite Interface
     */
    
    /**
     * Initializes the frequency and cavity mode constant from the given proxy
     * element.  The SMF node is taken from the proxy then queried directly.
     * Eh, I don't like this.
     *
     * @see xal.model.elem.ElementSeq#initializeFrom(xal.sim.scenario.LatticeElement)
     *
     * @since  Dec 5, 2014  @author Christopher K. Allen
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);
        
        AcceleratorNode smfNode = latticeElement.getHardwareNode();
        
        // If this the underlying node is not an RF Cavity there is nothing we can do
        if ( !(smfNode instanceof RfCavity) ) 
            return;
        
        RfCavity    smfRfCav = (RfCavity)smfNode;
        
        double  dblFreq  = 1.0e6 * smfRfCav.getCavFreq();    // convert to Hertz
        double  dblAmp   = 1.0e3 * smfRfCav.getDfltCavAmp(); // convert to Volts
        double  dblPhase = (Math.PI/180.0) * smfRfCav.getDfltCavPhase(); // convert to radians
        double  dblModeConst = smfRfCav.getStructureMode();
        
        this.setFrequency( dblFreq );
        this.setAmplitude( dblAmp );
        this.setPhase( dblPhase );
        this.setCavityModeConstant( dblModeConst );
    }

    /**
     * <p>
     * Sets the probes longitudinal phase to the phase of this cavity
     * upon entrance.  Then we propagate the probe through the 
     * composite structure as usual by calling the base class
     * <code>propagate</code> method.
     * </p>
     * <p>
     * It is unnecessary to override the <code>{@link #propagate(IProbe, double)}</code>
     * method since that method simply defers to the 
     * <code>{@link #propagate(IProbe)}</code> method ignoring the 
     * position parameter.
     * </p>
     *
     * @see xal.model.elem.ElementSeq#propagate(xal.model.IProbe)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {
        
        // This is the non-preferred way to do things - modeling elements 
        //  should not modify probes.  But right now I need to get my foot
        //  into this RF cavity door.
        //  TODO : modify this to conform to the Element/Algorithm/Probe design
        probe.setLongitudinalPhase( this.getPhase() );
        
        // This action is okay - it distributes parameters to the child
        //  modeling ELEMENTS of this cavity.  We are not acting on the
        //  probe component.
        this.distributeCavityProperties();
        
        // Now we propagate the probe through this composite modeling element
        //  as usual.
        super.propagate(probe);
    }

    /**
     * <p>
     * I am overriding this method even though a proper back propagation
     * <b>is impossible</b>.  We set the longitudinal phase of the probe to the
     * phase of the cavity as it backs into the exit.  The true phase
     * should be the phase of the particle as it leaves the cavity when
     * forward propagating, however, we have no way of knowing that 
     * phase a priori. 
     * </p>
     * <p>
     * It may be useful to use this setup during back propagations to 
     * explore various beam exit times and their effect.
     * </p>
     * <p>
     * It is unnecessary to override the 
     * <code>{@link #backPropagate(IProbe, double)}</code>
     * method since that method simply defers to the 
     * <code>{@link #backPropagate(IProbe)}</code> method ignoring the 
     * position parameter.
     * </p>
     *
     * @see xal.model.elem.ElementSeq#backPropagate(xal.model.IProbe)
     *
     * @since  Dec 16, 2014   by Christopher K. Allen
     */
    @Override
    public void backPropagate(IProbe probe) throws ModelException {
        probe.setLongitudinalPhase( this.getPhase() );
        
        this.distributeCavityProperties();
        super.backPropagate(probe);
    }

    
    /*
     * Support Methods
     */
    
    /**
     * Iterate through each direct child modeling element and check if it
     * exposes the <code>IRfCavityCell</code> interface.  If so, then
     * it is an accelerating cell within this cavity and we need to set
     * its index within the cavity and the cavity structure mode
     * constant for the cell.  Together these parameters allow the cavity
     * cell to adjust its field spatially in order to account for its position
     * in the cavity and the operating mode field structure.
     *
     * @since  Jan 9, 2015   by Christopher K. Allen
     */
    private void    distributeCavityProperties() {
        
        //  Initialize the loop
        int                     cntCells = 0;
        Iterator<IComponent>    iterCmps = super.localIterator();
        while ( iterCmps.hasNext() ) {
            IComponent cmp = iterCmps.next();
            
            // The child component is a cavity cell
            if (cmp instanceof IRfCavityCell) {
                IRfCavityCell   iCavCell = (IRfCavityCell)cmp;
                
                iCavCell.setCavityCellIndex( cntCells );
                iCavCell.setCavityModeConstant( this.getCavityModeConstant() );
                cntCells++;
            }
            
            // The child component is a drift space within an RF cavity
            if (cmp instanceof IdealRfCavityDrift) {
                IdealRfCavityDrift  modDrift = (IdealRfCavityDrift)cmp;
                
                modDrift.setFrequency( this.getFrequency() );
                modDrift.setCavityModeConstant( this.getCavityModeConstant() );
            }
            
        }
    }
}
