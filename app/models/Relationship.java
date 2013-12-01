package models;

import java.util.HashMap;

import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import play.db.jpa.Model;

@Entity
public class Relationship extends Model {
	
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

}
