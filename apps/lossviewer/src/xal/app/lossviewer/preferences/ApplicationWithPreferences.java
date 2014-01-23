package gov.sns.apps.lossviewer2.preferences;

import gov.sns.application.*;

abstract public class ApplicationWithPreferences extends ApplicationAdaptor {
	private ApplicationPreferences preferences;
	
	
	public ApplicationWithPreferences(){
		initializePreferences();
	}
	
	protected void initializePreferences() {
		preferences = new ApplicationPreferences(this);
	}
	public void editPreferences(final XalDocument doc) {
		try {
			
			preferences.showPreferenceDialog((AcceleratorDocumentWithPreferences)doc);
			
		}
		catch (ClassCastException cce) {
			
		}
	}
	
	public ApplicationPreferences getPreferences(){
		return preferences;
	}
	
	
	
	
}
