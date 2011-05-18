/**
 * ModelConfiguration.java
 *
 * @author Christopher K. Allen
 * @since  May 16, 2011
 *
 */

/**
 * ModelConfiguration.java
 *
 * @author  Christopher K. Allen
 * @since	May 16, 2011
 */
package xal.sim.cfg;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;

/**
 * Maintains all the configuration management information
 * for lattice generation.  The configuration details are
 * loaded from the lattice generation configuration files
 * then built into this managing data structure. 
 *
 * @author Christopher K. Allen
 * @since   May 16, 2011
 */
public class ModelConfiguration implements IArchive {
    
    /**
     * An enumeration of the top level data node names in the
     * lattice generation configuration file.  This is just a
     * convenient way to centralize the XML element strings
     * for management purposes.
     *
     * @author Christopher K. Allen
     * @since   May 16, 2011
     */
    public enum DOC {
        
        /** Section containing information on the modeling elements */
        ELEM("elements"),
        
        /** Section containing information on the accelerator hardware */
        HWARE("hardware"),
        
        /** Set of associations be the hardware devices and the modeling elements */
        ASSOC("associations");
        
        
        
        /**
         * Returns the name of the root node of the document.
         * That is, the highest level node of the document.
         *
         * @return  root element name of the configuration document
         *
         * @author Christopher K. Allen
         * @since  May 16, 2011
         */
        public static String getXmlDocumentName() { return "configuration"; }
        
        /**
         * Return the name of element which this enumeration constant
         * represents.  This is the name of the XML node in the 
         * model configuration file.
         *
         * @return  configuration file node name of this constant
         *
         * @author Christopher K. Allen
         * @since  May 16, 2011
         */
        public String getXmlElementName() { return this.strName; };
        
        
        /* private */
        
        /** The of the element which this enumeration constant represents */
        final private String        strName;
        
        /** Construct a new enumeration constant with the given name */
        private DOC(String strName) { this.strName = strName; };
    }
    
    
    /**
     * Enumeration of hardware nodes in the lattice generation configuration
     * file.
     *
     * @author Christopher K. Allen
     * @since   May 16, 2011
     */
    public enum HWARE { 
    
        /** Information about "thick" hardware */
        THK("thick"),
        
        /** Information about "thin" hardware */
        THN("thin"),
        
        /** Hardware that should be split (e.g., to mark the center) */
        SPLIT("split");
        

    
        /**
         * Returns the <i>type</i> attribute name for hardware nodes.
         * This attribute specifies the class type of the SMF hardware
         * device.
         *
         * @return  XML attribute name of the class type property
         *
         * @author Christopher K. Allen
         * @since  May 16, 2011
         */
        public static String getXmlTypeAttr() { return "type"; }
        
        /**
         * Return the name of element which this enumeration constant
         * represents.  This is the name of the XML node in the 
         * model configuration file.
         *
         * @return  configuration file node name of this constant
         *
         * @author Christopher K. Allen
         * @since  May 16, 2011
         */
        public String getElementName() { return this.strName; };
        
        
        /* private */
        
        /** The of the element which this enumeration constant represents */
        final private String        strName;
        
        /** Construct a new enumeration constant with the given name */
        private HWARE(String strName) { this.strName = strName; };
    }
    
    
    /*
     * Local Attributes
     */
    
    /** List of thin hardware types (class names) */
    private List<String>          lstThnHware;
    
    /** List of thick hardware types (class names) */
    private List<String>          lstThkHware;
    
    /** List of association classes between hardware and modeling elements */
    private List<AssociationDef>  lstAssoc;

    
    
    /*
     * Initialization
     */
    
    /**
     * Creates a new <code>ModelConfiguration</code> object and
     * initializes it using the configuration file at the given
     * location.
     * 
     * @param strUrlCfgFile             URL of the configuration file
     * 
     * @throws MalformedURLException        bad URL format
     * @throws  ResourceNotFoundException   the configuration file was not located at the given URL
     * @throws  ParseException              general XML parsing exception - could not read file
     * 
     * @author  Christopher K. Allen
     * @since   May 16, 2011
     */
    public ModelConfiguration(String strUrlCfgFile) 
        throws MalformedURLException, ResourceNotFoundException, ParseException  
    {
        this( new File(strUrlCfgFile) );
    }
    
    /**
     * Creates a new <code>ModelConfiguration</code> object and
     * initializes it using the configuration file at the given
     * location.
     * 
     * @param  fileCfg                  URL of the configuration file
     * 
     * @throws  ResourceNotFoundException   the configuration file was not located at the given URL
     * @throws  ParseException              general XML parsing exception - could not read file
     * 
     * @throws MalformedURLException    bad URL format
     *
     * @author  Christopher K. Allen
     * @since   May 16, 2011
     */
    public ModelConfiguration(File fileCfg) 
        throws MalformedURLException, ResourceNotFoundException, ParseException  
    {
        this( fileCfg.toURI().toURL() );
    }
    
