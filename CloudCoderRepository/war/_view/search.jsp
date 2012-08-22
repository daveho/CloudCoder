<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
		<link rel="stylesheet" type="text/css" href="${pageContext.servletContext.contextPath}/css/jquery.dataTables.css"/>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/search.js"></script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Search the exercise repository</h1>
			<p>Select problem type and tags</p>
			<p> Problem type:
			<select id="selectedProblemType">
				<option value="-1">Any problem type</option>
				<c:forEach var="problemType" items="${problemTypes}">
					<option value="${problemTypeOrdinals[problemType]}">${problemType}</option>
				</c:forEach>
			</select>
			Tags:
			<input id="selectedTags" type="text" size="60" />
			</p>
			
			<button id="submitButton">Search!</button> <span id="errorElt" class="error"></span>
			
			<table id="searchResultsTable">
				<thead>
						<tr>
							<th>Language</th>
							<th>Name</th>
							<th>Description</th>
							<th>Author</th>
							<th>Tags</th>
						</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
	</body>
</html>