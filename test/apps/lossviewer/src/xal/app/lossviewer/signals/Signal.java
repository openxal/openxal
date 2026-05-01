package xal.app.lossviewer.signals;


public interface Signal {
	public void close();
	public void start();
	public String getName();
	public int getID();
        public double doubleValue();
        public int getHistorySize();
        public void setHistorySize(int size);
        public boolean setValue(double v);

	
}
