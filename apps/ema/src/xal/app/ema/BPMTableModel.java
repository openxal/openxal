/*
 * SummayTableModel.java
 *
 * Created on March 16, 2005
 */

package xal.app.ema;

import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import java.text.*;
	
/** class to handle the bpm energy calculaton info table */
public class BPMTableModel  extends AbstractTableModel {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	    /** The analysisStuff this table pertains to */
	    private EmaDocument theDoc;
	    
	    /** the controller for the document */
	    protected BPMController bpmController;
	
	    //-------------- Member variables --------------//
	    /** The column name holder */
	    private String [] columnNames= {"BPM pair", "E (MeV)", "Mean (MeV)", "Sigma (MeV)", "N counts"};
	
	    // ---Constructor -//
	
	/** number or rows == number of variables */
	    private int nTableRows;
	    
	    public BPMTableModel (EmaDocument doc) {
		    theDoc = doc;
		    bpmController = theDoc.getBPMController();
	    }
	    
	   /** get the column names */
	
	    public String getColumnName(int col) 
	    { return columnNames[col]; }

	    /** Number of rows so far */
	    public int getRowCount() { 
		    return bpmController.selectedPairs.size();}
	
	    /** column count */
	    public int getColumnCount() { return columnNames.length;}
	
	    /** no cells are editable here */
	    public boolean isCellEditable(int row, int col) { 
		   return false;
	    }
	    
	    /** get the column class */
	    public Class<?> getColumnClass(int c) {
		if(c == 0) 
			return String.class;
		else if(c == 4)
			return Integer.class;
		else
			return Double.class;
	    }

	     /** the value distributor */
	
	    public Object getValueAt(int row, int col) 
	    { 
		String name = bpmController.selectedPairNames.get(row);
		
		if(col == 0) {
			return name;
		}
		if(col == 1) {
			return (bpmController.selectedPairs.get(name)).getEnergy();
		}
		
		if(col == 2) {
			return new Double((bpmController.selectedPairs.get(name)).getMean());
		}
		if(col == 3) {
			return new Double((bpmController.selectedPairs.get(name)).getSigma());
		}
		if(col == 4) {
			return  (bpmController.selectedPairs.get(name)).getCounts();
		}
		return null;
	    }
	    
	    /** return a text representation of the table contents */
	    protected String getText() {
		    String str = "";
		    // header
		    for (int l = 0; l< getColumnCount(); l++)
			    str += columnNames[l] + "\t";
		    str += "\n";
		    // contents:
		    for (int i = 0; i < getRowCount(); i++) {
			    String str1 = "";
			    for (int j = 0; j< getColumnCount(); j++) { 
				    Object obj = getValueAt(i,j);
				    if(obj != null) 
					    str1 += obj.toString() + "\t";
				    else
					    str1 += "\t\t";
			    }
			    str += str1 + "\n";
		    }
		    return str;
	    }
}
