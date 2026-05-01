/**
 * ResourceManager2.java
 *
 *  Created	: Jun 23, 2010
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.rscmgt;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.prefs.Preferences;

import javax.swing.ImageIcon;

import xal.app.pta.MainApplication;
import xal.tools.IconLib;
import xal.tools.ResourceManager;

/**
/**
 * <h4>PtaResourceManager</h4>
 * <p>
 * Utility class for managing the resources of the application.
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 23, 2010
 * @author Christopher K. Allen
 */
public class PtaResourceManager {
    
    
//    /*
//     * Debugging
//     */
//    
//    static public void main(String[] arrArgs) {
//        URL     urlRsc  = getResourceUrl("About.properties");
//        
//        
//        try {
//            InputStream     isRscLoc = openResource("Commands.ini");
//            ByteBuffer      bufBytes = ByteBuffer.allocate(100);
//            isRscLoc.read(bufBytes.array());
//            
//            String          strRscFile = new String( bufBytes.array() );
//            isRscLoc.close();
//            System.out.print(strRscFile);
//            
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        
//        try {
//            CharBuffer          bufCat = CharBuffer.allocate(1000);
//
//            URL                 urlCat = PtaResourceManager.class.getResource("../resources/About.properties");
//            InputStream         isCat  = urlCat.openStream();
//            InputStreamReader   isrCat = new InputStreamReader(isCat);
//            isrCat.read(bufCat);
//            String              strCat = bufCat.toString();
//            
//            isrCat.close();
//        
//        } catch (IOException e) {
//            e.printStackTrace();
//            
//        }
//        
//        try {
//            URL         urlAppCfg = PtaResourceManager.class.getResource("../resources/AppConfiguration.properties");
//            InputStream isAppCfg  = urlAppCfg.openStream();
//            Properties  prpAppCfg = new Properties();
//            prpAppCfg.load(isAppCfg);
//
//            isAppCfg.close();
//            
//            
//        } catch (IOException e) {
//            e.printStackTrace();
//            
//        }
//        
//    }
//    
    
    

    /*
     * Global Constants
     */

    
    
    /** The singleton preferences object */
    private static final Preferences            PREFS_CONFIG;
    
    
    
    /** Name of the resources directory relative to the location of <code>MainApplication</code> */
//    public static String                        STR_DIR_RESOURCES = "resources/";
//    public static String						STR_DIR_RESOURCES = System.getenv("OPENXAL_HOME") + "/apps/pta/resources/";
//    public static String                        STR_DIR_RESOURCES = ResourceManager.getResourceURL(MainApplication.class, "resources").toString();
    
    

    /*
     * Global Operations
     */
    

    /**
     * Load the singleton class objects.
     */
    static {
        PREFS_CONFIG = Preferences.userNodeForPackage( PtaResourceManager.class );
    }
    
    
    /**
     * <p>
     * Returns the <code>Preferences</code> object associated with 
     * the application class.  These are user-specific preferences
     * for the application.
     * </p>
     *
     * @return  user preferences for the application
     * 
     * @since  Jun 11, 2009
     * @author Christopher K. Allen
     */
    public static Preferences  getPreferences()      {
        return PREFS_CONFIG;
    }
    

    
    
    /*
     * Resource Locations
     */
    
    /**
     * <p>
     * Return the URL for the local resource name.
     * <p>
     * Provides the URL for the named resource.  The  
     * name is given with respect to the application 
     * resource folder location.  Specifically, if the
     * given resource has name "strLocalName.txt" then the 
     * returned value is
     * <br/>
     * <br/>
     * &emsp;  <code>file://../../../resources/strLocalName.txt</code>
     * </p>
     *
     * @param strLocalName      local name of the resource
     * 
     * @return  <code>URL</code> of the local resource, or null if not present
     * 
     * @since  Jun 15, 2009
     * @author Christopher K. Allen
     */
    public static URL getResourceUrl(String strLocalName)       {
        URL     urlLocalName = ResourceManager.getResourceURL(MainApplication.class, strLocalName);

        return urlLocalName;
        
//        String  strPathRel   = urlLocalName.toString();
//        
//        File file = new File(strPathRel);
//        URI uri = file.toURI();
//        URL url = null;
//		try {
//			url = uri.toURL();
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//        
//        //URL     urlResrc   = MainApplication.class.getResource(strPathRel);
//                
//        return url;
    }


