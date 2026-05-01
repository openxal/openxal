/*
 * ThinLens.java
 *
 * Created on November 1, 2002, 9:06 AM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to the new model architecture
 */

package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;






/**
 *  Represents a thin lens abstract modeling element in a particle beam transport/accelerator
 *  system.
 *  <p>
 *  A zero value for focal length indicates zero focusing strength, or infinite focal 
 *  length.  Positive focal lengths imply focusing while negative values imply defocusing.
 *
 * @author  Christopher K. Allen
 */
public class ThinLens extends ThinElement {
    
    
    /*
     *  Global Attributes
     */
    
    /** string type identifier for all ThinLens objects */
    public static final String      s_strType = "ThinLens";
    
    
    
    
    /*
     *  Local Attributes
     */
    
    /** focal length of the thin lens in the x phase plane */
    private double m_dblFocX = 0.0;
    
    /** focal length of the thin lens in the y phase plane */
    private double m_dblFocY = 0.0;
    
    /** focal length of the thin lens in the z phase plane */
    private double m_dblFocZ = 0.0;
    
    
    /*
     *  Initialization
     */
    
    /** 
     *  Creates a new instance of ThinLens 
     *  A zero value for focal length indicates zero focusing strength, or infinite focal 
     *  length.  Positive focal lengths imply focusing while negative values imply defocusing.
     *
     *  @param  strId   string identifier of element
     *  @param  dblFx   focal length in the x phase plane (<b>in meters</b>)
     *  @param  dblFy   focal length in the y phase plane (<b>in meters</b>)
     *  @param  dblFz   focal length in the z phase plane (<b>in meters</b>)
     */
    public ThinLens(String strId, double dblFx, double dblFy, double dblFz) {
        super(s_strType, strId);
        
        this.setFocalLengthX(dblFx);
        this.setFocalLengthY(dblFy);
        this.setFocalLengthZ(dblFz);
    }
    
    /** 
     *  JavaBean Constructor - creates a new unitialized instance of ThinLens
     *  
     *  <b>BE CAREFUL</b>
     */
    public ThinLens() 
    {
        super(s_strType);
    }

    /**
     *  Set the focal length in the x direction.
     *
     *  @param  dblFx   focal length in the x phase plane (<b>in meters</b>)
     */
    public void setFocalLengthX(double dblFx) {
        m_dblFocX = dblFx;
    }
    
    /**
     *  Set the focal length in the y direction.
     *
     *  @param  dblFy   focal length in the y phase plane (<b>in meters</b>)
     */
    public void setFocalLengthY(double dblFy) {
        m_dblFocY = dblFy;
    }
    
    /**
     *  Set the focal length in the z direction.
     *
     *  @param  dblFz   focal length in the z phase plane (<b>in meters</b>)
     */
    public void setFocalLengthZ(double dblFz) {
        m_dblFocZ = dblFz;
    }

    
    
    /*
     *  Property Queries
     */
    
    /**
     *  Return the focal length of thin lens in the x phase plane.
     *      Negative values indicate focusing effect
     *      Positive values indicate defocusing effect
     *
     *  @return     lens focal length (in <b>meters</b>)
     */
    double getFocalLengthX() { return m_dblFocX; };
    
    /**
     *  Return the focal length of thin lens in the y phase plane.
     *      Negative values indicate focusing effect
     *      Positive values indicate defocusing effect
     *
     *  @return     lens focal length (in <b>meters</b>)
     */
    double getFocalLengthY() { return m_dblFocY; };
    
    /**
     *  Return the focal length of thin lens in the z phase plane.
     *      Negative values indicate focusing effect
     *      Positive values indicate defocusing effect
     *
     *  @return     lens focal length (in <b>meters</b>)
     */
    double getFocalLengthZ() { return m_dblFocZ; };
    

    
    /*
     *  IElement Interface
     */
    
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         the value zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
        return 0.0;
    }
    
    /**
     *  Returns zero for the energy gain imparted to any probe by a thin lens.
     *
     *  @param  probe   dummy argument
     *
     *  @return         a value of zero
     */
    @Override
    public double   energyGain(IProbe probe)    { return 0.0; };
    
    /**
     *  Compute and return the block-diagonal transfer matrix representing  
     *  a thin lens in each phase plane.
     *  
     *  @param  probe   dummy argument, no probe parameters are used
     *
     *  @return         the block diagonal transfer matrix 
     *
     *  @exception  ModelException    this should not occur
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {
        PhaseMatrix  matPhi = PhaseMatrix.identity();       // linear portion of phase map
        
        // The x phase plane
        if (m_dblFocX != 0.0)
            matPhi.setElem(1,0, -1.0/m_dblFocX);
        
        // The y phase plane
        if (m_dblFocY != 0.0)
            matPhi.setElem(3,2, -1.0/m_dblFocY);
        
        // The z phase plane
        if (m_dblFocZ != 0.0)
            matPhi.setElem(5,4, -1.0/m_dblFocZ);
        
        return new PhaseMap( matPhi );
    };




    /*
     *  Testing and Debugging
     */
    
    
    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    @Override
    public void print(PrintWriter os)    {
        super.print(os);
        
        os.println("  X-plane focus      : " + this.getFocalLengthX() );
        os.println("  Y-plane focus      : " + this.getFocalLengthY() );
        os.println("  Z-plane focus      : " + this.getFocalLengthZ() );
    }
}
