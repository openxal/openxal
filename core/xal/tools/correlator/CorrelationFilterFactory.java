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
    
    static public CorrelationFilter defaultFilter() {
        return maxMissingFilter(0);
    }
    
    
    /** accept correlations with no more than maxMissing records */
    static public CorrelationFilter maxMissingFilter(final int maxMissing) {
        return new CorrelationFilter() {
            public boolean accept(Correlation correlation, int fullCount) {
                return correlation.numRecords() >= fullCount - maxMissing;
            }
        };
    }
    
    
    /** accept correlations with at least minCount records */
    static public CorrelationFilter minCountFilter(final int minCount) {
        return new CorrelationFilter() {
            public boolean accept(Correlation correlation, int fullCount) {
                return correlation.numRecords() >= minCount;
            }
        };
    }
    
    
    /** 
     * Convert a record filter to a correlation filter.
     * This is useful when stacking correlators and a correlation of one  
     * correlator is the record of another.
     */
    static public CorrelationFilter correlationFilter(final RecordFilter recordFilter) {
        return new CorrelationFilter() {
            public boolean accept(Correlation correlation, int fullCount) {
                return recordFilter.accept(correlation);
            }
        };
    }
    
    
    /** Creates a new instance of CorrelationFilterFactor */
    protected CorrelationFilterFactory() {
    }
}
