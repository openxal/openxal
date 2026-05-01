/*
 * FormulaInterpreter.java
 *
 * Created on August 19, 2013, 3:30 PM
 */

package xal.tools.formula;

import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;


/**
 * FormulaInterpreter is a class used to evaluate a formula with a given set of variables provided by the user.
 * @author  tap
 */
final public class FormulaInterpreter {
	/** standard script header */
	static final private String STANDARD_SCRIPT_HEADER;

	/** script engine to perform the formula evaluation */
	final private ScriptEngine SCRIPT_ENGINE;

	/** formula to evaluate */
	private String _formula;


	// static initializer
	static {
		STANDARD_SCRIPT_HEADER = generateStandardScriptHeader();
	}

	
    /** Creates a new instance of FormulaInterpreter */
    public FormulaInterpreter() {
        SCRIPT_ENGINE = new ScriptEngineManager().getEngineByName( "JavaScript" );		// standard scrip engine shipped with Java
		if ( SCRIPT_ENGINE == null ) {
			System.err.println( "Error: JavaScript engine is missing!" );
			throw new RuntimeException( "JavaScript engine is missing and needed for the Formula Interpreter!" );
		}

		try {
			SCRIPT_ENGINE.eval( STANDARD_SCRIPT_HEADER );
			_formula = "";
		}
		catch ( ScriptException exception ) {
			throw new RuntimeException( exception );
		}
    }


	/** getnerate the standard script header */
	static private String generateStandardScriptHeader() {
		final StringBuffer buffer = new StringBuffer();
		appendMappedMathFunctionToGlobal( "min", buffer );
		appendMappedMathFunctionToGlobal( "max", buffer );
		appendMappedMathFunctionToGlobal( "abs", buffer );
		appendMappedMathFunctionToGlobal( "pow", buffer );
		appendMappedMathFunctionToGlobal( "exp", buffer );
		appendMappedMathFunctionToGlobal( "log", buffer );
		appendMappedMathFunctionToGlobal( "sin", buffer );
		appendMappedMathFunctionToGlobal( "cos", buffer );
		appendMappedMathFunctionToGlobal( "tan", buffer );
		appendMappedMathFunctionToGlobal( "asin", buffer );
		appendMappedMathFunctionToGlobal( "acos", buffer );
		appendMappedMathFunctionToGlobal( "atan", buffer );
		return buffer.toString();
	}


	/** append the mapped math function to the specified buffer */
	static private void appendMappedMathFunctionToGlobal( final String name, final StringBuffer buffer ) {
		buffer.append( mapMathFunctionToGlobal( name ) );
	}


	/** Map the Math function to a global function */
	static private String mapMathFunctionToGlobal( final String name ) {
		return "var " + name + " = Math." + name + "; ";
	}
    
        
    /** 
     * Set the named variable to the specified value.  Overrides the previous value (if any) of the variable.
     * @param name      name of the variable
     * @param value     value assigned to the variable
     */
    final public void setVariable( final String name, final double value ) {
        SCRIPT_ENGINE.put( name, value );
    }
    
    
    /**
     * Determine if a variable of the specified name exists and has been assigned a value.
     * @param name The name of the variable for which to check.
     * @return true if the variable exists; false otherwise.
     */
    final public boolean hasVariable( final String name ) {
        return SCRIPT_ENGINE.get( name ) != null;
    }


	/** compile the specified formula */
	public void compile( final String formula ) {
		_formula = formula;
	}
    
    
    /** 
     * Compile and evaluate the specified formula.
     * @return The result of evaluating the formula with the present variable values.
     */
    public double evaluate( final String formula ) {
		compile( formula );
		return performEvaluation( formula );
    }


	/** evaluate the compiled formula */
	public double evaluate() {
		return performEvaluation( _formula );
	}


	/** perform the evaluation internally */
	private double performEvaluation( final String formula ) {
		try {
			final Object result = SCRIPT_ENGINE.eval( formula );
			if ( result instanceof Number ) {
				return ((Number)result).doubleValue();
			}
			else {
				throw new RuntimeException( "Formula does not evaluate to a number." );
			}
		}
		catch ( ScriptException exception ) {
			throw new RuntimeException( exception );
		}
	}
}
