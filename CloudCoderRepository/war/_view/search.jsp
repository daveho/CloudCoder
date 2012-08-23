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
				function(obj) { return problemTypeOrdinalToLanguage[obj.repo_problem.problem_type]; },
				function(obj) { return obj.repo_problem.testname; },
				function(obj) { return obj.repo_problem.brief_description; },
				function(obj) { return obj.repo_problem.author_name; },
				function(obj) { return obj.matched_tag_list.join(' '); }
			];

			// Variable to store the DataTable object so it can be used by callbacks			
			var dataTable;
			
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
						problemType: problemTypeOrdinal,
						selectedTags: $("#selectedTags").val()
					},
					success: function(data, textStatus, jqXHR) {
						// Result will be an array of JSON-encoded RepoProblemSearchResults
						//alert("Search returned " + data.length + " exercises");

						// Convert exercises to row tuples						
						var j, i;
						var rowData = [];
						for (j = 0; j < data.length; j++) {
							var tuple = [];
							for (i = 0; i < repoProblemConvertFields.length; i++) {
								tuple.push(repoProblemConvertFields[i](data[j]));
							}
							rowData.push(tuple);
						}
						
						$("#searchResultsTable").dataTable().fnClearTable();
						$("#searchResultsTable").dataTable().fnAddData(rowData);
						
						// Add a callback to all rows to handle row selection
						$('#searchResultsTable tbody tr').click(function(e) {
							if ($(this).hasClass('row_selected')) {
					            $(this).removeClass('row_selected');
					        } else {
					            dataTable.$('tr.row_selected').removeClass('row_selected');
					            $(this).addClass('row_selected');
					        }
						});
					},
					error: function(jqXHR, textStatus, errorThrown) {
						$("#errorElt").text(errorThrown);
					}
				});
			}
		
			$(document).ready(function() {
				$("#submitButton").click(onSubmit);
				
				// Enable DataTable on the search results table.
				dataTable = $("#searchResultsTable").dataTable();
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
							<th width="10%">Language</th>
							<th width="15%">Name</th>
							<th width="50%">Description</th>
							<th width="25%">Author</th>
							<th width="25%">Matched Tags</th>
						</tr>
				</thead>
				<tbody>
				</tbody>
			</table>
		</div>
	</body>
</html>