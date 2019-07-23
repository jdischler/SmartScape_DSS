package query;

import java.awt.Point;
//import java.awt.Rectangle;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.fasterxml.jackson.databind.JsonNode;
import play.Logger;
import transformData.FilenameCleaner;
import utils.Json;

/**
 * Areas of Interest are always rectangular in shape.
 */
public class Area_ofInterest {
	
	private static String DIRECTORY_PATH = "./presets/";
	private static String EXTENSION = ".txt";

	private static Double RADIUS = 6378137.0;
	private static Double HALF_SIZE = Math.PI * RADIUS;
//	var b = [-10062652.65061,5278380.4034,-9878152.65061,5415100.4034];
//	console.log('y1: ' + (360 * Math.atan(Math.exp(b[1] / RADIUS)) / Math.PI - 90));
	private static List<Area_ofInterest> mAvailableAreas = new ArrayList<Area_ofInterest>();
	
	public String mAreaName;
	public String mAreaDescription;
	
	// Coordinates used to overlay onto map
	// TODO: extend to the full rectangle
	public Double mCornerCoordsX, mCornerCoordsY;
	
	// get the raster lookup x,y index of the given x,y location in coordinates (3857 projection) 
	public Point indexOf(Double x, Double y) {

		int xx = (int)Math.round((mCornerCoordsX - x) / -30.0);
		// VERIFY: the change in y across the raster space doesn't seem to be perfectly linear in proj 3857
		int yy = (int)Math.round((mCornerCoordsY - y) / -30.0);  
		return new Point(xx,yy);
	}
	
	// Masking example for open layers 3
	//https://jsfiddle.net/Lngp3kzb/1/
	
	//-------------------------------------------------------------------------
	public static void defineArea(Area_ofInterest area) {
		
		mAvailableAreas.add(area);
		area.write();
	}
	
	//-------------------------------------------------------------------------
	public static Area_ofInterest getArea(Integer idx) {
		
		if (idx < 0 || idx >= mAvailableAreas.size()) {
			Logger.warn(" Area_ofInterest.getArea(): attempted to get illegal area of interest <<" + idx + ">>.");
			
			if (mAvailableAreas.size() > 0) {
				Logger.warn("  available range is (0 -> " + Integer.toString(mAvailableAreas.size()) + ")");
				Logger.warn("  using default area if interest (0)");
				return mAvailableAreas.get(0);
			}
			else {
				Logger.error("  no areas of interest have been defined");
				return null;
			}
		}
		
		return mAvailableAreas.get(idx);
	}

	//-------------------------------------------------------------------------
	private void ensureFolder(String folder) throws Exception {
		File writeFolder = new File(folder);
		if (writeFolder.exists() == false) {
			FileUtils.forceMkdir(writeFolder);
			if (writeFolder.exists() == false) {
				throw new RuntimeException(" Error - Writer queue directory creation failed!!");
			}
		}
	}
	
	// TODO: consider whether using the Jackson ObjectMapper is a much better fit
	//-------------------------------------------------------------------------
	private void write() {
		
		JsonNode data = Json.pack("name", mAreaName,
					"description", mAreaDescription,
					"atX", mCornerCoordsX,
					"atY", mCornerCoordsY
					);
		String path = DIRECTORY_PATH + FilenameCleaner.cleanFileName(mAreaName) + EXTENSION;
		
		try {
			ensureFolder(DIRECTORY_PATH);
			Json.toDisk(data, path);
		} catch (Exception e) {
			Logger.error(e.toString());
		}
	}
	
	// TODO: consider whether using the Jackson ObjectMapper is a much better fit
	//-------------------------------------------------------------------------
	private static Area_ofInterest read(String path) {
		
		String name, description;
		Double atX, atY;
		Area_ofInterest area = null;
		
		try {
			JsonNode data = Json.fromDisk(path);
			
			name = Json.safeGetString(data, "name");
			description = Json.safeGetOptionalString(data, "description", "");
			atX = Json.safeGetDouble(data, "atX");
			atY = Json.safeGetDouble(data, "atY");
			
			area = new Area_ofInterest();
			area.mAreaName = name;
			area.mAreaDescription = description;
			area.mCornerCoordsX = atX;
			area.mCornerCoordsY = atY;
			
		} catch (Exception e) {
			Logger.error(e.toString());
		}
		
		return area;
	}
	
	//-------------------------------------------------------------------------
	public static void loadPresets() {
		
		File folder = new File(DIRECTORY_PATH);
		File[] listOfFiles = folder.listFiles();
		
		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				String toLoad = DIRECTORY_PATH + listOfFiles[i].getName();
				Logger.info(" - Area_ofInterest.loadPresets: loading <<" + toLoad + ">>");
				Area_ofInterest area = read(toLoad);
				if (area != null) {
					mAvailableAreas.add(area);
				}
			}
			else if (listOfFiles[i].isDirectory()) {
				Logger.error("Subdirectories in Area of Interest Presets is not supported at this time");
			}
		}
		
	}
}
