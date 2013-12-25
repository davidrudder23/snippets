package controllers;

import graph.RelationshipTypes;

import java.util.HashMap;
import java.util.List;

import play.Logger;
import models.Snippet;
import models.SnippetType;
import models.User;

public class SnippetActions extends AbstractController {
	
	public static void addRelationshipModal(String snippetSeoName) {
		User user = Security.getLoggedInUser();
		// We'll need a list of all the snippets that we can write to
		
		Snippet snippet = Snippet.find("bySeoname", snippetSeoName).first();
		if (snippet == null) {
			renderText("Could not find snippet");
		}
		
		List<Snippet> snippets = Snippet.getReadableSnippets(user);
		
		if (snippets.contains(snippet)) {
			snippets.remove(snippet);
		}
		
		List<Snippet> alreadyRelatedSnippets = snippet.getRelatedSnippets();
		
		for (Snippet alreadyRelatedSnippet: alreadyRelatedSnippets) {
			if (snippets.contains(alreadyRelatedSnippet)) {
				snippets.remove(alreadyRelatedSnippet);
			}
		}
		
		render (user, snippet, snippets);
	}

	public static void addRelationship(String sourceSeoName, String destSeoName) {
		
		Snippet sourceSnippet = Snippet.find("bySeoname", sourceSeoName).first();
		Snippet destSnippet = Snippet.find("bySeoname", destSeoName).first();
		
		if (sourceSnippet == null) {
			Logger.debug ("Source snippet is null");
			Application.snippet(null);
		}
		
		if (destSnippet == null) {
			Logger.debug ("Dest snippet is null");
			Application.snippet(sourceSnippet.seoName);
		}
		
		Logger.debug ("Relating %s to %s", sourceSnippet, destSnippet);
		sourceSnippet.addRelationship(RelationshipTypes.RELATED_SNIPPET, destSnippet);
		Application.snippet(sourceSnippet.seoName);
	}
	
	
	public static void addNoteModal(String snippetSeoName) {
		User user = Security.getLoggedInUser();
		
		Snippet snippet = Snippet.findBySeoName(snippetSeoName);
		if (snippet == null) {
			renderText("Could not find snippet");
		}
		
		render (user, snippet);
	}

    public static void addNote(String snippetSeoName, String text) {
		User user = Security.getLoggedInUser();
		
		Snippet snippet = Snippet.findBySeoName(snippetSeoName);
		if (snippet == null) {
			Application.snippet(null);
		}

		HashMap<String, String> attributes = new HashMap<String, String>();
		attributes.put("text", text);
		user.addRelationship(RelationshipTypes.NOTE, snippet, attributes);
		Application.snippet(snippetSeoName);

    }

	public static void addSnippetModal() {
		User user = Security.getLoggedInUser();
		
		render (user);
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
    	Application.snippet(snippet.seoName);
    }

}