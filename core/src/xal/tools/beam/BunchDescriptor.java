/**
 * 
 */
package xal.tools.beam;

import xal.tools.data.DataAdaptor;
import xal.tools.data.DataFormatException;
import xal.tools.data.IArchive;
import xal.tools.beam.Twiss3D.SpaceIndex3D;

/**
 * Encapsulation of an analytic description of a beam bunch using
 * Twiss parameters and a profile.
 * 
 * @author Christopher K. Allen
 * 
 * @deprecated  I'm not sure this is used, so I am deprecating it until
 *              I am certain.  Then it will be removed.
 */
@Deprecated
public class BunchDescriptor implements IArchive {

    /*
     * Global Constants
     */

    /** general value attribute tag */
    protected static final String ATTR_VALUE = "value";
    
    
    /** element tag for envelope twiss parameters */
    protected static final String LABEL_DISTRIBUTION = "dist";
    
    /** attribute tag for profile data */
    protected static final String ATTR_PROFILE = "profile";
    
    /** element tag for centroid data */
    protected static final String LABEL_CENTROID = "centroid";

//    /** element tag for envelope twiss parameters */
//    protected static final String LABEL_TWISS = "twiss";
//    
//    /** attribute tags for Twiss parameters */
//    protected static final String ATTR_ALPHA_X = "alphaX";
//    protected static final String ATTR_BETA_X = "betaX";
//    protected static final String ATTR_EMIT_X = "emitX";
//    protected static final String ATTR_ALPHA_Y = "alphaY";
//    protected static final String ATTR_BETA_Y = "betaY";
//    protected static final String ATTR_EMIT_Y = "emitY";
//    protected static final String ATTR_ALPHA_Z = "alphaZ";
//    protected static final String ATTR_BETA_Z = "betaZ";
//    protected static final String ATTR_EMIT_Z = "emitZ";
//    
    
    /*
     * Local Attributes
     */
    
    /** the distribution profile */
    private ProfileIndex        enmProfile;
    
    /** current centroid position */
    private PhaseVector         vecCentroid;
    
//    /** twiss parameters for bunch envelope */ 
//    private Twiss[]             arrTwiss;
//
    /** twiss parameters for the bunch envelope */
    private Twiss3D             twissEnv;
    
    
    /*
     * Initialization
     */
    
    /**
     * Create a new, empty <code>BunchDistribution</code> object.
     */
    public BunchDescriptor() {
        this.enmProfile = ProfileIndex.NONE;
        this.vecCentroid = PhaseVector.newZero();
        this.twissEnv = new Twiss3D();
    }

    /**
     * Copy Constructor.  Create a new <code>BunchDescriptor</code> object and
     * initialize it to the state of the argument.
     * 
     * @param dist  <code>BunchDescriptor</code> containing initializing state information
     */
    public BunchDescriptor(BunchDescriptor dist) {
        this.setProfile(dist.getProfile());
        this.setCentroid(dist.getCentroid());
        this.setTwiss(dist.getTwiss());
    }
    
    /**
     * Initializing Constructor: Create a new <code>BunchDescriptor</code> object
     * and initialize it with data from the data source behind the <code>DataAdaptor</code>
     * interface.
     * 
     * @param   daSource    data source containing initializing data
     * 
     * @throws  DataFormatException parsing error of data source
     */
    public BunchDescriptor(DataAdaptor daSource) throws DataFormatException {
        this.load(daSource);
    }
    
    /**
     * Initializing Constructor: Create a new <code>BunchDescriptor</code> object
     * and initialize to the given argument values.
     * 
     * @param   profile     distribution profile index
     * @param   vecCent     bunch centroid location
     * @param   twissX      x-plane Twiss parameters
     * @param   twissY      y-plane Twiss parameters
     * @param   twissZ      z-plane Twiss parameters
     */
    public BunchDescriptor(ProfileIndex iProfile, PhaseVector vecCent, Twiss twissX, Twiss twissY, Twiss twissZ) {
        this.setProfile(iProfile);
        this.setCentroid(vecCent);
        this.setTwiss(SpaceIndex3D.X, twissX);
        this.setTwiss(SpaceIndex3D.Y, twissY);
        this.setTwiss(SpaceIndex3D.Z, twissZ);
    }
    
