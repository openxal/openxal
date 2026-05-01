package xal.app.quadshaker;

/**
 *  The ScanObject class represents the object of scanning procedure which is
 *  the client of the ScanRunner class. The ScanRunner class calls the methods
 *  of the ScanObject class instance in certain order. To use the ScanObject
 *  class it should be expanded, and its methods should be overridden.
 *
 *@author     shishlo
 */
public class ScanObject {

	private ScanRunner scanRunner = null;

	/**
	 *  Constructor for the ScanObject object. It is empty, so subclasses may avoid
	 *  calling super() in constructors.
	 */
	public ScanObject() { }

	/**
	 *  Sets the scanRunner for the ScanObject object. The method is final, and it
	 *  cannot be overridden.
	 *
	 *@param  scanRunner  The new scanRunner
	 */
	public final void setScanRunner(ScanRunner scanRunner) {
		this.scanRunner = scanRunner;
		if(this != scanRunner.getScanObject()) {
			scanRunner.setScanObject(this);
		}
	}

	/**
	 *  Returns the scanRunner of the ScanObject object. The method is final, and
	 *  it cannot be overridden.
	 *
	 *@return    The scanRunner
	 */
	public final ScanRunner getScanRunner() {
		return scanRunner;
	}


	/**
	 *  It is empty method here. It should be overridden. User will put inside the
	 *  initialization procedure that should be performed before scan.
	 */
	public void initScan() {
	}

	/**
	 *  It is empty method here. It should be overridden. User will put inside the
	 *  necessary action before the start or resuming the scan. At this point it is
	 *  possible to have some part of the scan already done. The information about
	 *  the scan state should be irrelevant here.
	 */
	public void startScan() { }

	/**
	 *  This method will always return false here. It should be overridden by user.
	 *  The user should keep in mind that the step is finished only after accouting
	 *  (method accountStep()).
	 *
	 *@return    The true if the next step exist.
	 */
	public boolean nextStepExists() {
		return false;
	}

	/**
	 *  It is empty method here. It should be overridden. It should include the
	 *  actions before measuremets. This is the right place to set the value to the
	 *  scan variable. There will be a time gap (time step) between this method and
	 *  calls to validateStep() and accountStep() methods.
	 */
	public void makeStep() {
	}

	/**
	 *  This method will always return true here. It could be overridden by user if
	 *  it is necessary.
	 *
	 *@return    The true if the result is validated.
	 */
	public boolean validateStep() {
		return true;
	}

	/**
	 *  It is empty method here. It should be overridden. This method is called
	 *  after time=(time step) the makeStep() method. It is the right place to
	 *  measure the results and perform actions to go to the next step.
	 */
	public void accountStep() {
	}

	/**
	 *  It is empty method here. It should be overridden. The conditions that were
	 *  before the beginning of the scan should be restored.
	 */
	public void restoreInitialState() {
	}

	/**
	 *  It is empty method here. It should be overridden. It will be called if the
	 *  error happens.
	 */
	public void errorHappened() {
	}


	/**
	 *  It is empty method here. It could be overridden. It could be intermediate
	 *  stop. This fact can be found by using nextStepExists() method.
	 */
	public void scanFinished() {
	}


	/**
	 *  Returns the progress of the scan in percents. If user will not override
	 *  this method the result will be always zero.
	 *
	 *@return    The progress value in percents.
	 */
	public int getProgress() {
		return 0;
	}
}

