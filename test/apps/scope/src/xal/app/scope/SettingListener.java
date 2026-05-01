/*
 * SettingListener.java
 *
 * Created on May 8, 2003, 2:50 PM
 */

package xal.app.scope;

/**
 * SettingListener is an interface used by an object to broadcast that a setting 
 * has changed.
 *
 * @author  tap
 */
interface SettingListener {
    /**
     * A setting from the sender has changed.
     * @param source The object whose setting changed.
     */
    public void settingChanged(Object source);
}
