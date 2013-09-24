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
    final private KeyValueFilteredTableModel<ProbeProperty> PROPERTY_TABLE_MODEL;

    /** List of properties that appear in the properties table */
    private List<ProbeProperty> propertyList = new ArrayList<ProbeProperty>();
    
    /** Map that contains an object keyed by group name. The Object instance is where the properties and methods. */
    private Map<String, Object> propertyClasses = new HashMap<>();

    /** Used to look for methods given a method name key */
    final private KeyValueAdaptor KEY_VALUE_ADAPTOR;
    
    /** Probe that is being edited */
    final private Probe PROBE;

	
    /* Constructor that takes a window parent
     * and a probe to fetch properties from
     */
    public SimpleProbeEditor( final Frame owner, final Probe probe ) {
        super( owner, "Probe Editor", true );	//Set JDialog's owner, title, and modality
        
        PROBE = probe;					// Set the probe to edit
		KEY_VALUE_ADAPTOR = new KeyValueAdaptor();
		PROPERTY_TABLE_MODEL = new KeyValueFilteredTableModel<ProbeProperty>( new ArrayList<ProbeProperty>(), "group", "property", "value");

		final EditablePropertyContainer probeContainer = EditableProperty.getInstanceWithRoot( "Probe", probe );
		System.out.println( probeContainer );

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

	
    /* setTableProperties()
     *
     * Sets the table data with the properties found through introspection
     *
     * Takes each property from the propertyClasses HashMap and adds
     * them to the list of properties.
     */
    private void setTableProperties() {
        
        //Cycle through each element in the HashMap
        for(String key : propertyClasses.keySet()) {
            //The instance of the object that will have its properties taken
            Object instance = propertyClasses.get(key);
            //Get the BeanInfo from the instance's class
			final BeanInfo beanInfo = null;
            //Get each property descriptor from the BeanInfo
            PropertyDescriptor[] descriptors = getPropertyDescriptors(beanInfo);
            
            //Cycle through each property descriptor found in the class
            for(int propIndex = 0; propIndex < descriptors.length; propIndex++) {
                
                //The property's write method for setting data
                Method write = descriptors[propIndex].getWriteMethod();
                //The property's read method for retreiving data 
                Method read = descriptors[propIndex].getReadMethod();
                
                //If there is not a getter AND setter for each property, we can not edit the property
                if(write != null && read != null) {
                    
                    //Gets the value of the property from the instance's read method
                    Object result = getPropertyValue( read, instance);
                    
                    //Filter out classes we don't want to edit
                    if(isEditableClass(result.getClass())) {
                        //Add the property as a ProbeProperty to the list of editable properties
                        propertyList.add(new ProbeProperty(key, descriptors[propIndex].getName(), result, result.getClass()));
                    }//if(isEditableClass())
                }//if(write && read)
            }//for(descriptors)
            
        }//for(HashMap keys)
        
        //Update the properties table
        refreshView();
    }
    
    
    /* getPropertyValue(Method, Object)
     *
     * Gets the value of a read method by invoking that method on an object instance
     *
     */
    public Object getPropertyValue(Method method, Object object) {
        
        //Result from invoking the read method
        Object result = null;
        
        try {
            //Try to invoke the read method and get its value
            result = method.invoke( object );
        } catch (IllegalAccessException iae) {
            System.err.println(iae.getMessage());
        }
        catch (InvocationTargetException ite) {
            System.err.println(ite.getMessage());
        }
        
        //TODO: handle null
        //Return the result
        return result == null ? "null" : result;
    }

	
    /* getPropertyDescriptor(BeanInfo)
     *
     * Gets the PropertyDescriptors from a BeanInfo
     *
     */
    public PropertyDescriptor[] getPropertyDescriptors( BeanInfo bean ) {
        //If the bean is not null, return the descriptors
		return bean != null ? bean.getPropertyDescriptors() : new PropertyDescriptor[0];
	}
	
    
    /* initializeComponents()
     *
     * Initialize the components of the probe editor
     *
     */
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
				//Save the properties
				saveProbeProperties();
				//Mark the properties as unchanged/saved
				setPropertiesAsUnchanged();
				//Re-enable the button
				applyButton.setEnabled( false );
			}
		});
		
        //Add the close button to the button panel
        controlPanel.add( closeButton );
        //Add the apply button to the button panel
        controlPanel.add( applyButton );
        
        //Text field that filters the properties
        final JTextField filterTextField = new JTextField();
        
        //Set the text field properts to search field
        filterTextField.putClientProperty( "JTextField.variant", "search" );
        filterTextField.putClientProperty( "JTextField.Search.Prompt", "Property Filter" );
        
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
        //Match the property's keys with their method
		PROPERTY_TABLE_MODEL.setMatchingKeyPaths( "group", "property", "value");
        //Set the table filter component to the text field
		PROPERTY_TABLE_MODEL.setInputFilterComponent( filterTextField );
        //Set the editable column to the "value" column
        PROPERTY_TABLE_MODEL.setColumnEditable( "value", true );
        
        //Set the model to the table
        propertyTable.setModel( PROPERTY_TABLE_MODEL );
        
        //Update the table contents
        refreshView();
        
        //Add the scrollpane to the table with a vertical scrollbar
        final JScrollPane scrollPane = new JScrollPane( propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED );
        
        //Add the text field to the top of the dialog
        mainContainer.add( filterTextField, BorderLayout.NORTH );
        //Add the table to the center of the dialog
        mainContainer.add( scrollPane, BorderLayout.CENTER );
        //Add the buttons to the bottom of the dialog
        mainContainer.add( controlPanel, BorderLayout.SOUTH );
        //Add everything to the dialog
        add( mainContainer );
    }

	
    /** Set the values of the table to the property list */
    private void refreshView() {
        //Set the records as the properties from the property list
        PROPERTY_TABLE_MODEL.setRecords( propertyList );
    }

	
    /** Set the properties of the probe that have been changed */
    private void saveProbeProperties() {
        //Go through each value in the properties HashMap
        for(String key : propertyClasses.keySet()) {
            
            //The instance of the object from the class
            Object instance = propertyClasses.get(key);
            //Get the BeanInfo from the class in the HashMap
			final BeanInfo beanInfo = null;
//            BeanInfo beanInfo = getBeanObjectBeanInfo(instance.getClass());
            //Get the PropertyDescriptors from the class
            PropertyDescriptor[] descriptors = getPropertyDescriptors(beanInfo);
            
            //Go through each descriptor
            for(int propIndex = 0; propIndex < descriptors.length; propIndex++) {
                
                //Write method of the descriptor
                Method write = descriptors[propIndex].getWriteMethod();
                //Read method of the descriptor
                Method read = descriptors[propIndex].getReadMethod();
                
                //Do nothing if there is not both a read and write method
                if(write != null && read != null) {
                    
                    //Find the right property in the list based on name
                    for(ProbeProperty pp : propertyList)
                    {
                        if(pp.hasChanged() && pp.getProperty().equals(descriptors[propIndex].getName())) {
                            //Try to invoke the write method with the changed property
                            try {
                                //Call the invoke method with the instance and value
                                System.out.println("Set property " + pp.getProperty());
                                write.invoke( instance, pp.getValue());
                            } catch (Exception e) {
                                //Display an error saying the property could not be set
                                System.out.println("Could not set property '" + pp.getProperty() + "' with value " + pp.getValue() + " of type " + pp.getValueType());
                                System.err.println(e.getMessage());
                            }// try/catch
                        }// if(correct property)
                    }// for(each ProbeProperty)
                }// if(write && read != null)
            }// for(descriptors)
        }// for(each HashMap key)
        //Update the table contents
        refreshView();
    }

	
    /** Determine if the property's class is editable or not based on the EDITABLE_CLASSES attribute */
    private boolean isEditableClass(Class<?> propertyClass) {
//        
//        //Look through each class in the EDITABLE_CLASSES array
//        for(Class<?> c : EDITABLE_CLASSES) {
//            if(propertyClass == c)
//                return true;
//        }
//        
        return false;
    }
	
    
    /* setPropertiesAsUnchanged()
     *
     * Sets property to be marked as unchanged after applying the changes
     *
     */
    private void setPropertiesAsUnchanged() {
        for(ProbeProperty pp : propertyList) {
            if(pp.hasChanged()) pp.setHasChanged(false);
        }
    }


	
    /** ProbeProperty record that gets dislpayed in the property table */
    private class ProbeProperty {
        //Class type of the property
        private Class<?> _type;
        //Group name, and property name of the property
        private String _group, _property;
        //Actual value of the property
        private Object _value;
        //If the property has been changed or not
        private boolean _hasChanged = false;
        
        
        //ProbeProperty Constructor that takes a group name, property name, value, and class type
        public ProbeProperty(String group, String property, Object value, Class<?> type)
        {
            //Initialize the attributes
            _type = type;
            _property = property;
            _value = value;
            _group = group;
        }
        
        /* getGroup()
         *
         * return the group name formatted with html to be bold
         *
         */
        public String getGroup() {
            return "<html><b>" + _group + "</b></html>";
        }
        
        /* getType()
         *
         * return the class type of the property
         *
         */
        public Class<?> getType() {
            return _type;
        }
        
        /* getValueType()
         *
         * return value type of the class
         *
         */
        public String getValueType() {
            return _type.getSimpleName();
        }
        
        /* getProperty()
         *
         * return the property name
         *
         */
        public String getProperty() {
            return _property;
        }
        
        /* getValue()
         *
         * return the value of the property
         *
         */
        public Object getValue() {
            return _value;
        }
        
        /* hasChanged()
         *
         * return if the value has changed
         *
         */
        public boolean hasChanged() {
            return _hasChanged;
        }
        
        /* setChanged()
         *
         * return if the value has changed
         *
         */
        public void setHasChanged(boolean changed) {
            _hasChanged = changed;
        }
        
        /* setValue(Boolean)
         *
         * Set the value of the value
         *
         * If the value is type Boolean, this method will be called and
         * the value would be set appropriately
         *
         */
        public void setValue(Boolean value) {
//            if( !applyButton.isEnabled() ) applyButton.setEnabled(true);
            _hasChanged = true;
            _value = value;
        }
        
        /* setValue(String)
         *
         * Set the value of the property and parse
         * the value appropriately
         *
         */
        public void setValue(String value) {
            try {
                if( _type == Double.class ){
                    _value = Double.parseDouble(value);
                }
                else if(_type == Float.class) {
                    _value = Float.parseFloat(value);
                }
                else if(_type == Integer.class) {
                    _value = Integer.parseInt(value);
                }
                else if(_type == Boolean.class) {
                    _value = Boolean.parseBoolean(value);
                }
                else if(_type == Long.class) {
                    _value = Long.parseLong(value);
                }
                else if(_type == Short.class) {
                    _value = Short.parseShort(value);
                }
                else if(_type == Byte.class) {
                    _value = Byte.parseByte(value);
                }
                else {
                    _value = value;
                }
//                if(!applyButton.isEnabled()) applyButton.setEnabled(true);
                _hasChanged = true;
            } catch (Exception e) {
                System.err.println("Invalid property value " + value + " for " + getProperty());
            }
        }
    }
}



