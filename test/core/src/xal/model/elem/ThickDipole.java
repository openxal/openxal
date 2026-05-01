/*
 * Quadrupole.java
 *
 * Created on Dec. 19 , 2003
 */

package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.ElementaryFunction;

/**
 * Represents a thick magnetic dipole magnet for a beam 
 * transport/accelerator system.
 * 
 * NOTE:
 *  - !!! Bending is assumed to be horizontal for now !!!!
 * 
 * It has provisions for a general wedge magnet, with arbitrary 
 * entrance / exit angles. The MAD convention for sector magnets is followed
 * for coordinates, signs,  and lengths.
 * The formulation from D. Carey's Optics book + Transport manual are used.
 *
 * TODO - Add "tilt" angle of the dipole, and add charge of the probe to 
 * get the right bend radius.
 *
 * @author  jdg
 */
public class ThickDipole extends ThickElectromagnet {

    /*
     *  Global Attributes
     */

    /** string type identifier for all ThickDipole objects */
    public static final String s_strType = "ThickDipole";

    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String s_strPathLength = "PathLength";  // all thick elements have length - CKA
    public static final String s_strField = "MagField";
    public static final String s_strEntranceAngle = "EntranceAngle";
    public static final String s_strExitAngle = "ExitAngle";
    public static final String s_strQuadComponent = "QuadComponent";

    /*
     *  Attributes
     */

    /** Path length in the magnet (m) */
    private double pathLength = 0.0;

    /** Entrance angle (rad) */
    private double entranceAngle = 0.0;

    /** Exit angle (rad) */
    private double exitAngle = 0.0;

    /** The gap height (m) */
    private double gapHeight = 0.;

    /** The dimensionless integral term for the fringe field focusing
     * Should be = 1/6 for linear drop off, ~ 0.4 for clamped Rogowski coil
     * or 0.7 for an unclamped Rogowski coil.
     */
    private double fringeIntegral = 0.;


    /** The quadrupole term = 1/(B-rho) * dB_y/dx */
    private double k1 = 0.;

    //hs alignment
    private double alignx = 0.0;
    private double aligny = 0.0;
    private double alignz = 0.0;

