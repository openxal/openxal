/*
 * IdealMagSectorDipole.java
 *
 * Created on March 10, 2004
 */

package xal.model.elem;

import java.io.PrintWriter;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.BendingMagnet;
import xal.tools.beam.optics.QuadrupoleLens;
import xal.tools.math.ElementaryFunction;
import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.sync.IElectromagnet;

/**
 * <p>Represents a bending magnetic dipole magnet for a beam in a sector 
 * configuration.  Thus, there are no edge effects as the beam enters
 * the magnet orthogonally.
 * </p>
 * 
 * <p>The MAD convention for sector magnets is followed
 * for coordinates, signs,  and lengths.
 * The formulation from D. Carey's Optics book, Transport manual, and
 * H. Wiedemann's books are used.
 * </p>
 *
 * NOTES:
 * <p> Both the dipole effects and the quadrupole (focusing) effects vary
 * off the design values with differing magnetic field strengths.  This
 * situation is in contrast with the previous version of this class
 * where only the quadrupole effects varied, and in contract with the
 * class <code>ThickDipole</code> where only the dipole effects varied.
 * </p>
 * <h3>References</h3>
 * <p>
 * [1] D.C. Carey, The Optics of Charged Particle Beams (Harwood, 1987)
 * <br>
 * [2] H. Wiedemann, Particle Accelerator Physics I, 2nd Ed. (Springer, 1999)
 * </p>
 * 
 * @author John D. Galambos
 * @author Jeff Holmes
 * @author Christopher K. Allen
 * 
 * @see xal.model.elem.IdealMagSectorDipole2
 * @see xal.model.elem.ThickDipole
 * 
 */
public class IdealMagSectorDipole2 extends ThickElectromagnet {


    
    /*
     * Global Constants
     */
    
    /** A small number used for comparing probe positions which are stepped via addition*/
    private static final double    EPS = 1e-12;
    
    

    /*
     *  Global Attributes
     */

    /** string type identifier for all IdealMagSectorDipole objects */
    public static final String s_strType = "IdealMagSectorDipole";


    /** Parameters for XAL MODEL LATTICE dtd */
    
    /** Tag for parameters in the XML configuration file */
    public static final String s_strPathLength = "PathLength";  // all thick elements have length - CKA
    
    /** Tag for parameters in the XML configuration file */
    public static final String s_strField = "MagField";
    
    /** Tag for parameters in the XML configuration file */
    public static final String s_strEntranceAngle = "EntranceAngle";
    
    /** Tag for parameters in the XML configuration file */
    public static final String s_strExitAngle = "ExitAngle";

    /** Tag for parameters in the XML configuration file */
    public static final String s_strQuadComponent = "QuadComponent";



    /*
     *  Local Attributes
     */
    /** K0 (no length) */
    private double K0 = 0;
    
    /** The gap height (m) */
    private double  dblGapHeight = 0.0;
    
    /** design orbit path length through magnet */
    private double  dblPathLen = 0.0;
    
    /** design orbit bend angle (radians) */
    private double  dblBendAng = 0.0;
    
    /** magnet quadrupole field index -(R0/B0)(dB/dR) */
    private double  dblQuadFldIndex = 0.0;

