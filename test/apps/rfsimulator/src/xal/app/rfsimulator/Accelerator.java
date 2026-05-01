/*
 * Accelerator.java
 *
 * Created on March 15, 2006, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package xal.app.rfsimulator;

/**
 *
 * @author y32
 */

public class Accelerator implements Runnable {
    
    RFDocument doc;
    /*
    double xtune;
    double ytune;
    double xbeta1;
    double ybeta1;
    double xalpha1;
    double yalpha1;
    double xbeta2;
    double ybeta2;
    double xalpha2;
    double yalpha2;
    */
    
    double etotal;
    double circle;
    double gammat;
    double beta2;
    double gamma2;
    double slip;
    double jump;
    
    boolean accumulate;
    
    //double coef = 0.0;             
    int sfbk = 0;
    int saff = 0;
    int K =0;
    int J =0;
    int L =0;
    int ai=0;
    int aj=0;
    int bi=0;
    int bj=0;
    
    Fft fft;
    Beam[] beams;    
    RFLoop[] loops;
    Signal[][] lp;
    Signal[] er;    
    Signal bm;  
    
    /*                
    Matrix mx; 
    Matrix my;
    Matrix mz;
    Vector vx;
    Vector vy;
    Vector vz;
    */
    
    /** Creates a new instance of Accelerator */
    public Accelerator(RFDocument my) {
        
        doc = my;
        /*
        xtune = 6.23;
        ytune = 6.20;
        xbeta1 = 10.659;
        xalpha1=  0.219;
        xbeta2 = 10.724;
        xalpha2= -0.233;
        ybeta1 = 11.038;
        yalpha1=  0.183;
        ybeta2 = 11.330;
        yalpha2= -0.247;
        */
                        
        //5.245869^2
        gammat = 27.5191416;        
        circle = 248.00935;        
        slip   = 0.0;
        jump   = 0.0;        
        etotal = 1938.27;
        beta2  = 0.7656704;
        gamma2 = 4.2674937;       
        /*
        double u, a, g, b;
        
        u = 2.*Math.PI*xtune;
        a = xalpha1;
        b = xbeta1;
        g = (1. + a*a)/b;
         
        mx = new Matrix(Math.cos(u)+a*Math.sin(u),
                        b*Math.sin(u),  -g*Math.sin(u),
                        Math.cos(u)-a*Math.sin(u));
         
        u = 2.*Math.PI*ytune;
        a = yalpha1;
        b = ybeta1;
        g = (1. + a*a)/b;
         
        my = new Matrix(Math.cos(u)+a*Math.sin(u),
                        b*Math.sin(u),  -g*Math.sin(u),
                        Math.cos(u)-a*Math.sin(u));
        
        vx = new Vector();
        vy = new Vector();
        */    
        
        loops = new RFLoop[2];
        beams = new Beam[50000];
        lp = new Signal[128][1024];
        er = new Signal[128];
    }    
    
    public void initaccel() {
    }
        
    public RFLoop getloop(int j) {
        return loops[j];
    }
    
    private void response(Signal[] step) {
        
         double f1, f2;        
         Signal filter;
         
         for (int i=0; i<step.length; i++) {
             step[i].compute();         
         }         
         Fft.transfm(1, step.length, step);
         
         for (int i=0; i<step.length; i++) {
             
             //within-turn
             f1 = (float) (1 + i);
             
             //turn-by-turn (totally 1024 turns) 
             /*
             if (i < 0.5*step.length)
                 f1 = 1. + i*0.0009765625;
             else
                 f1 = 1. - i*0.0009765625;
             */
             
             f2 = doc.loopq*(f1 - 1./f1);
             filter = new Signal(1./Math.sqrt(1.+f2*f2), Math.atan(f2));
             step[i].times(filter);
             step[i].compute();
         }         
         Fft.transfm(-1, step.length, step);
    }
    
