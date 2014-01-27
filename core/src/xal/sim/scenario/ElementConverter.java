/*
 * ElementConverter.java
 * 
 * Created on Oct 3, 2013
 */

package xal.sim.scenario;

import java.util.logging.Level;
import java.util.logging.Logger;

import xal.model.IComponent;
import xal.model.elem.Element;
import xal.model.elem.ElementSeq;
import xal.model.elem.IElectromagnet;
import xal.model.elem.ThickElement;
import xal.smf.impl.Magnet;

/**
 * Abstract class which a base for a converter from a SMF node to online model element.
 * The class provides 
 * 
 * @author Ivo List
 *
 */
abstract class ElementConverter {
	protected boolean thin = false;

	/**
	 * While generating lattice there's an important distinction between thick and thin elements.
	 * An element is thin if it has zero length or it's "virutally" thin when a converter says so.
	 * 
	 * @return true if element is "virtually" thin
	 */
	public boolean isThin() {
		return thin;
	}	
	
	/**
	 * Main conversion method calls abstract convert() method and adds some helpful conversion.
	 * It sets online model element's id, position, length.
	 * For magnets it sets orientation.
	 * 
	 * @param latticeElement the SMF node to convert
	 * @return online model element
	 */
	public IComponent doConvert(LatticeElement latticeElement)
	{
		IComponent component = convert(latticeElement);
		if (component instanceof Element) {
			Element element = (Element)component;
			element.setId(latticeElement.getNode().getId());			
			element.setPosition(latticeElement.getCenter());
			if (element instanceof ThickElement)
				((ThickElement)element).setLength(latticeElement.getLength());		
		} else if (component instanceof ElementSeq) 
		{
			ElementSeq elementSeq = (ElementSeq)component;
			elementSeq.setId(latticeElement.getNode().getId());			
		}
		if (latticeElement.getNode() instanceof Magnet && component instanceof IElectromagnet) {
			setupOrientation((Magnet)latticeElement.getNode(), (IElectromagnet)component);
		}
		return component;
	}
	
	/**
	 * Conversion method to be provided by the user
	 * 
	 * @param element the SMF node to convert
	 * @return online model element
	 */
	protected abstract IComponent convert(LatticeElement element);
	
	/**
	 * Helper method to set orientation on the magnets.
	 * 
	 * @param node the SMF magnet node
	 * @param element online model node
	 */
	public static void setupOrientation(Magnet magnetNode, IElectromagnet electromagnetElement)
	{
		int orientation = IElectromagnet.ORIENT_NONE;
		if (magnetNode.isHorizontal()) {
			orientation = IElectromagnet.ORIENT_HOR;
		} else if (magnetNode.isVertical()) {
			orientation = IElectromagnet.ORIENT_VER;
		} else {
		    //  This is an exceptional condition!
		    //    Something went wrong if we made it here
		    //    Let's say so
		    
		    // CKA - we are going to skip this since skew quadrupoles (soft type = "QSC")
		    //    have no orientation and always throw this warning
//		    String    strSrc = magnetNode.getId() + "/" + magnetNode.getClass().getName();
//		    String    strMsg = "Encountered an un-oriented electromagnet hardware object";
//		    Logger    logGbl = Logger.getLogger("global");
//		    
//		    logGbl.log(Level.WARNING, strMsg + " : " + strSrc);
//            System.out.println("WARNING!: " + strMsg + " : " + strSrc);
		}
		electromagnetElement.setOrientation(orientation);
	}
}