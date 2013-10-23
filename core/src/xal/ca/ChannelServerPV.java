/*
 * ChannelServerPV.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.ca;


/**
 * Abstract ChannelServerPV.
 * @author  tap
 */
abstract public class ChannelServerPV {
	/** Constructor */
	protected ChannelServerPV() {}


	/** get the units */
	abstract public String getUnits();


	/** set the units */
	abstract public void setUnits( final String units );


	/** get the precision */
	abstract public short getPrecision();


	/** set the precision */
	abstract public void setPrecision( final short precision );


	/** get the lower display limit */
	abstract public Number getLowerDispLimit();


	/** set the lower display limit */
	abstract public void setLowerDispLimit( final Number lowerLimit );


	/** get the upper display limit */
	abstract public Number getUpperDispLimit();


	/** set the upper display limit */
	abstract public void setUpperDispLimit( final Number upperLimit );


	/** get the lower alarm limit */
	abstract public Number getLowerAlarmLimit();


	/** set the lower alarm limit */
	abstract public void setLowerAlarmLimit( final Number lowerLimit );


	/** get the upper alarm limit */
	abstract public Number getUpperAlarmLimit();


	/** set the upper alarm limit */
	abstract public void setUpperAlarmLimit( final Number upperLimit );


	/** get the lower control limit */
	abstract public Number getLowerCtrlLimit();


	/** set the lower control limit */
	abstract public void setLowerCtrlLimit( final Number lowerLimit );


	/** get the upper control limit */
	abstract public Number getUpperCtrlLimit();


	/** set the upper control limit */
	abstract public void setUpperCtrlLimit( final Number upperLimit );


	/** get the lower warning limit */
	abstract public Number getLowerWarningLimit();


	/** set the lower warning limit */
	abstract public void setLowerWarningLimit( final Number lowerLimit );


	/** get the upper warning limit */
	abstract public Number getUpperWarningLimit();


	/** set the upper warning limit */
	abstract public void setUpperWarningLimit( final Number upperLimit );
}
