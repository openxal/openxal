/**
 *
 * IdealEDipole.java adapted from ThickDipole.java
 * C.Benatti 11/11/2011
 *
 * ThickDipole.java
 * Created on Dec. 19, 2003 by John Galambos
 * Modified on Dec. 20, 2005 by Jeff Holmes
 *
 */

/*
 * Modified:
 *      07/18/08 MDW    - switch to MAD's longitudinal coordinates
 */

package xal.model.elem;

import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.sim.scenario.LatticeElement;
import xal.smf.impl.EDipole;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.BendingMagnet;


/**
 *
 * Represents a thick electric dipole magnet for a beam
 * transport/accelerator system.
 *
 * NOTE:
 *  - !!! Bending is assumed to be horizontal for now !!!!
 *
 * There are provisions for a general wedge magnet, with arbitrary
 * entrance/exit angles. The MAD convention for sector magnets is followed
 * for coordinates, signs, and lengths.
 * The formulation from D. Carey's Optics book + Transport manual are used.
 *
 * TODO - Add "tilt" angle of the dipole.
 *
 * @author  jdg
 * @author  Carla Benatti 
 *
 */


public class IdealEDipole extends ThickElectrostatic
{

  /**
   *
   * string type identifier for all ThickDipole objects
   *
   */

  public static final String s_strType = "IdealEDipole";

  /**
   *
   * Parameters for XAL MODEL LATTICE dtd
   *
   */

  public static final String s_strPathLength = "PathLength";
    /**
     *
     * all thick elements have length - CKA.
     *
     */

  public static final String s_strParamVoltage = "Voltage";
  public static final String s_strEntranceAngle = "EntranceAngle";
  public static final String s_strExitAngle = "ExitAngle";
  public static final String s_strQuadComponent = "QuadComponent";

  /** Applied Voltage */
  private double m_dblVoltage = 0.0;

  /**
   *
   * Path length in the magnet (m)
   *
   */

  private double pathLength = 0.0;

  /**
   *
   * the reference bend angle (rad)
   *
   */

  private double referenceBendAngle = 0.0;

  /**
   *
   * Entrance angle (rad)
   *
   */

  private double entranceAngle = 0.0;

  /**
   *
   * Exit angle (rad)
   *
   */

  private double exitAngle = 0.0;

  /**
   *
   * The gap height (m)
   *
   */

  private double gapHeight = 0.;

  /**
   *
   * The dimensionless integral term for the fringe field focusing:
   * Should be:
   *   = 1/6 for linear drop off,
   *   =~0.4 for clamped Rogowski coil,
   *   = 0.7 for an unclamped Rogowski coil, or
   *   = 0.45 for square edge - non saturating magnet.
   *
   */

  private double fringeIntegral = 0.;

  /**
   *
   * Orientation of dipole (for now hardwired to horizontal).
   * This interface needs a general angle.
   *
   */

  private int m_enmOrient = ORIENT_HOR;
    
  /**
   *
   * The quadrupole term = 1/(B-rho) * dB_y/dx
   *
   */

  private double k1 = 0.;

  /**
   * dipole indicator, 0 = first half of a split dipole, 1 = means second half of a split dipole, 2 = others
   */
  private int dipoleInd = 2;
  
  /**
   *
   *  Creates a new instance of IdealEDipole.
   *
   *  @param  strId  identifier for this IdealEDipole object
   *  @param  m_dblVoltage    voltage applied to pole tip (in <b>kV</b>)
   *  @param  len    pathLength of the dipole (in m)
   *  @param  entAng entrance angle of the dipole (in rad)
   *  @param  exitAng exit angle of the dipole (in rad)
   *  @param  gap    full pole gap of the dipole (in m)
   *  @param  fInt   The dimensionless integral term for
   *                 the extended fringe field focusing (dimensionless).
   *                 Should be:
   *                   = 1/6 for linear drop off,
   *                   =~0.4 for clamped Rogowski coil,
   *                   = 0.7 for an unclamped Rogowski coil, or
   *                   = 0.45 for square edge - non saturating magnet.
   *
   */

  public IdealEDipole(String strId, double m_dblVoltage, double len, double entAng, double exitAng, double gap, double fInt)
  {
    super( s_strType, strId, len );
    this.setVoltage(m_dblVoltage);
    entranceAngle = entAng;
    exitAngle = exitAng;
    gapHeight = gap;
    fringeIntegral = fInt;
  }

