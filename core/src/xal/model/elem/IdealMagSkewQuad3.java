/*
 * IdealMagSkewQuad3.java
 * 
 * Created on March 13, 2007
 *
 */
package xal.model.elem;

import java.io.PrintWriter;

import xal.sim.scenario.LatticeElement;
import xal.smf.impl.Magnet;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.DriftSpace;
import xal.tools.beam.optics.QuadrupoleLens;
import xal.tools.math.r3.R3x3;
import xal.model.IProbe;
import xal.model.elem.sync.IElectromagnet;


/**
 * <p>
 * Represents a "skewed" ideal quadrupole magnet.  Such a device is a quadrupole
 * magnet which is rolled about the beam axis by 45&deg;. It is a corrective device
 * which is meant to be driven with a bipolar supply.  Thus, the sign of the 
 * magnetic field parameter (see {@link IdealMagSkewQuad3#setMagField(double)})
 * can be either positive or negative
 * </p>
 * <p>
 * Looking down the beam axis toward a "skew quadrupole", a positive 
 * "skew" angle means the element is rotated <em>clockwise</em> by 45&deg;.
 * (This convention is consistent with the Tait-Bryan angles of aerospace 
 * engineering.)  
 * </p>
 * <p>
 * Note that the action of a rotated element
 * on phase space is also a rotation of the beam coordinates,
 * but in 
 * the <b>opposing</b> direction.  Thus, if this element is to be represented
 * simply as a rotated <code>IdealMagQuad</code> element, then
 * the coordinate rotation angles should negative that
 * of the elements rotation angles. 
 * </p>  
 * <p>
 * With respect to the <code>IdealMagQuad</code> class, this class has one
 * less parameter, the <code>setOrientation()</code> parameter.  
 * This condition results from the fact that skew quadrupole hardware
 * objects can be driven in either direction (i.e., they have bipolar
 * power supplies).  Thus, the skew angle for any <code>IdealMagSkewQuad3</code>
 * object is always the same.  Driving the device with a negative supply
 * (see {@link IdealMagSkewQuad3#setMagField(double)}) in effect changes
 * its "orientation."
 * </p>
 * 
 * <table cellpadding="10">
 * <tr>
 *   <td>
 *   Specifically,
 *   <br>
 *   <br>- A (+45&deg;) skew quadrupole driven with a positive field focuses
 *   in the 2<sup>nd</sup> and 4<sup>th</sup> quadrants.
 *   </td>
 * </tr>
 * <tr>
 *   <td>
 *   - A (+45&deg;) skew quadrupole driven with a negative field focuses
 *   in the 1<sup>st</sup> and 3<sup>rd</sup> quadrants.
 *   </td>
 * </tr>
 * </table>
 * 
 * <p>
 * NOTES:
 * <br>
 * </p>
 * 
 * @author Christopher K. Allen
 * @since  Mar 13, 2008
 * 
 * @see xal.model.elem#IdealMagQuad
 * @see xal.model.elem#CoordinateRotation
 *
 */
public class IdealMagSkewQuad3 extends ThickElectromagnet {



    /*
     *  Global Constants
     */

    
    //
    // String Identifiers
    //
    
    /** string type identifier for all IdealMagSectorDipole objects */
    public static final String  STR_TYPE_ID = "IdealMagSkewQuad3";

    
    
    
    // 
    // Class Parameters
    //
    
    /** The roll angle defining the skew (in radians) */
    public static final double  DBL_SKEW_ANGLE = Math.PI/4.0;
    
    /** orientation of the internal <code>IdealMagQuad</code> object */
    public static final int     INT_ORIENT = IElectromagnet.ORIENT_HOR;
    
    
    

    /*
     *  Global Attributes
     */

    
    /** Phase coordinate rotation in SO(6) taking beam coordinates to natural quadrupole coordinates */
    private static PhaseMatrix  MAT_SO6_TOQUAD_COORDS;
    
    /** Phase coordinate rotation in SO(6) natural quadrupole coordinates to beam coordinates */
    private static PhaseMatrix  MAT_SO6_TOBEAM_COORDS;
    

    
    
