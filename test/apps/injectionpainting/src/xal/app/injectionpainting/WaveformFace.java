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
import xal.extension.bricks.WindowReference;
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
import xal.extension.widgets.plot.*;
import xal.tools.apputils.files.RecentFileTracker;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

enum Plane {  
	HOR,VER; 
}

class KickerWaveform {

	private static String[] supportedWaves = {"root","flat","lin","true","power"};
	private static String[] displayNames   = {"Root t", "Flattop","Linear", "True Root","Power"};
	private String funType      = "";    // Settable.
	private Plane plane         = Plane.HOR;
	//Amplitude variables
	private double startamp     = 100.0;
	private double endamp       = 48.0;
	private double clOrbAmp     = 100.0; // Only applies to True Root.
	private double paintPower   = 0.333; 

	// Timing variables all are in microseconds. 
	// hold time should be such that the painting starts at the same time this means that 
	// holdtime = 3000 - ramptime - flattime
	// Changing this would require changing kicker timing, which we could do, but should only be done if there's 
	// a good reason
	private double holdtime     = 1520.0;  // 3000 - ramptime - flattime; Need to fix when ramp/flat are settable. 
	private double ramptime     = 1000.0; // Settable - but not yet in app. #original was 2ms, now 1ms for kicker upgrade
	private double flattime     = 480.0;  // Settable - but not yet in app. 
	private double painttime    = 1000.0; // Settable
	private double fallofftime  = 300.0;  // Settable
	private double leftovertime = 0.0; // calcuated from others
	
	//Default values for UTCA
	private double deltat = 0.4; // Used to scale timing, dt between samples	
	private double dsignal = 0.049;   //Appears to be unused now.
	private static int wavesize = 12500;

	//Old Yokogawa values if needed.
	// private double deltat  = 0.5086826; // Used to scale timing, dt between samples
	// // dsignal was probably used for scaling it is 100/(2^12/2) where 2^12/2 is the max signal since yokogawa uses signed
	// // 12 bit, and we are restricted to + values. 
	// private double dsignal = 0.049;   //Appears to be unused now.
	// private static int wavesize = 16384;

	// With upgraded kickers (max = 1600A) maxtime should be 3ms, was 5ms previously. 
	private static int maxtime    = 3000; // Maximum allowed NON-ZERO waveform time. Determined by power supply and magnet. 
	private int maxbuffertime = 20;

	final static private int MAX_ROOT_NAME_LENGTH = 17; // This appears to be from the PV (.w16 will be added to name)
	final static private double MAX_SLEW = 0.203; // Don't know where this comes from. Presumably it includes the time step used here and needs to be corrected for the new 0.4us deltat - NJE 2021-12-09

	protected double[] waveform_t = new double[wavesize];
	protected double[] waveform_x = new double[wavesize];

	protected short[] kicker1_x = new short[wavesize];
	protected short[] kicker2_x = new short[wavesize];
	protected short[] kicker3_x = new short[wavesize];
	protected short[] kicker4_x = new short[wavesize];

	protected double[] kickerRatios = {1.0,1.0,1.0,1.0};

	KickerWaveform(Plane p){
		funType = supportedWaves[0];
		plane = p;
		//These ratios were determined experimentally by 
	    // T. Pelaia, I think. Will try to find ref. - NJE
	    // These are no longer needed in the waveforms with the new UTCA setup.
	    // Kicker scaling will be provided by voltage control. 
	    switch(plane){
	    	case HOR:
	    		kickerRatios[0] = 1.0;
	    		kickerRatios[1] = 0.588;
	    		kickerRatios[2] = 0.588;
	    		kickerRatios[3] = 1.0;
	    		break;
	    	case VER:
	    		kickerRatios[0] = 0.744;
				kickerRatios[1] = 1.0;
				kickerRatios[2] = 1.0;
				kickerRatios[3] = 0.744;
	    		break;
	    }

	}

	public void setStartAmp(double a){ startamp = a;}
	public void setEndAmp(double a){ endamp = a;}
	public void setClosedOrbitAmp(double a){ clOrbAmp = a;}

