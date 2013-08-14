/*
 * BPMAgent.java
 *
 * Copyright (c) 2001-2010 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on July 23, 2010
 */

package xal.app.ringmeasurement;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.tools.messaging.*;
import xal.tools.statistics.RunningWeightedStatistics;


import java.util.*;

/**
 * @author cp3, tep
 * BPM agent for use with MIALive.java.
 */
 
public class BPMAgent{

	protected BPM BPMNode;
    protected AcceleratorSeq sequence;
    protected Channel BPMXChannel;
	protected Channel BPMYChannel;
	volatile public RunningWeightedStatistics xStats;
	volatile public RunningWeightedStatistics yStats;
    volatile public double[] xTBT;
	volatile public double[] yTBT;

    // Creates a new BPMAgent 
    public BPMAgent(AcceleratorSeq aSequence, BPM newBPMNode, int numTurns) {
		xTBT = new double[numTurns];
		yTBT = new	double[numTurns];
		xStats = new RunningWeightedStatistics(0.2);
		yStats = new RunningWeightedStatistics(0.2);
        BPMNode = newBPMNode;
        sequence = aSequence;
		
		BPMXChannel = BPMNode.getChannel(BPM.X_TBT_HANDLE);
		BPMYChannel = BPMNode.getChannel(BPM.Y_TBT_HANDLE);
		makeXChannelConnectionListener();
		makeYChannelConnectionListener();
		BPMXChannel.requestConnection();
		BPMYChannel.requestConnection();
		Channel.flushIO();
	}
    
    
    // Name of the BPM as given by its unique ID
    public String name() {
        return BPMNode.getId();
    }
    
    
    // Get the node being wrapped
    public BPM getNode() {
        return BPMNode;
    }
    
    
    // Get the position of the BPM
    public double getPosition() {
        return sequence.getPosition(BPMNode);
    }
    
    
    // Test if the BPM node is okay
    public boolean isOkay() {
        return BPMNode.getStatus();
    }


	// Get the horizontal TBT array of the BPM
    public double[] getXTBT() {
        return xTBT;
    }


	// Get the vertical TBT array of the BPM
	public double[] getYTBT() {
		return yTBT;
	}


	// Add data to the horizontal running average value
	public void addXPoint(double val) {
		xStats.addSample(val);
	}


	// Add data to the vertical running average value
	public void addYPoint(double val) {
		yStats.addSample(val);
	}


	// Get the horizontal average value
	public double getXMean() {
		return xStats.mean();
	}


	// Get the vertical average value
	public double getYMean() {
		return yStats.mean();
	}


	// Reset the horizontal running averages
	public void clearXAvg() {
		xStats.clear();
	}


	// Reset the vertical running averages
	public void clearYAvg() {
		yStats.clear();
	}


    public void addXChannelConnectionListener(ConnectionListener listener) {
		BPMXChannel.addConnectionListener(listener);
    }

	public void addYChannelConnectionListener(ConnectionListener listener) {
		BPMYChannel.addConnectionListener(listener);
	}
    
    public void makeXChannelConnectionListener(){
		this.addXChannelConnectionListener(new ConnectionListener(){
			public void connectionMade(Channel aChannel) {
					makeBPMXMonitor();
			}
			public void connectionDropped(Channel aChannel) {
				System.out.println("x connection lost");
			}
		});
    }

	public void makeYChannelConnectionListener(){
		this.addYChannelConnectionListener(new ConnectionListener(){
			public void connectionMade(Channel aChannel) {
					makeBPMYMonitor();
			}
			public void connectionDropped(Channel aChannel) {
				System.out.println("y connection lost");
			}
		});
    }
    
    public void makeBPMXMonitor(){
		try{
			BPMXChannel.addMonitorValue(new IEventSinkValue(){
				public void eventValue(ChannelRecord record, Channel chan){
					xTBT = record.doubleArray();
				}
			}, Monitor.VALUE);
		}
		catch(ConnectionException e){
			e.printStackTrace();
		}
		catch(MonitorException e){
			e.printStackTrace();
		}
    }

	public void makeBPMYMonitor(){
		try{
			BPMYChannel.addMonitorValue(new IEventSinkValue(){
				public void eventValue(ChannelRecord record, Channel chan){
					yTBT = record.doubleArray();
				}
			}, Monitor.VALUE);
		}
		catch(ConnectionException e){
			e.printStackTrace();
		}
		catch(MonitorException e){
			e.printStackTrace();
		}
    }
}
