/*************************************************************
 //
 // class StoredResults:
 // This class is responsible for the Graphic User Interface
 // components and action listeners for stored results.
 //
 /*************************************************************/

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
import java.io.File.*;
import javax.swing.text.PlainDocument;
import java.lang.*;
import xal.extension.widgets.swing.*;
import xal.tools.statistics.*;
import xal.tools.apputils.EdgeLayout;
import xal.tools.apputils.files.RecentFileTracker;
import xal.extension.widgets.plot.*;
import xal.tools.data.*;
import xal.extension.fit.lsm.*;
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;


public class StoredResultsPanel extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    public DataTableModel datatablemodel;
	
    private DataTable resultsdatatable;
    private boolean linearplot = true;
    private boolean plotrawdata = true;
    private boolean plotfitdata = true;
    private boolean fixlinscale = false;
    private boolean fixlogscale = false;
    
    private JButton refreshbutton;
    private JButton plotbutton;
    private JButton exportbutton;
    
    private ArrayList<DataAttribute> attributes;
    
    private String[] plottypes = {"Plot Linear Values", "Plot Linear, Fixed Scale", "Plot Log Values",  "Plot Log, Fixed Scale"};
    private String[] plotdatatypes = {"Plot Raw and Fit Data", "Plot Raw Data Only", "Plot Fit Data Only"};
    private JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
    private JComboBox<String> plotchooser = new JComboBox<String>(plotdatatypes);
    
    private JFileChooser fc;
    
    GenDocument doc;
    EdgeLayout layout = new EdgeLayout();
    FunctionGraphsJPanel datapanel;
    JScrollPane datascrollpane;
    
    
    public StoredResultsPanel(){}
	
	//Member function Constructor
    public StoredResultsPanel(GenDocument aDocument){
		
		doc=aDocument;
        
		makeComponents(); //Creation of all GUI components
		setStyling();     //Set the styling of components
		addComponents();  //Add all components to the layout and panels
        
		setAction();      //Set the action listeners
        
    }
    
    public void addComponents(){
		EdgeLayout layout = new EdgeLayout();
		mainPanel.setLayout(layout);
		layout.add(datapanel, mainPanel, 20, 15, EdgeLayout.LEFT);
		layout.add(scalechooser, mainPanel, 10, 235, EdgeLayout.LEFT);
		layout.add(plotchooser, mainPanel, 200, 235, EdgeLayout.LEFT);
		layout.add(refreshbutton, mainPanel, 10, 275, EdgeLayout.LEFT);
		layout.add(plotbutton, mainPanel, 200, 275, EdgeLayout.LEFT);
		layout.add(datascrollpane, mainPanel, 8, 330, EdgeLayout.LEFT);
		layout.add(exportbutton, mainPanel, 10, 460, EdgeLayout.LEFT);
		this.add(mainPanel);
	}
    
    public void makeComponents(){
		mainPanel = new JPanel();
		mainPanel.setPreferredSize(new Dimension(450, 530));
		mainPanel.setBorder(BorderFactory.createTitledBorder("View Stored Results"));
		//mainPanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
		datapanel = new FunctionGraphsJPanel();
		datapanel.setPreferredSize(new Dimension(395, 210));
		datapanel.setGraphBackGroundColor(Color.WHITE);
        
		attributes = new ArrayList<DataAttribute>();
		attributes.add(new DataAttribute("file", new String("").getClass(), true) );
		attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
		attributes.add(new DataAttribute("direction", new String("").getClass(), true) );
		attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
		resultsdatatable = new DataTable("DataTable", attributes);
        
		refreshbutton = new JButton("Refresh Table");
		plotbutton = new JButton("Plot Selected Profiles");
		exportbutton = new JButton("Export Results");
        
		fc = new JFileChooser();
        
		makeDataTable();
        
    }
    
    
    public void setAction(){
		scalechooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(scalechooser.getSelectedIndex() == 0){
					linearplot = true;
					fixlinscale = false;
					fixlogscale = false;
				}
				if(scalechooser.getSelectedIndex() == 1){
					linearplot = true;
					fixlinscale = true;
					fixlogscale = false;
				}
				if(scalechooser.getSelectedIndex() == 2){
					linearplot = false;
					fixlinscale = false;
					fixlogscale = false;
				}
				if(scalechooser.getSelectedIndex() == 3){
					linearplot = false;
					fixlinscale = false;
					fixlogscale = true;
				}
			}
		});
		plotchooser.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(plotchooser.getSelectedIndex()==0){
					plotrawdata = true;
					plotfitdata = true;
				}
				if(plotchooser.getSelectedIndex()==1){
					plotrawdata = true;
					plotfitdata = false;
				}
				if(plotchooser.getSelectedIndex()==2){
					plotrawdata = false;
					plotfitdata = true;
				}
			}
		});
		refreshbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				resultsdatatable = doc.wireresultsdatabase;
				refreshTable();
			}
		});
		plotbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int nrows = datatablemodel.getRowCount();
				ArrayList<Integer> wires = new ArrayList<Integer>();
				for(int i=0; i<nrows; i++){
					if(((Boolean)datatable.getValueAt(i, 4)).booleanValue() == true){
						wires.add(new Integer(i));
					}
				}
				plotData(wires);
			}
		});
		exportbutton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int returnValue = fc.showSaveDialog(StoredResultsPanel.this);
				if(returnValue == JFileChooser.APPROVE_OPTION){
					File file = fc.getSelectedFile();
					try{
						writeMatLabFile(file);
					}
					catch(IOException ioe){
					}
				}
				else{
					System.out.println("Save command canceled by user.");
				}
			}
		});
	}
    
    
   @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    
    public void plotData(ArrayList<Integer> wires){
        
		int colorindex = 1;
        
		datapanel.removeAllGraphData();
		boolean plotdata = false;
		
		ArrayList<BasicGraphData> grapharray = new ArrayList<BasicGraphData>();
        
		Iterator<Integer> itr = wires.iterator();
		while(itr.hasNext()){
            
			ArrayList<double[]> wiredata = new ArrayList<double[]>();
            
			int rownumber = (itr.next()).intValue();
			String filename = (String)datatable.getValueAt(rownumber, 0);
			String name = (String)datatable.getValueAt(rownumber, 1);
			String direction = (String)datatable.getValueAt(rownumber, 2);
            
			Map<String, String> bindings = new HashMap<String, String>();
			bindings.put("file", filename);
			bindings.put("wire", name);
			bindings.put("direction", direction);
			GenericRecord record =  resultsdatatable.record(bindings);
			wiredata=(ArrayList<double[]>)record.valueForKey("data");
            
			if(plotrawdata){
                
				BasicGraphData graphdata = new BasicGraphData();
                
				double[] sdata = wiredata.get(2);
				double[] data = wiredata.get(3);
				if(!linearplot){
					double temp;
					double[] logdata = new double[data.length];
					for(int j=0; j<logdata.length; j++){
						temp=data[j];
						if(temp<=0.0) temp=0.00001;
						logdata[j] = Math.log(temp)/Math.log(10);
					}
					graphdata.addPoint(sdata, logdata);
				}
				else{
					graphdata.addPoint(sdata, data);
				}
				graphdata.setDrawPointsOn(true);
				graphdata.setDrawLinesOn(false);
				graphdata.setGraphProperty("Legend", new String(filename+":"+name+":"+direction+":raw"));
				graphdata.setGraphColor(IncrementalColors.getColor(colorindex));
				grapharray.add(graphdata);
			}
            
			if(plotfitdata){
				BasicGraphData fitgraphdata = new BasicGraphData();
				fitgraphdata.removeAllPoints();
				double[] fitparams = wiredata.get(0);
				int i = 0;
                
				double xmin = fitparams[5] - 5*fitparams[4];
				double xmax = fitparams[5] + 5*fitparams[4];
				double points=100.0;
				double inc = (xmax - xmin)/points;
				int npoints = (new Double(points)).intValue();
				double sfit[] = new double[npoints];
				double yfit[] = new double[npoints];
                
				double amp1=fitparams[0]; double amp2=fitparams[1];
				double sigma1=fitparams[2]; double sigma2=fitparams[3];
				double center=fitparams[5]; double offset=fitparams[6];
				double exp1 = fitparams[7]; double exp2 = fitparams[8];
                
				double x = xmin;
				while(x <= xmax && i<npoints){
					sfit[i]=x;
					if(doc.lastfitSuperGauss) {
						yfit[i] = amp1*Math.exp(-(Math.pow(Math.abs(x-center), exp1))/(2.0*(Math.pow(sigma1, exp1)))) - amp2*Math.exp(-(Math.pow(Math.abs(x-center), exp2))/(2.0*(Math.pow(sigma2, exp2))));
					}
					else {
						yfit[i] = offset + amp1*Math.exp(-(x-center)*(x-center)/(2.0*sigma1*sigma1)) - amp2*Math.exp(-(x-center)*(x-center)/(2.0*sigma2*sigma2));
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
				fitgraphdata.setGraphProperty("Legend", new String(filename+":"+name+":"+direction+":fit"));
				fitgraphdata.setGraphColor(IncrementalColors.getColor(colorindex));
				grapharray.add(fitgraphdata);
			}
			colorindex++;
		}
        
		Iterator<BasicGraphData> yitr = grapharray.iterator();
        
		while(yitr.hasNext()){
			datapanel.addGraphData(yitr.next());
		}
        
		datapanel.setLegendButtonVisible(true);
		datapanel.setName("    Selected Profiles");
		datapanel.setLegendButtonVisible(true);
		if(linearplot){
			if(fixlinscale){
				datapanel.setLimitsAndTicksY(0.001, 1.1, .2);
			}
			else{
				datapanel.setExternalGL(null);
			}
		}
		if(!linearplot){
			if(fixlogscale){
				datapanel.setLimitsAndTicksY(-3.0, 0.3, .6);
			}
			else{
				datapanel.setExternalGL(null);
			}
		}
    }
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    private void refreshTable(){
        
		datatablemodel.clearAllData();
        
		ArrayList<Object> tabledata = new ArrayList<Object>();
        
		if(resultsdatatable.records().size() == 0){
			System.out.println("No data available to load!");
		}
		else{
			Collection<GenericRecord> records = resultsdatatable.records();
			Iterator<GenericRecord> itr = records.iterator();
			while(itr.hasNext()){
				tabledata.clear();
				GenericRecord record = itr.next();
				String filename = (String)record.valueForKey("file");
				String wire = (String)record.valueForKey("wire");
				String direction = (String)record.valueForKey("direction");
				ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
				double[] fitparams = results.get(0);
				double[] fitparams_err = results.get(1);
                
				tabledata.add(new String(filename));
				tabledata.add(new String(wire));
				tabledata.add(new String(direction));
				tabledata.add(new Double(fitparams[4]));
				tabledata.add(new Boolean(false));
				datatablemodel.addTableData(new ArrayList<Object>(tabledata));
			}
			datatablemodel.fireTableDataChanged();
		}
	}
    
    
    public void setStyling(){
		
    }
    
    
    public void makeDataTable(){
		String[] colnames = {"File", "Wire", "H/V","RMS", "Plot"};
        
		datatablemodel = new DataTableModel(colnames, 0);
        
		datatable = new JTable(datatablemodel);
		datatable.getColumnModel().getColumn(0).setMinWidth(165);
		datatable.getColumnModel().getColumn(1).setMinWidth(110);
		datatable.getColumnModel().getColumn(2).setMaxWidth(35);
		datatable.getColumnModel().getColumn(3).setMaxWidth(45);
		datatable.getColumnModel().getColumn(4).setMaxWidth(32);
        
		datatable.setRowSelectionAllowed(false);
		datatable.setColumnSelectionAllowed(false);
		datatable.setCellSelectionEnabled(false);
        
		datatable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		datascrollpane = new JScrollPane(datatable);
		datascrollpane.setColumnHeaderView(datatable.getTableHeader());
        
		datascrollpane.setPreferredSize(new Dimension(405, 100));
        
    }
    
    @SuppressWarnings ("unchecked") //had to suppress becayse valueForKey returns object and does not allow for specific casting
    public void writeMatLabFile(File file) throws IOException{
        if(resultsdatatable.records().size() == 0){
            System.out.println("No data available to write!");
        }
        else{
            OutputStream fout = new FileOutputStream(file);
            Collection<GenericRecord> records = resultsdatatable.records();
            Iterator<GenericRecord> itr = records.iterator();
            while(itr.hasNext()){
                GenericRecord record = itr.next();
                String filename = (String)record.valueForKey("file");
                String wire = (String)record.valueForKey("wire");
                String direction = (String)record.valueForKey("direction");
                ArrayList<double[]> results = (ArrayList<double[]>)record.valueForKey("data");
                double[] fitparams = results.get(0);
                double[] fitparams_err = results.get(1);
                String line = "%  " + filename + ":" + wire + ":" + direction + "\n";
                double[] sdata = results.get(2);
                double[] data = results.get(3);
                
                line = line + sdata.length + "\t" + data.length + "\n";
                for(int i=0; i<sdata.length; i++){
                    line = line + sdata[i] + "\t" + data[i] + "\n";
                }
                
                double[] a = fitparams;
                line = line + "%%% ";
				
                byte buf[] = line.getBytes();
                fout.write(buf);
            }
            fout.close();
        }
		
    }
    
}








