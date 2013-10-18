/*
 * ExportToMatlab.java
 *
 * Created on August 5, 2004, 10:58 AM
 */

package xal.app.wirescan;

import java.io.*;
import java.util.*;
import java.text.*;

import xal.extension.wirescan.apputils.*;

/**
 *
 * @author  Paul Chu
 */
public class ExportToMatlab {
    
    boolean readingRawArrays = false;
    boolean readingFitArrays = false;
    boolean zeroData = false;
    ArrayList<xal.extension.wirescan.apputils.WireData> wires = new ArrayList<xal.extension.wirescan.apputils.WireData>();
    
    WireDataFileParser wdfp;
    
    String name = "";
    String header = "";
                
    DecimalFormat float1 = new DecimalFormat("###.####");
    DecimalFormat float2 = new DecimalFormat("#.########");
    DecimalFormat int1 = new DecimalFormat("###");
    
    /** Creates a new instance of ExportToMatlab */
    public ExportToMatlab() {
    }
       
    /**
     * read in saved wire scan data file
     */
    public void readFile(File file) {
        wdfp = new WireDataFileParser(file);
        wires = wdfp.readFile(file);
    }
        
    public void dump2Matlab(File file) {
        wdfp = new WireDataFileParser(file);
        header = wdfp.getHeader();
        
        try {
            FileWriter outFile = new FileWriter(file);
            String space = "  ";
            String comment = "%";
            String str = "";

            // write header info
            str = comment + header + "\n";
            char buffer_header[] = new char[str.length()];
            str.getChars(0, str.length(), buffer_header, 0);
            outFile.write(buffer_header);	

            char[] buffer;
            for (int i=0; i<wires.size(); i++) {
                xal.extension.wirescan.apputils.WireData ws = (wires.get(i));
                str = comment + ws.getName() + "\n";

                buffer = new char[str.length()];
                str.getChars(0, str.length(), buffer, 0);
                outFile.write(buffer);
               
                int nstepsRaw = 0;
                int nstepsFit = 0;
                try {
                    nstepsRaw = ws.getRawPosition().length;
                    nstepsFit = ws.getFitPosition().length;
                }
                catch (NullPointerException e) {
                    
                }
                
                str = int1.format(i+1) + space + int1.format(nstepsRaw) 
                    + space + int1.format(nstepsFit) + space + "0\n";
                buffer = new char[str.length()];
                str.getChars(0, str.length(), buffer, 0);
                outFile.write(buffer);

                str = "";
                for (int j=0; j<6; j++) {
                    str = str + float1.format(ws.getXFit()[j]) + space + float1.format(ws.getYFit()[j]) 
                        + space + float1.format(ws.getZFit()[j]) + space + "0\n";
                }
                buffer = new char[str.length()];
                str.getChars(0, str.length(), buffer, 0);
                outFile.write(buffer);
                
                str = "";
                for (int j=0; j<6; j++) {
                    str = str + float1.format(ws.getXRMS()[j]) + space + float1.format(ws.getYRMS()[j]) 
                        + space + float1.format(ws.getZRMS()[j]) + space + "0\n";
                }
                buffer = new char[str.length()];
                str.getChars(0, str.length(), buffer, 0);
                outFile.write(buffer);
                
                for (int j=0; j<nstepsRaw-1; j++) {
                    if(ws.getRawPosition()[j] > 0.) {
			str = float1.format(ws.getRawPosition()[j]) + space + float2.format(ws.getXRaw()[j]) 
                               + space  + float2.format(ws.getYRaw()[j])+ space 
                               + float2.format(ws.getZRaw()[j]) + "\n";
                        buffer = new char[str.length()];
                        str.getChars(0, str.length(), buffer, 0);
                        outFile.write(buffer);
                    }
                }
                
                for (int j=0; j<nstepsFit-1; j++) {
                    if(ws.getFitPosition()[j] > 0.) {
			str = float1.format(ws.getFitPosition()[j]) + space + float2.format(ws.getXFitData()[j]) 
                               + space  + float2.format(ws.getYFitData()[j])+ space 
                               + float2.format(ws.getZFitData()[j]) + "\n";
                        buffer = new char[str.length()];
                        str.getChars(0, str.length(), buffer, 0);
                        outFile.write(buffer);
                    }                    
                }
                
            }
            outFile.close();
        } catch (IOException e) {
            
        }
    }
    
    public void dumpSigmas(File file) {
        try {
            FileWriter outFile = new FileWriter(file);
            String space = "  ";
            String comment = "%";
            String str = "";
            
            // write header info
            str = comment + header + "\n";
            char buffer_header[] = new char[str.length()];
            str.getChars(0, str.length(), buffer_header, 0);
            outFile.write(buffer_header);	

            char[] buffer;
            
            for (int i=0; i<wires.size(); i++) {
               xal.extension.wirescan.apputils.WireData ws = (wires.get(i));
                str = ws.getName() + float1.format(ws.getXFitSigma()) + "\t" + float1.format(ws.getYFitSigma()) 
                    + "\t" + float1.format(ws.getZFitSigma()) + "\n";
                buffer = new char[str.length()];
                str.getChars(0, str.length(), buffer_header, 0);
                outFile.write(buffer);
            }
        } catch (IOException e) {
            
        }
        
    }
    
}
