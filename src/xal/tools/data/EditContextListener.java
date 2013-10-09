//
//  EditContextListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** interface for handling edit context events */
public interface EditContextListener {
	/**
	 * Handle the event indicating that a table was added to the edit context.
	 * @param context the edit context to which the table was added
	 * @param table the table added to the edit context
	 */
	public void tableAdded( EditContext context, DataTable table );
	
	
	/**
	 * Handle the event indicating that a table was removed from the edit context.
	 * @param context the edit context from which the table was removed
	 * @param table the table removed from the edit context
	 */
	public void tableRemoved( EditContext context, DataTable table );

}
