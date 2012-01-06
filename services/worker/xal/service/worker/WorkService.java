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
}

