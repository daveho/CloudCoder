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
	
	/** Width of a field that is a single component (such as a label). */
	public static final int SINGLE_COMPONENT_FIELD_WIDTH = 780;
	
	/** Full height for a help text component. */
	public static final int FULL_HELP_TEXT_HEIGHT = 250;
	
	/** Progress bar height. */
	public static final int PROGRESS_BAR_HEIGHT = (FIELD_HEIGHT * 200) / 300;
}
