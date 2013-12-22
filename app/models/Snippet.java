package models;

import graph.GraphModel;
import graph.RelationshipTypes;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PostLoad;
import javax.persistence.PostUpdate;
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

	@PostUpdate
	public void postUpdate() {
		authors = null;
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
