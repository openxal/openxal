//
//  BorderProxyFactory.java
//  xal
//
//  Created by Thomas Pelaia on 7/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import javax.swing.border.*;
import java.util.Map;
import java.util.HashMap;


/** Factory for generating border proxies */
public class BorderProxyFactory {
	/** table of proxies keyed by type */
	static protected Map<String,BorderProxy<Border>> PROXY_TABLE;
	
	
	// static initializer
	static {
		PROXY_TABLE = new HashMap<String,BorderProxy<Border>>();
		
		register( getBorderProxy( EtchedBorder.class, "Etched Border" )  );
		register( getLoweredBevelBorderProxy( "Lowered Bevel" ) );
		register( getRaisedBevelBorderProxy( "Raised Bevel" ) );
		register( getTitledBorderProxy( "Titled Border" ) );
	}
	
	
	/** register the proxy in the proxy table */
	@SuppressWarnings( "unchecked" )	// must convert border proxy subtypes to the subtype of Border
	static protected void register( final BorderProxy<? extends Border> proxy ) {
		PROXY_TABLE.put( proxy.getType(), (BorderProxy<Border>)proxy );
	}
	
	
	/** get a border proxy with the specified type */
	static public BorderProxy<Border> getBorderProxy( final String type ) {
		if ( PROXY_TABLE.containsKey( type ) ) {
			return PROXY_TABLE.get( type );
		}
		else {
			final String swingType = "javax.swing.border." + type;
			return PROXY_TABLE.get( swingType );
		}
	}
	
	
	/** Create a border proxy for a border with an empty constructor */
	static public <T extends Border> BorderProxy<T> getBorderProxy( final Class<T> borderClass, final String name ) {
		return new BorderProxy<T>( borderClass ) {			
			public String getName() {
				return name;
			}
		};
	}
	
	
	/** Create a title border proxy */
	static public BorderProxy<TitledBorder> getTitledBorderProxy( final String name ) {
		return new BorderProxy<TitledBorder>( TitledBorder.class ) {			
			/** Get the array of constructor arguments */
			public Class<?>[] getConstructorParameterTypes() {
				return new Class<?>[] { String.class };
			}
			
			
			/** Get the array of constructor arguments */
			public Object[] getConstructorParameters() {
				return new Object[] { "title" };
			}
			
			
			public String getName() {
				return name;
			}
		};
	}
	
	
	/** Create a title border proxy */
	static public BorderProxy<BevelBorder> getLoweredBevelBorderProxy( final String name ) {
		return new BorderProxy<BevelBorder>( BevelBorder.class ) {
			/** Get the array of constructor arguments */
			public Class<?>[] getConstructorParameterTypes() {
				return new Class<?>[] { Integer.TYPE };
			}
			
			
			/** Get the array of constructor arguments */
			public Object[] getConstructorParameters() {
				return new Object[] { BevelBorder.LOWERED };
			}
			
			
			public String getName() {
				return name;
			}
			
			
			/** get the type of the prototype */
			public String getType() {
				return "javax.swing.border.BevelBorder_Lowered";
			}
		};
	}
	
	
	/** Create a title border proxy */
	static public BorderProxy<BevelBorder> getRaisedBevelBorderProxy( final String name ) {
		return new BorderProxy<BevelBorder>( BevelBorder.class ) {
			/** Get the array of constructor arguments */
			public Class<?>[] getConstructorParameterTypes() {
				return new Class<?>[] { Integer.TYPE };
			}
			
			
			/** Get the array of constructor arguments */
			public Object[] getConstructorParameters() {
				return new Object[] { BevelBorder.RAISED };
			}
			
			
			public String getName() {
				return name;
			}
			
			
			/** get the type of the prototype */
			public String getType() {
				return "javax.swing.border.BevelBorder_Raised";
			}
		};
	}
}
