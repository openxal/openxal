/**
 * This class performs energy calculations, based on TOF
 * @author    J. Galambos
 */

package xal.app.ema;

import xal.tools.beam.EnergyFinder;
import xal.tools.correlator.Correlation;
import xal.model.probe.Probe;
import xal.model.probe.traj.ProbeState;
import xal.ca.*;

import java.util.*;

public class BPMEnergyCalculator implements Runnable {
	
	/** whether to keep  running */
	volatile boolean keepOn;
	
	/** flag to check if am already running */
	volatile boolean amRunning = false;
	
	/** the bpmController that is managing stuff */
	private BPMController bpmController;
	
	/** the energy  finder for TOF information */
	private EnergyFinder energyFinder;
	
	/** the active correlation being analzed */
	volatile Correlation<ChannelTimeRecord> activeCorrelation;
	
	/** the latest aquired correlation waiting to be analzed */
	volatile Correlation<ChannelTimeRecord> latestCorrelation;
	
	/** the calculation thread */
	private Thread calcThread;
	
	/** the constructor */
	public BPMEnergyCalculator(Probe<? extends ProbeState<?>> probe, BPMController cont) {
		keepOn = false;
		bpmController = cont;
		energyFinder = new EnergyFinder(probe, 402.5);
		calcThread = new Thread(this, "Energy Calculator");
		//start();
	}

	/** start the energy calculation thread */
	protected void  start() {
		keepOn = true;
		if(!amRunning) calcThread.start();
	}
	
	/** update the latest correlation to work on */
	protected void setCorrelation (Correlation<ChannelTimeRecord>  correlation) {
		latestCorrelation = correlation;
	}
	
	/** what to do to calculate the energy */
	public void run() {
		double diff, energy;
		ChannelRecord c1, c2;
		amRunning = true;
		while(keepOn) {
			if(latestCorrelation != null && latestCorrelation != activeCorrelation) {
				activeCorrelation = latestCorrelation;
				Collection <BPMPair> pairs = bpmController.selectedPairs.values();
				for (BPMPair pair : pairs) {
					if(activeCorrelation.isCorrelated(pair.getChannel1().getId()) && activeCorrelation.isCorrelated(pair.getChannel2().getId()) ) {
						c1 = (ChannelRecord)activeCorrelation.getRecord(pair.getChannel1().getId());
						c2 = (ChannelRecord)activeCorrelation.getRecord(pair.getChannel2().getId());
						diff = c2.doubleValue() - c1.doubleValue();
						energyFinder.initCalc(pair.getLength(), pair.getWGuess());
						energy = energyFinder.findEnergy(diff);
						pair.energy = new Double(energy);
						pair.stats.addSample(energy);
					}
				}
				try {
					bpmController.updateBPMTable();
					Thread.sleep(500);
				}
				catch (Exception ex) {
					bpmController.dumpErr("Trouble sleeping in the BPM calculator");
				}
			// add a sleep and table update here
			}
	    }
	}

}

