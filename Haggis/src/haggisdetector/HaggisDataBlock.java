package haggisdetector;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;

public class HaggisDataBlock extends PamDataBlock<HaggisDataUnit> {

	public HaggisDataBlock(String dataName, PamProcess parentProcess, int channelMap) {
		super(HaggisDataUnit.class, dataName, parentProcess, channelMap);
	}

}
