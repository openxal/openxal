/*************************************************************
//
// class ChromFace:
// This class is responsible for the Graphic User Interface
// components and action listeners for the chrom setting tab.
//
/*************************************************************/

package xal.app.rocs;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.io.*;
import java.lang.*;
import xal.extension.application.Application;
import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;

import java.net.URL;

public class ChromFace extends JPanel implements OpticsListener{
    
    private static final long serialVersionUID = 1L;
    
    CalcSettings set = new CalcSettings();
    EdgeLayout layout = new EdgeLayout();	
 
    //Variable Declaration
    //Declaration of all GUI components
    protected OpticsListener opticsProxy;

    private TextScrollDouble[] scrollX = new TextScrollDouble[1];
    private TextScrollDouble[] scrollY = new TextScrollDouble[1];
    private TextScrollDouble scrollPercent = new TextScrollDouble();
    private DecimalField[] decOut = new DecimalField[4];
    private DecimalField[] decRead = new DecimalField[4];
    private DecimalField energyfield;
    private DecimalField xChrom, yChrom;
    public JComboBox<String> tunepoint, increment, pincrement;
    private JButton calcbutton1, submitbutton, calcbutton2;
    private JToggleButton usePercentAdj;
    private JPanel mainPanel;
    private JPanel[] p = new JPanel[9];

    private JLabel lblOutput,lblX,lblY,lblInc,lblTune,lblReadBack;
    private JLabel lblPercent, lblPInc;
    private JLabel[] slabel = new JLabel[4];
    private JLabel[] blank = new JLabel[5];
    private JLabel xLabel, yLabel, setpointlabel;
    private JLabel energylabel;
    private NumberFormat numFor;

    public double chromMinx, chromMaxx, chromMiny, chromMaxy, x, y;
    public double tune_x, tune_y;
    public double[] local_k = new double[4];
    public double[] frozen_k = new double[4];
    public int pvcounter=0;
    public int[] mademonitor= new int[6];
    
    private String[] incarray = {"0.01","0.05","0.1","0.25", "0.5", "1.0"}; 
    private String[] tunearray = {"(6.23, 6.20)", "(6.23, 6.20)"};
    private String[] pincarray = {"0.1","1","5","10"}; 
    GenDocument doc;
    GenWindow window;
    URL url;
    int i;
    
    
    
    //Member function Constructor
    public ChromFace(GenDocument aDocument, GenWindow parent){
	
	url = Application.getAdaptor().getResourceURL( "Chrom_623_620.dat" );
	doc=aDocument;
	doc.addOpticsListener(this);
	window = parent;
	
	setPreferredSize(new Dimension(700,550));
	setLayout(layout);
	numFor = NumberFormat.getNumberInstance();
	numFor.setMinimumFractionDigits(4);

	callRead();       //Read in data from file
	makeComponents(); //Creation of all GUI components
	setColor();       //Set the color for the buttons
	addComponents();  //Add all components to the layout and panels
	setTips();        //Set the tool tips
	setAction();      //Set the action listeners
	reconcileK(doc.getSextK());
    }

