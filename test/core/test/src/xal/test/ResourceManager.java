//
// ResourceManager.java
// Open XAL 
//
// Created by Tom Pelaia on 9/13/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.test;

import java.net.URL;
import java.io.File;

import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;


/** ResourceManager is not a test suite, but rather it is a utility used by test suites to load recources from the common test resources package. */
public class ResourceManager {
    /** 
     * Get the URL to the specified resource in the common test resources package.
     * @param resourcePath full modified package path (see the notes for the getResource() method of java.lang.Class) relative to the resources location
     * @return URL to the resource or null if there is none found
     */
    static public URL getResourceURL( final String resourcePath ) {
        return getResourceURL( ResourceManager.class, resourcePath );
    }


	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	static public URL getResourceURL( final Class<?> rootClass, final String resourcePath ) {
		return xal.tools.ResourceManager.getResourceURL( "test", rootClass, resourcePath );
	}


    /** Load and get the default test accelerator */
    static public URL getTestAcceleratorURL() {
        return getResourceURL( "/config/main.xal" );
    }

    
    /** Load and get the default test accelerator */
    static public Accelerator getTestAccelerator() {
		final URL opticsURL = getTestAcceleratorURL();
		return opticsURL != null ? getAcceleratorAtURL( opticsURL ) : null;
    }

    
    /** 
     * Load and get the accelerator at the specified resource path
     * @param resourcePath fully qualified modified package path to the resource (see the notes for the getResource() method of java.lang.Class)
     */
    static public Accelerator getAcceleratorForResource( final String resourcePath ) {
        final URL opticsURL = getResourceURL( resourcePath );
		return getAcceleratorAtURL( opticsURL );
    }


    /**
     * Load and get the accelerator at the specified resource path
     * @param opticsURL URL to the optics
     */
    static public Accelerator getAcceleratorAtURL( final URL opticsURL ) {
        return opticsURL != null ? XMLDataManager.getInstance( opticsURL ).getAccelerator() : null;
    }


	/** Get the fully qualified output file given the relative path within the output directory. */
	static public File getOutputFile( final String relativePath ) {
		final File testDirectory = getTestDirectory();
		if ( testDirectory == null ) {
			final String errorMessage = "Error getting the output file from ResourceManager. The test directory was null and must be specified using the runtime property, xal.tests.root or an environment variable of the same name.";
			System.err.println( errorMessage );
			throw new RuntimeException( errorMessage );
		}
		final File outputDirectory = new File( testDirectory, "output" );
		final File outputFile = new File( outputDirectory, relativePath );
		outputFile.getParentFile().mkdirs();
		//System.out.println( "Output File: " + outputFile.getAbsolutePath() );
		return outputFile;
	}


	/** get the test directory path */
	static private File getTestDirectory() {
		final String TEST_DIRECTORY_PROPERTY = "xal.tests.root";
		final String testDirectoryPropertyPath = System.getProperty( TEST_DIRECTORY_PROPERTY );

		// first check system properties and if it exists then use it's value
		if ( testDirectoryPropertyPath != null ) {
			//System.out.println( "Getting the test directory from the property." );
			return new File( testDirectoryPropertyPath );
		}
		else {		// check for an environment variable of the same name
			final String testDirectoryEnvironmentPath = System.getenv( TEST_DIRECTORY_PROPERTY );
			if ( testDirectoryEnvironmentPath != null ) {
				//System.out.println( "Getting the test directory from the environment variable." );
				return new File( testDirectoryEnvironmentPath );
			}
			else {	// check if the xal home path is specified and if so build the tests path relative to it
				//System.out.println( "Getting the test directory from the home path." );
				final String homePath = xal.tools.ResourceManager.getProjectHomePath();
				if ( homePath != null ) {
					final File buildDir = new File( homePath, "build" );
					return new File( buildDir, "tests" );
				}
				else {
					return null;
				}
			}
		}
	}
}
