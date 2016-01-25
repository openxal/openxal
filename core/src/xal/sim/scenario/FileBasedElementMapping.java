/*
 * FileBasedElementMapping.java
 * 
 * Created on Jan 27, 2014
 */

package xal.sim.scenario;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import javax.swing.text.StyledEditorKit.BoldAction;

import xal.model.IComponent;
import xal.model.IComposite;
import xal.model.Sector;
import xal.model.ModelException;
import xal.model.elem.IdealDrift;
import xal.model.elem.IdealRfCavityDrift;
import xal.model.elem.Marker;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * <p>
 * The default element mapping implemented as singleton.
 * </p>
 * <p>
 * The convention is to name the mapping definition file
 * <tt>ModelConfig.modconfig</tt> and place it in the same directory as
 * the Open XAL master configuration file <tt>main.xal</tt>.  A entry reference to
 * the mapping definition is made within the main configuration file.
 * </p>
 * <p>
 * <h4>CKA NOTES - Dec 3, 2014</h4>
 * &middot; The class no longer appears to be a singleton.  You may create multiple
 * instances using the same or difference mapping definition files.
 * </p>
 * 
 * @author Ivo List
 * @since  Jan 27, 2014
 */
public class FileBasedElementMapping extends ElementMapping {

    /*
     * Global Constants
     */
    
    /** Model Configuration schema */
    final public static String elementMappingSchema = "/xal/schemas/ModelConfig.xsd";
    
    /*
     * Global Attributes
     */
    
    /** 
     * The class singleton instance 
     * 
     * @deprecated  CKA - never used
     */
    @Deprecated
    protected static ElementMapping instance;
    

    /*
     * Global Methods
     */
    /**
     * <p>
     * Creates a new element mapping objects from the given definition file and its
     * provided schema.  This file should contain a set of 
     * (hardware type id, model class type) pairs arranged according to the given schema.
     * It is usually named <tt>ModelConfig.modconfig</tt>
     * </p>
     * <p>
     * Current the default element type is set to <code>{@link xal.model.elem.Marker}</code>
     * and the default sequence type is set to <code>{@link xal.model.Sector}</code>
     * </p>
     * 
     * @param urlModelConfig    location of the element mapping definition file  
     * @param schemaUrl         location of the schema definition file (i.e., xsd file)
     * 
     * @return          a new element mapping object defined according to the given file
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    public static ElementMapping loadFrom(String urlModelConfig, String schemaUrl) {

        // Attach a data adaptor to the URL resource
        DataAdaptor daDoc = XmlDataAdaptor.adaptorForUrl(urlModelConfig, false, schemaUrl);
        FileBasedElementMapping mapHwToModElem = new FileBasedElementMapping();

        // Drill down to the data adaptor node containing the (hardware, model) associations 
        DataAdaptor daCfg = daDoc.childAdaptor( "configuration" );
        
        if (daCfg.hasAttribute("debug")) mapHwToModElem.bolDebug = daCfg.booleanValue("debug");
        if (daCfg.hasAttribute("divMags")) mapHwToModElem.bolDivMags = daCfg.booleanValue("divMags");
        if (daCfg.hasAttribute("subsectionCtrOrigin")) mapHwToModElem.bolSubsectionCtrOrigin = daCfg.booleanValue("subsectionCtrOrigin");
        
        DataAdaptor daAssoc = daCfg.childAdaptor("associations");
        
        // For each map of hardware node to modeling element enter that association into the  table
        List<DataAdaptor> lstSrcDas = daAssoc.childAdaptors("map");
        for (DataAdaptor daSrc : lstSrcDas) {
            try {
                
                mapHwToModElem.putMap(daSrc.stringValue("smf"), daSrc.stringValue("model"));
                
            } catch (ClassNotFoundException e) {
                
                System.err.println("ClassNotFound when loading " + urlModelConfig + ": " + e.getMessage());
            }
        }

        
        // Now we get the elements either have general hardware associations or no hardware
        //  associations
        DataAdaptor daElements = daCfg.childAdaptor("elements");

        // Get the default modeling element used whenever there is no association for a hardware node
        try {
            
            mapHwToModElem.setDefaultElement(daElements.childAdaptor("default").stringValue("type"));
            
        } catch (ClassNotFoundException e) {
            
            System.err.println("ClassNotFound when loading " + urlModelConfig + ", using default element: " + e.getMessage());

            mapHwToModElem.clsDefaultElem = Marker.class;
        }
        
        // Get the default sequence modeling element used whenever a hardware sequence has no model counterpart
        try {
            DataAdaptor daSeq = daElements.childAdaptor("sequence");
            if (daSeq == null) throw new ClassNotFoundException("No sequence attribute in model configuration file.");
            String      strClsNm = daSeq.stringValue("type");

            mapHwToModElem.setDefaultSequence(strClsNm);
            
        } catch (ClassNotFoundException e) {
            System.err.println("Problem when loading " + urlModelConfig + ", using default sequence element: " + e.getMessage());

            mapHwToModElem.clsDefaultSeq = Sector.class;
        }
        
        // Get the element type used to represent general drift spaces (e.g., through transports)
        try {

            mapHwToModElem.setDrift(daElements.childAdaptor("drift").stringValue("type"));
        
        } catch (ClassNotFoundException e) {
        
            System.err.println("Problem when loading " + urlModelConfig + ", using default drift: " + e.getMessage());
            mapHwToModElem.clsDriftElem = IdealDrift.class;
        }
        
        // The the element type used to represent an RF cavity drift space
        try {
            DataAdaptor   daCavDrift = daElements.childAdaptor("rfcavdrift");
            if (daCavDrift == null) throw new ClassNotFoundException("No rfcavdrift element in model configuration file.");
            String        strClsName = daCavDrift.stringValue("type");

            mapHwToModElem.setRfCavityDrift(strClsName);

        } catch (ClassNotFoundException e) {
            System.err.println("Problem when loading " + urlModelConfig + ", using default RF cavity drift: " + e.getMessage());
            mapHwToModElem.clsRfCavDriftElem = IdealRfCavityDrift.class;
        }
        
        // Return the completed collection of (hardware node, modeling element) associations
        return mapHwToModElem;
    }


    /* 
     * Local Attributes
     */
    
