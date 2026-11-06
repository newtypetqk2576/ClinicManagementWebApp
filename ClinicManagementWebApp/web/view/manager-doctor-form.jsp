<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title><c:choose><c:when test="${createMode}">Thêm bác sĩ</c:when><c:otherwise>Chỉnh sửa bác sĩ</c:otherwise></c:choose></title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1><c:choose><c:when test="${createMode}">Thêm bác sĩ</c:when><c:otherwise>Chỉnh sửa bác sĩ</c:otherwise></c:choose></h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <c:if test="${not createMode}">
                <dl>
                    <dt>Mã bác sĩ</dt><dd><c:out value="${doctor.doctorID}"/></dd>
                    <dt>Tài khoản</dt>
                    <dd><c:choose><c:when test="${not empty doctor.userID}">Đã liên kết</c:when><c:otherwise>Chưa liên kết</c:otherwise></c:choose></dd>
                </dl>
            </c:if>
            <c:url var="doctorsUrl" value="/admin/doctors"/>
            <form method="post" action="<c:out value="${doctorsUrl}"/>">
                <input type="hidden" name="action" value="${createMode ? 'create' : 'update'}">
                <c:if test="${not createMode}">
                    <input type="hidden" name="id" value="<c:out value="${doctor.doctorID}"/>">
                </c:if>
                <label for="fullName">Họ và tên</label>
                <input id="fullName" name="fullName" maxlength="100" required
                       value="<c:out value="${doctor.fullName}"/>">
                <label for="specialty">Chuyên khoa</label>
                <input id="specialty" name="specialty" maxlength="100" required
                       value="<c:out value="${doctor.specialty}"/>">
                <label for="phone">Số điện thoại</label>
                <input id="phone" name="phone" maxlength="15"
                       value="<c:out value="${doctor.phone}"/>">
                <label for="isActive">Trạng thái</label>
                <select id="isActive" name="isActive" required>
                    <option value="true" <c:if test="${activeValue eq 'true'}">selected</c:if>>Đang hoạt động</option>
                    <option value="false" <c:if test="${activeValue eq 'false'}">selected</c:if>>Ngừng hoạt động</option>
                </select>
                <button type="submit"><c:choose><c:when test="${createMode}">Thêm bác sĩ</c:when><c:otherwise>Lưu thay đổi</c:otherwise></c:choose></button>
            </form>
            <p><a href="<c:out value="${doctorsUrl}"/>">Về danh sách bác sĩ</a></p>
        </main>
    </body>
</html>
