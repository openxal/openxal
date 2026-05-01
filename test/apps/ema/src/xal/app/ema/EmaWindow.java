/*
 * Window.java
 *
 * Created on March 9, 2005
 */

package xal.app.ema;

import javax.swing.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.border.*;
import java.text.*;
import java.io.*;

import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.impl.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;

/**
 * Controls the swing componenet setup for the Window Application
 *
 * @author  jdg
 */
public class EmaWindow extends AcceleratorWindow {
    /** serialization identifier */
    private static final long serialVersionUID = 1L;
    protected JTabbedPane mainTabbedPane;
    private EmaDocument theDoc;
    /** main panels */ 
   static protected Color buttonColor = new Color(0,225,225);
    protected JTextField errorText;
    protected JList<Object> presetBPMList;
    protected DecimalField energyGuessField, minCurField;
    
    /** list of linac BPMs the user can pick from */
    protected JList<Object> BPM1List = new JList<Object>();
    protected JList<Object> BPM2List = new JList<Object>();
    protected JList<Object> BCMList = new JList<Object>();
    /** the date of the last linac BPM update */
    protected JLabel linacBPMDate = new JLabel("Not Started");
    
    /** the bpm panel */
    protected JSplitPane bpmPanel;
       
    /** Creates a new instance of MainWindow */

    public EmaWindow(EmaDocument aDocument) {
        super(aDocument);
        setSize(950, 650);
	theDoc = aDocument;
	
	errorText = new JTextField();
	errorText.setForeground(java.awt.Color.RED);
	
        makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {

        Container container = getContentPane();
	// panel for main control and setup
	//JPanel setupPanel = makeSetupPanel();

	BPM1List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	BPM2List.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	BCMList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	
	mainTabbedPane = new JTabbedPane();
	mainTabbedPane.setVisible(true);

	makeBPMPanel();
	mainTabbedPane.add("Linac Avg Energy",  bpmPanel);
		
	container.add(mainTabbedPane,BorderLayout.CENTER);
	container.add(errorText,BorderLayout.SOUTH);
    }    
    
    /** contruct the panel to do the accelerator device selection / setup */
    private void makeBPMPanel() {
	JPanel setupPanel = makeBPMSetupPanel(theDoc.bpmController);
	JPanel resultsPane = makeResultsPanel();
	
	//bpmPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, setupPanel, tablePanel);
	bpmPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, setupPanel, resultsPane);
	bpmPanel.setOneTouchExpandable(true);
    }
    
    protected void updateListData() {
	BPM1List.setListData(theDoc.linacBPMs.toArray());
	BPM2List.setListData(theDoc.linacBPMs.toArray());
	BCMList.setListData(theDoc.bpmController.theBCMs.toArray());
	BCMList.setSelectedIndex(0);
    }
    
