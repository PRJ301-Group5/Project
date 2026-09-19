<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:choose>
    <c:when test="${not empty sessionScope.user}">
        <c:redirect url="/assignments"/>
    </c:when>
    <c:otherwise>
        <c:redirect url="/login"/>
    </c:otherwise>
</c:choose>
