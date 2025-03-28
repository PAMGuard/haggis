package haggisdetector;

import PamDetection.PamDetection;
import PamguardMVC.AcousticDataUnit;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataUnit;

public class HaggisDataUnit extends PamDataUnit<PamDataUnit,PamDataUnit> implements PamDetection, AcousticDataUnit {


	private HaggisClass haggisClass;

	/**
	 * Main Constructor for new detections
	 *
	 * @param timeMilliseconds the time in milliseconds when the detection started
	 * @param channelBitmap the channel bitmap for this detection
	 * @param startSample the starting sample number
	 * @param duration the duration of the detection, in number of samples
	 */
	public HaggisDataUnit(long timeMilliseconds, int channelBitmap, long startSample, long duration) {
		super(timeMilliseconds, channelBitmap, startSample, duration);
	}

	/**
	 * Constructor used when reading back from binary files. 
	 * @param dataUnitBaseData
	 * @param hagClass
	 */
	public HaggisDataUnit(DataUnitBaseData dataUnitBaseData, HaggisClass hagClass) {
		super(dataUnitBaseData);
		setHaggisClass(hagClass);
	}

	/**
	 * @return the haggisClass
	 */
	public HaggisClass getHaggisClass() {
		return haggisClass;
	}

	/**
	 * @param haggisClass the haggisClass to set
	 */
	public void setHaggisClass(HaggisClass haggisClass) {
		this.haggisClass = haggisClass;
	}

	@Override
	public String getSummaryString() {
		// use standard string and add Haggis specific information. 
		String baseStr =  super.getSummaryString();
		if (haggisClass == null) {
			return baseStr;
		}
		baseStr += String.format("Sub type: %s", haggisClass.getHaggisType().toLowerString());
		baseStr += String.format("<br>Class score: %4.2f", haggisClass.getScore());
		return baseStr;
	}


}
