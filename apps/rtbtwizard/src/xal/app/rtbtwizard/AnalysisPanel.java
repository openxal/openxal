
package xal.app.rtbtwizard;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.JOptionPane;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.Timer.*;
import java.net.URL;
import java.util.List;
import java.io.*;
import java.lang.*;
import xal.tools.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.tools.plot.*;
import xal.tools.data.*;
import xal.tools.fit.lsm.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.solver.*;
//import xal.tools.formula.*;
import xal.tools.solver.hint.*;
import xal.tools.solver.algorithm.*;
import xal.tools.solver.market.*;
import xal.tools.solver.solutionjudge.*;

public class AnalysisPanel extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    
    //private StoredResultsPanel storedresultspanel;
    private DataTable masterdatabase;
    private DataTable resultsdatatable;
    
    private JButton removebutton;
    private JButton fitbutton;
    private JButton storebutton;
    private JButton hnormbutton;
    private JButton vnormbutton;
    private JButton vcutbutton;
    private JButton centerbutton;
    private JButton clearbutton;
    private JButton gnormbutton;
    private JButton fitthreshbutton;
    private JButton statfitbutton;
    private JButton fitallbutton;
    
    private JLabel sigma1label;
    private JLabel sigma2label;
    private JLabel rmslabel;
    private JLabel amp1label;
    private JLabel amp2label;
    private JLabel centerlabel;
    private JLabel pedestallabel;
    private JLabel[] rlabels = new JLabel[3];
    private JPanel fitresultspanel;
    private JPanel vcutpanel;
    private JPanel hnormpanel;
    private JPanel vnormpanel;
    private JPanel centerpanel;
    private JPanel fitthreshpanel;
    
    private boolean dataHasBeenFit = false;
    private boolean linearplot = true;
    private boolean freezefloor = true;
    private boolean thresholdexists = false;
    private boolean gaussfit = false;
    private String filename;
    private String wirename;
    private String direction;
    private String label;
    private String[] plottypes = {"Plot Linear Values", "Plot Log Values"};
    private String[] flooroptions = {"Freeze Offset at 0.0", "Fit Offset"};
    private String[] calcmodes = {"Two Gauss", "SuperGauss", "Two Super Gauss"};
    
    private JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
    private JComboBox<String> floorchooser = new JComboBox<String>(flooroptions);
    private JComboBox<String> calcmodechooser = new JComboBox<String>(calcmodes);
    
    NumberFormat numFor = NumberFormat.getNumberInstance();
    
    DecimalField[] result = new DecimalField[7];
    DecimalField[] err = new DecimalField[7];
    DecimalField vcut;
    DecimalField hnorm;
    DecimalField vnorm;
    DecimalField center;
    DecimalField fitthreshold;
    
    String currentdataname;
    ArrayList<ArrayList<Double>> currentdata;
    
    double sdata[];
    double data[];
    double[] snewdata;
    double[] newdata;
    double fitparams[] = new double[9];
    double fitparams_err[] = new double[9];
    GenDocument doc;
    EdgeLayout layout = new EdgeLayout();
    FunctionGraphsJPanel datapanel;
    ArrayList<DataAttribute> attributes;
    DataTableModel datatablemodel;
    
    
    public AnalysisPanel(){}
    //Member function Constructor
    public AnalysisPanel(GenDocument aDocument, DataTableModel dtm){
		
		datatablemodel = dtm;
		doc=aDocument;
		//storedresultspanel = new StoredResultsPanel(doc);
        
		makeComponents(); //Creation of all GUI components
		setStyling();     //Set the styling of components
		addComponents();  //Add all components to the layout and panels
        
		setAction();      //Set the action listeners
        
    }
    
    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		layout.add(datapanel, mainPanel, 10, 15, EdgeLayout.LEFT);
		layout.add(scalechooser, mainPanel, 15, 240, EdgeLayout.LEFT);
		layout.add(removebutton, mainPanel, 15, 275, EdgeLayout.LEFT);
		layout.add(hnormpanel, mainPanel, 10, 310, EdgeLayout.LEFT);
		layout.add(centerpanel, mainPanel, 10, 350, EdgeLayout.LEFT);
		layout.add(vnormpanel, mainPanel, 10, 390, EdgeLayout.LEFT);
		layout.add(vcutpanel, mainPanel, 10, 425, EdgeLayout.LEFT);
        
		layout.add(fitallbutton, mainPanel, 220, 260, EdgeLayout.LEFT);
		layout.add(fitbutton, mainPanel, 345, 260, EdgeLayout.LEFT);
		layout.add(statfitbutton, mainPanel, 240, 290, EdgeLayout.LEFT);
		layout.add(fitresultspanel, mainPanel, 225, 320, EdgeLayout.LEFT);
		layout.add(storebutton, mainPanel, 75, 500, EdgeLayout.LEFT);
		layout.add(clearbutton, mainPanel, 205, 500, EdgeLayout.LEFT);
		layout.add(calcmodechooser, mainPanel, 270, 230, EdgeLayout.LEFT);
        
		this.add(mainPanel);
    }
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(490, 530));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Profile Analysis"));
		//mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
		datapanel = new FunctionGraphsJPanel();
		datapanel.setPreferredSize(new Dimension(390, 210));
		datapanel.setGraphBackGroundColor(Color.WHITE);
        
		currentdata = new ArrayList<ArrayList<Double>>();
        
		attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("file", new String("").getClass(), true) );
		attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
		attributes.add(new DataAttribute("direction", new String("").getClass(), true) );
		attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
		resultsdatatable = new DataTable("DataTable", attributes);
		
		removebutton = new JButton("Remove Point");
		fitbutton = new JButton("Fit Current Data");
		storebutton = new JButton("Store Results");
		hnormbutton = new JButton("H Normalize By:");
		vnormbutton = new JButton("V Normalize To:");
		//gnormbutton = new JButton("Normalize By Fit");
		vcutbutton = new JButton("V Cut Below:     ");
		centerbutton = new JButton("H Offset By:      ");
		clearbutton = new JButton("Clear Stored Results");
		statfitbutton = new JButton("Stat RMS Current Data Set");
		fitallbutton = new JButton("Fit & Store All");
		//fitthreshbutton = new JButton("Fit Data Above:");
        
		filename = new String("");
		wirename = new String("");
		direction = new String("");
		label = new String("");
        
		numFor.setMinimumFractionDigits(3);
		for(int i=0; i<=6; i++){
			result[i] = new DecimalField(0,6,numFor);
			err[i] = new DecimalField(0,6,numFor);
		}
		hnorm = new DecimalField(1.0,4,numFor);
		vnorm = new DecimalField(1.0,4,numFor);
		vcut = new DecimalField(0.01,4,numFor);
		center = new DecimalField(0.0,4,numFor);
		fitthreshold = new DecimalField(0.0,4,numFor);
        
		rlabels[0] = new JLabel("Parameter");
		rlabels[1] = new JLabel("   Value");
		rlabels[2]= new JLabel("   Error");
        
		sigma1label = new JLabel("Sigma1 (s1) ");
		sigma2label = new JLabel("Sigma2 (s2) ");
		rmslabel = new JLabel("RMS         ");
		amp1label = new JLabel("Amp1 (a1)  ");
		amp2label = new JLabel("Amp2 (a2)  ");
		centerlabel = new JLabel("Center (x0) ");
		pedestallabel = new JLabel("Offset (y0) ");
        
		fitresultspanel = new JPanel();
		fitresultspanel.setPreferredSize(new Dimension(250, 170));
		fitresultspanel.setBorder(BorderFactory.createTitledBorder("Fit Results"));
		fitresultspanel.setLayout(new GridLayout(8,2));
		fitresultspanel.add(rlabels[0]); fitresultspanel.add(rlabels[1]); //fitresultspanel.add(rlabels[2]);
		fitresultspanel.add(amp1label); fitresultspanel.add(result[0]);  //fitresultspanel.add(err[0]);
		fitresultspanel.add(amp2label); fitresultspanel.add(result[1]); //fitresultspanel.add(err[1]);
		fitresultspanel.add(sigma1label); fitresultspanel.add(result[2]);  //fitresultspanel.add(err[2]);
		fitresultspanel.add(sigma2label); fitresultspanel.add(result[3]); //fitresultspanel.add(err[3]);
		fitresultspanel.add(rmslabel); fitresultspanel.add(result[4]); //fitresultspanel.add(err[3]);
		fitresultspanel.add(centerlabel); fitresultspanel.add(result[5]); //fitresultspanel.add(err[4]);
		fitresultspanel.add(pedestallabel); fitresultspanel.add(result[6]); //fitresultspanel.add(err[5]);
        
		vcutpanel = new JPanel();
		vcutpanel.add(vcutbutton);
		vcutpanel.add(vcut);
        
		vnormpanel = new JPanel();
		vnormpanel.add(vnormbutton);
		vnormpanel.add(vnorm);
        
		hnormpanel = new JPanel();
		hnormpanel.add(hnormbutton);
		hnormpanel.add(hnorm);
        
		centerpanel = new JPanel();
		centerpanel.add(centerbutton);
		centerpanel.add(center);
		
		calcmodechooser.setSelectedIndex(1);
        
		//fitthreshpanel = new JPanel();
		//fitthreshpanel.add(fitthreshbutton);
		//fitthreshpanel.add(fitthreshold);
        
	}
	
	
    public void setAction(){
		scalechooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(scalechooser.getSelectedIndex()==0){
					linearplot = true;
				}
				else{
					linearplot = false;
				}
				plotData();
			}
		});
		/*
		 floorchooser.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
         if(floorchooser.getSelectedIndex()==0){
         freezefloor = true;
         }
         else{
         freezefloor = false;
         }
         }
		 });
		 */
        
		calcmodechooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(calcmodechooser.getSelectedIndex()==0){
					gaussfit = true;
					doc.lastfitSuperGauss = false;
					System.out.println("Changing to " + doc.lastfitSuperGauss);
				}
				else{
					gaussfit = false;
					doc.lastfitSuperGauss = true;
					System.out.println("Changing to " + doc.lastfitSuperGauss);
				}
			}
		});
		
		removebutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				removePoint();
			}
		});
        
		fitallbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
                
				thresholdexists=false;
                
				//DataTableModel datatablemodel = pf.getDataTableModel();
				System.out.println(datatablemodel);
				for(int i=0;i<datatablemodel.getRowCount();i++){
                    
					String filename = (String)datatablemodel.getValueAt(i,0);
					String wire = (String)datatablemodel.getValueAt(i,1);
                    
					resetCurrentData(filename, wire, "H");
					if (gaussfit) {
						gaussFit(thresholdexists, 0.0);
					}
					else {
						superGaussFit(thresholdexists, 0.0);
					}
					storeResult();
					resetCurrentData(filename, wire, "V");
					if (gaussfit) {
						gaussFit(thresholdexists, 0.0);
					}
					else {
						superGaussFit(thresholdexists, 0.0);
					}
					storeResult();
                    
				}
			}
		});
        
		fitbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				thresholdexists=false;
				if(gaussfit){
					gaussFit(thresholdexists, 0.0);
				}
				else{
					superGaussFit(thresholdexists, 0.0);
				}
			}
		});
        
		hnormbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				normalizeHorizontal();
			}
		});
        
		vnormbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				normalizeVertical();
			}
		});
        
		/*
         gnormbutton.addActionListener(new ActionListener(){
         public void actionPerformed(ActionEvent e) {
         normalizeByGaussian();
         }
		 });
		 */
		
		vcutbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cutVertical();
			}
		});
        
		centerbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				centerOffset();
			}
		});
        
		storebutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				storeResult();
			}
		});
        
		clearbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				clearResults();
			}
		});
		
		statfitbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				statFit(thresholdexists, fitthreshold.getDoubleValue());
			}
		});
    }
    
    
    public void removePoint(){
        
	    Integer index =datapanel.getPointChosenIndex();
	    
	    if(index != null){
			int newsize = sdata.length - 1;
			int iindex = index.intValue();
			double[] oldsdata = sdata;
			double[] olddata = data;
			double[] tempsdata = new double[newsize];
			double[] tempdata = new double[newsize];
            
			for(int i=0; i<newsize; i++){
				if(i < iindex){
					tempsdata[i]=oldsdata[i];
					tempdata[i]=olddata[i];
				}
				else{
					tempsdata[i]=oldsdata[i+1];
					tempdata[i]=olddata[i+1];
				}
			}
            
			sdata = tempsdata;
			data = tempdata;
            
			plotData();
	    }
    }
    
    
    public void normalizeHorizontal(){
		double norm = hnorm.getDoubleValue();
		int size = sdata.length;
		double max = 0.0;
		for(int i=0; i<size; i++) sdata[i] /= norm;
        
		dataHasBeenFit=false;
		plotData();
	}
    
    
    public void normalizeVertical(){
		double norm = vnorm.getDoubleValue();
		int size = sdata.length;
		double max = 0.0;
		for(int i=0; i<size; i++) if(data[i] > max) max = data[i];
        
		if(max != 0.0){
			for(int i=0; i<size; i++) data[i] *= norm/max;
		}
		dataHasBeenFit=false;
		plotData();
	}
    
    
    public void cutVertical(){
		double threshold = vcut.getDoubleValue();
		int gooddatapoints = 0;
		int currentsize = sdata.length;
        
		for(int i=0; i<currentsize; i++) if(data[i] >= threshold) gooddatapoints++;
        
		double[] tempsdata = new double[gooddatapoints];
		double[] tempdata = new double[gooddatapoints];
        
		int j=0;
		for(int i=0; i<currentsize; i++){
			if(data[i] >= threshold){
				tempsdata[j] = sdata[i];
				tempdata[j] = data[i];
				j++;
			}
		}
		
		sdata = tempsdata;
		data = tempdata;
        
		dataHasBeenFit=false;
		plotData();
    }
	
    public void centerOffset(){
		double cent = center.getDoubleValue();
		int size = sdata.length;
		for(int i=0; i<size; i++) sdata[i] -= cent;
		dataHasBeenFit=false;
		plotData();
    }
	
    public void gaussFit(boolean datathresh, double vthresh){
		
		if(datathresh){
			double threshold = vthresh;
			int gooddatapoints = 0;
			int currentsize = sdata.length;
            
			for(int i=0; i<currentsize; i++) if(data[i] >= threshold) gooddatapoints++;
            
			double[] tempsdata = new double[gooddatapoints];
			double[] tempdata = new double[gooddatapoints];
            
			int j=0;
			for(int i=0; i<currentsize; i++){
				if(data[i] >= threshold){
					tempsdata[j] = sdata[i];
					tempdata[j] = data[i];
					j++;
				}
			}
			
			snewdata = tempsdata;
			newdata = tempdata;
		}
		else{
			snewdata = sdata;
			newdata = data;
		}
        
		//Guess at the initial fit parameters
		double xmax=0.0;
		double centguess=0.0;
		double width=0.0;
        
		int size=newdata.length;
		int imax = 0;
		for(int i=0; i<size; i++){
			if(newdata[i] > xmax){
				xmax=newdata[i];
				imax = i;
			}
		}
		double leftedge=snewdata[0];
		double rightedge=snewdata[size-1];
		for(int i=0; i<size; i++){
			if(newdata[i] > xmax*0.1){
				leftedge=snewdata[i];
				break;
			}
		}
		for(int i=imax; i<size; i++){
			if(newdata[i] < xmax*0.1){
				rightedge=snewdata[i];
				break;
			}
		}
		centguess = (rightedge - leftedge)/2.0 + leftedge;
		width = (rightedge - centguess)/2.0;
        
		double amp1 = 2*xmax; double amp2 = amp1/2.0;
		double sigma1 = width; double sigma2 = sigma1/2.0;
		double center = centguess;
		double offset = 0.0;
        
		//Do the fit.
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		variables.add(new Variable("amp1",amp1, 0, 50.0));
		variables.add(new Variable("amp2",amp2, 0, 50.0));
		variables.add(new Variable("sigma1",sigma1, 1, 50));
		variables.add(new Variable("sigma2",sigma2, 1, 50));
		variables.add(new Variable("center",center, -200, 200));
		variables.add(new Variable("offset",offset, -0.1, 0.1));
        
		ArrayList<Objective> objectives = new ArrayList<Objective>();
		objectives.add(new TargetObjective( "diff", 0.0 ) );
        
		Evaluator1 evaluator = new Evaluator1( objectives, variables );
        
		Problem problem = new Problem( objectives, variables, evaluator );
		problem.addHint(new InitialDelta( 0.05) );
        
		double solvetime= 2;
		Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper( 1, solvetime, 0.999 );
		Solver solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper );
        
		solver.solve( problem );
		System.out.println("score is " + solver.getScoreBoard());
		Trial best = solver.getScoreBoard().getBestSolution();
        
		// rerun with solution to populate results table
		calcError(variables, best);
		Iterator<Variable> itr = variables.iterator();
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = best.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("amp1")) amp1= value;
            if(name.equalsIgnoreCase("amp2")) amp2 = value;
            if(name.equalsIgnoreCase("sigma1")) sigma1 = value;
            if(name.equalsIgnoreCase("sigma2")) sigma2 = value;
            if(name.equalsIgnoreCase("center")) center = value;
            if(name.equalsIgnoreCase("offset")) offset = value;
        }
        
		//Get the RMS of the entire distribution
		double xmin = center - 5*sigma1;
		xmax = center + 5*sigma1;
		double points = 200.0;
		double inc = (xmax - xmin)/points;
		int npoints = (new Double(points)).intValue();
		double sfit[] = new double[npoints];
		double yfit[] = new double[npoints];
		double x = xmin;
		int i = 0;
        
		while(x <= xmax && i<npoints){
			sfit[i]=x;
			yfit[i] = amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2));
			x+=inc;
			i++;
		}
		double sqrrms=0.0;
		double rms=0.0;
		double totals=0.0;
		double least = 0.0;
		double mean = 0.0;
		double tot = 0.0;
		for(i=0; i<sfit.length; i++){
			mean+=sfit[i]*yfit[i];
			tot+=yfit[i];
		}
		mean /= tot;
        
		for(i=0; i<sfit.length; i++){
			sqrrms+=Math.pow((sfit[i]-mean),2)*yfit[i];
		}
        
		sqrrms /=tot;
		rms=Math.sqrt(sqrrms);
		fitparams[0] = amp1; result[0].setValue(amp1);
	    fitparams[1] = amp2; result[1].setValue(amp2);
	    fitparams[2] = sigma1; result[2].setValue(sigma1);
	    fitparams[3] = sigma2; result[3].setValue(sigma2);
	    fitparams[4] = rms; result[4].setValue(rms);
	    fitparams[5] = center; result[5].setValue(center);
	    fitparams[6] = offset; result[6].setValue(offset);
		fitparams[7] = 0.0; fitparams[8] = 0.0;
        
        
		dataHasBeenFit=true;
		plotData();
		//updateResultsPanel();
		
	}
    
    
    public double calcError(ArrayList<Variable> vars, Trial trial){
        
		double error = 0.0;
		double temp = 0.0;
		int size = snewdata.length;
		double amp1=0.0;
		double amp2=0.0;
		double sigma1=0.0;
		double sigma2=0.0;
		int exp1 = 0;
		int exp2 = 0;
		double center=0.0;
		double offset=0.0;
		double x;
        
		Iterator<Variable> itr = vars.iterator();
        while(itr.hasNext()){
            Variable variable = itr.next();
            double value = trial.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("amp1")) amp1= value;
            if(name.equalsIgnoreCase("amp2")) amp2 = value;
            if(name.equalsIgnoreCase("sigma1")) sigma1 = value;
            if(name.equalsIgnoreCase("sigma2")) sigma2 = value;
            if(name.equalsIgnoreCase("N1")) exp1 = (int)value;
            if(name.equalsIgnoreCase("N2")) exp2 = (int)value;
            if(name.equalsIgnoreCase("center")) center = value;
            if(name.equalsIgnoreCase("offset")) offset = value;
        }
        for(int i=0; i<size; i++){
            x=snewdata[i];
            if (gaussfit) {
                temp = amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2)) + offset;
            }
            else {
                temp = amp1*Math.exp(-(Math.pow(Math.abs((x-center)), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs((x-center)), exp2))/(2.0*(Math.pow(sigma2, exp2)))) + offset;
            }
            error += Math.pow((newdata[i] - temp), 2.0);
        }
		error = Math.sqrt(error);
		return error;
	}
    
    
	public void superGaussFit(boolean datathresh, double vthresh){
		
		
		if(datathresh){
			double threshold = vthresh;
			int gooddatapoints = 0;
			int currentsize = sdata.length;
			
			for(int i = 0; i<currentsize; i++) if(data[i] >= threshold) gooddatapoints++;
			
			double[] tempsdata = new double[gooddatapoints];
			double[] tempdata = new double[gooddatapoints];
			
			int j=0;
			for(int i=0; i<currentsize; i++){
				if(data[i] >= threshold){
					tempsdata[j] = sdata[i];
					tempdata[j] = data[i];
					j++;
				}
			}
			snewdata = tempsdata;
			newdata = tempdata;
		}
		else{
			snewdata = sdata;
			newdata = data;
		}
		
		//Guess at the initial fit parameters
		double xmax = 0.0;
		double centguess = 0.0;
		double width = 0.0;
		
		int size = newdata.length;
		int imax = 0;
		for(int i = 0; i < size; i++){
			if(newdata[i] > xmax){
				xmax = newdata[i];
				imax = i;
			}
		}
		double leftedge = snewdata[0];
		double rightedge = snewdata[size-1];
		for(int i = 0; i < size; i++){
			if(newdata[i] > xmax*0.1){
				leftedge = snewdata[i];
				break;
			}
		}
		for(int i = imax; i < size; i++){
			if(newdata[i] < xmax*0.1){
				rightedge = snewdata[i];
				break;
			}
		}
		centguess = (rightedge - leftedge)/2.0 + leftedge;
		width = (rightedge - centguess)/2.0;
		
		double amp1 = 2*xmax; double amp2 = amp1/2.0;
		double sigma1 = width; double sigma2 = sigma1/2.0;
		double center = centguess;
		double offset = 0.0;
		int exp1 = 5; int exp2 = 5;
		
		//Do the fit.
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		
		variables.add(new Variable("amp1", amp1, 0.0, 50.0));
		variables.add(new Variable("sigma1", sigma1, 1.0, 50.0));
		variables.add(new Variable("center", center, -200.0, 200.0));
		variables.add(new Variable("offset", offset, -0.1, 0.1));
		variables.add(new Variable("N1", exp1, 1, 10));
		if (calcmodechooser.getSelectedIndex()==1){
			amp2 =0.0; sigma2 = 0.0; exp2 = 0;
			variables.add(new Variable("amp2", amp2, 0.0, 0.0));
			variables.add(new Variable("sigma2", sigma2, 0.0, 0.0));
			variables.add(new Variable("N2", exp2, 0.0, 0));
		}
		else{
			variables.add(new Variable("amp2", amp2, 0.0, 50.0));
			variables.add(new Variable("sigma2", sigma2, 1.0, 50.0));
			variables.add(new Variable("N2", exp2, 1, 10));
		}
		
		
		ArrayList<Objective> objectives = new ArrayList<Objective>();
		objectives.add(new TargetObjective("diff", 0.0));
		
		Evaluator1 evaluator = new Evaluator1(objectives, variables);
		
		Problem problem = new Problem(objectives, variables, evaluator);
		problem.addHint(new InitialDelta(0.05));
		
		double solvetime=5.0;
		Stopper maxSolutionStopper = SolveStopperFactory.minMaxTimeSatisfactionStopper(1, solvetime, 0.999);
		Solver solver = new Solver(new RandomShrinkSearch(), maxSolutionStopper);
		
		solver.solve(problem);
		System.out.println("SuperGauss Fit Routine Results");
		System.out.println("score is " + solver.getScoreBoard());
		Trial best = solver.getScoreBoard().getBestSolution();
		
		// rerun with solution to populate results table
		calcError(variables, best);
		Iterator<Variable> itr = variables.iterator();
        while(itr.hasNext()){
			Variable variable = itr.next();
            double value = best.getTrialPoint().getValue(variable);
            String name = variable.getName();
            if(name.equalsIgnoreCase("amp1")) amp1 = value;
            if(name.equalsIgnoreCase("amp2")) amp2 = value;
            if(name.equalsIgnoreCase("sigma1")) sigma1 = value;
            if(name.equalsIgnoreCase("sigma2")) sigma2 = value;
            if(name.equalsIgnoreCase("center")) center = value;
            if(name.equalsIgnoreCase("offset")) offset = value;
            if(name.equalsIgnoreCase("N1")) exp1 = (int)value;
            if(name.equalsIgnoreCase("N2")) exp2 = (int)value;
		}
		
		//Get the RMS of the entire distribution
		double xmin = center - 5*sigma1;
		xmax = center + 5*sigma1;
		double points = 200.0;
		double inc = (xmax - xmin)/points;
		int npoints = (new Double(points)).intValue();
		double sfit[] = new double[npoints];
		double yfit[] = new double[npoints];
		double x = xmin;
		int i = 0;
		
		while(x <= xmax && i<npoints){
			sfit[i]=x;
			//yfit[i] = amp1*Math.exp(-(Math.pow(Math.abs(x-center), exp1)))/(2.0*(Math.pow(sigma1, exp1))) - amp2*Math.exp(-(Math.pow(Math.abs(x-center), exp2)))/(2.0*(Math.pow(sigma2, exp2)));
			yfit[i] = amp1*Math.exp(-(Math.pow(Math.abs((x-center)), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs((x-center)), exp2))/(2.0*(Math.pow(sigma2, exp2))));
			x+=inc;
			i++;
		}
		double sqrrms= 0.0;
		double rms= 0.0;
		double totals= 0.0;
		double least = 0.0;
		double mean = 0.0;
		double tot = 0.0;
		for(i=0; i<sfit.length; i++){
			mean+=sfit[i]*yfit[i];
			tot+=yfit[i];
		}
		mean /= tot;
		
		for(i=0; i<sfit.length; i++){
			sqrrms+=Math.pow((sfit[i]-mean),2)*yfit[i];
		}
		
		sqrrms /=tot;
		rms=Math.sqrt(sqrrms);
		
	    fitparams[0] = amp1; result[0].setValue(amp1);
	    fitparams[1] = amp2; result[1].setValue(amp2);
	    fitparams[2] = sigma1; result[2].setValue(sigma1);
	    fitparams[3] = sigma2; result[3].setValue(sigma2);
	    fitparams[4] = rms; result[4].setValue(rms);
	    fitparams[5] = center; result[5].setValue(center);
	    fitparams[6] = offset; result[6].setValue(offset);
		fitparams[7] = exp1; fitparams[8] = exp2;
		
		dataHasBeenFit=true;
		plotData();
		//updateResultsPanel();
		
    }
    
    
    public void statFit(boolean datathresh, double vthresh){
		
		double[] snewdata;
		double[] newdata;
		int currentsize = sdata.length;
        
		if(datathresh){
			double threshold = vthresh;
			int gooddatapoints = 0;
            
			for(int i=0; i<currentsize; i++) if(data[i] >= threshold) gooddatapoints++;
            
			double[] tempsdata = new double[gooddatapoints];
			double[] tempdata = new double[gooddatapoints];
            
			int j=0;
			for(int i=0; i<currentsize; i++){
				if(data[i] >= threshold){
					tempsdata[j] = sdata[i];
					tempdata[j] = data[i];
					j++;
				}
			}
			snewdata = tempsdata;
			newdata = tempdata;
			currentsize=gooddatapoints;
		}
		else{
			snewdata = sdata;
			newdata = data;
		}
        
		double mean=0.0;
		double sqrrms=0.0;
		double rms=0.0;
		double totals=0.0;
		for(int i=0; i<currentsize; i++){
			mean+=snewdata[i]*newdata[i];
			totals+=newdata[i];
		}
		mean /= totals;
        
		for(int i=0; i<currentsize; i++){
			sqrrms+=Math.pow((snewdata[i]-mean),2)*newdata[i];
		}
		sqrrms /=totals;
		rms=Math.sqrt(sqrrms);
        
        
		fitparams[0] = 0; result[0].setValue(0);
		fitparams[1] = 0; result[1].setValue(0);
		fitparams[2] = 0; result[2].setValue(0);
		fitparams[3] = 0; result[3].setValue(0);
		fitparams[4] = rms; result[4].setValue(rms);
		fitparams[5] = mean; result[5].setValue(mean);
		fitparams[6] = 0; result[6].setValue(0);
		fitparams[7] = 0.0; fitparams[8] = 0.0;
		//plotData();
		//updateResultsPanel();
		
	}
    
    
    public void storeResult(){
        
		GenericRecord record = new GenericRecord(resultsdatatable);
		ArrayList<double[]> results = new ArrayList<double[]>();
		double[] fit = new double[fitparams.length];
		System.arraycopy(fitparams, 0, fit, 0, fitparams.length);
		double[] errors = new double[fitparams_err.length];
		System.arraycopy(fitparams_err, 0, errors, 0, fitparams_err.length);
		results.add(fit);
		results.add(errors);
		results.add(sdata);
		results.add(data);
        
		Map<String, String> bindings = new HashMap<String, String>();
		bindings.put("file", filename);
		bindings.put("wire", wirename);
		bindings.put("direction", direction);
        
		if(resultsdatatable.record(bindings) != null){
			resultsdatatable.remove(resultsdatatable.record(bindings));
		}
	    
		record.setValueForKey(new String(filename), "file");
		record.setValueForKey(new String(wirename), "wire");
		record.setValueForKey(new String(direction), "direction");
		record.setValueForKey(new ArrayList<double[]>(results), "data");
        
		resultsdatatable.add(record);
		doc.wireresultsdatabase = resultsdatatable;
        
	}
	
	
    public void clearResults(){
		if(resultsdatatable.records().size() == 0){
			System.out.println("No records to remove!");
		}
		else{
			Collection<GenericRecord> records = resultsdatatable.records();
			Iterator<GenericRecord> itr = records.iterator();
			while(itr.hasNext()){
				GenericRecord record = itr.next();
				resultsdatatable.remove(record);
			}
			doc.wireresultsdatabase = resultsdatatable;
		}
	}
	
	
    public void plotData(){
		
		datapanel.removeAllGraphData();
        
		BasicGraphData rawgraphdata = new BasicGraphData();
		BasicGraphData fitgraphdata = new BasicGraphData();
		
		//Add the raw data
		if(!linearplot){
			double temp;
			double[] logdata = new double[data.length];
			for(int i=0; i<logdata.length; i++){
				temp=data[i];
				if(temp<=0.0) temp=0.00001;
				logdata[i] = Math.log(temp)/Math.log(10);
			}
			rawgraphdata.addPoint(sdata,logdata);
		}
		else{
			rawgraphdata.addPoint(sdata, data);
		}
		rawgraphdata.setDrawPointsOn(true);
		rawgraphdata.setDrawLinesOn(false);
		rawgraphdata.setGraphProperty("Legend", new String("raw data"));
		rawgraphdata.setGraphColor(Color.RED);
		datapanel.addGraphData(rawgraphdata);
        
		//Add the most recent fit, if one has been done
		if( dataHasBeenFit ){
			int i = 0;
			double xmin, xmax;
            
			xmin = fitparams[5] - 5*fitparams[2];
			xmax = fitparams[5] + 5*fitparams[2];
			
			double points=100.0;
			double inc = (xmax - xmin)/points;
			int npoints = (new Double(points)).intValue();
			double sfit[] = new double[npoints];
			double yfit[] = new double[npoints];
			double amp1 = 0.0;
			double amp2 = 0.0;
			double sigma1 = 0.0;
			double sigma2 = 0.0;
			double center = 0.0;
			double offset = 0.0;
			int exp1 = 0;
			int exp2 = 0;
            
			amp1=fitparams[0]; amp2=fitparams[1];
			sigma1=fitparams[2]; sigma2=fitparams[3];
			center=fitparams[5]; offset=fitparams[6];
			exp1=(int)fitparams[7]; exp2=(int)fitparams[8];
            
			double x = xmin;
			while(x <= xmax && i<npoints){
				sfit[i]=x;
				if (gaussfit) {
					yfit[i] = offset + amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2));
				}
				else {
					yfit[i] = offset + amp1*Math.exp(-(Math.pow(Math.abs((x-center)), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs((x-center)), exp2))/(2.0*(Math.pow(sigma2, exp2))));
				}
				x+=inc;
				i++;
			}
            
			if(!linearplot){
				double temp;
				double[] ylogfit = new double[yfit.length];
				for(int j=0; j<ylogfit.length; j++){
					temp=yfit[j];
					if(temp<=0.0) temp=0.00001;
					ylogfit[j] = Math.log(temp)/Math.log(10);
				}
				fitgraphdata.addPoint(sfit, ylogfit);
			}
			else{
				fitgraphdata.addPoint(sfit, yfit);
			}
			fitgraphdata.setDrawPointsOn(false);
			fitgraphdata.setDrawLinesOn(true);
			fitgraphdata.setGraphProperty("Legend", new String("fit data"));
			fitgraphdata.setGraphColor(Color.BLACK);
			rawgraphdata.setDrawLinesOn(false);
			datapanel.addGraphData(fitgraphdata);
		}
        
		datapanel.setLegendButtonVisible(true);
		datapanel.setChooseModeButtonVisible(true);
		datapanel.setName("   "+ label);
        
	}
    
    
    /*
     public void updateResultsPanel(){
     for(int i=0; i<=3; i++){
     result[i].setValue(fitparams[i]);
     err[i].setValue(fitparams_err[i]);
     }
     }
     */
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    public void resetCurrentData(String file, String wire, String direct){
		
		filename = file;
		wirename = wire;
		direction = direct;
		label = (new String(filename + ":" + wirename + ":" + direction));
        
		ArrayList<ArrayList<Double>> currentdat = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> wiredata = new ArrayList<ArrayList<Double>>();
        
		DataTable masterdatabase = doc.wiredatabase;
        
		Map<String, String> bindings = new HashMap<String, String>();
		bindings.put("file", filename);
		bindings.put("wire", wire);
		GenericRecord record =  masterdatabase.record(bindings);
		wiredata=(ArrayList<ArrayList<Double>>)record.valueForKey("data");
        
		ArrayList<Double> slist = wiredata.get(0);
		ArrayList<Double> xlist = wiredata.get(1);
		ArrayList<Double> ylist = wiredata.get(2);
		ArrayList<Double> zlist = wiredata.get(3);
        
		if(direction.equals("H")){
			slist = wiredata.get(4);
		}
		else{
			slist = wiredata.get(5);
		}
        
		double[] sdat = new double[slist.size()];
		double[] dat = new double[slist.size()];
        
		double xmax=0; double zmax=0;
        
		for(int i=0; i<slist.size(); i++){
			if(Math.abs((xlist.get(i)).doubleValue()) > Math.abs(xmax))
				xmax = (xlist.get(i)).doubleValue();
		}
        
		if(direction.equals("H")){
			currentdat.add(slist);
			currentdat.add(xlist);
			for(int i=0; i<slist.size(); i++){
				sdat[i]=(slist.get(i)).doubleValue();
				if (xmax < 0)
					dat[i]=-(xlist.get(i)).doubleValue();
				else
					dat[i]=(xlist.get(i)).doubleValue();
			}
		}
		
		if(direction.equals("V")){
			currentdat.add(slist);
			currentdat.add(ylist);
			for(int i=0; i<slist.size(); i++){
				sdat[i]=(slist.get(i)).doubleValue();
				if (xmax < 0)
					dat[i]=-(ylist.get(i)).doubleValue();
				else
					dat[i]=(ylist.get(i)).doubleValue();
			}
		}
		
		if(direction.equals("D")){
			currentdat.add(slist);
			currentdat.add(zlist);
			for(int i=0; i<slist.size(); i++){
				sdat[i]=Math.sqrt(2.0)*((slist.get(i)).doubleValue());
				if (xmax < 0)
					dat[i]=-(zlist.get(i)).doubleValue();
				else
					dat[i]=(zlist.get(i)).doubleValue();
			}
		}
		
		//Reset global data arrays.
		currentdata.clear();
		currentdata = currentdat;
		sdata = sdat;
		data = dat;
        
	}
    
    
    public void setStyling(){
		/*
		 Color color = new Color(150,170,200);
		 scalechooser.setBackground(color);
		 floorchooser.setBackground(color);
		 removebutton.setBackground(color);
		 fitbutton.setBackground(color);
		 storebutton.setBackground(color);
		 hnormbutton.setBackground(color);
		 vnormbutton.setBackground(color);
		 gnormbutton.setBackground(color);
		 vcutbutton.setBackground(color);
		 centerbutton.setBackground(color);
		 clearbutton.setBackground(color);
		 */
	}
    
    public void setNewDataFlag(boolean flag){
        dataHasBeenFit= !flag;
    }
    
    
    //Evaluates beam properties for a trial point
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
    
    
    // objective class for solver.
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
    
}