    public void addComponents(){

	EdgeLayout toprightlayout = new EdgeLayout();
	p[1].setPreferredSize(new Dimension(300,200));
	p[1].setBorder(BorderFactory.createRaisedBevelBorder());
	p[1].setLayout(toprightlayout);
	toprightlayout.setConstraints(energylabel, 10, 10, 100, 100, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(energyfield, 10, 160, 100, 0, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(lblInc, 45, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(increment, 42, 140, 100, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(lblX,       90, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(scrollX[0], 85, 120, 200, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(lblY,       120, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(scrollY[0], 115, 120, 200, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toprightlayout.setConstraints(calcbutton1, 160, 60, 10, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	
	p[1].add(energylabel); p[1].add(energyfield);
	p[1].add(lblInc); p[1].add(increment);
	p[1].add(lblX); p[1].add(scrollX[0]);
	p[1].add(lblY); p[1].add(scrollY[0]);
	p[1].add(calcbutton1);
	
	p[2].setBorder(BorderFactory.createRaisedBevelBorder());
	p[2].setLayout(new GridLayout(6,1));
	p[2].add(blank[0]);
	for(i=0; i<=3; i++) p[2].add(slabel[i]);

	p[3].setBorder(BorderFactory.createRaisedBevelBorder());
	p[3].setLayout(new GridLayout(6,1));
	p[3].add(lblOutput);
	for(i=0; i<=3; i++) p[3].add(decOut[i]);
	p[3].add(submitbutton);
       
	p[4].setBorder(BorderFactory.createRaisedBevelBorder());
	p[4].setLayout(new GridLayout(6,1));
	p[4].add(lblReadBack);
	for(i=0; i<=3; i++) p[4].add(decRead[i]);

	p[5].setLayout(new GridLayout(1,2));  
	p[5].add(p[2]);
	p[5].add(p[3]);
	p[5].add(p[4]);
       
	p[6].setLayout(new GridLayout(2,2));
	p[6].add(xLabel);
	p[6].add(xChrom);
	p[6].add(yLabel);
	p[6].add(yChrom);

	p[7].setBorder(BorderFactory.createTitledBorder(""));
	p[7].setLayout(new GridLayout(1,2));
	p[7].add(setpointlabel);
	p[7].add(p[6]);
	
	EdgeLayout topleftlayout = new EdgeLayout();
	p[8].setPreferredSize(new Dimension(300,200));
	p[8].setBorder(BorderFactory.createRaisedBevelBorder());
	p[8].setLayout(topleftlayout);
	topleftlayout.setConstraints(usePercentAdj, 10, 45, 100, 100, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	//topleftlayout.setConstraints(energylabel, 40, 10, 100, 100, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	//topleftlayout.setConstraints(energyfield, 40, 160, 100, 0, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	topleftlayout.setConstraints(lblPInc, 75, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	topleftlayout.setConstraints(pincrement, 72, 140, 100, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	topleftlayout.setConstraints(lblPercent, 110, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	topleftlayout.setConstraints(scrollPercent, 125, 10, 200, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	topleftlayout.setConstraints(calcbutton2, 160, 60, 10, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	
	p[8].add(usePercentAdj);
	//p[8].add(energylabel); p[8].add(energyfield);
	p[8].add(lblPInc); p[8].add(pincrement);
	p[8].add(lblPercent); p[8].add(scrollPercent);
	p[8].add(calcbutton2);
	
	mainPanel.setBorder(BorderFactory.createTitledBorder(" Set Chromaticity "));
	mainPanel.setPreferredSize(new Dimension(650,500));
	EdgeLayout mainlayout = new EdgeLayout();
	mainPanel.setLayout(mainlayout);
	mainlayout.setConstraints(p[1], 20, 15, 300, 300, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	mainlayout.setConstraints(p[8], 20, 335, 350, 10, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	mainlayout.setConstraints(p[7], 240, 175, 100, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	mainlayout.setConstraints(p[5], 300, 115, 10, 300, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	
	mainPanel.add(p[1]); mainPanel.add(p[8]);
	mainPanel.add(p[5]); mainPanel.add(p[7]);
	layout.add(mainPanel, this, 20, 20, EdgeLayout.LEFT);        
   }

    public void makeComponents(){
	//Create the components with default values
	scrollX[0] = new TextScrollDouble(0,0.0,chromMinx,chromMaxx,.001,.001);
	scrollY[0] = new TextScrollDouble(0,0.0,chromMiny,chromMaxy,.001,.001);
	scrollPercent = new TextScrollDouble(0,0,-10,10,1,0.1);
	scrollPercent.Disable();

       	lblOutput = new JLabel(" Sextupole Set Points: ");
	lblReadBack = new JLabel(" Sextupole Read Back: ");
	lblX = new JLabel("X chromaticity: ");
	lblY = new JLabel("Y chromaticity: ");
	
	lblInc = new JLabel("Slider Increment:");
	lblTune = new JLabel("Nearest Tune:");

	lblPercent = new JLabel("% Change in Magnet Settings: ");
	lblPInc = new JLabel("Slider Increment:      ");
	
	energylabel = new JLabel("Scale for Energy (GeV):");

	slabel[0] = new JLabel(" SV03a07 (Amps):   "); 
	slabel[1] = new JLabel(" SH04 (Amps):      ");
	slabel[2] = new JLabel(" SV05 (Amps):      "); 
	slabel[3] = new JLabel(" SH06 (Amps):      ");

	for(i=0; i<=4; i++){
	blank[i] = new JLabel("");
	}

	xLabel = new JLabel("  X Chrom. ");
	yLabel = new JLabel("  Y Chrom. ");

	usePercentAdj = new JToggleButton("Click to adjust by percent");
	usePercentAdj.setSelected(false);

	setpointlabel = new JLabel("Current Setpoint: ");
	
	calcbutton1 = new JButton("Calculate Sextupoles");
	submitbutton = new JButton(" Submit ");
	submitbutton.setEnabled(false);
	calcbutton2 = new JButton("Calculate Sextupoles");
	calcbutton2.setEnabled(false);

	for(i=0; i<=8; i++) p[i] = new JPanel();
	mainPanel = new JPanel();
	tunepoint = new JComboBox<String>(tunearray);
	increment = new JComboBox<String>(incarray);
	pincrement = new JComboBox<String>(pincarray);
	pincrement.setEnabled(false);
	pincrement.setSelectedIndex(1);

	xChrom = new DecimalField(0, 3, numFor);
	xChrom.clear();
	yChrom = new DecimalField(0, 3, numFor);
	yChrom.clear();

	energyfield = new DecimalField(1.0, 6, numFor);

	//Declare the decimal fields, but also check for channel connection 
	//and set listener for enabling and disabling the fields. 
	for(i=0; i<=3; i++){
		decOut[i] = new DecimalField(0,6,numFor);

		// if the current and field channels are connected then activate the current text field and setup magnet field the limits
		if( doc.sextCurrentChannel[i].isConnected() && doc.sext_ch[i].isConnected() ){
			setupFieldLimits( i, doc.sext_ch[i] );
			activateCurrentSet(i, decOut[i], doc.sextCurrentChannel[i]);
		}
		else {
			decOut[i].setEnabled(false);
		}
		
		doc.sextCurrentChannel[i].addChannelConnectionListener(new ConnectionListener(){
			final int j=i;
			public void connectionMade(Channel aChannel) {
				activateCurrentSet(j, decOut[j], doc.sextCurrentChannel[j]);
			}
			public void connectionDropped(Channel aChannel) {
				decOut[j].setEnabled(false);
				pvcounter--;
				refreshSubmitStatus();
			}
		});
	}

	//Declare readback decimal fields, and make a readback listener for each field.
	for(i=0; i<=3; i++){
		decRead[i] = new DecimalField(0,6,numFor);
		if(doc.sextCurrentChannel[i].isConnected()==true){
		    decRead[i].setEnabled(true);
		    if(mademonitor[i]!=1){
			makeCurrentMonitor(decRead[i], doc.sextCurrentChannel[i]);
			mademonitor[i]=1;
		    }
		}
		if(doc.sextCurrentChannel[i].isConnected()==false){
		    decRead[i].setEnabled(false);
		}
		doc.sextCurrentChannel[i].addChannelConnectionListener(new ConnectionListener(){
		    final int j=i;
		    public void connectionMade(Channel aChannel) {
			decRead[j].setEnabled(true);
			if(mademonitor[j]!=1){
			   makeCurrentMonitor(decRead[j], doc.sextCurrentChannel[j]);
			   mademonitor[j]=1;
			}
		    }
		    public void connectionDropped(Channel aChannel) {
			decRead[j].setEnabled(false);
		    }
		});
	}
    }

	/********** this is all messed up since we are mixing field and current ************/
	public void activateCurrentSet(int i, final DecimalField dField, ChannelAgent sextCurrentChannel){
		dField.setEnabled(true);
		pvcounter++;
		refreshSubmitStatus();
	}


	public void setupFieldLimits(int i, ChannelAgent sext_ch){
		doc.sext_k_llimit[i]=sext_ch.getMagLowLimit();
		doc.sext_k_ulimit[i]=sext_ch.getMagUpLimit();
		System.out.println("Sextupole " + sext_ch.theChannel.channelName() + " control limits are = " + doc.sext_k_llimit[i] + " to " + doc.sext_k_ulimit[i]);
	}

	/********** this is all messed up since we are mixing field and current ************/
//    public void activateFieldSet(int i, final DecimalField dField, ChannelAgent sextCurrentChannel){
//		dField.setEnabled(true);
//		doc.sext_k_llimit[i]=sext_ch.getMagLowLimit();
//		doc.sext_k_ulimit[i]=sext_ch.getMagUpLimit();
//		System.out.println("Sextupole " + sext_ch.theChannel.channelName() + " control limits are = " + doc.sext_k_llimit[i] + " to " + doc.sext_k_ulimit[i]);
//		pvcounter++;
//		refreshSubmitStatus();
//    }

    public void refreshSubmitStatus(){
		submitbutton.setEnabled( pvcounter >= 4 );
    }
	
	public void makeCurrentMonitor(final DecimalField dField, ChannelAgent currentChannel){
		currentChannel.addReadbackListener( new ReadbackListener(){
			public void updateReadback(Object sender, String name, double value){
				dField.setValue(value);
			}
		});
	}


    public void setAction(){
	//Add all of the action listeners.

	//Action listeners for the nearest tune point
	//This will determine which chromaticity grid is used
	tunepoint.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		if(tunepoint.getSelectedIndex() == 0){
		    tune_x=6.23;
		    tune_y=6.20;
		}
		if(tunepoint.getSelectedIndex() == 1){
		    tune_x=6.23;
		    tune_y=6.20;
		}
	    }
	});
	//Action listeners for the increment field
	//Increment action listener -- allows user to set the increment
	increment.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		if(increment.getSelectedIndex() == 0){
		    scrollX[0].setIncrement(.01);
		    scrollY[0].setIncrement(.01);}
		if(increment.getSelectedIndex() == 1){
		    scrollX[0].setIncrement(.05);
		    scrollY[0].setIncrement(.05);}
		if(increment.getSelectedIndex() == 2){
		    scrollX[0].setIncrement(.1);
		    scrollY[0].setIncrement(.1);}
		if(increment.getSelectedIndex() == 3){
		    scrollX[0].setIncrement(.25);
		    scrollY[0].setIncrement(.25);}
		if(increment.getSelectedIndex() == 4){
		    scrollX[0].setIncrement(.5);
		    scrollY[0].setIncrement(.5);}
		if(increment.getSelectedIndex() == 5){
		    scrollX[0].setIncrement(1.0);
		    scrollY[0].setIncrement(1.0);}
	    }
	});

	//Action listeners for the percent change increment field
	//Increment action listener -- allows user to set the increment
	pincrement.addActionListener(new ActionListener(){ 
		public void actionPerformed(ActionEvent e) {
		    if(pincrement.getSelectedIndex() == 0){
			scrollPercent.setIncrement(0.1);
			scrollPercent.setIncrement(0.1);}
		    if(pincrement.getSelectedIndex() == 1){
			scrollPercent.setIncrement(1.0);
			scrollPercent.setIncrement(1.0);}
		    if(pincrement.getSelectedIndex() == 2){
			scrollPercent.setIncrement(5.0);
			scrollPercent.setIncrement(5.0);}
		    if(pincrement.getSelectedIndex() == 3){
			scrollPercent.setIncrement(10.0);
			scrollPercent.setIncrement(10.0);}
		}
	});
	
	//Action Listener for the calcbutton1 button
	//Calculates sext strengths from the grid interpolation.
	calcbutton1.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    x = scrollX[0].getValue(); 
		    y = scrollY[0].getValue();
		    doc.callChromCalc(url,energyfield.getDoubleValue(),x,y);
		    for(i=0; i<=3; i++) decOut[i].setValue(local_k[i]);
		    usePercentAdj.setSelected(false);
		    scrollPercent.Disable();
		    pincrement.setEnabled(false);
		    calcbutton2.setEnabled(false);
		    doc.settingChanged(this);
		}
	    });			     
	
	usePercentAdj.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {

		if(usePercentAdj.isSelected()==true){
		    double prange[] = new double[2];
		    int i, imin, imax;
		    
		    prange=doc.getPercentRange(local_k);
		    scrollPercent.setRange(prange[0],prange[1]);
		    scrollPercent.setValue(0.0);
		    scrollPercent.Enable();
		    pincrement.setEnabled(true);
		    calcbutton2.setEnabled(true);
		    frozen_k[0]=local_k[0];
		    frozen_k[1]=local_k[1];
		    frozen_k[2]=local_k[2];
		    frozen_k[3]=local_k[3];
		}
		if(usePercentAdj.isSelected()==false){
		    scrollPercent.Disable();
		    pincrement.setEnabled(false);
		    calcbutton2.setEnabled(false);
		}
	    }
	});

	//Action Listener for the submitbutton button
	//It sends the values to Channel Access
	submitbutton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    for(i=0; i<=3; i++) local_k[i]=decOut[i].getDoubleValue();
		    doc.setSextK(local_k);
		    doc.setSextChannelAccess();
		    doc.setChroms(x,y);
		    doc.settingChanged(this);
		}
	    }); 

    	//Action Listener for the b4 button
	//It set the sextupole magnets by % values
	calcbutton2.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    double[] temp = new double[4];
		    local_k[0] = frozen_k[0]*(1+(scrollPercent.getValue() * 0.01)); 
		    local_k[1] = frozen_k[1]*(1+(scrollPercent.getValue() * 0.01)); 
		    local_k[2] = frozen_k[2]*(1+(scrollPercent.getValue() * 0.01)); 
		    local_k[3] = frozen_k[3]*(1+(scrollPercent.getValue() * 0.01));
		    decOut[0].setValue(local_k[0]);
		    decOut[1].setValue(local_k[1]);
		    decOut[2].setValue(local_k[2]);
		    decOut[3].setValue(local_k[3]);
		    doc.setSextK(local_k);
		    doc.setChroms(-2222,-2222);
		    scrollPercent.setValue(0);
		    doc.settingChanged(this);
		}
	    });
	   

    }
    
    public void setColor(){
	//this color is the same as the default color that is 
	//used by the Java classes for their scroll bars etc...
	Color color = new Color(150,150,210);
	calcbutton1.setBackground(color); 
	submitbutton.setBackground(color);
	calcbutton2.setBackground(color);
	usePercentAdj.setBackground(color);
    }

    public void callRead(){  //Do some parameter initialization.
	try{              
	    set.readData(url);
	}
	catch(IOException ioe){}

	//Setting limits for the scrollbars
	chromMinx = set.getMinx(); chromMaxx = set.getMaxx();
	chromMiny = set.getMiny(); chromMaxy = set.getMaxy();
    }

    public void setTips(){
	//Set all tool tip text
	//the resonance grid does not have a tool tip.. that may be
	//something to add in future
	tunepoint.setToolTipText("Increment setting for the x and y tunes.");
	increment.setToolTipText("Increment setting for the scrollbars.");
	p[1].setToolTipText("Sextupole set point values after interpolation.");
	p[5].setToolTipText("Sextupole read back values.");
	calcbutton1.setToolTipText("Calculation based on the x and y chrom");
	submitbutton.setToolTipText("Send sextupole values to machine");
    }
  

    public void updateQuadK(Object sender, double[] k) {
    }
    public void updateSextK(Object sender, double[] k) {
	reconcileK(k);
    }
    public void reconcileK(double[] k){
        System.arraycopy(k, 0, local_k, 0, k.length);
	for(i=0; i<=3; i++) decOut[i].setValue(local_k[i]);
    }
    public void updateTunes(Object sender, double tunex, double tuney){}
    public void updateChroms(Object sender, double chromx, double chromy){
	if(chromx == -2222) xChrom.clear();
	else{
	    xChrom.setValue(chromx);
	}
	if(chromy == -2222) yChrom.clear();
	else{
	    yChrom.setValue(chromy);
	}
    }
    public void updatePhases(Object sender, double phasex, double phasey){}
}



