package org.cloudcoder.app.shared.model;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.GeneratedValue;
//import javax.persistence.GenerationType;
//import javax.persistence.Id;
//import javax.persistence.Table;
//import javax.persistence.Transient;

import com.google.gwt.user.client.rpc.IsSerializable;

//@Entity
//@Table(name="courses")
public class Course implements IsSerializable {
	/**
	 * Make sure this is kept up to date with the courses table in
	 * the database.
	 */
	public static final int NUM_FIELDS = 6;

//	@Id
//	@GeneratedValue(strategy=GenerationType.AUTO)
//	@Column(name="id")
	private int id;
	
//	@Column(name="name")
	private String name;
	
//	@Column(name="title")
	private String title;
	
//	@Column(name="url")
	private String url;

//	@Column(name="term_id")
	private int termId;
	
//	@Column(name="year")
	private int year;
	
//	@Transient
	private Term term;
	
	public Course() {
		
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getTitle() {
		return title;
	}
	
	public String getUrl() {
		return url;
	}
	
	public void setUrl(String url) {
		this.url = url;
	}
	
	public void setTermId(int termId) {
		this.termId = termId;
	}
	
	public int getTermId() {
		return termId;
	}
	
	public void setYear(int year) {
		this.year = year;
	}
	
	public int getYear() {
		return year;
	}
	
	public void setTerm(Term term) {
		this.term = term;
	}
	
	public Term getTerm() {
		return term;
	}
	
	@Override
	public String toString() {
		return name + " - " + title;
	}

	public TermAndYear getTermAndYear() {
		return new TermAndYear(term, year);
	}
}
