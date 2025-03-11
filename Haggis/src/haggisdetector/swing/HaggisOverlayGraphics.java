package haggisdetector.swing;

import java.awt.Color;

import PamView.PamDetectionOverlayGraphics;
import PamView.PamSymbol;
import PamView.PamSymbolType;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;

/**
 * Graphics examples showing how to draw the detector output on the map and
 * on the spectrogram display.
 * The class GeneralProjector knows how to turn parameters such as lat, long, time, 
 * frequency into meaningful screen coordinates. It will tell us which types
 * of coordinates are required for each display, if we can provide them we can
 * go ahead and draw. The canDraw function is mainly used by Pamguard for making
 * up options menus of what can be plotted on top of the various displays. The 
 * PanelOverlayDraw is attached to a specific data block, so this one class needs
 * to handle drawing on all different types of display. 
 * @author Doug
 *
 */
public class HaggisOverlayGraphics extends PamDetectionOverlayGraphics {

	public static final SymbolData defaultSymbol = new SymbolData(PamSymbolType.SYMBOL_TRIANGLED, 15, 15, 1, false, Color.cyan, Color.cyan);

	public HaggisOverlayGraphics(PamDataBlock parentDataBlock) {
		super(parentDataBlock, new PamSymbol(defaultSymbol));
	}

//	@Override
//	public PamKeyItem createKeyItem(GeneralProjector projector, int keyType) {
////		return new BasicKeyItem(ClickDetSymbolChooser.getClickSymbol(0).clone(), name);
//		return new BasicKeyItem(this.getParentDataBlock().getPamSymbolManager().getSymbolChooser(null, projector).getPamSymbol(projector, this.getParentDataBlock()), name);
//	}


}
