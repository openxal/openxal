package xal.app.injdumpwizard.utils;


import java.text.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;
import java.util.*;

import xal.ca.*;
import xal.extension.widgets.swing.*;
import xal.tools.text.ScientificNumberFormat;

/**
 * This class is a Table Model to show the Name-Value pairs. 
 * The update of the values should be performed outside the model.
 * We do not use Channel-Value table, because in this application 
 * we also have plain tables. The performance of this table model 
 * is not great, so it should be used only for relatively small
 * numbers of the name-value pair. 
 *
 *@author     shishlo
 */
public class  NameValueTableModel extends AbstractTableModel{
	/** serialization ID */
    private static final long serialVersionUID = 1L;

	private Vector<TableRecord> tb_recV = new Vector<TableRecord>();
	private ScientificNumberFormat format = new ScientificNumberFormat( 6, 12, false );
	private String[] col_names = new String[2];
	
	/**
	*  Constructor for the NameValuesTableModel object
	*/
	public NameValueTableModel(){
		col_names[0] = "PV Name";
		col_names[1] = "Value";		
	}
	
	/**
	*  Returns the number of rows.
	*/
	public int getRowCount(){
		return tb_recV.size();
	}
	
	
	/**
	*  Returns the number of columns.
	*/	
  public int getColumnCount(){
		return 2;
	}
	
	/**
	*  Returns the name of the coulumn.
	*/		
	public String getColumnName(int column) {
		return col_names[column];
	}
	
	/**
	*  Sets the name of the coulumn.
	*/			
	public void setCoulmnName(String cl_name, int column){
		col_names[column] = cl_name;
	}
	
	/**
	*  Returns the value at the certain row and column.
	*/	
  public Object getValueAt(int row, int column){
		switch(column){
		case 0:
			return tb_recV.get(row).getName();
		case 1:
			TableRecord tr = tb_recV.get(row);
			if(tr.getStatus()){
				return format.format(tr.getValue());
			}
			break;
		default:
			break;
		}
		return ""; 
	}
	
	/**
	*  Adds the table record to the table
	*/	
	public void addTableRecord(String name){
		TableRecord trec = new TableRecord(name,0.,false);
		tb_recV.add(trec);
	}

	/**
	*  Returns the vector of records.
	*/	
	public Vector<TableRecord> getRecords(){
		return tb_recV;
	}

	/**
	*  Returns the record with this name or null.
	*/		
	public TableRecord getRecord(String name){
		Iterator<TableRecord> iter = tb_recV.iterator();
		while(iter.hasNext()){
			TableRecord tr = iter.next();
			if(tr.getName().equals(name)){
				return tr;
			}
		}
		return null;
	}
	

	
	
}




