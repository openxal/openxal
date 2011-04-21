/*
 * Created on Oct 16, 2003
 */
package xal.smf.proxy;

/**
 * Encapsulates various exceptions encountered by the proxy subsystem, simplifies
 * handling exceptions for subsystem clients.
 * 
 * @author Craig McChesney
 */
public class ProxyException extends Exception {
	
	
	// Constructors ============================================================
	
	protected ProxyException(String msg) {
		super(msg);
	}

}
