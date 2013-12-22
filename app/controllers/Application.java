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
	public static void authenticate() {
		User user = Security.getLoggedInUser();
		if (user==null) Security.login();
	}

    public static void index() {
    	snippet(null, null);
    }
    
    public static void snippet(Long snippetId, String snippetName) {
		User user = Security.getLoggedInUser();
		
		Snippet snippet = null;
		if (snippetId!=null) {
			snippet = Snippet.findById(snippetId);
		}
		
		if ((snippet == null) && (snippetName!=null)) {
			snippet = Snippet.find("byName", snippetName).first();
		}
		
		if (snippet == null) {
	    	List<Snippet> mySnippets = user.getSnippets();
	    	
	    	if ((mySnippets != null) && (mySnippets.size()>0)) {
	    		snippet = mySnippets.get(0);
	    	}
		}

    	render(snippet);
    }
    
    public static void getNextSnippet(String currentSnippetName) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (currentSnippetName == null) {
			snippet(null, null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
			snippet(null, null);
    	}

    	boolean foundIt = false;
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.name.equals(currentSnippetName)) {
    			foundIt = true;
    		} else if (foundIt) {
    			snippet = mySnippet;
    			snippet(null, snippet.name);
    		}
    	}
    	
    	if (foundIt) {
			snippet = mySnippets.get(0);
			snippet(null, snippet.name);
    	}
    	snippet(null, null);
    	
    }
    
    public static void getPrevSnippet(String snippetName) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetName == null) {
        	snippet(null, null);
    	}
    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets == null) || (mySnippets.size()<=1)) {
        	snippet(null, null);
    	}

    	snippet = mySnippets.get(mySnippets.size()-1);
    	
    	for (Snippet mySnippet: mySnippets) {
    		if (mySnippet.name.equals(snippetName)) {
    			snippet(null, snippet.name);
    		}
    		snippet = mySnippet;
    	}
    	snippet(null, null);
    }
    
    
    public static void getMySnippetAjaxJson(Long snippetId) {
		User user = Security.getLoggedInUser();
    	Snippet snippet = null;
    	if (snippetId != null) {
    		snippet = Snippet.findById(snippetId);
    	}
    	
    	if (snippet != null) {
			renderJSON(snippet, snippet.getJsonSerializer());
    	}

    	List<Snippet> mySnippets = user.getSnippets();
    	
    	if ((mySnippets != null) && (mySnippets.size()>0)) {
    		snippet = mySnippets.get(0);
			renderJSON(snippet, snippet.getJsonSerializer());
    	}

		renderText("Add a snippet");
    }
    

    public static void addSnippet(String name, String text, String snippetType, Long relatedSnippetId) {
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

    	Snippet relatedSnippet = null;
    	if (relatedSnippetId != null) {
    		relatedSnippet = Snippet.findById(relatedSnippetId);
    	}
    	if (relatedSnippet != null) {
    		snippet.addRelationship(RelationshipTypes.RELATED_SNIPPET, relatedSnippet);
    	}
    	snippet(null, snippet.name);
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