    /** SAD quad component. 
     * if dblQuadFldIndex = 0, and dblQuadComponent !=0,
     * kQuad = dblQuadComponent/len;
      */
	private double dblQuadComponent = 0.0;

    
    /** flag to use design field from bending angle and path instead of bfield */
//    private double bolFieldPathFlag = 0.0;
    private boolean bolFieldPathFlag = false;



    
    /*
     * Initialization
     */
    
    
    /** 
     * Default constructor - creates a new uninitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagSectorDipole2() {
	super(s_strType);
    };
    
    /** 
     * Default constructor - creates a new uninitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagSectorDipole2(String strId) {
        super(s_strType, strId);
    };
    
    /** 
     *  Creates a new instance of IdealMagSectorDipole 
     *
     *  @param  strId  identifier for this IdealMagSectorDipole object
     *  @param  dblFld field gradient strength (in <b>Tesla</b>)
     *  @param  dblLen pathLength of the dipole (in m)
     *  @param  dblGap  full pole gap of the dipole (in m)
     *  @param  dblFldInd  The dimensionless integral term for the extended fringe field focusing, 
     *                      Should be = 1/6 for linear drop off, ~ 0.4 for clamped Rogowski coil, 
     *                      or 0.7 for an unclamped Rogowski coil. (dimensionless)
     *
    */
    public IdealMagSectorDipole2(String strId, double dblLen, 
                                int enmOrient, double dblFld, 
                                double dblGap, double dblFldInd) 
    {
    super(s_strType, strId, dblLen);

        this.setGapHeight(dblGap);
        this.setMagField(dblFld);
        this.setFieldIndex(dblFldInd);
        this.setOrientation(enmOrient);
    };



    
    /**
     * This is the design bending curvature <i>h</i> = 1/<i>R</i><sub>0</sub> where
     * <i>R</i><sub>0</sub> is the design bending radius.
     * 
     * @return the design curvature of the bending magnet
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public double getK0() {
        return K0;
    }
    
    /**
     * Set the design curvature <i>h</i> of the bending magnet.
     *
     * @param dbl   design curvature <i>h</i> = 1/<i>R</i><sub>0</sub> where 
     *              <i>R</i><sub>0</sub> is the design path radius. 
     *
     * @author Christopher K. Allen
     * @since  Apr 19, 2011
     */
    public void setK0(double dbl) {
        K0 = dbl;
    }
    
    /**
     * Set the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * 
     *      n := -(R0/B0)(dB/dR)
     * 
     * where R0 is the radius of the design orbit, B0 is the field at the
     * design orbit (@see IdealMagSectorDipole#getField), and dB/dR is the
     * derivative of the field with respect to the path deflection - evaluated
     * at the design radius R0.
     * 
     * @param dblFldInd     field index of the magnet (unitless)     
     */
    public void setFieldIndex(double dblFldInd) {
        this.dblQuadFldIndex = dblFldInd;
    }
    
    /**
     * quad K1 component defined in SAD (=normal k1 * L)
     */
    public void setQuadComponent(double dbl) {
        this.dblQuadComponent = dbl;
    }
    
    
    /**
     * Set the gap size between the dipole magnet poles.
     * 
     * @param dblGap    gap size in <b>meters</b>
     */
    public void setGapHeight(double dblGap)  {
        this.dblGapHeight = dblGap;
    }
    
    /**
     * Set the reference (design) orbit path-length through
     * the magnet.
     * 
     * @param   dblPathLen      path length of design trajectory (meters)
     * 
     */
    public void setDesignPathLength(double dblPathLen)  {
        this.dblPathLen = dblPathLen;
    }
    
    /**
     * Set the bending angle of the reference (design) orbit.
     * 
     * @param   dblBendAng      design trajectory bending angle (radians) 
     */
    public void setDesignBendAngle(double dblBendAng)   {
        this.dblBendAng = dblBendAng;
    }

    /**
     * sako - to set field path flag.  
     * <code>true</code> means use design path throughout.
     * 
     * @param ba    new field path flag value
     */
    public void setFieldPathFlag(boolean ba) {
        bolFieldPathFlag = ba;
    }
    
    /*
     * Attribute Query
     */    
     
     
    /**
     * Return the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * <br>
     * <br>
     *      n := -(R0/B0)(dB/dR)
     * <br>
     * <br>
     * where R0 is the radius of the design orbit, B0 is the field at the
     * design orbit (see IdealMagSectorDipole#getField), and dB/dR is the
     * derivative of the field with respect to the path deflection - evaluated
     * at the design radius R0.
     * 
     * @return  field index of the magnet at the design orbit (unitless)     
     */
    public double getFieldIndex()   {
        return this.dblQuadFldIndex;
    }

    /**
     *  Returns the quadrupole field component of the bending magnet.
     *  
     * @return quadrupole field component of this magnet
     */
    public double getQuadComponent()   {
        return this.dblQuadComponent;
    }


    /**
     * Return the gap size between the dipole magnet poles.
     * 
     * @return  gap size in <b>meters</b>
     */
    public double getGapHeight()  {
        return this.dblGapHeight;
    }
    
