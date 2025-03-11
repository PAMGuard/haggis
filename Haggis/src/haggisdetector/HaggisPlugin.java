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


package haggisdetector;

import PamModel.PamDependency;
import PamModel.PamPluginInterface;
import fftManager.FFTDataUnit;

/**
 * This is the class that makes this module available as a PAMGuard plugin which can 
 * be added to an existing PAMGuard configuration. <p> 
 * When developing, it's easiest to copy PamMode.MamModel and add the detector as for
 * other modules, linking to a complete PAMGuard project code. This makes debugging easy across
 * both the plugin and across PAMGuard. 
 * <p>
 * Once developed though, export a jar file without including PamModel in the export and put
 * the jar in your Program Files/Pamguard/plugins folder and it should work with any PAMGuard installation. 
 * @author dg50
 *
 */
public class HaggisPlugin implements PamPluginInterface {
	
	String jarFile;

	@Override
	public String getClassName() {
		return HaggisControl.class.getName();
	}

	@Override
	public String getDefaultName() {
		return "Haggis Detector";
	}

	@Override
	public String getDescription() {
		return "Haggis Detector";
	}

	@Override
	public String getMenuGroup() {
		return "Detectors";
	}

	@Override
	public String getToolTip() {
		return "Detector for the Scottish Haggis";
	}

	@Override
	public PamDependency getDependency() {
		return new PamDependency(FFTDataUnit.class, "fftManager.PamFFTControl");
	}

	@Override
	public int getMinNumber() {
		return 0;
	}

	@Override
	public int getMaxNumber() {
		return 1;
	}

	@Override
	public int getNInstances() {
		return 1;
	}

	@Override
	public boolean isItHidden() {
		return false;
	}

	@Override
	public String getHelpSetName() {
		return "haggisdetector/help/HaggisHelp.hs";
	}

	@Override
	public void setJarFile(String jarFile) {
		this.jarFile = jarFile;
	}

	@Override
	public String getJarFile() {
		return jarFile;
	}

	@Override
	public String getDeveloperName() {
		return "Doug Gillespie";
	}

	@Override
	public String getContactEmail() {
		return "support@pamguard.org";
	}

	@Override
	public String getVersion() {
		return "2.0.0";
	}

	@Override
	public String getPamVerDevelopedOn() {
		return "2.02.15";
	}

	@Override
	public String getPamVerTestedOn() {
		return "2.02.16";
	}

	@Override
	public String getAboutText() {
		String desc = "Detector for the Scottish Wild Haggis <em>Haggis scoticus<em> https://haggiswildlifefoundation.com/what-is-wild-haggis/";
		return desc;
	}

	/* (non-Javadoc)
	 * @see PamModel.PamPluginInterface#allowedModes()
	 */
	@Override
	public int allowedModes() {
		return PamPluginInterface.ALLMODES;
	}

}
