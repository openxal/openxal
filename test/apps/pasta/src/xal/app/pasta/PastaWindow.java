/*
 * Pasta.java
 *
 * Created on June 12, 2004
 */

package xal.app.pasta;

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
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.scan.*;
import xal.sim.scenario.Scenario;

/**
 * Controls the swing componenet setup for the Pasta Application
 *
 * @author  jdg
 */
public class PastaWindow extends AcceleratorWindow {
    
    private static final long serialVersionUID = 1L;
    
    protected JTabbedPane mainTabbedPane;
    private PastaDocument theDoc;
    /** main panels */ 
    private JPanel setupPanel, scanPanel,  scan1DPanel, analysisSetupPanel;
    private JSplitPane analysisSplitPane;
    /** the lists to choose BPMs and cavity */
    protected JList<Object>  BPM1List, BPM2List, cavityList, BCMList;//, algorithmList;
    private JScrollPane BPM1SelectScrollPane,BPM2SelectScrollPane, cavitySelectScrollPane, BCMSelectScrollPane;
    private JLabel BPM1ListLabel, BPM2ListLabel, cavityListLabel, BCMListLabel;
    protected JComboBox<String> algorithmChooser;
    protected JTextArea pvListTextArea, pvListTextArea1D;
    private JFileChooser jfc;
    /** label to display design amp + phase */
    protected JTextArea designValLabel = new JTextArea("Design Values");
    /** area to display the calculated setpoint values */
    protected JTextArea setpointValArea = new JTextArea("");
    /** a pane; to hold radio buttons to select the scan curve for which the cavity
    * voltage applies */
    private JPanel radioPanel = new JPanel();
    
    /** checkboxes to indicate whether the BPMs will be used in analysis. */
    protected  JCheckBox useBPM1Box, useBPM2Box, useCavOffBox;
    
    /** panel to hold radio buttons for scan use selection */
    private JPanel selectScansPanel = new JPanel();
    
    /** Analysis panel components */
     private JTable analysisSetupTable;
    
    protected JTextField errorText;
    
    protected JProgressBar progressBar = new JProgressBar();
    
    protected JButton setupButton, spButton, matchButton, setPntButton;
    protected DoubleInputTextField timeoutTextField, minScanPhaseField, maxScanPhaseField, minBPMAmpField, nModelStepsField, DTLPhaseOffsetField, BPMPhaseDiffOffsetField;
    protected JTextField errorField;
    
		
		//buttons to remove scan points
		protected JButton removeCavOnPointButton = new JButton("Remove One Scan Point");
		protected JButton removeCavOffPointButton = new JButton("Remove One Scan Point");
		
    //private Color buttonColor = Color.CYAN;
    private Color buttonColor = new Color(0,225,255);
    
    private DecimalFormat spFormat= new DecimalFormat("####.###");   
    
    /** button to select data wrapping about 360 deg. */
    protected JToggleButton useWrappingButton = new JToggleButton("unwrap data");
    
    /** button to also plot the output energy */
    protected JToggleButton plotWOutButton = new JToggleButton("plot E-Out");
    
    /**graph panel to display scanned data */
    protected FunctionGraphsJPanel graphAnalysis     = new FunctionGraphsJPanel();  
 
   //------------------------------------------------------------
    //parameter PV controller and panel
    //------------------------------------------------------------
     
