/*
 * GenerationManager.java
 *
 * Created on October 2, 2002, 6:15 PM
 *
 */

package xal.sim.latgen;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;




/**
 *
 * @author  CKAllen
 */
public class GenerationManager {
    
    /*
     *  Configurable Parameters 
     */
    
    /** Map of available IGenerationRule's */
    private HashMap     m_mapRules;
    
    
    
    /** Creates a new instance of GenerationManager */
    public GenerationManager() {
    }
    
    
    /*
     *  Private Support Functions
     */
    
    /**
     *  Add a rule to the map of rule lists.  Create new list if necessary.
     */
    protected void addRule(IGenerationRule rule)  {
        int cntNodes = rule.getNodeSet().size();
        Integer intNodes = new Integer(cntNodes);
        
        if (!m_mapRules.containsKey(intNodes))   {
            LinkedList lstNew = new LinkedList();
            m_mapRules.put(intNodes, lstNew);
        }
        List lst = (List)m_mapRules.get(intNodes);
        lst.add(rule);
    };
    
    
    /**
     *  Get generation rule corresponding to a set of AcceleratorNode types.
     */
    protected IGenerationRule    getRule(Set setNodeTypes)   throws GenerationException {
        int cntNodes = setNodeTypes.size();
        Integer intNodes = new Integer(cntNodes);
        
        if (!m_mapRules.containsKey(intNodes))
            throw new GenerationException("GenerationManager::getRule() - no rule available for node type set.");
        
        List lstRules = (List)m_mapRules.get(intNodes);
        
        return selectRule(setNodeTypes, lstRules);
    };
    
    
    /**
     *  Selects a valid IGenerationRule object from a list of rules corresponding to the same number 
     *  of node types.
     */
    private IGenerationRule selectRule(Set setNodeTypes, List lstRules) throws GenerationException   {
        Iterator iterRules = lstRules.iterator();
        while (iterRules.hasNext()) {
            IGenerationRule rule = (IGenerationRule)iterRules.next();
            Set setNodeComb = rule.getNodeSet();
            if (setNodeComb.containsAll(setNodeTypes))
                return rule;
        }
        
        throw new GenerationException("GenerationManager::selectRule() - no rule available for node type set.");
    };
        
}
