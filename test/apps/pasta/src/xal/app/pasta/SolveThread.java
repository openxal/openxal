package xal.app.pasta;

import java.util.*;
import javax.swing.*;
import java.awt.event.*;

/**
  * A class to launch a solve pasta calculation in a seperate thread
 * @author  J. Galambos
 */

public class SolveThread implements Runnable {

    /** The pasta documentto deal with */
    private PastaDocument theDoc;
    
    private Thread thread;
    
    private int fractionComplete = 0;
    private int max;
    
    private javax.swing.Timer timer;

    public SolveThread(PastaDocument doc) {
	    theDoc = doc;
	    // pad the max time - the solver seems to use a bit more time
	    max = (int) (theDoc.analysisStuff.timeoutPeriod) + 30;
	    thread = new Thread(this, "singlePass thread");
	    thread.start();
    }


     
    public void run() {
	    
	    // for the progress bar:
	    final int delay = 2000; //milliseconds
	    fractionComplete = 0;
	    ActionListener taskPerformer = new ActionListener() {
		public void actionPerformed(ActionEvent evt) {
		     fractionComplete += delay/1000;
		     theDoc.myWindow().progressBar.setValue(fractionComplete);
		     if(fractionComplete > max) 	    theDoc.myWindow().progressBar.setIndeterminate(true);
		}
	    };
	    
	    theDoc.myWindow().progressBar.setIndeterminate(false);
	    theDoc.myWindow().progressBar.setMinimum(0);
	    theDoc.myWindow().progressBar.setMaximum(max);
	    
	    theDoc.myWindow().matchButton.setEnabled(false);
	    theDoc.myWindow().spButton.setEnabled(false);
	    theDoc.myWindow().setPntButton.setEnabled(false);
	    
	    // progress bar timer
            timer = new javax.swing.Timer(delay, taskPerformer);
	    timer.start();
	    
	    // the real calculation:
	    theDoc.analysisStuff.solve();
	    
	    theDoc.myWindow().matchButton.setEnabled(true);
	    theDoc.myWindow().spButton.setEnabled(true);	    	    
	    theDoc.myWindow().setPntButton.setEnabled(true);
	    theDoc.myWindow().progressBar.setValue(0);
	    theDoc.myWindow().progressBar.setIndeterminate(false);
	    timer.stop();
	    
    }
}
