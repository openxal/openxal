package xal.app.quadshaker;

/**
 *  Description of the Interface
 *
 *@author     shishlo
 */
public interface Dev_Element {

	/**
	 *  Returns the name attribute of the Dev_Element object
	 *
	 *@return    The name value
	 */
	public String getName();

	/**
	 *  Returns the active attribute of the Dev_Element object
	 *
	 *@return    The active value
	 */
	public boolean isActive();

	/**
	 *  Sets the active attribute of the Dev_Element object
	 *
	 *@param  state  The new active value
	 */
	public void setActive(boolean state);

}

