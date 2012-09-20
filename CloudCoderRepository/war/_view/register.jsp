<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title="Search the repository"></repo:headStuff>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/jquery.validate.min.js"></script>
		<script type="text/javascript" src="${pageContext.servletContext.contextPath}/js/jquery.form.js"></script>
		<script type="text/javascript">
			$(document).ready(function() {
				$("#regForm").validate({
					submitHandler: function(form) {
						alert("Submit handler!");
						$(this).ajaxSubmit({
							dataType: "json",
							success: function(data, textStatus, jqXHR) {
								alert("Success!");
							},
							error: function(jqXHR, textStatus, errorThrown) {
								alert("Error!");
							}
						});
					}
				});
			});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
		<h1>Create an account</h1>
		<p>Please enter your account information:</p>
		<form id="regForm">
			<table>
				<tr>
					<td class="label">First name:</td>
					<td><input name="u_firstname" class="required" type="text" size="20"></input></td>
				</tr>
				<tr>
					<td class="label">Last name:</td>
					<td><input name="u_lastname" class="required" type="text" size="20"></input></td>
				</tr>
				<tr>
					<td class="label">Email address:</td>
					<td><input name="u_username" class="required email" type="text" size="30"></input></td>
				</tr>
				<tr>
					<td class="label">Website URL:</td>
					<td><input name="u_website" type="text" size="50"></input></td>
				</tr>
				<tr>
					<td class="label">Password:</td>
					<td><input name="u_password" class="required" type="password" size="12"></input></td>
				</tr>
				<tr>
					<td class="label">Password (confirm):</td>
					<td><input name="u_passwordConfirm" class="required" type="password" size="12"></input></td>
				</tr>
				
				<tr>
					<td></td><td><input name="submitElt" type="submit" value="Submit!" /></td>
				</tr>
				
			</table>
		</form>
		</div>
	</body>
</html>