package xal.app.lossviewer.dndcomponents.lossplot;

import xal.tools.apputils.ChartPopupAdaptor;

public interface LossChartAdaptor extends ChartPopupAdaptor {
	
	public void setVisible(boolean isVisible);
	public boolean isVisible();
	public void setLimitVisible(boolean isVisible);
	public boolean isLimitVisible();
	
}
