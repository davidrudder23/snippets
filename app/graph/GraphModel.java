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
	
	public void addRelationship(String relationshipType, GraphModel relatedTo) {
		addRelationship(relationshipType, relatedTo, null);
	}
	public void addRelationship(String relationshipType, GraphModel relatedTo, HashMap<String, String> attributes) {
		save();
		Relationship relationship = new Relationship();
		relationship.type = relationshipType;
		
		relationship.sourceId = getId();
		relationship.sourceClass = getClass().getName();
		
		relationship.destinationId = relatedTo.getId();
		relationship.destinationClass = relatedTo.getClass().getName();
		
		relationship.attributes = attributes;
		relationship.save();
	}
	
	public void removeRelationship(String relationshipType, GraphModel relatedTo) {
		List<Relationship> relationships = (List<Relationship>)getRelations(relationshipType, relatedTo.getClass());
		
		for (Relationship relationship: relationships) {
			if (relationship.destinationId.equals(relatedTo.getId())) {
				relationship.delete();
			}
		}
	}
	
	public List<?> getRelations(String relationshipType, Class destinationClass) {
		Long sourceId = getId();
		Class sourceClass = getClass();
		Logger.debug ("Getting relationships with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		List<Relationship> relationships = Relationship.find("byTypeAndSourceIdAndSourceClassAndDestinationClass", relationshipType, sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		Logger.debug ("Found %s relationships from source to dest with arguments id: %d sourceClass: %s destClass: %s", relationships.size(), sourceId, sourceClass, destinationClass);
		List<Model> relations = new ArrayList<>();
		
		try {
			Method findMethod = destinationClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return relations;
			}
			
			for (Relationship relationship: relationships) {
				Model model = (Model)findMethod.invoke(null, relationship.destinationId);
				Logger.debug ("Found model=%s", model);
				relations.add(model);
			}
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}
		
		relationships = Relationship.find("byTypeAndDestinationIdAndDestinationClassAndSourceClass", relationshipType, sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		Logger.debug ("Found %s relationships from dest to source with arguments id: %d sourceClass: %s destClass: %s", relationships.size(), sourceId, sourceClass, destinationClass);

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
