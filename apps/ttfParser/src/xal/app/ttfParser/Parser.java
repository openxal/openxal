/*
 * Parser.java - Parse transit time factor polynomial data from an xml file and crates a new xml file for loading into the optics configuration
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttfParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;
import xal.tools.ResourceManager;
import xal.tools.data.*;

/**
 * The Class Parser.
 */
public class Parser {
	
	/** The status of the parser (Not currently implemented). */
	String status;
	
	/** A custom data format, which uses a map to stores a list of data for a specific key. */
	DataTree dataTree = new DataTree();
	
	/** This map stores the history of what and what has not already been packed in pack(). */
	static Map<String,DataAdaptor> memoryMap = new HashMap<String, DataAdaptor>();
	
	/**
	 * Parses the file : file, and stores the results into a data tree. see DataTree.java
	 *
	 * @param file The file to parse
	 * @throws ParseException the parse exception
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws MalformedURLException the malformed url exception
	 */
	// This method uses the XMLDataAdaptor to go trhough each RF gap and fidn the required polynomials
	public void parse(File file) throws ParseException, ResourceNotFoundException, MalformedURLException{
		
		XmlDataAdaptor daptDoc = XmlDataAdaptor.adaptorForFile(file,false);
		DataAdaptor daptLinac = daptDoc.childAdaptor("SNS_Linac");
		
		int iii = 0;
		//lstDaptSeq is a list of all the primary sequences in the linac. In this case: MEBT, DTL, CCL, and SCL
		List<DataAdaptor> lstDaptSeq = daptLinac.childAdaptors("accSeq");
		
		for (DataAdaptor daptSeq : lstDaptSeq){
			
			String primSeqID = daptSeq.stringValue("name");
			// lstDaptCav is a list of all the secondary sequences: MEBT1, MEBT2, MEBT3, MEBT4, DTL1, DTL2, etc...
			List<DataAdaptor> lstDaptCav = daptSeq.childAdaptors("cavity");
			
			for (DataAdaptor daptCav : lstDaptCav){
				
				String secSeqID = daptCav.stringValue("name");
				
				/** lstRfGaps is a list of all the rfgaps in the given secondary sequence.*/
				List<DataAdaptor> lstRfGaps = daptCav.childAdaptors("rf_gap");
				
				for (DataAdaptor daptRF : lstRfGaps) {
					// START: The following block of code up until END parses out the the sine and cosine transit time factor polynomials and their derivatives
					String gapName = daptRF.stringValue("name");
					
					DataAdaptor daptTTF = daptRF.childAdaptor("polyT");
					DataAdaptor coeffTTF = daptTTF.childAdaptor("coeff");
					String ttfValue = coeffTTF.stringValue("arr");
					
					DataAdaptor daptSTF = daptRF.childAdaptor("polyS");
					DataAdaptor coeffSTF = daptSTF.childAdaptor("coeff");
					String stfValue = coeffSTF.stringValue("arr");
					
					DataAdaptor daptTTFP = daptRF.childAdaptor("polyTP");
					DataAdaptor coeffTTFP = daptTTFP.childAdaptor("coeff");
					String ttfpValue = coeffTTFP.stringValue("arr");
					
					DataAdaptor daptSTFP = daptRF.childAdaptor("polySP");
					DataAdaptor coeffSTFP = daptSTFP.childAdaptor("coeff");
					String stfpValue = coeffSTFP.stringValue("arr");
					// END
					// Here, we create a list of all the data, and uses DataTree to attach this data to a given RF gap
					List<String> currentValueList = new ArrayList<>(Arrays.asList(primSeqID, secSeqID, ttfValue, ttfpValue, stfValue, stfpValue));

					dataTree.addListToTree(gapName, currentValueList);
					iii++;
					
				
				}
			}
		}
		System.out.println(iii);
		
	}
	
	/**
	 * This method uses the ResourceManager to get the URL of the the given file aFile.
	 *
	 * @param aFile the file to get the URL of
	 * @return the URL of aFile
	 */
	public URL fileURL(String aFile){
		URL aFileURL = ResourceManager.getResourceURL(this.getClass(), aFile); //Something like this
		
		// if the given file is not found by the resource manager, then print a warning (FUTURE: add GUI warning dialog
		if (aFileURL == null) {
			System.out.println("FAILED");
		}
		return aFileURL;
	}
	
