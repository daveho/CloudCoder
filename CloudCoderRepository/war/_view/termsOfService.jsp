<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Terms of service"></repo:headStuff>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Terms of Service</h1>
			<p>
				These are the current terms of service for the CloudCoder exercise repository.
			</p>
			<ol class="termsOfService">
				<li>
					<b>Only post redistributable content here.</b>
					All exercises uploaded to the repository must be either (a) original
					work by the user uploading the exercise, or (b) licensed under terms
					compatible with the license specified in the exercise.
					Reposting of non-redistributable work (such as exercises from
					non-freely redistributable textbook) is prohibited, and
					at its discretion the CloudCoder team may remove such
					exercises. 
				</li>
				<li>
					<b>Think before uploading.</b>
					By uploading an exercise, you agree to make it available forever under
					your chosen license.  Once an exercise is uploaded, there is no way
					to remove it.  (Only exercises violating the terms of service
					will be removed.)
				</li>
				<li>
					<b>Be nice.</b>
					Use of offensive or hostile language in exercises, comments, and other text
					is prohibited.  The CloudCoder team, at its discretion, may
					remove offensive content.
				</li>
				<li>
					<b>No spam.</b>
					Content uploaded to the repository (such as exercises and comments)
					must be related to programming exercises.  The
					CloudCoder team, at its discretion, may remove irrelevant content. 
				</li>
			</ol>
		</div>
	</body>
</html>