/*************************************************************
 //
 // class TuneFace:
 // This class is responsible for the Graphic User Interface
 // components and action listeners for the tune setting tab.
 //
 /*************************************************************/

package xal.app.ringinjection;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.table.*;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.data.*;
import xal.extension.application.*;
import xal.model.*;
import xal.model.probe.*;
import xal.model.probe.traj.*;
import xal.model.probe.traj.*;
import xal.model.xml.*;
import xal.model.alg.*;
import xal.sim.scenario.*;
import xal.model.probe.*;

import java.io.File;
import java.io.*;
import java.lang.*;
import java.text.NumberFormat;
import java.text.DecimalFormat;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.beam.*;
import xal.tools.apputils.*;
import xal.extension.widgets.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;

public class SteererFace extends JPanel implements InjSpotListener {
	/** serial version required by Serializable */
	private static final long serialVersionUID = 1L;

	public JPanel mainPanel;
	public JPanel resultsPanel;
	public JPanel requestPanel;
	public JPanel solvePanel;
	public JPanel steererPanel;
	public JPanel correctorPanel;
	public JTable steerertable;

	private JComboBox<String> syncstate;
	private String[] syncstates = {"Model Live Lattice", "Model Design Lattice"};

	private NumberFormat avgFor;
	private NumberFormat steererFor;

	private SteererTableModel steerertablemodel;
	private SteererTableModel resultstablemodel;

	private JButton calcbutton;
	private JButton submitbutton;
	private JButton revertbutton;

	private HDipoleCorr[] hsteererNodes;
	private VDipoleCorr[] vsteererNodes;

	private Accelerator accl;
	private AcceleratorSeqCombo seq;

	private ArrayList<String> correctorlist;
	private String[] steerers = {"HEBT_Mag:DCH22","HEBT_Mag:DCH24","HEBT_Mag:DCH28", "HEBT_Mag:DCH30", "HEBT_Mag:DCV29", "HEBT_Mag:DCV31"};
	//JLabel initialguess = new JLabel("----Initial Fit Parameters----");
	//JLabel pointslabel = new JLabel("Number of Points to Fit:");
	JLabel xlabel = new JLabel("x (mm): ");
	JLabel xplabel = new JLabel("x' (mrad): ");
	JLabel ylabel = new JLabel("y (mm): ");
	JLabel yplabel = new JLabel("y' (mrad):");

	JLabel xlabelnew = new JLabel("x (mm): ");
	JLabel xplabelnew = new JLabel("x' (mrad): ");
	JLabel ylabelnew = new JLabel("y (mm): ");
	JLabel yplabelnew = new JLabel("y' (mrad):");

	JLabel xlabelsolve = new JLabel("x (mm): ");
	JLabel xplabelsolve = new JLabel("x' (mrad): ");
	JLabel ylabelsolve = new JLabel("y (mm): ");
	JLabel yplabelsolve = new JLabel("y' (mrad):");

	JCheckBox dch22 = new JCheckBox("DCH22");
	JCheckBox dch24 = new JCheckBox("DCH24");
	JCheckBox dch28 = new JCheckBox("DCH28");
	JCheckBox dch30 = new JCheckBox("DCH30");
	JCheckBox sptm = new JCheckBox("InjSptm");

	JLabel maxtimelabel = new JLabel("Solver Max Time (s):");
	JLabel initsptmlabel = new JLabel("Initial Sptm Guess (T):");

	JLabel labeldch22 = new JLabel(" DCH22");
	JLabel labeldch24 = new JLabel(" DCH24");
	JLabel labeldch28 = new JLabel(" DCH28");
	JLabel labeldch30 = new JLabel(" DCH30");
	JLabel labelsptm = new JLabel(" Sptm");
	JLabel labeldcv29 = new JLabel(" DCV29");
	JLabel labeldcv31 = new JLabel(" DCV31");
	JLabel blanklabel = new JLabel("");


