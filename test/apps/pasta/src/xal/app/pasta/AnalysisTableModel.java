/*
 * AnalysisTableModel.java
 *
 * Created on June 14, 2004
 */

package xal.app.pasta;

import java.awt.*;
import java.util.*;
import javax.swing.table.*;
import java.text.*;
	
/** class to handle the display analysis control variable info table */
public class AnalysisTableModel  extends AbstractTableModel {
	
    private static final long serialVersionUID = 1L;
    
	    DecimalFormat fieldFormat = new DecimalFormat("####.#####"); 
	    
            private String cavPhaseOffsetLabel, WInLabel;
 	    
	    /** The analysisStuff this table pertains to */
	    private AnalysisStuff analStuff;
	
	    //-------------- Member variables --------------//
	    /** The column name holder */
	    private String [] columnNames= {"Parameter", "Value", "Vary"};	    
	
	    // --------------------Constructor ------------------//
	
	/** number or rows == number of variables */
	    private int nTableRows = 3;
	    
	    public AnalysisTableModel (AnalysisStuff as) {
		    analStuff = as;
		    cavPhaseOffsetLabel = new String("Cav Phase Offset (deg)");
	            WInLabel = new String("Energy In (MeV)");	
	    }
	    
	   /** get the column names */
	
	    public String getColumnName(int col) 
	    { return columnNames[col]; }

	    /** Number of rows so far */
	    public int getRowCount() { return nTableRows;}
	
	    /** column count */
	    public int getColumnCount() { return 3;}
	
	    /** no cells are editable here */
	    public boolean isCellEditable(int row, int col) {     
		    if (col == 0)
			    return false;
		    else
			    return true;
	    }
	    
	    /** get the column class */
	    public Class<?> getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	    }

	     /** the value distributor */
	
	    public Object getValueAt(int row, int col) 
	    { 
		Object val=null;
		
		if(col == 2) {
			return analStuff.variableActiveFlags.get(row);
		}
		
		if(row == 0) {
			if(col==0) return  cavPhaseOffsetLabel;
			//return new Double(cavPhaseOffset);
			return fieldFormat.format(analStuff.cavPhaseOffset);
		}
		
		if(row == 1) {
			if (col == 0) return WInLabel;
			//return new Double(WIn);
			return fieldFormat.format(analStuff.WIn);
		}
			
		if(row == 2) {
			if(col == 0) return "Cav Voltage scan " + analStuff.amplitudeVariableIndex + " (MV/m)";
			//val = (Double) ampValueV.get(row-2);
			return fieldFormat.format(analStuff.cavityVoltage);
		}
		/*
		if(row == 3) {
			if(col == 0) return "Model BPM diff fudge (deg)";
			//val = (Double) ampValueV.get(row-2);
			return fieldFormat.format(analStuff.theDoc.fudgePhaseOffset);
		}		
		
	        */
		return val;
	
	    }
		public void setValueAt(Object value, int row, int col)
		{
			double val = 1.;
	
			// Don't do anything if it's the first column
		
			if(col == 1) {
			    try {
				    val = Double.parseDouble((String) value);
				    if(row == 0) analStuff.cavPhaseOffset = val;
				    if(row == 1) analStuff.WIn = val;
				    if(row == 2) {
					    analStuff.cavityVoltage = val;
					    analStuff.updateAmpFactors();
				    }
				    if(row == 3) analStuff.theDoc.fudgePhaseOffset = val;
				    analStuff.theDoc.myWindow().errorText.setText("");
			    }  catch (java.lang.NumberFormatException exc)  {
				    Toolkit.getDefaultToolkit().beep();
				    analStuff.theDoc.myWindow().errorText.setText("Hmmm - looks like you did not type a real number?");
			    }
			}
			
			if(col == 2) {
				analStuff.variableActiveFlags.set(row, value);
			}
			fireTableCellUpdated(row, col);
		}
	}
