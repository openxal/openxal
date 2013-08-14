/*
 * ParseWirefile.java
 *
 * Created on November 12, 2004 */

package xal.app.escap;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.ca.*;
import xal.tools.messaging.*;
import xal.tools.statistics.*;
import java.io.*;
import java.net.URL;
import java.util.*;

/**
 * Read and parse a wirescan file, extract data and send out as HashMap.
 *
 * @author  cp3
 */
 
public class ParseScannerFile{
    
    public ArrayList<ArrayList<Double>> data = new ArrayList<ArrayList<Double>>();
    
    /** Creates new ParseWireFile */
    public ParseScannerFile() {
	
    }
    
    public ArrayList<ArrayList<Double>> parseFile(File newfile) throws IOException  {
	String s;
	String[] tokens;
	
	URL url = newfile.toURI().toURL();
	
	InputStream is = url.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
        
	///s=br.readLine();
	while((s=br.readLine()) != null){
	    tokens = s.split("\\s+");
	    int nvalues = tokens.length;
	    ArrayList<Double> columndata = new ArrayList<Double>();
	    for(int i = 0; i< nvalues; i++){
		if(tokens[i].length()>0){
		    columndata.add(new Double(Double.parseDouble(tokens[i])));
		}	
	    }
	    data.add(columndata);
	  
	}
	System.out.println("Matrix size is " + data.size() + " by " + ((ArrayList)data.get(0)).size());
	return data;
 }

}
 


