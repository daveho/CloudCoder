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

public class Event {
    public Event(EventType eventType, long eventId, long order, long subjectId, String[] toolInstnaces) {
        this.eventType = eventType;
        this.eventId = eventId;
        this.order = order;
        this.subjectId = subjectId;
        this.toolInstances = toolInstnaces;
        // TODO: CodeStateId
    }

    public EventType getEventType() {
        return eventType;
    }

    public String[] getToolInstances() {
        return toolInstances;
    }

    public long getSubjectId() {
        return subjectId;
    }

    public long getOrder() {
        return order;
    }

    public long getEventId() {
        return eventId;
    }

    public String[] toStrings() {
        return new String[] {
            getEventType().getValue(),
            String.valueOf(getEventId()),
            String.valueOf(getOrder()),
            String.valueOf(getSubjectId()),
            String.join(",", getToolInstances())
        };
    }

    public static String[] COLUMN_NAMES = { "EventType", "EventID", "Order", "SubjectID", "ToolInstances" };

    private EventType eventType;
    private long eventId;
    private long order;
    private long subjectId;
    private String[] toolInstances;
}