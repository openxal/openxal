/*
 * LatticeGenerator.java
 *
 * Created on October 1, 2002, 4:08 PM
 *
 */

package xal.sim.latgen;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import xal.model.Lattice;
import xal.smf.Accelerator;
import xal.smf.AcceleratorNode;
import xal.smf.AcceleratorSeq;



/**
 *
 * @author  CKAllen
 * @author  Craig McChesney
 */
public class LatticeGenerator {
    
    /*
     *  Data Source Enumerations
     */
    private final int   DATASRC_NONE = 0;
    private final int   DATASRC_LIVE = 1;
    private final int   DATASRC_DESIGN = 2;
    

    private Accelerator m_accel = null;
    
    private AcceleratorSeq m_seq = null;
    
    /** Creates a new instance of LatticeGenerator */
    public LatticeGenerator() {
    }
    
    
    /**
     * Generate a lattice model object for the given AcceleratorSeq object.
     * Finds a collection of accelerator nodes at each unique position, and builds
     * the lattice elements for that collection using the generation rule
     * registered in the GenerationManager.
     * 
     * @param seq Accelerator sequence to build a Lattice for
     * @return    a Lattice that models the supplied accelerator sequence
     */
    public Lattice generate(AcceleratorSeq seq) {
        Lattice lat = new Lattice();
        
        GenerationManager mgrGen = new GenerationManager();
        
            
        return lat;
    };
    
    
    /*
     *  Internal Support Functions
     */
    
    
    /**
     *  Uses generation rules to build a sequence of Element objects from a composite node structure.
     *  The current lattice under generation is augmented by these elements.
     *
     *  @param  lstNodes    list of composite nodes
     *  @param  lat         current Lattice object under generation
     */
    
    private void    buildElements(List lstNodes, Lattice lat){
        Iterator iter = lstNodes.iterator();

		System.out.println();
		System.out.println("position: " + ((AcceleratorNode)lstNodes.get(0)).getPosition());
		System.out.println("building node set:");
        
        while (iter.hasNext())  {
            AcceleratorNode node = (AcceleratorNode)iter.next();
//            System.out.println(node.getId());
//            Element elem = new Element(node, node.getLength(), node.getPosition());
//            lat.add(elem);
        }
    };
}
