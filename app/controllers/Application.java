package controllers;

import play.*;
import play.mvc.*;
import graph.RelationshipTypes;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.*;

import jsnlog.JSNLog;

import org.apache.commons.lang.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import models.*;

public class Application extends AbstractController {

	@Before
	public static void authenticateAndSetup() {
		User user = Security.getLoggedInUser();
		if (user==null) Security.login();
	}

    public static void index() {
    	snippet(null);
    }
    
    public static void snippet(String snippetName) {
		User user = Security.getLoggedInUser();
		
		Snippet snippet = null;
		snippet = Snippet.find("bySeoName", snippetName).first();

		if (snippet == null) {
	    	List<Snippet> mySnippets = user.getSnippets();
	    	Logger.debug ("Found snippets - %s", mySnippets);
	    	
	    	if ((mySnippets != null) && (mySnippets.size()>0)) {
	    		snippet = mySnippets.get(0);
	    	}
		}
		
		if (snippet == null) {
			newSnippet();
		}

    	render(snippet);
    }
    
    public static void newSnippet() {
		User user = Security.getLoggedInUser();
    	Snippet snippet = new Snippet();
    	
    	render(user, snippet);
    }
    
    public static void getNextSnippet(String currentSnippetName) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (currentSnippetName == null) {
			snippet(null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
			snippet(null);
    	}

    	boolean foundIt = false;
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.seoName.equals(currentSnippetName)) {
    			foundIt = true;
    		} else if (foundIt) {
    			snippet = mySnippet;
    			snippet(snippet.seoName);
    		}
    	}
    	
    	if (foundIt) {
			snippet = mySnippets.get(0);
			snippet(snippet.seoName);
    	}
    	snippet(null);
    	
    }
    
    public static void getPrevSnippet(String snippetName) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetName == null) {
        	snippet(null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
        	snippet(null);
    	}

    	snippet = mySnippets.get(mySnippets.size()-1);
    	
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.seoName.equals(snippetName)) {
    			snippet(snippet.seoName);
    		}
    		snippet = mySnippet;
    	}
    	snippet(null);
    }
    
	public static void tag(String tagName) {
		Tag tag = Tag.find("byName", tagName).first();
		
		if (tag == null) Application.index();
		
		render(tag);
	}
	
    public static void getNextTag(String currentTagName) {
		User user = Security.getLoggedInUser();
    	Tag tag = null;
    	if (currentTagName == null) {
			tag(null);
    	}
    	List<Tag> myTags = user.getTags();
    	
    	if ((myTags == null) || (myTags.size()<=1)) {
			tag(null);
    	}

    	boolean foundIt = false;
    	for (Tag myTag: myTags) {
    		if (myTag.name.equals(currentTagName)) {
    			foundIt = true;
    		} else if (foundIt) {
    			tag = myTag;
    			tag(tag.name);
    		}
    	}
    	
    	if (foundIt) {
			tag = myTags.get(0);
			tag(tag.name);
    	}
    	tag(null);
    	
    }
    
    public static void getPrevTag(String tagName) {
		User user = Security.getLoggedInUser();
    	Tag tag = null;
    	if (tagName == null) {
        	tag(null);
    	}
    	List<Tag> myTags = user.getTags();
    	
    	if ((myTags == null) || (myTags.size()<=1)) {
        	tag(null);
    	}

    	tag = myTags.get(myTags.size()-1);
    	
    	for (Tag myTag: myTags) {
    		if (myTag.name.equals(tagName)) {
    			tag(tag.name);
    		}
    		tag = myTag;
    	}
    	tag(null);
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