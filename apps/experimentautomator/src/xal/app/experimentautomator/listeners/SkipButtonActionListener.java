package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.exception.CompletedException;
import xal.app.experimentautomator.exception.NotificationException;

public class SkipButtonActionListener implements ActionListener {

	private EADocument EADoc;

	public SkipButtonActionListener(EADocument eadoc) {
		EADoc = eadoc;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			EADoc.skipStep();
		} catch (NotificationException ex) {
			EADoc.displayError("Error", ex.getMessage());
		} catch (CompletedException ex) {
			EADoc.displayError("Error",
					"No more steps in Scan Table. May Re-Initialize.");
		}
	}
}
