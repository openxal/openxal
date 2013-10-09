/*
 * EditContext.java
 *
 * Created on May 10, 2002, 2:18 PM
 */

package xal.tools.data;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;


/**
 * EditContext manages dynamic data held in all associated tables.
 * @author  tap
 */
public class EditContext {
	/** tag for a table group within a data adaptor */
	static final public String GROUP_TAG = "tablegroup";
	
	/** message center for dispatching events from this edit context */
    final protected MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events from this edit context */
    final protected EditContextListener NOTICE_PROXY;
	
	/** Map of tables associated with group */
    final private Map<String,Set<DataTable>> TABLE_MAP_BY_GROUP;
	
	/** Map of tables by name */
    final private Map<String,DataTable> TABLE_MAP_BY_NAME;
	
	
    /** Constructor */
    public EditContext() {
        TABLE_MAP_BY_GROUP = new HashMap<String,Set<DataTable>>();
        TABLE_MAP_BY_NAME = new HashMap<String,DataTable>();
        
		MESSAGE_CENTER = new MessageCenter( "Edit Context" );
        NOTICE_PROXY = MESSAGE_CENTER.registerSource( this, EditContextListener.class );
    }
    
    
	/**
	 * Add the listener to receive edit context events.
	 * @param listener the listener to receive edit context events from this context
	 */
    public void addEditContextListener( final EditContextListener listener ) {
        MESSAGE_CENTER.registerTarget( listener, this, EditContextListener.class );
    }
    
    
	/**
	 * Remove the listener from receiving edit context events.
	 * @param listener the listener to remove from receving edit context events from this context
	 */
    public void removeEditContextListener( final EditContextListener listener ) {
        MESSAGE_CENTER.removeTarget( listener, this, EditContextListener.class );
    }
	
	
	/** Perform a deep copy of the specified table group from the specified edit context */
	public void importTablesFromContext( final EditContext editContext, final String group ) {
		// first copy the specified group from the specified edit context into a data adaptor
		final XmlDataAdaptor docAdaptor = XmlDataAdaptor.newEmptyDocumentAdaptor();
		editContext.writeGroupToDataAdaptor( docAdaptor, group );
		// extract the group into the current edit context
		importTablesFromDataAdaptor( docAdaptor, group );
	}
	
    
    /** Import the tables from the data adaptor and assign them to the specified group in this edit context. */
	public void importTablesFromDataAdaptor( final DataAdaptor docAdaptor, final String tableGroup ) {
        final Collection<DataTable> tableSet = new HashSet<DataTable>();
		
        final DataAdaptor tableGroupAdaptor = docAdaptor.childAdaptor( EditContext.GROUP_TAG );
        final List<DataAdaptor> tableAdaptors = tableGroupAdaptor.childAdaptors( DataTable.DATA_LABEL );
        for( final DataAdaptor tableAdaptor : tableAdaptors ) {
            try {
                final DataTable table = new DataTable( tableAdaptor );
				
                tableSet.add( table );
            }
            catch ( MissingPrimaryKeyException exception ) {
				final String name = tableAdaptor.stringValue( "name" );
				final String message = "Will skip reading table: " + name + " due to missing primary key!";
				Logger.getLogger("global").log( Level.WARNING, message, exception );
                System.err.println(message);
            }
        }
		
        addTablesToGroup( tableSet, tableGroup );
    }
	
    
    /** Write all tables associated with the specified group to the specified data adaptor. */
	public void writeGroupToDataAdaptor( final DataAdaptor docAdaptor, final String group ) {
        final DataAdaptor tablegroupAdaptor = docAdaptor.createChild( GROUP_TAG );
		
        final Collection<DataTable> tables = getTablesForGroup( group );
        for ( final DataTable table : tables ) {
            final DataListener handler = table.dataHandler();
            final DataAdaptor tableAdaptor = tablegroupAdaptor.createChild( DataTable.DATA_LABEL );
            try {
                handler.write( tableAdaptor );
            }
            catch ( MissingPrimaryKeyException exception ) {
				final String message = "Will skip writing table: " + table.name() + " due to missing primary key!";
                System.err.println( message );
				Logger.getLogger("global").log( Level.WARNING, message, exception );
            }
        }
    }	
    
    
    public void clear() {
        TABLE_MAP_BY_GROUP.clear();
        TABLE_MAP_BY_NAME.clear();
    }
    
    
    /** Get the table associated with the specified table name. */
    public DataTable getTable( final String name ) {
        return TABLE_MAP_BY_NAME.get( name );
    }
    
    
    /** Get the collection of all table names associated with this context. */
    public Collection<String> getTableNames() {
        return TABLE_MAP_BY_NAME.keySet();
    }
    
    
    /** Get the collection of all tables in the edit context. */
    public Collection<DataTable> getTables() {
        return TABLE_MAP_BY_NAME.values();
    }
    
    
    /**
     * Return the tables associated with the specified group and make a set if it doesn't already exist.
	 * @return the set of tables associated with the specified group
     */
    private Collection<DataTable> tableSetForGroup( final String group ) {
        Set<DataTable> tableSet = null;

        if ( !TABLE_MAP_BY_GROUP.containsKey( group ) ) {
            tableSet = new HashSet<DataTable>();
            TABLE_MAP_BY_GROUP.put( group, tableSet );
        }
        else {
            tableSet = TABLE_MAP_BY_GROUP.get( group );
        }

        return tableSet;
    }
        
        
    /** Add the table to the edit context. */
    public void addTableToGroup( final DataTable newTable, final String group ) {
        final String name = newTable.name();
        TABLE_MAP_BY_NAME.put( name, newTable );
        final Collection<DataTable> tableSet = tableSetForGroup( group );
        tableSet.add( newTable );
        
        NOTICE_PROXY.tableAdded( this, newTable );
    }
    
    
    /** Add the tables to the edit context. */
    public void addTablesToGroup( final Collection<DataTable> newTables, final String group ) {
		for ( final DataTable newTable : newTables ) {
            addTableToGroup( newTable, group );
        }
    }
    
    
    /** Get all table groups */
    public Collection<String> getTableGroups() {
        return TABLE_MAP_BY_GROUP.keySet();
    }


