<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Search the exercise repository</h1>
			<p>Select problem type and tags</p>
			<p> Problem type:
			<select id="selectedProblemType">
				<option>Any problem type</option>
				<c:forEach var="problemType" items="${problemTypes}">
					<option>${problemType}</option>
				</c:forEach>
			</select>
			Tags:
			<input id="selectedTags" type="text" size="60" />
			</p>
			
			<button id="submitButton">Search!</button>
		</div>
	</body>
</html>