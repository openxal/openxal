//
//  WaveformFace.java
//  xal
//
//  Created by S. Cousineau on October 1, 2007
//  Copyright 2006 Oak Ridge National Lab. All rights reserved.
//

package xal.app.injectionpainting;

import xal.ca.*;
import xal.extension.application.*;
import xal.tools.bricks.WindowReference;
import xal.tools.messaging.MessageCenter;

import java.net.URL;
import java.awt.event.*;
import java.awt.Toolkit;
import javax.swing.event.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.Timer;
import java.util.*;
import java.util.Date; 
import java.sql.Connection;
import java.awt.Color;
import java.net.*;
import java.io.*;
import xal.tools.plot.*;
import xal.tools.apputils.files.RecentFileTracker;


/** Controller for the client that monitors the trip monitor services */
public class WaveformFace {
	/** reference to the main window */
	final protected WindowReference windowReference;
	
	/** Kicker ramp time in us */
	private double ramptime = 2000.0;   //Fixed
	
	/** Pre-injection flat top time */
	private double flattime = 1000.0;   //Fixed
	
	/** Duration of the kicker paiting function */
	private double painttime = 1000.0;  //User settable
	
	/** Time duration for the linear fall off of the kickers to zero */
	private double fallofftime = 500.0;//User settable
	
	/** Any extra time needed to fill up the full 5000us waveform */
	private double leftovertime = 0.0;  //5000us minus sum of other times
	
	/** Spacing between points */
	private double deltat = 0.508626;
	
	/** Digitized signal spacing */
	private double dsignal = 0.049;
	
	/** Size of waveform array */
	private int wavesize = 16384;
	
	/** Maximum allowed nonzero waveform time **/
	private int maxtime = 5000;
	
	/** Safety time buffer between max allowed time and endpaint **/
	private int maxbuffertime = 20; 
	
	/** Maximum number of characters allowed in user root name **/
	final static private int MAX_ROOT_NAME_LENGTH = 17;
	
	protected double[] masterhwave = new double[wavesize];
	protected double[] mastervwave = new double[wavesize];
    
    /** indicates whether horizontal waveforms will be submitted when the user loads the latest waveforms */
    private boolean _enableHorizontalWaveformSubmission = true;
    
    /** indicates whether vertical waveforms will be submitted when the user loads the latest waveforms */
    private boolean _enableVerticalWaveformSubmission = true;
    
	
	JTable hTable;
	JTable vTable;
	InputTableModel hinputtablemodel;
	InputTableModel vinputtablemodel;
	FunctionGraphsJPanel hplot;
	FunctionGraphsJPanel vplot;
	JButton hplotButton;
	JButton vplotButton;
	JScrollPane hPane;
	JScrollPane vPane;
	JLabel hwavelabel;
	JLabel vwavelabel;
	JComboBox<String> hwaveformBox;
	JComboBox<String> vwaveformBox;
	String hwaveformtype = new String("Root t");
	String vwaveformtype = new String("Root t");
	JButton hwriteButton; 
	JButton vwriteButton;
	JLabel fileLabel;
    
	private JTextField hRootNameField;
	private JTextField vRootNameField;
	
	String hchildname = new String("");
	String vchildname = new String("");
	String hcurrentfilename = new String("");
	String vcurrentfilename = new String("");
	final private RecentFileTracker WAVEFORM_FOLDER_TRACKER;
	JSplitPane masterPane; 
	
