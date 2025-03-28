package haggisdetector;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serializable;

import javax.swing.JMenuItem;

import PamController.PamControlledUnit;
import PamController.PamControlledUnitSettings;
import PamController.PamControllerInterface;
import PamController.PamSettingManager;
import PamController.PamSettings;
import haggisdetector.swing.HaggisParametersDialog;
import haggisdetector.swing.HaggisPluginPanelProvider;

/**
 * Simple detector designed to detect the Scottish Wild Haggis
 * <em>Haggis scoticus</em> https://haggiswildlifefoundation.com/what-is-wild-haggis/
 * <br>
 * The detector is a very simple in band energy detector followed by a statistical classifier.
 * The detector will subscribe to a block of FFT (spectrogram) data and measure
 * the background noise in a given frequency band over some time period
 * and compare the signal to that background measure. If the SNR is >
 * threshold a detection starts, if it's below threshold it stops again.
 * <br>
 * The statistical classifier then analyses the incoming sound and attempts
 * to classify it to Haggis sub species.
 * <br>
 * Given the low likelihood of actually encountering a wild haggis, the main purpose
 * of this detector is to act as a template for developers wishing to create
 * their own detectors for more common species.
 * <br>
 * All PAMGuard modules start with an instance of PamControlledUnit, so please scroll on ...
 *
 * @author Doug Gillespie
 *
 */
public class HaggisControl extends PamControlledUnit implements PamSettings {

	private HaggisProcess haggisProcess;

	private HaggisParameters haggisParameters;

	private HaggisPluginPanelProvider haggisPluginPanelProvider;

	public static final String unitType = "Haggis Detector";

	/**
	 * Must have a default constructor that takes a single String as an argument.
	 * @param unitName Instance specific name to give this module.
	 */
	public HaggisControl(String unitName) {
		super(unitType, unitName);

		/*
		 * create the parameters that will control the process.
		 * (do this before creating the process in case the process
		 * tries to access them from it's constructor).
		 */
		haggisParameters = new HaggisParameters();

		/*
		 * make a HaggisProcess - which will actually do the detecting
		 * for us. Although the super class PamControlledUnit keeps a list
		 * of processes in this module, it's also useful to keep a local
		 * reference.
		 * Adding the process means that PAMGuards core management tools can
		 * find this process, and eventually any output data from the process
		 */
		addPamProcess(haggisProcess = new HaggisProcess(this));

		/*
		 * provide plug in panels for the bottom of the spectrogram displays
		 * (and any future displays that support plug in panels)
		 */
		haggisPluginPanelProvider = new HaggisPluginPanelProvider(this);

		/*
		 * Tell the PAmguard settings manager that we have settings we wish to
		 * be saved between runs. IF settings already exist, the restoreSettings()
		 * function will get called back from here with the most recent settings.
		 */
		PamSettingManager.getInstance().registerSettings(this);
	}

	@Override
	public void notifyModelChanged(int changeType) {
		super.notifyModelChanged(changeType);

		/*
		 * This gets called every time a new module is added - make sure
		 * that the HaggisProcess get's a chance to look around and see
		 * if there is data it wants to subscribe to.
		 */
		switch (changeType) {
		case PamControllerInterface.ADD_CONTROLLEDUNIT:
		case PamControllerInterface.REMOVE_CONTROLLEDUNIT:
		case PamControllerInterface.ADD_DATABLOCK:
		case PamControllerInterface.REMOVE_DATABLOCK:
			getHaggisProcess().prepareProcess();
		}
	}

	/*
	 * Menu item and action for detection parameters...
	 *  (non-Javadoc)
	 * @see PamController.PamControlledUnit#createDetectionMenu(java.awt.Frame)
	 */
	@Override
	public JMenuItem createDetectionMenu(Frame parentFrame) {
		JMenuItem menuItem = new JMenuItem(getUnitName() + " Parameters");
		menuItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				showParamsDialog(parentFrame);
			}
		});
		return menuItem;
	}

	/** 
	 * Show the parameterd dialog and update process if they change. 
	 * @param parentFrame
	 */
	private void showParamsDialog(Frame parentFrame) {
		HaggisParameters newParams = HaggisParametersDialog.showDialog(parentFrame, this,
				haggisParameters);
		/*
		 * The dialog returns null if the cancel button was set. If it's
		 * not null, then clone the parameters onto the main parameters reference
		 * and call preparePRocess to make sure they get used !
		 */
		if (newParams != null) {
			haggisParameters = newParams.clone();
			getHaggisProcess().prepareProcess();
		}
	}

	/**
	 * These next three functions are needed for the PamSettings interface
	 * which will enable Pamguard to save settings between runs
	 */
	@Override
	public Serializable getSettingsReference() {
		return getHaggisParameters();
	}

	@Override
	public long getSettingsVersion() {
		return HaggisParameters.serialVersionUID;
	}

	@Override
	public boolean restoreSettings(PamControlledUnitSettings pamControlledUnitSettings) {
		haggisParameters = (HaggisParameters) pamControlledUnitSettings.getSettings();
		return true;
	}

	/**
	 * @return the haggisProcess
	 */
	public HaggisProcess getHaggisProcess() {
		return haggisProcess;
	}

	/**
	 * @return the haggisParameters
	 */
	public HaggisParameters getHaggisParameters() {
		return haggisParameters;
	}

}
