package xal.app.sclmonitor;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class RFTableModel extends AbstractTableModel {
	
	static final long serialVersionUID = 0;
	
	/** number of devices in the seqence + header + trailer for plot buttons */
	private int nRows;

	/** number of table columns */
	private int nColumns;

	/** The data holders */
	private Object[][] dataMatrix;

	/** the PV handle names */
	private String[] columnNames;
	
	/** Container for row labels */
	private ArrayList<String> rowNames;
	
	/** constructor */
	public RFTableModel(String[] colNames, int numRows) {
		columnNames = colNames;
		nRows = numRows;
		nColumns = colNames.length;
		dataMatrix = new Object[numRows][nColumns];
		rowNames = new ArrayList<String>(nRows);
	}	

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public int getRowCount() {
		return nRows;
	}

	public int getColumnCount() {
		return nColumns;
	}

	/** method to add a row name */
	public void addRowName(String name, int row) {
		rowNames.add(row, name);
	}

	/** method to store the PVCells */
	public void addPVCell(InputPVTableCell pvCell, int row, int col) {
		dataMatrix[row][col] = pvCell;
	}

	public Object getValueAt(int row, int col) {

		if (col == 0)
			return rowNames.get(row);

		else if (col == 1 || col == 3)
			return ((InputPVTableCell) dataMatrix[row][col]);
		else
			return ((String) dataMatrix[row][col]);
	}
	
	public void setValueAt(Object value, int row, int col) {

		// Don't do anything if it's an InputPVTableCell
		if (col == 2 || col == 4) {
			dataMatrix[row][col] = (String) value;
		}

		
	}

}
