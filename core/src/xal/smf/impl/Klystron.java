package xal.smf.impl;

import xal.ca.Channel;
import xal.ca.ChannelFactory;
import xal.ca.ConnectionException;
import xal.ca.GetException;
import xal.ca.PutException;
import xal.tools.data.DataAdaptor;
import xal.tools.data.DataListener;
import xal.smf.Accelerator;
import xal.smf.ChannelSuite;
import xal.smf.NoSuchChannelException;

public class Klystron implements DataListener {

	static public final String KLYS_AMP_SET_HANDLE = "klysAmpSet";
	static public final String KLYS_PHASE_SET_HANDLE = "klysPhaseSet";
	static public final String KLYS_AMP_RB_HANDLE = "klysAmp";
	static public final String KLYS_PHASE_RB_HANDLE = "klysPhase";
	
	protected Accelerator accelerator;
    protected ChannelSuite channelSuite;
    protected String strId;
    public static final String      s_strType = "KLYS";

    private Channel klysAmpSetC = null;
    private Channel klysPhaseSetC = null;
    private Channel klysAmpC = null;
    private Channel klysPhaseC = null;
    
    private int[] powerFact;
    private int[] controlFlag;
    private boolean stat = false;


    /**
     * Constructor for Klystron using the same channel factory as the provided accelerator.
     * @param anAccelerator the accelerator object this klystron belongs to
     */
    public Klystron( final Accelerator anAccelerator ) {
		this.accelerator = anAccelerator;
		this.channelSuite = anAccelerator != null ? new ChannelSuite( anAccelerator.channelSuite().getChannelFactory() ) : new ChannelSuite();
    }

    
//    @Override
	public String dataLabel() {
		return "KLYS";
	}

    /**
     * Find the channel for the specified handle.
     * @param handle The handle for the channel to fetch
     * @return the channel if found or null if not found
     */
    public Channel findChannel( final String handle ) {
        return channelSuite.getChannel( handle );
    }
    
  /**
     * Get the channel corresponding to the specified handle and connect it. 
     * @param handle The handle for the channel to get.
     * @return The channel associated with this node and the specified handle or null if there is no match.
     * @throws xal.smf.NoSuchChannelException if no such channel as specified by the handle is associated with this node.
     * @throws xal.ca.ConnectionException if the channel cannot be connected
     */
    public Channel getAndConnectChannel( final String handle ) throws NoSuchChannelException, ConnectionException {
        Channel channel = getChannel( handle );
        channel.connectAndWait();
        
        return channel;
    }
    
    /**
     * Get the channel for the specified handle.
     * @param handle The handle for the channel to fetch
     * @return the channel
     */
    public Channel getChannel( final String handle ) throws NoSuchChannelException {
        final Channel channel = findChannel( handle );
        
        if ( channel == null ) {
            throw new NoSuchChannelException( handle );
        }
        
        return channel;
    }
    
    /**
     * Get the channel suite.
     * @return the channel suite.
     */
    public ChannelSuite getChannelSuite() {
        return channelSuite;
    }
    
    /**
     * Get the klystron's amplitude.
     * @return klystron amplitude
     * @throws ConnectionException
     * @throws GetException
     */
    public double getKlysAmp() throws ConnectionException, GetException {
    	klysAmpC = getAndConnectChannel(KLYS_AMP_RB_HANDLE);
    	
    	return klysAmpC.getValDbl();
    }
    
    /**
     * Get the klystron's phase.
     * @return klystron phase
     * @throws ConnectionException
     * @throws GetException
     */
    public double getKlysPhase() throws ConnectionException, GetException {
    	klysPhaseC = getAndConnectChannel(KLYS_PHASE_RB_HANDLE);
    	
    	return klysPhaseC.getValDbl();
    }
    
   /**
     * Get the unique power supply ID
     * @return The power supply ID
     */
    public String getId() {
        return strId;
    }
    
    /**
     * Get predefined control flags.
     * @return control flag in integer array
     */
    public int[] getControlFlag() {
    	return controlFlag;
    }
    
    /**
     * Get power distribution factors
     * @return power distribution flags in integer array
     */
    public int[] getPowerFact() {
    	return powerFact;
    }
    
    /**
     * Get the klystron type.
     * @return klystron type
     */
    public String getType() {
    	return s_strType;
    }
    
    public boolean getStatus() {
    	return stat;
    }
    
    /**
     * Set klystron amplitude.
     * @param newAmp new klystron amplitude
     * @throws ConnectionException
     * @throws PutException
     */
    public void setKlysAmp(double newAmp) throws ConnectionException, PutException {
    	klysAmpSetC = getAndConnectChannel(KLYS_AMP_SET_HANDLE);
    	klysAmpSetC.putVal(newAmp);
    }
    
    /**
     * Set klystron phase.
     * @param newPhase new klystron phase
     * @throws ConnectionException
     * @throws PutException
     */
    public void setKlysPhase(double newPhase) throws ConnectionException, PutException {
    	klysPhaseSetC = getAndConnectChannel(KLYS_PHASE_SET_HANDLE);
    	klysPhaseSetC.putVal(newPhase);
    }
    
    /**
     * Set control flag.
     * @param cFlag control flag in integer array
     */
    public void setControlFlag(int[] cFlag) {
    	controlFlag = cFlag;
    }
    
    /**
     * Set Power distribution factors.
     * @param pFact power distribution factors in integer array
     */
    public void setPowerFact(int[] pFact) {
    	powerFact = pFact;
    }
    
    public void setStatus(boolean stat) {
    	this.stat = stat;
    }
    
//    @Override
	public void update(DataAdaptor adaptor) {
        strId = adaptor.stringValue("id");
        DataAdaptor suiteAdaptor = adaptor.childAdaptor("channelsuite");
        channelSuite.update(suiteAdaptor);		
	}

//	@Override
	public void write(DataAdaptor adaptor) {
        adaptor.setValue("id", strId);
        adaptor.setValue("type", getType());
        adaptor.writeNode(channelSuite);		
	}

}