    /** class type of the modeling element used whenever there is no map entry */
	protected Class<? extends IComponent> clsDefaultElem;
	
	/** class type of the modeling element used whenever there is no map entry for a hardware sequence */
	protected  Class<? extends IComposite> clsDefaultSeq;
	
	/** class type of the general drift spaces created on demand */
	protected Class<? extends IComponent> clsDriftElem;
	
	/** class type of the RF cavity drift spaced created on demand */
	protected Class<? extends IComponent> clsRfCavDriftElem;
	
	
	/*
	 * Initialization
	 */
	
	
	/**
	 * Constructor for the singleton <code>FileBasedElementMapping</code>.
	 *
	 * @since  Dec 3, 2014
	 */
	protected FileBasedElementMapping() {
	}
	
	
	/*
	 * ElementMapping Requirements
	 */
	
	/**
	 * <p>
	 * Returns the modeling element type for a hardware node used whenever
	 * no specific (hardware,model) association has been defined.
	 * </p>
	 * <p>
	 * This value is initialized in global method 
	 * <code>{@link #loadFrom(String, String)}</code>.
	 * </p>
	 *
	 * @see xal.sim.scenario.ElementMapping#getDefaultElementType()
	 *
	 * @since  Dec 5, 2014  @author Christopher K. Allen
	 */
	@Override
	public Class<? extends IComponent> getDefaultElementType() {
		return this.clsDefaultElem;
	}

	/**
	 * <p>
     * Returns the modeling element type for a hardware sequence used whenever
     * no specific (hardware,model) association has been defined.
     * </p>
     * <p>
     * This value is initialized in global method 
     * <code>{@link #loadFrom(String, String)}</code>.
     * </p>
     *
     * @see xal.sim.scenario.ElementMapping#getDefaultSequenceType()
     *
     * @since  Dec 5, 2014  @author Christopher K. Allen
     */
    @Override
    public Class<? extends IComposite> getDefaultSequenceType() {
        return this.clsDefaultSeq;
    }


    /**
	 * Creates a general drift space.
	 *
	 * @see xal.sim.scenario.ElementMapping#createDefaultDrift(java.lang.String, double)
	 *
	 * @author Christopher K. Allen
	 * @since  Dec 3, 2014
	 */
	@Override
	public IComponent createDefaultDrift(String name, double len) throws ModelException {
		try {
			return clsDriftElem.getConstructor(String.class, double.class).newInstance(name, len);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ModelException("Problem when instantiating drift element", e);				
		}		
	}

	/**
	 * Creates a new drift space within a coupled-cavity RF tank.
     *
     * @see xal.sim.scenario.ElementMapping#createRfCavityDrift(java.lang.String, double, double, double)
     *
     * @author Christopher K. Allen
     * @since  Dec 3, 2014
     */
    @Override
    public IComponent createRfCavityDrift(String name, double len, double freq, double mode) throws ModelException {
        try {
            Constructor<? extends IComponent> ctorElem  = this.clsRfCavDriftElem.getConstructor(String.class, double.class, double.class, double.class);
            IComponent  elemDrift = ctorElem.newInstance(name, len, freq, mode); 
            
            return elemDrift;
            
        } catch (InstantiationException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException
                | NoSuchMethodException | SecurityException e) {
            throw new ModelException("Problem when instantiating RF cavity drift element", e);                
        }       
    }

    /*
     * Internal Support
     */
    
    @SuppressWarnings( "unchecked" )
    private void setDefaultElement(String stringValue) throws ClassNotFoundException {
        clsDefaultElem = (Class<? extends IComponent>) Class.forName(stringValue);      
    }
    
    @SuppressWarnings("unchecked")
    private void setDefaultSequence(String strClassType) throws ClassNotFoundException {
        this.clsDefaultSeq = (Class<? extends IComposite>) Class.forName(strClassType);
    }


    @SuppressWarnings( "unchecked" )
	private void setDrift(String stringValue) throws ClassNotFoundException {
		clsDriftElem = (Class<? extends IComponent>) Class.forName(stringValue);
		
	}
    
    @SuppressWarnings( "unchecked" )
    private void setRfCavityDrift(String strClsName) throws ClassNotFoundException {
        this.clsRfCavDriftElem = (Class<? extends IComponent>) Class.forName(strClsName);
    }

	@SuppressWarnings( "unchecked" )
	private void putMap(String smf, String model) throws ClassNotFoundException {
		putMap(smf, (Class<? extends IComponent>)Class.forName(model));
	}
}
