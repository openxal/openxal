/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.util.List;

import xal.smf.AcceleratorNode;

/**
 * Specifies interface for property accessors that return acclerator node
 * property values.
 * 
 * @author Craig McChesney
 */
public interface PropertyAccessor {
	
	// Abstract Interface ======================================================
	
	double doubleValueFor(AcceleratorNode node, String property, String mode)
		throws ProxyException;
	
	List propertyNames();

}
