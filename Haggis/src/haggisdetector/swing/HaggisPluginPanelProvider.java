package haggisdetector.swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.ListIterator;

import Layout.DisplayPanel;
import Layout.DisplayPanelContainer;
import Layout.DisplayPanelProvider;
import Layout.DisplayProviderList;
import Layout.PamAxis;
import PamUtils.PamUtils;
import PamView.PamColors;
import PamView.PamColors.PamColor;
import PamguardMVC.PamConstants;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.PamObservable;
import PamguardMVC.PamObserver;
import gpl.GPLStateDataUnit;
import haggisdetector.BackgroundDataUnit;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisProcess;

/**
 * Provide a graphics panel, or panels that can be added to the bottom of spectrogram
 * displays. The implementation of DisplayPanelProvider is only there to let Pamguard
 * know that panels can be created. On request, this will create any number
 * of Panels which will get added to the displays. 
 * 
 * There are two basic ways that these things can update their data. One is to 
 * follow cue of the redrawing of the spectrogram, the other is to subscribe to 
 * the some data block coming out of the detector. In this instance, we subscribe to 
 * the backgroundDataBlock in the HaggisProcess.  
 * 
 * Note that we're handling several channels of data here !
 *  
 * @author Doug
 *
 */
public class HaggisPluginPanelProvider implements DisplayPanelProvider {
	
	private HaggisControl haggisController;

	public HaggisPluginPanelProvider(HaggisControl haggisController) {
		// hold a reference to the Controller running this display
		this.haggisController = haggisController;
		// tell the provider list that I'm available.
		DisplayProviderList.addDisplayPanelProvider(this);
	}

	public DisplayPanel createDisplayPanel(DisplayPanelContainer displayPanelContainer) {
		return new HaggisPanel(this, displayPanelContainer);
	}

	public String getDisplayPanelName() {
		return "Haggis detector";
	}

	/**
	 * The class that actually does the display work. 
	 * @author Doug
	 *
	 */
	public class HaggisPanel extends DisplayPanel implements PamObserver{

		private HaggisPluginPanelProvider haggisPluginPanelProvider;
		
		private HaggisProcess haggisProcess;
		
		PamAxis westAxis;
		
		double minValue = -10;
		
		double maxValue = +25;
		
		PamDataBlock backgroundDataBlock;
		
		public HaggisPanel(HaggisPluginPanelProvider haggisPluginPanelProvider, DisplayPanelContainer displayPanelContainer) {
			super(haggisPluginPanelProvider, displayPanelContainer);
			this.haggisPluginPanelProvider = haggisPluginPanelProvider;
			haggisProcess = haggisPluginPanelProvider.haggisController.getHaggisProcess();
			westAxis = new PamAxis(0, 0, 1, 1, minValue, maxValue, true, "dB", "%.0f");

			// subscribe to the background data block
			backgroundDataBlock = haggisProcess.getBackgroundDataBlock();
			backgroundDataBlock.addObserver(this);
			backgroundDataBlock.setNaturalLifetimeMillis(30000);
		}

		@Override
		public PamObserver getObserverObject() {
			return this;
		}
		
		@Override
		public PamAxis getWestAxis() {
			return westAxis;
		}

		private int lastClear = 0;		
		private final int clearOffset = 4;
		@Override
		/*
		 * This one gets called every time the spectrogram display advances. 
		 * however, this probably isn't called in the AWT thread, but one of PAMGuards
		 * many processing threads. 
		 * Best not to do any drawing here, but call repaint which will cause the window to update. A 
		 * callback from the windows repaint function will then get used to do that actual drawing 
		 * in the AWT thread
		 */
		public void containerNotification(DisplayPanelContainer displayContainer, int noteType) {
			
			repaint(100);
			
		}
		
		int getYPixel(double value) {
			return (int) (getInnerHeight() * (maxValue - value) / (maxValue - minValue));
		}

