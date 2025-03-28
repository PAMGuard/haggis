package haggisdetector;

import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnit2D;

public class BackgroundDataUnit extends DataUnit2D implements AcousticDataUnit {

	private double background;

	/**
	 * Main Constructor
	 *
	 * @param timeMilliseconds the time in milliseconds when the detection started
	 * @param channelBitmap the channel bitmap for this detection
	 * @param startSample the starting sample number
	 * @param duration the duration of the detection, in number of samples
	 * @param background background measurement, in dB
	 */
	public BackgroundDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration, double background) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
		this.background = background;
	}

	public double getBackground() {
		return background;
	}

	public void setBackground(double background) {
		this.background = background;
	}

	/* (non-Javadoc)
	 * @see PamguardMVC.DataUnit2D#getMagnitudeData()
	 */
	@Override
	public double[] getMagnitudeData() {
		return null;
	}

}
