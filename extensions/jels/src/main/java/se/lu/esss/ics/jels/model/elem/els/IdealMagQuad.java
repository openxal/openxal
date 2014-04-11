/*
 * IdealMagQuad.java
 *
 * Created on October 7, 2002, 10:36 PM
 *
 * Modified:
 *      02/13/03 CKA    - refactored to new model architecture
 *      03/21/03 CKA    - added JavaBean
 */

package se.lu.esss.ics.jels.model.elem.els;



import java.io.PrintWriter;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.model.elem.IElectromagnet;
import xal.model.elem.ThickElectromagnet;
import xal.model.elem.ThickElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;


public class IdealMagQuad extends ThickElectromagnet {
    /** string type identifier for all IdealMagQuad objects */
    public static final String s_strType = "IdealMagQuad";

    /** Parameters for XAL MODEL LATTICE dtd */
    public static final String s_strParamOrient = "Orientation";
    public static final String s_strParamField = "MagField";


    /*
     * Initialization
     */
         
         
    /** 
     *  Creates a new instance of IdealMagQuad 
     *
     *  @param  strId     identifier for this IdealMagQuad object
     *  @param  enmOrient enumeration specifying the quadrupole orientation
     *                    (ORIENT_HOR or ORIENT_VER)
     *  @param  dblFld    field gradient strength (in <b>Tesla/meter</b>)
     *  @param  dblLen    length of the quadrupole
     */
    public IdealMagQuad(
        String strId,
        int enmOrient,
        double dblFld,
        double dblLen) {
        super(s_strType, strId, dblLen);

        this.setOrientation(enmOrient);
        this.setMagField(dblFld);
    };

    /** 
     *  JavaBean constructor - creates a new unitialized instance of IdealMagQuad
     *
     *  <b>BE CAREFUL</b>
     */
    public IdealMagQuad() {
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
     *  @return         the elapsed time through section<bold>Units: seconds</bold> 
     */
    @Override
    public double elapsedTime(IProbe probe, double dblLen)  {
        return super.compDriftingTime(probe, dblLen);
    }
    
    /**
     *  Return the energy gain imparted to a particular probe.  For an ideal quadrupole
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
     *  Compute the partial transfer map of an ideal quadrupole for the particular probe.
     *  Computes transfer map for a section of quadrupole <code>dblLen</code> meters in length.
     *  @param  probe   supplies the charge, rest and kinetic energy parameters
     *  @param  length  compute transfer matrix for section of this length
     *  @return         transfer map of ideal quadrupole for particular probe
     */
    @Override
    public PhaseMap transferMap( final IProbe probe, final double L) {    	
    	double k;
    	double relativistic_betarho;    	
    	double relativistic_gamma = probe.getGamma();
    	double G = getMagField();
    	
    	relativistic_betarho=probe.getSpeciesRestEnergy()/LightSpeed*probe.getBeta()*relativistic_gamma;
    	k=Math.sqrt(Math.abs(G/relativistic_betarho));

    	PhaseMatrix matPhi = new PhaseMatrix();
    	
    	if (G * probe.getSpeciesCharge()>0)
    	{    		
    		matPhi.setElem(0, 0, Math.cos(k*L));;	
    		matPhi.setElem(0,1,Math.sin(k*L)/k);
    		matPhi.setElem(1,0,-k*Math.sin(k*L));
    		matPhi.setElem(1,1,Math.cos(k*L));	

    		matPhi.setElem(2,2,Math.cosh(k*L));	
    		matPhi.setElem(2,3,Math.sinh(k*L)/k);
    		matPhi.setElem(3,2,k*Math.sinh(k*L));
    		matPhi.setElem(3,3,Math.cosh(k*L));	

    		matPhi.setElem(4,4,1);
    		matPhi.setElem(4,5,L/Math.pow(relativistic_gamma,2));
    		matPhi.setElem(5,5,1);
    	}
    	else if (G * probe.getSpeciesCharge() < 0)
    	{
    		matPhi.setElem(0,0,Math.cosh(k*L));	
    		matPhi.setElem(0,1,Math.sinh(k*L)/k);
    		matPhi.setElem(1,0,k*Math.sinh(k*L));
    		matPhi.setElem(1,1,Math.cosh(k*L));	

    		matPhi.setElem(2,2,Math.cos(k*L));	
    		matPhi.setElem(2,3,Math.sin(k*L)/k);
    		matPhi.setElem(3,2,-k*Math.sin(k*L));
    		matPhi.setElem(3,3,Math.cos(k*L));	

    		matPhi.setElem(4,4,1);
    		matPhi.setElem(4,5,L/Math.pow(relativistic_gamma,2));
    		matPhi.setElem(5,5,1);
    	} else
			try {
				return new IdealDrift("", L).transferMap(probe, L); //TODO make nicer
			} catch (ModelException e) { 
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
