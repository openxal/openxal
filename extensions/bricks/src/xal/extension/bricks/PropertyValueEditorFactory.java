//
//  PropertyValueEditorFactory.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.net.URL;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;

import xal.tools.data.*;


/** Factory for creating property value editor instances */
public class PropertyValueEditorFactory {
	/** Get an editor of strings */
	static PropertyValueEditor<String> getStringEditor() {
		return new PropertyValueTextEditor<String>() {
			/** get the cell editor value */
			public String getEditorValue( final BricksContext context ) {
				return ((JTextField)getEditorComponent()).getText();
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "value", value.toString() );
			}
			
			
			/** read from a data adaptor */
			public String readValue( final DataAdaptor adaptor ) {
				return adaptor.stringValue( "value" );
			}
		};
	}
	
	
	/** Get an editor of strings */
	static PropertyValueEditor<Double> getDoubleEditor() {
		return new PropertyValueNumberEditor<Double>() {
			/** get the cell editor value */
			public Double getEditorValue( final BricksContext context ) {
				final String text = ((JTextField)getEditorComponent()).getText();
				return new Double( text );
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "value", (Double)value );
			}
			
			
			/** read from a data adaptor */
			public Double readValue( final DataAdaptor adaptor ) {
				return adaptor.doubleValue( "value" );
			}
		};
	}
	
	
	/** Get an editor of strings */
	static PropertyValueEditor<Integer> getIntegerEditor() {
		return new PropertyValueNumberEditor<Integer>() {
			/** get the cell editor value */
			public Integer getEditorValue( final BricksContext context ) {
				final String text = ((JTextField)getEditorComponent()).getText();
				return new Integer( text );
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "value", (Integer)value );
			}
			
			
			/** read from a data adaptor */
			public Integer readValue( final DataAdaptor adaptor ) {
				return adaptor.intValue( "value" );
			}
		};
	}
	
	
	/** Get an editor of strings */
	static PropertyValueEditor<String> getSimpleRenderer() {
		return new PropertyValueTextEditor<String>() {
			/** get the cell editor value */
			public String getEditorValue( final BricksContext context ) {
				return null;
			}
			
			
			/** instantiate a component */
			public Component getEditorComponentInstance() {
				return null;
			}
			
			
			/** read from a data adaptor */
			public String readValue( final DataAdaptor adaptor ) {
				return null;
			}
		};
	}
	
	
	/** Get an editor of boolean values */
	static PropertyValueEditor<Boolean> getBooleanEditor() {
		return new PropertyValueEditor<Boolean>() {
			/** instantiate a component */
			public Component getEditorComponentInstance() {
				final JCheckBox checkBox = new JCheckBox();
				checkBox.addActionListener( new ActionListener() {
					public void actionPerformed( final ActionEvent event ) {
						if ( _currentCellEditor != null )  _currentCellEditor.fireEditingStopped();
					}
				});
				return checkBox;
			}
			
			
			/** get the cell editor value */
			public Boolean getEditorValue( final BricksContext context ) {
				return new Boolean( ((JCheckBox)getEditorComponent()).isSelected() );
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				((JCheckBox)getEditorComponent()).setSelected( ((Boolean)value).booleanValue() );
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				((JCheckBox)getRenderingComponent()).setSelected( ((Boolean)value).booleanValue() );
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object value, final DataAdaptor adaptor ) {
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "value", (Boolean)value );
			}
			
			
			/** read from a data adaptor */
			public Boolean readValue( final DataAdaptor adaptor ) {
				return adaptor.booleanValue( "value" );
			}
		};
	}
	
	
	/** Get an editor of color values */
	static PropertyValueColorEditor getColorEditor() {
		return new PropertyValueColorEditor();
	}
	
	
	/** Get an editor of Font values */
	static PropertyValueEditor<Font> getFontEditor() {
		return new PropertyValueTextEditor<Font>() {
			/** get the cell editor value */
			public Font getEditorValue( final BricksContext context ) {
				final JTextField textField = (JTextField)getEditorComponent();
				final String[] valueStrings = textField.getText().split( "," );
				final String name = valueStrings[0].trim();
				final int style = Integer.parseInt( valueStrings[1].trim() );
				final int size = Integer.parseInt( valueStrings[2].trim() );
				return new Font( name, style, size );
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				final JTextField textField = (JTextField)getEditorComponent();
				if ( value != null ) {
					final Font font = (Font)value;
					textField.setText( font.getName() + ", " + font.getStyle() + ", " + font.getSize() );
				}
				else {
					textField.setText( "" );
				}
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				final JLabel textLabel = (JLabel)getRenderingComponent();
				if ( value != null ) {
					final Font font = (Font)value;
					textLabel.setText( "name:  " + font.getName() + ", style:  " + font.getStyle() + ", size:  " + font.getSize() );
				}
				else {
					textLabel.setText( "" );
				}
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
				final Font value = (Font)rawValue;
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "fontName", value.getName() );
				adaptor.setValue( "style", value.getStyle() );
				adaptor.setValue( "size", value.getSize() );
			}
			
			
			/** read from a data adaptor */
			public Font readValue( final DataAdaptor adaptor ) {
				final String fontName = adaptor.stringValue( "fontName" );
				final int style = adaptor.intValue( "style" );
				final int size = adaptor.intValue( "size" );
				return new Font( fontName, style, size );
			}
		};
	}
	
	
	/** Get an editor of Dimension values */
	static PropertyValueEditor<Dimension> getDimensionEditor() {
		return new PropertyValueTextEditor<Dimension>() {
			/** get the cell editor value */
			public Dimension getEditorValue( final BricksContext context ) {
				final JTextField textField = (JTextField)getEditorComponent();
				final String[] valueStrings = textField.getText().split( "," );
				final int width = Integer.parseInt( valueStrings[0].trim() );
				final int height = Integer.parseInt( valueStrings[1].trim() );
				return new Dimension( width, height );
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				final JTextField textField = (JTextField)getEditorComponent();
				if ( value != null ) {
					final Dimension dimension = (Dimension)value;
					textField.setText( dimension.width + ", " + dimension.height );
				}
				else {
					textField.setText( "" );
				}
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				final JLabel textLabel = (JLabel)getRenderingComponent();
				if ( value != null ) {
					final Dimension dimension = (Dimension)value;
					textLabel.setText( "width:  " + dimension.width + ", height:  " + dimension.height );
				}
				else {
					textLabel.setText( "" );
				}
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
				final Dimension value = (Dimension)rawValue;
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "width", value.width );
				adaptor.setValue( "height", value.height );
			}
			
			
			/** read from a data adaptor */
			public Dimension readValue( final DataAdaptor adaptor ) {
				final int width = adaptor.intValue( "width" );
				final int height = adaptor.intValue( "height" );
				return new Dimension( width, height );
			}
		};
	}
	
	
	/** Get an editor of Rectangle values */
	static PropertyValueEditor<Rectangle> getRectangleEditor() {
		return new PropertyValueTextEditor<Rectangle>() {
			/** get the cell editor value */
			public Rectangle getEditorValue( final BricksContext context ) {
				final JTextField textField = (JTextField)getEditorComponent();
				final String[] valueStrings = textField.getText().split( "," );
				final int x = Integer.parseInt( valueStrings[0].trim() );
				final int y = Integer.parseInt( valueStrings[1].trim() );
				final int width = Integer.parseInt( valueStrings[2].trim() );
				final int height = Integer.parseInt( valueStrings[3].trim() );
				return new Rectangle( x, y, width, height );
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				final JTextField textField = (JTextField)getEditorComponent();
				if ( value != null ) {
					final Rectangle rectangle = (Rectangle)value;
					textField.setText( rectangle.x + ", " + rectangle.y + ", " + rectangle.width + ", " + rectangle.height );
				}
				else {
					textField.setText( "" );
				}
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				final JLabel textLabel = (JLabel)getRenderingComponent();
				if ( value != null ) {
					final Rectangle rectangle = (Rectangle)value;
					textLabel.setText( "x:  " + rectangle.x + ", y:  " + rectangle.y +  ", width:  " + rectangle.width + ", height:  " + rectangle.height );
				}
				else {
					textLabel.setText( "" );
				}
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
				final Rectangle value = (Rectangle)rawValue;
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "x", value.x );
				adaptor.setValue( "y", value.y );
				adaptor.setValue( "width", value.width );
				adaptor.setValue( "height", value.height );
			}
			
			
			/** read from a data adaptor */
			public Rectangle readValue( final DataAdaptor adaptor ) {
				final int x = adaptor.intValue( "x" );
				final int y = adaptor.intValue( "y" );
				final int width = adaptor.intValue( "width" );
				final int height = adaptor.intValue( "height" );
				return new Rectangle( x, y, width, height );
			}
		};
	}
	
	
	/** Get an editor of Insets values */
	static PropertyValueEditor<Insets> getInsetsEditor() {
		return new PropertyValueTextEditor<Insets>() {
			/** get the cell editor value */
			public Insets getEditorValue( final BricksContext context ) {
				final JTextField textField = (JTextField)getEditorComponent();
				final String[] valueStrings = textField.getText().split( "," );
				final int top = Integer.parseInt( valueStrings[0].trim() );
				final int left = Integer.parseInt( valueStrings[1].trim() );
				final int bottom = Integer.parseInt( valueStrings[2].trim() );
				final int right = Integer.parseInt( valueStrings[3].trim() );
				return new Insets( top, left, bottom, right );
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				final JTextField textField = (JTextField)getEditorComponent();
				if ( value != null ) {
					final Insets insets = (Insets)value;
					textField.setText( insets.top + ", " + insets.left + ", " + insets.bottom + ", " + insets.right );
				}
				else {
					textField.setText( "" );
				}
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				final JLabel textLabel = (JLabel)getRenderingComponent();
				if ( value != null ) {
					final Insets insets = (Insets)value;
					textLabel.setText( "top:  " + insets.top + ", left:  " + insets.left +  ", bottom:  " + insets.bottom + ", right:  " + insets.right );
				}
				else {
					textLabel.setText( "" );
				}
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
				final Insets value = (Insets)rawValue;
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "top", value.top );
				adaptor.setValue( "left", value.left );
				adaptor.setValue( "bottom", value.bottom );
				adaptor.setValue( "right", value.right );
			}
			
			
			/** read from a data adaptor */
			public Insets readValue( final DataAdaptor adaptor ) {
				final int top = adaptor.intValue( "top" );
				final int left = adaptor.intValue( "left" );
				final int bottom = adaptor.intValue( "bottom" );
				final int right = adaptor.intValue( "right" );
				return new Insets( top, left, bottom, right );
			}
		};
	}
	
	
	/** Get an editor of icons */
	static PropertyValueEditor<Icon> getIconEditor() {
		return new PropertyValueTextEditor<Icon>() {
			/** get the cell editor value */
			public Icon getEditorValue( final BricksContext context ) {
				try {
					final JTextField textField = (JTextField)getEditorComponent();
					final String text = textField.getText().trim();
					final String[] valueStrings = text.split( ":", 2 );
					final String group = valueStrings.length > 1 ? valueStrings[0].trim() : "";
					final String iconName = valueStrings.length > 1 ? valueStrings[1].trim() : text;
					return IconResource.getInstance( context.getSourceURL(), group, iconName );
				}
				catch ( Exception exception ) {
					exception.printStackTrace();
					return null;
				}
			}
			
			
			/** set the editor value */
			public void setEditorValue( final Object value ) {
				final JTextField textField = (JTextField)getEditorComponent();
				if ( value != null && value instanceof IconResource ) {
					final IconResource icon = (IconResource)value;
					final String group = icon.getGroup();
					final String iconName = icon.getIconName();
					textField.setText( group != null && !group.isEmpty() ? group + ":" + iconName : iconName );	// group:iconName or just iconName if no group is specified
				}
				else {
					textField.setText( "" );
				}
			}
			
			
			/** set the rendering value */
			public void setRenderingValue( final Object value ) {
				final JLabel iconLabel = (JLabel)getRenderingComponent();
				if ( value != null && value instanceof IconResource ) {
					final IconResource icon = (IconResource)value;
					iconLabel.setText( "group:  " + icon.getGroup() + ", name:  " + icon.getIconName() );
				}
				else {
					iconLabel.setText( "" );
				}
			}
			
			
			/** write to a data adaptor */
			public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
				final IconResource value = (IconResource)rawValue;
				super.writeValue( name, value, adaptor );
				adaptor.setValue( "group", value.getGroup() );
				adaptor.setValue( "iconName", value.getIconName() );
			}
			
			
			/** read from a data adaptor */
			public Icon readValue( final DataAdaptor adaptor ) {
				final String group = adaptor.stringValue( "group" );
				final String iconName = adaptor.stringValue( "iconName" );
				final String contextURLSpec = adaptor.stringValue( "contextURL" );
				try {
					final URL contextURL = contextURLSpec != null ? new URL( contextURLSpec ) : null;
					return IconResource.getInstance( contextURL, group, iconName );
				}
				catch( Exception exception ) {
					exception.printStackTrace();
					return null;
				}
			}
		};
	}
}


