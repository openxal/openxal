/*
 * ControlFace.java
 *
 * Created on June 26, 2007, 1:49 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

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

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.ca.*;
import xal.tools.*;
import xal.tools.beam.*;
import xal.tools.xml.*;
import xal.tools.data.*;
import xal.tools.messaging.*;

import java.util.HashMap;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.apputils.*;
import xal.model.probe.traj.*;
import xal.model.probe.*;
import xal.model.xml.*;
import xal.sim.scenario.*;
import xal.model.alg.*;
import xal.service.pvlogger.*;
import xal.extension.solver.*;

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


//import com.sun.org.apache.xpath.internal.operations.Variable;
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
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.tools.xml.XmlDataAdaptor;
import xal.model.xml.*;
import xal.model.alg.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
import xal.smf.data.*;
import xal.smf.impl.qualify.*;
import xal.extension.solver.*;
//import xal.tools.formula.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import xal.tools.beam.*;
import xal.extension.application.smf.*;
import xal.extension.application.*;
import xal.smf.proxy.*;
import xal.smf.impl.*;

/**
 *
 * @author v1j
 */
public class ControlFace extends JPanel {
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    ArrayList<Variable> variables;
    Problem problem;
    
    RecentFileTracker ft;
    private JFileChooser fc;
    
    private HashMap<String, Integer> pvloggermap;
    private DataTable edmresultsdatatable;
    private DataTable fitresultsdatatable;
    private JTable resultsdatatable;
    private DataTableModel resultsdatatablemodel;
    private JScrollPane resultsdatascrollpane;
    
    GenDocument doc;
    
    private EnvelopeProbe solverprobe;
	private Scenario solvermodel;
	private EnvelopeProbe initprobe;
	private Scenario model;
    
    EdgeLayout layout = new EdgeLayout();
    public JPanel mainPanel;
    private JButton loadtable;
    private String[] initstate = {"No PV Logger Files"};
    private JComboBox<String> machinestatechooser = new JComboBox<String>(initstate);
    //private JLabel targetlabel;
    private JLabel magnetlabel;
    private JButton targetsequencebutton;
    private JButton magnetsequencebutton;
    private JButton add;
    private JButton forward;
    private JButton backward;
    private JButton clearbutton;
    private JButton clearallbutton;
    private JComboBox<String> targetlist;
    private JButton solvebutton;
    private JButton singlepassbutton;
    private JLabel timelabel;
    private JLabel tablelabel;
    private JLabel targetlabel;
    private JLabel satisfaction;
    private JTextField satfield;
    private JTextField timefield;
    private JButton scantwiss;
    private JButton probeeditbutton;
    private String[] graphs = {"Plot Betas", "Plot Alphas", "Plot RMS Size"};
    private JComboBox<String> graphChooser = new JComboBox<String>(graphs);
    private String[] solverchoice = {"Load From PV Logger State", "Load from Last Solution"};
    private JComboBox<String> solverChooser = new JComboBox<String>(solverchoice);
    private String[] resultchoice = {"Display Beta", "Display Alpha", "Display RMS"};
    private JComboBox<String> resultChooser = new JComboBox<String>(resultchoice);
    private boolean probeedited = false;
    ScrollPaneLayout controllayout;
    TableColumnModel alldatacolumns;
    JScrollPane magnetscrollpane;
    DataTableModel magnetmodel;
    public JTable magnettable;
    
    JScrollPane datascrollpane;
    DataTableModel datamodel;
    public JTable datatable;
    
    int solutionsposition;
    JScrollPane controlpane;
    ArrayList<DataTableModel> controlmodels = new ArrayList<DataTableModel>();
    ArrayList<JTable> controltables = new ArrayList<JTable>();
    JPanel controlpanel;
    
    JScrollPane resultscrollpane;
    DataTableModel resultmodel;
    public JTable resulttable;
    private JPanel magnetresultPanel;
    private JPanel valueresultPanel;
    private JButton custominput;
    
    ModelFace modelface;
    JPanel plotpanel;
    FunctionGraphsJPanel modelplot;
    
    private Accelerator accl;
    private AcceleratorSeqCombo seq;
    private ArrayList<Trial> allsolutions;
    private ArrayList<AcceleratorSeq> magnetseqlist = new ArrayList<AcceleratorSeq>();
    private ArrayList<Object> magnetseqlistnames = new ArrayList<Object>();
    private ArrayList<AcceleratorNode> magnetnodes = new ArrayList<AcceleratorNode>();
    private ArrayList<AcceleratorSeq> targetseqlist = new ArrayList<AcceleratorSeq>();
    private ArrayList<Object> targetseqlistnames = new ArrayList<Object>();
    private ArrayList<AcceleratorNode> targetnodes = new ArrayList<AcceleratorNode>();
    
    double alphax0, betax0, emitx0, alphay0, betay0, emity0, alphaz0, betaz0, emitz0;
    double currentenergy, currentcharge, currentcurrent;
    boolean betaplot=true;
    boolean alphaplot=false;
    boolean sizeplot=false;
    boolean startfromlast=false;
    boolean firstload = true;
    int s=0;
    PVLoggerDataSource plds;
	
    NumberFormat numfor = NumberFormat.getNumberInstance();
    
    
    String init;
    GridLimits limits = new GridLimits();
    /** Creates a new instance of ControlFace */
	public ControlFace(GenDocument aDocument, ModelFace modelface){
		
		doc=aDocument;
		accl = doc.accl;
		edmresultsdatatable = doc.masterdatatable;
		fitresultsdatatable = doc.resultsdatatable;
		pvloggermap = doc.masterpvloggermap;
		this.modelface = modelface;
		
		makeComponents(); //Creation of all GUI components
		addComponents();  //Add all components to the layout and panels
		setAction();      //Set the action listeners
	}
	
	public void makeComponents(){
        fc = new JFileChooser();
		ft = new RecentFileTracker(1, this.getClass(), "wsfile");
		ft.applyRecentFolder(fc);
		
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(1100, 750));
		loadtable = new JButton("Load Table");
        
		custominput = new JButton("Custom Input");
        
		magnetlabel = new JLabel("Select magnets to vary:");
        //targetlabel = new JLabel("Select location of desired beam size");
		tablelabel = new JLabel("Parameter Goals Table: ");
		targetlabel = new JLabel("Select location of goal point:");
		
		timelabel = new JLabel("Solver Time");
		timefield = new JTextField(4);
		timefield.setText("10");
		
