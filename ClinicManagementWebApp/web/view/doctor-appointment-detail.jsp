<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Appointment details</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Appointment details</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Appointment</h2>
            <dl>
                <dt>Appointment ID</dt><dd><c:out value="${appointment.appointmentID}"/></dd>
                <dt>PatientName</dt><dd><c:out value="${appointment.patient.fullName}"/></dd>
                <dt>DoctorName</dt><dd><c:out value="${appointment.doctorName}"/></dd>
                <dt>Reason</dt><dd><c:out value="${appointment.reason}"/></dd>
                <dt>CreatedAt</dt><dd><c:out value="${appointment.createdAt}"/></dd>
                <dt>AppointmentDate</dt><dd><c:out value="${appointment.appointmentDate}"/></dd>
                <dt>Status</dt><dd><c:out value="${appointment.status}"/></dd>
            </dl>

            <c:if test="${appointment.status eq 'BOOKED' and (managerMode or appointment.doctorID eq doctorID)}">
                <c:url var="appointmentsUrl" value="${routeBase}"/>
                <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                    <input type="hidden" name="action" value="confirm">
                    <input type="hidden" name="id" value="<c:out value="${appointment.appointmentID}"/>">
                    <button type="submit">Confirm appointment</button>
                </form>
            </c:if>

            <h2>Patient information</h2>
            <dl>
                <dt>Patient ID</dt><dd><c:out value="${appointment.patient.patientID}"/></dd>
                <dt>Full name</dt><dd><c:out value="${appointment.patient.fullName}"/></dd>
                <dt>Date of birth</dt><dd><c:out value="${appointment.patient.dateOfBirth}"/></dd>
                <dt>Gender</dt><dd><c:out value="${appointment.patient.gender}"/></dd>
                <dt>Phone</dt><dd><c:out value="${appointment.patient.phone}"/></dd>
                <dt>Email</dt><dd><c:out value="${appointment.patient.email}"/></dd>
                <dt>Address</dt><dd><c:out value="${appointment.patient.address}"/></dd>
                <dt>Patient created at</dt><dd><c:out value="${appointment.patient.createdAt}"/></dd>
            </dl>
            <c:url var="newRecordUrl" value="${recordBasePath}/new">
                <c:param name="appointmentID" value="${appointment.appointmentID}"/>
            </c:url>
            <p><a href="<c:out value="${newRecordUrl}"/>">Create medical record from this appointment</a></p>
            <c:url var="listUrl" value="${routeBase}"/>
            <p><a href="<c:out value="${listUrl}"/>">Back to appointments</a></p>
        </main>
    </body>
</html>
