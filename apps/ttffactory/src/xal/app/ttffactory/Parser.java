/*
 * Parser.java - Parse transit time factor polynomial data from an xml file and crates a new xml file for loading into the optics configuration
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import xal.tools.xml.XmlDataAdaptor;
import xal.tools.xml.XmlDataAdaptor.ParseException;
import xal.tools.xml.XmlDataAdaptor.ResourceNotFoundException;
import xal.tools.ResourceManager;
import xal.tools.data.*;

/**
 * Parse transit time factor polynomial data from an xml file and creates a new xml file for 
 * loading into the optics configuration.
 * 
 * @author James Ghawaly Jr.
 * @since  Mon June 15 13:23:35 EDT 2015
 *
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
	// This method uses the XMLDataAdaptor to go through each RF gap and find the required polynomials. FOR ANDREI'S DATA ONLY (T(k))
	public void parse(File file) throws ParseException, ResourceNotFoundException, MalformedURLException{
		
		XmlDataAdaptor daptDoc = XmlDataAdaptor.adaptorForFile(file,false);
		DataAdaptor daptLinac = daptDoc.childAdaptor("SNS_Linac");
		
//		int iii = 0;
		//lstDaptSeq is a list of all the primary sequences in the linac. In this case: MEBT, DTL, CCL, and SCL
		List<DataAdaptor> lstDaptSeq = daptLinac.childAdaptors("accSeq");
		
		for (DataAdaptor daptSeq : lstDaptSeq){
			
			String primSeqID = daptSeq.stringValue("name");
			// lstDaptCav is a list of all the secondary sequences: MEBT1, MEBT2, MEBT3, MEBT4, DTL1, DTL2, etc...
			List<DataAdaptor> lstDaptCav = daptSeq.childAdaptors("cavity");
			
			for (DataAdaptor daptCav : lstDaptCav){
				
				String secSeqID = daptCav.stringValue("name");
				
				String frequency = daptCav.stringValue("frequency");
				/** lstRfGaps is a list of all the rfgaps in the given secondary sequence.*/
				List<DataAdaptor> lstRfGaps = daptCav.childAdaptors("rf_gap");
				
				for (DataAdaptor daptRF : lstRfGaps) {
					// START: The following block of code up until END parses out the the sine and cosine transit time factor polynomials and their derivatives
					String gapName = daptRF.stringValue("name");
					
					String betaMin = daptRF.stringValue("beta_min");
					String betaMax = daptRF.stringValue("beta_max");
					
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
					List<String> currentValueList = new ArrayList<>(Arrays.asList(primSeqID, secSeqID, ttfValue, ttfpValue, stfValue, stfpValue, frequency, betaMin, betaMax, null, null, null, null));

					dataTree.addListToTree(gapName, currentValueList);
//					iii++;
					
				
				}
			}
		}
		//System.out.println(iii);
		
	}
	
	/**
	 * This method uses the ResourceManager to get the URL of the the given file aFile.
	 *
	 * @param aFile the file to get the URL of
	 * @return the URL of aFile
	 */
	public URL fileURL(String aFileName){
		URL aFileURL = ResourceManager.getResourceURL(this.getClass(), aFileName); //Something like this
		//System.out.println(aFileURL);
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
	public void pack(File aFileToCreate, DataTree thisDatTree, Boolean TB) {
		XmlDataAdaptor daptWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor primary = daptWrite.createChild("xdxf");
		primary.setValue("date","02.04.2014");
		primary.setValue("system","sns");
		primary.setValue("ver","2.0.0");
		
		ArrayList<String> sequences = new ArrayList<String>();
		ArrayList<String> cavities = new ArrayList<String>();
		ArrayList<String> gaps = new ArrayList<String>();
		
		Tools tools = new Tools();
		
//		int ii = 0;
		
		// go through all the keys and values in the datatree
		Set<Entry<String, List<String>>> entrySet = thisDatTree.getEntrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			
			// get the current value from the datatree
			List<String> value = entry.getValue();
			// get the primary sequence that this key belongs too
			String localPrimary = value.get(0);
			// get the cavity that this key belongs too, and convert it into a form readable by the accelerator
			String localSecondary = tools.getSecondaryName(localPrimary,value.get(1));
			// get the name of the current gap and convert it into a form readable by the accelerator
			String localGapName = tools.getFullName(localSecondary, entry.getKey());
			if (TB) {
				localGapName = entry.getKey();
			}
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
//							ii++;
							
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
					System.out.println(localPrimary);
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
	 * Gets the data tree.
	 *
	 * @return the data tree
	 */
	public DataTree getDataTree() {
		return this.dataTree;
	}
	
	/**
	 * Creates an XML file containing the frequency, beta_min, and beta_max of every RF Gap in the Accelerator
	 *
	 * @param aFileToCreate The file to write to
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public void createBetaConfigFile(File aFileToCreate) throws IOException {
		// create an XML file writer for writing the beta and frequency information into an XML file
		XmlDataAdaptor daptWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor primary = daptWrite.createChild("xdxf");
		primary.setValue("date","02.04.2014");
		primary.setValue("system","sns");
		primary.setValue("ver","2.0.0");
		
		Tools tools = new Tools();
		
		Set<Entry<String, List<String>>> entrySet = dataTree.getEntrySet();
		for (Entry<String, List<String>> entry : entrySet) {
			
			// get the current value from the datatree
			List<String> value = entry.getValue();
			// get the primary sequence that this key belongs too
			String localPrimary = value.get(0);
			// get the cavity that this key belongs too, and convert it into a form readable by the accelerator
			String localSecondary = tools.getSecondaryName(localPrimary,value.get(1));
			// get the name of the current gap and convert it into a form readable by the accelerator
			String localGapName = tools.getFullName(localSecondary, entry.getKey());
			// get the frequency, betaMin, and betaMax of the local gap
			String localFrequency = value.get(6);
			String localBetaMin = value.get(7);
			String localBetaMax = value.get(8);
			
			DataAdaptor gap = primary.createChild(localGapName);
			gap.setValue("frequency", localFrequency);
			gap.setValue("beta_min", localBetaMin);
			gap.setValue("beta_max", localBetaMax);
			
		}

		daptWrite.writeTo(aFileToCreate);
	}
	
	/**
	 * Read beta config file.
	 *
	 * @throws ParseException the parse exception
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws MalformedURLException the malformed url exception
	 */
	public void readBetaConfigFile() throws ParseException, ResourceNotFoundException, MalformedURLException{
		
		//MAKE SURE THAT THE betas.xml FILE IS LOCATED UNDER THE RESOURCES DIRECTORY UNDER THIS APPLICATION'S DIRECTORY
		//File file = new File(System.getProperty("user.dir") + "/apps/ttffactory/resources/betas.xml");
		
		//URL fileURL = fileURL("resources/betas.xml");
		
		URL fileURL = ResourceManager.getResourceURL(Main.class, "betas.xml");

		File file = new File(fileURL.getPath());
		
		XmlDataAdaptor daptDoc = XmlDataAdaptor.adaptorForFile(file,false);
		DataAdaptor daptMain = daptDoc.childAdaptor("xdxf");
		
		for(DataAdaptor datDapt:daptMain.childAdaptors()) {
			String gapName   = datDapt.name();
			String frequency = datDapt.stringValue("frequency");
			String betaMin   = datDapt.stringValue("beta_min");
			String betaMax   = datDapt.stringValue("beta_max");
			
			List<String> currentValueList = new ArrayList<>(Arrays.asList(null,null,null,null,null,null,frequency, betaMin, betaMax,null,null,null,null));

			dataTree.addListToTree(gapName, currentValueList);
		}
		
	}
	
	/**
	 * Fix Andrei's Polynomial Names. Returns a data tree with the parsed data from Andrei's file in proper OpenXal format
	 *
	 * @param priorName the prior name
	 */
	public DataTree getFixedAndreiPolyNames() {
		DataTree fixedNameTree                    = new DataTree();
		Set<Entry<String, List<String>>> entrySet = dataTree.getEntrySet();
		
		Tools tools = new Tools();
		
		for (Entry<String, List<String>> entry : entrySet) {
			String oldName    = entry.getKey();
			List<String> data = entry.getValue();
			String newName = tools.transformAndreiName(oldName);
			
			fixedNameTree.addListToTree(newName, data);
		}
		
		return fixedNameTree;
	}
	
}