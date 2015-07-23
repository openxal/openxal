/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.event.ChangeEvent;
import javax.swing.event.EventListenerList;

import xal.extension.widgets.swing.wheelswitch.comp.ArrowButton;
import xal.extension.widgets.swing.wheelswitch.util.ColorHelper;

/**
 * Descedant of <code>javax.swing.JComponent</code> that contains two 
 * <code>ArrowButton</code>s acting as a two-way (up/down) control. 
 * 
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public class UpDownButton extends JComponent
{
	private static final long serialVersionUID = 1L;

	protected ChangeEvent changeEvent = null;
	
	private int value = 0;

	public static int UP_PRESSED = -1;
	public static int DOWN_PRESSED = -2;
		
	/**
	 * Constructor for UpDownButton.
	 */			
	public UpDownButton() {
		super();
		listenerList = new EventListenerList();	
		setLayout(new UpDownLayout());
		ArrowButton bn = new ArrowButton(ArrowButton.Orientation.UP);
		bn.setFocusable(false);
		bn.setEnabled(isEnabled());
	    bn.setBackground(ColorHelper.getCosyControl());
		bn.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				value = UP_PRESSED;
				fireUpDownChanged();				
			}
		});
		ArrowButton bs = new ArrowButton(ArrowButton.Orientation.DOWN);
		bs.setFocusable(false);
		bs.setEnabled(isEnabled());
	    bs.setBackground(ColorHelper.getCosyControl());
		bs.addActionListener(new ActionListener() {
            @Override
			public void actionPerformed(ActionEvent e) {
				value = DOWN_PRESSED;
				fireUpDownChanged();				
			}
		});
		add(bn);
		add(bs);
	}		

	/**
	 * Returns <code>UpDownButton.UP_PRESSED</code> if the upper arrowed button was last pressed
	 * and <code>UpDownButton.DOWN_PRESSED</code> if the lower arrowed button was last pressed
	 */			
	public int getValue() {
		return value;
	}

  /**
   * Adds an <code>UpDownListener</code> to the array
   * of listeners currently registered for listening to the changes 
   * of the <code>UpDownButton</code>. These listeners are notified
   * whenever the user clicks the <code>UpDownButton</code>.
   * 
   * @param l
   * @see UpDownListener
   */
	public void addUpDownListener(UpDownListener l) {
	   listenerList.add(UpDownListener.class, l);	
	}

  /**
   * Fires a <code>java.swing.event.ChangeEvent</code> to all currently
   * registered <code>UpDownListener</code>s of the <code>UpDownButton</code>.
   */
	public void fireUpDownChanged() {
		Object[] listeners = listenerList.getListenerList();
		for (int i = listeners.length-2; i>=0; i-=2) {
		    if (listeners[i]==UpDownListener.class) {
				if (changeEvent == null) {
					changeEvent = new ChangeEvent(this);
				}
				((UpDownListener)listeners[i+1]).upDownChanged(changeEvent);
		    }	       
		}
	}	

  /**
   * Removes an <code>UpDownListener</code> from the array
   * of listeners currently registered for listening to the
   * changes of the <code>UpDownButton</code>.
   */
	public void removeUpDownListener(UpDownListener l) {
	   listenerList.remove(UpDownListener.class, l);		
	}

  /**
   * This method was overridden to implement correct 
   * <code>UpDownButton</code> placement inside the 
   * <code>Wheelswitch</code> container.
   * 
   * @see JComponent#getPreferredSize()
   */
    @Override
	public Dimension getPreferredSize() {
		return new Dimension(24,48);
	}
	
  /**
   * This method was overridden to implement correct 
   * <code>UpDownButton</code> placement inside the 
   * <code>Wheelswitch</code> container.
   * 
   * @see JComponent#getMinimumSize()
   */
    @Override
	public Dimension getMinimumSize() {
		return new Dimension(12,24);
	}	
	/* (non-Javadoc)
	 * @see java.awt.Component#setEnabled(boolean)
	 */
    @Override
	public void setEnabled(boolean arg0) {
		super.setEnabled(arg0);
		getComponent(0).setEnabled(arg0);
		getComponent(1).setEnabled(arg0);
	}

}

