<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${not empty sessionScope.Role}">
<nav class="app-navigation" aria-label="Main navigation">
    <ul class="app-navigation__items">
        <c:url var="appointmentsUrl" value="/appointments"/>
        <c:choose>
            <c:when test="${sessionScope.Role eq 'PATIENT'}">
                <li><a href="<c:out value="${appointmentsUrl}"/>">Appointment</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'DOCTOR'}">
                <c:url var="doctorAppointmentsUrl" value="/doctor/appointments"/>
                <li><a href="<c:out value="${doctorAppointmentsUrl}"/>">Appointment</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'MANAGER'}">
                <c:url var="managerAppointmentsUrl" value="/admin/appointments"/>
                <li><a href="<c:out value="${managerAppointmentsUrl}"/>">Appointment</a></li>
            </c:when>
            <c:otherwise>
                <li><a href="<c:out value="${appointmentsUrl}"/>">Appointment <span class="app-navigation__placeholder">Planned</span></a></li>
            </c:otherwise>
        </c:choose>
        <c:url var="servicesUrl" value="/services"/>
        <c:url var="medicalRecordsUrl" value="/medical-records"/>
        <c:choose>
            <c:when test="${sessionScope.Role eq 'PATIENT'}">
                <li><a href="<c:out value="${servicesUrl}"/>">Service</a></li>
                <li><a href="<c:out value="${medicalRecordsUrl}"/>">Medical Record</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'DOCTOR'}">
                <c:url var="doctorServicesUrl" value="/doctor/services"/>
                <c:url var="doctorMedicalRecordsUrl" value="/doctor/medical-records"/>
                <li><a href="<c:out value="${doctorServicesUrl}"/>">Service</a></li>
                <li><a href="<c:out value="${doctorMedicalRecordsUrl}"/>">Medical Record</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'MANAGER'}">
                <c:url var="managerServicesUrl" value="/doctor/services"/>
                <c:url var="managerMedicalRecordsUrl" value="/admin/medical-records"/>
                <li><a href="<c:out value="${managerServicesUrl}"/>">Service</a></li>
                <li><a href="<c:out value="${managerMedicalRecordsUrl}"/>">Medical Record</a></li>
            </c:when>
            <c:otherwise>
                <li><a href="<c:out value="${servicesUrl}"/>">Service <span class="app-navigation__placeholder">Planned</span></a></li>
                <li><a href="<c:out value="${medicalRecordsUrl}"/>">Medical Record <span class="app-navigation__placeholder">Planned</span></a></li>
            </c:otherwise>
        </c:choose>
        <c:if test="${sessionScope.Role eq 'DOCTOR'}">
            <c:url var="doctorMedicinesUrl" value="/doctor/medicines"/>
            <li><a href="<c:out value="${doctorMedicinesUrl}"/>">Medicine</a></li>
            <c:url var="patientsUrl" value="/patients"/>
            <li><a href="<c:out value="${patientsUrl}"/>">Patient</a></li>
        </c:if>
        <c:if test="${sessionScope.Role eq 'MANAGER'}">
            <c:url var="managerPatientsUrl" value="/patients"/>
            <li><a href="<c:out value="${managerPatientsUrl}"/>">Patient</a></li>
            <c:url var="managerMedicinesUrl" value="/doctor/medicines"/>
            <li><a href="<c:out value="${managerMedicinesUrl}"/>">Medicine</a></li>
            <c:url var="usersUrl" value="/admin/users"/>
            <li><a href="<c:out value="${usersUrl}"/>">User</a></li>
            <c:url var="doctorsUrl" value="/admin/doctors"/>
            <li><a href="<c:out value="${doctorsUrl}"/>">Doctor</a></li>
        </c:if>
    </ul>
</nav>
</c:if>
