<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Lịch hẹn của tôi</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Lịch hẹn của tôi</h1>
            <c:if test="${not empty message}"><p role="status"><c:out value="${message}"/></p></c:if>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>

            <h2>Đặt lịch khám</h2>
            <c:url var="appointmentsUrl" value="/appointments"/>
            <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                <input type="hidden" name="action" value="create">
                <label for="doctorID">Bác sĩ</label>
                <select id="doctorID" name="doctorID" required>
                    <option value="">Chọn bác sĩ</option>
                    <c:forEach var="doctor" items="${doctors}">
                        <option value="<c:out value="${doctor.doctorID}"/>"
                                <c:if test="${doctor.doctorID eq doctorIDValue}">selected</c:if>>
                            <c:out value="${doctor.fullName}"/> — <c:out value="${doctor.specialty}"/>
                        </option>
                    </c:forEach>
                </select>
                <label for="appointmentDate">Ngày giờ hẹn</label>
                <input id="appointmentDate" type="datetime-local" name="appointmentDate"
                       value="<c:out value="${appointmentDateValue}"/>" required>
                <label for="reason">Lý do khám</label>
                <textarea id="reason" name="reason" maxlength="255"><c:out value="${reasonValue}"/></textarea>
                <button type="submit">Đặt lịch khám</button>
            </form>

            <h2>Danh sách lịch hẹn</h2>
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
                                        <td><c:out value="${appointment.doctorName}"/></td>
                                        <td><c:out value="${appointment.reason}"/></td>
                                        <td><list:dateTime value="${appointment.createdAt}"/></td>
                                        <td><list:dateTime value="${appointment.appointmentDate}"/></td>
                                        <td><list:status value="${appointment.status}"/></td>
                                        <td>
                                            <c:if test="${appointment.cancellable}">
                                                <form method="post" action="<c:out value="${appointmentsUrl}"/>">
                                                    <input type="hidden" name="action" value="cancel">
                                                    <input type="hidden" name="appointmentID"
                                                           value="<c:out value="${appointment.appointmentID}"/>">
                                                    <button type="submit">Hủy lịch</button>
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
