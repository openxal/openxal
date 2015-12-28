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
 * Represents a thick magnetic dipole magnet for a beam in a sector 
 * configuration.  Thus, there are no edge effects as the beam enters
 * the magnet orthogonally.
 * 
 * The MAD convention for sector magnets is followed
 * for coordinates, signs,  and lengths.
 * The formulation from D. Carey's Optics book, Transport manual, and
 * H. Wiedemann's books are used.
 * 
 *
 * TODO - Add "tilt" angle of the dipole, and add charge of the probe to 
 * get the right bend radius.
 *
 * @author jdg
 * @author Christopher K. Allen
 * 
 * @see "D.C. Carey, The Optics of Charged Particle Beams (Harwood, 1987)"
 * @see "H. Wiedemann, Particle Accelerator Physics I, 2nd Ed. (Springer, 1999)"   
 *     
 * @deprecated  This class has been replaced by <code>IdealMagSectorDipole2</code>
 */
@Deprecated
public class IdealMagSectorDipole extends ThickElectromagnet {



    /*
     *  Global Attributes
     */

    /** string type identifier for all IdealMagSectorDipole objects */
    public static final String s_strType = "IdealMagSectorDipole";


    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String s_strPathLength = "PathLength";  // all thick elements have length - CKA
    public static final String s_strField = "MagField";
    public static final String s_strEntranceAngle = "EntranceAngle";
    public static final String s_strExitAngle = "ExitAngle";
    public static final String s_strQuadComponent = "QuadComponent";



    /*
     *  Attributes
     */

    /** The gap height (m) */
    private double m_dblGap = 0.0;


    /** magnet field index -(R0/B0)(dB/dR) */
    private double m_dblFldInd = 0.0;

    
    //hs
    /** Path length in the magnet (m) */
    private double pathLength = 0.0;

    
    /*
     * Initialization
     */
    
    
    /** 
     * Default constructor - creates a new unitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagSectorDipole() {
	super(s_strType);
    };
    /** 
     * Default constructor - creates a new unitialized instance of 
     * IdealMagSectorDipole.      
     * This is the constructor called in automatic lattice generation.
     * Thus, all element properties are set following construction.
     */
    public IdealMagSectorDipole(String strId) {
        super(s_strType, strId);
    };
    
    /** 
     *  Creates a new instance of IdealMagSectorDipole 
     *
     *  @param  strId  identifier for this IdealMagSectorDipole object
     *  @param  dblFld    field gradient strength (in <b>Tesla</b>)
     *  @param  dblLen    pathLength of the dipole (in m)
     *  @param  enmOrient orientation
     *  @param  dblGap    full pole gap of the dipole (in m)
     *  @param  dblFldInd field index
     *
    */
    public IdealMagSectorDipole(String strId, double dblLen, 
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
        this.m_dblFldInd = dblFldInd;
    }
    
    /**
     * Set the gap size between the dipole magnet poles.
     * 
     * @param dblGap    gap size in <b>meters</b>
     */
    public void setGapHeight(double dblGap)  {
        this.m_dblGap = dblGap;
    }
    

