<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="View exercise"></repo:headStuff>
		<script type="text/javascript">
		function getTags() {
			$.ajax(
				"${pageContext.servletContext.contextPath}/getTags",
				{
					data: { repoProblemId: ${RepoProblem.id} },
					dataType: "json",
					error: function(jqXHR, textStatus, errorThrown) {
						$("#repoProblemTags").text("Could not retrieve tags: " + errorThrown);
					},
					success: function(data, textStatus, jqXHR) {
						// Update repoProblemTags div
						$("#repoProblemTags").empty();
						var i, elt;
						for (i = 0; i < data.length; i++) {
							elt = $("<span></span>")
								.addClass("repoProblemTag")
								.text(data[i]);
							$("#repoProblemTags").append(elt).append(" ");
						}
					},
					type: "GET"
				}
			);
		}

		$(document).ready(function() {
			// Get initial tags
			getTags();
		
			$("#addTagButton").click(function() {
				$.ajax(
					"${pageContext.servletContext.contextPath}/addTag",
					{
						data: {
							repoProblemId: ${RepoProblem.id},
							tag: $("#addTag").val()
						},
						dataType: "json",
						error: function(jqXHR, textStatus, errorThrown) {
							$("#addTagResult").text("Error adding tag: " + errorThrown);
						},
						success: function(data, textStatus, jqXHR) {
							$("#addTagResult").text(data.message);
							$("#addTag").val(""); // clear the textbox
							getTags(); // update tags
						},
						type: "POST"
					}
				);
			});
		});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Exercise ${RepoProblem.testname}</h1>
			
			<p><b>Author:</b> ${fn:escapeXml(RepoProblem.authorName)} &lt;<a href="mailto:${fn:escapeXml(RepoProblem.authorEmail)}">${fn:escapeXml(RepoProblem.authorEmail)}</a>&gt;</p>
			
			<p><b>Programming language:</b> ${RepoProblem.problemType.language.name} </p>
			
			<p><b>Hash code:</b> (use this to import the exercise into CloudCoder)</p>
			<blockquote><span class="exerciseHash">${RepoProblem.hash}</span></blockquote>
			
			<p><b>Description:</b></p>
			<blockquote class="exerciseDescription">
				<repo:sanitizeHTML html="${RepoProblem.description}"/>
			</blockquote>
			
			<p><b>Tags:</b></p>
			<div id="repoProblemTags"></div>
			<p>
				<c:if test="${empty User}">
					<a href="${pageContext.servletContext.contextPath}/login">Log in</a> to add a tag to this exercise
				</c:if>
				<c:if test="${!empty User}">
					Add a tag to this problem:
					<input id="addTag" type="text" size="25" />
					<button id="addTagButton">Add tag!</button>
					<span id="addTagResult"></span>
				</c:if>
			</p>
			
			<p><b>Test cases (${fn:length(RepoTestCases)}):</b></p>
			<c:forEach var="repoTestCase" items="${RepoTestCases}">
				${fn:escapeXml(repoTestCase.testCaseName)}
			</c:forEach>
			
			<p><b>License:</b> <a href="${RepoProblem.license.url}">${RepoProblem.license.name}</a>
			
			<c:if test="${! empty RepoProblem.parentHash}">
			<p>
				<b>Provenance:</b> This exercise is based on exercise
				<a href="${pageContext.servletContext.contextPath}/exercise/${RepoProblem.parentHash}">${RepoProblem.parentHash}</a>
			</p>
			</c:if>
		</div>
	</body>
</html>