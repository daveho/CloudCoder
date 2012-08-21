<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Log In"></repo:headStuff>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#usernameElt").focus();
			});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<form action="${pageContext.servletContext.contextPath}/login" method="POST">
				<table>
					<tr>
						<td>Username: </td>
						<td><input id="usernameElt" name="username" type="text" size="20" value="${username}" /></td>
					</tr>
					<tr>
						<td>Password: </td>
						<td><input name="password" type="password" size="20" /></td>
					</tr>
				</table>
				<input type="submit" value="Log In!" />
			</form>
			<c:if test="${! empty error}">
				<div class="error">${error}</div>
			</c:if>
		</div>
	</body>
</html>