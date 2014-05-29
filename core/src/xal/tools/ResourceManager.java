//
//  ResourceManager.java
//  xal
//
//  Created by Thomas Pelaia on 5/23/2014.
//  Copyright 2014 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.net.URL;
import java.util.regex.*;


/** Provide normalized methods for getting resources */
abstract public class ResourceManager {
	/** pattern to match an XAL package name */
	static final Pattern XAL_PACKAGE_PATTERN = Pattern.compile( "^xal\\.(\\w+)\\.(\\w+)(\\..*)?$" );

	/** default resource manager */
	static final ResourceManager DEFAULT_MANAGER;


	/** static initializer */
	static {
		DEFAULT_MANAGER = new JarredResourceManager();
	}


	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	abstract public URL fetchResourceURL( final Class<?> rootClass, final String resourcePath );


	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	static public URL getResourceURL( final Class<?> rootClass, final String resourcePath ) {
		return DEFAULT_MANAGER.fetchResourceURL( rootClass, resourcePath );
	}
}



/** Resource manager that loads resources from jar files  */
class JarredResourceManager extends ResourceManager {
	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	public URL fetchResourceURL( final Class<?> rootClass, final String resourcePath ) {
		final URL directResourceURL = fetchDirectResourceURL( rootClass, resourcePath );
		return directResourceURL != null ? directResourceURL : fetchContainerResourceURL( rootClass, resourcePath );
	}


	/** Look relative to the class (applies to core) */
	private URL fetchDirectResourceURL( final Class<?> rootClass, final String resourcePath ) {
		return rootClass.getResource( resourcePath );
	}


	/** Look in the container's corresponding resources directory */
	public URL fetchContainerResourceURL( final Class<?> rootClass, final String resourcePath ) {
		final String packageName = rootClass.getPackage().getName();
		final Matcher packageMatcher = XAL_PACKAGE_PATTERN.matcher( packageName );
		final int groupCount = packageMatcher.groupCount();

		if ( packageMatcher.matches() && groupCount >= 2 ) {
			final String containerType = packageMatcher.group(1);	// e.g. extension, plugin, app, service
			final String container = packageMatcher.group( 2 );		// e.g. application, widgets, pvlogger, scan1d, launcher

			final StringBuilder pathBuilder = new StringBuilder( "/xal/" );
			pathBuilder.append( containerType );
			pathBuilder.append( "/" + container + "/resources" );

			if ( groupCount == 3 ) {
				final String packageSuffix = packageMatcher.group( 3 );
				if ( packageSuffix != null && packageSuffix.length() > 0 ) {
					final String suffixPath = packageSuffix.replaceAll( "\\.", "/" );
					pathBuilder.append( suffixPath );
				}
			}

			pathBuilder.append( "/" + resourcePath );

			final String path = pathBuilder.toString();
//			System.out.println( "Fetching container resource with path: " + path );
			return rootClass.getResource( path );
		}
		else {
			return null;
		}
	}
}
