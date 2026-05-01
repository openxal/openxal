/**
 * AppProperties.java
 *
 *  Created	: Jun 17, 2009
 *  Author      : Christopher K. Allen 
 */
package xal.app.pta.rscmgt;


import xal.app.pta.MainApplication;
import xal.app.pta.tools.property.PropertiesManager;
import xal.app.pta.tools.property.Property;
import xal.smf.scada.ScadaFieldDescriptor;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Properties;

/**
 * <h1>DeviceProperties</h1>
 * <p>
 * Manages the configuration properties for the application.  The properties
 * are keep in configuration properties file indicated by the constant
 * <code>{@link #STR_FILE_CONFIG}</code>.  The application configuration
 * attributes are available programmatically using the enumeration
 * <code>APP</code> which provides methods for type conversion.
 * </p>
 *
 * <p>
 * <b>Ported from XAL on Jul 17, 2014.</b><br>
 * &middot; Jonathan M. Freed
 * </p>
 *
 * @since  Jun 17, 2009
 * @author Christopher K. Allen
 * 
 * @see DeviceProperties#STR_FILE_CONFIG
 */
public class DeviceProperties extends PropertiesManager {

    
    /*
     * Global Constants
     */
    
    
    /**  The name of the default device configuration file */
    public static final String  STR_FILE_CONFIG = "DefaultDevice.properties";
    
    
    /** Suffix used for parameter label */
    public static final String STR_LBL = ".LABEL";
    
    /** Suffix used for the parameter type */
    public static final String STR_TYP = ".TYPE";
    
    /** Suffix used for maximum values */
    public static final String STR_MAX = ".MAX";
    
    /** Suffix used for minimum values */
    public static final String STR_MIN = ".MIN";
    
    /** Suffix used for initial values */
    public static final String STR_INI = ".INIT";
    
    /** Suffix used for values field */
    public static final String STR_VALS = ".VALS";
    
    /** Suffix used for increment values */
    public static final String STR_DEL = ".DEL";
    
    /** Suffix used for display formatting the numeric values */
    public static final String STR_FMT = ".FMT";
    
    /** Suffix used for the value of an error condition */
    public static final String STR_ERR = ".ERR";
    
    /** Suffix used for the value of the normal condition */
    public static final String STR_NML = ".NML";
    
    
    
    
    /*
     * Global Attributes
     */
    
    
    /** The singleton class instance of device properties */
    private static DeviceProperties          MGR_DEV_PROPS;
    
//    /** The application configuration properties map */
//    private static Properties     PROP_CONFIG;
//    
    
    
    /*
     * Class Initialization
     */

    /**
     *  Static block setting the configuration properties
     *  map upon class-loader invocation.
     *
     * @since  Jul 15, 2009
     * @author Christopher K. Allen
     */
    static {
        try {
            Properties  PROP_CONFIG = PtaResourceManager.getProperties(STR_FILE_CONFIG);
            
            MGR_DEV_PROPS = new DeviceProperties(PROP_CONFIG);
            
        } catch (IOException e) {
            
            String      strErrMsg   = "Device configuration mechanism corrupted. \n" +
                                      "See DeviceProperties class. \n";
            
            MainApplication.applicationLaunchFailure(strErrMsg, e);
        }
    }

    
    
    /*
     * Class Operations
     */
    
    
    /**
     * Provides access to the singleton application properties
     * manager.  
     *
     * @return  The single instance of the application properties manager 
     * 
     * @since  Jul 9, 2010
     * @author Christopher K. Allen
     */
    public static synchronized DeviceProperties getInstance() {
        return MGR_DEV_PROPS;
    }
    
    

    /*
     * Device Properties by Name
     */
    
    /**
     * <p>
     * Enumeration of the global configuration properties for the 
     * the device configuration.  These parameters are for the
     * display formatting of parameters.
     * </p>
     * 
     * @author Christopher K. Allen
     * @since Jun 11, 2009
     *
     */
    public enum NUMFMT {
        
