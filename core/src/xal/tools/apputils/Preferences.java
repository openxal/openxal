package xal.tools.apputils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class Preferences extends java.util.prefs.AbstractPreferences {
	protected java.util.prefs.Preferences userPrefs;
	protected Properties sysPrefs;
	protected Set<String> usrKeys;
	
	protected Preferences(java.util.prefs.Preferences userPrefs, String name) {
		super(parentPrefs(userPrefs), name);
		this.userPrefs = userPrefs;
	}

	private static Preferences parentPrefs(java.util.prefs.Preferences userPrefs) {
		java.util.prefs.Preferences parentPrefs = userPrefs.parent();
		if (parentPrefs == null) return null;
		return new Preferences(parentPrefs, parentPrefs.name());
	}

	@Override
	protected AbstractPreferences childSpi(String name) {
		java.util.prefs.Preferences child = userPrefs.node(name);
		return new Preferences(child, child.name());
	}

	@Override
	protected String[] childrenNamesSpi() throws BackingStoreException {
		return userPrefs.childrenNames();
	}

	@Override
	protected void flushSpi() throws BackingStoreException {
		userPrefs.flush();
	}
	
	private boolean usrContains(String key) {
		String keys[];
		try {
			keys = userPrefs.keys();
			for (String userKey : keys)
				if (key.equals(userKey)) return true;
		} catch (BackingStoreException e) {
		}
		return false;
	}

	protected String fullName() {
		if ("".equals(name())) return "xal";
		Preferences parent = (Preferences)parent();
		return parent == null || "".equals(parent.name()) ? name() : parent.fullName() + "." + name();
	}
	
	protected Properties getSysPrefs()
	{
		if (sysPrefs == null) {
			String confDir = System.getenv("OPENXAL_CONFIG_DIR");
			if (confDir == null) confDir = "/etc/openxal";
			File confFile = new File(confDir + "/" + fullName());
			//System.out.printf("%s\n", confFile.toString());
			if (confFile.exists()) {
				sysPrefs = new Properties();
				try {
					sysPrefs.load(new FileReader(confFile));
				} catch (IOException e) {
				}
			}
		}
		return sysPrefs;
	}
	
	@Override
	protected String getSpi(String key) {
		if (usrContains(key) || !getSysPrefs().containsKey(key)) return userPrefs.get(key, null);
		return getSysPrefs().getProperty(key);
	}

	@Override
	protected String[] keysSpi() throws BackingStoreException {
		Set<String> keys = getSysPrefs().stringPropertyNames();
		keys.addAll(Arrays.asList(userPrefs.keys()));
		return keys.toArray(new String[keys.size()]);
	}

	@Override
	protected void putSpi(String key, String value) {
		userPrefs.put(key, value);
	}

	@Override
	protected void removeNodeSpi() throws BackingStoreException {
		userPrefs.removeNode();
	}

	@Override
	protected void removeSpi(String key) {
		userPrefs.remove(key);
	}

	@Override
	protected void syncSpi() throws BackingStoreException {
		userPrefs.sync();
	}
	
	public static java.util.prefs.Preferences nodeForPackage(Class<?> c)
	{
		java.util.prefs.Preferences userPrefs = Preferences.userNodeForPackage(c);
		return new Preferences(userPrefs, userPrefs.name());
	}
	
	/*public static void main(String[] args) {
		java.util.prefs.Preferences prefs = nodeForPackage(xal.sim.scenario.Scenario.class);
		System.out.printf("%s\n", prefs.get("configURL", "blah"));
	}*/
}
