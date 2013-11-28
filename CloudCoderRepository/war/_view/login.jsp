<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
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
			<h1>Log in to the CloudCoder exercise repository</h1>
			<form action="${pageContext.servletContext.contextPath}/login" method="POST">
				<table>
					<tr>
						<td>Username: </td>
						<td>
							<input id="usernameElt" name="username" type="text" size="20" value="${username}" />
							<a href="${pageContext.servletContext.contextPath}/forgottenPassword">I forgot my password</a>
						</td>
					</tr>
					<tr>
						<td>Password: </td>
						<td><input name="password" type="password" size="20" /></td>
					</tr>
					<tr>
						<td></td>
						<td><input type="submit" value="Log In!" /></td>
					</tr>
				</table>
				<input type="hidden" name="redirectPath" value="${redirectPath}" />
			</form>
			<p></p>
			<c:if test="${! empty error}">
				<div class="error">${error}</div>
			</c:if>
		</div>
	</body>
</html>