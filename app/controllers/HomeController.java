package controllers;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
//import com.gargoylesoftware.htmlunit.javascript.host.Console;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import analysis.Analyzer_HistogramNew;
import analysis.Analyzer_LandcoverProportions;
import analysis.Analyzer_ModelResults;
import analysis.ModelResult;
import analysis.ScenarioSetupResult;
import fileHandling.QueuedWriter;
import models.OldEngine;
import play.Logger;
import play.mvc.*;
import utils.GlobalAssumptions;
import utils.Json;
import utils.RandomString;
import utils.ServerStartup;
import utils.Subset;
import query.Layer_Base;
import query.Query;
import query.Scenario;
import resources.Farm;

//• On connect:
//	• Create new user id if needed (stored in cookies)

//• Assign any short-term scenario resources:
//	• Main one might be a clone of the farm data set which the user can then modify
//	• Secondary one might be the current scenario itself so the user has the option of refreshing the browser but still
//		keeping their scenario setup
//	• Another might be a copy of the assumptions, again so they could be cached for a browser refresh?

//	• Client changes:
//	• Alert user about cookie policy (cookies required). Specifically, cookies are used to link your scenarios to the
//		computing resources that will process the scenario. Cookies may be used for research purposes 
//		(linking multiple scenario runs...ie, tracking what types of things users are doing)

//	• Render assigned farm data set points
//	• When farm data set changes, need to send new to client and redraw
//	• Scenario setups could be sent more regularly and stored in the session cache?

// 	• dynamic masking may be doable with: https://github.com/come/csg2d.js/

//	• general design changes: store things like job keys, scenario keys, and similar in cookies?

/*SmartScape 2.0 Design Considerations

Performance Management / Resource Cleanup:
	•	Unbounded access to CPU and MEM can't be allowed in the future
	•	Dealing with disk asset cleanup
	•	Overall reduction of disk footprint for temporary-ish results
	•	Efficiently managing models that can communicate with other models and/or rely on another model for results before doing its own work

User / Website Related:
	•	ADA accessibility considerations (visually impaired) and color blind
	•	Cookie transparency, cookie user agreements, general cookie policy
	•	Google analytics tracking concerns
	•	Registered user features complications. Requires tracking email addresses, storing passwords, risk of data breach
	•	Mobile vs. desktop layout considerations. Mobile design likely needs to be hugely different and overall simplified
	•	Lack of level-of-detail improvements to selection/heatmap displays during map zoom
	•	Satellite map layer requirements. Most likely paid unless we host Open Street Maps data here. Which then opens up the requirement to periodically update that to maintain accuracy. Plus additional server resources (map server) and setup complications
	•	Reliance on ExtJs libraries and potential for licensing requirements

Area of Interest / subsetting:
	•	requirement for full datasets for each area
	•	ease of defining new areas of interests, potentially outside of Wisconsin
	•	Inter-screen communication between Portal and Application

Models:
	•	Effort to develop new models.
	•	General complexity of model modules communicating and sharing results.
	•	Potential for one or more models to not be relevant or available based on area and/or lack of input data
	•	Provide feedback on overall model run progress
	•	Potentially have a model run queue and feedback to the end-user where they are in the run queue
*/

//------------------------------------------------------------------
public class HomeController extends Controller {
	
	public class SessionCacheStore {
		
		public Long mLastAccessSeconds;
		public JsonNode mQueryDef;
		// TODO: Store the cached Farm here
		// TODO: expire these on a process thread
		
		public SessionCacheStore() {
			updateAccessSeconds();
		}
		public void updateAccessSeconds() {
			this.mLastAccessSeconds = System.currentTimeMillis() / 1000l;
		}
		public Long getAgeSeconds() {
			return System.currentTimeMillis() / 1000l - this.mLastAccessSeconds;
		}
		public void query(JsonNode queryDef) {
			mQueryDef = queryDef;
		}
		public JsonNode query() {
			return mQueryDef;
		}
		
	}
	
