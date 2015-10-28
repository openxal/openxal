/**
 * 
 */
package xal.app.machinesimulator;

/**
 * @author luxiaohan
 *this class define a scalar parameter with label name, key path which map to the parameter
 */
public class ScalarParameter extends Parameter {
	/**the key path map to this parameter*/
	private String keyPath;
	/**the name of the specified parameter*/
	private String parameterName;
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param key the key path
	 */
	public ScalarParameter( final String label, final String key ) {
		super( label );
		keyPath=key;
	}
	/**
	 * get the name of the specified parameter
	 * @param keyPath the key path 
	 * @return the name
	 */
	public String getParameterName( final String keyPath ) {
		if( isThisParameter( keyPath ) ) parameterName=super.getLabel();
		else parameterName=null;
       return parameterName;
	}
	/**
	 * get the key path map to this parameter
	 * @return the key path
	 */
	public String getKeyPath(){
		return keyPath;
	}
	/**
	 * get a symbol ( for scalar parameter just return the label for now )
	 * @param the key path
	 */
	public String getSymbol() {
		return super.getLabel();
	}
	/**
	 * get the plane of the parameter
	 * this method is to stay the same with VectorParameter
	 * sometimes we need to set different property in different plane
	 * but the vector parameter and scalar parameter mix together
	 * we don't want to write another code to separate them.
	 * so just add a method in ScalarParameter to return a invalid character.
	 */
	public String getPlane( final String keyPath ){
		return "0";
	}
	/**
	 * Identify whether the key path map to this parameter
	 * @param keyPath the key path
	 * @return true or false
	 */
	public boolean isThisParameter( final String keyPath ){
		return this.keyPath.equals( keyPath );
	}


}
