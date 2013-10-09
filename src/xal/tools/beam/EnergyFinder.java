package xal.tools.beam;

import xal.model.IProbe;

/**
 * A class to find the energy of the beam, given information about measured  phase differences between BPMs
 * We assume the BPMs report phase differences from -180 to 180
 * @author  J. Galambos
 */

public class EnergyFinder {

	/** rest mass of the beam (MeV) */
	private double restMass;
	
	/** separation of the BPMs (m) */
	private double length;
	
	/** the initial guess of the beam energy (MeV) 
	* we will try and find the beam energy that gives the right phase,
	* within a 2*pi interval from this energy */
	private double EGuess;
		
	/** frequency of BPM phase (Hz) */
	private double frequency;
	
	/** Speed of light in a vacuum (meters/second) */
	//private final double cLight = 299792458;  
	
	/** error tolerance (relative) */
	private final double tol = 1.e-5;
	
	/** max number of iterations */
	private final int nMax = 30;
	
	/** constructor 
	* @param probe for the beam
	* @param the BPM frequency (MHz)
	*/
	public EnergyFinder(IProbe probe, double freq) {
	    restMass = probe.getSpeciesRestEnergy()/1.e6;
	    frequency = freq*1.e6;
    }

    /** initialize problem specific information */
    public void initCalc(double l, double E) {
	    EGuess = E;
	    length = l;
    }
    
   /** solve the problem, using a simple linear step scheme
    * @param the phase difference between BPMs in deg
    * @param the starting guess for energy (MeV) */
    
    public double findEnergy(double targetPhase, double E) {
	    EGuess = E;
	    return findEnergy(targetPhase);
    }
	    /** solve the problem, using a simple linear step scheme
    * @param the phase difference between BPMs in deg*/
    public double findEnergy(double targetPhase) {
	    double error, errorOld = 1.;
	    double ENew, slope, b, step, temp;
	    int nTrys = 0;
	    // solve in space -180 < phi < 180
	    error = findPhase(EGuess) - targetPhase;
	    //if (error > 360.) error -= 360.;
	    if(error < -180. ) error+= 360.;
	    if(error > 180. ) error-= 360.;
	    //System.out.println("EGuess = " + EGuess + " error = " + error);
	    errorOld = error;
	    //EOld = EGuess;
	    //step = EGuess * 1.e-4;
	    //if(error > 180.)
		    //step = -EGuess * 0.005;
	    //else
		    step = EGuess * 0.005;
	    ENew = EGuess + step;
	    while (Math.abs((error/targetPhase)) > tol && (nTrys < nMax) )  {
		    error = findPhase(ENew) - targetPhase;
		    //if (error > 360.) error -= 360.;
		    if(error < -180. ) error+= 360.;
		    if(error > 180. ) error-= 360.;
		    slope = (error - errorOld)/step;
		    //System.out.println("E = " + ENew + " error = " + error);
		    b = error - slope*ENew;
		    temp = -b/slope;
		    step = temp - ENew;
		    errorOld = error;
		    ENew = temp;
		    nTrys++;
	    }
	    if( nTrys < nMax) return ENew;
	    else return -1.;
    }
    
    /** find the phase for a given energy */
    private double findPhase(double E) {
	    double gamma = 1 + E/restMass;
	    double beta = Math.sqrt(1.0 - 1.0/(gamma*gamma));
	    double time = length/(beta*IConstants.LightSpeed);
	    double phase = ((time * frequency)%1.) * 360.;
	    return phase;
    }
}
