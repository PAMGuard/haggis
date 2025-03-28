package haggisdetector.swing;

import java.awt.Color;

import PamView.GeneralProjector;
import PamView.PamColors;
import PamView.symbol.PamSymbolChooser;
import PamView.symbol.SymbolData;
import PamView.symbol.modifier.SymbolModType;
import PamView.symbol.modifier.SymbolModifier;
import PamguardMVC.PamDataUnit;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisDataUnit;
import haggisdetector.HaggisTypes;

/**
 * Modifier to add to the symbol manager that will colour the detections by their types. 
 * @author dg50
 *
 */
public class HaggisSymbolModifier extends SymbolModifier {

	private SymbolData symbolData = new SymbolData();
	
	public HaggisSymbolModifier(PamSymbolChooser symbolChooser) {
		super("Haggis Type", symbolChooser, SymbolModType.FILLCOLOUR | SymbolModType.LINECOLOUR);
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
		HaggisTypes type = haggisClass.getHaggisType();
		int ord = type.ordinal();
		Color col = PamColors.getInstance().getWhaleColor(ord);
		symbolData.setFillColor(col);
		symbolData.setLineColor(col);
		return symbolData;
	}

}
