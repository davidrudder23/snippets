package jobs;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import models.Snippet;
import models.User;
import play.Logger;
import play.jobs.Job;
import play.jobs.OnApplicationStart;

@OnApplicationStart
public class Bootstrap extends Job {

	public void doJob() {
		List<User> users = User.findAll();
		for (User user : users) {
			user.delete();
		}
		List<Snippet> snippets = Snippet.findAll();
		for (Snippet snippet : snippets) {
			snippet.delete();
		}

		User user = User.findByUsername("drig");
		if (user == null) {
			Logger.debug("Adding default test user");
			user = new User();
			user.username = "drig";
			user.setPassword("test");
			user.save();
		}

		if (snippets.size() <= 0) {
			Logger.debug("Adding default test snippet");
			Snippet snippet = new Snippet();
			snippet.name = "test snippet";
			snippet.text = "this is a test of snippets";
			
			HashMap<String, String> authorAttribs = new HashMap<String, String>();
			authorAttribs.put("date", new Date().toString());
			authorAttribs.put("type", "Original Author");
			
			snippet.addRelationship(user, authorAttribs);
			snippet.save();
		}
	}

}
