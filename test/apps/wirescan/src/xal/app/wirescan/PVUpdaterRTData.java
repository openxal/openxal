/*
 * PVUpdaterRTData.java
 *
 * Created on November 4, 2004, 4:05 PM
 */

package xal.app.wirescan;

import java.util.*;

import xal.ca.*;
import xal.ca.correlator.*;      // for correlator
import xal.tools.correlator.*;   // for correlattion

/**
 * This class handles the wire scanner real-time data update.  Use channel correlator to ensure position, 
 * horizontal, diagonal and vertical PVs are all synchronized.
 *
 * @author  Paul Chu
 */
public class PVUpdaterRTData implements CorrelationNotice<ChannelTimeRecord>, IEventSinkValue, Runnable {
    
   /** the correlator */
    protected ChannelCorrelator correlator;
    
    private Channel[] myChannels;
    private String theID;
    private WireDoc theDoc;
    private WirePanel thePanel;
    private WireData wd;
    
    private ArrayList<ChannelTimeRecord> posArray, vArray, dArray, hArray;
    
    private int counter = 0;
    
    private double stopPos;
    
    Monitor mon;

    /** Creates a new instance of PVUpdaterRTData 
     *
     * @param channels real-time position, vertical, diagonal and horizontal PVs (in this order)
     * @param wsID wire scanner ID
     * @param wiredocument WireDocument
     */
    public PVUpdaterRTData(Channel[] channels, String wsID, WireDoc wiredocument, 
             WirePanel wp, double stopPos) {
        myChannels = channels;
        theID = wsID;
        theDoc = wiredocument;
        thePanel = wp;
        this.stopPos = stopPos;
                
	wd = theDoc.wireDataMap.get((Object) theID);

        double deltaT = 10.;
        correlator = new ChannelCorrelator(deltaT);
        
        posArray = new ArrayList<ChannelTimeRecord>();
        vArray = new ArrayList<ChannelTimeRecord>();
        dArray = new ArrayList<ChannelTimeRecord>();
        hArray = new ArrayList<ChannelTimeRecord>();
        
        // clean up correlator first
        if (correlator.numActiveChannels() != 0)
            correlator.removeAllChannels();
        
        // add channels to the correlator
        for (int i=0; i<myChannels.length; i++)
            correlator.addChannel(myChannels[i]);
        
        correlator.addListener(this);
        
        try {
            myChannels[0].addMonitorValue(this, Monitor.VALUE);
        } catch (ConnectionException e) {
            System.out.println(e);
        } catch (MonitorException e) {
            System.out.println(e);
        }
        
    }
    
    public void newCorrelation(Object sender, Correlation<ChannelTimeRecord> correlation) {
	ChannelTimeRecord posValue, vValue, dValue, hValue;
        
        posValue =
            (correlation.getRecord(myChannels[0].getId()));
        posArray.add(counter, posValue);
        
	vValue = 
            (correlation.getRecord(myChannels[1].getId()));
        vArray.add(counter, vValue);
        
	dValue = 
            (correlation.getRecord(myChannels[2].getId()));
        dArray.add(counter, dValue);
        
	hValue = 
           (correlation.getRecord(myChannels[3].getId()));
        hArray.add(counter, hValue);
        
        refreshGraph();
        
        counter++;
    }    
    
    public void noCorrelationCaught(Object sender) {
	System.out.println("No Correlation found");
    }
    
    public void resetData() {
        counter = 0;
        posArray.clear();
        vArray.clear();
        dArray.clear();
        hArray.clear();
    }
    
    public void startCorrelator() {
        correlator.startMonitoring();
        System.out.println("Correlator Started");
    }
    
    public void stopCorrelator() {
        correlator.stopMonitoring();
        removeMonitor(myChannels[0]);
    }
    
    protected void removeMonitor(Channel aChannel) {
        // remove channel monitor
        mon.clear();
    }

    public void eventValue(ChannelRecord record, Channel chan) {
        if (record.doubleValue() >= stopPos) {
            stopCorrelator();
            System.out.println("Correlator stopped");
        }
    }
    
    public void refreshGraph() {
      Thread update = new Thread(this);
      update.start();
    }
    
    public void run() {
	Object[] posa =  posArray.toArray();
	Object[] va =  vArray.toArray();
	Object[] da =  dArray.toArray();
	Object[] ha =  hArray.toArray();        
        
        double[] pos = new double[counter];
        for (int i=0; i<counter; i++)
            pos[i] = stopPos;
        
        double[] v = new double[counter];
        double[] d = new double[counter];
        double[] h = new double[counter];
        
        boolean outOfBounds = false;
        
        for (int i=0; i<counter; i++) {
            try {
                pos[i] = ((ChannelRecord) posa[i]).doubleValue();
            } catch (ArrayIndexOutOfBoundsException e) {
                outOfBounds = true;
            }
            
            if (!outOfBounds) {
                // convert to regular arrays so they can be used by plotting package
                // also convert to all positive numbers for log scale display
                if (((ChannelRecord) va[i]).doubleValue() != 0.0)
                    v[i] = Math.abs(((ChannelRecord) va[i]).doubleValue());
                else
                    v[i] = 1.e-4;
                if (((ChannelRecord) da[i]).doubleValue() != 0.0)
                    d[i] = Math.abs(((ChannelRecord) da[i]).doubleValue());
                else
                    d[i] = 1.e-4;
                if (((ChannelRecord) ha[i]).doubleValue() != 0.0)
                    h[i] = Math.abs(((ChannelRecord) ha[i]).doubleValue());
                else
                    h[i] = 1.e-4;
            }
        }
        
        wd.position = pos;
        wd.vvaluesS = v;
        wd.dvaluesS = d;
        wd.hvaluesS = h;
    }
    
}
