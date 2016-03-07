package org.cloudcoder.app.wizard.model;

/**
 * Control selective enablement of a {@link Page} in
 * the {@link Document}.
 */
public interface ISelectivePageEnablement {
	/**
	 * Determine if the {@link Page} is enabled.
	 * 
	 * @param document the {@link Document}
	 * @return true if the {@link Page} is enabled, false otherwise
	 */
	public boolean isEnabled(Document document);
}
