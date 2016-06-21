/*
 *  IdealMagFringeQuadFace
 * 
 * Created on June 13, 2016
 *
 */
package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

import xal.model.IProbe;
import xal.model.ModelException;

/**
 * <p>
 * Represents the action of a quadrupole fringe field as a thin lens effect.  The focusing/
 * defocusing action of the leakage fields are applied instantaneously with a magnitude
 * given by the integral of the phase advance <i>k</i>(<i>z</i>) outside the quadrupole.
 * Specifically, let the quadrupole have entrance position <i>z</i> = 0 and 
 * assume an hard-edge field representation within the magnet with (constant) gradient 
 * <i>B</i><sub>0</sub>. The wave number <i>k</i><sub>0</sub> within the quadrupole is given by
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>k</i><sub>0</sub> = [<i>qB</i><sub>0</sub> / &beta;&gamma;<i>mc</i><sup>2</sup> ]<sup>1/2</sup>
 * <br/>
 * <br/>
 * Outside the quadrupole in the region <i>z</i> < 0 the wave number is variable with some
 * profile <i>k</i>(<i>z</i>).  Assuming this profile is known, we compute it for the case
 * <i>B</i><sub>0</sub> = 1, producing the normalized wave number <i>k</i><sub><i>n</i></sub>(<i>z</i>).
 * Denote by <i>K</i><sub>0</sub><sup>-</sup> the integral of 
 * normalized <i>k</i><sub><i>n</i></sub>(<i>z</i>)
 * in the region <i>z</i> &in; (-&infin;,0) we have
 * <br/>
 * <br/>
 * &nbsp; &nbsp; <i>K</i><sub>0</sub><sup>-</sup> &trie; <sub><sub>-&infin;</sub></sub> &int;<sup><sup>0</sup></sup>
 *                                    <i>k</i><sub><i>n</i></sub>(<i>z</i>) <i>dz</i> .
 * <br/>
 * <br/>
 * Therefore, the total phase advance of the fringe field &Delta;&theta;<sup>-</sup> 
 * is found by scaling
 * The integral <i>K</i><sub>0</sub><sup>-</sup> by the quadrupole magnitude <i>B</i><sub>0</sub> giving
 * <br/>
 * <br/>
 * &nbsp; &nbsp; &Delta;&theta; = <i>B</i><sub>0</sub><i>K</i><sub>0</sub><sup>-</sup> .
 * <br/>
 * <br/>
 * There are analogous definitions for the exit integral <i>K</i><sub>0</sub><sup>+</sup> and
 * for higher order moments <i>K</i><sub><i>n</i></sub><sup>&pm;</sup>.
 * </p>
 * <p>
 * <h4>NOTES - CKA</h4>
 * <ul>
 *   <li>I deleted <em>a lot</em> of code.  It either had no relevance to a fringe field
 *   or it was never used.
 *   <li>Lets start with the first integrals <i>K</i><sub>0</sub><sup>&pm;</sup> and go
 *   from there.
 * </ul>
 * </p>
 * 
 *  
 *   
 * @author Christopher K. Allen
 * @author X.H.Lu
 * @version June 13,2016
 *
 */
public class IdealMagFringeQuadFace extends ThinElectromagnet {

    /*
     *  Global Constants
     */
    
    /** the string type identifier for all IdealMagFringeQuadFace */
    public static final String      s_strType = "IdealMagFringeQuadFace";
    
    
    /*
     *  Local Attributes
     */
 
    /** 1st moment of fringe field */
    private double              dblFringeInt1 = 0.0;
    
    /** 2nd moment of fringe field */
    private double              dblFringeInt2 = 0.0;

    
    
    /*
     * Initialization
     */
    
    /**
     * Default constructor - creates a new uninitialized instance of 
     * IdealMagFringeQuadFace.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagFringeQuadFace() {
        super(s_strType);
    }

    /**
     * Constructor providing the instance identifier for the element.
     * 
     * @param strId     string identifier for element
     */
    public IdealMagFringeQuadFace(String strId) {
        super(s_strType, strId);
    }
 
    /**
     * Set the first-order moment integral of the quadrupole fringe field
     * 
     * @param   dblFringeInt  field moment f1 (<b>dimensionless</b>)
     */
    public void setFringeIntegral1(double dblFringeInt) {
        this.dblFringeInt1 = dblFringeInt;
    }
    
    /**
     * Set the first-order moment integral of the quadrupole fringe field
      * 
     * @param   dblFringeInt  field moment f1 (<b>dimensionless</b>)
     */
    public void setFringeIntegral2(double dblFringeInt) {
        this.dblFringeInt2 = dblFringeInt;
    }
 
 
    /*
     * Property Query
     */

    /**
     * Set the second-order moment integral of the quadrupole fringe field
     * 
     * @return   second-order integral of fringe field (<b>dimensionless</b>)
     * 
     * @see IdealMagFringeQuadFace#setFringeIntegral(double)
     */
    public double  getFringeIntegral1() {
        return this.dblFringeInt1;
    }

    public double  getFringeIntegral2() {
        return this.dblFringeInt2;
    }


    /*
     *  IElectromagnet Interface
     */
   
    /**
     * Returns the time taken for the probe to propagate through element.
     * 
     *  @param  probe   propagating probe
     *  
     *  @return         value of zero 
     */
    @Override
    public double elapsedTime(IProbe probe)  {
        return 0.0;
    }
    
    /**
     *  Return the energy gain for this Element.
     *
     *  @param  probe   propagating probe
     *
     *  @return         value of zero
     */
    @Override
    public double   energyGain(IProbe probe)     { 
        return 0.0; 
    };
    


    /**
     * Compute and return the transfer map for this quadrupole magnet
     * pole face element.
     * @param probe
     * @return
     * @throws ModelException
     * 
     * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        // Xiaohan, which quantity did you integrate for the fringe integral f1?
        //  was it the wave number k(z), or the field B(z)?  Looks like its the
        //  field.
        //
        //  - The integral needs to be done for a normalized k(z) or B(z), e.g.,
        //  B0 = 1 for the hard edge case.
        //  
        //  - Then we scale by the magnitude of the lens #getMagField() (in base class)
        //
        
        double Er = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double p = Math.sqrt(w*(w+2*Er));
        double q = probe.getSpeciesCharge();
        
        double f1 = this.getFringeIntegral1();
        
        //focus length 1/(L*sqrt(k))
        double focus = (q*f1*LightSpeed)/p;
        
        PhaseMatrix matPhi = PhaseMatrix.identity();
        
        matPhi.setElem( 1, 0, -focus);//X plane
        matPhi.setElem(3, 2, focus);// Y plane
        

 	   	PhaseMatrix Phidx = applyAlignError(matPhi);	
 	   	matPhi = Phidx;
 	   
        return new PhaseMap(matPhi);

    }

}
