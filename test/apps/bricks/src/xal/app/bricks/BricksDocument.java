/*
 * BuilderDocument.java
 *
 * Created on Fri April 17 15:12:21 EDT 2006
 *
 * Copyright (c) 2006 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.bricks;

import xal.extension.application.*;
import xal.extension.bricks.*;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;

import java.net.URL;
import java.util.List;
import java.beans.*;
import java.awt.event.*;


/**
 * BuilderDocument
 * @author  tap
 */
class BricksDocument extends XalDocument implements DataListener, BrickListener {
	/** root brick */
	final RootBrick ROOT_BRICK;
	
	/** context in which the bricks runtime runs */
	private final BricksContext CONTEXT;
	
	
	/** Constructor */
    public BricksDocument() {
        this( null );
    }
    
    
    /** 
     * Create a new document loaded from the URL file 
     * @param url The URL of the file to load into the new document.
     */
    public BricksDocument( final java.net.URL url ) {
		ROOT_BRICK = new RootBrick();
        setSource( url );
		
		CONTEXT = new BricksContext( url );
		
        if ( url != null ) {
			System.out.println( "Opening document: " + url );
			final DataAdaptor documentAdaptor = XmlDataAdaptor.adaptorForUrl( url, false );
			update( documentAdaptor.childAdaptor( dataLabel() ) );
		}
		
		setHasChanges( false );
    }
	
	
	/** Get the context within which bricks is run */
	public BricksContext getContext() {
		return CONTEXT;
	}
    
    
    /**
     * Make a main window by instantiating the my custom window.
     */
    public void makeMainWindow() {
        mainWindow = new BricksWindow( this, ROOT_BRICK );
		ROOT_BRICK.addBrickListener( this );		
		setHasChanges( false );
		
		mainWindow.addWindowListener( new WindowAdapter() {
			public void windowOpened( final WindowEvent event ) {
				BricksDocument.this.setHasChanges( false );
			}
		});
    }
	
    
    /**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs( final URL url ) {
        writeDataTo( this, url );
    }
    
    
    /**
     * Custom code called by writeDataTo() after data has been successfully written to the specified URL.
     * @param dataRoot DataListener root of the document to save
     * @param url The URL to which the document should be saved.
     */
    public void handleDataWrittenTo( final DataListener dataRoot, final URL url ) {
        CONTEXT.setSourceURL( url );
    }

	
	/**
	 * Convenience method to get this document's corresponding window cast as an Energy Manager window.
	 * @return this document's corresponding window
	 */
	protected BricksWindow getBricksWindow() {
		return (BricksWindow)getMainWindow();
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return "BricksDocument";
	}
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {		
		final DataAdaptor rootAdaptor = adaptor.childAdaptor( RootBrick.DATA_LABEL );
		rootAdaptor.setValue( "contextURL", CONTEXT.getSourceURL().toString() );
        ROOT_BRICK.update( rootAdaptor );
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "version", "1.0.0" );
        adaptor.setValue( "date", new java.util.Date().toString() );
		
        adaptor.writeNode( getBricksWindow().getRootBrick() );		
	}
	
	
	/**
	 * Handle the event in which nodes have been added to a container
	 * @param source the source of the event
	 * @param container the node to which nodes have been added
	 * @param nodes the nodes which have been added
	 */
	public void nodesAdded( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Handle the event in which nodes have been removed from a container
	 * @param source the source of the event
	 * @param container the node from which nodes have been removed
	 * @param nodes the nodes which have been removed
	 */
	public void nodesRemoved( final Object source, final Brick container, final List<BeanNode<?>> nodes ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Handle the event in which a bean's property has been changed
	 * @param beanNode the node whose property has changed
	 * @param propertyDescritpr the property which has changed
	 * @param value the new value
	 */
	public void propertyChanged( final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value ) {
		setHasChanges( true );
	}
	
	
	/**
	 * Handle the event in which a brick's tree path needs refresh
	 * @param source the source of the event
	 * @param brick the brick at which the refresh needs to be done
	 */
	public void treeNeedsRefresh( final Object source, final Brick brick ) {
		setHasChanges( true );
	}
}