/** color property editor */
class PropertyValueColorEditor extends PropertyValueEditor<Color> {
	protected JColorChooser COLOR_CHOOSER;
	
	
	/** Constructor */
	public PropertyValueColorEditor() {
		// for some reason this constructor is not being called
	}
	
	
	/** get the color chooser */
	protected JColorChooser getColorChooser() {
		if ( COLOR_CHOOSER == null ) {
			COLOR_CHOOSER = new JColorChooser();
		}
		return COLOR_CHOOSER;
	}
	
	
	/** instantiate a component */
	public Component getEditorComponentInstance() {
		final JButton button = new JButton( "Color" );
		final ActionListener handler = new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {}
		};
		final JDialog dialog = JColorChooser.createDialog( button, "Pick a Color", true, getColorChooser(), handler, null );				
		button.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				dialog.setVisible( true );
				if ( _currentCellEditor != null )  _currentCellEditor.fireEditingStopped();
			}
		} );
		
		return button;
	}
	
	
	/** instantiate a component */
	public Component getRenderingComponentInstance() {
		return new JButton( "Color" );
	}
	
	
	/** get the cell editor value */
	public Color getEditorValue( final BricksContext context ) {
		final Color color = getColorChooser().getColor();
		return color;
	}
	
	
	/** set the editor value */
	public void setEditorValue( final Object value ) {
		getColorChooser().setColor( (Color)value );
		((JButton)getRenderingComponent()).setForeground( (Color)value );
	}
	
	
	/** set the rendering value */
	public void setRenderingValue( final Object value ) {
		((JButton)getRenderingComponent()).setForeground( (Color)value );
	}
	
	
	/** write to a data adaptor */
	public void writeValue( final String name, final Object rawValue, final DataAdaptor adaptor ) {
		final Color value = (Color)rawValue;
		super.writeValue( name, value, adaptor );
		adaptor.setValue( "red", value.getRed() );
		adaptor.setValue( "blue", value.getBlue() );
		adaptor.setValue( "green", value.getGreen() );
		adaptor.setValue( "alpha", value.getAlpha() );
	}
	
	
	/** read from a data adaptor */
	public Color readValue( final DataAdaptor adaptor ) {
		final int red = adaptor.intValue( "red" );
		final int green = adaptor.intValue( "green" );
		final int blue = adaptor.intValue( "blue" );
		final int alpha = adaptor.intValue( "alpha" );
		return new Color( red, green, blue, alpha );
	}
}
