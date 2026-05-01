/*
 * Working.java
 *
 * Created on Mon January 6 16:30:29 EST 2012
 *
 * Copyright (c) 2012 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.worker;

import xal.extension.service.OneWay;

import java.util.Date;


/**
 * Demo service interface.
 * @author  tap
 */
public interface Working {
    /** add two numbers */
    public double add( final double summand, final double addend );


	/** add an array of integers and return the sum */
	public int sumIntegers( final int[] summands );


    /** get the launch time */
    public Date getLaunchTime();


	/** calculate the sinusoid waveform from zero to 2pi */
	public double[] generateSinusoid( final double amplitude, final double frequency, final double phase, final int numPoints );


	/** say hello to the person with the specified name */
	public String sayHelloTo( final String name );


    /** shutdown the service */
    @OneWay
    public void shutdown( final int code );
}

