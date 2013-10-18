//
//  BorderProxy.java
//  xal
//
//  Created by Thomas Pelaia on 7/12/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import javax.swing.border.*;

import xal.tools.data.*;


/** interface for providing border node behavior */
abstract public class BorderProxy<T extends Border> extends BeanProxy<T> {
	/** data label */
	public static String DATA_LABEL = "BorderProxy";
	
	
	/** Constructor */
	public BorderProxy( final Class<T> prototypeClass ) {
		super( prototypeClass );
	}
	
	
	/** generator */
	static public BorderProxy<Border> getInstance( final DataAdaptor adaptor ) {
		return BorderProxyFactory.getBorderProxy( adaptor.stringValue( "type" ) );
	}
	
	
    /** 
	* Provides the name used to identify the class in an external data source.
	* @return a tag that identifies the receiver's type
	*/
	public String dataLabel() {
		return DATA_LABEL;
	}
}
