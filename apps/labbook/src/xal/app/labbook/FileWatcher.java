//
//  FileWatcher.java
//  xal
//
//  Created by Thomas Pelaia on 9/20/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.labbook;

import xal.tools.StringJoiner;

import java.io.*;
import java.util.*;
import java.util.prefs.Preferences;


/** Watches a specified folders for new files of particular types */
public class FileWatcher {
	/** sorts files in reverse order of modification date (i.e. newest files first) */
	static protected Comparator<File> FILE_SORTER;
	
	/** preference ID for the file watcher folders */
	final protected String PREFERENCE_ID;
	
	/** preferences */
	final protected Preferences PREFERENCES;
	
	/** file filter */
	final protected FileFilter FILE_FILTER;
	
	/** file filter by modification date */
	final protected FreshFileFilter FRESH_FILE_FILTER;
	
	/** list of folders to watch */
	protected List<File> _folders;
	
	
	// static initializer
	static {
		FILE_SORTER = FileModificationComparator.getReverseSorterInstance();
	}
	
	
	/** Constructor */
	public FileWatcher( final Preferences preferences, final String preferenceID, final FileFilter mediaFilter, final FreshFileFilter freshFileFilter ) {
		PREFERENCES = preferences;
		PREFERENCE_ID = preferenceID;
		FRESH_FILE_FILTER = freshFileFilter;
		FILE_FILTER = andFileFilters( freshFileFilter, mediaFilter );
		_folders =  new ArrayList<File>();
		
		restorePreferences();
	}
	
	
	/**
	 * Get an instance that filters for image files modified within the specified time range
	 * @param preferenceNode class for which the file watcher preferences are stored
	 * @return the file watcher
	 */
	static public FileWatcher getImageWatcherInstance( final Class<ImageController> preferenceNode ) {
		final Preferences preferences = Preferences.userNodeForPackage( preferenceNode );
		return new FileWatcher( preferences, "IMAGE WATCHER FOLDERS", getImageFileFilter( false ), new FreshFileFilter() );
	}
	
	
	/**
	 * Get an instance that filters for image files modified within the specified time range
	 * @param preferenceNode class for which the file watcher preferences are stored
	 * @return the file watcher
	 */
	static public FileWatcher getAttachmentWatcherInstance( final Class<AttachmentController> preferenceNode ) {
		final Preferences preferences = Preferences.userNodeForPackage( preferenceNode );
		return new FileWatcher( preferences, "ATTACHMENT WATCHER FOLDERS", getAttachmentFileFilter( false ), new FreshFileFilter() );
	}
	
	
	/** save preferences */
	protected void savePreferences() {
		final List<File> folders = _folders;        
        StringJoiner joiner = new StringJoiner( "," );
        joiner.append( folders.toArray() );
        PREFERENCES.put( PREFERENCE_ID, joiner.toString() );
	}
	
	
	/** restore preferences */
	protected void restorePreferences() {
		final String pathsString = PREFERENCES.get( PREFERENCE_ID, "" );
		if ( pathsString != null && pathsString != "" ) {
			final String[] paths = pathsString.split( "," );
			for ( final String path : paths ) {
				justWatchFolder( new File( path ) );
			}
		}
	}
	
	
	/** list the filtered files */
	public List<File> listFiles() {
		final List<File> files = new ArrayList<File>();
		
		FRESH_FILE_FILTER.setReferenceDate( new Date() );
		final List<File> folders = new ArrayList<File>( _folders );
		for ( final File folder : folders ) {
			if ( folder.exists() ) {
				final File[] subFiles = folder.listFiles( FILE_FILTER );
				for ( final File subFile : subFiles ) {
					files.add( subFile );
				}
			}
		}
		
		Collections.sort( files, FILE_SORTER );
		
		return files;
	}
	
	
	/** get the folders to watch */
	public List<File> getFolders() {
		return _folders;
	}
	
	
	/** get the folder at the specified index */
	public File getFolder( final int index ) {
		return _folders.get( index );
	}
	
	
	/** watch the folder without adding it to the preferences */
	protected void justWatchFolder( final File folder ) {
		if ( !_folders.contains( folder ) ) {		// prevent duplicates
			_folders.add( folder );
			Collections.sort( _folders );
		}
	}
	
	
	/** add a new folder to the watch list  */
	public void watchFolder( final File folder ) {
		justWatchFolder( folder );
		savePreferences();
	}
	
	
	/** remove a folder from the watch list */
	public void ignoreFolder( final File folder ) {
		_folders.remove( folder );
		savePreferences();
	}
	
	
	/**
	 * Combine file filters using "And" logic so that all criteria must be met for acceptance.
	 * @param fileFilters the list of file filters to combine
	 * @return a file filter that validates against each of the supplied file filters
	 */
	static public FileFilter andFileFilters( final FileFilter... fileFilters ) {
		return new FileFilter() {
            public boolean accept( final File file ) {
				for ( final FileFilter filter : fileFilters ) {
					if ( !filter.accept( file ) )  return false;
				}
                return true;
            }
        };		
	}
	
	
    /**
	 * Create a file filter that accepts image files.
	 * @param includeFolders true to include folders and false to exclude them
     * @return The file filter that accepts image files
     */
    static public FileFilter getImageFileFilter( final boolean includeFolders ) {
		final String[] imageTypes = { "gif", "jpg", "png" };
		return getFileTypeFilter( imageTypes, includeFolders );
    }
	
	
    /**
	 * Create a file filter that accepts attachment files.
	 * @param includeFolders true to include folders and false to exclude them
     * @return The file filter that accepts image files
     */
    static public FileFilter getAttachmentFileFilter( final boolean includeFolders ) {
		final String[] mediaTypes = { "txt", "html", "pdf", "zip", "xls" };
		return getFileTypeFilter( mediaTypes, includeFolders );
    }
	
	
    /**
	 * Create a file filter that accepts files of the specified types.  A type is identified by its filename suffix.
     * @param fileTypes Array of file types to accept
	 * @param includeFolders true to include folders and false to exclude them
     * @return The file filter that accepts the specified file types
     */
    static public FileFilter getFileTypeFilter( final String[] fileTypes, final boolean includeFolders ) {
        return new FileFilter() {
            public boolean accept( final File file ) {
                if ( file.isDirectory() )  return includeFolders;
				
				final String extension = getFileExtension( file );
				
                for ( int index = 0 ; index < fileTypes.length ; index++ ) {
					if ( isMatch( extension, fileTypes[index] ) ) {
						return true;
					}
                }
				
                return false;
            }
        };
    }
	
	
	/**
	 * Determine the file's extension.
	 * @param file  the file for which to get the extension
	 * @return the file's extension
	 */
	static protected String getFileExtension( final File file ) {
		final String name = file.getName().toLowerCase();
		final int extensionIndex = name.lastIndexOf('.');
		
		return ( extensionIndex < ( name.length() - 2 ) && extensionIndex >= 0 ) ? name.substring( extensionIndex + 1 ) : "";
	}
	
	
	/**
	 * Determine whether the extension matches the file type.  If the file type is the wildcard extension, then accept all extensions as matching.
	 * @param extension  the file extension to test for matching to the file type.
	 * @param fileType  the file type against which to test for matching the file extension.
	 * @return true  if the extension matches the file type
	 */
	static protected boolean isMatch( final String extension, final String fileType ) {
		return extension.equalsIgnoreCase( fileType );
	}
}