    public void run() {
        
        double t;
        double dt;
        //double averagebeam;
        double kr = Math.sqrt(beams[0].mc2*beams[0].getbeta()*beams[0].getbeta()*beams[0].getgamma()*
                              beams[0].getgamma()*beams[0].getgamma()/Math.PI)/beams[0].getenergy();
        double actp;
        double acta;
        double fc1 = 0.04*Math.PI;
        double fc2 = 0.015625*Math.PI;
        
        //Signal[] tmpp = new Signal[128];
        //Signal[] tmpi = new Signal[128];        
            
        sfbk = Math.round((float)doc.cabledelay/(float)doc.step);
        saff = Math.round((float)doc.affdelay/(float)doc.step);
            
        ai = saff/128;
        aj = saff%128;            
        bi = sfbk/128;
        bj = sfbk%128;
            
        beams[0].inject(doc.de, doc.dz);
	    
        doc.myWindow().progressBar.setIndeterminate(true);
                        
        for (int i = 0; i<1024; i++) {
                K = i/100;
                J = i%100;
                
                if (K < 10) {
                    doc.amp[i] = doc.ampset[0][K] + 0.01*J*(doc.ampset[0][K+1]-doc.ampset[0][K]); 
                    doc.phs[i] = doc.phsset[0][K] + 0.01*J*(doc.phsset[0][K+1]-doc.phsset[0][K]);
                }
                
                else {
                    doc.amp[i] = doc.ampset[0][K]; 
                    doc.phs[i] = doc.phsset[0][K];
                }
        }
            
        etotal = beams[0].mc2 + beams[0].getenergy();
        beta2  = beams[0].getbeta()*beams[0].getbeta();
        gamma2 = beams[0].getgamma()*beams[0].getgamma();
            
        // Assume de tiny
        slip = 2.*Math.PI*(1./gamma2-1./gammat)/etotal/beta2;
        //coef = doc.beamcurrent*doc.chopper/doc.period; 
        
        J = Math.round((float)doc.pulsestart/(float)doc.period);
        if (J < 24)
                J = 24;
        
        for (int j =0; j<2; j++ ) {
            
                loops[j]= new RFLoop();
                loops[j].setcalibration(0.6, 1000.0);
                //loops[j].getfb().setgain(doc.gain[j]);
                //loops[j].getfb().setmode(doc.getfbk());
                loops[j].getfwd().setgain(doc.k, doc.kp, doc.ki);
                //loops[j].getfwd().setmode(doc.getaff());
        }
        
        for (int j =0; j<128; j++ ) {    
                er[j] = new Signal();
                for (int i=0; i<1024; i++) {
                    lp[j][i] = new Signal();
                    doc.ei[j][i] = new Signal();
                    doc.ep[j][i] = new Signal();                                 
                }             
        }
        
        // Any effort to speed the following part will be good one 
        for (;;) {
                
            /*    
            if (doc.getstop()) {
                doc.errormsg("Stop running");            
                return;
            }
            */
                            
            t = 0.0;           
            //averagebeam = 0.0;
            accumulate = false;                         
            bm = new Signal(); 
            loops[0].getcav().setresidue(bm);
            doc.lossrate = 0.;            
                        
            for (int i=1; i<1024; i++) {
                
                if (doc.getstop()) {
                    doc.myWindow().progressBar.setIndeterminate(false);                    
                    doc.errormsg("Stop running");            
                    return;
                }
                                
                K = i/100;             
                L = i%100;
                
		        doc.myWindow().progressBar.setValue(i);
                
                if ( !accumulate ) {
                    if ( i >= J )
                        accumulate = true;
                }                
                //else if (doc.beamdetune) {
                //    averagebeam = coef*(i-J);
                //}
                                
                if (K < 10) {
                    // changing R & Q
                    /*
                    loops[0].getcav().setcavity(48.0 - i*0.033, doc.frequency[0], 
                                     doc.detune[0][K] + 0.01*L*(doc.detune[0][K+1]-doc.detune[0][K]));                     
                    loops[0].getcav().setcircuit(1666.7 - 0.5*i, 3.00E-9, 7.6E-6);                                                         
                     */ 
                    // constant R & Q
                    loops[0].getcav().setcavity(48, doc.frequency[0], 
                                     doc.detune[0][K] + 0.01*L*(doc.detune[0][K+1]-doc.detune[0][K]));
                    loops[0].getcav().setcircuit(1667, 3.00E-9, 7.6E-6);                              
                    
                    loops[0].getfb().setgain(doc.gain[K] + 0.01*L*(doc.gain[K+1]-doc.gain[K]));
                    //loops[0].getcav().currentdetune(averagebeam);
                                        
                /* To save time:
                 * #1 cavities (3) with exactly the same control 
                 * #2 cavity with perfect control
                 *
                 * loops[1].getcav().setcavity(27., doc.frequency[1], doc.detune[1][K]);                
                 * loops[1].getcav().setcircuit(2666.7, 0.75E-9, 7.6E-6);
                 */ 
                    
                }  
                
                else {                                        
                    //loops[0].getcav().setcavity(48.0 - i*0.033, doc.frequency[0], 
                    //                 doc.detune[0][9] + 0.01*L*(doc.detune[0][9]-doc.detune[0][8])); 
                    //loops[0].getcav().setcircuit(1666.7 - 0.5*i, 3.00E-9, 7.6E-6);                
                    
                    loops[0].getcav().setcavity(48.0, doc.frequency[0], 
                                       doc.detune[0][10] + 0.01*L*(doc.detune[0][10]-doc.detune[0][9])); 
                    loops[0].getcav().setcircuit(1667, 3.00E-9, 7.6E-6);                
                    
                    loops[0].getfb().setgain(doc.gain[10]);                    
                    //loops[0].getcav().currentdetune(averagebeam);                
                }
                                
                dt = -64.*doc.step;
                                              
                for (int j = 0; j<128; j++) {
                                         
                    Signal s = new Signal(doc.amp[i], doc.phs[i] - Math.PI + j*0.0490873852124); 
                    
                    // error signal in loop                   
                    er[j] = new Signal(s);
                                       
                    if (accumulate) {                        
                        bm = new Signal( (beams[0].getcurrent(dt, doc.chopper) 
                                             + (0.1-0.2*Math.random())*doc.dc)*(i-J),
                                          (0.1 - 0.2*Math.random())*doc.dz - 0.5*Math.PI );                        
                    }
                    
                    // loop delay
                    if ( t >= doc.cabledelay) {
                        
                         if (j < bj) {                             
                             lp[j+128-bj][i-1-bi].rott(doc.rott[0]);
                             er[j].minus(lp[j+128-bj][i-1-bi]);
                         }
                         
                         else {
                             lp[j-bj][i-bi].rott(doc.rott[0]);                             
                             er[j].minus(lp[j-bj][i-bi]);
                         }                         
                    }
                    
                    // minimum bandwidth 5 kHz
                    if ( doc.bandwidth > 4999.999 )
                         response(er);
                       
                    
                    //closed loop
                    if ( doc.getfbk() && t > doc.ramp ) {
                        
                            //feedforward only after loop closed
                            if ( doc.getaff() && t > doc.start ) {
                                    if (i + ai < 1024) {  
                                        
                                        if ( j + aj < 128)
                                            loops[0].loopcircle(s, er[j], doc.ep[j+aj][i+ai],
                                                            doc.ei[j+aj][i+ai], bm, doc.step);
                                        
                                        else if (i + ai < 1023)
                                            loops[0].loopcircle(s, er[j], doc.ep[j+aj-128][i+ai+1], 
                                                            doc.ei[j+aj-128][i+ai+1], bm, doc.step);
                                        
                                        else
                                            loops[0].loopcircle(s, er[j], doc.ep[j+aj-128][1023],
                                                            doc.ei[j+aj-128][1023], bm, doc.step);                                                                    
                                    } 
                                    
                                    else {
                                        loops[0].loopcircle(s, er[j], doc.ep[j][1023], doc.ei[j][1023], 
                                                bm, doc.step);                                    
                                    }
                            }
                            
                            else
                                loops[0].loopcircle(s, er[j], bm, doc.step);                                
                    }
                    
                    //open loop
                    else {                        
                         loops[0].loopcircle(s, bm, doc.step);
                    }
                                        
                    lp[j][i] = new Signal(loops[0].getloop());
                    doc.ep[j][i]=new Signal(s);
                    doc.ep[j][i].minus(lp[j][i]);
                                                                                                   
                    doc.ei[j][i] = new Signal(doc.ep[j][i]);
                    doc.ei[j][i].plus(doc.ei[j][i-1]);
                    
                    // Turn-by-turn
                    if (j == 64) {
                        doc.rfpower[i] = loops[0].getcav().getfwdpower()*0.001;
                        doc.bmct[i] = bm.getamp();
                    }
                            
                    //Last turn
                    if (i == 1023) {
                        doc.power1[j]  =loops[0].getcav().getfwdpower()*0.001;
                        doc.voltage1[j]=loops[0].getcv().getamp()*0.001;
                    }                       
                    
                    //tmpp[j] = new Signal(doc.ep[j][i]);
                    //tmpi[j] = new Signal(doc.ei[j][i]);
                    
                    dt += doc.step;  
                    t  += doc.step;                    
                }
                
                doc.erra[i]=doc.ep[64][i].getamp();
                doc.errp[i]=doc.ep[64][i].getphase();
                doc.picka[i]=lp[64][i].getamp();
                doc.pickp[i]=lp[64][i].getphase();
                
                /*
                response(tmpp);
                response(tmpi);
                
                for (int p=0; p<128; p++) {
                    doc.ep[p][i] = new Signal(tmpp[p]);
                    doc.ei[p][i] = new Signal(tmpi[p]);                    
                }          
                */
                
                // Particle tracking with actual #1 harmonic and perfect #2 harmonic 
                if (i >= J) {                    
                    for (int p=0; p<50; p++) {
                        beams[p+50*(i-J)].inject(doc.de, doc.dz); 
                        beams[p+50*(i-J)].transport(0.0, (p%50 - 24.5)*fc1*doc.chopper/doc.period);
                    }
                                        
                    for (int b = i-J; b > -1; b-- ) {
                        
                        for (int p = 0; p <50; p++) {
                            
                            actp = beams[p+50*b].getdp(); 
                            acta = doc.picka[i]; 
                            
                            while (actp > Math.PI ) {
                                actp = actp - 2.*Math.PI; 
                            }
                            
                            while (actp <= -Math.PI ) {
                                actp = actp + 2.*Math.PI; 
                            }
                            
                            for (int w = 0; w < 128; w ++) {
                                
                                if ( actp < fc2*w - Math.PI ) {
                                    
                                    acta = lp[w-1][i].getamp()  + (actp - fc2*w + Math.PI)*
                                            (lp[w][i].getamp()-lp[w-1][i].getamp())/fc2; 
                                    
                                    actp = lp[w-1][i].getphase()+ (actp - fc2*w + Math.PI)*
                                            (lp[w][i].getphase()-lp[w-1][i].getphase())/fc2;                                    
                                    
                                    //actp = lp[w-1][i].getphase();                                    
                                    //acta = lp[w-1][i].getamp(); 
                                    
                                    break;                                    
                                }
                            }
                                                         
                            jump = 3.E-3*(acta*Math.sin(actp)-doc.picka[i]*Math.sin(doc.pickp[i]))
                                   -1.E-3*doc.ampset[1][9]*(Math.sin(2.*beams[p+50*b].getdp())
                                          - Math.sin(doc.phsset[1][9]));                                                           
                            beams[p+50*b].transport(jump, -beams[p+50*b].getde()*slip );
                        }
                    } 
                }
               
                /*
                //Simple particle tracking with turn-by-turn #1 and perfect #2
                if (i >= J) {
                    
                    for (int p=0; p<50; p++) {
                        beams[p+50*(i-J)].inject(doc.de, doc.dz); 
                        beams[p+50*(i-J)].transport(0.0, fc1*(p%50 - 24.5)*doc.chopper/doc.period);
                    }
                                                                               
                    for (int b = i-J; b > -1; b-- ) {                        
                        for (int p = 0; p <50; p++) {
                            
                            jump = 3.E-3*doc.picka[i]*(Math.sin(beams[p+50*b].getdp())- Math.sin(doc.pickp[i]))
                                   -1.E-3*doc.ampset[1][9]*(Math.sin(2.*beams[p+50*b].getdp())
                                          - Math.sin(doc.phsset[1][9]));                         
                            beams[p+50*b].transport(jump, -beams[p+50*b].getde()*slip );
                        }
                    }                     
                } 
                */ 
            }
            
            // may use another thread to do the following stuff
            doc.power1[128]   = doc.power1[0];
            doc.voltage1[128] = doc.voltage1[0];
            jump = loops[0].getcv().getamp()*3.E-6;   
            
            for (int j= 0; j< 50000; j++) {
                    doc.bmde[j] = 0.0;
                    doc.bmdp[j] = 0.0;
            }
            
            if ( J < 1024 ) {
                for (int j = 0; j < 50*(1024-J); j++) {
            
                    doc.bmde[j]=beams[j].getde()/beams[j].getenergy();
                    doc.bmdp[j]=beams[j].getdp()*57.2957795;
                   
                    while (doc.bmdp[j] > 180) {
                        doc.bmdp[j] -= 360;
                    }
                
                    while (doc.bmdp[j] < -180) {
                        doc.bmdp[j] += 360;
                    }
                    
                    if (doc.bmdp[j] >= doc.kik2[0]) {
                        doc.lossrate += 0.002; 
                    }
                    
                    else if (doc.bmdp[j] <= doc.kik1[0]) {
                        doc.lossrate += 0.002;
                    }
                }
            }
            
            for (int j =0; j< 129; j++) {
                
                 // Simplify => always assume perfect harmonic #2 and maximum acceptance
                 doc.voltage2[j]= 1000*jump*Math.sin(doc.phase[j]*0.01745329)
                                  -doc.ampset[1][9]*Math.sin(doc.phase[j]*0.03490658);                  
                 doc.sep1[j]=kr*Math.sqrt(jump*(Math.cos(doc.phase[j]*0.01745329)+ 1.)
                             + 0.5E-3*doc.ampset[1][9]*(Math.cos(doc.phase[j]*0.03490658-Math.PI)+1.));
                 doc.sep2[j]=-doc.sep1[j];           
            }
            
            // Separatrix needs a lot of cares
             /*
            for (int j = 0; j< 51; j++) {                
                try {
                    doc.sep1[j]=kr*Math.sqrt(3.E-3*doc.ampset[0][9]*(Math.cos(doc.phase[j])
                                                + Math.cos(doc.phsset[0][9])
                                                - (doc.phase[j]+doc.phsset[0][9])*Math.sin(doc.phsset[0][9]))
                                            +1.E-3*doc.ampset[1][9]*(0.5*(Math.cos(2.*doc.phase[j] - Math.PI)
                                                + Math.cos(2.*doc.phsset[1][9]))
                                                - (doc.phsset[1][9]+doc.phase[j]-Math.PI)*Math.sin(2.*doc.phsset[1][9])));
                } catch (MathException me) {
                   ... deal with exception                  
                } finally {
                   ... many problems must be fix before using the separatrix 
                }
            }
            */
            
            doc.getMonitor().plotcurves();
            doc.getBeam().plotcurves();
            doc.errormsg("Completed pulse " + doc.pulsenumber);            
            doc.pulsenumber += 1;
            //doc.errormsg("Completed pulse " + doc.getBeam().tfpulse.getText());
        }         
    }
}

