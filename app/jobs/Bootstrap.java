package jobs;

import graph.RelationshipTypes;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.management.relation.RelationType;

import models.Snippet;
import models.SnippetType;
import models.User;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		User user = User.findByUsername("drig");
		if (user == null) {
			Logger.debug("Adding default test user");
			user = new User();
			user.username = "drig";
			user.setPassword("test");
			user.save();
		}
		
		List<String> typeNames = new ArrayList<String>();
		typeNames.add("SQL");
		typeNames.add("Notes");
		typeNames.add("CSV");
		typeNames.add("Text");
		typeNames.add("HTML");
		
		for (String typeName: typeNames) {
			SnippetType type = SnippetType.findByName(typeName);
			if (type == null) {
				type = new SnippetType();
				type.name = typeName;
				type.save();
			}
		}

		List<Snippet> snippets = Snippet.findAll();
		if (snippets.size() <= 0) {
			Logger.debug("Adding default test snippet");
			Snippet snippet = new Snippet();
			snippet.name = "test snippet";
			snippet.text = "this is a test of snippets";
			
			HashMap<String, String> authorAttribs = new HashMap<String, String>();
			authorAttribs.put("date", new Date().toString());
			authorAttribs.put("type", "Original Author");
			
			snippet.addRelationship(RelationshipTypes.AUTHOR, user, authorAttribs);
			snippet.save();
		}
	}

}
