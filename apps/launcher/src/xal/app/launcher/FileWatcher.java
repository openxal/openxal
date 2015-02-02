//
//  FileWatcher.java
//  xal
//
//  Created by Thomas Pelaia on 9/20/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.launcher;

import xal.tools.StringJoiner;
import xal.tools.data.*;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.regex.Pattern;


/** Watches specified folders for new files of particular types */
public class FileWatcher implements DataListener {
	/** Name of the library folder to skip during deep searches under the watched directories. Library directories may contain scripts and jar files which should not be executed directly by the Launcher. */
	static private final String LIBRARY_FOLDER_NAME = "lib";

	/** default maximum folder depth for which to search for matching files */
	static private final int DEFAULT_MAX_FOLDER_SEARCH_DEPTH = 1;
	
	/** DataAdaptor label used in reading and writing */
	static public final String DATA_LABEL = "FileWatcher";
	
	/** sorts files by file name */
	static private Comparator<File> FILE_SORTER;
	
	/** model */
	final private LaunchModel MODEL;
	
	/** list of folders to watch */
	private List<File> _folders;
	
	
	// static initializer
	static {
		FILE_SORTER = FileNameComparator.getInstance();
	}
	
	
	/** Constructor */
	public FileWatcher( final LaunchModel model ) {
		MODEL = model;
		
		_folders =  new ArrayList<File>();
	}
	
	
	/** preconfigure when initializing without a document file */
	public void preConfigure() {
		try {
			// set the watch folder to be the directory containing the jar file that launched this application
			final URL jarURL = getClass().getProtectionDomain().getCodeSource().getLocation();
			final File jarFile = new File( jarURL.toURI() );
			final File applicationsDirectory = jarFile.getParentFile();
			justWatchFolder( applicationsDirectory );			// watch the applications directory
			
			final File productsDirectory = applicationsDirectory.getParentFile();		// products directory is the parent of the apps directory
			justWatchFolder( new File( productsDirectory, "scripts" ) );	// watch the scripts directory
		}
		catch( Exception exception ) {
			exception.printStackTrace();
		}
	}
	
    
    /** 
     * provides the name used to identify the class in an external data source.
     * @return The tag for this data node.
     */
    public String dataLabel() {
        return DATA_LABEL;
    }
    
    
    /**
     * Instructs the receiver to update its data based on the given adaptor.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void update( final DataAdaptor adaptor ) {
		_folders.clear();
		final List<DataAdaptor> pathAdaptors = adaptor.childAdaptors( "Paths" );
		for ( final DataAdaptor pathAdaptor : pathAdaptors ) {
			final String path = pathAdaptor.stringValue( "path" );
			justWatchFolder( new File( path ) );
		}
    }
    
    
    /**
     * Instructs the receiver to write its data to the adaptor for external storage.
     * @param adaptor The data adaptor corresponding to this object's data node.
     */
    public void write( final DataAdaptor adaptor ) {
		for ( final File folder : _folders ) {
			final DataAdaptor pathAdaptor = adaptor.createChild( "Paths" );
			pathAdaptor.setValue( "path", folder.getPath() );
		}
    }
		
	
	/** list the filtered files */
	public List<File> listFiles() {
		final List<File> files = new ArrayList<File>();
		final List<Rule> rules = MODEL.getRules();
		
		final List<File> folders = new ArrayList<File>( _folders );
		for ( final File folder : folders ) {
			if ( folder.exists() ) {
				files.addAll( listFilesUnder( folder, rules, 0 ) );
			}
		}
		
		Collections.sort( files, FILE_SORTER );
		
		return files;
	}
	
	
	/** list matching files in the specified folder searching deeply */
	private List<File> listFilesUnder( final File folder, final List<Rule> rules, final int depth ) {
		final List<File> files = new ArrayList<File>();
		try {
			final String resolvedPath = folder.getCanonicalPath();	// absolute path with links resolved
			final File[] subFiles = folder.listFiles();
			for ( final File subFile : subFiles ) {
				if ( subFile.isDirectory() && !subFile.getName().equals( LIBRARY_FOLDER_NAME ) ) {		// deep search but skipping the library folder
					// ignore hidden files and avoid cycles by checking that the sub folder is not an ancestor of the current folder
					try {
						if ( depth < DEFAULT_MAX_FOLDER_SEARCH_DEPTH && !subFile.isHidden() && !resolvedPath.startsWith( subFile.getCanonicalPath() ) ) {
							files.addAll( listFilesUnder( subFile, rules, depth+1 ) );
						}
					}
					catch( IOException exception ) {
						exception.printStackTrace();
					}
				}
				else if ( matches( subFile, rules ) ) {
					files.add( subFile );
				}
			}
		}
		catch ( IOException exception ) {
			exception.printStackTrace();
		}
		
		return files;
	}
	
	
	/** determine whether the specified file satisfies the specified rule */
	static private boolean matches( final File file, final List<Rule> rules ) {
		final String name = file.getName();
		
		// check each rule in order until a file name is matched and if a match is found, determine whether it is for excluding the file
		if ( name != null && name.length() > 0 ) {
			for ( final Rule rule : rules ) {
				if ( rule.matchesFileName( name ) ) {
					return !rule.excludes();	//if the file name matches then test whether the rule is for exclusion
				}
			}
		}
		
		return false;		
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
	private void justWatchFolder( final File folder ) {
		if ( !_folders.contains( folder ) ) {		// prevent duplicates
			_folders.add( folder );
			Collections.sort( _folders );
		}
	}
	
	
	/** add new folders to the watch list  */
	public void watchFolders( final File[] folders ) {
		for ( final File folder : folders ) {
			justWatchFolder( folder );
		}
		MODEL.postModifications();
	}
	
	
	/** add a new folder to the watch list  */
	public void watchFolder( final File folder ) {
		justWatchFolder( folder );
		MODEL.postModifications();
	}
	
	
	/** remove a folder from the watch list */
	public void ignoreFolder( final File folder ) {
		_folders.remove( folder );
		MODEL.postModifications();
	}
}



/** compares files by name */
class FileNameComparator implements Comparator<File> {
	/** get instance */
	static public FileNameComparator getInstance() {
		return new FileNameComparator();
	}
	
	
	/** compare two files by modification date */
	public int compare( final File firstFile, final File secondFile ) {
		return firstFile.getName().compareTo( secondFile.getName() );
	}
	
	
	/** check for comparator equality */
	public boolean equals( final Object comparator ) {
		return this == comparator;
	}


	/** override hashCode() as required to be consistent with equals() */
	public int hashCode() {
		return 1;	// an instance of this class is equivalent to any other
	}
}
