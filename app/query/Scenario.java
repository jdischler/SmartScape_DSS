package query;

import play.*;
import utils.ClientUser;
import utils.GlobalAssumptions;
import utils.Json;
import utils.ManagementOptions;
import utils.RandomString;

import java.util.*;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.*;

// TODO: probably rethink
//------------------------------------------------------------------------------
public class Scenario 
{
	//--------------------------------------------------------------------------
	private static final boolean DETAILED_DEBUG_LOGGING = true;
	private static final void detailedLog(String detailedMessage) {
		
		if (DETAILED_DEBUG_LOGGING) {
			Logger.debug(detailedMessage);
		}
	}
	
	// Scenarios can be cached for sharing amongst other threads
	private static Map<String, Scenario>	mCachedScenarios;
	private long mCachedAtTime;
	
	public GlobalAssumptions mAssumptions;
	public Selection mSelection; 
	public String mOutputDir;
	private JsonNode mConfiguration;
	public int[][] mNewRotation; // copy of Rotation layer, but selection transformed
	public ClientUser mOptionalUser = null; // yup, can be null
	
	//--------------------------------------------------------------------------
	public Scenario(ClientUser user) {
		mOptionalUser = user; // ca be null
	}

	//--------------------------------------------------------------------------
	public final int getWidth() {
		if (mSelection != null) {
			return mSelection.getWidth();
		}
		return 0;
	}

	//--------------------------------------------------------------------------
	public final int getHeight() {
		if (mSelection != null) {
			return mSelection.getHeight();
		}
		return 0;
	}
	
	//--------------------------------------------------------------------------
	public void setAssumptions(JsonNode clientAssumptions) {
		
		mAssumptions = new GlobalAssumptions();
		try {
			mAssumptions.setFromClient(clientAssumptions);
		} 
		catch (Exception e) {
			Logger.info(e.toString());
		}
	}
	
	// Returns a cacheStringID, which should be saved and returned to free the scenario...
	//--------------------------------------------------------------------------
	public static final String cacheScenario(Scenario theScenario, String clientID) {
		
		if (mCachedScenarios == null) {
			mCachedScenarios = new HashMap<String, Scenario>();
		}
		
		int tryCount = 0;
		while(tryCount < 1000) {
			String scenarioCacheID = RandomString.get(5) + 
						clientID + 
						((tryCount > 0) ? Integer.toString(tryCount) : "");
			if (!mCachedScenarios.containsKey(scenarioCacheID)) {
				mCachedScenarios.put(scenarioCacheID, theScenario);
				theScenario.mCachedAtTime = System.currentTimeMillis();
				return scenarioCacheID;
			}
			tryCount++;
		}
		
		return null;
	}
	
	//--------------------------------------------------------------------------
	public static final void checkPurgeStaleScenarios() {
		
		if (mCachedScenarios == null) {
			return;
		}
		// giving 5 minutes 		
		long expireHours = 0 * 10 * 60 * 1000; // 0 hour -> minutes -> seconds -> milliseconds
		long roughlyNow = System.currentTimeMillis();
		for (Map.Entry<String, Scenario> entry : mCachedScenarios.entrySet()) {
			Logger.warn("Have possibly stale scenario objects hanging around...");
			Scenario value = entry.getValue();
			if (roughlyNow - value.mCachedAtTime > expireHours) {
				Logger.error("Error - removing potentially stale scenario. " +
					"Anything caching a scenario should be remove cached scenario when " +
					"done using that scenario!");
				String key = entry.getKey();
				releaseCachedScenario(key);
			}
		}
	}
	
	//--------------------------------------------------------------------------
	public static final Scenario getCachedScenario(String cacheStringID) {
		
		if (mCachedScenarios == null) {
			Logger.warn("Attempting to fetch a scenario but the cache has not been initialized!");
			return null;
		}
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.warn("Attempting to fetch scenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return null;
		}
		
		return res;
	}
	
