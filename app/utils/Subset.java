package utils;

//import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;


import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import play.Logger;

// Subsets are always rectangular in shape.
//
//-----------------------------------------------------------
public class Subset {
	
	private static String PRESETS_PATH = "./layerData/subset_presets.json";
	private static List<Subset> mAvailableSubsets = new ArrayList<Subset>();
	
	public String mName;
	public String mDescription;
	
	public String mAssetDirectory;
	
	// Coordinates used to overlay onto map or clip a hole in the map mask
	public Rectangle2D.Double mCoordinates;
	
	// Coordinates used to index into and extract the relevant section of data in the larger set
	public Rectangle mMapping;
	
	
	//-------------------------------------------------------------------------
	Subset(String name, String description, String assetDirectory) {
		mName = name;
		mDescription = description;
		mAssetDirectory = assetDirectory;
	}
	
	// Masking example for open layers 3
	//https://jsfiddle.net/Lngp3kzb/1/
	
	// FIXME: TODO: y may not be entirely linear in 3857 projection?
	//-------------------------------------------------------------------------
	public Point indicesFor(Double x, Double y) {
		int idx = (int)Math.round((mCoordinates.x - x) / -30.0);
		int idy = (int)Math.round((mCoordinates.y - y) / 30.0);
		return new Point(idx,idy);
	}		
	
	//-----------------------------------------------------------------
	public Point indicesFor(Point2D.Double point) {
		return indicesFor(point.x, point.y);
	}
	
	// FIXME: TODO: have a way to get one by name or something else instead?
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
	public JsonNode getMapMask() {
		
		return Json.pack("x", mCoordinates.x,
				"y", mCoordinates.y,
				"w", mCoordinates.width,
				"h", mCoordinates.height
			);
	}
	
	// TODO: consider whether using the Jackson ObjectMapper is a much better fit
	//-------------------------------------------------------------------------
	public static void loadPresets() {
		
		Logger.info("Looking for a subset presets file");
		try {
			JsonNode data = Json.fromDisk(PRESETS_PATH);
			if (!data.isArray()) throw new Exception("Subset: loadPresets: expected a JSON array of presets");
			
			ArrayNode array = (ArrayNode)data;
			for (int i = 0; i < array.size(); i++) {
				String name, description, assets;
				
				JsonNode elem = array.get(i);
				
				name = Json.safeGetString(elem, "name");
				description = Json.safeGetOptionalString(elem, "description", "");
				assets = Json.safeGetString(elem, "assets");
				
				Subset subset = new Subset(name, description, assets);
				subset.mCoordinates = Json.getRectangleDouble(elem, "coordinates");
				subset.mMapping = Json.getRectangleInteger(elem, "mapping");
				
				mAvailableSubsets.add(subset);
			}
			
		} catch (Exception e) {
			Logger.error(e.toString());
		}
	}
}

