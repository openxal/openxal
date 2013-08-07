package xal.app.ringmeasurement;

import java.util.*;
import xal.smf.impl.*;

import javax.swing.table.AbstractTableModel;

public class QuadTableModel extends AbstractTableModel {
	static final long serialVersionUID = 0;

	ArrayList<MagnetMainSupply> allQuads;

	int rowSize = 0;

	private String[] columnNames;

	private Object[][] data;

	/** Container for row labels */
	private ArrayList<String> rowNames = new ArrayList<String>(rowSize);
	
	private boolean isOnline = true;

	public QuadTableModel(ArrayList<MagnetMainSupply> quads, String[] colNames) {
		allQuads = quads;
		rowSize = allQuads.size();
		columnNames = colNames;
		data = new Object[rowSize][columnNames.length];
	}

	public int getRowCount() {
		return data.length;
	}

	public int getColumnCount() {
		return columnNames.length;
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	/** method to add a row name */
	public void addRowName(String name, int row) {
		rowNames.add(row, name);
	}

	/** method to store the PVCells */
	public void addPVCell(InputPVTableCell pvCell, int row, int col) {
		data[row][col] = pvCell;
	}

	public boolean isCellEditable(int row, int col) {
		if (col < 4) {
			return false;
		} else {
			return true;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			return  rowNames.get(rowIndex);
		} else if (columnIndex == 2 || columnIndex == 1) {
			if (isOnline)
				return (InputPVTableCell) data[rowIndex][columnIndex];
			else
				return (String) data[rowIndex][columnIndex];
		} else {
			return (String) data[rowIndex][columnIndex];
		}
	}

	public void setValueAt(Object value, int row, int col) {
		if (col == 3 || col == 1 || col == 4) {
			data[row][col] = (String) value;
		}
		
//		fireTableCellUpdated(row, col);
		return;
	}
	
	protected void setAppMode(boolean ind) {
		isOnline = ind;
	}
}
