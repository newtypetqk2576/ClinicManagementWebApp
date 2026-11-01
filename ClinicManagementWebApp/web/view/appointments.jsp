<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Patient appointments</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>My appointments</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Book an appointment</h2>
            <c:url var="appointmentsUrl" value="/appointments"/>
            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="doctorID">Doctor</label>
                <select id="doctorID" name="doctorID" required>
                    <option value="">Select a doctor</option>
                    <c:forEach var="doctor" items="${doctors}">
                        <option value="<c:out value="${doctor.doctorID}"/>"
                                <c:if test="${doctor.doctorID eq doctorIDValue}">selected</c:if>>
                            <c:out value="${doctor.fullName}"/> — <c:out value="${doctor.specialty}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="appointmentDate">Date and time</label>
                <input id="appointmentDate" type="datetime-local" name="appointmentDate"
                       value="<c:out value="${appointmentDateValue}"/>" required>
                <label for="reason">Reason</label>
                <textarea id="reason" name="reason" maxlength="255"><c:out value="${reasonValue}"/></textarea>
                <button type="submit">Create appointment</button>
            </form>

            <h2>Appointment history</h2>
            <c:choose>
                <c:when test="${empty appointments}">
                    <p>You do not have any appointments yet.</p>
                </c:when>
                <c:otherwise>
                    <table>
                        <thead>
                            <tr>
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
                                    <td><c:out value="${appointment.doctorName}"/></td>
                                    <td><c:out value="${appointment.reason}"/></td>
                                    <td><c:out value="${appointment.createdAt}"/></td>
                                    <td><c:out value="${appointment.appointmentDate}"/></td>
                                    <td><c:out value="${appointment.status}"/></td>
                                    <td>
                                        <c:if test="${appointment.cancellable}">
                                            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                                                <input type="hidden" name="action" value="cancel">
                                                <input type="hidden" name="appointmentID"
                                                       value="<c:out value="${appointment.appointmentID}"/>">
                                                <button type="submit">Cancel</button>
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
