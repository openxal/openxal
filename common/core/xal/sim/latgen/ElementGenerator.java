/*
 * Created on Feb 20, 2004
 */
package xal.sim.latgen;

import xal.model.IElement;
import xal.model.IModelDataSource;
import xal.model.ModelException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.util.Hashtable;
import java.util.Properties;

/**
 * Class for dynamically creating modeling elements corresponding to hardware
 * <code>SMF</code> elements. This correspondence is determined by a
 * configuration file explicitly describing the mappings from package
 * <code>gov.sns.xal.smf</code> to package <code>gov.sns.xal.model</code>.
 * 
 * @author Craig McChesney
 * @author Christopher K. Allen
 */
public class ElementGenerator {

	/*
	 * Global Variables
	 */

	/** default configuration file name */
	public static final String s_strFileConfigDef = "Config\\xal\\ModelGenNodeMap.txt";

	/** key is accelerator node class, model element class */
	private static Hashtable<Object, Object> s_mapDefault;

	/*
	 * Local Attributes
	 */

	/** map of hardware to modeling element */
	private Hashtable m_mapSmfMod;

	/*
	 * Classloader Initialization
	 */

	static {

		try {
			s_mapDefault = ElementGenerator
					.loadConfiguration(ElementGenerator.s_strFileConfigDef);

		} catch (GenerationException e) {
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}

		//        s_mapDefault = new Properties();
		//     
		//        try {
		//            File file = new File(ElementGenerator.s_strFileConfigDef);
		//            
		//            if (file.exists()) {
		//                FileInputStream in = new FileInputStream(file);
		//
		//                try {
		//                    s_mapDefault.load(in);
		//                    
		//                } finally {
		//                    in.close();
		//                    
		//                }
		//            }
		//            
		//        } catch (Exception exc) {
		//        }
		//        
	}

	/*
	 * Static Methods
	 */

	/**
	 * Load the hardware-model element map from a configuration file.
	 * 
	 * @param strFileConfig
	 *            text-based configuration file
	 * 
	 * @return map of hardware object to modeling element
	 */
	private static Hashtable<Object,Object> loadConfiguration(String strFileConfig)
			throws GenerationException {

		Properties map = new Properties();

		try {
			File file = new File(strFileConfig);
			FileInputStream in = new FileInputStream(file);

			try {
				map.load(in);

			} finally {
				in.close();

			}

		} catch (Exception e) {
			throw new GenerationException(
					"ElementGenerator#loadConfiguration(): unable to load configuration");
		}

		return map;
	}

	// Public Class Methods ====================================================

	/**
	 * Generates a lattice element for the supplied node.
	 * 
	 * @param objectSource
	 *            <code>AcceleratorNode</code> to generate a lattice element for
	 * 
	 * @return a lattice element for the supplied node
	 * 
	 * @throws GenerationException
	 *             if the node type is unknown or other error generating the
	 *             element
	 */
	public static IElement generateElementFor(Object objectSource)
			throws GenerationException {

		// throw an exception if the provided node class is unknown
		String classNameElement = mappingFor(objectSource);

		// create an instance of the corresponding element class
		Class classElement;
		IElement elementInstance = null;
		if (classNameElement == null) {
			//change this to map to marker instead of throw exception???
			//throw new GenerationException("no map entry for: " +
			//objectSource.getClass().getName());
			try {
				classElement = Class.forName("gov.sns.xal.model.elem.Marker");
				elementInstance = (IElement) classElement.newInstance();
			} catch (ClassNotFoundException e) {
				// do nothing here
			} catch (InstantiationException e1) {
				throw new GenerationException("Error creating instance of: "
						+ classNameElement);
			} catch (IllegalAccessException e1) {
				throw new GenerationException("Error creating instance of: "
						+ classNameElement);
			}
		} else {

			try {
				classElement = Class.forName(classNameElement);
				elementInstance = (IElement) classElement.newInstance();
			} catch (ClassNotFoundException e) {
				throw new GenerationException("Element class not loaded: "
						+ classNameElement);
			} catch (InstantiationException e1) {
				throw new GenerationException("Error creating instance of: "
						+ classNameElement);
			} catch (IllegalAccessException e1) {
				throw new GenerationException("Error creating instance of: "
						+ classNameElement);
			}
		}

		//      Map valueMap = null;
		//      try {
		//          valueMap = PrimaryPropertyAccessor.valueMapFor(objectSource,
		// SynchronizationManager.DEFAULT_SYNC_MODE);
		//      } catch (ProxyException e2) {
		//          throw new GenerationException("ProxyException getting values for: " +
		// objectSource);
		//      }
		//      try {
		//          SynchronizationManager.resync(elementInstance, valueMap);
		//      } catch (SynchronizationException e3) {
		//          throw new GenerationException("SynchronizationException initializing
		// element with property values.");
		//      }

		// initialize element from node
		if (!(objectSource instanceof IModelDataSource))
			throw new GenerationException(
					"Source object doesn't implement IModelDataSource: "
							+ objectSource.getClass().getName());
		IModelDataSource source = (IModelDataSource) objectSource;
		try {
			elementInstance.initializeFrom(source);
		} catch (ModelException e2) {
			throw new GenerationException("ModelException initializing: "
					+ elementInstance + " from: " + objectSource);
		}

		return elementInstance;

	}

	// Private Class Methods ===================================================

	/**
	 * Returns true if there is a mapping for the supplied node, false
	 * otherwise.
	 * 
	 * @param smfNode
	 *            node to find mapping for
	 * 
	 * @return true if there is a mapping for smfNode, false otherwise
	 */
	private static String mappingFor(Object classSource) {
		final Object mapValue = s_mapDefault.get(classSource.getClass().getName());
        return mapValue != null ? mapValue.toString() : null;
	}

	/*
	 * Initialization
	 */

	public ElementGenerator() {
		Hashtable mapDef = ElementGenerator.getDefaultMapping();
		this.setElementMap(mapDef);
	}

	/*
	 * Internal Support
	 */

	/**
	 * Set the internal map of hardware node (SMF) to model element
	 * (model.elem).
	 * 
	 * @param map
	 *            the new hardware to model map
	 */

	private void setElementMap(Hashtable map) {
		this.m_mapSmfMod = map;
	}

	/**
	 * Return the default hardware to model mapping used by the class.
	 * 
	 * @return default configuration mapping
	 */
	private static Hashtable<Object,Object> getDefaultMapping() {
		return ElementGenerator.s_mapDefault;
	}

	/*
	 * Testing and Debugging
	 */

	/**
	 * Test Driver
	 */
	public static void main(String arrArgs[]) {
		String strFile = "ElementGeneratorTestFile.txt";
		File fileTest = new File(strFile);

		try {
			FileWriter os = new FileWriter(fileTest);
			os.write("test");
			os.close();

		} catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace(System.out);

		}

	}
}
