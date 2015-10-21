/**
 * 
 */
package xal.app.machinesimulator;

/**
 * @author luxiaohan
 *this class define a vector parameter with label name,key path which map to the parameter
 */
public class VectorParameter extends Parameter {
	/**the key path map to this parameter*/
	private String keyPath;
	/**the prefix of the key path*/
	private String keyPrefix;
	/**the suffix of the key path*/
	private String keySuffix=null;
	/**the name of the specified parameter*/
	private String parameterName;
	/**the plane of the specified parameter*/
	private String plane;
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param keyPrefix the prefix of the key path
	 */
	public VectorParameter(final String label,final String keyPrefix ) {
		super(label);
		this.keyPrefix=keyPrefix;
		
	}
	
	/**
	 * Constructor
	 * @param label the label of parameter
	 * @param keyPrefix the prefix of the key path
	 * @param keySuffix the suffix of the key path  
	 */
	public VectorParameter(final String label,final String keyPrefix,final String keySuffix) {
		super(label);
		this.keyPrefix=keyPrefix;
		this.keySuffix=keySuffix;
		
	}
	/**
	 * get the label of the parameter
	 * @return
	 */
	public String getLabel(){
		return super.getLable();
	}
	/**
	 * get the key path of the parameter for x-plane
	 * @return the key path
	 */
	public String getKeyPathForX(){
		if(keySuffix==null) keyPath=keyPrefix+".toArray."+"0";
		else keyPath=keyPrefix+"."+"0"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * get the key path of the parameter for y-plane
	 * @return the key path
	 */
	public String getKeyPathForY(){
		if(keySuffix==null) keyPath=keyPrefix+".toArray."+"1";
		else keyPath=keyPrefix+"."+"1"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * get the key path of the parameter for z-plane
	 * @return the key path
	 */
	public String getKeyPathForZ(){
		if(keySuffix==null) keyPath=keyPrefix+".toArray."+"2";
		else keyPath=keyPrefix+"."+"2"+"."+keySuffix;
		return keyPath;
	}
	/**
	 * get the name of the specified parameter
	 * @param keyPath the key path 
	 * @return the name
	 */
	public String getParameterName(final String keyPath) {
		if(isThisParameter(keyPath)){
			String label=super.getLable();
			if(keyPath.equals(getKeyPathForX())) parameterName=label+"X";
			if(keyPath.equals(getKeyPathForY())) parameterName=label+"Y";
			if(keyPath.equals(getKeyPathForZ())) parameterName=label+"Z";
		}
		else parameterName=null;
		return parameterName;
	}
	/**
	 * get the plane of the specified parameter
	 * @param the key path 
	 * @return the plane
	 */
	public String getPlane(final String keyPath){
		if(isThisParameter(keyPath)){
			if(keyPath.equals(getKeyPathForX())) plane="X";
			if(keyPath.equals(getKeyPathForY())) plane="Y";
			if(keyPath.equals(getKeyPathForZ())) plane="Z";
		}
		else plane="0";
		return plane;
	}
	/**
	 * Identify whether the key path map to this parameter
	 * @param keyPath the key path
	 * @return true or false
	 */
	public boolean isThisParameter(String keyPath) {
		boolean result=false;
		if(keyPath.equals(getKeyPathForX())|keyPath.equals(getKeyPathForY())|keyPath.equals(getKeyPathForZ())) result=true ;
		return result;
	}

}
