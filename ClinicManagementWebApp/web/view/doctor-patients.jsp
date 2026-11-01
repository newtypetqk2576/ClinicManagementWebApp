<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Patients</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Patients</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Create patient</h2>
            <c:url var="patientsUrl" value="/patients"/>
            <form method="post" action="<c:out value="${patientsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="fullName">Full name</label>
                <input id="fullName" name="fullName" maxlength="100" required
                       value="<c:out value="${formPatient.fullName}"/>">
                <label for="dateOfBirth">Date of birth</label>
                <input id="dateOfBirth" type="date" name="dateOfBirth"
                       value="<c:out value="${dateOfBirthValue}"/>">
                <label for="gender">Gender</label>
                <select id="gender" name="gender" required>
                    <option value="">Select a gender</option>
                    <option value="Nam" <c:if test="${formPatient.gender eq 'Nam'}">selected</c:if>>Nam</option>
                    <option value="Nữ" <c:if test="${formPatient.gender eq 'Nữ'}">selected</c:if>>Nữ</option>
                    <option value="Không" <c:if test="${formPatient.gender eq 'Không'}">selected</c:if>>Không</option>
                </select>
                <label for="phone">Phone</label>
                <input id="phone" name="phone" maxlength="15" required
                       value="<c:out value="${formPatient.phone}"/>">
                <label for="email">Email</label>
                <input id="email" type="email" name="email" maxlength="100"
                       value="<c:out value="${formPatient.email}"/>">
                <label for="address">Address</label>
                <textarea id="address" name="address" maxlength="255"><c:out value="${formPatient.address}"/></textarea>
                <button type="submit">Create patient</button>
            </form>

            <h2>Patient list</h2>
            <c:choose>
                <c:when test="${empty patients}">
                    <p>No patients were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr><th scope="col">Name</th><th scope="col">Phone</th><th scope="col">Actions</th></tr>
                        </thead>
                        <tbody>
                            <c:forEach var="patient" items="${patients}">
                                <tr>
                                    <td><c:out value="${patient.fullName}"/></td>
                                    <td><c:out value="${patient.phone}"/></td>
                                    <td>
                                        <c:url var="editUrl" value="/patients">
                                            <c:param name="id" value="${patient.patientID}"/>
                                        </c:url>
                                        <a href="<c:out value="${editUrl}"/>">Edit</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </c:otherwise>
            </c:choose>
        </main>
    </body>
</html>
