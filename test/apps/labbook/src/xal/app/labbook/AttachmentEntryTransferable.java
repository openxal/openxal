//
//  AttachmentEntryTransferable.java
//  xal
//
//  Created by Thomas Pelaia on 9/25/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.awt.datatransfer.*;
import java.util.List;
import java.util.ArrayList;


/** Implement a Transferable for the media entry being dragged */
public class AttachmentEntryTransferable implements Transferable {
	/** define the media entry flavor */
	static public final DataFlavor MEDIA_ENTRY_FLAVOR;
	
	/** the list of flavors associated with application transfer */
	static public final DataFlavor[] FLAVORS;
	
	/** The entries being transferred */
	protected final List<AttachmentEntry> ENTRIES;
	
	
	// static initializer
	static {
		DataFlavor flavor = null;
		try {
			flavor = new DataFlavor( DataFlavor.javaJVMLocalObjectMimeType + ";class=xal.app.labbook.AttachmentEntry" );
		}
		catch( ClassNotFoundException exception ) {
			exception.printStackTrace();
		}
		finally {
			MEDIA_ENTRY_FLAVOR = flavor;
			FLAVORS = new DataFlavor[] { MEDIA_ENTRY_FLAVOR };
		}
	}
	
	
	/**
	 * Constructor
	 * @param entries The image entries being transferred
	 */
	public AttachmentEntryTransferable( final List<AttachmentEntry> entries ) {
		ENTRIES = new ArrayList<AttachmentEntry>( entries );
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
