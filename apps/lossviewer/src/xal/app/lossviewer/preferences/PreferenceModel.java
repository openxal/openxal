package gov.sns.apps.lossviewer2.preferences;

public interface PreferenceModel {
	public void firePreferenceChanged(PreferenceEvent preferenceEvent);
	public void addPreferenceListener(PreferenceListener pl);
	public void removePreferenceListener(PreferenceListener pl);
	public void removeAllPreferenceListeners();

}
