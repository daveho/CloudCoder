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

public class EventType {
    public static EventType SessionStart = new EventType("Session.Start");
    public static EventType SessionEnd = new EventType("Session.End");
    public static EventType ProjectOpen = new EventType("Project.Open");
    public static EventType ProjectClose = new EventType("Project.Close");
    public static EventType FileCreate = new EventType("File.Create");
    public static EventType FileDelete = new EventType("File.Delete");
    public static EventType FileOpen = new EventType("File.Open");
    public static EventType FileClose = new EventType("File.Close");
    public static EventType FileRename = new EventType("File.Rename");
    public static EventType FileEdit = new EventType("File.Edit");
    public static EventType FileFocus = new EventType("File.Focus");
    public static EventType Compile = new EventType("Compile");
    public static EventType CompileError = new EventType("Compile.Error");
    public static EventType CompileWarning = new EventType("Compile.Warning");
    public static EventType Submit = new EventType("Submit");
    public static EventType RunProgram = new EventType("Run.Program");
    public static EventType RunTest = new EventType("Run.Test");
    public static EventType DebugProgram = new EventType("Debug.Program");
    public static EventType DebugTest = new EventType("Debug.Test");
    public static EventType ResourceView = new EventType("Resource.View");

    private EventType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    private String value;
}