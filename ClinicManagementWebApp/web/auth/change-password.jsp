<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Change password</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Change password</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:if test="${not empty success}"><p role="status"><c:out value="${success}"/></p></c:if>
            <form method="post" action="${pageContext.request.contextPath}/account/change-password">
                <label>Current password <input type="password" name="currentPassword" maxlength="255" autocomplete="current-password" required></label>
                <label>New password <input type="password" name="newPassword" maxlength="255" autocomplete="new-password" required></label>
                <label>Confirm new password <input type="password" name="confirmPassword" maxlength="255" autocomplete="new-password" required></label>
                <button type="submit">Change password</button>
            </form>
            <p><a href="${pageContext.request.contextPath}/">Home</a></p>
        </main>
    </body>
</html>
