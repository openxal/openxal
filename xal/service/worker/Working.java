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

import java.util.Date;


/**
 * Demo service interface.
 * @author  tap
 */
public interface Working {
    /** add two numbers */
    public double add( final double summand, final double addend );
}

