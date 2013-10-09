/*
 * ChannelSetException.java
 *
 * Created on May 23, 2003, 11:01 AM
 */

package xal.app.scope;

import xal.ca.Channel;

/**
 * ChannelSetException is an exception that occurs during a failed attempt to 
 * set a channel in a ChannelModel.  The exception gets thrown if the
 * waveform channel, time delay channel or sample period channel cannot connect.
 * The exception stores information about the cause including the name of the waveform 
 * affected, which kind of channel couldn't connect (waveform, delay or sample period)
 * and the channel that could not connect.
 *
 * @author  tap
 */
public class ChannelSetException extends RuntimeException {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // Type constants
    final static public int WAVEFORM_CHANNEL = 0;
    final static public int TIME_DELAY_CHANNEL = 1;
    final static public int SAMPLE_PERIOD_CHANNEL = 2;
    final static public int TRIGGER_CHANNEL = 3;
    
    // instance variables
    protected String waveformName;
    protected int type;
    protected Channel channel;
    
    /** Creates a new instance of ChannelSetException */
    public ChannelSetException(String aWaveformName, int aType, Channel aChannel) {
        waveformName = aWaveformName;
        type = aType;
        channel = aChannel;
    }
    
    
    /**
     * Override getMessage() to return the message appropriate for this class.
     * The message returned is customized depending on the cause of the 
     * channel set exception: can't connect to the waveform channel, the time
     * delay channel or the sample period channel.
     * @return The message describing the exception.
     */
    public String getMessage() {
        String message = "Unknown Channel Set Exception!";
        switch(type) {
            case WAVEFORM_CHANNEL:
                message = "Cannot connect to the waveform channel: " + waveformName + "\n";
                message += "Please verify the name of the channel and be sure that \n";
                message += "your EPICS addresses and ports are properly configured.";
                break;
            case TIME_DELAY_CHANNEL:
                message = "Cannot connect to the waveform time delay channel: " + channel.channelName() + "\n";
                message += "associated with the waveform: " + waveformName + "\n";
                message += "This delay channel is essential for determining the time offset of the waveform.\n";
                message += "Please verify that the waveform you selected has a corresponding time delay channel \n";
                message += "and that your EPICS addresses and ports are properly configured.";
                break;
            case SAMPLE_PERIOD_CHANNEL:
                message = "Cannot connect to the waveform sample period channel: " + channel.channelName() + "\n";
                message += "associated with the waveform: " + waveformName + "\n";
                message += "This sample period channel is essential for determining the time width of each waveform element.\n";
                message += "Please verify that the waveform you selected has a corresponding sample period channel \n";
                message += "and that your EPICS addresses and ports are properly configured.";
                break;
            case TRIGGER_CHANNEL:
                message = "Cannot connect to the trigger channel: \"" + channel.channelName() + "\"\n";
                message += "Please verify the name of the channel and be sure that \n";
                message += "your EPICS addresses and ports are properly configured.";
                break;
        }
        
        return message;
    }
}
