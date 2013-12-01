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
		List<Model> relations = getRelations(RelationshipTypes.AUTHOR, getId(), getClass(), Snippet.class);
		
		authors = new ArrayList<User>();
		
		for (Model relation: relations) {
			if (relation instanceof User) {
				authors.add((User)relation);
			}
		}
		
		return authors;
	}
	
	@PostUpdate
	public void postUpdate() {
		authors = null;
	}
	
}
