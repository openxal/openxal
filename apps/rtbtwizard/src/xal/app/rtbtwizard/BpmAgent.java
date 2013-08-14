/*
 * BpmAgent.java
 *
 */

package xal.app.rtbtwizard;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.tools.*;
import xal.ca.*;
import xal.tools.messaging.MessageCenter;
import xal.tools.statistics.RunningWeightedStatistics;

import java.util.*;

/**
 * Wrap a BPM node with methods that conveniently provide information and control
 * relevant to orbit correction.  A BPM agent provides data specific to a
 * particular plane, hence a BPM nodes gets wrapped by a horizontal and a
 * vertical BpmAgent.  BpmAgent is abstract, but it has two concrete subclasses
 * that provide plane specific identification (HorzBpmAgent and VertBpmAgent).
 *
 * @author  cp3
 */
public class BpmAgent implements AveragingChannelDoubleValueChangeListener {
	/** message center for dispatching events */
	final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy to forward events to registered listeners */
	final protected BpmAgentListener EVENT_PROXY;
	
	/** monitors and maintains the running horizontal average value */
	final protected AveragingChannelMonitor X_AVERAGING_CHANNEL_MONITOR;
	
	/** monitors and maintains the running vertical average value */
	final protected AveragingChannelMonitor Y_AVERAGING_CHANNEL_MONITOR;
	
