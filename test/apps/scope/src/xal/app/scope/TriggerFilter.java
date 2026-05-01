/*
 * TriggerFilter.java
 *
 * Created on June 3, 2003, 9:12 AM
 */

package xal.app.scope;

import xal.ca.ChannelTimeRecord;
import xal.tools.correlator.RecordFilter;
import xal.tools.data.*;

/**
 * TriggerFilter wraps a record filter which it generates based on the parameters
 * provided to it.
 *
 * @author  tap
 */
abstract public class TriggerFilter implements DataListener {
	/** data label to key the data adaptor */
    final public static String dataLabel = "Trigger_Filter";

	/** record filter */
    protected RecordFilter<ChannelTimeRecord> recordFilter;

	/** parameters */
    protected Parameter[] parameters;

	
    /** Constructor */
    public TriggerFilter() {}
    
    
    /** 
     *  dataLabel() provides the name used to identify the class in an 
     *  external data source.
     */
    public String dataLabel() {
        return dataLabel;
    }

    
    /**
     *  Instructs the receiver to update its data based on the given adaptor.
     */
    abstract public void update(DataAdaptor adaptor);
    
    
    /**
     *  Instructs the receiver to write its data to the adaptor for external
     *  storage.
     */
    public void write(DataAdaptor adaptor) {
        adaptor.setValue("type", getLabel());
    }
    
    
    /**
     * Subclasses must override this method to provide a unique label indicating
     * which kind of filter it is.
     * @return Label identifying the TriggerFilter subclass.
     */
    abstract public String getLabel();
    
    
    /**
     * Updates the filter based on the parameters set.  Basically it provides 
     * a new record filter.
     */
    abstract public void updateFilter();
    
    
    /**
     * Get the record filter associated with this trigger filter.
     * @return the record filter
     */
    public RecordFilter<ChannelTimeRecord> getRecordFilter() {
        return recordFilter;
    }
    
    
    /**
     * Get the parameters used to create the record filter.
     * @return The filter parameters.
     */
    public Parameter[] getParameters() {
        return parameters;
    }
    
    
    /**
     * Get the named parameter.
     * @param tag The tag identifying the parameter by its label.
     * @return The parameter associated with the specified tag.
     */
    public Parameter getParameter(String tag) {
        for ( int index = 0 ; index < parameters.length ; index++ ) {
            if ( parameters[index].getLabel().equals(tag) )  return parameters[index];
        }
        
        return null;
    }
}
