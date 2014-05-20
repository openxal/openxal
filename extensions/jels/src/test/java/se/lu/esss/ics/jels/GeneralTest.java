package se.lu.esss.ics.jels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.lu.esss.ics.jels.model.elem.jels.JElsElementMapping;
import xal.model.ModelException;
import xal.model.alg.EnvelopeTracker;
import xal.model.alg.Tracker;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.ElementMapping;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.Twiss;

public class GeneralTest {

	// load test
	// load probe

	private static EnvelopeProbe setupOpenXALProbe() {
		EnvelopeTracker envelopeTracker = new EnvelopeTracker();			
		envelopeTracker.setRfGapPhaseCalculation(true);
		envelopeTracker.setUseSpacecharge(true);
		envelopeTracker.setEmittanceGrowth(false);
		envelopeTracker.setStepSize(0.004);
		envelopeTracker.setProbeUpdatePolicy(Tracker.UPDATE_EXIT);
		
		EnvelopeProbe envelopeProbe = new EnvelopeProbe();
		envelopeProbe.setAlgorithm(envelopeTracker);		
		
		return envelopeProbe;
	}
	
	public static void setupInitialParameters(EnvelopeProbe probe) {
		probe.setSpeciesCharge(-1);
		probe.setSpeciesRestEnergy(9.3829431e8);
		//elsProbe.setSpeciesRestEnergy(9.38272013e8);	
		probe.setKineticEnergy(3e6);//energy
		probe.setPosition(0.0);
		probe.setTime(0.0);		
				
		double beta_gamma = probe.getBeta() * probe.getGamma();
	
		
		probe.initFromTwiss(new Twiss[]{new Twiss(-0.1763,0.2442,0.2098*1e-6 / beta_gamma),
										  new Twiss(-0.3247,0.3974,0.2091*1e-6 / beta_gamma),
										  new Twiss(-0.5283,0.8684,0.2851*1e-6 / beta_gamma)});
		probe.setBeamCurrent(0.0);
		//probe.setBeamCurrent(50e-3);
		
		// probe.setBunchFrequency(4.025e8); 	
	}
	
	// load optics
	
	private static AcceleratorSeq loadAcceleratorSequence() {
		/* Loading SMF model */				
		Accelerator accelerator = XMLDataManager.acceleratorWithUrlSpec(JElsDemo.class.getResource("main.xal").toString());
				
		if (accelerator == null)
		{			
			throw new Error("Accelerator is empty. Could not load the default accelerator.");
		} 			
		return accelerator;
	}
	
	
	enum Column {
		POSITION(0),
		GAMA_1(1),
		RMSX(2),
		RMSXp(3),
		RMSY(4),
		RMSYp(5),
		RMSZ(6),
		RMSdpp(7),
		RMSZp(8),
		BETAX(9),
		BETAY(10);
		
		int value;
		private Column(int value) {
			this.value = value;
		}
	}
	// run
	@Test
	public void run() throws ModelException, IOException 
	{
		double dataTW[][] = loadTWData();
		
		EnvelopeProbe probe = setupOpenXALProbe(); // OpenXAL probe & algorithm
		setupInitialParameters(probe);
	    AcceleratorSeq sequence = loadAcceleratorSequence();
		Scenario scenario = Scenario.newScenarioFor(sequence);		
		scenario.setProbe(probe);			
						
		// Setting up synchronization mode
		scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);					
		scenario.resync();
						
		// Running simulation
		scenario.setStartElementId("BEGIN_mebt");
		scenario.run();
		
		// Getting results
		Trajectory trajectory = probe.getTrajectory();
		
		Iterator<ProbeState> iterState= trajectory.stateIterator();
	 
		int ns= trajectory.numStates();
		
