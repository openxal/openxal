/*
 * RecordSet.java
 *
 * Created on May 10, 2002, 2:19 PM
 */

package xal.tools.data;

import xal.tools.messaging.MessageCenter;

import java.util.*;
import java.util.logging.*;
import java.lang.reflect.*;


/******************************************************************************
 * DataTable is internal storage resembling a database table.  An instance of DataTable consists of an associated schema and a number of records.
 * Each record must be of the same class and have the same keys.
 * Note that for performance reasons this class is not thread safe.  Users who need thread safety must provide explicit synchronization on the table.
 * @author  tap
 */
public class DataTable {
	/** table label inside of a data adaptor */
	static final public String DATA_LABEL = "table";
	
    static final public String NODE_KEY = "nodeId";
    static final private Class<GenericRecord> DEFAULT_RECORD_CLASS = GenericRecord.class;
	
	/** message center for dispatching data table notices to registered listeners */
	final private MessageCenter MESSAGE_CENTER;
	
	/** proxy which forwards events to registered listeners */
    final private DataTableListener NOTICE_PROXY;
    
	/** name of this table */
    private String _name;
	
	/** class of this table's records */
    private Class<? extends GenericRecord> _recordClass;
	
	/** table of hashed records */
    private KeyTable _keyTable;
	
	/** table schema of attributes */
    private Schema _schema;
	
	
    /** 
	 * Constructor 
	 */
    public DataTable( final String aName, final Collection<DataAttribute> attributes ) {
        this( aName, attributes, GenericRecord.class );
    }
    
    
	/**
	 * Primary constructor
	 */
    public DataTable( final String aName, final Collection<DataAttribute> attributes, final Class<? extends GenericRecord> aRecordClass ) {
        MESSAGE_CENTER = new MessageCenter( "Data Table" );
        NOTICE_PROXY = MESSAGE_CENTER.registerSource( this, DataTableListener.class );
		
        _name = aName;
        _schema = new Schema( attributes );
        this._recordClass = aRecordClass;
        _keyTable = new KeyTable();
    }
    
    
	/**
	 * Constructor
	 * @param adaptor the adaptor from which to construct the data table
	 */
    public DataTable( final DataAdaptor adaptor ) {
        MESSAGE_CENTER = new MessageCenter( "Data Table" );
        NOTICE_PROXY = MESSAGE_CENTER.registerSource( this, DataTableListener.class );
		
        DataListener importer = dataHandler();
        importer.update( adaptor );
    }
    
    
	/**
	 * Add the specified listener as a receiver of events from this data table.
	 * @param listener the listener to receive events
	 */
    public void addDataTableListener( final DataTableListener listener ) {
        MessageCenter messageCenter = MessageCenter.defaultCenter();
        messageCenter.registerTarget( listener, this, DataTableListener.class );
    }
    
    
	/**
	 * Remove the specified listener from receiving events from this data table.
	 * @param listener the listener to remove from receiving events
	 */
    public void removeDataTableListener( final DataTableListener listener ) {
        MessageCenter messageCenter = MessageCenter.defaultCenter();
        messageCenter.removeTarget( listener, this, DataTableListener.class );
    }

	
	/** convienience method for user to detect record class */
	public Class<? extends GenericRecord> getRecordClass() { return _recordClass; }
    
    
    /** Handle reading and writing from a data adaptor */
	@SuppressWarnings( "unchecked" )	// need to cast Class forName() call
    public DataListener dataHandler() throws MissingPrimaryKeyException {
        /* Anonymous class responsible for reading and writing an instance of DataTable with the data store */
        return new DataListener() {
            final static private String NAME_ATTRIBUTE = "name";
            final static private String RECORD_CLASS_ATTRIBUTE = "recordClass";

			/** Get the data label */
            public String dataLabel() {
                return DATA_LABEL;
            }


			/** Update the table from the data adaptor */
            @SuppressWarnings( "unchecked" )
            public void update( final DataAdaptor adaptor ) {
                _name = adaptor.stringValue( NAME_ATTRIBUTE );

                if ( adaptor.hasAttribute( RECORD_CLASS_ATTRIBUTE ) ) {
                    String recordClassName = "";
                    try {
                        recordClassName = adaptor.stringValue( RECORD_CLASS_ATTRIBUTE );
                        _recordClass = (Class<? extends GenericRecord>)Class.forName( recordClassName );
                    }
                    catch( ClassNotFoundException exception ) {
						final String message = "Warning, the specified record class, \"" 
                        + recordClassName + "\" was not found, will substitute " + 
                        DEFAULT_RECORD_CLASS.getName();
                        System.err.println( message );
						Logger.getLogger("global").log( Level.WARNING, message, exception );
                       _recordClass = DEFAULT_RECORD_CLASS;
                    }
                }
                else {
                    _recordClass = DEFAULT_RECORD_CLASS;
                }

                // There can only be one schema
                final List<DataAdaptor> schemaList = adaptor.childAdaptors( "schema" );
                final DataAdaptor schemaAdaptor = schemaList.get(0);
				_schema = new Schema();
				_schema.update( schemaAdaptor );					

                // Now that we have a schema, we can instantiate the keyTable
                _keyTable = new KeyTable();

                // read generic records
                final List<DataAdaptor> recordAdaptors = adaptor.childAdaptors( "record" );
                for ( final DataAdaptor recordAdaptor : recordAdaptors ) {
                    try {
                        final Constructor<GenericRecord> constructor = (Constructor<GenericRecord>)_recordClass.getConstructor( new Class<?>[] {DataTable.class} );

                        GenericRecord record = constructor.newInstance( new Object[] {DataTable.this} );
                        record.update( recordAdaptor );
                        add( record );
                    }
                    catch(Exception exception) {
						Logger.getLogger("global").log( Level.SEVERE, "Error reading record.", exception );
						exception.printStackTrace();
                    }
                }
            }


			/** Archive this instance to the data adaptor. */
			public void write( final DataAdaptor adaptor) {
                adaptor.setValue( "name", _name );
                adaptor.setValue( "recordClass", _recordClass.getName() );

				adaptor.writeNode( _schema );					

				final Collection<GenericRecord> records = records();
				adaptor.writeNodes( records );
            }
        };
    }
    
    
    /** Get the name of this table. */
    public String name() {
        return _name;
    }
    

