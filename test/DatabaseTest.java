import graph.RelationshipTypes;

import java.util.List;

import models.Snippet;
import models.User;

import org.junit.Test;

import play.Logger;
import play.test.UnitTest;


public class DatabaseTest extends UnitTest {

	@Test
	public void saveAndLoadUser() {
		User user = null;
		
		user = User.findByLogin("drig", "vdvv");
		assertNull(user);
		
		user = User.findByLogin("d", "test");
		assertNull(user);
		
		user = User.findByLogin("drig", "test");
		assertNotNull(user);
		
		user = new User();
		user.username = "drig2";
		user.setPassword("test");
		user.save();

		
		User drig2 = User.findByUsername("drig2");
		assertNotNull("Didn't find users", drig2);
		
		user = User.findByUsername("drig2");
		user.delete();
		
		user = User.findByUsername("drig2");
		assertNull("Deletion of user2 didn't work", user);
	}
	
	@Test
	public void snippetTest() {
		try {
			User user = User.findByUsername("drig");
			assertTrue("user did not get snippet relationship", user.getSnippets().size()>0);
			
			List<Snippet> snippets = Snippet.findByName("test snippet");
			assertTrue("did not find snippets", snippets!=null && snippets.size()>0);
			for (Snippet snippet: snippets) {
				assertTrue("snippet did not get author relationship", snippet.getAuthors().size()<=0);
			}
		} catch (Exception anyExc) {
			anyExc.printStackTrace();
			assertTrue(anyExc.getMessage(), false);
		}
		
	}
}
