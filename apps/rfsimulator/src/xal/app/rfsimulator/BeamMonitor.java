/*
 * BeamMonitor.java
 *
 * Created on March 15, 2006, 5:32 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

import xal.smf.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.text.NumberFormat;

import xal.tools.swing.DecimalField;
import xal.tools.apputils.SimpleChartPopupMenu;
import xal.tools.plot.*;

public class BeamMonitor implements ItemListener, ActionListener {
                    
	RFDocument myDoc; 
        JPanel monitorPanel = null;
        JPanel results = null;
        JPanel control = null;
        
        protected FunctionGraphsJPanel plotpwd;
        protected FunctionGraphsJPanel plotbeam;
                
        //JButton reset;        
        JButton start;
        JButton stop;        
        JButton dumpdt;
                
        DecimalField tfcurrent;
        DecimalField tfenergy;
        DecimalField tflength;        
        DecimalField tfde;        
        DecimalField tfdp;
        DecimalField tfdc;
                
        JLabel jlcurrent;
        JLabel jlenergy;
        JLabel jllength;
        JLabel jlde;
        JLabel jldp;
        JLabel jldc;
        
        JLabel jlpulse;
        JTextField tfpulse;
        JLabel jlloss;
        JTextField tfloss;
        
        NumberFormat nf = NumberFormat.getNumberInstance();
        
        double current = 0.;
        double energy = 0.;
        double length = 0.;
        double de =0.;
        double dp =0.;
        double dc =0.;
        
	    CurveData cdcavp;        
	    CurveData cdbmct;     
        CurveData cdcav1;        
        CurveData cdcav2;        
        CurveData cdpow1;        
                
        BasicGraphData cdsep1;
        BasicGraphData cdsep2;
        BasicGraphData cdbeam;
        BasicGraphData cdkik1;
        BasicGraphData cdkik2;        
                
        public JPanel makePanel()
        {
                monitorPanel = new JPanel();
                BorderLayout gdl = new BorderLayout();
                monitorPanel.setLayout(gdl);                
                  
                mkresults();
                mkcontrol();
                
                monitorPanel.add(results, BorderLayout.CENTER);                                
		monitorPanel.add(control, BorderLayout.WEST);
                                
                return monitorPanel;
	}
        
        private void mkresults() {
                if (control != null) {
                    monitorPanel.remove(control);
                }
                
                cdcavp = new CurveData();    
                cdbmct = new CurveData();                    
                cdcav1 = new CurveData();
                cdcav2 = new CurveData();
                cdpow1 = new CurveData();
                
                cdsep1 = new BasicGraphData();
                cdsep2 = new BasicGraphData();
                cdbeam = new BasicGraphData();        
                cdkik1 = new BasicGraphData();
                cdkik2 = new BasicGraphData();
                
                plotpwd = new FunctionGraphsJPanel();
                plotbeam = new FunctionGraphsJPanel();               
                                   
                results = new JPanel();
                results.setLayout(new GridLayout(2,2));
                
		results.addMouseListener(new SimpleChartPopupMenu(plotbeam));
                plotbeam.setLayout(new FlowLayout());
		plotbeam.setGraphBackGroundColor(Color.white);
		plotbeam.setPreferredSize(new Dimension(500,300));
		plotbeam.setAxisNames("Phase (deg)", "dE/E");
                cdbeam.setGraphColor(Color.RED);       
                cdsep1.setGraphColor(Color.BLUE);       
                cdsep2.setGraphColor(Color.BLUE);                
                cdkik1.setGraphColor(Color.GREEN);       
                cdkik2.setGraphColor(Color.GREEN);                
		plotbeam.addGraphData(cdbeam);
		plotbeam.addGraphData(cdsep1);                
		plotbeam.addGraphData(cdsep2);
		plotbeam.addGraphData(cdkik1);                
		plotbeam.addGraphData(cdkik2);
                cdsep1.setDrawLinesOn(true);
                cdsep2.setDrawLinesOn(true);
                cdsep1.setDrawPointsOn(false);
                cdsep2.setDrawPointsOn(false);
                cdbeam.setDrawPointsOn(true);
                cdbeam.setDrawLinesOn(false);
                cdbeam.setGraphPointSize(1);                
                plotbeam.setVisible(true);
                                               
		results.addMouseListener(new SimpleChartPopupMenu(plotpwd));
                plotpwd.setLayout(new FlowLayout());                
		plotpwd.setGraphBackGroundColor(Color.white);
		plotpwd.setPreferredSize(new Dimension(500,300));
		plotpwd.setAxisNames("Turns & Last turn", "P(kW), Ib(A), U(kV)");
                cdcav1.setColor(Color.GREEN); 
                cdcav2.setColor(Color.GREEN); 
                cdpow1.setColor(Color.RED);                 
                cdcavp.setColor(Color.RED);
                cdbmct.setColor(Color.BLUE);           
		plotpwd.addCurveData(cdcavp);
		plotpwd.addCurveData(cdbmct);
		plotpwd.addCurveData(cdcav1);
		plotpwd.addCurveData(cdcav2);
		plotpwd.addCurveData(cdpow1);
                plotpwd.setVisible(true);
                
                results.add(plotpwd);
                results.add(plotbeam);             
        }
        
        public void reset() {
               monitorPanel.setVisible(false);
               mkresults();
               mkcontrol();               
               monitorPanel.add(control, BorderLayout.WEST);                                
	       monitorPanel.add(results, BorderLayout.CENTER);                              
        }
        
        private void mkcontrol() {
            
                if (control != null) {
                    monitorPanel.remove(control);
                }
                control = new JPanel();
                control.setLayout(new GridLayout(20,1));
                /*
                reset = new JButton("Reset");
                reset.setEnabled(true);                                
		reset.addActionListener(this);
                */
                start = new JButton("Start");
                start.setEnabled(true);                                
		start.addActionListener(this);
                
                stop = new JButton("Stop");
                stop.setEnabled(true);                                
		stop.addActionListener(this);
                
                //dumpdt = new JButton("Bm detune on");
                dumpdt = new JButton("dump result");
                dumpdt.setEnabled(false);                                
		dumpdt.addActionListener(this);
                
                control.add(start);
                control.add(stop);                
                //control.add(reset);
                control.add(dumpdt);
                jlpulse = new JLabel("pulse number");
                tfpulse = new JTextField(" 0");
                JLabel jlloss = new JLabel("beam loss (%)");
                tfloss = new JTextField(" 0");
                JLabel dummy = new JLabel(""); 
                jlcurrent = new JLabel("Beam current (mA)");
                jlenergy = new JLabel("Beam energy (MeV)");
                jllength = new JLabel("Pulse start (us)");
                jlde = new JLabel("dE (MeV)");
                jldp = new JLabel("dP (deg)");
                jldc = new JLabel("dC (mA)");
                tfcurrent = new DecimalField(current, 12);
                tfenergy = new DecimalField(energy, 12);
                tflength = new DecimalField(length, 12);
                tfde = new DecimalField(de, 12);
                tfdp = new DecimalField(dp, 12);
                tfdc = new DecimalField(dc, 12);
                control.add(jlpulse);
                control.add(tfpulse);
                control.add(jlloss);
                control.add(tfloss);
                control.add(dummy);
                control.add(jlenergy);
                control.add(tfenergy);
                control.add(jlcurrent);
                control.add(tfcurrent);
                control.add(jllength);
                control.add(tflength);
                control.add(jlde);
                control.add(tfde);
                control.add(jldp);
                control.add(tfdp);
                control.add(jldc);
                control.add(tfdc);                
        }
        
	public void itemStateChanged(ItemEvent ie) {
		Checkbox cb = (Checkbox) ie.getItemSelectable();
	}
        
	public void actionPerformed(ActionEvent ae) {
                /*                            
                if (ae.getActionCommand().equals("Reset")) { 
                     myDoc.getController().stoploop();
                     myDoc.getController().initloop();                    
                }                
                */
                if (ae.getActionCommand().equals("Stop")) {                                         
                     myDoc.getController().stoploop();
                     start.setEnabled(true);                
                     dumpdt.setEnabled(true);      
                }
                
                /*
                else if (ae.getActionCommand().equals("Bm detune on")) { 
                     myDoc.beamdetune = true;
                     dumpdt.setText("Bm detune off");
                }
                
                
                else if (ae.getActionCommand().equals("Bm detune off")) { 
                     myDoc.beamdetune = false;
                     dumpdt.setText("Bm detune on");
                }
                */
                
                else if (ae.getActionCommand().equals("dump result")) {
                     dumpdt.setEnabled(false);                                                    
                     dumpresults();                   
                }
                
                else if (ae.getActionCommand().equals("Start")) {
                     stop.setEnabled(true);                                          
                     myDoc.getController().startloop(); 
                }                
        }
        
        protected void plotcurves() {
            
            cdcavp.setPoints(myDoc.count, myDoc.rfpower);
            cdbmct.setPoints(myDoc.count, myDoc.bmct);            
            cdcav1.setPoints(myDoc.axis, myDoc.voltage1);
            cdcav2.setPoints(myDoc.axis, myDoc.voltage2);
            cdpow1.setPoints(myDoc.axis, myDoc.power1);
            
            cdsep1.addPoint(myDoc.phase, myDoc.sep1);                
            cdsep2.addPoint(myDoc.phase, myDoc.sep2);
            cdkik1.addPoint(myDoc.kik1, myDoc.kk);                
            cdkik2.addPoint(myDoc.kik2, myDoc.kk);
            cdbeam.addPoint(myDoc.bmdp, myDoc.bmde);
            
            plotpwd.refreshGraphJPanel();
            plotbeam.refreshGraphJPanel();
            
            nf.setMaximumFractionDigits(3);              
            tfpulse.setText(String.valueOf(myDoc.pulsenumber));
            tfloss.setText(nf.format(myDoc.lossrate));            
        }
        
        public void dumpresults(){
            myDoc.dumpdata();
        }
        
 	public BeamMonitor(RFDocument doc) {
            myDoc = doc;
        }                                
}