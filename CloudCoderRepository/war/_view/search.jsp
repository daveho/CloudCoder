<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
		<script type="text/javascript">
			var onSubmit = function() {
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
						alert("Search returned " + data.length + " exercises");
						
						// TODO: display the exercises by adding them to the DOM tree
					},
					error: function(jqXHR, textStatus, errorThrown) {
						$("#errorElt").text(errorThrown);
					}
				});
			};
		
			$(document).ready(function() {
				$("#submitButton").click(onSubmit);
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
		</div>
	</body>
</html>