//
//  ResourceManager.java
//  xal
//
//  Created by Thomas Pelaia on 5/23/2014.
//  Copyright 2014 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.regex.*;


/** Provide normalized methods for getting resources */
abstract public class ResourceManager {
	static final protected String RESOURCES_FILE_SEARCH_PROPERTY = "OPENXAL_FIND_RESOURCES_IN_ROOT";

	/** default resource manager */
	static final private ResourceManager DEFAULT_MANAGER;


	/** static initializer */
	static {
		DEFAULT_MANAGER = useFileResourceManager() ? getFileResourceManager() : getJarredResourceManager();
	}


	/** determine whether to use the file resource manager by first looking at the OPENXAL_FIND_RESOURCES_IN_HOME property and then the corresponding environment variable if necessary */
	static private boolean useFileResourceManager() {
		// the property set to true indicates whether to find resources under the OPENXAL_HOME directory instead of the jar files
		final boolean hasProperty = System.getProperty( RESOURCES_FILE_SEARCH_PROPERTY ) != null;

		// first check system properties and if it exists then use it's value
		if ( hasProperty ) {
			return Boolean.getBoolean( RESOURCES_FILE_SEARCH_PROPERTY );
		}
		else {		// check for an environment variable of the same name
			final String environment = System.getenv( RESOURCES_FILE_SEARCH_PROPERTY );
			return Boolean.parseBoolean( environment );
		}
	}


	/** get the singleton instance of the jarred resource manager */
	static private FileResourceManager getFileResourceManager() {
		return FileResourceManager.getInstance();
	}


	/** get the singleton instance of the jarred resource manager */
	static private JarredResourceManager getJarredResourceManager() {
		return JarredResourceManager.getInstance();
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
		final URL resourceURL = DEFAULT_MANAGER.fetchResourceURL( rootClass, resourcePath );
		//System.out.println( "Resource URL: " + resourceURL + " for resource: " + resourcePath + " relative to package: " + rootClass.getPackage().getName() );
		return resourceURL;
	}
}



/** Resource manager that loads resources from jar files  */
class JarredResourceManager extends ResourceManager {
	/** singleton instance */
	final static private JarredResourceManager RESOURCE_MANAGER;


	// static initializer
	static {
		RESOURCE_MANAGER = new JarredResourceManager();
	}


	/** get the singleton instance */
	static public JarredResourceManager getInstance() {
		return RESOURCE_MANAGER;
	}


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
		final PackagePartition packagePartition = PackagePartition.getValidInstance( rootClass );

