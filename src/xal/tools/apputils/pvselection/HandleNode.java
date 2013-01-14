package xal.tools.apputils.pvselection;

import javax.swing.tree.*;

import xal.ca.*;

public class HandleNode extends DefaultMutableTreeNode {
	private static final long serialVersionUID = 0L;
	
    private boolean itIsSignal = false;

    private Channel channel = null;

    private String signalName = null;

    public HandleNode(){}

    public HandleNode(Object value){
	super(value);
    }

    public boolean isSignal(){
	return itIsSignal;
    }

    public void setAsSignal(boolean itIsSignal){
	this.itIsSignal = itIsSignal;
    }

    public void setChannel(Channel channelIn){
	channel = channelIn;
    }

    public Channel getChannel(){
	return channel;
    }

    public String getChannelName(){
	if(channel != null){
	    return channel.channelName();
	}
	return null;
    }

    public String getChannelId(){
	if(channel != null){
	    return channel.getId();
	}
	return null;
    }

    public void setSignalName(String signalName){
	this.signalName = signalName;
    }
  
    public String getSignalName(){
	return signalName;
    } 
}
