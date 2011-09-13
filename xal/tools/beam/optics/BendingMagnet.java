/*
 * Created on May 19, 2004
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package xal.tools.beam.optics;

import xal.model.IProbe;
import xal.tools.beam.IConstants;

/**
 * @author Chris Allen
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BendingMagnet {

    /**
     * Compute the path curvature of a magnetic dipole for the given probe. 
     * The path curvature is 1/R where R is the bending radius of the dipole
     * (radius of curvature).  Note that for zero fields the radius of
     * curvature is infinite while the path curvature is zero.
     * 
     * @param   probe   probe object to be deflected
     * @param   dblFld  constant field strength of magnet  
     * 
     * @return  dipole path curvature for given probe (in <b>1/meters</b>)
     */
    public static double   compCurvature(IProbe probe, double dblFld) {

        // Get  parameters
        double B0 = dblFld;
        double c  = IConstants.LightSpeed;
        
        double e = probe.getSpeciesCharge();
        double Er = probe.getSpeciesRestEnergy();
        double gamma = probe.getGamma();
        double beta  = probe.getBeta();


        // Compute the equilibrium curvature h=1/R
        double  h = (e*c*B0)/(beta*gamma*Er);

        return h;
    }


}
