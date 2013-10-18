

package xal.extension.widgets.swing;
import java.awt.*;
import java.awt.event.*;
import java.text.NumberFormat;
import java.text.*;
import java.util.*;
import java.io.*;
import xal.extension.widgets.swing.*;
import javax.swing.*; 
import javax.swing.event.*; 

/**
 * This is a class for creating a scroll bar and text box combination
 * which operates with doubles.      

 */

public class TextScrollDouble extends JPanel{

    private static final long serialVersionUID = 1L;
    
    public DecimalField textField;
    public JScrollBar scrollBar;
    private int orientation;
    private int scrollrange;
    private int fracdigits;
    private int ivalue;
    private int textdigits;
    private int increment;
    private double dvalue;
    private double minincrement;
    private double currentincrement;
    private double drangemin;
    private double drangemax;
    private NumberFormat numberFormat;
    
    /**
     * An instance the class.  Default values used are:
     * orient (relative layout of text and scroll) = 0 (horizontal); 
     * tdigits (total number of digits in the text field) = 6;
     * fdigits  (digits after the decimal) = 3;
     * dval (initial value in the text box) = 0.0; 
     * min (minimum of the range) = 0.0;
     * max (maximum of the range) = 10.0;
     * cincrement (current increment) = 0.01;
     * minincrement (minimum allowable increment) = 0.001; 
     */
    public TextScrollDouble()
    {
	orientation = 0;
	drangemin = 0.0;
	drangemax = 10.0;
	currentincrement = 0.01;
	minincrement = 0.001;
	dvalue = drangemin;
	ivalue = 0;
	fracdigits = 3;	
	textdigits = 6;
	
	ActivateTextScroll();
    }
	
    /**
     * An instance the class.  Default values used are: 
     * tdigits (total number of digits in the text field) = 6;
     * fdigits  (digits after the decimal) = 3;
     * dval (initial value in the text box) = 0.0; 
     * minincrement (minimum allowable increment) = 0.001;
     */
    public TextScrollDouble(int orient, double min, double max, 
			    double cincrement)
    {
	orientation = orient;
	drangemin = min;
	drangemax = max;
	currentincrement = cincrement;
	
	minincrement = 0.001;
	dvalue = drangemin;
	fracdigits = 3;	
	textdigits = 6;

	ActivateTextScroll();
    }
    
    
    /**
     * An instance the class.  Default values used are: 
     * tdigits (total number of digits in the text field) = 6;
     * fdigits  (digits after the decimal) = 3;
     */
    public TextScrollDouble(int orient, double dval, 
			    double min, double max, 
			    double cincrement, double mincrement)
    {
	orientation = orient;
	drangemin = min;
	drangemax = max;
	currentincrement = cincrement;
	minincrement = mincrement;
	dvalue = dval;

	fracdigits = 3;	
	textdigits = 6;
	ActivateTextScroll();
    }

    /**
     * An instance of the class.  All values defined by user.
     * @param orient the relative layout of the text box and scroll panel 
     * (0 is horizontal, 1 is vertical) 
     * @param tdigits the total number of digits in the text field
     * @param fdigits the number of digits after the decimal 
     * @param dval the initial value in the text box 
     * @param min the minimum of the range
     * @param max the maximum of the range
     * @param cincrement the current increment
     * @param mincrement the minimum allowable increment 
     */
    public TextScrollDouble(int orient, int tdigits, int fdigits, 
			    double dval, double min, double max, 
			    double cincrement, double mincrement)
    {
	orientation = orient;
	drangemin = min;
	drangemax = max;
	currentincrement = cincrement;
	minincrement = mincrement;
	dvalue = dval;
	textdigits = tdigits;
	fracdigits = fdigits;
	ActivateTextScroll();
    }
  

