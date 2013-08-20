/*
 * TriggerFilterFactory.java
 *
 * Created on June 3, 2003, 10:09 AM
 */

package xal.app.scope;

import xal.ca.correlator.*;
import xal.tools.data.*;


/**
 * TriggerFilterFactory provides trigger filter instances.
 *
 * @author  tap
 */
public class TriggerFilterFactory {
    
    /** Creates a new instance of TriggerFilterFactory */
    protected TriggerFilterFactory() {}
    
    
    /**
     * Gets an array of names corresponding to the available trigger filters.
     * @return Array of trigger filter names.
     */
    static String[] triggerFilterTypes() {
        return new String[] {"None", "Min Double", "Max Double", "Range Double"};
    }
    
    
    /**
     * Creates an instance of the trigger filter corresponding to the specified label.
     * @return A new TriggerFilter corresponding to the specified label or null if none exists.
     */
    static TriggerFilter newTriggerFilter(String label) {
        if ( label.equals("None") ) {
            return null;
        }
        else if ( label.equals("Min Double") ) {
            return minDoubleFilter();
        }
        else if ( label.equals("Max Double") ) {
            return maxDoubleFilter();
        }
        else if ( label.equals("Range Double") ) {
            return rangeDoubleFilter();
        }
        else {
            return null;
        }
    }
    
    
    /**
     * Decode a trigger filter from a data adaptor.
     * @param adaptor The data adaptor to decode
     * @return the trigger filter decoded from the adaptor
     */
    static TriggerFilter decodeFilter(DataAdaptor adaptor) {
        String filterType = adaptor.stringValue("type");
        TriggerFilter filter = newTriggerFilter(filterType);
        if ( filter != null ) {
            filter.update(adaptor);
        }
        return filter;
    }
    
    
    /**
     * Creates a new trigger filter that accepts the monitored value if it is less 
     * than the value of the parameter associate with the "max" tag.
     * @return the trigger filter supporting the max filtering
     */
    static TriggerFilter maxDoubleFilter() {
        return new TriggerFilter() {
            { 
                parameters = new Parameter[] {new Parameter(Double.class, "max", new Double(0))};
                updateFilter();
            }
            
            public String getLabel() { return "Max Double"; }
            
            public void update(DataAdaptor adaptor) {
                double maxLimit = adaptor.doubleValue("max");
                getParameter("max").setValue( new Double(maxLimit) );
                updateFilter();
            }
            
            public void write(DataAdaptor adaptor) {
                super.write(adaptor);
                adaptor.setValue("max", getParameter("max").getValue().doubleValue());
            }
            
            public void updateFilter() {
                double maxLimit = getParameter("max").getValue().doubleValue();
                recordFilter = RecordFilterFactory.maxDoubleFilter(maxLimit);
            }
        };
    }
    
    
    /**
     * Creates a new trigger filter that accepts the monitored value if it is more 
     * than the value of the parameter associate with the "min" tag.
     * @return the trigger filter supporting the min filtering
     */
    static TriggerFilter minDoubleFilter() {
        return new TriggerFilter() {
            { 
                parameters = new Parameter[] {new Parameter(Double.class, "min", new Double(0))};
                updateFilter();
            }
            
            public String getLabel() { return "Min Double"; }
            
            public void update(DataAdaptor adaptor) {
                double maxLimit = adaptor.doubleValue("min");
                getParameter("min").setValue( new Double(maxLimit) );
                updateFilter();
            }
            
            public void write(DataAdaptor adaptor) {
                super.write(adaptor);
                adaptor.setValue("min", getParameter("min").getValue().doubleValue());
            }
            
            public void updateFilter() {
                double minLimit = getParameter("min").getValue().doubleValue();
                recordFilter = RecordFilterFactory.minDoubleFilter(minLimit);
            }
        };
    }
    
    
    /**
     * Creates a new trigger filter that accepts the monitored value if it is in
     * the range of the min and max parameters.
     * @return the trigger filter supporting the range filtering
     */
    static TriggerFilter rangeDoubleFilter() {
        return new TriggerFilter() {
            { 
                parameters = new Parameter[] {
                    new Parameter(Double.class, "min", new Double(0)),
                    new Parameter(Double.class, "max", new Double(0))
                };
                updateFilter();
            }
            
            public String getLabel() { return "Range Double"; }
            
            public void update(DataAdaptor adaptor) {
                double minLimit = adaptor.doubleValue("min");
                double maxLimit = adaptor.doubleValue("max");
                getParameter("min").setValue( new Double(minLimit) );
                getParameter("max").setValue( new Double(maxLimit) );
                updateFilter();
            }
            
            public void write(DataAdaptor adaptor) {
                super.write(adaptor);
                adaptor.setValue("min", getParameter("min").getValue().doubleValue());
                adaptor.setValue("max", getParameter("max").getValue().doubleValue());
            }
            
            public void updateFilter() {
                double minLimit = getParameter("min").getValue().doubleValue();
                double maxLimit = getParameter("max").getValue().doubleValue();
                recordFilter = RecordFilterFactory.rangeDoubleFilter(minLimit, maxLimit);
            }
        };
    }
}
