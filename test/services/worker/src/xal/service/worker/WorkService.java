/*
 * WorkService.java
 *
 * Created on Mon January 6 13:50:13 EST 2012
 *
 * Copyright (c) 2012 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.service.worker;

import java.util.Date;


/**
 * Demo service providing demo work.
 * @author  tap
 */
public class WorkService implements Working {
    /** add two numbers */
    public double add( final double summand, final double addend ) {
        return summand + addend;
    }


	/** add the integers and return the resulting sum */
	public int sumIntegers( final int[] summands ) {
		int sum = 0;

		for ( final int summand : summands ) {
			sum += summand;
		}

		return sum;
	}

    
    /** get the launch time */
    public Date getLaunchTime() {
        return Main.getLaunchTime();
    }

	
	/** calculate the sinusoid waveform from zero to 2pi */
	public double[] generateSinusoid( final double amplitude, final double frequency, final double phase, final int numPoints ) {
		final double omega = 2 * Math.PI * frequency;
		final double step = 1.0 / ( numPoints - 1 );

		final double[] waveform = new double[numPoints];
		double x = 0.0;
		for ( int windex = 0 ; windex < numPoints ; windex++ ) {
			waveform[windex] = amplitude * Math.sin( omega * x + phase );
			x += step;
		}

		return waveform;
	}


	/** say hello to the person with the specified name */
	public String sayHelloTo( final String name ) {
		return name != null && !name.isEmpty() ? "Hello, " + name + "!" : "Greetings!";
	}

    
    /** shutdown the service */
    public void shutdown( final int code ) {
        Main.shutdown( code );
    }
}

