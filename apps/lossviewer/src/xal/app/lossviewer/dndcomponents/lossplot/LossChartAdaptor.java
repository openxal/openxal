package xal.app.lossviewer.dndcomponents.lossplot;

import gov.sns.tools.apputils.*;

public interface LossChartAdaptor extends ChartPopupAdaptor {
	
	public void setVisible(boolean isVisible);
	public boolean isVisible();
	public void setLimitVisible(boolean isVisible);
	public boolean isLimitVisible();
	
}
