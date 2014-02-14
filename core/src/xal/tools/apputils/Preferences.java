package xal.tools.apputils;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.prefs.BackingStoreException;
import java.util.prefs.NodeChangeListener;
import java.util.prefs.PreferenceChangeListener;

public class Preferences extends java.util.prefs.Preferences {
	protected java.util.prefs.Preferences usr, sys;
	protected Set<String> usrKeys, sysKeys;

	protected Preferences(java.util.prefs.Preferences usr, java.util.prefs.Preferences sys)
	{
		this.usr = usr;
		this.sys = sys;
	}	
	
	protected boolean useUser(String key)
	{
		if (usrKeys == null) {
			try {
				sysKeys = new HashSet<String>(Arrays.asList(sys.keys()));			
			} catch (BackingStoreException e) {
				e.printStackTrace();
				sysKeys = new HashSet<>();
			}
			try {
				usrKeys = new HashSet<String>(Arrays.asList(usr.keys()));			
			} catch (BackingStoreException e) {
				e.printStackTrace();
				usrKeys = new HashSet<>();
			}
		}
		return usrKeys.contains(key) || !sysKeys.contains(key);
	}
	
	@Override
	public String absolutePath() {
		return usr.absolutePath();
	}

	@Override
	public void addNodeChangeListener(NodeChangeListener ncl) {
		usr.addNodeChangeListener(ncl);
		
	}

	@Override
	public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
		usr.addPreferenceChangeListener(pcl);
		
	}

	@Override
	public String[] childrenNames() throws BackingStoreException {
		return usr.childrenNames();
	}

	@Override
	public void clear() throws BackingStoreException {
		usr.clear();	
		usrKeys = null;
	}

	@Override
	public void exportNode(OutputStream os) throws IOException,
			BackingStoreException {
		usr.exportNode(os);
		
	}

	@Override
	public void exportSubtree(OutputStream os) throws IOException,
			BackingStoreException {
		usr.exportSubtree(os);
		
	}

	@Override
	public void flush() throws BackingStoreException {
		usr.flush();		
	}

	@Override
	public String get(String key, String def) {
		return useUser(key) ? usr.get(key, def) : sys.get(key, def);
	}

	@Override
	public boolean getBoolean(String key, boolean def) {
		return useUser(key) ? usr.getBoolean(key, def) : sys.getBoolean(key, def);
	}

	@Override
	public byte[] getByteArray(String key, byte[] def) {
		return useUser(key) ? usr.getByteArray(key, def) : sys.getByteArray(key, def);
	}

	@Override
	public double getDouble(String key, double def) {
		return useUser(key) ? usr.getDouble(key, def) : sys.getDouble(key, def);
	}

	@Override
	public float getFloat(String key, float def) {
		return useUser(key) ? usr.getFloat(key, def) : sys.getFloat(key, def);
	}

	@Override
	public int getInt(String key, int def) {
		return useUser(key) ? usr.getInt(key, def) : sys.getInt(key, def);
	}

	@Override
	public long getLong(String key, long def) {
		return useUser(key) ? usr.getLong(key, def) : sys.getLong(key, def);
	}

	@Override
	public boolean isUserNode() {
		return true;
	}

	@Override
	public String[] keys() throws BackingStoreException {
		return usr.keys();
	}

	@Override
	public String name() {
		return usr.name();
	}

	@Override
	public java.util.prefs.Preferences node(String pathName) {
		return new Preferences(usr.node(pathName), sys.node(pathName));
	}

	@Override
	public boolean nodeExists(String pathName) throws BackingStoreException {
		return usr.nodeExists(pathName) || sys.nodeExists(pathName);
	}

	@Override
	public java.util.prefs.Preferences parent() {
		return new Preferences(usr.parent(), sys.parent());
	}

	@Override
	public void put(String key, String value) {
		usr.put(key, value);		
	}

	@Override
	public void putBoolean(String key, boolean value) {
		usr.putBoolean(key, value);		
	}

	@Override
	public void putByteArray(String key, byte[] value) {
		usr.putByteArray(key, value);
	}

	@Override
	public void putDouble(String key, double value) {
		usr.putDouble(key, value);		
	}

	@Override
	public void putFloat(String key, float value) {
		usr.putFloat(key, value);
	}

	@Override
	public void putInt(String key, int value) {
		usr.putInt(key, value);
	}

	@Override
	public void putLong(String key, long value) {
		usr.putLong(key, value);
	}

	@Override
	public void remove(String key) {
		usr.remove(key);	
		usrKeys = null;
	}

	@Override
	public void removeNode() throws BackingStoreException {
		usr.removeNode();		
		usrKeys = null;
	}

	@Override
	public void removeNodeChangeListener(NodeChangeListener ncl) {
		usr.removeNodeChangeListener(ncl);
	}

	@Override
	public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
		usr.removePreferenceChangeListener(pcl);		
	}

	@Override
	public void sync() throws BackingStoreException {
		usr.sync();		
		usrKeys = null;
	}

	@Override
	public String toString() {
		return usr.toString();
	}
	
	public static java.util.prefs.Preferences nodeForPackage(Class<?> c)
	{
		return new Preferences(Preferences.userNodeForPackage(c),
				Preferences.systemNodeForPackage(c));
	}
}
