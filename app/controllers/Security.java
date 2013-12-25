package controllers;

import models.User;
import play.data.validation.Required;
import play.mvc.Controller;
import play.mvc.Scope.Session;

public class Security extends Controller {
	
	public static void login() {
		render();
	}
	
	public static void logout() {
		Session.current().clear();

		Application.index();
	}
	
	public static void register() {
		render();
	}
	
	public static void processRegistration(@Required String username, String email, @Required String password) {
		User user = new User();
		user.username = username;
		user.email = email;
		user.setPassword(password);
		user.save();
		
		Session.current().put("user", user.username);
		Application.index();
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
