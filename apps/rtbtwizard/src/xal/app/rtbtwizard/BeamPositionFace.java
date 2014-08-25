/*
 * BeamPositionFace.java
 *
 */
package xal.app.rtbtwizard;

import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.io.*;

import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import xal.extension.application.*;

import java.util.Timer.*;

import xal.tools.data.*;
import xal.extension.application.smf.*;
import xal.smf.data.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.proxy.ElectromagnetPropertyAccessor;
import xal.model.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
//import xal.tools.optimizer.*;
import xal.tools.beam.Twiss;
import xal.extension.widgets.plot.*;

import java.text.NumberFormat;

import xal.extension.widgets.swing.DecimalField;
import xal.tools.apputils.EdgeLayout;
import xal.tools.data.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.tools.beam.*;
import xal.tools.statistics.*;

import java.text.DecimalFormat;

import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.extension.widgets.apputils.SimpleProbeEditor;
// TODO CKA - OVER HALF THE IMPORTS ARE NEVER USED

/**
 * Performs matching to find steerer strengths for desired injection
 * spot position and angle on the foil.
 * @author  cp3
 */

public class BeamPositionFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    private AcceleratorSeq accSeq;
    private Scenario scenario;
    private TransferMapProbe probe;
	private ParticleProbe particleProbe;
    private JButton probeeditbutton;
    // private ParameterProxy xp0;
    
    private Accelerator accl = new Accelerator();
    private DecimalField bpm27x;
    private DecimalField bpm27y;
    private DecimalField bpm29x;
    private DecimalField bpm29y;
    private DecimalField bpm30x;
    private DecimalField bpm30y;
    private NumberFormat numFor;
    private JPanel resultsPanel;
    private JPanel initPanel;
    private FunctionGraphsJPanel plotpanel;
    private JButton solvebutton;
    private JButton dumpbutton;
    private JButton averagebutton;
    public JPanel mainPanel;
    public JTable resultstable;
    private volatile boolean collectflag;
    public java.util.Timer timer;
    private JFileChooser fc;         // TODO CKA - NEVER USED
    protected Date startTime;
    
    JScrollPane resultsscrollpane;
    ResultsTableModel resultstablemodel;
    EdgeLayout layout = new EdgeLayout();
    JLabel blanklabel = new JLabel("");
    JLabel xlabel = new JLabel("X(mm)");
    JLabel ylabel = new JLabel("Y(mm)");
    JLabel bpm27label = new JLabel("BPM27: ");
    JLabel bpm29label = new JLabel("BPM29: ");
    JLabel bpm30label = new JLabel("BPM30: ");
    
    Object[][] tabledata = new Object[3][3];
    double[] bpm27data = new double[2];
    double[] bpm30data = new double[2];
    double[] bpm27_30data = new double[2];
    GridLimits limits = new GridLimits();
    JComboBox<String> syncstate;
    JComboBox<String> bcm1box;
    String latticestate = "Live";
    String[] syncstates = {"Model Live Lattice", "Model PV Logger Lattice"};
    String[] BCMs = {"RTBT_Diag:BCM25"};
    String bcm1name;
    String bpm27name = new String("RTBT_Diag:BPM27");
    String bpm29name = new String("RTBT_Diag:BPM29");
    String bpm30name = new String("RTBT_Diag:BPM30");
    JTextField pvloggerfield;
    JLabel usepvlabel;
    JLabel bpmchoicelabel;
    JLabel resultslabel;
    Integer pvloggerid = new Integer(0);
    public HashMap<String, MutableUnivariateStatistics> bpmxaverages = new HashMap<String, MutableUnivariateStatistics>();
    public HashMap<String, MutableUnivariateStatistics> bpmyaverages = new HashMap<String, MutableUnivariateStatistics>();
    
    ArrayList<BpmAgent> agents;
    
    double bpm29xoffset = 5.38;
    double currentenergy = 1e9;
    double bpm27_x;
    double bpm27_y;
    double bpm29_x;
    double bpm29_y;
    double bpm30_x;
    double bpm30_y;
    double xo=0;
    double yo=0;
    double xop=0;
    double yop=0;
	
    GenDocument doc;
    
    public BeamPositionFace(GenDocument aDocument, JPanel mainpanel) {
        doc=aDocument;
        agents=doc.bpmagents;
        setPreferredSize(new Dimension(950,600));
        setLayout(layout);
        init();
        setAction();
        addcomponents();
    }
    
    public void addcomponents(){
        
        layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
        this.add(mainPanel);
        
        EdgeLayout newlayout = new EdgeLayout();
        mainPanel.setLayout(newlayout);
        GridLayout initgrid = new GridLayout(4, 3);
        initPanel.setLayout(initgrid);
        initPanel.add(blanklabel); initPanel.add(xlabel); initPanel.add(ylabel);
        initPanel.add(bpm27label); initPanel.add(bpm27x); initPanel.add(bpm27y);
        initPanel.add(bpm29label); initPanel.add(bpm29x); initPanel.add(bpm29y);
        initPanel.add(bpm30label); initPanel.add(bpm30x); initPanel.add(bpm30y);
        
        newlayout.setConstraints(syncstate, 20, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(syncstate);
        newlayout.setConstraints(usepvlabel, 60, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(usepvlabel);
        newlayout.setConstraints(pvloggerfield, 75, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(pvloggerfield);
        newlayout.setConstraints(probeeditbutton, 105, 20, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(probeeditbutton);
        
        newlayout.setConstraints(bpmchoicelabel, 10, 250, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        //mainPanel.add(bpmchoicelabel);
        //newlayout.setConstraints(bcm1box, 30, 250, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        //mainPanel.add(bcm1box);
        newlayout.setConstraints(averagebutton, 75, 250, 20, 5, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(averagebutton);
        newlayout.setConstraints(dumpbutton,110, 250, 20, 5, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(dumpbutton);
        
        newlayout.setConstraints(initPanel, 10, 480, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(initPanel);
        newlayout.setConstraints(solvebutton, 130, 580, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solvebutton);
        
        newlayout.setConstraints(resultslabel, 210, 250, 20, 5, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(resultslabel);
        newlayout.setConstraints(resultsscrollpane, 235, 250, 20, 5, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(resultsscrollpane);
        //newlayout.setConstraints(plotpanel, 270, 70, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        //mainPanel.add(plotpanel);
        
    }
    
    
    public void init(){
        
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(950,570));
        
        resultsPanel = new JPanel();
        resultsPanel.setPreferredSize(new Dimension(260,110));
        resultsPanel.setBorder(BorderFactory.createTitledBorder("Results"));
        
        initPanel = new JPanel();
        initPanel.setPreferredSize(new Dimension(260,110));
        initPanel.setBorder(BorderFactory.createTitledBorder("Initial BPM Values"));
        
        plotpanel = new FunctionGraphsJPanel();
        plotpanel.setPreferredSize(new Dimension(800, 250));
        plotpanel.setGraphBackGroundColor(Color.WHITE);
        
        solvebutton = new JButton("Solve");
        
        numFor = NumberFormat.getNumberInstance();
        numFor.setMinimumFractionDigits(4);
        bpm27x= new DecimalField(0.0, 4, numFor);
        bpm27y= new DecimalField(0.0, 4, numFor);
        bpm29x= new DecimalField(0.0, 4, numFor);
        bpm29y= new DecimalField(0.0, 4, numFor);
        bpm30x= new DecimalField(0.0, 4, numFor);
        bpm30y= new DecimalField(0.0, 4, numFor);
        
        usepvlabel = new JLabel("PV Logger ID: ");
        bpmchoicelabel = new JLabel("Choose Validator BCM: ");
        resultslabel = new JLabel("Results: ");
        pvloggerfield = new JTextField(8);
        
        syncstate = new JComboBox<String>(syncstates);
        probeeditbutton = new JButton("Edit Model Probe");
        averagebutton = new JButton("Begin Averaging BPMs");
        dumpbutton = new JButton("Write Averages to File");
        bcm1box = new JComboBox<String>(BCMs);
        //bpm2box = new JComboBox(BPMs);
        bcm1box.setSelectedIndex(0);
        //bpm2box.setSelectedIndex(7);
        bcm1name = new String(BCMs[0]);
        //bpm2name = new String(BPMs[7]);
        
        makeResultsTable();
        
        //Initialize results table.
        
        tabledata[0][0] = new String("BPMs 27 and 29");
        tabledata[1][0] = new String("BPMs 29 and 30");
        tabledata[2][0] = new String("BPMs 27 and 30");
        resultstablemodel.setTableData(tabledata);
        
        accl = doc.getAccelerator();
        AcceleratorSeq rtbt1=accl.getSequence("RTBT1");
        AcceleratorSeq rtbt2=accl.getSequence("RTBT2");
        ArrayList<AcceleratorSeq> lst = new ArrayList<AcceleratorSeq>();
        lst.add(rtbt1);
        lst.add(rtbt2);
        
        accSeq = new AcceleratorSeqCombo("RTBT", lst);
        try{
            scenario = Scenario.newScenarioFor( accSeq );

            TransferMapTracker tracker = AlgorithmFactory.createTransferMapTracker(accSeq);
            probe=ProbeFactory.getTransferMapProbe(accSeq, tracker);

			final ParticleTracker particleTracker = AlgorithmFactory.createParticleTracker( accSeq );
			particleProbe = ProbeFactory.createParticleProbe( accSeq, particleTracker );

            currentenergy=probe.getKineticEnergy();
            scenario.setProbe(probe);
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        //benchmark();
        pvloggerfield.setText("");
        
        Iterator<BpmAgent> itr = agents.iterator();
        while(itr.hasNext()){
            BpmAgent bpmagent = itr.next();
            MutableUnivariateStatistics xstats = new MutableUnivariateStatistics();
            MutableUnivariateStatistics ystats = new MutableUnivariateStatistics();
            bpmxaverages.put(bpmagent.name(), xstats);
            (bpmxaverages.get(bpmagent.name())).addSample(0.0);
            bpmyaverages.put(bpmagent.name(), ystats);
            (bpmyaverages.get(bpmagent.name())).addSample(0.0);
        }
        
        collectflag=false;
        timer = new java.util.Timer();
        
        timer.schedule(new TimerTask(){
            public void run(){
                TaskList();
            }
        }, 1000, 1000);
        
    }
    
    public void TaskList(){
        
        if(collectflag==true){
            CollectAverages();
        }
    }
    
    public void CollectAverages(){
        
        //System.out.println("Averaging BPMs...");
        Iterator<BpmAgent> itr = (agents).iterator();
        while(itr.hasNext()){
            BpmAgent bpmagent = itr.next();
            if(bpmagent.isConnected()){
                (bpmxaverages.get(bpmagent.name())).addSample(bpmagent.getXAvg());
                (bpmyaverages.get(bpmagent.name())).addSample(bpmagent.getYAvg());
            }
        }
        bpm27x.setValue((bpmxaverages.get(bpm27name)).mean());
        bpm29x.setValue((bpmxaverages.get(bpm29name)).mean());
        bpm30x.setValue((bpmxaverages.get(bpm30name)).mean());
        bpm27y.setValue((bpmyaverages.get(bpm27name)).mean());
        bpm29y.setValue((bpmyaverages.get(bpm29name)).mean());
        bpm30y.setValue((bpmyaverages.get(bpm30name)).mean());
        
    }
    
    
    
    public void setAction(){
        
        solvebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                solvesystem();
            }
        });
        
        syncstate.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(syncstate.getSelectedIndex() == 0){
                    latticestate="Live";
                }
                if(syncstate.getSelectedIndex() == 1){
                    latticestate="PVLogger";
                }
            }
        });
        
        bcm1box.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int index = bcm1box.getSelectedIndex();
                bcm1name=BCMs[index];
                System.out.println("BCM 1 is: " + bcm1name);
            }
        });
        
        
        probeeditbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                SimpleProbeEditor spe = new SimpleProbeEditor( doc.myWindow(), scenario.getProbe() );
                scenario.setProbe(spe.getProbe());
                currentenergy=probe.getKineticEnergy();
				particleProbe.setKineticEnergy( currentenergy );
            }
        });
        
        averagebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                if(collectflag==true){
                    collectflag=false;
                    averagebutton.setText("Begin Averaging");
                    //backgroundlabel.repaint();
                    averagebutton.setSelected(false);
                }
                else{
                    //First clear out old background values.
                    Iterator itr = agents.iterator();   // CKA - Raw Type
                    while(itr.hasNext()){
                        BpmAgent bpmagent = (BpmAgent)itr.next();
                        (bpmxaverages.get(bpmagent.name())).clear();
                        (bpmyaverages.get(bpmagent.name())).clear();
                    }
                    averagebutton.setText("Stop Averaging");
                    collectflag=true;
                    averagebutton.setSelected(true);
                }
            }
        });
        
        
        dumpbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae) {
                /*int returnValue = fc.showSaveDialog(BeamPositionFace.this);
                 if(returnValue == fc.APPROVE_OPTION){
                 File file = fc.getSelectedFile();
                 try{
                 writeFile(file);
                 }
                 catch(IOException ioe){
                 }
                 
                 }
                 else{
                 System.out.println("Export command canceled by user.");
                 }
                 */
                
                startTime = new Date();
                SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd");
                String datePart = df.format(startTime);
                datePart = datePart.replaceAll(" ", "");
                datePart = datePart.replace('/', '.');
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                String timePart = sdf.format(startTime);
                timePart = timePart.replaceAll(" ", "");
                timePart = timePart.replace(':', '.');
                String fileString = datePart + "." + timePart + ".txt";
                File filePath = Application.getApp().getDefaultDocumentFolder();
                File file = new File(filePath, fileString);
                JPanel dummypanel = new JPanel();
                BeamPositionFace face = new BeamPositionFace(doc, dummypanel);   // TODO CKA - NEVER USED
                try{
                    (BeamPositionFace.this).writeFile(file);
                    System.out.println("done writing " + file + " to " + filePath);
                    //exportlabel.setText("Exported BPMs to file " + fileString);
                }
                catch(IOException ioe){}
            }
            
        });
    }
    
    public void solvesystem(){
        
        bpm27data=solve(bpm27name, bpm29name);
        bpm30data=solve(bpm29name, bpm30name);
        bpm27_30data=solve(bpm27name, bpm30name);
        
        double bpm30xcorrection = -10.0;
        double bpm27ycorrection = 0.0;
        double bpm27xcorrection = -3.6;
        //This one has a significant slope w.r.t initial beam trajectory
        double bpm30ycorrection = 0.0;
        bpm30ycorrection = bpm30y.getDoubleValue()*0.463 - 7.588;
        double bpm27_30xcorrection = -6.6;
        double bpm27_30ycorrection = -3.3;
        
        double bpm27xresult = bpm27data[0] + bpm27xcorrection;
        double bpm30xresult = bpm30data[0] + bpm30xcorrection;
        double bpm27_30xresult = bpm27_30data[0] + bpm27_30xcorrection;
        double bpm27yresult = bpm27data[1] + bpm27ycorrection;
        double bpm30yresult = bpm30data[1] + bpm30ycorrection;
        double bpm27_30yresult = bpm27_30data[1] + bpm27_30ycorrection;
        
        DecimalFormat decfor =  new DecimalFormat("###.000");
        
        tabledata[0][1] = new Double(decfor.format(bpm27xresult));
        tabledata[1][1] = new Double(decfor.format(bpm30xresult));
        tabledata[2][1] = new Double(decfor.format(bpm27_30xresult));
        tabledata[0][2] = new Double(decfor.format(bpm27yresult));
        tabledata[1][2] = new Double(decfor.format(bpm30yresult));
        tabledata[2][2] = new Double(decfor.format(bpm27_30yresult));
        
        resultstablemodel.setTableData(tabledata);
        
        System.out.println("*-------Results Summary------------*");
        System.out.println("Horizontal: BPM 27/29 Raw/Corrected = " + bpm27data[0] + " / " + bpm27xresult);
        System.out.println("Horizontal: BPM 29/30 Raw/Corrected = " + bpm30data[0] + " / " + bpm30xresult);
        System.out.println("Horizontal: BPM 27/30 Raw/Corrected = " + bpm27_30data[0] + " / " + bpm27_30xresult);
        System.out.println("Vertical: BPM 27/29 Raw/Corrected = " + bpm27data[1] + " / " + bpm27yresult);
        System.out.println("Vertical: BPM 29/30 Raw/Corrected = " + bpm30data[1] + " / " + bpm30yresult);
        System.out.println("Vertical: BPM 27/30 Raw/Corrected = " + bpm27_30data[1] + " / " + bpm27_30yresult);
        
        //doc.xpos = (bpm27xresult+bpm30xresult+bpm27_30xresult)/3.0;
        //doc.ypos = (bpm27yresult+bpm30yresult+bpm27_30yresult)/3.0;
        
    }
    
    public double[] solve(String bpm1name, String bpm2name) {
        
        double x=0.0; double y=0.0;
        double[] data = new double[2];
        resetprobe();

		// first set the probe and run it to get the transfer map and first order matrix
		scenario.setProbe( probe );
        
        System.out.println("energy is = " + probe.getKineticEnergy());
        try {
            if(latticestate.equals("Live"))
                scenario.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
            if(latticestate.equals("PVLogger")){
                String id = pvloggerfield.getText();
                pvloggerid = new Integer(Integer.parseInt(id));
                scenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
                if(pvloggerid !=0){
                    PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
                    scenario = plds.setModelSource(accSeq, scenario);
                }
                else{
                    System.out.println("No PV Logger ID Found!");
                }
            }
            
            System.out.println("Using " + bpm1name + " " + bpm2name);
            scenario.setStartElementId(bpm1name);
            scenario.setStopElementId(bpm2name);
            scenario.resync();
            scenario.run();
            
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        /*
         Trajectory trajectory = (Trajectory)probe.getTrajectory();
         ProbeState[] states = trajectory.statesInPositionRange(0.0, 100.0);
         for(ProbeState state : states){
         System.out.println(state.getElementId() + " = " + state.getPosition());
         }
         TransferMapState state = (TransferMapState)states[13];
         
         PhaseMap map = state.getTransferMap();
         */
        
        PhaseMap map = probe.getTransferMap();
        PhaseMatrix mat = map.getFirstOrder();
        //mat.print();
        
        if(bpm1name.equals("RTBT_Diag:BPM27")){
            xo=bpm27x.getDoubleValue()/1000.0;
            yo=bpm27y.getDoubleValue()/1000.0;
            xop=0;
            yop=0;
            System.out.println("Choosing 27 for first");
        }
        else{
            xo=(bpm29x.getDoubleValue() - bpm29xoffset)/1000.0;
            yo=bpm29y.getDoubleValue()/1000.0;
            xop=0;
            yop=0;
            System.out.println("Choosing 29 for first");
        }
        
        if(bpm2name.equals("RTBT_Diag:BPM29")){
            x=(bpm29x.getDoubleValue() - bpm29xoffset)/1000.0;
            y=bpm29y.getDoubleValue()/1000.0;
            System.out.println("Choosing 29 for second");
        }
        else{
            x=bpm30x.getDoubleValue()/1000.0;
            y=bpm30y.getDoubleValue()/1000.0;
            System.out.println("Choosing 30 for second");
        }
        
        xop = (x - xo*mat.getElem(0,0) - mat.getElem(0,6))/mat.getElem(0,1);
        yop = (y - yo*mat.getElem(2,2) - mat.getElem(2,6))/mat.getElem(2,3);


        // now run the scenario using the particle probe
		scenario.setProbe( particleProbe );
        scenario.unsetStopNode();
        resetprobe();
        scenario.setStartElementId(bpm1name);
        particleProbe.setPhaseCoordinates( new PhaseVector( xo, xop, yo, yop, 0.0, 0.0 ) );
        
        try{
            scenario.resync();
            scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        
        final Trajectory<?> particleTrajectory = scenario.getTrajectory();
        final ParticleProbeState windowstate = (ParticleProbeState)particleTrajectory.stateForElement("RTBT_Vac:VIW");
        final ParticleProbeState targetstate = (ParticleProbeState)particleTrajectory.stateForElement("RTBT:Tgt");
        PhaseVector windowcoords = windowstate.getPhaseCoordinates();    // TODO CKA - NEVER USED
        PhaseVector targetcoords = targetstate.getPhaseCoordinates();

        //System.out.println("bpm 1 " + bpm1coords.getx() +  " " +bpm1coords.gety());
        //System.out.println("bpm 2 " + bpm2coords.getx() +  " " +bpm2coords.gety());
        //System.out.println("Window " + windowcoords.getx() +  " " +windowcoords.gety());
        System.out.println("Target " + 1000.0*targetcoords.getx() +  " " + 1000.0*targetcoords.gety());
        
        DecimalFormat decfor =  new DecimalFormat("###.000");    // TODO CKA - NEVER USED
        
        data[0] = 1000*targetcoords.getx();
        data[1] = 1000*targetcoords.gety();
        
        
        return data;
    }
    
    
    
    public void makeResultsTable(){
        
        String[] colnames = {"BPM Pair Used", "Horizontal Position (mm)", "Vertical Position (mm)"};
        
        resultstablemodel = new ResultsTableModel(colnames,3);
        
        resultstable = new JTable(resultstablemodel);
        resultstable.getColumnModel().getColumn(0).setMinWidth(125);
        resultstable.getColumnModel().getColumn(1).setMinWidth(160);
        resultstable.getColumnModel().getColumn(2).setMinWidth(162);
        
        resultstable.setPreferredScrollableViewportSize(resultstable.getPreferredSize());
        resultstable.setRowSelectionAllowed(false);
        resultstable.setColumnSelectionAllowed(false);
        resultstable.setCellSelectionEnabled(true);
        
        resultsscrollpane = new JScrollPane(resultstable,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        resultsscrollpane.setColumnHeaderView(resultstable.getTableHeader());
        resultstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        resultsscrollpane.setPreferredSize(new Dimension(450, 65));
        
    }
    
	
    public void resetPlot(){
        
        plotpanel.removeAllGraphData();
        BasicGraphData hgraphdata = new BasicGraphData();
        BasicGraphData vgraphdata = new BasicGraphData();	
        
        ArrayList<Double> sdata = new ArrayList<Double>();
        ArrayList<Double> hdata = new ArrayList<Double>();
        ArrayList<Double> vdata = new ArrayList<Double>();
        
        resetprobe();
		scenario.setProbe( particleProbe );
        scenario.setStartElementId(bpm27name);
        particleProbe.setPhaseCoordinates(new PhaseVector(xo, xop, yo, yop, 0.0, 0.0) );
        try{
            scenario.resync();
            scenario.run();
        }
        catch(Exception exception){
            exception.printStackTrace();
        }
        
        Trajectory<TransferMapState> traj= probe.getTrajectory();
        Iterator<?> iterState= traj.stateIterator();
        
        while(iterState.hasNext()){
            ParticleProbeState state= (ParticleProbeState)iterState.next();
            sdata.add(state.getPosition());
            PhaseVector coords=state.getPhaseCoordinates();
            hdata.add(coords.getx());
            vdata.add(coords.gety());
        }
        
        int size = sdata.size() - 1;
        
        double[] s = new double[size];
        double[] x = new double[size];
        double[] y = new double[size];
        for(int i=0; i<size; i++){
            s[i]=(sdata.get(i)).doubleValue();
            x[i]=(hdata.get(i)).doubleValue();
            y[i]=(vdata.get(i)).doubleValue();
        }
        
        hgraphdata.addPoint(s, x);
        hgraphdata.setDrawPointsOn(true);
        hgraphdata.setDrawLinesOn(true);
        hgraphdata.setGraphProperty("Legend", new String("Horizontal"));
        hgraphdata.setGraphColor(Color.RED);
        vgraphdata.addPoint(s, y);
        vgraphdata.setDrawPointsOn(true);
        vgraphdata.setDrawLinesOn(true);
        vgraphdata.setGraphProperty("Legend", new String("Vertical"));
        vgraphdata.setGraphColor(Color.BLUE);
        limits.setSmartLimits();
        //limits.setXmax(14.0);
        plotpanel.setExternalGL(limits);
        
        
        plotpanel.addGraphData(hgraphdata);
        plotpanel.addGraphData(vgraphdata);
    }	
    
    void resetprobe(){
        probe.reset();
        probe.setKineticEnergy( currentenergy );
		particleProbe.reset();
		particleProbe.setKineticEnergy( currentenergy );
    }
    
    void writeFile(File file) throws IOException{
        OutputStream fout = new FileOutputStream(file);
        String line;
        Iterator<BpmAgent> itr = (doc.bpmagents).iterator();
        while(itr.hasNext()){
            BpmAgent bpmagent = itr.next();  
            //System.out.println("avge is " + ((MutableUnivariateStatistics)bpmxaverages.get(bpmagent.name())).mean());
            double xavg=(bpmxaverages.get(bpmagent.name())).mean();
            double yavg=(bpmyaverages.get(bpmagent.name())).mean();
            //System.out.println("avg is " + xavg + " " + yavg);
            line = bpmagent.name() + "\t" +  xavg + "\t" + yavg + "\n";
            byte buf[] = line.getBytes();
            fout.write(buf);
        }
        fout.close();
    }
}



