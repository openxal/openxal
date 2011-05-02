/*
 * GenerationRule.java
 *
 * Created on October 3, 2002, 12:01 PM
 */

package xal.sim.latgen;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import xal.smf.AcceleratorNode;





/**
 *
 * @author  CKAllen
 * 
 * @deprecated THis is left over from the first attempt - we are redesign using dynamic
 * configuration.
 */
@Deprecated
public abstract class GenerationRule implements IRule {
    
    /** List of node types for generation rule */
    private HashSet  m_setTypes;

    
    abstract public List getElements(Set setNodeCombin) throws GenerationException;

    /** 
     *  Creates a new instance of GenerationRule 
     *
     *  @param  arrTypes    string array of node types
     */
    protected GenerationRule(String[] arrTypes) {
        m_setTypes = new HashSet();

        for (int i=0; i<arrTypes.length; i++)  
            m_setTypes.add(arrTypes[i]);
    }
    
    
    public Set getNodeCombination() {
        return m_setTypes;
    };
};




/**
 *  Implements rules and transfer matrix generation for horizontal quadrupoles.
 */

 class QHRule extends GenerationRule {
    
     
    /** target node type string */
    public static String[] s_arrTypeStrings = {"QH"};
    
    
    /** Default number of slice to decompose a quadrupole */
    public static final int     s_cntMagSlices = 10;
    
    
    /** number of slice to decompose quadrupole */
    private int                 m_cntSlices;
    
    /** source accelerator node */
    private AcceleratorNode     m_node;
    

    public QHRule()                 { this(s_cntMagSlices); };
    public QHRule(int cntSlices)    { super(s_arrTypeStrings); m_cntSlices = cntSlices; };
    
    
    
    public List getElements(Set setNodeCombin) throws GenerationException {
        LinkedList lstElems = new LinkedList();
        
        return lstElems;
    };
    
    
//    /**
//     *  (Re)build the transfer matrix for an Element object using current data or
//     *  configuration of the AcceleratorNode object.
//     */
//    public Matrix buildTransferMatrix() {
//        Matrix Phi = new Matrix(7,7, 0.0);
//
//        /*
//	Quadrupole quad = (Quadrupole)(m_node);
//
//	MagnetBucket mpb = null;
//	double field;
//
//		try {
//		    field = theQuad.getDesignField();
//		    double l = theQuad.getEffLength();
//		} catch (Exception e) {
//		    System.out.println("Electric Field on " 
//				       + theQuad.getId() 
//				       + " not set, for stability sake,"
//				       + "returning identity matrix");
//		    return Matrix.identity(6,6);
//		}
//	    }
//	
//	boolean negFlag = false;
//	if(field < 0)
//	    negFlag = true;
//	
//	field = Math.abs(field);
//	
//	double k = Math.sqrt( (E.getSpeedOfLight() * field) 
//			      / (E.getParticleRestEnergy() 
//				 * E.getBetaGamma()) );
//	
//	double[][] X = { {Math.cos( k * deltaL ),          
//			  Math.sin( k * deltaL ) / k},
//			 {Math.sin( k * deltaL ) * k * -1, 
//			  Math.cos( k * deltaL ) } };
//	
//	double[][] Y = { {cosh( k * deltaL ),     sinh( k * deltaL ) / k},
//			 {sinh( k * deltaL ) * k, cosh( k * deltaL ) } };
//	
//	double[][] Z = { {1, deltaL}, 
//			 {0, 1} }; 
//
//	Matrix newMap = new Matrix(6, 6, 0);
//	
//	if(negFlag == false)
//	    for (int y = 0; y < 2; y++)
//		for(int x = 0; x < 2; x++)
//		    {
//			newMap.set(x, y, X[x][y]);
//			newMap.set(x+2, y+2, Y[x][y]);
//			newMap.set(x+4, y+4, Z[x][y]);
//		    }
//	else
//	    for (int y = 0; y < 2; y++)
//		for(int x = 0; x < 2; x++)
//		    {
//			newMap.set(x, y, Y[x][y]);
//			newMap.set(x+2, y+2, X[x][y]);
//			newMap.set(x+4, y+4, Z[x][y]);
//		    }
//	
//         */
//        return Phi;
//    };
    
};
