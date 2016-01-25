/*ElectricFieldReader.java - Reads the electric field data on the axis of the RF gap from the electric field files.
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * ElectricFieldReader.java - Reads the electric field data on the axis of the 
 * RF gap from the electric field files.
 * 
 * @author James Ghawaly Jr.
 * @since   Mon June 15 13:23:35 EDT 2015
 */
public class ElectricFieldReader {
	
	/** The Electric field data. */
	ArrayList<Double> EData = new ArrayList<Double>();
	
	/** The Z coordinate points data. */
	ArrayList<Double> ZData = new ArrayList<Double>();
	
	/**
	 * Instantiates a new electric file reader.
	 *
	 * @param electricFilePath the path to the file containing the electric field data
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public ElectricFieldReader(String electricFilePath) throws IOException {
		Boolean startOfAxis = false;
		Boolean endOfAxis = false;
		
		BufferedReader electricFile = new BufferedReader(new FileReader(electricFilePath));
		
		while(!endOfAxis) {
			if(electricFile == null) {
				System.out.println("BARF");
			}
			String thisLine = electricFile.readLine().trim().replaceAll("\\s+"," ");
			String[] thisSplitLine = thisLine.split(" ");

			if(thisSplitLine.length>1 && thisSplitLine[1].startsWith("0.000") && thisSplitLine[0] != "END") { // GET ALL STRINGS, FOR EACH STRING, REMOVE MULT. AND TRAILING WHITESPACE, SPLIT BY WHITESPACE, AND EVALUATE IF AT END POINT
				startOfAxis = true;
				ZData.add(Double.parseDouble(thisSplitLine[0]));
				EData.add(Double.parseDouble(thisSplitLine[2]));
			}

			else if(startOfAxis) {
				endOfAxis = true;
			}
			
		}
		electricFile.close();
	}
	
	/**
	 * Gets the array containing the Z-coordinate data ArrayList<Double>.
	 *
	 * @return the dbl zpoints
	 */
	public ArrayList<Double> getDblZpoints() {
		return ZData;
	}
	
	/**
	 * Gets the array containing the electric field data ArrayList<Double>.
	 *
	 * @return the dbl e field
	 */
	public ArrayList<Double> getDblEField() {
		return EData;
	}
}