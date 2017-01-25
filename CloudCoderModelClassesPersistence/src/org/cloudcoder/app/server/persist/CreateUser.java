// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2017, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2017, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.util.Properties;
import java.util.Scanner;

import org.cloudcoder.app.server.persist.util.ConfigurationUtil;
import org.cloudcoder.app.server.persist.util.DBUtil;

public class CreateUser {
	public static void main(String[] args) throws Exception {
		Scanner keyboard = new Scanner(System.in);
		
		Class.forName(JDBCDatabase.JDBC_DRIVER_CLASS);
		Properties config = DBUtil.getConfigProperties();
		Connection conn = DBUtil.connectToDatabase(config, "cloudcoder.db");
		
		System.out.println("Create a new CloudCoder user account");
		
		String ccUserName = ConfigurationUtil.ask(keyboard, "Username? ");
		String ccPassword = ConfigurationUtil.ask(keyboard, "Password? ");
		String ccFirstname = ConfigurationUtil.ask(keyboard, "First name? " );
		String ccLastname= ConfigurationUtil.ask(keyboard, "Last name? ");
		String ccEmail= ConfigurationUtil.ask(keyboard, "Email address? ");
		String ccWebsite= ConfigurationUtil.ask(keyboard, "Website URL? ");
		
		ConfigurationUtil.createOrUpdateUser(conn, ccUserName, ccFirstname, ccLastname, ccEmail, ccPassword, ccWebsite);

		System.out.println("Success!");
	}
}
