/*
 * FileFilterFactory.java
 *
 * Created on Thu May 20 09:45:22 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.tools.apputils.files;

import xal.tools.StringJoiner;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;


/**
 * FileFilterFactory generates file filters based on supplied file types.
 *
 * @author  tap
 */
public class FileFilterFactory {
	/** wildcard extension */
	static public final String WILDCARD_FILE_EXTENSION = "*";
	
    /** Creates a new instance of FileFilterFactory */
    protected FileFilterFactory() {}
    
    
    /**
     * Applies the file filters to the file chooser and returns this file chooser.
	 * Existing file filters are removed from the file chooser and then new file filters
	 * are added to the file chooser consistent with the specified file types.
	 * @param fileChooser the file chooser to which to add the file filters
     * @param fileTypes An array of file types to accept
     * @return The file chooser that accepts the specified file types (same file chooser as the argument)
     */
    static public JFileChooser applyFileFilters( final JFileChooser fileChooser, final String[] fileTypes ) {
        for ( int index = 0 ; index < fileTypes.length ; index++ ) {
            FileFilter filter = getFileFilter(fileTypes[index]);
            fileChooser.addChoosableFileFilter(filter);
        }

		final FileFilter aggregateFilter = getSupportedFileFilter( fileTypes );
        fileChooser.addChoosableFileFilter( aggregateFilter );
		fileChooser.setFileFilter( aggregateFilter );
        
        return fileChooser;
    }
    
    
    /**
     * Create a file filter that accepts files of the specified type.  This is 
     * a supporting method for creating file choosers.  A type is identified
     * by its filename suffix.
     * @param fileType File type to accept
     * @return The file filter that accepts the specified file type
     */
    static public FileFilter getFileFilter( final String fileType ) {
        return new FileFilter() {
			final String suffix = "." + fileType.toLowerCase();
			
            public boolean accept( final java.io.File file ) {
                if ( file.isDirectory() )  return true;
				
				final String extension = getFileExtension( file );
				return isMatch( extension, fileType );
            }
            
            
            /**
             * Get the file type as the file filter description.
             * @return File filter description.
             */
            public String getDescription() {
                return fileType;
            }
        };
    }
    
    
    
    
    /**
     * Create a file filter that accepts files of the specified types.  This is 
     * a supporting method for creating file choosers.  A type is identified
     * by its filename suffix.
     * @param fileTypes Array of file types to accept
     * @return The file filter that accepts the specified file types
     */
    static public FileFilter getSupportedFileFilter( final String[] fileTypes ) {
        return new FileFilter() {
            public boolean accept( final java.io.File file ) {
                if ( file.isDirectory() )  return true;
				
				final String extension = getFileExtension( file );
				
                for ( int index = 0 ; index < fileTypes.length ; index++ ) {
					if ( isMatch( extension, fileTypes[index] ) ) {
						return true;
					}
                }
				
                return false;
            }
            
            
            /**
             * Description of the file filter which is simply "Supported Files".
             * @return Description of the file filter.
             */
            public String getDescription() {
                return "Supported Files";
            }
        };
    }
	
	
	/**
	 * Determine the file's extension.
	 * 
	 * @param file  the file for which to get the extension
	 * @return the file's extension
	 */
	static protected String getFileExtension( final java.io.File file ) {
		final String name = file.getName().toLowerCase();
		final int extensionIndex = name.lastIndexOf('.');
		
		return ( extensionIndex < ( name.length() - 2 ) && extensionIndex >= 0 ) ? name.substring(extensionIndex + 1) : "";
	}
	
	
	/**
	 * Determine whether the extension matches the file type.  If the file type is the wildcard extension, then accept all extensions as matching.
	 * 
	 * @param extension  the file extension to test for matching to the file type.
	 * @param fileType  the file type against which to test for matching the file extension.
	 * @return true  if the extension matches the file type
	 */
	static protected boolean isMatch( final String extension, final String fileType ) {
		return fileType.equals( WILDCARD_FILE_EXTENSION ) || extension.equalsIgnoreCase( fileType );
	}
}
