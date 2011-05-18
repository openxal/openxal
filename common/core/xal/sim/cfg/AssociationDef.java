/**
 * AssociationDef.java
 *
 * @author Christopher K. Allen
 * @since  May 10, 2011
 *
 */

/**
 * AssociationDef.java
 *
 * @author  Christopher K. Allen
 * @since	May 10, 2011
 */
package xal.sim.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xal.model.IElement;
import xal.smf.AcceleratorNode;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * An <code>AssociationDef</code> object is a mapping between a hardware
 * device derived from <code>{@link AcceleratorNode}</code> to its
 * corresponding modeling element which exposes the 
 * <code>{@link IElement}</code> interface.
 *
 *
 * @author Christopher K. Allen
 * @since   May 10, 2011
 */
public class AssociationDef implements IArchive {

    
        
    /**
     * An enumeration of association attributes.  This is
     * also a central location to keep the XML attribute
     * names used in the configuration file, and also the
     * element name for associations.
     *
     * @author Christopher K. Allen
     * @since   May 12, 2011
     */
    public enum ATTR {
        
        /**
         * The class name of the SMF accelerator node hardware device.
         */
        SMF("smf"),
        
        /**
         * The class name of the online modeling element corresponding 
         * to the hardware device. 
         */
        MDL("model");
        
        
        /**
         * Returns the XML element name used for the association
         * in the configuration file.
         *
         * @return  XML element name for associations
         *
         * @author Christopher K. Allen
         * @since  May 12, 2011
         */
        static public String    getXmlElementName() {
            return "map";
        }
        
        /**
         * Returns the XML attribute name used for the association
         * attribute in the configuration file.
         *
         * @return  XML attribute name with the association
         *
         * @author Christopher K. Allen
         * @since  May 12, 2011
         */
        public String   getXmlAttributeName() {
            return this.strAttrNm;
        }
        
        
        /* Private */
        
        /** The XML attribute name used in the configuration file */
        final private String    strAttrNm;
        
        /** Sets the attribute name */
        private ATTR(String strAttrNm) {
            this.strAttrNm  = strAttrNm;
        }
    }
    
    
//    /**
//     * Enumeration of the child elements of an association.
//     * Used as a central location for the names used in the
//     * XML configuration file. 
//     *
//     * @author Christopher K. Allen
//     * @since   May 12, 2011
//     */
//    public enum ELEM {
//        
//        /**
//         * The initialization parameters for an association 
//         */
//        INIT("initialize"),
//        
//        /**
//         *  The synchronization section of an association 
//         */
//        SYNC("synchronize"),
//        
//        /**
//         *  Definition of a synchronization mode
//         */
//        MODE("mode");
//
//        /**
//         * Returns the XML attribute name used for the association
//         * attribute in the configuration file.
//         *
//         * @return  XML attribute name with the association
//         *
//         * @author Christopher K. Allen
//         * @since  May 12, 2011
//         */
//        public String   getXmlName() {
//            return this.strElemNm;
//        }
//        
//        
//        /* Private */
//        
//        /** The XML attribute name used in the configuration file */
//        final private String    strElemNm;
//        
//        /** Sets the attribute name */
//        private ELEM(String strElemNm) {
//            this.strElemNm  = strElemNm;
//        }
//    }
        
    
    /*
     * Local Attributes
     */
    
    /** class name of the SMF hardware device */
    private String      strClsHware;
    
    /** class name of the corresponding modeling element */
    private String      strClsModel;
    
    
    /** map of (SyncModeId,SynchronizationMap), storing the synchronization maps by their IDs*/
    final private Map<String, SynchronizationMap>   mapSyncMode;


    /*
     * Initialization
     */
    
    
    /**
     * Creates a new <code>AssociationDef</code> object and initializes
     * it with the data provided by the source with the <code>DataAdaptor</code>
     * interface.  The new object is initialized with a call to the 
     * <code>IArchive</code> method <code>{@link #load(DataAdaptor)}</code>.
     * 
     * @param daSource  source of initializing data defining the new association
     * 
     * @throws DataFormatException  the data source has an unreadable data format
     *
     * @author  Christopher K. Allen
     * @since   May 12, 2011
     * 
     * @see AssociationDef#load(DataAdaptor)
     */
    public AssociationDef(DataAdaptor daSource) throws DataFormatException {
        this();
        
        this.load(daSource);
    }
    
    /**
     * Creates a new, uninitialized <code>AssociationDef</code> object.
     * 
     *
     * @author  Christopher K. Allen
     * @since   May 12, 2011
     */
    public AssociationDef() {
        this.mapSyncMode = new HashMap<String, SynchronizationMap>();
    }
    
    
    /*
     * Attributes
     */
    
    /**
     * Returns the Java class name of the hardware class
     * constituting this association.
     * 
     * @return the class name of the hardware type
     */
    public String   getHardwareClassName() {
        return this.strClsHware;
    }

    /**
     * Returns the Java class name of the modeling class
     * constituting this association.
     * 
     * @return class name of the modeling element
     */
    public String getModelClassName() {
        return this.strClsModel;
    }


    /*
     * IArchive Interface
     */
    
    /**
     * Saves the model association to the data archive behind
     * the <code>DataAdaptor</code> interface.
     * 
     * @param daArchive data archive receiving the association definition
     * 
     * @since May 12, 2011
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        
    }


    /**
     * Load the properties of this association from the data source
     * with the <code>DataAdaptor</code> interface.
     * 
     * @param daSource data source defining a new association
     * 
     * @throws DataFormatException  <var>daSource</var> has unreadable data format
     * 
     * @since May 12, 2011
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {
        
        // Check that all attributes are present
        for (ATTR attr : ATTR.values())
            if ( !daSource.hasAttribute( attr.getXmlAttributeName()) )
                throw new DataFormatException( attr.getXmlAttributeName() + " attribute missing.");
        
        // Load the association attributes
        this.strClsHware = daSource.stringValue( ATTR.SMF.getXmlAttributeName() );
        this.strClsModel = daSource.stringValue( ATTR.MDL.getXmlAttributeName() );
        
        
        // Get all the synchronization modes for this association
        List<DataAdaptor> lstSyncDas = daSource.childAdaptors( SynchronizationMap.ATTR.getXmlElementName() );

        for (DataAdaptor daSyncMode : lstSyncDas) {
            SynchronizationMap  synMode = new SynchronizationMap( daSyncMode );
            
            String      strModeId = synMode.getModeId();
            
            this.mapSyncMode.put(strModeId, synMode);
        }
    }
    
}