		@Override
		public void destroyPanel() {

			if (backgroundDataBlock != null) {
				backgroundDataBlock.deleteObserver(this);
			}
			
		}

		public String getObserverName() {
			return "Haggis plug in panel";
		}

		public long getRequiredDataHistory(PamObservable o, Object arg) {
			
			long millis = (long) this.displayPanelContainer.getXDuration()+10000;
			return millis;
		}

		public void noteNewSettings() {
		}

		public void removeObservable(PamObservable o) {
		}

		public void setSampleRate(float sampleRate, boolean notify) {
		}
		
		@Override
		public void masterClockUpdate(long milliSeconds, long sampleNumber) {
		}

		int[] lastPlottedValues = new int[32];
		int[] lastXValue = new int[32];
		/**
		 * new data have arrived - work out what channel it's from and plot it.
		 */
		public void update(PamObservable o, PamDataUnit arg) {
		}

		@Override
		public void prepareImage() {
			drawImage();
		}
		
		/**
		 * Draw the image. This gets called from the AWT thread just as the display 
		 * is about to be updated. Note that the display may be wrapping or 
		 * scrolling. This should handle both. 
		 */
		private void drawImage() {
			DisplayPanelContainer displayContainer = getDisplayPanelContainer();
			double xPix = displayContainer.getCurrentXPixel();
			long xTime = displayContainer.getCurrentXTime();
			double xDuration = displayContainer.getXDuration();
			long minTime = (long) (xTime-xDuration);
			BufferedImage image = getDisplayImage();
			int imHeight = image.getHeight();
			int imWidth = image.getWidth();
			double xScale = imWidth / xDuration;
			Graphics g = getDisplayImage().getGraphics();
			
			PamColors pamColours = PamColors.getInstance();
			g.setColor(pamColours.getColor(PamColor.PlOTWINDOW));
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			

			/**
			 * Draw the threshold line
			 */
			g.setColor(Color.BLACK);
			double threshold = haggisController.getHaggisParameters().threshold;
			int yb = getYPixel(threshold);
			g.drawLine(0, yb, imWidth, yb);
			
			ArrayList dataCopy = backgroundDataBlock.getDataCopy();
			int[] prevX = new int[PamConstants.MAX_CHANNELS];
			int[] prevY = new int[PamConstants.MAX_CHANNELS];
			Arrays.fill(prevX, Integer.MIN_VALUE);
			PamColors pamColors = PamColors.getInstance();
			int nChan = PamUtils.getNumChannels(backgroundDataBlock.getChannelMap());
			int iDat = dataCopy.size();
			Color lineCol;
			while (--iDat>=0) {
				BackgroundDataUnit backgroundDataUnit = (BackgroundDataUnit) dataCopy.get(iDat);
				int chan = PamUtils.getSingleChannel(backgroundDataUnit.getSequenceBitmap());
				if (chan < 0) {
					continue;
				}
				int xt = (int) (xPix + (backgroundDataUnit.getTimeMilliseconds()-xTime) * xScale);
				if (xt < 0) xt += imWidth;
				double level = backgroundDataUnit.getBackground();
				int y = getYPixel(level);
				Color col = level > threshold ? Color.red : pamColors.getChannelColor(chan);
				g.setColor(col);
				if (prevY[chan] != Integer.MIN_VALUE && xt <= prevX[chan]) {
					g.drawLine(xt, y, prevX[chan], prevY[chan]);
				}
				prevY[chan] = y;
				prevX[chan] = xt;
				if (backgroundDataUnit.getTimeMilliseconds() < minTime) {
					break;
				}
			}
		}

		@Override
		public void addData(PamObservable observable, PamDataUnit pamDataUnit) {
			update(observable, pamDataUnit);			
		}

		@Override
		public void updateData(PamObservable observable, PamDataUnit pamDataUnit) {
			update(observable, pamDataUnit);
		}

		@Override
		public void receiveSourceNotification(int type, Object object) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
