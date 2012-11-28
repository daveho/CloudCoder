package org.cloudcoder.builder2.model;

/**
 * A bytecode artifact such as a Java classfile,
 * where the binary representation is stored in memory.
 * 
 * @author David Hovemeyer
 */
public class Bytecode {
	private String className;
	private byte[] code;
	
	public Bytecode(String className, byte[] code) {
		this.className = className;
		this.code = code;
	}
	
	public String getClassName() {
		return className;
	}
	
	public byte[] getCode() {
		return code;
	}
}
