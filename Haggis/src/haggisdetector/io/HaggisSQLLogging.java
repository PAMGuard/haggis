package haggisdetector.io;

import generalDatabase.PamDetectionLogging;
import generalDatabase.PamTableDefinition;
import generalDatabase.PamTableItem;
import generalDatabase.SQLTypes;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataUnit;
import haggisdetector.HaggisTypes;

import java.sql.Types;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;

public class HaggisSQLLogging extends PamDetectionLogging {

	HaggisControl haggisController;
	
	PamTableDefinition tableDefinition;
	
//	PamTableItem dateItem, durationItem, lowFreqItem, highFreqItem, energyItem, channelItem;
	PamTableItem energyItem;
	
	// haggis classification
	PamTableItem classScore, classType;
	
	public HaggisSQLLogging(HaggisControl haggisController, PamDataBlock pamDataBlock) {
		// call the super constructor. 
		super(pamDataBlock, UPDATE_POLICY_WRITENEW);
		
		// hold a reference to the Controller. 
		this.haggisController = haggisController;
		
		// create the table definition. 
		tableDefinition = (PamTableDefinition) getTableDefinition();
		PamTableItem tableItem;

		// add additional table items not included in PamDetectionLogging 
		tableDefinition.addTableItem(energyItem = new PamTableItem("energyDB", Types.DOUBLE));
		tableDefinition.addTableItem(classScore = new PamTableItem("Score", Types.DOUBLE));
		tableDefinition.addTableItem(classType = new PamTableItem("Type", Types.CHAR, 12));
		
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
		
		HaggisDataUnit haggisDataUnit = (HaggisDataUnit) pamDataUnit;
		HaggisClass haggisClass = haggisDataUnit.getHaggisClass();
		if (haggisClass == null) {
			classScore.setValue(null);
			classType.setValue(null);
		}
		else {
			classScore.setValue(haggisClass.getScore());
			classType.setValue(haggisClass.getHaggisType().toString());
		}
	}


	@Override
	protected PamDataUnit createDataUnit(SQLTypes sqlTypes, long timeMilliseconds, int databaseIndex) {
		
		HaggisDataUnit haggisDaataUnit = new HaggisDataUnit(timeMilliseconds, 0, 0, 0);	// correct values for channel, start sample and duration set in call to fillDataUnit 
		fillDataUnit(sqlTypes, haggisDaataUnit);
		
		haggisDaataUnit.setCalculatedAmlitudeDB(sqlTypes.makeDouble(energyItem.getValue()));
		
		// and stuff bespoke to the Haggis classification. 
		double score = classScore.getDoubleValue();
		String type = classType.getStringValue();
		if (type != null) {
			HaggisTypes hagType = HaggisTypes.valueOf(type);
			HaggisClass haggisClass = new HaggisClass(hagType, score);
			haggisDaataUnit.setHaggisClass(haggisClass);
		}
		
		return haggisDaataUnit;
	}

}
