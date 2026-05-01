//
//  PropertyValueTextFieldEditor.java
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


/** property value editor */
abstract public class PropertyValueTextEditor<T> extends PropertyValueEditor<T> {
	/** instantiate a component */
	public Component getRenderingComponentInstance() {
		return new JLabel();
	}
	
	
	/** instantiate a component */
	public Component getEditorComponentInstance() {
		return new JTextField();
	}
	
	
	/** get the cell editor value */
	abstract public T getEditorValue( final BricksContext context );
	
	
	/** set the editor value */
	public void setEditorValue( final Object value ) {
		((JTextField)getEditorComponent()).setText( value != null ? value.toString() : "" );
	}
	
	
	/** set the rendering value */
	public void setRenderingValue( final Object value ) {
		((JLabel)getRenderingComponent()).setText( value != null ? value.toString() : "" );
	}
}
