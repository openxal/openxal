/**
 * 
 */
package xal.model.elem;

import xal.model.IProbe;
import xal.model.ModelException;
import xal.sim.scenario.LatticeElement;
import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.math.r3.R3;
import xal.tools.math.r3.R3x3;

/**
 * @author ilist
 *
 */
public abstract class SectionEndpoint extends ThinElement {
	protected double len;
	
	public SectionEndpoint(String strType) {
		super(strType);
	}
	
	/**
	 * @see xal.model.elem.ThinElement#elapsedTime(xal.model.IProbe)
	 */
	@Override
	protected double elapsedTime(IProbe probe) {		
		return 0;
	}

	/**
	 * @see xal.model.elem.ThinElement#energyGain(xal.model.IProbe)
	 */
	@Override
	protected double energyGain(IProbe probe) {
		return 0;
	}

	public static class SectionStart extends SectionEndpoint {
		/** string type identifier for all SectionEndpoint objects */
	    public static final String s_strType = "BeginSection";

		public SectionStart() {
			super(s_strType);
		}
		
		/**
		 * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
		 */
		@Override
		protected PhaseMap transferMap(IProbe probe) throws ModelException {
			PhaseMatrix matPhi = PhaseMatrix.identity();
			
			double px = getPhiX();
		    double py = getPhiY();
		    double pz = getPhiZ();
	    	double dx = getAlignX();
	        double dy = getAlignY();
	        double dz = getAlignZ();
	        
		    if (px != 0. || py != 0.) {
		    	PhaseMatrix T = PhaseMatrix.translation(new PhaseVector(px*len/2., -px, py*len/2., -py, 0., 0.));		    	
		    	matPhi = matPhi.times(T);
		    }
		    
		    if (pz != 0.) {		   
		    	PhaseMatrix R = PhaseMatrix.rotationProduct(R3x3.newRotationZ(-pz));		    
		    	matPhi = matPhi.times(R);
		    }		   

	        if ((dx != 0)||(dy != 0)||(dz !=0)) {
	            PhaseMatrix T = PhaseMatrix.spatialTranslation(new R3(-dx, -dy, -dz));
	        	matPhi = matPhi.times(T);
	        }			
			
			return new PhaseMap(matPhi);
		}
	}
	

	public static class SectionEnd extends SectionEndpoint {
		/** string type identifier for all SectionEndpoint objects */
		public static final String s_strType = "EndSection";
		
		public SectionEnd() {
			super(s_strType);
		}	
		
		/**
		 * @see xal.model.elem.ThinElement#transferMap(xal.model.IProbe)
		 */
		@Override
		protected PhaseMap transferMap(IProbe probe) throws ModelException {
			PhaseMatrix matPhi = PhaseMatrix.identity();

			double px = getPhiX();
		    double py = getPhiY();
		    double pz = getPhiZ();
			double dx = getAlignX();
	        double dy = getAlignY();
	        double dz = getAlignZ();
		        
		    if (px != 0. || py != 0.) {
		    	PhaseMatrix T = PhaseMatrix.translation(new PhaseVector(px*len/2., px, py*len/2., py, 0., 0.)); 		    
		    	matPhi = T.times(matPhi);
		    }
		    
		    if (pz != 0.) {		   
		    	PhaseMatrix R = PhaseMatrix.rotationProduct(R3x3.newRotationZ(pz));
		    	matPhi = R.times(matPhi);	    		    
		    }

	        if ((dx != 0)||(dy != 0)||(dz !=0)) {
	        	 PhaseMatrix T = PhaseMatrix.spatialTranslation(new R3(dx,dy,dz));
	             matPhi = T.times(matPhi);
	        } 
			   		   
			return new PhaseMap(matPhi);
		}
		
	}

	@Override
	public void initializeFrom(LatticeElement latticeElement) {
		// TODO Auto-generated method stub
		super.initializeFrom(latticeElement);
		len = latticeElement.getNode().getLength();
	}
}


