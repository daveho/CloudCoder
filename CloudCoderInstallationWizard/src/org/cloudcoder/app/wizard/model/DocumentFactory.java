package org.cloudcoder.app.wizard.model;

public class DocumentFactory {
	public static Document create() {
		Document document = new Document();
		
		// Add pages
		Page welcomePage = new Page("welcome");
		welcomePage.addHelpText("msg", "Welcome message");
		document.addPage(welcomePage);
		
		Page awsCredentialsPage = new Page("aws");
		awsCredentialsPage.addHelpText("msg", "Message");
		awsCredentialsPage.add(new StringValue("accessKeyID", "Access key ID"));
		awsCredentialsPage.add(new StringValue("secretAccessKey", "Secret access key"));
		document.addPage(awsCredentialsPage);
		
		return document;
	}
}
