package controllers;

import models.User;
import play.mvc.Controller;
import play.mvc.Scope.Session;

public class Security extends Controller {
	
	public static void login() {
		render();
	}
	
	public static User getLoggedInUser() {
		String username = Session.current().get("user");
		if (username == null) return null;
		User user = User.findByUsername(username);
		
		return user;
	}
	
	public static boolean isLoggedIn() {
		return getLoggedInUser() != null;
	}
	
	public static void authenticate(String username, String password) {
		User user = User.findByLogin(username, password);
		
		if (user == null) {
			flash.error("Login failed");
			login();
		}
		
		Session.current().put("user", user.username);
		
		Application.snippet(null);
	}

}