    /**
     * Opens the named resource and connects an input stream 
     * to it. 
     *
     * @param strRscName  the name of the file with respect 
     *                    to the resources directory
     * 
     * @return            and input stream connected to the given resource
     * 
     * @throws IOException      unable to find the resource
     * 
     * @since  Nov 12, 2009
     * @author Christopher K. Allen
     */
    public static InputStream   openResource(String strRscName) throws IOException {
        URL             urlSrc  = PtaResourceManager.getResourceUrl(strRscName);
        InputStream     isPath  = urlSrc.openStream();
        
        return isPath;
    }
    


    
    /*
     * Resource Objects
     */
    
    /**
     * <p>
     * Return a <code>Properties</code> object initialized
     * according to the property file name.  That is, we
     * assume that the argument is the file name of a
     * property map.  Such files have line-by-line formats
     * given by
     * <br/>
     * <br/>
     * &emsp;  <tt>key</tt> = <tt>value</tt>
     * <br/>
     * <br/>
     * where both <tt>key</tt> and <tt>value</tt> are ASCII
     * character strings.
     * </p>
     *
     * @param strMapFile        file name of property map
     * 
     * @return  a <code>Properties</code> object representing property file
     * 
     * @throws IOException      file is missing or corrupt (i.e., not a property map)
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    public static Properties  getProperties(String strMapFile) throws IOException {

        // Open an input stream connected to the file
        InputStream is  = openResource(strMapFile);

        // Create the map, load the property values, and close the stream
        Properties  map = new Properties();
        map.load(is);
        is.close();
        
        return map;
    }
    

    /**
     * <p>
     * Returns the icon created from the image with the given
     * resource name.  
     * </p> 
     * <p>
     * We first check if the indicated icon is part of the
     * <code>IconLib</code> suite of icons.  If so, the 
     * argument is formatted as follows:
     * <br/>
     * <br/>
     * &nbsp;  <code>strRscName</code> = 
     *         "<tt>IconLib:<i>group</i>:<i>image_file</i></tt>"
     * <br/>
     * <br/>
     * where <code><i>group</i></code> is the group name
     * (i.e., the sub-directory) in the icon library suite 
     * and <code><i>image_file</i></code> is the specific file
     * name of the image.  
     * </p>
     * <p>
     * If the icon is not in the <code>IconLib</code> suite,
     * then it should be the file name within the application's
     * resource directory.
     * </p>
     *
     * @param strRscName      name of the icon or image file name
     * 
     * @return  icon created from given image file, 
     *          or <code>null</code> if failure
     * 
     * @since  Jun 16, 2009
     * @author Christopher K. Allen
     */
    public static ImageIcon     getImageIcon(String strRscName) {
        
        if (strRscName == null) 
            return null;
            
        // The image file is part of the XAL framework
        if (strRscName.startsWith("IconLib")) {
            String[]    arrTokens = strRscName.split(":");

            ImageIcon icon = (ImageIcon) IconLib.getIcon(arrTokens[1], arrTokens[2]);
            
            return icon;
        }
        
        // The image file is local to the application
        URL       urlIcon  = PtaResourceManager.getResourceUrl(strRscName);
        ImageIcon icnImage = new ImageIcon(urlIcon);

        return icnImage;
    }


    
    /*
     * Support Methods
     */
    
    
    /**
     * Utility class - prevent any instances of
     * <code>PtaResourceManager</code> objects.
     *
     *
     * @since     Jun 16, 2009
     * @author    Christopher K. Allen
     */
    private PtaResourceManager()      {};
}
