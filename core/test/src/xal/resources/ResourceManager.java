//
// ResourceManager.java
// Open XAL 
//
// Created by Tom Pelaia on 9/13/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.resources;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.JFileChooser;

import xal.smf.Accelerator;
import xal.smf.data.XMLDataManager;


/** 
 * <p>
 * ResourceManager is not a test suite, but rather it is a utility used by 
 * test suites to load resources from the common test resources package. 
 * </p>
 * <p>
 * The first time the resource manager is used it asked the user for the location
 * of the resources directory.  Once this value is provided it is stored 
 * persistently in the <code>ResourceManager</code> preferences.  Currently
 * this value must be explicitly set to <code>null</code> in order to reset
 * the resource directory location.
 * </p>
 * <p>
 * There is a companion file <tt>Resources.properties</tt> which contains configurable
 * properties for use by the resource manager.  
 * </p>
 */
public class ResourceManager {
    
    /*
     * Global Attributes
     */
    
    /** The name of the default properties file */
    static final String         STR_PROPERTIES_FILE = "Resources.properties";
    
    /** the resource manager properties map */
    static final Properties     PROPS_RESOURCES = new Properties();

    
    
    /** The key for the resource location preference value */
    static final String         STR_KEY_RESDIR = "location";
    
    /** the resource manager preferences */
    static final Preferences    PREFS_RESOURCES = Preferences.userNodeForPackage(ResourceManager.class);
    

    
    
    /**
     * The class loader finds the resource properties file and initializes the
     * global class properties with the contents. 
     */
    static {

        
        try {
            URL         urlPropFile = ResourceManager.class.getResource(STR_PROPERTIES_FILE);
            InputStream istrProps = urlPropFile.openStream();
            
            PROPS_RESOURCES.load(istrProps);
            
        } catch (IOException e) {
            System.err.println("Unable to initialize the ResourceManager properties :" + e);
            e.printStackTrace();
        }
    }
    
    
    /*
     * Global Methods
     */
    
    /** 
     * Get the URL to the specified resource in the common test resources package.
     * 
     * @param strResPath full modified package path (see the notes for the 
     *                        getResource() method of java.lang.Class) relative to the resources location
     * 
     * @return                          URL to the resource or null if there is none found
     */
    static public URL getResourceURL( final String strResPath ) {
        String  strResDir = PREFS_RESOURCES.get(STR_KEY_RESDIR, null);

        // Ask the user for the resource location if it is not already set
        if (strResDir == null) {
            JFileChooser fcResDir = new JFileChooser();
            fcResDir.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fcResDir.setName("Set the location of the resources directory");
            
            int intResult = fcResDir.showDialog(null, "Resource Directory");
            if (intResult == JFileChooser.APPROVE_OPTION) {
                File    fileResDir = fcResDir.getSelectedFile();
                
                strResDir = fileResDir.getAbsolutePath();
                PREFS_RESOURCES.put(STR_KEY_RESDIR, strResDir);
                
            } else {
                
                return null;
            }
        }

        try {
            File        fileAbsPath = new File(strResDir, strResPath);
            URI         uriResource = fileAbsPath.toURI();
            URL         urlResource = uriResource.toURL();

            return urlResource;

        } catch (MalformedURLException e) {

            return null;
        }
    }


    /** Load and get the default test accelerator */
    static public URL getTestAcceleratorURL() {
        String  strAccelPath = PROPS_RESOURCES.getProperty("TestAccelPath");
        
        return getResourceURL( strAccelPath);
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
}
