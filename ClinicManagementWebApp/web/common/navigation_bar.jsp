<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${not empty sessionScope.Role}">
<nav class="app-navigation" aria-label="Điều hướng chính">
    <ul class="app-navigation__items">
        <c:if test="${sessionScope.Role eq 'MANAGER'}">
            <li><a href="<c:url value="/admin/dashboard"/>">Tổng quan</a></li>
        </c:if>
        <c:url var="appointmentsUrl" value="/appointments"/>
        <c:choose>
            <c:when test="${sessionScope.Role eq 'PATIENT'}">
                <li><a href="<c:out value="${appointmentsUrl}"/>">Lịch hẹn</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'DOCTOR'}">
                <c:url var="doctorAppointmentsUrl" value="/doctor/appointments"/>
                <li><a href="<c:out value="${doctorAppointmentsUrl}"/>">Lịch hẹn</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'MANAGER'}">
                <c:url var="managerAppointmentsUrl" value="/admin/appointments"/>
                <li><a href="<c:out value="${managerAppointmentsUrl}"/>">Lịch hẹn</a></li>
            </c:when>
            <c:otherwise>
                <li><a href="<c:out value="${appointmentsUrl}"/>">Lịch hẹn <span class="app-navigation__placeholder">Dự kiến</span></a></li>
            </c:otherwise>
        </c:choose>
        <c:url var="servicesUrl" value="/services"/>
        <c:url var="medicalRecordsUrl" value="/medical-records"/>
        <c:choose>
            <c:when test="${sessionScope.Role eq 'PATIENT'}">
                <li><a href="<c:out value="${servicesUrl}"/>">Dịch vụ khám</a></li>
                <li><a href="<c:out value="${medicalRecordsUrl}"/>">Hồ sơ khám</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'DOCTOR'}">
                <c:url var="doctorServicesUrl" value="/doctor/services"/>
                <c:url var="doctorMedicalRecordsUrl" value="/doctor/medical-records"/>
                <li><a href="<c:out value="${doctorServicesUrl}"/>">Dịch vụ khám</a></li>
                <li><a href="<c:out value="${doctorMedicalRecordsUrl}"/>">Hồ sơ khám</a></li>
            </c:when>
            <c:when test="${sessionScope.Role eq 'MANAGER'}">
                <c:url var="managerServicesUrl" value="/doctor/services"/>
                <c:url var="managerMedicalRecordsUrl" value="/admin/medical-records"/>
                <li><a href="<c:out value="${managerServicesUrl}"/>">Dịch vụ khám</a></li>
                <li><a href="<c:out value="${managerMedicalRecordsUrl}"/>">Hồ sơ khám</a></li>
            </c:when>
            <c:otherwise>
                <li><a href="<c:out value="${servicesUrl}"/>">Dịch vụ khám <span class="app-navigation__placeholder">Dự kiến</span></a></li>
                <li><a href="<c:out value="${medicalRecordsUrl}"/>">Hồ sơ khám <span class="app-navigation__placeholder">Dự kiến</span></a></li>
            </c:otherwise>
        </c:choose>
        <c:if test="${sessionScope.Role eq 'DOCTOR'}">
            <c:url var="doctorMedicinesUrl" value="/doctor/medicines"/>
            <li><a href="<c:out value="${doctorMedicinesUrl}"/>">Thuốc</a></li>
            <c:url var="patientsUrl" value="/patients"/>
            <li><a href="<c:out value="${patientsUrl}"/>">Bệnh nhân</a></li>
        </c:if>
        <c:if test="${sessionScope.Role eq 'MANAGER'}">
            <c:url var="managerPatientsUrl" value="/patients"/>
            <li><a href="<c:out value="${managerPatientsUrl}"/>">Bệnh nhân</a></li>
            <c:url var="managerMedicinesUrl" value="/doctor/medicines"/>
            <li><a href="<c:out value="${managerMedicinesUrl}"/>">Thuốc</a></li>
            <c:url var="usersUrl" value="/admin/users"/>
            <li><a href="<c:out value="${usersUrl}"/>">Tài khoản</a></li>
            <c:url var="doctorsUrl" value="/admin/doctors"/>
            <li><a href="<c:out value="${doctorsUrl}"/>">Bác sĩ</a></li>
        </c:if>
    </ul>
</nav>
</c:if>
