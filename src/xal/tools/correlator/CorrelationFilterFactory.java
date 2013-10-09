/*
 * CorrelationFilterFactor.java
 *
 * Created on August 1, 2002, 4:54 PM
 */

package xal.tools.correlator;

/**
 *
 * @author  tap
 */
public class CorrelationFilterFactory {
    
    static public <RecordType> CorrelationFilter<RecordType> defaultFilter() {
        return maxMissingFilter( 0 );
    }
    
    
    /** accept correlations with no more than maxMissing records */
    static public <RecordType> CorrelationFilter<RecordType> maxMissingFilter( final int maxMissing ) {
        return new CorrelationFilter<RecordType>() {
            public boolean accept( final Correlation<RecordType> correlation, final int fullCount ) {
                return correlation.numRecords() >= fullCount - maxMissing;
            }
        };
    }
    
    
    /** accept correlations with at least minCount records */
    static public <RecordType> CorrelationFilter<RecordType> minCountFilter( final int minCount ) {
        return new CorrelationFilter<RecordType>() {
            public boolean accept( final Correlation<RecordType> correlation, final int fullCount ) {
                return correlation.numRecords() >= minCount;
            }
        };
    }
    
    
    /** 
     * Convert a record filter to a correlation filter. This is useful when stacking correlators and a correlation of one  correlator is the record of another.
     */
    static public <RecordType> CorrelationFilter<RecordType> correlationFilter( final RecordFilter<Correlation<RecordType>> recordFilter ) {
        return new CorrelationFilter<RecordType>() {
            public boolean accept( final Correlation<RecordType> correlation, final int fullCount ) {
                return recordFilter.accept( correlation );
            }
        };
    }
    
    
    /** Creates a new instance of CorrelationFilterFactor */
    protected CorrelationFilterFactory() {
    }
}
