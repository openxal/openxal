/*
 * ProgressRenderer.java
 */

package xal.app.wirescan;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

public class ProgressRenderer implements TableCellRenderer {
	private JProgressBar theProgBar;
	private JLabel nullLabel;

	public ProgressRenderer  () {
		nullLabel = new JLabel("Null");
	}

	public Component getTableCellRendererComponent(JTable table,
	                Object value,
	                boolean isSelected,
	                boolean hasFocus,
	                int row,
	                int column) {
		if(value != null) {
			return (JProgressBar) value;
		}
		else return nullLabel;
	}

	// The following methods override the defaults for performance reasons
	public void validate() {
	}

	public void revalidate() {
	}

	protected void firePropertyChange(String propertyName, Object oldValue,
			Object newValue) {
	}

	/*
	 * public void firePropertyChange(String propertyName, boolean oldValue,
	 * boolean newValue) {}
	 */
	public void repaint(int x, int y, int width, int height) {
	}

	public void repaint(long tm, int x, int y, int width, int height) {
	}

}

