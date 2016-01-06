/*
 * IdealMagSkewQuad.java
 *
 * Created on June 8, 2006
 *
 * Modified:
 *
 */

package xal.model.elem;



import java.io.PrintWriter;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.DriftSpace;
import xal.tools.beam.optics.QuadrupoleLens;

import xal.model.IProbe;



/**
 * Represents an ideal magnetic skew quadrupole magnet for a beam
 * transport/accelerator system.
 *
 * @author Jeff Holmes
 * 
 * @deprecated  This class has been replaced by <code>IdealMagSkewQuad3</code>
 */
@Deprecated
public class IdealMagSkewQuad extends IdealMagQuad {
    /** string type identifier for all IdealMagSkewQuad objects */
    public static final String s_strType = "IdealMagSkewQuad";

    /** Quadrupole rotation angle */
    private double m_dblSkewAngle = 45.0;




    /*
     * Initialization
     */


    /**
     *  Creates a new instance of IdealMagSkewQuad
     *
     *  @param  strId     identifier for this IdealMagSkewQuad object
     *  @param  enmOrient enumeration specifying the skew quadrupole orientation
     *                    (13 focuses in quadrants 1 and 3 and
     *                     24 focuses in quadrants 2 and 4)
     *  @param  dblFld    field gradient strength (in <b>Tesla/meter</b>)
     *  @param  dblLen    length of the skew quadrupole
     *  @param  dblAng    angle of the skew quadrupole:
     *                    dblAng &gt; 0 -&gt; focuses in quadrants 1 and 3
     *                    dblAng &lt; 0 -&gt; focuses in quadrants 2 and 4
     */
    public IdealMagSkewQuad(
        String strId,
        int enmOrient,
        double dblFld,
        double dblLen,
        double dblAng) {
        super( strId, enmOrient, dblFld, dblLen);

        this.setOrientation(enmOrient);
        this.setMagField(dblFld);
        this.setSkewAngle(dblAng);
    };


    /*
     *  IElectromagnet Interface
     */

    /**
     *  Return the orientation enumeration code.
     *
     *  @return     13          - quadrupole focuses in first and third quadrants
     *              24          - quadrupole focuses in second and fourth quadrants
     *              ORIENT_NONE - error
     */

    /**
     *  Get the rotation angle of the electromagnet.
     *
     *  @return     rotation angle (in <b>degrees</b>).
     */
    public double getSkewAngle() {
        return m_dblSkewAngle;
    };


    /**
     *  Set the rotation angle of the electromagnet.
     *
     *  @param  dblAngle    rotation angle (in <b>degrees</b>).
     */
    public void setSkewAngle(double dblAngle) {
        m_dblSkewAngle = dblAngle;
    };



    /*
     *  ThickElement Protocol
     */

    /**
     *  Compute the partial transfer map of an ideal skew quadrupole for the particular probe.
     *  Computes transfer map for a section of skew quadrupole <code>dblLen</code> meters in length.
     *  @param  probe   supplies the charge, rest and kinetic energy parameters
     *  @param  length  compute transfer matrix for section of this length
     *  @return         transfer map of ideal skew quadrupole for particular probe
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

        // Build the tranfer matrix from its component quad blocks and rotations
        PhaseMatrix matPhi  = new PhaseMatrix();
        PhaseMatrix matQuad = new PhaseMatrix();
        PhaseMatrix matRotP = new PhaseMatrix();
        PhaseMatrix matRotM = new PhaseMatrix();
        PhaseMatrix matTmp  = new PhaseMatrix();

        // Set up horizontal focusing normal quad
        matQuad.setSubMatrix( 0, 1, 0, 1, arrF );
	matQuad.setSubMatrix( 2, 3, 2, 3, arrD );
        matQuad.setSubMatrix( 4, 5, 4, 5, arr0 ); // a drift space longitudinally
        matQuad.setElem( 6, 6, 1.0 ); // homogeneous coordinates

        // Set up rotation
        double angle = Math.PI * m_dblSkewAngle / 180.;
        double cs = Math.cos(angle);
        double sn = Math.sin(angle);
        matRotP.setElem( 0, 0,  cs );
        matRotP.setElem( 0, 2,  sn );
        matRotP.setElem( 1, 1,  cs );
        matRotP.setElem( 1, 3,  sn );
        matRotP.setElem( 2, 0, -sn );
        matRotP.setElem( 2, 2,  cs );
        matRotP.setElem( 3, 1, -sn );
        matRotP.setElem( 3, 3,  cs );
        matRotP.setElem( 4, 4, 1.0 );
        matRotP.setElem( 5, 5, 1.0 );
        matRotP.setElem( 6, 6, 1.0 );

        // Set up inverse rotation
        matRotM = matRotP.inverse();

    /**
     *
     * Multiply the 3 matrices together to get skew quad.
     *
     */

    matTmp = matQuad.times(matRotP);
    matPhi = matRotM.times(matTmp);

    return new PhaseMap( matPhi );
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
        os.println("  skew quad angle    : " + this.getSkewAngle());
    };


};
