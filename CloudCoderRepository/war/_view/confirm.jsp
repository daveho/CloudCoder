<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Confirm registration"></repo:headStuff>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>Confirm registration</h1>
			
			<c:if test="${confSuccess}">
				<p>${confMessage}</p>
				<p>You should now be able to <a href="${pageContext.servletContext.contextPath}/login">log in</a>.</p>
			</c:if>
			<c:if test="${!confSuccess}">
				<p class="error">${confMessage}</p>
			</c:if>
		</div>
	</body>
</html>