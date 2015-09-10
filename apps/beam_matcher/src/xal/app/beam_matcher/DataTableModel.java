package xal.app.beam_matcher;

import xal.ca.*;
import xal.smf.impl.BPM;
import java.awt.*;
import java.text.*;
import javax.swing.table.AbstractTableModel;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.*;
import java.util.*;

public class DataTableModel extends AbstractTableModel{
    
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
    /**  the PV handle names */
    private String[] columnNames;
    
    /** number of devices in the seqence  header  trailer for plot buttons */
    private int nRows;
    
    /** number of table columns */
    private int nColumns;
    
    /** The data holders */
    //private Object[][] dataMatrix;
    private ArrayList<ArrayList<Object>> dataArray;
    
    /** Container for the JButtons */
    private ArrayList<JButton> jButtons;
    
    /** Container for row labels */
    private ArrayList<String> rowNames;
    
    /** Container for BPM agents **/
    private ArrayList<BPM> agents;
    
    /** constructor */
    public DataTableModel(String[] colNames, int numRows){
        columnNames = colNames;
        nRows = numRows;
        nColumns = colNames.length;
        
        dataArray = new ArrayList<ArrayList<Object>>();
        rowNames = new ArrayList<String>(nRows);
        jButtons = new ArrayList<JButton>(nRows);
        agents = new ArrayList<BPM>(nRows);
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
        return dataArray.size();
    }
    
    public int getColumnCount(){
        return nColumns;
    }
    
    
    public Object getValueAt(int row, int col)
    {
        return (dataArray.get(row)).get(col);
    }
    
    public void setValueAt(Object value, int row, int col)
    {
        ArrayList<Object> data = dataArray.get(row);
        data.set(col, value);
    }
    
    public void setTableData(int rows, ArrayList<ArrayList<Object>> data, ArrayList<BPM> agentList){
        nRows = rows;
        dataArray=data;
        fireTableDataChanged();
    }
    
    public void addTableData(ArrayList<Object> data){
        dataArray.add(data);
    }
    
    public void updateCell(int row, int col){
        fireTableCellUpdated(row, col);
    }
    
    public Class<?> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
    
    public void clearAllData(){
        
    }
    
}
