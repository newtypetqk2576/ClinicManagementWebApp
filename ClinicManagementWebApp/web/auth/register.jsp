<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Tạo tài khoản bệnh nhân | Clinic Management</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <main class="auth-card">
            <div class="auth-card__intro">
                <p class="eyebrow">Dành cho bệnh nhân</p>
                <h1>Tạo tài khoản bệnh nhân</h1>
                <p>Dùng tài khoản để đặt lịch khám, theo dõi lịch hẹn và xem hồ sơ khám của bạn.</p>
                <a href="<c:url value="/"/>">← Về trang chủ</a>
            </div>
            <div class="auth-card__content">
                <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
                <c:set var="submitted" value="${not empty error ? param : null}"/>
                <form method="post" action="<c:url value="/register"/>">
                    <div class="auth-card__fields">
                        <label class="auth-card__full">Tên đăng nhập
                            <input name="username" maxlength="50" autocomplete="username" required
                                   value="<c:out value="${submitted.username}"/>">
                        </label>
                        <label>Mật khẩu
                            <input type="password" name="password" maxlength="255" autocomplete="new-password" required>
                        </label>
                        <label>Xác nhận mật khẩu
                            <input type="password" name="confirmPassword" maxlength="255" autocomplete="new-password" required>
                        </label>
                        <label>Họ và tên
                            <input name="fullName" maxlength="100" autocomplete="name" required
                                   value="<c:out value="${submitted.fullName}"/>">
                        </label>
                        <label>Giới tính
                            <select name="gender" required>
                                <option value="">Chọn giới tính</option>
                                <c:forEach var="genderOption" items="${genderOptions}">
                                    <option value="<c:out value="${genderOption}"/>"
                                            <c:if test="${submitted.gender eq genderOption}">selected</c:if>>
                                        <c:out value="${genderOption}"/>
                                    </option>
                                </c:forEach>
                            </select>
                        </label>
                        <label>Số điện thoại
                            <input type="tel" name="phone" maxlength="15" autocomplete="tel" required
                                   value="<c:out value="${submitted.phone}"/>">
                        </label>
                        <label>Email (không bắt buộc)
                            <input type="email" name="email" maxlength="100" autocomplete="email"
                                   value="<c:out value="${submitted.email}"/>">
                        </label>
                        <label class="auth-card__full">Địa chỉ (không bắt buộc)
                            <input name="address" maxlength="255" autocomplete="street-address"
                                   value="<c:out value="${submitted.address}"/>">
                        </label>
                    </div>
                    <button type="submit">Tạo tài khoản</button>
                </form>
                <p>Đã có tài khoản? <a href="<c:url value="/login"/>">Đăng nhập</a></p>
            </div>
        </main>
    </body>
</html>
