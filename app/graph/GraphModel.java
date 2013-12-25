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
	
	/**
	 * This gets a list of all the relationships that this model has with another
	 * of the specified type.  This returns the *relationships*, not the actual objects.
	 * One example of the use is to say "Show me all the tags I relate to, in one way
	 * or another".  We'll use this to build basic access control 
	 * 
	 * @param targetClass
	 * @return
	 */
	public List<Relationship> getRelationships(Class targetClass) {
		String thisClassName = getClass().getName();
		List<Relationship> relationships = Relationship.find("(destinationClass=? and destinationId=? and sourceClass=?) OR (sourceClass=? and sourceId=? and destinationClass=?)", 
				thisClassName, id, targetClass.getName(), 
				thisClassName, id, targetClass.getName()).fetch();
		//Logger.debug("For class %s, relationships=%s", targetClass.getName(), relationships);
		
		return relationships;
	}
	
	/**
	 * This gets a list of all the relationships that this model has with another model.  
	 * This returns the *relationships*, not the actual objects.
	 * One example of the use is to say "Show me all the tags I relate to, in one way
	 * or another".  We'll use this to build basic access control 
	 * 
	 * @param targetClass
	 * @return
	 */
	public List<Relationship> getRelationships(Class targetClass, Long targetId) {
		String thisClassName = getClass().getName();
		Logger.debug ("Getting relationships for destinationClass=%s and destinationId=%s and sourceClass=%s and sourceId=%s", thisClassName, id, targetClass.getName(), targetId);
		List<Relationship> relationships = Relationship.find("(destinationClass=? and destinationId=? and sourceClass=? and sourceId=?) OR (sourceClass=? and sourceId=? and destinationClass=? and destinationId=?)", 
				thisClassName, id, targetClass.getName(), targetId,
				thisClassName, id, targetClass.getName(), targetId).fetch();
		
		return relationships;
	}
	
	public List<?> getRelations(String relationshipType, Class destinationClass) {
		Long sourceId = getId();
		Class sourceClass = getClass();
		//Logger.debug ("Getting relationships with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		List<Relationship> relationships = Relationship.find("byTypeAndSourceIdAndSourceClassAndDestinationClass", relationshipType, sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		//Logger.debug ("Found %s relationships from source to dest with arguments id: %d sourceClass: %s destClass: %s", relationships.size(), sourceId, sourceClass, destinationClass);
		List<Model> relations = new ArrayList<>();
		
		try {
			Method findMethod = destinationClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return relations;
			}
			
			for (Relationship relationship: relationships) {
				Model model = (Model)findMethod.invoke(null, relationship.destinationId);
				//Logger.debug ("Found model=%s", model);
				relations.add(model);
			}
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}
		
		relationships = Relationship.find("byTypeAndDestinationIdAndDestinationClassAndSourceClass", relationshipType, sourceId, sourceClass.getName(), destinationClass.getName()).fetch();
		//Logger.debug ("Found %s relationships from dest to source with arguments id: %d sourceClass: %s destClass: %s", relationships.size(), sourceId, sourceClass, destinationClass);

		try {
			Method findMethod = destinationClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return relations;
			}
			
			for (Relationship relationship: relationships) {
				Model model = (Model)findMethod.invoke(null, relationship.sourceId);
				//Logger.debug ("Found model=%s", model);
				relations.add(model);
			}
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}
		
		return relations;
	}

}
