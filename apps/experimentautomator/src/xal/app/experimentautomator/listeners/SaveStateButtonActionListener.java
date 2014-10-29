package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.exception.NotificationException;

public class SaveStateButtonActionListener implements ActionListener {

	private EADocument EADoc;

	public SaveStateButtonActionListener(EADocument eadoc) {
		EADoc = eadoc;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			EADoc.saveState();
		} catch (NotificationException e1) {
			EADoc.displayError("Error", e1.getMessage());
		}
	}
}
