/*
 * IdealMagSectorDipole.java
 *
 * Created on March 10, 2004
 */

package eu.ess.jels.model.elem.els;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IElectromagnet;
import xal.model.elem.ThickElectromagnet;
import xal.model.elem.ThickElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;

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
 * <p>
 * <h4>References</h4>
 * [1] D.C. Carey, The Optics of Charged Particle Beams (Harwood, 1987)
 * <br/>
 * [2] H. Wiedemann, Particle Accelerator Physics I, 2nd Ed. (Springer, 1999)
 * </p>
 * 
 * @author John D. Galambos
 * @author Jeff Holmes
 * @author Christopher K. Allen
 * 
 * @see xal.model.elem.IdealMagSectorDipole2
 * @see xal.model.elem.ThickDipole
 */
public class IdealMagSectorDipole2 extends ThickElectromagnet {



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
    private double fieldPathFlag = 0.0;



    
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
     * sako to set field path flag
     * @param ba
     */
    public void setFieldPathFlag(double ba) {
        fieldPathFlag = ba;
    }
    
    /*
     * Attribute Query
     */    
     
     
    /**
     * Return the magnetic field index of the magnet evaluated at the design
     * orbit.   The field index is defined as
     * 
     *      n := -(R0/B0)(dB/dR)
     * 
     * where R0 is the radius of the design orbit, B0 is the field at the
     * design orbit (@see IdealMagSectorDipole#getField), and dB/dR is the
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
    public double getFieldPathFlag() {
        return fieldPathFlag;
    }

    
    /*
     * Dynamic Parameters
     */
 
    /**
     * Compute and return the bending radius of the design orbit  
     * throught the magnet.  Note that this value is the inverse of  
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
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return super.compDriftingTime(probe, dblLen);
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
    	PhaseMatrix  matPhi  = new PhaseMatrix();

    	double rho = compDesignBendingRadius();
    	double alfa = dblLen/rho;
    	double N = getFieldIndex();
    	
    	double h=Math.signum(alfa)/Math.abs(rho);
    	double kx=Math.sqrt(1-N)*Math.abs(h);
    	double ky=Math.sqrt(N)*Math.abs(h);

    	double Deltas=dblLen;
 
    	if (N==0 && getOrientation() == IElectromagnet.ORIENT_HOR)
    	{
    		matPhi.setElem(0,0,Math.cos(kx*Deltas));
    		matPhi.setElem(0,1,Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(1,0,-kx*Math.sin(kx*Deltas));
    		matPhi.setElem(1,1,Math.cos(kx*Deltas));

    		matPhi.setElem(2,2,1);
    		matPhi.setElem(2,3,Deltas);
    		matPhi.setElem(3,3,1);

    		matPhi.setElem(4,4,1);
    		//matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getBeta()*probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(5,5,1);

    		matPhi.setElem(4,0,-h*Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(4,1,-h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));

    		matPhi.setElem(0,5,h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));
    		matPhi.setElem(1,5,h*Math.sin(kx*Deltas)/kx);
    	}
    	if (N==0 && getOrientation()==IElectromagnet.ORIENT_VER)
    	{
    		matPhi.setElem(0,0,1);
    		matPhi.setElem(0,1,Deltas);
    		matPhi.setElem(1,1,1);

    		matPhi.setElem(2,2,Math.cos(kx*Deltas));
    		matPhi.setElem(2,3,Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(3,2,-kx*Math.sin(kx*Deltas));
    		matPhi.setElem(3,3,Math.cos(kx*Deltas));

    		matPhi.setElem(4,4,1);
    		//matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getBeta()*probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(5,5,1);

    		matPhi.setElem(4,2,-h*Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(4,3,-h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));

    		matPhi.setElem(2,5,h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));
    		matPhi.setElem(3,5,h*Math.sin(kx*Deltas)/kx);
    	}
    	if (N>0 && N<1 && getOrientation()==IElectromagnet.ORIENT_HOR)
    	{
    		matPhi.setElem(0,0,Math.cos(kx*Deltas));
    		matPhi.setElem(0,1,Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(1,0,-kx*Math.sin(kx*Deltas));
    		matPhi.setElem(1,1,Math.cos(kx*Deltas));

    		matPhi.setElem(2,2,Math.cos(ky*Deltas));
    		matPhi.setElem(2,3,Math.sin(ky*Deltas)/ky);
    		matPhi.setElem(3,2,-ky*Math.sin(ky*Deltas));
    		matPhi.setElem(3,3,Math.cos(ky*Deltas));

    		matPhi.setElem(4,4,1);
    		//matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getBeta()*probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(4,5,-Math.pow(h,2)*(kx*Deltas*Math.pow(probe.getBeta(),2)-Math.sin(kx*Deltas))/Math.pow(kx,3)+Deltas/Math.pow(probe.getGamma(),2)*(1-Math.pow(h/kx,2)));
    		matPhi.setElem(5,5,1);

    		matPhi.setElem(4,2,-h*Math.sin(kx*Deltas)/kx);
    		matPhi.setElem(4,3,-h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));

    		matPhi.setElem(2,5,h*(1-Math.cos(kx*Deltas))/Math.pow(kx,2));
    		matPhi.setElem(3,5,h*Math.sin(kx*Deltas)/kx);
    	}
    	
    	return new PhaseMap(matPhi);
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
