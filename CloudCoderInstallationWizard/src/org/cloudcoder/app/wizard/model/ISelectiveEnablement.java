package org.cloudcoder.app.wizard.model;

/**
 * Control the selective enablement of an {@link IValue} on
 * a {@link Page}.
 */
public interface ISelectiveEnablement extends Cloneable {
	/**
	 * Check whether an {@link IValue} should
	 * be enabled/disabled selectively.
	 * 
	 * @param page the {@link Page}; this will typically be a
	 *        copy of the actual page with updated values
	 *        based on UI field contents
	 * @return true if the named value should be enabled,
	 *         false otherwise
	 */
	public boolean isEnabled(Page page);
}