	/**
	 * This method is used to create a new xdxf configuration file and pack it with the transit time factor polynomials that were parsed from file.parse()
	 *
	 * @param aFileToCreate the name of the file to create
	 */
	public void pack(File aFileToCreate) {
		XmlDataAdaptor daptWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor primary = daptWrite.createChild("xdxf");
		primary.setValue("date","02.04.2014");
		primary.setValue("system","sns");
		primary.setValue("ver","2.0.0");
		
		ArrayList<String> sequences = new ArrayList();
		ArrayList<String> cavities = new ArrayList();
		ArrayList<String> gaps = new ArrayList();
		
		int ii = 0;
		
		// go through all the keys and values in the datatree
		for (Entry<String, List<String>> entry : dataTree.map.entrySet()) {
			
			// get the current value from the datatree
			List<String> value = entry.getValue();
			// get the primary sequence that this key belongs too
			String localPrimary = value.get(0);
			// get the cavity that this key belongs too, and convert it into a form readable by the accelerator
			String localSecondary = getSecondaryName(localPrimary,value.get(1));
			// get the name of the current gap and convert it into a form readable by the accelerator
			String localGapName = getFullName(localSecondary, entry.getKey());
			// get the ttf, stf, ttfp, and stfp of the current gap
			String localTTF = value.get(2);
			String localTTFP = value.get(3);
			String localSTF = value.get(4);
			String localSTFP = value.get(5);
			
			DataAdaptor filePrimary = null;
			DataAdaptor fileCavity = null;
			DataAdaptor fileGap = null;
			
			Boolean done = false;
			// while the program is not done (done = false), continue looping
			while (!done){
				//System.out.println("Currently Analyzing: "+localGapName);
				if (sequences.contains(localPrimary)){ //check if this primary sequence has already been made
					
					filePrimary = memoryMap.get(localPrimary);
					if (cavities.contains(localSecondary)){ //check if this cavity has already been made
						
						fileCavity = memoryMap.get(localSecondary);
						
						if (gaps.contains(localGapName)){ //check if this gap has already been made, if it has, there is a problem
							
						}
						else { //create a new gap, special handling is required for MEBT and SCL
							if (!localPrimary.startsWith("SCL") || !localPrimary.startsWith("MEBT")){
								fileGap = filePrimary.createChild("node");
							}
							else{
								fileGap = fileCavity.createChild("node");
							}
							fileGap.setValue("id", localGapName);
							gaps.add(localGapName);
							
							DataAdaptor att = fileGap.createChild("attributes");
							DataAdaptor dataPlace = att.createChild("rfgap");
							// set the values of the information, trim off the leading and trailing whitespace and replace all of the remaining whitespace with commas
							dataPlace.setValue("ttfCoeffs",localTTF.trim().replaceAll("\\s+", ","));
							dataPlace.setValue("ttfpCoeffs",localTTFP.trim().replaceAll("\\s+", ","));
							dataPlace.setValue("stfCoeffs",localSTF.trim().replaceAll("\\s+", ","));
							dataPlace.setValue("stfpCoeffs",localSTFP.trim().replaceAll("\\s+", ","));
							
							addKeyToMap(localGapName, fileGap);
							done = true;
							ii++;
							
							System.out.println("Finished Analyzing: " + localGapName + " Current Gap Count: " + ii);
							
						}
					}
					else{ //create a new cavity sequence
						if (!localPrimary.startsWith("SCL") || !localPrimary.startsWith("MEBT")){
							cavities.add(localSecondary);
						}
						else{
							fileCavity = filePrimary.createChild("sequence");
							fileCavity.setValue("id", localSecondary);
							cavities.add(localSecondary);
							addKeyToMap(localSecondary, fileCavity);
						}
						
					}
					
				}
				else { //create a new primary sequence
					
					filePrimary = primary.createChild("sequence");
					filePrimary.setValue("id", localPrimary);
					sequences.add(localPrimary);
					addKeyToMap(localPrimary, filePrimary);
					
				}
			}
		}
		
		try {
			daptWrite.writeTo(aFileToCreate);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	/**
	 * This method returns the list of gaps.
	 *
	 * @return the list of RF gaps in the parsed accelerator
	 */
	public ArrayList<String> getGapList() {
		return dataTree.getGaps();
	}
	
	/**
	 * This method adds the name of the data adaptor key paired with its data adaptor to the map.
	 *
	 * @param keyValue the key value (name of data adaptor)
	 * @param datAdapt the actual data adaptor
	 */
	public void addKeyToMap(String keyValue, DataAdaptor datAdapt){
		memoryMap.put(keyValue, datAdapt);
	}
	
	/**
	 * This method retrieves a specific value from a specific RF gap.
	 *
	 * @param gapId the gap id
	 * @param valueId the value id, can be ttf, stf, ttfp, or stfp
	 * @return the requested value
	 */
	public String getValue(String gapId, String valueId) {
		return dataTree.getValue(gapId, valueId);
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
		String cavNum = null; // For DTL, MEBT, and CCL, the cavity number is the last character of the string. For example: DTL4 translates to DTL:Cav04
		
		cavNum = preSeq.substring(preSeq.length() - 1);
		if (primSeq.startsWith("MEBT")) {
			postSeq = "MEBT_RF:Bnch0" + cavNum;
		}
		else if (primSeq.startsWith("DTL")) {
			postSeq = "DTL_RF:Cav0" + cavNum;
		}
		else if (primSeq.startsWith("CCL")) {
			postSeq = "CCL_RF:Cav0" + cavNum;
		}
		else if ((primSeq.startsWith("SCLHigh")) || (primSeq.startsWith("SCLMed"))){
			postSeq = preSeq.replace(":", "_RF:");
		}
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
		String postName = null;
		String gapNum = origName.substring(origName.lastIndexOf("g") + 1);
		if (gapNum.length() == 1) {
			gapNum = "0" + gapNum;
		}
		return secName + ":Rg" + gapNum;
	}
	
	/**
	 * The main method.
	 *
	 * @param args There are no methods
	 */
	public static void main(String[] args){
		// nothing to do here
		}
	
}