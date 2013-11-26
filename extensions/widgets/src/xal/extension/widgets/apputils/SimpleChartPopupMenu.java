/*
 * SimpleChartPopupMenu.java
 *
 * Created on January 21, 2003, 9:08 AM
 */

package xal.extension.widgets.apputils;

import xal.tools.apputils.*;
import xal.tools.apputils.ImageCaptureManager;

import java.awt.Frame;
import java.awt.Dialog;
import java.awt.Component;
import java.awt.event.*;
import java.awt.Toolkit;
import java.awt.Window;
import javax.swing.*;
import java.util.*;
import java.io.*;


/**
 * Popup menu that can be attached to a Chart to provide common actions for the user.
 * Supported chart types must have a ChartPopupAdaptor and an associated constructor 
 * in this class.
 * If you want the menu to appear with a popup event, then you must add it as a mouse 
 * listener of the target view.  Alternatively you can use one of the static 
 * convenience methods: <code>addPopupMenuTo()</code> to both create the popup menu 
 * and add it as a mouse listener to the chart.
 *
 * @author  tap
 */
public class SimpleChartPopupMenu extends JPopupMenu implements MouseListener {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	// action ID constants
	static final public String SCALE_ONCE_ID = "scale-once";
	static final public String X_AUTOSCALE_ID = "x-autoscale";
	static final public String Y_AUTOSCALE_ID = "y-autoscale";
	static final public String X_GRID_ID = "x-grid-toggle";
	static final public String Y_GRID_ID = "y-grid-toggle";
	static final public String OPTIONS_DIALOG_ID = "options-dialog";
	static final public String IMAGE_CAPTURE_ID = "save-image";
	
    // Chart references
    protected ChartPopupAdaptor chartAdaptor;
    
	// Menu action table keyed by action ID
	protected Map<String,Action> _actionTable;
	
    // Menu actions
    protected Action scaleOnceAction;
    protected Action xAutoScaleAction;
    protected Action yAutoScaleAction;
    protected Action xGridAction;
    protected Action yGridAction;
    protected Action optionsAction;
    protected Action imageCaptureAction;

