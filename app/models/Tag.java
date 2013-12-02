package models;

import javax.persistence.Entity;

import graph.GraphModel;
import graph.RelationshipTypes;

@Entity
public class Tag extends GraphModel {

	public String name;
	
	/*
	 * Gets the number of times a snippets has been tagged with this tag
	 */
	public int numberTagged() {
		return getRelations(RelationshipTypes.TAGGED, Snippet.class).size();
	}
}
