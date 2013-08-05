/*
 * ParseWirefile.java
 *
 * Created on November 12, 2004 */

package xal.app.rtbtwizard;

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

public class ParseWireFile{
    
    public ArrayList<Object> wiredata = new ArrayList<Object>();
    
    /** Creates new ParseWireFile */
    public ParseWireFile() {
        
    }
    
    public ArrayList<Object> parseFile(File newfile) throws IOException  {
        String s;
        String firstName;
        String header;
        String name = null;
        Integer PVLoggerID = new Integer(0);
        String[] tokens;
        int nvalues = 0;
        double num1, num2, num3;
        double xoffset = 1.0;
        double xdelta = 1.0;
        double yoffset = 1.0;
        double ydelta = 1.0;
        double zoffset = 1.0;
        double zdelta = 1.0;
        boolean readfit = false;
        boolean readraw = false;
        boolean zerodata = false;
        boolean baddata = false;
        boolean harpdata = false;
        ArrayList<Double> fitparams = new ArrayList<Double>();
        ArrayList<Double> xraw = new ArrayList<Double>();
        ArrayList<Double> yraw = new ArrayList<Double>();
        ArrayList<Double> zraw = new ArrayList<Double>();
        ArrayList<Double> sraw = new ArrayList<Double>();
        ArrayList<Double> sxraw = new ArrayList<Double>();
        ArrayList<Double> syraw = new ArrayList<Double>();
        ArrayList<Double> szraw = new ArrayList<Double>();
        
        //Open the file.
        //URL url = getClass().getResource(filename);
        
        URL url = newfile.toURI().toURL();
        
        InputStream is = url.openStream();
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        
        
        while((s=br.readLine()) != null){
            tokens = s.split("\\s+");
            nvalues = tokens.length;
            firstName = tokens[0];
            if((tokens[0]).length()==0){  //Skip blank lines
                readraw = false;
                readfit = false;
                continue;
            }
            if( (nvalues == 4) && (!firstName.startsWith("---")) ){
                if( (Double.parseDouble(tokens[1]) == 0.) && (Double.parseDouble(tokens[2]) == 0.) && (Double.parseDouble(tokens[3]) == 0.) ){
                    zerodata = true;
                }
                else{
                    zerodata = false;
                }
                if( tokens[1].equals("NaN") || tokens[2].equals("NaN") || tokens[3].equals("NaN")){
                    baddata = true;
                }
                else{
                    baddata = false;
                }
            }
            if(firstName.startsWith("start")){
                header = s;
            }
            if(firstName.indexOf("WS") > 0){
                if(name != null){
                    dumpData(name, fitparams, sraw, sxraw, syraw, szraw, yraw, zraw, xraw);
                }
                name = tokens[0];
                readraw = false;
                readfit = false;
                zerodata = false;
                baddata = false;
                harpdata=false;
                fitparams.clear();
                xraw.clear();
                yraw.clear();
                zraw.clear();
                sraw.clear();
                sxraw.clear();
                syraw.clear();
                szraw.clear();
            }
            //if(firstName.startsWith("Area")) ;
            //if(firstName.startsWith("Ampl")) ;
            //if(firstName.startsWith("Mean")) ;
            if(firstName.startsWith("Sigma")){
                fitparams.add(new Double(Double.parseDouble(tokens[3]))); //xfit
                fitparams.add(new Double(Double.parseDouble(tokens[1]))); //yfit
                fitparams.add(new Double(Double.parseDouble(tokens[5]))); //zfit
            }
            //if(firstName.startsWith("Offset")) ;
            //if(firstName.startsWith("Slope")) ;
            if((firstName.equals("Position")) && ((tokens[2]).equals("Raw")) ){
                readraw = true;
                continue;
            }
            if((firstName.equals("Position")) && ((tokens[2]).equals("Fit")) ){
                readfit = true;
                continue;
            }
            if((firstName.contains("Harp"))){
                xraw.clear();
                yraw.clear();
                zraw.clear();
                sraw.clear();
                sxraw.clear();
                syraw.clear();
                szraw.clear();
                harpdata = true;
                readraw = true;
                name = tokens[0];
                continue;
            }
            if(firstName.startsWith("---")) continue ;
            
            if(harpdata==true){
                if((tokens[0]).length()!=0){  //Skip blank lines
                    if(firstName.startsWith("PVLogger")){
                        try{
                            PVLoggerID = new Integer(Integer.parseInt(tokens[2]));
                        }
                        catch(NumberFormatException e){
                        }
                    }
                    else{
                        sxraw.add(new Double(Double.parseDouble(tokens[0])));
                        xraw.add(new Double(Double.parseDouble(tokens[1])));
                        syraw.add(new Double(Double.parseDouble(tokens[2])));
                        yraw.add(new Double(Double.parseDouble(tokens[3])));
                        szraw.add(new Double(Double.parseDouble(tokens[4])));
                        zraw.add(new Double(Double.parseDouble(tokens[5])));
                        
                    }
                }
                continue;
            }
            
            if(readraw && (!zerodata) && (!baddata) ){
                sraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
                sxraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
                syraw.add(new Double(Double.parseDouble(tokens[0])/Math.sqrt(2.0)));
                szraw.add(new Double(Double.parseDouble(tokens[0])));
                yraw.add(new Double(Double.parseDouble(tokens[1])));
                zraw.add(new Double(Double.parseDouble(tokens[2])));
                xraw.add(new Double(Double.parseDouble(tokens[3])));
            }
            if(firstName.startsWith("PVLogger")){
                try{
                    PVLoggerID = new Integer(Integer.parseInt(tokens[2]));
                }
                catch(NumberFormatException e){
                }
            }
            
            
        }
        dumpData(name, fitparams, sraw, sxraw, syraw, szraw, yraw, zraw, xraw);
        //writeData();
        wiredata.add(PVLoggerID);
        return wiredata;
    }
    
    private void dumpData(String label, ArrayList<Double> fitparams, ArrayList<Double> sraw, ArrayList<Double> sxraw, ArrayList<Double> syraw, ArrayList<Double> szraw, ArrayList<Double> yraw, ArrayList<Double> zraw, ArrayList<Double> xraw){
        HashMap<String, Object> data = new HashMap<String, Object>();
        
        data.put("name", label);
        data.put("fitparams", new ArrayList<Double>(fitparams));
        data.put("sdata", new ArrayList<Double>(sraw));
        data.put("sxdata", new ArrayList<Double>(sxraw));
        data.put("sydata", new ArrayList<Double>(syraw));
        data.put("szdata", new ArrayList<Double>(szraw));
        data.put("xdata", new ArrayList<Double>(xraw));
        data.put("ydata", new ArrayList<Double>(yraw));
        data.put("zdata", new ArrayList<Double>(zraw));
        
        wiredata.add((HashMap)data);
        
    }
    
    
}



