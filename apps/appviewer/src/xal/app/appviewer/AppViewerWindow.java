/*
 * AppViewerWindow.java
 *
 * Created on Fri Oct 10 15:12:03 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.appviewer;

import xal.application.*;
import xal.tools.data.GenericRecord;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


/**
 * AppViewerWindow
 * @author  tap
 */
class AppViewerWindow extends XalWindow implements AppViewerListener, SwingConstants {
	/** Table of applications running on the local network */

    private static final long serialVersionUID = 1L;
	
    private JTable appTable;
	
	/** Table selection action to garbage collect the selected application */
	private Action garbageCollectionAction;
	
	/** Table selection action to quit the selected application */
	private Action quitSelectionAction;
	
	/** Table selection action to force quit the selected application */
	private Action forceQuitSelectionAction;
	
	/** action to reveal the selected application */
	private Action _revealSelectedAppsAction;
	
	/** Field for entering and displaying the update period */
	private JTextField periodField;
	
	
    /** Creates a new instance of MainWindow */
    public AppViewerWindow(final AppViewerDocument aDocument, final AppTableModel appTableModel) {
        super(aDocument);
        setSize(800, 400);
		makeContent(appTableModel);
		manageActions();
		aDocument.getModel().addAppViewerListener(this);
    }
	
	
	/**
	 * Update the view to reconcile it with the model.
	 */
	protected void updateView() {
		periodField.setText( String.valueOf( getModel().getUpdatePeriod() ) );
		periodField.selectAll();
	}
	
	
	/**
	 * Build the component contents of the window.
	 * @param appTableModel The table model to use for building the table view that displays applications.
	 */
	protected void makeContent(final AppTableModel appTableModel) {
		Box mainView = new Box(VERTICAL);
		getContentPane().add(mainView);
		
		JPanel periodPanel = new JPanel();
		periodPanel.setLayout( new FlowLayout(FlowLayout.LEFT) );
		periodPanel.add( new JLabel("update period (sec): ") );
		periodField = new JTextField(2);
		periodField.addActionListener( new AbstractAction() {
            private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) {
				applyPeriodSetting();
			}
		});
		periodField.addFocusListener( new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				periodField.selectAll();
			}
			public void focusLost(FocusEvent event) {
				// can get called when window is closed, so make sure we still have a document
				if ( document != null ) {
					updateView();
					periodField.setCaretPosition(0);
					periodField.moveCaretPosition(0);
				}
			}
		});

		periodField.setHorizontalAlignment(RIGHT);
		periodPanel.add(periodField);
		periodPanel.setBorder( new EtchedBorder() );
		periodPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10+periodField.getHeight()) );
		mainView.add(periodPanel);
		
		appTable = new JTable(appTableModel);
		
		TableCellRenderer numericCellRenderer = makeNumericCellRenderer();
		TableColumnModel columnModel = appTable.getColumnModel();
        columnModel.getColumn(appTableModel.TOTAL_MEMORY_COLUMN).setCellRenderer(numericCellRenderer);
        columnModel.getColumn(appTableModel.FREE_MEMORY_COLUMN).setCellRenderer(numericCellRenderer);
		
        JScrollPane appScrollPane = new JScrollPane(appTable);
		appScrollPane.setColumnHeaderView( appTable.getTableHeader() );
		appScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        appScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		mainView.add(appScrollPane);
		
		updateView();
	}
	
	
	/**
	 * Apply to the model the period setting from the period field.
	 */
	protected void applyPeriodSetting() {
		try {
			int period = Integer.parseInt( periodField.getText() );
			period = Math.max(period, 1);
			period = Math.min(period, 99);
			getModel().setUpdatePeriod(period);
		}
		catch(NumberFormatException exception) {
			Toolkit.getDefaultToolkit().beep();
		}
		updateView();
	}
    
    
    /**
     * Right justify text associated with numeric values.
     * @return A renderer for numeric values.
     */
    private TableCellRenderer makeNumericCellRenderer() {
        return new DefaultTableCellRenderer() {
            private static final long serialVersionUID = 1L;
            public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                label.setHorizontalAlignment(RIGHT);
                return label;
            }
        };
    }
	
	
	/**
	 * Add event listeners to manage the enable state of the actions.
	 */
	protected void manageActions() {
		setTableSelectionActionsEnabled(false);
		appTable.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				setTableSelectionActionsEnabled(appTable.getSelectedRow() >= 0);
			}
		});		
	}
	
	
	/**
	 * Enable/disable table selection actions depending on the specified state.
	 * @param state enable actions if true and disable actions if false
	 */
	protected void setTableSelectionActionsEnabled(boolean state) {
		garbageCollectionAction.setEnabled(state);
		quitSelectionAction.setEnabled(state);
		forceQuitSelectionAction.setEnabled(state);
	}
    
    
    /**
     * Register actions specific to this window instance. 
     * @param commander The commander with which to register the custom commands.
     */
    protected void customizeCommands(Commander commander) {
		garbageCollectionAction = new AbstractAction("collect-garbage") {
            private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent event) {
				collectGarbageOnSelections();
            }
		};
		
		commander.registerAction(garbageCollectionAction);
		
        quitSelectionAction = new AbstractAction("quit-selections") {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				quitSelections();
            }
		};
		commander.registerAction(quitSelectionAction);
		
        forceQuitSelectionAction = new AbstractAction("force-quit-selections") {
            private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent event) {
				forceQuitSelections();
            }
		};
		commander.registerAction(forceQuitSelectionAction);
		
		// reveal the selected application
        _revealSelectedAppsAction = new AbstractAction( "reveal-apps" ) {
            private static final long serialVersionUID = 1L;

			public void actionPerformed( final ActionEvent event ) {
				revealSelectedApplications();
            }
		};
		commander.registerAction( _revealSelectedAppsAction );
	}
	
	
	/**
	 * Get the main model.
	 * @return the main model.
	 */
	public AppViewerModel getModel() {
		return appViewerDocument().getModel();
	}
	
	
	/**
	 * Convenience method for getting the document as an instance of AppViewerDocument.
	 * @return The document cast as an instace of AppViewerDocument.
	 */
	public AppViewerDocument appViewerDocument() {
		return (AppViewerDocument)document;
	}
	
	
	/**
	 * Garbage collect the applications corresponding to the selected rows of the application table.
	 */
	public void collectGarbageOnSelections() {
		((AppTableModel)appTable.getModel()).collectGarbageOnSelections( appTable.getSelectedRows() );
	}
	
	
	/**
	 * Quit the applications corresponding to the selected rows of the application table.
	 */
	public void quitSelections() {
		((AppTableModel)appTable.getModel()).quitSelections( appTable.getSelectedRows() );
	}
	
	
	/**
	 * Force quit the applications corresponding to the selected rows of the application table.
	 */
	public void forceQuitSelections() {
		((AppTableModel)appTable.getModel()).forceQuitSelections( appTable.getSelectedRows() );
	}
	
	
	/** Reveal the selected application. */
	public void revealSelectedApplications() {
		((AppTableModel)appTable.getModel()).revealSelectedApplications( appTable.getSelectedRows() );
	}
	
	
	/**
	 * The list of applications has changed.
	 * @param source the model posting the event
	 * @param records The records of every application found on the local network.
	 */
	public void applicationsChanged(AppViewerModel source, java.util.List<GenericRecord> records) {}
	
	
	/**
	 * An application's record has been updated
	 * @param source the model posting the event
	 * @param record the updated record
	 */
	public void applicationUpdated(AppViewerModel source, GenericRecord record) {}
	
	
	/**
	 * Notification that a remote message exception has occurred
	 * @param source the model posting the event
	 * @param handler the handler for which the remote exception occurred
	 * @param exception the remote exception that occurred
	 */
	public void remoteException(AppViewerModel source, AppHandler handler, Exception exception) {
		String message = "A remote exception occurred while updating the application:  \"" + handler.getID() + "\"";
		displayError("Remote Exception", message, exception);
	}
}




