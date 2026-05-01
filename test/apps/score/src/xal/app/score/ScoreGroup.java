/*
 * ScoreGroup.java
 *
 * Created on Mon Dec 08 11:39:25 EST 2003
 *
 * Copyright (c) 2003 Spallation Neutron Source
 * Oak Ridge National Laboratory
 * Oak Ridge, TN 37830
 */

package xal.app.score;

import java.util.*;

import xal.tools.data.DataTable;
/**
 * ScoreGroup is a container for the lists of PVs a Score set is comprised of.
 * This is essentially a set of ScoreRows that do not yet contain any PV values. 
 * It is used as a template of PV names etc., to start creating snapshots with.
 * It is similar to a ScoreSnapshot object, but has no timestamp nor values.
 *
 * @author  jdg
 */
public class ScoreGroup {
	
	/** label of the score group */
	protected final String LABEL;
	
	/** description of the score group */
	protected final String DESCRIPTION;
		
	/** the  score rows in this group */
	private ScoreRow[] _rows;
	
	/**
	 * Constructor
	 * @param label The group's label
	 * @param description A description of the group
	 * @param rows The list of ScoreRows in this group
	 */
	public ScoreGroup(String label, String description, ScoreRow[] rows) {
		LABEL = label;
		DESCRIPTION = description;
		_rows = rows;
	}
	public ScoreGroup(String label, ScoreRow[] rows) {
		this(label, "None", rows);
	}	
	
	/**
	 * Get the group label.
	 * @return the group label.
	 */
	public String getLabel() {
		return LABEL;
	}
	
	/**
	 * Get a description of the group
	 * @return a description of this group>
	 */
	public String getDescription() {
		return DESCRIPTION;
	}	
	
	/**
	 * Create a populated PVData set structure from this info
	 * Since there is no saved values for this group template, NaNs are inserted.
	 */
	protected PVData createPVData() {
		PVData pvd = new PVData(getLabel());
		String spName, rbName;
		 for (int i=0; i< _rows.length; i++){
			 String sys = (_rows[i]).getSystem();
			 String subSys = (_rows[i]).getSubSystem();
			 spName = (_rows[i].getSPName() == null) ? "null" : _rows[i].getSPName();
			 rbName = (_rows[i].getRBName() == null) ? "null" : _rows[i].getRBName();
			 final DataTypeAdaptor dataTypeAdaptor = _rows[i].getDataTypeAdaptor();
			 final String spVal  = null;
			 final String rbVal  = null;
			 boolean urb = (_rows[i]).useRB();
			 pvd.addRecord(sys, subSys, dataTypeAdaptor, spName , spVal, rbName,  rbVal, urb);
		 }
		 return pvd;
	}
	
	/** return the list of scoreRows this group is comprosed of */
	protected ScoreRow[] getScoreRows() {
		return _rows;
	}
}