    /**
     * Set the profile descriptor for this distribution.
     * 
     *  @param  iProfile     distribution profile descriptor
     */
    public void setProfile(ProfileIndex iProfile)    {
        this.enmProfile = iProfile;
    }
    
    /**
     * Set the centroid location of the beam bunch in homogeneous
     * coordinates.
     * 
     * @param   vecCentroid     new centroid of the bunch (x,x',y,y',z,z',1)
     */
    public void setCentroid(PhaseVector vecCentroid)   {
        this.vecCentroid = vecCentroid;
    }
    
    /**
     * Set the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * @param   twiss   twiss parameters
     */
    public void setTwiss(SpaceIndex3D iPlane, Twiss twiss)   {
        this.twissEnv.setTwiss(iPlane , twiss);
    }
    
    /** 
     * Set all three twiss parameters sets for the distribution
     * 
     * @param twissEnv     new set of Twiss objects (hor, vert,long)
     * 
     * @see gov.sns.tools.beam.Twiss3D
     */
    public void setTwiss(Twiss3D twissEnv) {
        this.twissEnv = twissEnv;
    }
    
    
    
    /*
     * Attribute Queries
     */
        
    
    /**
     * Get the distribution profile descriptor.
     * 
     * @return  profile descriptor object for this distribution
     */
    public ProfileIndex    getProfile()    {
        return this.enmProfile;
    }
    
    /**
     * Get the centroid location of the beam bunch in homogeneous
     * coordinates.
     * 
     * @return  centroid of the bunch (x,x',y,y',z,z',1)
     */
    public PhaseVector  getCentroid()   {
        return this.vecCentroid;
    }
    
    
    /**
     * Returns the Twiss parameters for the given phase plane.
     * 
     * @param   iPlane  phase plane index
     * 
     * @return  twiss parameters for given phase plane
     */
    public Twiss    getTwiss(SpaceIndex3D iPlane)    {
        return this.getTwiss().getTwiss(iPlane);
    }
    
    /** 
     * Returns the array of Twiss parameters for this distribution 
     * for all three planes.
     * 
     * @return  set of all Twiss parameters describing envelope
     */
    public Twiss3D getTwiss() { 
        return this.twissEnv;
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
        
        for (SpaceIndex3D index : SpaceIndex3D.values()) {
            int i = index.val();

            arrEmit[i] = this.getTwiss(index).getEmittance();
        }
        
        return arrEmit;
    }
    
    
    