        /** Display format for small integer values */
        SMALL_INT("NumberFormatSmallInteger"),
        
        /** Display format for large integer values */
        LARGE_INT("NumberFormatLargeInteger"),
        
        /** Display format for small double values */
        SMALL_DBL("NumberFormatSmallDouble"),
        
        /** Display format for large double values */
        LARGE_DBL("NumberFormatLargeDouble");
        
        

        /*
         * Operations
         */
        
        /**
         * Returns the formatter for the display of device
         * parameters with the given numeric type and
         * range.
         *
         * @return      formatter object for create string representation of 
         *              parameter values
         * 
         * @since  Jan 12, 2010
         * @author Christopher K. Allen
         */
        public NumberFormat     getFormat() {
            Property    prpFmtPtn = MGR_DEV_PROPS.getProperty(this.strFmtName);
            String      strFmtPtn = prpFmtPtn.asString();
            
            DecimalFormat fmt = new DecimalFormat(strFmtPtn);
            
            return fmt;
        }

        /*
         * Private Attributes
         */
        
        /** The (property) name of the format */
        private final String            strFmtName;
        
        /**
         * <p> 
         * Constructs the application properties enumeration 
         * initializes the properties to their values in the 
         * configuration file.
         * </p>
         * 
         * @param       strPropName     name of the property in the property file
         * 
         * @since  Jun 11, 2009
         * @author Christopher K. Allen
         * 
         * 
         * @see        DeviceProperties#STR_FILE_CONFIG
         */
        private NUMFMT(String strPropName) {
            this.strFmtName = strPropName;
        }
    }


