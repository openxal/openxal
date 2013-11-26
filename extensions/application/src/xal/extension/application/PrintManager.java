/*
 * PrintManager.java
 *
 * Created on March 25, 2003, 3:42 PM
 */

package xal.extension.application;

import java.awt.print.*;
import javax.swing.JOptionPane;
import java.util.logging.*;


/**
 * Manage document printing.
 *
 * @author  tap
 */
class PrintManager {
    //- static variables  ------------------------------------------------------
    static protected PrintManager defaultManager;
    
    //- instance variables -----------------------------------------------------
    protected PageFormat pageFormat;
    
    
    /** static constructor */
    static {
        defaultManager = new PrintManager();
    }
    
    
    /** Creates a new instance of PrintManager */
    public PrintManager() {
        pageFormat = new PageFormat();
    }
    
    
    /**
     * Get the default print manager.  The print manager is shared by the application.
     * @return The default print manager instance.
     */
    static public PrintManager defaultManager() {
        return defaultManager;
    }
    
    
    /**
     * Get the page format set by the user or the default one if none has been set.
     * @return The page format to use for printing.
     */
    public PageFormat getPageFormat() {
        return pageFormat;
    }
    
    
    /**
     * Show the PageSetup dialog so the user can set the PageFormat.
     */
    public void pageSetup() {
        pageFormat = PrinterJob.getPrinterJob().pageDialog(pageFormat);
    }
    
    
    /**
     * Print a document.
     * @param document The document to print.
     */
    public void print( final XalAbstractDocument document ) {        
		try {
			PrinterJob printJob = PrinterJob.getPrinterJob();
			printJob.setPageable( document );
			if ( printJob.printDialog() ) {
                printJob.print();
            }
        }
		catch( Exception exception ) {
			System.err.println( exception );
			document.displayError( "Print error", "Print Exception...", exception );
			Logger.getLogger("global").log( Level.WARNING, "Print error.", exception );
		}
    }
}
