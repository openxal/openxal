/*
 * @(#)NoiseGenerator.java          0.1 03/03/2004
 *
 * Copyright (c) 2001-2004 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 */

package xal.app.virtualaccelerator;

import xal.ca.Channel;
import xal.ca.ConnectionException;
import xal.ca.GetException;

/**
 * NoiseGenerator generates noise for readback PVs 
 *
 * @version   0.1  02 Mar 2004
 * @author  Paul Chu
 */

public class NoiseGenerator {
    /**
     * input the "set" PV and return double value as the corresponding 
     * readback PV value
     *
     * @param pvVal the "set" PV value
     * @param noiseLevel noise level for the readback PV 
     * @param offset offset from the nominal value
     * @return readback PV value with noise added
     */
    public static double setValForPV(double pvVal, double noiseLevel, double offset) {
        return pvVal + noiseLevel * (Math.random()-0.5)*2./100. + offset;
    }
    
    
    /**
     * Get an array with noise based on a nominal value
     * @param arrayLength total length of the array
     * @param dataLength length of the array to populate with data starting with index 0
     * @param nominalValue nominal value for entire array
     * @param noiseLevel level of the noise to apply
     * @param offset offset from the nominal value
     */
    public static double[] noisyArrayForNominal( final double nominalValue, final int arrayLength, final int dataLength,  final double noiseLevel, final double offset ) {
        if ( dataLength > arrayLength )  throw new IllegalArgumentException( "The data length: " + dataLength + " must be less than or equal to the array length: " + arrayLength + " to generate a noisy array." );
        
        final double[] noisyArray = new double[arrayLength];
        for ( int index = 0 ; index < dataLength ; index++ ) {
            noisyArray[index] = setValForPV( nominalValue, noiseLevel, offset );
        }
        
        return noisyArray;
    }
    
    
    /** Get the average from the array */
    public static double getAverage( final double[] array, final int dataLength ) {
        if ( dataLength <= 0 )  return 0.0;
        
        double sum = 0.0;
        
        for ( int index = 0 ; index < dataLength ; index++ ) {
            sum += array[index];
        }
        
        return sum / dataLength;
    }
}
