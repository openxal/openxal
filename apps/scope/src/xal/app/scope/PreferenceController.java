/*
 * PreferenceController.java
 *
 * Created on Fri Oct 31 11:42:27 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import javax.swing.*;
import javax.swing.event.*;
import java.awt.Component;
import java.awt.event.*;
import java.text.*;


/**
 * PreferenceController is the controller for application and document preferences.
 * The application has one preference controller and it displays the values for a 
 * selected document when it is made visible.
 *
 * @author  tap
 */
class PreferenceController implements SwingConstants {
	final static private DecimalFormat timespanFormat = new DecimalFormat("0.0##");
	
	
	/** The preference panel */
	protected JDialog dialog;
	
	/** Controls */
	protected JTextField correlatorTimeField;
	
	/** The scope document */
	protected ScopeDocument document;
	
	
	/**
	 * Constructor for the preference panel controller
	 */
	public PreferenceController(ScopeDocument aDocument) {
		document = aDocument;
		
		dialog = new JDialog(document.getDocumentWindow(), true);
		dialog.setTitle("Preferences");
		
		buildView();
	}
	
	
	/**
	 * Build the components that will appear in the preference panel.
	 */
	protected void buildView() {
		Box mainView = Box.createVerticalBox();
		dialog.getContentPane().add(mainView);
		
		Box row = Box.createHorizontalBox();
		row.add( Box.createHorizontalGlue() );
		row.add( new JLabel("Correlator Timespan (sec):  ") );
		correlatorTimeField = new JTextField(6);
		correlatorTimeField.setHorizontalAlignment(RIGHT);
		correlatorTimeField.addActionListener ( new ActionListener() {
			public void actionPerformed(final ActionEvent event) {
				try {
					double value = Double.parseDouble( correlatorTimeField.getText() );
					document.getModel().setCorrelatorTimespan(value);
					updateView();
				}
				catch(Exception exception) {
					java.awt.Toolkit.getDefaultToolkit().beep();
					updateView();
					correlatorTimeField.selectAll();
				}
			}
		});
		correlatorTimeField.addFocusListener( new FocusListener() {
			public void focusGained(FocusEvent event) {
				correlatorTimeField.selectAll();
			}
			public void focusLost(FocusEvent event) {
				correlatorTimeField.setSelectionStart(0);
				correlatorTimeField.setSelectionEnd(0);				
			}
		});
		correlatorTimeField.getDocument().addDocumentListener( new DocumentListener() {
			public void changedUpdate(DocumentEvent event) {}
			
			public void insertUpdate(DocumentEvent event) {
				try {
					final String text = correlatorTimeField.getText().trim();
					if ( text != null && !text.isEmpty() && !text.equals( "." ) ) {
						Double.parseDouble(text);
					}
				}
				catch(Exception exception) {
					java.awt.Toolkit.getDefaultToolkit().beep();
				}				
			}
			
			public void removeUpdate(DocumentEvent event) {}
		});
		row.add(correlatorTimeField);
		mainView.add(row);
		
		dialog.pack();
	}
	
	
	/**
	 * Update the preference panel view to reflect the underlying model preferences.
	 */
	protected void updateView() {
		double timespan = document.getModel().getCorrelatorTimespan();
		correlatorTimeField.setText( timespanFormat.format(timespan) );
	}
	
	
	/**
	 * Show the preference panel for the specified document.
	 * @param aDocument The document whose preferences are being edited.
	 */
	public void show() {
		updateView();
		dialog.setLocationRelativeTo( document.getDocumentWindow() );
		dialog.setVisible( true );
	}
}

