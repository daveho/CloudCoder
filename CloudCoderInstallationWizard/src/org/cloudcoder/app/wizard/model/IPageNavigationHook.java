package org.cloudcoder.app.wizard.model;

/**
 * Hook to be executed on page navigation events.
 */
public interface IPageNavigationHook {
	/**
	 * Called when the next button is clicked.
	 * 
	 * @param document the {@link Document}
	 */
	public void onNext(Document document);
}
