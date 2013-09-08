package org.cloudcoder.analysis.features.java;

public class Course
{
    private String name="";
    private String title="";
    private String credit="";
    private String foundation="";
    private String faculty="";
    private String period="";
    private String days="";
    private String building="";
    private String room="";
    private String diversity="---";
    private String writing="---";
    private String speaking="---";
    private String prereqs="none";
    private String myClass = "";
    
    @Override
	public String toString() {
        return name+"\t"+title+"\t"+credit+"\t"+
                foundation+"\t"+diversity+"\t"+writing+"\t"+speaking+"\t"+
                prereqs+"\t"+faculty+"\t"+
                period+"\t"+days+"\t"+building+"\t"+room+"\t";
    }
    
    public static String td(String data,String myClass) {
    	if(myClass != ""){
    		return "<td class=\""+myClass+"\">"+data+"</td>";
    	} else {
    		return "<td>"+data+"</td>";
    	}
    }
    
    public String toRow() {
        return "<tr>"+
                td(name,myClass)+
                td(title,myClass)+
                td(credit,myClass)+
                td(foundation,myClass)+td(diversity,myClass)+td(writing,myClass)+td(speaking,myClass)+
                td(prereqs,myClass)+td(faculty,myClass)+
                td(period,myClass)+td(days,myClass)+td(building,myClass)+td(room,myClass)+
                "</tr>";
    }
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getCredit() {
        return credit;
    }
    public void setCredit(String credit) {
        this.credit = credit;
    }
    public String getFoundation() {
        return foundation;
    }
    public void setFoundation(String foundation) {
        this.foundation = foundation;
    }
    public String getFaculty() {
        return faculty;
    }
    public void setFaculty(String faculty) {
        this.faculty = faculty;
    }
    public String getPeriod() {
        return period;
    }
    public void setPeriod(String period) {
        this.period = period;
    }
    public String getDays() {
        return days;
    }
    public void setDays(String days) {
        this.days = days;
    }
    public String getBuilding() {
        return building;
    }
    public void setBuilding(String building) {
        this.building = building;
    }
    public String getRoom() {
        return room;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public String getDiversity() {
        return diversity;
    }
    public void setDiversity(String diversity) {
        this.diversity = diversity;
    }
    public String getWriting() {
        return writing;
    }
    public void setWriting(String writing) {
        this.writing = writing;
    }
    public String getSpeaking() {
        return speaking;
    }
    public void setSpeaking(String speaking) {
        this.speaking = speaking;
    }
    public String getPrereqs() {
        return prereqs;
    }
    public void setPrereqs(String prereqs) {
        this.prereqs = prereqs;
    }
    
    public void setClass(String c){
    	myClass = c;
    }
}
