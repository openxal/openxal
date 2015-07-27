/*Tools.java Provides tools to assist the ttffactory application
 * @author James Ghawaly Jr.
 * Created on Mon June 21 14:06:57 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.util.Arrays;

import xal.app.ttffactory.PolynomialFitter.Polynomial;
import xal.tools.math.rn.Rmxn;
import xal.tools.math.rn.Rn;


/**
 * The Class ElectricFileReader.
 */
public class Tools {
	
	public String transformName(String priorName) {
		String newName = "";
		
		String[] splitName = priorName.split("_|\\.");      // split the string using "-" and "." as the delimiters
		System.out.println(Arrays.toString(splitName));
		String priorToken1 = splitName[0];
		String priorToken2 = splitName[1];
		//String priorToken3 = splitName[2];
		
		// this is the cavity number of the gap
		String cavNum = String.format("%02d", Integer.parseInt(priorToken1.replaceAll("\\D+","")));
		String gapNum = "";
		
		try {
			// this is the gap number of the gap
			gapNum = String.format("%02d", Integer.parseInt(priorToken2.replaceAll("\\D+","")));
			
		} catch(NumberFormatException e) {
			
			gapNum = "";
		}
		
		// this is the main ID (DTL, MEBT, etc.) of the sequence
		String mainID = "";
		if(priorToken1.startsWith("MEBT")) {
			mainID = priorToken1.substring(0,4).toUpperCase() + "_RF";
		} else {
			mainID = priorToken1.substring(0,3).toUpperCase() + "_RF";
		}
		String cavityIdentifier = "";
		String sclCavityKey = "";
		
		switch(mainID) {
		case "MEBT_RF": cavityIdentifier = "Bnch";
			break;
		case "DTL_RF": cavityIdentifier = "Cav";
			break;
		case "CCL_RF": cavityIdentifier = "Cav";
			break;
		case "SCL_RF": cavityIdentifier = "Cav";
					   sclCavityKey = "a"      ;
			break;
		default: cavityIdentifier = "";
			break;
		}
		
		newName = mainID + ":" + cavityIdentifier + cavNum + sclCavityKey + ":" + "Rg" + gapNum;
		
		return newName;
	}
	
	/**
	 * Check if the gap is an end gap
	 *
	 *@param name name of the gap
	 * @return True if gap is an end gap, else False
	 */
	public Boolean isEndGap(String name) {
		Boolean isEndGap = false;
		
		if(name.contains("END")) {
			isEndGap = true;
		}
		return isEndGap;
	}
	
	/**
	 * Matrix transpose.
	 *
	 * @param priorMat the matrix to transpose
	 * @return the the transposed matrix
	 */
	public Rmxn matrixTranspose(Rmxn priorMat) {
		int columnSize = priorMat.getColCnt();
		int rowSize = priorMat.getRowCnt();
		
		Rmxn postMat = new Rmxn(columnSize, rowSize);
		
		for(int rowIndex = 0;rowIndex<rowSize;rowIndex++) {
			for(int columnIndex = 0;columnIndex<columnSize;columnIndex++) {
				postMat.setElem(columnIndex, rowIndex, priorMat.getElem(rowIndex, columnIndex));
			}
		}
		
		return postMat;
	}
	
	/**
	 * Matrix transpose.
	 *
	 * @param priorMat the matrix to transpose
	 * @return the the transposed matrix
	 */
	public Rmxn matrixTranspose(Rn priorMat) {
		int size = priorMat.getSize();
		
		Rmxn postMat = new Rmxn(1, size);
		
		for(int rowIndex = 0;rowIndex<=size;rowIndex++) {
			postMat.setElem(rowIndex, 0, priorMat.getElem(rowIndex));
		}
		
		return postMat;
	}
	
    public String initValues(double[] data, double[] positionArray) {
    	
    	PolynomialFitter polyFitter = new PolynomialFitter(4);
    	int i = 0;
    	for(double dbl: data) {
    		polyFitter.addPoint(dbl, positionArray[i]);
    		i++;
    	}
    	
    	Polynomial init = polyFitter.getBestFit();
		return init.toString();
    	
    }
	
}