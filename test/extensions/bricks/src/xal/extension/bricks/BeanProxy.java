//
//  BeanProxy.java
//  xal
//
//  Created by Thomas Pelaia on 7/11/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;

import xal.tools.data.*;


/** proxy for generating a Java Bean object */
abstract public class BeanProxy<ViewType> implements DataListener {
	/** prototype class */
	final protected Class<ViewType> PROTOTYPE_CLASS;
	
	
	/** Constructor */
	public BeanProxy( final Class<ViewType> prototypeClass ) {
		PROTOTYPE_CLASS = prototypeClass;
	}
	
	
	/** Create an instance of the specified view */
	public ViewType getBeanInstance( final Class<ViewType> theClass ) {
		try {
			final Constructor<ViewType> constructor = theClass.getConstructor( getConstructorParameterTypes() );
			final Object[] parameters = getConstructorParameters();
			return getBeanInstance( theClass, constructor, parameters );
		}
		catch( Exception exception ) {
			throw new RuntimeException( "Can't instantiate class:  " + theClass.toString() );
		}
	}
	
	
	/** Create an instance of the specified view */
	public ViewType getBeanInstance( final Class<ViewType> theClass, final Constructor<ViewType> constructor, Object... parameters ) {
		try {
			constructor.setAccessible( true );
			final ViewType object = constructor.newInstance( parameters );
			setup( object );
			return object;
		}
		catch( Exception exception ) {
			exception.printStackTrace();
			throw new RuntimeException( "Can't instantiate class:  " + theClass.toString(), exception );
		}
	}
	
	
	/** setup the instance after construction */
	public void setup( final ViewType object ) {}
	
	
	/** setup the instance after construction with prototype data */
	public void setupPrototype( final ViewType object ) {}
	
	
	/** Get the class of the view */
	final public Class<ViewType> getPrototypeClass() {
		return PROTOTYPE_CLASS;
	}
	
	
	/**
	 * Get the prototype view
	 * @return the prototype view
	 */
	final public ViewType getPrototype() {
		final ViewType object = getBeanInstance( PROTOTYPE_CLASS );
		setupPrototype( object );
		return object;
	}
	
	
	/**
	 * Get the array of constructor arguments
	 * @return the constructor arguments
	 */
	@SuppressWarnings( "rawtypes" )		// generics don't mix with arrays
	public Class[] getConstructorParameterTypes() {
		return new Class[0];
	}
	
	
	/**
	 * Get the array of constructor arguments
	 * @return the constructor arguments
	 */
	public Object[] getConstructorParameters() {
		return new Object[0];
	}
	
	
	/** Get an icon representation for the view */
	public Icon getIcon()  {
		return null;
	}
	
	
	/** get the name of the prototype */
	public String getType() {
		return PROTOTYPE_CLASS.getName();
	}
	
	
	/** get the name of the prototype */
	public String getName() {
		return PROTOTYPE_CLASS.getName();
	}
	
	
	/** get the short name of the prototype */
	public String getShortName() {
		final String[] words = getName().split( "\\W" );
		return words[ words.length - 1 ];
	}
	
	
	/** Get a textual representation of the view  */
	public String getText() {
		return PROTOTYPE_CLASS.getName();
	}
	
	
	/** get the jython reference snippet */
	public String getJythonReferenceSnippet( final BeanNode<?> node ) {
		final String symbol = node.getTag().toLowerCase().replaceAll( " ", "_" );	// lower the case of the tag and replace spaces with underscores
		
		final StringBuffer buffer = new StringBuffer();
		buffer.append( symbol );
		buffer.append( " = " );
		buffer.append( "window_reference." );
		buffer.append( getReferenceSnippetFetchMethodName() );
		buffer.append( "(" );
		buffer.append( getReferenceSnippetFetchMethodArgumentsString( node ) );
		buffer.append( ")" );
		
		return buffer.toString();
	}
		
	
	/** get the java reference snippet */
	public String getJavaReferenceSnippet( final BeanNode<?> node ) {
		final StringBuffer buffer = new StringBuffer();
		buffer.append( "final " );
		buffer.append( node.getShortClassName() );
		buffer.append( " " );
		//buffer.append( getShortName() );
		buffer.append( generateJavaReferenceSymbol( node ) );
		buffer.append( " = (" );
		buffer.append( node.getShortClassName() );
		buffer.append( ")windowReference." );
		buffer.append( getReferenceSnippetFetchMethodName() );
		buffer.append( "(" );
		buffer.append( getReferenceSnippetFetchMethodArgumentsString( node ) );
		buffer.append( ");" );
		
		return buffer.toString();
	}
	
	
	/** Generate the Java symbol for the specified node by lowering the case of the first character, stripping whitespace and capitalizing the first word character */
	private static String generateJavaReferenceSymbol( final BeanNode<?> node ) {
		final String tag = node.getTag();
		final int tagLength = tag.length();
		final StringBuffer buffer = new StringBuffer();
		
		// lower the case of the first character of the symbol
		buffer.append( Character.toLowerCase( tag.charAt( 0 ) ) );
		
		boolean nextCharBeginsWord = false;		// indicates whether the next character begins a word
		for ( int index = 1; index < tagLength ; index++ ) {
			final char theCharacter = tag.charAt( index );
			if ( Character.isWhitespace( theCharacter ) ) {		// whitespace separates words
				nextCharBeginsWord = true;	// space indicates that next character begins a new word
			}
			else {
				if ( nextCharBeginsWord ) {
					buffer.append( Character.toUpperCase( theCharacter ) );		// capitalize the first character of the word
				}
				else {
					buffer.append( theCharacter );	// simply append the character if it is part of word and not the first character
				}
				nextCharBeginsWord = false;
			}
		}
		
		return buffer.toString();
	}
	
	
	/** get the java reference snippet */
	public String getXALReferenceSnippet( final BeanNode<ViewType> node ) {
		return getJavaReferenceSnippet( node );
	}
	
	
	/**
	 * Get the java declaration snippet
	 * @return the java declaration snippet
	 */
	public String getJavaDeclarationSnippet( final BeanNode<ViewType> node ) {
		final StringBuffer buffer = new StringBuffer();
		buffer.append( node.getShortClassName() );
		buffer.append( " " );
		buffer.append( generateJavaReferenceSymbol( node ) );
		buffer.append( ";" );
		
		return buffer.toString();
	}
	
	
	/**
	 * Get the reference snippet method name
	 * @return the method name
	 */
	protected String getReferenceSnippetFetchMethodName() {
		return "getView";
	}
	
	
	/**
	 * Get the reference snippet method arguments
	 * @return the method arguments
	 */
	protected String getReferenceSnippetFetchMethodArgumentsString( final BeanNode<?> node ) {
		return " \"" + node.getTag() + "\" ";
	}
	
	
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
	}
    
    
    /**
	 * Write data to the data adaptor for storage.
     * @param adaptor The adaptor to which the receiver's data is written
     */
    public void write( final DataAdaptor adaptor ) {
		adaptor.setValue( "type", getType() );
	}
	
	
	/** get string representation */
	public String toString() {
		return getName();
	}
}
