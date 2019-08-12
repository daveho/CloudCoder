package org.cloudcoder.progsnap2;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Header {
	private Map<String, Integer> posMap;
	
	public Header() {
		posMap = new HashMap<String, Integer>();
	}

	public void init(String[] headerRow) {
		for (int i = 0; i < headerRow.length; i++) {
			posMap.put(headerRow[i], i);
		}
	}

	public Header extend(String... columns) {
		Header extended = new Header();
		extended.posMap.putAll(this.posMap);
		int n = posMap.size();
		for (int i = 0; i < columns.length; i++) {
			if (posMap.containsKey(columns[i])) {
				throw new IllegalStateException("Extending header with existing column " + columns[i]);
			}
			extended.posMap.put(columns[i], n++);
		}
		return extended;
	}
	
	public String[] createRow() {
		String[] row = new String[posMap.size()];
		Arrays.fill(row, "");
		return row;
	}
	
	public void copyValues(String[] srcRow, String[] destRow) {
		if (srcRow.length > destRow.length) {
			throw new IllegalArgumentException("Source row has more columns than destination row");
		}
		System.arraycopy(srcRow, 0, destRow, 0, srcRow.length);
	}
	
	public void putValue(String colName, String value, String[] row) {
		Integer idx = posMap.get(colName);
		if (idx == null) {
			throw new IllegalArgumentException("Unknown column name " + colName);
		}
		if (idx >= row.length) {
			throw new IllegalArgumentException("Row too small for column " + colName);
		}
		row[idx] = value;
	}

	public String getValue(String colName, String[] row) {
		Integer idx = posMap.get(colName);
		if (idx == null) {
			throw new IllegalArgumentException("Unknown column name " + colName);
		}
		if (idx >= row.length) {
			throw new IllegalArgumentException("Row doesn't have value for " + colName + " column");
		}
		return row[idx];
	}
	
	public RowView asRowView(final String[] row) {
		return new RowView() {
			@Override
			public String get(int index) {
				return row[index];
			}

			@Override
			public String get(String colName) {
				return getValue(colName, row);
			}
			
			@Override
			public void copyFrom(RowView rowView, String colName) {
				put(colName, rowView.get(colName));
			}
			
			@Override
			public void put(String colName, String value) {
				putValue(colName, value, row);
			}
		};
	}

	public String[] getHeaderRow() {
		String[] headerRow = createRow();
		for (Map.Entry<String, Integer> entry : posMap.entrySet()) {
			headerRow[entry.getValue()] = entry.getKey();
		}
		return headerRow;
	}
}
