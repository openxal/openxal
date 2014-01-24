package xal.app.lossviewer.signals;



public class ArraySignalValue implements SignalValue {
	
	/**
	 * Method getSignal
	 *
	 * @return   a Signal
	 *
	 */
	public Signal getSignal() {
		// TODO: Implement this method
		return null;
	}
	
	
	
	public String getStringValue() {
		if(value==null)
			return null;
		StringBuffer buf = new StringBuffer();
		for (float e : value) {
			buf.append(e+" ");
		}
		return buf.toString();
	}
	
	
	private float[] value;
	
	private long tst;
	
	public ArraySignalValue(final long tst, final float[] v) {
		this.value = v;
		this.tst = tst;
	}
	
	public float[] getValue() {
		return value;
	}
	public long getTimestamp() {
		return tst;
	}
	public String toString(){
		return "("+tst+","+value+")";
	}
}
