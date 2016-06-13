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
 * Represents the action of a quadrupole face as a thin lens effect.
 *   
 * @author Christopher K. Allen
 * @author X.H.Lu
 * @version June 13,2016
 *
 */
public class IdealMagFringeQuadFace extends ThinElectromagnet {

    /*
     *  Global Attributes
     */
    
    /** the string type identifier for all IdealMagFringeQuadFace */
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
    
 
    /** 1st moment of fringe field */
    private double              dblFringeInt1 = 0.0;
    /** 2nd moment of fringe field */
    private double              dblFringeInt2 = 0.0;

    /** flag to use design field instead of bfield */
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
     * IdealMagFringeQuadFace.      
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
