/*
 * DataTree.java - Creates a data structure for organizing and accessing information about a specified RF gap
 * @author James Ghawaly Jr.
 * Created on Mon June 15 13:23:35 EDT 2015
 *
 * Copyright (c) 2015 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.ttffactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * The Class DataTree.
 */
public class DataTree {
	
	/** Create a new map */
	final Map<String,List<String>> map = new HashMap<String, List<String>>();
	
	/**
	 * This method adds a field to the current map, which matches the gap_id key to the values pertaining to that key
	 *
	 * @param gapID the gap id
	 * @param lstStr a list of string values mapped to the gap ID
	 */
	// This method adds a field to the current map, which matches the gap_id key to the values pertaining to that key
	public void addListToTree(String gapID,List<String> lstStr){
		map.put(gapID,lstStr);
	}
	
	/**
	 * This method is used to get a certain value for a certain gap
	 *
	 * @param key the key of which the data value belongs to
	 * @param valueIdentifier the value identifier, can be ttf, stf, ttfp, or stfp
	 * @return the value
	 */
	// Example: To get the ttf polynomial of MEBT3 RF Gap 1, use dataTree.getValue("MEBT3:Rg1","ttf")
	public String getValue(String key,String valueIdentifier) throws ArrayIndexOutOfBoundsException {
		int index = 0;
		List<String> values;
		String value;
		// This switch statement returns the index of the information to fetch
		switch (valueIdentifier) {
			case "primary_sequence": index = 0;
				break;
			case "secondary_sequence": index = 1;
				break;
			case "ttf": index = 2;
				break;
			case "ttfp": index = 3;
				break;
			case "stf": index = 4;
				break;
			case "stfp": index = 5;
				break;
			case "frequency": index = 6;
				break;
			case "beta_min": index = 7;
				break;
			case "beta_max": index = 8;
				break;
			case "ttf_string": index = 9;
				break;
			case "stf_string": index = 10;
				break;
			case "ttfp_string": index = 11;
				break;
			case "stfp_string": index = 12;
				break;
			default: index = -10;
				break;
		}
		// return the value list pertaining to the key
		values = map.get(key);
		// return the individual value pertaining to the valueIdentifier
		value = values.get(index);

		return value;
	}
	
	/**
	 * Return an array list of gaps
	 *
	 * @return the array list of gaps
	 */
	public ArrayList<String> getGaps() {
		ArrayList<String> keyList = new ArrayList<String>();
		Set<String> keys = map.keySet();
		
		//create an iterator to go through each entry of the data tree
		for (Iterator<String> i = keys.iterator(); i.hasNext();){
			String key = i.next().toString();
			keyList.add(key);
		}
		return keyList;
				
	}
	
	/**
	 * Get the size of the DataTree
	 *
	 * @return the number of entries in the DataTree
	 */
	public int size() {
		return map.size();
	}
	
	/**
	 * Gets the map.
	 *
	 * @return the map
	 */
	public Map<String, List<String>> getMap() {
		return this.map;
	}
	
	/**
	 * Gets the entry set.
	 *
	 * @return the entry set
	 */
	public Set<Entry<String,List<String>>> getEntrySet() {
		return map.entrySet();
	}
	
	public Entry<String,List<String>> getFirstEntry() {
		
		Set<Entry<String,List<String>>> curEntSet = getEntrySet();
		Entry<String,List<String>> curEnt = null;
		int i = 0;
		for (Entry<String,List<String>> entry:curEntSet) {
			if(i>0) {
				break;
			}
			curEnt = entry;
			i++;
		}
		
		return curEnt;
	}

	
}