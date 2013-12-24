package models;

import graph.GraphModel;
import graph.RelationshipTypes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Transient;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import play.data.validation.Required;
import play.db.jpa.Model;
import util.StringUtils;

@Entity
public class Snippet extends GraphModel {

	@Required
	public String name;

	public String text;
	
	public String seoName;

	@Transient
	private List<User> authors = null;

	public static List<Snippet> findByName(String name) {
		List<Snippet> snippets = Snippet.find("byName", name).fetch();
		return snippets;
	}

	public List<User> getAuthors() {
		// if (authors !=null) return authors;
		authors = (List<User>) getRelations(RelationshipTypes.AUTHOR, Snippet.class);

		return authors;
	}

	public List<Tag> getTags() {
		// if (snippets!=null) return snippets;
		List<Tag> tags = (List<Tag>) getRelations(RelationshipTypes.TAGGED, Tag.class);

		return tags;
	}

	public List<Snippet> getRelatedSnippets() {
		List<Snippet> snippets = (List<Snippet>) getRelations(RelationshipTypes.RELATED_SNIPPET, Snippet.class);
		return snippets;
	}
	
	public static List<Snippet> getReadableSnippets(User user) {
		
		List<Snippet> snippets = new ArrayList();
		List<Relationship> relationships = user.getRelationships(Snippet.class);
		
		for (Relationship relationship: relationships) {
			if (RelationshipTypes.AUTHOR.equals(relationship.type)) {
				if (relationship.destinationClass.equals(Snippet.class.getName())) {
					Snippet snippet = Snippet.findById(relationship.destinationId);
					if (snippet != null) {
						snippets.add(snippet);
					}
				} else if (relationship.sourceClass.equals(Snippet.class.getName())) {
					Snippet snippet = Snippet.findById(relationship.sourceId);
					if (snippet != null) {
						snippets.add(snippet);
					}
				}
			}
		}
		
		return snippets;
		
	}
	
	@PreUpdate
	public void preUpdateHandler() {
		if (StringUtils.isEmpty(seoName))
			fixSeoName();
	}

	@PostLoad
	public void postLoadHandler() {
		if (StringUtils.isEmpty(seoName)) {
			fixSeoName();
			save();
		}
	}
	
	@PostUpdate
	public void postUpdateHandler() {
		authors = null;
	}

	public void setName(String name) {
		this.name = name;
		fixSeoName();
	}
	
	public void fixSeoName() {
		seoName = new String(name);
		seoName = seoName.trim().
				toLowerCase().
				replaceAll(" ", "_").
				replaceAll("[^0-9a-zA-Z_]", "");
	}

	public JsonSerializer<Snippet> getJsonSerializer() {
		return new JsonSerializer<Snippet>() {

			@Override
			public JsonElement serialize(Snippet snippet, Type type, JsonSerializationContext context) {
				GsonBuilder gsonBuilder = new GsonBuilder();
				Gson gson = gsonBuilder.create();

				JsonObject jsonObject = new JsonObject();
				jsonObject.addProperty("id", snippet.id);
				jsonObject.addProperty("name", snippet.name);
				jsonObject.addProperty("seoName", snippet.seoName);
				jsonObject.addProperty("text", snippet.text);
				
				jsonObject.add("tags", context.serialize(snippet.getTags()));
				List<Snippet> relatedSnippets = snippet.getRelatedSnippets();
				List<Long> relatedSnippetIds = new ArrayList<Long>();
				for (Snippet relatedSnippet: relatedSnippets) {
					relatedSnippetIds.add(relatedSnippet.id);
				}
				jsonObject.add("relatedSnippetIds", context.serialize(relatedSnippetIds));
				jsonObject.add("authors", context.serialize(snippet.getAuthors()));
				return jsonObject;
			}
		};
	}

}
