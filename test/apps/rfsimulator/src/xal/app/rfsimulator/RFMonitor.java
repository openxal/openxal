/**
 * RFMonitor.java
 *
 * Created on March 15, 2006, 5:31 PM
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

import xal.extension.widgets.swing.DecimalField;
import xal.extension.widgets.apputils.SimpleChartPopupMenu;
import xal.extension.widgets.plot.*;

public class RFMonitor implements ItemListener, ActionListener {  
                       
        RFDocument myDoc;
                        
        JPanel monitorPanel = null;
        JPanel results = null;
        JPanel control = null;
        
        protected FunctionGraphsJPanel plotamp;
        protected FunctionGraphsJPanel plotphs;
        
	//JComboBox cavity;
        Boolean second;        
        JButton stop;
        
        //close-open
        JButton close;
        //start-stop
        JButton start;
        //on-off
        JButton on;
        
        JTextField tfpulse;                 
        DecimalField tfk;
        DecimalField tfkp;
        DecimalField tfki;               
        DecimalField tfdelay;
        DecimalField tframp;
        DecimalField tfaff;
        DecimalField tfgain;
        DecimalField tfrot;
                
        JLabel jlrot;                
        JLabel jlk;
        JLabel jlpulse;
        JLabel jldelay;
        JLabel jlaff;
        JLabel jlramp;
        JLabel jlgain;
        
        double ffk=0.;
        double ffkp=0.;
        double ffki=0.;
        double dramp=0.;
        double daff=0.;
        double dgain=0.;
        double delay=0.;
        double drot=0.;
        
	    CurveData cava;        
        CurveData cavp;
        CurveData seta;
        CurveData setp;
        CurveData erra;
        CurveData errp;
                                        
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
                
                cava = new CurveData();
                cavp = new CurveData();        
                seta = new CurveData();
                setp = new CurveData();        
                erra = new CurveData();
                errp = new CurveData();        
                
                plotamp = new FunctionGraphsJPanel();
                plotphs = new FunctionGraphsJPanel();               
                                   
                results = new JPanel();
                results.setLayout(new GridLayout(2,2));
                
		results.addMouseListener(new SimpleChartPopupMenu(plotphs));
                plotphs.setLayout(new FlowLayout());
		plotphs.setGraphBackGroundColor(Color.white);
		plotphs.setPreferredSize(new Dimension(500,300));
		plotphs.setAxisNames("Turns", "Cav, Set & Err (rad)");
                
                cavp.setColor(Color.RED);       
                setp.setColor(Color.BLUE);       
                errp.setColor(Color.GREEN);       
		plotphs.addCurveData(setp);      
		plotphs.addCurveData(cavp);                
		plotphs.addCurveData(errp);                
                plotphs.setVisible(true);
                                               
		results.addMouseListener(new SimpleChartPopupMenu(plotamp));
                plotamp.setLayout(new FlowLayout());                
		plotamp.setGraphBackGroundColor(Color.white);
		plotamp.setPreferredSize(new Dimension(500,300));
		plotamp.setAxisNames("Turns", "Cav, Set & Err (kV)");
                cava.setColor(Color.RED);
                seta.setColor(Color.BLUE);           
                erra.setColor(Color.GREEN);           
		plotamp.addCurveData(cava);
		plotamp.addCurveData(seta);
		plotamp.addCurveData(erra);
                plotamp.setVisible(true);
                results.add(plotamp);
                results.add(plotphs);                
        }
        
        public void reset() {
               monitorPanel.setVisible(false);
               mkresults();
               mkcontrol();
               monitorPanel.add(control, BorderLayout.NORTH);                                
	       monitorPanel.add(results, BorderLayout.CENTER);                              
        }
        
        protected void plotcurves() {
                
                cava.setPoints(myDoc.count, myDoc.picka);
                seta.setPoints(myDoc.count, myDoc.amp);
                erra.setPoints(myDoc.count, myDoc.erra);
                
                cavp.setPoints(myDoc.count, myDoc.pickp);
                setp.setPoints(myDoc.count, myDoc.phs);
                errp.setPoints(myDoc.count, myDoc.errp);
                
                plotamp.refreshGraphJPanel();
                plotphs.refreshGraphJPanel();
                tfpulse.setText(String.valueOf(myDoc.pulsenumber));
        }
        
        private void mkcontrol() {
            
                if (control != null) {
                    monitorPanel.remove(control);
                }
                
                control = new JPanel();
                control.setLayout(new GridLayout(20,1));
                //cavity = new JComboBox(myDoc.cav);
                //cavity.setForeground(Color.BLUE);
                
                stop = new JButton("Stop");
                stop.setEnabled(true);                                
		stop.addActionListener(this);
                        
                close = new JButton("Close loop");
                close.setEnabled(true);                                
		close.addActionListener(this);
                start = new JButton("Start");
                start.setEnabled(true);                                
		start.addActionListener(this);
                
                on = new JButton("Turn on aff");
                on.setEnabled(true);                                
		on.addActionListener(this);
                
                jlk = new JLabel("K, Kp, Ki");
                jlpulse = new JLabel("pulse number");
                tfpulse = new JTextField(" 0");
                jldelay = new JLabel("Aff delay (us)");
                jlgain = new JLabel("Loop gain");
                tfgain = new DecimalField(dgain, 12);
                jlrot = new JLabel("Gain rotation (deg)");
                tfrot = new DecimalField(drot, 12);
                jlramp = new JLabel("Ramp time (us)");
                tframp = new DecimalField(dramp, 12);
                //control.add(cavity);
                control.add(start);               
                control.add(stop);                                
                control.add(jlpulse);
                control.add(tfpulse);
                control.add(jlgain);                 
                control.add(tfgain);
                control.add(jlrot);
                control.add(tfrot);
                control.add(jlramp);
                control.add(tframp);
                control.add(close);
                //JLabel dummy = new JLabel("");
                //control.add(dummy);
                control.add(jlk);
                tfk = new DecimalField(ffk, 12);
                tfkp = new DecimalField(ffkp, 12);
                tfki = new DecimalField(ffki, 12);
                control.add(tfk);
                control.add(tfkp);
                control.add(tfki);
                jldelay = new JLabel("Time ahead (us)");
                tfdelay = new DecimalField(delay, 12);
                jlaff = new JLabel("Start time (us)");
                tfaff = new DecimalField(daff,12);
                control.add(jldelay);
                control.add(tfdelay);
                control.add(jlaff);
                control.add(tfaff);
                control.add(on);                
        }
                
	public void itemStateChanged(ItemEvent ie) {
		Checkbox cb = (Checkbox) ie.getItemSelectable();
	}
        
	public void actionPerformed(ActionEvent ae) {
            
                /*
                if (ae.getActionCommand().equals(cavity.getActionCommand())) {
                        cv = cavity.getSelectedIndex();                        
                }
            
                if (ae.getActionCommand().equals("Reset")) { 
                        myDoc.getController().stoploop();                    
                        myDoc.getController().initloop();
                }               
                */
                
                if (ae.getActionCommand().equals("Stop")) {
                        myDoc.getController().stoploop();
                        //start.setText("Start");                                      
                        start.setEnabled(true);                                         
                        
                }
                
                else if (ae.getActionCommand().equals("Start")) {
                        myDoc.getController().initloop();                    
                        myDoc.getController().startloop();
                        stop.setEnabled(true);                                                                
                        //start.setText("Stop");
                }
                
                else if (ae.getActionCommand().equals("Close loop")) {
                        myDoc.setfbk(true);
                        close.setText("Open loop");                    
                }
                
                else if (ae.getActionCommand().equals("Open loop")) {
                        myDoc.setfbk(false);
                        close.setText("Close loop");                                        
                }
                
                else if (ae.getActionCommand().equals("Turn on aff")) {
                        myDoc.setaff(true);
                        on.setText("Turn off aff");                                                                
                }
                
                else if (ae.getActionCommand().equals("Turn off aff")) {
                        myDoc.setaff(false);
                        on.setText("Turn on aff");                                                                                    
                }
        }
                        
 	public RFMonitor(RFDocument doc) {
            myDoc = doc;            
        }                                
}