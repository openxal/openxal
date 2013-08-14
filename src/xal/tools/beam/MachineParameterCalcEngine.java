/**
 * MachineParameterEngine.java
 *
 * Author  : Christopher K. Allen
 * Since   : Aug 14, 2013
 */
package xal.tools.beam;

import xal.model.probe.traj.IPhaseState;
import xal.tools.math.r3.R3;
import xal.tools.data.DataAdaptor;
import Jama.Matrix;

/**
 * Class <code></code>.
 *
 *
 * @author Christopher K. Allen
 * @author Patrick Scruggs
 * @since  Aug 14, 2013
 */
public class MachineParameterCalcEngine {




	/** number of modes */
	static final private int NUM_MODES = 3;

	private PhaseMatrix _phaseMatrix;

	public MachineParameterCalcEngine() {
		_phaseMatrix = new PhaseMatrix();
	}


	public PhaseVector calculateFixedPoint() {
		Matrix A = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_ZP, 0, PhaseMatrix.IND_ZP ).minus( Matrix.identity(PhaseMatrix.IND_ZP+1, PhaseMatrix.IND_ZP+1) );
		Matrix b = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_ZP, PhaseMatrix.IND_HOM, PhaseMatrix.IND_HOM ).times( -1 );

		//sako
		//Matrix MZ = m_matPhase.getMatrix(IND_Z,IND_ZP,IND_Z,IND_ZP);
		//      System.out.println("det(MZ), det(A) = "+MZ.det()+" "+A.det());
		//      System.out.println("###### MZ = ("+MZ.get(0,0)+","+MZ.get(0,1)+")("+MZ.get(1,0)+","+MZ.get(1,1)+")");

		PhaseVector sol;

		if (A.det()==0) {
			Matrix Axy = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_YP, 0, PhaseMatrix.IND_YP ).minus( Matrix.identity(PhaseMatrix.IND_YP+1, PhaseMatrix.IND_YP+1) );
			Matrix bxy = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_YP, PhaseMatrix.IND_HOM, PhaseMatrix.IND_HOM ).times( -1 );
			Matrix solutionxy = Axy.solve(bxy);
			//System.out.println("A.det()=0, sxy solved");
			sol = new PhaseVector( solutionxy.get(PhaseMatrix.IND_X, 0), solutionxy.get(PhaseMatrix.IND_XP, 0), solutionxy.get(PhaseMatrix.IND_Y, 0), solutionxy.get(PhaseMatrix.IND_YP, 0), 0, 0 );//sako, check z, zp components!
		} else {

			Matrix solution = A.solve(b);
			sol = new PhaseVector( solution.get(PhaseMatrix.IND_X, 0), solution.get(PhaseMatrix.IND_XP, 0), solution.get(PhaseMatrix.IND_Y, 0), solution.get(PhaseMatrix.IND_YP, 0), solution.get(PhaseMatrix.IND_Z, 0), solution.get(PhaseMatrix.IND_ZP, 0) );
		}
		return sol;
	}

	public R3   compPhaseAdvance(Twiss[] twissOld, Twiss[] twissNew)  {

		int     i;          // loop control
		int     iElem;      // matrix element index
		double  dblR12;     // sub-matrix element R12
		double  dblPhsAd;   // phase advance
		double  betaOld, betaNew;
		R3      vecPhsAd = new R3();    // returned set of phase advances

		for (i=0; i<3; i++) {           // Loop through each plane
			iElem = 2*i;

			dblR12 = _phaseMatrix.getElem(iElem, iElem+1);
			betaOld = twissOld[i].getBeta();
			betaNew = twissNew[i].getBeta();
			dblPhsAd = Math.asin(dblR12/Math.sqrt(betaOld * betaNew) );

			vecPhsAd.set(i, dblPhsAd);
		}

		return vecPhsAd;
	}

	public double[] calculateDispersion(final double gamma) {
		Matrix A = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_YP, 0, PhaseMatrix.IND_YP ).minus( Matrix.identity(PhaseMatrix.IND_YP+1, PhaseMatrix.IND_YP+1) );
		Matrix b = _phaseMatrix.getMatrix().getMatrix( 0, PhaseMatrix.IND_YP, PhaseMatrix.IND_ZP, PhaseMatrix.IND_ZP ).times( -1./(gamma*gamma) );

		Matrix solution = A.solve(b);

		return new double[] { solution.get(PhaseMatrix.IND_X, 0), solution.get(PhaseMatrix.IND_XP, 0), solution.get(PhaseMatrix.IND_Y, 0), solution.get(PhaseMatrix.IND_YP, 0) };
	}

	//	    public Twiss[] getTwiss() {
	//	            final double PI2 = 2 * Math.PI;
	//	            final double[] tunes = _trajectory.getTunes();
	//	            final PhaseMatrix matrix = _phaseMatrix;
	//	            
	//	            final Twiss[] twiss = new Twiss[NUM_MODES];
	//	            for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
	//	                final int index = 2 * mode;
	//	                final double sinMu = Math.sin( PI2 * tunes[mode] );// _tunes could be NaN
	//	                final double m11 = matrix.getElem( index, index );
	//	                final double m12 = matrix.getElem( index, index + 1 );
	//	                final double m22 = matrix.getElem( index + 1, index + 1 );
	//	                final double beta = m12 / sinMu;
	//	                final double alpha = ( m11 - m22 ) / ( 2 * sinMu );
	//	                final double emittance = Double.NaN;
	//	                twiss[mode] = new Twiss( alpha, beta, emittance );
	//	            }
	//	            
	//	            return twiss;
	//	    }


	/**
	 * Unused, courtesy of ICoordinateState & IProbeState
	 */

	public PhaseVector phaseCoordinates() { return null; }

	public PhaseVector getFixedOrbit() { return null; }

	public void setKineticEnergy(double W) { }

	public double getKineticEnergy() { return Double.NaN; }

	public void setTime(double t) { }

	public double getTime() { return Double.NaN; }

	public void setPosition(double s) { }

	public double getPosition() { return Double.NaN; }

	public void setSpeciesCharge(double q) { }

	public double getSpeciesCharge() {return Double.NaN; }

	public void setSpeciesRestEnergy(double Er) { }

	public double getSpeciesRestEnergy() { return Double.NaN; }

	public void setElementId(String id) { }

	public String getElementId() { return null; }

	public void load(DataAdaptor da) { }

	public void save(DataAdaptor da) { }
}

