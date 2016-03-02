package org.cloudcoder.app.wizard.exec;

public interface IInstallSubStep {
	/**
	 * @return one-line description of the sub step
	 */
	public String getDescription();
	
	/**
	 * Execute synchronously.
	 * @throws ExecException
	 */
	public void execute() throws ExecException;
}