	//Changing flattime or ramptime requires re-calculating holdtime
	public void setFlatTime(double a){ flattime = a; setHoldTime(3000.0 - getFlatTime() - getRampTime()); }
	public void setRampTime(double a){ ramptime = a; setHoldTime(3000.0 - getFlatTime() - getRampTime()); }

	public void setPaintTime(double a){ painttime = a; }
	public void setFallTime(double a){ fallofftime = a;}
	public void setHoldTime(double h){ holdtime = h;}

	public void setFunType(String a){ funType = a;}
	public void setPowExp(double a){ paintPower = a;}


	public static int getMaxTime(){return maxtime;};
	public static int getWavesize(){return wavesize;};
	public double getDeltaT(){return deltat;};
	public double getWavetime(){return (deltat*wavesize);};

	public double getStartAmp()      { return startamp    ;}
	public double getEndAmp()        { return endamp      ;}
	public double getClosedOrbitAmp(){ return clOrbAmp    ;}

	public double getHoldTime(){return holdtime;}
	public double getRampTime(){return ramptime;}
	public double getFlatTime(){return flattime;}

	public double getPaintTime()     { return painttime   ;}
	public double getFallTime()      { return fallofftime ;}
	public String getFunType()       { return funType     ;}

	public String getPlaneIDStr(){ return (plane == Plane.HOR) ? "H" : "V";}

	public static String[] getSupportedWaves(){
		return supportedWaves;
	}

	public static String[] getDisplayNames(){
		return displayNames;
	}

	protected String generateWaveformBasename(){

	    //Create a default name for the file
		Integer paintint    = new Integer((new Double(painttime)).intValue());
		Integer startampint = new Integer((new Double(startamp)).intValue());
		Integer endampint   = new Integer((new Double(endamp)).intValue());

		String paintstring    = paintint.toString();
		String startampstring = startampint.toString();
		String endampstring   = endampint.toString();


		String paintformstring = funType; //new String("");

		String fileString = new String(paintformstring + paintstring + "us-" + startampstring + "t" + endampstring );

        if ( fileString.length() > MAX_ROOT_NAME_LENGTH )  fileString = fileString.substring( 0, MAX_ROOT_NAME_LENGTH );    // trim the name to the allowed length

        return fileString;
    }

    public double[] getTimeSteps(){
    	return waveform_t;
    }

    public double[] getWaveform(){
    	return waveform_x;
    }

    private double paintFunction(double time){

    	// Separated from main waveform painting.
    	// Time should be (0,painttime) in this function.
    	// makes it a little easier to figure out what's
    	// going on. 

    	double val;
    	double amp = startamp - endamp;
    	
		switch(funType){
			case "root":
				val = amp*(1-Math.sqrt(time/painttime)) + endamp;
				break;
			case "flat":
				val = startamp;
				break;
			case "lin":
				val=amp*(1-(time/painttime)) + endamp;
				break;
			case "true":
				double aCO = clOrbAmp;
			    double paramS = aCO*aCO*painttime;
			    paramS /= (endamp-startamp);
			    paramS /= (startamp+endamp-2.0*aCO);
	
				double del = (aCO-startamp)*(aCO-startamp);
			    del = del/(aCO*aCO);

		    	val=aCO*(1-Math.sqrt(time/paramS + del));
		    	break;
		    case "power":
		    	val=amp*(1-Math.pow(time/painttime, paintPower))  + endamp;
		    	break;
		    default:
		    	val = startamp;
		    	break;
		}
		return val;
    }

