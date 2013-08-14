/*
* PVTableModel.java
*
* Created on March 16, 2005
*/

package xal.app.mtv;

import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import java.text.*;

/** class to handle the display analysis control variable info table */
public class PVTableModel  extends AbstractTableModel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
	
	private PVsPanel pvPanel;
	
	//-------------- Member variables --------------//
	/** The column name holder */
	private String [] columnNames= {"PV Name", "Value"};	    
	
	// ---Constructor -//
	public PVTableModel (PVsPanel pvPanel_in) {
		pvPanel = pvPanel_in;
	}
	
	/** get the column names */
	
	public String getColumnName(int col) 
	{ return columnNames[col]; }
	
	/** Number of rows so far */
	public int getRowCount() { 
		return pvPanel.getCellsNumber();
	}
	
	/** column count */
	public int getColumnCount() { return columnNames.length;}
	
	/** no cells are editable here */
	public boolean isCellEditable(int row, int col) {     
		return true;
	}
	
	/** get the column class */
	public Class<?> getColumnClass(int c) {
		if(c ==0)
			return String.class;
		else
			return PVTableCell.class;
	}
	
	/** the value distributor */
	public Object getValueAt(int row, int col) 
	{ 
		PVTableCell cell = pvPanel.getCell(row);
		if(cell.isDummy()) return null;
		if(col == 0) {
				return cell.getChannelWrapper().getId();
		}
		if(col == 1) {
			return cell;
		}		
		return null;
	}
}
