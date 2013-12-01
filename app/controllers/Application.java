package controllers;

import play.*;
import play.mvc.*;

import java.util.*;

import models.*;

public class Application extends Controller {

	@Before
	public static void authenticate() {
		User user = Security.getLoggedInUser();
		if (user==null) Security.login();
	}
    public static void index() {
		User user = Security.getLoggedInUser();
    	List<Snippet> snippets = user.getSnippets();
        render(user, snippets);
    }

}