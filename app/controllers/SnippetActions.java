package controllers;

import graph.RelationshipTypes;

import java.util.List;

import play.Logger;
import models.Snippet;
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
}
