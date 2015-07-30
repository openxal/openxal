//Example channel access
package xal.app.rocs;
import xal.ca.*;
import xal.tools.messaging.*;

public class ChannelAgent implements IEventSinkValue, ConnectionListener{
	
    protected Channel theChannel;
	private volatile ChannelRecord _lastRecord;
    
    protected MessageCenter messageCenter;
	
    protected ReadbackListener readbackProxy;
    
	
	/** Constructor */
    public ChannelAgent(Channel channel){
		messageCenter = new MessageCenter("Readback Message");
		
		_lastRecord = null;
		theChannel = channel;
		channel.addConnectionListener( this );
		
		readbackProxy=messageCenter.registerSource(this, ReadbackListener.class);	
		
		theChannel.requestConnection();
    }


	/* get the wrapped channel */
	public Channel getChannel() {
		return theChannel;
	}
	
	
	public void addReadbackListener(ReadbackListener listener) {
        messageCenter.registerTarget(listener, this, ReadbackListener.class);
		if ( _lastRecord != null && theChannel != null )  listener.updateReadback( this, theChannel.channelName(), _lastRecord.doubleValue() );
    }
	
    public void connectionMade(Channel aChannel) {
		makeMonitor(aChannel);
    }
	
    public void connectionDropped(Channel aChannel) {
		System.out.println("Channel dropped " + aChannel.channelName() );
    }
    
    public void addChannelConnectionListener(ConnectionListener listener){
		theChannel.addConnectionListener(listener);
    }
    
    public boolean isConnected(){
		return theChannel.isConnected(); 
    }
    
    public void setValue(double val){
		try {
			theChannel.putVal(val);
			Channel.flushIO();
		}
		catch (ConnectionException e){
			System.err.println("Unable to connect to channel access.");
		}
		catch (PutException e){
			System.err.println("Unable to set process variables.");
		}
		return;
    }


	/* test whether the specified value is within the channel's limits */
	public boolean isWithinLimits( final double value ) {
		return value > getMagLowLimit() && value < getMagUpLimit();
	}


	/* get a text description of the limits range suitable for output to user */
	public String getLimitsDescription() {
		return "(" + getMagLowLimit() + " to " + getMagUpLimit() + ")";
	}
    
	
    public double getMagLowLimit(){
        double limit = 0.0;
        try{
            limit = theChannel.lowerControlLimit().doubleValue();
        }
        catch (ConnectionException e){
            System.err.println("Unable to connect to channel access.");
        }   
        catch (GetException e){
            System.err.println("Unable to get process variables.");
        }	
        return limit;
    }
	
    public double getMagUpLimit(){
        double limit = 0.0;
		try{
			limit = theChannel.upperControlLimit().doubleValue();
		}
		catch (ConnectionException e){
			System.err.println("Unable to connect to channel access.");
		}   
		catch (GetException e){
			System.err.println("Unable to get process variables.");
		}
		return limit;
    }
    
    private void makeMonitor(Channel aChannel) {
		try {
		    aChannel.addMonitorValue(this, Monitor.VALUE);	
		}
		catch(ConnectionException exc) {
		    exc.printStackTrace();
		}
		catch(MonitorException exc) {
		    exc.printStackTrace();
		}
    }
    
    public void eventValue(ChannelRecord record, Channel chan){
	    readbackProxy.updateReadback(this, chan.channelName(), record.doubleValue());
		_lastRecord = record;
    }
    
}









