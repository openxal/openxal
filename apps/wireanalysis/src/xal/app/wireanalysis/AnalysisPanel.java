/*************************************************************
 //
 // class AnalysisPanel:
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
import xal.extension.fit.lsm.*;
import xal.extension.solver.*;
import xal.extension.solver.hint.*;
import xal.extension.solver.algorithm.*;
import xal.extension.solver.market.*;
import xal.extension.solver.solutionjudge.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;


public class AnalysisPanel extends JPanel{

    /** ID for serializable version */
    private static final long serialVersionUID = 1L;

    public JPanel mainPanel;
    public JTable datatable;

    private StoredResultsPanel storedresultspanel;
    private DataTable masterdatatable;
    private DataTable resultsdatatable;

    private JButton removebutton;
    private JButton fitbutton;
    private JButton storebutton;
    private JButton hnormbutton;
    private JButton vnormbutton;
    private JButton vcutbutton;
	private JButton voffsetbutton;
	private JButton rangecutbutton;
    private JButton centerbutton;
    private JButton clearbutton;
    private JButton gnormbutton;
    private JButton anormbutton;
    private JButton fitthreshbutton;
    private JButton fitallbutton;

    private JLabel sigmalabel;
    private JLabel amplabel;
    private JLabel centerlabel;
    private JLabel pedestallabel;
    private JLabel[] rlabels = new JLabel[3];
    private JPanel fitresultspanel;
    private JPanel vcutpanel;
	private JPanel voffsetpanel;
	private JPanel rangecutpanel;
    private JPanel hnormpanel;
    private JPanel vnormpanel;
    private JPanel centerpanel;
    private JPanel fitthreshpanel;

    private boolean dataHasBeenFit = false;
    private boolean linearplot = true;
    private boolean freezefloor = false;
    private boolean thresholdexists = false;
    private boolean gaussfit = true;
    private boolean twogaussfit = false;
	private boolean supergaussfit = false;
    private String filename;
    private String wirename;
    private String direction;
    private String label;
    private String[] plottypes = {"Plot Linear Values", "Plot Log Values"};
    private String[] flooroptions = {"Freeze Offset at 0.0", "Fit Data Offset"};
    private String[] calcmodes = {"Single Gauss Fit", "Two Gauss Fit", "Super Gauss Fit", "Two Super Gauss Fit", "Statistical RMS"};

    private JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
    private JComboBox<String> floorchooser = new JComboBox<String>(flooroptions);
    private JComboBox<String> calcmodechooser = new JComboBox<String>(calcmodes);

    NumberFormat numFor = NumberFormat.getNumberInstance();
	NumberFormat numRan = NumberFormat.getNumberInstance();

    DecimalField[] result = new DecimalField[4];
    DecimalField[] err = new DecimalField[4];
    DecimalField vcut;
	DecimalField voffset;
	DecimalField rangecut1;
	DecimalField rangecut2;
    DecimalField hnorm;
    DecimalField vnorm;
    DecimalField centerfield;
    DecimalField fitthreshold;

    String currentdataname;
    ArrayList<ArrayList<Double>> currentdata;

    double sdata[];
    double data[];
    double fitparams[] = new double[4];
    double fitparams_err[] = new double[4];
    double twogaussparams[] = new double[6];
	double supergaussparams[] = new double[8];
    GenDocument doc;
    DataTableModel datatablemodel;
    EdgeLayout layout = new EdgeLayout();
    FunctionGraphsJPanel datapanel;
    ArrayList<DataAttribute> attributes;
    //The following variables are for the two- and super-Gaussian fits.
    double[] stempdata;
    double[] tempdata;
    double amp1; double amp2;
    double sigma1; double sigma2;
    double center;
    double offset;
	int exp1;
	int exp2;

    public AnalysisPanel(){}
    //Member function Constructor
    public AnalysisPanel(GenDocument aDocument, DataTableModel dtm){

		doc=aDocument;
		datatablemodel = dtm;
		storedresultspanel = new StoredResultsPanel(doc);

		makeComponents(); //Creation of all GUI components
		setStyling();     //Set the styling of components
		addComponents();  //Add all components to the layout and panels

		setAction();      //Set the action listeners

    }


    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		layout.add(datapanel, mainPanel, 10, 15, EdgeLayout.LEFT);
		layout.add(scalechooser, mainPanel, 15, 235, EdgeLayout.LEFT);
		layout.add(removebutton, mainPanel, 10, 265, EdgeLayout.LEFT);
		layout.add(hnormpanel, mainPanel, 5, 291, EdgeLayout.LEFT);
		layout.add(centerpanel, mainPanel, 5, 322, EdgeLayout.LEFT);
		layout.add(vnormpanel, mainPanel, 5, 353, EdgeLayout.LEFT);
		layout.add(vcutpanel, mainPanel, 5, 384, EdgeLayout.LEFT);
		layout.add(voffsetpanel, mainPanel, 5, 415, EdgeLayout.LEFT);
		layout.add(rangecutpanel, mainPanel, 5, 446, EdgeLayout.LEFT);
		layout.add(gnormbutton, mainPanel, 10, 490, EdgeLayout.LEFT);
		layout.add(anormbutton, mainPanel, 10, 515, EdgeLayout.LEFT);
		layout.add(fitallbutton, mainPanel, 225, 235, EdgeLayout.LEFT);
		layout.add(calcmodechooser, mainPanel, 225, 265, EdgeLayout.LEFT);
		layout.add(floorchooser, mainPanel, 225, 295, EdgeLayout.LEFT);

		layout.add(fitbutton, mainPanel, 225, 335, EdgeLayout.LEFT);
		//layout.add(fitthreshpanel, mainPanel, 225, 255, EdgeLayout.LEFT);
		layout.add(fitresultspanel, mainPanel, 245, 375, EdgeLayout.LEFT);
		layout.add(storebutton, mainPanel, 65, 545, EdgeLayout.LEFT);
		layout.add(clearbutton, mainPanel, 195, 545, EdgeLayout.LEFT);

		this.add(mainPanel);
	}


    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(460, 575));
		mainPanel.setBorder(BorderFactory.createTitledBorder("Profile Analysis"));
		//mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());

		datapanel = new FunctionGraphsJPanel();
		datapanel.setPreferredSize(new Dimension(400, 210));
		datapanel.setGraphBackGroundColor(Color.WHITE);

		currentdata = new ArrayList<ArrayList<Double>>();

		attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("file", new String("").getClass(), true) );
		attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
		attributes.add(new DataAttribute("direction", new String("").getClass(), true) );
		attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
		resultsdatatable = new DataTable("DataTable", attributes);

		removebutton = new JButton("Remove Point");
		fitbutton = new JButton("Fit Current Data Only");
		storebutton = new JButton("Store Results");
		hnormbutton = new JButton("H Normalize By:");
		vnormbutton = new JButton("V Normalize To:");
		gnormbutton = new JButton("Normalize By Fit");
		anormbutton = new JButton("Normalize By Area");
		vcutbutton = new JButton("V Cut Below:     ");
		rangecutbutton = new JButton("Set Range: ");
		voffsetbutton = new JButton("V Offset By:     ");
		centerbutton = new JButton("H Offset By:      ");
		clearbutton = new JButton("Clear Stored Results");
		fitthreshbutton = new JButton("Fit Data Above:");
		fitallbutton = new JButton("Fit All Profiles");
		floorchooser.setSelectedIndex(1);

		filename = new String("");
		wirename = new String("");
		direction = new String("");
		label = new String("");

		numRan.setMinimumFractionDigits(1);
		numFor.setMinimumFractionDigits(3);
		for(int i=0; i<=3; i++){
			result[i] = new DecimalField(0,6,numFor);
			err[i] = new DecimalField(0,6,numFor);
		}
		hnorm = new DecimalField(1.0,4,numFor);
		vnorm = new DecimalField(1.0,4,numFor);
		vcut = new DecimalField(0.01,4,numFor);
		rangecut1 = new DecimalField(-100,0,numRan);
		rangecut2 = new DecimalField(100,0,numRan);
		voffset = new DecimalField(0.01,4,numFor);
		centerfield = new DecimalField(0.0,4,numFor);
		fitthreshold = new DecimalField(0.0,4,numFor);

		rlabels[0] = new JLabel("Parameter");
		rlabels[1] = new JLabel("   Value");
		rlabels[2]= new JLabel("   Error");

		sigmalabel = new JLabel(" Sigma = ");
		amplabel = new JLabel(" Amp. = ");
		centerlabel = new JLabel(" Center = ");
		pedestallabel = new JLabel(" Offset = ");

		fitresultspanel = new JPanel();
		fitresultspanel.setPreferredSize(new Dimension(200, 130));
		fitresultspanel.setBorder(BorderFactory.createTitledBorder("Fit Results"));
		fitresultspanel.setLayout(new GridLayout(5,3));
		fitresultspanel.add(rlabels[0]); fitresultspanel.add(rlabels[1]); fitresultspanel.add(rlabels[2]);
		fitresultspanel.add(sigmalabel); fitresultspanel.add(result[0]);  fitresultspanel.add(err[0]);
		fitresultspanel.add(amplabel); fitresultspanel.add(result[1]); fitresultspanel.add(err[1]);
		fitresultspanel.add(centerlabel); fitresultspanel.add(result[2]); fitresultspanel.add(err[2]);
		fitresultspanel.add(pedestallabel); fitresultspanel.add(result[3]); fitresultspanel.add(err[3]);

		vcutpanel = new JPanel();
		vcutpanel.add(vcutbutton);
		vcutpanel.add(vcut);

		voffsetpanel = new JPanel();
		voffsetpanel.add(voffsetbutton);
		voffsetpanel.add(voffset);
		rangecutpanel = new JPanel();
		rangecutpanel.add(rangecutbutton);
		rangecutpanel.add(rangecut1);
		rangecutpanel.add(rangecut2);


		vnormpanel = new JPanel();
		vnormpanel.add(vnormbutton);
		vnormpanel.add(vnorm);

		hnormpanel = new JPanel();
		hnormpanel.add(hnormbutton);
		hnormpanel.add(hnorm);

		centerpanel = new JPanel();
		centerpanel.add(centerbutton);
		centerpanel.add(centerfield);

		fitthreshpanel = new JPanel();
		fitthreshpanel.add(fitthreshbutton);
		fitthreshpanel.add(fitthreshold);

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
		floorchooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(floorchooser.getSelectedIndex()==0){
					freezefloor = true;
					System.out.println("Freezing the fit floor at zero.");
				}
				if(floorchooser.getSelectedIndex()==1){
					freezefloor = false;
					System.out.println("Unfreezing the fit floor.");
				}
			}
		});

		calcmodechooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(calcmodechooser.getSelectedIndex()==0){
					gaussfit = true;
					twogaussfit = false;
					supergaussfit = false;
				}
				else if(calcmodechooser.getSelectedIndex()==1){
					twogaussfit = true;
					gaussfit = false;
					supergaussfit = false;
				}
				else if (calcmodechooser.getSelectedIndex()==2 || calcmodechooser.getSelectedIndex()==3) {
					supergaussfit = true;
					gaussfit = false;
					twogaussfit = false;
				}
				else{
					gaussfit = false;
					twogaussfit = false;
					supergaussfit = false;
				}
				plotData();
			}
		});

		removebutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				removePoint();
			}
		});

		fitbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				thresholdexists=false;
				if(gaussfit){
					gaussFit(thresholdexists, 0.0);
				}
				else if(twogaussfit){
					twoGaussFit(thresholdexists, 0.0);
				}
				else if(supergaussfit) {
					superGaussFit(thresholdexists, 0.0);
				}
				else{
					statFit(thresholdexists, 0.0);
				}
			}
		});

		fitallbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {

				thresholdexists=false;
				for(int i=0;i<datatablemodel.getRowCount();i++){

					String filename = (String)datatablemodel.getValueAt(i,0);
					String wire = (String)datatablemodel.getValueAt(i,1);

					resetCurrentData(filename, wire, "H");
					if(gaussfit){
						gaussFit(thresholdexists, 0.0);
					}
					else if(twogaussfit){
						twoGaussFit(thresholdexists, 0.0);
					}
					else if(supergaussfit) {
						superGaussFit(thresholdexists, 0.0);
					}
					else{
						statFit(thresholdexists, 0.0);
					}
					//gaussFit(thresholdexists, 0.0);
					storeResult();

					resetCurrentData(filename, wire, "V");
					if(gaussfit){
						gaussFit(thresholdexists, 0.0);
					}
					else if(twogaussfit){
						twoGaussFit(thresholdexists, 0.0);
					}
					else if (supergaussfit) {
						superGaussFit(thresholdexists, 0.0);
					}
					else{
						statFit(thresholdexists, 0.0);
					}

					storeResult();

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

		gnormbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				normalizeByGaussian();
			}
		});

		anormbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				normalizeByArea();
			}
		});

		vcutbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cutVertical();
			}
		});

		voffsetbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				offsetVertical();
			}
		});
		rangecutbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				cutRange();
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

		fitthreshbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				thresholdexists = true;
				if(gaussfit){
					gaussFit(thresholdexists, fitthreshold.getDoubleValue());
				}
				else if(twogaussfit){
					twoGaussFit(thresholdexists, fitthreshold.getDoubleValue());
				}
				else if(supergaussfit) {
					superGaussFit(thresholdexists, fitthreshold.getDoubleValue());
				}
				else{
					statFit(thresholdexists, fitthreshold.getDoubleValue());
				}
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
				else
				{
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


	public void normalizeByArea(){
		int size = sdata.length;
		double newarea = 0.0;
		double area = 0.0;
		double offset = (data[0] + data[1] + data[2])/3.0;
		for(int i=0; i<size; i++) data[i] -= offset;
		for(int i=1; i<size; i++){
			newarea = Math.abs((sdata[i] - sdata[i-1]))*((data[i] + data[i-1])/2.0);
			area += newarea;
		}
		for(int i=0; i<size; i++) data[i] /= area;

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


    public void normalizeByGaussian(){
		if(dataHasBeenFit){
            double norm = vnorm.getDoubleValue();
			int size = sdata.length;
			double max = 0.0;
			for(int i=0; i<size; i++) if(data[i] > max) max = data[i];
			for(int i=0; i<size; i++){
				sdata[i] -= fitparams[2];
				sdata[i] /= fitparams[0];
				data[i] -= fitparams[3];
				data[i] /= fitparams[1];
			}
			dataHasBeenFit=false;
			plotData();
		}

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
	public void cutRange(){
		double domain1 = rangecut1.getDoubleValue();
		double domain2 = rangecut2.getDoubleValue();
		int gooddatapoints = 0;
		int currentsize = sdata.length;

		for(int i=0; i<currentsize; i++) if(sdata[i] >= domain1 && sdata[i] <= domain2) gooddatapoints++;

		double[] tempsdata = new double[gooddatapoints];
		double[] tempdata = new double[gooddatapoints];

		int j=0;
		for(int i=0; i<currentsize; i++){
			if(sdata[i] >= domain1 && sdata[i] <= domain2){
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



	public void offsetVertical(){
		double offset = voffset.getDoubleValue();
		int size = data.length;
		for(int i=0; i<size; i++) data[i] -= offset;
		dataHasBeenFit=false;
		plotData();
    }

    public void centerOffset(){
		double cent = centerfield.getDoubleValue();
		int size = sdata.length;
		for(int i=0; i<size; i++) sdata[i] -= cent;
		dataHasBeenFit=false;
		plotData();
    }

    public void gaussFit(boolean datathresh, double vthresh){
		System.out.println("Fiting Gaussian");
		double[] snewdata;
		double[] newdata;
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

		Gaussian gs = new Gaussian();

		gs.setData(snewdata, newdata);

		gs.fitParameter(Gaussian.SIGMA, true);
		gs.fitParameter(Gaussian.AMP, true);
		gs.fitParameter(Gaussian.CENTER, true);
		gs.fitParameter(Gaussian.PEDESTAL, true);

		int iterations = 1;
		boolean result = gs.guessAndFit(iterations);

		gs.setParameter(Gaussian.SIGMA, gs.getParameter(Gaussian.SIGMA));
		gs.setParameter(Gaussian.AMP, gs.getParameter(Gaussian.AMP));
		gs.setParameter(Gaussian.CENTER, gs.getParameter(Gaussian.CENTER));
		if(freezefloor){
			gs.setParameter(Gaussian.PEDESTAL, 0.0);
			gs.fitParameter(Gaussian.PEDESTAL, false);
		}
		else{
			gs.setParameter(Gaussian.PEDESTAL, gs.getParameter(Gaussian.PEDESTAL));
			gs.fitParameter(Gaussian.PEDESTAL, true);
		}

		iterations=5;
		result = gs.fit();

		dataHasBeenFit = result;
		if( dataHasBeenFit ){
			fitparams[0] = gs.getParameter(Gaussian.SIGMA);
			fitparams[1] = gs.getParameter(Gaussian.AMP);
			fitparams[2] = gs.getParameter(Gaussian.CENTER);
			fitparams[3] = gs.getParameter(Gaussian.PEDESTAL);
			fitparams_err[0] = gs.getParameterError(Gaussian.SIGMA);
			fitparams_err[1] = gs.getParameterError(Gaussian.AMP);
			fitparams_err[2] = gs.getParameterError(Gaussian.CENTER);
			fitparams_err[3] = gs.getParameterError(Gaussian.PEDESTAL);
			twogaussparams[0] = 0;
			twogaussparams[1] = 0;
			twogaussparams[2] = 0;
			twogaussparams[3] = 0;
			twogaussparams[4] = 0;
			twogaussparams[5] = 0;
			supergaussparams[0] = 0.0;
			supergaussparams[1] = 0.0;
			supergaussparams[2] = 0.0;
			supergaussparams[3] = 0.0;
			supergaussparams[4] = 0.0;
			supergaussparams[5] = 0.0;
			supergaussparams[6] = 0.0;
			supergaussparams[7] = 0.0;
		}

		plotData();
		updateResultsPanel();
    }


    public void twoGaussFit(boolean datathresh, double vthresh){
		System.out.println("Fiting Two Gaussians");
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
			stempdata = tempsdata;
			tempdata = tempdata;
		}
		else{
			stempdata = sdata;
			tempdata = data;
		}

		//Guess at the initial fit parameters
		double xmax=0.0;
		double centguess=0.0;
		double width=0.0;

		int size=tempdata.length;
		int imax = 0;
		for(int i=0; i<size; i++){
			if(tempdata[i] > xmax){
				xmax=tempdata[i];
				imax = i;
			}
		}
		double leftedge=stempdata[0];
		double rightedge=stempdata[size-1];
		for(int i=0; i<size; i++){
			if(tempdata[i] > xmax*0.1){
				leftedge=stempdata[i];
				break;
			}
		}
		for(int i=imax; i<size; i++){
			if(tempdata[i] < xmax*0.1){
				rightedge=stempdata[i];
				break;
			}
		}
		centguess = (rightedge - leftedge)/2.0 + leftedge;
		width = (rightedge - centguess)/2.0;

		amp1 = 2*xmax; amp2 = amp1/2.0;
		sigma1 = width; sigma2 = sigma1/2.0;
		center = centguess;
		offset = 0.0;

		//Do the fit.
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		variables.add(new Variable("amp1",amp1, 0, 50.0));
		variables.add(new Variable("amp2",amp2, 0, 50.0));
		variables.add(new Variable("sigma1",sigma1, 1, 50));
		variables.add(new Variable("sigma2",sigma2, 1, 50));
		variables.add(new Variable("center",center, -200, 200));
		if(!freezefloor){
			variables.add(new Variable("offset",offset, -0.1, 0.1));
		}

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

		fitparams[0] = rms;
		fitparams[1] = 0;
		fitparams[2] = center;
		fitparams[3] = offset;
		twogaussparams[0] = amp1;
		twogaussparams[1] = amp2;
		twogaussparams[2] = sigma1;
		twogaussparams[3] = sigma2;
		twogaussparams[4] = center;
		twogaussparams[5] = 0.0;
		supergaussparams[0] = 0.0;
		supergaussparams[1] = 0.0;
		supergaussparams[2] = 0.0;
		supergaussparams[3] = 0.0;
		supergaussparams[4] = 0.0;
		supergaussparams[5] = 0.0;
		supergaussparams[6] = 0.0;
		supergaussparams[7] = 0.0;

		dataHasBeenFit = true;
		plotTwoGaussData();
		updateResultsPanel();

    }


	public void superGaussFit(boolean datathresh, double vthresh){
		System.out.println("Fiting Super Gaussians");
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
			stempdata = tempsdata;
			tempdata = tempdata;
		}
		else{
			stempdata = sdata;
			tempdata = data;
		}

		//Guess at the initial fit parameters
		double xmax=0.0;
		double centguess=0.0;
		double width=0.0;

		int size=tempdata.length;
		int imax = 0;
		for(int i=0; i<size; i++){
			if(tempdata[i] > xmax){
				xmax=tempdata[i];
				imax = i;
			}
		}
		double leftedge=stempdata[0];
		double rightedge=stempdata[size-1];
		for(int i=0; i<size; i++){
			if(tempdata[i] > xmax*0.1){
				leftedge=stempdata[i];
				break;
			}
		}
		for(int i=imax; i<size; i++){
			if(tempdata[i] < xmax*0.1){
				rightedge=stempdata[i];
				break;
			}
		}
		centguess = (rightedge - leftedge)/2.0 + leftedge;
		width = (rightedge - centguess)/2.0;

		amp1 = 2*xmax; amp2 = amp1/2.0;
		sigma1 = width; sigma2 = sigma1/2.0;
		center = centguess;
		offset = 0.0;
		exp1 = 5; exp2 = 5;

		//Do the fit.
		ArrayList<Variable> variables =  new ArrayList<Variable>();
		variables.add(new Variable("amp1",amp1, 0, 50.0));
		variables.add(new Variable("sigma1",sigma1, 1, 50));
		variables.add(new Variable("N1",exp1, 1, 10));
		variables.add(new Variable("center",center, -200, 200));
		if (calcmodechooser.getSelectedIndex()==2){
			amp2 = 0.0; sigma2 = 0.0; exp2 = 0;
			variables.add(new Variable("amp2", amp2, 0.0, 0.0));
			variables.add(new Variable("sigma2", sigma2, 0.0, 0.0));
			variables.add(new Variable("N2", exp2, 0.0, 0));
		}
		else{
			variables.add(new Variable("amp2", amp2, 0.0, 50.0));
			variables.add(new Variable("sigma2", sigma2, 1.0, 50.0));
			variables.add(new Variable("N2", exp2, 1, 10));
		}

		if(!freezefloor){
			variables.add(new Variable("offset",offset, -0.1, 0.1));
		}

		ArrayList<Objective> objectives = new ArrayList<Objective>();
		objectives.add(new TargetObjective( "diff", 0.0 ) );

		Evaluator1 evaluator = new Evaluator1( objectives, variables );

		Problem problem = new Problem( objectives, variables, evaluator );
		problem.addHint(new InitialDelta( 0.05) );

		double solvetime= 5;
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
            if(name.equalsIgnoreCase("N1")) exp1 = (int)value;
            if(name.equalsIgnoreCase("N2")) exp2 = (int)value;
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
			yfit[i] = amp1*Math.exp(-(Math.pow((x-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow((x-center), exp2))/(2.0*(Math.pow(sigma2, exp2))));
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

		fitparams[0] = rms;
		fitparams[1] = 0;
		fitparams[2] = center;
		fitparams[3] = offset;
		twogaussparams[0] = 0;
		twogaussparams[1] = 0;
		twogaussparams[2] = 0;
		twogaussparams[3] = 0;
		twogaussparams[4] = 0;
		twogaussparams[5] = 0;
		supergaussparams[0] = amp1;
		supergaussparams[1] = amp2;
		supergaussparams[2] = sigma1;
		supergaussparams[3] = sigma2;
		supergaussparams[4] = center;
		supergaussparams[5] = offset;
		supergaussparams[6] = exp1;
		supergaussparams[7] = exp2;
		dataHasBeenFit = true;
		plotSuperGaussData();
		updateResultsPanel();
    }


	public double calcError(ArrayList<Variable> vars, Trial trial){

		double error = 0.0;
		double temp = 0.0;
		int size = stempdata.length;
		double amp1=0.0;
		double amp2=0.0;
		double sigma1=0.0;
		double sigma2=0.0;
		double center=0.0;
		double offset=0.0;
		int exp1=0;
		int exp2=0;
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
			if(name.equalsIgnoreCase("center")) center = value;
			if(name.equalsIgnoreCase("offset")) offset = value;
			if(name.equalsIgnoreCase("N1")) exp1 = (int)value;
			if(name.equalsIgnoreCase("N2")) exp2 = (int)value;
		}

		for(int i=0; i<size; i++){
			x=stempdata[i];
			if (supergaussfit) {
				temp = amp1*Math.exp(-(Math.pow((x-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow((x-center), exp2))/(2.0*(Math.pow(sigma2, exp2)))) + offset;
			}
			else {
				temp = amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2)) + offset;
			}
			error += Math.pow((tempdata[i] - temp), 2.0);
		}
		error = Math.sqrt(error);
		return error;
    }


    public void statFit(boolean datathresh, double vthresh){
		System.out.println("Fiting Stat RMS");
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
		double least = 0.0;
		for(int i=0; i<currentsize; i++){
			mean+=snewdata[i]*newdata[i];
			totals+=newdata[i];
			if(newdata[i]<least) least = newdata[i];
		}
		mean /= totals;

		// Be careful doing this.  Adding a constant to the profile will prevent
		// the NAN computation, but the
		// answer becomes dubious - moment calculations are sensitive to offsets
		// CKA
		if(least != 0.0){
			System.out.println("Offsetting data by the negative offset found:" + least);
			for(int i=0; i<currentsize; i++){
				newdata[i] += Math.abs(least);
			}
		}

		for(int i=0; i<currentsize; i++){
			sqrrms+=Math.pow((snewdata[i]-mean),2)*newdata[i];
		}
		sqrrms /=totals;
		rms=Math.sqrt(sqrrms);

		fitparams[0] = rms;
		fitparams[1] = 0.0;
		fitparams[2] = mean;
		fitparams[3] = 0.0;
		fitparams_err[0] = 0.0;
		fitparams_err[1] = 0.0;
		fitparams_err[2] = 0.0;
		fitparams_err[3] = 0.0;
		twogaussparams[0] = 0.0;
		twogaussparams[1] = 0.0;
		twogaussparams[2] = 0.0;
		twogaussparams[3] = 0.0;
		twogaussparams[4] = 0.0;
		twogaussparams[5] = 0.0;

		//plotData();
		updateResultsPanel();
    }


    public void storeResult(){

		GenericRecord record = new GenericRecord(resultsdatatable);
		ArrayList<double[]> results = new ArrayList<double[]>();
		double[] fit = new double[fitparams.length];
		System.arraycopy(fitparams, 0, fit, 0, fitparams.length);
		double[] errors = new double[fitparams_err.length];
		double[] twogauss = new double[twogaussparams.length];
		double[] supergauss = new double[supergaussparams.length];
		System.arraycopy(fitparams_err, 0, errors, 0, fitparams_err.length);
		System.arraycopy(twogaussparams, 0, twogauss, 0, twogaussparams.length);
		System.arraycopy(supergaussparams, 0, supergauss, 0, supergaussparams.length);
		results.add(fit);
		results.add(errors);
		results.add(sdata);
		results.add(data);
		results.add(twogauss);
		results.add(supergauss);

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
		record.setValueForKey(new ArrayList<double[]>(results), "twogauss");

		resultsdatatable.add(record);
		doc.resultsdatatable = resultsdatatable;
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
			doc.resultsdatatable = resultsdatatable;
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
			double a[] = fitparams;

			double xmin = a[2] - 5*a[0];
			double xmax = a[2] + 5*a[0];
			double points=100.0;
			double inc = (xmax - xmin)/points;
			int npoints = (new Double(points)).intValue();
			double sfit[] = new double[npoints];
			double yfit[] = new double[npoints];

			double x = xmin;
			while(x <= xmax && i<npoints){
				sfit[i]=x;
				yfit[i] = a[3] + a[1]*Math.exp(-(x-a[2])*(x-a[2])/(2.0*a[0]*a[0]));
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


	public void plotTwoGaussData(){

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

			double xmin = center - 5*sigma2;
			double xmax = center + 5*sigma2;
			double points=100.0;
			double inc = (xmax - xmin)/points;
			int npoints = (new Double(points)).intValue();
			double sfit[] = new double[npoints];
			double yfit[] = new double[npoints];
			double x = xmin;

			System.out.println("amp1, amp2 " + amp1 + "  " + amp2);
			System.out.println("sig1, sig2 " + sigma1 + "  " + sigma2);
			System.out.println("center, offset " + center + "  " + offset);
			while(x <= xmax && i<npoints){
				sfit[i]=x;
				yfit[i] = offset + amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2));
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


	public void plotSuperGaussData(){

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

			double xmin = center - 4*sigma1;
			double xmax = center + 4*sigma1;
			double points=100.0;
			double inc = (xmax - xmin)/points;
			int npoints = (new Double(points)).intValue();
			double sfit[] = new double[npoints];
			double yfit[] = new double[npoints];
			double x = xmin;

			System.out.println("amp1, amp2 " + amp1 + "  " + amp2);
			System.out.println("sig1, sig2 " + sigma1 + "  " + sigma2);
			System.out.println("N1, N2" + exp1 + " " + exp2);
			System.out.println("center, offset " + center + "  " + offset);
			while(x <= xmax && i<npoints){
				sfit[i]=x;
				yfit[i] = offset + amp1*Math.exp(-(Math.pow((x-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow((x-center), exp2))/(2.0*(Math.pow(sigma2, exp2))));
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


    public void updateResultsPanel(){
		for(int i=0; i<=3; i++){
			result[i].setValue(fitparams[i]);
			err[i].setValue(fitparams_err[i]);
		}
    }

	@SuppressWarnings ("unchecked") //Had to suppress because valueforkey returns object
    public void resetCurrentData(String file, String wire, String direct){
		filename = file;
		wirename = wire;
		direction = direct;
		label = (new String(filename + ":" + wirename + ":" + direction));

		ArrayList<ArrayList<Double>> currentdat = new ArrayList<ArrayList<Double>>();
		ArrayList<ArrayList<Double>> wiredata = new ArrayList<ArrayList<Double>>();

		DataTable masterdatatable = doc.masterdatatable;

		Map<String, String> bindings = new HashMap<String, String>();
		bindings.put("file", filename);
		bindings.put("wire", wire);
		GenericRecord record =  masterdatatable.record(bindings);
		wiredata=(ArrayList<ArrayList<Double>>)record.valueForKey("data");

		//ArrayList slist = (ArrayList)wiredata.get(0);
		ArrayList<Double> sxlist = wiredata.get(1);
		ArrayList<Double> sylist = wiredata.get(2);
		ArrayList<Double> szlist = wiredata.get(3);
		ArrayList<Double> xlist = wiredata.get(4);
		ArrayList<Double> ylist = wiredata.get(5);
		ArrayList<Double> zlist = wiredata.get(6);

		double[] sdat = new double[sxlist.size()];
		double[] dat = new double[sxlist.size()];

		double xmax=0; double zmax=0;

		for(int i=0; i<sxlist.size(); i++){
			if(Math.abs((xlist.get(i)).doubleValue()) > Math.abs(xmax))
				xmax = (xlist.get(i)).doubleValue();
		}

		if(direction.equals("H")){
			currentdat.add(sxlist);
			currentdat.add(xlist);
			for(int i=0; i<sxlist.size(); i++){
				sdat[i]=(sxlist.get(i)).doubleValue();
				dat[i]=(xlist.get(i)).doubleValue();
			}
		}
		if(direction.equals("V")){
			currentdat.add(sylist);
			currentdat.add(ylist);
			for(int i=0; i<sylist.size(); i++){
				sdat[i]=(sylist.get(i)).doubleValue();
				dat[i]=(ylist.get(i)).doubleValue();
			}
		}
		if(direction.equals("D")){
			currentdat.add(szlist);
			currentdat.add(zlist);
			for(int i=0; i<szlist.size(); i++){
				sdat[i]=((szlist.get(i)).doubleValue());
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