    /** Get all keys for this table. */
    public Collection<String> keys() {
		return _schema.keys();
    }
    
    
    /** Get the primary keys for the table. */
    public Collection<String> primaryKeys() {
		return _schema.PRIMARY_KEYS;
    }
	
	
	/**
	 * Determine if this table contains the specified record
	 * @param record The record to test for membership in this table
	 * @return true if this table contains the record and false if not
	 */
	public boolean hasRecord( final GenericRecord record ) {
		return records().contains( record );
	}
    
    
    /** Add the record to the table. */
    public void add( final GenericRecord record ) throws AddRecordException {
		_keyTable.add( record );			
        NOTICE_PROXY.recordAdded( this, record );
    }
    
    
    /** Remove the specified record from this table. */
    public void remove( final GenericRecord record ) {
		_keyTable.remove( record );			
        NOTICE_PROXY.recordRemoved( this, record );
    }
    
    
    /** Return the attributes of this table from the table's schema. */
    public Collection<DataAttribute> attributes() {
		return _schema.attributes();			
    }
	
	
	/**
	 * Order the records according to the specified sort ordering.
	 * @param records the records to sort.
	 * @param ordering the sort ordering used to sort the records
	 * @return The records sorted by the sort ordering.
	 */
	public List<GenericRecord> orderRecords( final Collection<GenericRecord> records, final SortOrdering ordering ) {
		final List<GenericRecord> results = new ArrayList<GenericRecord>( records );			
		Collections.sort( results, ordering );
		return results;
	}
    
    
    /** Fetch all records held in the table */
    public Collection<GenericRecord> records() {
		return _keyTable.records();
    }
	
	
	/**
	 * Fetch all records held in the table and sort them according to the sort ordering.
	 * @param ordering The sort ordering used to sort the records.
	 * @return All of the records ordered according to the sort ordering.
	 */
	public List<GenericRecord> getRecords( final SortOrdering ordering ) {
		return orderRecords( _keyTable.records(), ordering );			
	}
    
    
    /**  
     * Fetch the record with matching key/value pair bindings.  The keys 
     * must be one or more of the primary keys.  If the record is not unique,
     * an exception will be thrown.
     */
    public <ValueType extends Object> GenericRecord record( final Map<String,ValueType> bindings ) throws NonUniqueRecordException {
		return _keyTable.record( bindings );			
    }
    
    
    /**  
     * Fetch the record with a matching key/value pair binding.  The key 
     * must be a primary key.  If the record is not unique,
     * an exception will be thrown.
     */
    public GenericRecord record( final String key, final Object value ) throws NonUniqueRecordException {
		return _keyTable.record( key, value );			
    }
    
    
    /**  
     * Fetch the records with matching key/value pair bindings.  The keys 
     * must be one or more of the primary keys.
	 * @param bindings The map of key/value pairs where the keys correspond to a subset of primary keys and the values are the ones we want to match.
	 * @return The matching records.
     */
    public <ValueType extends Object> Collection<GenericRecord> records( final Map<String,ValueType> bindings ) {
		return _keyTable.records( bindings );			
    }
	
	
	/**
     * Fetch the records with matching key/value pair bindings and sort them according to the sort ordering.
     * The keys must be one or more of the primary keys.
	 * @param bindings The map of key/value pairs where the keys correspond to a subset of primary keys and the values are the ones we want to match.
	 * @param ordering The sort ordering used to sort the records.
	 * @return The matching records sorted according to the ordering.
	 */
	public <ValueType extends Object> List<GenericRecord> getRecords( final Map<String,ValueType> bindings, final SortOrdering ordering ) {
		return orderRecords( records( bindings ), ordering );
	}
    
    
    /**  
     * Fetch the records with a matching key/value pair binding.  The key 
     * must be a primary key.
	 * @param key A primary key to fetch against.
	 * @param value The value of the primary key to match.
	 * @return the matching records.
     */
    public Collection<GenericRecord> records( final String key, final Object value ) {
		return _keyTable.records( key, value );			
    }
	
	
	/**
     * Fetch the records with a matching key/value pair binding and sort them according to the sort ordering.
     * The key must be a primary key.
	 * @param key A primary key to fetch against.
	 * @param value The value of the primary key to match.
	 * @param ordering The sort ordering used to sort the records.
	 * @return The matching records sorted according to the ordering.
	 */
	public List<GenericRecord> getRecords( final String key, final Object value, final SortOrdering ordering ) {
		return orderRecords( records( key, value ), ordering );
	}
    
    
    /**  
     * Fetch the record with a matching nodeId.  One of the primary keys
     * must be "nodeId".  If the record is not unique, an exception will be thrown.
     */
    public GenericRecord recordForNode( final String nodeId ) throws NonUniqueRecordException {
        return record( NODE_KEY, nodeId );
    }
        
    
    /**  Fetch the records with a matching nodeId.  One of the primary keys must be "nodeId". */
    public Collection<GenericRecord> recordsForNode( final String nodeId ) {
        return records( NODE_KEY, nodeId );
    }
	
	
	/**
	 * Get the unique values of the specified primary key column.
	 * @param key The primary key column whose unique values we want to fetch.
	 * @return the unique values of the specified column.
	 */
	final public Collection<Object> getUniquePrimaryKeyValues( final String key ) {
		return _keyTable.getUniquePrimaryKeyValues( key );			
	}
    
	
    /** Reindex the record based on new primary key values (if any). */
	synchronized final void reIndex( final GenericRecord record, final String key, final Object oldValue ) {
		if ( _schema.isPrimaryKey( key ) && this.hasRecord( record ) ) {
			_keyTable.reIndex( record, key, oldValue );
		}
	}
	
	
    
