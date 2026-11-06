<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết lịch hẹn</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Chi tiết lịch hẹn</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Lịch hẹn</h2>
            <dl>
                <dt>Mã lịch hẹn</dt><dd><c:out value="${appointment.appointmentID}"/></dd>
                <dt>Bệnh nhân</dt><dd><c:out value="${appointment.patient.fullName}"/></dd>
                <dt>Bác sĩ</dt><dd><c:out value="${appointment.doctorName}"/></dd>
                <dt>Lý do khám</dt><dd><c:out value="${appointment.reason}"/></dd>
                <dt>Ngày tạo</dt><dd><list:dateTime value="${appointment.createdAt}"/></dd>
                <dt>Ngày giờ hẹn</dt><dd><list:dateTime value="${appointment.appointmentDate}"/></dd>
                <dt>Trạng thái</dt><dd><list:status value="${appointment.status}"/></dd>
            </dl>

            <c:if test="${appointment.status eq 'BOOKED' and (managerMode or appointment.doctorID eq doctorID)}">
                <c:url var="appointmentsUrl" value="${routeBase}"/>
                <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                    <input type="hidden" name="action" value="confirm">
                    <input type="hidden" name="id" value="<c:out value="${appointment.appointmentID}"/>">
                    <button type="submit">Xác nhận lịch hẹn</button>
                </form>
            </c:if>

            <h2>Thông tin bệnh nhân</h2>
            <dl>
                <dt>Mã bệnh nhân</dt><dd><c:out value="${appointment.patient.patientID}"/></dd>
                <dt>Họ và tên</dt><dd><c:out value="${appointment.patient.fullName}"/></dd>
                <dt>Ngày sinh</dt><dd><c:out value="${appointment.patient.dateOfBirth}"/></dd>
                <dt>Giới tính</dt><dd><c:out value="${appointment.patient.gender}"/></dd>
                <dt>Số điện thoại</dt><dd><c:out value="${appointment.patient.phone}"/></dd>
                <dt>Email</dt><dd><c:out value="${appointment.patient.email}"/></dd>
                <dt>Địa chỉ</dt><dd><c:out value="${appointment.patient.address}"/></dd>
                <dt>Ngày tạo hồ sơ bệnh nhân</dt><dd><c:out value="${appointment.patient.createdAt}"/></dd>
            </dl>
            <c:url var="newRecordUrl" value="${recordBasePath}/new">
                <c:param name="appointmentID" value="${appointment.appointmentID}"/>
            </c:url>
            <p><a href="<c:out value="${newRecordUrl}"/>">Tạo hồ sơ khám từ lịch hẹn này</a></p>
            <c:url var="listUrl" value="${routeBase}"/>
            <p><a href="<c:out value="${listUrl}"/>">Về danh sách lịch hẹn</a></p>
        </main>
    </body>
</html>
