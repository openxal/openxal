package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.exception.NotificationException;
import xal.model.ModelException;

public class InitButtonActionListner implements ActionListener {

	private EADocument EADoc;

	public InitButtonActionListner(EADocument eadoc) {
		EADoc = eadoc;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		try {
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@Override
				public Void doInBackground() throws ModelException,
						NotificationException {

					EADoc.initialize();
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

		} catch (Exception e) {
			EADoc.displayError("Error", e.getMessage());
		}
	}
}
