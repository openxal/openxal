package xal.app.experimentautomator.exception;

public class ThresholdException extends Exception {
    /** serialization ID */
    private static final long serialVersionUID = 1L;
    
	public Double expected;
	public Double actual;
	public String pvName;

	public ThresholdException(String pvName, String expected, String actual) {
		this.pvName = pvName;
		this.expected = Double.valueOf(expected);
		this.actual = Double.valueOf(actual);
	}

	public ThresholdException(String pvName, Double expected, Double actual) {
		this.pvName = pvName;
		this.expected = expected;
		this.actual = actual;
	}

}
