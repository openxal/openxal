/*
 * RFcavity.java
 *
 * Created on March 15, 2006, 2:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class RFcavity {
        
    double qloaded;
    double frequency;
    
    double rsh;    
    double capacitance;
    double inductance;
    
    double detune;    
    double fwdpower;
    double rflpower;
    double vb;
    
    Signal residue;
    Signal zl;
    Signal rfin;
    Signal bmin;
    
    double fill;
    double angle;
    double dfactor;
    
    double omiga;
    double dom;
    
    /** Creates a new instance of RFcavity */
    public RFcavity() {
        
        rsh = 1667.0;
        capacitance = 3.0E-9;
        inductance = 7.6E-6;
        
        qloaded = 50.0;        
        frequency = 1.05E6;
        fwdpower = 0.0;
        rflpower = 0.0;
        
        detune = 0.0;        
        dfactor = 0.0;
        angle =0.0;        
        omiga = 2.*Math.PI*frequency;
        fill  = 2.*qloaded/omiga;        
        dom   = omiga;        
        residue = new Signal();
    }    
    
    private void compute() {
        double d = 1. + detune/frequency;
        dfactor = qloaded*(d - 1./d);
        angle = Math.atan(dfactor);        
        omiga = frequency*2.*Math.PI;
        
        //dfactor = qloaded*detune*(2.*frequency + detune)/(frequency*(frequency+detune));
        //angle = -Math.atan(dfactor);
        
        if (omiga > 999.)
            fill = 2.*qloaded/omiga;        
    } 
            
    public void setcavity(double q, double f, double df) {
        
        if (q > 0.999)
            qloaded = q;
        
        if (f > 999.)
            frequency = f;        
        
        if (df > 0.1)
            detune = df;        

        dom = 2.*(detune+frequency)*Math.PI; 
        compute();
    }
    
    /*  
    public void currentdetune(double ct) {
        // linear
        //  450 A -> 180 kHz
        if (ct > 0.001) {
            detune = ct*400.; 
            compute();    
        }
    }
    */
    
    public void setcircuit(double r, double c, double i) { 
        if (r > 9.9)  {
            rsh = r;        
            capacitance = c;
            inductance = i;
        }        
    }
    
    public void setresidue(Signal rf) {
        residue = new Signal(rf);
    }
    
    public Signal getrf() {
        return residue;
    }
            
    public Signal getfield(Signal rf, Signal bm, double dt) {      
        
        vb = dt/fill;        
        residue.damp(vb);
        residue.rott(dt*omiga);
        
        rfin = new Signal(rf);        
        bmin = new Signal(bm); 
        zl = new Signal(rsh*vb/Math.sqrt(1.0+dfactor*dfactor), angle);        
        rfin.times(zl);        
        bmin.times(zl);
        
        residue.plus(rfin);                        
        residue.plus(bmin);
        
        fwdpower = rf.getamp()*rf.getamp()*rsh;
        
        // Actual Power and Beam Loading Determined by Amp-Cav Coupling
        // Vb = 2*Ib*Rs/(1+beta)
        // Vg = 2*sqrt(beta*Pg*Rs)/(1+beta) 
        // But no one cares about RF power and, at here, beta ~ 1.0
        
        return residue;
        
    }
    
    public double getfwdpower() {
        return fwdpower;
    }    
/*             
    public double getfill() {
        return qloaded/(frequency*Math.PI);
    }
    
    public double getband() {
        return frequency/qloaded; 
    }
    
    public double getdetune() {
        return Math.atan(2.*qloaded*detune/frequency);
    }
    
    public double getrflpower() {
        return rflpower;
    }    
 */
}

