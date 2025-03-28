package haggisdetector.dataselector;

import PamguardMVC.dataSelector.DataSelectParams;
import PamguardMVC.dataSelector.DataSelector;
import PamguardMVC.dataSelector.DataSelectorCreator;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataBlock;

public class HaggisSelectCreator extends DataSelectorCreator{

	private HaggisControl haggisControl;
	private HaggisDataBlock haggisDataBlock;

	public HaggisSelectCreator(HaggisControl haggisControl,  HaggisDataBlock haggisDataBlock) {
		super(haggisDataBlock);
		this.haggisControl = haggisControl;
		this.haggisDataBlock = haggisDataBlock;
	}

	@Override
	public DataSelector createDataSelector(String selectorName, boolean allowScores, String selectorType) {
		return new HaggisSelector(haggisControl, haggisDataBlock, selectorName, allowScores);
	}

	@Override
	public DataSelectParams createNewParams(String name) {
		return new HaggisSelectorParams();
	}


}
