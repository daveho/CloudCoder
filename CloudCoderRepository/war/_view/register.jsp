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
						var queryUri = "${pageContext.servletContext.contextPath}/register";
						$(this).ajaxSubmit({
							url: queryUri,
							data: {
								u_username: $("#u_username").val(),
								u_firstname: $("#u_firstname").val(),
								u_lastname: $("#u_lastname").val(),
								u_email: $("#u_email").val(),
								u_website: $("#u_website").val(),
								u_password: $("#u_password").val()
							},
							dataType: "json",
							type: "post",
							success: function(data, textStatus, jqXHR) {
								//alert("Success!");
								$("#messageElt").text(data.message);
								if (data.success) {
									$("#messageElt").removeClass("error");
								} else {
									$("#messageElt").addClass("error");
								}
							},
							error: function(jqXHR, textStatus, errorThrown) {
								$("#messageElt").text("Error: " + errorThrown);
								$("#messageElt").addClass('error');
							}
						});
					},
					rules: {
						u_passwordConfirm: {
							required: true,
							equalTo: "#u_password"
						}
					}
				});
			});
		</script>
	</head>
	<body>
		<repo:topBanner/>
		<div id="content">
		<h1>Create an account</h1>
		<p>Please enter your account information (* denotes a required field):</p>
		<form id="regForm">
			<table>
				<tr>
					<td><label for="u_username">* User name:</label></td>
					<td><input id="u_username" name="u_username" class="required" type="text" size="20"></input></td>
				</tr>
				<tr>
					<td><label for="u_firstname">* First name:</label></td>
					<td><input id="u_firstname" name="u_firstname" class="required" type="text" size="20"></input></td>
				</tr>
				<tr>
					<td><label for="u_lastname">* Last name:</label></td>
					<td><input id="u_lastname" name="u_lastname" class="required" type="text" size="20"></input></td>
				</tr>
				<tr>
					<td><label for="u_email">* Email address:</label></td>
					<td><input id="u_email" name="u_email" class="required email" type="text" size="30"></input></td>
				</tr>
				<tr>
					<td><label for="u_website">Website URL:</label></td>
					<td><input id="u_website" name="u_website" type="text" size="50"></input></td>
				</tr>
				<tr>
					<td><label for="u_password">* Password:</label></td>
					<td><input id="u_password" name="u_password" class="required" type="password" size="12"></input></td>
				</tr>
				<tr>
					<td><label for="u_passwordConfirm">* Password (confirm):</label></td>
					<td><input id="u_passwordConfirm" name="u_passwordConfirm" class="required" type="password" size="12"></input></td>
				</tr>
				<tr>
					<td></td>
					<td><input name="submitElt" type="submit" value="Submit!" /></td>
				</tr>
			</table>
			
			<p id="messageElt"></p>
		</form>
		</div>
	</body>
</html>