package haggisdetector;

import PamDetection.PamDetection;
import PamguardMVC.PamDataUnit;

public class HaggisDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection {

	/**
	 * Main Constructor
	 * 
	 * @param timeMilliseconds the time in milliseconds when the detection started
	 * @param channelBitmap the channel bitmap for this detection
	 * @param startSample the starting sample number
	 * @param duration the duration of the detection, in number of samples
	 */
	public HaggisDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);

	}


}
