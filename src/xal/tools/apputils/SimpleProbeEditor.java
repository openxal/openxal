/*
 * SimpleProbeEditor.java
 *
 * Created on June 17, 2013, 8:51 AM
 *
 * @author Patrick Scruggs
 */

package xal.tools.apputils;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.HashMap;
import java.util.ArrayList;
import java.awt.Frame;
import javax.swing.*;
import javax.swing.table.*;
import xal.model.probe.Probe;
import xal.model.IAlgorithm;
import xal.tools.bricks.*;
import xal.tools.swing.KeyValueFilteredTableModel;
import xal.tools.data.KeyValueAdaptor;
import java.beans.*;
import java.lang.reflect.*;

/**
 * SimpleProbeEditor
 */
public class SimpleProbeEditor extends JDialog {
    
    //Private serializable version ID
    private static final long serialVersionUID = 1L;
    

    //Buttons for applying changes and closing the editor
    private JButton applyButton, closeButton;
    //Table that contains property records
    private JTable propertyTable;
    //TextField that filters the properties
    private JTextField filterTextField;
    //Table model of ProbeProperty records
    private KeyValueFilteredTableModel<ProbeProperty> PROPERTY_TABLE_MODEL;
    //Panel that contains every component
    private JPanel searchPanel;
    //Panel that contains the apply and close button
    private JPanel applyButtonPanel;
    //ScrollPane attached to the properties table
    private JScrollPane scrollPane;
    
    //List of properties that appear in the properties table
    List<ProbeProperty> propertyList = new ArrayList<ProbeProperty>();
    
    //HashMap that contains an object instance with a group name as the key
    //The Object instance is where the properties and methods are pulled from
    //The key is the group name displayed next to the property
    HashMap<String, Object> propertyClasses = new HashMap<String, Object>();
    
    //List of property types that can be modified
    //Other types are filtered out
    Class<?>[] editableClasses = { Double.class, Integer.class, String.class, Boolean.class };
    
    //Used to look for methods given a method name key
    private KeyValueAdaptor keyValueAdaptor = new KeyValueAdaptor();
    
    
    //Probe that is being editted
    public Probe probe;
    
    //The probe's algorithm
    private IAlgorithm algorithm;
    
    /* Constructor that takes a window parent
     * and a probe to fetch properties from
     */
    public SimpleProbeEditor(Frame owner, Probe probe)
    {
        //Set JDialog's owner, title, and modality
        super(owner, "Probe Editor", true);
        
        //Set the probe and algorithm
        this.probe = probe;
        algorithm = probe.getAlgorithm();
        
        //Add the probe to the map so its properties will be fetched
        propertyClasses.put("Probe", probe);
        //Add the probe's algorithm to the map so its properties will be fetched
        propertyClasses.put("Algorithm", algorithm);
        
        
        /**
         *  Methods that do not have get/set convention can be added here
         *  as well as methods from probes that subclass Probe and have
         *  methods that Probe does not
         **/
        try {
            
            /* Try to get the phaseCoordinates() or getPhaseCoordinates() method from the probe given
             * phaseCoordinates() is found in ParticleProbe - this method will be added if SimpleProbeEditor is constructed
             * with a ParticleProbe object.
             */
            Object phaseCoords = keyValueAdaptor.valueForKey(probe, "phaseCoordinates");
            propertyClasses.put("Phase_Coordinates", phaseCoords);
            
        } catch(Exception e) {
            System.out.println(e.getMessage());
        }
        
        //Set the window size
        setSize(600, 600);
        //Set up each component in the editor
        initializeComponents();
        //Fit the components in the window
        pack();
        //Center the editor in relation to the frame that constructed the editor
        setLocationRelativeTo(owner);
        //Populate the properties table
        setTableProperties();
        //Make the window visible
        setVisible(true);
    }
    
    /* getProbe()
     *
     * Returns the probe associated with the editor
     *
     */
    public Probe getProbe() {
        return probe;
    }
    
