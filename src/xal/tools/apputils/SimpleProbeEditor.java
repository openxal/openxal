/*
 * SimpleProbeEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Tom Pelaia
 * @author Patrick Scruggs
 */

package xal.tools.apputils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Frame;
import javax.swing.*;
import javax.swing.table.*;
import java.util.*;

import xal.model.probe.Probe;
import xal.model.IAlgorithm;
import xal.tools.bricks.*;
import xal.tools.swing.KeyValueFilteredTableModel;
import xal.tools.data.KeyValueAdaptor;
import java.beans.*;
import java.lang.reflect.*;


/** SimpleProbeEditor */
public class SimpleProbeEditor extends JDialog {
    /** Private serializable version ID */
    private static final long serialVersionUID = 1L;

    /** Table model of ProbeProperty records */
    final private KeyValueFilteredTableModel<PropertyRecord> PROPERTY_TABLE_MODEL;

    /** List of properties that appear in the properties table */
    final private List<PropertyRecord> PROBE_PROPERTY_RECORDS;

    /** Probe that is being edited */
    final private Probe PROBE;

	
    /* Constructor that takes a window parent
     * and a probe to fetch properties from
     */
    public SimpleProbeEditor( final Frame owner, final Probe probe ) {
        super( owner, "Probe Editor", true );	//Set JDialog's owner, title, and modality
        
        PROBE = probe;					// Set the probe to edit

		// generate the probe property tree
		final EditablePropertyContainer probePropertyTree = EditableProperty.getInstanceWithRoot( "Probe", probe );
		//System.out.println( probePropertyTree );

		PROBE_PROPERTY_RECORDS = PropertyRecord.toRecords( probePropertyTree );

		PROPERTY_TABLE_MODEL = new KeyValueFilteredTableModel<>( PROBE_PROPERTY_RECORDS, "path", "value", "units" );
		PROPERTY_TABLE_MODEL.setColumnName( "path", "Property" );
		PROPERTY_TABLE_MODEL.setColumnEditKeyPath( "value", "editable" );

        setSize( 600, 600 );			// Set the window size
        initializeComponents();			// Set up each component in the editor
        pack();							// Fit the components in the window
        setLocationRelativeTo( owner );	// Center the editor in relation to the frame that constructed the editor
        setVisible(true);				// Make the window visible
    }
	
    
    /** 
	 * Get the probe to edit
     * @return probe associated with this editor
     */
    public Probe getProbe() {
        return PROBE;
    }

    
    /** Initialize the components of the probe editor */
    public void initializeComponents() {
        //Panel containing all elements
        final JPanel mainContainer = new JPanel();
        //Set the layout of the panel to a BorderLayout
        mainContainer.setLayout( new BorderLayout() );
        //Panel containing apply and close button with a 1 row, 2 column grid layout
        final JPanel controlPanel = new JPanel( new GridLayout(1, 2) );

        //Apply button
        final JButton applyButton = new JButton( "Apply" );
        applyButton.setEnabled( false );

        //Close button
        final JButton closeButton = new JButton( "Close" );
        
        //Set the close button's action to close the dialog
        closeButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        //Add the action listener as the ApplyButtonListener
        applyButton.addActionListener( new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		
        //Add the close button to the button panel
        controlPanel.add( closeButton );
        //Add the apply button to the button panel
        controlPanel.add( applyButton );

        //Table containing the properties that can be modified
        final JTable propertyTable = new JTable() {
            //Serializable version ID
            private static final long serialVersionUID = 1L;
            
            //Get the cell editor for the table
            @Override
            public TableCellEditor getCellEditor(int row, int col) {
                //Value at [row, col] of the table
                Object value = getValueAt(row, col);
                
                //Set the appropriate editor for each value type
                if( value instanceof Boolean )
                    return getDefaultEditor( Boolean.class );
                else if( value instanceof Double )
                    return getDefaultEditor( Double.class );
                else if( value instanceof Integer )
                    return getDefaultEditor( Integer.class );
                
                //Default editor (String type)
                return super.getCellEditor( row, col );
            }
            
            //Get the cell renderer for the table to change how values are displayed
            @Override
            public TableCellRenderer getCellRenderer(int row, int col) {
                //Value at [row, col]
                Object value = getValueAt(row, col);
 
                //Set the renderer of each type
                //Boolean = checkbox display
                //Double/Int = right aligned display
                if( value instanceof Boolean )
                    return getDefaultRenderer( Boolean.class );
                else if( value instanceof Double )
                    return getDefaultRenderer( Double.class );
                else if( value instanceof Integer )
                    return getDefaultRenderer( Integer.class );
                
                //Default = left aligned string display
                return super.getCellRenderer( row, col );
            }
        };
        
        //Set the table to allow one-click edit
        ((DefaultCellEditor) propertyTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
        
        //Resize the last column
        propertyTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN);
        //Allow single selection only
		propertyTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        //Set the model to the table
        propertyTable.setModel( PROPERTY_TABLE_MODEL );

        //Configure the text field to filter the table
        final JTextField filterTextField = new JTextField();
        filterTextField.putClientProperty( "JTextField.variant", "search" );
        filterTextField.putClientProperty( "JTextField.Search.Prompt", "Property Filter" );
		PROPERTY_TABLE_MODEL.setInputFilterComponent( filterTextField );
        mainContainer.add( filterTextField, BorderLayout.NORTH );

        //Add the scrollpane to the table with a vertical scrollbar
        final JScrollPane scrollPane = new JScrollPane( propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        mainContainer.add( scrollPane, BorderLayout.CENTER );

        //Add the buttons to the bottom of the dialog
        mainContainer.add( controlPanel, BorderLayout.SOUTH );

        //Add everything to the dialog
        add( mainContainer );
    }
}



/** Wraps a property for display as a record in a table */
class PropertyRecord {
	/** wrapped property */
	final private EditableProperty PROPERTY;

	/** current value which may be pending */
	private Object _value;

	/** indicates that this record has unsaved changes */
	private boolean _hasChanges;


	/** Constructor */
	public PropertyRecord( final EditableProperty property ) {
		PROPERTY = property;

		// the value is only meaningful for primitive properties (only thing we want to display)
		_value = property.isPrimitive() ? PROPERTY.getValue() : null;
		_hasChanges = false;
	}


	/** name of the property */
	public String getName() {
		return PROPERTY.getName();
	}


	/** Get the path to this property */
	public String getPath() {
		return PROPERTY.getPath();
	}


	/** Get the property type */
	public Class<?> getPropertyType() {
		return PROPERTY.getPropertyType();
	}


	/** Get the value for this property */
	public Object getValue() {
		return _value;
	}


	/** set the pending value */
	public void setValueAsObject( final Object value ) {
		if ( isEditable() ) {
			_hasChanges = true;
			_value = value;
		}
	}


	/** set the pending value */
	public void setValue( final boolean value ) {
		setValueAsObject( Boolean.valueOf( value ) );
	}


	/** Set the pending string value. Most values (except for boolean) are set as string since the table cell editor does so. */
	public void setValue( final String value ) {
		final Class<?> rawType = getPropertyType();
		if ( rawType == String.class ) {
			setValueAsObject( value );
		}
		else {
			try {
				final Class<?> type = rawType.isPrimitive() ? _value.getClass() : rawType;	// convert to wrapper type (e.g. double.class to Double.class) if necessary
				final Object objectValue = toObjectOfType( value, type );
				setValueAsObject( objectValue );
			}
			catch( Exception exception ) {
				System.err.println( "Exception: " + exception );
				System.err.println( "Error parsing the value: " + value + " as " + rawType );
			}
		}
	}


	/** Convert the string to an Object of the specified type */
	static private Object toObjectOfType( final String stringValue, final Class<?> type ) {
		try {
			// every wrapper class has a static method named "valueOf" that takes a String and returns a corresponding instance of the wrapper
			final Method converter = type.getMethod( "valueOf", String.class );
			return converter.invoke( null, stringValue );
		}
		catch ( Exception exception ) {
			throw new RuntimeException( "No match to parse string: " + stringValue + " as " + type );
		}
	}


	/** synonym for isEditable so the table model will work */
	public boolean getEditable() {
		return isEditable();
	}


	/** only primitive properties are editable */
	public boolean isEditable() {
		return PROPERTY.isPrimitive();
	}


	/** indicates whether this record has unpublished changes */
	public boolean hasChanges() {
		return _hasChanges;
	}


	/** publish the pending value to the underlying property if editable and marked with unpublished changes */
	public void publishIfNeeded() {
		if ( isEditable() && hasChanges() ) {
			publish();
		}
	}


	/** publish the pending value to the underlying property */
	public void publish() {
		PROPERTY.setValue( _value );
		_hasChanges = false;
	}


	/** Get the units */
	public String getUnits() {
		return PROPERTY.getUnits();
	}


	/** Generate a flat list of records from the given property tree */
	static public List<PropertyRecord> toRecords( final EditablePropertyContainer propertyTree ) {
		final List<PropertyRecord> records = new ArrayList<>();
		appendPropertiesToRecords( propertyTree, records );
		return records;
	}


	/** append the properties in the given tree to the records nesting deeply */
	static private void appendPropertiesToRecords( final EditablePropertyContainer propertyTree, final List<PropertyRecord> records ) {
		// add all the primitive properties
		final List<EditablePrimitiveProperty> properties = propertyTree.getChildPrimitiveProperties();
		for ( final EditablePrimitiveProperty property : properties ) {
			records.add( new PropertyRecord( property ) );
		}

		// navigate down through each container and append their sub trees
		final List<EditablePropertyContainer> containers = propertyTree.getChildPropertyContainers();
		for ( final EditablePropertyContainer container : containers ) {
			records.add( new PropertyRecord( container ) );		// add the container itself
			appendPropertiesToRecords( container, records );	// add the containers descendents
		}
	}
}



/** base class for a editable property */
abstract class EditableProperty {
    /** array of classes for which the property can be edited directly */
    final static protected Set<Class<?>> EDITABLE_PROPERTY_TYPES = new HashSet<>();

	/** property name */
	final protected String NAME;

	/** path to this property */
	final protected String PATH;

	/** target object which is assigned the property */
	final protected Object TARGET;

	/** property descriptor */
	final protected PropertyDescriptor PROPERTY_DESCRIPTOR;


	// static initializer
	static {
		// cache the editable properties in a set for quick comparison later
		final Class<?>[] editablePropertyTypes = { Double.class, Double.TYPE, Integer.class, Integer.TYPE, Boolean.class, Boolean.TYPE, String.class };
		for ( final Class<?> type : editablePropertyTypes ) {
			EDITABLE_PROPERTY_TYPES.add( type );
		}
	}


	/** Constructor */
	protected EditableProperty( final String pathPrefix, final String name, final Object target, final PropertyDescriptor descriptor ) {
		NAME = name;
		PATH = pathPrefix != null && pathPrefix.length() > 0 ? pathPrefix + "." + name : name;
		TARGET = target;
		PROPERTY_DESCRIPTOR = descriptor;
	}


	/** Constructor */
	protected EditableProperty( final String pathPrefix, final Object target, final PropertyDescriptor descriptor ) {
		this( pathPrefix, descriptor.getName(), target, descriptor );
	}


	/** Get an instance starting at the root object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object root ) {
		return EditablePropertyContainer.getInstanceWithRoot( name, root );
	}


	/** name of the property */
	public String getName() {
		return NAME;
	}


	/** Get the path to this property */
	public String getPath() {
		return PATH;
	}


	/** Get the property type */
	public Class<?> getPropertyType() {
		return PROPERTY_DESCRIPTOR != null ? PROPERTY_DESCRIPTOR.getPropertyType() : null;
	}


	/** Get the value for this property */
	public Object getValue() {
		if ( TARGET != null && PROPERTY_DESCRIPTOR != null ) {
			final Method getter = PROPERTY_DESCRIPTOR.getReadMethod();
			try {
				return getter.invoke( TARGET );
			}
			catch( Exception exception ) {
				System.err.println( exception );
				return null;
			}
		}
		else {
			return null;
		}
	}


	/** set the value */
	abstract public void setValue( final Object value );


	/** Get the units */
	public String getUnits() {
		return null;
	}


	/** determine whether the property is a container */
	abstract public boolean isContainer();

	/** determine whether the property is a primitive */
	abstract public boolean isPrimitive();


    /*
     * Get the property descriptors for the given bean info
     * @param target object for which to get the descriptors
	 * @return the property descriptors for non-null beanInfo otherwise null
     */
    static protected PropertyDescriptor[] getPropertyDescriptors( final Object target ) {
		if ( target != null ) {
			final BeanInfo beanInfo = getBeanInfo( target );
			return getPropertyDescriptorsForBeanInfo( beanInfo );
		}
		else {
			return null;
		}
	}


    /*
     * Get the property descriptors for the given bean info
     * @param beanInfo bean info
	 * @return the property descriptors for non-null beanInfo otherwise null
     */
    static private PropertyDescriptor[] getPropertyDescriptorsForBeanInfo( final BeanInfo beanInfo ) {
		return beanInfo != null ? beanInfo.getPropertyDescriptors() : null;
	}


    /** Convenience method to get the BeanInfo for an object's class */
	static private BeanInfo getBeanInfo( final Object object ) {
		if ( object != null ) {
			return getBeanInfoForType( object.getClass() );
		}
		else {
			return null;
		}
	}


    /** Convenience method to get the BeanInfo for the given type */
	static private BeanInfo getBeanInfoForType( final Class<?> propertyType ) {
		if ( propertyType != null ) {
			try {
				return Introspector.getBeanInfo( propertyType );
			}
			catch( IntrospectionException exception ) {
				return null;
			}
		}
		else {
			return null;
		}
	}


	/** Get a string represenation of this property */
	public String toString() {
		return getPath();
	}
}



/** editable property representing a primitive that is directly editable */
class EditablePrimitiveProperty extends EditableProperty {
	/** property's units */
	final private String UNITS;


	/** Constructor */
	protected EditablePrimitiveProperty( final String pathPrefix, final Object target, final PropertyDescriptor descriptor ) {
		super( pathPrefix, target, descriptor );

		UNITS = fetchUnits();
	}


	/** fetch the units */
	private String fetchUnits() {
		// form the accessor as get<PropertyName>Units() replacing <PropertyName> with the property's name whose first character is upper case
		final char[] nameChars = getName().toCharArray();
		nameChars[0] = Character.toUpperCase( nameChars[0] );		// capitalize the first character of the name
		final String propertyName = String.valueOf( nameChars );	// property name whose first character is upper case

		// first look for a method of the form get<PropertyName>Units() taking no arguments and returning a String
		final String unitsAccessorName = "get" + propertyName + "Units";
		try {
			final Method unitsAccessor = TARGET.getClass().getMethod( unitsAccessorName );
			if ( unitsAccessor.getReturnType() == String.class ) {
				return (String)unitsAccessor.invoke( TARGET );
			}
		}
		catch ( NoSuchMethodException exception ) {
			// fallback look for a method of the form getUnitsForProperty( String name ) returning a String
			try {
				final Method unitsAccessor = TARGET.getClass().getMethod( "getUnitsForProperty", String.class );
				if ( unitsAccessor.getReturnType() == String.class ) {
					return (String)unitsAccessor.invoke( TARGET, getName() );
				}
				return "";
			}
			catch( Exception fallbackException ) {
				return "";
			}
		}
		catch( Exception exception ) {
			System.out.println( exception );
			return "";
		}

		return "";
	}


	/** determine whether the property is a container */
	public boolean isContainer() {
		return false;
	}


	/** determine whether the property is a primitive */
	public boolean isPrimitive() {
		return true;
	}


	/** Set the value for this property */
	public void setValue( final Object value ) {
		if ( TARGET != null && PROPERTY_DESCRIPTOR != null ) {
			final Method setter = PROPERTY_DESCRIPTOR.getWriteMethod();
			try {
				setter.invoke( TARGET, value );
			}
			catch( Exception exception ) {
				throw new RuntimeException( "Cannot set value " + value + " on target: " + TARGET + " with descriptor: " + PROPERTY_DESCRIPTOR.getName(), exception );
			}
		}
		else {
			if ( TARGET == null && PROPERTY_DESCRIPTOR == null ) {
				throw new RuntimeException( "Cannot set value " + value + " on target because both the target and descriptor are null." );
			}
			else if ( TARGET == null ) {
				throw new RuntimeException( "Cannot set value " + value + " on target with descriptor: " + PROPERTY_DESCRIPTOR.getName() + " because the target is null." );
			}
			else if ( PROPERTY_DESCRIPTOR == null ) {
				throw new RuntimeException( "Cannot set value " + value + " on target: " + TARGET + " because the property descriptor is null." );
			}
		}
	}


	/** Get the units */
	public String getUnits() {
		return UNITS;
	}


	/** Get a string represenation of this property */
	public String toString() {
		return getPath() + ": " + getValue() + " " + getUnits();
	}
}



/** base class for a container of editable properties */
class EditablePropertyContainer extends EditableProperty {
	/** target for child properties */
	final protected Object CHILD_TARGET;

	/** set of ancestors to reference to prevent cycles */
	final private Set<Object> ANCESTORS;
	
	/** list of child primitive properties */
	protected List<EditablePrimitiveProperty> _childPrimitiveProperties;

	/** list of child property containers */
	protected List<EditablePropertyContainer> _childPropertyContainers;


	/** Primary Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final String name, final Object target, final PropertyDescriptor descriptor, final Object childTarget, final Set<Object> ancestors ) {
		super( pathPrefix, name, target, descriptor );

		CHILD_TARGET = childTarget;
		ANCESTORS = ancestors;
	}


	/** Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Object childTarget, final Set<Object> ancestors ) {
		this( pathPrefix, descriptor.getName(), target, descriptor, childTarget, ancestors );
	}


	/** Constructor */
	protected EditablePropertyContainer( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Set<Object> ancestors ) {
		this( pathPrefix, target, descriptor, generateChildTarget( target, descriptor ), ancestors );
	}


	/** Create an instance with the specified root Object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object rootObject ) {
		final Set<Object> ancestors = new HashSet<Object>();
		return new EditablePropertyContainer( "", name, null, null, rootObject, ancestors );
	}


	/** Generat the child target from the target and descriptor */
	static private Object generateChildTarget( final Object target, final PropertyDescriptor descriptor ) {
		try {
			final Method readMethod = descriptor.getReadMethod();
			return readMethod.invoke( target );
		}
		catch( Exception exception ) {
			return null;
		}
	}

	
	/** determine whether the property is a container */
	public boolean isContainer() {
		return true;
	}


	/** determine whether the property is a primitive */
	public boolean isPrimitive() {
		return false;
	}


	/** set the value */
	public void setValue( final Object value ) {
		throw new RuntimeException( "Usupported operation attempting to set the value of the editable property container: " + getPath() + " with value " + value );
	}


	/** determine whether this container has any child properties */
	public boolean isEmpty() {
		return getChildCount() == 0;
	}


	/** get the number of child properties */
	public int getChildCount() {
		generateChildPropertiesIfNeeded();
		return _childPrimitiveProperties.size() + _childPropertyContainers.size();
	}


	/** Get the child properties */
	public List<EditableProperty> getChildProperties() {
		generateChildPropertiesIfNeeded();
		final List<EditableProperty> properties = new ArrayList<>();
		properties.addAll( _childPrimitiveProperties );
		properties.addAll( _childPropertyContainers );
		return properties;
	}


	/** Get the list of child primitive properties */
	public List<EditablePrimitiveProperty> getChildPrimitiveProperties() {
		generateChildPropertiesIfNeeded();
		return _childPrimitiveProperties;
	}


	/** Get the list of child property containers */
	public List<EditablePropertyContainer> getChildPropertyContainers() {
		generateChildPropertiesIfNeeded();
		return _childPropertyContainers;
	}


	/** generate the child properties if needed */
	protected void generateChildPropertiesIfNeeded() {
		if ( _childPrimitiveProperties == null ) {
			generateChildProperties();
		}
	}


	/** Generate the child properties this container's child target */
	protected void generateChildProperties() {
		_childPrimitiveProperties = new ArrayList<>();
		_childPropertyContainers = new ArrayList<>();

		final PropertyDescriptor[] descriptors = getPropertyDescriptors( CHILD_TARGET );
		if ( descriptors != null ) {
			for ( final PropertyDescriptor descriptor : descriptors ) {
				if ( descriptor.getPropertyType() != Class.class ) {
					generateChildPropertyForDescriptor( descriptor );
				}
			}
		}
	}


	/** Generate the child properties starting at the specified descriptor for this container's child target */
	protected void generateChildPropertyForDescriptor( final PropertyDescriptor descriptor ) {
		final Class<?> propertyType = descriptor.getPropertyType();

		if ( EDITABLE_PROPERTY_TYPES.contains( propertyType ) ) {
			// if the property is an editable primitive with both a getter and setter then return the primitive property instance otherwise null
			final Method getter = descriptor.getReadMethod();
			final Method setter = descriptor.getWriteMethod();
			if ( getter != null && setter != null ) {
				_childPrimitiveProperties.add( new EditablePrimitiveProperty( PATH, CHILD_TARGET, descriptor ) );
			}
			return;		// reached end of branch so we are done
		}
		else if ( propertyType == null ) {
			return;
		}
		else if ( propertyType.isArray() ) {
			// property is an array
			System.out.println( "Property type is array for target: " + CHILD_TARGET + " with descriptor: " + descriptor.getName() );
			return;
		}
		else {
			// property is a plain container
			if ( !ANCESTORS.contains( CHILD_TARGET ) ) {	// only propagate down the branch if the targets are unique (avoid cycles)
				final Set<Object> ancestors = new HashSet<Object>( ANCESTORS );
				ancestors.add( CHILD_TARGET );
				final EditablePropertyContainer container = new EditablePropertyContainer( PATH, CHILD_TARGET, descriptor, ancestors );
				if ( container.getChildCount() > 0 ) {	// only care about containers that lead to editable properties
					_childPropertyContainers.add( container );
				}
			}
			return;
		}
	}


	/** Get a string represenation of this property */
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append( getPath() + ":\n" );
		for ( final EditableProperty property : getChildProperties() ) {
			buffer.append( "\t" + property.toString() + "\n" );
		}
		return buffer.toString();
	}
}



/** container for an editable property that is an array */
class EditableArrayProperty extends EditablePropertyContainer {
	/** Constructor */
	protected EditableArrayProperty( final String pathPrefix, final Object target, final PropertyDescriptor descriptor, final Set<Object> ancestors ) {
		super( pathPrefix, target, descriptor, ancestors );
	}

	// TODO: complete implementation of array property
}