    /*************************************************************************
     * KeyTable holds a map (valueTable) whose keys are the primary keys and whose values are ValueHash tables.  Each ValueHash table corresponds to a single primary key.
     */
    final private class KeyTable {
		/** value hashes keyed by the primary key name */
        final private Map<String,ValueHash> VALUE_TABLE;
        
        
		/** Constructor */
        public KeyTable() {
            VALUE_TABLE = new HashMap<String,ValueHash>();
            
			for ( final String key : _schema.primaryKeys() ) {
				final ValueHash valueHash = new ValueHash( key );
				VALUE_TABLE.put( key, valueHash );
			}				
        }
        
        
        /** 
		 * Return all records in the table
		 * @return all records in the table
		 */
        public Collection<GenericRecord> records() {
			final Collection<ValueHash> valueHashes = VALUE_TABLE.values();
			
			// each value hash has a copy of all of the records, so we only need one
			final Iterator<ValueHash> valueHashIter = valueHashes.iterator();
						
			// every value hash contains all records, so we only need one
			return valueHashes.isEmpty() ? Collections.<GenericRecord>emptySet() : valueHashIter.next().records();				
        }
        
        
        /** Get a record matching all of the primary key bindings. Bindings should include all primary keys to ensure a unique record. */
        public <ValueType extends Object> GenericRecord record( final Map<String,ValueType> bindings ) throws NonUniqueRecordException {
            final Collection<GenericRecord> records = records( bindings );
            
            if ( records.size() > 1 ) {
                throw new NonUniqueRecordException( bindings );
            }
            
            final Iterator<GenericRecord> recordIter = records.iterator();
			return recordIter.hasNext() ? recordIter.next() : null; 
        }
        
        
        /** Get a record matching the specified primary key value. The key should be the sole primary key to assure a unique record. */
        public GenericRecord record( final String key, final Object value ) throws NonUniqueRecordException {
            final Map<String,Object> bindings = new HashMap<String,Object>(1);
            bindings.put( key, value );
            
            return record( bindings );
        }
        
        
		/** Fetch all records matching the primary key bindings. You may use a subset of primary keys since multiple records may be returned. */
        public <ValueType extends Object> Collection<GenericRecord> records( final Map<String,ValueType> bindings ) {
            final Collection<GenericRecord> records = new HashSet<GenericRecord>();
            final Set<Map.Entry<String,ValueType>> entries = bindings.entrySet();
            
            if ( entries.size() == 0 )  return Collections.<GenericRecord>emptySet();
            
            final Iterator<Map.Entry<String,ValueType>> entryIter = entries.iterator();
            Map.Entry<String,ValueType> entry = entryIter.next();
			Collection<GenericRecord> entryRecords = records( entry );
            records.addAll( entryRecords );
            while ( entryIter.hasNext() && !records.isEmpty() ) {
                entry = entryIter.next();
                entryRecords = records( entry );
                records.retainAll( entryRecords );
            }
            
            return records;
        }
        
        
		/** Fetch the records matching the key/value pair specified in the entry. */
        private <ValueType extends Object> Collection<GenericRecord> records( final Map.Entry<String,ValueType> entry ) {
            final String key = entry.getKey();
            final Object value = entry.getValue();
            return records( key, value );
        }
        
        
		/** Get all of the records matching the specified primary key/value pair */
        public Collection<GenericRecord> records( final String key, final Object value ) {
			return valueTable( key ).records( value );				
        }
        
        
		/** Get the value table corresponding to the specified primary key */
        private ValueHash valueTable( final String key ) {
			return VALUE_TABLE.get( key );				
        }
		
		
		/**
		 * Get the unique values of the specified column.
		 * @param key The column whose unique values we want to fetch.
		 * @return the unique values of the specified column.
		 */
		final protected Collection<Object> getUniquePrimaryKeyValues( final String key ) {
			return valueTable( key ).getUniqueKeyValues();
		}
        
        
		/** Get the primary key bindings associated with the specified record. */
        private Map<String,Object> primaryBindings( final GenericRecord record ) {
            final Map<String,Object> bindings = new HashMap<String,Object>();
			for ( final String key : _schema.primaryKeys() ) {
				final Object value = record.valueForKey( key );
				bindings.put( key, value );
			}
			
			return bindings;
        }
        
        
		/**
		 * Determine whether there is an existing record with the same primary bindings as the specified record.
		 * @param record the record whose primary key bindings we wish to test
		 * @return true if there is an existing record with the same primary key bindings as the specified record and false otherwise
		 */
        private boolean hasConflictingRecord( final GenericRecord record ) {
            final Map<String,Object> bindings = primaryBindings( record );
            return record( bindings ) != null;
        }
		
		
		/**
		 * Re-index the hash table for a change in the specified record's value for the specified key
		 * @param record the record whose primary key value has changed
		 * @param key the primary key associated with the modified value
		 * @param oldValue old value associated with the specified primary key
		 */
		final public void reIndex( final GenericRecord record, final String key, final Object oldValue ) {
			valueTable( key ).reIndex( record, oldValue );				
		}
        
        
		/**
		 * Add the specified record to the hash table
		 * @param record the record to add
		 */
        public void add( final GenericRecord record ) throws AddRecordException {
            if ( hasConflictingRecord( record ) ) {
                throw new AddRecordException( record );
            }

            for ( final String key : _schema.primaryKeys() ) {
                final ValueHash valueHash = valueTable( key );
                valueHash.add( record );
            }
        }
        
        
		/**
		 * Remove the specified record from the hash table
		 * @param record the record to remove
		 */
        public void remove( final GenericRecord record ) {
            for ( final String key : _schema.primaryKeys() ) {
                final ValueHash valueHash = valueTable( key );
                valueHash.remove( record );
            }
        }
    }
    
    
    /**************************************************************************
     * ValueHash is a class used to index values associated with primary keys.  An instance is associated with a single primary key.  The contained map
     * holds all values associated with the primary key.  The value acts as the key in the map and the set of all records sharing that value is the 
     * value in the map.  Each ValueHash contains exactly one reference to each record in the table.
     */
    final private class ValueHash {
		/** The primary key for which to maintain a hash of values. */
        final private String PRIMARY_KEY;
		
