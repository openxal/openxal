/*
 * @@COPYRIGHT@@
 */
package xal.extension.widgets.swing.wheelswitch;

import java.util.EventListener;

import javax.swing.event.ChangeEvent;

/**
 * UpDownListener listens to changes in the UpDownButton 
 * where it is registered.
 * 
 * @author <a href="mailto:jernej.kamenik@cosylab.com">Jernej Kamenik</a>
 * @version $id$
 */
public interface UpDownListener extends EventListener {
		public void upDownChanged(ChangeEvent e);
}
