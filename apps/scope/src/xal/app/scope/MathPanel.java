/*
 * MathPanel.java
 *
 * Created on July 29, 2003, 10:37 AM
 *
 * Copyright 2003, Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.scope;

import xal.tools.dispatch.DispatchQueue;

import javax.swing.*;
import javax.swing.text.*;
import javax.swing.border.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
 * MathPanel allows the user to define a formula for a waveform that is a function
 * of one or more channels.
 *
 * @author  tap
 */
public class MathPanel extends javax.swing.Box implements SwingConstants, SettingListener {
	/** satisfy serializable */
	final static private long serialVersionUID = 1;
	
    // style constants
    static final Style editStyle, agreeStyle, errorStyle;
    
    // model variables
    protected MathModel model;
    
    // visual components
    protected JTextPane formulaBox;
    protected JButton formulaSetButton;
    protected JToggleButton enableButton;
    
    
    static {
        StyleContext context = new StyleContext();
        agreeStyle = context.addStyle(null, null);
        StyleConstants.setForeground(agreeStyle, Color.black);
        editStyle = context.addStyle(null, null);
        StyleConstants.setForeground(editStyle, Color.gray);
        errorStyle = context.addStyle(null, null);
        StyleConstants.setForeground(errorStyle, Color.red);
    }
    
    
    /** Creates a new instance of MathPanel */
    public MathPanel() {
        this( null );
    }
    
    
    /** Creates a new instance of MathPanel */
    public MathPanel( final MathModel aModel ) {
        super( VERTICAL );

		setModel( null, aModel );
        initComponents();
    }
    
    
    /** 
     * Set a new math model for display.  Buttons are used to select which 
     * math model to display.  When the user presses a math button we 
     * update the math panel with the information for the corresponding model.
     * @param sender The button that fired the event
     * @param newModel The new math model to represent
     */
    public void setModel( final AbstractButton sender, final MathModel newModel ) {
		if ( model != null ) {
			model.removeSettingListener( this );
		}

        model = newModel;

		if ( newModel != null ) {
			newModel.addSettingListener( this );
		}

        try {
			if ( model != null ) {
				final String title = model.getId();
				final TitledBorder border = (TitledBorder)getBorder();
				border.setTitle( title );
				border.setBorder( new LineBorder( sender.getForeground() ) );
			}
        }
        catch(Exception excpt) {
            System.err.println(excpt);
        }
        
        updateView();
    }
    
    
    /** Reset the keyboard focus to the appropriate control */
    void resetDefaultFocus() {
        formulaBox.requestFocusInWindow();
    }
    
    
    /** Update the view to reflect the model state */
    protected void updateView() {
        if ( model != null ) {
            formulaBox.setText( model.getFormula() );
            updateFormulaStyle();
            enableButton.setSelected( model.isEnabled() );
            enableButton.setEnabled( model.canEnable() );
        }
        
        repaint();
    }

	
	/**
     * A setting from the sender has changed.
     * @param source The object whose setting changed.
     */
    public void settingChanged( final Object source ) {
		if ( DispatchQueue.getCurrentQueue() == DispatchQueue.getMainQueue() ) {
			updateView();
		}
		else {
			DispatchQueue.getMainQueue().dispatchAsync( new Runnable() {
				public void run() {
					updateView();
				}
			});
		}
	}


    
    /**
     * Update the style of the formula text to reflect the status of the formula.
     */
    protected void updateFormulaStyle() {
        StyledDocument formulaDocument = formulaBox.getStyledDocument();
        if ( !formulaBox.getText().equals( model.getFormula() ) ) {
            formulaDocument.setParagraphAttributes(0, formulaDocument.getLength(), editStyle, true);
        }
        else if ( model.hasCompileErrors() ) {
            formulaDocument.setParagraphAttributes(0, formulaDocument.getLength(), errorStyle, true);
        }
        else {
            formulaDocument.setParagraphAttributes(0, formulaDocument.getLength(), agreeStyle, true);
        }            
    }
    
    
    /** 
     * Create and layout the components on the panel.
     */
    protected void initComponents() {
        //----------------- setup the panel
        TitledBorder border = new TitledBorder("Math");
        border.setBorder( new LineBorder(Color.black) );
        setBorder(border);
        
        // add the formula label
        Box labelRow = new Box(HORIZONTAL);
        labelRow.add( new JLabel("formula:") );
        labelRow.add( Box.createHorizontalGlue() );
        
        formulaSetButton = new JButton("Set");
        formulaSetButton.addActionListener( new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                try {
                    model.setFormula( formulaBox.getText() );
                }
                catch( RuntimeException exception ) {
                    String message = "The formula entered does not compile.";
                    String title = "Compile Error";
                    JOptionPane.showMessageDialog(MathPanel.this, message, title, JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        labelRow.add(formulaSetButton);
        
        labelRow.add( Box.createHorizontalStrut(5) );
        enableButton = new JToggleButton("Enable");
        enableButton.addActionListener( new ActionListener() {
            public void actionPerformed(final ActionEvent event) {
                model.toggleEnable();
                updateView();
            }
        });
        labelRow.add(enableButton);
        
        add(labelRow);
        add( Box.createVerticalStrut(2) );
        
        // add a text box
        formulaBox = new JTextPane();
        formulaBox.setStyledDocument( new DefaultStyledDocument() {
			/** satisfy serializable */
			final static private long serialVersionUID = 1;

            final StyledDocument document = formulaBox.getStyledDocument();
                        
            public void insertString(int offset, String string, AttributeSet attributes) throws BadLocationException {
                super.insertString(offset, string, attributes);
                updateFormulaStyle();
            }
            
            public void remove(int offset, int length) throws BadLocationException {
                super.remove(offset, length);
                updateFormulaStyle();
            }
        });
		formulaBox.addFocusListener( new FocusAdapter() {
			public void focusGained(FocusEvent event) {
				formulaBox.selectAll();
			}
			public void focusLost(FocusEvent event) {
				formulaBox.setCaretPosition(0);
				formulaBox.moveCaretPosition(0);
			}
		});
        
        add(formulaBox);
        
        // force the panel to resize so it will be drawn
        setSize(getPreferredSize());
        
        // update the view
        updateView();
    }
}
