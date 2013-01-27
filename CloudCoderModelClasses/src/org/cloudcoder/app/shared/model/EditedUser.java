package org.cloudcoder.app.shared.model;

/**
 * Data about a {@link User} that is being edited in the context
 * of a {@link Course}.
 * 
 * @author David Hovemeyer
 */
public class EditedUser {
	private User user;
	private String currentPassword;
	private String password;
	private int section;
	private CourseRegistrationType registrationType;
	
	public EditedUser() {
		
	}
	
	public void setUser(User user) {
		this.user = user;
	}
	
	public User getUser() {
		return user;
	}
	
	public void setCurrentPassword(String currentPassword) {
		this.currentPassword = currentPassword;
	}
	
	public String getCurrentPassword() {
		return currentPassword;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getPassword() {
		return password;
	}
	
	public void setSection(int section) {
		this.section = section;
	}
	
	public int getSection() {
		return section;
	}
	
	public void setRegistrationType(CourseRegistrationType registrationType) {
		this.registrationType = registrationType;
	}
	
	public CourseRegistrationType getRegistrationType() {
		return registrationType;
	}
}
