/*
 * ThickElement.java
 *
 * Created on September 10, 2002, 2:47 PM
 *
 */

package xal.model.elem;

import xal.model.IComposite;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IRfCavity;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.math.r3.R3;
import xal.tools.math.r3.R3x3;



/**
 * <p>
 *  Base class for all modeling elements having a finite length.  As such, space charge
 *  will affect probes propagation through these elements.
 * </p>
 * <p>
 *  The transfer matrix that is stored in the parent element is actually the incremental
 *  transfer matrix of the full element.  Thus, derived classes should implement the
 *  method subTransferMatrix() that returns the nSecs root of the full transfer 
 *  matrix.  Deriving beamline elements from this class allows space charge kicks to 
 *  be applied at nSecs equally spaced locations throughout the element.
 * </p>
 * 
 * @author  Christopher K. Allen
 */
public abstract class ThickElement extends Element {


    /*
     *  Local Attributes
     */

    /** total length of the element */
    private double      m_dblLen = 0.0;
    
    /** total length of the node before it was sliced by scenario generator */
    private double		m_dblNodeLen = 0.0;
    
    /** position of the element slice within the node */
    private boolean firstSlice = true, lastSlice = true;
    

    /*
     *  Initialization
     */

    /**
     * Default constructor to be used by automatic lattice generation.
     * Creates a new parameter-uninitialized instance of ThickElement.
     *
     * @param   strType     the string type-identifier of the element type
     */
    public ThickElement(String strType)   {
        super(strType);
    }

    /**
     * Default constructor to be used by automatic lattice generation.
     * Creates a new instance of ThickElement specifying its type identifier
     * and its instance identifier.
     *
     * @param   strType     the string type-identifier of the element type
     * @param   strId       the string identifier of the element instance
     */
    public ThickElement(String strType, String strId)   {
        super(strType, strId);
    }

    /** 
     *  Creates a new instance of ThickElement
     *
     *  @param  strType     string type identifier for the element
     *  @param  strId       string instance identifier for this element
     *  @param  dblLen      total length of the element (<b>in meters</b>)
     */
    public ThickElement(String strType, String strId, double dblLen) { 
        super(strType, strId);        
        this.setLength(dblLen);
    };

    /**
     * Conversion method to be provided by the user
     * 
     * @param latticeElement the SMF node to convert
     */
    @Override
    public void initializeFrom(LatticeElement latticeElement) {
        super.initializeFrom(latticeElement);
        setLength(latticeElement.getLength());
        m_dblNodeLen = latticeElement.getNode().getLength();
        firstSlice = latticeElement.getPartNr() == 0;
        lastSlice = latticeElement.getPartNr() == latticeElement.getParts() - 1;
    }

    /**
     *  Set the length of the element.
     *
     *  @param  dblLen      lenght of element (in <b>meters</b>)
     *
     */
    public void setLength(double dblLen)    {
        this.m_dblLen = dblLen;
    };


    /*
     *  IComponent Interface
     */

    /** 
     *  Return the total length of this element
     *
     *   @return    total element length (in <b>meters</b>)
     */
    @Override
    public double getLength() { return m_dblLen; };

    /**
     *  Return the energy gain of the beamline element over a subsection of the
     *  specified length.
     *
     *  @param  probe   probe for which energy gain is to be determined
     *  @param  dblLen   subsection length to calculate energyGain for
     *
     *  @return         the value #subEnergyGain(probe)*#getCount()
     */
    @Override
    public abstract double energyGain(IProbe probe, double dblLen);

    /**
     *
     * @see xal.model.elem.Element#elapsedTime(xal.model.IProbe, double)
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public abstract double elapsedTime(IProbe probe, double dblLen);

    /**
     *
     * This is a kluge to make RF gaps work, since frequency is not defined for 
     * modeling elements outside RF cavities.  For such elements we simply return 
     * 0 phase advance.  For elements where frequency is defined, we compute the
     * phase advance as the angular frequency times the elapsed time through
     * the element (see <code>{@link #elapsedTime(IProbe, double)}</code>).
     *
     * @see xal.model.elem.Element#longitudinalPhaseAdvance(xal.model.IProbe, double)
     *
     * @since  Jan 22, 2015   by Christopher K. Allen
     */
    @Override
    public double longitudinalPhaseAdvance(IProbe probe, double dblLen) {
        
        // We check if our parent is an RF cavity, then check all the way up the hierarchy
        IComposite cpsParent = this.getParent();
        while (cpsParent != null) {
            if (cpsParent instanceof IRfCavity) {
                IRfCavity cavParent = (IRfCavity)cpsParent;
                double    f  = cavParent.getCavFrequency();
                double    dt = this.elapsedTime(probe, dblLen);
                
                double d_phi = 2.0 * Math.PI * f * dt;
                
                return d_phi;
            }
            
            // Look all the way up the hierarchy until top level (i.e., parent is null)
            cpsParent = cpsParent.getParent();
        }
        
        return 0.0;
    }
    