	JScrollPane steererscrollpane;
	JScrollPane resultsscrollpane;

	Color mycolor = new Color(190,190,220);

	EdgeLayout layout = new EdgeLayout();

	DecimalField x = new DecimalField();
	DecimalField xp = new DecimalField();
	DecimalField y = new DecimalField();
	DecimalField yp = new DecimalField();

	DecimalField xnew = new DecimalField();
	DecimalField xpnew = new DecimalField();
	DecimalField ynew = new DecimalField();
	DecimalField ypnew = new DecimalField();

	DecimalField[] spot = new DecimalField[4];
	DecimalField[] steerer = new DecimalField[7];

	DecimalField maxtime = new DecimalField();
	DecimalField initsptm = new DecimalField();
	HashMap<String,Double> lastfields = new HashMap<>();

	double[] inj_params = new double[4];

	private double xinit = 0.0;
	private double xpinit = 0.0;
	private double yinit = 0.0;
	private double ypinit = 0.0;
	private double[] new_params = new double[4];
	private double[] init_params = new double[4];

	private String latticestate = "Live";
	GenDocument doc;

	int i;

	//Constructors
	public SteererFace(){
	}

	public SteererFace(GenDocument aDocument){

		doc=aDocument;

		setPreferredSize(new Dimension(900,500));
		setLayout(layout);

		makeComponents(); //Creation of all GUI components
		addComponents();  //Add all components to the layout and panels
		setAction();      //Set the action listeners
		setUpModel();  	  //Set up the accelerator model


	}

