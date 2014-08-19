/*
 * MatchingFace.java
 *
 */
package xal.app.beam_matcher;

import xal.tools.apputils.EdgeLayout;
import xal.tools.beam.PhaseVector;
import xal.tools.beam.Twiss;
import xal.extension.widgets.plot.BasicGraphData;
import xal.extension.widgets.plot.FunctionGraphsJPanel;
import xal.extension.widgets.plot.GridLimits;
import xal.extension.solver.Evaluator;
import xal.extension.solver.Objective;
import xal.extension.solver.Problem;
import xal.extension.solver.SolveStopperFactory;
import xal.extension.solver.Solver;
import xal.extension.solver.algorithm.DirectedStep;
import xal.extension.solver.Stopper;
import xal.extension.solver.Trial;
import xal.extension.solver.Variable;
import xal.extension.solver.algorithm.RandomShrinkSearch;
import xal.extension.solver.hint.*;
import xal.extension.widgets.swing.DecimalField;
import java.text.NumberFormat;
import xal.sim.scenario.AlgorithmFactory;



import xal.ca.*;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import java.awt.*;
import java.awt.event.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import Jama.Matrix;

import xal.model.ModelException;
import xal.tools.beam.CovarianceMatrix;
import xal.model.probe.Probe;
import xal.model.probe.EnvelopeProbe;
import xal.sim.scenario.ProbeFactory;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.ProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;
import xal.smf.AcceleratorSeqCombo;
import xal.smf.impl.Electromagnet;



/**
 * Peforms the matching operations
 * @author  Sarah Cousineau
 * TODO Don't make a bunch of accelerator objects
 */

public class MatchingFace extends JPanel{
    
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    
    public static final String STR_ID = "HEBT1";
    
    public AcceleratorSeqCombo seq;
    
    Accelerator accl;
    
    EdgeLayout layout = new EdgeLayout();
    
    JScrollPane bMatcherQuadPane;
    JScrollPane bMatcherWireScanPane;
    JScrollPane bMatcherFieldLimitsPane;
    private JTable quadTable;
    private JTable wireScanTable;
    private JTable fieldLimitsTable;
    private DataTableModel beamMatcherDataTableModel;
    private DataTableModel beamMatcherDataTableModel2;
    private DataTableModel beamMatcherDataTableModel3;
    private String[] bMatcherSelColNames = {"Quadrupole", "Field Value"};
    private String[] bMatcherSelColNames2 = {"WireScanner Name", "X Value","Y Value"};
    private String[] bMatcherSelColNames3 = {"Quadrupole", "Max % changed", "Defined Upper Field Limit", "Abs Upper Field Limit"};
    
    JPanel mainPanel;
    
    WireScanData wireScanData = new WireScanData();
    GenDocument doc;
    Scenario model;
    EnvelopeProbe probe;
    //Trajectory<?> traj;
    ProbeState<?> state;
    PhaseVector coordinates;
    ArrayList<Variable> variables = new ArrayList<Variable>();
    ArrayList<Objective> objectives = new ArrayList<Objective>();
    Evaluator1 evaluator;
    Problem problem;
    Solver solver;
    AcceleratorNode[] wsArray = new AcceleratorNode[4];
    int numNodes = 6;
    String[] quadNames = {"SCL_Mag:QH30", "SCL_Mag:QV30","SCL_Mag:QH32","SCL_Mag:QV32","SCL_Mag:QH33","HEBT_Mag:QV01"};
    
    
    JRadioButton xALRadioButton;
    JRadioButton channelAccessRadioButton;
    JButton copyButton;
    ButtonGroup selectorButtonGroup;
    JButton modelrunner;
    JButton matrixButton;
    JButton runSolverButton;
    JButton testWSDButton;
    JButton solveButton;
    JButton updateGraph;
    JButton settwissbutton;
    
    JLabel solverLabel;
    JLabel solveEvalNumLabel;
    JLabel fieldRangeLabel;
    
    DecimalField solverIterationNumber;
    DecimalField inputFieldRangePercent;
    DecimalField excursionHint;
    
    NumberFormat numForm = NumberFormat.getInstance();
    DecimalFormat decForm = new DecimalFormat("0.000000");
    DecimalFormat intForm = new DecimalFormat("0");
    
    double meritFunc;
    
    double fieldOne, fieldTwo, fieldThree, fieldFour, fieldFive, fieldSix;
    
    double one_fieldOne, one_fieldTwo, one_fieldThree, one_fieldFour, one_fieldFive, one_fieldSix, one_sigmaY1, one_sigmaY2, one_sigmaY3, one_sigmaY4, one_sigmaX1,
    
    one_sigmaX2, one_sigmaX3, one_sigmaX4, two_sigmaY1, two_sigmaY2, two_sigmaY3, two_sigmaY4;
    
    
    
    double sigmaX1,sigmaY1,sigmaX2,sigmaY2,sigmaX3,sigmaY3,sigmaX4,sigmaY4;
    
    double upLimOne,upLimTwo,upLimThree,upLimFour,upLimFive,upLimSix;
    
    double usrLim0,usrLim1,usrLim2,usrLim3,usrLim4,usrLim5;
    
    double alphax0;
    double betax0;
    double emitx0;
    double alphay0;
    double betay0;
    double emity0;
    double alphaz0;
    double betaz0;
    
    int trialNum;
    
    JFrame frame;
    
    /*Test objects for graph*/
    private FunctionGraphsJPanel plotpanel;
    GridLimits limits = new GridLimits();
    
    
    //Constructor
    public MatchingFace(GenDocument aDocument) {
        
        doc=aDocument;
        
        acclSetup();
        makeComponents();
        setLayout(layout);
        init();
        makeQuadTable();
        makeWireScanTable();
        makeFieldLimitsTable();
        setAction();
        
        addcomponents();
        
    }
    
