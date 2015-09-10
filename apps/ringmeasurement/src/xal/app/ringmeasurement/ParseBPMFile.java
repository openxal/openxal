/*
 * ParseWirefile.java
 *
 * Created on May 13, 2008 */

package xal.app.ringmeasurement;

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
 * Read and parse a RingBPMViewer app BPM export file, and export data as an 
 * ArrayList of HashMaps
 *
 * @author  cp3
 */
 
public class ParseBPMFile{
    
    HashMap<String, ArrayList<Double>> xdatamap = new HashMap<String, ArrayList<Double>>();
    HashMap<String, ArrayList<Double>> ydatamap = new HashMap<String, ArrayList<Double>>();
    HashMap<String, ArrayList<Double>> ampdatamap = new HashMap<String, ArrayList<Double>>();
    ArrayList<HashMap<String, ArrayList<Double>>> data = new ArrayList<HashMap<String, ArrayList<Double>>>();
    
    /** Creates new ParseWireFile */
    public ParseBPMFile() {
	
    }
    
    public ArrayList<HashMap<String, ArrayList<Double>>> parseFile(File newfile) throws IOException  {
	
	String s; 
	String firstname;
	String secondname;
	String direction;
	String header;
	String name = null;
	String[] tokens;
	boolean readingHArrays = false;
	boolean readingVArrays = false;
	boolean readingAArrays = false;

	ArrayList<Double> xturndat = new ArrayList<Double>();
	ArrayList<Double> yturndat = new ArrayList<Double>();
	ArrayList<Double> ampturndat = new ArrayList<Double>();
	int nvalues;
	
	URL url = newfile.toURI().toURL();
	
	InputStream is = url.openStream();
	InputStreamReader isr = new InputStreamReader(is);
	BufferedReader br = new BufferedReader(isr);
        s=br.readLine();
	s=br.readLine();
	s=br.readLine();
	s=br.readLine();
	s=br.readLine();
	s=br.readLine();
	s=br.readLine();
	
	while((s=br.readLine()) != null){
	    tokens = s.split("\\s+");
	    nvalues = tokens.length;
	    
	    if(nvalues < 1)  
		continue;  //Skip blank lines.
	    
	    firstname = tokens[0];
	    secondname = tokens[1];
	    
	    if(secondname.startsWith("BPM")){
		if(readingHArrays)
		    dumpxData(name, xturndat);
		else if(readingVArrays)
		    dumpyData(name, yturndat);
		else if(readingAArrays)
		    dumpampData(name, ampturndat);
	    
	    	direction = tokens[4];
		
		if(direction.equals("HORIZONTAL")){
		    readingHArrays = true;
		    readingVArrays = false;
		    readingAArrays = false;
		}
		if(direction.equals("VERTICAL")){
		    readingVArrays = true; 
		    readingHArrays = false;
		    readingAArrays = false;
		}
		if(direction.equals("AMPLITUDE")){
		    readingVArrays = false;
		    readingHArrays = false;
		    readingAArrays = true;
		}
		name = tokens[3];
		xturndat.clear(); 
		yturndat.clear();
		ampturndat.clear(); 
	    }
	    
	    if(secondname.startsWith("WAVEFORM"))
		continue;
	    
	    if (nvalues == 3){
		if(readingHArrays)
		    xturndat.add(new Double(Double.parseDouble(tokens[2])));
		else if(readingVArrays)
		    yturndat.add(new Double(Double.parseDouble(tokens[2])));
		else if(readingAArrays)
		    ampturndat.add(new Double(Double.parseDouble(tokens[2])));
            
	    }
	}
	dumpampData(name, ampturndat);
	data.add(xdatamap);
	data.add(ydatamap);
	data.add(ampdatamap);
	    
	return data;
    }	
    
    
    public void dumpxData(String name, ArrayList<Double> xturndata){
	if(xturndata.size() > 0){
	    //System.out.println("Found Horizontal for BPM " + name);
	    xdatamap.put(new String(name), new ArrayList<Double>(xturndata));
	}
    }
    public void dumpyData(String name, ArrayList<Double> yturndata){
	if(yturndata.size() > 0){
	   // System.out.println("Found Vertical for BPM " + name);
	    ydatamap.put(new String(name), new ArrayList<Double>(yturndata));
	}
    }
    public void dumpampData(String name, ArrayList<Double> ampturndata){
	if(ampturndata.size() > 0){
	   // System.out.println("Found Amp for BPM " + name);
	    ampdatamap.put(new String(name), new ArrayList<Double>(ampturndata));
	}
    }
}
 


