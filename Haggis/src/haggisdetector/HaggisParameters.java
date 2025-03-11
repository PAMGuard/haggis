package haggisdetector;

import java.awt.Color;
import java.io.Serializable;

import PamView.PamSymbol;
import PamView.PamSymbolType;

/**
 * for each module, it's best to keep all parameters controlling that module
 * in a single class which must be serialisable so that the Pamguard settings 
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
	

	@Override
	/**
	 * overriding the clone function enables you to clone (copy) 
	 * these parameters easily in your code without having to 
	 * continually cast to (WorkshopProcessParameters) or handle
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
