<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html lang="en">

<body>
	<h1><c:out value="${message.summary}"/></h1>
	<p>
		<c:out value="${message.text}"/>
	</p>
	<p>
		<a id="p" href="#" data-bind="<c:out value="${message.text}"/>">Process Text</a>
	</p>
	<p>
		<a href="./fix?id=120">Safe</a> |
		<a href="./fix?id=121">HTML</a> |
		<a href="./fix?id=122">Contextual</a>
	</p>
	<script type="text/javascript" src="../resources/js/jquery-1.8.3.js"></script>
	<script src="../resources/js/xss/fix.js" type="text/javascript"></script>
</body>

</html>