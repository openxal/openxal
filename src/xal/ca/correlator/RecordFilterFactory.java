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
	 * @param filter  The filter to negate
	 * @return        a new filter which is opposite the specified filter
	 */
	public static RecordFilter<ChannelTimeRecord> notFilter( final RecordFilter<ChannelTimeRecord> filter ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord rawRecord ) {
					return !filter.accept( rawRecord );
				}
			};
	}
	
	
	/**
	 * Generate a filter to Filter records checking for equality to the specified value.
	 * @param target  the target value against which to check for equality
	 * @return        a new equality filter
	 */
	public static RecordFilter<ChannelTimeRecord> equalityDoubleFilter( final double target ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					return record.doubleValue() == target;
				}
			};
	}
	
	
	/**
	 * Filter records with a lower inclusive limit on the value
	 * @param minValue  lower limit
	 * @return          filter for records whose value is greater than or equal to the lower limit
	 */
	public static RecordFilter<ChannelTimeRecord> minDoubleFilter( final double minValue ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					return record.doubleValue() >= minValue;
				}
			};
	}
	
	
	/**
	 * Filter records with a lower exclusive limit on the value
	 * @param minValue  lower limit
	 * @return          filter for records whose value is strictly greater than the lower limit
	 */
	public static RecordFilter<ChannelTimeRecord> exlusiveMinDoubleFilter( final double minValue ) {
		return
		new RecordFilter<ChannelTimeRecord>() {
			public boolean accept( final ChannelTimeRecord record ) {
				return record.doubleValue() > minValue;
			}
		};
	}
	
	
	/**
	 * Filter records with an upper inclusive limit on the value
	 * @param maxValue  upper limit
	 * @return          filter for records whose value is less than or equal to the upper limit
	 */
	public static RecordFilter<ChannelTimeRecord> maxDoubleFilter( final double maxValue ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					return record.doubleValue() <= maxValue;
				}
			};
	}
	
	
	/**
	 * Filter records with an upper exclusive limit on the value
	 * @param maxValue  upper limit
	 * @return          filter for records whose value is strictly less than the upper limit
	 */
	public static RecordFilter<ChannelTimeRecord> exclusiveMaxDoubleFilter( final double maxValue ) {
		return
		new RecordFilter<ChannelTimeRecord>() {
			public boolean accept( final ChannelTimeRecord record ) {
				return record.doubleValue() < maxValue;
			}
		};
	}
	
	
	/**
	 * Filter records with an upper limit on the value
	 * @param minValue  Description of the Parameter
	 * @param maxValue  Description of the Parameter
	 * @return          Description of the Return Value
	 */
	public static RecordFilter<ChannelTimeRecord> rangeDoubleFilter( final double minValue, final double maxValue ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					double value = record.doubleValue();
					return value >= minValue && value <= maxValue;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the status
	 * @param maxStatus  Description of the Parameter
	 * @return           Description of the Return Value
	 */
	public static RecordFilter<ChannelTimeRecord> maxStatusFilter( final int maxStatus ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					int status = record.status();
					return status <= maxStatus;
				}
			};
	}


	/**
	 * Filter records with an upper limit on the severity
	 * @param maxSeverity  Description of the Parameter
	 * @return             Description of the Return Value
	 */
	public static RecordFilter<ChannelTimeRecord> maxSeverityFilter( final int maxSeverity ) {
		return
			new RecordFilter<ChannelTimeRecord>() {
				public boolean accept( final ChannelTimeRecord record ) {
					int severity = record.severity();
					return severity <= maxSeverity;
				}
			};
	}
}

