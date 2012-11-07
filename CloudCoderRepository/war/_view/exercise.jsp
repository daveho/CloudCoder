<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="View exercise"></repo:headStuff>
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
			
			<c:if test="${fn:length(RepoProblemTags) > 0}">
				<p><b>Tags:</b></p>
				<c:forEach var="tag" items="${RepoProblemTags}">
					<span class="repoProblemTag">${tag.name}</span>
				</c:forEach>
			</c:if>
			<p>
				<c:if test="${empty User}">
					Log in to tag this problem
				</c:if>
				<c:if test="${!empty User}">
					Add a tag to this problem
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