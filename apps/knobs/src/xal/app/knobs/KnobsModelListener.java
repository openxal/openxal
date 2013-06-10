//
//  KnobsModelListener.java
//  xal
//
//  Created by Thomas Pelaia on 10/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.knobs;


/** Model events */
public interface KnobsModelListener {
	/**
	 * Handle the event indicating that the groups in the model have changed.
	 * @param model The model whose groups have changed.
	 */
	public void groupsChanged( KnobsModel model );
	
	
	/**
	 * Handle the event indicating that the knobs model has been modified.
	 * @param model The model which has been modified.
	 */
	public void modified( KnobsModel model );
}
