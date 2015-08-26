/*
 *  IdealMagDipoleFace
 * 
 * Created on May 17, 2004
 *
 */
package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

import xal.model.IProbe;
import xal.model.ModelException;

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
 */
public class IdealMagFringeQuadFace extends ThinElectromagnet {

    /*
     *  Global Attributes
     */
    
    /** the string type identifier for all IdealMagSteeringDipole's */
    public static final String      s_strType = "IdealMagFringeQuadFace";
    
    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String      s_strParamLenEff = "EffLength";
    public static final String      s_strParamOrient = "Orientation";
    public static final String      s_strParamField  = "MagField";
    
    
    /*
     *  Local Attributes
     */
    
  
   
    /** bending plane of dipole */
    private boolean                 entrFlag = true;
    
    public void setEntrFlag(boolean entr) {
    	entrFlag  =  entr;
    }
    
    /** BRho Scaling factor (only valid when fieldpathflag=1 */
    private double bRhoScaling = 1;
    public double getBRhoScaling() {
    	return bRhoScaling;
    }
    
   public void setBRhoScaling(double d) {
    	// TODO Auto-generated method stub
    	bRhoScaling = d;
    }
    
 
    /** 1st moment of fringe field defined a al H. Matsuda */
    private double              dblFringeInt1 = 0.0;
    /** 2nd moment of fringe field defined a al H. Matsuda */
    private double              dblFringeInt2 = 0.0;

    /** flag to use design field from bending angle and path instead of bfield */
    private double fieldPathFlag = 0.0;
    /** K1 (T/m) length excluded */
    private double  K1 = 0.0;
    
    private double nominalKineEnergy = 0.0;
 
    public void setNominalKineEnergy(double ba) {
    	nominalKineEnergy = ba;
    }
 
    public double getNominalKineEnergy() {
	return nominalKineEnergy;
    }
    
    /*
     * Initialization
     */
    
    /**
     * Default constructor - creates a new unitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagFringeQuadFace() {
        super(s_strType);
    }

    
    /**
     * K1 (T/m)
     * @return K1
     */
    public double getK1() {
    	return K1;
    }
    
    /**
     * K1 (T/m)
     */
    public void setK1(double dbl) {
    	K1 = dbl;
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
     * Set the first-order moment integral of the dipole fringe field
     * as described by H. Matsuda.  The integral determines the amount of 
     * defocusing caused by the fringe field.  Denoting the integral <i>f1</i>
     * has the definition
      * 
     *      f1 := sign(a)sqrt(a)
     *      a  = -24 [I1sad-I0sad^2/2]
     *      where I(n)sad := Integral{[z - z0] k(z)/k0}dz
      *     
     * where <i>k0</i> is normal k0*Len. The integral 
     * taken from -infinity to <i>z</i> = infinity.
      * 
     * @param   dblFringeInt  field moment f1 (<b>dimensionless</b>)
     */
    public void setFringeIntegral1(double dblFringeInt) {
        this.dblFringeInt1 = dblFringeInt;
    }
    /**
     * Set the first-order moment integral of the dipole fringe field
     * as described by H. Matsuda.  The integral determines the amount of 
     * defocusing caused by the fringe field.  Denoting the integral <i>f1</i>
     * has the definition
      * 
     *      f2 := [I2sad-I0sad^2/3]
     *      where I(n)sad := Integral{[z - z0] k(z)/k0}dz
      *     
     * where <i>k0</i> is normal k0*Len. The integral 
     * taken from -infinity to <i>z</i> = infinity.
      * 
     * @param   dblFringeInt  field moment f1 (<b>dimensionless</b>)
     */
    public void setFringeIntegral2(double dblFringeInt) {
        this.dblFringeInt2 = dblFringeInt;
    }
    /**
     * sako to set field path flag
     * @param ba
     */
    public void setFieldPathFlag(double ba) {
        fieldPathFlag = ba;
    }
 
 
    /*
     * Property Query
     */

    /**
     * Set the second-order moment integral of the dipole fringe field
     * as described by H. Matsuda.  The integral determines the amount of 
     * defocusing caused by the fringe field.  
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

   
    public boolean getEntr() {
        return this.entrFlag;
    };


    
    /**
     * Return the field path flag.
     * 
     *  @return     field path flag = 1 (use design field) or 0 (use bField parameter)
     */
    public double getFieldPathFlag() {
        return fieldPathFlag;
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
     * Compute and return the transfer map for this dipole magnet
     * pole face element.
     * @param probe
     * @return
     * @throws ModelException
     * 
     * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
     */
    @Override
    protected PhaseMap transferMap(IProbe probe) throws ModelException {

        double Er = probe.getSpeciesRestEnergy();
        double w = probe.getKineticEnergy();
        double p = Math.sqrt(w*(w+2*Er));
       
        double bPathFlag = getFieldPathFlag();
        if (bPathFlag == 1) {//if bpathflag =1, then use nominal k0 from nominal kine energy
        	double w0 = getNominalKineEnergy();
        	if (w0==0.) {
        		w0 = probe.getKineticEnergy();
        		setNominalKineEnergy(w0);
        	}
        	double p0 = Math.sqrt(w0*(w0+2*Er));
		
			setBRhoScaling(p/p0);//save brho scaling. when nominalKineEnergy = 0, set 1.
		}
	    
        // Get  parameters
        double f1 = this.getFringeIntegral1();
        double f2 = this.getFringeIntegral2();
        double I1 = Math.signum(f1)*f1*f1;
        double I2 = -0.5*f2;
        
		 double q = probe.getSpeciesCharge();
     
        double k = 0;
        
        if (bPathFlag == 0) {
     	   k = (q * LightSpeed * getMagField() ) / p;
        } else if (bPathFlag == 1) {//brhoscaling
     	   k =  (q * LightSpeed * getMagField() *getBRhoScaling())/p;
        } else {
     	   k = getK1();
        }
     

        PhaseMatrix matPhi = PhaseMatrix.identity();
    
        if (entrFlag) {
        	matPhi.setElem(0,0, 1-k*I1);
        	matPhi.setElem(0,1, -2*k*I2);
        	//matPhi.setElem(1,0, -k*k*I3);
        	matPhi.setElem(1,1, 1+k*I1);
        	
          matPhi.setElem(2,2, 1+k*I1);
        	matPhi.setElem(2,3, 2*k*I2);
        	//matPhi.setElem(3,2, -k*k*I3);
        	matPhi.setElem(3,3, 1-k*I1);
        } else {
           matPhi.setElem(0,0, 1+k*I1);
        	matPhi.setElem(0,1, -2*k*I2);
        	//matPhi.setElem(1,0, -k*k*I3);
        	matPhi.setElem(1,1, 1-k*I1);
        	
          matPhi.setElem(2,2, 1-k*I1);
        	matPhi.setElem(2,3, 2*k*I2);
        	//matPhi.setElem(3,2, -k*k*I3);
        	matPhi.setElem(3,3, 1+k*I1);
        }
        

 	   	PhaseMatrix Phidx = applyAlignError(matPhi);	
 	   	matPhi = Phidx;
 	   
        return new PhaseMap(matPhi);

    }

}
