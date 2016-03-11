package org.cloudcoder.app.wizard.model;

/**
 * An {@link ISelectivePageEnablement} for pages which are always
 * disabled.  This is useful for pages containing internal
 * variables.
 */
public class DisablePage implements ISelectivePageEnablement {
	public static final DisablePage INSTANCE = new DisablePage();
	
	private DisablePage() {
	}
	
	@Override
	public boolean isEnabled(Document document) {
		return false;
	}
}
