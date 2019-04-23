package utils;

//import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import transformData.FilenameCleaner;

/**
 * Subsets are always rectangular in shape.
 */
public class Subset {
	
	private static String DIRECTORY_PATH = "./presets/";
	private static String EXTENSION = ".txt";
	
	private static List<Subset> mAvailableSubsets = new ArrayList<Subset>();
	
	public String mSubsetName;
	public String mSubsetDescription;
	
	// Coordinates used to overlay onto map
	public Double mCornerCoordsX, mCornerCoordsY;
	
	// Coordinates used to index into and extract the relevant section of data in the larger set
//	public Rectangle mSubsetArea;
	
	
	// Data for automated cleanup of custom subsets
	public Boolean mCustomSubset = false;
	// Custom subsets exist for at least 24 hours. Any usage of that custom subset refreshes the countdown
	public Integer mCustomSubsetHoursLeft = 25; 
	
	// Masking example for open layers 3
	//https://jsfiddle.net/Lngp3kzb/1/
	
	//-------------------------------------------------------------------------
	public static void defineSubset(Subset subset) {
		
		mAvailableSubsets.add(subset);
		subset.write();
	}
	
	//-------------------------------------------------------------------------
	public static Subset getSubset(Integer idx) {
		
		if (idx < 0 || idx >= mAvailableSubsets.size()) {
			Logger.warn(" Subset.getSubset(): attempted to get illegal subset <<" + idx + ">>.");
			
			if (mAvailableSubsets.size() > 0) {
				Logger.warn("  available range is (0 -> " + Integer.toString(mAvailableSubsets.size()) + ")");
				Logger.warn("  using default subset (0)");
				return mAvailableSubsets.get(0);
			}
			else {
				Logger.error("  no subsets have been defined");
				return null;
			}
		}
		
		return mAvailableSubsets.get(idx);
	}

	//-------------------------------------------------------------------------
	private void ensureFolder(String folder) throws Exception {
		File writeFolder = new File(folder);
		if (writeFolder.exists() == false) {
			FileUtils.forceMkdir(writeFolder);
			//writeFolder.mkdirs();
			if (writeFolder.exists() == false) {
				throw new RuntimeException(" Error - Writer queue directory creation failed!!");
			}
		}
	}
	
	// TODO: consider whether using the Jackson ObjectMapper is a much better fit
	//-------------------------------------------------------------------------
	private void write() {
		
		JsonNode data = Json.pack("name", mSubsetName,
					"description", mSubsetDescription,
					"atX", mCornerCoordsX,
					"atY", mCornerCoordsY,
//					"area", mSubsetArea,
					"isCustom", mCustomSubset,
					"expiresHours", mCustomSubsetHoursLeft);
		String path = DIRECTORY_PATH + FilenameCleaner.cleanFileName(mSubsetName) + EXTENSION;
		
		try {
			ensureFolder(DIRECTORY_PATH);
			Json.toDisk(data, path);
		} catch (Exception e) {
			Logger.error(e.toString());
		}
	}
	
	// TODO: consider whether using the Jackson ObjectMapper is a much better fit
	//-------------------------------------------------------------------------
	private static Subset read(String path) {
		
		String name, description;
		Double atX, atY;
//		Rectangle area;
		Boolean isCustom;
		Integer hoursLeft = 25; 
		Subset subset = null;
		
		try {
			JsonNode data = Json.fromDisk(path);
			
			name = Json.safeGetString(data, "name");
			description = Json.safeGetOptionalString(data, "description", "");
			atX = Json.safeGetDouble(data, "atX");
			atY = Json.safeGetDouble(data, "atY");
			isCustom = Json.safeGetOptionalBoolean(data, "isCustom", false);
			hoursLeft = Json.safeGetOptionalInteger(data, "expiresHours", hoursLeft);
//			area = Json.safeGetRectangle(data, "area");
			
			subset = new Subset();
			subset.mSubsetName = name;
			subset.mSubsetDescription = description;
			subset.mCornerCoordsX = atX;
			subset.mCornerCoordsY = atY;
//			subset.mSubsetArea = area;
			subset.mCustomSubset = isCustom;
			subset.mCustomSubsetHoursLeft = hoursLeft;
			
		} catch (Exception e) {
			Logger.error(e.toString());
		}
		
		return subset;
	}
	
	//-------------------------------------------------------------------------
	public static void loadPresets() {
		
		File folder = new File(DIRECTORY_PATH);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String toLoad = DIRECTORY_PATH + listOfFiles[i].getName();
				Logger.info(" - Subset.loadPresets: loading <<" + toLoad + ">>");
				Subset subset = read(toLoad);
				if (subset != null) {
					mAvailableSubsets.add(subset);
				}
			}
			else if (listOfFiles[i].isDirectory()) {
				Logger.error("Subdirectories in Subset Presets is not supported at this time");
			}
		}
		
	}
}
	
