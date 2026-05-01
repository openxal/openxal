/**
 * This class extends the GenericRecord class to accomodate the storage of 
 * Channel objects (one for setpoint - and one for readbacks).
 *
 * @version   1.0
 * @author    J. Galambos
 */

package xal.app.score;

import java.util.*;

import xal.ca.*;
import xal.tools.data.*;
import xal.tools.text.FormattedNumber;

public class ScoreRecord extends GenericRecord {
    /** the setpoint Channel */
    private ChannelWrapper _setPointChannel;

    /** the readback Channel */
    private ChannelWrapper _readbackChannel;
	
	
    /** the constuctor */
    public ScoreRecord( final DataTable dataTable ) { 
		super( dataTable );
		_readbackChannel = null;
		_setPointChannel = null;
    }
	
	
	/** get the data adaptor associated with this record's data type */
	public DataTypeAdaptor getDataTypeAdaptor() {
		final String dataType = stringValueForKey( PVData.DATA_TYPE_KEY );
		return DataTypeAdaptor.adaptorForType( dataType );
	}
	
	
	/** get the PV Type */
	public String getSignalType() {
		return stringValueForKey( PVData.typeKey );
	}
	
	
	/** get the system */
	public String getSystem() {
		return stringValueForKey( PVData.systemKey );
	}
	
	
	/** get the readback signal */
	public String getReadbackPV() {
		final String signal = stringValueForKey( PVData.rbNameKey );
		return _readbackChannel != null ? signal : "";
	}
	
	
	/** get the setpoint signal */
	public String getSetpointPV() {
		final String signal = stringValueForKey( PVData.spNameKey );
		return _setPointChannel != null ? signal : "";
	}
	
	
	/** get the saved setpoint value */
	public Object getSavepointValue() {
		return getSavedSetpointValue();
	}
	
	
	/** get the saved setpoint value */
	public Object getSavedSetpointValue() {
		final String stringValue = stringValueForKey( PVData.spSavedValKey );
		return getDataTypeAdaptor().parse( stringValue );
	}
	
	
	/** get the saved setpoint value */
	public String getSavedSetpointAsString() {
		return _setPointChannel != null ? stringValueForKey( PVData.spSavedValKey ) : "";
	}
	
	
	/** get the saved readback value */
	public Object getReadbackValue() {
		return getSavedReadbackValue();
	}
	
	
	/** get the saved readback value */
	public Object getSavedReadbackValue() {
		final String stringValue = stringValueForKey( PVData.rbSavedValKey );
		return getDataTypeAdaptor().parse( stringValue );
	}
	
	
	/** get the saved readback value */
	public String getSavedReadbackAsString() {
		return _readbackChannel != null ? stringValueForKey( PVData.rbSavedValKey ) : "";
	}
	
	
    /** set the sp channel */
    public void setSPChannel( ChannelWrapper channel ) { _setPointChannel = channel; }
	
	
    /** get the sp channel */
    public ChannelWrapper getSPChannel() { return _setPointChannel; }
	
	
	/** Get the latest live setpoint value */
	public Object getLiveSetPointValue() {
		return getDataTypeAdaptor().getValue( _setPointChannel );
	}
	
	
	/** Get the latest live setpoint value */
	public String getLiveSetPointAsString() {
		return _setPointChannel != null ? getDataTypeAdaptor().asString( getLiveSetPointValue() ) : "";
	}
	
	
    /** set the rb channel */
    public void setRBChannel( ChannelWrapper channel ) { _readbackChannel = channel; }
	
	
    /** get the rb channel */
    public ChannelWrapper getRBChannel() { return _readbackChannel; }
	
	
	/** Get the latest live readback value */
    public Object getLiveReadbackValue() {
		return getDataTypeAdaptor().getValue( _readbackChannel );
	}
	
	
	/** Get the latest live readback value as a string */
    public String getLiveReadbackAsString() {
		return _readbackChannel != null ? getDataTypeAdaptor().asString( getLiveReadbackValue() ) : "";
	}
	
	
	/** Determine whether the setpoint (live versus saved) is within tolerance */
	public boolean isSetpointWithinTolerance( final double tolerance ) {
		return getDataTypeAdaptor().isWithinTolerance( getLiveSetPointValue(), getSavedSetpointValue(), tolerance );
	}
	
	
	/** Determine whether the readback (live versus saved and setpoint) is within tolerance */
	public boolean isReadbackWithinTolerance( final double tolerance ) {
		final DataTypeAdaptor dataTypeAdaptor = getDataTypeAdaptor();
		final Object readback = getLiveReadbackValue();
		return ( _setPointChannel == null || dataTypeAdaptor.isWithinTolerance( readback, getLiveSetPointValue(), tolerance ) ) && dataTypeAdaptor.isWithinTolerance( readback, getSavedReadbackValue(), tolerance );
	}
	
	
	/** get the relative error (as percent) between the live setpoint value and the saved setpoint value */
	public double getSetpointRelativeError() {
		if ( _setPointChannel != null ) {
			final DataTypeAdaptor dataTypeAdaptor = getDataTypeAdaptor();
			final Object savedSetPoint = getSavedSetpointValue();
			final Object liveSetPoint = getLiveSetPointValue();
			return 100.0 * dataTypeAdaptor.getRelativeError( liveSetPoint, savedSetPoint );
		}
		else {
			return 0.0;
		}
	}
	
	
	/** get the formatted relative error (as percent) between the live setpoint value and the saved setpoint value */
	public Number getFormattedSetpointRelativeError() {
		return new FormattedNumber( "0.000", getSetpointRelativeError() );
	}
	
	
	/** 
	 * Determine the relative error (as percent) between the live readback value and the live setpoint value and between the saved readback and the live readback.
	 * @return the maximum of the relative error between the live readback and either the live setpoint or the saved readback
	 */
	public double getReadbackRelativeError() {
		if ( _readbackChannel != null ) {
			final DataTypeAdaptor dataTypeAdaptor = getDataTypeAdaptor();
			final Object savedReadback = getSavedReadbackValue();
			final Object liveReadback = getLiveReadbackValue();
			final Object liveSetPoint = getLiveSetPointValue();
			final double liveError = dataTypeAdaptor.getRelativeError( liveReadback, savedReadback );
			final double settingError = _setPointChannel != null ? dataTypeAdaptor.getRelativeError( liveReadback, liveSetPoint ) : 0.0;
			return 100.0 * Math.max( liveError, settingError );
		}
		else {
			return 0.0;
		}
	}
	
	
	/** get the formatted relative error (as percent) between the live setpoint value and the saved setpoint value */
	public Number getFormattedReadbackRelativeError() {
		return new FormattedNumber( "0.000", getReadbackRelativeError() );
	}
}
