<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>My medical records</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>My medical records</h1>
            <c:choose>
                <c:when test="${empty records}">
                    <p>You do not have any medical records yet.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">Doctor</th>
                                <th scope="col">VisitDate</th>
                                <th scope="col">Diagnosis</th>
                                <th scope="col">Status</th>
                                <th scope="col">Details</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="record" items="${records}">
                                <tr>
                                    <td><c:out value="${record.doctorName}"/></td>
                                    <td><c:out value="${record.visitDate}"/></td>
                                    <td><c:out value="${record.diagnosis}"/></td>
                                    <td><c:out value="${record.status}"/></td>
                                    <td>
                                        <c:url var="recordUrl" value="/medical-records">
                                            <c:param name="id" value="${record.medicalRecordID}"/>
                                        </c:url>
                                        <a href="<c:out value="${recordUrl}"/>">View record</a>
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