    /**
     * Return the path length of the design trajectory through the
     * magnet.
     * 
     * @return      design trajectory path length (in meters)
     */
    public double   getDesignPathLength()   {
        return this.dblPathLen;
    }
    
    /**
     * Return the bending angle of the magnet's design trajectory.
     * 
     *  @return     design trajectory bending angle (in radians)
     */
    public double   getDesignBendingAngle() {
        return this.dblBendAng;
    }
    
    /**
     * Return the field path flag.
     * 
     *  @return     field path flag = 1 (use design field) or 0 (use bField parameter)
     */
    public boolean getFieldPathFlag() {
        return bolFieldPathFlag;
    }

    
    /*
     * Dynamic Parameters
     */
    
    /**
     * Compute and return the curvature of the design orbit through 
     * the magnet.  Note that this value is the inverse of the design 
     * curvature radius R0.
     * 
     * @return  the design curvature 1/R0 (in 1/meters)
     * 
     * @see IdealMagSectorDipole2#compDesignBendingRadius()
     */
    public double  compDesignCurvature()   {
        double  L0     = this.getDesignPathLength();
        double  theta0 = this.getDesignBendingAngle();
        
        double h0 = theta0/L0;
        return h0;
    }
    
    /**
     * Compute and return the bending radius of the design orbit  
     * throughout the magnet.  Note that this value is the inverse of  
     * design curvature h0.
     * 
     * @return  the design bending radius R0 (in meters)
     * 
     * @see IdealMagSectorDipole2#compDesignCurvature()
     */
    public double  compDesignBendingRadius()   {
        double  L0     = this.getDesignPathLength();
        double  theta0 = this.getDesignBendingAngle();
        
        double R0 = L0/theta0;
        
        return R0;
    }
    
    /**
     * Compute the path curvature within the dipole for the given probe. 
     * The path curvature is 1/R where R is the bending radius of the dipole
     * (radius of curvature).  Note that for zero fields the radius of
     * curvature is infinite while the path curvature is zero.
     * 
     * @param   probe   probe object to be deflected
     * 
     * @return  dipole path curvature for given probe (in <b>1/meters</b>)
     */
    public double   compProbeCurvature(IProbe probe) {

        return BendingMagnet.compCurvature(probe, this.getMagField());
    }
    
    
    /**
     * <p>Compute and return the quadrupole focusing constant for the current 
     * dipole settings and the given probe.  The curvature for the current
     * magnet settings and probe state is used - making this a dynamic quantity.
     * The field index does not change within the magnet.
     * </p>   
     * 
     * NOTE:
     * <p>- This value may be negative if the resulting curvature is negative
     * This condition means we are bending toward the negative x direction and
     * does not imply defocusing.
     * </p>
     * 
     * <p>- This value is essentially the same as the field index
     * of the magnet - the two values differ by a constant, the curvature
     * squared.  The square-root of this value provides the betatron
     * phase advance wave number.
     * </p>
     *
     * <br>
     * <br>
     *  K_quad := (1/R)(1/B)(dB/dR)
     * <br>
     * <br>
     *          = - h^2 * n0
     * <br>
     * <br>
     * <p>
     * where <i>K_quad</i> is the quadrupole focusing constant, <i>R</i> is 
     * the bending radius at current settings, <i>B</i> is the current magnet 
     * field strength , <i>h = 1/R</i> is the curvature at the current settings,
     * and <i>n0</i> is the field index. 
     * </p> 
     * 
     * @param   probe   we use the probe velocity to determine curvature
     *  
     * @return          quadrupole focusing constant (in 1/meters^2)
     */
    public double   compQuadrupoleConstant(IProbe probe)    {
        double      n0 = this.getFieldIndex();
        double      h  = BendingMagnet.compCurvature(probe, this.getMagField());
        
        double k_quad = - h*h*n0;
        
         if (n0 == 0) {
        	 k_quad = getQuadComponent();
         }
        
        return k_quad;
    }
    
