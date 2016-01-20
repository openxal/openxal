/**
 * 
 */
package xal.app.machinesimulator;

import java.util.HashMap;
import java.util.Map;

/**
 * @author luxiaohan
 *This class define a Model vector parameter with label name,key path which map to the parameter
 */
public class ModelVectorParameter extends Parameter {
	/**the key path map to this parameter*/
	private String keyPath;
	/**the prefix of the key path*/
	private String keyPrefix;
	/**the suffix of the key path*/
	private String keySuffix = null;
	/**the name of the specified parameter*/
	private String parameterName = null;
	/**the plane of the specified parameter*/
	private String plane="0";
	/** a symbol to represent the parameter*/
	private String symbol;
	/**a map array to hold all the key path */
	private Map<String, String> keyPaths;
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param symbol a symbol to represent the parameter
	 * @param keyPrefix the prefix of the key path
	 */
	public ModelVectorParameter( final String label, final String symbol, final String keyPrefix ) {
		super( label );
		this.keyPrefix = keyPrefix;
		this.symbol = symbol;
	}
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param symbol a symbol to represent the parameter
	 * @param keyPrefix the prefix of the key path
	 * @param keySuffix the suffix of the key path  
	 */
	public ModelVectorParameter(final String label,final String symbol,final String keyPrefix,final String keySuffix ) {
		super(label);
		this.keyPrefix = keyPrefix;
		this.keySuffix = keySuffix;
		this.symbol = symbol;
	}
	/**
	 * get the label of the parameter
	 * @return
	 */
	public String getLabel(){
		return super.getLabel();
	}
	/**
	 * get the key path of the parameter for x-plane
	 * @return the key path
	 */
	public String getKeyPathForX(){
		if( keySuffix == null ) keyPath=keyPrefix+".toArray."+"0";
		else keyPath=keyPrefix+"."+"0"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * get the key path of the parameter for y-plane
	 * @return the key path
	 */
	public String getKeyPathForY(){
		if( keySuffix == null ) keyPath=keyPrefix+".toArray."+"1";
		else keyPath=keyPrefix+"."+"1"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * get the key path of the parameter for z-plane
	 * @return the key path
	 */
	public String getKeyPathForZ(){
		if( keySuffix == null ) keyPath=keyPrefix+".toArray."+"2";
		else keyPath = keyPrefix+"."+"2"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * put the the key path of all planes to a map list
	 * @return key paths
	 */
	public Map<String,String> getKeyPathToArray(){
		keyPaths=new HashMap<String,String>(3);
		keyPaths.put( "X",getKeyPathForX() );
		keyPaths.put( "Y",getKeyPathForY() );
		keyPaths.put( "Z",getKeyPathForZ() );
		return keyPaths;
	}
	/**
	 * get the name of the specified parameter
	 * @param keyPath the key path 
	 * @return the name
	 */
	public String getParameterName( final String keyPath ) {
		String label=super.getLabel();
		if( keyPath.equals( getKeyPathForX() ) ) parameterName=label+".X";
		else if( keyPath.equals( getKeyPathForY() ) ) parameterName=label+".Y";
		else if( keyPath.equals( getKeyPathForZ() ) ) parameterName=label+".Z";
		return parameterName;
	}
	/**
	 * get a symbol to represent parameter
	 * @return a symbol
	 */
	public String getSymbol() {
		return "<html>&"+symbol+"</html>";
	}
	/**
	 * get a symbol for x plane to represent parameter's name 
	 * @return a symbol
	 */
	public String getSymbolForX(){
		String symbolX;
		if ( symbol == "" ) symbolX = "<html>"+super.getLabel()+"<sub>x</sub></html>";
		else symbolX = "<html>&"+symbol+";<sub>x</sub></html>";
		return symbolX;
	}
	/**
	 * get a symbol for y plane to represent parameter's name 
	 * @return a symbol
	 */
	public String getSymbolForY(){
		String symbolY;
		if ( symbol == "" ) symbolY = "<html>"+super.getLabel()+"<sub>y</sub></html>";
		else symbolY = "<html>&"+symbol+";<sub>y</sub></html>";
		return symbolY;
	}
	/**
	 * get a symbol for z plane to represent parameter's name 
	 * @return a symbol
	 */
	public String getSymbolForZ(){
		String symbolZ;
		if ( symbol == "" ) symbolZ = "<html>"+super.getLabel()+"<sub>z</sub></html>";
		else symbolZ = "<html>&"+symbol+";<sub>z</sub></html>";
		return symbolZ;
	}
	/**
	 * get the plane of the specified parameter
	 * @param the key path 
	 * @return the plane
	 */
	public String getPlane( final String keyPath ){
		if( keyPath.equals( getKeyPathForX() ) ) plane="X";
		else if( keyPath.equals( getKeyPathForY() ) ) plane="Y";
		else if( keyPath.equals( getKeyPathForZ() ) ) plane="Z";;
		return plane;
	}
	/**
	 * Identify whether the key path map to this parameter
	 * @param keyPath the key path
	 * @return true or false
	 */
	public boolean isThisParameter( final String keyPath ) {
		boolean result=false;
		if( keyPath.equals( getKeyPathForX() )|keyPath.equals( getKeyPathForY() )|keyPath.equals(getKeyPathForZ() ) ) result=true ;
		return result;
	}

}
