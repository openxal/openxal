package xal.app.experimentautomator.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.ExecutionException;

import javax.swing.SwingWorker;

import xal.app.experimentautomator.core.EADocument;
import xal.app.experimentautomator.exception.CompletedException;
import xal.app.experimentautomator.exception.NotificationException;
import xal.app.experimentautomator.exception.PausedException;
import xal.model.ModelException;

public class StartButtonActionListener implements ActionListener {

	private EADocument EADoc;

	/**
	 * 
	 * @param eadoc
	 */
	public StartButtonActionListener(EADocument eadoc) {
		EADoc = eadoc;
	}

	/**
	 * Much of this mess is in place only to use a SwingWorker class allowing
	 * concurrent threads to operate. This is in order to leave the GUI
	 * accessible while the experiment runs.
	 * 
	 * @author rnewhouse
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		try {

			EADoc.unsetPause();
			EADoc.disableButtons();
			SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {

				@Override
				public Void doInBackground() throws ModelException,
						NotificationException {
					try {

						// The only block that's actually being executed
						while (true) {
							Integer acquisitionCase = EADoc.executeStep();
							if (acquisitionCase
									.equals(EADocument.ACQUISITION_CASE_PAUSE)) {
								EADoc.pauseForAcquisition();
							}
							EADoc.acquireStep();
							EADoc.completeStep();
						}

					} catch (NotificationException ex) {
						EADoc.displayError("Error", ex.getMessage());
					} catch (CompletedException ex) {
						EADoc.setStatusText("Experiment Complete");
						EADoc.setMessageText("Experiment Complete");
					} catch (PausedException ex) {
						EADoc.setStatusText("Experiment Paused");
					}
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
					} finally {
						EADoc.enableButtons();
					}
				}
			};

			worker.execute();
			EADoc.setStatusText("Experiment Running");

		} catch (Exception ex) {
			EADoc.displayError("Error", ex.getMessage());
		}

	}
}
