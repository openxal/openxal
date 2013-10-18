/*
 * WireData.java
 *
 * Created on January 5, 2005, 10:38 AM
 */

package xal.extension.wirescan.apputils;

import java.io.*;
import java.util.*;
import java.text.*;

/**
 *
 * @author  Paul Chu
 */
public class WireData {
        String name = "";
        double[] xFit1;
        double[] yFit1;
        double[] zFit1;
        double[] xRMS1;
        double[] yRMS1;
        double[] zRMS1;
        double[] xRaw1;
        double[] yRaw1;
        double[] zRaw1;
        double[] xFitData1;
        double[] yFitData1;
        double[] zFitData1;
        double[] posRaw1;
        double[] posFit1;
        
        public WireData(String n) {
            name = n;
        }
        
        public String getName() {
            return name;
        }
        
        public void setData(double[] xFit,
                            double[] yFit,
                            double[] zFit,
                            double[] xRMS,
                            double[] yRMS,
                            double[] zRMS,
                            double[] xRaw,
                            double[] yRaw,
                            double[] zRaw,
                            double[] xFitData,
                            double[] yFitData,
                            double[] zFitData,
                            double[] posRaw,
                            double[] posFit) {
//             xFit1 = new double[xFit.length];
             xFit1 = xFit;
             yFit1 = yFit;
             zFit1 = zFit;
             xRMS1 = xRMS;
             yRMS1 = yRMS;
             zRMS1 = zRMS;
             xRaw1 = xRaw;
             yRaw1 = yRaw;
             zRaw1 = zRaw;
             xFitData1 = xFitData;
             yFitData1 = yFitData;
             zFitData1 = zFitData;
             posRaw1 = posRaw;
             posFit1 = posFit;
        }
        
	public double[] getXFit() {
		return xFit1;
	}
	
	public double[] getYFit() {
		return yFit1;
	}
	
	public double[] getZFit() {
		return zFit1;
	}
	
	public double[] getXRMS() {
		return xRMS1;
	}
	
	public double[] getYRMS() {
		return yRMS1;
	}
	
	public double[] getZRMS() {
		return zRMS1;
	}
	
	public double getXFitSigma() {
		return xFit1[3];
	}
	
	public double getYFitSigma() {
		return yFit1[3];
	} 
	
	public double getZFitSigma() {
		return zFit1[3];
	} 
	
	public double getXRMSSigma() {
		return xRMS1[3];
	}
	
	public double getYRMSSigma() {
		return yRMS1[3];
	} 
	
	public double getZRMSSigma() {
		return zRMS1[3];
	} 
	
	public double getXFitCentroid() {
		return xFit1[2];
	}
	
	public double getYFitCentroid() {
		return yFit1[2];
	} 
	
	public double getZFitCentroid() {
		return zFit1[2];
	} 
	
	public double getXRMSCentroid() {
		return xRMS1[2];
	}
	
	public double getYRMSCentroid() {
		return yRMS1[2];
	} 
	
	public double getZRMSCentroid() {
		return zRMS1[2];
	} 
	
	public double[] getXRaw() {
		return xRaw1;
	}
	
	public double[] getYRaw() {
		return yRaw1;
	} 
	
	public double[] getZRaw() {
		return zRaw1;
	} 
	
	public double[] getXFitData() {
		return xFitData1;
	}
	
	public double[] getYFitData() {
		return yFitData1;
	} 
	
	public double[] getZFitData() {
		return zFitData1;
	} 
	
	public double[] getRawPosition() {
		return posRaw1;
	}
	
	public double[] getFitPosition() {
		return posFit1;
	}
}