  /**
   * 
   *  JavaBean constructor - creates a new uninitialized instance
   *                         of ThickDipole.
   *  This is the constructor called in automatic lattice generation.
   *  Thus, all element properties are set following construction.
   *
   *  <b>BE CAREFUL</b>
   *
   */

  public IdealEDipole()
  {
    super( s_strType );
  }

  /**
   *
   * Sets the dipole entrance angle.
   *
   * @param   dblAng      entrance angle in <b>radians</b>
   * @author  Christopher K. Allen
   *
   */

  public void setEntranceAngle(double dblAng)
  {
    this.entranceAngle = dblAng;
  }

  /**
   *
   * Returns the dipole entrance angle.
   *
   * @author  J. Galambos
   *
   */

  public double getEntranceAngle()
  {
    return entranceAngle;
  }


  /**
   *
   * Sets the dipole exit angle.
   *
   * @param   dblAng      exit angle in <b>radians</b>
   * @author  J. Galambos
   *
   */

  public void setExitAngle(double dblAng)
  {
    this.exitAngle = dblAng;
  }

  /**
   *
   * Returns the dipole exit angle.
   *
   * @author  J. Galambos
   *
   */

  public double getExitAngle()
  {  
    return exitAngle; 
  }


  /**
   *
   * Sets the quadrupole field index.
   *
   * @param   k      = 1/B-rho * d B_y/dx
   *
   */

  public void setKQuad(double k)
  {
    this.k1 = k;
  }

  /**
   *
   * Returns the quadrupole field index.
   *
   * @return    k1     = 1/B-rho * d B_y/dx
   *
   */

  public double getKQuad()
  {
    return k1;
  }
  
  /**
   * convenient method to return fringe field integral
   * @return fringe field integral
   */
  public double getFieldIntegral() {
	  return fringeIntegral;
  }


  /**
   *
   * Sets the reference bend angle (rad).
   *
   */

  public void setDesignBendAngle(double dblAng)
  {
    this.referenceBendAngle = dblAng;
  }

  /**
   *
   * Returns the reference bend angle (rad).
   *
   */

  public double getDesignBendAngle()
  {
    return referenceBendAngle;
  }



  /**
   *
   *  IElectromagnet Interface
   *
   */


  /**
   *
   *  Sets the magnet orientation.
   *
   *  @param  enmOrient   magnet orientation enumeration code
   *  @see    #getOrientation
   *
   */

  public void setOrientation(int enmOrient)
  {
    m_enmOrient = enmOrient;
  }
  
  /**
   * Set dipole indicator so the transfer map can handle edge effect correctly.
   * @param ind 0 = first half of a dipole, 1 = second half and 2 = no split
   */
  public void setDipoleInd(int ind) {
	  dipoleInd = ind;
  }

  /**
   *
   *  Returns the magnet orientation.
   *
   *  @return
   *     ORIENT_HOR  - dipole has steering action in x (horizontal) plane
   *     ORIENT_VER  - dipole has steering action in y (vertical) plane
   *     ORIENT_NONE - error
   *
   */

  public int getOrientation()
  {
    return m_enmOrient;
  }


  /**
   *
   *  Sets the magnetic field strength.
   *
   *  @param  voltage    pole tip voltage (in <b>kV</b>).
   *
   */

  public void setVoltage(double voltage)
  {
    m_dblVoltage = voltage;
  }

  /**
   *
   *  Returns the applied voltage.
   *
   *  @return     voltage at the pole tips (in <b>kV</b>).
   *
   */

  public double getVoltage()
  {
    return m_dblVoltage;
  }


  /**
   *
   *  ThickElement Abstract Functions
   *
   */


  /**
   *
   * Returns the time taken for the probe to drift through part of the
   * element.
   *
   *  @param  probe   propagating probe
   *  @param  dblLen  subsection propagation length <b>meters</b>
   *
   *  @return         elapsed propagaton time<b>Units: seconds</b>
   *
   */

  public double elapsedTime(IProbe probe, double dblLen)
  {
    return super.compDriftingTime(probe, dblLen);
  }


  /**
   *
   *  Returns the energy gain imparted to a particular probe.
   *  For an ideal dipole magnet this value is always zero.
   *
   *  @param  dblLen  dummy argument
   *  @param  probe   dummy argument
   *
   *  @return         returns a zero value
   *
   */

