/*
 * Twiss3D.java 
 * 
 * Created  : December, 2006
 * Author   : Christopher K. Allen
 */
package xal.tools.beam;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;

/**
 * Encapsulates the Twiss parameters for describing a beam bunch.  Specifically,
 * we have three sets of Twiss parameters, one for each phase plane.  This class
 * is basically a container allowing type safe passing of Twiss parameters rather
 * passing an array which allows for runtime errors.
 * 
 * @author Christopher K. Allen
 *
 */
public class Twiss3D implements IArchive {

    
    
    
    /*
     * Global Constants
     */

    /** element tag for envelope twiss parameters */
    protected static final String LABEL_TWISS = "twiss";
    
    /** attribute tags for Twiss parameters */
    protected static final String ATTR_ALPHA_X = "ax";
    protected static final String ATTR_BETA_X  = "bx";
    protected static final String ATTR_EMIT_X  = "ex";
    protected static final String ATTR_ALPHA_Y = "ay";
    protected static final String ATTR_BETA_Y  = "by";
    protected static final String ATTR_EMIT_Y  = "ey";
    protected static final String ATTR_ALPHA_Z = "az";
    protected static final String ATTR_BETA_Z  = "bz";
    protected static final String ATTR_EMIT_Z  = "ez";
    
    
    /*
     * Local Attributes
     */
    
    /** twiss parameters for bunch envelope */ 
    private Twiss[]             arrTwiss = new Twiss[3];
    
    
    
    
    /*
     * Initialization
     */
    
    
    /**
     * Create a new, empty  
     */
    public Twiss3D() {
    }

    /**
     * Copy Constructor.  Create a new <code>Twiss3D</code> object which is
     * a <b>deep copy</b> of the given argument.
     * 
     * @param t3d  <code>Twiss3D</code> containing initializing state information
     */
    public Twiss3D(Twiss3D t3d) {
        for (SpaceIndex3D index : SpaceIndex3D.values())    {
            this.arrTwiss[index.val()] = new Twiss(t3d.getTwiss(index));
        }
    }
    
    /**
     * Initializing Constructor: Create a new <code>Twiss3D</code> object
     * and initialize to the given argument values.
     * 
     * @param   twissX      x-plane Twiss parameters
     * @param   twissY      y-plane Twiss parameters
     * @param   twissZ      z-plane Twiss parameters
     */
    public Twiss3D(Twiss twissX, Twiss twissY, Twiss twissZ) {
        this.setTwiss(SpaceIndex3D.X, twissX);
        this.setTwiss(SpaceIndex3D.Y, twissY);
        this.setTwiss(SpaceIndex3D.Z, twissZ);
    }
    
    /**
     * Initializing Constructor: Create a new <code>Twiss3D</code> object
     * and initialize it with data from the data source behind the <code>DataAdaptor</code>
     * interface.
     * 
     * @param   daSource    data source containing initializing data
     * 
     * @throws  DataFormatException parsing error of data source
     */
    public Twiss3D(DataAdaptor daSource) throws DataFormatException {
        this.load(daSource);
    }
    
    /**
     * Set the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * @param   twiss   twiss parameters
     */
    public void setTwiss(SpaceIndex3D iPlane, Twiss twiss)   {
        this.arrTwiss[iPlane.val()] = twiss;
    }
    
    
    /*
     * Attribute Queries
     */
        
    
    /**
     * Returns the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * 
     * @return  twiss parameters for given phase plane
     */
    public Twiss    getTwiss(SpaceIndex3D iPlane)    {
        return this.arrTwiss[iPlane.val()];
    }
    
    /** 
     * Returns the array of Twiss parameters for this distribution 
     * for all three planes.
     * 
     * @return array(twiss-H, twiss-V, twiss-L)
     */
    public Twiss[] getTwiss() { 
        return this.arrTwiss;
    }
    
    
    

    /*
     * Computed Properties
     */

    
    /**
     *  Convenience Method:  Returns the rms emittances for this distribution as
     *  from the individual Twiss parameters.  
     * 
     * @return array (ex,ey,ez) of rms emittances
     */
    public double[] rmsEmittances() {
        double  arrEmit[] = new double[3];
        
        for (int i = 0; i < 3; i++) 
            arrEmit[i] = this.getTwiss()[i].getEmittance();
        
        return arrEmit;
    }
    
    
    
