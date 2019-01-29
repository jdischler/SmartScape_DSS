package controllers;

import play.mvc.*;

public class HomeController extends Controller {

	public Result index() {
		return ok(views.html.index.render());
	}
	
	public Result nav() {
		return ok(views.html.nav.render());
	}

	public Result alt() {
		return ok(views.html.alt.render());
	}	
}