		/** Map of record sets associated with a value for the primary key. */
        final private Map<Object,Set<GenericRecord>> RECORD_SET_TABLE;
        
		
		/**
		 * Constructor
		 * @param primaryKey the primary key for which to hash values.
		 */
        public ValueHash( final String primaryKey ) {
            this.PRIMARY_KEY = primaryKey;
            RECORD_SET_TABLE = new HashMap<Object,Set<GenericRecord>>();
        }
		
		
		/**
		 * Get the unique values of the primary key.
		 * @return The unique values of the primary key.
		 */
		final public Collection<Object> getUniqueKeyValues() {
			return RECORD_SET_TABLE.keySet();
		}
        
        
		/** Get all records in this value hash */
        final public Collection<GenericRecord> records() {
            final Collection<GenericRecord> unionRecordSet = new HashSet<GenericRecord>();
            final Collection<Set<GenericRecord>> recordSets = RECORD_SET_TABLE.values();
            
			for ( final Set<GenericRecord> recordSet : recordSets ) {
                unionRecordSet.addAll( recordSet );
			}
            
            return unionRecordSet;
        }
        
        
		/** Get all records whose primary key matches the specified value */
        final public Set<GenericRecord> records( final Object value ) {
            final Set<GenericRecord> records = RECORD_SET_TABLE.get( value );
            return records != null ? records : Collections.<GenericRecord>emptySet();
        }
        
        
		/** add a record keyed by its primary key */
        final public void add( final GenericRecord record ) {
            final Object value = record.valueForKey( PRIMARY_KEY );
            Set<GenericRecord> recordSet = RECORD_SET_TABLE.get( value );
            
            if ( recordSet == null ) {
                recordSet = new HashSet<GenericRecord>();
                RECORD_SET_TABLE.put( value, recordSet );
            }
            recordSet.add( record );
        }
        
        
		/** remove the specified record from the hash */
        final public void remove( final GenericRecord record ) {
            Object value = record.valueForKey( PRIMARY_KEY );
            Set<GenericRecord> recordSet = RECORD_SET_TABLE.get( value );
            
            if ( recordSet == null )  return;
            
            recordSet.remove( record );
        }
		
		
		/** re-index this hash for the specified record replacing the record's old value with the new one */
		final public void reIndex( final GenericRecord record, final Object oldValue ) {
			records( oldValue ).remove( record );
			add( record );
		}
		
		
		/** Get a string representation of this ValueHash */
		public String toString() {
			return PRIMARY_KEY + ": " + RECORD_SET_TABLE.toString();
		}
    }
    
    
    