class Matrix {
    double m11;
    double m12;
    double m21;
    double m22;
    
    Vector v1 = null;
    Vector v2 = null;
    
    public Matrix() {
        m11 = 0.0;
        m12 = 0.0;
        m21 = 0.0;
        m22 = 0.0;
    }
                
    public Matrix(double a11, double a12, double a21, double a22){
        m11 = a11;
        m12 = a12;
        m21 = a21;
        m22 = a22;
    }
    
    public void setvector(double a1, double a2) {
        v1 = new Vector(a1, a2);
    }
    
    public void setvector(Vector a) {
        v1 = new Vector(a.getv1(), a.getv2());
    }
    
    public Vector getvector() {
        if (v1 != null) {
            double x;
            double y;
            x = m11*v1.getv1() + m12*v1.getv2();
            y = m21*v1.getv1() + m22*v1.getv2();
            v2 = new Vector(x,y);        
            return v2;
        }        
        return null;
    }    
}

class Vector {
    double v1;
    double v2;
    
    public Vector(double a1, double a2) {
        v1 = a1;
        v2 = a2;
    }
    
    public Vector() {
        v1 = 0.0;
        v2 = 0.0;
    }
   
    public double getv1() {
        return v1;
    }
    
    public double getv2() {
        return v2;
    }
}