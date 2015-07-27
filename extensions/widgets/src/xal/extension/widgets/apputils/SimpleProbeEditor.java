/*
 * SimpleProbeEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Tom Pelaia
 * @author Patrick Scruggs
 */

package xal.extension.widgets.apputils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Frame;

import javax.swing.*;
import javax.swing.table.*;

import java.util.*;

import xal.tools.annotation.AProperty.NoEdit;
import xal.tools.annotation.AProperty.Units;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.model.probe.Probe;
import xal.extension.widgets.swing.*;
import xal.tools.data.*;

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
    final private Probe<?> PROBE;

	/** model column for the value in the property table */
	final private int PROPERTY_TABLE_VALUE_COLUMN;

	 public SimpleProbeEditor( final Frame owner, final Probe<?> probe ) {
		 this(owner, probe, true);
	 }
	 
    /* Constructor that takes a window parent
     * and a probe to fetch properties from
     */
    public SimpleProbeEditor( final Frame owner, final Probe<?> probe, boolean visible ) {
        super( owner, "Probe Editor", true );	//Set JDialog's owner, title, and modality
        
        PROBE = probe;					// Set the probe to edit

		// generate the probe property tree
		final EditablePropertyContainer probePropertyTree = EditableProperty.getInstanceWithRoot( "Probe", probe );
		//System.out.println( probePropertyTree );

		PROBE_PROPERTY_RECORDS = PropertyRecord.toRecords( probePropertyTree );

		PROPERTY_TABLE_MODEL = new KeyValueFilteredTableModel<>( PROBE_PROPERTY_RECORDS, "displayLabel", "value", "units" );
		PROPERTY_TABLE_MODEL.setMatchingKeyPaths( "path" );					// match on the path
		PROPERTY_TABLE_MODEL.setColumnName( "displayLabel", "Property" );
		PROPERTY_TABLE_MODEL.setColumnEditKeyPath( "value", "editable" );	// the value is editable if the record is editable
		PROPERTY_TABLE_VALUE_COLUMN = PROPERTY_TABLE_MODEL.getColumnForKeyPath( "value" );	// store the column for the "value" key path

        setSize( 600, 600 );			// Set the window size
        initializeComponents();			// Set up each component in the editor
        setLocationRelativeTo( owner );	// Center the editor in relation to the frame that constructed the editor
        setVisible(visible);				// Make the window visible
    }
	
    
    /** 
	 * Get the probe to edit
     * @return probe associated with this editor
     */
    public Probe<?> getProbe() {
        return PROBE;
    }


	/** publish record values to the probe */
	private void publishToProbe() {
		for ( final PropertyRecord record : PROBE_PROPERTY_RECORDS ) {
			record.publishIfNeeded();
		}
		PROPERTY_TABLE_MODEL.fireTableDataChanged();
	}


	/** revert the record values from the probe */
	private void revertFromProbe() {
		for ( final PropertyRecord record : PROBE_PROPERTY_RECORDS ) {
			record.revertIfNeeded();
		}
		PROPERTY_TABLE_MODEL.fireTableDataChanged();
	}

    
    /** Initialize the components of the probe editor */
    public void initializeComponents() {
        //main view containing all components
        final Box mainContainer = new Box( BoxLayout.Y_AXIS );

        // button to revert changes back to last saved state
        final JButton revertButton = new JButton( "Revert" );
		revertButton.setToolTipText( "Revert values back to those in probe." );
        revertButton.setEnabled( false );

        // button to publish changes
        final JButton publishButton = new JButton( "Publish" );
		publishButton.setToolTipText( "Publish values to the probe." );
        publishButton.setEnabled( false );

        // button to publish changes and dismiss the panel
        final JButton okayButton = new JButton( "Okay" );
		okayButton.setToolTipText( "Publish values to the probe and dismiss the dialog." );
        okayButton.setEnabled( true );

        //Add the action listener as the ApplyButtonListener
        revertButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				revertFromProbe();
				revertButton.setEnabled( false );
				publishButton.setEnabled( false );
			}
		});

        //Add the action listener as the ApplyButtonListener
        publishButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				publishToProbe();
				revertButton.setEnabled( false );
				publishButton.setEnabled( false );
			}
		});

        //Add the action listener as the ApplyButtonListener
        okayButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				try {
					publishToProbe();
					dispose();
				}
				catch( Exception exception ) {
					JOptionPane.showMessageDialog( SimpleProbeEditor.this, exception.getMessage(), "Error Publishing", JOptionPane.ERROR_MESSAGE );
					System.err.println( "Exception publishing values to probe: " + exception );
				}
			}
		});

		PROPERTY_TABLE_MODEL.addKeyValueRecordListener( new KeyValueRecordListener<KeyValueTableModel<PropertyRecord>,PropertyRecord>() {
			public void recordModified( final KeyValueTableModel<PropertyRecord> source, final PropertyRecord record, final String keyPath, final Object value ) {
				revertButton.setEnabled( true );
				publishButton.setEnabled( true );
			}
		});

        //Table containing the properties that can be modified
        final JTable propertyTable = new JTable() {
            /** Serializable version ID */
            private static final long serialVersionUID = 1L;

			/** renderer for a table section */
			private final TableCellRenderer SECTION_RENDERER = makeSectionRenderer();

            
            //Get the cell editor for the table
            @Override
            public TableCellEditor getCellEditor( final int row, final int col ) {
                //Value at [row, col] of the table
                final Object value = getValueAt( row, col );

				if ( value == null ) {
					return super.getCellEditor( row, col );
				}
				else {
                    return getDefaultEditor( value.getClass() );
				}
            }
            
            //Get the cell renderer for the table to change how values are displayed
            @Override
            public TableCellRenderer getCellRenderer( final int row, final int column ) {
				// index of the record in the model
				final int recordIndex = this.convertRowIndexToModel( row );
				final PropertyRecord record = PROPERTY_TABLE_MODEL.getRecordAtRow( recordIndex );
				final Object value = getValueAt( row, column );

                //Set the renderer according to the property type (e.g. Boolean => checkbox display, numeric => right justified)
				if ( !record.isEditable() ) {
                    return SECTION_RENDERER;
				}
				else if ( value == null ) {
                    return super.getCellRenderer( row, column );
				}
				else {
					final TableCellRenderer renderer = getDefaultRenderer( value.getClass() );
					if ( renderer instanceof DefaultTableCellRenderer ) {
						final DefaultTableCellRenderer defaultRenderer = (DefaultTableCellRenderer)renderer;
						final int modelColumn = convertColumnIndexToModel( column );
						// highlight the cell if the column corresponds to the value and it has unpublished changes
						defaultRenderer.setForeground( modelColumn == PROPERTY_TABLE_VALUE_COLUMN && record.hasChanges() ? Color.BLUE : Color.BLACK );
					}
					return renderer;
				}
            }


			private TableCellRenderer makeSectionRenderer() {
				final DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
				renderer.setBackground( Color.GRAY );
				renderer.setForeground( Color.WHITE );
				return renderer;
			}
        };
        
        //Set the table to allow one-click edit
        ((DefaultCellEditor) propertyTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
        propertyTable.setDefaultRenderer(Double.class, new DefaultTableCellRenderer() {
        	{
        		setHorizontalAlignment(JLabel.RIGHT);
        	}
        	
            public void setValue(Object value) {                
                setText((value == null) ? "" : String.format(Locale.ROOT, "%10.7g", value));
            }
        });
        
        //Resize the last column
        propertyTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN);
        //Allow single selection only
		propertyTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );

        //Set the model to the table
        propertyTable.setModel( PROPERTY_TABLE_MODEL );

        //Configure the text field to filter the table
        final JTextField filterTextField = new JTextField();
		filterTextField.setMaximumSize( new Dimension( 32000, filterTextField.getPreferredSize().height ) );
        filterTextField.putClientProperty( "JTextField.variant", "search" );
        filterTextField.putClientProperty( "JTextField.Search.Prompt", "Property Filter" );
		PROPERTY_TABLE_MODEL.setInputFilterComponent( filterTextField );
        mainContainer.add( filterTextField, BorderLayout.NORTH );

        //Add the scrollpane to the table with a vertical scrollbar
        final JScrollPane scrollPane = new JScrollPane( propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        mainContainer.add( scrollPane );

        //Add the buttons to the bottom of the dialog
        final Box controlPanel = new Box( BoxLayout.X_AXIS );
		controlPanel.add( revertButton );
		controlPanel.add( Box.createHorizontalGlue() );
        controlPanel.add( publishButton );
        controlPanel.add( okayButton );
        mainContainer.add( controlPanel );

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

		// initialize the value and status from the underlying property
		revert();
	}


	/** name of the property */
	public String getName() {
		return PROPERTY.getName();
	}


	/** Get the path to this property */
	public String getPath() {
		return PROPERTY.getPath();
	}


	/**  Get the label for display. */
	public String getDisplayLabel() {
		return isEditable() ? getName() : getPath();
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
			_value = value;

			// get the property's current value
			final Object propertyValue = PROPERTY.getValue();

			// if the value is really different from the property's current value then mark it as having changes
			// if the value is null then look for strict equality otherwise compare using equals
			if ( ( value == null && value != propertyValue ) || ( value != null && !value.equals( propertyValue ) )  ) {
				_hasChanges = true;
			}
			else {
				_hasChanges = false;
			}
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


	/** revert to the property value if this record is editable and has unpublished changes */
	public void revertIfNeeded() {
		if ( isEditable() && hasChanges() ) {
			revert();
		}
	}


	/** revert back to the current value of the underlying property */
	public void revert() {
		// the value is only meaningful for primitive properties (only thing we want to display)
		_value = PROPERTY.isPrimitive() ? PROPERTY.getValue() : null;
		_hasChanges = false;
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
		records.add( new PropertyRecord( propertyTree ) );		// add the container itself

		// add all the primitive properties
		final List<EditablePrimitiveProperty> properties = propertyTree.getChildPrimitiveProperties();
		for ( final EditablePrimitiveProperty property : properties ) {
			records.add( new PropertyRecord( property ) );
		}

		// navigate down through each container and append their sub trees
		final List<EditablePropertyContainer> containers = propertyTree.getChildPropertyContainers();
		for ( final EditablePropertyContainer container : containers ) {
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
		final Class<?>[] editablePropertyTypes = { Double.class, Double.TYPE, Float.class, Float.TYPE, Integer.class, Integer.TYPE, Short.class, Short.TYPE, Long.class, Long.TYPE, Boolean.class, Boolean.TYPE, String.class };
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
		// first check to see if there is a Units annotation (ideal when known at compile time) on the accessor method and use it otherwise fallback to fetching by unit property methods
		final Method readMethod = PROPERTY_DESCRIPTOR.getReadMethod();
		final Units units = readMethod != null ? readMethod.getAnnotation( Units.class ) : null;
		if ( units != null ) {
			return units.value();
		}
		else {		// unit property methods allow for dynamic units (i.e. units not known at runtime)
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
		final Method getter = descriptor.getReadMethod();

		// include only properties if the getter exists and is not deprecated and not marked hidden
		if ( getter != null && getter.getAnnotation( Deprecated.class ) == null && getter.getAnnotation( NoEdit.class ) == null ) {
			final Class<?> propertyType = descriptor.getPropertyType();

			if ( EDITABLE_PROPERTY_TYPES.contains( propertyType ) ) {
				// if the property is an editable primitive with both a getter and setter then return the primitive property instance otherwise null
				final Method setter = descriptor.getWriteMethod();
				// include only properties if the setter exists and is not deprecated (getter was already filtered in an enclosing block) and not marked hidden
				if ( setter != null && setter.getAnnotation( Deprecated.class ) == null && setter.getAnnotation( NoEdit.class ) == null ) {
					_childPrimitiveProperties.add( new EditablePrimitiveProperty( PATH, CHILD_TARGET, descriptor ) );
				}
				return;		// reached end of branch so we are done
			}
			else if ( propertyType == null ) {
				return;
			}
			else if ( propertyType.isArray() ) {
				// property is an array
				//			System.out.println( "Property type is array for target: " + CHILD_TARGET + " with descriptor: " + descriptor.getName() );
				return;
			}
			else {
				Object target = generateChildTarget( CHILD_TARGET, descriptor );
				
				if ( propertyType.equals(CovarianceMatrix.class) )
				{
					target =  new TwissCovarianceMatrixBridge((CovarianceMatrix)target);
				}
				
				// property is a plain container
				if ( !ANCESTORS.contains( target ) ) {	// only propagate down the branch if the targets are unique (avoid cycles)
					final Set<Object> ancestors = new HashSet<Object>( ANCESTORS );
					ancestors.add( target );
					final EditablePropertyContainer container = new EditablePropertyContainer( PATH, CHILD_TARGET, descriptor, target, ancestors );
					if ( container.getChildCount() > 0 ) {	// only care about containers that lead to editable properties
						_childPropertyContainers.add( container );
					}
				}
			
				return;
			}
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

class TwissCovarianceMatrixBridge {
	Twiss[] twiss;
	PhaseVector mean;
	CovarianceMatrix m;
	
	public TwissCovarianceMatrixBridge(CovarianceMatrix m) {
		twiss = m.computeTwiss();
		mean = m.getMean();
		this.m = m;
	}
	
	protected void update()
	{
		m.setMatrix( CovarianceMatrix.buildCovariance(twiss[0], twiss[1], twiss[2], mean).getArrayCopy() );
	}
	
	public double getAlphaX() {
		return twiss[0].getAlpha();
	}
	
	public double getAlphaY() {
		return twiss[1].getAlpha();
	}
	
	public double getAlphaZ() {
		return twiss[2].getAlpha();
	}
	
	public void setAlphaX(double alpha) {
		twiss[0].setTwiss(alpha, twiss[0].getBeta(), twiss[0].getEmittance());
		update();
	}
	
	public void setAlphaY(double alpha) {
		twiss[1].setTwiss(alpha, twiss[1].getBeta(), twiss[1].getEmittance());
		update();
	}

	public void setAlphaZ(double alpha) {
		twiss[2].setTwiss(alpha, twiss[2].getBeta(), twiss[2].getEmittance());
		update();
	}
	
	public double getBetaX() {
		return twiss[0].getBeta();
	}
	
	public double getBetaY() {
		return twiss[1].getBeta();
	}
	
	public double getBetaZ() {
		return twiss[2].getBeta();
	}
	
	public void setBetaX(double beta) {
		twiss[0].setTwiss(twiss[0].getAlpha(), beta, twiss[0].getEmittance());
		update();
	}
	
	public void setBetaY(double beta) {
		twiss[1].setTwiss(twiss[1].getAlpha(), beta, twiss[1].getEmittance());
		update();
	}

	public void setBetaZ(double beta) {
		twiss[2].setTwiss(twiss[2].getAlpha(), beta, twiss[2].getEmittance());
		update();
	}
	
	public double getEmitX() {
		return twiss[0].getEmittance();
	}
	
	public double getEmitY() {
		return twiss[1].getEmittance();
	}
	
	public double getEmitZ() {
		return twiss[2].getEmittance();
	}
	
	public void setEmitX(double emit) {
		twiss[0].setTwiss(twiss[0].getAlpha(), twiss[0].getBeta(), emit);
		update();
	}
	
	public void setEmitY(double emit) {
		twiss[1].setTwiss(twiss[1].getAlpha(), twiss[1].getBeta(), emit);
		update();
	}

	public void setEmitZ(double emit) {
		twiss[2].setTwiss(twiss[2].getAlpha(), twiss[2].getBeta(), emit);
		update();
	}	
}
