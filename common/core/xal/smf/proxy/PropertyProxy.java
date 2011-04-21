/*
 * Created on Oct 23, 2003
 */
package xal.smf.proxy;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Acts as a proxy for some accelerator node's property (e.g., a magnet's field
 * or an RF gap's frequency).
 * 
 * @author Craig McChesney
 */
public class PropertyProxy {
	
	// Instance variables ======================================================
	
	private Method sourceAccessor;
	private double scalingFactor = 1.0; //hack for unit conversion
	private boolean makePositive = false; // apply abs before returning value?
	
	
	// Constructors ============================================================
	
	protected PropertyProxy(Class aClass, String accessor) {
		initMethod(aClass, accessor);
	}
	
	protected PropertyProxy(Class aClass, String accessor, boolean absFlag) {
		makePositive = absFlag;
		initMethod(aClass, accessor);
	}
	
	protected PropertyProxy(Class aClass, String accessor, double scale) {
		scalingFactor = scale;
		initMethod(aClass, accessor);
	}
	
	
	// Proxy Operations ========================================================
	
	public double doubleValueFor(Object source) throws ProxyException {
		if (source == null)
			throw new IllegalArgumentException( "null source not allowed in doubleValueFor" );
		Double val = null;
		try {
			val = (Double) sourceAccessor.invoke(source, (Object[])null);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			throw new ProxyException("IllegalArgumentException getting proxy value");
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			throw new ProxyException("IllegalAccessException getting proxy value");
		} catch (InvocationTargetException e) {
			e.getCause().printStackTrace();
			throw new ProxyException("InvocationTargetException getting proxy value");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new ProxyException("error getting proxy value: " + t.getMessage());
		}
		
		// return the scaled value
		double retval = val.doubleValue() * scalingFactor;
		if (makePositive) retval = Math.abs(retval);
		return retval;
	}
	
	// Private Support =========================================================
	
	private void initMethod(Class objClass, String methodName) {
		Method method = null;
		try {
			method = objClass.getMethod(methodName, (Class[])null);
		} catch (SecurityException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("unable to access method: " + methodName);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			throw new IllegalArgumentException("no such method: " + methodName);
		}
		sourceAccessor = method;
	}
	

}
