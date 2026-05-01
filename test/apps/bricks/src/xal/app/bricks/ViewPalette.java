//
//  ViewPalette.java
//  xal
//
//  Created by Thomas Pelaia on 4/18/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.bricks;

import xal.extension.bricks.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.util.Vector;


/** Palette of views which can be dropped onto a window. */
public class ViewPalette extends JTabbedPane {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	/** Constructor */
	public ViewPalette() {
		super( JTabbedPane.LEFT );

        setSize( 100, 600 );
		setMaximumSize( new Dimension( 125, 32000 ) );
		makeContent();
	}
	
	
	/** Make the content for the window. */
	private void makeContent() {
		addTab( this, "Controls", makeControlsViews() );
		addTab( this, "Text", makeTextViews() );
		addTab( this, "Data", makeDataViews() );
		addTab( this, "Containers", makeContainers() );
		addTab( this, "Windows", makeWindows() );
		addTab( this, "Borders", makeBorders() );
	}
	
	
	/** add a tab of views to the tabbed pane */
	@SuppressWarnings( {"rawtypes", "unchecked"} )		// JList only supports generics in Java 7 or later
	private void addTab( final JTabbedPane tabbedPane, final String name, final Vector<? extends BeanProxy> views ) {
		final JList list = new JList();
		
		list.setDragEnabled( true );
		list.setTransferHandler( new ViewTransferHandler( list ) );
		list.setListData( views );
		list.setCellRenderer( new ViewCellRenderer() );
		
		tabbedPane.add( name, new JScrollPane( list ) );
	}
	
	
	/** add a tab of borders to the tabbed pane */
	@SuppressWarnings( {"rawtypes", "unchecked"} )		// JList only supports generics in Java 7 or later
	private void addBorderTab( final JTabbedPane tabbedPane, final String name, final Vector<BorderProxy> borders ) {
		final JList list = new JList();
		
		list.setDragEnabled( true );
		list.setTransferHandler( new ViewTransferHandler( list ) );
		list.setListData( borders );
		list.setCellRenderer( new ViewCellRenderer() );
		
		tabbedPane.add( name, new JScrollPane( list ) );
	}
	
	
	/** make the controls views */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<ViewProxy> makeControlsViews() {
		final Vector<ViewProxy> views = new Vector<ViewProxy>();
		
		views.add( ViewProxyFactory.getViewProxy( "JButton" ) );
		views.add( ViewProxyFactory.getViewProxy( "JToggleButton" ) );
		views.add( ViewProxyFactory.getViewProxy( "JCheckBox" ) );
		views.add( ViewProxyFactory.getViewProxy( "JRadioButton" ) );
		views.add( ViewProxyFactory.getViewProxy( "JSpinner" ) );
		views.add( ViewProxyFactory.getViewProxy( "JProgressBar" ) );
		views.add( ViewProxyFactory.getViewProxy( "JSlider" ) );
		views.add( ViewProxyFactory.getViewProxy( "JComboBox" ) );

		return views;
	}
	
	
	/** make the controls views */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<ViewProxy> makeTextViews() {
		final Vector<ViewProxy> views = new Vector<ViewProxy>();
		
		views.add( ViewProxyFactory.getViewProxy( "JLabel" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTextField" ) );
		views.add( ViewProxyFactory.getViewProxy( "JPasswordField" ) );
		views.add( ViewProxyFactory.getViewProxy( "JFormattedTextField" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTextArea" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTextPane" ) );
		views.add( ViewProxyFactory.getViewProxy( "JEditorPane" ) );
		
		return views;
	}
	
	
	/** make the controls views */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<ViewProxy> makeDataViews() {
		final Vector<ViewProxy> views = new Vector<ViewProxy>();
		
		views.add( ViewProxyFactory.getViewProxy( "JList" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTable" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTree" ) );
		views.add( ViewProxyFactory.getViewProxy( "xal.extension.widgets.plot.FunctionGraphsJPanel" ) );
				
		return views;
	}
	
	
	/** make the containers */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<ViewProxy> makeContainers() {
		final Vector<ViewProxy> views = new Vector<ViewProxy>();
		
		views.add( ViewProxyFactory.getViewProxy( "JScrollPane" ) );
		views.add( ViewProxyFactory.getViewProxy( "JSplitPane" ) );
		views.add( ViewProxyFactory.getViewProxy( "JTabbedPane" ) );
		views.add( ViewProxyFactory.getViewProxy( "JPanel" ) );
		views.add( ViewProxyFactory.getViewProxy( "JToolBar" ) );
		views.add( ViewProxyFactory.getViewProxy( "Box_Horizontal" ) );
		views.add( ViewProxyFactory.getViewProxy( "Box_Vertical" ) );
		views.add( ViewProxyFactory.getViewProxy( "Box_HorizontalGlue" ) );
		views.add( ViewProxyFactory.getViewProxy( "Box_VerticalGlue" ) );
		
		return views;
	}
	
	
	/** make the windows */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<ViewProxy> makeWindows() {
		final Vector<ViewProxy> views = new Vector<ViewProxy>();

		views.add( ViewProxyFactory.getViewProxy( "JFrame" ) );
		views.add( ViewProxyFactory.getViewProxy( "JDialog" ) );
		
		return views;
	}
	
	
	/** make the controls views */
	@SuppressWarnings( "rawtypes" )		// the types are irrelevant
	private Vector<BorderProxy> makeBorders() {
		final Vector<BorderProxy> views = new Vector<BorderProxy>();
		
		views.add( BorderProxyFactory.getBorderProxy( "EtchedBorder" ) );
		views.add( BorderProxyFactory.getBorderProxy( "BevelBorder_Lowered" ) );
		views.add( BorderProxyFactory.getBorderProxy( "BevelBorder_Raised" ) );
		views.add( BorderProxyFactory.getBorderProxy( "TitledBorder" ) );
		
		return views;
	}
	
	
	
	/** Knob list transfer handler */
	@SuppressWarnings( "rawtypes" )		// JList only supports generics in Java 7 or later
	class ViewTransferHandler extends TransferHandler {
        /** serialization identifier */
        private static final long serialVersionUID = 1L;
        
		final protected JList VIEW_LIST;
		
		
		/** Constructor */
		public ViewTransferHandler( final JList list ) {
			VIEW_LIST = list;
		}
		
		
		/** transfer views from the palette */
		protected Transferable createTransferable( final JComponent component ) {
			final BeanProxy<?> proxy = (BeanProxy<?>)VIEW_LIST.getSelectedValue();
			if ( proxy != null ) {
				return new ViewTransferable( proxy );
			}
			else {
				return null;
			}
		}
		
		
		/** provides copy or move operation */
		public int getSourceActions( final JComponent component ) {
			return COPY;
		}
	}
}


/** render the view as an icon */
@SuppressWarnings( "rawtypes" )		// JList and ListCellRenderer only support generics in Java 7 or later
class ViewCellRenderer extends JLabel implements ListCellRenderer {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    
	public Component getListCellRendererComponent( final JList list, final Object value, final int index, boolean isSelected, boolean cellHasFocus ) {
		if ( value instanceof ViewProxy ) {
			final BeanProxy proxy = (BeanProxy)value;
			final Icon icon = proxy.getIcon();
			if ( icon != null ) {
				setText( "" );
				setIcon( icon );
			}
			else {
				setIcon( null );
				setText( proxy.getName() );
			}
		}
		else if ( value == null ) {
			System.out.println( "Null value at index:  " + index );
		}
		else {
			setIcon( null );
			setText( value.toString() );
		}
		
		if ( isSelected ) {
			setBackground( list.getSelectionBackground() );
			setForeground( list.getSelectionForeground() );
		}
		else {
			setBackground( list.getBackground() );
			setForeground( list.getForeground() );
		}
		
		setFont( list.getFont() );
		setEnabled( list.isEnabled() );
		setOpaque( true );
		return this;
	}
}