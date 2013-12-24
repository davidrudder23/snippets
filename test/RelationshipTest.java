import java.util.List;

import org.junit.Test;

import models.Relationship;
import models.Snippet;
import models.User;
import play.test.UnitTest;


public class RelationshipTest extends UnitTest {

	@Test
	public void testGetRelationships() {
		User user = User.find("byUsername", "drig").first();
		
		assertNotNull(user);
		
		List<Relationship> relationships = user.getRelationships(Snippet.class);
		assertNotNull(relationships);
		assertTrue(relationships.size()>0);
	}
}
