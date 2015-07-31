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

import xal.tools.math.rn.Rmxn;
import xal.tools.math.rn.Rn;


/**
 * The Class ElectricFileReader.
 */
public class Tools {
	
	public String transformName(String priorName) {
		String newName = "";
		
		String[] splitName = priorName.split("_|\\.");      // split the string using "-" and "." as the delimiters
		//System.out.println(Arrays.toString(splitName));
		String priorToken1 = splitName[0];
		String priorToken2 = splitName[1];
		
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
		
		if (name.contains("END")) { isEndGap = true; }

		return isEndGap;
	}
	
	/**
	 * Checks if given gap name is an inner gap.
	 *
	 * @param name the name of the gap
	 * @return true if is inner gap, false if outer
	 */
	public Boolean isInnerGap(String name) {
		Boolean inner = false;
		if (name.contains("INNER")) {  inner = true; }
		return inner;
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
	
	public String getPrimaryName(String preSeq) {
		String primaryName = null;
		
		if      (preSeq.startsWith("MEBT"))            {primaryName = "MEBT";} 
		else if (preSeq.startsWith("DTL"))             {primaryName = "DTL";}
		else if (preSeq.startsWith("CCL"))             {primaryName = "CCL";}
		else if (preSeq.startsWith("SCL"))             {primaryName = "SCL";}
		
		return primaryName;
	}
	
	/**
	 * This method converts the name of the secondary sequence into a usable form for the accelerator.
	 *
	 * @param primSeq the primary sequence name
	 * @param preSeq the original secondary sequence name
	 * @return the secondary sequence name readable by the accelerator
	 */
	public String getSecondaryName(String primSeq,String preSeq) {
		String postSeq = null;
		String cavNum = null; // For DTL, MEBT, and CCL, the cavity number is the last character of the string. For example: DTL4 translates to DTL_RF:Cav04
		
		cavNum = preSeq.substring(preSeq.length() - 1);
		if (primSeq.startsWith("MEBT"))                                                {postSeq = "MEBT_RF:Bnch0" + cavNum;}
		else if (primSeq.startsWith("DTL"))                                            {postSeq = "DTL_RF:Cav0" + cavNum;}
		else if (primSeq.startsWith("CCL"))                                            {postSeq = "CCL_RF:Cav0" + cavNum;}
		else if (primSeq.startsWith("SCL"))                                            {postSeq = preSeq.replace(":", "_RF:");}
		
		return postSeq;
	}

	/**
	 * This method uses the secondary sequence name and the original name to create the full name of the gap that is consistent with the accelerator's naming scheme.
	 *
	 * @param secName the secondary sequence name
	 * @param origName the original gap name
	 * @return the full new name readable by the accelerator
	 */
	public String getFullName(String secName, String origName) {
		String gapNum = origName.substring(origName.lastIndexOf("g") + 1);
		
		if (gapNum.length() == 1) {gapNum = "0" + gapNum;}
		
		return secName + ":Rg" + gapNum;
	}
	
	/**
	 * Transform andrei name to that needed for OpenXAL. Can be used for direct conversion
	 *
	 * @param preSeq the original name
	 * @return the new name
	 */
	public String transformAndreiName(String preSeq) {
		String primName = getPrimaryName(preSeq);
		String secoName = getSecondaryName(primName,preSeq);
		String wholName = getFullName(secoName,preSeq);
		
		return wholName;
	}
	
		
	/**
	 * L2 norm error. Calculate the L2 Norm error of two equally length double arrays
	 *
	 * @param vals1 the first double array
	 * @param vals2 the second double array
	 * @param delta the delta term
	 * @return the double L2 Norm error
	 */
	public double l2NormError(double[] vals1, double[] vals2, double delta) {
		double totalError = 0.0;
		for(int i = 0;i<vals1.length;i++) {
			double incrementError = Math.pow(Math.abs(vals2[i]-vals1[i]),2.0);
			totalError+=incrementError;
		}
		return Math.sqrt(totalError)*delta;
	}
	/**
	 * String array to double array.
	 *
	 * @param stringArray the string array
	 * @return the double array
	 */
	public double[] stringArrayToDoubleArray(String[] stringArray) {
		double[] doubleArray = new double[stringArray.length];
		
		for(int i=0;i<stringArray.length;i++) {
			doubleArray[i] = Double.parseDouble(stringArray[i]);
		}
		
		return doubleArray;
	}
	
}