		if ( packagePartition != null ) {
			final String containerType = packagePartition.COMPONENT_TYPE;	// e.g. app, extension, plugin, service
			final String container = packagePartition.COMPONENT_NAME;		// e.g. application, widgets, pvlogger, scan1d, launcher

			final StringBuilder pathBuilder = new StringBuilder( "/" + packagePartition.PACKAGE_PREFIX + "/" );		// e.g. "/xal/"
			pathBuilder.append( containerType );
			pathBuilder.append( "/" + container + "/resources" );

			final String packageSuffix = packagePartition.PACKAGE_SUFFIX;
			if ( packageSuffix != null && packageSuffix.length() > 0 ) {
				final String suffixPath = packageSuffix.replaceAll( "\\.", "/" );
				pathBuilder.append( "/" + suffixPath );
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



/** Resource manager that loads resources from file system  */
class FileResourceManager extends ResourceManager {
	/** root location of the project */
	final static private String PROJECT_HOME_PROPERTY = "OPENXAL_HOME";

	/** singleton instance */
	final static private FileResourceManager RESOURCE_MANAGER;

	/** root relative to which the resources will be found */
	final private File ROOT_FILE;


	// static initializer
	static {
		RESOURCE_MANAGER = createInstance();
	}


	/** get the singleton instance */
	static public FileResourceManager getInstance() {
		return RESOURCE_MANAGER;
	}


	/** create an instance using the system properties */
	static private FileResourceManager createInstance() {
		final String home = getProjectHomePath();
		if ( home != null ) {
			return new FileResourceManager( home );
		}
		else {
			throw new RuntimeException( RESOURCES_FILE_SEARCH_PROPERTY + " property set to true, but " +  PROJECT_HOME_PROPERTY + " is not set, so cannot initialize ResourceManager." );
		}
	}


	/** Constructor */
	public FileResourceManager( final String rootPath ) {
		ROOT_FILE = new File( rootPath );
	}


	/** Constructor */
	public FileResourceManager( final File rootFile ) {
		ROOT_FILE = rootFile;
	}


	/** get the path to the project home based on the OPENXAL_HOME property or corresponding environment variable if necessary */
	static private String getProjectHomePath() {
		// the property set to true indicates whether to find resources under the OPENXAL_HOME directory instead of the jar files
		final String path = System.getProperty( PROJECT_HOME_PROPERTY );

		return path != null ? path : System.getenv( PROJECT_HOME_PROPERTY );
	}


	/**
	 * Get the URL to the specified resource relative to the specified class
	 * @param rootClass class at the root of the group (this class must be at the same location as the resources directory in the jar file)
	 * @param path to the resource relative to the group's resources directory
	 */
	public URL fetchResourceURL( final Class<?> rootClass, final String resourcePath ) {
		// first look for the resource in core
		final URL coreResourceURL = fetchCoreResourceURL( rootClass, resourcePath );
		if ( coreResourceURL != null ) {
			return coreResourceURL;
		}
		else {
			// look for the resource based on the package's container (e.g. extension, app, service, etc.)
			final URL containerResourceURL = fetchContainerResourceURL( rootClass, false, resourcePath );
			if ( containerResourceURL != null ) {
				return containerResourceURL;
			}
			else {
				// sometimes resources for apps and services are associated with a corresponding app/service specific extension (e.g. services/pvlogger/extension/resources/configuraiton.xml)
				return fetchContainerResourceURL( rootClass, true, resourcePath );
			}
		}
	}


	/** Look relative to the class (applies to core) */
	private URL fetchCoreResourceURL( final Class<?> rootClass, final String resourcePath ) {
		try {
			// first try to find a site specific resource
			final File siteCoreResource = fetchCoreResourceFile( rootClass, "site", resourcePath );
			if ( siteCoreResource.exists() ) {
				return siteCoreResource.toURI().toURL();
			}
			else {		// next try to find the resource in the common component
				final File coreResource = fetchCoreResourceFile( rootClass, null, resourcePath );
				if ( coreResource.exists() ) {
					return coreResource.toURI().toURL();
				}
				else {
					return null;
				}
			}
		}
		catch( MalformedURLException exception ) {
			throw new RuntimeException( "Malformed URL when fetching resource URL from file.", exception );
		}
	}


	/** Look relative to the class (applies to core) */
	private File fetchCoreResourceFile( final Class<?> rootClass, final String prefix, final String resourcePath ) {
		final File baseFile = prefix != null ? new File( ROOT_FILE, prefix ) : ROOT_FILE;
		final File coreDirectory = new File( baseFile, "core" );
		final File resourcesDirectory = new File( coreDirectory, "resources" );

		// replace package dot delimiter with URL slash delimiter (should work on all platforms if we use URLs here instead of files)
		final String packagePath = rootClass.getPackage().getName().replaceAll( "\\.", "/" );
		final String pathFromResources = packagePath + "/" + resourcePath;

		// use URLs to avoid file system path separator dependencies
		try {
			final URL resourcesURL = resourcesDirectory.toURI().toURL();
			final URL resourceURL = new URL( resourcesURL, pathFromResources );

			return new File( resourceURL.toURI() );
		}
		catch( MalformedURLException exception ) {
			throw new RuntimeException( "Malformed URL when fetching resource URL from file.", exception );
		}
		catch( URISyntaxException exception ) {
			throw new RuntimeException( "URI syntax exception when fetching resource URL from file.", exception );
		}
	}


	/** Look in the container's corresponding resources directory */
	public URL fetchContainerResourceURL( final Class<?> rootClass, final boolean includeExtension,  final String resourcePath ) {
		try {
			// first try to find a site specific resource
			final File siteContainerResource = fetchContainerResourceFile( rootClass, "site", includeExtension, resourcePath );
			if ( siteContainerResource != null && siteContainerResource.exists() ) {
				return siteContainerResource.toURI().toURL();
			}
			else {		// next try to find the resource in the common component
				final File containerResource = fetchContainerResourceFile( rootClass, null, includeExtension, resourcePath );
				if ( containerResource != null && containerResource.exists() ) {
					return containerResource.toURI().toURL();
				}
				else {
					return null;
				}
			}
		}
		catch( MalformedURLException exception ) {
			throw new RuntimeException( "Malformed URL when fetching container resource URL from file.", exception );
		}
	}


	/** Look in the container's corresponding resources directory */
	public File fetchContainerResourceFile( final Class<?> rootClass, final String prefix, final boolean includeExtension, final String resourcePath ) {
		final PackagePartition packagePartition = PackagePartition.getValidInstance( rootClass );

		if ( packagePartition != null ) {
			final String containerType = packagePartition.COMPONENT_TYPE;	// e.g. app, extension, plugin, service
			final String container = packagePartition.COMPONENT_NAME;		// e.g. application, widgets, pvlogger, scan1d, launcher

			final File baseDirectory = prefix != null ? new File( ROOT_FILE, prefix ) : ROOT_FILE;	// e.g. ${OPENXAL_HOME} or ${OPENXAL_HOME}/site
			if ( !baseDirectory.exists() )  return null;

			final File containerTypeRoot = new File( baseDirectory, containerType + "s" );		// e.g. ${OPENXAL_HOME}/extensions
			if ( !containerTypeRoot.exists() )  return null;

			final File containerDirectory = new File( containerTypeRoot, container );			// e.g. ${OPENXAL_HOME}/extensions/application
			if ( !containerDirectory.exists() )  return null;

			final File resourcesParent = includeExtension ? new File( containerDirectory, "extension" ) : containerDirectory;		// e.g. ${OPENXAL_HOME}/site/services/pvlogger/extension
			if ( !resourcesParent.exists() )  return null;

			final File resourcesDirectory = new File( resourcesParent, "resources" );		// e.g. ${OPENXAL_HOME}/extensions/application/resources
			if ( !resourcesDirectory.exists() )  return null;

			// replace package dot delimiter with URL slash delimiter (should work on all platforms if we use URLs here instead of files)
			final String packageSuffix = packagePartition.PACKAGE_SUFFIX;
			final String relativePackagePath = packageSuffix != null ? packageSuffix.replaceAll( "\\.", "/" ) : null;		// e.g. smf  (replacing dots with /)
			final String pathFromResources = relativePackagePath != null ? relativePackagePath + "/" + resourcePath : resourcePath;		// e.g. smf/menudef.properties

			// use URLs to avoid file system path separator dependencies
			try {
				final URL resourcesURL = resourcesDirectory.toURI().toURL();
				final URL resourceURL = new URL( resourcesURL, pathFromResources );		// e.g. file://${OPENXAL_HOME}/extensions/application/resources/smf/menudef.properties

				return new File( resourceURL.toURI() );		// e.g. ${OPENXAL_HOME}/extensions/application/resources/smf/menudef.properties
			}
			catch( MalformedURLException exception ) {
				throw new RuntimeException( "Malformed URL when fetching resource URL from file.", exception );
			}
			catch( URISyntaxException exception ) {
				throw new RuntimeException( "URI syntax exception when fetching resource URL from file.", exception );
			}
		}
		else {
			return null;
		}
	}
}



/** package parsed into parts */
class PackagePartition {
	/** pattern to match an XAL package name */
	static final protected Pattern XAL_PACKAGE_PATTERN = Pattern.compile( "^(\\w+)\\.(\\w+)\\.(\\w+)(\\..+)?$" );

	/** package name */
	final public String PACKAGE_NAME;

	/** prefix to the package */
	final public String PACKAGE_PREFIX;		// e.g. xal

	/** component type */
	final public String COMPONENT_TYPE;		// e.g. app, extension, plugin, service

	/** name of the component */
	final public String COMPONENT_NAME;		// e.g. application, widgets, pvlogger, scan1d, launcher

	/** package suffix */
	final public String PACKAGE_SUFFIX;		// e.g. smf in xal.extension.application.smf


	/** Constructor */
	public PackagePartition( final Class<?> rootClass ) {
		PACKAGE_NAME = rootClass.getPackage().getName();

		final Matcher packageMatcher = XAL_PACKAGE_PATTERN.matcher( PACKAGE_NAME );
		final int groupCount = packageMatcher.groupCount();

		final String[] parts = new String[groupCount];

		if ( packageMatcher.matches() ) {
			PACKAGE_PREFIX = groupCount > 0 ? packageMatcher.group( 1 ) : null;		// e.g. xal

			COMPONENT_TYPE = groupCount > 1 ? packageMatcher.group( 2 ) : null;		// e.g. extension

			COMPONENT_NAME = groupCount > 2 ? packageMatcher.group( 3 ) : null;		// e.g. application

			// last package substring stripping the leading "."
			if ( groupCount > 3 ) {
				final String rawSuffix = packageMatcher.group( 4 );
				PACKAGE_SUFFIX = rawSuffix != null ? rawSuffix.substring(1) : null;		// e.g. smf
			}
			else {
				PACKAGE_SUFFIX = null;
			}
		}
		else {
			PACKAGE_PREFIX = null;
			COMPONENT_TYPE = null;
			COMPONENT_NAME = null;
			PACKAGE_SUFFIX = null;
		}
	}


	/** get an instance of the package partition if it has at least the three required parts (everything but suffix) or null if not */
	static public PackagePartition getValidInstance( final Class<?> rootClass ) {
		final PackagePartition partition = new PackagePartition( rootClass );
		return partition.COMPONENT_NAME != null ? partition : null;		// if it has the component name it has prefix, type and name and thus is valid
	}
}
