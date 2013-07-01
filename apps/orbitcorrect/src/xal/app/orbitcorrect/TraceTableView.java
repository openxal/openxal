/*
 * TraceTableView.java
 *
 * Created on Wed Jan 07 13:01:01 EST 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;


/**
 * TraceTableView
 *
 * @author  tap
 */
public class TraceTableView extends Box {
	
    private static final long serialVersionUID = 1L;
    
    // components
	protected JTable _table;
	
	
	/**
	 * Constructor
	 */
	public TraceTableView(TraceTableModel model) {
		super(BoxLayout.Y_AXIS);
		buildView(model);
	}
	
	
	/**
	 *
	 */
	protected void buildView(TraceTableModel model) {
		_table = new JTable(model);
		add( _table.getTableHeader() );
		add(_table);
	}
}

