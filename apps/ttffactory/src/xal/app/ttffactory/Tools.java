/*Tools.java Provides tools to assist the ttffactory application
 * @author James Ghawaly Jr.
 * Created on Mon June 21 14:06:57 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.io.PrintStream;
import xal.model.ModelException;
import xal.model.alg.EnvTrackerAdapt;
import xal.model.probe.EnvelopeProbe;
import xal.model.probe.traj.EnvelopeProbeState;
import xal.model.probe.traj.Trajectory;
import xal.sim.scenario.AlgorithmFactory;
import xal.sim.scenario.ProbeFactory;
import xal.sim.scenario.Scenario;
import xal.smf.Accelerator;
import xal.smf.AcceleratorSeq;
import xal.smf.data.XMLDataManager;
import xal.tools.beam.CovarianceMatrix;
import xal.tools.math.rn.Rmxn;
import xal.tools.math.rn.Rn;


/**
 * The Class ElectricFieldReader.
 */
public class Tools {

	
	/** The ostr typeout. */
	private static PrintStream        OSTR_TYPEOUT;
	
	/** The accl test. */
	private static Accelerator		  ACCL_TEST;
	
	/** The seq test. */
	private static AcceleratorSeq	  SEQ_TEST;
	
	/** The algorithm. */
	private static EnvTrackerAdapt    ALGORITHM;
	
	/**
	 * Returns the list of names attriuted to given electric field map
	 *
	 * @param priorName the name of the field's gap
	 * @return the list of names
	 */
	public String[] getCCLNameList(String priorName) {
		
		// I am sure there is a much prettier way to do this, but I was in a hurry, so this works, but is not pretty.
		
		String[] splitName = priorName.split(":");      // split the string using "-" and "." as the delimiters
		//System.out.println(Arrays.toString(splitName));
		String priorToken1 = splitName[0];
		String priorToken2 = splitName[1];
		String priorToken3 = splitName[2];
		
		// this is the cavity number of the gap
//		String cavNum = String.format("%02d", Integer.parseInt(priorToken2.replaceAll("\\D+","")));
		String gapNum = String.format("%02d", Integer.parseInt(priorToken3.replaceAll("\\D+","")));
		
		String baseName = "";
		String[] nameList = new String[8];
		
		switch(gapNum) {
		case "01": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "01";
			nameList[1] = baseName + "02";
			nameList[2] = baseName + "03";
			nameList[3] = baseName + "04";
			nameList[4] = baseName + "05";
			nameList[5] = baseName + "06";
			nameList[6] = baseName + "07";
			nameList[7] = baseName + "08";
			break;
		case "02": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "09";
			nameList[1] = baseName + "10";
			nameList[2] = baseName + "11";
			nameList[3] = baseName + "12";
			nameList[4] = baseName + "13";
			nameList[5] = baseName + "14";
			nameList[6] = baseName + "15";
			nameList[7] = baseName + "16";
		break;
		case "03":
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "17";
			nameList[1] = baseName + "18";
			nameList[2] = baseName + "19";
			nameList[3] = baseName + "20";
			nameList[4] = baseName + "21";
			nameList[5] = baseName + "22";
			nameList[6] = baseName + "23";
			nameList[7] = baseName + "24";
		 	break;
		case "04": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "25";
			nameList[1] = baseName + "26";
			nameList[2] = baseName + "27";
			nameList[3] = baseName + "28";
			nameList[4] = baseName + "29";
			nameList[5] = baseName + "30";
			nameList[6] = baseName + "31";
			nameList[7] = baseName + "32";
			break;
		case "05": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "33";
			nameList[1] = baseName + "34";
			nameList[2] = baseName + "35";
			nameList[3] = baseName + "36";
			nameList[4] = baseName + "37";
			nameList[5] = baseName + "38";
			nameList[6] = baseName + "39";
			nameList[7] = baseName + "40";
			break;
		case "06": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "41";
			nameList[1] = baseName + "42";
			nameList[2] = baseName + "43";
			nameList[3] = baseName + "44";
			nameList[4] = baseName + "45";
			nameList[5] = baseName + "46";
			nameList[6] = baseName + "47";
			nameList[7] = baseName + "48";
	 		break;
		case "07": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "49";
			nameList[1] = baseName + "50";
			nameList[2] = baseName + "51";
			nameList[3] = baseName + "52";
			nameList[4] = baseName + "53";
			nameList[5] = baseName + "54";
			nameList[6] = baseName + "55";
			nameList[7] = baseName + "56";
			break;
		case "08": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "57";
			nameList[1] = baseName + "58";
			nameList[2] = baseName + "59";
			nameList[3] = baseName + "60";
			nameList[4] = baseName + "61";
			nameList[5] = baseName + "62";
			nameList[6] = baseName + "63";
			nameList[7] = baseName + "64";
			break;
		case "09": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "65";
			nameList[1] = baseName + "66";
			nameList[2] = baseName + "67";
			nameList[3] = baseName + "68";
			nameList[4] = baseName + "69";
			nameList[5] = baseName + "70";
			nameList[6] = baseName + "71";
			nameList[7] = baseName + "72";
	 		break;
		case "10": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "73";
			nameList[1] = baseName + "74";
			nameList[2] = baseName + "75";
			nameList[3] = baseName + "76";
			nameList[4] = baseName + "77";
			nameList[5] = baseName + "78";
			nameList[6] = baseName + "79";
			nameList[7] = baseName + "80";
			break;
		case "11": 
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "81";
			nameList[1] = baseName + "82";
			nameList[2] = baseName + "83";
			nameList[3] = baseName + "84";
			nameList[4] = baseName + "85";
			nameList[5] = baseName + "86";
			nameList[6] = baseName + "87";
			nameList[7] = baseName + "88";
			break;
		case "12": ;
			baseName = priorToken1 + ":" + priorToken2 + ":" + "Rg";
			nameList[0] = baseName + "89";
			nameList[1] = baseName + "90";
			nameList[2] = baseName + "91";
			nameList[3] = baseName + "92";
			nameList[4] = baseName + "93";
			nameList[5] = baseName + "94";
			nameList[6] = baseName + "95";
			nameList[7] = baseName + "96";
	 		break;
		}
		
		return nameList;
		
	}
	
