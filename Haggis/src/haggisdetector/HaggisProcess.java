package haggisdetector;

import java.awt.Color;

import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import haggisdetector.io.HaggisBinaryStore;
import haggisdetector.io.HaggisSQLLogging;
import haggisdetector.swing.HaggisOverlayGraphics;
import haggisdetector.swing.HaggisSymbolManager;

/**
 * All PAMGuard detectors have one or more PamProcess's. These do the actual work of
 * detecting the sound you're interested in. The process shouldn't contain any graphics,
 * just the maths you want to do to detect something. It will most likely subscribe to
 * data generated from some upstream process, in this case an FFT module that will send
 * in spectrogram data. The Process will also have an output PamDataBlock, into which it
 * will send any detections it's made.
 * @author dg50
 *
 */
public class HaggisProcess extends PamProcess {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 12, 12, true, Color.BLACK, Color.pink);

	/**
	 * Reference back to the PamContolledUnit for this module. We'll need this
	 * for getting important parameters.
	 */
	private HaggisControl haggisControl;


	/**
	 * Classifier for candidate detections
	 */
	private HaggisClassifier haggisClassifier;

	/**
	 * Reference to the FFT source data block we're going to subscribe to
	 */
	private FFTDataBlock fftDataBlock;


	/**
	 * Datablock for output data for any detections we make.
	 */
	private HaggisDataBlock outputDataBlock;

	/**
	 * data block used exclusively for parsing data on SNR levels to
	 * plug in display panels
	 */
	private PamDataBlock<BackgroundDataUnit> backgroundDataBlock;

	/**
	 * bitmap of channels in use.
	 */
	private int usedChannels; // bitmap of channels being analysed.

	/**
	 * reference to a list of detectors handling data from a single channel each. Each will
	 * operate independently, but will output data into a common data block.
	 */
	private ChannelDetector[] channelDetectors;

	/**
	 * Frequency bins for energy summation.
	 */
	private int bin1, bin2;

	/**
	 * detection threshold as a simpel ratio (not dB !)
	 */
	private double thresholdRatio;

	/**
	 * constatns for background update.
	 */
	private double backgroundUpdateConstant, backgroundUpdateConstant1;

	/**
	 * At some point we'll need to get back to the original ADC data
	 * and hydrophone information in order to convert amplitude data
	 * to dB re 1 micropascal - so we'll need daqProcess.
	 */
	private AcquisitionProcess daqProcess;

	/**
	 * Constructor called from a PamControlledUnit. This will set up everything required
	 * by the detector, such as output data blocks and their graphics components (though no actual
	 * graphics in this class). It also adds binary storage and database storage to the process output
	 * data.
	 * <br>
	 * The constructor does not attempt to subscribe to the source data block. this is because the source
	 * module may not have been created yet, so that will happen in a call to prepareProcess  which will
	 * get called automatically once all PAMGuard modules are loaded, or whenever the configuration of
	 * this detector is changes.
	 * @param haggisControl Reference back to PamControlledUnit
	 */
	public HaggisProcess(HaggisControl haggisControl) {
		// you must call the constructor from the super class.
		super(haggisControl, null);

		// keep a reference to the PamControlledUnit contolling this process.
		this.haggisControl = haggisControl;

		haggisClassifier = new HaggisClassifier(haggisControl);

		/* create an output data block and add to the output block list so that other detectors
		 * can see it. Also set up an overlay graphics class for use with the data block.
		 *
		 * My preferred method now days is subclass off PamDataBlock to some other class
		 * I may even make PamDataBlock abstract one day, to force people to do this.
		 */
		outputDataBlock = new HaggisDataBlock(haggisControl, haggisControl.getUnitName(), this, 0);
		/*
		 * Swing graphics which can overlay data on spectrogram, maps, etc.
		 */
		outputDataBlock.setOverlayDraw(new HaggisOverlayGraphics(outputDataBlock));
		/**
		 * A symbol manager, which allows the user to select types and colours of symbols for
		 * all PAMGuard displays.
		 */
		outputDataBlock.setPamSymbolManager(new HaggisSymbolManager(haggisControl, outputDataBlock));
		/**
		 * Database logging
		 */
		outputDataBlock.SetLogging(new HaggisSQLLogging(haggisControl, outputDataBlock));
		/*
		 * Binary file storage. Normally it's not entirely sensible to have both binary and database
		 * storage. For Haggis, given the expected low data rate, and the simplicity of the data, 
		 * database storage is the most sensible, but binary storage is inluded as an example. 
		 */
		outputDataBlock.setBinaryDataSource(new HaggisBinaryStore(haggisControl, outputDataBlock));
		/*
		 * Register the datablock, so that other PAMGuard processes and displays can find it by
		 * looping through PamControlledUnits, their PamProcesses, etc.
		 */
		addOutputDataBlock(outputDataBlock);

		/*
		 * Create a datablock for background measurements, but don't register it - it will be found anyway
		 * by the display plug in that uses it.
		 */
		backgroundDataBlock = new PamDataBlock<>(BackgroundDataUnit.class,
				haggisControl.getUnitName(), this, 0);
	}

	@Override
	public void pamStart() {

		for (int i = 0; i <= PamUtils.getHighestChannel(usedChannels); i++) {
			if (((1<<i) & usedChannels) > 0) {
				channelDetectors[i].pamStart();
			}
		}

	}

	@Override
	public void pamStop() {
		// this method is abstract in the superclass and must therefore be overridden
		// even though it doesn't do anything.
	}

	/**
	 * PamProcess already implements PamObserver, enabling it to subscribe
	 * to PamDataBlocks and get notifications of new data. So that the
	 * Pamguard Profiler can monitor CPU usage in each module, the function
	 * update(PamObservable o, PamDataUnit arg) from the PAmObserver interface
	 * is implemented in PamProcess. PamProcess.update handles profiling
	 * tasks and calls newData where the developer can process newly
	 * arrived data.
	 */
	@Override
	public void newData(PamObservable observable, PamDataUnit dataUnit) {
		/*
		 * Cast to an FFTDataUnit. The way this detector is set up, it's only possible
		 * to subscribe to FFT data, so this is safe. Some detectors may take multiple
		 * input types, e.g. raw audio OR FFT data, in which case you'll need to check
		 * what's coming in, either by looking at the class of the dataUnit, or the
		 * observable, which will be the datablock we've subscribed to (in this case
		 * fftDataBlock)
		 */
		FFTDataUnit fftDataUnit = (FFTDataUnit) dataUnit;
		// see which channel it's from
		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		// check that a detector has been instantiated for that channel
		if (channelDetectors == null ||
				channelDetectors.length <= chan ||
				channelDetectors[chan] == null) {
			return; // this shouldn't ever happen !
		}

		/**
		 * Call the detector that's working with data from that channel.
		 */
		channelDetectors[chan].newData(observable, fftDataUnit);
	}

	@Override
	/**
	 * One of the 'annoyances' of the very flexible Pamguard model is that
	 * it's impossible to say at development time exactly what order
	 * pamguard models will be generated in. This example is dependent on
	 * FFT data but it's possible (although unlikely) that the FFT producing
	 * module will be created after this module. Therefore there isn't much point in
	 * looking for the FFT data in this modules constructor. When new modules
	 * are added, a notification is sent to all PamControlledUnits. I've arranged
	 * for HaggisController to call prepareProcess() when this happens so
	 * that we can look around and try to find an FFT data block.
	 */
	public void prepareProcess() {
		super.prepareProcess();
		/*
		 * Need to hunt around now in the Pamguard model and try to find the FFT
		 * data block we want and then subscribe to it. This is now easy since
		 * everything is referred to by a long dataname.
		 */

		HaggisParameters params = haggisControl.getHaggisParameters();

		fftDataBlock = (FFTDataBlock) PamController.getInstance().getDataBlockByLongName(params.fftDataName);
		if (fftDataBlock == null) {
			// try to find any fft data.
			fftDataBlock = (FFTDataBlock) PamController.getInstance().getDataBlock(FFTDataUnit.class, 0);
		}

		setParentDataBlock(fftDataBlock);

		if (fftDataBlock == null) {
			return;
		}

		/*
		 * usedChannels will be a combination of what we want and what's available.
		 */
		usedChannels = fftDataBlock.getChannelMap() & params.channelList;

		/*
		 * Tell the output data block which channels data may come from.
		 */
		outputDataBlock.setChannelMap(usedChannels);

		/**
		 * allocate references to a list of detectors - one for each channel used.
		 */
		channelDetectors = new ChannelDetector[PamUtils.getHighestChannel(usedChannels)+1];
		for (int i = 0; i <= PamUtils.getHighestChannel(usedChannels); i++) {
			if (((1<<i) & usedChannels) > 0) {
				channelDetectors[i] = new ChannelDetector(i);
			}
		}

		/*
		 * The following could be done every time new data arrive, but
		 * it's quicker to do them once here - not that it makes a lot of
		 * difference since these operations are v. quick compared to
		 * the actual fft calculations.
		 */
		/*
		 * work out the bins for energy summation and check they are valid.
		 */
		bin1 = (int) (fftDataBlock.getFftLength() / fftDataBlock.getSampleRate() * params.lowFreq);
		bin2 = (int) (fftDataBlock.getFftLength() / fftDataBlock.getSampleRate() * params.highFreq);
		bin1 = Math.min(bin1, fftDataBlock.getFftLength()/2-1);
		bin1 = Math.max(bin1, 0);
		bin2 = Math.min(bin2, fftDataBlock.getFftLength()/2-1);
		bin2 = Math.max(bin2, 0);

		/*
		 * convert the threshold which was set in dB to a simple energy ratio
		 */
		thresholdRatio = Math.pow(10., params.threshold/10.);

		/*
		 * work out decay constants for background update - this will be a decaying average over time.
		 *
		 */
		double secsPerBin = fftDataBlock.getFftHop() / fftDataBlock.getSampleRate();
		backgroundUpdateConstant = secsPerBin / params.backgroundTimeConstant;
		backgroundUpdateConstant1 = 1.0 - backgroundUpdateConstant;

		/*
		 * This should always go back to an Acquisition process by working
		 * back along the chain of data blocks and pamprocesses.
		 */
		try {
			daqProcess = (AcquisitionProcess) getSourceProcess();
		}
		catch (ClassCastException ex) {
			daqProcess = null;
		}

	}

	/**
	 * @return the backgroundDataBlock
	 */
	public PamDataBlock<BackgroundDataUnit> getBackgroundDataBlock() {
		return backgroundDataBlock;
	}

	/**
	 * Since the detector may be running on several channels, make a sub class for the actual detector
	 * code so that multiple instances may be created.
	 * @author Doug
	 *
	 */
	class ChannelDetector {

		/*
		 * Which channel is this detector operating on
		 */
		int channel;

		/*
		 * currently above threshold or not ?
		 */
		boolean detectionOn = false;

		/*
		 * measure of background noise
		 */
		double background = 0;

		/*
		 * how many times have new data arrived.
		 */
		int callCount = 0;

		/*
		 * use the first setupCount datas to set the background
		 * before starting to do any detection.
		 */
		static final int setupCount = 20;

		/*
		 * some information about each detection.
		 */
//		long detectionStart;
		long detectionEndSample;
		long detectionStartSample;
		double detectionEnergy;
		int detectionSliceCount;

		public ChannelDetector(int channel) {
			this.channel = channel;
		}

		public void pamStart() {
			background = 0;
			detectionOn = false;
			callCount = 0;
		}

		/**
		 * Performs the same function as newData in the outer class, but this
		 * time it should only ever get called with data for a single channel.
		 * For the first few calls, it just updates the background by taking a straight mean
		 * of the energy values. After that it calculates a decaying average of
		 * the background.
		 * @param o
		 * @param arg
		 */
		public void newData(PamObservable o, FFTDataUnit arg) {
		  double energy = energySum(arg.getFftData(), bin1, bin2);
		  if (callCount++ < setupCount) {
			  background += energy / setupCount;
			  return;
		  }
		  boolean overThresh = (energy > background * thresholdRatio);

		  /*
		   * Need to put the dB over background measures into a datablock so that the
		   * spectrogram display plug ins can subscribe to it and plot it.
		   */
		  double lastDbValue = 10 * Math.log10(energy / background);
		  BackgroundDataUnit bdu = new BackgroundDataUnit(absSamplesToMilliseconds(arg.getStartSample()),
				  arg.getChannelBitmap(), arg.getStartSample(), 0, lastDbValue);
		  getBackgroundDataBlock().addPamData(bdu);

		  background *= backgroundUpdateConstant1;
		  background += (energy * backgroundUpdateConstant);

		  if (overThresh && !detectionOn) {
			  startDetection(arg, energy);
		  }
		  else if (overThresh && detectionOn) {
			  continueDetection(arg, energy);
		  }
		  else if (!overThresh && detectionOn) {
			  endDetection();
		  }
		  // don't need to handle overThresh == false && detectionOn == false

		}

		private double energySum(ComplexArray complexArray, int bin1, int bin2) {
			double e = 0;
			for (int i = bin1; i <= bin2; i++) {
				e += complexArray.magsq(i);
			}
			return e;
		}

		/**
		 * Called when the detection band energy exceeds threshold to start a detection.
		 * @param dataUnit
		 * @param energy
		 */
		private void startDetection(FFTDataUnit dataUnit, double energy) {
			detectionStartSample = dataUnit.getStartSample();
			detectionEndSample = detectionStartSample + dataUnit.getSampleDuration();
			detectionEnergy = energy;
			detectionSliceCount = 1;
			detectionOn = true;
		}

		/**
		 * Called whenever a detection has been started and the level is still above
		 * threshold.
		 * @param dataUnit
		 * @param energy
		 */
		private void continueDetection(FFTDataUnit dataUnit, double energy) {
			detectionEndSample = dataUnit.getStartSample() + dataUnit.getSampleDuration();
			detectionEnergy += energy;
			detectionSliceCount += 1;
		}

		/**
		 * Called when the energy has dropped back below threshsold to carry out necessary
		 * tasks to end and clean up the detection.
		 */
		private void endDetection() {
			/*
			 * Create a new output data unit.
			 */
			HaggisDataUnit haggisDataUnit  = new HaggisDataUnit(absSamplesToMilliseconds(detectionStartSample),
					1<<channel, detectionStartSample, detectionEndSample-detectionStartSample);

			/*
			 * fill in detection information (this could have been added to the HaggisDataUnit constructor)
			 */
			haggisDataUnit.setFrequency(new double[]{haggisControl.getHaggisParameters().lowFreq,
					haggisControl.getHaggisParameters().highFreq});

			/*
			 * now work out the energy in dB re 1 micropascal. This requires knowledge from
			 * both the hydrophone array and from the digitiser. Fortunately, the Acquisitionprocess, which
			 * is a subclass of PamProcess can handle all this for us.
			 */
			double aveAmplitude = detectionEnergy / detectionSliceCount;
			haggisDataUnit.setMeasuredAmpAndType(daqProcess.fftAmplitude2dB(aveAmplitude,
					channel, fftDataBlock.getSampleRate(), fftDataBlock.getFftLength(),
					true, false), DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA);

			// and run the classifier
			HaggisClass haggisClass = haggisClassifier.classify(haggisDataUnit);

			/*
			 * This particular classifier can return null, in which case it's not a haggis and we don't
			 * want to store it so only do this if the classifier returned non null data
			 */
			if (haggisClass != null) {
				// set the class / sub species in the data
				haggisDataUnit.setHaggisClass(haggisClass);
				/*
				 *  Send the data to the output data block. This will automatically send
				 *  the data off for storage in the database and/or binary store and make it
				 *  available to any displays compatible with information in HaggisOverlayDraw.
				 */
				outputDataBlock.addPamData(haggisDataUnit);
			}

			detectionOn = false;
		}

	}

}
