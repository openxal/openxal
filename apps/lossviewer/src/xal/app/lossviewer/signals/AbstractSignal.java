package xal.app.lossviewer.signals;

import xal.app.lossviewer.*;

public abstract class AbstractSignal implements Signal {
	private int ID;
	private String name;
        private int historySize = 0;
	
	protected Dispatcher dispatcher;
	


	public void setID(int iD) {
		ID = iD;
	}
        
        public int getHistorySize(){
            return historySize;
        }
        public void setHistorySize(int size){
            historySize=size;
        }
	

	public int getID() {
		return ID;
	}
	

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
	public void setDispatcher(Dispatcher sd){
		this.dispatcher=sd;
	}

        
}