    protected BPM bpmNode;
    protected AcceleratorSeq sequence;
    
    
    /** Creates new BpmAgent */
    public BpmAgent( final AcceleratorSeq aSequence, final BPM newBpmNode ) {
		MESSAGE_CENTER = new MessageCenter( "BPM Agent" );
		EVENT_PROXY = MESSAGE_CENTER.registerSource( this, BpmAgentListener.class );
		
        bpmNode = newBpmNode;
        sequence = aSequence;
        
		X_AVERAGING_CHANNEL_MONITOR = new AveragingChannelMonitor( bpmNode.getChannel( BPM.X_AVG_HANDLE ), this );
		Y_AVERAGING_CHANNEL_MONITOR = new AveragingChannelMonitor( bpmNode.getChannel( BPM.Y_AVG_HANDLE ), this );
    }
	
	
	/** add a listener of BPM Agent events */
	public void addBpmAgentListener( final BpmAgentListener listener ) {
		MESSAGE_CENTER.registerTarget( listener, this, BpmAgentListener.class );
	}
	
	
	/** remove a listener of BPM Agent events */
	public void removeBpmAgentListener( final BpmAgentListener listener ) {
		MESSAGE_CENTER.removeTarget( listener, this, BpmAgentListener.class );
	}
    
    
    /** Name of the BPM as given by its unique ID */
    public String name() {
        return bpmNode.getId();
    }
    
    
    /** Get the node being wrapped */
    public BPM getNode() {
        return bpmNode;
    }
    
    
    /** Get the position of the BPM */
    public double position() {
        return getPosition();
    }
    
    
    /** Get the position of the BPM */
    public double getPosition() {
        return sequence.getPosition(bpmNode);
    }
    
    
	/** determine whether the BPM has atleast one valid plane */
	public boolean hasValidPlane() {
		return getXAvgChannel().isValid() || getYAvgChannel().isValid();
	}
    
    
	/** get the channel for X-Average */
	public Channel getXAvgChannel() {
		return bpmNode.getChannel( BPM.X_AVG_HANDLE );
	}
    
    
	/** get the channel for Y-Average */
	public Channel getYAvgChannel() {
		return bpmNode.getChannel( BPM.Y_AVG_HANDLE );
	}
	
	
	/** clear the beam position statistics */
	public void clearBeamPositionRunningAverage() {
		X_AVERAGING_CHANNEL_MONITOR.clearStatistics();
		Y_AVERAGING_CHANNEL_MONITOR.clearStatistics();
	}
	
	
	/** start averaging */
	public void startAveraging() {
		X_AVERAGING_CHANNEL_MONITOR.startAveraging();
		Y_AVERAGING_CHANNEL_MONITOR.startAveraging();
	}
	
	
	/** stop averaging */
	public void stopAveraging() {
		X_AVERAGING_CHANNEL_MONITOR.stopAveraging();
		Y_AVERAGING_CHANNEL_MONITOR.stopAveraging();
	}
    
	
	/** determine if the the monitor has any samples */
	public boolean hasRunningAverageSamples() {
		return X_AVERAGING_CHANNEL_MONITOR.hasRunningAverageSamples() && Y_AVERAGING_CHANNEL_MONITOR.hasRunningAverageSamples();
	}
    
    
	/** determine if the the monitor has at least one valid plane and samples in all valid planes */
	public boolean hasRunningAverageSamplesInValidPlanes() {
		if ( hasValidPlane() ) {
			final Channel xAvgChannel = getXAvgChannel();
			final Channel yAvgChannel = getYAvgChannel();
            
			// either a channel must be invalid or have running average samples
			final boolean xAvgChannelSatisfied = !isXAvgChannelValid() || X_AVERAGING_CHANNEL_MONITOR.hasRunningAverageSamples();
			final boolean yAvgChannelSatisfied = !isYAvgChannelValid() || Y_AVERAGING_CHANNEL_MONITOR.hasRunningAverageSamples();
			return xAvgChannelSatisfied && yAvgChannelSatisfied;
		}
		else {
			return false;
		}
	}
    
    
	/** determine whether the XAvg channel is marked valid */
	public boolean isXAvgChannelValid() {
		return getXAvgChannel().isValid();
	}
    
    
	/** determine whether the YAvg channel is marked valid */
	public boolean isYAvgChannelValid() {
		return getYAvgChannel().isValid();
	}
    
    
	/** determine whether all channels are valid for this BPM */
	public boolean allChannelsValid() {
		return isXAvgChannelValid() && isYAvgChannelValid();
	}
    
	
	/** get the running average beam position */
	public double[] getRunningAverageBeamPosition() {
		return new double[] { getHorizontalRunningAverageBeamPosition(), getVerticalRunningAverageBeamPosition() };
	}
	
	
	/** get the running average beam position */
	public double[] getRunningAverageBeamPositionError() {
		return new double[] { getHorizontalRunningAverageBeamPositionError(), getVerticalRunningAverageBeamPositionError() };
	}
	
	
	/** get the running average horizontal beam position */
	public double getHorizontalRunningAverageBeamPosition() {
		return X_AVERAGING_CHANNEL_MONITOR.getRunningAverage();
	}
	
	
	/** get the running average horizontal beam position */
	public double getHorizontalRunningAverageBeamPositionError() {
		return X_AVERAGING_CHANNEL_MONITOR.getRunningStandardDeviationOfMean();
	}
	
	
	/** get the running average horizontal beam position */
	public double getVerticalRunningAverageBeamPosition() {
		return Y_AVERAGING_CHANNEL_MONITOR.getRunningAverage();
	}
	
	
	/** get the running average horizontal beam position */
	public double getVerticalRunningAverageBeamPositionError() {
		return Y_AVERAGING_CHANNEL_MONITOR.getRunningStandardDeviationOfMean();
	}
	
    
    /** Test if the BPM node is okay */
    public boolean isConnected() {
		return bpmNode.getChannel( BPM.X_AVG_HANDLE ).isConnected() && bpmNode.getChannel( BPM.Y_AVG_HANDLE ).isConnected();
    }
    
    /** Test if the BPM node is okay */
    public boolean isOkay() {
        return isOkay(bpmNode);
    }
    
    /** Test whether the given BPM node has a good status and all its channels can connect */
    static public boolean isOkay(BPM bpm) {
        return bpm.getStatus();
    }
    
    
    /** Identify the bpm agent by its BPM name */
    public String toString() {
        return name();
    }
    
    
    /** Return a list of BPM nodes associated with the list of bpmAgents */
    static public List<BPM> getNodes(List<BpmAgent> bpmAgents) {
        int count = bpmAgents.size();
        List<BPM> bpmNodes = new ArrayList<BPM>(count);
        
        for ( int index = 0 ; index < count ; index++ ) {
            BpmAgent bpmAgent = bpmAgents.get(index);
            bpmNodes.add( bpmAgent.getNode() );
        }
        return bpmNodes;
    }
    
	
    /** Get the average horizontal position reading from the last turn **/
    public double getXAvg(){
		double xavg=0.0;
		try{
			xavg = bpmNode.getXAvg();
		}
		catch(ConnectionException e){
			System.out.println("Could not connect with:" + bpmNode.getId());
		}
		catch(GetException e){
			System.out.println("Could not get value for:" + bpmNode.getId());
		}
		
		return xavg;
    }
	
	
    /** Get the average vertical position reading from the last turn **/
    public double getYAvg(){
		double yavg=0.0;
		try{
			yavg = bpmNode.getYAvg();
		}
		catch(ConnectionException e){
			System.out.println("Could not connect with:" + bpmNode.getId());
		}
		catch(GetException e){
			System.out.println("Could not get value from:" + bpmNode.getId());
		}
		return yavg;
    }
	
	
	/** event indicating that the double value has changed */
	public void valueChanged( final AveragingChannelMonitor source, final Channel channel, final double value ) {
		if ( source == X_AVERAGING_CHANNEL_MONITOR ) {
			EVENT_PROXY.xRunningAverageValueChanged( this, value );
		}
		else if ( source == Y_AVERAGING_CHANNEL_MONITOR ) {
			EVENT_PROXY.yRunningAverageValueChanged( this, value );
		}
	}
}



