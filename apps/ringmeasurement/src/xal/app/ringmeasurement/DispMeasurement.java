/*
 * Created on Mar 4, 2005
 *
 * Copyright (c) 2001-2005 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 * 
 */
package xal.app.ringmeasurement;

import java.util.*;

import xal.smf.impl.BPM;
import xal.tools.fit.lsm.*;

/**
 * For dispersion measurement at a BPM location.  This class takes an array of 
 * BPM measurements and perform line fit to get dispersion at this BPM location.
 * 
 * @author chu
 */
public class DispMeasurement {
	
	List<BPM> myBPMs;
	double disp[];
	double[][] data;
	double[][] eng;
	final int no_pts = 5;
	
	public DispMeasurement(List<BPM> bpms) {
		myBPMs = bpms;
	}
	
	public void setBPMDataArray(double[][] data) {
		this.data = data;
	}
	
	public void setBPMDataArray(double[] data) {
		this.data = new double[1][];
		this.data[0] = data;
	}
	
	public void setEnergyDataArray(double[][] data) {
		this.eng = data;
	}
	
	public void setEnergyDataArray(double[] data) {
		this.eng = new double[1][];
		this.eng[0] = data;
	}
	
	public double[] getDispersions() {
		// fit BPMs one-by-one
			for (int i=0; i<data.length; i++) {
				Polynomial p = new Polynomial(1);
				p.setData(eng[i], data[i]);
				p.setParameter(0, 0.);
				p.setParameter(1, 0.);
				p.fit();
				
				disp[i] = p.getParameter(1);
			}
		return disp;
	}
	
	public double getDispersion() {
	    return disp[0];
	}
	
}
