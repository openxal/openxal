/*
 * DensityFace.java
 *
 */
package xal.app.rtbtwizard;

import xal.tools.swing.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.messaging.*;
import xal.ca.*;
import xal.tools.data.*;
import java.text.NumberFormat;

import java.util.*;
import java.io.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.table.*;

import xal.model.xml.*;
//import xal.tools.optimizer.*;
import xal.tools.plot.*;
import java.text.NumberFormat;
import xal.tools.swing.DecimalField;
import xal.tools.apputils.EdgeLayout;
import xal.tools.beam.*;
import java.text.DecimalFormat;
import xal.tools.solver.*;
//import xal.tools.formula.*;
import xal.tools.solver.hint.*;
import xal.tools.solver.algorithm.*;
import xal.tools.solver.market.*;
import xal.tools.solver.solutionjudge.*;
import xal.service.pvlogger.sim.PVLoggerDataSource;
import xal.tools.apputils.SimpleProbeEditor;

import xal.tools.apputils.files.RecentFileTracker;
/**
 * Performs density analysis
 * @author  cp3
 */

public class DensityFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    
    private AnalysisPanel analysispanel;
    private StoredResultsPanel resultspanel;
    
    EdgeLayout layout = new EdgeLayout();
    
    GenDocument doc;
    
    JLabel bcmlabel = new JLabel("BCM Charge: ");
    JLabel avglabel= new JLabel("Average Target Peak Density (N/mm^2): ");
    JPanel mainPanel;
    JButton loadbutton;
    JButton solvebutton;
    JButton bcmbutton;
    
    DataTable resultsdatabase;
    HashMap<String, Integer> pvloggermap;
    //Object[][] tabledata = new Object[8][4];
    ArrayList<String> wirenames = new ArrayList<String>();
    
    JScrollPane datascrollpane;
    DataTableModel datatablemodel;
    
    JTable datatable;
    JTextField Np;
    JTextField avgdensity;
    NumberFormat numFor = NumberFormat.getNumberInstance();
    
    
    double ppp = 0.0;
    
    public DensityFace(GenDocument aDocument, JPanel mainpanel) {
        doc=aDocument;
        setPreferredSize(new Dimension(960,800));
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
        GridLayout initgrid = new GridLayout(6, 4);
        
        newlayout.setConstraints(loadbutton, 5, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(loadbutton);
        newlayout.setConstraints(bcmbutton, 50, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(bcmbutton);
        newlayout.setConstraints(bcmlabel, 90, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(bcmlabel);
        newlayout.setConstraints(Np, 90, 110, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(Np);
        newlayout.setConstraints(solvebutton, 120, 10, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(solvebutton);
        newlayout.setConstraints(datascrollpane, 5, 210, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(datascrollpane);
        newlayout.setConstraints(avglabel, 160, 200, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(avglabel);
        newlayout.setConstraints(avgdensity, 160, 460, 100, 10, EdgeLayout.LEFT, EdgeLayout.NO_GROWTH);
        mainPanel.add(avgdensity);
    }
    
    public void init(){
        
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(950,600));
        loadbutton = new JButton("Load Fitted Profiles");
        solvebutton = new JButton("Find Peak Density");
        bcmbutton = new JButton("Get BCM Charge (ppp)");
        
        makeDataTable();
        
        //numFor.setMinimumFractionDigits(3);
        //Np = new DecimalField(1.0,4,numFor);
        Np = new JTextField("0.0");
        Np.setPreferredSize(new Dimension(80,20));
        avgdensity = new JTextField("");
        avgdensity.setPreferredSize(new Dimension(80,20));
        
        wirenames.add(new String("RTBT_Diag:WS20"));
        wirenames.add(new String("RTBT_Diag:WS21"));
        wirenames.add(new String("RTBT_Diag:WS23"));
        wirenames.add(new String("RTBT_Diag:WS24"));
        wirenames.add(new String("RTBT_Diag:Harp"));
        
    }
    
    
    
    public void setAction(){
        
        loadbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                resultsdatabase = doc.wireresultsdatabase;
                refreshTable();
            }
        });
        bcmbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                String bcmchoice = new String("RTBT_Diag:BCM25");
                DecimalFormat decfor =  new DecimalFormat("0.###E0");
                double charge = 0.0;
                Channel bcmChargeCh = ChannelFactory.defaultFactory().getChannel(bcmchoice+":Q");
                try {
                    charge = Math.abs(bcmChargeCh.getValDbl());
                } catch (ConnectionException ce) {
                    System.out.println("Cannot connect to " + bcmChargeCh.getId());
                } catch (GetException ce) {
                    System.out.println("Cannot get value from " + bcmChargeCh.getId());
                }
                Channel.flushIO();
                //doc.initBCM(bcmchoice);
                //doc.startBCMMonitor();
                ppp = charge/1.602e-19;
                Np.setText(decfor.format(ppp));
                System.out.println("Charge is " + charge + " " + ppp);
                doc.charge=ppp;
                //System.out.println("doc charge is " + doc.charge);
                //doc.stopBCMMonitor();
            }
        });
        
        solvebutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                HashMap beamarearatios = doc.beamarearatios;
                HashMap windowarearatios = doc.windowarearatios;
                DecimalFormat decfor =  new DecimalFormat("0.###E0");
                int nrows = datatablemodel.getRowCount();
                double xroot = 0.0;
                double Gx = 0.0;
                double yroot = 0.0;
                double Gy = 0.0;
                double rho_wire = 0.0;
                double areafac = 1.0;
                double wareafac = 1.0;
                double rho_target = 0.0;
                double rho_window = 0.0;
                double np = 0.0;
                double avg_rho_target=0.0;
                double avg_rho_window=0.0;
                double count=0.0;
                double peakfac = 0.0;
                double sumdensity = 0.0;
                
                Double N = new Double(0.0);
                np=Double.parseDouble(Np.getText());
                System.out.println("Number of protons is " + np);
                doc.charge=np;
                avg_rho_target = 0.0;
                avg_rho_window = 0.0;
                count = 0.0;
                for(int i=0; i<nrows; i++){
                    if((Boolean)datatable.getValueAt(i,1) == true){
                        String label = (String)datatable.getValueAt(i,0);
                        xroot = getMaxima(label, new String("H"));
                        Gx = getDensity(xroot,label, new String("H"));
                        yroot = getMaxima(label, new String("V"));
                        Gy = getDensity(yroot,label, new String("V"));
                        rho_wire=np*Gy*Gx;
                        System.out.println("xroot is " + xroot + "; yroot is " + yroot);
                        System.out.println("Gx = " + Gx + " Gy = " + Gy + " Gx*Gy*np " + rho_wire);
                        areafac = ((Double)beamarearatios.get(label)).doubleValue();
                        wareafac = ((Double)windowarearatios.get(label)).doubleValue();
                        rho_target=rho_wire*areafac * 0.96;
                        rho_window=rho_wire*wareafac;
                        peakfac = rho_target/(1.25e-4)/np;
                        sumdensity += rho_target;
                        System.out.println("For " + label + " tareafac is  " + areafac);
                        System.out.println("For " + label + " wareafac is  " + wareafac);
                        System.out.println("x, Gx, y, Gy are = " + xroot + " " + Gx + " " + yroot + " " + Gy);
                        datatablemodel.setValueAt(new String(decfor.format(rho_wire)), i, 2);
                        datatablemodel.setValueAt(new String(decfor.format(rho_window)), i, 3);
                        datatablemodel.setValueAt(new String(decfor.format(rho_target)), i, 4);
                        datatablemodel.setValueAt(new String(decfor.format(peakfac)), i, 5);
                        avg_rho_target += rho_target;
                        avg_rho_window += rho_window;
                        count += 1.0;
                    }
                }
                avg_rho_target /= count;
                avg_rho_window /= count;
                double avedensity = sumdensity /= count;
                avgdensity.setText(decfor.format(avedensity));
                System.out.println("target ave is " + avg_rho_target);
                doc.tdensity = avg_rho_target;
                doc.wdensity = avg_rho_window;
                
                datatablemodel.fireTableDataChanged();
            }
        });
        
    }
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    private double getDensity(double x, String wirename, String direct){
		
		double G=0;
		double amp1=0;
		double amp2=0;
		double sigma1=0;
		double sigma2=0;
		double rms=0;
		double center=0;
		double exp1 = 0;
		double exp2 = 0;
		ArrayList<Object> tabledata = new ArrayList<Object>();
		if(resultsdatabase.records().size() == 0){
			System.out.println("No data available to load!");
		}
		else{
			Collection<GenericRecord> records = resultsdatabase.records();
			Iterator<GenericRecord> itr = records.iterator();
			while(itr.hasNext()){
				tabledata.clear();
				GenericRecord record = itr.next();
				String wire = (String)record.valueForKey("wire");
				String direction = (String)record.valueForKey("direction");
				if(wire.equals(wirename) && direction.equals(direct)){
					ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
					double[] fitparams = results.get(0);
					amp1=fitparams[0];
					amp2=fitparams[1];
					sigma1=fitparams[2];
					sigma2=fitparams[3];
					rms=fitparams[4];
					center=fitparams[5];
					exp1 = fitparams[7];
					exp2 = fitparams[8];
				}
			}
			
            double xleft = center - 4*rms;
            double xright = center + 4*rms;
            double step = Math.abs((xright - xleft)/1000.0);
            double xiter = xleft;
            double area = 0.0;
            double ymax = 0.0;
            double xmax = 0.0;
            
            if (exp1 != 0 || exp2 != 0){
                System.out.println("Supergauss density \n");
                while(xiter < xright){
                    double g = amp1*Math.exp(-(Math.pow(Math.abs(xiter-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs(xiter-center), exp2))/(2.0*(Math.pow(sigma2, exp2))));
                    if(g > ymax){
                        ymax = g;
                        xmax = xiter;
                    }
                    area += g*step;
                    xiter += step;
                }
                
                G = 1/area * (amp1*Math.exp(-(Math.pow(Math.abs(xmax-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs(xmax-center), exp2))/(2.0*(Math.pow(sigma2, exp2)))));
            }
            else {
                System.out.println("Two gauss density \n");
                G=1/Math.sqrt(2*Math.PI) * 1/(amp1*sigma1 - amp2*sigma2) *
                (amp1*Math.exp(-Math.pow((x-center),2)/(2*sigma1*sigma1)) - amp2*Math.exp(-Math.pow((x-center),2)/(2*sigma2*sigma2)));
			}
            
		}
        return G;
    }
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
	private double getMaxima(String wirename, String direct){
        
		double root1=0;
		double root2amp=0;
		double root2ln=0;
		double root2arg=0;
		double root2=0;
		double root = 0;
		double amp1=0;
		double amp2=0;
		double sigma1=0;
		double sigma2=0;
		double center=0;
		ArrayList<Object> tabledata = new ArrayList<Object>();
		if(resultsdatabase.records().size() == 0){
			System.out.println("No data available to load!");
		}
		else{
			Collection<GenericRecord> records = resultsdatabase.records();
			Iterator<GenericRecord> itr = records.iterator();
			while(itr.hasNext()){
				tabledata.clear();
				GenericRecord record = itr.next();
                
				String wire = (String)record.valueForKey("wire");
				String direction = (String)record.valueForKey("direction");
				if(wire.equals(wirename) && direction.equals(direct)){
					ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
					double[] fitparams = results.get(0);
					amp1=fitparams[0];
					amp2=fitparams[1];
					sigma1=fitparams[2];
					sigma2=fitparams[3];
					center=fitparams[5];
				}
			}
			root1 = center;
			root2arg = 2*Math.pow(sigma1*sigma2,2)/(sigma2*sigma2 - sigma1*sigma1);
			root2ln = Math.log(amp1/amp2 * Math.pow(sigma2/sigma1,2));
			root2 = root2arg * root2ln;
			if(root2 <= 0){
				root = root1;
			}
			else{
				root = Math.sqrt(root2) + center;
			}
		}
		return root;
    }
    
    
    private void refreshTable(){
        
        datatablemodel.clearAllData();
        
        ArrayList<Object> tabledata = new ArrayList<Object>();
        ArrayList<String> wiresadded = new ArrayList<String>();
        
        Collection<GenericRecord> records = resultsdatabase.records();
	    Iterator<GenericRecord> itr = records.iterator();
	    while(itr.hasNext()){
            tabledata.clear();
            GenericRecord record = itr.next();
            String wire = (String)record.valueForKey("wire");
            System.out.println("wire is " + wire);
            if(wiresadded.contains(wire)){
            }
            else{
                wiresadded.add(wire);
                tabledata.add(new String(wire));
                tabledata.add(new Boolean(true));
                tabledata.add(new Double(0.0));
                tabledata.add(new Double(0.0));
                tabledata.add(new Double(0.0));
                tabledata.add(new Double(0.0));
                datatablemodel.addTableData(new ArrayList<Object>(tabledata));
            }
            datatablemodel.fireTableDataChanged();  
	    }
    }
    
    /*
     public void clearTable(){
     if(datatable.records().size() == 0){
     System.out.println("No files to remove!");
     }
     else{
     Collection records = datatable.records();
     Iterator itr = records.iterator();	
     while(itr.hasNext()){
     GenericRecord record = (GenericRecord)itr.next();
     datatable.remove(record);
     }
     //doc.datatable = datatable;
     }
     //datatablemodel.clearAllData();
     }*/
    
    public void makeDataTable(){
        
        String[] colnames = {"Wire", "Use", "Wire Density (N/mm^2)", "Window Density (N/mm^2)", "Target Density (N/mm^2)", "Peaking Fac."};
        
        datatablemodel = new DataTableModel(colnames, 0);
        
        datatable = new JTable(datatablemodel);
        datatable.getColumnModel().getColumn(0).setMinWidth(137);
        datatable.getColumnModel().getColumn(1).setMaxWidth(35);
        datatable.getColumnModel().getColumn(2).setMinWidth(150);
        datatable.getColumnModel().getColumn(3).setMinWidth(160);
        datatable.getColumnModel().getColumn(4).setMinWidth(165);
        datatable.getColumnModel().getColumn(5).setMinWidth(90);
        
        datatable.setRowSelectionAllowed(false);
        datatable.setColumnSelectionAllowed(false);
        datatable.setCellSelectionEnabled(false);
        
        datatable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        datascrollpane = new JScrollPane(datatable);
        datascrollpane.setColumnHeaderView(datatable.getTableHeader());
        
        datascrollpane.setPreferredSize(new Dimension(740, 125));
        
        
    }
    
    
    
	
    
	
    
    
    
    
}

