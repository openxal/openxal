//
// Coder.java
// 
//
// Created by Tom Pelaia on 1/12/12
// Copyright 2012 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding;

import java.util.List;


/** Declares methods a coder should implement. */
public interface Coder {
	/** Encode an object */
    public String encode( final Object value );
    
    
	/** Decode the archive */
    public Object decode( final String archive );
    
    
    /** Register the custom type by class and its associated adaptor  */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor );
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes();
}