	static Map<String,HomeController.SessionCacheStore> userCache = new ConcurrentHashMap<String,HomeController.SessionCacheStore>(32);

	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
		Logger.info("Server Startup Finished");
	}
	
	//------------------------------------------------------------------
	public Result landing() {
		return ok(views.html.landing.render());
	}
	
	//--------------------------------------------------------------------------
	public Result getLandcoverProportions() {
		
		return ok(Analyzer_LandcoverProportions.get(request().body().asJson()));
	}

	//--------------------------------------------------------------------------
	public Result getRadarData() {

		return ok(Analyzer_ModelResults.get(request().body().asJson()));
	}
	
	//------------------------------------------------------------------
	public Result main() {
		return ok(views.html.main.render());
	}

	//------------------------------------------------------------------
	// SmartScape APP related
	//------------------------------------------------------------------
	
	//------------------------------------------------------------------
	public Result app() throws Exception {

		refreshUser();
		return ok(views.html.app.render());
	}
	
	// TODO: update?
	//------------------------------------------------------------------
	private SessionCacheStore refreshUser() throws Exception {
		
		String user = session("user");
		SessionCacheStore cache = null;
		
		// Sessions can outlast the internal cache...
	  	if (user != null) {
	  		if (!userCache.containsKey(user)) {
	  			// user wash flushed from the internal cache, reset
	  			cache = new SessionCacheStore();
	    		userCache.put(user, cache);
	  		}
	  		else {
	  			// update active user timers to delay the flush event
	  			cache = userCache.get(user);
	  			cache.updateAccessSeconds();
	  		}
	  		return cache;
	  	}
	  	
		int trySanityCount = 0;
	  	while (trySanityCount < 512) {
	    	user = RandomString.get(16);
	    	if (userCache.containsKey(user)) {
	    		user = null;
	    		trySanityCount++;
	    	}
	    	else {
	    		cache = new SessionCacheStore();
	    		userCache.put(user, cache);
	            session("user", user);
	    		break;
	    	}
	    }
	  	
	  	if (user == null || !userCache.containsKey(user) || cache == null) throw new Exception("no user id assigned");
	  	return cache;
	}
	
	//------------------------------------------------------------------------------------------------
	private SessionCacheStore refreshUser(JsonNode queryDef) throws Exception {
		SessionCacheStore cache = refreshUser();
		if (cache != null) {
			cache.query(queryDef);
		}
		
		return cache;
	}
	
	//--------------------------------------------------------------------------
	// SmartScape APP startup related
	//--------------------------------------------------------------------------
	
	// TODO: generalize to any subset related startup details??
	//--------------------------------------------------------------------------
	public Result getMapMask() {
		
		return ok(Subset.getSubset(0).getMapMask());
	}
	
	//--------------------------------------------------------------------------
	public Result layerParmRequest() {
		
		JsonNode request = request().body().asJson();
		try {
			JsonNode ret = Layer_Base.getParameter(request);
			if (ret != null) 
			{
				return ok(ret);
			}
		}
		catch(Exception e) {
			Logger.error(e.toString());
		}
		
		return badRequest(); // TODO: add return errors if needed...
	}
	
	//--------------------------------------------------------------------------
	// SmartScape APP query related
	//--------------------------------------------------------------------------

	// Called on startup but also called when farms are changed via a farm transform
	//--------------------------------------------------------------------------
	public Result getFarmGeoJson() {
		return ok(Farm.toGeoJson());
	}

	//--------------------------------------------------------------------------
	public Result createSelection() throws Exception {

		SessionCacheStore cache = refreshUser(request().body().asJson());
		
		Query query = new Query();
		JsonNode result = query.selection(cache.mQueryDef);//request().body().asJson());
		return ok(result);
	}

	//--------------------------------------------------------------------------
	public Result showOcclusion() throws Exception {

		Query query = new Query();
		JsonNode request = request().body().asJson();
		JsonNode first = request.get("first"),
				second = request.get("second");
		
		Logger.info("Called into query");
		Logger.info(request.toString());
		if (first.isNull()) {
			JsonNode result = query.selection(second);
			return ok(result);
		}
		else {
			JsonNode result = query.occludedSelection(first, second);
			return ok(result);
		}
	}
	
	//--------------------------------------------------------------------------
	// SmartScape APP Model run related
	//--------------------------------------------------------------------------

	//--------------------------------------------------------------------------
	public Result requestModelRun() throws Exception {

		// FIXME: TODO:
		String clientID = "jeff";
		String folder = "client_" + clientID + "/0";
		JsonNode request = request().body().asJson();
		
		// NOTE: user can be null
		Scenario scenario = new Scenario(null);
		// FIXME:
		scenario.setAssumptions(Json.pack("assumptions", GlobalAssumptions.getDefaultsForClient()));
		scenario.getTransformedRotation(request);
		scenario.mOutputDir = folder;
		
		String cacheID = Scenario.cacheScenario(scenario, clientID);

		ObjectNode sendback = JsonNodeFactory.instance.objectNode();
		sendback.put("scenarioID", cacheID);
		
		QueuedWriter.queueResults(new ScenarioSetupResult(folder, scenario.mNewRotation,
			scenario.mSelection.mRasterData, scenario.mSelection.mWidth, scenario.mSelection.mHeight));
		//ScenarioLogger.queueResults(scenario);
		
		String jobKey = OldEngine.scheduleJob(scenario);
		
		// TODO: save jobKey in session?
		return ok(Json.pack("scenarioID", cacheID, "jobKey", jobKey));
	}

	//--------------------------------------------------------------------------
	public Result getModelRunProgress() throws Exception {

		JsonNode request = request().body().asJson();
		String jobKey = Json.safeGetString(request, "jobKey");
		
		Boolean done = OldEngine.isJobDone(jobKey);
		Logger.error("Is done? " + done);
		
		return ok(Json.pack("done", done));
	}
	
	// TODO: add more logging here. is getting a null pointer exception in Analyzer_HistogramNew.binify(line 269)
	//--------------------------------------------------------------------------
	public Result getModelRunResults() throws Exception {
		
		JsonNode request = request().body().asJson();
		
		String scenarioID = Json.safeGetString(request, "scenarioID");
		String jobKey = Json.safeGetString(request, "jobKey");
		
		Scenario scenario = Scenario.getCachedScenario(scenarioID);
		List<ModelResult> results = OldEngine.getJobResults(jobKey);

		// SendBack to Client
		ObjectNode sendBack  = JsonNodeFactory.instance.objectNode();
		
		if (results == null) {
			// FIXME: Um...well, OK but not OK at all
			return ok();
		}
		Analyzer_HistogramNew histogram = new Analyzer_HistogramNew();
		
		// Try to do an in-memory compare of (usually) default...
		//	if layer is not in memory, try doin a file-based compare
		for (int i = 0; i < results.size(); i++) {
			
			ModelResult res = results.get(i);
			
			String clientID = Json.safeGetOptionalString(request, "clientID", "jeff");
			String clientFolder = "client_" + clientID + "/";
			int compare1ID = Json.safeGetOptionalInteger(request, "compare1ID", -1); // is to compare against default
			String runFolder = Integer.toString(compare1ID) + "/";
		
			String path1 = "";
			// Asking to compare against DEFAULT?
			if (compare1ID == -1) {
				path1 = "default/" + res.mName;
				
				// See if the layer is in memory (it usually will be unless the server was started
				//	with the DEFAULTS NOT loaded...)
				Layer_Base layer = Layer_Base.getLayer(path1);
				if (layer != null) {
					// other layer is in memory so compare with that.
					float[][] data1 = layer.getFloatData();
					if (data1 == null) {
						Logger.error("could not get layer in runModelCluster");
					}
					else {
						sendBack.set(res.mName, 
							histogram.run(res.mWidth, res.mHeight, data1, scenario.mSelection,
											res.mRasterData, scenario.mSelection));
					}
					continue; // process next result...
				}
			}
			else {
				path1 = clientFolder + runFolder + res.mName;
			}
			
			// Compare to file was not in memory, set up the real path and we'll try to load it for
			//	comparison (which is slower...booo)
			path1 = "./layerData/" + path1 + ".dss";
			sendBack.set(res.mName, 
					histogram.run(new File(path1), scenario.mSelection,
									res.mWidth, res.mHeight, res.mRasterData, scenario.mSelection));
		}
		// decrement ref count and remove it for real if not needed...
		Scenario.releaseCachedScenario(scenarioID);
		QueuedWriter.queueResults(results);

		return ok(sendBack);
	}
}