	/**
	 * Returns the list of names attriuted to given electric field map
	 *
	 * @param priorName the name of the field's gap
	 * @return the list of names
	 */
	public String[] getSCLNameList(String priorName) {
		
		// I am sure there is a much prettier way to do this, but I was in a hurry, so this works, but is not pretty.
		
		String[] splitName = priorName.split(":");      // split the string using "-" and "." as the delimiters
		//System.out.println(Arrays.toString(splitName));
		String priorToken1 = splitName[0];
		String priorToken2 = splitName[1].substring(0, splitName[1].length()-1);
		String priorToken3 = splitName[2];
		
		// this is the cavity number of the gap
		String cavNum = String.format("%02d", Integer.parseInt(priorToken2.replaceAll("\\D+","")));
//		String gapNum = String.format("%02d", Integer.parseInt(priorToken3.replaceAll("\\D+","")));
		
//		String baseName = "";
		String[] nameList = new String[4];
		
		if (getHighOrMed(cavNum) == "High") {
			nameList[0] = priorToken1 + ":" + priorToken2 + "a:" + priorToken3;
			nameList[1] = priorToken1 + ":" + priorToken2 + "b:" + priorToken3;
			nameList[2] = priorToken1 + ":" + priorToken2 + "c:" + priorToken3;
			nameList[3] = priorToken1 + ":" + priorToken2 + "d:" + priorToken3;
			
		} else {
			nameList[0] = priorToken1 + ":" + priorToken2 + "a:" + priorToken3;
			nameList[1] = priorToken1 + ":" + priorToken2 + "b:" + priorToken3;
			nameList[2] = priorToken1 + ":" + priorToken2 + "c:" + priorToken3;
			nameList[3] = null;
		}
		
		return nameList;
	}
		
		
	