		satisfaction = new JLabel("Satisfaction: ");
		satfield = new JTextField(5);
        
		forward = new JButton(">");
		backward = new JButton("<");
        
		magnetsequencebutton = new JButton("Select Sequence");
		targetsequencebutton = new JButton("Select Sequence");
        
		targetlist = new JComboBox<String>();
		targetlist.addItem("Select Element");
        
		probeeditbutton = new JButton("Edit Probe");
		probeeditbutton.setEnabled(false);
		solvebutton = new JButton("Solve");
		singlepassbutton = new JButton("Single Pass");
		clearbutton = new JButton("Clear Goals Table");
		clearallbutton = new JButton("Clear All Data");
		
        add = new JButton("Add");
		
        magnetresultPanel = new JPanel();
        valueresultPanel = new JPanel();
        makeMagnetTable();
        makeDataTable();
        makeMagnetResultTable();
		makeResultsDataTable();
		
        controlpanel = new JPanel();
        controlpanel.setLayout(new FlowLayout());
		
        controlpane = new JScrollPane(controlpanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		controlpane.getVerticalScrollBar().setValue(0);
		controlpane.getHorizontalScrollBar().setValue(0);
		controlpane.setPreferredSize(new Dimension(720, 105));
        
        modelplot = new FunctionGraphsJPanel();
		modelplot.setPreferredSize(new Dimension(800, 200));
		modelplot.setGraphBackGroundColor(Color.WHITE);
		plotpanel = new JPanel();
		plotpanel.setPreferredSize(new Dimension(830, 230));
		plotpanel.setBorder(BorderFactory.createTitledBorder("Raw Data Display"));
		plotpanel.setBorder(BorderFactory.createRaisedBevelBorder());
        plotpanel.add(modelplot);
        
		numfor.setMinimumFractionDigits(2);
		numfor.setMaximumFractionDigits(3);
	}
	
	public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		magnetresultPanel.add(resultscrollpane);
		valueresultPanel.add(resultsdatascrollpane);
		
		layout.add(loadtable, mainPanel, 150, 10, EdgeLayout.LEFT);
		layout.add(clearallbutton, mainPanel, 150, 40, EdgeLayout.LEFT);
		layout.add(datascrollpane, mainPanel, 290, 2, EdgeLayout.LEFT);
		
		layout.add(targetlabel, mainPanel, 0, 140, EdgeLayout.LEFT);
		layout.add(targetsequencebutton, mainPanel, 0, 160, EdgeLayout.LEFT);
		layout.add(targetlist, mainPanel, 0, 195, EdgeLayout.LEFT);
		layout.add(add, mainPanel, 220, 195, EdgeLayout.LEFT);
		
		layout.add(magnetlabel, mainPanel, 0, 285, EdgeLayout.LEFT);
		layout.add(magnetsequencebutton, mainPanel, 0, 305, EdgeLayout.LEFT);
		layout.add(solverChooser, mainPanel, 138, 305, EdgeLayout.LEFT);
		layout.add(magnetscrollpane, mainPanel, 0, 335, EdgeLayout.LEFT);
		
		layout.add(probeeditbutton, mainPanel, 370, 310, EdgeLayout.LEFT);
		layout.add(singlepassbutton, mainPanel, 370, 340, EdgeLayout.LEFT);
		layout.add(timelabel, mainPanel, 370, 375, EdgeLayout.LEFT);
		layout.add(timefield, mainPanel, 460, 375, EdgeLayout.LEFT);
		layout.add(solvebutton, mainPanel, 370, 400, EdgeLayout.LEFT);
		layout.add(satisfaction, mainPanel, 370, 440, EdgeLayout.LEFT);
		layout.add(satfield, mainPanel, 460, 437, EdgeLayout.LEFT);
		layout.add(magnetresultPanel, mainPanel, 525, 300, EdgeLayout.LEFT);
		layout.add(resultChooser, mainPanel, 910, 280, EdgeLayout.LEFT);
		layout.add(valueresultPanel, mainPanel, 780, 300, EdgeLayout.LEFT);
		
		layout.add(tablelabel, mainPanel, 370, 125, EdgeLayout.LEFT);
		layout.add(clearbutton, mainPanel, 760, 120, EdgeLayout.LEFT);
		layout.add(controlpane, mainPanel, 360, 150, EdgeLayout.LEFT);
		
		layout.add(graphChooser, mainPanel, 55, 570, EdgeLayout.LEFT);
		layout.add(plotpanel, mainPanel, 220, 470, EdgeLayout.LEFT);
		
