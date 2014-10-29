package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.exception.NotificationException;

public class SaveButtonActionListener implements ActionListener {

	private EADocument EADoc;

	public SaveButtonActionListener(EADocument eadoc) {
		EADoc = eadoc;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {

			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@Override
				public Void doInBackground() throws NotificationException {

					EADoc.saveData();

					return null;
				}

				@Override
				public void done() {
					try {
						this.get();
					} catch (InterruptedException e) {
						EADoc.displayError("Error", e.getMessage());
					} catch (ExecutionException e) {
						EADoc.displayError("Error", e.getMessage());
					}
				}
			};

			worker.execute();

		} catch (Exception ex) {
			EADoc.displayError("Error", ex.getMessage());
		}
	}
}
