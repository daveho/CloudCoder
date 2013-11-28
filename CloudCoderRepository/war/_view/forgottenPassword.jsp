<!DOCTYPE html>
<%@ taglib uri="/WEB-INF/repo.tld" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Forgotten Password"></repo:headStuff>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#emailAddressElt").focus();
			});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
			<c:if test="${! empty message}">
				<p>${message}</p>
			</c:if>

			<c:if test="${empty message}">
				<p>
					Enter your email address, then click the submit button.  You should
					receive an email with a password reset link shortly.
				</p>
				<form action="${pageContext.servletContext.contextPath}/forgottenPassword" method="post">
					<table>
						<tr>
							<td>Email address:</td>
							<td><input id="emailAddressElt" name="emailAddress" type="text" size="40" value="${emailAddress}" /></td>
						</tr>
						<tr>
							<td></td>
							<td><input type="submit" value="Submit" /></td>
						</tr>
					</table>
				</form>
				<c:if test="${! empty error}">
					<div class="error">${error}</div>
				</c:if>
			</c:if>		
		</div>
	</body>
</html>