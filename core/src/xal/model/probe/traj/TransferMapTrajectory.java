/*
 * Created on Jun 9, 2004
 *
 *  Copyright   SNS/LANL, 2004
 */
package xal.model.probe.traj;

import xal.tools.beam.PhaseMap;
import xal.tools.beam.PhaseMatrix;
import xal.tools.math.r3.R3;

import java.util.Iterator;

/**
 * Specializes the <code>Trajectory</code> class to the 
 * <code>TransferMapProbe<code> behavior.
 * 
 * @author  Christopher K. Allen
 * @since   Jun 9, 2004
 * @version Oct 22, 2013
 *
 */
public class TransferMapTrajectory extends Trajectory {
	static final private int NUM_MODES = 3;
	
	/** full turn map at the lattice origin */
	@Deprecated
	final private PhaseMap _originFullTurnMap;
	
	/** tunes for x, y and z */
	@Deprecated
	final private double[] _tunes;
	
	/** tunes for x, y and z */
	@Deprecated
	final private double[] _fullTunes;
	
	
	/** indicates if the tune needs to be calculated */
	private boolean _needsTuneCalculation;
	/** indicates if the tune needs to be calculated */
	private boolean _needsFullTuneCalculation;

	/**
	 * Constructor
	 */
	public TransferMapTrajectory() {
		_originFullTurnMap = new PhaseMap();
		_tunes = new double[3];
		_fullTunes = new double[3];
		_needsTuneCalculation = true;
		 _needsFullTuneCalculation = true;
	}


    /**
     * Create and return an uninitialized <code>ProbeState</code> object
     * of the appropriate type.
     * 
     * @return  an empty <code>TransferMapState</code> object
     * 
     * @see gov.sns.xal.model.probe.traj.Trajectory#newProbeState()
     * @see gov.sns.xal.model.probe.traj.TransferMapState
     */
    @Override
    protected ProbeState newProbeState() {
        return new TransferMapState( this, null, null );
    }
	
	/**
	 * Set the full turn map to the one specified.
	 *
	 * @param fullTurnMap the full turn map to use for the trajectory
	 * 
	 * @deprecated This is not a parameter you can change post simulation
	 */
    @Deprecated
	public void setFullTurnMap( final PhaseMap fullTurnMap ) {
		_originFullTurnMap.setFrom( fullTurnMap );
	}
	
	
	/**
	 * Get the full turn map at the origin.
	 */
	public PhaseMap getFullTurnMapAtOrigin() {
		return _originFullTurnMap;
	}
	
	
	/**
	 * Get the x, y and z tunes.
	 * @return the array of tunes of the three modes (x, y and z).
	 * 
	 * @deprecated TransferMapProbes do not have tunes
	 */
	@Deprecated
	public double[] getTunes() {
		calculateTunesIfNeeded();
		
		return _tunes;
	}
	
	/**
	 * CKA- I'm adding Javadoc to be complete.  This computation
	 * really belongs in a separate probe mechanism.
	 *
	 * @return I guess it's supposed to be the ring tune
	 *
	 * @author Unknown
	 * @since  Unknown
	 * 
     * @deprecated TransferMapProbes do not have tunes
	 */
	@Deprecated
	public double[] getFullTunes() {
		calculateFullTunesIfNeeded();
		
		return _fullTunes;
	}
	
	/**
	 * 
	 *
	 * @author Christopher K. Allen
	 * @since  Oct 22, 2013
	 * 
     * @deprecated Moved to xal.tools.beam.calc
	 */
	@Deprecated
	private void calculateFullTunesIfNeeded() {


		if (!_needsFullTuneCalculation) {
			return;
		} else {
			_needsFullTuneCalculation = false;
		}
	
		// is it necessary?
		_needsTuneCalculation = true;
		
		calculateTunesIfNeeded();

		Iterator<ProbeState> iter = this.stateIterator();
		
		
		int nx=0;
		int ny=0;
		int nz=0;
		
		double betaxMax = 0;
		double betayMax = 0;
		double betazMax = 0;
		
		double epsilon = Math.PI/4;
		
		int counter = 0;
		while (iter.hasNext()) {
			TransferMapState state = (TransferMapState)iter.next();
			R3 beta = state.getBetatronPhase();
					
			if ((betaxMax > 2*Math.PI-epsilon)&&(beta.getx()<epsilon)) {
				betaxMax = 0;
				nx++;
			} else if (beta.getx() > betaxMax) {
				betaxMax = beta.getx();
			}
			if ((betayMax > 2*Math.PI-epsilon)&&(beta.gety()<epsilon)) {
				betayMax = 0;
				ny++;
			} else if (beta.gety()> betayMax) {
				betayMax = beta.gety();
			}
			if ((betazMax > 2*Math.PI-epsilon)&&(beta.getz()<epsilon)) {
				betayMax = 0;
				ny++;
			} else if (beta.getz()> betazMax) {
				betazMax = beta.gety();
			}
			counter++;
		}
		
		if (_tunes[0] < 0) {
			_fullTunes[0] = _tunes[0] + (nx+1);
		} else {
			_fullTunes[0] = _tunes[0] + nx;
		}
		if (_tunes[1] < 0) {
			_fullTunes[1] = _tunes[1] + (ny+1);
		} else {
			_fullTunes[1] = _tunes[1] + ny;
		}
		if (_tunes[2] < 0) {
			_fullTunes[2] = _tunes[2] + (nz+1);
		} else {
			_fullTunes[2] = _tunes[2] + nz;
		}
		
	}
	
	
	/**
	 * Calculate the x, y and z tunes
	 * 
	 * @deprecated Moved to xal.tools.beam.calc
	 */
    //sako version look at the sign of M12, and determine the phase
    // since M12=beta*sin(mu) and beta>0, if M12>0, sin(mu)>0, 0<mu<pi
    //                                    if M12<0, sin(mu)<0, -pi<mu<0
	// sako
	@Deprecated
	private void calculateTunesIfNeeded() {
		if ( _needsTuneCalculation ) {
			final double PI2 = 2 * Math.PI;
			final PhaseMatrix matrix = _originFullTurnMap.getFirstOrder();
			
			for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
				final int index = 2 * mode;
				double trace = matrix.getElem( index, index ) + matrix.getElem( index + 1, index + 1 );

				double m12   = matrix.getElem( index, index+1 );                               
				double mu    = Math.acos( trace / 2 );
				// problem is when abs(trace)>1, then _tunes are Double.NaN
			    if (m12<0) {
				    mu *= (-1);
				}
				_tunes[mode] = mu / PI2;			
			}
			_needsTuneCalculation = false;
		}
	}

    /* original
    
	private void calculateTunesIfNeeded() {
		if ( _needsTuneCalculation ) {
			final double PI2 = 2 * Math.PI;
			final PhaseMatrix matrix = _originFullTurnMap.getFirstOrder();
			
			for ( int mode = 0 ; mode < NUM_MODES ; mode++ ) {
				final int index = 2 * mode;
				double trace = matrix.getElem( index, index ) + matrix.getElem( index + 1, index + 1 );
				_tunes[mode] = Math.acos( trace / 2 ) / PI2;					
			}
			
			_needsTuneCalculation = false;
		}
	}*/
}