    @Override
    public void setAlignX(double x) {
        alignx = x;
    }
    @Override
    public void setAlignY(double y) {
        aligny = y;
    }
    @Override
    public void setAlignZ(double z) {
        alignz = z;
    }
    public double getALignX() {
        return alignx;
    }
    @Override
    public double getAlignY() {
        return aligny;
    }
    @Override
    public double getAlignZ() {
        return alignz;
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
     * Initialization
     */


    /** 
     *  Creates a new instance of ThickDipole 
     *
     *  @param  strId  identifier for this ThickDipole object
     *  @param  fld    field gradient strength (in <b>Tesla</b>)
     *  @param  len    pathLength of the dipole (in m)
     *  @param  entAng entrance angle of the dipole (in rad)
     *  @param  exitAng exit angle of the dipole (in rad)
     *  @param  gap    full pole gap of the dipole (in m)
     *  @param  fInt   The dimensionless integral term for the extended fringe field focsing, Should be = 1/6 for linear drop off, ~ 0.4 for clamped Rogowski coil, or 0.7 for an unclamped Rogowski coil. (dimensionless)
     *
     */
    public ThickDipole(
            String strId, double fld, double len, double entAng, double exitAng, double gap, double fInt) {
        super(s_strType, strId, len);
        this.setMagField(fld);
        entranceAngle = entAng;
        exitAngle = exitAng;
        gapHeight = gap;
        fringeIntegral = fInt;
    }

    /** 
     *  JavaBean constructor - creates a new unitialized instance of ThickDipole     * 
     * This is the constructor called in automatic lattice generation.  Thus, all
     * element properties are set following construction.
     *
     *  <b>BE CAREFUL</b>
     */
    public ThickDipole() {
        super(s_strType);
    }

    /**
     * Sets the entrance angle of the beam into the dipole.
     * 
     * @param   dblAng      entrance angle in <b>radians</b>
     *
     * @author  Christopher K. Allen 
     */
    public void setEntranceAngle(double dblAng) { this.entranceAngle = dblAng; }
    
    /**
     * Gets the entrance angle of the beam into the dipole.
     * @author  J. Galambos 
     */
    public double getEntranceAngle() { return entranceAngle; }

    /**
     * Sets the entrance angle of the beam into the dipole.
     * @param   dblAng      exit angle in <b>radians</b>
     * @author  J. Galambos 
     */
    public void setExitAngle(double dblAng) { this.exitAngle = dblAng; }

    /**
     * Gets the exit angle of the beam into the dipole.
     * @author  J. Galambos 
     */
    public double getExitAngle() {  
        return exitAngle; }

    /**
     * Sets the quad. field index term
     * @param   k      = 1/B-rho) * d B_y/dx
     */
    public void setKQuad(double k) { this.k1 = k; }

    /**
     * Gets the quad. field index term = 1/B-rho * d B_y/dx
     */
    public double getKQuad() { return k1; }

    /*
     *  ThickElement Abstract Functions
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
    }


    /** set the fring field integral factor 
     * @param fint the field integral a la MAD
     * = 1/6 for linear drop off
     * = 0.4 for clamped Rogowski coil
     * = 0.7 for unclamped Rogowski coil
     * = 0.45 for square edge - non saturating magnet
     */
    public void setFieldIntegral(double fint) { fringeIntegral = fint;}

    /** set the gap height 
     * @param gap = full gap height (m)
     */
    public void setGapHeight(double gap) { gapHeight = gap;}

    /** get field index nQ
     * 
     */
    public double getFieldIndex(IProbe probe) {
        // Get  parameters
        double B = this.getMagField(); // opposite


        //hs
        double path = this.getPathLength();
        double alpha = this.getBendAngle()/180.*Math.PI;

        double bPathFlag = this.getFieldPathFlag();

//        double w = probe.getKineticEnergy();

        double rho = -1;
        if (alpha!=0) {
            rho = Math.abs(path/alpha);
        }

        double hrho = 0;
        if (rho!=0) {
            if (alpha<0) {//sign of alpha = sign of h
                hrho = -1./rho;
            } else {
                hrho = 1./rho;
            }
        }

        double charge = probe.getSpeciesCharge();
        double Etotal = probe.getSpeciesRestEnergy() * probe.getGamma();

        double beta = probe.getBeta();

        double h = 0.2998e9 * B / (Etotal * beta *charge);

        //this was for old RDB double h = 0.2998e9 * B / (Etotal * beta *Math.abs(charge)); 
        //hs 
        System.out.println("h, hrho = "+h+" "+hrho);
//        double s = probe.getPosition();

        if (bPathFlag==1.) {
            h = hrho; //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
        }
        double n = 0.;
        if( h!= 0.) {
            n = -getKQuad() / (h * h); // transform to transport notation - simpler for coding
        }


        return n;
    }
    /**
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
    @Override
    public PhaseMap transferMap(IProbe probe, double dL)
    throws ModelException {
        //hs
//        String envTrackerId = "EnvelopeTracker";
//        String algType = probe.getAlgorithm().getType();

        double nQ = 0.;

        // Get  parameters
        double B = this.getMagField(); // opposite

        //hs
        double path = this.getPathLength();
        double alpha = this.getBendAngle()/180.*Math.PI;

        double bPathFlag = this.getFieldPathFlag();

//        double w = probe.getKineticEnergy();

        /*
	double rho = -1;
	if (alpha!=0) {
	    rho = Math.abs(path/alpha);
	}

	System.out.println("id alpha, path, rho = "+getId()+" "+alpha+" "+path+" "+rho);

	double hrho = 0;
	if (rho!=0) {
	    if (alpha<0) {//sign of alpha = sign of h
	    	hrho = -1./rho;
	    } else {
	    	hrho = 1./rho;
	    }
	}
         */
        double hrho = 0;
        if (path != 0) {
            //		hrho = -alpha/path;
            hrho = alpha/path;
        }

        //	if (algType.equals(envTrackerId)) {
        //	}

        //	double charge = probe.getSpeciesCharge()/IConstants.UnitCharge;
        double charge = probe.getSpeciesCharge();
        double Etotal = probe.getSpeciesRestEnergy() * probe.getGamma();


        double beta = probe.getBeta();

//        double Beff = alpha/path*Etotal*beta/(0.2998e9*charge);
        //System.out.println("id, B, Beff = "+getId()+" "+B+" "+Beff);

        // Compute the bending constant h  == 1/ bend radius (1/meter)

        //was default	
        double h = 0.2998e9 * B / (Etotal * beta *charge);
        //	double h = -0.2998e9 * B / (Etotal * beta *charge);
        System.out.println("h, hrho = "+h + " "+ hrho);

        //this was for old RDB double h = 0.2998e9 * B / (Etotal * beta *Math.abs(charge)); 
//        double s = probe.getPosition();

        if (bPathFlag==1.) {
            h = hrho; //if fieldPathFlag=1, use hrho (calculated from rho) instead of h(calculated from p and B)
        }

        if( h!= 0.) {
            nQ = -getKQuad() / (h * h); // transform to transport notation - simpler for coding
        }

        double kx = Math.sqrt(1 - nQ) * h;
        //ohkawa
        if (nQ >= 1) {
            kx = Math.sqrt(nQ - 1) * h;
        }
        double ky = Math.sqrt(Math.abs(nQ))* h;

        //System.out.print("name= " + probe.getCurrentElement() + " h = " + new Double(h));
        //System.out.println(" nQ = " + new Double(nQ));

        // The fringe field angle from the extended field:
        double entranceAnglePhi = gapHeight * h * (1. + Math.pow(Math.sin(entranceAngle),2.) )/Math.cos(entranceAngle) * fringeIntegral;

        double exitAnglePhi = gapHeight * h * (1. + Math.pow(Math.sin(exitAngle),2.) )/Math.cos(exitAngle) * fringeIntegral;

        // Compute the transfer matrix components
        double[][] arrB = {
                { Math.cos(kx * dL), Math.sin(kx * dL)/kx },
                { -Math.sin(kx* dL) * kx, Math.cos(kx * dL) },
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

        //double hStar = h *charge/Math.abs(charge);
        double hStar = h;
        // The entrance pole face matrix 
        PhaseMatrix matEntrance = PhaseMatrix.identity();
        matEntrance.setElem(1,0 , hStar* Math.tan(entranceAngle));
        matEntrance.setElem(3,2, -hStar* Math.tan(entranceAngle - entranceAnglePhi));

        // The exit pole face matrix 
        PhaseMatrix matExit = PhaseMatrix.identity();
        matExit.setElem(1,0, hStar * Math.tan(exitAngle));
        matExit.setElem(3,2, -hStar  * Math.tan(exitAngle - exitAnglePhi));

        // Multiply the 3 matrices together, starting at entrance side
        PhaseMatrix matProd1 = matBody.times(matEntrance);
        PhaseMatrix matProd2 = matExit.times(matProd1);
        //System.out.println(getId() + "  len = " + new Double(dL) + "  k = " + new Double(k) + "  field = " + new Double(getMagField()) );

        //hs add alignment errors...

        return new PhaseMap(matProd2);
        //return new PhaseMap(matBody);

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
    }

}
