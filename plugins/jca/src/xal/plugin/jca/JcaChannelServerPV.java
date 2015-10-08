/*
 * JcaChannelServerPV.java
 *
 * Created on October 21, 2013, 9:37 AM
 */

package xal.plugin.jca;

import xal.ca.ChannelServerPV;

import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;


/**
 * Concrete JcaChannelServerPV wraps MemoryProcessVariable
 * @author  tap
 */
public class JcaChannelServerPV extends ChannelServerPV {
	/** native process variable wrapped by this instance */
	final private MemoryProcessVariable NATIVE_PROCESS_VARIABLE;


	/** Constructor */
	protected JcaChannelServerPV( final MemoryProcessVariable nativeProcessVariable ) {
		NATIVE_PROCESS_VARIABLE = nativeProcessVariable;
	}


	/** get the units */
	public String getUnits() {
		return NATIVE_PROCESS_VARIABLE.getUnits();
	}


	/** set the units */
	public void setUnits( final String units ) {
		NATIVE_PROCESS_VARIABLE.setUnits( units );
	}


	/** get the precision */
	public short getPrecision() {
		return NATIVE_PROCESS_VARIABLE.getPrecision();
	}


	/** set the precision */
	public void setPrecision( final short precision ) {
		NATIVE_PROCESS_VARIABLE.setPrecision( precision );
	}


	/** get the lower display limit */
	public Number getLowerDispLimit() {
		return NATIVE_PROCESS_VARIABLE.getLowerDispLimit();
	}


	/** set the lower display limit */
	public void setLowerDispLimit( final Number lowerLimit ) {
		NATIVE_PROCESS_VARIABLE.setLowerDispLimit( lowerLimit );
	}


	/** get the upper display limit */
	public Number getUpperDispLimit() {
		return NATIVE_PROCESS_VARIABLE.getUpperDispLimit();
	}


	/** set the upper display limit */
	public void setUpperDispLimit( final Number upperLimit ) {
		NATIVE_PROCESS_VARIABLE.setUpperDispLimit( upperLimit );
	}


	/** get the lower alarm limit */
	public Number getLowerAlarmLimit() {
		return NATIVE_PROCESS_VARIABLE.getLowerAlarmLimit();
	}


	/** set the lower alarm limit */
	public void setLowerAlarmLimit( final Number lowerLimit ) {
		NATIVE_PROCESS_VARIABLE.setLowerAlarmLimit( lowerLimit );
	}


	/** get the upper alarm limit */
	public Number getUpperAlarmLimit() {
		return NATIVE_PROCESS_VARIABLE.getUpperAlarmLimit();
	}


	/** set the upper alarm limit */
	public void setUpperAlarmLimit( final Number upperLimit ) {
		NATIVE_PROCESS_VARIABLE.setUpperAlarmLimit( upperLimit );
	}


	/** get the lower control limit */
	public Number getLowerCtrlLimit() {
		return NATIVE_PROCESS_VARIABLE.getLowerCtrlLimit();
	}


	/** set the lower control limit */
	public void setLowerCtrlLimit( final Number lowerLimit ) {
		NATIVE_PROCESS_VARIABLE.setLowerCtrlLimit( lowerLimit );
	}


	/** get the upper control limit */
	public Number getUpperCtrlLimit() {
		return NATIVE_PROCESS_VARIABLE.getUpperCtrlLimit();
	}


	/** set the upper control limit */
	public void setUpperCtrlLimit( final Number upperLimit ) {
		NATIVE_PROCESS_VARIABLE.setUpperCtrlLimit( upperLimit );
	}


	/** get the lower warning limit */
	public Number getLowerWarningLimit() {
		return NATIVE_PROCESS_VARIABLE.getLowerWarningLimit();
	}


	/** set the lower warning limit */
	public void setLowerWarningLimit( final Number lowerLimit ) {
		NATIVE_PROCESS_VARIABLE.setLowerWarningLimit( lowerLimit );
	}


	/** get the upper warning limit */
	public Number getUpperWarningLimit() {
		return NATIVE_PROCESS_VARIABLE.getUpperWarningLimit();
	}


	/** set the upper warning limit */
	public void setUpperWarningLimit( final Number upperLimit ) {
		NATIVE_PROCESS_VARIABLE.setUpperWarningLimit( upperLimit );
	}
}
