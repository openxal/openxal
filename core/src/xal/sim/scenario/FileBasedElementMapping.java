/*
 * FileBasedElementMapping.java
 * 
 * Created on Jan 27, 2014
 */

package xal.sim.scenario;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.List;

import xal.model.IComponent;
import xal.model.ModelException;
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

	public static ElementMapping loadFrom(URL urlModelConfig) throws ClassNotFoundException {
		DataAdaptor daDoc = XmlDataAdaptor.adaptorForUrl(urlModelConfig, false);
		FileBasedElementMapping elementMapping = new FileBasedElementMapping();
			
	    DataAdaptor daCfg = daDoc.childAdaptor( "configuration" );	    
        DataAdaptor daAssoc = daCfg.childAdaptor("associations");        
        List<DataAdaptor> lstSrcDas = daAssoc.childAdaptors("map");
        for (DataAdaptor daSrc : lstSrcDas) {
            elementMapping.putMap(daSrc.stringValue("smf"), daSrc.stringValue("model"));                      
        }
        
        DataAdaptor daElements = daCfg.childAdaptor("elements");
        
        elementMapping.setDefault(daElements.childAdaptor("default").stringValue("type"));
        elementMapping.setDrift(daElements.childAdaptor("drift").stringValue("type"));
        
        return elementMapping;
	}


	private void setDrift(String stringValue) throws ClassNotFoundException {
		driftElement = (Class<? extends IComponent>) Class.forName(stringValue);
		
	}

	private void setDefault(String stringValue) throws ClassNotFoundException {
		defaultElement = (Class<? extends IComponent>) Class.forName(stringValue);		
	}


	private void putMap(String smf, String model) throws ClassNotFoundException {
		putMap(smf, (Class<? extends IComponent>)Class.forName(model));
	}
}
