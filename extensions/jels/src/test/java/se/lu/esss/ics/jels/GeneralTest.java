package se.lu.esss.ics.jels;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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
import static org.junit.Assert.*;

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
		POSITION(0,0, 0.),
		GAMA_1(1,1, 1e-3),
		RMSX(2,2,1e-1),
		RMSXp(3,3,1e-1),
		RMSY(4,4,1e-1),
		RMSYp(5,5,1e-1),
		RMSZ(6,6,1e-1),
		RMSdpp(7,7,1e-1),
		RMSZp(8,8,1e-1),
		BETAX(9,24,0.5),
		BETAY(10,25,0.5);
		
		int openxal;
		int tracewin;
		double allowedError;
		
		private Column(int openxal, int tracewin, double allowedError) {
			this.openxal = openxal;
			this.tracewin = tracewin;
			this.allowedError = allowedError;
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
		
		double[][] dataOX = new double[11][ns];
	    //BasicGraphData myDataX = new BasicGraphData();
		int i = 0;
		while (iterState.hasNext())
	 	{
			EnvelopeProbeState ps = (EnvelopeProbeState) iterState.next();
		        
		    Twiss[] twiss;	
			twiss = ps.twissParameters();			
			
			dataOX[Column.POSITION.openxal][i] = ps.getPosition();
			dataOX[Column.GAMA_1.openxal][i] = ps.getGamma() - 1.; 
			dataOX[Column.RMSX.openxal][i] = twiss[0].getEnvelopeRadius();
			dataOX[Column.RMSXp.openxal][i] = Math.sqrt(twiss[0].getGamma()*twiss[0].getEmittance());
			dataOX[Column.RMSY.openxal][i] = twiss[1].getEnvelopeRadius();
			dataOX[Column.RMSYp.openxal][i] = Math.sqrt(twiss[1].getGamma()*twiss[1].getEmittance());
			dataOX[Column.RMSZ.openxal][i] = twiss[2].getEnvelopeRadius()/ps.getGamma();
			dataOX[Column.RMSdpp.openxal][i] = Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())*ps.getGamma();
			dataOX[Column.RMSZp.openxal][i] = Math.sqrt(twiss[2].getGamma()*twiss[2].getEmittance())/ps.getGamma();
			dataOX[Column.BETAX.openxal][i] = twiss[0].getBeta();
			dataOX[Column.BETAY.openxal][i] = twiss[1].getBeta();		
		    i=i+1;
		}
		
		Column[] allCols = Column.values();
		for (int j = 1; j < allCols.length; j++) {
			double e = compare(dataOX[0], dataTW[0], dataOX[allCols[j].openxal], dataTW[allCols[j].tracewin]);
			System.out.printf("%s: %E\n",allCols[j].name(), e);
			assertTrue(allCols[j].name()+" not within the allowed error", e < allCols[j].allowedError);
			//System.out.printf("%E %E\n",dataOX[allCols[j].openxal][0], dataTW[allCols[j].tracewin][0]);
		}
	}
	
	private double[][] loadTWData() throws IOException
	{
		final int TWcols = 26;
		int nlines = countLines(GeneralTest.class.getResource("ess_out.txt"));
		
		BufferedReader br = new BufferedReader(new InputStreamReader(GeneralTest.class.getResource("ess_out.txt").openStream()));
		//drop headers
		br.readLine();
		br.readLine();
		
		double[][] data = new double[TWcols][nlines-2];
		
		int i = 0;
		for(String line; (line = br.readLine()) != null; i++) {
			String cols[] = line.split("\t", TWcols + 1);
			for (int j = 0; j<TWcols; j++) {
				data[j][i] = new Double(cols[j]);
			}
		}
		br.close();
		return data;
	}
	
	
	private int countLines(URL resource) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(resource.openStream()));
		int i = 0;
		while (br.readLine()!=null) i++;;
		br.close();
		return i;
	}

	/**
	 * Compares two tabulated functions.
	 * @param xa x values of first function
	 * @param xb x values of second function
	 * @param ya y values of first function
	 * @param yb y values of second function
	 * @return returns relative error
	 */
	private double compare(double[] xa, double[] xb, double[] ya, double yb[]) {
		return integrateL1sup(xa,xb,ya,yb)/integrateSup(xb,yb);
	}
	
	/**
	 * Integrates absolute value of a tabulated function. Interpolation is from left, i.e. f(x)=f(x_i) on [x_i,x_i+1).
	 * @param x x values of function
	 * @param y y values of function
	 * @return the integral
	 */
	private double integrateSup(double[] x, double[] y) {
		double I = 0.;
		for (int i = 0; i<x.length-1; i++) {
			I += Math.abs(y[i]) * (x[i+1]-x[i]);
		}
		return I;
	}
	
	/**
	 * Integrates absolute difference of two tabulated functions. Interpolation is from left, i.e. f(x)=f(x_i) on [x_i,x_i+1).
	 * @param xa x values of first function
	 * @param xb x values of second function
	 * @param ya y values of first function
	 * @param yb y values of second function
	 * @return value of the integral
	 */
	private double integrateL1sup(double[] xa, double[] xb, double[] ya, double yb[]) {
		if (xa.length == 0) return integrateSup(xb, yb);
		if (xb.length == 0) return integrateSup(xa, ya);
		
		double x0 = Math.min(xa[0], xb[0]);
		double f0 = ya[0];
		double g0 = yb[0];
		double I = 0.;
		
		for (int i = 0, j = 0; i<xa.length || j < xb.length; )
		{
			double x1,f1 = f0,g1 = g0;
			if (j>=xb.length) {
				x1 = xa[i];
				f1 = ya[i];
				i++;
			} else if  (i>=xa.length) {
				x1 = xb[j];
				g1 = yb[j];
				j++;
			} else if (xa[i]<xb[j]) {
				x1 = xa[i];
				f1 = ya[i];
				i++;
			} else {
				x1 = xb[j];
				g1 = yb[j];
				j++;
			}
			I+=Math.abs(f0-g0)*(x1-x0);
			f0 = f1;
			g0 = g1;
			x0 = x1;
		}
		return I;
	}
	
	/**
	 * Integrates absolute difference of two tabulated functions. Interpolation is linear.
	 * @param xa x values of first function
	 * @param xb x values of second function
	 * @param ya y values of first function
	 * @param yb y values of second function
	 * @return value of the integral
	 */
	private double integrateL1linear(double[] xa, double[] xb, double[] ya, double yb[]) {
		
		// merge the positions together
		double p[] = new double [xa.length + xb.length];
		for (int i = 0, j = 0; i<xa.length || j < xb.length; )
		{
			if (j>=xb.length) {
				p[i+j] = xa[i];
				i++;
			} else if  (i>=xa.length) {
				p[i+j] = xb[j];
				j++;
			} else if (xa[i]<xb[j]) {
				p[i+j] = xa[i];
				i++;
			} else {
				p[i+j] = xb[j];
				j++;
			}
		}
		
		// interpolate
		double f[] = new double[p.length];
		for (int i = 0, k = 0; i<p.length; i++) {
			while (k<xa.length && p[i] >= xa[k]) k++;
			if (p[i] == xa[k-1]) {
				f[i] = ya[k-1];
			} else {
				// interpolate
				if (k >= xa.length) break;
				double a0 = (p[i] - xa[k-1]);
				double a1 = (xa[k] - p[i]);
				f[i] = (a0*ya[k-1] + a1*ya[k]) / (a0+a1);
			}
		}
		
		double g[] = new double[p.length];
		for (int i = 0, k = 0; i<p.length; i++) {
			while (k<xb.length && p[i] >= xb[k]) k++;
			if (p[i] == xb[k-1]) {
				g[i] = yb[k-1];
			} else {
				// interpolate
				if (k >= xb.length) break;
				double a0 = (p[i] - xb[k-1]);
				double a1 = (xb[k] - p[i]);
				g[i] = (a0*yb[k-1] + a1*yb[k]) / (a0+a1);
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
