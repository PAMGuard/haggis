/*
 *  PAMGUARD - Passive Acoustic Monitoring GUARDianship.
 * To assist in the Detection Classification and Localisation
 * of marine mammals (cetaceans).
 *
 * Copyright (C) 2006
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */



package haggisdetector.swing;

import PamView.symbol.PamSymbolChooser;
import PamView.symbol.StandardSymbolManager;
import PamView.symbol.SymbolData;
import PamguardMVC.PamDataBlock;
import haggisdetector.HaggisControl;

/**
 * Symbol manager. This is a standard symbol manager with two additional modifiers, 
 * the first of which will change colours based on the Haggis Type, the second on the score. 
 * @author dg50
 *
 */
public class HaggisSymbolManager extends StandardSymbolManager {

	private HaggisControl haggisController;

	public HaggisSymbolManager(HaggisControl haggisController, PamDataBlock pamDataBlock) {
		super(pamDataBlock, new SymbolData());
		this.haggisController = haggisController;

		addSymbolOption(StandardSymbolManager.HAS_LINE);
		addSymbolOption(HAS_CHANNEL_OPTIONS);
		addSymbolOption(HAS_SYMBOL);
	}

	@Override
	public void addSymbolModifiers(PamSymbolChooser psc) {
		super.addSymbolModifiers(psc);
		psc.addSymbolModifier(new HaggisSymbolModifier(psc));
		psc.addSymbolModifier(new HaggisSymbolModifier2(psc));
	}
}
