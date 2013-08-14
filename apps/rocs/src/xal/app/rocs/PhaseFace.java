/*************************************************************/
//
// class TuneFace:
// This class is responsible for the Graphic User Interface
// components and action listeners for the tune setting tab.
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
import xal.tools.swing.*;
import xal.tools.apputils.EdgeLayout;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;
import java.net.URL;

public class PhaseFace extends JPanel implements OpticsListener{
    
    private static final long serialVersionUID = 1L;
    
    CalcSettings set = new CalcSettings();
    EdgeLayout layout = new EdgeLayout();	
 
    //Variable Declaration
    //Declaration of all GUI components
    protected OpticsListener opticsProxy;

    private TextScrollDouble[] scrollX = new TextScrollDouble[1];
    private TextScrollDouble[] scrollY = new TextScrollDouble[1];
    private DecimalField[] decOut = new DecimalField[6];
    private DecimalField[] decRead = new DecimalField[6];
    private DecimalField energyfield;
    private DecimalField xPhase, yPhase;
    private JComboBox<String> tunepoint, increment;
    private JButton calcbutton, submitbutton;
    private ButtonGroup group;
    private JRadioButton Achr, NoAchr;

    public int dataset=0;

    public double phaseMinx, phaseMaxx;
    public double phaseMiny, phaseMaxy;
    public double x, y;
    public double tune_x, tune_y;
    public double[] local_k = new double[6];
    private double xdefault = 4*0.25, ydefault = 4*0.27;
    public int pvcounter=0;
    public int[] mademonitor= new int[6];

    private JLabel lblOutput,lblX,lblY,lblInc,lblTune,lblReadBack;
    private JLabel[] klabel = new JLabel[6];
    private JLabel blank;
    private JLabel xLabel, yLabel, setpointlabel;
    private JLabel energylabel;
    private NumberFormat numFor;
 

    public JPanel mainPanel;
    public JPanel[] p = new JPanel[12];
    private String[] incarray = {"0.001", "0.005", "0.01"}; 
    private String[] tunearray = {"Qx=6.23, Qy=6.20", "Qx=6.23, Qy=6.20"};
    GenDocument doc; 
    GenWindow window;
    URL url;
    
    int i;
    
