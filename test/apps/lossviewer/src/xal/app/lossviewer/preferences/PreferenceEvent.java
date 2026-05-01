package xal.app.lossviewer.preferences;


public class PreferenceEvent  {
	
	private String prefName;
	
	private Object source;
	
	private Object newValue;
	PreferenceEvent(String prefName, Object newValue, Object source){
		this.prefName = prefName;
		this.source=source;
		this.newValue=newValue;
		
	}
	
	public Object getSource(){
		return source;
	}
	public String getPreferenceName(){
		return prefName;
	}
	public String toString(){
		return source+"["+prefName+","+newValue+"]";
	}
}