    /**************************************************************************
     * This exception is thrown when attempting to fetch a single record 
     * with bindings and more than one record matches the criteria.
     */
    static public class NonUniqueRecordException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;

        private Map<String,Object> bindings;
        
		
		/** Constructor */
        @SuppressWarnings( "unchecked" )    // exception classes don't support generics so we have no choice but to cast
        public <ValueType> NonUniqueRecordException( final Map<String,ValueType> theBindings ) {
            bindings = (Map<String,Object>)theBindings;
        }
        
        
		/**
		 * Get the exception message.
		 */
        public String getMessage() {
            String message = "Attempt to get a unique record for the bindings: " + bindings;
            return message;
        }
    }
    
    
    
    /************************************************************************
     * Exception thrown when attempting to add a record which causes an 
     * inconsistency.  Most likely this happens when a record with the same
     * primary key(s) already exist in the table.
     */
    public class AddRecordException extends RuntimeException {
        /** serialization ID */
        private static final long serialVersionUID = 1L;
        
        private GenericRecord record;

        
		/**
		 * Constructor 
		 */
        public AddRecordException( final GenericRecord aRecord ) {
            record = aRecord;
        }
        
        
		/**
		 * Get the exception message.
		 */
        public String getMessage() {
            String message = "Failed attempt to add the record: " + record;
            return message;
        }
    }
    
    
    
    /** This class represents the schema of the table which specifies the attributes belonging to the table. */
    public class Schema implements DataListener {
		/** map of attributes keyed by name */
        final protected Map<String,DataAttribute> ATTRIBUTE_TABLE;
		
		/** collection of primary keys  */
        final protected Collection<String> PRIMARY_KEYS;

        
		/** Empty Constructor */
        public Schema() {
            this( Collections.<DataAttribute>emptySet() );
        }
        
        
		/** Primary constructor */
        public Schema( final Collection<DataAttribute> attributes ) throws MissingPrimaryKeyException {
            ATTRIBUTE_TABLE = new HashMap<String,DataAttribute>();
            PRIMARY_KEYS = new HashSet<String>();
            addAttributes( attributes );
        }
        
        
		/** Get all of the keys for the schema. */
        public Set<String> keys() {
            return ATTRIBUTE_TABLE.keySet();
        }
        
        
		/** Validate that the primary keys are defined for this schema. */
        protected void validatePrimaryKeys() throws MissingPrimaryKeyException {
            if ( PRIMARY_KEYS.isEmpty() ) {
                throw new MissingPrimaryKeyException( DataTable.this.name() );
            }
        }
        
        
		/**
		 * Get the collection of all attributes in this schema
		 * @return the collection of all attributes in this schema
		 */
        public Collection<DataAttribute> attributes() {
            return ATTRIBUTE_TABLE.values();
        }
        
        
		/**
		 * Add to the schema each attribute in the specified collection
		 * @param attributes The collection of attributes to add to the schema
		 */
        private void addAttributes( final Collection<DataAttribute> attributes ) {
			for ( final DataAttribute attribute : attributes ) {
                addAttribute( attribute );
			}
        }
        
        
		/**
		 * Add a new attribute to the schema
		 * @param attribute The attribute to add to the schema
		 */
        public void addAttribute( final DataAttribute attribute ) {
            final String attributeName = attribute.name();
            
            ATTRIBUTE_TABLE.put( attributeName, attribute );
            
            if ( attribute.isPrimaryKey() ) {
                PRIMARY_KEYS.add( attributeName );
            }
        }
        
        
		/**
		 * Get the collection of primary keys
		 * @return The collection of primary keys
		 */
        public Collection<String> primaryKeys() {
            return PRIMARY_KEYS;
        }
		
		
		/**
		 * Determine if the specified key is a primary key
		 * @param key The key to test for being a primary key
		 * @return true if the key is a primary key and false if it isn't a primary key
		 */
		public boolean isPrimaryKey( final String key ) {
			return PRIMARY_KEYS.contains( key );
		}
        
        
		/** Get the data label. */
        public String dataLabel() {
            return "schema";
        }
        
        
		/** Update the schema from the data adaptor. */
        public void update( final DataAdaptor schemaAdaptor ) throws MissingPrimaryKeyException {
			final List<DataAdaptor> attributeAdaptors = schemaAdaptor.childAdaptors( "attribute" );
			for ( final DataAdaptor attributeAdaptor : attributeAdaptors ) {
                final DataAttribute attribute = new DataAttribute( attributeAdaptor );  
                addAttribute( attribute );
            }
            validatePrimaryKeys();
        }
        
        
		/** Write the schema out to the data adaptor. */
        public void write( final DataAdaptor schemaAdaptor ) throws MissingPrimaryKeyException {
            validatePrimaryKeys();
            
            final Collection<DataAttribute> attributes = attributes();
			for ( final DataAttribute attribute : attributes ) {
                final DataListener attributeWriter = attribute.readerWriter();
                schemaAdaptor.writeNode( attributeWriter );
            }
        }
    }
}


