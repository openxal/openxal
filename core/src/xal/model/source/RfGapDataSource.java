/*
 * Created on Mar 8, 2004
 */
package xal.model.source;

import xal.model.IModelDataSource;
import xal.tools.math.poly.UnivariateRealPolynomial;

/**
 * Specifies interface for sources used to construct RfGap elements.
 * 
 * @author Craig McChesney
 */
public interface RfGapDataSource extends IModelDataSource {
	
	// Public Interface ========================================================

	/** returns true if this is the first gap in a multi cell cavity */
	public boolean isFirstGap();
	
	/** the gap cell length (m) */
	public double getGapLength();
	
	/** returns the offset of the gap center from the cell center (m) 
	* these may be different e.g. for a DTL cavity */
	public double getGapOffset();

	/** returns a polynomial fit of the TTF vs. beta */
        public UnivariateRealPolynomial getTTFFit();

	/** returns a polynomial fit of the TTF-prime vs. beta */
        public UnivariateRealPolynomial getTTFPrimeFit();	
	
	/** returns a polynomial fit of the S factor vs. beta */
        public UnivariateRealPolynomial getSFit();

	/** returns a polynomial fit of the S-prime vs. beta */
        public UnivariateRealPolynomial getSPrimeFit();	

	/** returns 0 if the gap is part of a 0 mode cavity structure (e.g. DTL)
	* returns 1 if the gap is part of a pi mode cavity (e.g. CCL, Superconducting)
	*/
	public double getStructureMode();	
}
