//
//  DemoWindow.java
//  xal
//
//  Created by Thomas Pelaia on 3/31/05.
//  Copyright 2005 Oak Ridge National Lab. All rights reserved.
//

package xal.app.desktopdemo;

import javax.swing.*;
import javax.swing.JToggleButton.ToggleButtonModel;
import java.awt.event.*;
import java.util.*;
import java.util.logging.*;

import xal.extension.application.*;


/** demonstration document window */
public class DemoWindow extends XalInternalWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    protected JTextArea _textView;
    
    
    /** Constructor */
    public DemoWindow( final XalInternalDocument document ) {
        super( document );
        setSize( 500, 600 );
        makeContent();
    }
    
    
    /** 
	* Getter of the text view that displays the document content.
	* @return The text area that displays the document text.
	*/
    JTextArea getTextView() {
        return _textView;
    }
    
	
    /** Overrides the inherited method to create the toolbar. */
    public boolean usesToolbar() {
        return true;
    }
    
    
    /** Create the main window subviews. */
    protected void makeContent() {
        _textView = new JTextArea();
        _textView.setLineWrap( true );
        _textView.setWrapStyleWord( true );
        final JScrollPane scrollPane = new JScrollPane( _textView );
        getContentPane().add( scrollPane );
        
        JTextField textField = new JTextField();
        getContentPane().add( textField, "South" );
    }	
}
