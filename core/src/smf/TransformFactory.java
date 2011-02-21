/*
 * TransformFactory.java
 *
 * Created on July 8, 2003, 3:36 PM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.smf;

import xal.tools.reflect.Selector;
import xal.tools.reflect.Selector.MethodNotFoundException;
import xal.tools.data.*;
import xal.tools.transforms.*;


/**
 * TransformFactory generates a <code>ValueTransform</code> from a <code>DataAdaptor</code>.
 *
 * @author  tap
 */
public class TransformFactory {
    
    /** Creates a new instance of TransformFactory */
    protected TransformFactory() {
    }
    
    
    /**
     * Generate a transform from the given adaptor.  The adaptor defines the 
     * properties of the desired transform including the type and any supporting 
     * parameters.  The static method called to generate the transform is determined
     * by the type and constructed by appending "Transform" to get the 
     * method name.  All generator methods must be declared static and take 
     * an adaptor as its only argument.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     * @throws xal.smf.NoSuchTransformException if the transform of the specified type cannot be generated.
     */
    static public ValueTransform getTransform(DataAdaptor adaptor) throws NoSuchTransformException {
        String type = adaptor.stringValue("type");
        String methodName = type + "Transform";
        Selector selector = new Selector(methodName, DataAdaptor.class);
        
        try {
            return (ValueTransform)selector.invokeStatic(TransformFactory.class, adaptor);
        }
        catch(MethodNotFoundException exception) {
            throw new NoSuchTransformException(type);
        }        
    }
    
    
    /**
     * Generate a value transform which simply scales a double value.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleScaleTransform(final DataAdaptor adaptor) {
        double scale = adaptor.doubleValue("scale");
        return DataTransformFactory.doubleScaleTransform(scale).valueTransform();
    }
    
    
    /**
     * Generate a value transform which applies a simple offset transform to a 
     * double value scalar.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleTranslationTransform(final DataAdaptor adaptor) {
        double offset = adaptor.doubleValue("offset");
        return DataTransformFactory.doubleTranslationTransform(offset).valueTransform();
    }
    
    
    /**
     * Generate a value transform which applies a simple linear transform (scale
     * and offset) to a double value scalar.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleLinearTransform(final DataAdaptor adaptor) {
        double scale = adaptor.doubleValue("scale");
        double offset = adaptor.doubleValue("offset");
        return DataTransformFactory.doubleLinearTransform(scale, offset).valueTransform();
    }
    
    
    /**
     * Generate a value transform which simply scales a double array of values.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleArrayScaleTransform(final DataAdaptor adaptor) {
        double scale = adaptor.doubleValue("scale");
        return DataTransformFactory.doubleArrayScaleTransform(scale).valueTransform();
    }
    
    
    /**
     * Generate a value transform which applies a simple offset transform to a 
     * double array of values.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleArrayTranslationTransform(final DataAdaptor adaptor) {
        double offset = adaptor.doubleValue("offset");
        return DataTransformFactory.doubleArrayTranslationTransform(offset).valueTransform();
    }
    
    
    /**
     * Generate a value transform which applies a simple linear transform (scale
     * and offset) to a double array of values.
     * @param adaptor The adaptor defining the transform.
     * @return A value transform with the properties specified by the adaptor.
     */
    static public ValueTransform doubleArrayLinearTransform(final DataAdaptor adaptor) {
        double scale = adaptor.doubleValue("scale");
        double offset = adaptor.doubleValue("offset");
        return DataTransformFactory.doubleArrayLinearTransform(scale, offset).valueTransform();
    }
}
