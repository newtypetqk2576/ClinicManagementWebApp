<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>User details</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>User details</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <dl>
                <dt>User ID</dt><dd><c:out value="${user.userID}"/></dd>
                <dt>Username</dt><dd><c:out value="${user.username}"/></dd>
                <dt>Password</dt><dd><c:out value="${user.passwordHash}"/></dd>
                <dt>Role</dt><dd><c:out value="${user.role}"/></dd>
                <dt>Is active</dt><dd><c:out value="${user.active}"/></dd>
                <dt>Created at</dt><dd><c:out value="${user.createdAt}"/></dd>
                <dt>Updated at</dt><dd><c:out value="${user.updatedAt}"/></dd>
            </dl>
            <c:if test="${not empty user.patient}">
                <h2>Linked patient</h2>
                <dl>
                    <dt>Patient ID</dt><dd><c:out value="${user.patient.patientID}"/></dd>
                    <dt>Full name</dt><dd><c:out value="${user.patient.fullName}"/></dd>
                    <dt>Date of birth</dt><dd><c:out value="${user.patient.dateOfBirth}"/></dd>
                    <dt>Gender</dt><dd><c:out value="${user.patient.gender}"/></dd>
                    <dt>Phone</dt><dd><c:out value="${user.patient.phone}"/></dd>
                    <dt>Email</dt><dd><c:out value="${user.patient.email}"/></dd>
                    <dt>Address</dt><dd><c:out value="${user.patient.address}"/></dd>
                    <dt>Created at</dt><dd><c:out value="${user.patient.createdAt}"/></dd>
                    <dt>User ID</dt><dd><c:out value="${user.patient.userID}"/></dd>
                </dl>
            </c:if>
            <c:if test="${not empty user.doctor}">
                <h2>Linked doctor</h2>
                <dl>
                    <dt>Doctor ID</dt><dd><c:out value="${user.doctor.doctorID}"/></dd>
                    <dt>Full name</dt><dd><c:out value="${user.doctor.fullName}"/></dd>
                    <dt>Specialty</dt><dd><c:out value="${user.doctor.specialty}"/></dd>
                    <dt>Phone</dt><dd><c:out value="${user.doctor.phone}"/></dd>
                    <dt>Is active</dt><dd><c:out value="${user.doctor.active}"/></dd>
                    <dt>User ID</dt><dd><c:out value="${user.doctor.userID}"/></dd>
                </dl>
            </c:if>
            <c:if test="${empty user.patient and empty user.doctor}">
                <p>No patient or doctor profile is linked to this user.</p>
            </c:if>
            <h2>Edit user</h2>
            <c:url var="usersUrl" value="/admin/users"/>
            <form method="post" action="<c:out value="${usersUrl}"/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<c:out value="${user.userID}"/>">
                <label for="username">Username</label>
                <input id="username" name="username" maxlength="50" required
                       value="<c:out value="${user.username}"/>">
                <label for="password">New password (leave blank to keep current password)</label>
                <input id="password" name="password" type="text" maxlength="255">
                <label for="isActive">Status</label>
                <select id="isActive" name="isActive" required>
                    <option value="true" <c:if test="${user.active}">selected</c:if>>Active</option>
                    <option value="false" <c:if test="${not user.active}">selected</c:if>>Inactive</option>
                </select>
                <button type="submit">Save changes</button>
            </form>
            <p><a href="<c:out value="${usersUrl}"/>">Back to users</a></p>
        </main>
    </body>
</html>