    protected void makeComponents() {
        solverIterationNumber = new DecimalField(10, 6, intForm);
        inputFieldRangePercent = new DecimalField(15.0, 6, decForm);
        excursionHint = new DecimalField(0.20, 6, decForm);
        
        
    }
    //Initialization of components on AnalysisFace
    public void init(){
        
        
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(900, 850));
        
        
        
        // Table Stuff Jul 27
        beamMatcherDataTableModel = new DataTableModel(bMatcherSelColNames, 0);
        quadTable = new JTable(beamMatcherDataTableModel);
        bMatcherQuadPane = new JScrollPane(quadTable,
                                           JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                           JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bMatcherQuadPane.setPreferredSize(new Dimension(550, 130));
        
        
        beamMatcherDataTableModel2 = new DataTableModel(bMatcherSelColNames2, 0);
        wireScanTable = new JTable(beamMatcherDataTableModel2);
        bMatcherWireScanPane = new JScrollPane(wireScanTable,
                                               JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                               JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bMatcherWireScanPane.setPreferredSize(new Dimension(550, 120));
        
        beamMatcherDataTableModel3 = new DataTableModel(bMatcherSelColNames3, 0);
        fieldLimitsTable = new JTable(beamMatcherDataTableModel3);
        bMatcherFieldLimitsPane = new JScrollPane(fieldLimitsTable,
                                                  JScrollPane.VERTICAL_SCROLLBAR_NEVER,
                                                  JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        bMatcherFieldLimitsPane.setPreferredSize(new Dimension(550, 130));
        
        
        copyButton = new JButton("Copy Field Values to Trial Values");
        selectorButtonGroup = new ButtonGroup();
        xALRadioButton = new JRadioButton("Use XAL Methods", false);
        selectorButtonGroup.add(xALRadioButton);
        channelAccessRadioButton = new JRadioButton("Use Channel Access", true);
        selectorButtonGroup.add(channelAccessRadioButton);
        
        modelrunner = new JButton("Run the Model");
        matrixButton = new JButton("Matrix Development");
        runSolverButton = new JButton("Run the Solver");
        testWSDButton = new JButton("Test Wire Scanner class");
        updateGraph = new JButton("Update the Graph");
        
        solveEvalNumLabel = new JLabel("Number Of Evaluations (integer)");
        fieldRangeLabel = new JLabel("Excursion Hint for Directed Step");
        
        settwissbutton = new JButton("Set Twiss at SCL BPM29");
        
        //Set some sensible initial Twiss
        alphax0 = 11.2578;
        betax0 = 32.7035;
        emitx0 = 0.219;
        alphay0 = -2.1947;
        betay0 = 11.5882;
        emity0 = 0.251;
        alphaz0 = 10.535;
        betaz0 = 0.0352;
        
        /*Graph*/
        plotpanel = new FunctionGraphsJPanel();
        plotpanel.setPreferredSize(new Dimension(800, 230));
        plotpanel.setGraphBackGroundColor(Color.WHITE);
        
        frame = new JFrame("FrameDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        frame.pack();
        
    }
    
    
    
    //Set the action listeners
    public void setAction(){
        modelrunner.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                getModelWireScanData();
            }
            
        });
        matrixButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                getAllQuads();
            }
            
        });
        runSolverButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                try {
                    
                    runSolver();
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (GetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            
            
        });
        testWSDButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                double x,y;
                try {
                    
                    
                    if (channelAccessRadioButton.isSelected() == true) {
                        y = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS01");
                        System.out.println(y);
                        System.out.println("Got HERE!!");
                        x = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS01");
                        System.out.println(x);
                    }
                    else {
                        y = wireScanData.wireScanVertByName("HEBT_Diag:WS01");
                        System.out.println(y);
                        System.out.println("Got HERE!!");
                        x = wireScanData.wireScanHorizByName("HEBT_Diag:WS01");
                        System.out.println(x);
                    }
                    
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (GetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                
            }
        });
        
        /* Select to use the set field values */
        xALRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                
            }
        });
        
        /* Select to use the set field values */
        channelAccessRadioButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
            }
        });
        beamMatcherDataTableModel3.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent arg0) {
            }
        });
        updateGraph.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                try {
                    setupPlot();
                } catch (ConnectionException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (GetException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
        
        settwissbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                NumberFormat numfor = NumberFormat.getNumberInstance();
                numfor.setMinimumFractionDigits(3);
                final JDialog limitdialog = new JDialog();
                limitdialog.setLayout(new GridLayout(8,2));
                limitdialog.add(new JLabel("Twiss"));
                limitdialog.add(new JLabel("Value"));
                
                final JTextField alphaXinitial = new JTextField(10);
                final JTextField betaXinitial = new JTextField(10);
                final JTextField emitXinitial = new JTextField(10);
                final JTextField alphaYinitial = new JTextField(10);
                final JTextField betaYinitial = new JTextField(10);
                final JTextField emitYinitial = new JTextField(10);
                
                alphaXinitial.setText(numfor.format(alphax0));
                alphaYinitial.setText(numfor.format(alphay0));
                betaXinitial.setText(numfor.format(betax0));
                betaYinitial.setText(numfor.format(betay0));
                emitXinitial.setText(numfor.format(emitx0));
                emitYinitial.setText(numfor.format(emity0));
                limitdialog.add(new JLabel(" Alphax"));
                limitdialog.add(alphaXinitial);
                limitdialog.add(new JLabel(" Betax [m]         "));
                limitdialog.add(betaXinitial);
                limitdialog.add(new JLabel(" Emitx [pi-mm-mrad]"));
                limitdialog.add(emitXinitial);
                limitdialog.add(new JLabel(" Alphay            "));
                limitdialog.add(alphaYinitial);
                limitdialog.add(new JLabel(" Betay [m]         "));
                limitdialog.add(betaYinitial);
                limitdialog.add(new JLabel(" Emity [pi-mm-mrad]"));
                limitdialog.add(emitYinitial);
                JButton set = new JButton(" Set");
                
                limitdialog.add(set);
                limitdialog.pack();
                limitdialog.setVisible(true);
                set.addActionListener(new ActionListener(){
                    public void actionPerformed(ActionEvent e){
                        alphax0=Double.parseDouble(alphaXinitial.getText());
                        betax0=Double.parseDouble(betaXinitial.getText());
                        emitx0 = Double.parseDouble(emitXinitial.getText());
                        alphay0=Double.parseDouble(alphaYinitial.getText());
                        betay0 = Double.parseDouble(betaYinitial.getText());
                        emity0 = Double.parseDouble(emitYinitial.getText());
                        limitdialog.setVisible(false);
                    }
                });
            }
        });
        
    }
    
    
    //Add all components to the layout and panels
    public void addcomponents(){
        
        layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
        this.add(mainPanel);
        
        EdgeLayout newlayout = new EdgeLayout();
        mainPanel.setLayout(newlayout);
        
        mainPanel.add(modelrunner);
        
        newlayout.setConstraints(matrixButton,50, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(matrixButton);
        
        newlayout.setConstraints(runSolverButton,150, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(runSolverButton);
        
        newlayout.setConstraints(solverIterationNumber,200, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solverIterationNumber);
        
        newlayout.setConstraints(testWSDButton,240, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(testWSDButton);
        
        newlayout.setConstraints(excursionHint,300, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(excursionHint);
        
        mainPanel.setLayout(newlayout);
        
        /* Add primary BMatcher data display */
        newlayout.add(bMatcherQuadPane, mainPanel, 260, 0, EdgeLayout.LEFT);
        newlayout.add(bMatcherWireScanPane, mainPanel, 260, 150, EdgeLayout.LEFT);
        newlayout.add(bMatcherFieldLimitsPane, mainPanel, 260, 270, EdgeLayout.LEFT);
        newlayout.add(solveEvalNumLabel, mainPanel, 5, 185, EdgeLayout.LEFT);
        newlayout.add(fieldRangeLabel, mainPanel, 5, 285, EdgeLayout.LEFT);
        newlayout.add(xALRadioButton, mainPanel, 1, 360, EdgeLayout.LEFT);
        newlayout.add(channelAccessRadioButton, mainPanel, 1, 380, EdgeLayout.LEFT);
        
        /*add graph*/
        
        newlayout.setConstraints(updateGraph,410, 0, 0, 0, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(updateGraph);
        
        newlayout.setConstraints(plotpanel, 450, 0, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(plotpanel);
        
        newlayout.setConstraints(settwissbutton, 700, 30, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(settwissbutton);
        
        
        
        
    }
    double deriv;
    
    double dblPct1, dblPct2, dblPct3, dblPct4; {
        
        dblPct1 = 25.0;
        dblPct2 = 25.0;
        dblPct3 = 25.0;
        dblPct4 = 25.0;
    }
    
    public void getModelWireScanData() {
        ModelWireScanData modelWireScanData = new ModelWireScanData();
        try {
            
            model = modelWireScanData.virtualAcceleratorOne(1, 2, 3, 4, 0., 0., 0., 0.);
            
            ArrayList<Double> data = modelWireScanData.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
            System.out.println(data);
            
            model = modelWireScanData.virtualAcceleratorOne(1, 2, 3, 4, dblPct1, dblPct2, dblPct3, dblPct4);
            
            data = modelWireScanData.extractWsOneBeamSize(model, "HEBT_Diag:WS01");
            System.out.println(data);
            
            
        } catch (GetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    public void getAllQuads() {
        //              AcceleratorHardware hware = new AcceleratorHardware();
        //              System.out.println(hware.getAllQuadrupoles(STR_ID));
        try {
            Matrix(0.150000000000000);
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    
    
    public void acclSetup() {
        
        /* Set up the model */
        try {
            doc = GenDocument.getInstance();
            accl = doc.getAccelerator();
            ArrayList <AcceleratorSeq> seqList = new ArrayList<AcceleratorSeq>();
            seqList.add(accl.getSequence("SCLHigh"));
            seqList.add(accl.getSequence("HEBT1"));
            seq = AcceleratorSeqCombo.getInstance("HEBTCombo", seqList);
            
            try {
                
                probe = ProbeFactory.getEnvelopeProbe(seq, AlgorithmFactory.createEnvTrackerAdapt( seq ));
                
            } catch ( InstantiationException exception ) {
                System.err.println( "Instantiation exception creating probe." );
                exception.printStackTrace();
            }
            
            model = Scenario.newScenarioFor(seq);
            model.setStartElementId("SCL_Diag:BPM29");
            model.setSynchronizationMode(Scenario.SYNC_MODE_RF_DESIGN);
            model.setProbe(probe);
            model.resync();
            
            
            wsArray[0] = seq.getNodeWithId("HEBT_Diag:WS01");
            wsArray[1] = seq.getNodeWithId("HEBT_Diag:WS02");
            wsArray[2] = seq.getNodeWithId("HEBT_Diag:WS03");
            wsArray[3] = seq.getNodeWithId("HEBT_Diag:WS04");
            
        }
        catch (ModelException e) {
            System.out.println(e);
        }
        
    }
    
    public void makeQuadTable() {
        quadTable.getColumnModel().getColumn(0).setPreferredWidth(75);
        quadTable.getColumnModel().getColumn(1).setPreferredWidth(75);
        quadTable.setPreferredScrollableViewportSize(quadTable.getPreferredSize());
        quadTable.setRowSelectionAllowed(false);
        quadTable.setColumnSelectionAllowed(false);
        quadTable.setCellSelectionEnabled(false);
        beamMatcherDataTableModel.fireTableDataChanged();
        for (int i = 0; i < numNodes; i++) {
            ArrayList<Object> tableData = new ArrayList<Object>();
            tableData.add((seq.getNodeWithId(quadNames[i])).getId());
            try {
                tableData.add(decForm.format((((Electromagnet)seq.getNodeWithId(quadNames[i])).getField())));
                
            } catch (ConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            beamMatcherDataTableModel.addTableData(tableData);
        }
        beamMatcherDataTableModel.fireTableDataChanged();
        
    }
    /**
     * Code for making the Table containing the wireScanner Values
     */
    public void makeWireScanTable() {
        wireScanTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        wireScanTable.getColumnModel().getColumn(1).setPreferredWidth(80);
        wireScanTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        
        /* Populate the wScanTable with data   (or try to) */
        for (int i = 0; i < 4; i++) {
            ArrayList<Object> tableData2 = new ArrayList<Object>();
            tableData2.add(wsArray[i].getId());
            
            try {
                tableData2.add(decForm.format(wireScanData.wireScanHorizByNameCA(wsArray[i].getId())));
                tableData2.add(decForm.format(wireScanData.wireScanVertByNameCA(wsArray[i].getId())));
            } catch (ConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            beamMatcherDataTableModel2.addTableData(tableData2);
        }
        beamMatcherDataTableModel2.fireTableDataChanged();
    }
    
    
    
    public void makeFieldLimitsTable() {
        fieldLimitsTable.getColumnModel().getColumn(0).setPreferredWidth(10);
        fieldLimitsTable.getColumnModel().getColumn(1).setPreferredWidth(30);
        fieldLimitsTable.getColumnModel().getColumn(2).setPreferredWidth(50);
        fieldLimitsTable.getColumnModel().getColumn(3).setPreferredWidth(32);
        fieldLimitsTable.setEditingColumn(1);
        usrLim0 = usrLim1 = usrLim2 = usrLim3 = usrLim4 = usrLim5 = inputFieldRangePercent.getDoubleValue();
        Double[] usrInput = {usrLim0,usrLim1,usrLim2,usrLim3,usrLim4,usrLim5};
        
        
        for (int i = 0; i < numNodes; i++) {
            ArrayList<Object> tableData = new ArrayList<Object>();
            tableData.add((seq.getNodeWithId(quadNames[i])).getId());
            double pct;
            tableData.add(usrInput[i]);
            try {
                tableData.add(((1+(usrInput[i])*.01))*((Electromagnet)seq.getNodeWithId(quadNames[i])).getField());
                
                tableData.add((((Electromagnet)seq.getNodeWithId(quadNames[i])).upperFieldLimit()));
            } catch (ConnectionException e) {
                e.printStackTrace();
            } catch (GetException e) {
                e.printStackTrace();
            }
            beamMatcherDataTableModel3.addTableData(tableData);
        }
        beamMatcherDataTableModel3.fireTableDataChanged();
    }
    
    
    
    
    protected void runSolver() throws ConnectionException, GetException {
        trialNum = 0;
        beamMatcherDataTableModel3.fireTableDataChanged();
        double percent1 = 0.01*((Double)fieldLimitsTable.getValueAt(0,1)).doubleValue();
        double percent2 = 0.01*((Double)fieldLimitsTable.getValueAt(1,1)).doubleValue();
        double percent3 = 0.01*((Double)fieldLimitsTable.getValueAt(2,1)).doubleValue();
        double percent4 = 0.01*((Double)fieldLimitsTable.getValueAt(3,1)).doubleValue();
        double percent5 = 0.01*((Double)fieldLimitsTable.getValueAt(4,1)).doubleValue();
        double percent6 = 0.01*((Double)fieldLimitsTable.getValueAt(5,1)).doubleValue();
        
        
        double fieldRangePercent = (0.01*(inputFieldRangePercent.getDoubleValue()));
        Double[] usrInput = {usrLim0,usrLim1,usrLim2,usrLim3,usrLim4,usrLim5};
        
        for(int i = 0;i<beamMatcherDataTableModel3.getRowCount();i++){
            double valueMag = ((Double)beamMatcherDataTableModel3.getValueAt(i,1)).doubleValue();
            usrInput[i] = valueMag;
        }
        usrLim0 = usrInput[0];
        usrLim1 = usrInput[1];
        usrLim2 = usrInput[2];
        usrLim3 = usrInput[3];
        usrLim4 = usrInput[4];
        usrLim5 = usrInput[5];
        
        fieldOne = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH30")).getField();
        fieldTwo = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV30")).getField();
        fieldThree = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH32")).getField();
        fieldFour = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV32")).getField();
        fieldFive = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH33")).getField();
        fieldSix = ((Electromagnet)seq.getNodeWithId("HEBT_Mag:QV01")).getField();
        
        
        upLimOne = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH30")).upperFieldLimit();
        upLimTwo = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV30")).upperFieldLimit();
        upLimThree = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH32")).upperFieldLimit();
        upLimFour = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV32")).upperFieldLimit();
        upLimFive = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH33")).upperFieldLimit();
        upLimSix = ((Electromagnet)seq.getNodeWithId("HEBT_Mag:QV01")).upperFieldLimit();
        
        usrLim0 = (1 + usrLim0)*.01*fieldOne;
        usrLim1 = (1 + usrLim1)*.01*fieldTwo;
        usrLim2 = (1 + usrLim2)*.01*fieldThree;
        usrLim3 = (1 + usrLim3)*.01*fieldFour;
        usrLim4 = (1 + usrLim4)*.01*fieldFive;
        usrLim5 = (1 + usrLim5)*.01*fieldSix;
        
        
        String err0 = "" , err1 = "" ,err2 = "" ,err3 = "",err4 = "",err5 = "";
        
        if (upLimOne>usrLim0){upLimOne = usrLim0;}
        else{ upLimOne = (upLimOne*0.99);
            err0 = "Lower Field Range % on SCL_Mag:QH30";
        }
        if (upLimTwo>usrLim1) {upLimTwo = usrLim1;}
        else{ upLimTwo = (upLimTwo*0.99);
            err1 = "Lower Field Range % on SCL_Mag:QV30";
        }
        if (upLimThree>usrLim2) {upLimThree = usrLim2;}
        else{ upLimThree = (upLimThree*0.99);
            err2 = "Lower Field Range % on SCL_Mag:QH32";
        }
        if (upLimFour>usrLim3) {upLimFour = usrLim3;}
        else{ upLimFour = (upLimFour*0.99);
            err3 = "Lower Field Range % on SCL_Mag:QV32";
        }
        if (upLimFive>usrLim4) {upLimFive = usrLim4;}
        else{ upLimFive = (upLimFive*0.99);
            err4 = "Lower Field Range % on SCL_Mag:QH33";
        }
        if (upLimSix>usrLim5) {upLimSix = usrLim5;}
        else{ upLimSix = (upLimSix*0.99);
            err5 = "Lower Field Range % on HEBT_Mag:QV01";
        }
        
        if (err0 != "" || err1 != "" || err2 != "" || err3 != "" || err4 != "" || err5 != "") {
            JOptionPane.showMessageDialog(frame,err0 + "\n" + err1 + "\n" + err2 + "\n" + err3 + "\n" + err4 + "\n" + err5);
            
        }
        
        System.out.println("Upper One " + upLimOne);
        System.out.println("Upper Two " + upLimTwo);
        System.out.println("Upper Three " + upLimThree);
        System.out.println("Upper Four " + upLimFour);
        System.out.println("Upper Five " + upLimFive);
        System.out.println("Upper Six " + upLimSix);
        
        /* Resync to flush any cached field values */
        try {
            model.resync();
        }
        catch (ModelException e) {
            System.out.println(e);
        }
        
        /* Determine which Quadruples will be variables for the solver */
        variables.clear();
        
        Variable var1 = new Variable("SCL_Mag:QH30", fieldOne,
                                     fieldOne * (1 + percent1), fieldOne * (1 - percent1));
        variables.add(var1);
        Variable var2 = new Variable("SCL_Mag:QV30", fieldTwo,
                                     fieldTwo * (1 - percent2), fieldTwo * (1 + percent2));
        variables.add(var2);
        Variable var3 = new Variable("SCL_Mag:QH32", fieldThree,
                                     fieldThree * (1 + percent3), fieldThree * (1 - percent3));
        variables.add(var3);
        Variable var4 = new Variable("SCL_Mag:QV32", fieldFour,
                                     fieldFour * (1 - percent4), fieldFour * (1 + percent4));
        variables.add(var4);
        Variable var5 = new Variable("SCL_Mag:QH33", fieldFive,
                                     fieldFive * (1 + percent5), fieldFive * (1 - percent5));
        variables.add(var5);
        Variable var6 = new Variable("HEBT_Mag:QV01", fieldSix,
                                     fieldSix * (1 - percent6), fieldSix* (1 + percent6));
        variables.add(var6);
        
        
        
        
        
        /* Alert the user that no variables have been selected */
        if (variables.isEmpty()) {
            //JOptionPane errorAlert = new JOptionPane();
            JOptionPane.showMessageDialog(null, "Must select magnets to vary.",
                                          "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        /* Set solver objective */
        objectives.add(new TargetObjective("Target Error", 0.00000000));
        
        /* Run the solver */
        evaluator = new Evaluator1(objectives, variables);
        problem = new Problem(objectives, variables, evaluator);
        Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper(0);
        maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper((int) solverIterationNumber.getValue());
        solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper);
        new Thread() {
            public void run() {
                solver.solve(problem);
                System.out.println(solver.getScoreBoard());
                Trial best = solver.getScoreBoard().getBestSolution();
                calcError(variables, best);
                
            }
        }.start();
        
        /* Run the solver with directed step*/
        //evaluator = new Evaluator1(objectives, variables);
        //problem = new Problem(objectives, variables, evaluator);
        //Stopper maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper((int) 0);
        //maxSolutionStopper = SolveStopperFactory.maxEvaluationsStopper((int) solverIterationNumber.getValue());
        //solver = new Solver( new DirectedStep(), maxSolutionStopper);
        //problem.addHint(ExcursionHint.getFractionalExcursionHint(excursionHint.getValue()));
        //new Thread() {
        //    public void run() {
        //          solver.solve(problem);
        //        System.out.println(solver.getScoreBoard());
        //      Trial best = solver.getScoreBoard().getBestSolution();
        //    calcError(variables, best);
        
        //}
        //}.start();
        
        
    }
    
    
    
    
    /* Method for evaluating the accuracy of the solver */
    
    public double calcError(ArrayList<Variable> vars, Trial trial) {
        updateVariables(vars, trial);
        //              JOptionPane.showMessageDialog(frame, "Iteration complete.");
        try {
            if (channelAccessRadioButton.isSelected() == true) {
                System.out.println("Used CA");
                sigmaY1 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS01");
                sigmaY2 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS02");
                sigmaY3 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS03");
                sigmaY4 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS04");
                
                sigmaX1 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS01");
                sigmaX2 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS02");
                sigmaX3 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS03");
                sigmaX4 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS04");
            }
            else {
                System.out.println("Used XAL");
                sigmaY1 = wireScanData.wireScanVertByName("HEBT_Diag:WS01");
                sigmaY2 = wireScanData.wireScanVertByName("HEBT_Diag:WS02");
                sigmaY3 = wireScanData.wireScanVertByName("HEBT_Diag:WS03");
                sigmaY4 = wireScanData.wireScanVertByName("HEBT_Diag:WS04");
                
                sigmaX1 = wireScanData.wireScanHorizByName("HEBT_Diag:WS01");
                sigmaX2 = wireScanData.wireScanHorizByName("HEBT_Diag:WS02");
                sigmaX3 = wireScanData.wireScanHorizByName("HEBT_Diag:WS03");
                sigmaX4 = wireScanData.wireScanHorizByName("HEBT_Diag:WS04");
            }
            
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        meritFunc = (sigmaX1 - sigmaX3)*(sigmaX1 - sigmaX3) + (sigmaX2 - sigmaX4)*(sigmaX2 - sigmaX4) + (sigmaY1 - sigmaY3)*(sigmaY1 - sigmaY3) + (sigmaY2 - sigmaY4)*(sigmaY2 - sigmaY4);
        
        
        
        
        beamMatcherDataTableModel.clearAllData();
        beamMatcherDataTableModel2.clearAllData();
        
        for (int i = 0; i < numNodes; i++) {
            ArrayList<Object> tableData = new ArrayList<Object>();
            tableData.add((seq.getNodeWithId(quadNames[i])).getId());
            try {
                tableData.add(decForm.format((((Electromagnet)seq.getNodeWithId(quadNames[i])).getField())));
                
            } catch (ConnectionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (GetException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            beamMatcherDataTableModel.addTableData(tableData);
            
        }
        
        for (int i = 0; i < 4; i++) {
            
            ArrayList<Object> tableData2 = new ArrayList<Object>();
            tableData2.add(wsArray[i].getId());
            
            try {
                if (channelAccessRadioButton.isSelected() == true) {
                    tableData2.add(decForm.format(wireScanData.wireScanHorizByNameCA(wsArray[i].getId())));
                    
                    tableData2.add(decForm.format(wireScanData.wireScanVertByNameCA(wsArray[i].getId())));
                    
                }
                else {
                    tableData2.add(decForm.format(wireScanData.wireScanHorizByName(wsArray[i].getId())));
                    
                    tableData2.add(decForm.format(wireScanData.wireScanVertByName(wsArray[i].getId())));
                    
                }
            } catch (ConnectionException e) {
                e.printStackTrace();
            } catch (GetException e) {
                e.printStackTrace();
            }
            
            beamMatcherDataTableModel2.addTableData(tableData2);
        }
        beamMatcherDataTableModel2.fireTableDataChanged();
        beamMatcherDataTableModel.fireTableDataChanged();
        
        System.out.println(meritFunc);
        
        return meritFunc;
        
    }
    
    
    /* Method for updating variables for the solver */
    void updateVariables(ArrayList<Variable> vars, Trial trial) {
        Iterator<Variable> itr = vars.iterator();
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            System.out.println("Magnetic Field Value: " + value);
            String name = variable.getName();
            for (int i = 0; i < numNodes; i++) {
                
                if (name.equals(quadNames[i])) {
                    try {
                        ((Electromagnet)seq.getNodeWithId(quadNames[i])).setField(value);
                    } catch (ConnectionException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (PutException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                    
                }
                
            }
        }
        
        //              try {
        //                    Thread.sleep(5000);
        //          } catch (InterruptedException e1) {
        //                // TODO Auto-generated catch block
        //              e1.printStackTrace();
        //    }
        JOptionPane.showMessageDialog(frame, "Iteration completed. Error: " + meritFunc + "\nTrial Number: " + trialNum );
        System.out.println("Iteration completed. Error: " + meritFunc + "\nTrial Number: " + trialNum);
        
        trialNum = trialNum + 1;
        
        try {
            setupPlot();
        } catch (ConnectionException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (GetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
    
    /* Evaluator class for the solver */
    class Evaluator1 implements Evaluator{
        
        protected ArrayList<Variable> _variables;
        protected ArrayList<Objective> _objectives;
        
        public Evaluator1(final ArrayList<Objective> objectives, final ArrayList<Variable> variables) {
            _objectives = objectives;
            _variables = variables;
        }
        
        public void evaluate(final Trial trial){
            double error = 0.0;
            Iterator<Objective> itr = _objectives.iterator();
            while(itr.hasNext()){
                TargetObjective objective = (TargetObjective)itr.next();
                error = calcError(_variables, trial);
                trial.setScore(objective, error);
            }
            
        }
    }
    
    
    /* Objective class for the solver */
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
    double[] lstDerivatives = new double[25];
    public void Matrix(double h) throws ConnectionException, GetException {
        
        
        one_fieldOne = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH30")).getField();
        one_fieldTwo = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV30")).getField();
        one_fieldThree = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH32")).getField();
        one_fieldFour = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QV32")).getField();
        one_fieldFive = ((Electromagnet)seq.getNodeWithId("SCL_Mag:QH33")).getField();
        one_fieldSix = ((Electromagnet)seq.getNodeWithId("HEBT_Mag:QV01")).getField();
        
        Double[] lstFields = {one_fieldOne, one_fieldTwo, one_fieldThree, one_fieldFour, one_fieldFive, one_fieldSix};
        
        if (channelAccessRadioButton.isSelected() == true) {
            one_sigmaY1 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS01");
            one_sigmaY2 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS02");
            one_sigmaY3 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS03");
            one_sigmaY4 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS04");
            
            one_sigmaX1 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS01");
            one_sigmaX2 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS02");
            one_sigmaX3 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS03");
            one_sigmaX4 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS04");
        }
        
        else {
            one_sigmaY1 = wireScanData.wireScanVertByName("HEBT_Diag:WS01");
            one_sigmaY2 = wireScanData.wireScanVertByName("HEBT_Diag:WS02");
            one_sigmaY3 = wireScanData.wireScanVertByName("HEBT_Diag:WS03");
            one_sigmaY4 = wireScanData.wireScanVertByName("HEBT_Diag:WS04");
            
            one_sigmaX1 = wireScanData.wireScanHorizByName("HEBT_Diag:WS01");
            one_sigmaX2 = wireScanData.wireScanHorizByName("HEBT_Diag:WS02");
            one_sigmaX3 = wireScanData.wireScanHorizByName("HEBT_Diag:WS03");
            one_sigmaX4 = wireScanData.wireScanHorizByName("HEBT_Diag:WS04");
            
        }
        
        
        for (int i = 0; i < numNodes; i++) {
            try {
                System.out.println(quadNames[i]);
                ((Electromagnet)seq.getNodeWithId(quadNames[i])).setField(lstFields[i]*(1.+h));
                JOptionPane.showMessageDialog(frame, "Press OK.");
                
                
                if (channelAccessRadioButton.isSelected() == true) {
                    two_sigmaY1 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS01");
                    two_sigmaY2 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS02");
                    two_sigmaY3 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS03");
                    two_sigmaY4 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS04");
                }
                else {
                    two_sigmaY1 = wireScanData.wireScanVertByName("HEBT_Diag:WS01");
                    two_sigmaY2 = wireScanData.wireScanVertByName("HEBT_Diag:WS02");
                    two_sigmaY3 = wireScanData.wireScanVertByName("HEBT_Diag:WS03");
                    two_sigmaY4 = wireScanData.wireScanVertByName("HEBT_Diag:WS04");
                }
                if (i == 0) {
                    lstDerivatives[1] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldOne);
                    lstDerivatives[2] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldOne);
                    lstDerivatives[3] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldOne);
                    lstDerivatives[4] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldOne);
                }
                
                if (i == 1) {
                    lstDerivatives[5] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldTwo);
                    lstDerivatives[6] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldTwo);
                    lstDerivatives[7] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldTwo);
                    lstDerivatives[8] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldTwo);
                }
                
                if (i == 2) {
                    lstDerivatives[9] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldThree);
                    lstDerivatives[10] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldThree);
                    lstDerivatives[11] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldThree);
                    lstDerivatives[12] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldThree);
                }
                
                if ( i == 3) {
                    lstDerivatives[13] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldFour);
                    lstDerivatives[14] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldFour);
                    lstDerivatives[15] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldFour);
                    lstDerivatives[16] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldFour);
                }
                if ( i == 4) {
                    lstDerivatives[17] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldFive);
                    lstDerivatives[18] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldFive);
                    lstDerivatives[19] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldFive);
                    lstDerivatives[20] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldFive);
                }
                if ( i == 5) {
                    lstDerivatives[21] = Derivative(h, one_sigmaY1, two_sigmaY1, one_fieldSix);
                    lstDerivatives[22] = Derivative(h, one_sigmaY2, two_sigmaY2, one_fieldSix);
                    lstDerivatives[23] = Derivative(h, one_sigmaY3, two_sigmaY3, one_fieldSix);
                    lstDerivatives[24] = Derivative(h, one_sigmaY4, two_sigmaY4, one_fieldSix);
                }
                Thread.sleep(1000);
                
                ((Electromagnet)seq.getNodeWithId(quadNames[i])).setField(lstFields[i]);
            } catch (PutException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        
        double[][] avals = {{lstDerivatives[1],lstDerivatives[2],lstDerivatives[3],lstDerivatives[4]},{lstDerivatives[5],lstDerivatives[6],lstDerivatives[7],lstDerivatives[8]},
            
            {lstDerivatives[9],lstDerivatives[10],lstDerivatives[11],lstDerivatives[12]},{lstDerivatives[13],lstDerivatives[14],lstDerivatives[15],lstDerivatives[16]}
            
            ,{lstDerivatives[17],lstDerivatives[18],lstDerivatives[19],lstDerivatives[20]},{lstDerivatives[21],lstDerivatives[22],lstDerivatives[23],lstDerivatives[24]}};
        
        Matrix A = new Matrix(avals);
        A.print(30,15);
    }
    
    public double Derivative(double h, double sigma_initial, double sigma_after, double field) {
        deriv = ((sigma_after - sigma_initial)/(h*field));
        
        return deriv;
    }
    
    public void setupPlot() throws ConnectionException, GetException {
        plotpanel.removeAllGraphData();
        BasicGraphData hgraphdata = new BasicGraphData();
        BasicGraphData vgraphdata = new BasicGraphData();
        BasicGraphData xgraphdata = new BasicGraphData();
        BasicGraphData ygraphdata = new BasicGraphData();
        
        ArrayList<Double> sdata = new ArrayList<Double>();
        ArrayList<Double> hdata = new ArrayList<Double>();
        ArrayList<Double> vdata = new ArrayList<Double>();
        
        ArrayList<String> namelist = new ArrayList<String>();
        namelist.add("HEBT_Diag:WS01");
        namelist.add("HEBT_Diag:WS02");
        namelist.add("HEBT_Diag:WS03");
        namelist.add("HEBT_Diag:WS04");
        
        String[] lstName = {"HEBT_Diag:WS01", "HEBT_Diag:WS02", "HEBT_Diag:WS03", "HEBT_Diag:WS04"};
        
        Twiss xTwiss = new Twiss(alphax0, betax0, emitx0*1e-6);
        Twiss yTwiss = new Twiss(alphay0, betay0, emity0*1e-6);
        Twiss zTwiss = new Twiss(alphaz0, betaz0, 11.4e-3);
        Twiss[] tw = new Twiss[3];
        tw[0]=xTwiss;
        tw[1]=yTwiss;
        tw[2]=zTwiss;
        probe.setKineticEnergy(925.e6);
        probe.reset();
        probe.initFromTwiss(tw);
        
        try {
            model.resync();
            model.run();
        } catch (ModelException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        //              if (channelAccessRadioButton.isSelected() == true) {
        //                      one_sigmaY1 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS01");
        //                      one_sigmaY2 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS02");
        //                      one_sigmaY3 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS03");
        //                      one_sigmaY4 = wireScanData.wireScanVertByNameCA("HEBT_Diag:WS04");
        //
        //                      one_sigmaX1 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS01");
        //                      one_sigmaX2 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS02");
        //                      one_sigmaX3 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS03");
        //                      one_sigmaX4 = wireScanData.wireScanHorizByNameCA("HEBT_Diag:WS04");
        //              }
        //
        //              else {
        //                      one_sigmaY1 = wireScanData.wireScanVertByName("HEBT_Diag:WS01");
        //                      one_sigmaY2 = wireScanData.wireScanVertByName("HEBT_Diag:WS02");
        //                      one_sigmaY3 = wireScanData.wireScanVertByName("HEBT_Diag:WS03");
        //                      one_sigmaY4 = wireScanData.wireScanVertByName("HEBT_Diag:WS04");
        //
        //                      one_sigmaX1 = wireScanData.wireScanHorizByName("HEBT_Diag:WS01");
        //                      one_sigmaX2 = wireScanData.wireScanHorizByName("HEBT_Diag:WS02");
        //                      one_sigmaX3 = wireScanData.wireScanHorizByName("HEBT_Diag:WS03");
        //                      one_sigmaX4 = wireScanData.wireScanHorizByName("HEBT_Diag:WS04");
        //
        //              }
        Trajectory<EnvelopeProbeState> traj = probe.getTrajectory();
        //              System.out.println("trajectory is = "  traj);
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
        //              String[] lstWS = {"HEBT_Diag:WS01","HEBT_Diag:WS02","HEBT_Diag:WS03","HEBT_Diag:WS04"};
        int i;
        //              for (i = 0; i < 4; i){
        //                      System.out.println(lstWS[i]);
        //                      double rx = wireScanData.wireScanHorizByNameCA(lstWS[i]);
        //                      double ry = wireScanData.wireScanVertByNameCA(lstWS[i]);
        //                      hdata.add(rx);
        //                      vdata.add(ry);
        //              }
        
        int size = sdata.size() - 1;
        
        double[] s = new double[size];
        double[] x = new double[size];
        double[] y = new double[size];
        
        for(i=0; i<size; i++){
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
        //limits.setSmartLimits();
        //limits.setXmax(14.0);
        plotpanel.setExternalGL(limits);
        
        plotpanel.addGraphData(hgraphdata);
        plotpanel.addGraphData(vgraphdata);
        
        int datasize = namelist.size();
        //int size = namelist.size();
        double[] srdata = new double[datasize];
        double[] xrdata = new double[datasize];
        double[] yrdata = new double[datasize];
        
        //what is the point of this line?
        //I commented it out since traj is instantiated earlier in this method.
        // - Jonathan M. Freed
        //traj = probe.getTrajectory();
        
        EnvelopeProbeState newstate;
        Twiss[] newtwiss;
        double rx, ry;
        for(i =0; i<datasize; i++){
            newstate = traj.statesForElement(namelist.get(i)).get(0);
            srdata[i]=newstate.getPosition();
            System.out.println(beamMatcherDataTableModel2.getValueAt(i, 1));
            xrdata[i]=wireScanData.wireScanHorizByNameCA(lstName[i]);
            yrdata[i]=wireScanData.wireScanVertByNameCA(lstName[i]);
            System.out.println("For " + namelist.get(i) + "pos is " + newstate.getPosition());
        }
        xgraphdata.addPoint(srdata, xrdata);
        xgraphdata.setDrawPointsOn(true);
        xgraphdata.setDrawLinesOn(false);
        xgraphdata.setGraphColor(Color.RED);
        ygraphdata.addPoint(srdata, yrdata);
        ygraphdata.setDrawPointsOn(true);
        ygraphdata.setDrawLinesOn(false);
        ygraphdata.setGraphColor(Color.BLUE);
        
        plotpanel.addGraphData(xgraphdata);
        plotpanel.addGraphData(ygraphdata);
        limits.setSmartLimits();
        //limits.setXmin(srdata[0] - 2.0);
        plotpanel.setExternalGL(limits);
        
    }
    
    
    
}
