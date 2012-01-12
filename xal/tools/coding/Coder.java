//
// ConversionAdaptor.java
// 
//
// Created by Tom Pelaia on 12/27/11
// Copyright 2011 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.coding;

import java.util.List;


/** Declares methods a coder should implement. */
public interface Coder {
	/** Encode an object */
    public String archive( final Object value );
    
    
	/** Decode the archive */
    public Object unarchive( final String archive );
    
    
    /** Register the custom type by class and its associated adaptor  */
    public <CustomType,RepresentationType> void registerType( final Class<CustomType> type, final ConversionAdaptor<CustomType,RepresentationType> adaptor );
    
    
    /** Register the custom type by name and its associated adaptor */
    public <CustomType,RepresentationType> void registerType( final String type, final ConversionAdaptor<CustomType,RepresentationType> adaptor );
    
    
    /** Get a list of all types which are supported for coding and decoding */
    public List<String> getSupportedTypes();
}
