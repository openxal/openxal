//
//  ViewProxy.java
//  xal
//
//  Created by Thomas Pelaia on 7/3/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.image.*;
import javax.swing.*;
import java.beans.*;

import xal.tools.data.*;


/** interface for providing view node behavior */
abstract public class ViewProxy<ViewType extends Component> extends BeanProxy<ViewType> {
	/** data label */
	final static public String DATA_LABEL = "ViewProxy";
	
	/** indicates whether the component should accept components */
	final protected boolean IS_CONTAINER;
	
	/** indicates whether to display a prototype icon */
	final protected boolean MAKE_ICON;
	
	
	/** Constructor */
	public ViewProxy( final Class<ViewType> prototypeClass, final boolean isContainer, final boolean makeIcon ) {
		super( prototypeClass );
		IS_CONTAINER = isContainer;
		MAKE_ICON = makeIcon;
	}
	
	
	/** generator */
	static public ViewProxy<?> getInstance( final DataAdaptor adaptor ) {
		return ViewProxyFactory.getViewProxy( adaptor.stringValue( "type" ) );
	}
	
	
	/** Determine whether the view should be treated as a container */
	public boolean isContainer() {
		return IS_CONTAINER;
	}
	
	
	/** determine if the view is a window */
	public boolean isWindow() {
		return java.awt.Window.class.isAssignableFrom( PROTOTYPE_CLASS );
	}
	
	
	/**
	 * Get the container to which sub views should be added
	 * @param view the view whose container is to be gotten
	 * @return the view's container
	 */
	public Container getContainer( final ViewType view ) {
		return view instanceof RootPaneContainer ? ((RootPaneContainer)view).getContentPane() : view instanceof Container ? (Container)view : null;
	}
	
	
	/** handle child node property change */
	public void handleChildNodePropertyChange( final ViewNode node, final BeanNode<?> beanNode, final PropertyDescriptor propertyDescriptor, final Object value ) {}
	
	
	/** Get an icon representation for the view */
	public Icon getIcon()  {
		if ( MAKE_ICON && JComponent.class.isAssignableFrom( PROTOTYPE_CLASS ) ) {
			return new ImageIcon( getIconImage() );
		}
		else {
			return null;
		}
	}
	
	
	/** Get an image representation for the view */
	public Image getIconImage()  {
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo( PROTOTYPE_CLASS );
			final Image image = beanInfo.getIcon( BeanInfo.ICON_COLOR_16x16 );
			return image != null ? image : makeImage();
		}
		catch ( IntrospectionException exception ) {
			return makeImage();
		}
	}
	
	
	/** make the image from the component itself */
	private Image makeImage() {
		final JComponent view = (JComponent)getPrototype();
		final Dimension imageSize = view.getPreferredSize();
		final int width = imageSize.width <= 0 ? 60 : imageSize.width;
		final int height = imageSize.height <= 0 ? 40 : imageSize.height;
		view.setSize( width, height );
		final BufferedImage image = new BufferedImage( width, height, BufferedImage.TYPE_3BYTE_BGR );
		view.paint( image.createGraphics() );
		return image;		
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    public String dataLabel() {
		return DATA_LABEL;
	}
}
