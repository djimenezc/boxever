package controllers;

import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

public class Application extends Controller {

	public static Result getCurrencyRateData(final String value) {
		return ok("value: {Boxever -- Currency Monitor}");
	}

	public static Result index() {
		return ok(index.render("Boxever -- Currency Monitor"));
	}

	public static Result refresh(final String value) {
		return ok(index.render("Boxever -- Currency Monitor"));
	}

	public static Result refreshAll() {
		return ok("value: {Boxever -- Currency Monitor}");
	}

}