    // Other components
	protected Component _chart;
    protected SimpleChartDialog chartDialog;
    protected JFileChooser fileChooser;

    
    /** 
	 * Primary constructor
	 */
    public SimpleChartPopupMenu(Component aChart, ChartPopupAdaptor anAdaptor) {
		_chart = aChart;
        chartAdaptor = anAdaptor;
		setup();
    }
    
    
    /**
     * Create a simple chart popup menu for a FunctionGraphsJPanel chart
     */
    public SimpleChartPopupMenu(xal.extension.widgets.plot.FunctionGraphsJPanel aChart) {
		this( aChart, new xal.extension.widgets.plot.FunctionGraphsPopupAdaptor(aChart) );
    }
    
    
    /**
     * Convenience method for creating a SimpleChartPopupMenu and adding it as a 
     * menu listener to the chart.
     * @param aChart The chart to manage
	 * @param anAdaptor The chart popup adaptor to use
     * @return The popup menu instance
     */
    static public SimpleChartPopupMenu addPopupMenuTo(Component aChart, ChartPopupAdaptor anAdaptor) {
        SimpleChartPopupMenu menu = new SimpleChartPopupMenu(aChart, anAdaptor);
        aChart.addMouseListener(menu);
        return menu;
    }
    
    
    /**
     * Convenience method for creating a SimpleChartPopupMenu and adding it as a 
     * menu listener to the chart.
     * @param aChart The chart to manage
     * @return The popup menu instance
     */
    static public SimpleChartPopupMenu addPopupMenuTo(xal.extension.widgets.plot.FunctionGraphsJPanel aChart) {
		return addPopupMenuTo( aChart, new xal.extension.widgets.plot.FunctionGraphsPopupAdaptor(aChart) );
    }
    
    
    /**
     * Initialize the popup menu.
     */
    protected void setup() {		
		_actionTable = new HashMap<String,Action>(8);
        initComponents();
    }
	
	
	/**
	 * Get the chart dialog and make it if it does not already exist
	 * @return the chart dialog
	 */
	private SimpleChartDialog getChartDialog() {
		if ( chartDialog != null )  return chartDialog;
		
		Window owner = SwingUtilities.windowForComponent(_chart);
		
		if (owner instanceof Frame) {
			chartDialog = new SimpleChartDialog((Frame)owner, _chart, chartAdaptor);
		}
		else if (owner instanceof Dialog) {
			chartDialog = new SimpleChartDialog((Dialog)owner, _chart, chartAdaptor);
		}
		else {
			chartDialog = new SimpleChartDialog(_chart, chartAdaptor);
		}
		
		return chartDialog;
	}
    
    
    /** Create and initialize the GUI components */
    protected void initComponents() {
		defineActions();
		storeActions();
		buildMenu();
    }
	
	
	/**
	 * Define the actions for the popup menu
	 */
	protected void defineActions() {
        // scale the X and Y axes once
        scaleOnceAction = new AbstractAction("Scale X and Y Once")  {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                chartAdaptor.scaleXandY();
            }
        };
        
        
        // toggle x auto scale
        xAutoScaleAction = new AbstractAction("Autoscale X") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                chartAdaptor.setXAutoScale( !chartAdaptor.isXAutoScale() );
            }
        };
        
        
        // toggle y auto scale
        yAutoScaleAction = new AbstractAction("Autoscale Y") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                chartAdaptor.setYAutoScale( !chartAdaptor.isYAutoScale() );
            }
        };
        
        
        // define x grid menu item action
        xGridAction = new AbstractAction("Show X Grid") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                chartAdaptor.setXGridVisible( !chartAdaptor.isXGridVisible() );
            }
        };
        
        
        // define y grid menu item action
        yGridAction = new AbstractAction("Show Y Grid") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                chartAdaptor.setYGridVisible( !chartAdaptor.isYGridVisible() );
            }
        };
		
        
        // define options menu item action
        optionsAction = new AbstractAction("Options...") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                getChartDialog().showDialog();
            }
        };

        
        // define options menu item action
        imageCaptureAction = new AbstractAction("Save as PNG") {
            /** serialization ID */
            private static final long serialVersionUID = 1L;
            
            public void actionPerformed(java.awt.event.ActionEvent event) {
                try {
                    ImageCaptureManager.defaultManager().saveSnapshot( chartAdaptor.getChartComponent() );
                }
                catch(java.awt.AWTException exception) {
                    System.err.println(exception);
                    JOptionPane.showMessageDialog(chartAdaptor.getChartComponent(), exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
                }
                catch(IOException exception) {
                    System.err.println(exception);
                    JOptionPane.showMessageDialog(chartAdaptor.getChartComponent(), exception.getMessage(), exception.getClass().getName(), JOptionPane.WARNING_MESSAGE);
                }
            }
        };
	}
	
	
	/**
	 * Put the actions in the table.
	 */
	protected void storeActions() {
		_actionTable.put( SCALE_ONCE_ID, scaleOnceAction );
		_actionTable.put( X_AUTOSCALE_ID, xAutoScaleAction );
		_actionTable.put( Y_AUTOSCALE_ID, yAutoScaleAction );
		_actionTable.put( X_GRID_ID, xGridAction );
        _actionTable.put( Y_GRID_ID, yGridAction );
		_actionTable.put( OPTIONS_DIALOG_ID, optionsAction );
		_actionTable.put( IMAGE_CAPTURE_ID, imageCaptureAction );
	}
	
	
	/**
	 * Build the popup menu by adding all of the defined actions
	 */
	protected void buildMenu() {
        add( scaleOnceAction );
        add( xAutoScaleAction );
        add( yAutoScaleAction );
        addSeparator();
        add( xGridAction );
        add( yGridAction );
        addSeparator();
        add( optionsAction );
        addSeparator();
        add( imageCaptureAction );
	}
    
    
    /** Update the components to reflect the state of the chart */
    protected void update() {
        xAutoScaleAction.putValue( Action.NAME, ( chartAdaptor.isXAutoScale() ) ? "Freeze X Scale" : "Autoscale X" );
        yAutoScaleAction.putValue( Action.NAME, ( chartAdaptor.isYAutoScale() ) ? "Freeze Y Scale" : "Autoscale Y" );
        
		xGridAction.putValue( Action.NAME, ( chartAdaptor.isXGridVisible() ) ? "Hide X Grid" : "Show X Grid"  );
		yGridAction.putValue( Action.NAME, ( chartAdaptor.isYGridVisible() ) ? "Hide Y Grid" : "Show Y Grid"  );
		        
        pack();
    }
	
	
	/**
	 * Enable/Disable the action specified by the actionID.
	 * @param actionID The id of the action to enable/disable.
	 * @param enableState The desired enable/disable state.
	 */
	public void setActionEnabled( final String actionID, final boolean enableState ) {
		Action action = _actionTable.get( actionID );
		action.setEnabled( enableState );
	}
    
    
    /** implement MouseListener interface */
    public void mouseClicked(MouseEvent event) {}
    
    /** implement MouseListener interface */
    public void mouseEntered(MouseEvent event) {}

    /** implement MouseListener interface */
    public void mouseExited(MouseEvent event) {}
    
    /** implement MouseListener interface */
    public void mousePressed(MouseEvent event) {
		handleMouseEvent( event );
    }
    
    /** implement MouseListener interface */
    public void mouseReleased(MouseEvent event) {
		handleMouseEvent( event );
	}
	
	/** handle the mouse event */
	public void handleMouseEvent( final MouseEvent event ) {
        if ( event.isPopupTrigger() ) {
            update();
            show(chartAdaptor.getChartComponent(), event.getX(), event.getY());
        }		
	}
}