	/** Constructor */
	public WaveformFace( final WindowReference mainWindowReference ) {
        WAVEFORM_FOLDER_TRACKER = new RecentFileTracker( 1, this.getClass(), "wsfile" );  // tracks the location of waveform files

	    windowReference = mainWindowReference;        
	    initializeViews();
	    makeTables();
	    setAction();
	}
	
	
	/** initialize views */
    // Had to suppress warnings getView returns object that cannot be cast. 
    @SuppressWarnings ("unchecked")
	protected void initializeViews() {
	    masterPane = (JSplitPane)windowReference.getView( "masterPane" );
		
	    hplot = (FunctionGraphsJPanel)windowReference.getView( "HWaveformPlot" );
	    vplot = (FunctionGraphsJPanel)windowReference.getView( "VWaveformPlot" ); 
	    hplot.setAxisNames(" t (us)", "Signal (%)");
	    vplot.setAxisNames(" t (us)", "Signal (%)");
	    hTable = (JTable)windowReference.getView( "H Table" );
	    vTable = (JTable)windowReference.getView( "V Table" );
	    hplotButton = (JButton)windowReference.getView( "HPlotButton" );
	    hplotButton.addActionListener( new ActionListener() {
		public void actionPerformed( final ActionEvent event ) {
		    hplot.removeAllGraphData();
		    makeHWave();
		}
	    });	
	    vplotButton = (JButton)windowReference.getView( "VPlotButton" );
	    vplotButton.addActionListener( new ActionListener() {
		public void actionPerformed( final ActionEvent event ) {
		    vplot.removeAllGraphData();
		    makeVWave();
		}
	    });	
	    	    
	    hwavelabel = (JLabel)windowReference.getView( "HWaveLabel" );
        hwaveformBox =  (JComboBox<String>)windowReference.getView("H Combo Box");
	    hwaveformBox.addItem(new String("root t"));
	    hwaveformBox.addItem(new String("flattop"));
	    hwaveformBox.addItem(new String("linear"));
	    vwavelabel = (JLabel)windowReference.getView( "VWaveLabel" );
        vwaveformBox = (JComboBox<String>)windowReference.getView("V Combo Box");
	    vwaveformBox.addItem(new String("root t"));
	    vwaveformBox.addItem(new String("flattop"));
	    vwaveformBox.addItem(new String("linear"));
	    hPane = (JScrollPane)windowReference.getView( "H Scroll Pane" );
	    vPane = (JScrollPane)windowReference.getView( "V Scroll Pane" );
	    hwriteButton = (JButton)windowReference.getView( "HWriteButton" );
	    vwriteButton = (JButton)windowReference.getView( "VWriteButton" );
        
		vRootNameField = (JTextField)windowReference.getView( "vrootname" );
        vRootNameField.setDocument( new RootNameDocument( MAX_ROOT_NAME_LENGTH ) );
        vRootNameField.setColumns( MAX_ROOT_NAME_LENGTH );
        vRootNameField.setMaximumSize( vRootNameField.getPreferredSize() );
        
		hRootNameField = (JTextField)windowReference.getView( "hrootname" );
        hRootNameField.setDocument( new RootNameDocument( MAX_ROOT_NAME_LENGTH ) );
        hRootNameField.setColumns( MAX_ROOT_NAME_LENGTH );
        hRootNameField.setMaximumSize( hRootNameField.getPreferredSize() );
        
        
        final JButton submitLatestWaveformsButton = (JButton)windowReference.getView( "SubmitLatestWaveformsButton" );
        submitLatestWaveformsButton.addActionListener(new ActionListener(){ 
            public void actionPerformed(ActionEvent e) {
                final File waveformDirectory = getWaveformDirectory( true );
                if ( waveformDirectory != null ) {
                    if ( canSubmitLatestHorizontalWaveforms() )  submitHWaveforms();
                    if ( canSubmitLatestVerticalWaveforms() )  submitVWaveforms();
                }
                else {
                    System.out.println( "Warning: Waveform loading canceled. Waveform directory must be specified." );
                }
            }
        });
        
        
        final JCheckBox horizontalSubmitCheckbox = (JCheckBox)windowReference.getView( "HorizontalSubmitCheckbox" );
        horizontalSubmitCheckbox.setSelected( _enableHorizontalWaveformSubmission );
        horizontalSubmitCheckbox.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                _enableHorizontalWaveformSubmission = horizontalSubmitCheckbox.isSelected();
            }
        });
        
        final JCheckBox verticalSubmitCheckbox = (JCheckBox)windowReference.getView( "VerticalSubmitCheckbox" );
        verticalSubmitCheckbox.setSelected( _enableVerticalWaveformSubmission );
        verticalSubmitCheckbox.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                _enableVerticalWaveformSubmission = verticalSubmitCheckbox.isSelected();
            }
        });
                
        final JButton submitSelectedWaveformsButton = (JButton)windowReference.getView( "SubmitSelectedWaveformsButton" );
        submitSelectedWaveformsButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                final JFileChooser chooser = new JFileChooser( getWaveformDirectory() );
                int returnStatus = chooser.showOpenDialog( windowReference.getWindow() );
                if( returnStatus == JFileChooser.APPROVE_OPTION ){
                    final File selection = chooser.getSelectedFile(); 
                    final String fileName = selection.getName();
                    
                    // find all related files with a common root delimited by an underscore
                    final String[] tokens = fileName.split( "_" );
                    System.out.println("tokens are " + tokens[0] + "\t" + tokens[1]);
                    
                    if ( tokens[1].contains( "H" ) ){
                        hcurrentfilename = tokens[0];       // common root for horizontal waveforms
                        submitHWaveforms();
                    }
                    else if ( tokens[1].contains( "V" ) ){
                        vcurrentfilename = tokens[0];       // common root for vertical waveforms
                        submitVWaveforms();
                    }
                }
                else{
                    System.out.println("Open command canceled by user.");
                }
            }
        });

	    
	    fileLabel = (JLabel)windowReference.getView( "fileLabel" );
	            
        final JButton waveformDirectorySelectionButton = (JButton)windowReference.getView( "WaveformDirectorySelectionButton" );
        waveformDirectorySelectionButton.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                final int responseStatus = JOptionPane.showConfirmDialog( windowReference.getWindow(), "For EXPERTS ONLY to select waveform directory. Continue?", "Select Waveform Directory", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE ); 
                switch( responseStatus ) {
                    case JOptionPane.OK_OPTION:
                        requestWaveformDirectory();
                        break;
                    default:
                        break;
                }
            }
        });
        
        final JCheckBox expertModeCheckbox = (JCheckBox)windowReference.getView( "ExpertModeCheckbox" );
        expertModeCheckbox.addActionListener( new ActionListener() {
            public void actionPerformed( final ActionEvent event ) {
                waveformDirectorySelectionButton.setEnabled( expertModeCheckbox.isSelected() );
            }
        });
        
        updateWaveformDirectoryLabel();
    }
    
    
    /** determine whether the latest horizontal waveforms can be submitted */
    public boolean canSubmitLatestHorizontalWaveforms() {
        return _enableHorizontalWaveformSubmission && ( hcurrentfilename != null && hcurrentfilename.length() > 0 );
    }
    
    
    /** determine whether the latest vertical waveforms can be submitted */
    public boolean canSubmitLatestVerticalWaveforms() {
        return _enableVerticalWaveformSubmission && ( vcurrentfilename != null && vcurrentfilename.length() > 0 );
    }
    
    
    /** determine whether any of the latest waveforms can be submitted */
    public boolean canSubmitAnyLatestWaveforms() {
        return canSubmitLatestHorizontalWaveforms() || canSubmitLatestVerticalWaveforms();
    }
    
    
    public void updateWaveformDirectoryLabel() {
        final JLabel waveformDirectoryLabel = (JLabel)windowReference.getView( "WaveformDirectoryLabel" );
        final File waveformDirectory = WAVEFORM_FOLDER_TRACKER.getMostRecentFile();
        if ( waveformDirectory != null ) {
            waveformDirectoryLabel.setText( waveformDirectory.getAbsolutePath() );
        }
        else {
            waveformDirectoryLabel.setText( "" );
        }
    }
    

	public void setAction(){
	
	hwaveformBox.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		if(hwaveformBox.getSelectedIndex() == 0){
		   hwaveformtype = "Root t";
		}
		if(hwaveformBox.getSelectedIndex() == 1){
		   hwaveformtype = "Flat t"; 
		}
		if(hwaveformBox.getSelectedIndex() == 2){
		   hwaveformtype = "Linear t"; 
		}
	    }
	});  
	
	vwaveformBox.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		if(vwaveformBox.getSelectedIndex() == 0){
		   vwaveformtype = "Root t";
		}
		if(vwaveformBox.getSelectedIndex() == 1){
		   vwaveformtype = "Flat t"; 
		}
		if(vwaveformBox.getSelectedIndex() == 2){
		   vwaveformtype = "Linear t"; 
		}
	    }
	});  
	
	hwriteButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		generateHBinary(masterhwave);
	    }
	});  
	vwriteButton.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		generateVBinary(mastervwave);
	    }
	});
	
	}
    
    
    /** Get the directory where the waveforms reside (requesting it from the user if necessary) */
    private File getWaveformDirectory() {
        return getWaveformDirectory( false );
    }
    
    
    /** Get the directory where the waveforms reside (requesting it from the user if necessary) */
    private File getWaveformDirectory( final boolean requestIfMissing ) {
        final File defaultDirectory = WAVEFORM_FOLDER_TRACKER.getMostRecentFile();
        if ( defaultDirectory == null && requestIfMissing ) {       // if none set then request it from the user
            final File selectedDirectory = requestWaveformDirectory();
            return selectedDirectory;
        }
        else {
            return defaultDirectory;
        }
    }
    
    
    /** Request the waveform directory from the user */
    private File requestWaveformDirectory() {
        final JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle( "Select Directory to Waveforms" );
        chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
        final File mostRecentFile = WAVEFORM_FOLDER_TRACKER.getMostRecentFile();
        if ( mostRecentFile != null ) {
            final File defaultFolder = ( mostRecentFile.exists() && mostRecentFile.isDirectory() ) ? mostRecentFile : mostRecentFile.getParentFile();   // make sure we have a directory
            chooser.setCurrentDirectory( defaultFolder );
        }
        final int responseStatus = chooser.showOpenDialog( windowReference.getWindow() );
        switch( responseStatus ) {
            case JFileChooser.APPROVE_OPTION:
                final File selection = chooser.getSelectedFile();
                WAVEFORM_FOLDER_TRACKER.cacheURL( selection );
                updateWaveformDirectoryLabel();
                return selection;
            default:
                return null;
        }
    }
    
	
	/** make the H plot */
	protected void makeHPlot(double[] sdata, double[] data ) {
	    final BasicGraphData graphData = new BasicGraphData();
	    graphData.setGraphColor( Color.BLUE );
	    graphData.addPoint(sdata, data);
	    hplot.addGraphData(graphData );	

		//Create a default name for the file
		double painttime = Double.valueOf(((String)hTable.getValueAt(2,1)).trim()).doubleValue();
		double startamp = Double.valueOf(((String)hTable.getValueAt(0,1)).trim()).doubleValue();
		double endamp = Double.valueOf(((String)hTable.getValueAt(1,1)).trim()).doubleValue();
		Integer paintint = new Integer((new Double(painttime)).intValue());
		Integer startampint = new Integer((new Double(startamp)).intValue());
		Integer endampint = new Integer((new Double(endamp)).intValue());
		String paintstring = paintint.toString();
		String startampstring = startampint.toString();
		String endampstring = endampint.toString();
		int paintform = hwaveformBox.getSelectedIndex();
		String paintformstring = new String("");
		if(paintform == 0) paintformstring = "root";
		if(paintform == 1) paintformstring = "flat";
		if(paintform == 2) paintformstring = "lin";
		hchildname = new String(paintformstring + paintstring + "us-" + startampstring + "t" + endampstring);
        if ( hchildname.length() > MAX_ROOT_NAME_LENGTH )  hchildname = hchildname.substring( 0, MAX_ROOT_NAME_LENGTH );    // trim the name to the allowed length
		System.out.println("hchildname is " + hchildname);
		hRootNameField.setText(hchildname);
		
	}	
	/** make the V plot */
	protected void makeVPlot(double[] sdata, double[] data ) {
	    final BasicGraphData graphData = new BasicGraphData();
	    graphData.setGraphColor( Color.BLUE );
	    graphData.addPoint(sdata, data);
	    vplot.addGraphData(graphData );	
		//Create a default name for the file
		double painttime = Double.valueOf(((String)hTable.getValueAt(2,1)).trim()).doubleValue();
		double startamp = Double.valueOf(((String)hTable.getValueAt(0,1)).trim()).doubleValue();
		double endamp = Double.valueOf(((String)hTable.getValueAt(1,1)).trim()).doubleValue();
		Integer paintint = new Integer((new Double(painttime)).intValue());
		Integer startampint = new Integer((new Double(startamp)).intValue());
		Integer endampint = new Integer((new Double(endamp)).intValue());
		String paintstring = paintint.toString();
		String startampstring = startampint.toString();
		String endampstring = endampint.toString();
		int paintform = vwaveformBox.getSelectedIndex();
		String paintformstring = new String("");
		if(paintform == 0) paintformstring = "root";
		if(paintform == 1) paintformstring = "flat";
		if(paintform == 2) paintformstring = "lin";
		vchildname = new String(paintformstring + paintstring + "us-" + startampstring + "t" + endampstring);
        if ( vchildname.length() > MAX_ROOT_NAME_LENGTH )  vchildname = vchildname.substring( 0, MAX_ROOT_NAME_LENGTH );    // trim the name to the allowed length
		System.out.println("vchildname is " + vchildname);
		vRootNameField.setText(vchildname);
		
	}	
	
	
	/** make the Table */
	protected void makeTables( ) {
	    String[] colnames = {"Parameter", "Value"};
	    int nrows = 2;
	    hinputtablemodel = new InputTableModel(colnames, 4);
	    hTable.setModel(hinputtablemodel);
	    hTable.getColumnModel().getColumn(0).setMinWidth(150);
	    hTable.getColumnModel().getColumn(1).setMinWidth(100);
	    hinputtablemodel.setValueAt(new String("Start Paint Amp (%)"), 0, 0);
	    hinputtablemodel.setValueAt(new String("End Paint Amp (%)"), 1, 0);
	    hinputtablemodel.setValueAt(new String("Paint Time (us)"), 2, 0);
	    hinputtablemodel.setValueAt(new String("Fall-Off Time (us)"), 3, 0);
	    hinputtablemodel.setValueAt(new String("100.0"), 0, 1);
	    hinputtablemodel.setValueAt(new String("50.0"), 1, 1);
	    hinputtablemodel.setValueAt(new String("1000.0"), 2, 1);
	    hinputtablemodel.setValueAt(new String("500.0"), 3, 1);
	    hinputtablemodel.fireTableDataChanged();  
	    
	    vinputtablemodel = new InputTableModel(colnames, 4);
	    vTable.setModel(vinputtablemodel);
	    vTable.getColumnModel().getColumn(0).setMinWidth(150);
	    vTable.getColumnModel().getColumn(1).setMinWidth(100);
	    vinputtablemodel.setValueAt(new String("Start Paint Amp (%)"), 0, 0);
	    vinputtablemodel.setValueAt(new String("End Paint Amp (%)"), 1, 0);
	    vinputtablemodel.setValueAt(new String("Paint Time (us)"), 2, 0);
	    vinputtablemodel.setValueAt(new String("Fall-Off Time (us)"), 3, 0);
	    vinputtablemodel.setValueAt(new String("100.0"), 0, 1);
	    vinputtablemodel.setValueAt(new String("50.0"), 1, 1);
	    vinputtablemodel.setValueAt(new String("1000.0"), 2, 1);
	    vinputtablemodel.setValueAt(new String("500.0"), 3, 1);
	    vinputtablemodel.fireTableDataChanged();  
	    
	    
	}
	
	
	/** make the H Waveform */
	protected void makeHWave( ) {
	    double[] swave = new double[wavesize];
	    double[] wave  = new double[wavesize];
	    double ttime = 0.0;
	    double startamp = Double.valueOf(((String)hTable.getValueAt(0,1)).trim()).doubleValue();
	    double endamp = Double.valueOf(((String)hTable.getValueAt(1,1)).trim()).doubleValue();
	    double amp = startamp - endamp;
	    double painttime = Double.valueOf(((String)hTable.getValueAt(2,1)).trim()).doubleValue();
	    double falltime = Double.valueOf(((String)hTable.getValueAt(3,1)).trim()).doubleValue();
	    double transitiontime = 0.0;
	    boolean slewflag = false;
	  
	    
	    if(startamp < endamp){
	      boolean transitionneeded = true;
	      transitiontime = 50.0*deltat;
	      falltime -= transitiontime;
	    }
	    double toffset1 = ramptime + flattime;
	    double toffset2 = ramptime + flattime + painttime;
	    double toffset3 = ramptime + flattime + painttime + transitiontime;
	    double toffset4 = ramptime + flattime + painttime + transitiontime + falltime;
	    double riseslope = startamp/ramptime;
	    double fallslope = -endamp/falltime;
	    double totaltime = ramptime + flattime + painttime + transitiontime + falltime;
	    fileLabel.setText("");
	    if( totaltime > (maxtime - maxbuffertime)){
		fileLabel.setText("I'm sorry, I can not create this waveform because it is too long. Please reduce fall-off time.");
	    }
	    else{
	    for(int i=0; i<wavesize; i++){
		swave[i] = deltat*i;
		if(swave[i] < ramptime){
		    wave[i]=riseslope*swave[i];
		}
		else if(swave[i] > ramptime && swave[i] < (toffset1)){
		    wave[i] = startamp;
		}
		else if(swave[i] > toffset1 && swave[i] < toffset2){
		    if(hwaveformtype.equals("Flat t")){
			wave[i]=startamp;
		    }
		    if(hwaveformtype.equals("Root t")){
			wave[i]=amp*(1-Math.sqrt((swave[i] - toffset1)/painttime)) + endamp;
		    }
		    if(hwaveformtype.equals("Linear t")){
			wave[i]=amp*(1-(swave[i] - toffset1)/painttime) + endamp;    
		    }
		}
		else if(swave[i] > toffset2 && swave[i] < toffset3){
		   	wave[i]=wave[i-1];
		}
		else if(swave[i] > toffset3 && swave[i] <= toffset4){
		     if(hwaveformtype.equals("Flat t")){
                        wave[i]=(-startamp/falltime)*(swave[i] - toffset3) + startamp;
		     }
			wave[i]=fallslope*(swave[i] - toffset3) + endamp;
		}
		else{
		    wave[i]=0.0;
		}
		//Do a kicker slew rate check
		if(!slewflag && i>0){
		    if(Math.abs(wave[i]-wave[i-1]) > 0.203){
			//System.out.println("First slew violation is i " + i + "  " + wave[i] +  "   " + wave[i-1]);
			slewflag = true;
		    }
		}
		
	    }
	    if(slewflag){
		//System.out.println("Warning: You may be in violation of the maximum slew rate.");
		//fileLabel.setText("Warning: This waveform may be in violation of the maximum slew rate.");
	    }
	    else{
		fileLabel.setText("");
	    }
	    masterhwave = wave;
	    makeHPlot(swave, wave);
	    }
	}	

	/** make the H Waveform */
	protected void makeVWave( ) {
	    double[] swave = new double[wavesize];
	    double[] wave  = new double[wavesize];
	    double ttime = 0.0;
	    double startamp = Double.valueOf(((String)vTable.getValueAt(0,1)).trim()).doubleValue();
	    double endamp = Double.valueOf(((String)vTable.getValueAt(1,1)).trim()).doubleValue();
	    double amp = startamp - endamp;
	    double painttime = Double.valueOf(((String)vTable.getValueAt(2,1)).trim()).doubleValue();
	    double falltime = Double.valueOf(((String)vTable.getValueAt(3,1)).trim()).doubleValue();
	    double transitiontime = 0.0;
	    boolean slewflag = false;
	  
	    if(startamp < endamp){
	      boolean transitionneeded = true;
	      transitiontime = 50.0*deltat;
	      falltime -= transitiontime;
	      System.out.println("A transition time is necessary before fall-off.");
	    }
	    double toffset1 = ramptime + flattime;
	    double toffset2 = ramptime + flattime + painttime;
	    double toffset3 = ramptime + flattime + painttime + transitiontime;
	    double toffset4 = ramptime + flattime + painttime + transitiontime + falltime;
	    double riseslope = startamp/ramptime;
	    double fallslope = -endamp/falltime;    
	    double totaltime = ramptime + flattime + painttime + transitiontime + falltime;
	    fileLabel.setText("");
	    if( totaltime > (maxtime - maxbuffertime)){
		fileLabel.setText("I'm sorry, I can not create this waveform because it is too long. Please reduce fall-off time.");
	    }
	    else{
	    for(int i=0; i<wavesize; i++){
		swave[i] = deltat*i;
		if(swave[i] < ramptime){
		    wave[i]=riseslope*swave[i];
		}
		else if(swave[i] > ramptime && swave[i] < (toffset1)){
		    wave[i] = startamp;
		}
		else if(swave[i] > toffset1 && swave[i] < toffset2){
		    if(vwaveformtype.equals("Flat t")){
			wave[i]=startamp;
		    }
		    if(vwaveformtype.equals("Root t")){
			wave[i]=amp*(1-Math.sqrt((swave[i] - toffset1)/painttime)) + endamp;
		    }
		    if(vwaveformtype.equals("Linear t")){
			wave[i]=amp*(1-(swave[i] - toffset1)/painttime) + endamp;
		    }
		}
		else if(swave[i] > toffset2 && swave[i] < toffset3){
		   	wave[i]=wave[i-1];
		}
		else if(swave[i] > toffset3 && swave[i] < toffset4){	
			wave[i]=fallslope*(swave[i] - toffset3) + endamp;
		}
		else{
		    wave[i]=0.0;
		}
		if(!slewflag && i>0){
		    if(Math.abs(wave[i]-wave[i-1]) > 0.203){
			//System.out.println("First slew violation is i " + i + "  " + wave[i] +  "   " + wave[i-1]);
			slewflag = true;
		    }
		}
	    }
	    if(slewflag){
		//System.out.println("Warning: You may be in violation of the maximum slew rate.");
		//fileLabel.setText("Warning: This waveform may be in violation of the maximum slew rate.");
	    }
	    else{
		fileLabel.setText("");
	    }
	    mastervwave = wave;
	    makeVPlot(swave, wave);
	    }
	}	


	protected void generateHBinary(double[] wave){
	    
	    int length = wave.length;
	    char[] h1wavechars = new char[length];
	    char[] h2wavechars = new char[length];
	    char[] h3wavechars = new char[length];
	    char[] h4wavechars = new char[length];
	    double scaledwave;
	    char swappedchar;
	    double h1ratio=1.0;
	    double h2ratio=0.588;
	    double h3ratio=0.588;
	    double h4ratio=1.0;
	    
	    for(int i=0; i<length; i++){
		scaledwave = (h1ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		h1wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (h2ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		h2wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (h3ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		h3wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (h4ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		h4wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
	    }   
	    writeHFiles(h1wavechars, h2wavechars, h3wavechars, h4wavechars);
	}
	
	protected void generateVBinary(double[] wave){
	    
	    int length = wave.length;
	    char[] v1wavechars = new char[length];
	    char[] v2wavechars = new char[length];
	    char[] v3wavechars = new char[length];
	    char[] v4wavechars = new char[length];
	    double scaledwave;
	    char swappedchar;
	    double v1ratio=0.744;
	    double v2ratio=1.0;
	    double v3ratio=1.0;
	    double v4ratio=0.744;
	    
	    for(int i=0; i<length; i++){
		scaledwave = (v1ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		v1wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (v2ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		v2wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (v3ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		v3wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
		scaledwave = (v4ratio*wave[i]/100.0 + 1)/2.0 * 4095.0;
		swappedchar  = (char)scaledwave;
		v4wavechars[i] = (char)((char)(swappedchar >> 8) + (char)(swappedchar << 8));
	    }   
	    writeVFiles(v1wavechars, v2wavechars, v3wavechars, v4wavechars);
	}
	
	
	
	protected void writeHFiles(char[] h1wave, char[] h2wave, char[] h3wave, char[] h4wave){
	    
        try{
            
            final File file = getWaveformDirectory( true );
            System.out.println( "Writing files to: " + file );
            if( file != null ){
                String parentpath = file.getPath();
                //String childname = file.getName();
                String childname = hRootNameField.getText();
                
                final String errorMessage = validateBaseFileName( childname );
                if( errorMessage == null ){
                    String h1filename = childname + "_H1.w16";
                    String h2filename = childname + "_H2.w16";
                    String h3filename = childname + "_H3.w16";
                    String h4filename = childname + "_H4.w16";
                    //Write waveforms for first each H kicker
                    File h1file = new File(parentpath, h1filename);
                    FileOutputStream h1out = new FileOutputStream(h1file); 
                    DataOutputStream h1data = new DataOutputStream(h1out);
                    for(int j=0; j<h1wave.length; j++){
                        h1data.writeChar(h1wave[j]);
                    }
                    File h2file = new File(parentpath, h2filename);
                    FileOutputStream h2out = new FileOutputStream(h2file); 
                    DataOutputStream h2data = new DataOutputStream(h2out);
                    for(int j=0; j<h2wave.length; j++){
                        h2data.writeChar(h2wave[j]);
                    }
                    File h3file = new File(parentpath, h3filename);
                    FileOutputStream h3out = new FileOutputStream(h3file); 
                    DataOutputStream h3data = new DataOutputStream(h3out);
                    for(int j=0; j<h3wave.length; j++){
                        h3data.writeChar(h3wave[j]);
                    }
                    File h4file = new File(parentpath, h4filename);
                    FileOutputStream h4out = new FileOutputStream(h4file); 
                    DataOutputStream h4data = new DataOutputStream(h4out);
                    for(int j=0; j<h4wave.length; j++){
                        h4data.writeChar(h4wave[j]);
                    } 
                    h1data.flush();
                    h2data.flush();
                    h3data.flush();
                    h4data.flush();
                    h1out.close();
                    h2out.close();
                    h3out.close();
                    h4out.close();
                    
                    hcurrentfilename = childname;
                    fileLabel.setText("Wrote files with rootnames " + h1filename + ", " + h2filename + ", " + h3filename + ", " + h4filename);
                }
                else{
                    Toolkit.getDefaultToolkit().beep();
                    fileLabel.setText("Error: " + errorMessage + "  Please try again.");
                }
            }
            else{
                fileLabel.setText( "Warning: No directory specified for writing files..." );
            }
        }
        catch(IOException ioe){
        }
        
	}
    
    
    /** 
     * Validate the base file name
     * @return the error message if any otherwise null if the name is valid
     */
    private String validateBaseFileName( final String baseFileName ) {
        if ( baseFileName == null || baseFileName.length() == 0 )  return "Root name cannot be empty.";
        else if ( baseFileName.length() > MAX_ROOT_NAME_LENGTH )  return "Root name exceeds " + MAX_ROOT_NAME_LENGTH + " character limit.";
        else if ( baseFileName.contains( "_" ) )  return "Root name contains illegal underscore character.";
        else return null;
    }
	    
	 
	protected void writeVFiles(char[] v1wave, char[] v2wave, char[] v3wave, char[] v4wave){
	    
        try{            
            final File file = getWaveformDirectory( true );
            if( file != null ){                
                String parentpath = file.getPath();
                //String childname = file.getName();
                String childname = vRootNameField.getText();
                
                final String errorMessage = validateBaseFileName( childname );
                if( errorMessage == null ){
                    String v1filename = childname + "_V1.w16";
                    String v2filename = childname + "_V2.w16";
                    String v3filename = childname + "_V3.w16";
                    String v4filename = childname + "_V4.w16";
                    //Write waveforms for first each V kicker
                    File v1file = new File(parentpath, v1filename);
                    FileOutputStream v1out = new FileOutputStream(v1file); 
                    DataOutputStream v1data = new DataOutputStream(v1out);
                    for(int j=0; j<v1wave.length; j++){
                        v1data.writeChar(v1wave[j]);
                    }
                    File v2file = new File(parentpath, v2filename);
                    FileOutputStream v2out = new FileOutputStream(v2file); 
                    DataOutputStream v2data = new DataOutputStream(v2out);
                    for(int j=0; j<v2wave.length; j++){
                        v2data.writeChar(v2wave[j]);
                    }
                    File v3file = new File(parentpath, v3filename);
                    FileOutputStream v3out = new FileOutputStream(v3file); 
                    DataOutputStream v3data = new DataOutputStream(v3out);
                    for(int j=0; j<v3wave.length; j++){
                        v3data.writeChar(v3wave[j]);
                    }
                    File v4file = new File(parentpath, v4filename);
                    FileOutputStream v4out = new FileOutputStream(v4file); 
                    DataOutputStream v4data = new DataOutputStream(v4out);
                    for(int j=0; j<v4wave.length; j++){
                        v4data.writeChar(v4wave[j]);
                    } 
                    v1data.flush();
                    v2data.flush();
                    v3data.flush();
                    v4data.flush();
                    v1out.close();
                    v2out.close();
                    v3out.close();
                    v4out.close();
                    vcurrentfilename = childname; 
                    fileLabel.setText("Wrote files with rootnames " + v1filename + ", " + v2filename + ", " + v3filename + ", " + v4filename);
                }
                else{
                    Toolkit.getDefaultToolkit().beep();
                    fileLabel.setText("Error: " + errorMessage + "  Please try again.");
                }
            }
            else{
                fileLabel.setText( "Warning: No directory specified for writing files..." );
            }
        }
        catch(IOException ioe){
        }
        
	}
	
	/** Routine to submit the last saved H waveform */
	protected void submitHWaveforms(){
        System.out.println( "Submitting horizontal waveforms with root: " + hcurrentfilename );
	    
	    String h1name = new String(hcurrentfilename + "_H1.w16");
	    String h2name = new String(hcurrentfilename + "_H2.w16");
	    String h3name = new String(hcurrentfilename + "_H3.w16");
	    String h4name = new String(hcurrentfilename + "_H4.w16");
	    
	    if(hcurrentfilename.equals("")){
		System.out.println("There is no waveform file saved.");
	    }
	    else{
		Channel h1ch;
		Channel h2ch;
		Channel h3ch;
		Channel h4ch;
		h1ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickH01:7121:FGWAVE");
		h2ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickH02:7121:FGWAVE");
		h3ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickH03:7121:FGWAVE");
		h4ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickH04:7121:FGWAVE");
		h1ch.connectAndWait();
		h2ch.connectAndWait();
		h3ch.connectAndWait();
		h4ch.connectAndWait();
		try {
		    h1ch.putVal(h1name);
		    h2ch.putVal(h2name);
		    h3ch.putVal(h3name);
		    h4ch.putVal(h4name);
		    Channel.flushIO();
		    System.out.println("Loaded filenames: " + h1name + "\t" + h2name + "\t" + h3name + "\t" + h4name);
			fileLabel.setText("Loaded filenames: " + h1name + "\t" + h2name + "\t" + h3name + "\t" + h4name);
		}
		catch (ConnectionException e){
		    System.err.println("Unable to connect to channel access.");
			fileLabel.setText("Unable to connect to channel access.");
		}
		catch (PutException e){
		    System.err.println("Unable to set process variables.");
			fileLabel.setText("Unable to set process variables.");

		}
	    }
	}
	    
	/** Routine to submit the last saved V waveform */
	protected void submitVWaveforms(){
        System.out.println( "Submitting vertical waveforms with root: " + vcurrentfilename );
	    
	    String v1name = new String(vcurrentfilename + "_V1.w16");
	    String v2name = new String(vcurrentfilename + "_V2.w16");
	    String v3name = new String(vcurrentfilename + "_V3.w16");
	    String v4name = new String(vcurrentfilename + "_V4.w16");
	    
	    if(vcurrentfilename.equals("")){
		System.out.println("There is no waveform file saved.");
	    }
	    else{
		Channel v1ch;
		Channel v2ch;
		Channel v3ch;
		Channel v4ch;
		v1ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickV01:7121:FGWAVE");
		v2ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickV02:7121:FGWAVE");
		v3ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickV03:7121:FGWAVE");
		v4ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKickV04:7121:FGWAVE");
		v1ch.connectAndWait();
		v2ch.connectAndWait();
		v3ch.connectAndWait();
		v4ch.connectAndWait();
		try {
		    v1ch.putVal(v1name);
		    v2ch.putVal(v2name);
		    v3ch.putVal(v3name);
		    v4ch.putVal(v4name);
		    Channel.flushIO();
		    System.out.println("Loaded filenames: " + v1name + "\t" + v2name + "\t" + v3name + "\t" + v4name);
			fileLabel.setText("Loaded filenames: " + v1name + "\t" + v2name + "\t" + v3name + "\t" + v4name);
		}
		catch (ConnectionException e){
		    System.err.println("Unable to connect to channel access.");
			fileLabel.setText("Unable to connect to channel access.");

		}
		catch (PutException e){
		    System.err.println("Unable to set process variables.");
			fileLabel.setText("Unable to set process variables.");
		}
	    }
	}
	
	
	
	/** get the main window */
	protected DefaultXalWindow getMainWindow() {
		return (DefaultXalWindow)windowReference.getWindow();
	}
	
}



/** document for managing the content of the root name text fields */
class RootNameDocument extends PlainDocument {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    /** maximum length of text in the text field */
    final private int MAX_TEXT_LENGTH;
    
    
    /** Constructor */
    public RootNameDocument( final int maxTextLength ) {
        MAX_TEXT_LENGTH = maxTextLength;
    }
    
    
    /** Override the insert method to check for invalid characters (i.e. underscore: "_") and verify text length is within maximum allowed */
    public void insertString( final int offset, final String newText, final AttributeSet attributes ) throws BadLocationException {
        final int proposedLength = newText.length() + this.getLength();
        if ( newText.contains( "_" ) || proposedLength > MAX_TEXT_LENGTH ) {
            Toolkit.getDefaultToolkit().beep();
        }
        else {
            super.insertString( offset, newText, attributes );
        }
    }
}

