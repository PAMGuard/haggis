package haggisdetector;

import java.awt.Color;
import java.util.ArrayList;

import fftManager.Complex;
import fftManager.FFTDataBlock;
import fftManager.FFTDataUnit;
import haggisdetector.io.HaggisSQLLogging;
import haggisdetector.swing.HaggisOverlayGraphics;
import haggisdetector.swing.HaggisSymbolManager;
import Acquisition.AcquisitionProcess;
import PamController.PamController;
import PamUtils.PamUtils;
import PamUtils.complex.ComplexArray;
import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.DataUnitBaseData;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamProcess;
import clickDetector.tdPlots.ClickDetSymbolManager;

public class HaggisProcess extends PamProcess {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_SQUARE, 12, 12, true, Color.BLACK, Color.pink);

	/**
	 * Refefence to PamContolledUnit for this module
	 */
	private HaggisControl haggisControl;
	
	/**
	 * Reference to the FFT source data block
	 */
	private FFTDataBlock fftDataBlock;
	
	
	/**
	 * Datablock for output data.
	 */
	private HaggisDataBlock outputDataBlock;
	
	/*
	 * data block used exclusively for parsing data on SNR levels to 
	 * plug in display panels 
	 */
	private PamDataBlock<BackgroundDataUnit> backgroundDataBlock;
	
	/**
	 * bitmap of channels in use. 
	 */
	private int usedChannels; // bitmap of channels being analysed. 
	
	/**
	 * reference to a list of detectors handling data from a single channel each. 
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
	
	public HaggisProcess(HaggisControl haggisContro) {
		// you must call the constructor from the super class. 
		super(haggisContro, null);
		
		// keep a reference to the PamControlledUnit contolling this process. 
		this.haggisControl = haggisContro;
		
		/* create an output data block and add to the output block list so that other detectors
		 * can see it. Also set up an overlay graphics class for use with the data block. 
		 * 
		 * My preferred method now days is subclass off PamDataBlock to some other class
		 * I may even make PamDataBlock abstract one day, to force people to do this. 
		 */
		outputDataBlock = new HaggisDataBlock(haggisContro.getUnitName(), this, 0);
		outputDataBlock.setOverlayDraw(new HaggisOverlayGraphics(outputDataBlock));
		outputDataBlock.setPamSymbolManager(new HaggisSymbolManager(haggisContro, outputDataBlock));
		outputDataBlock.SetLogging(new HaggisSQLLogging(haggisContro, outputDataBlock));
		addOutputDataBlock(outputDataBlock);
		
		/*
		 * Create a datablock for background measurements, but don't register it - it will be found anyway 
		 * by the display plug in that uses it. 
		 */
		backgroundDataBlock = new PamDataBlock<BackgroundDataUnit>(BackgroundDataUnit.class, 
				haggisContro.getUnitName(), this, 0);
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

	@Override
	/**
	 * PamProcess already implements PamObserver, enabling it to subscribe
	 * to PamDataBlocks and get notifications of new data. So that the 
	 * Pamguard Profiler can monitor CPU usage in each module, the function
	 * update(PamObservable o, PamDataUnit arg) from the PAmObserver interface
	 * is implemented in PamProcess. PamProcess.update handles profiling
	 * tasks and calls newData where the developer can process newly 
	 * arrived data. 
	 */
	public void newData(PamObservable o, PamDataUnit arg) {
		// see which channel it's from
		FFTDataUnit fftDataUnit = (FFTDataUnit) arg;
		int chan = PamUtils.getSingleChannel(fftDataUnit.getChannelBitmap());
		// check that a detector has been instantiated for that channel
		if (channelDetectors == null || 
				channelDetectors.length <= chan || 
				channelDetectors[chan] == null) {
			return;
		}
		
		channelDetectors[chan].newData(o, fftDataUnit);
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
	 * for WorkshopController to call prepareProcess() when this happens so 
	 * that we can look around and try to find an FFT data block. 
	 */
	public void prepareProcess() {
		super.prepareProcess();
		/*
		 * Need to hunt around now in the Pamguard model and try to find the FFT
		 * data block we want and then subscribe to it. This is now easy since
		 * everything is referred to by a long dataname. 
		 */

		fftDataBlock = (FFTDataBlock) PamController.getInstance().getDataBlockByLongName(haggisControl.getWorkshopProcessParameters().fftDataName);
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
		usedChannels = fftDataBlock.getChannelMap() & haggisControl.getWorkshopProcessParameters().channelList;
		
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
		bin1 = (int) (fftDataBlock.getFftLength() / fftDataBlock.getSampleRate() * 
				haggisControl.getWorkshopProcessParameters().lowFreq);
		bin2 = (int) (fftDataBlock.getFftLength() / fftDataBlock.getSampleRate() * 
				haggisControl.getWorkshopProcessParameters().highFreq);
		bin1 = Math.min(bin1, fftDataBlock.getFftLength()/2-1);
		bin1 = Math.max(bin1, 0);
		bin2 = Math.min(bin2, fftDataBlock.getFftLength()/2-1);
		bin2 = Math.max(bin2, 0);
		
		/*
		 * convert the threshold which was set in dB to a simple energy ratio
		 */
		thresholdRatio = Math.pow(10., haggisControl.getWorkshopProcessParameters().threshold/10.);
		
		/* 
		 * work out decay constants for background update - this will be a decaying average over time.
		 * 
		 */
		double secsPerBin = fftDataBlock.getFftHop() / fftDataBlock.getSampleRate();
		backgroundUpdateConstant = secsPerBin / haggisControl.getWorkshopProcessParameters().backgroundTimeConstant;
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
		  
		  if (overThresh == true && detectionOn == false) {
			  startDetection(arg, energy);
		  }
		  else if (overThresh == true && detectionOn == true) {
			  continueDetection(arg, energy);
		  }
		  else if (overThresh == false && detectionOn == true) {
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
		
		private void startDetection(FFTDataUnit dataUnit, double energy) {
			detectionStartSample = dataUnit.getStartSample();
			detectionEndSample = detectionStartSample + dataUnit.getSampleDuration();
			detectionEnergy = energy;
			detectionSliceCount = 1;
			detectionOn = true;
		}
		private void continueDetection(FFTDataUnit dataUnit, double energy) {
			detectionEndSample = dataUnit.getStartSample() + dataUnit.getSampleDuration();
			detectionEnergy += energy;
			detectionSliceCount += 1;
		}
		private void endDetection() {
			/*
			 * Get a new data unit from the data block (allows efficient recycling of 
			 * unused units)
			 */
			HaggisDataUnit wdu  = new HaggisDataUnit(absSamplesToMilliseconds(detectionStartSample),
					1<<channel, detectionStartSample, detectionEndSample-detectionStartSample);
			
			/*
			 * fill in detection information 
			 */
			wdu.setFrequency(new double[]{haggisControl.getWorkshopProcessParameters().lowFreq,
					haggisControl.getWorkshopProcessParameters().highFreq});
			
			/*
			 * now work out the energy in dB re 1 micropascal. This requires knowledge from
			 * both the hydrophone array and from the digitiser. Fortunately, the Acquisitionprocess, which 
			 * is a subclass of PamProcess can handle all this for us. 
			 */
			double aveAmplitude = detectionEnergy / detectionSliceCount;
			wdu.setMeasuredAmpAndType(daqProcess.fftAmplitude2dB(aveAmplitude, 
					channel, fftDataBlock.getSampleRate(), fftDataBlock.getFftLength(), 
					true, false), DataUnitBaseData.AMPLITUDE_SCALE_DBREMPA);
			
			/*
			 * put the unit back into the datablock, at which point all subscribers will
			 * be notified. 
			 */
			outputDataBlock.addPamData(wdu);
			
			detectionOn = false;
		}
		
	}

}
