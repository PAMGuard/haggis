package haggisdetector;

import PamView.symbol.PamSymbolManager;
import PamguardMVC.PamDataBlock;
import PamguardMVC.PamProcess;
import PamguardMVC.dataSelector.DataSelectorCreator;
import haggisdetector.dataselector.HaggisSelectCreator;
import haggisdetector.tethys.HaggisSpeciesManager;
import haggisdetector.tethys.HaggisTethysProvider;
import tethys.TethysControl;
import tethys.pamdata.TethysDataProvider;
import tethys.species.DataBlockSpeciesManager;

/**
 * Main stream of Haggis output data. This is simply the base class with overridden functions
 * for the Tethys interface, species manager, and symbol manager, and data selector, all of which are bespoke for 
 * this particular detector. 
 * @author dg50
 *
 */
public class HaggisDataBlock extends PamDataBlock<HaggisDataUnit> {

	private HaggisTethysProvider haggisTethysProvider;

	private HaggisSpeciesManager haggisSpeciesManager;

	private HaggisControl haggisControl;
	
	private HaggisSelectCreator haggisSelectCreator;

	public HaggisDataBlock(HaggisControl haggisControl, String dataName, PamProcess parentProcess, int channelMap) {
		super(HaggisDataUnit.class, dataName, parentProcess, channelMap);
		this.haggisControl = haggisControl;
		haggisSelectCreator = new HaggisSelectCreator(haggisControl, this);
		setCanClipGenerate(true); // needed for the clip generator to be able to list it as a trigger. 
	}

	@Override
	public TethysDataProvider getTethysDataProvider(TethysControl tethysControl) {
		if (haggisTethysProvider == null) {
			haggisTethysProvider = new HaggisTethysProvider(tethysControl, getFirstRawSourceDataBlock());
		}
		return haggisTethysProvider;
	}

	@Override
	public DataBlockSpeciesManager<HaggisDataUnit> getDatablockSpeciesManager() {
		if (haggisSpeciesManager == null) {
			haggisSpeciesManager = new HaggisSpeciesManager(haggisControl, this);
		}
		return haggisSpeciesManager;
	}

	@Override
	public DataSelectorCreator getDataSelectCreator() {
		/*
		 * We provide a data select creator. Many different data selectors may be created from this
		 * for different displays and different downstream tasks. 
		 */
		return haggisSelectCreator;
	}


}
