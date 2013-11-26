//
//  BorderNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Enumeration;

import xal.tools.data.*;


/** brick which represents a view */
public class BorderNode extends BeanNode<Border> {
	/** data label */
	public static String DATA_LABEL = "BorderNode";
	
	/** Primary Constructor */
    @SuppressWarnings( "unchecked" )    // nothing we can do to type BorderNode any tighter without introducing a type on BorderNode
	public BorderNode( final BorderProxy<Border> borderProxy, final Map<String,Object> beanSettings, final String tag ) {
		super( borderProxy, beanSettings, tag );		
	}
	
	
	/** Primary Constructor */
	public BorderNode( final BorderProxy<Border> borderProxy ) {
		this( borderProxy, null, borderProxy.getName() );
	}
	
	
	/** Constructor */
	public BorderNode( final BorderNode node ) {
		this ( node.getBorderProxy(), node.BEAN_SETTINGS, node.getTag() );
		
		setCustomBeanClassName( node.getCustomBeanClassName() );
	}
	
	
	/** get the bean instance */
	protected Border getPrototypeBean( final BeanProxy<Border> beanProxy ) {
		return beanProxy.getPrototype();
	}
	
	
	/** generator */
	static public BorderNode getInstance( final DataAdaptor adaptor ) {		
		final DataAdaptor proxyAdaptor = adaptor.childAdaptor( BorderProxy.DATA_LABEL );
		final BorderProxy<Border> borderProxy = BorderProxy.getInstance( proxyAdaptor );
		final String tag = adaptor.stringValue( "tag" );
		final BorderNode node = new BorderNode( borderProxy, null, tag );
		
		node.update( adaptor );
		
		return node;
	}
	
	
	/**
	 * Get the border.
	 * @return the border
	 */
	public Border getBorder() {
		return BEAN_OBJECT;
	}
	
	
	/**
	 * Get the border proxy
	 * @return the border proxy
	 */
	public BorderProxy<Border> getBorderProxy() {
		return (BorderProxy<Border>)BEAN_PROXY;
	}
	
	
	/**
	 * Determine if the brick can add the specified view
	 * @return true if it can add the specified view and false if not
	 */
	public boolean canAdd( final BeanProxy<?> beanProxy ) {
		return false;
	}
	
	
	/** refresh display */
	public void refreshDisplay() {
		final BeanNode<?> node = (BeanNode<?>)getContainingBrick();
		if ( node != null ) {
			node.refreshDisplay();
		}
	}
	
	
	/** Remove this brick from its parent */
	public void removeFromParent() {
		final ViewNode parent = (ViewNode)getContainingBrick();
		if ( parent.getBorderNode() == this ) {
			parent.setBorderNode( null );
		}
	}
	
	
	/** Display the bean's window */
	public void display() {
		final ViewNode parent = (ViewNode)getContainingBrick();
		if ( parent.getBorderNode() == this ) {
			parent.display();
		}
	}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
	public String dataLabel() {
		return DATA_LABEL;
	}
}
