/*
 * SettingListener.java
 *
 * Created on May 8, 2003, 2:50 PM
 */

package xal.app.rocs;

/**
 * SettingListener is an interface used by an object to broadcast 
 * that a setting has changed.
 *
 * @author  tap
 */
interface SettingListener {
    public void settingChanged(Object source);
}
