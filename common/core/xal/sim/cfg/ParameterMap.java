/**
 * ParameterMap.java
 *
 * @author Christopher K. Allen
 * @since  May 11, 2011
 *
 */

/**
 * ParameterMap.java
 *
 * @author  Christopher K. Allen
 * @since	May 11, 2011
 */
package xal.sim.cfg;

import xal.sim.latgen.GenerationException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * <p>
 * Represents a parameter association between the hardware and the
 * modeling element of the outer class.  Basically this consists 
 * of the matching "<i>getter</i>" 
 * method of the hardware device to the appropriate "<i>setter</i>" 
 * method of the modeling element.
 * There may be many parameter associations between a hardware device
 * and modeling element, or there may be none.
 * </p>
 *
 * @author Christopher K. Allen
 * @since   May 10, 2011
 */
public class ParameterMap implements IArchive {

    
    
    /*
     * Global Constants
     */
    

    /**
     * An enumeration of the attributes of a <code>ParameterMap</code>
     * object.  From each enumeration constant you may also recover
     * the name for the parameter attribute used in the XML configuration
     * file (see {@link #getXmlAttributeName()}.
     *
     * @author Christopher K. Allen
     * @since   May 11, 2011
     */
    public enum ATTR {
        /**
         * Name of the parameter
         */
        NAME("name"),
        
        /**
         * SMF device method name which is source of the parameter values 
         * (i.e., "getter").
         */
        HSRC("hget"),
        
        /**
         * Modeling element method name which is the sink of parameter values
         * (i.e., "setter").
         */
        MSNK("mset"),
        
        /**
         * Data type of the underlying parameter values.
         */
        TYPE("type");
        
        
        /** 
         * Returns the XML element name used to identify <code>ParameterMap</code> 
         * data in the XML configuration files.
         * 
         * @return  the XML element names for <code>ParameterMap</code> objects
         */
        public static String    getXmlElementName() {
            return "parameter";
        }
        
        /**
         * Returns the name used for this attribute in the XML
         * model-generation configuration file.
         *
         * @return  name of this parameter attribute used in the configuration file 
         *
         * @author Christopher K. Allen
         * @since  May 11, 2011
         */
        public String   getXmlAttributeName() {
            return this.strXmlNm;
        }
        
        
        
        /*
         * Support
         */
        
        /** The XML attribute name in the configuration file */
        final private String    strXmlNm;
        
        /** Initializing constructor */
        private ATTR(String strXmlNm) {
            this.strXmlNm = strXmlNm;
        }
    }
    
    
    /*
     * Global Operations
     */
    
    /**
     * Creates a new <code>ParameterMap</code> object from the properties contained
     * in the given data source.
     *
     * @param daSrc     data source containing parameter specifications
     * 
     * @return          a new <code>ParameterMap</code> object with the properties
     *                  specified in the data source

     * @throws DataFormatException
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    static public ParameterMap create(DataAdaptor daSrc) throws DataFormatException {
        for (ATTR attr : ATTR.values()) {
            String      strAttrNm = attr.getXmlAttributeName();
            
            if ( !daSrc.hasAttribute(strAttrNm) )
                throw new DataFormatException(strAttrNm + " attribute is corrupt");
        }

        String  strName   = daSrc.stringValue( ATTR.NAME.getXmlAttributeName() );
        String  strSmfget = daSrc.stringValue( ATTR.HSRC.getXmlAttributeName() );
        String  strMset   = daSrc.stringValue( ATTR.MSNK.getXmlAttributeName() );
        String  strType   = daSrc.stringValue( ATTR.TYPE.getXmlAttributeName() );

        return new ParameterMap(strType, strSmfget, strMset, strName);
        
//        try {
//            ParameterMap   prm = new ParameterMap(strType, strSmfget, strMset, strName);
//            
//            return prm;
//            
//        } catch (GenerationException e) {
//            throw new DataFormatException("Unknown underlying data type in parameter", e);
//        }
    }
    
    
    
    /*
     * Local Attributes
     */
    
    /** The name of the parameter */
    private String      strPrmNm;
    
    /** The source method for parameter values */
    private String      strSrcNm;
    
    /** The sink method for parameter values */
    private String      strSnkNm;
    
    /** The data type of the parameter */
    private String      strClsTyp;
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * 
     * @param daSource
     * 
     * @throws DataFormatException
     *
     * @author  Christopher K. Allen
     * @since   May 12, 2011
     */
    public ParameterMap(DataAdaptor daSource) throws DataFormatException {
        this.load(daSource);
    }
    
