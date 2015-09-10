//
//  FileModificationComparator.java
//  xal
//
//  Created by Thomas Pelaia on 9/21/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import java.io.*;
import java.util.*;


/** compares files by modification date */
public class FileModificationComparator implements Comparator<File> {
	/** get instance */
	static public FileModificationComparator getInstance() {
		return new FileModificationComparator();
	}
	
	
	/** get reverse sorter instance */
	static public Comparator<File> getReverseSorterInstance() {
		return Collections.reverseOrder( getInstance() );
	}
	
	
	/** compare two files by modification date */
	public int compare( final File firstFile, final File secondFile ) {
		final long firstMofification = firstFile.lastModified();
		final long secondModification = secondFile.lastModified();
		return firstMofification > secondModification ? 1 : firstMofification < secondModification ? -1 : 0;
	}
	
	
	/** check for comparator equality */
	public boolean equals( final Object comparator ) {
		return this == comparator;
	}


	/** Override hashCode() as required for consistency with equals() */
	public int hashCode() {
		return super.hashCode();
	}
}
