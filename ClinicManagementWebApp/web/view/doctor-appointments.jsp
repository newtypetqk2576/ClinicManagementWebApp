<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${managerMode}">Manager appointments</c:when><c:otherwise>Doctor appointments</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>All appointments</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="createAppointmentUrl" value="${routeBase}/new"/>
            <p><a href="<c:out value="${createAppointmentUrl}"/>">Create appointment</a></p>
            <c:choose>
                <c:when test="${empty appointments}">
                    <p>No appointments were found.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
                                <th scope="col">PatientName</th>
                                <th scope="col">DoctorName</th>
                                <th scope="col">Reason</th>
                                <th scope="col">CreatedAt</th>
                                <th scope="col">AppointmentDate</th>
                                <th scope="col">Status</th>
                                <th scope="col">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="appointment" items="${appointments}">
                                <tr>
                                    <td><c:out value="${appointment.patientName}"/></td>
                                    <td><c:out value="${appointment.doctorName}"/></td>
                                    <td><c:out value="${appointment.reason}"/></td>
                                    <td><c:out value="${appointment.createdAt}"/></td>
                                    <td><c:out value="${appointment.appointmentDate}"/></td>
                                    <td><c:out value="${appointment.status}"/></td>
                                    <td>
                                        <c:url var="detailUrl" value="${routeBase}">
                                            <c:param name="id" value="${appointment.appointmentID}"/>
                                        </c:url>
                                        <a href="<c:out value="${detailUrl}"/>">Detail</a>
                                        <c:if test="${appointment.status eq 'BOOKED' and (managerMode or appointment.doctorID eq doctorID)}">
                                            <c:url var="appointmentsUrl" value="${routeBase}"/>
                                            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                                                <input type="hidden" name="action" value="confirm">
                                                <input type="hidden" name="id"
                                                       value="<c:out value="${appointment.appointmentID}"/>">
                                                <button type="submit">Confirm</button>
                                            </form>
                                        </c:if>
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
