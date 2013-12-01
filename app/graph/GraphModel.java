package graph;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Relationship;
import play.Logger;
import play.db.jpa.Model;

public abstract class GraphModel extends Model {
	
	public void addRelationship(GraphModel relatedTo) {
		addRelationship(relatedTo, null);
	}
	public void addRelationship(GraphModel relatedTo, HashMap<String, String> attributes) {
		save();
		Relationship relationship = new Relationship();
		relationship.sourceId = getId();
		relationship.sourceClass = getClass().getName();
		
		relationship.destinationId = relatedTo.getId();
		relationship.destinationClass = relatedTo.getClass().getName();
		
		relationship.attributes = attributes;
		relationship.save();
	}
	
	public List<Model> getRelations(Long sourceId, Class sourceClass, Class destinationClass) {
		List<Relationship> relationships = Relationship.find("bySourceIdAndSourceClassAndDestinationClass", sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		Logger.debug ("Found %s relationships from source to dest", relationships.size());
		List<Model> relations = new ArrayList<>();
		
		try {
			Method findMethod = sourceClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return relations;
			}
			
			for (Relationship relationship: relationships) {
				Model model = (Model)findMethod.invoke(null, relationship.destinationId);
				relations.add(model);
			}
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}
		
		relationships = Relationship.find("byDestinationIdAndDestinationClassAndSourceClass", sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		Logger.debug ("Found %s relationships from dest to source", relationships.size());
		
		try {
			Method findMethod = destinationClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return relations;
			}
			
			for (Relationship relationship: relationships) {
				Model model = (Model)findMethod.invoke(null, relationship.sourceId);
				Logger.debug ("Found model=%s", model);
				relations.add(model);
			}
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}
		
		return relations;
	}

}
