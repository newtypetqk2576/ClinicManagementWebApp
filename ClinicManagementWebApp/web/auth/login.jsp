<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <title>Đăng nhập | Clinic Management</title>
        <link rel="stylesheet" href="${pageContext.request.contextPath}/css/common-layout.css">
    </head>
    <body>
        <jsp:include page="/common/header.jsp"/>
        <main class="auth-card">
            <div class="auth-card__intro">
                <p class="eyebrow">Tài khoản phòng khám</p>
                <h1>Đăng nhập</h1>
                <p>Đăng nhập để truy cập lịch hẹn và các chức năng dành cho tài khoản của bạn.</p>
                <a href="<c:url value="/"/>">← Về trang chủ</a>
            </div>
            <div class="auth-card__content">
                <c:if test="${registered}"><p role="status">Đã tạo tài khoản bệnh nhân. Bạn có thể đăng nhập ngay.</p></c:if>
                <c:if test="${not empty error}"><p role="alert"><c:out value="${error}"/></p></c:if>
                <form method="post" action="<c:url value="/login"/>">
                    <label>Tên đăng nhập
                        <input name="username" maxlength="50" autocomplete="username" required
                               value="<c:out value="${not empty error ? param.username : ''}"/>">
                    </label>
                    <label>Mật khẩu
                        <input type="password" name="password" maxlength="255" autocomplete="current-password" required>
                    </label>
                    <button type="submit">Đăng nhập</button>
                </form>
                <p>Chưa có tài khoản? <a href="<c:url value="/register"/>">Tạo tài khoản bệnh nhân</a></p>
            </div>
        </main>
    </body>
</html>
