//
//  ViewInspector.java
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
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.util.List;
import java.util.ArrayList;


/** Inspector for setting view properites */
public class ViewInspector extends Box {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** table of property settings */
	final protected JTable PARAMETER_TABLE;
	
	/** field for editing the node's tag */
	final protected JTextField TAG_FIELD;
	
	/** field for editing the node's custom class */
	final protected JTextField CUSTOM_CLASS_FIELD;
	
	/** check box for enabling/disabling custom classes */
	final protected JCheckBox CUSTOM_CLASS_ENABLE;
	
	
	/** property editor manager */
	final protected PropertyValueEditorManager PROPERTY_EDITOR_MANAGER;
	
	/** indicates whether this palette has ever been positioned */
	protected boolean _hasBeenPositioned;
	
	/** view node to inspect */
	protected BeanNode<?> _node;
	
	
	/** Constructor */
	public ViewInspector() {
		super( BoxLayout.Y_AXIS );

		setSize( 400, 600 );
		
		PARAMETER_TABLE = new JTable();
		PARAMETER_TABLE.setRowHeight( (int) ( 1.2 * PARAMETER_TABLE.getRowHeight() ) );
		
		PROPERTY_EDITOR_MANAGER = PropertyValueEditorManager.getDefaultManager();
		_hasBeenPositioned = false;
		
		TAG_FIELD = new JTextField();
		CUSTOM_CLASS_FIELD = new JTextField();
		CUSTOM_CLASS_ENABLE = new JCheckBox( "" );
		
		makeContent();
	}
	
	
	/**
	 * Inspect the specified view node
	 */
	public void inspect( final BricksContext context, final BeanNode<?> node ) {
		setViewNode( context, node );
	}
	
	
	/**
	 * Set the view node
	 */
	public void setViewNode( final BricksContext context, final BeanNode<?> node ) {
		_node = node;
		
		final ViewInspectorTableModel tableModel = new ViewInspectorTableModel( node, PROPERTY_EDITOR_MANAGER );
		PARAMETER_TABLE.setModel( tableModel );
		final PropertyValueCellEditor editor = new PropertyValueCellEditor( context, PARAMETER_TABLE, PROPERTY_EDITOR_MANAGER, tableModel );
		PARAMETER_TABLE.getColumnModel().getColumn( ViewInspectorTableModel.VALUE_COLUMN ).setCellEditor( editor );
		PARAMETER_TABLE.getColumnModel().getColumn( ViewInspectorTableModel.VALUE_COLUMN ).setCellRenderer( editor );
		
		if ( node != null ) {
			TAG_FIELD.setText( node.getTag() );
			TAG_FIELD.setEnabled( true );
			
			refreshCustomClassView();
		}
		else {
			TAG_FIELD.setText( "" );
			TAG_FIELD.setEnabled( false );
			CUSTOM_CLASS_FIELD.setText( "" );
			CUSTOM_CLASS_FIELD.setEnabled( false );
			CUSTOM_CLASS_ENABLE.setSelected( false );
		}
	}
	
	
	/** refresh the custom class field to reflect the custom class field if any */
	private void refreshCustomClassView() {
		final BeanNode<?> node = _node;
		
		final boolean hasCustomBeanClass = node.hasCustomBeanClass();
		CUSTOM_CLASS_FIELD.setText( hasCustomBeanClass? node.getCustomBeanClassName() : node.getClassName() );
		CUSTOM_CLASS_FIELD.setForeground( hasCustomBeanClass ? Color.BLACK : Color.GRAY );
		CUSTOM_CLASS_FIELD.setEnabled( hasCustomBeanClass );
		CUSTOM_CLASS_ENABLE.setSelected( hasCustomBeanClass  );		
	}
	
	
	/**
	 * Get the view node
	 */
	public BeanNode<?> getViewNode() {
		return _node;
	}
	
	
	/** Make the content for the window. */
	protected void makeContent() {
		this.add( makeSettingsView() );
		this.add( makeBeanPropertiesView() );
	}
	
	
	/** make the bean properties view */
	protected Component makeBeanPropertiesView() {
		final JComponent view = new JScrollPane( PARAMETER_TABLE );
		
		view.setBorder( BorderFactory.createTitledBorder( "Bean Parameters" ) );
		
		return view;
	}
	
	
	/** make the settings view */
	protected Component makeSettingsView() {
		final Box view = new Box( BoxLayout.Y_AXIS );
		
		view.add( makeTagView() );
		view.add( makeCustomClassView() );
		
		return view;
	}
	
	
	/** make the tag view */
	protected Component makeTagView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		
		view.setBorder( BorderFactory.createTitledBorder( "Tag" ) );
		
		TAG_FIELD.setMaximumSize( new Dimension( 10000, TAG_FIELD.getPreferredSize().height ) );
		
		TAG_FIELD.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _node != null ) {
					_node.setTag( TAG_FIELD.getText() );
				}
			}
		});
		
		view.add( TAG_FIELD );
		
		return view;
	}
	
	
	/** make the custom class view */
	protected Component makeCustomClassView() {
		final Box view = new Box( BoxLayout.X_AXIS );
		view.setBorder( BorderFactory.createTitledBorder( "Custom Class" ) );
		
		CUSTOM_CLASS_FIELD.setMaximumSize( new Dimension( 10000, TAG_FIELD.getPreferredSize().height ) );
		
		CUSTOM_CLASS_FIELD.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _node != null ) {
					_node.setCustomBeanClassName( CUSTOM_CLASS_FIELD.getText() );
				}
			}
		});
		
		CUSTOM_CLASS_ENABLE.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				if ( _node != null ) {
					final boolean shouldEnable = CUSTOM_CLASS_ENABLE.isSelected();
					_node.setCustomBeanClassName( shouldEnable ? _node.getClassName() : null );
					refreshCustomClassView();
				}
			}
		});
		
		view.add( CUSTOM_CLASS_ENABLE );
		view.add( CUSTOM_CLASS_FIELD );
		
		return view;
	}
}




