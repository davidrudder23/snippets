package controllers;

import play.*;
import play.mvc.*;
import graph.RelationshipTypes;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import jsnlog.JSNLog;

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
		User user = Security.getLoggedInUser();

    	render();
    }
    
    public static void getMySnippetAjaxHtml(Long snippetId) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetId != null) {
    		snippet = Snippet.findById(snippetId);
    	}
    	
    	if (snippet != null) {
        	render(snippet);
    		
    	}

    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets != null) && (mySnippets.size()>0)) {
    		snippet = mySnippets.get(0);
    		render(snippet);
    	}

		renderText("Add a snippet");
    }
    
    public static void getMySnippetAjaxJson(Long snippetId) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetId != null) {
    		snippet = Snippet.findById(snippetId);
    	}
    	
    	if (snippet != null) {
    		snippet.tags = snippet.getTags();
    		snippet.tags = snippet.getTags();
        	renderJSON(snippet);
    		
    	}

    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets != null) && (mySnippets.size()>0)) {
    		snippet = mySnippets.get(0);
    		snippet.tags = snippet.getTags();
    		renderJSON(snippet);
    	}

		renderText("Add a snippet");
    }
    
    public static void getMySnippetNextAjaxJson(Long snippetId) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetId == null) {
        	getMySnippetAjaxJson(null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
        	getMySnippetAjaxJson(null);
    	}

    	boolean foundIt = false;
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.id.equals(snippetId)) {
    			foundIt = true;
    		} else if (foundIt) {
    			snippet = mySnippet;
        		snippet.tags = snippet.getTags();
    			renderJSON(snippet);
    		}
    	}
    	
    	if (foundIt) {
			snippet = mySnippets.get(0);
    		snippet.tags = snippet.getTags();
			renderJSON(snippet);
    	}
    	getMySnippetAjaxJson(null);
    }
    
    public static void getMySnippetPrevAjaxJson(Long snippetId) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetId == null) {
        	getMySnippetAjaxJson(null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
        	getMySnippetAjaxJson(null);
    	}

    	snippet = mySnippets.get(mySnippets.size()-1);
    	
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.id.equals(snippetId)) {
        		snippet.tags = snippet.getTags();
    			renderJSON(snippet);
    		}
    		snippet = mySnippet;
    	}
    	getMySnippetAjaxJson(null);
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
    
	public static void jsnLog() {
		try {
			InputStream postBody = Http.Request.current().body;
			
			StringBuffer json = new StringBuffer();
			
			byte[] inputBuffer = new byte[1024];
			int size = 0;
			while ((size = postBody.read(inputBuffer, 0, inputBuffer.length)) > 0) {
				json.append(new String(inputBuffer, 0, size));
			}
			
			JSNLog.getPlayFramework1_xLogger().log(json.toString());
			
			renderText("{status: \"success\"}");
		} catch (IOException e) {
			Logger.warn(e, "Could not log JSNLog message");
			renderText("{status: \"failed\", message:\"%s\"}", e.getMessage());
		}
	}
}