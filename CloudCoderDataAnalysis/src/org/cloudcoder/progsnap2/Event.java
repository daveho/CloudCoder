// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2016, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2016, David H. Hovemeyer <david.hovemeyer@gmail.com>
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU Affero General Public License as published by
// the Free Software Foundation, either version 3 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Affero General Public License for more details.
//
// You should have received a copy of the GNU Affero General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

package org.cloudcoder.progsnap2;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class Event {
	private static final int EVENT_ID_SPACING = 20;
	
    public Event(EventType eventType, int eventId, int order, int subjectId, String[] toolInstnaces) {
        this.setEventType(eventType);
        // CloudCoder "native" event ids are multiplied by 40 in order to create
        // some space for ProgSnap 2 events that originate from a single
        // CloudCoder event.
        this.setEventId(((long)eventId) * EVENT_ID_SPACING);
        this.setOrder(order);
        this.setSubjectId(subjectId);
        this.setToolInstances(toolInstnaces);
    }

    public String getProgramOutput() {
        return programOutput;
    }

    public void setProgramOutput(String programOutput) {
        this.programOutput = programOutput;
    }

    public String getProgramInput() {
        return programInput;
    }

    public void setProgramInput(String programInput) {
        this.programInput = programInput;
    }

    public EventInitiator getEventInitiator() {
        return eventInitiator;
    }

    public void setEventInitiator(EventInitiator eventInitiator) {
        this.eventInitiator = eventInitiator;
    }

    public ProgramResult getProgramResult() {
        return programResult;
    }

    public void setProgramResult(ProgramResult programResult) {
        this.programResult = programResult;
    }

    public String[] getToolInstances() {
        return toolInstances;
    }

    public void setToolInstances(String[] toolInstances) {
        this.toolInstances = toolInstances;
    }

    public Integer getSubjectId() {
        return subjectId;
    }

    public void setSubjectId(Integer subjectId) {
        this.subjectId = subjectId;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public Long getEventId() {
        return eventId;
    }

    // DHH: Made this private because ProgSnap 2 event ids are derived
    // from CloudCoder ids in a way that is internal to this class,
    // so it's not a good idea to allow them to be set arbitrarily.
    private void setEventId(Long eventId) {
        this.eventId = eventId;
    }
    
    /**
     * "Bump" an event id by specified increment.
     * This can be used to ensure that multiple ProgSnap 2 events that
     * are generated from a single CloudCoder event are given different
     * event ids. 
     * 
     * @param increment the increment to add to the event id
     */
    public void bumpEventId(int increment) {
    	if (increment >= EVENT_ID_SPACING) {
    		throw new IllegalArgumentException("Increment of " + increment +
    				" exceeds event id spacing of " + EVENT_ID_SPACING);
    	}
    	this.eventId += increment;
    }

    public EventType getEventType() {
        return eventType;
    }

    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public Integer getParentEventId() {
        return parentEventId;
    }

    public void setParentEventId(Integer parentEventId) {
        this.parentEventId = parentEventId;
    }

    public Integer getTeamId() {
        return teamId;
    }

    public void setTeamId(Integer teamId) {
        this.teamId = teamId;
    }

    public String getExperimentalCondition() {
        return experimentalCondition;
    }

    public void setExperimentalCondition(String experimentalCondition) {
        this.experimentalCondition = experimentalCondition;
    }

    public Integer getProblemId() {
        return problemId;
    }

    public void setProblemId(Integer problemId) {
        this.problemId = problemId;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getAssignmentId() {
        return assignmentId;
    }

    public void setAssignmentId(Integer assignmentId) {
        this.assignmentId = assignmentId;
    }

    public Integer getTermId() {
        return termId;
    }

    public void setTermId(Integer termId) {
        this.termId = termId;
    }

    public Integer getCourseSectionId() {
        return courseSectionId;
    }

    public void setCourseSectionId(Integer courseSectionId) {
        this.courseSectionId = courseSectionId;
    }

    public Integer getCourseId() {
        return courseId;
    }

    public void setCourseId(Integer courseId) {
        this.courseId = courseId;
    }

    public Integer getSessionId() {
        return sessionId;
    }

    public void setSessionId(Integer sessionId) {
        this.sessionId = sessionId;
    }

    public String getServerTimestamp() {
        if (serverTimestamp == null) {
            return "";
        }

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df.format(new Date(serverTimestamp));
    }

    public String getServerTimezone() {
        if (serverTimestamp == null) {
            return "";
        }

        // TODO: Not sure about this
        return "-0000";
    }

    public void setServerTimestampt(Long serverTimestamp) {
        this.serverTimestamp = serverTimestamp;
    }
    
    public void setCodeStateId(String codeStateId) {
		this.codeStateId = codeStateId;
	}
    
    public String getCodeStateId() {
		return codeStateId;
	}

    public String[] toStrings() {
        return new String[] {
            eventType.getValue(),
            eventId.toString(),
            order.toString(),
            subjectId.toString(),
            String.join(";", toolInstances),
            toStringIfExists(parentEventId),
            toStringIfExists(this.getServerTimestamp()),
            toStringIfExists(this.getServerTimezone()),
            toStringIfExists(sessionId),
            toStringIfExists(courseId),
            toStringIfExists(courseSectionId),
            toStringIfExists(termId),
            toStringIfExists(assignmentId),
            toStringIfExists(resourceId),
            toStringIfExists(problemId),
            toStringIfExists(experimentalCondition),
            toStringIfExists(teamId),
            toStringIfExists(programResult),
            toStringIfExists(eventInitiator),
            toStringIfExists(programInput),
            toStringIfExists(programOutput),
            toStringIfExists(codeStateId),
        };
    }

    public static String[] COLUMN_NAMES = {
        "EventType", "EventID", "Order", "SubjectID", "ToolInstances", "ParentEventID",
        "ServerTimestamp", "ServerTimezone", "SessionID", "CourseID", "CourseSectionID",
        "TermID", "AssignmentID", "ResourceID", "ProblemID", "ExperimentalCondition", "TeamID",
        "ProgramResult", "EventInitiator", "ProgramInput", "ProgramOutput", "CodeStateId"
    };

    private EventType eventType;
    private Long eventId;
    private Integer order;
    private Integer subjectId;
    private String[] toolInstances;
    private Integer parentEventId;
    private Long serverTimestamp;
    private Integer sessionId;
    private Integer courseId;
    private Integer courseSectionId;
    private Integer termId;
    private Integer assignmentId;
    private Integer resourceId;
    private Integer problemId;
    private String experimentalCondition;
    private Integer teamId;
    private ProgramResult programResult;
    private EventInitiator eventInitiator;
    private String programInput;
    private String programOutput;
    private String codeStateId;

    private String toStringIfExists(Object obj) {
        if (obj == null) {
            return "";
        }
        return obj.toString();
    }
}