    /*
     * IArchive Interface
     */ 
    
    
    /**
     * Save the state of this object to the data sink behind the <code>DataAdaptor</code>
     * interface.
     * 
     * @param  daSink   data sink represented by <code>DataAdaptor</code> interface
     * 
     * @see xal.tools.data.IArchive#save(xal.tools.data.DataAdaptor)
     */
    public void save(DataAdaptor daSink) {
        
        DataAdaptor daTwiss = daSink.createChild(Twiss3D.LABEL_TWISS);
        daTwiss.setValue(Twiss3D.ATTR_ALPHA_X, this.getTwiss(SpaceIndex3D.X).getAlpha());
        daTwiss.setValue(Twiss3D.ATTR_BETA_X, this.getTwiss(SpaceIndex3D.X).getBeta());
        daTwiss.setValue(Twiss3D.ATTR_EMIT_X, this.getTwiss(SpaceIndex3D.X).getEmittance());
        daTwiss.setValue(Twiss3D.ATTR_ALPHA_Y, this.getTwiss(SpaceIndex3D.Y).getAlpha());
        daTwiss.setValue(Twiss3D.ATTR_BETA_Y, this.getTwiss(SpaceIndex3D.Y).getBeta());
        daTwiss.setValue(Twiss3D.ATTR_EMIT_Y, this.getTwiss(SpaceIndex3D.Y).getEmittance());
        daTwiss.setValue(Twiss3D.ATTR_ALPHA_Z, this.getTwiss(SpaceIndex3D.Z).getAlpha());
        daTwiss.setValue(Twiss3D.ATTR_BETA_Z, this.getTwiss(SpaceIndex3D.Z).getBeta());
        daTwiss.setValue(Twiss3D.ATTR_EMIT_Z, this.getTwiss(SpaceIndex3D.Z).getEmittance());           
    }
        
    /**
     * Recover the state values particular to <code>BunchDistribution</code> objects 
     * from the data source.
     *
     * @param  daSource   data source represented by a <code>DataAdaptor</code> interface
     * 
     * @exception DataFormatException     state information in data source is malformatted
     *  
     * @see xal.tools.data.IArchive#load(xal.tools.data.DataAdaptor)
     */
    public void load(DataAdaptor daSource) throws DataFormatException  {
        
        // Recover the Twiss parameter node and load them
        DataAdaptor daTwiss = daSource.childAdaptor(Twiss3D.LABEL_TWISS); 
        if (daTwiss == null)
            throw new DataFormatException("Twiss3D#load(): no child element = " + LABEL_TWISS);
        if (daTwiss.hasAttribute(Twiss3D.ATTR_ALPHA_X)) {
            Twiss[] twiss = new Twiss[3];
            twiss[0] = new Twiss(daTwiss.doubleValue(Twiss3D.ATTR_ALPHA_X), 
                                 daTwiss.doubleValue(Twiss3D.ATTR_BETA_X),
                                 daTwiss.doubleValue(Twiss3D.ATTR_EMIT_X));
            twiss[1] = new Twiss(daTwiss.doubleValue(Twiss3D.ATTR_ALPHA_Y), 
                                 daTwiss.doubleValue(Twiss3D.ATTR_BETA_Y),
                                 daTwiss.doubleValue(Twiss3D.ATTR_EMIT_Y));
            twiss[2] = new Twiss(daTwiss.doubleValue(Twiss3D.ATTR_ALPHA_Z), 
                                 daTwiss.doubleValue(Twiss3D.ATTR_BETA_Z),
                                 daTwiss.doubleValue(Twiss3D.ATTR_EMIT_Z));
            this.setTwiss(twiss);
        }
    }

    

    /*
     * Support Functions
     */
    
    /** 
     * Set all three Twiss parameters sets for the distribution
     * 
     * CKA Notes:
     * - I don't like this method since it can break the self-consistency 
     * of the class.  But it's here for backward convenience within the class and 
     * I added the check for 3D and the subsequent runtime error.
     * 
     * @param twiss new 3 dimensional array of Twiss objects (hor, vert,long)
     * 
     * @throws  IllegalArgumentException    argument is not three-dimensional
     */
    private void setTwiss(Twiss [] arrTwiss) throws IllegalArgumentException  {
        if (arrTwiss.length != 3)
            throw new IllegalArgumentException("Twiss3D#setTwiss - argument not three-dimensional");
        
        this.arrTwiss = arrTwiss;
    }
    
    
}
