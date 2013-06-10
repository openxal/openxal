/*
 * RFController.java
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
import java.io.*;
import java.util.*;

import xal.tools.swing.DecimalField;
import xal.tools.apputils.SimpleChartPopupMenu;
import xal.tools.plot.*;

public class RFController implements ItemListener {
    
	RFDocument myDoc;
        Accelerator accel;
        
        JPanel control;
        JPanel inPanel;
        JPanel shPanel;
        
        CurveData cdamp1 = new CurveData();        
        CurveData cdphase1 = new CurveData();
        CurveData cdamp2 = new CurveData();
        CurveData cdphase2 = new CurveData();
        CurveData cddetune1 = new CurveData();
        CurveData cddetune2 = new CurveData();
        
        protected FunctionGraphsJPanel first;
        protected FunctionGraphsJPanel second;
        
        JTextField tffile;
        String filename;
        JButton jbread;
        JButton jbccpt;
        
        DecimalField dfamp1;
        DecimalField dfamp2;
        DecimalField dfphase1;
        DecimalField dfphase2;
        DecimalField dfdetune1;
        DecimalField dfdetune2;
        DecimalField dfchopper;
        DecimalField dfkicker;
        DecimalField dfcable;
        DecimalField dfband;
        
        double amp1=0.;
        double amp2=0.;
        double phase1=0.;
        double phase2=0.;
        double detune1=0.;
        double detune2=0.;
        
        double cable=0.;
        double chopper=0.;
        double kicker=0.;
        double band = 0.;
        
        //JLabel jlfile;
        JLabel jlamp;
        JLabel jlphase;
        JLabel jldetune;
        JLabel jlchopper;
        JLabel jlkicker;
        JLabel jlcable;
        JLabel jlband;
        
        protected JPanel makePanel() {
            
                filename = "default.txt";                                                                                 
                          
		jbread = new JButton("Read from file");
                jbread.setEnabled(true);
		jbread.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                            Scanner infile = null;
                            try {
                                infile = new Scanner(new File(tffile.getText()));
                            } catch (FileNotFoundException fe) {
                                myDoc.errormsg("Did not found file " + tffile.getText());
                                return;
                            }
                            
                            try {
                                
                                chopper = infile.nextDouble();
                                kicker  = infile.nextDouble();
                                cable   = infile.nextDouble();
                                band    = infile.nextDouble();
                                
                                myDoc.rott[0]=infile.nextDouble()*0.01745329;
                                myDoc.rott[1]=infile.nextDouble()*0.01745329;
                                myDoc.ramp=infile.nextDouble()*1E-6;
                                myDoc.affdelay=infile.nextDouble()*1E-6;
                                myDoc.start=infile.nextDouble()*1E-6;
                                myDoc.k=infile.nextDouble(); 
                                myDoc.kp= infile.nextDouble();
                                myDoc.ki=infile.nextDouble();
  
                                myDoc.beamenergy=infile.nextDouble();
                                myDoc.beamcurrent=infile.nextDouble()*1E-3;
                                myDoc.pulsestart=infile.nextDouble()*1E-6;
                                myDoc.de=infile.nextDouble();
                                myDoc.dz=infile.nextDouble()*0.01745329;
                                myDoc.dc=infile.nextDouble()*1E-3;
             
                                
                                for (int i=0; i<11; i++ ) {
                                    myDoc.turn[i]     =infile.nextDouble();
                                    myDoc.ampset[0][i]=infile.nextDouble();
                                    myDoc.phsset[0][i]=infile.nextDouble()*0.01745329;
                                    myDoc.detune[0][i]=infile.nextDouble()*1E3;
                                    myDoc.ampset[1][i]=infile.nextDouble();
                                    myDoc.phsset[1][i]=infile.nextDouble()*0.01745329;
                                    myDoc.detune[1][i]=infile.nextDouble()*1E3;                                    
                                    myDoc.gain[i]     =infile.nextDouble();                                    
                                }
                                
                            } catch (NoSuchElementException ne) {
                                myDoc.errormsg("Error reading " + ne);
                                infile.close();                                
                                return;
                                
                            } catch (IllegalStateException ie) {
                                myDoc.errormsg("Error reading " + ie);
                                infile.close();                                
                                return;
                            } 
                            
                            infile.close(); 
                            update();
                            plotcurves();                            
                        }
                });
                
                inPanel  = new JPanel(new GridLayout(20,1)); 
                //jlfile   = new JLabel("Filename");
                jlamp    = new JLabel("Amp 1 and 2 (kV)");
                jlphase  = new JLabel("Phase 1 and 2 (deg)");
                jldetune = new JLabel("Detune 1 and 2 (kHz)");
                jlchopper= new JLabel("Chopper gate (us)");
                jlkicker = new JLabel("Ext. Kicker gate (us)");
                tffile   = new JTextField(filename);
                jbread.setForeground(Color.BLUE);
                //inPanel.add(jlfile);
                inPanel.add(tffile);                
                inPanel.add(jbread);
                inPanel.add(jlamp);
                JLabel jlband = new JLabel("Bandwidth (kHz)");
                dfband = new DecimalField(band, 12);
                
                dfamp1 = new DecimalField(amp1, 12);
                dfamp2 = new DecimalField(amp2, 12);
                dfphase1 = new DecimalField(phase1, 12);
                dfphase2 = new DecimalField(phase2, 12);
                dfdetune1= new DecimalField(detune1, 12);
                dfdetune2= new DecimalField(detune2, 12);
                dfchopper= new DecimalField(chopper, 12);
                dfkicker = new DecimalField(kicker, 12);
                jlcable = new JLabel("Total loop delay (us)"); 
                dfcable = new DecimalField(cable, 12);        
                inPanel.add(dfamp1);
                inPanel.add(dfamp2);
                inPanel.add(jlphase);
                inPanel.add(dfphase1);
                inPanel.add(dfphase2);
                
                inPanel.add(jldetune);
                inPanel.add(dfdetune1);
                inPanel.add(dfdetune2);
                
                inPanel.add(jlchopper);
                inPanel.add(dfchopper);
                inPanel.add(jlkicker);
                inPanel.add(dfkicker);
                inPanel.add(jlcable);
                inPanel.add(dfcable);
                inPanel.add(jlband);
                inPanel.add(dfband);
                jbccpt = new JButton("Use input");
                jbccpt.setEnabled(true);
                jbccpt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
                                chopper = dfchopper.getDoubleValue();
                                kicker  = dfkicker.getDoubleValue();
                                cable   = dfcable.getDoubleValue();
                                band    = dfband.getDoubleValue();
                                
                                for (int i=0; i<11; i++ ) {
                                    myDoc.turn[i]= (float) i*100;
                                    myDoc.ampset[0][i]= dfamp1.getDoubleValue();
                                    myDoc.phsset[0][i]= dfphase1.getDoubleValue()*0.01745329;
                                    myDoc.detune[0][i]= dfdetune1.getDoubleValue()*1E3;
                                    myDoc.ampset[1][i]= dfamp2.getDoubleValue();
                                    myDoc.phsset[1][i]= dfphase2.getDoubleValue()*0.01745329;
                                    myDoc.detune[1][i]= dfdetune2.getDoubleValue()*1E3;
                                }    
                                
                                plotcurves();
                                //initloop();
                        }
                });
                
                inPanel.add(jbccpt);                
                JPanel shPanel = new JPanel();     
                shPanel.setLayout(new GridLayout(2,2));                
                first  = new FunctionGraphsJPanel();
                second = new FunctionGraphsJPanel();                
		shPanel.addMouseListener(new SimpleChartPopupMenu(first));
                
                first.setLayout(new FlowLayout());
		first.setGraphBackGroundColor(Color.white);
		first.setPreferredSize(new Dimension(500,300));
		first.setAxisNames("Turns", "Amp1, Phs1");
                
                cdamp1.setColor(Color.RED);    
                cdphase1.setColor(Color.BLUE);       
                cddetune1.setColor(Color.GREEN);
                
		first.addCurveData(cdamp1);                
		first.addCurveData(cdphase1);                
		first.addCurveData(cddetune1);                
                first.setVisible(true);
                
                                                               
                second.setLayout(new FlowLayout());
		second.setGraphBackGroundColor(Color.white);
		second.setPreferredSize(new Dimension(500,300));
		second.setAxisNames("Turns", "Amp2, Phs2");
                
                cdamp2.setColor(Color.RED);       
                cdphase2.setColor(Color.BLUE);
                cddetune2.setColor(Color.GREEN);
		second.addCurveData(cdamp2);                
		second.addCurveData(cdphase2);                
		second.addCurveData(cddetune2);                
                second.setVisible(true);
                shPanel.add(first);
                shPanel.add(second);
                
                control = new JPanel();
                control.setLayout(new BorderLayout());                
                control.add(shPanel, BorderLayout.CENTER);
                control.add(inPanel, BorderLayout.WEST);
		control.setVisible(true);                
                return control;               
                
	}
        
        private void plotcurves() {
            
                cdamp1.setPoints(myDoc.turn,  myDoc.ampset[0]);
                cdamp2.setPoints(myDoc.turn,  myDoc.ampset[1]);
                cdphase1.setPoints(myDoc.turn,  myDoc.phsset[0]);
                cdphase2.setPoints(myDoc.turn,  myDoc.phsset[1]);
                //cddetune1.setPoints(myDoc.turn,  myDoc.detune[0]);
                //cddetune2.setPoints(myDoc.turn,  myDoc.detune[1]);
                first.refreshGraphJPanel();
                second.refreshGraphJPanel();                
        }
        
	public void itemStateChanged(ItemEvent ie) {
		Checkbox cb = (Checkbox) ie.getItemSelectable();
	}
                       
 	public RFController(RFDocument doc) {
            myDoc = doc;
            accel = new Accelerator(doc);
        }
        
      public void stoploop() {
            myDoc.setstop(true);
            myDoc.getBeam().dumpdt.setEnabled(true); 
      }
        
      public void startloop() {
                           
            if (myDoc.getstop()) {
                myDoc.getBeam().dumpdt.setEnabled(false); 
                
                //if (myDoc.pulsenumber == 0) {
                initloop();                
                //}
                 
                myDoc.setstop(false);            
                Thread t = new Thread(accel);
                t.start();
            }
      }
                
      public void initloop() {
            
            myDoc.cabledelay = cable*1.E-6;
            myDoc.kicker = kicker*1.E-6;            
            myDoc.chopper = chopper*1.E-6;
            myDoc.rott[0] = myDoc.getMonitor().tfrot.getDoubleValue()*0.01745329;
            myDoc.rott[1] = myDoc.rott[0];       
            myDoc.pulsenumber = 0;
            myDoc.bandwidth = band*1E3;
            myDoc.lossrate  = 0.;
            
            for (int i=0; i<129; i++) {
                    myDoc.voltage1[i] = 0.0;
                    myDoc.voltage2[i] = 0.0;
                    myDoc.power1[i] = 0.0;
                    myDoc.axis[i] = i*7.8125;
            }
            
            for (int i = 0; i<1024; i++) {
                    myDoc.rfpower[i] = 0.0;
                    myDoc.bmct[i] = 0.0;
                    myDoc.erra[i] = 0.0;
                    myDoc.errp[i] = 0.0;
                    myDoc.picka[i] = 0.0;
                    myDoc.pickp[i] = 0.0;                 
            }
            
            myDoc.getMonitor().dgain =  myDoc.getMonitor().tfgain.getDoubleValue();
            myDoc.ramp = myDoc.getMonitor().tframp.getDoubleValue()*1E-6;                    
            
            if (myDoc.getMonitor().dgain > 0.02) {                
                    for (int i=0; i<11; i++)
                        myDoc.gain[i] = myDoc.getMonitor().dgain;                    
            }
                
            myDoc.getMonitor().ffk = myDoc.getMonitor().tfk.getDoubleValue();
            
            if (myDoc.getMonitor().ffk > 0.02) {
                
                    myDoc.affdelay = myDoc.getMonitor().tfdelay.getDoubleValue()*1E-6;
                    myDoc.start = myDoc.getMonitor().tfaff.getDoubleValue()*1E-6;
                    
                    myDoc.k = myDoc.getMonitor().ffk;                    
                    myDoc.kp = myDoc.getMonitor().tfkp.getDoubleValue();
                    myDoc.ki = myDoc.getMonitor().tfki.getDoubleValue();               
            }
            
            myDoc.beamenergy = myDoc.getBeam().tfenergy.getDoubleValue();
            
            if (myDoc.beamenergy < 700.)
                myDoc.beamenergy = 1000.0;
            
            myDoc.de = myDoc.getBeam().tfde.getDoubleValue();
            myDoc.dc = myDoc.getBeam().tfdc.getDoubleValue()*0.001;
            myDoc.dz = myDoc.getBeam().tfdp.getDoubleValue()*0.01745329;
            
            myDoc.beamcurrent = myDoc.getBeam().tfcurrent.getDoubleValue()*0.001;            
            myDoc.pulsestart = myDoc.getBeam().tflength.getDoubleValue()*1.E-6;
            
            // test beams                              
            for (int i=0; i<50000; i++) {
                accel.beams[i] = new Beam(myDoc.beamenergy, myDoc.beamcurrent);
            } 
                        
            //accel.initaccel(); 
            myDoc.period = accel.circle/(accel.beams[0].getbeta()*2.99792458E8);
            myDoc.step = 0.0078125*myDoc.period;             
            myDoc.frequency[0] = 1./myDoc.period;            
            myDoc.frequency[1] = 2.*myDoc.frequency[0];
            myDoc.loopq = 0.5*myDoc.frequency[0]/myDoc.bandwidth;
            //mydoc.loopf = myDoc.frequency[0];
                    
            for (int i =0; i<2; i++) {
                myDoc.kik1[i] = -180*myDoc.kicker/myDoc.period;
                myDoc.kik2[i] = -myDoc.kik1[i];
            }
      }
      
      public void update() {
             dfchopper.setValue(chopper);
             dfkicker.setValue(kicker);
             dfcable.setValue(cable); 
             dfband.setValue(band);
                            
             myDoc.getMonitor().tframp.setValue(myDoc.ramp*1E6);
             myDoc.getMonitor().tfrot.setValue(myDoc.rott[0]*57.29578);
             myDoc.getMonitor().tfdelay.setValue(myDoc.affdelay*1E6);
             myDoc.getMonitor().tfaff.setValue(myDoc.start*1E6);
             myDoc.getMonitor().tfk.setValue(myDoc.k);
             myDoc.getMonitor().tfkp.setValue(myDoc.kp);
             myDoc.getMonitor().tfki.setValue(myDoc.ki);
             myDoc.getBeam().tfenergy.setValue(myDoc.beamenergy);
             myDoc.getBeam().tfcurrent.setValue(myDoc.beamcurrent*1E3);
             myDoc.getBeam().tflength.setValue(myDoc.pulsestart*1E6);
             myDoc.getBeam().tfde.setValue(myDoc.de);
             myDoc.getBeam().tfdp.setValue(myDoc.dz*57.29578);
             myDoc.getBeam().tfdc.setValue(myDoc.dc*1E3);
      }
}
