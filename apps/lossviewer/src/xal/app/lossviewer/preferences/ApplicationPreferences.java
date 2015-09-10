package xal.app.lossviewer.preferences;

import xal.extension.application.*;
import java.util.*;
import java.util.prefs.*;

public class ApplicationPreferences implements PreferenceModel{
	private PreferencesDialog dialog;
	private Preferences userPrefs;
	private Map<String,Object> defaultPreferences;
	private Map<String,Object> appPreferences;
	
	private ApplicationWithPreferences appAdaptor;
	
	public Object get(String key) {
		
		return appPreferences.get(key);
	}
	public Map<String,Object> getPreferencesFor(String identifier){
		Map<String,Object> result = new HashMap<String,Object>();
		for (String prefname : appPreferences.keySet()) {
			if(prefname.startsWith(identifier)){
				result.put(prefname,appPreferences.get(prefname));
			}
		}
		return result;
	}
	
	
	public String[] getKeys() {
		
		return appPreferences.keySet().toArray(new String[]{});
	}
	
	public void showPreferenceDialog(AcceleratorDocumentWithPreferences doc) {
		dialog.showPreferenceDialog(doc);
	}
	
	public ApplicationPreferences(ApplicationWithPreferences app) {
		this.appAdaptor = app;
		initializePreferences();
	}
	
	public void initializePreferences() {
		
		defaultPreferences = new HashMap<String,Object>();
		appPreferences = new HashMap<String,Object>();
		
		//	Map prefs = Util.loadResourceBundle(Application.getApp().getAdaptor().getPathToResource("defaults"));
		//Map<String,String> prefs = Util.loadResourceBundle(appAdaptor.getClass().getPackage().getName() + ".resources.defaults");
		Map<String,String> prefs = Util.loadResourceBundle( appAdaptor.getResourceURL( "defaults.properties" ) );

		
		for (String key : prefs.keySet()) {
			
			Object value = ObjectConverter.createObjectFromString(prefs.get(key));
//			System.out.println(key + " " + value);
			appPreferences.put(key, value);
			defaultPreferences.put(key, value);
		}
		userPrefs = Preferences.userNodeForPackage(this.getClass());
		
		//overwrite default prefs with user preferences
//		System.out.println("Overwriting with system prefs");
		for (Object e : prefs.keySet()) {
			String key = (String)e;
			Object value = userPrefs.get(key, null);
			
			if (value != null) {
				appPreferences.put(key, value);
			//	System.out.println(key + " " + value);
			}
			
		}
		
		
		dialog = new PreferencesDialog(this);
	}
	
	public void updateUserPreferences(Map<String,Object> documentPrefs) {
		for (String key : documentPrefs.keySet()) {
			Object defaultValue = defaultPreferences.get(key);
			Object newValue = documentPrefs.get(key);
			Object currentValue = appPreferences.get(key);
			if (defaultValue == null || newValue == null||newValue.equals(currentValue))
				continue;
			
			if(defaultValue.equals(newValue)){
				userPrefs.remove(key);
			}
			else {
				userPrefs.put(key, ObjectConverter.convertObjectToString(newValue));
			}
			appPreferences.put(key,newValue);
			firePreferenceChanged(new PreferenceEvent(key,newValue,this));
		//	System.out.println(defaultValue + " updated to " + newValue);
		}
		
		try {
			userPrefs.flush();
		}
		catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	List<PreferenceListener> listeners = new ArrayList<PreferenceListener>();
	
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
}
