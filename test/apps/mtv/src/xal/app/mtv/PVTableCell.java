/* PVTableCell class */

package xal.app.mtv;

import javax.swing.JTable;
import javax.swing.table.TableModel;
import java.text.*;
import java.awt.event.*;

import xal.ca.*;
import xal.tools.text.ScientificNumberFormat;

/**
* This is an intermediate object to facilitate the table display 
* from the PVData objects.
* A table cell that listens to CA. This class bundles together some
* functionality that will be used by the java TableModel and
* will hide some details for getting info about live channels
*
*/

public class PVTableCell
{   
	/** The channel this cell is monitoring */
	private ChannelWrapper theChannel = null;
	
	/** indicator that this is a placeholder 
	* (needed  for general xml parsing of the DataTable Structure */
	private boolean amADummy;
	private String dummyString = " "; 
	
	/** the cell could represent b_set PV for quads this is a cell for b_book */
	private PVTableCell b_book_cell = null;
	private  boolean isBindToBBook = false;
	
	private double memoryValue = 0.;
	private boolean isChanged = false;
	private volatile boolean memButtonAct = false;
	private boolean wasClicked = false;
	
	/** format for displaying numbers */
	private ScientificNumberFormat scientificFormat = new ScientificNumberFormat( 6, 12, false );
	
	//--------------------------------------
	// constructors:
	//---------------------------------------
	/** constructor used by the dummy table cells */
	public PVTableCell(){amADummy = true;}
	
	/** constructor used by table cells with a real PV in it */
	public PVTableCell(ChannelWrapper channel){
		amADummy = false;
		theChannel = channel;
		addValueChangeListener();
	}
	
	/** constructor used by table cells with a real PV in it */
	public PVTableCell(String channelName){
		theChannel = new ChannelWrapper(channelName);
		amADummy = false;
		addValueChangeListener();
	}
	
	/** constructor used by table cells with a real PV in it */
	public PVTableCell(Channel channel){
		theChannel = new ChannelWrapper(channel);
		amADummy = false;
		addValueChangeListener();
	}   
	
	/** returns the ChannelWrapper instance */
	public ChannelWrapper getChannelWrapper(){
		return theChannel;
	}	
	
	private void addValueChangeListener(){
		ActionListener valueChangeListener = new ActionListener(){
			public void actionPerformed(ActionEvent evnt){
				if(wasClicked){
					if(memButtonAct != true){
						isChanged = true;
					}
					else{
						memButtonAct = false;
						isChanged = false;
					}
				}
			}
		};
		if(theChannel != null){
			theChannel.setValueChangeListener(valueChangeListener);
		}
	}
	
	/** sets the child b_book cell */
	public void setBBookCell(PVTableCell b_book_cell) {
		this.b_book_cell = b_book_cell;
	}		
	
	/** sets the child b_book cell */
	public PVTableCell getBBookCell() {
		return b_book_cell;
	}	
	
	/** returns true or false about binding to B_Book */
	public boolean getIsBundToBBook(){
		return isBindToBBook;
	}
	
	/** Sets true or false about binding to B_Book */
	public void setIsBundToBBook(boolean isBindToBBook){
		this.isBindToBBook = isBindToBBook;
	}
	
	/** returns the name of the channel */
	public String PVName() {return theChannel.getId();}

	/** memorizes the current value */
	public void memorizeValue(){
		if(!amADummy){
			memoryValue = theChannel.getValDbl();
			isChanged = false;
			wasClicked = true;
		}
	}
	
	/** returns true is the cell was clkicked */
	public boolean wasClicked(){
		return wasClicked;
	}
	
	/** returns the memorized value */
	public double getValueFromMemory(){
		return memoryValue;
	}
	
	/** restores the value from memory*/
	public void restoreValueFromMemory(){
		if(!amADummy){
			try{
				theChannel.getChannel().putVal(memoryValue);
				isChanged = false;
				memButtonAct = true;
			}
			catch(Exception ex) {
				System.out.println("trouble sending new value to pv: " + theChannel.getId());
				isChanged = true;
				amADummy = true;
				dummyString = theChannel.getId()+"-BAD!";
			}	
		}		
	}
	
	/** returns true if the value has been changed */
	public boolean isValueChanged(){
		return isChanged;
	}
	
	/** sets that the value has been changed */
	public void valueChanged(){
		isChanged = true;
	}
	
	/** returns true if there is a connected channel in this cell */    
	public boolean isConnected(){
		if(amADummy) {
			return false;
		}
		return theChannel.isConnected(); 
	}
	
	/** Output the PV value to a String needed by the tablemodel */
	public String toString() {
		if(amADummy) return dummyString;
		return scientificFormat.format(theChannel.getValDbl());
	}
	
	/** 
	* It returns the double value.
	*/	
	public double getValDbl(){ 
		return theChannel.getValDbl();
	}

	/** 
	* It returns true or false. If it is true then the table cell will be empty. 
	*/
	public boolean isDummy(){
		return amADummy;
	}
	
	protected Channel getChannel() {
		if(amADummy == false){
			return theChannel.getChannel();
		}
		return null;
	}
	
}
