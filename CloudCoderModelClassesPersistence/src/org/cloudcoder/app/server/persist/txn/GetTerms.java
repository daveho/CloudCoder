// CloudCoder - a web-based pedagogical programming environment
// Copyright (C) 2011-2015, Jaime Spacco <jspacco@knox.edu>
// Copyright (C) 2011-2015, David H. Hovemeyer <david.hovemeyer@gmail.com>
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

package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.Term;

/**
 * Query to get all of the defined {@link Term}s.
 * 
 * @author David Hovemeyer
 */
public class GetTerms extends AbstractDatabaseRunnableNoAuthException<Term[]> {

	@Override
	public String getDescription() {
		return " get terms";
	}

	@Override
	public Term[] run(Connection conn) throws SQLException {
		PreparedStatement stmt = prepareStatement(
				conn,
				"select * from cc_terms order by seq");
		ResultSet resultSet = executeQuery(stmt);
		
		ArrayList<Term> result = new ArrayList<Term>();
		while (resultSet.next()) {
			Term term = new Term();
			DBUtil.loadModelObjectFields(term, Term.SCHEMA, resultSet);
			result.add(term);
		}
		
		return result.toArray(new Term[result.size()]);
	}

}
