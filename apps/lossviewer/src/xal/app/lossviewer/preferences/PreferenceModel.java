package xal.app.lossviewer.preferences;

public interface PreferenceModel {
	public void firePreferenceChanged(PreferenceEvent preferenceEvent);
	public void addPreferenceListener(PreferenceListener pl);
	public void removePreferenceListener(PreferenceListener pl);
	public void removeAllPreferenceListeners();

}
