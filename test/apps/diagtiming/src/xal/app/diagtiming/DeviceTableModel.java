package xal.app.diagtiming;

import javax.swing.table.AbstractTableModel;
import java.util.*;

public class DeviceTableModel extends AbstractTableModel {
	static final long serialVersionUID = 0;

	/** the PV handle names */
	private String[] columnNames;

	/** number of devices in the seqence + header + trailer for plot buttons */
	private int nRows;

	/** number of table columns */
	private int nColumns;

	/** The data holders */
	private Object[][] dataMatrix;

	/** Container for row labels */
	private ArrayList<String> rowNames;

	/** constructor */
	public DeviceTableModel(String[] colNames, int numRows) {
		columnNames = colNames;
		nRows = numRows;
		nColumns = colNames.length;
		dataMatrix = new Object[numRows][nColumns];
		rowNames = new ArrayList<String>(nRows);
	}

	/** method to store the PVCells */
	public void addPVCell(InputPVTableCell pvCell, int row, int col) {
		dataMatrix[row][col] = pvCell;
	}

	/** method to store the PVCells */
	public void addPVCell(ComboBoxPVCell pvCell, int row, int col) {
		dataMatrix[row][col] = pvCell;
	}

	/** method to add a row name */
	public void addRowName(String name, int row) {
		rowNames.add(row, name);
	}

	// The table model interface methods:

	public boolean isCellEditable(int row, int col) {
		// for linac table
		if (getColumnCount() == 11) {
			if (col < 6) {
				return false;
			} else {
				return true;
			}
		}
		// for BCM
		else if (getColumnCount() == 1) {
			return false;
		}
		// for ring table
		else {
			 if (col == 0) {
			 return false;
			 }
			 else
			return true;
		}
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

	public Object getValueAt(int row, int col) {

		if (col == 0) {
			return rowNames.get(row);
		}
		else {
		// for linac BPMs
		if (getColumnCount() == 11) {
			if (col > 0 && col < 6)
				return ((InputPVTableCell) dataMatrix[row][col]);
			else
				return ((String) dataMatrix[row][col]);
		}
		// for ring BPMs
		else {
			if (col > 0 && col < 20 && col != 3 && col != 5 && col != 8 
					&& col != 11 && col != 14
					&& col != 6 && col != 9 && col != 12 && col != 15 && col != 16)
				return ((InputPVTableCell) dataMatrix[row][col]);
			else if (col == 3 || col == 5 || col == 8 || col == 11 
					|| col == 14 || col == 6 || col == 9 || col ==12 
					|| col == 15 || col == 16)
				return ((ComboBoxPVCell) dataMatrix[row][col]);
			else
				return ((String) dataMatrix[row][col]);
		}
		}
	}

	public void setValueAt(Object value, int row, int col) {

		// for linac BPMs
		if (getColumnCount() == 11) {

			// Don't do anything if it's an InputPVTableCell
			if (col > 5) {
				dataMatrix[row][col] = (String) value;
			}

		}
		// for ring BPMs
		else {
			if (col > 0 && col < 20 && col != 3 && col != 5 && col != 8 
					&& col != 11 && col != 14
					&& col != 6 && col != 9 && col != 12 && col != 15 && col != 16) {
				// if (!updateInd)
				((InputPVTableCell) dataMatrix[row][col]).setUpdating(false);
				((InputPVTableCell) dataMatrix[row][col])
						.setCellValue((String) value);
			} else if (col == 3 || col == 5 || col == 8 || col == 11 
					|| col == 14 || col == 6 || col == 9 || col ==12
					|| col == 15 || col == 16) {
				((ComboBoxPVCell) dataMatrix[row][col]).setUpdating(false);
				// handle the cell properly in the setCellValue()
				((ComboBoxPVCell) dataMatrix[row][col])
						.setCellValue((String) value);				
			} else if (col == 1) {
				if (((InputPVTableCell) dataMatrix[row][col]).recordValue.doubleValue() > 0.) {
					((InputPVTableCell) dataMatrix[row][col]).setCellValue((String) value);
				}
			}
		}

	}

}
