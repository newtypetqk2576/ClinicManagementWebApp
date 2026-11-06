<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@taglib prefix="list" tagdir="/WEB-INF/tags"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Chi tiết tài khoản</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <jsp:include page="/common/navigation_bar.jsp"/>
        <main>
            <h1>Chi tiết tài khoản</h1>
            <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
            <dl>
                <dt>Mã tài khoản</dt><dd><c:out value="${user.userID}"/></dd>
                <dt>Tên đăng nhập</dt><dd><c:out value="${user.username}"/></dd>
                <dt>Mật khẩu</dt><dd><c:out value="${user.passwordHash}"/></dd>
                <dt>Vai trò</dt><dd><list:status value="${user.role}"/></dd>
                <dt>Đang hoạt động</dt><dd><c:out value="${user.active}"/></dd>
                <dt>Ngày tạo</dt><dd><list:dateTime value="${user.createdAt}"/></dd>
                <dt>Ngày cập nhật</dt><dd><list:dateTime value="${user.updatedAt}"/></dd>
            </dl>
            <c:if test="${not empty user.patient}">
                <h2>Bệnh nhân liên kết</h2>
                <dl>
                    <dt>Mã bệnh nhân</dt><dd><c:out value="${user.patient.patientID}"/></dd>
                    <dt>Họ và tên</dt><dd><c:out value="${user.patient.fullName}"/></dd>
                    <dt>Ngày sinh</dt><dd><c:out value="${user.patient.dateOfBirth}"/></dd>
                    <dt>Giới tính</dt><dd><c:out value="${user.patient.gender}"/></dd>
                    <dt>Số điện thoại</dt><dd><c:out value="${user.patient.phone}"/></dd>
                    <dt>Email</dt><dd><c:out value="${user.patient.email}"/></dd>
                    <dt>Địa chỉ</dt><dd><c:out value="${user.patient.address}"/></dd>
                    <dt>Ngày tạo</dt><dd><c:out value="${user.patient.createdAt}"/></dd>
                    <dt>Mã tài khoản</dt><dd><c:out value="${user.patient.userID}"/></dd>
                </dl>
            </c:if>
            <c:if test="${not empty user.doctor}">
                <h2>Bác sĩ liên kết</h2>
                <dl>
                    <dt>Mã bác sĩ</dt><dd><c:out value="${user.doctor.doctorID}"/></dd>
                    <dt>Họ và tên</dt><dd><c:out value="${user.doctor.fullName}"/></dd>
                    <dt>Chuyên khoa</dt><dd><c:out value="${user.doctor.specialty}"/></dd>
                    <dt>Số điện thoại</dt><dd><c:out value="${user.doctor.phone}"/></dd>
                    <dt>Đang hoạt động</dt><dd><c:out value="${user.doctor.active}"/></dd>
                    <dt>Mã tài khoản</dt><dd><c:out value="${user.doctor.userID}"/></dd>
                </dl>
            </c:if>
            <c:if test="${empty user.patient and empty user.doctor}">
                <p>Tài khoản này chưa liên kết với hồ sơ bệnh nhân hoặc bác sĩ.</p>
            </c:if>
            <h2>Chỉnh sửa tài khoản</h2>
            <c:url var="usersUrl" value="/admin/users"/>
            <form method="post" action="<c:out value="${usersUrl}"/>">
                <input type="hidden" name="action" value="update">
                <input type="hidden" name="id" value="<c:out value="${user.userID}"/>">
                <label for="username">Tên đăng nhập</label>
                <input id="username" name="username" maxlength="50" required
                       value="<c:out value="${user.username}"/>">
                <label for="password">Mật khẩu mới (bỏ trống để giữ mật khẩu hiện tại)</label>
                <input id="password" name="password" type="text" maxlength="255">
                <label for="isActive">Trạng thái</label>
                <select id="isActive" name="isActive" required>
                    <option value="true" <c:if test="${user.active}">selected</c:if>>Đang hoạt động</option>
                    <option value="false" <c:if test="${not user.active}">selected</c:if>>Ngừng hoạt động</option>
                </select>
                <button type="submit">Lưu thay đổi</button>
            </form>
            <p><a href="<c:out value="${usersUrl}"/>">Về danh sách tài khoản</a></p>
        </main>
    </body>
</html>
