package xal.app.pasta;

import java.util.*;
import javax.swing.*;

/**
 * A class to launch a single pass pasta calculation in a seperate thread
 * @author  J. Galambos
 */

public class CalcThread implements Runnable {

    /** The pasta documentto deal with */
    private PastaDocument theDoc;
    
    private Thread thread;

    public CalcThread(PastaDocument doc) {
	    theDoc = doc;
	    thread = new Thread(this, "singlePass thread");
	    thread.start();
    }

    public void run() {
	    theDoc.myWindow().progressBar.setIndeterminate(true);
	    theDoc.myWindow().matchButton.setEnabled(false);
	    theDoc.myWindow().spButton.setEnabled(false);
	    theDoc.myWindow().setPntButton.setEnabled(false);
	    
	    // the real calculation:
	    theDoc.analysisStuff.doCalc();
	    theDoc.analysisStuff.plotUpdate();
	    
	    theDoc.myWindow().matchButton.setEnabled(true);
	    theDoc.myWindow().spButton.setEnabled(true);	    	    
	    theDoc.myWindow().progressBar.setIndeterminate(false);
	    theDoc.myWindow().spButton.setEnabled(true);
	    theDoc.myWindow().setPntButton.setEnabled(true);	    
    }
}
