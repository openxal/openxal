package xal.model.elem;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.optics.DriftSpace;
import xal.model.IProbe;

import java.io.PrintWriter;

/**
 * <p>
 * Models an ideal solenoid magnet.  I don't know who implemented this class
 * or when he or she did so.  I can't really comment on details yet.
 * </p>
 *
 * @author Christopher K. Allen
 * @since   Apr 19, 2011
 */
public class IdealMagSolenoid  extends ThickElectromagnet {

    
    /*
     * Global Attributes
     */
    /** string type identifier for all IdealMagSolenoid objects */
    public static final String s_strType = "IdealMagSolenoid";

    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String s_strParamField = "MagField";

    
    /*
     * Initialization
     */
         
         
    /** 
     *  Creates a new instance of IdealMagSolenoid 
     *
     *  @param  strId     identifier for this IdealMagSolenoid object
     *  @param  dblFld    field gradient strength (in <b>Tesla</b>)
     *  @param  dblLen    length of the solenoid
     */
    public IdealMagSolenoid(String strId, double dblFld, double dblLen) {
        super(s_strType, strId, dblLen);

        this.setMagField(dblFld);
    };

    /** 
     *  JavaBean constructor - creates a new unitialized instance of IdealMagSolenoid
     *
     *  <b>BE CAREFUL</b>
     */
    public IdealMagSolenoid() {
        super(s_strType);
    };


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
     *  Return the energy gain imparted to a particular probe.  For an ideal solenoid
     *  magnet this value is always zero.
     *  @param  probe   dummy argument
     *  @param  dblLen  dummy argument
     *  @return         returns a zero value
     */
    @Override
    public double energyGain(IProbe probe, double dblLen) {
        return 0.0;
    }

	
    /**
     *  Compute the partial transfer map of an ideal solenoid for the particular probe.
     *  Computes transfer map for a section of solenoid <code>dblLen</code> meters in length.
     *  @param  probe   supplies the charge, rest and kinetic energy parameters
     *  @param  length  compute transfer matrix for section of this length
     *  @return         transfer map of ideal quadrupole for particular probe
     */
    @Override
    public PhaseMap transferMap( final IProbe probe, final double length ) {
		double charge = probe.getSpeciesCharge();
        double Er = probe.getSpeciesRestEnergy();
        double beta = probe.getBeta();
        double gamma = probe.getGamma();
        
        // focusing constant (radians/meter)
        final double k = ( charge * LightSpeed * getMagField() ) / ( Er * beta * gamma )/2.;
//		final double kSqrt = Math.sqrt( Math.abs( k ) );

        // Compute the transfer matrix components
//		double r11 = Math.cos(k*length)*Math.cos(k*length);
//		double r12 = 2.*Math.sin(k*length)*Math.cos(k*length)/k;
//		double r13 = Math.sin(k*length)*Math.cos(k*length);
//		double r14 = 2.*Math.sin(k*length)*Math.sin(k*length)/k;
//		double r21 = -1*k*Math.sin(k*length)*Math.cos(k*length)/2.;
//		double r41 = k*Math.sin(k*length)*Math.sin(k*length)/2.;
		double r11 = Math.cos(k*length)*Math.cos(k*length);
		double r12 = Math.sin(k*length)*Math.cos(k*length)/k;
		double r13 = Math.sin(k*length)*Math.cos(k*length);
		double r14 = Math.sin(k*length)*Math.sin(k*length)/k;
		double r21 = -1*k*Math.sin(k*length)*Math.cos(k*length);
		double r41 = k*Math.sin(k*length)*Math.sin(k*length);
		
        final double[][] arr0 = DriftSpace.transferDriftPlane( length );

		// Build the tranfer matrix from its component blocks
        PhaseMatrix matPhi = new PhaseMatrix();

        matPhi.setElem(0, 0, r11);
        matPhi.setElem(1, 1, r11);
        matPhi.setElem(2, 2, r11);
        matPhi.setElem(3, 3, r11);
        matPhi.setElem(0, 1, r12);
        matPhi.setElem(2, 3, r12);
        matPhi.setElem(0, 2, r13);
        matPhi.setElem(1, 3, r13);
        matPhi.setElem(2, 0, -1.*r13);
        matPhi.setElem(3, 1, -1.*r13);
        matPhi.setElem(0, 3, r14);
        matPhi.setElem(2, 1, -1.*r14);
        matPhi.setElem(1, 0, r21);
        matPhi.setElem(3, 2, r21);
        matPhi.setElem(3, 0, r41);
        matPhi.setElem(1, 2, -1.*r41);
        
        matPhi.setSubMatrix( 4, 5, 4, 5, arr0 ); // a drift space longitudinally
        matPhi.setElem( 6, 6, 1.0 ); // homogeneous coordinates
        
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
    }
    

}
