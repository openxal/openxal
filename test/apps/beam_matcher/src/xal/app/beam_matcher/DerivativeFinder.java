package xal.app.beam_matcher;

import xal.model.ModelException;

public class DerivativeFinder {
    /** Approximates the derivative at a chosen function value
     * @author Matthew Reat
     *
     * */
    
    
    private double deriv;
    
    //	public DerivativeFinder(double q) {
    //		this.q = q;
    //	}
    public double differentiate(double wsvalue1, double wsvalue2, double q) throws ModelException {
        
        //		ModelWireScanData MWSD = new ModelWireScanData();
        //		ModelMagneticField MMF = new ModelMagneticField();
        //
        //		wsvalue1 = MWSD.getbs_h1();
        //		wsvalue2 = MWSD.getbs_h2();
        //
        //		q = MMF.getStepSize();
        
        
        deriv = (wsvalue2 - wsvalue1)/q;
        
        System.out.println(deriv);
        
        return deriv;
        
    }
}