    /**
     * <p>
     * Computes and returns the path length variation factor.  This is the factor by with the 
     * synchronous particle path length expands or contracts about the design path length when
     * considering the effects of dipole field strength other than the design value.
     * Denoting this quantity as <i>w</i> then it can be expressed
     * <br>
     * <br>
     * &nbsp; &nbsp; <i>w</i> = 1 - <i>h</i>/<i>h</i><sub>0</sub> ,
     * <br>
     * <br>
     * where <i>h</i><sub>0</sub> is the bending curvature of the design field and <i>h</i> is the
     * bending curvature of the current field strength.  Thus, for a distance &Delta;<i>s</i><sub>0</sub>
     * along the design path, the synchronous particle actually travels a distance
     * <br>
     * <br>
     * &nbsp; &nbsp; &Delta;<i>s</i> = <i>w</i>&Delta;<i>s</i><sub>0</sub>
     * <br>
     * <br>
     * along the actual path. 
     * </p>
     *  
     * @param probe     probe moving a some velocity determining the bending curvature
     * 
     * @return          the path length variation factor <i>w</i>
     *
     * @author Christopher K. Allen
     * @since  Nov 27, 2013
     */
    public double   compPathLengthVariationFactor(IProbe probe) {

        // Get the field magnitude and check that it is not zero.  (If so there is no length variation.)
        final double B  = this.getMagField();
        
        if (B == 0.0) 
            return 1.0;
        
        
        // Compute the bending curvature for the design and for the current field magnitude
        double h0 = this.compDesignCurvature();// h0 polarity = alpha polarity
        double h  = 0;
        if (getFieldPathFlag() == false) {
            
            h = BendingMagnet.compCurvature(probe, B);// h polarity = e*B0 polarity
            
        } else {
            
            h =  h0;
            
        } 
        
        /*
         * Compute and return path variation parameter
         */
        final double dblLenFactor = 1.0 - h/h0;
        
        return dblLenFactor;
    }
 
    /*
     *  ThickElement Protocol
     */

