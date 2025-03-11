package haggisdetector.io;

import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataUnit;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class HaggisSQLLogging extends PamDetectionLogging {

	HaggisControl workshopController;
	
	PamTableDefinition tableDefinition;
	
//	PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem, channelItem;
	PamTableItem energyItem;
	
	public HaggisSQLLogging(HaggisControl workshopController, PamDataBlock pamDataBlock) {
		// call the super constructor. 
		super(pamDataBlock, UPDATE_POLICY_WRITENEW);
		
		// hold a reference to the Controller. 
		this.workshopController = workshopController;
		
		// create the table definition. 
		tableDefinition = (PamTableDefinition) getTableDefinition();
		PamTableItem tableItem;

		// add additional table items not included in PamDetectionLogging 
		tableDefinition.addTableItem(energyItem = new PamTableItem("energyDB", Types.DOUBLE));
		tableDefinition.setUseCheatIndexing(true);
	}


	@Override
	/*
	 * This gets called back from the database manager whenever a new dataunit is
	 * added to the datablock. All we have to do is set the data values for each 
	 * field and they will be inserted into the database. 
	 * If formats are incorrect, the SQL write statement is likely to fail !
	 */
	public void setTableData(SQLTypes sqlTypes, PamDataUnit pamDataUnit) {
		super.setTableData(sqlTypes, pamDataUnit);

		HaggisDataUnit wdu = (HaggisDataUnit) pamDataUnit;
		energyItem.setValue(wdu.getAmplitudeDB());
	}


	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		
		HaggisDataUnit wdu = new HaggisDataUnit(timeMilliseconds, 0, 0, 0);	// correct values for channel, start sample and duration set in call to fillDataUnit 
		fillDataUnit(sqlTypes, wdu);
		wdu.setCalculatedAmlitudeDB(sqlTypes.makeDouble(energyItem.getValue()));
		return wdu;
	}

}
