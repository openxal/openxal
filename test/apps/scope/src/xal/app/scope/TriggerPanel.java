/*
 * TriggerPanel.java
 *
 * Created on May 30, 2003, 12:53 PM
 */

package xal.app.scope;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import xal.ca.view.ChannelNameDocument;
import xal.tools.text.NumericParser;


/**
 * View that displays and controls the state of the trigger.
 *
 * @author  tap
 */
public class TriggerPanel extends Box implements TriggerListener, SwingConstants {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // ----------- model
    protected Trigger trigger;
    
    // feedback views
    protected JProgressBar channelSetProgressBar;
	protected JLabel connectionLabel;
    
    // ----------- controls
    protected JTextField channelField;
    protected JButton channelSetButton;
    protected JToggleButton enableButton;
    protected JComboBox<String> filterMenu;
    protected JTable filterTable;
    protected ParameterTableModel filterTableModel;
    
    // ----------- state
    protected boolean updatingView;
    
    
    /** Creates new form TriggerPanel */
    public TriggerPanel(Trigger aTrigger) {
        super(VERTICAL);
        updatingView = false;
        trigger = aTrigger;
        initComponents();
        trigger.addTriggerListener(this);
    }
    
    
    /** Update the display to reflect the underlying model */
    protected void updateView(AbstractButton sender) {
        updateView();
    }
	
	
	/** 
	 * Safely update the view regardless if it is called from the event dispatch thread 
	 * or another application thread 
	 */
	protected void safelyUpdateView() {
		if ( SwingUtilities.isEventDispatchThread() ) {
			updateView();
		}
		else {
			dispatchUpdateView();
		}
	}
	
	
	/** Update the view in a thread safe way by using the update in the Swing event dispatch queue */
	protected void dispatchUpdateView() {
		try {
			SwingUtilities.invokeAndWait( new Runnable() {
				public void run() {
					updateView();
				}
			});
		}
		catch(InterruptedException exception) {
			System.err.println(exception);
			exception.printStackTrace();
		}
		catch(java.lang.reflect.InvocationTargetException exception) {
			System.err.println(exception);
			exception.printStackTrace();
		}
	}
    
    
    /** Update the view with model information */
    protected void updateView() {
        updatingView = true;
        // reconcile controls here
        channelField.setText( trigger.getChannelName() );
        enableButton.setEnabled( trigger.canEnable() );
        enableButton.setSelected( trigger.isEnabled() );
		enableButton.setText( trigger.isEnabled() ? "Enabled" : "Enable");
        filterMenu.setEnabled( trigger.canEnable() );
        filterMenu.setSelectedItem( trigger.getFilterLabel() );
        filterTable.setEnabled( trigger.canEnable() );
        filterTableModel.setParameters( trigger.getFilterParameters() );
        
		/*
        channelSetProgressBar.setVisible( trigger.isSettingChannel() );
        channelSetProgressBar.setIndeterminate( trigger.isSettingChannel() );
		*/
        channelField.setEnabled( !trigger.isSettingChannel() );
        channelSetButton.setEnabled( !trigger.isSettingChannel() );
		connectionLabel.setText( connectionString(trigger.isConnected()) );
        
        repaint();
        updatingView = false;
    }
	
	
	/**
	 * Get the string for displaying the connection status
	 * @param isConnected specifies whether to display a "connected" label or "disconnected" label
	 * @return a green "connected" label if connected and a red "disconnected" label if not
	 */
	protected String connectionString(boolean isConnected) {
		final String header = "<html><body>";
		final String footer = "</body></html>";
		final String body =  isConnected ? "<font color=\"#00bb00\">Connected</font>" : "<font color=\"#ff0000\">Disconnected</font>";
		return header + body + footer;
	}
    
    
    /** Reset the keyboard focus to the appropriate control */
    void resetDefaultFocus() {
        channelField.requestFocusInWindow();
    }
    
    
    /** 
     * Create and layout the components on the panel.
     */
    protected void initComponents() {
        //----------------- setup the panel
        TitledBorder border = new TitledBorder("Trigger");
        border.setBorder( new LineBorder(Color.black) );
        setBorder(border);

        Box channelRow = new Box(HORIZONTAL);
        //----------------- add a channel field
        channelField = new JTextField(new ChannelNameDocument(), "", 20);
        channelField.setMaximumSize( channelField.getPreferredSize() );
		channelField.addFocusListener( new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				channelField.selectAll();
			}
			public void focusLost(FocusEvent event) {
				channelField.setCaretPosition(0);
				channelField.moveCaretPosition(0);
			}
		});
		channelField.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
				performChannelSet(event);
            }
		});
        channelRow.add(channelField);
        channelRow.add(Box.createHorizontalStrut(5));
        
        //----------------- add a set button
        channelSetButton = new JButton();
        channelSetButton.setText("Set");
        channelRow.add(channelSetButton);
        channelSetButton.addActionListener(new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
				performChannelSet(event);
            }
        });
        add(channelRow);
        channelRow.setMaximumSize( channelRow.getPreferredSize() );
        
        channelSetProgressBar = new JProgressBar();
        //add(channelSetProgressBar);
		connectionLabel = new JLabel("");
		add(connectionLabel);
        
        add( Box.createVerticalStrut(10) );
        Box enableRow = new Box(HORIZONTAL);
        enableRow.add( Box.createGlue() );
        //----------------- add an enable button
        enableButton = new JToggleButton("Enable");
        enableButton.setMargin( new Insets(1, 1, 1, 1) );
        enableButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if ( trigger != null ) {
                    trigger.setEnabled( enableButton.isSelected() );
                    updateView();
                }
            }
        });
        enableRow.add(enableButton);
        add(enableRow);
        
        //----------------- add the trigger filter box
        Box filterBox = new Box(VERTICAL);
        filterBox.setBorder( new TitledBorder("Filter") );        
        add(filterBox);
        
        filterMenu = new JComboBox<>( TriggerFilterFactory.triggerFilterTypes() );
        filterMenu.setMaximumSize( filterMenu.getPreferredSize() );
        filterMenu.addItemListener( new ItemListener() {
            public void itemStateChanged(ItemEvent event) {
                if (updatingView)  return;      // only handle user initiated events
                switch( event.getStateChange() ) {
                    case ItemEvent.SELECTED:
                        String filterLabel = (String)event.getItem();
                        // Be sure only to change the filter when the types change to 
                        // avoid creating a new filter while loading a document.
                        if ( !trigger.getFilterLabel().equals(filterLabel) ) {
                            trigger.setTriggerFilter( TriggerFilterFactory.newTriggerFilter(filterLabel) );
                            updateView();
                        }
                        break;
                    default:
                        break;
                }
            }
        });
        filterBox.add(filterMenu);
        
        filterBox.add( Box.createVerticalStrut(10) );
        filterTableModel = new ParameterTableModel( trigger.getFilterParameters() );
        filterTableModel.addTableModelListener( new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                if ( !updatingView ) {
                    trigger.refresh();
                }
            }
        });
        filterTable = new JTable(filterTableModel);
        TableColumn filterValueColumn = filterTable.getColumnModel().getColumn(ParameterTableModel.VALUE_COLUMN);
        filterValueColumn.setCellRenderer( new ValueEditor() );
        filterValueColumn.setCellEditor( new ValueEditor() );
        JScrollPane filterTablePane = new JScrollPane(filterTable);
        filterBox.add(filterTablePane);
                
        
        // force the panel to resize so it will be drawn
        setSize(getPreferredSize());
    }
	
	
	/**
	 * Set the channel to that specified by the PV in the channel field.
	 * @param event The event that initiated the channel set.
	 */
	protected void performChannelSet(final ActionEvent event) {
		// need a place to post an error dialog box in case an exception 
		// is thrown and this view isn't visible
		final Container container = this.getParent();
		
		Thread thread = new Thread() {
			public void run() {
				try {
					channelSetButton.setEnabled(false);
					channelField.setEnabled(false);
					handleChannelSetAction(event, container);
				}
				finally {
					dispatchUpdateView();
				}
			}
		};
		thread.start();
	}
    
	
    /** 
     * Change the channel name to the new name selected by the user.
     * @param event The event that initiated the channel PV change.
     * @param container The place relative to where error message dialog boxes may be placed.
     */
    protected void handleChannelSetAction(final ActionEvent event, final Container container) {
        try {
            trigger.setChannel(channelField.getText());
        }
        catch(ChannelSetException exception) {
            System.err.println(exception);
            String title = "Channel Set Exception";
            String message = exception.getMessage();
            Toolkit.getDefaultToolkit().beep();
            JOptionPane.showMessageDialog(container, message, title, JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
    /**
     * Event indicating that the specified trigger has been enabled.
     * @param source Trigger posting the event.
     */
    public void triggerEnabled(Trigger source) {
        updateView();
    }
    
    
    /**
     * Event indicating that the specified trigger has been disabled.
     * @param source Trigger posting the event.
     */
    public void triggerDisabled(Trigger source) {
        updateView();
    }
    
    
    /**
     * Event indicating that the trigger channel is disabled.
     * @param source Trigger posting the event.
     */
    public void channelDisabled(Trigger source) {
        updateView();
    }
    
    
    /**
     * Event indicating that the trigger channel state has changed.
     * @param source Trigger posting the event.
     */
    public void channelStateChanged(Trigger source) {
        updateView();
    }
}



/** 
 * ParameterTableModel is a table model for the trigger filter table of parameters.
 * It allows the user to set the values of the various trigger filter parameters.
 */
class ParameterTableModel extends AbstractTableModel {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    // constants
    final static public int LABEL_COLUMN = 0;
    final static public int VALUE_COLUMN = 1;
    
    // data
    protected Parameter[] parameters;

    
    /** Constructor for the corrector table model */
    public ParameterTableModel(Parameter[] newParameters) {
        super();
        parameters = newParameters;
    }
    
    
    /**
     * Set the specified parameters to display in the table.
     * @param newParameters The new parameters to display in the table.
     */
    synchronized public void setParameters(Parameter[] newParameters) {
        parameters = newParameters;
        fireTableDataChanged();
    }


    /** Get the number of columns in the table */
    public int getColumnCount() { 
        return 2; 
    }


    /** Get the number of rows in the table */
    synchronized public int getRowCount() { 
        return parameters.length;
    }
    
    
    /**
     * Get the parameter for the specified row index.
     * @param row The table row for which the parameter gets displayed.
     * @return parameter corresponding to the specified row
     */
    synchronized public Parameter getParameter(int row) {
        return parameters[row];
    }


    /** Get the value at a given table cell. */
    synchronized public Object getValueAt(int row, int column) {
        // in case a record was removed since getRowCount
        if ( row >= getRowCount() )  return "";

        Parameter parameter = getParameter(row);
        Object value;

        switch(column) {
            case LABEL_COLUMN:
                value = parameter.getLabel();
                break;
            case VALUE_COLUMN:
                value = parameter.getValue().toString();
                break;
            default:
                value = "";
                break;
        }

        return value; 
    }


    /** Get the title of each column */
    public String getColumnName(int column) {
        String title;

        switch(column) {
            case LABEL_COLUMN:
                title = "Parameter";
                break;
            case VALUE_COLUMN:
                title = "Value";
                break;
            default:
                title = "";
                break;
        }

        return title;
    }


    /** Get the value class associated with each column */
    public Class<?> getColumnClass(int column) {
        Class<?> columnClass;

        switch(column) {
            case LABEL_COLUMN:
                columnClass = String.class;
                break;
            case VALUE_COLUMN:
                columnClass = String.class;
                break;
            default:
                columnClass = String.class;
                break;
        }

        return columnClass;
    }


    /** Identify which cells can be edited. */
    public boolean isCellEditable(int row, int column) {
        boolean canEdit;

        switch(column) {
            case LABEL_COLUMN:
                canEdit = false;
                break;
            case VALUE_COLUMN:
                canEdit = true;
                break;
            default:
                canEdit = false;
                break;
        }

        return canEdit;
    }


    /** Change the value of the corrector to reflect the value entered into the cell. */
    synchronized public void setValueAt( final Object value, final int row, final int column ) {
        if ( row >= getRowCount() )  return;

        final Parameter parameter = getParameter( row );
        try {
            switch(column) {
                case VALUE_COLUMN:
                    final Number numericValue = NumericParser.getNumericValue( (String)value, parameter.getType() );
                    parameter.setValue( numericValue );
                    break;
                default:
                    break;
            }
        }
        catch( NumberFormatException excpt ) {
            Toolkit.getDefaultToolkit().beep();
        }

        fireTableCellUpdated(row, column);
    }
}    
    

/** editor for the field column in the corrector table */
class ValueEditor extends DefaultCellEditor implements TableCellRenderer {
	/** constant required to keep serializable happy */
	static final private long serialVersionUID = 1L;

    public ValueEditor() {
        super(new JTextField());
        ((JTextField)editorComponent).setEditable(true);
        ((JTextField)editorComponent).setHorizontalAlignment(JTextField.RIGHT);
        setClickCountToStart(1);
    }


    /** Return the corrector field as a string */
    public Object getCellEditorValue() {
        return ((JTextField)editorComponent).getText();
    }


    /** Use an editable JTextField to display each corrector field */
    public java.awt.Component getTableCellEditorComponent(JTable table, 
      Object value, boolean isSelected, int row, int column) {

        String stringValue = value.toString();     
        ((JTextField)editorComponent).setText(stringValue);
        ((JTextField)editorComponent).selectAll();

        return editorComponent;            
    }


    // implement TableCellRenderer
    public java.awt.Component getTableCellRendererComponent(JTable table, 
      Object value, boolean isSelected, boolean hasFocus, int row, int column) {

        String stringValue = value.toString();
        ((JTextField)editorComponent).setText( stringValue );            

        return editorComponent;
    }
}

