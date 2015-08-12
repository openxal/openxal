/*
 * DateGraphFormat.java
 *
 * Created on March 25, 2004, 1:13 PM
 */

package xal.extension.widgets.plot;

import java.text.*;

/**
 * DateGraphFormat is the subclass of the NumberFormat class.
 * It produces the x-axis markers as a date by using an internal date format.
 * X-axis values are supposed to be the number of seconds since January 1, 1970, 00:00:00 GMT.
 * To use this format for the x-axis call the method setNumberFormatX(NumberFormat df) of
 * the instance of FunctionGraphsJPanel class with the DateGraphFormat instance instead of 
 * NumberFormat.
 *
 * @author  shishlo
 */                                                                                         
public class DateGraphFormat extends NumberFormat{
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    

    private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
    

    /**
     * Constructor with a default DateFormat (it is SimpleDateFormat("HH:mm:ss")). 
     */
    public DateGraphFormat(){
    }


    /**
     * Constructor with a date format pattern as the parameter. 
     */
    public DateGraphFormat(String pattern){
	((SimpleDateFormat) dateFormat).applyPattern(pattern);
    }

    /**
     * Constructor with a DateFormat as a parameter. 
     */
    public DateGraphFormat(DateFormat dateFormatIn){
	if(dateFormatIn != null){
	    dateFormat = dateFormatIn;
	}   
    }

	/** Don't call. Just returns null. Satisfied abstract method requirement. */
	public Number parse( final String input, final ParsePosition position ) {
		return null;
	}


    /**
     * The overridden format method of the NumberFormat class.
     */
    public StringBuffer format(double t_sec, StringBuffer toAppendTo, FieldPosition pos){
		return format( (long)t_sec, toAppendTo, pos );
    }


	/**
	 * The overridden format method of the NumberFormat class.
	 */
	public StringBuffer format(long t_sec, StringBuffer toAppendTo, FieldPosition pos){
		long t_l = 1000 * t_sec;
		return dateFormat.format(new java.util.Date(t_l), toAppendTo, pos);
	}


    /**
     * Returns the instance of the DateFormat class
     * that is used to format the seconds to the date. 
     */
    public DateFormat getDateFormat(){
	return dateFormat;
    }

    /**
     * Sets the instance of the DateFormat class
     * that will be used to format the seconds to the date. 
     */
    public void setDateFormat(DateFormat dateFormat){
	if(dateFormat != null){
	    this.dateFormat = dateFormat;
	}
    }

    /**
     * Transforms the date to the seconds since January 1, 1970, 00:00:00 GMT.
     * @return double as a number of seconds
     */
    static public double getSeconds(java.util.Date date){
	return (double) (date.getTime()/1000L); 
    }

    /**
     * Transforms the seconds since January 1, 1970, 00:00:00 GMT to the date.
     * @return an instance of the Date class.
     */
    static public java.util.Date getDate(double seconds){
	return new java.util.Date(1000L * ((long)(seconds))); 
    }
}
