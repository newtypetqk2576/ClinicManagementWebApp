<%@tag pageEncoding="UTF-8"%>
<%@attribute name="rows" required="true" type="java.util.List"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<c:choose>
    <c:when test="${empty rows}">
        <p>Không tìm thấy lịch hẹn.</p>
    </c:when>
    <c:otherwise>
        <c:url var="appointmentsUrl" value="${routeBase}"/>
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
                    <c:forEach var="appointment" items="${rows}">
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
