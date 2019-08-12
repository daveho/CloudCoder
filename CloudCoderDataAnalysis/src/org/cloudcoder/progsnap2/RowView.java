package org.cloudcoder.progsnap2;

public interface RowView {
	public String get(int index);
	public String get(String colName);
	public void copyFrom(RowView rowView, String colName);
	public void put(String colName, String value);
}
