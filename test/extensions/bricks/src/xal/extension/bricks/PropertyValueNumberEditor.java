//
//  PropertyValueNumberEditor.java
//  xal
//
//  Created by Thomas Pelaia on 7/5/06.
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.extension.bricks;

import java.beans.*;
import java.lang.reflect.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.border.*;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;


/** property value editor */
abstract public class PropertyValueNumberEditor<T> extends PropertyValueTextEditor<T> {
	/** Constructor */
	public PropertyValueNumberEditor() {
		((JTextField)EDITOR_COMPONENT).setHorizontalAlignment( JTextField.RIGHT );
		((JLabel)RENDERING_COMPONENT).setHorizontalAlignment( JLabel.RIGHT );
	}
}