    /**
     * Returns the {@link #STR_LBL} configuration property for the
     * given parameter enumeration.
     *
     * @param pfdKey  The enumeration value
     * 
     * @return          string label for the parameter represented by enumeration
     * 
     * @throws IllegalArgumentException could not find property in the configuration file
     * 
     * @since  Jan 12, 2010
     * @author Christopher K. Allen
     */
    public static String    getLabel(ScadaFieldDescriptor pfdKey) 
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_LBL;
        String          strKey = pfdKey.getRbHandle() + STR_LBL;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        
        return value.asString();
    }
    
    /**
     * Returns the class type ({@link #STR_TYP} configuration property
     * for the given parameter enumeration.
     *
     * @param pfdKey  The parameter enumeration value (i.e., parameter)
     * 
     * @return          Type of the parameter as specified in the configuration file
     * 
     * @throws IllegalArgumentException could not find property in the configuration file,
     *                                  or the class type specified was bad
     * 
     * @since  Jan 12, 2010
     * @author Christopher K. Allen
     */
    public static Class<?> getType(ScadaFieldDescriptor pfdKey)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_TYP;
        String          strKey = pfdKey.getRbHandle() + STR_TYP;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        try {
            String      strCls  = value.asString();
            Class<?>    clsType = Class.forName(strCls);

            return clsType;

        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Unknown class type " + value.asString());
        }
    }

    /**
     * <p>
     * Returns the lower limit of the given parameter.  
     * </p>
     * <p>
     * Note the parameter
     * is identified by its enumeration constant.  The key for
     * the returned value is formed from the dot-separated
     * concatenation
     * <br/>
     * <br/>
     * <tt>className</tt> + "." + <tt>enumName</tt> + "." + <tt>STR_MIN</tt>
     * <br/>
     * <br/>
     * where <tt>className</tt> is the formal class
     * name of the enumeration and <tt>enumName</tt>  is the
     * name of the enumeration constant.  This key is then used
     * to access the value in the configuration file 
     * (see {@link #STR_FILE_CONFIG})
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; This value is returned as a {@link Property} object
     * since we do not know the (numeric) type of the value.
     * </p>
     *
     * @param pfdParam  enumeration constant
     * 
     * @return  the minimum value of the parameter as a <code>PropertyValue</code> object
     * 
     * @throws IllegalArgumentException  the key was not found in the configuration file
     * 
     * @see     #STR_MIN
     * 
     * @since  Dec 21, 2009
     * @author Christopher K. Allen
     */
    public static Property      getMinLimit(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName()+ STR_MIN;
        String          strKey = pfdParam.getRbHandle() + STR_MIN;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        return value;
    }
    
    /**
     * <p>
     * Returns the upper limit of the given parameter.  
     * </p>
     * <p>
     * Note the parameter
     * is identified by its enumeration constant.  The key for
     * the returned value is formed from the dot-separated
     * concatenation
     * <br/>
     * <br/>
     * <tt>className</tt> + "." + <tt>enumName</tt> + "." + <tt>"MAX"</tt>
     * <br/>
     * <br/>
     * where <tt>className</tt> is the formal class
     * name of the enumeration and <tt>enumName</tt>  is the
     * name of the enumeration constant.  This key is then used
     * to access the value in the configuration file 
     * (see {@link #STR_FILE_CONFIG})
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; This value is returned as a {@link Property} object
     * since we do not know the (numeric) type of the value.
     * </p>
     *
     * @param pfdParam  enumeration constant
     * 
     * @return  the maximum value of the parameter as a <code>PropertyValue</code> object
     * 
     * @throws IllegalArgumentException  the key was not found in the configuration file
     * 
     * @since  Dec 21, 2009
     * @author Christopher K. Allen
     */
    public static Property      getMaxLimit(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_MAX;
        String          strKey = pfdParam.getRbHandle() + STR_MAX;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        return value;
    }
    
    /**
     * <p>
     * Returns the initial value for the given parameter.  This is meant as a convenient
     * value to initialize a parameter.
     * </p>
     * <p>
     * Note the parameter
     * is identified by its enumeration constant.  The key for
     * the returned value is formed from the dot-separated
     * concatenation
     * <br/>
     * <br/>
     * <tt>className</tt> + "." + <tt>enumName</tt> + "." + <tt>"INIT"</tt>
     * <br/>
     * <br/>
     * where <tt>className</tt> is the formal class
     * name of the enumeration and <tt>enumName</tt>  is the
     * name of the enumeration constant.  This key is then used
     * to access the value in the configuration file 
     * (see {@link #STR_FILE_CONFIG})
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; This value is returned as a {@link Property} object
     * since we do not know the (numeric) type of the value.
     * </p>
     *
     * @param pfdParam  enumeration constant
     * 
     * @return  the maximum value of the parameter as a <code>PropertyValue</code> object
     * 
     * @throws IllegalArgumentException  the key was not found in the configuration file
     * 
     * @since  Dec 21, 2009
     * @author Christopher K. Allen
     */
    public static Property      getInitialValue(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
        String          strKey = pfdParam.getRbHandle() + STR_INI;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        return value;
    }
    

    /**
     * <p>
     * Returns the increment value for the given parameter.  This is the 
     * value that is used by some GUI controls, such as sliders and
     * knobs, as the granularity for changes in the given parameter.  
     * </p>
     * <p>
     * Note the parameter
     * is identified by its enumeration constant.  The key for
     * the returned value is formed from the dot-separated
     * concatenation
     * <br/>
     * <br/>
     * <tt>className</tt> + "." + <tt>enumName</tt> + "." + <tt>"MAX"</tt>
     * <br/>
     * <br/>
     * where <tt>className</tt> is the formal class
     * name of the enumeration and <tt>enumName</tt>  is the
     * name of the enumeration constant.  This key is then used
     * to access the value in the configuration file 
     * (see {@link #STR_FILE_CONFIG})
     * </p>
     * <p>
     * <h4>NOTE:</h4>
     * &middot; This value is returned as a {@link Property} object
     * since we do not know the (numeric) type of the value.
     * </p>
     *
     * @param pfdParam  enumeration constant
     * 
     * @return  the maximum value of the parameter as a <code>PropertyValue</code> object
     * 
     * @throws IllegalArgumentException  the key was not found in the configuration file
     * 
     * @since  Dec 21, 2009
     * @author Christopher K. Allen
     */
    public static Property      getIncrement(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_DEL;
        String          strKey = pfdParam.getRbHandle() + STR_DEL;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        return value;
    }
    
    /**
     * Return the configuration file display format for the given
     * device parameter.
     *
     * @param pfdParam  the device parameter enumeration constant 
     * 
     * @return  the numeric formatting object to display parameter values
     * 
     * @throws IllegalArgumentException  the key was not found in the configuration file
     * 
     * @since  Jan 12, 2010
     * @author Christopher K. Allen
     */
    public static NumberFormat      getDisplayFormat(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_FMT;
        String          strKey = pfdParam.getRbHandle() + STR_FMT;
        Property        prpVal = MGR_DEV_PROPS.getProperty(strKey);

        if (prpVal.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        String          strFmt  = prpVal.asString();
        DecimalFormat   fmtDspl = new DecimalFormat(strFmt);

        return fmtDspl;
    }
    
    
    /**
     * Return the value of the error condition - for 
     * a device status parameter.
     *
     * @param pfdParam  the device status parameter enumeration constant 
     * 
     * @return  the numeric value of the error condition
     * 
     * @throws IllegalArgumentException  the key not found or bad number format
     * 
     * @since  Jan 12, 2010
     * @author Christopher K. Allen
     */
    public static Integer getErrorValue(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_ERR;
        String          strKey = pfdParam.getRbHandle() + STR_ERR;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);
        
        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        try {
            String          strVal = value.asString();
            Integer         intVal = Integer.parseInt(strVal); 

            return intVal;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad number format for key " + strKey, e);
            
        }
    }
    
    /**
     * Return the value of the normal operating condition - for 
     * a device status parameter.
     *
     * @param pfdParam  the device status parameter enumeration constant 
     * 
     * @return  the numeric value of the normal condition
     * 
     * @throws IllegalArgumentException  the key not found or bad number format
     * 
     * @since  Jan 12, 2010
     * @author Christopher K. Allen
     */
    public static Integer getNormalValue(ScadaFieldDescriptor pfdParam)
        throws IllegalArgumentException
    {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_NML;
        String          strKey = pfdParam.getRbHandle() + STR_NML;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        try {
            String          strVal = value.asString();
            Integer         intVal = Integer.parseInt(strVal); 

            return intVal;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad number format for key " + strKey, e);
            
        }
    }
    
    /**
     * Returns the array of values as integers, as specified in the
     * device configuration file.  The values are specified as 
     * as vector of Comma Separated Values (CSVs) in the configuration
     * file, with the suffix key <code>{@link #STR_VALS}</code> 
     *
     * @param pfdParam  the parameter descriptor
     * 
     * @return  an array of integers parsed from the the configuration file entry
     * 
     * @since  Jan 25, 2010
     * @author Christopher K. Allen
     */
    public static Integer[]      getValuesInt(ScadaFieldDescriptor pfdParam) {
//        String          strKey = pfdParam.getClass().getName()+ "." + pfdParam.getFieldName() + STR_VALS;
        String          strKey = pfdParam.getRbHandle() + STR_VALS;
        Property        value  = MGR_DEV_PROPS.getProperty(strKey);

        if (value.isNull())
            throw new IllegalArgumentException("Unable to find value for key " + strKey);

        try {
            String          strPropVal = value.asString();
            String[]        arrTokens  = strPropVal.split(",");
            Integer[]       arrVals    = new Integer[arrTokens.length];
            
            int         i=0;
            for (String strVal : arrTokens) 
                arrVals[i++] = Integer.parseInt(strVal);

            return arrVals;
            
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Bad number format for key " + strKey, e);
            
        }
    }
    
    
    
    
    /*
     * Local Methods
     */
    
    /**
     * Singleton class - we must restrict instantiation.
     *
     *
     * @since     Jul 15, 2009
     * @author    Christopher K. Allen
     */
    private DeviceProperties(Properties propsDefault)      {
        super(DeviceProperties.class, propsDefault);
    };
    
}
