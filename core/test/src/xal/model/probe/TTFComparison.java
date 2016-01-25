package xal.model.probe;


import java.io.PrintStream;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import xal.model.alg.EnvTrackerAdapt;
import xal.sim.scenario.AlgorithmFactory;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.smf.impl.RfGap;

import java.util.List;

import xal.model.IElement;
import xal.model.ModelException;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.tools.math.fnc.poly.RealUnivariatePolynomial;


public class TTFComparison {
	/*
     * Global Variables
     */
	/** The seq id. */
	private static String seqID = "MEBT";  // To choose a different sequence or combosequence, change the seqID string
	
	/** The ostr typeout. */
	private static PrintStream        OSTR_TYPEOUT;
	
	/** The accl test. */
	private static Accelerator		  ACCL_TEST;
	
	/** The seq test. */
	private static AcceleratorSeq	  SEQ_TEST;
	
	/** The algorithm. */
	private static EnvTrackerAdapt    ALGORITHM;
	
	/**
	 * Sets up the global variable before testing.
	 *
	 * @throws Exception the exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	
	@BeforeClass
    public static void setUpBeforeClass() throws Exception {
		OSTR_TYPEOUT = System.out;
		OSTR_TYPEOUT.println("Launching TTF Comparison JUnitTest...");
		ACCL_TEST = XMLDataManager.loadDefaultAccelerator();
		SEQ_TEST = ACCL_TEST.findSequence(seqID);
        ALGORITHM = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
	}
	/**
	 * Tests whether or not the individual RF Gap TTFs are loaded into the simulation.
	 *
	 * @throws InstantiationException the instantiation exception
	 * @throws ModelException the model exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	
	@Test
	public final void test() throws InstantiationException, ModelException {

	    // Increase the number of adaptive steps for stiff systems
		ALGORITHM.setMaxIterations(30000);
		
		// Create the simulation probe
        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(SEQ_TEST,ALGORITHM);
        probe.reset();
		
        // Create the simulation scenario, assign the probe, and run
		Scenario model = Scenario.newScenarioFor(SEQ_TEST);
		
		model.setProbe(probe);
		model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		model.resync();
		model.run();
		
		List<RfGap> lstGaps = SEQ_TEST.getAllNodesOfType("RG");
		for (RfGap gap : lstGaps) {
			List<IElement> lstElems = model.elementsMappedTo(gap);
			IElement       elemLast = lstElems.get( lstElems.size() - 1 );
			@SuppressWarnings("unchecked")
            List<EnvelopeProbeState> lstStates = (List<EnvelopeProbeState>) model.trajectoryStatesForElement( elemLast.getId() );
			
			//gets the last state
            EnvelopeProbeState state = lstStates.get(0);
		    
		    //gets the kinetic energy at this state
		    Double W = state.getKineticEnergy();
		    
		    //gets the transit time factor for this gap
		    Double dblTtf = gap.getGapTTF();
		    
		    //gets beta for this gap
		    Double beta = state.getBeta();
		    
		    //gets the name of the gap
		    String name = gap.getId();
		    
		    //creates a polynomial from the TTF Fit
		    RealUnivariatePolynomial polyT = gap.getTTFFit();
		    RealUnivariatePolynomial polyS = gap.getSFit();
		    RealUnivariatePolynomial polyTp = gap.getTTFPrimeFit();
		    RealUnivariatePolynomial polySp = gap.getSPrimeFit();

		    Double k = calculateK(seqID,beta);
		    
		    //evaluate the polynomial at k
		    Double dblT = polyT.evaluateAt(beta);
		    Double dblS = polyS.evaluateAt(k);
		    Double dblTp = polyTp.evaluateAt(k);
		    Double dblSp = polySp.evaluateAt(k);
		    
		    //print information to standard output
		    OSTR_TYPEOUT.println("ID: " + name +" Energy: " + W + " Beta: " + beta 
		            + " Design T: " + dblTtf
		            + " T(k): " + dblT
		            + " T'(k): " + dblTp
		            + " S(k): " + dblS
		            + " S'(k): " + dblSp
		            );
		}
		
		

	}
	/**
	 * Tear down after class.
	 *
	 * @throws Exception the exception
	 * @since  July 6, 2015   by James M. Ghawaly Jr.
	 */
	@AfterClass
    public static void tearDownAfterClass() throws Exception {
		OSTR_TYPEOUT.println("Test Complete");
    }
	
	/**
	 * Calculate k.
	 *
	 * @param seqName the name of the sequence
	 * @param beTA Double: Beta
	 * @return k as double
	 */
	public double calculateK(String seqName,Double beTA) {
		Double frequency = null;
		if(seqName.startsWith("SCL")||seqName.startsWith("CCL")) {
			frequency = 805000000.0;
		}
		else {
			frequency = 402500000.0;
		}
		return (2*Math.PI*frequency)/(beTA*299792458);
	}

}
