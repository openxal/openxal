/*
 *  AlgorithmMarketListener.java
 *
 *  Created on Tue Sep 21 13:09:13 EDT 2004
 *
 *  Copyright (c) 2004 Spallation Neutron Source
 *  Oak Ridge National Laboratory
 *  Oak Ridge, TN 37830
 */
package xal.extension.solver.market;

import xal.extension.solver.*;


/**
 * AlgorithmMarketListener
 *
 * @author   tap
 * @since    Sep 21, 2004
 */
public interface AlgorithmMarketListener {
	/**
	 * Event indicating that the algorithm pool changed.
	 *
	 * @param market   The market whose pool has changed.
	 * @param oldPool  The old algorithm pool.
	 * @param newPool  The new algorithm pool.
	 */
	public void poolChanged( AlgorithmMarket market, AlgorithmPool oldPool, AlgorithmPool newPool );
}

