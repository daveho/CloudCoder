package org.cloudcoder.app.wizard.ui;

import java.awt.Color;

public interface UIConstants {
	/** Background color for invalid UI fields. */
	public static final Color INVALID_FIELD_BG = new Color(255, 192, 192);
	
	/** Height in pixels of components in a "standard" UI field. */
	public static final int FIELD_COMPONENT_HEIGHT = 32;
	
	/** Extra padding for the height of UI fields. */
	public static final int FIELD_PADDING = 8;
	
	/** Full height in pixels of a "standard" UI field, including padding. */
	public static final int FIELD_HEIGHT = FIELD_COMPONENT_HEIGHT + FIELD_PADDING;
	
	/** Label width. */
	public static final int LABEL_WIDTH = 240;
	
	/** Width in pixels of the editable component in a "standard" UI field. */
	public static final int FIELD_COMPONENT_WIDTH = 420;
	
}
