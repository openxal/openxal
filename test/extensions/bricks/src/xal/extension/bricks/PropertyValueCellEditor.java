//
//  PropertyValueCellEditor.java
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
import javax.swing.event.*;


/** cell editor for property values */
public class PropertyValueCellEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** context in which bricks is run */
	final private BricksContext CONTEXT;
	
	final protected PropertyValueEditorManager PROPERTY_EDITOR_MANAGER;
	final protected PropertyTableModel TABLE_MODEL;
	protected PropertyValueEditor<?> _currentEditor;
	
	
	/** Constructor */
	public PropertyValueCellEditor( final BricksContext context, final JTable table, final PropertyValueEditorManager propertyEditorManager, final PropertyTableModel tableModel ) {
		CONTEXT = context;
		PROPERTY_EDITOR_MANAGER = propertyEditorManager;
		TABLE_MODEL = tableModel;
	}
	
	
	/** get the editor for the property corresponding to the specified table row */
	protected PropertyValueEditor<?> getEditor( final int row ) {
		final Class<?> propertyClass = TABLE_MODEL.getPropertyClass( row );
		PropertyValueEditor<?> editor;
		
		if ( propertyClass != null && PROPERTY_EDITOR_MANAGER.hasEditor( propertyClass ) ) {
			editor = PROPERTY_EDITOR_MANAGER.getEditor( propertyClass );
		}
		else {
			editor = PROPERTY_EDITOR_MANAGER.getEditor( String.class );
		}
		
		return editor;
	}
	
	
	/** get the component */
	public Component getTableCellEditorComponent( final JTable table, final Object value, final boolean isSelected, final int row, final int column ) {
		_currentEditor = getEditor( row );
		_currentEditor.setCurrentCellEditor( this );
		_currentEditor.setEditorValue( value );
		return _currentEditor.getEditorComponent();
	}
	
	
	/** get the cell editor value */
	public Object getCellEditorValue() {
		return _currentEditor.getEditorValue( CONTEXT );
	}
	
	
	/**
	 * Get the table component.
	 */
	final public Component getTableCellRendererComponent( final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column ) {
		final PropertyValueEditor<?> editor = getEditor( row );
		final JComponent component = (JComponent)editor.getRenderingComponent();
		editor.setRenderingValue( value );
		component.setOpaque( isSelected );
		return component;
	}
	
	
	/** Make this method public */
	public void fireEditingStopped() {
		super.fireEditingStopped();
	}
}
