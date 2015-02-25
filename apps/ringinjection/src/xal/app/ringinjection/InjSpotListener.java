/*
 * OpticsListener.java
 *
 * Created on 11/25/2003
 */

package xal.app.ringinjection;

import xal.ca.*;

import java.util.List;
import java.util.Date;

/**
 * update the display to reflect the latest data
 *
 * @author  smc
 */
public interface InjSpotListener {
	public void updateInjSpot(double[] params);
}

