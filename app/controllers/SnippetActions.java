package controllers;

import graph.RelationshipTypes;

import java.util.HashMap;
import java.util.List;

import play.Logger;
import models.Relationship;
import models.Snippet;
import models.SnippetType;
import models.Tag;
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
			Application.snippet(null);
		}
		
		if (destSnippet == null) {
			Application.snippet(sourceSnippet.seoName);
		}
		
		sourceSnippet.addRelationship(RelationshipTypes.RELATED_SNIPPET, destSnippet);
		Application.snippet(sourceSnippet.seoName);
	}
	
	public static void removeRelationship(String parentSnippetName, String relatedSnippetName) {
		Snippet parent = Snippet.findBySeoName(parentSnippetName);
		if (parent == null) {
			Application.snippet(null);
		}
		Snippet related = Snippet.findBySeoName(relatedSnippetName);
		if (related == null) {
			Application.snippet(parentSnippetName);
		}
		
		Relationship relationship = Relationship.find("bySourceClassAndSourceIdAndDestinationClassAndDestinationId", Snippet.class.getName(), parent.id, Snippet.class.getName(), related.id).first();
		if (relationship == null) {
			relationship = Relationship.find("bySourceClassAndSourceIdAndDestinationClassAndDestinationId", Snippet.class.getName(), related.id, Snippet.class.getName(), parent.id).first();
		}
		
		if (relationship == null) {
			Application.snippet(parentSnippetName);
		}
		
		relationship.delete();
		Application.snippet(parentSnippetName);
		
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
    
	public static void deleteSnippet(String snippetName) {
		Snippet snippet = Snippet.findBySeoName(snippetName);
		if (snippet == null) {
			Application.index();
		}
		
		snippet.delete();
		Application.index();
	}
	
	public static void removeTag(String snippetName, String tagName) {
		Snippet parent = Snippet.findBySeoName(snippetName);
		if (parent == null) {
			Application.snippet(null);
		}
		Tag tag = Tag.find("byName", tagName).first();
		if (tag == null) {
			Application.snippet(snippetName);
		}
		
		Relationship relationship = Relationship.find("bySourceClassAndSourceIdAndDestinationClassAndDestinationId", Snippet.class.getName(), parent.id, Tag.class.getName(), tag.id).first();
		if (relationship == null) {
			relationship = Relationship.find("bySourceClassAndSourceIdAndDestinationClassAndDestinationId", Snippet.class.getName(), tag.id, Tag.class.getName(), parent.id).first();
		}
		
		if (relationship == null) {
			Application.snippet(snippetName);
		}
		
		relationship.delete();
		Application.snippet(snippetName);
		
	}
}