/** base class for a editable property */
abstract class EditableProperty {
    /** array of classes for which the property can be edited directly */
    final static protected Set<Class<?>> EDITABLE_PROPERTY_TYPES = new HashSet<>();

	/** property name */
	final private String NAME;

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
	protected EditableProperty( final String name, final Object target, final PropertyDescriptor descriptor ) {
		NAME = name;
		TARGET = target;
		PROPERTY_DESCRIPTOR = descriptor;
	}


	/** Constructor */
	protected EditableProperty( final Object target, final PropertyDescriptor descriptor ) {
		this( descriptor.getName(), target, descriptor );
	}


	/** Get an instance starting at the root object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object root ) {
		return EditablePropertyContainer.getInstanceWithRoot( name, root );
	}


	/** name of the property */
	public String getName() {
		return NAME;
	}


	/** Get the property type */
	public Class<?> getPropertyType() {
		return PROPERTY_DESCRIPTOR != null ? PROPERTY_DESCRIPTOR.getPropertyType() : null;
	}


	/** Get the value for this property */
	public Object getValue() {
		if ( TARGET != null && PROPERTY_DESCRIPTOR != null ) {
			final Method getter = PROPERTY_DESCRIPTOR.getReadMethod();
			if ( getter.isAccessible() ) {
				try {
					return getter.invoke( TARGET );
				}
				catch( Exception exception ) {
					return null;
				}
			}
			else {
				return null;
			}
		}
		else {
			return null;
		}
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
		return getName();
	}
}



