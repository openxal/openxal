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


	/** 
	 * Get the units 
	 * @return units
	 */
	abstract public String getUnits();


	/** 
	 * Set the units
	 * @param units to apply
	 */
	abstract public void setUnits( final String units );


	/** 
	 * Get the precision 
	 * @return precision
	 */
	abstract public short getPrecision();


	/** 
	 * Set the precision 
	 * @param precision to apply
	 */
	abstract public void setPrecision( final short precision );


	/** 
	 * Get the lower display limit 
	 * @return lower display limit
	 */
	abstract public Number getLowerDispLimit();


	/** 
	 * Set the lower display limit 
	 * @param lowerLimit to apply
	 */
	abstract public void setLowerDispLimit( final Number lowerLimit );


	/** 
	 * Get the upper display limit 
	 * @return upper display limit
	 */
	abstract public Number getUpperDispLimit();


	/** 
	 * Set the upper display limit 
	 * @param upperLimit to apply
	 */
	abstract public void setUpperDispLimit( final Number upperLimit );


	/** 
	 * Get the lower alarm limit 
	 * @return lower alarm limit
	 */
	abstract public Number getLowerAlarmLimit();


	/** 
	 * Set the lower alarm limit 
	 * @param lowerLimit to apply
	 */
	abstract public void setLowerAlarmLimit( final Number lowerLimit );


	/** 
	 * Get the upper alarm limit 
	 * @return upper alarm limit
	 */
	abstract public Number getUpperAlarmLimit();


	/** 
	 * Set the upper alarm limit 
	 * @param upperLimit to apply
	 */
	abstract public void setUpperAlarmLimit( final Number upperLimit );


	/** 
	 * Get the lower control limit 
	 * @return lower control limit
	 */
	abstract public Number getLowerCtrlLimit();


	/** 
	 * Set the lower control limit 
	 * @param lowerLimit to apply
	 */
	abstract public void setLowerCtrlLimit( final Number lowerLimit );


	/** 
	 * Get the upper control limit 
	 * @return upper control limit
	 */
	abstract public Number getUpperCtrlLimit();


	/** 
	 * Set the upper control limit 
	 * @param upperLimit to apply
	 */
	abstract public void setUpperCtrlLimit( final Number upperLimit );


	/** 
	 * Get the lower warning limit 
	 * @return lower warning limit
	 */
	abstract public Number getLowerWarningLimit();


	/** 
	 * Set the lower warning limit 
	 * @param lowerLimit to apply
	 */
	abstract public void setLowerWarningLimit( final Number lowerLimit );


	/** 
	 * Get the upper warning limit 
	 * @return upper warning limit
	 */
	abstract public Number getUpperWarningLimit();


	/** 
	 * Set the upper warning limit 
	 * @param upperLimit to apply
	 */
	abstract public void setUpperWarningLimit( final Number upperLimit );
}
