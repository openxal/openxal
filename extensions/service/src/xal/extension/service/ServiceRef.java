/*
 * ServiceRef.java
 *
 * Created on Mon Oct 13 10:09:59 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.extension.service;

import javax.jmdns.ServiceInfo;
import java.util.regex.*;


/**
 * ServiceRef wraps the native Rendezvous ServiceInfo and provides a reference to the service
 * that hides a direct reference to Rendezvous.
 *
 * @author  tap
 */
public class ServiceRef {
    /** standard local suffix for the service type */
    static final String LOCAL_TYPE_SUFFIX = "_tcp.local.";
    
	/** Pattern matching the protocol and DNS part of the type */
	static final private Pattern TYPE_PATTERN;
	
	/** Property identifying the local service handler */
	static final String SERVICE_KEY = "remote_service_handler";
	
	/** Redezvous service info */
	private ServiceInfo _serviceInfo;
		
	
	static {
		TYPE_PATTERN = Pattern.compile("(\\w++)\\._tcp\\.local\\.");
	}

	
	/**
	 * Create a new service reference to wrap the specified service info.
	 */
	ServiceRef(ServiceInfo serviceInfo) {
		_serviceInfo = serviceInfo;
	}
	
	
	/**
	 * Get the name of the service.
	 * @return The name of the service provided.
	 */
	public String getServiceName() {
		return _serviceInfo.getPropertyString( SERVICE_KEY );
	}
	
	
	/**
	 * Get the unique raw name of the service provider.
	 * @return The raw name of the service provider.
	 */
	public String getRawName() {
		return _serviceInfo.getName();
	}
	
	
	/**
	 * Get the type of service provided.
	 * @return The type of service provided.
	 */
	public String getType() {
		return getBaseType( _serviceInfo.getType() );
	}
	
	
	/**
	 * Get the fully qualified type
	 * @return the fully qualified type
	 */
	public String getFullType() {
		return _serviceInfo.getType();
	}
    
    
    /** get the service address given the service info */
    private static String getHostAddress( final ServiceInfo info ) {
        final String[] hostAddresses = info.getHostAddresses();
        final String hostAddress = hostAddresses != null && hostAddresses.length > 0 ? hostAddresses[0] : null;
		return hostAddress;
    }
	
	
	/**
	 * Get the address of the remote service.
	 * @return the address of the remote service
	 */
	public String getHostAddress() {
        return getHostAddress( _serviceInfo );
	}
	
	
	/**
	 * Get the port for connecting to the remote service.
	 * @return the port for connecting to the remote service
	 */
	public int getPort() {
		return _serviceInfo.getPort();
	}
	
	
	/**
	 * Get the Rendezvous service info which is wrapped by this instance.
	 * @return The wrapped Rendezvous service info.
	 */
	ServiceInfo getServiceInfo() {
		return _serviceInfo;
	}
	
	
	/**
	 * Override equals to compare the wrapped service info instances.
	 * @param other The other service reference against which to compare this one.
	 * @return true if the other service reference refers to the same service provider as the this instance and false otherwise.
	 */
	public boolean equals(Object other) {
		return _serviceInfo.equals( ((ServiceRef)other)._serviceInfo); 
	}


	/** override hashCode as required for consistency with equals */
	public int hashCode() {
		return _serviceInfo.hashCode();
	}
	
	
	/**
	 * Internally used to strip the communication protocol and DNS address from the full type name.
	 * @param fullType The full rendezvous type (e.g. "greeting._tcp._local.")
	 * @return Just the simple base type (e.g. "greeting")
	 */
	static protected String getBaseType( final String fullType ) {
		Matcher matcher = TYPE_PATTERN.matcher(fullType);
		matcher.matches();
		return matcher.group(1);
	}
		
	
	/**
	 * Internally used to strip the type information from the name to get just the base name.
	 * @param fullName  The full rendezvous name (e.g. "Mary.greeting._tcp._local.")
	 * @param fullType  The full rendezvous type (e.g. "greeting._tcp._local.")
	 * @return The simple base name (e.g. "Mary")
	 */
	static protected String getBaseName(final String fullName, final String fullType) {
		int typeIndex = fullName.lastIndexOf(fullType);
		
		return fullName.substring(0, typeIndex-1);
	}
	
	
	/**
	 * Internally used to construct the full bonjour type from the simple base type.
	 * @param baseType The simple base type (e.g. "greeting")
	 * @return The full rendezvous type (e.g. "greeting._tcp.local.")
	 */
	static protected String getFullType( final String baseType ) {
		return "_" + baseType.toLowerCase() + "." + LOCAL_TYPE_SUFFIX;
	}
}

