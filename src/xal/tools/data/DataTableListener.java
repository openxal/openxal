//
//  DataTableListener.java
//  xal
//
//  Created by Thomas Pelaia on 6/6/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.data;


/** Interface for handling data table events */
public interface DataTableListener {
	/**
	 * Event indicating that a record has been added to a table.
	 * @param table the table to which the record has been added
	 * @param record the record added to the table
	 */
	public void recordAdded( DataTable table, GenericRecord record );
	
	
	/**
	 * Event indicating that a record has been removed from a table.
	 * @param table the table from which the record has been removed
	 * @param record the record removed from the table
	 */
	public void recordRemoved( DataTable table, GenericRecord record );
}
