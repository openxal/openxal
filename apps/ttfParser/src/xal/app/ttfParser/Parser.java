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
import java.net.MalformedURLException;
import java.net.URL;
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

public class Parser {
	String status;
	DataTree dataTree = new DataTree();
	
	// This method uses the XMLDataAdaptor to go trhough each RF gap and fidn the required polynomials
	public void parse(File file) throws ParseException, ResourceNotFoundException, MalformedURLException{
		
		XmlDataAdaptor daptDoc = XmlDataAdaptor.adaptorForFile(file,false);
		DataAdaptor daptLinac = daptDoc.childAdaptor("SNS_Linac");
		
		//lstDaptSeq is a list of all the primary sequences in the linac. In this case: MEBT, DTL, CCL, and SCL
		List<DataAdaptor> lstDaptSeq = daptLinac.childAdaptors("accSeq");
		
		for (DataAdaptor daptSeq : lstDaptSeq){
			
			String primSeqID = daptSeq.stringValue("name");
			// lstDaptCav is a list of all the secondary sequences: MEBT1, MEBT2, MEBT3, MEBT4, DTL1, DTL2, etc...
			List<DataAdaptor> lstDaptCav = daptSeq.childAdaptors("cavity");
			
			for (DataAdaptor daptCav : lstDaptCav){
				
				String secSeqID = daptCav.stringValue("name");
				// lstRfGaps is a list of all the rfgaps in the given secondary sequence.
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
					
				
				}
			}
		}
	}
	
	// This method uses the ResourceManager to get the URL of the the given file aFile
	public URL fileURL(String aFile){
		URL aFileURL = ResourceManager.getResourceURL(this.getClass(), aFile); //Something like this
		
		// if the given file is not found by the resource manager, then print a warning (FUTURE: add GUI warning dialog
		if (aFileURL == null) {
			System.out.println("FAILED");
		}
		return aFileURL;
	}
	
	// This method is used to create a new xdxf configuration file and pack it with the transit time factor polynomials
	public void pack(String aFileToCreate) {
		XmlDataAdaptor daptWrite = XmlDataAdaptor.newEmptyDocumentAdaptor();
		DataAdaptor primary = daptWrite.createChild("xdxf");
		primary.setValue("date","02.04.2014");
		primary.setValue("system","sns");
		primary.setValue("ver","2.0.0");
		
		for (Entry<String, List<String>> entry : dataTree.map.entrySet()) {
			String localGapName = entry.getKey();
			List<String> value = entry.getValue();
			String localPrimary = value.get(0);
			String localSecondary = value.get(1);
			if (primary.hasAttribute("sequence")){
				
			}
		}
	}
	
	// This method returns the list of gaps
	public ArrayList<String> getGapList() {
		return dataTree.getGaps();
	}
	
	// This method retrieves a specific value from a specific RF gap
	public String getValue(String gapId, String valueId) {
		return dataTree.getValue(gapId, valueId);
	}
	
	// This method converts the name of the secondary sequence into a usable form for the accelerator (Does not work for SCL)
	public String getSecondaryName(String primSeq,String preSeq) {
		String postSeq = null;
		String cavNum = preSeq.substring(preSeq.lastIndexOf(primSeq) + 1);
		if (primSeq == "MEBT") {
			postSeq = primSeq + "_RF:Bnch0" + cavNum;
		}
		else if (primSeq == "DTL") {
			postSeq = primSeq + "_RF:Cav0" + cavNum;
		}
		else if (primSeq == "CCL") {
			postSeq = primSeq + "_RF:Cav0" + cavNum;
		}
		else {
			postSeq = "INVALID SEQUENCE FOR FUNCTION";
		}
		return postSeq;
	}
	
	// This method uses the secondary sequence name and the original name to create the full name of the gap that is consistent with the accelerator's naming scheme
	public String getFullName(String secName, String origName) {
		String postName = null;
		String gapNum = origName.substring(origName.lastIndexOf("Rg") + 1);
		if (gapNum.length() == 1) {
			gapNum = "0" + gapNum;
		}
		return secName + ":Rg" + gapNum;
	}
	
	public static void main(String[] args){
		
		}
	
}