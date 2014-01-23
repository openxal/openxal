package gov.sns.apps.lossviewer2.signals;

public interface SignalValue {
	public long getTimestamp();
	public String getStringValue();
	public Signal getSignal();

	
}
