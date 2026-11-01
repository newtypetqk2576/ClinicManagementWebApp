<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Sign in</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body class="auth-page">
        <main class="auth-card">
            <h1>Sign in</h1>
            <c:if test="${registered}"><p role="status">Patient account created. Sign in to continue.</p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <form method="post" action="${pageContext.request.contextPath}/login">
                <label>Username <input name="username" maxlength="50" autocomplete="username" required></label>
                <label>Password <input type="password" name="password" maxlength="255" autocomplete="current-password" required></label>
                <button type="submit">Sign in</button>
            </form>
            <p><a href="${pageContext.request.contextPath}/register">Create a patient account</a></p>
        </main>
    </body>
</html>
