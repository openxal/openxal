/*
 * TraceChartDialog.java
 *
 * Created on Thu Jul 22 10:41:51 EDT 2004
 *
 * Copyright (c) 2004 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.orbitcorrect;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.event.*;
import java.awt.Color;
import java.awt.Component;
import java.util.*;


/**
 * TraceChartDialog
 *
 * @author  tap
 * @since Jul 22, 2004
 */
public class TraceChartDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    
	/** Chart model with the trace sources for which the dialog displays and sets properties */
	protected ChartModel _chartModel;
	
	/** Table for displaying trace properties */
	protected JTable _traceTable;
	
	/** Table model for the trace table */
	protected TraceTableModel _traceTableModel;
	
	
	/**
	 * Primary constructor
	 * @param chartModel the chart model for which this dialog displays trace properties
	 * @param owner the owner of this dialog box
	 * @param title the title of this dialog box
	 * @param modal true for modal behavior
	 */
	public TraceChartDialog(ChartModel chartModel, JFrame owner, String title, boolean modal) {
		super(owner, title, modal);
        setSize(500, 400);
		
		_traceTableModel = new TraceTableModel(null);
		setChartModel(chartModel);
		
		makeContent();
	}
	
	
	/**
	 * Minimal Constructor with a default title and modal behavior enabled.
	 * @param chartModel the chart model for which this dialog displays trace properties
	 * @param owner the owner of this dialog box
	 */
	public TraceChartDialog(ChartModel chartModel, JFrame owner) {
		this(chartModel, owner, "Trace Chart Properties", true);
	}
	
    
    /**
     * Create the dialog's subviews.
     */
    protected void makeContent() {
		Box mainView = new Box(BoxLayout.X_AXIS);
		getContentPane().add(mainView);
		
		mainView.add( createTableView() );
	}
	
	
	/**
	 * Create the table view.
	 * @return the new table view
	 */
	protected Component createTableView() {
		_traceTable = new JTable(_traceTableModel);
		JScrollPane scrollPane = new JScrollPane(_traceTable);
		
		return scrollPane;
	}
	
	
	/**
	 * Set the chart model to the one specified.
	 * @param chartModel the new chart model
	 */
	public void setChartModel(final ChartModel chartModel) {
		_chartModel = chartModel;
		_traceTableModel.setChartModel(chartModel);
	}
	
	
	/**
	 * Show this dialog window near the specified component
	 * @param component the component near which to display this dialog
	 */
	public void showNear(Component component) {
		setLocationRelativeTo(component);
		setVisible(true);
	}
}

