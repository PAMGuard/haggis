package haggisdetector;

/**
 * Class for Haggis classification information. Better to put all this in one 
 * class than to pass multiple parameters into the HaggisDataUnit
 * @author dg50
 *
 */
public class HaggisClass {

	private HaggisTypes haggisType;

	private double score;

	public HaggisClass(HaggisTypes haggisType, double score) {
		super();
		this.haggisType = haggisType;
		this.score = score;
	}

	/**
	 * @return the haggisType
	 */
	public HaggisTypes getHaggisType() {
		return haggisType;
	}

	/**
	 * @return the score
	 */
	public double getScore() {
		return score;
	}


}
