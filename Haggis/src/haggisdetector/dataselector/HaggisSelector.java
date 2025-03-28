package haggisdetector.dataselector;

import PamView.dialog.PamDialogPanel;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataBlock;
import haggisdetector.HaggisDataUnit;
import pamViewFX.fxSettingsPanes.DynamicSettingsPane;

public class HaggisSelector extends DataSelector {

	private HaggisControl haggisControl;
	
	private HaggisSelectorParams selParams;
	
	private HaggisSelectPanel haggisSelectPanel;

	public HaggisSelector(HaggisControl haggisControl, HaggisDataBlock pamDataBlock, String selectorName, boolean allowScores) {
		super(pamDataBlock, selectorName, allowScores);
		this.haggisControl = haggisControl;
	}

	@Override
	public void setParams(DataSelectParams dataSelectParams) {
		try {
			selParams = (HaggisSelectorParams) dataSelectParams;
		}
		catch (ClassCastException e) {
			e.printStackTrace(); // shouldn't happen
		}
	}

	@Override
	public HaggisSelectorParams getParams() {
		if (selParams == null) {
			selParams = new HaggisSelectorParams();
		}
		return selParams;
	}

	@Override
	public PamDialogPanel getDialogPanel() {
		if (haggisSelectPanel == null) {
			haggisSelectPanel = new HaggisSelectPanel(this);
		}
		return haggisSelectPanel;
	}

	@Override
	public DynamicSettingsPane<Boolean> getDialogPaneFX() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double scoreData(PamDataUnit pamDataUnit) {
		getParams(); // make sure they are there 
		HaggisDataUnit haggisDataUnit = (HaggisDataUnit) pamDataUnit;
		HaggisClass haggisClass = haggisDataUnit.getHaggisClass();
		if (haggisClass == null) {
			return 0;
		}
		// check that the species we have is ticked. 
		boolean wantClass = selParams.isType(haggisClass.getHaggisType());
		if (wantClass == false) {
			return 0;
		}
		// also return a no if the score is too low. 
		if (haggisClass.getScore() < selParams.getMinScore()) {
			return 0;
		}
		// otherwise return the score. Most things using selectors either go 0, or non zero, but there
		// are a few such as the alarm system that can handle scores. 
		return haggisClass.getScore();
	}

}
