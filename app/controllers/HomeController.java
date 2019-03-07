package controllers;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.inject.Inject;

import play.Logger;
import play.mvc.*;
import utils.RandomString;
import utils.ServerStartup;

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
	
	//------------------------------------------------------------------
	public Result index() {
		return ok(views.html.index.render());
	}
	
	//------------------------------------------------------------------
	public Result nav() {
		return ok(views.html.nav.render());
	}
	
	//------------------------------------------------------------------
	public Result nav_hydro() {
		return ok(views.html.nav_hydro.render());
	}
	
	//------------------------------------------------------------------
	public Result nav2() {
		return ok(views.html.nav2.render()); 
	}

	//------------------------------------------------------------------
	public Result alt() {
		
		String user = session("user");
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
		Logger.info(user);
		return ok(views.html.alt.render());
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
}
