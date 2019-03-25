package controllers;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;

import play.Logger;
import play.mvc.*;
import play.mvc.Http.Context;
import utils.RandomString;
import utils.ServerStartup;
import query.Layer_Base;
import query.Query;

//------------------------------------------------------------------
public class HomeController extends Controller {
	
	public class SessionCacheStore {
		public Date mTimeStamp = new Date();
		
	}
	
	static Map<String,SessionCacheStore> userCache = new ConcurrentHashMap<String,SessionCacheStore>(32);

	//------------------------------------------------------------------
	@Inject
	public HomeController(ServerStartup startup) {
		Logger.info("Server Startup Finished");
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
	
	//------------------------------------------------------------------
	public Result landing() {
		return ok(views.html.landing.render());
	}
	
	//------------------------------------------------------------------
	public Result app() {
		
	/*	String user = session("user");
		if (user == null) {
			user = createNewUser();
		}	
		
    	if (user == null) {
	  		return badRequest("User is still null");
	  	}
    	else if (!userCache.containsKey(user)) {
    		Logger.info(user);
    		userCache.put(user, new SessionCacheStore());
	  		return badRequest("User isn't null but it isn't in the cache. Adding to cache. Refresh page...");
    	}
		Logger.info(user);*/
		return ok(views.html.app.render());
	}
	
	//------------------------------------------------------------------
	private String createNewUser() {
		
		String user = null;
		int trySanityCount = 0;
		
	  	while (user == null && trySanityCount < 256) {
	    	user = RandomString.get(16);
	    	if (userCache.containsKey(user)) {
	    		user = null;
	    		trySanityCount++;
	    	}
	    	else {
	    		userCache.put(user, new SessionCacheStore());
	            session("user", user);
	    		break;
	    	}
	    }
		return user;
	}
	
	//--------------------------------------------------------------------------
	public Result createSelection() throws Exception {

		Query query = new Query();
		JsonNode result = query.selection(request().body().asJson());
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
	
}