    protected void generateMasterWaveform(){
    	double[] swave = new double[wavesize];
	    double[] wave  = new double[wavesize];

	    String waveType = funType;

	    System.out.println(waveType);

	    double ttime = 0.0;

	    if ( waveType.equals("flat") ) endamp = startamp; //Should go in endamp setter.

	    double amp = startamp - endamp;
	    
	    double transitiontime = 0.0;
	    boolean slewflag = false;
	  
	    if(startamp < endamp){
	      boolean transitionneeded = true;
	      transitiontime = 50.0*deltat;
	      fallofftime -= transitiontime;
	      System.out.println("A transition time is necessary before fall-off.");
	    } 
	    holdtime = 3000.0 - flattime - ramptime;
	    double toffset0 = holdtime + ramptime;
	    double toffset1 = holdtime + ramptime + flattime;
	    double toffset2 = holdtime + ramptime + flattime + painttime;
	    double toffset3 = holdtime + ramptime + flattime + painttime + transitiontime;
	    double toffset4 = holdtime + ramptime + flattime + painttime + transitiontime + fallofftime;
	    double riseslope = startamp/ramptime;
	    double fallslope = -endamp/fallofftime;    
	    double totaltime = ramptime + flattime + painttime + transitiontime + fallofftime;
	    // System.out.println(String.valueOf(holdtime));
	    System.out.println(String.valueOf(totaltime));
	    System.out.println(String.valueOf(maxtime));
	    if( totaltime > (maxtime - maxbuffertime)){
			// fileLabel.setText("I'm sorry, I can not create this waveform because it is too long. Please reduce fall-off time.");
			System.out.println("I'm sorry, I can not create this waveform because it is too long. Please reduce fall-off time.");
	    }else{
	    	for(int i=0; i<wavesize; i++){
				swave[i] = deltat*i;
				if(swave[i] < holdtime){
					// Prologue 
					wave[i] = 0.0; 
				} 
				else if(swave[i] <= toffset0){
					// Ramp up
			    	wave[i]=riseslope*(swave[i]-holdtime); 
				}
				else if(swave[i] > ramptime && swave[i] <= (toffset1)){
					// Flat top
			    	wave[i] = startamp; 
				}
				else if(swave[i] > toffset1 && swave[i] <= toffset2){
					// Painting
					double time = swave[i] - toffset1;
					wave[i] = paintFunction(time);  
				}
				else if(swave[i] > toffset2 && swave[i] <= toffset3){
					// Transition period
		   			wave[i]=wave[i-1];
				}
				else if(swave[i] > toffset3 && swave[i] <= toffset4){
					// Fall off	
					wave[i]=fallslope*(swave[i] - toffset3) + endamp;
				}
				else{
					// Epilog 
		    		wave[i]=0.0;
				}

				if(!slewflag && i>0){
		    		if(Math.abs(wave[i]-wave[i-1]) > MAX_SLEW){
						//System.out.println("First slew violation is i " + i + "  " + wave[i] +  "   " + wave[i-1]);
						slewflag = true;
		    		}
				}
	    	}
	    	//End of for loop
	    	if(slewflag){
				System.out.println("Warning: You may be in violation of the maximum slew rate.");
				//fileLabel.setText("Warning: This waveform may be in violation of the maximum slew rate.");
	    	}else{
	    		System.out.println("");
				//fileLabel.setText("");
	    	}
	    	waveform_x = wave;
	    	waveform_t = swave;
		}

    }

    public short[] getKickerWaveform(int kicker){
    	short[] wf = new short[wavesize];

    	for( int i = 0; i < wavesize; i++ ){
    		short wfs = (short)Math.round(scaleWave(waveform_x[i], kickerRatios[kicker]));
    		wf[i] = wfs;
    	}

    	return wf;
    }

     protected double scaleWave(double wave, double scale)
	{
		// Yokogawa uses 16 bit, little endian,
		// 12 bits are used, 4 ignored, 1 bit is "sign"
		// 2048 = "0"
		// For unipolar PS, values should 
		// lie between (2048,4095)
		// return (scale*wave/100.0 + 1)/2.0 * 4095.0; //This version sets 0 = 2047
		// which is not correct, Yokogawa docs spec. that 0 = 2048.
		// Endian-ness will be enfoced at write time by ByteBuffer

		return (scale*wave/100.0)*2047 + 2048;
	}


