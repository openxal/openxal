package xal.app.lossviewer.preferences;

import xal.extension.application.*;

import xal.extension.application.smf.*;
import java.awt.*;
import java.net.*;
import java.util.*;

import java.util.List;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.tools.xml.XmlDataAdaptor;

public abstract class AcceleratorDocumentWithPreferences extends AcceleratorDocument
		implements PreferenceListener,PreferenceModel, DataListener {
	
	Map<String, Object> preferences = new HashMap<String,Object>();
	ApplicationPreferences appPrefs;
	
	
	public Object get(String key) {
		Object value = preferences.get(key);
		if (value == null)
			value = appPrefs.get(key);
		return value;
	}
	
	public void put(String key, Object newValue) {
		preferences.put(key,newValue);
		firePreferenceChanged(new PreferenceEvent(key,newValue,this));
	}
	public Map<String, Object> getDocumentPreferences() {
		return preferences;
	}
	
	List<PreferenceListener> listeners = new ArrayList<PreferenceListener>();
	
	public void preferenceChanged(PreferenceEvent pe){
		firePreferenceChanged(pe);
	}
	
	public void firePreferenceChanged(PreferenceEvent preferenceEvent) {
		for (PreferenceListener listener : listeners) {
			listener.preferenceChanged(preferenceEvent);
			
		}
	}
	
	public void addPreferenceListener(PreferenceListener pl){
		listeners.add(pl);
	}
	public void removePreferenceListener(PreferenceListener pl){
		listeners.remove(pl);
	}
	
	public void removeAllPreferenceListeners(){
		listeners.clear();
	}
	
	Rectangle bounds;
	
	public Rectangle getMainWindowBounds(){
		return bounds;
	}
	public void update(DataAdaptor dataAdaptor) {
		
		DataAdaptor windowPosition = dataAdaptor.childAdaptor("Bounds");
		
		bounds = new Rectangle();
		
		bounds.x=windowPosition.intValue("x");
		bounds.y=windowPosition.intValue("y");
		bounds.width=windowPosition.intValue("width");
		bounds.height=windowPosition.intValue("height");
		
		DataAdaptor prefDA = dataAdaptor.childAdaptor("DocumentPreferences");
		if(prefDA==null)
			return;
		List<DataAdaptor> entries = prefDA.childAdaptors();
		for (DataAdaptor entry : entries) {
			if(entry.name().equals("entry")){
				String key = entry.stringValue("key");
				String valueStr = entry.stringValue("value");
				Object value = ObjectConverter.createObjectFromString(valueStr);
				preferences.put(key,value);
			}
			
		}
	}
	
	public void write(DataAdaptor dataAdaptor) {
		DataAdaptor windowPosition = dataAdaptor.createChild("Bounds");
		XalWindow window = getMainWindow();
		Rectangle b = window.getBounds();
		
		windowPosition.setValue("x",b.x);
		windowPosition.setValue("y",b.y);
		windowPosition.setValue("width",b.width);
		windowPosition.setValue("height",b.height);
		
		DataAdaptor prefDA = dataAdaptor.createChild("DocumentPreferences");
		for (String key : preferences.keySet()) {
			DataAdaptor entryDA = prefDA.createChild("entry");
			entryDA.setValue("key",key);
			entryDA.setValue("value",ObjectConverter.convertObjectToString(preferences.get(key)));
		}
		
	}
	
	/**
     * Save the document to the specified URL.
     * @param url The URL to which the document should be saved.
     */
    public void saveDocumentAs(URL url) {
		XmlDataAdaptor xda = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor da = xda.createChild(dataLabel());
		write(da);
		xda.writeToUrl(url);
		setHasChanges(false);
    }
	
	public abstract String dataLabel();
	
	public AcceleratorDocumentWithPreferences() {
		appPrefs = ((ApplicationWithPreferences)Application.getAdaptor()).getPreferences();
		appPrefs.addPreferenceListener(this);
	}
	
	protected void loadFrom(java.net.URL url) {
		setSource(url);
		if(url!=null){
			XmlDataAdaptor xda = XmlDataAdaptor.adaptorForUrl(url,false);
			DataAdaptor da = xda.childAdaptor(dataLabel());
			if(da!=null){
				update(da);
			}
			else {
				da = xda.childAdaptor(dataLabelForPreviousVersion());
				if(da!=null){
					updateFromPreviousVersion(da);
				}
			}
		}
	}
	
	protected String dataLabelForPreviousVersion() {
		return null;
	}
	
	
	protected  void updateFromPreviousVersion(DataAdaptor da){
		
	}
		
	
}