    //    /**
    //     *  <p>
    //     *  Compute the transfer map for a subsection of this element whose length
    //     *  is dblLen.  If dblLen is greater than or equal to the element's length,
    //     *  return the transfer map for the full element.  Note that this may not be
    //     *  very useful for an element with differential acceleration.
    //     *
    //     *  @param  probe   probe supplying parameters for the transfer matrix calculation
    //     *  @param  dblLen  length of element subsection to compute transfer map for
    //     * 
    //     *  @return         the full tranfer map of this element
    //     *
    //     *  @exception  ModelException    exception occurred in subTransferMap() method
    //     */
    //    @Override
    //    public abstract PhaseMap transferMap(IProbe probe, double dblLen) throws ModelException;

    /**
     * Returns the total length of the node, before the element was sliced by scenario generator
     * @return original node length
     */
	public double getNodeLen() {
		return m_dblNodeLen;
	}
	
	/**
	 * Checks if this is the first subslice transfer matrix is requested for
	 * @param position position of the probe
	 * @return is this the first subslice
	 */
    protected boolean isFirstSubslice(double position) {
    	return firstSlice && Math.abs(position - (getPosition() - getLength()/2.)) < 1e-6;
    }
    
    
    /**
	 * Checks if this is the last subslice transfer matrix is requested for
	 * @param position position of the probe + subslice length
	 * @return is this the last sub-slice
	 */
    protected boolean isLastSubslice(double position) {
    	return lastSlice && Math.abs(position - (getPosition() + getLength()/2.)) < 1e-6;
    }
 
    /**
     * <h2>Add Rotation and Displacement Error to Transfer Matrix</h2>
     * <p>
     * Method to add the effects of a spatial rotation and displacement to the
     * beamline element represented by the given transfer matrix.
     * 
     * Method is optimized to add transformation only to the first and last sub-slice of
     * the element. Besides reducing number of matrix multiplications, there is also less
     * numerical error.
     *
     * @param   matPhi      transfer matrix <b>&Phi;</b> to be processed
     * @return  transfer matrix <b>&Phi;</b> after applying displacement
     * 
     * @author  Ivo List
     * 
     * @see PhaseMatrix
     * @see PhaseMatrix#translation(PhaseVector)
     */
    protected PhaseMatrix applyErrors(PhaseMatrix matPhi, IProbe probe, double length)
    {
		if (isFirstSubslice(probe.getPosition())) {
			double px = getPhiX();
		    double py = getPhiY();
		    double pz = getPhiZ();
	    	double dx = getAlignX();
	        double dy = getAlignY();
	        double dz = getAlignZ();
	        
		    if (px != 0. || py != 0.) {
		    	PhaseMatrix T = PhaseMatrix.translation(new PhaseVector(px*m_dblNodeLen/2., -px, py*m_dblNodeLen/2., -py, 0., 0.));		    	
		    	matPhi = matPhi.times(T);
		    }
		    
		    if (pz != 0.) {		   
		    	PhaseMatrix R = PhaseMatrix.rotationProduct(R3x3.newRotationZ(-pz));		    
		    	matPhi = matPhi.times(R);
		    }		   

	        if ((dx != 0)||(dy != 0)||(dz !=0)) {
	            PhaseMatrix T = PhaseMatrix.spatialTranslation(new R3(-dx, -dy, -dz));
	        	matPhi = matPhi.times(T);
	        }
		}
		
		if (isLastSubslice(probe.getPosition() + length)) {
			double px = getPhiX();
		    double py = getPhiY();
		    double pz = getPhiZ();
			double dx = getAlignX();
	        double dy = getAlignY();
	        double dz = getAlignZ();
	        
		    if (px != 0. || py != 0.) {
		    	PhaseMatrix T = PhaseMatrix.translation(new PhaseVector(px*m_dblNodeLen/2., px, py*m_dblNodeLen/2., py, 0., 0.)); 		    
		    	matPhi = T.times(matPhi);
		    }
		    
		    if (pz != 0.) {		   
		    	PhaseMatrix R = PhaseMatrix.rotationProduct(R3x3.newRotationZ(pz));
		    	matPhi = R.times(matPhi);	    		    
		    }

	        if ((dx != 0)||(dy != 0)||(dz !=0)) {
	        	 PhaseMatrix T = PhaseMatrix.spatialTranslation(new R3(dx,dy,dz));
	             matPhi = T.times(matPhi);
	        } 
		}
		   		   
		return matPhi;
    }   
}