    /**
     * Returns the time taken for the probe to drift through part of the
     * element.
     * 
     *  @param  probe   propagating probe
     *  @param  dblLen  length of subsection to propagate through <b>meters</b>
     *  
     *  @return         the elapsed time through section<b>Units: seconds</b> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        
        // Convert the given design path length to actual length
        double  dblLenFac = this.compPathLengthVariationFactor(probe);
        double  dblLenVar = dblLenFac*dblLen;
        
        // Compute the elapsed time drifting to the modified path length
        double  dblTime = super.compDriftingTime(probe, dblLenVar);
        
        return dblTime;
    }
    
    /**
     *  Return the energy gain imparted to a particular probe.  For an ideal quadrupole
     *  magnet this value is always zero.
     *
     *  @param  dblLen  dummy argument
     *  @param  probe   dummy argument
     *
     *  @return         returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    };



    /**
     * <p>Compute the partial transfer map of an ideal sector magnet for the 
     * particular probe.  Computes transfer map for a section of magnet
     * <code>h</code> meters in length.
     * </p>
     * 
     * NOTE
     * <p>The <b>arc length</b> <i>dL</i> of the probe will probably be larger 
     * than the physical step length <i>h</i>.  This is because the path length
     * of the design trajectory is generally larger than the physical length
     * (otherwise no bending would occur).
     * </p>
     *
     *  @param  dblLen  physical step length (meters)
     *  @param  probe   uses the rest and kinetic energy parameters from the probe
     *
     *  @return         transfer map of ideal sector magnet for particular probe
     *
     *  @exception  ModelException    unknown quadrupole orientation
     */
    @Override
    public PhaseMap transferMap(final IProbe probe, final double dblLen) 
        throws ModelException
    {
        
        /*
         *  Get parameters
         */
        
        final double B     = this.getMagField();
        final double gamma = probe.getGamma();
        
        //
        // Check for zero fields - we cannot support this
        //  Actually, now we replace the dipole magnet with a drift space if zero fields are detected.
        //  This situation assumes a straight section since a real-world beam would likely hit the 
        //  wall of an unenergized magnetic.
        //
        if (( (getFieldPathFlag() == true)  && (getDesignBendingAngle() == 0.) )	|| 
             ((getFieldPathFlag() == false) && (B == 0.0)) ) 
        {
            //      throw new ModelException("IdealMagSectorDipole#transferMap() - cannot support zero fields.");
        	
            //sako 27 sep 07 to avoid B=0 problem
            // Build transfer matrix for a drift space.  Assume the magnet has been replaced by a drift section
    		PhaseMatrix  matPhi  = new PhaseMatrix();
                
    		double mat0[][] = new double [][] {{1.0, dblLen}, {0.0, 1.0}};
    		matPhi.setSubMatrix(0,1, 0,1, mat0);
    		matPhi.setSubMatrix(2,3, 2,3, mat0);
    		matPhi.setSubMatrix(4,5, 4,5, mat0);
    		matPhi.setElem(6,6, 1.0);
  
    		return new PhaseMap(matPhi);
        }
        
        /*
         * Compute the bending constant h == 1 / bend radius (1/meter)
         */
        
        final double h0 = this.compDesignCurvature();// h0 polarity = alpha polarity
        double h  = 0;
        if (getFieldPathFlag() == false) {
            
        	h = BendingMagnet.compCurvature(probe, B);// h polarity = e*B0 polarity
        	
        } else if (getFieldPathFlag() ==  true)  {
            
        	h =  h0;
        	
        } else {
            
        	h = this.getK0();
        }
       // 28 Nov 07 H. Sako
       // alpha definition same as trace3d
        //regardless sign of the particles, right bend for alpha>0, left bend for alpha<0
        //since M15 term polarity is h polarity = alpha polarity in both.
        // for negative particles, B negative -> alpha positive
             
        // in trace3d, alpha>0 with x- kick (right)
        //            alpha>0 with y+ kick (upper)
        // for both positive and negative particles
        // rho = alpha/path , same sign with alpha
        
        final double R0 = 1.0 / h0;
        final double R  = 1.0 / h;
        
        /*
         * Compute the arc length step size
         */
        final double dAng = this.compAngleStepSize(probe, dblLen);
        final double dL   = Math.abs( dAng*R0 );
        
        /*
         * Compute path variation parameter
         */
        final double zprimeProbe = (R - R0) / (R * gamma * gamma);
        
        
        /*
         *  Compute quadrupole focusing constants
         */
        final double kQuad = this.compQuadrupoleConstant(probe);
        final double kBend = h*h + kQuad;

        final double kx = Math.sqrt(Math.abs(kBend));
        final double ky = Math.sqrt(Math.abs(kQuad));
        
        /*
         *
         * Compute the transfer matrix components.
         *
         */
        
        double[][] arrZero = new double[][] {{0,0},{0,0}};
        double[][] arrWork = arrZero;
        
        double M05;
        double M15;
        double M40;
        double M41;
        double M45;
        double M06;
        double M16;
        double M46;
        
        
        if(kBend >= 0.0)
        {
            arrWork = QuadrupoleLens.transferFocPlane(kx, dL);
            M05 = gamma * gamma * h0 * (1. - Math.cos(kx * dL)) / (kx * kx);
            M15 = gamma * gamma * h0 * Math.sin(kx * dL) / kx;
            M40 = -h0 * Math.sin(kx * dL) / kx;
            M41 = -h0 * (1. - Math.cos(kx * dL)) / (kx * kx);
            M45 = dL - gamma * gamma * h0 * h0 *
            (kx * dL - Math.sin(kx * dL)) / (kx * kx * kx);
        }
        else
        {
            arrWork = QuadrupoleLens.transferDefPlane(kx, dL);
            M05 = gamma * gamma * h0 *
            (ElementaryFunction.cosh(kx * dL) - 1.) / (kx * kx);
            M15 = gamma * gamma * h0 * ElementaryFunction.sinh(kx * dL) / kx;
            M40 = -h0 * ElementaryFunction.sinh(kx * dL) / kx;
            M41 = -h0 * (ElementaryFunction.cosh(kx * dL) - 1.) / (kx * kx);
            M45 = dL - gamma * gamma * h0 * h0 *
            (ElementaryFunction.sinh(kx * dL) - kx * dL)
            / (kx * kx * kx);
        }
        
        M05 *= R / R0;
        M15 *= R / R0;
        M45 *= R / R0;
        M06 = M05 * zprimeProbe;
        M16 = M15 * zprimeProbe;
        M46 = M45 * zprimeProbe;
        
        final double[][] arrX = arrWork;
        
        
        arrWork = arrZero;
        
        if(kQuad >= 0.0)
        {
            arrWork = QuadrupoleLens.transferDefPlane(ky, dL);
        }
        else
        {
            arrWork = QuadrupoleLens.transferFocPlane(ky, dL);
        }
        
        final double[][] arrY = arrWork;
        
        
        /**
         *
         * Build the dipole body tranfer matrix.
         *
         */
        
        PhaseMatrix matBody = PhaseMatrix.identity();
   
        switch (this.getOrientation()) {

        case IElectromagnet.ORIENT_HOR:
            
            matBody.setSubMatrix(0, 1, 0, 1, arrX);
            matBody.setSubMatrix(2, 3, 2, 3, arrY);
            
            matBody.setElem(0, 5, M05);
            matBody.setElem(1, 5, M15);
            
            matBody.setElem(4, 0, M40);
            matBody.setElem(4, 1, M41);
            matBody.setElem(4, 5, M45);
            
            matBody.setElem(0, 6, M06);
            matBody.setElem(1, 6, M16);
            matBody.setElem(4, 6, M46);
            
            break;
            
        case IElectromagnet.ORIENT_VER:
        	
            matBody.setSubMatrix(0, 1, 0, 1, arrY);
            matBody.setSubMatrix(2, 3, 2, 3, arrX);
            
            matBody.setElem(2, 5, M05);
            matBody.setElem(3, 5, M15);
            
            matBody.setElem(4, 2, M40);
            matBody.setElem(4, 3, M41);
            matBody.setElem(4, 5, M45);
            
            matBody.setElem(2, 6, M06);
            matBody.setElem(3, 6, M16);
            matBody.setElem(4, 6, M46);
   
            break;
        }
        
        //4 Feb 08, sako try to apply align error be careful.
       
  	   PhaseMatrix Phidx = applyAlignError(matBody);	
	   matBody = Phidx;

       return new PhaseMap( matBody );     
       

    }
    