	//--------------------------------------------------------------------------
	public static final void releaseCachedScenario(String cacheStringID) {
		
		if (mCachedScenarios == null) {
			Logger.warn("Attempting to uncache a scenario but the cache has not been initialized!");
			return;
		}
		
		Scenario res = mCachedScenarios.get(cacheStringID);
		if (res == null) {
			Logger.warn("Attempting to uncache scenario named <" + cacheStringID + 
							"> but that does not appear to be cached");
			return;
		}
		
		detailedLog(" - releasing cache for scenario, cache string named <" + cacheStringID + ">");
		mCachedScenarios.remove(cacheStringID);
	}
	
	
	//--------------------------------------------------------------------------
/*	public JsonNode run() {
		
		mNewRotation = duplicateRotation();
		transformRotation(mNewRotation);
		
		Models model = new Models();
		JsonNode SendBack = model.modeloutcome(null, mSelection, mOutputDir, mNewRotation);
		return SendBack;
	}*/

	//--------------------------------------------------------------------------
	public int[][] getTransformedRotation(JsonNode configuration) {
		
		mConfiguration = configuration;
		
		detailedLog("Beginning transform rotation...");
		detailedLog("...Current rotation duplicating...");
		mNewRotation = duplicateRotation();
		detailedLog("...Duplicated rotation transforming...");
		mNewRotation = transformRotation(mNewRotation);
		detailedLog("...Transform complete!!");
		
		return mNewRotation;
	}
	
	//--------------------------------------------------------------------------
	private int[][] duplicateRotation() {
	
		// uses clone to duplicate the data array
		Layer_Integer originalCDL = Layer_CDL.get();
		int [][] originalData = originalCDL.getIntData();
		
		int height = originalCDL.getHeight();
		
		mNewRotation = new int[height][];
		for (int y = 0; y < height; y++) {
			mNewRotation[y] = originalData[y].clone(); 
		}
		return mNewRotation;
	}

