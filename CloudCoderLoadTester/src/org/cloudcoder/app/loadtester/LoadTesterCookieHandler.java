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

import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * A global default {@link CookieHandler} that delegates cookie
 * handling to thread-local cookie managers.
 * 
 * @author David Hovemeyer
 */
public class LoadTesterCookieHandler extends CookieHandler {
	private ThreadLocal<CookieManager> threadLocalCookieManager = new ThreadLocal<CookieManager>();

	private static final LoadTesterCookieHandler theInstance = new LoadTesterCookieHandler();
	
	/**
	 * Get the singleton instance.
	 * This should be installed as the default cookie handlers by
	 * calling {@link CookieHandler#setDefault(CookieHandler)}.
	 * 
	 * @return the singleton instance
	 */
	public static CookieHandler getInstance() {
		return theInstance;
	}
	
	private CookieManager getDelegate() {
		CookieManager delegate = threadLocalCookieManager.get();
		if (delegate == null) {
			//System.out.println("New delegate for thread " + Thread.currentThread().getId());
			delegate = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
			threadLocalCookieManager.set(delegate);
		}
		return delegate;
	}
	
	@Override
	public Map<String, List<String>> get(URI uri, Map<String, List<String>> requestHeaders) throws IOException {
		Map<String, List<String>> map = getDelegate().get(uri, requestHeaders);
		//System.out.println("Get cookies for thread " + Thread.currentThread().getId() + ": " + map.toString());
		return map;
	}

	@Override
	public void put(URI uri, Map<String, List<String>> responseHeaders) throws IOException {
		getDelegate().put(uri, responseHeaders);
		//System.out.println("Putting cookies for thread " + Thread.currentThread().getId() + ": " + responseHeaders);
	}

}