    /** 
     * <p>
     * Compute the phase coordinate transforms back and forth between natural
     * coordinates of magnet and the beam coordinates.
     * </p>
     * <p>
     * Note that global attributes <code>MAT_SO6_2QUAD</code> and 
     * <code>MAT_SO6_2BEAM</code> would more naturally be constants.
     * However, because of the tools for working with matrices in 
     * <i>SO</i>(6) &subst; <i>SO</i>(7) it is easy to compute these
     * objects on start up from the constant <code>DBL_SKEW_ANGLE</code>.
     * </p>
     */
    static {
        
        R3x3  matSO3 = R3x3.newRotationZ( - DBL_SKEW_ANGLE);
        
        MAT_SO6_TOQUAD_COORDS = PhaseMatrix.rotationProduct( matSO3 );
        MAT_SO6_TOBEAM_COORDS = MAT_SO6_TOQUAD_COORDS.transpose();
    }
    

    
    /**
     * <p>
     * toBeamFrame
     * </p>
     * <p>
     * Converts a linear transform (represented as a <code>PhaseMatrix</code> object)
     * from its representation in the magnet coordinates to its representation in 
     * the beam frame phase coordinates.
     * </p> 
     *
     * @param matQuad   linear transform in natural quadrupole magnet representation
     * 
     * @return          linear transform in beam frame phase coordinates representation
     * 
     * @since  Mar 13, 2008
     * @author Christopher K. Allen
     */
    public static synchronized PhaseMatrix toBeamFrame(PhaseMatrix matQuad) {
        PhaseMatrix     matBeam = MAT_SO6_TOBEAM_COORDS.times( matQuad.times(MAT_SO6_TOQUAD_COORDS) );

        return matBeam;
    }
    
    
    /*
     * Initialization
     */

    /**
     *  Creates a new, initialized instance of <code>IdealMagSkewQuad3</code>.
     *  The length and field strength are set to the given values.
     *
     *  @param  strId     string identifier for this element
     *  @param  dblFld    field gradient strength (in <b>Tesla/meter</b>)
     *  @param  dblLen    length of the skew quadrupole body
     *  
     * @author Christopher K. Allen
     * @since  Mar 14, 2008
     *  
     */
    public IdealMagSkewQuad3(String strId, double dblFld, double dblLen) 
    {
        super(STR_TYPE_ID, strId, dblLen);
        // Skew quadrupole have no orientation per se.
        // Or, rather, they all have the same orientation.

        this.setMagField(dblFld);
    };


    /**
     * Create new, uninitialized <code>IdealMagSkewQuad3</code> object.
     * Use this contructor with caution as the length of the magnet is
     * uninitialized.
     *  
     * @author Christopher K. Allen
     * @since  Mar 14, 2008
     */
    public IdealMagSkewQuad3(String strId) {
        super(STR_TYPE_ID, strId);
    }

    /**
     * Create new, uninitialized <code>IdealMagSkewQuad3</code> object.
     * Use this contructor with caution as the length of the magnet is
     * uninitialized.
     *  
     * @author Christopher K. Allen
     * @since  Mar 14, 2008
     */
    public IdealMagSkewQuad3() {
        super(STR_TYPE_ID);
    }
    


    /*
     * Attribute Query
     */    
     
     
    /**
     * <p>
     * Get the skew angle of the quadrupole magnet.  For
     * a description of this parameter see
     * {@link IdealMagSkewQuad3}.
     * </p>
     * <p>
     * Note that this parameter is fixed to the value of
     * {@link IdealMagSkewQuad3#DBL_SKEW_ANGLE}. 
     * </p>
     * 
     * @return  skew angle about the beam axis (in <b>radians</b>)
     * 
     * @see IdealMagSkewQuad3
     * @see IdealMagSkewQuad3#DBL_SKEW_ANGLE
     */
    public double   getSkewAngle() {
        return DBL_SKEW_ANGLE;
    }
       
    


    /*
     *  IElectromagnet Interface
     */


    /**
     * <p>
     *  Return the enumeration code specifying the focusing orientation of the 
     *  quadrupole.  Note that the orientation of an <code>IdealMagSkewQuad3</code>
     *  object <b>cannot</b> change.  Thus, this method will always return the
     *  value <code>IElectromagnet.ORIENT_NONE</code>
     * </p>
     * <p>
     *  NOTE:
     *  <br>The magnet orientation property has no effect upon the operation of the
     *  modeling element.  This method is needed to satisfy the
     *  <code>IElectromagnet</code> interface, which should be 
     *  re-designed because of this fact.
     *  </p>
     *  
     *  @return     the value <code>IElectromagnet.ORIENT_NONE</code>
     */
    @Override
    public int getOrientation() {
        return IElectromagnet.ORIENT_NONE;
    };