		this.add(mainPanel);
	}
	
	public void setAction(){
		
		loadtable.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent  ae) {
                if(firstload){
                    firstload=false;
                }
                else{
                    clearAllData();
                }
                ArrayList<Object> tabledata = new ArrayList<Object>();
                for(int i=0;i<modelface.storedtable.getRowCount();i++){
                    for(int j=0;j<modelface.storedtable.getColumnCount();j++){
                        tabledata.add(modelface.storedtable.getValueAt(i,j));
                    }
                    datamodel.addTableData(new ArrayList<Object>(tabledata));
                    tabledata.clear();
                }
                datamodel.fireTableDataChanged();
                //Initialize a data source
                plds = new PVLoggerDataSource((doc.masterpvloggermap.get((String)datatable.getValueAt(0,0))).intValue());
			}
		});
		
		clearallbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent  ae) {
				clearAllData();
            }
        });
		
		magnetsequencebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                NonConsecutiveSeqSelector selector = new NonConsecutiveSeqSelector();
                selector.selectSequence();
                magnetseqlistnames = selector.getSeqList();
                System.out.println("magnetseqlistnames is " + magnetseqlistnames);
                Iterator<Object> itr = magnetseqlistnames.iterator();
                magnetseqlistnames = selector.getSeqList();
                magnetseqlist.clear();
                magnetnodes.clear();
                magnetmodel.clearAllData();
                while(itr.hasNext()){
                    AcceleratorSeq seq = accl.getSequence( itr.next().toString() );
                    magnetseqlist.add(seq);
                }
                
                ArrayList<Object> tabledata = new ArrayList<Object>();
                ArrayList<String> psadded = new ArrayList<String>();
                tabledata.clear();
                AcceleratorSeqCombo accseq = new AcceleratorSeqCombo("Seq", magnetseqlist);
                TypeQualifier ps_quad_qualifier = AndTypeQualifier.qualifierWithStatusAndType( true, "quad" ).and( new NotTypeQualifier( "pmag" ) );
                magnetnodes.addAll(accseq.getAllNodesWithQualifier( ps_quad_qualifier ));
                
                for(int i=0; i<magnetnodes.size(); i++){
                    tabledata.clear();
                    String name = magnetnodes.get(i).getId();
                    MagnetMainSupply ps = ((Electromagnet)accseq.getNodeWithId(name)).getMainSupply();
                    if(!psadded.contains(ps.getId())){
                        psadded.add(ps.getId());
                        tabledata.add(ps.getId());
                        tabledata.add((new Double(0.0)));
                        tabledata.add(new Boolean(false));
                        tabledata.add(new Double(10.0));
                        magnetmodel.addTableData(new ArrayList<Object>(tabledata));
                    }
                }
                updateMagnetTable();
                magnetmodel.fireTableDataChanged();
            }
		});
		
		targetsequencebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                NonConsecutiveSeqSelector selector = new NonConsecutiveSeqSelector();
                selector.selectSequence();
                targetseqlistnames = selector.getSeqList();
                Iterator<Object> itr = targetseqlistnames.iterator();
                targetseqlistnames = selector.getSeqList();
                targetseqlist.clear();
                targetnodes.clear();
                while(itr.hasNext()){
                    AcceleratorSeq seq = accl.getSequence( itr.next().toString() );
                    targetseqlist.add(seq);
                    targetnodes.addAll(seq.getAllNodes());
                }
                for(int i=0;i<targetnodes.size();i++){
                    targetlist.addItem(targetnodes.get(i).getId());
                }
                setInitProbe();
            }
        });
        
		probeeditbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                SimpleProbeEditor spe = new SimpleProbeEditor(doc.getAcceleratorWindow(), model.getProbe());
                //spe.createSimpleProbeEditor(model.getProbe());
            }
		});
        
		
		singlepassbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                singlePass();
            }
		});
		
        
		solvebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                solve();
            }
		});
		
		add.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
				int s = 0;
				for(int i=0;i<datatable.getRowCount();i++) if((Boolean)datatable.getValueAt(i,8)) s = i;
				double initposition = seq.getPosition(seq.getNodeWithId((String)datatable.getValueAt(s,1)));
				double controlposition = seq.getPosition(seq.getNodeWithId((String)targetlist.getSelectedItem()));
				System.out.println("initial position and control position are " + initposition + "  " + controlposition);
				if (initposition <= controlposition){
					addControlTable((String)targetlist.getSelectedItem());
				}
				else{
				    System.out.println("Warning: The control point you picked is upstream of the initial condition.");
				}
            }
        });
		
		clearbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                controltables.clear();
                controlmodels.clear();
                controlpanel.removeAll();
                controlpanel.validate();
                controlpane.validate();
                controlpanel.repaint();
                controlpane.repaint();
            }
        });
		
		graphChooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent  ae) {
                if(graphChooser.getSelectedIndex()==0){
                    betaplot=true;
                    alphaplot=false;
                    sizeplot = false;
                }
                if(graphChooser.getSelectedIndex()==1){
                    betaplot=false;
                    alphaplot=true;
                    sizeplot = false;
                }
                if(graphChooser.getSelectedIndex()==2){
                    betaplot=false;
                    alphaplot=false;
                    sizeplot = true;
                }
                resetPlot();
            }
        });
		
		solverChooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent  ae) {
				if(solverChooser.getSelectedIndex()==0){
					startfromlast=false;
					System.out.println("Starting from PV logger values.");
				}
				if(solverChooser.getSelectedIndex()==1){
					startfromlast=true;
					System.out.println("Starting from last solution");
				}
				if(magnetnodes.size()>0){
					updateMagnetTable();
				}
			}
		});
		
		resultChooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent  ae) {
                resetResultsTable();
            }
		});
		
	}
	
    
	private void setInitProbe(){
		int s = 0;
		for(int i=0;i<datatable.getRowCount();i++) if((Boolean)datatable.getValueAt(i,8)) s = i;
		String init = (String)datatable.getValueAt(s,1);
		int id = doc.masterpvloggermap.get((String) datatable.getValueAt(s,0));
		Integer pvloggerid;
		if(id != 0){
			pvloggerid = new Integer(id);
			System.out.println("Using PV Logger ID from selected file.  ID # " + pvloggerid);
		}
		else{
			pvloggerid = doc.currentpvloggerid;
			System.out.println("Warning: File has no PV Log ID.  Using ID " + pvloggerid);
		}
		
		//Intelligently construct a sequence from initial condition to control point;
		AcceleratorNode initnode = accl.getNode(init);
		AcceleratorSeq initseq = initnode.getParent();
		AcceleratorSeq finalseq = targetseqlist.get(targetseqlist.size()-1);
		System.out.println("Begin and end sequences are " + initseq + " and " + finalseq);
		ArrayList<AcceleratorSeqCombo> allseqcombos;
		allseqcombos=(ArrayList<AcceleratorSeqCombo>
                      )AcceleratorSeqCombo.getInstancesForRange("Seq", initseq, finalseq);
		seq = allseqcombos.get(0);
        
        EnvelopeProbe tempprobe = null;
        try {
            IAlgorithm etracker = AlgorithmFactory.createEnvTrackerAdapt( seq );
            tempprobe = ProbeFactory.getEnvelopeProbe(seq, etracker);
            initprobe = ProbeFactory.getEnvelopeProbe(seq, etracker);
            
        } catch ( InstantiationException exception ) {
            
        }
        
		try{
            model = Scenario.newScenarioFor(seq);
			model.setProbe(tempprobe);
			model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			plds.updatePVLoggerId((pvloggerid.longValue()));
			model = plds.setModelSource(seq,model);
            model.resync();
            model.run();
        } catch(Exception e){}
		
        Trajectory<EnvelopeProbeState> traj = tempprobe.getTrajectory();
		EnvelopeProbeState newstate = traj.stateForElement(init);
		initprobe.setKineticEnergy(newstate.getKineticEnergy());
		System.out.println("Current Energy in update probe is " + newstate.getKineticEnergy());
		initprobe.setPosition(seq.getPosition(seq.getNodeWithId(init)));
		
		alphax0 = (Double) datatable.getValueAt(s,2);
        alphay0 = (Double) datatable.getValueAt(s,3);
        betax0 = (Double) datatable.getValueAt(s,4);
        betay0 = (Double) datatable.getValueAt(s,5);
        emitx0 = (Double) datatable.getValueAt(s,6);
        emity0 = (Double) datatable.getValueAt(s,7);
        CovarianceMatrix covarianceMatrix = newstate.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        
        alphaz0 = twiss[2].getAlpha();
        betaz0 = twiss[2].getBeta();
        emitz0 = twiss[2].getEmittance();
		Twiss xTwiss = new Twiss(alphax0, betax0, emitx0*1e-6);
		Twiss yTwiss = new Twiss(alphay0, betay0, emity0*1e-6);
        Twiss zTwiss = new Twiss(alphaz0, betaz0, emitz0);
        Twiss[] tw= new Twiss[3];
        tw[0] = (xTwiss);
        tw[1] = (yTwiss);
        tw[2] = (zTwiss);
        initprobe.initFromTwiss(tw);
		model.setProbe(initprobe);
		System.out.println("Twiss x is " + tw[0] + " at position " + initprobe.getPosition());
		probeeditbutton.setEnabled(true);
	}
    
    
	public void addControlTable(String Id){
		
        String[] colnames = {Id , "Initial", "Goal", "Weight", "Select", "Result"};
        
        DataTableModel tablemodel = new DataTableModel(colnames, 0);
        controlmodels.add(tablemodel);
        
		JTable table = new JTable(tablemodel);
        controltables.add(table);
		table.getColumnModel().getColumn(0).setMinWidth(120);
		table.getColumnModel().getColumn(1).setMinWidth(60);
		table.getColumnModel().getColumn(2).setMinWidth(60);
		table.getColumnModel().getColumn(3).setMinWidth(50);
		table.getColumnModel().getColumn(4).setMinWidth(40);
		table.getColumnModel().getColumn(5).setMinWidth(70);
		
        controlpanel.add(table);
        
        Twiss[] twiss = getTwiss(Id);
        
        ArrayList<Object> tabledata = new ArrayList<Object>();
        tabledata.add(" X Beta");
        tabledata.add(numfor.format(twiss[0].getBeta()));
        tabledata.add("0.0");
		tabledata.add("1.0");
		tabledata.add(new Boolean(false));
		tabledata.add("");
        tablemodel.addTableData(new ArrayList<Object>(tabledata));
        tabledata.clear();
        tabledata.add(" X Alpha");
        tabledata.add(numfor.format(twiss[0].getAlpha()));
        tabledata.add("0.0");
		tabledata.add("1.0");
		tabledata.add(new Boolean(false));
		tabledata.add("");
        tablemodel.addTableData(new ArrayList<Object>(tabledata));
		tabledata.clear();
        tabledata.add(" Y Beta");
        tabledata.add(numfor.format(twiss[1].getBeta()));
        tabledata.add("0.0");
		tabledata.add("1.0");
		tabledata.add(new Boolean(false));
		tabledata.add("");
        tablemodel.addTableData(new ArrayList<Object>(tabledata));
        tabledata.clear();
        tabledata.add(" Y Alpha");
        tabledata.add(numfor.format(twiss[1].getAlpha()));
        tabledata.add("0.0");
		tabledata.add("1.0");
		tabledata.add(new Boolean(false));
		tabledata.add("");
        tablemodel.addTableData(new ArrayList<Object>(tabledata));
		
        tablemodel.fireTableDataChanged();
		table.validate();
		JScrollPane controlscroll = new JScrollPane(table,JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		controlscroll.setPreferredSize(new Dimension(420, 80));
		controlscroll.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        controlpanel.add(controlscroll);
        controlpanel.validate();
        controlpanel.repaint();
        controlpane.validate();
	}
	
	
	public Twiss[] getTwiss(String Id){
		
        final EnvelopeProbe localprobe = EnvelopeProbe.newInstance(initprobe);
		int s = 0;
		for(int i=0;i<datatable.getRowCount();i++)
            if((Boolean)datatable.getValueAt(i,8)) s = i;
		
		Scenario localmodel;
		try{
            localmodel = Scenario.newScenarioFor(seq);
			localmodel.setProbe(localprobe);
			localmodel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			plds.updatePVLoggerId(( doc.masterpvloggermap.get((String) datatable.getValueAt(s,0))).intValue());
			localmodel = plds.setModelSource(seq, localmodel);
            localmodel.setStartNode((String)datatable.getValueAt(s,1));
            localmodel.resync();
            localmodel.run();
        }catch(Exception e){}
		
        Trajectory<EnvelopeProbeState> traj = localprobe.getTrajectory();
		EnvelopeProbeState newstate = traj.statesForElement(Id).get(0);
		
        CovarianceMatrix covarianceMatrix = newstate.getCovarianceMatrix();
        Twiss[] twiss = covarianceMatrix.computeTwiss();
        
        return twiss;
	}
	
	
	public ArrayList<Object> parseFile(File newfile){
		ParseWireFile parsefile = new ParseWireFile();
		ArrayList<Object> newdata = new ArrayList<Object>();
		try{
			newdata = parsefile.parseFile(newfile);
		}
		catch(IOException e){
			System.out.println("Warning, returning empty data set.");
		}
		return newdata;
	}
	
	
	public void updateMagnetTable(){
		
		int selected =0;
		for(int i=0;i<datatable.getRowCount();i++){  //Which wire file set to use.
			if((Boolean)datatable.getValueAt(i,8)){
				selected = i;
			}
		}
		int id = doc.masterpvloggermap.get((String) datatable.getValueAt(selected,0));
		Integer pvloggerid;
		
		if(id != 0){
			pvloggerid = new Integer(id);
			System.out.println("Using PV Logger ID from selected file.  ID # " + pvloggerid);
		}
		else{
			pvloggerid = doc.currentpvloggerid;
			System.out.println("Warning: File has no PV Log ID.  Using ID " + pvloggerid);
		}
		plds.updatePVLoggerId(pvloggerid.longValue());
		final Map<String,Double> quadmap = plds.getMagnetPSMap();
		
		if(!startfromlast){	//Initialize from PVlogger
			for(int i = 0;i<magnettable.getRowCount();i++){
				String name = (String)magnettable.getValueAt(i,0) + ":B_Set";
				double value = (quadmap.get(name)).doubleValue();
				magnettable.setValueAt(new Double(value),i,1);
			}
			System.out.println("Loading from PV logger.");
		}
		if(startfromlast){	//Initialize from previous solution
			for(int k = 0; k<resulttable.getRowCount(); k++){
				String resultname = (String)resulttable.getValueAt(k,0);
				for(int i = 0; i<magnettable.getRowCount(); i++){
					if(resultname.equals((String)magnettable.getValueAt(i,0))){
						double value = ((Double)resulttable.getValueAt(k,1)).doubleValue();
						magnettable.setValueAt(new Double(value), i,1);
					}
				}
			}
			System.out.println("Loading from last solution.");
		}
		magnetmodel.fireTableDataChanged();
	}
	
	
	public void singlePass(){
		solverprobe = EnvelopeProbe.newInstance(initprobe);
		
		int s = 0;
		for(int i=0;i<datatable.getRowCount();i++)
            if((Boolean)datatable.getValueAt(i,8)) s = i;
        
		try{
            solvermodel = Scenario.newScenarioFor(seq);
			solvermodel.setProbe(solverprobe);
			solvermodel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			plds.updatePVLoggerId(( doc.masterpvloggermap.get((String) datatable.getValueAt(s,0))).intValue());
			solvermodel = plds.setModelSource(seq, solvermodel);
            solvermodel.setStartNode((String)datatable.getValueAt(s,1));
			
			for(int i = 0;i<magnettable.getRowCount();i++){  //This routine assigns user-specifiec magnet values
				MagnetMainSupply ps = accl.getMagnetMainSupply((String)magnettable.getValueAt(i,0));
				double valuemag = ((Double)magnettable.getValueAt(i,1)).doubleValue();
				Object[] psnodes = ps.getNodes().toArray();
				for(int j=0 ; j < psnodes.length; j++){
                    Electromagnet magnet = (Electromagnet)psnodes[j];
                    if(seq.contains(magnet)){
                        double value = ((Electromagnet)seq.getNodeWithId(magnet.getId())).toFieldFromCA(valuemag);
                        solvermodel.setModelInput(seq.getNodeWithId(magnet.getId()), ElectromagnetPropertyAccessor.PROPERTY_FIELD, value);
                    }
                }
            }
            solvermodel.resync();
            solvermodel.run();
        } catch(Exception e){}
		
		resetPlot();
		resetControlTables();
		resetResultsTable();
	}
	
	
	
	public void solve(){
		
		int s = 0;
		for(int i=0;i<datatable.getRowCount();i++)
            if((Boolean)datatable.getValueAt(i,8)) s = i;
		
		int id =  doc.masterpvloggermap.get((String) datatable.getValueAt(s,0));
		Integer pvloggerid;
		if(id != 0){
			pvloggerid = new Integer(id);
			System.out.println("Using PV Logger ID from selected file.  ID # " + pvloggerid);
		}
		else{
			pvloggerid = doc.currentpvloggerid;
			System.out.println("Warning: File has no PV Log ID.  Using ID " + pvloggerid);
		}
        
		solverprobe = EnvelopeProbe.newInstance(initprobe);
		try{
            solvermodel = Scenario.newScenarioFor(seq);
			solvermodel.setProbe(solverprobe);
			solvermodel.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
			plds.updatePVLoggerId(pvloggerid.intValue());
			solvermodel = plds.setModelSource(seq, solvermodel);
            solvermodel.setStartNode((String)datatable.getValueAt(s,1));
			
			for(int i = 0;i<magnettable.getRowCount();i++){  //This routine assigns user-specifiec magnet values
				MagnetMainSupply ps = accl.getMagnetMainSupply((String)magnettable.getValueAt(i,0));
				double valuemag = ((Double)magnettable.getValueAt(i,1)).doubleValue();
				Object[] psnodes = ps.getNodes().toArray();
				for(int j=0 ; j < psnodes.length; j++){
					Electromagnet magnet = (Electromagnet)psnodes[j];
					if(seq.contains(magnet)){
						double value = ((Electromagnet)seq.getNodeWithId(magnet.getId())).toFieldFromCA(valuemag);
						solvermodel.setModelInput(seq.getNodeWithId(magnet.getId()), ElectromagnetPropertyAccessor.PROPERTY_FIELD, value);
					}
				}
			}
            solvermodel.resync();
            solvermodel.run();
        } catch(Exception e){}
		
		ArrayList<Variable> variables = assignMagnets();
		ArrayList<Objective> objectives = new ArrayList<Objective>();
        objectives.add(new TargetObjective( "diff", 0. ) );
        Evaluator1 evaluator = new Evaluator1( objectives, variables );
        Problem problem = new Problem( objectives, variables, evaluator );
        problem.addHint(new InitialDelta( 0.1 ) );
		showTargetVals();
        Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, Integer.parseInt(timefield.getText()), 0.999 );
        Solver solver = new  Solver(maxSolutionStopper);
        Trial best = solver.getScoreBoard().getBestSolution();
        if(best != null) updateVariables(best);
		
		System.out.println("Solving...");
        solver.solve( problem );
        System.out.println(solver.getScoreBoard());
        allsolutions =(ArrayList<Trial>) solver.getScoreBoard().getSolutionJudge().getOptimalSolutions();
        best = solver.getScoreBoard().getBestSolution();
		
		// Post final solutions.
        calcError(variables, best);
        showTargetVals();
        ArrayList<Object> resultdata = new ArrayList<Object>();
        if(!startfromlast) resultmodel.clearAllData();
        solutionsposition=0;
        for(int i = 0; i<variables.size(); i++){
            resultdata.clear();
			boolean already = false;
			for(int j = 0; j<resulttable.getRowCount(); j++){
				if( (variables.get(i)).getName() == (String)resulttable.getValueAt(j,0)){
					resulttable.setValueAt( (best.getTrialPoint()).getValue( variables.get(i)), j, 1);
					already = true;
				}
			}
			if(!already){
				resultdata.add((variables.get(i)).getName());
				resultdata.add((best.getTrialPoint()).getValue( variables.get(i)));
				resultmodel.addTableData(new ArrayList<Object>(resultdata));
			}
		}
		numfor.setMinimumFractionDigits(3);
		satfield.setText((new Double(numfor.format(best.getSatisfaction()))).toString());
		resultmodel.fireTableDataChanged();
		resetPlot();
		resetControlTables();
		resetResultsTable();
		if(startfromlast) updateMagnetTable();
	}
	
    
	private ArrayList<Variable> assignMagnets(){  //Assigns initial magnet values and solver variable list
		variables = new ArrayList<Variable>();
		
		for(int i = 0;i<magnettable.getRowCount();i++){
			MagnetMainSupply ps = accl.getMagnetMainSupply((String)magnettable.getValueAt(i,0));
			double valuemag = ((Double)magnettable.getValueAt(i,1)).doubleValue();
			Object[] psnodes = ps.getNodes().toArray();
			for(int j=0 ; j < psnodes.length; j++){
				Electromagnet magnet = (Electromagnet)psnodes[j];
				if(seq.contains(magnet)){
					double value = ((Electromagnet)seq.getNodeWithId(magnet.getId())).toFieldFromCA(valuemag);
					solvermodel.setModelInput(seq.getNodeWithId(magnet.getId()), ElectromagnetPropertyAccessor.PROPERTY_FIELD, value );
				}
			}
            if((Boolean)magnettable.getValueAt(i,2)){
				double frac = (((Double)magnettable.getValueAt(i,3)).doubleValue())/100.0;
				double dlimit = 1-frac;
				if(dlimit < 0) dlimit = 0;
				double ulimit = 1+frac;
				variables.add(new Variable(ps.getId(), valuemag, valuemag*dlimit,valuemag*ulimit));
				System.out.println("Adding variable" + ps.getId() + " with value "+ valuemag);
			}
		}
		return variables;
	}
	
    
	void loadlastbest(){
		System.out.println("Loading last best");
		for(int i = 0; i<resulttable.getRowCount(); i++){
			MagnetMainSupply ps = accl.getMagnetMainSupply((String)resulttable.getValueAt(i,0));
			Object[] psnodes = ps.getNodes().toArray();
			for(int j=0 ; j < psnodes.length; j++){
				Electromagnet magnet = (Electromagnet)psnodes[j];
				double magvalue = ((Double)resulttable.getValueAt(i,1)).doubleValue();
				double value = ((Electromagnet)seq.getNodeWithId(magnet.getId())).toFieldFromCA(magvalue);
				solvermodel.setModelInput(seq.getNodeWithId(magnet.getId()), ElectromagnetPropertyAccessor.PROPERTY_FIELD, value );
			}
		}
	}
	
    
	public double calcError2(){
		
        Trajectory<EnvelopeProbeState> traj = solverprobe.getTrajectory();
        ArrayList<EnvelopeProbeState> state = new ArrayList<EnvelopeProbeState>();
        ArrayList<Double> AlphaX = new ArrayList<Double>();
        ArrayList<Double> AlphaY = new ArrayList<Double>();
        ArrayList<Double> BetaX = new ArrayList<Double>();
        ArrayList<Double> BetaY = new ArrayList<Double>();
        
		//Creates and array of Twiss values that we are trying to control
        for(int i = 0; i < controltables.size(); i++){
			state.add(traj.statesForElement(controltables.get(i).getColumnName(0)).get(0));
            CovarianceMatrix covarianceMatrix = state.get(i).getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
			AlphaX.add(twiss[0].getAlpha());
			AlphaY.add(twiss[1].getAlpha());
			BetaX.add(twiss[0].getBeta());
			BetaY.add(twiss[1].getBeta());
        }
		
		double error = 0.;
        for(int i = 0; i < controltables.size(); i++){
			if((Boolean)controltables.get(i).getValueAt(0,4)){
				error += Math.pow(BetaX.get(i)-Double.parseDouble((String)controltables.get(i).getValueAt(0,2)),2.)*Double.parseDouble((String)controltables.get(i).getValueAt(0,3));
			}
			if((Boolean)controltables.get(i).getValueAt(1,4)){
				error += Math.pow(AlphaX.get(i)-Double.parseDouble((String)controltables.get(i).getValueAt(1,2)),2.)*Double.parseDouble((String)controltables.get(i).getValueAt(1,3));
			}
			if((Boolean)controltables.get(i).getValueAt(2,4)){
				error += Math.pow(BetaY.get(i)-Double.parseDouble((String)controltables.get(i).getValueAt(2,2)),2.)*Double.parseDouble((String)controltables.get(i).getValueAt(2,3));
			}
			if((Boolean)controltables.get(i).getValueAt(3,4)){
				error += Math.pow(AlphaY.get(i)-Double.parseDouble((String)controltables.get(i).getValueAt(3,2)),2.)*Double.parseDouble((String)controltables.get(i).getValueAt(3,3));
			}
		}
		return error;
	}
	
	public void updateVariables(Trial trial){
		
		Problem prob = trial.getProblem();
		ArrayList<Variable> vars =  new ArrayList<Variable>();
		vars =(ArrayList<Variable>) prob.getVariables();
		variables.clear();
		
		for(int i=0;i<vars.size();i++){
            double value = (trial.getTrialPoint()).getValue( vars.get(i) );
            String name = (vars.get(i)).getName();
            double lower =  (vars.get(i)).getLowerLimit();
            double upper =  (vars.get(i)).getUpperLimit();
            variables.add( new Variable(name, value, lower, upper));
		}
		problem.setVariables(variables);
	}
	
	
	public double calcError(ArrayList<Variable> vars,Trial trial){
		
        updateMags(vars, trial);
		
        try{
			solverprobe = EnvelopeProbe.newInstance(initprobe);
			solvermodel.setProbe(solverprobe);
			solvermodel.resync();
			solvermodel.run();
        }catch(Exception e){}
        double err = calcError2();
        return err;
	}
	
	
	public void updateMags(ArrayList<Variable> vars,Trial trial){
		
        for(int i=0;i<vars.size();i++){
			String name =(vars.get(i)).getName();
			MagnetMainSupply ps = accl.getMagnetMainSupply(name);
			Object[] psnodes = ps.getNodes().toArray();
			for(int j=0 ; j < psnodes.length; j++){
				Electromagnet magnet = (Electromagnet)psnodes[j];
				double magvalue = (trial.getTrialPoint()).getValue( vars.get(i));
				double value = ((Electromagnet)seq.getNodeWithId(magnet.getId())).toFieldFromCA(magvalue);
				solvermodel.setModelInput(seq.getNodeWithId(magnet.getId()), ElectromagnetPropertyAccessor.PROPERTY_FIELD, value );
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
    
	
	public void showTargetVals(){
        EnvelopeProbe probe = (EnvelopeProbe) solvermodel.getProbe();
		Trajectory<EnvelopeProbeState> traj = probe.getTrajectory();
        EnvelopeProbeState target = traj.statesForElement((String)targetlist.getSelectedItem()).get(0);
	}
	
	
	public void resetPlot(){
		
		modelplot.removeAllGraphData();
		ArrayList<Double> sdata = new ArrayList<Double>();
		ArrayList<Double> hdata = new ArrayList<Double>();
		ArrayList<Double> vdata = new ArrayList<Double>();
		BasicGraphData hgraphdata = new BasicGraphData();
		BasicGraphData vgraphdata = new BasicGraphData();
		BasicGraphData xgraphdata = new BasicGraphData();
		BasicGraphData ygraphdata = new BasicGraphData();
		
		Trajectory<EnvelopeProbeState> traj = solverprobe.getTrajectory();
		Iterator<EnvelopeProbeState> iterState = traj.stateIterator();
		boolean firstpos = true;
		double offset = 0.0;
		
		while(iterState.hasNext()){
			if(firstpos){
				EnvelopeProbeState firststate= iterState.next();
				offset = firststate.getPosition();
				firstpos = false;
			}
			if(betaplot){
				EnvelopeProbeState state= iterState.next();
				sdata.add(state.getPosition()-offset);
                CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
                Twiss[] twiss = covarianceMatrix.computeTwiss();
                
				double rx = twiss[0].getBeta();
				double ry = twiss[1].getBeta();
				double pos = state.getPosition() - offset;
				hdata.add(rx);
				vdata.add(ry);
			}
			//Plot Alphas
			if(alphaplot){
				EnvelopeProbeState state= iterState.next();
				sdata.add(state.getPosition()-offset);
                CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
                Twiss[] twiss = covarianceMatrix.computeTwiss();
                
				double rx = twiss[0].getAlpha();
				double ry = twiss[1].getAlpha();
				hdata.add(rx);
				vdata.add(ry);
			}
			if(sizeplot){
				EnvelopeProbeState state= iterState.next();
				sdata.add(state.getPosition()-offset);
                CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
                Twiss[] twiss = covarianceMatrix.computeTwiss();
                
				double rx = 1000.0*twiss[0].getEnvelopeRadius();
				double ry = 1000.0*twiss[1].getEnvelopeRadius();
				hdata.add(rx);
				vdata.add(ry);
			}
			
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
        
        int datasize = controltables.size();
		double[] srdata = new double[datasize];
		double[] xrdata = new double[datasize];
		double[] yrdata = new double[datasize];
		traj= solverprobe.getTrajectory();
		EnvelopeProbeState newstate;
		Twiss[] newtwiss;
		double rx, ry;
		boolean addedxdata = false;
		boolean addedydata = false;
		int selected = 0;
		
		for(int i=0;i<datatable.getRowCount();i++){
			if((Boolean)datatable.getValueAt(i,8)){
				selected = i;
			}
		}
		
		for(int i = 0; i<datasize; i++){
			newstate = traj.statesForElement(controltables.get(i).getColumnName(0)).get(0);
			srdata[i]=newstate.getPosition()-offset;
            if(betaplot){
				if((Boolean)controltables.get(i).getValueAt(0,4)){
					xrdata[i]=(Double.parseDouble((String)controltables.get(i).getValueAt(0,2)));
					addedxdata = true;
				}
				if((Boolean)controltables.get(i).getValueAt(2,4)){
					yrdata[i]=(Double.parseDouble((String)controltables.get(i).getValueAt(2,2)));
					addedydata = true;
				}
			}
			if(alphaplot){
				if((Boolean)controltables.get(i).getValueAt(1,4)){
					xrdata[i]=(Double.parseDouble((String)controltables.get(i).getValueAt(1,2)));
					addedxdata = true;
				}
				if((Boolean)controltables.get(i).getValueAt(3,4)){
					yrdata[i]=(Double.parseDouble((String)controltables.get(i).getValueAt(3,2)));
					addedydata = true;
				}
			}
			if(sizeplot){
				if((Boolean)controltables.get(i).getValueAt(0,4)){
					double betax = (Double.parseDouble((String)controltables.get(i).getValueAt(0,2)));
					double emitx0 = (Double) datatable.getValueAt(selected,6);
					xrdata[i]=Math.sqrt(betax*emitx0);
					addedxdata = true;
				}
				if((Boolean)controltables.get(i).getValueAt(2,4)){
					double betay = (Double.parseDouble((String)controltables.get(i).getValueAt(2,2)));
					double emity0 = (Double) datatable.getValueAt(selected,7);
					yrdata[i]=Math.sqrt(betay*emity0);
					addedydata = true;
				}
			}
			
		}
		
		if(addedxdata){
			xgraphdata.addPoint(srdata, xrdata);
			xgraphdata.setDrawPointsOn(true);
			xgraphdata.setDrawLinesOn(false);
			xgraphdata.setGraphColor(Color.RED);
		}
		if(addedydata){
			ygraphdata.addPoint(srdata, yrdata);
			ygraphdata.setDrawPointsOn(true);
			ygraphdata.setDrawLinesOn(false);
			ygraphdata.setGraphColor(Color.BLUE);
		}
		
        if(addedxdata) modelplot.addGraphData(xgraphdata);
		if(addedydata) modelplot.addGraphData(ygraphdata);
		modelplot.addGraphData(hgraphdata);
		modelplot.addGraphData(vgraphdata);
        
        limits.setSmartLimits();
        modelplot.setExternalGL(limits);
		
	}
	
	
	public void resetControlTables(){
		Trajectory<EnvelopeProbeState> traj = solverprobe.getTrajectory();
		EnvelopeProbeState state;
		
		for(int i = 0; i < controltables.size(); i++){
			state = traj.statesForElement(controltables.get(i).getColumnName(0)).get(0);
            CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
            Twiss[] twiss = covarianceMatrix.computeTwiss();
            
        	(controltables.get(i)).setValueAt(twiss[0].getBeta(), 0, 5);
			(controltables.get(i)).setValueAt(twiss[0].getAlpha(), 1, 5);
			(controltables.get(i)).setValueAt(twiss[1].getBeta(), 2, 5);
			(controltables.get(i)).setValueAt(twiss[1].getAlpha(), 3, 5);
			DataTableModel model = (DataTableModel)(controltables.get(i)).getModel();
			model.fireTableDataChanged();
        }
	}
	
	
	void resetResultsTable(){
		Trajectory<EnvelopeProbeState> traj = solverprobe.getTrajectory();
		resultsdatatablemodel.clearAllData();
		
		boolean beginrecord = false;
		numfor.setMinimumFractionDigits(2);
        for(AcceleratorNode node : seq.getNodes(true)){
			String name= node.getId();
			if(name.equals((String)datatable.getValueAt(s,1))){
				beginrecord = true;
			}
			if(beginrecord){
				ArrayList<Object> tabledata = new ArrayList<Object>();
				System.out.println("name is " + name);
				EnvelopeProbeState state = traj.stateForElement(name);
                CovarianceMatrix covarianceMatrix = state.getCovarianceMatrix();
                Twiss[] twiss = covarianceMatrix.computeTwiss();
                
				double valuex;
				double valuey;
				if(resultChooser.getSelectedIndex()==0){
					valuex = twiss[0].getBeta();
					valuey = twiss[1].getBeta();
				}
				else if(resultChooser.getSelectedIndex()==1){
					valuex = twiss[0].getAlpha();
					valuey = twiss[1].getAlpha();
				}
				else{
					valuex = 1000.0*twiss[0].getEnvelopeRadius();
					valuey = 1000.0*twiss[1].getEnvelopeRadius();
				}
				tabledata.add(new String(name));
				tabledata.add(numfor.format(valuex));
				tabledata.add(numfor.format(valuey));
				resultsdatatablemodel.addTableData(new ArrayList<Object>(tabledata));
			}
		}
		resultsdatatablemodel.fireTableDataChanged();
	}
	
	
    public void clearAllData(){
		datamodel.clearAllData();
		datamodel.fireTableDataChanged();
		controltables.clear();
		controlmodels.clear();
		controlpanel.removeAll();
		controlpanel.validate();
		controlpane.validate();
		controlpanel.repaint();
		controlpane.repaint();
		targetseqlistnames.clear();
		targetseqlist.clear();
		targetnodes.clear();
		targetlist.removeAllItems();
		targetlist.addItem("Select Element");
		magnetmodel.clearAllData();
		magnetseqlist.clear();
		magnetnodes.clear();
		magnetmodel.clearAllData();
		resultmodel.clearAllData();
		resultmodel.fireTableDataChanged();
		resultsdatatablemodel.clearAllData();
		resultsdatatablemodel.fireTableDataChanged();
		modelplot.removeAllGraphData();
		timefield.setText("10");
		satfield.setText("0.0");
		startfromlast=false;
		solverChooser.setSelectedIndex(0);
    }
    
    public void makeDataTable(){
        String[] colnames = {"File Name", "Element", "alphax", "alphay", "betax", "betay", "emitx", "emity", "Select"};
		
		datamodel = new DataTableModel(colnames, 0);
		
		datatable = new JTable(datamodel);
		datatable.getColumnModel().getColumn(0).setMinWidth(165);
		datatable.getColumnModel().getColumn(1).setMinWidth(120);
		datatable.getColumnModel().getColumn(2).setMinWidth(50);
		datatable.getColumnModel().getColumn(3).setMinWidth(50);
		datatable.getColumnModel().getColumn(4).setMinWidth(50);
		datatable.getColumnModel().getColumn(4).setMinWidth(50);
		datatable.getColumnModel().getColumn(5).setMinWidth(20);
		alldatacolumns = datatable.getColumnModel();
		
		datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
		datatable.setRowSelectionAllowed(false);
		datatable.setColumnSelectionAllowed(false);
		datatable.setCellSelectionEnabled(false);
		
        datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		datascrollpane.getVerticalScrollBar().setValue(0);
		datascrollpane.getHorizontalScrollBar().setValue(0);
		datascrollpane.setPreferredSize(new Dimension(800, 100));
	}
	
	
	public void makeMagnetTable(){
		String[] colnames = {"Magnet Name", "B(T/m)", "Select", "% Var."};
		
		magnetmodel = new DataTableModel(colnames, 0);
		
		magnettable = new JTable(magnetmodel);
		magnettable.getColumnModel().getColumn(0).setMinWidth(155);
		magnettable.getColumnModel().getColumn(1).setMinWidth(50);
		magnettable.getColumnModel().getColumn(2).setMinWidth(55);
		magnettable.getColumnModel().getColumn(3).setMinWidth(45);
		magnettable.setPreferredScrollableViewportSize(magnettable.getPreferredSize());
		magnettable.setRowSelectionAllowed(false);
		magnettable.setColumnSelectionAllowed(false);
		magnettable.setCellSelectionEnabled(false);
		
		magnetscrollpane = new JScrollPane(magnettable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		magnetscrollpane.getVerticalScrollBar().setValue(0);
		magnetscrollpane.getHorizontalScrollBar().setValue(0);
		magnetscrollpane.setPreferredSize(new Dimension(330, 100));
	}
    
	
	public void makeMagnetResultTable(){
		String[] colnames = {"Magnet Name", "Value"};
		resultmodel = new DataTableModel(colnames, 0);
		
		resulttable = new JTable(resultmodel);
		resulttable.getColumnModel().getColumn(0).setMinWidth(150);
		resulttable.getColumnModel().getColumn(1).setMinWidth(50);
		
		resulttable.setPreferredScrollableViewportSize(resulttable.getPreferredSize());
		resulttable.setRowSelectionAllowed(false);
		resulttable.setColumnSelectionAllowed(false);
		resulttable.setCellSelectionEnabled(false);
		
		resultscrollpane = new JScrollPane(resulttable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		resultscrollpane.getVerticalScrollBar().setValue(0);
		resultscrollpane.getHorizontalScrollBar().setValue(0);
		resultscrollpane.setPreferredSize(new Dimension(225, 100));
		magnetresultPanel.setBorder(BorderFactory.createTitledBorder("Magnet Results:"));
		
	}
    
    
    public void makeResultsDataTable(){
		String[] colnames = {"Element", "X", "Y"};
		
		resultsdatatablemodel = new DataTableModel(colnames, 0);
		
		resultsdatatable = new JTable(resultsdatatablemodel);
		resultsdatatable.getColumnModel().getColumn(0).setMinWidth(130);
		resultsdatatable.getColumnModel().getColumn(1).setMaxWidth(50);
		resultsdatatable.getColumnModel().getColumn(2).setMaxWidth(50);
		
		resultsdatatable.setRowSelectionAllowed(false);
		resultsdatatable.setColumnSelectionAllowed(false);
		resultsdatatable.setCellSelectionEnabled(false);
		
		resultsdatatable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		resultsdatascrollpane = new JScrollPane(resultsdatatable);
		resultsdatascrollpane.setColumnHeaderView(resultsdatatable.getTableHeader());
		
		resultsdatascrollpane.setPreferredSize(new Dimension(250, 100));
		valueresultPanel.setBorder(BorderFactory.createTitledBorder("Twiss Results:"));
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
				trial.setScore(objective, error);
            }
        }
    }
}