/** editable property representing a primitive that is directly editable */
class EditablePrimitiveProperty extends EditableProperty {
	/** Constructor */
	protected EditablePrimitiveProperty( final Object target, final PropertyDescriptor descriptor ) {
		super( target, descriptor );
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
			if ( setter.isAccessible() ) {
				try {
					setter.invoke( TARGET, value );
				}
				catch( Exception exception ) {
					throw new RuntimeException( "Cannot set value " + value + " on target: " + TARGET + " with descriptor: " + PROPERTY_DESCRIPTOR.getName(), exception );
				}
			}
			else {
				throw new RuntimeException( "Cannot set value " + value + " on target: " + TARGET + " with descriptor: " + PROPERTY_DESCRIPTOR.getName() + " because the set method is not accessible." );
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
}



/** base class for a container of editable properties */
class EditablePropertyContainer extends EditableProperty {
	/** list of child properties */
	final List<EditableProperty> CHILD_PROPERTIES;

	/** target for child properties */
	final protected Object CHILD_TARGET;


	/** Primary Constructor */
	protected EditablePropertyContainer( final String name, final Object target, final PropertyDescriptor descriptor, final Object childTarget ) {
		super( name, target, descriptor );

		CHILD_PROPERTIES = new ArrayList<EditableProperty>();

		CHILD_TARGET = childTarget;
	}


	/** Constructor */
	protected EditablePropertyContainer( final Object target, final PropertyDescriptor descriptor, final Object childTarget ) {
		this( descriptor.getName(), target, descriptor, childTarget );
	}


	/** Constructor */
	protected EditablePropertyContainer( final Object target, final PropertyDescriptor descriptor ) {
		this( target, descriptor, generateChildTarget( target, descriptor ) );
	}


	/** Create an instance witht the specified root Object */
	static public EditablePropertyContainer getInstanceWithRoot( final String name, final Object rootObject ) {
		final EditablePropertyContainer container = new EditablePropertyContainer( name, null, null, rootObject );
		container.generateChildPropertiesWithAncestor( rootObject );
		return container;
	}


	/** Generat the child target from the target and descriptor */
	static private Object generateChildTarget( final Object target, final PropertyDescriptor descriptor ) {
		try {
			final Method readMethod = descriptor.getReadMethod();
			return readMethod.invoke( target );
		}
		catch( Exception exception ) {
//			System.err.println( "Exception generating child target for target: " + target + " and descriptor: " + descriptor.getName() );
//			System.err.println( "Exception: " + exception );
//			exception.printStackTrace();
			return null;
		}
	}


	/** determine whether this container has any child properties */
	public boolean isEmpty() {
		return CHILD_PROPERTIES.size() == 0;
	}


	/** get the number of child properties */
	public int getChildCount() {
		return CHILD_PROPERTIES.size();
	}

	
	/** determine whether the property is a container */
	public boolean isContainer() {
		return true;
	}


	/** determine whether the property is a primitive */
	public boolean isPrimitive() {
		return false;
	}


	/** Get the child properties */
	public List<EditableProperty> getChildProperties() {
		return CHILD_PROPERTIES;
	}


	/** Generate the child properties this container's child target */
	public void generateChildPropertiesWithAncestor( final Object ancestor ) {
		final Set<Object> rootAncestor = new HashSet<Object>();
		generateChildPropertiesWithAncestors( rootAncestor );
	}


	/** Generate the child properties this container's child target */
	protected void generateChildPropertiesWithAncestors( final Set<Object> ancestors ) {
		final PropertyDescriptor[] descriptors = getPropertyDescriptors( CHILD_TARGET );
		if ( descriptors != null ) {
			for ( final PropertyDescriptor descriptor : descriptors ) {
				if ( descriptor.getPropertyType() != Class.class ) {
					generateChildPropertyForDescriptorAndAncestors( descriptor, ancestors );
				}
			}
		}
	}


	/** Generate the child properties starting at the specified descriptor for this container's child target */
	protected void generateChildPropertyForDescriptorAndAncestors( final PropertyDescriptor descriptor, final Set<Object> ancestorsReference ) {
		final Set<Object> ancestors = new HashSet<Object>( ancestorsReference );	// make a copy so it is unique for each branch
		final Class<?> propertyType = descriptor.getPropertyType();

		if ( EDITABLE_PROPERTY_TYPES.contains( propertyType ) ) {
			// if the property is an editable primitive with both a getter and setter then return the primitive property instance otherwise null
			final Method getter = descriptor.getReadMethod();
			final Method setter = descriptor.getWriteMethod();
			if ( getter != null && setter != null ) {
				CHILD_PROPERTIES.add( new EditablePrimitiveProperty( CHILD_TARGET, descriptor ) );
			}
			return;		// reached end of branch so we are done
		}
		else if ( propertyType == null ) {
			return;
		}
		else if ( propertyType.isArray() ) {
			// property is an array
			return;
		}
		else {
			// property is a plain container
			if ( !ancestors.contains( CHILD_TARGET ) ) {	// only propagate down the branch if the targets are unique (avoid cycles)
				ancestors.add( CHILD_TARGET );
				final EditablePropertyContainer container = new EditablePropertyContainer( CHILD_TARGET, descriptor );
				container.generateChildPropertiesWithAncestors( ancestors );
				if ( container.getChildCount() > 0 ) {	// only care about nontrivial containers
					CHILD_PROPERTIES.add( container );
				}
			}
			return;
		}
	}


	/** Get the editable properties of the specified target */
	static public List<EditableProperty> getChildProperties( final Object target ) {
		return null;
	}


	/** Get the child property for the given descriptor */
	protected EditableProperty getChildProperty( final PropertyDescriptor descriptor ) {
		return null;
	}


	/** Get a string represenation of this property */
	public String toString() {
		final StringBuilder buffer = new StringBuilder();
		buffer.append( "\n" + getName() + ":\n" );
		for ( final EditableProperty property : CHILD_PROPERTIES ) {
			buffer.append( "\t" + property.toString() + "\n" );
		}
		buffer.append( "\n" );
		return buffer.toString();
	}
}


/** container for an editable property that is an array */
class EditableArrayProperty extends EditablePropertyContainer {
	/** Constructor */
	protected EditableArrayProperty( final Object target, final PropertyDescriptor descriptor ) {
		super( target, descriptor );
	}
}