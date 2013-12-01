package controllers;

import play.*;
import play.mvc.*;
import graph.RelationshipTypes;

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
    
    public static void addSnippet(String name, String text, String snippetType) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = new Snippet();
    	snippet.name = name;
    	snippet.text = text;
    	snippet.addRelationship(RelationshipTypes.AUTHOR, user);
    	
    	SnippetType type = SnippetType.findByName(name);
    	if (type!=null) {
    		snippet.addRelationship(RelationshipTypes.SNIPPET_TYPE, type);
    	}
    	
    	snippet.save();
    	index();
    }

}