	protected void writeW16File(String path, String name, int kicker){
		// Write binary waveforms. 
		//
		// https://cdn.tmi.yokogawa.com/BL7077_51E_010.pdf 
		// According to docs for WE7121 format for w16 waveforms
		// is 16 bit little endian, first 12 bits used last 4 ignored.
		// 1 bit of 12 for sign, 11 for magnitude i.e. short < 2048 is 'negative'
		// In practice, this means `short` type in range (1,4095) allowed by Yokogawa
		// Since PS is unipolar, (2048,4095) is allowed.
		// Scaling happens elsewhere. 
		short[] ar = getKickerWaveform(kicker);
		try{
			File f = new File(path, name);
			// FileOutputStream fos  = new FileOutputStream(f); 
			FileChannel fCh = new FileOutputStream(f).getChannel();
			ByteBuffer   bb = ByteBuffer.allocate(2*KickerWaveform.getWavesize()); //16-bit x wavesize
			bb.order(ByteOrder.LITTLE_ENDIAN);

			for(int j=0; j<ar.length; j++){ 
				bb.putShort(ar[j]);
			}
			bb.flip();
			fCh.write(bb);
			fCh.close();

		}catch(IOException ioe){}

	}

    public short[] getKickerWaveformUTCA(int kicker){
    	short[] wf = new short[wavesize];

    	for( int i = 0; i < wavesize; i++ ){
    		wf[i] = scaleForUTCA(waveform_x[i]); // No ratios in uTCA system. All kickers are at same value for production. - NJE
    	}

    	return wf;
    }

    protected short scaleForUTCA(double wave)
	{
		// uTCA has 14 bits available, java 'short' is 16-bit
		double x = wave/100.0;
		x *= 16383;

		return (short)(Math.round(x));
	}

	protected String getUTCAcomment(){
		String comment = "";
		comment += "#" + " holdtime(us) " + String.valueOf(holdtime) + "\n";
		comment += "#" + " ramptime(us) " + String.valueOf(ramptime) + "\n";
		comment += "#" + " flattime(us) " + String.valueOf(flattime) + "\n";
		comment += "#" + " painttime(us) " + String.valueOf(painttime) + "\n";
		comment += "#" + " fallofftime(us) " + String.valueOf(fallofftime) + "\n";
		comment += "#" + " leftovertime(us) " + String.valueOf(leftovertime) + "\n";
		comment += "#" + " maxtime(us) " + String.valueOf(maxtime) + "\n";
		comment += "#" + " paintfunction " + String.valueOf(funType) + "\n";
		comment += "#" + " startamp " + String.valueOf(startamp) + "\n";
		comment += "#" + " endamp " + String.valueOf(endamp) + "\n";
		comment += "#" + " closedoritamp " + String.valueOf(clOrbAmp) + "\n";
		comment += "#" + " deltat " + String.valueOf(deltat) + "\n";
		return comment; 
	}

	protected void writeUTCAFile(String path, String name, int kicker){
		short[] ar = getKickerWaveformUTCA(kicker); 
		try{
			File f = new File(path,name);
			FileWriter fw = new FileWriter(f);
			fw.write(getUTCAcomment());
			for( int j=0; j<ar.length; j++){
				fw.write(String.valueOf(ar[j])); 
				fw.write("\n");
			}
			
			fw.close();
		}catch(IOException ioe){}

		return;
	}

}



/** Controller for the client that monitors the trip monitor services */
public class WaveformFace {
	/** reference to the main window */
	final protected WindowReference windowReference;
	
	final static private int MAX_ROOT_NAME_LENGTH = 17;
    
    /** indicates whether horizontal waveforms will be submitted when the user loads the latest waveforms */
    private boolean _enableHorizontalWaveformSubmission = true;
    
    /** indicates whether vertical waveforms will be submitted when the user loads the latest waveforms */
    private boolean _enableVerticalWaveformSubmission = true;

    private KickerWaveform[] waveObjects = { new KickerWaveform(Plane.HOR), new KickerWaveform(Plane.VER) };
    
	
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
	
	String lastSavedHFile = new String("");
	String lastSavedVFile = new String("");

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
	
