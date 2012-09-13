<!DOCTYPE html>
<%@ taglib tagdir="/WEB-INF/tags" prefix="repo" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html>
	<head>
		<repo:headStuff title=""/>
	</head>

	<body>
		<repo:topBanner/>
		<div id="content">
			<h1>CloudCoder exercise repository</h1>
			<p>
				Welcome to the CloudCoder exercise repository! The repository contains
				programming exercises contributed by users of the
				<a href="http://cloudcoder.org/">CloudCoder</a> programming
				exercise system.  All of the exercises in the repository are
				<em>freely redistributable</em> under permissive licenses
				such as <a href="http://creativecommons.org/">Creative Commons</a> and
				<a href="http://www.gnu.org/copyleft/fdl.html">GNU FDL</a>.
				(The CloudCoder software is open source, distributed under the terms
				of the <a href="http://www.gnu.org/licenses/agpl.html">GNU AGPL</a> license.)
			</p>
			
			<p>
				We invite you to browse the repository to look at the available exercises.
				If you are using CloudCoder in a course you are teaching, it is simple
				to import any exercise in the repository into your own CloudCoder installation.
				And, if you have written your own CloudCoder exercises, we encourage you
				to share them here!
			</p>
			
			<h2>What would you like to do?</h2>
			
			<ul class="navChoiceList">
				<li>
					<a href="${pageContext.servletContext.contextPath}/search">Browse or search the repository</a> &mdash;
					find problems to use in your courses.
				</li>
				<li>
					<a href="${pageContext.servletContext.contextPath}/register">Create an account</a> &mdash;
					allows you to share your own problems, tag problems, and comment on problems.
				</li>
			</ul>
			
		</div>
	</body>
</html>