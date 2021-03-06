package models;

import graph.GraphModel;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.PostUpdate;
import javax.persistence.PrePersist;
import javax.persistence.Transient;

import org.apache.commons.codec.binary.Hex;

import play.Logger;
import play.data.validation.Required;
import play.db.jpa.Model;

@Entity
public class User extends GraphModel {

	public String username;
	
	public String hashedPassword;
	
	public String salt;
	
	@Transient
	private List<Snippet> snippets = null;
	
	@Transient
	private static char[] characters = {
		'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z',
		'A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z',
		'1','2','3','4','5','6','7','8','9','!','@','#','$','%','^','&','*','(',')'
	};
	
	public void setPassword(String password) {
		SecureRandom random = new SecureRandom();
		
		salt = "";
		for (int saltChar = 0; saltChar < 4; saltChar++) {
			salt += User.characters[Math.abs(random.nextInt()%characters.length)];
		}
		
		hashedPassword = User.hashPassword(username, salt, password);
	}
	
	private static String hashPassword (String username, String salt, String password) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-1");
			digest.update(username.getBytes());
			digest.update(":".getBytes());
			digest.update(salt.getBytes());
			digest.update(":".getBytes());
			digest.update(password.getBytes());
			
			byte[] hashInBytes = digest.digest();
			
			return Hex.encodeHexString(hashInBytes);
		} catch (Exception anyExc) {
			Logger.warn(anyExc, "Could not hash password");
		}
		return null;
	}
	
	public List<Snippet> getSnippets() {
		//if (snippets!=null) return snippets;
		List<Model> relations = getRelations(getId(), getClass(), Snippet.class);
		
		snippets = new ArrayList<Snippet>();
		
		for (Model relation: relations) {
			if (relation instanceof Snippet) {
				snippets.add((Snippet)relation);
			}
		}
		
		return snippets;
	}
	
	@PostUpdate
	public void postUpdate() {
		snippets = null;
	}
	
	@PrePersist
	public void prePersist() {
		if (hashedPassword == null) hashedPassword = "";
	}
	
	public static User findByUsername(String username) {
		List<User> users = User.find("byUsername", username).fetch();
		if (users == null) {
			Logger.debug ("Users is null");
			return null;
		}
		Logger.debug ("Found %d users", users.size());
		if (users.size()<=0) return null;
		
		return users.get(0);
	}
	
	public static User findByLogin(String username, String password) {
		List<User> users = User.find("byUsername", username).fetch();
		if (users == null) {
			Logger.debug ("Users is null");
			return null;
		}
		Logger.debug ("Found %d users", users.size());
		if (users.size()<=0) return null;
		
		for (User user: users) {
			String hashedPassword = User.hashPassword(username, user.salt, password);
			if ((hashedPassword!=null) && (user.hashedPassword!=null) && (user.hashedPassword.equals(hashedPassword))) {
				return user;
			}
		}
		return null;
	}
}