	public KickerWaveform getKicker(Plane plane){
		KickerWaveform k = waveObjects[0];	
		switch(plane){
			case HOR:
				k = waveObjects[0];
				break;
			case VER:
				k = waveObjects[1];
				break;
		}

		return k;

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
	    // Set fixed limits on plots 0-105%
	    hplot.setLimitsAndTicksY(0.0,10.0,10,1);
	    vplot.setLimitsAndTicksY(0.0,10.0,10,1);

	    hplot.setLimitsAndTicksX(0.0,1000,10,1);
	    vplot.setLimitsAndTicksX(0.0,1000,10,1);

	    hTable = (JTable)windowReference.getView( "H Table" );
	    vTable = (JTable)windowReference.getView( "V Table" );

	    hplotButton = (JButton)windowReference.getView( "HPlotButton" );
	    hplotButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
		    	hplot.removeAllGraphData();
		    	updateWaveforms();
				makePlanePlot(Plane.HOR);
			}
	    });	

	    vplotButton = (JButton)windowReference.getView( "VPlotButton" );
	    vplotButton.addActionListener( new ActionListener() {
			public void actionPerformed( final ActionEvent event ) {
		    	vplot.removeAllGraphData();
		    	updateWaveforms();
				makePlanePlot(Plane.VER);
			}
	    });	
	    	    
	    hwavelabel = (JLabel)windowReference.getView( "HWaveLabel" );
        hwaveformBox =  (JComboBox<String>)windowReference.getView("H Combo Box");

        vwavelabel = (JLabel)windowReference.getView( "VWaveLabel" );
        vwaveformBox = (JComboBox<String>)windowReference.getView("V Combo Box");

        for(int i = 0; i < (KickerWaveform.getDisplayNames().length); i++){

	    	hwaveformBox.addItem(new String(KickerWaveform.getSupportedWaves()[i]));
	    	vwaveformBox.addItem(new String(KickerWaveform.getSupportedWaves()[i]));
	    }


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
                    if ( canSubmitLatestHorizontalWaveforms() )  submitWaveforms(Plane.HOR);
                    if ( canSubmitLatestVerticalWaveforms() )  submitWaveforms(Plane.VER);
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
                        lastSavedHFile = tokens[0];       // common root for horizontal waveforms
                        submitWaveforms(Plane.HOR);
                    }
                    else if ( tokens[1].contains( "V" ) ){
                        lastSavedVFile = tokens[0];       // common root for vertical waveforms
                        submitWaveforms(Plane.VER);
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

    public void setAction(){
	
		hwaveformBox.addActionListener(new ActionListener(){ 
	    	public void actionPerformed(ActionEvent e) {
	    		updateWaveforms();
	    	}
		});  
	
		vwaveformBox.addActionListener(new ActionListener(){ 
	    	public void actionPerformed(ActionEvent e) {
	    		updateWaveforms();
	    	}
		});  
	
		hwriteButton.addActionListener(new ActionListener(){ 
	    	public void actionPerformed(ActionEvent e) {
	    		writePlaneFiles(Plane.HOR);
	    	}
		});  
		vwriteButton.addActionListener(new ActionListener(){ 
	    	public void actionPerformed(ActionEvent e) {
				writePlaneFiles(Plane.VER);
	    	}
		});
	
	}

    /** make the Table */
	protected void makeTables( ) {
	    String[] colnames = {"Parameter", "Value"};
	    int nrows = 2;

	    hinputtablemodel = new InputTableModel(colnames, 8);
	    hTable.setModel(hinputtablemodel);
	    hTable.getColumnModel().getColumn(0).setMinWidth(150);
	    hTable.getColumnModel().getColumn(1).setMinWidth(100);

	    hinputtablemodel.setValueAt(new String("Start Paint Amp (%)"), 0, 0);
	    hinputtablemodel.setValueAt(new String("End Paint Amp (%)"), 1, 0);
	    hinputtablemodel.setValueAt(new String("Ramp Time (us)"), 2, 0);
	    hinputtablemodel.setValueAt(new String("Flat Time (us)"), 3, 0);
	    hinputtablemodel.setValueAt(new String("Paint Time (us)"), 4, 0);
	    hinputtablemodel.setValueAt(new String("Fall-Off Time (us)"), 5, 0);
	    hinputtablemodel.setValueAt(new String("CO Amp - True Root Only"), 6, 0);
	    hinputtablemodel.setValueAt(new String("Exponent - Power Only"), 7, 0);

	    hinputtablemodel.setValueAt(new String("100.0"), 0, 1);
	    hinputtablemodel.setValueAt(new String("50.0"), 1, 1);
	    hinputtablemodel.setValueAt(new String("1000.0"), 2, 1);
	    hinputtablemodel.setValueAt(new String("480.0"), 3, 1);
	    hinputtablemodel.setValueAt(new String("1000.0"), 4, 1);
	    hinputtablemodel.setValueAt(new String("300.0"), 5, 1);
	    hinputtablemodel.setValueAt(new String("120.0"), 6, 1);
	    hinputtablemodel.setValueAt(new String("2.0"), 7, 1);
	    hinputtablemodel.fireTableDataChanged();  
	    
	    vinputtablemodel = new InputTableModel(colnames, 8);
	    vTable.setModel(vinputtablemodel);
	    vTable.getColumnModel().getColumn(0).setMinWidth(150);
	    vTable.getColumnModel().getColumn(1).setMinWidth(100);

	    vinputtablemodel.setValueAt(new String("Start Paint Amp (%)"), 0, 0);
	    vinputtablemodel.setValueAt(new String("End Paint Amp (%)"), 1, 0);
	    vinputtablemodel.setValueAt(new String("Ramp Time (us)"), 2, 0);
	    vinputtablemodel.setValueAt(new String("Flat Time (us)"), 3, 0);
	    vinputtablemodel.setValueAt(new String("Paint Time (us)"), 4, 0);
	    vinputtablemodel.setValueAt(new String("Fall-Off Time (us)"), 5, 0);
	    vinputtablemodel.setValueAt(new String("CO Amp - True Root Only"), 6, 0);
	    vinputtablemodel.setValueAt(new String("Exponent - Power Only"), 7, 0);

	    vinputtablemodel.setValueAt(new String("100.0"), 0, 1);
	    vinputtablemodel.setValueAt(new String("50.0"), 1, 1);
	    vinputtablemodel.setValueAt(new String("1000.0"), 2, 1);
	    vinputtablemodel.setValueAt(new String("480.0"), 3, 1);
	    vinputtablemodel.setValueAt(new String("1000.0"), 4, 1);
	    vinputtablemodel.setValueAt(new String("300.0"), 5, 1);
	    vinputtablemodel.setValueAt(new String("120.0"), 6, 1);
	    vinputtablemodel.setValueAt(new String("2.0"), 7, 1);
	    vinputtablemodel.fireTableDataChanged();  
	    
	    
	}
    
    
    /** determine whether the latest horizontal waveforms can be submitted */
    public boolean canSubmitLatestHorizontalWaveforms() {
        return _enableHorizontalWaveformSubmission && ( lastSavedHFile != null && lastSavedHFile.length() > 0 );
    }
    
    
    /** determine whether the latest vertical waveforms can be submitted */
    public boolean canSubmitLatestVerticalWaveforms() {
        return _enableVerticalWaveformSubmission && ( lastSavedVFile != null && lastSavedVFile.length() > 0 );
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
    

    public void updateWaveforms(){

    	String[] types = KickerWaveform.getSupportedWaves();

    	// Update Horizontal 
	    int type = hwaveformBox.getSelectedIndex();
	    hwaveformtype = types[type];
	    getKicker(Plane.HOR).setFunType(types[type]);

	    double startamp  = Double.valueOf(((String)hTable.getValueAt(0,1)).trim()).doubleValue();
	    double endamp    = Double.valueOf(((String)hTable.getValueAt(1,1)).trim()).doubleValue();
	    double ramptime  = Double.valueOf(((String)hTable.getValueAt(2,1)).trim()).doubleValue();
	    double flattime  = Double.valueOf(((String)hTable.getValueAt(3,1)).trim()).doubleValue();

	    double painttime = Double.valueOf(((String)hTable.getValueAt(4,1)).trim()).doubleValue();
	    double falltime  = Double.valueOf(((String)hTable.getValueAt(5,1)).trim()).doubleValue();
	    double coAmp     = Double.valueOf(((String)hTable.getValueAt(6,1)).trim()).doubleValue();
	    double powExp    = Double.valueOf(((String)hTable.getValueAt(7,1)).trim()).doubleValue();

	    getKicker(Plane.HOR).setStartAmp( startamp );
	    getKicker(Plane.HOR).setEndAmp( endamp );

	    getKicker(Plane.HOR).setClosedOrbitAmp( coAmp );
		getKicker(Plane.HOR).setPowExp( powExp );

	    getKicker(Plane.HOR).setRampTime( ramptime );
	    getKicker(Plane.HOR).setFlatTime( flattime );
	    getKicker(Plane.HOR).setPaintTime( painttime );
	    getKicker(Plane.HOR).setFallTime( falltime );

		
		// Update Vertical 
		type = vwaveformBox.getSelectedIndex();
	    vwaveformtype = types[type];
	    getKicker(Plane.VER).setFunType(types[type]);

		startamp  = Double.valueOf(((String)vTable.getValueAt(0,1)).trim()).doubleValue();
		endamp    = Double.valueOf(((String)vTable.getValueAt(1,1)).trim()).doubleValue();
		ramptime  = Double.valueOf(((String)hTable.getValueAt(2,1)).trim()).doubleValue();
		flattime  = Double.valueOf(((String)hTable.getValueAt(3,1)).trim()).doubleValue();

	    painttime = Double.valueOf(((String)vTable.getValueAt(4,1)).trim()).doubleValue();
	    falltime  = Double.valueOf(((String)vTable.getValueAt(5,1)).trim()).doubleValue();
	    coAmp     = Double.valueOf(((String)vTable.getValueAt(6,1)).trim()).doubleValue();
	    powExp    = Double.valueOf(((String)vTable.getValueAt(7,1)).trim()).doubleValue();

	    getKicker(Plane.VER).setStartAmp( startamp );
	    getKicker(Plane.VER).setEndAmp( endamp );

	    getKicker(Plane.VER).setClosedOrbitAmp( coAmp );
		getKicker(Plane.VER).setPowExp( powExp );
		
	    getKicker(Plane.VER).setRampTime( ramptime );
	    getKicker(Plane.VER).setFlatTime( flattime );
	    getKicker(Plane.VER).setPaintTime( painttime );
	    getKicker(Plane.VER).setFallTime( falltime );

		//Generate the updated waveforms. 
		getKicker(Plane.HOR).generateMasterWaveform();
		getKicker(Plane.VER).generateMasterWaveform();

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

    protected void updateNameField(Plane plane){

    	String name = getKicker(plane).generateWaveformBasename();

    	switch(plane){
    		case HOR:
				hRootNameField.setText(name);	
				System.out.println("hchildname set to " + name);
				break;
			case VER:
				vRootNameField.setText(name);
				System.out.println("vchildname set to " + name);
				break;

		}
    }
    	
	/** make the plot for specified plane */
	protected void makePlanePlot(Plane p) {
		
		updateWaveforms();

	    FunctionGraphsJPanel plot = (p == Plane.HOR) ? hplot : vplot;

	    double xlim = getKicker(p).getWavetime();
	    plot.setLimitsAndTicksX(0.0,(int)(xlim/10.+1),10,1);
	    
	    final BasicGraphData graphData = new BasicGraphData();
	    graphData.setGraphColor(Color.RED);

	    getKicker(p).generateMasterWaveform();
	    graphData.addPoint(getKicker(p).getTimeSteps(), getKicker(p).getWaveform());
	    plot.addGraphData(graphData);

	    updateNameField(p);		
		
	}	
	
	protected void makeWaveform(Plane plane){

		updateWaveforms();
		makePlanePlot(plane);

	}	

	/**
	* Generate file name
	* @return string with file name
	*/

	protected String generateFilename(String base, Plane p, int kicker){
		//Note - kickers are stored in an array so accessed with (0,3), but 
		// are referred to as kickers 1-4
		String name = new String( base + "_" + getKicker(p).getPlaneIDStr() + Integer.toString(kicker) + ".w16" );
		return name;

	}

	protected String generateUTCAFilename(String base, Plane p, int kicker){
		//Note - kickers are stored in an array so accessed with (0,3), but 
		// are referred to as kickers 1-4
		String name = new String( base + "_" + getKicker(p).getPlaneIDStr() + Integer.toString(kicker) + ".inj" );
		return name;

	}

	/**
	* Write binary files, or ASCII files as the case may be.
	*/
	protected void writePlaneFiles(Plane p){

		String planeString = getKicker(p).getPlaneIDStr();
		String childname = "";

		if( p == Plane.HOR ){
			childname = hRootNameField.getText();
			lastSavedHFile = childname;
		}else if( p == Plane.VER){
			childname = vRootNameField.getText();
			lastSavedVFile = childname;
		}
            
        final File file = getWaveformDirectory( true );
        System.out.println( "Writing files to: " + file );

        if( file != null ){
            String parentpath = file.getPath();
            
            final String errorMessage = validateBaseFileName( childname );

            if( errorMessage == null )
            {
            	String message = "Wrote files with rootnames ";

            	for(int i = 0; i < 4; i++){
            		// String name  = generateFilename(childname, p, i+1);
            		// getKicker(p).writeW16File(parentpath, name, i);

            		String name = generateUTCAFilename(childname, p, i+1);
            		getKicker(p).writeUTCAFile(parentpath,name, i); //Probably need to write files kicker by kicker now. 
            		message += name + " ";
            	}
                
                fileLabel.setText(message);
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
	    		
	
	/** Routine to submit the last saved waveforms */
	protected void submitWaveforms(Plane plane){
		String mess = (plane == Plane.HOR) ? "Submitting horizontal waveforms with root: " + lastSavedHFile : "Submitting vertical waveforms with root: " + lastSavedVFile;

        System.out.println( mess );
	    
        String currentName = (plane == Plane.HOR) ? lastSavedHFile : lastSavedVFile;

	    String k1name = generateFilename(currentName, plane, 1);
	    String k2name = generateFilename(currentName, plane, 2);
	    String k3name = generateFilename(currentName, plane, 3);
	    String k4name = generateFilename(currentName, plane, 4);
	    
	    if(currentName.equals("")){
			System.out.println("There is no waveform file saved.");
	    }
	    else{
			Channel k1ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKick" + getKicker(plane).getPlaneIDStr() + "01:7121:FGWAVE");
			Channel k2ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKick" + getKicker(plane).getPlaneIDStr() + "02:7121:FGWAVE");
			Channel k3ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKick" + getKicker(plane).getPlaneIDStr() + "03:7121:FGWAVE");
			Channel k4ch = ChannelFactory.defaultFactory().getChannel("Ring_Mag:PS_IKick" + getKicker(plane).getPlaneIDStr() + "04:7121:FGWAVE");

			k1ch.connectAndWait();
			k2ch.connectAndWait();
			k3ch.connectAndWait();
			k4ch.connectAndWait();

			try{
		    	k1ch.putVal(k1name);
		    	k2ch.putVal(k2name);
		    	k3ch.putVal(k3name);
		    	k4ch.putVal(k4name);
		    	Channel.flushIO();
		    	System.out.println("Loaded filenames: " + k1name + "\t" + k2name + "\t" + k3name + "\t" + k4name);
				fileLabel.setText( "Loaded filenames: " + k1name + "\t" + k2name + "\t" + k3name + "\t" + k4name);
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

