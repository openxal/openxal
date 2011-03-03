/*
 *  RecordFilters.java
 *
 *  Created on July 23, 2002, 3:02 PM
 */
package xal.ca.correlator;

import xal.tools.correlator.RecordFilter;
import xal.ca.ChannelTimeRecord;

/**
 * Factory for common <code>ChannelRecordFilter</code> instances.
 *
 * @author   tap
 */
public class RecordFilterFactory {

	/** Creates a new instance of RecordFilters */
	protected RecordFilterFactory() { }


	/**
	 * Generate a filter to filter records by checking that the specified filter is not fulfilled.
	 *
	 * @param filter  The filter to negate
	 * @return        a new filter which is opposite the specified filter
	 */
	public static RecordFilter notFilter( final RecordFilter filter ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					return !filter.accept( rawRecord );
				}
			};
	}


	/**
	 * Generate a filter to Filter records checking for equality to the specified value.
	 *
	 * @param target  the target value against which to check for equality
	 * @return        a new equality filter
	 */
	public static RecordFilter equalityDoubleFilter( final double target ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					final ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					return record.doubleValue() == target;
				}
			};
	}


	/**
	 * Filter records with a lower limit on the value
	 *
	 * @param minValue  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public static RecordFilter minDoubleFilter( final double minValue ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					return record.doubleValue() >= minValue;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the value
	 *
	 * @param maxValue  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public static RecordFilter maxDoubleFilter( final double maxValue ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					return record.doubleValue() <= maxValue;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the value
	 *
	 * @param minValue  Description of the Parameter
	 * @param maxValue  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public static RecordFilter rangeDoubleFilter( final double minValue, final double maxValue ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					double value = record.doubleValue();
					return value >= minValue && value <= maxValue;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the status
	 *
	 * @param maxStatus  Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public static RecordFilter maxStatusFilter( final int maxStatus ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					int status = record.status();
					return status <= maxStatus;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the severity
	 *
	 * @param maxSeverity  Description of the Parameter
	 * @return             Description of the Return Value
	 */
	public static RecordFilter maxSeverityFilter( final int maxSeverity ) {
		return
			new RecordFilter() {
				public boolean accept( final Object rawRecord ) {
					ChannelTimeRecord record = (ChannelTimeRecord)rawRecord;
					int severity = record.severity();
					return severity <= maxSeverity;
				}
			};
	}
}

