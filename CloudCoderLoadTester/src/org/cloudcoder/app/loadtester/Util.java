// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2013, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2013, David H. Hovemeyer <david.hovemeyer@gmail.com>
// Copyright (C) 2013, York College of Pennsylvania
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

package org.cloudcoder.app.loadtester;

import java.util.concurrent.Callable;

public class Util {
	/**
	 * Attempt to perform RPC, retrying up to 3 times if an exception occurs.
	 * 
	 * @param f the RPC operation to perform
	 * @return the result of the RPC operation
	 * @throws Exception
	 */
	public static<E> E doRPC(Callable<E> f) throws Exception {
		int retryCount = 0;
		Exception e = null;
		while (retryCount < 3) {
			try {
				return f.call();
			} catch (Exception ex) {
				e = ex;
				retryCount++;
				LoadTesterActivityReporter.getInstance().reportRecoverableException(ex);
			}
		}
		throw e;
	}

}
