package xal.app.sclmonitor;

import java.awt.event.*;

/**
 * This class causes the table to update at a specified frequency that is
 * presently hardwired to 2 Hz. Only the table cells pointing to channels are
 * updated.
 */

public class TableProdder extends Thread

{

	private RFTableModel m_iodtm;

	private int m_msecs = 500;

	private int last_row;

	public TableProdder(RFTableModel iodtm) {
		m_iodtm = iodtm;
		// last row is plot buttons
//		last_row = iodtm.getRowCount() - 1;
		last_row = iodtm.getRowCount();
	}

	public void run() {

		while (true) {
			try {
				sleep(m_msecs);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			m_iodtm.fireTableRowsUpdated(0, last_row);

		}
	}
}
