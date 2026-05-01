

package xal.app.ringinjection;

import xal.ca.*;

import java.awt.*;
import java.text.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;

public class SteererTableModel extends AbstractTableModel{
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

	/**  the PV handle names */
	private String[] columnNames;

	/** number of devices in the seqence + header + trailer for plot buttons */
	private int nRows;

	/** number of table columns */
	private int nColumns;

	/** The data holders */
	private Object[][] dataMatrix;

	/** Container for the JButtons */
	private ArrayList<JButton> jButtons;

	/** Container for row labels */
	private ArrayList<String> rowNames;

	/** constructor */
	public SteererTableModel(String[] colNames, int numRows){
		columnNames = colNames;
		nRows = numRows;
		nColumns = colNames.length;
		dataMatrix = new Object[numRows][nColumns];
		rowNames = new ArrayList<>(nRows);
		jButtons = new ArrayList<>(nRows);
	}

	//** method to store the JButtons */

	public void addJButton(int rowNumber, JButton button) {
		jButtons.add(rowNumber, button);
	}

	/** method to add a row name */
	public void addRowName(String name, int row) {
		rowNames.add(row, name);
	}

	// The table model interface methods:

	public boolean isCellEditable(int row, int col) {
		if(col < 1){
			return false;
		}
		else{
			return true;
		}
	}

	public String getColumnName(int col){
		return columnNames[col];
	}

	public int getRowCount(){
		return nRows;
	}

	public int getColumnCount(){
		return nColumns;
	}



	public Object getValueAt(int row, int col)
	{
		return (dataMatrix[row][col]);

	}

	public void setValueAt(Object value, int row, int col)
	{
		dataMatrix[row][col] = value;
	}

	public void setTableData(int rows, Object data[][]){
		nRows = rows;
		dataMatrix=data;
		fireTableDataChanged();
	}

	public void updateCell(int row, int col){
		fireTableCellUpdated(row, col);
	}

	public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}

}