    /*
     * Internal Support
     */


	/**
     * Compute the step in the design trajectory angle given the physical
     * step size <i>dL</code> at the current <code>probe</code> position.
     * 
     * @param   probe   probe object with magnet domain
     * @param   dL      physical distance to step within magnet
     * 
     * @return  the step angle corresponding to dL (in radians)
     * 
     * @author  Christopher K. Allen
     */
    private double  compAngleStepSize(IProbe probe, double dL)  {
        double  s1 = this.compProbeLocation(probe);
        double  s2 = s1 + dL;

//        double  a1 = this.compCurrentAngle(s1);
//        double  a2 = this.compCurrentAngle(s2);
        double  a1 = this.compCurrentAngle2(s1);
        double  a2 = this.compCurrentAngle2(s2);
 
        double  dAng = a2 - a1;
        
        return dAng;
    }
    
    /**
     * <p>Compute and return the partial deflection angle of the
     * design trajectory at position <i>s</i> within the magnet.
     * Note that <i>s</i> is not the position along the design
     * trajectory.  That value is found by multiplying the
     * returned value by the curvature radius.
     * </p>
     * 
     * NOTE
     * <p> This function is necessary since the space charge calculations
     * step through the <b>physical</b> distance of the magnet, not the
     * design path.
     * </p>
     * 
     * <p> The result is computed using repeated application of the law
     * of cosines.
     * </p>
     * 
     *  @param  s   physical distance from magnet entrance location (meters)
     * 
     *  @return     the partial deflection angle at distance s
     *  
     *  @author Christopher K. Allen
     *  @author sako   2007/11/27, exception handling
     *  
     *  @deprecated This method provides suspicious results and has been replaced by 
     *              compCurrentAngle2()
     */
    @Deprecated
    private double  compCurrentAngle(double s) {
        double  R0 = this.compDesignBendingRadius();
        double  L0 = this.getLength();

        double num = R0 - ( (L0/2.0)/R0 )*s;
        //double den = Math.sqrt( s*s + R0*R0 - s*L0 );
        double den = Math.sqrt( R0*R0 + s*(s - L0) );
        double ratio = num/den;

        if (ratio>1) {
            
            ratio = 1;
        
        } else if (ratio<-1) {
        
            ratio = -1;
        }
        
        double theta = Math.acos(ratio);
//        double theta2 = compCurrentAngle2(s);
        return theta;
    }
    