/** monitors a channel and maintains a running average of the monitored values */
class AveragingChannelMonitor implements ConnectionListener, IEventSinkValue {
	/** keeps the running statistics on channel values */
	final protected RunningWeightedStatistics VALUE_STATS;
	
	/** event proxy which forwards value change events to registered listeners */
	final protected AveragingChannelDoubleValueChangeListener VALUE_EVENT_LISTENER;
	
	/** channel to monitor */
	final protected Channel CHANNEL;
	
	/** monitor */
	protected Monitor MONITOR;
	
	/** indicates whether update the running average */
	volatile protected boolean _updateStatistics;
	
	
	/** constructor */
	public AveragingChannelMonitor( final Channel channel, final AveragingChannelDoubleValueChangeListener listener ) {
		VALUE_EVENT_LISTENER = listener;
		VALUE_STATS = new RunningWeightedStatistics( 0.2 );
		
		_updateStatistics = false;
        
		CHANNEL = channel;
		CHANNEL.addConnectionListener( this );
        
		// connect to the channel if it is marked valid; otherwise, ignore the values coming from the channel
		if ( channel.isValid() ) {
			CHANNEL.requestConnection();
			Channel.flushIO();
		}
	}
	
	
	/** determine if the the monitor has any samples */
	public boolean hasRunningAverageSamples() {
		return VALUE_STATS.population() > 0;
	}
	
	
	/** get the running average */
	public double getRunningAverage() {
		return VALUE_STATS.population() > 0 ? VALUE_STATS.mean() : Double.NaN;
	}
	
	
	/** get the running standard deviation of the mean */
	public double getRunningStandardDeviationOfMean() {
		return VALUE_STATS.population() > 0 ? VALUE_STATS.sampleStandardDeviationOfMean() : 0.0;
	}
	
	
	/** get the channel */
	public Channel getChannel() {
		return CHANNEL;
	}
	
	
	/** post the value change event */
	protected void postValueChangeEvent() {
		VALUE_EVENT_LISTENER.valueChanged( this, CHANNEL, VALUE_STATS.mean() );
	}
	
	
	/** clear the running statistics */
	public void clearStatistics() {
		VALUE_STATS.clear();
		postValueChangeEvent();
	}
	
	
	/** start averaging */
	public void startAveraging() {
		clearStatistics();
		_updateStatistics = true;
	}
	
	
	/** stop averaging */
	public void stopAveraging() {
		_updateStatistics = false;
	}
    
	
    /** Indicates that a connection to the specified channel has been established. */
    public void connectionMade( final Channel channel ) {
		if ( MONITOR == null ) {
			try {
				MONITOR = CHANNEL.addMonitorValue( this, Monitor.VALUE );
				Channel.flushIO();
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}
		}
	}
    
	
    /** Indicates that a connection to the specified channel has been dropped. */
    public void connectionDropped( final Channel channel ) {}
	
	
	/** handler monitor events */
    public void eventValue( final ChannelRecord record, final Channel channel ) {
		if ( _updateStatistics ) {
			VALUE_STATS.addSample( record.doubleValue() );
			postValueChangeEvent();
		}
	}
}



/** listener for changes in a double value */
interface AveragingChannelDoubleValueChangeListener {
	/** event indicating that the double value has changed */
	public void valueChanged( final AveragingChannelMonitor source, final Channel channel, final double value );
}

