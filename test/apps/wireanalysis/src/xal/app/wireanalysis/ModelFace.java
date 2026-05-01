/*************************************************************
 //
 // class ModelFace:
 // This class is responsible for the Graphic User Interface
 // components and action listeners.
 //
 /*************************************************************/

package xal.app.wireanalysis;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.table.*;

import java.awt.*;

import javax.swing.*;

import java.awt.event.*;
import java.util.*;
import java.util.Timer.*;
import java.net.URL;
import java.util.List;
import java.io.*;
import java.lang.*;

import xal.extension.widgets.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.extension.widgets.plot.*;
import xal.tools.data.*;

import java.text.NumberFormat;

import xal.tools.messaging.*;
import xal.ca.*;

import java.text.NumberFormat;
import java.text.DecimalFormat;

import xal.tools.apputils.NonConsecutiveSeqSelector;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.tools.xml.XmlDataAdaptor;
import xal.sim.scenario.Scenario;
import xal.sim.sync.SynchronizationException;
import xal.tools.beam.Twiss;
import xal.smf.*;
import xal.extension.widgets.apputils.SimpleProbeEditor;
import xal.model.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.model.xml.*;
import xal.model.alg.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
import xal.smf.data.*;
import xal.extension.solver.*;
//import xal.tools.formula.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import xal.tools.beam.*;
import xal.extension.application.smf.*;
import xal.extension.application.*;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.tools.beam.CovarianceMatrix;
//TODO: CKA - OVER HALF THE IMPORTS ARE NEVER USED

public class ModelFace extends JPanel{
    
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    public JTable storedtable;
    
    public AcceleratorSeqCombo seq;
    private Accelerator accl; //= new Accelerator();
    private EnvelopeProbe solverprobe;
	private EnvelopeProbe initprobe;
    private Scenario scenario;
	private Scenario solvermodel;
    private Trajectory<EnvelopeProbeState> traj;
    private ArrayList<AcceleratorSeq> sectionnames = new ArrayList<AcceleratorSeq>();   // TODO: CKA - NEVER USED
    private ArrayList<AcceleratorSeq> seqlist = new ArrayList<AcceleratorSeq>();
    private ArrayList<AcceleratorNode> nodes = new ArrayList<AcceleratorNode>();
    private AcceleratorSeqCombo solveforseq;
    
    private HashMap<String, Integer> pvloggermap;
    private DataTable edmresultsdatatable;
    private DataTable fitresultsdatatable;
    GenDocument doc;
    
    private JButton plotbutton;
    private JButton solvebutton;
    private JButton loadbutton;
    private JButton probeeditbutton;
    private JButton pvloggerbutton;     // TODO: CKA - NEVER USED
    private JButton store;
    private JButton average;
	private JButton clearbutton;
    private JButton setlimits;
    private JTextField pvloggerfield;
	private JTextField xchisquarefield;
	private JTextField ychisquarefield;
    private JButton selectSequence;
    private JButton singlepassbutton;
    private JComboBox<String> elementList;
    private NumberFormat numfor;
    
    private String[] initstate = {"No PV Logger Files"};
    private JComboBox<String> machinestatechooser = new JComboBox<String>(initstate);
    private String[] rms = { "Use EDM rms values", "Use user rms values"};
    private JComboBox<String> rmschooser = new JComboBox<String>(rms);
    private String[] graphs = {"Plot x and y Graphs", "Plot x Graphs", "Plot y Graphs", "Plot Alphax vs. Betax", "Plot Alphay vs. Betay", "Plot Alpha vs. Beta"};
    private JComboBox<String> graphChooser = new JComboBox<String>(graphs);
    private ArrayList<String> filesloaded;
    ArrayList<Object> seqlistnames = new ArrayList<Object>();
    
    private String lastusedelement;     // TODO: CKA - NEVER USED
    private JLabel machinestatelabel;
    private JLabel usepvlabel;
    private JLabel errorlabel;
	private JLabel chisquarelabel;
	private JLabel xlabel;
	private JLabel ylabel;
    private String selectedfile;
    private Integer pvloggerid;
    private JLabel timelabel;
    private JTextField timefield;
    
    private boolean useMEBT = false;
    private boolean useDTL = false;
    private boolean useCCL = false;
    private boolean useSCL = false;
    private boolean useHEBT = false;
    private boolean useRTBT = false;
    
    private boolean plotable = false;       // TODO: CKA - NEVER USED
    private boolean edmrms = true;          // TODO: CKA - NEVER USED
    private boolean userrms = true;         // TODO: CKA - NEVER USED
    private boolean elementchanged = true;
    private boolean probeedited = false;    // TODO: CKA - NEVER USED
    
    private JButton exportbutton;
    private JFileChooser fc;
    
    ArrayList<String> fullWirenamelist = new ArrayList<String>();
    ArrayList<String> fullFilenamelist = new ArrayList<String>();
    ArrayList<Double> xUserfulldatalist = new ArrayList<Double>();
    ArrayList<Double> yUserfulldatalist = new ArrayList<Double>();
    ArrayList<Double> xEdmfulldatalist = new ArrayList<Double>();
    ArrayList<Double> yEdmfulldatalist = new ArrayList<Double>();
    ArrayList<String> Wirenamelist = new ArrayList<String>();
    ArrayList<String> Filenamelist = new ArrayList<String>();
    ArrayList<Double> xUserdatalist = new ArrayList<Double>();
    ArrayList<Double> yUserdatalist = new ArrayList<Double>();
    ArrayList<Double> xEdmdatalist = new ArrayList<Double>();
    ArrayList<Double> yEdmdatalist = new ArrayList<Double>();
    //ArrayList<Object> masternamelist = new ArrayList<Object>();
    ArrayList<BasicGraphData> xGraphdata = new ArrayList<BasicGraphData>();
    ArrayList<BasicGraphData> yGraphdata = new ArrayList<BasicGraphData>();
    ArrayList<BasicGraphData> hGraphdata = new ArrayList<BasicGraphData>();
    ArrayList<BasicGraphData> vGraphdata = new ArrayList<BasicGraphData>();
    BasicGraphData xGraph = new BasicGraphData();
    BasicGraphData yGraph = new BasicGraphData();
    BasicGraphData hGraph = new BasicGraphData();
    BasicGraphData vGraph = new BasicGraphData();
    ResultsTableModel twisstablemodel;
    public JTable resultsTable;
    private JPanel resultsPanel;
    public JTable twisstable;
    JScrollPane twissscrollpane;
    
    double currentenergy = 1e9;
    double currentI = 0.0;
    double currentcharge = 0.0;
    double alphax0, betax0;
    double alphay0, betay0;
    double emitx0, emity0, emitz0;
    double alphaz0, betaz0;
    double alphaXmax;
    double alphaYmax;
    double betaXmax;
    double betaYmax;
    double emitXmax;
    double emitYmax;
    double alphaXmin;
    double alphaYmin;
    double betaXmin;
    double betaYmin;
    double emitXmin;
    double emitYmin;
    boolean limitsWereSet = false;
    boolean firsttime = true;
	boolean modelinitialized = false;
    double[] currenttwiss = new double[6];
    JScrollPane datascrollpane;
    JScrollPane storedscrollpane;
    DataTableModel datatablemodel;
    DataTableModel storedmodel;
    JPanel plotpanel;
    FunctionGraphsJPanel modelplot;
    SimpleChartPopupMenu popupMenu;
    GridLimits limits = new GridLimits();
    
    
    //Member function Constructor
    public ModelFace(GenDocument aDocument){
		
		doc=aDocument;
		accl = doc.accl;
		edmresultsdatatable = doc.masterdatatable;
		fitresultsdatatable = doc.resultsdatatable;
		pvloggermap = doc.masterpvloggermap;
        
		double alphax=currenttwiss[0]; double betax=currenttwiss[1];      // TODO: CKA - NEVER USED
		double alphay=currenttwiss[3]; double betay=currenttwiss[4];      // TODO: CKA - NEVER USED
        
		makeComponents(); //Creation of all GUI components
		addComponents();  //Add all components to the layout and panels
        
		setAction();      //Set the action listeners
    }
    
    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		
		layout.add(datascrollpane,mainPanel, 230, 0, EdgeLayout.LEFT);
		layout.add(storedscrollpane, mainPanel, 2, 255, EdgeLayout.LEFT);
		
		resultsPanel.add(twissscrollpane);
		plotpanel.add(modelplot);
        
