//
//  BeanNode.java
//  xal
//
//  Created by Thomas Pelaia on 7/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.lang.reflect.*;
import java.util.*;

import xal.tools.data.*;


/** brick which represents a Java Bean */
abstract public class BeanNode<T> extends Brick implements DataListener {
	/** data label from bean properties */
	final static protected String BEAN_DATA_LABEL = "BeanProperty";
	
	/** bean object */
	final protected T BEAN_OBJECT;
	
	/** the bean proxy */
	final protected BeanProxy<T> BEAN_PROXY;
	
	/** bean settings */
	final protected Map<String,Object> BEAN_SETTINGS;
	
	/** tag for identifying this node */
	protected String _tag;
	
	/** custom bean class name */
	protected String _customBeanClassName;
	
	
	/** Primary Constructor */
	public BeanNode( final BeanProxy<T> beanProxy, final Map<String,Object> beanSettings, final String tag ) {
		BEAN_PROXY = beanProxy;
		BEAN_OBJECT = getPrototypeBean( beanProxy );
		BEAN_SETTINGS = new HashMap<String,Object>();
		
		_tag = tag;
		_customBeanClassName = null;
		
		if ( beanSettings != null ) {
			BEAN_SETTINGS.putAll( beanSettings );
			applyBeanSettings();
		}
	}
	
	
	/** Constructor */
	public BeanNode( final BeanNode<T> node ) {
		this( node.BEAN_PROXY, node.BEAN_SETTINGS, node.getTag() );
		
		setCustomBeanClassName( node.getCustomBeanClassName() );
	}
	
	
	/** Constructor */
	public BeanNode( final BeanProxy<T> beanProxy ) {
		this( beanProxy, null, beanProxy.getName() );
	}
	
	
	/**
	 * Get the view proxy
	 * @return the view proxy
	 */
	public BeanProxy<T> getBeanProxy() {
		return BEAN_PROXY;
	}
	
	
	/** get the bean instance */
	protected T getPrototypeBean( final BeanProxy<T> beanProxy ) {
		return beanProxy.getPrototype();
	}
	
	
	/** Get the bean object */
	public T getBeanObject() {
		return BEAN_OBJECT;
	}
	
	
	/**
	 * Get the bean info of the view.
	 * Get the view's bean info
	 */
	public BeanInfo getBeanObjectBeanInfo() {
		try {
			return Introspector.getBeanInfo( BEAN_OBJECT.getClass() );
		}
		catch( IntrospectionException exception ) {
			return null;
		}
	}
	
	
	/**
	 * Get this node's tag
	 * @return this node's tag
	 */
	public String getTag() {
		return _tag;
	}
	
	
	/**
	 * Set this node's tag
	 * @param tag the new tag
	 */
	public void setTag( final String tag ) {
		_tag = tag;
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * Determine whether this node has a custom bean class
	 * @return true if this node has a custom bean class and false if not
	 */
	public boolean hasCustomBeanClass() {
		return _customBeanClassName != null;
	}
	
	
	/**
	 * Get this node's custom bean class name
	 * @return this node's custom bean class name
	 */
	public String getCustomBeanClassName() {
		return _customBeanClassName;
	}
	
	
	/**
	 * Set this node's custom bean class name
	 * @param name the new custom bean class name
	 */
	public void setCustomBeanClassName( final String name ) {
		_customBeanClassName = name;
		EVENT_PROXY.treeNeedsRefresh( this, this );
	}
	
	
	/**
	 * get the fully qualified class name 
	 * @return the custom class name if it exists or the prototype class name if there is no custom class
	 */
	public String getClassName() {
		return hasCustomBeanClass() ? getCustomBeanClassName() : BEAN_PROXY.getPrototypeClass().getName();
	}
	
	
	/**
	 * get the short version of the class name 
	 * @return the short version of the class name
	 */
	public String getShortClassName() {
		final String[] words = getClassName().split( "\\W" );
		return words[ words.length - 1 ];
	}
	
	
	/** get the jython reference snippet */
	public String getJythonReferenceSnippet() {
		return BEAN_PROXY.getJythonReferenceSnippet( this );
	}
	
	
	/** get the java reference snippet */
	public String getJavaReferenceSnippet() {
		return BEAN_PROXY.getJavaReferenceSnippet( this );
	}
	
	
	/** get the java reference snippet */
	public String getXALReferenceSnippet() {
		return BEAN_PROXY.getXALReferenceSnippet( this );
	}
	
	
	/**
	 * Get the java declaration snippet
	 * @return the java declaration snippet
	 */
	public String getJavaDeclarationSnippet() {
		return BEAN_PROXY.getJavaDeclarationSnippet( this );
	}
		
	
	/** refresh display */
	public void refreshDisplay() {}
	
	
	/** apply the bean settings */
	protected void applyBeanSettings() {
		final Map<String,PropertyDescriptor> descriptorTable = getProperyDescriptorTable();
		
		final Iterator<String> nameIter = BEAN_SETTINGS.keySet().iterator();
		while( nameIter.hasNext() ) {
			final String name = nameIter.next();
			final PropertyDescriptor descriptor = descriptorTable.get( name );
			final Object value = BEAN_SETTINGS.get( name );
			try {
				setPropertyValue( descriptor, value );
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}
		}
	}
	
	
	/** Get the property descriptor table keyed by property name */
	protected Map<String,PropertyDescriptor> getProperyDescriptorTable() {
		final BeanInfo beanInfo = getBeanObjectBeanInfo();
		final PropertyDescriptor[] descriptors = beanInfo != null ? beanInfo.getPropertyDescriptors() : new PropertyDescriptor[0];
		final Map<String,PropertyDescriptor> descriptorTable = new HashMap<String,PropertyDescriptor>( BEAN_SETTINGS.size() );
		for ( final PropertyDescriptor descriptor : descriptors ) {
			descriptorTable.put( descriptor.getName(), descriptor );
		}
		return descriptorTable;
	}
	
	
	/** get the property value */
	public Object getPropertyValue( final PropertyDescriptor propertyDescriptor ) throws Exception {
		final Method method = propertyDescriptor.getReadMethod();
		return method != null ? method.invoke( BEAN_OBJECT ) : null;
	}
	
	
	/** update the property with the specified value */
	public void setPropertyValue( final PropertyDescriptor propertyDescriptor, final Object value ) {
		final Method method = propertyDescriptor.getWriteMethod();
		
		try {
			method.invoke( BEAN_OBJECT, value );
		}
		catch ( InvocationTargetException exception ) {
			if ( exception.getCause() != null ) {
				throw new RuntimeException( exception.getCause() );
			}
			else {
				throw new RuntimeException( exception );
			}
		}
		catch ( IllegalAccessException exception ) {
			throw new RuntimeException( exception );
		}
		
		final String name = propertyDescriptor.getName();
		BEAN_SETTINGS.put( name, value );
		EVENT_PROXY.propertyChanged( this, propertyDescriptor, value );
		refreshDisplay();
	}
	
	
	/**
	 * Get the containing node
	 * @return the parent view node
	 */
	public ViewNodeContainer getViewNodeContainer() {
		final Object parent = getParent();
		return parent instanceof ViewNodeContainer ? (ViewNodeContainer)parent : null;
	}
	
	
	/** Display the bean's window */
	public void display() {}
	
	
    /** 
	 * Provides the name used to identify the class in an external data source.
	 * @return a tag that identifies the receiver's type
	 */
    abstract public String dataLabel();
    
    
    /**
	 * Update the data based on the information provided by the data provider.
     * @param adaptor The adaptor from which to update the data
     */
    public void update( final DataAdaptor adaptor ) {
		if ( adaptor.hasAttribute( "customBeanClass" ) ) {
			setCustomBeanClassName( adaptor.stringValue( "customBeanClass" ) );
		}
		
		final PropertyValueEditorManager editorManager = PropertyValueEditorManager.getDefaultManager();
		final Map<String,PropertyDescriptor> descriptorTable = getProperyDescriptorTable();
		final List<DataAdaptor> beanAdaptors = adaptor.childAdaptors( BEAN_DATA_LABEL );
		for ( final DataAdaptor beanAdaptor : beanAdaptors ) {
			try {
				beanAdaptor.setValue( "contextURL", adaptor.stringValue( "contextURL" ) );
				final String name = beanAdaptor.stringValue( "name" );
				final PropertyDescriptor propertyDescriptor = descriptorTable.get( name );
				final Class<?> propertyType = propertyDescriptor.getPropertyType();
				final PropertyValueEditor<?> propertyEditor = editorManager.getEditor( propertyType );
				final Object value = propertyEditor.readValue( beanAdaptor );
				setPropertyValue( propertyDescriptor, value );
			}
			catch( Exception exception ) {
				exception.printStackTrace();
			}
		}
	}
    
    
    /**
		* Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "tag", _tag );
		
		if ( _customBeanClassName != null ) {
			adaptor.setValue( "customBeanClass", _customBeanClassName );
		}
		
		adaptor.writeNode( BEAN_PROXY );
				
		final Set<Map.Entry<String,Object>> settings = BEAN_SETTINGS.entrySet();
		for ( final Map.Entry<String,Object> setting : settings ) {
			final String name = setting.getKey();
			final Object value = setting.getValue();
			adaptor.writeNode( getPropertyArchiver( name, value ) );
		}
	}
	
	
	/** get the archiver of bean propertiers */
	static public DataListener getPropertyArchiver( final String name, final Object value ) {
		return new DataListener() {
			/** 
			* Provides the name used to identify the class in an external data source.
			* @return a tag that identifies the receiver's type
			*/
			public String dataLabel() {
				return BEAN_DATA_LABEL;
			}
			
			
			/**
			 * Update the data based on the information provided by the data provider.
			 * @param adaptor The adaptor from which to update the data
			 */
			public void update( final DataAdaptor adaptor ) {
			}
			
			
			/**
			 * Write data to the data adaptor for storage.
			 * @param adaptor The adaptor to which the receiver's data is written
			 */
			public void write( final DataAdaptor adaptor ) {
				final PropertyValueEditor<?> editor = PropertyValueEditorManager.getDefaultManager().getEditor( value.getClass() );
				editor.writeValue( name, value, adaptor );
			}			
		};
	}
	
	
	/** get a label */
	public String toString() {
		return _tag;
	}	
}
