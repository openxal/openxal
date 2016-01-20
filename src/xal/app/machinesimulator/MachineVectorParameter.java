/**
 * 
 */
package xal.app.machinesimulator;

/**
 * @author luxiaohan
 *This class map to a Machine vector parameter
 *the keyPath map to this parameter must contain type, plane and record information e.g.BPM.X.1
 */
public class MachineVectorParameter extends Parameter {
	
	/**parameter name*/
	private String parameterName;
	/**the symbol*/
	private String symbol;
	/**the plane of the specified parameter*/
	private String plane="0";
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param type the type of the parameter
	 */
	public MachineVectorParameter( final String label, final String type ) {
		super( label );
		parameterName = null;
		this.symbol = type;
	}
	
	/**
	 * get the name of the specified parameter
	 * @param keyPath the key path 
	 * @return the name
	 */
	public String getParameterName( final String keyPath ) {
		if ( isThisParameter( keyPath ) ) {
			if ( keyPath.contains("x") || keyPath.contains("X") ) parameterName = symbol + ".X";
			else if ( keyPath.contains("y") || keyPath.contains("Y") ) parameterName = symbol + ".Y";
			else if ( keyPath.contains("z") || keyPath.contains("Z") ) parameterName = symbol + ".Z";
		}

		return parameterName;
	}
	/**
	 * get a symbol to represent parameter
	 * @return a symbol
	 */
	public String getSymbol() {
		return symbol;
	}
	/**
	 * get the plane of the specified parameter
	 * @param the key path 
	 * @return the plane
	 */
	public String getPlane( final String keyPath ) {
		if ( keyPath.contains("x") || keyPath.contains("X") ) plane = "X";
		else if ( keyPath.contains("y") || keyPath.contains("Y") ) plane = "Y";
		else if ( keyPath.contains("z") || keyPath.contains("Z") ) plane = "Z";
		return plane;
	}
	/**
	 * Identify whether the key path map to this parameter
	 * @param keyPath the key path
	 * @return true or false
	 */
	public boolean isThisParameter( final String keyPath ) {
		boolean result=false;
		if ( keyPath.contains( symbol ) ) result = true;
		return result;
	}

}
