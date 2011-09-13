package org.cloudcoder.app.shared.model;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;

import com.google.gwt.user.client.rpc.IsSerializable;

//@Entity
//@Table(name="course_registrations")
public class CourseRegistration implements IsSerializable {
//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	@Column(name="id")
	private int id;
	
//	@Column(name="course_id")
	private int courseId;
	
//	@Column(name="user_id")
	private int userId;
	
//	@Column(name="registration_type")
	private int registrationType;
	
	public CourseRegistration() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setCourseId(int courseId) {
		this.courseId = courseId;
	}
	
	public int getCourseId() {
		return courseId;
	}
	
	public void setUserId(int userId) {
		this.userId = userId;
	}
	
	public int getUserId() {
		return userId;
	}
	
	public int getRegistrationType() {
		return registrationType;
	}
	
	public void setRegistrationType(int registrationType) {
		this.registrationType = registrationType;
	}
	
}
