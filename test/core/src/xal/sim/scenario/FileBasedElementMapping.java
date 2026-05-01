/*
 * FileBasedElementMapping.java
 * 
 * Created on Jan 27, 2014
 */

package xal.sim.scenario;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import xal.model.IComponent;
import xal.model.ModelException;
import xal.model.elem.IdealDrift;
import xal.model.elem.Marker;
import xal.tools.data.DataAdaptor;
import xal.tools.xml.XmlDataAdaptor;

/**
 * The default element mapping implemented as singleton.
 * 
 * @author Ivo List
 *
 */
public class FileBasedElementMapping extends ElementMapping {
	protected static ElementMapping instance;
	
	protected Class<? extends IComponent> defaultElement;
	protected Class<? extends IComponent> driftElement;
	
	/** Model Configuration schema */
    final public static String elementMappingSchema = "/xal/schemas/ModelConfig.xsd";
	
	protected FileBasedElementMapping() {
	}
	
	@Override
	public Class<? extends IComponent> getDefaultConverter() {
		return defaultElement;
	}

	@Override
	public IComponent createDrift(String name, double len) throws ModelException {
		try {
			return driftElement.getConstructor(String.class, double.class).newInstance(name, len);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			throw new ModelException("Problem when instantiating drift element", e);				
		}		
	}

	public static ElementMapping loadFrom(String urlModelConfig, String schemaUrl) {
		
			DataAdaptor daDoc = XmlDataAdaptor.adaptorForUrl(urlModelConfig, false, schemaUrl);
			FileBasedElementMapping elementMapping = new FileBasedElementMapping();
				
			DataAdaptor daCfg = daDoc.childAdaptor( "configuration" );	    
			DataAdaptor daAssoc = daCfg.childAdaptor("associations");        
			List<DataAdaptor> lstSrcDas = daAssoc.childAdaptors("map");
			for (DataAdaptor daSrc : lstSrcDas) {
				try {
					elementMapping.putMap(daSrc.stringValue("smf"), daSrc.stringValue("model"));                      
				} catch (ClassNotFoundException e) {
					System.err.println("ClassNotFound when loading " + urlModelConfig + ": " + e.getMessage());
				}
			}
			
			DataAdaptor daElements = daCfg.childAdaptor("elements");
			
			try {
				elementMapping.setDefault(daElements.childAdaptor("default").stringValue("type"));
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFound when loading " + urlModelConfig + ", using default default: " + e.getMessage());
				elementMapping.defaultElement = Marker.class;
			}
			try {
				elementMapping.setDrift(daElements.childAdaptor("drift").stringValue("type"));
			} catch (ClassNotFoundException e) {
				System.err.println("ClassNotFound when loading " + urlModelConfig + ", using default drift: " + e.getMessage());
				elementMapping.driftElement = IdealDrift.class;
			}
			return elementMapping;
	}


	@SuppressWarnings( "unchecked" )
	private void setDrift(String stringValue) throws ClassNotFoundException {
		driftElement = (Class<? extends IComponent>) Class.forName(stringValue);
		
	}

	
	@SuppressWarnings( "unchecked" )
	private void setDefault(String stringValue) throws ClassNotFoundException {
		defaultElement = (Class<? extends IComponent>) Class.forName(stringValue);		
	}


	@SuppressWarnings( "unchecked" )
	private void putMap(String smf, String model) throws ClassNotFoundException {
		putMap(smf, (Class<? extends IComponent>)Class.forName(model));
	}
}
