/**
 * 
 */
package xal.app.machinesimulator;


/**
 * @author luxiaohan
 * Parameter is a abstract class which 
 */
public abstract class Parameter {
	/**the label of parameter*/
	private String label;
	
	/**Empty constructor*/
	public Parameter(){		
	}
	
	/** Constructor*/
	public Parameter( final String label ){
		this.label=label;
	}
	/**get the label*/
	public String getLabel(){
		return this.label;
	}
	
	/**
	 * get the name of the specified parameter
	 * @param object 
	 * @return The name
	 */
	public abstract String getParameterName( final String object );
	/**
	 * get a symbol to represent parameter's name 
	 * @return a symbol
	 */
	public abstract String getSymbol();
	
	/**
	 * get the plane of the specified parameter
	 * @param object
	 * @return The plane
	 */
	public abstract String getPlane( final String object );
	
	/**
	 * Identify whether the key path map to this parameter
	 * @param keyPath the key path
	 * @return
	 */
	public abstract boolean isThisParameter( final String keyPath );


}
