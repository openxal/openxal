//
//  NodeView.java
//  xal
//
//  Created by Thomas Pelaia on 4/19/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//


package xal.app.energymanager;

import javax.swing.*;

import java.util.List;


/** View of a node's properties and state. */
public class NodeView extends Box {
    
    private static final long serialVersionUID = 1L;
    
	protected NodeAgent _node;
	
	
	/** Primary constructor */
	public NodeView( final NodeAgent node ) {
		super( BoxLayout.Y_AXIS );
		
		setNode( node );
	}
	
	
	/** Constructor */
	public NodeView() {
		this( null );
	}
	
	
	/**
	 * Set the node to the one specified.
	 * @param node the node to view.
	 */
	public void setNode( final NodeAgent node ) {
		_node = node;
		makeView( node );
	}
	

	/** 
	 * Make the view for the specified node.
	 * @param node the node for which to generate the view.
	 */
	private void makeView( final NodeAgent node ) {
		removeAll();
		
		if ( node == null )  return;
		
		add( new JLabel( node.getID() ) );
		
		final List<LiveParameter> parameters = node.getLiveParameters();
		for ( int index = 0 ; index < parameters.size() ; index++ ) {
			add( new LiveParameterView( parameters.get(index) ) );
		}
		
		validate();
	}
}
