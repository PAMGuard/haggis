package haggisdetector.swing;

import java.awt.Color;

import PamView.ColourArray;
import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisDataUnit;

/**
 * Modify colour based on score using a hot colour array. 
 * @author dg50
 *
 */
public class HaggisSymbolModifier2 extends SymbolModifier {

	private int NCOLS = 20; // more than enough. 
	private ColourArray scoreColours;
	
	private SymbolData symbolData = new SymbolData();

	public HaggisSymbolModifier2(PamSymbolChooser symbolChooser) {
		super("Haggis Score", symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
		scoreColours = ColourArray.createHotArray(NCOLS+1);
	}

	@Override
	public SymbolData getSymbolData(GeneralProjector projector, PamDataUnit dataUnit) {
		/**
		 * Modify graphic to use a colour based on the class. 
		 */
		HaggisDataUnit haggisDataUnit = (HaggisDataUnit) dataUnit;
		HaggisClass haggisClass = haggisDataUnit.getHaggisClass();
		if (haggisClass == null) {
			return null;
		}
		double score = haggisClass.getScore();
		int intInd = (int) Math.round(score*NCOLS);
		intInd = Math.max(0, intInd);
		intInd = Math.min(NCOLS, intInd);
		Color col = scoreColours.getColour(intInd);
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);
		return symbolData;
	}

}