    //hs bend angle
    private double bendAngle = 0.0;
    private double fieldPathFlag = 0.0;
    
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
        return this.m_dblFldInd;
    }

    /**
     * Return the gap size between the dipole magnet poles.
     * 
     * @return  gap size in <b>meters</b>
     */
    public double getGapHeight()  {
        return this.m_dblGap;
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
    public double   compCurvature(IProbe probe) {

        return BendingMagnet.compCurvature(probe, this.getMagField());
//        // Get  parameters
//        double B0 = this.getMagField();
////        double n0 = this.getFieldIndex();
//        
//        double e = probe.getSpeciesCharge();
//        double Er = probe.getSpeciesRestEnergy();
//        double gamma = probe.getGamma();
//        double beta  = probe.getBeta();
//
//
//        // Compute the equilibrium curvature h=1/R
//        double  h = (e*LightSpeed*B0)/(beta*gamma*Er);
//
//        return h;
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
     * Compute the partial transfer map of an ideal sector magnet for the 
     * particular probe.  Computes transfer map for a section of magnet
     * <code>dblLen</code> meters in length.
     *
     *  @param  dL      compute transfer matrix for section of this path length
     *  @param  probe   uses the rest and kinetic energy parameters from the probe
     *
     *  @return         transfer map of ideal sector magnet for particular probe
     *
     *  @exception  ModelException    unknown quadrupole orientation
     */
    @Override
    public PhaseMap transferMap(IProbe probe, double dL) 
        throws ModelException 
    {

	// Get  parameters
	double B0 = this.getMagField();
        double n0 = this.getFieldIndex();
        
	double e = probe.getSpeciesCharge();
	double Er = probe.getSpeciesRestEnergy();
        double gamma = probe.getGamma();
	double beta  = probe.getBeta();

	double c = IProbe.LightSpeed;
	double path = this.getPathLength();
	double alpha = this.getBendAngle();
//	double rhof = 0;
//	if (alpha!=0) {
//			rhof = path/alpha;
//	}
//	double Bf = 0;
//	if (rhof!=0) {
//		Bf = gamma*beta*Er/(c*rhof);
//	}
        // Compute the equilibrium radius R0 and curvature h=1/R0
        double  R0 = 0;
        if (e*c*B0 != 0) {
        	R0 = beta*gamma*Er/(e*c*B0);//h polarity = e * B0 polarity
        }
        double  h  = 0;
        if (R0!=0) {
        	h = 1.0/R0;
        }
        
    	// in ThickDipole double h = 0.2998e9 * B / (Etotal * beta *charge);
        //hs calculate hrho
 
    	double bPathFlag = this.getFieldPathFlag();
   	  
    	if (bPathFlag==1.) {
    	   	//double path = this.getPathLength();
    	    //double alpha = this.getBendAngle();
    	    double hrho = 0;
    	    if (path != 0) {
    		  hrho = alpha/path; //hrho polarity = alpha polarity
    	     }                      
    	    // h polarity = hrho polarity
             //therefore, e*B0polarity = alpha polarity   	
    	    h = hrho; //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
    	}
  
        
        // Compute the focusing constants in both the bending plane 
        double      kb;             // focusing strength in the bending plane
        double      kf;             // focusing strength in the free plane
        
        double[][]  arrBend;        // sub-transfer matrix for bending plane
        double[][]  arrFree;        // sub-transfer matrix for free plane 
    
        // Compute the transfer submatrix in the bending plane
        if ( n0 <= 1.0 ) {      // focusing or zero (n==1) in the bending direction
            kb      = Math.sqrt(1.0 - n0)*h;
            arrBend = QuadrupoleLens.transferFocPlane(kb, dL);
            
        } else {                // defocusing in the bending direction
            kb      = Math.sqrt(n0 - 1.0)*h;
            arrBend = QuadrupoleLens.transferDefPlane(kb, dL);
            
        }
                
        // Compute the transfer submatrix in the free plane
        if (n0 >= 0.0)   {      // focusing or zero (n==0) in the free plane
            kf      = Math.sqrt(n0)*h;
            arrFree = QuadrupoleLens.transferFocPlane(kf, dL);
            
        } else {                // defocusing in the free plane
            kf      = Math.sqrt(-n0)*h;
            arrFree = QuadrupoleLens.transferDefPlane(kf, dL);
            
        }


        // Compute dispersive elements
        double      dx;                         // dispersion coefficient
        double      dxp;                        // deflection coefficient
        double      dz;                         // differential path length coefficient

        if (Math.abs(kb) > ElementaryFunction.EPS)  {   // finite bending plane focusing 
            double      Cb     = arrBend[0][0];     // cosine-like function value
            double      Sb     = arrBend[0][1];     // sine-like function value
        
            dx  = R0*(1.0 - Cb)/(1.0 - n0);
            dxp = Sb*h;
	    //was default            dz  = (dL - Sb)/(1.0 - n0);
            //from ThickDipole and Trace3D, 31 Jan 07 Sako
            dz = (dL*beta*beta-Sb)/(1-n0)-dL*(1-1/(1-n0))/(gamma*gamma);
  
        } else {                                        // near zero bending plane focusing  
            double  a  = h*dL;      // arc angle of bend through dL
            double  a2 = a*a;       // arc angle squared   
            double  q  = kb*dL;     // betatron phase advance through bend 
            double  q2 = q*q;       // phase advance squared
            
            //was wrong dx  = R0*0.5*a2*( 1.0 + (q2/12.0)*(-1.0 + q2/3.0)); (3.0 should be 30)
            dx  = R0*0.5*a2*(1 - q2/12 + q2*q2/360);
            dxp = a*(1.0 + (q2/6.0)*(-1.0 + q2/20.0));//ok
            //dz  = dL*a2*(1.0/6.0 + q2*(-1.0/120 + q2/5040.0)); was default
            //dz  = dL*a2/q2*(beta*beta-1+q2/6.0 - q2*q2/120 + q2*q2*q2/5040)-dL/gamma/gamma*(1-a2/q2);
            //a2 therms cancel out (above formula diverce when q2=0)
            dz  = dL*a2*(1/6 - q2/120 + q2*q2/5040)-dL/gamma/gamma;
 
        }


        // Build the full transfer matrix 
        PhaseMatrix matPhi = PhaseMatrix.identity(); // full transfer matrix

        switch (this.getOrientation()) {

            case IElectromagnet.ORIENT_HOR:
                matPhi.setSubMatrix(0,1, 0,1, arrBend);
                matPhi.setElem(0,5, dx);
                matPhi.setElem(1,5, dxp);
                matPhi.setElem(4,0, -dxp);
                matPhi.setElem(4,1, -dx);
                matPhi.setElem(4,5, -dz);
                
                matPhi.setSubMatrix(2,3, 2,3, arrFree);            
                break;
                
            case IElectromagnet.ORIENT_VER:
                matPhi.setSubMatrix(2,3, 2,3, arrBend);
                matPhi.setElem(2,5, dx);
                matPhi.setElem(3,5, dxp);
                matPhi.setElem(4,2, -dxp);
                matPhi.setElem(4,3, -dx);
                matPhi.setElem(4,5, -dz);
                
                matPhi.setSubMatrix(0,0, 1,1, arrFree);
                break;
                
            default:
                throw new ModelException("IdealMagSectorDipole#transferMap() - bad magnet orientation.");
        }
         matPhi = applyAlignError(matPhi);
         
	return new PhaseMap(matPhi);
    }

    /**
     *  A version from ThickDipole. The results is very similar as this.transferMap
     *  Compute the partial transfer map of an ideal quadrupole for the particular probe.
     *  Computes transfer map for a section of quadrupole <code>dblLen</code> meters
     *  in length.
     *
     *  @param  dL  compute transfer matrix for section of this path length
     *  @param  probe   uses the rest and kinetic energy parameters from the probe
     *
     *  @return         transfer map of ideal quadrupole for particular probe
     *
     *  @exception  ModelException    unknown quadrupole orientation
     */
    public PhaseMap transferMapThickDipole(IProbe probe, double dL)
	throws ModelException {

	double nQ = 0.;

	// Get  parameters
	double B = this.getMagField(); // opposite

	//hs
	double path = this.getPathLength();
	double alpha = this.getBendAngle()/180.*Math.PI;

	double bPathFlag = this.getFieldPathFlag();

	double w = probe.getKineticEnergy();

	double hrho = 0;
	if (path != 0) {
//		hrho = -alpha/path;
		hrho = alpha/path;
	}
	

   double charge = probe.getSpeciesCharge();
	double Etotal = probe.getSpeciesRestEnergy() * probe.getGamma();

	
	double beta = probe.getBeta();

	final double c = IProbe.LightSpeed;
	double Beff = alpha/path*Etotal*beta/(c*charge);
	//System.out.println("id, B, Beff = "+getId()+" "+B+" "+Beff);
	
	// Compute the bending constant h  == 1/ bend radius (1/meter)

//was default	
	double h = c * B / (Etotal * beta *charge);
//	double h = -0.2998e9 * B / (Etotal * beta *charge);
	System.out.println("h, hrho = "+h + " "+ hrho);
	
	//this was for old RDB double h = 0.2998e9 * B / (Etotal * beta *Math.abs(charge)); 
	double s = probe.getPosition();

	if (bPathFlag==1.) {
	    h = hrho; //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
	}

	if( h!= 0.) {
	   //nQ = -getKQuad() / (h * h); // transform to transport notation - simpler for coding
		nQ = getFieldIndex();
	}

	double kx = Math.sqrt(1 - nQ) * h;
	//ohkawa
	if (nQ >= 1) {
	    kx = Math.sqrt(nQ - 1) * h;
	}
	double ky = Math.sqrt(Math.abs(nQ))* h;
	

	// Compute the transfer matrix components
	double[][] arrB =
	    { { Math.cos(kx * dL), Math.sin(kx * dL)/kx}, {
		-Math.sin(kx* dL) * kx, Math.cos(kx * dL)
	    }
	    };

	// Build the diople body tranfer matrix

	PhaseMatrix matBody = PhaseMatrix.identity();
	
	
	matBody.setSubMatrix(0, 1, 0, 1, arrB); // the H bend
	/* def
	matBody.setElem(0, 5, (1. - Math.cos(kx*dL)) * h/Math.pow(kx,2.) ); 
	matBody.setElem(1, 5, Math.sin(kx*dL) * h/kx ); 
	matBody.setElem(4, 0, -Math.sin(kx*dL) * h/kx ); 
	matBody.setElem(4, 1, -(1. - Math.cos(kx*dL)) * h/Math.pow(kx,2.) ); 
	matBody.setElem(4, 5, -(kx*dL - Math.sin(kx*dL)) * Math.pow((h/kx), 2.)/kx ); 
	*/
	//kx*kx=h*h*(1-n0)
	//(0,5) -> (1-cos(kx*dL))/(h(1-n0)) (1/h=R0)         dx  = R0*(1.0 - Cb)/(1.0 - n0)
	//(1,5)  -> sin(kx*dL)/sqrt(1-n0)                    dxp = Sb*h (Sb=sin(kx*dL)/kx)
	//(4,5)-> -(kx*dL*beta*beta - Math.sin(kx*dL)) * ((h/kx)*(h/kx))/kx +dL*(1. -h*h/(kx*kx))*(1. -beta*beta)
	//         term1= -(dL*beta*beta-Sb)/(1-n0)
	//          term2=dL*(1-1/(1-n0))/(gamma*gamma)
	// (4,5) -> (kx*dL*beta*beta - ElementaryFunction.sinh(kx*dL)) * Math.pow((h/kx), 2.)/kx  +dL* (1. +h*h/(kx*kx)) * (1. -beta*beta)
	//          term1= (dL*beta*beta-Sb)/(n0-1)
	//           term2=dL* (1. +1/(n0-1)) /(gamma*gamma)
	//ohkawa
	if (nQ < 1) {
	    matBody.setElem(0, 5, (1. - Math.cos(kx*dL)) * h/(kx*kx) ); 
	    matBody.setElem(1, 5, Math.sin(kx*dL) * h/kx ); 
	    matBody.setElem(4, 0, -Math.sin(kx*dL) * h/kx ); 
	    matBody.setElem(4, 1, -(1. - Math.cos(kx*dL)) * h/(kx*kx) ); 
	    matBody.setElem(4, 5, -(kx*dL*beta*beta - Math.sin(kx*dL)) * ((h/kx)*(h/kx))/kx +dL*(1. -h*h/(kx*kx))*(1. -beta*beta)); 
	} else {
	    matBody.setElem(0, 0, ElementaryFunction.cosh(kx*dL));
	    matBody.setElem(0, 1, ElementaryFunction.sinh(kx*dL)/kx);
	    matBody.setElem(1, 0, ElementaryFunction.sinh(kx*dL)*kx);
	    matBody.setElem(1, 1, ElementaryFunction.cosh(kx*dL));
	    matBody.setElem(0, 5, -(1. - ElementaryFunction.cosh(kx*dL)) * h/(kx*kx) ); 
	    matBody.setElem(1, 5, ElementaryFunction.sinh(kx*dL) * h/kx ); 
	    matBody.setElem(4, 0, -ElementaryFunction.sinh(kx*dL) * h/kx ); 
	    matBody.setElem(4, 1, (1. - ElementaryFunction.cosh(kx*dL)) * h/(kx*kx) ); 
	    matBody.setElem(4, 5, (kx*dL*beta*beta - ElementaryFunction.sinh(kx*dL)) * Math.pow((h/kx), 2.)/kx  +dL* (1. +h*h/(kx*kx)) * (1. -beta*beta));
	}


	if(nQ >= 0) { // focusing in vertical
	    matBody.setElem(2, 2, Math.cos(ky*dL) );
	    matBody.setElem(2, 3, dL * ElementaryFunction.sinc(ky*dL) ); // = l* sin(kl)/kl
	    matBody.setElem(3, 2, -ky* Math.sin(ky*dL) );
	    matBody.setElem(3, 3, Math.cos(ky*dL) );
	}
	else { // defocusing in vertical
	    matBody.setElem(2, 2, ElementaryFunction.cosh(ky*dL) );
	    matBody.setElem(2, 3, dL * ElementaryFunction.sinch(ky*dL) ); // = l* sin(kl)/kl
	    matBody.setElem(3, 2, ky* ElementaryFunction.sinh(ky*dL) );
	    matBody.setElem(3, 3, ElementaryFunction.cosh(ky*dL) );
	}
	
//	double hStar = h;
	// The entrance pole face matrix 
//	PhaseMatrix matEntrance = PhaseMatrix.identity();
//	matEntrance.setElem(1,0 , hStar* Math.tan(entranceAngle));
//	matEntrance.setElem(3,2, -hStar* Math.tan(entranceAngle - entranceAnglePhi));

	// The exit pole face matrix 
//	PhaseMatrix matExit = PhaseMatrix.identity();
//	matExit.setElem(1,0, hStar * Math.tan(exitAngle));
//	matExit.setElem(3,2, -hStar  * Math.tan(exitAngle - exitAnglePhi));

	// Multiply the 3 matrices together, starting at entrance side
//	PhaseMatrix matProd1 = matBody.times(matEntrance);
//	PhaseMatrix matProd2 = matExit.times(matProd1);


	//return new PhaseMap(matProd2);
	return new PhaseMap(matBody);

    }


    /*
     * Internal Support
     */
    


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
