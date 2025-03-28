package haggisdetector.tethys;

import java.util.ArrayList;

import haggisdetector.HaggisTypes;
import tethys.species.DataBlockSpeciesCodes;

public class HaggisSpeciesCodes extends DataBlockSpeciesCodes {

	public HaggisSpeciesCodes() {
		super("Unknown");
	}

	@Override
	public ArrayList<String> getSpeciesNames() {
		HaggisTypes[] types = HaggisTypes.values();
		ArrayList<String> names = new ArrayList<>();
		for (int i = 0; i < types.length; i++) {
			names.add(types[i].toString());
		}
		return names;
	}



}
