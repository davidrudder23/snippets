package controllers;

import models.Tag;
import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class TagController extends Controller {
	@Before
	public static void authenticate() {
		User user = Security.getLoggedInUser();
		if (user==null) Security.login();
	}


}