/** Filter files by modification date within time range of a specified reference time */
class FreshFileFilter implements FileFilter {
	/** default number of hours within which to accept files by modification date.  files older than this are not accepted. */
	static final double DEFAULT_MODIFICATION_HOUR_RANGE = 16.0;
	
	/** time range since the reference date in milliseconds */
	final long MODIFICATION_RANGE;
	
	/** time before relative to which other file modifications are compared */
	protected long _referenceTime;
	
	
	/** Primary Constructor */
	public FreshFileFilter( final Date referenceDate, final double modificationHourRange ) {
		MODIFICATION_RANGE = (long)( 1000 * 3600 * modificationHourRange );	// convert hours to milliseconds
		setReferenceDate( referenceDate );
	}
	
	
	/** Constructor */
	public FreshFileFilter( final Date referenceDate ) {
		this( referenceDate, DEFAULT_MODIFICATION_HOUR_RANGE );
	}

	
	/** Constructor */
	public FreshFileFilter() {
		this( new Date() );
	}
	
	
	/** set the reference date */
	public void setReferenceDate( final Date referenceDate ) {
		_referenceTime = referenceDate.getTime();
	}
	
	
	/** accept files which were modified more recently than the modification range relative to the reference date */
	public boolean accept( final File file ) {
		return ( _referenceTime - file.lastModified() ) < MODIFICATION_RANGE;
	}			
}



