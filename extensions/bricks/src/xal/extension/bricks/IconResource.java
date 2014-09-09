//
//  IconResource.java
//  xal
//
//  Created by Thomas Pelaia on 9/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import javax.swing.ImageIcon;
import java.net.URL;

import xal.tools.IconLib;


/** Icon identified by a URL */
public class IconResource extends ImageIcon {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	/** URL of the image for the icon */
	final private URL IMAGE_URL;
	
	/** group of icons */
	final protected String GROUP;
	
	/** icon name */
	final protected String ICON_NAME;
	
	
	/** Constructor */
	private IconResource( final URL imageURL, final String group, final String iconName ) {
		super( imageURL );
		
		IMAGE_URL = imageURL;
		GROUP = group;
		ICON_NAME = iconName;
	}
	
	
	/** create a new instance */
	static IconResource getInstance( final URL contextURL, final String group, final String iconName ) {
		try {
			final URL imageURL = group != null && !group.isEmpty() ? IconLib.getIconURL( group, iconName ) : new URL( contextURL, iconName );
			return new IconResource( imageURL, group, iconName );
		}
		catch ( Exception exception ) {
			exception.printStackTrace();
			return null;
		}
	}
	
	
	/** 
	 * Get the icon's URL 
	 * @return the icon's URL
	 */
	public URL getURL() {
		return IMAGE_URL;
	}
	
	
	/**
	 * Get the icon's group
	 * @return the icon's group
	 */
	public String getGroup() {
		return GROUP;
	}
	
	
	/**
	 * Get the icon's name
	 * @return the icon's name
	 */
	public String getIconName() {
		return ICON_NAME;
	}
	
	
	/**
	 * Description of this icon
	 * @return the description of this icon
	 */
	public String toString() {
		return "group:  "  + GROUP + ", icon name:  " + ICON_NAME;
	}
}
