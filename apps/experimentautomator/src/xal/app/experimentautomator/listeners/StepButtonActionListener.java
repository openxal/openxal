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

public class StepButtonActionListener implements ActionListener {

	private EADocument EADoc;

	public StepButtonActionListener(EADocument eadoc) {
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
						Integer acquisitionCase = EADoc.executeStep();
						if (acquisitionCase
								.equals(EADocument.ACQUISITION_CASE_PAUSE)) {
							EADoc.pauseForAcquisition();
						}

						EADoc.acquireStep();
						EADoc.completeStep();

					} catch (NotificationException ex) {
						EADoc.displayError("Error", ex.getMessage());
					} catch (CompletedException ex) {
						EADoc.setStatusText("Experiment Complete");
						EADoc.displayError("Error",
								"No more steps in Scan Table. May Re-Initialize.");
					} catch (PausedException ex) {
						// Do Nothing???
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

		} catch (Exception ex) {
			EADoc.displayError("Error", ex.getMessage());
		}
	}
}
