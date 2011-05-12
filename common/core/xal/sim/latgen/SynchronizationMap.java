/**
 * SynchronizationMap.java
 *
 * @author Christopher K. Allen
 * @since  May 12, 2011
 *
 */

/**
 * SynchronizationMap.java
 *
 * @author  Christopher K. Allen
 * @since	May 12, 2011
 */
package xal.sim.latgen;

import java.util.LinkedList;
import java.util.List;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * <p>
 * This class essentially defines the configuration of a
 * <i>synchronization mode</i>.  It is a set of parameter
 * mappings between a hardware device and its corresponding
 * modeling element.  It is assumed that how the mappings are
 * used depends upon the <em>mode</em> being defined.
 * </p>
 * <p>
 * It is also assumed that this class will be instantiated
 * according to the configuration file used by the modeling 
 * scenario generator.  Thus, the lack of initiailizing
 * methods.
 * </p>
 *
 * @author Christopher K. Allen
 * @since   May 12, 2011
 */
public class SynchronizationMap implements IArchive {


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
         * The name (ID) of the synchronization mode. 
         */
        MODE("mode");
        
        
        /**
         * Returns the XML element name used for the synchronization mode
         * in the configuration file.
         *
         * @return  XML element name for associations
         *
         * @author Christopher K. Allen
         * @since  May 12, 2011
         */
        static public String    getXmlElementName() {
            return "synchronization";
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
    
    
    
    /*
     * Local Attributes
     */
    
    /** class name of the SMF hardware device */
    private String      strModeId;
    
    
    /** list of (Hardware,model) parameter mappings for element initialization */
    final private List<ParameterMap>    lstSyncPrm;
    
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new, uninitialized <code>SynchronizationMap</code>
     * object.
     *
     * @author  Christopher K. Allen
     * @since   May 12, 2011
     */
    public SynchronizationMap() {
        this.lstSyncPrm = new LinkedList<ParameterMap>();
    }
    
    /**
     * Creates a new <code>SynchronizationMap</code> object and
     * initializes it uses the data source with the 
     * <code>DataAdaptor</code> interface.
     * 
     * @param daArchive     data source defining a new synchronization mode 
     *
     * @throws DataFormatException  <var>daArchive</var> has unreadable data format
     * 
     * @author  Christopher K. Allen
     * @since   May 12, 2011
     * 
     * @see SynchronizationMap#load(DataAdaptor)
     */
    public SynchronizationMap(DataAdaptor daArchive) throws DataFormatException {
        this();
        
        this.load(daArchive);
    }
    
    
    /*
     * Attributes
     */
    
    /**
     * Returns the mode identifier string of this
     * synchronization map.
     * 
     * @return  synchronization set id
     *
     * @author Christopher K. Allen
     * @since  May 12, 2011
     */
    public String getModeId() { 
        return this.strModeId;
    }
    
    /**
     * Returns the set of parameters defining this
     * synchronization mode.
     *
     * @return  list of parameter maps
     *
     * @author Christopher K. Allen
     * @since  May 12, 2011
     */
    public List<ParameterMap>   getSyncParameters() {
        return this.lstSyncPrm;
    }


    
    /*
     * IArchive Interface
     */
    
    
    /**
     * Save the synchronization map to the data sink behind
     * the <code>DataAdaptor</code> interface.
     * 
     * @param daArchive   data sink to receive synchronization map
     *  
     * @since May 12, 2011
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    @Override
    public void save(DataAdaptor daArchive) {
        DataAdaptor daSync = daArchive.createChild( ATTR.getXmlElementName() );

        // Set the synchronization mode identifier
        daSync.setValue( ATTR.MODE.getXmlAttributeName(), this.getModeId());
        
        // Set all the synchronization parameters
        for (ParameterMap pmap : this.lstSyncPrm) 
            pmap.save(daSync);
    }


    /**
     * Creates the synchronization map by reading the information
     * in from the the data source with the <code>DataAdaptor</code>
     * interface.
     * 
     * @param daSource   data source for defining the synchronization map
     * 
     * @throws DataFormatException  <var>daSource</var> has unreadable data format
     * 
     * @since May 12, 2011
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    @Override
    public void load(DataAdaptor daSource) throws DataFormatException {

        if ( !daSource.hasAttribute( ATTR.MODE.getXmlAttributeName() ) )
            throw new DataFormatException(ATTR.MODE.getXmlAttributeName() + " attribute not present");

        this.strModeId = daSource.stringValue( ATTR.MODE.getXmlAttributeName() );
        
        // Load all the synchronization parameters
        List<DataAdaptor> setPrms = daSource.childAdaptors(ParameterMap.ATTR.getXmlElementName());
        
        for (DataAdaptor daPrm : setPrms) {
            ParameterMap    map = new ParameterMap(daPrm);
            
            this.lstSyncPrm.add(map);
        }
    }
    
}
