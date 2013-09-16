/*
 * @@COPYRIGHT@@
 */
package xal.tools.swing.wheelswitch;

import xal.tools.swing.wheelswitch.comp.SimpleButton;

/**
 * Descedant of <code>Digit</code> displaying static text.
 * Objects of this class also cannot be selected. 
 * 
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class StaticDigit extends Digit {

	/**
	 * Constructor for StaticDigit.
	 * 
	 * @param value to be displayed.
	 * @see Digit#Digit()
	 */			
	public StaticDigit(String value) {
		super();
		setActionMode(SimpleButton.NULL_ACTION_MODE);
		setText(value);
	}

	/**
	 * This method has been overriden to disable selection of static digits.
	 */
	public void setSelected(boolean newSel){}

	/**
	 * This method has been overriden to disable selection of static digits.
	 */
	public boolean isSelected() {return false;}
}
