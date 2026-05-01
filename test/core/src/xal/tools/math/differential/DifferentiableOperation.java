//
// DifferentiableOperation.java: Source file for 'DifferentiableOperation'
// Project xal
//
// Created by Tom Pelaia II on 4/29/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.math.differential;

import java.util.Map;
import java.util.HashMap;


/** DifferentiableOperation */
abstract public class DifferentiableOperation {
    /** precedence at addition level */
    static public final int ADDITION_PRECEDENCE = 0;
    
    /** precedence at subtraction level */
    static public final int SUBTRACTION_PRECEDENCE = ADDITION_PRECEDENCE + 1;
        
    /** precedence at product level */
    static public final int PRODUCT_PRECEDENCE = SUBTRACTION_PRECEDENCE + 1;
    
    /** precedence at quotient level */
    static public final int QUOTIENT_PRECEDENCE = PRODUCT_PRECEDENCE + 1;
    
    /** precedence at power level */
    static public final int POWER_PRECEDENCE = QUOTIENT_PRECEDENCE + 1;
    
    /** precedence at symbol level */
    static public final int SYMBOL_PRECEDENCE = POWER_PRECEDENCE + 1;
    
    
    /** get a constant operation representing the specified constant value */
    static public DifferentiableOperation getConstant( final double value ) {
        return DifferentiableConstant.getInstance( value );
    }
    
    
    /** get an operation representing the variable */
    static public DifferentiableVariable getVariable( final String name, final double defaultValue ) {
        return DifferentiableVariable.getInstance( name, defaultValue );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param userSubstitutions a user supplied map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted if the map is not null and not empty otherwise just return this operation
     */
    final public DifferentiableOperation copyWithSubstitutions( final Map<DifferentiableVariable,DifferentiableOperation> userSubstitutions ) {
        // copy the user supplied map as the substitution map will modify the substitutions
        final Map<DifferentiableOperation,DifferentiableOperation> substitutions = new HashMap<DifferentiableOperation,DifferentiableOperation>( userSubstitutions );
        return substitutions != null && !substitutions.isEmpty() ? copySubstituting( substitutions ) : this;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    abstract protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions );
    
    
    /** 
     * Construct a copy of this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    final protected DifferentiableOperation copySubstitutingWithCache( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        final DifferentiableOperation cachedSubstitute = substitutions.get( this );
        if ( cachedSubstitute != null ) {
            return cachedSubstitute;
        }
        else {
            final DifferentiableOperation substitute = copySubstituting( substitutions );
            substitutions.put( this, substitute );
            return substitute;
        }
    }

    
    /** Evaluate the operation with the default variable values */
    final public double evaluate() {
        return evaluate( null );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap ) {
        final Map<DifferentiableOperation,Double> cache = new HashMap<DifferentiableOperation,Double>();
        return evaluate( valueMap, cache );
    }
    
    
    /** get the operation precedence */
    abstract protected int getPrecedence();
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    abstract protected double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache );
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    final protected double evaluateWithCache( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        final Double cachedValue = cache.get( this );
        if ( cachedValue != null ) {
            return cachedValue.doubleValue();
        }
        else {
            final double value = evaluate( valueMap, cache );
            cache.put( this, value );
            return value;
        }
    }
    
    
    /** Get the derivative with respect to the specified variable */
    abstract public DifferentiableOperation getDerivative( final DifferentiableVariable variable );
    
    
    /** add the arguments */
    static private DifferentiableOperation sum( final DifferentiableOperation firstAddend, final DifferentiableOperation ... addends ) {
        DifferentiableOperation sum = firstAddend;
        for ( final DifferentiableOperation addend : addends ) {
            sum = sum.plus( addend );
        }
        return sum;
    }
    
    
    /** add the arguments */
    static public DifferentiableOperation sum( final DifferentiableOperation ... addends ) {
        return sum( DifferentiableZero.getInstance(), addends );
    }
    
    
    /** add the specified arguments to this */
    public DifferentiableOperation plus( final DifferentiableOperation ... addends ) {
        return sum( this, addends );
    }
    
    
    /** add the addend to this operation returning the new operation */
    @SuppressWarnings( "cast" )     // cast is required to call the specific method with a negation argument
    public DifferentiableOperation plus( final DifferentiableOperation addend ) {
        return addend instanceof DifferentiableZero ? this : addend instanceof DifferentiableNegation ? plus( (DifferentiableNegation)addend ) : DifferentiableAddition.add( this, addend );
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final double addend ) {
        return plus( new DifferentiableConstant( addend ) );
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final DifferentiableZero addend ) {
        return this;
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final DifferentiableNegation addend ) {
        return minus( addend.getArgument() );
    }
    
    
    /** subtract the subtrahend from this operation returning the new operation */
    @SuppressWarnings( "cast" )     // cast is required to call the specific method with a negation argument
    public DifferentiableOperation minus( final DifferentiableOperation subtrahend ) {
        return subtrahend instanceof DifferentiableZero ? this : subtrahend instanceof DifferentiableNegation ? minus( (DifferentiableNegation)subtrahend ) : DifferentiableSubtraction.subtract( this, subtrahend );
    }
    
    
    /** subtract the subtrahend from this operation returning the new operation */
    public DifferentiableOperation minus( final double subtrahend ) {
        return minus( new DifferentiableConstant( subtrahend ) );
    }
    
    
    /** subtract the subtrahend from this operation returning the new operation */
    public DifferentiableOperation minus( final DifferentiableZero subtrahend ) {
        return this;
    }
    
    
    /** subtract the subtrahend from this operation returning the new operation */
    public DifferentiableOperation minus( final DifferentiableNegation subtrahend ) {
        return plus( subtrahend.getArgument() );
    }
    
    
    /** multiply the arguments */
    static public DifferentiableOperation multiply( final DifferentiableOperation ... multiplicands ) {
        return multiply( DifferentiableOne.getInstance(), multiplicands );
    }
    
    
    /** multiply the arguments */
    static private DifferentiableOperation multiply( final DifferentiableOperation firstMultiplicand, final DifferentiableOperation ... multiplicands ) {
        DifferentiableOperation product = firstMultiplicand;
        for ( final DifferentiableOperation multiplicand : multiplicands ) {
            product = product.times( multiplicand );
        }
        return product;
    }
    
    
    /** multiply the arguments */
    public DifferentiableOperation times( final DifferentiableOperation ... multiplicands ) {
        return multiply( this, multiplicands );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    @SuppressWarnings( "cast" )     // cast is required to call the specific times method for zero
    public DifferentiableOperation times( final DifferentiableOperation multiplicand ) {
        return multiplicand instanceof DifferentiableZero ? times( (DifferentiableZero)multiplicand ) : multiplicand instanceof DifferentiableOne ? this : DifferentiableMultiplication.multiply( this, multiplicand );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final double value ) {
        return times( DifferentiableConstant.getInstance( value ) );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableZero multiplicand ) {
        return DifferentiableZero.getInstance();
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableOne multiplicand ) {
        return this;
    }
    
    
    /** Divide the operation from this */
    @SuppressWarnings( "cast" )     // cast is required to call the specific method with a quotient argument
    public DifferentiableOperation over( final DifferentiableOperation divisor ) {
        return divisor instanceof DifferentiableOne ? this : divisor instanceof DifferentiableDivision ? over( (DifferentiableDivision)divisor ) : DifferentiableDivision.divide( this, divisor );
    }
    
    
    /** Divide the operation from this */
    public DifferentiableOperation over( final double value ) {
        return over( DifferentiableConstant.getInstance( value ) );
    }
    
    
    /** Divide the operation from this */
    public DifferentiableOperation over( final DifferentiableOne divisor ) {
        return this;
    }
    
    
    /** Divide the operation from this */
    public DifferentiableOperation over( final DifferentiableDivision divisor ) {
        return times( divisor.reciprocal() );
    }
    
    
    /** multiply the operation by negative one */
    public DifferentiableOperation negate() {
        return DifferentiableNegation.negate( this );
    }
    
    
    /** get the reciprocal of this operation */
    public DifferentiableOperation reciprocal() {
        return DifferentiableDivision.divide( 1.0, this );
    }
    
    
    /** get the absolute value of this operation */
    public DifferentiableOperation abs() {
        return DifferentiableAbsoluteValue.abs( this );
    }
    
    
    /** get this operation raised to the specified power */
    public DifferentiableOperation pow( final double power ) {
        return DifferentiableConstantPower.pow( this, power );
    }
    
    
    /** get this operation raised to the specified power */
    public DifferentiableOperation pow( final DifferentiableOperation power ) {
        return DifferentiablePower.pow( this, power );
    }
    
    
    /** get the square root of this operation */
    public DifferentiableOperation sqrt() {
        return DifferentiableSquareRoot.sqrt( this );
    }
    
    
    /** get the sine value of this operation */
    public DifferentiableOperation sin() {
        return DifferentiableSine.sin( this );
    }
    
    
    /** get the cosine value of this operation */
    public DifferentiableOperation cos() {
        return DifferentiableCosine.cos( this );
    }
    
    
    /** get the tangent value of this operation */
    public DifferentiableOperation tan() {
        return DifferentiableTangent.tan( this );
    }
    
    
    /** get the arc sine value of this operation */
    public DifferentiableOperation asin() {
        return DifferentiableArcSine.asin( this );
    }
    
    
    /** get the arc cosine value of this operation */
    public DifferentiableOperation acos() {
        return DifferentiableArcCosine.acos( this );
    }
    
    
    /** get the arc tangent value of this operation */
    public DifferentiableOperation atan() {
        return DifferentiableArcTangent.atan( this );
    }
    
    
    /** get the hyperbolic sine value of this operation */
    public DifferentiableOperation sinh() {
        return DifferentiableSinh.sinh( this );
    }
    
    
    /** get the hyperbolic cosine value of this operation */
    public DifferentiableOperation cosh() {
        return DifferentiableCosh.cosh( this );
    }
    
    
    /** get the hyperbolic tangent value of this operation */
    public DifferentiableOperation tanh() {
        return DifferentiableTanh.tanh( this );
    }
    
    
    /** get the exponential of this operation */
    public DifferentiableOperation exp() {
        return DifferentiableExponential.exp( this );
    }
    
    
    /** get the natural logarithm of this operation */
    public DifferentiableOperation log() {
        return DifferentiableLogarithm.log( this );
    }
    
    
    /** Generate the string representation based on the precedence of the parent operation */
    protected String toString( final int parentPrecedence ) {
        return toString( parentPrecedence, false );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. */
    abstract protected boolean isEquivalentTo( final DifferentiableOperation operation );
    
    
    /** Test whether this operation is equal to or equivalent to the specified operation. */
    final public boolean isEqualTo( final DifferentiableOperation operation ) {
        return this.equals( operation ) || ( this.getClass().equals( operation.getClass() ) && this.isEquivalentTo( operation ) );
    }
    
    
    /** Generate the string representation based on the precedence of the parent operation */
    protected String toString( final int parentPrecedence, final boolean closeIfEqualPrecedence ) {
        final int precedence = getPrecedence();
        
        if ( precedence < parentPrecedence || ( closeIfEqualPrecedence && precedence == parentPrecedence ) ) {
            return "(" + toString() + ")";
        }
        else {
            return toString();
        }
    }
}



/** constant operation */
class DifferentiableConstant extends DifferentiableSymbol {
    /** constant value */
    final private double VALUE;
    
    
    /** Constructor */
    public DifferentiableConstant( final double value ) {
        VALUE = value;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return this;
    }
    
    
    /** generate a new constant operation for the specified constant value */
    static public DifferentiableConstant getInstance( final double value ) {
        if ( value == 0.0 ) {
            return DifferentiableZero.getInstance();
        }
        else if ( value == 1.0 ) {
            return DifferentiableOne.getInstance();
        }
        else {
            return new DifferentiableConstant( value );
        }
    }
    
    
    /** get the constant value */
    public double getValue() {
        return VALUE;
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return VALUE;
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return DifferentiableZero.getInstance();
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final DifferentiableOperation addend ) {
        return addend instanceof DifferentiableConstant ? plus( (DifferentiableConstant)addend ) : super.plus( addend );
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final DifferentiableConstant addend ) {
        return DifferentiableConstant.getInstance( this.VALUE + addend.VALUE );
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final double value ) {
        return DifferentiableConstant.getInstance( this.VALUE + value );
    }
    
    
    /** perform constant subtraction */
    public DifferentiableOperation minus( final DifferentiableOperation subtrahend ) {
        return subtrahend instanceof DifferentiableConstant ? minus( (DifferentiableConstant)subtrahend ) : super.minus( subtrahend );
    }
    
    
    /** perform constant subtraction */
    public DifferentiableOperation minus( final DifferentiableConstant subtrahend ) {
        return DifferentiableConstant.getInstance( this.VALUE - subtrahend.VALUE );
    }
    
    
    /** perform constant subtraction */
    public DifferentiableOperation minus( final double subtrahend ) {
        return DifferentiableConstant.getInstance( this.VALUE - subtrahend );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableOperation multiplicand ) {
        return multiplicand instanceof DifferentiableConstant ? times( (DifferentiableConstant)multiplicand ) : super.times( multiplicand );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableConstant multiplicand ) {
        return DifferentiableConstant.getInstance( this.VALUE * multiplicand.VALUE );
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final double value ) {
        return DifferentiableConstant.getInstance( this.VALUE * value );
    }
    
    
    /** perform constant division */
    public DifferentiableOperation over( final DifferentiableOperation divisor ) {
        return divisor instanceof DifferentiableConstant ? over( (DifferentiableConstant)divisor ) : super.over( divisor );
    }
    
    
    /** perform constant division */
    public DifferentiableOperation over( final DifferentiableConstant divisor ) {
        return DifferentiableConstant.getInstance( this.VALUE / divisor.VALUE );
    }
    
    
    /** perform constant division */
    public DifferentiableOperation over( final double divisor ) {
        return DifferentiableConstant.getInstance( this.VALUE / divisor );
    }
    
    
    /** get the absolute value of this operation */
    public DifferentiableOperation abs() {
        return VALUE >= 0 ? this : getInstance( -VALUE );
    }
    
    
    /** get the absolute value of this operation */
    public DifferentiableOperation negate() {
        return getInstance( -VALUE );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Indicates whether the internal values are equal. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return VALUE == ((DifferentiableConstant)operation).VALUE;
    }
    
    
    /** get the string representation */
    public String toString() {
        return String.valueOf( VALUE );
    }
}



/** constant operation representing zero */
class DifferentiableZero extends DifferentiableConstant {
    /** singleton constant */
    static final DifferentiableZero ZERO_OPERATION;
    
    
    // static initializer
    static {
        ZERO_OPERATION = new DifferentiableZero();
    }
    
    
    /** Constructor */
    private DifferentiableZero() {
        super( 0.0 );
    }
    
    
    /** get the singleton instance */
    static public DifferentiableZero getInstance() {
        return ZERO_OPERATION;
    }
    
    
    /** add the addend to this operation returning the new operation */
    public DifferentiableOperation plus( final DifferentiableOperation addend ) {
        return addend;
    }
    
    
    /** subtract the operation from this */
    public DifferentiableOperation minus( final DifferentiableOperation subtrahend ) {
        return subtrahend.negate();
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableOperation multiplicand ) {
        return this;
    }
    
    
    /** Divide the operation from this */
    public DifferentiableOperation over( final DifferentiableOperation divisor ) {
        return this;
    }
    
    
    /** multiply the operation by negative one */
    public DifferentiableOperation negate() {
        return this;
    }
}



/** constant operation representing one */
class DifferentiableOne extends DifferentiableConstant {
    /** singleton constant */
    static final DifferentiableOne ONE_OPERATION;
    
    
    // static initializer
    static {
        ONE_OPERATION = new DifferentiableOne();
    }
    
    
    /** Constructor */
    private DifferentiableOne() {
        super( 1.0 );
    }
    
    
    /** get the singleton instance */
    static public DifferentiableOne getInstance() {
        return ONE_OPERATION;
    }
    
    
    /** multiply the multiplicand to this operation returning the new operation */
    public DifferentiableOperation times( final DifferentiableOperation multiplicand ) {
        return multiplicand;
    }
}



/** Operation for adding operation addends */
class DifferentiableAddition extends DifferentiableOperation {
    final private DifferentiableOperation SUMMAND;
    final private DifferentiableOperation ADDEND;
    
    
    /** Constructor */
    public DifferentiableAddition( final DifferentiableOperation summand, final DifferentiableOperation addend ) {
        if ( summand instanceof DifferentiableConstant ) {  // always put constants at the end
            ADDEND = summand;
            SUMMAND = addend;
        }
        else {
            SUMMAND = summand;
            ADDEND = addend;
        }
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableAddition( SUMMAND.copySubstitutingWithCache( substitutions ), ADDEND.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.ADDITION_PRECEDENCE;
    }
    
    
    /** generate the addition operation */
    static public DifferentiableOperation add( final DifferentiableOperation summand, final DifferentiableOperation addend ) {
        if ( addend instanceof DifferentiableNegation )  return summand.minus( addend.negate() );
        
        if ( summand.isEqualTo( addend.negate() ) )  return DifferentiableOperation.getConstant( 0.0 );
        
        // collect all constants if any and place them at the end so constants can be coalesced into a single constant
        
        double constantSum = 0.0;                       // coalesce constants to this variable
        DifferentiableOperation operationSum = null;    // gather non-constant operations to this variable
        
        // test whether the summand is an addition operation so any constants can be collected and coalesced
        if ( summand instanceof DifferentiableAddition ) {
            final DifferentiableAddition summandAddition = (DifferentiableAddition)summand;
            if ( summandAddition.ADDEND instanceof DifferentiableConstant ) {
                constantSum += ((DifferentiableConstant)summandAddition.ADDEND).getValue();
                operationSum = summandAddition.SUMMAND;
            }
            else {
                operationSum = summand;
            }
        }
        else if ( summand instanceof DifferentiableConstant ) {
            constantSum += ((DifferentiableConstant)summand).getValue();
        }
        else {
            operationSum = summand;
        }
        
        // test whether the addend is an addition operation so any constants can be collected and coalesced
        if ( addend instanceof DifferentiableAddition ) {
            final DifferentiableAddition addendAddition = (DifferentiableAddition)addend;
            if ( addendAddition.ADDEND instanceof DifferentiableConstant ) {
                constantSum += ((DifferentiableConstant)addendAddition.ADDEND).getValue();
                operationSum = operationSum != null ? new DifferentiableAddition( operationSum, addendAddition.SUMMAND ) : addendAddition.SUMMAND;
            }
            else {
                operationSum = operationSum != null ? new DifferentiableAddition( operationSum, addend ) : addend;
            }
        }
        else if ( addend instanceof DifferentiableConstant ) {
            constantSum += ((DifferentiableConstant)addend).getValue();
        }
        else {
            operationSum = operationSum != null ? new DifferentiableAddition( operationSum, addend ) : addend;
        }
        
        if ( constantSum == 0.0 ) {
            return operationSum != null ? operationSum : DifferentiableOperation.getConstant( 0.0 );
        }
        else {
            final DifferentiableOperation constantSumOperation = DifferentiableOperation.getConstant( constantSum );
            return operationSum != null ? new DifferentiableAddition( operationSum, constantSumOperation ) : constantSumOperation;
        }
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return SUMMAND.evaluateWithCache( valueMap, cache ) + ADDEND.evaluateWithCache( valueMap, cache );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return SUMMAND.getDerivative( variable ).plus( ADDEND.getDerivative( variable ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. The summands and addends must be equal but the order doesn't matter. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ( SUMMAND.isEqualTo( ((DifferentiableAddition)operation).SUMMAND ) && ADDEND.isEqualTo( ((DifferentiableAddition)operation).ADDEND ) ) || 
            ( SUMMAND.isEqualTo( ((DifferentiableAddition)operation).ADDEND ) && ADDEND.isEqualTo( ((DifferentiableAddition)operation).SUMMAND ) );
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return SUMMAND.toString( precedence ) + " + " + ADDEND.toString( precedence );
    }
}



/** Operation for subtracting operations */
class DifferentiableSubtraction extends DifferentiableOperation {
    final private DifferentiableOperation MINUEND;
    final private DifferentiableOperation SUBTRAHEND;
    
    
    /** Constructor */
    public DifferentiableSubtraction( final DifferentiableOperation minuend, final DifferentiableOperation subtrahend ) {
        MINUEND = minuend;
        SUBTRAHEND = subtrahend;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableSubtraction( MINUEND.copySubstitutingWithCache( substitutions ), SUBTRAHEND.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.SUBTRACTION_PRECEDENCE;
    }
    
    
    /** generate the addition operation */
    static public DifferentiableOperation subtract( final DifferentiableOperation minuend, final DifferentiableOperation subtrahend ) {
        return minuend.isEqualTo( subtrahend ) ? DifferentiableOperation.getConstant( 0.0 ) : new DifferentiableSubtraction( minuend, subtrahend );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return MINUEND.evaluateWithCache( valueMap, cache ) - SUBTRAHEND.evaluateWithCache( valueMap, cache );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return MINUEND.getDerivative( variable ).minus( SUBTRAHEND.getDerivative( variable ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Indicates whether the minuend and subtrahend are equal to those of the specified operation. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return MINUEND.isEqualTo( ((DifferentiableSubtraction)operation).MINUEND ) && SUBTRAHEND.isEqualTo( ((DifferentiableSubtraction)operation).SUBTRAHEND );
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return MINUEND.toString( precedence ) + " - " + SUBTRAHEND.toString( precedence, true );
    }
}



/** Operation for multiplying two operations */
class DifferentiableMultiplication extends DifferentiableOperation {
    final private DifferentiableOperation MULTIPLICAND;
    final private DifferentiableOperation MULTIPLIER;
    
    
    /** Constructor */
    public DifferentiableMultiplication( final DifferentiableOperation multiplicand, final DifferentiableOperation multiplier ) {
        if ( multiplier instanceof DifferentiableConstant ) {   // always put constants at the front
            MULTIPLICAND = multiplier;
            MULTIPLIER = multiplicand;
        }
        else {
            MULTIPLICAND = multiplicand;
            MULTIPLIER = multiplier;
        }
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableMultiplication( MULTIPLICAND.copySubstitutingWithCache( substitutions ), MULTIPLIER.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.PRODUCT_PRECEDENCE;
    }
    
    
    /** generate the multiplication operation */
    static public DifferentiableOperation multiply( final DifferentiableOperation multiplicand, final DifferentiableOperation multiplier ) {
        // collect all constants if any and place them at the front so constants can be coalesced into a single constant
        
        double constantProduct = 1.0;                       // coalesce constants to this variable
        DifferentiableOperation operationProduct = null;    // gather non-constant operations to this variable
        
        // test whether the multiplicand is a multiplication operation so any constants can be collected and coalesced
        if ( multiplicand instanceof DifferentiableMultiplication ) {
            final DifferentiableMultiplication multiplicandProduct = (DifferentiableMultiplication)multiplicand;
            if ( multiplicandProduct.MULTIPLICAND instanceof DifferentiableConstant ) {
                constantProduct *= ((DifferentiableConstant)multiplicandProduct.MULTIPLICAND).getValue();
                operationProduct = multiplicandProduct.MULTIPLIER;
            }
            else {
                operationProduct = multiplicand;
            }
        }
        else if ( multiplicand instanceof DifferentiableConstant ) {
            constantProduct *= ((DifferentiableConstant)multiplicand).getValue();
        }
        else {
            operationProduct = multiplicand;
        }
        
        // test whether the multiplier is a multiplication operation so any constants can be collected and coalesced
        if ( multiplier instanceof DifferentiableMultiplication ) {
            final DifferentiableMultiplication multiplierProduct = (DifferentiableMultiplication)multiplier;
            if ( multiplierProduct.MULTIPLICAND instanceof DifferentiableConstant ) {
                constantProduct *= ((DifferentiableConstant)multiplierProduct.MULTIPLICAND).getValue();
                operationProduct = operationProduct != null ? new DifferentiableMultiplication( operationProduct, multiplierProduct.MULTIPLIER ) : multiplierProduct.MULTIPLIER;
            }
            else {
                operationProduct = operationProduct != null ? new DifferentiableMultiplication( operationProduct, multiplier ) : multiplier;
            }
        }
        else if ( multiplier instanceof DifferentiableConstant ) {
            constantProduct *= ((DifferentiableConstant)multiplier).getValue();
        }
        else {
            operationProduct = operationProduct != null ? new DifferentiableMultiplication( operationProduct, multiplier ) : multiplier;
        }
                
        if ( constantProduct == 0.0 ) {
            return DifferentiableOperation.getConstant( 0.0 );
        }
        else if ( constantProduct == 1.0 ) {
            return operationProduct != null ? operationProduct : DifferentiableOperation.getConstant( 1.0 );
        }
        else {
            final DifferentiableOperation constantProductOperation = DifferentiableOperation.getConstant( constantProduct );
            return operationProduct != null ? new DifferentiableMultiplication( constantProductOperation, operationProduct ) : constantProductOperation;
        }
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return MULTIPLICAND.evaluateWithCache( valueMap, cache ) * MULTIPLIER.evaluateWithCache( valueMap, cache );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return MULTIPLICAND.getDerivative( variable ).times( MULTIPLIER ).plus( MULTIPLICAND.times( MULTIPLIER.getDerivative( variable ) ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Indicates whether the multiplicands and multipiers match those of the specified operation. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ( MULTIPLICAND.isEqualTo( ((DifferentiableMultiplication)operation).MULTIPLICAND ) && MULTIPLIER.isEqualTo( ((DifferentiableMultiplication)operation).MULTIPLIER ) ) || 
              ( MULTIPLICAND.isEqualTo( ((DifferentiableMultiplication)operation).MULTIPLIER ) && MULTIPLIER.isEqualTo( ((DifferentiableMultiplication)operation).MULTIPLICAND ) );
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return MULTIPLICAND.toString( precedence ) + " * " + MULTIPLIER.toString( precedence );
    }
}



/** Operation for dividing two operations */
class DifferentiableDivision extends DifferentiableOperation {
    final private DifferentiableOperation DIVIDEND;
    final private DifferentiableOperation DIVISOR;
    
    
    /** Constructor */
    public DifferentiableDivision( final DifferentiableOperation dividend, final DifferentiableOperation divisor ) {
        DIVIDEND = dividend;
        DIVISOR = divisor;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableDivision( DIVIDEND.copySubstitutingWithCache( substitutions ), DIVISOR.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.QUOTIENT_PRECEDENCE;
    }
    
    
    /** generate the division operation */
    static public DifferentiableOperation divide( final DifferentiableOperation dividend, final DifferentiableOperation divisor ) {
        return dividend.isEqualTo( divisor ) ? DifferentiableOperation.getConstant( 1.0 ) : new DifferentiableDivision( dividend, divisor );
    }
    
    
    /** generate the division operation */
    static public DifferentiableOperation divide( final double dividend, final DifferentiableOperation divisor ) {
        return new DifferentiableDivision( getConstant( dividend ), divisor );
    }
    
    
    /** get the reciprocal operation */
    public DifferentiableOperation reciprocal() {
        return DIVISOR.over( DIVIDEND );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return DIVIDEND.evaluateWithCache( valueMap, cache ) / DIVISOR.evaluateWithCache( valueMap, cache );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return DIVIDEND.getDerivative( variable ).minus( DIVISOR.getDerivative( variable ).times( DIVIDEND ).over( DIVISOR ) ).over( DIVISOR );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. Indicates whether the dividend and divers match those of the specified operation. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return DIVIDEND.isEqualTo( ((DifferentiableDivision)operation).DIVIDEND ) && DIVISOR.isEqualTo( ((DifferentiableDivision)operation).DIVISOR ); 
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return DIVIDEND.toString( precedence ) + " / " + DIVISOR.toString( precedence, true );
    }
}



/** Operation for getting the negative of an operation */
class DifferentiableNegation extends DifferentiableOperation {
    final private DifferentiableOperation ARGUMENT;
    
    
    /** Constructor */
    public DifferentiableNegation( final DifferentiableOperation argument ) {
        ARGUMENT = argument;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableNegation( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.PRODUCT_PRECEDENCE;
    }
    
    
    /** get the argument to negate */
    DifferentiableOperation getArgument() {
        return ARGUMENT;
    }
    
    
    /** generate the division operation */
    static public DifferentiableOperation negate( final DifferentiableOperation argument ) {
        return new DifferentiableNegation( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return - ARGUMENT.evaluateWithCache( valueMap, cache );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.getDerivative( variable ).negate();
    }
    
    
    /** get the negation value of this operation */
    public DifferentiableOperation negate() {
        return ARGUMENT;
    }
    
    
    /** get the absolute value of this operation */
    public DifferentiableOperation abs() {
        return ARGUMENT.abs();
    }
    
    
    /** get the cosine of this operation */
    public DifferentiableOperation cos() {
        return ARGUMENT.cos();
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances.*/
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ARGUMENT.isEqualTo( ((DifferentiableNegation)operation).ARGUMENT );
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return "-" + ARGUMENT.toString( precedence );
    }
}



/** Operation for getting the absolute value of an operation */
class DifferentiableAbsoluteValue extends DifferentiableSymbol {
    final private DifferentiableOperation ARGUMENT;
    
    
    /** Constructor */
    public DifferentiableAbsoluteValue( final DifferentiableOperation argument ) {
        ARGUMENT = argument;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableAbsoluteValue( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the argument to negate */
    DifferentiableOperation getArgument() {
        return ARGUMENT;
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation abs( final DifferentiableOperation argument ) {
        return new DifferentiableAbsoluteValue( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.abs( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.getDerivative( variable ).times( ARGUMENT ).over( ARGUMENT.abs() );
    }
    
    
    /** get the absolute value of this operation */
    public DifferentiableOperation abs() {
        return this;
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances. */
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ARGUMENT.isEqualTo( ((DifferentiableAbsoluteValue)operation).ARGUMENT );
    }
    
    
    /** get the string representation */
    public String toString() {
        return "|" + ARGUMENT + "|";
    }
}



/** Operation for getting the sine of an operation */
class DifferentiableSine extends DifferentiableUnaryOperation {    
    /** Constructor */
    public DifferentiableSine( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableSine( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation sin( final DifferentiableOperation argument ) {
        return new DifferentiableSine( argument );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "sin"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.cos();
    }

    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.sin( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the cosine of an operation */
class DifferentiableCosine extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableCosine( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableCosine( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation cos( final DifferentiableOperation argument ) {
        return argument instanceof DifferentiableAbsoluteValue ? cos( (DifferentiableAbsoluteValue)argument ) : argument instanceof DifferentiableNegation ? cos( (DifferentiableNegation)argument ) : new DifferentiableCosine( argument );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation cos( final DifferentiableAbsoluteValue argument ) {
        return new DifferentiableCosine( argument.getArgument() );
    }
    
    
    /** generate the division operation */
    static public DifferentiableOperation cos( final DifferentiableNegation argument ) {
        return new DifferentiableCosine( argument.getArgument() );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "cos"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.sin().negate();
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.cos( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the tangent of an operation */
class DifferentiableTangent extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableTangent( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableTangent( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation tan( final DifferentiableOperation argument ) {
        return new DifferentiableTangent( argument );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "tan"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.cos().pow( -2.0 );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.tan( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the arc sine of an operation */
class DifferentiableArcSine extends DifferentiableUnaryOperation {    
    /** Constructor */
    public DifferentiableArcSine( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableArcSine( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation asin( final DifferentiableOperation argument ) {
        return new DifferentiableArcSine( argument );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "asin"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return DifferentiableOne.getInstance().minus( ARGUMENT.pow( 2 ) ).sqrt().reciprocal();
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.asin( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the arc cosine of an operation */
class DifferentiableArcCosine extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableArcCosine( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableArcCosine( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation acos( final DifferentiableOperation argument ) {
        return new DifferentiableArcCosine( argument );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "acos"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return DifferentiableConstant.getInstance( -1.0 ).over( DifferentiableOne.getInstance().minus( ARGUMENT.pow( 2 ) ).sqrt() );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.acos( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the arc tangent of an operation */
class DifferentiableArcTangent extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableArcTangent( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableArcTangent( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation atan( final DifferentiableOperation argument ) {
        return new DifferentiableArcTangent( argument );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "atan"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return DifferentiableOne.getInstance().plus( ARGUMENT.pow( 2 ) ).reciprocal();
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.atan( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
}



/** Operation for getting the hyperbolic sine of an operation */
class DifferentiableSinh extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableSinh( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableSinh( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation sinh( final DifferentiableOperation argument ) {
        return new DifferentiableSinh( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.sinh( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "sinh"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.cosh();
    }
}



/** Operation for getting the hyperbolic cosine of an operation */
class DifferentiableCosh extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableCosh( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableCosh( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
   
    
    /** generate the operation */
    static public DifferentiableOperation cosh( final DifferentiableOperation argument ) {
        return argument instanceof DifferentiableAbsoluteValue ? cosh( (DifferentiableAbsoluteValue)argument ) : argument instanceof DifferentiableNegation ? cosh( (DifferentiableNegation)argument ) : new DifferentiableCosh( argument );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation cosh( final DifferentiableAbsoluteValue argument ) {
        return new DifferentiableCosh( argument.getArgument() );
    }
    
    
    /** generate the division operation */
    static public DifferentiableOperation cosh( final DifferentiableNegation argument ) {
        return new DifferentiableCosh( argument.getArgument() );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.cosh( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "cosh"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.sinh();
    }
}



/** Operation for getting the hyperbolic tangent of an operation */
class DifferentiableTanh extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableTanh( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableTanh( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation tanh( final DifferentiableOperation argument ) {
        return new DifferentiableTanh( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.tanh( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "tanh"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.cosh().pow( -2.0 );
    }
}



/** Operation for raising the argument to a constant power */
class DifferentiableConstantPower extends DifferentiableSymbol {
    /** argument to raise to the power */
    final protected DifferentiableOperation ARGUMENT;
    
    /** power to which to raise the argument */
    final private double POWER;
    
    
    /** Constructor */
    public DifferentiableConstantPower( final DifferentiableOperation argument, final double power ) {
        ARGUMENT = argument;
        POWER = power;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableConstantPower( ARGUMENT.copySubstitutingWithCache( substitutions ), POWER );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.POWER_PRECEDENCE;
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation pow( final DifferentiableOperation argument, final double power ) {
        return power == 0.0 ? DifferentiableOne.getInstance() : power == 1.0 ? argument : power == 0.5 ? DifferentiableSquareRoot.sqrt( argument ) : new DifferentiableConstantPower( argument, power );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation pow( final DifferentiableOperation argument, final DifferentiableConstant power ) {
        return DifferentiableConstantPower.pow( argument, power.getValue() );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.pow( ARGUMENT.evaluateWithCache( valueMap, cache ), POWER );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return new DifferentiableConstant( POWER ).times( ARGUMENT.getDerivative( variable ) ).times( DifferentiableConstantPower.pow( ARGUMENT, POWER - 1.0 ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances.*/
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ARGUMENT.isEqualTo( ((DifferentiableConstantPower)operation).ARGUMENT ) && POWER == ((DifferentiableConstantPower)operation).POWER;
    }
   
    
    /** get the string representation */
    public String toString() {
        return ARGUMENT.toString( getPrecedence() ) + " ^ " + POWER;
    }
}



/** Operation for raising the argument to an arbitrary power */
class DifferentiablePower extends DifferentiableSymbol {
    /** argument to raise to the power */
    final protected DifferentiableOperation ARGUMENT;
    
    /** power to which to raise the argument */
    final protected DifferentiableOperation POWER;
    
    
    /** Constructor */
    public DifferentiablePower( final DifferentiableOperation argument, final DifferentiableOperation power ) {
        ARGUMENT = argument;
        POWER = power;
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiablePower( ARGUMENT.copySubstitutingWithCache( substitutions ), POWER.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** get the operation precedence */
    protected int getPrecedence() {
        return DifferentiableOperation.POWER_PRECEDENCE;
    }
    
    /** generate the operation */
    static public DifferentiableOperation pow( final DifferentiableOperation argument, final DifferentiableOperation power ) {
        return power instanceof DifferentiableConstant ? pow( argument, (DifferentiableConstant)power ) : new DifferentiablePower( argument, power );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation pow( final DifferentiableOperation argument, final double power ) {
        return DifferentiableConstantPower.pow( argument, power );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation pow( final DifferentiableOperation argument, final DifferentiableConstant power ) {
        return DifferentiableConstantPower.pow( argument, power.getValue() );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.pow( ARGUMENT.evaluateWithCache( valueMap, cache ), POWER.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the derivative with respect to the specified variable */
    final public DifferentiableOperation getDerivative( final DifferentiableVariable variable ) {
        return this.times( POWER.getDerivative( variable ).times( ARGUMENT.log() ).plus( POWER.times( ARGUMENT.getDerivative( variable ) ).over( ARGUMENT ) ) );
    }
    
    
    /** Test whether this operation is equivalent to the specified operation when the two operations are different instances.*/
    protected boolean isEquivalentTo( final DifferentiableOperation operation ) {
        return ARGUMENT.isEqualTo( ((DifferentiablePower)operation).ARGUMENT ) && POWER == ((DifferentiablePower)operation).POWER;
    }
    
    
    /** get the string representation */
    public String toString() {
        final int precedence = getPrecedence();
        return ARGUMENT.toString( precedence ) + " ^ " + POWER.toString( precedence, true );
    }
}



/** Operation for taking the square root */
class DifferentiableSquareRoot extends DifferentiableConstantPower {
    /** Constructor */
    public DifferentiableSquareRoot( final DifferentiableOperation argument ) {
        super( argument, 0.5 );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableSquareRoot( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation sqrt( final DifferentiableOperation argument ) {
        return new DifferentiableSquareRoot( argument );
    }
    
    
    /** get the string representation */
    public String toString() {
        return "sqrt(" + ARGUMENT + ")";
    }
}



/** Operation for performing the exponential */
class DifferentiableExponential extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableExponential( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableExponential( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation exp( final DifferentiableOperation argument ) {
        return new DifferentiableExponential( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.exp( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "exp"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return this;
    }
}



/** Operation for performing the logarithm */
class DifferentiableLogarithm extends DifferentiableUnaryOperation {
    /** Constructor */
    public DifferentiableLogarithm( final DifferentiableOperation argument ) {
        super( argument );
    }
    
    
    /** 
     * Construct a copy this operation substituting the operations given in the map.
     * @param substitutions map of the new operations keyed by the current operations to be substituted
     * @return a new operation with operations substituted
     */
    protected DifferentiableOperation copySubstituting( final Map<DifferentiableOperation,DifferentiableOperation> substitutions ) {
        return new DifferentiableLogarithm( ARGUMENT.copySubstitutingWithCache( substitutions ) );
    }
    
    
    /** generate the operation */
    static public DifferentiableOperation log( final DifferentiableOperation argument ) {
        return new DifferentiableLogarithm( argument );
    }
    
    
    /** Evaluate the operation for the specified variable values using the default value if this variable is not specified in the map */
    public double evaluate( final DifferentiableVariableValues valueMap, final Map<DifferentiableOperation,Double> cache ) {
        return Math.log( ARGUMENT.evaluateWithCache( valueMap, cache ) );
    }
    
    
    /** Get the label for the operation */
    final public String getLabel() { return "ln"; }
    
    
    /** Get the derivative for just this operation without regard for the chain rule */
    final public DifferentiableOperation getDirectDerivative( final DifferentiableVariable variable ) {
        return ARGUMENT.reciprocal();
    }
}



