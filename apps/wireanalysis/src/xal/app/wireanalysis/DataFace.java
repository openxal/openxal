/*************************************************************
 //
 // class DataFace:
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
import java.text.NumberFormat;
import xal.tools.messaging.*;
import xal.ca.*;

public class DataFace extends JPanel{
    
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
    
    public JPanel mainPanel;
    public JTable datatable;
    
    private JFileChooser fc;
    private DataTable masterdatatable;
    private ArrayList<DataAttribute> attributes;
    private ArrayList<String> filesopened;
    private HashMap<String, Integer> pvloggermap;
    
    GenDocument doc;
    private boolean linearplot = true;
    private String[] plottypes = {"Plot Linear Values", "Plot Log Values"};
    private JComboBox<String> scalechooser = new JComboBox<String>(plottypes);
    private JButton openbutton;
    private JButton clearbutton;
    private JButton plotbutton;
    
    RecentFileTracker ft;
    JScrollPane datascrollpane;
    DataTableModel datatablemodel;
    JPanel rawdatapanel;
    FunctionGraphsJPanel xrawdatapanel;
    FunctionGraphsJPanel yrawdatapanel;
    FunctionGraphsJPanel zrawdatapanel;
    
    public DataFace(){}
    //Member function Constructor
    public DataFace(GenDocument aDocument){
		
        doc=aDocument;
        
        makeComponents(); //Creation of all GUI components
        setStyling();     //Set the color for the buttons
        addComponents();  //Add all components to the layout and panels
        
        setAction();      //Set the action listeners
    }
    
    public void addComponents(){
        EdgeLayout layout = new EdgeLayout();
        mainPanel.setLayout(layout);
        
        layout.add(openbutton, mainPanel, 40, 10, EdgeLayout.LEFT);
        layout.add(clearbutton, mainPanel, 40, 40, EdgeLayout.LEFT);
        
        layout.add(datascrollpane,mainPanel, 250, 10, EdgeLayout.LEFT);
        rawdatapanel.add(xrawdatapanel);
        rawdatapanel.add(yrawdatapanel);
        rawdatapanel.add(zrawdatapanel);
        layout.add(scalechooser,mainPanel, 40, 130, EdgeLayout.LEFT);
        layout.add(plotbutton, mainPanel, 220, 130, EdgeLayout.LEFT);
        
        layout.add(rawdatapanel, mainPanel, 5, 190, EdgeLayout.LEFT);
        
        this.add(mainPanel);
    }
    
    public void makeComponents(){
        mainPanel = new JPanel();
        mainPanel.setPreferredSize(new Dimension(950, 650));
        
        fc = new JFileChooser();
        ft = new RecentFileTracker(1, this.getClass(), "wsfile");
        ft.applyRecentFolder(fc);
        
        attributes = new ArrayList<DataAttribute>();
        attributes.add(new DataAttribute("file", new String("").getClass(), true) );
        attributes.add(new DataAttribute("wire", new String("").getClass(), true) );
        attributes.add(new DataAttribute("data", new ArrayList<DataAttribute>().getClass(), false) );
        masterdatatable = new DataTable("DataTable", attributes);
        pvloggermap = new HashMap<String, Integer>();
        
        filesopened = new ArrayList<String>();
        
        xrawdatapanel = new FunctionGraphsJPanel();
        xrawdatapanel.setPreferredSize(new Dimension(300, 270));
        xrawdatapanel.setGraphBackGroundColor(Color.WHITE);
        yrawdatapanel = new FunctionGraphsJPanel();
        yrawdatapanel.setPreferredSize(new Dimension(300, 270));
        yrawdatapanel.setGraphBackGroundColor(Color.WHITE);
        zrawdatapanel = new FunctionGraphsJPanel();
        zrawdatapanel.setPreferredSize(new Dimension(300, 270));
        zrawdatapanel.setGraphBackGroundColor(Color.WHITE);
        rawdatapanel = new JPanel();
        rawdatapanel.setPreferredSize(new Dimension(950, 350));
        rawdatapanel.setBorder(BorderFactory.createTitledBorder("Raw Data Display"));
        rawdatapanel.setBorder(BorderFactory.createRaisedBevelBorder());
        
        makeDataTable();
        
        plotbutton = new JButton("Plot Selected Profiles");
        openbutton = new JButton("Add New Wirescan File");
        clearbutton = new JButton("Clear Data Table");
    }
    
    
    public void setAction(){
        openbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int returnValue = fc.showOpenDialog(DataFace.this);
                if(returnValue == JFileChooser.APPROVE_OPTION){
                    File file = fc.getSelectedFile();
                    ft.cacheURL(file);
                    
                    ArrayList<Object> newdata = new ArrayList<Object>(parseFile(file));
                    
                    String name = new String(file.toString());
                    String[] tokens;
                    tokens=name.split("/");
                    String filename = new String(tokens[tokens.length-1]);
                    Integer pvloggerid = (Integer)newdata.get(newdata.size()-1);
                    pvloggermap.put(filename, pvloggerid);
                    doc.masterpvloggermap = pvloggermap;
                    
                    newdata.remove(newdata.size()-1);
                    
                    if(!filesopened.contains(filename)){
                        System.out.println("Opening file: " + filename);
                        filesopened.add(filename);
                        toTable(filename, newdata);
                        toMasterDataTable(filename, newdata);
                    }
                    else{
                        System.out.println("File " + filename + " has already been opened.");
                    }
                }
                else{
                    System.out.println("Open command cancelled by user.");
                }
            }
        });
        
        plotbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                int nrows = datatablemodel.getRowCount();
                ArrayList<Integer> wires = new ArrayList<Integer>();
                for(int i=0; i<nrows; i++){
                    if(((Boolean)datatable.getValueAt(i, 5)).booleanValue() == true){
                        wires.add(new Integer(i));
                    }
                }
                plotData(wires);
            }
        });
        
        clearbutton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                clearTable();
                filesopened.clear();
            }
        });
        scalechooser.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e) {
                if(scalechooser.getSelectedIndex()==0){
                    linearplot = true;
                }
                else{
                    linearplot = false;
                }
            }
        });
    }
    
	
    public void setStyling(){
		
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
    
    @SuppressWarnings ("unchecked") //had to suppress warnings because data held multiple types.
    public void toTable(String filename, ArrayList<Object> data){
        
        Iterator<Object> itr = data.iterator();
        
        while(itr.hasNext()){
            HashMap<String, Object> map = new HashMap<String, Object>();
            ArrayList<Object> tabledata = new ArrayList<Object>();
            map = (HashMap<String, Object>)itr.next();
            tabledata.add(filename);
            tabledata.add((String)map.get("name"));
            tabledata.add(((ArrayList)map.get("fitparams")).get(0));
            tabledata.add(((ArrayList)map.get("fitparams")).get(1));
            tabledata.add(((ArrayList)map.get("fitparams")).get(2));
            tabledata.add(new Boolean(false));
            
            datatablemodel.addTableData(new ArrayList<Object>(tabledata));
        }
        datatablemodel.fireTableDataChanged();
    }
    
    
    
    @SuppressWarnings ("unchecked") //had to suppress warnings because HashMap contains multiple types and would not allow for specific casting.
    public void toMasterDataTable(String filename, ArrayList<Object> data){
        Iterator<Object> itr = data.iterator();
        while(itr.hasNext()){
            HashMap<String, Object> map = new HashMap<String, Object>();
            //HashMap datamap = new HashMap();
            ArrayList<ArrayList<Double>> rawdata = new ArrayList<ArrayList<Double>>();
            ArrayList<ArrayList<Double>> paramdata = new ArrayList<ArrayList<Double>>();
            
            map = (HashMap<String, Object>)itr.next();
            String name = new String((String)map.get("name"));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sxdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("sydata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("szdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("xdata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("ydata")));
            rawdata.add(new ArrayList<Double>((ArrayList<Double>)map.get("zdata")));
            paramdata.addAll(new ArrayList<ArrayList<Double>>((ArrayList<ArrayList<Double>>)map.get("fitparams")));
            
            //datamap.put(name, rawdata);
            //datamap.put("rms", paramdata);
            
            GenericRecord record = new GenericRecord(masterdatatable);
            record.setValueForKey(filename, "file");
            record.setValueForKey(name, "wire");
            record.setValueForKey(rawdata, "data");
            record.setValueForKey(paramdata, "rms");
            
            masterdatatable.add(record);
        }
        //Update in the GenDocument file
        doc.masterdatatable = masterdatatable;
    }
    
    public void clearTable(){
        if(masterdatatable.records().size() == 0){
            System.out.println("No files to remove!");
        }
        else{
            Collection<GenericRecord> records = masterdatatable.records();
            Iterator<GenericRecord> itr = records.iterator();
            while(itr.hasNext()){
                GenericRecord record = itr.next();
                masterdatatable.remove(record);
            }
            doc.masterdatatable = masterdatatable;
        }
        datatablemodel.clearAllData();
        
    }
	
    
    @SuppressWarnings ("unchecked") //Had to suppress warning because valueforkey returns object and would not allow for specific casting
    public void plotData(ArrayList<Integer> wires){
        int xcolorindex = 1;
        int ycolorindex = 1;
        int zcolorindex = 1;
        
        xrawdatapanel.removeAllGraphData();
        yrawdatapanel.removeAllGraphData();
        zrawdatapanel.removeAllGraphData();
        
        ArrayList<BasicGraphData> xgrapharray = new ArrayList<BasicGraphData>();
        ArrayList<BasicGraphData> ygrapharray = new ArrayList<BasicGraphData>();
        ArrayList<BasicGraphData> zgrapharray = new ArrayList<BasicGraphData>();
        
        Iterator<Integer> itr = wires.iterator();
        while(itr.hasNext()){
            
            ArrayList<ArrayList<Double>> wiredata = new ArrayList<ArrayList<Double>>();
            BasicGraphData xgraphdata = new BasicGraphData();
            BasicGraphData ygraphdata = new BasicGraphData();
            BasicGraphData zgraphdata = new BasicGraphData();
            
            int rownumber = (itr.next()).intValue();
            String filename = (String)datatable.getValueAt(rownumber, 0);
            String name = (String)datatable.getValueAt(rownumber, 1);
            
            Map<String, String> bindings = new HashMap<String, String>();
            bindings.put("file", filename);
            bindings.put("wire", name);
            GenericRecord record = masterdatatable.record(bindings);
            wiredata=(ArrayList<ArrayList<Double>>)record.valueForKey("data");
            
            //ArrayList slist = (ArrayList)wiredata.get(0);
            ArrayList<Double> sxlist = wiredata.get(1);
            ArrayList<Double> sylist = wiredata.get(2);
            ArrayList<Double> szlist = wiredata.get(3);
            ArrayList<Double> xlist =  wiredata.get(4);
            ArrayList<Double> ylist =  wiredata.get(5);
            ArrayList<Double> zlist =  wiredata.get(6);
            
            int size = sxlist.size();
            
            //double[] sdata = new double[sxlist.size()];
            double[] sxdata = new double[sxlist.size()];
            double[] sydata = new double[sylist.size()];
            double[] szdata = new double[szlist.size()];
            double[] xdata = new double[xlist.size()];
            double[] ydata = new double[ylist.size()];
            double[] zdata = new double[zlist.size()];
            
            for(int i=0; i<size; i++){
                //sdata[i]=((Double)slist.get(i)).doubleValue();
                sxdata[i] = (sxlist.get(i)).doubleValue();
                sydata[i] = (sylist.get(i)).doubleValue();
                szdata[i] = (szlist.get(i)).doubleValue();
                xdata[i]=(xlist.get(i)).doubleValue();
                ydata[i]=(ylist.get(i)).doubleValue();
                zdata[i]=(zlist.get(i)).doubleValue();
            }
            
            double xmax=0; double ymax=0; double zmax=0;
            
            for(int i=0; i<size; i++){
                if(Math.abs(xdata[i]) > Math.abs(xmax)) xmax = xdata[i];
                if(Math.abs(ydata[i]) > Math.abs(ymax)) ymax = ydata[i];
                if(Math.abs(zdata[i]) > Math.abs(zmax)) zmax = zdata[i];
            }
            
            if(xmax < 0){
                for(int i=0; i<size; i++){
                    xdata[i]*=-1;
                }
            }
            if(ymax < 0){
                for(int i=0; i<size; i++){
                    ydata[i]*=-1;
                }
            }
            if(zmax < 0){
                for(int i=0; i<size; i++){
                    zdata[i]*=-1;
                }
            }
            
            
            
            if(!linearplot){
                double temp;
                double[] logxdata = new double[xdata.length];
                double[] logydata = new double[ydata.length];
                double[] logzdata = new double[zdata.length];
                for(int i=0; i<logxdata.length; i++){
                    temp=xdata[i];
                    if(temp<=0.0) temp=0.00001;
                    logxdata[i] = Math.log(temp)/Math.log(10);
                }
                for(int i=0; i<logydata.length; i++){
                    temp=ydata[i];
                    if(temp<=0.0) temp=0.00001;
                    logydata[i] = Math.log(temp)/Math.log(10);
                }
                for(int i=0; i<logzdata.length; i++){
                    temp=zdata[i];
                    if(temp<=0.0) temp=0.00001;
                    logzdata[i] = Math.log(temp)/Math.log(10);
                }
                xgraphdata.addPoint(sxdata,logxdata);
                ygraphdata.addPoint(sydata,logydata);
                zgraphdata.addPoint(szdata,logzdata);
            }
            else{
                xgraphdata.addPoint(sxdata, xdata);
                ygraphdata.addPoint(sydata, ydata);
                zgraphdata.addPoint(szdata, zdata);
            }
            xgraphdata.setDrawPointsOn(true);
            xgraphdata.setDrawLinesOn(true);
            xgraphdata.setGraphProperty("Legend", new String(filename+":"+name));
            xgraphdata.setGraphColor(IncrementalColors.getColor(xcolorindex++));
            ygraphdata.setDrawPointsOn(true);
            ygraphdata.setDrawLinesOn(true);
            ygraphdata.setGraphProperty("Legend", new String(filename+":"+name));
            ygraphdata.setGraphColor(IncrementalColors.getColor(ycolorindex++));
            zgraphdata.setDrawPointsOn(true);
            zgraphdata.setDrawLinesOn(true);
            zgraphdata.setGraphProperty("Legend", new String(filename+":"+name));
            zgraphdata.setGraphColor(IncrementalColors.getColor(zcolorindex++));
            
            xgrapharray.add(xgraphdata);
            ygrapharray.add(ygraphdata);
            zgrapharray.add(zgraphdata);
        }
        
        Iterator<BasicGraphData> xitr = xgrapharray.iterator();
        Iterator<BasicGraphData> yitr = ygrapharray.iterator();
        Iterator<BasicGraphData> zitr = zgrapharray.iterator();
        
        while(xitr.hasNext()){
            xrawdatapanel.addGraphData(xitr.next());
        }
        while(yitr.hasNext()){
            yrawdatapanel.addGraphData(yitr.next());
        }
        while(zitr.hasNext()){
            zrawdatapanel.addGraphData(zitr.next());
        }
        
        xrawdatapanel.setName("   Horizontal");
        xrawdatapanel.setLegendButtonVisible(true);
        yrawdatapanel.setName("    Vertical");
        yrawdatapanel.setLegendButtonVisible(true);
        zrawdatapanel.setName("    Diagonal");
        zrawdatapanel.setLegendButtonVisible(true);
    }
    
    public void makeDataTable(){
        String[] colnames = {"File Name", "Wire", "D fit", "V fit", "H fit", "Plot"};
        
        datatablemodel = new DataTableModel(colnames, 0);
        
        datatable = new JTable(datatablemodel);
        datatable.getColumnModel().getColumn(0).setMinWidth(185);
        datatable.getColumnModel().getColumn(1).setMinWidth(120);
        datatable.getColumnModel().getColumn(2).setMaxWidth(50);
        datatable.getColumnModel().getColumn(3).setMaxWidth(50);
        datatable.getColumnModel().getColumn(4).setMaxWidth(50);
        datatable.getColumnModel().getColumn(5).setMinWidth(50);
        
        datatable.setPreferredScrollableViewportSize(datatable.getPreferredSize());
        datatable.setRowSelectionAllowed(false);
        datatable.setColumnSelectionAllowed(false);
        datatable.setCellSelectionEnabled(false);
        
        datascrollpane = new JScrollPane(datatable,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        datascrollpane.getVerticalScrollBar().setValue(0);
        datascrollpane.getHorizontalScrollBar().setValue(0);
        datascrollpane.setPreferredSize(new Dimension(620, 100));
        
        
    }
    
    
    
}








