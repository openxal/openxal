/*
 * WireDataTableModel.java
 */

package xal.app.wirescan;

import javax.swing.table.AbstractTableModel;

import xal.extension.application.*;
import xal.smf.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;

/**
 * This class sets up the post scan data table.
 * For each position, X, Y, and Z there are fit and rms data from a successful
 * wirescanner scan.  Each of the six position data also has six data associated
 * with it.  These are:
 * <ul>
 *	<li>Area</li>
 *	<li>Amplitude</li>
 *	<li>Mean</li>
 *	<li>Sigma</li>
 *	<li>Offset</li>
 *	<li>Slope</li>
 * </ul>
 * Each data is from the WireData class.
 *
 *
 * @author	S. Bunch
 * @version	1.0
 * @see AbstractTableModel
 * @see WireData
 */
public class WireDataTableModel  extends AbstractTableModel
{
    
    private static final long serialVersionUID = 1L;
    
	private final String[] columnNames = {"Name","VFIT","VRMS","DFIT","DRMS","HFIT","HRMS"};
	private Object[][] data;

	/**
	 * Constructor for WireDataTableModel.
	 *
	 */
	public WireDataTableModel() {
		data = new Object[6][7];
		data[0][0] = new String("Area");
		data[1][0] = new String("Amplitude");
		data[2][0] = new String("Mean");
		data[3][0] = new String("Sigma");
		data[4][0] = new String("Offset");
		data[5][0] = new String("Slope");
		/* Initialize table data */
		for(int i=0; i < 6; i++) {
			for(int j=1; j < 7; j++) {
				data[i][j] = new Double(0.0);
			}
		}
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
	public int getRowCount() { return 6;}

	/**
	 * Returns the number of columns.
	 * Part of the AbstractTableModel interface
	 * @return			Number of columns
	 * @see AbstractTableModel
	 */
	public int getColumnCount() { return 7;}

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
	public void setValueAt(Double value, int row, int col) {
		data[row][col] = (Object) value;
		fireTableCellUpdated(row, col);
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
		return false;
	}
}
