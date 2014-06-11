/*
 * WireTableModel.java
 */

package xal.app.wirescan;

import java.util.ArrayList;

import javax.swing.JProgressBar;
import javax.swing.table.AbstractTableModel;

import xal.extension.application.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * This class sets up the main control panel table.
 * The table is propagated with the IDs of the wirescanners in
 * the current selected sequence, a check box to select the
 * wirescanner for a scan run, and a progress bar to show the
 * current progress of a scan.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see AbstractTableModel
 */
public class WireTableModel  extends AbstractTableModel
{
    
    private static final long serialVersionUID = 1L;
    
	private boolean firstUse = true;
	private WireDoc theDoc;
	private ArrayList<Object> saveStates = new ArrayList<Object>();
	private final String[] columnNames = {"Wirescanner(s)","Select","Relative Wire Position"};
	private Object[][] data;

	/**
	 * The WireTableModel constructor.
	 * @param wiredocument	The WireDoc to get data from
	 */
	public WireTableModel(WireDoc wiredocument) {
		theDoc = wiredocument;
		data = new Object[theDoc.wirescanners.size()][3];
		int rows = 0;
		while(rows < theDoc.wirescanners.size()) {
			data[rows][0] = (Object) ((theDoc.wirescanners.get(rows)).getId());
			data[rows][2] = new JProgressBar();
			if (firstUse){
				data[rows][1] = new Boolean(false);
				saveStates.add(data[rows][1]);
			}
			else {
				data[rows][1] = (Boolean) saveStates.get(rows);
			}
			rows++;
		}
		firstUse = false;
	}

	/**
	 * Returns the name of each column.
	 * Part of the AbstractTableModel interface
	 * @param col		Column index
	 * @return			Names of each column in the table
	 * @see AbstractTableModel
	 */
	public String getColumnName(int col)
	{ return columnNames[col]; }

	/**
	 * Returns the number of rows.
	 * Part of the AbstractTableModel interface
	 * @return			Number of rows
	 * @see AbstractTableModel
	 */
	public int getRowCount() { return theDoc.wirescanners.size();}

	/**
	 * Returns the number of columns.
	 * Part of the AbstractTableModel interface
	 * @return			Number of columns
	 * @see AbstractTableModel
	 */
	public int getColumnCount() { return 3;}

	/**
	 * Returns the data.
	 * Part of the AbstractTableModel interface
	 * @param row		Row to get from
	 * @param col		Column to get from
	 * @return			The object in the specified row, col
	 * @see AbstractTableModel
	 */
	public Object getValueAt(int row, int col) {

		return data[row][col];
	}

	/**
	 * Sets data in the table.
	 * Part of the AbstractTableModel interface
	 * @param value		Value to set
	 * @param row		Row to set data in
	 * @param col		Column to set data in
	 * @see AbstractTableModel
	 */
	public void setValueAt(Object value, int row, int col) {
		data[row][col] = value;
		fireTableCellUpdated(row, col);
	}

	/**
	 * Sets the current position for the progress bar.
	 * @param progbar	The progress bar to update.
	 * @param value	The current position to set the progress bar to.
	 */
	public void setCurrentValueAt(JProgressBar progbar, int value) {
		progbar.setValue(value);
		fireTableDataChanged();
	}

	/**
	 * Sets the maximum value for the progress bar.
	 * @param value	The maximum value to set the progress bar to
	 * @param row	The row containing the progress bar to set
	 */
	public void setMaxValueAt(double value, int row) {
		((JProgressBar) data[row][2]).setMaximum((int) value);
	}

	/**
	 * Returns the class.
	 * Part of the AbstractTableModel interface
	 * @param c		Column to get from
	 * @return		The class of the specified column
	 * @see AbstractTableModel
	 */
	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

	/**
	 * Returns whether or not the specified cell can be edited.
	 * Part of the AbstractTableModel interface
	 * @param row		Row to check
	 * @param col		Column to check
	 * @return			boolean value if the cell is editable or not
	 * @see AbstractTableModel
	 */
	public boolean isCellEditable(int row, int col) {
		if (col == 0) {
			return false;
		} else {
			return true;
		}
	}
}
