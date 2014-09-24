package xal.tools.twissobserver;



/**
 * <p>
 * Class for organizing location data into an easily managed format.
 * </p>
 * @author Eric Dai
 * @author Christopher K. Allen
 * @since June 19, 2012
 * 
 * @deprecated  Replaced by <code>{@link Measurement}</code>
 *
 */
@Deprecated
public class LocData {
    
    /** String containing desired element ID */
    public String strDevId;
    
	/** Double containing horizontal beam size at specified element location */
	public Double dblRmsSizeHor;
	
	/** Double containing vertical beam size at given element location */
	public Double dblRmsSizeVer;
}
