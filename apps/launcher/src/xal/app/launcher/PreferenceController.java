/*
 * ConnectionPreferenceController.java
 *
 * Created on Tue March 9 16:37:55 EST 2004
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.launcher;

import xal.tools.apputils.PathPreferenceSelector;

import java.util.prefs.*;
import java.net.*;


/**
 * PreferenceController is a convenience class for displaying a PathPreferenceSelector for 
 * the default document to open when the application launches.
 *
 * @author  tap
 */
public class PreferenceController {
	/** The key for the default document URL preference */
	final static private String DOCUMENT_KEY;
	
	/** User preferences for the Main class */
	final static private Preferences DEFAULTS;
	
	/** The suffix of the document for filtering the default document open browser */
	final static private String SUFFIX = ".launch";
	
	/** Description to appear in the default document open browser */
	final static private String DESCRIPTION = "Default launcher document";
	
	
	/**
	 * Static initializer
	 */
	static {
		DOCUMENT_KEY = "DEFAULT_DOCUMENT";
		DEFAULTS = getDefaults();
	}
	
	
	/**
	 * Constructor which is hidden since this class only has static methods.
	 */
	protected PreferenceController() {}
	
	
	/**
	 * Get the user preferences for this class
	 * @return the user preferences for this class
	 */
	static protected Preferences getDefaults() {
		return xal.tools.apputils.Preferences.nodeForPackage(Main.class);
	}
	
	
	/**
	 * Get the URL of the default connection dictionary properties file
	 * @return the URL of the default connection dictionary properties file
	 * @throws java.net.MalformedURLException if the default URL spec cannot form a valid URL
	 */
	static public URL getDefaultDocumentURL() throws MalformedURLException {
		String urlSpec = getDefaultDocumentURLSpec();
		
		if ( urlSpec == null || urlSpec.isEmpty() ) {
			return null;
		}
		else {
			return new URL(urlSpec);
		}
	}
	
	
	/**
	 * Get the URL Spec of the default launcher document
	 * @return the URL Spec of the default launcher document
	 */
	static public String getDefaultDocumentURLSpec() {
		return getDefaults().get(DOCUMENT_KEY, "");
	}
	
	
	/** Set the URL Spec of the default launcher document */
	static public void setDefaultDocumentURLSpec( final String urlSpec ) {
		getDefaults().put( DOCUMENT_KEY, urlSpec );
	}
	
	
	/**
	 * Display the PathPreferenceSelector with the specified Frame as the owner.
	 * @param owner The owner of the PathPreferenceSelector dialog.
	 */
	static public void displayPathPreferenceSelector(java.awt.Frame owner) {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( owner, DEFAULTS, DOCUMENT_KEY, SUFFIX, DESCRIPTION );
		selector.setLocationRelativeTo( owner );
		selector.setVisible( true );
	}
	
	
	/**
	 * Display the PathPreferenceSelector with the specified Dialog as the owner.
	 * @param owner The owner of the PathPreferenceSelector dialog.
	 */
	static public void displayPathPreferenceSelector(java.awt.Dialog owner) {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( owner, DEFAULTS, DOCUMENT_KEY, SUFFIX, DESCRIPTION );
		selector.setLocationRelativeTo(owner);
		selector.setVisible( true );
	}
	
	
	/**
	 * Display the PathPreferenceSelector with no owner.
	 */
	static public void displayPathPreferenceSelector() {
		final PathPreferenceSelector selector;
		selector = new PathPreferenceSelector( DEFAULTS, DOCUMENT_KEY, SUFFIX, DESCRIPTION );
		selector.setVisible( true );
	}
}