    /**
     * Creates a new <code>ParameterMap</code> object with the given
     * attributes.
     * 
     * @param strTypeName   The data type (class type) of the parameter
     * @param strMthSrc     Name of the getter method on the hardware device
     * @param strMthSnk     Name of the setter method on the modeling element
     * 
     * @throws GenerationException  The data type is undefined or a method is unreachable/nonexistent 
     *
     * @author  Christopher K. Allen
     * @since   May 10, 2011
     */
    public ParameterMap(String strTypeName, String strMthSrc, String strMthSnk)
        throws GenerationException
    {
        this(strTypeName, strMthSrc, strMthSnk, null);
    }
    
    
    /**
     * Creates a new association <code>ParameterMap</code> object according to
     * the given specifications.
     * 
     * @param strTypeNm   The data type (class type) of the parameter
     * @param strSrcNm     Name of the getter method on the hardware device
     * @param strSnkNm     Name of the setter method on the modeling element
     * @param strPrmNm   Name of this parameter, that is, its label
     * 
//     * @throws GenerationException  The data type is undefined  
     * 
     * @author  Christopher K. Allen
     * @since   May 10, 2011
     */
    public ParameterMap(String strTypeNm, String strSrcNm, String strSnkNm, String strPrmNm) 
//        throws GenerationException 
    {
        this.strClsTyp = strTypeNm;
        this.strPrmNm  = strPrmNm;
        this.strSrcNm  = strSrcNm;
        this.strSnkNm  = strSnkNm;
        
//        try {
//            this.strClsTyp  = Class.forName(strTypeNm);
//
//        } catch (ClassNotFoundException e) {
//            throw new GenerationException("Unable to identify parameter type", e);
//        }
    }
    
    /**
     * Returns the name identifier of this parameter.
     *  
     * @return  ParameterMap name
     *
     * @author Christopher K. Allen
     * @since  May 10, 2011
     */
    public String   getName() {
        return this.strPrmNm;
    }
    
    /**
     * Returns the method name of the SMF device type
     * which is the source of parameter values 
     * (i.e., during synchronization). 
     *
     * @return  SMF device getter method
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    public String   getSourceName() {
        return this.strSrcNm;
    }
    
    /**
     * Returns the method name of the online modeling element 
     * which is the sink of parameter values 
     * (i.e., during synchronization). 
     *
     * @return  online model element setter method
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    public String   getSinkName() {
        return this.strSnkNm;
    }
    
    /**
     * Returns the data type (class type) of the underlying
     * parameter values.
     *
     * @return  data type of this parameter
     *
     * @author Christopher K. Allen
     * @since  May 11, 2011
     */
    public String  getDataType() {
        return this.strClsTyp;
    }
    
//    /**
//     * Returns the data type (class type) of the underlying
//     * parameter values.
//     *
//     * @return  data type of this parameter
//     *
//     * @author Christopher K. Allen
//     * @since  May 11, 2011
//     */
//    public Class<?> getDataType() {
//        return this.strClsTyp;
//    }
//    
    
    
    /*
     * IArchive Interface
     */

    
    /**
     * Saves the internal state of this <code>ParameterMap</code> object
     * to the given data sink.
     * 
     * @param   daArchive   data sink with <code>DataAdaptor</code> interface
     * 
     * @since May 11, 2011
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        DataAdaptor daPrm = daArchive.createChild( ATTR.getXmlElementName() );
        
        daPrm.setValue(ATTR.NAME.getXmlAttributeName(), this.getName() );
        daPrm.setValue(ATTR.HSRC.getXmlAttributeName(), this.getSourceName() );
        daPrm.setValue(ATTR.MSNK.getXmlAttributeName(), this.getSinkName() );
        daPrm.setValue(ATTR.TYPE.getXmlAttributeName(), this.getDataType() );
    }

    /**
     * Loads the properties of the <code>Parameter</code> object from the 
     * data source behind the <code>DataAdaptor</code> interface.
     * 
     * @param   daSrc   data source containing state data for parameter
     * 
     * @since May 11, 2011
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSrc) throws DataFormatException {
        
        // Check that all the attributes are there
        for (ATTR attr : ATTR.values()) {
            String      strAttrNm = attr.getXmlAttributeName();
            
            if ( !daSrc.hasAttribute(strAttrNm) )
                throw new DataFormatException(strAttrNm + " attribute is corrupt");
        }


        // Load the attribute values
        this.strClsTyp = daSrc.stringValue(ATTR.TYPE.getXmlAttributeName()); 
        this.strPrmNm  = daSrc.stringValue(ATTR.NAME.getXmlAttributeName());
        this.strSrcNm  = daSrc.stringValue(ATTR.HSRC.getXmlAttributeName());
        this.strSnkNm  = daSrc.stringValue(ATTR.MSNK.getXmlAttributeName());

//        String  strTypeName = daSrc.stringValue(ATTR.TYPE.getXmlAttributeName());
//
//        
//        // Get the class type of the parameter
//        try {
//            this.strClsTyp  = Class.forName(strTypeName);
//
//        } catch (ClassNotFoundException e) {
//            throw new DataFormatException("Unable to identify parameter type", e);
//            
//        }
        
    }
    
}