    /** Creates a new instance of MainWindow */
    public PastaWindow(PastaDocument aDocument) {
        super(aDocument);
        setSize(950, 825);
	theDoc = aDocument;
        BPM1List = new JList<Object>();
        BPM2List = new JList<Object>();
        cavityList = new JList<Object>();
	BCMList = new JList<Object>();
	
	//String algNames[] = {"Random", "Simplex", "Combo"};
	String algNames[] = {"Random", "Simplex", "Powell"};
	algorithmChooser = new JComboBox<String>(algNames);
	algorithmChooser.setSelectedIndex(1);

	BPM1ListLabel = new JLabel("BPM 1");
	BPM1ListLabel.setHorizontalAlignment(SwingConstants.CENTER);
	BPM2ListLabel = new JLabel("BPM 2");
	BPM2ListLabel.setHorizontalAlignment(SwingConstants.CENTER);	
	cavityListLabel = new JLabel("Cavity");
	cavityListLabel.setHorizontalAlignment(SwingConstants.CENTER);
	BCMListLabel = new JLabel("Validator BCM ");
	BCMListLabel.setHorizontalAlignment(SwingConstants.CENTER);
		
	BPM1SelectScrollPane = new JScrollPane(BPM1List);
	BPM2SelectScrollPane = new JScrollPane(BPM2List);
	cavitySelectScrollPane = new JScrollPane(cavityList);
	BCMSelectScrollPane = new JScrollPane(BCMList);	
	
	setupButton = new JButton("Set Selections");
	setupButton.setBackground(buttonColor);
	
	// scan stuff
	pvListTextArea = new JTextArea();
        pvListTextArea.setLineWrap(true);
        pvListTextArea.setWrapStyleWord(true);	
	pvListTextArea1D = new JTextArea();
        pvListTextArea1D .setLineWrap(true);
        pvListTextArea1D.setWrapStyleWord(true);		
	// analysis panel stuff
	
	useWrappingButton.setSelected(false);
	
        analysisSetupTable = new JTable(theDoc.analysisStuff.analysisTableModel); 	
		
	errorText = new JTextField();
	errorText.setForeground(java.awt.Color.RED);
	
	SimpleChartPopupMenu.addPopupMenuTo(graphAnalysis);
        graphAnalysis.setOffScreenImageDrawing(true);
        graphAnalysis.setName("Analysis : BPM Phase Difference vs. Cavity Phase");
        graphAnalysis.setAxisNames("Cavity Phase (deg)","BPM Phase Diff (deg)");
        graphAnalysis.setGraphBackGroundColor(Color.white);
	graphAnalysis.setLegendButtonVisible(true);
	
	//remove point buttons actions
	removeCavOnPointButton.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				Vector<BasicGraphData> gdV = theDoc.scanStuff.graphScan.getAllGraphData();
				double minX = theDoc.scanStuff.graphScan.getCurrentMinX();
				double maxX = theDoc.scanStuff.graphScan.getCurrentMaxX();
				double minY = theDoc.scanStuff.graphScan.getCurrentMinY();
				double maxY = theDoc.scanStuff.graphScan.getCurrentMaxY();
				Object [] resObjs = GraphDataOperations.getGraphDataAndPointIndexInside(gdV,minX,maxX,minY,maxY);
				if(resObjs == null || resObjs.length != 2){
					errorText.setText(null);
					errorText.setText("Use zoom to select one point!");
					return;
				}
				BasicGraphData gd = (BasicGraphData) resObjs[0];
				Integer IndP = (Integer) resObjs[1];
				if (gd != null && IndP != null) {
					int ind = IndP.intValue();
					for(int i = 0; i < gdV.size(); i++){
						BasicGraphData gd0 = gdV.get(i);
						gd0.removePoint(ind);
					}
					theDoc.scanStuff.graphScan.clearZoomStack();
					theDoc.scanStuff.graphScan.refreshGraphJPanel();
				} else {
					errorText.setText(null);
					errorText.setText("Use zoom to select one point!");
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				errorText.setText(null);
			}
	});	    	
	
	removeCavOffPointButton.addActionListener(new java.awt.event.ActionListener(){
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				
				Vector<BasicGraphData> gdV = theDoc.scanStuff.graphScan1D.getAllGraphData();
				double minX = theDoc.scanStuff.graphScan1D.getCurrentMinX();
				double maxX = theDoc.scanStuff.graphScan1D.getCurrentMaxX();
				double minY = theDoc.scanStuff.graphScan1D.getCurrentMinY();
				double maxY = theDoc.scanStuff.graphScan1D.getCurrentMaxY();
				Object [] resObjs = GraphDataOperations.getGraphDataAndPointIndexInside(gdV,minX,maxX,minY,maxY);
				if(resObjs == null || resObjs.length != 2){
					errorText.setText(null);
					errorText.setText("Use zoom to select one point!");
					return;
				}
				BasicGraphData gd = (BasicGraphData) resObjs[0];
				Integer IndP = (Integer) resObjs[1];
				if (gd != null && IndP != null) {
					int ind = IndP.intValue();
					for(int i = 0; i < gdV.size(); i++){
						BasicGraphData gd0 = gdV.get(i);
						gd0.removePoint(ind);
					}
					theDoc.scanStuff.graphScan1D.clearZoomStack();
					theDoc.scanStuff.graphScan1D.refreshGraphJPanel();
				} else {
					errorText.setText(null);
					errorText.setText("Use zoom to select one point!");
					Toolkit.getDefaultToolkit().beep();
					return;
				}
				errorText.setText(null);
			}
	});	
	
	
	
	
	makeContent();
    }
    
    /**
     * Create the main window subviews.
     */
    protected void makeContent() {

        Container container = getContentPane();
	// panel for main control and setup
	makeSetupPanel();

       	makeScanPanel();
       	makeScan1DPanel();
	
	makeAnalysisPanel();
	
	mainTabbedPane = new JTabbedPane();
	mainTabbedPane.setVisible(true);
	
	mainTabbedPane.add("Setup", setupPanel);
	mainTabbedPane.add("Scan-Cavity Off", scan1DPanel);
	mainTabbedPane.add("Scan- Cavity On", scanPanel);
	mainTabbedPane.add("Analysis", analysisSplitPane);
	
	container.add(mainTabbedPane,BorderLayout.CENTER);
	container.add(errorText,BorderLayout.SOUTH);

    }    
    
 
    /** contruct the panel to do the accelerator device selection / setup */
    private void makeSetupPanel() {
       	setupPanel = new JPanel();
	GridBagLayout spGridBag = new GridBagLayout();
	setupPanel.setLayout(spGridBag);
	setupPanel.setPreferredSize( new Dimension(200,200));
	
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.BOTH;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = 0;
	spGridBag.setConstraints(BPM1ListLabel, gbc);
	setupPanel.add(BPM1ListLabel);
	gbc.gridx = 1; gbc.gridy = 0;
	spGridBag.setConstraints(BPM2ListLabel, gbc);
	setupPanel.add(BPM2ListLabel);
	gbc.gridx = 2; gbc.gridy = 0;
	spGridBag.setConstraints(cavityListLabel, gbc);
	setupPanel.add(cavityListLabel);
	gbc.gridx = 3; gbc.gridy = 0;
	spGridBag.setConstraints(BCMListLabel, gbc);
	setupPanel.add(BCMListLabel);	
	
	BPM1List.setVisibleRowCount(8);
	gbc.weighty = 1.;
	gbc.gridx = 0; gbc.gridy = 1; gbc.gridheight = 5;
	spGridBag.setConstraints(BPM1SelectScrollPane, gbc);	
	setupPanel.add(BPM1SelectScrollPane);

	BPM2List.setVisibleRowCount(8);
	gbc.gridx = 1; gbc.gridy = 1; gbc.gridheight = 5;
	spGridBag.setConstraints(BPM2SelectScrollPane, gbc);	
	setupPanel.add(BPM2SelectScrollPane);

	cavityList.setVisibleRowCount(8);
	gbc.gridx = 2; gbc.gridy = 1; gbc.gridheight = 5;
	spGridBag.setConstraints(cavitySelectScrollPane, gbc);
	setupPanel.add(cavitySelectScrollPane );

	BCMList.setVisibleRowCount(8);
	gbc.gridx = 3; gbc.gridy = 1; gbc.gridheight = 5;
	spGridBag.setConstraints(BCMSelectScrollPane, gbc);
	setupPanel.add(BCMSelectScrollPane );
	
	gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = 6; gbc.gridwidth = 1;
	spGridBag.setConstraints(setupButton, gbc);
	setupPanel.add(setupButton);
	setupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setAccelComponents();		
            }
	});	    
    }
    
    /** construct the panel to control the scanning */
    
     private void makeScanPanel(){
	scanPanel = new JPanel(new BorderLayout());
	scanPanel.setPreferredSize( new Dimension(200,400));
	
	JPanel tmp_0 = new JPanel();
	tmp_0.setLayout(new VerticalLayout());
	tmp_0.add(theDoc.scanStuff.scanController.getJPanel());
	tmp_0.add(theDoc.scanStuff.avgCntr.getJPanel(0));
	tmp_0.add(theDoc.scanStuff.vldCntr.getJPanel());
	
	JScrollPane pvTextScrollPane = new JScrollPane(pvListTextArea);
	pvTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	
	pvTextScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	
	
	tmp_0.add(pvTextScrollPane);
	
	Border etchedBorder = BorderFactory.createEtchedBorder();
	
	scanPanel.add(tmp_0,BorderLayout.WEST);
	
	JPanel tmp_1 = new JPanel(new BorderLayout());
	tmp_1.add(theDoc.scanStuff.graphScan,BorderLayout.CENTER);
	JPanel tmp_2 = new JPanel(new FlowLayout());
	tmp_2.add(removeCavOnPointButton);
	tmp_1.add(tmp_2,BorderLayout.SOUTH);
	
	scanPanel.add(tmp_1,BorderLayout.CENTER); 
	
		 }
    
   /** construct the panel to control the scanning */
    
     private void makeScan1DPanel(){
	scan1DPanel = new JPanel(new BorderLayout());
	scan1DPanel.setPreferredSize( new Dimension(200,400));
	
        JPanel tmp_0 = new JPanel();
	tmp_0.setLayout(new VerticalLayout());
        tmp_0.add(theDoc.scanStuff.scanController1D.getJPanel());
        tmp_0.add(theDoc.scanStuff.avgCntr1D.getJPanel(0));
        tmp_0.add(theDoc.scanStuff.vldCntr1D.getJPanel());
	
	JScrollPane pvTextScrollPane = new JScrollPane(pvListTextArea1D);
	pvTextScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);	
	pvTextScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);	

	tmp_0.add(pvTextScrollPane);
	
	Border etchedBorder = BorderFactory.createEtchedBorder();
	
	JPanel tmp_1 = new JPanel(new BorderLayout());
	tmp_1.add(theDoc.scanStuff.graphScan1D,BorderLayout.CENTER);
	JPanel tmp_2 = new JPanel(new FlowLayout());
	tmp_2.add(removeCavOffPointButton);
	tmp_1.add(tmp_2,BorderLayout.SOUTH);
		
	scan1DPanel.add(tmp_0,BorderLayout.WEST);
	scan1DPanel.add(tmp_1,BorderLayout.CENTER); 
    }   
    
    /** construct the panel to control the analysis */
    
     private void makeAnalysisPanel(){
	      
	     
	Insets sepInsets = new Insets(5, 0, 5, 0);
	Insets nullInsets = new Insets(0, 0, 0, 0);
	     
	// layout the analysis panel
	analysisSetupPanel = new JPanel(new BorderLayout());
	GridBagLayout anGridBag = new GridBagLayout();
	analysisSetupPanel.setLayout(anGridBag);
	analysisSetupPanel.setPreferredSize( new Dimension(250,400));
        analysisSetupTable.getColumnModel().getColumn(0).setPreferredWidth(170);
        analysisSetupTable.getColumnModel().getColumn(1).setPreferredWidth(75);

	// from top -> down:
	int sumy = 0;
	
	// checkboxes for BPMs to use in analysis
	/*
	useBPM1Box = new JCheckBox("Use BPM1", true);
	useBPM2Box = new JCheckBox("Use BPM2", true);
	useCavOffBox = new JCheckBox("Use Cav Off", true);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy;
	gbc.gridwidth = 1;
	anGridBag.setConstraints(useBPM1Box, gbc);
	analysisSetupPanel.add(useBPM1Box);
	gbc.gridx = 1; gbc.gridy = sumy;
	anGridBag.setConstraints(useBPM2Box, gbc);
	analysisSetupPanel.add(useBPM2Box);
	gbc.gridx = 2; gbc.gridy = sumy++;
	anGridBag.setConstraints(useCavOffBox, gbc);
	analysisSetupPanel.add(useCavOffBox);
	*/
	JPanel checkBoxPanel = new JPanel();
	useBPM1Box = new JCheckBox("Use BPM1", true);
	useBPM2Box = new JCheckBox("Use BPM2", true);
	useCavOffBox = new JCheckBox("Use Cav Off", true);
	checkBoxPanel.add(useBPM1Box);
	checkBoxPanel.add(useBPM2Box);
	checkBoxPanel.add(useCavOffBox);
	GridBagConstraints gbc = new GridBagConstraints();
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	gbc.gridwidth = 2;
	anGridBag.setConstraints(checkBoxPanel , gbc);
	analysisSetupPanel.add(checkBoxPanel);
	
	
	// import scan data:
	
	JButton analysisSetupButton = new JButton("Import Scan Data");	
	analysisSetupButton.setBackground(buttonColor);
	gbc.fill = GridBagConstraints.NONE;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	gbc.insets = new Insets(10, 5, 10,5);
	gbc.gridwidth = 2;
	anGridBag.setConstraints(analysisSetupButton, gbc);
	analysisSetupPanel.add(analysisSetupButton);
	
	// min BPM amplitude
	
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = nullInsets;
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel minBPMAmpLabel = new JLabel("Minimum BPM Amplitude (mA) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(minBPMAmpLabel, gbc);
	analysisSetupPanel.add(minBPMAmpLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	minBPMAmpField = new DoubleInputTextField( (new Double(theDoc.analysisStuff.minBPMAmp)).toString());
	minBPMAmpField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.minBPMAmp = minBPMAmpField.getValue();	
            }
	});		
	anGridBag.setConstraints(minBPMAmpField, gbc);
	analysisSetupPanel.add(minBPMAmpField);
	
	// RF phase offset
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel DTLPhaseOffsetLabel = new JLabel("Offset in X (deg) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(DTLPhaseOffsetLabel, gbc);
	analysisSetupPanel.add(DTLPhaseOffsetLabel);	

	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	DTLPhaseOffsetField = new DoubleInputTextField( (new Double(theDoc.DTLPhaseOffset)).toString());
	DTLPhaseOffsetField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.DTLPhaseOffset = DTLPhaseOffsetField.getValue();	
            }
	});	
	anGridBag.setConstraints(DTLPhaseOffsetField, gbc);
	analysisSetupPanel.add(DTLPhaseOffsetField);
	
        /*
	// BPM phase offset
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel BPMPhaseOffsetLabel = new JLabel("Offset in Y (deg) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(BPMPhaseOffsetLabel, gbc);
	analysisSetupPanel.add(BPMPhaseOffsetLabel);	

	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	BPMPhaseDiffOffsetField = new DoubleInputTextField( (new Double(theDoc.BPMPhaseDiffOffset)).toString());
	BPMPhaseDiffOffsetField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.BPMPhaseDiffOffset = BPMPhaseDiffOffsetField.getValue();	
            }
	});	
	anGridBag.setConstraints(BPMPhaseDiffOffsetField, gbc);
	analysisSetupPanel.add(BPMPhaseDiffOffsetField);
	*/
        
	// use data wrapping button:
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	anGridBag.setConstraints(useWrappingButton, gbc);
	analysisSetupPanel.add(useWrappingButton);
	
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;	gbc.gridwidth = 2;
	gbc.weightx = 1.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep1 = new JSeparator(SwingConstants.HORIZONTAL);
	anGridBag.setConstraints(sep1, gbc);
	analysisSetupPanel.add(sep1);
	sep1.setVisible(true);
	
	// The design phase and amplitudes:
	
	//designValLabel.setHorizontalAlignment(SwingConstants.CENTER);	
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	anGridBag.setConstraints(designValLabel, gbc);
	analysisSetupPanel.add(designValLabel);	
	
	gbc.insets = nullInsets;
	JLabel tableLabel = new JLabel("Matching Variables:");
	tableLabel.setHorizontalAlignment(SwingConstants.CENTER);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	anGridBag.setConstraints(tableLabel, gbc);
	analysisSetupPanel.add(tableLabel);
	
	// The variable table
	
	gbc.weightx = 1.; gbc.weighty = 1.;
	gbc.fill = GridBagConstraints.BOTH;
	//gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.gridheight = 3;
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(analysisSetupTable, gbc);
	analysisSetupPanel.add(analysisSetupTable);
	sumy+=3;

        gbc.gridwidth = 1;	
	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JButton guessButton = new JButton("Initial Guess");
	guessButton.setBackground(buttonColor);	
	guessButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.initialGuess();
            }
	});		
	anGridBag.setConstraints(guessButton, gbc);
	analysisSetupPanel.add(guessButton);
        sumy+=2;
        
        gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep2 = new JSeparator(SwingConstants.HORIZONTAL);	
	anGridBag.setConstraints(sep2, gbc);
	analysisSetupPanel.add(sep2);
        
 	gbc.fill = GridBagConstraints.BOTH;
	gbc.insets = nullInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	updateScanNumberSelector();
	radioPanel.setPreferredSize(new Dimension(25, 100));
	anGridBag.setConstraints(radioPanel, gbc);
	analysisSetupPanel.add(radioPanel);	
	
	// scan use selection:
	
	gbc.fill = GridBagConstraints.BOTH;
	gbc.insets = nullInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	//selectScansPanel.setPreferredSize(new Dimension(25, 200));
	anGridBag.setConstraints(selectScansPanel, gbc);
	analysisSetupPanel.add(selectScansPanel);	

	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep3 = new JSeparator(SwingConstants.HORIZONTAL);	
	anGridBag.setConstraints(sep3, gbc);
	analysisSetupPanel.add(sep3);
	
	
	gbc.insets = nullInsets;
	JLabel setupLabel = new JLabel("Setup:");
	setupLabel.setHorizontalAlignment(SwingConstants.CENTER);	
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 0; gbc.gridy = sumy++;
	anGridBag.setConstraints(setupLabel, gbc);
	analysisSetupPanel.add(setupLabel);

	// min scan phase
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel minScanPhaseLabel = new JLabel("Minimum scan phase (deg) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(minScanPhaseLabel, gbc);
	analysisSetupPanel.add(minScanPhaseLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	minScanPhaseField = new DoubleInputTextField( (new Double(theDoc.analysisStuff.phaseModelMin)).toString());
	minScanPhaseField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.phaseModelMin = minScanPhaseField.getValue();
		theDoc.analysisStuff.makeCalcPoints();		
            }
	});		
	anGridBag.setConstraints(minScanPhaseField, gbc);
	analysisSetupPanel.add(minScanPhaseField);

	// max scan phase:
	
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel maxScanPhaseLabel = new JLabel("Maximum scan phase (deg) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(maxScanPhaseLabel, gbc);
	analysisSetupPanel.add(maxScanPhaseLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	maxScanPhaseField = new DoubleInputTextField( (new Double(theDoc.analysisStuff.phaseModelMax)).toString());
	maxScanPhaseField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.phaseModelMax = maxScanPhaseField.getValue();
		theDoc.analysisStuff.makeCalcPoints();		
            }
	});		
	anGridBag.setConstraints(maxScanPhaseField, gbc);
	analysisSetupPanel.add(maxScanPhaseField);

	// N model steps
	
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel nModelStepsLabel = new JLabel("Number of model steps/scan : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(nModelStepsLabel, gbc);
	analysisSetupPanel.add(nModelStepsLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	nModelStepsField = new DoubleInputTextField( (new Integer(theDoc.analysisStuff.nCalcPoints)).toString());
	nModelStepsField.setNumberFormat(new DecimalFormat("###"));
	nModelStepsField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.nCalcPoints = (int) nModelStepsField.getValue();
		theDoc.analysisStuff.makeCalcPoints();
            }
	});		
	anGridBag.setConstraints(nModelStepsField, gbc);
	analysisSetupPanel.add(nModelStepsField);

	// solver timeout limit
	
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel timeoutLabel = new JLabel("Solver time limit (sec) : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(timeoutLabel, gbc);
	analysisSetupPanel.add(timeoutLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	timeoutTextField = new DoubleInputTextField( (new Double(theDoc.analysisStuff.timeoutPeriod)).toString());
	timeoutTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.timeoutPeriod = timeoutTextField.getValue();	
            }
	});		
	anGridBag.setConstraints(timeoutTextField, gbc);
	analysisSetupPanel.add(timeoutTextField);
	
        /*
	// algorithm chooser
	
	gbc.weightx = 0.; gbc.weighty = 0.;
	JLabel algorithmLabel = new JLabel("Algorithm : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(algorithmLabel, gbc);
	analysisSetupPanel.add(algorithmLabel);	

	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	anGridBag.setConstraints(algorithmChooser, gbc);
	analysisSetupPanel.add(algorithmChooser);
	algorithmChooser.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.algorithmId = ((String) algorithmChooser.getSelectedItem());		
            }
	});
	
	// probe file chooser
	
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel probeChooserLabel = new JLabel("Pick a Probe");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(probeChooserLabel, gbc);
	analysisSetupPanel.add(probeChooserLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	JButton probeChooserButton = new JButton("Probe");
	jfc = new JFileChooser(theDoc.analysisStuff.probeFile);
	probeChooserButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                int result = jfc.showOpenDialog(theDoc.myWindow());
		if(result == JFileChooser.APPROVE_OPTION) {
			theDoc.analysisStuff.probeFile = jfc.getSelectedFile();
			theDoc.analysisStuff.setProbe(theDoc.analysisStuff.probeFile);
			// make sure  that RF phase is calculated	//(theDoc.analysisStuff.theProbe.getAlgorithm()).setRfGapPhaseCalculation(true);
		}
	    }
	});		
	anGridBag.setConstraints(probeChooserButton, gbc);
	analysisSetupPanel.add(probeChooserButton);
*/

	// visual seperator 
	
	gbc.gridwidth = 2;
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.insets = sepInsets;
	gbc.gridheight = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JSeparator sep4 = new JSeparator(SwingConstants.HORIZONTAL);	
	anGridBag.setConstraints(sep4, gbc);
	analysisSetupPanel.add(sep4);	
	
	
	// action buttons
	
	gbc.gridwidth = 1;	
	gbc.weightx = 0.; gbc.weighty = 0.;
	JLabel spLabel = new JLabel("Single Pass : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(spLabel, gbc);
	analysisSetupPanel.add(spLabel);

	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	spButton = new JButton("Single Pass");
	spButton.setBackground(buttonColor);	
	spButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //theDoc.analysisStuff.doCalc();
		CalcThread calcThread = new CalcThread(theDoc);
            }
	});		
	anGridBag.setConstraints(spButton, gbc);
	analysisSetupPanel.add(spButton);

	// solve button:
	
	gbc.weightx = 0.; gbc.weighty = 0.;
	JLabel matchLabel = new JLabel("Do the Matching : ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(matchLabel, gbc);
	analysisSetupPanel.add(matchLabel);

	gbc.weightx = 1.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	matchButton = new JButton("Start Solver");
	matchButton.setBackground(buttonColor);	
	matchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //theDoc.analysisStuff.solve();	
		SolveThread solveThread = new SolveThread(theDoc);		
            }
	});	
	anGridBag.setConstraints(matchButton, gbc);
	analysisSetupPanel.add(matchButton);
	
	
	// the progress bar
	gbc.weightx = 0.; gbc.weighty = 1.;
	gbc.gridwidth = 2;
	gbc.gridx = 0; gbc.gridy = sumy++;
	anGridBag.setConstraints(progressBar, gbc);
	analysisSetupPanel.add(progressBar);	
	
	// the residual error 	
	gbc.gridwidth = 1;
	gbc.weightx = 1.; gbc.weighty = 0.;
	JLabel errorLabel = new JLabel("Error: ");	
	gbc.gridx = 0; gbc.gridy = sumy;
	anGridBag.setConstraints(errorLabel, gbc);
	analysisSetupPanel.add(errorLabel);	
	
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 1; gbc.gridy = sumy++;
	errorField = new JTextField(prettyString(theDoc.analysisStuff.errorTotal) );
	anGridBag.setConstraints(errorField, gbc);
	analysisSetupPanel.add(errorField);

	// find setpoint stuff:
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 0; gbc.gridy = sumy;
	setPntButton = new JButton("Find Setpoints");
	setPntButton.setBackground(buttonColor);	
	setPntButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.updateSetpoints();
		
		String ans = "Phase = " + spFormat.format(theDoc.analysisStuff.cavPhaseSetpoint) + "\nAmp  = " + spFormat.format(theDoc.analysisStuff.cavAmpSetpoint) + "\nWOut (MeV) = " + spFormat.format(theDoc.analysisStuff.WOutCalc);
		setpointValArea.setText(ans);
            }
	});		
	anGridBag.setConstraints(setPntButton, gbc);
	analysisSetupPanel.add(setPntButton);
	
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 0.; gbc.weighty = 0.;
	gbc.gridx = 1; gbc.gridy = sumy++;
	anGridBag.setConstraints(setpointValArea, gbc);
	analysisSetupPanel.add(setpointValArea);
		
	// send new setpoint:
	gbc.weightx = 0.; gbc.weighty = 1.;	
	gbc.gridx = 0; gbc.gridy = sumy++;
	JButton sendSetPntButton = new JButton("Send New Setpoints to EPICS");
	sendSetPntButton.setBackground(buttonColor);	
	sendSetPntButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.sendNewSetpoints();
            }
	});
	anGridBag.setConstraints(sendSetPntButton, gbc);
	analysisSetupPanel.add(sendSetPntButton);

	// The analysis plot panel:	
	
	Border etchedBorder = BorderFactory.createEtchedBorder();
        JPanel graphPanel = new JPanel();
        graphPanel.setLayout(new BorderLayout());
        graphPanel.setBorder(etchedBorder);
        graphPanel.add(graphAnalysis,BorderLayout.CENTER);
	
	graphPanel.add(plotWOutButton, BorderLayout.SOUTH);

	plotWOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                if(theDoc.analysisStuff.haveData) theDoc.analysisStuff.plotUpdate();
		System.out.println(theDoc.analysisStuff.haveData);
            }
	});	 

	
	// add action listeners to buttons:
	analysisSetupButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                theDoc.analysisStuff.init();
            }
	});	 

	analysisSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, analysisSetupPanel, graphPanel);
    }   

    /** update the lists for the BPMs and cavitiy choices
     */

    protected void updateSelectionLists() {
	    
	BPM1List.setListData(theDoc.theBPMs.toArray());
	BPM2List.setListData(theDoc.theBPMs.toArray());	
	cavityList.setListData(theDoc.theCavities.toArray());
	BCMList.setListData(theDoc.theBCMs.toArray());	
    }
    
    /** method to handle the selection of the BPMs and Cavity to use */
    protected void setAccelComponents() {
	    
	theDoc.analysisStuff.modelReady = false;    
	BPM  BPM1 =  (BPM) BPM1List.getSelectedValue();   
	BPM  BPM2 =  (BPM) BPM2List.getSelectedValue(); 
	RfCavity cav = (RfCavity) cavityList.getSelectedValue();
	
	if(BPM1 == null || BPM2 == null || cav == null) {
		Toolkit.getDefaultToolkit().beep();
		errorText.setText("Hey - you need to select both BPMs and the cavity to use");
		return;
	}
	if(BPM1 == BPM2) {
		Toolkit.getDefaultToolkit().beep();
		errorText.setText("Hey - you need to select unique BPMs");
		return;
	}	
	if(theDoc.getSelectedSequence().getPosition(BPM1) < theDoc.getSelectedSequence().getPosition(cav) || theDoc.getSelectedSequence().getPosition(BPM2) < theDoc.getSelectedSequence().getPosition(cav) ) {
		Toolkit.getDefaultToolkit().beep();
		errorText.setText("The BPMs must be downstream of the cavity");
		return;		
	}	
	
	theDoc.BPM1 = BPM1;
	theDoc.BPM2 = BPM2;
	theDoc.theCavity = cav;
	theDoc.theBCM = (CurrentMonitor) BCMList.getSelectedValue(); 
	
	errorText.setText("");

	theDoc.scanStuff.updateScanVariables(BPM1, BPM2, cav, theDoc.theBCM);
	System.out.println("Devices set OK");
	System.out.println("BPM1 = " + BPM1.getId());
	System.out.println("BPM2 = " + BPM2.getId());
	System.out.println("cav = " + cav.getId());	
	pvListTextArea.setText(theDoc.scanStuff.thePVText);
	pvListTextArea1D.setText(theDoc.scanStuff.thePVText);
	// store the original phase + amp. before we iterate these during matching.
	theDoc.theDesignPhase = cav.getDfltCavPhase();
	theDoc.theDesignAmp = cav.getDfltCavAmp();
	// make a model 
	try {
		theDoc.analysisStuff.theModel = Scenario.newScenarioFor(theDoc.theSequence);
		theDoc.analysisStuff.probeInit();
		System.out.println("Model set for " + theDoc.theSequence.getId());
	}
	catch (Exception ex) {
		Toolkit.getDefaultToolkit().beep();
		String errText = "Darn! I couldn't set up the model for your selected sequence";
		errorText.setText(errText);
		System.err.println(errText);			
		return;			
	}
	updateDesignValLabel();
	theDoc.setHasChanges(true);	  
    } 
    
    protected void updateDesignValLabel() {
	String label = "Design phase (deg) = " + (new Double(theDoc.theDesignPhase)).toString();
	label += "\nDesign Amp (MV/m) = " + (new Double(theDoc.theDesignAmp)).toString();
	label += "\nInput Energy (MeV) = " +  (new Double(theDoc.analysisStuff.defaultEnergy)).toString();
	designValLabel.setText(label);
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
    
    /** set up the panel to display radio buttons for selecting the scan number 
    * corresponding to the cavityVoltage variable */
    
    protected void updateScanNumberSelector() {
	    radioPanel.removeAll();
	    radioPanel.setLayout(new FlowLayout());
	    JLabel label = new JLabel("Scan for Voltage Variable");
	    radioPanel.add(label);
	    if(theDoc.analysisStuff.nParamAmpVals ==0) return;
	    
	    ButtonGroup group = new ButtonGroup();
	    
	    for (int i=0; i< theDoc.analysisStuff.nParamAmpVals; i++) {
		    JRadioButton radio = new JRadioButton( (new Integer(i)).toString());
		    group.add(radio);
		    if(i == theDoc.analysisStuff.amplitudeVariableIndex) radio.setSelected(true);
		    radio.addItemListener(new java.awt.event.ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
				    String num = ((JRadioButton) e.getItemSelectable()).getText();
				    int j = Integer.parseInt(num);
				    theDoc.analysisStuff.amplitudeVariableIndex = j;
				    System.out.println("switched to " + j);
				    theDoc.analysisStuff.updateAmpFactors();
			    }
		    });   
		    radioPanel.add(radio);
	    }
	    
    }
    
     /** set up the panel to display radio buttons for selecting the scans to analyze */
    
    protected void updateScanUseSelector() {
	    selectScansPanel.removeAll();
	    selectScansPanel.setLayout(new FlowLayout());
	    JLabel label = new JLabel("Select scans to model");
	    selectScansPanel.add(label);
	    if(theDoc.analysisStuff.nParamAmpVals ==0) return;
	    	    
	    for (int i=0; i< theDoc.analysisStuff.nParamAmpVals; i++) {
		    final JRadioButton radioB = new JRadioButton( (new Integer(i)).toString());
		    radioB.setSelected(true);
		    radioB.addItemListener(new java.awt.event.ItemListener() {
			    public void itemStateChanged(ItemEvent e) {
				    String num = ((JRadioButton) e.getItemSelectable()).getText();
				    int j = Integer.parseInt(num);
				    boolean tf = radioB.isSelected();
				    theDoc.useScanInMatch.set(j, new Boolean(tf));
				    System.out.println("param " + j + " is " + tf);
			    }
		    });   
		    selectScansPanel.add(radioB);
	    }
	    
    }
    
    protected void updateInputFields() {    
	    minBPMAmpField.setValue(theDoc.analysisStuff.minBPMAmp);
	    DTLPhaseOffsetField.setValue(theDoc.DTLPhaseOffset);
	    //BPMPhaseDiffOffsetField.setValue(theDoc.BPMPhaseDiffOffset);
	    minScanPhaseField.setValue(theDoc.analysisStuff.phaseModelMin);
	    maxScanPhaseField.setValue(theDoc.analysisStuff.phaseModelMax);
	    nModelStepsField.setValue(theDoc.analysisStuff.nCalcPoints);
	    timeoutTextField.setValue(theDoc.analysisStuff.timeoutPeriod);
	    if(theDoc.analysisStuff.amplitudeVariableIndex > 0 ) updateScanNumberSelector();
    }
    
        /** plot the measured data to the graphPanel *
    *  any existing data is removed */
    
    protected void plotMeasuredData() {
	    
	    graphAnalysis.removeAllGraphData(); 
	    
	    Vector<Double> phaseCavMeasuredV, phaseDiffBPMMeasuredV;
	    BasicGraphData curveDataMeasured;
	    
	    for (int i = 0; i < theDoc.analysisStuff.phasesCavMeasured.size(); i++) {
		   phaseCavMeasuredV =  theDoc.analysisStuff.phasesCavMeasured.get(new Integer(i));
		   if(theDoc.analysisStuff.phaseDiffsBPMMeasured.size() <i+1) {
			String errText = "Oh no!, The cavity and BPM phase containers are different sizes\n - I can't plot this";
			theDoc.myWindow().errorText.setText(errText);
			System.err.println(errText);
			return;
		   }
		   phaseDiffBPMMeasuredV = theDoc.analysisStuff.phaseDiffsBPMMeasured.get(new Integer(i));
		   double [] pCavM = theDoc.analysisStuff.toDouble(phaseCavMeasuredV);
		   double [] pBPMM = theDoc.analysisStuff.toDouble(phaseDiffBPMMeasuredV);
		   curveDataMeasured = new BasicGraphData();
		   curveDataMeasured.addPoint(pCavM,pBPMM);
		   curveDataMeasured.setDrawLinesOn(true);
		   curveDataMeasured.setDrawPointsOn(false);
		   String caseNum = (new Integer(i)).toString();
		   curveDataMeasured.setGraphProperty("Legend", "Measured diff "+caseNum); curveDataMeasured.setGraphColor(IncrementalColors.getColor(i));
		   
		   graphAnalysis.addGraphData(curveDataMeasured);   
	    }	    

    }
    
    /** plot the model data to the graphPanel *
    *  any existing data is retained */
    
    protected void plotModelData() {
	    
	    Vector<Double> phaseCavModelV, phaseDiffBPMModelV, WOutV;
	    BasicGraphData curveDataModel, WOutModel;
	    theDoc.analysisStuff.WOutModelMap.clear();
	    
	    for (int i = 0; i < theDoc.useScanInMatch.size(); i++) {
		   if( theDoc.useScanInMatch.get(i).booleanValue()) {
			   phaseCavModelV =  theDoc.analysisStuff.phasesCavModelScaledV.get(new Integer(i));
			   if(theDoc.analysisStuff.phaseDiffsBPMModelV.get(new Integer(i)) == null) {
				String errText = "Oh no!, The model cavity and BPM phase containers are different sizes\n - I can't plot this";
				theDoc.myWindow().errorText.setText(errText);
				System.err.println(errText);
				return;
			   }
			   if(theDoc.analysisStuff.WOutsV.get(new Integer(i)) == null) {
					String errText = "Oh no!, The model Wout and BPM phase containers are different sizes\n - I can't plot this";
					theDoc.myWindow().errorText.setText(errText);
					System.err.println(errText);
					return;
			   }
			   phaseDiffBPMModelV = theDoc.analysisStuff.phaseDiffsBPMModelV.get(new Integer (i));
			   double [] pCavM = theDoc.analysisStuff.toDouble(phaseCavModelV);
			   double [] pBPMM = theDoc.analysisStuff.toDouble(phaseDiffBPMModelV);
			   curveDataModel = new BasicGraphData();
			   curveDataModel.addPoint(pCavM,pBPMM);
			   curveDataModel.setDrawLinesOn(false);
			   curveDataModel.setDrawPointsOn(true);
			   String caseNum = (new Integer(i)).toString();
			   curveDataModel.setGraphProperty("Legend", "Model diff "+caseNum); curveDataModel.setGraphColor(IncrementalColors.getColor(i));
			   graphAnalysis.addGraphData(curveDataModel);
				   		   
			   WOutV = theDoc.analysisStuff.WOutsV.get(new Integer(i));
			   double [] WOut= theDoc.analysisStuff.toDouble(WOutV);
			   WOutModel = new BasicGraphData();
			   WOutModel.addPoint(pCavM,WOut);
			   WOutModel.setDrawLinesOn(false);
			   WOutModel.setDrawPointsOn(true);
			   WOutModel.setGraphProperty("Legend", "Model diff "+caseNum); WOutModel.setGraphColor(IncrementalColors.getColor(i));
			   if(theDoc.myWindow().plotWOutButton.isSelected()) {			   
				   graphAnalysis.addGraphData(WOutModel);
			   }
			   theDoc.analysisStuff.WOutModelMap.put (new Integer(i), WOutModel);
		    }
	    }
    }
    
}
