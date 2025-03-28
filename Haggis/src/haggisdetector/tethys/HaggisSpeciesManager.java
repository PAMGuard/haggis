package haggisdetector.tethys;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataBlock;
import haggisdetector.HaggisDataUnit;
import tethys.species.DataBlockSpeciesCodes;
import tethys.species.DataBlockSpeciesManager;

public class HaggisSpeciesManager extends DataBlockSpeciesManager {

	private HaggisControl haggisControl;
	
	private HaggisSpeciesCodes haggisSpeciesCodes;

	public HaggisSpeciesManager(HaggisControl haggisControl, HaggisDataBlock dataBlock) {
		super(dataBlock);
		this.haggisControl = haggisControl;
		haggisSpeciesCodes = new HaggisSpeciesCodes();
	}

	@Override
	public DataBlockSpeciesCodes getSpeciesCodes() {
		return haggisSpeciesCodes;
	}

	@Override
	public String getSpeciesCode(PamDataUnit dataUnit) {
		HaggisDataUnit haggisDataUnit = (HaggisDataUnit) dataUnit;
		HaggisClass haggisClass = haggisDataUnit.getHaggisClass();
		if (haggisClass == null) {
			return null;
		}
		return haggisClass.getHaggisType().toString();
	}

}