    /**
     * Creates a new <code>ModelConfiguration</code> object and
     * initializes it using the configuration file at the given
     * location.
     * 
     * @param urlCfgFile             URL of the configuration file
     * 
     * @throws  ResourceNotFoundException   the configuration file was not located at the given URL
     * @throws  ParseException              general XML parsing exception - could not read file
     * 
     * @author  Christopher K. Allen
     * @since   May 16, 2011
     */
    public ModelConfiguration(URL urlCfgFile) throws ResourceNotFoundException, ParseException {
        super();

        DataAdaptor daDoc = XmlDataAdaptor.adaptorForUrl(urlCfgFile, false);
        DataAdaptor daCfg = daDoc.childAdaptor( DOC.getXmlDocumentName() );

        this.load(daCfg);
        
//        DataAdaptor daDoc = XmlDataAdaptor.adaptorForUrl(urlCfgFile, false);
//        DataAdaptor daCfg = daDoc.childAdaptor( DOC.getXmlDocumentName() );
//
//        DataAdaptor daHware = daCfg.childAdaptor( DOC.HWARE.getXmlElementName() );
//        this.lstThnHware = this.loadThinHardware(daHware);
//        this.lstThkHware = this.loadThickHardware(daHware);
//        
//        DataAdaptor daAssoc = daCfg.childAdaptor( DOC.ASSOC.getXmlElementName() );
//        this.lstAssoc    = this.loadAssociations(daAssoc);
    }
    
    
    
    /*
     * IArchive Interface
     */
    
    /**
     * Saves the model configuration state to the data store
     * with the <code>{@link DataAdaptor}</code> interface.
     * 
     * @param   daArchive   data archive to receive model configuration 
     * 
     * @since May 18, 2011
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        DataAdaptor daCfg = daArchive.createChild( DOC.getXmlDocumentName() );
        
    }

    /**
     * Sets the state of the model configuration from the data source
     * behind the <code>{@link DataAdaptor}</code> interface.
     * 
     * @param   daSource    data source containing model configuration 
     * 
     * @throws DataFormatException  bad data format in data source (unreadable)
     * 
     * @since May 18, 2011
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {

        DataAdaptor daCfg = daSource.childAdaptor( DOC.getXmlDocumentName() );

        DataAdaptor daHware = daCfg.childAdaptor( DOC.HWARE.getXmlElementName() );
        this.lstThnHware = this.loadThinHardware(daHware);
        this.lstThkHware = this.loadThickHardware(daHware);
        
        DataAdaptor daAssoc = daCfg.childAdaptor( DOC.ASSOC.getXmlElementName() );
        this.lstAssoc    = this.loadAssociations(daAssoc);
    }
    
    
    
    
    /*
     * Support Methods
     */
    
    /**
     * Loads the list of hardware devices that are to be treated
     * as thin (indivisible, or "atomic") modeling elements given
     * the <code>DataAdaptor</code> for the <code>hardware</code>
     * section of the XML configuration file.  The returned list
     * list is simply the set of hardware class names of SMF devices.
     *
     * @param daHware   hardware node of the configuration file
     * 
     * @return          list of hardware type names to be treated as thin elements
     *
     * @author Christopher K. Allen
     * @since  May 16, 2011
     */
    private List<String>    loadThinHardware(DataAdaptor daHware) {
        
        // Create the new list
        List<String>    lstTypNms = new LinkedList<String>();
        
        // Get the list of thin hardware elements
        List<DataAdaptor>   lstThnDas = daHware.childAdaptors( HWARE.THN.getElementName() );
        
        for (DataAdaptor da : lstThnDas) 
            if ( da.hasAttribute(HWARE.getXmlTypeAttr()) ) {
                String  strTypNm = da.stringValue( HWARE.getXmlTypeAttr() );
                
                lstTypNms.add(strTypNm);
            }
        
        
        return lstTypNms;
    }
    
    /**
     * Loads the list of hardware devices that are to be treated
     * as thick (i.e., may be divided) modeling elements given
     * the <code>DataAdaptor</code> for the <code>hardware</code>
     * section of the XML configuration file.  The returned list
     * list is the set of hardware class names of SMF devices which
     * are thick.
     *
     * @param daHware   hardware node of the configuration file
     * 
     * @return          list of hardware type names to be treated as thick elements
     *
     * @author Christopher K. Allen
     * @since  May 16, 2011
     */
    private List<String>    loadThickHardware(DataAdaptor daHware) {
        
        // Create the new list
        List<String>    lstTypNms = new LinkedList<String>();
        
        // Get the list of thin hardware elements
        List<DataAdaptor>   lstThkDas = daHware.childAdaptors( HWARE.THK.getElementName() );
        
        for (DataAdaptor da : lstThkDas) 
            if ( da.hasAttribute(HWARE.getXmlTypeAttr()) ) {
                String  strTypNm = da.stringValue( HWARE.getXmlTypeAttr() );
                
                lstTypNms.add(strTypNm);
            }
        
        
        return lstTypNms;
    }
    
    /**
     * Loads the list of associations between hardware devices 
     * and modeling elements given the <code>DataAdaptor</code> 
     * for the <code>associations</code>
     * section of the XML configuration file.  
     *
     * @param daAssoc   association node of the configuration file
     * 
     * @return          list of hardware device to modeling element associations
     *
     * @author Christopher K. Allen
     * @since  May 16, 2011
     */
    private List<AssociationDef>    loadAssociations(DataAdaptor daAssoc) {
        
        // Create the new list
        List<AssociationDef>    lstAsses = new LinkedList<AssociationDef>();
        
        // Get the list of thin hardware elements
        List<DataAdaptor>   lstAssDas = daAssoc.childAdaptors( AssociationDef.ATTR.getXmlElementName() );
        
        for (DataAdaptor da : lstAssDas) {
            AssociationDef assoc = new AssociationDef(da);
            
            lstAsses.add(assoc);
        }
        
        
        return lstAsses;
    }

}
