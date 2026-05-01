//
//  IconLib.java
//  xal
//
//  Created by Thomas Pelaia on 3/1/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.tools;

import java.net.URL;
import javax.swing.Icon;
import javax.swing.ImageIcon;


/** Fetch icons by name */
public class IconLib {
	/** Enum for groups of icons in the library */
	public enum IconGroup {
		CUSTOM		( "custom" ),
		DEVELOPMENT	( "development" ), 
		GENERAL		( "general" ), 
		MEDIA		( "media" ), 
		NAVIGATION	( "navigation" ),
		TABLE		( "table" ),
		TEXT		( "text" );
		
		/** name of this group */
		private final String _name;
		
		/** 
		 * Constructor 
		 * @param name the name to assign the group
		 */
		IconGroup( final String name ) {
			_name = name;
		}
		
		/** get the name of the group */
		public String getName() {
			return _name;
		}
			
		/** Override the string representation to get the name of the group */
        public String toString() {
			return getName();
		}
	}
	
	
    /**
	 * Get the path to the specified named icon which resides in the library.
	 * @param group the group of icon, i.e. one of DEVELOPMENT, GENERAL, MEDIA, NAVIGATION, TABLE, TEXT
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The path to the specified icon in classpath notation
     */
    static protected String getPathToIcon( final IconGroup group, final String iconName ) {
		return getPathToIcon( group.toString(), iconName );
    }
	
	
	/**
	 * Get the URL to the specified Icon.
	 * @param group the group of icon, i.e. one of DEVELOPMENT, GENERAL, MEDIA, NAVIGATION, TABLE, TEXT
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The URL to the specified icon
	 */
	static public URL getIconURL( final IconGroup group, final String iconName ) {
		final String path = getPathToIcon( group, iconName );
		return ResourceManager.getResourceURL( IconLib.class, path );
	}
	
	
	/**
	 * Get the URL to the specified Icon.
	 * @param group the group of icon, i.e. one of DEVELOPMENT, GENERAL, MEDIA, NAVIGATION, TABLE, TEXT
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The URL to the specified icon
	 */
	static public Icon getIcon( final IconGroup group, final String iconName ) {
		return new ImageIcon( getIconURL( group, iconName ) );
	}
	
    
    /**
	 * Get the path to the specified named icon which resides in the library.
	 * @param group the group of icon, i.e. one of "development", "general", "media", "navigation", "table", "text"
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The path to the specified icon in classpath notation
     */
    static protected String getPathToIcon( final String group, final String iconName ) {
        return "icons/" + group + "/" + iconName;
    }
	
	
	/**
	 * Get the URL to the specified Icon.
	 * @param group the group of icon, i.e. one of "development", "general", "media", "navigation", "table", "text"
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The URL to the specified icon
	 */
	static public URL getIconURL( final String group, final String iconName ) {
		final String path = getPathToIcon( group, iconName );
		return ResourceManager.getResourceURL( IconLib.class, path );
	}
	
	
	/**
	 * Get the URL to the specified Icon.
	 * @param group the group of icon, i.e. one of "development", "general", "media", "navigation", "table", "text"
	 * @param iconName the name of the icon to fetch, e.g. "Cut24.gif"
     * @return The URL to the specified icon
	 */
	static public Icon getIcon( final String group, final String iconName ) {
		return new ImageIcon( getIconURL( group, iconName ) );
	}
}
