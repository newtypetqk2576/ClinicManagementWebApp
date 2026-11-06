<%@tag pageEncoding="UTF-8"%>
<%@attribute name="value" required="true" type="java.lang.String"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<span class="status-label">
    <c:choose>
        <c:when test="${value eq 'BOOKED'}">Đã đặt</c:when>
        <c:when test="${value eq 'CONFIRMED'}">Đã xác nhận</c:when>
        <c:when test="${value eq 'IN_PROGRESS'}">Đang khám</c:when>
        <c:when test="${value eq 'COMPLETED'}">Hoàn thành</c:when>
        <c:when test="${value eq 'CANCELLED'}">Đã hủy</c:when>
        <c:when test="${value eq 'PATIENT'}">Bệnh nhân</c:when>
        <c:when test="${value eq 'DOCTOR'}">Bác sĩ</c:when>
        <c:when test="${value eq 'MANAGER'}">Quản lý</c:when>
        <c:when test="${value eq 'DRAFT'}">Bản nháp</c:when>
        <c:when test="${value eq 'ISSUED'}">Đã phát hành</c:when>
        <c:when test="${value eq 'PAID'}">Đã thanh toán</c:when>
        <c:when test="${value eq 'UNPAID'}">Chưa thanh toán</c:when>
        <c:when test="${value eq 'CASH'}">Tiền mặt</c:when>
        <c:when test="${value eq 'BANK_TRANSFER'}">Chuyển khoản</c:when>
        <c:when test="${value eq 'CARD'}">Thẻ</c:when>
        <c:when test="${empty value}">—</c:when>
        <c:otherwise><c:out value="${value}"/></c:otherwise>
    </c:choose>
</span>
