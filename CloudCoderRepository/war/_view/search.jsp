<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
		<link rel="stylesheet" type="text/css" href="${pageContext.servletContext.contextPath}/css/jquery.dataTables.css"/>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/jquery.dataTables.min.js"></script>
		<script type="text/javascript">
			var problemTypeOrdinalToLanguage = ${problemTypeOrdinalToLanguage};
			
			// How to format a raw RepoProblem JSON object as a tuple to be displayed in the
			// search results DataTable.
			var repoProblemConvertFields = [
				function(obj) { return problemTypeOrdinalToLanguage[obj.problem_type]; },
				function(obj) { return obj.testname; },
				function(obj) { return obj.brief_description; },
				function(obj) { return obj.author_name; },
				function(obj) { return 'foo bar'; } // tags: not implemented yet
			];
			
			// Initiate an AJAX request to retrieve search results.
			function onSubmit() {
				var problemTypeOrdinal = $("#selectedProblemType option:selected").attr('value');
				//alert("Problem type is " + problemType);
				
				var queryUri = "${pageContext.servletContext.contextPath}/search";
				$.ajax({
					url: queryUri,
					dataType: "json",
					type: "post",
					data: {
						problemType: problemTypeOrdinal 
					},
					success: function(data, textStatus, jqXHR) {
						// Result will be an array of JSON-encoded exercises
						//alert("Search returned " + data.length + " exercises");
						
						var data = $.map(data, function(obj, index) {
							var tuple = [];
							for (var i = 0; i < repoProblemConvertFields.length; i++) {
								tuple.push(repoProblemConvertFields[i](obj));
							}
							return tuple;
						});
						
						$("#searchResultsTable").dataTable().fnClearTable();
						$("#searchResultsTable").dataTable().fnAddData(data);
					},
					error: function(jqXHR, textStatus, errorThrown) {
						$("#errorElt").text(errorThrown);
					}
				});
			}
		
			$(document).ready(function() {
				$("#submitButton").click(onSubmit);
				
				// Enable DataTables on the search results table.
				$("#searchResultsTable").dataTable();
			});
		</script>
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