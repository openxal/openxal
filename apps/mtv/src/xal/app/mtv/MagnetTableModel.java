/*
* MagnetTableModel.java
*
* Created on March 16, 2005
*/

package xal.app.mtv;

import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import java.text.*;

/** class to handle the display analysis control variable info table */
public class MagnetTableModel  extends AbstractTableModel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	
	/** The analysisStuff this table pertains to */
	private MagnetPanel magPanel;
	
	//-------------- Member variables --------------//
	/** The column name holder */
	private String [] columnNames= {"Magnet", "B Set Main", "B Set Trim", "B readback", "B Book"};	    
	
	// ---Constructor -//
	
	/** number or rows == number of variables */
	private int nTableRows;
	
	public MagnetTableModel (MagnetPanel mp) {
		magPanel = mp;
	}
	
	/** get the column names */
	
	public String getColumnName(int col) 
	{ return columnNames[col]; }
	
	/** Number of rows so far */
	public int getRowCount() { 
		return magPanel.magnetNames.size();
	}
	
	/** column count */
	public int getColumnCount() { return columnNames.length;}
	
	/** no cells are editable here */
	public boolean isCellEditable(int row, int col) {     
		return true;
	}
	
	/** get the column class */
	public Class<?> getColumnClass(int c) {
		if(c == 0)
			return String.class;
		else
			return PVTableCell.class;
	}
	
	/** the value distributor */
	public Object getValueAt(int row, int col) 
	{ 
		
		if(col == 0) {
			return magPanel.magnetNames.get(row);
		}
		if(col == 1) {
			return magPanel.B_Sets.get(row);
		}		
		if(col == 2) {
			return magPanel.B_Trim_Sets.get(row);
		}
		if(col == 3) {
			return magPanel.B_RBs.get(row);
		}
		if(col == 4) {
			return magPanel.B_Books.get(row);
		}
		return null;
	}
}
