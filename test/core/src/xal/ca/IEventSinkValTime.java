/*
 * IEventSinkVal_TimeDbl.java
 *
 * Created on June 26, 2002, 5:27 PM
 */

package xal.ca;

/**
 *
 * @author  tap
*/
public interface IEventSinkValTime {
    public void eventValue(ChannelTimeRecord record, Channel chan);
}
