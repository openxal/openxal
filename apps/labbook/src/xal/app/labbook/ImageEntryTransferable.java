//
//  ImageEntryTransferable.java
//  xal
//
//  Created by Thomas Pelaia on 9/22/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.ArrayList;


/** Implement a Transferable for the image entry being dragged */
class ImageEntryTransferable implements Transferable {
	/** define the image entry flavor */
	static public final DataFlavor IMAGE_ENTRY_FLAVOR;
	
	/** the list of flavors associated with application transfer */
	static public final DataFlavor[] FLAVORS;
	
	/** The entries being transferred */
	protected final List<ImageEntry> ENTRIES;
	
	
	// static initializer
	static {
		DataFlavor flavor = null;
		try {
			flavor = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType + ";class=xal.app.labbook.ImageEntry" );
		}
		catch( ClassNotFoundException exception ) {
			exception.printStackTrace();
		}
		finally {
			IMAGE_ENTRY_FLAVOR = flavor;
			FLAVORS = new DataFlavor[] { IMAGE_ENTRY_FLAVOR };
		}
	}
	
	
	/**
	 * Constructor
	 * @param entres The image entries being transferred
	 */
	public ImageEntryTransferable( final List<ImageEntry> entries ) {
		ENTRIES = new ArrayList<ImageEntry>( entries );
	}
	
	
	/**
	 * Get the data being transfered which in this case is simply the list of image entries
	 * @param flavor The flavor of the transfer
	 * @return The entries being transfered
	 */
	public Object getTransferData( final DataFlavor flavor ) {
		return ENTRIES;
	}
	
	
	/**
	 * The flavors handled by this transferable which is presently just KNOB_FLAVOR
	 * @return the array of flavors handled
	 */
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}
	
	
	/**
	 * Test if the specified flavor is supported by this instance.  Only KNOB_FLAVOR is currently supported.
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

