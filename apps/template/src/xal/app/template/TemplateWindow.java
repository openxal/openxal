/*
 * TemplateWindow.java
 *
 * Created on Fri Oct 10 15:12:03 EDT 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.template;

import xal.extension.application.*;

import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;


/**
 * TemplateViewerWindow
 *
 * @author  somebody
 */
class TemplateWindow extends XalWindow implements SwingConstants {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    /** Creates a new instance of MainWindow */
    public TemplateWindow(final XalDocument aDocument) {
        super(aDocument);
        setSize(800, 600);
    }
}




