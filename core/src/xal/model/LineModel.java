/*
 * Created on May 28, 2004
 */
package xal.model;


import java.util.regex.Pattern;

import xal.model.elem.ElementSeq;

/**
 * Represents a model lattice for a linear accelerating structure.
 * Or in other words, and accelerator <em>line</em>.
 * 
 * @author Christopher K. Allen
 * 
 * @deprecated  This class is never used
 */
@Deprecated
public class LineModel extends ElementSeq {
    

    /*
     * Global Constants
     */
    
    /** I believe this is set of allowable characters for device IDs */
    private static final Pattern validElementPattern = Pattern.compile("^[a-zA-Z:_].*$");


    /*
     * Global Operations
     */
    
    /**
     * I believe Sako-san wrote this to check for "valid" elements
     * by looking at their string identifiers
     * (see <code>{@link #validElementPattern}</code>).  I'm not 
     * sure what valid is, however.
     *
     * @param elem0     modeling element string identifier ?
     * 
     * @return         <code>true</code> if the identifier meets the criteria,
     *                 <code>false</code> otherwise
     *
     * @author H. Sako
     * @author Christopher K. Allen
     * @since  Unknown
     */
    private static boolean isValidElement(String elem0) {
        return elem0 != null && validElementPattern.matcher(elem0).matches();
    }

    
    /**
     * Create a new <code>LineModel</code> object with the given
     * string type identifier.  The type refers to the type
     * of accelerating structure this models.
     * 
     * @param strType   this object's type class (name)
     *
     * @author  Christopher K. Allen
     * @since   May 28, 2004
     */
    public LineModel(String strType) {
        super(strType);
    }
    
    /**
     * Create a new <code>LineModel</code> object with the given
     * type and given string identifier.
     * 
     * @param strType   the type class of the accelerating structure
     * @param strId     the identifier of this particular structure
     *
     * @author  Christopher K. Allen
     * @since   May 28, 2004
     */
    public LineModel(String strType, String strId) {
        super(strType, strId);
    }
    
    /**
     * Create a new <code>LineModel</code> object with the given
     * type, given string identifier, and while reserving the given
     * number of positions for internal modeling elements.  Note that
     * if the number of child elements grows large than the reservation
     * number, extra space is automatically allocated.  It's just faster
     * to use this feature if you do know the size of the child set. 
     * 
     * @param strType   the type class of the accelerating structure
     * @param strId     the identifier of this particular structure
     * @param szReserve reserve allocation size for child modeling elements
     *
     * @author  Christopher K. Allen
     * @since   May 28, 2004
     */
    public LineModel(String strType, String strId, int szReserve) {
        super(strType, strId, szReserve);
    }
    
    /** 
     * <h2>Override of {@link xal.model.elem.ElementSeq#propagate(xal.model.IProbe)}</h2>
     *
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     *
     * @see xal.model.elem.ElementSeq#propagate(xal.model.IProbe)
     */
    @Override
    public void propagate(IProbe probe) throws ModelException {
//        System.out.println("LineModel.propagate called");
        
        String elem0 = probe.getCurrentElement();
        if (isValidElement(elem0)) {
//            System.out.println("found valid elem0 = "+elem0);
            propagateWithElement(probe, elem0);
            
        } else {
            propagateWithoutElement(probe);
        }
    }

    /** 
     * <h2>Override of {@link xal.model.elem.ElementSeq#backPropagate(xal.model.IProbe)}</h2>
     *
     * @author Christopher K. Allen
     * @since Feb 27, 2009
     *
     * @see xal.model.elem.ElementSeq#backPropagate(xal.model.IProbe)
     */
    @Override
    public void backPropagate(IProbe probe) throws ModelException {
        System.out.println("LineModel.backPropagate called");
        
        String elem0 = probe.getCurrentElement();
        if (isValidElement(elem0)) {
            System.out.println("found valid elem0 = "+elem0);
            backPropagateWithElement(probe, elem0);
            
        } else {
            backPropagateWithoutElement(probe);
        }
    }
    
    
    /*
     * Internal Support
     */

    /**
     * Just added comment - don't know what this does.
     * 
     * @param probe     beam representation
     * @param elem0     modeling element identifier
     * 
     * @throws ModelException       I don't think this is thrown
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    private void propagateWithElement(IProbe probe, String elem0) throws ModelException {
        double s = 0;
        boolean started = false;
        
        for(IComponent comp : getForwardCompList()) {
            if (started) {
                comp.propagate(probe);
                
            } else if (elem0.equals(comp.getId())) {
                System.out.println("found elem0, s = "+elem0+" "+s);
                probe.setPosition(s);
                probe.initialize();
                comp.propagate(probe);
                started = true;
            }
            
            s += comp.getLength();
        }
    }

    /**
     * Just added comment - don't know what this does.
     * 
     * @param probe     beam representation
     * @param elem0     modeling element identifier
     * 
     * @throws ModelException       I don't think this is thrown
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    private void propagateWithoutElement(IProbe probe) throws ModelException {
        double s0 = probe.getPosition();
        double s = 0;
        
        for(IComponent comp : getForwardCompList()) {
            double len = comp.getLength();
            
            if(s0 <= s) {
                comp.propagate(probe);
                
            } else if(s0 <= s+len) {
                comp.propagate(probe,s0-s);
                
            } else {
                // skip
                System.out.println("skip elem "+comp.getId());
                System.out.println("s0, s, len = "+s0+" "+s+" "+len);
            }
            
            s += len;
        }
    }

    /**
     * Just added comment - obvious this is part of the back propagation mechanism.
     * 
     * @param probe     beam representation
     * @param elem0     modeling element identifier
     * 
     * @throws ModelException       I don't think this is thrown
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    private void backPropagateWithElement(IProbe probe, String elem0) throws ModelException {
        double s = this.getLength();
        boolean started = false;
        
        for(IComponent comp : getReverseCompList()) {
            if (started) {
                comp.propagate(probe);
                
            } else if (elem0.equals(comp.getId())) {
                System.out.println("found elem0, s = "+elem0+" "+s);
                probe.setPosition(s);
                probe.initialize();
                comp.propagate(probe);
                started = true;
            }
            
            s -= comp.getLength();
        }
    }

    /**
     * Just added comment - obvious this is part of the back propagation mechanism.
     * 
     * @param probe     beam representation
     * @param elem0     modeling element identifier
     * 
     * @throws ModelException       I don't think this is thrown
     *
     * @author Christopher K. Allen
     * @since  Nov 9, 2011
     */
    private void backPropagateWithoutElement(IProbe probe) throws ModelException {
        double s0 = probe.getPosition();
        double s = this.getLength();
        
        for(IComponent comp : getReverseCompList()) {
            double len = comp.getLength();
            
            if(s0 >= s) {
                comp.backPropagate(probe);
                
            } else if (s0 >= s-len) {
                comp.propagate(probe,s0-s);
                
            } else {
                // skip
                System.out.println("skip elem "+comp.getId());
                System.out.println("Probe pos = " + s0 + ", Seq pos = " + s + ", Elem len = " + len);
            }
            
            s -= len;
        }
        
    }
}
