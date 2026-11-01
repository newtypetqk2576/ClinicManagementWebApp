<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${managerMode}">Manager medical records</c:when><c:otherwise>Medical records</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Medical records</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:url var="newRecordUrl" value="${routeBase}/new"/>
            <p><a href="<c:out value="${newRecordUrl}"/>">Create medical record</a></p>
            <c:choose>
                <c:when test="${empty records}">
                    <p>No medical records were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Patient</th>
                                <th scope="col">Doctor</th>
                                <th scope="col">Visit date</th>
                                <th scope="col">Symptoms</th>
                                <th scope="col">Status</th>
                                <th scope="col">Detail</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="record" items="${records}">
                                <tr>
                                    <td><c:out value="${record.patientName}"/></td>
                                    <td><c:out value="${record.doctorName}"/></td>
                                    <td><c:out value="${record.visitDate}"/></td>
                                    <td><c:out value="${record.symptoms}"/></td>
                                    <td><c:out value="${record.status}"/></td>
                                    <td>
                                        <c:url var="detailUrl" value="${routeBase}">
                                            <c:param name="id" value="${record.medicalRecordID}"/>
                                        </c:url>
                                        <a href="<c:out value="${detailUrl}"/>">Detail / edit</a>
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
