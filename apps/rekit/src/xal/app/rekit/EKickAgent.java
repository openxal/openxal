/*
 * EKickAgent.java
 *
 * Copyright (c) 2011 Oak Ridge National Laboratory
 * Oak Ridge, Tenessee 37831, U.S.A.
 * All rights reserved.
 *
 * Created on June 13, 2011
 */

package xal.app.rekit;

import xal.tools.*;
import xal.smf.*;
import xal.smf.impl.*;
import xal.smf.impl.ExtractionKicker;
import xal.ca.*;

import java.util.*;

/**
 * @author cp3, zoy
 * @author Sarah Cousineau, Taylor Patterson
 * Class for easily storing and retrieving EKicker properties.
 */

public class EKickAgent{
	
	protected AcceleratorNode EKickNode;
    protected AcceleratorSeq sequence;
	protected boolean useReadback;
	
    /* Creates a new EKickAgent */
    public EKickAgent(AcceleratorSeq aSequence, AcceleratorNode newEKickNode,
					  boolean readback) {
		
        EKickNode = newEKickNode;
        sequence = aSequence;
		useReadback = readback;
		((Electromagnet)EKickNode).setUseFieldReadback(false);

	}
    
    
    /* Name of the EKicker as given by its unique ID */
    public String name() {
        return EKickNode.getId();
    }
	
	
	/* Get method for accessing the field value of an EKicker */
	public double getValue() {
		double value = 0.0;
		try {
			if (this.useReadback) {
				value = ((Electromagnet)EKickNode).getFieldReadback();
			}
			else {
				value = ((Electromagnet)EKickNode).getField();
			}
		}
		catch (ConnectionException e) {
			System.out.println(e);
		}
		catch (GetException e) {
			System.out.println(e);
		}
		return value;
	}
	
	
	/* Set method for the field value of an EKicker */
	public void setValue(double val) {
		try {
			((Electromagnet)EKickNode).setField(val);
		}
		catch (ConnectionException e) {
			System.out.println(e);
		}
		catch (PutException e) {
			System.out.println(e);
		}
		return;
	}
	
	
	/* Get method for accessing the voltage value of an EKicker */
	public double getVoltage() {
		double value = 0.0;
		try {
			value = ((ExtractionKicker)EKickNode).getVoltageSetting();
		}
		catch (ConnectionException e) {
			System.out.println(e);
		}
		catch (GetException e) {
			System.out.println(e);
		}
		return value;
	}
	
	
	/* Check the operational status of the EKicker */
	public boolean isOkay() {
		return EKickNode.getStatus();
	}
	
	
	/* Set method for using set or readback values */
	public void setUseReadback(boolean b) {
		this.useReadback = b;
		return;
	}
	
	
	/* Get the upper limit field value of the EKicker */
	public double upperLimit(double limit) {
		double val = 0.0;
		try {
			if (this.useReadback) {
				val = (((Electromagnet)EKickNode).getFieldReadback()
					   / ((Electromagnet)EKickNode).getField()) * limit;
			}
			else {
				val = limit;
			}

		}
		catch (ConnectionException e) {
			System.out.println(e);
		}
		catch (GetException e) {
			System.out.println(e);
		}
		return val;
	}
}