    /**
     * <p>
     *  NOTE:
     *  <br>This method has no effect.  It is needed to satisfy the
     *  <code>IElectromagnet</code> interface, which should be 
     *  re-designed because of this fact.
     *  </p>
     *  <p>
     *  Normally this method sets the quadrupole magnet focusing orientation, as
     *  required by the <code>IElectromagnet</code> interface.
     * </p>
     *  
     *  @param  enmOrient   focusing orientation enumeration code (not used)
     *
     *  @see    IdealMagSkewQuad3#getOrientation
     */
    @Override
    public void setOrientation(int enmOrient) {};

    


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
     *   
     * @since   Mar 13, 2008
     * @author  Christopher K. Allen
     *
     * @see xal.model.elem.ThickElement#elapsedTime(xal.model.IProbe, double)
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return super.compDriftingTime(probe, dblLen);
    }
    
    /**
     * 
     *  Return the energy gain imparted to a particular probe.  For an ideal quadrupole
     *  magnet this value is always zero.
     *  
     *  @param  probe   dummy argument
     *  @param  dblLen  dummy argument
     *  @return         returns a zero value
     *  
     * @since   Mar 13, 2008
     * @author  Christopher K. Allen
     *
     * @see xal.model.elem.ThickElement#energyGain(xal.model.IProbe, double)
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
       return 0.0;
    }

    
    /**
     *  Compute the partial transfer map of an ideal quadrupole for the particular probe.
     *  Computes transfer map for a section of quadrupole <code>dblLen</code> meters in length.
     *  @param  probe   supplies the charge, rest and kinetic energy parameters
     *  @param  length  compute transfer matrix for section of this length
     *  @return         transfer map of ideal quadrupole for particular probe
     *  
     * @since   Mar 13, 2008
     * @author  Christopher K. Allen
     *
     * @see xal.model.elem.ThickElement#transferMap(xal.model.IProbe, double)
     */
    @Override
    public PhaseMap transferMap( final IProbe probe, final double length ) {
        double charge = probe.getSpeciesCharge();
        double Er = probe.getSpeciesRestEnergy();
        double beta = probe.getBeta();
        double gamma = probe.getGamma();
        
        // focusing constant (radians/meter)
        final double k = ( charge * LightSpeed * getMagField() ) / ( Er * beta * gamma );
        final double kSqrt = Math.sqrt( Math.abs( k ) );

        // Compute the transfer matrix components
        final double[][] arrF = QuadrupoleLens.transferFocPlane( kSqrt, length );
        final double[][] arrD = QuadrupoleLens.transferDefPlane( kSqrt, length );
        final double[][] arr0 = DriftSpace.transferDriftPlane( length );
        
        // Build the transfer matrix from its component blocks
        PhaseMatrix matQuad = new PhaseMatrix();

        matQuad.setSubMatrix( 4, 5, 4, 5, arr0 ); // a drift space longitudinally
        matQuad.setElem( 6, 6, 1.0 ); // homogeneous coordinates
        
        if ( k >= 0.0 ) {
            matQuad.setSubMatrix( 0, 1, 0, 1, arrF );
            matQuad.setSubMatrix( 2, 3, 2, 3, arrD );            
        }
        else if ( k < 0.0 ) {
            matQuad.setSubMatrix( 0, 1, 0, 1, arrD );
            matQuad.setSubMatrix( 2, 3, 2, 3, arrF );            
        }
        
        PhaseMatrix matBeam = IdealMagSkewQuad3.toBeamFrame(matQuad);
        
        return new PhaseMap( matBeam );
    }


    /*
     *  Testing and Debugging
     */

    /**
     *  Dump current state and content to output stream.
     *
     *  @param  os      output stream object
     */
    public void print(PrintWriter os) {
        super.print(os);

        os.println("  magnetic field     : " + this.getMagField());
        os.println("  skew angle         : " + this.getSkewAngle());
    };

    
    
    
    /*
     * Internal Support
     */
     
}