    /** Get all tables associated with the specified group */
    public Collection<DataTable> getTablesForGroup( final String group ) {
        final Collection<DataTable> tables = TABLE_MAP_BY_GROUP.get( group );
        return Collections.unmodifiableCollection( tables );
    }

        
    /** Remove the table from the edit context. */
    public void remove( final DataTable aTable ) {
        String name = aTable.name();
        TABLE_MAP_BY_NAME.remove( name );
        
        // Now remove the table from all groups (even though it only exists in one)
        final Collection<String> groups = getTableGroups();
		for ( final String group : groups ) {
            Collection<DataTable> tableSet = tableSetForGroup( group );
            tableSet.remove( aTable );
        }

        NOTICE_PROXY.tableRemoved( this, aTable );
    }


    /** Get all of the records from the table given by the table name. */
    public Collection<GenericRecord> records( final String tableName ) {
        final DataTable table = getTable( tableName );
        return table.records();
    }
    
    
    /** Get the records from the table where the bindings map is valid. */
    public <ValueType extends Object> Collection<GenericRecord> records( final String tableName, final Map<String,ValueType> bindings ) {
        final DataTable table = getTable( tableName );
        return table.records( bindings );
    }
    
    
    /** Get the records from the table where the bindings map is valid. */
    public <ValueType extends Object> Collection<GenericRecord> records( final DataTable table, final Map<String,ValueType> bindings ) {
        return table.records( bindings );
    }
    
    
    /** Get the records from the table where the value for the specified key matches. */
    public Collection<GenericRecord> records(String tableName, String key, Object value) {
        DataTable table = getTable( tableName );
        return table.records( key, value );
    }
    
    
    /** Get the records from the table associated with the node. */
    public Collection<GenericRecord> recordsForNode( final String tableName, final String nodeId ) {
        final DataTable table = getTable( tableName );
        return table.recordsForNode( nodeId );
    }
    
    
    /** Get a single record from the table associated with the node. */
    public GenericRecord recordForNode(String tableName, String nodeId) {
        return getTable(tableName).recordForNode(nodeId);
    }
}




