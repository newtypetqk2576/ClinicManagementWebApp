<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Doctors</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Doctors</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newDoctorUrl" value="/admin/doctors/new"/>
            <p><a href="<c:out value="${newDoctorUrl}"/>">Create doctor</a></p>
            <c:choose>
                <c:when test="${empty doctors}">
                    <p>No doctors were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">ID</th>
                                <th scope="col">Name</th>
                                <th scope="col">Specialty</th>
                                <th scope="col">Phone</th>
                                <th scope="col">Status</th>
                                <th scope="col">Account</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="doctor" items="${doctors}">
                                <tr>
                                    <td><c:out value="${doctor.doctorID}"/></td>
                                    <td><c:out value="${doctor.fullName}"/></td>
                                    <td><c:out value="${doctor.specialty}"/></td>
                                    <td><c:out value="${doctor.phone}"/></td>
                                    <td><c:choose><c:when test="${doctor.active}">Active</c:when><c:otherwise>Inactive</c:otherwise></c:choose></td>
                                    <td><c:choose><c:when test="${not empty doctor.userID}">Linked</c:when><c:otherwise>Not linked</c:otherwise></c:choose></td>
                                    <td>
                                        <c:url var="editDoctorUrl" value="/admin/doctors/edit">
                                            <c:param name="id" value="${doctor.doctorID}"/>
                                        </c:url>
                                        <a href="<c:out value="${editDoctorUrl}"/>">Edit</a>
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
