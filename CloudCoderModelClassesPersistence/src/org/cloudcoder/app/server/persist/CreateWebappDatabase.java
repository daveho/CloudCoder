package org.cloudcoder.app.server.persist;

import org.cloudcoder.app.shared.model.Change;

public class CreateWebappDatabase {
	public static void main(String[] args) {
		String sql = DBUtil.getCreateTableStatement(Change.SCHEMA, "cc_changes");
		System.out.println(sql);
	}
}