    /** make a panel to display linac BPM results */
    private JPanel makeResultsPanel() {
    	Insets sepInsets = new Insets(5, 0, 5, 0);
	Insets nullInsets = new Insets(0, 0, 0, 0);
	GridBagConstraints gbc = new GridBagConstraints();
	GridBagLayout rsGridBag = new GridBagLayout();
	int sumy = 0;
	gbc.gridwidth = 2;gbc.gridheight = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	
	JPanel resultsPane = new JPanel(new BorderLayout());
	resultsPane.setLayout(rsGridBag);
	rsGridBag.setConstraints(linacBPMDate, gbc);
	resultsPane.add(linacBPMDate);
	linacBPMDate.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	
	gbc.gridwidth = 3;
	gbc.gridy = sumy;
	sumy +=5;
	JScrollPane tablePane = new JScrollPane(theDoc.bpmController.bpmResultsTable);
	rsGridBag.setConstraints(tablePane, gbc);
	resultsPane.add(tablePane);
	
	gbc.gridwidth = 1;
	gbc.gridx = 0; gbc.gridy = sumy;
	JButton resetButton = new JButton("Reset");
	rsGridBag.setConstraints(resetButton, gbc);
	resultsPane.add(resetButton);
	resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                  theDoc.bpmController.resetStats();
            }
	});
	
	gbc.gridwidth = 1;
	gbc.gridx = 1; gbc.gridy = sumy;
	JButton removeButton = new JButton("Remove Selected");
	rsGridBag.setConstraints(removeButton, gbc);
	resultsPane.add(removeButton);
	removeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                  theDoc.bpmController.removeSelectedPairs();
            }
	});	
	
	JButton exportTableButton = new JButton("Export Table");
	exportTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                  theDoc.bpmController.exportAvgTable();
            }
	});
	gbc.gridx = 2; gbc.gridy = sumy++;
	rsGridBag.setConstraints(exportTableButton, gbc);
	resultsPane.add(exportTableButton);
	
	// button to start the monitoring
	JButton startButton = new JButton("Start Monitoring");
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.insets = nullInsets;
	gbc.gridx = 0; gbc.gridy = sumy;
	rsGridBag.setConstraints(startButton , gbc);
	resultsPane.add(startButton);
	startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.bpmController.startBPMMeter();	
            }
	});
	
	// button to stop the monitoring
	JButton stopButton = new JButton("Stop Monitoring");
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.insets = nullInsets;
	gbc.gridx = 1; gbc.gridy = sumy++;
	rsGridBag.setConstraints(stopButton , gbc);
	resultsPane.add(stopButton);
	stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.bpmController.stopBPMMeter();	
            }
	});
	
	return resultsPane;
    }

    /** makes a panel to setup and contorol the action of a BPM cont6roller */
    private JPanel makeBPMSetupPanel(final BPMController bpmController) {
    	Insets sepInsets = new Insets(5, 0, 5, 0);
	Insets nullInsets = new Insets(0, 0, 0, 0);
	GridBagConstraints gbc = new GridBagConstraints();
	
	JPanel setupPanel = new JPanel();
	GridBagLayout spGridBag = new GridBagLayout();
	setupPanel.setLayout(spGridBag);
	setupPanel.setPreferredSize( new Dimension(250,500));

	int sumy = 0;
	
	JLabel topLabel = new JLabel("BPM Pair Selection");
	gbc.insets =nullInsets;
	topLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(topLabel, gbc);
	setupPanel.add(topLabel);

	JSeparator sep0 = new JSeparator(SwingConstants.HORIZONTAL);
	sep0.setVisible(true);
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	//JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);	
	spGridBag.setConstraints(sep0, gbc);
	setupPanel.add(sep0);
	
	JLabel preSetBPMLabel = new JLabel("Preset BPM Pairs");
	gbc.insets =nullInsets;
	preSetBPMLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	
	gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(preSetBPMLabel, gbc);
	setupPanel.add(preSetBPMLabel);

	presetBPMList= new JList<Object>();
	//JScrollPane BPMSelectScrollPane = new JScrollPane(presetBPMList);
	//presetBPMList.setVisibleRowCount(8);
	//presetBPMList.setListData(theDoc.bpmController.getDefaultPairs().toArray());
	gbc.gridx = 0; gbc.gridy = sumy; gbc.gridheight = 5;
	//spGridBag.setConstraints(BPMSelectScrollPane, gbc);
	//setupPanel.add(BPMSelectScrollPane);
	spGridBag.setConstraints(presetBPMList, gbc);
	setupPanel.add(presetBPMList);

	sumy+=5;

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1; gbc.gridwidth = 2;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
	spGridBag.setConstraints(sep1, gbc);
	setupPanel.add(sep1);
	
	gbc.gridwidth = 2;
	JLabel customPairLabel = new JLabel("Custom Pair");
	customPairLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	gbc.gridheight = 1; 
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(customPairLabel, gbc);
	setupPanel.add(customPairLabel);
	
	JLabel BPM1Label = new JLabel("Select BPM1");
	gbc.insets = nullInsets;
	gbc.gridheight = 1; gbc.gridwidth = 1;
	gbc.gridx = 0; gbc.gridy = sumy;
	spGridBag.setConstraints(BPM1Label, gbc);
	setupPanel.add(BPM1Label);

	
	JLabel BPM2Label = new JLabel("Select BPM2");
	gbc.gridheight = 1; gbc.gridwidth = 1;
	gbc.gridx = 1; gbc.gridy = sumy++;
	spGridBag.setConstraints(BPM2Label, gbc);
	setupPanel.add(BPM2Label);
	
	JScrollPane BPM1SelectScrollPane = new JScrollPane(BPM1List);
	JScrollPane BPM2SelectScrollPane = new JScrollPane(BPM2List);
	BPM1List.setVisibleRowCount(5);
	BPM2List.setVisibleRowCount(5);
	
	gbc.weighty = 1.;gbc.gridwidth = 1;
	gbc.gridx = 0; gbc.gridy = sumy; gbc.gridheight = 5;
	spGridBag.setConstraints(BPM1SelectScrollPane, gbc);	
	setupPanel.add(BPM1SelectScrollPane);

	gbc.weighty = 1.;gbc.gridwidth = 1;
	gbc.gridx = 1; gbc.gridy = sumy; gbc.gridheight = 5;
	spGridBag.setConstraints(BPM2SelectScrollPane, gbc);	
	setupPanel.add(BPM2SelectScrollPane);
	sumy += 5;
	
	gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(sep1, gbc);
	setupPanel.add(sep1);	
	
	JLabel energyGuessLabel = new JLabel("Energy Guess (MeV)");
	energyGuessLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
	gbc.gridheight = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(energyGuessLabel, gbc);
	setupPanel.add(energyGuessLabel);
	
	energyGuessField = new DecimalField(950., 9);
    gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	spGridBag.setConstraints(energyGuessField, gbc);
	setupPanel.add(energyGuessField);

	// visual seperator 
	
	sep1.setVisible(true);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	//JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);	
	spGridBag.setConstraints(sep1, gbc);
	setupPanel.add(sep1);
	
	// visual seperator 
	
	JLabel BCMLabel = new JLabel("BCM Filter");
	gbc.gridheight = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	spGridBag.setConstraints(BCMLabel, gbc);
	setupPanel.add(BCMLabel);
	
	JLabel BCMCurLabel = new JLabel("Min. Cur (mA)");
	gbc.gridheight = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy++;
	spGridBag.setConstraints(BCMCurLabel, gbc);
	setupPanel.add(BCMCurLabel);

	gbc.gridx = 0; gbc.gridy = sumy; 
	gbc.gridwidth = 1;
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 0.; gbc.weighty = 0.;
	spGridBag.setConstraints(BCMList, gbc);
	setupPanel.add(BCMList);
	
	minCurField = new DecimalField(5., 4);
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy++;
	spGridBag.setConstraints(minCurField, gbc);
	setupPanel.add(minCurField);

	gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;	
	spGridBag.setConstraints(sep1, gbc);
	setupPanel.add(sep1);

	JButton pickPresetBPMs = new JButton("Add selected");
	gbc.gridheight = 1;
	gbc.gridx = 0; gbc.gridy = sumy++;	spGridBag.setConstraints(pickPresetBPMs, gbc);
	setupPanel.add(pickPresetBPMs);
	pickPresetBPMs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bpmController.addSelectedPairs(presetBPMList.getSelectedValuesList().toArray());
            }
	});		
	return setupPanel;

    }
    
    /** make a nice displayable string from a number */
    
     public String prettyString(double num) {

        // force the number display as the format of either "0.0000" or "0.000E0" depending on the value
        DecimalFormat fieldFormat;      
        if (Math.abs(num) > 10000. || Math.abs(num) < 0.1)
            fieldFormat = new DecimalFormat("0.000E0");
        else 
            fieldFormat = new DecimalFormat("#####.#");
        return fieldFormat.format(num);
     }
     
     
         /** dump the contents of the summary table to a text file */
    protected void exportTable() {
	    File file;
	    JFileChooser chooser = new JFileChooser();
	    int returnVal = chooser.showOpenDialog(theDoc.myWindow());
	    if(returnVal == JFileChooser.APPROVE_OPTION) {
		   file = chooser.getSelectedFile();
		   try {
			   OutputStream ofStream = new FileOutputStream(file);
			   //String tableText = theDoc.controller.cavScaleTableModel.getText();
			   //ofStream.write(tableText.getBytes());
			   ofStream.close();
		   }
		   catch (Exception exc) {
			   System.out.println("Problem exporting bpm energy table " + exc.getMessage());
		   }
	    }
    }
}