		layout.setConstraints(resultsPanel, 120, 450, 10, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        
		mainPanel.add(resultsPanel);
        
		layout.add(loadbutton, mainPanel, 2, 10, EdgeLayout.LEFT);
		layout.add(machinestatelabel, mainPanel, 2, 50, EdgeLayout.LEFT);
		layout.add(machinestatechooser, mainPanel, 2, 70, EdgeLayout.LEFT);
		layout.add(usepvlabel, mainPanel, 2, 100, EdgeLayout.LEFT);
		layout.add(pvloggerfield, mainPanel, 2, 120, EdgeLayout.LEFT);
		layout.add(selectSequence, mainPanel, 2, 150, EdgeLayout.LEFT);
		layout.add(elementList, mainPanel, 2, 180, EdgeLayout.LEFT);
		layout.add(setlimits, mainPanel, 0, 215, EdgeLayout.LEFT);
		layout.add(chisquarelabel,mainPanel, 810,120, EdgeLayout.LEFT);
		layout.add(xlabel, mainPanel, 810, 145, EdgeLayout.LEFT);
		layout.add(ylabel, mainPanel, 810, 175, EdgeLayout.LEFT);
		layout.add(xchisquarefield, mainPanel, 840, 140, EdgeLayout.LEFT);
		layout.add(ychisquarefield, mainPanel, 840, 170, EdgeLayout.LEFT);
		
		layout.add(rmschooser, mainPanel, 230, 105, EdgeLayout.LEFT);
		layout.add(singlepassbutton, mainPanel, 230, 145, EdgeLayout.LEFT);
		layout.add(timelabel, mainPanel, 230,185, EdgeLayout.LEFT);
		layout.add(timefield, mainPanel, 320, 185, EdgeLayout.LEFT);
		layout.add(solvebutton, mainPanel, 230, 215, EdgeLayout.LEFT);
		layout.add(probeeditbutton, mainPanel, 305, 215, EdgeLayout.LEFT);
		layout.add(store, mainPanel, 410, 215, EdgeLayout.LEFT);
		
		layout.add(average, mainPanel, 810, 245, EdgeLayout.LEFT);
		layout.add(plotbutton, mainPanel, 810, 275, EdgeLayout.LEFT);
		layout.add(graphChooser, mainPanel, 810, 305, EdgeLayout.LEFT);
		layout.add(clearbutton, mainPanel, 810, 330, EdgeLayout.LEFT);
		layout.add(plotpanel, mainPanel, 2, 380, EdgeLayout.LEFT);
		layout.add(errorlabel, mainPanel, 2, 680, EdgeLayout.LEFT);
		layout.add(exportbutton, mainPanel, 680, 650, EdgeLayout.LEFT);
		this.add(mainPanel);
    }
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(1000, 750));
		
		pvloggermap = new HashMap<String, Integer>();
        
		resultsPanel = new JPanel();
		resultsPanel.setPreferredSize(new Dimension(350,85));
        
		modelplot = new FunctionGraphsJPanel();
		modelplot.setPreferredSize(new Dimension(850, 220));
		modelplot.setGraphBackGroundColor(Color.WHITE);
		plotpanel = new JPanel();
		plotpanel.setPreferredSize(new Dimension(880, 250));
		plotpanel.setBorder(BorderFactory.createTitledBorder("Raw Data Display"));
		plotpanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
		machinestatelabel = new JLabel("Take machine state from file: ");
		usepvlabel = new JLabel("or, manually enter PV logger id: ");
		errorlabel = new JLabel("Errorbar");
		chisquarelabel = new JLabel("Sqrt(Chi^2) Fit Error");
		xlabel = new JLabel("X: ");
		ylabel = new JLabel("Y: ");
		selectedfile = new String("None");
		pvloggerid = new Integer(0);
		setlimits = new JButton("Set Init Twiss and Limits");
        
		numfor = NumberFormat.getNumberInstance();
		numfor.setMinimumFractionDigits(2);
		numfor.setMaximumFractionDigits(3);
		makeDataTable();
        
		plotbutton = new JButton("Plot Selected");
		loadbutton = new JButton("Load Table");
		average = new JButton("Average Selected");
		pvloggerbutton = new JButton("PV Logger Button");
		singlepassbutton = new JButton("Single Pass");
		probeeditbutton = new JButton("Edit Probe");
		probeeditbutton.setEnabled(false);
		solvebutton = new JButton("Solve");
		exportbutton = new JButton("Export XY Plot Data");
		store = new JButton("Store");
		clearbutton = new JButton("Clear Table");
		machinestatechooser.setEnabled(false);
		rmschooser.setSelectedIndex(1);
		pvloggerfield = new JTextField();
		xchisquarefield = new JTextField();
		xchisquarefield.setPreferredSize(new Dimension(100,30));
		ychisquarefield = new JTextField();
		ychisquarefield.setPreferredSize(new Dimension(100, 30));
		pvloggerfield.setPreferredSize(new Dimension(200,30));
		filesloaded = new ArrayList<String>();
		selectSequence = new JButton("Select sequence");
		elementList = new JComboBox<String>();
		elementList.addItem("Select Element");
		timelabel = new JLabel("Solver Time:");
		timefield = new JTextField(4);
		timefield.setText("20");
		makeTwissTable();
		twisstablemodel.setValueAt(new String("Alpha"),0,0);
		twisstablemodel.setValueAt(new String("Beta"),1,0);
		twisstablemodel.setValueAt(new String("Emit (un-norm)"),2,0);
		twisstablemodel.setValueAt(new String("Emit (norm)"),3,0);
        
		fc = new JFileChooser();
		makeStoredResultsTable();
        
    }
    
    
    public void setAction(){
        
		selectSequence.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent  ae) {
                NonConsecutiveSeqSelector selector = new NonConsecutiveSeqSelector();
                selector.selectSequence();
                seqlistnames = selector.getSeqList();
                Iterator<Object> itr = seqlistnames.iterator();
                seqlist.clear();
                nodes.clear();
                
                while(itr.hasNext()){
                    AcceleratorSeq sequence = accl.getSequence( itr.next().toString() );
                    seqlist.add(sequence);
                    nodes.addAll(sequence.getAllNodes(true));
                }
                solveforseq = new AcceleratorSeqCombo("choosen",seqlist);
                ArrayList<AcceleratorSeq> sequences =(ArrayList<AcceleratorSeq>) accl.getSequences();
                int p=sequences.indexOf(accl.getSequence((String)seqlistnames.iterator().next()));  // TODO: CKA - NEVER USED
                for(int i=0;i<nodes.size();i++)
                {
                    elementList.addItem(nodes.get(i).getId());
                }
                probeedited=false;
                modelinitialized = false;
            }
        });
        
        loadbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                edmresultsdatatable = doc.masterdatatable;
                fitresultsdatatable = doc.resultsdatatable;
                pvloggermap = doc.masterpvloggermap;
                refreshTable();
                System.out.println("About to refresh file array.");
                refreshFileArray();
                if(pvloggerid.intValue() == 0){
                    System.out.println("Error: No PV Logger ID found!");
                    machinestatechooser.setSelectedItem(new String("User Defined"));
                }
                else{
                    machinestatechooser.setEnabled(true);
                    System.out.println(" pvloggerid is = " + pvloggerid);
                    doc.currentpvloggerid = pvloggerid;
                }
			}
        });
        
        
        pvloggerfield.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                if(selectedfile.equals(new String("User Defined")) && (pvloggerfield.getText() != null)){
                    String id = pvloggerfield.getText();
                    pvloggerid = new Integer(Integer.parseInt(id));
                    doc.currentpvloggerid = pvloggerid;
                    doc.masterpvloggermap.put((String)datatablemodel.getValueAt(0,0), pvloggerid);
                    System.out.println("User defined pvlog id is = " + pvloggerid);
                    if(edmresultsdatatable.records().size() != 0){
                        probeeditbutton.setEnabled(true);
                    }
                }
            }
        });
        
        
        elementList.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                elementchanged=true;
                modelinitialized=false;
                probeeditbutton.setEnabled(false);
                System.out.println("Changing to new element :" + (String)elementList.getSelectedItem());
                errorlabel.setText("WARNING: Model probe set to default values when element is changed! ");
			}
        });
        
	    
        average.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                double axsum = 0;
                double aysum = 0;
                double bxsum = 0;
                double bysum = 0;
                double exsum = 0;
                double eysum = 0;
                double count = 0;
                double axdev = 0;
                double aydev = 0;
                double bxdev = 0;
                double bydev = 0;
                double exdev = 0;
                double eydev = 0;
                for(int i=0;i<storedtable.getRowCount();i++)
                    if ((Boolean)storedtable.getValueAt(i,8)){
                        axsum += (Double) storedtable.getValueAt(i,2);
                        aysum += (Double) storedtable.getValueAt(i,3);
                        bxsum += (Double) storedtable.getValueAt(i,4);
                        bysum += (Double) storedtable.getValueAt(i,5);
                        exsum += (Double) storedtable.getValueAt(i,6);
                        eysum += (Double) storedtable.getValueAt(i,7);
                        count++;
                    }
                for (int i = 0;i<storedtable.getRowCount();i++)
                    if ((Boolean)storedtable.getValueAt(i,8)){
                        axdev += ((Double) storedtable.getValueAt(i,2)-(axsum/count))*((Double) storedtable.getValueAt(i,2)-(axsum/count));
                        aydev += ((Double) storedtable.getValueAt(i,3)-(aysum/count))*((Double) storedtable.getValueAt(i,3)-(aysum/count));
                        bxdev += ((Double) storedtable.getValueAt(i,4)-(bxsum/count))*((Double) storedtable.getValueAt(i,4)-(bxsum/count));
                        bydev += ((Double) storedtable.getValueAt(i,5)-(bysum/count))*((Double) storedtable.getValueAt(i,5)-(bysum/count));
                        exdev += ((Double) storedtable.getValueAt(i,6)-(exsum/count))*((Double) storedtable.getValueAt(i,6)-(exsum/count));
                        eydev += ((Double) storedtable.getValueAt(i,7)-(eysum/count))*((Double) storedtable.getValueAt(i,7)-(eysum/count));
                    }
                twisstablemodel.setValueAt(axsum/count, 0, 1);
                twisstablemodel.setValueAt(aysum/count, 0, 2);
                twisstablemodel.setValueAt(Math.sqrt(axdev/count), 0, 3);
                twisstablemodel.setValueAt(Math.sqrt(aydev/count), 0, 4);
                twisstablemodel.setValueAt(bxsum/count, 1, 1);
                twisstablemodel.setValueAt(bysum/count, 1, 2);
                twisstablemodel.setValueAt(Math.sqrt(bxdev/count), 1, 3);
                twisstablemodel.setValueAt(Math.sqrt(bydev/count), 1, 4);
                twisstablemodel.setValueAt(exsum/count, 2, 1);
                twisstablemodel.setValueAt(eysum/count, 2, 2);
                twisstablemodel.setValueAt(Math.sqrt(exdev/count), 2, 3);
                twisstablemodel.setValueAt(Math.sqrt(eydev/count), 2, 4);
                twisstablemodel.fireTableDataChanged();
            }
        });
        
        
        singlepassbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sortWirenamelist();
                if(!errorcheck()) singlepass();
            }
        });
        
        machinestatechooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                //if(edmresultsdatatable.records().size() != 0){
                if(machinestatechooser.getItemCount() > 0 ){
                    selectedfile = (String)machinestatechooser.getSelectedItem();
                    String id = pvloggerfield.getText();
                    if(selectedfile.equals(new String("User Defined"))){
                        if(!id.equals(new String(""))){
                            System.out.println("Taking pvlogger id from user defined box.");
                            System.out.println("User Id is " + pvloggerid);
                            pvloggerid = new Integer(Integer.parseInt(id));
                            doc.currentpvloggerid = pvloggerid;
                            System.out.println("Storing " + (String)datatablemodel.getValueAt(0,0) + " with " +pvloggerid);
                            doc.masterpvloggermap.put((String)datatablemodel.getValueAt(0,0), pvloggerid);
                        }
                        else{
                            System.out.println("Please enter a pvlogger id in the box... In the meantime I will take it from the first file listed.");
                        }
                    }
                    if(!selectedfile.equals(new String("User Defined")) || id.equals(new String(""))){
                        pvloggerid = pvloggermap.get(selectedfile);
                        System.out.println("Taking pvlogger from file " + selectedfile + "; pvloggerid is = " + pvloggerid);
                        doc.currentpvloggerid = pvloggerid;
                        
                    }
                }
            }
        });
        
        rmschooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(rmschooser.getSelectedIndex()==0){
                    edmrms = true;
                    userrms = true;
                }
                if(rmschooser.getSelectedIndex()==1){
                    edmrms = true;
                    userrms = false;
                }
                if(rmschooser.getSelectedIndex()==2){
                    edmrms = false;
                    userrms = true;
                }
            }
        });
        
        plotbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                plotfromstorage();
            }
        });
        
        probeeditbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                SimpleProbeEditor spe = new SimpleProbeEditor(doc.getAcceleratorWindow(), scenario.getProbe());  // TODO: CKA - NEVER USED
                //		spe.createSimpleProbeEditor(scenario.getProbe());
                System.out.println("In probe and beam current is " + initprobe.getBeamCurrent());
                
            }
        });
        
        solvebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                sortWirenamelist();
                if(!errorcheck()) solve();
            }
        });
		
        store.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                updateStored();
                xGraphdata.add(xGraph);
                yGraphdata.add(yGraph);
                hGraphdata.add(hGraph);
                vGraphdata.add(vGraph);
            }
        });
        
        clearbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
                storedmodel.clearAllData();
                storedmodel.fireTableDataChanged();
            }
        });
        
        exportbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int returnValue = fc.showSaveDialog(ModelFace.this);
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    try{
                        exportPlotData(file);
                    }
                    catch(IOException ioe){
                        ioe.printStackTrace();
                    }
                }
                else{
                    System.out.println("Save command canceled by user.");
                }
                
            }
        });
        
        setlimits.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                final JDialog limitdialog = new JDialog();
                limitdialog.setLayout(new GridLayout(8,4));
                limitdialog.add(new JLabel("Twiss"));
                limitdialog.add(new JLabel("Inital"));
                limitdialog.add(new JLabel("Lower"));
                limitdialog.add(new JLabel("Upper"));
                
                final JTextField alphaXinitial = new JTextField(10);
                final JTextField alphaXlower = new JTextField(10);
                final JTextField alphaXupper = new JTextField(10);
                final JTextField betaXinitial = new JTextField(10);
                final JTextField betaXlower = new JTextField(10);
                final JTextField betaXupper = new JTextField(10);
                final JTextField emitXinitial = new JTextField(10);
                final JTextField emitXlower = new JTextField(10);
                final JTextField emitXupper = new JTextField(10);
                final JTextField alphaYinitial = new JTextField(10);
                final JTextField alphaYlower = new JTextField(10);
                final JTextField alphaYupper = new JTextField(10);
                final JTextField betaYinitial = new JTextField(10);
                final JTextField betaYlower = new JTextField(10);
                final JTextField betaYupper = new JTextField(10);
                final JTextField emitYinitial = new JTextField(10);
                final JTextField emitYlower = new JTextField(10);
                final JTextField emitYupper = new JTextField(10);
                alphaXinitial.setText(numfor.format(alphax0)); alphaXlower.setText(numfor.format(alphaXmin)); alphaXupper.setText(numfor.format(alphaXmax));
                alphaYinitial.setText(numfor.format(alphay0)); alphaYlower.setText(numfor.format(alphaYmin)); alphaYupper.setText(numfor.format(alphaYmax));
                betaXinitial.setText(numfor.format(betax0)); betaXlower.setText(numfor.format(betaXmin)); betaXupper.setText(numfor.format(betaXmax));
                betaYinitial.setText(numfor.format(betay0)); betaYlower.setText(numfor.format(betaYmin)); betaYupper.setText(numfor.format(betaYmax));
                emitXinitial.setText(numfor.format(emitx0)); emitXlower.setText(numfor.format(emitXmin)); emitXupper.setText(numfor.format(emitXmax));
                emitYinitial.setText(numfor.format(emity0)); emitYlower.setText(numfor.format(emitYmin)); emitYupper.setText(numfor.format(emitYmax));
                limitdialog.add(new JLabel("Alphax"));
                limitdialog.add(alphaXinitial);
                limitdialog.add(alphaXlower);
                limitdialog.add(alphaXupper);
                limitdialog.add(new JLabel("Betax [m]         "));
                limitdialog.add(betaXinitial);
                limitdialog.add(betaXlower);
                limitdialog.add(betaXupper);
                limitdialog.add(new JLabel("Emitx [pi-mm-mrad]"));
                limitdialog.add(emitXinitial);
                limitdialog.add(emitXlower);
                limitdialog.add(emitXupper);
                limitdialog.add(new JLabel("Alphay            "));
                limitdialog.add(alphaYinitial);
                limitdialog.add(alphaYlower);
                limitdialog.add(alphaYupper);
                limitdialog.add(new JLabel("Betay [m]         "));
                limitdialog.add(betaYinitial);
                limitdialog.add(betaYlower);
                limitdialog.add(betaYupper);
                limitdialog.add(new JLabel("Emity [pi-mm-mrad]"));
                limitdialog.add(emitYinitial);
                limitdialog.add(emitYlower);
                limitdialog.add(emitYupper);
                JButton set = new JButton("Set");
                sortWirenamelist();
                //if(!errorcheck());
                
                
                IAlgorithm etracker = null;
                
                try {
                    
                    etracker = AlgorithmFactory.createEnvTrackerAdapt( seq );
                    
                } catch ( InstantiationException exception ) {
                    System.err.println( "Instantiation exception creating tracker." );
                    exception.printStackTrace();
                }
                EnvelopeProbe tempprobe = ProbeFactory.getEnvelopeProbe(seq, etracker);
                tempprobe = EnvelopeProbe.newInstance(initprobe);
                Scenario tempscenario;
                try{
                    tempscenario = Scenario.newScenarioFor(seq);
                    tempscenario.setProbe(tempprobe);
                    tempscenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
                    PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
                    tempscenario = plds.setModelSource(seq, tempscenario);
                    tempscenario.resync();
                    tempscenario.run();
                    plds.closeConnection();
                }catch(Exception ex){}
                
                Trajectory<EnvelopeProbeState> traj = tempprobe.getTrajectory();
                Iterator<EnvelopeProbeState> iterState= traj.stateIterator();
                EnvelopeProbeState state = iterState.next();
                CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
                Twiss[] twiss = covarianceMatrix.computeTwiss();    // TODO: CKA - NEVER USED
                
                limitdialog.add(set);
                limitdialog.pack();
                limitdialog.setVisible(true);
                set.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        alphax0=Double.parseDouble(alphaXinitial.getText());
                        alphaXmin = Double.parseDouble(alphaXlower.getText());
                        alphaXmax = Double.parseDouble(alphaXupper.getText());
                        betax0=Double.parseDouble(betaXinitial.getText());
                        betaXmin = Double.parseDouble(betaXlower.getText());
                        betaXmax = Double.parseDouble(betaXupper.getText());
                        emitx0 = Double.parseDouble(emitXinitial.getText());
                        emitXmin = Double.parseDouble(emitXlower.getText());
                        emitXmax = Double.parseDouble(emitXupper.getText());
                        alphay0=Double.parseDouble(alphaYinitial.getText());
                        alphaYmin = Double.parseDouble(alphaYlower.getText());
                        alphaYmax = Double.parseDouble(alphaYupper.getText());
                        betay0 = Double.parseDouble(betaYinitial.getText());
                        betaYmin = Double.parseDouble(betaYlower.getText());
                        betaYmax = Double.parseDouble(betaYupper.getText());
                        emity0 = Double.parseDouble(emitYinitial.getText());
                        emitYmin = Double.parseDouble(emitYlower.getText());
                        emitYmax = Double.parseDouble(emitYupper.getText());
                        limitsWereSet = true;
                        limitdialog.setVisible(false);
                    }
                });
            }
        });
    }
    
    public void buildseq(){
        if(((String)elementList.getSelectedItem()).equals("Select Element")){
            elementList.setEditable(true);
            elementList.setSelectedItem(Wirenamelist.get(0));
            elementList.setEditable(false);
            errorlabel.setText("No Element was selected. Twiss was found at first wire.");
        }
        /*
         int startp = findSeqp((ArrayList)accl.getSequences(),(String)elementList.getSelectedItem());
         int endp  = findSeqp((ArrayList)accl.getSequences(),(String)Wirenamelist.get(0));
         for(int i=1;i<Wirenamelist.size();i++){
         if(findSeqp((ArrayList)accl.getSequences(),(String)Wirenamelist.get(i))>endp) endp = findSeqp((ArrayList)accl.getSequences(),(String)Wirenamelist.get(i));
         }
         seq = new AcceleratorSeqCombo("Combo", accl.getSequences().subList(startp,endp+1));
         */
        seq = solveforseq;
        lastusedelement = (String)elementList.getSelectedItem();
    }
    
    
    public void updateStored(){
        ArrayList<Object> storeddata = new ArrayList<Object>();
		if(((String)machinestatechooser.getSelectedItem()).equals("User Defined")){
			storeddata.add((String)datatablemodel.getValueAt(0,0));
		}
		if(!((String)machinestatechooser.getSelectedItem()).equals("User Defined")){
			storeddata.add(new String((String)machinestatechooser.getSelectedItem()));
		}
        storeddata.add(new String((String)elementList.getSelectedItem()));
        storeddata.add(new Double((Double)twisstable.getValueAt(0,1)));
        storeddata.add(new Double((Double)twisstable.getValueAt(0,2)));
        storeddata.add(new Double((Double)twisstable.getValueAt(1,1)));
        storeddata.add(new Double((Double)twisstable.getValueAt(1,2)));
        storeddata.add(new Double((Double)twisstable.getValueAt(2,1)));
        storeddata.add(new Double((Double)twisstable.getValueAt(2,2)));
        storeddata.add(new Boolean(false));
        storedmodel.addTableData(new ArrayList<Object>(storeddata));
        storedmodel.fireTableDataChanged();
        
    }
    public void plotfromstorage(){
        modelplot.removeAllGraphData();
        if(graphChooser.getSelectedIndex()>2){
            modelplot.setPreferredSize(new Dimension(300,300));
            plotpanel.setPreferredSize(new Dimension(830,330));
        }
        else{
            modelplot.setPreferredSize(new Dimension(800, 200));
            plotpanel.setPreferredSize(new Dimension(830, 230));
        }
        for(int i=0;i<storedtable.getRowCount();i++){
            if((Boolean)storedtable.getValueAt(i,8))
            {
                if(graphChooser.getSelectedIndex()==3 || graphChooser.getSelectedIndex()==5){
                    BasicGraphData point = new BasicGraphData();
                    point.addPoint((Double)storedtable.getValueAt(i,2),(Double)storedtable.getValueAt(i,4));
                    plotGraph(point);
                }
                if(graphChooser.getSelectedIndex()==4 || graphChooser.getSelectedIndex()==5){
                    BasicGraphData point = new BasicGraphData();
                    point.addPoint((Double)storedtable.getValueAt(i,3),(Double)storedtable.getValueAt(i,5));
                    plotGraph(point);
                }
                if(graphChooser.getSelectedIndex()==0 || graphChooser.getSelectedIndex()==1){
                    plotGraph(xGraphdata.get(i));
                    plotGraph(hGraphdata.get(i));
                }
                if(graphChooser.getSelectedIndex()==0 || graphChooser.getSelectedIndex()==2){
                    plotGraph(yGraphdata.get(i));
                    plotGraph(vGraphdata.get(i));
                }
                
            }
        }
    }
    public int findSeqp(ArrayList<AcceleratorSeq> searchedseq, String tofind)
    {
        for(int i=0;i<searchedseq.size();i++){
            AcceleratorSeq sequence = searchedseq.get(i);
            LinkedList<AcceleratorNode> nodeslist = (LinkedList<AcceleratorNode>) sequence.getAllNodes();
            for(int j=0;j<nodeslist.size();j++){
                if(nodeslist.get(j).getId().equalsIgnoreCase(tofind))
                    return i;
            }
        }
        return 0;
    }
    public int findNodep(AcceleratorSeq searchedseq, String tofind){
        LinkedList<AcceleratorNode> nodeslist = (LinkedList<AcceleratorNode>) searchedseq.getAllNodes();
        for(int j=0;j<nodeslist.size();j++){
            if(nodeslist.get(j).getId().equalsIgnoreCase(tofind))
                return j;
        }
        return 0;
    }
    
    public boolean errorcheck(){
        int count = 0;
        for(int i=0;i<datatable.getRowCount();i++)
            if((Boolean)datatable.getValueAt(i,6)) count++;
        if(count<3){
            errorlabel.setText("Please select at least three wires");
            return true;
        }
        for(int i=0;i<datatable.getRowCount();i++){
            if((Boolean)datatable.getValueAt(i,6) && ((findNodep(seq,(String)datatable.getValueAt(i,1))<findNodep(seq,(String)elementList.getSelectedItem())))){
                errorlabel.setText("Please only select an element upstream from the first Wire");
                return true;
            }
        }
        return false;
    }
    
    public void selectSequences(){
		int rows = datatablemodel.getRowCount();
		useMEBT = false;
		useDTL = false;
		useCCL = false;
		useSCL = false;
		useHEBT = false;
		useRTBT = false;
		String currentwire = new String("");
		for(int i = 0; i < rows; i++){
			currentwire = (String)datatablemodel.getValueAt(i,1);
			if(currentwire.startsWith("MEBT") && useMEBT == false) useMEBT = true;
			if(currentwire.startsWith("DTL") && useDTL == false) useDTL = true;
			if(currentwire.startsWith("CCL") && useCCL == false) useCCL = true;
			if(currentwire.startsWith("SCL") && useSCL == false) useSCL = true;
			if(currentwire.startsWith("HEBT") && useHEBT == false) useHEBT = true;
			if(currentwire.startsWith("RTBT") && useRTBT == false) useRTBT = true;
		}
    }
	
	
    
    @SuppressWarnings ("unchecked") //Had to suppress warning because valueforkey returns object and would not allow for specific casting
    
    public void refreshTable(){
		datatablemodel.clearAllData();
		ArrayList<Object> tabledata = new ArrayList<Object>();
		
		if(edmresultsdatatable.records().size() == 0){
			System.out.println("No data available to load!");
		}
		else{
            Collection<GenericRecord> records = edmresultsdatatable.records();
            Iterator<GenericRecord> itr = records.iterator();
            while(itr.hasNext()){
                tabledata.clear();
                GenericRecord record = itr.next();
                String filename=(String)record.valueForKey("file");
                String wire = (String)record.valueForKey("wire");
                ArrayList<Double> params = (ArrayList<Double>)record.valueForKey("rms");
                
                tabledata.add(new String(filename));
                tabledata.add(new String(wire));
                if(!params.isEmpty()){
                    tabledata.add(new Double((params.get(2)).doubleValue()));
                    tabledata.add(new Double((params.get(1)).doubleValue()));
                }
                else{
                    tabledata.add(new Double(0.0));
                    tabledata.add(new Double(0.0));
                }
                if(fitresultsdatatable.records().size() == 0){
                    tabledata.add(new Double(0.0));
                    tabledata.add(new Double(0.0));
                }
                else{
                    Map<String, String> hbindings = new HashMap<String, String>();
                    String hdirection = new String("H");
                    String vdirection = new String("V");
                    hbindings.put("file", filename);
                    hbindings.put("wire", wire);
                    hbindings.put("direction", hdirection);
                    Map<String, String> vbindings = new HashMap<String, String>();
                    vbindings.put("file", filename);
                    vbindings.put("wire", wire);
                    vbindings.put("direction", vdirection);
                    GenericRecord hfitrecord =  fitresultsdatatable.record(hbindings);
                    if(hfitrecord==null){
                        tabledata.add(new Double(0.0));
                    }
                    else{
                        ArrayList<double[]> data = (ArrayList<double[]>)hfitrecord.valueForKey("data");
                        double[] rms = data.get(0);
                        tabledata.add(new Double(rms[0]));
                    }
                    GenericRecord vfitrecord =  fitresultsdatatable.record(vbindings);
                    if(vfitrecord==null){
                        tabledata.add(new Double(0.0));
                    }
                    else{
                        ArrayList<double[]> data = (ArrayList<double[]>)vfitrecord.valueForKey("data");
                        double[] rms = data.get(0);
                        tabledata.add(new Double(rms[0]));
                    }
                }
                tabledata.add(new Boolean(true));
                datatablemodel.addTableData(new ArrayList<Object>(tabledata));
            }
            datatablemodel.fireTableDataChanged();
        }
    }
    
    public void refreshFileArray(){
		int rows = datatablemodel.getRowCount();
		filesloaded.clear();
		machinestatechooser.setEnabled(false);
		machinestatechooser.removeAllItems();
		for(int i = 0; i < rows; i++){
			String filename = new String();
			filename = (String)datatablemodel.getValueAt(i,0);
			if(!filesloaded.contains(filename)){
				filesloaded.add(new String(filename));
				machinestatechooser.addItem((String)datatablemodel.getValueAt(i,0));
			}
		}
		machinestatechooser.addItem(new String("User Defined"));
		machinestatechooser.setSelectedIndex(0);
		machinestatechooser.setEnabled(true);
        
	}
	
    void initModel(){
        
	    String init = (String)elementList.getSelectedItem();
		
        IAlgorithm etracker = null;
        
        try {
            
            etracker = AlgorithmFactory.createEnvTrackerAdapt( seq );
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating tracker." );
            exception.printStackTrace();
        }
        
        
        EnvelopeProbe tempprobe = ProbeFactory.getEnvelopeProbe(seq, etracker);
        
        
        try {
            
            initprobe = ProbeFactory.getEnvelopeProbe(seq, AlgorithmFactory.createEnvTrackerAdapt( seq ));
            
            
        } catch ( InstantiationException exception ) {
            System.err.println( "Instantiation exception creating probe." );
            exception.printStackTrace();
        }
        
        Scenario tempscenario;
        
		try{
			tempscenario = Scenario.newScenarioFor(seq);
			tempscenario.setProbe(tempprobe);
			tempscenario.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
			tempscenario = plds.setModelSource(seq, tempscenario);
			tempscenario.resync();
			tempscenario.run();
			plds.closeConnection();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Trajectory<EnvelopeProbeState> traj = tempprobe.getTrajectory();
		EnvelopeProbeState newstate = traj.stateForElement(init);
		initprobe.applyState(newstate);
		modelinitialized = true;
        
		initprobe.setKineticEnergy(newstate.getKineticEnergy());
		initprobe.setPosition(seq.getPosition(seq.getNodeWithId(init)));
		
        CovarianceMatrix covarianceMatrix = newstate.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        
		
		if(firsttime || elementchanged){
			alphax0=twiss[0].getAlpha();
			betax0=twiss[0].getBeta();
			alphay0=twiss[1].getAlpha();
			betay0=twiss[1].getBeta();
			alphaz0 = twiss[2].getAlpha();
			betaz0 = twiss[2].getBeta();
			emitx0 = twiss[0].getEmittance()*1e6;
			emity0 = twiss[1].getEmittance()*1e6;
			emitz0 = twiss[2].getEmittance();
			elementchanged = false;
			firsttime = false;
		}
		Twiss xTwiss = new Twiss(alphax0, betax0, emitx0/1e6);
		Twiss yTwiss = new Twiss(alphay0, betay0, emity0/1e6);
		Twiss zTwiss = new Twiss(alphaz0, betaz0, emitz0);
		Twiss[] tw = new Twiss[3];
		tw[0]=xTwiss;
		tw[1]=yTwiss;
		tw[2]=zTwiss;
		initprobe.initFromTwiss(tw);
		
		try{
			scenario = Scenario.newScenarioFor(seq);
			scenario.setProbe(initprobe);
			scenario.setStartNode((String)elementList.getSelectedItem());
		}catch(Exception e){
			e.printStackTrace();
		}
        
        
		if(!probeeditbutton.isEnabled()) probeeditbutton.setEnabled(true);
        
    }
	
	
    public void singlepass(){
		errorlabel.setText(" ");
		if(!modelinitialized) initModel();
		solverprobe = EnvelopeProbe.newInstance(initprobe);
		solverprobe.getAlgorithm().setRfGapPhaseCalculation( false );
		
		final Tracker defaultTracker = (Tracker)initprobe.getAlgorithm();
		// want to copy the tracker if we can to avoid editing the shared probe tracker elsewhere
		//if ( defaultTracker.canCopy() ) {
        final Tracker tracker = (Tracker)defaultTracker.copy();
        solverprobe.setAlgorithm( tracker );
		//}
		solverprobe.initialize();
		
		if(limitsWereSet){
			Twiss xTwiss = new Twiss(alphax0, betax0, emitx0/1e6);
			Twiss yTwiss = new Twiss(alphay0, betay0, emity0/1e6);
			Twiss zTwiss = new Twiss(alphaz0, betaz0, emitz0);
			Twiss[] tw = new Twiss[3];
			tw[0]=xTwiss;
			tw[1]=yTwiss;
			tw[2]=zTwiss;
			solverprobe.initFromTwiss(tw);
		}
		
		System.out.println("Starting with ax, bx, ex = " + alphax0 +  " " + betax0 + " " + emitx0);
		System.out.println("Starting with ay, by, ey = " + alphay0 +  " " + betay0 + " " + emity0);
		System.out.println("Starting with az, bz, ez = " + alphaz0 +  " " + betaz0 + " " + emitz0);
        
        
		try{
			solvermodel = Scenario.newScenarioFor(seq);
			solvermodel.setProbe(solverprobe);
			solvermodel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			solvermodel.setStartNode((String)elementList.getSelectedItem());
			PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
			solvermodel = plds.setModelSource(seq, solvermodel);
			solvermodel.resync();
			solvermodel.run();
			plds.closeConnection();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		resetPlot();
		if(!probeeditbutton.isEnabled()) probeeditbutton.setEnabled(true);
        
    }
	
    
	
    public void solve(){
		errorlabel.setText(" ");
		if(!modelinitialized) initModel();
		
		solverprobe = EnvelopeProbe.newInstance(initprobe);
		
		final Tracker defaultTracker = (Tracker)initprobe.getAlgorithm();
		// want to copy the tracker if we can to avoid editing the shared probe tracker elsewhere
		//if ( defaultTracker.canCopy() ) {
        final Tracker tracker = (Tracker)defaultTracker.copy();
        solverprobe.setAlgorithm( tracker );
		//}
		solverprobe.initialize();
		
		solverprobe.getAlgorithm().setRfGapPhaseCalculation( false );
		
		if(limitsWereSet){
			Twiss xTwiss = new Twiss(alphax0, betax0, emitx0/1e6);
			Twiss yTwiss = new Twiss(alphay0, betay0, emity0/1e6);
			Twiss zTwiss = new Twiss(alphaz0, betaz0, emitz0);
			Twiss[] tw = new Twiss[3];
			tw[0]=xTwiss;
			tw[1]=yTwiss;
			tw[2]=zTwiss;
			solverprobe.initFromTwiss(tw);
		}
		setInitLimits();
		System.out.println("Starting with ax, bx, ex = " + alphax0 +  " " + betax0 + " " + emitx0);
		System.out.println("Starting with ay, by, ey = " + alphay0 +  " " + betay0 + " " + emity0);
		System.out.println("Starting with az, bz, ez = " + alphaz0 +  " " + betaz0 + " " + emitz0);
        
		try{
			solvermodel = Scenario.newScenarioFor(seq);
			System.out.println("seq in solver model is " + seq);
			solvermodel.setProbe(solverprobe);
			solvermodel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			solvermodel.setStartNode((String)elementList.getSelectedItem());
			PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
			solvermodel = plds.setModelSource(seq, solvermodel);
			solvermodel.resync();
			plds.closeConnection();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		variables.add(new Variable("alphaX",alphax0, alphaXmin, alphaXmax));
		variables.add(new Variable("betaX",betax0, betaXmin, betaXmax));
		variables.add(new Variable("alphaY", alphay0, alphaYmin, alphaYmax));
		variables.add(new Variable("betaY",betay0, betaYmin, betaYmax));
		variables.add(new Variable("emitX",emitx0, emitXmin, emitXmax));
		variables.add(new Variable("emitY",emity0, emitYmin, emitYmax));
		ArrayList<Objective> objectives = new ArrayList<Objective>();
		objectives.add(new TargetObjective( "diff", 0.0 ) );
        
		Evaluator1 evaluator = new Evaluator1( objectives, variables );
        
		Problem problem = new Problem( objectives, variables, evaluator );
		problem.addHint(new InitialDelta( 0.1 ) );
		
		Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, Integer.parseInt(timefield.getText()) , 0.999 );
		//Solver solver = new Solver(new DirectedStep(),maxSolutionStopper );
		Solver solver = new Solver(new RandomShrinkSearch(),maxSolutionStopper );
		
		solver.solve( problem );
		System.out.println("score is " + solver.getScoreBoard());
		Trial best = solver.getScoreBoard().getBestSolution();
		
		// rerun with solution to populate results table
		calcError(variables, best);
		updateProbe(variables, best);
		try{
			solvermodel.resync();
			solvermodel.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
		
		resetTwissTable(variables,best);
		resetPlot();
		postFinalErrors(variables,best);
		
    }
	
    
    void setInitLimits(){
        
		//Set upper limits on alpha and beta to max values in sequence, lower limit to zero or neg. max.
		
		EnvelopeProbe tempprobe = EnvelopeProbe.newInstance(initprobe);
		
		Scenario tempscenario;
		try{
			tempscenario = Scenario.newScenarioFor(seq);
			tempscenario.setProbe(tempprobe);
			tempscenario.setStartNode((String)elementList.getSelectedItem());
			PVLoggerDataSource plds = new PVLoggerDataSource(pvloggerid.intValue());
			tempscenario = plds.setModelSource(seq, tempscenario);
			tempscenario.resync();
			tempscenario.run();
			plds.closeConnection();
		}catch(Exception e){
			e.printStackTrace();
		}
		
		Trajectory<EnvelopeProbeState> traj = tempprobe.getTrajectory();
        Iterator<?> iterState= traj.stateIterator();
        EnvelopeProbeState state= (EnvelopeProbeState)iterState.next();
        
        CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        
        if(!limitsWereSet || elementchanged){
			alphaXmax = Math.abs(twiss[0].getAlpha());
			alphaYmax = Math.abs(twiss[1].getAlpha());
			betaXmax = twiss[0].getBeta();
			betaYmax = twiss[1].getBeta();
			emitXmax = twiss[0].getEmittance()*1e6;
			emitYmax = twiss[1].getEmittance()*1e6;
			boolean RTBTlimit = false;
			while(iterState.hasNext()){
				state= (EnvelopeProbeState)iterState.next();
                
                covarianceMatrix = state.getCovarianceMatrix();
                twiss = covarianceMatrix.computeTwiss();
                
                
				if(Math.abs(twiss[0].getAlpha()) > alphaXmax) alphaXmax = Math.abs(twiss[0].getAlpha());
				if(Math.abs(twiss[1].getAlpha()) > alphaYmax) alphaYmax = Math.abs(twiss[1].getAlpha());
                if(!RTBTlimit){
                    if(twiss[0].getBeta() > betaXmax) betaXmax = twiss[0].getBeta();
                    if(twiss[1].getBeta() > betaYmax) betaYmax = twiss[1].getBeta();
                    if(state.toString().contains("RTBT_Diag:WS24")) RTBTlimit = true;
                }
				if(twiss[0].getEmittance() > emitXmax) emitXmax = twiss[0].getEmittance()*1e6;
				if(twiss[1].getEmittance() > emitYmax) emitYmax = twiss[1].getEmittance()*1e6;
            }
			alphaXmax *= 1.0;
			alphaYmax *= 1.0;
			betaXmin *= 1.0;
			betaXmax *= 1.0;
			emitXmax *= 4;
			emitYmax *= 4;
			alphaXmin = -alphaXmax;
			alphaYmin = -alphaYmax;
			betaXmin = 0;
			betaYmin = 0;
			emitXmin = 0.05;
			emitYmin = 0.05;
			limitsWereSet=true;
        }
		
		
    }
    
    
    public double calcError(ArrayList<Variable> vars, Trial trial){
		updateProbe(vars, trial);
		try{
			solvermodel.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
        
		Trajectory<EnvelopeProbeState> traj= solverprobe.getTrajectory();
		double error = 0.0;
		int size = Wirenamelist.size();
		double rx=0;
		double ry=0;
		
		for(int i =0; i<size; i++){
			EnvelopeProbeState state = traj.statesForElement(Wirenamelist.get(i)).get(0);
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
			
			rx =  twiss[0].getEnvelopeRadius();
			ry =  twiss[1].getEnvelopeRadius();
			if(rmschooser.getSelectedIndex()==0){
                error += Math.pow( (rx*1000. - (xEdmdatalist.get(i)).doubleValue()), 2.);
                error += Math.pow( (ry*1000. - (yEdmdatalist.get(i)).doubleValue()), 2.);
            }
            else{
                error += Math.pow( (rx*1000. - (xUserdatalist.get(i)).doubleValue()), 2.);
                error += Math.pow( (ry*1000. - (yUserdatalist.get(i)).doubleValue()), 2.);
            }
		}
		error = Math.sqrt(error);
     	return error;
    }
    
    
	public void postFinalErrors(ArrayList<Variable> vars, Trial trial){
		updateProbe(vars, trial);
		try{
			solvermodel.run();
		}
		catch(Exception exception){
			exception.printStackTrace();
		}
		
		Trajectory<EnvelopeProbeState> traj= solverprobe.getTrajectory();
		double xerror = 0.0;
		double yerror = 0.0;
		int size = Wirenamelist.size();
		double rx=0;
		double ry=0;
		
		for(int i =0; i<size; i++){
			EnvelopeProbeState state = traj.statesForElement(Wirenamelist.get(i)).get(0);
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
			
			rx =  twiss[0].getEnvelopeRadius();
			ry =  twiss[1].getEnvelopeRadius();
			if(rmschooser.getSelectedIndex()==0){
				xerror += Math.pow( (rx*1000. - (xEdmdatalist.get(i)).doubleValue()), 2.);
				yerror += Math.pow( (ry*1000. - (yEdmdatalist.get(i)).doubleValue()), 2.);
			}
			else{
				xerror += Math.pow( (rx*1000. - (xUserdatalist.get(i)).doubleValue()), 2.);
				yerror += Math.pow( (ry*1000. - (yUserdatalist.get(i)).doubleValue()), 2.);
			}
		}
		xerror = Math.sqrt(xerror);
		yerror = Math.sqrt(yerror);
		DecimalFormat decfor =  new DecimalFormat("#####.000");
		xchisquarefield.setText((new Double(decfor.format(xerror))).toString());
		ychisquarefield.setText((new Double(decfor.format(yerror))).toString());
     	System.out.println("Final error is " + xerror + "  " + yerror);
    }
    
	
    public void resetPlot(){
		modelplot.setPreferredSize(new Dimension(800, 200));
		plotpanel.setPreferredSize(new Dimension(830, 230));
		modelplot.removeAllGraphData();
		BasicGraphData hgraphdata = new BasicGraphData();
		BasicGraphData vgraphdata = new BasicGraphData();
		BasicGraphData xgraphdata = new BasicGraphData();
		BasicGraphData ygraphdata = new BasicGraphData();
		
		ArrayList<Double> sdata = new ArrayList<Double>();
		ArrayList<Double> hdata = new ArrayList<Double>();
		ArrayList<Double> vdata = new ArrayList<Double>();
        
		Trajectory<EnvelopeProbeState> traj= solverprobe.getTrajectory();
		Iterator<EnvelopeProbeState> iterState= traj.stateIterator();
        
        
		while(iterState.hasNext()){
			EnvelopeProbeState state= iterState.next();
			sdata.add(state.getPosition());
            
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
			double rx =  1000.0*twiss[0].getEnvelopeRadius();
			double ry =  1000.0*twiss[1].getEnvelopeRadius();
			hdata.add(rx);
			vdata.add(ry);
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
		hgraphdata.setDrawPointsOn(false);
		hgraphdata.setDrawLinesOn(true);
		hgraphdata.setGraphProperty("Legend", new String("Horizontal"));
		hgraphdata.setGraphColor(Color.RED);
		vgraphdata.addPoint(s, y);
		vgraphdata.setDrawPointsOn(false);
		vgraphdata.setDrawLinesOn(true);
		vgraphdata.setGraphProperty("Legend", new String("Vertical"));
		vgraphdata.setGraphColor(Color.BLUE);
		modelplot.setExternalGL(limits);
		
		modelplot.addGraphData(hgraphdata);
		modelplot.addGraphData(vgraphdata);
        
		hGraph = hgraphdata;
		vGraph = vgraphdata;
		
		int datasize = Wirenamelist.size();
		double[] srdata = new double[datasize];
		double[] xrdata = new double[datasize];
		double[] yrdata = new double[datasize];
		traj= solverprobe.getTrajectory();
		EnvelopeProbeState newstate;
		Twiss[] newtwiss;         // TODO: CKA - NEVER USED
		double rx, ry;            // TODO: CKA - NEVER USED
		
		for(int i =0; i<datasize; i++){
			newstate = traj.statesForElement(Wirenamelist.get(i)).get(0);
			srdata[i]=newstate.getPosition();
			if(rmschooser.getSelectedIndex()==0){
				xrdata[i]=(xEdmdatalist.get(i)).doubleValue();
				yrdata[i]=(yEdmdatalist.get(i)).doubleValue();
			}
			else{
				xrdata[i]=(xUserdatalist.get(i)).doubleValue();
				yrdata[i]=(yUserdatalist.get(i)).doubleValue();
			}
        }
		xgraphdata.addPoint(srdata, xrdata);
		xgraphdata.setDrawPointsOn(true);
		xgraphdata.setDrawLinesOn(false);
		xgraphdata.setGraphColor(Color.RED);
		ygraphdata.addPoint(srdata, yrdata);
		ygraphdata.setDrawPointsOn(true);
		ygraphdata.setDrawLinesOn(false);
		ygraphdata.setGraphColor(Color.BLUE);
		
		modelplot.addGraphData(xgraphdata);
		modelplot.addGraphData(ygraphdata);
		xGraph = xgraphdata;
		yGraph = ygraphdata;
		limits.setSmartLimits();
		modelplot.setExternalGL(limits);
    }
    
    
    public void exportPlotData(File file) throws IOException{
        
		int datasize = Wirenamelist.size();
		double[] srdata = new double[datasize];
		double[] xrdata = new double[datasize];
		double[] yrdata = new double[datasize];
		traj= solverprobe.getTrajectory();
		EnvelopeProbeState newstate;
        
		for(int i =0; i<datasize; i++){
			newstate = traj.statesForElement(Wirenamelist.get(i)).get(0);
            srdata[i]=newstate.getPosition();
			if(rmschooser.getSelectedIndex()==0){
                xrdata[i]=(xEdmdatalist.get(i)).doubleValue();
                yrdata[i]=(yEdmdatalist.get(i)).doubleValue();
            }
            else{
                xrdata[i]=(xUserdatalist.get(i)).doubleValue();
                yrdata[i]=(yUserdatalist.get(i)).doubleValue();
            }
		}
        
        OutputStream fout = new FileOutputStream(file);
        String line = "# Wire RMS: position (m), x(mm), y(mm) +\n";
        for(int i =0; i<datasize; i++){
            line = line + Wirenamelist.get(i) + "\t" + srdata[i] + "\t" + xrdata[i] + "\t" + yrdata[i] + "\n";
        }
        line = line + "#\n# Model fit: Element, position (m), x(mm), y(mm)\n";
        
		solverprobe = EnvelopeProbe.newInstance(initprobe);
		
		final Tracker defaultTracker = (Tracker)initprobe.getAlgorithm();
		// want to copy the tracker if we can to avoid editing the shared probe tracker elsewhere
		//if ( defaultTracker.canCopy() ) {
        final Tracker tracker = (Tracker)defaultTracker.copy();
        solverprobe.setAlgorithm( tracker );
		//}
		solverprobe.initialize();
		
		try{
			solvermodel.setProbe(solverprobe);
			solvermodel.setStartNode((String)elementList.getSelectedItem());
			solvermodel.resync();
			solvermodel.run();
        }
        catch(Exception exception){
			exception.printStackTrace();
        }
        Trajectory<EnvelopeProbeState> traj= solverprobe.getTrajectory();
        Iterator<EnvelopeProbeState> iterState= traj.stateIterator();
        
        while(iterState.hasNext()){
            EnvelopeProbeState state= iterState.next();
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
            double rx =  1000.0*twiss[0].getEnvelopeRadius();
            double ry =  1000.0*twiss[1].getEnvelopeRadius();
            if(!(state.getElementId()).equals("")){
                line = line + state.getElementId() + "    " + state.getPosition() + "    " + rx + "    " + ry + "\n";
            }
        }
        
		byte buf[] = line.getBytes();
		fout.write(buf);
		fout.close();
    }
    
    
    public void plotGraph(BasicGraphData g){
        modelplot.addGraphData(g);
        limits.setSmartLimits();
        modelplot.setExternalGL(limits);
    }
    
    
    public void resetTwissTable(ArrayList<Variable> vars, Trial trial){
		Iterator<Variable> itr = vars.iterator();
		traj= solverprobe.getTrajectory();
		EnvelopeProbeState newstate = traj.statesForElement((String)elementList.getSelectedItem()).get(0);
		
		double T = newstate.getKineticEnergy();
		double m = newstate.getSpeciesRestEnergy();
		double gamma = T/m + 1;
		double beta = Math.abs(Math.sqrt(1-1/(gamma*gamma)));
		double gammabeta = gamma*beta;
        
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            String name = variable.getName();
            DecimalFormat decfor =  new DecimalFormat("###.000");
            if(name.equalsIgnoreCase("alphaX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),0,1);
                currenttwiss[0]=value;
                alphax0=value;
            }
            if(name.equalsIgnoreCase("alphaY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),0,2);
                currenttwiss[3]=value;
                alphay0=value;
            }
            if(name.equalsIgnoreCase("betaX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),1,1);
                currenttwiss[1]=value;
                betax0=value;
            }
            if(name.equalsIgnoreCase("betaY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),1,2);
                currenttwiss[4]=value;
                betay0=value;
            }
            if(name.equalsIgnoreCase("emitY")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),2,2);
                twisstablemodel.setValueAt(new Double(decfor.format(value*gammabeta)),3,2);
                currenttwiss[5]=value;
                emity0=value;
            }
            if(name.equalsIgnoreCase("emitX")){
                twisstablemodel.setValueAt(new Double(decfor.format(value)),2,1);
                twisstablemodel.setValueAt(new Double(decfor.format(value*gammabeta)),3,1);
                currenttwiss[2]=value;
                emitx0=value;
            }
        }
		twisstablemodel.fireTableDataChanged();
        
    }
	
	
    void updateProbe(ArrayList<Variable> vars, Trial trial){
		double alphax=0.0;
		double alphay=0.0;
		double betax=0.0;
		double betay=0.0;
		double emitx=0.0;
		double emity=0.0;
		
		solverprobe = EnvelopeProbe.newInstance(initprobe);
        
		final Tracker defaultTracker = (Tracker)initprobe.getAlgorithm();
		// want to copy the tracker if we can to avoid editing the shared probe tracker elsewhere
		//if ( defaultTracker.canCopy() ) {
        final Tracker tracker = (Tracker)defaultTracker.copy();
        solverprobe.setAlgorithm( tracker );
		//}
		solverprobe.initialize();
		solvermodel.setProbe(solverprobe);
		Iterator<Variable> itr = vars.iterator();
		
		while(itr.hasNext()){
			Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("alphaX")) alphax = value;
            if(name.equalsIgnoreCase("alphaY")) alphay = value;
            if(name.equalsIgnoreCase("betaX")) betax = value;
            if(name.equalsIgnoreCase("betaY")) betay = value;
            if(name.equalsIgnoreCase("emitY")){
				emity = value * 1.e-6;
            }
            if(name.equalsIgnoreCase("emitX")){
                emitx = value * 1.e-6;
            }
        }
		Twiss xTwiss = new Twiss(alphax, betax, emitx);
		Twiss yTwiss = new Twiss(alphay, betay, emity);
		Twiss zTwiss = new Twiss(alphaz0,betaz0,emitz0);
        
		Twiss[] tw= new Twiss[3];
		tw[0]=xTwiss;
		tw[1]=yTwiss;
		tw[2]=zTwiss;
        
		solverprobe.initFromTwiss(tw);
    }
    
    
	public void makeTwissTable(){
		String[] colnames = {"Parameter", "X", "Y"};
        
		twisstablemodel = new ResultsTableModel(colnames,4);
		
		twisstable = new JTable(twisstablemodel);
		twisstable.getColumnModel().getColumn(0).setMinWidth(100);
		twisstable.getColumnModel().getColumn(1).setMinWidth(115);
		twisstable.getColumnModel().getColumn(2).setMinWidth(115);
		
		twisstable.setRowSelectionAllowed(false);
		twisstable.setColumnSelectionAllowed(false);
		twisstable.setCellSelectionEnabled(true);
		twissscrollpane = new JScrollPane(twisstable,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		twissscrollpane.setColumnHeaderView(twisstable.getTableHeader());
		twisstable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		twissscrollpane.setPreferredSize(new Dimension(324, 84));
    }
    
    
    public void makeStoredResultsTable(){
        String[] colnames = {"File Name", "Element solved for", "alphax", "alphay", "betax", "betay", "emitx", "emity", "Select"};
        
		storedmodel = new DataTableModel(colnames, 0);
		
		storedtable = new JTable(storedmodel);
		storedtable.getColumnModel().getColumn(0).setMinWidth(165);
		storedtable.getColumnModel().getColumn(1).setMinWidth(120);
		storedtable.getColumnModel().getColumn(2).setMinWidth(50);
		storedtable.getColumnModel().getColumn(3).setMinWidth(50);
		storedtable.getColumnModel().getColumn(4).setMinWidth(50);
		storedtable.getColumnModel().getColumn(4).setMinWidth(50);
		storedtable.getColumnModel().getColumn(5).setMinWidth(20);
		
		storedtable.setPreferredScrollableViewportSize(storedtable.getPreferredSize());
		storedtable.setRowSelectionAllowed(false);
		storedtable.setColumnSelectionAllowed(false);
		storedtable.setCellSelectionEnabled(false);
        
		storedscrollpane = new JScrollPane(storedtable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		storedscrollpane.getVerticalScrollBar().setValue(0);
		storedscrollpane.getHorizontalScrollBar().setValue(0);
		storedscrollpane.setPreferredSize(new Dimension(800, 100));
    }
    
    public void makeDataTable(){
		String[] colnames = {"File Name", "Wire", "Edm x_rms", "Edm y_rms", "User X_rms", "User Y_rms", "Select"};
        
		datatablemodel = new DataTableModel(colnames, 0);
		
		datatable = new JTable(datatablemodel);
		datatable.getColumnModel().getColumn(0).setMinWidth(165);
		datatable.getColumnModel().getColumn(1).setMinWidth(120);
		datatable.getColumnModel().getColumn(2).setMinWidth(50);
		datatable.getColumnModel().getColumn(3).setMinWidth(50);
		datatable.getColumnModel().getColumn(4).setMinWidth(50);
		datatable.getColumnModel().getColumn(5).setMinWidth(20);
		
		datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
		datatable.setRowSelectionAllowed(false);
		datatable.setColumnSelectionAllowed(false);
		datatable.setCellSelectionEnabled(false);
        
		datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		datascrollpane.getVerticalScrollBar().setValue(0);
		datascrollpane.getHorizontalScrollBar().setValue(0);
		datascrollpane.setPreferredSize(new Dimension(655, 100));
        
        
    }
	
    public void sortWirenamelist(){
        
		xUserfulldatalist.clear();
        yUserfulldatalist.clear();
        xEdmfulldatalist.clear();
        yEdmfulldatalist.clear();
        fullWirenamelist.clear();
        fullFilenamelist.clear();
        xUserdatalist.clear();
        yUserdatalist.clear();
        xEdmdatalist.clear();
        yEdmdatalist.clear();
        Wirenamelist.clear();
        Filenamelist.clear();
		
        for(int i=0;i<datatable.getRowCount();i++){
            xUserfulldatalist.add((Double)datatable.getValueAt(i,4));
            yUserfulldatalist.add((Double)datatable.getValueAt(i,5));
            xEdmfulldatalist.add((Double)datatable.getValueAt(i,2));
            yEdmfulldatalist.add((Double)datatable.getValueAt(i,3));
            fullWirenamelist.add((String)datatable.getValueAt(i,1));
            fullFilenamelist.add((String)datatable.getValueAt(i,0));
            if ((Boolean)datatable.getValueAt(i,6)){
				Wirenamelist.add((String)datatable.getValueAt(i,1));
            }
        }
		buildseq();
	    Iterator<String> itr = Wirenamelist.iterator();
		ArrayList<AcceleratorNode> wirenodelist = new ArrayList<AcceleratorNode>();
		while(itr.hasNext()){
			String name  = itr.next();
			AcceleratorNode wirenode = seq.getNodeWithId(name);
			wirenodelist.add(wirenode);
		}
		
		Wirenamelist.clear();
		seq.sortNodes(wirenodelist);
		Iterator<AcceleratorNode> newitr = wirenodelist.iterator();
		while(newitr.hasNext()){
			String name  = (newitr.next()).getId();
			for(int i=0;i<datatable.getRowCount();i++){
				if(((String)datatable.getValueAt(i,1)).equals(name)){
					xUserdatalist.add((Double)datatable.getValueAt(i,4));
					yUserdatalist.add((Double)datatable.getValueAt(i,5));
					xEdmdatalist.add((Double)datatable.getValueAt(i,2));
					yEdmdatalist.add((Double)datatable.getValueAt(i,3));
					Wirenamelist.add((String)datatable.getValueAt(i,1));
					Filenamelist.add((String)datatable.getValueAt(i,0));
                }
			}
		}
    }
    
	
    class TargetObjective extends Objective{
		protected final double _target;
        public TargetObjective(final String name, final double target){
            super(name);
            _target=target;
        }
        public double satisfaction(double value){
            double error = _target - value;
            return 1.0/(1+error*error);
        }
    }
    
	class Evaluator1 implements Evaluator{
		protected ArrayList<Objective> _objectives;
		protected ArrayList<Variable> _variables;
		public Evaluator1( final ArrayList<Objective> objectives, final ArrayList<Variable> variables ) {
            _objectives = objectives;
            _variables = variables;
		}
		
		public void evaluate(final Trial trial){
            double error =0.0;
            Iterator<Objective> itr = _objectives.iterator();
            while(itr.hasNext()){
                TargetObjective objective = (TargetObjective)itr.next();
                error = calcError(_variables, trial);
				if ( Double.isNaN( error ) ) {
//					System.out.println( "Veto trial with NaN error..." );
					trial.vetoTrial( new TrialVeto( trial, null, "NaN error" ) );
				}
				else {
//					System.out.println( "Setting score for error: " + error );
					trial.setScore(objective, error);
				}
            }
            
        }
    }
    
}
