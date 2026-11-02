<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Edit patient</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Edit patient</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <dl>
                <dt>Patient ID</dt><dd><c:out value="${patient.patientID}"/></dd>
                <dt>Created at</dt><dd><c:out value="${patient.createdAt}"/></dd>
                <dt>Patient account</dt>
                <dd><c:choose><c:when test="${not empty patient.userID}">Linked</c:when><c:otherwise>Not linked</c:otherwise></c:choose></dd>
            </dl>
            <c:url var="patientsUrl" value="/patients"/>
            <form method="post" action="<c:out value="${patientsUrl}"/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<c:out value="${patient.patientID}"/>">
                <label for="fullName">Full name</label>
                <input id="fullName" name="fullName" maxlength="100" required
                       value="<c:out value="${patient.fullName}"/>">
                <label for="dateOfBirth">Date of birth</label>
                <input id="dateOfBirth" type="date" name="dateOfBirth"
                       value="<c:out value="${dateOfBirthValue}"/>">
                <label for="gender">Gender</label>
                <select id="gender" name="gender" required>
                    <option value="">Select a gender</option>
                    <c:forEach var="genderOption" items="${genderOptions}">
                        <option value="<c:out value="${genderOption}"/>"
                                <c:if test="${patient.gender eq genderOption}">selected</c:if>>
                            <c:out value="${genderOption}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="phone">Phone</label>
                <input id="phone" name="phone" maxlength="15" required
                       value="<c:out value="${patient.phone}"/>">
                <label for="email">Email</label>
                <input id="email" type="email" name="email" maxlength="100"
                       value="<c:out value="${patient.email}"/>">
                <label for="address">Address</label>
                <textarea id="address" name="address" maxlength="255"><c:out value="${patient.address}"/></textarea>
                <button type="submit">Save changes</button>
            </form>
            <c:url var="listUrl" value="/patients"/>
            <p><a href="<c:out value="${listUrl}"/>">Back to patients</a></p>
        </main>
    </body>
</html>