    /* setTableProperties()
     *
     * Sets the table data with the properties found through introspection
     *
     * Takes each property from the propertyClasses HashMap and adds
     * them to the list of properties.
     */
    public void setTableProperties() {
        
        //Cycle through each element in the HashMap
        for(String key : propertyClasses.keySet()) {
            //The instance of the object that will have its properties taken
            Object instance = propertyClasses.get(key);
            //Get the BeanInfo from the instance's class
            BeanInfo beanInfo = getBeanObjectBeanInfo(instance.getClass());
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
    public void initializeComponents()
    {
        //Panel containing all elements
        searchPanel = new JPanel();
        //Set the layout of the panel to a BorderLayout
        searchPanel.setLayout(new BorderLayout());
        //Panel containing apply and close button with a 1 row, 2 column grid layout
        applyButtonPanel = new JPanel(new GridLayout(1, 2));
        //Apply button
        applyButton = new JButton( "Apply" );
        applyButton.setEnabled(false);
        //Close button
        closeButton = new JButton( "Close" );
        
        //Set the close button's action to close the dialog
        closeButton.addActionListener( new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        //Add the action listener as the ApplyButtonListener
        applyButton.addActionListener( new ApplyButtonListener() );
        //Add the close button to the button panel
        applyButtonPanel.add( closeButton );
        //Add the apply button to the button panel
        applyButtonPanel.add( applyButton );
        
        //Text field that filters the properties
        filterTextField = new JTextField();
        
        //Set the text field properts to search field
        filterTextField.putClientProperty("JTextField.variant", "search");
        filterTextField.putClientProperty("JTextField.Search.Prompt", "Property Filter");
        
        //Table containing the properties that can be modified
        propertyTable = new JTable(null, new Object[] {"Group", "Property", "Value" }) {
            
            //Serializable version ID
            private static final long serialVersionUID = 1L;
            
            //Get the cell editor for the table
            @Override
            public TableCellEditor getCellEditor(int row, int col) {
                //Value at [row, col] of the table
                Object value = getValueAt(row, col);
                
                //Set the appropriate editor for each value type
                if(value instanceof Boolean)
                    return getDefaultEditor(Boolean.class);
                else if(value instanceof Double)
                    return getDefaultEditor(Double.class);
                else if(value instanceof Integer)
                    return getDefaultEditor( Integer.class );
                
                //Default editor (String type)
                return super.getCellEditor(row, col);
            }
            
            //Get the cell renderer for the table to change how values are displayed
            @Override
            public TableCellRenderer getCellRenderer(int row, int col) {
                //Value at [row, col]
                Object value = getValueAt(row, col);
 
                //Set the renderer of each type
                //Boolean = checkbox display
                //Double/Int = right aligned display
                if(value instanceof Boolean)
                    return getDefaultRenderer( Boolean.class );
                else if(value instanceof Double)
                    return getDefaultRenderer( Double.class );
                else if(value instanceof Integer)
                    return getDefaultRenderer( Integer.class );
                
                //Default = left aligned string display
                return super.getCellRenderer(row, col);
            }
        };
        
        //Set the table to allow one-click edit
        ((DefaultCellEditor) propertyTable.getDefaultEditor(Object.class)).setClickCountToStart(1);
        
        //Resize the last column
        propertyTable.setAutoResizeMode( JTable.AUTO_RESIZE_LAST_COLUMN);
        //Allow single selection only
		propertyTable.setSelectionMode( ListSelectionModel.SINGLE_SELECTION );
        //Set the table's model to a KeyValueFiltered model with the property's group, name, and the value of the property
		PROPERTY_TABLE_MODEL = new KeyValueFilteredTableModel<ProbeProperty>( new ArrayList<ProbeProperty>(), "group", "property", "value");
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
        scrollPane = new JScrollPane(propertyTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        //Add the text field to the top of the dialog
        searchPanel.add(filterTextField, BorderLayout.NORTH);
        //Add the table to the center of the dialog
        searchPanel.add(scrollPane, BorderLayout.CENTER);
        //Add the buttons to the bottom of the dialog
        searchPanel.add(applyButtonPanel, BorderLayout.SOUTH);
        //Add everything to the dialog
        add(searchPanel);
    }
    
    /* refreshView()
     *
     * Set the values of the table to the property list
     *
     */
    private void refreshView() {
        //Set the records as the properties from the property list
        PROPERTY_TABLE_MODEL.setRecords( propertyList );
    }
    
    /* getBeanObjectBeanInfo(Class<?>)
     *
     * Get the BeanInfo from a class
     *
     */
	public BeanInfo getBeanObjectBeanInfo(Class<?> beanClass) {
		//Try to get the BeanInfo from the class given
        try {
			return Introspector.getBeanInfo( beanClass );
		}
        //Throw an exception if the BeanInfo could not be obtained
		catch( IntrospectionException exception ) {
			return null;
		}
	}
    
    /* saveProbeProperties()
     *
     * Set the properties of the probe that have been changed
     *
     */
    private void saveProbeProperties()
    {
        //Go through each value in the properties HashMap
        for(String key : propertyClasses.keySet()) {
            
            //The instance of the object from the class
            Object instance = propertyClasses.get(key);
            //Get the BeanInfo from the class in the HashMap
            BeanInfo beanInfo = getBeanObjectBeanInfo(instance.getClass());
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
    
    /* isEditableClass(Class<?>)
     *
     * Determine if the property's class is editable or not based on the editableClasses attribute
     *
     */
    private boolean isEditableClass(Class<?> propertyClass) {
        
        //Look through each class in the editableClasses array
        for(Class<?> c : editableClasses) {
            if(propertyClass == c)
                return true;
        }
        
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
    
    /* Class: ProbeProperty
     *
     * Description: ProbeProperty record that gets dislpayed in the property table
     *
     */
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
            if(!applyButton.isEnabled()) applyButton.setEnabled(true);
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
                if(!applyButton.isEnabled()) applyButton.setEnabled(true);
                _hasChanged = true;
            } catch (Exception e) {
                System.err.println("Invalid property value " + value + " for " + getProperty());
            }
        }
    }
    
    /* Class: ApplyButtonListener
     * Implements: AcitonListener
     * Description: Sets the Apply button's action
     *
     */
    private class ApplyButtonListener implements ActionListener {
        
        @Override
        public void actionPerformed(ActionEvent e) {
            //Save the properties
            saveProbeProperties();
            //Mark the properties as unchanged/saved
            setPropertiesAsUnchanged();
            //Re-enable the button
            applyButton.setEnabled(false);
        }
    }
}
