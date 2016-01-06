/*
 *  IdealMagDipoleFace
 * 
 * Created on May 17, 2004
 *
 */
package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.BendingMagnet;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IElectromagnet;

/**
 * Represents the action of a rotated dipole face as a thin lens effect.  Note
 * that there is always an associated dipole magnet for any 
 * <code>IdealMagDipoleFace</code>.  The two objects should provide the same 
 * values for the <code>IElectromagnet</code> interface.  Note that a dipole
 * face rotation has the same effect both on beam entering the dipole or
 * exiting the dipole.  
 * The model for the pole face effect is taken from D.C. Carey's book.
 *   
 * @author Christopher K. Allen
 *
 *  @see    "D.C. Carey, The Optics of Charged Particle Beams (Harwood, 1987)"
 *  
 *  @deprecated This class has been replaced by <code>IdealMagDipoleFace2</code>
 */
@Deprecated
public class IdealMagDipoleFace extends ThinElectromagnet {

    /*
     *  Global Attributes
     */
    
    /** the string type identifier for all IdealMagSteeringDipole's */
    public static final String      s_strType = "IdealMagDipoleFace";
    
    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String      s_strParamLenEff = "EffLength";
    public static final String      s_strParamOrient = "Orientation";
    public static final String      s_strParamField  = "MagField";
    
    
    
    
    
    /*
     *  Local Attributes
     */

    
    /** The dipole gap height (m) */
    private double              m_dblGap = 0.0;

    /** internal pole face angle made with respect to the design trajectory */
    private double              m_dblAngFace = 0.0;
    
    /** second moment of fringe field defined a al Carey */
    private double              m_dblMmtFrng = 0.0;
    

    
    /*
     * Initialization
     */
    
    /**
     * Default constructor - creates a new unitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagDipoleFace() {
        super(s_strType);
    }

    /**
     * Constructor providing the instance identifier for the element.
     * 
     * @param strId     string identifier for element
     */
    public IdealMagDipoleFace(String strId) {
        super(s_strType, strId);
    }
    
    /**
     * Set the angle between the pole face normal vector and the design 
     * trajectory.  This can be either at the magnet entrance or exit, the
     * effect is the same.
     * 
     * @param dblAngPole    pole face angle in <b>radians</b>
     * 
     */
    public void setPoleFaceAngle(double dblAngPole) {
        this.m_dblAngFace = dblAngPole;
    }
    
    /**
     * Set the gap height between the magnet poles.
     * 
     * @param dblGap    gap size in <b>meters</b>
     */
    public void setGapHeight(double dblGap)  {
        this.m_dblGap = dblGap;
    }
    
    /**
     * Set the second-order moment integral of the dipole fringe field
     * as described by D.C. Carey.  The integral determines the amount of 
     * defocusing caused by the fringe field.  Denoting the integral <i>I2</i>
     * it has the definition
     * 
     *      I2 := Integral{ B(z)[B0 - B(z)]/(g B0^2) }dz
     * 
     * where <i>g</i> is the gap height, <i>B0</i> is the hard edge value for 
     * the magnetic field, and <i>B(z)</i> is the true magnetic field along the
     * design trajectory with path length parameter <i>z</i>.  The integral 
     * taken from a location <i>z0</i> within the magnet where <i>B(z0)=B0</i>
     * out to <i>z</i> = infinity.
     * 
     * Some examples values are the following:
     *      I2 = 0.1666     linear drop off
     *      I2 = 0.4        clamped Rogowski coil
     *      I2 = 0.7        unclamped Rogoski coil
     * 
     * @param   dblFrngMmt  field moment I2 (<b>dimensionless</b>)
     */
    public void setFringeIntegral(double dblFrngMmt) {
    }

    

    //hs bend angle
    private double bendAngle = 0.0;
    private double fieldPathFlag = 0.0;
    private double pathLength = 0.0;
    
    public void setPathLength(double pl) {
  	  pathLength = pl;
     }
    public void setBendAngle(double ba) {
	  bendAngle = ba;
     }
    public void setFieldPathFlag(double ba) {
	  fieldPathFlag = ba;
     }
    public double getPathLength() {
	  return pathLength;
     }
    public double getBendAngle() {
	  return bendAngle;
    }
    public double getFieldPathFlag() {
	  return fieldPathFlag;
    }
    /*
     * Accessors
     */
     
    /**
     * Return distance between dipole magnet poles.
     * 
     * @return      gap height in <b>meters</b>
     */
    public double  getGapHeight()  {
        return this.m_dblGap;
     }

    /**
     * Return the angle between the pole face normal vector and the design 
     * trajectory.  This can be either at the magnet entrance or exit, the
     * effect is the same.
     * 
     * @return       pole face angle in <b>radians</b>
     */    
    public double   getPoleFaceAngle()  {
        return this.m_dblAngFace;     
    }
    
    /**
     * Set the second-order moment integral of the dipole fringe field
     * as described by D.C. Carey.  The integral determines the amount of 
     * defocusing caused by the fringe field.  
     * 
     * @return   second-order integral of fringe field (<b>dimensionless</b>)
     * 
     * @see IdealMagDipoleFace#setFringeIntegral(double)
     */
    public double  getFringeIntegral() {
        return this.m_dblMmtFrng;
    }



    /*
     * IElement Interface
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
     * @param probe
     * @return
     * @throws ModelException
     * 
     * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        // Get  parameters
        double B  = this.getMagField(); // opposite
        double g  = this.getGapHeight();
        double I2 = this.getFringeIntegral();
        double h  = BendingMagnet.compCurvature(probe, B);

 
    	double bPathFlag = this.getFieldPathFlag();

    	double w = probe.getKineticEnergy();
  
    	if (bPathFlag==1.) {
    	       //hs calculate hrho
    	    double path = this.getPathLength();
    	    double alpha = this.getBendAngle();
    	    double hrho=0;
    	  	if (path != 0) {
    		  hrho = alpha/path;
            }
    	    h = hrho; //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
    	}
    	
        
        double q = probe.getSpeciesCharge();
        double Etotal = probe.getSpeciesRestEnergy() * probe.getGamma();
        double beta = probe.getBeta();


        //System.out.print("name= " + probe.getCurrentElement() + " h = " + new Double(h));
        //System.out.println(" nQ = " + new Double(nQ));

        // The fringe field angle from the extended field:
        double dblAngFace = this.getPoleFaceAngle();
        double sin  = Math.sin(dblAngFace);
        double cos  = Math.cos(dblAngFace);
        double dblAngDefl = g * h * ((1. + sin*sin )/cos) * I2;

        // Compute the transfer matrix components
//original        double      hStar = h * q / Math.abs(q);
        double hStar = h;
        PhaseMatrix matPhi = PhaseMatrix.identity();
    
        switch (this.getOrientation())  {
            case IElectromagnet.ORIENT_HOR:
                matPhi.setElem(1,0 , hStar* Math.tan(dblAngFace));
                matPhi.setElem(3,2, -hStar* Math.tan(dblAngFace - dblAngDefl));
                break;
                
            case IElectromagnet.ORIENT_VER:
                matPhi.setElem(1,0 , -hStar* Math.tan(dblAngFace - dblAngDefl));
                matPhi.setElem(3,2,   hStar* Math.tan(dblAngFace));
                break;
                
            default:
                throw new ModelException("IdealMagDipoleFace#transferMap() - bad magnet orientation.");
        }

        matPhi = applyAlignError(matPhi);
        
        return new PhaseMap(matPhi);

    }

}
