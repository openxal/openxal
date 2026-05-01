/*
 * Parameter.java
 *
 * Created on June 3, 2003, 8:42 AM
 */

package xal.app.scope;

/**
 * Parameter is a class which defines a numeric data type, a label and a value.
 *
 * @author  tap
 */
public class Parameter {
	/** numeric type */
    private Class<? extends Number> type;

	/** the paramter label */
    private String label;

	/** the current value */
    private Number value;


    /** Creates a new instance of Parameter */
    public Parameter( final Class<? extends Number> aType, final String aLabel ) {
        this( aType, aLabel, null );
    }
    
    
    /** Creates a new instance of Parameter */
    public Parameter( final Class<? extends Number> aType, final String aLabel, final Number defaultValue ) {
        type = aType;
        label = aLabel;
        value = defaultValue;
    }
    
    
    /**
     * Get the parameter label
     * @return label of the parameter
     */
    public String getLabel() {
        return label;
    }
    
    
    /**
     * Get the numeric type of the parameter.
     * @return the numeric type of the parameter
     */
    public Class<? extends Number> getType() {
        return type;
    }
    
    
    /**
     * Set the value of the parameter.
     * @param newValue new value of the parameter
     * @throws IllegalArgumentException if the value is not compatible with the parameter type
     */
    public void setValue( final Number newValue ) throws IllegalArgumentException {
        if ( newValue != null && !type.isInstance(newValue) ) {
            throw new IllegalArgumentException("Attempt to set value of class " + newValue.getClass().getName() + " for argument of type " + type);
        }
        value = newValue;
    }
    
    
    /**
     * Get the value of the parameter
     * @return value of the parameter
     */
    public Number getValue() {
        return value;
    }
}
