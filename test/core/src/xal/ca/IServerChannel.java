/*
 * IServerChannel.java
 *
 * Created July, 2015
 */

package xal.ca;


/**
 * Interface for a server channel.
 * @author  Ivo
 */
public interface IServerChannel {
	/** 
	 * Get the units 
	 * @return units
	 */
	String getUnits();


	/** 
	 * Set the units
	 * @param units to apply
	 */
	void setUnits( final String units );

	/** 
	 * Set the lower display limit 
	 * @param lowerLimit to apply
	 */
	void setLowerDispLimit( final Number lowerLimit );

	/** 
	 * Set the upper display limit 
	 * @param upperLimit to apply
	 */
	void setUpperDispLimit( final Number upperLimit );

	/** 
	 * Set the lower alarm limit 
	 * @param lowerLimit to apply
	 */
	void setLowerAlarmLimit( final Number lowerLimit );

	/** 
	 * Set the upper alarm limit 
	 * @param upperLimit to apply
	 */
	void setUpperAlarmLimit( final Number upperLimit );

	/** 
	 * Set the lower control limit 
	 * @param lowerLimit to apply
	 */
	void setLowerCtrlLimit( final Number lowerLimit );

	/** 
	 * Set the upper control limit 
	 * @param upperLimit to apply
	 */
	void setUpperCtrlLimit( final Number upperLimit );

	/** 
	 * Set the lower warning limit 
	 * @param lowerLimit to apply
	 */
	void setLowerWarningLimit( final Number lowerLimit );

	/** 
	 * Set the upper warning limit 
	 * @param upperLimit to apply
	 */
	void setUpperWarningLimit( final Number upperLimit );
	
	/** 
	 * Set wheather the value of this channel can be changed by the clients.
	 * @param settable true if it can be changed
	 */
	void setSettable( final boolean settable );
}