    /*
     * IArchive Interface
     */ 
    
    
    /**
     * Save the state of this object to the data sink behind the <code>DataAdaptor</code>
     * interface.
     * 
     *  @param  daSink   data sink represented by <code>DataAdaptor</code> interface
     */
    public void save(DataAdaptor daSink) {
        
        DataAdaptor daDist = daSink.createChild(BunchDescriptor.LABEL_DISTRIBUTION);
        daDist.setValue(BunchDescriptor.ATTR_PROFILE, this.getProfile().val());
        
        DataAdaptor daCent = daDist.createChild(BunchDescriptor.LABEL_CENTROID);
        daCent.setValue(BunchDescriptor.ATTR_VALUE, this.getCentroid().toString());

        this.getTwiss().save(daSink);
        
//        DataAdaptor daTwiss = daDist.createChild(BunchDescriptor.LABEL_TWISS);
//        daTwiss.setValue(BunchDescriptor.ATTR_ALPHA_X, this.getTwiss(SpaceIndex3D.X).getAlpha());
//        daTwiss.setValue(BunchDescriptor.ATTR_BETA_X, this.getTwiss(SpaceIndex3D.X).getBeta());
//        daTwiss.setValue(BunchDescriptor.ATTR_EMIT_X, this.getTwiss(SpaceIndex3D.X).getEmittance());
//        daTwiss.setValue(BunchDescriptor.ATTR_ALPHA_Y, this.getTwiss(SpaceIndex3D.Y).getAlpha());
//        daTwiss.setValue(BunchDescriptor.ATTR_BETA_Y, this.getTwiss(SpaceIndex3D.Y).getBeta());
//        daTwiss.setValue(BunchDescriptor.ATTR_EMIT_Y, this.getTwiss(SpaceIndex3D.Y).getEmittance());
//        daTwiss.setValue(BunchDescriptor.ATTR_ALPHA_Z, this.getTwiss(SpaceIndex3D.Z).getAlpha());
//        daTwiss.setValue(BunchDescriptor.ATTR_BETA_Z, this.getTwiss(SpaceIndex3D.Z).getBeta());
//        daTwiss.setValue(BunchDescriptor.ATTR_EMIT_Z, this.getTwiss(SpaceIndex3D.Z).getEmittance());           
    }
        
    /**
     * Recover the state values particular to <code>BunchDistribution</code> objects 
     * from the data source.
     *
     *  @param  daSource   data source represented by a <code>DataAdaptor</code> interface
     * 
     *  @exception DataFormatException     state information in data source is malformatted
     */
    public void load(DataAdaptor daSource) throws DataFormatException  {
        
        // Recover the distribution node and load the profile index
        DataAdaptor daDist = daSource.childAdaptor(BunchDescriptor.LABEL_DISTRIBUTION);
        if (daDist == null)
            throw new DataFormatException("BunchDescriptor#load(): no child element = " + LABEL_DISTRIBUTION);
        if (daDist.hasAttribute(BunchDescriptor.ATTR_PROFILE)) {
            int iProfile = daDist.intValue(BunchDescriptor.ATTR_PROFILE);
            this.setProfile(ProfileIndex.descriptorFromIndex(iProfile));
        }
        
        // Recover the centroid node and load it
        DataAdaptor daCent = daDist.childAdaptor(BunchDescriptor.LABEL_CENTROID);
        if (daCent == null)
            throw new DataFormatException("BunchDescriptor#load(): no child element = " + LABEL_CENTROID);
        if (daCent.hasAttribute(BunchDescriptor.ATTR_VALUE))    {
            String  strCent = daCent.stringValue(BunchDescriptor.ATTR_VALUE);
            this.setCentroid( PhaseVector.parse(strCent) );
        }
            
        // Recover the Twiss parameter node and load them
        this.twissEnv = new Twiss3D(daSource);
        
//        DataAdaptor daTwiss = daDist.childAdaptor(BunchDescriptor.LABEL_TWISS); 
//        if (daTwiss == null)
//            throw new DataFormatException("BunchDescriptor#load(): no child element = " + LABEL_TWISS);
//        if (daTwiss.hasAttribute(BunchDescriptor.ATTR_ALPHA_X)) {
//            Twiss[] twiss = new Twiss[3];
//            twiss[0] = new Twiss(daTwiss.doubleValue(BunchDescriptor.ATTR_ALPHA_X), 
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_BETA_X),
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_EMIT_X));
//            twiss[1] = new Twiss(daTwiss.doubleValue(BunchDescriptor.ATTR_ALPHA_Y), 
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_BETA_Y),
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_EMIT_Y));
//            twiss[2] = new Twiss(daTwiss.doubleValue(BunchDescriptor.ATTR_ALPHA_Z), 
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_BETA_Z),
//                    daTwiss.doubleValue(BunchDescriptor.ATTR_EMIT_Z));
//            this.setTwiss(twiss);
//        }
    }
    
    
}
