package xal.app.lossviewer.signals;


public class ScalarSignalValue implements SignalValue {
	
	private Signal signal = null;
	
	public Signal getSignal() {
			
		return signal;
	}
	
	

	public String getStringValue() {
		
		return String.valueOf(value);
	}
	
	
	private double value;
	
	private long tst;
	
	public ScalarSignalValue(final Signal s,final long tst, final double v) {
		this.value = v;
		this.tst = tst;
		signal=s;
	}
	
	public double getValue() {
		return value;
	}
	public long getTimestamp() {
		return tst;
	}
	public String toString(){
		return getSignal().getName()+"("+tst+","+value+")";
	}
}
