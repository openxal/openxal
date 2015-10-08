/*
 * Copyright (c) 2015 by Cosylab
 *
 * The full license specifying the redistribution, modification, usage and other
 * rights and obligations is included with the distribution of this project in
 * the file "LICENSE-CAJ". If the license is not included visit Cosylab web site,
 * <http://www.cosylab.com>.
 *
 * THIS SOFTWARE IS PROVIDED AS-IS WITHOUT WARRANTY OF ANY KIND, NOT EVEN THE
 * IMPLIED WARRANTY OF MERCHANTABILITY. THE AUTHOR OF THIS SOFTWARE, ASSUMES
 * _NO_ RESPONSIBILITY FOR ANY CONSEQUENCE RESULTING FROM THE USE, MODIFICATION,
 * OR REDISTRIBUTION OF THIS SOFTWARE.
 */
package xal.plugin.jca.server;

import gov.aps.jca.cas.ProcessVariableEventCallback;

import com.cosylab.epics.caj.cas.util.DefaultServerImpl;
import com.cosylab.epics.caj.cas.util.MemoryProcessVariable;

/**
 * {@link ServerMemoryRecord} (PV) extends {@link MemoryProcessVariable} and overrides its {@link #write}
 * method, to disable writing to read-only channels.
 * 
 * @version 0.1 13 Jul 2015
 * @author Blaz Kranjc <blaz.kranjc@cosylab.com>
 */
public class ServerMemoryRecord extends ServerMemoryProcessVariable {
	protected ServerMemoryProcessVariable lowerWarningLimitPV, upperWarningLimitPV;
    protected ServerMemoryProcessVariable lowerAlarmLimitPV, upperAlarmLimitPV;
    protected ServerMemoryProcessVariable lowerDispLimitPV, upperDispLimitPV;
    protected ServerMemoryProcessVariable lowerCtrlLimitPV, upperCtrlLimitPV;
        
    /**
     * Creates and registers the main PV and all field PVs that are expected.
     * 
     * @param name	name of the PV.
	 * @param eventCallback	event callback, where to report value changes if <code>interest</code> is <code>true</code>.
	 * @param initialValue	initial value
	 * 
     * @see MemoryProcessVariable
     */
    public ServerMemoryRecord(String name, ProcessVariableEventCallback eventCallback, double[] initialValue, DefaultServerImpl channelServer) {
        super(name, eventCallback, initialValue, channelServer);
        
        channelServer.registerProcessVaribale(name+".VAL", this);
        
        lowerWarningLimitPV = new ServerMemoryProcessVariable(name+".LOW", eventCallback, new double[] {0.}, channelServer);
        upperWarningLimitPV = new ServerMemoryProcessVariable(name+".HIGH", eventCallback, new double[] {0.}, channelServer);
        
        lowerAlarmLimitPV = new ServerMemoryProcessVariable(name+".LOLO", eventCallback, new double[] {0.}, channelServer);
        upperAlarmLimitPV = new ServerMemoryProcessVariable(name+".HIHI", eventCallback, new double[] {0.}, channelServer);
        
        lowerDispLimitPV = new ServerMemoryProcessVariable(name+".LOPR", eventCallback, new double[] {0.}, channelServer);
        upperDispLimitPV = new ServerMemoryProcessVariable(name+".HOPR", eventCallback, new double[] {0.}, channelServer);
        
        lowerCtrlLimitPV = new ServerMemoryProcessVariable(name+".DRVL", eventCallback, new double[] {0.}, channelServer);
        upperCtrlLimitPV = new ServerMemoryProcessVariable(name+".DRVH", eventCallback, new double[] {0.}, channelServer);        
    }
    
    @Override
	public Number getLowerAlarmLimit() {
		return ((double[])lowerAlarmLimitPV.getValue())[0];
	}

	@Override
	public void setLowerAlarmLimit(Number lowerAlarmLimit) {
		lowerAlarmLimitPV.setValue(new double[]{lowerAlarmLimit.doubleValue()});
	}

	@Override
	public Number getLowerCtrlLimit() {
		return ((double[])lowerCtrlLimitPV.getValue())[0];
	}

	@Override
	public void setLowerCtrlLimit(Number lowerCtrlLimit) {
		lowerCtrlLimitPV.setValue(new double[]{lowerCtrlLimit.doubleValue()});
	}
	

	@Override
	public Number getLowerDispLimit() {
		return ((double[])lowerDispLimitPV.getValue())[0];
	}

	@Override
	public void setLowerDispLimit(Number lowerDispLimit) {
		lowerDispLimitPV.setValue(new double[]{lowerDispLimit.doubleValue()});
	}

	@Override
	public Number getLowerWarningLimit() {
		return ((double[])lowerWarningLimitPV.getValue())[0];
	}

	@Override
	public void setLowerWarningLimit(Number lowerWarningLimit) {
		lowerWarningLimitPV.setValue(new double[]{lowerWarningLimit.doubleValue()});
	}

	@Override
	public Number getUpperAlarmLimit() {
		return ((double[])upperAlarmLimitPV.getValue())[0];
	}

	@Override
	public void setUpperAlarmLimit(Number upperAlarmLimit) {
		upperAlarmLimitPV.setValue(new double[]{upperAlarmLimit.doubleValue()});
	}

	@Override
	public Number getUpperCtrlLimit() {
		return ((double[])upperCtrlLimitPV.getValue())[0];
	}

	@Override
	public void setUpperCtrlLimit(Number upperCtrlLimit) {
		upperCtrlLimitPV.setValue(new double[]{upperCtrlLimit.doubleValue()});
	}

	@Override
	public Number getUpperDispLimit() {
		return ((double[])upperDispLimitPV.getValue())[0];
	}

	@Override
	public void setUpperDispLimit(Number upperDispLimit) {
		upperDispLimitPV.setValue(new double[]{upperDispLimit.doubleValue()});
	}

	@Override
	public Number getUpperWarningLimit() {
		return ((double[])upperWarningLimitPV.getValue())[0];
	}

	@Override
	public void setUpperWarningLimit(Number upperWarningLimit) {
		upperWarningLimitPV.setValue(new double[]{upperWarningLimit.doubleValue()});
	}
}
