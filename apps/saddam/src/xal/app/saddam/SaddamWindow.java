/*
 * Saddam.java
 *
 * Created on June 12, 2004
 */

package xal.app.saddam;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.extension.widgets.plot.*;
import xal.extension.scan.*;
/**
 * Controls the swing componenet setup for the Saddam Application
 *
 * @author  jdg
 */
public class SaddamWindow extends AcceleratorWindow {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    private SaddamDocument theDoc;
    /** the list that contains the types in the chossen sequence */
    protected JList<String> typeList = new JList<String>();
    protected JList<String> signalList = new JList<String>();
    protected  JComboBox<String> actionChoice;
    protected JLabel sequenceLabel = new JLabel("No sequence selected");
    
    protected Vector<String> nullDevices, nullSignals, nullValues, actionChoices;
    
    private JPanel mainPanel = new JPanel();

    protected DoubleInputTextField valueField;
    protected JList<String> valueList;
    
    protected JTextArea textArea;
    
    //------------------------------------------------------------
    //parameter PV controller and panel
    //-----------------------------------------------------------
     
    /** Creates a new instance of MainWindow */
    public SaddamWindow(SaddamDocument aDocument) {
        super(aDocument);
        setSize(600, 500);
	theDoc = aDocument;
 	nullDevices = new Vector<String>();
	nullDevices.add("Pick Sequence");
	nullSignals = new Vector<String>();
	nullSignals.add("Pick Device Type");
	nullValues = new Vector<String>();
	nullValues.add("No Values Available");
	actionChoices = new Vector<String>();
	actionChoices.add("Set Value");
	actionChoices.add("Increment Value");
	actionChoices.add("Multiply Value");	
	actionChoice = new JComboBox<String>(actionChoices);
	
        makeContent();
        Container container = getContentPane();
	container.add(mainPanel);
	
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {

 	// panel for main control and setup
	GridBagLayout setGridBag = new GridBagLayout();
	mainPanel.setLayout(setGridBag);

	Insets sepInsets = new Insets(5, 0, 5, 0);
	Insets nullInsets = new Insets(0, 0, 0, 0);
	Insets defaultInsets = new Insets(5,5,5,5);
	
	int sumy = 0;
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	gbc.insets = new Insets(10, 5, 10,5);
	gbc.gridwidth = 3;
	setGridBag.setConstraints(sequenceLabel, gbc);
	mainPanel.add(sequenceLabel);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.weightx = 1.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
	setGridBag.setConstraints(sep1, gbc);
	mainPanel.add(sep1);
	sep1.setVisible(true);
	
	JLabel typeLabel = new JLabel("Device Type");
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(typeLabel, gbc);
	mainPanel.add(typeLabel);
	
	JLabel pvLabel = new JLabel("PV Choice");
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(pvLabel, gbc);
	mainPanel.add(pvLabel);
	
	JLabel actionLabel = new JLabel("Action Choice");
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 2; gbc.gridy = sumy++;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(actionLabel, gbc);
	mainPanel.add(actionLabel);	
	
	typeList.setListData(nullDevices);
	typeList.setVisibleRowCount(6);
	typeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);	
	JScrollPane typeScrollPane = new JScrollPane(typeList);
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(typeScrollPane, gbc);
	mainPanel.add(typeScrollPane);
	typeList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                theDoc.typeSelected();
            }
	});	
	
	signalList.setListData(nullSignals);
	signalList.setVisibleRowCount(6);
	signalList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);		
	JScrollPane signalScrollPane = new JScrollPane(signalList);
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(signalScrollPane, gbc);
	mainPanel.add(signalScrollPane);
	signalList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                theDoc.signalSelected();
            }
	});	
	
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 2; gbc.gridy = sumy++;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(actionChoice, gbc);
	mainPanel.add(actionChoice);

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridwidth = 3;	gbc.weightx = 1.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);
	setGridBag.setConstraints(sep2, gbc);
	mainPanel.add(sep2);
	sep2.setVisible(true);
	
	JLabel setValueLabel = new JLabel("Set Value");
	gbc.gridwidth = 1;
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(setValueLabel, gbc);
	mainPanel.add(setValueLabel);

	JLabel selectValueLabel = new JLabel("Select Value");
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy++;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(selectValueLabel, gbc);
	mainPanel.add(selectValueLabel);
	
	valueField = new DoubleInputTextField( (new Double(theDoc.newSetValue)).toString());
	valueField.setNumberFormat(new DecimalFormat("####.#####"));
	valueField.setEnabled(false);
	valueField.setPreferredSize(new Dimension(80, 20));
	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 0; gbc.gridy = sumy;
	valueField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.newSetValue= valueField.getValue();
            }
	});		
	setGridBag.setConstraints(valueField, gbc);
	mainPanel.add(valueField);

	valueList = new JList<String>(nullValues);
	valueList.setEnabled(false);
	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	valueList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                theDoc.newSetValueString =  valueList.getSelectedValue();
            }
	});		
	setGridBag.setConstraints(valueList, gbc);
	mainPanel.add(valueList);
	
	JSeparator sep3 = new JSeparator(SwingConstants.HORIZONTAL);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridwidth = 3;	gbc.weightx = 1.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	setGridBag.setConstraints(sep2, gbc);
	mainPanel.add(sep3);
	sep3.setVisible(true);
	
	JButton confirmButton = new JButton("Confirm");
	confirmButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.confirmIt();		
            }
	});		
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(confirmButton, gbc);
	mainPanel.add(confirmButton);
	
	
	textArea = new JTextArea("");
	JScrollPane textScrollPane = new JScrollPane(textArea);
	textScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        textScrollPane.setPreferredSize(new Dimension(100, 150));
        textScrollPane.setMinimumSize(new Dimension(10, 10));	
	gbc.insets = nullInsets;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridwidth = 2; gbc.gridheight = 2;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	setGridBag.setConstraints(textScrollPane, gbc);
	mainPanel.add(textScrollPane);		
	// The analysis plot panel:	
	
	JButton doItButton = new JButton("Set'em");
	doItButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.fireSignals();		
            }
	});	
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.insets = defaultInsets;
	gbc.gridwidth = 1;
	setGridBag.setConstraints(doItButton, gbc);
	mainPanel.add(doItButton);	
	
	// the type selection list:
	
	//	container.add(mainTabbedPane,BorderLayout.CENTER);
	//container.add(errorText,BorderLayout.SOUTH);

    }
    
    /** update the type list up with a new set of types */
    
    protected void updateTypeList() {
	    sequenceLabel.setText("Sequence = " + theDoc.theSequence.getId());
	    typeList.setListData(theDoc.theTypes);
	    signalList.setListData(nullSignals);
	    valueField.setEnabled(false);
	    valueList.setEnabled(false);
    }
    
    /** the types were reset - so reset all dependent components */
    
    protected void typesReset() {
	    valueList.setListData(nullValues);
	    valueField.setEnabled(false);
	    valueList.setEnabled(false);	    	    
    }
    
}
