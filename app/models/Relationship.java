package models;

import graph.GraphModel;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import play.Logger;
import play.db.jpa.Model;

@Entity
public class Relationship extends Model {
	
	public String type;
	
	public String sourceClass;
	public Long sourceId;
	
	public String destinationClass;
	public Long destinationId;
	
	@Transient
	public HashMap<String, String> attributes;
	
	@Lob()
	public String attributesJson; 
	
	@PostLoad
	public void parseAttributes() {
		if (attributesJson == null) return;
		
		Gson gson = new Gson();
		attributes = (HashMap<String, String>)gson.fromJson(attributesJson, HashMap.class);
	}
	
	@PrePersist
	public void encodeAttributes() {
		if ((attributes == null) || (attributes.size()<=0)) {
			attributesJson = "";
			return;
		}
		Gson gson = new Gson();
		attributesJson = gson.toJson(attributes);
	}
	
	public GraphModel getTarget(Class targetClass) {
		String targetClassName = targetClass.getName();
		Long targetId = null;
		
		Logger.debug ("Looking for targetClass=%s", targetClassName);
		
		if (destinationClass.equals(targetClassName)) {
			targetId = destinationId;
		} else if (sourceClass.equals(targetClassName)) {
			targetId = sourceId;
		}
		
		Logger.debug ("targetId=%d", targetId);
		if (targetId == null) {
			return null;
		}
		
		try {
			Method findMethod = targetClass.getMethod("findById", Object.class);
			
			if (findMethod==null) {
				Logger.warn("Could not find findById method in JPA Model with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
				return null;
			}
			
			GraphModel model = (GraphModel)findMethod.invoke(null, targetId);
			
			return model;
		} catch (Exception e) {
			Logger.warn(e, "Could not load relations with arguments id: %d sourceClass: %s destClass: %s", sourceId, sourceClass, destinationClass);
		}

		
		return null;
	}

}
