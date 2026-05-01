//
//  FunctionGraphsXALSynopticAdaptor.java
//  xal
//
//  Created by Thomas Pelaia on 9/16/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.widgets.smf;

import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.smf.AcceleratorSeq;

import java.awt.event.*;
import java.awt.*;


/** Provides a convenience method for adding and configuring an XAL Synoptic view to a FunctionGraphsJPanel */
public class FunctionGraphsXALSynopticAdaptor {
	/** Private Constructor */
	private FunctionGraphsXALSynopticAdaptor() {}
	
	
	/**
	 * Assign the synoptic view to the specified chart and configure it to auto size as the chart's position axis changes.
	 * You will also need to explicitly add the synoptic panel to a container to be viewed along with your chart.
	 * @param chart the chart to which to add the synoptic view
	 * @param sequence the accelerator sequence for which to generate the synoptic view
	 * @return the synoptic view
	 */
	static public XALSynopticPanel assignXALSynopticViewTo( final FunctionGraphsJPanel chart, final AcceleratorSeq sequence ) {
		final XALSynopticPanel synopticView = new XALSynopticPanel();
		synopticView.setAcceleratorSequence( sequence );
		
		synopticView.setMaximumSize( new Dimension( 10000, 60 ) );
		synopticView.setMinimumSize( new Dimension( 1, 40 ) );
		synopticView.setPreferredSize( new Dimension( chart.getWidth(), 50 ) );
		synopticView.setBackground( chart.getBackground() );
		
		chart.addHorLimitsListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
				synchronizeSynopticView( synopticView, chart );
			}
		});
		
		return synopticView;
	}
	
	
	/** Synchronize the node view with the chart's horizontal axis. */
	static private void synchronizeSynopticView( final XALSynopticPanel synopticView, final FunctionGraphsJPanel chart ) {
        final double start = chart.getCurrentMinX();
        final double end = chart.getCurrentMaxX();
        
        final int left = chart.getScreenX( start );
        final int right = synopticView.getWidth() - chart.getScreenX( end );
        
        if ( synopticView.getMargin().left != left || synopticView.getMargin().right != right || start != synopticView.getStartPosition() || end != synopticView.getEndPosition() ) {
            synopticView.setMargin( new Insets(5, left, 5, right) );
			if ( start < synopticView.getEndPosition() ) {
				synopticView.setStartPosition( start );
				synopticView.setEndPosition( end );
			}
			else {
				synopticView.setEndPosition( end );
				synopticView.setStartPosition( start );
			}
            synopticView.repaint();
        }
	}
}
