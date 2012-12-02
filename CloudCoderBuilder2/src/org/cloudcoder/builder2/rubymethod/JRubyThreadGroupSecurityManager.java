// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2012, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2012, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.builder2.rubymethod;

import java.security.Permission;

import org.cloudcoder.builder2.javasandbox.ThreadGroupSecurityManager;

/**
 * Custom {@link ThreadGroupSecurityManager} implementation for testing
 * student Ruby code using JRuby.  Grants some permissions that
 * are not granted to Java or Jython submissions.  Unfortunately,
 * this probably reduces the safety of Ruby submissions, but what can you do?
 * 
 * @author David Hovemeyer
 */
public class JRubyThreadGroupSecurityManager extends ThreadGroupSecurityManager {

	public JRubyThreadGroupSecurityManager(ThreadGroup threadGroup) {
		super(threadGroup);
	}

	@Override
	protected void check(Permission perm) {
        if (perm.getName().endsWith("/jruby-complete-1.7.0.jar")) {
        	// JRuby requires access to its jarfile to load the built-in Ruby classes
        	return;
        } else if (perm.getName().equals("suppressAccessChecks")) {
        	// Sadly, JRuby requires this.
        	// This is probably dangerous to allow.
        	return;
        } else {
        	// Delegate to base class
        	super.check(perm);
        }
	}
}
