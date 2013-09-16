/*
 * @@COPYRIGHT@@
 */
package xal.tools.swing.wheelswitch;

import java.util.EventListener;

/**
 * This interface is used to notify the listeners of value change.
 * 
 * @author Ales Pucelj
 * @version @@VERSION@@
 */
public interface SetListener extends EventListener {

	/**
	 * Notifies that a set command has occured.
	 * 
	 * @param e SetEvent
	 */
	void setPerformed(SetEvent e);

}
