<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ attribute name="title" required="true" %>
<title>CloudCoder Exercise Repository<c:if test="${! empty title}"> - ${title}</c:if></title>
<link rel="stylesheet" type="text/css" href="${pageContext.servletContext.contextPath}/css/cloudcoderRepo.css"></link>
