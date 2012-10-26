package org.cloudcoder.importer;

import java.io.FileInputStream;

public class ToMySQLString {
	public static void main(String[] args) throws Exception {
		FileInputStream in = new FileInputStream(args[0]);
		StringBuilder buf = new StringBuilder();
		while (true) {
			int c = in.read();
			if (c < 0) {
				break;
			}
			if (c == '\n') {
				buf.append("\\n");
			} else if (c == '\t') {
				buf.append("\\t");
			} else {
				buf.append((char)c);
			}
		}
		System.out.println("'" + buf.toString() + "'");
	}
}
