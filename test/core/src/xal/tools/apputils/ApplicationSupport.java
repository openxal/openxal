//
//  ApplicationSupport.java
//  xal
//
//  Created by Thomas Pelaia on 10/17/2013.
//  Copyright 2013 Oak Ridge National Lab. All rights reserved.
//

package xal.tools.apputils;

import javax.swing.JOptionPane;
import java.awt.KeyboardFocusManager;
import java.awt.Toolkit;
import java.awt.Window;


/** Common application utilities */
public class ApplicationSupport {
    /** serialization ID */
    private static final long serialVersionUID = 1L;


    /**
     * Get the active window which is in focus for this application.  It is typically a good window relative to which you can place application warning dialog boxes.
     * @return The active window
     */
    static public Window getActiveWindow() {
        return KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
    }


	/**
	 * Display a confirmation dialog with a title and message
	 * @param title The title of the dialog
	 * @param message The message to display
	 * @return YES_OPTION or NO_OPTION
	 */
	static public int displayConfirmDialog( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        return JOptionPane.showConfirmDialog( getActiveWindow(), message, title, JOptionPane.YES_NO_OPTION );
	}


    /**
     * Display a warning dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        displayWarning( exception.getClass().getName(), message );
    }


    /**
     * Display a warning dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayWarning( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.WARNING_MESSAGE );
    }


    /**
     * Display a warning dialog box with information about the exception.
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayWarning( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        final String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.WARNING_MESSAGE );
    }


    /**
     * Display an error dialog box.
     * @param title Title of the warning dialog box.
     * @param message The warning message to appear in the warning dialog box.
     */
    static public void displayError( final String title, final String message ) {
        Toolkit.getDefaultToolkit().beep();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }


    /**
     * Display an error dialog box with information about the exception.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        displayError( exception.getClass().getName(), message );
    }


    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayError( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }


    /**
     * Display an error dialog box with information about the exception.  This method allows
     * clarification about the consequences of the exception (e.g. "Save Failed:").
     * @param title Title of the warning dialog box.
     * @param prefix Text that should appear in the dialog box before the exception messasge.
     * @param exception The exception about which the warning dialog is displayed.
     */
    static public void displayApplicationError( final String title, final String prefix, final Exception exception ) {
        Toolkit.getDefaultToolkit().beep();
        String message = prefix + "\n" + "Exception: " + exception.getClass().getName() + "\n" + exception.getMessage();
        JOptionPane.showMessageDialog( getActiveWindow(), message, title, JOptionPane.ERROR_MESSAGE );
    }
}