    /**
     * <p>
     * Compute and return the partial deflection angle of the
     * design trajectory at a distance <i>s</i> from the magnet
     * entrance.
     * Note that here <i>s</i> is <b>not</b> the position along the design
     * trajectory.  That value is found by multiplying the
     * returned value by the bending radius of the magent.
     * </p>
     * <p>
     * <h4>NOTES</h4>
     * &middot; I have rewritten this method yet again because I am unsure of
     * the previous formula derivations within the other methods.
     * <br/>
     * &middot; THe formulas derived here are quite simple, based solely
     * on trigonometry applied to regions within the bending radius of the 
     * magnet.
     * <br/>
     * &middot; This function is necessary since the space charge calculations
     * step through the <b>physical</b> distance of the magnet, not the
     * design path.
     * <br/>
     * &middot; The result is computed using repeated application of the law
     * of cosines.
     * </p>
     * 
     * @param s     the distance within the magnetic from the entrance,
     *              Note <var>s</var> must be in the interval [0,<i>L</i><sub>0</sub>] where
     *              <i>L</i><sub>0</sub> is the length of the magnet
     * 
     * @return      the partial bending angle at that internal position
     * 
     * @throws  IllegalArgumentException s not in the interval [0,<i>L</i><sub>0</sub>] 
     *          (no longer thrown - messages are sent to system error stream) 
     *
     * @since  Jul 8, 2015   by Christopher K. Allen
     */
    private double compCurrentAngle2(double s) throws IllegalArgumentException {
        double  L0   = this.getLength();
        double theta = Math.abs( this.getDesignBendingAngle() );
        
        // Check for illegal argument
        if (s < 0.0 - EPS) {
            System.err.println("IdealMagSectorDipole#compCurrentAngle2(): probe position s=" + s + " is less than 0, i.e. before the dipole entrance");
            
            return 0.0;
        }
        
        if (s > L0 + EPS) {
            System.err.println("IdealMagSectorDipole#compCurrentAngle2(): probe position s=" + s + " is greater than L0, i.e. outside dipole exit");
            
            return theta;
        }

        // Compute trigonometric angles and partial angles
        double R0    = Math.abs( this.compDesignBendingRadius() );

        double thetaBy2 = Math.asin( (L0/2)/R0 );  // have the total bending angle
        double partDefl = Math.asin( (s - (L0/2))/R0 ); // partial deflection for s>L0/2
                                                            // complement of partial deflection for s<L0/2
        
        // Compute the deflection angle for a position s meters into the magnet
        //  and return it.
        double  dTheta = thetaBy2 + partDefl;
        
        return dTheta;
    }

    /**
     * This method approximates the partial deflection angle of the
     * design trajectory a position <i>s</i> within the magnet.  This 
     * method may be somewhat faster than 
     * <code>IdealMagSectorDipole#compCurrentAngle(double)</code> by
     * avoiding the computation of one arc-cosine.  The returned value
     * is the derivative of the above function at <i>s</i> = 0 times
     * <i>s</i>.
     * 
     *  @param  s   physical distance from magnet entrance location (meters)
     * 
     *  @return     the partial deflection angle at distance s
     *  
     *  @author Christopher K. Allen
     *  
     *  @see    IdealMagSectorDipole2#compCurrentAngle(doube)
     */
    @SuppressWarnings("unused")
    private double approxCurrentAngle(double s) {
        
        double  h0 = this.compDesignCurvature();
        double  L0 = this.getLength();
        
        double  area = h0*L0/2;
        
        return s*h0*Math.sqrt(1 - area*area);
    }
    
    

    /*
     *  Testing and Debugging
     */

    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    @Override
    public void print(PrintWriter os) {
	super.print(os);

	os.println("  magnetic field     : " + this.getMagField());
	os.println("  magnet orientation : " + this.getOrientation());
    };

};