    private void ActivateTextScroll(){

	numberFormat = NumberFormat.getNumberInstance();
	numberFormat.setMinimumFractionDigits(fracdigits);
	
	textField = new DecimalField(dvalue, textdigits, numberFormat);

	scrollrange = nearestInt((drangemax-drangemin)/minincrement);
	increment = nearestInt(currentincrement/minincrement);
	ivalue = convertTextFieldNumber(dvalue, increment);

	scrollBar = new JScrollBar(0, ivalue, 0, 0, scrollrange);
	scrollBar.setUnitIncrement(increment);
	scrollBar.setBlockIncrement(increment);
	
	textField.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    if(textField.getDoubleValue() > drangemax ||
		       textField.getDoubleValue() < drangemin){
			JOptionPane frame = new JOptionPane();
			JOptionPane.showMessageDialog(frame, 
						      "Value is out of range",
						      "Warning", 
						      JOptionPane
						      .WARNING_MESSAGE);
		    }
			double dvalue = textField.getDoubleValue();
			int ivalue = convertTextFieldNumber(dvalue, increment);
			scrollBar.setValue(ivalue);
		}
	    });

	scrollBar.addAdjustmentListener(new AdjustmentListener(){
		public void adjustmentValueChanged(AdjustmentEvent e){
		    int ivalue = scrollBar.getValue();
		    double dvalue = convertScrollBarNumber(ivalue, increment);
		    textField.setValue(dvalue);
		}
	    });
	
	JPanel comboPane = new JPanel();
	if(orientation == 0){
	    comboPane.setLayout(new BoxLayout(comboPane, BoxLayout.X_AXIS));
	}
	else{
	    comboPane.setLayout(new BoxLayout(comboPane, BoxLayout.Y_AXIS));
	}
	comboPane.add(textField);
	comboPane.add(scrollBar);
	this.add(comboPane);
    }

    /**
     * Set the value in the text field
     */
    public void setValue(double value){
	textField.setValue(value);
	int ivalue = convertTextFieldNumber(value, increment);
	scrollBar.setValue(ivalue);
    }

    /** 
     * Set the minimum value of the range
     */
    public void setRange(double minimum, double maximum){
	drangemin = minimum;
	drangemax = maximum;
	scrollrange = nearestInt((drangemax-drangemin)/minincrement);
	scrollBar.setMaximum(scrollrange);
    }    

    /** 
     * Set the maximum value of the range
     */
    public void setMaximum(double value){
	drangemax = value;
	ActivateTextScroll();
    }    

    /** 
     * Set the increment of the scroll bar.  Can not be less than the 
     * minimum increment.
     */
    public void setIncrement(double dincrement)
    {
	currentincrement = dincrement;
	Double drange = new Double((drangemax - drangemin)/currentincrement);
	increment = nearestInt(drange.doubleValue());
	scrollBar.setUnitIncrement(scrollrange/increment);
	scrollBar.setBlockIncrement(scrollrange/increment);
    }
    

    /**
     * Set the size of the scroll bar.
     */
    public void setPreferredSize(int width, int heigth){
	scrollBar.setPreferredSize(new Dimension(width, heigth));
    }

    /**
     * Get the value in the text field.
     */
    public double getValue(){
	double value = textField.getDoubleValue();
	return value;
    }

    /** 
     * Get the minimum value of the range
     */
    public double getMinimum(){
	double value = drangemin;
	return value;
    }
    
    /**
     * Get the maximum value of the range
     */
    public double getMaximum(){
	double value = drangemax;
	return value;
    }

    /** 
     * Get the current increment
     */ 
    public int getIncrement(){
        int value = increment;
	return value;
    }
    

    /** 
     * Disable.
     */ 
    public void Disable(){
        scrollBar.setEnabled(false);
	textField.setEnabled(false);
    }
    
    /** 
     * Enable.
     */ 
    public void Enable(){
        scrollBar.setEnabled(true);
	textField.setEnabled(true);
    }

    private int nearestInt(double darg)
    {
	int iResult;
	int iCompare;	
	Double dVal;
	Double dCompare;
	
	dVal = new Double(darg);
	iResult = dVal.intValue();

	dCompare = new Double(darg + 0.5);
	iCompare= dCompare.intValue();
	
	if(iCompare > iResult) iResult = iCompare;
	
	return iResult;
    }



    private int convertTextFieldNumber(double dValue, int iprecision){
	
	int iValue; 
	int irangeDelta;
	Integer iPrecision;
	Integer iRangeDelta;
	double drangeDelta;    
	Double tempD;

	drangeDelta = drangemax-drangemin;
	
	irangeDelta = scrollrange;
	iRangeDelta = new Integer(irangeDelta);
	iPrecision = new Integer(iprecision);
        tempD=new Double((dValue - drangemin)*
			 iPrecision.doubleValue()/drangeDelta
			 *(iRangeDelta.doubleValue())/
			   iPrecision.doubleValue());
		    
	iValue = nearestInt(tempD.doubleValue());
	
	return iValue;
    }


    private double convertScrollBarNumber(int iValue, int iprecision){
	
       	int irangeDelta;
	Integer iPrecision;
	Integer iRangeDelta;
	double dValue;
	double drangeDelta;    
	Double tempD;

	drangeDelta = drangemax-drangemin;
	
	irangeDelta = scrollrange;
	iRangeDelta = new Integer(irangeDelta);
	iPrecision = new Integer(iprecision);

	dValue = drangemin + iValue/iRangeDelta.doubleValue()*drangeDelta;

	return dValue;
    }

}




