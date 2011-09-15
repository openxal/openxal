//
// DifferentiableUnaryOperation.java: Source file for 'DifferentiableUnaryOperation'
// Project xal
//
// Created by Tom Pelaia II on 5/3/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;


/** DifferentiableUnaryOperation */
abstract public class DifferentiableUnaryOperation extends DifferentiableSymbol {
    /** argument on which the operation is performed */
    final protected DifferentiableOperation ARGUMENT;
    
    
    /** Constructor */
    protected DifferentiableUnaryOperation( final DifferentiableOperation argument ) {
        ARGUMENT = argument;
    }
    
    
    /** get the argument to negate */
    protected DifferentiableOperation getArgument() {
        return ARGUMENT;
    }
    
    
    /** Get the label for the operation */
    abstract public String getLabel();
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    abstract public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable );
    
    
    /** Get the derivative with respect to the specified variable applying the chain rule for the argument */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.getDerivative( variable ).times( getDirectDerivative( variable ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Returns true if the arguments match. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ARGUMENT.isEqualTo( ((DifferentiableUnaryOperation)operation).ARGUMENT );
    }
    
    
    /** get the string representation */
    public String toString() {
        return getLabel() + "(" + ARGUMENT + ")";
    }
}