		double[][] dataOX = new double[ns][11];
	    //BasicGraphData myDataX = new BasicGraphData();
		int i = 0;
		while (iterState.hasNext())
	 	{
			EnvelopeProbeState ps = (EnvelopeProbeState) iterState.next();
		        
		    Twiss[] twiss;	
			twiss = ps.twissParameters();			
			
			dataOX[i][Column.POSITION.value] = ps.getPosition();
			dataOX[i][Column.GAMA_1.value] = ps.getGamma() - 1.; 
			dataOX[i][Column.RMSX.value] = twiss[0].getEnvelopeRadius();
			dataOX[i][Column.RMSXp.value] = Math.sqrt(twiss[0].getGamma()*twiss[0].getEmittance());
			dataOX[i][Column.RMSY.value] = twiss[1].getEnvelopeRadius();
			dataOX[i][Column.RMSYp.value] = Math.sqrt(twiss[1].getGamma()*twiss[1].getEmittance());
			dataOX[i][Column.RMSZ.value] = twiss[2].getEnvelopeRadius()/ps.getGamma();
			dataOX[i][Column.RMSdpp.value] = Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())*ps.getGamma();
			dataOX[i][Column.RMSZp.value] = Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())/ps.getGamma();
			dataOX[i][Column.BETAX.value] = twiss[0].getBeta();
			dataOX[i][Column.BETAY.value] = twiss[1].getBeta();		
		    i=i+1;
		}
		
		compare(dataOX, dataTW, Column.GAMA_1.value, Column.GAMA_1.value);
	}
	
	private double[][] loadTWData() throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(GeneralTest.class.getResource("ess_out.txt").openStream()));
		//drop headers
		br.readLine();
		br.readLine();
		
		List<double[]> lines = new ArrayList<>();
		
		int i = 0;
		for(String line; (line = br.readLine()) != null; ) {
			String cols[] = line.split("\t");
			double dline[] = new double[cols.length];
			for (int j = 0; j<cols.length; j++) {
				dline[j] = new Double(cols[j]);
			}
			lines.add(dline);
		}
		return lines.toArray(new double[lines.size()][]);
	}
	
	
	// compare results

	private double compare(double[][] dataA, double[][] dataB, int colA, int colB) {
		
		// merge the positions together
		double p[] = new double [dataA.length + dataB.length];
		for (int i = 0, j = 0; i<dataA.length || j < dataB.length; )
		{
			if (j>=dataB.length) {
				p[i+j] = dataA[i][0];
				i++;
			} else if  (i>=dataA.length) {
				p[i+j] = dataB[j][0];
				j++;
			} else if (dataA[i][0]<dataB[j][0]) {
				p[i+j] = dataA[i][0];
				i++;
			} else {
				p[i+j] = dataB[j][0];
				j++;
			}
		}
		
		// interpolate
		double f[] = new double[p.length];
		for (int i = 0, k = 0; i<p.length; i++) {
			while (k<dataA.length && p[i] >= dataA[k][0]) k++;
			if (p[i] == dataA[k-1][0]) {
				f[i] = dataA[k-1][colA];
			} else {
				// interpolate
				if (k >= dataA.length) break;
				double a0 = (p[i] - dataA[k-1][0]);
				double a1 = (dataA[k][0] - p[i]);
				f[i] = (a0*dataA[k-1][colA] + a1*dataA[k][colA]) / (a0+a1);
			}
		}
		
		double g[] = new double[p.length];
		for (int i = 0, k = 0; i<p.length; i++) {
			while (k<dataB.length && p[i] >= dataB[k][0]) k++;
			if (p[i] == dataB[k-1][0]) {
				g[i] = dataB[k-1][colB];
			} else {
				// interpolate
				if (k >= dataB.length) break;
				double a0 = (p[i] - dataB[k-1][0]);
				double a1 = (dataB[k][0] - p[i]);
				g[i] = (a0*dataB[k-1][colB] + a1*dataB[k][colB]) / (a0+a1);
			}
		}
		
		// calculate L1
		double I = 0.;
		for (int i = 1; i<p.length; i++) {
			if ((f[i-1]-g[i-1])*(f[i]-g[i]) < 0.) {
				//intersection
				double p0 = ( (f[i]-g[i])*p[i]-(f[i-1]-g[i-1])*p[i-1] ) / (f[i]-f[i-1]-g[i]+g[i-1]);
				I += (p0-p[i-1]) * Math.abs(f[i-1]-g[i-1]) + (p[i]-p0) * Math.abs(f[i]-g[i]);
			} else {
				I += Math.abs((f[i-1]-g[i-1])+(f[i]-g[i]))*(p[i]-p[i-1]);
			}
		}
		I/=2.;
		return I;
	}

}
