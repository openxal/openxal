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
 * This class sets up a post scan sigma fit table.
 *
 * @author	S. Bunch
 * @version	1.0
 * @see AbstractTableModel
 * @see WireData
 */
public class PostTableModel  extends AbstractTableModel
{
    private static final long serialVersionUID = 1L;
    
	private final String[] columnNames = {"Name","VFIT","DFIT","HFIT"};
	private Object[][] data;
	private WireDoc theDoc;
	private Object theID;
	private WireData wd;
	/**
	 * Constructor for WireDataTableModel.
	 *
	 */
	public PostTableModel(WireDoc wiredocument) {
		theDoc = wiredocument;
//		wd = (WireData) theDoc.wireDataMap.get(theID);
		data = new Object[theDoc.selectedWires.size()][4];
		int rows = 0;
		while(rows < theDoc.selectedWires.size()) {
			theID = (Object) (( theDoc.selectedWires.get(rows)).getId());
			data[rows][0] = theID;
			wd = theDoc.wireDataMap.get(theID);
			data[rows][1] = new Double(wd.xsigmaf);
			data[rows][2] = new Double(wd.ysigmaf);
			data[rows][3] = new Double(wd.zsigmaf);
			rows++;
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
	public int getRowCount() { return theDoc.selectedWires.size();}

	/**
	 * Returns the number of columns.
	 * Part of the AbstractTableModel interface
	 * @return			Number of columns
	 * @see AbstractTableModel
	 */
	public int getColumnCount() { return 4;}

	/**
	 * Returns the data.
	 * Part of the AbstractTableModel interface
	 * @param row		Row to get from
	 * @param col		Column to get from
	 * @return			The object in the specified row, col
	 * @see AbstractTableModel
	 */
	public Object getValueAt(int row, int col) {
	    //	    System.out.println(row + "  " + col);
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
