package xal.app.injdumpwizard.utils;

/**
* The auxiliary class defining the name-value table record
*
*@author     shishlo
*/
public class TableRecord{
	
	private String name = "";
	private double value = 0;
	private boolean status = false;
	
	/**
	*  Constructor for the TableRecord object
	*/
	public TableRecord(String name_in, double value_in, boolean status_in){
		name = name_in;
		value = value_in;
		status = status_in;
	}
	
	/**
	*  Returns the name of the record.
	*/	
	public String getName(){
		return name;
	}
	
	/**
	*  Returns the value of the record.
	*/		
	public double getValue(){
		return value;
	}
	
	/**
	*  Returns the status of the record.
	*/		
	public boolean getStatus(){
		return status;
	}
	
	/**
	*  Sets the value of the record.
	*/		
	public void setValue(double value_in){
		value = value_in;
	}
	
	/**
	*  Sets the status of the record.
	*/		
	public void setStatus(boolean status_in){
		status = status_in;
	}	
}	
