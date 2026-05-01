//
//  ViewInspectorTableModel.java
//  xal
//
//  Created by Thomas Pelaia on 7/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import java.awt.Window;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;

import xal.tools.apputils.ApplicationSupport;


/** Inspector table model */
class ViewInspectorTableModel extends AbstractTableModel implements PropertyTableModel {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	final static public int NAME_COLUMN = 0;
	final static public int VALUE_COLUMN = 1;
	
	final protected PropertyDescriptor[] PROPERTY_DESCRIPTORS;
	final protected BeanNode<?> BEAN_NODE;
	final protected PropertyValueEditorManager PROPERTY_VALUE_EDITOR_MANAGER;
	
	
	/** Constructor */
	public ViewInspectorTableModel( final BeanNode<?> node, final PropertyValueEditorManager propertyEditorManager ) {
		BEAN_NODE = node;
		PROPERTY_VALUE_EDITOR_MANAGER = propertyEditorManager;
		
		if ( node != null ) {
			final BeanInfo beanInfo = node.getBeanObjectBeanInfo();
			final PropertyDescriptor[] descriptors = beanInfo != null ? beanInfo.getPropertyDescriptors() : new PropertyDescriptor[0];
			final List<PropertyDescriptor> editableDescriptors = new ArrayList<PropertyDescriptor>( descriptors.length );
			for ( final PropertyDescriptor descriptor : descriptors ) {
				if ( isPropertyEditable( descriptor ) && isPropertyReadable( descriptor ) ) {
					editableDescriptors.add( descriptor );
				}
			}
			PROPERTY_DESCRIPTORS = new PropertyDescriptor[editableDescriptors.size()];
			editableDescriptors.toArray( PROPERTY_DESCRIPTORS );
		}
		else {
			PROPERTY_DESCRIPTORS = new PropertyDescriptor[0];
		}
	}
	
	
	/** get the property descriptor for the specified row */
	public PropertyDescriptor getPropertyDescriptor( final int row ) {
		return PROPERTY_DESCRIPTORS[row];
	}
	
	
	/** get the property descriptor for the specified row */
	public Class<?> getPropertyClass( final int row ) {
		return getPropertyDescriptor( row ).getPropertyType();
	}
	
	
	/** determine if the property descriptor is editable */
	protected boolean isPropertyEditable( final PropertyDescriptor descriptor ) {
		if ( descriptor.getWriteMethod() != null ) {
			//System.out.println( descriptor.getPropertyType() );
			return PROPERTY_VALUE_EDITOR_MANAGER.hasEditor( descriptor.getPropertyType() );
		}
		else {
			return false;
		}		
	}
	
	
	/** determine if the property descriptor is readable */
	protected boolean isPropertyReadable( final PropertyDescriptor descriptor ) {
		if ( descriptor.getReadMethod() != null ) {
			return PROPERTY_VALUE_EDITOR_MANAGER.hasEditor( descriptor.getPropertyType() );
		}
		else {
			return false;
		}		
	}
	
	
	
	/**
		* Get the name of the specified column.
	 * @param column the index of the column for which to get the name.
	 * @return the name of the specified column
	 */
	public String getColumnName( final int column ) {
		switch ( column ) {
			case NAME_COLUMN:
				return "Parameter";
			case VALUE_COLUMN:
				return "Value";
			default:
				return "?";
		}
	}
	
	
	/**
		* Get the data class for the specified column.
	 */
	public Class<?> getColumnClass( final int column ) {
		switch( column ) {
			default:
				return String.class;
		}
	}
	
	
	/**
		* Determine if the specified cell is editable.
	 */
	public boolean isCellEditable( final int row, final int column ) {
		switch( column ) {
			case VALUE_COLUMN:
				final PropertyDescriptor propertyDescriptor = PROPERTY_DESCRIPTORS[row];
				return isPropertyEditable( propertyDescriptor );
			default:
				return false;
		}
	}
	
	
	/**
		* Get the number of rows to display.
	 * @return the number of rows to display.
	 */
	public int getRowCount() {
		return PROPERTY_DESCRIPTORS.length;
	}
	
	
	/**
		* Get the number of columns to display.
	 * @return the number of columns to display.
	 */
	public int getColumnCount() {
		return 2;
	}
	
	
	/**
		* Get the value for the specified cell.
	 * @param row the row of the cell to update.
	 * @param column the column of the cell to update.
	 * @return the value to display in the specified cell.
	 */
	public Object getValueAt( final int row, final int column ) {
		switch ( column ) {
			case NAME_COLUMN:
				return PROPERTY_DESCRIPTORS[row].getName();
			case VALUE_COLUMN:
				try {
					final PropertyDescriptor propertyDescriptor = PROPERTY_DESCRIPTORS[row];
					return BEAN_NODE.getPropertyValue( propertyDescriptor );
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					return "None";
				}
			default:
				return "?";
		}
	}
	
	
	/** set the cell value */
	public void setValueAt( final Object value, final int row, final int column ) {
		switch ( column ) {
			case VALUE_COLUMN:
				try {
					final PropertyDescriptor propertyDescriptor = PROPERTY_DESCRIPTORS[row];
					BEAN_NODE.setPropertyValue( propertyDescriptor, value );
                    break;
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					ApplicationSupport.displayWarning( "Error Setting Value", "Property Setting Exception:", exception );
					return;
				}
			default:
				return;
		}
	}
}
