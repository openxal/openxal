package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import xal.app.experimentautomator.core.EADocument;

public class StopButtonActionListener implements ActionListener {

	private EADocument EADoc;

	public StopButtonActionListener(EADocument eadoc) {
		EADoc = eadoc;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		EADoc.requestPause();
	}
}
