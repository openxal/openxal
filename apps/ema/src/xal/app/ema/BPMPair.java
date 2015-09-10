/**
 * This class holds information about a BPM pair to do TOF calcs.
 * @author    J. Galambos
 */

package xal.app.ema;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.tools.statistics.*;

import java.util.*;

public class BPMPair {
	
	/** the 1st BPM label */
	private String BPM1Name;
	/** the 2nd BPM label */
	private String BPM2Name;
	
	/** the 1st BPM phase Channel */
	private Channel BPM1Channel;
	
	/** the 2nd BPM phase Channel */
	private Channel BPM2Channel;
	
	/** the frequency of the BPMs phase (MHz) */
	private double frequency;
	
	/** the distance between the BPM pair (m) */
	protected double length;
	
	/** the initial guess for the energy - used since the solution is degenerate (multivalue) (MeV)*/
	private double WGuess;
	
	/** the statistics of the average energy jitter from this BPM pair */
	protected MutableUnivariateStatistics stats;
	
	/** the calculated energy (MeV) */
	protected Double energy = new Double(0.);
	
	/** the constructor */
	public BPMPair(String name1, String name2) {
		BPM1Name = name1;
		BPM2Name = name2;
		stats = new MutableUnivariateStatistics();
		setChannels();
	}
	
	protected void setChannels() {
		String name1 = BPM1Name + ":phaseAvg";
		String name2 = BPM2Name + ":phaseAvg";
		Channel channel1 = ChannelFactory.defaultFactory().getChannel(name1);
		Channel channel2 = ChannelFactory.defaultFactory().getChannel(name2);
		setChannels(channel1, channel2);
	}
	
	protected void setChannels(Channel c1, Channel c2) {
		BPM1Channel = c1;
		BPM2Channel = c2;
	}
	
	protected void setLength(double l) { length = l;}
	protected double getLength() { return length;}
	protected void setWGuess(double w) { WGuess = w;}
	protected double getWGuess() { return WGuess;}	
	protected void setFreq(double f) { frequency = f;}
	protected double getFreq() { return frequency;}
	protected void setChannel1(Channel c) {BPM1Channel = c;}
	protected Channel getChannel1() { return BPM1Channel;}
	protected void setChannel2(Channel c) {BPM2Channel = c;}
	protected Channel getChannel2() { return BPM2Channel;}	
	protected String getBPM1Name() { return BPM1Name;}
	protected String getBPM2Name() { return BPM2Name;}
	/** the average of the statistics */
	protected double getMean(){return stats.mean();}
	/** the standard deviation of the statistics */
	protected double getSigma(){return stats.standardDeviation();}
	protected Integer getCounts() { return new Integer(stats.population());}
	
	protected Double getEnergy() { return energy;}
	protected void setEnergy(double w) { energy = new Double(0.);}
}

