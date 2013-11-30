package org.cloudcoder.app.server.persist.txn;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.cloudcoder.app.server.persist.util.AbstractDatabaseRunnableNoAuthException;
import org.cloudcoder.app.server.persist.util.DBUtil;
import org.cloudcoder.app.shared.model.IModelObject;

public class InsertModelObject<E extends IModelObject<E>> extends AbstractDatabaseRunnableNoAuthException<E> {
	private E obj;
	
	public InsertModelObject(E obj) {
		this.obj = obj;
	}

	@Override
	public String getDescription() {
		return "insert a model object";
	}

	@Override
	public E run(Connection conn) throws SQLException {
		String insert = DBUtil.createInsertStatement(obj.getSchema());
		PreparedStatement stmt = prepareStatement(conn, insert, PreparedStatement.RETURN_GENERATED_KEYS);
		Queries.storeNoIdGeneric(obj, stmt, 0, obj.getSchema());
		return obj;
	}

}
