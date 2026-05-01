/**
 * PropertiesManager.java
 * 
 * Created : Jun 24, 2010 Author : Christopher K. Allen
 */
package xal.app.pta.tools.property;

import xal.app.pta.MainApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

/**
 * <p>
 * Manages persistent properties associated with a given class
 * type.  These properties can, thus, be used to manage the look
 * and behavior of an application between launch and termination.
 * </p>
 * <p>
 * The properties are stored as (key,value) pairs in the 
 * Java <code>{@link Preferences}</code> tree of the 
 * class type given at creation.  Thus, the persistence mechanism
 * is handled through the <code>{@link Preferences}</code> mechanism.
 *  Default values of the properties can be provided by a
 * given Java <code>{@link Properties}</code> object at creation.  
 * (The default value of a property is used if no current value exists.) 
 * </p>
 * 
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 * 
 * @since Jun 24, 2010
 * 
 * @author Christopher K. Allen
 */
public class PropertiesManager {

    
    
    /*
     * Local Attributes
     */
    
    /** The <code>Preferences</code> object where the current property values are stored */
    private final Preferences           prfCurVal;
    
    /** The <code>Properties</code> object where the default property values are stored */
    private final Properties            prpDefVal;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * <p>
     * Creates a new <code>PropertiesManager</code> object.
     * </p>
     * <p>
     * The properties are stored as (key,value) pairs in the 
     * Java <code>{@link Preferences}</code> tree of the given
     * class.  Default values of the properties are provided by the
     * given Java <code>{@link Properties}</code> object.  (The
     * default value of a property is used if no current value exists.) 
     * </p>
     *
     * @param clsManaged        class to which the properties are associated
     * @param prpDefVal         set of default property used if no current value
     *
     * @since     Jul 8, 2010
     * @author    Christopher K. Allen
     */
    public PropertiesManager(Class<?> clsManaged, Properties prpDefVal) {
        this.prpDefVal = prpDefVal;
        this.prfCurVal = Preferences.userNodeForPackage(clsManaged);
    }
    
    
    /**
     * Sets the value of the given property to the new one
     * provided in the argument.
     *
     * @param prop      property to receive new value
     * @param strVal    new property value 
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public void setPropertyValue(Property prop, String strVal) {
        String  strName = prop.getName();
        
        this.prfCurVal.put(strName, strVal);
    }
    
    /**
     * Sets the value of the given object with property characteristics
     * (i.e., <code>{@link Property.IProperty}</code> the new one
     * provided in the argument.
     *
     * @param ifcProp   property object to receive new value
     * @param strVal    new property value 
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public void setPropertyValue(Property.IProperty ifcProp, String strVal) {
        Property        prop = ifcProp.getValue();
        
        this.setPropertyValue(prop, strVal);
    }
    
    
    /*
     * Operations
     */
    
    /**
     * <p>
     * Return the raw (string) value of the property with the
     * given name.
     * </p>
     *
     * @param   strPropName     application configuration property name
     * 
     * @return                  the raw value of the given property, 
     *                          or <code>null</code> if nonexistent
     * 
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     */
    public String getRawPropertyValue(String strPropName) {
        String          strPropDef = this.prpDefVal.getProperty(strPropName);
        String          strPropVal = this.prfCurVal.get(strPropName, strPropDef);
        
        return strPropVal;
    }

    /**
     * <p>
     * Return the value of the given application configuration
     * property as a 
     * <code>Property</code> object,
     * which can then be converted to the desired Java type.
     * <br/>
     * <h4>NOTE:</h4>
     * &middot; For all predefined application properties
     * use one of the enclosed property enumerations.
     * </p>
     *
     * @param   strPropName     application configuration property name
     * 
     * @return                  <code>Property</code> object encapsulating 
     *                          raw value of the given property, 
     * 
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     * 
     * @see     Property
     */
    public Property     getProperty(String strPropName) {
        Property       prpVal = new Property(strPropName, this);
        
        return prpVal;
    }
    
    /*
     * Properties Management
     */
    
    /**
     * Clears out all current property values. The properties table is
     * then reset to all the default property values, as provided by the 
     * <code>{@link Properties}</code> object at the 
     * construction of this <code>PropertiesManager</code>.
     *
     * 
     * @since  Jul 8, 2010
     * @author Christopher K. Allen
     */
    public void restoreDefaultValues() {

        try {
            this.prfCurVal.clear();
            
        } catch (BackingStoreException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Error during property erase");
            
        }

        for ( Map.Entry<Object, Object> entry : this.prpDefVal.entrySet() ) {
            
            try {
                String      strKey = (String)entry.getKey();
                String      strVal = (String)entry.getValue();
                
                this.prfCurVal.put(strKey, strVal);

            } catch (ClassCastException e) {
                MainApplication.getEventLogger().logException(getClass(), e, "Default property appears corrupt: " + entry);
                
            }
            
        }
    }
    
    /**
     * Explicitly saves the current property values to a file at 
     * the given URL.  The property values should naturally have
     * persistence, being normally kept in a system-dependent
     * backing store. However, this method can be used to create
     * a disk-file copy of the current set of property values.  That
     * property value set can then be restored at a later time using
     * the method <code>{@link #loadValues(File)}</code>.
     *
     * @param file      location of property value file to be created
     * 
     * @since  Jul 8, 2010
     * @author Christopher K. Allen
     * 
     * @see     PropertiesManager#loadValues(File)
     * @see     Preferences#exportSubtree(java.io.OutputStream)
     */
    public void storeValues(File file) {
        try {
//            File                file = new File(strUrl);
            FileOutputStream    os   = new FileOutputStream(file);
            
            this.prfCurVal.exportSubtree(os);
            
        } catch (FileNotFoundException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Unable to open file: " + file.getAbsolutePath());

        } catch (IOException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Unable to write to file: " + file.getAbsolutePath());
            
        } catch (BackingStoreException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Unknown file error - " + file.getAbsolutePath());
            
        }
    }
    
    /**
     * Loads the current property values from the given XML
     * file.  The XML file should be in the format appropriate
     * for the Java <code>{@link Preferences}</code> mechanism.
     *
     * @param file    file containing stored properties
     * 
     * @since  Jul 8, 2010
     * @author Christopher K. Allen
     * 
     * @see     PropertiesManager#storeValues(File)
     * @see     Preferences#importPreferences(java.io.InputStream)
     */
    public void loadValues(File file) {
        try {
//            File                file = new File(strUrl);
            FileInputStream     is   = new FileInputStream(file);
            
            Preferences.importPreferences(is);
            
        } catch (FileNotFoundException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Unable to open file: " + file.getAbsolutePath());

        } catch (IOException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Unable to write to file: " + file.getAbsolutePath());
            
        } catch (InvalidPreferencesFormatException e) {
            MainApplication.getEventLogger().logException(getClass(), e, "Invalid XML document type: " + file.getAbsolutePath());
            
        }
        
    }
    

}