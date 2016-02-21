package org.cloudcoder.app.wizard.model;

public class DocumentFactory {
	public static Document create() {
		Document document = new Document();
		
		// Add pages
		Page welcomePage = new Page("welcome");
		welcomePage.addHelpText("msg");
		document.addPage(welcomePage);
		
		
		return document;
	}
}
