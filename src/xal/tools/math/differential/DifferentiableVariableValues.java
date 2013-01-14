//
// DifferentiableVariable.java: Source file for 'DifferentiableVariable'
// Project xal
//
// Created by Tom Pelaia II on 5/2/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;

import java.util.Map;
import java.util.Hashtable;


/** Contains the assigned values for variables */
public class DifferentiableVariableValues {
    /** values keyed by variable */
    final private Map<DifferentiableVariable,Double> VALUE_MAP;
    
    
    /** Constructor */
    public DifferentiableVariableValues() {
        VALUE_MAP = new Hashtable<DifferentiableVariable,Double>();
    }
    
    
    /** Get a new instance */
    static public DifferentiableVariableValues getInstance() {
        return new DifferentiableVariableValues();
    }
    
    
    /** unassign all variable values */
    public void clear() {
        VALUE_MAP.clear();
    }
    
    
    /** number of assignments */
    public int assignmentCount() {
        return VALUE_MAP.size();
    }
    
    
    /** assign the value to the variable */
    public void assignValue( final DifferentiableVariable variable, final double value ) {
        VALUE_MAP.put( variable, value );
    }
    
    
    /** unassign the value to the variable */
    public void unassignValue( final DifferentiableVariable variable ) {
        VALUE_MAP.remove( variable );
    }
    
    
    /** Determine whether a value has been assigned for the specified variable */
    public boolean isAssignedValue( final DifferentiableVariable variable ) {
        return VALUE_MAP.containsKey( variable );
    }
    
    
    /** Get the assigned value */
    public double getAssignedValue( final DifferentiableVariable variable ) {
        return VALUE_MAP.get( variable );
    }
    
    
    /** Get the value for the variable using the assigned value if it exists; otherwise using the variable's default value */
    public double getValue( final DifferentiableVariable variable ) {
        return isAssignedValue( variable ) ? getAssignedValue( variable ) : variable.getDefaultValue();
    }
    
    
    /** Get the string representation of this mapping */
    public String toString() {
        return VALUE_MAP.toString();
    }
}