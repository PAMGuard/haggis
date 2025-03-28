package haggisdetector;

import java.io.Serializable;

/**
 * for each module, it's best to keep all parameters controlling that module
 * in a single class which must be serializable so that the Pamguard settings
 * manager can save it between runs.
 * @author Doug
 * @see PamController.PamSettingManager
 *
 */
public class HaggisParameters implements Serializable, Cloneable {

	static final long serialVersionUID = 1;

	/**
	 * Use names of FFT data blocks, not indexes,
	 * they are more robust to changes.
	 */
	public String fftDataName;

	/**
	 * Low frequency for energy summation
	 */
	public int lowFreq = 1000;

	/**
	 * High frequency for energy summation
	 */
	public int highFreq = 10000;

	/**
	 * time constant for background noise measurement.
	 */
	public double backgroundTimeConstant = 10;

	/**
	 * Detection threshold in dB.
	 */
	public double threshold = 6;

	/**
	 * Bitmap of channels to be used - use all available.
	 */
	public int channelList = 0xFFFF;

	/**
	 * Minimum confidence score for the classifier.
	 */
	public double minConfidence = 0.5;
	
	/**
	 * Keep / store unclassified candidate sounds. This effectively 
	 * turns it into a quite generic energy detector. 
	 */
	public boolean keepUnclassified = false;
	
	/**
	 * Return 0 from the classifier if the config is not in the highlands. 
	 */
	public boolean scotlandOnly = false;


	@Override
	/**
	 * overriding the clone function enables you to clone (copy)
	 * these parameters easily in your code without having to
	 * continually cast to (HaggisParameters) or handle
	 * the exception CloneNotSupportedException.
	 */
	public HaggisParameters clone() {
		try {
			return (HaggisParameters) super.clone();
		}
		catch (CloneNotSupportedException ex) {
			ex.printStackTrace();
			return null;
		}
	}

}
