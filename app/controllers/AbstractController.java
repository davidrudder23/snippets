package controllers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import models.Snippet;
import models.Tag;
import models.User;
import play.Logger;
import play.mvc.Before;
import play.mvc.Controller;
import utils.ValueComparator;

public abstract class AbstractController extends Controller {

	@Before
	public static void globalParams() {
		User user = Security.getLoggedInUser();
		renderArgs.put("user", user);
    	
		List<Snippet> snippets = user.getSnippets();
		renderArgs.put("snippets", snippets);

		HashMap<String, Integer> tagNamesAndCounts = new HashMap<String, Integer>();
		for (Snippet snippet: snippets) {
			List<Tag> snippetTags = snippet.getTags();
			for (Tag snippetTag: snippetTags) {
				String tagName = snippetTag.name;
				Integer tagCount = tagNamesAndCounts.get(tagName);
				if (tagCount == null) tagCount = new Integer(0);
				tagCount++;
				tagNamesAndCounts.put(tagName, tagCount);
			} 
		}
		
		Logger.debug ("%s", tagNamesAndCounts);
	    ValueComparator<String, Integer> comparator = new ValueComparator<String, Integer> (tagNamesAndCounts);
	    TreeMap<String, Integer> sortedMap = new TreeMap<String, Integer> (comparator);
	    
	    for (String key: tagNamesAndCounts.keySet()) {
	    	Logger.debug ("Adding key %s with value %s", key, tagNamesAndCounts.get(key));
	    	sortedMap.put(key, tagNamesAndCounts.get(key));
	    }
	    //sortedMap.putAll(tagNamesAndCounts);
		Logger.debug ("%s", sortedMap);
	    List<String> tagNames = new ArrayList<String> (sortedMap.keySet());
	    
	    Logger.debug ("Tag names:");
	    for (String tagName: tagNames) {
	    	Logger.debug("  %s", tagName);
	    }

		renderArgs.put("tagNames", tagNames);
	}
}

