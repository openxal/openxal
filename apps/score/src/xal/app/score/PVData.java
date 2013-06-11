/**
 * This class is to work with  a set of related PVs at a 
 * presribed system level.
 *
 * @version   1.0
 * @author    J. Galambos
 */

package xal.app.score;

import java.util.*;
import java.text.*;

import xal.tools.data.*;
import xal.ca.*;

public class PVData

{

    //-------------- Member variables --------------//

    /** the dataTable that is used to map info to a TableModel */
    protected DataTable dataTable;

    /** The name of this device */
    protected String name;

    /** names for keys in the dataTable */

    static public final String DATA_TYPE_KEY = "dataType";
    static public final String typeKey = "type";
    static public final String systemKey = "system";
    static public final String rbNameKey = "rbName";
    static public final String rbSavedValKey = "rbSavedVal";
    static public final String spNameKey = "spName";
    static public final String spSavedValKey = "spSavedVal";
    static public final String restoreRBValKey = "useRBforRestore";  

    // --------------------Constructor ------------------//


    /**
     * the constructor for PVData
     */
    public PVData(String nm) {

	name = nm;
	dataTable = newDataTable(name);
    }
    
    // -----------------------member methods   --------------//

    /** get the data table with all the records in it */
    public DataTable getDataTable() {return dataTable;}

    /** method to set the dataTable from an externally created one.*/
    protected void setDataTable(DataTable dt) { dataTable = dt;};
    
    /** set up the data table structure */
    protected DataTable newDataTable(String name) {
        final List<DataAttribute> attributes = new ArrayList<DataAttribute>();
        
        attributes.add( new DataAttribute(systemKey, String.class, true) );
        attributes.add( new DataAttribute(typeKey, String.class, true) );
        attributes.add( new DataAttribute(rbNameKey, String.class, true) );
        attributes.add( new DataAttribute(spNameKey, String.class, true) );
        attributes.add( new DataAttribute( DATA_TYPE_KEY, String.class, false, DataTypeAdaptor.DEFAULT_TYPE ) );
        attributes.add( new DataAttribute(spSavedValKey, String.class, false) );
        attributes.add( new DataAttribute(rbSavedValKey, String.class, false) );
		attributes.add( new DataAttribute(restoreRBValKey, Boolean.class, false) );
        
        return new DataTable( name, attributes, ScoreRecord.class );
    }
	
	
    /** remove all records from this table */
    protected void clear() {
		Collection<GenericRecord> records = getDataTable().records();
		Iterator<GenericRecord> itr = records.iterator();
		while (itr.hasNext()) {
			getDataTable().remove((ScoreRecord) itr.next());
		}
    }
    
	
    /** a method to add a set of  related setpoint + 
     * readback PV information to the dataTable.  
     * Used by jython script for converting spreadsheet to xml format 
	 */
    public void addRecord( String sys, String type, final String dataType, String setPV, String setVal, String rbPV, String rbVal ) {
		final ScoreRecord record = new ScoreRecord(dataTable);
		
		record.setValueForKey(sys, systemKey);
		record.setValueForKey(type, typeKey);
		record.setValueForKey( dataType, DATA_TYPE_KEY );
		record.setValueForKey(setPV, spNameKey);
		record.setValueForKey(setVal, spSavedValKey);
		record.setValueForKey(rbPV, rbNameKey);
		record.setValueForKey(rbVal, rbSavedValKey);
		record.setValueForKey(new Boolean(false), restoreRBValKey);	
		dataTable.add(record);
    }
    
    /** a method to add a set of  related setpoint + 
     * readback PV information to the dataTable.  
     * Used by jython script for converting spreadsheet to xml format 
	 */
    public void addRecord(String sys, String type, final DataTypeAdaptor dataTypeAdaptor, String setPV, String setVal, String rbPV, String rbVal, boolean useRBval) {
		final ScoreRecord record = new ScoreRecord(dataTable);

		record.setValueForKey(sys, systemKey);
		record.setValueForKey(type, typeKey);
		final String dataType = dataTypeAdaptor != null ? dataTypeAdaptor.getType() : "";
		record.setValueForKey( dataType, DATA_TYPE_KEY );
		record.setValueForKey(setPV, spNameKey);
		record.setValueForKey(setVal, spSavedValKey);
		record.setValueForKey(rbPV, rbNameKey);
		record.setValueForKey(rbVal, rbSavedValKey);
		record.setValueForKey(new Boolean(useRBval), restoreRBValKey);

		dataTable.add(record);
    }   
    
}
