<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Lịch hẹn</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Lịch hẹn</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:url var="createAppointmentUrl" value="${routeBase}/new"/>
            <p><a class="button" href="<c:out value="${createAppointmentUrl}"/>">Đặt lịch khám</a></p>
            <section aria-labelledby="today-appointments">
                <h2 id="today-appointments">Lịch hẹn hôm nay</h2>
                <list:appointmentTable rows="${todaysAppointments}"/>
            </section>
            <section aria-labelledby="all-appointments">
                <h2 id="all-appointments">Tất cả lịch hẹn</h2>
                <list:filters/>
                <list:appointmentTable rows="${appointments}"/>
                <list:pagination/>
            </section>
        </main>
    </body>
</html>
