/*
 * ProfileDescriptor.java
 * 
 * Created December, 2006
 * 
 * Christopher K. Allen
 */
package xal.tools.beam;


/**
 * Enumeration of supported phase space beam profiles.
 * 
 * @author Christopher K. Allen
 *
 */
public enum ProfileIndex {

    /*
     *  Enumeration of Supported Distributions
     */
    
    /** No distribution profile specified - usually indicates error condition */
    NONE(0),
    
    /** Kapchinskij-Vladimirskij (or canonical) distribution - uniformly distributed on phase-space surface */
    KV(1),
    
    /** Waterbag distribution - uniform in 6D phase space */
    WATERBAG(2),
    
    /** Parabolic distribution - parabolic in 6D phase space */
    PARABOLIC(3),
    
    /** Semi-Gaussian distribution - uniform in 3D configuration, gaussian in momentum */
    SEMIGAUSSIAN(4),

    /** Gaussian distribution - gaussian in 6D phase space */
    GAUSSIAN(5);


    
    /*
     * Global Methods
     */
    
    /**
     * Get the <code>ProfileDescriptor</code> object have the given index
     * value.
     * 
     * @param index     index of desired profile descriptor object
     * 
     * @return          profile descriptor object have the given index 
     */
    static public ProfileIndex    descriptorFromIndex(int index)    {
        for (ProfileIndex profile : ProfileIndex.values())    {
            if (profile.val() == index)
                return profile;
        }
        
        return NONE;
    }
    
    
    
    /*
     * Local Attributes
     */
    
    /** the profile index */
    private final int   iProfile;
    
    
    /*
     * Initialization
     */
    
    /**
     * Construct a new <code>ProfileDescriptor</code> object with the proper
     * profile index value.
     * 
     * @param   iProfile profile index
     */
    ProfileIndex(int iProfile)    {
        this.iProfile = iProfile;
    }
    
    
    /*
     * Attribute Query
     */
    
    /**
     * Return the index value of the current <code>ProfileDescriptor</code>
     * object.
     * 
     * @return  profile index value
     */
    public int  val()   {
        return iProfile;
    }
    
    
}
