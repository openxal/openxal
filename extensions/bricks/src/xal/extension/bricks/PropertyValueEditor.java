//
//  PropertyValueEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
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

import xal.tools.data.*;


/** property value editor */
abstract public class PropertyValueEditor<ValueType> {
	final protected Component EDITOR_COMPONENT;
	final protected Component RENDERING_COMPONENT;
	
	protected PropertyValueCellEditor _currentCellEditor;
	
	
	/** Constructor */
	public PropertyValueEditor() {
		EDITOR_COMPONENT = getEditorComponentInstance();
		RENDERING_COMPONENT = getRenderingComponentInstance();
	}
	
	
	/** write to a data adaptor */
	public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
		adaptor.setValue( "name", name );
	}
	
	
	/** write to a data adaptor */
	abstract public ValueType readValue( final DataAdaptor adaptor );
	
	
	/** Determine if the component supports editing */
	public boolean isEditable() {
		return EDITOR_COMPONENT != null;
	}
	
	
	/** get the component */
	public Component getEditorComponent() {
		return EDITOR_COMPONENT;
	}
	
	
	/** get the component */
	public Component getRenderingComponent() {
		return RENDERING_COMPONENT;
	}
	
	/** instantiate a component */
	public Component getRenderingComponentInstance() {
		return getEditorComponentInstance();
	}
	
	
	/** instantiate a component */
	abstract public Component getEditorComponentInstance();	
	
	
	/** get the cell editor value */
	abstract public ValueType getEditorValue( final BricksContext context );
	
	
	/** set the editor value */
	abstract public void setEditorValue( final Object value );
	
	
	/** set the rendering value */
	abstract public void setRenderingValue( final Object value );
	
	
	/** set the current cell editor */
	public void setCurrentCellEditor( final PropertyValueCellEditor cellEditor ) {
		_currentCellEditor = cellEditor;
	}
}
