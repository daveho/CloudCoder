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

package org.cloudcoder.app.server.persist;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cloudcoder.app.shared.model.Language;
import org.cloudcoder.app.shared.model.ProblemType;
import org.cloudcoder.app.shared.model.RepoProblem;
import org.cloudcoder.app.shared.model.RepoProblemSearchCriteria;
import org.cloudcoder.app.shared.model.RepoProblemSearchResult;
import org.cloudcoder.app.shared.model.RepoProblemTag;

/**
 * Perform a search for {@link RepoProblem}s matching specified criteria.
 * 
 * @author David Hovemeyer
 */
public class RepoProblemSearch {
	// Build a map of Languages to search clauses matching ProblemTypes
	// using those languages.  The repository supports search by language
	// rather than problem type (since that is probably more useful).
	private static final Map<Language, String> LANGUAGE_TO_PROBLEM_TYPE_SEARCH_CLAUSE_MAP = new HashMap<Language, String>();
	static {
		for (ProblemType problemType : ProblemType.values()) {
			Language language = problemType.getLanguage();
			String clause = LANGUAGE_TO_PROBLEM_TYPE_SEARCH_CLAUSE_MAP.get(language);
			clause = (clause == null ? "" : clause + " or ") +
					"rp." + RepoProblem.PROBLEM_TYPE.getName() + " = " + problemType.ordinal();
			LANGUAGE_TO_PROBLEM_TYPE_SEARCH_CLAUSE_MAP.put(language, clause);
		}
	}
	
	private RepoProblemSearchCriteria searchCriteria;
	private List<RepoProblemSearchResult> searchResultList;
	
	public RepoProblemSearch() {
		this.searchResultList = new ArrayList<RepoProblemSearchResult>();
	}
	
	public void setSearchCriteria(RepoProblemSearchCriteria searchCriteria) {
		this.searchCriteria = searchCriteria;
	}
	
	public void execute(Connection conn, AbstractDatabaseRunnableNoAuthException<?> dbRunnable) throws SQLException {
		// Special case: if neither language nor tags is specified,
		// then the search criteria are invalid and we return no results.
		// The client Javascript should refuse to submit such searches
		// (and inform the user of the error).
		if (searchCriteria.isEmpty()) {
			return;
		}
		
		if (searchCriteria.getTagList().isEmpty()) {
			// Searching by language only
			searchByLanguage(conn, dbRunnable);
		} else {
			// Search by tags and (maybe) language
			StringBuilder sql = new StringBuilder()
				.append("select rp.*, rpt.* ")
				.append("  from ")
				.append(RepoProblem.SCHEMA.getDbTableName())
				.append(" as rp, ")
				.append(RepoProblemTag.SCHEMA.getDbTableName())
				.append(" as rpt ")
				.append(" where rp.id = rpt.repo_problem_id and (");
			
			for (int i = 0; i < searchCriteria.getTagList().size(); i++) {
				if (i > 0) {
					sql.append(" or ");
				}
				sql.append("rpt.name = ?");
			}
			
			sql.append(")");
			
			if (searchCriteria.getLanguage() != null) {
				sql.append(" and (");
				sql.append(LANGUAGE_TO_PROBLEM_TYPE_SEARCH_CLAUSE_MAP.get(searchCriteria.getLanguage()));
				sql.append(")");
			}
			
			sql.append(" order by rp." + RepoProblem.ID.getName() + " asc");
			
			String query = sql.toString();
			System.out.println("query: " + query);
			
			PreparedStatement stmt = dbRunnable.prepareStatement(
					conn,
					query
			);

			int index = 1;
			for (String tag : searchCriteria.getTagList()) {
				stmt.setString(index++, tag);
			}
			
			ResultSet resultSet = dbRunnable.executeQuery(stmt);
			
			RepoProblemSearchResult curSearchResult = null;
			
			while (resultSet.next()) {
				RepoProblem repoProblem = new RepoProblem();
				DBUtil.loadModelObjectFields(repoProblem, RepoProblem.SCHEMA, resultSet);
				
				if (curSearchResult == null || curSearchResult.getRepoProblem().getId() != repoProblem.getId()) {
					curSearchResult = new RepoProblemSearchResult();
					curSearchResult.setRepoProblem(repoProblem);
					searchResultList.add(curSearchResult);
				}
				
				RepoProblemTag repoProblemTag = new RepoProblemTag();
				DBUtil.loadModelObjectFields(repoProblemTag, RepoProblemTag.SCHEMA, resultSet, RepoProblem.SCHEMA.getNumFields() + 1);
				
				curSearchResult.addMatchedTag(repoProblemTag.getName());
				//System.out.println(curSearchResult.getRepoProblem().getTestname() + " => " + repoProblemTag.getName());
			}
		}
	}

	protected void searchByLanguage(Connection conn,
			AbstractDatabaseRunnableNoAuthException<?> dbRunnable)
			throws SQLException {
		PreparedStatement stmt = dbRunnable.prepareStatement(
				conn,
				"select * from " + RepoProblem.SCHEMA.getDbTableName() + " as rp " +
				" where " + LANGUAGE_TO_PROBLEM_TYPE_SEARCH_CLAUSE_MAP.get(searchCriteria.getLanguage()));

		ResultSet resultSet = dbRunnable.executeQuery(stmt);
		while (resultSet.next()) {
			RepoProblem repoProblem = new RepoProblem();
			DBUtil.loadModelObjectFields(repoProblem, RepoProblem.SCHEMA, resultSet);
			
			RepoProblemSearchResult searchResult = new RepoProblemSearchResult();
			searchResult.setRepoProblem(repoProblem);
			
			searchResultList.add(searchResult);
		}
	}
	
	public List<RepoProblemSearchResult> getSearchResultList() {
		return searchResultList;
	}
}
