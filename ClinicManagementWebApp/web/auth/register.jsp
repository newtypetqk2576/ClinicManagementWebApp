<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Patient registration</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body class="auth-page">
        <main class="auth-card">
            <h1>Create a patient account</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <form method="post" action="${pageContext.request.contextPath}/register">
                <label>Username <input name="username" maxlength="50" autocomplete="username" required></label>
                <label>Password <input type="password" name="password" maxlength="255" autocomplete="new-password" required></label>
                <label>Confirm password <input type="password" name="confirmPassword" maxlength="255" autocomplete="new-password" required></label>
                <label>Full name <input name="fullName" maxlength="100" autocomplete="name" required></label>
                <label>Phone <input name="phone" maxlength="15" autocomplete="tel" required></label>
                <label>Email <input type="email" name="email" maxlength="100" autocomplete="email"></label>
                <label>Address <input name="address" maxlength="255" autocomplete="street-address"></label>
                <button type="submit">Register</button>
            </form>
            <p><a href="${pageContext.request.contextPath}/login">Already registered? Sign in</a></p>
        </main>
    </body>
</html>