  public double energyGain(IProbe probe, double dblLen)
  {
    return 0.0;
  }


  /**
   *
   * Sets the fringe field integral factor.
   *
   * @param fint the field integral a la MAD
   *   = 1/6 for linear drop off
   *   = 0.4 for clamped Rogowski coil
   *   = 0.7 for unclamped Rogowski coil
   *   = 0.45 for square edge - non saturating magnet
   *
   */

  public void setFieldIntegral(double fint)
  {
    fringeIntegral = fint;
  }


  /**
   *
   * Sets the gap height
   *
   * @param gap = full gap height (m)
   *
   */

  public void setGapHeight(double gap)
  {
    gapHeight = gap;
  }


  /**
   *
   *  Computes the partial transfer map of an ideal dipole
   *     for the particular probe.
   *  Computes transfer map for a section.
   *     of dipole <code>dblLen</code> meters in length.
   *
   *  @param  probe   uses the rest and kinetic energy
   *                  parameters from the probe.
   *  @param  dL  compute transfer matrix for section of this path length.
   *
   *  @return         transfer map of ideal dipole for particular probe.
   *  @throws  ModelException    unknown quadrupole orientation.
   *
   */

  public PhaseMap transferMap(final IProbe probe, final double dL) throws ModelException
  {

    // Some constants (for series approximations)
    
    final double c1 = 1.0;
    final double c2 = 1.0 / 2.0;
    final double c3 = 1.0 / 24.0;
    final double c4 = 1.0 / 720.0;
    final double s1 = 1.0;
    final double s2 = 1.0 / 6.0;
    final double s3 = 1.0 / 120.0;
    final double s4 = 1.0 / 5040.0;
    final double cg0 = 1.0 / 20.0;
    final double cg1 = 5.0 / 840.0;
    final double cg2 = 21.0 / 60480.0;
    final double ch0 = 1.0 / 56.0;
    final double ch1 = 14.0 / 4032.0;
    final double ch2 = 147.0 / 443520.0;

    // Magnet parameters

    double angle = getDesignBendAngle() * dL / this.getLength();
    System.out.println("Element " + this.getId() + " theta " + angle);
    //angle = -angle;
    
    double kQuad = getKQuad();
    double entAng = entranceAngle;
    double exAng = exitAngle;
    double V = this.getVoltage();

    // Relativistic parameters

    double beta = probe.getBeta();
    double gamma = probe.getGamma();
    double bi = 1.0 / beta;
    double bi2 = bi * bi;
    double gi = 1.0 / gamma;
    double gi2 = gi * gi;
    double bi2gi2 = bi2 * gi2 ;

    // Compute the bending constant h == 1 / bend radius (1/meter);
    // if h and hProbe have opposite signs, the bend is defined for
    // the wrong charge sign (this can happen at injection, due to
    // stripping) ... the following redefines the bend parameters for
    // this case, assuming an entrance angle of zero

    double h = angle / dL;
    
    //Not sure about curvature here with V instead of B
    double hProbe = BendingMagnet.compCurvature(probe, V);

    if(h * hProbe < 0.0)
    {
      angle = -angle;
      h = -h;
     kQuad = -kQuad;
      entAng = 0.0;
      exAng  = exAng + 2.0 * angle;
    }

    //  Compute body horizontal and longitudinal matrix elements
    
    double xksq = h * h + kQuad;
    double xk = Math.sqrt(Math.abs(xksq));
    double xkl = xk * dL;
    double xklsq = xksq * dL * dL;
   
    double cx = 0.0;
    double sx = 0.0;
    double dx = 0.0;
    double fx = 0.0;
    double gx = 0.0;
    double hx = 0.0;

    if(Math.abs(xklsq) < 0.01)
    {
      cx = (c1 - xklsq * (c2 - xklsq * c3));
      sx = (s1 - xklsq * (s2 - xklsq * s3)) * dL;
      dx = (c2 - xklsq * (c3 - xklsq * c4)) * dL * dL;
      fx = (s2 - xklsq * (s3 - xklsq * s4)) * Math.pow(dL , 3);
      gx = (cg0 - xklsq * (cg1 - xklsq * cg2)) * Math.pow(dL , 5);
      hx = (ch0 - xklsq * (ch1 - xklsq * ch2)) * Math.pow(dL , 7);
    }
    else
    {
      if(xklsq > 0.0)
      {
        cx = Math.cos(xkl);
        sx = Math.sin(xkl) / xk;
      }
      else
      {
        cx = Math.cosh(xkl);
        sx = Math.sinh(xkl) / xk;
      }
      dx = (1.0 - cx) / xksq;
      fx = (dL  - sx) / xksq;
      gx = (3.0 * dL - sx * (4.0 - cx)) / (2.0 * xksq *xksq);
      hx = (15.0 * dL - sx * (22.0 - 9.0 * cx + 2.0 * cx * cx)) / (6.0 * Math.pow(xksq , 3));
    }

    double M00 = cx;
    double M01 = sx;
    double M05 = h * dx * bi;
    double M10 = -xksq * sx;
    double M11 = cx;
    double M15 = h * sx * bi;
    double M40 = -M15;
    double M41 = -M05;
    double M45 = dL * bi2gi2 - h * h * fx * bi2;

//  Body Vertical
    
    double yksq = -kQuad;
    double yk = Math.sqrt(Math.abs(yksq));
    double ykl = yk * dL;
    double yklsq = yksq * dL * dL;
   
    double cy = 0.0;
    double sy = 0.0;

    if(Math.abs(yklsq) < 0.01)
    {
      cy = (c1 - yklsq * (c2 - yklsq * c3));
      sy = (s1 - yklsq * (s2 - yklsq * s3)) * dL;
    }
    else if (yklsq > 0.0)
    {
      cy = Math.cos(ykl);
      sy = Math.sin(ykl) / yk;
    }
    else
    {
      cy = Math.cosh(ykl);
      sy = Math.sinh(ykl) / yk;
    }

    double M22 = cy;
    double M23 = sy;
    double M32 = -yksq * sy;
    double M33 = cy;

    // The fringe field angle from the extended field

    double sEn = Math.sin(entAng);
    double cEn = Math.cos(entAng);
    double entranceAnglePhi = gapHeight * h * fringeIntegral * (1.0 + sEn * sEn) / cEn;

    double sEx = Math.sin(exAng);
    double cEx = Math.cos(exAng);
    double exitAnglePhi = gapHeight * h * fringeIntegral * (1.0 + sEx * sEx) / cEx;

    // this is related to the angle error (I think)

    double rho = 1.0e12;
    if (h != 0.0)
      rho = 1.0 / h;
    
    double rhoProbe = 1.0e12;
    if (hProbe != 0.0)
      rhoProbe = 1.0 / hProbe;
    
    double zprimeProbe = (rhoProbe - rho) / (rhoProbe * gamma * gamma);

    double M06 = M05 * zprimeProbe;
    double M16 = M15 * zprimeProbe;
    double M46 = M45 * zprimeProbe;

    // The following three lines need to be better understood

//  M05 *= rhoProbe / rho;
//  M15 *= rhoProbe / rho;
//  M45 *= rhoProbe / rho;

    // Assemble the body and edge focusing matrices
    
    PhaseMatrix matBody = PhaseMatrix.identity();
    PhaseMatrix matEntrance = PhaseMatrix.identity();
    PhaseMatrix matExit = PhaseMatrix.identity();

        
    matBody.setElem(0, 0, M00);
    matBody.setElem(0, 1, M01);
    matBody.setElem(0, 5, M05);
    matBody.setElem(1, 0, M10);
    matBody.setElem(1, 1, M11);
    matBody.setElem(1, 5, M15);
    matBody.setElem(2, 2, M22);
    matBody.setElem(2, 3, M23);
    matBody.setElem(3, 2, M32);
    matBody.setElem(3, 3, M33);
    matBody.setElem(4, 0, M40);
    matBody.setElem(4, 1, M41);
    matBody.setElem(4, 5, M45);

    //C.Benatti--not sure about these matrix elements
    //set to zero--error in angle here?? introduces offset in x,x'
    matBody.setElem(0, 6, 0);//M06
    matBody.setElem(1, 6, 0);//M16
    matBody.setElem(4, 6, 0);//M46

    
	
    
    
    matEntrance.setElem(1, 0, h * Math.tan(entAng));
    matEntrance.setElem(3, 2, -h * Math.tan(entAng - entranceAnglePhi));

    matExit.setElem(1, 0, h * Math.tan(exAng));
    matExit.setElem(3, 2, -h * Math.tan(exAng - exitAnglePhi));
    
    // Multiply the 3 matrices together, starting at entrance side;
    // deal with "split bend disease" here

    PhaseMatrix matProd1;
    PhaseMatrix matProd2;
    
    switch(dipoleInd)
    {
    case 0:
      // if this is the first half of a split dipole
      matProd2 = matBody.times(matEntrance);
      break;
    case 1:
      // if this is the second half of a split dipole
      matProd2 = matExit.times(matBody);
      break;
    case 2:	
      // if this is a normal dipole with both edges
      matProd1 = matBody.times(matEntrance);
      matProd2 = matExit.times(matProd1);
      break;
    default:
      // default is normal dipole
      matProd1 = matBody.times(matEntrance);
      matProd2 = matExit.times(matProd1);
      break;
    }

    // rotate the 4x4 matrix by 90 deg if it is a vertical bend

    if (getOrientation() == xal.smf.impl.Magnet.VERTICAL)
    {
      // prepare a rotation matrix
        
      PhaseMatrix rotMat = PhaseMatrix.identity();
      rotMat.setElem(0, 0, 0.);
      rotMat.setElem(1, 1, 0.);
      rotMat.setElem(2, 2, 0.);
      rotMat.setElem(3, 3, 0.);
      rotMat.setElem(0, 2, -1.);
      rotMat.setElem(1, 3, -1.);
      rotMat.setElem(2, 0, 1.);
      rotMat.setElem(3, 1, 1.);

      // form the transpose matrix

      PhaseMatrix rotMatTrans = rotMat.transpose();

      // rotate

      matProd2 = matProd2.times(rotMatTrans);
      matProd2 = rotMat.times(matProd2);
      
      //System.out.println("Element " + this.getId() + " Matrix " + matProd2);
    }

/*    System.out.println(this.getId() + ": dL = " + dL
     + ", angle = " + angle
     + ", kQuad = " + kQuad
     + ", B = " + B
     + "\n\t, entAng = " + entAng
     + ", exAng = " + exAng
     + ", gapHeight = " + gapHeight
     + ", fringeIntegral = " + fringeIntegral
     + "\n\t, beta = " + beta
     + ", gamma = " + gamma
     + "\n\t, h = " + h
     + ", hProbe = " + hProbe
     + ", rho = " + rho
     + ", rhoProbe = " + rhoProbe
     + ", zprimeProbe = " + zprimeProbe
     + "\n\t, entrAnglePhi = " + entranceAnglePhi
     + ", exitAnglePhi = " + exitAnglePhi
     + ", dipoleInd = " + dipoleInd
     + "\n\t, M00 = " + M00
     + ", M01 = " + M01
     + ", M05 = " + M05
     + "\n\t, M10 = " + M10
     + ", M11 = " + M11
     + ", M15 = " + M15
     + "\n\t, M22 = " + M22
     + ", M23 = " + M23
     + ", M32 = " + M32
     + ", M33 = " + M33
     + "\n\t, M40 = " + M40
     + ", M41 = " + M41
     + ", M45 = " + M45
     + "\n\t, M06 = " + M06
     + ", M16 = " + M16
     + ", M46 = " + M46
    );
*/    
    return new PhaseMap( matProd2 );		
  }


  /**
   *  Dump current state and content to output stream.
   *
   *  @param  os      output stream object
   */

  public void print(PrintWriter os)
  {
    super.print(os);

    os.println("  applied voltage    : " + this.getVoltage());
    os.println("  magnet orientation : " + this.getOrientation());
  }

  @Override
  public void initializeFrom(LatticeElement element) {
	super.initializeFrom(element);				
	 
	EDipole edp = (EDipole) element.getHardwareNode();
	double len_sect = element.getLength();		
	double len_path0 = edp.getDfltPathLength();
	double ang_bend0 = edp.getDfltBendAngle() * Math.PI / 180.0;

	double R_bend0 = len_path0 / ang_bend0;
	double ang_bend = ang_bend0 * (len_sect / len_path0);
	double len_path = R_bend0 * ang_bend;
	
	setPathLength(len_path);		
	setDesignBendAngle(ang_bend);
	
  }
  
  public void setPathLength(double dblPathLen)  {
	  pathLength = dblPathLen;
  }
  
  public double getPathLength() {
	  return pathLength;
  }
};
