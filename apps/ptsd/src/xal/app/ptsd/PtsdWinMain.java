/*
 * PtsdWinMain.java
 *
 * Created on Tues July 22 
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ptsd;

import xal.extension.application.XalDocument;
import xal.extension.smf.application.AcceleratorWindow;

import javax.swing.JFrame;
import javax.swing.SwingConstants;

/**
 * TemplateViewerWindow
 *
 * @author  somebody
 */
class PtsdWinMain extends AcceleratorWindow implements SwingConstants {
    
    
    /**  long */
    private static final long serialVersionUID = 1L;
    
    
    /*
     * Local Attributes
     */
    
    
    /** display frame for signal plots */
    private JFrame      frmPlots;
    
    /** display frame for appliation options */
    private JFrame      frmOptions;

    
    
    /** 
     * Creates a new instance of MainWindow
     *  
     * @param aDocument
     *  
     **/
    public PtsdWinMain(final XalDocument aDocument) {
        super(aDocument);
        setSize(800, 600);
    }
}




