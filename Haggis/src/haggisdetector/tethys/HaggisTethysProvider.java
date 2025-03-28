package haggisdetector.tethys;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisDataUnit;
import nilus.Detection;
import nilus.Detection.Parameters;
import nilus.Helper;
import tethys.TethysControl;
import tethys.output.StreamExportParams;
import tethys.output.TethysExportParams;
import tethys.pamdata.AutoTethysProvider;

public class HaggisTethysProvider extends AutoTethysProvider {

	private Helper helper;
	
	public HaggisTethysProvider(TethysControl tethysControl, PamDataBlock pamDataBlock) {
		super(tethysControl, pamDataBlock);
		try {
			helper = new Helper();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
	}

	@Override
	public Detection createDetection(PamDataUnit dataUnit, TethysExportParams tethysExportParams,
			StreamExportParams streamExportParams) {
		Detection det = super.createDetection(dataUnit, tethysExportParams, streamExportParams);
		HaggisDataUnit haggisDataUnit = (HaggisDataUnit) dataUnit;
		Parameters detParams = det.getParameters();
		HaggisClass haggisClass = haggisDataUnit.getHaggisClass();
		if (haggisClass != null) {
			// add the score and the type
			detParams.setScore(haggisClass.getScore());
			try {
				helper.AddAnyElement(detParams.getUserDefined().getAny(), "HAGGISTYPE", haggisClass.getHaggisType().toString());
			} catch (JAXBException | ParserConfigurationException e) {
				e.printStackTrace();
			}
		}
		return det;
	}

}
