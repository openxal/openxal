/*
 * ChannelModelListener.java
 *
 * Created on January 23, 2003, 4:25 PM
 */

package xal.app.scope;

import xal.ca.*;

/**
 * Interface that specifies events posted by a ChannelModel instance.
 *
 * @author  tap
 */
public interface ChannelModelListener {
    /**
     * Event indicating that the specified channel is being enabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being enabled.
     */
    public void enableChannel(ChannelModel source, Channel channel);
    
    
    /**
     * Event indicating that the specified channel is being disabled.
     * @param source ChannelModel posting the event.
     * @param channel The channel being disabled.
     */
    public void disableChannel(ChannelModel source, Channel channel);
    
    
    /**
     * Event indicating that the channel model has a new channel.
     * @param source ChannelModel posting the event.
     * @param channel The new channel.
     */
    public void channelChanged(ChannelModel source, Channel channel);
    
    
    /**
     * Event indicating that the channel model has a new array of element times.
     * @param source ChannelModel posting the event.
     * @param elementTimes The new element times array measured in turns.
     */
    public void elementTimesChanged(ChannelModel source, final double[] elementTimes);
}
