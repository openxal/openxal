/*
 * SaddamDocument.java
 *
 * Created on June 14, 2004
 */

package xal.app.saddam;

import java.util.*;
import java.net.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;
import javax.swing.event.*;
import java.awt.Toolkit;

import xal.ca.*;
import xal.extension.application.*;
import xal.extension.application.smf.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.qualify.*;
import xal.tools.data.*;

/**
 * This class contains the primary internal working objects of the 
 * settem application. E.g. which parts of the accelerator are being used.
 *
 * @author  jdg
 */
public class ChannelManager implements ConnectionListener {

    
    /** a map used to check connection statuses  */
    private Map<String, Boolean> connectionMap;
    
   /** the document this belongs to */
   protected SaddamDocument theDoc;
   
   
    /** Create a new empty document */
    public ChannelManager(SaddamDocument doc) {
	    theDoc = doc;
	    connectionMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
    }
    
    /** check connection status of a list of PVs*/
    
    protected void checkConnections(List<String> pvList) {
	    
	    connectionMap.clear();
	    	    
	    Iterator<String> itr1 = pvList.iterator();
	    while(itr1.hasNext() ) {
		    String name = itr1.next();
		    Channel chan = ChannelFactory.defaultFactory().getChannel(name);
		    connectionMap.put(name, new Boolean(false)); 
		    chan.addConnectionListener(this);
		    chan.requestConnection();
	    }
		    
	    // wait a bit to see if they connect:		    
	    int i=0;
	    int nDisconnects = connectionMap.size();
	    
	    while (nDisconnects > 0 && i < 5) {
			try {
				Thread.sleep(400);
			} catch (InterruptedException e) {
				System.out.println("Sleep interrupted during connection check");
				System.err.println( e.getMessage() );
				e.printStackTrace();
			}
			
			nDisconnects = 0;
			Collection<Boolean> vals = connectionMap.values();
			Iterator<Boolean> itr = vals.iterator();
			while (itr.hasNext()){
				Boolean tf = itr.next();
				if( !(tf.booleanValue()) ) nDisconnects++;
			}
			i++;
	    }
	    if( nDisconnects > 0) {
			Toolkit.getDefaultToolkit().beep();
			theDoc.myWindow().textArea.append((new Integer(nDisconnects)).toString() + " PVs were not able to connect");		    
			System.out.println(nDisconnects + " PVs were not able to connect");
	    }
	    else { 
			String text = "All  channesl connected";
			theDoc.myWindow().textArea.append(text);
			System.out.println(text);
	    }
    }
    
    /** Listener interface matheods*/
    
    public void connectionMade (Channel chan) {
		  String name = chan.getId();
		  connectionMap.put(name, new Boolean(true));
    }
    
    public void connectionDropped(Channel chan) { }    
}

