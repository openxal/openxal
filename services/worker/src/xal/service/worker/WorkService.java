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
    
    
    /** shutdown the service */
    public void shutdown( final int code ) {
        Main.shutdown( code );
    }
}

