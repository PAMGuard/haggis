package haggisdetector;

/**
 * Main species subtypes for the Scottish Haggis that can be output
 * from the Haggis Classifier.
 * see https://en.wikipedia.org/wiki/Wild_haggis
 * @author dg50
 *
 */
public enum HaggisTypes {
	UNKNOWN, CLOCKWISE, ANTICLOCKWISE;

	/**
	 * Leave the toString function alone so that it can be 
	 * used to write Strings to storage that will be recognised
	 * by valueOf() function. 
	 * @return
	 */
	public String toLowerString() {
		switch (this) {
		case ANTICLOCKWISE:
			return "Anticlockwise";
		case CLOCKWISE:
			return "Clockwise";
		case UNKNOWN:
			return "Unknown";
		default:
			return null;
		}
	}

	/**
	 * Default selection for data selectors. 
	 * @return
	 */
	public boolean defaultSelection() {
		switch (this) {
		case ANTICLOCKWISE:
		case CLOCKWISE:
			return true;
		case UNKNOWN:
			return false;
		default:
			return false;		
		}
	}
}
