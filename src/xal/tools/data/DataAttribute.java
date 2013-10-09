/*
 * DataAttribute.java
 *
 * Created on May 22, 2002, 2:35 PM
 */

package xal.tools.data;

import java.util.logging.*;


/**
 * DataAttribute class
 * @author  tap
 */
public class DataAttribute {
	private String DEFAULT_VALUE;
    private String name;
    private Class<?> type;
    private boolean isPrimaryKey;

	
    /** Creates new DataAttribute */
    public DataAttribute( String aName, Class<?> aType, boolean primaryState, String defaultValue ) {
        name = aName;
        type = aType;
        isPrimaryKey = primaryState;
		DEFAULT_VALUE = defaultValue;
    }

	
    /** Creates new DataAttribute */
    public DataAttribute( String aName, Class<?> aType, boolean primaryState ) {
		this( aName, aType, primaryState, null );
    }

    
    public DataAttribute( DataAdaptor adaptor ) {
        DataListener reader = readerWriter();
        reader.update(adaptor);
    }
    
    
    public DataListener readerWriter() {
        return new ReaderWriter();
    }
    
    
    public String name() {
        return name;
    }
    
    
    public Class<?> type() {
        return type;
    }
    
    
    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }
	
	
	/**
	 * Get the default value to assign for this attribute if a value is not specified.
	 * @return the default value for this attribute
	 */
	public String getDefaultStringValue() {
		return DEFAULT_VALUE;
	}
    
    
    
    /*
     * ReaderWriter is responsible for reading and writing a DataAttribute 
     * object based on a DataAdaptor adaptor.
     */
    private class ReaderWriter implements DataListener {
        public String dataLabel() {
            return "attribute";
        }
        
        
        public void update( DataAdaptor adaptor ) {
            name = adaptor.stringValue("name");
            try {
                String typeName = adaptor.stringValue("type");
                type = Class.forName(typeName);
            }
            catch( Exception exception ) {
                System.err.println( exception );
				Logger.getLogger("global").log( Level.SEVERE, "Error during update.", exception );
                exception.printStackTrace();
            }
            
            if ( adaptor.hasAttribute("isPrimaryKey") ) {
                isPrimaryKey = adaptor.booleanValue("isPrimaryKey");
            }
            else {
                isPrimaryKey = false;
            }
			
			if ( adaptor.hasAttribute( "defaultValue" ) ) {
				DEFAULT_VALUE = adaptor.stringValue( "defaultValue" );
			}
        }
        
        
        public void write( DataAdaptor adaptor ) {
            adaptor.setValue("name", name);
            
            String typeName = type.getName();
            adaptor.setValue("type", typeName);
            
            adaptor.setValue("isPrimaryKey", isPrimaryKey);
			
			if ( DEFAULT_VALUE != null ) {
				adaptor.setValue( "defaultValue", DEFAULT_VALUE );
			}
        }
    }
}
