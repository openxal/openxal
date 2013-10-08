package eu.ess.jels.smf.attr;

import java.util.ArrayList;
import java.util.Arrays;

import xal.smf.attr.Attribute;
import xal.smf.attr.AttributeBucket;
import xal.smf.attr.MagnetBucket;
import xal.tools.ArrayTool;


/**
 * Attribute set for additional magnet information about Fringe-Fields factors<br>
 *
 * gap - total gap of magnet (m)
 * entrK1 - Upstream edge face Fringe-field factor (default = 0.45)
 * entrK2 - Upstream edge face Fringe-field factor (default = 2.80)
 * exitK1 - Downstream edge face Fringe-field factor (default = 0.45)
 * exitK2 - Downstream edge face Fringe-field factor (default = 2.80)
 * @author  Ivo List 
 */

public class Magnet2Bucket extends MagnetBucket {
    /** ID for serializable version */
    private static final long serialVersionUID = 1L;
        
    /*
     *  Constants
     */    
    private final static String[]   c_arrNames = {  "gap",      // Total gap of magnet
                                                    "entrK1", // entry Fringe-field factor K1
                                                    "entrK2", // entry Fringe-field factor K2
                                                    "exitK1", // exit Fringe-field factor K1
                                                    "exitK2", // exit Fringe-field factor K2                                                    
                                    };
    
        
    /** Override virtual to provide type signature */
    @Override
    public String[] getAttrNames()  { 
    	String[] attrs = super.getAttrNames();
    	String[] allAttrs = new String[attrs.length + c_arrNames.length];
    	System.arraycopy(attrs, 0, allAttrs, 0, attrs.length);
    	System.arraycopy(c_arrNames, 0, allAttrs, attrs.length, c_arrNames.length);
    	return allAttrs;    	
    }
        
    public Magnet2Bucket() {
        super();
        
        m_attGap  = new Attribute(0.0);
        m_attEntrK1 = new Attribute(0.45 );
        m_attEntrK2 = new Attribute(2.8);
        m_attExitK1 = new Attribute(0.45 );
        m_attExitK2 = new Attribute(2.8);
       
        super.registerAttribute(c_arrNames[0], m_attGap);
        super.registerAttribute(c_arrNames[1], m_attEntrK1);
        super.registerAttribute(c_arrNames[2], m_attEntrK2);
        super.registerAttribute(c_arrNames[3], m_attExitK1);
        super.registerAttribute(c_arrNames[4], m_attExitK2);
    }
    
    /** total gap of magnet (m) */
    private Attribute       m_attGap;            
    /**  Upstream edge face Fringe-field factor (default = 0.45) */
    private Attribute       m_attEntrK1;           
    /** Upstream edge face Fringe-field factor (default = 2.80) */
    private Attribute       m_attEntrK2;          
    /** Downstream edge face Fringe-field factor (default = 0.45) */
    private Attribute       m_attExitK1;         
    /** Downstream edge face Fringe-field factor (default = 2.80) */
    private Attribute       m_attExitK2;


	public double getGap() {
		return m_attGap.getDouble();
	}

	public void setGap(double value) {
		m_attGap.set(value);
	}

	public double getEntrK1() {
		return m_attEntrK1.getDouble();
	}

	public void setEntrK1(double value) {
		m_attEntrK1.set(value);
	}

	public double getEntrK2() {
		return m_attEntrK2.getDouble();
	}

	public void setEntrK2(double value) {
		m_attEntrK2.set(value);
	}

	public double getExitK1() {
		return m_attExitK1.getDouble();
	}

	public void setExitK1(double value) {
		m_attExitK1.set(value);
	}

	public double getExitK2() {
		return m_attExitK2.getDouble();
	}

	public void setExitK2(double value) {
		this.m_attExitK2.set(value);
	}        
    
    
   }
