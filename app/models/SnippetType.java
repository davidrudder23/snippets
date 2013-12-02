package models;

import javax.persistence.Entity;
import javax.persistence.PrePersist;

import org.apache.commons.lang.StringUtils;

import graph.GraphModel;

@Entity
public class SnippetType extends GraphModel {

	public String name;
	
	public String rendererClass;
	
	public String rendererName;
	
	@PrePersist
	public void prePersist() {
		if (StringUtils.isEmpty(rendererClass)) {
			rendererClass = "graph.renderer."+name+"Renderer";
		}
		if (StringUtils.isEmpty(rendererName)) {
			rendererName = name;
		}
	}
	public static SnippetType findByName(String name) {
		return SnippetType.find("byName", name).first();
	}
}
