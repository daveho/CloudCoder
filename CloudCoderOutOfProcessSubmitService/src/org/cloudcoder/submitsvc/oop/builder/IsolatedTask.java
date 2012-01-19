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

package org.cloudcoder.submitsvc.oop.builder;

/**
 * An isolated Task that can be executed in a separately thread, where the thread
 * might be unsafely killed with the stop() method in Thread.
 * 
 * <b>Tasks executed by this functor must not have access to any
 * shared state so that it's impossible for these tasks to leave
 * anything in an inconsistent state</b>
 * 
 * In other words, Tasks are designed to be like a sub-process that produces
 * an output of type T.  If the Task is stopped before completing, then the
 * result type simply won't be there, but no internal state should be left
 * in an inconsistent state.
 * 
 * @author jspacco
 *
 * @param <T>
 */
public interface IsolatedTask<T> {
    public T execute() throws Throwable;
}