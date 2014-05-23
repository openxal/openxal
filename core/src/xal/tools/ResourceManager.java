//
//  ResourceManager.java
//  xal
//
//  Created by Thomas Pelaia on 5/23/2014.
//  Copyright 2014 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.net.URL;


/** Provide normalized methods for getting resources */
public class ResourceManager {
	/** pattern to match an XAL package name */
	static final Pattern XAL_PACKAGE_PATTERN = Pattern.compile( "$xal\\.(\\w+)\\.(\\w+)(\\..*)?" );


	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	static public URL getResourceURL( final Class<T> rootClass, final String resourcePath ) {
		final String packageName = rootClass.getPackage().toString();
		final Matcher packageMatcher = XAL_PACKAGE_PATTERN.matcher( packageName );

		if ( packageMatcher.matches() && packageMatcher.groupCount() >= 2 ) {
			final String path = "/xal/" + packageMatcher.group(1) + "/" + packageMatcher.group( 2 ) + "/resources/" + resourcePath;
			return rootClass.getClassLoader().getResource( path );
		}
		else {
			return null;
		}
	}
}
