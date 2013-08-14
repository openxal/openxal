package xal.app.wireanalysis;

import xal.ca.*;

import java.awt.*;
import java.text.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;

public class ResultsTableModel extends AbstractTableModel{
    
    /** ID for serializable version */
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
    public ResultsTableModel(String[] colNames, int numRows){
        columnNames = colNames;
        nRows = numRows;
        nColumns = colNames.length;
        dataMatrix = new Object[numRows][nColumns];
        rowNames = new ArrayList<String>(nRows);
        jButtons = new ArrayList<JButton>(nRows);
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
        if(col < 5){
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
        //BPMAgent bpmagent = new BPMAgent();
        //bpmagent = (BpmAgent)agents.get(row);
        //if(col == nColumns-1) {
	    //return ((BpmAgent) agents.get(row));
	    //return bpmagent;
        //}
        //else {
        return (dataMatrix[row][col]);
        //}
        
    }
    
    public void setValueAt(Object value, int row, int col)
    {
        dataMatrix[row][col] = value;
    }
    
    public void setTableData(Object data[][]){
        dataMatrix=data;
        fireTableDataChanged();
    }
    
    public void updateCell(int row, int col){
        fireTableCellUpdated(row, col);
    }
    
	public void clearAllData(){
		dataMatrix = new Object[nRows][nColumns];
	}
    //public Class getColumnClass(int c) {
	//return getValueAt(0, c).getClass();
    //}
    
}