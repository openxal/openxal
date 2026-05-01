/*
 * OpticsListener.java
 *
 * Created on 08/20/2003
 */

package xal.app.rocs;

import xal.ca.*;

import java.util.List;
import java.util.Date;

/**
 * update the display to reflect the latest data
 *
 * @author  tap
 */
public interface OpticsListener {
    public void updateQuadK(Object sender, double[] k);
    public void updateSextK(Object sender, double[] k);
    public void updateTunes(Object sender, double tunex, double tuney);
    public void updateChroms(Object sender, double chromx, double chromy);
    public void updatePhases(Object sender, double phasex, double phasey);
}

