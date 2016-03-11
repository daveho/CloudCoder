package org.cloudcoder.app.wizard.model;

/**
 * Page navigation hook whose execution is conditional on a boolean
 * field being true (checked).
 */
public class ConditionalPageNavigationHook implements IPageNavigationHook {
	private String conditionName;
	private IPageNavigationHook delegate;
	
	public ConditionalPageNavigationHook(String conditionName, IPageNavigationHook delegate) {
		this.conditionName = conditionName;
		this.delegate = delegate;
	}

	@Override
	public void onNext(Document document) {
		if (document.getValue(conditionName).getBoolean()) {
			delegate.onNext(document);
		}
	}
}