	public String getCavityNumber(String priorName) {
		
		String[] splitName = priorName.split(":");      // split the string using "-" and "." as the delimiters
		//System.out.println(Arrays.toString(splitName));
		String priorToken2 = splitName[1];
		
		// this is the cavity number of the gap
		String cavNum = String.format("%02d", Integer.parseInt(priorToken2.replaceAll("\\D+","")));
		
		return cavNum;
	}
	
	/**
	 * Transform filename into OpenXAL readable name.
	 *
	 * @param priorName the prior name
	 * @return the string
	 */
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
					   sclCavityKey = "a"      ; //This will not work for generating xdxf
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
	
	/**
	 * For determining if SCL cavity number is high or medium
	 *
	 * @param cavNum the cavity number
	 * @return String "High" or "Med"
	 */
	public String getHighOrMed(String cavNum) {
		String highOrMed = "";
		if (Integer.parseInt(cavNum) > 11) {
			highOrMed = "High";
		} else {
			highOrMed = "Med";
		}
		return highOrMed;
	}
	
	/**
	 * Gets the primary name needed for OpenXAL
	 *
	 * @param preSeq the prior name
	 * @return the primary name for OpenXAL
	 */
	public String getPrimaryName(String preSeq) {
		String primaryName = null;
		
		String cavNum = getCavityNumber(preSeq);
		if(cavNum.startsWith("0")) { cavNum = cavNum.substring(1); }
		
		if      (preSeq.startsWith("MEBT"))            {primaryName = "MEBT";} //+ cavNum;} 
		else if (preSeq.startsWith("DTL"))             {primaryName = "DTL"  + cavNum;}
		else if (preSeq.startsWith("CCL"))             {primaryName = "CCL"  + cavNum;}
		else if (preSeq.startsWith("SCL"))             {primaryName = "SCL"  + getHighOrMed(cavNum);}
		
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
		String gapNum = "";
		try{
		gapNum = origName.substring(origName.lastIndexOf("g") + 1);}
		catch (NullPointerException e) {
			System.out.println(origName);
			e.printStackTrace();
		}
		
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
	
	public void envelopeComparison(String seqID) throws InstantiationException, ModelException {
		OSTR_TYPEOUT = System.out;
		OSTR_TYPEOUT.println("Launching Model RF Gap TTF tester...");
		ACCL_TEST = XMLDataManager.loadDefaultAccelerator();
		SEQ_TEST = ACCL_TEST.findSequence(seqID);
        ALGORITHM = AlgorithmFactory.createEnvTrackerAdapt(SEQ_TEST);
        
	    // Increase the number of adaptive steps for stiff systems
		ALGORITHM.setMaxIterations(30000);
		
		// Create the simulation probe
        EnvelopeProbe probe = ProbeFactory.getEnvelopeProbe(SEQ_TEST,ALGORITHM);
        probe.reset();
		
        // Create the simulation scenario, assign the probe, and run
		Scenario model = Scenario.newScenarioFor(SEQ_TEST);
		
		model.setProbe(probe);
		model.setSynchronizationMode(Scenario.SYNC_MODE_DESIGN);
		model.resync();
		model.run();

		
		// Extract the simulation data and type out
		Trajectory<EnvelopeProbeState> trjData = probe.getTrajectory();

		for ( EnvelopeProbeState state : trjData ) {
            CovarianceMatrix matCov  = state.getCovarianceMatrix();

            double          dblPos  = state.getPosition();
            double          dblSigX = matCov.getSigmaX();
            double          dblSigY = matCov.getSigmaY();
            double          dblSigZ = matCov.getSigmaZ();
			
            String      strLine = "" + dblPos + '\t' + dblSigX + '\t' + dblSigY + '\t' + dblSigZ;
            
            System.out.println(strLine);
		}
	}
	
}