	public void addComponents(){

		layout.setConstraints(mainPanel, 0, 0, 0, 0, EdgeLayout.ALL_SIDES, EdgeLayout.GROW_BOTH);
		this.add(mainPanel);

		EdgeLayout newlayout = new EdgeLayout();
		mainPanel.setLayout(newlayout);

		GridBagLayout resultsgrid = new GridBagLayout();
		resultsPanel.setLayout(resultsgrid);
		GridBagConstraints c = new GridBagConstraints();
		c.gridwidth=1;
		c.gridx=0; c.gridy=1;
		c.insets=new Insets(10,2,0,2);
		resultsgrid.setConstraints(xlabel, c);
		resultsPanel.add(xlabel);
		c.gridx=1; c.gridy=1;
		resultsgrid.setConstraints(x, c);
		resultsPanel.add(x);
		c.gridx=0; c.gridy=2;
		resultsgrid.setConstraints(xplabel, c);
		resultsPanel.add(xplabel);
		c.gridx=1; c.gridy=2;
		resultsgrid.setConstraints(xp, c);
		resultsPanel.add(xp);
		c.gridx=0; c.gridy=3;
		resultsgrid.setConstraints(ylabel, c);
		resultsPanel.add(ylabel);
		c.gridx=1; c.gridy=3;
		resultsgrid.setConstraints(y, c);
		resultsPanel.add(y);
		c.gridx=0; c.gridy=4;
		resultsgrid.setConstraints(yplabel, c);
		resultsPanel.add(yplabel);
		c.gridx=1; c.gridy=4;
		resultsgrid.setConstraints(yp, c);
		resultsPanel.add(yp);

		GridBagLayout requestgrid = new GridBagLayout();
		requestPanel.setLayout(requestgrid);
		GridBagConstraints d = new GridBagConstraints();
		d.gridwidth=1;
		d.gridx=0; d.gridy=1;
		d.insets=new Insets(10,2,0,2);
		requestgrid.setConstraints(xlabelnew, d);
		requestPanel.add(xlabelnew);
		d.gridx=1; d.gridy=1;
		requestgrid.setConstraints(xnew, d);
		requestPanel.add(xnew);
		d.gridx=0; d.gridy=2;
		requestgrid.setConstraints(xplabelnew, d);
		requestPanel.add(xplabelnew);
		d.gridx=1; d.gridy=2;
		requestgrid.setConstraints(xpnew, d);
		requestPanel.add(xpnew);
		d.gridx=0; d.gridy=3;
		requestgrid.setConstraints(ylabelnew, d);
		requestPanel.add(ylabelnew);
		d.gridx=1; d.gridy=3;
		requestgrid.setConstraints(ynew, d);
		requestPanel.add(ynew);
		d.gridx=0; d.gridy=4;
		requestgrid.setConstraints(yplabelnew, d);
		requestPanel.add(yplabelnew);
		d.gridx=1; d.gridy=4;
		requestgrid.setConstraints(ypnew, d);
		requestPanel.add(ypnew);
		d.gridx=0; d.gridy=5;
		d.gridwidth=2;


		GridBagLayout solvegrid = new GridBagLayout();
		solvePanel.setLayout(solvegrid);
		GridBagConstraints e = new GridBagConstraints();
		e.gridwidth=1;

		e.gridx=0; e.gridy=1;
		e.insets=new Insets(10,2,0,2);
		solvegrid.setConstraints(xlabelsolve, e);
		solvePanel.add(xlabelsolve);

		e.gridx=1; e.gridy=1;
		spot[0].setMinimumSize( spot[0].getPreferredSize() );
		solvegrid.setConstraints(spot[0], e);
		solvePanel.add(spot[0]);

		e.gridx=0; e.gridy=2;
		solvegrid.setConstraints(xplabelsolve, e);
		solvePanel.add(xplabelsolve);

		e.gridx=1; e.gridy=2;
		spot[1].setMinimumSize( spot[1].getPreferredSize() );
		solvegrid.setConstraints(spot[1], e);
		solvePanel.add(spot[1]);

		e.gridx=0; e.gridy=3;
		solvegrid.setConstraints(ylabelsolve, e);
		solvePanel.add(ylabelsolve);

		e.gridx=1; e.gridy=3;
		spot[2].setMinimumSize( spot[2].getPreferredSize() );
		solvegrid.setConstraints(spot[2], e);
		solvePanel.add(spot[2]);

		e.gridx=0; e.gridy=4;
		solvegrid.setConstraints(yplabelsolve, e);
		solvePanel.add(yplabelsolve);

		e.gridx=1; e.gridy=4;
		spot[3].setMinimumSize( spot[3].getPreferredSize() );
		solvegrid.setConstraints(spot[3], e);
		solvePanel.add(spot[3]);

		GridBagLayout steerergrid = new GridBagLayout();
		steererPanel.setLayout(steerergrid);
		GridBagConstraints f = new GridBagConstraints();
		f.gridwidth=1;
		f.gridx=0; f.gridy=1;
		f.insets=new Insets(10,2,0,2);
		steerergrid.setConstraints(labeldch22, f);
		steererPanel.add(labeldch22);
		f.gridx=1; f.gridy=1;
		steerergrid.setConstraints(steerer[0], f);
		steererPanel.add(steerer[0]);
		f.gridx=0; f.gridy=2;
		steerergrid.setConstraints(labeldch24, f);
		steererPanel.add(labeldch24);
		f.gridx=1; f.gridy=2;
		steerergrid.setConstraints(steerer[1], f);
		steererPanel.add(steerer[1]);
		f.gridx=0; f.gridy=3;
		steerergrid.setConstraints(labeldch28, f);
		steererPanel.add(labeldch28);
		f.gridx=1; f.gridy=3;
		steerergrid.setConstraints(steerer[2], f);
		steererPanel.add(steerer[2]);
		f.gridx=0; f.gridy=4;
		steerergrid.setConstraints(labeldch30, f);
		steererPanel.add(labeldch30);
		f.gridx=1; f.gridy=4;
		steerergrid.setConstraints(steerer[3], f);
		steererPanel.add(steerer[3]);
		f.gridx=0; f.gridy=5;
		//steerergrid.setConstraints(labelsptm, f);
		//steererPanel.add(labelsptm);
		steerergrid.setConstraints(blanklabel, f);
		steererPanel.add(blanklabel);
		//f.gridx=1; f.gridy=5;
		//steerergrid.setConstraints(steerer[4], f);
		//steererPanel.add(steerer[4]);
		f.gridx=0; f.gridy=6;
		steerergrid.setConstraints(labeldcv29, f);
		steererPanel.add(labeldcv29);
		f.gridx=1; f.gridy=6;
		steerergrid.setConstraints(steerer[4], f);
		steererPanel.add(steerer[4]);
		f.gridx=0; f.gridy=7;
		steerergrid.setConstraints(labeldcv31, f);
		steererPanel.add(labeldcv31);
		f.gridx=1; f.gridy=7;
		steerergrid.setConstraints(steerer[5], f);
		steererPanel.add(steerer[5]);
		f.gridx=1; f.gridy=8;
		steerergrid.setConstraints(submitbutton, f);
		steererPanel.add(submitbutton);

		correctorPanel.add(dch22);
		correctorPanel.add(dch24);
		correctorPanel.add(dch28);
		correctorPanel.add(dch30);

		newlayout.setConstraints(requestPanel, 20, 20, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(requestPanel);

		newlayout.setConstraints(solvePanel, 20, 275, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(solvePanel);

		newlayout.setConstraints(maxtimelabel, 200, 170, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(maxtimelabel);

		newlayout.setConstraints(maxtime, 200, 310, 270, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(maxtime);

		newlayout.setConstraints(syncstate, 235, 177, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);	mainPanel.add(syncstate);

		//newlayout.setConstraints(resultsscrollpane, 200, 400, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		//mainPanel.add(resultsscrollpane);
		newlayout.setConstraints(steererscrollpane, 285, 10, 100, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(steererscrollpane);

		newlayout.setConstraints(calcbutton, 415, 180, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(calcbutton);

		newlayout.setConstraints(submitbutton, 455, 60, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(submitbutton);

		newlayout.setConstraints(revertbutton, 455, 260, 200, 100, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
		mainPanel.add(revertbutton);

	}

	public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(900,500));
		mainPanel.setVisible(true);

		resultsPanel = new JPanel();
		resultsPanel.setPreferredSize(new Dimension(200,150));
		resultsPanel.setBorder(BorderFactory.createTitledBorder("Measured Position at Foil"));

		requestPanel = new JPanel();
		requestPanel.setPreferredSize(new Dimension(200,150));
		requestPanel.setBorder(BorderFactory.createTitledBorder("Desired Change at Foil"));

		solvePanel = new JPanel();
		solvePanel.setPreferredSize(new Dimension(200,150));
		solvePanel.setBorder(BorderFactory.createTitledBorder("Solver Results"));

		steererPanel = new JPanel();
		steererPanel.setPreferredSize(new Dimension(200,300));
		steererPanel.setBorder(BorderFactory.createTitledBorder("Steerer Setpoints"));

		correctorPanel = new JPanel();
		correctorPanel.setPreferredSize(new Dimension(200,100));
		correctorPanel.setBorder(BorderFactory.createTitledBorder("Choose H Correctors to Vary"));
		dch28.setSelected(true);
		dch30.setSelected(true);

		correctorlist = new ArrayList<>();
		correctorlist.add("HEBT_Mag:DCH22");
		correctorlist.add("HEBT_Mag:DCH24");
		correctorlist.add("HEBT_Mag:DCH28");
		correctorlist.add("HEBT_Mag:DCH30");
		correctorlist.add("HEBT_Mag:DCV29");
		correctorlist.add("HEBT_Mag:DCV31");

		makeSteererTable();
		//makeSteererResultsTable();

		calcbutton = new JButton("Calculate Steerers");
		calcbutton.setBackground(mycolor);
		submitbutton = new JButton("Submit Selected Steerers");
		submitbutton.setBackground(mycolor);
		revertbutton = new JButton("Revert to Previous Values");
		revertbutton.setBackground(mycolor);

		avgFor = NumberFormat.getNumberInstance();
		avgFor.setMaximumFractionDigits(3);

		steererFor = NumberFormat.getNumberInstance();
		steererFor.setMinimumFractionDigits(5);

		syncstate = new JComboBox<>(syncstates);

		x = new DecimalField(0.0, 6, avgFor);
		x.setMinimumSize( x.getPreferredSize() );

		xp = new DecimalField(0.0, 6, avgFor);
		xp.setMinimumSize( xp.getPreferredSize() );

		y = new DecimalField(0.0, 6, avgFor);
		y.setMinimumSize( y.getPreferredSize() );

		yp = new DecimalField(0.0, 6, avgFor);
		yp.setMinimumSize( yp.getPreferredSize() );

		xnew = new DecimalField(0.0, 6, avgFor);
		xnew.setMinimumSize( xnew.getPreferredSize() );

		xpnew = new DecimalField(0.0, 6, avgFor);
		xpnew.setMinimumSize( xpnew.getPreferredSize() );

		ynew = new DecimalField(0.0, 6, avgFor);
		ynew.setMinimumSize( ynew.getPreferredSize() );

		ypnew = new DecimalField(0.0, 6, avgFor);
		ypnew.setMinimumSize( ypnew.getPreferredSize() );


		maxtime= new DecimalField(100.0, 3, avgFor);
		initsptm= new DecimalField(0.20469, 4, avgFor);

		for(int i=0; i<=3; i++)
			spot[i] = new DecimalField(0.0, 6, avgFor);
		for(int i=0; i<=6; i++)
			steerer[i]= new DecimalField(0.0, 8, steererFor);

		for(int i=0; i<4; i++) inj_params[i]=0.0;
	}

	public void setAction(){

		syncstate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(syncstate.getSelectedIndex() == 0){
					latticestate="Live";
				}
				if(syncstate.getSelectedIndex() == 1){
					latticestate="Design";
				}
			}
		});


		calcbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				//calculateInitConditions();
				new_params[0]=xnew.getDoubleValue();
				new_params[1]=xpnew.getDoubleValue();
				new_params[2]=ynew.getDoubleValue();
				new_params[3]=ypnew.getDoubleValue();
				//System.out.println("inits are " +xnew.getValue() +  " " + xpnew.getValue());

				ArrayList<String> correctorlist = new ArrayList<>();
				for(int i=0; i <= steerertable.getRowCount()-1; i++){
					steerertable.setValueAt(new Boolean(false), i, 3);
					if(((Boolean)steerertable.getValueAt(i, 1)).booleanValue() == true){
						correctorlist.add( (String)steerertable.getValueAt(i,0) );
						steerertable.setValueAt(new Boolean(true), i, 3);
					}
				}

				CalculateSteerers calcsteerers = new CalculateSteerers(doc, latticestate, correctorlist, maxtime.getDoubleValue());

				calcsteerers.run(new_params);

				double[] final_values = calcsteerers.getFinalSpot();
				double[] final_steerers = calcsteerers.getFinalSteerers();
				for(int i=0; i<4; i++){
					spot[i].setValue(final_values[i]);
				}
				for(int i=0; i<6; i++){
					steerertablemodel.setValueAt(final_steerers[i], i, 2);
				}
				steerertablemodel.fireTableDataChanged();

			}
		});


		submitbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				saveCorrectors();
				setCorrectors();
			}
		});

		revertbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent ae) {
				revertCorrectors();
			}
		});

	}


	public void setUpModel(){
		accl = new Accelerator();
		accl = doc.getAccelerator();
		AcceleratorSeq hebt1=accl.getSequence("HEBT1");
		AcceleratorSeq hebt2=accl.getSequence("HEBT2");
		ArrayList<AcceleratorSeq> lst = new ArrayList<>();
		lst.add(hebt1);
		lst.add(hebt2);
		seq = new AcceleratorSeqCombo("HEBT", lst);
	}


	public void makeSteererTable(){
		String[] colnames = {"Device", "Make Variable", "Suggested Value", "Submit"};
		//steerertablemodel = new SteererTableModel(colnames, doc.nsteerers);
		steerertablemodel = new SteererTableModel(colnames, 6);
		steerertable = new JTable(steerertablemodel);
		steerertable.getColumnModel().getColumn(0).setPreferredWidth(120);
		steerertable.getColumnModel().getColumn(1).setPreferredWidth(120);
		steerertable.getColumnModel().getColumn(2).setPreferredWidth(120);
		steerertable.getColumnModel().getColumn(3).setPreferredWidth(120);
		steerertable.setPreferredScrollableViewportSize(steerertable.getPreferredSize());
		steerertable.setRowSelectionAllowed(false);
		steerertable.setColumnSelectionAllowed(false);
		steerertable.setCellSelectionEnabled(false);

		steererscrollpane = new JScrollPane(steerertable);
		steererscrollpane.getVerticalScrollBar().setValue(0);
		steererscrollpane.setPreferredSize(new Dimension(480, 115));

		//Set some initial values.
		int i =0;
		boolean initial = false;
		for ( final String name : correctorlist ) {
			steerertablemodel.setValueAt(name, i, 0);
			if(i<2){
				steerertablemodel.setValueAt(new Boolean(false), i, 1);
			}
			else{
				steerertablemodel.setValueAt(new Boolean(true), i, 1);
			}
			steerertablemodel.setValueAt(0.0, i, 2);
			steerertablemodel.setValueAt(new Boolean(false), i, 3);
			i++;
		}
		steerertablemodel.fireTableDataChanged();
	}

	public void saveCorrectors(){

		lastfields.clear();


		for(int i=0; i<steerers.length; i++){
			double field = 0.0;
			String name = steerers[i];
			Dipole corrector=(Dipole)seq.getNodeWithId(name);
			try{
				field = corrector.getField();
			}
			catch(Exception e){
				e.printStackTrace();
			}
			lastfields.put(name, new Double(field));
			//System.out.println("Saving " + name + " with field " + field);
		}

	}


	public void setCorrectors(){
		//System.out.println("row count is " + steerertablemodel.getRowCount());
		for(int i=0; i <= steerertablemodel.getRowCount()-1; i++){
			if(((Boolean)steerertable.getValueAt(i, 3)).booleanValue() == true){
				String name = (String)steerertable.getValueAt(i,0);
				double value = ((Double)steerertable.getValueAt(i,2)).doubleValue();

				try{
					((Dipole)seq.getNodeWithId(name)).setField(value);
				}
				catch(ConnectionException exception){
					exception.printStackTrace();
				}
				catch(PutException exception){
					exception.printStackTrace();
				}
			}
		}
		Channel.flushIO();
	}

	public void revertCorrectors(){

		for(int i=0; i <= steerertablemodel.getRowCount()-1; i++){
			if(((Boolean)steerertable.getValueAt(i, 3)).booleanValue() == true){
				String name = (String)steerertable.getValueAt(i,0);
				Double value = lastfields.get(name);
				//System.out.println("Assigning " + name + " field value of " + value.doubleValue());
				try{
					((Dipole)seq.getNodeWithId(name)).setField(value.doubleValue());
				}
				catch(ConnectionException exception){
					exception.printStackTrace();
				}
				catch(PutException exception){
					exception.printStackTrace();
				}
			}
		}
		Channel.flushIO();
	}


	public void updateInjSpot(double[] params) {
		x.setValue(params[0]);
		xp.setValue(params[1]);
		y.setValue(params[2]);
		yp.setValue(params[3]);
		inj_params=params;
	}



}
/*

 class CheckBoxListener implements ItemListener{
 public void itemStateChanged(ItemEvent e){
	Object source e.getItemSelectable();
	if (source == chinButton){
	}
	if (source =
 */




