package models;

import graph.GraphModel;
import graph.RelationshipTypes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PostUpdate;
import javax.persistence.Transient;

import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class Snippet extends GraphModel {
	
	@Required
	public String name;
	
	public String text;
	
	@Transient
	private List<User> authors = null;

	public static List<Snippet> findByName(String name) {
		List<Snippet> snippets = Snippet.find("byName", name).fetch();
		return snippets;
	}
	
	public List<User> getAuthors() {
		//if (authors !=null) return authors;
		authors = (List<User>)getRelations(RelationshipTypes.AUTHOR, Snippet.class);
		
		return authors;
	}
	
	public List<Tag> getTags() {
		//if (snippets!=null) return snippets;
		List<Tag> tags= (List<Tag>)getRelations(RelationshipTypes.TAGGED, Tag.class);
		
		return tags;
	}

	@PostUpdate
	public void postUpdate() {
		authors = null;
	}
	
}
