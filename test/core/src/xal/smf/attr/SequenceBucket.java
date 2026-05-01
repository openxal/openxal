/*
 * SequencerBucket.java
 *
 * Created on 5/31/2002
 */

package xal.smf.attr;

/**
 *
 * A bucket to hold Information about sequences. In particular,
 * how long the sequence is, and allowed predecessor sequences.
 * @author J. Galambos
 * @version 1.0
 */


public class SequenceBucket extends AttributeBucket {
    /** serialization ID */
    private static final long serialVersionUID = 1L;

    
    /*
     *  Constants
     */

    public final static String  c_strType = "sequence"; 

    final static String[]       c_arrNames = {"predecessors"
                                };
    
    
    /*
     *  Local Attributes
     */
    
    private Attribute m_attPredecessors;

    
    /*
     *  User Interface
     */
    
    /** Furnish a unique type id  */
    public String getType()         { return c_strType; };

    public String[] getAttrNames()  { return c_arrNames; };
    

     
    
    /** Creates new SequenceBucket */
    public SequenceBucket() {
        super();
        
        String sa[] = new String[2]; // can have at most 2 predecessors
        m_attPredecessors  = new Attribute(sa);
        
        super.registerAttribute(c_arrNames[0], m_attPredecessors);
    };

    
    public String[]   getPredecessors()  { return m_attPredecessors.getArrStr(); };
    
    public void setPredecessors(String [] sa) { m_attPredecessors.set(sa); };
    
};
