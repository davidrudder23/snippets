package controllers;

import play.*;
import play.mvc.*;
import graph.RelationshipTypes;

import java.util.*;

import org.apache.commons.lang.StringUtils;

import models.*;

public class Application extends AbstractController {

	@Before
	public static void authenticate() {
		User user = Security.getLoggedInUser();
		if (user==null) Security.login();
	}

    public static void index() {
    	mysnippets();
    }
    
    public static void mysnippets() {

        render();
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
    	mysnippets();
    }

    public static void removeTagAjaxUpdate(Long tagId, Long snippetId) {
    	if (tagId == null) {
    		renderText("Can not find tag");
    	}
    	
    	
    }
    
    public static void addTagAjaxUpdate(String name) {
    	User user = Security.getLoggedInUser();
    	Tag tag = new Tag();
    	tag.name = name;
    	tag.addRelationship(RelationshipTypes.AUTHOR, user);
    	tag.save();
    	
    	renderText("Tag %s added", name);
    }
    
    public static void getMyTagsAjaxJson() {
    	User user = Security.getLoggedInUser();
    	List<Tag> tags = user.getTags();
    	
    	List<String> tagNames = new ArrayList();
    	for (Tag tag: tags) {
   			tagNames.add(tag.name);
    	}
    	renderJSON(tagNames);
    }
    
    public static void tagSnippetAjaxUpdate(Long snippetId, String tagName, Long tagId) {
    	User user = Security.getLoggedInUser();
    	
    	Snippet snippet = Snippet.findById(snippetId);
    	if (snippet == null) {
    		renderText("Snippet not found");
    	}
    	
    	Tag tag = null;
    	if (tagId!=null) {
    		tag = Tag.findById(tagId);
    	}
    	
    	if (tag == null) {
    		if (StringUtils.isEmpty(tagName)) {
    			renderText("Tag not found");
    			return;
    		}
    		
    		tag = Tag.find("byName", tagName).first();
    	}
    	
    	if (tag == null) {
    		tag = new Tag();
    		tag.name = tagName;
    		HashMap<String, String> tagAttribs = new HashMap<String, String>();
    		tagAttribs.put("date", new Date().toString());
    		tag.addRelationship(RelationshipTypes.AUTHOR, user, tagAttribs);
    		tag.save();
    	}
    	
    	List<Tag> existingTags = snippet.getTags();
    	for (Tag existingTag: existingTags) {
    		if (existingTag.name.equals(tagName)) {
    			renderText("The snippet is already tagged with %s", tagName);
    		}
    	}

    	HashMap<String, String> tagAttribs = new HashMap<String, String>();
    	
    	tagAttribs.put("date", new Date().toString());
    	tagAttribs.put("tagger", user.username);

    	snippet.addRelationship(RelationshipTypes.TAGGED, tag, tagAttribs);
    	
    	renderText("Snippet tagged \"%s\"", tag.name);
    }
}