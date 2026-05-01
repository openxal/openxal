/*
 * Created on 2005/01/27
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package xal.tools.dyn;

import xal.tools.beam.PhaseMatrix;

/**
 * @author sako
 *
 * transform mtrix R(dr) used to include alignment errors of magnets
 * R should be applied to the transfer matrix
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * 
 * @deprecated  Not used anywhere
 */
@Deprecated
public class TranslationMatrix extends PhaseMatrix {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
	public TranslationMatrix(double dx, double dy, double dz) {
		super();
		//make the identity matrix
		for (int i=0;i<this.getSize();i++) {
			setElem(i,i,1);
		}
		setElem(0,0,1+dx);
		setElem(2,2,1+dy);
		setElem(4,4,1+dz);
	}


	/**
     * Handles object creation required by the base class. 
     *
	 * @see xal.tools.beam.PhaseMatrix#newInstance()
	 *
	 * @author Ivo List
	 * @author Christopher K. Allen
	 * @since  Jun 17, 2014
	 */
	@Override
	protected PhaseMatrix newInstance() {
		return new TranslationMatrix(0.,0.,0.);
	}
}