    //Member function Constructor
    public PhaseFace(GenDocument aDocument, GenWindow parent){
		
	doc=aDocument;
	doc.addOpticsListener(this);
	url = getClass().getResource("resources/Phase_623_620_Achromat.dat");
	window = parent;
	
	setPreferredSize(new Dimension(500,600));
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

	EdgeLayout toplayout = new EdgeLayout();
	p[0].setPreferredSize(new Dimension(350,240));
	p[0].setBorder(BorderFactory.createRaisedBevelBorder());
	p[0].setLayout(toplayout);
	toplayout.setConstraints(Achr, 10, 10, 100, 100, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(NoAchr, 30, 10, 100, 0, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(lblTune, 60, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(tunepoint, 60, 120, 100, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(lblInc, 85, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(increment, 85, 120, 100, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(energylabel, 120, 10, 100, 100, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(energyfield, 120, 170, 100, 0, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(lblX,       145, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(scrollX[0], 140, 190, 200, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(lblY,       175, 10, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(scrollY[0], 170, 190, 200, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	toplayout.setConstraints(calcbutton, 205, 80, 10, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);

	p[0].add(Achr); p[0].add(NoAchr);
	p[0].add(lblTune); p[0].add(tunepoint);
	p[0].add(lblInc); p[0].add(increment);
	p[0].add(energylabel); p[0].add(energyfield);
	p[0].add(lblX); p[0].add(scrollX[0]);
	p[0].add(lblY); p[0].add(scrollY[0]);
	p[0].add(calcbutton);
	
	p[3].setBorder(BorderFactory.createRaisedBevelBorder());
	p[3].setLayout(new GridLayout(8,1));
	p[3].add(blank);
	for(i=0; i<=5; i++) p[3].add(klabel[i]);
	p[3].add(submitbutton);
	
	p[4].setBorder(BorderFactory.createRaisedBevelBorder());
	p[4].setLayout(new GridLayout(8,1));
	p[4].add(lblOutput);
	for(i=0; i<=5; i++) p[4].add(decOut[i]);
	p[4].add(submitbutton);
	   
	p[5].setBorder(BorderFactory.createRaisedBevelBorder());
	p[5].setLayout(new GridLayout(8,1));
	p[5].add(lblReadBack);
	for(i=0; i<=5; i++) p[5].add(decRead[i]); 
	
	p[6].setLayout(new GridLayout(1,2));  
	p[6].add(p[3]);
	p[6].add(p[4]);
	p[6].add(p[5]);
	   
	p[7].setLayout(new GridLayout(2,2));
	p[7].add(xLabel);
	p[7].add(xPhase);
	p[7].add(yLabel);
	p[7].add(yPhase);
	
	p[8].setBorder(BorderFactory.createTitledBorder(""));
	p[8].setLayout(new GridLayout(1,2));
	p[8].add(setpointlabel);
	p[8].add(p[7]);

	mainPanel.setBorder(BorderFactory.createTitledBorder(" Set Arc Phase "));
	mainPanel.setPreferredSize(new Dimension(450,550));
	EdgeLayout mainlayout = new EdgeLayout();
	mainPanel.setLayout(mainlayout);
	mainlayout.setConstraints(p[0], 20, 50, 300, 10, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	mainlayout.setConstraints(p[8], 270, 90, 10, 10, EdgeLayout.TOP, EdgeLayout.NO_GROWTH);
	mainlayout.setConstraints(p[6], 330, 30, 10, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
	
	mainPanel.add(p[0]); mainPanel.add(p[8]); mainPanel.add(p[6]); 
	layout.add(mainPanel, this, 20, 10, EdgeLayout.LEFT);        
   }

    public void makeComponents(){
	//Creation of all GUI components with default values
	scrollX[0] = new TextScrollDouble(0,xdefault,phaseMinx,phaseMaxx,.001,.001);
	scrollY[0] = new TextScrollDouble(0,ydefault,phaseMiny,phaseMaxy,.001,.001);
	lblOutput = new JLabel  (" Calculated Fields:");
	lblReadBack = new JLabel(" Machine Settings: ");
	
	lblX = new JLabel("X arc phase (in units of 2pi): ");
	lblY = new JLabel("Y arc phase (in units of 2pi): ");
	
	lblInc = new JLabel("Increment: ");
	lblTune = new JLabel("Nearest Tune: ");
	klabel[0] = new JLabel(" QH03a05a07 (T/m):");
	klabel[1] = new JLabel(" QH02a08 (T/m):   ");
	klabel[2] = new JLabel(" QH04a06 (T/m):   ");
	klabel[3] = new JLabel(" QV01a09 (T/m):   ");
	klabel[4] = new JLabel(" QV11a12 (T/m):   ");
	klabel[5] = new JLabel(" QH10a13 (T/m):   ");
	
	blank = new JLabel("");

	xLabel = new JLabel("  X Phase. ");
	yLabel = new JLabel("  Y Phase. ");

	setpointlabel = new JLabel("Current Setpoint: ");

	energylabel = new JLabel("Scale for Energy (GeV):");
	
	calcbutton = new JButton("Calculate Quad Strengths");
	submitbutton = new JButton(" Submit ");

	Achr = new JRadioButton("Do not allow dispersion in straight sections",true);
	NoAchr = new JRadioButton("Allow dispersion in straight sections",false);
	group = new ButtonGroup();
	group.add(Achr);
	group.add(NoAchr);
	
	for(i=0; i<=11; i++) p[i] = new JPanel();
	mainPanel = new JPanel();
	tunepoint = new JComboBox<String>(tunearray);
	increment = new JComboBox<String>(incarray);
	
	xPhase = new DecimalField(0, 3, numFor);
	xPhase.clear();
	yPhase = new DecimalField(0, 3, numFor);
	yPhase.clear();
	
	energyfield = new DecimalField(1.0, 6, numFor);
	//Declare the decimal fields, but also check for channel connection 
	//and set listener for enabling and disabling the fields. 
	for(i=0; i<=5; i++){
	    decOut[i] = new DecimalField(0,6,numFor);
	    if(doc.quad_ch[i].isConnected()==true){
		activateFieldSet(i, decOut[i], doc.quad_ch[i]);
	    }
	    if(doc.quad_ch[i].isConnected()==false){
		decOut[i].setEnabled(false);
	    }
	    doc.quad_ch[i].addChannelConnectionListener(new ConnectionListener(){
		final int j=i;
		public void connectionMade(Channel aChannel) {
		    activateFieldSet(j, decOut[j], doc.quad_ch[j]);
		}
		public void connectionDropped(Channel aChannel) {
		    decOut[j].setEnabled(false);
		    pvcounter--;
		    refreshSubmitStatus();
		}
	    });
	}  
	//Declare readback decimal fields, and make a readback listener for each field.
	for(i=0; i<=5; i++){
		decRead[i] = new DecimalField(0,6,numFor);
		if(doc.quad_ch[i].isConnected()==true){
		    decRead[i].setEnabled(true);
		    if(mademonitor[i]!=1){
			makeFieldMonitor(decRead[i], doc.quad_ch[i]);
			mademonitor[i]=1;
		    }
		}
		if(doc.quad_ch[i].isConnected()==false){
		    decRead[i].setEnabled(false);
		}
		doc.quad_ch[i].addChannelConnectionListener(new ConnectionListener(){
		    final int j=i;
		    public void connectionMade(Channel aChannel) {
			decRead[j].setEnabled(true);
			if(mademonitor[j]!=1){
			   makeFieldMonitor(decRead[j], doc.quad_ch[j]);
			   mademonitor[j]=1;
			}
		    }
		    public void connectionDropped(Channel aChannel) {
			decRead[j].setEnabled(false);
		    }
		});
	}
    }
    
    public void activateFieldSet(int i, final DecimalField dField, ChannelAgent quad_ch){
	dField.setEnabled(true);
	doc.quad_k_llimit[i]=quad_ch.getMagLowLimit();
	doc.quad_k_ulimit[i]=quad_ch.getMagUpLimit();
	pvcounter++;
	refreshSubmitStatus();
    }
    
    public void refreshSubmitStatus(){
	if(pvcounter>=6) submitbutton.setEnabled(true);
	else submitbutton.setEnabled(false);
    }
	
    public void makeFieldMonitor(final DecimalField dField, ChannelAgent quad_ch){
	quad_ch.addReadbackListener( new ReadbackListener(){ 
	    public void updateReadback(Object sender, String name, double value){
		    dField.setValue(value);
	    }
	});
    }
    

    public void setAction(){
	
	Achr.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		url = getClass().getResource("resources/Phase_623_620_Achromat.dat");
		scrollX[0].Enable();
		scrollY[0].Enable();
		callRead();
		scrollX[0].setRange(phaseMinx,phaseMaxx);
	    }
	});

	NoAchr.addActionListener(new ActionListener(){ 
	    public void actionPerformed(ActionEvent e) {
		url = getClass().getResource("resources/Phase_623_620_NoAchromat.dat");
		scrollX[0].Enable();
		scrollY[0].setValue(ydefault);
		scrollY[0].Disable();
		callRead();
		scrollX[0].setRange(phaseMinx,phaseMaxx);
	    }
	});
	
	//Action listeners for the nearest tune point
	//This will determine which phase grid is used
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
				scrollX[0].setIncrement(0.001);
				scrollY[0].setIncrement(0.001);}
			if(increment.getSelectedIndex() == 1){
				scrollX[0].setIncrement(0.005);
				scrollY[0].setIncrement(0.005);}
			if(increment.getSelectedIndex() == 2){
				scrollX[0].setIncrement(0.01);
				scrollY[0].setIncrement(0.01);}
	    }
	});
	
	//Action Listener for the calcbutton button
	//Calculates sext strengths from the grid interpolation.
	calcbutton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    x = scrollX[0].getValue()/4.0; 
		    y = scrollY[0].getValue()/4.0;
		    doc.callPhaseCalc(url,energyfield.getDoubleValue(),x,y);
		    for(i=0; i<=5; i++) decOut[i].setValue(local_k[i]);
		    doc.settingChanged(this);
		}
	    });

	//Action Listener for the submitbutton button
	//It sends the values to Channel Access
	submitbutton.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent ae) {
		    for(i=0; i<=5; i++) local_k[i]=decOut[i].getDoubleValue();
		    doc.setQuadK(local_k);
		    doc.setQuadChannelAccess();
		    doc.setPhases(x,y);
		    doc.settingChanged(this);
		    doc.settingChanged(this);
		}
	    });
	}
    
    public void setColor(){
	//this color is the same as the default color that is 
	//used by the Java classes for their scroll bars etc...
		Color color = new Color(150,150,210);
		calcbutton.setBackground(color); 
		submitbutton.setBackground(color);
    }

    public void callRead(){
	try{
	    set.readData(url);      //method throughs an exception
	}
	catch(IOException ioe){}

	//Setting limits for the scrollbars
		phaseMinx = 4*set.getMinx(); 
		phaseMaxx = 4*set.getMaxx();
		phaseMiny = 4*set.getMiny(); 
		phaseMaxy = 4*set.getMaxy();
		//System.out.println("min, max x =" + phaseMinx +  " " + phaseMaxx);
		//System.out.println("min, max x =" + phaseMiny +  " " + phaseMaxy);
    }

    public void setTips(){
	//Set all tool tip text
		tunepoint.setToolTipText("Increment setting for the x and y tunes.");
		increment.setToolTipText("Increment setting for the scrollbars.");
		p[1].setToolTipText("Sextupole set point values after interpolation.");
		p[5].setToolTipText("Sextupole read back values.");
		calcbutton.setToolTipText("Calculation based on the x and y tune");
		submitbutton.setToolTipText("Send quadrupole values to machine");
    }
  

    public void updateQuadK(Object sender, double[] k) {
	reconcileK(k);
    }
    public void updateSextK(Object sender, double[] k) {
    }
    public void reconcileK(double[] k){
        System.arraycopy(k, 0, local_k, 0, k.length);
	for(i=0; i<=5; i++) decOut[i].setValue(local_k[i]);
    }
    public void updateTunes(Object sender, double tunex, double tuney){}
    public void updateChroms(Object sender, double chromx, double chromy){}
    public void updatePhases(Object sender, double phasex, double phasey){
	if(phasex != 0.0 && phasey != 0.0){
	    xPhase.setValue(4*phasex);
	    yPhase.setValue(4*phasey);
	}
	else{
	    xPhase.clear();
	    yPhase.clear();
	}
    };
}









