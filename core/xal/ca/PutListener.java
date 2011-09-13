/*
 * PutListener.java
 *
 * Created on September 18, 2002, 12:31 PM
 */

package xal.ca;

/**
 * PutListener is an interface for a put event listener.
 *
 * @author  tap
 */
public interface PutListener {
    public void putCompleted(Channel chan);
}
