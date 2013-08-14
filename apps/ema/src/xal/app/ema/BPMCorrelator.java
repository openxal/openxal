/**
 * This class performs energy calculations, based on TOF
 * @author    J. Galambos
 */

package xal.app.ema;

import xal.tools.correlator.*;
import xal.ca.correlator.*;
import xal.ca.*;

import java.util.*;

public class BPMCorrelator implements CorrelationNotice<ChannelTimeRecord> {
	
	/** the correlator that gets related BPM phases */
	private ChannelCorrelator theCorrelator;
	
	/** the time width to consider signals correlated within (sec) */
	private double correlationWidth = 5.;
	
	/** list of PV names to correlate */
	private ArrayList <String> pvNames = new ArrayList<String>();
	
	/** the calculator to send the correlation to, when received */
	private BPMEnergyCalculator calculator;
	
	/** a poster to  periodically post correlation updates. This is needed because at present (2/2/2006) the diag. timestamps are not updating together */
	private PeriodicPoster<ChannelTimeRecord> poster;
	
	/** the period to post new correlations, if any (sec) */
	private double postPeriod = 1.;
	
	/** the constructor */
	public BPMCorrelator() {
		theCorrelator = new ChannelCorrelator(correlationWidth);
        poster = new PeriodicPoster<ChannelTimeRecord>(theCorrelator, postPeriod);
		poster.addCorrelationNoticeListener(this);
		//setTimespan(1.);
		setFilter(CorrelationFilterFactory.<ChannelTimeRecord>maxMissingFilter(1));
	}

	/** set a filter for the correlator (e.g. signal for minimum beam current) */
	protected void  setFilter(CorrelationFilter<ChannelTimeRecord> newFilter) {
		theCorrelator.setCorrelationFilter(newFilter);
	}
	
	/** set a filter for minimum average current 
	* @param BCMPVName = the name of the PV supplying the avg. beam current
	* @param minCur = the minmum current (A) */
	protected void addMinCurrentFilter(String BCMPVName, double minCur) {
		theCorrelator.addChannel("BCMPVName", RecordFilterFactory.<ChannelTimeRecord>rangeDoubleFilter(0,minCur) );
	}
	/** set the time span for a good correlation */
	
	protected void setTimespan(double dt) {
		theCorrelator.setBinTimespan(dt);
	}
	
	/** add a pair of BPMs to monitor */
	protected void addPair(BPMPair pair) {
		
		if(!pvNames.contains(pair.getBPM1Name()) )
		{
			pvNames.add(pair.getBPM1Name());
			theCorrelator.addChannel(pair.getChannel1());
		}
		if(!pvNames.contains(pair.getBPM2Name()) )
		{
			pvNames.add(pair.getBPM2Name());
			theCorrelator.addChannel(pair.getChannel2());
		}		
	}
	
	
	/** remove apair from the correlator */
	protected void removePair(BPMPair pair) {
		if(pvNames.contains(pair.getBPM1Name()) )
		{
			pvNames.remove(pair.getBPM1Name());
			theCorrelator.removeChannel(pair.getChannel1());
		}
		if(!pvNames.contains(pair.getBPM2Name()) )
		{
			pvNames.remove(pair.getBPM2Name());
			theCorrelator.removeChannel(pair.getChannel2());
		}		
	}
	
	/** add a filtered channel to correlate */
	private void addFilteredChannel(String name, RecordFilter<ChannelTimeRecord> filter) {
		theCorrelator.addChannel(name,filter);
	}
	
	/** sets the calculator to use */
	protected void setCalculator (BPMEnergyCalculator calc) {calculator = calc;}
	
	/** The listener for new correlations */
	public void newCorrelation(Object sender,Correlation<ChannelTimeRecord> correlation) {
		calculator.setCorrelation(correlation);
	}
	
	public void noCorrelationCaught(Object sender) {
		
	}
	
	/** start the correlator going */
	protected void startMonitor() {
		theCorrelator.startMonitoring();
		poster.start();
	}
	
	/** stop the correlator */
	protected void stopMonitor() {
		theCorrelator.stopMonitoring();
	}
}
