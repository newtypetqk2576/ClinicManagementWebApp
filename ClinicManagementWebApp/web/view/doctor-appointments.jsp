<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${managerMode}">Lịch hẹn</c:when><c:otherwise>Lịch hẹn</c:otherwise></c:choose></title>
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
            <list:filters/>
            <c:choose>
                <c:when test="${empty appointments}">
                    <p>Không tìm thấy lịch hẹn.</p>
                </c:when>
                <c:otherwise>
                    <div class="table-scroll">
                        <table>
                            <thead>
                                <tr>
                                    <th scope="col">Bệnh nhân</th>
                                    <th scope="col">Bác sĩ</th>
                                    <th scope="col">Lý do khám</th>
                                    <th scope="col">Ngày tạo</th>
                                    <th scope="col">Ngày giờ hẹn</th>
                                    <th scope="col">Trạng thái</th>
                                    <th scope="col">Thao tác</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="appointment" items="${appointments}">
                                    <tr>
                                        <td><c:out value="${appointment.patientName}"/></td>
                                        <td><c:out value="${appointment.doctorName}"/></td>
                                        <td><c:out value="${appointment.reason}"/></td>
                                        <td><list:dateTime value="${appointment.createdAt}"/></td>
                                        <td><list:dateTime value="${appointment.appointmentDate}"/></td>
                                        <td><list:status value="${appointment.status}"/></td>
                                        <td>
                                            <c:url var="detailUrl" value="${routeBase}">
                                                <c:param name="id" value="${appointment.appointmentID}"/>
                                            </c:url>
                                            <a href="<c:out value="${detailUrl}"/>">Xem chi tiết</a>
                                            <c:if test="${appointment.status eq 'BOOKED' and (managerMode or appointment.doctorID eq doctorID)}">
                                                <c:url var="appointmentsUrl" value="${routeBase}"/>
                                                <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                                                    <input type="hidden" name="action" value="confirm">
                                                    <input type="hidden" name="id"
                                                           value="<c:out value="${appointment.appointmentID}"/>">
                                                    <button type="submit">Xác nhận</button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
            <list:pagination/>
        </main>
    </body>
</html>
