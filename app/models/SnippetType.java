package models;

import javax.persistence.Entity;

import graph.GraphModel;

@Entity
public class SnippetType extends GraphModel {

	public String name;
	
	public String renderedClass;
	
	public String rendererName;
	
	public static SnippetType findByName(String name) {
		return SnippetType.find("byName", name).first();
	}
}
