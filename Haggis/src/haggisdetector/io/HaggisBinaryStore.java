package haggisdetector.io;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;

import PamguardMVC.PamDataBlock;
import PamguardMVC.PamDataUnit;
import binaryFileStorage.BinaryDataSource;
import binaryFileStorage.BinaryHeader;
import binaryFileStorage.BinaryObjectData;
import binaryFileStorage.ModuleFooter;
import binaryFileStorage.ModuleHeader;
import haggisdetector.HaggisClass;
import haggisdetector.HaggisControl;
import haggisdetector.HaggisDataUnit;
import haggisdetector.HaggisTypes;

public class HaggisBinaryStore extends BinaryDataSource {

	private HaggisControl haggisControl;
	
	private ByteArrayOutputStream bos;
	
	private static final int OBJECTTYPE = 1; // always 1  some binary streams allow for > 1 data type. 

	public HaggisBinaryStore(HaggisControl haggisControl, PamDataBlock sisterDataBlock) {
		super(sisterDataBlock);
		this.haggisControl = haggisControl;
		/**
		 * Make and reuse the output stream. It may grow a bit the first
		 * time, which takes a bit of CPU, but after that it will re-use the same memory. 
		 */
		bos = new ByteArrayOutputStream(20);
	}

	@Override
	public String getStreamName() {
		return "Haggis";
	}

	@Override
	public int getStreamVersion() {
		return 1;
	}

	@Override
	public int getModuleVersion() {
		return 1;
	}

	@Override
	public byte[] getModuleHeaderData() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BinaryObjectData getPackedData(PamDataUnit pamDataUnit) {
		bos.reset();
		HaggisDataUnit haggisData = (HaggisDataUnit) pamDataUnit;
		// most information is already written, so just add the classification data. 
		DataOutputStream dos = new DataOutputStream(bos);
		HaggisClass haggisClass = haggisData.getHaggisClass();
		try {
			dos.writeFloat((float) haggisClass.getScore());// don't use more space than necessary. 
			dos.writeUTF(haggisClass.getHaggisType().toString());
		} catch (IOException e) {
			// will never happen for a byte output stream. 
			e.printStackTrace();
		} 
		BinaryObjectData objData = new BinaryObjectData(OBJECTTYPE, bos.toByteArray());
		return objData;
	}

	@Override
	public PamDataUnit sinkData(BinaryObjectData binaryObjectData, BinaryHeader bh, int moduleVersion) {
		DataInputStream dis = binaryObjectData.getDataInputStream();
		double score = 0;
		String type = null;
		try {
			score = dis.readFloat();
			type = dis.readUTF();
		} catch (IOException e) {
			// will never happen for a byte output stream unless we read beyone the length of the data. 
			e.printStackTrace();
		}
		HaggisTypes hagType = HaggisTypes.valueOf(type);
		HaggisClass hagClass = new HaggisClass(hagType, score);
		HaggisDataUnit hagDataUnit = new HaggisDataUnit(binaryObjectData.getDataUnitBaseData(), hagClass);
		return hagDataUnit;
	}

	@Override
	public ModuleHeader sinkModuleHeader(BinaryObjectData binaryObjectData, BinaryHeader bh) {
		return null;
	}

	@Override
	public ModuleFooter sinkModuleFooter(BinaryObjectData binaryObjectData, BinaryHeader bh,
			ModuleHeader moduleHeader) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void newFileOpened(File outputFile) {
		// TODO Auto-generated method stub
		
	}

}
