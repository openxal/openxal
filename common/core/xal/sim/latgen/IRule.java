/*
 * IGenerationRule.java
 *
 * Created on October 2, 2002, 5:54 PM
 */

package xal.sim.latgen;


import java.util.*;


/**
 *  Interface to be exposed by classes implementing lattice element generation rules.  Such classes
 *  should take a set of AcceleratorNode objects (including a single element) and return an ordered 
 *  list of Element objects modeling the node set [set the function getElements()].
 *
 * @author  CKAllen
 * 
 * @deprecated  This is version 0.1 (never finished) which we are abandoning for dynamic 
 *              configuration.
 */

@Deprecated
public interface IRule {
    
    /**
     *  Returns a set of AcceleratorNode type string identifiers (order is not important).
     *  The class implementing this interface is able to generate an order list of Element
     *  objects that model this node combination.
     *  
     *  @return     set (unordered) of AcceleratorNode objects that generation rule handles
     */
    public Set getNodeCombination();
    
    
    /**
     *  Returns an ordered list of Element objects that model the given node set.
     *
     *  @param  setNodeCombin   node combination 
     *  @return                 generated modeling Element object list
     */
    public List getElements(Set setNodeCombin) throws GenerationException;
    
};
