//
//  PropertyValueEditorManager.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.util.*;


/** manage the property value editor relationship to classes to render */
public class PropertyValueEditorManager {
	final protected Map<String, PropertyValueEditor<?>> EDITOR_TABLE;
	
	final protected PropertyValueEditor<?> DEFAULT_EDITOR;
	
	final static PropertyValueEditorManager DEFAULT_MANAGER;
	
	
	// static initializer
	static {
		DEFAULT_MANAGER = new PropertyValueEditorManager();
	}
	
	
	/** Constructor */
	public PropertyValueEditorManager() {
		EDITOR_TABLE = new HashMap<String, PropertyValueEditor<?>>();
		DEFAULT_EDITOR = PropertyValueEditorFactory.getSimpleRenderer();
		
		register( String.class, PropertyValueEditorFactory.getStringEditor() );
		register( Double.class, PropertyValueEditorFactory.getDoubleEditor() );
		register( Double.TYPE, PropertyValueEditorFactory.getDoubleEditor() );
		register( Integer.class, PropertyValueEditorFactory.getIntegerEditor() );
		register( Integer.TYPE, PropertyValueEditorFactory.getIntegerEditor() );
		register( Boolean.class, PropertyValueEditorFactory.getBooleanEditor() );
		register( Boolean.TYPE, PropertyValueEditorFactory.getBooleanEditor() );
		register( java.awt.Color.class, PropertyValueEditorFactory.getColorEditor() );
		register( java.awt.Font.class, PropertyValueEditorFactory.getFontEditor() );
		register( java.awt.Dimension.class, PropertyValueEditorFactory.getDimensionEditor() );
		register( java.awt.Rectangle.class, PropertyValueEditorFactory.getRectangleEditor() );
		register( java.awt.Insets.class, PropertyValueEditorFactory.getInsetsEditor() );
		register( javax.swing.Icon.class, PropertyValueEditorFactory.getIconEditor() );
		register( IconResource.class, PropertyValueEditorFactory.getIconEditor() );
	}
	
	
	/** Get the default manager */
	static public PropertyValueEditorManager getDefaultManager() {
		return DEFAULT_MANAGER;
	}
	
	
	/** register editors for classes */
	public void register( final Class<?> theClass, final PropertyValueEditor<?> editor ) {
		EDITOR_TABLE.put( theClass.toString(), editor );
	}
	
	
	/** determine if the there is an editor registered for the specified class */
	public boolean hasEditor( final Class<?> theClass ) {
		return EDITOR_TABLE.containsKey( theClass.toString() );
	}
	
	
	/** get the editor for a given class */
	public PropertyValueEditor<?> getEditor( final Class<?> theClass ) {
		final PropertyValueEditor<?> editor = EDITOR_TABLE.get( theClass.toString() );
		return editor != null ? editor : DEFAULT_EDITOR;
	}
	
	
	/** get the editor for a given class name */
	public PropertyValueEditor<?> getEditor( final String className ) {
		try {
			return getEditor( Class.forName( className ) );
		}
		catch( Exception exception ) {
			return null;
		}
	}
}