	//--------------------------------------------------------------------------
	private int[][] transformRotation(int[][] rotationToTransform) {
	
		Query query = new Query();
		
		JsonNode transformQueries = mConfiguration.get("transforms");
		if (transformQueries != null && transformQueries.isArray()) {
			
			detailedLog("...Has Transforms array...");
			Selection currentSelection = null, oldSelection = null;
			ArrayNode transformArray = (ArrayNode)transformQueries;
			int count = transformArray.size();
			
			for (int i = 0; i < count; i++) {
				detailedLog("...Processing one array element in the transform list...");
				JsonNode transformElement = transformArray.get(i);
				
				if (transformElement == null) {
					Logger.warn("Boooo....transform element was null.");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				else if (!transformElement.isObject()) {
					Logger.warn("Booooooo.....transform element is not an object");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				
				// get the new land-use...but remember that it needs to be in the format of a bit mask "position" that 
				//	corresponds to the index vs. the index value itself.
				JsonNode transformConfig = transformElement.get("transform");
				if (transformConfig == null || !transformConfig.isObject()) {
					Logger.warn("Boooo....transform config does not exist or is not an object");
					continue; // TODO: signal back to client that an error happened vs. just doing nothing
				}
				
				ObjectNode transformConfigObj = (ObjectNode)transformConfig;
				
				int newLandUse = transformConfigObj.get("land_use").intValue();
				detailedLog("  + New land use code: " + Integer.toString(newLandUse));
				newLandUse = Layer_Integer.indexToMask(newLandUse);
				
				JsonNode managementOptions = transformConfigObj.get("options");
				if (managementOptions != null && managementOptions.isObject()) {
					detailedLog("  +-- Management Options from Client: " + managementOptions.toString());
					try {
						JsonNode fertNode = managementOptions.get("type");
						if (fertNode != null && fertNode.isObject()) { 
							ObjectNode fertilizerOptions = (ObjectNode)fertNode;
							if (fertilizerOptions.get("fertilizer").booleanValue()) {
								detailedLog("  +--- Applying Fertilizer");
								newLandUse = ManagementOptions.E_Fertilizer.setOn(newLandUse); // else no fertilizer
								if (fertilizerOptions.get("FertilizerManure").booleanValue()) {
									detailedLog("  +--- Fertilizer Is Manure");
									newLandUse = ManagementOptions.E_Manure.setOn(newLandUse); // else is synthetic
									if (fertilizerOptions.get("FertilizerFallSpread").booleanValue()) {
										newLandUse = ManagementOptions.E_FallManure.setOn(newLandUse); // else is spread other time
										detailedLog("  +--- Fertilizer Is Fall Spread Manure");
									}
								}
							}
						}
						if (Json.safeGetOptionalBoolean(managementOptions, "Tillage", false)) {
							newLandUse = ManagementOptions.E_Till.setOn(newLandUse); // else is no-till
							detailedLog("  +--- Applying Tillage");
						}
						if (Json.safeGetOptionalBoolean(managementOptions, "CoverCrop", false)) {
							newLandUse = ManagementOptions.E_CoverCrop.setOn(newLandUse); // else is no-covercrop
							detailedLog("  +--- Applying CoverCrop");
						}
						if (Json.safeGetOptionalBoolean(managementOptions, "Contour", false)) {
							newLandUse = ManagementOptions.E_Contour.setOn(newLandUse); // else is no-contour
							detailedLog("  +--- Applying Contouring");
						}
						if (Json.safeGetOptionalBoolean(managementOptions, "Terraced", false)) {
							newLandUse = ManagementOptions.E_Terrace.setOn(newLandUse); // else is no-terraces
							detailedLog("  +--- Applying Terracing");
						}
					} catch (Exception e) {
					}
				}
				
				try {
					currentSelection = query.execute(transformElement, mOptionalUser);
				} catch (Exception e) {
					Logger.info(e.toString());
				}
				
				int pixelsSelectedFromQuery = currentSelection.countSelectedPixels();
				float perc = 1.0f;
//				Logger.info("  Num pixels selected from query: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
				
				if (oldSelection != null) {
					// remove the old selection from the current/new selection
					//	this prevents us from running a transform on land that is
					//	already transformed....
					currentSelection.removeSelection(oldSelection);
					int actualPixelsSelectedFromQuery = currentSelection.countSelectedPixels();
//					Logger.info("  Num pixels selected after removing old selection: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
					perc = actualPixelsSelectedFromQuery / (float)pixelsSelectedFromQuery;
					detailedLog("  Pixels removed from selection: " +
						Integer.toString(pixelsSelectedFromQuery - actualPixelsSelectedFromQuery));
				}
				
				detailedLog("  Actual selection percentage: " + 
					Float.toString(perc * 100));
				
				// Run the transform on a (possibly) reduced selection
				//	e.g., if this is the second or later query in a series,
				//	the first (highest priority) transform will trump any subsequent transforms
				int x, y;
								
				for (y = 0; y < currentSelection.mHeight; y++) {
					for (x = 0; x < currentSelection.mWidth; x++) {
						if (currentSelection.isSelected(x, y)) {
							rotationToTransform[y][x] = newLandUse;			
						}
					}
				}
				
				if (oldSelection != null) {
					// Now grow the selection up to be the sum of both selections
					//	...thereby potentially growing the selection up to include
					//	more pixels...which will then be candidates for being excluded
					//	from subsequent transform passes...
					currentSelection.combineSelection(oldSelection);
//					Logger.info("  Num pixels selected after combining new and old selection: " +
//						Integer.toString(currentSelection.countSelectedPixels()));
				}
				
				oldSelection = currentSelection;
			}
			
			mSelection = currentSelection;
		}
		
		return rotationToTransform;
	}
}

