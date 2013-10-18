//
//  ViewTransferable.java
//  xal
//
//  Created by Thomas Pelaia on 4/17/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/** transferable for transfering views */
public class ViewTransferable implements Transferable {
	/** define the view flavor */
	static public final DataFlavor VIEW_FLAVOR;
	
	/** the list of flavors associated with view transfer */
	static public final DataFlavor[] FLAVORS;
	
	/** The views being transferred */
	protected final List<BeanProxy<?>> VIEW_PROXIES;
	
	
	// static initializer
	static {
		VIEW_FLAVOR = new DataFlavor( BeanProxy.class, "View" );
		FLAVORS = new DataFlavor[] { VIEW_FLAVOR };
	}
	
	
	/**
	 * Primary Constructor
	 * @param beanProxies The views being transferred
	 */
	public ViewTransferable( final List<BeanProxy<?>> beanProxies ) {
		VIEW_PROXIES = new ArrayList<BeanProxy<?>>( beanProxies );
	}
	
	
	/**
	 * Constructor
	 * @param viewProxy The view to transfer
	 */
	public ViewTransferable( final BeanProxy<?> viewProxy ) {
		this( Collections.<BeanProxy<?>>singletonList( viewProxy ) );
	}
	
	
	/**
	 * Get the data being transfered which in this case is simply the list of views
	 * @param flavor The flavor of the transfer
	 * @return The views being transfered
	 */
	public Object getTransferData( final DataFlavor flavor ) {
		return VIEW_PROXIES;
	}
	
	
	/**
	 * The flavors handled by this transferable which is presently just VIEW_FLAVOR
	 * @return the array of flavors handled
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}
	
	
	/**
	 * Test if the specified flavor is supported by this instance.  Only VIEW_FLAVOR is currently supported.
	 * @param flavor The flavor to test.
	 * @return true if the flavor is among the supported flavors and false otherwise.
	 */
	public boolean isDataFlavorSupported( final DataFlavor flavor ) {
		for ( int index = 0 ; index < FLAVORS.length ; index++ ) {
			if ( FLAVORS[index].equals( flavor ) )  return true;
		}
		return false;
	}
}
